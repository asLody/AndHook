package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.AndHook;
import andhook.lib.HookHelper;
import andhook.ui.MainActivity;

import android.util.Log;

public final class Virtual {
    public String a(final String s) {
        MainActivity.output("public Virtual::a hit with " + s + ", this = "
                + this);
        return s + "_a";
    }

    @SuppressWarnings("unused")
    private static String b(final Object objVirtual, final String s) {
        MainActivity.output("private static Virtual::b hit with " + s
                + ", this = " + objVirtual);
        return HookHelper.invokeObjectOrigin(objVirtual, s) + "_b";
    }

    private static void hookWithinSameClass() throws Exception {
        final Method ma = Virtual.class.getDeclaredMethod("a", String.class);
        final Method mb = Virtual.class.getDeclaredMethod("b", Object.class,
                String.class);

        HookHelper.hook(ma, mb);

        final String result = new Virtual().a("test");
        MainActivity.output("result = " + result);

        if (!result.endsWith("_a_b"))
            throw new RuntimeException("unexpected result " + result);

        NativeAssert.run(ma);
    }

    private static class A {
        public int get() {
            MainActivity.output("public A::get hit, this = " + this);
            return 0;
        }
    }

    public static class B extends A {
        @Override
        public int get() {
            MainActivity.output("public B::get hit, this = " + this);
            return super.get() + 1;
        }

        static int faked_get(final Object obj) {
            MainActivity.output("static B::faked_get hit, obj = " + obj);
            return 1;
        }
    }

    private static void hookInheritedClass() throws Exception {
        Log.i(AndTest.LOG_TAG, "hooking public A::get...");

        final int ra = (new B()).get();
        if (ra != 1 && ra != 2)
            throw new RuntimeException("unexpected result ra " + ra);

        final Method ma = A.class.getDeclaredMethod("get");
        final Method mb = B.class.getDeclaredMethod("faked_get", Object.class);
        AndHook.hookNoBackup(ma, mb);

        final int rb = (new B()).get();

        if (rb != 2)
            throw new RuntimeException("unexpected result rb " + rb + "!= 2");
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("virtual method hook test...");

        try {
            hookWithinSameClass();

            MainActivity.output("    ");

            hookInheritedClass();
        } catch (final Exception e) {
            MainActivity.alert(e);
            return;
        }

        MainActivity.info("virtual method hook test passed");
    }
}
