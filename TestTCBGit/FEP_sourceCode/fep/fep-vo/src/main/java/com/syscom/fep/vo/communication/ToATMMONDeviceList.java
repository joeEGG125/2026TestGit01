package com.syscom.fep.vo.communication;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * 接受ATMMON的電文
 * 
 * @author Han
 */
@XStreamAlias("request")
public class ToATMMONDeviceList extends BaseJsonCommu {	
	private String GroupID;
	private String MCNo;
	private String Model;
	public String getGroupID() {
		return GroupID;
	}
	public void setGroupID(String groupID) {
		GroupID = groupID;
	}
	public String getMCNo() {
		return MCNo;
	}
	public void setMCNo(String mCNo) {
		MCNo = mCNo;
	}
	public String getModel() {
		return Model;
	}
	public void setModel(String model) {
		Model = model;
	}

	
	
	
}
