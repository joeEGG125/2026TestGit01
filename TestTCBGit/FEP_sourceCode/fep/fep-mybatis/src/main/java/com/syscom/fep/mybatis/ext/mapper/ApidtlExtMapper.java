package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ApidtlMapper;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface ApidtlExtMapper extends ApidtlMapper {

    /**
     * zk add
     */
    List<HashMap<String,Object>> queryApidtl(@Param("apidtlTxDate") String apidtlTxDate,@Param("webType")String webType);

    /**
     * zk add
     */
    HashMap<String,Object> getApibatchTotFee(@Param("apidtlTxDate") String apidtlTxDate);
}