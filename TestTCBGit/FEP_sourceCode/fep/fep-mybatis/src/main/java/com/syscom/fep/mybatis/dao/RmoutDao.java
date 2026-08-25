package com.syscom.fep.mybatis.dao;

import com.syscom.fep.mybatis.model.Rmout;

public interface RmoutDao {

	Rmout queryByPrimaryKeyWithUpdLock(String rmoutTxdate, String rmoutBrno, String rmoutOriginal, String rmoutFepno);

}
