package com.syscom.fep.mybatis.ext.mapper;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CardtsmMapper;
import com.syscom.fep.mybatis.model.Cardtsm;
import jakarta.annotation.Resource;

@Resource
public interface CardtsmExtMapper extends CardtsmMapper {

	/**
	 * 2021-04-19 ZhaoKai add
	 */
	Cardtsm getSingleCard(@Param("cardCardno") String cardCardno);
}