package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.FundlogMapper;
import com.syscom.fep.mybatis.model.Fundlog;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface FundlogExtMapper extends FundlogMapper {

    /**
     * zk add
     */
    Fundlog getFUNDLOGByFGSeqno(@Param("fundlogFgSeqno") String fundlogFgSeqno);

    /**
     * xy add
     * UI019201 引用
     * @param fundlogTxDate UI日期
     * @return HashMap<String,Object> COUNT SUM(FUNDLOG_FG_AMT)
     */
    List<HashMap<String,Object>> getFUNDLOGSumAmtBytxDate(@Param("fundlogTxDate") String fundlogTxDate);
}