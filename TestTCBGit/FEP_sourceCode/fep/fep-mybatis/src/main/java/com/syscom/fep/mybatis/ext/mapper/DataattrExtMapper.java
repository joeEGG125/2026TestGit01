package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.DataattrMapper;
import com.syscom.fep.mybatis.model.Dataattr;
import jakarta.annotation.Resource;

@Resource
public interface DataattrExtMapper extends DataattrMapper {
	/**
	 * 2021/04/25
	 * ZhaoKai add
	 */
	List<Dataattr> queryAllData(@Param("orderBy") String orderBy);
}