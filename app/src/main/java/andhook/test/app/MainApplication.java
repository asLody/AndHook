package andhook.test.app;

import android.app.Application;
import android.util.Log;

import andhook.lib.AndHook;
import andhook.lib.HookHelper;
import andhook.test.AndTest;
import andhook.test.SimpleHookConfig;

public final class MainApplication extends Application {

    private static final String TAG = AndTest.LOG_TAG;

    private static void installDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((th, t) -> {
            Log.wtf(AndTest.LOG_TAG, th + " has uncaught exception " + t);
            Log.wtf(AndTest.LOG_TAG, t);
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        installDefaultUncaughtExceptionHandler();

        AndHook.ensureNativeLibraryLoaded(null);

        Log.i(AndTest.LOG_TAG, "\nApplication started.\n--------------------------------");

        // 从指定的配置类中加载被 @Hook 标记的方法.
        HookHelper.applyHooks(SimpleHookConfig.class);
        Log.d(TAG, "SimpleHookConfig applied");
    }
}
