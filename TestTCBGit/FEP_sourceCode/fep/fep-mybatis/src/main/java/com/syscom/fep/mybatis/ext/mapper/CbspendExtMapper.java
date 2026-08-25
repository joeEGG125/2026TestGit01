package com.syscom.fep.mybatis.ext.mapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CbspendMapper;
import com.syscom.fep.mybatis.model.Cbspend;

import jakarta.annotation.Resource;

@Resource
public interface CbspendExtMapper extends CbspendMapper {
	/**
	 * 2021/06/08
	 * Joseph add
	 */
	List<Map<String, Object>> queryAllData();
	List<Map<String, Object>> getCBSPENDByTXDATEAndZone(@Param("cbspendTxDate") String cbspendTxDate, @Param("cbspendSuccessFlag") Short cbspendSuccessFlag, @Param("cbspendSubsys") Short cbspendSubsys, @Param("cbspendZone") String cbspendZone, @Param("cbspendCbsTxCode") String cbspendCbsTxCode);
    BigDecimal getsumOfTxAMT(@Param("cbspendTxDate") String cbspendTxDate, @Param("cbspendSuccessFlag") Short cbspendSuccessFlag, @Param("cbspendSubsys") Short cbspendSubsys, @Param("cbspendZone") String cbspendZone, @Param("cbspendCbsTxCode") String cbspendCbsTxCode);
    int updateByPrimaryKey(Cbspend record);
	List<Map<String, Object>> QueryResendCNT(@Param("cbspendTxDate") String txdate, @Param("cbspendZone") String zone, @Param("cbspendTbsdy") String tbsdy, @Param("cbspendSubsys") String subsys);
    int UpdateResendCNT(@Param("cbspendTxDate") String txdate,@Param("cbspendEjfno") BigDecimal ejfno, @Param("cbspendZone") String zone, @Param("cbspendTbsdy") String tbsdy, @Param("cbspendSubsys") String subsys);

}