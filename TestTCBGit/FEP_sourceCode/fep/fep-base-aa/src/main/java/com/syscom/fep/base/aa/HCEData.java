package com.syscom.fep.base.aa;

import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.hce.HCEGeneral;

public class HCEData extends MessageBase {

	private HCEGeneral txHceObject;
	
	/**
	 * ATM電文通用物件
	 */
	private ATMGeneral atmtxObject;
	
	private Npsunit npsunit;

	public HCEGeneral getTxObject() {
		return txHceObject;
	}
	public void setTxObject(HCEGeneral txHceObject) {
		this.txHceObject = txHceObject;
	}
	public Npsunit getNpsunit() {
		return npsunit;
	}
	public void setNpsunit(Npsunit npsunit) {
		this.npsunit = npsunit;
	}
	public ATMGeneral getAtmtxObject() {
		return atmtxObject;
	}
	public void setAtmtxObject(ATMGeneral atmtxObject) {
		this.atmtxObject = atmtxObject;
	}

}
