package apk.andhook.test;

import android.util.Log;

@SuppressWarnings("WeakerAccess")
public final class A {
    public A() {
        Log.i(this.getClass().toString(), "Constructor hit");
    }

    public static String AA(final String s) {
        Log.i(A.class.toString(), "public static method A::AA hit!");
        return "return from A::AA with param " + s;
    }
}
