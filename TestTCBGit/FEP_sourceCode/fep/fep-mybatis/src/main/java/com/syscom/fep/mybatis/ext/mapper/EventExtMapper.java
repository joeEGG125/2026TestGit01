package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;
import jakarta.annotation.Resource;
import com.syscom.fep.mybatis.mapper.EventMapper;
import com.syscom.fep.mybatis.model.Event;

@Resource
public interface EventExtMapper extends EventMapper {
	/**
	 * Ben add 
	 */
	List<Event> CheckEVENTForAlarmDelete(String alarmNo);
}