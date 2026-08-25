package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_012290_Form;
import com.syscom.fep.web.form.inbk.UI_012290_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 請求傳送交易結果-2290(前營業日)
 *
 * @author ChenYu
 */
@Controller
public class UI_012290Controller extends BaseController {

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_012290_Form form = new UI_012290_Form();
        // 交易日期
        form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));

        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_012290/inquiryMain")
    public String getpendingDateStanBkno(@ModelAttribute UI_012290_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);

        FISCData aData = new FISCData();
        FISC obj;
        FEPReturnCode rtn = FEPReturnCode.Normal;
        aData.setTxObject(new FISCGeneral());
        aData.setMessageID("2290");
        obj = new FISC(aData);
        obj.setFeptxn(new FeptxnExt());

        try {
            form.setQueryflagtxt("");
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(form.getBkno()))
                auditLog.addParam("財金STAN:bkno", form.getBkno());
            if (StringUtils.isNotBlank(form.getStan()))
                auditLog.addParam("財金STAN:stan", form.getStan());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            rtn = obj.searchFeptxn(form.getTradingDate().replace("-",""),form.getBkno().trim(),StringUtils.leftPad(form.getStan(),7,"0"));
            if (rtn != FEPReturnCode.Normal){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                form.setQueryflagtxt("True");
            }else {
                if ( !form.getTradingDate().replace("-","").equals(obj.getFeptxn().getFeptxnTxDate()) || 1 != obj.getFeptxn().getFeptxnSubsys() ||
                   1 != obj.getFeptxn().getFeptxnPending() || !"4001".equals(obj.getFeptxn().getFeptxnRepRc()) 
                   || !StringUtils.equals(obj.getFeptxn().getFeptxnCbsProc(), "N")) {
                    this.showMessage(mode, MessageType.INFO, QueryNoData);
                    form.setQueryflagtxt("True");
//                    WebUtil.putInAttribute(mode, AttributeName.DetailEntity, obj.getFeptxn());
                }else {
                    form.setQueryflagtxt("True");
                    mode.put("queryflagtxt",form.getQueryflagtxt());
                    WebUtil.putInAttribute(mode, AttributeName.DetailEntity, obj.getFeptxn());
                    this.showMessage(mode, MessageType.INFO, QuerySuccess);
                }
            }
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER,programError);
//            this.showMessage(mode, MessageType.DANGER,ex.getMessage());
        }

        return Router.UI_012290.getView();
    }

    @PostMapping(value = "/inbk/UI_012290/inquiryDetail")
    @ResponseBody
    public BaseResp execute(@RequestBody UI_012290_FormDetail form, ModelMap mode){
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp response = new BaseResp();

        try{
            if ( !"True".equals(form.getQueryflagtxt())) {
                response.setMessage(MessageType.DANGER, PleaseDoQuery);
                return response;
            }
            FISCGeneral aData = new FISCGeneral();
            aData.setINBKRequest(new FISC_INBK());
            aData.setSubSystem(FISCSubSystem.INBK);
            aData.getINBKRequest().setMessageKind(MessageFlow.Request);
            aData.getINBKRequest().setProcessingCode("2290");
            aData.getINBKRequest().setMessageType("0200");
            aData.getINBKRequest().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(form.getTradingDate().replace("-","")) +
                    new SimpleDateFormat("HHmm").format(new Date()));
            aData.getINBKRequest().setTxnSourceInstituteId(StringUtils.rightPad(form.getBkno().trim(),7,"0"));
            aData.getINBKRequest().setSystemTraceAuditNo(StringUtils.leftPad(form.getStan(),7,"0"));
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(form.getBkno()))
                auditLog.addParam("財金STAN:bkno", form.getBkno());
            if (StringUtils.isNotBlank(form.getStan()))
                auditLog.addParam("財金STAN:stan", form.getStan());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            // Call AA
            this.infoMessage("Start to Call AA via FEPHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
            FEPHandler fepHandler = new FEPHandler();
            fepHandler.dispatch(FEPChannel.FISC, aData);
            // 將AA RC 顯示在UI上
            if (StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription(MessageError);
            }
            String[] message = aData.getDescription().split("-");
            // 若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
            if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
                response.setMessage(MessageType.INFO, aData.getDescription());
            } else {
                response.setMessage(MessageType.DANGER, aData.getDescription());
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER,programError);
//            response.setMessage(MessageType.DANGER, ex.getMessage());
        }
        return response;
    }
}
