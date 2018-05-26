package andhook.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;

/*
 * Keep this class if you want to compatible with YunOS.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class YunOSHelper {
    // Holds a mapping from Java type names to native type codes.
    private static final HashMap<Class<?>, String> PRIMITIVE_TO_SIGNATURE;

    static {
        PRIMITIVE_TO_SIGNATURE = new HashMap<>(9);
        PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
        PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
        PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
        PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
        PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
        PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
        PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
        PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
        PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");
    }

    /**
     * Returns the internal name of {@code clazz} (also known as the
     * descriptor).
     */
    public static String getSignature(final Class<?> clazz) {
        final String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        } else {
            return "L" + clazz.getName().replace('.', '/') + ";";
        }
    }

    /**
     * Returns the native type codes of {@code clazz}.
     */
    public static String getNativeTypeCode(final Class<?> clazz) {
        final String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        }
        return "L";
    }

    // @SuppressWarnings("ConstantConditions")
    private static String getSignature(final Class<?> retType,
                                       final Class<?>[] parameterTypes) {
        final StringBuilder result = new StringBuilder();

        result.append('(');
        for (final Class<?> parameterType : parameterTypes) {
            result.append(getSignature(parameterType));
        }
        result.append(")");
        result.append(getSignature(retType));

        return result.toString();
    }

    public static String getSignature(final Member m) {
        if (m instanceof Method) {
            final Method md = (Method) m;
            return getSignature(md.getReturnType(), md.getParameterTypes());
        }
        if (m instanceof Constructor) {
            final Constructor<?> c = (Constructor<?>) m;
            return getSignature(void.class, c.getParameterTypes());
        }
        return null;
    }

    // @SuppressWarnings("ConstantConditions")
    private static String getShorty(final Class<?> retType,
                                    final Class<?>[] parameterTypes) {
        final StringBuilder result = new StringBuilder();

        result.append(getNativeTypeCode(retType));
        for (final Class<?> parameterType : parameterTypes) {
            result.append(getNativeTypeCode(parameterType));
        }

        return result.toString();
    }

    // @Keep
    public static String getShorty(final Member m) {
        if (m instanceof Method) {
            final Method md = (Method) m;
            return getShorty(md.getReturnType(), md.getParameterTypes());
        }
        if (m instanceof Constructor) {
            final Constructor<?> c = (Constructor<?>) m;
            return getShorty(void.class, c.getParameterTypes());
        }
        return null;
    }
}
