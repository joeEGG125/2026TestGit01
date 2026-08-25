package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.inbk.UI_013101_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 應用系統連線作業-3101
 * @author  Chen_yu
 * @create 2021/10/18
 */
@Controller
public class UI_013101Controller extends BaseController {
    @Autowired
    private InbkService inbkService;
    @Autowired
    private AtmService atmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_013101_Form form = new UI_013101_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_013101/confirmClick")
    @ResponseBody
    public BaseResp<UI_013101_Form> queryClick(@RequestBody UI_013101_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();

        FISCGeneral aData = new FISCGeneral();
        FEPHandler fepHandler = new FEPHandler();
        String[] message;
        LogData logContext = new LogData();
        InbkService obj = new InbkService();
        try {
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            if (StringUtils.isNotBlank(form.getApidddl())) {
                auditLog.addParam("選擇AP-ID", form.getApidddl());
            }
            if (StringUtils.isNotBlank(form.getCurlbl())) {
                auditLog.addParam("幣別", form.getCurlbl());
            }
            setAuditLog(auditLog);

            getLogContext().setProgramName("UI_013101");
            aData.setOPCRequest(new FISC_OPC());
            aData.setSubSystem(FISCSubSystem.OPC);
            aData.getOPCRequest().setMessageKind(MessageFlow.Request);
            aData.getOPCRequest().setProcessingCode("3101");
            aData.getOPCRequest().setMessageType("0600");
            aData.getOPCRequest().setAPID(form.getApidddl());

//            if ("1600".equals(form.getApidddl())) {
//                aData.getOPCRequest().setCUR(form.getCurlbl());
//            }

            //add by Maxine on 2011/09/02 for EMS加UserId
            aData.getOPCRequest().setLogContext(new LogData());
            //modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
            aData.getOPCRequest().getLogContext().setTxUser(WebUtil.getUser().getUserId());

            //Call AA
            fepHandler.dispatch(FEPChannel.FEP, aData);

            //將AA RC 顯示在UI上
            if (StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription(MessageError);
            }
             message = aData.getDescription().split("-");
            //若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
            if (message != null && message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
                //Jim, 2012/8/21, 同步EAINET
                FISC txFISCBusiness = new FISC();
                txFISCBusiness.setLogContext(logContext);
                FEPReturnCode rtnCode = txFISCBusiness.updateEAINETForCheckInOut("Y");
                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("AA執行成功，更新EAINET失敗");
                    getLogContext().setReturnCode(rtnCode);
                    obj.inbkLogMessage(Level.INFO, logContext);
                }
                response.setMessage(MessageType.SUCCESS, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
            }else {
                response.setMessage(MessageType.DANGER, aData.getDescription());
            }
            getData();
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            response.setMessage(MessageType.DANGER, programError);
        }
        return (BaseResp<UI_013101_Form>) response;
    }

    private void getData() throws Exception{

        //Dim defSYSSTAT As New Tables.DefSYSSTAT
//        Sysstat dtSysstat = new Sysstat();
//        Fcrmstat defFcrmstat = new Fcrmstat();
            //defSYSSTAT = SysStatus.PropertyValue
            atmService.getStatus();
            inbkService.getFCRMSTAT();

//            if (dtSysstat!=null) {
//
//            }
    }
}
