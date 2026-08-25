package com.syscom.fep.enchelper;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.mybatis.model.Feptxn;

/**
 * !!!注意, 這裡繼承的ENCHelper類必須是最後一XXXENCHelper類
 * 
 * !!!目前的繼承順序是ENCHelper-->APPENCHelper-->FISCENCHelper-->ATMENCHelper-->CommonENCHelper
 * 
 * !!!並且除了ENCHelper類之外, 其他XXXENCHelper類的構建函數必須是protected, 也就是說ENCHelper類是入口
 * 
 * @author Richard
 *
 */
public final class ENCHelper extends RMENCHelper {

	public ENCHelper(Feptxn feptxn, MessageBase txData) {
		super(feptxn, txData);
	}

	public ENCHelper(Feptxn feptxn) {
		super(feptxn);
	}

	public ENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq, MessageBase txData) {
		super(msgId, channel, subsys, ej, atmno, atmseq, txData);
	}
	
	public ENCHelper(FISCData txData) {
		super(txData);
	}

	/**
	 * For TSM
	 *
	 * @param msgId
	 * @param ej
	 */
	public ENCHelper(String msgId, int ej) {
		super(msgId, ej);
	}

	/**
	 * 2022-07-28 Richard add
	 *
	 * @param txData
	 */
	public ENCHelper(MessageBase txData) {
		super(txData);
	}
}
