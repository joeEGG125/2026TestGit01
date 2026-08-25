package com.syscom.fep.web.service;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.ext.model.FeptxnMsgRsExt;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.aa.inbk.SendConfirmByManual;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.server.common.cbsprocess.CBTXEND;
import com.syscom.fep.server.common.cbsprocess.IBXII002;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InbkService extends BaseService {
    private static final String ProgramName = InbkService.class.getSimpleName();

    @Autowired
    private AllbankExtMapper allbankExtMapper;
    @Autowired
    private BsdaysMapper bsdaysMapper;
    @Autowired
    private BsdaysExtMapper bsdaysExtMapper;
    @Autowired
    private FwdtxnExtMapper fwdtxnExtMapper;
    @Autowired
    private SeqnoMapper seqnoMapper;
    @Autowired
    private FundlogExtMapper fundlogExtMapper;
    @Autowired
    private ClrdtlExtMapper clrdtlExtMapper;
    @Autowired
    private BatchExtMapper batchExtMapper;
    @Autowired
    private AtmmstrMapper atmmstrMapper;
    @Autowired
    private FwdrstMapper fwdrstMapper;
    @Autowired
    private ClrtotalMapper clrtotalMapper;
    @Autowired
    private FeptxnExtMapper feptxnExtMapper;
    @Autowired
    private AptotExtMapper aptotExtMapper;
    @Autowired
    private IctltxnExtMapper ictltxnExtMapper;
    @Autowired
    private BrapExtMapper brapExtMapper;
    @Autowired
    private HkbrapExtMapper hkbrapExtMapper;
    @Autowired
    private MobrapExtMapper mobrapExtMapper;
    @Autowired
    private ZoneExtMapper zoneExtMapper;
    @Autowired
    private FcrmstatExtMapper fcrmstatExtMapper;
    @Autowired
    private NpsbatchExtMapper npsbatchExtMapper;
    @Autowired
    private NpsdtlExtMapper npsdtlExtMapper;
    @Autowired
    private ApibatchExtMapper apibatchExtMapper;
    @Autowired
    private ApidtlExtMapper apidtlExtMapper;
    @Autowired
    private FeptxnDao feptxnDao;
    @Autowired
    private SysstatExtMapper sysstatExtMapper;
    @Autowired
    private InbkparmExtMapper inbkparmExtMapper;
    @Autowired
    private BinExtMapper binExtMapper;
    @Autowired
    private BinMapper binMapper;
    @Autowired
    private InbkparmMapper inbkparmMapper;
    @Autowired
    private CbspendExtMapper cbspendExtMapper;

    @Autowired
    private ObtltxnExtMapper obtltxnExtMapper;
    @Autowired
    private FeptxntcbMapper feptxntcbMapper;

    @Override
    protected SubSystem getSubSystem() {
        return SubSystem.INBK;
    }

    public BigDecimal getCbspendSummary(String cbspendTxDate, Short cbspendSuccessFlag, Short cbspendSubsys, String cbspendZone,
                                        String cbspendCbsTxCode) throws Exception {
        try {
            return cbspendExtMapper.getsumOfTxAMT(cbspendTxDate, cbspendSuccessFlag, cbspendSubsys, cbspendZone, cbspendCbsTxCode);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int UpdateCBSPEND(Cbspend cbspend) throws Exception {
        int iRes = 0;
        try {
            iRes = cbspendExtMapper.updateByPrimaryKey(cbspend);
            return iRes;
        } catch (Exception e) {
            sendEMS(e);
            return iRes;
        }
    }

    public int UpdateResendCNT(String txdate, BigDecimal ejfno, String zone, String tbsdy, String subsys) throws Exception {
        try {
            int iRes = 0;
            iRes = cbspendExtMapper.UpdateResendCNT(txdate, ejfno, zone, tbsdy, subsys);
            return iRes;
        } catch (Exception e) {
            sendEMS(e);
            return 0;
        }
    }

    public List<Map<String, Object>> SelectResendCNT(String txdate, String zone, String tbsdy, String subsys)
            throws Exception {
        try {
            return cbspendExtMapper.QueryResendCNT(txdate, zone, tbsdy, subsys);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Map<String, Object>> GetCBSPENDByTXDATE(String cbspendTxDate, Short cbspendSuccessFlag, Short cbspendSubsys, String cbspendZone,
                                                        String cbspendCbsTxCode) {
    	return cbspendExtMapper.getCBSPENDByTXDATEAndZone(cbspendTxDate, cbspendSuccessFlag, cbspendSubsys, cbspendZone, cbspendCbsTxCode);
    }

    public List<Inbkparm> getINBKPARMByPK(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
                                          BigDecimal INBKPARM_RANGE_FROM) {

        Inbkparm definbkparm = new Inbkparm();
        definbkparm.setInbkparmApid(APID);
        definbkparm.setInbkparmAcqFlag(a);
        definbkparm.setInbkparmCur(INBKPARM_CUR);
        if (StringUtils.isNotBlank(INBKPARM_EFFECT_DATE)) {
            INBKPARM_EFFECT_DATE = StringUtils.replace(INBKPARM_EFFECT_DATE, "-", StringUtils.EMPTY);
            definbkparm.setInbkparmEffectDate(INBKPARM_EFFECT_DATE);
        }
        definbkparm.setInbkparmRangeFrom(INBKPARM_RANGE_FROM);

        return inbkparmExtMapper.getINBKPARMByPK(definbkparm);
    }

    public List<Bin> getBinByPK(String BINNO, String BINBKNO) {

        return binExtMapper.getBinByPrimaryKey(BINNO, BINBKNO);
    }

    public List<Inbkparm> getInbkparmAll() {
        return inbkparmExtMapper.queryInbkparmAll();
    }

    public List<Bin> getBinAll() {
        return binExtMapper.queryBinAll();
    }

    public Integer updateINBKPARM(Inbkparm inbkparm) {
        try {
            // 回傳的為TaskId
            return inbkparmExtMapper.updateByPrimaryKey(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer updateBIN(Bin bin) {
        try {
            // 回傳的為TaskId
            return binExtMapper.updateByPrimaryKey(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer insertINBKPARM(Inbkparm inbkparm) {
        try {
            return inbkparmMapper.insertSelective(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer insertBIN(Bin bin) {
        try {
            return binMapper.insertSelective(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Inbkparm getInbkparmByPK(String APID, String a, String INBKPARM_CUR, String INBKPARM_EFFECT_DATE,
                                    BigDecimal INBKPARM_RANGE_FROM, String pcode) {
        return inbkparmMapper.selectByPrimaryKey(APID, pcode, a, INBKPARM_EFFECT_DATE, INBKPARM_CUR, INBKPARM_RANGE_FROM);
    }

    public Bin getBinDataByPK(String binno, String binbkno) {
        return binMapper.selectByPrimaryKey(binno, binbkno);
    }

    public Integer deleteINBKPARM(Inbkparm inbkparm) {
        try {
            // 回傳的為TaskId
            return inbkparmExtMapper.deleteByPrimaryKey(inbkparm);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public Integer deleteBin(String binno, String binbkno, int userid) {
        try {
            BinExt bin = new BinExt();
            bin.setBinNo(binno);
            bin.setBinBkno(binbkno);
            bin.setUpdateUserid(userid);
            // 回傳的為TaskId
            return binExtMapper.deleteByPrimaryKey(bin);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            return 0;
        }
    }

    public int UpdateFCRMSTAT(Fcrmstat fcrmstat) throws Exception {
        int iRes = 0;
        try {
            // 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
            fcrmstat.setLogAuditTrail(true);
            fcrmstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            fcrmstat.setUpdateUser(fcrmstat.getUpdateUserid());
            iRes = fcrmstatExtMapper.updateByPrimaryKeySelective(fcrmstat);
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    /**
     * 更新系統狀態 UI_019010
     * @param sysstat
     * @return
     * @throws Exception
     */
    public int UpdateSYSSTAT(Sysstat sysstat) throws Exception {
        int iRes = 0;
        try {
            // 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
            sysstat.setLogAuditTrail(true);
            sysstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            sysstat.setUpdateUser(sysstat.getUpdateUserid());
            iRes = sysstatExtMapper.updateByHbkno(
                    sysstat.getSysstatHbkno(),
                    sysstat.getSysstatAoct1000(),
                    sysstat.getSysstatAoct1100(),
                    sysstat.getSysstatAoct1200(),
                    sysstat.getSysstatAoct1300(),
                    sysstat.getSysstatAoct1400(),
                    sysstat.getSysstatMbact1000(),
                    sysstat.getSysstatMbact1100(),
                    sysstat.getSysstatMbact1200(),
                    sysstat.getSysstatMbact1300(),
                    sysstat.getSysstatMbact1400(),
                    sysstat.getSysstatMbact2000(),
                    sysstat.getSysstatMbact2200(),
                    sysstat.getSysstatMbact2500(),
                    sysstat.getSysstatMbact2510(),
                    sysstat.getSysstatMbact2520(),
                    sysstat.getSysstatMbact2530(),
                    sysstat.getSysstatMbact2540(),
                    sysstat.getSysstatMbact2550(),
                    sysstat.getSysstatMbact2560(),
                    sysstat.getSysstatMbact2570(),
                    sysstat.getSysstatMbact2700(),
                    sysstat.getSysstatMbact7100(),
                    sysstat.getSysstatMbact7300()
            );
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }
    
    /**
     * 更新 主機系統狀態 UI_060291
     * @param sysstat
     * @return
     * @throws Exception
     */
    public int UpdateSYSSTATForSystemStatus(Sysstat sysstat) throws Exception {
        int iRes = 0;
        try {
            // 參照.NET要求, 以下三行要加入, 因為要記錄audit trail log
            sysstat.setLogAuditTrail(true);
            sysstat.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
            sysstat.setUpdateUser(sysstat.getUpdateUserid());
            iRes = sysstatExtMapper.updateByHbknoForSystemStatus(
                    sysstat.getSysstatHbkno(),
                    sysstat.getSysstatCbs(),
        			sysstat.getSysstatFcs(),
        			sysstat.getSysstatCredit(),
        			sysstat.getSysstatMtp(),
        			sysstat.getSysstatTwmp(),
        			sysstat.getSysstatFido()
            );
            return iRes;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    public int lockFwdtxn(Fwdtxn def) throws Exception {
        try {
            return fwdtxnExtMapper.lockFWDTXN(def);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getFwdtxn(Fwdtxn def, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    fwdtxnExtMapper.getFwdtxn(def);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public HashMap<String, Object> getFailTimes(Fwdtxn def) throws Exception {
        try {
            return fwdtxnExtMapper.getFailTimes(def);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fundlog getFundlogByFgSeqno(String fundlogFgSeqno) throws Exception {
        try {
            return fundlogExtMapper.getFUNDLOGByFGSeqno(fundlogFgSeqno);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.insertSelective(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.updateByPrimaryKeySelective(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int deleteFundlog(Fundlog fundlog) throws Exception {
        try {
            return fundlogExtMapper.deleteByPrimaryKey(fundlog);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateClrdtlByPK(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.updateByPrimaryKeySelective(clrdtl);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertClrdtl(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.insertSelective(clrdtl);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrdtl getClrdtlByPrimaryKey(Clrdtl clrdtl) throws Exception {
        try {
            return clrdtlExtMapper.selectByPrimaryKey(clrdtl.getClrdtlTxdate(), clrdtl.getClrdtlApId(), clrdtl.getClrdtlPaytype());
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public String getSEQNOByPK() throws Exception {
        try {
            String seqnoName = "FGTWD";
            Seqno seqno = seqnoMapper.selectByPrimaryKey(seqnoName);
            if (seqno != null) {
                seqno.setSeqnoName("FGTWD");
                seqno.setSeqnoNextid(seqno.getSeqnoNextid() + 1);
                seqnoMapper.updateByPrimaryKeySelective(seqno);
                return String.valueOf(seqno.getSeqnoNextid() - 1);
            } else {
                seqno = new Seqno();
                seqno.setSeqnoName("FGTWD");
                seqno.setSeqnoNextid(2);
                seqnoMapper.insertSelective(seqno);
                return "1";
            }
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Bsdays getBsdaysByPk(String bsdaysZoneCode, String bsdaysDate) throws Exception {
        try {
            return bsdaysMapper.selectByPrimaryKey(bsdaysZoneCode, bsdaysDate);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Bsdays getBsdaysByNBSDY(String bsdaysNbsdy) throws Exception {
        try {
            return bsdaysExtMapper.getLBsdays(bsdaysNbsdy);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Bsdays> getLatestBsdayBefore(String statLbsdy) throws Exception {
        try {
            return bsdaysExtMapper.getLBsdaysBefore(statLbsdy);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Batch getSingleBATCHByDef(String atchName) throws Exception {
        try {
            return batchExtMapper.getSingleBATCHByDef(atchName);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fwdtxn getFwdtxn(String fwdtxnTxDate, String fwdtxnTxId) throws Exception {
        try {
            return fwdtxnExtMapper.selectByPrimaryKey(fwdtxnTxDate, fwdtxnTxId);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Atmmstr getAtmmstr(String atmNo) throws Exception {
        try {
            return atmmstrMapper.selectByPrimaryKey(atmNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int updateFwdtxnByPrimaryKey(Fwdtxn fwdtxn) throws Exception {
        try {
            return fwdtxnExtMapper.updateByPrimaryKeySelective(fwdtxn);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public int insertFwdrst(Fwdrst fwdrst) throws Exception {
        try {
            return fwdrstMapper.insertSelective(fwdrst);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Fwdrst getFwdrst(String fwdrstTxDate, String fwdrstTxId, Short fwdrstRunNo) throws Exception {
        try {
            return fwdrstMapper.selectByPrimaryKey(fwdrstTxDate, fwdrstTxId, fwdrstRunNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrtotal getClrtotal(String clrtotalStDate, String clrtotalCur, Short clrtotalSource) throws Exception {
        try {
            return clrtotalMapper.selectByPrimaryKey(clrtotalStDate, clrtotalCur, clrtotalSource);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public String getSysstatTbsdy() throws Exception {
        try {
            return sysstatExtMapper.getSysstatTbsdy();
        }
        catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public String getSysstatLbsdy() throws Exception {
        try {
            return sysstatExtMapper.getSysstatLbsdy();
        }
        catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<FeptxnMsgRsExt> getAPIFeptxnByStan(String FeptxnTxDate, String BankNo, String StanNo) throws Exception {
        try {
            return feptxnDao.getAPIFeptxnByStan(FeptxnTxDate, BankNo, StanNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * FEP Web 查詢OPC交易記錄
     *
     * @param feptxn
     * @param nbsday
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Feptxn> getFeptxnByTxDate(FeptxnExt feptxn, String nbsday, Integer pageNum, Integer pageSize) throws Exception {
        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            feptxnDao.setTableNameSuffix(feptxn.getTableNameSuffix(), StringUtils.join(ProgramName, ".getFeptxnByTxDate"));
            return feptxnDao.selectByDatetimeAndPcodesAndBknosAndStansAndEjnos(
                    feptxn.getFeptxnTxDate(),
                    feptxn.getFeptxnPcode(),
                    feptxn.getFeptxnBkno(),
                    feptxn.getFeptxnStan(),
                    feptxn.getFeptxnEjfno(),
                    nbsday, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public void inbkLogMessage(Level level, LogData log) {
        logMessage(level, log);
    }

    /**
     * 查詢請求傳送滯留信息
     *
     * @param way
     * @param sysstatHbkno
     * @param datetime
     * @param stime
     * @param etime
     * @param datetimeo
     * @param bkno
     * @param stan
     * @param trad
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Feptxn> selectByRetention(String way, String sysstatHbkno, String datetime, String stime, String etime,
                                              String datetimeo, String bkno, String stan, String trad, Integer pageNum, Integer pageSize) throws Exception {
        try {
            FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
            return feptxnDao.selectByRetention(way, sysstatHbkno, datetime, stime, etime, datetimeo, bkno, stan, trad, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Inbkpend> getINBKPend2270(String ORI_TBSDY, String TX_DATE, String BKNO, String STAN, String OPCODE,
                                          String OSTAN, String SYSSTATHBKNO, String sqlSortExpression) {
        InbkpendExtMapper inbkpend = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
        return inbkpend.getINBKPend2270(ORI_TBSDY, TX_DATE, BKNO, STAN, OPCODE, OSTAN, SYSSTATHBKNO, sqlSortExpression);
    }

    public List<Map<String, Object>> getINBKPend2270csv(String ORI_TBSDY, String TX_DATE, String BKNO, String STAN, String OPCODE,
                                                        String OSTAN, String SYSSTATHBKNO, String sqlSortExpression) throws Exception {
        try {
            List<Map<String, Object>> dt = new ArrayList<>();
            InbkpendExtMapper inbkpend = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
            dt = inbkpend.getINBKPend2270csv(ORI_TBSDY, TX_DATE, BKNO, STAN, OPCODE, OSTAN, SYSSTATHBKNO, sqlSortExpression);
            return dt;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Feptxn getFeptxnByPk(Feptxn feptxn, String tbsdy) throws Exception {
        try {
            if (StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(String.valueOf(feptxn.getFeptxnEjfno()))) {
                FeptxnDao feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
                feptxnDao.setTableNameSuffix(tbsdy, StringUtils.join(ProgramName, ".getFeptxnByPk"));
                return feptxnDao.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
            } else {
                return null;
            }

        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Feptxn getFEPTXNFor2280(Feptxn feptxn, String sysDatetime, String sysStatHbkno, String tableNameSuffix) throws Exception {
        try {
            if (StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(feptxn.getFeptxnBkno()) && StringUtils.isNotBlank(feptxn.getFeptxnStan())) {
                return feptxnExtMapper.get01FEPTXNFor2280(tableNameSuffix, sysDatetime, feptxn.getFeptxnTxDate(), feptxn.getFeptxnBkno(), sysStatHbkno, feptxn.getFeptxnStan(),
                        feptxn.getFeptxnTbsdyFisc());
            } else {
                return null;
            }

        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public List<Zone> selectAll() throws Exception {
        try {
            return zoneExtMapper.selectAll();
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * xy add
     * UI019201 引用
     *
     * @param aptotStDate
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getAPTOTByStDate(String aptotStDate) throws Exception {
        try {
            return aptotExtMapper.getAPTOTByStDate(aptotStDate);
        } catch (Exception e) {
            LogData logContext = new LogData();
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * xy add
     * UI019201 引用
     *
     * @param aptotStDate
     * @return COUNT SUM(FUNDLOG_FG_AMT)
     * @throws Exception
     */
    public List<HashMap<String, Object>> getFUNDLOGByTxDate(String aptotStDate) throws Exception {
        LogData logContext = new LogData();
        try {
            return fundlogExtMapper.getFUNDLOGSumAmtBytxDate(aptotStDate);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Ictltxn searchIctltxn(Ictltxn tempIctl) throws Exception {
        try {
            return ictltxnExtMapper.getIctltxn(tempIctl);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(ex, this.getInnerMessage(ex));
        }
    }

    /**
     * xy add
     * UI019202 引用
     *
     * @param aptotStDate
     * @param apId
     * @param ascFlag
     * @return
     * @throws Exception
     */
    public List<HashMap<String, Object>> getAPTOTSumAmtByStDateAPIDKind(String aptotStDate,
                                                                        String apId, String ascFlag) throws Exception {
        LogData logContext = new LogData();
        try {
            if (!"*".equals(apId.substring(3, 4))) {} else {
                apId = apId.substring(0, 3);
            }
            return aptotExtMapper.getAPTOTSumAmtByStDateAPIDKind(aptotStDate, apId, ascFlag);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getBrap(String zone, String stDate, String pcode, String apId, String txType,
                                                     String brno, String deptCode, Integer pageNum, Integer pageSize, String brapCur) {
        PageInfo<HashMap<String, Object>> dt;
        switch (zone) {
            case "TWN":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        brapExtMapper.getBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            case "HKG":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        hkbrapExtMapper.getHKBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            case "MAC":
                dt = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                    @Override
                    public void doSelect() {
                        mobrapExtMapper.getMOBRAPBySTDateForUI(stDate, pcode, apId, txType, brno, deptCode, brapCur);
                    }
                });
                break;
            default:
                dt = null;
                break;
        }
        return dt;
    }

    /**
     * 查詢請求傳送交易結果
     *
     * @param datetime
     * @param inbkpendPcode
     * @param pageNum
     * @param pageSize
     * @return
     * @throws Exception
     */
    public PageInfo<Inbkpend> getINBKPendList(String datetime, String inbkpendPcode, Integer pageNum, Integer pageSize) throws Exception {
        try {
            return feptxnDao.getINBKPendList(datetime, inbkpendPcode, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> getFWDTXNByTSBDYFISC(String fwdrstTxDate, String selectValue, String fwdtxnTxId,
                                                                  String channel, String fwdtxnTroutActno, String fwdtxnTrinBkno,
                                                                  String fwdtxnTrinActno, String fwdtxnTxAmt, Short sysFail,
                                                                  Integer pageNum, Integer pageSize) throws Exception {
        try {
            return feptxnDao.getFWDTXNByTSBDYFISC(fwdrstTxDate, selectValue, fwdtxnTxId, channel, fwdtxnTroutActno, fwdtxnTrinBkno, fwdtxnTrinActno, fwdtxnTxAmt, sysFail, pageNum, pageSize);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Inbkpend getInbkpendByBknoStan(Inbkpend inbkpend) throws Exception {
        try {
            InbkpendExtMapper inbkpendExtMapper = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
            return inbkpendExtMapper.getpendingDateStanBkno(inbkpend);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Clrtotal getCLRTOTALByPrimaryKey(String clrtotalStDate, String clrtotalCur, Short clrtotalSource) throws Exception {
        LogData logContext = new LogData();
        try {
            return clrtotalMapper.selectByPrimaryKey(clrtotalStDate, clrtotalCur, clrtotalSource);
        } catch (Exception e) {
            logContext.setProgramException(e);
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public Npsbatch queryNPSBATCHWithOne (String fileid, String txdate,String BatchNo) throws Exception{
        try {
            return npsbatchExtMapper.queryNPSBATCHWithOne(fileid, txdate,BatchNo);
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<Npsbatch> queryNPSBATCH(String fileid, String txdate, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<Npsbatch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    npsbatchExtMapper.queryNPSBATCH(fileid, txdate);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> showDetail(String batno, int pageNum, int pageSize) throws Exception {
        try {
            // 分頁查詢
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    npsdtlExtMapper.showDetail(batno);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<Apibatch> queryApibatch(String beginDate, String endDate, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<Apibatch> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    apibatchExtMapper.queryApibatch(beginDate, endDate);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public PageInfo<HashMap<String, Object>> queryApidtl(String archivesDate, String webType, int pageNum, int pageSize) throws Exception {
        try {
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    apidtlExtMapper.queryApidtl(archivesDate.replace("/", ""), webType);
                }
            });
            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    public HashMap<String, Object> getApibatchTotFee(String archivesDate) throws Exception {
        try {
            return apidtlExtMapper.getApibatchTotFee(archivesDate.replace("/", ""));
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * 查詢FCRMSTAT
     *
     * @return Fcrmstat
     */
    public Fcrmstat getFCRMSTAT() throws Exception {
        Fcrmstat fcrmstat = new Fcrmstat();
        try {
            fcrmstat.setFcrmstatCurrency("001");
            List<Fcrmstat> list = fcrmstatExtMapper.queryByPrimaryKey(fcrmstat);
            if (list.size() > 0) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    /**
     * ALLBANK 相關 資料庫函式
     * 該銀行代號是否存在ALLBANK中
     * 透過SAFE QueryByPrimaryKey
     *
     * @param bankNo
     * @return
     */
    public Boolean checkBankExist(String bankNo) {
        Allbank allbank = new Allbank();
        try {
            allbank.setAllbankBkno(bankNo);
            if (allbankExtMapper.queryAllBankByBkno(allbank).size() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public Sysstat getStatus() throws Exception {
        try {
            return sysstatExtMapper.selectAll().get(0);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(ex);
            throw ExceptionUtil.createException(this.getInnerMessage(ex));
        }
    }

    /**
     * 2022-06-02 Han add
     *
     * @param obtltxnTxDate
     * @param obtltxnEjfno
     * @return Obtltxn
     */
    public Obtltxn getOBTLTXNbyPK(String obtltxnTxDate, Long obtltxnEjfno) {

        return obtltxnExtMapper.getOBTLTXNbyPK(obtltxnTxDate, obtltxnEjfno);
    }

    /**
     * 2022-05-31 Han add
     * HashMap<String, Object>
     *
     * @param txtTroutBkno
     * @param txtTroutActno
     * @param txtTxAMT
     * @param txtOrderNO
     * @param txtMerchantId
     * @param txTransactDate
     * @param txTransactDateE
     * @param txtBkno
     * @param txtStan
     * @return
     */
    public List<Obtltxn> getObtlTxn(String txtTroutBkno, String txtTroutActno, String txtTxAMT,
                                        String txtOrderNO, String txtMerchantId, String txTransactDate, String txTransactDateE, String txtBkno,
                                        String txtStan) {

        return obtltxnExtMapper.getObtlTxn(txtTroutBkno, txtTroutActno, txtTxAMT, txtOrderNO, txtMerchantId,
                txTransactDate, txTransactDateE, txtBkno, txtStan);
    }

    /**
     * @param sysstatHbkno
     * @param sysstatLbsdyFisc
     * @param sysstatTbsdyFisc
     * @param sysstatNbsdyFisc
     * @return
     */
    public int updateSyssTatLTNbsdyFisc(String sysstatHbkno, String sysstatLbsdyFisc, String sysstatTbsdyFisc, String sysstatNbsdyFisc) {
    	return sysstatExtMapper.updateSyssTatLTNbsdyFisc(sysstatHbkno, sysstatLbsdyFisc, sysstatTbsdyFisc, sysstatNbsdyFisc);
    }

    /**
     * UI019510 UI019520 UI019530 查詢2700收款/付款/Pending 交易處理結果
     * @param queryDate  UI_營業日 (YYYYMMDD)
     * @param querySelect UI_查詢交易 (全部/成功/失敗/未完成) (收款/付款)
     * @param pageNum    頁碼
     * @param pageSize   每頁筆數
     * @param webId
     * @return PageInfo
     */
    public PageInfo<HashMap<String, Object>> query2700TxnResult(String queryDate, String querySelect, Integer pageNum, Integer pageSize, String webId) throws Exception {
        try {
            // 1. 讀取 BSDAYS
            List<Bsdays> bsdaysList = bsdaysExtMapper.getBsdaysByDateOrNbsdy(queryDate);
            if (bsdaysList == null || bsdaysList.isEmpty()) {
                return null;
            }

            // 交易起日 = 第一筆BSDASY_DATE，交易迄日 = 最後一筆BSDASY_DATE
            String stDate = bsdaysList.get(0).getBsdaysDate();
            String endDate = bsdaysList.get(bsdaysList.size() - 1).getBsdaysDate();

            Sysstat sysstat = sysstatExtMapper.selectAll().get(0);
            String hbkno = sysstat.getSysstatHbkno();

            // 2. FEPTXN
            PageInfo<HashMap<String, Object>> pageInfo = PageHelper.startPage(pageNum, pageSize, pageNum > 0 && pageSize > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    if ("019510".equals(webId)) { // (收款/原存)
                        feptxnDao.getFEPTXN2700Result510(stDate, endDate, queryDate, hbkno, querySelect);
                    } else if ("019520".equals(webId)) { // (付款/代理)
                        feptxnDao.getFEPTXN2700Result520(stDate, endDate, queryDate, hbkno, querySelect);
                    } else if ("019530".equals(webId)) { // (Pending交易確認/回覆)
                        feptxnDao.getFEPTXN2700Result530(stDate, endDate, queryDate, hbkno, querySelect);
                    }
                }
            });

            return pageInfo;
        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }

    /**
     * UI019530 資金調撥Pending交易確認及回覆
     * @param txDate
     * @param bkno
     * @param stan
     * @param action
     */
    public String process019530Action(String txDate, String bkno, String stan, String action) throws Exception {
        try {
            FeptxnExt feptxnEntity = feptxnDao.get2700Tx530Action(txDate, bkno, stan);
            if (feptxnEntity == null) {
                return "查無資料";
            }

            String cbsRc = StringUtils.isBlank(feptxnEntity.getFeptxnCbsRc()) ? "" : feptxnEntity.getFeptxnCbsRc().trim();
            String conRc = StringUtils.isBlank(feptxnEntity.getFeptxnConRc()) ? "" : feptxnEntity.getFeptxnConRc().trim();
            String conExcpCode = StringUtils.isBlank(feptxnEntity.getFeptxnConExcpCode()) ? "" : feptxnEntity.getFeptxnConExcpCode().trim();

            LogData localLogContext = new LogData();
            localLogContext.setProgramName("UI019530");
            localLogContext.setTxDate(txDate);
            localLogContext.setStan(stan);

            // 依 UI Action button 執行對應動作
            switch (action) {
                case "successIn": // 成功入帳
                    if (StringUtils.isNotBlank(conRc) &&!"4001".equals(conRc) && "PEND".equals(cbsRc)) {
                        feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, conRc);
                        return "已記錄交易成功";
                    } else if (StringUtils.isNotBlank(conRc)) {
                        return "交易已完成";
                    } else {
                        feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, "4001");
                        return "已記錄交易成功";
                    }

                case "failIn": // 失敗入帳
                    if (StringUtils.isNotBlank(conRc) &&!"4001".equals(conRc) && "PEND".equals(cbsRc)) {
                        feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, conRc);
                        return "已記錄交易失敗";
                    } else if (StringUtils.isNotBlank(conRc)) {
                        return "交易已完成";
                    } else {
                        feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, "0501");
                        return "已記錄交易失敗";
                    }

                case "successOut": // 成功扣帳
                    if (StringUtils.isNotBlank(conRc)) {
                        return "交易已完成";
                    }
                    feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, "4001");
                    return "已記錄交易成功";

                case "failOut": // 失敗扣帳
                    if (StringUtils.isNotBlank(conRc)) {
                        return "交易已完成";
                    }
                    feptxnDao.updateFeptxnConExcpCode(txDate, bkno, stan, "0501");
                    return "已記錄交易失敗";

                case "reviewIn": // 收款覆核 Button
                    if (StringUtils.isBlank(conExcpCode)) {
                        return "承辦尚未處理";
                    }
                    FISCData txData = new FISCData();
                    txData.setLogContext(localLogContext);
                    txData.setFeptxnDao(this.feptxnDao);
                    txData.setTxChannel(FEPChannel.FEP);
                    txData.setTxSubSystem(SubSystem.INBK);
                    txData.setMessageID(feptxnEntity.getFeptxnPcode());
                    txData.setMessageFlowType(MessageFlow.Request);
                    txData.setFiscTeleType(com.syscom.fep.vo.enums.FISCSubSystem.INBK);
                    feptxnEntity.setFeptxnConRc(conExcpCode); // sendToCBS使用
                    txData.setFeptxn(feptxnEntity);

                    FISC_INBK fiscReqIn = new FISC_INBK();
                    FISCGeneral fiscGenIn = new FISCGeneral();
                    fiscGenIn.setEJ(feptxnEntity.getFeptxnEjfno());
                    fiscGenIn.setSubSystem(com.syscom.fep.vo.enums.FISCSubSystem.INBK);
                    fiscGenIn.setINBKRequest(fiscReqIn);
                    txData.setTxObject(fiscGenIn);
                    FISC fiscBusiness = new FISC(txData);

                    Feptxntcb feptxntcbrecord = feptxntcbMapper.selectByPrimaryKey(feptxnEntity.getFeptxnTxDate(), feptxnEntity.getFeptxnEjfno());
                    if (feptxntcbrecord == null) {
                        return "FEPTXNTCB查無資料";
                    }

                    txData.setFeptxntcb(feptxntcbrecord);
                    if ("4001".equals(conExcpCode)) {
                        /* 轉入行為本行, 送主機入帳 */
                        String AATxTYPE = "1";
                        String AATxRs = ""; // 2026/8/10 修改
                        /* 2026/6/26 修改 */
                        ACBSAction hostAA = new IBXII002(txData);
                        CBS cbs = new CBS(hostAA, txData);
                        FEPReturnCode rtnCode = cbs.sendToCBS(AATxTYPE, AATxRs);

                        if (rtnCode != FEPReturnCode.Normal) {
                            localLogContext.setRemark("通知主機交易成功時發生錯誤!!");
                            FEPBase.sendEMS(localLogContext);
                            return "通知主機交易成功時發生錯誤，請與承辦人員確認 !!";
                        } else { /* 2026/6/26 修改 */
                            feptxnDao.updateFeptxnConRc(txDate, bkno, stan, conExcpCode, "A"); /*成功*/
                        }
                    } else {
                        /* 沖轉跨行代收付 */
                        FEPReturnCode processRtnCode = fiscBusiness.processAptot(true);
                        if (processRtnCode != FEPReturnCode.Normal) {
                            return "ProcessAPTOT 沖轉失敗";
                        }

                        String AATxTYPE = ""; // 不需提供此值
                        String AATxRs = "N";    // 不需等待主機回應
                        ACBSAction hostAA = new CBTXEND(txData);
                        CBS cbs = new CBS(hostAA, txData);
                        FEPReturnCode rtnCode = cbs.sendToCBS(AATxTYPE, AATxRs);

                        if (rtnCode != FEPReturnCode.Normal) {
                            localLogContext.setRemark("通知主機交易失敗時發生錯誤!!");
                            FEPBase.sendEMS(localLogContext);
                            return "通知主機交易失敗時發生錯誤，請與承辦人員確認 !!";
                        } else {
                            feptxnDao.updateFeptxnConRc(txDate, bkno, stan, conExcpCode, "C"); /*Accept-Reverse*/
                        }
                    }
                    return "收款覆核，交易已完成";

                case "reviewOut": // 扣款覆核 Button
                    if (StringUtils.isBlank(conExcpCode)) {
                        return "承辦尚未處理";
                    }
                    int uiRc = "4001".equals(conExcpCode) ? 1 : 2; //SendConfirmByManual 的選項

                    FISCData manualTxData = new FISCData();
                    manualTxData.setLogContext(localLogContext);
                    manualTxData.setFeptxnDao(this.feptxnDao);
                    manualTxData.setFiscTeleType(com.syscom.fep.vo.enums.FISCSubSystem.INBK);
                    manualTxData.setMessageID(feptxnEntity.getFeptxnPcode()); // 底層會切割字串
                    manualTxData.setTxChannel(FEPChannel.FEP);
                    manualTxData.setTxSubSystem(SubSystem.INBK);
                    manualTxData.setMessageFlowType(MessageFlow.Request);
                    manualTxData.setTxRequestMessage("");
                    manualTxData.setFeptxn(feptxnEntity);

                    Msgctl mockMsgCtl = new Msgctl();
                    mockMsgCtl.setMsgctlStatus((short)1); // 1: 代表交易啟用正常
                    manualTxData.setMsgCtl(mockMsgCtl);

                    FISC_INBK fiscReq = new FISC_INBK();
                    fiscReq.setRsCode(String.valueOf(uiRc));
                    fiscReq.setEj(feptxnEntity.getFeptxnEjfno());
                    // AA 的 searchFeptxn 會將日期做 substring(0,7) 然後轉西元年，所以先轉成民國年
                    String rocDate = CalendarUtil.adStringToROCString(feptxnEntity.getFeptxnTxDate());
                    fiscReq.setTxnInitiateDateAndTime(rocDate + feptxnEntity.getFeptxnTxTime());
                    fiscReq.setTxDatetimeFisc(feptxnEntity.getFeptxnTxDate());
                    manualTxData.setFiscreq(fiscReq);

                    FISCGeneral fiscGen= new FISCGeneral();
                    fiscGen.setEJ(feptxnEntity.getFeptxnEjfno());
                    fiscGen.setSubSystem(com.syscom.fep.vo.enums.FISCSubSystem.INBK);
                    fiscGen.setINBKRequest(fiscReq); // 讓 AA 讀取模擬參數
                    fiscGen.setINBKConfirm(fiscReq);

                    manualTxData.setTxObject(fiscGen);
                    SendConfirmByManual confirmAA = new SendConfirmByManual(manualTxData);
                    confirmAA.processRequestData();

                    if (manualTxData.getLogContext().getReturnCode() != null &&
                            !CommonReturnCode.Normal.equals(manualTxData.getLogContext().getReturnCode())) {
                        localLogContext.setRemark("發送 Confirm 電文失敗!!");
                        FEPBase.sendEMS(localLogContext);
                        return "發送 Confirm 電文失敗";
                    }
                    return "扣款覆核，交易已完成";

                default:
                    return "未知的動作";
            }

        } catch (Exception e) {
            sendEMS(e);
            throw ExceptionUtil.createException(e, this.getInnerMessage(e));
        }
    }
}
