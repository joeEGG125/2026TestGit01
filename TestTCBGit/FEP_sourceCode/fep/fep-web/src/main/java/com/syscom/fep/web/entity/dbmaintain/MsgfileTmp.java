package com.syscom.fep.web.entity.dbmaintain;

import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.model.Msgfile;

public class MsgfileTmp extends Msgfile{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
     * 來源通道轉中文
     */
    private String msgfileChannelTxt;
    
    /**
     * 子系統轉中文
     */
    private String msgfileSubsysTxt;
    
    /**
     * 送事件監控轉中文
     */
    private String msgfileSendEmsTxt;
    
	public MsgfileTmp() {}
    
	public MsgfileTmp(Msgfile msgfile) {
		if (msgfile == null)
			return;
		BeanUtils.copyProperties(msgfile, this);
	}

    public String getMsgfileSendEmsTxt() {
		return msgfileSendEmsTxt;
	}

	public void setMsgfileSendEmsTxt(String msgfileSendEmsTxt) {
		this.msgfileSendEmsTxt = msgfileSendEmsTxt;
	}

    public String getMsgfileSubsysTxt() {
		return msgfileSubsysTxt;
	}

	public void setMsgfileSubsysTxt(String msgfileSubsysTxt) {
		this.msgfileSubsysTxt = msgfileSubsysTxt;
	}

    public String getMsgfileChannelTxt() {
		return msgfileChannelTxt;
	}

	public void setMsgfileChannelTxt(String msgfileChannelTxt) {
		this.msgfileChannelTxt = msgfileChannelTxt;
	}
}
