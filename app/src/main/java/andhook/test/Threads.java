package andhook.test;

import andhook.lib.AndHook;
import andhook.lib.xposed.XC_MethodHook;
import andhook.lib.xposed.XC_MethodHook.Unhook;
import andhook.lib.xposed.XposedHelpers;
import andhook.test.ui.MainActivity;

public final class Threads {
    private static boolean passed;

    public static void test() {
        MainActivity.clear();
        MainActivity.output("Thread hook test...");

        passed = false;

        // @NOTE that hooking methods of Thread.class is possibly unstable
        // as they may be on the stack before, which could result in
        // unexpected stack unwinding.
        AndHook.stopDaemons();
        android.os.SystemClock.sleep(100);

        try {
            final class XRunnable implements Runnable {
                private String s;

                XRunnable(final String s) {
                    this.s = s;
                }

                public void run() {
                    MainActivity.output(s);
                }
            }
            final Unhook uk = XposedHelpers.findAndHookMethod(Thread.class,
                    "run", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(
                                final MethodHookParam param) throws Throwable {
                            MainActivity.runAction(new XRunnable("before " + param.thisObject));
                            passed = true;
                        }

                        @Override
                        protected void afterHookedMethod(
                                final MethodHookParam param) throws Throwable {
                            MainActivity.runAction(new XRunnable("after " + param.thisObject));
                        }
                    });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final class FinalAction implements Runnable {
                        private RuntimeException e;

                        private FinalAction(final RuntimeException e) {
                            this.e = e;
                        }

                        public void run() {
                            MainActivity.output(e);

                            if (passed)
                                MainActivity.info("Thread hook test passed");
                            else
                                MainActivity
                                        .alert("failed to hook Thread::run!");
                        }
                    }

                    MainActivity.runAction(new FinalAction(new RuntimeException("test")));
                }
            }, AndTest.LOG_TAG).start();

            android.os.SystemClock.sleep(100);
            AndHook.startDaemons();
            System.gc();
            android.os.SystemClock.sleep(200);

            uk.unhook();
        } catch (final Throwable t) {
            MainActivity.alert(t);
        }
    }
}
