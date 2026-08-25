package com.syscom.fep.frmcommon.log;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class LogMDC {
    private static final Map<String, String> keyToDefaultValueMap = new HashMap<>();

    private LogMDC() {}

    public static void setDefaultValue(String[] keys, String[] defaultValues) {
        if (ArrayUtils.isEmpty(keys) || ArrayUtils.isEmpty(defaultValues)) {
            return;
        } else if (keys.length != defaultValues.length) {
            return;
        }
        for (int i = 0; i < keys.length; i++) {
            if (StringUtils.isBlank(keys[i]) || defaultValues[i] == null) {
                continue;
            }
            keyToDefaultValueMap.put(keys[i], defaultValues[i]);
        }
    }

    public static Map<String, String> getAll(String... keys) {
        Map<String, String> map = new HashMap<>();
        if (ArrayUtils.isNotEmpty(keys)) {
            for (String key : keys) {
                map.put(key, MDC.get(key));
            }
        }
        return map;
    }

    public static void put(Map<String, String> map) {
        if (MapUtils.isNotEmpty(map)) {
            for (String key : map.keySet()) {
                MDC.put(key, map.get(key));
            }
        }
    }

    public static void clear(String... keptKeys) {
        String[] values = null;
        if (ArrayUtils.isNotEmpty(keptKeys)) {
            values = new String[keptKeys.length];
            for (int i = 0; i < keptKeys.length; i++) {
                values[i] = MDC.get(keptKeys[i]);
            }
        }
        MDC.clear();
        if (ArrayUtils.isNotEmpty(values)) {
            for (int i = 0; i < keptKeys.length; i++) {
                put(keptKeys[i], values[i]);
            }
        }
    }

    public static void put(String key, String value, String... defaultValue) {
        String putValue = value;
        if (putValue == null) {
            if (ArrayUtils.isNotEmpty(defaultValue) && defaultValue[0] != null) {
                putValue = defaultValue[0];
            } else {
                String def = keyToDefaultValueMap.get(key);
                if (def != null) {
                    putValue = def;
                } else {
                    putValue = StringUtils.EMPTY;
                }
            }
        }
        MDC.put(key, putValue);
    }

    public static String get(String key, String... defaultValue) {
        String value = MDC.get(key);
        if (StringUtils.isBlank(value)) {
            if (ArrayUtils.isNotEmpty(defaultValue) && defaultValue[0] != null) {
                return defaultValue[0];
            }
        }
        return value;
    }
}
