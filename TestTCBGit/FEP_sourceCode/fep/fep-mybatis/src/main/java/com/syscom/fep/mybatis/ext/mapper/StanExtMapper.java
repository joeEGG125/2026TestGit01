package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.StanMapper;

import jakarta.annotation.Resource;

@Resource
public interface StanExtMapper extends StanMapper {

    public int getStanSequence();

    public void resetStanSequence();

    public void resetStanToNext();

}
