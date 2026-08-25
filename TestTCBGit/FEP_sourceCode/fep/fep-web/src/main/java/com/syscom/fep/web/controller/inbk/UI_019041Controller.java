package com.syscom.fep.web.controller.inbk;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FwdtxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FwdtxnExt;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019041_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 預約轉帳整批重發處理
 *
 * @author xingyun_yang
 * @create 2021/8/25
 */
@Controller
public class UI_019041Controller extends BaseController {

    @Autowired
    FwdtxnExtMapper fwdtxnExtMapper;
    @Autowired
    private InbkService inbkService;
    @Autowired
    private BatchExtMapper batchExtMapper;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019041_Form form = new UI_019041_Form();
        // 系統日期 不可改變
        form.setFwdrstTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        // 初始化失敗筆數 系統 客戶 其他
        mode.addAttribute("failTimes", "0");
        mode.addAttribute("sysFailTimes", "0");
        mode.addAttribute("otherFailTimes", "0");
        mode.addAttribute("custFailTimes", "0");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019041/getFWDTXNByTSBDYFISC")
    public String doInquiryDetail(@ModelAttribute UI_019041_Form form, ModelMap mode) {
        form.setFwdrstTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢");
        // 塞入auditLog.params
        if (StringUtils.isNotBlank(String.valueOf(form.getFwdrstTxDate())))
            auditLog.addParam("預約檔日期", String.valueOf(form.getFwdrstTxDate()));
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        try {
            String fwdrstTxDate = form.getFwdrstTxDate().replace("-", "");
            Short sysFail = 0;
            Integer pageNum = form.getPageNum();
            Integer pageSize = form.getPageSize();
            PageInfo<HashMap<String, Object>> pageInfo = inbkService.getFWDTXNByTSBDYFISC(
                    fwdrstTxDate, "1", "", "", "", "", "", "", sysFail, pageNum, pageSize);
            PageData<UI_019041_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
            int i = 0;
            List<HashMap<String, Object>> fwdtxnandrstExtHashs = new ArrayList<>(pageData.getList().size());
            if (pageData.getList().size() > 0) {
                int tempVar = pageData.getList().size();
                for (i = 0; i < tempVar; i++) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("FWDTXN_TX_DATE", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TX_DATE")));
                    hashMap.put("FWDTXN_TX_ID", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TX_ID")));
                    hashMap.put("FWDRST_TX_ID", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_TX_ID")));
                    hashMap.put("FWDTXN_CHANNEL_S", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_CHANNEL_S")));
                    hashMap.put("FWDRST_TX_DATE", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TX_DATE")));
                    hashMap.put("FWDTXN_PCODE", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_PCODE")));
                    hashMap.put("SYSSTAT_HBKNO", nullToEmptyStr(SysStatus.getPropertyValue().getSysstatHbkno()));
                    hashMap.put("FWDTXN_TROUT_ACTNO", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TROUT_ACTNO")));
                    hashMap.put("FWDTXN_TRIN_BKNO", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TRIN_BKNO")));
                    hashMap.put("FWDTXN_TRIN_ACTNO", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TRIN_ACTNO")));
                    hashMap.put("FWDTXN_TX_AMT", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_TX_AMT")));
                    hashMap.put("FWDRST_RUN_NO", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_RUN_NO")));
                    hashMap.put("FWDRST_EJFNO", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_EJFNO")));
                    hashMap.put("FWDRST_TXRUST", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_TXRUST")));
                    hashMap.put("FWDRST_REPLY_CODE", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_REPLY_CODE")));
                    hashMap.put("FWDRST_ERR_MSG", nullToEmptyStr(pageData.getList().get(i).get("FWDRST_ERR_MSG")));
                    hashMap.put("FWDTXN_RERUN_FG", nullToEmptyStr(pageData.getList().get(i).get("FWDTXN_RERUN_FG")));
                    fwdtxnandrstExtHashs.add(hashMap);
                }
            }
            List<FwdtxnExt> fwdtxnandrstExts =
                    fwdtxnExtMapper.getSummary(fwdrstTxDate, "1", "", "", "", "", "", "", sysFail);
            // 失敗筆數
            int failTimes = 0;
            // 系統原因
            int sysFailTimes = 0;
            // 其它原因(財金)
            int otherFailTimes = 0;
            // 客戶原因(主機)
            int custFailTimes = 0;
            for (FwdtxnExt s : fwdtxnandrstExts) {
                if (!"    ".equals(s.getFwdtxnReplyCode()) && !"0000".equals(s.getFwdtxnReplyCode())) {
                    failTimes++;
                    if (!StringUtils.isBlank(s.getFwdtxnReplyCode())) {
                        if ("EF".equals(s.getFwdtxnReplyCode().substring(0, 2))) {
                            sysFailTimes++;
                        } else if ("EX".equals(s.getFwdtxnReplyCode().substring(0, 2))) {
                            otherFailTimes++;
                        } else {
                            custFailTimes++;
                        }
                    } else {
                        custFailTimes++;
                    }
                }
            }
            if (fwdtxnandrstExtHashs == null || fwdtxnandrstExtHashs.size() == 0) {
                mode.addAttribute("failTimes", "0");
                mode.addAttribute("sysFailTimes", "0");
                mode.addAttribute("otherFailTimes", "0");
                mode.addAttribute("custFailTimes", "0");
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                pageData.setList(fwdtxnandrstExtHashs);
                mode.addAttribute("failTimes", failTimes);
                mode.addAttribute("sysFailTimes", sysFailTimes);
                mode.addAttribute("otherFailTimes", otherFailTimes);
                mode.addAttribute("custFailTimes", custFailTimes);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019041.getView();
    }

    /**
     * 執行重送
     *
     * @param form
     * @return
     */
    @PostMapping(value = "/inbk/UI_019041/queryBatchByName")
    @ResponseBody
    public BaseResp<?> queryBatchByName(@RequestBody UI_019041_Form form) {
        this.infoMessage("開始執行UI_019041, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        // 執行信息
        String result = StringUtils.EMPTY;
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("執行");
        // 塞入auditLog.params
        if (StringUtils.isNotBlank(String.valueOf(form.getFwdrstTxDate())))
            auditLog.addParam("預約檔日期", String.valueOf(form.getFwdrstTxDate()));
        // 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        try {
            String fwdrstTxDate = form.getFwdrstTxDate().replace("-", "");
            String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH).replace("-", "");
            if (!fwdrstTxDate.equals(date)) {
                result = "預約日期=系統日期, 才可執行預約跨轉整批重發";
                response.setMessage(MessageType.DANGER, result);
                return response;
            }
            BatchJobLibrary batchLib = new BatchJobLibrary();
            List<Batch> dt = batchExtMapper.queryBatchByName("INBK_RETFR_RERUN");
            if (dt.size() > 0) {
                result = StringUtils.join("啟動批次", dt.get(0).getBatchBatchid(), "-INBK_RETFR_RERUN成功!");
                response.setMessage(MessageType.SUCCESS, result);
                batchLib.startBatch(
                        dt.get(0).getBatchExecuteHostName(),
                        dt.get(0).getBatchBatchid().toString(),
                        dt.get(0).getBatchStartjobid().toString(),
                        "");
                // 執行預約跨轉單筆重發處理的記錄
                LogData logContext = new LogData();
                logContext.setChannel(FEPChannel.FEP);
                logContext.setSubSys(SubSystem.INBK);
                logContext.setProgramName("UI_019041");
                logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                logContext.setMessage("UI019041");
                logContext.setTxUser(WebUtil.getUser().getUserId());
                logContext.setRemark(TxHelper.getRCFromErrorCode(FEPReturnCode.RETFRReRun, FEPChannel.FEP, logContext));
                logMessage(Level.INFO, logContext);
            } else {
                result = "查詢不到批次定義, 批次啟動失敗!";
                response.setMessage(MessageType.DANGER, result);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }
}
