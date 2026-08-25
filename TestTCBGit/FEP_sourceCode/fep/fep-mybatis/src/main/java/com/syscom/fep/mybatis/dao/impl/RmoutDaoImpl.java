package com.syscom.fep.mybatis.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.mybatis.dao.RmoutDao;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.model.Rmout;

@Repository
@Lazy
public class RmoutDaoImpl implements RmoutDao {
	private static final LogHelper logger = LogHelperFactory.getRepositoryLogger();
	@Autowired
	private PlatformTransactionManager fepdbTransactionManager;
	@Autowired
	private RmoutExtMapper mapper;

	/**
	 * 2021-12-17 Richard add
	 * 
	 * 因為SQL為SELECT ... FOR UPDATE, 會對行進行鎖定, 所以這裡包一層事務, SQL執行完畢立即commit避免被鎖
	 */
	@Override
	public Rmout queryByPrimaryKeyWithUpdLock(String rmoutTxdate, String rmoutBrno, String rmoutOriginal, String rmoutFepno) {
		TransactionStatus txStatus = fepdbTransactionManager.getTransaction(new DefaultTransactionDefinition());
		Rmout rmout = null;
		try {
			rmout = mapper.queryByPrimaryKeyWithUpdLock(rmoutTxdate, rmoutBrno, rmoutOriginal, rmoutFepno);
			fepdbTransactionManager.commit(txStatus);
		} catch (Exception e) {
			logger.error(e, e.getMessage());
			fepdbTransactionManager.rollback(txStatus);
			throw e;
		}
		return rmout;
	}
}
