package com.syscom.fep.mybatis.ext.mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.WkpostdtlMapper;

/**
 * @author xingyun_yang
 * @create 2021/9/17
 */
@Resource
public interface WkpostdtlExtMapper extends WkpostdtlMapper {

	/**
	 * xy add
	 * UI019401 引用 根據 table 的值可分別查三個表
	 * 
	 * @param txDate
	 * @param sysCode
	 * @param acBranchCode
	 * @param drCrSide
	 * @param acCode
	 * @param subAcCode
	 * @param dtlAcCode
	 * @param deptCode
	 * @param txAmt
	 * @param table WKPOSTDTL or HKWKPOSTDTL or MOWKPOSTDTL
	 * @return
	 */
	List<HashMap<String, Object>> getWkPostDtl(
			@Param("txDate") Date txDate,
			@Param("sysCode") String sysCode,
			@Param("acBranchCode") String acBranchCode,
			@Param("drCrSide") String drCrSide,
			@Param("acCode") String acCode,
			@Param("subAcCode") String subAcCode,
			@Param("dtlAcCode") String dtlAcCode,
			@Param("deptCode") String deptCode,
			@Param("txAmt") String txAmt,
			@Param("table") String table);
}
