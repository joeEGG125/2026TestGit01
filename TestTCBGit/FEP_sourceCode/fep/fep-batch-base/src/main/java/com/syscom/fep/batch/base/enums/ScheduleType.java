package com.syscom.fep.batch.base.enums;

public enum ScheduleType {
    Daily,
    DailyRepetition,
    Monthly,
    MonthDayOfWeek,
    Weekly;

    public static boolean isDaily(ScheduleType scheduleType) {
        return scheduleType == Daily || scheduleType == DailyRepetition;
    }

    public static boolean isMonthly(ScheduleType scheduleType) {
        return scheduleType == Monthly || scheduleType == MonthDayOfWeek;
    }

    public static boolean isWeekly(ScheduleType scheduleType) {
        return scheduleType == Weekly;
    }
}
