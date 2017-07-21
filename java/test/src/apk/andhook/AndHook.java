package apk.andhook;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Pair;

/**
 * @author rrrfff
 * @version 2.0.0
 *
 */
public class AndHook {
	static {
		System.loadLibrary("andhook");
	}

	public static final class HookHelper {
		private static final ConcurrentHashMap<Pair<String, String>, Integer> sBackups = new ConcurrentHashMap<>();

		public static void hook(final Method origin, final Method replace) {
			final Pair<String, String> origin_key = Pair.create(origin
					.getDeclaringClass().getName(), origin.getName());
			final Pair<String, String> target_key = Pair.create(replace
					.getDeclaringClass().getName(), replace.getName());
			final Integer slot = AndHook.hook(origin, replace);
			sBackups.put(origin_key, slot);
			sBackups.put(target_key, slot);
		}

		private static int getBackupSlot() {
			final StackTraceElement currentStack = Thread.currentThread()
					.getStackTrace()[4];
			return sBackups.get(Pair.create(currentStack.getClassName(),
					currentStack.getMethodName()));
		}

		public static void callVoidOrigin(final Object receiver,
				final Object... params) {
			AndHook.invokeVoidMethod(getBackupSlot(), receiver, params);
		}

		public static void callStaticVoidOrigin(final Class<?> clazz,
				final Object... params) {
			AndHook.invokeVoidMethod(getBackupSlot(), clazz, params);
		}

		public static boolean callBooleanOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeBooleanMethod(getBackupSlot(), receiver,
					params);
		}

		public static boolean callStaticBooleanOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeBooleanMethod(getBackupSlot(), clazz, params);
		}

		public static byte callByteOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeByteMethod(getBackupSlot(), receiver, params);
		}

		public static byte callStaticByteOrigin(Class<?> clazz,
				final Object... params) {
			return AndHook.invokeByteMethod(getBackupSlot(), clazz, params);
		}

		public static short callShortOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeShortMethod(getBackupSlot(), receiver, params);
		}

		public static short callStaticShortOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeShortMethod(getBackupSlot(), clazz, params);
		}

		public static char callCharOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeCharMethod(getBackupSlot(), receiver, params);
		}

		public static char callStaticCharOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeCharMethod(getBackupSlot(), clazz, params);
		}

		public static int callIntOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeIntMethod(getBackupSlot(), receiver, params);
		}

		public static int callStaticIntOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeIntMethod(getBackupSlot(), clazz, params);
		}

		public static long callLongOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeLongMethod(getBackupSlot(), receiver, params);
		}

		public static long callStaticLongOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeLongMethod(getBackupSlot(), clazz, params);
		}

		public static float callFloatOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeFloatMethod(getBackupSlot(), receiver, params);
		}

		public static float callStaticFloatOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeFloatMethod(getBackupSlot(), clazz, params);
		}

		public static double callDoubleOrigin(final Object receiver,
				final Object... params) {
			return AndHook
					.invokeDoubleMethod(getBackupSlot(), receiver, params);
		}

		public static double callStaticDoubleOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeDoubleMethod(getBackupSlot(), clazz, params);
		}

		public static Object callObjectOrigin(final Object receiver,
				final Object... params) {
			return AndHook
					.invokeObjectMethod(getBackupSlot(), receiver, params);
		}

		public static Object callStaticObjectOrigin(final Class<?> clazz,
				final Object... params) {
			return AndHook.invokeObjectMethod(getBackupSlot(), clazz, params);
		}

		public static Method findMethod(final Class<?> cls, final String name,
				final Class<?>... parameterTypes) {
			try {
				return cls.getDeclaredMethod(name, parameterTypes);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static native void replaceMethod(final Method origin,
			final Method replace);

	public static native int hook(final Method origin, final Method replace);

	public static native void hookNoBackup(final Method origin,
			final Method replace);

	public static native void ensureClassInitialized(final Class<?> origin);

	public static void invokeVoidMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.invokeVoidMethod(slot, receiver, params);
		} else {
			ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static void invokeStaticVoidMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			DalvikHook.invokeVoidMethod(slot, clazz, params);
		} else {
			ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static boolean invokeBooleanMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeBooleanMethod(slot, receiver, params);
		} else {
			return (boolean) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static boolean invokeStaticBooleanMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeBooleanMethod(slot, clazz, params);
		} else {
			return (boolean) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static byte invokeByteMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeByteMethod(slot, receiver, params);
		} else {
			return (byte) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static byte invokeStaticByteMethod(final int slot, Class<?> clazz,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeByteMethod(slot, clazz, params);
		} else {
			return (byte) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static short invokeShortMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeShortMethod(slot, receiver, params);
		} else {
			return (short) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static short invokeStaticShortMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeShortMethod(slot, clazz, params);
		} else {
			return (short) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static char invokeCharMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeCharMethod(slot, receiver, params);
		} else {
			return (char) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static char invokeStaticCharMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeCharMethod(slot, clazz, params);
		} else {
			return (char) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static int invokeIntMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeIntMethod(slot, receiver, params);
		} else {
			return (int) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static int invokeStaticIntMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeIntMethod(slot, clazz, params);
		} else {
			return (int) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static long invokeLongMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeLongMethod(slot, receiver, params);
		} else {
			return (long) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static long invokeStaticLongMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeLongMethod(slot, clazz, params);
		} else {
			return (long) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static float invokeFloatMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeFloatMethod(slot, receiver, params);
		} else {
			return (float) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static float invokeStaticFloatMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeFloatMethod(slot, clazz, params);
		} else {
			return (float) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static double invokeDoubleMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeDoubleMethod(slot, receiver, params);
		} else {
			return (double) ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static double invokeStaticDoubleMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeDoubleMethod(slot, clazz, params);
		} else {
			return (double) ArtHook.invokeMethod(slot, null, params);
		}
	}

	public static Object invokeObjectMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeObjectMethod(slot, receiver, params);
		} else {
			return ArtHook.invokeMethod(slot, receiver, params);
		}
	}

	public static Object invokeStaticObjectMethod(final int slot,
			final Class<?> clazz, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 19) {
			return DalvikHook.invokeObjectMethod(slot, clazz, params);
		} else {
			return ArtHook.invokeMethod(slot, null, params);
		}
	}

	protected static final class Native {
		private static native void a();

		private static native void b();
	}

	private static final class ArtHook {
		public static native Object invokeMethod(final int slot,
				final Object receiver, final Object... params);
	}

	private static final class DalvikHook {
		public static native void invokeVoidMethod(final int slot,
				final Object receiver, final Object... params);

		public static native boolean invokeBooleanMethod(final int slot,
				final Object receiver, final Object... params);

		public static native byte invokeByteMethod(final int slot,
				final Object receiver, final Object... params);

		public static native short invokeShortMethod(final int slot,
				final Object receiver, final Object... params);

		public static native char invokeCharMethod(final int slot,
				final Object receiver, final Object... params);

		public static native int invokeIntMethod(final int slot,
				final Object receiver, final Object... params);

		public static native long invokeLongMethod(final int slot,
				final Object receiver, final Object... params);

		public static native float invokeFloatMethod(final int slot,
				final Object receiver, final Object... params);

		public static native double invokeDoubleMethod(final int slot,
				final Object receiver, final Object... params);

		public static native Object invokeObjectMethod(final int slot,
				final Object receiver, final Object... params);
	}
}
