package com.zzhalex233.ae2cellrender.client.drive.compat;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public final class ReflectiveDriveAccess {

    private ReflectiveDriveAccess() {
    }

    public static boolean matchesClass(@Nullable Object target, String className) {
        return target != null && target.getClass().getName().equals(className);
    }

    public static boolean invokeBoolean(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Boolean && (Boolean) value;
    }

    public static int invokeInt(Object target, String methodName, int fallback) {
        Object value = invoke(target, methodName);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    public static int invokeInt(Object target, String methodName, int fallback, int argument) {
        Object value = invoke(target, methodName, argument);
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    public static EnumFacing invokeFacing(Object target, String methodName, EnumFacing fallback) {
        Object value = invoke(target, methodName);
        return value instanceof EnumFacing ? (EnumFacing) value : fallback;
    }

    @Nullable
    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static Object invoke(Object target, String methodName, int argument) {
        try {
            Method method = target.getClass().getMethod(methodName, Integer.TYPE);
            return method.invoke(target, argument);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
