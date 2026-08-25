package com.syscom.fep.mybatis.deslog.ext.mapper;

import com.syscom.fep.mybatis.deslog.mapper.DeslogMapper;
import com.syscom.fep.mybatis.deslog.model.Deslog;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface DeslogExtMapper extends DeslogMapper {
    int TruncateByLogDate(
            @Param("tableNameSuffix") String tableNameSuffix);
}