package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.Rmfiscout4Mapper;
import com.syscom.fep.mybatis.model.Rmfiscout4;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface Rmfiscout4ExtMapper extends Rmfiscout4Mapper {

    List<HashMap<String, Object>> queryByPrimaryKey(Rmfiscout4 record);


    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMFISCOUT4forBatchDaily();
}
