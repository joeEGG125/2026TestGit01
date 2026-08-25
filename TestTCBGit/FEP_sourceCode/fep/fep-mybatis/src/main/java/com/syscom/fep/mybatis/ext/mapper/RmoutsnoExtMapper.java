package com.syscom.fep.mybatis.ext.mapper;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.model.Rmoutsno;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RmoutsnoExtMapper extends RmoutsnoMapper {


    /**
     * zk zdd
     *
     *
     */
    int updateRMOUTSNObyFISCResNO(Rmoutsno record);

    /**
     * xy add
     */
    List<Rmoutsno> getRMOUTSNOByNONotEqualREPNO(@Param("rmoutTxDate") String rmoutTxDate);

    /**
     * zk zdd
     *
     *
     */
    List<Rmoutsno> getRMOUTSNOByDef(Rmoutsno record);

    /**
     * cy add
     *
     */
     List<HashMap<String,Object>> getRMOUTSNOByPK(@Param("rmoutsnoSenderBank")String rmoutsnoSenderBank, @Param("rmoutsnoReceiverBank")String rmoutsnoReceiverBank, @Param("isSpecificRcvBank")Boolean isSpecificRcvBank);

    /**
     * xy add BtBatchDaily 調用
     */
    int updateRMOUTSNOforBatchDaily();
}