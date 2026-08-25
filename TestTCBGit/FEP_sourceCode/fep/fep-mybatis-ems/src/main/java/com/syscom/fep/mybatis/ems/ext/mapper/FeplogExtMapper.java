package com.syscom.fep.mybatis.ems.ext.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.ems.mapper.FeplogMapper;
import com.syscom.fep.mybatis.ems.model.Feplog;

@Resource
public interface FeplogExtMapper extends FeplogMapper {
	/**
	 * 2021-08-29 Richard add for FEPLOG查詢
	 *
	 * @param tableNameSuffix
	 * @param feplog
	 * @param logDateBegin
	 * @param logDateEnd
	 * @param ejfnoList
	 */
	List<Feplog> getMultiFepLogByDef(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feplog") Feplog feplog,
			@Param("logDateBegin") Date logDateBegin,
			@Param("logDateEnd") Date logDateEnd,
			@Param("ejfnoList") List<Long> ejfnoList);
	List<Feplog> getMultiFepLogByDef_019050(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feplog") Feplog feplog
			);

	List<Feplog> getFeplogByDef(@Param("tableNameSuffix") String tableNameSuffix, @Param("feplog") Feplog feplog);

	public List<Feplog> getFepLog(@Param("argsMap") Map<String,Object> argsMap);

    int deleteByLogDate(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("logDateBegin") Date logDateBegin,
			@Param("logDateEnd") Date logDateEnd);

	int TruncateByLogDate(
			@Param("tableNameSuffix") String tableNameSuffix);
}