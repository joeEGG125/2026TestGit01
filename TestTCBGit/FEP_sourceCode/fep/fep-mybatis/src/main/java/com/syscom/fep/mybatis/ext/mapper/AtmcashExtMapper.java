package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.AtmcashMapper;
import com.syscom.fep.mybatis.model.Atmcash;
import jakarta.annotation.Resource;

@Resource
public interface AtmcashExtMapper extends AtmcashMapper {
	/**
	 * 2021/04/29
	 *
	 * ZhaoKai ADD
	 */
	List<Atmcash> getAtmCashByAtmNo(@Param("atmcashAtmno") String atmcashAtmno, @Param("orderBy") String orderBy);

	/**
	 * 2021/05/20 ADD BY WJ
	 */
	int updateBusinessDay(@Param("atmcashAtmno") String atmcashAtmno);

	/**
	 * 2021/05/25 ADD BY WJ
	 */
	Map<String, Object> getAtmCashByCurAtmNoForIWD(@Param("atmcashAtmno") String atmcashAtmno, @Param("atmcashCur") String atmcashCur);

	/**
	 * 2022/05/19 ADD BY Han
	 */
	List<Atmcash> GetATMCashByATMNO(@Param("feptxnAtmno")String feptxnAtmno, @Param("orderBy")String orderBy);
	
	/**
	 * 2022/05/20 ADD BY Han
	 */
	List<Atmcash> GetATMCashByATMNOGroupBy(@Param("feptxnAtmno")String feptxnAtmno);
}