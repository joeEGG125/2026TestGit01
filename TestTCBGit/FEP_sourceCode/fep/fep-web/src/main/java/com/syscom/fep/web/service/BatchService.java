package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.transaction.TransactionWrapper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.web.entity.batch.BatchDailyRepetitionType;
import com.syscom.fep.web.entity.batch.MaintainTask;
import com.syscom.safeaa.mybatis.extmapper.SyscomroleExtMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BatchService extends BaseService {

    @Autowired
    BatchExtMapper batchExtMapper;

    @Autowired
    SubsysExtMapper subsysExtMapper;

    @Autowired
    JobtaskExtMapper jobtaskExtMapper;

    @Autowired
    JobsExtMapper jobsExtMapper;

    @Autowired
    TaskExtMapper taskExtMapper;

    @Autowired
    FepuserExtMapper fepuserExtMapper;

    @Autowired
    HistoryExtMapper historyExtMapper;

    @Autowired
    TwslogExtMapper twslogExtMapper;

    @Autowired
    SyscomroleExtMapper syscomroleExtMapper;

    /**
     * 取得 Batch ALL
     *
     * @return
     * @throws Exception
     */
    public List<Batch> getBatchAll() throws Exception {
        try {
            List<Batch> batchList = batchExtMapper.getBatchAll();
            for (Batch batch : batchList) {
                this.checkBatch(batch);
            }
            return batchList;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Integer getRoleID(String roleId) throws Exception {
        try {
            return syscomroleExtMapper.queryRoleIdByNo(roleId);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getAllBatch(String batchName, List<String> subsysList, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    batchExtMapper.getAllBatch(batchName, subsysList);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Batch getBatchByID(Integer batchId) {
        return this.checkBatch(batchExtMapper.selectByPrimaryKey(batchId));
    }

    public List<Subsys> getSubsysAll() {
        return subsysExtMapper.queryAll();
    }

    public int insertBatch(Batch batch) {
        this.checkBatch(batch);
        return batchExtMapper.insertSelective(batch);
    }

    public int updateBatch(Batch batch, String userId, boolean clearBatchnextruntime) throws Exception {
        RefInt ret = new RefInt();
        this.checkBatch(batch);
        batch.setUpdateUser(Integer.parseInt(userId));
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper, 注意TransactionWrapper必須寫進try(...)中
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務
            wrapper.beginTransaction(() -> {
                // update batch
                int ret1 = batchExtMapper.updateByPrimaryKeySelective(batch);
                if (ret1 <= 0) {
                    // rollback by manual
                    wrapper.rollback();
                    return;
                }
                int ret2 = 0;
                // 2025-12-18 Richard add 將Batchnextruntime欄位的值清掉
                if (clearBatchnextruntime) {
                    ret2 = batchExtMapper.updateBatchNextruntime(batch.getBatchBatchid(), null);
                    if (ret2 <= 0) {
                        // rollback by manual
                        wrapper.rollback();
                        return;
                    }
                }
                // commit by manual
                wrapper.commit();
                // set result
                ret.set(ret1 + ret2);
            });
            return ret.get();
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * 以下四個欄位的值, 過濾一下, 避免後面使用出現NumberFormatException
     *
     * @param batch
     */
    private Batch checkBatch(Batch batch) {
        if (batch != null) {
            if (StringUtils.isNotBlank(batch.getBatchScheduleWeekdays())) {
                if (batch.getBatchScheduleWeekdays().contains(",")) {
                    String[] temps = batch.getBatchScheduleWeekdays().split(",");
                    List<String> list = new ArrayList<String>();
                    for (String temp : temps) {
                        if (StringUtils.isNumeric(temp)) {
                            list.add(temp);
                        }
                    }
                    batch.setBatchScheduleWeekdays(StringUtils.join(list, ","));
                }
            }
            if (StringUtils.isNotBlank(batch.getBatchScheduleMonths())) {
                if (batch.getBatchScheduleMonths().contains(",")) {
                    String[] temps = batch.getBatchScheduleMonths().split(",");
                    List<String> list = new ArrayList<String>();
                    for (String temp : temps) {
                        if (StringUtils.isNumeric(temp)) {
                            list.add(temp);
                        }
                    }
                    batch.setBatchScheduleMonths(StringUtils.join(list, ","));
                }
            }
            if (StringUtils.isNotBlank(batch.getBatchScheduleMonthdays())) {
                if (batch.getBatchScheduleMonthdays().contains(",")) {
                    String[] temps = batch.getBatchScheduleMonthdays().split(",");
                    List<String> list = new ArrayList<String>();
                    for (String temp : temps) {
                        if (StringUtils.isNumeric(temp)) {
                            list.add(temp);
                        }
                    }
                    batch.setBatchScheduleMonthdays(StringUtils.join(list, ","));
                }
            }
            if (StringUtils.isNotBlank(batch.getBatchScheduleWhickweeks())) {
                if (batch.getBatchScheduleWhickweeks().contains(",")) {
                    String[] temps = batch.getBatchScheduleWhickweeks().split(",");
                    List<String> list = new ArrayList<String>();
                    for (String temp : temps) {
                        if (StringUtils.isNumeric(temp)) {
                            list.add(temp);
                        }
                    }
                    batch.setBatchScheduleWhickweeks(StringUtils.join(list, ","));
                }
            }
            if (StringUtils.isBlank(batch.getBatchZone())) {
                batch.setBatchZone(ZoneCode.TWN); // 如果沒有選擇地區別, 則預設塞入TWN
            }
            if (batch.getBatchDenyconcurrentexec() == null) {
                batch.setBatchDenyconcurrentexec(DbHelper.toShort(Boolean.FALSE));
            }
        }
        return batch;
    }

    public void deleteScheduleTask(String hostName, int batchId, String batchName, boolean forceDelete) throws Exception {
        BatchJobLibrary bcl = new BatchJobLibrary();
        bcl.deleteTask(hostName, String.valueOf(batchId), batchName, forceDelete);
        bcl.dispose();
    }

    public List<Map<String, Object>> getHistoryQuery(String batchName, String batchStartDate, String batchShortName, String subsys) {
        return historyExtMapper.getHistoryQuery(batchName, batchStartDate, batchShortName, subsys);
    }

    public List<Map<String, Object>> getTWSLOG(String twsTaskName, String batchStartDate) {
        return twslogExtMapper.getTwslogQuery(twsTaskName, batchStartDate);
    }

    public void createScheduleTask(int batchId) throws Exception {
        List<HashMap<String, Object>> hashMap = batchExtMapper.getBatchFirstTaskById(batchId);
        if (CollectionUtils.isNotEmpty(hashMap)) {
            if (hashMap.get(0).get("BATCH_SCHEDULE_TYPE") != null) {
                hashMap.get(0).putIfAbsent("BATCH_DESCRIPTION", StringUtils.EMPTY);
                boolean bEnableSchedule = DbHelper.toBoolean(String.valueOf(hashMap.get(0).get("BATCH_ENABLE"))) && DbHelper.toBoolean(String.valueOf(hashMap.get(0).get("BATCH_SCHEDULE")));
                BatchJobLibrary bcl = new BatchJobLibrary();
                switch (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_TYPE"))) {
                    case "D": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        String batchDailyRepetitionType = isnullTz(hashMap.get(0).get("BATCH_DAILY_REPETITION_TYPE"));
                        // 2026-01-08 Richard modified 依據BATCH_DAILY_REPETITION_TYPE來決定送Daily還是DailyRepetition
                        // if (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINTERVAL")).equals("0")) {
                        if (batchDailyRepetitionType.equals(BatchDailyRepetitionType.DAY.getValue())) {
                            bcl.createDailyTask(
                                    isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                    isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                    isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                    Short.parseShort(isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_DAYINTERVAL"))),
                                    bEnableSchedule);
                        } else if (batchDailyRepetitionType.equals(BatchDailyRepetitionType.TIME.getValue())) {
                            bcl.createDailyRepetitionTask(
                                    isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                    isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                    isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                    isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                    isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                    (short) 0,
                                    bEnableSchedule,
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINTERVAL")),
                                    isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_REPETITIONINDURATION")));
                        }
                        break;
                    }
                    case "W": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int weekdays = 0;
                        String[] wdays = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKDAYS")).split("[,]", -1);
                        for (int i = 0; i < wdays.length; i++) {
                            if (StringUtils.isNumeric(wdays[i]))
                                weekdays += Integer.parseInt(wdays[i]);
                        }
                        bcl.createWeeklyTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                weekdays,
                                Integer.parseInt(isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKINTERVAL"))),
                                bEnableSchedule);
                        break;
                    }
                    case "M": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int months = 0;
                        String[] smonths = hashMap.get(0).get("BATCH_SCHEDULE_MONTHS").toString().split("[,]", -1);
                        for (int i = 0; i < smonths.length; i++) {
                            if (StringUtils.isNumeric(smonths[i]))
                                months += Integer.parseInt(smonths[i]);
                        }
                        bcl.createMonthlyTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_MONTHDAYS")),
                                months, false, bEnableSchedule);
                        break;
                    }
                    case "O": {
                        if (!hashMap.get(0).containsKey("TASK_COMMAND")) {
                            hashMap.get(0).put("TASK_COMMAND", "");
                        }
                        if (!hashMap.get(0).containsKey("TASK_COMMANDARGS")) {
                            hashMap.get(0).put("TASK_COMMANDARGS", "");
                        }
                        int months = 0;
                        String[] smonths = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_MONTHS")).split("[,]", -1);
                        for (int i = 0; i < smonths.length; i++) {
                            if (StringUtils.isNumeric(smonths[i]))
                                months += Integer.parseInt(smonths[i]);
                        }
                        int weekdays = 0;
                        String[] wdays = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WEEKDAYS")).split("[,]", -1);
                        for (int i = 0; i < wdays.length; i++) {
                            if (StringUtils.isNumeric(wdays[i]))
                                weekdays += Integer.parseInt(wdays[i]);
                        }
                        int whickweeks = 0;
                        String[] wweeks = isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WHICKWEEKS")).split("[,]", -1);
                        for (int i = 0; i < wweeks.length; i++) {
                            if (StringUtils.isNumeric(wweeks[i]))
                                whickweeks += Integer.parseInt(wweeks[i]);
                        }
                        boolean bRunLastWeekOfMonth = false;
                        if (isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_WHICKWEEKS")).contains("16")) {
                            bRunLastWeekOfMonth = true;
                        }
                        bcl.createMonthlyDayOfWeekTask(
                                isnullTz(hashMap.get(0).get("BATCH_EXECUTE_HOST_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_BATCHID")),
                                isnullTz(hashMap.get(0).get("BATCH_NAME")),
                                isnullTz(hashMap.get(0).get("BATCH_STARTJOBID")),
                                isnullTz(hashMap.get(0).get("BATCH_DESCRIPTION")),
                                isnullTz(hashMap.get(0).get("TASK_COMMAND")),
                                isnullTz(hashMap.get(0).get("TASK_COMMANDARGS")),
                                isnullTz(hashMap.get(0).get("BATCH_SCHEDULE_STARTTIME")),
                                weekdays,
                                months,
                                whickweeks,
                                bRunLastWeekOfMonth,
                                bEnableSchedule);
                        break;
                    }
                }
            }
        }
    }

    public List<HashMap<String, Object>> getJobTaskByBatchId(int batchid) {
        return jobtaskExtMapper.getJobTaskByBatchId(batchid);
    }

    public int deleteBatch(int batchId) throws Exception {
        Batch dBatch = new Batch();
        dBatch.setBatchBatchid(batchId);
        dBatch = batchExtMapper.selectByPrimaryKey(dBatch.getBatchBatchid());
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (dBatch != null) {
                // 刪除Batch 相關的Task
                int ret = jobtaskExtMapper.deleteByBatchId(batchId);
                // 刪除Batch相關的Job
                ret += jobsExtMapper.deleteByBatchId(batchId);
                // 最後才是刪除Batch
                Batch batch = new Batch();
                batch.setBatchBatchid(batchId);
                ret += batchExtMapper.deleteByPrimaryKey(batch);
                // 刪除排程中的批次
                deleteScheduleTask(dBatch.getBatchExecuteHostName(), batchId, dBatch.getBatchName(), false);
                transactionManager.commit(txStatus);
                return ret;
            }
            return 0;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    public List<Task> getTaskAll() {
        return taskExtMapper.queryTaskAll();
    }

    public Task getTaskById(Integer taskID) {
        return taskExtMapper.selectByPrimaryKey(taskID);
    }

    public int updateJob(Jobs job, Integer taskId) throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int iRes = jobsExtMapper.updateByPrimaryKeySelective(job);
            iRes += jobtaskExtMapper.updateTaskIdByJobId(job.getJobsJobid(), taskId);
            transactionManager.commit(txStatus);
            return iRes;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    public int insertJobAndTask(Jobs job, Integer taskId) throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 找出目前JOB的筆數
            int jobCount = jobsExtMapper.getJobsCountByBatchId(job.getJobsBatchid());
            job.setJobsSeq(jobCount + 1);
            int ret = jobsExtMapper.insertSelective(job); // 回傳的為JOBId
            int jobid = job.getJobsJobid().intValue();
            if (jobCount == 0) {
                // 如果為第一個新增的JOB則Update BATCH檔的啟動JOB欄位
                Batch oBatch = new Batch();
                oBatch.setBatchBatchid(job.getJobsBatchid());
                oBatch.setBatchStartjobid(jobid);
                oBatch.setBatchZone(null); // 避免原本的欄位被更新掉
                ret += batchExtMapper.updateByPrimaryKeySelective(oBatch);
            }
            // 新增至JOBTASK
            Jobtask jobtask = new Jobtask();
            jobtask.setJobtaskJobid(jobid);
            jobtask.setJobtaskTaskid(taskId);
            ret += insertJobTask(jobtask);
            transactionManager.commit(txStatus);
            return ret;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    private int insertJobTask(Jobtask jobTask) {
        int ret = 0;
        // 找出目前JOB的筆數
        Jobtask dt = jobtaskExtMapper.getMaxJobTasktByJobId(jobTask.getJobtaskJobid());
        if (dt == null) {
            jobTask.setJobtaskStepid((short) 1);
            jobTask.setJobtaskWaitfortask("0");
            // 如果為第一個新增的JOBTASK則Update JOB檔的啟動TASK欄位
            Jobs oJob = new Jobs();
            oJob.setJobsJobid(jobTask.getJobtaskJobid());
            oJob.setJobsStarttaskid(jobTask.getJobtaskTaskid());
            ret += jobsExtMapper.updateByPrimaryKeySelective(oJob);
        } else {
            jobTask.setJobtaskStepid((short) (dt.getJobtaskStepid() + 1));
            jobTask.setJobtaskWaitfortask(String.valueOf(dt.getJobtaskTaskid()));
        }
        ret += jobtaskExtMapper.insertSelective(jobTask);
        return ret;
    }

    public int deleteJob(Integer jobId) throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 先刪除JOB相關的TASK
            int ret = jobtaskExtMapper.deleteByJobId(jobId);
            Jobs dJobs = new Jobs();
            dJobs.setJobsJobid(jobId);
            List<Jobs> dt = jobsExtMapper.getDataTableByPrimaryKey(dJobs.getJobsJobid());
            if (!dt.isEmpty()) {
                ret += jobsExtMapper.deleteByPrimaryKey(dJobs);
                // 刪除某筆Job,可能影響到JOBS_SEQ跳號,所以依目前SEQ順序重新Update
                List<Jobs> dtJobs = jobsExtMapper.getJobsByBatchId(dt.get(0).getJobsBatchid());
                Batch dBatch = new Batch();
                dBatch.setBatchZone(null); // 避免原本的欄位被更新掉
                if (!dtJobs.isEmpty()) {
                    for (int i = 0; i < dtJobs.size(); i++) {
                        if (i == 0) {// 第一個job
                            dBatch.setBatchBatchid(dtJobs.get(0).getJobsBatchid());
                            dBatch.setBatchStartjobid(dtJobs.get(0).getJobsJobid());
                        }
                        Jobs jobs = new Jobs();
                        jobs.setJobsJobid(dtJobs.get(i).getJobsJobid());
                        jobs.setJobsSeq(i + 1);
                        ret += jobsExtMapper.updateByPrimaryKeySelective(jobs);
                    }
                } else {
                    dBatch.setBatchBatchid(dt.get(0).getJobsBatchid());
                    dBatch.setBatchStartjobid(0);
                }
                // 刪除某筆Job,可能影響到Batch檔StartJobId,所以一律以目前第一個Job更新Batch檔StartJobId
                ret += batchExtMapper.updateByPrimaryKeySelective(dBatch);
            }
            transactionManager.commit(txStatus);
            return ret;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    public List<Task> getTaskByName(String taskName, String direction) {
        return taskExtMapper.getTaskByName(taskName, direction);
    }

    public Integer updateSelectTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.updateByPrimaryKeySelective(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 新增一筆 Task
     */
    public Integer insertTask(Task defTask) {
        try {
            return taskExtMapper.insertSelective(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 更新一筆 Task
     */
    public Integer updateTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.updateByPrimaryKey(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    /**
     * 刪除一筆 Task
     */
    public Integer deleteTask(Task defTask) {
        try {
            // 回傳的為TaskId
            return taskExtMapper.deleteByPrimaryKey(defTask);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Batch getBatchQueryByPrimaryKey(Batch batch) {
        return batchExtMapper.selectByPrimaryKey(batch.getBatchBatchid());
    }

    public PageInfo<HashMap<String, Object>> getHistoryByInstanceId(Integer batchId, String instanceId, Integer pageNum, Integer pageSize) {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    historyExtMapper.getHistoryById(batchId, instanceId);
                }
            });
            return pageInfo;
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return null;
        }
    }

    public List<Jobs> getJobsByBatchId(Integer batchid) {
        return jobsExtMapper.getJobsByBatchId(batchid);
    }

    public Map<String, Object> getLogByLogFile(String historyLogfile) {
        return historyExtMapper.getLogByLogFile(historyLogfile);
    }

    public PageInfo<Batch> queryScheduledBatchByNameAndSubsys(String batchName, List<String> batchSubsys, Integer pageNum, Integer pageSize) throws Exception {
        try {
            PageInfo<Batch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    batchExtMapper.queryScheduledBatchByNameAndSubsys(batchName, batchSubsys);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Batch> getAllBatchByLastRunTime(String batchName, String sqlSortExpression) throws Exception {
        try {
            return batchExtMapper.getAllBatchByLastRunTime(batchName, sqlSortExpression);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * 2024-04-18 Richard add for 批次設定管理, 在新增模式下新增批次程式
     *
     * @param batch
     * @param tasks
     * @return
     * @throws Exception
     */
    public int insertBatchJobTask(Batch batch, List<MaintainTask> tasks) throws Exception {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 新增Batch檔資料, 同時取到新增後的BATCH_BATCHID
            int ret = insertBatch(batch);
            if (CollectionUtils.isNotEmpty(tasks)) {
                for (int i = 0; i < tasks.size(); i++) {
                    MaintainTask maintainTask = tasks.get(i);
                    Task task = maintainTask.getTask();
                    Jobs jobs = new Jobs();
                    jobs.setJobsBatchid(batch.getBatchBatchid());
                    jobs.setJobsDelay(0);
                    jobs.setJobsDescription(task.getTaskDescription());
                    jobs.setJobsName(task.getTaskName());
                    jobs.setJobsSeq(i + 1);
                    jobs.setJobsStarttaskid(task.getTaskId());
                    jobs.setJobsContinueonfail((short) maintainTask.getJobsContinueonfail());
                    ret += jobsExtMapper.insertSelective(jobs); // 新增Jobs檔資料, 同時取回新增後的JOBS_JOBID
                    if (i == 0) {
                        // 如果是第一筆資料, 則同時要更新Batch檔的BATCH_STARTJOBID
                        batch.setBatchStartjobid(jobs.getJobsJobid());
                        Batch updateBatch = new Batch();
                        updateBatch.setBatchBatchid(jobs.getJobsBatchid());
                        updateBatch.setBatchStartjobid(jobs.getJobsJobid());
                        updateBatch.setBatchZone(null); // 避免原本的欄位被更新掉
                        ret += batchExtMapper.updateByPrimaryKeySelective(updateBatch);
                    }
                    // 新增Jobtask檔資料
                    Jobtask jobtask = new Jobtask();
                    jobtask.setJobtaskJobid(jobs.getJobsJobid());
                    jobtask.setJobtaskTaskid(task.getTaskId());
                    jobtask.setJobtaskStepid((short) 1);
                    jobtask.setJobtaskWaitfortask("0");
                    ret += jobtaskExtMapper.insertSelective(jobtask);
                    // 如果第一筆資料, 則同時要更新Jobs檔的JOBS_STARTTASKID
                    if (i == 0) {
                        Jobs updateJob = new Jobs();
                        updateJob.setJobsJobid(jobtask.getJobtaskJobid());
                        updateJob.setJobsStarttaskid(jobtask.getJobtaskTaskid());
                        ret += jobsExtMapper.updateByPrimaryKeySelective(updateJob);
                    }
                }
            }
            transactionManager.commit(txStatus);
            return ret;
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<HashMap<String, Object>> getAllBatch(List<String> subsysList) throws Exception {
        try {
            return batchExtMapper.getAllBatch(null, subsysList);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateBatchHost(String sourceHost, String targetHost) throws Exception {
        try {
            return batchExtMapper.updateBatchHost(sourceHost, targetHost);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateBatchResult(Integer batchBatchid, String batchResult) throws Exception {
        try {
            return batchExtMapper.updateBatchResult(batchBatchid, batchResult);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }
}
