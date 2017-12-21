package andhook.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

/**
 * @author rrrfff
 * @version 3.0.1
 */
@SuppressWarnings({"unused", "WeakerAccess", "JniMissingFunction"})
public final class AndHook {
    private final static String LOG_TAG = "AndHook";

    static {
        try {
            System.loadLibrary("AndHook");
        } catch (final UnsatisfiedLinkError e0) {
            try {
                // compatible with libhoudini
                System.loadLibrary("AndHookCompat");
            } catch (final UnsatisfiedLinkError e1) {
                // still failed, YunOS?
                throw new UnsatisfiedLinkError("incompatible platform, "
                        + e0.getMessage());
            }
        }
    }

    public static void ensureNativeLibraryLoaded() {
        final AndHook dummy = new AndHook();
    }

    public static native int backup(final Member origin);

    public static native int backup(final Class<?> clazz, final String name,
                                    final String signature);

    public static native boolean hook(final Member origin,
                                      final Object extra, int shared_backup);

    public static native boolean hook(final Class<?> clazz, final String name,
                                      final String signature, final Object extra, int shared_backup);

    public static int hook(final Member origin, final Object extra) {
        int slot = backup(origin);
        if (slot != -1) {
            if (!hook(origin, extra, slot))
                slot = -1;
        }
        return slot;
    }

    public static int hook(final Class<?> clazz, final String name,
                           final String signature, final Object extra) {
        int slot = backup(clazz, name, signature);
        if (slot != -1) {
            if (!hook(clazz, name, signature, extra, slot))
                slot = -1;
        }
        return slot;
    }

    public static void hookNoBackup(final Member origin, final Object extra) {
        hook(origin, extra, -1);
    }

    public static void hookNoBackup(final Class<?> clazz, final String name,
                                    final String signature, final Object extra) {
        hook(clazz, name, signature, extra, -1);
    }


    public static native boolean restore(final int slot, final Member origin);

    public static native boolean suspendAll();

    public static native void resumeAll();

    public static native void ensureClassInitialized(final Class<?> origin);

    public static native void enableFastDexLoad(final boolean enable);

    public static native void optimizeMethod(final Member target);

    public static native void jitCompile(final Member target);

    public static native void deoptimizeMethod(final Member target);

    private static native void dumpClassMethods(final Class<?> clazz,
                                                final String clsname);

    public static void dumpClassMethods(final Class<?> clazz) {
        dumpClassMethods(clazz, null);
    }

    public static void dumpClassMethods(final String clsname) {
        dumpClassMethods(null, clsname);
    }

    private static native Object invoke(final int slot, final Object receiver,
                                        final Object... params);

