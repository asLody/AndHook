/*
 *
 *  @author : rrrfff@foxmail.com
 *  @date   : 2017/12/25
 *  https://github.com/rrrfff/AndHook
 *
 */
#pragma once
#include <jni.h>
#include <stdint.h>
#include <unistd.h>

#define AK_ANDROID_RUNTIME  ((const char *)-1)
#define AK_RWX              (PROT_READ | PROT_WRITE | PROT_EXEC)

#ifdef __cplusplus
# define AK_BOOL bool
extern "C" {
#else
# define AK_BOOL int
#endif

    /// <summary>
    /// Gets handle to specified module or NULL if the target is not currently loaded
    /// </summary>
    const void *AKGetImageByName(const char *name/* = AK_ANDROID_RUNTIME*/);
    /// <summary>
    /// Gets the address of defined symbols or NULL
    /// </summary>
    void *AKFindSymbol(const void *handle, const char *symbol);
    /// <summary>
    /// Gets the base address of defined symbols or NULL
    /// </summary>
    void *AKGetBaseAddress(const void *handle);
    /// <summary>
    /// Releases internal memory (without affecting the module state)
    /// </summary>
    void AKCloseImage(const void *handle);
    /// <summary>
    /// Intercepts native method
    /// </summary>
    void AKHookFunction(const void *symbol, const void *replace, void **result/* = NULL*/);
    /// <summary>
    /// Intercepts native method and writes trampoline to rwx.
    /// @warning rwx should be aligned properly and large enough to hold the trampoline
    /// </summary>
    void *AKHookFunctionV(const void *symbol, const void *replace, void *rwx, const uintptr_t size/* = 64*/);
    /// <summary>
    /// Sets protection on the specified region of memory.
    /// @addr is NOT necessary to be aligned to a page boundary
    /// </summary>
    AK_BOOL AKProtectMemory(const void *addr, uintptr_t len, int prot/* = AK_RWX*/);
    /// <summary>
    /// Patches the specified region of memory
    /// </summary>
    AK_BOOL AKPatchMemory(const void *addr, const void *data, uintptr_t len);
    /// <summary>
    /// Intercepts java method using native code
    /// </summary>
    void AKJavaHookMethod(JNIEnv *env, jclass clazz, const char *method, const char *signature,
                          const void *replace, jmethodID *result/* = NULL*/);
    /// <summary>
    /// Intercepts java method using native code 
    /// </summary>
    void AKJavaHookMethodV(jmethodID methodId, const void *replace, jmethodID *result/* = NULL*/);
    /// <summary>
    /// Marks the specified java method as native (if not) and backups original method if result != NULL
    /// </summary>
    AK_BOOL AKForceNativeMethod(jmethodID methodId, const void *jni_entrypoint, AK_BOOL fast_native/* = false*/,
                                jmethodID *result/* = NULL*/);
    /// <summary>
    /// Copies the specified java method.
    /// @warning The result method's access flags may be changed if the original method is virtual
    /// </summary>
    AK_BOOL AKShadowCopyMethod(jmethodID methodId, jmethodID *result);
    /// <summary>
    /// Registers the native method and returns the new entry point. NB The returned entry point might
    /// be different from the native_method argument if some MethodCallback modifies it.
    /// @warning This function provided is intended only for Android O or later,
    /// as there are possible runtime callbacks that we should notify.
    /// </summary>
    const void *AKRegisterNative(jmethodID methodId, const void *native_method, bool fast_native/* = false*/);
    /// <summary>
    /// Restores java method from backup
    /// </summary>
    AK_BOOL AKRestoreMethod(jmethodID j_backup, jmethodID methodId);
    /// <summary>
    /// Sets the specified java method's entrypoint to its OAT code or forces JIT compilation if available [ART only]
    /// </summary>
    void AKOptimizeMethod(jmethodID method);
    /// <summary>
    /// Triggers JIT compilation if available [ART only, since API 24].
    /// @warning Before API 27, the JIT compiler cannot compile native jni method
    /// </summary>
    AK_BOOL AKForceJitCompile(jmethodID method);
    /// <summary>
    /// Forces the specified java method to be executed in the interpreter [ART only]
    /// </summary>
    void AKDeoptimizeMethod(jmethodID method);
    /// <summary>
    /// Dumps all the virtual and direct methods of the specified class
    /// </summary>
    void AKDumpClassMethods(JNIEnv *env, jclass clazz/* = NULL*/, const char *clsname/* = NULL*/);
    /// <summary>
    /// Ensures all threads running Java suspend and that those not running Java don't start
    /// </summary>
    AK_BOOL AKLockJavaThreads();
    /// <summary>
    /// Unlocks and broadcasts a notification to all threads interrupted by AKLockJavaThreads
    /// </summary>
    void AKUnlockJavaThreads();
    /// <summary>
    /// Suspends all threads in the current process except the calling one.
    /// This function works by spawning a Linux task which then attaches to every
    /// thread in the caller process with ptrace.
    /// @warning Calling a library function while threads are suspended could cause a
    /// deadlock, if one of the treads happens to be holding a libc lock
    /// </summary>
    AK_BOOL AKSuspendAllThreads();
    /// <summary>
    /// Resumes all threads suspended by AKSuspendAllThreads
    /// </summary>
    void AKResumeAllThreads();
    /// <summary>
    /// Returns the user-visible SDK version of Android framework
    /// </summary>
    int AKGetSdkVersion();
    /// <summary>
    /// Returns the absolute path to the application specific cache directory with trailing slash
    /// </summary>
    const char *AKGetCacheDirectory();
    /// <summary>
    /// Returns the absolute path to the application specific libs directory with trailing slash
    /// </summary>
    const char *AKGetNativeLibsDirectory();
    /// <summary>
    /// Enables or disables dex fast-loading mechanism [ART only]
    /// </summary>
    void AKEnableFastDexLoad(AK_BOOL enable);

#ifdef __cplusplus
}
#endif
