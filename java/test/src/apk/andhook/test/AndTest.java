package apk.andhook.test;

import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.provider.Settings.Secure;
import android.util.Log;
import apk.andhook.AndHook.HookHelper;

public class AndTest {
	private static void testHookStaticMethodFromDifferentClass() {
		try {
			// The following code should be called once and only once
			// as A.class and B.class may not have been initialized and
			// AndHook will keep global reference of them to prevent GC collections.
			apk.andhook.AndHook.ensureClassInitialized(A.class);
			apk.andhook.AndHook.ensureClassInitialized(B.class);
			
			final Method m1 = A.class.getDeclaredMethod("AA",
					String.class);
			final Method m2 = B.class.getDeclaredMethod("BB",
					String.class);
			Log.d(AndTest.class.toString(),
					"begin hook public static method A::AA...");
			HookHelper.hook(m1, m2);
			Log.d(AndTest.class.toString(), "end hook public static method A::AA");

			Log.d(AndTest.class.toString(),
					"calling public static method A::AA...");
			Log.d(AndTest.class.toString(), "public static method A::AA returns ["
					+ A.AA("test") + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String a1(final String s) {
		Log.d(AndTest.class.toString(), "public static method a1 hit!");
		return "return from a1 with param " + s;
	}

	public static String a2(final String s) {
		Log.d(AndTest.class.toString(), "public static method a2 hit!");
		try {
			final Object obj = HookHelper.callStaticObjectOrigin(AndTest.class,
					s + "+a2");
			Log.d(AndTest.class.toString(), "callStaticObjectOrigin return "
					+ obj);
			obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "return from a2 with param " + s;
	}

	private static void testHookStaticMethod() {
		try {
			final Method m1 = AndTest.class.getDeclaredMethod("a1",
					String.class);
			final Method m2 = AndTest.class.getDeclaredMethod("a2",
					String.class);
			Log.d(AndTest.class.toString(),
					"begin hook public static method a1...");
			HookHelper.hook(m1, m2);
			Log.d(AndTest.class.toString(), "end hook public static method a1");

			Log.d(AndTest.class.toString(),
					"calling public static method a1...");
			Log.d(AndTest.class.toString(), "public static method a1 returns ["
					+ a1("test") + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String b1(final String s) {
		Log.d(AndTest.class.toString(), "public method b1 hit!");
		Log.d(AndTest.class.toString(), "this class is "
				+ this.getClass().getName());
		return "return from b1 with param " + s;
	}

	public String b2(final String s) {
		Log.d(AndTest.class.toString(), "public method b2 hit!");
		Log.d(AndTest.class.toString(), "this class is "
				+ this.getClass().getName());
		try {
			final Object obj = HookHelper.callObjectOrigin(this, s + "+b2");
			Log.d(AndTest.class.toString(), "callObjectOrigin return " + obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "return from b2 with param " + s;
	}

	private static void testHookMethod() {
		try {
			final Method m1 = AndTest.class.getDeclaredMethod("b1",
					String.class);
			final Method m2 = AndTest.class.getDeclaredMethod("b2",
					String.class);
			Log.d(AndTest.class.toString(), "begin hook public method b1...");
			HookHelper.hook(m1, m2);
			Log.d(AndTest.class.toString(), "end hook public method b1");

			Log.d(AndTest.class.toString(), "calling public method b1...");
			Log.d(AndTest.class.toString(), "public method b1 returns ["
					+ (new AndTest()).b1("test") + "]");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getString(ContentResolver resolver, String name) {
		Log.d(AndTest.class.toString(), "hit name = " + name);
		return (String) HookHelper.callStaticObjectOrigin(Secure.class,
				resolver, name);
	}

	private static void testHookSystemStaticMethod() {
		final String target = "getString";
		try {
			final Method origin = Secure.class.getDeclaredMethod(target,
					ContentResolver.class, String.class);
			final Method fake = AndTest.class.getDeclaredMethod(target,
					ContentResolver.class, String.class);
			HookHelper.hook(origin, fake);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void RunTest(final android.content.Context context,
			final android.content.ContentResolver resolver) {
		testHookStaticMethodFromDifferentClass();
		testHookStaticMethod();
		testHookMethod();

		Log.d("SecureHook", Secure.getString(resolver, Secure.ANDROID_ID));
		testHookSystemStaticMethod();
		Log.d("SecureHook", Secure.getString(resolver, Secure.ANDROID_ID));
	}
}
