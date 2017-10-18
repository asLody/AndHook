/*
 *
 *  @author : rrrfff@foxmail.com
 *  @date   : 2017/10/17
 *  https://github.com/rrrfff/AndHook
 *
 */
#pragma once
#include <jni.h>
#include <stdint.h>
#include <unistd.h>

#define ANDROID_RUNTIME  ((const char *)-1)
#define JNI_METHOD_SIZE  132u

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
	jmethodID MSGetMethodID(JNIEnv *env, intptr_t backup, void *buffer/* JNI_METHOD_SIZE */);
	/// <summary>
	/// Marks the specified java method as native (if not) and returns backup-slot index
	/// </summary>
	intptr_t MSForceNativeMethod(jmethodID origin, void ***jni_entrypoint, bool should_backup);
	/// <summary>
	/// Forces the specified java method to be executed in the interpreter [ART only]
	/// </summary>
	void MSDeoptimizeMethod(jmethodID method);
	/// <summary>
	/// Dumps all the virtual and direct methods of the specified class
	/// </summary>
	void MSDumpClassMethods(JNIEnv *env, jclass clazz/* = NULL*/, const char *clsname/* = NULL*/);
	/// <summary>
	/// Ensures all threads running Java suspend and that those not running Java don't start
	/// </summary>
	bool MSLockJavaThreads();
	/// <summary>
	/// Unlocks and broadcasts a notification to all threads interrupted by MSLockJavaThreads
	/// </summary>
	void MSUnlockJavaThreads();
	/// <summary>
	/// Suspends all threads in the current process except the calling one.
	/// This function works by spawning a Linux task which then attaches to every
	/// thread in the caller process with ptrace.
	/// @warning calling a library function while threads are suspended could cause a
	/// deadlock, if one of the treads happens to be holding a libc lock
	/// </summary>
	bool MSSuspendAllThreads();
	/// <summary>
	/// Resumes all threads suspended by MSSuspendAllThreads
	/// </summary>
	void MSResumeAllThreads();
	/// <summary>
	/// Returns the absolute path to the application specific cache directory with trailing slash
	/// </summary>
	const char *MSGetCacheDirectory();
	/// <summary>
	/// Enables or disables dex fast-loading mechanism [ART only]
	/// </summary>
	void MSEnableFastDexLoad(bool enable);

#ifdef __cplusplus
}
#endif