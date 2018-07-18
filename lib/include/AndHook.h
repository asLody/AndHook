/*
 *
 *  @author : Rprop (r_prop@outlook.com)
 *  @date   : 2018/07/19
 *  https://github.com/Rprop/AndHook
 *
 */
#pragma once
#include <jni.h>
#include <stdint.h>
#include <unistd.h>
#include <sys/mman.h>

#define AK_ANDROID_RUNTIME  NULL
#define AK_RWX              (PROT_READ | PROT_WRITE | PROT_EXEC)
#ifdef __cplusplus
# define AK_BOOL            bool
# define AK_DEFAULT(v)      = v
extern "C" {
#else
# define AK_BOOL            char
# define AK_DEFAULT(v)
#endif

    /// <summary>
    /// Loads the specified module using system linker or NULL if failed.
    /// @warning `caller_addr` is necessary for loading system libraries since Android N
    /// </summary>
    const void *AKLoadImage(const char *path, int flag, const void *caller_addr AK_DEFAULT(NULL));
    /// <summary>
    /// Gets handle to specified module or NULL if the target is not currently loaded
    /// </summary>
    const void *AKGetImageByName(const char *name AK_DEFAULT(AK_ANDROID_RUNTIME));
    /// <summary>
    /// Gets the address of defined symbol or NULL
    /// </summary>
    void *AKFindSymbol(const void *handle, const char *symbol);
    /// <summary>
    /// Gets the address of anonymous symbol or NULL
    /// </summary>
    void *AKFindAnonymity(const void *handle, const uintptr_t st_value);
    /// <summary>
    /// Gets the base address of defined module or NULL
    /// </summary>
    void *AKGetBaseAddress(const void *handle);
    /// <summary>
    /// Releases internal memory (without affecting the module state)
    /// </summary>
    void AKCloseImage(const void *handle);
    /// <summary>
    /// Intercepts native method
    /// </summary>
    void AKHookFunction(const void *symbol, const void *replace, void **result AK_DEFAULT(NULL));
    /// <summary>
    /// Intercepts native method and writes trampoline to rwx.
    /// @warning `rwx` should be aligned properly and large enough to hold the trampoline
    /// </summary>
    void *AKHookFunctionV(const void *symbol, const void *replace, void *rwx, const uintptr_t size AK_DEFAULT(64));
    /// <summary>
    /// Intercepts native method if possible and writes trampoline to rwx.
    /// @warning `rwx` should be aligned properly and large enough to hold the trampoline
    /// </summary>
    void *AKHookFunctionEx(const void *symbol, const uintptr_t overwritable, const void *replace,
                           void *rwx, const uintptr_t size AK_DEFAULT(64));
    /// <summary>
    /// Creates a wrapper function of `func` to preserve the contents of the registers.
    /// @warning For x86 and x86_64, this function does nothing and simply returns the original `func`
    /// </summary>
    void *AKWrapFunction(const void *func);
    /// <summary>
    /// Prints the specified region of memory to the log
    /// </summary>
    void AKPrintHexBinary(const void *addr, uintptr_t len, const char *name AK_DEFAULT(NULL));
    /// <summary>
    /// Sets protection on the specified region of memory.
    /// `addr` is NOT necessary to be aligned to a page boundary
    /// </summary>
    AK_BOOL AKProtectMemory(const void *addr, uintptr_t len, int prot AK_DEFAULT(AK_RWX));
    /// <summary>
    /// Sets protection and patches the specified region of memory
    /// </summary>
    AK_BOOL AKPatchMemory(const void *addr, const void *data, uintptr_t len);
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
    /// Enables or disables dex fast-loading mechanism [ART only]
    /// </summary>
    void AKEnableFastDexLoad(AK_BOOL enable);
    /// <summary>
    /// Retrieves the last build date
    /// </summary>
    const char *AKLastBuildDate();

    /// <summary>
    /// Performs the basic initialization for java-related api.
    /// The function ensures that this initialization occurs only once,
    /// even when multiple threads may attempt the initialization
    /// </summary>
    jint AKInitializeOnce(JNIEnv *env AK_DEFAULT(NULL), JavaVM *jvm AK_DEFAULT(NULL));
    /// <summary>
    /// Registers internal native methods with the specified classloader that loads the library
    /// </summary>
    void AKRegisterLibrary(JNIEnv *env, jobject classloader);
    /// <summary>
    /// Intercepts java method using native code
    /// </summary>
    void AKJavaHookMethod(JNIEnv *env, jclass clazz, const char *method, const char *signature,
                          const void *replace, jmethodID *result AK_DEFAULT(NULL));
    /// <summary>
    /// Intercepts java method using native code
    /// </summary>
    void AKJavaHookMethodV(JNIEnv *env, jmethodID methodId, const void *replace, jmethodID *result AK_DEFAULT(NULL));
    /// <summary>
    /// Marks the specified java method as native (if not) and backups original method if `result` != NULL.
    /// @warning The method must not be inlined or in any threads call stack
    /// </summary>
    AK_BOOL AKForceNativeMethod(JNIEnv *env, jmethodID methodId, const void *jni_entrypoint, AK_BOOL fast_native AK_DEFAULT(0),
                                jmethodID *result AK_DEFAULT(NULL));
    /// <summary>
    /// Copies the specified java method.
    /// @warning The result method's access flags may be changed if the original method is virtual
    /// </summary>
    AK_BOOL AKShadowCopyMethod(JNIEnv *env, jmethodID methodId, jmethodID *result);
    /// <summary>
    /// Restores java method from backup
    /// </summary>
    AK_BOOL AKRestoreMethod(jmethodID j_backup, jmethodID j_dest);
    /// <summary>
    /// Registers the native method and returns the new entry point. 
    /// The returned entry point might be different from the native_method argument 
    /// if some MethodCallback modifies it.
    /// @warning This function provided is intended only for Android O or later,
    /// as there are possible runtime callbacks that we should notify
    /// </summary>
    const void *AKRegisterNative(jmethodID methodId, const void *native_method, AK_BOOL fast_native AK_DEFAULT(0));
    /// <summary>
    /// Gets the entry of the specified native method or NULL if the method is not native or has not yet been registered
    /// </summary>
    void *AKGetNativeEntry(jmethodID methodId, AK_BOOL *critical_native AK_DEFAULT(NULL));
    /// <summary>
    /// Creates a DexClassLoader that loads classes from .jar and .apk files containing a classes.dex entry.
    /// This function never throws exceptions.
    /// @warning parameter `cache_dir` is deprecated and has no effect since Android O
    /// </summary>
    jobject AKLoadFileDex(JNIEnv *env, const char *dex_path, const char *cache_dir AK_DEFAULT(NULL),
                          const char *lib_path AK_DEFAULT(NULL), jobject parent AK_DEFAULT(NULL));
    /// <summary>
    /// Creates a PathClassLoader that operates on two given lists of files and directories.
    /// This function never throws exceptions
    /// </summary>
    jobject AKLoadPathDex(JNIEnv *env, const char *dex_path,
                          const char *lib_path AK_DEFAULT(NULL), jobject parent AK_DEFAULT(NULL));
    /// <summary>
    /// Creates a InMemoryClassLoader that loads classes from a buffer containing a DEX file [ART only, since API 26].
    /// This function never throws exceptions
    /// </summary>
    jobject AKLoadMemoryDex(JNIEnv *env, const void *buffer, jlong capacity, jobject parent AK_DEFAULT(NULL));
    /// <summary>
    /// Loads a locally-defined class with the specified classloader or the system ClassLoader
    /// This function never throws exceptions
    /// </summary>
    jclass AKLoadClass(JNIEnv *env, jobject classloader, const char *name, jthrowable *exception AK_DEFAULT(NULL));
    /// <summary>
    /// Returns the system ClassLoader which represents the CLASSPATH
    /// </summary>
    jobject AKGetSystemClassLoader(JNIEnv *env);
    /// <summary>
    /// Returns the ClassLoader which loads the specified class
    /// </summary>
    jobject AKGetClassLoader(JNIEnv *env, jclass clazz);
    /// <summary>
    /// Returns the context ClassLoader associated with the specified Context instance  
    /// </summary>
    jobject AKGetContextClassLoader(JNIEnv *env, jobject context);
    /// <summary>
    /// Returns the user-visible SDK version of Android framework
    /// </summary>
    int AKGetSdkVersion();
    /// <summary>
    /// Returns the absolute path to the application specific cache directory with trailing slash
    /// </summary>
    const char *AKGetCacheDirectory();
    /// <summary>
    /// Sets the absolute path to the cache directory
    /// </summary>
    const char *AKSetCacheDirectory(const char *path);
    /// <summary>
    /// Returns the absolute path to the application specific libs directory with trailing slash
    /// </summary>
    const char *AKGetNativeLibsDirectory();
    /// <summary>
    /// Sets the specified java method's entrypoint to its OAT code or forces JIT compilation if available [ART only]
    /// </summary>
    void AKOptimizeMethod(jmethodID method);
    /// <summary>
    /// Triggers JIT compilation if available [ART only, since API 24].
    /// @warning Before Android P, the JIT compiler cannot compile native jni method
    /// </summary>
    AK_BOOL AKForceJitCompile(jmethodID method);
    /// <summary>
    /// Forces the specified java method to be executed in the interpreter [ART only]
    /// </summary>
    void AKDeoptimizeMethod(jmethodID method);
    /// <summary>
    /// Dumps all the virtual and direct methods of the specified class
    /// </summary>
    void AKDumpClassMethods(JNIEnv *env, jclass clazz AK_DEFAULT(NULL), const char *clsname AK_DEFAULT(NULL));
    /// <summary>
    /// Restarts daemons stopped by AKStopJavaDaemons
    /// </summary>
    AK_BOOL AKStartJavaDaemons(JNIEnv *env);
    /// <summary>
    /// Stops daemons so that the zygote can be a single-threaded process
    /// </summary>
    AK_BOOL AKStopJavaDaemons(JNIEnv *env);
    /// <summary>
    /// Ensures all threads running Java suspend and that those not running Java don't start
    /// </summary>
    AK_BOOL AKLockJavaThreads();
    /// <summary>
    /// Unlocks and broadcasts a notification to all threads interrupted by AKLockJavaThreads
    /// </summary>
    void AKUnlockJavaThreads();
    /// <summary>
    /// Enables or disables logging
    /// </summary>
    void AKDisableLogging(bool state AK_DEFAULT(false));

    /// <summary>
    /// Table of interface function pointers
    /// </summary>
    struct AKInvokeInterface {
        intptr_t version;
        const void *(*LoadImage)(const char *path, int flag, const void *caller_addr /* = NULL*/);
        const void *(*GetImageByName)(const char *name /* = AK_ANDROID_RUNTIME */);
        void *(*FindSymbol)(const void *handle, const char *symbol);
        void *(*FindAnonymity)(const void *handle, const uintptr_t st_value);
        void *(*GetBaseAddress)(const void *handle);
        void(*CloseImage)(const void *handle);
        void(*HookFunction)(const void *symbol, const void *replace, void **result /* = NULL */);
        void *(*HookFunctionV)(const void *symbol, const void *replace, void *rwx, const uintptr_t size /* = 64 */);
        void *(*HookFunctionEx)(const void *symbol, const uintptr_t overwritable, const void *replace, void *rwx, const uintptr_t size /* = 64 */);
        void *(*WrapFunction)(const void *func);
        void(*PrintHexBinary)(const void *addr, uintptr_t len, const char *name /* = NULL */);
        AK_BOOL(*ProtectMemory)(const void *addr, uintptr_t len, int prot /* = AK_RWX */);
        AK_BOOL(*PatchMemory)(const void *addr, const void *data, uintptr_t len);
        AK_BOOL(*SuspendAllThreads)();
        void(*ResumeAllThreads)();
        void(*EnableFastDexLoad)(AK_BOOL enable);
        const char *(*LastBuildDate)();
        jint(*InitializeOnce)(JNIEnv *env /* = NULL */, JavaVM *jvm /* = NULL */);
        void(*RegisterLibrary)(JNIEnv *env, jobject classloader);
        void(*JavaHookMethod)(JNIEnv *env, jclass clazz, const char *method, const char *signature, const void *replace, jmethodID *result /* = NULL */);
        void(*JavaHookMethodV)(JNIEnv *env, jmethodID methodId, const void *replace, jmethodID *result /* = NULL */);
        AK_BOOL(*ForceNativeMethod)(JNIEnv *env, jmethodID methodId, const void *jni_entrypoint, AK_BOOL fast_native /* = 0 */, jmethodID *result /* = NULL */);
        AK_BOOL(*ShadowCopyMethod)(JNIEnv *env, jmethodID methodId, jmethodID *result);
        AK_BOOL(*RestoreMethod)(jmethodID j_backup, jmethodID j_dest);
        const void *(*RegisterNative)(jmethodID methodId, const void *native_method, AK_BOOL fast_native /* = 0 */);
        void *(*GetNativeEntry)(jmethodID methodId, AK_BOOL *critical_native /* = NULL */);
        jobject(*LoadFileDex)(JNIEnv *env, const char *dex_path, const char *cache_dir /* = NULL */, const char *lib_path /* = NULL */, jobject parent /* = NULL */);
        jobject(*LoadPathDex)(JNIEnv *env, const char *dex_path, const char *lib_path /* = NULL */, jobject parent /* = NULL */);
        jobject(*LoadMemoryDex)(JNIEnv *env, const void *buffer, jlong capacity, jobject parent /* = NULL */);
        jclass(*LoadClass)(JNIEnv *env, jobject classloader, const char *name, jthrowable *exception);
        jobject(*GetSystemClassLoader)(JNIEnv *env);
        jobject(*GetClassLoader)(JNIEnv *env, jclass clazz);
        jobject(*GetContextClassLoader)(JNIEnv *env, jobject context);
        int(*GetSdkVersion)();
        const char *(*GetCacheDirectory)();
        const char *(*SetCacheDirectory)(const char *path);
        const char *(*GetNativeLibsDirectory)();
        void(*OptimizeMethod)(jmethodID method);
        AK_BOOL(*ForceJitCompile)(jmethodID method);
        void(*DeoptimizeMethod)(jmethodID method);
        void(*DumpClassMethods)(JNIEnv *env, jclass clazz /* = NULL */, const char *clsname /* = NULL */);
        AK_BOOL(*StartJavaDaemons)(JNIEnv *env);
        AK_BOOL(*StopJavaDaemons)(JNIEnv *env);
        AK_BOOL(*LockJavaThreads)();
        void(*UnlockJavaThreads)();
        void(*DisableLogging)(bool state);
    };
    /// <summary>
    /// Retrieves invocation interfaces
    /// </summary>
    struct AKInvokeInterface *AKGetInvokeInterface();

#ifdef __cplusplus
}
#endif