    public static void invokeVoidMethod(final int slot, final Object receiver,
                                        final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            Dalvik.invokeVoidMethod(slot, receiver, params);
        } else {
            invoke(slot, receiver, params);
        }
    }

    public static boolean invokeBooleanMethod(final int slot,
                                              final Object receiver, final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeBooleanMethod(slot, receiver, params);
        } else {
            return (boolean) invoke(slot, receiver, params);
        }
    }

    public static byte invokeByteMethod(final int slot, final Object receiver,
                                        final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeByteMethod(slot, receiver, params);
        } else {
            return (byte) invoke(slot, receiver, params);
        }
    }

    public static short invokeShortMethod(final int slot,
                                          final Object receiver, final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeShortMethod(slot, receiver, params);
        } else {
            return (short) invoke(slot, receiver, params);
        }
    }

    public static char invokeCharMethod(final int slot, final Object receiver,
                                        final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeCharMethod(slot, receiver, params);
        } else {
            return (char) invoke(slot, receiver, params);
        }
    }

    public static int invokeIntMethod(final int slot, final Object receiver,
                                      final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeIntMethod(slot, receiver, params);
        } else {
            return (int) invoke(slot, receiver, params);
        }
    }

    public static long invokeLongMethod(final int slot, final Object receiver,
                                        final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeLongMethod(slot, receiver, params);
        } else {
            return (long) invoke(slot, receiver, params);
        }
    }

    public static float invokeFloatMethod(final int slot,
                                          final Object receiver, final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeFloatMethod(slot, receiver, params);
        } else {
            return (float) invoke(slot, receiver, params);
        }
    }

    public static double invokeDoubleMethod(final int slot,
                                            final Object receiver, final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return Dalvik.invokeDoubleMethod(slot, receiver, params);
        } else {
            return (double) invoke(slot, receiver, params);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeObjectMethod(final int slot,
                                           final Object receiver, final Object... params) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            return (T) Dalvik.invokeObjectMethod(slot, receiver, params);
        } else {
            return (T) invoke(slot, receiver, params);
        }
    }


    /**
     * Returns the result of dynamically invoking this method. Equivalent to
     * {@code receiver.methodName(arg1, arg2, ... , argN)}.
     * <p>
     * <p>If the method is static, the receiver argument is ignored (and may be null).
     * <p>
     * <p>If the invocation completes normally, the return value itself is
     * returned. If the method is declared to return a primitive type, the
     * return value is boxed. If the return type is void, null is returned.
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final int slot, final Object receiver,
                                     final Object... params) {
        return (T) invoke(slot, receiver, params);
    }

    public static final class HookHelper {
        public static final String constructorName = "<init>";
        private static final ConcurrentHashMap<Pair<String, String>, Integer> sBackups = new ConcurrentHashMap<>();
        private static Method getSignature = null;

        static {
            try {
                final Class<?> clazz = ClassLoader.getSystemClassLoader()
                        .loadClass("libcore.reflect.Types");
                getSignature = clazz.getDeclaredMethod("getSignature",
                        Class.class);
                getSignature.setAccessible(true);
            } catch (final Exception e) {
                Log.e(AndHook.LOG_TAG, e.getMessage(), e);
            }
        }

        @SuppressWarnings("ConstantConditions")
        public static String getSignature(final Class<?> retType,
                                          final Class<?>[] parameterTypes) {
            try {
                final StringBuilder result = new StringBuilder();

                result.append('(');
                for (final Class<?> parameterType : parameterTypes) {
                    result.append(getSignature.invoke(null, parameterType));
                }
                result.append(")");
                result.append(getSignature.invoke(null, retType));

                return result.toString();
            } catch (final Exception e) {
                Log.e(AndHook.LOG_TAG, "failed to get signature!", e);
            }
            return null;
        }

        private static void warnKnownIssue(final Class<?> clazz,
                                           final Method replace) {
            if (!Modifier.isStatic(replace.getModifiers())
                    || replace.getParameterTypes().length < 1
                    || replace.getParameterTypes()[0].isPrimitive()) {
                Log.e(AndHook.LOG_TAG,
                        "method "
                                + replace.getDeclaringClass().getName()
                                + "@"
                                + replace.getName()
                                + " must be static and its first argument must be Class<?> or Object!");
            }
        }

        public static void hook(final Member origin, final Method replace) {
            warnKnownIssue(origin.getDeclaringClass(), replace);
            final int slot = AndHook.backup(origin);
            if (slot != -1 && saveBackupSlot(slot, replace)) {
                AndHook.hook(origin, replace, slot);
            }
        }

        public static void hook(final Class<?> clazz, final String name,
                                final String signature, final Method replace) {
            warnKnownIssue(clazz, replace);
            final int slot = AndHook.backup(clazz, name, signature);
            if (slot != -1 && saveBackupSlot(slot, replace)) {
                AndHook.hook(clazz, name, signature, replace, slot);
            }
        }

        private static boolean saveBackupSlot(final Integer slot,
                                              final Method md) {
            // a simple workaround for overloaded methods
            final Pair<String, String> key = Pair.create(md.getDeclaringClass()
                    .getName(), md.getName()
                    + (md.getParameterTypes().length - 1));
            if (sBackups.containsKey(key)) {
                Log.e(AndHook.LOG_TAG, "duplicate key error!");
                return false;
            }
            sBackups.put(key, slot);
            Log.i(LOG_TAG, "saved backup for " + key.first + "@" + key.second
                    + ", slot = " + slot);
            return true;
        }

        private static int getBackupSlot(final int identifier) {
            final StackTraceElement stack = Thread.currentThread()
                    .getStackTrace()[4];
            final Integer slot = sBackups.get(Pair.create(stack.getClassName(),
                    stack.getMethodName() + identifier));
            if (slot == null) {
                Log.e(LOG_TAG, "no backup found for " + stack.getClassName()
                        + "@" + stack.getMethodName() + "@" + identifier);
                return -1;
            }
            return slot;
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

        @SuppressWarnings("unchecked")
        public static <T> T invokeObjectOrigin(final Object receiver,
                                               final Object... params) {
            return (T) AndHook.invoke(getBackupSlot(params.length), receiver,
                    params);
        }

        public static void setObjectField(final Object obj, final String name,
                                          final Object value) {
            final Field f = findFieldHierarchically(obj.getClass(), name);
            if (f != null) {
                try {
                    f.set(obj, value);
                } catch (final Exception e) {
                    Log.e(AndHook.LOG_TAG, "failed to set instance field "
                            + name, e);
                }
            }
        }

        public static void setStaticObjectField(final Class<?> clazz,
                                                final String name, final Object value) {
            final Field f = findFieldHierarchically(clazz, name);
            if (f != null) {
                try {
                    f.set(null, value);
                } catch (final Exception e) {
                    Log.e(AndHook.LOG_TAG, "failed to set static field " + name
                            + " for class " + clazz.getName(), e);
                }
            }
        }

        public static Class<?> findClass(final String classname) {
            return findClass(classname, AndHook.class.getClassLoader());
        }

        public static Class<?> findClass(final String classname,
                                         final ClassLoader loader) {
            try {
                return loader.loadClass(classname);
            } catch (final Exception e) {
                Log.e(AndHook.LOG_TAG, "failed to find class " + classname
                        + " on ClassLoader " + loader, e);
            }
            return null;
        }

        public static Field findFieldHierarchically(final Class<?> clazz,
                                                    final String name) {
            Field f = null;
            Class<?> c = clazz;
            do {
                try {
                    f = c.getDeclaredField(name);
                } catch (final NoSuchFieldException e) {
                    c = c.getSuperclass();
                    if (c == null)
                        break;
                }
            } while (f == null);
            if (f != null) {
                f.setAccessible(true);
            } else {
                Log.e(AndHook.LOG_TAG, "failed to find field " + name
                        + " of class " + clazz.getName());
            }
            return f;
        }

        public static Constructor<?> findConstructorHierarchically(final Class<?> clazz,
                                                                   final Class<?>... parameterTypes) {
            Constructor<?> m = null;
            Class<?> c = clazz;
            do {
                try {
                    m = c.getConstructor(parameterTypes);
                } catch (final NoSuchMethodException e) {
                    c = c.getSuperclass();
                    if (c == null)
                        break;
                }
            } while (m == null);
            if (m != null) {
                m.setAccessible(true);
            } else {
                Log.e(AndHook.LOG_TAG, "failed to find constructor of class " + clazz.getName());
            }
            return m;
        }

        public static Method findMethodHierarchically(final Class<?> clazz,
                                                      final String name, final Class<?>... parameterTypes) {
            Method m = null;
            Class<?> c = clazz;
            do {
                try {
                    m = c.getDeclaredMethod(name, parameterTypes);
                } catch (final NoSuchMethodException e) {
                    c = c.getSuperclass();
                    if (c == null)
                        break;
                }
            } while (m == null);
            if (m != null) {
                m.setAccessible(true);
            } else {
                Log.e(AndHook.LOG_TAG, "failed to find method " + name
                        + " of class " + clazz.getName());
            }
            return m;
        }

        private static boolean isConstructor(final Class<?> clazz,
                                             final String methodname) {
            final String clsname = clazz.getName();
            return methodname.equals(constructorName)
                    || clsname.endsWith("." + methodname)
                    || clsname.endsWith("$" + methodname);
        }

        @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
        @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
        public @interface Hook {
            String value() default ""; // class name

            Class<?> clazz() default Hook.class; // class

            String name() default ""; // target method name

            boolean need_origin() default true;
        }

        public static void applyHooks(final Class<?> holdClass) {
            applyHooks(holdClass, holdClass.getClassLoader());
        }

        public static void applyHooks(final Class<?> holdClass,
                                      final ClassLoader loader) {
            for (final Method hookMethod : holdClass.getDeclaredMethods()) {
                final Hook hookInfo = hookMethod.getAnnotation(Hook.class);
                if (hookInfo != null) {
                    String name = hookInfo.name();
                    if (name.isEmpty())
                        name = hookMethod.getName();

                    Class<?> clazz = hookInfo.clazz();
                    try {
                        if (clazz == Hook.class)
                            clazz = loader.loadClass(hookInfo.value());
                        final Class<?>[] _parameterTypes = hookMethod
                                .getParameterTypes();
                        if (_parameterTypes.length < 1) {
                            Log.e(AndHook.LOG_TAG, "unexpected args number!");
                            continue;
                        }
                        final Class<?>[] parameterTypes = new Class[_parameterTypes.length - 1];
                        System.arraycopy(_parameterTypes, 1, parameterTypes, 0,
                                parameterTypes.length);

                        Member origin;
                        if (isConstructor(clazz, name)) {
                            origin = findConstructorHierarchically(clazz, parameterTypes);
                        } else {
                            origin = findMethodHierarchically(clazz, name,
                                    parameterTypes);
                        }
                        if (origin != null) {
                            if (Modifier.isStatic(origin.getModifiers())) {
                                AndHook.ensureClassInitialized(clazz);
                            }
                            if (hookInfo.need_origin()) {
                                hook(origin, hookMethod);
                            } else {
                                hookNoBackup(origin, hookMethod);
                            }
                        }
                    } catch (final Exception e) {
                        Log.e(AndHook.LOG_TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private static final class Dalvik {
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
