package com.syscom.fep.gateway.cmd;

import java.util.*;

public class GatewayCommandUtil {
    private static final int PAD_LIMIT = 8192;
    private static final Map<Character, Character> whiteChars = new HashMap<>();

    static {
        for (int i = 0; i <= 126; i++) {
            whiteChars.put((char) i, (char) i);
        }
    }

    private GatewayCommandUtil() {
    }

    public static String cleanString(String aString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aString.length(); i++) {
            char charAt = aString.charAt(i);
            Character character = whiteChars.get(charAt);
            if (character != null) {
                sb.append(character);
            }
        }
        return sb.toString();
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(final CharSequence cs) {
        if (isEmpty(cs)) return true;
        for (int i = 0; i < cs.length(); i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static <T> String printOut(T... t) {
        String str = join(t);
        System.out.print(str);
        return str;
    }

    public static <T> String printOutLn(T... t) {
        String str = join(t);
        System.out.println(str);
        return str;
    }

    public static <T> String printErrLn(T... t) {
        String str = join(t);
        System.err.println(str);
        return str;
    }

    public static <T> String join(T... t) {
        if (t == null) {
            return null;
        }
        final StringJoiner joiner = new StringJoiner("");
        for (int i = 0; i < t.length; i++) {
            joiner.add(toStringOrEmpty(t[i]));
        }
        return joiner.toString();
    }

    public static String toStringOrEmpty(final Object obj) {
        return Objects.toString(obj, "");
    }

    public static String repeat(final String str, final int repeat) {
        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return "";
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1:
                return repeat(str.charAt(0), repeat);
            case 2:
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default:
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }
}
