package com.syscom.fep.mybatis.enc.ext.mapper;

import com.syscom.fep.mybatis.enc.mapper.WhitelistMapper;
import com.syscom.fep.mybatis.enc.model.Whitelist;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface WhitelistExtMapper extends WhitelistMapper {

    List<Whitelist> selectAll();

}