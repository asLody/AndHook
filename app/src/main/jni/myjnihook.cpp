#include <jni.h>
#include <android/log.h>
#include "include/AndHook.h"
#define AKLog(...) __android_log_print(ANDROID_LOG_INFO, __FUNCTION__, __VA_ARGS__)
#define AKHook(X)  AKHookFunction(reinterpret_cast<void *>(X), reinterpret_cast<void *>(my_##X), reinterpret_cast<void **>(&sys_##X));

static jmethodID sys_getGTalkDeviceId;
static jstring JNICALL my_getGTalkDeviceId(JNIEnv *env, jclass obj, jlong j)
{
    // call the original method
    jstring js = reinterpret_cast<jstring>(env->CallStaticObjectMethod(obj, sys_getGTalkDeviceId, j));
    if (js != NULL) {
        AKLog("%s", env->GetStringUTFChars(js, NULL));
    } //if

    return env->NewStringUTF("faked_GTalkDeviceId");
}

static int(*sys_access)(const char *pathname, int mode);
static int my_access(const char *pathname, int mode)
{
    AKLog("access %s, %d", pathname, mode);
    if (strstr(pathname, "/system/bin/su") != NULL ||
        strstr(pathname, "/system/xbin/su") != NULL) {
        return -1;
    } //if

    // call the original function
    return sys_access(pathname, mode);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_EVERSION;
    } //if

    AKLog("starting jni hook...");
    jclass clazz = env->FindClass("android/provider/Settings");
    AKJavaHookMethod(env, clazz, 
                     "getGTalkDeviceId", "(J)Ljava/lang/String;",   // hooked method
                     reinterpret_cast<void *>(my_getGTalkDeviceId), // our method 
                     &sys_getGTalkDeviceId                          // backup method id
    );
    AKLog("jni hook done.");

    AKLog("starting native hook...");
    AKHookFunction(reinterpret_cast<void *>(access),      // hooked function
                   reinterpret_cast<void *>(my_access),   // our function
                   reinterpret_cast<void **>(&sys_access) // backup function pointer
    );
    AKLog("native hook done.");

    // tigger `access` call
    access("libAndHook.so", F_OK);

    return JNI_VERSION_1_6;
}