package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ApibatchMapper;
import com.syscom.fep.mybatis.model.Apibatch;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface ApibatchExtMapper extends ApibatchMapper {

    /**
     *  zk add
     */
    List<Apibatch> queryApibatch(@Param("beginDate")String beginDate, @Param("endDate")String endDate);
}