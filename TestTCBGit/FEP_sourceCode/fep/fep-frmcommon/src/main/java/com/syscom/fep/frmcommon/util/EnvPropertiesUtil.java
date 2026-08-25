package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

public class EnvPropertiesUtil {
    private static final LogHelper logger = new LogHelper();

    private EnvPropertiesUtil() {}

    public static List<String> getPropertyList(String propName, String delimiter) {
        return getPropertyList(propName, delimiter, t -> {
            return t;
        });
    }

    public static <T> List<T> getPropertyList(String propName, String delimiter, Function<String, T> func) {
        List<T> list = new ArrayList<>();
        String value = getProperty(propName, null);
        if (StringUtils.isNotBlank(value)) {
            String[] strs = value.split(delimiter);
            for (String str : strs) {
                list.add(func.apply(str));
            }
            return list;
        }
        return list;
    }

    public static boolean getProperty(String propName, boolean defaultValue) {
        String value = getProperty(propName, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    public static short getProperty(String propName, short defaultValue) {
        String value = getProperty(propName, String.valueOf(defaultValue));
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            logger.error(e, e.getMessage());
        }
        return defaultValue;
    }

    public static long getProperty(String propName, long defaultValue) {
        String value = getProperty(propName, String.valueOf(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.error(e, e.getMessage());
        }
        return defaultValue;
    }

    public static int getProperty(String propName, int defaultValue) {
        String value = getProperty(propName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error(e, e.getMessage());
        }
        return defaultValue;
    }

    public static String getProperty(String propName, String defaultValue) {
        Environment env = getEnvironment();
        if (env != null) {
            return env.getProperty(propName, defaultValue);
        }
        return defaultValue;
    }

    public static boolean hasProperty(String propName) {
        Environment env = getEnvironment();
        if (env != null) {
            return env.containsProperty(propName);
        }
        return false;
    }

    public static String replaceValue(Properties properties, String value) {
        if (StringUtils.isNotBlank(value)) {
            int begin, end;
            String key;
            while (true) {
                begin = value.indexOf("${");
                end = value.indexOf("}", begin);
                if (begin == -1 || end == -1) {
                    break;
                }
                key = value.substring(begin + 2, end);
                value = value.replace(StringUtils.join("${", key, "}"), properties.getProperty(key));
            }
        }
        return value;
    }

    private static Environment getEnvironment() {
        return SpringBeanFactoryUtil.getBean(Environment.class, false);
    }
}