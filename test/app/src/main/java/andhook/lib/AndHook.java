package andhook.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

/**
 * @author rrrfff
 * @version 2.3.0
 */
@SuppressWarnings({ "unused", "WeakerAccess", "JniMissingFunction" })
public class AndHook {
	private final static String LOG_TAG = "AndHook";

	static {
		System.loadLibrary("AndHook");
	}

	public static final class HookHelper {
		private static final ConcurrentHashMap<Pair<String, String>, Integer> sBackups = new ConcurrentHashMap<>();
		private static Method getSignature = null;
		static {
			try {
				getSignature = Constructor.class.getDeclaredMethod(
						"getSignature", (Class<?>[]) null);
				getSignature.setAccessible(true);
			} catch (NoSuchMethodException e) {
				Log.e(AndHook.LOG_TAG, e.getMessage(), e);
			}
		}

		public static void hook(final Method origin, final Method replace) {
			final int slot = AndHook.hook(origin, replace);
			if (slot >= 0) {
				saveBackupSlot(slot, origin.getDeclaringClass(),
						origin.getName(), replace);
			}
		}

		public static void hook(final Class<?> clazz, final String name,
				final String signature, final Method replace) {
			final int slot = AndHook.hook(clazz, name, signature, replace);
			if (slot >= 0) {
				saveBackupSlot(slot, clazz, name, replace);
			}
		}

		private static void saveBackupSlot(final Integer slot,
				final Class<?> clazz, final String name, final Method replace) {
			// a simple workaround for overloaded methods
			final String identifier = replace.getParameterTypes().length + "";
			// ugly solution
			final Pair<String, String> origin_key = Pair.create(
					clazz.getName(), name + identifier);
			final Pair<String, String> target_key = Pair.create(replace
					.getDeclaringClass().getName(), replace.getName()
					+ identifier);
			if (sBackups.containsKey(origin_key)
					|| sBackups.containsKey(target_key)) {
				Log.e(AndHook.LOG_TAG, "duplicate key error!");
			}
			sBackups.put(origin_key, slot);
			sBackups.put(target_key, slot);
			if (Build.VERSION.SDK_INT <= 20
					&& !origin_key.first.equals(target_key.first)
					&& !origin_key.second.equals(target_key.second)) {
				final Pair<String, String> special_key = Pair.create(
						target_key.first, origin_key.second);
				if (sBackups.containsKey(special_key)) {
					Log.e(AndHook.LOG_TAG, "duplicate key error!");
				}
				sBackups.put(special_key, slot);
			}
		}

		private static int getBackupSlot(final int identifier) {
			final StackTraceElement currentStack = Thread.currentThread()
					.getStackTrace()[4];
			return sBackups.get(Pair.create(currentStack.getClassName(),
					currentStack.getMethodName() + identifier));
		}

		public static void invokeVoidOrigin(final Object receiver,
				final Object... params) {
			AndHook.invokeVoidMethod(getBackupSlot(params.length), receiver,
					params);
		}

		public static boolean invokeBooleanOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeBooleanMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static byte invokeByteOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeByteMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static short invokeShortOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeShortMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static char invokeCharOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeCharMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static int invokeIntOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeIntMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static long invokeLongOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeLongMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static float invokeFloatOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeFloatMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static double invokeDoubleOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeDoubleMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static Object invokeObjectOrigin(final Object receiver,
				final Object... params) {
			return AndHook.invokeObjectMethod(getBackupSlot(params.length),
					receiver, params);
		}

		public static Method findMethod(final Class<?> clazz,
				final String name, final Class<?>... parameterTypes) {
			Method m = null;
			try {
				m = clazz.getDeclaredMethod(name, parameterTypes);
			} catch (final NoSuchMethodException e) {
			}
			if (m == null) {
				try {
					m = clazz.getMethod(name, parameterTypes);
				} catch (final NoSuchMethodException e) {
				}
			}
			if (m == null)
				Log.e(AndHook.LOG_TAG, "failed to find method " + name);
			return m;
		}

		@SuppressWarnings("ConstantConditions")
		private static String asConstructor(final Class<?> clazz,
				final String method, final Class<?>[] parameterTypes) {
			final String clsname = clazz.getName();
			if (!method.equals("<init>") && !clsname.endsWith("." + method)
					&& !clsname.endsWith("$" + method))
				return null;

			try {
				return (String) getSignature.invoke(clazz
						.getConstructor(parameterTypes));
			} catch (final Exception e) {
				android.util.Log.e(AndHook.class.toString(),
						"failed to get signature of constructor!", e);
			}
			return null;
		}

