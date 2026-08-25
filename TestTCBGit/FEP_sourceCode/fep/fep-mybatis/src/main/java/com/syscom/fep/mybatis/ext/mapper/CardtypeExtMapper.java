package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import com.syscom.fep.mybatis.mapper.CardtypeMapper;
import com.syscom.fep.mybatis.model.Cardtype;
import jakarta.annotation.Resource;

@Resource
public interface CardtypeExtMapper extends CardtypeMapper {
	/**
	 * ZhaoKai add 2021/05/19
	 */
	List<Cardtype> queryAllData();
}