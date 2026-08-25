package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.WebauditdescMapper;
import com.syscom.fep.mybatis.model.Webauditdesc;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface WebauditdescExtMapper extends WebauditdescMapper {

    List<Webauditdesc> selectByProgramid(@Param("programid") String programid);

}
