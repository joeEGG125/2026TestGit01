package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FcrminMapper;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface FcrminExtMapper extends FcrminMapper {
    /*
     * 2021/10/14 zk add
     * */
    int checkRemitNumberExist(@Param("txDate")String txDate, @Param("snederBank")String snederBank, @Param("receiverBank")String receiverBank);
}
