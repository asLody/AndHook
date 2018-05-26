package andhook.test;

import andhook.lib.xposed.XC_MethodHook;
import andhook.lib.xposed.XposedHelpers;
import andhook.test.ui.MainActivity;

public final class InnerException {
    private static boolean passed = false;

    private static final class k {
        static synchronized void a() {
            MainActivity.output("k->a");
            q.b();
        }

        static synchronized void e() {
            MainActivity.output("k->e");
            q.f();
        }
    }

    private static final class q {
        q() {
            MainActivity.output("q->constructor");
        }

        static synchronized void a() {
            MainActivity.output("q->a");
            k.a();
        }

        static void b() {
            MainActivity.output("q->b");
            c();
            new q();
        }

        static synchronized void c() {
            MainActivity.output("q->c");
            d();
        }

        static synchronized void d() {
            MainActivity.output("q->d");
            try {
                e();
            } catch (final Exception e) {
                MainActivity.output("q->d exception");
                throw new UnsupportedOperationException(e);
            }
        }

        static void e() {
            MainActivity.output("q->e");
            k.e();
        }

        static void f() {
            MainActivity.output("q->f");
            throw new UnsupportedOperationException("test");
        }
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("inner exception test...");

        XposedHelpers.findAndHookMethod(q.class, "b", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param)
                    throws Throwable {
                super.beforeHookedMethod(param);
                MainActivity.output("q->b before");
            }
        });
        XposedHelpers.findAndHookMethod(q.class, "d", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param)
                    throws Throwable {
                super.beforeHookedMethod(param);
                MainActivity.output("q->d before");
                passed = true;
            }
        });

        try {
            q.a();
        } catch (final UnsupportedOperationException e) {
            MainActivity.output(e);
        }

        if (passed)
            MainActivity.info("inner exception test passed");
        else
            MainActivity.alert("inner exception test failed!");

    }
}
