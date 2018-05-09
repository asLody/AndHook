package andhook.test;

import andhook.lib.HookHelper;
import andhook.lib.HookHelper.Hook;
import andhook.test.ui.MainActivity;

public final class Native {
    private static boolean passed = false;

    // @CriticalNative since Android O
    @Hook(clazz = android.os.SystemClock.class, need_origin = true)
    private static long currentThreadTimeMillis(final Class<?> clazz) {
        passed = true;
        MainActivity.output("currentThreadTimeMillis called, class = " + clazz);
        MainActivity.output(new RuntimeException("test"));
        return HookHelper.invokeLongOrigin(null);
    }

    public static void test() {
        MainActivity.clear();
        MainActivity.output("hook native java...");

        passed = false;
        HookHelper.applyHooks(Native.class);
        android.os.SystemClock.currentThreadTimeMillis();

        if (passed)
            MainActivity.info("native java hook test passed");
        else
            MainActivity.alert("native java hook test failed");
    }
}
