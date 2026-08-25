package com.syscom.fep.frmcommon.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.scheduler.enums.CronExpField;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsOfTheYear;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.MathUtil;

public class CronExpressionGenerator {
    private final static LogHelper logger = new LogHelper();

    public final static String CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK_AND_DAY_OF_WEEK = "CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK_AND_DAY_OF_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_DAY_OF_WEEK = "CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_DAY_OF_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK = "CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK = "CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_WEEK = "CANNOT_GENERATE_CAUSE_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_LAST_WEEK = "CANNOT_GENERATE_CAUSE_LAST_WEEK";
    public final static String CANNOT_GENERATE_CAUSE_MINUTES_INTERVAL_GREATER_AND_EQUALS_THAN_60 = "CANNOT_GENERATE_CAUSE_MINUTES_INTERVAL_GREATER_AND_EQUALS_THAN_60";

    private CronExpressionGenerator() {}

    /**
     * 按天運行
     *
     * @param startTime    開始時間
     * @param daysInterval 多少天運行一次
     * @return
     */
    public static String generateCronExpressionByDaily(Date startTime, int daysInterval) {
        Calendar triggerStartTime = Calendar.getInstance();
        triggerStartTime.setTime(startTime);
        List<String> cronExpressionList = CronExpField.getCronExpressionList();
        cronExpressionList.set(CronExpField.SECOND.getIndex(), Integer.toString(triggerStartTime.get(Calendar.SECOND)));
        cronExpressionList.set(CronExpField.MINUTE.getIndex(), Integer.toString(triggerStartTime.get(Calendar.MINUTE)));
        cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(triggerStartTime.get(Calendar.HOUR_OF_DAY)));
        // 多少天運行一次
        if (daysInterval > 1) {
            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(),
                    StringUtils.join(CronExpField.DAY_OF_MONTH.getValue(), "/", daysInterval));
        }
        return toCronExpression(cronExpressionList);
    }

    /**
     * 按天運行
     *
     * @param startTime       開始時間
     * @param daysInterval    多少天運行一次
     * @param minutesInterval 多少分鐘運行一次
     * @param hoursDuration   運行幾個小時
     * @return
     * @throws UnsupportedOperationException
     */
    public static String generateCronExpressionByDailyRepetition(Date startTime, int daysInterval, int minutesInterval, int hoursDuration) throws UnsupportedOperationException {
        // 先檢核
        if (minutesInterval > 60 * 24) {
            throw ExceptionUtil.createIllegalArgumentException("minutesInterval = [", minutesInterval, "], which cannot be greater than 1440!!!");
        } else if (hoursDuration > 24) {
            throw ExceptionUtil.createIllegalArgumentException("hoursDuration = [", hoursDuration, "], which cannot be greater than 24!!!");
        }
        Calendar triggerStartTime = Calendar.getInstance();
        triggerStartTime.setTime(startTime);
        List<String> cronExpressionList = CronExpField.getCronExpressionList();
        cronExpressionList.set(CronExpField.SECOND.getIndex(), Integer.toString(triggerStartTime.get(Calendar.SECOND)));
        cronExpressionList.set(CronExpField.MINUTE.getIndex(), Integer.toString(triggerStartTime.get(Calendar.MINUTE)));
        cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(triggerStartTime.get(Calendar.HOUR_OF_DAY)));
        // 如果分鐘數大於60, 就要拆成時分
        int hourInterval = 0;
        if (minutesInterval >= 60) {
            hourInterval = minutesInterval / 60;
            minutesInterval = minutesInterval % 60;
            // 大於60分鐘, 並且分鐘數不能被60整除, 需要藉助policy創建schedule
            if (minutesInterval > 0) {
                throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_MINUTES_INTERVAL_GREATER_AND_EQUALS_THAN_60);
            }
            if (hourInterval == 1) {
                cronExpressionList.set(CronExpField.HOUR.getIndex(), CronExpField.HOUR.getValue());
            } else {
                cronExpressionList.set(CronExpField.HOUR.getIndex(), StringUtils.join(CronExpField.HOUR.getValue(), "/", hourInterval));
            }
        }
        // 多少分鐘運行一次
        if (minutesInterval == 1) {
            cronExpressionList.set(CronExpField.MINUTE.getIndex(), CronExpField.MINUTE.getValue());
        } else if (minutesInterval > 1) {
            cronExpressionList.set(CronExpField.MINUTE.getIndex(), StringUtils.join(CronExpField.MINUTE.getValue(), "/", minutesInterval));
        }
        // 運行幾個小時
        if (hoursDuration == 0) {
            if (hourInterval > 1) {
                // 上面大於1的時候會塞值, 所以這裡就不要再塞了
            } else {
                cronExpressionList.set(CronExpField.HOUR.getIndex(), CronExpField.HOUR.getValue());
            }
        } else if (hoursDuration == 1) {
            // 不管上面hourInterval是多少, 這裡限定只運行一小時, 那麼就要重新塞入
            cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(triggerStartTime.get(Calendar.HOUR_OF_DAY)));
        } else if (hoursDuration > 1) {
            Calendar triggerEndTime = Calendar.getInstance();
            triggerEndTime.setTime(startTime);
            triggerEndTime.add(Calendar.HOUR_OF_DAY, hoursDuration - 1);
            cronExpressionList.set(CronExpField.HOUR.getIndex(),
                    StringUtils.join(triggerStartTime.get(Calendar.HOUR_OF_DAY), "-", triggerEndTime.get(Calendar.HOUR_OF_DAY),
                            // 如果hourInterval大於1, 說明還要間隔小時數
                            hourInterval > 1 ? StringUtils.join("/", hourInterval) : StringUtils.EMPTY));
        }
        // 多少天運行一次
        if (daysInterval > 1) {
            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), StringUtils.join(CronExpField.DAY_OF_MONTH.getValue(), "/", daysInterval));
        }
        return toCronExpression(cronExpressionList);
    }

    /**
     * 按周運行
     *
     * @param startTime     開始時間
     * @param daysOfWeek    某周的哪一天, 具體參考{@DaysOfTheWeek}
     * @param weeksInterval 多少周運行一次
     * @return
     */
    public static String generateCronExpressionByWeekly(Date startTime, int daysOfWeek, int weeksInterval) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        List<String> cronExpressionList = CronExpField.getCronExpressionList();
        cronExpressionList.set(CronExpField.SECOND.getIndex(), Integer.toString(cal.get(Calendar.SECOND)));
        cronExpressionList.set(CronExpField.MINUTE.getIndex(), StringUtils.join(cal.get(Calendar.MINUTE)));
        cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
        // 一周的哪一天, 具體參考{@DaysOfTheWeek}
        if (daysOfWeek > 0 && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
            setDaysOfWeek(daysOfWeek, cronExpressionList);
        }
        // 多少周運行一次
        else if (weeksInterval > 0) {
            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(),
                    StringUtils.join(CronExpField.DAY_OF_MONTH.getValue(), "/", weeksInterval * 7));
        }
        return toCronExpression(cronExpressionList);
    }

    /**
     * 按月運行
     *
     * @param startTime           開始時間
     * @param monthsOfYear        某年的哪一月
     * @param daysOfMonth         某月的哪一天
     * @param runOnLastDayOfMonth 是否某月最後一天運行
     * @return
     */
    public static String generateCronExpressionByMonthly(Date startTime, int monthsOfYear, String daysOfMonth, boolean runOnLastDayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        List<String> cronExpressionList = CronExpField.getCronExpressionList();
        cronExpressionList.set(CronExpField.SECOND.getIndex(), Integer.toString(cal.get(Calendar.SECOND)));
        cronExpressionList.set(CronExpField.MINUTE.getIndex(), StringUtils.join(cal.get(Calendar.MINUTE)));
        cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
        // 某月的哪一天
        if (StringUtils.isNotBlank(daysOfMonth)) {
            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), daysOfMonth);
        }
        // 是否某月最後一天運行
        else if (runOnLastDayOfMonth) {
            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "L");
        }
        // 某年的哪一月
        if (monthsOfYear > 0 && monthsOfYear != MonthsOfTheYear.AllMonths.getValue()) {
            setMonthsOfYear(monthsOfYear, cronExpressionList);
        }
        return toCronExpression(cronExpressionList);
    }

    /**
     * 按月周天運行
     *
     * @param startTime            開始時間
     * @param monthsOfYear         某年的哪一月, 具體參考{@MonthsOfTheYear}
     * @param weeksOfMonth         某月的哪一周, 具體參考{@WeeksOfTheMonth}
     * @param daysOfWeek           某周的哪一天, 具體參考{@DaysOfTheWeek}
     * @param runOnLastWeekOfMonth 是否某月最後一周運行
     * @return
     * @throws UnsupportedOperationException
     */
    public static String generateCronExpressionByMonthDayOfWeek(Date startTime, int monthsOfYear, int weeksOfMonth, int daysOfWeek, boolean runOnLastWeekOfMonth) throws UnsupportedOperationException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        List<String> cronExpressionList = CronExpField.getCronExpressionList();
        cronExpressionList.set(CronExpField.SECOND.getIndex(), Integer.toString(cal.get(Calendar.SECOND)));
        cronExpressionList.set(CronExpField.MINUTE.getIndex(), StringUtils.join(cal.get(Calendar.MINUTE)));
        cronExpressionList.set(CronExpField.HOUR.getIndex(), Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
        // 某年的哪一月
        if (monthsOfYear > 0 && monthsOfYear != MonthsOfTheYear.AllMonths.getValue()) {
            setMonthsOfYear(monthsOfYear, cronExpressionList);
        }
        // 指定每個月最後一周
        if (runOnLastWeekOfMonth) {
            // 指定 某月的哪一周 & 某周的哪一天
            if (weeksOfMonth > 0 && daysOfWeek > 0) {
                // 指定 某月的哪一周 & 某周的哪一天
                if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
                    // 某月指定不是最後一周
                    if (weeksOfMonth != WeeksOfTheMonth.LastWeek.getValue()) {
                        // 某月指定周任意一天 & 某月最後一周任意一天
                        // 又要某一周, 又要最後一周, 無法創建
                        throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK_AND_DAY_OF_WEEK);
                    }
                    // 某月指定最後一周
                    else {
                        // 如果daysOfWeek只是一天, 則直接可以創建
                        List<Integer> daysOfWeekList = MathUtil.splitByPow2(daysOfWeek);
                        if (CollectionUtils.isNotEmpty(daysOfWeekList) && daysOfWeekList.size() == 1) {
                            // 因為都是指定最後一周, 可以創建
                            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "?");
                            cronExpressionList.set(CronExpField.DAY_OF_WEEK.getIndex(),
                                    StringUtils.join(DaysOfTheWeek.fromValue(daysOfWeek).getQuartzValue(), WeeksOfTheMonth.LastWeek.getQuartzValue()));
                        } else {
                            // 最後一周的任意幾天, 無法創建
                            throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK);
                        }
                    }
                }
                // 指定某月任意一天
                else if (weeksOfMonth == WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
                    // 某月任意一周, 包含某月最後一周, 可以創建
                    setDaysOfWeek(daysOfWeek, cronExpressionList);
                }
                // 指定某月第幾周
                else if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek == DaysOfTheWeek.AllDays.getValue()) {
                    // 無論是指定第幾周, 還是最後一周, 沒有具體某天, 所以無法創建
                    throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK);
                }
            }
            // 只有某月的哪一周
            else if (weeksOfMonth > 0) {
                if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue()) {
                    // 無論是指定第幾周, 還是最後一周, 沒有具體某天, 所以無法創建
                    throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_LAST_WEEK);
                }
            }
            // 只有某周的哪一天
            else if (daysOfWeek > 0) {
                // 指定某周第幾天
                if (daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
                    List<Integer> daysOfWeekList = MathUtil.splitByPow2(daysOfWeek);
                    if (CollectionUtils.isNotEmpty(daysOfWeekList) && daysOfWeekList.size() == 1) {
                        // 因為都是指定最後一周, 可以創建
                        cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "?");
                        cronExpressionList.set(CronExpField.DAY_OF_WEEK.getIndex(),
                                StringUtils.join(DaysOfTheWeek.fromValue(daysOfWeek).getQuartzValue(), WeeksOfTheMonth.LastWeek.getQuartzValue()));
                    } else {
                        // 某月最後一周的任意幾天, 無法創建
                        throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK);
                    }
                } else {
                    // 某月最後一周 無法組建
                    throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_LAST_WEEK);
                }
            }
        }
        // 不指定每個月最後一周
        else {
            if (weeksOfMonth > 0 && daysOfWeek > 0) {
                if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
                    List<Integer> daysOfWeekList = MathUtil.splitByPow2(daysOfWeek);
                    if (weeksOfMonth != WeeksOfTheMonth.LastWeek.getValue()) {
                        List<Integer> weeksOfMonthList = MathUtil.splitByPow2(weeksOfMonth);
                        // 第幾周, 第幾天是固定的, 可以創建
                        if (CollectionUtils.isNotEmpty(daysOfWeekList) && daysOfWeekList.size() == 1
                                && CollectionUtils.isNotEmpty(weeksOfMonthList) && weeksOfMonthList.size() == 1) {
                            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "?");
                            cronExpressionList.set(CronExpField.DAY_OF_WEEK.getIndex(),
                                    StringUtils.join(DaysOfTheWeek.fromValue(daysOfWeek).getShortName(), "#", WeeksOfTheMonth.fromValue(weeksOfMonth).getQuartzValue()));
                        } else {
                            // 任意幾周的任意幾天
                            throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_WEEK_AND_DAY_OF_WEEK);
                        }
                    } else {
                        // 最後一周的第幾天是固定的, 可以創建
                        if (CollectionUtils.isNotEmpty(daysOfWeekList) && daysOfWeekList.size() == 1) {
                            cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "?");
                            cronExpressionList.set(CronExpField.DAY_OF_WEEK.getIndex(),
                                    StringUtils.join(DaysOfTheWeek.fromValue(daysOfWeek).getQuartzValue(), WeeksOfTheMonth.LastWeek.getQuartzValue()));
                        } else {
                            // 最後一周的任意幾天
                            throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_BOTH_LAST_WEEK_AND_DAY_OF_WEEK);
                        }
                    }
                } else if (weeksOfMonth == WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
                    setDaysOfWeek(daysOfWeek, cronExpressionList);
                } else if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue() && daysOfWeek == DaysOfTheWeek.AllDays.getValue()) {
                    // 某月指定周 無法組建
                    throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_WEEK);
                }
            } else if (weeksOfMonth > 0) {
                if (weeksOfMonth != WeeksOfTheMonth.AllWeeks.getValue()) {
                    // 某月指定周 無法組建
                    throw ExceptionUtil.createUnsupportedOperationException(CANNOT_GENERATE_CAUSE_WEEK);
                }
            } else {
                setDaysOfWeek(daysOfWeek, cronExpressionList);
            }
        }
        return toCronExpression(cronExpressionList);
    }

    private static void setDaysOfWeek(int daysOfWeek, List<String> cronExpressionList) {
        if (daysOfWeek > 0 && daysOfWeek != DaysOfTheWeek.AllDays.getValue()) {
            StringBuilder sb = new StringBuilder();
            List<Integer> dayOfWeekList = MathUtil.splitByPow2(daysOfWeek);
            try {
                for (int dayOfWeek : dayOfWeekList) {
                    DaysOfTheWeek dayOfTheWeek = DaysOfTheWeek.fromValue(dayOfWeek);
                    sb.append(dayOfTheWeek.getShortName()).append(",");
                }
            } catch (IllegalArgumentException e) {
                logger.warn(e, e.getMessage());
            }
            if (sb.length() > 0) {
                cronExpressionList.set(CronExpField.DAY_OF_MONTH.getIndex(), "?");
                cronExpressionList.set(CronExpField.DAY_OF_WEEK.getIndex(), sb.deleteCharAt(sb.length() - 1).toString());
            }
        }
    }

    private static void setMonthsOfYear(int monthsOfYear, List<String> cronExpressionList) {
        if (monthsOfYear > 0 && monthsOfYear != MonthsOfTheYear.AllMonths.getValue()) {
            StringBuilder sb = new StringBuilder();
            List<Integer> monthOfYearList = MathUtil.splitByPow2(monthsOfYear);
            try {
                for (int monthOfYear : monthOfYearList) {
                    MonthsOfTheYear monthsOfTheYear = MonthsOfTheYear.fromValue(monthOfYear);
                    sb.append(monthsOfTheYear.getShortName()).append(",");
                }
            } catch (IllegalArgumentException e) {
                logger.warn(e, e.getMessage());
            }
            if (sb.length() > 0) {
                cronExpressionList.set(CronExpField.MONTH.getIndex(), sb.deleteCharAt(sb.length() - 1).toString());
            }
        }
    }

    private static String toCronExpression(List<String> cronExpressionList) {
        return StringUtils.join(cronExpressionList, StringUtils.SPACE);
    }
}
