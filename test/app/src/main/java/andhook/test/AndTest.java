package andhook.test;

import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.util.Log;

import andhook.lib.AndHook;

@SuppressWarnings({"all"})
public class AndTest {
    public final static String LOG_TAG = "AndHook_Test";

    public static void RunTest(final ContextWrapper context,
                               final ContentResolver resolver) {
        Log.i(AndTest.LOG_TAG, "\nhook test started.\n--------------------------------");
        // libAndHook.so must be loaded before libmyjnihook.so
        AndHook.ensureNativeLibraryLoaded();

        Log.i(AndTest.LOG_TAG, "\nhook in C/C++...\n--------------------------------");
        try {
            System.loadLibrary("myjnihook");
        } catch (final UnsatisfiedLinkError e) {
            Log.w(AndTest.LOG_TAG, "failed to load myjnihook!");
        }

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        Xposed.doHook();

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        WideningConversion.doHook();

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        Static.doHook();

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        Constructor.doHook();

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        Virtual.doHook();

        Log.i(AndTest.LOG_TAG, "\n--------------------------------");
        SystemClass.doHook(resolver);

        Log.i(AndTest.LOG_TAG, "\n--------------------------------\nhook test done.\n");
    }
}
