package com.syscom.fep.mybatis.his.ext.mapper;

import com.syscom.fep.mybatis.his.mapper.UcdidMapper;
import com.syscom.fep.mybatis.his.model.Ucdid;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


@Resource
public interface HisUcdidExtMapper extends UcdidMapper {

    int updateByPrimaryKeySelective(Ucdid record);
    /**
     * 2025-09-02 add
     *
     */
    List<Ucdid> queryByIdnoAndHealthId(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid, @Param("ucdidUcdrChannel") String ucdidUcdrChannel);
    int updateHistory(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid);
    Ucdid selectByIdno(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid);

    /**
     * 普發同步UI
     */
    Map<String, Object> getUcdidDataBySeleted(
            @Param("idno") String idno,
            @Param("healthId") String healthId,
            @Param("txCode") String txCode,
            @Param("txDate") String txDate,
            @Param("stan") String stan
    );

    //20251002 add 查詢前一日待處理的普發資料
    List<Ucdid> selectForStatusUpdate(@Param("wkDate") String wkDate);
}
