package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.TxtypeMapper;
import com.syscom.fep.mybatis.model.Txtype;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface TxtypeExtMapper extends TxtypeMapper {

	/**
	 * 取得第一層
	 * @return
	 */
	List<Map<String,String>> getLevel1TxType();

	/**
	 * 取得第二層by第一層的txType
	 * @param txType1
	 * @return
	 */
	List<Map<String,String>> getLevel2ByTxType1(@Param("txType1") String txType1);
	/**
	 * 取得第二層+第一層
	 * @return
	 */
	List<Map<String,String>> getLevel2();

	/**
	 * 取得Txtype全部資料
	 * @return
	 */
	List<Txtype> getAllData();
	
	/**
	 * 取得分pcode的資料
	 * @param argsMap
	 * @return
	 */
	List<HashMap<String, Object>> getTxtypeForPcode(@Param("argsMap") Map<String,Object> argsMap);
}