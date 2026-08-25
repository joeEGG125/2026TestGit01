package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import com.syscom.fep.mybatis.mapper.ProgramMapper;

@Resource
public interface ProgramExtMapper extends ProgramMapper{

	/**
	 * Bruce add 已授權及未授權程式
	 * @return
	 */
	public List<Map<String, Object>> getProgramf();
}
