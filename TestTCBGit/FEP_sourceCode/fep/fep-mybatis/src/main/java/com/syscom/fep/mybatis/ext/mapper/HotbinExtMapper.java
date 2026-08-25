package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.HotbinMapper;
import com.syscom.fep.mybatis.model.Hotbin;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

@Resource
public interface HotbinExtMapper extends HotbinMapper {

	 /**
     * Han add 2022/06/17
     * @param txtBinNo
     * @param txtBinOrg
     * @return 
     */
	List<Hotbin> queryHOTBINByPKLike(@Param("txtBinNo")String txtBinNo, @Param("txtBinOrg")String txtBinOrg);

	void deleteHOTBIN(@Param("binNo")String binNo);
	

}
