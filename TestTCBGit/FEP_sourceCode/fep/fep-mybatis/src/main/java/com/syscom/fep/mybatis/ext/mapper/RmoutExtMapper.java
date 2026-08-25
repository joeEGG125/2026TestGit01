package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.ext.model.RmoutExt;
import com.syscom.fep.mybatis.mapper.RmoutMapper;
import com.syscom.fep.mybatis.model.Rmout;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RmoutExtMapper extends RmoutMapper {

    Rmout getRMOUTByCheckORGData(Rmout oRmout);

    Rmout getSingleRMOUT(Rmout oRmout);

    Rmout getRMOUTForCheckOutData(Rmout oRmout);

    List<Rmout> getRmoutByDef(Rmout oRmout);

    List<RmoutExt> getRMOUTUnionRMOUTEByDef(@Param("rmout")Rmout rmout,@Param("isOrderByFISCSNO")Boolean isOrderByFISCSNO);

    Rmout queryByPrimaryKeyWithUpdLock(@Param("rmoutTxdate") String rmoutTxdate, @Param("rmoutBrno") String rmoutBrno, @Param("rmoutOriginal") String rmoutOriginal, @Param("rmoutFepno") String rmoutFepno);

    int updateByPrimaryKeyWithStat(@Param("rmout") Rmout rmout, @Param("stats")String[] stats);

    int updateRMOUTByFISCNO(Rmout rmout);

    List<HashMap<String,Object>> getRmoutSummaryCnt(@Param("rmoutTxdate") String rmoutTxdate, @Param("code") String code);

    List<HashMap<String,Object>> get1172SummaryCnt(@Param("txdate") String txdate, @Param("sndCode") String sndCode,
                                                    @Param("stat") String stat,@Param("rtnCode") String rtnCode);

    List<HashMap<String,Object>> getAutoBackSummaryCnt(@Param("txdate") String txdate, @Param("sndCode") String sndCode,
                                                   @Param("stat") String stat,@Param("rtnCode") String rtnCode);

    List<HashMap<String,Object>> getAutoBackBank();

    List<HashMap<String,Object>> getTopRMOUTByApdateSenderBank(@Param("topNumber") Integer topNumber,
                                                               @Param("sendDate")String sendDate,
                                                               @Param("senderBank")String senderBank);
    List<HashMap<String,Object>> getTopRMOUTByApdateSenderBank1(@Param("topNumber") Integer topNumber,
                                                               @Param("sendDate")String sendDate,
                                                               @Param("senderBank")String senderBank);
    /**
    * 2021/11/29 ZK
    *
    * */
    List<Rmout> getRMOUTbyTXAMT(@Param("txDate") String txDate, @Param("txAmt")  String txAmt);

    /**
     * 2021/11/29 ZK
     *
     * */
    List<Rmout> getRMOUTbyReceiverBANK(@Param("txDate") String txDate, @Param("receiverBANK")  String receiverBANK);

    /**
     *  zk add
     *  2021-11-29
     */
    int updateRMOUTByDef(@Param("owpriority")String owpriority, @Param("rmout") Rmout rmout);
    
    /**
    * 2021/12/03 wj
    *
    * */
    List<HashMap<String,Object>> getRMOUTForUI028230(@Param("txDate") String txDate);
}
