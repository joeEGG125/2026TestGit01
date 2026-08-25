package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.UpbinMapper;
import com.syscom.fep.mybatis.model.Upbin;
import jakarta.annotation.Resource;

@Resource
public interface UpbinExtMapper extends UpbinMapper {

	/**
	 * 2021-04-20 ZhaoKai add
	 * 
	 * @param orderBy
	 * @return
	 */
	List<Upbin> queryAllData(@Param("orderBy") String orderBy);
	
	int deleteAll();

}