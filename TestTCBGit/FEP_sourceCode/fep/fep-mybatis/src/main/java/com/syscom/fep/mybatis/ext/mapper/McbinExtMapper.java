package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.McbinMapper;
import com.syscom.fep.mybatis.model.Mcbin;
import jakarta.annotation.Resource;

@Resource
public interface McbinExtMapper extends McbinMapper {

	List<Mcbin> queryAllData(@Param("orderBy") String orderBy);
}