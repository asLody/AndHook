package andhook.test;

import andhook.lib.AndHook;
import andhook.test.ui.MainActivity;

public final class JNI {
    private static native boolean java_hook();

    private static native boolean native_hook();

    public static void test() {
        MainActivity.clear();
        MainActivity.output("hook in C/C++...");

        try {
            // libAndHook.so must be loaded before libmyjnihook.so
            AndHook.ensureNativeLibraryLoaded(null);
            System.loadLibrary("myjnihook");
        } catch (final Throwable t) {
            MainActivity.alert(t);
            MainActivity.alert("failed to load myjnihook!");
            return;
        }

        boolean passed = false;
        MainActivity.info("JNI->java_hook");
        try {
            passed = java_hook();
        } catch (final Throwable t) {
            MainActivity.alert(t);
        }

        MainActivity.info("JNI->native_hook");
        try {
            passed = native_hook() && passed;
        } catch (final Throwable t) {
            MainActivity.alert(t);
        }

        if (passed)
            MainActivity.info("jni hook test passed");
        else
            MainActivity.alert("jni hook test failed");
    }
}
