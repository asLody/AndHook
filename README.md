# AndHook
AndHook is a lightweight hook framework for android, which supports both dalvik and art environment. It is primarily written in C++ and easy to use.  

# Documentation
A Chinese Manual is available [here](https://github.com/rrrfff/AndHook/tree/master/docs).   

# Support
Android 4.x or later (including Android O), most of the ROMs and devices (armeabi-v7a, arm64-v8a, x86, x86_64).
- java method replacement (hook java method in java)
- jni hook (hook java method in C/C++)
- native hook (hook native method in C/C++)

# Java Method Replacement
Similar to AndFix/Legend, AndHook's `Java Method Replacement` is based on underlying ArtMethod replacement, but goes further (may not stable enough).  

# JNI Hook
Now, you can hook java method in C/C++ entirely, which can be very handy when working on application analysis or reverse engineering. Its technical principle is very simple: making the target java method to be native and replacing entry_point_from_jni_.  

# Native Hook
~~Based on substrate module currently, so not ARM64 support.~~   
Based on Cydia Substrate module (armeabi-v7a, x86, x86_64) and [And64InlineHook](https://github.com/rrrfff/And64InlineHook) (arm64-v8a).   
