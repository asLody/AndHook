package andhook.test.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import andhook.lib.AndHook;
import andhook.test.AndHookConfig;
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

public class MainActivity extends Activity {
    private static final String TAG = AndTest.LOG_TAG;
    @SuppressLint("StaticFieldLeak")
    private static MainActivity thiz = null;
    @SuppressLint("StaticFieldLeak")
    private static EditText tv_status = null;
    @SuppressLint("StaticFieldLeak")
    private static TextView tv_more = null;
    private static CharSequence cv_more = null;

    public static void runAction(final Runnable action) {
        if (thiz != null)
            thiz.runOnUiThread(action);
    }

    //region log to both logcat and ui
    public static void alert(final String s) {
        Log.e(AndTest.LOG_TAG, s);
        if (tv_more != null)
            tv_more.setText(Html.fromHtml("<font color=red>" + cv_more
                    + "</font>"));
        if (tv_status != null)
            tv_status.append(Html.fromHtml("<font color=red>"
                    + s.replace("\n", "<br/>") + "</font><br/>"));
    }

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
    //endregion

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.i(AndTest.LOG_TAG, "MainActivity.super::onCreate start");
        super.onCreate(savedInstanceState);
        Log.i(AndTest.LOG_TAG, "MainActivity.super::onCreate end");

        logTheNumber(1);

        setContentView(R.layout.activity_main);

        thiz = this;
        tv_status = findViewById(R.id.status);
        tv_status.setMovementMethod(new ScrollingMovementMethod());
        tv_status.setOnLongClickListener(v -> {
            //region Copy content to clipboard
            final ClipboardManager cm = (ClipboardManager) getSystemService(MainApplication.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(AndTest.LOG_TAG, tv_status.getText().toString()));
            Toast.makeText(thiz, "Copied!", Toast.LENGTH_LONG).show();
            return true;
            //endregion
        });
        tv_more = findViewById(R.id.more);
        cv_more = tv_more.getText();

        clear();

        output(AndHook.class + " version " + AndHook.VERSION + " (" + AndHook.getVersionInfo() + ")");

        if (!AndHookConfig.passed) alert("Activity::onCreate hook failed!");

        findViewById(R.id.JNI).setOnClickListener(v -> JNI.test());
        findViewById(R.id.Xposed).setOnClickListener(v -> Xposed.test());
        findViewById(R.id.Constructor).setOnClickListener(v -> Constructor.test());
        findViewById(R.id.GC).setOnClickListener(v -> GC.test());
        findViewById(R.id.Static).setOnClickListener(v -> Static.test());
        findViewById(R.id.Virtual).setOnClickListener(v -> Virtual.test());
        findViewById(R.id.WideningConversion).setOnClickListener(v -> WideningConversion.test());
        findViewById(R.id.SystemClass).setOnClickListener(v -> SystemClass.test(getContentResolver()));
        findViewById(R.id.Native).setOnClickListener(v -> Native.test());
        findViewById(R.id.Thread).setOnClickListener(v -> Threads.test());
        findViewById(R.id.Exception).setOnClickListener(v -> InnerException.test());
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "MainActivity.super::onStart: start");
        super.onStart();
        Log.i(TAG, "MainActivity.super::onStart: end");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "MainActivity.super::onResume: start");
        super.onResume();
        Log.i(TAG, "MainActivity.super::onResume: end");
    }

    @SuppressWarnings("SameParameterValue")
    private void logTheNumber(int i) {
        Log.i(TAG, "logTheNumber: i should be 1, but " + i);
    }
}
