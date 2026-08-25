package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.ClrdtlMapper;
import com.syscom.fep.mybatis.model.Clrdtl;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface ClrdtlExtMapper extends ClrdtlMapper {

    /**
     * zk add
     */
    int insertSelectiveThread(Clrdtl record);

    /**
     * zk add
     */
    List<Clrdtl> selectByPrimaryKeyThread(@Param("clrdtlTxdate") String clrdtlTxdate, @Param("clrdtlApId") String clrdtlApId);

    /**
     * zk add
     */
    Clrdtl queryTopCLRDTL(@Param("clrdtlTxdate") String clrdtlTxdate, @Param("clrdtlApId") String clrdtlApId);

    /**
     * zk add
     */
    int updateByPrimaryKeySelectiveThread(Clrdtl record);

    List<Clrdtl> queryByPrimaryKey(Clrdtl record);
}