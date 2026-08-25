package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.UcdidMapper;
import com.syscom.fep.mybatis.model.Ucdid;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Resource
public interface UcdidExtMapper extends UcdidMapper {

    //int updateByPrimaryKeySelective(Ucdid record);
    /**
     * 2025-09-02 add
     *
     */
    List<Ucdid> queryByIdnoAndHealthId(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid, @Param("nocondition") String nocondition);
    int updateHistory(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid);
    Ucdid selectByIdno(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid);
    Ucdid selectByIdnoAndLFStan(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid, @Param("feptxntcb2566Stan") String feptxntcb2566Stan);
    Ucdid selectTopByIdno(@Param("ucdidIdno") String ucdidIdno, @Param("ucdidHealthid") String ucdidHealthid);

    Map<String, Object> getUcdidDataBySeleted(
            @Param("idno") String idno,
            @Param("healthId") String healthId,
            @Param("txCode") String txCode,
            @Param("txDate") String txDate,
            @Param("stan") String stan
    );

//    Map<String, Object> getUcdidDataByIdno(
//            @Param("idno") String idno,
//            @Param("healthId") String healthId
//    );
    List<Map<String, Object>> getUcdidDataByIdno(@Param("argsMap") Map<String, Object> argsMap);

    //20251002 add 查詢前一日待處理的普發資料
    List<Ucdid> selectForStatusUpdate(@Param("wkDate") String wkDate);
}
