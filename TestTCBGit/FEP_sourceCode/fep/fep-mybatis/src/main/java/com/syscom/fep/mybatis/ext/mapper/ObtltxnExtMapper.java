package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.mapper.ObtltxnMapper;
import com.syscom.fep.mybatis.model.Obtltxn;
import jakarta.annotation.Resource;

@Resource
public interface ObtltxnExtMapper extends ObtltxnMapper {

	List<Obtltxn> getQrptxnByIcSeqno(
			@Param("obtltxnTxDateFisc") String obtltxnTxDateFisc,
			@Param("obtltxnAtmType") String obtltxnAtmType,
			@Param("obtltxnTroutActno") String obtltxnTroutActno,
			@Param("obtltxnIcSeqno") String obtltxnIcSeqno,
			@Param("obtltxnOrderNo") String obtltxnOrderNo);

	/*
	 * 2021-08-23 ZhaoKai add
	 */
	Obtltxn queryOBTLXNByStan(@Param("obtltxnTbsdyFisc") String obtltxnTbsdyFisc,
			@Param("obtltxnBkno") String obtltxnBkno,
			@Param("obtltxnStan") String obtltxnStan);

	/**
	 * 2021-10-11 Richard add for OBConfirmI
	 * 
	 * @param obtltxn
	 * @return
	 */
	int updateByStan(Obtltxn obtltxn);
	
	/**
	 * 
	 * @param obtltxnTxDate
	 * @param obtltxnEjfno
	 * @return Obtltxn
	 */
	Obtltxn getOBTLTXNbyPK(@Param("obtltxnTxDate")String obtltxnTxDate, @Param("obtltxnEjfno")Long obtltxnEjfno);
	
	/**
	 * 2022-05/31 Han Add
	 * 
	 * @param txtTroutBkno
	 * @param txtTroutActno
	 * @param txtTxAMT
	 * @param txtOrderNO
	 * @param txtMerchantId
	 * @param txTransactDate
	 * @param txTransactDateE
	 * @param txtBkno
	 * @param txtStan
	 * @return
	 */
	List<Obtltxn> getObtlTxn(@Param("txtTroutBkno") String txtTroutBkno,
			@Param("txtTroutActno") String txtTroutActno, @Param("txtTxAMT") String txtTxAMT,
			@Param("txtOrderNO") String txtOrderNO, @Param("txtMerchantId") String txtMerchantId,
			@Param("txTransactDate") String txTransactDate, @Param("txTransactDateE") String txTransactDateE,
			@Param("txtBkno") String txtBkno, @Param("txtStan") String txtStan);

	
}