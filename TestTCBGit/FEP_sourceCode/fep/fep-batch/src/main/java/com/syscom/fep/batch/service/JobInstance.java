package com.syscom.fep.batch.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.mybatis.vo.JobsContinueOnFail;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.vo.FEPBatch;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.slf4j.event.Level;

public class JobInstance {
    private List<Map<String, Object>> jobData;
    private final Map<String, Boolean> doneFlag = new HashMap<>();
    private final Map<Integer, JobState> eachJobResult = new HashMap<>();
    private final BatchJobManager jobManager;
    private final JobHelper jobHelper;
    private String batchName;
    private Calendar batchStartTime;
    private final String jobInstanceId;
    private int currentBatch;
    private int currentJob;
    private int currentTask;
    private int currentStep;
    private int currentJobStep;
    private String currentJobLog;
    private JobState currentState;
    private BatchResult currentResult;
    private String currentMessage;
    private long historySeq;
    private int notifyType;
    private String notifyMail;
    private String notifyPhone;
    private String customParameters;
    private String hostName;

    public JobInstance(FEPBatch jobInfo) {
        this.jobManager = SpringBeanFactoryUtil.getBean(BatchJobManager.class);
        this.jobHelper = SpringBeanFactoryUtil.getBean(JobHelper.class);
        this.jobInstanceId = jobInfo.getTaskParameters().getInstanceId();
        this.currentState = JobState.parse(jobInfo.getTaskParameters().getState());
        this.currentBatch = Integer.parseInt(jobInfo.getTaskParameters().getBatchId());
        this.currentStep = Integer.parseInt(jobInfo.getTaskParameters().getStepId());
        this.currentJob = Integer.parseInt(jobInfo.getTaskParameters().getJobId());
        this.currentJobLog = jobInfo.getTaskParameters().getLogFile();
        this.hostName = jobInfo.getTaskParameters().getHostName();
    }

