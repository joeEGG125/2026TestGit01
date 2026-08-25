package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmbtchmtrMapper;
import com.syscom.fep.mybatis.model.Rmbtchmtr;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Resource
public interface RmbtchmtrExtMapper extends RmbtchmtrMapper {

    // add by wj 2021/11/30
    List<HashMap<String,Object>> getRMBTCHMTRByDef(@Param("rmbtchmtr")Rmbtchmtr rmbtchmtr);
}
