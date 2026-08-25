package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CbactMapper;
import com.syscom.fep.mybatis.model.Cbact;
import jakarta.annotation.Resource;

@Resource
public interface CbactExtMapper extends CbactMapper {
	/**
	 * 2021/04/28
	 * WJ add
	 */
	List<Cbact> queryCbactForMask(@Param("cbactBkno") String cbactBkno, @Param("cbactActno") String cbactActno);
	
}