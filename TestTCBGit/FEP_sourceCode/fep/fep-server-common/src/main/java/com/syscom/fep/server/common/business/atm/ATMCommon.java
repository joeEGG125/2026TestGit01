package com.syscom.fep.server.common.business.atm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.util.ChipICPIN;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.StanGenerator;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.InbkparmAcqFlag;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class ATMCommon extends BusinessBase {
    private RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header header;
    private RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq body;

    private Inbk2160Mapper inbk2160Mapper;
    private ATMData atmTxData;
    protected ATMGeneralRequest atmReq;
    protected RCV_NB_GeneralTrans_RQ nbReq;
    protected RCV_VA_GeneralTrans_RQ vaReq;
    protected RCV_HCE_GeneralTrans_RQ hceReq;
    protected RCV_VO_GeneralTrans_RQ voReq;
    protected ATMGeneralResponse atmRes;
    protected Atmmstr atmMstr;
    protected Atmstat atmStat;
    protected ATMTXCD txCode;
    protected static final String SpACTNO1 = "0003900100001688"; // 信用卡專戶1
    protected static final String SpACTNO2 = "0003900100002988";// 信用卡專戶2
    protected static final String SpACTNO3 = "0003900100001773";// 信用卡專戶3
    protected static final String SpACTNO4 = "0012100100400649";// 信用卡專戶4

    protected MsgfileMapper msgFileMapper = SpringBeanFactoryUtil.getBean(MsgfileMapper.class);
    protected RetainMapper retainMapper = SpringBeanFactoryUtil.getBean(RetainMapper.class);
    protected AtmstatMapper atmstatMapper = SpringBeanFactoryUtil.getBean(AtmstatMapper.class);
    protected AtmmstrMapper atmmstrMapper = SpringBeanFactoryUtil.getBean(AtmmstrMapper.class);
    protected SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
    protected CbactExtMapper cbactExtMapper = SpringBeanFactoryUtil.getBean(CbactExtMapper.class);
    protected ChannelMapper channelMapper = SpringBeanFactoryUtil.getBean(ChannelMapper.class);
    protected AtmcExtMapper atmcExtMapper = SpringBeanFactoryUtil.getBean(AtmcExtMapper.class);
    protected AtmcoinExtMapper atmcoinExtMapper = SpringBeanFactoryUtil.getBean(AtmcoinExtMapper.class);
    protected EmvcMapper emvcMapper = SpringBeanFactoryUtil.getBean(EmvcMapper.class);
    protected AtmcashExtMapper atmcashExtMapper = SpringBeanFactoryUtil.getBean(AtmcashExtMapper.class);
    protected AtmboxlogExtMapper atmboxlogExtMapper = SpringBeanFactoryUtil.getBean(AtmboxlogExtMapper.class);
    protected AtmboxExtMapper atmboxExtMapper = SpringBeanFactoryUtil.getBean(AtmboxExtMapper.class);
    protected MsgctlMapper msgctlMapper = SpringBeanFactoryUtil.getBean(MsgctlMapper.class);
    protected AtmcoinlogMapper atmcoinlogMapper = SpringBeanFactoryUtil.getBean(AtmcoinlogMapper.class);
    protected BusiExtMapper busiExtMapper = SpringBeanFactoryUtil.getBean(BusiExtMapper.class);
    protected CorpayMapper corpayMapper = SpringBeanFactoryUtil.getBean(CorpayMapper.class);
    protected BinMapper binMapper = SpringBeanFactoryUtil.getBean(BinMapper.class);
    protected ActivityMapper activityMapper = SpringBeanFactoryUtil.getBean(ActivityMapper.class);
    protected BarcodeExtMapper barcodeExtMapper = SpringBeanFactoryUtil.getBean(BarcodeExtMapper.class);
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    protected UpbinExtMapper upbinExtMapper = SpringBeanFactoryUtil.getBean(UpbinExtMapper.class);

    protected ATMCommon() {
        super();
    }

    /**
     * ATMBusiness為處理ATM交易相關邏輯之共用程式,故需要傳入ATMData初始化
     * 初始化時,AA會將ATM交易訊息物件傳入(ATMData),建構式中會將ATM電文的上行,下行物件先存放在Local變數中以方便處理
     *
     * @param atmMsg Business撰寫重點 1.如果是不需回傳值的以FepReturnCode回傳處理結果 2.必須撰寫Try
     *               Catch,如有例外則需呼叫SendEMS,並回傳適當FEPReturnCode
     *               3.如果呼叫DBXXX物件做資料庫處理,記得在Finally呼叫Dispose方法關閉連線(DBFeptxn除外)
     *
     *               <history> <modify> <modifier>Kyo</modifier> <reason>
     *               BRSID(001B0019)：例外訊息未THROW正確 </reason> <date>2010/03/09</date>
     *               </modify> </history>
     * @throws Exception
     */
    protected ATMCommon(ATMData atmMsg) throws Exception {
        super(atmMsg);
        this.atmTxData = atmMsg;
        this.atmReq = this.atmTxData.getTxObject().getRequest();
        this.atmRes = this.atmTxData.getTxObject().getResponse();
        // 2010-10-27 by kyo for sendtoCredit 呼叫pinblkconvert用
        //--ben-20220922-//this.mGeneralData.setFpb2(this.atmReq.getFPB2());
        //--ben-20220922-//this.mGeneralData.setFpb2New(this.atmReq.getFpb2New());

        //--ben-20220922-//this.txCode = ATMTXCD.parse(this.atmReq.getTXCD());
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = null;
        //--ben-20220922-//String atmAtmno = StringUtils.join(this.atmReq.getBRNO(), this.atmReq.getWSNO());
//		String atmAtmno = StringUtils.join("","");
        String atmAtmno = atmTxData.getAtmNo();
        if(atmTxData.getTxChannel().equals(FEPChannel.POS)) {
        	atmAtmno = "POS00001";
        }else if(atmTxData.getTxChannel().equals(FEPChannel.EAT)) {
        	atmAtmno = "NEATM001";
        }
        this.atmMstr = this.atmmstrMapper.selectByPrimaryKey(atmAtmno);
        if (this.atmMstr == null) {
            rtnCode = IOReturnCode.ATMMSTRNotFound;
            throw ExceptionUtil.createNullPointException("ATMMSTR讀取失敗,ATMNO-", atmAtmno);
        }
        this.setATMMSTR(this.atmMstr);
        this.atmStat = this.atmstatMapper.selectByPrimaryKey(this.atmMstr.getAtmAtmno());
        if (this.atmStat == null) {
            rtnCode = IOReturnCode.ATMSTATNotFound;
            throw ExceptionUtil.createNullPointException("ATMSTAT讀取失敗,ATMNO-", this.atmMstr.getAtmAtmno());

        }
    }

    /**
     * Fly 2019/09/17 VAA2566允許其他Channel進來
     *
     * @param atmMsg
     * @param api
     */
    protected ATMCommon(ATMData atmMsg, String api) {
        super(atmMsg);
        this.atmTxData = atmMsg;
        this.atmReq = this.atmTxData.getTxObject().getRequest();
        this.atmRes = this.atmTxData.getTxObject().getResponse();

        this.atmMstr = new Atmmstr();
        this.atmMstr.setAtmZone(ZoneCode.TWN);
        this.atmMstr.setAtmLoc((short) 0);
        this.setATMMSTR(this.atmMstr);
    }

    public Atmstat getAtmStat() {
        return atmStat;
    }

    public void setAtmStat(Atmstat atmStat) {
        this.atmStat = atmStat;
    }

    public ATMData getAtmTxData() {
        return atmTxData;
    }

    public void setAtmTxData(ATMData value) {
        atmTxData = value;
    }

    /**
     * ATMMSTR屬性 For AA.ATMRefill.SendToHost() 讀取ATMMSTR主檔的屬性ATM_ZONE 判斷是否SendToATMP
     *
     * @return DefATMMSTR
     */
    public Atmmstr getAtmStr() {
        return this.atmMstr;
    }

    /**
     * 檢核轉入帳號是否轉入本行信用卡號
     *
     *
     * <history> <modify> <modifier>Ruling</modifier> <reason>Function
     * Design</reason> <date>2015/12/30</date> </modify> <modify>
     * <modifier>Ruling</modifier> <reason>加log 及 MASTER DEBIT加悠遊</reason>
     * <date>2018/05/24</date> </modify> </history>
     */
    public FEPReturnCode checkTrinCredit() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Bin defBIN = new BinExt();
        String TwoZero = "00"; // 補兩個零

        try {
            if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
                // 轉入帳號狀況檢核錯誤 E074
                getLogContext().setRemark("轉入帳號為Null或空白");
                this.logMessage(getLogContext());
                rtnCode = FEPReturnCode.TranInACTNOError;
                return rtnCode;
            }

            if (!TwoZero.equals(getFeptxn().getFeptxnTrinActno().substring(0, 2))) {
                // AE卡號15位，帳號第1碼為'0'，讀取帳號第2~7碼
                if ("0".equals(getFeptxn().getFeptxnTrinActno().substring(0, 1))) {
                    // 以行庫代號( SYSSTAT_HBKNO)及FEPTXN_TRIN_ACTNO[2:6]為 KEY
                    defBIN.setBinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
                    defBIN.setBinNo(getFeptxn().getFeptxnTrinActno().substring(1, 7));
                } else {
                    // 以行庫代號( SYSSTAT_HBKNO)及FEPTXN_TRIN_ACTNO[1:6]為 KEY
                    defBIN.setBinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
                    defBIN.setBinNo(getFeptxn().getFeptxnTrinActno().substring(0, 6));
                }
                defBIN = binMapper.selectByPrimaryKey(defBIN.getBinNo(), defBIN.getBinBkno());
                // 讀取 BIN 檔
                if (defBIN != null) {
                    getFeptxn().setFeptxnTrinKind(defBIN.getBinProd());
                    rtnCode = checkDigit(getFeptxn().getFeptxnTrinActno());
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                } else {
                    // 如轉入非本行信用卡，回給 ATM 錯誤訊息
                    getLogContext().setRemark("轉入非永豐信用卡");
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.TranInACTNOError;
                    return rtnCode;
                }

                // 2018-05-24 Modify by Ruling for MASTER DEBIT加悠遊
                if (BINPROD.Debit.equals(getFeptxn().getFeptxnTrinKind())) {
                    getLogContext().setRemark("轉入帳號為本行簽帳金融卡(DEBIT卡)");
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.TranInACTNOError;
                    return rtnCode;
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    public boolean checkADMData(String admArea) {
        try {
            // 檢查是否為存提款機
            if (this.atmMstr.getAtmAtmtype() != 2) {
                return false;
            }
            if (admArea.length() >= 8) {
                //--ben-20220922-//if (atmReq.getAdmboxArea().substring(8).length() % 29 == 0) {
                if (true) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 讀取拆解CoinArea用的資料
     *
     * @return
     */
    public boolean checkCOINData() {
        try {
            // 檢查是否為存提款機
            if (getAtmStr().getAtmAtmtype() != 2) {
                return false;
            }
            //--ben-20220922-//if (atmReq.getCoinArea().length() >= 6) {
            if (true) {
                //--ben-20220922-//if ((atmReq.getCoinArea().substring(6).length()) % 22 == 0) {
                if (true) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * ''' <summary>
     * ''' 取得交易記錄相關資料(在確認類AA開始時處理)
     * ''' </summary>
     * ''' <returns>FEPTXN的DEF物件</returns>
     * ''' <remarks>檢核原交易帳號，ATMControl的TMO取得資料的KEY不同</remarks>
     * ''' <history>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>Function modify</reason>
     * ''' <date>2009/02/02</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>SPEC修改邏輯，若本營業日沒找到，找上營業日</reason>
     * ''' <date>2010/03/11</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>SPEC修正for 避免取得錯誤原交易的問題，多比對帳號與金額</reason>
     * ''' <date>2010/05/04</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>BugReport(001B0466):SPEC修正邏輯:換日後第一筆交易 取不到原交易。改使用交易日期NEW一個DBFEPTXN物件</reason>
     * ''' <date>2010/05/13</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>由於機台送上來的ORI_ATMSEQ_1為00000000 所以增加判斷若為00000000則帶系統日期</reason>
     * ''' <date>2010/05/22</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>SPEC新增:電文AEX及TMO 不需檢核原交易之帳號及金額</reason>
     * ''' <date>2010/05/31</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>調整邏輯:電文為AEX及TMO一定不是confirm電文，因此使用_txcode變數判斷，判斷送上來的電文是否為AEX或TMO即可</reason>
     * ''' <date>2010/06/13</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>BugReport(001B0669):SPEC修改 若電文為CDF則判斷轉入帳號</reason>
     * ''' <date>2010/06/15</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>ChenLi</modifier>
     * ''' <reason> 方便釐清找不到原交易問題，增加Log</reason>
     * ''' <date>2014/12/09</date>
     * ''' </modify>
     * ''' </history>
     *
     * @return
     */
    public Feptxn checkConData() {
        Feptxn dbfeptxn;
        String date = String.valueOf(Integer.parseInt(atmReq.getTRANDATE().substring(0, 2)) + 2000);
        String day = atmReq.getTRANDATE().substring(2, 6);
        String FSCODE = atmReq.getFSCODE();
        String PCODE = atmReq.getPICCPCOD();
        String MSGCAT = StringUtils.trimToEmpty(atmReq.getMSGCAT());
        String MSGTYP = StringUtils.trimToEmpty(atmReq.getMSGTYP());
        //FC、FT前置交易無Con電文，濾掉此電文的處理
        if("F".equals(MSGCAT) && ("C".equals(MSGTYP.substring(0, 1)) || "T".equals(MSGTYP.substring(0, 1)))){
            feptxn = null;
            return feptxn;
        }
        feptxn.setFeptxnTxDateAtm(date + day);
        feptxn.setFeptxnAtmno(getAtmTxData().getAtmNo());
        feptxn.setFeptxnAtmSeqno(atmReq.getTRANSEQ());
        feptxn.setFeptxnTxCode(FSCODE);
        feptxn.setFeptxnMsgid(FSCODE); //like 'FSCODE%'
        feptxn.setFeptxnTraceEjfno(0);
        // 20250427 移除(FEPTXN_MSGID, 1, 2) != 'FC' SQL條件
        dbfeptxn = feptxnDao.getFEPTXNForATMCheckConData(feptxn.getFeptxnTxDateAtm(), feptxn.getFeptxnAtmno(), feptxn.getFeptxnAtmSeqno(), feptxn.getFeptxnTxCode(),feptxn.getFeptxnMsgid(),feptxn.getFeptxnTraceEjfno());
        if (dbfeptxn == null) {
            //ATM 交易日期-1 再找一次
            String prevDate = java.time.LocalDate
                    .parse(feptxn.getFeptxnTxDateAtm(), java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                    .minusDays(1)
                    .format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            dbfeptxn = feptxnDao.getFEPTXNForATMCheckConData(prevDate, feptxn.getFeptxnAtmno(), feptxn.getFeptxnAtmSeqno(), feptxn.getFeptxnTxCode(),feptxn.getFeptxnMsgid(),feptxn.getFeptxnTraceEjfno());
        }

        //檔名SEQ為SYSSTAT_TBSDY_FISC[7,2]
        String trk2 = atmReq.getTRK2();
        int trk_index = trk2.indexOf("=");
        if(trk_index > 0) {
        }else {
        	trk_index = trk2.indexOf("D");
        	if(trk_index > 0) {
        		trk2 = trk2.substring(0, trk_index) + "=" + trk2.substring(trk_index + 1);
        	}
        }
        
        if((StringUtils.isNotBlank(PCODE)  && StringUtils.equals(PCODE.substring(0, 2), "26")) 
        		|| (FSCODE.equals("P5") || FSCODE.equals("P6")) ) {
        	atmReq.setTRK2(trk2);
        }
        
        try {
        	// ATM.REQ是轉ASCII後的位置
        	String tmAscii = EbcdicConverter.fromHex(CCSID.English, getAtmTxData().getTxRequestMessage());
            if (dbfeptxn == null) {
                feptxn = null;
                return feptxn;
            } else if ( (("P1".equals(FSCODE) && !StringUtils.equals("K 200", tmAscii.substring(179, 184))))
            		&& atmReq.getCARDPART1().substring(6, 19).equals(StringUtils.trim(dbfeptxn.getFeptxnMajorActno()))) {
            	//磁條卡密碼變更
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("磁條卡密碼變更");
                logMessage(this.logContext);
                return dbfeptxn;
            } else if (!"AA".equals(MSGTYP) && "FV".equals(FSCODE)
            		&& atmReq.getFADATA().substring(0, 16).equals(dbfeptxn.getFeptxnMajorActno())) {
            	 //指靜脈建置
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("指靜脈建置");
                logMessage(this.logContext);
                return dbfeptxn;
            } else if ((FSCODE.equals("P5") || FSCODE.equals("P6")) 
            		&& atmReq.getTRK2().equals(dbfeptxn.getFeptxnTrk2())) {
            	//國際卡密碼變更
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("國際卡密碼變更");
                logMessage(this.logContext);
                return dbfeptxn;
            } else if ("E".equals(atmReq.getCARDFMT()) 
            		&& "26".equals(atmReq.getPICCPCOD().substring(0, 2)) 
            		&& atmReq.getTRK2().equals(dbfeptxn.getFeptxnTrk2())) {
            	//國際卡EMV交易
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("國際卡EMV交易");
                logMessage(this.logContext);
                return dbfeptxn;
            } else if (!FSCODE.equals("DX")
            		&& StringUtils.trim(atmReq.getCARDDATA().substring(21, 37)).equals(StringUtils.trim(dbfeptxn.getFeptxnMajorActno()))) {
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("非DX");
                logMessage(this.logContext);
                return dbfeptxn;
            } else if (FSCODE.equals("DX")) {
                this.logContext.setProgramName(ProgramName + ".checkConData");
                this.logContext.setRemark("DX");
                logMessage(this.logContext);
                return dbfeptxn;
            }
            this.logContext.setProgramName(ProgramName + ".checkConData");
            this.logContext.setRemark("return feptxn = null");
            logMessage(this.logContext);
            feptxn = null;
            return feptxn;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return null;
        }
    }
    
    public Feptxn eatm_checkConData() {
        Feptxn dbfeptxn;
        String date = String.valueOf(Integer.parseInt(atmReq.getTRANDATE().substring(0, 2)) + 2000);
        String day = atmReq.getTRANDATE().substring(2, 6);
        feptxn.setFeptxnTxDateAtm(date + day);
        feptxn.setFeptxnAtmno(atmReq.getIPYDATA().substring(10, 18));
        feptxn.setFeptxnAtmSeqno(atmReq.getTRANSEQ());
        feptxn.setFeptxnTxCode(atmReq.getFSCODE());
        feptxn.setFeptxnChannelEjfno(getAtmTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader().getCLIENTTRACEID());

        dbfeptxn = feptxnDao.getFEPTXNForConData(feptxn.getFeptxnTxDateAtm(), feptxn.getFeptxnAtmno(), feptxn.getFeptxnAtmSeqno(), feptxn.getFeptxnTxCode(), feptxn.getFeptxnChannelEjfno());
        try {
            if (dbfeptxn == null) {
                return dbfeptxn;
            } else if (!atmReq.getCARDDATA().substring(21, 37).equals(dbfeptxn.getFeptxnTroutActno()) || (Objects.toString(StringUtils.isNotBlank(Objects.toString(atmReq.getAMTNOND())) ? atmReq.getAMTNOND().divide(new BigDecimal("100")) : atmReq.getAMTNOND())).equals(dbfeptxn.getFeptxnTxAmt())) {
                feptxn = null;
                return feptxn;
            } else {
                return dbfeptxn;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return null;
        }
    }

    // [20221202]: 移除prepareWPFEPTxn方法，新增PrepareFEPTXN_EMV方法

    /**
     * 將ATM_TITA電文相關欄位, 準備寫入 FEPTxn
     *
     * @return
     */
    public FEPReturnCode PrepareFEPTXN_EMV() {
        try {
        	BigDecimal oneHundred = new BigDecimal("100");
            // 1. 以ATM_TITA相關資料, 寫入 FEPTXN
            getFeptxn().setFeptxnMsgid(getAtmTxData().getMessageID()); // 訊息代號
            Calendar systemDate = Calendar.getInstance();
            getFeptxn().setFeptxnTxDate(FormatUtil.dateTimeFormat(systemDate, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(FormatUtil.dateTimeFormat(systemDate, FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); // 交易時間
            // ATM交易日期(電文資料是西元後兩碼+月日共6碼)
            String atmReqTranDate = atmReq.getTRANDATE();
            String date = Objects.toString(Integer.parseInt(atmReqTranDate.substring(0, 2)) + 2000);
            String day = atmReqTranDate.substring(2, 6);
            getFeptxn().setFeptxnTxDateAtm(date + day);
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + atmReq.getTRANTIME());
            getFeptxn().setFeptxnStan(getStan()); // 自行、跨行交易皆須提供追蹤序號
            getFeptxn().setFeptxnEjfno(getEj()); // 電子日誌序號
            getFeptxn().setFeptxnAtmSeqno(atmReq.getTRANSEQ()); // ATM交易序號
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnAtmno(getAtmTxData().getAtmNo()); // Handler帶入的櫃員機代號
            getFeptxn().setFeptxnTxCode(atmReq.getFSCODE()); // ATM交易代號
            getFeptxn().setFeptxnTxrust("0"); // 處理結果
            getFeptxn().setFeptxnChannel(getAtmTxData().getTxChannel().name()); // 通道別
            getFeptxn().setFeptxnAtmType(atmReq.getPITRMTYP()); // 端末型態
            Zone defZone = getZoneByZoneCode("TWN");
            if (defZone == null) {
                return IOReturnCode.ZONENotFound;
            }
            if (defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); // ATM 所在地區 MOD
            }
            if (StringUtils.isNotBlank(defZone.getZoneTbsdy())) {
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy()); // 自行營業日
            }
            String trk2 = atmReq.getTRK2();
            int trk_index = trk2.indexOf("=");
            if(trk_index > 0) {
            }else {
            	trk_index = trk2.indexOf("D");
            	if(trk_index > 0) {
            		trk2 = trk2.substring(0, trk_index) + "=" + trk2.substring(trk_index + 1);
            	}
            }
            getFeptxn().setFeptxnTrk2(trk2); //國際卡二軌放信用卡號
            getFeptxn().setFeptxnTmoFlag((short) 0); //預設
            
            if(("P5".equals(getFeptxn().getFeptxnTxCode()) || "P6".equals(getFeptxn().getFeptxnTxCode()))){
            	getFeptxn().setFeptxnPcode("2471");
            	getFeptxn().setFeptxnTroutBkno(SysStatus.getPropertyValue().getSysstatHbkno());
            	getFeptxn().setFeptxnFiscFlag((short) 0); //自行
            	//子系統
            	getFeptxn().setFeptxnSubsys(getAtmTxData().getMsgCtl().getMsgctlSubsys());
            	getFeptxn().setFeptxnTmoFlag((short) 1);
            }else {
    			getFeptxn().setFeptxnPcode(atmReq.getPICCPCOD()); // 財金交易代號
    			getFeptxn().setFeptxnFiscFlag((short) 1); // 跨行
    			// 子系統
    			getFeptxn().setFeptxnSubsys((short) 1);
    			String BIN_NO = getFeptxn().getFeptxnTrk2().substring(0, 6);
    			String BIN_BKNO = SysStatus.getPropertyValue().getSysstatHbkno();
    			Bin bin = binMapper.selectByPrimaryKey(BIN_NO, BIN_BKNO);
    			if (bin != null) {
    				getFeptxn().setFeptxnTroutKind(bin.getBinProd());
    	           if (StringUtils.equals(getFeptxn().getFeptxnPcode(), "2620") ||
    	            			StringUtils.equals(getFeptxn().getFeptxnPcode(), "2630")){ //PLUS、Cirrus
    	            	//合庫發卡之COMB卡及CREDIT CARD不可使用2620、2630
    	            	getFeptxn().setFeptxnTmoFlag((short) 0); 
    	            }else if (StringUtils.equals(getFeptxn().getFeptxnPcode().substring(3, 4), "1")){ //餘額查詢交易
    	            	//合庫卡不可使用國際卡餘額查詢交易( 2601, 2621,2631 )
    	            	getFeptxn().setFeptxnTmoFlag((short) 0); 
    	            }else {
    	            	getFeptxn().setFeptxnTmoFlag(bin.getBinEmvflag());
    	            }
    			}else {
    				getFeptxn().setFeptxnTmoFlag((short) 1);
    			}
    			
            }
			// 國際卡卡號取值，最多16位
            if(getFeptxn().getFeptxnTrk2().contains("D") || getFeptxn().getFeptxnTrk2().contains("=")) {
                String processedTrk2 = getFeptxn().getFeptxnTrk2().split("[D=]", 2)[0];
                getFeptxn().setFeptxnTroutActno(processedTrk2.length() > 16 ? processedTrk2.substring(0,16) : processedTrk2);
            } else {
                getFeptxn().setFeptxnTroutActno(getFeptxn().getFeptxnTrk2().substring(0, 16));
            }

			if (StringUtils.equals(getFeptxn().getFeptxnPcode().substring(0, 3), "260")) { // 銀聯卡交易
				getFeptxn().setFeptxnTmoFlag((short) 0);
				for (int i = 4; i <= 12; i++) {
					Short UPBIN_BIN_LENGTH = Short.valueOf(String.valueOf(i));
					String UPBIN_BIN = getFeptxn().getFeptxnTrk2().substring(0, i);
					Upbin upbin = upbinExtMapper.selectByPrimaryKey(UPBIN_BIN_LENGTH, UPBIN_BIN);
					if (upbin != null) {
						getFeptxn().setFeptxnTroutKind("U");// 銀聯卡
						getFeptxn().setFeptxnTmoFlag((short) 1); // 可使用銀聯卡交易
						break;
					}
				}
			}
            //發卡行
            //260X銀聯/264X JCB
            String pcode_03 = getFeptxn().getFeptxnPcode().substring(0, 3);
            if(StringUtils.equals(pcode_03, "260") || StringUtils.equals(pcode_03, "264")) {
            	getFeptxn().setFeptxnTroutBkno("944");
            }else if(StringUtils.equals(pcode_03, "262")) {
            	getFeptxn().setFeptxnTroutBkno("947");
            }else if(StringUtils.equals(pcode_03, "263")) {
            	getFeptxn().setFeptxnTroutBkno("946");
            }
            getFeptxn().setFeptxnPinblock(atmReq.getSSCODE()); // 磁條卡密碼
            getFeptxn().setFeptxnExcpCode(StringUtils.substring(atmReq.getSTATUS(), 0, 2)); // ATM上送交易STATUS
            getFeptxn().setFeptxnMsgflow("A1"); // (ATM REQUEST)
            // 代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(getAtmTxData().getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());    //財金營業日
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTbsdyFisc(getAtmTxData().getTbsdyFISC());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }
            // ATM交易日期時間
            getFeptxn().setFeptxnTxDatetimeFisc(atmReq.getPICCBI19());
            if(StringUtils.equals(feptxn.getFeptxnPcode().substring(0, 2), "26")) { //預借現金交易
            	 getFeptxn().setFeptxnTxCur("TWD"); //提領幣別
            	 getFeptxn().setFeptxnTxAmt(atmReq.getAMTDISP().divide(oneHundred)); //交易金額
            	 getFeptxn().setFeptxnTxAmtAct(atmReq.getAMTDISP().divide(oneHundred)); //現金吐鈔金額
            }
            if (DbHelper.toBoolean(getAtmTxData().getMsgCtl().getMsgctlFisc2way())) {
            	getFeptxn().setFeptxnWay((short) 2);
            } else {
            	getFeptxn().setFeptxnWay((short) 3);
            }
            getFeptxn().setFeptxnAtmZone("TWN");
            // Channel：ATM可分PCODE上線，Y：交易轉送主機處理，N：FEP處理
            // Channel：非ATM(EATM/POS)，沒有分PCODE上線機制 */
            if(StringUtils.equals(getFeptxn().getFeptxnChannel(), "ATM")) {
            	getFeptxn().setFeptxnCbsProc(getAtmTxData().getMsgCtl().getMsgctlCbsProc());
            } else {
            	getFeptxn().setFeptxnCbsProc("N");
            }
            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    public FEPReturnCode prepareConFEPTXN() {
        try {
            // 1.以ATM_TITA相關資料, 寫入 FEPTXN
            String strEj = "";
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Confirm_Request);
            // 存入 Confirm EJNO
            getFeptxn().setFeptxnTraceEjfno(getAtmTxData().getEj()); // 電子日誌序號
            // 更新 Confirm 相關資訊
            // 2010-04-20 modified by kyo for 日期存入資料庫要轉西元年
            //--ben-20220922-//getFeptxn().setFeptxnConAtmSeqno1(CalendarUtil.rocStringToADString(atmReq.getAtmseq_1()));
            //--ben-20220922-//getFeptxn().setFeptxnConAtmSeqno2(atmReq.getAtmseq_2());
            //--ben-20220922-//getFeptxn().setFeptxnConTxCode(atmReq.getTXCD());
            // [20221006]依照dolly規格書新增及修改程式碼
            getFeptxn().setFeptxnConAtmSeqno1(Objects.toString(Integer.parseInt(StringUtils.substring(atmReq.getTRANDATE(), 0, 2)) + 2000) + StringUtils.substring(atmReq.getTRANDATE(), 2, 6)); // 交易日期(西元年)
            getFeptxn().setFeptxnConAtmSeqno2(atmReq.getTRANSEQ()); // 交易序號
            getFeptxn().setFeptxnConTxCode(atmReq.getFSCODE() + "C"); // 交易代號
            getFeptxn().setFeptxnConTxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); // 交易時間
            getFeptxn().setFeptxnConExcpCode(StringUtils.substring(atmReq.getSTATUS(), 0, 2)); // ATM上送交易STATUS 異常代碼

            if(getFeptxntcb() != null) {
            	getFeptxntcb().setFeptxntcbPbmcrd(StringUtils.substring(atmReq.getSTATUS(), 14, 15)); // 異常代碼(吃卡註記)
            }
            
            return FEPReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

//	public FEPReturnCode prepareInbk2160() {
//		try {
//			setInbk2160(new Inbk2160());
//			inbk2160Mapper = SpringBeanFactoryUtil.getBean(Inbk2160Mapper.class);
//			FEPReturnCode rtnCode = CommonReturnCode.Normal;
//			// FEPTXN欄位, 寫入INBK2160
//			getInbk2160().setInbk2160TxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)); /* 交易日期 */
//			getInbk2160().setInbk2160Ejfno(TxHelper.generateEj()); /*電子日誌序號*/
//			getInbk2160().setInbk2160Bkno(SysStatus.getPropertyValue().getSysstatHbkno()); /*交易啟動銀行*/
//			getInbk2160().setInbk2160TxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); /*交易時間*/
//			getInbk2160().setInbk2160Atmno(getFeptxn().getFeptxnAtmno()); /* 端末機代號 */
//			getInbk2160().setInbk2160MajorActno(getFeptxn().getFeptxnMajorActno());
//			getInbk2160().setInbk2160TroutBkno(getFeptxn().getFeptxnTroutBkno());
//			getInbk2160().setInbk2160TroutActno(getFeptxn().getFeptxnTroutActno());
//			getInbk2160().setInbk2160OriTrinBkno(getFeptxn().getFeptxnTrinBkno());
//			getInbk2160().setInbk2160TxAmt(getFeptxn().getFeptxnTxAmt());
//			getInbk2160().setInbk2160Stan(getStan());
//			getInbk2160().setInbk2160FiscFlag(getFeptxn().getFeptxnFiscFlag());
//			getInbk2160().setInbk2160ReqRc("0000");
//			getInbk2160().setInbk2160Subsys((short)1);
//			getInbk2160().setInbk2160Pcode(FISCPCode.PCode2160.getValueStr());
//			getInbk2160().setInbk2160Twmp("001");
//			getInbk2160().setInbk2160Chrem(getFeptxn().getFeptxnChrem());
//			getInbk2160().setInbk2160OriPcode(getFeptxn().getFeptxnPcode());
//			getInbk2160().setInbk2160OriBkno(getFeptxn().getFeptxnBkno());
//			getInbk2160().setInbk2160OriStan(getFeptxn().getFeptxnStan());
//			getInbk2160().setInbk2160OriTxDate(getFeptxn().getFeptxnTxDate());
//			getInbk2160().setInbk2160OriTxTime(getFeptxn().getFeptxnTxTime());
//			if(StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno7())){
//				getInbk2160().setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno7());
//			}else{
//				getInbk2160().setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno() + "0000");
//			}
//			getInbk2160().setInbk2160OriIcSeqno(getFeptxn().getFeptxnIcSeqno());
//			if(FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    ||FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    ||FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())){
//                getInbk2160().setInbk2160OriIcdata("1001");
//            }else{
//				getInbk2160().setInbk2160OriIcdata("0000");
//			}
//			getInbk2160().setInbk2160OriRepRc(getFeptxn().getFeptxnRepRc());
//			if(StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno7())){
//				getInbk2160().setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno7());
//			}else{
//				if(StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())){
//					getInbk2160().setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno()+"0000");
//				}
//			}
//			getInbk2160().setInbk2160OriTrinActno(getFeptxn().getFeptxnTrinActno());
//			getInbk2160().setInbk2160OriAtmType(getFeptxn().getFeptxnAtmType());
//			getInbk2160().setInbk2160OriFeeCustpay(getFeptxn().getFeptxnFeeCustpay());
//			getInbk2160().setInbk2160OriMerchantId(getFeptxn().getFeptxnOrderNo());
//			getInbk2160().setInbk2160OriOrderNo(getFeptxn().getFeptxnOrderNo());
//			/* 2022/10/31 增加手機條碼 */
//			if(StringUtils.isNotBlank(getFeptxn().getFeptxnLuckyno())
//					&& "/".equals(getFeptxn().getFeptxnLuckyno().substring(0,1))
//			        && (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
//					||FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
//					||FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode()))){
//				getInbk2160().setInbk2160OriBarcode(getFeptxn().getFeptxnLuckyno().substring(0,8));
//			}
//			if(!"TWD".equals(getFeptxn().getFeptxnTxCur())){
//				getInbk2160().setInbk2160OriTxnAmtCur(getFeptxn().getFeptxnTxAmt());
//				getInbk2160().setInbk2160OriTxCur(getFeptxn().getFeptxnTxCur());
//			}
//			getInbk2160().setInbk2160OriEjfno(getFeptxn().getFeptxnEjfno());
//			getInbk2160().setInbk2160OriTxCode(getFeptxn().getFeptxnTxCode());
//			getInbk2160().setInbk2160OriTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
//			getInbk2160().setInbk2160OriReqRc(getFeptxn().getFeptxnReqRc());
//			getInbk2160().setInbk2160DesBkno("9430000"); /* 台灣PAY */
//			this.insertINBK2160();
//
//			return rtnCode;
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			sendEMS(getLogContext());
//			return FEPReturnCode.ProgramException;
//		}
//	}
//
//	public FEPReturnCode insertINBK2160() {
//		try {
//			int insertINBK2160 = inbk2160Mapper.insertSelective(getInbk2160());
//			if (insertINBK2160 <= 0) {
//				return FEPReturnCode.FEPTXNInsertError;
//			} else {
//				return FEPReturnCode.Normal;
//			}
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			getLogContext().setProgramName(ProgramName + ".AddTXData");
//			sendEMS(getLogContext());
//		}
//		return FEPReturnCode.Normal;
//	}

    /**
     * 從 Request 對應 Response (header)
     *
     * @throws Exception
     */
    public void mapResponseFromRequest() throws Exception {
        // 2011-06-01 by kyo for 在可能換日後的位置 強制reload Cache資料
        FEPCache.reloadCache(CacheItem.SYSSTAT);
        FEPCache.reloadCache(CacheItem.ZONE);

        // Dim defZone As New Tables.DefZONE
        getAtmTxData().setAtmZone(getZoneByZoneCode(atmMstr.getAtmZone()));

        if (getAtmTxData().getAtmZone() == null) {
            throw ExceptionUtil.createException("ZONE讀取失敗,ZONE_CODE-", getAtmTxData().getAtmZone().getZoneCode());
        }

        // ------------------------- Header -------------------------
        // 交易別
        //--ben-20220922-//atmRes.setTxcdO(atmReq.getTXCD());

        // 永豐要求ATM校時，傳回系統日期
        // 交易日期
        //ben20221118	atmRes.setDATE(StringUtils.leftPad(CalendarUtil.adStringToROCString(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)), 8, '0'));

        // 交易時間
        //ben20221118	atmRes.setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));

        // ATM ID / ATM 代號 ATM所屬分行 + ATM 代號
        //--ben-20220922-//atmRes.setATMNO(StringUtils.join(atmReq.getBRNO(), atmReq.getWSNO()));

        // MODE / 營業日MODE
        //ben20221118	atmRes.setModeO(getAtmTxData().getAtmZone().getZoneCbsMode().toString());

        // '本營業日 / 西元轉民國年
        if (ZoneCode.TWN.equals(atmMstr.getAtmZone())) {
            //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(SysStatus.getPropertyValue().getSysstatTbsdyFisc()));
        } else {
            // 港澳 NCB, 本營業日改在 T24 COB才換日
            if (ZoneCode.HKG.equals(atmMstr.getAtmZone())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//                if ("U".equals(SysStatus.getPropertyValue().getSysstatCbsHkg())) {
//                    //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneTbsdy()));
//                } else {
                    // 港澳NCB:本營業日改在T24COB才換日
                    if (getAtmTxData().getAtmZone().getZoneCbsMode() == 1) {
                        // 換日前本營業日
                        //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneTbsdy()));
                    } else {
                        // 換日後次營業日
                        //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneNbsdy()));
                    }
//                }
            } else if (ZoneCode.MAC.equals(atmMstr.getAtmZone())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//                if ("U".equals(SysStatus.getPropertyValue().getSysstatCbsMac())) {
//                    //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneTbsdy()));
//                } else {
                    // 港澳NCB:本營業日改在T24COB才換日
                    if (getAtmTxData().getAtmZone().getZoneCbsMode() == 1) {
                        // 換日前本營業日
                        //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneTbsdy()));
                    } else {
                        // 換日後次營業日
                        //ben20221118	atmRes.setDdO(CalendarUtil.adStringToROCString(getAtmTxData().getAtmZone().getZoneNbsdy()));
                    }
//                }
            }
        }

        // 信封存款MODE
        //ben20221118	atmRes.setDepmodeO("6");

        // ATM序號1 YYYYMMDD
        //--ben-20220922-//atmRes.setAtmseqO1(atmReq.getAtmseq_1());

        // ATM序號2 YYYYMMDD
        //--ben-20220922-//atmRes.setAtmseqO2(atmReq.getAtmseq_2());
    }

    /**
     * 將ATM電文欄位搬入FEPTXN Class中
     * SPEC FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
     *
     * @return FEPReturnCode
     */
    public FEPReturnCode prepareFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String strEj = "";
        Zone defZone = new Zone();
        /* SPEC  與 程式對照表			*/
        // ATM_TITA 	= atmReq		電文來源=ATMGeneralRequest
        //                eatmReq       電文來源=RCV_EATM_GeneralTrans_RQ
        // FETPTXN_  	= getFeptxn()
        // ATMTxData	= getAtmTxData()
        // SYSSTAT_		= SysStatus.getPropertyValue()		=抓取資料表內容 SYSSTAT
        // SYSSTAT_HBKNO= SysStatus.getPropertyValue().getSysstatHbkno()
        //				= atmMstr							=抓取資料表內容 ATMMSTR
        //				= getZoneByZoneCode(zone_code)		=抓取資料表內容 ZONE
        // MSGCTL_		= getAtmTxData().getMsgCtl()
        String TRANDATE = null;
        String TRANSEQ = null;
        String IPYDATA = null;
        String FSCODE = null;
        String PICCBI11 = null;
        String PICCBI28 = null;
        String CARDDATA = null;
        String SPECIALDATA = null;
        String PICCBI55 = null;
        BigDecimal AMTDISP = new BigDecimal(0);
        BigDecimal AMTNOND = new BigDecimal(0);
        String TADATA = null;
        String PICCBI9 = null;
        String PICCTAC = null;
        String STATUS = null;
        String FADATA = null;
        String PICCPCOD = null;
        String PICCBI19 = null;
        String TACODE = null;
        String WSID = null;
        String TRANTIME = null;
        String MSGTYP = null;
        try {
			/*
			2022/11/10 Aster增加判斷(當通道別=EAT 電文來源改為 RCV_EATM_GeneralTrans_RQ 其它則電文來源為ATM_GeneralTrans_RQ)
			IF  Channel = "EAT"   		(EAT=WEBATM)
				ATM_TITA 電文請參考 RCV_EATM_GeneralTrans_RQ
			ELSE						(ATM,POS)
				ATM_TITA 電文請參考 ATM_GeneralTrans_RQ
 	        END IF
			*/


            BigDecimal oneHundred = new BigDecimal("100");
            if ("EAT".equals(getAtmTxData().getTxChannel().name())) {
                header = atmTxData.getTxObject().getEatmrequest().getBody().getRq().getHeader();
                body = atmTxData.getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
                TRANDATE = body.getTRANDATE();
                TRANSEQ = body.getTRANSEQ();
                IPYDATA = body.getIPYDATA();
                FSCODE = body.getFSCODE();
                PICCBI11 = body.getPICCBI11();
                PICCBI28 = body.getPICCBI28();
                CARDDATA = body.getCARDDATA();
                SPECIALDATA = body.getSPECIALDATA();
                PICCBI55 = body.getPICCBI55();
                AMTDISP = (body.getAMTDISP() != null) ? body.getAMTDISP().divide(oneHundred) : null;
                AMTNOND = (body.getAMTNOND() != null) ? body.getAMTNOND().divide(oneHundred) : null;
                TADATA = body.getTADATA();
                PICCBI9 = body.getPICCBI9();
                PICCTAC = body.getPICCTAC();
                STATUS = body.getSTATUS();
                FADATA = body.getFADATA();
                PICCPCOD = body.getPICCPCOD();
                PICCBI19 = body.getPICCBI19();
                TACODE = body.getTACODE();
                WSID = body.getWSID();
                TRANTIME = body.getTRANTIME();
                MSGTYP = body.getMSGTYP();
            } else {
                TRANDATE = atmReq.getTRANDATE();
                TRANSEQ = atmReq.getTRANSEQ();
                IPYDATA = atmReq.getIPYDATA();
                FSCODE = atmReq.getFSCODE();
                PICCBI11 = atmReq.getPICCBI11();
                PICCBI28 = atmReq.getPICCBI28();
                CARDDATA = atmReq.getCARDDATA();
                SPECIALDATA = atmReq.getSPECIALDATA();
                PICCBI55 = atmReq.getPICCBI55();
                AMTDISP = StringUtils.isNotBlank(Objects.toString(atmReq.getAMTDISP())) ? atmReq.getAMTDISP().divide(oneHundred) : atmReq.getAMTDISP();
                AMTNOND = StringUtils.isNotBlank(Objects.toString(atmReq.getAMTNOND())) ? atmReq.getAMTNOND().divide(oneHundred) : atmReq.getAMTNOND();
                TADATA = atmReq.getTADATA();
                PICCBI9 = atmReq.getPICCBI9();
                PICCTAC = atmReq.getPICCTAC();
                STATUS = atmReq.getSTATUS();
                FADATA = atmReq.getFADATA();
                PICCPCOD = atmReq.getPICCPCOD();
                PICCBI19 = atmReq.getPICCBI19();
                TACODE = atmReq.getTACODE();
                WSID = atmReq.getWSID();
                TRANTIME = atmReq.getTRANTIME();
                MSGTYP = atmReq.getMSGTYP();
            }
            getFeptxn().setFeptxnMsgid(getAtmTxData().getMessageID()); // 訊息代號
            getFeptxn().setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); // 交易時間
            //ex. 20221102-->"20"+TRANDATE[1:2]+TRANDATE[3:4]
            if (StringUtils.isNotBlank(TRANDATE))
                getFeptxn().setFeptxnTxDateAtm("20" + TRANDATE.substring(0, 2) + TRANDATE.substring(2, 6)); // ATM交易日期(電文資料是西元後兩碼+月日共6碼)
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + atmReq.getTRANTIME());
            getFeptxn().setFeptxnEjfno(getAtmTxData().getEj()); // 電子日誌序號
            getFeptxn().setFeptxnAtmSeqno(TRANSEQ); // ATM交易序號
            //交易序號給號(2024/01/16與客戶討論後，前置交易暫不給STAN)
            // EAT 預約交易(RV)由前端給STAN(9000001~9999999)
            // POS 交易由前端給STAN(9000001~9999999)
            if ("FCC".equals( (atmReq.getMSGCAT() + atmReq.getMSGTYP()) )) {
            	 //一般交易給號：0000001~8999999、A000001~E999999
            	 getFeptxn().setFeptxnStan(getStan());
            }else if (!"FC".equals( (atmReq.getMSGCAT() + atmReq.getMSGTYP().substring(0, 1)) )
                    && !"FT".equals(atmReq.getMSGCAT() + atmReq.getMSGTYP().substring(0, 1))
            		&& !"RV".equals(atmReq.getLANGID()) 
            		&& !"POS".equals(getAtmTxData().getTxChannel().name())) {
            	 //一般交易給號：0000001~8999999、A000001~E999999
            	 getFeptxn().setFeptxnStan(getStan());
            }else if("RV".equals(atmReq.getLANGID()) || "POS".equals(getAtmTxData().getTxChannel().name())){
            	// POS 及 EAT 預約交易
            	getFeptxn().setFeptxnStan(atmReq.getSPECIALDATA().substring(46, 53));
            }
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnAtmno(getAtmTxData().getAtmNo()); // 櫃員機代號 // [20221201]修改程式碼: 改成getAtmTxData().getAtmNo()
            getFeptxn().setFeptxnTxCode(FSCODE);    // ATM交易代號
            getFeptxn().setFeptxnAtmChk(PICCBI11);    // 端末設備查核碼
            getFeptxn().setFeptxnTxrust("0"); // 處理結果
            /* 10/1 新增, for 回應給 ATM */
            getFeptxn().setFeptxnChannel(getAtmTxData().getTxChannel().name()); //通道別
            getFeptxn().setFeptxnAtmType(PICCBI28);    //端末設備型態
            defZone = getZoneByZoneCode(atmMstr.getAtmZone());
            if (defZone == null) {
                return IOReturnCode.ZONENotFound;
            }
            if (defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
            }
            if (StringUtils.isNotBlank(defZone.getZoneTbsdy())) {
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy());    //自行營業日
            }
            if (StringUtils.isNotBlank(CARDDATA)) {
                getFeptxn().setFeptxnTroutBkno(CARDDATA.substring(13, 16));     //轉出行
                getFeptxn().setFeptxnTroutActno(CARDDATA.substring(21, 37));    //轉出帳號
            }
            
            if (ATMTXCD.D8.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.I5.name().equals(getFeptxn().getFeptxnTxCode())) { //企業存款
				if (StringUtils.isNotBlank(TADATA)) {
					getFeptxn().setFeptxnTrinActno(TADATA.substring(0, 6) + StringUtils.repeat(" ", 10)); //銷帳編號
				}
                if (StringUtils.isNotBlank(CARDDATA)) {
                	getFeptxn().setFeptxnTrinBkno(CARDDATA.substring(3, 6)); // 銀行別
                    getFeptxn().setFeptxnTelephone(CARDDATA.substring(21, 31)); //連絡電話
                }
                getFeptxn().setFeptxnTroutBkno(SysStatus.getPropertyValue().getSysstatHbkno()); //轉出行放本行
            } else if (ATMTXCD.I4.name().equals(getFeptxn().getFeptxnTxCode())) {
                if (StringUtils.isNotBlank(CARDDATA)) {
                    getFeptxn().setFeptxnTrinBkno(FADATA.substring(2, 5)); //存入銀行
                    getFeptxn().setFeptxnTrinActno(CARDDATA.substring(21, 37)); //存入帳號
                }
            } else if ("D".equals(getFeptxn().getFeptxnTxCode().substring(0, 1))) { //一般存款
                if (StringUtils.isBlank(FADATA.trim())) {
                    getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatHbkno()); //存入行放本行
                } else {
                	getFeptxn().setFeptxnTrinBkno(FADATA.substring(2, 5)); //存入行
                }
                getFeptxn().setFeptxnTrinBkno7( getFeptxn().getFeptxnTrinBkno() + "0000");
                getFeptxn().setFeptxnTrinActno(TADATA.substring(0, 16)); //存入帳號
                //客戶輸入的聯絡電話
                if (StringUtils.isNotBlank(SPECIALDATA))
                    getFeptxn().setFeptxnTelephone(SPECIALDATA.substring(0, 10));
                if ("DA".equals(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTroutBkno7(SysStatus.getPropertyValue().getSysstatHbkno() + "9998");
                } else if ("DC".equals(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTroutBkno7(SysStatus.getPropertyValue().getSysstatHbkno() + "9999");
                }
            } else { //轉帳類交易
                if ("T5".equals(getFeptxn().getFeptxnTxCode()) || "T7".equals(getFeptxn().getFeptxnTxCode())) { //15類，自繳稅
                    getFeptxn().setFeptxnPaytype(FADATA.substring(2, 7)); //繳款類別
                    getFeptxn().setFeptxnTaxUnit(TADATA.substring(0, 3)); //稽徵機關
                    getFeptxn().setFeptxnIdno(TADATA.substring(3, 14)); //身分證統編
                    // 轉入銀行代號=財金
                    getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatFbkno());
                    getFeptxn().setFeptxnTroutBkno7(getFeptxn().getFeptxnTroutBkno() + "0000");
                } else if ("T6".equals(getFeptxn().getFeptxnTxCode()) || "T8".equals(getFeptxn().getFeptxnTxCode())) { //非15類，核定稅
                    getFeptxn().setFeptxnPaytype(FADATA.substring(2, 7)); //繳款類別
                    //繳款期限，轉西元年
                    getFeptxn().setFeptxnDueDate(String.valueOf(Integer.parseInt(FADATA.substring(7, 9)) + 2011) + FADATA.substring(9, 13));        //繳款期限,轉西元年
                    getFeptxn().setFeptxnReconSeqno(StringUtils.trim(TADATA));    //銷帳編號 // [20221214]修改。SA Dolly
                    getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatFbkno());    //轉入銀行代號=財金
                    getFeptxn().setFeptxnTrinActno(StringUtils.rightPad(getFeptxn().getFeptxnReconSeqno(), 16, "0"));        //轉入帳號=銷帳編號  /* 補滿 16位, 右補 0 */
                    getFeptxn().setFeptxnTroutBkno7(getFeptxn().getFeptxnTroutBkno() + "0000");
                } else if ("T".equals(getFeptxn().getFeptxnTxCode().substring(0, 1))) { //轉帳交易
                    if (PICCPCOD.equals("2523")) {
                        getFeptxn().setFeptxnTrinBkno(getFeptxn().getFeptxnTroutBkno());
                        getFeptxn().setFeptxnTrinActno(StringUtils.leftPad(TADATA.trim(), 16, "0"));
                    } else if (PICCPCOD.equals("2522")) {
                        getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
                        getFeptxn().setFeptxnTrinActno(StringUtils.leftPad(TADATA.trim(), 16, "0"));
                    } else {
                        if (StringUtils.isBlank(FADATA)) {
                            //轉入行
                            getFeptxn().setFeptxnTrinBkno(TADATA.substring(0, 3));
                            getFeptxn().setFeptxnTrinActno(StringUtils.leftPad(TADATA.substring(3,19).trim(),16,"0"));
                        } else {
                            //轉入他行
                            getFeptxn().setFeptxnTrinBkno(FADATA.substring(2, 5));
                            getFeptxn().setFeptxnTrinActno(StringUtils.leftPad(TADATA.trim(), 16, "0"));
                        }
                    }
                } else if ("E".equals(getFeptxn().getFeptxnTxCode().substring(0, 1))) {    //全國性繳費
                    getFeptxn().setFeptxnPaytype(FADATA.substring(0, 5));        //繳款類別
                    getFeptxn().setFeptxnDueDate(FADATA.substring(5, 13));       //繳款期限(西元年)
                    getFeptxn().setFeptxnBusinessUnit(FADATA.substring(13, 21)); //委託單位代號
                    getFeptxn().setFeptxnReconSeqno(TADATA.substring(0, 16));    //銷帳編號
                    getFeptxn().setFeptxnTrinActno(getFeptxn().getFeptxnReconSeqno());
                    getFeptxn().setFeptxnPayno(TADATA.substring(16, 20));        //費用代號
                    getFeptxn().setFeptxnFiscRrn(getFeptxn().getFeptxnBusinessUnit() + getFeptxn().getFeptxnPayno());
                    getFeptxn().setFeptxnRemark(getFeptxn().getFeptxnPayno() + getFeptxn().getFeptxnReconSeqno() + getFeptxn().getFeptxnDueDate());

                    if(StringUtils.isNotBlank(IPYDATA)){
                        getFeptxn().setFeptxnTrinBkno(IPYDATA.substring(0, 3));
                    }
                } else if ("W2".equals(getFeptxn().getFeptxnTxCode())) {
                	// 無卡提款Q1(QRCODE被掃、Q2(QRCODE主掃)
                	getFeptxn().setFeptxnActivityCode(FADATA.substring(0, 2));
                }
            }
            /* For  AW(無卡提款申請)、WF(指靜脈提款前置交易)、
            P4(指靜脈密碼變更及前置交易) /指靜脈相關建置交易(F?) */
            if ( "AW".equals(getFeptxn().getFeptxnTxCode()) ||  "WF".equals(getFeptxn().getFeptxnTxCode()) ||
            		 "P4".equals(getFeptxn().getFeptxnTxCode()) ||  "FP".equals(getFeptxn().getFeptxnTxCode()) ||
            		 "FV".equals(getFeptxn().getFeptxnTxCode()) || "FA".equals(getFeptxn().getFeptxnTxCode()) ||
            		 "FD".equals(getFeptxn().getFeptxnTxCode()) || "FS".equals(getFeptxn().getFeptxnTxCode()) ||
            		 "FX".equals(getFeptxn().getFeptxnTxCode())) { 
                getFeptxn().setFeptxnIdno(atmReq.getSPECIALDATA().substring(53, 63)); // ATM_TITA.SPECIALDATA[54:10]  '身分證ID
            }else if ("WP".equals(getFeptxn().getFeptxnTxCode())) {
                getFeptxn().setFeptxnIdno(atmReq.getFADATA().substring(2, 12)); // ATM_TITA.FADATA[3:10]  '身分證ID
            }
            /* For  LF(金融帳戶核驗) */
            if ("LF".equals(getFeptxn().getFeptxnTxCode())) {
            	//活動代碼：TT (全民普發1萬)，FD ( FIDO)
            	getFeptxn().setFeptxnActivityCode(atmReq.getFADATA().substring(0, 2));
            	getFeptxn().setFeptxnIdno(atmReq.getFADATA().substring(2, 12)); //身分證ID
                if (feptxn.getFeptxnActivityCode().equals("TT")) {
                    String trk3 = "10" +  /*業務類別: 10跨行金融帳戶資訊核驗 */
                            "00" +  /* 交易類別: 00晶片卡核驗 */
                            "01" +  /* 核驗項目: 01 */
                            getFeptxn().getFeptxnIdno();  /* 左靠右補空白 */
                    trk3 = StringUtils.rightPad(trk3, 88, " ")  /* 總長度補滿至88 */
                            + "99";  /* 89-90全民普發 */
                    getFeptxn().setFeptxnTrk3(trk3);
                } else if ("FD".equals(feptxn.getFeptxnActivityCode())) { //FIDO
                    String trk3 = "10" +  /*業務類別: 10跨行金融帳戶資訊核驗 */
                            "00" +  /* 交易類別: 00晶片卡核驗 */
                            "01" +  /* 核驗項目: 01 */
                            getFeptxn().getFeptxnIdno();  /* 左靠右補空白 */
                    trk3 = StringUtils.rightPad(trk3, 88, " ")  /* 總長度補滿至88 */
                            + "01";  /* 89-90核驗用途 */
                    getFeptxn().setFeptxnTrk3(trk3);
                } else {
                    String trk3 = "10" +  /*業務類別: 10跨行金融帳戶資訊核驗 */
                            "00" +  /* 交易類別: 00晶片卡核驗 */
                            "01" +  /* 核驗項目: 01 */
                            getFeptxn().getFeptxnIdno();  /* 左靠右補空白 */
                    trk3 = StringUtils.rightPad(trk3, 90, " ");  /* 總長度補滿至90 */
                    getFeptxn().setFeptxnTrk3(trk3);
                }
            }

            // 前置交易FT1(FSCODE：01) 、FT2(FSCODE：02)
            if ("01".equals(getFeptxn().getFeptxnTxCode())){
                feptxn.setFeptxnTrinBkno(FADATA.substring(2, 5)); //轉入銀行代號
                feptxn.setFeptxnTrinActno(TADATA);                //轉入銀行帳號
            } else if ("02".equals(getFeptxn().getFeptxnTxCode())) {
                feptxn.setFeptxnTrinActno(TADATA);                //轉入手機門號
            }

            //帳戶幣別
            getFeptxn().setFeptxnTxCurAct("TWD");
            /* 交易幣別、帳戶交易金額及提領金額的處理 */
            if (ATMTXCD.US.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.JP.name().equals(getFeptxn().getFeptxnTxCode())) {
                /* 外幣以台幣帳戶扣帳，手續費幣別仍為台幣 */
                getFeptxn().setFeptxnFeeCur(CurrencyType.TWD.name());
                BigDecimal exrateB = null;
                if (StringUtils.isNotBlank(TADATA)) {
                    exrateB = new BigDecimal(StringUtils.substring(TADATA, 0, 8));
                }
                getFeptxn().setFeptxnExrate(exrateB); // 匯率
                switch (getFeptxn().getFeptxnTxCode()) {
                    case "US": //美金
                        getFeptxn().setFeptxnTxCur(CurrencyType.USD.name());
                        break;
                    case "JP": //日幣
                        getFeptxn().setFeptxnTxCur(CurrencyType.JPY.name());
                        break;
                }
                getFeptxn().setFeptxnTxAmtAct(AMTDISP);
                getFeptxn().setFeptxnTxAmt(AMTNOND);
            } else {
                getFeptxn().setFeptxnTxCur("TWD");
                if (!AMTDISP.toString().equals("0")) {
                    getFeptxn().setFeptxnTxAmt(AMTDISP); //提領或存入金額
                } else {
                    getFeptxn().setFeptxnTxAmt(AMTNOND); //轉帳類金額
                }
                //帳戶交易金額
                getFeptxn().setFeptxnTxAmtAct(getFeptxn().getFeptxnTxAmt());
            }

            //晶片卡交易序號
            if (StringUtils.isNotBlank(PICCBI9)) {
                getFeptxn().setFeptxnIcSeqno(PICCBI9);
            }
            //端末設備查核碼
            if (StringUtils.isNotBlank(atmReq.getPICCBI11())) {
            	getFeptxn().setFeptxnAtmChk(atmReq.getPICCBI11());
            }
            //晶片卡卡片帳號(DX：無卡存款前置—傳送簡訊驗證碼沒有卡片資料)
            if(!"DX".equals(atmReq.getFSCODE())) {
                getFeptxn().setFeptxnMajorActno(atmReq.getCARDDATA().substring(21, 37));//[22:16]
            }
            if("DA".equals(atmReq.getFSCODE()) || "DC".equals(atmReq.getFSCODE())) {
            	String ICMEMO = "0" + feptxn.getFeptxnTroutBkno() + 
            			StringUtils.leftPad(feptxn.getFeptxnTroutActno(), 16, "0") + 
            			StringUtils.repeat("0", 40);
            	getFeptxn().setFeptxnIcmark(ICMEMO);
            	//交易接收銀行
            	getFeptxn().setFeptxnDesBkno(getFeptxn().getFeptxnTrinBkno());
            }else if (StringUtils.isNotBlank(PICCBI55)) {    //不為空
                //晶片卡 REMARK 欄位(進AA未轉ASC)
                getFeptxn().setFeptxnIcmark(PICCBI55);
            }
            //ATM上送TAC值(不含TAC Len)
            if (StringUtils.isNotBlank(PICCTAC)) { 
            	getFeptxn().setFeptxnIcTac(PICCTAC.substring(4, 20));
            }
            //餘額查詢(其他帳號)、存款或轉帳的入帳帳號
            getFeptxn().setFeptxnTxActno(StringUtils.substring(TADATA.trim(),0,16));
            // ATM回應代碼
            getFeptxn().setFeptxnExcpCode(STATUS.substring(0, 2)); //ATM上送交易STATUS
           
            // 財金交易代號
            if("WF".equals(FSCODE)) {
            	//電文未給，FEP補上
            	 getFeptxn().setFeptxnPcode("2510");
            }else {
            	 getFeptxn().setFeptxnPcode(PICCPCOD);
            }
           

            /* for 財金公司手機門號跨行轉帳 */
            if ("TM".equals(TACODE)) { //手機門號轉帳
                getFeptxn().setFeptxnTrinBkno7(getFeptxn().getFeptxnTrinBkno() + "8999");
                getFeptxn().setFeptxnTelephone(TADATA.trim().substring(TADATA.trim().length() - 10));
                getFeptxn().setFeptxnMtp("Y");
            }
            getFeptxn().setFeptxnAtmZone(ZoneCode.TWN);

            /* EATM 通道的電文處理*/
            if ("EAT".equals(getAtmTxData().getTxChannel().name())) { /* EATM */
                getFeptxn().setFeptxnMsgkind(header.getMSGKIND());     //訊息種類
                getFeptxn().setFeptxnAtmBrno(header.getBRANCHID());    //分行代號
                getFeptxn().setFeptxnChannelEjfno(header.getCLIENTTRACEID());    //外部通道電子日誌序號
                // 轉出附言欄
                getFeptxn().setFeptxnFmrem(EbcdicConverter.changeChinese(body.getPIEFNOTE())); //給轉出方備註
                // 中文附言欄
                getFeptxn().setFeptxnChrem(EbcdicConverter.changeChinese(body.getPIETNOTE())); //給轉入方備註
                if("RV".equals(atmReq.getLANGID())) {
                	getFeptxn().setFeptxnOrderDate(atmReq.getPIEODT().substring(0, 8)); //預約日期
                	getFeptxn().setFeptxnStan(atmReq.getPIEOSTAN()); //預約交易由前端提供
                }
            }

            // 是否跨行
            if("WP".equals(feptxn.getFeptxnTxCode().trim())) {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
                // 子系統
                getFeptxn().setFeptxnSubsys(getAtmTxData().getMsgCtl().getMsgctlSubsys());
            } else if ((StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno())
            		&& !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()))
                    || (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
            )) {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
                // 子系統
                getFeptxn().setFeptxnSubsys((short)1);
            } else {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
                // 子系統
                getFeptxn().setFeptxnSubsys(getAtmTxData().getMsgCtl().getMsgctlSubsys());
            }
            //代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(getAtmTxData().getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());    //財金營業日
            } else {
                /* 2/4 修改判斷為代理交易 */
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) &&
                        StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())
                ) {
                    getFeptxn().setFeptxnTbsdyFisc(getAtmTxData().getTbsdyFISC());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ATM_Request); // "A1" '(ATM REQUEST)
            getFeptxn().setFeptxnTxDatetimeFisc(PICCBI19); //ATM交易日期時間
            if (DbHelper.toBoolean(getAtmTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
            	getFeptxn().setFeptxnWay((short) 3);
            }

            /* 分PCODE上線的交易，Y:主機交易,N:FEP交易*/
            // Channel：ATM可分PCODE上線，Y：交易轉送主機處理，N：FEP處理
            // Channel：非ATM(EATM/POS)，沒有分PCODE上線機制 */
            if(StringUtils.equals(getFeptxn().getFeptxnChannel(), "ATM")   ) {
            	getFeptxn().setFeptxnCbsProc(getAtmTxData().getMsgCtl().getMsgctlCbsProc());
            } else {
            	getFeptxn().setFeptxnCbsProc("N");
            }
            
            return rtnCode;
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    public FEPReturnCode prepareOtherFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getFeptxn().setFeptxnMsgid(getAtmTxData().getMessageID()); // 訊息代號
            getFeptxn().setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); // 交易時間
            /*以上二個欄位只需 GET 一次 SYSTEM DATETIME*/
            //ATM交易日期(電文資料是西元後兩碼+月日共6碼)
            getFeptxn().setFeptxnTxDateAtm("20" + atmReq.getTRANDATE().substring(0, 2) + atmReq.getTRANDATE().substring(2, 6)); // ATM交易日期(電文資料是西元後兩碼+月日共6碼)
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + atmReq.getTRANTIME());
            getFeptxn().setFeptxnEjfno(getAtmTxData().getEj()); // 電子日誌序號
            //交易序號給號
            if (!"FC".equals( (atmReq.getMSGCAT() + atmReq.getMSGTYP().substring(0, 1)) )) {
            	 //一般交易給號：0000001~8999999、A000001~F999999
            	 getFeptxn().setFeptxnStan(getStan());
            }
            //依ATM交易判斷欄位存FEPTXN
            //ATM 交易代號
			getFeptxn().setFeptxnTxCode(atmReq.getFSCODE());
			// ATM交易序號
			getFeptxn().setFeptxnAtmSeqno(atmReq.getTRANSEQ());
            // ATM上送交易STATUS
            getFeptxn().setFeptxnExcpCode(atmReq.getSTATUS().substring(0, 2));
           
            String cattyp = atmReq.getMSGCAT() + atmReq.getMSGTYP();
            String fsCode = atmReq.getFSCODE();
            String tmAscii = EbcdicConverter.fromHex(CCSID.English, getAtmTxData().getTxRequestMessage());
            
            //磁條卡密碼變更
            if (cattyp.equals("FC1") || ("P1".equals(fsCode) && !StringUtils.equals("K 200", tmAscii.substring(179, 184))) ) { //磁條密碼變更&前置FC1
                getFeptxn().setFeptxnTroutBkno(atmReq.getCARDPART1().substring(3, 6));
                getFeptxn().setFeptxnMajorActno(atmReq.getCARDPART1().substring(6, 19));
                getFeptxn().setFeptxnCardSeq(Short.valueOf(atmReq.getCARDPART1().substring(19, 21)));
            }else if(fsCode.equals("FV")) { //指靜脈建置(非前置)
            	 getFeptxn().setFeptxnMajorActno(atmReq.getFADATA());
            }else if(!cattyp.equals("FC6") && !fsCode.equals("FV")) {//企業無卡存款前置～查詢企業名稱(FC6)、指靜脈建置(FV)，此兩種無卡交易不給值
            	 //卡片帳號
                getFeptxn().setFeptxnMajorActno(atmReq.getCARDDATA().substring(21, 37));//[22:16]
            }
            
            //企業無卡存款前置～查詢企業名稱FC6(待實際電文確認)
            if (cattyp.equals("FC6")) {
                getFeptxn().setFeptxnTrinBkno(atmReq.getT3BNKID().trim());
                getFeptxn().setFeptxnTrinActno(atmReq.getTADATA().trim());
                getFeptxn().setFeptxnTxCur("TWD");
                //右邊兩位是小數位，需轉換
				getFeptxn().setFeptxnTxAmt(atmReq.getAMTDISP().divide(new BigDecimal(100)));
            }
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnAtmno(getAtmTxData().getAtmNo()); // 櫃員機代號 // [20221201]修改程式碼: 改成getAtmTxData().getAtmNo()
            getFeptxn().setFeptxnTxrust("0"); // 處理結果
            getFeptxn().setFeptxnChannel(getAtmTxData().getTxChannel().name()); //通道別
            Zone defZone = getZoneByZoneCode(atmMstr.getAtmZone());
            if (defZone == null) {
                return IOReturnCode.ZONENotFound;
            }
            if (defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
            }
            if (StringUtils.isNotBlank(defZone.getZoneTbsdy())) {
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy());    //自行營業日
            }
            // 子系統
            getFeptxn().setFeptxnSubsys(getAtmTxData().getMsgCtl().getMsgctlSubsys());
            getFeptxn().setFeptxnAtmZone("TWN");
            // 自行交易
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));
            // 財金營業日
            getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
            getFeptxn().setFeptxnMsgflow("A1");
            if (DbHelper.toBoolean(getAtmTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
            	if ( getFeptxn().getFeptxnFiscFlag() == 0 && DbHelper.toBoolean(getAtmTxData().getMsgCtl().getMsgctlAtm2way())) {
            		 getFeptxn().setFeptxnWay((short) 2);
            	}else {
            		getFeptxn().setFeptxnWay((short) 3);
            	}
            }
            /* 分PCODE上線的交易，Y:主機交易,N:FEP交易*/
            // Channel：ATM可分PCODE上線，Y：交易轉送主機處理，N：FEP處理
            // Channel：非ATM(EATM/POS)，沒有分PCODE上線機制 */
            if(StringUtils.equals(getFeptxn().getFeptxnChannel(), "ATM")   ) {
            	getFeptxn().setFeptxnCbsProc(getAtmTxData().getMsgCtl().getMsgctlCbsProc());
            } else {
            	getFeptxn().setFeptxnCbsProc("N");
            }
            return rtnCode;
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 寫入ATM清算檔
     *
     * @param itype 1:入帳 2:沖正 3:換日後交易
     * @return 錢箱分八個且幣別可能不同，因此繳庫金額與裝鈔金額以陣列暫存 且錢箱幣別也以陣列來存，最後才以幣別為準來總計
     * 若有三個幣別則會一次寫入三筆資料入ATMC
     */
    public FEPReturnCode insertATMC(int itype) {
        @SuppressWarnings("unused")
        String tita = getAtmTxData().getTxResponseMessage(); // 第一階段整串ATM電文上優利主機
        ATMTXCD txid = ATMTXCD.parse(getFeptxn().getFeptxnTxCode());
        String wBrnoSt = ""; // 清算分行暫存變數
        String wBrnoSt2 = ""; // 清算分行暫存變數2
        ArrayList<Integer> wTbckAmt = new ArrayList<Integer>(); // 繳庫金額暫存陣列
        ArrayList<Integer> wTrwtAmt = new ArrayList<Integer>(); // 裝鈔金額暫存陣列
        ArrayList<Integer> wTttiAmt = new ArrayList<Integer>(); // 剩餘金額暫存陣列
        ArrayList<String> wBoxCur = new ArrayList<String>();// 錢箱幣別暫存陣列
        String wZoneSt = "";
        String wZoneCur = ""; // 跨區手續費幣別暫存變數
        @SuppressWarnings("unused")
        int wFee = 0; // 跨區手續費
        byte wSelf1 = 0;
        int iCur = 0; // 幣別種類暫存
        try {
            Zone defZone = new Zone();
            Atmc defAtmc = new Atmc();

            // 2010-04-08 modify by Kyo for SPEC 新增邏輯
            if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag(), false) && BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
                return CommonReturnCode.Normal;
            }

            // BugReport(001B0022):WEBATM不用取得鈔箱檔
            if (!FEPChannel.EAT.name().equals(getFeptxn().getFeptxnChannel())) {
                List<Atmcash> atmcashList = atmcashExtMapper.getAtmCashByAtmNo(atmMstr.getAtmAtmno(), "ATMCASH_BOXNO");
                if (CollectionUtils.isEmpty(atmcashList)) {
                    // 2010-06-01 by kyo 程式調整
                    return IOReturnCode.ATMCASHNotFound;
                }
                // 2010-05-19 補上TTI剩餘金額累計 for 換日後第一筆TTI交易使用
                for (int iBoxNo = 0; iBoxNo < atmcashList.size(); iBoxNo++) {
                    if (iBoxNo == 0) {
                        wBoxCur.add(atmcashList.get(iBoxNo).getAtmcashCur());
                        wTbckAmt.add(atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashLeftLbsdy().intValue());
                        wTrwtAmt.add(atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashRefill().intValue());
                        wTttiAmt.add(
                                atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * (atmcashList.get(iBoxNo).getAtmcashRefill().intValue() - atmcashList.get(iBoxNo).getAtmcashPresent().intValue()));
                    } else {
                        // BugReport(001B0343):當有ATMCash中幣別為空字串時不列入計算
                        if (StringUtils.isBlank(atmcashList.get(iBoxNo).getAtmcashCur())) {
                            iCur = wBoxCur.indexOf(atmcashList.get(iBoxNo).getAtmcashCur());
                            if (iCur != -1) {
                                wTbckAmt.set(iCur, wTbckAmt.get(iCur).intValue() + atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashLeftLbsdy().intValue());
                                // BugReport(001B0319):誤搬至wTbckAmt中造成遺漏
                                wTrwtAmt.set(iCur, wTrwtAmt.get(iCur).intValue() + atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashRefill().intValue());
                                wTttiAmt.set(iCur, wTttiAmt.get(iCur).intValue() + (atmcashList.get(iBoxNo).getAtmcashUnit().intValue()
                                        * (atmcashList.get(iBoxNo).getAtmcashRefill().intValue() - atmcashList.get(iBoxNo).getAtmcashPresent().intValue())));
                            } else {
                                wBoxCur.add(atmcashList.get(iBoxNo).getAtmcashCur());
                                wTbckAmt.add(atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashLeftLbsdy().intValue());
                                wTrwtAmt.add(atmcashList.get(iBoxNo).getAtmcashUnit().intValue() * atmcashList.get(iBoxNo).getAtmcashRefill().intValue());
                                wTttiAmt.add(atmcashList.get(iBoxNo).getAtmcashUnit().intValue()
                                        * (atmcashList.get(iBoxNo).getAtmcashRefill().intValue() - atmcashList.get(iBoxNo).getAtmcashPresent().intValue()));
                            }
                        }
                    }
                }
            }

            // ----------同區交易, 判斷 ATM 所在清算分行及主清算幣別----------Start
            // 條件成立 表示"同區交易"
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno()) && StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {

                // 2010-04-08 modify by Kyo for SPEC 新增邏輯
                wBrnoSt = getFeptxn().getFeptxnAtmBrno(); // 清算分行
                if (itype == ATMCTxType.Accounting.getValue()) {// 表記帳
                    getFeptxn().setFeptxnCbsDscpt(getAtmTxData().getMsgCtl().getMsgctlCbsDscpt());

                    // ACW/AFT 須判斷不同卡別, 寫入 ATMC
                    if (!ATMTXCD.ACW.name().equals(getFeptxn().getFeptxnTxCode()) && !ATMTXCD.ATF.name().equals(getFeptxn().getFeptxnTxCode())) {
                        getFeptxn().setFeptxnTxCodeAtmc(getAtmTxData().getMsgCtl().getMsgctlTxCodeAtmc());
                    }

                    // 2010-03-30 modify by kyo for spec logic modified
                    // Combo卡預借現金(IWD), 寫入 ATMC為MWD
                    if (ATMTXCD.IWD.name().equals(getFeptxn().getFeptxnTxCode()) && BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {
                        getFeptxn().setFeptxnTxCodeAtmc("MWD");
                    }
                    // 轉帳加值GIFT 卡(IFT), 寫入 ATMC為GFT
                    // BugReport(001B0116):應該使用轉入帳號判斷而不是轉出
                    if (ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode()) && BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
                        getFeptxn().setFeptxnTxCodeAtmc("GFT");
                    }
                    // Combo卡預現轉帳(IFT), 寫入 ATMC為MFT
                    if (ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode()) && BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {
                        getFeptxn().setFeptxnTxCodeAtmc("MFT");
                        // 自行-摘要 210
                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                            getFeptxn().setFeptxnCbsDscpt("210");
                        } else {
                            // 跨行-摘要 131
                            getFeptxn().setFeptxnCbsDscpt("131");
                        }
                    }
                    // 2010-03-30 modify by kyo for spec logic modified
                    // 台幣戶提外幣(IFW), 寫入 ATMC 為 FWD
                    if (ATMTXCD.IFW.name().equals(getFeptxn().getFeptxnTxCode()) && ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())
                            && StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {
                        getFeptxn().setFeptxnTxCodeAtmc("FWD");
                        getFeptxn().setFeptxnCbsDscpt("001");
                    }

                    // 自行
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                        // 2016/12/28 Modify by Ruling for 自行EMV預借現金(EAV/EAM)
                        // 成立表示為"預借現金"交易
                        if (txid == ATMTXCD.CAV || txid == ATMTXCD.CAM || txid == ATMTXCD.CAJ || txid == ATMTXCD.CAA || txid == ATMTXCD.EAV || txid == ATMTXCD.EAM) {
//                            if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAscChannel(), false)) {
//                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IssuerBank.getValue()); // 本行
//                            } else {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue()); // 聯行
//                            }
                        }
                        // 判斷轉出帳號
                        if (StringUtils.isBlank(getFeptxn().getFeptxnTroutActno())) {
                            if (getFeptxn().getFeptxnTroutActno().substring(0, 2).equals("00") && getFeptxn().getFeptxnTroutActno().substring(2, 5).equals(wBrnoSt)) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IssuerBank.getValue()); // 本行
                                // BugReport(001B0254):與SA確認後判斷轉入帳號非空，才跑跨行邏輯
                                // BugReport(001B0153):判斷邏輯寫錯<>寫成=
                            } else if (!getFeptxn().getFeptxnTroutActno().substring(0, 2).equals("00") && StringUtils.isBlank(getFeptxn().getFeptxnTrinBkno())
                                    && StringUtils.isBlank(getFeptxn().getFeptxnTrinBkno()) && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue()); // 跨行
                            } else {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue()); // 聯行
                            }

                            // add by Maxine for 12/6 修改 for 跨行預約轉帳 */
                            if (getFeptxn().getFeptxnTxCode().equals("IPA") && !StringUtils.isBlank(getFeptxn().getFeptxnTrinBkno())
                                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue()); // 跨行
                            }

                            // 2016/09/07 Modify by Ruling for ATM新功能-跨行存款
                            if (getFeptxn().getFeptxnTxCode().equals("ODR") && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue()); // 跨行
                            }

                            // BugReport(001B0110):CDF僅有轉入帳號有值，又因為CDF轉出銀行為807因此補上此邏輯。
                        } else if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
                            if (getFeptxn().getFeptxnTrinActno().substring(0, 2).equals("00") && getFeptxn().getFeptxnTrinActno().substring(2, 5).equals(wBrnoSt)) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IssuerBank.getValue()); // 本行
                            } else {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue()); // 聯行
                            }
                        }
                    } else {
                        // 跨行
                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                            if (getFeptxn().getFeptxnTrinActno().substring(0, 2).equals("00") && getFeptxn().getFeptxnTrinActno().substring(2, 5).equals(atmMstr.getAtmBrnoSt())) {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IssuerBank.getValue()); // 本行
                            } else {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue()); // 聯行
                            }
                            // 2010-07-27 by kyo for SPEC修正:7/27 修正 for 銀聯卡 (補上CUP電文判斷)
                            // 2011-05-12 by kyo for spec修正(connie):/* 2011/5/11 修正 for 國際卡提款需區分國內或國外卡 */
                            // Fly 2016/02/02 for EMV晶片卡交易
                        } else if (!StringUtils.isBlank(getFeptxn().getFeptxnPcode())
                                && (getFeptxn().getFeptxnPcode().substring(0, 2).equals("24") || getFeptxn().getFeptxnPcode().substring(0, 2).equals("26"))) {
                            if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCurSet())) {/// *清算幣別為台幣*/
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue()); /// *跨行*/
                            } else {
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InternationBank.getValue()); /// *跨國*/
                                // 2015/09/11 Modify by Ruling for EMV拒絶磁條卡交易:VISA卡跨國交易收處理費
                                if (("CWV2410".equals(getFeptxn().getFeptxnMsgid()) || "CAV2420".equals(getFeptxn().getFeptxnMsgid()) || "EWV2620".equals(getFeptxn().getFeptxnMsgid())
                                        || "EAV2622".equals(getFeptxn().getFeptxnMsgid())) && "Y".equals(getFeptxn().getFeptxnRsCode().trim())) {
                                    getFeptxn().setFeptxnSelfcd((short) SelfCDType.InternationBankHaveFee.getValue()); /// *跨國收費*/
                                }
                            }
                            // ElseIf txid = ATMTXCD.CWV OrElse txid = ATMTXCD.CWM OrElse txid = ATMTXCD.CUP Then
                            // getFeptxn().setFeptxnSelfcd(); = CType(SelfCDType.InternationBank, Byte) '跨國
                        } else {
                            getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue()); // 跨行
                        }
                        // 2012/07/03 Modifby by Ruling for 國際卡餘額查詢(IQC/IQV/IQM)，財金清算別為台幣，FEPTXN_SELFCD的值要等於4（跨國）
                        if (ATMTXCD.IQC.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.IQV.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.IQM.name().equals(getFeptxn().getFeptxnTxCode())) {
                            getFeptxn().setFeptxnSelfcd((short) SelfCDType.InternationBank.getValue()); // 跨國
                        }
                    }
                }
            }
            // ----------同區交易, 判斷 ATM 所在清算分行及主清算幣別----------End

            // ----------跨區提款交易----------Start
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno()) && StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {
                if (txid == ATMTXCD.IFW) {
                    getFeptxn().setFeptxnCbsDscpt("206");
                    getFeptxn().setFeptxnTxCodeAtmc(ATMTXCD.IFW.name());

                    defZone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
                    if (defZone == null) {
                        // 2010-06-01 by kyo 程式調整
                        return IOReturnCode.ZONENotFound;
                    } else {
//						wZoneSt = defZone.getZoneVirtualBrno();
//						wZoneCur = defZone.getZoneCur(); // 跨區手續費幣別
                        // 2010-05-22 by kyo for SPEC修改跨區手續費由SYSCONF取值
                        if (CurrencyType.TWD.name().equals(wZoneCur)) {
                            wFee = ATMPConfig.getInstance().getCrossChargeTWD(); // 跨區手續費
                        } else if (CurrencyType.HKD.name().equals(wZoneCur)) {
                            wFee = ATMPConfig.getInstance().getCrossChargeHKD(); // 跨區手續費
                        } else if (CurrencyType.MOP.name().equals(wZoneCur)) {
                            wFee = ATMPConfig.getInstance().getCrossChargeMOP(); // 跨區手續費
                        }
                    }

                    wBrnoSt2 = getFeptxn().getFeptxnAtmBrno();

                    // 香港地區
                    if (ATMZone.HKG.name().equals(getFeptxn().getFeptxnZoneCode())) {
                        // 香港卡在澳門跨區提款
                        if (ATMZone.MAC.name().equals(atmMstr.getAtmZone())) {
                            if (CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCur())) {
                                wSelf1 = 8;
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue());
                            } else if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur())) {
                                wSelf1 = 5;
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue());
                            }
                        } else if (ATMZone.TWN.name().equals(atmMstr.getAtmZone())) {
                            if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                                wSelf1 = 7;
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue());
                            }
                        }
                        // 澳門地區
                    } else if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnZoneCode())) {
                        // 澳門卡在香港跨區提款
                        if (ATMZone.HKG.name().equals(atmMstr.getAtmZone())) {
                            if (CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCurAct())
                                    && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur())) {
                                wSelf1 = 9;
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue());
                            }
                            if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCurAct())
                                    && CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur())) {
                                wSelf1 = 6;
                                getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue());
                            }
                        }
                        if (ATMZone.TWN.name().equals(atmMstr.getAtmZone())) {
                            wSelf1 = 9;
                            getFeptxn().setFeptxnSelfcd((short) SelfCDType.IntraBank.getValue());
                        }
                        // 台灣卡跨區
                    } else if (ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())) {
                        // 台灣卡在香港跨區提款與在澳門跨區提款
                        if (ATMZone.HKG.name().equals(atmMstr.getAtmZone()) || ATMZone.MAC.name().equals(atmMstr.getAtmZone())) {
                            wSelf1 = 7;
                            getFeptxn().setFeptxnSelfcd((short) SelfCDType.IssuerBank.getValue());
                        }
                    }
                } else {
                    // 2010-04-08 modify by Kyo for SPEC 新增邏輯
                    // 海外 IIQ/IQ2
                    wBrnoSt2 = getFeptxn().getFeptxnAtmBrno(); // 清算分行
                    getFeptxn().setFeptxnCbsDscpt(getAtmTxData().getMsgCtl().getMsgctlCbsDscpt());
                    getFeptxn().setFeptxnTxCodeAtmc(getAtmTxData().getMsgCtl().getMsgctlTxCodeAtmc());
                    getFeptxn().setFeptxnSelfcd((short) SelfCDType.InterBank.getValue());

                }
            }
            // ----------跨區提款交易----------End

            // ----------同區交易寫一筆ATMC----------Start
            // BugReport(001B0099): TTI僅寫入一筆剩餘金額要SUM起來
            // 2010-05-21 modify by kyo for spec modified:修改query ATMC時的條件固定為TTI，因為換日後ATM未必發送TTI
            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {
                // 2010-05-25 by kyo for SPEC修改:多幣別若使用財金前營業日會有問題，改用ZONE檔的前營業日
                defZone = getZoneByZoneCode(atmMstr.getAtmZone());
                if (defZone == null) {
                    return IOReturnCode.ZONENotFound;
                }
                if (itype == ATMCTxType.ChangeDay.getValue()) {
                    // 2010-05-24 by kyo for SPEC修改:為了卡片系統，TTI需要寫入多種幣別
                    for (int i = 0; i < wBoxCur.size(); i++) {
                        // 2010-12-09 by kyo for spec update 使用sysstat_lbsdy_fisc
                        defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                        defAtmc.setAtmcTbsdyFisc(SysStatus.getPropertyValue().getSysstatLbsdyFisc());
                        // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                        defAtmc.setAtmcBrnoSt(null);
                        defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmc.setAtmcCur(wBoxCur.get(i));
                        defAtmc.setAtmcTxCode(ATMTXCD.TTI.name());
                        defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                        defAtmc.setAtmcSelfcd((short) 0);
                        // BugReport(001B0431):TTI不需要使用迴圈，幣別僅台幣
                        // BugReport(001B0343):修改迴圈的指標
                        Atmc record = atmcExtMapper.getAtmcByConditions(
                                defAtmc.getAtmcTbsdy(),
                                defAtmc.getAtmcTbsdyFisc(),
                                defAtmc.getAtmcBrnoSt(),
                                defAtmc.getAtmcAtmno(),
                                defAtmc.getAtmcCur(),
                                defAtmc.getAtmcTxCode(),
                                defAtmc.getAtmcDscpt(),
                                defAtmc.getAtmcSelfcd());
                        if (record == null) {
                            defAtmc.setAtmcTbsdy(defZone.getZoneLbsdy());
                            defAtmc.setAtmcTbsdyFisc(SysStatus.getPropertyValue().getSysstatLbsdyFisc()); // 財金前營業日
                            defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                            defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                            defAtmc.setAtmcCur(wBoxCur.get(i));
                            defAtmc.setAtmcTxCode(ATMTXCD.TTI.name());
                            defAtmc.setAtmcDscpt("000");
                            defAtmc.setAtmcSelfcd((short) 0);
                            defAtmc.setAtmcCrCnt(1);
                            // BugReport(001B0431):對存提款機做額外處理
                            if (atmMstr.getAtmAtmtype() != null &&
                                    atmMstr.getAtmAtmtype() == ATMType.ADM.getValue() && CurrencyType.TWD.name().equals(wBoxCur.get(i))) {
                                // 2013/01/31 Modify by Ruling for 新增硬幣機(提款)
                                int w_COIN_AMT = 0;
                                if (DbHelper.toBoolean(getATMMSTR().getAtmCoin())) {
                                    List<Long> atmcoins = atmcoinExtMapper.getAtmCoinForInventoryCash(getFeptxn().getFeptxnAtmno(), atmStat.getAtmstatCrwtSeqno());
                                    if (CollectionUtils.isNotEmpty(atmcoins)) {
                                        w_COIN_AMT = atmcoins.get(i).intValue();
                                    }
                                }
                                defAtmc.setAtmcCrAmt(BigDecimal.valueOf(atmStat.getAtmstatDepAmt().doubleValue() + w_COIN_AMT + wTttiAmt.get(i).doubleValue())); // 剩餘金額

                                // '2012/10/18 Modify by Ruling for 新增硬幣機的業務
                                // defAtmc.setAtmcCrAmt(atmStat.getAtmstatDepAmt() + atmStat.ATMSTAT_CDEP_AMT + wTttiAmt(i) '剩餘金額
                            } else {
                                defAtmc.setAtmcCrAmt(BigDecimal.valueOf(wTttiAmt.get(i).doubleValue())); // 剩餘金額
                            }
                            defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                            defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                            defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                            defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                            if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                return IOReturnCode.ATMCInsertError;
                            }
                        }
                    }
                }
                if (itype == ATMCTxType.Accounting.getValue() || itype == ATMCTxType.EC.getValue()) {
                    // 裝鈔交易
                    // BugReport(001B0343):多個鈔箱發生錯誤，colletion應該使用i變數，但誤用為iCur
                    if (txid == ATMTXCD.RWT || txid == ATMTXCD.RWF || txid == ATMTXCD.RWS) {
                        getFeptxn().setFeptxnTxCodeAtmc(ATMTXCD.RWT.name());
                        getFeptxn().setFeptxnSelfcd((short) 0);
                        // 15:30前CBS摘要為001
                        if (getFeptxn().getFeptxnTxnmode() == 1) {
                            getFeptxn().setFeptxnCbsDscpt("001");
                        } else {
                            getFeptxn().setFeptxnCbsDscpt("002");
                        }

                        // 2010-05-11 modified by kyo for SPEC拆開繳庫與裝鈔邏輯
                        // 2010-05-13 modified by kyo for CODING Error繳庫金額幣別搬錯欄位
                        // 繳庫金額-邏輯處理
                        ArrayList<String> bkCur = new ArrayList<String>();
                        if (atmStat.getAtmstatBkAmtTbsdy() > 0) {
                            bkCur.add(CurrencyType.TWD.name());
                        }
                        if (atmStat.getAtmstatBkAmtUsdTbsdy() > 0) {
                            bkCur.add(CurrencyType.USD.name());
                        }
                        if (atmStat.getAtmstatBkAmtHkdTbsdy() > 0) {
                            bkCur.add(CurrencyType.HKD.name());
                        }
                        if (atmStat.getAtmstatBkAmtJpyTbsdy() > 0) {
                            bkCur.add(CurrencyType.JPY.name());
                        }
                        if (atmStat.getAtmstatBkAmtMopTbsdy() > 0) {
                            bkCur.add(CurrencyType.MOP.name());
                        }

                        for (int i = 0; i < bkCur.size(); i++) {
                            // BugReport(001B0766):2010-06-23 by kyo 裝鈔與繳庫邏輯更動不同欄位，需要清空物件，否則可能殘留之前的檔案，造成錯誤
                            defAtmc = new Atmc();
                            defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                            // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                            defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                            // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                            defAtmc.setAtmcBrnoSt(null);
                            defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                            defAtmc.setAtmcCur(bkCur.get(i));
                            defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                            defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                            defAtmc.setAtmcSelfcd(getFeptxn().getFeptxnSelfcd());
                            Atmc record = atmcExtMapper.getAtmcByConditions(
                                    defAtmc.getAtmcTbsdy(),
                                    defAtmc.getAtmcTbsdyFisc(),
                                    defAtmc.getAtmcBrnoSt(),
                                    defAtmc.getAtmcAtmno(),
                                    defAtmc.getAtmcCur(),
                                    defAtmc.getAtmcTxCode(),
                                    defAtmc.getAtmcDscpt(),
                                    defAtmc.getAtmcSelfcd());
                            if (record != null) {
                                // 更新ATMC
                                record.setAtmcDrCnt(record.getAtmcDrCnt() + 1); // 借方筆數
                                if (CurrencyType.TWD.name().equals(bkCur.get(i))) {
                                    record.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtTbsdy())); // 繳庫金額
                                } else if (CurrencyType.USD.name().equals(bkCur.get(i))) {
                                    record.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtUsdTbsdy())); // 繳庫金額
                                } else if (CurrencyType.HKD.name().equals(bkCur.get(i))) {
                                    record.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtHkdTbsdy())); // 繳庫金額
                                } else if (CurrencyType.JPY.name().equals(bkCur.get(i))) {
                                    record.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtJpyTbsdy())); // 繳庫金額
                                } else if (CurrencyType.MOP.name().equals(bkCur.get(i))) {
                                    record.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtMopTbsdy())); // 繳庫金額
                                }
                                // 2010-06-03 by kyo for 程式調整:需QUERY，因為要承接DR_CNT跟DR_AMT
                                // 2010-06-01 by kyo for 程式調整:不需QUERY，直接UPDATE
                                if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) {// 更新ATMC
                                    return IOReturnCode.ATMCUpdateError;
                                }
                                defAtmc = record;
                            } else {
                                // 寫入ATMC
                                defAtmc.setAtmcDrCnt(1); // 借方筆數
                                if (CurrencyType.TWD.name().equals(bkCur.get(i))) {
                                    defAtmc.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtTbsdy())); // 繳庫金額
                                } else if (CurrencyType.USD.name().equals(bkCur.get(i))) {
                                    defAtmc.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtUsdTbsdy())); // 繳庫金額
                                } else if (CurrencyType.HKD.name().equals(bkCur.get(i))) {
                                    defAtmc.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtHkdTbsdy())); // 繳庫金額
                                } else if (CurrencyType.JPY.name().equals(bkCur.get(i))) {
                                    defAtmc.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtJpyTbsdy())); // 繳庫金額
                                } else if (CurrencyType.MOP.name().equals(bkCur.get(i))) {
                                    defAtmc.setAtmcDrAmt(BigDecimal.valueOf(atmStat.getAtmstatBkAmtMopTbsdy())); // 繳庫金額
                                }
                                // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                                defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                                // defAtmc.ATMC_TBSDY_FISC = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC
                                defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                                defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                                defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                                defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                                defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                                if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                    // 2010-06-01 by kyo 程式調整
                                    return IOReturnCode.ATMCInsertError;
                                }
                            }
                        }
                        // 2010-05-11 modified by kyo for SPEC拆開繳庫與裝鈔邏輯
                        // 裝鈔金額-邏輯處理
                        for (int i = 0; i < wBoxCur.size(); i++) {
                            // BugReport(001B0766):2010-06-23 by kyo 裝鈔與繳庫邏輯更動不同欄位，需要清空物件，否則可能殘留之前的檔案，造成錯誤
                            defAtmc = new Atmc();
                            defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                            // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                            defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                            // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                            defAtmc.setAtmcBrnoSt(null);
                            defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                            defAtmc.setAtmcCur(wBoxCur.get(i));
                            defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                            defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                            defAtmc.setAtmcSelfcd(getFeptxn().getFeptxnSelfcd());
                            Atmc record = atmcExtMapper.getAtmcByConditions(
                                    defAtmc.getAtmcTbsdy(),
                                    defAtmc.getAtmcTbsdyFisc(),
                                    defAtmc.getAtmcBrnoSt(),
                                    defAtmc.getAtmcAtmno(),
                                    defAtmc.getAtmcCur(),
                                    defAtmc.getAtmcTxCode(),
                                    defAtmc.getAtmcDscpt(),
                                    defAtmc.getAtmcSelfcd());
                            if (record != null) {
                                // 更新ATMC
                                record.setAtmcCrCnt(record.getAtmcCrCnt() + 1); // 貸方筆數
                                record.setAtmcCrAmt(BigDecimal.valueOf(record.getAtmcCrAmt().doubleValue() + wTrwtAmt.get(i).doubleValue())); // 裝鈔金額
                                // 2010-06-03 by kyo for 程式調整:需QUERY，因為要承接DR_CNT跟DR_AMT
                                // 2010-06-01 by kyo for 程式調整:不需QUERY，直接UPDATE
                                if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) {// 更新ATMC
                                    return IOReturnCode.ATMCUpdateError;
                                }
                                defAtmc = record;
                            } else {
                                // 寫入ATMC
                                // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                                defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                                // defAtmc.ATMC_TBSDY_FISC = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC
                                defAtmc.setAtmcCrCnt(1); // 貸方筆數
                                defAtmc.setAtmcCrAmt(BigDecimal.valueOf(wTrwtAmt.get(i))); // 裝鈔金額
                                defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                                defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                                defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                                defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                                defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                                if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                    return IOReturnCode.ATMCInsertError;
                                }
                            }
                        }
                    } else {
                        // 其他交易
                        if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir())) {
                            // BugReport(001B0303):與永豐討論, 海外分行同區交易(不含提款), 以帳戶幣別寫入 ATMC
                            // BugReport(001B0446):2010-05-11 coding error 寫錯判斷邏輯，與SPEC不合
                            // BugReport(001B0463):2010-05-13 搬移此邏輯到CheckCardStatus()
                            defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                            // 2017/10/31 Modify by Ruling for ATM預現交易，改抓交易發生財金營業日
                            defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                            // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                            defAtmc.setAtmcBrnoSt(null);
                            defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                            defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCur());
                            defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                            defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                            defAtmc.setAtmcSelfcd(getFeptxn().getFeptxnSelfcd());
                            Atmc record = atmcExtMapper.getAtmcByConditions(
                                    defAtmc.getAtmcTbsdy(),
                                    defAtmc.getAtmcTbsdyFisc(),
                                    defAtmc.getAtmcBrnoSt(),
                                    defAtmc.getAtmcAtmno(),
                                    defAtmc.getAtmcCur(),
                                    defAtmc.getAtmcTxCode(),
                                    defAtmc.getAtmcDscpt(),
                                    defAtmc.getAtmcSelfcd());
                            if (record != null) {
                                // 更新ATMC
                                if (itype == ATMCTxType.Accounting.getValue()) {// 入帳
                                    // 2010-07-27 by kyo for SPEC修改:跨行交易 若為轉入交易 記錄貸方
                                    // 2010-07-26 by kyo for spec修改:7/26 修正, 存款寫入貸方筆數/金額
                                    // 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金捐款
                                    if ((ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.ODF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.DDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc()))
                                            || (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())
                                            && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()))) {
                                        record.setAtmcCrCnt(record.getAtmcCrCnt() + 1); // 貸方筆數/金額
                                        record.setAtmcCrAmt(BigDecimal.valueOf(record.getAtmcCrAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue()));
                                    } else {
                                        record.setAtmcDrCnt(record.getAtmcDrCnt() + 1);
                                        record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue()));
                                    }
                                    // 2015/09/11 Modify by Ruling for EMV拒絶磁條卡交易:VISA卡跨國交易收處理費
                                    if ((getFeptxn().getFeptxnMsgid().equals("CWV2410") || getFeptxn().getFeptxnMsgid().equals("CAV2420") || getFeptxn().getFeptxnMsgid().equals("EWV2620")
                                            || getFeptxn().getFeptxnMsgid().equals("EAV2622")) && getFeptxn().getFeptxnSelfcd() == SelfCDType.InternationBankHaveFee.getValue()) {
                                        record.setAtmcTxCntDr(record.getAtmcTxCntDr() + 1);
                                        record.setAtmcTxFeeDr(BigDecimal.valueOf(record.getAtmcTxFeeDr().doubleValue() + getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                                    }
                                } else if (itype == ATMCTxType.EC.getValue()) {// 沖正
                                    // 2010-07-27 by kyo for SPEC修改:跨行交易 若為轉入交易 記錄貸方
                                    // 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金捐款
                                    if ((ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.ODF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.DDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc()))
                                            || (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())
                                            && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()))) {
                                        record.setAtmcCrCnt(record.getAtmcCrCnt() - 1); // 貸方筆數/金額
                                        record.setAtmcCrAmt(BigDecimal.valueOf(record.getAtmcCrAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue()));
                                    } else {
                                        record.setAtmcDrCnt(record.getAtmcDrCnt() - 1);
                                        record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue()));
                                    }
                                    // 2015/09/11 Modify by Ruling for EMV拒絶磁條卡交易:VISA卡跨國交易收處理費
                                    if (("CWV2410".equals(getFeptxn().getFeptxnMsgid())
                                            || "CAV2420".equals(getFeptxn().getFeptxnMsgid())
                                            || "EWV2620".equals(getFeptxn().getFeptxnMsgid())
                                            || "EAV2622".equals(getFeptxn().getFeptxnMsgid()))
                                            && getFeptxn().getFeptxnSelfcd() == SelfCDType.InternationBankHaveFee.getValue()) {
                                        record.setAtmcTxCntDr(record.getAtmcTxCntDr() - 1);
                                        record.setAtmcTxFeeDr(BigDecimal.valueOf(record.getAtmcTxFeeDr().doubleValue() - getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                                    }
                                }
                                // 2010-06-03 by kyo for 程式調整:需QUERY，因為要承接DR_CNT跟DR_AMT
                                // 2010-06-01 by kyo for 程式調整:不需QUERY，直接UPDATE
                                if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) {// 更新ATMC
                                    return IOReturnCode.ATMCUpdateError;
                                }
                                defAtmc = record;
                            } else {
                                // 寫入ATMC
                                if (itype == ATMCTxType.Accounting.getValue()) {// 入帳
                                    // 2010-07-26 by kyo for spec修改:7/26 修正, 存款寫入貸方筆數/金額
                                    // 2010-07-27 by kyo for SPEC修改:跨行交易 若為轉入交易 記錄貸方
                                    // 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金捐款
                                    if ((ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.ODF.name().equals(getFeptxn().getFeptxnTxCodeAtmc())
                                            || ATMTXCD.DDF.name().equals(getFeptxn().getFeptxnTxCodeAtmc()))
                                            || (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())
                                            && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()))) {
                                        defAtmc.setAtmcCrCnt(1);
                                        defAtmc.setAtmcCrAmt(getFeptxn().getFeptxnTxAmt());
                                    } else {
                                        defAtmc.setAtmcDrCnt(1);
                                        defAtmc.setAtmcDrAmt(getFeptxn().getFeptxnTxAmt());
                                    }
                                    // 2015/09/11 Modify by Ruling for EMV拒絶磁條卡交易:VISA卡跨國交易收處理費
                                    if (("CWV2410".equals(getFeptxn().getFeptxnMsgid())
                                            || "CAV2420".equals(getFeptxn().getFeptxnMsgid())
                                            || "EWV2620".equals(getFeptxn().getFeptxnMsgid())
                                            || "EAV2622".equals(getFeptxn().getFeptxnMsgid()))
                                            && getFeptxn().getFeptxnSelfcd() == SelfCDType.InternationBankHaveFee.getValue()) {
                                        defAtmc.setAtmcTxCntDr(1);
                                        defAtmc.setAtmcTxFeeDr(getFeptxn().getFeptxnFeeCustpay());
                                    }
                                    // 2017/10/31 Modify by Ruling for ATM預現交易，改抓交易發生財金營業日
                                    defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                                    // defAtmc.ATMC_TBSDY_FISC = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC
                                    defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                                    defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                                    defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                                    defAtmc.setAtmcBrnoSt(wBrnoSt);
                                    defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                                    if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                        return IOReturnCode.ATMCInsertError;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // ----------同區交易寫一筆ATMC----------End

            // ----------跨區提款交易寫二筆 ATMC，其他交易寫一筆----------Start
            // 跨區交易
            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir()) && (itype == ATMCTxType.Accounting.getValue() || itype == ATMCTxType.EC.getValue())) {
                if (ATMTXCD.IFW.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 寫入帳戶地區清算分行
                    // 2010-12-08 by ed for spec /* 10/7 修改為卡片所在地區營業日 */
                    defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdyAct());
                    // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                    defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                    // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                    defAtmc.setAtmcBrnoSt(null);
                    defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                    defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCurAct());
                    defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                    defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                    defAtmc.setAtmcSelfcd((short) wSelf1);
                    Atmc record = atmcExtMapper.getAtmcByConditions(
                            defAtmc.getAtmcTbsdy(),
                            defAtmc.getAtmcTbsdyFisc(),
                            defAtmc.getAtmcBrnoSt(),
                            defAtmc.getAtmcAtmno(),
                            defAtmc.getAtmcCur(),
                            defAtmc.getAtmcTxCode(),
                            defAtmc.getAtmcDscpt(),
                            defAtmc.getAtmcSelfcd());
                    if (record != null) {
                        // 更新ATMC
                        if (itype == ATMCTxType.Accounting.getValue()) {
                            record.setAtmcDrCnt(record.getAtmcDrCnt() + 1); // 借方筆數
                            // 2010-06-29 by kyo for spec修改:修正跨區提款 ATMC 算法
                            if (getFeptxn().getFeptxnTxCur().equals(getFeptxn().getFeptxnTxCurAct())) {
                                record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue())); // 借方金額
                            } else {
                                record.setAtmcDrAmt(
                                        BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() + getFeptxn().getFeptxnTxAmtAct().doubleValue() - getFeptxn().getFeptxnFeeCustpayAct().doubleValue())); // 借方金額(不含手續費)
                            }
                            record.setAtmcTxCntDr(record.getAtmcTxCntDr() + 1);
                            record.setAtmcTxFeeDr(BigDecimal.valueOf(record.getAtmcTxFeeDr().doubleValue() + getFeptxn().getFeptxnFeeCustpayAct().doubleValue())); // 借方手續費金額
                        } else if (itype == ATMCTxType.EC.getValue()) {// 沖正
                            record.setAtmcDrCnt(record.getAtmcDrCnt().intValue() - 1); // 借方筆數
                            // 2010-06-29 by kyo for spec修改:修正跨區提款 ATMC 算法
                            if (getFeptxn().getFeptxnTxCur().equals(getFeptxn().getFeptxnTxCurAct())) {
                                record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue())); // 借方金額
                            } else {
                                record.setAtmcDrAmt(
                                        BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() - (getFeptxn().getFeptxnTxAmtAct().doubleValue() - getFeptxn().getFeptxnFeeCustpayAct().doubleValue()))); // 借方金額(不含手續費)
                            }
                            record.setAtmcTxCntDr(record.getAtmcTxCntDr().intValue() - 1);
                            record.setAtmcTxFeeDr(BigDecimal.valueOf(record.getAtmcTxFeeDr().doubleValue() - getFeptxn().getFeptxnFeeCustpayAct().doubleValue())); // 借方手續費金額
                        }
                        // 2010-06-03 by kyo for 程式調整:需QUERY，因為要承接DR_CNT跟DR_AMT
                        // 2010-06-01 by kyo for 程式調整:不需QUERY，直接UPDATE
                        if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) { // 更新ATMC
                            return IOReturnCode.ATMCUpdateError;
                        }
                        defAtmc = record;
                    } else {
                        // 寫入ATMC
                        if (itype == ATMCTxType.Accounting.getValue()) {
                            // 2010-12-08 by ed for spec /* 10/7 修改為卡片所在地區營業日 */
                            defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdyAct());
                            // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                            defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                            // defAtmc.ATMC_TBSDY_FISC = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC
                            defAtmc.setAtmcBrnoSt(wZoneSt);
                            defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                            defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCurAct());
                            defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCode());
                            defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                            ;
                            defAtmc.setAtmcSelfcd((short) wSelf1);
                            defAtmc.setAtmcDrCnt(1);
                            // 2010-06-29 by kyo for spec修改:修正跨區提款 ATMC 算法
                            if (getFeptxn().getFeptxnTxCur().equals(getFeptxn().getFeptxnTxCurAct())) {
                                defAtmc.setAtmcDrAmt(getFeptxn().getFeptxnTxAmt()); // 借方金額
                            } else {
                                defAtmc.setAtmcDrAmt(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmtAct().doubleValue() - getFeptxn().getFeptxnFeeCustpayAct().doubleValue())); // 借方金額(不含手續費)
                            }
                            defAtmc.setAtmcTxCntDr(1);
                            defAtmc.setAtmcTxFeeDr(getFeptxn().getFeptxnFeeCustpayAct());
                            defAtmc.setAtmcLoc((short) 9); // 行外記號
                            defAtmc.setAtmcZone(getFeptxn().getFeptxnZoneCode()); // ATM所在地區
                            defAtmc.setAtmcCurSt(getFeptxn().getFeptxnFeeCur()); // 跨區手續費幣別
                            defAtmc.setAtmcCrossFlag((short) 1); // 跨區清算記號
                            if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                return IOReturnCode.ATMCInsertError;
                            }
                        }
                    }
                }

                // 2010-06-29 by kyo 寫入清算分行邏輯更動不同欄位，需要清空物件，否則可能殘留之前的檔案，造成錯誤
                defAtmc = new Atmc();
                // 寫入ATM清算分行
                defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                // 2010-06-01 by kyo for SPEC修改:清算分行欄位沒有建立INDEX且非必要欄位故移除
                defAtmc.setAtmcBrnoSt(null);
                defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCur());
                defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                defAtmc.setAtmcSelfcd(getFeptxn().getFeptxnSelfcd());
                Atmc record = atmcExtMapper.getAtmcByConditions(
                        defAtmc.getAtmcTbsdy(),
                        defAtmc.getAtmcTbsdyFisc(),
                        defAtmc.getAtmcBrnoSt(),
                        defAtmc.getAtmcAtmno(),
                        defAtmc.getAtmcCur(),
                        defAtmc.getAtmcTxCode(),
                        defAtmc.getAtmcDscpt(),
                        defAtmc.getAtmcSelfcd());
                if (record != null) {
                    // 更新ATMC
                    if (itype == ATMCTxType.Accounting.getValue()) {
                        record.setAtmcDrCnt(record.getAtmcDrCnt().intValue() + 1);
                        record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue()));
                    } else {
                        record.setAtmcDrCnt(record.getAtmcDrCnt().intValue() - 1);
                        record.setAtmcDrAmt(BigDecimal.valueOf(record.getAtmcDrAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue()));
                    }
                    // 2010-06-03 by kyo for 程式調整:需QUERY，因為要承接DR_CNT跟DR_AMT
                    // 2010-06-01 by kyo for 程式調整:不需QUERY，直接UPDATE
                    if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) {// 更新ATMC
                        return IOReturnCode.ATMCUpdateError;
                    }
                    defAtmc = record;
                } else {
                    // 寫入ATMC
                    if (itype == ATMCTxType.Accounting.getValue()) {
                        defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                        // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                        defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                        // defAtmc.ATMC_TBSDY_FISC = SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC
                        // 2010-04-06 modify by kyo for 搬錯欄位
                        defAtmc.setAtmcBrnoSt(wBrnoSt2);
                        defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCur());
                        defAtmc.setAtmcTxCode(getFeptxn().getFeptxnTxCodeAtmc());
                        defAtmc.setAtmcDscpt(getFeptxn().getFeptxnCbsDscpt());
                        defAtmc.setAtmcSelfcd(getFeptxn().getFeptxnSelfcd());
                        defAtmc.setAtmcDrCnt(1);
                        // 2010-04-06 modify by kyo for 搬錯欄位
                        defAtmc.setAtmcDrAmt(getFeptxn().getFeptxnTxAmt());
                        defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                        defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                        defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt()); // 跨區手續費幣別
                        defAtmc.setAtmcCrossFlag((short) 1); // 跨區清算記號
                        if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                            return IOReturnCode.ATMCInsertError;
                        }
                    }
                }
            }
            // ----------跨區提款交易寫二筆 ATMC，其他交易寫一筆----------End

            /// * 5/4修改 for COB ATM 現金日結表 */
            // ----------ATM 庫存現金處理----------Start
            if (itype == ATMCTxType.COBCash.getValue()) {
                /// * 裝鈔交易 */
                if (ATMTXCD.RWT.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.RWF.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.RWS.name().equals(getFeptxn().getFeptxnTxCode())) {
                    for (int i = 0; i < wBoxCur.size(); i++) {
                        defAtmc = new Atmc();
                        defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                        // modified by maxine for 2/21 修改 for TTS , 寫入財金營業日
                        // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                        defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                        defAtmc.setAtmcBrnoSt(null);
                        defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmc.setAtmcCur(wBoxCur.get(i));
                        defAtmc.setAtmcTxCode(ATMTXCD.TTS.name());
                        defAtmc.setAtmcDscpt("000");
                        defAtmc.setAtmcSelfcd((short) 0);
                        Atmc record = atmcExtMapper.getAtmcByConditions(
                                defAtmc.getAtmcTbsdy(),
                                defAtmc.getAtmcTbsdyFisc(),
                                defAtmc.getAtmcBrnoSt(),
                                defAtmc.getAtmcAtmno(),
                                defAtmc.getAtmcCur(),
                                defAtmc.getAtmcTxCode(),
                                defAtmc.getAtmcDscpt(),
                                defAtmc.getAtmcSelfcd());
                        if (record != null) {
                            // 更新ATMC
                            // 2011-05-18 by kyo for fix bug.不應該累加
                            record.setAtmcCrAmt(BigDecimal.valueOf(wTrwtAmt.get(i).intValue())); // 裝鈔金額
                            if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) { // 更新ATMC
                                return IOReturnCode.ATMCUpdateError;
                            }
                            defAtmc = record;
                        } else {
                            // 寫入ATMC
                            defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                            defAtmc.setAtmcCrCnt(1); // 貸方筆數
                            defAtmc.setAtmcCrAmt(BigDecimal.valueOf(wTrwtAmt.get(i))); // 裝鈔金額
                            defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                            defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                            defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                            defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                            defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                            if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                                return IOReturnCode.ATMCInsertError;
                            }
                        }
                    }
                } else {
                    /// * 其他交易-提款/存款交易含入帳及沖正 */
                    int i = wBoxCur.indexOf(getFeptxn().getFeptxnTxCur());
                    defAtmc.setAtmcTbsdy(getFeptxn().getFeptxnTbsdy());
                    // modified by maxine for 2/21 修改 for TTS , 寫入財金營業日
                    // 2017/11/10 Modify by Ruling for 財金換日時，改抓交易發生財金營業日
                    defAtmc.setAtmcTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                    defAtmc.setAtmcBrnoSt(null);
                    defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                    defAtmc.setAtmcCur(getFeptxn().getFeptxnTxCur());
                    defAtmc.setAtmcTxCode(ATMTXCD.TTS.name());
                    defAtmc.setAtmcDscpt("000");
                    defAtmc.setAtmcSelfcd((short) 0);
                    // BugReport(001B0431):TTI不需要使用迴圈，幣別僅台幣
                    // BugReport(001B0343):修改迴圈的指標
                    Atmc record = atmcExtMapper.getAtmcByConditions(
                            defAtmc.getAtmcTbsdy(),
                            defAtmc.getAtmcTbsdyFisc(),
                            defAtmc.getAtmcBrnoSt(),
                            defAtmc.getAtmcAtmno(),
                            defAtmc.getAtmcCur(),
                            defAtmc.getAtmcTxCode(),
                            defAtmc.getAtmcDscpt(),
                            defAtmc.getAtmcSelfcd());
                    if (record != null) {
                        // 更新ATMC
                        if (atmMstr.getAtmAtmtype() != null
                                && atmMstr.getAtmAtmtype() == ATMType.ADM.getValue()
                                && CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            // 2013/02/01 Modify by Ruling for 新增硬幣機(提款)
                            int w_COIN_AMT = 0;
                            if (DbHelper.toBoolean(getATMMSTR().getAtmCoin())) {
                                List<Long> atmcoins = atmcoinExtMapper.getAtmCoinForInventoryCash(getFeptxn().getFeptxnAtmno(), atmStat.getAtmstatCrwtSeqno());
                                if (CollectionUtils.isNotEmpty(atmcoins)) {
                                    w_COIN_AMT = atmcoins.get(i).intValue();
                                }
                            }
                            record.setAtmcCrAmt(BigDecimal.valueOf(atmStat.getAtmstatDepAmt().doubleValue() + w_COIN_AMT + wTttiAmt.get(i).doubleValue())); // 剩餘金額

                            // '2012/10/18 Modify by Ruling for 新增硬幣機的業務
                            // defAtmc.setAtmcCrAmt(); = atmStat.getAtmstatDepAmt() + atmStat.ATMSTAT_CDEP_AMT + wTttiAmt(i) '剩餘金額
                        } else {
                            record.setAtmcCrAmt(BigDecimal.valueOf(wTttiAmt.get(i).intValue())); // 剩餘金額
                        }
                        if (atmcExtMapper.updateByPrimaryKeySelective(record) <= 0) {// 更新ATMC
                            return IOReturnCode.ATMCUpdateError;
                        }
                        defAtmc = record;
                    } else {
                        // 寫入ATMC
                        defAtmc.setAtmcBrnoSt(atmMstr.getAtmBrnoSt());
                        defAtmc.setAtmcAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmc.setAtmcCur(wBoxCur.get(i));
                        defAtmc.setAtmcTxCode(ATMTXCD.TTS.name());
                        defAtmc.setAtmcDscpt("000");
                        defAtmc.setAtmcSelfcd((short) 0);
                        defAtmc.setAtmcCrCnt(1);
                        // BugReport(001B0431):對存提款機做額外處理
                        if (atmMstr.getAtmAtmtype() != null
                                && atmMstr.getAtmAtmtype() == ATMType.ADM.getValue()
                                && CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            // 2013/02/01 Modify by Ruling for 新增硬幣機(提款)
                            int w_COIN_AMT = 0;
                            if (DbHelper.toBoolean(getATMMSTR().getAtmCoin())) {
                                List<Long> atmcoins = atmcoinExtMapper.getAtmCoinForInventoryCash(getFeptxn().getFeptxnAtmno(), atmStat.getAtmstatCrwtSeqno());
                                if (CollectionUtils.isNotEmpty(atmcoins)) {
                                    w_COIN_AMT = atmcoins.get(i).intValue();
                                }
                            }
                            defAtmc.setAtmcCrAmt(BigDecimal.valueOf(atmStat.getAtmstatDepAmt().doubleValue() + w_COIN_AMT + wTttiAmt.get(i).doubleValue())); // 剩餘金額

                            // '2012/10/18 Modify by Ruling for 新增硬幣機的業務
                            // defAtmc.setAtmcCrAmt(); = atmStat.getAtmstatDepAmt() + atmStat.ATMSTAT_CDEP_AMT + wTttiAmt(i) '剩餘金額
                        } else {
                            defAtmc.setAtmcCrAmt(BigDecimal.valueOf(wTttiAmt.get(i))); // 剩餘金額
                        }
                        defAtmc.setAtmcLoc(atmMstr.getAtmLoc()); // 行外記號
                        defAtmc.setAtmcZone(atmMstr.getAtmZone()); // ATM所在地區
                        defAtmc.setAtmcCurSt(atmMstr.getAtmCurSt());
                        defAtmc.setAtmcCrossFlag((short) 0); // 跨區清算記號
                        if (atmcExtMapper.insertSelective(defAtmc) <= 0) {// 寫入ATMC
                            return IOReturnCode.ATMCInsertError;
                        }
                    }
                }
            }
            // ----------ATM 庫存現金處理----------End

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 更新 ATM 鈔匣
     */
    public FEPReturnCode updateATMCash(int iType) {
        try {
            Atmbox defAtmBox = new Atmbox();
            Atmboxlog defAtmBoxLog = new Atmboxlog();
            Atmcoin defAtmCoin = new Atmcoin();
            List<Atmcash> atmcashList = null;
            int intBoxNo = 0; // 暫存錢箱編號
            // BugReport(001B0760):使用SHORT運算造成溢位，改為INTEGER
            int[] arrCnt = new int[8]; // 暫存錢箱異動電文
            int FEPtxnDSPCNT = 0;
            if ((iType == ATMCashTxType.Accounting.getValue()) || (iType == ATMCashTxType.EC.getValue())) {
                atmcashList = atmcashExtMapper.getAtmCashByAtmNo(getFeptxn().getFeptxnAtmno(), "ATMCASH_BOXNO");
                if (CollectionUtils.isEmpty(atmcashList)) {
                    return IOReturnCode.ATMCASHNotFound;
                }

                arrCnt[0] = getFeptxn().getFeptxnDspcnt1();
                arrCnt[1] = getFeptxn().getFeptxnDspcnt2();
                arrCnt[2] = getFeptxn().getFeptxnDspcnt3();
                arrCnt[3] = getFeptxn().getFeptxnDspcnt4();
                arrCnt[4] = getFeptxn().getFeptxnDspcnt5();
                arrCnt[5] = getFeptxn().getFeptxnDspcnt6();
                arrCnt[6] = getFeptxn().getFeptxnDspcnt7();
                arrCnt[7] = getFeptxn().getFeptxnDspcnt8();
            }
            // 先塞欄位
            if (iType == ATMCashTxType.Accounting.getValue()) {// 扣帳更新ATM鈔匣數處理
                // BugReport(001B0317):由於轉帳，繳費之類的交易，不需要包transaction，所以變更邏輯
                switch (getAtmTxData().getMsgCtl().getMsgctlTxtype2()) {
                    case 2:
                    case 3:
                        getAtmStat().setAtmstatTfrCnt(getAtmStat().getAtmstatTfrCnt() + 1);
                        getAtmStat().setAtmstatTfrAmt(getAtmStat().getAtmstatTfrAmt() + getFeptxn().getFeptxnTxAmt().longValue());
                        getAtmStat().setAtmstatTfrCntTbsdy(getAtmStat().getAtmstatTfrCntTbsdy() + 1);
                        getAtmStat().setAtmstatTfrAmtTbsdy(getAtmStat().getAtmstatTfrAmtTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                        if (atmstatMapper.updateByPrimaryKeySelective(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        } else {
                            // 2010-06-01 by kyo 程式調整
                            return CommonReturnCode.Normal;
                        }
                        // 2010-05-04 modified by kyo for SPEC修改跨行繳稅TXTYPE2為18
                    case 4:
                    case 18:
                        getAtmStat().setAtmstatPayCnt(getAtmStat().getAtmstatPayCnt() + 1);
                        getAtmStat().setAtmstatPayAmt(getAtmStat().getAtmstatPayAmt() + getFeptxn().getFeptxnTxAmt().longValue());
                        getAtmStat().setAtmstatPayCntTbsdy(getAtmStat().getAtmstatPayCntTbsdy() + 1);
                        getAtmStat().setAtmstatPayAmtTbsdy(getAtmStat().getAtmstatPayAmtTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        } else {
                            // 2010-06-01 by kyo 程式調整
                            return CommonReturnCode.Normal;
                        }
                    case 17:
                        // BugReport(001B0369):新增存款累計次數邏輯
                        // 累計存款次數/金額 以及本日累計次數
                        getAtmStat().setAtmstatDepCur(getFeptxn().getFeptxnTxCur());
                        // 2012/09/04 Modify by Ruling for 新增硬幣機的業務
                        // 2016/07/28 Modify by Ruling for ATM新功能-跨行存款&現金損款
                        // 2020/10/19 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈存款(PDR)
                        if (ATMTXCD.IDR.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.ODR.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.DDR.name().equals(getFeptxn().getFeptxnTxCode())
                                || ATMTXCD.PDR.name().equals(getFeptxn().getFeptxnTxCode())) {
                            getAtmStat().setAtmstatDepCnt(getAtmStat().getAtmstatDepCnt() + 1);
                            getAtmStat().setAtmstatDepAmt(getAtmStat().getAtmstatDepAmt() + getFeptxn().getFeptxnTxAmt().longValue());
                            getAtmStat().setAtmstatDepCurTbsdy(getFeptxn().getFeptxnTxCur());
                            getAtmStat().setAtmstatDepCntTbsdy(getAtmStat().getAtmstatDepCntTbsdy() + 1);
                            getAtmStat().setAtmstatDepAmtTbsdy(getAtmStat().getAtmstatDepAmtTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                        } else {
                            // 紙鈔及硬幣金額為0時視為紙鈔存款(因Hitachi的企業入金(BDR)電文沒有FEPTXN_CASH_AMT、FEPTXN_COIN_AMT這兩個欄位)
                            if (getFeptxn().getFeptxnCashAmt().intValue() == 0 && getFeptxn().getFeptxnCoinAmt().intValue() == 0) {
                                getAtmStat().setAtmstatDepCnt(getAtmStat().getAtmstatDepCnt() + 1);
                                getAtmStat().setAtmstatDepAmt(getAtmStat().getAtmstatDepAmt() + getFeptxn().getFeptxnTxAmt().longValue());
                                getAtmStat().setAtmstatDepCurTbsdy(getFeptxn().getFeptxnTxCur());
                                getAtmStat().setAtmstatDepCntTbsdy(getAtmStat().getAtmstatDepCntTbsdy() + 1);
                                getAtmStat().setAtmstatDepAmtTbsdy(getAtmStat().getAtmstatDepAmtTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                            } else {
                                if (getFeptxn().getFeptxnCashAmt().intValue() > 0) {
                                    // 累計裝鈔後紙鈔存入次數/金額
                                    getAtmStat().setAtmstatDepCnt(getAtmStat().getAtmstatDepCnt() + 1);
                                    getAtmStat().setAtmstatDepAmt(getAtmStat().getAtmstatDepAmt() + getFeptxn().getFeptxnCashAmt().longValue());
                                    // 累計本日紙鈔存入次數/金額
                                    getAtmStat().setAtmstatDepCntTbsdy(getAtmStat().getAtmstatDepCntTbsdy() + 1);
                                    getAtmStat().setAtmstatDepAmtTbsdy(getAtmStat().getAtmstatDepAmtTbsdy() + getFeptxn().getFeptxnCashAmt().longValue());
                                }
                                if (getFeptxn().getFeptxnCoinAmt().intValue() > 0) {
                                    // 累計裝鈔後硬幣存入次數/金額
                                    getAtmStat().setAtmstatCdepCnt(getAtmStat().getAtmstatCdepCnt() + 1);
                                    getAtmStat().setAtmstatCdepAmt(getAtmStat().getAtmstatCdepAmt() + getFeptxn().getFeptxnCoinAmt().longValue());
                                    // 累計本日硬幣存入次數/金額
                                    getAtmStat().setAtmstatCdepCntTbsdy(getAtmStat().getAtmstatCdepCntTbsdy() + 1);
                                    getAtmStat().setAtmstatCdepAmtTbsdy(getAtmStat().getAtmstatCdepAmtTbsdy() + getFeptxn().getFeptxnCoinAmt().longValue());
                                }
                            }
                        }

                        // 2018/10/18 Modify by Ruling for OKI硬幣機功能
                        // 現金繳費找零(CFT)硬幣找零
                        if (getFeptxn().getFeptxnCoinWamt().intValue() > 0) {
                            // 累計裝鈔後硬幣提領次數/金額
                            getAtmStat().setAtmstatCcwdCnt(getAtmStat().getAtmstatCcwdCnt() + 1);
                            getAtmStat().setAtmstatCcwdAmt(getAtmStat().getAtmstatCcwdAmt() + getFeptxn().getFeptxnCoinWamt().longValue());
                            // 累計本日硬幣提領次數/金額
                            getAtmStat().setAtmstatCcwdCntTbsdy(getAtmStat().getAtmstatCcwdCntTbsdy() + 1);
                            getAtmStat().setAtmstatCcwdAmtTbsdy(getAtmStat().getAtmstatCcwdAmtTbsdy() + getFeptxn().getFeptxnCoinWamt().longValue());
                        }

                        // 現金繳費找零(CFT)紙鈔找零
                        if (getFeptxn().getFeptxnCashWamt().intValue() > 0) {
                            if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                                // 累計裝鈔後提款次數/金額
                                getAtmStat().setAtmstatCwdCnt(getAtmStat().getAtmstatCwdCnt() + 1);
                                getAtmStat().setAtmstatCwdAmt(getAtmStat().getAtmstatCwdAmt() + getFeptxn().getFeptxnCashWamt().longValue());
                                // 累計本日硬幣提領次數/金額
                                getAtmStat().setAtmstatCwdCntTbsdy(getAtmStat().getAtmstatCwdCntTbsdy() + 1);
                                getAtmStat().setAtmstatCwdAmtTbsdy(getAtmStat().getAtmstatCwdAmtTbsdy() + getFeptxn().getFeptxnCashWamt().longValue());
                            }
                        }

                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        } else {
                            if (getFeptxn().getFeptxnCashWamt().intValue() > 0) {
                                if (atmcashList != null) {
                                    for (Atmcash dr : atmcashList) {
                                        intBoxNo = dr.getAtmcashBoxno().intValue();
                                        FEPtxnDSPCNT = arrCnt[intBoxNo - 1];
                                        if (FEPtxnDSPCNT > 0) {
                                            dr.setAtmcashPresent(dr.getAtmcashPresent() + FEPtxnDSPCNT);
                                            // 累計本營業日提領次數
                                            dr.setAtmcashCwdCntTbsdy(dr.getAtmcashCwdCntTbsdy() + 1);
                                            // 累計本營業日提領金額
                                            dr.setAtmcashCwdAmtTbsdy(dr.getAtmcashCwdAmtTbsdy() + dr.getAtmcashUnit() * FEPtxnDSPCNT);
                                        }
                                    }
                                }

                                int rowsAffected = 0;
                                try {
                                    rowsAffected = updateDataSet(atmcashList);
                                } catch (Exception ex) {
                                    // 再更新一次
                                    getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH失敗，嘗試Retry");
                                    getLogContext().setProgramException(ex);
                                    logMessage(Level.ERROR, getLogContext());
                                    rowsAffected = updateDataSet(atmcashList);
                                    getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH Retry 成功");
                                    this.logMessage(getLogContext());
                                }

                                if (rowsAffected <= 0) {
                                    getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH失敗，異動筆數=" + rowsAffected);
                                    this.logMessage(getLogContext());
                                    return IOReturnCode.ATMCASHUpdateError;
                                }
                            }

                            return CommonReturnCode.Normal;
                        }
                        // Fly 2016/03/21 修改 for EMV (21, 22, 23, 24, 25)
                    case 1:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 16:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        // 累計台幣領次數/金額
                        if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            // 2013/01/31 Modify by Ruling for 新增硬幣機(提款)
                            // 2013/04/18 Modify by Ruling for 新增硬幣機(提款):硬幣提款不能提領紙鈔不需更新ATMCASH
                            // 2018/10/18 Modify by Ruling for OKI硬幣機功能:FEPTXN_COIN_AMT(硬幣存入金額)改為FEPTXN_COIN_WAMT(硬幣找零金額)
                            if (ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
                                if (getFeptxn().getFeptxnCoinWamt().intValue() > 0) {
                                    // 累計裝鈔後硬幣提領次數/金額
                                    getAtmStat().setAtmstatCcwdCnt(getAtmStat().getAtmstatCcwdCnt() + 1);
                                    getAtmStat().setAtmstatCcwdAmt(getAtmStat().getAtmstatCcwdAmt() + getFeptxn().getFeptxnCoinWamt().longValue());
                                    // 累計本日硬幣提領次數/金額
                                    getAtmStat().setAtmstatCcwdCntTbsdy(getAtmStat().getAtmstatCcwdCntTbsdy() + 1);
                                    getAtmStat().setAtmstatCcwdAmtTbsdy(getAtmStat().getAtmstatCcwdAmtTbsdy() + getFeptxn().getFeptxnCoinWamt().longValue());
                                }
                            } else {
                                // 累計提款次數
                                getAtmStat().setAtmstatCwdCnt(getAtmStat().getAtmstatCwdCnt() + 1);
                                // BugReport(001B0317):SPEC補充邏輯 搬移AtmStat.ATMSTAT_CWD_AMT欄位
                                // 累計提款金額
                                getAtmStat().setAtmstatCwdAmt(getAtmStat().getAtmstatCwdAmt() + getFeptxn().getFeptxnTxAmt().longValue());
                                // 累計本日提款次數
                                getAtmStat().setAtmstatCwdCntTbsdy(getAtmStat().getAtmstatCwdCntTbsdy() + 1);
                                // 累計本日提款金額
                                getAtmStat().setAtmstatCwdAmtTbsdy(getAtmStat().getAtmstatCwdAmtTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                                // BugReport(001B0431):拿掉累積繳庫金額邏輯
                                // 累計本營業日繳庫金額
                                // getAtmStat().ATMSTAT_BK_AMT_TBSDY -= getFeptxn().getFeptxnTxAmt()
                            }
                        } else if (CurrencyType.USD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntUsdTbsdy(getAtmStat().getAtmstatCwdCntUsdTbsdy() + 1);
                            getAtmStat().setAtmstatCwdAmtUsdTbsdy(getAtmStat().getAtmstatCwdAmtUsdTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_USD_TBSDY -= getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntHkdTbsdy(getAtmStat().getAtmstatCwdCntHkdTbsdy() + 1);
                            getAtmStat().setAtmstatCwdAmtHkdTbsdy(getAtmStat().getAtmstatCwdAmtHkdTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_HKD_TBSDY -= getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.JPY.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntJpyTbsdy(getAtmStat().getAtmstatCwdCntJpyTbsdy() + 1);
                            getAtmStat().setAtmstatCwdAmtJpyTbsdy(getAtmStat().getAtmstatCwdAmtJpyTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_JPY_TBSDY -= getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntMopTbsdy(getAtmStat().getAtmstatCwdCntMopTbsdy() + 1);
                            getAtmStat().setAtmstatCwdAmtMopTbsdy(getAtmStat().getAtmstatCwdAmtMopTbsdy() + getFeptxn().getFeptxnTxAmt().longValue());
                        }
                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        }
                        break;
                    default:
                        // BugReport(001B0370):2010-04-29 modified by kyo for 處理額外的CASE需要直接return
                        // 2010-06-01 by kyo 程式調整
                        return CommonReturnCode.Normal;
                }

                // 2013/04/18 Modify by Ruling for 新增硬幣機(提款):硬幣提款不能提領紙鈔不需更新ATMCASH
                if (!ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
                    if (atmcashList != null) {
                        for (Atmcash dr : atmcashList) {
                            intBoxNo = dr.getAtmcashBoxno();
                            FEPtxnDSPCNT = arrCnt[intBoxNo - 1];
                            if (FEPtxnDSPCNT > 0) {
                                // BugReport(001B0296) 2010-04-22 modified by kyo for 計算ATMCASH_PRESENT(上次裝鈔後吐鈔次數)時 多計算一次(CODING疏失)
                                // BRSID1(001B0145):Modify By Matt\\
                                // Fly 2016/03/21 修改 for EMV (21, 22, 23, 24, 25)
                                switch (getAtmTxData().getMsgCtl().getMsgctlTxtype2()) {
                                    case 1:
                                    case 9:
                                    case 10:
                                    case 11:
                                    case 12:
                                    case 13:
                                    case 14:
                                    case 16:
                                    case 21:
                                    case 22:
                                    case 23:
                                    case 24:
                                    case 25:
                                        dr.setAtmcashPresent(dr.getAtmcashPresent() + FEPtxnDSPCNT);
                                        // 累計本營業日提領次數
                                        dr.setAtmcashCwdCntTbsdy(dr.getAtmcashCwdCntTbsdy() + 1);
                                        // 累計本營業日提領金額
                                        dr.setAtmcashCwdAmtTbsdy(dr.getAtmcashCwdAmtTbsdy() + dr.getAtmcashUnit() * FEPtxnDSPCNT);
                                        // BugReport(001B0431):拿掉累積繳庫金額邏輯
                                        // 累計本營業日繳庫金額
                                        // dr.Item("ATMCASH_BK_AMT_TBSDY") = CType(dr.Item("ATMCASH_BK_AMT_TBSDY"), Decimal) - CType(dr.Item("ATMCASH_UNIT"), Integer) * FEPtxnDSPCNT
                                        break;
                                }
                            }
                        }
                    }

                    // 2016/03/28 Modify by Ruling for 避免更新時發生SQL Timeout再更新一次
                    int rowsAffected = 0;
                    try {
                        rowsAffected = updateDataSet(atmcashList);
                    } catch (Exception ex) {
                        // 再更新一次
                        getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH失敗，嘗試Retry");
                        getLogContext().setProgramException(ex);
                        logMessage(Level.ERROR, getLogContext());
                        rowsAffected = updateDataSet(atmcashList);
                        getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH Retry 成功");
                        this.logMessage(getLogContext());
                    }
                    // Dim rowsAffected As Integer = dbAtmcash.UpdateDataSet(dsAtmCash)
                    if (rowsAffected <= 0) {
                        getLogContext().setRemark("UpdateATMCash-入扣帳-更新ATMCASH失敗，異動筆數=" + rowsAffected);
                        this.logMessage(getLogContext());
                        // BugReport(001B0370):防止未begin就rollback
                        return IOReturnCode.ATMCASHUpdateError;
                    }
                }

                // 2011-05-06 by kyo for spec update:/* 5/4 修改 for COB ATM 現金日結表*/
                // 2013/11/08 Modify by Ruling for 港/澳 ATM 均寫入TTS
                // If getFeptxn().FEPTXN_ATM_ZONE = ATMZone.TWN.ToString Then

            } else if (iType == ATMCashTxType.EC.getValue()) {// 沖正更新ATM鈔匣數處理
                // 2010-04-21 modified by kyo for 沖正時須使用原交易的MSGID 取得原交易的MSGCTL
                Msgctl defMsgCtl = new Msgctl();
                defMsgCtl.setMsgctlMsgid(getFeptxn().getFeptxnMsgid());
                defMsgCtl = msgctlMapper.selectByPrimaryKey(defMsgCtl.getMsgctlMsgid());
                if (defMsgCtl == null) {
                    throw ExceptionUtil.createException("GetTxid Error");
                }
                // BugReport(001B0317):由於轉帳，繳費之類的交易，不需要包transaction，所以變更邏輯
                switch (defMsgCtl.getMsgctlTxtype2()) {
                    case 2:
                    case 3:
                        getAtmStat().setAtmstatTfrCnt(getAtmStat().getAtmstatTfrCnt() - 1);
                        getAtmStat().setAtmstatPayAmt(getAtmStat().getAtmstatPayAmt() - getFeptxn().getFeptxnTxAmt().longValue());
                        getAtmStat().setAtmstatTfrCntTbsdy(getAtmStat().getAtmstatTfrCntTbsdy() - 1);
                        getAtmStat().setAtmstatTfrAmtTbsdy(getAtmStat().getAtmstatTfrAmtTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        } else {
                            // 2010-06-01 by kyo 程式調整
                            return CommonReturnCode.Normal;
                        }
                        // 2010-05-04 modified by kyo for SPEC修改跨行繳稅TXTYPE2為18
                    case 4:
                    case 18:
                        getAtmStat().setAtmstatPayCnt(getAtmStat().getAtmstatPayCnt() - 1);
                        getAtmStat().setAtmstatPayAmt(getAtmStat().getAtmstatPayAmt() - getFeptxn().getFeptxnTxAmt().longValue());
                        getAtmStat().setAtmstatPayCntTbsdy(getAtmStat().getAtmstatPayCntTbsdy() - 1);
                        getAtmStat().setAtmstatPayAmtTbsdy(getAtmStat().getAtmstatPayAmtTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        } else {
                            // 2010-06-01 by kyo 程式調整
                            return CommonReturnCode.Normal;
                        }
                        // Fly 2016/03/21 修改 for EMV (21, 22, 23, 24, 25)
                    case 1:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 16:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                        // 累計台幣領次數/金額
                        if (CurrencyType.TWD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            // 2013/01/31 Modify by Ruling for 新增硬幣機(提款)
                            // 2013/04/18 Modify by Ruling for 新增硬幣機(提款):硬幣提款不能提領紙鈔不需更新ATMCASH
                            // 2018/10/18 Modify by Ruling for OKI硬幣機功能:FEPTXN_COIN_AMT(硬幣存入金額)改為FEPTXN_COIN_WAMT(硬幣找零金額)
                            if (ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
                                if (getFeptxn().getFeptxnCoinWamt().intValue() > 0) {
                                    // 累計裝鈔後硬幣提領次數/金額
                                    getAtmStat().setAtmstatCcwdCnt(getAtmStat().getAtmstatCcwdCnt() - 1);
                                    getAtmStat().setAtmstatCcwdAmt(getAtmStat().getAtmstatCcwdAmt() - getFeptxn().getFeptxnCoinWamt().longValue());
                                    // 累計本日硬幣提領次數/金額
                                    getAtmStat().setAtmstatCcwdCntTbsdy(getAtmStat().getAtmstatCcwdCntTbsdy() - 1);
                                    getAtmStat().setAtmstatCcwdAmtTbsdy(getAtmStat().getAtmstatCcwdAmtTbsdy() - getFeptxn().getFeptxnCoinWamt().longValue());
                                }
                            } else {
                                // 累計提款次數
                                getAtmStat().setAtmstatCwdCnt(getAtmStat().getAtmstatCwdCnt() - 1);
                                // BugReport(001B0317):SPEC補充邏輯 搬移AtmStat.ATMSTAT_CWD_AMT欄位
                                // 累計提款金額
                                getAtmStat().setAtmstatCwdAmt(getAtmStat().getAtmstatCwdAmt() - getFeptxn().getFeptxnTxAmt().longValue());
                                // 累計本日提款次數
                                getAtmStat().setAtmstatCwdCntTbsdy(getAtmStat().getAtmstatCwdCntTbsdy() - 1);
                                // 累計本日提款金額
                                getAtmStat().setAtmstatCwdAmtTbsdy(getAtmStat().getAtmstatCwdAmtTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                                // BugReport(001B0431):拿掉累積繳庫金額邏輯
                                // 累計本營業日繳庫金額
                                // getAtmStat().ATMSTAT_BK_AMT_TBSDY += getFeptxn().getFeptxnTxAmt()
                            }
                        } else if (CurrencyType.USD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntUsdTbsdy(getAtmStat().getAtmstatCwdCntUsdTbsdy() - 1);
                            getAtmStat().setAtmstatCwdAmtUsdTbsdy(getAtmStat().getAtmstatCwdAmtUsdTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_USD_TBSDY += getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.HKD.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntHkdTbsdy(getAtmStat().getAtmstatCwdCntHkdTbsdy() - 1);
                            getAtmStat().setAtmstatCwdAmtHkdTbsdy(getAtmStat().getAtmstatCwdAmtHkdTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_HKD_TBSDY += getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.JPY.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntJpyTbsdy(getAtmStat().getAtmstatCwdCntJpyTbsdy() - 1);
                            getAtmStat().setAtmstatCwdAmtJpyTbsdy(getAtmStat().getAtmstatCwdAmtJpyTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                            // getAtmStat().ATMSTAT_BK_AMT_JPY_TBSDY += getFeptxn().getFeptxnTxAmt()
                        } else if (CurrencyType.MOP.name().equals(getFeptxn().getFeptxnTxCur())) {
                            getAtmStat().setAtmstatCwdCntMopTbsdy(getAtmStat().getAtmstatCwdCntMopTbsdy() - 1);
                            getAtmStat().setAtmstatCwdAmtMopTbsdy(getAtmStat().getAtmstatCwdAmtMopTbsdy() - getFeptxn().getFeptxnTxAmt().longValue());
                        }
                        if (atmstatMapper.updateByPrimaryKey(getAtmStat()) <= 0) {
                            return IOReturnCode.ATMSTATUpdateError;
                        }
                        break;
                }

                // 2013/04/18 Modify by Ruling for 新增硬幣機(提款):硬幣提款不能提領紙鈔不需更新ATMCASH
                if (!ATMTXCD.ICW.name().equals(getFeptxn().getFeptxnTxCode())) {
                    if (atmcashList != null) {
                        for (Atmcash dr : atmcashList) {
                            intBoxNo = dr.getAtmcashBoxno();
                            FEPtxnDSPCNT = arrCnt[intBoxNo - 1];
                            if (FEPtxnDSPCNT > 0) {
                                // BugReport(001B0296) 2010-04-22 modified by kyo for 計算ATMCASH_PRESENT(上次裝鈔後吐鈔次數)時 多計算一次(CODING疏失)
                                // BRSID1(001B0145):Modify By Matt\\
                                // Fly 2016/03/21 修改 for EMV (21, 22, 23, 24, 25)
                                switch (defMsgCtl.getMsgctlTxtype2()) {
                                    case 1:
                                    case 9:
                                    case 10:
                                    case 11:
                                    case 12:
                                    case 13:
                                    case 14:
                                    case 16:
                                    case 21:
                                    case 22:
                                    case 23:
                                    case 24:
                                    case 25:
                                        dr.setAtmcashPresent(dr.getAtmcashPresent() - FEPtxnDSPCNT);
                                        // 累計本營業日提領次數
                                        dr.setAtmcashCwdCntTbsdy(dr.getAtmcashCwdCntTbsdy() - 1);
                                        // 累計本營業日提領金額
                                        dr.setAtmcashCwdAmtTbsdy(dr.getAtmcashCwdAmtTbsdy() - dr.getAtmcashUnit() * FEPtxnDSPCNT);
                                        // BugReport(001B0431):拿掉累積繳庫金額邏輯
                                        // 累計本營業日繳庫金額
                                        // dr.Item("ATMCASH_BK_AMT_TBSDY") = CType(dr.Item("ATMCASH_BK_AMT_TBSDY"), Decimal) + CType(dr.Item("ATMCASH_UNIT"), Integer) * FEPtxnDSPCNT
                                        break;
                                }
                            }
                        }
                    }

                    // 2016/03/28 Modify by Ruling for 避免更新時發生SQL Timeout再更新一次
                    int rowsAffected = 0;
                    try {
                        rowsAffected = updateDataSet(atmcashList);
                    } catch (Exception ex) {
                        // 再更新一次
                        getLogContext().setRemark("UpdateATMCash-沖正-更新ATMCASH失敗，嘗試Retry");
                        getLogContext().setProgramException(ex);
                        logMessage(Level.ERROR, getLogContext());
                        rowsAffected = updateDataSet(atmcashList);
                        getLogContext().setRemark("UpdateATMCash-沖正-更新ATMCASH Retry 成功");
                        this.logMessage(getLogContext());
                    }
                    // Dim rowsAffected As Integer = dbAtmcash.UpdateDataSet(dsAtmCash)
                    if (rowsAffected <= 0) {
                        getLogContext().setRemark("UpdateATMCash-沖正-更新ATMCASH失敗，異動筆數=" + rowsAffected);
                        this.logMessage(getLogContext());
                        return IOReturnCode.ATMCASHUpdateError;
                    }
                }

                // 2011-05-06 by kyo for spec update:/* 5/4 修改 for COB ATM 現金日結表*/
                // 2013/11/08 Modify by Ruling for 港/澳 ATM 均寫入TTS

            } else if (iType == ATMCashTxType.Box.getValue()) {// 更新ATM BOX鈔匣數處理
                int intM = 0; // 用來計算啟始位置
                //--ben-20220922-//String strAREA = atmReq.getAdmboxArea().substring(8);
                String strAREA = "";

                if (checkADMData(strAREA)) {
                    //--ben-20220922-//int intRWTSEQNO = Integer.parseInt(atmReq.getAdmboxArea().substring(0, 4)); // 裝鈔序號
                    int intRWTSEQNO = Integer.parseInt(""); // 裝鈔序號
                    //--ben-20220922-//int intBOXCNT = Integer.parseInt(atmReq.getAdmboxArea().substring(6, 8)); // 錢箱數量
                    int intBOXCNT = Integer.parseInt(""); // 錢箱數量

                    defAtmBox.setAtmboxRwtSeqno(intRWTSEQNO); // 裝鈔序號
                    // 將 ATM 電文寫入 ATMBOXLOG

                    defAtmBoxLog.setAtmboxlogTxDate(getFeptxn().getFeptxnTxDateAtm()); // 交易日期
                    // 2010-06-14 by kyo for SPEC新增:確認電文放入 TRACE EJFNO
                    if (getFeptxn().getFeptxnWay() == 3) {
                        defAtmBoxLog.setAtmboxlogEjfno(getFeptxn().getFeptxnTraceEjfno()); // FEP交易序號
                    } else {
                        defAtmBoxLog.setAtmboxlogEjfno(getFeptxn().getFeptxnEjfno()); // FEP交易序號
                    }

                    defAtmBoxLog.setAtmboxlogAtmno(getFeptxn().getFeptxnAtmno()); // ATM代號
                    defAtmBoxLog.setAtmboxlogRwtSeqno(intRWTSEQNO); // 裝鈔序號
                    defAtmBoxLog.setAtmboxlogBoxCnt((short) intBOXCNT); // ADM目前鈔箱個數
                    // 2010-05-19 modified by kyo for 記錄完整的BOXAREA
                    //--ben-20220922-//defAtmBoxLog.setAtmboxlogBoxArea(atmReq.getAdmboxArea()); // 錢箱資料區
                    Atmboxlog atmBoxLog = atmboxlogExtMapper.selectByPrimaryKey(defAtmBoxLog.getAtmboxlogTxDate(), defAtmBoxLog.getAtmboxlogEjfno());
                    if (atmBoxLog == null) {
                        atmboxlogExtMapper.insertSelective(defAtmBoxLog);
                    } else {
                        return IOReturnCode.ATMBOXLOGInsertError;
                    }

                    // 以 FEPTXN_TX_DATE, FEPTXN_AMTNO,
                    // ATM_TITA.RWTSEQ, ATMBOX_SETTLE = 0 ,
                    // 錢箱編號, 幣別, 面額為 KEY , 讀取 ATMBOX
                    // 如不存在, 則 INSERT ATMBOX
                    // 如存在, 則更新 ATMBOX 張數資料

                    for (int I = 0; I < intBOXCNT; I++) {
                        defAtmBox.setAtmboxTxDate(getFeptxn().getFeptxnTxDateAtm());
                        defAtmBox.setAtmboxAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmBox.setAtmboxSettle((short) 0); // 結帳別
                        // BugReport(001B0486):此處更新ATMBOX時須注意refill不更新
                        defAtmBox.setAtmboxBrnoSt(getAtmStr().getAtmBrnoSt());

                        defAtmBox.setAtmboxBoxno(Short.valueOf(strAREA.substring(intM, intM + 1))); // 錢箱編號
                        intM += 1;
                        defAtmBox.setAtmboxCur(getCurrencyByBSP(strAREA.substring(intM, intM + 2)).getCurcdAlpha3()); // 幣別
                        intM += 2;
                        defAtmBox.setAtmboxUnit(Integer.parseInt(strAREA.substring(intM, intM + 6))); // 面額
                        // 主KEY的部分
                        Atmbox atmBox = atmboxExtMapper.queryByCandidateKey(defAtmBox);
                        if (atmBox == null) {
                            // 2010-11-18 by kyo for CodeGen 修改後Insert語法變更
                            // 2011-03-28 by kyo 修正("Insert ATMBOX時沒有把裝鈔序號的欄位的ispassed屬性改為true，造成寫成錯誤的序號")
                            // 2016/02/04 Modify by Ruling for CWF交易之錢箱張數資料送負值，如"00-9"無法轉成'Integer'導致程式發生例外
                            // 裝鈔張數
                            intM += 6;
                            defAtmBox.setAtmboxRefill(0);
                            // 存款張數
                            intM += 4;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-新增ATMBOX-存款張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                defAtmBox.setAtmboxDeposit(0);
                            } else {
                                defAtmBox.setAtmboxDeposit(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                            }
                            // 吐鈔張數
                            intM += 4;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-新增ATMBOX-吐鈔張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                defAtmBox.setAtmboxPresent(0);
                            } else {
                                defAtmBox.setAtmboxPresent(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                            }
                            // 拒絶張數
                            intM += 4;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-新增ATMBOX-拒絶張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                defAtmBox.setAtmboxReject(0);
                            } else {
                                defAtmBox.setAtmboxReject(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                            }
                            // 無法辨識張數
                            intM += 4;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-新增ATMBOX-無法辨識張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                defAtmBox.setAtmboxUnknown(0);
                            } else {
                                defAtmBox.setAtmboxUnknown(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                            }
                            intM += 4;
                            if (atmboxExtMapper.insertSelective(defAtmBox) <= 0) {
                                return IOReturnCode.ATMBOXInsertError;
                            }
                        } else {
                            // 2010-11-18 by kyo for CodeGen 修改後Insert語法變更
                            // 2011-03-28 by kyo 修正("Insert ATMBOX時沒有把裝鈔序號的欄位的ispassed屬性改為true，造成寫成錯誤的序號")
                            // 2016/02/04 Modify by Ruling for CWF交易之錢箱張數資料送負值，如"00-9"無法轉成'Integer'導致程式發生例外
                            int parseValue = 0;
                            // refill不更新
                            // 存款張數
                            intM += 10;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-更新ATMBOX-存款張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                parseValue = 0;
                            } else {
                                parseValue = Integer.parseInt(strAREA.substring(intM, intM + 4));
                            }
                            atmBox.setAtmboxDeposit(atmBox.getAtmboxDeposit() + parseValue);
                            // 吐鈔張數
                            intM += 4;
                            parseValue = 0;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-更新ATMBOX-吐鈔張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                parseValue = 0;
                            } else {
                                parseValue = Integer.parseInt(strAREA.substring(intM, intM + 4));
                            }
                            atmBox.setAtmboxPresent(atmBox.getAtmboxPresent() + parseValue);
                            // 拒絶張數
                            intM += 4;
                            parseValue = 0;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-更新ATMBOX-拒絶張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                parseValue = 0;
                            } else {
                                parseValue = Integer.parseInt(strAREA.substring(intM, intM + 4));
                            }
                            atmBox.setAtmboxReject(atmBox.getAtmboxReject() + parseValue);
                            // 無法辨識張數
                            intM += 4;
                            parseValue = 0;
                            if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                                getLogContext().setRemark("UpdateATMCash-iType為Box-更新ATMBOX-無法辨識張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                                parseValue = 0;
                            } else {
                                parseValue = Integer.parseInt(strAREA.substring(intM, intM + 4));
                            }
                            atmBox.setAtmboxUnknown(atmBox.getAtmboxUnknown() + parseValue);
                            intM += 4;
                            // 2010-05-29 by kyo for code modify:將更新邏輯搬至ELSE中才不會INSERT完後又多UPDATE一次
                            if (atmboxExtMapper.updateByCandidateKey(atmBox) <= 0) {// 更新資料，若無則新增一筆
                                return IOReturnCode.ATMBOXUpdateError;
                            }
                        }
                    }
                }

            } else if (iType == ATMCashTxType.Coin.getValue()) {// 更新ATM COIN 鈔匣數處理
                // 2012/09/04 Modify by Ruling for 新增硬幣機的業務
                // 2014/03/03 Modify by Ruling for 大容量循環式Glory硬幣存提款機
                //--ben-20220922-//if (ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnConTxCode()) && StringUtils.isNotBlank(atmReq.getPID())) {
                if (ATMTXCD.CDF.name().equals(getFeptxn().getFeptxnConTxCode()) && StringUtils.isNotBlank("")) {
                    // 大容量循環式Glory硬幣存提款機
                    int atmcoinDEPOSIT = 0;
                    for (int I = 0; I <= 4; I++) {
                        defAtmCoin.setAtmcoinTxDate(getFeptxn().getFeptxnTxDateAtm());
                        defAtmCoin.setAtmcoinAtmno(getFeptxn().getFeptxnAtmno());
                        //--ben-20220922-//defAtmCoin.setAtmcoinRwtSeqno(Integer.parseInt(atmReq.getCRWTSEQ()));
                        defAtmCoin.setAtmcoinSettle((short) 0);
                        //--ben-20220922-//defAtmCoin.setAtmcoinBoxno(Short.valueOf(atmReq.getPID().substring(I * 2, I * 2 + 2))); // 如:PID=0203040105，拆解為BOXNO:2,3,4,1,5
                        defAtmCoin.setAtmcoinCur(CurrencyType.TWD.name()); // 硬幣存款固定放台幣
                        switch (I) {
                            case 0:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT01().intValue());
                                //--ben-20220922-//atmcoinDEPOSIT = atmReq.getDEPOSIT01().intValue();
                                break;
                            case 1:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT02().intValue());
                                //--ben-20220922-//atmcoinDEPOSIT = atmReq.getDEPOSIT02().intValue();
                                break;
                            case 2:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT03().intValue());
                                //--ben-20220922-//atmcoinDEPOSIT = atmReq.getDEPOSIT03().intValue();
                                break;
                            case 3:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT04().intValue());
                                //--ben-20220922-//atmcoinDEPOSIT = atmReq.getDEPOSIT04().intValue();
                                break;
                            case 4:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT05().intValue());
                                //--ben-20220922-//atmcoinDEPOSIT = atmReq.getDEPOSIT05().intValue();
                                break;
                        }
                        if (atmcoinDEPOSIT > 0) {
                            // 硬幣存入個數>0才要更新ATMCOIN
                            // 2016/02/05 Modify by Ruling for 先update再insert
                            defAtmCoin.setAtmcoinDeposit(atmcoinDEPOSIT);
                            if (atmcoinExtMapper.updateByCandidateKeyAccumulation(defAtmCoin) <= 0) {
                                // 更新ATMCOIN失敗改做新增
                                if (atmcoinExtMapper.insertSelective(defAtmCoin) <= 0) {
                                    getLogContext().setTableName("ATMCOIN");
                                    getLogContext().setPrimaryKeys("ATMCOIN_TX_DATE:" + defAtmCoin.getAtmcoinTxDate() + ";ATMCOIN_ATMNO:" + defAtmCoin.getAtmcoinAtmno() + ";ATMCOIN_RWT_SEQNO:"
                                            + defAtmCoin.getAtmcoinRwtSeqno() + ";ATMCOIN_SETTLE:" + defAtmCoin.getAtmcoinSettle() + ";ATMCOIN_BOXNO:" + defAtmCoin.getAtmcoinBoxno()
                                            + ";ATMCOIN_CURCD:" + defAtmCoin.getAtmcoinCur() + ";ATMCOIN_UNIT:" + defAtmCoin.getAtmcoinUnit());
                                    getLogContext().setReturnCode(IOReturnCode.InsertFail);
                                    this.logMessage(getLogContext());
                                    return IOReturnCode.InsertFail;
                                }
                            }
                        }
                    }
                } else {
                    for (int I = 0; I <= 4; I++) {
                        defAtmCoin.setAtmcoinTxDate(getFeptxn().getFeptxnTxDateAtm());
                        defAtmCoin.setAtmcoinAtmno(getFeptxn().getFeptxnAtmno());
                        //--ben-20220922-//defAtmCoin.setAtmcoinRwtSeqno(Integer.parseInt(atmReq.getCRWTSEQ()));
                        defAtmCoin.setAtmcoinSettle((short) 0);
                        defAtmCoin.setAtmcoinBoxno((short) 1); // 硬幣存款固定放1
                        defAtmCoin.setAtmcoinCur(CurrencyType.TWD.name()); // 硬幣存款固定放台幣
                        switch (I) {
                            case 0:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT01().intValue());
                                break;
                            case 1:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT02().intValue());
                                break;
                            case 2:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT03().intValue());
                                break;
                            case 3:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT04().intValue());
                                break;
                            case 4:
                                //--ben-20220922-//defAtmCoin.setAtmcoinUnit(atmReq.getUNIT05().intValue());
                                break;
                        }
                        if (defAtmCoin.getAtmcoinUnit() != 0) {
                            // 2016/02/05 Modify by Ruling for 先update再insert
                            // 面額<>0才要更新ATMCOIN
                            switch (I) {
                                case 0:
                                    //--ben-20220922-//defAtmCoin.setAtmcoinDeposit(atmReq.getDEPOSIT01().intValue());
                                    break;
                                case 1:
                                    //--ben-20220922-//defAtmCoin.setAtmcoinDeposit(atmReq.getDEPOSIT02().intValue());
                                    break;
                                case 2:
                                    //--ben-20220922-//defAtmCoin.setAtmcoinDeposit(atmReq.getDEPOSIT03().intValue());
                                    break;
                                case 3:
                                    //--ben-20220922-//defAtmCoin.setAtmcoinDeposit(atmReq.getDEPOSIT04().intValue());
                                    break;
                                case 4:
                                    //--ben-20220922-//defAtmCoin.setAtmcoinDeposit(atmReq.getDEPOSIT05().intValue());
                                    break;
                            }
                            if (atmcoinExtMapper.updateByCandidateKeyAccumulation(defAtmCoin) <= 0) {
                                // 更新ATMCOIN失敗改做新增
                                if (atmcoinExtMapper.insertSelective(defAtmCoin) <= 0) {
                                    getLogContext().setTableName("ATMCOIN");
                                    getLogContext().setPrimaryKeys("ATMCOIN_TX_DATE:" + defAtmCoin.getAtmcoinTxDate() + ";ATMCOIN_ATMNO:" + defAtmCoin.getAtmcoinAtmno() + ";ATMCOIN_RWT_SEQNO:"
                                            + defAtmCoin.getAtmcoinRwtSeqno() + ";ATMCOIN_SETTLE:" + defAtmCoin.getAtmcoinSettle() + ";ATMCOIN_BOXNO:" + defAtmCoin.getAtmcoinBoxno()
                                            + ";ATMCOIN_CURCD:" + defAtmCoin.getAtmcoinCur() + ";ATMCOIN_UNIT:" + defAtmCoin.getAtmcoinUnit());
                                    getLogContext().setReturnCode(IOReturnCode.InsertFail);
                                    this.logMessage(getLogContext());
                                    return IOReturnCode.InsertFail;
                                }
                            }
                        }
                    }
                }

            } else if (iType == ATMCashTxType.CoinICW.getValue()) {// 更新ATM COIN 鈔匣數處理
                // Fly 2018/10/09 For OKI硬幣機存提款
                int intM = 0; // 用來計算啟始位置
                //--ben-20220922-//String strAREA = atmReq.getCoinArea().substring(6);
                String strAREA = "";

                if (checkCOINData()) {
                    Atmcoinlog defCoinLog = new Atmcoinlog();
                    //--ben-20220922-//int intRWTSEQNO = Integer.parseInt(atmReq.getCoinArea().substring(0, 4)); // 裝鈔序號
                    int intRWTSEQNO = Integer.parseInt(""); // 裝鈔序號
                    //--ben-20220922-//byte intCOINCNT = Byte.parseByte(atmReq.getCoinArea().substring(4, 6)); // 錢箱數量
                    byte intCOINCNT = Byte.parseByte(""); // 錢箱數量
                    defCoinLog.setAtmcoinlogTxDate(getFeptxn().getFeptxnTxDate());
                    if (getFeptxn().getFeptxnWay() == 3) {
                        defCoinLog.setAtmcoinlogEjfno(getFeptxn().getFeptxnTraceEjfno());
                    } else {
                        defCoinLog.setAtmcoinlogEjfno(getFeptxn().getFeptxnEjfno());
                    }
                    defCoinLog.setAtmcoinlogAtmno(getFeptxn().getFeptxnAtmno());
                    defCoinLog.setAtmcoinlogRwtSeqno(intRWTSEQNO);
                    defCoinLog.setAtmcoinlogBoxCnt((short) intCOINCNT);
                    //--ben-20220922-//defCoinLog.setAtmcoinlogCoinArea(atmReq.getCoinArea());
                    atmcoinlogMapper.insertSelective(defCoinLog);
                    for (int I = 0; I < intCOINCNT; I++) {
                        defAtmCoin.setAtmcoinTxDate(getFeptxn().getFeptxnTxDateAtm());
                        defAtmCoin.setAtmcoinAtmno(getFeptxn().getFeptxnAtmno());
                        defAtmCoin.setAtmcoinRwtSeqno(intRWTSEQNO);
                        defAtmCoin.setAtmcoinSettle((short) 0);
                        defAtmCoin.setAtmcoinBoxno(Short.valueOf(strAREA.substring(intM, intM + 1))); // 錢箱編號
                        intM += 1;
                        defAtmCoin.setAtmcoinCur(getCurrencyByBSP(strAREA.substring(intM, intM + 2)).getCurcdAlpha3()); // 幣別
                        intM += 2;
                        defAtmCoin.setAtmcoinUnit(Integer.parseInt(strAREA.substring(intM, intM + 3))); // 面額
                        // 裝鈔張數
                        intM += 3;
                        if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                            defAtmCoin.setAtmcoinDeposit(0);
                            getLogContext().setRemark("UpdateATMCash-iType為CoinICW-新增ATMCOIN-存款張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));

                        } else {
                            defAtmCoin.setAtmcoinDeposit(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                        }
                        // 吐鈔張數
                        intM += 4;
                        if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                            defAtmCoin.setAtmcoinPresent(0);
                            getLogContext().setRemark("UpdateATMCash-iType為CoinICW-新增ATMCOIN-吐鈔張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));

                        } else {
                            defAtmCoin.setAtmcoinPresent(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                        }
                        // 拒絶張數
                        intM += 4;
                        if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                            defAtmCoin.setAtmcoinReject(0);
                            getLogContext().setRemark("UpdateATMCash-iType為CoinICW-新增ATMCOIN-拒絶張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));

                        } else {
                            defAtmCoin.setAtmcoinReject(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                        }
                        // 無法辨識張數
                        intM += 4;
                        if (!StringUtils.isNumeric(strAREA.substring(intM, intM + 4))) {
                            defAtmCoin.setAtmcoinUnknown(0);
                            getLogContext().setRemark("UpdateATMCash-iType為CoinICW-新增ATMCOIN-無法辨識張數無法轉成數字值為" + strAREA.substring(intM, intM + 4));
                            sendEMS(getLogContext());
                        } else {
                            defAtmCoin.setAtmcoinUnknown(Integer.parseInt(strAREA.substring(intM, intM + 4)));
                        }
                        intM += 4;

                        if (atmcoinExtMapper.updateByCandidateKeyAccumulation(defAtmCoin) <= 0) {
                            // 更新ATMCOIN失敗改做新增
                            if (atmcoinExtMapper.insertSelective(defAtmCoin) <= 0) {
                                getLogContext().setTableName("ATMCOIN");
                                getLogContext().setPrimaryKeys("ATMCOIN_TX_DATE:" + defAtmCoin.getAtmcoinTxDate() + ";ATMCOIN_ATMNO:" + defAtmCoin.getAtmcoinAtmno() + ";ATMCOIN_RWT_SEQNO:"
                                        + defAtmCoin.getAtmcoinRwtSeqno() + ";ATMCOIN_SETTLE:" + defAtmCoin.getAtmcoinSettle() + ";ATMCOIN_BOXNO:" + defAtmCoin.getAtmcoinBoxno() + ";ATMCOIN_CURCD:"
                                        + defAtmCoin.getAtmcoinCur() + ";ATMCOIN_UNIT:" + defAtmCoin.getAtmcoinUnit());
                                getLogContext().setReturnCode(IOReturnCode.InsertFail);
                                this.logMessage(getLogContext());
                                return IOReturnCode.InsertFail;
                            }
                        }
                    }
                }
            }

            // 2010-06-01 by kyo 程式調整
            return CommonReturnCode.Normal;

        } catch (Exception ex) {
            // 2010-09-01 by kyo for coding error 避免程式發生例外時並未beginTransaction又發生例外
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 寫入 EMV 收付累計檔
     */
    public FEPReturnCode insertEMVC(int type) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Emvc defEMVC = new Emvc();
        @SuppressWarnings("unused")
        String serviceCode = ""; // TRK2等號後第5位
        String pan = ""; // TRK2等號前16-19位

        try {
            // 寫入 EMV 收付累計檔
            if (StringUtils.isBlank(getFeptxn().getFeptxnTrk2()) || getFeptxn().getFeptxnTrk2().indexOf("=") < 16) {
                getLogContext().setRemark("InsertEMVC-FEPTXN_TRK2為NULL或等號前的長度不足16位,FEPTXN_TRK2=" + getFeptxn().getFeptxnTrk2());
                this.logMessage(getLogContext());
                return ATMReturnCode.Track2Error;
            }
            pan = getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")).trim();
            defEMVC.setEmvcTxDate(getFeptxn().getFeptxnTxDate());
            defEMVC.setEmvcPan(pan);
            defEMVC.setEmvcCurcd(getFeptxn().getFeptxnTxCur());
            Emvc emvc = emvcMapper.selectByPrimaryKey(defEMVC.getEmvcTxDate(), defEMVC.getEmvcPan(), defEMVC.getEmvcCurcd());
            if (emvc != null) {
                // 有資料，更新 EMVC 筆數/金額
                emvc.setEmvcTxDate(getFeptxn().getFeptxnTxDate());
                emvc.setEmvcPan(pan);
                emvc.setEmvcCurcd(getFeptxn().getFeptxnTxCur());
                if (type == ATMCTxType.Accounting.getValue()) {
                    // 入帳 增加累計筆數/金額
                    emvc.setEmvcTxCnt(emvc.getEmvcTxCnt() + 1);
                    if (getFeptxn().getFeptxnRsCode() != null && getFeptxn().getFeptxnRsCode().trim().equals("Y")) {
                        // 加手續費
                        emvc.setEmvcTxAmt(BigDecimal.valueOf(emvc.getEmvcTxAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue() + getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                    } else {
                        emvc.setEmvcTxAmt(BigDecimal.valueOf(emvc.getEmvcTxAmt().doubleValue() + getFeptxn().getFeptxnTxAmt().doubleValue()));
                    }
                } else {
                    // 沖正 減掉累計筆收/金額
                    emvc.setEmvcTxCnt(emvc.getEmvcTxCnt() - 1);
                    if (getFeptxn().getFeptxnRsCode().trim().equals("Y")) {
                        // 減手續費
                        emvc.setEmvcTxAmt(BigDecimal.valueOf(emvc.getEmvcTxAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue() - getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                    } else {
                        emvc.setEmvcTxAmt(BigDecimal.valueOf(emvc.getEmvcTxAmt().doubleValue() - getFeptxn().getFeptxnTxAmt().doubleValue()));
                    }
                }

                if (emvcMapper.updateByPrimaryKeySelective(emvc) <= 0) {
                    rtnCode = IOReturnCode.UpdateFail;
                    getLogContext().setRemark(
                            "InsertEMVC-" + ((type == 1) ? "入帳" : "沖正") + "更新失敗，EMVC_TX_DATE=" + emvc.getEmvcTxDate() + "，EMVC_PAN=" + emvc.getEmvcPan() + "，EMVC_CURCD=" + emvc.getEmvcCurcd());
                    this.logMessage(getLogContext());
                    return rtnCode;
                }
            } else {
                // 無資料，新增 EMVC 筆數/金額
                if (type == ATMCTxType.Accounting.getValue()) {
                    defEMVC.setEmvcTxDate(getFeptxn().getFeptxnTxDate());
                    defEMVC.setEmvcPan(pan);
                    defEMVC.setEmvcCurcd(getFeptxn().getFeptxnTxCur());
                    defEMVC.setEmvcTxCnt(1);
                    if (getFeptxn().getFeptxnRsCode().trim().equals("Y")) {
                        // 加手續費
                        defEMVC.setEmvcTxAmt(BigDecimal.valueOf(getFeptxn().getFeptxnTxAmt().doubleValue() + getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                    } else {
                        defEMVC.setEmvcTxAmt(getFeptxn().getFeptxnTxAmt());
                    }

                    if (emvcMapper.insertSelective(defEMVC) <= 0) {
                        rtnCode = IOReturnCode.InsertFail;
                        getLogContext().setRemark("InsertEMVC-入帳新增失敗，EMVC_TX_DATE=" + defEMVC.getEmvcTxDate() + "，EMVC_PAN=" + defEMVC.getEmvcPan() + "，EMVC_CURCD=" + defEMVC.getEmvcCurcd());
                        this.logMessage(getLogContext());
                        return rtnCode;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".insertEMVC");// + MethodBase.GetCurrentMethod().Name
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * ''' <summary>
     * ''' 更新ATM狀態檔
     * ''' </summary>
     * ''' <returns></returns>
     * ''' <remarks></remarks>
     * ''' <history>
     * ''' <modify>
     * ''' <modifier>Jim</modifier>
     * ''' <reason>修改時間相關欄位值</reason>
     * ''' <date>2010/01/07</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>BugReport(001B0107):進行提款，未取卡造成吃卡的交易，card檔沒有註記RSCARD這個欄位。</reason>
     * ''' <date>2010/01/07</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Matt</modifier>
     * ''' <reason>BugReport(001B0150):預借現金CAA/CAV/CAM/CAJ，沒有取回卡片，讓ATM吃卡，在ATMSTAT的吃卡註記沒有累加。</reason>
     * ''' <date>2010/04/07</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>修改送EMS兩次的BUG</reason>
     * ''' <date>2010/05/22</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>查無原交易，要先更新狀態檔</reason>
     * ''' <date>2010/05/24</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>寫入資料庫時間 統一使用24H制</reason>
     * ''' <date>2010/05/31</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Kyo</modifier>
     * ''' <reason>調整程式</reason>
     * ''' <date>2010/06/01</date>
     * ''' </modify>
     * ''' <modify>
     * ''' <modifier>Husan</modifier>
     * ''' <reason> 本行卡時才更新卡片檔吃卡註記</reason>
     * ''' <date>2011/09/05</date>
     * ''' </modify>
     * ''' </history>
     *
     * @return
     */
    public FEPReturnCode updateATMStatus() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            Msgfile msgFile = new Msgfile(); // MSGFILE(錯誤訊息檔)
            Retain retain = new Retain(); // RETAIN(留置卡片檔)
            @SuppressWarnings("unused")
            Atmcash atmCash = new Atmcash(); // 鈔箱檔

            // 判斷FEPTXN_ATMNO是否為空值
            // New的時候，已經有查詢過一次
            if (StringUtils.isNotBlank(this.feptxn.getFeptxnAtmno())) {
                this.atmStat.setAtmstatAtmno(this.feptxn.getFeptxnAtmno());
            } else {
                return ATMReturnCode.ATMNoIsNotExist;
            }
            /**
             * [2]將 ATM_TITADATA 鈔匣模組狀況, 更新 ATMStatus 檔 及其它欄位
             */
            //--ben-20220922-//this.atmStat.setAtmstatService(Short.parseShort(this.atmReq.getSERVICE())); // ATM營業狀態
            //--ben-20220922-//this.atmStat.setAtmstatNotes(StringUtils.join(
            //--ben-20220922-//		this.atmReq.getNOTES1(), this.atmReq.getNOTES2(),
            //--ben-20220922-//		this.atmReq.getNOTES3(), this.atmReq.getNOTES4(),
            //--ben-20220922-//		this.atmReq.getNOTES5(), this.atmReq.getNOTES6(),
            //--ben-20220922-//		this.atmReq.getNOTES7(), this.atmReq.getNOTES8())); // 鈔箱1~8狀態
            //--ben-20220922-//this.atmStat.setAtmstatJournalPrint(Short.parseShort(this.atmReq.getJOUS())); // 序時紙捲狀態
            //--ben-20220922-//this.atmStat.setAtmstatReceiptPrint(Short.parseShort(this.atmReq.getADVS())); // 交易明細表狀態
            //--ben-20220922-//this.atmStat.setAtmstatDeposit(Short.parseShort(this.atmReq.getDEPS())); // 存款模組狀態
            //--ben-20220922-//this.atmStat.setAtmstatMcrw(Short.parseShort(this.atmReq.getMCRWS()));// 磁條讀寫頭狀態
            //--ben-20220922-//this.atmStat.setAtmstatEncry(Short.parseShort(this.atmReq.getENCRS())); // ENCRYPTOR狀態
            //--ben-20220922-//this.atmStat.setAtmstatStatms(Short.parseShort(this.atmReq.getSTAMS())); // 對帳單模組狀態
            //--ben-20220922-//this.atmStat.setAtmstatDispen(Short.parseShort(this.atmReq.getDISPEN())); // 吐鈔模組狀態
            // 2012/07/10 Modify by Ruling for 收到ATM電文，將ATM狀態改為連線
            this.atmStat.setAtmstatStatus((short) 0); // ATM狀態
            // 2012/09/07 Modify by Ruling for 新增硬幣機的業務:將硬幣存款(提款)模組狀態寫入ATMSTAT
            if (StringUtils.isBlank(this.atmReq.getFILLER1())) {
                this.atmStat.setAtmstatDcoin((short) 0);
            } else {
                this.atmStat.setAtmstatDcoin(Short.parseShort(this.atmReq.getFILLER1())); // 硬幣存款模組狀態
            }
            //--ben-20220922-//this.atmStat.setAtmstatWcoin(Short.parseShort(this.atmReq.getSTAMS())); // 硬幣提款模組狀態
            this.atmStat.setAtmstatAtmSeqno(this.feptxn.getFeptxnAtmSeqno()); // 最近ATM交易序號
            // BugReport(001B0365):補上BOX_DATE與CWD_DATE的搬移邏輯
            // 寫入裝鈔日期/EJ
            if (ATMTXCD.RWT.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.RWF.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.RWS.name().equals(this.feptxn.getFeptxnTxCode())) {
                // 2012-01-18 modified by KK
                this.atmStat.setAtmstatBoxDate(this.feptxn.getFeptxnTxDate());
                this.atmStat.setAtmstatBoxEfjno(this.feptxn.getFeptxnEjfno());
            }
            // 寫入最近提款日期
            if (this.atmTxData.getMsgCtl().getMsgctlTxtype2() != null
                    && (this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 1
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 9
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 10
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 11
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 12
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 13
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 14
                    || this.atmTxData.getMsgCtl().getMsgctlTxtype2() == 16)) {
                // 2012-01-18 modified by KK 寫入最近提款時間
                this.atmStat.setAtmstatCwdDate(this.feptxn.getFeptxnTxDate());
                this.atmStat.setAtmstatCwdTime(this.feptxn.getFeptxnTxTime());
            }
            // 2013/02/23 Modify by Ruling for ATM服務報表:所有電文皆記錄最近發生異常訊息代號
            //--ben-20220922-//this.atmStat.setAtmstatErrCode(this.atmReq.getEXPCD()); // 最近發生異常訊息代號
            //--ben-20220922-//if (StringUtils.isNotBlank(this.atmReq.getEXPCD())
            //--ben-20220922-//		&& this.atmReq.getEXPCD().length() >= 3
            //--ben-20220922-//		&& !"000".equals(this.atmReq.getEXPCD().substring(this.atmReq.getEXPCD().length() - 3, this.atmReq.getEXPCD().length()))) { // ATM 電文有 EXPCD欄位且 EXPCD 後三碼不為 000, 須記錄最近異常訊息代碼
            // 2010-10-26 by Ed for 配合Code Gen的變動修改判斷式，因空白被Trim掉造成的exception*/
            // 2013/02/23 Modify by Ruling for ATM服務報表:移式移至上面，所有電文皆記錄最近發生異常訊息代號
            //--ben-20220922-//	Calendar cal = CalendarUtil.rocStringToADDate(StringUtils.join(this.atmReq.getAtmseq_1(), FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
            //--ben-20220922-//	if (cal != null) {
            //--ben-20220922-//		this.atmStat.setAtmstatErrorTime(cal.getTime()); // 最近發生異常時間(轉成西元年)
            //--ben-20220922-//	}
            //--ben-20220922-//}
            this.atmStat.setAtmstatTxCode(this.feptxn.getFeptxnTxCode()); // 最近交易電文 & 最近交易日期 & 最近交易時間
            //--ben-20220922-//this.atmStat.setAtmstatTxDate(CalendarUtil.rocStringToADString(this.atmReq.getAtmseq_1())); // (轉成西元年)
            // 2012-01-18 modified by KK for 增加寫入提款時間
            this.atmStat.setAtmstatTxTime(this.feptxn.getFeptxnTxTime());
            // 2013/02/23 Modify by Ruling for ATM服務報表:所有電文皆記錄最近交易帳號 & 最近交易金額
            this.atmStat.setAtmstatBkno(this.feptxn.getFeptxnTroutBkno());
            this.atmStat.setAtmstatActno(this.feptxn.getFeptxnTroutActno());
            this.atmStat.setAtmstatAmt(this.feptxn.getFeptxnTxAmt());
            /**
             * [3]根據FEPTXN_TX_CODE會處理三種情況
             *
             * 1.OEX 異常狀況處理 2.IWD,IFT,IPY 3.AEX 留置卡片處理
             */
            // 2010-09-13 by kyo for SPEC modify: /* 9/8 新增 for SNS 處理 */
            if (ATMTXCD.SNS.name().equals(this.feptxn.getFeptxnTxCode())) {
                // 將 SNS 電文寫入 ATMSTAT
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getDISERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatDiserr(Integer.parseInt(this.atmReq.getDISERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getJOUERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatJouerr(Integer.parseInt(this.atmReq.getJOUERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getADVERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatAdverr(Integer.parseInt(this.atmReq.getADVERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getDEPERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatDeperr(Integer.parseInt(this.atmReq.getDEPERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getMCRWERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatMcrwerr(Integer.parseInt(this.atmReq.getMCRWERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getENCRERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatEncrerr(Integer.parseInt(this.atmReq.getENCRERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getSTAMERR())) {
                //--ben-20220922-//	this.atmStat.setAtmstatStamerr(Integer.parseInt(this.atmReq.getSTAMERR()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getCLSTUS())) {
                //--ben-20220922-//	this.atmStat.setAtmstatClstus(Integer.parseInt(this.atmReq.getCLSTUS()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getHCMDSTUS())) {
                //--ben-20220922-//	this.atmStat.setAtmstatHcmdstus(Integer.parseInt(this.atmReq.getHCMDSTUS()));
                //--ben-20220922-//}
                //--ben-20220922-//if (StringUtils.isNumeric(this.atmReq.getRCVRSTUS())) {
                //--ben-20220922-//	this.atmStat.setAtmstatRcvrstus(Integer.parseInt(this.atmReq.getRCVRSTUS()));
                //--ben-20220922-//}
            }
            // OEX 異常狀況處理
            else if (ATMTXCD.OEX.name().equals(this.feptxn.getFeptxnTxCode())) {
                rtnCode = this.updateATMStatusForOEX();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                // 2016/11/17 ATM定時發送OEX電文
                //--ben-20220922-//if ("N".equals(this.atmReq.getNEWOEX())) {
                //--ben-20220922-//	rtnCode = this.updateATMStatusForOEXNew();
                //--ben-20220922-//	if (rtnCode != CommonReturnCode.Normal) {
                //--ben-20220922-//		return rtnCode;
                //--ben-20220922-//	}
                //--ben-20220922-//} // IWD, IFT, IPY
            } else if (ATMTXCD.IWD.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.IFT.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.IPY.name().equals(this.feptxn.getFeptxnTxCode())) {
                // 最近交易帳號 & 最近交易金額
                // 2013/02/23 Modify by Ruling for ATM服務報表:移式移至上面，所有電文皆記錄最近交易帳號 & 最近交易金額
            }
            // 3.AEX 留置卡片處理
            else if (ATMTXCD.AEX.name().equals(this.feptxn.getFeptxnTxCode())) {
                //--ben-20220922-//msgFile = this.msgFileMapper.selectByPrimaryKey(1, this.atmReq.getEXPCD()); // 檢核留置卡片註記
                if (msgFile != null) {
                    // BugReport(001B0150):預借現金CAA/CAV/CAM/CAJ，沒有取回卡片，讓ATM吃卡，在ATMSTAT的吃卡註記沒有累加。
                    if (PolyfillUtil.ctype(msgFile.getMsgfileRetain())) {
                        this.atmStat.setAtmstatEatCard(this.atmStat.getAtmstatEatCard() + 1); // 累加吃卡總數
                        // 2017/06/15 Modify by Ruling for 留置卡片將卡號存入FEPTXN
                        //--ben-20220922-//this.feptxn.setFeptxnMajorActno(this.atmReq.getCHACT());
                        // 2010-05-31 by kyo for 寫入資料庫時間 統一使用24H制
                        Calendar cal =
                                //--ben-20220922-//CalendarUtil.rocStringToADDate(StringUtils.join(this.atmReq.getAtmseq_1(), FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
                                CalendarUtil.rocStringToADDate(StringUtils.join("", FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
                        if (cal != null) {
                            retain.setRetainTime(cal.getTime()); // 將卡片資料寫入留置卡片檔
                        }
                        //--ben-20220922-//retain.setRetainAtmno(StringUtils.join(this.atmReq.getBRNO(), this.atmReq.getWSNO()));
                        //--ben-20220922-//retain.setRetainBkno(this.atmReq.getBKNO());
                        //--ben-20220922-//retain.setRetainCardno(this.atmReq.getCHACT());
                        retain.setRetainCardSeq((short) 0);
                        retain.setRetainTbsdy(this.feptxn.getFeptxnTbsdy());
                        //--ben-20220922-//retain.setRetainErrCode(this.atmReq.getEXPCD());
                        retain.setRetainBrnoAdm(this.atmMstr.getAtmBrnoMa()); // ATM 管理分行
                        // 其他欄位給初值
                        if (this.retainMapper.insert(retain) == 0) {
                            rtnCode = IOReturnCode.RETAINInsertError;
                            return rtnCode;
                        }
                        // BugReport(001B0107):更新卡片檔吃卡記號
                        // 2011-09-05 by Husan 程式調整
                        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTroutBkno())) {
                            Feptxn feptxn = this.checkConData();
                            if (feptxn != null) {
                                // 2012/08/08 Modify by Ruling for AEX電文要檢核原交易帳號及金額，如與原交易相等則更新原交易之卡片檔吃卡註記
                                //--ben-20220922-//if (this.atmReq.getTXACT().equals(feptxn.getFeptxnTroutActno())
                                //--ben-20220922-//		&& feptxn.getFeptxnTxAmt().compareTo(this.atmReq.getTXAMT()) == 0) {
                                //--ben-20220922-//	rtnCode = this.updateRetainCard(feptxn);
                                //--ben-20220922-//}
                            } else {
                                // 2010-05-24 by kyo for 查無原交易，要先更新狀態檔
                                if (this.atmstatMapper.updateByPrimaryKey(this.atmStat) == 0) {
                                    rtnCode = IOReturnCode.ATMSTATUpdateError;
                                    return rtnCode;
                                }
                                // BugReport(001B0497):2010-05-19 modified by kyo for
                                // 沒取得原交易，需要回錯誤碼OriginalMessageNotFound
                                // 2010-05-22 by kyo for 多送一次EMS，應該只送一次
                                return CommonReturnCode.OriginalMessageNotFound;
                            }
                        }
                    }
                } else {
                    // 2010-06-01 by kyo 程式調整
                    return IOReturnCode.MSGFILENotFound;
                }
            }

            // [4]更新 ATMSTATUS
            if (atmstatMapper.updateByPrimaryKey(atmStat) == 0) {
                // 2010-06-01 by kyo 程式調整
                return IOReturnCode.ATMSTATUpdateError;
            }
            return rtnCode;
        } catch (Exception e) {
            this.getLogContext().setProgramException(e);
            sendEMS(this.getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateATMStatusForOEX() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            // Fly 2019/05/15 BDM納入FEP監控
            //--ben-20220922-//if (StringUtils.isBlank(this.atmReq.getQRCODE())) {
            //--ben-20220922-//	this.atmStat.setAtmstatQrcode((short) 0);
            //--ben-20220922-//} else {
            //--ben-20220922-//	this.atmStat.setAtmstatQrcode(Short.parseShort(this.atmReq.getQRCODE()));
            //--ben-20220922-//}
            if (FeptxnEXCD.A003.name().equals(this.feptxn.getFeptxnExcpCode())) {
                // 2010-05-05 modified by kyo for CODING ERROR欄位搬移錯誤應該為supv而非status
                this.atmStat.setAtmstatSupv((short) 1); // 進入行員功能
            } else if (FeptxnEXCD.A004.name().equals(this.feptxn.getFeptxnExcpCode())) {
                this.atmStat.setAtmstatSupv((short) 0); // 離開行員功能
            } else if (FeptxnEXCD.R002.name().equals(this.feptxn.getFeptxnExcpCode())) {
                //--ben-20220922-//switch (this.atmReq.getMsgtypeBody()) {
                switch ("") {
                    case "01":
                        this.atmStat.setAtmstatPmd1((short) 1);
                    case "02":
                        this.atmStat.setAtmstatPmd2((short) 1);
                    case "03":
                        this.atmStat.setAtmstatPmd3((short) 1);
                    case "04":
                        this.atmStat.setAtmstatPmd4((short) 1);
                        // Fly 2018/11/22 跑馬燈增加05(財金暫停(異常))/99(其他)
                    case "05":
                        this.atmStat.setAtmstatPmd5((short) 1);
                    case "99":
                        // Fly 2020/10/14 跑馬燈增加顯示(06~98):依工作單執行
                        this.atmStat.setAtmstatPmd99((short) 1);
                        // this.atmStat.setAtmstatPmdcd(this.atmReq.getMsgtypeBody()); // TODO 沒有這個欄位???
                    default:
                        this.atmStat.setAtmstatPmd99((short) 1);
                        // this.atmStat.setAtmstatPmdcd(this.atmReq.getMsgtypeBody()); // TODO 沒有這個欄位???
                }
            } else if (FeptxnEXCD.R003.name().equals(this.feptxn.getFeptxnExcpCode())) {
                //--ben-20220922-//switch (this.atmReq.getMsgtypeBody()) {
                switch ("") {
                    case "01":
                        this.atmStat.setAtmstatPmd1((short) 0);
                    case "02":
                        this.atmStat.setAtmstatPmd2((short) 0);
                    case "03":
                        this.atmStat.setAtmstatPmd3((short) 0);
                    case "04":
                        this.atmStat.setAtmstatPmd4((short) 0);
                        // Fly 2018/11/22 跑馬燈增加05(財金暫停(異常))/99(其他)
                    case "05":
                        this.atmStat.setAtmstatPmd5((short) 0);
                    case "99":
                        // Fly 2020/10/14 跑馬燈增加顯示(06~98):依工作單執行
                        this.atmStat.setAtmstatPmd99((short) 1);
                        // this.atmStat.setAtmstatPmdcd(this.atmReq.getMsgtypeBody()); // TODO 沒有這個欄位???
                    default:
                        this.atmStat.setAtmstatPmd99((short) 1);
                        // this.atmStat.setAtmstatPmdcd(this.atmReq.getMsgtypeBody()); // TODO 沒有這個欄位???
                }
            }
            // BugReport(001B0365):新增B002(ATM 暫停服務)處理邏輯原B001為ATM 開始服務
            // 2010-05-12 modified by kyo for spec修改邏輯:拿掉B002判斷，修改B001邏輯
            else if (FeptxnEXCD.B001.name().equals(this.feptxn.getFeptxnExcpCode())) {
                // 2012/05/31 Modify by Ruling for 配合永豐修改，判斷次程式版本/廣告版本日期是否有異動
                //--ben-20220922-//if (StringUtils.isNotBlank(this.atmReq.getAtmverPgm())
                //--ben-20220922-//		&& StringUtils.isNotBlank(CalendarUtil.rocStringToADString(this.atmReq.getAtmverPgm().trim()))) { // 更新ATM程式版本
                //--ben-20220922-//	String pgmDate = CalendarUtil.rocStringToADString(this.atmReq.getAtmverPgm().trim());
                //--ben-20220922-//	if (!pgmDate.equals(this.atmStat.getAtmstatApVersionN().trim())) {
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionO(this.atmStat.getAtmstatApVersionN());
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionDateO(this.atmStat.getAtmstatApVersionDateN());
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionTimeO(this.atmStat.getAtmstatApVersionTimeN());
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionN(pgmDate); // 將 ATM 電文程式版本寫入本次
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionDateN(this.feptxn.getFeptxnTxDate());
                //--ben-20220922-//		this.atmStat.setAtmstatApVersionTimeN(this.feptxn.getFeptxnTxTime());
                //--ben-20220922-//	}
                //--ben-20220922-//}

                //--ben-20220922-//if (StringUtils.isNotBlank(this.atmReq.getAtmverAdv())
                //--ben-20220922-//		&& StringUtils.isNotBlank(CalendarUtil.rocStringToADString(this.atmReq.getAtmverAdv().trim()))) { // 更新廣告版本
                //--ben-20220922-//	String advDate = CalendarUtil.rocStringToADString(this.atmReq.getAtmverAdv().trim());
                //--ben-20220922-//	if (!advDate.equals(this.atmStat.getAtmstatAdvVersionN().trim())) {
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionO(this.atmStat.getAtmstatApVersionN()); // 將本次廣告程式版本/日期/時間搬至上次
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionDateO(this.atmStat.getAtmstatApVersionDateN());
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionTimeO(this.atmStat.getAtmstatApVersionTimeN());
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionN(advDate); // 將 ATM 電文程式版本寫入本次
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionDateN(this.feptxn.getFeptxnTxDate());
                //--ben-20220922-//		this.atmStat.setAtmstatAdvVersionTimeN(this.feptxn.getFeptxnTxTime());
                //--ben-20220922-//	}
                //--ben-20220922-//}

                // 更新ATM服務狀態
                this.atmStat.setAtmstatService(DbHelper.toShort(false)); // 服務中
                // 2014/05/27 Modify by Ruling for 永豐要求修改OEX送B001(自動櫃員機開始服務)時表示離開行員功能
                this.atmStat.setAtmstatSupv((short) 0); // 離開行員功能
            }
            return rtnCode;
        } catch (Exception e) {
            this.getLogContext().setProgramException(e);
            sendEMS(this.getLogContext());
            return IOReturnCode.ATMSTATUpdateError;
        }
    }

    private FEPReturnCode updateATMStatusForOEXNew() {
        // TODO 自動生成的方法存根 Richard
        return null;
    }

    private FEPReturnCode updateRetainCard(Feptxn feptxn) {
        // TODO 自動生成的方法存根 Richard
        return null;
    }

    /**
     * @param atmcashes
     * @return
     */
    public int updateDataSet(List<Atmcash> atmcashes) {
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int rowsAffected = 0;
            for (Atmcash atmcash : atmcashes) {
                if (atmcashExtMapper.selectByPrimaryKey(atmcash.getAtmcashAtmno(), atmcash.getAtmcashBoxno()) == null) {
                    atmcashExtMapper.insertSelective(atmcash);
                    rowsAffected += 1;
                } else {
                    atmcashExtMapper.updateByPrimaryKeySelective(atmcash);
                    rowsAffected += 1;
                }
            }
            /*
             * for (Atmcash atmcash : atmcashes) {
             * atmcashMapper.deleteByPrimaryKey(atmcash.getAtmcashAtmno(), atmcash.getAtmcashBoxno());
             * rowsAffected += 1;
             * }
             */
            transactionManager.commit(txStatus);
            return rowsAffected;
        } catch (Exception e) {
            // 設置手動回滾
            transactionManager.rollback(txStatus);
            throw e;
        }
    }

    public FEPReturnCode processBCD() {

        Activity defACTIVITY = new Activity();
        Barcode defBARCODE = new Barcode();
        try {
            // 1.判斷交易日期是否在活動期間
            // 檢核活動檔
            //--ben-20220922-//defACTIVITY.setActivityId(atmReq.getACTID().intValue());
            Activity activity = activityMapper.selectByPrimaryKey(defACTIVITY.getActivityId());
            if (activity == null) {
                getLogContext()
                        .setRemark("ProcessBCD-找不到ACTIVITY資料, ACTIVITY_ID = " + defACTIVITY.getActivityId().toString());
                this.logMessage(getLogContext());
                return ATMReturnCode.OtherCheckError;
            }

            // 檢核活動期間
            if (getFeptxn().getFeptxnTxDate().compareTo(activity.getActivityBeginDate()) < 0
                    || getFeptxn().getFeptxnTxDate().compareTo(activity.getActivityEndDate()) > 0) {
                getLogContext().setRemark("ProcessBCD-不在活動期間內, ACTIVITY_BEGIN_DATE = " + activity.getActivityBeginDate()
                        + ", ACTIVITY_END_DATE = " + activity.getActivityEndDate());
                this.logMessage(getLogContext());
                return ATMReturnCode.OtherCheckError;
            }

            // 2.取得活動條碼
            //--ben-20220922-//defBARCODE.setBarcodeActId(atmReq.getACTID().intValue());
            Barcode barcode = barcodeExtMapper.queryByActId(defBARCODE.getBarcodeActId());
            if (barcode == null) {
                getLogContext().setRemark(
                        "ProcessBCD-找不到BARCODE資料, BARCODE_ACT_ID = " + defBARCODE.getBarcodeActId().toString());
                this.logMessage(getLogContext());
                return ATMReturnCode.OtherCheckError;
            } else {
                // 將活動條碼傳回ATM
                //ben20221118	atmRes.setBARCODE(barcode.getBarcodeCode());
                // 更新BARCODE資料
                barcode.setBarcodeTxDate(getFeptxn().getFeptxnTxDate());
                barcode.setBarcodeEjfno(getFeptxn().getFeptxnEjfno());
                barcode.setBarcodeStatus("Y"); // 已用
                if (barcodeExtMapper.updateByPrimaryKey(barcode) <= 0) {
                    getLogContext().setTableName("BARCODE");
                    getLogContext().setPrimaryKeys("BARCODE_ACT_ID:" + barcode.getBarcodeActId().toString()
                            + ";BARCODE_CODE:" + barcode.getBarcodeCode());
                    getLogContext().setReturnCode(IOReturnCode.UpdateFail);
                    this.logMessage(getLogContext());
                    return IOReturnCode.UpdateFail;
                }
            }

            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "." + "processBCD");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 2021-07-19 Richard add
     * <p>
     * 取得跨行存款手續費
     * New Function for ATM新功能-跨行存款
     *
     * @param refFee
     * @return
     */
    public FEPReturnCode getODRFEE(RefBase<BigDecimal> refFee) {
        Inbkparm inbkparm = new Inbkparm();
        InbkparmExtMapper inbkparmExtMapper = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
        try {
            inbkparm.setInbkparmApid("ODR");
            inbkparm.setInbkparmPcode(StringUtils.EMPTY);
            inbkparm.setInbkparmAcqFlag("A");
            inbkparm.setInbkparmEffectDate(this.getFeptxn().getFeptxnTxDate());
            inbkparm.setInbkparmCur(this.getFeptxn().getFeptxnTxCur());
            Inbkparm record = inbkparmExtMapper.queryByPK(inbkparm);
            if (record == null) {
                this.logContext.setRemark(StringUtils.join(
                        "INBKPARM 找不到跨行存款手續費,INBKPARM_APID=", inbkparm.getInbkparmApid(),
                        " INBKPARM_PCODE=", inbkparm.getInbkparmPcode(),
                        " INBKPARM_ACQ_FLAG=", inbkparm.getInbkparmAcqFlag(),
                        " INBKPARM_EFFECT_DATE=", inbkparm.getInbkparmEffectDate(),
                        " INBKPARM_CUR=", inbkparm.getInbkparmCur()));
                this.logMessage(this.logContext);
                return ATMReturnCode.OtherCheckError;
            }
            if (!DbHelper.toBoolean(record.getInbkparmFeeType())) {
                // 固定金額
                refFee.set(record.getInbkparmFeeCustpay());
                this.logContext.setRemark(StringUtils.join("跨行存款手續費=", refFee.get().toString()));
                this.logMessage(this.logContext);
            }
            return CommonReturnCode.Normal;
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".getODRFEE"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 2021-07-20 Richard add
     * <p>
     * 跨行存款交易累計處理
     *
     * @param type 1:入帳 2:沖正
     * @return
     */
    public FEPReturnCode insertODRC(int type) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Odrc odrc = new Odrc();
        OdrcMapper odrcMapper = SpringBeanFactoryUtil.getBean(OdrcMapper.class);
        try {
            odrc.setOdrcTxDate(this.getFeptxn().getFeptxnTxDate());
            odrc.setOdrcBkno(this.getFeptxn().getFeptxnTroutBkno());
            odrc.setOdrcActno(this.getFeptxn().getFeptxnMajorActno());
            odrc.setOdrcCurcd(this.getFeptxn().getFeptxnTxCur());
            Odrc record = odrcMapper.selectByPrimaryKey(odrc.getOdrcTxDate(), odrc.getOdrcBkno(), odrc.getOdrcActno(), odrc.getOdrcCurcd());
            if (record != null) {
                // 更新 ODRC 筆數/金額
                record.setOdrcTxDate(this.getFeptxn().getFeptxnTxDate());
                record.setOdrcBkno(this.getFeptxn().getFeptxnTroutBkno());
                record.setOdrcActno(this.getFeptxn().getFeptxnMajorActno());
                record.setOdrcCurcd(this.getFeptxn().getFeptxnTxCur());
                if (type == ATMCTxType.Accounting.getValue()) {
                    // 入帳 增加累計筆數/金額
                    record.setOdrcTxCnt(record.getOdrcTxCnt() + 1);
                    record.setOdrcTxAmt(record.getOdrcTxAmt().add(this.getFeptxn().getFeptxnTxAmt()));
                } else {
                    // 沖正 減掉累計筆收/金額
                    record.setOdrcTxCnt(record.getOdrcTxCnt() - 1);
                    record.setOdrcTxAmt(record.getOdrcTxAmt().subtract(this.getFeptxn().getFeptxnTxAmt()));
                }

                if (odrcMapper.updateByPrimaryKey(record) <= 0) {
                    rtnCode = IOReturnCode.UpdateFail;
                    this.logContext.setRemark(StringUtils.join(
                            "InsertODRC-", (type == 1 ? "入帳" : "沖正"), "更新失敗",
                            "，ODRC_TX_DATE=", record.getOdrcTxDate(),
                            "，ODRC_BKNO=", record.getOdrcBkno(),
                            "，ODRC_ACTNO=", record.getOdrcActno(),
                            "，ODRC_CURCD=", record.getOdrcCurcd()));
                    this.logMessage(this.logContext);
                    return rtnCode;
                }
            } else {
                // 無資料，新增 ODRC 筆數/金額
                if (type == ATMCTxType.Accounting.getValue()) {
                    odrc.setOdrcTxCnt(1);
                    odrc.setOdrcTxAmt(this.getFeptxn().getFeptxnTxAmt());
                    if (odrcMapper.insertSelective(odrc) <= 0) {
                        rtnCode = IOReturnCode.InsertFail;
                        this.logContext.setRemark(StringUtils.join(
                                "InsertODRC-入帳新增失敗，ODRC_TX_DATE=", odrc.getOdrcTxDate(),
                                "，ODRC_BKNO=", odrc.getOdrcBkno(),
                                "，ODRC_ACTNO=", odrc.getOdrcActno(),
                                "，ODRC_CURCD=", odrc.getOdrcCurcd()));
                        this.logMessage(this.logContext);
                        return rtnCode;
                    }
                }
            }
            return rtnCode;
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".getODRFEE"));
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    // 呼叫永豐的 Chip.dll 取得密碼
    public String getATMPWD(String cardNo) {
        logContext.setRemark("GetATMPWD-取得密碼");
        this.logMessage(logContext);
        String sSsCode = ChipICPIN.getSCode(cardNo);
        return sSsCode;
    }

    /**
     * 列印Coupon券紀錄
     *
     *
     * <history> <modify> <modifier>Ruling</modifier> <reason>NEW</reason>
     * <date>2014/11/14</date> </modify> </history>
     */
    public final FEPReturnCode insertCPN() {
        Coupon defCOUPON = new Coupon();
        CouponMapper dbCOUPON = SpringBeanFactoryUtil.getBean(CouponMapper.class);
        String sDate = "";
        String eDate = "";
        String fileName = "";
        String prtTime = "";

        try {
            for (int I = 0; I <= 5; I++) {
                switch (I) {
                    case 0:
                        //--ben-20220922-//sDate = atmReq.getSDATE1().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE1().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME1().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME1().trim();
                        break;
                    case 1:
                        //--ben-20220922-//sDate = atmReq.getSDATE2().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE2().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME2().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME2().trim();
                        break;
                    case 2:
                        //--ben-20220922-//sDate = atmReq.getSDATE3().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE3().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME3().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME3().trim();
                        break;
                    case 3:
                        //--ben-20220922-//sDate = atmReq.getSDATE4().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE4().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME4().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME4().trim();
                        break;
                    case 4:
                        //--ben-20220922-//sDate = atmReq.getSDATE5().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE5().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME5().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME5().trim();
                        break;
                    case 5:
                        //--ben-20220922-//sDate = atmReq.getSDATE6().trim();
                        //--ben-20220922-//eDate = atmReq.getEDATE6().trim();
                        //--ben-20220922-//fileName = atmReq.getFILENAME6().trim();
                        //--ben-20220922-//prtTime = atmReq.getPRTIME6().trim();
                        break;
                }

                if (StringUtils.isNotBlank(fileName)) {
                    defCOUPON.setCouponTxDate(getFeptxn().getFeptxnTxDate());
                    defCOUPON.setCouponTxTime(getFeptxn().getFeptxnTxTime());
                    defCOUPON.setCouponAtmno(getFeptxn().getFeptxnAtmno());
                    defCOUPON.setCouponSdate(sDate);
                    defCOUPON.setCouponEdate(eDate);
                    defCOUPON.setCouponFilename(fileName);
                    defCOUPON.setCouponPrtTime(prtTime);

                    if (dbCOUPON.insert(defCOUPON) <= 0) {
                        logContext.setRemark("COUPON 新增資料失敗!! COUPON_TX_DATE=" + getFeptxn().getFeptxnTxDate()
                                + ", COUPON_TX_TIME=" + getFeptxn().getFeptxnTxTime() + ", COUPON_ATMNO="
                                + getFeptxn().getFeptxnAtmno());
                        this.logMessage(logContext);
                    }
                }
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 計算原幣戶提領原幣手續費
     *
     * @return FEPReturnCode 計算外幣戶提領外幣手續費 <history> <modify>
     * <modifier>Kyo</modifier> <reason>Function design</reason>
     * <date>2010/12/02</date> </modify> <modify> <modifier>Kyo</modifier>
     * <reason>日幣小數點取到整數</reason> <date>2011/05/11</date> </modify>
     * </history>
     */
    public final FEPReturnCode getFee() {
        Inbkparm defINBKPARM = new Inbkparm();
        InbkparmExtMapper dbINBKPARM = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
        try {
            defINBKPARM.setInbkparmApid(ATMTXCD.IFW.name());
            defINBKPARM.setInbkparmAcqFlag(InbkparmAcqFlag.Acquire);
            defINBKPARM.setInbkparmEffectDate(getFeptxn().getFeptxnTbsdy());
            defINBKPARM.setInbkparmCur(getFeptxn().getFeptxnTxCur());
            // 2012/09/19 Modify by Ruling for 點掉交易戶金額在INBKPARM金額區間，避免抓不到手續費
            // defINBKPARM.INBKPARM_RANGE_FROM = FepTxn.FEPTXN_TX_AMT
            Inbkparm inbkparm = dbINBKPARM.queryByPK(defINBKPARM);
            if (inbkparm == null) {
                return FEPReturnCode.NoRateTable; // "E211" /* 查無匯率 */
            }

            if (!DbHelper.toBoolean(inbkparm.getInbkparmFeeType())) {
                /// *固定金額*/
                getFeptxn().setFeptxnFeeCustpayAct(inbkparm.getInbkparmFeeCustpay());
            } else {
                /// * 依提領金額百分比 */
                if (CurrencyType.JPY.name().equals(getFeptxn().getFeptxnTxCur())) {
                    getFeptxn().setFeptxnFeeCustpayAct(MathUtil.roundUp(getFeptxn().getFeptxnTxAmt()
                            .multiply(inbkparm.getInbkparmFeeCustpay()).divide(new BigDecimal(100)), 0));
                } else {
                    getFeptxn().setFeptxnFeeCustpayAct(MathUtil.roundUp(getFeptxn().getFeptxnTxAmt()
                            .multiply(inbkparm.getInbkparmFeeCustpay()).divide(new BigDecimal(100)), 2));
                }
                if (getFeptxn().getFeptxnFeeCustpayAct().compareTo(inbkparm.getInbkparmFeeMin()) < 0) {
                    getFeptxn().setFeptxnFeeCustpayAct(inbkparm.getInbkparmFeeMin());
                }
            }

        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 準備VATXN物件
     *
     * @return <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <date>2018/09/17</date>
     * <reason>2566約定及核驗服務類別項目</reason>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareVATXNforVA(RefBase<Vatxn> vatxn, RCV_VA_GeneralTrans_RQ tita) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnStan(getFeptxn().getFeptxnStan());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            String VACATE = tita.getBody().getRq().getSvcRq().getVACATE();//'業務類別代號’
            String AEIPYTP = tita.getBody().getRq().getSvcRq().getAEIPYTP();//‘交易類別’
            vatxn.get().setVatxnCate(VACATE);
            vatxn.get().setVatxnType(AEIPYTP);
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnTroutKind(null == getFeptxn().getFeptxnTroutKind() ? StringUtils.SPACE : getFeptxn().getFeptxnTroutKind());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());

            // TODO: 2021/6/22
            vatxn.get().setUpdateUserid(0);
            vatxn.get().setUpdateTime(new Date());

            switch (vatxn.get().getVatxnCate()) {
//                case "01": // 業務類別(01):境內電子支付約定連結申請
                case "02": // 業務類別(02): 線上約定繳費
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnBusino(tita.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
                    vatxn.get().setVatxnBusinessUnit(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO());
                    vatxn.get().setVatxnPaytype(tita.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
                    vatxn.get().setVatxnFeeno(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
                    vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                    vatxn.get().setVatxnPactno(tita.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2());
                    vatxn.get().setVatxnActno(getFeptxn().getFeptxnTroutActno());
                    break;
                case "10":
                    vatxn.get().setVatxnItem(tita.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP());
                    // Fly 2019/01/03 將財金回應結果寫入
                    switch (vatxn.get().getVatxnItem()) {
                        case "00": //'除卡片及帳號外，無其他核驗項目
                            // 代財金回覆4001 異動
//	                    	 getFeptxn().setFeptxnTrk3(fiscINBKReq.getMEMO());
                        case "01": // 身份證號或外國人統一編號
                            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                            break;
                        case "02": // 持卡人之行動電話號碼
                            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                            break;
                        case "03": // 持卡人之出生年月日
                            vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                            break;
                        case "04": // 持卡人之住家電話號碼
                            vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                            break;
                        case "11": // 持卡人之身分證號及行動電話號碼
                            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                            break;
                        case "12": // 持卡人之身分證號、行動電話號碼及出生年月
                            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                            vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                            break;
                        case "13": // 持卡人之身分證號、行動電話號碼及住家電話
                            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                            vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                            break;
                        case "14": // 持卡人之身分證號、行動電話號碼、出生年月日及住家電話號碼
                            vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                            vatxn.get().setVatxnMobile(tita.getBody().getRq().getSvcRq().getMOBILENO());
                            vatxn.get().setVatxnBirthday(tita.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY());
                            vatxn.get().setVatxnHphone(tita.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME());
                            break;
                    }
                    break;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareVATXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    public FEPReturnCode prepareFEPTXNTCB() {
        try {
            getFeptxntcb().setFeptxntcbTxDate(getFeptxn().getFeptxnTxDate());
            getFeptxntcb().setFeptxntcbEjfno(getFeptxn().getFeptxnEjfno());
            // 報表判斷條件，避免主機逾時無法更新，先給值
            if ( getFeptxn().getFeptxnFiscFlag()==0 || StringUtils.equals(getFeptxn().getFeptxnTxCode(), "D6") ) {
                getFeptxntcb().setFeptxntcbTxnflow("C"); // 自行
            }else{
                getFeptxntcb().setFeptxntcbTxnflow("A"); // 跨行
            }

            // Language identifier (offset 71-72)
            getFeptxntcb().setFeptxntcbLangid(atmReq.getLANGID());
            // From-account data (offset 75-76)  //2024-06-20 增加判斷條件
            if(("P5".equals(getFeptxn().getFeptxnTxCode()) || "P6".equals(getFeptxn().getFeptxnTxCode()))) {
                //國際卡-帳戶類別
                getFeptxntcb().setFeptxntcbFacode(atmReq.getACCODE());
            }else if("ATM".equals(getFeptxn().getFeptxnChannel())
                    && StringUtils.isNotBlank(getFeptxn().getFeptxnPcode()) && "252".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
                //ATM轉帳-收款戶類型
                getFeptxntcb().setFeptxntcbActtype(atmReq.getSPECIALDATA().substring(16, 18));
            }else if(StringUtils.isNotBlank(getFeptxn().getFeptxnPcode()) && "26".equals(getFeptxn().getFeptxnPcode().substring(0, 2)) && getFeptxn().getFeptxnTmoFlag() != null){
                //國際卡-帳戶類別
                getFeptxntcb().setFeptxntcbFacode(atmReq.getACCODE());
                //IC卡序號
                getFeptxntcb().setFeptxntcbPicdseq(atmReq.getPICDSEQ());
                //POS ENTRY MODE
                getFeptxntcb().setFeptxntcbPiposent(atmReq.getPIPOSENT());
                //第二軌資料讀取來源：C / M
                getFeptxntcb().setFeptxntcbPitk2frm(atmReq.getPITK2FRM());
                        //晶片處理結果(FAA: 放空白，FSE時才有可能有值)
                getFeptxntcb().setFeptxntcbPiarpcrc(atmReq.getPIARPCRC());
                //銀聯卡卡號最多19位，另存於FEPTXNTCB
                if(StringUtils.equals(getFeptxn().getFeptxnPcode().substring(0, 3), "260") && getFeptxn().getFeptxnTmoFlag() == 1) {
                    String processedEmvCard = getFeptxn().getFeptxnTrk2().split("[D=]", 2)[0];
                    getFeptxntcb().setFeptxntcbEmvcard(
                            processedEmvCard.length() > 19 ? processedEmvCard.substring(0, 19) : processedEmvCard
                    );
                } else {
                    if(StringUtils.isNotBlank(getFeptxn().getFeptxnTroutActno())){
                        getFeptxntcb().setFeptxntcbEmvcard(getFeptxn().getFeptxnTroutActno());
                    }
                }
            }else if(StringUtils.isNotBlank(atmReq.getFADATA())){
                getFeptxntcb().setFeptxntcbFacode(atmReq.getFADATA().substring(0 ,2));
            }
            // CARD STATUS 卡片留置(offset 52)，等IMS 下送確認，會再UPDATE
            getFeptxntcb().setFeptxntcbPbmcrd(atmReq.getSTATUS().substring(14, 15));
            //晶片金融卡代號(offset 179)
            getFeptxntcb().setFeptxntcbPiccdid(atmReq.getPICCDID());

            if (StringUtils.equals("D8", getFeptxn().getFeptxnTxCode())) { // 企業存款
                // 企業存款銷帳編號(授權代碼)(offset 148)
                getFeptxntcb().setFeptxntcbD9cno(atmReq.getCARDDATA().substring(6, 19));
            }

            if (StringUtils.equals(getFeptxn().getFeptxnTxCode(), "US") || StringUtils.equals(getFeptxn().getFeptxnTxCode(), "JP")) {// 外幣提款
                //本國人/外國人身分別代號(offset 109)
                getFeptxntcb().setFeptxntcbFcitype(atmReq.getTADATA().substring(10, 11));
            }

            if (!StringUtils.equals("FV", getFeptxn().getFeptxnTxCode())) { //非指靜脈申請交易
                // Identification card data indicator (offset 141)
                getFeptxntcb().setFeptxntcbCardfmt(atmReq.getCARDFMT());
            }

            // 轉換下送ATM的交易類別(報表分類使用)
            if(StringUtils.equals(getFeptxn().getFeptxnTxCode().substring(0, 1), "T")
                    && StringUtils.equals(getFeptxn().getFeptxnMtp(), "Y")) {
                getFeptxntcb().setFeptxntcbTxntypeCode("TM"); // 手機門號轉帳
            }else if(StringUtils.equals(getFeptxn().getFeptxnTxCode().substring(0, 1), "T")
                    || StringUtils.equals(getFeptxn().getFeptxnTxCode().substring(0, 1), "E")) {
                getFeptxntcb().setFeptxntcbTxntypeCode("53"); // 一般門號轉帳及繳費
            }else if(StringUtils.equals(getFeptxn().getFeptxnTxCode(), "P1")
                    && StringUtils.equals(getFeptxntcb().getFeptxntcbCardfmt(), "0")) {
                getFeptxntcb().setFeptxntcbTxntypeCode("10"); //磁條密碼變更
            }else if(StringUtils.equals(getFeptxn().getFeptxnTxCode(), "LF")){
                getFeptxntcb().setFeptxntcbTxntypeCode("CL"); // 金融帳戶核驗
                if ( ("TT").equals(getFeptxn().getFeptxnActivityCode())) { //全民普發身分驗證
                    getFeptxntcb().setFeptxntcbHealthcard(atmReq.getTADATA().trim());
                }
                if (!getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno()))
                {
                    getFeptxntcb().setFeptxntcbTxnflow("A");
                }
            }else if(StringUtils.isNotBlank(getFeptxn().getFeptxnPcode())
                    && StringUtils.equals(getFeptxn().getFeptxnPcode().substring(0, 2), "26")
                    && StringUtils.equals(getFeptxn().getFeptxnPcode().substring(3, 4), "1")) {
                getFeptxntcb().setFeptxntcbTxntypeCode("09"); // 國際卡餘額查詢
            }else if(StringUtils.isNotBlank(getFeptxn().getFeptxnPcode())
                    && StringUtils.equals(getFeptxn().getFeptxnPcode().substring(0, 2), "26")) {
                getFeptxntcb().setFeptxntcbTxntypeCode("01"); // 國際卡提款及預借現金
            }else {
                switch(getFeptxn().getFeptxnTxCode()) {
                    case  "I1":
                    case  "I2":
                        //餘額查詢
                        getFeptxntcb().setFeptxntcbTxntypeCode("59");
                        break;
                    case "F1":
                    case "F2":
                    case "F3":
                    case "F4":
                    case "F5":
                    case "F6":
                    case "F7":
                    case "W1":
                    case "US":
                    case "JP":
                        //提款
                        getFeptxntcb().setFeptxntcbTxntypeCode("51");
                        break;
                    case "WP":
                        //提款
                        getFeptxntcb().setFeptxntcbTxntypeCode("51");
                        //全民普發現金一萬，紀錄2566驗證交易序號及健保卡號
                        getFeptxntcb().setFeptxntcb2566Stan(atmReq.getSPECIALDATA().substring(46, 53)); //ATM_TITA.SPECIALDATA[47,7]
                        getFeptxntcb().setFeptxntcbHealthcard(atmReq.getTADATA().trim());
                        getFeptxntcb().setFeptxntcbTxntypeCode("WP");
                        break;
                    case "D5":
                    case "D6":
                    case "DA":
                    case "DC":
                    case "D8":
                        //卡片存款、企業存款
                        getFeptxntcb().setFeptxntcbTxntypeCode("DP");
                        break;
                    case "P1":
                        getFeptxntcb().setFeptxntcbTxntypeCode("60");
                        break;
                    case "P5":
                    case "P6":
                        getFeptxntcb().setFeptxntcbTxntypeCode("10");
                        break;
                    case  "SS":
                        getFeptxntcb().setFeptxntcbTxntypeCode("58");
                        break;
                    case  "NP":
                        getFeptxntcb().setFeptxntcbTxntypeCode("N" + atmReq.getSPECIALDATA().substring(0, 1));
                        break;
                    default:
                        getFeptxntcb().setFeptxntcbTxntypeCode(getFeptxn().getFeptxnTxCode());
                }
            }
            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /// #Region "財金相關處理"

    /**
     * 取得新的STAN(7碼)
     *
     * @return <history>
     * <modify>
     * <modifier>Ashiang</modifier>
     * <reason></reason>
     * <date>2009/12/01</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>整批轉即的批次會取到相同的STAN，修正GetStan會重覆問題</reason>
     * <date>2016/05/11</date>
     * </modify>
     * </history>
     */
    @Override
    public String getStan() {
// 2022-11-23 Richard modified
// Stan採用新的取法
//        SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
//        Stan stan = spCaller.getStan();
//        return StringUtils.leftPad(String.valueOf(stan.getStan()), 7, '0');
        StanGenerator stanGenerator = SpringBeanFactoryUtil.getBean(StanGenerator.class);
        return stanGenerator.generate();
    }

    public RCV_VA_GeneralTrans_RQ getVaReq() {
        return vaReq;
    }

    public void setVaReq(RCV_VA_GeneralTrans_RQ vaReq) {
        this.vaReq = vaReq;
    }

    public RCV_NB_GeneralTrans_RQ getNbReq() {
        return nbReq;
    }

    public void setNbReq(RCV_NB_GeneralTrans_RQ nbReq) {
        this.nbReq = nbReq;
    }

    public RCV_HCE_GeneralTrans_RQ getHceReq() {
        return hceReq;
    }

    public void setHceReq(RCV_HCE_GeneralTrans_RQ hceReq) {
        this.hceReq = hceReq;
    }

}
