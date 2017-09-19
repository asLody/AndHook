# AndHook
AndHook is a lightweight hook framework for android, which supports both dalvik and art environment. It is primarily written in C++ and easy to use.  

# Support
Android 4.x or later (including Android O), most of the ROMs and devices (armeabi-v7a, arm64-v8a, x86, x86_64).
- java method replacement
- jni hook (hook java method in C/C++)
- native hook

# Java Method Replacement
Similar to AndFix/Legend, AndHook's `Java Method Replacement` is based on underlying ArtMethod replacement, but goes further (may not stable enough as AndHook is new-born).  

# JNI Hook
Now, you can hook java method in C/C++ entirely, which can be very handy when working on application analysis or reverse engineering. Its technical principle is very simple: making the target java method to be native.  

# Native Hook
Based on substrate module currently, so not ARM64 support.   
