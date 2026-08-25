package com.syscom.fep.web.form.common;

import java.util.List;
import java.util.Map;

import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.BaseForm;
import com.syscom.safeaa.mybatis.vo.SyscomroleInfoVo;
import com.syscom.safeaa.mybatis.vo.SyscomrolemembersAndCulture;

public class UI_080040_Form extends BaseForm {
	private static final long serialVersionUID = 1L;

	private Integer pid;

	private Integer id;

	private String no;

	private Integer empid;

	private String name;

	private String usermail;

	private String effectdate;

	private String expireddate;

	private List<SyscomroleInfoVo> dataList;

	private List<Map<String, String>> allList;

	private List<SyscomrolemembersAndCulture> selectList;

	private List<Integer> ids;

	/**
	 * 執行結果
	 */
	private boolean result = true;
	/**
	 * 訊息類別
	 */
	private MessageType messageType;
	/**
	 * 訊息內容
	 */
	private String message = "";

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public Integer getEmpid() {
		return empid;
	}

	public void setEmpid(Integer empid) {
		this.empid = empid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsermail() {
		return usermail;
	}

	public void setUsermail(String usermail) {
		this.usermail = usermail;
	}

	public String getEffectdate() {
		return effectdate;
	}

	public void setEffectdate(String effectdate) {
		this.effectdate = effectdate;
	}

	public String getExpireddate() {
		return expireddate;
	}

	public void setExpireddate(String expireddate) {
		this.expireddate = expireddate;
	}

	public List<SyscomroleInfoVo> getDataList() {
		return dataList;
	}

	public void setDataList(List<SyscomroleInfoVo> dataList) {
		this.dataList = dataList;
	}

	public List<Map<String, String>> getAllList() {
		return allList;
	}

	public void setAllList(List<Map<String, String>> allList) {
		this.allList = allList;
	}

	public List<SyscomrolemembersAndCulture> getSelectList() {
		return selectList;
	}

	public void setSelectList(List<SyscomrolemembersAndCulture> selectList) {
		this.selectList = selectList;
	}

	public List<Integer> getIds() {
		return ids;
	}

	public void setIds(List<Integer> ids) {
		this.ids = ids;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessage(MessageType messageType, String message) {
		this.setMessageType(messageType);
		this.setMessage(message);
	}
}
