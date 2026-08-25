package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import com.syscom.fep.mybatis.enums.RelationalOperators;
import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.NpschkMapper;
import com.syscom.fep.mybatis.model.Npschk;

@Resource
public interface NpschkExtMapper extends NpschkMapper {


	/**
	 * Han 2022-07-21
	 * @param orderBy
	 * @return
	 */
	List<Npschk> queryAllData(@Param("orderBy")String orderBy);


	/**
	 * Han 2022-07-21
	 * @param effDate
	 * @return
	 */
	void NPSCHKtoNPSNUIT(@Param("effDate")String effDate);

	/**
     * Han 2022-07-21
     */
	List<Map<String,Object>> queryByEffDateCompare(@Param("effDate")String effDate, @Param("operators") RelationalOperators operators);

	/**
	 * 將生效資料更新全國性繳費委託單位檔 批次查詢
	 * @param dffectDate
	 * @return
	 */
	public List<Npschk> selectByEffectDate(@Param("dffectDate") String dffectDate);
}