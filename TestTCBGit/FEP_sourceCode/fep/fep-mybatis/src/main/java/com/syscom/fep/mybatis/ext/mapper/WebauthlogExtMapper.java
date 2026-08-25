package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.WebauthlogMapper;
import com.syscom.fep.mybatis.model.Webauthlog;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface WebauthlogExtMapper extends WebauthlogMapper {

    int deleteByPk(Webauthlog record);

    Webauthlog selectByPk(@Param("appdate") String appdate, @Param("appid") String appid, @Param("compid") String compid, @Param("deptid") String deptid, @Param("empid") String empid, @Param("funid") String funid, @Param("granttype") String granttype, @Param("grantdesc") String grantdesc);

    int updateByPkSelective(Webauthlog record);

    int updateByPk(Webauthlog record);

}