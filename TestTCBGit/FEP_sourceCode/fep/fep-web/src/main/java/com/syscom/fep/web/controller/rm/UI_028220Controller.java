package com.syscom.fep.web.controller.rm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.syscom.safeaa.utils.ReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Rmbtchmtr;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028220_Form;
import com.syscom.fep.web.form.rm.UI_028220_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 大批匯款回饋監控啟動
 *
 * @author jie
 * @create 2021/11/24
 */
@Controller
public class UI_028220Controller extends BaseController {

    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028220_Form form = new UI_028220_Form();
        form.setUrl("/rm/UI_028220/queryClick");
        this.queryClick(form, mode);
    }

    @PostMapping(value = "/rm/UI_028220/queryClick")
    public String queryClick(@ModelAttribute UI_028220_Form form, ModelMap mode) {
        this.infoMessage("大批匯款回饋監控啟動");
        this.doKeepFormData(mode, form);
        logContext.setRemark(StringUtils.join("UI_028220查詢"));
        logMessage(Level.INFO, logContext);
        Boolean hasFile = false;
        // '匯出
        try {
            Rmbtchmtr defRMBTCHMTR = new Rmbtchmtr();
            defRMBTCHMTR.setRmbtchmtrRemdate(
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRMBTCHMTR.setRmbtchmtrFlag("1");// 1 已回饋
            PageInfo<HashMap<String, Object>> dtResult = rmService.getRMBTCHMTRbyDef(defRMBTCHMTR, form.getPageNum(),
                    form.getPageSize());
            if (dtResult == null || dtResult.getSize() == 0) {
                logContext.setRemark(StringUtils.join("大批匯款回饋主檔(RMBTCHMTR)無資料"));
                logMessage(Level.INFO, logContext);
                this.showMessage(mode, MessageType.DANGER, "大批匯款回饋主檔(RMBTCHMTR)無資料");
            } else {
                hasFile = true;
            }

            if (hasFile) {
                PageData<UI_028220_Form, HashMap<String, Object>> pageData = new PageData<>(dtResult, form);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            } else {
                this.showMessage(mode, MessageType.DANGER, "FEP RMBTCHMTR無任何資料需要重新回饋");
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            this.showMessage(mode, MessageType.DANGER, ex.toString());
        }
        return Router.UI_028220.getView();
    }

    // Gridview 第一列查詢單筆明細
    @PostMapping(value = "/rm/UI_028220/inquiryDetail")
    public String doInquiryDetail(@ModelAttribute UI_028220_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        WebUtil.putInAttribute(mode, AttributeName.DetailEntity, form);
        return Router.UI_028220_Detail.getView();
    }

    @PostMapping(value = "/rm/UI_028220/btnComit", produces = "application/json;charset=utf-8")
    public String btnComit(@ModelAttribute UI_028220_FormDetail form, ModelMap mode) {
        this.infoMessage("大批匯款回饋監控啟動");
        this.doKeepFormData(mode, form);
        BaseResp<UI_028220_FormDetail> response = new BaseResp<>();
        logContext.setRemark(StringUtils.join("確認"));
        logMessage(Level.INFO, logContext);

        // '匯出
        try {
            String batchName = "RM_SyncOutBatch";
            List<HashMap<String, Object>> dtResult = rmService.getAllBatch(batchName);
            if (dtResult == null || dtResult.size() == 0) {
                logContext.setRemark(StringUtils.join("批次平台找不到批次名稱為", batchName, "的資料!"));
                logMessage(Level.INFO, logContext);
                this.showMessage(mode, MessageType.DANGER, "批次平台找不到批次名稱為" + batchName + "的資料!");
            } else {
                logContext.setRemark(StringUtils.join("GetBatchID count =", dtResult.size()));
                logMessage(Level.INFO, logContext);
                // 2025-01-21 Richard modified start for 【DoS by Sleep】
                // String par = " /REMDATE:" + form.getRmbtchmtrRemDate() + " /SDN:" + form.getRmbtchmtrSdn() + " /BATCHNO:" + form.getRmbtchmtrTimes();
                String par = " /REMDATE:" + ReflectUtil.getFieldValue(form, "rmbtchmtrRemDate", StringUtils.EMPTY)
                        + " /SDN:" + ReflectUtil.getFieldValue(form, "rmbtchmtrSdn", StringUtils.EMPTY)
                        + " /BATCHNO:" + ReflectUtil.getFieldValue(form, "rmbtchmtrTimes", StringUtils.EMPTY);
                // 2025-01-21 Richard modified end for 【DoS by Sleep】
                logContext.setRemark(
                        StringUtils.join("StartBatch BATCHID =", dtResult.get(0).get("BATCH_BATCHID"), " STARTJOBID= ", dtResult.get(0).get("BATCH_STARTJOBID"), " Parameters= ", par.toString()));
                logMessage(Level.INFO, logContext);
                BatchJobLibrary batchLib = new BatchJobLibrary();
                batchLib.startBatch(
                        dtResult.get(0).get("BATCH_EXECUTE_HOST_NAME").toString(),
                        dtResult.get(0).get("BATCH_BATCHID").toString(),
                        dtResult.get(0).get("BATCH_STARTJOBID").toString(),
                        par.toString());
                logContext.setRemark(StringUtils.join("大批匯款回饋檔監控啟動成功", form.getRmbtchmtrTimes()));
                logMessage(Level.INFO, logContext);
                this.showMessage(mode, MessageType.SUCCESS, "大批匯款回饋檔監控啟動成功");
                prepareAndSendEMSData("傳送成功" + form.getRmbtchmtrTimes());
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            response.setMessage(MessageType.DANGER, programError);
        }
        return Router.UI_028220_Detail.getView();
    }

    private void prepareAndSendEMSData(String strMsg) throws Exception {
        LogData logContext = new LogData();
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028220");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028220");
        logContext.setMessageGroup("4");
        logContext.setRemark(strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.SendRMBTCHBackTele);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);

    }
}
