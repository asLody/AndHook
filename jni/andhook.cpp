/*
 *	
 *	@author : rrrfff@foxmail.com
 *
 */
#include <jni.h>
#include <dlfcn.h>
#include <sys/system_properties.h>
#include <sys/atomics.h>
#include <android/log.h>
#include "dalvik_vm.h"
#define MAX_BACKUP_SLOTS          32
#define MAX_PARAMS_ALLOWED        8
#define LOGV(...)                 ((void)__android_log_print(ANDROID_LOG_VERBOSE, __FUNCTION__, __VA_ARGS__))
#define LOGD(...)                 ((void)__android_log_print(ANDROID_LOG_DEBUG, __FUNCTION__, __VA_ARGS__))
#define LOGI(...)                 ((void)__android_log_print(ANDROID_LOG_INFO, __FUNCTION__, __VA_ARGS__))
#define LOGW(...)                 ((void)__android_log_print(ANDROID_LOG_WARN, __FUNCTION__, __VA_ARGS__))
#define LOGE(...)                 ((void)__android_log_print(ANDROID_LOG_ERROR, __FUNCTION__, __VA_ARGS__))
#define LOGW(...)                 ((void)__android_log_print(ANDROID_LOG_WARN, __FUNCTION__, __VA_ARGS__))
#define LOGF(...)                 ((void)__android_log_print(ANDROID_LOG_FATAL, __FUNCTION__, __VA_ARGS__))
#define __LIBC_INLINE__           __attribute__((always_inline))
#define LIKELY(exp)               (__builtin_expect((exp) != 0, true))
#define UNLIKELY(exp)             (__builtin_expect((exp) != 0, false))
#define METHOD_PTR(p)             reinterpret_cast<uintptr_t>(p)
#define NATIVE_METHOD(n, s)       #n, s, reinterpret_cast<void *>(n)
#define PLoad(lib)                ::dlopen(lib, RTLD_LAZY)
#define PFree(handle)             ::dlclose(handle)
#define PInvoke(so, symbol)       __PInvoke<__typeof__(symbol)>(so, SYMBOL_##symbol, #symbol)
template <typename func> __LIBC_HIDDEN__ __LIBC_INLINE__ func *__PInvoke(void *handle, const char *mangled_symbol, const char *symbol)
{
	void *sym = ::dlsym(handle, mangled_symbol);
	if (UNLIKELY(sym == NULL)) sym = ::dlsym(handle, symbol);
	return reinterpret_cast<func *>(sym);
}

//-------------------------------------------------------------------------

static volatile int g_slotindex = 0;
static void        *g_dvm;
static Method       g_backups[MAX_BACKUP_SLOTS];
static __typeof__(&dvmFindPrimitiveClass)  g_dvmFindPrimitiveClass;
static __typeof__(&dvmBoxPrimitive)        g_dvmBoxPrimitive;
static __typeof__(&dvmUnboxPrimitive)      g_dvmUnboxPrimitive;
static __typeof__(&dvmDecodeIndirectRef)   g_dvmDecodeIndirectRef;
static __typeof__(&dvmReleaseTrackedAlloc) g_dvmReleaseTrackedAlloc;
static __typeof__(&dvmThreadSelf)          g_dvmThreadSelf;

//-------------------------------------------------------------------------

static int __native_hook(JNIEnv *env, Method *vm_origin, jobject target)
{
//	Method *vm_origin = reinterpret_cast<Method *>(env->FromReflectedMethod(origin));
	Method *vm_target = reinterpret_cast<Method *>(env->FromReflectedMethod(target));

//	Dalvik puts private, static, and constructors into non-virtual table
//	g_dvmIsDirectMethod(vm_origin)
	if (IS_METHOD_FLAG_SET(vm_origin, ACC_PRIVATE) || 
		IS_METHOD_FLAG_SET(vm_origin, ACC_STATIC) || *vm_origin->name == '<') {
		LOGI("%s: hook direct %s%s -> %s%s", __FUNCTION__,
			 vm_origin->clazz->descriptor, vm_origin->name,
			 vm_target->clazz->descriptor, vm_target->name);
		*vm_origin = *vm_target;
		return 1;
	} //if

	if (IS_METHOD_FLAG_SET(vm_origin, ACC_ABSTRACT) ||
		vm_origin->methodIndex >= vm_origin->clazz->vtableCount) {
		LOGE("%s: hook failed %s%s -> %s%s", __FUNCTION__,
			 vm_origin->clazz->descriptor, vm_origin->name,
			 vm_target->clazz->descriptor, vm_target->name);
		return -1;
	} //if

	LOGI("%s: hook virtual %s%s -> %s%s", __FUNCTION__,
		 vm_origin->clazz->descriptor, vm_origin->name,
		 vm_target->clazz->descriptor, vm_target->name);
	vm_origin->clazz->vtable[vm_origin->methodIndex] = vm_target;
	return 0;
}

//-------------------------------------------------------------------------

static void JNICALL dvmHookNativeNoBackup(JNIEnv *env, jclass, jobject origin, jobject target)
{
	Method *vm_origin = reinterpret_cast<Method *>(env->FromReflectedMethod(origin));
	__native_hook(env, vm_origin, target);
}

