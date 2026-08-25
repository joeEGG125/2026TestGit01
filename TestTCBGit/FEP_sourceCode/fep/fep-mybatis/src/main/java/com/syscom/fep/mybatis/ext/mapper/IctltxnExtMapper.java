package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.model.Inbkpend;
import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.IctltxnMapper;
import com.syscom.fep.mybatis.model.Ictltxn;
import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface IctltxnExtMapper extends IctltxnMapper {

	/**
	 * 2021-04-21 ZhaoKai add
	 */
	Ictltxn queryByOriData(@Param("ictltxnTxDate") String ictltxnTxDate, @Param("ictltxnStan") String ictltxnStan);

	/**
	 * 2021-08-20 ChenYu add
	 */
	Ictltxn getIctltxn(Ictltxn ictltxn);

}