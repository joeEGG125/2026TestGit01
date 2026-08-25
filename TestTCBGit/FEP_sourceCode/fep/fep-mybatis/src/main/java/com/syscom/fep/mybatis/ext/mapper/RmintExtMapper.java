package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmintMapper;
import com.syscom.fep.mybatis.model.Rmint;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RmintExtMapper extends RmintMapper {

    public int updateByPrimaryKey(Rmint oRmint, boolean initialFieldStatus);

    /*
    * zk add
    * */
    List<Rmint> getArrayListForService11X2(Rmint rmint);

    /*
     * zk add
     * */
    List<HashMap<String,Object>> getRMINTByTxdateBrno(@Param("txdate") String txdate, @Param("brno") String brno);

    Integer updateByPrimaryKeyWithStat(@Param("rmint") Rmint rmint, @Param("stat")String stat);
}
