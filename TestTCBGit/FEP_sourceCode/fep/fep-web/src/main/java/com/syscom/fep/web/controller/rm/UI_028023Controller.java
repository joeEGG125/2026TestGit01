package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.server.common.handler.FISCHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028023_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
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
 * 通匯待解筆數查詢
 * @author xingyun_yang
 * @create 2021/9/23
 */
@Controller
public class UI_028023Controller extends BaseController {


    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028023_Form form = new UI_028023_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/rm/UI_028023/queryClick")
    @ResponseBody
    public BaseResp<UI_028023_Form> queryClick(@RequestBody UI_028023_Form form) {
        this.infoMessage("通匯待解筆數查詢, 條件 = [", form, "]");
        BaseResp<UI_028023_Form> response = new BaseResp<>();
        LogData logContext = new LogData();
        try {
            logContext.setRemark(StringUtils.join("AA1515查詢, 查詢方式=",form.getOrgPcodeDdl()));
            logMessage(Level.DEBUG,logContext);

            response = sendToAa(form);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }

    /**
     * 與AA相關處理
     */
    private BaseResp<UI_028023_Form> sendToAa(UI_028023_Form form){
        FEPHandler fepHandler = new FEPHandler();
        BaseResp<UI_028023_Form> response = new BaseResp<>();
        // Dim message As String
        FISCGeneral fiscData = new FISCGeneral();
        LogData logContext = new LogData();
        try{
            fiscData.setRMRequest(new FISC_RM());
            fiscData.getRMRequest().setProcessingCode("1515");
            fiscData.getRMRequest().setMessageKind(MessageFlow.Request);

            fiscData.setSubSystem(FISCSubSystem.RM);
            fiscData.getRMRequest().setMessageType("0200");
            fiscData.getRMRequest().setOrgPcode(form.getOrgPcodeDdl());

            fepHandler.dispatch(FEPChannel.FEP,fiscData);
            if (fiscData.getDescription().trim().equals(NormalRC.External_OK)){
                //組ReturnCode
                logContext.setRemark(StringUtils.join("AA1515 success, response: ORG_PCODE=",
                        fiscData.getRMResponse().getOrgPcode(),", RM_PENDING_CNT=",
                        fiscData.getRMResponse().getRmPendingCnt(), ", RM_PENDING_AMT=",
                        fiscData.getRMResponse().getRmPendingAmt()));
                logMessage(Level.DEBUG,logContext);
                form.setoOrgPcode(fiscData.getRMResponse().getOrgPcode());
                form.setRmPendingCnt(fiscData.getRMResponse().getRmPendingCnt());
                form.setRmPendingAmt(fiscData.getRMResponse().getRmPendingAmt());
                response.setData(form);
                response.setMessage(MessageType.INFO,DealSuccess);
            }else {
                form.setoOrgPcode("");
                form.setRmPendingAmt("");
                form.setRmPendingAmt("");
                response.setData(form);
                response.setMessage(MessageType.DANGER,fiscData.getDescription());
            }
        }catch (Exception ex){
            response.setMessage(MessageType.DANGER,ex.getMessage());
        }
        return response;
    }
}
