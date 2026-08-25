package com.syscom.fep.mybatis.ext.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.syscom.fep.mybatis.mapper.CompanyMapper;
import com.syscom.fep.mybatis.model.Company;
import jakarta.annotation.Resource;

@Resource
public interface CompanyExtMapper extends CompanyMapper {
	/**
	 * 2021-05-08 ZhaoKai add
	 * 
	 * @param recid
	 * @return
	 */
	List<Company> getCompanyByBranch(@Param("recid") String recid);
}