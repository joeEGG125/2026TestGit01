package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NpsdtlMapper;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.mybatis.model.Odrc;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface NpsdtlExtMapper extends NpsdtlMapper {
    /**
     *返回詳單
     * @param batno
     * @return
     */
    List<HashMap<String,Object>> showDetail(@Param("batno")String batno);
    int deleteByBATNO(@Param("batno")String batno);

    List<Npsdtl> GetNPSDTLByBATNO(@Param("batno")String batno,@Param("seqNo")Integer seqNo);
    List<Npsdtl> GetNPSDTLByBATNOResult(@Param("batno")String batno,@Param("seqNo")Integer seqNo);
    List<Npsdtl> GetNPSDTLByBATNOforAll(@Param("batno")String batno);
    List<Npsdtl> GetNPSDTLByBATNOforAllforBatchJob(@Param("batno")String batno, @Param("wStop")int wStop, @Param("npsbatchResult")String npsbatchResult);
    String getNPSDTLByBATNOforCountBy02(@Param("batno")String batno,@Param("result")String result);

    Map<String, Object> getNPSDTLTotalCNTAMT(Npsdtl record);

}