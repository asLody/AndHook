#include <jni.h>
#include <android/log.h>
#include "include/AndHook.h"
#define AKLog(...) __android_log_print(ANDROID_LOG_VERBOSE, __FUNCTION__, __VA_ARGS__)
#define AKHook(X)  AKHookFunction(reinterpret_cast<void *>(X), reinterpret_cast<void *>(my_##X), reinterpret_cast<void **>(&sys_##X));

static jboolean  java_passed = JNI_FALSE;
static jmethodID sys_getGTalkDeviceId;
static jstring JNICALL my_getGTalkDeviceId(JNIEnv *env, jclass obj, jlong j)
{
    // call the original method
    jstring js = reinterpret_cast<jstring>(env->CallStaticObjectMethod(obj, sys_getGTalkDeviceId, j));
    if (js != NULL) {
        AKLog("%s", env->GetStringUTFChars(js, NULL));
    } //if

    java_passed = JNI_TRUE;
    return env->NewStringUTF("faked_GTalkDeviceId");
}

static jboolean JNICALL java_hook(JNIEnv *env, jclass)
{
    static bool hooked = false;

    AKLog("starting jni hook...");
    jclass clazz = env->FindClass("android/provider/Settings");
    if (!hooked) AKJavaHookMethod(env, clazz,
                                  "getGTalkDeviceId", "(J)Ljava/lang/String;",   // hooked method
                                  reinterpret_cast<void *>(my_getGTalkDeviceId), // our method 
                                  &sys_getGTalkDeviceId                          // backup method id
    );
    hooked = true;

    AKLog("calling getGTalkDeviceId...");
    env->CallStaticObjectMethod(clazz,
                                env->GetStaticMethodID(clazz, "getGTalkDeviceId", "(J)Ljava/lang/String;"),
                                static_cast<jlong>(__LINE__));

    AKLog("jni hook done, java_passed = %u", java_passed);
    return java_passed;
}

//-------------------------------------------------------------------------

static jboolean native_passed = JNI_FALSE;
static int(*sys_access)(const char *pathname, int mode);
static int my_access(const char *pathname, int mode)
{
    AKLog("my_access %s, %d", pathname, mode);
    native_passed = JNI_TRUE;

    if (strstr(pathname, "/system/bin/su") != NULL ||
        strstr(pathname, "/system/xbin/su") != NULL) {
        return -1;
    } //if

    AKLog("calling original sys_access %p...", sys_access);
    int r = sys_access(pathname, mode);

    AKLog("sys_access %p called, return value = %d", sys_access, r);
    return r;
}

static int(*sys_execve)(const char *, char * const *, char * const *);
static int my_execve(const char *a, char * const *b, char * const *c)
{
    AKLog("my_execve %s, %s, %p", a, b ? b[0] : "", c);
    native_passed = JNI_TRUE;

    AKLog("calling original sys_execve %p...", sys_execve);
    int r = sys_execve(a, b, c);

    AKLog("sys_execve %p called, return value = %d", sys_execve, r);
    return r;
}

static int(*sys_execv)(const char *name, char *const *argv);
extern int my_execv(const char *name, char *const *argv)
{
    AKLog("my_execv %s, %s", name, argv ? argv[0] : "");
    native_passed = JNI_TRUE;

    AKLog("calling original sys_execv %p...", sys_execv);
    int r = sys_execv(name, argv);

    AKLog("sys_execv %p called, return value = %d", sys_execv, r);
    return r;
}

static jboolean JNICALL native_hook(JNIEnv *env, jclass)
{
    static bool hooked = false;

    AKLog("starting native hook...");
    if (!hooked) AKHookFunction(reinterpret_cast<void *>(access),      // hooked function
                                reinterpret_cast<void *>(my_access),   // our function
                                reinterpret_cast<void **>(&sys_access) // backup function pointer
    );
    if (!hooked) AKHook(execv);
    if (!hooked) AKHook(execve);
    hooked = true;

    AKLog("triggering `access` call %p, %p...", &access, sys_execv);
    access("libAndHook.so", F_OK);

    AKLog("triggering `execv` call %p, %p...", &execv, sys_execv);
    char  args[] = "-A";
    char *argv[] = { args, NULL };
    execv("ps", argv);

    AKLog("triggering `execve` call %p, %p...", &execve, sys_execve);
    execve("ps", argv, argv);

    AKLog("native hook done, native_passed = %u", native_passed);
    return native_passed;
}

//-------------------------------------------------------------------------

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_EVERSION;
    } //if

    jclass JNI = env->FindClass("andhook/test/JNI");
    env->ExceptionClear();
    JNINativeMethod gMethods[] = {
        { "java_hook", "()Z", reinterpret_cast<void *>(&java_hook) },
        { "native_hook", "()Z", reinterpret_cast<void *>(&native_hook) }
    };
    env->RegisterNatives(JNI, gMethods, 2);

    return JNI_VERSION_1_6;
}