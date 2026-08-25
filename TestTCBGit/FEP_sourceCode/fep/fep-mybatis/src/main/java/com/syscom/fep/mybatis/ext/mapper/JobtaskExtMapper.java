package com.syscom.fep.mybatis.ext.mapper;

import java.util.HashMap;
import java.util.List;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.JobtaskMapper;
import com.syscom.fep.mybatis.model.Jobtask;

@Resource
public interface JobtaskExtMapper extends JobtaskMapper {
	/*
	 * zk 2022-01-14
	 */
	List<HashMap<String, Object>> getJobTaskByBatchId(Integer batchid);

	/*
	 * zk 2022-01-14
	 */
	int deleteByBatchId(Integer batchBatchid);

	/*
	 * zk 2022-01-14
	 */
	int updateTaskIdByJobId(@Param("jobId") Integer jobId, @Param("taskId") Integer taskId);

	/*
	 * zk 2022-01-14
	 */
	Jobtask getMaxJobTasktByJobId(Integer jobId);

	/*
	 * zk 2022-01-17
	 */
	int deleteByJobId(Integer jobId);
}