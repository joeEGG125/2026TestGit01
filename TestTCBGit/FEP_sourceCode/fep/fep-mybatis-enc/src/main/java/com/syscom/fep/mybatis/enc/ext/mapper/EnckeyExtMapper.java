package com.syscom.fep.mybatis.enc.ext.mapper;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.enc.mapper.EnckeyMapper;

@Resource
public interface EnckeyExtMapper extends EnckeyMapper {
	/**
	 * 2021-05-24 Richard add
	 * 
	 * @param bankid
	 * @param keytype
	 * @param keykind
	 * @param keyfn
	 * @param updateType
	 * @param newKey
	 * @return
	 */
	int updateKey(@Param("bankid") String bankid,
			@Param("keytype") String keytype,
			@Param("keykind") String keykind,
			@Param("keyfn") String keyfn,
			@Param("updateType") int updateType,
			@Param("newKey") String newKey);
}