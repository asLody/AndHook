LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE           := myjnihook
LOCAL_SRC_FILES        := myjnihook.cpp
LOCAL_CFLAGS           := -fpermissive -fno-rtti -fno-exceptions
LOCAL_C_INCLUDES       := $(LOCAL_PATH)
LOCAL_LDLIBS           := -llog
LOCAL_SHARED_LIBRARIES := AK
include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/../../../../lib/AndHook.mk
