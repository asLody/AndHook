package andhook.test.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import andhook.lib.AndHook;
import andhook.lib.HookHelper;
import andhook.lib.HookHelper.Hook;
import andhook.test.AndTest;
import andhook.test.Constructor;
import andhook.test.GC;
import andhook.test.InnerException;
import andhook.test.JNI;
import andhook.test.Native;
import andhook.test.R;
import andhook.test.Static;
import andhook.test.SystemClass;
import andhook.test.Threads;
import andhook.test.Virtual;
import andhook.test.WideningConversion;
import andhook.test.Xposed;
import andhook.test.app.MainApplication;

@SuppressWarnings("all")
public class MainActivity extends Activity {
    private static final String TAG = AndTest.LOG_TAG;
    private static boolean passed = false;
    private static MainActivity thiz = null;
    private static EditText tv_status = null;
    private static TextView tv_more = null;
    private static CharSequence cv_more = null;

    /**
     * 对 Activity 的 onCreate(Bunlde); 方法进行 Hook
     *
     * @param objActivity        被 Hook 的 activity 对象
     * @param savedInstanceState onCreate(Bunlde) 的 bundle 入参
     */
    @Hook(clazz = Activity.class, name = "onCreate")
    private static void Activity_onCreate(final Object objActivity, final Bundle savedInstanceState) {
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate start, this is " + objActivity.getClass());
        HookHelper.invokeVoidOrigin(objActivity, savedInstanceState);
        Log.i(AndTest.LOG_TAG, "HookedActivity::onCreate end");
        passed = true;
    }

    public static void runAction(final Runnable action) {
        if (thiz != null)
            thiz.runOnUiThread(action);
    }

    @SuppressWarnings("deprecation")
    public static void alert(final String s) {
        Log.e(AndTest.LOG_TAG, s);
        if (tv_more != null)
            tv_more.setText(Html.fromHtml("<font color=red>" + cv_more
                    + "</font>"));
        if (tv_status != null)
            tv_status.append(Html.fromHtml("<font color=red>"
                    + s.replace("\n", "<br/>") + "</font><br/>"));
    }

    @SuppressWarnings("deprecation")
    public static void info(final String s) {
        Log.i(AndTest.LOG_TAG, s);
        if (tv_status != null)
            tv_status.append(Html.fromHtml("<font color=green>"
                    + s.replace("\n", "<br/>") + "</font><br/>"));
    }

    public static void alert(final Throwable t) {
        alert(Log.getStackTraceString(t).trim());
    }

    public static void output(final String s) {
        Log.v(AndTest.LOG_TAG, s);
        if (tv_status != null)
            tv_status.append(s + "\n");
    }

    public static void output(final Throwable t) {
        output(Log.getStackTraceString(t).trim());
    }

    public static void clear() {
        if (tv_more != null)
            tv_more.setText(cv_more);
        if (tv_status != null) {
            tv_status.setText(null);
            tv_status.scrollTo(0, 0);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // 从指定的配置类中加载被 @Hook 标记的方法.
        // 本例演示的是加载 MainActivity 中的 Activity_onCreate(Activity, Bundle);
        Log.d(TAG, "MainActivity::onCreate: before apply @Hook");
        HookHelper.applyHooks(MainActivity.class);
        Log.d(TAG, "MainActivity::onCreate: after apply @Hook");


        Log.i(AndTest.LOG_TAG, "MainActivity.super::onCreate start");
        super.onCreate(savedInstanceState);
        Log.i(AndTest.LOG_TAG, "MainActivity.super::onCreate end");


        setContentView(R.layout.activity_main);

        thiz = this;
        tv_status = findViewById(R.id.status);
        tv_status.setMovementMethod(new ScrollingMovementMethod());
        tv_status.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                try {
                    final ClipboardManager cm = (ClipboardManager) thiz
                            .getSystemService(MainApplication.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(AndTest.LOG_TAG,
                            tv_status.getText().toString()));
                } catch (final Exception e) {
                    Log.wtf(AndTest.LOG_TAG, e);
                }

                Toast.makeText(thiz, "Copied!", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        tv_more = findViewById(R.id.more);
        cv_more = tv_more.getText();

        clear();
        output(AndHook.class + " version " + AndHook.VERSION + " ("
                + AndHook.getVersionInfo() + ")");
        if (!passed)
            alert("Activity::onCreate hook failed!");

        findViewById(R.id.JNI).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                JNI.test();
            }
        });
        findViewById(R.id.Xposed).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Xposed.test();
            }
        });
        findViewById(R.id.Constructor).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Constructor.test();
                    }
                });
        findViewById(R.id.GC).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                GC.test();
            }
        });
        findViewById(R.id.Static).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Static.test();
            }
        });
        findViewById(R.id.Virtual).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Virtual.test();
            }
        });
        findViewById(R.id.WideningConversion).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        WideningConversion.test();
                    }
                });
        findViewById(R.id.SystemClass).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        SystemClass.test(MainActivity.this.getContentResolver());
                    }
                });
        findViewById(R.id.Native).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Native.test();
                    }
                });
        findViewById(R.id.Thread).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Threads.test();
            }
        });
        findViewById(R.id.Exception).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                InnerException.test();
            }
        });
    }
}
