package apk.andhook.ui;

import android.app.Application;
import android.util.Log;

public final class MainApplication extends Application {
	@Override
	public void onCreate() {
		Log.i(MainApplication.class.toString(), "onCreate");
		apk.andhook.test.AndTest.RunTest(this, this.getContentResolver());
	}
}
