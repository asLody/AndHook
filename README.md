# AndHook
AndHook is a lightweight java method hook framework for android, which supports both dalvik and art environment. It is primarily written in C++ so as to achieve good performance, but has java friendly apis as well.  

# Support
Android 4.x or later, most of the ROMs and devices (armeabi-v7a and x86).

# Structure
AndHook consists of the following part:  
- java
	- AndHook.java    
		- optional bridge class of ArtHook and DalvikHook, and an inner class HookHelper is provided to help simplify method backup
	- ArtHook.java    
		- ART hook apis
	- DalvikHook.java    
		- Dalvik VM hook apis  
- jni
	- andhook.cpp    
		- JNI_OnLoad/JNI_OnUnLoad
	- art.cpp    
		- ART hook implementation
	- dalvik_vm.cpp    
		- Dalvik VM hook implementation 

# Usage
- simply replaces a method (compatible with all Android version, but may encounter some issues such as NoSuchMethodError. `You should try this first to see if it meets your requirements`):
```java
	AndHook.replaceMethod(final Method origin, final Method replacement);
```
- replaces a method and applys workaround for known issues (with limited self-adapting capabilities, needs more tests):
```java
	AndHook.hookNoBackup(final Method origin, final Method replacement);
```
- additional method backup (allows you to invoke orginal method anywhere):
```java
	// AndHook saves original method internationally, 
	// and returns just primitive index to reduce memory usage.
	int slot = AndHook.hook(final Method origin, final Method replacement);
	// Invokes original method. 
	// The box/unbox operations are done by AndHook in dalvik-vm mode while JVM in art mode.
	AndHook.invokeXXXMethod(slot, ...);
	// Or, you can use HookHelper, which saves the slot automatically for you.
```
For concrete usage, please see [AndTest.java](https://github.com/rrrfff/AndHook/blob/master/java/src/apk/andhook/test/AndTest.java)

# Reference
[¡¶AndroidÈÈÐÞ¸´Éý¼¶Ì½Ë÷¡ª¡ª×·Ñ°¼«ÖÂµÄ´úÂëÈÈÌæ»»¡·](https://yq.aliyun.com/articles/74598)  