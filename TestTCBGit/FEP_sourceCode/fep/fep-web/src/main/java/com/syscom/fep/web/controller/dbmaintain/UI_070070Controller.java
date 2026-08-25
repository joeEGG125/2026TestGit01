package com.syscom.fep.web.controller.dbmaintain;

import java.io.IOException;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Txtype;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.dbmaintain.UI_070070_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.MsgctlService;
import com.syscom.fep.web.util.WebUtil;

/**
 * UI070070 交易分PCODE切換
 */
@Controller
public class UI_070070Controller extends BaseController {
	@Autowired
	private MsgctlService msgctlService;
	
	
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_070070_Form form = new UI_070070_Form();
		try {
			this.setOptions(mode);
			
			this.doKeepFormData(mode, form);
		} catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}
	
	@PostMapping(value = "/dbmaintain/UI_070070/queryClick")
    public String queryClick(@ModelAttribute UI_070070_Form form, ModelMap mode, HttpServletResponse response, HttpServletRequest request) throws IOException {
		try {
			this.setOptions(mode);
			this.doKeepFormData(mode, form);
			Map<String, Object> argsMap = form.toMap();
			argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("查詢");
			// 塞入auditLog.params
			if (StringUtils.isNotBlank(form.getUiTxType1()))
				auditLog.addParam("交易類別", form.getUiTxType1());
			if (StringUtils.isNotBlank(form.getUiTxType2()))
				auditLog.addParam("業務分類", form.getUiTxType2());
			if (StringUtils.isNotBlank(form.getUiProcType()))
				auditLog.addParam("交易處理模式", form.getUiProcType());
			if (StringUtils.isNotBlank(form.getMsgctlMsgid()))
				auditLog.addParam("交易訊息", form.getMsgctlMsgid());
			// 最後塞入ThreadLocal變量中
			setAuditLog(auditLog);
			PageInfo<HashMap<String, Object>> pageInfo = msgctlService.queryPCodeData(argsMap);
			if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
            PageData<UI_070070_Form, HashMap<String, Object>> pageData = new PageData<UI_070070_Form, HashMap<String, Object>>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_070070.getView();
	}
	
	@PostMapping(value = "/dbmaintain/UI_070070/updateCbsProc")
	@ResponseBody
	public BaseResp<UI_070070_Form> updateCbsProc(@RequestBody List<UI_070070_Form> formList, @ModelAttribute ModelMap mode) throws Exception {
		BaseResp<UI_070070_Form> response = new BaseResp<>();
		try {
			int updateCount = 0;
			String procName = "";
			StringBuilder msgid = new StringBuilder();

			// 記錄auditLog
			AuditLog auditLog = new AuditLog();
			// 塞入auditLog.action
			auditLog.setAction("切換");

			for(UI_070070_Form from : formList) {
				// 塞入auditLog.params
				if (StringUtils.isNotBlank(from.getMsgctlMsgid())){
					msgid.append(from.getMsgctlMsgid()).append(",");
				}
				if (StringUtils.isNotBlank(from.getUiProcType())){

					if (from.getUiProcType().equals("Y")) {
						procName = "3-1";
					}
					else {
						procName = "3-2";
					}
				}
				// 建立weblog
				Msgctl msgctl = new Msgctl();
				msgctl.setMsgctlMsgid(from.getMsgctlMsgid());
				msgctl.setMsgctlCbsProc(from.getUiProcType());
				updateCount = msgctlService.updateMsgtlCbsProc(msgctl);
			}
			// 最後塞入ThreadLocal變量中
			msgid.setLength(msgid.length() - 1);
			auditLog.addParam("Msgid", msgid.toString());
			auditLog.addParam("ProcType", procName);
			setAuditLog(auditLog);
			this.infoMessage( "已將",msgid.toString(), " 切換到", procName );
			if(updateCount > 0) {				
				response.setMessage(MessageType.INFO, UpdateSuccess);				
			}else {
				response.setMessage(MessageType.INFO, UpdateFail);
			}
		}catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}
	
	private void setOptions(ModelMap mode) {
		List<Txtype> txtypeList = msgctlService.getTxtypeList();
		List<SelectOption<String>> selectOptionList1 = new ArrayList<>();
		List<SelectOption<String>> selectOptionList2 = new ArrayList<>();
		List<SelectOption<String>> selectOptionList3 = new ArrayList<>();
		List<String> type1List = new ArrayList<>();
		List<String> type2List = new ArrayList<>();
		HashMap<String, List<String>> dataMap = new HashMap<>();
		
		//交易類別
		selectOptionList1.add(new SelectOption<>("全部", ""));
		//業務分類
		selectOptionList2.add(new SelectOption<>("全部", ""));
	
		for(Txtype txtype : txtypeList) {
			if(!type1List.contains(txtype.getTxtypeType1())) {
				type1List.add(txtype.getTxtypeType1());
				selectOptionList1.add(new SelectOption<>(txtype.getTxtypeType1Name(), txtype.getTxtypeType1()));
				dataMap.put(txtype.getTxtypeType1(), new ArrayList<>());
			}
			if(!type2List.contains(txtype.getTxtypeType2())) {
				type2List.add(txtype.getTxtypeType2());
				selectOptionList2.add(new SelectOption<>(txtype.getTxtypeType2Name(), txtype.getTxtypeType2()));
			}
			dataMap.get(txtype.getTxtypeType1()).add(txtype.getTxtypeType2());
		}
		
		//交易處理模式
		selectOptionList3.add(new SelectOption<>("全部", ""));
		selectOptionList3.add(new SelectOption<>("IMS", "Y"));
		selectOptionList3.add(new SelectOption<>("FEP", "N"));
		
		WebUtil.putInAttribute(mode, AttributeName.Maps, dataMap);
		WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList1);
		WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList2);
		WebUtil.putInAttribute(mode, AttributeName.Options3, selectOptionList3);
	}

}
