package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.AndHook;
import andhook.lib.HookHelper;

import android.util.Log;

@SuppressWarnings({"all"})
public final class Virtual {
    public String b1(final String s) {
        Log.i(AndTest.LOG_TAG, "public method b1 hit, this is " + this);
        return "return from b1 with param " + s;
    }

    private static String b2(final Object objAndTest, final String s) {
        Log.i(AndTest.LOG_TAG, "public method b2 hit, this is " + objAndTest);
        try {
            final Object obj = HookHelper.invokeObjectOrigin(objAndTest, s + "+b2");
            Log.i(AndTest.LOG_TAG, "invokeObjectOrigin return " + obj);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "return from b2 with param " + s;
    }

    private static void hookNonStaticMethodUsingApi() {
        try {
            final Method m1 = Virtual.class.getDeclaredMethod("b1",
                    String.class);
            final Method m2 = Virtual.class.getDeclaredMethod("b2", Object.class,
                    String.class);
            Log.i(AndTest.LOG_TAG, "begin hook public method b1...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.LOG_TAG, "end hook public method b1");

            Log.i(AndTest.LOG_TAG, "calling public method b1...");
            Log.i(AndTest.LOG_TAG,
                    "public method b1 returns [" + (new Virtual()).b1("test")
                            + "]");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static class Virtual0 {
        public int getUserId() {
            Log.i(AndTest.LOG_TAG, "Virtual0 getUserId hit, this is " + this);
            return 0;
        }
    }

    public static class Virtual1 extends Virtual0 {
        @Override
        public int getUserId() {
            Log.i(AndTest.LOG_TAG, "Virtual1 getUserId hit, this is " + this);
            return super.getUserId();
        }
    }

    public static class Virtual2 extends Virtual1 {
        // we cannot simply replace virtual methods unless they have the same
        // method_index_ and we patch reflection mechanism for them.
        private static int fake_getUserId(final Object objVirtual) {
            Log.i(AndTest.LOG_TAG, "faked Virtual1 getUserId hit, this is " + objVirtual);
            return 0;
        }
    }

    public static void doHook() {
        hookNonStaticMethodUsingApi();

        Log.i(AndTest.LOG_TAG, "\nhook parent's virtual method...");
        // test pre call
        (new Virtual1()).getUserId();
        // hook
        AndHook.hookNoBackup(
                HookHelper.findMethodHierarchically(Virtual1.class, "getUserId"),
                HookHelper.findMethodHierarchically(Virtual2.class, "fake_getUserId", Object.class));
        // test post call
        (new Virtual1()).getUserId();
    }
}
