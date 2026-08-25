package com.syscom.fep.mybatis.ext.mapper;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.BusiMapper;
import com.syscom.fep.mybatis.model.Busi;
import jakarta.annotation.Resource;

@Resource
public interface BusiExtMapper extends BusiMapper {
	/**
	 * ADD BY WJ 20210525
	 * 
	 * @param busiIdno
	 * @return
	 */
	Busi selectById(@Param("busiIdno") String busiIdno);
}