package com.syscom.fep.batch.base.library;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.configurer.BatchBaseConfiguration;
import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.enums.ScheduleType;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.delegate.ActionListener;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.jms.JmsKind;
import com.syscom.fep.frmcommon.jms.JmsPayloadOperator;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.instance.batch.BatchQueueOperator;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostConfigurationProperties;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostConstant;
import com.syscom.fep.jms.instance.batch.hosts.BatchQueueHostOperator;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class BatchJobLibrary extends FEPBase {
    private static final LogHelper log = LogHelperFactory.getBatchLogger();
    private final FEPBatch jobData = new FEPBatch();
    private String _logPath;
    private String _batchName;
    private String logFile;
    public static final String BATCH_INVOKER_NAME = "jobSchedulerInvoker";
    // 開始執行時間
    long startTimeMillis;
    private String message;
    private Map<String, String> arguments;
    private Task task;

    public BatchJobLibrary() {
        this(new LogData());
    }

    public BatchJobLibrary(LogData logData) {
        this.logContext = logData;
        if (this.logContext == null) {
            this.logContext = new LogData();
        }
    }

    /**
     * 本建構式專門給批次程式使用
     *
     * @param task
     * @param args
     * @param logPath
     */
    public BatchJobLibrary(Task task, String[] args, String logPath) {
        this.task = task;
        this.arguments = new HashMap<String, String>();
        this.setLogPath(logPath);
        this.extractBatchParameter(args);
        LogHelperFactory.getTraceLogger().trace("Get batchName after extract parameter, _batchName = [", this._batchName, "]");
        List<String> argsList = new ArrayList<>();
        for (Entry<String, String> entry : this.arguments.entrySet()) {
            argsList.add(StringUtils.join("/", entry.getKey(), ":", entry.getValue()));
        }
        this.writeLog("Batch Parameters = [", StringUtils.join(argsList, StringUtils.SPACE), "], LogPath = [", this._logPath, "]");
        this.startTimeMillis = System.currentTimeMillis();
    }

    private void extractBatchParameter(String[] args) {
        for (int i = 0; i < args.length; i++) {
            LogHelperFactory.getTraceLogger().trace("BatchJobLibrary extract parameter = [", args[i], "]");
            String[] arg = args[i].split(":", 2);
            if (StringUtils.startsWithIgnoreCase(arg[0], "/instanceid")) {
                this.jobData.getTaskParameters().setInstanceId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/hostname")) {
                this.jobData.getTaskParameters().setHostName(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/batchid")) {
                this.jobData.getTaskParameters().setBatchId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/batchname")) {
                this._batchName = arg[1];
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/jobid")) {
                this.jobData.getTaskParameters().setJobId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/taskid")) {
                this.jobData.getTaskParameters().setTaskId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/stepid")) {
                this.jobData.getTaskParameters().setStepId(arg[1]);
            } else if (StringUtils.startsWithIgnoreCase(arg[0], "/BatchLogPath")) {
                this.setLogPath(arg[1]);
            } else {
                if (arg.length > 1) {
                    if (StringUtils.isNotBlank(arg[0]))
                        this.arguments.put(arg[0].substring(1), arg[1]);
                } else {
                    if (StringUtils.isNotBlank(arg[0]))
                        this.arguments.put(arg[0].substring(1), StringUtils.EMPTY);
                }
            }
        }
    }

    private void setLogPath(String logPath) {
        this._logPath = logPath;
        this.arguments.put("BatchLogPath", this._logPath);
    }

    public String startBatch(String hostName, String batchId, String jobId) throws Exception {
        return this.startBatch(hostName, batchId, jobId, StringUtils.EMPTY);
    }

    public String startBatch(String hostName, String batchId, String jobId, String customParameters) throws Exception {
        return this.startBatch(hostName, batchId, jobId, customParameters, 0);
    }

    public String startBatch(String hostName, String batchId, String jobId, int sleepInMillisecondsAfterStart) throws Exception {
        return this.startBatch(hostName, batchId, jobId, StringUtils.EMPTY, sleepInMillisecondsAfterStart);
    }

    public String startBatch(String hostName, String batchId, String jobId, String customParameters, int sleepInMillisecondsAfterStart) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        jobData.getTaskParameters().setJobId(jobId);
        jobData.getTaskParameters().setStepId("1");
        jobData.getTaskParameters().setInstanceId(UUID.randomUUID().toString());
        jobData.getTaskParameters().setState(String.valueOf(JobState.Start.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        jobData.getTaskParameters().setCustomParameters(customParameters);
        sendBatchQueue(hostName);
        if (sleepInMillisecondsAfterStart > 0) {
            // 2025-02-25 Richard modified for [DoS by Sleep]
            // try {
            //     Thread.sleep(sleepInMillisecondsAfterStart);
            // } catch (InterruptedException e) {
            //     com.syscom.fep.frmcommon.log.LogHelper.getAdditionalLogger().error(e, e.getMessage());
            // }
            ReflectUtil.envokeStaticMethod(Thread.class, "sleep", new Class[] {long.class}, new Object[] {sleepInMillisecondsAfterStart});
        }
        // 2025-12-19 Richard add 返回instanceId
        return jobData.getTaskParameters().getInstanceId();
    }

    public void stopBatch() throws Exception {
        jobData.getTaskParameters().setBatchId(jobData.getTaskParameters().getBatchId());
        jobData.getTaskParameters().setInstanceId(jobData.getTaskParameters().getInstanceId());
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Successful.getValue()));
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    public void rerunBatch(String hostName, String instanceId, String batchId, String jobId) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        jobData.getTaskParameters().setJobId(jobId);
        jobData.getTaskParameters().setStepId("1");
        jobData.getTaskParameters().setInstanceId(instanceId);
        jobData.getTaskParameters().setState(String.valueOf(JobState.Start.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        sendBatchQueue(hostName);
    }

    public void stopJob() {}

    /**
     * 開始批次工作
     *
     * @throws Exception
     */
    public void startTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Running.getValue()));
        jobData.getTaskParameters().setResult(String.valueOf(BatchResult.Running.getValue()));
        jobData.getTaskParameters().setLogFile(logFile);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為失敗,批次將會中止
     *
     * @throws Exception
     */
    public void abortTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Abort.getValue()));
        jobData.getTaskParameters().setMessage(this.message);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為失敗,但批次仍會繼續下一個Task
     *
     * @throws Exception
     */
    public void failedTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.Failed.getValue()));
        jobData.getTaskParameters().setMessage(this.message);
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    /**
     * 回報批次工作為成功結束
     *
     * @throws Exception
     */
    public void endTask() throws Exception {
        jobData.getTaskParameters().setState(String.valueOf(JobState.End.getValue()));
        sendBatchQueue(jobData.getTaskParameters().getHostName());
    }

    public void createDailyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                String startTime, short daysInterval, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(BATCH_INVOKER_NAME);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Daily.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getDailyTrigger().setDaysInterval(String.valueOf(daysInterval));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createDailyRepetitionTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                          String startTime, short daysInterval, boolean enable, String repetitionInterval, String repetitionDuration) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(BATCH_INVOKER_NAME);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.DailyRepetition.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getDailyTrigger().setDaysInterval(String.valueOf(daysInterval));
        jobData.getScheduleTask().getDailyTrigger().setRepetitionDuration(repetitionDuration);
        jobData.getScheduleTask().getDailyTrigger().setRepetitionInterval(repetitionInterval);
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createWeeklyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                 String startTime, int daysOfWeek, int weeksInterval, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(BATCH_INVOKER_NAME);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Weekly.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getWeeklyTrigger().setDaysOfWeek(String.valueOf(daysOfWeek));
        jobData.getScheduleTask().getWeeklyTrigger().setWeeksInterval(String.valueOf(weeksInterval));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createMonthlyTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                  String startTime, String daysOfMonth, int monthsOfYear, boolean runOnLastDayOfMonth, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(BATCH_INVOKER_NAME);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.Monthly.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getMonthlyTrigger().setDaysOfMonth(daysOfMonth);
        jobData.getScheduleTask().getMonthlyTrigger().setMonthsOfYear(String.valueOf(monthsOfYear));
        jobData.getScheduleTask().getMonthlyTrigger().setRunOnLastDayOfMonth(String.valueOf(runOnLastDayOfMonth));
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        sendBatchQueue(hostName);
    }

    public void createMonthlyDayOfWeekTask(String hostName, String batchId, String batchName, String jobId, String taskDescription, String action, String actionArguments,
                                           String startTime, int daysOfWeek, int monthsOfYear, int weeksOfMonth, boolean runOnLastWeekOfMonth, boolean enable) throws Exception {
        this.deleteBatchOnOthersHost(hostName, batchId, batchName);
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setTaskDescription(taskDescription);
        jobData.getScheduleTask().setAction(BATCH_INVOKER_NAME);
        jobData.getScheduleTask().setActionArguments(StringUtils.join("/batchid:", batchId, " /jobid:", jobId, StringUtils.SPACE, actionArguments));
        jobData.getScheduleTask().setScheduleType(ScheduleType.MonthDayOfWeek.toString());
        jobData.getScheduleTask().setStartTime(startTime);
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setDaysOfWeek(String.valueOf(daysOfWeek));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setMonthsOfYear(String.valueOf(monthsOfYear));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setWeeksOfMonth(String.valueOf(weeksOfMonth));
        jobData.getScheduleTask().getMonthlyDayOfWeekTrigger().setRunOnLastWeekOfMonth(String.valueOf(runOnLastWeekOfMonth));
        jobData.getScheduleTask().setDelete(Boolean.FALSE.toString());
        jobData.getScheduleTask().setEnable(String.valueOf(enable));
        sendBatchQueue(hostName);
    }

    /**
     * 在create Batch排程之前, 先要通知其他主機刪除有可能存在的Batch排程
     * 這個method先暫時mark掉留著
     *
     * @param hostName
     * @param batchId
     * @param batchName
     */
    private void deleteBatchOnOthersHost(String hostName, String batchId, String batchName) {
        // if (StringUtils.isBlank(hostName)) return;
        // FEPBatch jobData = new FEPBatch();
        // jobData.getTaskParameters().setBatchId(batchId);
        // // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        // jobData.getScheduleTask().setTaskName(batchName);
        // jobData.getScheduleTask().setDelete(Boolean.TRUE.toString());
        // Map<String, BatchQueueHostOperator> map = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_OPERATOR_MAP);
        // JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        // PlainTextMessage payload = new PlainTextMessage(JmsKind.QUEUE, configuration.getQueueNames().getBatch().getDestination(), StringUtils.EMPTY);
        // for (Entry<String, BatchQueueHostOperator> entry : map.entrySet()) {
        //     if (entry.getKey().equals(hostName)) continue;
        //     jobData.getTaskParameters().setHostName(entry.getKey());
        //     payload.setPayload(serializeToXml(jobData));
        //     BatchQueueHostOperator operator = entry.getValue();
        //     try {
        //         operator.sendQueue(payload, null, null);
        //         this.logContext.setProgramName(StringUtils.join(ProgramName, ".deleteTaskBeforeCreate"));
        //         this.logContext.setRemark(StringUtils.join("sendQueue to delete batch successful before create, hostName = [", hostName, "], batchId = [", batchId, "], batchName = [", batchName, "]!!!"));
        //         this.logMessage(this.logContext);
        //     } catch (Exception e) {
        //         this.logContext.setProgramName(StringUtils.join(ProgramName, ".deleteTaskBeforeCreate"));
        //         this.logContext.setRemark(StringUtils.join("sendQueue to delete batch failed before create, hostName = [", hostName, "], batchId = [", batchId, "], batchName = [", batchName, "]!!!"));
        //         this.logContext.setProgramException(e);
        //         sendEMS(this.logContext);
        //     }
        // }
    }

    public void deleteTask(String hostName, String batchId, String batchName, boolean forceDelete) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        jobData.getTaskParameters().setBatchId(batchId);
        // jobData.getScheduleTask().setTaskName(StringUtils.join("[", batchId, "-", batchName, "]"));
        jobData.getScheduleTask().setTaskName(batchName);
        jobData.getScheduleTask().setDelete(Boolean.TRUE.toString());
        jobData.getScheduleTask().setForceDelete(Boolean.toString(forceDelete));
        sendBatchQueue(hostName);
    }

    public void writeLog(Object... logContent) {
        this.writeLog(message -> {
            log.info(message);
        }, logContent);
    }

    public void writeErrorLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.error(t, message);
        }, logContent);
    }

    public void writeDebugLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.debug(t, message);
        }, logContent);
    }

    public void writeWarnLog(Throwable t, Object... logContent) {
        this.writeLog(message -> {
            log.warn(t, message);
        }, logContent);
    }

    private void writeLog(ActionListener<String> listener, Object... logContent) {
        if (StringUtils.isBlank(this._batchName)) {
            this._batchName = this.task.getClass().getSimpleName();
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(this._logPath)) {
            sb.append(this._logPath).append("/");
        } else {
            sb.append("logs/");
        }
        sb.append(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH))
                .append("/fep-batch-task/")
                .append(this._batchName);
        if (jobData.getTaskParameters() != null) {
            if (StringUtils.isNotBlank(jobData.getTaskParameters().getInstanceId()))
                sb.append("_").append(jobData.getTaskParameters().getInstanceId());
            if (StringUtils.isNotBlank(jobData.getTaskParameters().getStepId()))
                sb.append("_").append(jobData.getTaskParameters().getStepId());
        }
        sb.append(".log");
        this.logFile = new File(CleanPathUtil.cleanString(sb.toString())).getAbsolutePath();
        LogMDC.put(Const.MDC_BATCHJOB, StringUtils.replace(logFile, "/", "-"));
        LogMDC.put(Const.MDC_BATCHJOB_FILENAME, logFile);
        listener.actionPerformed(StringUtils.join(logContent));
        clearMDC();
    }

    public boolean isBsDay(String zone) {
        return this.isBsDay(zone, Integer.toString(CalendarUtil.dateValue(Calendar.getInstance())));
    }

    public boolean isBsDay(String zone, String bsday) {
        BsdaysExtMapper mapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
        try {
            Bsdays record = mapper.selectByPrimaryKey(zone, bsday);
            if (record != null) {
                return DbHelper.toBoolean(record.getBsdaysWorkday());
            } else {
                return false;
            }
        } catch (Exception e) {
            this.writeErrorLog(e, "BatchJobLibrary判斷營業日發生例外:", e.getMessage());
            return false;
        }
    }

    /**
     * 通知分行系統
     *
     * @param userId     要通知的櫃員編號
     * @param fileName   已完成的批號
     * @param systemName 系統名稱，請先填FCS，若確定要做再請分行增加FEP
     * @return
     */
    public boolean notifyBranch(String userId, String fileName, String systemName) {
        // TODO
        throw ExceptionUtil.createNotImplementedException("Not finished yet");
    }

    /**
     * 丟訊息到Batch Queue中
     *
     * @param hostName
     * @throws Exception
     */
    private void sendBatchQueue(String hostName) throws Exception {
        if (!isCallBatchJob()) return;
        JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
        // 預設Destination取JmsMsgConfiguration中的配置
        PlainTextMessage payload = new PlainTextMessage(JmsKind.QUEUE, configuration.getQueueNames().getBatch().getDestination(), StringUtils.EMPTY);
        boolean send = false;
        if (SpringBeanFactoryUtil.isBeanExist(BatchQueueHostConstant.JMS_OPERATOR_MAP)) {
            Map<String, BatchQueueHostOperator> hostNameToOperatorMap = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_OPERATOR_MAP);
            Map<String, BatchQueueHostConfigurationProperties> hostNameToPropertiesMap = SpringBeanFactoryUtil.getBean(BatchQueueHostConstant.JMS_PROPERTIES_MAP);
            if (hostNameToOperatorMap != null) {
                // 如果hostName沒有指定, 則所有的hostName都要丟
                if (StringUtils.isBlank(hostName)) {
                    Exception thrown = null;
                    for (Entry<String, BatchQueueHostOperator> entry : hostNameToOperatorMap.entrySet()) {
                        // 這裡要clone一份, 改變裡面的hostName再送出去
                        FEPBatch clone = new FEPBatch();
                        BeanUtils.copyProperties(this.jobData.getScheduleTask(), clone.getScheduleTask());
                        BeanUtils.copyProperties(this.jobData.getTaskParameters(), clone.getTaskParameters());
                        // 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
                        this.setDestination(payload, hostNameToPropertiesMap, entry.getKey());
                        try {
                            // sendBatchQueue
                            this.sendBatchQueue(entry.getValue(), entry.getKey(), clone, payload);
                        } catch (Exception e) {
                            thrown = e;
                        }
                    }
                    if (thrown != null) {
                        throw thrown;
                    }
                    send = true;
                } else {
                    // 丟訊息到指定的hostName所在的Queue上
                    BatchQueueHostOperator operator = hostNameToOperatorMap.get(hostName);
                    if (operator != null) {
                        // 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
                        this.setDestination(payload, hostNameToPropertiesMap, hostName);
                        // sendBatchQueue
                        this.sendBatchQueue(operator, hostName, this.jobData, payload);
                        send = true;
                    } else {
                        Exception e = ExceptionUtil.createException("Cannot send Batch Queue cause Incorrect Host Name, hostName:", hostName);
                        this.logContext.setSubSys(SubSystem.CMN);
                        this.logContext.setChannel(FEPChannel.BATCH);
                        this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendBatchQueue"));
                        this.logContext.setProgramException(e);
                        this.logContext.setRemark(StringUtils.join(e.getMessage(), ", batchName:", this._batchName));
                        sendEMS(this.logContext);
                        throw e;
                    }
                }
            }
        }
        // 如果上面都沒有發送成功, 則預設執行本機的批次
        if (!send) {
            BatchQueueOperator batchQueueOperator = SpringBeanFactoryUtil.getBean(BatchQueueOperator.class);
            this.sendBatchQueue(batchQueueOperator, FEPConfig.getInstance().getHostName(), this.jobData, payload);
        }
    }

    /**
     * 根據Batch Host配置檔自定義的Batch的Queue Name塞入Destination
     *
     * @param payload
     * @param hostNameToPropertiesMap
     * @param hostName
     */
    private void setDestination(PlainTextMessage payload, Map<String, BatchQueueHostConfigurationProperties> hostNameToPropertiesMap, String hostName) {
        if (hostNameToPropertiesMap != null) {
            BatchQueueHostConfigurationProperties properties = hostNameToPropertiesMap.get(hostName);
            if (properties != null && properties.getQueueNames() != null && properties.getQueueNames().getBatch() != null && StringUtils.isNotBlank(properties.getQueueNames().getBatch().getDestination())) {
                payload.setDestination(properties.getQueueNames().getBatch().getDestination());
            }
        }
    }

    private void sendBatchQueue(JmsPayloadOperator operator, String hostName, FEPBatch jobData, PlainTextMessage payload) throws Exception {
        jobData.getTaskParameters().setHostName(hostName);
        // 如果執行批次的參數中沒有/hostName, 則補上
        if (StringUtils.isBlank(jobData.getScheduleTask().getActionArguments())) {
            jobData.getScheduleTask().setActionArguments(StringUtils.join("/hostName:", hostName));
        } else if (!jobData.getScheduleTask().getActionArguments().contains("/hostName:")) {
            jobData.getScheduleTask().setActionArguments(StringUtils.join("/hostName:", hostName, StringUtils.SPACE, jobData.getScheduleTask().getActionArguments()));
        }
        payload.setPayload(serializeToXml(jobData));
        try {
            operator.sendQueue(payload, null, null);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendBatchQueue"));
            this.logContext.setRemark(StringUtils.join("sendBatchQueue successful",
                    " hostName = [", hostName, "],",
                    " queueName = [", payload.getDestination(), "],",
                    " batchId = [", jobData.getTaskParameters().getBatchId(), "],",
                    " batchName = [", this._batchName, "]!!!"));
            this.logContext.setMessage(jobData.getTaskParameters().getInstanceId());
            this.logMessage(this.logContext);
        } catch (Exception e) {
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendBatchQueue"));
            this.logContext.setRemark(StringUtils.join(
                    "sendBatchQueue failed,",
                    " hostName = [", hostName, "],",
                    " queueName = [", payload.getDestination(), "],",
                    " batchId = [", jobData.getTaskParameters().getBatchId(), "],",
                    " batchName = [", this._batchName, "]!!!"));
            this.logContext.setProgramException(e);
            this.logContext.setMessage(jobData.getTaskParameters().getInstanceId());
            sendEMS(this.logContext);
            throw e;
        }
    }

    /**
     * 獲取Task實例
     *
     * @param programName
     * @return
     * @throws Exception
     */
    public Task getBatchTask(String programName) throws Exception {
        // 先嘗試用內部引用方式獲取
        try {
            // 2024-09-12 Richard modified start for 【Unsafe Reflection】
            // Class<?> taskClazz = Class.forName(programName);
            Class<?> taskClazz = ReflectUtil.toClazz(programName, false);
            if (taskClazz == null)
                throw ExceptionUtil.createClassNotFoundException("class not found by name [", programName, "]");
            // 2024-09-12 Richard modified start end 【Unsafe Reflection】
            return (Task) taskClazz.newInstance();
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().trace("standalone環境下找不到是正常的, ", e.getMessage());
        }
        // 如果取不到, 則改用外部jar檔獲取
        BatchBaseConfiguration configuration = SpringBeanFactoryUtil.getBean(BatchBaseConfiguration.class);
        Object jarPath = ESAPIUtil.toFile(CleanPathUtil.cleanString(configuration.getTask().getPath()),
                FormatUtil.messageFormat(configuration.getTask().getJarNameTemplate(), programName.substring(programName.lastIndexOf(".") + 1)));
        return ReflectUtil.dynamicLoadClass(((File) jarPath).getAbsolutePath(), programName);
    }

    public void dispose() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public boolean isCallBatchJob() {
        BatchBaseConfiguration batchBaseConfiguration = SpringBeanFactoryUtil.getBean(BatchBaseConfiguration.class, false);
        if (batchBaseConfiguration != null) {
            return batchBaseConfiguration.isCallBatchJob();
        }
        if (MapUtils.isNotEmpty(this.arguments) && this.arguments.containsKey("CallBatchJob")) {
            return Boolean.parseBoolean(this.arguments.get("CallBatchJob"));
        }
        return true;
    }

    /**
     * 尋找傳入的變數
     *
     * @param args
     * @param found
     * @param defaultValue
     * @return
     */
    public static String findArg(String[] args, String found, String defaultValue) {
        if (args == null) return defaultValue;
        try {
            for (String arg : args) {
                if (arg.startsWith("/" + found)) {
                    int index = arg.indexOf(":");
                    if (index != -1) {
                        String value = arg.substring(index + 1).trim();
                        if (value.isEmpty())
                            return defaultValue;
                        return value;
                    }
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LogHelperFactory.getTraceLogger().warn("find arg \"" + found + "\" failed, ", e.getMessage());
        }
        return defaultValue;
    }
}
