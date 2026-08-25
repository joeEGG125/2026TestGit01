package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.ext.model.RmoutExt;
import com.syscom.fep.mybatis.mapper.PrgstatMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMINStatus;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMPCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author xingyun_yang
 * @create 2021/9/24
 */
@Service
public class RmService extends BaseService{
    @Autowired
    UserDefineExtMapper userDefineExtMapper;
    @Autowired
    MsgoutExtMapper msgoutExtMapper;
    @Autowired
    RmoutExtMapper rmoutExtMapper;
    @Autowired
    RmouteExtMapper rmouteExtMapper;
    @Autowired
    FeptxnExtMapper feptxnExtMapper;
    @Autowired
    CurcdExtMapper curcdExtMapper;
    @Autowired
    MsginExtMapper msginExtMapper;
    @Autowired
    RminExtMapper rminExtMapper;
    @Autowired
    RmoutsnoExtMapper rmoutsnoExtMapper;
    @Autowired
    RmstatExtMapper rmstatExtMapper;
    @Autowired
    AllbankExtMapper allbankExtMapper;
    @Autowired
    SysstatExtMapper sysstatExtMapper;
    @Autowired
    RmmonExtMapper rmmonExtMapper;
    @Autowired
    RmouttExtMapper rmouttExtMapper;
    @Autowired
    Rmfiscout1ExtMapper rmfiscout1ExtMapper;
    @Autowired
    Rmfiscout4ExtMapper rmfiscout4ExtMapper;
    @Autowired
    Rmfiscin1ExtMapper rmfiscin1ExtMapper;
    @Autowired
    Rmfiscin4ExtMapper rmfiscin4ExtMapper;
    @Autowired
    ClrtotalExtMapper clrtotalExtMapper;
    @Autowired
    PrgstatMapper prgstatMapper;
    @Autowired
    RminsnoExtMapper rminsnoExtMapper;
    @Autowired
    RmintExtMapper rmintExtMapper;
    @Autowired
    RmbtchmtrExtMapper rmbtchmtrExtMapper;
    @Autowired
    BatchExtMapper batchExtMapper;
    @Autowired
    RmbtchExtMapper rmbtchExtMapper;
    @Autowired
    ClrdtlExtMapper clrdtlExtMapper;
    public int getRmNo(String brno,String category) throws Exception {
        //呼叫storeprocedure
        Rmnoctl defRmnoctl = new Rmnoctl();
        int rmno = 0;
        try {
            defRmnoctl.setRmnoctlBrno(brno);
            defRmnoctl.setRmnoctlCategory(category);
            int nextId = 0;
            Map<String,Object> pars = new HashMap<>();
            pars.put("brno",defRmnoctl.getRmnoctlBrno());
            pars.put("category",defRmnoctl.getRmnoctlCategory());
            userDefineExtMapper.getRMNO(pars);
            String str = pars.toString();
            int begin =str.indexOf("=")+1;
            int end =str.indexOf(",");
            nextId = Integer.parseInt(str.substring(begin,end));
            rmno = nextId;
            defRmnoctl.setRmnoctlNo(rmno);
            return rmno;
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Integer insertMsgOut(Msgout defMsgOut)throws Exception{
        Integer iRes = 0;
        try {
            iRes = msgoutExtMapper.insertSelective(defMsgOut);
            return iRes;
        }catch (Exception e){
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * 以DefRMOUT為條件，做單純條件的查詢
     * @param defRmout 匯出主檔
     * @return
     * @throws Exception
     */
    public List<Rmout> getRmoutByDef(Rmout defRmout) throws Exception {
        try{
            List<Rmout> dtResult  = rmoutExtMapper.getRmoutByDef(defRmout);
            return dtResult;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Rmin> getRminByDef(Rmin defRmin) throws Exception {
        try{
            List<Rmin> dtResult  = rminExtMapper.getRminByDef(defRmin);
            return dtResult;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateRminByPrimaryKeyWithStat(Rmin defRmin,String stat) throws Exception {
        try{
            return rminExtMapper.updateByPrimaryKeyWithStat(defRmin,stat);
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateRmintByPrimaryKeyWithStat(Rmint defRmint,String stat) throws Exception {
        try{
            return rmintExtMapper.updateByPrimaryKeyWithStat(defRmint,stat);
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }


    public Rmin getRMINbyPK(Rmin defRmin) throws Exception {
        try{
            return rminExtMapper.selectByPrimaryKey(defRmin.getRminTxdate(),defRmin.getRminBrno(),defRmin.getRminFepno());
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public PageInfo<HashMap<String,Object>> getRMOUTSNO(String bkno,String bank,Boolean bool,Integer pageNum,Integer pageSize) throws Exception {
        try {
//            List<Rmoutsno> dt = null;
            PageInfo<HashMap<String,Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    rmoutsnoExtMapper.getRMOUTSNOByPK(bkno,bank,bool);
                }
            });
            return pageInfo;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Rmoute> getRMOUTEDataTableByPK(Rmoute defRmoute) throws Exception {
        try{
            List<Rmoute> dtResult  = rmouteExtMapper.getDataTableByPrimaryKey(defRmoute);
            return dtResult;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Rmin> queryRMINForUI028110(Rmin defRmin) throws Exception {
        try{
            List<Rmin> dtResult  = rminExtMapper.queryRMINForUI028110(defRmin);
            return dtResult;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Rmin queryRminByPrimaryKeyWithUpdLock(Rmin defRmin) throws Exception {
        try{
            return rminExtMapper.selectByPrimaryKey(defRmin.getRminTxdate(),defRmin.getRminBrno(),defRmin.getRminFepno());
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public PageInfo<Msgin> getMsgInByDef(Msgin defMsgin,int pageNum , int pageSize) throws Exception {
        PageInfo<Msgin> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                    msginExtMapper.getMsgInByDef(defMsgin);
            }
        });
        return pageInfo;
    }

    public List<RmoutExt> getRMOUTUnionRMOUTEByDef(Rmout defRmout,Boolean isOrderByFISCSNO) throws Exception {
        try{
            List<RmoutExt> dtResult  = rmoutExtMapper.getRMOUTUnionRMOUTEByDef(defRmout,isOrderByFISCSNO);
            return dtResult;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * 以DefMSGOUT為條件，做單純條件的查詢
     * @param oMsgout 一般通訊匯出檔
     * @return
     * @throws Exception
     */
    public PageInfo<Msgout> getMsgOutByDef(Msgout oMsgout ,int pageNum,int pageSize) throws Exception {
        PageInfo<Msgout> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                try {
                    msgoutExtMapper.getMsgOutByDef(oMsgout);
                } catch (Exception ex) {
                	getLogContext().setProgramException(ex);
                    sendEMS(ex);
                }
            }
        });
        return pageInfo;
    }

    public Feptxn getFeptxnByStan(Feptxn feptxn) throws Exception {
        FeptxnExt feptxnExt = new FeptxnExt();
        feptxnExt.setTableNameSuffix(feptxn.getFeptxnTxDate().substring(6, 8));
        try {
            return feptxnExtMapper.getFeptxnByStanbkno(feptxnExt.getTableNameSuffix(),feptxn.getFeptxnTxDate(),feptxn.getFeptxnBkno(),feptxn.getFeptxnStan());
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * Access CURCD
     * 取得所有CURCD_FISC_CUR資料。 透過SAFE DAL處理　
     * xy add
     * @return
     */
    public List<HashMap<String,String>> getAllFISCCurcd() throws Exception {
        try {
            List<HashMap<String,String>> dt = curcdExtMapper.queryAllCurcdAlpha3("CURCD_FISC_CUR");
            List<HashMap<String,String>> list = new ArrayList<>();
            for (int i = 0; i < dt.size(); i++) {
                HashMap map = new HashMap();
                if (dt.get(i).get("CURCD_FISC_CUR")!=null && dt.get(i).get("CURCD_FISC_CUR")!=""){
                    map.put("CURCD_ALPHA3",dt.get(i).get("CURCD_ALPHA3"));
                    map.put("CURCD_FISC_CUR",dt.get(i).get("CURCD_FISC_CUR"));
                    map.put("Output", StringUtils.join(dt.get(i).get("CURCD_FISC_CUR"),"-",dt.get(i).get("CURCD_ALPHA3")));
                    list.add(map);
                }
            }
            return list;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * UI_028150統計匯出狀況筆數
     */
    public List<HashMap<String,Object>> getRMOUTSummaryCnt() throws Exception {
        List<HashMap<String,Object>> dt = null;
        try {
            String rmoutTxdate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            String code = NormalRC.FISC_OK;
            dt = rmoutExtMapper.getRmoutSummaryCnt(rmoutTxdate,code);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
        return dt;
    }

    /**
     * UI_028150統計被退匯狀況筆數
     */
    public List<HashMap<String,Object>> get1172SummaryCnt() throws Exception{
        List<HashMap<String,Object>> dt = null;
        try {
            String txdate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            String sndCode = RMPCode.PCode1172;
            String stat = RMOUTStatus.BackExchange;
            String rtnCode = NormalRC.FISC_OK;
            dt = rmoutExtMapper.get1172SummaryCnt(txdate,sndCode,stat,rtnCode);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
        return dt;
    }
    /**
     * UI_028150統計被自動退匯狀況筆數
     */
    public List<HashMap<String,Object>> getAutoBackSummaryCnt() throws Exception{
        List<HashMap<String,Object>> dt = null;
        try {
            String rmoutTxdate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            String sndCode = RMPCode.PCode1172;
            String stat = RMOUTStatus.BackExchange;
            String rtnCode = NormalRC.FISC_OK;
            dt = rmoutExtMapper.getAutoBackSummaryCnt(rmoutTxdate,sndCode,stat,rtnCode);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
        return dt;
    }
    /**
     * UI_028150查詢被自動退匯的行庫
     */
    public List<HashMap<String,Object>> getAutoBackBank() throws Exception{
        List<HashMap<String,Object>> dt = null;
        try {
            dt = rmoutExtMapper.getAutoBackBank();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
        return dt;
    }
    /**
     * 以系統日讀取最後幾筆放行時間的匯款主檔交易記錄
     */
    public List<HashMap<String,Object>> getTopRMOUTByApdateSenderBank(Integer topNumber, String sendDate, String senderBank) throws Exception{
        List<HashMap<String,Object>> rtnResult = null;
        try {
            rtnResult = rmoutExtMapper.getTopRMOUTByApdateSenderBank(topNumber,sendDate,senderBank);
            return rtnResult;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public List<HashMap<String,Object>> getTopRMOUTByApdateSenderBank1(Integer topNumber, String sendDate, String senderBank) throws Exception{
        List<HashMap<String,Object>> rtnResult = null;
        try {
            rtnResult = rmoutExtMapper.getTopRMOUTByApdateSenderBank1(topNumber,sendDate,senderBank);
            return rtnResult;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public List<HashMap<String,Object>> getTopRMINByTxdateReciveBank(Integer topNumber, String sendDate, String senderBank) throws Exception{
        List<HashMap<String,Object>> rtnResult = null;
        try {
            rtnResult = rminExtMapper.getTopRMINByTxdateReciveBank(topNumber,sendDate,senderBank);
            return rtnResult;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Rmstat getRmstatByPk(Rmstat defRmstat) throws Exception {
        try {
            return rmstatExtMapper.queryByPrimaryKey(defRmstat);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer getALLBANKbyPK(Allbank defAllBank) throws Exception {
        Integer result = 0;
        try {
            if (allbankExtMapper.queryByPrimaryKey(defAllBank) != null) {
                result = 1;
            }
            return result;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Allbank> getALLBANKbyPKOne(Allbank defAllBank) throws Exception {
        try {
            List<Allbank> dt = allbankExtMapper.queryByPrimaryKey(defAllBank);
            return dt;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Allbank> getALLBANKDataTableByPK(Allbank defAllBank) throws Exception {
        try {
            return allbankExtMapper.getDataTableByPrimaryKey(defAllBank);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    /**
     * 取得系統狀態
     */
    public List<Sysstat> getStatus() throws Exception {
        try {
            List<Sysstat> dt = sysstatExtMapper.selectAll();
            return dt;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * 查詢待解筆數與金額
     */
    public HashMap<String,Object> getCntAmt() throws Exception {

        try {
            String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            HashMap<String,Object> clrtotal = clrtotalExtMapper.getCntAmt(date);
            return clrtotal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * UI_028150 查詢T24 Pending筆數
     */
    public List<HashMap<String,Object>> getT24Pending() throws Exception {
        try {
            List<HashMap<String,Object>> dbRmmon = rmmonExtMapper.getT24Pending();
            return dbRmmon;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    /**
     * 查詢全行之匯出未解筆數金額
     */
    public Integer getRMOUTTTotalCntByStat(String stat){
        String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
        try {
            return rmouttExtMapper.getTotalCntByStat(date,stat);
        } catch (Exception ex) {
            return -1;
        }
    }
    /**
     * 查詢全行之匯出未解筆數金額
     */
    public Integer getRMINTotalCntByStat(String stat){
        String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
        try {
            return rminExtMapper.getTotalCntByStat(date,stat);
        } catch (Exception ex) {
            return -1;
        }
    }

    public List<HashMap<String, Object>> getRMFISCOUT1ByPK028060280150(Rmfiscout1 rmfiscout1) throws Exception {
        try {
            return rmfiscout1ExtMapper.queryByPrimaryKey(rmfiscout1);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<HashMap<String, Object>> getRMFISCOUT4ByPK028060280150(Rmfiscout4 rmfiscout4) throws Exception {
        try {
            return rmfiscout4ExtMapper.queryByPrimaryKey(rmfiscout4);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<HashMap<String, Object>> getRMFISCIN1ByPK028060280150(Rmfiscin1 rmfiscin1) throws Exception {
        try {
            return rmfiscin1ExtMapper.queryByPrimaryKey(rmfiscin1);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<HashMap<String, Object>> getRMFISCIN4ByPK028060280150(Rmfiscin4 rmfiscin4) throws Exception {
        try {
            return rmfiscin4ExtMapper.queryByPrimaryKey(rmfiscin4);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Prgstat getPRGSTATByPK(Prgstat prgstat) throws Exception {
        try {
            return prgstatMapper.selectByPrimaryKey(prgstat.getPrgstatProgramid());
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Msgout> getMSGOUTBySTAT(String stat) throws Exception {
        Msgout defMsgout = new Msgout();
        try {
            defMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defMsgout.setMsgoutStat(stat);
            List<Msgout> tmp = msgoutExtMapper.getMsgOutByDef(defMsgout);
            return tmp;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Rmoutt> getRMOUTTbyDef(Rmoutt defRmoutt) throws Exception {
        try {
            defRmoutt.setRmouttFepno(null);
            List<Rmoutt> dtResult =rmouttExtMapper.getRMOUTTByDef(defRmoutt);
            return dtResult;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateRMFISCOUT1ByPK(Rmfiscout1 rmfiscout1) throws Exception {
        try {
            return rmfiscout1ExtMapper.updateByPrimaryKeySelective(rmfiscout1);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Rmoutsno> getRMOUTSNOByNONotEqualREPNO() throws Exception {
        try {
            String txDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            List<Rmoutsno> dtResult = rmoutsnoExtMapper.getRMOUTSNOByNONotEqualREPNO(txDate);
            return dtResult;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Integer updateRMOUTSNOByPK(Rmoutsno rmoutsno) throws Exception {
        try {
            return rmoutsnoExtMapper.updateByPrimaryKeySelective(rmoutsno) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Integer updatePRGSTATByPK(Prgstat prgstat) throws Exception {
        try {
            return prgstatMapper.updateByPrimaryKeySelective(prgstat) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateRMSTATbyPK(Rmstat rmstat) throws Exception {
        try {
            return rmstatExtMapper.updateByPrimaryKeySelective(rmstat) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public void logMessage(LogData log, Level level) {
        logMessage(level, log);
    }


    public List<Allbank> queryALLBANKByPKLike(Allbank defAllBank) {
        List<Allbank> dt = null;
        try {
            dt = allbankExtMapper.getALLBANKByPKLike(defAllBank);
            return dt;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
        }
		return dt;
    }

    public List<Rmoutsno> getRMOUTSNOByDef(Rmoutsno defRMOUTSNO){
        return rmoutsnoExtMapper.getRMOUTSNOByDef(defRMOUTSNO);
    }

    public int updateRMINSNOByPK(Rminsno defRMINSNO){
        return rminsnoExtMapper.updateByPrimaryKeySelective(defRMINSNO);
    }

    public int getRMINSNOByPK(Rminsno defRMINSNO){
        return rminsnoExtMapper.selectByPrimaryKey(defRMINSNO.getRminsnoSenderBank(),defRMINSNO.getRminsnoReceiverBank()) == null ? 1 :0;
    }

    public List<Rminsno> getRMINSNOByPKOne(Rminsno defRMINSNO){
        List<Rminsno> dt = rminsnoExtMapper.selectByPrimaryKeyOne(defRMINSNO.getRminsnoSenderBank(),defRMINSNO.getRminsnoReceiverBank());
        return  dt;
    }

    public Rmoutsno getRMOUTSNObyPK(Rmoutsno defRMOUTSNO){
        return rmoutsnoExtMapper.selectByPrimaryKey(defRMOUTSNO.getRmoutsnoSenderBank(),defRMOUTSNO.getRmoutsnoReceiverBank());
    }

    public FEPReturnCode addMSGOUTandUpdateRMOUTSNO(Msgout defMSGOUT, Rmoutsno defRMOUTSNO){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //由於要包Transaction必須一起做

            if (rmoutsnoExtMapper.updateByPrimaryKeySelective(defRMOUTSNO) != 1) {
                transactionManager.rollback(txStatus);
                rtnCode = IOReturnCode.RMOUTSNOUPDATEOERROR;
                return rtnCode;
            }

            if (msgoutExtMapper.insertSelective(defMSGOUT) != 1) {
                transactionManager.rollback(txStatus);
                rtnCode = IOReturnCode.MSGOUTINSERTTERROR;
                return rtnCode;
            }

            transactionManager.commit(txStatus);
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            throw ex;
        }
        return rtnCode;

    }

    public int updateRMFISCIN1ByPK(Rmfiscin1 defRMFISCIN1){
        return rmfiscin1ExtMapper.updateByPrimaryKeySelective(defRMFISCIN1);
    }

    public int updateRMFISCOUT4ByPK(Rmfiscout4 defRMFISCOUT4){
        return rmfiscout4ExtMapper.updateByPrimaryKeySelective(defRMFISCOUT4);
    }

    public int updateRMFISCIN4ByPK(Rmfiscin4 defRMFISCIN4){
        return rmfiscin4ExtMapper.updateByPrimaryKeySelective(defRMFISCIN4);
    }

    /**
     * query ALLBANK by ALLBANK_PKEY　
     */
    public List<Allbank> getALLBANKByPKLike(Allbank allbank) throws Exception {
        List<Allbank> dt = null;
        try {
            dt = allbankExtMapper.getALLBANKByPKRm(allbank);
            return dt;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public List<Allbank> getALLBANKByAddressLike(String address) throws Exception {
        try {
            List<Allbank> allbankList = allbankExtMapper.getALLBANKByAddressLike(address);
            return allbankList;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<String> getCountyList() throws Exception {
        try {
            List<String> countyList = allbankExtMapper.getCountyList();
            return countyList;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateALLBANKByAddressLike(String flag,String dBflag,String address) throws Exception {
        Integer rtn = -1;
        try {
            rtn = allbankExtMapper.updateALLBANKByAddressLike(flag,dBflag,address);
            return rtn;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<Allbank> getALLBANKByPKRmPageINfo(Integer pageNum, Integer pageSize, Allbank defAllbank) throws Exception {
        pageNum = pageNum == null ? 0 : pageNum;
        pageSize = pageSize == null ? 0 : pageSize;
        try {
            PageInfo<Allbank> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                     allbankExtMapper.getALLBANKByPKRm(defAllbank);
                }
            });
            return pageInfo;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Rmfiscin1 getRMFISCIN1ByPK(Rmfiscin1 defRMFISCIN1){
        return rmfiscin1ExtMapper.selectByPrimaryKey(defRMFISCIN1.getRmfiscin1SenderBank(),defRMFISCIN1.getRmfiscin1ReceiverBank());
    }

    public Rmfiscout1 getRMFISCOUT1ByPK(Rmfiscout1 rmfiscout1){
        return rmfiscout1ExtMapper.selectByPrimaryKey(rmfiscout1.getRmfiscout1SenderBank(),rmfiscout1.getRmfiscout1ReceiverBank());
    }

    public Rmfiscout4 getRMFISCOUT4ByPK(Rmfiscout4 defRMFISCOUT4){
        return rmfiscout4ExtMapper.selectByPrimaryKey(defRMFISCOUT4.getRmfiscout4SenderBank(),defRMFISCOUT4.getRmfiscout4ReceiverBank());
    }

    public Rmfiscin4 getRMFISCIN4ByPK(Rmfiscin4 defRMFISCIN4){
        return rmfiscin4ExtMapper.selectByPrimaryKey(defRMFISCIN4.getRmfiscin4SenderBank(),defRMFISCIN4.getRmfiscin4ReceiverBank());
    }

    public Rmstat getRmstatQueryByPrimaryKey(Rmstat rmstat){
        return rmstatExtMapper.selectByPrimaryKey(rmstat.getRmstatHbkno());
    }

    public Rmoutsno getRmoutsnoQueryByPrimaryKey(Rmoutsno rmoutsno){
        return rmoutsnoExtMapper.selectByPrimaryKey(rmoutsno.getRmoutsnoSenderBank(),rmoutsno.getRmoutsnoReceiverBank());
    }

    public int updateByPrimaryKeyRmoutsno(Rmoutsno rmoutsno){
        return rmoutsnoExtMapper.updateByPrimaryKeySelective(rmoutsno);
    }

    public Rminsno getRminsnoQueryByPrimaryKey(Rminsno rminsno){
        return rminsnoExtMapper.selectByPrimaryKey(rminsno.getRminsnoSenderBank(),rminsno.getRminsnoReceiverBank());
    }

    public Rmin getRminQueryByPrimaryKey(Rmin rmin){
        return rminExtMapper.selectByPrimaryKey(rmin.getRminTxdate(),rmin.getRminBrno(),rmin.getRminFepno());
    }

    public Rmint getRmintQueryByPrimaryKey(Rmint rmint){
        return rmintExtMapper.selectByPrimaryKey(rmint.getRmintTxdate(),rmint.getRmintBrno(),rmint.getRmintFepno());
    }

    public int updateByPrimaryKeyRminsno(Rminsno rminsno){
        return rminsnoExtMapper.updateByPrimaryKeySelective(rminsno);
    }
    public int updateByPrimaryKeyRmin(Rmin rmin){
        return rminExtMapper.updateByPrimaryKeySelective(rmin);
    }
    public Integer updateALLBANK(Allbank defAllbank) throws Exception {
        Integer iRes = 0;
        try {
            iRes = allbankExtMapper.updateByPrimaryKeySelective(defAllbank);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     * wj add UI028120 引用
     *
     * @throws Exception
     */
    public PageInfo<HashMap<String, Object>> getMSGINUnionMSGOUT(String txdate, String bkno, String fiscsno,
                                                                 String outENGMEMO, String inENGMEMO, int pageNum, int pageSize) throws Exception {
        LogData logContext = new LogData();
        Msgin defMSGIN = new Msgin();
        Msgout defMSGOUT = new Msgout();
        try {
            if (StringUtils.isNotBlank(bkno)) {
                if (StringUtils.isNotBlank(fiscsno) && Integer.parseInt(fiscsno) != 0) {
                    defMSGIN.setMsginFiscsno(fiscsno);
                    defMSGOUT.setMsgoutFiscsno(fiscsno);
                }
                defMSGIN.setMsginSenderBank(bkno);
                defMSGIN.setMsginTxdate(txdate);
                defMSGIN.setMsginEngmemo(inENGMEMO);
                defMSGOUT.setMsgoutReceiverBank(bkno);
                defMSGOUT.setMsgoutTxdate(txdate);
                defMSGOUT.setMsgoutEngmemo(outENGMEMO);
            } else {
                defMSGIN.setMsginTxdate(txdate);
                defMSGIN.setMsginEngmemo(inENGMEMO);
                defMSGOUT.setMsgoutTxdate(txdate);
                defMSGOUT.setMsgoutEngmemo(outENGMEMO);
            }
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            msginExtMapper.getMSGINDtByDef(defMSGIN, defMSGOUT);
                        }
                    });
            return pageInfo;
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Integer deleteALLBANK(Allbank defAllbank) throws Exception {
        Integer iRes = 0;
        try {
            iRes = allbankExtMapper.deleteByPrimaryKey(defAllbank);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer insertRmoutsno(Rmoutsno defRmoutsno) throws Exception {
        Integer iRes = 0;
        try {
            iRes = rmoutsnoExtMapper.insertSelective(defRmoutsno);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Integer insertRminsno(Rminsno defRminsno) throws Exception {
        Integer iRes = 0;
        try {
            iRes = rminsnoExtMapper.insertSelective(defRminsno);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public int updateRMINAndRMINTByPK(Rmin rmin, Rmint rmint) throws Exception {
        int iResult = 0;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {


            iResult = rminExtMapper.updateByPrimaryKeySelective(rmin);

            if (iResult == 1) {
                iResult = rmintExtMapper.updateByPrimaryKeySelective(rmint);
                if (iResult != 1) {
                    transactionManager.rollback(txStatus);
                    return iResult;
                }
            } else {
                transactionManager.rollback(txStatus);
                return iResult;
            }

            transactionManager.commit(txStatus);
            return iResult;
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            throw new Exception(getInnerMessage(ex));
        }

    }

    /**
     Query RMIN for 補送

     <history>
     <modify>
     <modifier>Jim</modifier>
     <reason>For UI028090</reason>
     <date>2010/3/17</date>
     </modify>
     </history>
     */
    public int getRMINForResend(RefBase<Rmin> rminRefBase) throws Exception {

        try {
            int i = 0;
            List<Rmin> rmins = rminExtMapper.getRMINForResend(rminRefBase.get());

            if(rmins.size() == 1){
                i = 1;
                rminRefBase.set(rmins.get(0));
            }else{
                i = rmins.size();
            }
            return i;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Rmout getRMOUTbyPK(Rmout defRMOUT){
        return rmoutExtMapper.selectByPrimaryKey(defRMOUT.getRmoutTxdate(),defRMOUT.getRmoutBrno(),defRMOUT.getRmoutOriginal(),defRMOUT.getRmoutFepno());
    }

    public int updateRMOUTbyPK(Rmout defRMOUT){
        return rmoutExtMapper.updateByPrimaryKeySelective(defRMOUT);
    }

    public int updateRMOUTTbyPK(Rmoutt rmoutt){
        return rmouttExtMapper.updateByPrimaryKeySelective(rmoutt);
    }

    public List<Rmout> getRMOUTbyTXAMT(String txAmt){
        String txDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return rmoutExtMapper.getRMOUTbyTXAMT(txDate,txAmt);
    }

    public List<Rmout> getRMOUTbyReceiverBANK(String receiverBANK){
        String txDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return rmoutExtMapper.getRMOUTbyReceiverBANK(txDate,receiverBANK);
    }

    public int updateRMOUTByDef(String owpriority,Rmout rmout){
        return rmoutExtMapper.updateRMOUTByDef(owpriority,rmout);
    }

    public List<Rmoutt> getRMOUTTbyTXAMT(String txAmt){
        String txDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return rmouttExtMapper.getRMOUTTbyTXAMT(txDate,txAmt);
    }

    public List<Rmoutt> getRMOUTTbyReceiverBANK(String receiverBANK){
        String txDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return rmouttExtMapper.getRMOUTTbyReceiverBANK(txDate,receiverBANK);
    }

    public int updateRMOUTTByDef(String owpriority,Rmoutt rmoutt){
        return rmouttExtMapper.updateRMOUTTByDef(owpriority,rmoutt);
    }

    public int updateRMOUTTByDefSelective(Rmoutt rmoutt){
        return rmouttExtMapper.updateByPrimaryKeySelective(rmoutt);
    }
    public int updateRMBTCHByDef(Rmbtch rmbtch){
        return rmbtchExtMapper.updateByPrimaryKeySelective(rmbtch);
    }

    public Integer insertAllBank(Allbank allbank) throws Exception {
        Integer iRes = 0;
        try {
            iRes = allbankExtMapper.insertSelective(allbank);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<HashMap<String, Object>> getRMOUTTSumByStatGroupByBrno(String[] stats, int pageNum, int pageSize){
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        rmouttExtMapper.getSumByStatGroupByBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),stats);
                    }
                });
        return pageInfo;
    }

    public PageInfo<HashMap<String, Object>> getRMINSumByStatGroupByBrno(String stat, int pageNum, int pageSize){
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        rminExtMapper.getSumByStatGroupByBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),stat);
                    }
                });
        return pageInfo;
    }

    public PageInfo<HashMap<String, Object>> getRMOUTTByTxdateBrno(String txdate,String brno, int pageNum, int pageSize){
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        rmouttExtMapper.getRMOUTTByTxdateBrno(txdate, brno);
                    }
                });
        return pageInfo;
    }

    public PageInfo<HashMap<String, Object>> getRMINTByTxdateBrno(String txdate,String brno, int pageNum, int pageSize){
        PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        rmintExtMapper.getRMINTByTxdateBrno(txdate, brno);
                    }
                });
        return pageInfo;
    }

    public Rmout getSingleRMOUT(Rmout rmout) throws Exception {
        try {
            return rmoutExtMapper.getSingleRMOUT(rmout);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Integer updateALLBANKByBKNO(Allbank allbank) throws Exception {
        Integer iRes = 0;
        try {
            if (allbank.getAllbankBkno()==null){
                return -1;
            }
            iRes = allbankExtMapper.updateALLBANKByBKNO(allbank);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public Integer deleteALLBANKByExceptBKNO(String bkno) throws Exception {
        try {
            return allbankExtMapper.deleteALLBANKByExceptBKNO(bkno) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    /**
     *查詢ALLBANK資料筆數
     */
    public Integer getALLBANKCnt() throws Exception {
        try {
            return allbankExtMapper.getALLBANKCnt() ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    /**
     *查詢ALLBANK參加財金證券匯款總筆數
     */
    public Integer getFISCAndBANKCnt() throws Exception {
        try {
            return allbankExtMapper.getFISCAndBANKCnt() ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public Integer updateMSGOUTByPK(Msgout msgout) throws Exception {
        try {
            return msgoutExtMapper.updateByPrimaryKeySelective(msgout) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<HashMap<String,Object>> getRMBTCHbyDef(Rmbtch defRMBTCH, int pageNum, int pageSize)
            throws Exception {
//        LogData logContext = new LogData();
        try {

            PageInfo<HashMap<String,Object>> pageInfo = PageHelper
                    .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            rmbtchExtMapper.getRMBTCHByDef(defRMBTCH);
                        }
                    });
            return pageInfo;
        } catch (Exception e) {
//            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<HashMap<String,Object>> getRMBTCHbyDef_UI028200(Rmbtch defRMBTCH) throws Exception {
//        LogData logContext = new LogData();
        try {
            return rmbtchExtMapper.getRMBTCHByDef(defRMBTCH);
        } catch (Exception e) {
//            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }
    /**
     * wj add UI028220 引用
     *
     * @throws Exception
     */
    public PageInfo<HashMap<String, Object>> getRMBTCHMTRbyDef(Rmbtchmtr defRMBTCHMTR, int pageNum, int pageSize)
            throws Exception {
//        LogData logContext = new LogData();
        try {

            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                        @Override
                        public void doSelect() {
                            rmbtchmtrExtMapper.getRMBTCHMTRByDef(defRMBTCHMTR);
                        }
                    });
            return pageInfo;
        } catch (Exception e) {
//            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * wj add UI028220_Detail 引用
     *
     * @throws Exception
     */
    public List<HashMap<String, Object>> getAllBatch(String batchName ) throws Exception {
        try {
            return batchExtMapper.getAllBatch(batchName, null);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    
    /**
	 * wj add UI028230 引用
	 *
	 * @throws Exception
	 */
	public PageInfo<HashMap<String, Object>> getRMOUTForUI028230(String txDate, int pageNum, int pageSize)
			throws Exception {
//		LogData logContext = new LogData();
		try {

			PageInfo<HashMap<String, Object>> pageInfo = PageHelper
					.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
						@Override
						public void doSelect() {
							rmoutExtMapper.getRMOUTForUI028230(txDate);
						}
					});
			return pageInfo;
		} catch (Exception e) {
//			logContext.setProgramException(e);
			sendEMS(e);
			throw ExceptionUtil.createException(e, this.getInnerMessage(e));
		}
	}
    public Integer updateCLRDTLByPK(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.updateByPrimaryKeySelective(clrdtl) ;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public List<Clrdtl> getCLRDTLByPK(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.queryByPrimaryKey(clrdtl);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<HashMap<String, Object>> getAMLReSendData(String txDate, int pageNum, int pageSize) throws Exception {
        try {
            return  PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    try {
                        rminExtMapper.getAMLReSendData(txDate);
                    } catch (Exception e) {
                    	getLogContext().setProgramException(e);
                        sendEMS(e);
                    }
                }
            });
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
    public List<HashMap<String, Object>> getAMLReSendData(String txDate){
        try {
            return rminExtMapper.getAMLReSendData(txDate);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            return null;
        }
    }
    public PageInfo<HashMap<String, Object>> getRMINByDateSendbankPendingEJ(String txdate,String senderBank,String stat,String ej, int pageNum, int pageSize) throws Exception {
        try {
            return  PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                        rminExtMapper.getRMINByDateSendbankPendingEJ(txdate,senderBank,stat,ej);
                }
            });
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<HashMap<String, Object>> getMSGINByDateSendbankFISCRtnCodeEJ(String txdate,String senderBank,String fiscRtnCode,String ej, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                        msginExtMapper.getMSGINByDateSendbankFISCRtnCodeEJ(txdate,senderBank,fiscRtnCode,ej);
                }
            });
            return pageInfo;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }

    public PageInfo<HashMap<String, Object>> getRMINUnionMSGIN(String txdate,String senderBank,String stat,String fiscRtnCode,String ej, int pageNum, int pageSize) throws Exception {

        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper
                    .startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                        rminExtMapper.getRMINUnionMSGIN(txdate,senderBank,stat,fiscRtnCode,ej);
//                        getRMINByDateSendbankPendingEJ(txdate,senderBank,stat,ej,pageNum,pageSize);
//                        getMSGINByDateSendbankFISCRtnCodeEJ(txdate,senderBank,fiscRtnCode,ej,pageNum,pageSize);
//                        rmService.getRMINUnionMSGIN(finalInputDate2,form.getSenderBank(),RMINStatus.Transferring,"0001",form.getEjfno());
                }
            });
            return pageInfo;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            throw new Exception(getInnerMessage(ex));
        }
    }
}
