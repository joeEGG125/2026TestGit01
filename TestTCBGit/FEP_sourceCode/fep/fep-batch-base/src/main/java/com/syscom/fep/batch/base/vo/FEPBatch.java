package com.syscom.fep.batch.base.vo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("FEPBatch")
public class FEPBatch {
    @XStreamAlias("TaskParameters")
    private FEPBatchTaskParameters taskParameters;
    @XStreamAlias("ScheduleTask")
    private FEPBatchScheduleTask scheduleTask;

    public FEPBatch() {
        taskParameters = new FEPBatchTaskParameters();
        scheduleTask = new FEPBatchScheduleTask();
    }

    public FEPBatchTaskParameters getTaskParameters() {
        return taskParameters;
    }

    public void setTaskParameters(FEPBatchTaskParameters taskParameters) {
        this.taskParameters = taskParameters;
    }

    public FEPBatchScheduleTask getScheduleTask() {
        return scheduleTask;
    }

    public void setScheduleTask(FEPBatchScheduleTask scheduleTask) {
        this.scheduleTask = scheduleTask;
    }

    @XStreamAlias("TaskParameters")
    public static class FEPBatchTaskParameters {
        @XStreamAsAttribute
        private String HostName;
        @XStreamAsAttribute()
        private String BatchId;
        @XStreamAsAttribute()
        private String JobId;
        @XStreamAsAttribute()
        private String TaskId;
        @XStreamAsAttribute()
        private String StepId;
        @XStreamAsAttribute()
        private String State;
        @XStreamAsAttribute()
        private String Result;
        @XStreamAsAttribute()
        private String InstanceId;
        @XStreamAsAttribute()
        private String Message;
        @XStreamAsAttribute()
        private String LogFile;
        @XStreamAsAttribute()
        private String CustomParameters;

        public String getHostName() {
            return HostName;
        }

        public void setHostName(String hostName) {
            HostName = hostName;
        }

        public String getBatchId() {
            return BatchId;
        }

        public void setBatchId(String batchId) {
            BatchId = batchId;
        }

        public String getJobId() {
            return JobId;
        }

        public void setJobId(String jobId) {
            JobId = jobId;
        }

        public String getTaskId() {
            return TaskId;
        }

        public void setTaskId(String taskId) {
            TaskId = taskId;
        }

        public String getStepId() {
            return StepId;
        }

        public void setStepId(String stepId) {
            StepId = stepId;
        }

        public String getState() {
            return State;
        }

        public void setState(String state) {
            State = state;
        }

        public String getResult() {
            return Result;
        }

        public void setResult(String result) {
            Result = result;
        }

        public String getInstanceId() {
            return InstanceId;
        }

        public void setInstanceId(String instanceId) {
            InstanceId = instanceId;
        }

        public String getMessage() {
            return Message;
        }

        public void setMessage(String message) {
            Message = message;
        }

        public String getLogFile() {
            return LogFile;
        }

        public void setLogFile(String logFile) {
            LogFile = logFile;
        }

        public String getCustomParameters() {
            return CustomParameters;
        }

        public void setCustomParameters(String customParameters) {
            CustomParameters = customParameters;
        }
    }

    @XStreamAlias("ScheduleTask")
    public static class FEPBatchScheduleTask {
        @XStreamAlias("DailyTrigger")
        private FEPBatchScheduleTaskDailyTrigger dailyTrigger;
        @XStreamAlias("WeeklyTrigger")
        private FEPBatchScheduleTaskWeeklyTrigger weeklyTrigger;
        @XStreamAlias("MonthlyTrigger")
        private FEPBatchScheduleTaskMonthlyTrigger monthlyTrigger;
        @XStreamAlias("MonthlyDayOfWeekTrigger")
        private FEPBatchScheduleTaskMonthlyDayOfWeekTrigger monthlyDayOfWeekTrigger;
        @XStreamAsAttribute()
        private String Enable;
        @XStreamAsAttribute()
        private String Delete;
        @XStreamAsAttribute()
        private String ScheduleType;
        @XStreamAsAttribute()
        private String StartTime;
        @XStreamAsAttribute()
        private String TaskName;
        @XStreamAsAttribute()
        private String TaskDescription;
        @XStreamAsAttribute()
        private String Action;
        @XStreamAsAttribute()
        private String ActionArguments;
        @XStreamAsAttribute()
        private String ForceDelete;

        public FEPBatchScheduleTask() {
            dailyTrigger = new FEPBatchScheduleTaskDailyTrigger();
            weeklyTrigger = new FEPBatchScheduleTaskWeeklyTrigger();
            monthlyTrigger = new FEPBatchScheduleTaskMonthlyTrigger();
            monthlyDayOfWeekTrigger = new FEPBatchScheduleTaskMonthlyDayOfWeekTrigger();
        }

        public FEPBatchScheduleTaskDailyTrigger getDailyTrigger() {
            return dailyTrigger;
        }

        public void setDailyTrigger(FEPBatchScheduleTaskDailyTrigger dailyTrigger) {
            this.dailyTrigger = dailyTrigger;
        }

        public FEPBatchScheduleTaskWeeklyTrigger getWeeklyTrigger() {
            return weeklyTrigger;
        }

        public void setWeeklyTrigger(FEPBatchScheduleTaskWeeklyTrigger weeklyTrigger) {
            this.weeklyTrigger = weeklyTrigger;
        }

        public FEPBatchScheduleTaskMonthlyTrigger getMonthlyTrigger() {
            return monthlyTrigger;
        }

