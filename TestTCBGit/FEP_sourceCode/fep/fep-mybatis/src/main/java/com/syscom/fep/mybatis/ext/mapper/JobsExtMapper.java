package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.JobsMapper;
import com.syscom.fep.mybatis.model.Jobs;

import jakarta.annotation.Resource;
import java.util.List;

@Resource
public interface JobsExtMapper extends JobsMapper {

	/*
	 * zk 2022-01-14
	 */
	int deleteByBatchId(Integer jobsBatchid);

	/*
	 * zk 2022-01-14
	 */
	int getJobsCountByBatchId(Integer jobsBatchid);

	/*
	 * zk 2022-01-17
	 */
	List<Jobs> getDataTableByPrimaryKey(Integer jobsJobid);

	/*
	 * zk 2022-01-17
	 */
	List<Jobs> getJobsByBatchId(Integer jobsBatchid);

}