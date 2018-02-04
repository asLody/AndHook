package andhook.ui;

import android.app.Application;
import android.util.Log;

public final class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(MainApplication.class.toString(), "onCreate");
        andhook.test.AndTest.RunTest(this, this.getContentResolver());
    }
}
