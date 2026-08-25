package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.InbkparmMapper;
import com.syscom.fep.mybatis.model.Inbkparm;

import java.util.List;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

@Resource
public interface InbkparmExtMapper extends InbkparmMapper {
	/**
	 * 2021-04-19 ZhaoKai add
	 * 2021-07-20 Richard modified
	 * 
	 * @param record
	 * @return
	 */
	Inbkparm selectByPrimaryKey(@Param("inbkparmApid") String inbkparmApid, @Param("inbkparmPcode") String inbkparmPcode, @Param("inbkparmAcqFlag") String inbkparmAcqFlag, @Param("inbkparmEffectDate") String inbkparmEffectDate, @Param("inbkparmCur") String inbkparmCur, @Param("inbkparmRangeFrom") String inbkparmRangeFrom);
	List<Inbkparm> queryInbkparmAll();

	List<Inbkparm> getINBKPARMByPK(Inbkparm record);
    int deleteByPrimaryKey(Inbkparm record);

    int updateByPrimaryKey(Inbkparm record);
	Inbkparm queryByPK(Inbkparm record);
}