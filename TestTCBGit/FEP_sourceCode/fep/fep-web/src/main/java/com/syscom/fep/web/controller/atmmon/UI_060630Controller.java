package com.syscom.fep.web.controller.atmmon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.mybatis.model.Subsys;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.entity.dbmaintain.MsgfileTmp;
import com.syscom.fep.web.form.atmmon.UI_060630_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060630_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.ChannelService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 錯誤訊息定查詢
 * @author bruce
 *
 */
@Controller
public class UI_060630Controller extends BaseController{	
	
	@Autowired
	private AtmService atmService;
    @Autowired
    private ChannelService channelService;
    
    private final String pleaseChoose = "所有";
    private HashMap<String,String> channelMaps = new HashMap<>();
    private HashMap<String, String> subsysMaps = new HashMap<>();

	@Override
	public void pageOnLoad(ModelMap mode) {
		UI_060630_FormMain form = new UI_060630_FormMain();
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		form.setUrl("/atmmon/UI_060630/bindGrid");
		this.bindGridData(form,argsMap,mode);
	}
	
	/**
	 * 查詢按鈕
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/atmmon/UI_060630/bindGrid")
	public String bindGrid(@ModelAttribute UI_060630_FormMain form, ModelMap mode) {
		Map<String, Object> argsMap = form.toMap();
		argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
		this.bindGridData(form, argsMap, mode);
		return Router.UI_060630.getView();
	}
	
	/**
	 * 查詢
	 * @param argsMap
	 * @param mode
	 * @return
	 */
	private void bindGridData(UI_060630_FormMain form, Map<String, Object> argsMap, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);// 保存當前表單資料
		try {
			this.setChannelOptions(mode);
			this.setSubsysOptions(mode);
			AuditLog auditLog = new AuditLog();
			auditLog.setAction("查詢");
			if (StringUtils.isNotBlank(form.getChannel())) {
				auditLog.addParam("來源通道", form.getChannel());
			}
			if (StringUtils.isNotBlank(form.getErrorCode())) {
				auditLog.addParam("訊息代碼", form.getErrorCode());
			}
			if (StringUtils.isNotBlank(form.getMsgfileFisc())) {
				auditLog.addParam("財金訊息代碼", form.getMsgfileFisc());
			}
			if (StringUtils.isNotBlank(form.getMsgfileAtm())) {
				auditLog.addParam("ATM訊息代碼", form.getMsgfileAtm());
			}
			if (StringUtils.isNotBlank(form.getMsgfileT24())) {
				auditLog.addParam("中心主機訊息代碼", form.getMsgfileT24());
			}
			if (StringUtils.isNotBlank(form.getMsgfileCredit())) {
				auditLog.addParam("信用卡訊息代號", form.getMsgfileCredit());
			}
			if (StringUtils.isNotBlank(form.getMsgfileCredit())) {
				auditLog.addParam("信用卡訊息代號", form.getMsgfileCredit());
			}
			if (StringUtils.isNotBlank(form.getSeverity())) {
				auditLog.addParam("訊息嚴重性", form.getSeverity());
			}
			if (StringUtils.isNotBlank(form.getMsgfileShortmsg())) {
				auditLog.addParam("訊息嚴重性", form.getMsgfileShortmsg());
			}
			setAuditLog(auditLog);

			PageInfo<Msgfile> pageInfo = atmService.queryByLikeJoinChannel(argsMap);
			//將Msgfile改成使用MsgfileTmp
			PageInfo<MsgfileTmp> newPageInfo = this.changeObject(pageInfo);
			BeanUtils.copyProperties(pageInfo, newPageInfo, "list");
			if (newPageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
			} else {
				this.changeFieldContent(newPageInfo);
				this.showMessage(mode, MessageType.INFO, QuerySuccess);
			}
			PageData<UI_060630_FormMain, MsgfileTmp> pageData = new PageData<UI_060630_FormMain, MsgfileTmp>(newPageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}	
	
	/**
	 * 來源通道超連結
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/atmmon/UI_060630/bindGridDetail")
	public String bindGridDetail(@ModelAttribute UI_060630_FormDetail form, ModelMap mode) {
		AuditLog auditLog = new AuditLog();
		auditLog.setAction("查詢");
		if (StringUtils.isNotBlank(form.getMsgfileChannelTxt())) {
			auditLog.addParam("來源通道", form.getMsgfileChannelTxt());
		}
		if (StringUtils.isNotBlank(form.getMsgfileErrorcode())) {
			auditLog.addParam("訊息代碼", form.getMsgfileErrorcode());
		}
		setAuditLog(auditLog);

		//轉成布林值
		form.setMsgfileSendEmsB(DbHelper.toBoolean(form.getMsgfileSendEms()));
		form.setMsgfileRetainB(DbHelper.toBoolean(form.getMsgfileRetain()));
		form.setMsgfileAuthB(DbHelper.toBoolean(form.getMsgfileAuth()));
		form.setMsgfileWarningB(DbHelper.toBoolean(form.getMsgfileWarning()));
		this.doKeepFormData(mode, form);
		return Router.UI_060630_Detail.getView();
	}
	
	/**
	 * 因為要將物件欄位做二次加工，所以新增一個MsgfileTmp繼承Msgfile
	 * @param pageInfo
	 * @return
	 */
	private PageInfo<MsgfileTmp> changeObject(PageInfo<Msgfile> pageInfo) {
		List<Msgfile> msgfileList = pageInfo.getList();
		List<MsgfileTmp> msgfileTmpList = new ArrayList<>(msgfileList.size());
		for (Msgfile msgfile : msgfileList) {
			MsgfileTmp msgfileTmp = new MsgfileTmp(msgfile);
			msgfileTmpList.add(msgfileTmp);
		}
		return PageInfo.of(msgfileTmpList);
	}
	
	/**
	 * 改變特定欄位內容
	 * @param pageInfo
	 */
	private void changeFieldContent(PageInfo<MsgfileTmp> pageInfo ) {
		for(MsgfileTmp msgfile : pageInfo.getList()) {
			msgfile.setMsgfileChannelTxt(this.getChannelName(String.valueOf(msgfile.getMsgfileChannel())));//來源通道
			msgfile.setMsgfileSubsysTxt(this.getSubSystemName(String.valueOf(msgfile.getMsgfileSubsys())));//子系統
			msgfile.setMsgfileSendEmsTxt(this.getAtmDeleteName(String.valueOf(msgfile.getMsgfileSendEms())));//送事件監控
		}
	}
	
    private void setChannelOptions(ModelMap mode) throws Exception {
    	if(channelMaps.size() != 0) {
    		channelMaps = new HashMap<>();
    	}
        List<Channel> channelList = channelService.queryAllDataByCE();
        List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
        selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
        for (int i = 0; i < channelList.size(); i++) {
        	String name = channelList.get(i).getChannelName();
        	String channelno = channelList.get(i).getChannelChannelno().toString();
            selectOptionList.add(new SelectOption<String>(name, channelno));
            channelMaps.put(channelno, name);
        }
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }
    
    private void setSubsysOptions(ModelMap mode) throws Exception {
    	if(subsysMaps.size() != 0) {
    		subsysMaps = new HashMap<>();
    	}
    	List<Subsys> subsysList = atmService.queryAllData("");
    	List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
        selectOptionList.add(new SelectOption<String>("全部", StringUtils.EMPTY));
        for(Subsys subsys : subsysList) {
        	String name = subsys.getSubsysNameS();
        	String subsysNo = subsys.getSubsysSubsysno().toString();
        	selectOptionList.add(new SelectOption<String>(name, subsysNo));
        	subsysMaps.put(subsysNo, name);
        }
        WebUtil.putInAttribute(mode, AttributeName.Options2, selectOptionList);
    }
	
	/**
	 * 將送事件監控轉成"是"或"否"
	 * @param sendEms
	 * @return
	 */
	private String getAtmDeleteName(String sendEms) {
		switch (sendEms) {
		case "0": return "否";
		case "1": return "是";
		default:return "";
		}
	}
	
	/**
	 * 將子系統轉換成英文
	 * @param subsys
	 * @return
	 */
	private String getSubSystemName(String subsys) {
		if(StringUtils.isBlank(subsys)) {
			return "";
		}else {
			switch (subsys) {
			case "0": return "None";
			case "1": return "INBK";
			case "2": return "RM";
	        case "3": return "ATMP";
	        case "4": return "CARD";
	        case "5": return "HSM";
	        case "6": return "MON";
	        case "7": return "RECS";
	        case "8": return "GW";
	        case "9": return "CMN";
			default:return subsys;
			}			
		}
	}
	
	/**
	 * 將來源通道轉換成英文
	 * @param channel
	 * @return
	 */
	private String getChannelName(String channel) {
		if(channelMaps.containsKey(channel)) {
			return channelMaps.get(channel);
		}
		return "";
	}
}
