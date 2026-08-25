package com.syscom.fep.web.controller.atmmon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Atmc;
import com.syscom.fep.mybatis.model.Curcd;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.entity.SelectOption;
import com.syscom.fep.web.entity.WebCodeConstant;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.form.atmmon.UI_060078_FormDetail;
import com.syscom.fep.web.form.atmmon.UI_060078_FormMain;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * ATM(ATMC)收付累計查詢
 * @author bruce
 *
 */
@Controller
public class UI_060078Controller extends BaseController{
	
	@Autowired
	private AtmService atmService;
	
	private Sysstat sysstat = null;
	
	private Zone zone = null;
	
	private final String zoneCode = "TWN";
	
	private final String pleaseChoose = "請選擇";

	@Override
	public void pageOnLoad(ModelMap mode) {
        // 初始化頁面 
		UI_060078_FormMain form = new UI_060078_FormMain();
		form.setSysstatTbsdyFisc(this.getTbsdyFisc(mode));//財金營業日
		form.setZoneZoneTbsdy(this.getTbsdy(mode));//自行營業日	
		this.setCurcdAlpha3(mode);//VB.net是使用自行定義標籤取得下拉，轉成java直接在初始化時取得幣別下拉選單
		form.setUrl("/atmmon/UI_060078/bindGrid");
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
	}
	
	/**
	 * 查詢
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping( value = "/atmmon/UI_060078/bindGrid")
	public String bindGrid(@ModelAttribute UI_060078_FormMain form, ModelMap mode) {
		this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);//保存當前表單資料
		//分頁
		try {
			if(this.checkAllField(form,mode)) {
				Map<String, Object> argsMap = form.toMap();
				argsMap.put("pageSize", WebCodeConstant.DetailGridViewPageSize);
				// 記錄auditLog
				AuditLog auditLog = new AuditLog();
				// 塞入auditLog.action
				auditLog.setAction("查詢");
				if (StringUtils.isNotBlank(form.getSysstatTbsdyFiscData()))
					auditLog.addParam("財金營業日", form.getSysstatTbsdyFiscData());
				if (StringUtils.isNotBlank(form.getZoneZoneTbsdyData()))
					auditLog.addParam("自行營業日", form.getZoneZoneTbsdyData());
				if (StringUtils.isNotBlank(form.getAtmNo()))
					auditLog.addParam("ATM代號", form.getAtmNo());
				if (StringUtils.isNotBlank(form.getCur()))
					auditLog.addParam("幣別", form.getCur());
				if (StringUtils.isNotBlank(form.getTxCode()))
					auditLog.addParam("交易別", form.getTxCode());
				// 最後塞入ThreadLocal變量中
				setAuditLog(auditLog);
				PageInfo<Atmc> pageInfo = atmService.queryATMCByDef(argsMap);
				if (pageInfo.getSize() == 0) {
					this.showMessage(mode, MessageType.INFO, QueryNoData);
				}else {
					this.showMessage(mode, MessageType.INFO, QuerySuccess);
	                //'若資料數少於預設pagesize 就把pagesize改為資料數
	                //'沒有資料的ROW之隱藏欄位仍然會顯示
					if(pageInfo.getSize() < Integer.parseInt(argsMap.get("pageSize").toString())) {
						argsMap.put("pageSize", pageInfo.getSize());
					}
				}
				PageData<UI_060078_FormMain, Atmc> pageData = new PageData<UI_060078_FormMain, Atmc>(pageInfo, form);
				WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
			}
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}		
		return Router.UI_060078.getView();
	}
	
	/**
	 * 驗證欄位
	 * @param form
	 * @return
	 */
	private boolean checkAllField(UI_060078_FormMain form,ModelMap mode) {
		StringBuffer errorMessage = null;
		if(!form.getTxCode().equals("TTS")) {
			if(StringUtils.isNotBlank(form.getSysstatTbsdyFisc())) {
				form.setSysstatTbsdyFiscData(form.getSysstatTbsdyFisc().replace("-", ""));
			}else {
				errorMessage = new StringBuffer("財金營業日為必填");
			}
		}
		
		if(StringUtils.isNotBlank(form.getZoneZoneTbsdy())) {
			form.setZoneZoneTbsdyData(form.getZoneZoneTbsdy().replace("-", ""));
		}
		
		if (StringUtils.isNotBlank(form.getAtmNo())) {
			char[] chars = form.getAtmNo().toCharArray();

			for (int i = 0; i < chars.length; i++) {
				if (!Character.isDigit(chars[i])) {
					errorMessage = new StringBuffer("ATM代號 請輸入小於六位的整數");
					break;
				}
			}
		}
		
		if(StringUtils.isBlank(errorMessage)) {
			return true;
		}else {
			this.showMessage(mode, MessageType.DANGER, errorMessage);
			return false;
		}	
	}
	
