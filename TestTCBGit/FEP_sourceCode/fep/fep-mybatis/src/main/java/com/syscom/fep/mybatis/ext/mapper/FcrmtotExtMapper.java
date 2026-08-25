package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FcrmtotMapper;
import com.syscom.fep.mybatis.model.Fcrmtot;

import jakarta.annotation.Resource;

@Resource
public interface FcrmtotExtMapper extends FcrmtotMapper {
    /*
     * 2021/10/14 zk add
     * */
    int updateForProcessFCRMTOT(Fcrmtot fcrmtot);
}
