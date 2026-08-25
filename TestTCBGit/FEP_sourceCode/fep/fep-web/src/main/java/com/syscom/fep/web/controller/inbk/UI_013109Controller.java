package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.inbk.UI_013109_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 應用系統狀態查詢-3109
 *
 * @author xingyun_yang
 * @create 2021/10/13
 */
@Controller
public class UI_013109Controller extends BaseController {
    @Autowired
    private InbkService inbkService;
    @Autowired
    private RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        try {
            this.bindConstant(mode);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
        // 初始化表單數據
        UI_013109_Form form = new UI_013109_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 為頁面綁定一些常量
     *
     * @param mode ModelMap
     */
    private void bindConstant(ModelMap mode) throws Exception {
        // 初始化PCODE下拉選單
//        List<SelectOption<String>> selectOptionList = new ArrayList<>();
//        List<HashMap<String, String>> list = rmService.getAllFISCCurcd();
//        selectOptionList.add(new SelectOption<>("", "0"));
//        for (Map<String, String> stringStringMap : list) {
//            selectOptionList.add(new SelectOption<>(stringStringMap.get("Output"), stringStringMap.get("Output")));
//        }
//        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    @PostMapping(value = "/inbk/UI_013109/confirmClick")
    public String confirmClick(@ModelAttribute UI_013109_Form form, ModelMap mode) throws Exception {
        this.infoMessage("執行明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        FISCGeneral aData = new FISCGeneral();
        FEPHandler fepHandler = new FEPHandler();
        String[] message;
        String inqStat = StringUtils.EMPTY;
        try {
            this.showMessage(mode, MessageType.WARNING, WebCodeConstant.ProgramExecuting);
            if (!checkData(form.getApId(), form.getBkno(), mode)) {
                return Router.UI_013109.getView();
            }

            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            if (StringUtils.isNotBlank(form.getBkno())) {
                auditLog.addParam("會員單位代號", form.getBkno());
            }
            if (StringUtils.isNotBlank(form.getApId())) {
                auditLog.addParam("應用系統代號", form.getApId());
            }
            if (StringUtils.isNotBlank(form.getCurDdl())) {
                auditLog.addParam("幣別", form.getCurDdl());
            }
            setAuditLog(auditLog);

            aData.setSubSystem(FISCSubSystem.OPC);
            aData.setOPCRequest(new FISC_OPC());
            aData.getOPCRequest().setMessageKind(MessageFlow.Request);
            aData.getOPCRequest().setProcessingCode("3109");
            aData.getOPCRequest().setMessageType("0600");
            aData.getOPCRequest().setAPID(form.getApId());
            aData.getOPCRequest().setBKNO(form.getBkno());


            //2011/11/04 Modify by Ruling for 財金電文中幣別是OPTIONAL，台幣不用組，外幣才要
//            if (!"0".equals(form.getCurDdl()) && !"000-TWD".equals(form.getCurDdl())) {
//                aData.getOPCRequest().setCUR(form.getCurDdl().substring(0,3));
//            }
            //Call AA
            fepHandler.dispatch(FEPChannel.FEP, aData);
            //將AA RC 顯示在UI上
            if (aData.getDescription() == null || "".equals(aData.getDescription().trim())) {
                aData.setDescription(MessageError);
            }
            message = aData.getDescription().split("-");
            //若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
            if (message.length == 2 && NormalRC.FISC_OK.equals(message[0])) {
                inqStat = aData.getOPCResponse().getReserve27();
                if (inqStat != null && inqStat.length() >= 4) {
                    String soctlbl = getSoctName(inqStat.substring(0, 1));
                    String aoctlbl = getAoctName(inqStat.substring(1, 2));
                    String mboctlbl = getMboctName(inqStat.substring(2, 3));
                    String mbactlbl = getMbactName(inqStat.substring(3, 4));
                    mode.addAttribute("soctlbl", soctlbl);
                    mode.addAttribute("aoctlbl", aoctlbl);
                    mode.addAttribute("mboctlbl", mboctlbl);
                    mode.addAttribute("mbactlbl", mbactlbl);
                }
                this.showMessage(mode, MessageType.INFO, TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
            } else {
                this.showMessage(mode, MessageType.DANGER, aData.getDescription());
            }
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        } finally {
        	try {
        		bindConstant(mode);
	        } catch (Exception ex) {
                this.errorMessage(ex, ex.getMessage());
	            this.showMessage(mode, MessageType.DANGER, programError);
	        }
        }
        return Router.UI_013109.getView();
    }

    /**
     * 資料檢核
     *
     * @return false or true
     */
    private Boolean checkData(String aPidCob, String bknoTxt, ModelMap mode) {
//        if (!inbkService.checkBankExist(bknoTxt)) {
//            this.showMessage(mode, MessageType.DANGER, WebCodeConstant.NoBankID);
//            return false;
//        }
//        if (!("1000".equals(aPidCob) || "1100".equals(aPidCob) || "1200".equals(aPidCob) ||
//                "1300".equals(aPidCob) || "1400".equals(aPidCob) || "1600".equals(aPidCob) ||
//                "2000".equals(aPidCob) || "2200".equals(aPidCob) || "2500".equals(aPidCob) ||
//                "2510".equals(aPidCob) || "2520".equals(aPidCob) || "2530".equals(aPidCob) ||
//                "2540".equals(aPidCob) || "2550".equals(aPidCob) || "2560".equals(aPidCob) ||
//                "2570".equals(aPidCob) || "7100".equals(aPidCob) || "7300".equals(aPidCob))) {
//            this.showMessage(mode, MessageType.DANGER, WebCodeConstant.ApidError);
//            return false;
//        }
        if (!(  "2000".equals(aPidCob) || "2200".equals(aPidCob) || "2500".equals(aPidCob) ||
                "2510".equals(aPidCob) || "2520".equals(aPidCob) || "2530".equals(aPidCob) ||
                "2540".equals(aPidCob) || "2550".equals(aPidCob) || "2560".equals(aPidCob) ||
                "2570".equals(aPidCob) || "2700".equals(aPidCob) || "7100".equals(aPidCob))) {            this.showMessage(mode, MessageType.DANGER, WebCodeConstant.ApidError);
            return false;
        }
        return true;
    }
}
