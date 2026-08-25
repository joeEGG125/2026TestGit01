package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;

/**
 * ATM異常訊息維護(查詢、新增、修改 頁面所需欄位)
 * @author Ben
 *
 */
public class UI_060080_Form extends BaseForm{

	private static final long serialVersionUID = 1L;

	private String alarm_no;			//警示代碼
	private String alarm_name;			//警示說明
	private String alarm_names;			//警示訊息
	private String alarm_icon;			//警示圖示URL
	private String alarm_sendems;		//是否送EMS
	private String alarm_log;			//是否記錄LOG
	private String alarm_autostop;		//自動停止服務
	private String alarm_console;		//顯示警示
	private String alarm_notify_email;	//Email警示
	private String alarm_notify_times;	//通知次數
	private String alarm_remark;		//備註
	private String btnType;				//按鈕別(新增=I、修改=E)
	private String errorFlag;			//存檔有誤(1=有誤)(用途：新增有誤時，須保留其它欄位內容)
	public String getAlarm_no() {
		return alarm_no;
	}
	public void setAlarm_no(String alarm_no) {
		this.alarm_no = alarm_no;
	}
	public String getAlarm_name() {
		return alarm_name;
	}
	public void setAlarm_name(String alarm_name) {
		this.alarm_name = alarm_name;
	}
	public String getAlarm_names() {
		return alarm_names;
	}
	public void setAlarm_names(String alarm_names) {
		this.alarm_names = alarm_names;
	}
	public String getAlarm_icon() {
		return alarm_icon;
	}
	public void setAlarm_icon(String alarm_icon) {
		this.alarm_icon = alarm_icon;
	}
	public String getAlarm_sendems() {
		return alarm_sendems;
	}
	public void setAlarm_sendems(String alarm_sendems) {
		this.alarm_sendems = alarm_sendems;
	}
	public String getAlarm_log() {
		return alarm_log;
	}
	public void setAlarm_log(String alarm_log) {
		this.alarm_log = alarm_log;
	}
	public String getAlarm_autostop() {
		return alarm_autostop;
	}
	public void setAlarm_autostop(String alarm_autostop) {
		this.alarm_autostop = alarm_autostop;
	}
	public String getAlarm_console() {
		return alarm_console;
	}
	public void setAlarm_console(String alarm_console) {
		this.alarm_console = alarm_console;
	}
	public String getAlarm_notify_email() {
		return alarm_notify_email;
	}
	public void setAlarm_notify_email(String alarm_notify_email) {
		this.alarm_notify_email = alarm_notify_email;
	}
	public String getAlarm_notify_times() {
		return alarm_notify_times;
	}
	public void setAlarm_notify_times(String alarm_notify_times) {
		this.alarm_notify_times = alarm_notify_times;
	}
	public String getAlarm_remark() {
		return alarm_remark;
	}
	public void setAlarm_remark(String alarm_remark) {
		this.alarm_remark = alarm_remark;
	}
	public String getBtnType() {
		return btnType;
	}
	public void setBtnType(String btnType) {
		this.btnType = btnType;
	}
	public String getErrorFlag() {
		return errorFlag;
	}
	public void setErrorFlag(String errorFlag) {
		this.errorFlag = errorFlag;
	}
}
