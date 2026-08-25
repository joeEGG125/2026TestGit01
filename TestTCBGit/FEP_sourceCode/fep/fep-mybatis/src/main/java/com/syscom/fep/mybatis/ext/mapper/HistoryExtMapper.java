package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import com.syscom.fep.mybatis.model.History;
import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.HistoryMapper;

@Resource
public interface HistoryExtMapper extends HistoryMapper {

	/**
	 * xy add by 2022-1-25
	 */
	List<Map<String, Object>> getHistoryById(@Param("batchId") Integer batchId, @Param("instanceId") String instanceId);
	List<Map<String, Object>> getHistoryQuery(@Param("batchName") String batchName, @Param("batchStartDate") String batchStartDate, @Param("batchShortName") String batchShortName, @Param("subsys") String subsys);

	/**
	 * 2022-02-21 Richard add
	 * 
	 * @param historyLogfile
	 * @return
	 */
	Map<String, Object> getLogByLogFile(@Param("historyLogfile") String historyLogfile);
}
