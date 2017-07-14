package apk.andhook;

import java.lang.reflect.Method;

public final class ArtHook {
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

	public static native Object invokeMethod(final int slot,
			final Object receiver, final Object... params);
}