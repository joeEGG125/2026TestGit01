package com.syscom.fep.frmcommon.util;

import org.apache.commons.io.FilenameUtils;

/**
 * 用來解決Fortify Path Manipulation Issues
 *
 * @author Richard
 */
public class CleanPathUtil {
//    private static final Map<Character, Character> whiteChars = new HashMap<>();
//
//    static {
//        for (int i = 0; i <= 126; i++) {
//            whiteChars.put((char) i, (char) i);
//        }
//    }

    private CleanPathUtil() {
    }

    public static String cleanString(String aString) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < aString.length(); i++) {
//            char charAt = aString.charAt(i);
//            Character character = whiteChars.get(charAt);
//            if (character != null) {
//                sb.append(character);
//            }
//        }
//        return sb.toString();
        return FilenameUtils.normalize(aString);
    }
}
