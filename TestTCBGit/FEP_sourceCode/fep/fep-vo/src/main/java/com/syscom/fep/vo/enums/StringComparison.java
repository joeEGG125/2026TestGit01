package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

public enum StringComparison {
    //
    // 摘要:
    //     使用區分區域性的排序規則和當前區域性比較字符串。
    CurrentCulture(0),
    //
    // 摘要:
    //     通過使用區分區域性的排序規則、當前區域性，并忽略所比較的字符串的大小寫，來比較字符串。
    CurrentCultureIgnoreCase(1),
    //
    // 摘要:
    //     使用區分區域性的排序規則和固定區域性比較字符串。
    InvariantCulture(2),
    //
    // 摘要:
    //     通過使用區分區域性的排序規則、固定區域性，并忽略所比較的字符串的大小寫，來比較字符串。
    InvariantCultureIgnoreCase(3),
    //
    // 摘要:
    //     使用序號（二進制）排序規則比較字符串。
    Ordinal(4),
    //
    // 摘要:
    //     通過使用序號（二進制）區分區域性的排序規則并忽略所比較的字符串的大小寫，來比較字符串。
    OrdinalIgnoreCase(5);

    private int value;

    private StringComparison(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StringComparison fromValue(int value) {
        for (StringComparison e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static StringComparison parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (StringComparison e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

}
