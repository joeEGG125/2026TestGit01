package com.syscom.fep.mybatis.dao;

import com.syscom.fep.mybatis.model.Rmoutt;

public interface RmouttDao {

	Rmoutt queryByPrimaryKeyWithUpdLock(String rmouttTxdate, String rmouttBrno, String rmouttOriginal, String rmouttFepno);
	
}
