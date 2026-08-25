package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericTypeUtil {

    private GenericTypeUtil() {
    }

    public static Type getGenericType(Class<?> cls, int index) {
        ParameterizedType parameterizedType = ((ParameterizedType) cls.getGenericSuperclass());
        Type[] types = parameterizedType.getActualTypeArguments();
        if (ArrayUtils.isEmpty(types) || index >= types.length) {
            return null;
        }
        return types[index];
    }

    public static <T> Class<T> getGenericSuperClass(Class<?> cls, int index) {
        Type type = getGenericType(cls, index);
        return type == null ? null : getClass(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(Type type) {
        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }
        return (Class<T>) type;
    }
}
