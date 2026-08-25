package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.BctlMapper;
import com.syscom.fep.mybatis.model.Bctl;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface BctlExtMapper extends BctlMapper {

    List<Bctl> getBCTLAlias(String orderBy);
    
    String  getBRAlias(String bctlBrno);
}
