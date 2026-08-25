package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.SubsysMapper;
import com.syscom.fep.mybatis.model.Subsys;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Resource
public interface SubsysExtMapper extends SubsysMapper {
    /*
    * zk 2022-1-12
    * */
    List<Subsys> queryAll();
    
    /**
     * Han add 2002-06-07
     * @param orderBy
     * @return List<Subsys>
     */
	List<Subsys> queryAllData(@Param("orderBy")String orderBy);
}