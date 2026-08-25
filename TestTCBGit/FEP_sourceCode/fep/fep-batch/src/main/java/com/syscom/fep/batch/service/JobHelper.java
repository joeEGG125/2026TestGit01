package com.syscom.fep.batch.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.enums.BatchResult;
import com.syscom.fep.batch.base.enums.JobState;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.job.BatchJobManager;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.mail.MailSender;
import com.syscom.fep.common.sms.mitake.MitakeSmsOperator;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.util.FEPNotifyUtil;
import com.syscom.fep.frmcommon.esapi.ESAPIUtil;
import com.syscom.fep.frmcommon.io.StreamGobbler;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.mail.MailData;
import com.syscom.fep.frmcommon.mail.MailPriority;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.HistoryExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.History;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

@Component
public class JobHelper extends FEPBase {
    @Autowired
    private BatchConfiguration batchConfiguration;
    @Autowired
    private BatchJobManager manager;
    @Autowired
    private BsdaysExtMapper bsdaysMapper;
    @Autowired
    private BatchExtMapper batchMapper;
    @Autowired
    private HistoryExtMapper historyMapper;
    @Autowired
    private FEPConfig fepConfig;
    // 2025-09-03 Richard modified 不透過fep-notify送mail, FEP自己送mail
    // @Autowired
    // private NotifyHelper notifyHelper;
    private static final String subject = "FEP批次管理系統通知";
    private final BatchJobLibrary batchLib = new BatchJobLibrary();

