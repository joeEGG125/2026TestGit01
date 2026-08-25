package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FcmsginMapper;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface FcmsginExtMapper extends FcmsginMapper {
    /*
    * 2021/10/14 zk add
    * */
    int checkRemitNumberExist(@Param("txDate")String txDate, @Param("snederBank")String snederBank, @Param("receiverBank")String receiverBank);
}
