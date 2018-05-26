package andhook.test;

import java.lang.reflect.Method;

import andhook.lib.HookHelper;
import andhook.lib.xposed.XC_MethodReplacement;
import andhook.lib.xposed.XposedBridge;
import andhook.lib.xposed.XposedHelpers;
import andhook.lib.xposed.XC_MethodHook;
import andhook.test.ui.MainActivity;

import android.annotation.TargetApi;
import android.os.Build;
import android.system.OsConstants;

public final class Xposed {
    private static boolean hooked = false;

    private static int print(final String msg, final long v) {
        MainActivity.output("print: " + msg + ", " + v);
        return (int) (msg.equals("test") ? -v : v);
    }

    private static boolean hook_print() {
        if (!hooked) {
            if (print("test", 2018L) != -2018) {
                MainActivity.alert("print returns wrong value unexpectedly");
                return false;
            }

            XposedHelpers.findAndHookMethod(Xposed.class, "print",
                    String.class, long.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            final String msg = (String) param.args[0];
                            final long v = (long) param.args[1];
                            MainActivity.output("original print: " + msg + ", "
                                    + v);

                            param.args[0] = "faked";
                            param.args[1] = v + 1L;
                        }
                    });
        }

        if (print("test", 2019L) != 2020) {
            MainActivity.alert("print returns wrong value unexpectedly after hook");
            return false;
        }

        hooked = true;
        return true;
    }

    private static boolean hook_IoBridge() {
        final Class<?> IoBridge = HookHelper.findClass("libcore/io/IoBridge");
        final Method socket = HookHelper.findMethodHierarchically(IoBridge,
                "socket", boolean.class);

        try {
            MainActivity.output("pre call socket...");
            if (socket.invoke(null, true) == null) {
                MainActivity.alert("socket returns null unexpectedly");
                return false;
            }
        } catch (final Exception e) {
            MainActivity.alert(e);
            return false;
        }

        final XC_MethodHook.Unhook uk = XposedBridge.hookMethod(socket,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param)
                            throws Throwable {
                        MainActivity.output("socket hit, returning null...");
                        return null;
                    }
                });

        try {
            MainActivity.output("post call socket...");
            if (socket.invoke(null, true) != null) {
                MainActivity.alert("socket returns non-null unexpectedly after hook");
                return false;
            }
        } catch (final Exception e) {
            MainActivity.alert(e);
            return false;
        }

        // As it's possible to have more than one XC_MethodHook per method hooked,
        // uk.unhook() can be used to remove the XC_MethodHook represented by uk,
        // while uk.restore() will cancel hook utterly (remove all the XC_MethodHook and restore any modifications).
        uk.restore();

        try {
            MainActivity.output("final call socket...");
            if (socket.invoke(null, true) == null) {
                MainActivity.alert("socket returns null unexpectedly");
                return false;
            }
        } catch (final Exception e) {
            MainActivity.alert(e);
            return false;
        }

        return true;
    }

    private static final class O {
        @TargetApi(Build.VERSION_CODES.O)
        private static boolean hook_IoBridge() {
            final Class<?> IoBridge = HookHelper
                    .findClass("libcore/io/IoBridge");
            final Method socket = HookHelper.findMethodHierarchically(IoBridge,
                    "socket", int.class, int.class, int.class);

            try {
                MainActivity.output("pre call socket...");
                if (socket.invoke(null, OsConstants.AF_INET,
                        OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) == null) {
                    MainActivity.alert("socket returns null unexpectedly");
                    return false;
                }
            } catch (final Exception e) {
                MainActivity.alert(e);
                return false;
            }

            final XC_MethodHook.Unhook uk = XposedBridge.hookMethod(socket,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(
                                MethodHookParam param) throws Throwable {
                            MainActivity
                                    .output("socket hit, returning null...");
                            return null;
                        }
                    });

            try {
                MainActivity.output("post call socket...");
                if (socket.invoke(null, OsConstants.AF_INET,
                        OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) != null) {
                    MainActivity.alert("socket returns non-null unexpectedly after hook");
                    return false;
                }
            } catch (final Exception e) {
                MainActivity.alert(e);
                return false;
            }

            // cancels hook
            uk.unhook();

            try {
                MainActivity.output("final call socket...");
                if (socket.invoke(null, OsConstants.AF_INET,
                        OsConstants.SOCK_STREAM, OsConstants.IPPROTO_TCP) == null) {
                    MainActivity.alert("socket returns null unexpectedly");
                    return false;
                }
            } catch (final Exception e) {
                MainActivity.alert(e);
                return false;
            }

            return true;
        }
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("xposed test...");

        if (!hook_print())
            return;

        MainActivity.output("    ");

        final boolean passed = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 ? hook_IoBridge()
                : O.hook_IoBridge();
        if (passed)
            MainActivity.info("xposed test passed");
    }
}
