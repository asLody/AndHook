package andhook.test.app;

import andhook.lib.AndHook;
import andhook.test.AndTest;

import android.app.Application;
import android.util.Log;

import java.lang.ref.WeakReference;

public final class MainApplication extends Application {
    public static WeakReference<Application> sApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        installDefaultUncaughtExceptionHandler();

        Log.i(AndTest.LOG_TAG, "\nApplication started.\n--------------------------------");
        AndHook.ensureNativeLibraryLoaded();
        sApp = new WeakReference<Application>(this);
    }

    private static void installDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(final Thread th, final Throwable t) {
                Log.wtf(AndTest.LOG_TAG, th + " has uncaught exception " + t);
                Log.wtf(AndTest.LOG_TAG, t);
            }
        });
    }
}
