package com.syscom.fep.mybatis.ext.mapper;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.EmvcMapper;
import jakarta.annotation.Resource;

@Resource
public interface EmvcExtMapper extends EmvcMapper {

	/**
	 * 2021-04-22 ZhaoKai add
	 */
	BigDecimal getEmvcByMonth(@Param("emvcTxDateStart") String emvcTxDateStart, @Param("emvcTxDateEnd") String emvcTxDateEnd, @Param("emvcPan") String emvcPan);

}