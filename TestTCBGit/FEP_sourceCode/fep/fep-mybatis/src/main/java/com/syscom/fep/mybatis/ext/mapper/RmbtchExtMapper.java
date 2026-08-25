package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmbtchMapper;
import com.syscom.fep.mybatis.model.Rmbtch;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RmbtchExtMapper extends RmbtchMapper {

    /**
     * add by xy 2021/12/20
     */
    List<HashMap<String,Object>> getRMBTCHByDef(Rmbtch rmbtch);
}
