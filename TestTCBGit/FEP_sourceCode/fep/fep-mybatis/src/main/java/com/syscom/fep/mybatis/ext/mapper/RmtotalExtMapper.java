package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmtotalMapper;
import com.syscom.fep.mybatis.model.Rmtotal;

import jakarta.annotation.Resource;

@Resource
public interface RmtotalExtMapper extends RmtotalMapper {
    /*
    * ZK ADD
    * */
    int updateForProcessRMTOTAL(Rmtotal rmtotal);
}