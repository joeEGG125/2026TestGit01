package com.syscom.fep.vo.text.atm;

/**
 * 2021-07-22 僅供ATM XML類的電文vo繼承
 * 
 * @author Richard
 *
 */
public abstract class ATMXmlBase extends ATMTextBase {
	
	@Override
	public ATMGeneral parseFlatfile(String flatfile) throws Exception {
		return null;
	}

	@Override
	public int getTotalLength() {
		return 0;
	}
}
