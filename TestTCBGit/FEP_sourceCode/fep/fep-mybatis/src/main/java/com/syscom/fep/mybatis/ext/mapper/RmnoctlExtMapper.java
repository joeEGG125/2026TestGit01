package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmnoctlMapper;

import jakarta.annotation.Resource;

@Resource
public interface RmnoctlExtMapper extends RmnoctlMapper {

    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMNOCTLforBatchDaily();

}
