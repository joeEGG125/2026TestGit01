package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.SysstatMapper;
import com.syscom.fep.mybatis.model.Sysstat;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Resource
public interface SysstatExtMapper extends SysstatMapper {
	/**
	 * 2021-04-20 Richard add
	 *
	 * @return
	 */
	List<Sysstat> selectAll();

	/**
	 * Han add 2022-07-22
	 * @return
	 */
	List<Map<String, Object>> getQueryAll();
	/**
	 * Zonghao add 2025-06-03
	 * @return
	 */
	String getSysstatTbsdy();
	String getSysstatLbsdy();

	Sysstat selectFirstByLbsdyFisc();

	int updateByHbkno(
			@Param("SYSSTAT_HBKNO")String SYSSTAT_HBKNO,
			@Param("SYSSTAT_AOCT_1000")String SYSSTAT_AOCT_1000,
			@Param("SYSSTAT_AOCT_1100")String SYSSTAT_AOCT_1100,
			@Param("SYSSTAT_AOCT_1200")String SYSSTAT_AOCT_1200,
			@Param("SYSSTAT_AOCT_1300")String SYSSTAT_AOCT_1300,
			@Param("SYSSTAT_AOCT_1400")String SYSSTAT_AOCT_1400,
			@Param("SYSSTAT_MBACT_1000")String SYSSTAT_MBACT_1000,
			@Param("SYSSTAT_MBACT_1100")String SYSSTAT_MBACT_1100,
			@Param("SYSSTAT_MBACT_1200")String SYSSTAT_MBACT_1200,
			@Param("SYSSTAT_MBACT_1300")String SYSSTAT_MBACT_1300,
			@Param("SYSSTAT_MBACT_1400")String SYSSTAT_MBACT_1400,
			@Param("SYSSTAT_MBACT_2000")String SYSSTAT_MBACT_2000,
			@Param("SYSSTAT_MBACT_2200")String SYSSTAT_MBACT_2200,
			@Param("SYSSTAT_MBACT_2500")String SYSSTAT_MBACT_2500,
			@Param("SYSSTAT_MBACT_2510")String SYSSTAT_MBACT_2510,
			@Param("SYSSTAT_MBACT_2520")String SYSSTAT_MBACT_2520,
			@Param("SYSSTAT_MBACT_2530")String SYSSTAT_MBACT_2530,
			@Param("SYSSTAT_MBACT_2540")String SYSSTAT_MBACT_2540,
			@Param("SYSSTAT_MBACT_2550")String SYSSTAT_MBACT_2550,
			@Param("SYSSTAT_MBACT_2560")String SYSSTAT_MBACT_2560,
			@Param("SYSSTAT_MBACT_2570")String SYSSTAT_MBACT_2570,
			@Param("SYSSTAT_MBACT_2700")String SYSSTAT_MBACT_2700,
			@Param("SYSSTAT_MBACT_7100")String SYSSTAT_MBACT_7100,
			@Param("SYSSTAT_MBACT_7300")String SYSSTAT_MBACT_7300
			);
	int updateByHbknoForSystemStatus(
			@Param("SYSSTAT_HBKNO")String SYSSTAT_HBKNO,
			@Param("SYSSTAT_CBS")Short SYSSTAT_CBS,
			@Param("SYSSTAT_FCS")Short SYSSTAT_FCS,
			@Param("SYSSTAT_CREDIT")Short SYSSTAT_CREDIT,
			@Param("SYSSTAT_MTP")Short SYSSTAT_MTP,
			@Param("SYSSTAT_TWMP")Short SYSSTAT_TWMP,
			@Param("SYSSTAT_FIDO")Short SYSSTAT_FIDO
			);
	int updateSyssTatLTNbsdyFisc(
			@Param("SYSSTAT_HBKNO")String SYSSTAT_HBKNO,
			@Param("SYSSTAT_LBSDY_FISC")String SYSSTAT_LBSDY_FISC,
			@Param("SYSSTAT_TBSDY_FISC")String SYSSTAT_TBSDY_FISC,
			@Param("SYSSTAT_NBSDY_FISC")String SYSSTAT_NBSDY_FISC
			);
	int updateKeySyncForChangeKey(Sysstat record);
}