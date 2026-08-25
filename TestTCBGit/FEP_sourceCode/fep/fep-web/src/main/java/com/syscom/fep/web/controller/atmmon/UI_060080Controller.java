package com.syscom.fep.web.controller.atmmon;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Alarm;
import com.syscom.fep.mybatis.model.Event;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060080_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * ATM異常訊息維護(系統管理)
 * @author Ben
 *
 */
@Controller
public class UI_060080Controller extends BaseController{
	
	@Autowired
	AtmService atmService;
	
	@Override
	public void pageOnLoad(ModelMap mode) {
		// 初始化表單資料
		UI_060080_Form form = new UI_060080_Form();
		form.setUrl("/atmmon/UI_060080/queryClick");
		this.queryClick(form, mode);
	}
	
	@PostMapping(value = "/atmmon/UI_060080/queryClick")
	private String queryClick(@ModelAttribute UI_060080_Form form, ModelMap mode) {
		bindGridData(form, mode);
		return Router.UI_060080.getView();
	}
	
	/**
	 * 資料整理
	 * 依查詢條件查詢主程式
	 */
	private void bindGridData(UI_060080_Form form, ModelMap mode) {
		this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			PageInfo<Alarm> pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
				@Override
				public void doSelect() {
					atmService.getAlarmByPKLike(form.getAlarm_no());
				}
			});
			if (pageInfo.getSize() == 0) {
				this.showMessage(mode, MessageType.INFO, QueryNoData);
				return;
			}
			PageData<UI_060080_Form, Alarm> pageData = new PageData<>(pageInfo, form);
			WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.WARNING, programError);
		}
	}
	
	/**
	 * 新增鈕、Grid中第二列修改按鈕 Event(共用畫面)
	 * 區別方式：form.getBtnType()= "I"=新增、"E"=修改 
	 */
	@PostMapping(value = "/atmmon/UI_060080/showDetail")
	private String showDetail(@ModelAttribute UI_060080_Form form, ModelMap mode){
		this.infoMessage("查詢明細資料_1, 條件 = [", form.toString(), "]");
		this.doKeepFormData(mode, form);
		try {
			String btnType = form.getBtnType();
			switch(btnType){
		    case "I":			//按下 新增鈕 狀態
		    	form = this.clearFormControl(form);
		    	break; 
		    case "E":			//按下 Grid中第二列修改按鈕(針對唯一記錄執行明細修改作業)
		    	Alarm alarm = atmService.getAlarmByNo(form.getAlarm_no());
				form.setAlarm_name(alarm.getAlarmName());
				form.setAlarm_names(alarm.getAlarmNameS());
				form.setAlarm_icon(alarm.getAlarmIcon());
				form.setAlarm_sendems(alarm.getAlarmSendems().toString());
				form.setAlarm_log(alarm.getAlarmLog().toString());
				form.setAlarm_autostop(alarm.getAlarmAutostop().toString());
				form.setAlarm_console(alarm.getAlarmConsole().toString());
				form.setAlarm_notify_email(alarm.getAlarmNotifyEmail());
				Short notifyTimes = (short) (alarm.getAlarmNotifyTimes()== null? 0:alarm.getAlarmNotifyTimes());
				form.setAlarm_notify_times(notifyTimes.toString());
		    	break; 
			}
	    	this.infoMessage("查詢明細資料_2, 條件 = [", form.toString(), "]");
			WebUtil.putInAttribute(mode, AttributeName.Form, form);
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
		}
		return Router.UI_060080_Detail.getView();
	}
	
	/**
	 * 設定畫面Button Delete的Event
	 * @param formList
	 * @param mode
	 * @return
	 */
	@PostMapping(value = "/atmmon/UI_060080/deleteList")
	@ResponseBody
	public BaseResp<UI_060080_Form> deleteList(@RequestBody List<UI_060080_Form> formList, ModelMap mode) {
		this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
		BaseResp<UI_060080_Form> response = new BaseResp<>();
		try {
			List<Event> events;
			int eventCount=0;
			for (UI_060080_Form form : formList) {
				events = atmService.CheckEVENTForAlarmDelete(form.getAlarm_no());
				if(events.size()<1) {
					Alarm alarm = new Alarm();
					alarm.setAlarmNo(form.getAlarm_no());
					atmService.deleteAlarm(alarm);
				}else {
					eventCount++;
					this.infoMessage("無法刪除,Event筆數 = "+events.size()+ " Alarm_no="+form.getAlarm_no());
				}
			}
			if(eventCount>0) {
				response.setMessage(MessageType.INFO, "EVENT資料仍有使用此警示編號，因此無法刪除");
			}else {
				response.setMessage(MessageType.INFO, DeleteSuccess);
			}
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			// show錯誤訊息到前台頁面
			response.setMessage(MessageType.DANGER, DeleteFail);
		}
		return response;
	}
	
	@PostMapping(value = "/atmmon/UI_060080/saveClick")
	private String saveClick(@ModelAttribute UI_060080_Form form,ModelMap mode) {
		this.infoMessage("存檔, 表單完整內容 = [", form.toString(), "]");
		Alarm alarm = new Alarm();
		int alarmFlag=0;
		if (checkAllField(alarm, form, mode)) {
			try {
				String btnType = form.getBtnType();
				switch(btnType){
			    case "I":			//按下 新增鈕 狀態
			    	this.infoMessage("新增, 主鍵 = "+alarm.getAlarmNo());
			    	
			    	List<Alarm> alarmList = atmService.getAlarmByPKLike(alarm.getAlarmNo());
			    	if (alarmList.size()>0) {
			    		form.setErrorFlag("1");
						this.showMessage(mode, MessageType.INFO, Multiple);
			    	}else {
			    		atmService.insertAlarm(alarm);
			    		form = this.clearFormControl(form);
						this.showMessage(mode, MessageType.INFO, InsertSuccess);
			    	}
			    	break; 
			    case "E":			//按下 Grid中第二列修改按鈕(針對唯一記錄執行明細修改作業)
			    	this.infoMessage("修改, 主鍵 = "+alarm.getAlarmNo());
					alarmFlag = atmService.updateAlarm(alarm);
					if (alarmFlag > 0) {
						this.showMessage(mode, MessageType.INFO, UpdateSuccess);
					} else {
						this.showMessage(mode, MessageType.INFO, UpdateFail);
					}
					break; 
				}
			} catch (Exception e) {
				this.errorMessage(e, e.getMessage());
				this.showMessage(mode, MessageType.WARNING, programError);
			}
		}
		this.doKeepFormData(mode, form);
		return Router.UI_060080_Detail.getView();
	}

	/**
	 * 檢核資料正確後，即送入ALARM中，以供後續新增或修改之用
	 * @param alarm
	 * @param form
	 * @param redirectAttributes
	 * @return
	 */
	private boolean checkAllField(Alarm alarm, UI_060080_Form form ,ModelMap mode) {
		this.infoMessage("檢查表單內容, 完整內容 = [", form.toString(), "]");
		try {
			alarm.setAlarmNo(form.getAlarm_no());			
			alarm.setAlarmName(form.getAlarm_name());		
			alarm.setAlarmNameS(form.getAlarm_names());
			alarm.setAlarmIcon(form.getAlarm_icon());
			if(StringUtils.isNotBlank(form.getAlarm_sendems())) {
				alarm.setAlarmSendems(Short.parseShort(form.getAlarm_sendems()));
			}
			if(StringUtils.isNotBlank(form.getAlarm_log())){
				alarm.setAlarmLog(Short.parseShort(form.getAlarm_log()));
			}
			if(StringUtils.isNotBlank(form.getAlarm_autostop())) {
				alarm.setAlarmAutostop(Short.parseShort(form.getAlarm_autostop()));
			}
			if(StringUtils.isNotBlank(form.getAlarm_console())) {
				alarm.setAlarmConsole(Short.parseShort(form.getAlarm_console()));
			}
			alarm.setAlarmNotifyEmail(form.getAlarm_notify_email());
			short notifyTimes;
			String SnotifyTimes=form.getAlarm_notify_times();
			if(SnotifyTimes == null || SnotifyTimes.length() == 0 || !StringUtils.isNumeric(SnotifyTimes)) {
				notifyTimes=0;
			}else {
				notifyTimes=Short.parseShort(SnotifyTimes);
			}
			alarm.setAlarmNotifyTimes(notifyTimes);
			alarm.setAlarmRemark(form.getAlarm_remark());
			return true;
		} catch (Exception e) {
			this.errorMessage(e, e.getMessage());
			this.showMessage(mode, MessageType.WARNING, programError);
			return false;
		}
	}
	
	/**
	 * 連續新增模式,清除單筆表單控制項內容
	 * @return
	 */
	private UI_060080_Form clearFormControl(UI_060080_Form formz) {
		String errorFlag = formz.getErrorFlag();	//新增時違反唯一值的狀態。目的：保留其它欄位內容
		UI_060080_Form form = new UI_060080_Form();
		form.setBtnType("I");
		if(!"1".equals(errorFlag)) {		// "1"=代表 新增失敗，須保留其它欄位資訊；非"1"=代表 新增成功，連續新增模式，不須保留其它欄位資訊
			form.setAlarm_no("");
			form.setAlarm_name("");
			form.setAlarm_names("");
			form.setAlarm_icon("");
			form.setAlarm_notify_email("");
			form.setAlarm_notify_times("");
			form.setAlarm_sendems("0");
			form.setAlarm_log("0");
			form.setAlarm_autostop("0");
			form.setAlarm_console("0");
		}
		return form;
	}
}
