package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.RminsnoMapper;
import com.syscom.fep.mybatis.model.Rminsno;

@Resource
public interface RminsnoExtMapper extends RminsnoMapper {

	public Rminsno queryByPrimaryKeyWithUpdLock(Rminsno oRminsno);

	// 2021/09/29 wj Add
	int updateRMINSNOforCHKG(Rminsno record);

	/**
	 * xy add BtBatchDaily 調用
	 */
	int updateRMINSNOforBatchDaily();

	List<Rminsno> queryRMINSNOByCDKEY_FLAG(Rminsno rminsno);

	List<Rminsno> selectByPrimaryKeyOne(@Param("rminsnoSenderBank") String rminsnoSenderBank, @Param("rminsnoReceiverBank") String rminsnoReceiverBank);
}
