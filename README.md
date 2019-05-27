# AndHook
A dynamic instrumentation framework designed for usage within process scope.

# Support
- Android 4.x or later (with preliminary support for Android 8.1 and 9.0)
- java method instrumentation (hook java method in Java/C/C++)
- native interception (hook native C/C++ functions in C/C++)

# How to use

In `app` module, we use an android project to show how to use the hook toolkit.

## Hook with AndHook

```java
/** Define hook configuration */
public class AndHookConfig {
    /** Hook Activity's onStart() method */
    @HookHelper.Hook(clazz = Activity.class)
    private static void onStart(Activity activity) {
        Log.d(AndTest.LOG_TAG, "onStart: HookedActivity::onStart start, this is " + activity.getClass());
        HookHelper.invokeVoidOrigin(activity);// invoke the origin method
        Log.d(AndTest.LOG_TAG, "onStart: HookedActivity::onStart end, this is " + activity.getClass());
    }
}

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Make sure AndHook's native library is loaded first.
        AndHook.ensureNativeLibraryLoaded(null);
        // Then apply hook configuration before target method running.
        HookHelper.applyHooks(AndHookConfig.class);
    }
}

public class MainActivity extends Activity {
    @Override
    protected void onStart() {
        Log.i(TAG, "MainActivity.super::onStart: start");
        super.onStart();// the method which is hooked
        Log.i(TAG, "MainActivity.super::onStart: end");
    }
}

// After MainActivity launched, you will be able to see log in logcat like:
// AndHook_Test: MainActivity.super::onStart: start
// AndHook_Test: onStart: HookedActivity::onStart start, this is class andhook.test.ui.MainActivity
// AndHook_Test: onStart: HookedActivity::onStart end, this is class andhook.test.ui.MainActivity
// AndHook_Test: MainActivity.super::onStart: end
```

# How does AndHook work?
![AndHook](https://github.com/Rprop/AndHook/raw/master/AndHook.png)

# How the method was intercepted?
![CallFlow](https://github.com/Rprop/AndHook/raw/master/CallFlow.png)

# Special Thanks
- cly.comp
- Meow

# References
- [Cydia Substrate](https://github.com/Rprop/AndHook/tree/6cca8575771d13cbe3907442e4ed6808381b6fd5/jni/utils/Substrate) (armeabi-v7a, x86, x86_64)
- [And64InlineHook](https://github.com/Rprop/And64InlineHook) (arm64-v8a)
