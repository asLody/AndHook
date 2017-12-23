package andhook.test;

import andhook.lib.AndHook.HookHelper;
import andhook.lib.AndHook.HookHelper.Hook;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

@SuppressWarnings("all")
public final class SystemClass {
    @Hook(clazz = android.app.Activity.class)
    private static void onCreate(final Object objActivity, final Bundle savedInstanceState) {
        Log.i(AndTest.LOG_TAG, "Activity::onCreate start, this is " + objActivity);
        HookHelper.invokeVoidOrigin(objActivity, savedInstanceState);
        Log.i(AndTest.LOG_TAG, "Activity::onCreate end");
    }

    @Hook(clazz = Secure.class)
    private static String getString(final Class<?> classSecure, final ContentResolver resolver,
                                    final String name) {
        Log.i(AndTest.LOG_TAG, "hit name = " + name + ", class = " + classSecure);
        if (name.equals(Secure.ANDROID_ID))
            return "8888888888888888";
        return HookHelper.invokeObjectOrigin(null, resolver, name);
    }

    public static void doHook(final ContentResolver resolver) {
        Log.i(AndTest.LOG_TAG, Secure.getString(resolver, Secure.ANDROID_ID));
        HookHelper.applyHooks(SystemClass.class);
        Log.i(AndTest.LOG_TAG, Secure.getString(resolver, Secure.ANDROID_ID));
    }
}