//-------------------------------------------------------------------------

static jint JNICALL dvmHookNative(JNIEnv *env, jclass, jobject origin, jobject target)
{
	int slot = __atomic_inc(&g_slotindex);
	if (UNLIKELY(slot < 0 || slot >= NELEM(g_backups))) {
		LOGW("%s: slots limit exceeded %d", __FUNCTION__, slot);
		return -1;
	} //if

	Method *vm_origin = reinterpret_cast<Method *>(env->FromReflectedMethod(origin));
	g_backups[slot]   = *vm_origin;
	
	if (__native_hook(env, vm_origin, target) == 0) {
		CLEAR_METHOD_FLAG(&g_backups[slot], ACC_PUBLIC);
		SET_METHOD_FLAG(&g_backups[slot], ACC_PRIVATE);
	} //if

	return slot;
}

//-------------------------------------------------------------------------

static jvalue JNICALL __invoke_backup(JNIEnv *env, jint slot, jobject receiver, jobjectArray params)
{
	jvalue ret; ret.j = 0L;

	if (UNLIKELY(slot < 0 || slot >= NELEM(g_backups))) {
		LOGW("%s: slots limit exceeded %d", __FUNCTION__, slot);
		return ret;
	} //if

	jvalue ps[MAX_PARAMS_ALLOWED];
	int np = env->GetArrayLength(params);
	if (UNLIKELY(np >= NELEM(ps))) {
		LOGW("%s: params limit exceeded %d", __FUNCTION__, np);
		return ret;
	} //if

	auto shorty = g_backups[slot].shorty;
	auto self   = g_dvmThreadSelf();
	while (--np >= 0) {
		ps[np].l = env->GetObjectArrayElement(params, np);
		if (shorty[np + 1] == 'L') continue; // 'shorty' descr uses L for all refs, incl array

		auto typeCls = g_dvmFindPrimitiveClass(shorty[np + 1]);
		if (typeCls != NULL) {
			auto obj = g_dvmDecodeIndirectRef(self, ps[np].l);
			if (UNLIKELY(obj == NULL)) {
				LOGW("%s: decode indirect ref %p failed", __FUNCTION__, ps[np].l);
				return ret;
			} //if
			if (UNLIKELY(!g_dvmUnboxPrimitive(obj, typeCls, reinterpret_cast<JValue *>(&ps[np])))) {
				LOGW("%s: unbox primitive param %p failed", __FUNCTION__, obj);
				return ret;
			} //if
		} //if		
	}

	auto originalMethod  = reinterpret_cast<jmethodID>(&g_backups[slot]);
	bool isStaticMethod  = IS_METHOD_FLAG_SET(&g_backups[slot], ACC_STATIC);
//	ClassObject *retType = g_dvmFindPrimitiveClass(shorty[0]);
	switch (shorty[0]) {
	case 'V':
		isStaticMethod ?
			env->CallStaticVoidMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallVoidMethodA(receiver, originalMethod, ps);
		return ret;
	case 'Z':
		ret.z = isStaticMethod ?
			env->CallStaticBooleanMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallBooleanMethodA(receiver, originalMethod, ps);
		break;
	case 'B':
		ret.b = isStaticMethod ?
			env->CallStaticByteMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallByteMethodA(receiver, originalMethod, ps);
		break;
	case 'S':
		ret.s = isStaticMethod ?
			env->CallStaticShortMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallShortMethodA(receiver, originalMethod, ps);
		break;
	case 'C':
		ret.c = isStaticMethod ?
			env->CallStaticCharMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallCharMethodA(receiver, originalMethod, ps);
		break;
	case 'I':
		ret.i = isStaticMethod ?
			env->CallStaticIntMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallIntMethodA(receiver, originalMethod, ps);
		break;
	case 'J':
		ret.j = isStaticMethod ?
			env->CallStaticLongMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallLongMethodA(receiver, originalMethod, ps);
		break;
	case 'F':
		ret.f = isStaticMethod ?
			env->CallStaticFloatMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallFloatMethodA(receiver, originalMethod, ps);
		break;
	case 'D':
		ret.d = isStaticMethod ?
			env->CallStaticDoubleMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallDoubleMethodA(receiver, originalMethod, ps);
		break;
	default: // objects case 'L' and '['
		ret.l = isStaticMethod ?
			env->CallStaticObjectMethodA(reinterpret_cast<jclass>(receiver), originalMethod, ps) :
			env->CallObjectMethodA(receiver, originalMethod, ps);
	}

//	auto obj = g_dvmBoxPrimitive(ret, retType);
//	return g_dvmReleaseTrackedAlloc(obj, self), g_addLocalReference(self, reinterpret_cast<Object *>(obj));

	return ret;
}

//-------------------------------------------------------------------------

static void JNICALL dvmInvokeVoidMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	__invoke_backup(env, slot, receiver, params);
}

//-------------------------------------------------------------------------

static jboolean JNICALL dvmInvokeBooleanMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).z;
}

