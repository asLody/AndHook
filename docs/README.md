# 关于AndHook
AndHook设计初衷是解决项目运行过程中遇到的一些实际问题，本身并未使用什么新技术，只是把积累下来的各种解决方案以框架形式结合起来，方便后续维护。   
目前，AndHook尚不支持进程间注入，但是可通过SO替换、VirtualApp等手段达成，如果读者对这块有通用的解决方案欢迎探讨~   

# 注意事项
为能顺利的完成HOOK，有以下几点需要关注：
- 需要保证涉及的类都是被初始化过的(即<clinit>被调用过)，因为如果在类初始化之前进行方法替换，可能在后续类初始化过程出现异常死锁或者属性被覆盖导致HOOK失效等情况；在JNI层面一般不会出现这种情况，因为一些JNI调用比如env->GetMethodID内部会保证类的已初始化状态，在JAVA层可以通过`AndHook.ensureClassInitialized`来做，而对于一些已知被初始化过的系统类则无须考虑这个。  
- 保证原始方法和替换方法的方法类型(virtual和direct)、返回值类型、参数类型一致，如果这些不一致，一般情况下都是会挂掉的。如果是JAVA方法替换，尽可能保证函数名称也是一致的，否则反射调用等情况下会出问题。  
- 对于非static方法的HOOK(针对JAVA方法替换而言)，不建议在相应的替换方法里使用invoke-virtual等指令调用所属类的其它方法或字段。可以这么考虑，这时候this指针是类A，却被用去调用类B的东西。一种解决方案是将替换方法当成跳板，跳到到另一个类中的函数去处理。
- 在Android N之后，要考虑类引用的情况，如果类未被任何引用则可能被GC回收。
- 需要自行考虑线程安全问题，同进程间注入一样，尚未找到合适的解决方案。

# 如何使用
AndHook所支持的三种HOOK方式都提供了尽可能简单的API，但是为了使用的方便也可能会有一些包装情况。
- 在JAVA层进行HOOK，除了基本的API，也支持杂注@Hook(clazz, value, name)，如果name不指定的话默认和替换方法同名，最后记得调用HookHelper.applyHooks来使杂注生效。[参考示例](https://raw.githubusercontent.com/rrrfff/AndHook/master/java/test/src/apk/andhook/test/AndTest.java)   
    ```java
	import apk.andhook.AndHook.HookHelper.Hook;
	@Hook(clazz = android.app.Activity.class) // @Hook("android.app.Activity")
	public void onCreate(final Bundle savedInstanceState) {
		Log.i(this.getClass().toString(), "Activity::onCreate start");
		HookHelper.callVoidOrigin(this, savedInstanceState);
		Log.i(this.getClass().toString(), "Activity::onCreate end");
	}
    ```
    此外有一些附加API可用，参见[AndHook.java](https://github.com/rrrfff/AndHook/tree/master/java/AndHook.java)
    ```java
	public static native void ensureClassInitialized(final Class<?> origin);
	public static native void deoptimizeMethod(final Method target);
	public static native void dumpClassMethods(final Class<?> clazz,
			final String clsname);
    ```
    其中，`ensureClassInitialized`用于设法保证类的已初始化状态并尽可能降低性能影响，在不同的Android版本中有不同实现，比如在JNI层调用AllocObject方法；   
    `deoptimizeMethod`用于强制某个方法以解释器方式执行，可用于解决一些函数内联的问题，Android本身是有去优化机制的(优化后的代码不利于调试)，参考[Deoptimize-Methods-on-Android-N](https://github.com/rrrfff/AndHook/wiki/Deoptimize-Methods-on-Android-N)；   
    `dumpClassMethods`用于Dump出某个类的全部virtual和direct方法，在不同的Android版本中有不同实现，dalvik下有方法签名信息，art下依靠引发特定错误让系统输出。
- 在C/C++代码中拦截JAVA方法，底层实现是系统提供的接口RegisterNatives，故而兼容性比较好，也没有签名问题。目前AndHook提供的API都尽可能和Cydia Substrate保持兼容。
    ```c++
	void MSJavaHookMethod(JNIEnv *env, jclass clazz, const char *method, const char *signature,
                          void *replace, intptr_t *result/* = NULL*/);
    ```
    最后一个参数result用来保存原始函数的备份索引，如果不需要，传入NULL即可，此时将无法调回原函数。该方法实际上是对`SetNativeMethod`的包装。*result可用于传入下列函数得到jmethodID
    ```c++
	jmethodID GetMethodID(JNIEnv *env, intptr_t backup, void *buffer/* JNI_METHOD_SIZE */);
    ```
    注意此处返回的jmethodID不应该被缓存，因为执行Moving GC后某些方法字段可能失效；此外GetMethodID并不发生查找操作，性能代价极小。参数buffer用于传入至少JNI_METHOD_SIZE字节大小并对齐的缓存区，可以使用alloca函数分配栈内存(不能假设返回的jmethodID就是buffer，在部分安卓版本下会忽略buffer而改用内部申请的内存区域)。
- 在C/C++代码中拦截NATIVE方法，底层实现是Inline Hook，除了部分特殊短函数外基本都能拦截。目前AndHook基于Cydia Substrate和And64InlineHook，仅支持主流CPU架构。
    ```c++
	void MSHookFunction(void *symbol, void *replace, void **result/* = NULL*/);
    ```
    最后一个参数result用来保存原始函数的指针，如果不需要，传入NULL即可，此时将无法调回原函数。如果*result不为NULL，则表明HOOK成功。鉴于部分方法无法通过dlsym得到以及Android N之后的相关限制，AndHook也提供了一些辅助函数用于获取目标函数指针(这部分实现和Cydia Substrate无关)
    ```c++
	const void *MSGetImageByName(const char *name/* = ANDROID_RUNTIME*/);
	void MSCloseImage(const void *handle);
	void *MSFindSymbol(const void *handle, const char *symbol);
    ```
    其中，`MSGetImageByName`用来得到特定的已加载模块，如果模块尚未被加载则返回NULL；传入ANDROID_RUNTIME可用于自适应获取libdvm.so或者libart.so模块。   
    `MSCloseImage`仅用于释放内部内存，不影响模块，参数handle允许传入NULL。   
    `MSFindSymbol`用于寻找特定符号，如果handle为NULL或者符号不存在则返回NULL。
- 具体使用而言，JAVA层只需将java/AndHook.java以及jniLibs/*.so放到工程下特定目录(保持包名结构)即可；   
NATIVE层使用略麻烦，头文件include/AndHook.h，静态链接的话需要把相应架构的libAndHook.so添加到链接器的库依赖项，同时需要注意so加载顺序；或者在so里面动态加载libAndHook.so也行。

# 写在最后
AndHook使用者也不多，很多问题未能及时发现和修复，但还是决定把这份Manual写出来，其中有些东西不仅仅是针对AndHook的，同样适用于其它框架，希望有所帮助~   
此外，推荐一些类似项目，都是目前来看还在更新维护的，相比而言，它们可能比AndHook受众更广、更稳定：   
- HookZz，https://github.com/jmpews/HookZz ，内联HOOK框架，支持短函数，目前仅支持ios，但作者已着手适配Android了。
- AndroidMethodHook，https://github.com/panhongwei/AndroidMethodHook ，原理同AndHook一致，亮点是使用dexmaker，灵活性更大。
- YAHFA，https://github.com/rk700/YAHFA ，仅支持ART，采用类似Inline Hook的方案来拦截Java方法，克服了方法整体替换的一些短板。
- StormHook，https://github.com/woxihuannisja/StormHook ，支持进程间注入。
