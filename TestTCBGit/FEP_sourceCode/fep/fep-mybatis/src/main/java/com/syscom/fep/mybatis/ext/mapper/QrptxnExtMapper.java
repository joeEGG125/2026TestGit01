package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.QrptxnMapper;
import com.syscom.fep.mybatis.model.Qrptxn;
import jakarta.annotation.Resource;

@Resource
public interface QrptxnExtMapper extends QrptxnMapper {
	/**
	 *
	 * 2021-04-23 ZhaoKai add
	 *
	 */
	List<Qrptxn> getQrptxnByIcSeqno(@Param("qrptxnTxDateFisc") String qrptxnTxDateFisc, @Param("qrptxnIcSeqno") String qrptxnIcSeqno);
}