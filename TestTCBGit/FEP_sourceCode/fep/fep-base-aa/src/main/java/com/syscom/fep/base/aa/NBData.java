package com.syscom.fep.base.aa;

import java.math.BigDecimal;

import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.mybatis.model.Vatxn;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.nb.NBFEPGeneral;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.VAFEPGeneral;

public class NBData extends MessageBase {

	private NBFEPGeneral txNbfepObject;
	private VAFEPGeneral txVafepObject;
	private RCV_NB_GeneralTrans_RQ txNBObject1;
	private RCV_VA_GeneralTrans_RQ txVAObject1;
	private NBGeneral txNBObject;

	public Vatxn getVatxn() {
		return vatxn;
	}

	public void setVatxn(Vatxn vatxn) {
		this.vatxn = vatxn;
	}

	private Vatxn vatxn;

	public NBGeneral getTxNBObject() {
		return txNBObject;
	}

	public void setTxNBObject(NBGeneral txNBObject) {
		this.txNBObject = txNBObject;
	}

	public ATMGeneral getAtmtxObject() {
		return atmtxObject;
	}

	public void setAtmtxObject(ATMGeneral atmtxObject) {
		this.atmtxObject = atmtxObject;
	}

	private ATMGeneral atmtxObject;


	public BigDecimal getCharge() {
		return charge;
	}

	public void setCharge(BigDecimal charge) {
		this.charge = charge;
	}

	public String getBrch() {
		return brch;
	}

	public void setBrch(String brch) {
		this.brch = brch;
	}

	public String getChargeFlag() {
		return chargeFlag;
	}

	public void setChargeFlag(String chargeFlag) {
		this.chargeFlag = chargeFlag;
	}

	private BigDecimal charge;
	private String brch;
	private String chargeFlag;

	public NBData getTxNBData() {
		return txNBData;
	}

	public void setTxNBData(NBData txNBData) {
		this.txNBData = txNBData;
	}

	private NBData txNBData;


	public NBFEPGeneral getTxNbfepObject() {
		return txNbfepObject;
	}

	public void setTxNbfepObject(NBFEPGeneral txNbfepObject) {
		this.txNbfepObject = txNbfepObject;
	}

	public NBGeneral getTxObject() {
		return txNBObject;
	}

	public void setTxObject(NBGeneral txNBObject) {
		this.txNBObject = txNBObject;
	}

	public RCV_NB_GeneralTrans_RQ getTxNBObject1() {
		return txNBObject1;
	}

	public void setTxNBObject1(RCV_NB_GeneralTrans_RQ txNBObject1) {
		this.txNBObject1 = txNBObject1;
	}

	public VAFEPGeneral getTxVafepObject() {
		return txVafepObject;
	}

	public void setTxVafepObject(VAFEPGeneral txVafepObject) {
		this.txVafepObject = txVafepObject;
	}

	public RCV_VA_GeneralTrans_RQ getTxVAObject1() {
		return txVAObject1;
	}

	public void setTxVAObject1(RCV_VA_GeneralTrans_RQ txVAObject1) {
		this.txVAObject1 = txVAObject1;
	}

}
