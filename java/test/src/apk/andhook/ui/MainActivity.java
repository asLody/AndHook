package apk.andhook.ui;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;
import apk.andhook.ui.R;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		android.widget.Button hookbutton = (android.widget.Button) findViewById(R.id.hookbutton);
		hookbutton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				android.util.Log.d(MainActivity.class.toString(), "begin...");
				for (int i = 0; i < 3; ++i) {
					apk.andhook.test.AndTest.a1(i + "");
					Secure.getString(MainActivity.this.getContentResolver(),
							Secure.ANDROID_ID);
				}
				android.util.Log.d(MainActivity.class.toString(), "end.");
			}
		});
		apk.andhook.test.AndTest.RunTest(this, this.getContentResolver());
	}
}
