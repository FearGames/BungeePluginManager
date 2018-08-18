package it.feargames.bungeepluginmanager;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@UtilityClass
public class ReflectionUtils {

    private static Field getFieldRecursive(@NonNull final Object object, @NonNull final String fieldName) throws NoSuchFieldException {
        Class<?> clazz = object.getClass();
        while (true) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                if ((clazz = clazz.getSuperclass()) == null) {
                    throw e;
                }
            }
        }
    }

    private static Field getStaticFieldRecursive(@NonNull final Class clazz, @NonNull final String fieldName) throws NoSuchFieldException {
        Class<?> currentClazz = clazz;
        while (true) {
            try {
                return currentClazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                if ((currentClazz = clazz.getSuperclass()) == null) {
                    throw e;
                }
            }
        }
    }

    public static <T> T getFieldValue(@NonNull final Object object, @NonNull final String fieldName) throws NoSuchFieldException {
        final Field field = getFieldRecursive(object, fieldName);
        field.setAccessible(true);
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(@NonNull final Object object, @NonNull final String fieldName, @NonNull final Object value) throws NoSuchFieldException {
        final Field field = getFieldRecursive(object, fieldName);
        field.setAccessible(true);
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getStaticFieldValue(@NonNull final Class<?> clazz, @NonNull final String fieldName) throws NoSuchFieldException {
        final Field field = getStaticFieldRecursive(clazz, fieldName);
        field.setAccessible(true);
        try {
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeMethod(@NonNull final Object object, @NonNull final String methodName, @NonNull final Object... arguments) throws InvocationTargetException, NoSuchMethodException {
        Class<?> clazz = object.getClass();
        do {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterTypes().length != arguments.length) {
                    continue;
                }
                try {
                    method.setAccessible(true);
                    method.invoke(object, arguments);
                    return;
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        throw new NoSuchMethodException(object.getClass().getName() + "." + methodName + " with " + arguments.length + " arguments");
    }
}
