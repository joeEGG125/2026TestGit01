package com.syscom.fep.web.controller.rm;

import java.util.Calendar;
import java.util.List;

import com.syscom.fep.server.common.handler.FEPHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.rm.UI_028022_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 通匯下行電文序號查詢
 *
 * @author xingyun_yang
 * @create 2021/9/23
 */
@Controller
public class UI_028022Controller extends BaseController {

    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028022_Form form = new UI_028022_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028022/queryClick")
    @ResponseBody
    public BaseResp<UI_028022_Form> queryClick(@RequestBody UI_028022_Form form) {
        this.infoMessage("通匯下行電文序號查詢, 條件 = [", form, "]");
        BaseResp<UI_028022_Form> response = new BaseResp<>();
        LogData logContext = new LogData();
        try {
            String orgFiScNo = StringUtils.leftPad(form.getOrgFiscno(), 7, '0');
            String orgPcode = form.getOrgPcode();
            logContext.setRemark("AA1514查詢, 查詢方式=" + orgPcode + ", 原電文序號=" + orgFiScNo);
            logMessage(Level.DEBUG, logContext);
            PageInfo<Msgout> dtMsgout = null;

            switch (orgPcode) {
                case "1111":
                case "1121":
                case "1131":
                case "1171":
                case "1181":
                case "1191":
                    Rmout txRmout = new Rmout();
                    txRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                    txRmout.setRmoutFiscsno(orgFiScNo);
                    List<Rmout> dtRmout = rmService.getRmoutByDef(txRmout);
                    logContext.setRemark("AA1514查詢, dtRMOUT.Rows.count=" + dtRmout.size());
                    logMessage(Level.DEBUG,logContext);
                    if (dtRmout.size()==1){
                        sendToAa(form,dtRmout.get(0).getRmoutFiscSndCode(),response);
                        break;
                    }else if (dtRmout.size()>1){
                        response.setMessage(MessageType.INFO,"匯出主檔(RMOUT)查到>1筆資料，請查明原因");
                        break;
                    }else {
                        response.setMessage(MessageType.INFO,"匯出主檔(RMOUT)無此資料");
                        break;
                    }
                case "1411":
                    Msgout txMsgout = new Msgout();
                    txMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                    txMsgout.setMsgoutFiscsno(orgFiScNo);
                    dtMsgout = rmService.getMsgOutByDef(txMsgout,form.getPageNum(),form.getPageSize());
                    if (dtMsgout.getList().size() ==1){
                        sendToAa(form,dtMsgout.getList().get(0).getMsgoutFiscSndCode(),response);
                        break;
                    }else if (dtMsgout.getList().size() >1){
                        response.setMessage(MessageType.INFO,"一般通訊匯出主檔(MSGOUT)查到>1筆資料，請查明原因");
                        break;
                    }else {
                        response.setMessage(MessageType.INFO,"一般通訊匯出主檔(MSGOUT) 無此資料");
                        break;
                    }
                default:
                    logContext.setRemark("AA1514查詢, PCODE error, Me.ORG_PCODE.SelectedItem.Value=" + form.getOrgPcode());
                    logMessage(Level.DEBUG,logContext);
                    response.setMessage(MessageType.INFO,"Error PCode");
                    break;
            }
        } catch (Exception ex) {
            logContext.setRemark("AA1514查詢, exception: " + ex.toString());
            logMessage(Level.DEBUG,logContext);
            response.setMessage(MessageType.INFO, WebCodeConstant.DealFail);
        }
        return response;
    }

    protected void sendToAa(UI_028022_Form form, String fiscSendCode,BaseResp<UI_028022_Form> response) throws Exception {
        FEPHandler fepHandler = new FEPHandler();
        LogData logContext = new LogData();
        FISCGeneral fiscData = new FISCGeneral();
        try{
            fiscData.setRMRequest(new FISC_RM());
            fiscData.getRMRequest().setProcessingCode("1514");
            fiscData.getRMRequest().setMessageKind(MessageFlow.Request);

            fiscData.setSubSystem(FISCSubSystem.RM);
            fiscData.getRMRequest().setMessageType("0200");

            fiscData.getRMRequest().setOrgPcode(fiscSendCode);
            fiscData.getRMRequest().setFiscNo(form.getOrgFiscno());

            logContext.setRemark("UI_028022, start to call AA1514");
            logMessage(Level.DEBUG,logContext);
            fepHandler.dispatch(FEPChannel.FEP,fiscData);

            logContext.setRemark("UI_028022, after AA1514, result = "+fiscData.getDescription().trim());
            logMessage(Level.DEBUG,logContext);
            if (fiscData.getDescription().trim().equals(NormalRC.External_OK)){
                // 組ReturnCode
                String status = fiscData.getRMResponse().getSTATUS();
                switch (status){
                    case "00":
                        form.setoMsg(fiscData.getRMResponse().getSTATUS());
                        break;
                    case "01":
                        form.setoMsg(fiscData.getRMResponse().getSTATUS() + "-財金未收到");
                        break;
                    case "02":
                        form.setoMsg(fiscData.getRMResponse().getSTATUS() + "-財金收但對方行未收到");
                        break;
                    case "03":
                        form.setoMsg(fiscData.getRMResponse().getSTATUS() + "-對方行已收到");
                        break;
                    case "04":
                        form.setoMsg(fiscData.getRMResponse().getSTATUS() + "-對方行已回訊財金");
                        break;
                    default:
                        break;
                }
                form.setoOrgFiscno(fiscData.getRMResponse().getFiscNo());
                form.setoOrgPcode(fiscData.getRMResponse().getOrgPcode());
                form.setoOrgReceiverBank(fiscData.getRMResponse().getReceiverBank());
                form.setoOrgRmsno(fiscData.getRMResponse().getBankNo());
                form.setoOrgStan(fiscData.getRMResponse().getOrgStan());
                form.setoOrgTxDatetime(fiscData.getRMResponse().getOrgTxDatetime());
                form.setoOrgTxamt(fiscData.getRMResponse().getTxAmt());
                response.setMessage(MessageType.INFO, WebCodeConstant.DealSuccess);
            }else {
                response.setMessage(MessageType.DANGER,fiscData.getDescription());
            }
        }catch (Exception ex){
            logContext.setRemark("SendToAA, exception: " + ex.toString());
            logMessage(Level.DEBUG,logContext);
            response.setMessage(MessageType.DANGER,ex.getMessage());
        }
        response.setData(form);
    }
}
