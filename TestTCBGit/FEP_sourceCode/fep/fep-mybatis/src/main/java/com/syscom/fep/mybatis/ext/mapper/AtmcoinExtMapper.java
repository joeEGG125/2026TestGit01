package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.AtmcoinMapper;
import com.syscom.fep.mybatis.model.Atmcoin;
import jakarta.annotation.Resource;

@Resource
public interface AtmcoinExtMapper extends AtmcoinMapper {
	/**
	 * 2021/04/29
	 *
	 * ZhaoKai ADD
	 */
	List<Long> getAtmCoinForInventoryCash(@Param("atmcoinatmno") String atmcoinatmno, @Param("atmcoinrwtseqno") Integer atmcoinrwtseqno);

	/**
	 * 2021/04/30
	 *
	 * ZhaoKai ADD
	 */
	int updateByCandidateKeyAccumulation(Atmcoin record);

	/**
	 * 2022/05/23
	 *
	 * Han ADD
	 */
	List<Map<String,String>> GetATMCoinByAtmnoRwtseqnoSettleforTTC(@Param("feptxnAtmno")String feptxnAtmno);

	/**
	 * 2022/05/24
	 *
	 * Han ADD
	 */
	List<Map<String, String>> GetATMCoinByAtmnoRwtseqnoSettle2(@Param("ATMCOIN_RWT_SEQNO")String ATMCOIN_RWT_SEQNO, @Param("ATMCOIN_SETTLE")int ATMCOIN_SETTLE);
	
	/**
	 * 2022/05/24
	 *
	 * Han ADD
	 */
	List<Map<String, String>> GetATMCoinByAtmnoRwtseqnoSettle3(@Param("ATMCOIN_ATMNO")String ATMCOIN_ATMNO, @Param("ATMCOIN_RWT_SEQNO")String ATMCOIN_RWT_SEQNO, @Param("ATMCOIN_SETTLE")int ATMCOIN_SETTLE);
}