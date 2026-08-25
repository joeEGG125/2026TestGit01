package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_018300_Form;
import com.syscom.fep.web.service.BatchService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跨行系統營運資料補遺
 * UI_018300
 *
 * @author Joseph
 * @create 2023/01/18
 */
@Controller
public class UI_018300Controller extends BaseController {
    @Autowired
    private BatchExtMapper batchExtMapper;
    @Autowired
    BatchService batchService;
    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_018300_Form form = new UI_018300_Form();
        form.setSdatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_SLASH));
        form.setEdatetime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_SLASH));
        Calendar ndate = Calendar.getInstance(); //結束time
        ndate.add(Calendar.MINUTE, -5);
        form.setEtime(FormatUtil.dateTimeFormat(ndate, FormatUtil.FORMAT_TIME_HH_MM_SS));
        Calendar ndate2 = Calendar.getInstance(); //起始time
        ndate2.add(Calendar.MINUTE, -35);
        form.setStime(FormatUtil.dateTimeFormat(ndate2, FormatUtil.FORMAT_TIME_HH_MM_SS));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_018300/Confirm018300")
    public String Confirm018300(@ModelAttribute UI_018300_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        String batchName = "FISCRecovery"; // 2024-12-19 調整呼叫的批次名稱
        String result = StringUtils.EMPTY;
        try {
            String sDate = form.getSdatetime().replace("/", "");
            String eDate = form.getEdatetime().replace("/", "");
            String sTime = form.getStime().trim().replace(":", "");
            String eTime = form.getEtime().trim().replace(":", "");
            if (Double.valueOf(sDate + sTime) > Double.valueOf(eDate + eTime)) {
                this.showMessage(mode, MessageType.WARNING, "起的日期時間" + form.getSdatetime() + form.getStime() + "> 迄的日期時間" + form.getEdatetime() + form.getEtime() + ", 請重新輸入日期時間");
                return Router.UI_018300.getView();
            }
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(String.valueOf(form.getSdatetime())))
                auditLog.addParam("交易日期時間起Date", String.valueOf(form.getSdatetime()));
            if (StringUtils.isNotBlank(String.valueOf(form.getStime())))
                auditLog.addParam("交易日期時間起Time", String.valueOf(form.getStime()));
            if (StringUtils.isNotBlank(String.valueOf(form.getEdatetime())))
                auditLog.addParam("交易日期時間迄Date", String.valueOf(form.getEdatetime()));
            if (StringUtils.isNotBlank(String.valueOf(form.getEtime())))
                auditLog.addParam("交易日期時間迄Time", String.valueOf(form.getEtime()));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            BatchJobLibrary batchLib = new BatchJobLibrary();
            List<Batch> dt = batchExtMapper.queryBatchByName(batchName);
            if (dt.size() > 0) {
                result = StringUtils.join("啟動批次", dt.get(0).getBatchBatchid(), "-INBK_RETFR_RERUN成功!");
                this.showMessage(mode, MessageType.SUCCESS, result);
                List<Task> list = batchService.getTaskByName("FISCRecovery","ASC");
                Task task = new Task();
                if (list == null){
                    this.showMessage(mode, MessageType.DANGER, "Task檔案資料不存在");
                    return Router.UI_018300.getView();
                }else {
                    task.setTaskId(list.get(0).getTaskId());
                }
                task.setTaskCommandargs("/TXDATE_B:"+sDate+" /TXDATE_E:"+eDate+" /TXTIME_B:"+sTime+" /TXTIME_E:"+eTime);
                task.setTaskName(null);
                task.setTaskCommand(null);
                batchService.updateSelectTask(task);
                Map<String, String> arguments = new HashMap<>();
                arguments.put("TXDATE_B",sDate);
                arguments.put("TXDATE_E",eDate);
                arguments.put("TXTIME_B",sTime);
                arguments.put("TXTIME_E",eTime);
                batchLib.setArguments(arguments);
                batchLib.startBatch(
                        dt.get(0).getBatchExecuteHostName(),
                        dt.get(0).getBatchBatchid().toString(),
                        dt.get(0).getBatchStartjobid().toString());
                // 執行預約跨轉單筆重發處理的記錄
                LogData logContext = new LogData();
                logContext.setChannel(FEPChannel.FEP);
                logContext.setSubSys(SubSystem.INBK);
                logContext.setProgramName("UI_018300");
                logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                logContext.setMessage("UI018300");
                logContext.setTxUser(WebUtil.getUser().getUserId());
                logContext.setRemark(TxHelper.getRCFromErrorCode(FEPReturnCode.RETFRReRun, FEPChannel.FEP, logContext));
                logMessage(Level.INFO, logContext);
                sendEMS(logContext);
            } else {
                result = "查詢不到批次定義, 批次啟動失敗!";
                this.showMessage(mode, MessageType.DANGER, result);
            }
        } catch (Exception e) {
            this.errorMessage(e, "批次啟動失敗, ", e.getMessage());
            this.showMessage(mode, MessageType.DANGER, "批次啟動失敗, ", programError);
            // 停留在當前頁
        }
        return Router.UI_018300.getView();
    }
}
