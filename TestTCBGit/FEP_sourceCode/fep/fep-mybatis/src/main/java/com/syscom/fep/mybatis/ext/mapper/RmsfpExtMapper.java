package com.syscom.fep.mybatis.ext.mapper;


import com.syscom.fep.mybatis.mapper.RmsfpMapper;
import com.syscom.fep.mybatis.model.Rmsfp;

import jakarta.annotation.Resource;

@Resource
public interface RmsfpExtMapper extends RmsfpMapper {

    Rmsfp getRMSFPByPKBeforeTbsdy(Rmsfp rmsfp);
}
