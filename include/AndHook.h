/*
 *
 *	@author : rrrfff@foxmail.com
 *  https://github.com/rrrfff/AndHook
 *
 */
#pragma once
#include <jni.h>
#include <stdint.h>
#include <unistd.h>

#define JNI_METHOD_SIZE 132u

#ifdef __cplusplus
extern "C" {
#endif

	// native
	void MSHookFunction(void *symbol, void *replace, void **result);
	// java
	intptr_t JAVAHookFunction(JNIEnv *env, jclass clazz, const char *method, const char *signature,
							  void *replace);
	jmethodID GetMethodID(JNIEnv *env, intptr_t backup, void *buffer/* JNI_METHOD_SIZE */);
	// java internal
	intptr_t  SetNativeMethod(jmethodID origin);
	// force interpreter, art only
	void Deoptimize(jmethodID outer);

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