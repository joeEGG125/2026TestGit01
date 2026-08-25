package com.syscom.fep.mybatis.his.ext.mapper;

import com.syscom.fep.mybatis.his.mapper.Inbk2160Mapper;
import com.syscom.fep.mybatis.his.model.Inbk2160;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Resource
public interface HisInbk2160ExtMapper extends Inbk2160Mapper {
    List<Inbk2160> selectOne(@Param("inbk2160Bkno") String inbk2160Bkno,@Param("inbk2160TxDate") String inbk2160TxDate,@Param("inbk2160Ejfno") Integer inbk2160Ejfno);
    List<Map<String, Object>> getTodayFinishTradeData(
            @Param("inbk2160Bkno")String inbk2160Bkno,
            @Param("inbk2160Subsys")Short inbk2160Subsys,
            @Param("inbk2160PrcResult")String inbk2160PrcResult);
    Inbk2160 getINBK2160ByFeptxn(@Param("inbk2160OriTxDate") String inbk2160OriTxDate, @Param("inbk2160OriStan") String inbk2160OriStan);
}
