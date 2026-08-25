package com.syscom.fep.mybatis.ext.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Upbin;

@Resource
public interface UserDefineExtMapper {

	// 2022-11-23 Richard marked
	// EJ的獲取方式改為從Sequence中取, 所以這個方法mark掉
	// public void getEj(Map<String, Integer> args);

	// 2022-11-22 Richard marked
	// Stan的獲取方式改為從Sequence中取, 所以這個方法mark掉
	// public void getStan(Map<String, Integer> args);

	public void getPbmrByIdNo(Map<String, Object> args);

	public void queryIbtId(Map<String, Object> args);

	public void getBoxByIdNo(Map<String, Object> args);

	public void getAtmTxSeq(Map<String, Object> args);
	
	public void decrementRMFISCOUT4NO(Map<String, Object> args);

	public void getRMNO(Map<String, Object> args);

	public void getRMFISCOUT1NO(Map<String, Object> args);

	public void getRMOUTSNONO(Map<String, Object> args);

	public void getRMFISCOUT4NO(Map<String, Object> args);

	public void getFCRMNO(Map<String, Object> args);

	public void getFCRMOUTSNO(Map<String, Object> args);
	
	public List<Map<String, Object>> queryAllMembers(Map<String, Object> args);

	public List<Map<String, Object>> queryAllAuditData(Map<String, Object> args);
	
	List<HashMap<String, Object>> queryWebAudit(Map<String, Object> args);
	
	List<HashMap<String, Object>> queryDirectReports(Map<String, Object> args);
	
}