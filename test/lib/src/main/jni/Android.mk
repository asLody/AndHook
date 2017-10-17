LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := myjnihook
LOCAL_CFLAGS := -fpermissive -fno-rtti -fno-exceptions
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_SRC_FILES := myjnihook.cpp
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES += libAndHook
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libAndHook
LOCAL_SRC_FILES := $(LOCAL_PATH)/../../../../app/src/main/jniLibs/$(TARGET_ARCH_ABI)/libAndHook.so
include $(PREBUILT_SHARED_LIBRARY)