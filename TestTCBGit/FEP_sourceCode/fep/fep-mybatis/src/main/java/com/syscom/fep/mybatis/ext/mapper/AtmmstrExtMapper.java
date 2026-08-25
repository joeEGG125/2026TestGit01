package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import com.syscom.fep.mybatis.model.Atmmstr;
import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.AtmmstrMapper;

@Resource
public interface AtmmstrExtMapper extends AtmmstrMapper {

    /**
     * 2021-11-08 Richard add for ATM Gateway
     *
     * @param atmIp
     * @return
     */
    Map<String, Object> getAtmmstrByAtmIp(@Param("atmIp") String atmIp);

    /**
     * 2021-11-09 Richard add for ATM Gateway
     *
     * @param atmAtmno
     * @param atmAtmpIp
     * @param atmAtmpPort
     * @return
     */
    int updateAtmmstrByAtmNoSelective(Atmmstr record);

    int updateAtmpByAtmmon(@Param("atmBrnoSt") String atmBrnoSt, @Param("atmModelno") String atmModelno, @Param("atmVendor") String atmVendor, @Param("atmIp") String atmIp, @Param("atmno") String atmno, @Param("atmStartDate") String atmStartDate, @Param("atmCityC") String atmCityC, @Param("atmAddressC") String atmAddressC, @Param("atmLocation") String atmLocation);

    /**
     * 2022-01-26 Richard add for ATM Gateway
     *
     * @param atmAtmno
     * @return
     */
    Map<String, Object> getAtmmstrByAtmNo(@Param("atmAtmno") String atmAtmno);

    /**
     * Bruce add Atm基本資料查詢
     *
     * @param argsMap
     * @return
     */
    public List<Map<String, Object>> getAtmBasicList(@Param("argsMap") Map<String, Object> argsMap);

    /**
     * Bruce add Atmmstr憑證更新
     *
     * @param argsMap
     * @return
     */
    public int updateAtmmstrByAtmatmNo(@Param("argsMap") Map<String, Object> argsMap);

    /**
     * 2023-09-18 Bruce add 取得所有 gateway ip
     *
     * @return
     */
    public List<Map<String, String>> getAtmpIpList();

    /**
     * 2022-05-06 Han add for SingleATM
     *
     * @param atmAtmno
     * @return
     */
    List<Map<String, Object>> getSingleATM(@Param("atmAtmno") String atmAtmno);

    /**
     * 2025-06-19 Zonghao add for ATMAddress
     *
     * @param atmAtmno
     * @return
     */
    String getATMAddressCByAtmno(@Param("atmAtmno") String atmAtmno);

    /**
     * 2026-03-24 Richard add for ATM Gateway
     *
     * @param atmFepConnection
     * @return
     */
    List<Map<String, Object>> getAtmmstrByAtmFepConnection(@Param("atmFepConnection") Short atmFepConnection);
}