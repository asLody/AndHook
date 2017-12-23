package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.AndHook.HookHelper;
import andhook.lib.xposed.XC_MethodReplacement;
import andhook.lib.xposed.XposedBridge;
import andhook.lib.xposed.XposedHelpers;
import andhook.lib.xposed.XC_MethodHook;
import andhook.lib.xposed.XC_MethodHook.MethodHookParam;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.system.OsConstants;
import android.util.Log;

@SuppressWarnings("all")
public final class Xposed {
    public static int print(final String msg, final long v) {
        Log.i(AndTest.LOG_TAG, "print: " + msg + ", " + v);
        return (int) v;
    }

    private static void hook_print() {
        if (print("test", 2018l) != 2018) {
            Log.w(AndTest.LOG_TAG,
                    "********print returns wrong value unexpectedly");
        }

        XposedHelpers.findAndHookMethod(Xposed.class, "print", String.class, long.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(
                            final MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);

                        final String msg = (String) param.args[0];
                        final long v = (long) param.args[1];
                        Log.i(AndTest.LOG_TAG, "original print: " + msg + ", "
                                + v);

                        // param.setResult(0);
                        // param.setThrowable(new
                        // IllegalArgumentException("error"));

                        param.args[0] = "faked";
                    }
                });

        if (print("test", 2018l) != 2018) {
            Log.w(AndTest.LOG_TAG,
                    "********print returns wrong value unexpectedly");
        }
    }

    private static void hook_IoBridge() {
        final Class<?> IoBridge = HookHelper.findClass("libcore/io/IoBridge");
        final Method socket = HookHelper.findMethodHierarchically(IoBridge,
                "socket", boolean.class);

        try {
            Log.i(AndTest.LOG_TAG, "\npre call socket...");
            if (socket.invoke(null, true) == null) {
                Log.w(AndTest.LOG_TAG, "********socket returns null unexpectedly");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }

        final XC_MethodHook.Unhook uk = XposedHelpers.findAndHookMethod(IoBridge, "socket", boolean.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.i(AndTest.LOG_TAG,
                                "!!!!!!!!socket hit, returning null...");
                        return null;
                    }
                });

        try {
            Log.i(AndTest.LOG_TAG, "post call socket...");
            if (socket.invoke(null, true) != null) {
                Log.w(AndTest.LOG_TAG,
                        "********socket returns non-null unexpectedly");
            } else {
                Log.i(AndTest.LOG_TAG, "test call socket passed 0.");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }

        // cancels hook
        uk.unhook();

        try {
            Log.i(AndTest.LOG_TAG, "final call socket...");
            if (socket.invoke(null, true) == null) {
                Log.w(AndTest.LOG_TAG,
                        "********socket returns null unexpectedly");
            } else {
                Log.i(AndTest.LOG_TAG, "test call socket passed 1.");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void hook_IoBridge_O() {
        final Class<?> IoBridge = HookHelper.findClass("libcore/io/IoBridge");
        final Method socket = HookHelper.findMethodHierarchically(IoBridge,
                "socket", int.class, int.class, int.class);

        try {
            Log.i(AndTest.LOG_TAG, "\npre call socket...");
            if (socket.invoke(null, OsConstants.AF_INET, OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) == null) {
                Log.w(AndTest.LOG_TAG, "********socket returns null unexpectedly");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }

        final XC_MethodHook.Unhook uk = XposedHelpers.findAndHookMethod(IoBridge, "socket", int.class, int.class, int.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.i(AndTest.LOG_TAG,
                                "!!!!!!!!socket hit, returning null...");
                        return null;
                    }
                });

        try {
            Log.i(AndTest.LOG_TAG, "post call socket...");
            if (socket.invoke(null, OsConstants.AF_INET, OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) != null) {
                Log.w(AndTest.LOG_TAG,
                        "********socket returns non-null unexpectedly");
            } else {
                Log.i(AndTest.LOG_TAG, "test call socket passed 0.");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }

        // cancels hook
        uk.unhook();

        try {
            Log.i(AndTest.LOG_TAG, "final call socket...");
            if (socket.invoke(null, OsConstants.AF_INET, OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) == null) {
                Log.w(AndTest.LOG_TAG,
                        "********socket returns null unexpectedly");
            } else {
                Log.i(AndTest.LOG_TAG, "test call socket passed 1.");
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "socket test error", e);
        }
    }

    public static void doHook() {
        Log.i(AndTest.LOG_TAG, "********xposed test started.********\n");
        try {
            hook_print();
            Log.i(AndTest.LOG_TAG, "    \n");
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                hook_IoBridge();
            } else {
                hook_IoBridge_O();
            }
        } catch (final Exception e) {
            Log.e(AndTest.LOG_TAG, "xposed test error", e);
        }

        Log.i(AndTest.LOG_TAG, "\n********xposed test done.********");
    }
}
