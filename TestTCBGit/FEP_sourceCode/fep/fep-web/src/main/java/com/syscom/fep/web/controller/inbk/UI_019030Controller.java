package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_EMVIC;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019030_Form;
import com.syscom.fep.web.form.inbk.UI_019030_FormDetail;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 代理交易人工補 Confirm
 *
 * @author ChenYu
 */
@Controller
public class UI_019030Controller extends BaseController {
    @Autowired
    public InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_019030_Form form = new UI_019030_Form();
        // 交易日期
        form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        form.setQueryok("");
        mode.addAttribute("checked","4001");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019030/inquiryMain")
    public String gettradingdateEjno(@ModelAttribute UI_019030_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        mode.addAttribute("checked","0501");
        Feptxn aFeptxn = new FeptxnExt();
        FEPReturnCode rc;
        String message;
        try{
            form.setQueryok("");
            // 交易日期
            aFeptxn.setFeptxnTxDate(form.getTradingDate().replace("-", ""));
            // EJ序號
            aFeptxn.setFeptxnEjfno(form.getEjnotxt());
            // 記錄auditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(String.valueOf(form.getEjnotxt())))
                auditLog.addParam("EJ序號", String.valueOf(form.getEjnotxt()));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            //檔名SEQ為 UI_交易日期[7:2]
            aFeptxn = inbkService.getFeptxnByPk(aFeptxn,SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6,8));
            if (aFeptxn == null){
                //下午4點以前
                if ( SysStatus.getPropertyValue().getSysstatLbsdyFisc().equals(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH)) &&
                        Double.valueOf(new SimpleDateFormat("HHmm").format(new Date()))<= 1600) {
                    aFeptxn = new FeptxnExt();
                    aFeptxn.setFeptxnTxDate(form.getTradingDate());
                    aFeptxn.setFeptxnEjfno(form.getEjnotxt());
                    //檔名SEQ為 BSDAYS_NBSDY[7:2]
                    aFeptxn = inbkService.getFeptxnByPk(aFeptxn,SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6,8));;
                    if (aFeptxn == null){
                        this.showMessage(mode,MessageType.INFO,"FEPTXN " + QueryNoData);
                       return Router.UI_019030.getView();
                    }
                }else {
                    this.showMessage(mode,MessageType.INFO,"FEPTXN " + QueryNoData);
                    return Router.UI_019030.getView();
                }
            }
            
            //只查詢3-2的判斷
            if(!StringUtils.equals(aFeptxn.getFeptxnCbsProc(), "N")) {
            	this.showMessage(mode,MessageType.INFO,"FEPTXN " + QueryNoData);
                return Router.UI_019030.getView();
            }
            
            if (!"4001".equals(aFeptxn.getFeptxnRepRc()) || StringUtils.isNotBlank(aFeptxn.getFeptxnConRc()) || !SysStatus.getPropertyValue().getSysstatHbkno().equals(aFeptxn.getFeptxnBkno())
                || !"1".equals( String.valueOf(aFeptxn.getFeptxnSubsys())) || !"3".equals( String.valueOf(aFeptxn.getFeptxnWay()))){
                rc = FEPReturnCode.OriginalMessageError;
                message = TxHelper.getMessageFromFEPReturnCode(rc);
                this.showMessage(mode,MessageType.INFO,message);
                return Router.UI_019030.getView();
            }
