package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.AndHook;
import andhook.lib.AndHook.HookHelper;

import android.util.Log;

@SuppressWarnings("all")
public final class Static {
    public static String a1(final String s) {
        Log.i(AndTest.LOG_TAG, "public static method Static::a1 hit!");
        return "return from Static::a1 with param " + s;
    }

    private static String a2(final Class<?> classStatic, final String s) {
        Log.i(AndTest.LOG_TAG, "public static method Static::a2 hit, class = " + classStatic);
        try {
            final Object obj = HookHelper.invokeObjectOrigin(null, s + "+a2");
            Log.i(AndTest.LOG_TAG, "invokeObjectOrigin[static] return " + obj);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "return from Static::a2 with param " + s;
    }

    private static void hookUsingApi() {
        try {
            final Method m1 = Static.class.getDeclaredMethod("a1",
                    String.class);
            final Method m2 = Static.class.getDeclaredMethod("a2", Class.class,
                    String.class);
            Log.i(AndTest.LOG_TAG, "begin hook public static method Static::a1...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.LOG_TAG, "end hook public static method Static::a1");

            Log.i(AndTest.LOG_TAG, "calling public static method Static::a1...");
            Log.i(AndTest.LOG_TAG, "public static method Static::a1 returns [" + a1("test")
                    + "]");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookMethodFromDifferentClassUsingApi() {
        try {
            // The following code should be called once and only once
            // as A.class and B.class may not have been initialized
            // if you use AndHook api directly.
            AndHook.ensureClassInitialized(A.class);
            AndHook.ensureClassInitialized(B.class);

            final Method m1 = A.class.getDeclaredMethod("AA", String.class);
            final Method m2 = B.class.getDeclaredMethod("BB", Class.class, String.class);
            Log.i(AndTest.LOG_TAG, "begin hook public static method A::AA...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.LOG_TAG, "end hook public static method A::AA");

            Log.i(AndTest.LOG_TAG, "calling public static method A::AA...");
            Log.i(AndTest.LOG_TAG,
                    "public static method A::AA returns [" + A.AA("test") + "]");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void doHook() {
        // hook using base AndHook api
        hookUsingApi();
        hookMethodFromDifferentClassUsingApi();
    }
}
