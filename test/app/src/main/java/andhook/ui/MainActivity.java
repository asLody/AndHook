package andhook.ui;

import andhook.test.AndTest;
import andhook.test.Constructor;
import andhook.test.Xposed;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

import andhook.ui.R;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("AndHook_" + this.getClass().getName(), "MainActivity::onCreate start");
        super.onCreate(savedInstanceState);
        Log.i("AndHook_" + this.getClass().getName(), "MainActivity::onCreate super start");
        setContentView(R.layout.activity_main);

        android.widget.Button hookbutton = (android.widget.Button) findViewById(R.id.hookbutton);
        hookbutton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                // in case of GC, JIT-Compile
                android.util.Log.i("AndHook_" + MainActivity.class.getName(), "begin...");
                for (int i = 0; i < 3; ++i) {
                    andhook.test.Static.a1(i + "");
                    Secure.getString(MainActivity.this.getContentResolver(),
                            Secure.ANDROID_ID);
                    new Constructor();
                    if (Xposed.print("test", i) != i) {
                        Log.w(AndTest.LOG_TAG,
                                "********print returns wrong value unexpectedly");
                    }
                }
                android.util.Log.i("AndHook_" + MainActivity.class.getName(), "end.");
            }
        });
    }
}
