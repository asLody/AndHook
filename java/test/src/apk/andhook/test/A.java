package apk.andhook.test;

import android.util.Log;

public final class A {
	public static String AA(final String s) {
		Log.i(A.class.toString(), "public static method A::AA hit!");
		return "return from A::AA with param " + s;
	}
}
