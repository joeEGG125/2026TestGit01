package com.syscom.fep.mybatis.ext.mapper;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.IsocodeMapper;
import com.syscom.fep.mybatis.model.Isocode;
import jakarta.annotation.Resource;

@Resource
public interface IsocodeExtMapper extends IsocodeMapper {
	/**
	 * ZhaoKai
	 * 2021/05/12 add
	 */
	Isocode queryByAlpha3(@Param("isocodeAlpha3") String isocodeAlpha3);
}