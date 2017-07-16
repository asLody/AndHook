package apk.andhook.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import apk.andhook.ui.R;
import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

	@SuppressLint("SdCardPath")
	public void testClassLoader() {
		final DexClassLoader dex1 = new DexClassLoader("/tmp/AndHook.dex",
				"/data/data/apk.dexloader/cache",
				"/data/data/apk.dexloader/lib", this.getClassLoader());
		final DexClassLoader dexClassLoader = new DexClassLoader(
				"/tmp/VirtualPhone.apk", "/data/data/apk.dexloader/cache",
				"/data/data/apk.dexloader/lib", dex1);
		try {
			final Class<?> preCls = Class.forName("io.virtualhook.PreHook",
					true, dexClassLoader);
			preCls.getDeclaredMethod("Init", ClassLoader.class,
					android.content.Context.class, String.class);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/*
		Class<?> c = java.lang.reflect.Method.class;
        try {
            while (c != Object.class) {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    f.setAccessible(true);
                    android.util.Log.d("java.lang.Class", f.getName() + "," + f.getModifiers() + ","+f.toGenericString());
                }
                c = c.getSuperclass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		*/
		apk.andhook.test.AndTest.RunTest(this, this.getContentResolver());
	}
}
