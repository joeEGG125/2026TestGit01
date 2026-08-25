package com.syscom.fep.mybatis.dao.impl;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.FeptxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FwdtxnExtMapper;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.ext.model.FeptxnMsgRsExt;
import com.syscom.fep.mybatis.mapper.FeptxntcbMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Inbkpend;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("feptxnDao")
@Lazy
// 合庫合併了FEPTXN檔, 所以這個也不需要了, 單例化
// @Scope("prototype")
public class FeptxnDaoImpl implements FeptxnDao {
    @Autowired
    private FeptxnExtMapper feptxnExtMapper;

    @Autowired
    private InbkpendExtMapper inbkpendExtMapper;

    @Autowired
    private FwdtxnExtMapper fwdtxnExtMapper;
    
    @Autowired
    private FeptxntcbMapper feptxntcbMapper;

    /**
     * 合庫合併了FEPTXN檔, 所以這裡寫死為空字串
     */
    private final String tableNameSuffix = StringUtils.EMPTY;

    public static String getTableName(String tableNameSuffix) {
        return StringUtils.join("FEPTXN", tableNameSuffix);
    }

    private Feptxn setTableNameSuffix(Feptxn feptxn) {
        if (feptxn != null) {
            if (feptxn instanceof FeptxnExt) {
                ((FeptxnExt) feptxn).setTableNameSuffix(this.tableNameSuffix);
            } else {
                return this.setTableNameSuffix(new FeptxnExt(feptxn));
            }
        }
        return feptxn;
    }

