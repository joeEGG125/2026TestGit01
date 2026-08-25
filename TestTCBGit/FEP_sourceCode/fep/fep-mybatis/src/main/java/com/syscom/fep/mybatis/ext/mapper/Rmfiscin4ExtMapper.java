package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.Rmfiscin4Mapper;
import com.syscom.fep.mybatis.model.Rmfiscin4;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface Rmfiscin4ExtMapper extends Rmfiscin4Mapper {
    List<HashMap<String, Object>> queryByPrimaryKey(Rmfiscin4 record);

    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMFISCIN4forBatchDaily();
}
