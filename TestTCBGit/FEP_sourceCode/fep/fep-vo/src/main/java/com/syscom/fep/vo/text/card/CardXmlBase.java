package com.syscom.fep.vo.text.card;

/**
 * 2021-11-16 僅供Card XML類的電文vo繼承
 * 
 * @author Richard
 *
 */
public abstract class CardXmlBase extends CardTextBase {
	
	@Override
	public CardGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public int getTotalLength() {
		return 0;
	}
}
