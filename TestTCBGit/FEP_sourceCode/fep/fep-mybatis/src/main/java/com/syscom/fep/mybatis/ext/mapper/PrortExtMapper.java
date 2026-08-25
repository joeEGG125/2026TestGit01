package com.syscom.fep.mybatis.ext.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.PrortMapper;

@Resource
public interface PrortExtMapper extends PrortMapper{

	/**
	 * Bruce add 群組與功能查詢 已授權程式
	 * @param fepgroupGroupId
	 * @return
	 */                                  
	public List<Map<String, Object>> getPrortByGroupIdLike(@Param("fepgroupGroupId") String fepgroupGroupId);
}
