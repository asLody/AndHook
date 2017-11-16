# Support
- direct methods (static, private and constructor)
- virtual methods (non-static public and protected)
- java annotation @Hook
- reflection call
- compatible with libhoudini

# Limits
- some special methods cannot be hooked, e.g. abstract methods
- methods with different signature is unstable (reflection may failed due to mismatched method name)
- fields hook not supported

# Structure
AndHook consists of only one java file (AndHook.java) and binaries for specified architectures, no other dependencies.  
- AndHook.java provides an optional bridge class of ArtHook and DalvikHook, and an inner class HookHelper to help simplify method backup.

# Usage
Just put AndHook.java and binaries (*.so files) into proper directories, and do what you want. You don't need to compile jni code yourself as precompiled binaries is provided.
- makes sure that all the classes involved are initialized, e.g. non-system classes, otherwise, a deadlock could occur. In addition, remember that the dynamically loaded classes may also be collected by GC since Android N. A simple workaround is to keep global reference(NewGlobalRef) of them.
```java
	AndHook.ensureClassInitialized(A.class);
	AndHook.ensureClassInitialized(B.class);
```
- simply replaces a method without backup:
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
For concrete usage, please see [AndTest.java](https://raw.githubusercontent.com/rrrfff/AndHook/master/test/app/src/main/java/apk/andhook/test/AndTest.java)

# Reference
[《Android热修复升级探索——追寻极致的代码热替换》](https://yq.aliyun.com/articles/74598)    
[Android N混合编译与对热补丁影响解析](https://github.com/WeMobileDev/article/blob/master/Android_N混合编译与对热补丁影响解析.md)    
[Implementing ART Just-In-Time (JIT) Compiler](https://source.android.com/devices/tech/dalvik/jit-compiler)    
