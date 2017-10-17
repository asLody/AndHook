package apk.andhook.test;

import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

import andhook.lib.AndHook;
import andhook.lib.AndHook.HookHelper;
import andhook.lib.AndHook.HookHelper.Hook;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AndTest {

    // @Hook(clazz = A.class)
    @Hook(clazz = A.class, name = "<init>")
    private void A() {
        Log.i(this.getClass().toString(), "Fake constructor hit");
        HookHelper.callVoidOrigin(this);
    }

    @Hook(clazz = android.app.Activity.class)
    public void onCreate(final Bundle savedInstanceState) {
        Log.i(this.getClass().toString(), "Activity::onCreate start");
        HookHelper.callVoidOrigin(this, savedInstanceState);
        Log.i(this.getClass().toString(), "Activity::onCreate end");
    }

    @Hook(clazz = Secure.class)
    public static String getString(final ContentResolver resolver,
                                   final String name) {
        Log.i(AndTest.class.toString(), "hit name = " + name);
        if (name.equals(Secure.ANDROID_ID))
            return "8888888888888888";
        return (String) HookHelper.callStaticObjectOrigin(Secure.class,
                resolver, name);
    }

    private static void testHookStaticMethodFromDifferentClass() {
        try {
            // The following code should be called once and only once
            // as A.class and B.class may not have been initialized.
            AndHook.ensureClassInitialized(A.class);
            AndHook.ensureClassInitialized(B.class);

            final Method m1 = A.class.getDeclaredMethod("AA", String.class);
            final Method m2 = B.class.getDeclaredMethod("BB", String.class);
            Log.i(AndTest.class.toString(),
                    "begin hook public static method A::AA...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.class.toString(),
                    "end hook public static method A::AA");

            Log.i(AndTest.class.toString(),
                    "calling public static method A::AA...");
            Log.i(AndTest.class.toString(),
                    "public static method A::AA returns [" + A.AA("test") + "]");
        } catch (Exception e) {
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
            final Object obj = HookHelper.callStaticObjectOrigin(AndTest.class,
                    s + "+a2");
            Log.i(AndTest.class.toString(), "callStaticObjectOrigin return "
                    + obj);
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
            Log.i(AndTest.class.toString(),
                    "begin hook public static method a1...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.class.toString(), "end hook public static method a1");

            Log.i(AndTest.class.toString(),
                    "calling public static method a1...");
            Log.i(AndTest.class.toString(), "public static method a1 returns ["
                    + a1("test") + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String b1(final String s) {
        Log.i(AndTest.class.toString(), "public method b1 hit!");
        Log.i(AndTest.class.toString(), "this class is "
                + this.getClass().getName());
        return "return from b1 with param " + s;
    }

    public String b2(final String s) {
        Log.i(AndTest.class.toString(), "public method b2 hit!");
        Log.i(AndTest.class.toString(), "this class is "
                + this.getClass().getName());
        try {
            final Object obj = HookHelper.callObjectOrigin(this, s + "+b2");
            Log.i(AndTest.class.toString(), "callObjectOrigin return " + obj);
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
            Log.i(AndTest.class.toString(), "begin hook public method b1...");
            HookHelper.hook(m1, m2);
            Log.i(AndTest.class.toString(), "end hook public method b1");

            Log.i(AndTest.class.toString(), "calling public method b1...");
            Log.i(AndTest.class.toString(), "public method b1 returns ["
                    + (new AndTest()).b1("test") + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("HardwareIds")
    public static void RunTest(final android.content.Context context,
                               final android.content.ContentResolver resolver) {
        // hook using base AndHook api
        AndHook.suspendAll();
        testHookStaticMethodFromDifferentClass();
        testHookStaticMethod();
        testHookMethod();
        AndHook.resumeAll();

        // hook using HookHelper.applyHooks
        Log.i("ANDROID_ID", Secure.getString(resolver, Secure.ANDROID_ID));
        HookHelper.applyHooks(AndTest.class, context.getClassLoader());
        Log.i("ANDROID_ID_FAKE", Secure.getString(resolver, Secure.ANDROID_ID));
        // test call constructor
        new A();

        // hook in C/C++
        System.loadLibrary("AndHook"); // libAndHook.so must be loaded before libmyjnihook.so
        System.loadLibrary("myjnihook");
    }
}
