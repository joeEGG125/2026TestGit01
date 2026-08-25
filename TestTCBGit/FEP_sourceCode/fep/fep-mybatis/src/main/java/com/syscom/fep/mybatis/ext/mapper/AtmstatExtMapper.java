package com.syscom.fep.mybatis.ext.mapper;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.AtmstatMapper;
import com.syscom.fep.mybatis.model.Atmstat;

import java.util.List;
import java.util.Map;

@Resource
public interface AtmstatExtMapper extends AtmstatMapper {

    /**
     * 2021-11-11 Richard add for ATM Gateway
     *
     * @param atmstat
     * @param atmAtmpIp
     * @return
     */
    int updateAtmstatByAtmAtmpIp(@Param("atmstat") Atmstat atmstat, @Param("atmAtmpIp") String atmAtmpIp);

    /**
     * 2022-08-17 Han add for update ATMMON status
     *
     * @param atmNo
     * @param enable
     * @param serviceStatus
     * @param connectStatus
     * @return
     */
    int updateATMStatus(
            @Param("atmNo") String atmNo,
            @Param("enable") String enable,
            @Param("serviceStatus") String serviceStatus,
            @Param("connectStatus") String connectStatus);

    /**
     * 2023-02-10 Richard add for ATM Gateway
     * 2023-09-19 Bruce Modify 依照明祥需求改用map接
     *
     * @param atmstatAtmnoList
     * @param atmstatStatus
     * @return
     */
    //List<Atmstat> selectAtmstatList(@Param("atmstatAtmnoList") List<String> atmstatAtmnoList, @Param("atmstatStatus") Short atmstatStatus);
    List<Map<String,Object>> selectAtmstatList(@Param("atmstatAtmnoList") List<String> atmstatAtmnoList, @Param("atmstatStatus") Short atmstatStatus, @Param("atmAtmpIp") String atmAtmpIp);

    /**
     * 2026-03-30 zonghao add 取得所有Ap Version ip
     *
     * @return
     */
    public List<Map<String, String>> getAtmApVersion();

    int selectAtmstatListCount(@Param("atmstatAtmnoList") List<String> atmstatAtmnoList, @Param("atmstatStatus") Short atmstatStatus, @Param("atmAtmpIp") String atmAtmpIp);

    int updateAtmstatEnable(@Param("atmNo") String atmNo, @Param("enable") String enable);
}
