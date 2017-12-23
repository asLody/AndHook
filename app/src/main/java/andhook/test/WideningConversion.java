package andhook.test;

import andhook.lib.AndHook.HookHelper;
import andhook.lib.AndHook.HookHelper.Hook;

import android.util.Log;

@SuppressWarnings("all")
public final class WideningConversion {
    private static float jfloat(final float a, final double b, final short c, final long e) {
        Log.i(AndTest.LOG_TAG, "jfloat hit, a = " + a + ", b = " + b + ", c = " + c + ", e = " + e);
        return 12.12f;
    }

    @Hook(clazz = WideningConversion.class, name = "jfloat")
    private static float fake_jfloat(final Class<?> clazz, final float a,
                                     final double b, final short c, final long e) {
        Log.i(AndTest.LOG_TAG, "fake_jfloat hit, a = " + a + ", b = " + b + ", c = " + c + ", e = " + e);
        Log.i(AndTest.LOG_TAG,
                "original jfloat = " + HookHelper.invokeFloatOrigin(null, a, b, c, e));
        return 11.11f;
    }

    private static double jdouble(final double a, final long b, final short c, final long e) {
        Log.i(AndTest.LOG_TAG, "jdouble hit, a = " + a + ", b = " + b + ", c = " + c + ", e = " + e);
        return 2017.2017f;
    }

    @Hook(clazz = WideningConversion.class, name = "jdouble")
    private static double fake_jdouble(final Class<?> clazz, final double a,
                                       final long b, final short c, final long e) {
        Log.i(AndTest.LOG_TAG, "fake_jdouble hit, a = " + a + ", b = " + b + ", c = " + c + ", e = " + e);
        Log.i(AndTest.LOG_TAG,
                "original jdouble = " + HookHelper.invokeDoubleOrigin(null, a, b, c, e));
        return 2018.2018f;
    }

    public static void doHook() {
        // test call
        Log.i(AndTest.LOG_TAG, "jfloat = " + jfloat(66.66f, 88.88, (short) 99, 5555000033330000l));
        Log.i(AndTest.LOG_TAG, "jdouble = " + jdouble(88.88, 8888000088880000l, (short) 99, 5555000033330000l));

        // hook using HookHelper
        HookHelper.applyHooks(WideningConversion.class);

        // test call
        Log.i(AndTest.LOG_TAG, "jfloat = " + jfloat(66.66f, 88.88, (short) 99, 5555000033330000l));
        Log.i(AndTest.LOG_TAG, "jdouble = " + jdouble(88.88, 8888000088880000l, (short) 99, 5555000033330000l));
    }
}
