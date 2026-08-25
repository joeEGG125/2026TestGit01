package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NpsbatchMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Resource
public interface NpsbatchExtMapper extends NpsbatchMapper {

    /**
     * xy 2021/8/26 add
     * @param fileid
     * @param txdate
     * @return
     */
    List<Npsbatch> queryNPSBATCH(@Param("fileid")String fileid, @Param("txdate")String txdate);
    List<Npsbatch> queryNPSBATCHbyBatchNo(@Param("txdate")String txdate, @Param("BatchNo")String BatchNo);

    Npsbatch queryNPSBATCHWithOne(@Param("fileid")String fileid, @Param("txdate")String txdate, @Param("BatchNo")String BatchNo);

    List<Map<String, Object>> selectNpsbatchWithNpsdtl(
            @Param("filename") String filename,
            @Param("txDate") String txDate,
            @Param("batchNo") String batchNo);

    Npsbatch selectNpsbatchWithNpsdtlbyVip(
            @Param("filename") String filename,
            @Param("txDate") String txDate,
            @Param("batchNo") String batchNo,
            @Param("branch") String branch);
    //2026/5/11 LeYun add NPAYResponseToChannel
    List<Npsbatch> getNpsbatchForResponse(
            @Param("W_FILENAME")String W_FILENAME,
            @Param("W_DATE")String W_DATE,
            @Param("W_BATCHNO")String W_BATCHNO);

}