		@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
		public @interface Hook {
			String value() default ""; // class name

			Class<?> clazz() default Hook.class; // class

			String name() default ""; // method name
		}

		public static void applyHooks(final Class<?> holdClass,
				final ClassLoader loader) {
			AndHook.ensureClassInitialized(holdClass);
			AndHook.suspendAll();
			for (final Method hookMethod : holdClass.getDeclaredMethods()) {
				final Hook hook = hookMethod.getAnnotation(Hook.class);
				if (hook != null) {
					String name = hook.name();
					if (name.isEmpty())
						name = hookMethod.getName();

					Class<?> clazz = hook.clazz();
					Method origin;
					try {
						if (clazz == Hook.class)
							clazz = loader.loadClass(hook.value());
						final Class<?>[] parameterTypes = hookMethod
								.getParameterTypes();
						final String sig = asConstructor(clazz, name,
								parameterTypes);
						if (sig != null) {
							// AndHook.ensureClassInitialized(hook.clazz());
							hook(clazz, "<init>", sig, hookMethod);
						} else {
							origin = findMethod(clazz, name, parameterTypes);
							if (origin != null) {
								AndHook.ensureClassInitialized(hook.clazz());
								hook(origin, hookMethod);
							}
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
			AndHook.resumeAll();
		}
	}

	@Deprecated
	public static native void replaceMethod(final Method origin,
			final Method replace);

	public static native int hook(final Method origin, final Method replace);

	public static native void hookNoBackup(final Method origin,
			final Method replace);

	public static native int hook(final Class<?> clazz, final String name,
			final String signature, final Method replace);

	public static native void hookNoBackup(final Class<?> clazz,
			final String name, final String signature, final Method replace);

	public static native boolean suspendAll();

	public static native void resumeAll();

	public static native void ensureClassInitialized(final Class<?> origin);

	public static native void enableFastDexLoad(final boolean enable);

	public static native void deoptimizeMethod(final Method target);

	public static native void dumpClassMethods(final Class<?> clazz,
			final String clsname);

	public static void dumpClassMethods(final Class<?> clazz) {
		dumpClassMethods(clazz, null);
	}

	public static void dumpClassMethods(final String clsname) {
		dumpClassMethods(null, clsname);
	}

	public static void invokeVoidMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			DalvikHook.invokeVoidMethod(slot, receiver, params);
		} else {
			invokeMethod(slot, receiver, params);
		}
	}

	public static boolean invokeBooleanMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeBooleanMethod(slot, receiver, params);
		} else {
			return (boolean) invokeMethod(slot, receiver, params);
		}
	}

	public static byte invokeByteMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeByteMethod(slot, receiver, params);
		} else {
			return (byte) invokeMethod(slot, receiver, params);
		}
	}

	public static short invokeShortMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeShortMethod(slot, receiver, params);
		} else {
			return (short) invokeMethod(slot, receiver, params);
		}
	}

	public static char invokeCharMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeCharMethod(slot, receiver, params);
		} else {
			return (char) invokeMethod(slot, receiver, params);
		}
	}

	public static int invokeIntMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeIntMethod(slot, receiver, params);
		} else {
			return (int) invokeMethod(slot, receiver, params);
		}
	}

	public static long invokeLongMethod(final int slot, final Object receiver,
			final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeLongMethod(slot, receiver, params);
		} else {
			return (long) invokeMethod(slot, receiver, params);
		}
	}

	public static float invokeFloatMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeFloatMethod(slot, receiver, params);
		} else {
			return (float) invokeMethod(slot, receiver, params);
		}
	}

	public static double invokeDoubleMethod(final int slot,
			final Object receiver, final Object... params) {
		if (android.os.Build.VERSION.SDK_INT <= 20) {
			return DalvikHook.invokeDoubleMethod(slot, receiver, params);
		} else {
			return (double) invokeMethod(slot, receiver, params);
		}
	}

	public static Object invokeObjectMethod(final int slot,
			final Object receiver, final Object... params) {
		return invokeMethod(slot, receiver, params);
	}

	private static native Object invokeMethod(final int slot,
			final Object receiver, final Object... params);

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

		public static Object invokeObjectMethod(final int slot,
				final Object receiver, final Object... params) {
			return AndHook.invokeMethod(slot, receiver, params);
		}
	}
}
