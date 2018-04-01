package andhook.test;

import andhook.lib.HookHelper;
import andhook.lib.HookHelper.Hook;
import andhook.ui.MainActivity;

public final class WideningConversion {
    private static final float const_float = 66.66f;
    private static final float const_float_second = 1212.1212f;
    private static final double const_double = 88.88;
    private static final double const_double_second = 1818.1818;
    private static final short const_short = 99;
    private static final long const_long = 5555000033330000L;
    private static final long const_long_second = 8888000066660000888L;

    private static float jfloat(final float a, final double b, final short c, final long d, final long e) {
        MainActivity.output("jfloat hit, float = " + a + ", double = " + b + ", short = " + c +
                ", long = " + d + ", long = " + e);
        if (a != const_float || b != const_double || c != const_short || d != const_long || e != const_long_second)
            throw new RuntimeException("Unexpected arguments!");
        return const_float_second;
    }

    @SuppressWarnings("unused")
    @Hook(clazz = WideningConversion.class, name = "jfloat")
    private static float fake_jfloat(final Class<?> clazz, final float a,
                                     final double b, final short c, final long d, final long e) {
        MainActivity.output("faked_jfloat hit, float = " + a + ", double = " + b +
                ", short = " + c + ", long = " + d + ", long = " + e);
        final float fr = HookHelper.invokeFloatOrigin(null, a, b, c, d, e);
        return fr == const_float_second ? const_float : 0;
    }

    private static double jdouble(final double a, final long b, final short c, final long d) {
        MainActivity.output("jdouble hit, double = " + a + ", long = " + b + ", short = " + c + ", long = " + d);
        return const_double_second;
    }

    @SuppressWarnings("unused")
    @Hook(clazz = WideningConversion.class, name = "jdouble")
    private static double fake_jdouble(final Class<?> clazz, final double a,
                                       final long b, final short c, final long d) {
        MainActivity.output("faked_jdouble hit, double = " + a + ", long = " + b + ", short = " + c + ", long = " + d);
        final double dr = HookHelper.invokeDoubleOrigin(null, a, b, c, d);
        return dr == const_double_second ? const_double : 0;
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("widening conversion test...");

        try {
            float f = jfloat(const_float, const_double, const_short, const_long, const_long_second);
            if (f != const_float_second && f != const_float)
                throw new RuntimeException("Unexpected result of jfloat!");
            double d = jdouble(const_double, const_long_second, const_short, const_long);
            if (d != const_double_second && d != const_double)
                throw new RuntimeException("Unexpected result of jdouble!");

            HookHelper.applyHooks(WideningConversion.class);

            f = jfloat(const_float, const_double, const_short, const_long, const_long_second);
            if (f != const_float)
                throw new RuntimeException("Unexpected result of jfloat after hook!");
            d = jdouble(const_double, const_long_second, const_short, const_long);
            if (d != const_double)
                throw new RuntimeException("Unexpected result of jdouble after hook!");
        } catch (final Exception e) {
            MainActivity.alert(e.toString());
            return;
        }

        MainActivity.info("widening conversion test passed");
    }
}
