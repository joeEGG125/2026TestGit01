package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.BitmapdefMapper;
import com.syscom.fep.mybatis.model.Bitmapdef;
import jakarta.annotation.Resource;

@Resource
public interface BitmapdefExtMapper extends BitmapdefMapper {
	/**
	 * 2021-04-20 ZhaoKai add
	 * 
	 * @param orderby
	 * @return
	 */
	List<Bitmapdef> queryAllData(@Param("orderBy") String orderBy);
}