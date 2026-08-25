package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import com.syscom.fep.mybatis.mapper.AlarmMapper;
import com.syscom.fep.mybatis.model.Alarm;
import jakarta.annotation.Resource;

@Resource
public interface AlarmExtMapper extends AlarmMapper {
	/**
	 * Ben add 
	 */
	List<Alarm> getAlarmByPKLike(String alarmNo);
}