package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang3.StringUtils;

public class NumberUtil {

    private NumberUtil() {}

    public static String toHex(int value) {
        return toHex(value, 0, null);
    }

    public static String toHex(int value, int leftPadSize, String padChar) {
        return toHex(value, true, leftPadSize, padChar);
    }

    public static String toHex(int value, boolean toUpperCase, int leftPadSize, String padChar) {
        String hex = Integer.toHexString(value);
        if (toUpperCase) {
            hex = hex.toUpperCase();
        }
        if (leftPadSize >= 2 && StringUtils.isNotEmpty(padChar)) {
            return StringUtils.leftPad(hex, leftPadSize, padChar);
        }
        return hex;
    }
}
