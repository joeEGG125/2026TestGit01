package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.BsdaysMapper;
import com.syscom.fep.mybatis.model.Bsdays;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Resource
public interface BsdaysExtMapper extends BsdaysMapper {

	/**
	 * bruce add 取得BsdaysDate
	 * @param transactDate
	 * @return
	 */
	public String getBsdaysDate(@Param("transactDate") String transactDate);
	
	/**
	 * Bruce add 
	 * @param zoneCode
	 * @param argsMap
	 * @param transactDate1
	 * @return
	 */
	public void getNextBusinessDate(Map<String,Object> argsMap);
	public List<Map<String, Object>> getBSDAYSByYearAndZoneAndDate(@Param("bsdays_ZONE_CODEDdl")String bsdays_ZONE_CODEDdl, @Param("txtBSDAYS_DATE")String txtBSDAYS_DATE);
	public List<Map<String, Object>> getBSDAYSByYearAndZone(@Param("BSDAYS_TARGET_YEAR")String BSDAYS_TARGET_YEAR, @Param("BSDAYS_TARGET_NEXT_YEAR")String BSDAYS_TARGET_NEXT_YEAR, @Param("BSDAYS_ZONE_CODE")String BSDAYS_ZONE_CODE);
	public void updateByPrimaryKey2(@Param("updateUserid")Long userId,
								   @Param("bsdays_ZONE_CODEDdl")String bsdays_ZONE_CODEDdl,
								   @Param("txtBSDAYS_DATE")String txtBSDAYS_DATE,
								   @Param("bsdays_WORKDAYDdl")String bsdays_WORKDAYDdl,
								   @Param("txtBSDAYS_JDAY")String txtBSDAYS_JDAY,
								   @Param("txtBSDAYS_NBSDY")String txtBSDAYS_NBSDY,
								   @Param("txtBSDAYS_WEEKNO")String txtBSDAYS_WEEKNO,
								   @Param("txtBSDAYS_ST_FLAG")String txtBSDAYS_ST_FLAG,
								   @Param("txtBSDAYS_ST_DATE_ATM")String txtBSDAYS_ST_DATE_ATM,
								   @Param("txtBSDAYS_ST_DATE_RM")String txtBSDAYS_ST_DATE_RM);
	public Bsdays getLBsdays(@Param("txtBSDAYS_NBSDY") String txtBSDAYS_NBSDY);
//	public List<Map<String, Object>> queryByPrimaryKey(@Param("bsdaysDate")String bsdaysDate,@Param("bsdaysZoneCode")String bsdaysZoneCode);
	public List<Bsdays> queryBSDAYSOfYearAndZone(@Param("BSDAYS_TARGET_YEAR")String BSDAYS_TARGET_YEAR, @Param("BSDAYS_ZONE_CODE")String BSDAYS_ZONE_CODE);
	public List<Bsdays> getLBsdaysBefore(@Param("txtLbsdy") String txtLbsdy);
	// 2026/6/17 2700新增
	public List<Bsdays> getBsdaysByDateOrNbsdy(@Param("queryDate") String queryDate);
}
