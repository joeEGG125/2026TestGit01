package com.syscom.fep.mybatis.ext.mapper;
import com.syscom.fep.mybatis.mapper.BinMapper;
import com.syscom.fep.mybatis.model.Bin;

import java.util.List;

import jakarta.annotation.Resource;

import com.syscom.fep.mybatis.model.Inbkparm;
import org.apache.ibatis.annotations.Param;

@Resource
public interface BinExtMapper extends BinMapper {

    /**
     * add by wj 20210911
     */
    List<Bin> getBinByDef(@Param("binBkno") String binBkno,
			@Param("binZone") String binZone);
    List<Bin> queryBinAll();
    List<Bin> getBinByPrimaryKey(@Param("binNo") String binNo,
                                 @Param("binBkno") String binBkno);

    int updateByPrimaryKey(Bin record);

}