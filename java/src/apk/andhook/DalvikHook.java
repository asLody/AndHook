package apk.andhook;

import java.lang.reflect.Method;

public final class DalvikHook {
	public static final class Native {
		private static native void a();

		private static native void b();
	}

	public static native void replaceMethod(final Method origin,
			final Method replace);

	public static native int hookNative(final Method origin,
			final Method replace);

	public static native void hookNativeNoBackup(final Method origin,
			final Method replace);

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