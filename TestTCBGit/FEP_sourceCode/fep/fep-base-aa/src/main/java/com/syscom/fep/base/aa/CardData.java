package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.card.CardGeneral;

public class CardData extends MessageBase {
	private CardGeneral txObject;

	public CardGeneral getTxObject() {
		return txObject;
	}

	public void setTxObject(CardGeneral txObject) {
		this.txObject = txObject;
	}
}