        public void setMonthlyTrigger(FEPBatchScheduleTaskMonthlyTrigger monthlyTrigger) {
            this.monthlyTrigger = monthlyTrigger;
        }

        public FEPBatchScheduleTaskMonthlyDayOfWeekTrigger getMonthlyDayOfWeekTrigger() {
            return monthlyDayOfWeekTrigger;
        }

        public void setMonthlyDayOfWeekTrigger(FEPBatchScheduleTaskMonthlyDayOfWeekTrigger monthlyDayOfWeekTrigger) {
            this.monthlyDayOfWeekTrigger = monthlyDayOfWeekTrigger;
        }

        public String getEnable() {
            return Enable;
        }

        public void setEnable(String enable) {
            Enable = enable;
        }

        public String getDelete() {
            return Delete;
        }

        public void setDelete(String delete) {
            Delete = delete;
        }

        public String getScheduleType() {
            return ScheduleType;
        }

        public void setScheduleType(String scheduleType) {
            ScheduleType = scheduleType;
        }

        public String getStartTime() {
            return StartTime;
        }

        public void setStartTime(String startTime) {
            StartTime = startTime;
        }

        public String getTaskName() {
            return TaskName;
        }

        public void setTaskName(String taskName) {
            TaskName = taskName;
        }

        public String getTaskDescription() {
            return TaskDescription;
        }

        public void setTaskDescription(String taskDescription) {
            TaskDescription = taskDescription;
        }

        public String getAction() {
            return Action;
        }

        public void setAction(String action) {
            Action = action;
        }

        public String getActionArguments() {
            return ActionArguments;
        }

        public void setActionArguments(String actionArguments) {
            ActionArguments = actionArguments;
        }

        public String getForceDelete() {
            return ForceDelete;
        }

        public void setForceDelete(String forceDelete) {
            ForceDelete = forceDelete;
        }
    }

    @XStreamAlias("DailyTrigger")
    public static class FEPBatchScheduleTaskDailyTrigger {
        @XStreamAsAttribute()
        private String DaysInterval;
        @XStreamAsAttribute()
        private String RepetitionInterval;
        @XStreamAsAttribute()
        private String RepetitionDuration;

        public String getDaysInterval() {
            return DaysInterval;
        }

        public void setDaysInterval(String daysInterval) {
            DaysInterval = daysInterval;
        }

        public String getRepetitionInterval() {
            return RepetitionInterval;
        }

        public void setRepetitionInterval(String repetitionInterval) {
            RepetitionInterval = repetitionInterval;
        }

        public String getRepetitionDuration() {
            return RepetitionDuration;
        }

        public void setRepetitionDuration(String repetitionDuration) {
            RepetitionDuration = repetitionDuration;
        }
    }

    @XStreamAlias("WeeklyTrigger")
    public static class FEPBatchScheduleTaskWeeklyTrigger {
        @XStreamAsAttribute()
        private String DaysOfWeek;
        @XStreamAsAttribute()
        private String WeeksInterval;

        public String getDaysOfWeek() {
            return DaysOfWeek;
        }

        public void setDaysOfWeek(String daysOfWeek) {
            DaysOfWeek = daysOfWeek;
        }

        public String getWeeksInterval() {
            return WeeksInterval;
        }

        public void setWeeksInterval(String weeksInterval) {
            WeeksInterval = weeksInterval;
        }
    }

    @XStreamAlias("MonthlyTrigger")
    public static class FEPBatchScheduleTaskMonthlyTrigger {
        @XStreamAsAttribute()
        private String DaysOfMonth;
        @XStreamAsAttribute()
        private String MonthsOfYear;
        @XStreamAsAttribute()
        private String RunOnLastDayOfMonth;

        public String getDaysOfMonth() {
            return DaysOfMonth;
        }

        public void setDaysOfMonth(String daysOfMonth) {
            DaysOfMonth = daysOfMonth;
        }

        public String getMonthsOfYear() {
            return MonthsOfYear;
        }

        public void setMonthsOfYear(String monthsOfYear) {
            MonthsOfYear = monthsOfYear;
        }

        public String getRunOnLastDayOfMonth() {
            return RunOnLastDayOfMonth;
        }

        public void setRunOnLastDayOfMonth(String runOnLastDayOfMonth) {
            RunOnLastDayOfMonth = runOnLastDayOfMonth;
        }
    }

    @XStreamAlias("MonthlyDayOfWeekTrigger")
    public static class FEPBatchScheduleTaskMonthlyDayOfWeekTrigger {
        @XStreamAsAttribute()
        private String DaysOfWeek;
        @XStreamAsAttribute()
        private String MonthsOfYear;
        @XStreamAsAttribute()
        private String WeeksOfMonth;
        @XStreamAsAttribute()
        private String RunOnLastWeekOfMonth;

        public String getDaysOfWeek() {
            return DaysOfWeek;
        }

        public void setDaysOfWeek(String daysOfWeek) {
            DaysOfWeek = daysOfWeek;
        }

        public String getMonthsOfYear() {
            return MonthsOfYear;
        }

        public void setMonthsOfYear(String monthsOfYear) {
            MonthsOfYear = monthsOfYear;
        }

        public String getWeeksOfMonth() {
            return WeeksOfMonth;
        }

        public void setWeeksOfMonth(String weeksOfMonth) {
            WeeksOfMonth = weeksOfMonth;
        }

        public String getRunOnLastWeekOfMonth() {
            return RunOnLastWeekOfMonth;
        }

        public void setRunOnLastWeekOfMonth(String runOnLastWeekOfMonth) {
            RunOnLastWeekOfMonth = runOnLastWeekOfMonth;
        }
    }
}
