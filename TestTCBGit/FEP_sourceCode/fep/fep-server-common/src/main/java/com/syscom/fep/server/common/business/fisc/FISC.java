package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;

public class FISC extends FISCRmCheck {

	public FISC(){
		super();
	}

	public FISC(FISCData fiscMessage){
		super(fiscMessage);
	}
}