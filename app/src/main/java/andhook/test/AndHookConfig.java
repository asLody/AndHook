package andhook.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import andhook.lib.HookHelper;
import andhook.test.ui.MainActivity;

/**
 * @author <a href="mailto:qq2325690622@gmail.com>Deng Chao</a> on 2019/5/23
 */
@SuppressWarnings("unused")
public class AndHookConfig {
    private static final String TAG = AndTest.LOG_TAG;
    public static boolean passed = false;

    /**
     * 对 Activity 的 onCreate(Bundle); 方法进行 Hook.
     *
     * @param objActivity        被 Hook 的 activity 对象
     * @param savedInstanceState onCreate(Bundle) 的 bundle 入参
     * @since v3.6.2
     */
    @HookHelper.Hook(clazz = Activity.class, name = "onCreate")// 指定 Hook 的类与方法
    private static void Activity_onCreate(final Object objActivity, final Bundle savedInstanceState) {
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate start, this is " + objActivity.getClass());
        HookHelper.invokeVoidOrigin(objActivity, savedInstanceState);
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate end");
        passed = true;
    }

    /**
     * 使用当前方法的方法名进行 Hook
     * <p>
     * 第一个参数可以使用被 Hook 的类型
     *
     * @since v3.6.2
     */
    @HookHelper.Hook(clazz = Activity.class)
    private static void onStart(Activity activity) {
        Log.d(TAG, "onStart: HookedActivity::onStart start, this is " + activity.getClass());
        HookHelper.invokeVoidOrigin(activity);
        Log.d(TAG, "onStart: HookedActivity::onStart end, this is " + activity.getClass());
    }

    /**
     * 必须使用正确的方法签名进行配置.
     * <p>
     * 使用错误的方法签名进行配置时会出现错误日志:
     * AndHook: failed to find method onResume of class android.app.Activity
     *
     * @since v3.6.2
     */
    @HookHelper.Hook(clazz = Activity.class)
    private static void onResume(Object activity, Bundle bundle) {
        Log.d(TAG, "onResume: HookedActivity::onResume start, this is " + activity.getClass() + " with bundle " + bundle);
        HookHelper.invokeVoidOrigin(activity);
        Log.d(TAG, "onResume: HookedActivity::onResume end, this is " + activity.getClass());
    }

    /**
     * 修改参数
     */
    @HookHelper.Hook(clazz = MainActivity.class)
    private static void logTheNumber(MainActivity activity, int number) {
        HookHelper.invokeVoidOrigin(activity, 2);
    }

    /**
     * 必须使用静态方法进行配置.
     * <p>
     * 使用非静态方法进行配置时会出现错误日志:
     * AndHook: method andhook.test.AndHookConfig@onResume must be static and its first argument must be Class<?> or Object!
     *
     * @since v3.6.2
     */
    @HookHelper.Hook(clazz = Activity.class, name = "onResume")
    private void nonStaticConfig(Object activity) {
        Log.d(TAG, "nonStaticConfig: This is a actually not called");
        HookHelper.invokeVoidOrigin(activity);
    }

}
