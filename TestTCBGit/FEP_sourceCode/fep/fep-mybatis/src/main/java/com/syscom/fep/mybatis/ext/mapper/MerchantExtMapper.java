package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.MerchantMapper;
import org.apache.ibatis.annotations.Param;
import jakarta.annotation.Resource;
import com.syscom.fep.mybatis.model.Merchant;

import java.util.List;

@Resource
public interface MerchantExtMapper extends MerchantMapper {

	void deleteMERCHANT();
	int batchInsertMERCHANT(List<Merchant> list);
}