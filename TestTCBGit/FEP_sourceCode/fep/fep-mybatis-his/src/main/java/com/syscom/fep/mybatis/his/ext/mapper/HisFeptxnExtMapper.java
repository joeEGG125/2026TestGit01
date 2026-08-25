package com.syscom.fep.mybatis.his.ext.mapper;

import com.syscom.fep.mybatis.his.mapper.FeptxnMapper;
import com.syscom.fep.mybatis.his.model.Feptxn;
import org.apache.ibatis.annotations.Param;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Resource
public interface HisFeptxnExtMapper extends FeptxnMapper {
	
	/**
	 * Bruce add 
	 * @param argsMap
	 * @return
	 */
	public List<Feptxn> getFeptxnByEj(@Param("argsMap") Map<String,Object> argsMap);
	
	/**
	 * Richard add for test
	 * 
	 * @param feptxn
	 * @return
	 */
	public Feptxn forTest(Feptxn feptxn);
	/**
	 * 
	 * 2021/04/23
	 * WJ ADD
	 * 2021-07-09 Richard modified
	 * 
	 * @param tableNameSuffix
	 * @param feptxnTxDateAtm
	 * @param feptxnAtmNo
	 * @param feptxnAtmSeqNo
	 * @return
	 */
	public Feptxn getFEPTXNForTMO(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnTxDateAtm") String feptxnTxDateAtm,
			@Param("feptxnAtmNo") String feptxnAtmNo,
			@Param("feptxnAtmSeqNo") String feptxnAtmSeqNo);

	public Feptxn getFEPTXNForConData(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnTxDateAtm") String feptxnTxDateAtm,
			@Param("feptxnAtmNo") String feptxnAtmNo,
			@Param("feptxnAtmSeqNo") String feptxnAtmSeqNo,
			@Param("feptxnTxCode") String feptxnTxCode,
			@Param("feptxnChannelEjfno") String feptxnChannelEjfno);
	public Feptxn getFEPTXNForATMConData(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnTxDateAtm") String feptxnTxDateAtm,
			@Param("feptxnAtmNo") String feptxnAtmNo,
			@Param("feptxnAtmSeqNo") String feptxnAtmSeqNo,
			@Param("feptxnTxCode") String feptxnTxCode);
	/**
	 * 2021/04/20
	 * ZhaoKai ADD
	 * 
	 * @param feptxnStan
	 * @param feptxnBkno
	 * @return
	 */
	public Feptxn getFEPTXNByStanAndBkno(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnStan") String feptxnStan,
			@Param("feptxnBkno") String feptxnBkno);

	/**
	 * 2021/04/22
	 * ZhaoKai ADD
	 * 
	 * @param tableNameSuffix
	 * @param feptxnReqDatetime
	 * @param feptxnBkno
	 * @param feptxnStan
	 * @return
	 */
	public Feptxn getFEPTXNByReqDateAndStan(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnReqDatetime") String feptxnReqDatetime,
			@Param("feptxnBkno") String feptxnBkno,
			@Param("feptxnStan") String feptxnStan);

	/**
	 * add by qin
	 * 2021-07-16 Richard modified
	 * 
	 * @param tableNameSuffix
	 * @param txdate
	 * @param channelejfno
	 * @param txrust
	 * @return
	 */
	public List<Feptxn> queryByChannelEJ(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("txdate") String txdate,
			@Param("channelejfno") String channelejfno,
			@Param("txrust") String txrust);

	/**
	 * 2021/05/24
	 * WJ ADD
	 * 
	 * @param tableNameSuffix
	 * @param feptxnTxDateAtm
	 * @param feptxnAtmNo
	 * @param feptxnAtmSeqNo
	 * @return
	 */
	public List<Feptxn> selectFEPTXNForCheckATMSeq(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnTxDateAtm") String feptxnTxDateAtm,
			@Param("feptxnAtmNo") String feptxnAtmNo,
			@Param("feptxnAtmSeqNo") String feptxnAtmSeqNo);

