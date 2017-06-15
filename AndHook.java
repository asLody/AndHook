package apk.andhook;

import java.lang.reflect.Method;

public class AndHook {
	public static void hookNoBackup(final Method origin, final Method replace) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.hookNoBackup(origin, replace);
		} else {
			ArtHook.hookNoBackup(origin, replace);
		}
	}

	public static void hook(Method origin, Method replace) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.hook(origin, replace);
		} else {
			ArtHook.hook(origin, replace);
		}
	}

	public static void callVoidOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.callVoidOrigin(receiver, params);
		} else {
			ArtHook.callOrigin(receiver, params);
		}
	}

	public static void callStaticVoidOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.callVoidOrigin(clazz, params);
		} else {
			ArtHook.callOrigin(null, params);
		}
	}

	public static boolean callBooleanOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callBooleanOrigin(receiver, params);
		} else {
			return (boolean) ArtHook.callOrigin(receiver, params);
		}
	}

	public static boolean callStaticBooleanOrigin(Class<?> clazz,
			Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callBooleanOrigin(clazz, params);
		} else {
			return (boolean) ArtHook.callOrigin(null, params);
		}
	}

	public static byte callByteOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callByteOrigin(receiver, params);
		} else {
			return (byte) ArtHook.callOrigin(receiver, params);
		}
	}

	public static byte callStaticByteOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callByteOrigin(clazz, params);
		} else {
			return (byte) ArtHook.callOrigin(null, params);
		}
	}

	public static short callShortOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callShortOrigin(receiver, params);
		} else {
			return (short) ArtHook.callOrigin(receiver, params);
		}
	}

	public static short callStaticShortOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callShortOrigin(clazz, params);
		} else {
			return (short) ArtHook.callOrigin(null, params);
		}
	}

	public static char callCharOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callCharOrigin(receiver, params);
		} else {
			return (char) ArtHook.callOrigin(receiver, params);
		}
	}

	public static char callStaticCharOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callCharOrigin(clazz, params);
		} else {
			return (char) ArtHook.callOrigin(null, params);
		}
	}

	public static int callIntOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callIntOrigin(receiver, params);
		} else {
			return (int) ArtHook.callOrigin(receiver, params);
		}
	}

	public static int callStaticIntOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callIntOrigin(clazz, params);
		} else {
			return (int) ArtHook.callOrigin(null, params);
		}
	}

	public static long callLongOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callLongOrigin(receiver, params);
		} else {
			return (long) ArtHook.callOrigin(receiver, params);
		}
	}

	public static long callStaticLongOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callLongOrigin(clazz, params);
		} else {
			return (long) ArtHook.callOrigin(null, params);
		}
	}

	public static float callFloatOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callFloatOrigin(receiver, params);
		} else {
			return (float) ArtHook.callOrigin(receiver, params);
		}
	}

	public static float callStaticFloatOrigin(Class<?> clazz, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callFloatOrigin(clazz, params);
		} else {
			return (float) ArtHook.callOrigin(null, params);
		}
	}

	public static double callDoubleOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callDoubleOrigin(receiver, params);
		} else {
			return (double) ArtHook.callOrigin(receiver, params);
		}
	}

	public static double callStaticDoubleOrigin(Class<?> clazz,
			Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callDoubleOrigin(clazz, params);
		} else {
			return (double) ArtHook.callOrigin(null, params);
		}
	}

	public static Object callObjectOrigin(Object receiver, Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callObjectOrigin(receiver, params);
		} else {
			return ArtHook.callOrigin(receiver, params);
		}
	}

	public static Object callStaticObjectOrigin(Class<?> clazz,
			Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.callObjectOrigin(clazz, params);
		} else {
			return ArtHook.callOrigin(null, params);
		}
	}
}