//            Feptxn bFeptxn = feptxnExtMapper.getFeptxnByPk(aFeptxn);
            //顯示畫面欄位如下:
            form.setQueryok("OK");
            mode.put("querok",form.getQueryok());
            WebUtil.putInAttribute(mode, AttributeName.DetailEntity, aFeptxn);
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER,ex.getMessage());
        }
        return Router.UI_019030.getView();
    }

    @PostMapping(value = "/inbk/UI_019030/excure")
    @ResponseBody
    public BaseResp<?> execute(@RequestBody UI_019030_FormDetail form) {

        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        try{
            if (!"OK".equals(form.getQueryok())){
                response.setMessage(MessageType.DANGER, PleaseDoQuery);
                return response;
            }

            FISCGeneral aData = new FISCGeneral();
            if ("26".equals(form.getFeptxnPcode().substring(0,2))){
                aData.setEMVICRequest(new FISC_EMVIC());
                aData.setSubSystem(FISCSubSystem.EMVIC);
                aData.getEMVICRequest().setProcessingCode("2000");
                aData.getEMVICRequest().setMessageType("0202");
                aData.getEMVICRequest().setMessageKind(MessageFlow.Request);
                aData.getEMVICRequest().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(StringUtils.rightPad(form.getTradingDate().replace("-",""),14,"0")));
                aData.getEMVICRequest().setOriTxDate(StringUtils.rightPad(form.getFeptxnTbsdyFisc(),12,"0"));
                aData.getEMVICRequest().setAuthCode(form.getRcrbl());
                aData.getEMVICRequest().setEj(form.getEjnotxt().intValue());
                //失敗原因
                aData.getEMVICRequest().setResponseCode(form.getAtmrctxt());
                aData.getEMVICRequest().setSyncCheckItem(form.getMsgid()); //借用SyncCheckItem傳msgid給FISCHandler
            }else {
                aData.setINBKRequest(new FISC_INBK());
                aData.setSubSystem(FISCSubSystem.INBK);
                aData.getINBKRequest().setProcessingCode("2000");
                aData.getINBKRequest().setMessageType("0202");
                aData.getINBKRequest().setMessageKind(MessageFlow.Request);
                aData.getINBKRequest().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(StringUtils.rightPad(form.getTradingDate().replace("-",""),14,"0")));
                aData.getINBKRequest().setTxDatetimeFisc(StringUtils.rightPad(form.getFeptxnTbsdyFisc(),12,"0"));  //暫存財金營業日告訴aa查詢那個table
                aData.getINBKRequest().setEj(form.getEjnotxt().intValue());
                aData.getINBKRequest().setRsCode(form.getRcrbl());
                //2014/09/22 Modify by Ruling for 修正發生程式例外的錯誤訊息
                aData.getINBKRequest().setResponseCode(form.getAtmrctxt());
                //aData.INBKRequest.ResponseCode = AA_RC
                aData.getINBKRequest().setSyncCheckItem(form.getMsgid()); //借用SyncCheckItem傳msgid給FISCHandler
            }
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getRcrbl()))
                auditLog.addParam("AuthCode", form.getRcrbl());
            if (StringUtils.isNotBlank(form.getAtmrctxt()))
                auditLog.addParam("失敗原因", form.getAtmrctxt());
            if (StringUtils.isNotBlank(form.getFeptxnTbsdyFisc()))
                auditLog.addParam("財金營業日", form.getFeptxnTbsdyFisc());
            if (StringUtils.isNotBlank(form.getFeptxnPcode()))
                auditLog.addParam("PCODE", form.getFeptxnPcode());
            if (StringUtils.isNotBlank(form.getTradingDate()))
                auditLog.addParam("交易日期", form.getTradingDate());
            if (StringUtils.isNotBlank(String.valueOf(form.getEjnotxt())))
                auditLog.addParam("EJ序號", String.valueOf(form.getEjnotxt()));
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            // Call AA
            this.infoMessage("Start to Call AA via FEPHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
            FEPHandler fepHandler = new FEPHandler();
            fepHandler.dispatch(FEPChannel.FEP, aData);

            //將AA RC 顯示在UI上
            if (StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription(MessageError);
            }
            if (TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal).equals(aData.getDescription())){
                response.setMessage(MessageType.INFO,aData.getDescription());
            }else {
                response.setMessage(MessageType.DANGER,aData.getDescription());
            }
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }

}
