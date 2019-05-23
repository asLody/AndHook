package andhook.test.app;

import android.app.Application;
import android.util.Log;

import andhook.lib.AndHook;
import andhook.test.AndTest;

public final class MainApplication extends Application {

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
    }
}
