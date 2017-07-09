package apk.andhook;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auother weishu
 * @date 17/3/6
 * @update 17/6/10
 */
public class ArtHook {
	private static final String TAG = "ArtHook";
	private static final Class<?> sAbstractMethod = Method.class
			.getSuperclass();
	private static final Map<Pair<String, String>, Method> sBackups = new ConcurrentHashMap<>();

	public static void hookNoBackup(final Method origin, final Method replace) {
		// replace method
		Unsafe.alignedCopy(MethodInspect.getMethodAddress(origin),
				MethodInspect.getMethodAddress(replace),
				MethodInspect.getArtMethodSize(),
				Modifier.isStatic(origin.getModifiers()));
	}

	public static void hook(final Method origin, final Method replace) {
		// 1. backup
		final Method backUp = backUp(origin, replace);
		// @TODO Overload method is not supported
		if (Build.VERSION.SDK_INT == 21 && Modifier.isStatic(origin.getModifiers())) {
			sBackups.put(
					Pair.create(origin.getDeclaringClass().getName(),
							origin.getName()), backUp);
		} else {
			sBackups.put(
					Pair.create(replace.getDeclaringClass().getName(),
							replace.getName()), backUp);
		}

		// Log.d("ART", replace.getDeclaringClass().getName() + ":" +
		// replace.getName());

		// 2. replace method
		hookNoBackup(origin, replace);
	}

	public static Object callOrigin(final Object receiver,
			final Object... params) {
		final StackTraceElement currentStack = Thread.currentThread()
				.getStackTrace()[4];
		// Log.d("ART", currentStack.getClassName() + ":" +
		// currentStack.getMethodName());
		final Method method = sBackups.get(Pair.create(
				currentStack.getClassName(), currentStack.getMethodName()));

		try {
			return method.invoke(receiver, params);
		} catch (Throwable e) {
			throw new RuntimeException("invoke origin method error", e);
		}
	}

	private static Method backUp(final Method origin, final Method replace) {
		try {
			if (Build.VERSION.SDK_INT < 23) {
				final Class<?> classArtMethod = Class
						.forName("java.lang.reflect.ArtMethod");
				final Constructor<?> constructorOfArtMethod = classArtMethod
						.getDeclaredConstructor();
				constructorOfArtMethod.setAccessible(true);
				final Constructor<Method> constructorOfmethod = Method.class
						.getDeclaredConstructor(classArtMethod);

				// new Method(ArtMethod artMethod)
				final Object newArtMethod = constructorOfArtMethod
						.newInstance();
				final Method newMethod = constructorOfmethod
						.newInstance(newArtMethod);
				newMethod.setAccessible(true);

				Unsafe.alignedCopy(MethodInspect.getMethodAddress(newMethod),
						MethodInspect.getMethodAddress(origin),
						MethodInspect.getArtMethodSize(), false);

				final Field accessFlagsField = classArtMethod
						.getDeclaredField("accessFlags");
				accessFlagsField.setAccessible(true);
				accessFlagsField.set(newArtMethod,
						(int) accessFlagsField.get(newArtMethod)
								& (~Modifier.PUBLIC) | Modifier.PRIVATE);
				return newMethod;
			} else {
				// AbstractMethod
				final Field accessFlagsField = sAbstractMethod
						.getDeclaredField("accessFlags");
				accessFlagsField.setAccessible(true);

				final Field artMethodField = sAbstractMethod
						.getDeclaredField("artMethod");
				artMethodField.setAccessible(true);

				// make the construct accessible, we can not just use
				// `setAccessible`
				final Constructor<Method> methodConstructor = Method.class
						.getDeclaredConstructor();
				final Field override = AccessibleObject.class
						.getDeclaredField(Build.VERSION.SDK_INT == 23/*
																	 * Build.
																	 * VERSION_CODES
																	 * .M
																	 */? "flag"
								: "override");
				override.setAccessible(true);
				override.set(methodConstructor, true);

				// clone the origin method
				final Method newMethod = methodConstructor.newInstance();
				newMethod.setAccessible(true);
				for (Field field : sAbstractMethod.getDeclaredFields()) {
					field.setAccessible(true);
					field.set(newMethod, field.get(origin));
				}

				// allocate new artMethod struct, we can not use memory managed
				// by JVM
				final int artMethodSize = (int) MethodInspect
						.getArtMethodSize();
				final ByteBuffer artMethod = ByteBuffer
						.allocateDirect(artMethodSize);
				Long artMethodAddress;
				int ACC_FLAG_OFFSET;
				if (Build.VERSION.SDK_INT < 24) {
					// Below Android N, the jdk implementation is not openjdk
					artMethodAddress = (Long) Reflection.get(Buffer.class,
							null, "effectiveDirectAddress", artMethod);
					// http://androidxref.com/6.0.0_r1/xref/art/runtime/art_method.h
					// GCRoot * 3, sizeof(GCRoot) =
					// sizeof(mirror::CompressedReference) =
					// sizeof(mirror::ObjectReference) = sizeof(uint32_t) = 4
					ACC_FLAG_OFFSET = 12;
				} else {
					artMethodAddress = (Long) Reflection.call(
							artMethod.getClass(), null, "address", artMethod,
							null, null);
					// http://androidxref.com/7.0.0_r1/xref/art/runtime/art_method.h
					// sizeof(GCRoot) = 4
					ACC_FLAG_OFFSET = 4;
				}
				Unsafe.alignedCopy(artMethodAddress,
						MethodInspect.getMethodAddress(origin), artMethodSize,
						false);

				final byte[] newMethodBytes = new byte[artMethodSize];
				artMethod.get(newMethodBytes);
				Log.d(TAG, "new: " + Arrays.toString(newMethodBytes));
				Log.i(TAG,
						"origin:"
								+ Arrays.toString(MethodInspect
										.getMethodBytes(origin)));

				// replace the artMethod of our new method
				artMethodField.set(newMethod, artMethodAddress);

				// modify the access flag of the new method
				final Integer accessFlags = (Integer) accessFlagsField
						.get(origin);
				Log.d(TAG, "Acc:" + accessFlags);
				final int privateAccFlag = accessFlags & ~Modifier.PUBLIC
						| Modifier.PRIVATE;
				accessFlagsField.set(newMethod, privateAccFlag);

				// 1. try big endian
				artMethod.order(ByteOrder.BIG_ENDIAN);
				int nativeAccFlags = artMethod.getInt(ACC_FLAG_OFFSET);
				Log.d(TAG, "bitendian:" + nativeAccFlags);
				if (nativeAccFlags == accessFlags) {
					// hit!
					artMethod.putInt(ACC_FLAG_OFFSET, privateAccFlag);
				} else {
					// 2. try little endian
					artMethod.order(ByteOrder.LITTLE_ENDIAN);
					nativeAccFlags = artMethod.getInt(ACC_FLAG_OFFSET);
					Log.d(TAG, "littleendian:" + nativeAccFlags);
					if (nativeAccFlags == accessFlags) {
						artMethod.putInt(ACC_FLAG_OFFSET, privateAccFlag);
					} else {
						// the offset is error!
						throw new RuntimeException(
								"native set access flags error!");
					}
				}

				return newMethod;
			}
		} catch (Throwable e) {
			throw new RuntimeException("can not backup method", e);
		}
	}

