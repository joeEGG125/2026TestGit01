package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Sysconf;

@Resource
public interface SysconfExtMapper extends SysconfMapper {
	/**
	 * 2021/04/26
	 * ZhaoKai add
	 */
	List<Sysconf> queryAllData(@Param("orderBy") String orderBy);

	/**
	 * Han add 2022-06-13
	 *
	 * @param sysconfSubsysno
	 * @param sysconfName
	 * @return List<Map<String,Object>>
	 */
	Map<String, Object> getAllDataByPk2(@Param("sysconfSubsysno") int sysconfSubsysno,
			@Param("sysconfName") String sysconfName);


	/**
	 * Han add 2022/06/08
	 *
	 * @param sysconfSubsysno
	 * @return List<Map<String,String>>
	 */
	List<Map<String, String>> getDataBySubSystem(@Param("sysconfSubsysno") int parseInt);

	/**
	 * Han add 2022/06/08
	 *
	 * @param sysconfSubsysno
	 * @return List<Map<String,String>>
	 */
	List<Map<String, String>> getAllBySubSystemName(@Param("sysconfName") String sysconfName);

	/**
	 * Han add 2022/06/08
	 *
	 * @return List<Map<String,String>>
	 */
	List<Map<String, String>> getAllData();

	/**
	 * Han add 2022/06/08
	 *
	 * @return List<Map<String,String>>
	 */
	List<Map<String, String>> getAllDataByPk(@Param("sysconfSubsysno") int sysconfSubsysno,
			@Param("sysconfName") String sysconfName);


	/**
	 * Han add 2022-06-16
	 *
	 * @param sysconfSubsysno
	 * @param sysconfName
	 * @param sysconfValue
	 * @param sysconfRemark
	 * @param sysconfType
	 * @param sysconfSubsysnoC
	 * @param sysconfReadonly
	 * @param sysconfDatatype
	 * @param sysconfReadonly2
	 */
	void updateByPrimaryKey(@Param("sysconfSubsysno") int sysconfSubsysno, @Param("sysconfName") String sysconfName,
			@Param("sysconfValue") String sysconfValue, @Param("sysconfRemark") String sysconfRemark,
			@Param("sysconfType") String sysconfType, @Param("sysconfReadonly") int sysconfReadonly,
			@Param("sysconfDatatype") String sysconfDatatype);

}