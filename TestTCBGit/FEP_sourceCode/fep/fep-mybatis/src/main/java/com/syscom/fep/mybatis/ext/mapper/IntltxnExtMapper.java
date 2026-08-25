package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.IntltxnMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import jakarta.annotation.Resource;

@Resource
public interface IntltxnExtMapper extends IntltxnMapper {
	/**
	 * 2021-04-19
	 * ZhaoKai add
	 */
	Intltxn queryByOriData(Intltxn record);

	/**
	 * 2021-08-04 Richard add
	 * 
	 * @param record
	 * @return
	 */
	Intltxn queryByOriDataEmv(Intltxn record);

	/**
	 * 2021-05-06
	 * ZhaoKai add
	 */
	List<Intltxn> selectForCheckOwdCount(
			@Param("intltxnTxDate") String intltxnTxDate,
			@Param("intltxnTxTime") String intltxnTxTime,
			@Param("intltxnTroutBkno") String intltxnTroutBkno,
			@Param("intltxnTroutActno") String intltxnTroutActno);
}