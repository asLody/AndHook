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
 * Optional helper class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class HookHelper {
    public static final String constructorName = "<init>";
    private static final ConcurrentHashMap<Pair<String, String>, Integer> sBackups = new ConcurrentHashMap<>();

    private static void warnKnownIssue(final Method replace) {
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
        if (uniqueKey(replace) == null)
            return;

        warnKnownIssue(replace);
        final int slot = AndHook.backup(origin);
        if (slot != -1 && saveBackupSlot(slot, replace)) {
            AndHook.hook(origin, replace, slot);
        }
    }

    public static void hook(final Class<?> clazz, final String name,
                            final String signature, final Method replace) {
        if (uniqueKey(replace) == null)
            return;

        warnKnownIssue(replace);
        final int slot = AndHook.backup(clazz, name, signature);
        if (slot != -1 && saveBackupSlot(slot, replace)) {
            AndHook.hook(clazz, name, signature, replace, slot);
        }
    }

    private static Pair<String, String> uniqueKey(final Method md) {
        // a simple workaround for overloaded methods
        final Pair<String, String> key = Pair.create(md.getDeclaringClass()
                .getName(), md.getName() + (md.getParameterTypes().length - 1));
        if (sBackups.containsKey(key)) {
            Log.e(AndHook.LOG_TAG, "duplicate key error! already hooked?");
            return null;
        }
        return key;
    }

    private static boolean saveBackupSlot(final Integer slot, final Method md) {
        final Pair<String, String> key = uniqueKey(md);
        if (key == null)
            return false;

        sBackups.put(key, slot);
        Log.i(AndHook.LOG_TAG, "saved backup for " + key.first + "@"
                + key.second + ", slot = " + slot);
        return true;
    }

    private static int getBackupSlot(final int identifier) {
        final StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
        final Integer slot = sBackups.get(Pair.create(stack.getClassName(),
                stack.getMethodName() + identifier));
        if (slot == null) {
            Log.e(AndHook.LOG_TAG,
                    "no backup found for " + stack.getClassName() + "@"
                            + stack.getMethodName() + "@" + identifier);
            return -1;
        }
        return slot;
    }

    public static void invokeVoidOrigin(final Object receiver,
                                        final Object... params) {
        AndHook.invokeVoidMethod(getBackupSlot(params.length), receiver, params);
    }

    public static boolean invokeBooleanOrigin(final Object receiver,
                                              final Object... params) {
        return AndHook.invokeBooleanMethod(getBackupSlot(params.length),
                receiver, params);
    }

    public static byte invokeByteOrigin(final Object receiver,
                                        final Object... params) {
        return AndHook.invokeByteMethod(getBackupSlot(params.length), receiver,
                params);
    }

    public static short invokeShortOrigin(final Object receiver,
                                          final Object... params) {
        return AndHook.invokeShortMethod(getBackupSlot(params.length),
                receiver, params);
    }

    public static char invokeCharOrigin(final Object receiver,
                                        final Object... params) {
        return AndHook.invokeCharMethod(getBackupSlot(params.length), receiver,
                params);
    }

    public static int invokeIntOrigin(final Object receiver,
                                      final Object... params) {
        return AndHook.invokeIntMethod(getBackupSlot(params.length), receiver,
                params);
    }

    public static long invokeLongOrigin(final Object receiver,
                                        final Object... params) {
        return AndHook.invokeLongMethod(getBackupSlot(params.length), receiver,
                params);
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
                Log.e(AndHook.LOG_TAG, "failed to set instance field " + name,
                        e);
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

    public static Constructor<?> findConstructorHierarchically(
            final Class<?> clazz, final Class<?>... parameterTypes) {
        Constructor<?> m = null;
        Class<?> c = clazz;
        do {
            try {
                m = c.getDeclaredConstructor(parameterTypes);
            } catch (final NoSuchMethodException e) {
                c = c.getSuperclass();
                if (c == null)
                    break;
            }
        } while (m == null);
        if (m != null) {
            m.setAccessible(true);
        } else {
            Log.e(AndHook.LOG_TAG, "failed to find constructor of class "
                    + clazz.getName());
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
                        origin = findConstructorHierarchically(clazz,
                                parameterTypes);
                    } else {
                        origin = findMethodHierarchically(clazz, name,
                                parameterTypes);
                    }
                    if (origin != null) {
                        if (Modifier.isStatic(origin.getModifiers())) {
                            AndHook.ensureClassInitialized(clazz);
                        }
                        if (hookInfo.need_origin()) {
                            HookHelper.hook(origin, hookMethod);
                        } else {
                            AndHook.hookNoBackup(origin, hookMethod);
                        }
                    }
                } catch (final Exception e) {
                    Log.e(AndHook.LOG_TAG, e.getMessage(), e);
                }
            }
        }
    }
}