package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.EjfnoMapper;

import jakarta.annotation.Resource;

@Resource
public interface EjfnoExtMapper extends EjfnoMapper {

    public int getEjfnoSequence();

}
