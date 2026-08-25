package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.Rmfiscin1Mapper;
import com.syscom.fep.mybatis.model.Rmfiscin1;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface Rmfiscin1ExtMapper extends Rmfiscin1Mapper {
    List<HashMap<String, Object>> queryByPrimaryKey(Rmfiscin1 record);

    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMFISCIN1forBatchDaily();
}
