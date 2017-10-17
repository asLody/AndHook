package apk.andhook.test;

import android.util.Log;

@SuppressWarnings("WeakerAccess")
public final class B {
    public B() {
        Log.i(this.getClass().toString(), "Constructor hit");
    }

    public static String BB(final String s) {
        Log.i(B.class.toString(), "public static method B::BB hit!");
        return "return from B::BB with param " + s;
    }
}
