package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmtotMapper;
import com.syscom.fep.mybatis.model.Rmtot;

import jakarta.annotation.Resource;

@Resource
public interface RmtotExtMapper extends RmtotMapper {
    /*
    * ZK ADD
    * */
    int updateForProcessRMTOT(Rmtot rmtot);
}
