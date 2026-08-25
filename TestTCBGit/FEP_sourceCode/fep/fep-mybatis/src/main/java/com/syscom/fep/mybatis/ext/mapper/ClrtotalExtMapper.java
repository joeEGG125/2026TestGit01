package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ClrtotalMapper;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;

@Resource
public interface ClrtotalExtMapper extends ClrtotalMapper {

    HashMap<String,Object> getCntAmt(@Param("date") String date);
}
