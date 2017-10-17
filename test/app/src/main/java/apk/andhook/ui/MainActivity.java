package apk.andhook.ui;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import apk.andhook.ui.R;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(this.getClass().toString(), "MainActivity::onCreate start");
		super.onCreate(savedInstanceState);
		Log.i(this.getClass().toString(), "MainActivity::onCreate super start");
		setContentView(R.layout.activity_main);

		android.widget.Button hookbutton = (android.widget.Button) findViewById(R.id.hookbutton);
		hookbutton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				android.util.Log.i(MainActivity.class.toString(), "begin...");
				for (int i = 0; i < 3; ++i) {
					apk.andhook.test.AndTest.a1(i + "");
					Secure.getString(MainActivity.this.getContentResolver(),
							Secure.ANDROID_ID);
				}
				android.util.Log.i(MainActivity.class.toString(), "end.");
			}
		});
	}
}
