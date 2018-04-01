package andhook.test;

import andhook.lib.HookHelper;
import andhook.lib.HookHelper.Hook;
import andhook.ui.MainActivity;

import android.content.ContentResolver;
import android.provider.Settings.Secure;

public final class SystemClass {
    private static final String FAKED_ANDROID_ID = "8888888888888888";

    @SuppressWarnings("unused")
    @Hook(clazz = Secure.class)
    private static String getString(final Class<?> classSecure, final ContentResolver resolver,
                                    final String name) {
        MainActivity.output(classSecure.getName() + "::getString hit, name = " + name);
        final String s = HookHelper.invokeObjectOrigin(null, resolver, name);
        if (name.equals(Secure.ANDROID_ID)) {
            MainActivity.output(s + " -> " + FAKED_ANDROID_ID);
            return FAKED_ANDROID_ID;
        }
        return s;
    }

    public static void test(final ContentResolver resolver) {
        MainActivity.clear();
        MainActivity.output("system class test...");

        MainActivity.output(Secure.getString(resolver, Secure.ANDROID_ID));
        HookHelper.applyHooks(SystemClass.class);
        if (!Secure.getString(resolver, Secure.ANDROID_ID).equals(FAKED_ANDROID_ID)) {
            MainActivity.alert("failed to hook Secure::getString!");
            return;
        }

        MainActivity.info("system class test passed");
    }
}
