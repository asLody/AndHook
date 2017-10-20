package apk.andhook.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import andhook.lib.AndHook;
import andhook.lib.AndHook.HookHelper;
import andhook.lib.AndHook.HookHelper.Hook;
import apk.andhook.test.Virtual.Virtual0;
import apk.andhook.test.Virtual.Virtual1;
import apk.andhook.test.Virtual.Virtual2;

@SuppressWarnings({ "unused", "WeakerAccess" })
public class AndTest {
	private final static String LOG_TAG = "AndHook_Test";

	@Hook(clazz = A.class, name = "<init>")
	private void A() {
		Log.i(this.getClass().toString(), "Fake constructor hit");
		HookHelper.invokeVoidOrigin(this);
	}

	@Hook(clazz = android.app.Activity.class)
	public void onCreate(final Bundle savedInstanceState) {
		Log.i(this.getClass().toString(), "Activity::onCreate start");
		HookHelper.invokeVoidOrigin(this, savedInstanceState);
		Log.i(this.getClass().toString(), "Activity::onCreate end");
	}

	@Hook(clazz = Secure.class)
	public static String getString(final ContentResolver resolver,
			final String name) {
		Log.i(AndTest.class.toString(), "hit name = " + name);
		if (name.equals(Secure.ANDROID_ID))
			return "8888888888888888";
		return (String) HookHelper.invokeObjectOrigin(null, resolver, name);
	}

	private static void testHookStaticMethodFromDifferentClass() {
		try {
			// The following code should be called once and only once
			// as A.class and B.class may not have been initialized.
			AndHook.ensureClassInitialized(A.class);
			AndHook.ensureClassInitialized(B.class);

			final Method m1 = A.class.getDeclaredMethod("AA", String.class);
			final Method m2 = B.class.getDeclaredMethod("BB", String.class);
			Log.i(LOG_TAG, "begin hook public static method A::AA...");
			HookHelper.hook(m1, m2);
			Log.i(LOG_TAG, "end hook public static method A::AA");

			Log.i(LOG_TAG, "calling public static method A::AA...");
			Log.i(LOG_TAG,
					"public static method A::AA returns [" + A.AA("test") + "]");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static String a1(final String s) {
		Log.i(AndTest.class.toString(), "public static method a1 hit!");
		return "return from a1 with param " + s;
	}

	public static String a2(final String s) {
		Log.i(AndTest.class.toString(), "public static method a2 hit!");
		try {
			final Object obj = HookHelper.invokeObjectOrigin(null, s + "+a2");
			Log.i(LOG_TAG, "invokeObjectOrigin[static] return " + obj);
		} catch (final Exception e) {
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
			Log.i(LOG_TAG, "begin hook public static method a1...");
			HookHelper.hook(m1, m2);
			Log.i(LOG_TAG, "end hook public static method a1");

			Log.i(LOG_TAG, "calling public static method a1...");
			Log.i(LOG_TAG, "public static method a1 returns [" + a1("test")
					+ "]");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public String b1(final String s) {
		Log.i(LOG_TAG, "public method b1 hit!");
		Log.i(LOG_TAG, "this class is " + this.getClass().getName());
		return "return from b1 with param " + s;
	}

	public String b2(final String s) {
		Log.i(LOG_TAG, "public method b2 hit!");
		Log.i(LOG_TAG, "this class is " + this.getClass().getName());
		try {
			final Object obj = HookHelper.invokeObjectOrigin(this, s + "+b2");
			Log.i(LOG_TAG, "invokeObjectOrigin return " + obj);
		} catch (final Exception e) {
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
			Log.i(LOG_TAG, "begin hook public method b1...");
			HookHelper.hook(m1, m2);
			Log.i(LOG_TAG, "end hook public method b1");

			Log.i(LOG_TAG, "calling public method b1...");
			Log.i(LOG_TAG,
					"public method b1 returns [" + (new AndTest()).b1("test")
							+ "]");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("HardwareIds")
	public static void RunTest(final ContextWrapper context,
			final ContentResolver resolver) {
		Log.i("RunTest", "\nhook using base AndHook api...");
		AndHook.suspendAll();
		testHookStaticMethodFromDifferentClass();
		testHookStaticMethod();
		testHookMethod();
		AndHook.resumeAll();

		Log.i("RunTest", "\nhook using HookHelper.applyHooks...");
		Log.i("ANDROID_ID", Secure.getString(resolver, Secure.ANDROID_ID));
		HookHelper.applyHooks(AndTest.class, context.getClassLoader());
		Log.i("ANDROID_ID_FAKE", Secure.getString(resolver, Secure.ANDROID_ID));
		// test call constructor
		new A();

		Log.i("RunTest", "\nhook parent's virtual method...");
		// test pre call
		(new Virtual1()).getUserId();
		// hook
		AndHook.hookNoBackup(
				HookHelper.findMethod(Virtual1.class, "getUserId", (Class<?>[]) null), 
				HookHelper.findMethod(Virtual2.class, "getUserId", (Class<?>[]) null));
		// test post call
		(new Virtual1()).getUserId();

		Log.i("RunTest", "\nhook in C/C++...");
		// libAndHook.so must be loaded before libmyjnihook.so
		System.loadLibrary("AndHook");
		try {
			System.loadLibrary("myjnihook");
		} catch (final UnsatisfiedLinkError e) {
			Log.w("RunTest", "failed to load myjnihook!");
		}

		Log.i("RunTest", "\nhook test done.\n");
	}
}
