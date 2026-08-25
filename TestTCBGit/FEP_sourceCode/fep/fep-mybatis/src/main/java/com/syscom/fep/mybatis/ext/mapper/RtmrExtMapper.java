package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RtmrMapper;
import com.syscom.fep.mybatis.model.Rtmr;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface RtmrExtMapper extends RtmrMapper {
    /*
    * zk add
    * */
    Rtmr getSingleRTMR(@Param("remdate")String remdate,@Param("fiscrmsno")String fiscrmsno,@Param("entseq")String entseq);
}
