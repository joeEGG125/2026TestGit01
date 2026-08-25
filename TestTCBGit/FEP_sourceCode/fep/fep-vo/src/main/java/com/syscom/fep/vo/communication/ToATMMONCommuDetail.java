package com.syscom.fep.vo.communication;

import java.io.Serializable;

import com.syscom.fep.frmcommon.util.XmlUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接受ATMMON的電文
 * 
 * @author Han
 */
public class ToATMMONCommuDetail implements Serializable {
	private static final long serialVersionUID = -5220733298948875794L;
	
	private String ATMNo;
	private String ConnectStatus;
	private String ServiceStatus;
	private String Enable;
	public String getATMNo() {
		return ATMNo;
	}
	public void setATMNo(String aTMNo) {
		ATMNo = aTMNo;
	}
	public String getConnectStatus() {
		return ConnectStatus;
	}
	public void setConnectStatus(String connectStatus) {
		ConnectStatus = connectStatus;
	}
	public String getServiceStatus() {
		return ServiceStatus;
	}
	public void setServiceStatus(String serviceStatus) {
		ServiceStatus = serviceStatus;
	}
	public String getEnable() {
		return Enable;
	}
	public void setEnable(String enable) {
		Enable = enable;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
