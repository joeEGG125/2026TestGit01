package com.syscom.fep.batch.invoker;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.batch.configurer.BatchConfiguration;
import com.syscom.fep.batch.job.BatchJobContext;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Bsdays;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 透过Quartz API执行BatchJobInvoker程式, 會呼叫這支程式
 *
 * @author Richard
 */
@Component(BatchJobLibrary.BATCH_INVOKER_NAME)
public class JobSchedulerInvoker extends FEPBase {
    @Autowired
    private BatchConfiguration configuration;
    @Autowired
    private BatchExtMapper batchMapper;
    @Autowired
    private BsdaysExtMapper bsdaysMapper;
    @Autowired
    private FEPConfig fepConfig;

    /**
     * 呼叫批次程式
     *
     * @param logData
     * @param batchJobContext
     */
    public void invoke(LogData logData, BatchJobContext batchJobContext) {
        logData.setSubSys(SubSystem.CMN);
        logData.setChannel(FEPChannel.BATCH);
        logData.setProgramName(StringUtils.join(ProgramName, ".invoke"));
        logData.setMessage(batchJobContext.getInstanceId());
        logData.setRemark("BatchJobSchedulerInvoker enter");
        logMessage(Level.INFO, logData);
        try {
            String[] args = batchJobContext.getActionArguments().split("\\s+");
            Map<String, String> arguments = this.extractBatchParameter(args);
            int batchId = Integer.parseInt(arguments.get("batchid"));
            Batch batch = batchMapper.selectByPrimaryKey(batchId);
            if (batch != null) {
                // // 批次平台在接收批次啟動要求時,如果BATCH_EXECUTE_HOST_NAME欄位是空值(不指定), 或符合本機的計算機名稱, 才執行此批次
                // if (StringUtils.isNotBlank(batch.getBatchExecuteHostName()) && !fepConfig.getHostName().equals(batch.getBatchExecuteHostName())) {
                //     logData.setMessage(batchJobContext.getInstanceId());
                //     logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Cancel run Batch because Batch Execute HostName Inconsistently, ", batchJobContext.getScheduleInfo()));
                //     logMessage(Level.WARN, logData);
                //     return;
                // }
                // // 檢核可執行的系統別
                // if (batch.getBatchSubsys() != null && !configuration.getSubSysList().contains(batch.getBatchSubsys().toString())) {
                //     logData.setMessage(batchJobContext.getInstanceId());
                //     logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Cancel run Batch because Subsys is not allowed, ", batchJobContext.getScheduleInfo()));
                //     logMessage(Level.WARN, logData);
                // }
                // // 檢核批次是否啟用
                // if (!DbHelper.toBoolean(batch.getBatchEnable())) {
                //     logData.setMessage(batchJobContext.getInstanceId());
                //     logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Cancel run Batch because Batch is disabled, ", batchJobContext.getScheduleInfo()));
                //     logMessage(Level.WARN, logData);
                //     return;
                // }
                // // 檢核是否營業日才執行
                // if (DbHelper.toBoolean(batch.getBatchCheckbusinessdate())) {
                //     if (!this.checkBusinessDate(batch.getBatchZone())) {
                //         logData.setMessage(batchJobContext.getInstanceId());
                //         logData.setRemark(StringUtils.join("BatchJobService Cancel Run by CheckBusinessDay fail, ", batchJobContext.getScheduleInfo()));
                //         logMessage(Level.WARN, logData);
                //         return;
                //     }
                // }
                // // 檢核是否允許重覆執行
                // Short batchDenyconcurrentexec = batch.getBatchDenyconcurrentexec();
                // if (DbHelper.toBoolean(batchDenyconcurrentexec)) {
                //     String batchResult = batch.getBatchResult();
                //     // 判斷BatchResult若仍為執行中則不執行
                //     if ("0".equals(batchResult)) {
                //         logData.setMessage(batchJobContext.getInstanceId());
                //         logData.setRemark(StringUtils.join("BatchJobService Cancel Run because deny concurrent execute, ", batchJobContext.getScheduleInfo()));
                //         logMessage(Level.WARN, logData);
                //         return;
                //     }
                // }
                // // 一天只能做一次且今天已做過則不做直接離開
                // if (DbHelper.toBoolean(batch.getBatchSingletime())
                //         && (batch.getBatchLastruntime() != null && CalendarUtil.equals(batch.getBatchLastruntime(), Calendar.getInstance().getTime(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN))
                //         && "1".equals(batch.getBatchResult())) {
                //     logData.setMessage(batchJobContext.getInstanceId());
                //     logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Cancel run Batch because already run once, ", batchJobContext.getScheduleInfo()));
                //     logMessage(Level.WARN, logData);
                //     return;
                // }
                logData.setMessage(batchJobContext.getInstanceId());
                logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Ready to start Batch, ", batchJobContext.getScheduleInfo()));
                logMessage(Level.INFO, logData);
                BatchJobLibrary batchLib = new BatchJobLibrary(logData);
                String instanceId = batchLib.startBatch(
                        fepConfig.getHostName(),
                        arguments.get("batchid"),
                        arguments.get("jobid"));
                batchJobContext.setInstanceId(instanceId); // 塞入instanceId
                logData.setMessage(batchJobContext.getInstanceId());
                logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Start Batch OK, ", batchJobContext.getScheduleInfo()));
                logMessage(Level.INFO, logData);
            } else {
                throw ExceptionUtil.createException("Batch Data Not Exist, batchId:", batchId);
            }
        } catch (Exception e) {
            logData.setMessage(batchJobContext.getInstanceId());
            logData.setProgramException(e);
            logData.setRemark(StringUtils.join("BatchJobSchedulerInvoker Start Batch Failed, ", batchJobContext.getScheduleInfo()));
            sendEMS(logData);
        } finally {
            logData.setSubSys(SubSystem.CMN);
            logData.setChannel(FEPChannel.BATCH);
            logData.setProgramName(StringUtils.join(ProgramName, ".invoke"));
            logData.setMessage(batchJobContext.getInstanceId());
            logData.setRemark("BatchJobSchedulerInvoker exit");
            logMessage(Level.INFO, logData);
        }
    }

    private boolean checkBusinessDate(String zone) {
        try {
            Bsdays bsdays = bsdaysMapper.selectByPrimaryKey(zone, Integer.toString(CalendarUtil.dateValue(Calendar.getInstance())));
            if (bsdays != null) {
                return DbHelper.toBoolean(bsdays.getBsdaysWorkday());
            }
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        return false;
    }

    private Map<String, String> extractBatchParameter(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (String arg : args) {
            int index = arg.indexOf(":");
            if (index > 1) {
                String key = arg.substring(1, index).trim();
                String value = arg.substring(index + 1).trim();
                arguments.put(key, value);
            }
        }
        LogHelperFactory.getTraceLogger().debug("[", ProgramName, "][extractBatchParameter]arguments=", arguments);
        return arguments;
    }
}
