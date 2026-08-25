package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.ws.client.WsClientFactory;
import com.syscom.fep.ws.client.entity.WsClientType;
import org.apache.commons.lang3.StringUtils;

public class T24Adapter extends AdapterBase {
	/**
	 * 從T24回來的電文
	 */
	public String messageFromT24;
	/**
	 * 送給T24的電文
	 */
	public String messageToT24;
	/**
	 * 地區代碼,用來判斷送那一台T24主機,預設為台灣主機
	 */
	private String area = "TWN"; // TODO ZK 賦初始值
	private MessageBase txData;
	// 2014-07-15 Modify by Ruling for 時間相近時，第一筆原送香港但實際卻送到台灣故將static拿掉
	private String t24ServiceUrl;
	private WsClientFactory wsClientFactory = SpringBeanFactoryUtil.getBean(WsClientFactory.class);

	public T24Adapter(MessageBase txData) {
		this.txData = txData;
	}

	public String getMessageFromT24() {
		return messageFromT24;
	}

	public void setMessageFromT24(String messageFromT24) {
		this.messageFromT24 = messageFromT24;
	}

	public String getMessageToT24() {
		return messageToT24;
	}

	public void setMessageToT24(String messageToT24) {
		this.messageToT24 = messageToT24;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Override
	public FEPReturnCode sendReceive() {
		FEPReturnCode rtnCode = CommonReturnCode.CBSResponseError;
		// t24ServiceUrl來自sysconf表中，name=T24ServiceUrl的數據
		if (this.txData.getTxSubSystem() == SubSystem.RM || this.txData.getLogContext().getSubSys() == SubSystem.RM) {
			this.t24ServiceUrl = RMConfig.getInstance().getT24ServiceUrl();
		} else {
			switch (this.area) {
				case ZoneCode.TWN:
					this.t24ServiceUrl = CMNConfig.getInstance().getT24ServiceUrl();
					break;
				case ZoneCode.MAC:
					this.t24ServiceUrl = ATMPConfig.getInstance().getMOT24ServiceUrl();
					break;
				case ZoneCode.HKG:
					this.t24ServiceUrl = ATMPConfig.getInstance().getHKT24ServiceUrl();
					break;
				default:
					this.t24ServiceUrl = CMNConfig.getInstance().getT24ServiceUrl();
					break;
			}
		}
		this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
		this.txData.getLogContext().setMessage(this.messageToT24);
		this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
		this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
		this.txData.getLogContext().setRemark(StringUtils.join("Ready Send data to T24 ", this.t24ServiceUrl));
		this.logMessage(this.txData.getLogContext());
		try {
			@SuppressWarnings("unused")
			String key = StringUtils.join(this.txData.getMessageID(), "_", this.getEj());
			this.messageFromT24 = this.sendMsgToT24(this.messageToT24);
			return FEPReturnCode.Normal;
		} catch (Exception e) {
			// TODO 還有更多的Exception，待補充
			this.txData.getLogContext().setProgramException(e);
			sendEMS(this.txData.getLogContext());
			rtnCode = CommonReturnCode.ProgramException;
		} finally {
			this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
			this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
			this.txData.getLogContext().setMessage(this.messageFromT24);
			this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
			this.txData.getLogContext().setRemark(StringUtils.join("Get data from T24 ", this.t24ServiceUrl));
			this.logMessage(this.txData.getLogContext());
		}
		// 呼叫T24所提供的Web Service
		return rtnCode;
	}

	/**
	 * Fly 2019/06/26 For R18 T24升級
	 * 
	 * @param reqData
	 * @return
	 */
	private String sendMsgToT24(String reqData) throws Exception {
		if (this.txData.getTxSubSystem() == SubSystem.RM || this.txData.getLogContext().getSubSys() == SubSystem.RM) {
			// 接口是一樣的，只是url不同而已
			return wsClientFactory.sendReceive(WsClientType.T24, this.t24ServiceUrl, this.messageToT24);
		} else {
			if (ZoneCode.MAC.equals(this.area) || ZoneCode.HKG.equals(this.area)) {
				// 接口是一樣的，只是url不同而已
				return wsClientFactory.sendReceive(WsClientType.T24, this.t24ServiceUrl, this.messageToT24);
			} else {
				// 接口是一樣的，只是url不同而已
				return wsClientFactory.sendReceive(WsClientType.T24, this.t24ServiceUrl, this.messageToT24);
			}
		}
	}
}
