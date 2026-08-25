package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmpostMapper;
import com.syscom.fep.mybatis.model.Rmpost;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface RmpostExtMapper extends RmpostMapper {

    List<Rmpost> getRMPOSTByPKBeforeTbsdy(Rmpost rmpost);
}
