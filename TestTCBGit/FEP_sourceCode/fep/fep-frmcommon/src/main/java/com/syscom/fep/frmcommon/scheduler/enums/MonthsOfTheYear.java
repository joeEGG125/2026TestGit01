package com.syscom.fep.frmcommon.scheduler.enums;

import com.syscom.fep.frmcommon.util.CalendarUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

public enum MonthsOfTheYear {
    January(1, Calendar.JANUARY, MonthsType.SOLAR),
    February(2, Calendar.FEBRUARY, MonthsType.LUNAR),
    March(4, Calendar.MARCH, MonthsType.SOLAR),
    April(8, Calendar.APRIL, MonthsType.LUNAR),
    May(16, Calendar.MAY, MonthsType.SOLAR),
    June(32, Calendar.JUNE, MonthsType.LUNAR),
    July(64, Calendar.JULY, MonthsType.SOLAR),
    August(128, Calendar.AUGUST, MonthsType.SOLAR),
    September(256, Calendar.SEPTEMBER, MonthsType.LUNAR),
    October(512, Calendar.OCTOBER, MonthsType.SOLAR),
    November(1024, Calendar.NOVEMBER, MonthsType.LUNAR),
    December(2048, Calendar.DECEMBER, MonthsType.SOLAR),
    AllMonths(4095, -1, null);

    private final int value;
    private final int monthOfYear;
    private final MonthsType monthsType;
    private final String shortName;

    private MonthsOfTheYear(int value, int monthOfYear, MonthsType monthsType) {
        this.value = value;
        this.monthOfYear = monthOfYear;
        this.monthsType = monthsType;
        this.shortName = this.name().substring(0, 3).toUpperCase();
    }

    public int getValue() {
        return value;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public MonthsType getMonthsType() {
        return monthsType;
    }

    public String getShortName() {
        return shortName;
    }

    public static MonthsOfTheYear fromValue(int value) {
        for (MonthsOfTheYear e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static MonthsOfTheYear fromMonthOfYear(int monthOfYear) {
        for (MonthsOfTheYear e : values()) {
            if (e.getMonthOfYear() == monthOfYear) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid monthOfYear = [" + monthOfYear + "]!!!");
    }

    public static MonthsOfTheYear parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (MonthsOfTheYear e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

    public static MonthsOfTheYear getMonthsOfTheYear(Calendar calendar, int amount) {
        return getMonthsOfTheYear(calendar, Calendar.MONTH, amount);
    }

    public static MonthsOfTheYear getMonthsOfTheYear(Calendar calendar, int field, int amount) {
        int monthOfYear = CalendarUtil.add(calendar, field, amount).get(Calendar.MONTH);
        return fromMonthOfYear(monthOfYear);
    }
}