	private static class Reflection {
		public static Object call(Class<?> clazz, final String className,
				final String methodName, final Object receiver,
				final Class<?>[] types, final Object[] params)
				throws RuntimeException {
			try {
				if (clazz == null)
					clazz = Class.forName(className);
				final Method method = clazz
						.getDeclaredMethod(methodName, types);
				method.setAccessible(true);
				return method.invoke(receiver, params);
			} catch (Throwable throwable) {
				throw new RuntimeException("reflect call error", throwable);
			}
		}

		public static Object get(Class<?> clazz, final String className,
				final String fieldName, final Object receiver) {
			try {
				if (clazz == null)
					clazz = Class.forName(className);
				final Field field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(receiver);
			} catch (Throwable e) {
				throw new RuntimeException("reflect get error", e);
			}
		}
	}

	public static class MethodInspect {
		private static long sMethodSize = 0;

		public static void a() {
		}

		public static void b() {
		}

		public static long getMethodAddress(final Method method) {
			final Object mirrorMethod = Reflection.get(sAbstractMethod, null,
					"artMethod", method);
			if (mirrorMethod.getClass().equals(Long.class)) {
				return (long) mirrorMethod;
			}
			return Unsafe.getObjectAddress(mirrorMethod);
		}

		public static long getArtMethodSize() {
			if (sMethodSize <= 0) {
				try {
					final Method f1 = MethodInspect.class
							.getDeclaredMethod("a");
					final Method f2 = MethodInspect.class
							.getDeclaredMethod("b");
					sMethodSize = getMethodAddress(f2) - getMethodAddress(f1);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return sMethodSize;
		}

		public static byte[] getMethodBytes(final Method method) {
			if (method == null) {
				return null;
			}
			final byte[] bytes = new byte[(int) getArtMethodSize()];
			final long baseAddr = getMethodAddress(method);
			libcore.io.Memory.peekByteArray(baseAddr, bytes, 0, bytes.length);
			return bytes;
		}
	}

	private static class Unsafe {
		private static sun.misc.Unsafe unsafe = null;

		private static void init() {
			try {
				unsafe = (sun.misc.Unsafe) Reflection.get(
						sun.misc.Unsafe.class, null, "THE_ONE", null);
			} catch (RuntimeException e) {
				throw new RuntimeException(
						"failed to get instance of sun.misc.Unsafe", e);
			}
		}

		public static long getObjectAddress(final Object o) {
			if (unsafe == null)
				init();
			final Object[] objects = { o };
			return unsafe.getInt(objects,
					unsafe.arrayBaseOffset(Object[].class));
		}

		public static void alignedCopy(long dst, long src, long length,
				boolean shouldFix) {
			// java.nio.ByteOrder.nativeOrder()
			int dex_method_index_ = 0;
			if (shouldFix && Build.VERSION.SDK_INT == 21) {
				dex_method_index_ = libcore.io.Memory.peekInt(dst + 16 * 4,
						false);
			}
			// String sdst = "", ssrc = "";
			for (length /= 4; length > 0; --length, dst += 4, src += 4) {
				// sdst += libcore.io.Memory.peekInt(dst, false) + ", ";
				// ssrc += libcore.io.Memory.peekInt(src, false) + ", ";
				libcore.io.Memory.pokeInt(dst,
						libcore.io.Memory.peekInt(src, false), false);
			}
			if (dex_method_index_ != 0) {
				libcore.io.Memory.pokeInt(dst - 16, dex_method_index_, false);
			}
			// Log.d("dst", sdst);
			// Log.d("src", ssrc);
		}
	}
}