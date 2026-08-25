package com.syscom.fep.mybatis.dao;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.ext.model.FeptxnMsgRsExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Inbkpend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FeptxnDao {

    public void setTableNameSuffix(String tableNameSuffix, String invoker);

    public String getTableNameSuffix();

    public int deleteByPrimaryKey(Feptxn feptxn);

    public int insert(Feptxn feptxn) throws Exception;

    public int insertSelective(Feptxn feptxn) throws Exception;

    public Feptxn selectByPrimaryKey(String feptxnTxDate, Integer feptxnEjfno) throws Exception;

    public Feptxn selectTraceEjfnoByPrimaryKey(String feptxnTxDate, Integer feptxnEjfno) throws Exception;

    public int updateByPrimaryKeySelective(Feptxn feptxn) throws Exception;

    public int updateByPrimaryKey(Feptxn feptxn) throws Exception;
    
    public int insertSelective(Feptxntcb feptxntcb) throws Exception;
    
    public int updateByPrimaryKeySelective(Feptxntcb feptxntcb) throws Exception;
    
    public Feptxntcb selectByPrimaryKeyForFeptxntcb(String feptxnTxDate, Integer feptxnEjfno) throws Exception;

    /**
     * Bruce add 以EJ查FepTxn
     *
     * @param argsMap
     * @return
     */
    public PageInfo<Feptxn> getFeptxnByEj(Map<String, Object> argsMap);

    /**
     * 2021/04/20
     * ZhaoKai ADD
     *
     * @param feptxnStan
     * @param feptxnBkno
     * @return
     */
    public Feptxn getFEPTXNByStanAndBkno(
            String feptxnStan,
            String feptxnBkno);

    /**
     * 2021/04/22
     * ZhaoKai ADD
     *
     * @param txDate
     * @param bkno
     * @param stan
     * @return
     */
    public Feptxn getFEPTXNByReqDateAndStan(
            String txDate,
            String bkno,
            String stan);

    /**
     * 2021/04/23
     * WJ ADD
     * 2021-07-09 Richard modified
     */
    public Feptxn getFEPTXNForTMO(
            String feptxnTxDateAtm,
            String feptxnAtmNo,
            String feptxnAtmSeqNo);


    public Feptxn getFEPTXNForConData(
            String feptxnTxDateAtm,
            String feptxnAtmNo,
            String feptxnAtmSeqNo,
            String feptxnTxCode,
            String feptxnChannelEjfno);

    public Feptxn getFEPTXNForATMConData(
            String feptxnTxDateAtm,
            String feptxnAtmNo,
            String feptxnAtmSeqNo,
            String feptxnTxCode,
            String feptxnMsgid);

    public Feptxn getFEPTXNForATMCheckConData(
            String feptxnTxDateAtm,
            String feptxnAtmNo,
            String feptxnAtmSeqNo,
            String feptxnTxCode,
            String feptxnMsgid,
            Integer feptxnTraceEjfno);
    /**
     * add by qin
     * 2021-07-19 Richard modified
     *
     * @param txdate
     * @param channelejfno
     * @param ejfno
     * @param txrust
     * @return
     */
    public int queryByChannelEJ(
            String txdate,
            String channelejfno,
            Integer ejfno,
            String txrust);

    public int queryByChannelEJAndChannel(
            String txdate,
            String channelejfno,
            Integer ejfno,
            String channel,
            String txrust);

    /**
     * 2021/05/24
     * WJ ADD
     *
     * @param feptxnTxDateAtm
     * @param feptxnAtmNo
     * @param feptxnAtmSeqNo
     * @return
     */
    public List<Feptxn> selectFEPTXNForCheckATMSeq(
            String feptxnTxDateAtm,
            String feptxnAtmNo,
            String feptxnAtmSeqNo);

    /**
     * FEP Web 查詢OPC交易記錄
     *
     * @param datetime
     * @param pcodes
     * @param bknos
     * @param stans
     * @param ejnos
     * @param nbSDY
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Feptxn> selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
            String datetime,
            String pcodes,
            String bknos,
            String stans,
            Integer ejnos,
            String nbSDY, Integer pageNum, Integer pageSize);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    public PageInfo<Feptxn> getFeptxn(Map<String, Object> args);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    public Map<String, Object> getFeptxnSummary(Map<String, Object> args);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnTxDate
     * @param feptxnEjfno
     * @return
     */
    public Map<String, Object> getFeptxnIntltxn(String feptxnTxDate, Integer feptxnEjfno);

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnEjfno
     * @return
     */
    public Map<String, Object> getFeptxnIntltxnFor019050(Integer feptxnEjfno);

    /**
     * 2021/08/20
     * zk add
     *
     * @param sysDatetime
     * @param feptxnTxDate
     * @param feptxnBkno
     * @param sysstatHbkno
     * @param feptxnStan
     * @param feptxnTbsdyFisc
     * @return
     */
    public List<Feptxn> getFEPTXNFor2280(
            String wk_TXDATE_S,
            String wk_TXDATE_E,
            String sysDatetime,
            String feptxnTxDate,
            String feptxnBkno,
            String sysstatHbkno,
            String feptxnStan,
            String feptxnTbsdyFisc);

    /**
     * 2023/01/17
     * Joseph add
     *
     * @return
     */
    public List<Feptxn> getFEPTXNFor2290(
            String wk_TXDATE_S,
            String wk_TXDATE_E,
            String feptxnTxDate,
            String feptxnBkno,
            String sysstatHbkno,
            String feptxnStan,
            String feptxnTbsdyFisc);


    /**
     * xingyun
     *
     * @param way
     * @param datetime
     * @param stime
     * @param etime
     * @param datetimeo
     * @param trad
     * @param bkno
     * @param stan
     * @return
     */
    public PageInfo<Feptxn> selectByRetention(
            String way, String sysstatHbkno, String datetime, String stime, String etime, String datetimeo, String bkno, String stan, String trad, Integer pageNum, Integer pageSize
    );

    /**
     * 2021/09/28
     * chenyang
     *
     * @param txDate
     * @param bkno
     * @param stan
     * @return
     */
    public Feptxn getFEPTXNMSTRByStan(String txDate, String bkno, String stan);


    /**
     * xy add
     *
     * @param datetime
     * @param inbkpendPcode
     * @return
     */
    public PageInfo<Inbkpend> getINBKPendList(String datetime, String inbkpendPcode, Integer pageNum, Integer pageSize);


    public PageInfo<HashMap<String, Object>> getFWDTXNByTSBDYFISC(String fwdrstTxDate, String selectValue, String fwdtxnTxId,
                                                                  String channel, String fwdtxnTroutActno, String fwdtxnTrinBkno,
                                                                  String fwdtxnTrinActno, String fwdtxnTxAmt, Short sysFail,
                                                                  Integer pageNum, Integer pageSize) throws Exception;

    /**
     * 2021/10/12
     * chenyu
     *
     * @param txDate
     * @param bkno
     * @param stan
     * @return
     */
    public Feptxn getFeptxnByStan(String txDate, String bkno, String stan);

    /**
     * zk Add
     */
    public List<Feptxn> queryFEPTXNForCheckPVDATA(
            String txDateAtm,
            String atmno,
            String atmSeqno,
            String txCode,
            String idno,
            String ascRc);

    /**
     * Han 	2022/08/24
     *
     * @param FEPTXN_TX_DATE
     * @param FEPTXN_BKNO
     * @param FEPTXN_STAN
     * @param FEPTXN_PCODE
     * @return
     */
    public List<Map<String, Object>> getgetCallAa2130Data(
            String tableNameSuffix,
            String FEPTXN_TX_DATE,
            String FEPTXN_BKNO,
            String FEPTXN_STAN,
            String FEPTXN_PCODE);

    public Feptxn getOldFeptxndata(
            String FEPTXN_TX_DATE_ATM,
            String FEPTXN_CHANNEL_EJFNO,
            String FEPTXN_CHANNEL);

    /**
     * Bruis 	2024/06/28
     *
     * @param tbsdyfisc
     * @param bkno
     * @param stan
     * @return
     */
    public Feptxn getoriFEPTXNData2270(String tbsdyfisc, String bkno, String stan);
    
    public Feptxn getFEPTXNForDSA(String feptxnTxDate, String feptxnAtmNo, String feptxnTxCode, String feptxnTxSeq, String feptxnMsgid);
    
    public int updateForP1AaRc(Feptxn feptxn) throws Exception;

    public List<FeptxnMsgRsExt> getAPIFeptxnByStan(String FeptxnTxDate, String BankNo, String StanNo);

    public int updateConfirmByPrimaryKey(Feptxn feptxn);

    /**
     * 2026/6/17 2700新增
     * LeYun
     */
    public List<Map<String, Object>> getFEPTXN2700Result510(
            String stDate,
            String endDate,
            String queryDate,
            String hbkno,
            String brapSelect);
    public List<Map<String, Object>> getFEPTXN2700Result520(
            String stDate,
            String endDate,
            String queryDate,
            String hbkno,
            String brapSelect);
    public List<Map<String, Object>> getFEPTXN2700Result530(
            String stDate,
            String endDate,
            String queryDate,
            String hbkno,
            String transSelect);
    public FeptxnExt get2700Tx530Action(String txDate, String bkno, String stan);
    int updateFeptxnConExcpCode(
            String txDate,
            String bkno,
            String stan,
            String conExcpCode);
    int updateFeptxnConRc(String txDate, String bkno, String stan, String conExcpCode, String txRust);
}
