package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import com.syscom.fep.mybatis.mapper.ZoneMapper;
import com.syscom.fep.mybatis.model.Zone;
import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

@Resource
public interface ZoneExtMapper extends ZoneMapper {

	List<Zone> selectAll();

	List<Zone> getDataByZone(String zoneCode);
	
    Zone getDataByZonee(@Param("zoneCode") String zone);
	
	Zone getDataTableByPrimaryKey(Zone record);
}