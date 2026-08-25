package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.mybatis.model.Fundlog;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_015313_Form;
import com.syscom.fep.web.form.inbk.UI_015313_FormDetail;
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

/**
 * 減少跨行基金餘額-放行-5313
 *
 * @author ChenYu
 */

@Controller
public class UI_015313Controller extends BaseController {

    String queryFlagTxt = "";

    @Autowired
    public InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_015313_Form form = new UI_015313_Form();

        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_015313/inquiryMain")
    @ResponseBody
    public UI_015313_Form getFundlog(@RequestBody UI_015313_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);

        form.setQueryFlagTxt("");
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getFgSeqno())) {
                auditLog.addParam("調撥序號", form.getFgSeqno());
            }
            setAuditLog(auditLog);

            Fundlog afundlog = inbkService.getFundlogByFgSeqno(form.getFgSeqno());

            if (afundlog == null ){
                form.setMessage(QueryNoData);
                form.setMessageType(MessageType.INFO);
                form.setResult("Fail");
            }else {
                form.setTxDateTxt(afundlog.getFundlogTxDate());
                form.setFgAmt(afundlog.getFundlogFgAmt().toString());
                form.setStanTxt(afundlog.getFundlogStan());
                if ("Y".equals(afundlog.getFundlogStatus()) || "P".equals(afundlog.getFundlogStatus()) || "N".equals(afundlog.getFundlogStatus())) {
                    form.setSelectedValue(afundlog.getFundlogStatus());
                }else {
                    form.setSelectedValue("");
                }
                form.setTlridTxt(afundlog.getFundlogTlrno());
                form.setSupIDTxt(WebUtil.getUser().getUserId());
                form.setQueryFlagTxt("True");
                form.setResult("Success");
            }
        }catch (Exception ex){
            form.setMessage(ex.getMessage());
            form.setMessageType(MessageType.DANGER);
            form.setResult("Fail");
            return form;
        }
        return form;
    }

    @PostMapping(value = "/inbk/UI_015313/inquiryDetail")
    @ResponseBody
    public BaseResp<?> execute(@RequestBody UI_015313_FormDetail form, ModelMap mode) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        INBKGeneral aData = new INBKGeneral();

        try {
            if (!"True".equals(form.getQueryFlagTxt())) {
                response.setMessage(MessageType.DANGER, PleaseDoQuery);
                return response;
            }

            AuditLog auditLog = new AuditLog();
            auditLog.setAction("確認");
            if (StringUtils.isNotBlank(form.getFgSeqno())) {
                auditLog.addParam("調撥序號", form.getFgSeqno());
            }
            setAuditLog(auditLog);

            aData.getRequest().setFGSEQNO(form.getFgSeqno());
            aData.getRequest().setSUPID(WebUtil.getUser().getUserId());

            //add by Maxine on 2011/09/02 for EMS加UserId
            //modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
            String txuser = WebUtil.getUser().getUserId();

//            this.infoMessage("Start to Call AA via FISCHandler.dispatch(FEPChannel.FEP, FISCGeneral) by condition = ", form.toString());
            FISCHandler fiscHandler = new FISCHandler();
            fiscHandler.dispatch(FEPChannel.FEP, aData,"531300", txuser);

            form.setTxDateTxt(aData.getResponse().getTXDATE());
            form.setFgAmt(String.valueOf(aData.getResponse().getFGAMT()));
            form.setStanTxt(aData.getResponse().getSTAN());
            if ("Y".equals(aData.getResponse().getSTATUS()) || "P".equals(aData.getResponse().getSTATUS()) || "N".equals(aData.getResponse().getSTATUS())) {
                form.setAtmWorkStatus(aData.getResponse().getSTATUS());
            }else {
                form.setAtmWorkStatus("");
            }
            form.setTlridTxt(aData.getResponse().getTLRID());
            form.setSupIDTxt(aData.getResponse().getSUPID());

            //modify by henny 20110317 for error code
            //將AA RC 顯示在UI上
            if (aData.getResponse().getRESULT() == null || StringUtils.isBlank(aData.getResponse().getRESULT())){
                aData.getResponse().setRESULT(MessageError);
            }
            String[] message = aData.getResponse().getRESULT().split("[-]", -1);
            if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {  //若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
                response.setMessage(MessageType.SUCCESS, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
            }else {
                response.setMessage(MessageType.DANGER, aData.getResponse().getRESULT());
            }
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }
}
