package com.syscom.fep.mybatis.ext.mapper;

import java.util.HashMap;
import java.util.List;

import com.syscom.fep.mybatis.mapper.CurcdMapper;
import com.syscom.fep.mybatis.model.Curcd;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface CurcdExtMapper extends CurcdMapper {
	/**
	 * add by wj
	 */
	List<Curcd> selectAll();

	/**
	 * xy add
	 */
	List<HashMap<String,String>> queryAllCurcdAlpha3(@Param("orderBy") String orderBy);
}