LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := myjnihook
LOCAL_CFLAGS := -fpermissive -fno-rtti -fno-exceptions
LOCAL_CFLAGS += -fvisibility=hidden -fvisibility-inlines-hidden -std=c++1y
LOCAL_CFLAGS += -g0 -O3 -fomit-frame-pointer
LOCAL_LDFLAGS += -Wl,--strip-all
LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_SRC_FILES := myjnihook.cpp
LOCAL_LDLIBS := -llog
#LOCAL_SHARED_LIBRARIES += libAndHook
LOCAL_LDLIBS += -L$(LOCAL_PATH)/../../../../lib/src/main/jniLibs/$(TARGET_ARCH_ABI)/ -lAndHook
include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#OCAL_MODULE := libAndHook
#LOCAL_SRC_FILES := libAndHook.so
#include $(PREBUILT_SHARED_LIBRARY)