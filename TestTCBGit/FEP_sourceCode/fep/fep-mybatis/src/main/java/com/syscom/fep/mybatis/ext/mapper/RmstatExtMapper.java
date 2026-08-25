package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmstatMapper;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmstat;

import jakarta.annotation.Resource;

@Resource
public interface RmstatExtMapper extends RmstatMapper {

    public Rmstat queryByPrimaryKey(Rmstat rmstat);

    public int updateAmlDaily();


}
