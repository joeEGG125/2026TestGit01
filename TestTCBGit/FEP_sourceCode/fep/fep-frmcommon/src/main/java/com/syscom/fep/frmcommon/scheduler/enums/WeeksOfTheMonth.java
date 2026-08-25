package com.syscom.fep.frmcommon.scheduler.enums;

import java.io.Serializable;
import java.util.Calendar;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;

public enum WeeksOfTheMonth implements Serializable {
    FirstWeek(1, 1, "1"),
    SecondWeek(2, 2, "2"),
    ThirdWeek(4, 3, "3"),
    FourthWeek(8, 4, "4"),
    LastWeek(16, 5, "L"),
    AllWeeks(31, -1, null);

    private final int value;
    private final int weekOfMonth;
    private final String quartzValue;

    private WeeksOfTheMonth(int value, int weekOfMonth, String quartzValue) {
        this.value = value;
        this.weekOfMonth = weekOfMonth;
        this.quartzValue = quartzValue;
    }

    public int getValue() {
        return value;
    }

    public int getWeekOfMonth() {
        return weekOfMonth;
    }

    public String getQuartzValue() {
        return quartzValue;
    }

    public static WeeksOfTheMonth fromValue(int value) {
        for (WeeksOfTheMonth e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static WeeksOfTheMonth fromWeekOfMonth(int weekOfMonth) {
        for (WeeksOfTheMonth e : values()) {
            if (e.getWeekOfMonth() == weekOfMonth) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid weekOfMonth = [" + weekOfMonth + "]!!!");
    }

    public static WeeksOfTheMonth parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (WeeksOfTheMonth e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

    public static WeeksOfTheMonth getWeeksOfTheMonth(Calendar calendar, int amount) {
        int weekOfMonth = CalendarUtil.getWeekOfMonth(CalendarUtil.add(calendar, Calendar.DAY_OF_MONTH, amount * 7));
        return fromWeekOfMonth(weekOfMonth);
    }

    public static WeeksOfTheMonth getWeeksOfTheMonth(Calendar calendar, int field, int amount) {
        int weekOfMonth = CalendarUtil.getWeekOfMonth(CalendarUtil.add(calendar, field, amount));
        return fromWeekOfMonth(weekOfMonth);
    }
}
