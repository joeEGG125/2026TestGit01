package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import jakarta.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.VendorMapper;
import com.syscom.fep.mybatis.model.Vendor;

@Resource
public interface VendorExtMapper extends VendorMapper{

	/**
	 * 廠牌下拉選單
	 * @return
	 */
	public List<Vendor> queryAllData();
	
	public Vendor selectByNo(@Param("venderName") String venderName);
}