	/**
	 * 財金營業日
	 * @param mode
	 * @return
	 */
	private String getTbsdyFisc(ModelMap mode) {
		try {
			this.sysstat = atmService.getStatus();
			if (this.sysstat != null) {
				String sysstatTbsdyFisc = this.sysstat.getSysstatTbsdyFisc();
				if (StringUtils.isNotBlank(sysstatTbsdyFisc)) {
					return charDateToDate(sysstatTbsdyFisc, "-");
				}
			}
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * 自行營業日
	 * @param mode
	 * @return
	 */
	private String getTbsdy(ModelMap mode) {
		try {
			//20230322 Bruce 先註解掉
			this.zone = null;//atmService.getDataByZone(this.zoneCode);
			if (this.zone != null) {
				String zonTbsdy = this.zone.getZoneTbsdy();
				if (StringUtils.isNotBlank(zonTbsdy)) {
					return charDateToDate(zonTbsdy, "-");
				}
			}
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return StringUtils.EMPTY;
	}	
	
	/**
	 * 取得幣別下拉選單
	 * @param mode
	 */
	private void setCurcdAlpha3(ModelMap mode) {
		try {
			List<Curcd> curcdsList = this.atmService.getAllCurcd();
			List<SelectOption<String>> selectOptionList = new ArrayList<SelectOption<String>>();
			selectOptionList.add(new SelectOption<String>(this.pleaseChoose, StringUtils.EMPTY));
			for(int i= 0 ; i < curcdsList.size() ; i++) {
				selectOptionList.add(new SelectOption<String>(curcdsList.get(i).getCurcdAlpha3(), curcdsList.get(i).getCurcdAlpha3()));
			}			
			WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
	}
	
	/**
	 * 每列明細
	 * @param form
	 * @param mode
	 * @return
	 */
	@PostMapping(value = "/atmmon/UI_060078/bindGridDetail")
	public String bindGridDetail(@ModelAttribute UI_060078_FormDetail form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		form.setAtmcBrnoSt(form.getAtmcBrnoSt() + "-" + this.getBrName(form.getAtmcBrnoSt(),mode));//清算分行
		form.setAtmcCur(form.getAtmcCur() + "-" + this.getZoneCurName(form.getAtmcCur()));//幣別
		form.setAtmcSelfcd(form.getAtmcSelfcd() + "-" + this.getSelfCdName(form.getAtmcSelfcd()));//保留
		form.setAtmcLoc(form.getAtmcLoc() + "-" + this.getAtmLocName(form.getAtmcLoc()));//行外記號
		form.setAtmcZone(form.getAtmcZone() + "-" + this.getAtmZoneName(form.getAtmcZone()));//ATM所在區域
		form.setAtmcCurSt(form.getAtmcCurSt() + "-" + this.getZoneCurName(form.getAtmcCurSt()));//ATM主清算幣別
		form.setAtmcCrossFlag(form.getAtmcCrossFlag() + "-" + this.getCrossFlagName(form.getAtmcCrossFlag()));//跨區交易記號
		//日期formate
		form.setAtmcTbsdyFisc(this.setDate(form.getAtmcTbsdyFisc()));
		form.setAtmcTbsdy(this.setDate(form.getAtmcTbsdy()));
		//格式化日期
//		DateTimeFormatter formate = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss");
//		LocalDateTime arrivalDate  = LocalDateTime.parse(form.getUpdateTime(),formate);
//		form.setUpdateTime(arrivalDate.format(formate));		
		DateFormat dateTimeformat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		form.setUpdateTime(dateTimeformat.format(new Date(form.getUpdateTime())));	
		this.doKeepFormData(mode, form);//保存當前表單資料
		return Router.UI_060078_Detail.getView();
	}
	
	/**
	 * 調整在明細頁顯示日期方式yyyy/MM/dd
	 * @param form
	 */
	private String setDate(String date) {
		String yyyy = date.substring(0,4);
		String MM = date.substring(4,6);
		return yyyy+"/"+MM+"/"+date.substring(6,8);
	}
	
	/**
	 * 保存當前表單資料
	 *
	 * @param map
	 * @param form
	 */
	protected void doKeepFormData(ModelMap mode, BaseForm form) {
		if (StringUtils.isBlank(form.getUrl())) {
			form.setUrl(WebUtil.getRequest().getRequestURI());
		}
		this.setCurcdAlpha3(mode);
		WebUtil.putInAttribute(mode, AttributeName.Form, form);
		WebUtil.getUser().addForm(form);
	}
	
	/**
	 * 取得分行中文名稱
	 * @param brNoSt
	 * @return
	 */
	private String getBrName(String brNoSt,ModelMap mode) {
		try {
			return atmService.getBRAlias(brNoSt);
		} catch (Exception ex) {
			this.errorMessage(ex,ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
			return "";
		}
	}
	
	/**
	 * 取得保留中文名稱
	 * @param selfCd
	 * @return
	 */
	private String getSelfCdName(String selfCd) {
		switch (selfCd) {
		case "1": return "本行";
		case "2": return "聯行";
		case "3": return "跨行";
		case "4": return "跨國";
		default : return "";
		}		
	}
	
	/**
	 * 取得行外記號中文名稱
	 * @param loc
	 * @return
	 */
	private String getAtmLocName(String loc) {
		switch (loc) {
		case "" : return "";
		case "0": return "行內";
		case "1": return "證券自";
		case "2": return "行外778結帳";
		default : return loc;
		}	
	}
	
	/**
	 * 取得ATM所在區域中文名稱
	 * @param zone
	 * @return
	 */
	private String getAtmZoneName(String zone) {
		switch (zone) {
		case "HKG" : return "香港";
		case "MAC" : return "澳門";
		case "TWN" : return "台灣";
		case ""    : return "";
		default : return zone;
		}			
	}
	
	/**
	 * 取得跨區交易記號中文名稱
	 * @param crossFlag
	 * @return
	 */
	private String getCrossFlagName(String crossFlag) {
		switch (crossFlag) {
		case "0" : return "非跨區交易";
		case "1" : return "跨區交易";
		default : return "";
		}		
	}
}
