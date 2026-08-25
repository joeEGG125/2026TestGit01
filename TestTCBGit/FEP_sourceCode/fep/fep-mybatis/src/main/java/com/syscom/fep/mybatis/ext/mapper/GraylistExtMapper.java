package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.GraylistMapper;
import com.syscom.fep.mybatis.model.Graylist;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

@Resource
public interface GraylistExtMapper extends GraylistMapper {
    void updateGraylist();
    int deleteByPreProcessNote();
    Boolean checkGraylist();
    @Override
    int insert(Graylist record);
    @Override
    int updateByPrimaryKeySelective(Graylist record);
    /**
     * 2025-06-06 add
     *
     * @param bankNo
     * @param actNo
     * @param TxnDate
     * @return
     */
    Graylist getGraylistByAct(@Param("bankNo") String bankNo, @Param("actNo") String actNo, @Param("TxnDate") String TxnDate);
}
