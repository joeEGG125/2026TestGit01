package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmouteMapper;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoute;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface RmouteExtMapper extends RmouteMapper {
    List<Rmoute> getDataTableByPrimaryKey(Rmoute rmoute);
}