//-------------------------------------------------------------------------

static jbyte JNICALL dvmInvokeByteMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).b;
}

//-------------------------------------------------------------------------

static jshort JNICALL dvmInvokeShortMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).s;
}

//-------------------------------------------------------------------------

static jchar JNICALL dvmInvokeCharMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).c;
}

//-------------------------------------------------------------------------

static jint JNICALL dvmInvokeIntMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).i;
}

//-------------------------------------------------------------------------

static jlong JNICALL dvmInvokeLongMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).j;
}

//-------------------------------------------------------------------------

static jfloat JNICALL dvmInvokeFloatMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).f;
}

//-------------------------------------------------------------------------

static jdouble JNICALL dvmInvokeDoubleMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).d;
}

//-------------------------------------------------------------------------

static jobject JNICALL dvmInvokeObjectMethod(JNIEnv *env, jclass, jint slot, jobject receiver, jobjectArray params)
{
	return __invoke_backup(env, slot, receiver, params).l;
}

//-------------------------------------------------------------------------

extern "C" {
	JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *unused)
	{
// 		char sdkVer[PROP_VALUE_MAX];
// 		__system_property_get("ro.build.version.sdk", sdkVer);
// 		if (atoi(sdkVer) <= 19) {
// 		} //if
		
		g_dvm = PLoad("libdvm.so");
		if (UNLIKELY(g_dvm == NULL)) {
			LOGE("%s: failed to load libdvm.so", __FUNCTION__);
			return JNI_ERR;
		} //if

		g_dvmFindPrimitiveClass  = PInvoke(g_dvm, dvmFindPrimitiveClass);
		g_dvmBoxPrimitive        = PInvoke(g_dvm, dvmBoxPrimitive);
		g_dvmUnboxPrimitive      = PInvoke(g_dvm, dvmUnboxPrimitive);
		g_dvmDecodeIndirectRef   = PInvoke(g_dvm, dvmDecodeIndirectRef);
		g_dvmReleaseTrackedAlloc = PInvoke(g_dvm, dvmReleaseTrackedAlloc);
		g_dvmThreadSelf          = PInvoke(g_dvm, dvmThreadSelf);
		if (UNLIKELY(!(METHOD_PTR(g_dvmFindPrimitiveClass) & METHOD_PTR(g_dvmBoxPrimitive) & 
					   METHOD_PTR(g_dvmUnboxPrimitive) & METHOD_PTR(g_dvmDecodeIndirectRef) &
					   METHOD_PTR(g_dvmReleaseTrackedAlloc) & METHOD_PTR(g_dvmThreadSelf)))) { // bugs?
			LOGI("%p %p %p %p", g_dvmFindPrimitiveClass, g_dvmBoxPrimitive,
				 g_dvmReleaseTrackedAlloc, g_dvmThreadSelf);
			LOGE("%s: failed to locate symbols", __FUNCTION__);
			return JNI_ERR;
		} //if

		JNIEnv *env;
		if (UNLIKELY(vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK)) {
			LOGE("%s: failed to obtain env", __FUNCTION__);
			return JNI_EVERSION;
		} //if
		
// 		jclass clazz = env->FindClass("com/xyapp/fuckit/MainActivity");
// 		JNINativeMethod gMethods[] = {
// 			{ NATIVE_METHOD(dvmDebug, "(Lcom/xyapp/fuckit/MainActivity;Landroid/telephony/TelephonyManager;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V") },
// 		};
		jclass clazz = env->FindClass("io/virtualhook/DalvikHook");
		JNINativeMethod gMethods[] = {
			{ NATIVE_METHOD(dvmHookNativeNoBackup, "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V") },
			{ NATIVE_METHOD(dvmHookNative, "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)I") },
			{ NATIVE_METHOD(dvmInvokeVoidMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)V") },
			{ NATIVE_METHOD(dvmInvokeBooleanMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)Z") },
			{ NATIVE_METHOD(dvmInvokeByteMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)B") },
			{ NATIVE_METHOD(dvmInvokeShortMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)S") },
			{ NATIVE_METHOD(dvmInvokeCharMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)C") },
			{ NATIVE_METHOD(dvmInvokeIntMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)I") },
			{ NATIVE_METHOD(dvmInvokeLongMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)J") },
			{ NATIVE_METHOD(dvmInvokeFloatMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)F") },
			{ NATIVE_METHOD(dvmInvokeDoubleMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)D") },
			{ NATIVE_METHOD(dvmInvokeObjectMethod, "(ILjava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;") },
		};
		if (UNLIKELY(clazz == NULL || env->RegisterNatives(clazz, gMethods, NELEM(gMethods)) < 0)) {
			LOGE("%s: failed to register natives", __FUNCTION__);
			return JNI_ERR;
		} //if
		env->DeleteLocalRef(clazz);

		return JNI_VERSION_1_6;
	}
	JNIEXPORT void JNICALL JNI_OnUnLoad(JavaVM *jvm, void *reserved)
	{
		if (LIKELY(g_dvm != NULL)) {
			PFree(g_dvm);
		} //if
	}
}