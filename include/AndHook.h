/*
 *
 *	@author : rrrfff@foxmail.com
 *  @date   : 2017/09/27
 *  https://github.com/rrrfff/AndHook
 *
 */
#pragma once
#include <jni.h>
#include <stdint.h>
#include <unistd.h>

#define ANDROID_RUNTIME ((const char *)-1)
#define JNI_METHOD_SIZE 132u

#ifdef __cplusplus
extern "C" {
#endif

	/// <summary>
	/// Gets handle to specified module or NULL if the target is not currently loaded
	/// </summary>
	const void *MSGetImageByName(const char *name/* = ANDROID_RUNTIME*/);
	/// <summary>
	/// Gets the addresses of defined symbols or NULL
	/// </summary>
	void *MSFindSymbol(const void *handle, const char *symbol);
	/// <summary>
	/// Releases internal memory without affecting the module state
	/// </summary>
	void MSCloseImage(const void *handle);
	/// <summary>
	/// Intercepts native methods
	/// </summary>
	void MSHookFunction(void *symbol, void *replace, void **result/* = NULL*/);
	/// <summary>
	/// Intercepts java methods using native code
	/// </summary>
	void MSJavaHookMethod(JNIEnv *env, jclass clazz, const char *method, const char *signature,
						  void *replace, intptr_t *result/* = NULL*/);
	/// <summary>
	/// Intercepts java methods using native code 
	/// </summary>
	void MSJavaHookMethodV(JNIEnv *env, jclass clazz, jmethodID methodId, const char *method, const char *signature,
						   void *replace, intptr_t *result/* = NULL*/);
	/// <summary>
	/// Gets the underlying jmethodID which can be used to call original function
	/// </summary>
	jmethodID GetMethodID(JNIEnv *env, intptr_t backup, void *buffer/* JNI_METHOD_SIZE */);
	/// <summary>
	/// Marks the specified java method as native and returns backup-slot index
	/// </summary>
	intptr_t SetNativeMethod(jmethodID origin, intptr_t should_backup);
	/// <summary>
	/// Forces the specified java method to be executed in the interpreter [ART only]
	/// </summary>
	void DeoptimizeMethod(jmethodID method);
	/// <summary>
	/// Dumps all the virtual and direct methods of the specified class [Dalvik only]
	/// </summary>
	void DumpClassMethods(JNIEnv *env, jclass clazz/* = NULL*/, const char *clsname/* = NULL*/);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
template<class T> class NativeHook
{
public:
	union {
		T         r;
		void     *p;
		uintptr_t v;
	};

public:
	NativeHook()  = default;
	void operator = (const NativeHook &) = delete;
	NativeHook(const NativeHook &)       = default;
	NativeHook(T func) {
		this->update(func);
	}
	NativeHook(const void *func) {
		this->update(func);
	}
	__always_inline void operator = (T my_func) {
		this->redirect(my_func);
	}

public:
	__always_inline void redirect(T my_func) {
		union {
			T     f;
			void *fp;
		}; 
		f = my_func;
		MSHookFunction(this->p, fp, &this->p);
	}
	template<class... P> __always_inline auto invoke(P &&... args) {
//		return this->r(std::forward<P>(args)...);
		return this->r(args...);
	}
	__always_inline T get() {
		return this->r;
	}
	__always_inline void update(T fr) {
		this->r = fr;
	}
	__always_inline void update(const void *fp) {
		this->p = const_cast<void *>(fp);
	}
	__always_inline void update(unsigned int fv) {
		this->v = fv;
	}
};
class AndHook
{
public: // native
	template<typename T> static __always_inline NativeHook<T> pre_hook(const void *ptr) {
		return NativeHook<T>(ptr);
	}
	template<typename T> static __always_inline NativeHook<T> pre_hook(T func) {
		return NativeHook<T>(__ptr(func));
	}
	template<typename T> static __always_inline NativeHook<T> pre_hook(T, const void *ptr) {
		return NativeHook<T>(ptr);
	}

public:

};
#endif // __cplusplus