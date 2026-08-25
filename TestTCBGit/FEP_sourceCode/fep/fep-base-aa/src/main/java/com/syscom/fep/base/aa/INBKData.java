package com.syscom.fep.base.aa;

import com.syscom.fep.mybatis.model.Ictltxn;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;

public class INBKData extends MessageBase {
	private INBKGeneral txObject;
	private NBData txNBData;
	private RCV_NB_GeneralTrans_RQ txObject1;

	public NBData getTxNBData() {
		return txNBData;
	}

	public void setTxNBData(NBData txNBData) {
		this.txNBData = txNBData;
	}

	public RCV_NB_GeneralTrans_RQ getTxObject1() {
		return txObject1;
	}

	public void setTxObject1(RCV_NB_GeneralTrans_RQ txObject1) {
		this.txObject1 = txObject1;
	}

	public final INBKGeneral getTxObject() {
		return txObject;
	}

	public final void setTxObject(INBKGeneral txObject) {
		this.txObject = txObject;
	}
}