    public boolean runProcess(LogData logData, String jobInstanceId, String programName, String arguments, RefString refErrMsg) {
        try {
            if (this.batchConfiguration.isExecuteDevelop()) {
                return this.runProcessInDevelop(logData, jobInstanceId, programName, arguments);
            }
            return this.runProcessInCommandline(logData, jobInstanceId, programName, arguments);
        } catch (Exception e) {
            refErrMsg.set(e.getMessage());
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.runProcess");
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE); // 這裡要還原FEPLOGGER記錄的檔名
        }
        return false;
    }

    /**
     * 透過fep-batch-cmdline呼叫批次程式
     *
     * @param logData
     * @param jobInstanceId
     * @param programName
     * @param arguments
     * @return
     * @throws Exception
     */
    private boolean runProcessInCommandline(LogData logData, String jobInstanceId, String programName, String arguments) throws Exception {
        if (StringUtils.isBlank(batchConfiguration.getExecuteShellPath()))
            throw ExceptionUtil.createException("${spring.fep.batch.execute.shell.path} in configuration file is null!!!");
        List<String> commands = new ArrayList<>(3);
        commands.add(batchConfiguration.getExecuteShellPath());
        commands.add(programName);
        commands.add(arguments);
        commands.add("true"); // 是否會送Queue給BatchControlService
        this.log(logData, "JobHelper.runProcessInCommandline", jobInstanceId, StringUtils.join("Begin to execute Command:", StringUtils.join(commands, StringUtils.SPACE)));
        ProcessBuilder processBuilder = new ProcessBuilder().command(commands);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        Consumer<String> consumer = this.batchConfiguration.isExecuteShellOutput() ? LogHelperFactory.getTraceLogger()::debug : null;
        String charsetName = StringUtils.isBlank(this.batchConfiguration.getExecuteShellCharsetName()) ? StandardCharsets.UTF_8.displayName() : this.batchConfiguration.getExecuteShellCharsetName();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), charsetName, consumer);
        new Thread(streamGobbler).start();
        this.log(logData, "JobHelper.runProcessInCommandline", jobInstanceId, StringUtils.join("Execute Succeed, Command:", StringUtils.join(commands, StringUtils.SPACE)));
        return true;
    }

    /**
     * 開發模式下呼叫批次程式
     *
     * @param logData
     * @param jobInstanceId
     * @param programName
     * @param arguments
     * @return
     * @throws Exception
     */
    private boolean runProcessInDevelop(LogData logData, String jobInstanceId, String programName, String arguments) throws Exception {
        this.log(logData, "JobHelper.runProcessInDevelop", jobInstanceId, StringUtils.join("Begin to run Task Program:", programName, ", arguments:", arguments));
        Task task = batchLib.getBatchTask(programName);
        LogMDC.put(Const.MDC_PROFILE, task.getClass().getSimpleName()); // 這裡要改變一下FEPLOGGER記錄的檔名, 記錄到對應的批次程式中
        task.execute(arguments.split(StringUtils.SPACE));
        this.log(logData, "JobHelper.runProcessInDevelop", jobInstanceId, StringUtils.join("Run Succeed, Task Program:", programName, ", arguments:", arguments));
        return true;
    }

    /**
     * 因為在上線前Table先不動,所以先把此Function修改內容先移至這裏,待上線後再改回呼叫Table的
     *
     * @param logData
     * @param batchId
     * @param jobId
     * @param jobSeq
     * @param stepId
     * @return
     */
    public List<Map<String, Object>> getBatchContext(LogData logData, int batchId, int jobId, int jobSeq, int stepId) {
        try {
            return batchMapper.getBatchContext(batchId, jobId, jobSeq, stepId);
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.getBatchContext");
            logData.setRemark(StringUtils.join("get Batch data failed, batchId:", batchId, ",jobId:", jobId, ",jobSeq:", jobSeq, ",stepId:", stepId));
            logData.setProgramException(e);
            sendEMS(logData);
        }
        return Collections.emptyList();
    }

    public void updateBatchResult(LogData logData, int batchId, String batchName, Calendar batchStartTime, String instanceId, BatchResult status, int notifyType, String notifyMail, String notifyPhone) {
        Batch batch = new Batch();
        batch.setBatchBatchid(batchId);
        batch.setBatchCurrentid(instanceId);
        batch.setBatchResult(String.valueOf(status.getValue()));
        if (status == BatchResult.Running) {
            batch.setBatchLastruntime(batchStartTime.getTime());
        }
        batch.setBatchZone(null); // 避免原本的欄位被更新掉
        try {
            batchMapper.updateByPrimaryKeySelective(batch);
            LogHelperFactory.getTraceLogger().info("UpdateBatchResult batchId = [", batchId, "], status = [", status, "]");
            // 通知
            if (notifyType > 0) {
                this.notifyResult(logData, batchName, instanceId, batchStartTime, notifyType, notifyMail, notifyPhone, status);
            }
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.updateBatchResult");
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            // 記錄執行結果到檔案中
            this.writeBatchResult(batchName, notifyType, status);
        }
    }

    /**
     * 1. 當批次設定需要通知時,  最後更新批次執行結果時, 根據BatchResult, 寫入一個檔案中,檔案的名稱及路徑請設在property中, 格式如下：
     * <p>
     * Timestamp,Message,Status (OK/Error)
     * <p>
     * 若BatchResult為Successful AND (成功需通知或成功失敗都通知)時, Message為 {批次名稱}執行成功, Status為OK
     * <p>
     * 若BatchResult為 Failed AND (失敗需通知或成功失敗都通知)時, Message為 {批次名稱}執行失敗, Status為Error
     * <p>
     * 2. 以Append方式記錄內容, 開始記錄前, 若該檔的最後更新日期不等於今天日期, 則先將檔案清空再開始記錄
     *
     * @param batchName
     * @param notifyType
     * @param result
     */
    private void writeBatchResult(String batchName, int notifyType, BatchResult result) {
        if (StringUtils.isBlank(batchName))
            return;
        switch (notifyType) {
            // 成功時通知
            case 1:
                if (result != BatchResult.Successful)
                    return;
                break;
            // 失敗時通知
            case 2:
                if (result != BatchResult.Failed)
                    return;
                break;
            // 成功失敗都通知
            case 3:
                if (result != BatchResult.Successful && result != BatchResult.Failed)
                    return;
                break;
            default:
                return;
        }
        synchronized (this) {
            String path = this.batchConfiguration.getResultFilePath();
            if (StringUtils.isBlank(path)) {
                LogHelperFactory.getTraceLogger().warn("cannot writeBatchResult, cause ResultFilePath is empty");
                return;
            }
            File file = ESAPIUtil.toFile(CleanPathUtil.cleanString(path));
            if (!file.getParentFile().exists()) {
                try {
                    FileUtils.forceMkdirParent(file);
                } catch (IOException e) {
                    LogData log = new LogData();
                    log.setSubSys(SubSystem.CMN);
                    log.setProgramName("JobHelper.writeBatchResult");
                    log.setRemark("forceMkdirParent error.");
                    log.setProgramException(e);
                    sendEMS(log);
                }
            }
            boolean append = true;
            // 開始記錄前, 若該檔的最後更新日期不等於今天日期, 則先將檔案清空
            if (file.exists() && !FormatUtil.dateFormat(file.lastModified()).equals(FormatUtil.dateFormat(Calendar.getInstance().getTimeInMillis()))) {
                // if (file.delete())
                //     LogHelperFactory.getTraceLogger().info("Delete previous day batch result file success, path = [", file.getAbsolutePath(), "]");
                // else
                //     LogHelperFactory.getTraceLogger().warn("Delete previous day batch result file failed, path = [", file.getAbsolutePath(), "]");
                append = false;
            }
            try {
                // Timestamp,Status (OK/Error),Message
                // 若BatchResult為Successful AND (成功需通知或成功失敗都通知)時, Message為 {批次名稱}執行成功, Status為OK
                // 若BatchResult為 Failed AND (失敗需通知或成功失敗都通知)時, Message為 {批次名稱}執行失敗, Status為Error
                // 請幫我調一下batchResult的格式如下:
                //
                // Timestamp:Status;(OK/Error);Message
                // A.分隔符號改成;
                // B.Timestamp 格式 改成 YYYYMMDDHHMMSS 如20241113153030
                // C.Message 放在最後,內容使用英文,如 {batchName} execute OK/Fail, 長度超過120 字元的部分trim掉
                String str = StringUtils.join(Arrays.asList(
                                FormatUtil.dateTimeFormat(Calendar.getInstance().getTime(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN),
                                result == BatchResult.Successful ? "OK" : "Error",
                                StringUtils.substring(StringUtils.join(batchName, " execute ", (result == BatchResult.Successful ? "OK" : "Fail")), 0, 120)),
                        ';');
                // 以Append方式記錄內容
                FileUtils.writeLines(file, StandardCharsets.UTF_8.name(), Collections.singletonList(str), append);
            } catch (IOException e) {
                LogData log = new LogData();
                log.setSubSys(SubSystem.CMN);
                log.setProgramName("JobHelper.writeBatchResult");
                log.setRemark("writeBatchResult error.");
                log.setProgramException(e);
                sendEMS(log);
            }
        }
    }

    public void updateBatchNextRunTime(LogData logData, int batchBatchid, String instanceId) {
        // 批次啟動時會呼叫這個方法, 但是注意, 只有排程中的批次才會取到BatchScheduler物件
        BatchScheduler batchScheduler = manager.getBatchScheduler(Integer.toString(batchBatchid));
        if (batchScheduler != null) {
            updateBatchNextRunTime(logData, batchBatchid, instanceId, batchScheduler.getNextFireTime());
        } else {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.updateBatchNextRunTime");
            logData.setRemark(StringUtils.join("Cannot update Batch Next Run time, cause Quartz Job not exist, batchBatchid: ", batchBatchid));
            logMessage(Level.WARN, logData);
        }
    }

    public void updateBatchNextRunTime(LogData logData, int batchBatchid, String instanceId, Date batchNextruntime) {
        try {
            log(logData, "JobHelper.updateBatchNextRunTime", instanceId, StringUtils.join("Update BatchNextruntime, batchBatchid:", batchBatchid, ", batchNextruntime:", FormatUtil.dateTimeFormat(batchNextruntime)));
            batchMapper.updateBatchNextruntime(batchBatchid, batchNextruntime);
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.updateBatchNextRunTime");
            logData.setRemark("update batch failed, batchBatchid:" + batchBatchid);
            logData.setProgramException(e);
            sendEMS(logData);
        }
    }

    public long addJobLog(LogData logData, String hostName, String instanceId, int batchId, int jobId, int taskId, int stepId, String message, JobState jobStatus, Calendar batchStartTime, String logFile) {
        History history = new History();
        history.setHistoryInstanceid(instanceId);
        history.setHistoryBatchid(batchId);
        history.setHistoryJobid(jobId);
        history.setHistoryTaskid(taskId);
        history.setHistoryStepid(stepId);
        history.setHistoryStarttime(batchStartTime.getTime());
        history.setHistoryTaskbegintime(Calendar.getInstance().getTime());
        history.setHistoryMessage(message);
        history.setHistoryStatus(String.valueOf(jobStatus.getValue()));
        history.setHistoryLogfile(logFile);
        history.setHistoryRunhost(hostName);
        if (StringUtils.isNotBlank(history.getHistoryMessage()) && history.getHistoryMessage().length() > 100) {
            history.setHistoryMessage(history.getHistoryMessage().substring(0, 100));
        }
        log(logData, "JobHelper.addJobLog", instanceId, StringUtils.join("insert history, ", history.toString()));
        try {
            historyMapper.insertSelective(history);
            return history.getHistorySeq();
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.addJobLog");
            logData.setRemark(StringUtils.join("insert history failed, ", history.toString()));
            logData.setProgramException(e);
            sendEMS(logData);
        }
        return 0;
    }

    public void updateJobLog(LogData logData, String instanceId, String message, JobState status, BatchResult batchStatus, String batchId, String jobId, String taskId, long historySeq, String jobLog) {
        History history = new History();
        history.setHistorySeq(historySeq);
        history.setHistoryInstanceid(instanceId);
        history.setHistoryBatchid(Integer.parseInt(batchId));
        history.setHistoryJobid(Integer.parseInt(jobId));
        history.setHistoryTaskid(Integer.parseInt(taskId));
        history.setHistoryMessage(StringUtils.join(status != null ? StringUtils.join(status.getDescription(), ". ") : StringUtils.EMPTY, message));
        if (StringUtils.isNotBlank(history.getHistoryMessage()) && history.getHistoryMessage().length() > 100) {
            history.setHistoryMessage(history.getHistoryMessage().substring(0, 100));
        }
        if (status == JobState.Running) {
            history.setHistoryTaskbegintime(Calendar.getInstance().getTime());
            history.setHistoryLogfile(jobLog); // 2025-09-19 Richard add 這裡補上log檔
        } else if (status == JobState.End || status == JobState.Failed || status == JobState.Abort) {
            history.setHistoryTaskendtime(Calendar.getInstance().getTime());
            History history1 = historyMapper.selectByPrimaryKey(historySeq);
            // 2025-04-18 Richard modified 這裡補上history1 != null的判斷
            if (history1 != null && history1.getHistoryTaskbegintime() != null) {
                long dur = history.getHistoryTaskendtime().getTime() - history1.getHistoryTaskbegintime().getTime();
                history.setHistoryDuration((int) dur);
            }
        }
        if (status != null) {
            history.setHistoryStatus(String.valueOf(status.getValue()));
        }
        log(logData, "JobHelper.updateJobLog", instanceId, StringUtils.join("update history, ", history.toString()));
        try {
            historyMapper.updateByPrimaryKeySelective(history);
            if (status == JobState.End || status == JobState.Failed || status == JobState.Abort) {
                history.setHistoryTaskendtime(history.getHistoryTaskendtime());
                if (StringUtils.isNotBlank(jobLog)) {
                    File file = new File(CleanPathUtil.cleanString(jobLog));
                    if (file.exists()) {
                        try {
                            // 為避免Task還沒關閉檔案先Sleep 1秒再開
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                        int bufferSize = 8 * 1024;
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), bufferSize)) {
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                sb.append(line).append("<br/>").append("\r\n");
                            }
                            if (sb.length() < 64000) {
                                history.setHistoryLogfilecontent(sb.toString());
                            }
                        } catch (Exception e) {
                            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
                        }
                        if (StringUtils.isNotBlank(history.getHistoryLogfilecontent())) {
                            try {
                                historyMapper.updateByPrimaryKeySelective(history);
                            } catch (Exception e) {
                                logData.setSubSys(SubSystem.CMN);
                                logData.setChannel(FEPChannel.BATCH);
                                logData.setProgramName("JobHelper.updateJobLog");
                                logData.setRemark("update history failed, historySeq:" + historySeq);
                                logData.setProgramException(e);
                                sendEMS(logData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.updateJobLog");
            logData.setRemark("update job log failed, historySeq:" + historySeq);
            logData.setProgramException(e);
            sendEMS(logData);
        }
    }

    public void updateJobLog(LogData logData, String instanceId, String message, JobState status, BatchResult batchStatus, Map<String, Object> map, long historySeq, String jobLog) {
        this.updateJobLog(logData, instanceId, message, status, batchStatus,
                String.valueOf(((Integer) map.get("BATCH_BATCHID")).longValue()),
                String.valueOf(((Integer) map.get("JOBS_JOBID")).longValue()),
                String.valueOf(((Integer) map.get("JOBTASK_TASKID")).longValue()),
                historySeq, jobLog);
    }

    public void log(LogData logData, String programName, String instanceId, String msg) {
        this.log(logData, Level.INFO, programName, instanceId, msg);
    }

    public void log(LogData logData, Level level, String programName, String instanceId, String msg) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_BATCH_CONTROL_SERVICE);
        if (logData == null)
            logData = new LogData();
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(programName);
        logData.setMessage(instanceId);
        logData.setRemark(msg);
        logMessage(level, logData);
    }

    private void notifyResult(LogData logData, String batchName, String instanceId, Calendar batchStartTime, int notifyType, String notifyMail, String notifyPhone, BatchResult result) throws Exception {
        String body = this.getNotifyBody(batchName, batchStartTime, result);
        if (StringUtils.isNotBlank(notifyMail)) {
            // String execStartTime = FormatUtil.dateTimeFormat(batchStartTime, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
            // String execEndTime = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
            // String body = StringUtils.join("批次名稱: ", batchName, "\n<br/>執行主機: ", fepConfig.getHostName(), "\n<br/>批次開始時間: ", execStartTime, "\n<br/>批次結束時間: ", execEndTime, "\n<br/>執行結果: ", result == BatchResult.Successful ? "成功" : "失敗");
            switch (notifyType) {
                // 成功時通知
                case 1:
                    if (result != BatchResult.Successful)
                        return;
                    break;
                // 失敗時通知
                case 2:
                    if (result != BatchResult.Failed && result != BatchResult.PartialFailed)
                        return;
                    break;
                // 成功失敗都通知
                case 3:
                    break;
            }
            MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class, false);
            if (mailSender != null) {
                log(logData, StringUtils.join(ProgramName, ".notifyResult"), instanceId, StringUtils.join("try to send mail, ", StringUtils.replace(body, "\n", ",")));
                // 2025-08-14 Richard add 這裡需要轉一次, 如果FEPNotifyMail_APD, FEPNotifyMail_SYS等這樣的值, 需要從sysconf檔中對應取出實際的值
                List<String> mailTo = FEPNotifyUtil.getNotifiyList(notifyMail);
                try {
                    // notifyHelper.sendSimpleMail(NotifyHelperTemplateId.BATCH, StringUtils.join(mailTo, ','), body, true);
                    // 2025-09-03 Richard modified不再使用notify送mail, 改用FEP自己送
                    MailData mailData = new MailData();
                    mailData.setFrom(batchConfiguration.getMailSender());
                    mailData.setTo(StringUtils.join(mailTo, ','));
                    mailData.setSubject(subject);
                    mailData.setBody(body);
                    if (result == BatchResult.Failed || result == BatchResult.PartialFailed)
                        mailData.setPriority(MailPriority.High);
                    mailSender.sendSimpleEmail(mailData);
                } catch (Exception e) {
                    logData.setSubSys(SubSystem.CMN);
                    logData.setChannel(FEPChannel.BATCH);
                    logData.setProgramName("JobHelper.notifyResult");
                    logData.setProgramException(e);
                    sendEMS(logData);
                }
            }
        }
        if (StringUtils.isNotBlank(notifyPhone)) {
            // 2025-09-03 Richard modified 簡訊改用三竹
            // HiairSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(HiairSmsOperator.class, false);
            MitakeSmsOperator smsOperator = SpringBeanFactoryUtil.getBean(MitakeSmsOperator.class, false);
            if (smsOperator != null) {
                log(logData, StringUtils.join(ProgramName, ".notifyResult"), instanceId, StringUtils.join("try to send sms, ", StringUtils.replace(body, "\n", ",")));
                // StringBuilder sb = new StringBuilder();
                // String result = "成功";
                // if ("0".equals(batch.getBatchResult())) {
                //     result = "";
                // } else if ("2".equals(batch.getBatchResult())) {
                //     result = "失敗";
                // } else if ("3".equals(batch.getBatchResult())) {
                //     result = "部分失敗";
                // }
                // sb.append(subject).append("-").append(batch.getBatchScheduleStarttime()).append("-")
                //         .append(batch.getBatchExecuteHostName()).append("-")
                //         .append(batch.getBatchName()).append("-").append(result);
                // String[] smsTelList = batchConfiguration.getSmsTelList();
                // String message = sb.toString();
                // 2025-08-14 Richard add 這裡需要轉一次, 如果FEPNotifyPhone_APD, FEPNotifyPhone_SYS等這樣的值, 需要從sysconf檔中對應取出實際的值
                List<String> smsTelList = FEPNotifyUtil.getNotifiyList(notifyPhone);
                smsOperator.send(smsTelList, body, true);
            }
        }
    }

    public boolean checkBusinessDate(LogData logData, String zone, String batchName, int batchId, String instanceId, String notifyMail) {
        if (StringUtils.isBlank(zone)) {
            if (StringUtils.isNotBlank(notifyMail)) {
                MailSender mailSender = SpringBeanFactoryUtil.getBean(MailSender.class, false);
                if (mailSender != null) {
                    // 2025-08-14 Richard add 這裡需要轉一次, 如果FEPNotifyMail_APD, FEPNotifyMail_SYS等這樣的值, 需要從sysconf檔中對應取出實際的值
                    List<String> mailTo = FEPNotifyUtil.getNotifiyList(notifyMail);
                    String body = StringUtils.join("批次名稱:", batchName, "檢核營業日失敗!未傳入地區別\n");
                    try {
                        // notifyHelper.sendSimpleMail(NotifyHelperTemplateId.BATCH, configuration.getMailList(), body, true);
                        // 2025-09-03 Richard modified不再使用notify送mail, 改用FEP自己送
                        MailData mailData = new MailData();
                        mailData.setFrom(batchConfiguration.getMailSender());
                        mailData.setTo(StringUtils.join(mailTo, ','));
                        mailData.setSubject(subject);
                        mailData.setBody(body);
                        mailData.setPriority(MailPriority.High);
                        mailSender.sendSimpleEmail(mailData);
                    } catch (Exception e) {
                        logData.setSubSys(SubSystem.CMN);
                        logData.setChannel(FEPChannel.BATCH);
                        logData.setProgramName("JobHelper.checkBusinessDate");
                        logData.setRemark("send mail failed");
                        logData.setProgramException(e);
                        sendEMS(logData);
                    }
                }
            }
            return false;
        }
        try {
            Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(zone, Integer.toString(CalendarUtil.dateValue(Calendar.getInstance())));
            if (bsdays != null) {
                boolean result = DbHelper.toBoolean(bsdays.getBsdaysWorkday());
                log(logData, StringUtils.join(ProgramName, ".checkBusinessDate"), instanceId, StringUtils.join("checkBusinessDate result:", result, ",zone:", zone, ",batchId:", batchId));
                return result;
            }
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.checkBusinessDate");
            logData.setRemark("select bsdays failed");
            logData.setProgramException(e);
            sendEMS(logData);
        }
        return false;
    }

    private String getNotifyBody(String batchName, Calendar batchStartTime, BatchResult result) {
        // FEP批次管理系統通知-11:42:54-fepap1D-ArchivingLogFile-成功
        return StringUtils.join(
                Arrays.asList("FEP批次管理系統通知",
                        FormatUtil.timeFormat(batchStartTime.getTime()),
                        fepConfig.getHostName(),
                        batchName,
                        result == BatchResult.Successful ? "成功" : "失敗"), "-");
    }

    public List<Batch> getAllScheduledBatch(LogData logData) {
        try {
            return batchMapper.getAllScheduledBatch();
        } catch (Exception e) {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName("JobHelper.getAllScheduledBatch");
            logData.setRemark("getAllScheduledBatch failed");
            logData.setProgramException(e);
            sendEMS(logData);
        }
        return null;
    }
}