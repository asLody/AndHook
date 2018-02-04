package andhook.test;

import andhook.lib.HookHelper;

import android.util.Log;

@SuppressWarnings("all")
public final class B {
    public B() {
        Log.i(AndTest.LOG_TAG, "constructor B::B hit, this is " + this);
    }

    private String b(String a, byte[] b, Object c, Object d, int e, Object f,
                     int g) {
        if (b != null) {
            if (a != null) {
                c = null;
                d = null;
                f = null;
            }
            e = 0;
            g = 0;
        }
        Log.i(AndTest.LOG_TAG, "private method B::b hit, this is " + this);
        return "B::b";
    }

    public static String BB(final Class<?> classA, final String s) {
        Log.i(AndTest.LOG_TAG, "public static method B::BB hit, class = " + classA);

        // shuffles bytecode
        double da = 0.01;
        double db = da + 0.01;
        da = db + 0.01;

        final B b = new B();
        b.b("BB", new byte[1], null, null, 0, null, 0);
        Log.i(AndTest.LOG_TAG, "invoking original public static method A::AA!");
        HookHelper.invokeObjectOrigin(null, s);
        return "return from B::BB with param " + s;
    }
}