	/**
	 * FEP Web 查詢OPC交易記錄
	 * 
	 * @param tableNameSuffix
	 * @param datetime
	 * @param pcodes
	 * @param bknos
	 * @param stans
	 * @param ejnos
	 * @param nbSDY
	 * @return
	 */
	public List<Feptxn> selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("datetime") String datetime,
			@Param("pcodes") String pcodes,
			@Param("bknos") String bknos,
			@Param("stans") String stans,
			@Param("ejnos") Integer ejnos,
			@Param("nbSDY") String nbSDY);

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢
	 * 
	 * @param map
	 * @return
	 */
	public List<Feptxn> getFeptxn(Map<String, Object> args);

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, Object> getFeptxnSummary(Map<String, Object> args);

	/**
	 * FEP Web 交易日誌(FEPTXN)查詢明細資料
	 * 
	 * @param tableNameSuffix
	 * @param feptxnTxDate
	 * @param feptxnEjfno
	 * @return
	 */
	public Map<String, Object> getFeptxnIntltxn(
			@Param("tableNameSuffix") String tableNameSuffix, 
			@Param("feptxnTxDate") String feptxnTxDate, 
			@Param("feptxnEjfno") Integer feptxnEjfno);

	public List<Feptxn> getFEPTXNFor2280(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("sysDatetime") String sysDatetime,
			@Param("feptxnTxDate") String feptxnTxDate,
			@Param("feptxnBkno") String feptxnBkno,
			@Param("sysstatHbkno") String sysstatHbkno,
			@Param("feptxnStan") String feptxnStan,
			@Param("feptxnTbsdyFisc") String feptxnTbsdyFisc);

	public List<Feptxn> getFEPTXNFor2290(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("feptxnTxDate") String feptxnTxDate,
			@Param("feptxnBkno") String feptxnBkno,
			@Param("sysstatHbkno") String sysstatHbkno,
			@Param("feptxnStan") String feptxnStan,
			@Param("feptxnTbsdyFisc") String feptxnTbsdyFisc);
	public Feptxn get01FEPTXNFor2280(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("sysDatetime") String sysDatetime,
			@Param("feptxnTxDate") String feptxnTxDate,
			@Param("feptxnBkno") String feptxnBkno,
			@Param("sysstatHbkno") String sysstatHbkno,
			@Param("feptxnStan") String feptxnStan,
			@Param("feptxnTbsdyFisc") String feptxnTbsdyFisc);

	/**
	 * xingyun_yang
	 * 2021-08-10 add
	 * @param tableNameSuffix
	 * @param way
	 * @param datetime
	 * @param stime
	 * @param etime
	 * @param datetimeo
	 * @param bkno
	 * @param stan
	 * @param trad
	 * @return
	 */
	public List<Feptxn> selectByRetention(@Param("tableNameSuffix") String tableNameSuffix,
										  @Param("way")String way,
										  @Param("sysstatHbkno")String sysstatHbkno,
										  @Param("datetime") String datetime,
										  @Param("stime") String stime,
										  @Param("etime") String etime,
										  @Param("datetimeo") String datetimeo,
										  @Param("bkno") String bkno,
										  @Param("stan") String stan,
										  @Param("trad") String trad
	);

	/**
	 * 2021/09/28
	 * ChenYang
	 * @param tableNameSuffix
	 * @param txDate
	 * @param bkno
	 * @param stan
	 * @return
	 */
	public Feptxn getFEPTXNMSTRByStan(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("txDate") String txDate,
			@Param("bkno") String bkno,
			@Param("stan") String stan);

	/**
	 * 2021/10/12
	 * ChenYu
	 * @param tableNameSuffix
	 * @param txDate
	 * @param bkno
	 * @param stan
	 * @return
	 */
	public Feptxn getFeptxnByStanbkno(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("txDate") String txDate,
			@Param("bkno") String bkno,
			@Param("stan") String stan);

	public List<Feptxn> queryFEPTXNForCheckPVDATA(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("txDateAtm") String txDateAtm,
			@Param("atmno") String atmno,
			@Param("atmSeqno") String atmSeqno,
			@Param("txCode") String txCode,
			@Param("idno") String idno,
			@Param("ascRc") String ascRc);

	/**
	 * Han add 2022-07-22
	 * @param string 
	 * @param _isWorkDay
	 * @param _fiscTbsdy
	 * @param _fiscNbsdy
	 * @param _beginTime
	 * @param _endTime
	 * @return
	 */
	public List<Map<String, Object>> GetFEPTXNForBT010080(
			//--ben start--
			//@Param("tableNameSuffix")String tableNameSuffix, 
			//@Param("_isWorkDay")boolean _isWorkDay, 
			//@Param("_fiscTbsdy")String  _fiscTbsdy, 
			//@Param("_fiscNbsdy")String  _fiscNbsdy,
			//@Param("_beginTime")String  _beginTime, 
			//@Param("_endTime")String  _endTime);
			@Param("tableNameSuffix")String tableNameSuffix,
			@Param("txdateb")String  txdateb,
			@Param("txdatee")String  txdatee
			);
			//--ben end--
	
	/**
	 * Han 	2022/07/29
	 * @param FEPTXN_TX_DATE
	 * @param FEPTXN_BKNO
	 * @param FEPTXN_STAN
	 * @param FEPTXN_PCODE
	 * @return
	 */
	public List<Map<String, Object>> getgetCallAa2130Data(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("FEPTXN_TX_DATE")String FEPTXN_TX_DATE, 
			@Param("FEPTXN_BKNO")String FEPTXN_BKNO, 
			@Param("FEPTXN_STAN")String FEPTXN_STAN,
			@Param("FEPTXN_PCODE")String FEPTXN_PCODE);
	
	/**
	 * @param FEPTXN_TX_DATE
	 * @param FEPTXN_BKNO
	 * @param FEPTXN_STAN
	 * @param FEPTXN_PCODE
	 * @return
	 */
	public Feptxn getOldFeptxndata(
			@Param("FEPTXN_TX_DATE_ATM") String FEPTXN_TX_DATE_ATM,
			@Param("FEPTXN_CHANNEL_EJFNO") String FEPTXN_CHANNEL_EJFNO,
			@Param("FEPTXN_CHANNEL") String FEPTXN_CHANNEL);

	/**
	 *
	 * @param FEPTXN_BKNO
	 * @param feptxnStan
	 * @param feptxnTxDate
	 * @return
	 */
	public Feptxn getoriFEPTXNData2120(
			@Param("FEPTXN_BKNO") String FEPTXN_BKNO,
			@Param("FEPTXN_STAN") String feptxnStan,
			@Param("FEPTXN_TX_DATE") String feptxnTxDate);

	/**
	 *
	 * @param FEPTXN_BKNO
	 * @param feptxnStan
	 * @param feptxnTxDate
	 * @param pcodes
	 * @return
	 */
	public Feptxn getoriFEPTXNData2150(
			@Param("FEPTXN_BKNO") String FEPTXN_BKNO,
			@Param("FEPTXN_STAN") String feptxnStan,
			@Param("FEPTXN_TX_DATE") String feptxnTxDate,
			@Param("FEPTXN_PCODE") String pcodes);

	public Feptxn getoriFEPTXNData2270(
			@Param("tableNameSuffix") String tableNameSuffix,
			@Param("tbsdyfisc") String tbsdyfisc,
			@Param("bkno") String bkno,
			@Param("stan") String stan);

	public HashMap<String,String> getPartion(@Param("tableName") String tableName,
											 @Param("partitionName") String partitionName);
	public int addFeptxnPartition(@Param("partitionName") String partitionName,
								  @Param("starting") String starting,
								  @Param("ending") String ending,
								  @Param("in") String in,
								  @Param("indexIn") String indexIn);
	public int addFeptxnTcbPartition(@Param("partitionName") String partitionName,
									 @Param("starting") String starting,
									 @Param("ending") String ending,
									 @Param("in") String in,
									 @Param("indexIn") String indexIn);
}
