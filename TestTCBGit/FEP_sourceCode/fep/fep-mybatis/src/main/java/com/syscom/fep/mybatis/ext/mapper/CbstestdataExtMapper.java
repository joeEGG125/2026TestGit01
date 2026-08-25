package com.syscom.fep.mybatis.ext.mapper;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CbstestdataMapper;
import com.syscom.fep.mybatis.model.Cbstestdata;

import java.util.List;

@Resource
public interface CbstestdataExtMapper extends CbstestdataMapper {
	public Cbstestdata selectByPrimaryKey(@Param("CBSTxid")String CBSTxid);
	public List<Cbstestdata> selectAll();
}