package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.TwslogMapper;
import com.syscom.fep.mybatis.model.Twslog;

@Resource
public interface TwslogExtMapper extends TwslogMapper {
	List<Map<String, Object>> getTwslogQuery(@Param("twsTaskname") String twsTaskName, @Param("batchStartDate") String batchStartDate);

	int insertTwslog(Twslog twslog);
}
