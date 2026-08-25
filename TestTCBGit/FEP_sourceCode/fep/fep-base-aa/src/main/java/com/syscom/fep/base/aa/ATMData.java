package com.syscom.fep.base.aa;

import com.syscom.fep.mybatis.model.Atmstat;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.hce.HCEGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.t24.T24PreClass;

public class ATMData extends MessageBase {
	/**
	 * EATM電文通用物件
	 */
	private ATMGeneral eatmtxObject;
	/**
	 * ATM電文通用物件
	 */
	private ATMGeneral txObject;
	/**
	 * HCE電文通用物件
	 */
	private HCEGeneral txHceObject;

	public NBData getTxNBData() {
		return txNBData;
	}

	public void setTxNBData(NBData txNBData) {
		this.txNBData = txNBData;
	}

	private NBData txNBData;
	/**
	 * ATM交易序號
	 */
	private String atmSeq;
	/**
	 * ATMSTAT物件
	 */
	private Atmstat atmStatus;
	private Bin bin;
	private Zone atmZone;
	private Zone cardZone;
	private T24PreClass t24Response;
	private String fscode;			//ATM交易代號
	private String atmNo;			//機台編號
	private String msgCategory;		//Message category
	private String msgType;			//Message type
	private String msgID;

	public RCV_NB_GeneralTrans_RQ getTxObject1() {
		return txObject1;
	}

	public void setTxObject1(RCV_NB_GeneralTrans_RQ txObject1) {
		this.txObject1 = txObject1;
	}

	private RCV_NB_GeneralTrans_RQ txObject1;

	public String getMsgCategory() {
		return msgCategory;
	}

	public void setMsgCategory(String msgCategory) {
		this.msgCategory = msgCategory;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public ATMGeneral getTxObject() {
		return txObject;
	}

	public ATMGeneral getEATMTxObject() {
		return eatmtxObject;
	}
	public void setTxObject(ATMGeneral txObject) {
		this.txObject = txObject;
	}

	public String getAtmSeq() {
		return atmSeq;
	}

	public void setAtmSeq(String atmSeq) {
		this.atmSeq = atmSeq;
	}

	public String getAtmNo() {
		return atmNo;
	}

	public void setAtmNo(String atmNo) {
		this.atmNo = atmNo;
	}
	
	public Atmstat getAtmStatus() {
		return atmStatus;
	}

	public void setAtmStatus(Atmstat atmStatus) {
		this.atmStatus = atmStatus;
	}

	public Bin getBin() {
		return bin;
	}

	public void setBin(Bin bin) {
		this.bin = bin;
	}

	public Zone getAtmZone() {
		return atmZone;
	}

	public void setAtmZone(Zone atmZone) {
		this.atmZone = atmZone;
	}

	public Zone getCardZone() {
		return cardZone;
	}

	public void setCardZone(Zone cardZone) {
		this.cardZone = cardZone;
	}

	public T24PreClass getT24Response() {
		return t24Response;
	}

	public void setT24Response(T24PreClass t24Response) {
		this.t24Response = t24Response;
	}

	public String getFscode() {
		return fscode;
	}

	public void setFscode(String fscode) {
		this.fscode = fscode;
	}

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public HCEGeneral getTxHceObject() {
		return txHceObject;
	}

	public void setTxHceObject(HCEGeneral txHceObject) {
		this.txHceObject = txHceObject;
	}
}
