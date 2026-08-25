package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RmouttMapper;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import org.apache.ibatis.annotations.Param;
import com.syscom.fep.mybatis.model.Rmoutt;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.math.BigDecimal;
import java.util.List;

@Resource
public interface RmouttExtMapper extends RmouttMapper {
    /**
     *  zk add
     *  2021-09-28
     */
    List<Rmoutt> getOverCLRFundLevel(@Param("txdate")String txdate,@Param("amount")String amount,@Param("bkno")String bkno);

    /**
     *  zk add
     *  2021-09-28
     */
    int updateByPrimaryKeySelectiveWithStat(@Param("rmoutt")Rmoutt rmoutt,@Param("stats")String[] stats);

    /**
     *  zk add
     *  2021-09-29
     */
    Rmoutt queryByPrimaryKeyWithUpdLock(@Param("rmouttTxdate") String rmouttTxdate, @Param("rmouttBrno") String rmouttBrno, @Param("rmouttOriginal") String rmouttOriginal, @Param("rmouttFepno") String rmouttFepno);

    Rmoutt queryByPrimaryKeyWithUpdLockWithTimeOut(@Param("rmouttTxdate") String rmouttTxdate, @Param("rmouttBrno") String rmouttBrno, @Param("rmouttOriginal") String rmouttOriginal, @Param("rmouttFepno") String rmouttFepno);
    /**
     *  zk add
     *  2021-09-29
     */
    Rmoutt getRMOUTTForService11X1(@Param("rmoutt")Rmoutt rmoutt,
                                   @Param("owprioritys")String[] owprioritys,
                                   @Param("fund1")BigDecimal fund1,
                                   @Param("fund2")BigDecimal fund2,
                                   @Param("bal")BigDecimal bal,
                                   @Param("OutwardCheckAmount")BigDecimal OutwardCheckAmount);


    /**
     *  zk add
     *  2021-10-08
     */
    Rmoutt getRMSNOForUpdateRMOUTSNO(@Param("rmouttTxdate")String rmouttTxdate,
                                     @Param("rmouttFiscRtnCode")String rmouttFiscRtnCode,
                                     @Param("allbankUnitBank")String allbankUnitBank);

    /**
     *  zk add
     *  2021-10-09
     */
    List<Rmoutt> getRMOUTTByDef(Rmoutt defRMOUTT);

    /**
     *  zk add
     *  2021-10-09
     */
    int updateRMOUTTByFISCNO(Rmoutt rmoutt);

    /**
     * xy add  查詢全行之匯出未解筆數金額
     * 2021-10-31
     */
    int getTotalCntByStat(@Param("date") String date, @Param("stat") String stat);

    /**
     *  zk add
     *  2021-11-29
     */
    List<Rmoutt> getRMOUTTbyTXAMT(@Param("txDate") String txDate, @Param("txAmt")  String txAmt);

    /**
     *  zk add
     *  2021-11-29
     */
    List<Rmoutt> getRMOUTTbyReceiverBANK(@Param("txDate") String txDate, @Param("receiverBANK")  String receiverBANK);

    /**
     *  zk add
     *  2021-11-29
     */
    int updateRMOUTTByDef(@Param("owpriority")String owpriority ,@Param("rmoutt")Rmoutt rmoutt);

    /**
     *  zk add
     *  2021-12-01
     */
    List<HashMap<String,Object>> getSumByStatGroupByBrno(@Param("date") String date, @Param("stats")String[] stats);

    /**
     *  zk add
     *  2021-12-01
     */
    List<HashMap<String,Object>> getRMOUTTByTxdateBrno(@Param("txdate") String txdate, @Param("brno") String brno);
}
