package com.syscom.fep.mybatis.ext.mapper;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.BarcodeMapper;
import com.syscom.fep.mybatis.model.Barcode;
import jakarta.annotation.Resource;

@Resource
public interface BarcodeExtMapper extends BarcodeMapper {

	/**
	 * add by wj 20210525
	 * 
	 * @param barcodeActId
	 * @return
	 */
	Barcode queryByActId(@Param("barcodeActId") Integer barcodeActId);

}