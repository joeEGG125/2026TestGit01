package com.syscom.fep.web.controller.rm;


import com.syscom.fep.server.common.handler.FEPHandler;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.rm.UI_028021_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 通匯上行電文序號查詢
 * @author  Chen_yu
 * @create 2021/10/09
 */
@Controller
public class UI_028021Controller extends BaseController {

    @Autowired
    RmService rmService;

    FISCGeneral FISCData = new FISCGeneral();

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028021_Form form = new UI_028021_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028021/queryClick")
    @ResponseBody
    public BaseResp<UI_028021_Form> queryClick(@RequestBody UI_028021_Form form) {
        this.infoMessage("查詢明細數據, 條件 = [", form, "]");
        BaseResp<UI_028021_Form> response = new BaseResp<>();

//        @SuppressWarnings("unused")
//        RmService obj = new RmService();
//        @SuppressWarnings("unused")
//        boolean isSendToAA = false;
//        @SuppressWarnings("unused")
//        String fiscno = "";
        try {
            getLogContext().setRemark("UI_028021查詢, 查詢方式=" + form.getOrgpcodeddl());
            logMessage(Level.DEBUG, logContext);
            sendToAA(form,response);
        }catch (Exception ex) {
            response.setMessage(MessageType.DANGER,programError);
        }
        return response;
    }

    protected void sendToAA(UI_028021_Form form, BaseResp<UI_028021_Form> response) {
        FEPHandler fepHandler = new FEPHandler();
        try {
            FISCData.setRMRequest(new FISC_RM());
            FISCData.getRMRequest().setProcessingCode("1513");
            FISCData.getRMRequest().setMessageKind(MessageFlow.Request);

            FISCData.setSubSystem(FISCSubSystem.RM);
            FISCData.getRMRequest().setMessageType("0200");
            FISCData.getRMRequest().setOrgPcode(form.getOrgpcodeddl());

            getLogContext().setRemark("UI_028021, start to call AA1513");
            logMessage(Level.DEBUG,getLogContext());
            fepHandler.dispatch(FEPChannel.FEP,FISCData);

            getLogContext().setRemark("UI_028021, after AA1513, result = " + FISCData.getDescription().trim());
            logMessage(Level.DEBUG,getLogContext());
            if ( NormalRC.External_OK.equals(FISCData.getDescription().trim()) && FISCData.getRMResponse() != null) {
                //組ReturnCode
                form.setOrgfiscno(FISCData.getRMResponse().getFiscNo());
                form.setOrgpcode(FISCData.getRMResponse().getOrgPcode());
                form.setOrgreceiverbank(FISCData.getRMResponse().getReceiverBank());
                form.setOrgrmsno(FISCData.getRMResponse().getBankNo());
                form.setOrgstan(FISCData.getRMResponse().getOrgStan());
                //Me.O_ORG_TX_DATETIME.Text = FISCData.RMResponse.ORG_TX_DATETIME
                form.setOrgtxamt(FISCData.getRMResponse().getTxAmt());
                response.setData(form);
                response.setMessage(MessageType.INFO, WebCodeConstant.DealSuccess);
            }else {
                form.setOrgfiscno("");
                form.setOrgpcode("");
                form.setOrgreceiverbank("");
                form.setOrgrmsno("");
                form.setOrgstan("");
                form.setOrgtxamt("");
                response.setData(form);
                response.setMessage(MessageType.DANGER,FISCData.getDescription());
            }
        }catch (Exception ex) {
            response.setMessage(MessageType.DANGER,programError);
        }
    }
}
