package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Resource
public interface NpsunitExtMapper extends NpsunitMapper {

    /**
     * zk add 2021-08-12
     */
    ArrayList<Npsunit> queryNPSUNITbyUNITNO(@Param("npsunitNo") String npsunitNo);

    /**
     * Han 2022-07-21
     */
	void truncateNPSUNIT();

    void deleteNPSUNIT();
	/**
     * Han 2022-07-21
     */
	List<Map<String,Object>> queryAllData(@Param("orderBy")String orderBy);

    /**
     * 將生效資料更新全國性繳費委託單位檔 批次寫入
     * @param npsunit
     * @return
     */
    public int insertByNpschk(@Param("npsunit") Npsunit npsunit);

    int batchInsertNPSUNIT(List<Npsunit> list);
    @Override
    Npsunit selectByNoAndPayType(@Param("npsunitNo") String npsunitNo, @Param("npsunitPaytype") String npsunitPaytype);


}