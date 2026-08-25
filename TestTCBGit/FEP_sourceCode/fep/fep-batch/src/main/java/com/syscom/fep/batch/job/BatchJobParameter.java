package com.syscom.fep.batch.job;

//import com.syscom.fep.batch.policy.impl.MonthDayOfWeekPolicy;
//import com.syscom.fep.batch.policy.impl.WeeklyPolicy;
import com.syscom.fep.frmcommon.scheduler.enums.DaysOfTheWeek;
import com.syscom.fep.frmcommon.scheduler.enums.MonthsOfTheYear;
import com.syscom.fep.frmcommon.scheduler.enums.WeeksOfTheMonth;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.*;

@Deprecated
public class BatchJobParameter implements Serializable {
    private String hostName;
    private String batchId; // 對應db中的PK
    private String group; // 對應quartz中JobKey和TriggerKey的group定義, 預設等於batchId
    private String name;
    private String description;
    private String policyClassname;
    private int totalTriggered = -1;
    private String action;
    private String actionArguments;
    private int weeksInterval;
    private Date triggerStartDateTime;
    private List<WeeksOfTheMonth> weeksOfTheMonthList = new ArrayList<>();
    private List<DaysOfTheWeek> daysOfTheWeekList = new ArrayList<>();
    private Map<Integer, Integer> nextExecuteDateTimeForDayOfWeekMap = Collections.synchronizedMap(new HashMap<>());
    private Date nextQuartzFireDateTime;
    private int hourInterval;
    private int minuteInterval;
    private Calendar latestExecutedDateTime;
    private Calendar nextExecutedDateTime;
    private String scheduleInfo;
    private List<Integer> daysOfTheMonthList = new ArrayList<>();
    private List<MonthsOfTheYear> monthsOfTheYearList = new ArrayList<>();
    private boolean runOnLastWeekOfMonth;
    private boolean runOnLastDayOfMonth;

    public static BatchJobParameter create(String hostName, String batchId, String name, String description, Date triggerStartTime, String action, String actionArguments) {
        return create(hostName, batchId, batchId, name, description, triggerStartTime, action, actionArguments);
    }

    public static BatchJobParameter create(String hostName, String batchId, String group, String name, String description, Date triggerStartTime, String action, String actionArguments) {
        return create(hostName, batchId, group, name, description, triggerStartTime, -1, action, actionArguments);
    }

    public static BatchJobParameter create(String hostName, String batchId, String group, String name, String description, Date triggerStartTime, int totalTriggered, String action, String actionArguments) {
        return create(hostName, batchId, group, name, description, triggerStartTime, totalTriggered, action, actionArguments, null);
    }

    public static BatchJobParameter create(String hostName, String batchId, String name, String description, Date triggerStartTime, String action, String actionArguments, Class<?> policyClass) {
        return create(hostName, batchId, batchId, name, description, triggerStartTime, action, actionArguments, policyClass);
    }

    public static BatchJobParameter create(String hostName, String batchId, String group, String name, String description, Date triggerStartTime, String action, String actionArguments, Class<?> policyClass) {
        return create(hostName, batchId, group, name, description, triggerStartTime, -1, action, actionArguments, policyClass);
    }

