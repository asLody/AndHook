package apk.andhook.ui;

import android.app.Application;

public final class MainApplication extends Application {

	@Override
	public void onCreate() {
		android.util.Log.i(MainApplication.class.toString(), "onCreate");

		apk.andhook.test.AndTest.RunTest(this, this.getContentResolver());
	}
}
