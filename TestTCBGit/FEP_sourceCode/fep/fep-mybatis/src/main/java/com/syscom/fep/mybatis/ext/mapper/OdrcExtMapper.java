package com.syscom.fep.mybatis.ext.mapper;

import java.util.Map;

import com.syscom.fep.mybatis.mapper.OdrcMapper;
import com.syscom.fep.mybatis.model.Odrc;
import jakarta.annotation.Resource;

@Resource
public interface OdrcExtMapper extends OdrcMapper {
	/**
	 * 2021-07-20 Richard add
	 * 
	 * @param record
	 * @return
	 */
	Map<String, Object> getOdrcByDay(Odrc record);
}