    private List<Feptxn> setTableNameSuffix(List<Feptxn> list) {
        List<Feptxn> resultList = null;
        if (list != null) {
            resultList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                resultList.add(this.setTableNameSuffix(list.get(i)));
            }
        }
        return resultList;
    }

    @Override
    public void setTableNameSuffix(String tableNameSuffix, String invoker) {
        // 合庫合併了FEPTXN檔, 所以這裡不需要再塞入
        // this.tableNameSuffix = tableNameSuffix;
        // LogHelperFactory.getTraceLogger().info("Switch to [FEPTXN", tableNameSuffix, "] by [", invoker, "]");
    }

    @Override
    public String getTableNameSuffix() {
        return this.tableNameSuffix;
    }

    @Override
    public int deleteByPrimaryKey(Feptxn feptxn) {
        return feptxnExtMapper.deleteByPrimaryKey(this.setTableNameSuffix(feptxn));
    }

    @Override
    public int insert(Feptxn feptxn) {
        return feptxnExtMapper.insert(this.setTableNameSuffix(feptxn));
    }

    @Override
    public int insertSelective(Feptxn feptxn) {
        return feptxnExtMapper.insertSelective(this.setTableNameSuffix(feptxn));
    }

    @Override
    public Feptxn selectByPrimaryKey(String feptxnTxDate, Integer feptxnEjfno) {
        // return this.setTableNameSuffix(feptxnExtMapper.selectByPrimaryKey(this.tableNameSuffix, feptxnTxDate, feptxnEjfno));
        return this.setTableNameSuffix(feptxnExtMapper.selectByPrimaryKey(feptxnTxDate, feptxnEjfno));
    }

    @Override
    public Feptxn selectTraceEjfnoByPrimaryKey(String feptxnTxDate, Integer feptxnEjfno) {
        // return this.setTableNameSuffix(feptxnExtMapper.selectByPrimaryKey(this.tableNameSuffix, feptxnTxDate, feptxnEjfno));
        return this.setTableNameSuffix(feptxnExtMapper.selectTraceEjfnoByPrimaryKey(feptxnTxDate, feptxnEjfno));
    }

    @Override
    public int updateByPrimaryKeySelective(Feptxn feptxn) {
        return feptxnExtMapper.updateByPrimaryKeySelective(this.setTableNameSuffix(feptxn));
    }

    @Override
    public int updateByPrimaryKey(Feptxn feptxn) {
        return feptxnExtMapper.updateByPrimaryKey(this.setTableNameSuffix(feptxn));
    }

    @Override
    public Feptxn getFEPTXNByStanAndBkno(String feptxnStan, String feptxnBkno) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNByStanAndBkno(this.tableNameSuffix, feptxnStan, feptxnBkno));
    }

    @Override
    public Feptxn getFEPTXNByReqDateAndStan(String feptxnReqDatetime, String feptxnBkno, String feptxnStan) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNByReqDateAndStan(this.tableNameSuffix, feptxnReqDatetime, feptxnBkno, feptxnStan));
    }

    @Override
    public Feptxn getFEPTXNForTMO(String feptxnTxDateAtm, String feptxnAtmNo, String feptxnAtmSeqNo) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNForTMO(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnAtmSeqNo));
    }

    @Override
    public Feptxn getFEPTXNForConData(String feptxnTxDateAtm, String feptxnAtmNo, String feptxnAtmSeqNo, String feptxnTxCode, String feptxnChannelEjfno) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNForConData(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnAtmSeqNo, feptxnTxCode,feptxnChannelEjfno));
    }

    @Override
    public Feptxn getFEPTXNForATMConData(String feptxnTxDateAtm, String feptxnAtmNo, String feptxnAtmSeqNo, String feptxnTxCode, String feptxnMsgid) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNForATMConData(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnAtmSeqNo, feptxnTxCode,feptxnMsgid));
    }

    @Override
    public Feptxn getFEPTXNForATMCheckConData(String feptxnTxDateAtm, String feptxnAtmNo, String feptxnAtmSeqNo, String feptxnTxCode, String feptxnMsgid, Integer feptxnTraceEjfno) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNForATMCheckConData(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnAtmSeqNo, feptxnTxCode,feptxnMsgid,feptxnTraceEjfno));
    }

    @Override
    public int queryByChannelEJ(String txdate, String channelejfno, Integer ejfno, String txrust) {
        List<Feptxn> result = this.setTableNameSuffix(feptxnExtMapper.queryByChannelEJ(this.tableNameSuffix, txdate, channelejfno, txrust));
        if (CollectionUtils.isEmpty(result)) { // 0筆
            return 0;
        } else if (result.size() == 1) { // 1筆(因含本筆需以FEPTXN_EJFNO判斷是否重覆)
            if (result.get(0).getFeptxnEjfno().equals(ejfno)) {
                return 0;
            } else {
                return 1;
            }
        } else { // 2筆以上
            return 1;
        }
    }

    @Override
    public int queryByChannelEJAndChannel(String txdate, String channelejfno, Integer ejfno, String txrust, String channel) {
        List<Feptxn> result = this.setTableNameSuffix(feptxnExtMapper.queryByChannelEJAndChannel(this.tableNameSuffix, txdate, channelejfno, txrust, channel));
        if (CollectionUtils.isEmpty(result)) { // 0筆
            return 0;
        } else if (result.size() == 1) { // 1筆(因含本筆需以FEPTXN_EJFNO判斷是否重覆)
            if (result.get(0).getFeptxnEjfno().equals(ejfno)) {
                return 0;
            } else {
                return 1;
            }
        } else { // 2筆以上
            return 1;
        }
    }

    @Override
    public List<Feptxn> selectFEPTXNForCheckATMSeq(String feptxnTxDateAtm, String feptxnAtmNo, String feptxnAtmSeqNo) {
        return this.setTableNameSuffix(feptxnExtMapper.selectFEPTXNForCheckATMSeq(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnAtmSeqNo));
    }

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
    @Override
    public PageInfo<Feptxn> selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
            String datetime,
            String pcodes,
            String bknos,
            String stans,
            Integer ejnos,
            String nbSDY, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分頁查詢
        PageInfo<Feptxn> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feptxnExtMapper.selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
                        tableNameSuffix,
                        DbHelper.avoidEmpty(datetime),
                        pcodes,
                        DbHelper.avoidEmpty(bknos),
                        DbHelper.avoidEmpty(stans),
                        ejnos,
                        nbSDY);
            }
        });
        // 這裡最後要再轉一次, 否則無法取到FeptxnExt物件
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    @Override
    public PageInfo<Feptxn> getFeptxn(Map<String, Object> args) {
        Integer pageNum = (Integer) args.get("pageNum");
        Integer pageSize = (Integer) args.get("pageSize");
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 千萬不要忘記塞入這個
        args.put("tableNameSuffix", this.tableNameSuffix);
        // 分頁查詢
        PageInfo<Feptxn> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feptxnExtMapper.getFeptxn(args);
            }
        });
        // 這裡最後要再轉一次, 否則無法取到FeptxnExt物件
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢
     *
     * @param args
     * @return
     */
    @Override
    public Map<String, Object> getFeptxnSummary(Map<String, Object> args) {
        args.put("tableNameSuffix", this.tableNameSuffix);
        return feptxnExtMapper.getFeptxnSummary(args);
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnTxDate
     * @param feptxnEjfno
     * @return
     */
    @Override
    public Map<String, Object> getFeptxnIntltxn(String feptxnTxDate, Integer feptxnEjfno) {
        Map<String, Object> result = feptxnExtMapper.getFeptxnIntltxn(this.tableNameSuffix, feptxnTxDate, feptxnEjfno);
        return result;
    }

    /**
     * FEP Web 交易日誌(FEPTXN)查詢明細資料
     *
     * @param feptxnEjfno
     * @return
     */
    @Override
    public Map<String, Object> getFeptxnIntltxnFor019050(Integer feptxnEjfno) {
        Map<String, Object> result = feptxnExtMapper.getFeptxnIntltxnFor019050(this.tableNameSuffix, feptxnEjfno);
        return result;
    }

    @Override
    public List<Feptxn> getFEPTXNFor2280(String wk_TXDATE_S, String wk_TXDATE_E, String sysDatetime, String feptxnTxDate, String feptxnBkno, String sysstatHbkno, String feptxnStan, String feptxnTbsdyFisc) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNFor2280(this.tableNameSuffix, wk_TXDATE_S, wk_TXDATE_E, sysDatetime, feptxnTxDate, feptxnBkno, sysstatHbkno, feptxnStan, feptxnTbsdyFisc));
    }

    @Override
    public List<Feptxn> getFEPTXNFor2290(String wk_TXDATE_S, String wk_TXDATE_E, String feptxnTxDate, String feptxnBkno, String sysstatHbkno, String feptxnStan, String feptxnTbsdyFisc) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNFor2290(this.tableNameSuffix, wk_TXDATE_S, wk_TXDATE_E, feptxnTxDate, feptxnBkno, sysstatHbkno, feptxnStan, feptxnTbsdyFisc));
    }

    @Override
    public PageInfo<Feptxn> selectByRetention(String way, String sysstatHbkno, String datetime, String stime, String etime, String datetimeo, String bkno, String stan, String trad, Integer pageNum,
                                              Integer pageSize) {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 找營業日
        String suffix = datetime.substring(6, 8);
        // 分頁查詢
        PageInfo<Feptxn> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feptxnExtMapper.selectByRetention(
                        suffix, way, sysstatHbkno, datetime, StringUtils.isNotBlank(stime) ? stime.replace(":", "") : "", StringUtils.isNotBlank(etime) ? etime.replace(":", "") : "", datetimeo, bkno, stan, trad);
            }
        });
        // 這裡最後要再轉一次, 否則無法取到FeptxnExt物件
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    /**
     * 2021/09/28
     * chenyang
     *
     * @param txDate
     * @param bkno
     * @param stan
     * @return
     */
    public Feptxn getFEPTXNMSTRByStan(String txDate, String bkno, String stan) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNMSTRByStan(this.tableNameSuffix, txDate, bkno, stan));
    }

    @Override
    public PageInfo<Inbkpend> getINBKPendList(String datetime, String inbkpendPcode, Integer pageNum, Integer pageSize) {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分頁查詢
        PageInfo<Inbkpend> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                inbkpendExtMapper.getINBKPendList(datetime, inbkpendPcode);
            }
        });
        return pageInfo;
    }

    @Override
    public PageInfo<HashMap<String, Object>> getFWDTXNByTSBDYFISC(String fwdrstTxDate, String selectValue, String fwdtxnTxId, String channel, String fwdtxnTroutActno, String fwdtxnTrinBkno,
                                                                  String fwdtxnTrinActno, String fwdtxnTxAmt, Short sysFail, Integer pageNum, Integer pageSize) throws Exception {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        // 分頁查詢
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                fwdtxnExtMapper.getFWDTXNByTSBDYFISC(fwdrstTxDate, selectValue, fwdtxnTxId, channel, fwdtxnTroutActno, fwdtxnTrinBkno, fwdtxnTrinActno, fwdtxnTxAmt, sysFail);
            }
        });
        pageInfo.setList(pageInfo.getList());
        return pageInfo;
    }

    public Feptxn getFeptxnByStan(String txDate, String bkno, String stan) {
        return this.setTableNameSuffix(feptxnExtMapper.getFeptxnByStanbkno(this.tableNameSuffix, txDate, bkno, stan));
    }

    public List<Feptxn> queryFEPTXNForCheckPVDATA(String txDateAtm, String atmno, String atmSeqno, String txCode, String idno, String ascRc) {
        return this.setTableNameSuffix(feptxnExtMapper.queryFEPTXNForCheckPVDATA(this.tableNameSuffix, txDateAtm, atmno, atmSeqno, txCode, idno, ascRc));
    }

    public Feptxn getOldFeptxndata(String txDateAtm, String channelEjfno, String channel) {
        return feptxnExtMapper.getOldFeptxndata(txDateAtm, channelEjfno, channel);
    }

    /**
     * Bruce add UI_060610 feplog 查回 feptxn
     */
    @Override
    public PageInfo<Feptxn> getFeptxnByEj(Map<String, Object> argsMap) {
        int pageNum = argsMap.get("pageNum") == null ? 0 : (int) argsMap.get("pageNum");
        int pageSize = argsMap.get("pageSize") == null ? 0 : (int) argsMap.get("pageSize");
        // 分頁查詢
        PageInfo<Feptxn> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                feptxnExtMapper.getFeptxnByEj(argsMap);
            }
        });
        pageInfo.setList(this.setTableNameSuffix(pageInfo.getList()));
        return pageInfo;
    }

    public List<Map<String, Object>> getgetCallAa2130Data(String tableNameSuffix, String FEPTXN_TX_DATE, String FEPTXN_BKNO, String FEPTXN_STAN, String FEPTXN_PCODE) {
        return feptxnExtMapper.getgetCallAa2130Data(tableNameSuffix, FEPTXN_TX_DATE, FEPTXN_BKNO, FEPTXN_STAN, FEPTXN_PCODE);
    }

    public Feptxn getoriFEPTXNData2120(String FEPTXN_BKNO, String feptxnStan, String feptxnTxDate) {
        return feptxnExtMapper.getoriFEPTXNData2120(FEPTXN_BKNO, feptxnStan, feptxnTxDate);
    }

    public Feptxn getoriFEPTXNData2150(String FEPTXN_BKNO, String feptxnStan, String feptxnTxDate, String pcodes) {
        return feptxnExtMapper.getoriFEPTXNData2150(FEPTXN_BKNO, feptxnStan, feptxnTxDate, pcodes);
    }

    public Feptxn getoriFEPTXNData2270(String tbsdyfisc, String bkno, String stan) {
        return this.setTableNameSuffix(feptxnExtMapper.getoriFEPTXNData2270(this.tableNameSuffix, tbsdyfisc, bkno, stan));
    }

	@Override
	public int insertSelective(Feptxntcb feptxntcb) throws Exception {
		return feptxntcbMapper.insertSelective(feptxntcb);
	}
	
	@Override
    public int updateByPrimaryKeySelective(Feptxntcb Feptxntcb) {
        return feptxntcbMapper.updateByPrimaryKeySelective(Feptxntcb);
    }
	
    @Override
    public Feptxntcb selectByPrimaryKeyForFeptxntcb(String feptxnTxDate, Integer feptxnEjfno) {
        return feptxntcbMapper.selectByPrimaryKey(feptxnTxDate, feptxnEjfno);
    }
    
    @Override
    public Feptxn getFEPTXNForDSA(String feptxnTxDateAtm, String feptxnAtmNo,String feptxnTxCode, String feptxnTxSeq, String feptxnMsgid) {
        return this.setTableNameSuffix(feptxnExtMapper.getFEPTXNForDSA(this.tableNameSuffix, feptxnTxDateAtm, feptxnAtmNo, feptxnTxCode, feptxnTxSeq, feptxnMsgid));
    }
    
    @Override
    public int updateForP1AaRc(Feptxn feptxn) {
        return feptxnExtMapper.updateForP1AaRc(this.setTableNameSuffix(feptxn));
    }

    @Override
    public List<FeptxnMsgRsExt> getAPIFeptxnByStan(String FeptxnTxDate, String BankNo, String StanNo) {
        return feptxnExtMapper.getFeptxnByStan(FeptxnTxDate, BankNo, StanNo);
    }

    @Override
    public int updateConfirmByPrimaryKey(Feptxn feptxn) {
        return feptxnExtMapper.updateConfirmByPrimaryKey(this.setTableNameSuffix(feptxn));
    }

    // 2026/6/17 2700新增
    public List<Map<String, Object>> getFEPTXN2700Result510(String stDate, String endDate, String queryDate, String hbkno, String brapSelect) {
        return feptxnExtMapper.getFEPTXN2700Result510(stDate, endDate, queryDate, hbkno, brapSelect);
    }
    public List<Map<String, Object>> getFEPTXN2700Result520(String stDate, String endDate, String queryDate, String hbkno, String brapSelect) {
        return feptxnExtMapper.getFEPTXN2700Result520(stDate, endDate, queryDate, hbkno, brapSelect);
    }
    public List<Map<String, Object>> getFEPTXN2700Result530(String stDate, String endDate, String queryDate, String hbkno, String transSelect) {
        return feptxnExtMapper.getFEPTXN2700Result530(stDate, endDate, queryDate, hbkno, transSelect);
    }
    public FeptxnExt get2700Tx530Action(String txDate, String bkno, String stan) {
        return feptxnExtMapper.get2700Tx530Action(txDate, bkno, stan);
    }
    public int updateFeptxnConExcpCode(String txDate, String bkno, String stan, String conExcpCode) {
        return feptxnExtMapper.updateFeptxnConExcpCode(txDate, bkno, stan, conExcpCode);
    }
    public int updateFeptxnConRc(String txDate, String bkno, String stan, String conExcpCode, String txRust){
        return feptxnExtMapper.updateFeptxnConRc(txDate, bkno, stan, conExcpCode, txRust);
    }
}
