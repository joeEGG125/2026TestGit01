package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.mapper.RminMapper;
import com.syscom.fep.mybatis.model.Rmin;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Resource
public interface RminExtMapper extends RminMapper {

    public int updateByPrimaryKey(Rmin oRmin, boolean initialFieldStatus);

    public Rmin getRMINByCheckINData(Rmin defRmin);

    List<Rmin> getSingleRMIN(Rmin oRmout);

    List<Rmin> queryRMINForUI028110(Rmin oRmin);

    List<Rmin> getRminByDef(Rmin oRmin);

    List<HashMap<String,Object>> getTopRMINByTxdateReciveBank(@Param("topNumber") Integer topNumber,
                                                                @Param("txDate")String txDate,
                                                                @Param("receiver")String receiver);
    /**
     * xy add  查詢全行之匯出未解筆數金額
     * 2021-10-31
     */
    int getTotalCntByStat(@Param("date") String date, @Param("stat") String stat);

    List<Rmin> getRMINForResend(Rmin rmin);

    /**
     *  zk add
     *  2021-12-01
     */
    List<HashMap<String,Object>> getSumByStatGroupByBrno(@Param("date") String date, @Param("stat") String stat);
    /**
     *  xy add
     *  2021-12-06
     */
    List<HashMap<String,Object>> getAMLReSendData(String date);

    /**
     *  cy add
     *  2021-12-06
     */
    List<HashMap<String,Object>> getRMINByDateSendbankPendingEJ(@Param("txdate") String txdate,@Param("senderBank")String senderBank,@Param("stat")String stat,@Param("ej")String ej);

    List<HashMap<String,Object>> getRMINUnionMSGIN(@Param("txdate") String txdate,@Param("senderBank")String senderBank,@Param("stat")String stat,@Param("fiscRtnCode")String fiscRtnCode,@Param("ej")String ej);
    Integer updateByPrimaryKeyWithStat(@Param("rmin") Rmin rmin,@Param("stat")String stat);
}

