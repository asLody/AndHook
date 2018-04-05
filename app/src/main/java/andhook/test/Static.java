package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.AndHook;
import andhook.lib.HookHelper;
import andhook.test.ui.MainActivity;

public final class Static {
    public static String a(final String s) {
        MainActivity.output("public static Static::a hit with " + s);
        return s + "_a";
    }

    @SuppressWarnings("unused")
    private static String b(final Class<?> classStatic, final String s) {
        MainActivity.output("private static Static::b hit with " + s
                + ", class = " + classStatic);
        return HookHelper.invokeObjectOrigin(null, s) + "_b";
    }

    private static void hookWithinSameClass() throws Exception {
        final Method ma = Static.class.getDeclaredMethod("a", String.class);
        final Method mb = Static.class.getDeclaredMethod("b", Class.class,
                String.class);

        MainActivity.output("hooking public static Static::a...");
        HookHelper.hook(ma, mb);

        MainActivity.output("calling public static Static::a...");
        final String result = a("test");
        MainActivity.output("result = " + result);

        if (!result.endsWith("_a_b"))
            throw new RuntimeException("unexpected result " + result);

        NativeAssert.run(ma);
    }

    private static final class A {
        public static String a(final String s) {
            MainActivity.output("public static A::a hit with " + s);
            return s + "_a";
        }
    }

    private static final class B {
        @SuppressWarnings("unused")
        private static String b(final Class<?> classA, final String s) {
            MainActivity.output("private static B::b hit with " + s
                    + ", class = " + classA);
            return HookHelper.invokeObjectOrigin(null, s) + "_b";
        }
    }

    private static void hookFromDifferentClass() throws Exception {
        if (NativeAssert.isBlackList()) {
            AndHook.ensureClassInitialized(A.class);
            AndHook.ensureClassInitialized(B.class);
        }

        final Method ma = A.class.getDeclaredMethod("a", String.class);
        final Method mb = B.class.getDeclaredMethod("b", Class.class,
                String.class);

        MainActivity.output("hooking public static A::a...");
        HookHelper.hook(ma, mb);

        MainActivity.output("calling public static A::a...");
        final String result = A.a("test");
        MainActivity.output("result = " + result);

        if (!result.endsWith("_a_b"))
            throw new RuntimeException("unexpected result " + result);

        NativeAssert.run(ma);
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("static method hook test...");

        try {
            hookWithinSameClass();

            MainActivity.output("    ");

            hookFromDifferentClass();
        } catch (final Exception e) {
            MainActivity.alert(e);
            return;
        }

        MainActivity.info("static method hook test passed");
    }
}
