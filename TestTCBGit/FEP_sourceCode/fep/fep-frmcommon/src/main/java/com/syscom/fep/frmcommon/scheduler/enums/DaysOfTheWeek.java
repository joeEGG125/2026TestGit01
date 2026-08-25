package com.syscom.fep.frmcommon.scheduler.enums;

import java.io.Serializable;
import java.util.Calendar;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;

public enum DaysOfTheWeek implements Serializable {
    Sunday(1, 7, Calendar.SUNDAY),
    Monday(2, 1, Calendar.MONDAY),
    Tuesday(4, 2, Calendar.TUESDAY),
    Wednesday(8, 3, Calendar.WEDNESDAY),
    Thursday(16, 4, Calendar.THURSDAY),
    Friday(32, 5, Calendar.FRIDAY),
    Saturday(64, 6, Calendar.SATURDAY),
    AllDays(127, -1, -1);

    private final int value;
    private final int dayOfWeek;
    private final int quartzValue;
    private final String shortName;

    private DaysOfTheWeek(int value, int dayOfWeek, int quartzValue) {
        this.value = value;
        this.dayOfWeek = dayOfWeek;
        this.quartzValue = quartzValue;
        this.shortName = this.name().substring(0, 3).toUpperCase();
    }

    public int getValue() {
        return value;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getQuartzValue() {
        return quartzValue;
    }

    public String getShortName() {
        return shortName;
    }

    public static DaysOfTheWeek fromValue(int value) {
        for (DaysOfTheWeek e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static DaysOfTheWeek fromDayOfWeek(int dayOfWeek) {
        for (DaysOfTheWeek e : values()) {
            if (e.getDayOfWeek() == dayOfWeek) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid dayOfWeek = [" + dayOfWeek + "]!!!");
    }

    public static DaysOfTheWeek parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (DaysOfTheWeek e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

    public static DaysOfTheWeek getDaysOfTheWeek(Calendar calendar, int amount) {
        return getDaysOfTheWeek(calendar, Calendar.DAY_OF_MONTH, amount);
    }

    public static DaysOfTheWeek getDaysOfTheWeek(Calendar calendar, int field, int amount) {
        int dayOfWeek = CalendarUtil.getDayOfWeek(CalendarUtil.add(calendar, field, amount));
        return fromDayOfWeek(dayOfWeek);
    }
}
