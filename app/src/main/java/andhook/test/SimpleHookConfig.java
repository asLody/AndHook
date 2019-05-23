package andhook.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import andhook.lib.HookHelper;

/**
 * @author <a href="mailto:qq2325690622@gmail.com>Deng Chao</a> on 2019/5/23
 */
public class SimpleHookConfig {
    public static boolean passed = false;

    /**
     * 对 Activity 的 onCreate(Bundle); 方法进行 Hook
     *
     * @param objActivity        被 Hook 的 activity 对象
     * @param savedInstanceState onCreate(Bundle) 的 bundle 入参
     */
    @SuppressWarnings("unused")
    @HookHelper.Hook(clazz = Activity.class, name = "onCreate")
    private static void Activity_onCreate(final Object objActivity, final Bundle savedInstanceState) {
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate start, this is " + objActivity.getClass());
        HookHelper.invokeVoidOrigin(objActivity, savedInstanceState);
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate end");
        passed = true;
    }
}
