package apk.andhook;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Pair;

public class DalvikHook {
	private static Map<Pair<String, String>, Integer> sBackups = new ConcurrentHashMap<>();

	private static native void dvmHookNativeNoBackup(Method origin,
			Method replace);

	private static native int dvmHookNative(Method origin, Method replace);

	// for backup methods
	private static native void dvmInvokeVoidMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native boolean dvmInvokeBooleanMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native byte dvmInvokeByteMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native short dvmInvokeShortMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native char dvmInvokeCharMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native int dvmInvokeIntMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native long dvmInvokeLongMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native float dvmInvokeFloatMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native double dvmInvokeDoubleMethod(int backUpSlot,
			Object receiver, Object... params);

	private static native Object dvmInvokeObjectMethod(int backUpSlot,
			Object receiver, Object... params);

	public static void hookNoBackup(final Method origin, final Method replace) {
		dvmHookNativeNoBackup(origin, replace);
	}

	public static void hook(Method origin, Method replace) {
		final int backUpSlot = dvmHookNative(origin, replace);
		// @TODO Overload method is not supported
		sBackups.put(
				Pair.create(replace.getDeclaringClass().getName(),
						replace.getName()), backUpSlot);
	}

	private static int getBackupMethodSlot() {
		final StackTraceElement currentStack = Thread.currentThread()
				.getStackTrace()[5];
		final int backupSlot = sBackups.get(Pair.create(
				currentStack.getClassName(), currentStack.getMethodName()));
		return backupSlot;
	}

	public static void callVoidOrigin(Object receiver, Object... params) {
		dvmInvokeVoidMethod(getBackupMethodSlot(), receiver, params);
	}

	public static boolean callBooleanOrigin(Object receiver, Object... params) {
		return dvmInvokeBooleanMethod(getBackupMethodSlot(), receiver, params);
	}

	public static byte callByteOrigin(Object receiver, Object... params) {
		return dvmInvokeByteMethod(getBackupMethodSlot(), receiver, params);
	}

	public static short callShortOrigin(Object receiver, Object... params) {
		return dvmInvokeShortMethod(getBackupMethodSlot(), receiver, params);
	}

	public static char callCharOrigin(Object receiver, Object... params) {
		return dvmInvokeCharMethod(getBackupMethodSlot(), receiver, params);
	}

	public static int callIntOrigin(Object receiver, Object... params) {
		return dvmInvokeIntMethod(getBackupMethodSlot(), receiver, params);
	}

	public static long callLongOrigin(Object receiver, Object... params) {
		return dvmInvokeLongMethod(getBackupMethodSlot(), receiver, params);
	}

	public static float callFloatOrigin(Object receiver, Object... params) {
		return dvmInvokeFloatMethod(getBackupMethodSlot(), receiver, params);
	}

	public static double callDoubleOrigin(Object receiver, Object... params) {
		return dvmInvokeDoubleMethod(getBackupMethodSlot(), receiver, params);
	}

	public static Object callObjectOrigin(Object receiver, Object... params) {
		return dvmInvokeObjectMethod(getBackupMethodSlot(), receiver, params);
	}
}
