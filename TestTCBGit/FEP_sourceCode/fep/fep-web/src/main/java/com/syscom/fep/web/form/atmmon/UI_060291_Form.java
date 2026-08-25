package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.form.BaseForm;

import java.util.List;

public class UI_060291_Form extends BaseForm {

	private static final long serialVersionUID = 1L;

	private List<UI_060291_Form_TreeData> treeData;

	private String[] checkList;

	private List<Channel> channelList;

	private Sysstat sysstat;

	private Integer channelLoopSize;
	private Integer oneRowSize;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<UI_060291_Form_TreeData> getTreeData() {
		return treeData;
	}

	public void setTreeData(List<UI_060291_Form_TreeData> treeData) {
		this.treeData = treeData;
	}

	public String[] getCheckList() {
//		return checkList;
		return checkList != null ? checkList.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】
	}

	public void setCheckList(String[] checkList) {
//		this.checkList = checkList;
		this.checkList = checkList != null ? checkList.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】

	}

	public List<Channel> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}

	public Sysstat getSysstat() {
		return sysstat;
	}

	public void setSysstat(Sysstat sysstat) {
		this.sysstat = sysstat;
	}

	public Integer getChannelLoopSize() {
		return channelLoopSize;
	}

	public void setChannelLoopSize(Integer channelLoopSize) {
		this.channelLoopSize = channelLoopSize;
	}

	public Integer getOneRowSize() {
		return oneRowSize;
	}

	public void setOneRowSize(Integer oneRowSize) {
		this.oneRowSize = oneRowSize;
	}
}
