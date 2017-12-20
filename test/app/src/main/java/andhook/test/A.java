package andhook.test;

import android.util.Log;

@SuppressWarnings("all")
public final class A {
    public A() {
        Log.i(AndTest.LOG_TAG, "constructor A::A hit, this is " + this);
    }

    private String a(String a, byte[] b, Object c, Object d, int e, Object f,
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
        Log.i(AndTest.LOG_TAG, "private method A::a hit, this is " + this);
        return "A::a";
    }

    public static String AA(final String s) {
        Log.i(AndTest.LOG_TAG, "public static method A::AA hit!");
        final A a = new A();
        a.a("AA", new byte[1], null, null, 0, null, 0);
        return "return from A::AA with param " + s;
    }
}