    public static BatchJobParameter create(String hostName, String batchId, String group, String name, String description, Date triggerStartTime, int totalTriggered, String action, String actionArguments, Class<?> policyClass) {
        BatchJobParameter parameter = new BatchJobParameter();
        parameter.setHostName(hostName);
        parameter.setBatchId(batchId);
        parameter.setGroup(group);
        parameter.setAction(action);
        parameter.setActionArguments(actionArguments);
        parameter.setDescription(description);
        parameter.setName(name);
        parameter.setPolicyClassname(policyClass != null ? policyClass.getName() : null);
        parameter.setTotalTriggered(totalTriggered);
        parameter.setTriggerStartDateTime(triggerStartTime);
        return parameter;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPolicyClassname() {
        return policyClassname;
    }

    public void setPolicyClassname(String policyClassname) {
        this.policyClassname = policyClassname;
    }

    public int getTotalTriggered() {
        return totalTriggered;
    }

    public void setTotalTriggered(int totalTriggered) {
        this.totalTriggered = totalTriggered;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionArguments() {
        return actionArguments;
    }

    public void setActionArguments(String actionArguments) {
        this.actionArguments = actionArguments;
    }

    public int getWeeksInterval() {
        return weeksInterval;
    }

    public void setWeeksInterval(int weeksInterval) {
        this.weeksInterval = weeksInterval;
    }

    public Date getTriggerStartDateTime() {
        return triggerStartDateTime;
    }

    public void setTriggerStartDateTime(Date triggerStartDateTime) {
        this.triggerStartDateTime = triggerStartDateTime;
    }

    public void setWeeksOfMonth(int weekOfMonth) {
        weeksOfTheMonthList.clear();
        List<Integer> weeksOfMonthList = MathUtil.splitByPow2(weekOfMonth);
        for (int weeksOfMonth : weeksOfMonthList) {
            weeksOfTheMonthList.add(WeeksOfTheMonth.fromValue(weeksOfMonth));
        }
    }

    public List<WeeksOfTheMonth> getWeeksOfTheMonthList() {
        return weeksOfTheMonthList;
    }

    public void setDaysOfWeek(int dayOfWeek) {
        daysOfTheWeekList.clear();
        List<Integer> daysOfWeekList = MathUtil.splitByPow2(dayOfWeek);
        for (int daysOfWeek : daysOfWeekList) {
            daysOfTheWeekList.add(DaysOfTheWeek.fromValue(daysOfWeek));
        }
    }

    public List<DaysOfTheWeek> getDaysOfTheWeekList() {
        return daysOfTheWeekList;
    }

    public int getNextExecutedDateTimeForDayOfWeek(int dayOfWeek) {
        Integer nextExecuteDateTime = nextExecuteDateTimeForDayOfWeekMap.get(dayOfWeek);
        return nextExecuteDateTime == null ? -1 : nextExecuteDateTime;
    }

    public void putNextExecuteDateTimeForDayOfWeek(int dayOfWeek, int nextExecuteDateTime) {
        nextExecuteDateTimeForDayOfWeekMap.put(dayOfWeek, nextExecuteDateTime);
    }

    public Map<Integer, Integer> getNextExecuteDateTimeForDayOfWeekMap() {
        return nextExecuteDateTimeForDayOfWeekMap;
    }

    public Date getNextQuartzFireDateTime() {
        return nextQuartzFireDateTime;
    }

    public void setNextQuartzFireDateTime(Date nextQuartzFireDateTime) {
        this.nextQuartzFireDateTime = nextQuartzFireDateTime;
    }

    public int getHourInterval() {
        return hourInterval;
    }

    public void setHourInterval(int hourInterval) {
        this.hourInterval = hourInterval;
    }

    public Calendar getNextExecutedDateTime() {
        return nextExecutedDateTime;
    }

    public void setNextExecutedDateTime(Calendar nextExecutedDateTime) {
        this.nextExecutedDateTime = nextExecutedDateTime;
        // 如果有用到WeeklyPolicy和MonthDayOfWeekPolicy, 這裡需要將triggerStartDateTime的時分秒塞入
        // if ((WeeklyPolicy.class.getName().equals(this.policyClassname) || MonthDayOfWeekPolicy.class.getName().equals(this.policyClassname))
        //         && this.triggerStartDateTime != null) {
        //     Calendar calendar = CalendarUtil.clone(this.triggerStartDateTime);
        //     if (CalendarUtil.timeValueInHourMinuteSecond(calendar) != CalendarUtil.timeValueInHourMinuteSecond(this.nextExecutedDateTime)) {
        //         this.nextExecutedDateTime.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        //         this.nextExecutedDateTime.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        //         this.nextExecutedDateTime.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
        //         this.nextExecutedDateTime.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND));
        //     }
        // }
    }

    public Calendar getLatestExecutedDateTime() {
        return latestExecutedDateTime;
    }

    public void setLatestExecutedDateTime(Calendar latestExecutedDateTime) {
        this.latestExecutedDateTime = latestExecutedDateTime;
    }

    public int getMinuteInterval() {
        return minuteInterval;
    }

    public void setMinuteInterval(int minuteInterval) {
        this.minuteInterval = minuteInterval;
    }

    public List<Integer> getDaysOfTheMonthList() {
        return daysOfTheMonthList;
    }

    public void setDaysOfMonth(String daysOfMonth) {
        daysOfTheMonthList.clear();
        if (StringUtils.isNotBlank(daysOfMonth)) {
            for (String day : daysOfMonth.split(",")) {
                daysOfTheMonthList.add(Integer.parseInt(day));
            }
        }
    }

    public List<MonthsOfTheYear> getMonthsOfTheYearList() {
        return monthsOfTheYearList;
    }

    public void setMonthsOfYear(int monthsOfYear) {
        monthsOfTheYearList.clear();
        List<Integer> monthOfYearList = MathUtil.splitByPow2(monthsOfYear);
        for (int monthOfYear : monthOfYearList) {
            monthsOfTheYearList.add(MonthsOfTheYear.fromValue(monthOfYear));
        }
    }

    public boolean isRunOnLastWeekOfMonth() {
        return runOnLastWeekOfMonth;
    }

    public void setRunOnLastWeekOfMonth(boolean runOnLastWeekOfMonth) {
        this.runOnLastWeekOfMonth = runOnLastWeekOfMonth;
    }

    public boolean isRunOnLastDayOfMonth() {
        return runOnLastDayOfMonth;
    }

    public void setRunOnLastDayOfMonth(boolean runOnLastDayOfMonth) {
        this.runOnLastDayOfMonth = runOnLastDayOfMonth;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BatchJobParameter other = (BatchJobParameter) that;
        return (this.getBatchId() == null ? other.getBatchId() == null : this.getBatchId().equals(other.getBatchId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBatchId() == null) ? 0 : getBatchId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static String getJobDataMapKey() {
        return BatchJobParameter.class.getSimpleName();
    }

    public String getLogContent() {
        return getLogContent(this.getBatchId(), this.getName(), this.getDescription());
    }

    public static String getLogContent(String batchId, String name, String description) {
        return StringUtils.join("[", batchId, "-", name, "(", description, ")]");
    }

    public String getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(String scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    public int getLatestNextRunTimeForDayOfWeek() {
        if (nextExecuteDateTimeForDayOfWeekMap.size() == 0) return -1;
        int date = nextExecuteDateTimeForDayOfWeekMap.values().stream().min(Integer::compare).get();
        return date;
    }
}
