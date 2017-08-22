# AndHook
AndHook is a lightweight java method hook framework for android, which supports both dalvik and art environment. It is primarily written in C++ so as to achieve optimal performance, but has java friendly apis as well.  

# Support
Android 4.x or later, most of the ROMs and devices (armeabi-v7a, arm64-v8a, x86, x86_64).

# Structure
AndHook consists of only one java file (AndHook.java) and binaries for specified architectures, no other dependencies.  
- AndHook.java provides an optional bridge class of ArtHook and DalvikHook, and an inner class HookHelper to help simplify method backup.

# Usage
Just put AndHook.java and binaries (*.so files) into proper directories, and do what you want. You don't need to compile jni code yourself as precompiled binaries is provided, and everyone who wants to contribute to it please contact me, thanks.
- makes sure that all the classes involved are initialized, e.g. non-system classes. To prevent GC collections, AndHook will keep global reference of the class as well.
```java
	AndHook.ensureClassInitialized(A.class);
	AndHook.ensureClassInitialized(B.class);
```
- simply replaces a method (compatible with all Android version, but may encounter some issues such as NoSuchMethodError. `You should try this first to see if it meets your requirements`):
```java
	AndHook.replaceMethod(final Method origin, final Method replacement);
```
- replaces a method and applys workaround for known issues mentioned above (with limited self-adapting capabilities, needs more tests):
```java
	AndHook.hookNoBackup(final Method origin, final Method replacement);
```
- additional method backup (allows you to invoke orginal method anywhere):
```java
	// AndHook saves original method internally, 
	// and returns just primitive index to reduce memory usage.
	int slot = AndHook.hook(final Method origin, final Method replacement);
	// Invokes original method. 
	// The box/unbox operations are done by AndHook in dalvik-vm mode while JVM in art mode.
	AndHook.invokeXXXMethod(slot, ...);
	// Or, you can use HookHelper, which saves the slot automatically for you.
```
For concrete usage, please see [AndTest.java](https://raw.githubusercontent.com/rrrfff/AndHook/master/java/test/src/apk/andhook/test/AndTest.java)

# Reference
[《Android热修复升级探索——追寻极致的代码热替换》](https://yq.aliyun.com/articles/74598)    
[Android N混合编译与对热补丁影响解析](https://github.com/WeMobileDev/article/blob/master/Android_N混合编译与对热补丁影响解析.md)    
[Implementing ART Just-In-Time (JIT) Compiler](https://source.android.com/devices/tech/dalvik/jit-compiler)    
