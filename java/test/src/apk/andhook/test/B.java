package apk.andhook.test;

import android.util.Log;

public final class B {
	public static String BB(final String s) {
		Log.i(B.class.toString(), "public static method B::BB hit!");
		return "return from B::BB with param " + s;
	}
}
