package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FcaptotMapper;
import com.syscom.fep.mybatis.model.Fcaptot;

import jakarta.annotation.Resource;

@Resource
public interface FcaptotExtMapper extends FcaptotMapper {
    /*
     * 2021/10/14 zk add
     * */
    int updateForProcessFCAPTOT(Fcaptot fcaptot);
}
