# AndHook
AndHook is a lightweight hook framework for android. It is primarily written in C++ and easy to use.  

# Support
Android 4.x or later (with preliminary support for Android O), excluding customized Android ROMs such as YunOS.
- java method replacement (hook java method in JAVA)
- jni hook (hook java method in C/C++)
- native hook (hook native method in C/C++)

# Java Method Replacement
Similar to AndFix/Legend, AndHook's `Java Method Replacement` is based on underlying ArtMethod replacement, but goes further (may not stable enough).

# JNI Hook
This allows you to intercept any java method in C/C++ entirely, which can be very handy when working on application analysis or reverse engineering. Its technical principle is very simple: making the target java method to be native and replacing entry_point_from_jni_.  

# Native Hook
Based on Cydia Substrate module (armeabi-v7a, x86, x86_64) and [And64InlineHook](https://github.com/rrrfff/And64InlineHook) (arm64-v8a).   
