package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class UUIDUtil {
    private UUIDUtil() {
    }

    public static String randomUUID() {
        return randomUUID(false);
    }

    public static String randomUUID(boolean removeDash) {
        String uuid = UUID.randomUUID().toString();
        if (removeDash) {
            return StringUtils.replace(uuid, "-", StringUtils.EMPTY);
        }
        return uuid;
    }
}