    /**
     * 本方法為Thread safe方法,同一時間只允許一個Thread進入
     *
     * @param logData
     */
    public synchronized void run(LogData logData) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        BatchConfiguration configuration = SpringBeanFactoryUtil.getBean(BatchConfiguration.class);
        List<Map<String, Object>> dtJob;
        jobHelper.log(logData, "JobInstance.Run", this.jobInstanceId,
                StringUtils.join("Begin to run job BatchId:", this.currentBatch, ",JobId:", this.currentJob, ",JobState:", this.currentState));
        switch (this.currentState) {
            case Start:
                // 2025-12-22 Richard add 透過Quartz取出BatchJobContext, 但是要注意, 只有排程的批次才會取到這個物件
                BatchJobContext batchJobContext = jobManager.getBatchJobContext(Integer.toString(this.currentBatch));
                // 設定logRemark用於後續列印log, 對於排程類的批次, 則塞入設定排程時的相關信息內容
                String logRemark = StringUtils.join(", Current BatchId:", this.currentBatch);
                if (batchJobContext != null) {
                    logRemark = StringUtils.join(", ", batchJobContext.getScheduleInfo());
                }
                // 20151027update:先查出Batch資料,檢查是否每天只能做一次再決定要不要往下做
                // 取得batch所有job task相關資料
                this.jobData = jobHelper.getBatchContext(logData, this.currentBatch, 0, 0, 0);
                if (CollectionUtils.isNotEmpty(this.jobData)) {
                    // 2025-11-11 收到批次啟動時, 先更新NEXTRUNTIME by Ashiang
                    jobHelper.updateBatchNextRunTime(logData, this.currentBatch, this.jobInstanceId);
                    // 取出批次的資料
                    Map<String, Object> batch = this.jobData.get(0);
                    // 批次平台在接收批次啟動要求時,如果BATCH_EXECUTE_HOST_NAME欄位是空值(不指定), 或符合本機的計算機名稱, 才執行此批次
                    Object hostName = batch.get("BATCH_EXECUTE_HOST_NAME");
                    if (hostName != null && StringUtils.isNotBlank(hostName.toString()) && !FEPConfig.getInstance().getHostName().equals(hostName.toString())) {
                        jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                StringUtils.join("JobInstance Cancel run Batch because Batch Execute HostName Inconsistently", logRemark));
                        return;
                    }
                    // 檢核可執行的系統別
                    Object batchSubsys = batch.get("BATCH_SUBSYS");
                    if (batchSubsys != null && StringUtils.isNotBlank(batchSubsys.toString()) && !configuration.getSubSysList().contains(batchSubsys.toString())) {
                        jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                StringUtils.join(StringUtils.join("JobInstance Cancel run Batch because Subsys is not allowed", logRemark)));
                        return;
                    }
                    // 檢核批次是否啟用
                    Object batchEnable = batch.get("BATCH_ENABLE");
                    if (batchEnable == null || !DbHelper.toBoolean(((Integer) batchEnable).shortValue())) {
                        jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                StringUtils.join("JobInstance Cancel run Batch because Batch is disabled", logRemark));
                        return;
                    }
                    // 檢核是否營業日才執行
                    Object batchCheckBusinessDate = batch.get("BATCH_CHECKBUSINESSDATE");
                    if (batchCheckBusinessDate != null && DbHelper.toBoolean(((Integer) batchCheckBusinessDate).shortValue())) {
                        if (!jobHelper.checkBusinessDate(logData, batch.get("BATCH_ZONE").toString(), batch.get("BATCH_NAME").toString(), DbHelper.getMapValue(batch, "BATCH_BATCHID", -1), this.jobInstanceId, batch.get("BATCH_NOTIFYMAIL").toString())) {
                            jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                    StringUtils.join("JobInstance Cancel Run by CheckBusinessDay fail", logRemark));
                            return;
                        }
                    }
                    // 檢核是否允許重覆執行
                    Object batchDenyconcurrentexec = batch.get("BATCH_DENYCONCURRENTEXEC");
                    if (batchDenyconcurrentexec != null && DbHelper.toBoolean(((Integer) batchDenyconcurrentexec).shortValue())) {
                        String batchResult = (String) batch.get("BATCH_RESULT");
                        // 判斷BatchResult若仍為執行中則不執行
                        if ("0".equals(batchResult)) {
                            jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                    StringUtils.join("JobInstance Cancel Run because deny concurrent execute", logRemark));
                            return;
                        }
                    }
                    // 一天只能做一次且今天已做過則不做直接離開
                    Object batchSingleTime = batch.get("BATCH_SINGLETIME");
                    if (batchSingleTime != null && DbHelper.toBoolean(((Integer) batchSingleTime).shortValue())) {
                        Object batchLastRunTime = batch.get("BATCH_LASTRUNTIME");
                        if (batchLastRunTime != null && CalendarUtil.equals((Date) batchLastRunTime, Calendar.getInstance().getTime(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)) {
                            jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId,
                                    StringUtils.join("JobInstance Cancel run Batch because already run once", logRemark));
                            return;
                        }
                    }
                }
                jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId, StringUtils.join("JobInstance start to run", logRemark));
                this.batchStartTime = Calendar.getInstance();
                // batch開始,先更新Batch執行狀態及開始時間
                jobHelper.updateBatchResult(logData, this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail, this.notifyPhone);
                if (CollectionUtils.isNotEmpty(this.jobData)) {
                    Map<String, Object> batch = this.jobData.get(0);
                    this.batchName = (String) batch.get("BATCH_NAME");
                    this.notifyType = ((BigDecimal) batch.get("BATCH_NOTIFYTYPE")).intValue();
                    this.notifyMail = (String) batch.get("BATCH_NOTIFYMAIL");
                    this.notifyPhone = (String) batch.get("BATCH_NOTIFYPHONE");
                    // 找出要執行Job中的第一個step
                    List<Map<String, Object>> list = this.jobData.stream().filter(t -> (Integer) t.get("JOBS_JOBID") == this.currentJob).collect(Collectors.toList());
                    // 執行同一Step中的所有Task
                    if (CollectionUtils.isNotEmpty(list)) {
                        for (Map<String, Object> map : list) {
                            // DELAY FOR JOB
                            try {
                                Thread.sleep((Integer) map.get("JOBS_DELAY") * 1000L);
                            } catch (InterruptedException e) {
                                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                            }
                            this.currentJobStep = (Integer) map.get("JOBS_SEQ");
                            boolean bResult = this.runTask(
                                    logData,
                                    FEPConfig.getInstance().getHostName(),
                                    this.currentBatch,
                                    (String) map.get("BATCH_NAME"),
                                    this.currentJob,
                                    (Integer) map.get("JOBTASK_TASKID"),
                                    ((Integer) map.get("JOBTASK_STEPID")).shortValue(),
                                    (String) map.get("TASK_COMMAND"),
                                    (String) map.get("TASK_COMMANDARGS"));
                            if (!bResult) {
                                break;
                            }
                        }
                    }
                }
                break;
            case Running:
                this.historySeq = jobHelper.addJobLog(logData,
                        FEPConfig.getInstance().getHostName(),
                        this.jobInstanceId,
                        this.currentBatch,
                        this.currentJob,
                        this.currentTask,
                        this.currentStep,
                        "工作開始執行",
                        this.currentState,
                        this.batchStartTime,
                        this.currentJobLog);
                break;
            case End:
            case Failed:
            case Abort:
                doneFlag.put(StringUtils.join(this.currentJob, "_", this.currentTask), true);
                // 找出目前結束的工作
                jobHelper.log(logData, "JobInstance.Run", this.jobInstanceId, StringUtils.join("currentJob:", currentJob, ",currentStep:", currentStep, ",currentTask:", currentTask));
                List<Map<String, Object>> list = this.jobData.stream().filter(t -> (Integer) t.get("JOBTASK_JOBID") == this.currentJob
                        && (Integer) t.get("JOBTASK_STEPID") == this.currentStep
                        && (Integer) t.get("JOBTASK_TASKID") == this.currentTask).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Map<String, Object> map : list) {
                        jobHelper.updateJobLog(logData, this.jobInstanceId, this.currentMessage, this.currentState, BatchResult.Running, map, this.historySeq, this.currentJobLog);
                        // 檢查等待的Task是否已完成
                        if (!checkWaitFlag(logData, this.currentJob, (String) map.get("JOBTASK_WAITFORTASK")))
                            return;
                    }
                }
                if (this.currentState == JobState.Abort) {
                    this.currentResult = BatchResult.Failed;
                    this.eachJobResult.put(this.currentJob, this.currentState);
                    jobHelper.updateBatchResult(logData, this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail, this.notifyPhone);
                    // AbortTask,結束批次執行
                    // 2025-06-10 Richard add start for 失敗時決定是否要繼續執行Job, 改由JOBS_CONTINUEONFAIL決定
                    if (this.checkJobsContinueonfail(logData, jobHelper, list))
                        break;
                    // 2025-06-10 Richard add end for 失敗時決定是否要繼續執行Job, 改由JOBS_CONTINUEONFAIL決定
                } else if (this.currentState == JobState.Failed) {
                    this.currentResult = BatchResult.PartialFailed;
                    this.eachJobResult.put(this.currentJob, this.currentState);
                    // 2025-06-10 Richard add start for 失敗時決定是否要繼續執行Job, 改由JOBS_CONTINUEONFAIL決定
                    if (this.checkJobsContinueonfail(logData, jobHelper, list))
                        break;
                    // 2025-06-10 Richard add end for 失敗時決定是否要繼續執行Job, 改由JOBS_CONTINUEONFAIL決定
                    // Failed繼續往下做
                } else {
                    // CurrentResult = BatchResult.Successful;
                    this.eachJobResult.put(this.currentJob, this.currentState);
                }
                this.currentStep += 1; // 執行成功目前步驟加1
                // 找出Job下一步驟的Task
                dtJob = jobHelper.getBatchContext(logData, this.currentBatch, this.currentJob, this.currentJobStep, this.currentStep);
                //
                if (CollectionUtils.isEmpty(dtJob)) {
                    // 無下一步驟,檢查是否有下一個Job
                    this.currentJobStep += 1;
                    this.currentStep = 1;
                    this.currentJob = 0;
                    dtJob = jobHelper.getBatchContext(logData, this.currentBatch, this.currentJob, this.currentJobStep, this.currentStep);
                }
                // 最後一個Job或是全部都Job都已執行過, 更新Batch Result
                if (CollectionUtils.isEmpty(dtJob) || (jobData != null && jobData.size() == eachJobResult.size())) {
                    // 無下一Job,更新Batch執行結果
                    if (this.eachJobResult.values().stream().filter(t -> t == JobState.End).count() == this.eachJobResult.size()) {
                        this.currentResult = BatchResult.Successful;
                    } else if (this.eachJobResult.values().stream().filter(t -> t == JobState.Abort).count() > 1) {
                        this.currentResult = BatchResult.Failed;
                    } else if (this.eachJobResult.values().stream().filter(t -> t == JobState.Failed).count() > 1) {
                        this.currentResult = BatchResult.PartialFailed;
                    }
                    jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId, StringUtils.join("no more jobs, Update batch result ", this.currentResult));
                    jobHelper.updateBatchResult(logData, this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, this.currentResult, this.notifyType, this.notifyMail, this.notifyPhone);
                } else {
                    // DELAY FOR THIS JOB
                    int delay = (Integer) dtJob.get(0).get("JOBS_DELAY") * 1000;
                    this.currentJob = (Integer) dtJob.get(0).get("JOBTASK_JOBID");
                    // 若下一個Job之前已執行過, 代表是重做的批次, 結束執行
                    if (eachJobResult.containsKey(this.currentJob)) {
                        jobHelper.log(logData, Level.WARN, "JobInstance.Run", this.jobInstanceId, StringUtils.join("JobId ", this.currentJob, "已執行過, 結束批次"));
                        break;
                    }
                    LogHelperFactory.getTraceLogger().info("Job Id ", this.currentJob, " Wait for ", delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                    }
                    for (Map<String, Object> map : dtJob) {
                        // 如果批次被停用則不執行此task
                        if (map.get("BATCH_ENABLE") == null || !DbHelper.toBoolean(((Integer) map.get("BATCH_ENABLE")).shortValue())) {
                            return;
                        }
                        boolean bResult = this.runTask(
                                logData,
                                FEPConfig.getInstance().getHostName(),
                                this.currentBatch,
                                (String) map.get("BATCH_NAME"),
                                this.currentJob,
                                (Integer) map.get("JOBTASK_TASKID"),
                                ((Integer) map.get("JOBTASK_STEPID")).shortValue(),
                                (String) map.get("TASK_COMMAND"),
                                (String) map.get("TASK_COMMANDARGS"));
                        if (!bResult) {
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private boolean checkWaitFlag(LogData logData, int jobId, String waitFlag) {
        if ("0".equals(waitFlag)) {
            return true;
        }
        String[] waitTask = waitFlag.split(",");
        for (String s : waitTask) {
            Boolean flag = this.doneFlag.get(StringUtils.join(jobId, "_", Integer.parseInt(s)));
            if (flag == null) {
                jobHelper.log(logData, Level.WARN, "JobInstance.checkWaitFlag", this.jobInstanceId, StringUtils.join("[JobInstance]Cannot find flag from map by key = [", jobId, "_", s, "], the doneFlag = [", doneFlag.toString(), "]"));
            }
            if (flag == null || !flag) {
                return false;
            }
        }
        return true;
    }

    private boolean runTask(LogData logData, String hostName, int batchId, String batchName, int jobId, int taskId, int stepId, String taskCommand, String taskArgs) {
        String errMsg = StringUtils.EMPTY;
        String args = StringUtils.join(
                "/hostName:", StringUtils.isBlank(hostName) ? FEPConfig.getInstance().getHostName() : hostName,
                " /instanceid:", this.jobInstanceId,
                " /taskid:", taskId,
                " /stepid:", stepId,
                " /batchname:", batchName,
                " /batchid:", batchId,
                this.getUniqueArgs(this.customParameters, taskArgs));
        JobHelper jobHelper = SpringBeanFactoryUtil.getBean(JobHelper.class);
        jobHelper.log(logData, "JobInstance.RunTask", this.jobInstanceId, StringUtils.join("Begin to run Task Command:", taskCommand, StringUtils.SPACE, args));
        String tskKey = StringUtils.join(jobId, "_", taskId);
        this.doneFlag.put(tskKey, false);
        RefString refErrMsg = new RefString(errMsg);
        boolean result = jobHelper.runProcess(logData, this.jobInstanceId, taskCommand, args, refErrMsg);
        errMsg = refErrMsg.get();
        jobHelper.log(logData, "JobInstance.RunTask", this.jobInstanceId, StringUtils.join("Run task complete. Result:", result, ",ErrMsg:", errMsg));
        if (!result) {
            this.historySeq = jobHelper.addJobLog(logData,
                    FEPConfig.getInstance().getHostName(),
                    this.jobInstanceId,
                    batchId,
                    jobId,
                    taskId,
                    stepId,
                    StringUtils.join("工作執行發生異常:", errMsg),
                    JobState.End,
                    this.batchStartTime,
                    this.currentJobLog);
            this.eachJobResult.put(this.currentJob, JobState.Failed);
            jobHelper.updateBatchResult(logData, this.currentBatch, this.batchName, this.batchStartTime, this.jobInstanceId, BatchResult.Failed, this.notifyType, this.notifyMail, this.notifyPhone);
            return false;
        }
        return true;
    }

    /**
     * 傳進來2組參數字串排除重覆部分再回傳,以第一個參數為主,第2個參數重覆會被排除掉
     *
     * @param arg1
     * @param arg2
     * @return
     */
    private String getUniqueArgs(String arg1, String arg2) {
        Map<String, String> map = new HashMap<>();
        this.filterUniqueArgs(map, arg1);
        this.filterUniqueArgs(map, arg2);
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : map.entrySet()) {
            sb.append(StringUtils.SPACE).append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 過濾重複的參數
     *
     * @param map
     * @param argument
     */
    private void filterUniqueArgs(Map<String, String> map, String argument) {
        if (StringUtils.isNotBlank(argument)) {
            String[] args = argument.split("\\s+");
            for (String arg : args) {
                if (StringUtils.isNotBlank(arg)) {
                    int index = arg.indexOf(":");
                    if (index > 1) {
                        String key = arg.substring(0, index).trim();
                        String value = arg.substring(index + 1).trim();
                        if (!map.containsKey(key))
                            map.put(key, value);
                    }
                }
            }
        }
    }

    private boolean checkJobsContinueonfail(LogData logData, JobHelper jobHelper, List<Map<String, Object>> list) {
        if (CollectionUtils.isNotEmpty(list) && DbHelper.getMapValue(list.get(0), "JOBS_CONTINUEONFAIL", JobsContinueOnFail.Interrupt.ordinal()) == JobsContinueOnFail.Interrupt.ordinal()) {
            jobHelper.log(logData, Level.WARN, "JobInstance.run", this.jobInstanceId,
                    StringUtils.join("Batch Interrupted cause JOBS_CONTINUEONFAIL = ", JobsContinueOnFail.Interrupt.ordinal(), ", currentState:", this.currentState, ", currentJob:", currentJob, ", currentStep:", currentStep, ", currentTask:", currentTask, ", data:", list.get(0).toString()));
            return true;
        }
        return false;
    }

    public int getCurrentBatch() {
        return currentBatch;
    }

    public int getCurrentJob() {
        return currentJob;
    }

    public int getCurrentTask() {
        return currentTask;
    }

    public String getCurrentJobLog() {
        return currentJobLog;
    }

    public long getHistorySeq() {
        return historySeq;
    }

    public String getBatchName() {
        return batchName;
    }

    public Calendar getBatchStartTime() {
        return batchStartTime;
    }

    public int getNotifyType() {
        return notifyType;
    }

    public String getNotifyMail() {
        return notifyMail;
    }

    public String getNotifyPhone() {
        return notifyPhone;
    }

    public void setCurrentJob(int currentJob) {
        this.currentJob = currentJob;
    }

    public void setCurrentJobLog(String currentJobLog) {
        this.currentJobLog = currentJobLog;
    }

    public void setCurrentState(JobState currentState) {
        this.currentState = currentState;
    }

    public void setCurrentResult(BatchResult currentResult) {
        this.currentResult = currentResult;
    }

    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public void setCurrentTask(int currentTask) {
        this.currentTask = currentTask;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public BatchResult getCurrentResult() {
        return currentResult;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
}
