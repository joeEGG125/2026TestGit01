package com.syscom.fep.vo.communication;

import java.io.Serializable;
import java.util.List;


/**
 * 接受ATMMON的電文
 * 
 * @author Han
 */
public class ToATMMONCommu implements Serializable {
	private static final long serialVersionUID = -5220733298948875794L;
	
	private List<ToATMMONCommuDetail> MsgRq  ;

	public List<ToATMMONCommuDetail> getMsgRq() {
		return MsgRq;
	}

	public void setMsgRq(List<ToATMMONCommuDetail> msgRq) {
		MsgRq = msgRq;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
