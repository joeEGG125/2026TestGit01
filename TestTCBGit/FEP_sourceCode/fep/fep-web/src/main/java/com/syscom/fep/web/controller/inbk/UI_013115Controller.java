package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.inbk.UI_013115_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.server.common.TxHelper;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @title 參加單位 CD/ATM 作業狀況通知
 * @date  2022/4/19
 * @author Han
 */
@Controller
public class UI_013115Controller extends BaseController {

	@Autowired
	private InbkService inbkService;
	
	@Autowired
	private AtmService atmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_013115_Form form = new UI_013115_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

	/**
     * 確認按鈕
     */
    @PostMapping(value = "/inbk/UI_013115/btnConfirm")
    public String btnConfirm(@ModelAttribute UI_013115_Form form, ModelMap mode) {
    	
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        
        //  * 檢查此櫃員機號存不存在ATM主檔中
        //	* 檢查此銀行代號存不存在全國銀行檔中
        //	* 不存在則返回
        if(!CheckData(form,mode)) {
    		return Router.UI_013115.getView();
    	}
        
        FISCGeneral fiscData = new FISCGeneral();
		FEPHandler fepHandler = new FEPHandler();
        
        try {
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("送出");
			if("2".equals(form.getBankRbl())) {
				auditLog.addParam("其他銀行", form.getBankNoTxt());
			}else{
				auditLog.addParam("本行", "");
			}
			if (StringUtils.isNotBlank(form.getaTMNoTxt())) {
				auditLog.addParam("ATM代號", form.getaTMNoTxt());
			}
			if (StringUtils.isNotBlank(form.getaTMWorkStatus())) {
				auditLog.addParam("ATM作業狀況", form.getaTMWorkStatus());
			}
			if (StringUtils.isNotBlank(form.getaTMServiceStatus())) {
				auditLog.addParam("ATM服務型態", form.getaTMServiceStatus());
			}
			setAuditLog(auditLog);

        	fiscData.setOPCRequest(new FISC_OPC());
        	fiscData.setSubSystem(FISCSubSystem.OPC);
        	
            fiscData.getOPCRequest().setAtmStatus("01"+StringUtils.rightPad(form.getaTMNoTxt(), 8, ' ')+"  ");
            fiscData.getOPCRequest().setAtmStatus(EbcdicConverter.toHex(CCSID.English,fiscData.getOPCRequest().getAtmStatus().length(),fiscData.getOPCRequest().getAtmStatus()));
            
            if("2".equals(form.getBankRbl())) {
            	fiscData.getOPCRequest().setBKNO(form.getBankRbl());
            }
            
            fiscData.getOPCRequest().setProcessingCode("3115");
        	fiscData.getOPCRequest().setMessageType("0600");
        	fiscData.getOPCRequest().setMessageKind(MessageFlow.Request);
        	
            fepHandler.dispatch(FEPChannel.FEP,fiscData);
            
            if(StringUtils.isBlank(fiscData.getDescription()) ) {
            	fiscData.setDescription(MessageError);
            }
            
            String massage[] = fiscData.getDescription().split("-");
            
            if(massage.length == 2 &&  NormalRC.FISC_OK.equals(massage[0])) {
            	if(fiscData.getOPCResponse().getAtmStatus().length() < 12) {
            		massage[0] = TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.LengthError);
            		showMessage(mode, MessageType.DANGER,massage[0]);
            		
            	}else {
            		switch(fiscData.getOPCResponse().getAtmStatus().substring(10,10+1)) {
	            		case "N" :
	            			form.setaTMWorkStatus("NORMAL (正常營運中)");
	          		       break;
	          		    case "C" :
	          		    	form.setaTMWorkStatus("CHECK OUT (正常關機)");
	          		    	break;
	          		    case "U":
	          		    	form.setaTMWorkStatus("UNKNOWN (未開機，原因不明)");
	          		    	break;
	          		    case "M":
	          		    	form.setaTMWorkStatus("MALFUNCTION (機器故障)");
	          		    	break;
	          		    case "S":
	          		    	form.setaTMWorkStatus("SHORT OF CASH (現鈔短缺)");
	          		    	break;
	          		    case "L":
	          		    	form.setaTMWorkStatus("LINE DOWN (CD/ATM與參加單位計算機中心間線路故障)");
	          		    	break;
	          		    case "P":
	          		    	form.setaTMWorkStatus("PURPOSE STOP SERVICE (人為關機)");
	          		    	break;
            		}
            		switch(fiscData.getOPCResponse().getAtmStatus().substring(11,11+1)) {
	            		case "E":
	            			form.setaTMServiceStatus("EXTENDED SERVICE (延時服務機器)");
	      		    		break;
	            		case "N":
	            			form.setaTMServiceStatus("NORMAL SERVICE (非延時服務機器)");
	      		    		break;
	            		case "9":
	      		    		form.setaTMServiceStatus("24 HOURS SERVICE (24小時服務機器)");
	      		    		break;
	            		case "D":
	      		    		form.setaTMServiceStatus("停止作業機器");
	      		    		break;
            		}
            		showMessage(mode, MessageType.INFO,TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.Normal));
            	}
            }else {
            	 showMessage(mode,MessageType.DANGER,fiscData.getDescription());
            }
        } catch (Exception ex) {
        	errorMessage(ex, ex.getMessage());
        	showMessage(mode,MessageType.DANGER,programError);
        }
        return Router.UI_013115.getView();
    }

	/**
	 * 檢查此"櫃員機號"存不存在ATM主檔中 
	 * 檢查此"銀行代號"存不存在全國銀行檔中
	 */
	private boolean CheckData(UI_013115_Form form, ModelMap mode) {

		List<Map<String, Object>> dt = atmService.getSingleATM(form.getaTMNoTxt());

		if (null == dt || dt.size() != 1) {
			showMessage(mode, MessageType.DANGER, WebCodeConstant.NoATMNo); // 此櫃員機號不存在ATM主檔中
			return false;
		}
//		if ("1".equals(form.getBankRbl())) {
//			if (!inbkService.checkBankExist(form.getBankNoTxt())) {
//				showMessage(mode, MessageType.DANGER, WebCodeConstant.NoBankID); // 此銀行代號不存在全國銀行檔中
//				return false;
//			}
//		}
		return true;
	}
}
