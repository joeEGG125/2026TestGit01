package com.syscom.fep.server.common.business;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.*;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.StanGenerator;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.Send2160Handler;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.NPSCUSTFeeName;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BusinessBase extends FEPBase {
    private Feptxn moriginalFEPTxn; // Response or Confirm時用來存放原始交易的FEPTxn記錄
    protected Feptxn feptxn; // 交易本筆的FEPTxn
    protected Feptxntcb feptxntcb; //TCB report 保留資料
    private FISCData mFISCData;
    private ATMData atmTxData;
    private Inbk2160 inbk2160;
    private RMData mRMData;
    @SuppressWarnings("unused")
    private T24Data t24Data;
    private Atmmstr mdefATMMSTR;
    private Card card;
    private Paytype paytype; // add by henny 20100902
    private String mCardTFRFlag;
    protected FeptxnDao feptxnDao;
    // FISC RC 正常
    public static final String FISCRCNormal = "4001";
    // 國際簽帳金融卡購物RRN-三萬元移轉
    public static final String RRN30000Trans = "188888889999";
    public static final String PAYTYPE30000Trans = "59999";
    protected MessageBase mGeneralData;
    // 虛擬櫃員機代號
    public static final String ATMNO_VIR = "90000";
    protected static final String rspCorrectStr = "4001";
    // 交易幣別
    public static final String CURTWD = "TWD";
    private NpscustMapper npscustMapper = SpringBeanFactoryUtil.getBean(NpscustMapper.class);
    public static final String INBKMessageType = "0200,0210,0202,0212,0203,0500,0502,0510,0581,0600,0602,0610,0699,0800,0802,0810";
    private Inbkparm inbkparm;
    protected Intltxn mINTLTXN; // add by Kyo 20100929
    private InbkparmExtMapper inbkparmExtMapper = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
    private CardtxnMapper cardtxnMapper = SpringBeanFactoryUtil.getBean(CardtxnMapper.class);
    private BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
    private SysstatMapper sysstatMapper = SpringBeanFactoryUtil.getBean(SysstatMapper.class);
    private ZoneMapper zoneMapper = SpringBeanFactoryUtil.getBean(ZoneMapper.class);
    private ListrateMapper listrateMapper = SpringBeanFactoryUtil.getBean(ListrateMapper.class);
    private CardExtMapper cardExtMapper = SpringBeanFactoryUtil.getBean(CardExtMapper.class);
    private SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
    private NwdregMapper nwdregMapper = SpringBeanFactoryUtil.getBean(NwdregMapper.class);
    private CurcdExtMapper curcdExtMapper = SpringBeanFactoryUtil.getBean(CurcdExtMapper.class);
    public static final int unisysTotaLengthError = 23;
    protected static final String unisysSecno = "12"; // 送主機用
    protected static final String unisysTrmType = "2";
    protected static final String unisysTlrNo = "97";
    public static final String unisysErrorRspMsgId1 = "E092";
    public static final String unisysErrorRspMsgId2 = "E063";
    public static final String unisysErrorRspMsgId3 = "E081";
    public static final String unisysRspErrorFlag = "E";
    private AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
    private NpsunitMapper npsunitMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    private HCEData mHCEtxData; // HCE資料物件

    private IVRData mIVRtxData; // IVR資料物件
    private NBData mNBtxData;

    private NotifyHelper notifyHelper = SpringBeanFactoryUtil.getBean(NotifyHelper.class);  //2024-05-09 新增Notify

    public MFTData getMftData() {
        return mftData;
    }

    public void setMftData(MFTData mftData) {
        this.mftData = mftData;
    }

    private MFTData mftData;
    private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private Inbk2160Mapper inbk2160Mapper = SpringBeanFactoryUtil.getBean(Inbk2160Mapper.class);
    private ATMData mTxData; // ATMP AA資料物件
    private RCV_HCE_GeneralTrans_RQ mHCEReq;
    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    public BusinessBase() {
        super();
    }

    public BusinessBase(MessageBase txData) {
        mGeneralData = txData;
        setFeptxn(mGeneralData.getFeptxn());
        setFeptxntcb(mGeneralData.getFeptxntcb());
        setFeptxnDao(mGeneralData.getFeptxnDao());
        setLogContext(mGeneralData.getLogContext());
        setEj(mGeneralData.getEj());
        setCard(mGeneralData.getCard()); // add by kyo for hostbusiness 共同存取 Card的資料
        setmCardTFRFlag(mGeneralData.getTfrFlag());
    }

    /**
     * 與交易關聯的FEPTxn物件
     *
     * <value></value>
     *
     * @return
     */
    public Feptxn getFeptxn() {
        return feptxn;
    }

    public void setFeptxn(Feptxn value) {
        this.feptxn = value;
    }

    public Inbk2160 getInbk2160() {
        return inbk2160;
    }

    public void setInbk2160(Inbk2160 value) {
        this.inbk2160 = value;
    }

    public Atmmstr getATMMSTR() { // add by henny 20100804{
        return mdefATMMSTR;
    }

    public void setATMMSTR(Atmmstr value) {
        mdefATMMSTR = value;
    }

    public Paytype getPaytype() {
        return paytype;
    }

    public void setPaytype(Paytype paytype) {
        this.paytype = paytype;
    }

    public Feptxntcb getFeptxntcb() {
        return feptxntcb;
    }

    public void setFeptxntcb(Feptxntcb feptxntcb) {
        this.feptxntcb = feptxntcb;
    }

    /**
     * 與交易關聯的原始交易FEPTxn物件
     *
     * <value></value>
     *
     * @return
     */
    public final Feptxn getOriginalFEPTxn() {
        return moriginalFEPTxn;
    }

    public final void setOriginalFEPTxn(Feptxn value) {
        moriginalFEPTxn = value;
    }

    public Intltxn getmINTLTXN() {
        return mINTLTXN;
    }

    public void setmINTLTXN(Intltxn mINTLTXN) {
        this.mINTLTXN = mINTLTXN;
    }

    /**
     * 檢核TITA的Error CODE是否為NORMAL
     *
     * @return
     */
    public boolean CheckEXPCD(String expcd) {
        if (StringUtils.leftPad(expcd, 4, "0").equals("0000")) {
            return true;
        }

        return false;
    }

    /**
     * 更新交易記錄相關資料(在AA結束前處理)
     *
     * @return
     */
    public FEPReturnCode updateTxData() {
        try {
            if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) > 0) {
                return FEPReturnCode.Normal;
            } else {
                return FEPReturnCode.FEPTXNUpdateError;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".UpdateTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.FEPTXNUpdateError;
        }
    }

    /**
     * 傳入幣別(BSP_CODE)取得該筆之幣別檔資料
     *
     * @param bspCode 00,01....
     * @return DefCURCD
     */
    public final Curcd getCurrencyByBSP(String bspCode) {
        try {
            List<Curcd> currency = FEPCache.getCurcdList();
            return currency.stream().filter(Curcd -> Curcd.getCurcdCurBsp().equals(bspCode)).findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".getCurrencyByBSP");
            sendEMS(getLogContext());
            return null;
        }
    }

    /**
     * 傳入ZONECODE取得ZONE檔資料
     * <p>
     *
     * @param zoneCode TWN,HKG,MAC
     * @return DefZONE
     */
    public Zone getZoneByZoneCode(String zoneCode) {
        try {
            return FEPCache.getZone(zoneCode);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".GetZoneByZoneCode");
            sendEMS(getLogContext());
        }
        return null;
    }

    public FEPReturnCode insertINBK2160p() {
        try {
            int insertINBK2160 = inbk2160Mapper.insertSelective(getInbk2160());
            if (insertINBK2160 <= 0) {
                return FEPReturnCode.FEPTXNInsertError;
            } else {
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".AddTXData");
            sendEMS(getLogContext());
        }
        return FEPReturnCode.Normal;
    }

    public final FISCData getFISCTxData() {
        return mFISCData;
    }

    public final void setFISCTxData(FISCData value) {
        mFISCData = value;
    }

    public RMData getmRMData() {
        return mRMData;
    }

    public void setmRMData(RMData mRMData) {
        this.mRMData = mRMData;
    }

    public final FEPReturnCode checkPAYTYPE() {
        PaytypeMapper paytypeMapper = SpringBeanFactoryUtil.getBean(PaytypeMapper.class);

        setPaytype(new Paytype());
        getPaytype().setPaytypeNo(getFeptxn().getFeptxnPaytype()); //繳款類別
        Paytype paytype = paytypeMapper.selectByPrimaryKey(getPaytype().getPaytypeNo());

        //以繳款類別, 讀取 PAYTYPE
        if (paytype == null) {
            //如查詢不到繳款類別，存褶類別欄位放空白, 仍可執行後續交易
            getPaytype().setPaytypeAliasname(""); // 稅務簡稱
            getPaytype().setPaytypeFullname(""); // 稅務全名
        } else {
            setPaytype(paytype);
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 新增交易記錄相關資料(在AA一開始時處理)
     *
     * @return <history>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>Function Modify</reason>
     * <date>2009/11/27</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode addTXData() {
        try {
            if (feptxnDao.insertSelective(getFeptxn()) <= 0) {
                return FEPReturnCode.FEPTXNInsertError;
            } else {
                return FEPReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".AddTXData");
            sendEMS(getLogContext());
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 將FEPTXN資料寫入NWDTXN
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>add for 無卡提款</reason>
     * <date>2017/01/25</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareNWDTXN(RefBase<Nwdtxn> nwdTxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            nwdTxn.get().setNwdtxnTxDate(getFeptxn().getFeptxnTxDate());
            nwdTxn.get().setNwdtxnEjfno(getFeptxn().getFeptxnEjfno());
            nwdTxn.get().setNwdtxnBkno(getFeptxn().getFeptxnBkno());
            nwdTxn.get().setNwdtxnStan(getFeptxn().getFeptxnStan());
            nwdTxn.get().setNwdtxnPcode(getFeptxn().getFeptxnPcode());
            nwdTxn.get().setNwdtxnAtmno(getFeptxn().getFeptxnAtmno());
            nwdTxn.get().setNwdtxnTxTime(getFeptxn().getFeptxnTxTime());
            nwdTxn.get().setNwdtxnFiscFlag(getFeptxn().getFeptxnFiscFlag());
            nwdTxn.get().setNwdtxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            nwdTxn.get().setNwdtxnTbsdy(getFeptxn().getFeptxnTbsdy());
            nwdTxn.get().setNwdtxnTxCur(getFeptxn().getFeptxnTxCur());
            nwdTxn.get().setNwdtxnNwdseq(getFeptxn().getFeptxnMajorActno());
            nwdTxn.get().setNwdtxnReqRc(getFeptxn().getFeptxnReqRc());
            nwdTxn.get().setNwdtxnRepRc(getFeptxn().getFeptxnRepRc());
            nwdTxn.get().setNwdtxnConRc(getFeptxn().getFeptxnConRc());
            nwdTxn.get().setNwdtxnTxrust(getFeptxn().getFeptxnTxrust());
            nwdTxn.get().setNwdtxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            nwdTxn.get().setNwdtxnTroutActno(getFeptxn().getFeptxnTroutActno());
            nwdTxn.get().setNwdtxnTroutKind(getFeptxn().getFeptxnTroutKind());
            nwdTxn.get().setNwdtxnBrno(getFeptxn().getFeptxnBrno());
            nwdTxn.get().setNwdtxnAccType(getFeptxn().getFeptxnAccType());
            nwdTxn.get().setNwdtxnZoneCode(getFeptxn().getFeptxnZoneCode());
            nwdTxn.get().setNwdtxnTxAmt(getFeptxn().getFeptxnTxAmt());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".PrepareNWDTXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 新增一筆資料
     *
     * @param //oFEPTXN FEPTXN物件
     * @return Int32, 大於0代表新增成功, 小於等於0代表新增失敗
     * <p>
     * 1. 只要Def物件屬性不給值該欄位就會不組在Update欄位清單中,所以如果該欄位不允許NULL就必須設定預設值,否則會有錯誤
     * 2. 以mDB.Transaction判斷是否由外部(AP)傳入Transaction物件, 若由外部傳入則不用處理Transaction, 若未傳入則再判斷若兩個Table都要處理就必須內部處理Transaction
     * 3. 若是INBK或ATMP子系統交易則必須同時新增FEPTXNDTL, 其他交易只要新增FEPTXNMSTR
     *
     * <modify>
     * <modifier>Anna Lin</modifier>
     * <reason>Code Analysis 錯誤修正</reason>
     * <date>2011/01/17</date>
     * </modify>
     */

    public final MessageBase getGeneralData() {
        return mGeneralData;
    }

    public final void setGeneralData(MessageBase value) {
        mGeneralData = value;
    }

    public Card getCard() {
        return this.card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getmCardTFRFlag() {
        return mCardTFRFlag;
    }

    public void setmCardTFRFlag(String mCardTFRFlag) {
        this.mCardTFRFlag = mCardTFRFlag;
    }

    public FeptxnDao getFeptxnDao() {
        return feptxnDao;
    }

    public void setFeptxnDao(FeptxnDao feptxnDao) {
        this.feptxnDao = feptxnDao;
    }

    /**
     * 身份證號轉碼
     *
     * @param idno 10位數字的身份證字號
     * @return 11位文數字
     * A->10,B->11, I->34, O-35>
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>NEW Function for 整批轉即時(2262)</reason>
     * <date>2014/04/21</date>
     * </modify>
     * </history>
     */
    public String mappingIDNO(String idno) {
        String mapIDNO = StringUtils.EMPTY;
        @SuppressWarnings("unused")
        String word1 = StringUtils.EMPTY, word2 = StringUtils.EMPTY;
        byte idno1 = (byte) idno.charAt(0);
        if (idno.trim().length() == 10 && idno1 >= 65 && idno1 <= 90 && PolyfillUtil.isNumeric(idno.substring(1, 2))) {
            // 本國人身份證號
            word1 = mappingWord(idno.substring(0, 1));
            mapIDNO = StringUtils.rightPad((word1 + idno.substring(1, 10)), 11, ' ');
        } else {
            // 統編或外國人身份證號
            mapIDNO = StringUtils.rightPad(idno.trim(), 11, ' ');
        }

        return mapIDNO;
    }

    /**
     * 1位A~Z英文轉2位0~9數字
     *
     * @param word 1位A~Z英文
     * @return 2位0~9數字
     * A->10,B->11, I->34, O-35>
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>NEW Function for 整批轉即時(2262)</reason>
     * <date>2014/04/21</date>
     * </modify>
     * </history>
     */
    public String mappingWord(String word) {
        if (word.trim().length() != 1) {
            return "  ";
        }
        byte word1 = (byte) word.charAt(0);
        switch (word) {
            case "A":
            case "B":
            case "C":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
                return String.valueOf(10 + (word1 - 65));
            case "I":
                return "34";
            case "J":
            case "K":
            case "L":
            case "M":
            case "N":
                return String.valueOf(18 + (word1 - 74));
            case "O":
                return "35";
            case "P":
            case "Q":
            case "R":
            case "S":
            case "T":
            case "U":
            case "V":
                return String.valueOf(23 + (word1 - 80));
            case "W":
                return "32";
            case "X":
                return "30";
            case "Y":
                return "31";
            case "Z":
                return "33";
            default:
                return "  ";
        }
    }

    /**
     * 傳入ISO幣別碼(CURCD_ISONO3)取得該筆之ISO幣別文字碼
     *
     * @param isono3
     * @return DefCURCD
     *
     * <history>
     * <modify>
     * <modifier>henny</modifier>
     * <reason>
     * 傳入ISO幣別碼(CURCD_ISONO3)取得該筆之ISO幣別文字碼
     * </reason>
     * <date>2010/4/1</date>
     * </modify>
     * </history>
     */
    public Curcd getAlpha3ByIsono3(String isono3) {
        try {
            List<Curcd> currency = FEPCache.getCurcdList();
            return currency.stream().filter(t -> t.getCurcdIsono3() != null && t.getCurcdIsono3().equals(isono3)).findFirst().orElse(null);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".getAlpha3ByIsono3");
            sendEMS(getLogContext());
            return null;
        }
    }

    /**
     * 檢核永豐繳費網委託單位代號
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>New for 繳費網PHASEI</reason>
     * <date>2016/01/04</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>移至BusinessBase</reason>
     * <date>2018/06/27</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkBPUNIT() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Npscust defNPSCUST = new Npscust();

        try {
            defNPSCUST.setNpscustUnitno(getFeptxn().getFeptxnBusinessUnit());
            defNPSCUST.setNpscustPaytype(getFeptxn().getFeptxnPaytype());
            defNPSCUST.setNpscustFeeno(getFeptxn().getFeptxnPayno());
            defNPSCUST = npscustMapper.selectByPrimaryKey(defNPSCUST.getNpscustUnitno(), defNPSCUST.getNpscustPaytype(), defNPSCUST.getNpscustFeeno());
            if (defNPSCUST == null) {
                getLogContext().setRemark("CheckBPUNIT-找不到NPSCUST資料, NPSCUST_UNITNO=" + getFeptxn().getFeptxnBusinessUnit() + ", NPSCUST_PAYTYPE=" + getFeptxn().getFeptxnPaytype() + ", NPSCUST_FEENO="
                        + getFeptxn().getFeptxnPayno());
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.NPSNotFound;
                return rtnCode;
            }

            if (FEPChannel.NETBANK.name().equals(getFeptxn().getFeptxnChannel())) {
                // 檢核ID+Acct及繳費網之繳費種類
                if (!NPSCUSTFeeName.IDACCT.equals(defNPSCUST.getNpscustFeeName()) || !defNPSCUST.getNpscustPbtype().equals(getFeptxn().getFeptxnPbtype())) {
                    getLogContext().setRemark("CheckBPUNIT-檢核NPSCUST_FEE_NAME<>'ID+Acct'或繳費網之繳費種類不一致, NPSCUST_FEE_NAME=" + defNPSCUST.getNpscustFeeName() + ", NPSCUST_PBTYPE="
                            + defNPSCUST.getNpscustPbtype() + ", FEPTXN_PBTYPE=" + getFeptxn().getFeptxnPbtype());
                    logMessage(Level.INFO, getLogContext());
                    rtnCode = FISCReturnCode.NPSNotFound;
                    return rtnCode;
                }
            } else if (FEPChannel.EAT.name().equals(getFeptxn().getFeptxnChannel())) {
                // 檢核WebATM+晶片卡
                if (!defNPSCUST.getNpscustFeeName().equals(NPSCUSTFeeName.WEBATMIC)) {
                    getLogContext().setRemark("CheckBPUNIT-檢核NPSCUST_FEE_NAME<>'WebATM+晶片卡', NPSCUST_FEE_NAME=" + defNPSCUST.getNpscustFeeName());
                    logMessage(Level.INFO, getLogContext());
                    rtnCode = FISCReturnCode.NPSNotFound;
                    return rtnCode;
                }

                getFeptxn().setFeptxnPbtype(defNPSCUST.getNpscustPbtype());
            }

            // 2018/06/27 Modify by Ruling for 增加檢核績效部門及繳費網繳費種類
            if (StringUtils.isBlank(defNPSCUST.getNpscustDept())) {
                getLogContext().setRemark("CheckBPUNIT-續效部門(NPSCUST_DEPT)為空或NULL");
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.NPSNotFound;
                return rtnCode;
            }

            if (StringUtils.isBlank(defNPSCUST.getNpscustPbtype())) {
                getLogContext().setRemark("CheckBPUNIT-繳費網繳費種類(NPSCUST_PBTYPE)為空或NULL");
                logMessage(Level.INFO, getLogContext());
                rtnCode = FISCReturnCode.NPSNotFound;
                return rtnCode;
            }

            // 2018/07/16 Modify by Ruling for 增加檢核繳費種類為03信用卡費及05其他費用時，銷帳編號若為空白或NULL回給前端錯誤
            if ("03,05".indexOf(defNPSCUST.getNpscustPbtype()) > -1 && StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
                getLogContext().setRemark("CheckBPUNIT-繳費網繳費種類為03信用卡費及05其他費用時，銷帳編號不能為空或NULL");
                logMessage(Level.INFO, getLogContext());
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".CheckBPUNIT");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 本程式用來處理"換日"
     * 1、要更換之本營業日來源
     * 1.1、若有輸入_INFISCTBSDY 則以 _INFISCTBSDY 為本營業日(_FISCTBSDY)
     * 1.2、若未輸入_INFISCTBSDY 則以 系統日 為本營業日(_FISCTBSDY)
     * 2、由 日曆檔(BSDAYS) 取得 本營業日(_FISCTBSDY) 相關資料
     * 3、更新 系統狀態檔(SYSSTAT)
     * 4、更新 地區檔(ZONE)
     *
     * @return <history>
     * <modify>
     * <modifier>Matt</modifier>
     * <reason>Function Modify</reason>
     * <date>2009/12/2</date>
     * </modify>
     * <modify>
     * <modifier>Matt</modifier>
     * <reason>
     * BRSID1(001B0017):三點半後無法自動換日；三點半後無法自動換日，SYSSTAT, ATMSTAT,ZONE沒有更新！
     * </reason>
     * <date>2010/03/09</date>
     * </modify>
     * <modify>
     * <modifier>Matt</modifier>
     * <reason>根據不同地區做換日</reason>
     * <date>2010/04/09</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>
     * 1.變更db.commit位置到If判斷中
     * 2.bugreport(001B0216)換日BUG，defZONE檔未取得值就使用，且使用錯誤欄位(SysStatus.PropertyValue)搬移至defZONE檔，應該用defsysstat
     * </reason>
     * <date>2010/04/13</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>BugReport(001B0504):港澳在5/21(五)放假，所以5/20(四)換日時，ZONE_TBSDY變成20100524(一)，但在5/21換日時，ZONE_TBSDY又再換了一次變成20100525(二)</reason>
     * <date>2010/05/21</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>調整程式</reason>
     * <date>2010/06/01</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>修改查詢條件使用FEPConfig.BankID(807)</reason>
     * <date>2010/06/24</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for 補上遺漏的換日時間欄位</reason>
     * <date>2010/08/05</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for 換日記號要設定為TRUE(8/12 修改)</reason>
     * <date>2010/08/12</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
     * <date>2011/03/16</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode changeDate(String inFISCTBSDY) {
        @SuppressWarnings("unused")
        int weekNo = 0; // 星期
        String sysDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN); // 系統日期
        String fiscTBSDY = ""; // 今天

        // add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下
        boolean W_CHGDAY_TWN = false;
//        boolean W_CHGDAY_HKG = false;
//        boolean W_CHGDAY_MAC = false;
//        String ZoneTBSDYHKG = "";
//        String ZoneTBSDYMAC = "";

        Bsdays bsdays = new Bsdays();
        Zone zone = new Zone();
        try {
            // modified by Maxine on 2011/08/25 for SYSSTAT自行讀DB
            Sysstat sysstate = sysstatMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno());
            if (sysstate == null) {
                return IOReturnCode.SYSSTATNotFound;
            }

            // 是否有輸入的財金營業日
            if (StringUtils.isNotBlank(inFISCTBSDY)) {
                // 若有，但與SYSSTAT_TBSDY_FISC(財金本營業日)，則回傳Normal
                if (inFISCTBSDY.equals(sysstate.getSysstatTbsdyFisc())) {
                    return CommonReturnCode.Normal;
                } else if (sysstate.getSysstatTbsdyFisc().compareTo(inFISCTBSDY) < 0) {
                    fiscTBSDY = inFISCTBSDY; // 本營業日 為 輸入的財金營業日
                    // add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下
                    W_CHGDAY_TWN = true;
                }
            } else {
                if (sysstate.getSysstatTbsdyFisc().compareTo(sysDate) <= 0) {// 若本營業日 <= 系統日期
                    fiscTBSDY = sysstate.getSysstatNbsdyFisc(); // 本營業日 為 次營業日
                    // add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下
                    W_CHGDAY_TWN = true;
                }
            }

            // 若 欲變更的本營業日 不為空值
            if (StringUtils.isNotBlank(fiscTBSDY)) {
                bsdays.setBsdaysDate(fiscTBSDY);
                bsdays.setBsdaysZoneCode(ZoneCode.TWN); // 日曆檔-地區
                bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
                if (bsdays == null) {
                    // 2010-06-01 by kyo 調整程式
                    return IOReturnCode.BSDAYSNotFound;
                }
                weekNo = bsdays.getBsdaysWeekno();
                sysstate.setSysstatLbsdyFisc(sysstate.getSysstatTbsdyFisc()); // 前一營業日
                sysstate.setSysstatTbsdyFisc(fiscTBSDY); // 本營業日
                sysstate.setSysstatNbsdyFisc(bsdays.getBsdaysNbsdy()); // 次營業日
                // 更新SYSSTAT檔
                if (sysstatMapper.updateByPrimaryKeySelective(sysstate) <= 0) {
                    // 2010-06-01 by kyo 調整程式
                    return IOReturnCode.SYSSTATUpdateError;
                }

                // 更新所有的地區檔
                // BugReport(001B0017):ZONE的CHGDAY不要改回TRUE才會換日
                // 2010/04/02 Modify by Matt
                // bugreport(001B0216): 2010/04/13 Modify by Kyo for 換日BUG，defZONE檔未取得值就使用，且使用錯誤欄位(SysStatus.PropertyValue)搬移至defZONE檔，應該用sysstate
                zone.setZoneCode(ATMZone.TWN.name());
                zone = zoneMapper.selectByPrimaryKey(zone.getZoneCode());
                if (zone == null) {
                    // 2010-06-01 by kyo 調整程式
                    return IOReturnCode.ZONENotFound;
                }
                // 台灣
                // 2010-08-12 by kyo for 換日記號要設定為TRUE(8/12 修改)
                zone.setZoneChgday(DbHelper.toShort(true));
                zone.setZoneCbsMode((short) ATMCBSMode.HalfOnline.getValue());
                // 2012/07/18 Modify by Ruling for 還原換日時間欄位
                // 2010-08-05 by kyo for 補上遺漏的換日時間欄位
                zone.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
                if (zoneMapper.updateByPrimaryKeySelective(zone) != 1) {
                    // 2010-06-01 by kyo 調整程式
                    return IOReturnCode.ZONEUpdateError;
                }
                // 2010-11-02 by Ed for 修改換日出錯，改為Spec上的寫法
            }

//            // BugReport(001B0504):2010-05-21 modified by kyo for 遺漏以系統日讀取日曆檔邏輯
//            bsdays.setBsdaysZoneCode(ATMZone.HKG.name());
//            bsdays.setBsdaysDate(sysDate);
//            bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
//            if (bsdays == null) {
//                // 2010-06-01 by kyo 調整程式
//                return IOReturnCode.BSDAYSNotFound;
//            }

            // 香港
            // 讀Zone檔
//            zone.setZoneCode(ATMZone.HKG.name());
//            zone = zoneMapper.selectByPrimaryKey(zone.getZoneCode());
//            if (zone == null) {
//                // 2010-06-01 by kyo 調整程式
//                return IOReturnCode.ZONENotFound;
//            }

            // 2013/06/19 Modify by Ruling for 港澳NCB:移至T24COB時才換日
            // BugReport(001B0504):2010-05-21 modified by kyo for 以ZONE檔次營業日讀取日曆檔邏輯搬到判斷條件內
//            if (zone.getZoneTbsdy().compareTo(bsdays.getBsdaysNbsdy()) < 0) {
//// 2024-03-06 Richard modified for SYSSTATE 調整
////                if ("U".equals(sysstate.getSysstatCbsHkg())) {
////                    // 讀次日日曆檔
////                    bsdays.setBsdaysZoneCode(ATMZone.HKG.name());
////                    bsdays.setBsdaysDate(zone.getZoneNbsdy());
////                    bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
////                    if (bsdays == null) {
////                        // 2010-06-01 by kyo 調整程式
////                        return IOReturnCode.BSDAYSNotFound;
////                    }
////                    // 2010-08-12 by kyo for 換日記號要設定為TRUE(8/12 修改)
////                    zone.setZoneChgday(DbHelper.toShort(true));
////                    zone.setZoneCode(ATMZone.HKG.name());
////                    zone.setZoneCbsMode((short) ATMCBSMode.HalfOnline.getValue());
////                    zone.setZoneLlbsdy(zone.getZoneLbsdy());
////                    zone.setZoneLbsdy(zone.getZoneTbsdy());
////                    zone.setZoneTbsdy(zone.getZoneNbsdy());
////                    zone.setZoneNbsdy(bsdays.getBsdaysNbsdy());
////                    zone.setZoneWeekno(bsdays.getBsdaysWeekno());
////                    // 2010-08-05 by kyo for 補上遺漏的換日時間欄位
////                    zone.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
////                    /// *add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下*/
////                    W_CHGDAY_HKG = true;
////                    ZoneTBSDYHKG = zone.getZoneTbsdy();
////                } else {
//                    zone.setZoneChgday(DbHelper.toShort(true));
//                    zone.setZoneCbsMode((short) ATMCBSMode.HalfOnline.getValue());
//                    // 2014/10/01 Modify by Ruling for 加文字檔log
//                    getLogContext().setRemark("ChangeDate-香港ZONE_CBS_MODE改為2");
//                    logMessage(Level.INFO, getLogContext());
////                }
//
//                if (zoneMapper.updateByPrimaryKeySelective(zone) != 1) {
//                    // 2010-06-01 by kyo 調整程式
//                    return IOReturnCode.ZONEUpdateError;
//                }
//            }
//
//            // BugReport(001B0504):2010-05-21 modified by kyo for 遺漏以系統日讀取日曆檔邏輯
//            bsdays.setBsdaysZoneCode(ATMZone.MAC.name());
//            bsdays.setBsdaysDate(sysDate);
//            bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
//            if (bsdays == null) {
//                // 2010-06-01 by kyo 調整程式
//                return IOReturnCode.BSDAYSNotFound;
//            }

            // 澳門
            // 讀Zone檔
//            zone.setZoneCode(ATMZone.MAC.name());
//            zone = zoneMapper.selectByPrimaryKey(zone.getZoneCode());
//            if (zone == null) {
//                // 2010-06-01 by kyo 調整程式
//                return IOReturnCode.ZONENotFound;
//            }

            // 2013/06/19 Modify by Ruling for 港澳NCB:移至T24COB時才換日
            // BugReport(001B0504):2010-05-21 modified by kyo for 以ZONE檔次營業日讀取日曆檔邏輯搬到判斷條件內
//            if (zone.getZoneTbsdy().compareTo(bsdays.getBsdaysNbsdy()) < 0) {
//// 2024-03-06 Richard modified for SYSSTATE 調整
////                if ("U".equals(sysstate.getSysstatCbsMac())) {
////                    // 讀次日日曆檔
////                    bsdays.setBsdaysZoneCode(ATMZone.MAC.name());
////                    bsdays.setBsdaysDate(zone.getZoneNbsdy());
////                    bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
////                    if (bsdays == null) {
////                        // 2010-06-01 by kyo 調整程式
////                        return IOReturnCode.BSDAYSNotFound;
////                    }
////                    // 2010-08-12 by kyo for 換日記號要設定為TRUE(8/12 修改)
////                    zone.setZoneChgday(DbHelper.toShort(true));
////                    zone.setZoneCode(ATMZone.MAC.name());
////                    zone.setZoneCbsMode((short) ATMCBSMode.HalfOnline.getValue());
////                    zone.setZoneLlbsdy(zone.getZoneLbsdy());
////                    zone.setZoneLbsdy(zone.getZoneTbsdy());
////                    zone.setZoneTbsdy(zone.getZoneNbsdy());
////                    zone.setZoneNbsdy(bsdays.getBsdaysNbsdy());
////                    zone.setZoneWeekno(bsdays.getBsdaysWeekno());
////                    // 2010-08-05 by kyo for 補上遺漏的換日時間欄位
////                    zone.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
////                    /// *add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下*/
////                    W_CHGDAY_MAC = true;
////                    ZoneTBSDYMAC = zone.getZoneTbsdy();
////                } else {
//                    zone.setZoneChgday(DbHelper.toShort(true));
//                    zone.setZoneCbsMode((short) ATMCBSMode.HalfOnline.getValue());
//                    // 2014/10/01 Modify by Ruling for 加文字檔log
//                    getLogContext().setRemark("ChangeDate-澳門ZONE_CBS_MODE改為2");
//                    logMessage(Level.INFO, getLogContext());
////                }
//
//                if (zoneMapper.updateByPrimaryKeySelective(zone) != 1) {
//                    // 2010-06-01 by kyo 調整程式
//                    return IOReturnCode.ZONEUpdateError;
//                }
//            }
            // 2010-11-02 by Ed for 修改換日出錯，改為Spec上的寫法
            // End If

            // 2011-06-01 by kyo for 在可能換日後的位置 強制reload Cache資料
            FEPCache.reloadCache(CacheItem.SYSSTAT);
            FEPCache.reloadCache(CacheItem.ZONE);

            /// *add by maxine on 2011/07/15 for 20110621 需顯示換日訊息於EMS, 為此LogData新增欄位如下*/
            FEPReturnCode InfoRC = CommonReturnCode.Normal;

            getLogContext().setRemark("W_CHGDAY_TWN=" + W_CHGDAY_TWN + ";");
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.INFO, getLogContext());
            if (W_CHGDAY_TWN) {
                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
                getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
                getLogContext().setMessageGroup("1"); // OPC

                InfoRC = CommonReturnCode.FISCBusinessDateChangeToYMD;
                getLogContext().setMessageParm13(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
                getLogContext().setProgramName(ProgramName);
                logMessage(getLogContext());
            }

            //2026/01/16 換日完成，檢核下一營業日是否為工作日
            sysstate = sysstatMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno());
            bsdays.setBsdaysDate(sysstate.getSysstatNbsdyFisc());
            bsdays.setBsdaysZoneCode(ZoneCode.TWN); // 日曆檔-地區
            bsdays = bsdaysMapper.selectByPrimaryKey(bsdays.getBsdaysZoneCode(), bsdays.getBsdaysDate());
            if (bsdays == null) {
                getLogContext().setRemark(sysstate.getSysstatNbsdyFisc()+" BSDAYS NOT FOUND");
                getLogContext().setProgramName(ProgramName);
                sendEMS(getLogContext());
            }else if (bsdays.getBsdaysWorkday() != 1 ) {//非工作日
                getLogContext().setRemark(sysstate.getSysstatNbsdyFisc()+" 非工作日");
                getLogContext().setProgramName(ProgramName);
                sendEMS(getLogContext());
            }

//            getLogContext().setRemark("W_CHGDAY_HKG=" + W_CHGDAY_HKG + ";");
//            getLogContext().setProgramName(ProgramName);
//            logMessage(Level.INFO, getLogContext());
//            if (W_CHGDAY_HKG) {
//                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
//                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
//                getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
//                getLogContext().setMessageGroup("1"); // OPC
//
//                InfoRC = CommonReturnCode.CBSBusinessDateChangeToYMD;
//                getLogContext().setMessageParm13("香港");
//                getLogContext().setMessageParm14(ZoneTBSDYHKG);
//                getLogContext().setProgramName(ProgramName);
//                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
//                getLogContext().setProgramName(ProgramName);
//                logMessage(getLogContext());
//            }
//
//            getLogContext().setRemark("W_CHGDAY_MAC=" + W_CHGDAY_MAC + ";");
//            getLogContext().setProgramName(ProgramName);
//            logMessage(Level.INFO, getLogContext());
//            if (W_CHGDAY_MAC) {
//                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
//                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
//                getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
//                getLogContext().setMessageGroup("1"); // OPC
//
//                InfoRC = CommonReturnCode.CBSBusinessDateChangeToYMD;
//                getLogContext().setMessageParm13("澳門");
//                getLogContext().setMessageParm14(ZoneTBSDYMAC);
//                getLogContext().setProgramName(ProgramName);
//                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
//                getLogContext().setProgramName(ProgramName);
//                logMessage(getLogContext());
//            }

            // 2.同步更新外圍系統EAINET(db)
            // 2012/05/22 Modify by Ruling for 更新外圍系統 EAINET DB 顯示於 EMS
            // 2011/6/8 Update By Connie
            /*
             * if (W_CHGDAY_TWN) {
             * StringBuilder mSQL = new StringBuilder();
             * DBHelper mDB = new DBHelper(FEPConfig.DBName);
             *
             * AbstractList<DbParameter> pamTemp = new AbstractList<DbParameter>();
             * mSQL.append("UPDATE LKSRV_EAINETFEP.EAINET.dbo.TBDAY SET ");
             *
             * mSQL.append("FISC_TBSDY = @FISC_TBSDY,");
             * pamTemp.add(mDB.CreateParameter("FISC_TBSDY", DbType.AnsiStringFixedLength, 8, ParameterDirection.Input, SysStatus.PropertyValue.SYSSTAT_TBSDY_FISC));
             * mSQL.append("FISC_NBSDY = @FISC_NBSDY,");
             * pamTemp.add(mDB.CreateParameter("FISC_NBSDY", DbType.AnsiStringFixedLength, 8, ParameterDirection.Input, SysStatus.PropertyValue.SYSSTAT_NBSDY_FISC));
             * mSQL.append("FISC_LBSDY = @FISC_LBSDY,");
             * pamTemp.add(mDB.CreateParameter("FISC_LBSDY", DbType.AnsiStringFixedLength, 8, ParameterDirection.Input, SysStatus.PropertyValue.SYSSTAT_LBSDY_FISC));
             * mSQL.append("FISC_MODE = @FISC_MODE,");
             * //modified by Maxine on 2012/03/02 for 財金換日時, 同步更新外圍系統EAINET DB, FISC_MODE 改成 2 (ATMCBSMode.HalfOnline)
             * pamTemp.add(mDB.CreateParameter("FISC_MODE", DbType.AnsiStringFixedLength, 1, ParameterDirection.Input, ATMCBSMode.HalfOnline.getValue()));
             * //pamTemp.Add(mDB.CreateParameter("FISC_MODE", DbType.AnsiStringFixedLength, 1, ParameterDirection.Input, defZONE.ZONE_CBS_MODE))
             * mSQL.append("FISC_UpdTime = GetDate()");
             *
             * //add By Maxine on 2012/03/02 for 增加Log紀錄執行SQL
             * String strLogSQL;
             * strLogSQL = mSQL.toString();
             * strLogSQL = strLogSQL.replace("@FISC_TBSDY", SysStatus.getPropertyValue().getSysstatTbsdyFisc());
             * strLogSQL = strLogSQL.replace("@FISC_NBSDY", SysStatus.getPropertyValue().getSysstatNbsdyFisc());
             * strLogSQL = strLogSQL.replace("@FISC_LBSDY", SysStatus.getPropertyValue().getSysstatLbsdyFisc());
             * strLogSQL = strLogSQL.replace("@FISC_MODE", String.valueOf(ATMCBSMode.HalfOnline.getValue()));
             *
             * try {
             * if (mDB.ExecuteNonQuery(mSQL.toString(), CommandType.Text, pamTemp) == 0) {
             * getLogContext().setRemark("Update LKSRV_EAINETFEP.EAINET.dbo.TBDAY 筆數為0，更新失敗  ");
             * //add By Maxine on 2012/03/02 for 增加Log紀錄執行SQL
             * getLogContext().setRemark(getLogContext().getRemark() + strLogSQL);
             * getLogContext().setProgramName(ProgramName);
             * sendEMS(getLogContext());
             * } else {
             * getLogContext().setRemark("Update LKSRV_EAINETFEP.EAINET.dbo.TBDAY 更新成功  ");
             * //add By Maxine on 2012/03/02 for 增加Log紀錄執行SQL
             * getLogContext().setRemark(getLogContext().getRemark() + strLogSQL);
             * getLogContext().setProgramName(ProgramName);
             * logMessage(Level.INFO, getLogContext());
             * //SendEMS
             * getLogContext().setpCode(getFeptxn().getFeptxnPcode());
             * getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
             * getLogContext().setFiscRC(getFeptxn().getFeptxnReqRc());
             * getLogContext().setMessageGroup("1"); //OPC
             * getLogContext().setMessageParm13(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
             * InfoRC = CommonReturnCode.EAINETBusinessDateChangeToYMD;
             * getLogContext().setRemark( TxHelper.GetMessageFromFEPReturnCode(InfoRC, getLogContext()));
             * }
             * } catch (Exception ex) {
             * //2010-08-11 by kyo for 明祥通知若有修改程式GetRCFromErrorCoe要使用4個參數的版本
             * //add By Maxine on 2012/03/02 for 增加Log紀錄執行SQL
             * getLogContext().setRemark(strLogSQL);
             * getLogContext().setProgramException(ex);
             * getLogContext().setProgramName(ProgramName + "." );//+ MethodBase.GetCurrentMethod().Name
             * sendEMS(getLogContext());
             * } finally {
             * pamTemp = null;
             * mSQL = null;
             * }
             * }
             */
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".changeDate"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkCompany(String branch) {
        FEPReturnCode rtn = FEPReturnCode.Normal;
        if (branch.length() != 3) {
            getLogContext().setRemark("branch長度有誤 branch:[" + branch + "]");
            logMessage(Level.INFO, getLogContext());
            rtn = FEPReturnCode.OtherCheckError;
            return rtn;
        }
        CompanyExtMapper companyExtMapper = SpringBeanFactoryUtil.getBean(CompanyExtMapper.class);
        List<Company> companies = companyExtMapper.getCompanyByBranch(branch);
        if (companies.size() <= 0) {
            getLogContext().setRemark(branch + "分行不存在");
            logMessage(Level.INFO, getLogContext());
            rtn = FEPReturnCode.OtherCheckError;
            return rtn;
        } else {
            if (StringUtils.isNotBlank(companies.get(0).getRseffdate())) {
                if (Integer.parseInt(getFeptxn().getFeptxnTxDate()) >= Integer.parseInt(companies.get(0).getRseffdate())) {
                    getLogContext().setRemark(branch + "分行已撤銷,轉換生效日[" + companies.get(0).getRseffdate() + "]");
                    logMessage(Level.INFO, getLogContext());
                    rtn = FEPReturnCode.OtherCheckError;
                    return rtn;
                }
            }
        }

        return rtn;
    }

    /**
     * 取得虛擬帳號之前置碼
     *
     * @param accNo
     * @return
     */
    public String getVirtualAccountPrefix(String accNo) {
        if (accNo.length() < 3) {
            throw new IllegalArgumentException("accNo參數長度至少須為3碼");
        }
        if (accNo.substring(0, 3).equals("287")) {
            return "AIG";
        } else {
            switch (accNo.substring(0, 1)) {
                case "7":
                    return "B";
                case "8":
                    return "C";
                case "9":
                    return "D";
                default:
                    return "";
            }
        }

    }

    public Inbkparm getInbkparm() {
        return inbkparm;
    }

    public void setInbkparm(Inbkparm value) {
        inbkparm = value;
    }

    public FEPReturnCode getInbkparmByPK() throws Exception {
        String acqFlag = null;
        String txCur = StringUtils.EMPTY;
        String wkAPID = StringUtils.EMPTY;
        this.inbkparm = new Inbkparm();
        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno())) {
            acqFlag = "A"; // 代理
        } else {
            if (!"4".equals(this.feptxn.getFeptxnPcode().substring(3, 4))
                    || SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTrinBkno())) { // 轉入交易
                acqFlag = "I"; // 被代理
            } else { // 跨行轉帳-轉出交易
                acqFlag = "O"; // 被代理-跨行轉出
            }
        }
        if (StringUtils.isBlank(this.feptxn.getFeptxnZoneCode())) {
            txCur = CurrencyType.TWD.name();
        } else {
            switch (this.feptxn.getFeptxnZoneCode()) {
                case ZoneCode.TWN:
                    txCur = CurrencyType.TWD.name();
                    break;
                case ZoneCode.HKG:
                    txCur = CurrencyType.HKD.name();
                    break;
                case ZoneCode.MAC:
                    txCur = CurrencyType.MOP.name();
                    break;
            }
        }

        // 因應國際卡餘額查詢作業, INBKPARM 新增 PCODE 欄位, 除了國際卡交易以外之 INBKPARM_PCODE 皆為空值
        String wkPCODE = StringUtils.EMPTY;

        // 判斷 PCODE 以得到 APID
        if (!"24".equals(this.feptxn.getFeptxnPcode().substring(0, 2))
                && !"26".equals(this.feptxn.getFeptxnPcode().substring(0, 2))) { // 非國際卡
            wkAPID = this.feptxn.getFeptxnPcode();
            wkPCODE = StringUtils.repeat(StringUtils.SPACE, 4); // 若PCODE = APID, 則 PCODE不需給值
        } else {
            // spec change 10/05/29
            // spec change 2010-10-05 modify by Husan 清算幣別欄位修正
            if (FISCPCode.PCode2410.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2430.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2411.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2620.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2621.getValueStr().equals(this.feptxn.getFeptxnPcode())) { // PLUS 國際提款交易&餘額查詢
                wkAPID = "2422";
            } else if (FISCPCode.PCode2420.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2622.getValueStr().equals(this.feptxn.getFeptxnPcode())) { // VISA 預借現金交易
                if ("TWD".equals(this.feptxn.getFeptxnTxCurSet())) { // 清算幣別=台幣
                    wkAPID = "2421";
                } else {
                    wkAPID = "2422";
                }
            } else if (FISCPCode.PCode2450.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2470.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2451.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2630.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2633.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2631.getValueStr().equals(this.feptxn.getFeptxnPcode())) { // CIRRUS 國際提款&餘額查詢交易
                wkAPID = "2432";
            } else if (FISCPCode.PCode2460.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2632.getValueStr().equals(this.feptxn.getFeptxnPcode())) { // MASTER 預借現金交易
                if (CurrencyType.TWD.name().equals(this.feptxn.getFeptxnTxCurSet())) { // 清算幣別=台幣
                    wkAPID = "2431";
                } else {
                    wkAPID = "2432";
                }
            } else if (FISCPCode.PCode2480.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2640.getValueStr().equals(this.feptxn.getFeptxnPcode())) {// JCB 預借現金交易
                wkAPID = "2441";
            } else if (FISCPCode.PCode2400.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2401.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2600.getValueStr().equals(this.feptxn.getFeptxnPcode())
                    || FISCPCode.PCode2601.getValueStr().equals(this.feptxn.getFeptxnPcode())) { // CUP 國際提款/餘額查詢交易
                wkAPID = "6282";
            }
            wkPCODE = this.feptxn.getFeptxnPcode(); // 若PCODE <> APID, 則 PCODE需給值
        }
        this.feptxn.setFeptxnApid(wkAPID);
        // 讀取(INBKPARM)
        this.inbkparm.setInbkparmCur(txCur);
        this.inbkparm.setInbkparmApid(wkAPID);
        this.inbkparm.setInbkparmAcqFlag(acqFlag);
        this.inbkparm.setInbkparmEffectDate(this.feptxn.getFeptxnTbsdyFisc());
        this.inbkparm.setInbkparmPcode(wkPCODE);
        getLogContext().setRemark(
                StringUtils.join(
                        "CUR=", txCur,
                        " APID=", wkAPID,
                        " ACQ_FLAG=", acqFlag,
                        " EFFECT_DATE=", this.feptxn.getFeptxnTbsdyFisc(),
                        " PCODE=", wkPCODE));
        logMessage(Level.INFO, getLogContext());
        Inbkparm result = inbkparmExtMapper.queryByPK(this.inbkparm);
        if (result == null) {
            return FEPReturnCode.INBKPARMNotFound;
        }
        this.inbkparm = result;
        return FEPReturnCode.Normal;
    }

    /**
     * 傳入幣別(ALPHA3)取得該筆之幣別檔資料
     *
     * @param currencyAlpha3 TWD,USD....
     * @return DefCURCD
     */
    public Curcd getCurrencyByAlpha3(String currencyAlpha3) {
        try {
            List<Curcd> currency = FEPCache.getCurcdList();
            return currency.stream().filter(Curcd -> Curcd.getCurcdAlpha3().equals(currencyAlpha3)).findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".getCurrencyByAlpha3");
            sendEMS(getLogContext());
            return null;
        }
    }

    public String getCardTFRFlag() {
        return mCardTFRFlag;
    }

    public void setCardTFRFlag(String value) {
        mCardTFRFlag = value;
    }

    // Fly 2020/03/23 修改 for 轉帳繳納口罩費用免手續費
    public FEPReturnCode getInbkparmByMASK() throws Exception {
        String acqFlag = null;
        String txCur = StringUtils.EMPTY;
        this.inbkparm = new Inbkparm();
        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno())) {
            acqFlag = "A"; // 代理
        } else {
            if (!"4".equals(this.feptxn.getFeptxnPcode().substring(3, 4))
                    || SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTrinBkno())) { // 轉入交易
                acqFlag = "I"; // 被代理
            } else { // 跨行轉帳-轉出交易
                acqFlag = "O"; // 被代理-跨行轉出
            }
        }
        if (StringUtils.isBlank(this.feptxn.getFeptxnZoneCode())) {
            txCur = CurrencyType.TWD.name();
        } else {
            switch (this.feptxn.getFeptxnZoneCode()) {
                case ZoneCode.TWN:
                    txCur = CurrencyType.TWD.name();
                    break;
                case ZoneCode.HKG:
                    txCur = CurrencyType.HKD.name();
                    break;
                case ZoneCode.MAC:
                    txCur = CurrencyType.MOP.name();
                    break;
            }
        }

        String wkPCODE = this.feptxn.getFeptxnPcode();
        String wkAPID = "MASK";
        // 讀取(INBKPARM)
        this.inbkparm.setInbkparmCur(txCur);
        this.inbkparm.setInbkparmApid(wkAPID);
        this.inbkparm.setInbkparmAcqFlag(acqFlag);
        this.inbkparm.setInbkparmEffectDate(this.feptxn.getFeptxnTbsdyFisc());
        this.inbkparm.setInbkparmPcode(wkPCODE);
        getLogContext().setRemark(
                StringUtils.join(
                        "CUR=", txCur,
                        " APID=", wkAPID,
                        " ACQ_FLAG=", acqFlag,
                        " EFFECT_DATE=", this.feptxn.getFeptxnTbsdyFisc(),
                        " PCODE=", wkPCODE));
        logMessage(Level.INFO, getLogContext());
        Inbkparm result = inbkparmExtMapper.queryByPK(this.inbkparm);
        if (result == null) {
            return FEPReturnCode.INBKPARMNotFound;
        }
        this.inbkparm = result;
        return FEPReturnCode.Normal;
    }

    /**
     * 透過FEPTXN取得inbkparm
     *
     * @return Definbkparm物件
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>跨行轉帳小額交易手續費調降</reason>
     * <date>2019/02/19</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode getInbkparmByIFT() {
        String acqFlag = null;
        String txCur = StringUtils.EMPTY;
        String wkAPID = StringUtils.EMPTY;
        String wkChannel = StringUtils.EMPTY;
        this.inbkparm = new Inbkparm();

        try {
            // 讀取INBKPARM參數-代理/被代理
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno())) {
                acqFlag = "A"; // 代理
            } else {
                if (!"4".equals(this.feptxn.getFeptxnPcode().substring(3, 4))
                        || SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTrinBkno())) { /// *轉入交易*/
                    acqFlag = "I"; // 被代理
                } else { // 跨行轉帳-轉出交易
                    acqFlag = "O"; // 被代理-跨行轉出
                }
            }

            // 讀取INBKPARM參數-幣別
            if (StringUtils.isBlank(this.feptxn.getFeptxnZoneCode())) {
                txCur = CurrencyType.TWD.name();
            } else {
                switch (this.feptxn.getFeptxnZoneCode()) {
                    case ZoneCode.TWN:
                        txCur = CurrencyType.TWD.name();
                        break;
                    case ZoneCode.HKG:
                        txCur = CurrencyType.HKD.name();
                        break;
                    case ZoneCode.MAC:
                        txCur = CurrencyType.MOP.name();
                        break;
                }
            }

            // 判斷端末設備型態欄位
            if ("6011".equals(this.feptxn.getFeptxnAtmType())) {
                wkChannel = "A"; // 實體ATM
            } else {
                wkChannel = "V"; // 其餘為網路
            }

            // 判斷交易金額及每日優惠得到APID
            if (this.feptxn.getFeptxnTxAmt() != null
                    && this.feptxn.getFeptxnTxAmt().intValue() <= INBKConfig.getInstance().getTFRBenefitTxAmt()) {
                // 本行為轉出行由T24判斷每日優惠 OR 本行為轉入行(2521/2524) OR 代理兼轉入(2522)及純代理(2523/2524)
                if ((SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTroutBkno()) && "Y".equals(this.feptxn.getFeptxnBenefit())) ||
                        (!SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno()) &&
                                SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTrinBkno()) &&
                                (FISCPCode.PCode2521.getValueStr().equals(this.feptxn.getFeptxnPcode()) ||
                                        FISCPCode.PCode2524.getValueStr().equals(this.feptxn.getFeptxnPcode()))
                                &&
                                MathUtil.compareTo(this.feptxn.getFeptxnFeeCustpay(), 0) == 0)
                        ||
                        (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno()) &&
                                (FISCPCode.PCode2522.getValueStr().equals(this.feptxn.getFeptxnPcode()) ||
                                        FISCPCode.PCode2523.getValueStr().equals(this.feptxn.getFeptxnPcode()) ||
                                        FISCPCode.PCode2524.getValueStr().equals(this.feptxn.getFeptxnPcode()))
                                &&
                                MathUtil.compareTo(this.feptxn.getFeptxnFeeCustpay(), 0) == 0)) {
                    // 500元以下每日每帳戶免費1次
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTroutBkno())) {
                        getLogContext().setRemark("轉帳小額交易手續費免費1次");
                        logMessage(Level.INFO, getLogContext());
                    }
                    wkAPID = StringUtils.join("BEN", wkChannel);
                } else {
                    // 500元以下超過優惠次數, 每筆手續費10元
                    getLogContext().setRemark("轉帳金額500元以下超過優惠次數，每筆手續費10元");
                    logMessage(Level.INFO, getLogContext());
                    wkAPID = StringUtils.join("FRE", wkChannel);
                }
            } else {
                // 超過每日優惠次數或交易金額500~1000元，每筆手續費10元
                getLogContext().setRemark("轉帳超過優惠次數或交易金額500~1000元，每筆手續費10元");
                logMessage(Level.INFO, getLogContext());
                wkAPID = StringUtils.join("FRE", wkChannel);
            }

            // 讀取(INBKPARM)
            this.inbkparm.setInbkparmCur(txCur);
            this.inbkparm.setInbkparmApid(wkAPID);
            this.inbkparm.setInbkparmAcqFlag(acqFlag);
            this.inbkparm.setInbkparmEffectDate(this.feptxn.getFeptxnTbsdyFisc());
            this.inbkparm.setInbkparmPcode(this.feptxn.getFeptxnPcode());
            getLogContext().setRemark(
                    StringUtils.join(
                            "CUR=", txCur,
                            " APID=", wkAPID,
                            " ACQ_FLAG=", acqFlag,
                            " EFFECT_DATE=", this.feptxn.getFeptxnTbsdyFisc(),
                            " PCODE=", this.feptxn.getFeptxnPcode()));
            logMessage(Level.INFO, getLogContext());
            Inbkparm record = inbkparmExtMapper.queryByPK(this.inbkparm);
            if (record == null) {
                return FEPReturnCode.INBKPARMNotFound;
            }
            this.inbkparm = record;
            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".getInbkparmByIFT"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 取IDNO前二位數字轉換為文字(A~Z)後傳回,轉換後回傳10位文數字
     *
     * @param idno 11位數字的身份證字號
     * @return 10位文數字
     * 10->A,11->B, 34->I, 35->O
     */
    public String mappingFirstDigit(String idno) {
        if (idno.length() != 11) {
            throw new IllegalArgumentException("IDNO必須為11位");
        }
        // 非數字不需轉換
        if (!PolyfillUtil.isNumeric(idno)) {
            return idno;
        }
        String newIdno = "";
        int firstDigit = Integer.parseInt(idno.substring(0, 2));

        switch (firstDigit) {
            case 1:
                newIdno = "A" + idno.substring(2);
                break;
            case 2:
                newIdno = "B" + idno.substring(2);
                break;
            case 3:
                newIdno = "C" + idno.substring(2);
                break;
            case 4:
                newIdno = "D" + idno.substring(2);
                break;
            case 5:
                newIdno = "E" + idno.substring(2);
                break;
            case 6:
                newIdno = "F" + idno.substring(2);
                break;
            case 7:
                newIdno = "G" + idno.substring(2);
                break;
            case 8:
                newIdno = "H" + idno.substring(2);
                break;
            case 9:
                newIdno = "I" + idno.substring(2);
                break;
            case 10:
                newIdno = "J" + idno.substring(2);
                break;
            case 11:
                newIdno = "K" + idno.substring(2);
                break;
            case 12:
                newIdno = "L" + idno.substring(2);
                break;
            case 13:
                newIdno = "M" + idno.substring(2);
                break;
            case 14:
                newIdno = "N" + idno.substring(2);
                break;
            case 15:
                newIdno = "O" + idno.substring(2);
                break;
            case 16:
                newIdno = "P" + idno.substring(2);
                break;
            case 17:
                newIdno = "Q" + idno.substring(2);
                break;
            case 18:
                newIdno = "R" + idno.substring(2);
                break;
            case 19:
                newIdno = "S" + idno.substring(2);
                break;
            case 20:
                newIdno = "T" + idno.substring(2);
                break;
            case 21:
                newIdno = "U" + idno.substring(2);
                break;
            case 22:
                newIdno = "V" + idno.substring(2);
                break;
            case 23:
                newIdno = "W" + idno.substring(2);
                break;
            case 24:
                newIdno = "X" + idno.substring(2);
                break;
            case 25:
                newIdno = "Y" + idno.substring(2);
                break;
            case 26:
                newIdno = "Z" + idno.substring(2);
                break;
        }

        return newIdno;
    }

    /**
     * 取IDNO前二位數字轉換為文字(A~Z)後傳回,轉換後回傳10位文數字
     *
     * @param idno 11位數字的身份證字號
     * @return 10位文數字
     * 10->A, 11->B, 34->I, 35->O
     */
    public String mappingFirstDigitIdno(String idno) {
        if (idno.length() != 11) {
            throw new IllegalArgumentException("IDNO必須為11位");
        }
        // 非數字不需轉換
        if (!PolyfillUtil.isNumeric(idno)) {
            return idno;
        }
        String newIdno = "";
        int firstDigit = Integer.parseInt(idno.substring(0, 2));

        switch (firstDigit) {
            case 10:
                newIdno = "A" + idno.substring(2);
                break;
            case 11:
                newIdno = "B" + idno.substring(2);
                break;
            case 12:
                newIdno = "C" + idno.substring(2);
                break;
            case 13:
                newIdno = "D" + idno.substring(2);
                break;
            case 14:
                newIdno = "E" + idno.substring(2);
                break;
            case 15:
                newIdno = "F" + idno.substring(2);
                break;
            case 16:
                newIdno = "G" + idno.substring(2);
                break;
            case 17:
                newIdno = "H" + idno.substring(2);
                break;
            case 34:
                newIdno = "I" + idno.substring(2);
                break;
            case 18:
                newIdno = "J" + idno.substring(2);
                break;
            case 19:
                newIdno = "K" + idno.substring(2);
                break;
            case 20:
                newIdno = "L" + idno.substring(2);
                break;
            case 21:
                newIdno = "M" + idno.substring(2);
                break;
            case 22:
                newIdno = "N" + idno.substring(2);
                break;
            case 35:
                newIdno = "O" + idno.substring(2);
                break;
            case 23:
                newIdno = "P" + idno.substring(2);
                break;
            case 24:
                newIdno = "Q" + idno.substring(2);
                break;
            case 25:
                newIdno = "R" + idno.substring(2);
                break;
            case 26:
                newIdno = "S" + idno.substring(2);
                break;
            case 27:
                newIdno = "T" + idno.substring(2);
                break;
            case 28:
                newIdno = "U" + idno.substring(2);
                break;
            case 29:
                newIdno = "V" + idno.substring(2);
                break;
            case 32:
                newIdno = "W" + idno.substring(2);
                break;
            case 30:
                newIdno = "X" + idno.substring(2);
                break;
            case 31:
                newIdno = "Y" + idno.substring(2);
                break;
            case 33:
                newIdno = "Z" + idno.substring(2);
                break;
        }

        // If firstDigit = 34 Then
        // newIdno = "I" & idno.Substring(2)
        // ElseIf firstDigit = 35 Then
        // newIdno = "O" & idno.Substring(2)
        // Else
        // If firstDigit < 18 Then
        // newIdno = Chr(firstDigit + 55) & idno.Substring(2)
        // ElseIf firstDigit < 23 Then
        // newIdno = Chr(firstDigit + 56) & idno.Substring(2)
        // Else
        // newIdno = Chr(firstDigit + 57) & idno.Substring(2)
        // End If
        // End If

        return newIdno;
    }

    /**
     * 將ATM電文欄位搬入CARDTXN Class中
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>負責處理線上註銷卡片(多筆)，增加介面傳db物件</reason>
     * <date>2019/10/25</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareCARDTXN(Card card) {
        return prepareCARDTXNforTran(card, null);
    }

    /**
     * 將ATM電文欄位搬入CARDTXN Class中
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Maxine</modifier>
     * <reason>新增程式</reason>
     * <date>2011/08/29</date>
     * <modifier>YIN</modifier>
     * <reason>由ATMBusiness調整到Business共用</reason>
     * <date>2011/09/29</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareCARDTXNforTran(Card card, String db) {// TODO 缺少參數 DBHelper db)
        Cardtxn defCARDTXN = new Cardtxn();
        // 2019/10/25 Modify by Ruling for 線上註銷卡片(多筆)，增加介面傳db物件
        // Dim dbCARDTXN As New Tables.DBCARDTXN(FEPConfig.DBName)

        if (card != null) {
            // CALL 取號程式, 取得新EJ
            // 寫入卡片異動檔 */
            defCARDTXN.setEjfno(TxHelper.generateEj());
            defCARDTXN.setActno(card.getCardActno()); // 帳號
            defCARDTXN.setCardSeq(card.getCardCardSeq()); // 序號
            defCARDTXN.setCardno(card.getCardCardno()); // 卡號
            defCARDTXN.setIdno(card.getCardIdno1()); // 身分證字號
            // 2012-01-04 modified by KK 更正交易日期
            defCARDTXN.setTxDate(getFeptxn().getFeptxnTxDate()); // 交易日期
            defCARDTXN.setBrno(getFeptxn().getFeptxnBrno()); // 交易分行
            defCARDTXN.setMsgid(getFeptxn().getFeptxnTxCode()); // 交易代號
            defCARDTXN.setChlEjfno(getFeptxn().getFeptxnEjfno().toString());
            defCARDTXN.setChlName(getFeptxn().getFeptxnChannel());
            defCARDTXN.setChlSendtime(new Date()); // 系統時間
            // 2011/08/19 modify by Ruling for 分行系統
            defCARDTXN.setSelf(card.getCardSelf()); // 限制主卡交易記號
            defCARDTXN.setIcpu(card.getCardIcpu()); // 申請消費扣款記號
            defCARDTXN.setAppgp(card.getCardAppGp()); // 國際卡申請
            defCARDTXN.setApptfr(card.getCardTfrFlag()); // 約定轉帳記號
            defCARDTXN.setLastSelf(card.getCardSelf()); // 限制主卡交易記號
            defCARDTXN.setLastIcpu(card.getCardIcpu()); // 申請消費扣款記號
            defCARDTXN.setLastAppgp(card.getCardAppGp()); // 國際卡申請
            defCARDTXN.setLastApptfr(card.getCardTfrFlag()); // 約定轉帳記號
            defCARDTXN.setStatus(card.getCardStatus()); // 變更後卡片狀態
            defCARDTXN.setLastStatus(card.getCardNbcd()); // 變更前卡片狀態
            // Fly 2018/05/22 卡檔狀態在途未啟用註銷(=7), 列入註銷
            if (card.getCardStatus() == ATMCardStatus.Cancel.getValue() || card.getCardStatus() == ATMCardStatus.WithoutUsageCancel.getValue()) {
                defCARDTXN.setTxcd((short) 5); // 申請項目=註銷 */
            }
            if (card.getCardStatus() == ATMCardStatus.Start.getValue()) {
                defCARDTXN.setTxcd((short) 1); /// *申請項目=啟用 */
            }
            if (card.getCardStatus() == ATMCardStatus.Lose.getValue()) {
                defCARDTXN.setTxcd((short) 4); /// *申請項目=掛失 */
            }
            // 2019/07/24 Modify by Ruling for 信用卡掛失Debit卡新增即時通知悠遊卡
            if (String.valueOf(SVCSTXCD.SD4.getValue()).equals(defCARDTXN.getMsgid())) {
                defCARDTXN.setEasylost(card.getCardEasylost());
            }
            // 2014/08/12 modify by Ruling for COMBO優化：將停卡理由碼寫入CARDTXN
            if (ATMTXCD.AP1.name().equals(defCARDTXN.getMsgid())) {
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnNoticeId())) {
                    defCARDTXN.setTermid(getFeptxn().getFeptxnNoticeId().trim());
                }
            }
            // 2017/04/14 modify by Ruling for 消費扣款功能自動啟用
            if (!StringUtils.isBlank(getFeptxn().getFeptxnPcode())
                    && (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2525.getValueStr().equals(getFeptxn().getFeptxnPcode()))) {
                defCARDTXN.setMsgid(getFeptxn().getFeptxnPcode());
                defCARDTXN.setTxcd((short) 3); // 申請項目=變更 */
                defCARDTXN.setIcpu((short) 1);
                defCARDTXN.setLastIcpu((short) 0);
            }

            // Fly 2018/03/13 for 國外提款提醒及管控機制
            if (!StringUtils.isBlank(getFeptxn().getFeptxnPcode())
                    && (FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode()))) {
                defCARDTXN.setMsgid(getFeptxn().getFeptxnPcode());
                defCARDTXN.setTxcd((short) 3); // 申請項目=變更 */
                defCARDTXN.setLastOwdGp(card.getCardOwdGp());
                defCARDTXN.setOwdGp("1");
            }

            if (cardtxnMapper.insertSelective(defCARDTXN) < 1) {
                // 2019/10/25 Modify by Ruling for 線上註銷卡片(多筆)，如db物件有Transaction要Rollback
                /*
                 * if (db.Transaction != null) { todo
                 * db.RollbackTransaction();
                 * }
                 */
                getLogContext().setReturnCode(FEPReturnCode.InsertFail);
                sendEMS(getLogContext());
                return FEPReturnCode.InsertFail;
            }
        }

        return FEPReturnCode.Normal;
    }

    /**
     * 檢核卡檔後更新卡檔
     *
     * @param card 要更新的卡檔物件
     *
     *             <history>
     *             <modify>
     *             <modifier>Kyo</modifier>
     *             <reason>從檢核卡檔把更新的部分切出來</reason>
     *             <date>2011/02/10</date>
     *             </modify>
     *             <modify>
     *             <modifier>Kyo</modifier>
     *             <reason>更新卡檔的CARD_TX_RC_N欄位值需要透過轉換</reason>
     *             <date>2011/03/15</date>
     *             </modify>
     *             <modify>
     *             <modifier>Kyo</modifier>
     *             <reason>卡檔的CARD_TX_RC_N欄位值若為正常，且為自行ATM交易就不用轉換直接填四位空白</reason>
     *             <date>2011/03/17</date>
     *             </modify>
     *             </history>
     */
    public void updateCard(Card card, RefBase<FEPReturnCode> rtn) {
        CardMapper cardMapper = SpringBeanFactoryUtil.getBean(CardMapper.class);
        try {
            // 3.更新卡片檔
            // 將本次交易資料班制上次交易資料
            card.setCardTxDateLast(card.getCardTxDate());
            card.setCardTxAtmidLast(card.getCardTxAtmid());
            card.setCardTxBankLast(card.getCardTxBank());
            card.setCardTxStanLast(card.getCardTxStan());
            card.setCardTxRcLast(card.getCardTxRcN());
            card.setCardTxCavLast(card.getCardTxCav());
            card.setCardTxConLast(card.getCardTxCon());
            card.setCardTxDate(getFeptxn().getFeptxnTxDate());
            card.setCardTxAtmid(getFeptxn().getFeptxnAtmno());
            card.setCardTxBank(getFeptxn().getFeptxnBkno());
            card.setCardTxStan(getFeptxn().getFeptxnEjfno().toString());
            if (rtn.get() == FEPReturnCode.Normal) {
                card.setCardTxRcN(NormalRC.ATM_OK);
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                    card.setCardTxRcN(TxHelper.getRCFromErrorCode(String.valueOf(rtn.get().getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
                } else {
                    card.setCardTxRcN(TxHelper.getRCFromErrorCode(String.valueOf(rtn.get().getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()));
                }
            }
            if (cardMapper.updateByPrimaryKeySelective(card) < 1) {
                rtn.set(IOReturnCode.CARDUpdateError);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public FEPReturnCode prepareOWDEMAIL(String txdate, String actno) {
//        SmsmsgExtMapper msmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
//        Smsmsg def = new Smsmsg();
//        SmlparmMapper dbSML = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//        Smlparm defSML = new Smlparm();
//
//        StringBuilder sSQL = new StringBuilder();
//        List<Smsmsg> smsmsgs = msmsgExtMapper.queryAll(txdate, actno);
//        if (smsmsgs.size() <= 0) {
//            getLogContext().setRemark("SMSMSG查無資料");
//            logMessage(Level.INFO, getLogContext());
//            return FEPReturnCode.Normal;
//        }
//        def.setSmsmsgTxDate(smsmsgs.get(0).getSmsmsgTxDate());
//        def.setSmsmsgEjfno(smsmsgs.get(0).getSmsmsgEjfno());
//        Smsmsg smsmsg = msmsgExtMapper.selectByPrimaryKey(def.getSmsmsgTxDate(), def.getSmsmsgEjfno());
//        if (smsmsg != null) {
//            def = smsmsg;
//        }
//        // Fly 2018/06/27 無電話號碼(簡訊)仍要繼續往下走
//        if (StringUtils.isBlank(def.getSmsmsgNumber())) {
//            getLogContext().setRemark("無電話號碼，不需送SMS");
//            logMessage(Level.INFO, getLogContext());
//        } else {
//            try {
//                def.setSmsmsgSend("Y");
//                if (msmsgExtMapper.updateByPrimaryKeySelective(def) <= 0) {
//                    getLogContext().setRemark("更新SMSMSG異常，不送SMS");
//                    logMessage(Level.INFO, getLogContext());
//                    return FEPReturnCode.Normal;
//                }
//            } catch (Exception ex) {
//                getLogContext().setRemark("更新SMSMSG異常，不送SMS  " + ex.toString());
//                logMessage(Level.INFO, getLogContext());
//                return FEPReturnCode.Normal;
//            }
//
//            sSQL.setLength(0);
//            defSML.setSmlparmType("S");
//            defSML.setSmlparmSeqno(2);
//            Smlparm smlparm = dbSML.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//            if (smlparm != null) {
//                defSML = smlparm;
//            }
//            String msg;
//            msg = defSML.getSmlparmContent();
//
//            // Fly 2018/06/12 將發送SMSDB移至TxHelper
//            TxHelper.sendSMSDB(def.getSmsmsgNumber(), msg, defSML.getSmlparmPriority().byteValue(), def.getSmsmsgBrno(), def.getSmsmsgPcode(), def.getSmsmsgCbsRrn(), def.getSmsmsgIdno(),
//                    defSML.getSmlparmCompany(), defSML.getSmlparmChannel(), getLogContext());
//        }
//
//        // Fly 2018/06/11 應判斷SMSMSG_EMAIL欄位
//        if (StringUtils.isBlank(def.getSmsmsgEmail())) {
//            getLogContext().setRemark("無EMAIL");
//            logMessage(Level.INFO, getLogContext());
//            return FEPReturnCode.Normal;
//        }
//
//        defSML.setSmlparmType("M");
//        defSML.setSmlparmSeqno(2);
//        Smlparm smlparm = dbSML.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//        if (smlparm != null) {
//            defSML = smlparm;
//        }
//        String mailBody = defSML.getSmlparmContent();
//        String subject = defSML.getSmlparmSubject();
//
//        // Fly 2018/06/12 將發送EMAIL移至TxHelper
//        TxHelper.sendMailHunter(defSML.getSmlparmProj(), def.getSmsmsgEmail(), defSML.getSmlparmFromname(), defSML.getSmlparmFromemail(), subject, mailBody, defSML.getSmlparmChannel(),
//                defSML.getSmlparmPgcode(), def.getSmsmsgIdno(), defSML.getSmlparmPriority().toString(), getLogContext());
//        // Dim sn As SendNow.SendNowSoapClient = New SendNow.SendNowSoapClient()
//
//        // getLogContext().setRemark(); = String.Format("Ready Send data to {0}", sn.Endpoint.Address.ToString())
//        // logMessage(LogLevel.Info, LogContext)
//        // getLogContext().setRemark(); = "MailHunter Result : " + sn.SendNowAPI(defSML.SMLPARM_PROJ, "", def.SMSMSG_EMAIL, defSML.SMLPARM_FROMNAME, defSML.SMLPARM_FROMEMAIL, subject, mailBody,
//        // defSML.SMLPARM_CHANNEL, defSML.SMLPARM_PGCODE, def.SMSMSG_IDNO,
//        // Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, "", defSML.SMLPARM_PRIORITY.ToString()).ToString()
//        // logMessage(LogLevel.Info, LogContext)

        return FEPReturnCode.Normal;
    }

    /**
     * 取得牌告匯率
     *
     * @param zoneCode input: char(3) 地區別
     * @param txCur    input: char(3) 提領幣別
     * @param txCurAct input: char(3) 主帳戶幣別
     * @param txAmt    input: decimail(13,2) 提領金額
     * @param txAmtAct output: decimail(13,2) 主帳戶金額
     * @param exRate   output: decimal(12,7) 匯率
     * @return <history>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>new function P2-201</reason>
     * <date>2010/10/05</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode getExchangeAmount(String zoneCode, String txCur, String txCurAct, BigDecimal txAmt, RefString txAmtAct, RefString exRate) {
        try {
            FEPReturnCode rtnCode = CommonReturnCode.Normal;
            RefString mdFlag = new RefString("");
            rtnCode = getExchangeRate(zoneCode, txCur, txCurAct, "0", exRate, mdFlag);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            if (mdFlag.get().equals("0")) {
                // 乘記號
                txAmtAct.set(String.valueOf(txAmt.multiply(new BigDecimal(exRate.get()))));
            } else {
                // 除記號
                txAmtAct.set(String.valueOf(txAmt.divide(new BigDecimal(exRate.get()))));
            }
            if (CurrencyType.TWD.name().equals(txCurAct)) {
                txAmtAct.set(MathUtil.roundUp(new BigDecimal(txAmtAct.get()), 0).toString()); // 四捨五入至整數
            } else {
                txAmtAct.set(MathUtil.roundUp(new BigDecimal(txAmtAct.get()), 2).toString()); // 四捨五入至整數
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".getExchangeAmount"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return FEPReturnCode.Normal;
    }

    public FEPReturnCode prepareATMFTEMAIL() {
//        Smsmsg def = new Smsmsg();
//        Smlparm defSML = new Smlparm();
//        StringBuilder sSQL = new StringBuilder();
//        try {
//            def.setSmsmsgTxDate(getFeptxn().getFeptxnTxDate());
//            def.setSmsmsgEjfno(getFeptxn().getFeptxnEjfno());
//            Smsmsg smsmsg = smsmsgMapper.selectByPrimaryKey(def.getSmsmsgTxDate(), def.getSmsmsgEjfno());
//            if (smsmsg == null) {
//                getLogContext().setRemark("SMSMSG查無資料");
//                logMessage(Level.INFO, getLogContext());
//                return FEPReturnCode.Normal;
//            }
//            if ("N".equals(smsmsg.getSmsmsgNotifyFg())) {
//                getLogContext().setRemark("客戶主動取消通知 不需送 SMS");
//                logMessage(Level.INFO, getLogContext());
//                return FEPReturnCode.Normal;
//            }
//
//            if (StringUtils.isNotBlank(smsmsg.getSmsmsgEmail()) && "1".equals(smsmsg.getSmsmsgEtouchFg())) {
//                smsmsg.setSmsmsgSend("Y");
//                smsmsg.setSmsmsgSendType("M");
//                // 組送MAIL
//
//                sSQL.setLength(0);
//                // pamTemp.Clear();
//                defSML.setSmlparmType("M");
//                defSML.setSmlparmSeqno(5);
//
//                Smlparm smlparm = smlparmMapper.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//                if (smlparm == null) {
//                    getLogContext().setRemark(StringUtils.join("SMLPARM查無資料 TYPE[", defSML.getSmlparmType(), "] SEQNO[", defSML.getSmlparmSeqno(), "]"));
//                    logMessage(Level.INFO, getLogContext());
//                    return FEPReturnCode.Normal;
//                }
//
//                String mailBody = smlparm.getSmlparmContent();
//                String subject = smlparm.getSmlparmSubject();
//
//                String txdate = smsmsg.getSmsmsgTxDate();
//                StringBuilder sb = new StringBuilder(txdate);
//                sb.insert(6, "月");
//                sb.insert(4, "年");
//                txdate = sb.toString() + "日";
//                String actno = smsmsg.getSmsmsgTroutActno().substring(2);
//                actno = actno.substring(0, 8) + "***" + actno.substring(11, 13) + "*";
//                sb = new StringBuilder(actno);
//                sb.insert(13, "-");
//                sb.insert(6, "-");
//                sb.insert(3, "-");
//                actno = sb.toString();
//                String time = smsmsg.getSmsmsgTxDate() + smsmsg.getSmsmsgTxTime();
//                sb = new StringBuilder(time);
//                sb.insert(12, ":");
//                sb.insert(10, ":");
//                sb.insert(8, " ");
//                sb.insert(6, "/");
//                sb.insert(4, "/");
//                time = sb.toString();
//                String brno = smsmsg.getSmsmsgBrno();
//
//                Allbank defBank = new Allbank();
//                defBank.setAllbankBkno(SysStatus.getPropertyValue().getSysstatHbkno());
//                defBank.setAllbankBrno(smsmsg.getSmsmsgBrno());
//                Allbank allbank = allbankExtMapper.selectByPrimaryKey(defBank.getAllbankBkno(), defBank.getAllbankBrno());
//                if (allbank != null) {
//                    brno = allbank.getAllbankFullname().trim();
//                }
//
//                mailBody = mailBody.replace("[PARM1]", (txdate));
//                mailBody = mailBody.replace("[PARM2]", brno);
//                mailBody = mailBody.replace("[PARM3]", actno);
//                mailBody = mailBody.replace("[PARM4]", smsmsg.getSmsmsgTxCur());
//                mailBody = mailBody.replace("[PARM5]", FormatUtil.longFormat(smsmsg.getSmsmsgTxAmt().intValue(), "#,##0"));
//                mailBody = mailBody.replace("[PARM6]", time);
//
//                TxHelper.sendMailHunter(smlparm.getSmlparmProj(), smsmsg.getSmsmsgEmail(), smlparm.getSmlparmFromname(),
//                        smlparm.getSmlparmFromemail(), subject, mailBody, smlparm.getSmlparmChannel(),
//                        smlparm.getSmlparmPgcode(), smsmsg.getSmsmsgIdno(), smlparm.getSmlparmPriority().toString(),
//                        getLogContext());
//            } else if (StringUtils.isNotBlank(smsmsg.getSmsmsgSmsPhone()) && "1".equals(smsmsg.getSmsmsgStouchFg())) {
//                smsmsg.setSmsmsgSend("Y");
//                smsmsg.setSmsmsgSendType("S");
//                // 組送SMS簡訊
//
//                sSQL.setLength(0);
//                // pamTemp.Clear();
//                defSML.setSmlparmType("S");
//                defSML.setSmlparmSeqno(3);
//                Smlparm smlparm = smlparmMapper.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//                if (smlparm == null) {
//                    getLogContext().setRemark(StringUtils.join("SMLPARM查無資料 TYPE[", defSML.getSmlparmType(), "] SEQNO[", "]", defSML.getSmlparmSeqno()));
//                    logMessage(Level.INFO, getLogContext());
//                    return FEPReturnCode.Normal;
//                }
//
//                String msg = smlparm.getSmlparmContent();
//                StringBuilder sb = new StringBuilder(smsmsg.getSmsmsgTxDate() + smsmsg.getSmsmsgTxTime().substring(0, 4));
//                sb.insert(10, ":");
//                sb.insert(8, " ");
//                sb.insert(6, "/");
//                sb.insert(4, "/");
//                msg = msg.replace("[PARM1]", sb.toString());
//                msg = msg.replace("[PARM2]", FormatUtil.longFormat(smsmsg.getSmsmsgTxAmt().intValue(), "#,##0"));
//                TxHelper.sendSMSDB(smsmsg.getSmsmsgSmsPhone(), msg, Byte.valueOf(smlparm.getSmlparmPriority().toString()),
//                        smsmsg.getSmsmsgBrno(), smsmsg.getSmsmsgPcode(), smsmsg.getSmsmsgCbsRrn(), smsmsg.getSmsmsgIdno(),
//                        smlparm.getSmlparmCompany(), smlparm.getSmlparmChannel(), getLogContext());
//            } else {
//                smsmsg.setSmsmsgSendType("P");
//                // 失聯客戶, 送核心支援系統
//            }
//
//            if (smsmsgMapper.updateByPrimaryKeySelective(smsmsg) <= 0) {
//                getLogContext().setRemark("SMSMSG更新失敗");
//                logMessage(Level.INFO, getLogContext());
//            }
//
//        } catch (Exception ex) {
//            getLogContext().setRemark("PrepareATMFTEMAIL發生異常 " + ex.toString());
//            logMessage(Level.INFO, getLogContext());
//        }
        return FEPReturnCode.Normal;
    }

    /**
     * 取得牌告匯率
     *
     * @param zoneCode input: char(3) 地區別
     * @param txCur    input: char(3) 提領幣別
     * @param txCurAct input: char(3) 主帳戶幣別
     * @param staff    input: char(1) 身份別 ("1":行員 空白:非行員)
     * @param exRate   output: decimal(12,7) 匯率
     * @param mdFlag   output: char(1) 乘除記號(0: * , 1:/)
     * @return <history>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>new function P2</reason>
     * <date>2010/10/05</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>ListRate DB Schema modify</reason>
     * <date>2011/1/12</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode getExchangeRate(String zoneCode, String txCur, String txCurAct, String staff, RefString exRate, RefString mdFlag) {
        try {
            Listrate listrate = new Listrate();
            // 判斷海外卡至台灣跨區提款
            if ((ATMZone.HKG.name().equals(zoneCode) || ATMZone.MAC.name().equals(zoneCode)) && CurrencyType.TWD.name().equals(txCur)) {
                zoneCode = ATMZone.TWN.name();
            }
            // 2012/05/09 Modify by Ruling for 香港卡至澳門跨區提領葡幣(MOP)，因香港敲價系統不提供MOP/HKD現鈔匯率
            // 故調整程式，匯率= 1/1.03 (為0.97087) 取至小數點後五位，四捨五入
            // 香港卡至澳門跨區提領葡幣
            if (ATMZone.HKG.name().equals(zoneCode) & CurrencyType.MOP.name().equals(txCur)) {
                getFeptxn().setFeptxnExrate(MathUtil.roundUp(1 / ATMPConfig.getInstance().getHKDMOPRate(), 5));
                getFeptxn().setFeptxnScash(getFeptxn().getFeptxnExrate());
                exRate.set(getFeptxn().getFeptxnExrate().toString());
                mdFlag.set("0"); // 代表乘(*)
                return CommonReturnCode.Normal;
            }
            // 2013/10/03 Modify by Ruling for 港澳NCB
            // 2012/07/16 Modify by Ruling for 澳門卡港幣戶(HKD)提領葡幣(MOP)
            // 2012/05/30 Modify by Ruling for 澳門卡至香港跨區提領港幣(HKD)，因澳門敲價系統不提供HKD/MOP現鈔匯率
            // 故調整程式，匯率= 1.03
            // 澳門卡至香港跨區提領港幣
            if (ATMZone.MAC.name().equals(zoneCode) && (CurrencyType.HKD.name().equals(txCur) || CurrencyType.MOP.name().equals(txCur))
                    && !DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                getFeptxn().setFeptxnExrate(BigDecimal.valueOf(ATMPConfig.getInstance().getHKDMOPRate()));
                getFeptxn().setFeptxnScash(getFeptxn().getFeptxnExrate());
                exRate.set(getFeptxn().getFeptxnExrate().toString());
                mdFlag.set("0"); // 代表乘(*)
                return CommonReturnCode.Normal;
            }
            // 以幣別對=提領幣別/主帳戶幣, 讀取牌告匯率
            listrate.setListrateZoneCode(zoneCode);
            listrate.setListratePairname(StringUtils.join(txCur, "/", txCurAct));
            Listrate record = listrateMapper.selectByPrimaryKey(listrate.getListrateZoneCode(), listrate.getListratePairname());
            if (record != null) {
                if ("1".equals(staff)) {// STAFF = "1" =>行員
                    // 2011-05-11 by kyo for spec修改:/* 5/11 修改 */搬錯欄位
                    getFeptxn().setFeptxnExrate(record.getListrateNotempsell()); // 行員優惠匯率
                } else {
                    // 非行員
                    getFeptxn().setFeptxnExrate(record.getListrateNotesell()); // 賣出現鈔
                }
                getFeptxn().setFeptxnScash(getFeptxn().getFeptxnExrate());
                exRate.set(getFeptxn().getFeptxnExrate().toString());
                mdFlag.set("0"); // 代表乘(*)
            } else {
                // 以幣別對=主帳戶幣別/提領幣別, 讀取牌告匯率 */
                listrate.setListrateZoneCode(zoneCode);
                listrate.setListratePairname(StringUtils.join(txCurAct, "/", txCur));
                record = listrateMapper.selectByPrimaryKey(listrate.getListrateZoneCode(), listrate.getListratePairname());
                if (record != null) {
                    if ("1".equals(staff)) {// STAFF = "1" =>行員
                        // 2015/05/19 Modify by Ruling for 海外卡跨區提款取錯匯率
                        getFeptxn().setFeptxnExrate(record.getListrateNotempbuy()); // 行員優惠匯率 */
                    } else {
                        // 非行員 */
                        // 2015/05/19 Modify by Ruling for 海外卡跨區提款取錯匯率
                        getFeptxn().setFeptxnExrate(listrate.getListrateNotebuy()); // 買入現鈔 */
                    }
                    getFeptxn().setFeptxnScash(getFeptxn().getFeptxnExrate());
                    exRate.set(getFeptxn().getFeptxnExrate().toString());
                    mdFlag.set("1"); // 代表除(/)
                } else {
                    return ATMReturnCode.NoRateTable;
                }
            }
            if ("0".equals(exRate.get())) {
                return ATMReturnCode.NoRateTable;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".getExchangeRate"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 檢核無卡提款密碼錯誤次數
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>Function Design</reason>
     * <date>2017/01/26</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkNCPWErrCnt(String type) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            switch (type) {
                case "1": // 密碼錯
                    getCard().setCardErrCnt((short) (getCard().getCardErrCnt().intValue() + 1));
                    if (getCard().getCardErrCnt() < ATMPConfig.getInstance().getNWDErrCount()) {
                        rtnCode = ENCReturnCode.ENCCheckPasswordError;
                        getLogContext().setRemark("密碼錯誤");
                        logMessage(Level.INFO, getLogContext());
                    } else {
                        rtnCode = ATMReturnCode.OverPasswordErrorCount;
                        getLogContext().setRemark("密碼錯誤已達5次");
                        logMessage(Level.INFO, getLogContext());
                        getCard().setCardNcstatus((short) 6);
                        getCard().setCardNccloseDate(getFeptxn().getFeptxnTxDate());
                        getCard().setCardNccloseTime(getFeptxn().getFeptxnTxTime());
                        prepareNCCARDTXN(getCard());
                    }
                    break;
                case "2": // 密碼正確
                    getCard().setCardErrCnt((short) 0);
                    break;
            }

            if (cardExtMapper.updateByPrimaryKeySelective(getCard()) <= 0) {
                rtnCode = IOReturnCode.UpdateFail;
                getLogContext().setRemark("CheckNCPWErrCnt-更新無卡提款卡片檔(NCCARD)失敗");
                logMessage(Level.INFO, getLogContext());
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkNCPWErrCnt");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {

        }
    }

    /**
     * 將ATM電文欄位搬入CARDTXN Class中
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>負責處理線上註銷卡片(多筆)，增加介面傳db物件</reason>
     * <date>2019/10/25</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareNCCARDTXN(Card CARD) {
        return prepareNCCARDTXNforTran(CARD, null);
    }

    /**
     * 寫入卡片異動檔
     *
     * @return <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <reason>For 無卡提款</reason>
     * <date>2017/01/24</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareNCCARDTXNforTran(Card CARD, String db) {
        Cardtxn defCARDTXN = new Cardtxn();

        // 2019/10/25 Modify by Ruling for 線上註銷卡片(多筆)，增加介面傳db物件

        try {
            if (CARD != null) {
                // 寫入卡片異動檔 */
                defCARDTXN.setEjfno(TxHelper.generateEj());
                defCARDTXN.setActno(CARD.getCardActno()); // 帳號
                defCARDTXN.setCardSeq(CARD.getCardCardSeq()); // 序號
                defCARDTXN.setCardno(CARD.getCardCardno()); // 卡號
                defCARDTXN.setTxDate(getFeptxn().getFeptxnTxDate()); // 交易日期
                defCARDTXN.setBrno(getFeptxn().getFeptxnBrno()); // 交易分行
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                    defCARDTXN.setMsgid(getFeptxn().getFeptxnPcode()); // 財金交易代號
                } else {
                    defCARDTXN.setMsgid(getFeptxn().getFeptxnTxCode()); // 交易代號
                }

                defCARDTXN.setChlEjfno(getFeptxn().getFeptxnEjfno().toString());
                defCARDTXN.setChlName(getFeptxn().getFeptxnChannel());
                defCARDTXN.setChlSendtime(new Date()); // 系統時間
                // Fly 2019/01/25 For 企業無卡提款
                if (ATMTXCD.NWA.name().equals(getFeptxn().getFeptxnTxCode())) {
                    if ("1".equals(getFeptxn().getFeptxnNoticeId())) {
                        defCARDTXN.setTxcd((short) 1); /// *申請項目=啟用 */
                        defCARDTXN.setTxname("企業戶無卡提款設定");
                    } else {
                        defCARDTXN.setTxcd((short) 5); // 申請項目=註銷 */
                        defCARDTXN.setTxname("企業戶無卡提款解除");
                    }
                } else {
                    if (CARD.getCardNcstatus() == ATMCardStatus.Cancel.getValue()) {
                        defCARDTXN.setTxcd((short) 5); // 申請項目=註銷 */
                    } else {
                        defCARDTXN.setTxcd((short) 1); /// *申請項目=啟用 */
                    }
                }

                if (cardtxnMapper.insertSelective(defCARDTXN) < 1) {
                    // 2019/10/25 Modify by Ruling for 線上註銷卡片(多筆)，如db物件有Transaction要Rollback
                    getLogContext().setReturnCode(FEPReturnCode.InsertFail);
                    sendEMS(getLogContext());
                    return FEPReturnCode.InsertFail;
                }
            }
        } catch (Exception ex) {
            // 2019/10/25 Modify by Ruling for 線上註銷卡片(多筆)，如db物件有Transaction要Rollback
            getLogContext().setReturnCode(FEPReturnCode.InsertFail);
            getLogContext().setRemark(ex.toString());
            sendEMS(getLogContext());
            return FEPReturnCode.InsertFail;
        }

        return FEPReturnCode.Normal;
    }

    /**
     * 檢核單筆提款限額
     *
     * @return <history>
     * <modify>
     * <modifier>Jim</modifier>
     * <reason>Function</reason>
     * <date>2009/12/9</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>調整程式</reason>
     * <date>2010/06/01</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>spec修改:以下判斷自行與跨行共用</reason>
     * <date>2010/07/13</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>spec修改:將檢核限額判斷分開兩段，不共用</reason>
     * <date>2010/07/21</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkTransLimit(Msgctl txMsgCtl) {
        BigDecimal limit = new BigDecimal(0);
        try {
            if (getFeptxn().getFeptxnSubsys() == SubSystem.INBK.getValue()) {
                /*跨行*/
                switch (txMsgCtl.getMsgctlCheckLimit()) {
                    case 1: // 檢核提款限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKTxLimit());
                        break;
                    case 2: // 檢核轉帳限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKFTLimit());
                        break;
                    case 3: // 檢核消費扣款限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKPCLimit());
                        break;
                    case 4: // 檢核全國繳費ID+Account限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKIDLimit());
                        break;
                    case 5: // 檢核晶片卡跨國提款限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getICTxLimit());
                        break;
                    case 6: // 檢核晶片卡跨國消費扣款限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getICPCLimit());
                        break;
                    case 7: // 檢核跨境電子支付限額
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getOBTxLimit());
                        break;
                    case 8: ///*檢核繳稅限額*  By Aster/
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKTAXLimit());
                        break;
                    case 9: /*檢核本行卡跨行存款限額*/
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKSDPLimit());
                        break;
                    case 10: /*檢核他行卡跨行存款限額*/
                        limit = BigDecimal.valueOf(INBKConfig.getInstance().getINBKODPLimit());
                        break;
                }
                getLogContext().setRemark("LIMIT=" + limit.doubleValue());
                logMessage(Level.DEBUG, getLogContext());
                /* 其他跨行交易 */
                switch (txMsgCtl.getMsgctlCheckLimit()) {
                    case 5:
                    case 6: // 國際卡交易(2450/1630/2545/2571)
//                        if (FISCPCode.PCode2450.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                                || FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
//                            if (getFeptxn().getFeptxnTxAmtAct().doubleValue() > limit.doubleValue()) {
//                                return CommonReturnCode.OverLimit;
//                            }
//                        } else {
//                            BigDecimal w_TOT_AMT = MathUtil.roundUp(getFeptxn().getFeptxnTxAmt().multiply(getFeptxn().getFeptxnExrate()), 0);// 四捨五入至元
//                            if (w_TOT_AMT.compareTo(limit) > 0) {
//                                return CommonReturnCode.OverLimit;
//                            }
//                        }
                        /* 2024/7/9 修改, 檢核單筆限額 */
                        if (getFeptxn().getFeptxnTxAmtAct().doubleValue() > limit.doubleValue()) {
                            return CommonReturnCode.OverLimit; // 提款金額超過每次限額
                        }
                        // Dim w_TOT_AMT As Decimal = Decimal.Round(getFeptxn().FEPTXN_TX_AMT * getFeptxn().FEPTXN_EXRATE, MidpointRounding.AwayFromZero) '四捨五入至元
                        // If w_TOT_AMT > limit Then
                        // Return CommonReturnCode.OverLimit
                        // End If
                        // 'If getFeptxn().FEPTXN_TX_AMT_ACT > limit Then
                        // ' Return CommonReturnCode.OverLimit
                        // 'End If
                        break;
                    case 7: // 跨境電子支付(2555)，單筆限額=消費者支付台幣總金額(含手續費)
                        if (getFeptxn().getFeptxnTxAmtSet().doubleValue() > limit.doubleValue()) {
                            return CommonReturnCode.OverLimit;
                        }
                        break;
                    default:
                        getLogContext().setRemark("FeptxnTxAmt=" + getFeptxn().getFeptxnTxAmt().doubleValue());
                        logMessage(Level.DEBUG, getLogContext());
                        if (getFeptxn().getFeptxnTxAmt().doubleValue() > limit.doubleValue()) {
                            return CommonReturnCode.OverLimit;
                        }
                        break;
                }
            } else {
                // 自行
//				Atmmstr atmmstr = new Atmmstr();
//				atmmstr.setAtmAtmno(getFeptxn().getFeptxnAtmno());
//				AtmmstrMapper atmmstrMapper = SpringBeanFactoryUtil.getBean(AtmmstrMapper.class);
//				atmmstr = atmmstrMapper.selectByPrimaryKey(atmmstr.getAtmAtmno());
//				if (atmmstr == null) {
//					return IOReturnCode.ATMMSTRNotFound;
//				}
//				if (ATMZone.TWN.name().equals(atmmstr.getAtmZone()) && StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {

                //一般帳號
                if (StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {
                    if (txMsgCtl.getMsgctlCheckLimit() == 1) {
                        // 檢核單筆提款限額
                        // If _defatmmstr.ATM_ATMTYPE = CInt(ATMType.ADM) Then
                        // '檢核存提款機單筆提領限額
                        // If getFeptxn().FEPTXN_TX_CUR = CurrencyType.TWD.ToString Then
                        // limit = Configuration.ATMPConfig.Instance.ADMLimit
                        // End If
                        // Else
                        // 檢核提款機單筆提領限額
                        if (String.valueOf(CurrencyType.TWD.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            // Fly 2020/04/14 新存提款機可提領外幣
//							if (atmmstr.getAtmAtmtype() == ATMType.ADM.getValue()) {
//								// 檢核存提款機單筆提領限額
//								limit = BigDecimal.valueOf(ATMPConfig.getInstance().getADMLimit());
//							} else {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getTWDTxLimit());
//							}
                        } else if (String.valueOf(CurrencyType.USD.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getUSDTxLimit());
                        } else if (String.valueOf(CurrencyType.JPY.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getJPYTxLimit());
                        }
                    }
                    if (txMsgCtl.getMsgctlCheckLimit() == 2) {
                        // 檢核單筆轉帳限額
                        limit = BigDecimal.valueOf(ATMPConfig.getInstance().getFTLimit());
                    }
                    if (txMsgCtl.getMsgctlCheckLimit() == 3) {
                        // 檢核單筆無卡提款限額
                        if (String.valueOf(CurrencyType.TWD.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getNCLimit());
                        } else if (String.valueOf(CurrencyType.USD.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getNCUSDLimit());
                        } else if (String.valueOf(CurrencyType.JPY.name()).equals(getFeptxn().getFeptxnTxCur())) {
                            limit = BigDecimal.valueOf(ATMPConfig.getInstance().getNCJPYLimit());
                        }
                    }
                    //檢核全國繳費ID+Account限額*  By Aster
                    if (txMsgCtl.getMsgctlCheckLimit() == 4) {
                        limit = BigDecimal.valueOf(ATMPConfig.getInstance().getINBKIDLimit());
                    }
                    /* 檢核晶片卡存款限額 */
                    if (txMsgCtl.getMsgctlCheckLimit() == 5) {
                        limit = BigDecimal.valueOf(ATMPConfig.getInstance().getADMLimit());
                    }
                    if (getFeptxn().getFeptxnTxAmt().doubleValue() > limit.doubleValue()) {
                        // 2018/11/22 Modify by Ruling for 外幣無卡提款，增加Log
                        getLogContext().setRemark("提款金額超過限額, 幣別=" + getFeptxn().getFeptxnTxCur() + ", 限額=" + limit + ", 提款金額=" + getFeptxn().getFeptxnTxAmt());
                        logMessage(Level.INFO, getLogContext());
                        return CommonReturnCode.OverLimit;
                    }
                }
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkTransLimit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } catch (ExceptionInInitializerError exErr) {
            //因 INBKConfig static 初始化時出錯 用來攔截錯誤
            getLogContext().setProgramException(exErr);
            getLogContext().setProgramName(ProgramName + ".checkTransLimit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核無卡提款資料
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>Function Design</reason>
     * <date>2017/03/08</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkNCNWDData(RefBase<Nwdtxn> nwdtxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Nwdreg defNWDREG = new Nwdreg();

        try {

            // 1.檢核是否有申請無卡提款
            defNWDREG.setNwdregTxDate(getFeptxn().getFeptxnTxDate());
            defNWDREG.setNwdregNwdseq(getFeptxn().getFeptxnMajorActno());
            Nwdreg nwdreg = nwdregMapper.selectByPrimaryKey(defNWDREG.getNwdregTxDate(), defNWDREG.getNwdregNwdseq());
            if (nwdreg == null) {
                if (ATMPConfig.getInstance().getNCCustTime().compareTo(getFeptxn().getFeptxnTxTime()) <= 0) {
                    rtnCode = ATMReturnCode.NWDSeqNotFound; //無此無卡提款序號
                    getLogContext().setRemark("無預約資料");
                    logMessage(Level.INFO, getLogContext());
                } else {
                    // 抓取前一天的資料
                    defNWDREG.setNwdregTxDate((Integer.parseInt(getFeptxn().getFeptxnTxDate().substring(0, 4)) - 1) + "/" + getFeptxn().getFeptxnTxDate().substring(4, 6) + "/"
                            + getFeptxn().getFeptxnTxDate().substring(6, 8));
                    // defNWDREG.NWDREG_TX_DATE = DateAdd(DateInterval.Day, -1, CDate(FepTxn.FEPTXN_TX_DATE)).ToString("yyyyMMDD")
                    defNWDREG.setNwdregNwdseq(getFeptxn().getFeptxnMajorActno());
                    nwdreg = nwdregMapper.selectByPrimaryKey(defNWDREG.getNwdregTxDate(), defNWDREG.getNwdregNwdseq());
                    if (nwdreg == null) {
                        rtnCode = ATMReturnCode.NWDSeqNotFound;
                        getLogContext().setRemark("往前一天查無預約資料");
                        logMessage(Level.INFO, getLogContext());
                    }
                }

                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            // 比對預約狀態
            if (!"0".equals(nwdreg.getNwdregTxrust())) {
                rtnCode = ATMReturnCode.NWDSeqNotMatch;
                //提款序號(已提款/已失效/已取消)
                getLogContext().setRemark("預約狀態不符, NWDREG_TXRUST=" + nwdreg.getNwdregTxrust());
                logMessage(Level.INFO, getLogContext());
            }

            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            // 比對交易時間是否超過失效日期時間
            if ((getFeptxn().getFeptxnTxDate() + " " + getFeptxn().getFeptxnTxTime()).compareTo(nwdreg.getNwdregLosDt()) > 0) {
                rtnCode = ATMReturnCode.NCStatusNotMatch;
                getLogContext().setRemark("超過有效提款時間, 交易時間=" + getFeptxn().getFeptxnTxDate() + " " + getFeptxn().getFeptxnTxTime() + ", 失效時間" + nwdreg.getNwdregLosDt());
                //提款序號(已提款/已失效/已取消)
                logMessage(Level.INFO, getLogContext());
                nwdreg.setNwdregTxDate(getFeptxn().getFeptxnTxDate());
                nwdreg.setNwdregNwdseq(getFeptxn().getFeptxnMajorActno());
                nwdreg.setNwdregTxrust("T"); // 已失效
                nwdregMapper.updateByPrimaryKeySelective(nwdreg);
            }

            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            // 2018/11/29 修改, 檢核無卡提款預約檔幣別
            if (!getFeptxn().getFeptxnTxCur().equals(nwdreg.getNwdregTxCur())) {
                rtnCode = ATMReturnCode.NWDSeqError; //E814
                getLogContext().setRemark("提款幣別與預約幣別不符, 提款幣別=" + getFeptxn().getFeptxnTxCur() + ", 預約幣別=" + nwdreg.getNwdregTxCur());
                logMessage(Level.INFO, getLogContext());
                return rtnCode;
            }

            //2018/10/17 修改for卡台/外幣提款, 檢核金額是否超過單筆限額
            rtnCode = checkTransLimit(getGeneralData().getMsgCtl());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            getFeptxn().setFeptxnTroutActno(nwdreg.getNwdregTroutActno());
            getFeptxn().setFeptxnIdno(nwdreg.getNwdregIdno());
            getFeptxn().setFeptxnDueDate(nwdreg.getNwdregTxDate()); // 借用FEPTXN_DUE_DATE欄位存放預約日期

            // 3. 將無卡提款預約資料寫入NWDTXN
            getFeptxn().setFeptxnDueDate(nwdreg.getNwdregTxDate());
            //借用DUE_DATE 欄位, 存放預約日期
            //將無卡提款預約資料寫入NWDTXN
            nwdtxn.get().setNwdtxnRegDate(nwdreg.getNwdregTxDate());
            nwdtxn.get().setNwdtxnRegAmt(nwdreg.getNwdregTxAmt());
            nwdtxn.get().setNwdtxnNwdseq(nwdreg.getNwdregNwdseq());
            nwdtxn.get().setNwdtxnRegDt(nwdreg.getNwdregRegDt());
            nwdtxn.get().setNwdtxnLosDt(nwdreg.getNwdregLosDt());
            //2020/7/2 將預約 CHANNEL 寫入NWDTXN
            nwdtxn.get().setNwdtxnRegChannel(nwdreg.getNwdregChannel());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkNCNWDData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核無卡提款狀態
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>add for 無卡提款</reason>
     * <date>2017/02/09</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkNCCardStatus() {
        Card defCARD = new Card();
        try {
            defCARD.setCardActno(getFeptxn().getFeptxnTroutActno());
            defCARD.setCardNcstatus((short) 1);
            defCARD.setCardNcstatusB((short) 1);
            if (ATMTXCD.NCS.name().equals(getFeptxn().getFeptxnTxCode())
                    && "1".equals(getFeptxn().getFeptxnNoticeId())) {
                List<Card> cardList = cardExtMapper.getNcCardByStatus(defCARD.getCardActno(), defCARD.getCardNcstatus());
                if (CollectionUtils.isNotEmpty(cardList)) {
                    getLogContext().setRemark("已設定過無卡提款(CARD_NCSTATUS=1)");
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.NCStatusNotMatch;
                }
            } else {
                defCARD = cardExtMapper.queryNcCardByStatus(defCARD.getCardActno(), defCARD.getCardNcstatus());
                if (defCARD == null) {
                    getLogContext().setRemark("無卡提款狀態不符(找不到Card資料)");
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.NCStatusNotMatch;
                } else {
                    // Fly 2019/04/12 For企業戶無卡提款 增加核查IDNO是否和卡檔一致
                    if (ATMTXCD.NWA.name().equals(getFeptxn().getFeptxnTxCode())) {
                        if (!(defCARD.getCardIdno1().trim() + defCARD.getCardIdno2()).equals(getFeptxn().getFeptxnIdno())) {
                            getLogContext().setRemark("身份證或統一編號與卡檔不符");
                            logMessage(Level.INFO, getLogContext());
                            return ATMReturnCode.IDError;
                        }
                    }

                    // 檢核是否有無卡提款KEYKIND
                    if (StringUtils.isBlank(defCARD.getCardNckeykind())) {
                        getLogContext().setRemark("CARD_NCKEYKIND為空白或NULL");
                        logMessage(Level.INFO, getLogContext());
                        return ATMReturnCode.OtherCheckError;
                    }
                    getFeptxn().setFeptxnCardSeq(defCARD.getCardCardSeq());
                    getFeptxn().setFeptxnZoneCode(defCARD.getCardZoneCode());
                }
            }
            if (ATMTXCD.NWA.name().equals(getFeptxn().getFeptxnTxCode())) {
                this.card = defCARD;
                // Fly 2019/01/25 企業戶無卡提款設定 NWA.APPLYTYPE 借放FEPTXN_NOTICE_ID
                if ("1".equals(getFeptxn().getFeptxnNoticeId())) {
                    if (defCARD.getCardNcstatusB() == 1) {
                        getLogContext().setRemark("已設定過企業戶無卡提款(CARD_NCSTATUS_B=1)");
                        logMessage(Level.INFO, getLogContext());
                        return ATMReturnCode.NCStatusNotMatch;
                    }
                } else if ("2".equals(getFeptxn().getFeptxnNoticeId())) {
                    if (defCARD.getCardNcstatusB() != 1) {
                        getLogContext().setRemark("已解除企業戶無卡提款或NCSTATUS_B錯誤");
                        logMessage(Level.INFO, getLogContext());
                        return ATMReturnCode.NCStatusNotMatch;
                    }
                } else {
                    getLogContext().setRemark("企業戶無卡提款設定記號錯誤");
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.ApplyTypeError;
                }
            }

            // 2019/11/19 Modify by Ruling for 卡片狀態為掛失或註銷，回覆前端錯誤訊息調整及Log加遮蔽
            // 2.無卡提款交易檢核卡片狀態
            if (FISCPCode.PCode2510.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || (ATMTXCD.NWR.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.NFE.name().equals(getFeptxn().getFeptxnTxCode()))) {
                if (defCARD.getCardStatus() > 4) {
                    getLogContext().setRemark("失效卡片(CARD_STATUS>4), CARD_ACTNO=" + defCARD.getCardActno().substring(0, 5) + "XXXXX" + defCARD.getCardActno().substring(10) + " CARD_SEQ="
                            + defCARD.getCardCardSeq() + " CARD_STATUS=" + defCARD.getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.CardLoseEfficacy;
                }
                if (defCARD.getCardStatus() < 4) {
                    // If defCARD.CARD_STATUS <> 4 Then
                    getLogContext().setRemark("卡片尚未生效(CARD_STATUS<4), CARD_ACTNO=" + defCARD.getCardActno().substring(0, 5) + "XXXXX" + defCARD.getCardActno().substring(10) + " CARD_SEQ="
                            + defCARD.getCardCardSeq() + " CARD_STATUS=" + defCARD.getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.CardNotEffective;
                }

                if ("1".equals(defCARD.getCardRscard())) {
                    getLogContext().setRemark(
                            "卡片被吃卡(CARD_RSCARD=1), CARD_ACTNO=" + defCARD.getCardActno().substring(0, 5) + "XXXXX" + defCARD.getCardActno().substring(10) + " CARD_SEQ=" + defCARD.getCardCardSeq());
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.CardNotEffective;
                }

                if (StringUtils.isBlank(defCARD.getCardAuth())) {
                    getLogContext().setRemark("無卡提款認證碼未設定(CARD_AUTH)為NULL或空白");
                    logMessage(Level.INFO, getLogContext());
                    return ATMReturnCode.CardNotEffective;
                }

                // Fly 2018/10/09 For 無卡外幣提款
                if (FISCPCode.PCode2510.getValueStr().equals(getFeptxn().getFeptxnPcode())
                        || ATMTXCD.NFE.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 會回塞給BUSINESS內的CARD物件，AA可以直接取不用傳參數
                    if (this.card == null) {
                        this.card = defCARD;
                    }
                }
            }

            if (!ZoneCode.TWN.equals(getFeptxn().getFeptxnZoneCode())) {
                getLogContext().setRemark("非台灣卡不得執行無卡提款設定, CARD_ZONE_CODE=" + defCARD.getCardZoneCode());
                logMessage(Level.INFO, getLogContext());
                return ATMReturnCode.CardNotEffective;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkNCCardStatus");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return null;
    }

    /**
     * 更新無卡提款預約狀態
     *
     * @return
     */
    public FEPReturnCode updateNWDReg() {
        Nwdreg defNWDREG = new Nwdreg();
        NwdregMapper dbNWDREG = SpringBeanFactoryUtil.getBean(NwdregMapper.class);

        try {
            defNWDREG.setNwdregTxDate(getFeptxn().getFeptxnDueDate());
            defNWDREG.setNwdregNwdseq(getFeptxn().getFeptxnMajorActno());

            // Fly 2018/10/09 For 外幣無卡提款
            defNWDREG.setNwdregExrate(getFeptxn().getFeptxnExrate());
            defNWDREG.setNwdregScash(getFeptxn().getFeptxnScash());
            defNWDREG.setNwdregDiscrate(getFeptxn().getFeptxnExrate());
            defNWDREG.setNwdregTxCurAct(getFeptxn().getFeptxnTxCurAct());
            defNWDREG.setNwdregTxAmtAct(getFeptxn().getFeptxnTxAmtAct());

            // 2018/03/15 Modify by Ruling for 跨行無卡提款:增加寫入交易啟動銀行代號
            defNWDREG.setNwdregBkno(getFeptxn().getFeptxnBkno());

            // 2018/03/15 Modify by Ruling for 跨行無卡提款:密碼錯5次才能鎖預約資料，原存交易需用財回的REP_RC做判斷
            if (("C901".equals(getFeptxn().getFeptxnReplyCode().trim()) || "E814".equals(getFeptxn().getFeptxnReplyCode().trim()))
                    || ("4301".equals(getFeptxn().getFeptxnRepRc().trim()) || "4707".equals(getFeptxn().getFeptxnRepRc().trim()))) {
                defNWDREG.setNwdregTxrust("0");
            } else {
                if ("0000".equals(getFeptxn().getFeptxnCbsRc().trim())) {
                    defNWDREG.setNwdregTxrust(getFeptxn().getFeptxnTxrust());
                } else if (StringUtils.isNotBlank(getFeptxn().getFeptxnCbsRc())) {
                    defNWDREG.setNwdregTxrust("T");
                } else {
                    defNWDREG.setNwdregTxrust(getFeptxn().getFeptxnTxrust());
                }
                if ("C902".equals(getFeptxn().getFeptxnReplyCode()) || "4303".equals(getFeptxn().getFeptxnRepRc())) {
                    defNWDREG.setNwdregTxrust("T");
                }
                defNWDREG.setNwdregNwdDate(getFeptxn().getFeptxnTxDate());
                defNWDREG.setNwdregEjfno(getFeptxn().getFeptxnEjfno());
                defNWDREG.setNwdregAtmno(getFeptxn().getFeptxnAtmno());
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnStan())) {
                    defNWDREG.setNwdregStan(getFeptxn().getFeptxnStan());
                }
                defNWDREG.setNwdregTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
                defNWDREG.setNwdregCbsRrn(getFeptxn().getFeptxnCbsRrn());
            }

            if (dbNWDREG.updateByPrimaryKeySelective(defNWDREG) > 0) {
                return CommonReturnCode.Normal;
            } else {
                return IOReturnCode.UpdateFail;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateNWDReg");
            sendEMS(getLogContext());
            return IOReturnCode.FEPTXNUpdateError;
        }
    }

    /**
     * 檢核卡片輸入密碼錯誤次數
     *
     * @param isPWPassed <history>
     *                   <modify>
     *                   <modifier>Kyo</modifier>
     *                   <reason>Function Design</reason>
     *                   <date>2009/12/16</date>
     *                   </modify>
     *                   </history>
     */
    public FEPReturnCode checkPWErrCnt(int isPWPassed) {
        FEPReturnCode rtn = FEPReturnCode.Normal;
        try {
            if (isPWPassed == 1) {// (密碼錯誤)
                if (getCard().getCardRetryCnt() > 0) {
                    getCard().setCardRetryCnt((short) (getCard().getCardRetryCnt().intValue() - 1));
                    rtn = ENCReturnCode.ENCCheckPasswordError; // 密碼錯誤
                } else {
                    getCard().setCardRscard("1");
                    // 2015/03/30 Modify by Ruling for 客戶密碼輸錯3次，更新吃卡日期及時間
                    getCard().setCardRcardDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                    getCard().setCardRcardTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
                    rtn = ATMReturnCode.OverPasswordErrorCount; // 密碼錯誤已達四次
                }
                cardExtMapper.updateByPrimaryKeySelective(getCard());
            } else {// 密碼正確
                if (getCard().getCardRetryCnt() < (byte) ATMPConfig.getInstance().getCardRetryCount()) {
                    getCard().setCardRetryCnt((short) ATMPConfig.getInstance().getCardRetryCount()); // 密碼錯誤重試次數改為(4)
                }
                if (cardExtMapper.updateByPrimaryKeySelective(getCard()) < 1) {
                    rtn = IOReturnCode.CARDUpdateError;
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkPWErrCnt");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtn;
    }

    /**
     * 檢核卡片狀態
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>Function design</reason>
     * <date>2009/12/14</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>
     * BugReport
     * 001B0096:APP電文發生AA_RC=3121 Reply_code=E984
     * </reason>
     * <date>2009/12/14</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>modiefied by kyo for DB Schema modify</reason>
     * <date>2010/05/07</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>同區交易時才搬移交易金額</reason>
     * <date>2010/05/12</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>BugReport(001B0463):海外分行同區交易(不含提款)，以帳戶幣別寫入 ATMC，必須在上主機之前修改</reason>
     * <date>2010/05/13</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改判斷邏輯：直接指定TX_CODE=海外分行同區交易(查詢(IIQ/IQ2)及轉帳(IFT) </reason>
     * <date>2010/05/14</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>spec modified:修改開卡失敗的BUG</reason>
     * <date>2010/05/21</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改跨區手續費由SYSCONF取值</reason>
     * <date>2010/05/22</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>BugReport(001B0518):進行交易時，應檢核CARD_TFR_FLAG、APP_GP、SELF等欄位</reason>
     * <date>2010/05/24</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>BugReport(001B0525):香港ATM+澳門卡進行交易(IIQ/IQ2)，在FEPTXN_TX_CUR被記錄為TWD，在ATMC_CUR也被記錄成TWD。</reason>
     * <date>2010/05/24</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改:將取得手續費幣別邏輯改到PrepareFEPTxn</reason>
     * <date>2010/05/31</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改:將邏輯搬到selfissue做</reason>
     * <date>2010/06/02</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for SPEC新增邏輯</reason>
     * <date>2010/06/09</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>修改例外錯誤，取卡檔前先判斷是否為NOTHING，與取最大序號問題修改</reason>
     * <date>2010/06/10</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>用信用卡號取最大卡片序號的資料</reason>
     * <date>2010/06/11</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>1.TODO事項完成:E355 rtncode列舉
     * 2.最後更新卡檔時不需再給PK欄位值</reason>
     * <date>2010/06/24</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>多餘步驟移除，不需要在檢核改搬移手續費</reason>
     * <date>2010/07/02</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for FEPTXN_IC_SEQNO為Char,CARD_ICTXSEQ為Int。因此應該改變Char的型態，或是CARD_ICTXSEQ要左補零</reason>
     * <date>2010/07/15</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改: 7/20 修改港澳卡於台灣跨區提款交易</reason>
     * <date>2010/07/20</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>SPEC修改: NCR故障，手續費幣別皆帶台幣，所以需要重搬FEPTXN_FEE_CUR</reason>
     * <date>2010/07/26</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for spec修改: 7/30 點掉 PNM 檢核, 移至 CashAdvance</reason>
     * <date>2010/07/30</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>for spec修改:更新卡檔的時機與欄位;修改BUG</reason>
     * <date>2011/03/15</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>修改讓hostbusiness也可以存取到卡的資料</reason>
     * <date>2011/03/29</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkCardStatus() {
        FEPReturnCode rtn = CommonReturnCode.Normal;
        Sysconf defSYSCONF = null;

        try {
            // 信用卡或GIFT卡,不須檢核卡片檔
            if (BINPROD.Credit.equals(getFeptxn().getFeptxnTrinKind()) || BINPROD.Gift.equals(getFeptxn().getFeptxnTrinKind())) {
                return rtn;
            }

            // 2012/08/10 Modify by Ruling for 企業入金，不須檢核卡片檔
            // 2012/09/07 Modify by Ruling for 新增硬幣機的業務:CCR不須檢核卡檔
            // 無卡存款/企業入金/無卡調帳號/無卡硬幣存款，不須檢核卡片檔
            if (ATMTXCD.CDR.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.G51.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.BDR.name().equals(getFeptxn().getFeptxnTxCode())
                    || ATMTXCD.CCR.name().equals(getFeptxn().getFeptxnTxCode())) {
                return rtn;
            }

            // 2010-10-19 by kyo for spec update /* 10/19自行交易之 Combo 卡才進行以下處理 Update By Connie*/
            // 2011/10/11 Modify by Ruling 自行/跨行Combo卡交易判斷方式相同
            // If .FEPTXN_TROUT_KIND = BINPROD.Combo AndAlso Not .FEPTXN_FISC_FLAG Then
            // 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊：比照Combo卡查卡檔
            if (BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind()) || BINPROD.Debit.equals(getFeptxn().getFeptxnTroutKind())) {
                // 2010-06-10 by kyo for 新增判斷是否已經NEW卡物件
                if (getCard() == null) {
                    setCard(new Card());
                }

                // 2013/01/03 Modify by Ruling for Combo 卡交易(IWD/IFT)
                if (ATMTXCD.IWD.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode())) {
                    if (StringUtils.isBlank(getFeptxn().getFeptxnIcmark())) {
                        rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                        getLogContext().setRemark("FEPTXN_ICMARK IsNullOrWhiteSpace, FEPTXN_ICMARK = " + getFeptxn().getFeptxnIcmark().trim());
                        logMessage(Level.INFO, getLogContext());
                        return rtn;
                    }
                    getFeptxn().setFeptxnCardSeq(Short.valueOf(getFeptxn().getFeptxnIcmark().substring(14, 16)));
                    getCard().setCardActno(getFeptxn().getFeptxnMajorActno());
                    getCard().setCardCardSeq(getFeptxn().getFeptxnCardSeq());
                    setCard(cardExtMapper.selectByPrimaryKey(getCard().getCardActno(), getCard().getCardCardSeq()));
                    if (getCard() == null) {
                        rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                        getLogContext().setRemark("找不到卡檔, Card.CARD_ACTNO = " + getFeptxn().getFeptxnMajorActno() + ", Card.CARD_CARD_SEQ = " + getFeptxn().getFeptxnCardSeq().toString());
                        logMessage(Level.INFO, getLogContext());
                        return rtn;
                    }

                    if (!getCard().getCardCreditno().equals(getFeptxn().getFeptxnTroutActno())) {
                        rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                        getLogContext().setRemark("信用卡號 <> 轉出帳號, Card.CARD_CREDITNO = " + getCard().getCardCreditno() + ", FEPTXN_TROUT_ACTNO = " + getFeptxn().getFeptxnTroutActno());
                        logMessage(Level.INFO, getLogContext());
                        return rtn;
                    }
                } else {
                    getCard().setCardCreditno(getFeptxn().getFeptxnTroutActno());
                    // 2010-06-10 by kyo for 取最大序號問題修改
                    // 2010-06-11 by kyo for 用信用卡號取最大卡片序號的資料
                    setCard(cardExtMapper.queryByCreditNoWithMaxCardSeq(getCard().getCardCreditno()));
                    if (getCard() == null) {
                        // 2012/09/26 Modify by Ruling for 國際卡交易回財金4414
                        rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                        getLogContext().setRemark("找不到最大卡片序號, Card.CARD_CREDITNO = " + getFeptxn().getFeptxnTroutActno());
                        logMessage(Level.INFO, getLogContext());
                        // rtn = ATMReturnCode.ComboCardStatusNotMatch '金融信用卡狀態不合，無法交易
                        return rtn;
                    }
                }

                // modified By Maxine for 12/22 修改 for 國際卡交易
                // 2011/11/28 Modify By Ruling for 國際卡交易將Combo卡國際卡提款的卡號及序號寫入FEPTXN
                // 2013/04/08 Modify by Ruling for 國際卡交易檢核是否為COMBO卡年度換卡
                // 2016/04/20 Modify by Ruling for EMV晶片卡2630原存交易
                // If .FEPTXN_FISC_FLAG = True Then
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && ("24".equals(getFeptxn().getFeptxnPcode().substring(0, 2)) || "26".equals(getFeptxn().getFeptxnPcode().substring(0, 2)))) {
                    // 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊
                    if (BINPROD.Debit.equals(getFeptxn().getFeptxnTroutKind())) {
                        // DEBIT卡檢核卡片狀態
                        getFeptxn().setFeptxnTroutActno(getCard().getCardActno());
                        getFeptxn().setFeptxnCardSeq(getCard().getCardCardSeq());
                        // 檢核卡片狀態
                        if (getCard().getCardStatus() == ATMCardStatus.Lose.getValue() || getCard().getCardStatus() == ATMCardStatus.LoseWithoutStart.getValue()) {
                            rtn = ATMReturnCode.CardLost; // 卡片已掛失
                            getLogContext().setRemark("卡片已掛失, Card.CARD_STATUS = " + getCard().getCardStatus().toString());
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get();
                            return rtn;
                        }
                        // 檢核國際卡交易
                        if ("0".equals(getCard().getCardAppGp())) {
                            rtn = ATMReturnCode.PlusCirrusNotApply; // 尚未申請國際卡功能
                            getLogContext().setRemark("尚未申請國際卡功能, Card.CARD_APP_GP = " + getCard().getCardAppGp());
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        }
                        // 檢核DEBIT卡效期
                        if (StringUtils.isBlank(getCard().getCardCrEndMmyy()) || getCard().getCardCrEndMmyy().trim().length() != 4) {
                            rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                            getLogContext().setRemark("DEBIT卡效期(CARD_CR_END_MMYY)為NULL或空白或長度不足4位");
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }
                        if (getCard().getCardCrEndMmyy().substring(0, 2).compareTo("01") < 0 || getCard().getCardCrEndMmyy().substring(0, 2).compareTo("12") > 0) {
                            rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                            getLogContext().setRemark("DEBIT卡效期月份不合理, Card.CARD_CR_END_MMYY = " + getCard().getCardCrEndMmyy().trim());
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }
                        String validYYMM = ""; // DEBIT卡效期由MMYY改為YYMM
                        validYYMM = getCard().getCardCrEndMmyy().trim().substring(2, 4) + getCard().getCardCrEndMmyy().trim().substring(0, 2);
                        if (getFeptxn().getFeptxnTxDate().substring(2, 6).compareTo(validYYMM) > 0) {
                            rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用
                            getLogContext().setRemark("交易日大於DEBIT卡效期, Card.CARD_CR_END_MMYY = " + getCard().getCardCrEndMmyy());
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }
                    } else {
                        // COMBO卡流程
                        if (getCard().getCardStatus() == ATMCardStatus.Create.getValue() && getCard().getCardCombine() == ATMCardCombine.SendOut.getValue()
                                && (getCard().getCardAppkind() == 2 || getCard().getCardAppkind() == 3)) {
                            /// *COMBO卡年度換卡*/
                            // 取得前張卡片的卡檔資料
                            Card oCard = new Card();
                            oCard.setCardCreditno(getFeptxn().getFeptxnTroutActno());
                            oCard.setCardCardSeq(getCard().getCardCardSeq());
                            Card card = cardExtMapper.getOldCardByMaxCardSeq(oCard);
                            if (card == null) {
                                rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                                getLogContext().setRemark("COMBO卡年度換卡-找不到舊卡, oCard.CARD_CREDITNO =" + oCard.getCardCreditno() + ", oCard.CARD_CARD_SEQ =" + oCard.getCardCardSeq());
                                logMessage(Level.INFO, getLogContext());
                                return rtn;
                            }
                            oCard = card;
                            getFeptxn().setFeptxnTroutActno(oCard.getCardActno());
                            getFeptxn().setFeptxnCardSeq(Short.valueOf(oCard.getCardSeqLos()));

                            // 檢核前張卡片是否吃卡
                            if ("1".equals(oCard.getCardRscard())) {
                                rtn = ATMReturnCode.CardLoseEfficacy; // 卡片失效
                                getLogContext().setRemark("COMBO卡年度換卡-舊卡的卡片失效, oCard.CARD_RSCARD = " + oCard.getCardRscard());
                                logMessage(Level.INFO, getLogContext());
                                return rtn;
                            }

                            // 檢核前張卡片狀態
                            if (oCard.getCardStatus() != ATMCardStatus.Start.getValue()) {
                                rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                                getLogContext().setRemark("COMBO卡年度換卡-舊卡的卡片狀態 <> 4, oldCard.CARD_STATUS = " + oCard.getCardStatus().toString());
                                logMessage(Level.INFO, getLogContext());
                                return rtn;
                            }

                            // 檢核前張卡片是否申請國際卡功能
                            if ("0".equals(oCard.getCardAppGp())) {
                                rtn = ATMReturnCode.PlusCirrusNotApply; // 尚未申請國際卡功能
                                getLogContext().setRemark("COMBO卡年度換卡-舊卡尚未申請國際卡功能, oCard.CARD_APP_GP = " + oCard.getCardAppGp());
                                logMessage(Level.INFO, getLogContext());
                                return rtn;
                            }

                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        } else {
                            /// *非COMBO卡年度換卡*/
                            getFeptxn().setFeptxnTroutActno(getCard().getCardActno());
                            getFeptxn().setFeptxnCardSeq(getCard().getCardCardSeq());
                            // 2012/09/26 Modify by Ruling for 國際卡交易回財金4412
                            if (getCard().getCardStatus() == ATMCardStatus.Lose.getValue() || getCard().getCardStatus() == ATMCardStatus.LoseWithoutStart.getValue()) {
                                rtn = ATMReturnCode.CardLost; // 卡片已掛失
                                getLogContext().setRemark("卡片已掛失, ACTNO = " + getFeptxn().getFeptxnTroutActno() + ", SEQ = " + getFeptxn().getFeptxnCardSeq());
                                logMessage(Level.INFO, getLogContext());
                                return rtn;
                            }
                            // 3/6 修改, 國際卡交易檢核 */
                            getLogContext().setChact(getFeptxn().getFeptxnTroutActno());
                            getLogContext().setRemark("Card.CARD_APP_GP =" + getCard().getCardAppGp());
                            logMessage(Level.INFO, getLogContext());
                            if ("0".equals(getCard().getCardAppGp())) {
                                rtn = ATMReturnCode.PlusCirrusNotApply; // 尚未申請國際卡功能
                                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                updateCard(getCard(), codeRefBase); // Update Card
                                rtn = codeRefBase.get(); // Update Card
                                return rtn;
                            }
                        }
                    }
                }

                // 檢核是否吃卡
                if ("1".equals(getCard().getCardRscard())) {
                    rtn = ATMReturnCode.CardLoseEfficacy; // 卡片失效
                    getLogContext().setRemark("卡片失效, Card.CARD_RSCARD = " + getCard().getCardRscard());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 檢核COMBO卡片狀態 */
                // 2019/11/19 Modify by Ruling for 卡片狀態為掛失或註銷，回覆前端錯誤訊息調整
                if (getCard().getCardStatus() > ATMCardStatus.Start.getValue()) {
                    rtn = ATMReturnCode.CardLoseEfficacy; // 失效卡片
                    getLogContext().setRemark("卡片狀態 > 4, Card.CARD_STATUS = " + getCard().getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                if (getCard().getCardStatus() < ATMCardStatus.Start.getValue()) {
                    // If Card.CARD_STATUS <> CInt(ATMCardStatus.Start) Then
                    rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                    getLogContext().setRemark("卡片狀態 < 4, Card.CARD_STATUS = " + getCard().getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 1/28 修改, COMBO卡檢核二卡合一狀態 */
                // 2018-05-22 Modify by Ruling for MASTER DEBIT加悠遊：DEBIT卡不需檢核二卡合一狀態
                if (BINPROD.Combo.equals(getFeptxn().getFeptxnTroutKind())) {
                    if (!ATMTXCD.PNC.name().equals(getFeptxn().getFeptxnTxCode())) {
                        // 檢核前卡新卡二卡合一狀態, 必須為 5 or 6 */
                        if (getCard().getCardCombine() != ATMCardCombine.SendOut.getValue() && getCard().getCardCombine() != ATMCardCombine.CombineStart.getValue()) {
                            rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                            getLogContext().setRemark("二卡合一狀態 <> 5 or 6, Card.CARD_COMBINE = " + getCard().getCardCombine());
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        }
                    }
                }

            } else {
                // 一般金融卡 */
                RefBase<Feptxn> refFeptxn = new RefBase<>(getFeptxn());
                setCard(getCardByPK(refFeptxn)); // 取得卡片資料
                setFeptxn(refFeptxn.get());
                if (getCard() == null) {
                    // 2011-07-12 by kyo for 修改錯誤代碼
                    rtn = ATMReturnCode.ICCardNotFound; // 找不到卡檔 */
                    return rtn;
                }
                getLogContext().setChact(getCard().getCardActno());

                // 2011-03-30 by kyo for 若找無卡檔也要存卡片序號，修正BUG/搬到GetCardByPK存值
                // .FEPTXN_CARD_SEQ = Card.CARD_CARD_SEQ
                // 2010-05-20 補上CODING 遺漏的欄位搬移
                getFeptxn().setFeptxnTxCurAct(getCard().getCardCur()); // 主帳號幣別
                getFeptxn().setFeptxnZoneCode(getCard().getCardZoneCode()); // 卡片所在地
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutKind()) && StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2()) && StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2().trim())
                        && DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                    getFeptxn().setFeptxnTroutActno(getCard().getCardActno());
                }

                // 2010-04-15 modified by kyo for SPEC 新增
                if (StringUtils.isBlank(getFeptxn().getFeptxnTxCurAct())) {
                    getFeptxn().setFeptxnTxCurAct(String.valueOf(CurrencyType.TWD.name())); // 帳戶交易金額
                }

                // 海外卡交易連線狀態判斷 */
                if (ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode()) || ATMTXCD.IFW.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 判斷海外地區交易連線狀態 */
                    if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnZoneCode())) {// 判斷澳門交易狀態 */
// 2024-03-06 Richard modified for SYSSTATE 調整
//                        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatMoIssue())) {
//                            // 澳門自行暫停服務 */
//                            return CommonReturnCode.IntraBankServiceStop;
//                        }
                    } else if (ATMZone.HKG.name().equals(getFeptxn().getFeptxnZoneCode())) {// 判斷香港交易狀態 */
// 2024-03-06 Richard modified for SYSSTATE 調整
//                        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatHkIssue())) {
//                            // 香港自行暫停服務 */
//                            return CommonReturnCode.IntraBankServiceStop;
//                        }
                    }
                }

                // 檢核是否吃卡
                if ("1".equals(getCard().getCardRscard())) {
                    rtn = ATMReturnCode.CardNotEffective; // 卡片失效
                    getLogContext().setRemark("卡片失效, Card.CARD_RSCARD = " + getCard().getCardRscard());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 1/28 修改: 檢核晶片卡記號 */
                // 2011-07-12 by kyo for 修改錯誤代碼
                // 2012/06/01 Modify by Ruling for 國際卡提款/餘額查詢交易，不檢核晶片卡申請記號(CARD_IC)
                // 2012/09/26 Modify by Ruling for 國際卡交易回財金4412
                // 2016/04/20 Modify by Ruling for EMV晶片卡2630原存交易
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && ("24".equals(getFeptxn().getFeptxnPcode().substring(0, 2)) || "26".equals(getFeptxn().getFeptxnPcode().substring(0, 2)))) {
                    // 國際卡交易
                    if (getCard().getCardStatus() == ATMCardStatus.Lose.getValue() || getCard().getCardStatus() == ATMCardStatus.LoseWithoutStart.getValue()) {
                        rtn = ATMReturnCode.CardLost; // 卡片已掛失
                        getLogContext().setRemark("卡片已掛失, ACTNO = " + getFeptxn().getFeptxnTroutActno() + "SEQ" + getFeptxn().getFeptxnCardSeq());
                        logMessage(Level.INFO, getLogContext());
                        return rtn;
                    }

                    // 2012/11/13 Modify by Ruling for 香港晶片卡:香港卡國際卡交易，判斷香港地區本營業日是否在國際卡有效期
                    // 判斷卡別必須為香港PLUS卡
                    if (ZoneCode.HKG.equals(getFeptxn().getFeptxnZoneCode())) {
                        if (getCard().getCardType() != 41) {
                            rtn = ATMReturnCode.NotICCard;
                            getLogContext().setRemark("Card.CARD_TYPE <> 41, Card.CARD_TYPE = " + getCard().getCardType());
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }

                        // 判斷香港地區本營業日是否超過國際卡有效期限
                        Zone defZONE = new Zone();
                        defZONE = getZoneByZoneCode(ZoneCode.HKG);
                        if (defZONE == null) {
                            return IOReturnCode.ZONENotFound;
                        }
                        if (Integer.parseInt(defZONE.getZoneTbsdy()) < Integer.parseInt(getCard().getCardTxDateB())
                                || Integer.parseInt(defZONE.getZoneTbsdy()) > Integer.parseInt(getCard().getCardTxDateE())) {
                            rtn = ATMReturnCode.ComboCardNotEffective;
                            getLogContext().setRemark("香港地區本營業日超過國際卡有效期限, Card.CARD_TX_DATE_B = " + getCard().getCardTxDateB() + ", Card.CARD_TX_DATE_E = " + getCard().getCardTxDateE());
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }
                    }

                    // 2013/11/18 Modify by ChenLi for 澳門晶片卡:國際卡交易，判斷澳門地區本營業日是否超過國際卡有效期限
                    if (ZoneCode.MAC.equals(getFeptxn().getFeptxnZoneCode())) {
                        // 判斷卡別必須為澳門PLUS卡
                        if (getCard().getCardType() != 42) {
                            rtn = ATMReturnCode.NotICCard;
                            getLogContext().setRemark("Card.Card_Type <> 42, Card.CARD_TYPE = " + getCard().getCardType());
                            logMessage(Level.INFO, getLogContext());
                            return rtn;
                        }

                        // 判斷澳門地區本營業日是否超過國際卡有效期限
                        Zone defZone = new Zone();
                        defZone = getZoneByZoneCode(ZoneCode.MAC);
                        if (defZone == null) {
                            return IOReturnCode.ZONENotFound;
                        }
                        // 2014/04/29 Modify by ChenLi for SPEC修改:判斷交易日期>=SYSCONF設定澳門209拒絶交易之生效日
                        defSYSCONF = new Sysconf();
                        defSYSCONF.setSysconfSubsysno((short) 1);
                        defSYSCONF.setSysconfName("MO209StopDate");
                        Sysconf sysconf = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
                        if (sysconf != null) {
                            if (Integer.parseInt(getFeptxn().getFeptxnTxDate()) >= Integer.parseInt(sysconf.getSysconfValue())) {
                                if (Integer.parseInt(defZone.getZoneTbsdy()) < Integer.parseInt(getCard().getCardTxDateB())
                                        || Integer.parseInt(defZone.getZoneTbsdy()) > Integer.parseInt(getCard().getCardTxDateE())) {
                                    rtn = ATMReturnCode.ComboCardNotEffective;
                                    getLogContext().setRemark("澳門209拒絕交易之生效日=" + sysconf.getSysconfValue() + "。澳門地區本營業日超過國際卡有效期限, Card.CARD_TX_DATE_B = " + getCard().getCardTxDateB()
                                            + "Card.CARD_TX_DATE_E = " + getCard().getCardTxDateE());
                                    logMessage(Level.INFO, getLogContext());
                                    return rtn;
                                }
                            }
                        } else {
                            getLogContext().setRemark("CheckCardStatus-SYSCONF找不到澳門209拒絶交易之生效日, SYSCONF_NAME=" + defSYSCONF.getSysconfName());
                            logMessage(Level.INFO, getLogContext());
                            return IOReturnCode.SYSCONFNotFound;
                        }
                    }
                } else {
                    // 非國際卡交易
                    if (getCard().getCardIc() != 1) {
                        rtn = ATMReturnCode.NotICCard; // 非晶片卡 */
                        getLogContext().setRemark("Card.CARD_IC <> 1, Card.CARD_IC = " + getCard().getCardIc());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                }

                // 2011-05-05 by kyo for 開卡流程新電文
                if (ATMTXCD.PNM.name().equals(getFeptxn().getFeptxnTxCode()) || ATMTXCD.PNX.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 2010-05-21 modified by kyo for spec modified:此邏輯造成開卡失敗，開卡時新卡status一定要是2 or 3 否則都錯
                    // Fly 2015/09/15 PNX新需求 如卡片狀態為啟用，交易仍可繼續進行
                    if (getCard().getCardStatus() != ATMCardStatus.Receive.getValue() && getCard().getCardStatus() != ATMCardStatus.Create.getValue()
                            && getCard().getCardStatus() != ATMCardStatus.Start.getValue()) {
                        // If Card.CARD_STATUS <> CInt(ATMCardStatus.Receive) AndAlso Card.CARD_STATUS <> CInt(ATMCardStatus.Create) Then
                        rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                        getLogContext().setRemark("卡片狀態 <> 2 or 3 or 4, Card.CARD_STATUS = " + getCard().getCardStatus().toString());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    // 2020/01/30 Modify by Ruling for 線上換發Debit卡：Debit卡PNX且卡片序號=1，回E113錯誤訊息，Combo回E984錯誤訊息
                    if (BINPROD.Debit.equals(TxHelper.getCardType(getCard().getCardType().byteValue()).getCrdrmark())) {
                        if (DbHelper.toBoolean(TxHelper.getCardType(getCard().getCardType().byteValue()).getFlowonlinedebit())) {
                            // 線上換發Debit卡，檢核卡片狀態*/
                            rtn = checkCardStatusByPNXforOnlineDebit();
                            if (rtn != CommonReturnCode.Normal) {
                                return rtn;
                            }
                        } else {
                            // 擋掉非線上換發Debit卡*/
                            rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                            getLogContext().setRemark("非線上換發Debit卡");
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        }
                    } else {
                        // 檢核新卡二卡合一狀態, 必須為 5 or 6 */
                        if (getCard().getCardCombine() != ATMCardCombine.SendOut.getValue() && getCard().getCardCombine() != ATMCardCombine.CombineStart.getValue()) {
                            rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                            getLogContext().setRemark("二卡合一狀態 <> 5 or 6, Card.CARD_COMBINE = " + getCard().getCardCombine());
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        }

                        // 1/26 修改, 新卡狀態為製卡且卡片序號>1 */
                        Card oCard = new Card();
                        if (getCard().getCardStatus() == ATMCardStatus.Create.getValue()) {
                            if (getCard().getCardCardSeq() > 1) {
                                // 檢核舊卡是否做過APP交易 */
                                // 2012/07/18 Modify by Ruling for PNX 增加 ICMARK 欄位
                                if (ATMTXCD.PNM.name().equals(getFeptxn().getFeptxnTxCode())) {
                                    oCard.setCardActno("00" + getFeptxn().getFeptxnTrk3().substring(6, 20));
                                    oCard.setCardCardSeq(Short.valueOf(getFeptxn().getFeptxnTrk3().substring(59, 61)));
                                } else {
                                    oCard.setCardActno(getFeptxn().getFeptxnMajorActno());
                                    oCard.setCardCardSeq(getFeptxn().getFeptxnCardSeq());
                                }
                                oCard.setCardCombine((short) ATMCardCombine.Extending.getValue());
                                oCard.setCardStatus((short) ATMCardStatus.Start.getValue());
                                Card card = cardExtMapper.getOldCardByMaxCardSeq(oCard);
                                if (card == null) {
                                    rtn = FEPReturnCode.ComboCardStatusNotMatch; /// *金融信用卡狀態不合，無法交易*/
                                    // 2020/02/24 Modify by Ruling for 調整log說明
                                    getLogContext().setRemark("找不到舊卡, oCARD_ACTNO = " + oCard.getCardActno() + ", oCard.CARD_CARD_SEQ < " + oCard.getCardCardSeq() + ", oCard.CARD_COMBINE = "
                                            + oCard.getCardCombine() + ", oCard.CARD_STATUS = " + oCard.getCardStatus());
                                    logMessage(Level.INFO, getLogContext());
                                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                    updateCard(getCard(), codeRefBase); // Update Card
                                    rtn = codeRefBase.get(); // Update Card
                                    return rtn;
                                }
                                oCard = card;
                            } else {
                                // 2012/08/28 Modify by Ruling for PNX且卡片序號=1，回錯誤訊息
                                // 2016/06/21 Modify by Ruling for COMBO開卡作業優化
                                rtn = FEPReturnCode.ComboCardStatusNotMatch; /// *金融信用卡狀態不合，無法交易(E984)*/
                                getLogContext().setRemark("CARD_STATUS=2(製卡) AND CARD_CARD_SEQ=1，回金融信用卡狀態不合，無法交易");
                                // rtn = FEPReturnCode.ComboCardNotEffective '/* 金融信用卡未啟用 */
                                // getLogContext().setRemark("CARD_STATUS=2(製卡) AND CARD_CARD_SEQ=1，回金融信用卡未啟用的錯誤訊息"
                                logMessage(Level.INFO, getLogContext());
                                // 2020/02/24 Modify by Ruling for 增加更新卡檔
                                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                updateCard(getCard(), codeRefBase); // Update Card
                                rtn = codeRefBase.get(); // Update Card
                                return rtn;
                            }
                        }

                        // 1/26 修改, 新卡狀態為啟用且卡片序號>1 */
                        if (getCard().getCardStatus() == ATMCardStatus.Receive.getValue() && getCard().getCardCardSeq() > 1) {
                            // 檢核舊卡資料不得為活卡(狀態<=4) */
                            // 2012/07/18 Modify by Ruling for PNX 增加 ICMARK 欄位
                            if (ATMTXCD.PNM.name().equals(getFeptxn().getFeptxnTxCode())) {
                                oCard.setCardActno("00" + getFeptxn().getFeptxnTrk3().substring(6, 20));
                            } else {
                                oCard.setCardActno(getFeptxn().getFeptxnMajorActno());
                            }
                            oCard.setCardCardSeq(getFeptxn().getFeptxnCardSeq());
                            if (cardExtMapper.queryStatusNot5678ByActno(oCard.getCardActno(), oCard.getCardCardSeq()) > 0) {
                                // If dbCard.QueryStatusNot5678ByActno(oCard.CARD_ACTNO, oCard.CARD_CARD_SEQ) > 1 Then
                                rtn = FEPReturnCode.ComboCardStatusNotMatch; /// *金融信用卡狀態不合，無法交易*/
                                getLogContext().setRemark("有舊卡且為活卡(狀態<=4), oCard.CARD_ACTNO = " + oCard.getCardActno() + ", oCard.CARD_CARD_SEQ < " + oCard.getCardCardSeq());
                                logMessage(Level.INFO, getLogContext());
                                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                updateCard(getCard(), codeRefBase); // Update Card
                                rtn = codeRefBase.get(); // Update Card
                                return rtn;
                            }
                        }
                    }

                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 磁條密碼變更(PN3) */
                // 2011-05-05 by kyo for 開卡流程新電文
                if (ATMTXCD.PN3.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.PN0.name().equals(getFeptxn().getFeptxnTxCode())) {
                    if (getCard().getCardStatus() != ATMCardStatus.Receive.getValue() && getCard().getCardStatus() != ATMCardStatus.Start.getValue()) {
                        rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                        getLogContext().setRemark("卡片狀態 <> 3 or 4, Card.CARD_STATUS = " + getCard().getCardStatus());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    // 2018/08/02 Modify by Ruling for MASTER DEBIT加悠遊：Master Debit/Combo卡不得執行PN0
                    if (!StringUtils.isBlank(getCard().getCardCreditno()) && !StringUtils.leftPad("0", 16, '0').equals(getCard().getCardCreditno())) {
                        rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                        getLogContext().setRemark("信用卡號有值, Card.CARD_CREDITNO =" + getCard().getCardCreditno());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 舊卡註銷(APP) */
                // 2011-05-05 by kyo for 開卡流程新電文
                if (ATMTXCD.APP.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.AP1.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 1/26 修改, 卡片狀態必須為啟用 */
                    if (getCard().getCardStatus() != ATMCardStatus.Start.getValue()) {
                        rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用 */
                        getLogContext().setRemark("卡片狀態 <> 4, Card.CARD_STATUS = " + getCard().getCardStatus().toString());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    // 1/26 修改, 檢核是否已做過APP交易 */
                    // 2011-05-18 by kyo for 判斷的欄位寫錯成card_status,
                    if (getCard().getCardCombine() == ATMCardCombine.Extending.getValue()) {
                        rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合,無法交易
                        getLogContext().setRemark("二卡合一狀態狀態 = 9, Card.CARD_COMBINE = " + getCard().getCardCombine());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    // 2012/07/15 Modify by Ruling for AP1 判斷 ICMARK
                    // 1/26 修改, 檢核欲開卡之新卡資料 */
                    Card nCard = new Card();
                    if (ATMTXCD.APP.name().equals(getFeptxn().getFeptxnTxCode())) {
                        nCard.setCardActno("00" + getFeptxn().getFeptxnTrk3().substring(6, 20));
                    } else {
                        nCard.setCardActno(getFeptxn().getFeptxnMajorActno());
                    }

                    // 2011-05-18 by kyo for 找新卡的CARD_TYPE傳錯參數，導致於抓不到新卡
                    nCard.setCardCardSeq(cardExtMapper.getMaxCardSeq(getCard().getCardActno(), "0")); // 第二個參數"0" 代表不加條件
                    Card card = cardExtMapper.selectByPrimaryKey(nCard.getCardActno(), nCard.getCardCardSeq());
                    if (card == null) {
                        rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合,無法交易
                        getLogContext().setRemark("找不到新卡, 新Card.CARD_ACTNO = " + nCard.getCardActno() + ", 新Card.CARD_CARD_SEQ = " + nCard.getCardCardSeq().toString());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    nCard = card;
                    // 2012/07/15 Modify by Ruling for AP1 判斷 ICMARK
                    // 比對新卡及舊卡序號 */
                    if (nCard.getCardCardSeq() <= getFeptxn().getFeptxnCardSeq()) {
                        // If .FEPTXN_TRK3.Length < 61 OrElse nCard.CARD_CARD_SEQ.ToString <= .FEPTXN_TRK3.Substring(59, 2) Then
                        rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合,無法交易
                        getLogContext().setRemark("新卡序號 <= 舊卡序號, 新Card.CARD_CARD_SEQ = " + nCard.getCardCardSeq() + ", FEPTXN_CARD_SEQ = " + getFeptxn().getFeptxnCardSeq());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    // 檢核新卡狀態, 必須為製卡 */
                    if (nCard.getCardStatus() != ATMCardStatus.Create.getValue()) {
                        rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合,無法交易
                        getLogContext().setRemark("卡片狀態 <> 2, 新Card.CARD_CARD_SEQ = " + nCard.getCardCardSeq() + ", 新CARD.CARD_STATUS = " + nCard.getCardStatus());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    // 檢核新卡二卡合一狀態, 必須為 5 or 6 */
                    if (nCard.getCardCombine() != ATMCardCombine.SendOut.getValue() && nCard.getCardCombine() != ATMCardCombine.CombineStart.getValue()) {
                        rtn = ATMReturnCode.ComboCardStatusNotMatch; // 金融信用卡狀態不合，無法交易
                        getLogContext().setRemark("二卡合一狀態 <> 5 or 6, 新Card.CARD_CARD_SEQ = " + nCard.getCardCardSeq() + ", 新Card.CARD_COMBINE = " + nCard.getCardCombine());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }

                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 2019/11/19 Modify by Ruling for 卡片狀態為掛失或註銷，回覆前端錯誤訊息調整
                if (getCard().getCardStatus() > ATMCardStatus.Start.getValue()) {
                    rtn = ATMReturnCode.CardLoseEfficacy; // 失效卡片
                    getLogContext().setRemark("卡片狀態 > 4, Card.CARD_STATUS = " + getCard().getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                if (getCard().getCardStatus() < ATMCardStatus.Start.getValue()) {
                    // If Card.CARD_STATUS <> CInt(ATMCardStatus.Start) Then
                    rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                    getLogContext().setRemark("卡片狀態 < 4, Card.CARD_STATUS = " + getCard().getCardStatus());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 續卡中只能執行餘額查詢交易
                if (getCard().getCardCombine() == ATMCardCombine.Extending.getValue()
                        && !ATMTXCD.IIQ.name().equals(getFeptxn().getFeptxnTxCode())) {
                    rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                    getLogContext().setRemark("續卡中只能執行餘額查詢交易, Card.CARD_COMBINE = " + getCard().getCardCombine() + ", FEPTXN_TX_CODE = " + getFeptxn().getFeptxnTxCode());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 1/28 修改:COMBO卡-金融卡密碼變更(PNB) 判斷COMBO卡磁條是否開卡 */
                if (ATMTXCD.PNB.name().equals(getFeptxn().getFeptxnTxCode())
                        && StringUtils.rightPad("9", 14, '9').equals(getFeptxn().getFeptxnTrk3().substring(79, 93))) {
                    rtn = ATMReturnCode.ComboCardNotEffective; // 金融信用卡未啟用 */
                    getLogContext().setRemark("金融卡密碼變更(PNB)交易, 金融信用卡未啟用, FEPTXN_TRK3(80,14) = " + getFeptxn().getFeptxnTrk3().substring(79, 93));
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 台灣卡須檢核身分證號是否與卡片檔相符
                // 2012/10/16 Modify by Ruling for 修正公司戶預約交易晶片卡備註欄位移，公司戶統編只有8位，上T24主機會Trim掉，長度不足，與CARD檔身份證號欄位比對不符
                if (!StringUtils.isBlank(getFeptxn().getFeptxnMajorActno()) && String.valueOf(ATMZone.TWN).equals(getFeptxn().getFeptxnZoneCode())) {
                    if (!StringUtils.isBlank(getFeptxn().getFeptxnIcmark())) {
                        if (StringUtils.isBlank(getCard().getCardIdno1())) {
                            rtn = ATMReturnCode.ICMARKError; // 晶片卡備註欄位檢核有誤
                            getLogContext().setRemark("Card.CARD_IDNO1 為NULL或空白");
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                        }

                        // 2015/06/09 Modify by Ruling for 改檢核隨機識別碼(CARD_RANDOMNO)
                        if (!getFeptxn().getFeptxnIcmark().substring(17, 17 + getCard().getCardIdno1().trim().length()).equals(getCard().getCardIdno1().trim())) {
                            if (StringUtils.isBlank(getCard().getCardRandomno())) {
                                rtn = ATMReturnCode.ICMARKError; // 晶片卡備註欄位檢核有誤
                                getLogContext().setRemark("Card.CARD_IDNO1 = " + getCard().getCardIdno1() + " Card.CARD_RANDOMNO 為NULL或空白");
                                logMessage(Level.INFO, getLogContext());
                                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                updateCard(getCard(), codeRefBase); // Update Card
                                rtn = codeRefBase.get(); // Update Card
                                return rtn;
                            }
                            if (!getFeptxn().getFeptxnIcmark().substring(17, 17 + getCard().getCardRandomno().trim().length()).equals(getCard().getCardRandomno().trim())) {
                                rtn = ATMReturnCode.ICMARKError; // 晶片卡備註欄位檢核有誤
                                getLogContext().setRemark("晶片卡備註欄位檢核有誤 FEPTXN_ICMARK = " + getFeptxn().getFeptxnIcmark() + " Card.CARD_RANDOMNO = " + getCard().getCardRandomno());
                                logMessage(Level.INFO, getLogContext());
                                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                                updateCard(getCard(), codeRefBase); // Update Card
                                rtn = codeRefBase.get(); // Update Card
                                return rtn;
                            }
                        }
                    }
                }

                // 如為提款/轉帳/繳費交易, 檢核主帳號交易 且 交易帳號<>卡片檔的主帳號
                if (DbHelper.toBoolean(getGeneralData().getMsgCtl().getMsgctlCheckCardSelf()) && "1".equals(getCard().getCardSelf())
                        && !getFeptxn().getFeptxnTroutActno().equals(getCard().getCardActno())) {
                    rtn = ATMReturnCode.ACTNOError; // 問題帳戶
                    getLogContext().setRemark("問題帳戶(MSGCTL_CHECK_CARD_SELF = True, Card.CARD_SELF = 1, FEPTXN_TROUT_ACTNO <> Card.CARD_ACTNO), FEPTXN_TROUT_ACTNO = "
                            + getFeptxn().getFeptxnTroutActno() + ", getCard().CARD_ACTNO = " + getCard().getCardActno());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 2011-07-12 by kyo for spec update:/* 7/1增加檢核for ANB/SMS */
                // 2011-08-09 by kyo for SMS電文不會帶IDNO的值所以不檢核
                if (ATMTXCD.ANB.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 檢核輸入身份證號後9碼與 CARD 檔是否相符 */
                    if (!getCard().getCardIdno1().substring(1, 10).equals(getFeptxn().getFeptxnIdno())) {
                        rtn = FEPReturnCode.ICMARKError;
                        getLogContext().setRemark(
                                "ANB交易, FEPTXN_IDNO <> Card.CARD_IDNO1[2,9], FEPTXN_IDNO = " + getFeptxn().getFeptxnIdno() + ", Card.CARD_IDNO1[2,9] = " + getCard().getCardIdno1().substring(1, 10));
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                }

                // 檢核晶片卡交易序號
                // 2017/02/15 Modify by Ruling for 無卡提款
                // 2012/07/18 Modify by Ruling for AP1/PN0/PNX電文有 ICMARK 無 IC_SEQNO
                // 2011-07-12 by kyo for ANB/SMS 電文有 ICMARK 無 IC_SEQNO and T24跨轉交易(Channel=FCS)不需檢核 IC_SEQNO
                // Fly 2018/10/05 For 外幣無卡提款 NFE/NFW電文有 ICMARK 無 IC_SEQNO
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnIcmark()) && !ATMTXCD.SMS.name().equals(getFeptxn().getFeptxnTxCode())
                        && !ATMTXCD.ANB.name().equals(getFeptxn().getFeptxnTxCode()) && !ATMTXCD.AP1.name().equals(getFeptxn().getFeptxnTxCode())
                        && !ATMTXCD.PN0.name().equals(getFeptxn().getFeptxnTxCode()) && !ATMTXCD.PNX.name().equals(getFeptxn().getFeptxnTxCode())
                        && !ATMTXCD.NCS.name().equals(getFeptxn().getFeptxnTxCode()) && !ATMTXCD.NWD.name().equals(getFeptxn().getFeptxnTxCode())
                        && !FEPChannel.FCS.name().equals(getFeptxn().getFeptxnChannel()) && !ATMTXCD.NFE.name().equals(getFeptxn().getFeptxnTxCode())
                        && !ATMTXCD.NFW.name().equals(getFeptxn().getFeptxnTxCode())) {
                    // 2010-07-14 by kyo for FEPTXN_IC_SEQNO為Char,CARD_ICTXSEQ為Int。因此應該改變Char的型態，或是CARD_ICTXSEQ要左補零
                    if (Integer.parseInt(getFeptxn().getFeptxnIcSeqno()) == getCard().getCardIctxseq()) {
                        rtn = ATMReturnCode.ICSeqNoDuplicate; // IC卡交易序號重複
                        getLogContext().setRemark(
                                "IC卡交易序號重複(FEPTXN_IC_SEQNO = Card.CARD_ICTXSEQ), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_ICTXSEQ = " + getCard().getCardIctxseq());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    if (Long.parseLong(getFeptxn().getFeptxnIcSeqno()) < getCard().getCardIctxseq() + 1) {
                        rtn = ATMReturnCode.ICSeqNoError; // IC卡交易序號錯誤
                        getLogContext().setRemark(
                                "IC卡交易序號錯誤(FEPTXN_IC_SEQNO < Card.CARD_ICTXSEQ + 1), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_ICTXSEQ = " + getCard().getCardIctxseq());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    // 檢核正確, 將 IC 卡交易序號寫入 CARD 檔
                    // 2010-04-23 modified by kyo for rtn=NORMAL時才搬移
                    getCard().setCardIctxseq(Integer.parseInt(getFeptxn().getFeptxnIcSeqno()));
                }

                // 國際卡交易檢核
                // 2010-05-07 modiefied by kyo for DB Schema modify
                // 2011-05-17 by kyo for spec(Connie):國際卡交易檢核-修正條件
                // 2016/04/20 Modify by Ruling for EMV晶片卡2630原存交易
                if ((ATMTXCD.IQ2.name().equals(getFeptxn().getFeptxnTxCode())
                        || (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && !StringUtils.isBlank(getFeptxn().getFeptxnPcode())
                        && ("24".equals(getFeptxn().getFeptxnPcode().substring(0, 2)) || "26".equals(getFeptxn().getFeptxnPcode().substring(0, 2)))))
                        && "0".equals(getCard().getCardAppGp())) {
                    rtn = ATMReturnCode.PlusCirrusNotApply; // 尚未申請國際卡功能
                    getLogContext().setRemark("尚未申請國際卡功能 Card.CARD_APP_GP = 0");
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // 2011-05-17 by kyo for spec(Connie):消費扣款交易檢核 –新增條件
                // 2011-05-30 by kyo for spec(Connie):預先授權交易檢核 –新增條件
                // 2017-04-14 by Ruling for spec(Sarah):消費扣款功能自動啟用:大於等於消費扣款功能自動啟用生效日之後，點掉消費扣款檢核
                // 2017-06-13 by Ruling for spec(Sarah):消費扣款功能自動啟用:改交易成功後才開啟

                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && !StringUtils.isBlank(getFeptxn().getFeptxnPcode())
                        && FISCPCode.PCode2551.getValueStr().equals(getFeptxn().getFeptxnPcode()) && getCard().getCardIcpu() == 0) {
                    rtn = FEPReturnCode.NotApplyTransferActno;
                    getLogContext().setRemark("該帳戶未申請轉帳約定 Card.CARD_ICPU = 0");
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

                // BugReport(001B0518):新增CARD_TFR_FLAG邏輯判斷
                // 2010-06-04 by kyo for SPEC新增判斷ZONE_CODE:
                if (getCard().getCardTfrFlag() == ATMCardTFRFlag.NotApply.getValue()
                        && ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())) {
                    switch (getGeneralData().getMsgCtl().getMsgctlTxtype2()) {
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 18:
                        case 19:
                            // 2010-06-24 by kyo TODO事項完成:E355 rtncode列舉
                            rtn = ATMReturnCode.NotApplyTransferActno;
                            getLogContext().setRemark("該帳戶未申請轉帳約定 Card.CARD_TFR_FLAG = 0");
                            logMessage(Level.INFO, getLogContext());
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get(); // Update Card
                            return rtn;
                    }
                }

                // 2018/12/26 Modify by Ruling for 數位帳戶製發卡:數三之2，約轉/非約轉，檢核轉帳限額
                if (String.valueOf(DigitalAccount.Dig3_2.getValue()).equals(getCard().getCardDigitalactno()) && getCard().getCardTfrFlag() != ATMCardTFRFlag.NotApply.getValue()) {
                    if (getGeneralData().getMsgCtl().getMsgctlTxtype2().intValue() == 2 && getFeptxn().getFeptxnTxAmt().intValue() > ATMPConfig.getInstance().getDGFTTxLimit()) {
                        rtn = FEPReturnCode.OverLimit;
                        getLogContext().setRemark("數三之2 單筆轉帳金額超過" + String.valueOf(ATMPConfig.getInstance().getDGFTTxLimit()) + "元");
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                }

                if (getCard().getCardStopdep() > 3) {
                    rtn = ATMReturnCode.ACTNONotAllowCashTX; // 該帳戶不允許做ATM存款/提領等交易
                    getLogContext().setRemark("禁止ATM存款記號 > 3, Card.CARD_STOPDEP = " + getCard().getCardStopdep());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }

            }

            // 2. 檢核跨區提款交易
            // 2010-10-13 by kyo for:/*Update by Connie 2010/10/11*/
            if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                /// *跨行交易*/
                if (!String.valueOf(ATMZone.TWN).equals(getFeptxn().getFeptxnZoneCode())) {
                    /// *海外卡*/
                    Zone defzone = new Zone();
                    defzone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
                    if (defzone == null) {
                        return IOReturnCode.ZONENotFound;
                    }

                    getFeptxn().setFeptxnTbsdyAct(defzone.getZoneTbsdy());
                    getFeptxn().setFeptxnTxnmode(defzone.getZoneCbsMode());
//					getFeptxn().setFeptxnFeeCur(defzone.getZoneCur());

                    // 2010-12-07 by kyo for spec update: /*將手續費優惠資訊存入 FEPTXN 以便海外主機使用*/
                    // 2014-01-13 Modify by Ruling for 港澳NCB：海外卡跨行餘額查詢(2500)
                    if ("2500".equals(getFeptxn().getFeptxnPcode())) {
                        getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
                    } else if ("2510".equals(getFeptxn().getFeptxnPcode())) {/// *跨行提款*/
                        getFeptxn().setFeptxnBoxCnt(getCard().getCardFreeTw()); // /*for台灣地區提款*/
                        if (getCard().getCardFreeTw() == 2) {/// *依金額減免*/
                            getFeptxn().setFeptxnFeeCustpayAct(getCard().getCardWvTw());
                        }
                    } else {
                        getFeptxn().setFeptxnBoxCnt(getCard().getCardFreeC1()); /// *for非發卡地區提款*/
                        if (getCard().getCardFreeC1() == 2) {/// *依金額減免*/
                            getFeptxn().setFeptxnFeeCustpayAct(getCard().getCardWvOt());
                        }
                    }
                } else {// 台灣卡 */
                    // 2/7 modified by maxine for spec 2/4 修改 for 限制公司戶及大陸人士國際金融卡交易 */
                    // 2016/04/20 Modify by Ruling for EMV晶片卡2630原存交易
                    if ("24".equals(getFeptxn().getFeptxnPcode().substring(0, 2)) || "26".equals(getFeptxn().getFeptxnPcode().substring(0, 2))) {
                        // 3/6 修改 by Maxine, 檢核判斷 CARD_APP_GP 移至前面
                        if ("6".equals(getCard().getCardIdno2())
                                || StringUtils.isBlank(StringUtils.rightPad(getCard().getCardIdno1(), 10, ' ').substring(StringUtils.rightPad(getCard().getCardIdno1(), 10, ' ').length() - 2))) {// (大陸人士)(公司戶)(未申請國際卡交易)
                            getLogContext().setRemark("Card.CARD_IDNO2=" + getCard().getCardIdno2() + ";Card.CARD_IDNO1 = " + getCard().getCardIdno1());
                            rtn = FISCReturnCode.TransactionNotFound; // 4701: 財金回覆-無此交易
                            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                            updateCard(getCard(), codeRefBase); // Update Card
                            rtn = codeRefBase.get();
                            return rtn;
                        }
                    }
                }
            } else if (StringUtils.isBlank(getFeptxn().getFeptxnTroutKind())) {
                /// *自行交易*/
                // 2010-10-28 by kyo for spec update: /* 10/27 修改, 改為抓取 FETPXN_ATM_ZONE */
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnZoneCode()) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmZone())
                        && !getFeptxn().getFeptxnZoneCode().equals(getFeptxn().getFeptxnAtmZone())) {
                    Zone defZone = new Zone();
                    defZone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
                    if (defZone == null) {
                        return IOReturnCode.ZONENotFound;
                    }

                    // 10/7 新增, 取得卡片所在地區本營業日及 MODE */
                    getFeptxn().setFeptxnTbsdyAct(defZone.getZoneTbsdy());
                    getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode());

                    // 手續費折原幣
                    // 2010-07-02 by kyo for 多餘步驟移除
                    // 2010-07-20 by kyo for SPEC修改: 7/20 修改港澳卡於台灣跨區提款交易
                    if (ATMTXCD.IFW.name().equals(getFeptxn().getFeptxnTxCode())) {
                        // 2010-07-26 by kyo for SPEC修改: NCR故障，手續費幣別皆帶台幣，所以需要重搬FEPTXN_FEE_CUR
//						getFeptxn().setFeptxnFeeCur(defZone.getZoneCur()); // 手續費幣別
                        if (ATMZone.TWN.name().equals(getFeptxn().getFeptxnAtmZone())
                                && !ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())
                                && getFeptxn().getFeptxnTxCurAct().equals(getFeptxn().getFeptxnFeeCur())) {
                            getFeptxn().setFeptxnFeeCustpayAct(getFeptxn().getFeptxnFeeCustpay());
                        }

                    } else if (ATMTXCD.IFE.name().equals(getFeptxn().getFeptxnTxCode())) {
                        defZone = getZoneByZoneCode(getFeptxn().getFeptxnZoneCode());
                        if (defZone == null) {
                            return IOReturnCode.ZONENotFound;
                        }
                        // 2010-05-22 by kyo for SPEC修改跨區手續費由SYSCONF取值
//						getFeptxn().setFeptxnFeeCur(defZone.getZoneCur()); // 手續費幣別
                        if (String.valueOf(CurrencyType.TWD.name()).equals(getFeptxn().getFeptxnFeeCur())) {
                            getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(ATMPConfig.getInstance().getCrossChargeTWD())); // 跨區手續費
                        } else if (String.valueOf(CurrencyType.HKD.name()).equals(getFeptxn().getFeptxnFeeCur())) {
                            getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(ATMPConfig.getInstance().getCrossChargeHKD())); // 跨區手續費
                        } else if (String.valueOf(CurrencyType.MOP.name()).equals(getFeptxn().getFeptxnFeeCur())) {
                            getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(ATMPConfig.getInstance().getCrossChargeMOP())); // 跨區手續費
                        }
                    }

                    // 取得虛擬分行代號
                    // '2010-10-28 by kyo for spec update: /* 10/27 修改, 改為抓取 FETPXN_ATM_ZONE */
                    defZone = getZoneByZoneCode(getFeptxn().getFeptxnAtmZone());
                    if (defZone == null) {
                        return IOReturnCode.ZONENotFound;
                    }

                    // 取得虛擬ATMNO
                    if (ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())) {
//						getFeptxn().setFeptxnAtmnoVir(defZone.getZoneVirtualBrno() + "80");

                    } else if (ATMZone.HKG.name().equals(getFeptxn().getFeptxnZoneCode())) {
//						getFeptxn().setFeptxnAtmnoVir(defZone.getZoneVirtualBrno() + "81");

                    } else if (ATMZone.MAC.name().equals(getFeptxn().getFeptxnZoneCode())) {
//						getFeptxn().setFeptxnAtmnoVir(defZone.getZoneVirtualBrno() + "82");

                    }
                }
                // 提領幣別=帳戶幣別, 寫入帳戶交易金額
                // 2010-05-12 modified by kyo for 同區交易時才搬移交易金額
                if (StringUtils.isBlank(getFeptxn().getFeptxnAtmnoVir()) && getFeptxn().getFeptxnTxCurAct().equals(getFeptxn().getFeptxnTxCur())) {
                    getFeptxn().setFeptxnTxAmtAct(getFeptxn().getFeptxnTxAmt()); // 帳戶交易金額
                }

                // BubReport(001B0525):拿掉是否跨區判斷，將IIQ IQ2拆出來另外判斷
                // BugReport(001B0463):2010-05-13 海外分行同區交易(不含提款)，以帳戶幣別寫入 ATMC，必須在上主機之前修改
                // 2010-05-14 modified by kyo for SPEC修改判斷邏輯：直接指定TX_CODE=海外分行同區交易(查詢(IIQ/IQ2)及轉帳(IFT)
                if ((ATMZone.HKG.name().equals(getFeptxn().getFeptxnZoneCode())
                        || ATMZone.MAC.name().equals(getFeptxn().getFeptxnZoneCode()))
                        && (ATMTXCD.IFT.name().equals(getFeptxn().getFeptxnTxCode()))) {
                    getFeptxn().setFeptxnTxCur(getFeptxn().getFeptxnTxCurAct());
                }

                // BubReport(001B0525):將IIQ IQ2拆出來另外判斷
                if (ATMTXCD.IIQ.name().equals(getFeptxn().getFeptxnTxCode())
                        || ATMTXCD.IQ2.name().equals(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTxCur(getFeptxn().getFeptxnTxCurAct());
                }
            }

            // 3.取得海外卡分行別
            // 2011-03-04 by kyo for connie updated spec /*海外卡之分行別仍是帳號前三位, 台灣卡則由 T24 帶回分行別 Connie 2011/3/4*/
            if (!ATMZone.TWN.name().equals(getFeptxn().getFeptxnZoneCode())) {
                getFeptxn().setFeptxnBrno(getFeptxn().getFeptxnTroutActno().substring(2, 5));
            }

            // 4.更新卡片檔
            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
            updateCard(getCard(), codeRefBase); // Update Card
            rtn = codeRefBase.get(); // Update Card

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkCardStatus");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            // 2011-03-29 by kyo for 讓hostbusiness的方法可以存取到Card的資料
            getGeneralData().setCard(getCard());
        }
        return rtn;
    }

    /**
     * 透過FEPTXN取得卡片狀態
     *
     * @return DefCARD物件
     *
     *
     * <history>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>Function design</reason>
     * <date>2010/02/04</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>Bugreport(001B0246):進行一般晶片卡(包含台灣卡/海外卡)交易時，GetCardByCreditnoStatus發生「資料處理發生異常」。</reason>
     * <date>2010/02/04</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>若找無卡檔也要存卡片序號，修正BUG</reason>
     * <date>2011/03/30</date>
     * </modify>
     * </history>
     */
    public Card getCardByPK(RefBase<Feptxn> oFeptxn) {
        if (this.card == null) {
            this.card = new Card();
            try {
                if (StringUtils.isNotBlank(oFeptxn.get().getFeptxnMajorActno())) {// 晶片卡交易以 ICMARK檢核卡片檔
                    // 2011-06-07 by kyo for spec modify:/* 6/7 修改 for 製發卡管理 */
                    // ICMARK 前三碼為銀行代號, 只有卡號, 無帳號及序號 */
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(oFeptxn.get().getFeptxnIcmark().substring(0, 3))) {
                        // 以 ICMARK前16位之卡號, 讀取卡檔 */
                        this.card.setCardCardno(oFeptxn.get().getFeptxnIcmark().substring(0, 16));
                    } else {
                        this.card.setCardActno(oFeptxn.get().getFeptxnMajorActno());
                        this.card.setCardCardSeq(Short.valueOf(oFeptxn.get().getFeptxnIcmark().substring(14, 16)));
                    }
                } else if (StringUtils.isNotBlank(oFeptxn.get().getFeptxnTrk3())) {// COMBO卡舊卡註銷(APP)/晶片卡現金存款(IDR)
                    this.card.setCardActno(oFeptxn.get().getFeptxnTroutActno());
                    this.card.setCardCardSeq(Short.valueOf(oFeptxn.get().getFeptxnTrk3().substring(59, 61)));
                } else if (StringUtils.isNotBlank(oFeptxn.get().getFeptxnTrk2())) {
                    if (StringUtils.isBlank(oFeptxn.get().getFeptxnTroutKind())) {// 國際卡餘額查詢(IQ2) 或原存行國際提款
                        this.card.setCardActno(oFeptxn.get().getFeptxnTroutActno());
                        String cardCardSeq = oFeptxn.get().getFeptxnTrk2().substring(35, 37);
                        if (StringUtils.isNotBlank(cardCardSeq)) {
                            this.card.setCardCardSeq(Short.valueOf(cardCardSeq));
                        }
                    } else {// Combo卡-原存行國際提款
                        this.card.setCardCreditno(oFeptxn.get().getFeptxnTrk2().substring(0, 16));
                        // this._Card.CARD_CARD_SEQ = Byte.Parse(.FEPTXN_TRK2.Substring(35, 2))
                    }
                }
                // 2011-03-30 by kyo for 若找無卡檔也要存卡片序號，修正BUG
                oFeptxn.get().setFeptxnCardSeq(this.card.getCardCardSeq());

                if (this.card != null) {
                    Card card = null;
                    // Bugreport(001B0246):修正判斷邏輯為空字串時
                    if (StringUtils.isBlank(this.card.getCardCreditno()) && StringUtils.isBlank(this.card.getCardCardno())) {
                        card = cardExtMapper.selectByPrimaryKey(this.card.getCardActno(), this.card.getCardCardSeq());
                    } else if (StringUtils.isBlank(this.card.getCardCreditno())) {
                        card = cardExtMapper.getSingleCard(this.card.getCardCardno());
                        if (card != null) {
                            oFeptxn.get().setFeptxnMajorActno(this.card.getCardActno());
                            oFeptxn.get().setFeptxnCardSeq(this.card.getCardCardSeq());
                        }
                    } else {
                        // 2010-04-15 modified by kyo for Combo卡-原存行國際提款
                        card = cardExtMapper.getCardByCreditNoStatus(this.card);
                    }
                    // 2019/11/19 Modify by Ruiing for 增加顯示Log並加遮蔽
                    if (card == null) {
                        String exceptionMessage = null;
                        if (StringUtils.isBlank(this.card.getCardCreditno()) && StringUtils.isBlank(this.card.getCardCardno())) {
                            exceptionMessage = StringUtils.join(
                                    "讀不到卡檔 (卡片帳號: ", this.card.getCardActno().substring(0, 5), "XXXXX", this.card.getCardActno().substring(10),
                                    " 卡片序號: ", this.card.getCardCardSeq(), ")");
                        } else if (StringUtils.isBlank(this.card.getCardCreditno())) {
                            exceptionMessage = StringUtils.join(
                                    "讀不到卡檔資料 (卡號: ", this.card.getCardCardno().substring(0, 5), "XXXXX", this.card.getCardCardno().substring(10), ")");
                        } else {
                            exceptionMessage = StringUtils.join(
                                    "讀不到卡檔資料 (信用卡卡號: ", this.card.getCardCardno().substring(0, 5), "XXXXX", this.card.getCardCardno().substring(10), ")");
                            getLogContext().setRemark("讀不到卡檔 (信用卡卡號: " + this.card.getCardCreditno().substring(0, 5) + "XXXXX" + this.card.getCardCreditno().substring(10) + ")");
                        }
                        getLogContext().setRemark(exceptionMessage);
                        logMessage(Level.INFO, getLogContext());
                        throw ExceptionUtil.createException(exceptionMessage);
                    } else {
                        this.card = card;
                    }
                }
            } catch (Exception ex) {
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(StringUtils.join(ProgramName, ".getCardByPK"));
                sendEMS(getLogContext());
                return null;
            }
        }
        return this.card;
    }

    /**
     * 線上換發Debit卡，PNX開卡檢核卡片狀態
     *
     * @return FEPReturnCode
     *
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>線上換發Debit卡</reason>
     * <date>2020/02/24</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkCardStatusByPNXforOnlineDebit() {
        FEPReturnCode rtn = CommonReturnCode.Normal;

        try {
            // 檢核新卡二卡合一狀態, 必須為 5 or 6 */
            if (getCard().getCardCombine() != ATMCardCombine.SendOut.getValue() && getCard().getCardCombine() != ATMCardCombine.CombineStart.getValue()) {
                rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                getLogContext().setRemark("(線上換發Debit卡)二卡合一狀態 <> 5 or 6, Card.CARD_COMBINE = " + getCard().getCardCombine());
                logMessage(Level.INFO, getLogContext());
                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                updateCard(getCard(), codeRefBase); // Update Card
                rtn = codeRefBase.get(); // Update Card
                return rtn;
            }

            // 新卡狀態為製卡且卡片序號>1 */
            Card oCard = new Card();
            if (getCard().getCardStatus() == ATMCardStatus.Create.getValue()) {
                if (getCard().getCardCardSeq() > 1) {
                    // 檢核舊卡是否做過APP交易 */
                    if (ATMTXCD.PNM.name().equals(getFeptxn().getFeptxnTxCode())) {
                        oCard.setCardActno("00" + getFeptxn().getFeptxnTrk3().substring(6, 20));
                        oCard.setCardCardSeq(Short.valueOf(getFeptxn().getFeptxnTrk3().substring(59, 61)));
                    } else {
                        oCard.setCardActno(getFeptxn().getFeptxnMajorActno());
                        oCard.setCardCardSeq(getFeptxn().getFeptxnCardSeq());
                    }
                    oCard.setCardCombine((short) ATMCardCombine.Extending.getValue());
                    oCard.setCardStatus((short) ATMCardStatus.Start.getValue());
                    Card card = cardExtMapper.getOldCardByMaxCardSeq(oCard);
                    if (card == null) {
                        rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                        getLogContext().setRemark("(線上換發Debit卡)找不到舊卡, oCARD_ACTNO = " + oCard.getCardActno() + ", oCard.CARD_CARD_SEQ < " + oCard.getCardCardSeq() + ", oCard.CARD_COMBINE = "
                                + oCard.getCardCombine() + ", oCard.CARD_STATUS = " + oCard.getCardStatus());
                        logMessage(Level.INFO, getLogContext());
                        RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                        updateCard(getCard(), codeRefBase); // Update Card
                        rtn = codeRefBase.get(); // Update Card
                        return rtn;
                    }
                    oCard = card;
                } else {
                    rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                    getLogContext().setRemark("(線上換發Debit卡)CARD_STATUS=2(製卡) AND CARD_CARD_SEQ=1，回金融信用卡狀態不合，無法交易");
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }
            }

            // 新卡狀態為啟用且卡片序號>1 */
            if (getCard().getCardStatus() == ATMCardStatus.Receive.getValue() && getCard().getCardCardSeq() > 1) {
                // 檢核舊卡資料不得為活卡(狀態<=4) */
                if (ATMTXCD.PNM.name().equals(getFeptxn().getFeptxnTxCode())) {
                    oCard.setCardActno("00" + getFeptxn().getFeptxnTrk3().substring(6, 20));
                } else {
                    oCard.setCardActno(getFeptxn().getFeptxnMajorActno());
                }
                oCard.setCardCardSeq(getFeptxn().getFeptxnCardSeq());
                if (cardExtMapper.queryStatusNot5678ByActno(oCard.getCardActno(), oCard.getCardCardSeq()) > 0) {
                    rtn = ATMReturnCode.CardNotEffective; // 卡片尚未生效
                    getLogContext().setRemark("(線上換發Debit卡)有舊卡且為活卡(狀態<=4), oCard.CARD_ACTNO = " + oCard.getCardActno() + ", oCard.CARD_CARD_SEQ < " + oCard.getCardCardSeq());
                    logMessage(Level.INFO, getLogContext());
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
                    updateCard(getCard(), codeRefBase); // Update Card
                    rtn = codeRefBase.get(); // Update Card
                    return rtn;
                }
            }
            return rtn;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkCardStatusByPNXforOnlineDebit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkDigit(String actno) {
        final String strWeight = "212121212121212";
        final int ModValue = 10;
        int checkSum = 0;
        String checkNo = "";
        String TempStr = "";
        int i = 0;
        try {
            if (!PolyfillUtil.isNumeric(actno) || actno.length() != 16) {
                return ATMReturnCode.TranInACTNOError; // 轉入帳號錯誤 TranInACTNOError */
            }

            for (i = 0; i < strWeight.length(); i++) {
                TempStr = String.valueOf(
                        Integer.valueOf(actno.substring(i, i + 1)) * Integer.valueOf(strWeight.substring(i, i + 1)));
                if (TempStr.length() > 1) {
                    TempStr = String.valueOf(
                            Integer.valueOf(TempStr.substring(0, 1)) + Integer.valueOf(TempStr.substring(1, 2)));
                }
                checkSum += Integer.parseInt(TempStr);
            }
            checkNo = String.valueOf((ModValue - (checkSum % ModValue)));
            // checkNo取最右邊一位
            if (!actno.substring(actno.length() - 1).equals(checkNo.substring(checkNo.length() - 1))) {
                getLogContext().setRemark("信用卡號檢查碼錯誤, 正確 = " + checkNo.substring(checkNo.length() - 1));
                logMessage(Level.INFO, getLogContext());
                return ATMReturnCode.TranInACTNOError; // 轉入帳號錯誤 TranInACTNOError
            }

            return CommonReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkDigit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // ''' <summary>
    // ''' 寫入香港SMS簡訊資料
    // ''' </summary>
    // ''' <returns>
    // ''' FEPReturnCode
    // ''' </returns>
    // ''' <remarks>不管送SMS是否成功一律回Normal</remarks>
    // ''' <history>
    // ''' <modify>
    // ''' <modifier>Ruling</modifier>
    // ''' <reason>新增程式</reason>
    // ''' <date>2014/10/24</date>
    // ''' </modify>
    // ''' </history>
    public FEPReturnCode prepareHKSMS() {
        // TODO 暫不實做
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        return rtnCode;
    }

    // 'Fly 2018/05/10 for 國外提款提醒及管控機制
    public FEPReturnCode prepareSMSMAIL() throws Exception {
//        SmsmsgMapper smsmsgMapper = SpringBeanFactoryUtil.getBean(SmsmsgMapper.class);
//        Smsmsg smsmsg = new Smsmsg();
//        SmlparmMapper smlparmMapper = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
//        Smlparm smlparm = new Smlparm();
//        smsmsg.setSmsmsgTxDate(getFeptxn().getFeptxnTxDate());
//        smsmsg.setSmsmsgEjfno(getFeptxn().getFeptxnEjfno());
//        smsmsg = smsmsgMapper.selectByPrimaryKey(smsmsg.getSmsmsgTxDate(), smsmsg.getSmsmsgEjfno());
//        if (smsmsg == null) {
//            getLogContext().setRemark("SMSMSG查無資料");
//            logMessage(Level.INFO, getLogContext());
//            return FEPReturnCode.Normal;
//        }
//        // 'Fly 2018/06/27 無電話號碼(簡訊)仍要繼續往下走
//        if (StringUtils.isBlank(smsmsg.getSmsmsgNumber())) {
//            getLogContext().setRemark("無電話號碼，不需送SMS");
//            logMessage(Level.INFO, getLogContext());
//        } else {
//            smsmsg.setSmsmsgSend("Y");
//            try {
//                if (smsmsgMapper.updateByPrimaryKeySelective(smsmsg) <= 0) {
//                    getLogContext().setRemark("更新SMSMSG異常，不送SMS");
//                    logMessage(Level.INFO, getLogContext());
//                    return FEPReturnCode.Normal;
//                }
//            } catch (Exception ex) {
//                getLogContext().setRemark(StringUtils.join("更新SMSMSG異常，不送SMS", ex.toString()));
//                logMessage(Level.INFO, getLogContext());
//                return FEPReturnCode.Normal;
//            }
//            smlparm.setSmlparmType("S");
//            smlparm.setSmlparmSeqno(1);
//            Smlparm ResultSmlparm = smlparmMapper.selectByPrimaryKey(smlparm.getSmlparmType(), smlparm.getSmlparmSeqno());
//            if (ResultSmlparm != null) {
//                smlparm = ResultSmlparm;
//            }
//            String msg = smlparm.getSmlparmContent();
//            msg = StringUtils.replace(msg, "[PARM1]",
//                    new StringBuilder()
//                            .append(smsmsg.getSmsmsgTxDate().substring(4))
//                            .append(smsmsg.getSmsmsgTxTime().substring(0, 4))
//                            .insert(6, ":")
//                            .insert(4, StringUtils.SPACE)
//                            .insert(2, "/").toString());
//            msg = StringUtils.replace(msg, "[PARM2]", FormatUtil.longFormat(smsmsg.getSmsmsgTxAmtAct().longValue(), "#,##0"));
//            // 'Fly 2018/06/12 將發送SMSDB移至TxHelper
//            TxHelper.sendSMSDB(
//                    smsmsg.getSmsmsgNumber(),
//                    msg,
//                    smlparm.getSmlparmPriority().byteValue(),
//                    smsmsg.getSmsmsgBrno(),
//                    smsmsg.getSmsmsgPcode(),
//                    smsmsg.getSmsmsgCbsRrn(),
//                    smsmsg.getSmsmsgIdno(),
//                    smlparm.getSmlparmCompany(),
//                    smlparm.getSmlparmChannel(),
//                    this.getLogContext());
//        }
//        // 'Fly 2018/06/11 應判斷SMSMSG_EMAIL欄位
//        if (StringUtils.isBlank(smsmsg.getSmsmsgEmail())) {
//            getLogContext().setRemark("無EMAIL");
//            logMessage(Level.INFO, getLogContext());
//            return FEPReturnCode.Normal;
//        }
//        smlparm.setSmlparmType("M");
//        smlparm.setSmlparmSeqno(1);
//        Smlparm ResultSmlparm = smlparmMapper.selectByPrimaryKey(smlparm.getSmlparmType(), smlparm.getSmlparmSeqno());
//        if (ResultSmlparm != null) {
//            smlparm = ResultSmlparm;
//        }
//        String txdate = smsmsg.getSmsmsgTxDate();
//        txdate = new StringBuffer().append(txdate).insert(6, "月").insert(4, "年").append("日").toString();
//        String actno = smsmsg.getSmsmsgTroutActno().substring(2);
//        actno = actno.substring(0, 8) + "***" + actno.substring(11, 13) + "*";
//        actno = new StringBuilder().append(actno).insert(13, "-").insert(6, "-").insert(3, "-").toString();
//        String time = StringUtils.join(smsmsg.getSmsmsgTxDate(), smsmsg.getSmsmsgTxTime());
//        time = new StringBuilder().append(time).insert(12, ":").insert(10, ":").insert(8, " ").insert(6, "/").insert(4, "/").toString();
//        String brno = smsmsg.getSmsmsgBrno();
//
//        Allbank allbank = new Allbank();
//        // 'Fly 2020/07/30 改抓SYSSTAT
//        allbank.setAllbankBkno(SysStatus.getPropertyValue().getSysstatHbkno());
//        allbank.setAllbankBkno(smsmsg.getSmsmsgBrno());
//        allbank = allbankExtMapper.selectByPrimaryKey(allbank.getAllbankBkno(), allbank.getAllbankBrno());
//        if (allbank != null && StringUtils.isNotBlank(allbank.getAllbankFullname())) {
//            brno = allbank.getAllbankFullname().trim();
//        }
//
//        String mailBody = smlparm.getSmlparmContent();
//        String subject = smlparm.getSmlparmSubject();
//        mailBody = StringUtils.replace(mailBody, "[PARM1]", txdate);
//        mailBody = StringUtils.replace(mailBody, "[PARM2]", brno);
//        mailBody = StringUtils.replace(mailBody, "[PARM3]", actno);
//        mailBody = StringUtils.replace(mailBody, "[PARM4]", smsmsg.getSmsmsgTxCur());
//        mailBody = StringUtils.replace(mailBody, "[PARM5]",
//                smsmsg.getSmsmsgTxAmt() != null ? FormatUtil.longFormat(smsmsg.getSmsmsgTxAmt().longValue(), "#,##0") : StringUtils.EMPTY);
//        mailBody = StringUtils.replace(mailBody, "[PARM6]",
//                smsmsg.getSmsmsgTxAmtAct() != null ? FormatUtil.longFormat(smsmsg.getSmsmsgTxAmtAct().longValue(), "#,##0") : StringUtils.EMPTY);
//        mailBody = StringUtils.replace(mailBody, "[PARM7]", time);
//        // 'Fly 2018/06/12 將發送EMAIL移至TxHelper
//        TxHelper.sendMailHunter(
//                smlparm.getSmlparmProj(),
//                smsmsg.getSmsmsgEmail(),
//                smlparm.getSmlparmFromname(),
//                smlparm.getSmlparmFromemail(),
//                subject,
//                mailBody,
//                smlparm.getSmlparmChannel(),
//                smlparm.getSmlparmPgcode(),
//                smsmsg.getSmsmsgIdno(),
//                smlparm.getSmlparmPriority().toString(),
//                this.getLogContext());
        return FEPReturnCode.Normal;
    }

    public FEPReturnCode checkVirActno() {
        try {
            // 檢核16位轉入帳號是否為虛擬帳號(前二碼<>'00')
            if (getFeptxn().getFeptxnTrinActno().trim().length() < 16) {
                return ATMReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
            }

            // 判斷轉入帳號第一碼
            switch (getFeptxn().getFeptxnTrinActno().substring(0, 1)) {
                case "0":
                    if (!"3".equals(getFeptxn().getFeptxnTrinActno().substring(1, 2))) {
                        return ATMReturnCode.TranInACTNOError;
                    } else {
                        // 檢核是否為本行AE卡BIN
                        if (!"C".equals(getFeptxn().getFeptxnTrinKind())) {
                            return ATMReturnCode.TranInACTNOError;
                        }
                    }
                    break;
                case "1":
                    return ATMReturnCode.TranInACTNOError;
                case "2":
                    // 轉入帳號第二、三碼必須為 87
                    if (!"87".equals(getFeptxn().getFeptxnTrinActno().substring(1, 3))) {
                        return ATMReturnCode.TranInACTNOError;
                    }
                    break;
                case "3":
                case "4":
                case "5":
                case "6":
                    if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
                        return ATMReturnCode.TranInACTNOError;
                    }
                    break;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkVirActno");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // <summary>
    // 檢核外圍系統 EJNO 是否重複
    // </summary>
    // <returns></returns>
    // <remarks></remarks>
    // <history>
    // <modify>
    // <modifier>HusanYin</modifier>
    // <reason>給定CHEJNODUPLICATE RETURN CODE</reason>
    // <date>2011/01/05</date>
    // <reason>執行分析後調整程式碼</reason>
    // <date>2011/01/07</date>
    // </modify>
    // <modify>
    // <modifier>ChenLi</modifier>
    // <reason>由於SelfNB亦會用到此function，故將其移至BusinessBase</reason>
    // <date>2013/06/27</date>
    // </modify>
    public FEPReturnCode checkChannelEJFNO() {
        int i = 0, j = 0;
        try {
            if (FEPChannel.FCS.toString().equals(getFeptxn().getFeptxnChannel())) {
                // 預約交易
                // 若 CHANNEL_EJFNO 重複且原交易為成功交易則 RETURN ERROR
                i = this.feptxnDao.queryByChannelEJ(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), "A");
                if (i <= 0) {
                    return CommonReturnCode.Normal;
                } else {
                    return FEPReturnCode.ChannelEJFNODuplicate;
                }
            } else {
                // NETBANK、IVR、MOBILBANK
                // 若 CHANNEL_EJFNO 重複則 RETURN ERROR
                i = this.feptxnDao.queryByChannelEJ(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), "");
                if (i <= 0) {
                    Zone twnZone = getZoneByZoneCode(ZoneCode.TWN);
                    if (twnZone == null) {
                        return IOReturnCode.ZONENotFound;
                    }
                    if (DbHelper.toBoolean(twnZone.getZoneChgday())) {
                        FeptxnDao fepTxnDaoLBSDY = SpringBeanFactoryUtil.getBean("feptxnDao");
                        fepTxnDaoLBSDY.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".checkChannelEJFNO"));
                        j = fepTxnDaoLBSDY.queryByChannelEJ(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), "");
                        if (j <= 0) {
                            return CommonReturnCode.Normal;
                        } else {
                            return FEPReturnCode.ChannelEJFNODuplicate;
                        }
                    } else {
                        return CommonReturnCode.Normal;
                    }
                } else {
                    return FEPReturnCode.ChannelEJFNODuplicate;
                }
            }
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkChannelEJFNO"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 外圍新增時原判斷唯一只有 FEPTXN_CHANNEL_EJFNO,改判斷唯一要用 FEPTXN_CHANNEL_EJFNO + FEPTXN_CHANNEL
     *
     * @return
     */
    public FEPReturnCode checkChannelEJFNOAndChannel() {
        int i = 0, j = 0;
        try {
            if (FEPChannel.FCS.toString().equals(getFeptxn().getFeptxnChannel())) {
                // 預約交易
                // 若 CHANNEL_EJFNO 重複且原交易為成功交易則 RETURN ERROR
                i = this.feptxnDao.queryByChannelEJAndChannel(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), getFeptxn().getFeptxnChannel(), "A");
                if (i <= 0) {
                    return CommonReturnCode.Normal;
                } else {
                    return FEPReturnCode.ChannelEJFNODuplicate;
                }
            } else {
                // NETBANK、IVR、MOBILBANK
                // 若 CHANNEL_EJFNO 重複則 RETURN ERROR
                i = this.feptxnDao.queryByChannelEJAndChannel(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), getFeptxn().getFeptxnChannel(), "");
                if (i <= 0) {
                    Zone twnZone = getZoneByZoneCode(ZoneCode.TWN);
                    if (twnZone == null) {
                        return IOReturnCode.ZONENotFound;
                    }
                    if (DbHelper.toBoolean(twnZone.getZoneChgday())) {
                        FeptxnDao fepTxnDaoLBSDY = SpringBeanFactoryUtil.getBean("feptxnDao");
                        fepTxnDaoLBSDY.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, ".checkChannelEJFNO"));
                        j = fepTxnDaoLBSDY.queryByChannelEJAndChannel(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnChannelEjfno(), getFeptxn().getFeptxnEjfno(), getFeptxn().getFeptxnChannel(), "");
                        if (j <= 0) {
                            return CommonReturnCode.Normal;
                        } else {
                            return FEPReturnCode.ChannelEJFNODuplicate;
                        }
                    } else {
                        return CommonReturnCode.Normal;
                    }
                } else {
                    return FEPReturnCode.ChannelEJFNODuplicate;
                }
            }
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkChannelEJFNO"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public boolean checkEXPCD(String expcd) {
        if (expcd == null) {
            return false;
        }
        // 2010-04-12 modified by kyo for 將空字串TRIM掉後補0補滿4位判斷是否為"0000"回傳正確
        // bugreport(001B0231):2010-04-14 modified by kyo for 修正改為把"trim掉後補0"的字串與"0000"作比較
        if ("0000".equals(StringUtils.leftPad(expcd.trim(), 4, "0"))) {
            return true;
        }
        return false;
    }

    /**
     * 2024-05-00 Joe add Email (使用Notify)
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode SendMail(Feptxntcb feptxntcb, Feptxn feptxn) { //Mail Notify
        getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendMail"));
        getLogContext().setRemark("SendMail Start.");
        logMessage(getLogContext());

        //若是feptxntcb getFeptxntcbTxDate or getFeptxntcbEjfno 為空值或NULL，則重新讀若是feptxntcb
        if (StringUtils.isBlank(feptxntcb.getFeptxntcbTxDate()) || feptxntcb.getFeptxntcbEjfno() == null) {
            try {
                feptxntcb = feptxnDao.selectByPrimaryKeyForFeptxntcb(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
                if (feptxntcb.getFeptxntcbTxDate().equals(feptxn.getFeptxnTxDate()) && feptxntcb.getFeptxntcbEjfno().equals(feptxn.getFeptxnEjfno())) {
                    getLogContext().setRemark("SendMail Data is Valid.");
                    logMessage(getLogContext());
                } else {
                    getLogContext().setRemark("sendMail 有異常." + FEPReturnCode.FEPTXNNotFound.toString());
                    return FEPReturnCode.FEPTXNNotFound;
                }
            } catch (Exception e) {
                getLogContext().setProgramException(e);
                getLogContext().setMessage("Load FEPTXNTCB error" + FEPReturnCode.FEPTXNNotFound.toString());
                getLogContext().setRemark("sendMail Error.");
                logMessage(getLogContext());
                return FEPReturnCode.FEPTXNNotFound;
            }
        }

        getLogContext().setRemark("FEPTXNTCB Data");
        getLogContext().setMessage("feptxntcb FeptxntcbTxDate:" + feptxntcb.getFeptxntcbTxDate() + ", feptxn FeptxnTxDate:" + feptxn.getFeptxnTxDate()
                + "; feptxntcb FeptxntcbEjfno:" + feptxntcb.getFeptxntcbEjfno() + ", feptxn FeptxnEjfno:" + feptxn.getFeptxnEjfno());
        logMessage(getLogContext());

        Map<String, String> parameter = new HashMap<>();
        String templateId = feptxntcb.getFeptxntcbNoticeNumber();
        String to = feptxntcb.getFeptxntcbNoticeEmail();
        String tx_datehm = null;
        int tx_amt_int = 0;
        String tx_actno3 = null;
        String tx_amt = null;


        if (feptxn.getFeptxnReqDatetime() != null) {
            tx_datehm = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + " " +
                    feptxn.getFeptxnReqDatetime().substring(8, 10) + ":" +
                    feptxn.getFeptxnReqDatetime().substring(10, 12);
        }
        if (feptxntcb.getFeptxntcbFromact() != null) {
            //tx_actno3 = feptxntcb.getFeptxntcbFromact().substring(feptxntcb.getFeptxntcbFromact().length()-3);
            //2024-05-24 先移除空白再取後3碼
            tx_actno3 = feptxntcb.getFeptxntcbFromact().trim().substring(feptxntcb.getFeptxntcbFromact().trim().length() - 3);
        }
        tx_amt_int = feptxn.getFeptxnTxAmtAct().intValue();
        if (tx_amt_int > 999) {
            tx_amt = Integer.toString(tx_amt_int).substring(0, Integer.toString(tx_amt_int).length() - 3) + "," + Integer.toString(tx_amt_int).substring(Integer.toString(tx_amt_int).length() - 3);
        } else {
            tx_amt = Integer.toString(tx_amt_int);
        }

        parameter.put("TX_DATEHM", tx_datehm);
        parameter.put("TX_AMT", tx_amt);
        parameter.put("TX_ACTNO3", tx_actno3);
        try {
            notifyHelper.sendATMSimpleMail(templateId, to, parameter, true, getLogContext().getEj());
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendMail"));
            getLogContext().setRemark("sendSimpleMail 異常.");
            logMessage(getLogContext());
            throw ExceptionUtil.createRuntimeException(e);
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 2024-05-00 Joe add 簡訊 (使用Notify)
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode SendSMS(Feptxntcb feptxntcb, Feptxn feptxn) { //SMS  Notify
        getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendSMS"));
        getLogContext().setRemark("SendSMS Start.");
        logMessage(getLogContext());

        //若是feptxntcb getFeptxntcbTxDate or getFeptxntcbEjfno 為空值或NULL，則重新讀若是feptxntcb
        if (StringUtils.isBlank(feptxntcb.getFeptxntcbTxDate()) || feptxntcb.getFeptxntcbEjfno() == null) {
            try {
                feptxntcb = feptxnDao.selectByPrimaryKeyForFeptxntcb(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
                if (feptxntcb.getFeptxntcbTxDate().equals(feptxn.getFeptxnTxDate()) && feptxntcb.getFeptxntcbEjfno().equals(feptxn.getFeptxnEjfno())) {
                    getLogContext().setRemark("SendSMS Data is Valid.");
                    logMessage(getLogContext());
                } else {
                    getLogContext().setRemark("SendSMS 有異常." + FEPReturnCode.FEPTXNNotFound.toString());
                    return FEPReturnCode.FEPTXNNotFound;
                }
            } catch (Exception e) {
                getLogContext().setProgramException(e);
                getLogContext().setMessage("Load FEPTXNTCB error" + FEPReturnCode.FEPTXNNotFound.toString());
                getLogContext().setRemark("SendSMS Error.");
                logMessage(getLogContext());
                return FEPReturnCode.FEPTXNNotFound;
            }
        }

        getLogContext().setRemark("FEPTXNTCB Data");
        getLogContext().setMessage("feptxntcb FeptxntcbTxDate:" + feptxntcb.getFeptxntcbTxDate() + ", feptxn FeptxnTxDate:" + feptxn.getFeptxnTxDate()
                + "; feptxntcb FeptxntcbEjfno:" + feptxntcb.getFeptxntcbEjfno() + ", feptxn FeptxnEjfno:" + feptxn.getFeptxnEjfno());
        logMessage(getLogContext());


        Map<String, String> parameter = new HashMap<>();
        String templateId = feptxntcb.getFeptxntcbNoticeNumber();
        String mo = feptxntcb.getFeptxntcbNoticeMobileno(); // 2025-04-09 Richard modified for [Cleartext Submission of Sensitive Information]
        int tx_amt_int = 0;

        String tx_datehm = null;  // yyy/mm/dd hh:mm (交易時間)
        String tx_datehmc = null; // YYY年MM月DD日HH時MM分 (含中文交易時間)
        String tx_actno3 = null; // XXX (帳號末三碼)
        String tx_dxtvc = null;  // XXXXXX  (交易驗證碼)
        String tx_date = null;  // YYY年MM月DD日 (含中文交易日)
        String tx_amt = null;  // ZZZZZZ9 (交易金額)
        String tx_amts = null;  // ZZZZ,ZZ9 (含三分位交易金額)
        String tx_amt4 = null;  // ZZZ9 (交易金額)
        String trOutActno = null;
        String trK2 = null;
        String frmAct = null;
        String idNo = null;

        if (feptxn.getFeptxnReqDatetime() != null) {
            //  yyy/mm/dd hh:mm (交易時間)
            tx_datehm = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + " " +
                    feptxn.getFeptxnReqDatetime().substring(8, 10) + ":" +
                    feptxn.getFeptxnReqDatetime().substring(10, 12);
            //  YYY年MM月DD日HH時MM分 (含中文交易時間)
            tx_datehmc = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "年" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "月" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + "日" +
                    feptxn.getFeptxnReqDatetime().substring(8, 10) + "時" +
                    feptxn.getFeptxnReqDatetime().substring(10, 12) + "分";
            // YYY年MM月DD日 (含中文交易日)
            tx_date = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "年" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "月" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + "日";
        }

        if (feptxntcb.getFeptxntcbFromact() != null) {
            //tx_actno3 = feptxntcb.getFeptxntcbFromact().substring(feptxntcb.getFeptxntcbFromact().length()-3);
            //2024-05-24 先移除空白再取後3碼
            tx_actno3 = feptxntcb.getFeptxntcbFromact().trim().substring(feptxntcb.getFeptxntcbFromact().trim().length() - 3);
            frmAct = feptxntcb.getFeptxntcbFromact().trim();
        }

        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutActno())) {
            trOutActno = feptxn.getFeptxnTroutActno().trim();
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrk2())) {
            trK2 = feptxn.getFeptxnTrk2().substring(0, 16);
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnIdno())) {
            idNo = feptxn.getFeptxnIdno();
        }

        tx_amt_int = feptxn.getFeptxnTxAmtAct().intValue();
        tx_amt = Integer.toString(tx_amt_int).trim();
        tx_amt4 = Integer.toString(tx_amt_int).trim(); //乘車碼車資
        //2024-05-31 合庫特殊格式僅加一個千分位
        if (tx_amt_int > 999) {
            tx_amts = Integer.toString(tx_amt_int).substring(0, Integer.toString(tx_amt_int).length() - 3) + "," + Integer.toString(tx_amt_int).substring(Integer.toString(tx_amt_int).length() - 3);
        } else {
            tx_amts = Integer.toString(tx_amt_int);
        }
        //2025-09-30 原存乘車碼車資
        switch (templateId) {
            case "M01":
                parameter.put("TX_DXTVC", tx_dxtvc);
                parameter.put("ApSysKey", tx_dxtvc);  //FEPTXNTCB_DXTVC
                break;
            case "M02":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("ApSysKey", trOutActno + trK2); //FEPTXN_TROUT_ACTNO+LEFT(FEPTXN_TRK2,16)
                break;
            case "M03":
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("ApSysKey", frmAct + trOutActno);  //FEPTXNTCB_FROMACT+FEPTXN_TROUT_ACTNO
                break;
            case "M04":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMT", tx_amt);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", trOutActno);  //FEPTXN_TROUT_ACTNO
                break;
            case "M05":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMT", tx_amt);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", frmAct + trOutActno);  //FEPTXNTCB_FROMACT+FEPTXN_TROUT_ACTNO
                break;
            case "M06":
                parameter.put("TX_DATE", tx_date);
                parameter.put("ApSysKey", idNo + trOutActno);  //FEPTXN_IDNO+FEPTXN_TROUT_ACTNO
                break;
            case "M07":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMT", tx_amt);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", trOutActno);  //FEPTXN_TROUT_ACTNO
                break;
            case "M08":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", idNo + trOutActno);  //FEPTXN_IDNO+FEPTXN_TROUT_ACTNO
                break;
            case "M09":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", idNo + trOutActno);  //FEPTXN_IDNO+FEPTXN_TROUT_ACTNO
                break;
            case "M10":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_ACTNO3", tx_actno3);
                parameter.put("ApSysKey", idNo + trOutActno);  //FEPTXN_IDNO+FEPTXN_TROUT_ACTNO
                break;
            case "M11":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("ApSysKey", trOutActno + trK2);  //FEPTXN_TROUT_ACTNO+LEFT(FEPTXN_TRK2,16)
                break;
            case "M12":
                parameter.put("TX_DATE", tx_date);
                parameter.put("TX_ATM4", tx_amt4);
                parameter.put("ApSysKey", idNo + trOutActno); //FEPTXN_IDNO+FEPTXN_TROUT_ACTNO
                break;
            default:
                break;
        }

        try {
            notifyHelper.sendSimpleSMS(templateId, mo, parameter, true, getLogContext().getEj()); // 2025-04-09 Richard modified for [Cleartext Submission of Sensitive Information]
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendSMS"));
            getLogContext().setRemark("sendSimpleSMS 異常.");
            logMessage(getLogContext());
            throw ExceptionUtil.createRuntimeException(e);
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 2024-05-00 Joe add PUSH推播 (使用Notify)
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode SendPush(Feptxntcb feptxntcb, Feptxn feptxn) { //Push  Notify
        getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendPush"));
        getLogContext().setRemark("SendPush Start.");
        logMessage(getLogContext());

        //若是feptxntcb getFeptxntcbTxDate or getFeptxntcbEjfno 為空值或NULL，則重新讀若是feptxntcb
        if (StringUtils.isBlank(feptxntcb.getFeptxntcbTxDate()) || feptxntcb.getFeptxntcbEjfno() == null) {
            try {
                feptxntcb = feptxnDao.selectByPrimaryKeyForFeptxntcb(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
                if (feptxntcb.getFeptxntcbTxDate().equals(feptxn.getFeptxnTxDate()) && feptxntcb.getFeptxntcbEjfno().equals(feptxn.getFeptxnEjfno())) {
                    getLogContext().setRemark("SendPush Data is Valid.");
                    logMessage(getLogContext());
                } else {
                    getLogContext().setRemark("SendPush 有異常." + FEPReturnCode.FEPTXNNotFound.toString());
                    return FEPReturnCode.FEPTXNNotFound;
                }
            } catch (Exception e) {
                getLogContext().setProgramException(e);
                getLogContext().setMessage("Load FEPTXNTCB error" + FEPReturnCode.FEPTXNNotFound.toString());
                getLogContext().setRemark("SendPush Error.");
                logMessage(getLogContext());
                return FEPReturnCode.FEPTXNNotFound;
            }
        }

        getLogContext().setRemark("FEPTXNTCB Data");
        getLogContext().setMessage("feptxntcb FeptxntcbTxDate:" + feptxntcb.getFeptxntcbTxDate() + ", feptxn FeptxnTxDate:" + feptxn.getFeptxnTxDate()
                + "; feptxntcb FeptxntcbEjfno:" + feptxntcb.getFeptxntcbEjfno() + ", feptxn FeptxnEjfno:" + feptxn.getFeptxnEjfno());
        logMessage(getLogContext());

        Map<String, String> parameter = new HashMap<>();
        String templateId = feptxntcb.getFeptxntcbNoticeNumber();
        String personId = feptxntcb.getFeptxntcbNoticeCusid();
        String tx_datehm = null; // yyy/mm/dd hh:mm (交易時間)
        String tx_datehmc = null; // YYY年MM月DD日HH時MM分 (含中文交易時間)
        int tx_amt_int = 0;
        String tx_actno3 = null; // XXX (帳號末三碼)
//            String tx_dxtvc = null;
//            String tx_date = null;
        String tx_amts = null; // ZZZZ,ZZ9 (含三分位交易金額)

        if (feptxn.getFeptxnReqDatetime() != null) {
            tx_datehm = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "/" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + " " +
                    feptxn.getFeptxnReqDatetime().substring(8, 10) + ":" +
                    feptxn.getFeptxnReqDatetime().substring(10, 12);
            tx_datehmc = (Integer.parseInt(feptxn.getFeptxnReqDatetime().substring(0, 4)) - 1911) + "年" +
                    feptxn.getFeptxnReqDatetime().substring(4, 6) + "月" +
                    feptxn.getFeptxnReqDatetime().substring(6, 8) + "日" +
                    feptxn.getFeptxnReqDatetime().substring(8, 10) + "時" +
                    feptxn.getFeptxnReqDatetime().substring(10, 12) + "分";
        }

        if (feptxntcb.getFeptxntcbFromact() != null) {
            //tx_actno3 = feptxntcb.getFeptxntcbFromact().substring(feptxntcb.getFeptxntcbFromact().length()-3);
            //2024-05-24 先移除空白再取後3碼
            tx_actno3 = feptxntcb.getFeptxntcbFromact().trim().substring(feptxntcb.getFeptxntcbFromact().trim().length() - 3);
        }
        tx_amt_int = feptxn.getFeptxnTxAmtAct().intValue();
        //2024-05-31 合庫特殊格式僅加一個千分位
        if (tx_amt_int > 999) {
            tx_amts = Integer.toString(tx_amt_int).substring(0, Integer.toString(tx_amt_int).length() - 3) + "," + Integer.toString(tx_amt_int).substring(Integer.toString(tx_amt_int).length() - 3);
        } else {
            tx_amts = Integer.toString(tx_amt_int);
        }
        switch (templateId) {
            case "P01":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("TX_ACTNO3", tx_actno3);
                break;
            case "P02":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("TX_ACTNO3", tx_actno3);
                break;
            case "P03":
                parameter.put("TX_DATEHM", tx_datehm);
                parameter.put("TX_AMTS", tx_amts);
                parameter.put("TX_ACTNO3", tx_actno3);
                break;
            case "P04":
                parameter.put("TX_DATEHMC", tx_datehmc);
                parameter.put("TX_AMTS", tx_amts);
                break;
            case "P05":
                parameter.put("TX_DATEHMC", tx_datehmc);
                parameter.put("TX_AMTS", tx_amts);
                break;
            default:
                break;
        }
        try { //PUSH 請求通知中心
            notifyHelper.sendSimplePush(templateId, personId, parameter, true, getLogContext().getEj());
        } catch (Exception e) {
            getLogContext().setProgramException(e);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".SendPush"));
            getLogContext().setRemark("sendSimplePush 異常.");
            logMessage(getLogContext());
            throw ExceptionUtil.createRuntimeException(e);
        }
        return FEPReturnCode.Normal;
    }


    /**
     * 20220914 Bruce add PUSH推播
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode preparePush(Feptxn feptxn) throws Exception {
        return this.prepareShared(feptxn, "P");
    }

    /**
     * 20220914 Bruce add 簡訊
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode prepareSms(Feptxn feptxn) throws Exception {
        return this.prepareShared(feptxn, "M");
    }

    /**
     * 20220914 Bruce add Email
     *
     * @param feptxn
     * @return
     */
    public FEPReturnCode prepareMail(Feptxn feptxn) throws Exception {
        return this.prepareShared(feptxn, "E");
    }

    /**
     * 20220915 Bruce add 推播共用模組
     *
     * @param feptxn
     * @param noticeType
     * @return
     */
    private FEPReturnCode prepareShared(Feptxn feptxn, String noticeType) throws Exception {
//        //取得簡訊資料檔
//        Smsmsg smsMsg = smsmsgMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());
//        if (smsMsg == null) {
//            getLogContext().setRemark("SMSMSG查無資料");
//            logMessage(Level.INFO, getLogContext());
//            return CommonReturnCode.Normal;
//        }
//
//        //判斷是否有電話號碼、email
//        if (StringUtils.isBlank(smsMsg.getSmsmsgNumber()) || StringUtils.isBlank(smsMsg.getSmsmsgEmail())) {
//            switch (noticeType) {
//                case "P":
//                case "M":
//                    getLogContext().setRemark("查無電話號碼 不需送 SMS");
//                    logMessage(Level.INFO, getLogContext());
//                    return CommonReturnCode.Normal;
//                case "E":
//                    getLogContext().setRemark("查無EMAIL 不需送 SMS");
//                    logMessage(Level.INFO, getLogContext());
//                    return CommonReturnCode.Normal;
//            }
//        }
//        smsMsg.setSmsmsgSend("Y");
//        //更新smsMsg檔
//        try {
//            smsmsgMapper.updateByPrimaryKeySelective(smsMsg);
//        } catch (Exception e) {
//            getLogContext().setRemark("SMSMSG更新失敗");
//            logMessage(Level.INFO, getLogContext());
//            return CommonReturnCode.Normal;
//        }
//        //將SMS訊息送至 NotifyHelper
//        NotifyData data = new NotifyData();
//        data.setTxDate(smsMsg.getSmsmsgTxDate());
//        data.setTxTime(smsMsg.getSmsmsgTxTime());
//        data.setEj(smsMsg.getSmsmsgEjfno());
//        switch (noticeType) {
//            case "P":
//                data.setSmlSeqNoForApp(1);
//                break;//push
//            case "M":
//                data.setSmlSeqNoForSMS(1);
//                break;
//            case "E":
//                data.setSmlSeqNoForMail(1);
//                break;
//        }
//        data.getParameterData().put("[PARAM1]", smsMsg.getSmsmsgTxDate() + smsMsg.getSmsmsgTxTime());
//        data.getParameterData().put("[PARAM2]", smsMsg.getSmsmsgTxAmtAct().toString());
//        data.getParameterData().put("[PARAM3]", smsMsg.getSmsmsgTroutActno().substring(smsMsg.getSmsmsgTroutActno().length() - 3, smsMsg.getSmsmsgTroutActno().length()));
//        NotifyHelper notify = new NotifyHelper(data);
//        notify.send();
        return CommonReturnCode.Normal;
    }

    /**
     * 負責處理 ATM 跨行交易狀態檢核
     *
     * @param pcode      FEPTxn
     * @param isAcquirer isAcquirer
     * @return <history>
     * <modify>
     * <modifier>HENNY</modifier>
     * <reason>Function Add</reason>
     * <date>2009/11/27</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkINBKStatus(String pcode, boolean isAcquirer) {
        FEPReturnCode rc_MBOCT = null;
        FEPReturnCode rc_MBACT = null;
        String wk_AOCT = null;
        String wk_MBACT = null;
        String wk_LOG = "";

        try {
            if (isAcquirer) { // 代理行{
                rc_MBOCT = FEPReturnCode.SenderBankOperationStop; /*發信單位主機未在跨行作業運作狀態*/
                rc_MBACT = FEPReturnCode.SenderBankServiceStop; /*發信單位該項跨行業務停止或暫停營業*/
            } else { //原存行
                rc_MBOCT = FEPReturnCode.ReceiverBankOperationStop; /*收信單位主機未在跨行作業運作狀態*/
                rc_MBACT = FEPReturnCode.ReceiverBankServiceStop; /*收信單位該項跨行業務停止或暫停營業*/
            }

            // Check M-BANK 及 FISC 連線狀態:
            // Check M-BANK OP status回應 0206 或 0203
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
                getLogContext().setRemark("CheckINBKStatus-本行系統主機未在跨行作業運作狀態");
                this.logMessage(getLogContext());
                return rc_MBOCT;
            }

            // Check FISC OP status 回應 0702
            if (!"1".equals(SysStatus.getPropertyValue().getSysstatSoct())) {
                getLogContext().setRemark("CheckINBKStatus-財金公司主機未在跨行作業運作狀態");
                this.logMessage(getLogContext());
                return FEPReturnCode.FISCOperationStop; /*財金公司主機未在跨行作業運作狀態*/
            }

            // 依交易類別(提款/轉帳/繳款),檢核財金及本行各作業連線狀態
            switch (pcode.substring(0, 3)) {
                case "250": //晶片卡 餘額查詢交易
                case "251": //晶片卡 跨行提款交易
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2510();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2510();
                    wk_LOG = "晶片卡提款作業(2510)";
                    break;
                case "252": // 晶片卡 轉帳交易
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2520();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2520();
                    wk_LOG = "晶片卡轉帳作業(2520)";
                    break;
                case "253": // 晶片卡 繳款交易
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2530();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2530();
                    wk_LOG = "晶片卡繳款作業(2530)";
                    break;
                case "254": // 晶片卡 消費扣款(變動費率)
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2540();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2540();
                    wk_LOG = "晶片卡消費扣款作業(2540)";
                    break;
                case "255": // 晶片卡 預先授權
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2550();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2550();
                    wk_LOG = "晶片卡預先授權作業(2550)";
                    break;
                case "256": // 全國性繳費
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2560();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2560();
                    wk_LOG = "晶片卡全國繳費作業(2560)";
                    break;
                case "257": // 晶片卡 跨國提款
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2570();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2570();
                    wk_LOG = "晶片卡跨國提款作業(2570)";
                    break;
                case "226": // CD/ATM 全國性繳費
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2200();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2000();
                    wk_LOG = "CD/ATM 共用系統轉帳作業(2200)";
                    break;
                default:
                    // Fly 2015/12/23 修改 for EMV 晶片卡交易
                    if ("24".equals(pcode.substring(0, 2)) || "26".equals(pcode.substring(0, 2))) {
                        // CD/ATM 國際提款交易
                        wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2000();
                        wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2000();
                        wk_LOG = "CD/ATM 共用系統提款作業(2000)";
                    } else {
                        return FEPReturnCode.Normal;
                    }
                    break;
            }

            // 晶片卡需先 Check 所有共用子系統 (M-BANK 及 FISC AP STATUS)
            if ("25".equals(pcode.substring(0, 2)) || "22".equals(pcode.substring(0, 2))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2500())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統(2500) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //發信單位該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2500())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統(2500) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務暫停或停止營業
                }
            }
            // Check M-BANK AP status 回應 0205 或 0202
            if ("2".equals(wk_MBACT)) {
                getLogContext().setRemark("本行" + wk_LOG + " AP EX-CHECK-OUT");
                this.logMessage(getLogContext());
                return rc_MBACT; //發信單位該項跨行業務停止或暫停營業
            }

            // Check FISC AP STATUS 回應 0701
            if ("2".equals(wk_AOCT)) {
                // 2014/11/04 Modify by Ruling 加記錄log
                getLogContext().setRemark("財金" + wk_LOG + " AP停止作業");
                this.logMessage(getLogContext());
                return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務暫停或停止營業
            }

            return FEPReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkINBKStatus");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 檢核委託單位代號檔
     *
     * @param feptxn feptxn
     *
     *               <history>
     *               <modify>
     *               <modifier>Henny</modifier>
     *               <reason></reason>
     *               <date>2010/4/13</date>
     *               </modify>
     *               <modify>
     *               <modifier>Husan</modifier>
     *               <reason>查詢條件只檢核委託單位代號、繳款類別</reason>
     *               <date>2011/10/04</date>
     *               </modify>
     *               </history>
     * @return
     */
    public FEPReturnCode checkNpsunit(Feptxn feptxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Npsunit npsunit = new Npsunit();
        try {
            if (!RRN30000Trans.equals(feptxn.getFeptxnFiscRrn()) || !PAYTYPE30000Trans.equals(feptxn.getFeptxnPaytype())) {// 非ATM移轉
                // 以檢核委託單位代號、繳款類別、費用代號讀取NPSUNIT
                npsunit.setNpsunitNo(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
                npsunit.setNpsunitPaytype(feptxn.getFeptxnPaytype()); // 繳款類別
                npsunit.setNpsunitFeeno(feptxn.getFeptxnPayno()); // 費用代號
                npsunit = npsunitMapper.selectByPrimaryKey(npsunit.getNpsunitNo(), npsunit.getNpsunitPaytype(), npsunit.getNpsunitFeeno());
                if (npsunit == null) {
                    return FISCReturnCode.NPSNotFound; // 委託單位代號錯誤(NPSNotFound)
                }

                // 將帳務代理行手續費寫入FEPTXN
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(StringUtils.leftPad(npsunit.getNpsunitBkno(), 7, '0').substring(0, 3))) {
                    feptxn.setFeptxnNpsAgbFee(npsunit.getNpsunitOtherFee1());
                }

                // 全國性繳費-帳務代理交易
                if ((FISCPCode.PCode2263.getValueStr().equals(feptxn.getFeptxnPcode())
                        || FISCPCode.PCode2264.getValueStr().equals(feptxn.getFeptxnPcode())
                        || FISCPCode.PCode2563.getValueStr().equals(feptxn.getFeptxnPcode())
                        || FISCPCode.PCode2564.getValueStr().equals(feptxn.getFeptxnPcode()))
                        && FEPChannel.FISC.name().equals(feptxn.getFeptxnChannel())) {
                    // 全國性繳費-帳務代理交易
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(npsunit.getNpsunitBkno().substring(0, 3))) {
//                        feptxn.setFeptxnChannel(FEPChannel.EBILL.name());
                        // 全國性繳費帳務代理(2263/2563/2264/2564)
                        feptxn.setFeptxnReconSeqno(StringUtils.rightPad(feptxn.getFeptxnRemark(), 20, ' ').substring(4, 20)); // 銷帳編號
                        // 全國性繳費帳務代理(2263/2264)
                        if (FISCPCode.PCode2264.getValueStr().equals(feptxn.getFeptxnPcode())
                                && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) {
                            //2264 交易轉入行為本行, 身份證號由附言欄拆出
                            feptxn.setFeptxnIdno(feptxn.getFeptxnRemark().substring(28, 39));
                            if (StringUtils.isBlank(feptxn.getFeptxnIdno())) {
                                getLogContext().setRemark("身份證號及統編之值為空或NULL");
                                this.logMessage(getLogContext());
                                return FISCReturnCode.CheckIDNOError; //身分證號有誤(4806)
                            }
                        }
                    }
                }

                // 全國性繳費業務之保險費強化機制：226X繳保險費，本行為轉出時，需檢核保險識別編號(Bitmap48)是否存在
                if ("226".equals(feptxn.getFeptxnPcode().substring(0, 3))
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
                    String insPayType = INBKConfig.getInstance().getINSPayType();
                    if (StringUtils.isNotBlank(insPayType)) {
                        String[] insPayTypes = insPayType.split(";");
                        if (ArrayUtils.isNotEmpty(insPayTypes)) {
                            for (int i = 0; i < insPayTypes.length; i++) {
                                if (insPayTypes[i] != null && insPayTypes[i].equals(feptxn.getFeptxnPaytype())) {
                                    if (feptxn.getFeptxnRemark().length() < 38 || StringUtils.isBlank(feptxn.getFeptxnRemark().substring(28, 38))) {
                                        getLogContext().setRemark("保險識別編號(Bitmap48)欄位長度小於38, 或值為空白");
                                        this.logMessage(getLogContext());
                                        return FISCReturnCode.MessageFormatError; //訊息格式或內容編輯錯誤(0101)
                                    }
                                }
                            }
                        }
                    } else {
                        LogHelperFactory.getTraceLogger().info("SYSCONF無此資料, SubSystemNo = 1 and SysconfName = \"INSPayType\"");
                    }
                }
                getFISCTxData().setNpsunit(npsunit);
            }

            if ("Y".equals(npsunit.getNpsunitMonthlyFg())) {
                // 月結
                feptxn.setFeptxnNpsMonthlyFg(DbHelper.toShort(true));
            }

            //繳費類別(00000~49999)為委託單位付費
            if (Double.valueOf(feptxn.getFeptxnPaytype()) >= 0 && Double.valueOf(feptxn.getFeptxnPaytype()) <= 49999) { // 委託單位付費
                if ("1".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //繳費作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) { //轉入單位代理清算應付手續費
                    feptxn.setFeptxnNpsClr((short) 2); //手續費清算單位為轉入行
                } else if ("2".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //發放作業
                        && feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {//轉出單位代理清算應付手續費
                    feptxn.setFeptxnNpsClr((short) 1); //手續費清算單位為轉出行
                }
                //繳費類別(50000~99999)為使用者付費
            } else if (Double.valueOf(feptxn.getFeptxnPaytype()) >= 50000 && Double.valueOf(feptxn.getFeptxnPaytype()) <= 99999) { // 使用者付費
                if ("1".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //繳費作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {//轉出單位代理清算應付手續費
                    feptxn.setFeptxnNpsClr((short) 1); //手續費清算單位為轉出行
                } else if ("2".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //發放作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) {//轉入單位代理清算應付手續費
                    feptxn.setFeptxnNpsClr((short) 2); //手續費清算單位為轉入行
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkNPSUNIT");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 檢核汽燃費之費用代號
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>New for 繳費網PHASEI</reason>
     * <date>2016/01/04</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkFuelPayType() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String trinBkno = "";

        try {
            // 檢核汔燃費之委託單位代號及類別代號
            if (!getFeptxn().getFeptxnBusinessUnit().equals("10000002") || !getFeptxn().getFeptxnPaytype().equals("40005")) {
                getLogContext().setRemark("CheckFuelPayType-FEPTXN_BUSINESS_UNIT<>'10000002' 或 FEPTXN_PAYTYPE<>'40005', FEPTXN_BUSINESS_UNIT=" + getFeptxn().getFeptxnBusinessUnit()
                        + ", FEPTXN_PAYTYPE=" + getFeptxn().getFeptxnPaytype());
                this.logMessage(getLogContext());
                rtnCode = FISCReturnCode.NPSNotFound;
                return rtnCode;
            }

            // 檢核汽燃費之費用代號
            // 檢核銷帳編號不得為空白
            if (StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
                getLogContext().setRemark("CheckFuelPayType-銷帳編號(FEPTXN_RECON_SEQNO)不得為空白");
                this.logMessage(getLogContext());
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }

            // 2017/06/15 Modify by Ruling for 配合WEBATM新增繳汽燃費功能(增加OFT/OFF電文)：WEBATM 比照實體ATM通路檢核委託單位代號檔
            // 依通路檢核費用代號前三碼
            if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())) {
                // 檢核實體ATM-汽燃費費用代號前三碼
                if (!getFeptxn().getFeptxnPayno().substring(0, 3).equals(INBKConfig.getInstance().getFuelATMPAYNO3())) {
                    String memark = "";
                    if (StringUtils.isBlank(INBKConfig.getInstance().getFuelNBPAYNO3())) {
                        memark = "INBKConfig FuelNBPAYNO3 為空值 ";
                    } else {
                        memark = "CheckFuelPayType-實體/WEB ATM汽燃費費用代號前三碼<>" + INBKConfig.getInstance().getFuelATMPAYNO3() + ", FEPTXN_PAYNO=" + getFeptxn().getFeptxnPayno();
                    }
                    getLogContext().setRemark(memark);
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.OtherCheckError;
                    return rtnCode;
                }
            } else {
                // 檢核網路通路-汽燃費費用代號前三碼
                if (!getFeptxn().getFeptxnPayno().substring(0, 3).equals(INBKConfig.getInstance().getFuelNBPAYNO3())) {
                    String memark = "";
                    if (StringUtils.isBlank(INBKConfig.getInstance().getFuelNBPAYNO3())) {
                        memark = "INBKConfig FuelNBPAYNO3 為空值 ";
                    } else {
                        memark = "CheckFuelPayType-網路通路汽燃費費用代號前三碼<>" + INBKConfig.getInstance().getFuelNBPAYNO3() + ", FEPTXN_PAYNO=" + getFeptxn().getFeptxnPayno();
                    }
                    getLogContext().setRemark(memark);
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.OtherCheckError;
                    return rtnCode;
                }
            }

            char thirdChar = getFeptxn().getFeptxnReconSeqno().charAt(2);
            /* 檢核銷帳編號第三碼必須為7區監理所代碼(2~8) */
            if (!(thirdChar == '2' || thirdChar == '3' || thirdChar == '4' || thirdChar == '5' || thirdChar == '6' || thirdChar == '7' || thirdChar == '8')) {
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }

            // 檢核汽燃費之轉入銀行
            if (!FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel()) || !FEPChannel.FISC.name().equals(getFeptxn().getFeptxnChannel())) {
                Npsunit conditionOfNpsunit = new Npsunit();
                conditionOfNpsunit.setNpsunitNo(getFeptxn().getFeptxnBusinessUnit());
                conditionOfNpsunit.setNpsunitPaytype(getFeptxn().getFeptxnPaytype());
                conditionOfNpsunit.setNpsunitFeeno(getFeptxn().getFeptxnPayno());
                Npsunit resultOfNpsunit = npsunitMapper.selectByPrimaryKey(conditionOfNpsunit.getNpsunitNo(), conditionOfNpsunit.getNpsunitPaytype(), conditionOfNpsunit.getNpsunitFeeno());
                if (resultOfNpsunit == null) {
                    // NPSUNIT無資料
                    getLogContext().setRemark(StringUtils.join(
                            "CheckFuelPayType-NPSUNIT找不到資料, NPSUNIT_NO=", conditionOfNpsunit.getNpsunitNo(),
                            ", NPSUNIT_PAYTYPE=", conditionOfNpsunit.getNpsunitPaytype(),
                            ", NPSUNIT_FEENO=", conditionOfNpsunit.getNpsunitFeeno()));
                    this.logMessage(getLogContext());
                    rtnCode = FISCReturnCode.NPSNotFound;
                    return rtnCode;
                } else {
                    // NPSUNIT有資料
                    if (StringUtils.isBlank(resultOfNpsunit.getNpsunitTrinBkno())) {
                        trinBkno = "000";
                    } else {
                        if (resultOfNpsunit.getNpsunitTrinBkno().trim().length() >= 3) {
                            trinBkno = resultOfNpsunit.getNpsunitTrinBkno().trim().substring(0, 3);
                        } else {
                            trinBkno = StringUtils.rightPad(resultOfNpsunit.getNpsunitTrinBkno().trim(), 3, '0').substring(0, 3);
                        }
                    }
                }
                if (!trinBkno.equals(getFeptxn().getFeptxnTrinBkno())) {
                    getLogContext().setRemark("CheckFuelPayType-轉入銀行別錯誤, FEPTXN_TRIN_BKNO=" + getFeptxn().getFeptxnTrinBkno() + ", 正確應為=" + trinBkno);
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.OtherCheckError;
                    return rtnCode;
                }

            }

            // 2016/02/04 Modify by Ruling for 從上面依網銀通路移出來因ATM也會共用
            // 檢核銷帳編號第三碼必須為7區監理所代碼(2~8)
            if ("2,3,4,5,6,7,8".indexOf(getFeptxn().getFeptxnReconSeqno().substring(2, 3)) == -1) {
                getLogContext().setRemark("CheckFuelPayType-銷帳編號第三碼必須為7區監理所代碼(2~8), FEPTXN_RECON_SEQNO=" + getFeptxn().getFeptxnReconSeqno());
                this.logMessage(getLogContext());
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }
            // 2016/02/04 Modify by Ruling for 加檢核銷帳編號第三碼必須等於費用代號第四碼
            // 檢核銷帳編號第三碼必須等於費用代號第四碼
            if (!getFeptxn().getFeptxnReconSeqno().substring(2, 3).equals(getFeptxn().getFeptxnPayno().substring(3, 4))) {
                getLogContext().setRemark("CheckFuelPayType-銷帳編號第三碼必須等於費用代號第四碼, FEPTXN_RECON_SEQNO=getFeptxn().getFeptxnReconSeqno(), FEPTXN_PAYNO=" + getFeptxn().getFeptxnPayno());
                this.logMessage(getLogContext());
                rtnCode = CommonReturnCode.RECNOError;
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkFuelPayType");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 將ATM_TITA.TITA電文相關欄位, 準備寫入 FEPTxn、VATXN
     *
     * @return
     */
    public FEPReturnCode VAPrepareFEPTXN() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        Zone defZone = new Zone();
        try {
            RCV_VA_GeneralTrans_RQ vaReq = mNBtxData.getTxVafepObject().getRequest();

            getFeptxn().setFeptxnTxDate(vaReq.getBody().getRq().getSvcRq().getINDATE()); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(vaReq.getBody().getRq().getSvcRq().getINTIME()); // 交易時間
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());

//            String roc = CalendarUtil.rocStringToADString(vaReq.getBody().getRq().getSvcRq().getAEICDAY());
            String roc = vaReq.getBody().getRq().getSvcRq().getAEICDAY();
            if (StringUtils.isNotBlank(roc) && roc.length() == 8) {
                getFeptxn().setFeptxnTxDatetimeFisc(roc + vaReq.getBody().getRq().getSvcRq().getAEICTIME());
            } else {
                roc = CalendarUtil.rocStringToADString(roc);
                getFeptxn().setFeptxnTxDatetimeFisc(FormatUtil.dateTimeFormat(CalendarUtil.parseDateTimeValue(Integer.valueOf(roc), Integer.valueOf(vaReq.getBody().getRq().getSvcRq().getAEICTIME() + "000")), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            }


            getFeptxn().setFeptxnChannel(vaReq.getBody().getRq().getHeader().getCHANNEL()); //通道別
            getFeptxn().setFeptxnMsgid(mNBtxData.getMsgCtl().getMsgctlMsgid()); // 訊息代號
            getFeptxn().setFeptxnCbsProc(mNBtxData.getMsgCtl().getMsgctlCbsProc());
            getFeptxn().setFeptxnMsgkind(vaReq.getBody().getRq().getHeader().getMSGKIND());//訊息種類
            getFeptxn().setFeptxnAtmBrno(vaReq.getBody().getRq().getHeader().getBRANCHID());//分行代號
            getFeptxn().setFeptxnMsgflow("A1");
            getFeptxn().setFeptxnChannelEjfno(vaReq.getBody().getRq().getHeader().getCLIENTTRACEID());//外部通道電子日誌序號
            getFeptxn().setFeptxnTxDateAtm(vaReq.getBody().getRq().getSvcRq().getINDATE());//交易日期
            getFeptxn().setFeptxnEjfno(mNBtxData.getEj()); // 電子日誌序號
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());//交易啟動銀行
            getFeptxn().setFeptxnTxCode(vaReq.getBody().getRq().getSvcRq().getFSCODE());//交易種類
            getFeptxn().setFeptxnPcode(vaReq.getBody().getRq().getSvcRq().getPCODE());
            getFeptxn().setFeptxnAtmno(vaReq.getBody().getRq().getSvcRq().getTERMINALID());//端末機代號
            String ipaddr = vaReq.getBody().getRq().getSvcRq().getIPADDR();
            getFeptxn().setFeptxnClientip(ipaddr.length() > 17 ? ipaddr.substring(0, 17) : ipaddr);//使用者登入IP
            getFeptxn().setFeptxnAtmType(vaReq.getBody().getRq().getSvcRq().getTERMINAL_TYPE());//端末設備型態
            String terminalCheckno = vaReq.getBody().getRq().getSvcRq().getTERMINAL_CHECKNO();
            getFeptxn().setFeptxnAtmChk(StringUtils.isBlank(terminalCheckno) ? "00000000" : terminalCheckno);//端末設備查核碼
            getFeptxn().setFeptxnIdno(vaReq.getBody().getRq().getSvcRq().getTAXIDNO()); //客戶IDNO
            getFeptxn().setFeptxnTxCurAct("TWD");
            getFeptxn().setFeptxnTxCur("TWD");
            getFeptxn().setFeptxnTxAmt(vaReq.getBody().getRq().getSvcRq().getTRANSAMT());//轉帳金額
            getFeptxn().setFeptxnTxrust("0");//處理結果(初始預設值)
            getFeptxn().setFeptxnTroutBkno(vaReq.getBody().getRq().getSvcRq().getAEIPCRBK());//轉出帳號銀行別
            if (defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
            }

            if ("02".equals(vaReq.getBody().getRq().getSvcRq().getVACATE())) {
                if (!"03".equals(vaReq.getBody().getRq().getSvcRq().getAEIPYTP())) {
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(vaReq.getBody().getRq().getSvcRq().getAEIPCRBK())) {
                        getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
                    } else {
                        getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
                    }
                } else {
                    Npsunit npsunit = dbNPSUNIT.selectByNoAndPayType(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
                    if (npsunit != null) {
                        String outbkno = npsunit.getNpsunitBkno().substring(0, 3);
                        getFeptxn().setFeptxnTroutBkno(outbkno);
                        if (StringUtils.startsWith(outbkno, SysStatus.getPropertyValue().getSysstatHbkno())) {
                            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));//自行記號
                        } else {
                            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));//跨行記號
                        }
                    } else {
                        getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));
                    }
                }
            } else {
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(vaReq.getBody().getRq().getSvcRq().getAEIPCRBK())) {
                    getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
                } else {
                    getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
                }
            }


            //代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(mNBtxData.getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());            //財金營業日
            } else {
                /* 2/4 修改判斷為代理交易 */
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) &&
                        StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())
                ) {
                    getFeptxn().setFeptxnTbsdyFisc(mNBtxData.getTbsdyFISC());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }

            defZone = zoneExtMapper.getDataByZonee("TWN");
            if (StringUtils.isNotBlank(defZone.getZoneTbsdy())) {
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy());    //自行營業日
            }
            getFeptxn().setFeptxnTbsdyAct(getFeptxn().getFeptxnTbsdy());//卡片所在地區營業日
            getFeptxn().setFeptxnSubsys(mNBtxData.getMsgCtl().getMsgctlSubsys());    //系統別

            if (DbHelper.toBoolean(mNBtxData.getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                // 判斷自行及 ATM 2 WAY Flag
                if (!DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && DbHelper.toBoolean(mNBtxData.getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnWay((short) 2);
                } else {
                    getFeptxn().setFeptxnWay((short) 3);
                }
            }

//            getFeptxn().setFeptxnNoticeId(vaReq.getBody().getRq().getSvcRq().getVACATE() + vaReq.getBody().getRq().getSvcRq().getAEIPYTP());
            if (StringUtils.equalsIgnoreCase(vaReq.getBody().getRq().getHeader().getCHANNEL(), "FID")) {
                getFeptxn().setFeptxnTrinBkno(vaReq.getBody().getRq().getSvcRq().getAEIPYBK());//AEIPYBK
            }

            getFeptxn().setFeptxnTroutActno(vaReq.getBody().getRq().getSvcRq().getAEIPYAC());/*約定時，此欄位為空白*/
            getFeptxn().setFeptxnMajorActno(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC());
            getFeptxn().setFeptxnIcSeqno(vaReq.getBody().getRq().getSvcRq().getIC_SEQNO());
            getFeptxn().setFeptxnIcTac(vaReq.getBody().getRq().getSvcRq().getIC_TAC());
            if (StringUtils.isNotBlank(vaReq.getBody().getRq().getSvcRq().getICMARK())) {
                getFeptxn().setFeptxnIcmark(vaReq.getBody().getRq().getSvcRq().getICMARK());//卡片備註欄
            } else {
                getFeptxn().setFeptxnIcmark(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getICMARK(), 60, "40"));
            }
            StringBuffer str = new StringBuffer();
            String AEIPYTP = vaReq.getBody().getRq().getSvcRq().getAEIPYTP();
            if ("02".equals(vaReq.getBody().getRq().getSvcRq().getVACATE())) {
                str = new StringBuffer();
                Npsunit npsunit = dbNPSUNIT.selectByNoAndPayType(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE());
                if (npsunit != null) {
                    if (StringUtils.equals(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), "01")) {
                        //業務類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        //交易類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        //身分證號或營利事業統一編號 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        //統一編號 8char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI(), 8, ' '));
                        //委託單位代號 8char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), 8, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitNo(), 8, ' '));
                        //機構代號 5char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), 5, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitPaytype(), 5, ' '));
                        //費用代號 4char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO(), 4, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitFeeno(), 4, ' '));
                        //行動電話 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        //電子支付帳戶帳號 16char(根據3-1成功電文此處填身分證)
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, '0'));
                        //約定扣款帳號 16char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC(), 16, '0'));
                    } else if (StringUtils.equalsAny(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), "03")) {
                        //業務類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        //交易類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        //身分證號或營利事業統一編號 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        //統一編號 8char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI(), 8, ' '));
                        //委託單位代號 8char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), 8, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitNo(), 8, ' '));
                        //機構代號 5char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), 5, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitPaytype(), 5, ' '));
                        //費用代號 4char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO(), 4, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitFeeno(), 4, ' '));
                        //行動電話 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        //電子支付帳戶帳號
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC(), 16, '0'));
                        //約定扣款帳號 16char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYAC(), 16, '0'));
                    } else if (StringUtils.equalsAny(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), "00")) {
                        //業務類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        //交易類別 2char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        //身分證號或營利事業統一編號 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        //統一編號 8char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI(), 8, ' '));
                        //委託單位代號 8char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), 8, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitNo(), 8, ' '));
                        //機構代號 5char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), 5, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitPaytype(), 5, ' '));
                        //費用代號 4char
//                    str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO(), 4, ' '));
                        str.append(StringUtils.rightPad(npsunit.getNpsunitFeeno(), 4, ' '));
                        //行動電話 10char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        //電子支付帳戶帳號
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC(), 16, '0'));
                        //約定扣款帳號 16char
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAEIPYAC2(), 16, '0'));
                    }
                }
                if (str.length() < 100) {
                    str.append(StringUtils.rightPad("", 100 - str.length(), ' '));
                }
                getFeptxn().setFeptxnTrk3(str.toString());

                if (StringUtils.equals(AEIPYTP, "00")) {
                    //約定
                    getFeptxn().setFeptxnTroutActno(StringUtils.leftPad(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC(), 16, "0"));
                    getFeptxn().setFeptxnMajorActno(StringUtils.leftPad(vaReq.getBody().getRq().getSvcRq().getAEIPCRAC(), 16, "0"));
                } else if (StringUtils.equalsAny(AEIPYTP, "01", "03")) {
                    //撤銷
                    getFeptxn().setFeptxnTroutActno(StringUtils.leftPad(vaReq.getBody().getRq().getSvcRq().getAEIPYAC(), 16, "0"));
                    getFeptxn().setFeptxnMajorActno(StringUtils.leftPad(vaReq.getBody().getRq().getSvcRq().getAEIPYAC(), 16, "0"));
                }
            } else {
                String tp = vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP();
                getFeptxn().setFeptxnTrk3(null);
                str = new StringBuffer();
                switch (tp) {
                    case "00"://除卡片及帳號外，無其他核驗項目
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        break;
                    case "01"://身份證號或外國人統一編號
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        break;
                    case "02"://持卡人之行動電話號碼
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        break;
                    case "03"://持卡人之出生年月日
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY(), 8, ' '));
                        break;
                    case "04":
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME(), 10, ' '));
                        break;
                    case "11":///持卡人之身分證號及行動電話號碼
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        break;
                    case "12":
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY(), 8, ' '));
                        break;
                    case "13":
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME(), 10, ' '));
                        break;
                    case "14":
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getMOBILENO(), 10, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getBIRTHDAY(), 8, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getTELHOME(), 10, ' '));
                        break;
                    case "15":
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getVACATE(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getSENDDATA().getAELFTP(), 2, ' '));
                        str.append(StringUtils.rightPad(vaReq.getBody().getRq().getSvcRq().getTAXIDNO(), 10, ' '));
                        break;
                }

                if (str.length() < 100) {
                    str.append(StringUtils.rightPad("", 100 - str.length(), ' '));
                }
                //2024-11-04 改判段欄位 00 -有卡
                if (StringUtils.equals(vaReq.getBody().getRq().getSvcRq().getAEIPYTP(), "00")) {
                    str.setCharAt(88, '0');
                    str.setCharAt(89, '1');
                }

                getFeptxn().setFeptxnTrk3(str.toString());
            }


        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    public FEPReturnCode ivr_PrepareFEPTxn() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        Zone defZone = new Zone();
        try {
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header header = mIVRtxData.getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq ivrbody = mIVRtxData.getTxObject().getRequest().getBody().getRq().getSvcRq();
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA ivrReqpay = mIVRtxData.getTxObject().getRequest().getBody().getRq().getSvcRq().getPAYDATA();

            getFeptxn().setFeptxnTxDate(ivrbody.getINDATE()); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(ivrbody.getINTIME()); // 交易時間
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnTxDatetimeFisc(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnChannel(header.getCHANNEL()); //通道別
            getFeptxn().setFeptxnMsgid(mIVRtxData.getMsgCtl().getMsgctlMsgid()); // 訊息代號
            getFeptxn().setFeptxnCbsProc(mIVRtxData.getMsgCtl().getMsgctlCbsProc());
            getFeptxn().setFeptxnMsgkind(header.getMSGKIND());//訊息種類
            getFeptxn().setFeptxnAtmBrno(header.getBRANCHID());//分行代號
            getFeptxn().setFeptxnMsgflow("A1");
            getFeptxn().setFeptxnChannelEjfno(header.getCLIENTTRACEID());//外部通道電子日誌序號
            getFeptxn().setFeptxnTxDateAtm(ivrbody.getINDATE());//交易日期
            getFeptxn().setFeptxnEjfno(getEj()); // 電子日誌序號
            if (String.valueOf(getEj()).length() >= 8) { /*因財金規格需要此欄位*/
                getFeptxn().setFeptxnIcSeqno(String.valueOf(getEj()).substring(0, 8));
            } else {
                getFeptxn().setFeptxnIcSeqno(String.valueOf(getEj()));
            }
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());//交易啟動銀行
            getFeptxn().setFeptxnTxCode(ivrbody.getFSCODE());//交易種類
            getFeptxn().setFeptxnPcode(ivrbody.getPCODE());
            getFeptxn().setFeptxnAtmno(ivrbody.getTERMINALID());//端末機代號
            getFeptxn().setFeptxnClientip(ivrbody.getIPADDR());//使用者登入IP
            getFeptxn().setFeptxnAtmType(ivrbody.getTERMINAL_TYPE());//端末設備型態
            getFeptxn().setFeptxnAtmChk(ivrbody.getTERMINAL_CHECKNO());//端末設備查核碼
            getFeptxn().setFeptxnZoneCode("TWN"); //地區別
            getFeptxn().setFeptxnTxCurAct("TWD");
            getFeptxn().setFeptxnTxCur("TWD");
            getFeptxn().setFeptxnTxAmt(ivrbody.getTRANSAMT());//轉帳金額
            getFeptxn().setFeptxnTxAmtAct(ivrbody.getTRANSAMT());//轉帳金額
            getFeptxn().setFeptxnTroutBkno(ivrbody.getTRNSFROUTBANK().substring(0, 3));//轉出帳號銀行別
            getFeptxn().setFeptxnTroutBkno7(ivrbody.getTRNSFROUTBANK());
            getFeptxn().setFeptxnIcmark("");
            getFeptxn().setFeptxnTroutActno(ivrbody.getTRNSFRINACCNT());//轉出帳號
            getFeptxn().setFeptxnMajorActno(ivrbody.getTRNSFRINACCNT());//轉出帳號

            if (StringUtils.isNotBlank(ivrReqpay.getNPPAYENDDATE())) {
                getFeptxn().setFeptxnPaytype(ivrReqpay.getNPPAYTYPE());//繳款類別
            }
            if (StringUtils.isNotBlank(ivrReqpay.getPAYENDDATE())) {
                getFeptxn().setFeptxnDueDate(ivrReqpay.getPAYENDDATE());//繳款期限
            }
            if (StringUtils.isNotBlank(ivrReqpay.getORGAN())) {
                getFeptxn().setFeptxnTaxUnit(ivrReqpay.getORGAN());//稽徵機關
            }
            if (StringUtils.isNotBlank(ivrReqpay.getNPID())) {
                getFeptxn().setFeptxnIdno(ivrReqpay.getNPID());//身分證/營利事業統一編號
            }
            if (StringUtils.isNotBlank(ivrReqpay.getPAYNO())) {
                getFeptxn().setFeptxnReconSeqno(ivrReqpay.getPAYNO());//銷帳編號
            }

            getFeptxn().setFeptxnTxrust("0");//處理結果(初始預設值)
            defZone = zoneExtMapper.getDataByZonee("TWN");
            if (defZone != null && defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
            }
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
                    && (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
            )) {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
            } else {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
            }

            //代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(mIVRtxData.getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());    //財金營業日
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) &&
                        StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }
            defZone = zoneExtMapper.getDataByZonee("TWN"); //ATM所在地區(MODE)
            if (defZone != null && defZone.getZoneTbsdy() != null) {
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy());//自行營業日
            }
            getFeptxn().setFeptxnTbsdyAct(getFeptxn().getFeptxnTbsdy());//卡片所在地區營業日
            getFeptxn().setFeptxnSubsys(mIVRtxData.getMsgCtl().getMsgctlSubsys());    //系統別

            if (DbHelper.toBoolean(mIVRtxData.getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                // 判斷自行及 ATM 2 WAY Flag
                if (!DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && DbHelper.toBoolean(mIVRtxData.getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnWay((short) 2);
                } else {
                    getFeptxn().setFeptxnWay((short) 3);
                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 將HCE_TITA電文相關欄位, 準備寫入 FEPTxn
     *
     * @return
     */
    public FEPReturnCode hce_PrepareFEPTxn() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        Zone defZone = new Zone();
        try {
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header header = mHCEtxData.getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq hcebody = mHCEtxData.getTxObject().getRequest().getBody().getRq().getSvcRq();

            getFeptxn().setFeptxnTxDate(hcebody.getINDATE()); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(hcebody.getINTIME()); // 交易時間
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            if (StringUtils.isNotBlank(hcebody.getIC_TAC_DATE())) {
                getFeptxn().setFeptxnTxDatetimeFisc(hcebody.getIC_TAC_DATE() + hcebody.getIC_TAC_TIME());
            } else {
                getFeptxn().setFeptxnTxDatetimeFisc(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            }

            getFeptxn().setFeptxnChannel(header.getCHANNEL()); //通道別
            getFeptxn().setFeptxnMsgid(mHCEtxData.getMsgCtl().getMsgctlMsgid()); // 訊息代號
            getFeptxn().setFeptxnCbsProc(mHCEtxData.getMsgCtl().getMsgctlCbsProc());
            getFeptxn().setFeptxnMsgkind(header.getMSGKIND());//訊息種類
            getFeptxn().setFeptxnAtmBrno(header.getBRANCHID());//分行代號
            getFeptxn().setFeptxnMsgflow("A1");
            getFeptxn().setFeptxnChannelEjfno(header.getCLIENTTRACEID());//外部通道電子日誌序號
            getFeptxn().setFeptxnTxDateAtm(hcebody.getINDATE());//交易日期
            getFeptxn().setFeptxnEjfno(mHCEtxData.getEj()); // 電子日誌序號
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());//交易啟動銀行
            getFeptxn().setFeptxnTxCode(hcebody.getFSCODE());//交易種類
            getFeptxn().setFeptxnPcode(hcebody.getPCODE());
            getFeptxn().setFeptxnAtmno(hcebody.getTERMINALID());//端末機代號
            getFeptxn().setFeptxnClientip(hcebody.getIPADDR());//使用者登入IP
            getFeptxn().setFeptxnAtmType(hcebody.getTERMINAL_TYPE());//端末設備型態
            getFeptxn().setFeptxnAtmChk(hcebody.getTERMINAL_CHECKNO());//端末設備查核碼
            getFeptxn().setFeptxnIcmark(hcebody.getICMARK());
//        	if(StringUtils.isNotBlank(hcebody.getICMARK())) {
//        		getFeptxn().setFeptxnIcmark(hcebody.getICMARK().length() >= 30 ? hcebody.getICMARK().substring(0, 30) : hcebody.getICMARK());//卡片備註欄
//        	}
            getFeptxn().setFeptxnTxCurAct("TWD");
            getFeptxn().setFeptxnTxCur("TWD");
            if (hcebody.getTRANSAMT() != null) {
                getFeptxn().setFeptxnTxAmt(hcebody.getTRANSAMT());
                getFeptxn().setFeptxnTxAmtAct(hcebody.getTRANSAMT());
            }
            if (hcebody.getTRNSFROUTBANK().length() > 3) {
                getFeptxn().setFeptxnTroutBkno(hcebody.getTRNSFROUTBANK().substring(0, 3));//轉出帳號銀行別
            } else if (hcebody.getTRNSFROUTBANK().length() == 3) {
                getFeptxn().setFeptxnTroutBkno(hcebody.getTRNSFROUTBANK());
            }
            getFeptxn().setFeptxnTroutBkno7(hcebody.getTRNSFROUTBANK());
            getFeptxn().setFeptxnTroutActno(hcebody.getTRNSFROUTACCNT());//轉出帳號
            getFeptxn().setFeptxnMajorActno(hcebody.getTRNSFROUTACCNT());//轉出帳號
            if (hcebody.getTRNSFRINBANK().length() > 3) {
                getFeptxn().setFeptxnTrinBkno(hcebody.getTRNSFRINBANK().substring(0, 3));
            } else if (hcebody.getTRNSFRINBANK().length() == 3) {
                getFeptxn().setFeptxnTrinBkno(hcebody.getTRNSFRINBANK());
            }
            getFeptxn().setFeptxnTrinBkno7(hcebody.getTRNSFRINBANK());
            getFeptxn().setFeptxnTrinActno(hcebody.getTRNSFRINACCNT());//轉入帳號
            getFeptxn().setFeptxnIcSeqno(hcebody.getIC_SEQNO());
            getFeptxn().setFeptxnIcTac(hcebody.getIC_TAC().length() > 16 ? hcebody.getIC_TAC().substring(0, 16) : hcebody.getIC_TAC());
            if ("TM".equals(hcebody.getTRANSTYPEFLAG())) {
//    			檢核 ATM_TITA.TrnsfrInBank(4:4) = ‘8999’
                if (hcebody.getTRNSFRINBANK().length() == 3) {
                    getFeptxn().setFeptxnTrinBkno7(hcebody.getTRNSFRINBANK().substring(0, 3) + "8999");
                } else {
                    if (hcebody.getTRNSFRINBANK().length() == 7) {
                        getFeptxn().setFeptxnTrinBkno7(hcebody.getTRNSFRINBANK().substring(0, 3) + "8999");
                    } else {
                        rtnCode = ATMReturnCode.OtherCheckError;
                        this.logContext.setRemark(StringUtils.join("手機門號轉帳，轉入銀行代碼錯誤"));
                        logMessage(Level.INFO, this.logContext);
                        return rtnCode;
                    }
                }
                if (hcebody.getTRNSFRINACCNT().length() > 10)
                    getFeptxn().setFeptxnTelephone(hcebody.getTRNSFRINACCNT().substring(hcebody.getTRNSFRINACCNT().length() - 10));
                else
                    getFeptxn().setFeptxnTelephone(hcebody.getTRNSFRINACCNT());
                getFeptxn().setFeptxnMtp("Y");
            } else {
                if (hcebody.getTRNSFRINBANK().length() == 3) {
                    getFeptxn().setFeptxnTrinBkno7(hcebody.getTRNSFRINBANK() + "0000");
                }
            }

            if (StringUtils.isNotBlank(hcebody.getTEXTMARK())) {
                if (hcebody.getTEXTMARK().length() > 20)
                    getFeptxn().setFeptxnChrem(hcebody.getTEXTMARK().substring(0, 20));
                else
                    getFeptxn().setFeptxnChrem(hcebody.getTEXTMARK());/*前端為Unicode  轉帳附言欄*/
            }
            getFeptxn().setFeptxnTxrust("0");//處理結果(初始預設值)
            defZone = zoneExtMapper.getDataByZonee("TWN");
            if (defZone != null && defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy());
            }
//            if ((StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno()) &&
//                    SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()))
//                    && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()
//            )) {
//                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
//            } else {
//                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));    //自行記號
//            }

            //2024-05-21 Jack Modified HCE
            String trOutBank = getFeptxn().getFeptxnTroutBkno();
            String trInBank = getFeptxn().getFeptxnTrinBkno();
            //因查詢自行跨行trInBank皆為null導致判斷錯誤 所以自行給trInBank=006,他行A轉他行A屬跨行
            if (StringUtils.equals(trOutBank, "006") && StringUtils.isBlank(trInBank)) {
                trInBank = "006";
            }
            String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
            if (!StringUtils.equals(trOutBank, trInBank) ||
                    (!StringUtils.equals(trOutBank, sysstatHbkno) &&
                            !StringUtils.equals(trInBank, sysstatHbkno))
                            && StringUtils.isNotBlank(trOutBank)) {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
            } else {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));     //自行記號
            }
            //代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(mHCEtxData.getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());            //財金營業日
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) &&
                        StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())) {
                    getFeptxn().setFeptxnTbsdyFisc(mHCEtxData.getTbsdyFISC());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }
            getFeptxn().setFeptxnTbsdyAct(getFeptxn().getFeptxnTbsdy());//卡片所在地區營業日
            getFeptxn().setFeptxnSubsys(mHCEtxData.getMsgCtl().getMsgctlSubsys());    //系統別

            if (DbHelper.toBoolean(mHCEtxData.getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                // 判斷自行及 ATM 2 WAY Flag
                if (!DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && DbHelper.toBoolean(mHCEtxData.getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnWay((short) 2);
                } else {
                    getFeptxn().setFeptxnWay((short) 3);
                }
            }

            if ("TW".equals(getFeptxn().getFeptxnTxCode())) {
                /* 轉入行必須為本行 */
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
                        || (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                        && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
                )) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }

            } else if ("T2".equals(getFeptxn().getFeptxnTxCode())) {
                /* 轉入行與轉出行必須為本行 */
                if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
                        || (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                        && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
                )) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;

                }
            } else if ("T4".equals(getFeptxn().getFeptxnTxCode())) {
                /* 轉出行必須為本行 */
                if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
                        || SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }
            } else if ("TA".equals(getFeptxn().getFeptxnTxCode()) || "TR".equals(getFeptxn().getFeptxnTxCode())) {
                /* 轉入行與轉出行不為本行 */
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
                        || (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
                )) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;

                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 將NB_TITA電文相關欄位, 準備寫入 FEPTxn
     * FEP10-000-SPC_NB_PrepareFEPTxn(收到NB電文寫入 FEPTXN)
     *
     * @return
     */
    public FEPReturnCode nb_PrepareFEPTxn() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        Zone defZone = new Zone();
        try {

            RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = mNBtxData.getTxNbfepObject().getRequest().getBody().getRq().getHeader();
            RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq body = mNBtxData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();

            getFeptxn().setFeptxnTxDate(body.getINDATE().trim()); // 交易日期(西元年)
            getFeptxn().setFeptxnTxTime(body.getINTIME().trim()); // 交易時間
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnTxDatetimeFisc(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnChannel(header.getCHANNEL().trim()); //通道別
            getFeptxn().setFeptxnMsgid(mNBtxData.getMsgCtl().getMsgctlMsgid().trim()); // 訊息代號
            getFeptxn().setFeptxnCbsProc(mNBtxData.getMsgCtl().getMsgctlCbsProc().trim());
            getFeptxn().setFeptxnMsgkind(header.getMSGKIND().trim());//訊息種類
            getFeptxn().setFeptxnAtmBrno(header.getBRANCHID().trim());//分行代號
            getFeptxn().setFeptxnMsgflow("A1");
            getFeptxn().setFeptxnChannelEjfno(header.getCLIENTTRACEID().trim());//外部通道電子日誌序號
            getFeptxn().setFeptxnTxDateAtm(body.getINDATE().trim());//交易日期
            getFeptxn().setFeptxnEjfno(mNBtxData.getEj()); // 電子日誌序號
            if (String.valueOf(getFeptxn().getFeptxnEjfno()).length() >= 8) {
                getFeptxn().setFeptxnIcSeqno(String.valueOf(getFeptxn().getFeptxnEjfno()).substring(0, 8));
            } else {
                getFeptxn().setFeptxnIcSeqno(String.valueOf(getFeptxn().getFeptxnEjfno()));
            }
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());//交易啟動銀行
            getFeptxn().setFeptxnTxCode(body.getFSCODE().trim());//交易種類
            getFeptxn().setFeptxnPcode(body.getPCODE().trim());
            getFeptxn().setFeptxnAtmno(body.getTERMINALID().trim());//端末機代號
            getFeptxn().setFeptxnClientip(body.getIPADDR().trim());//使用者登入IP
            getFeptxn().setFeptxnAtmType(body.getTERMINAL_TYPE().trim());//端末設備型態
            boolean cnkatmchk = StringUtils.isNotBlank(body.getTERMINAL_CHECKNO().trim()) && body.getTERMINAL_CHECKNO().length() < 8;
            getFeptxn().setFeptxnAtmChk(cnkatmchk ? StringUtils.rightPad(body.getTERMINAL_CHECKNO(), 8, " ") : body.getTERMINAL_CHECKNO().trim());//端末設備查核碼
            getFeptxn().setFeptxnIdno(body.getTRNSFROUTIDNO().trim());//轉出帳號統編
            getFeptxn().setFeptxnZoneCode("TWN");//地區別
            getFeptxn().setFeptxnTxCurAct("TWD");
            getFeptxn().setFeptxnTxCur("TWD");
            if (body.getTRANSAMT() != null) {
                feptxn.setFeptxnTxAmt(body.getTRANSAMT().setScale(2));
                feptxn.setFeptxnTxAmtAct(body.getTRANSAMT().setScale(2));
            }

            getFeptxn().setFeptxnBrno(body.getTRANBRANCH().trim());//轉出帳號清算分行
            getFeptxn().setFeptxnTroutBkno(body.getTRNSFROUTBANK().trim().substring(0, 3));//轉出帳號銀行別
            // 2026/08/06 財金電文的轉出行代號後四碼不須給分行代號
//            if ("2561".equals(feptxn.getFeptxnPcode()) && "EF".equals(feptxn.getFeptxnTxCode().substring(0, 2))) {
//                //汽燃費
//                getFeptxn().setFeptxnTroutBkno7(StringUtils.rightPad(body.getTRNSFROUTBANK().trim().substring(0, 3), 7, "0"));//右補0滿7碼
//                getFeptxn().setFeptxnTrinBkno7(StringUtils.rightPad(body.getTRNSFRINBANK().trim().substring(0, 3), 7, "0"));//右補0滿7碼
//            } else {
//                getFeptxn().setFeptxnTroutBkno7(StringUtils.rightPad(body.getTRNSFROUTBANK().trim(), 7, "0"));//右補0滿7碼
//                getFeptxn().setFeptxnTrinBkno7(StringUtils.rightPad(body.getTRNSFRINBANK().trim(), 7, "0"));//轉入帳號銀行別, 右補0滿7碼
//            }
            getFeptxn().setFeptxnTroutActno(StringUtils.leftPad(body.getTRNSFROUTACCNT().trim(), 16, "0"));//轉出帳號
            getFeptxn().setFeptxnMajorActno(StringUtils.leftPad(body.getTRNSFROUTACCNT().trim(), 16, "0"));//轉出帳號
            if (body.getTRNSFRINBANK().length() >= 3) {
                getFeptxn().setFeptxnTrinBkno(body.getTRNSFRINBANK().trim().substring(0, 3)); //轉入帳號銀行別
            } else {
                getFeptxn().setFeptxnTrinBkno(body.getTRNSFRINBANK().trim());
            }
            getFeptxn().setFeptxnTrinActno(body.getTRNSFRINACCNT().trim());//轉入帳號
            getFeptxn().setFeptxnIcmark(StringUtils.leftPad("C5C1C5C6C5E6C9F1D3C5", 60, "40"));

            if ( "TM".equals(body.getTRANSTYPEFLAG().trim()) ) {
                //手機門號轉帳
                getFeptxn().setFeptxnTrinBkno7( body.getTRNSFRINBANK().trim().substring(0, 3) + "8999");
                getFeptxn().setFeptxnTelephone( StringUtils.right(body.getTRNSFRINACCNT().trim(),10));
                getFeptxn().setFeptxnMtp("Y");
            }
            /* 手續費負擔別*/
            /* 13：轉入帳戶負擔(ATM全繳:值13)*/
            if ("13".equals(body.getFEEPAYMENTTYPE())) {
                getFeptxn().setFeptxnNpsClr((short) 2);//手續費負擔別
                /* 14 : 轉出轉入帳戶均擔*/
            } else if ("14".equals(body.getFEEPAYMENTTYPE())) {
                getFeptxn().setFeptxnNpsClr((short) 3);
                /* 15：轉出帳戶負擔*/
            } else if ("15".equals(body.getFEEPAYMENTTYPE().trim())) {
                getFeptxn().setFeptxnNpsClr((short) 1);
            }
            if (body.getCUSTPAYFEE() != null) {
                getFeptxn().setFeptxnFeeCustpayAct(body.getCUSTPAYFEE());//客戶應付手續費
                getFeptxn().setFeptxnFeeCustpay(body.getCUSTPAYFEE());
            }


            getFeptxn().setFeptxnPsbremFD(body.getTRNSFROUTNOTE().trim());//付款人自我備註
            getFeptxn().setFeptxnFmrem(body.getTRNSFROUTNOTE().trim());//付款人自我備註(網頁)
            getFeptxn().setFeptxnPsbremFC(body.getTRNSFRINNOTE().trim());//給收款人訊息

            if (StringUtils.isNotBlank(body.getTEXTMARK().trim())) {
                getFeptxn().setFeptxnChrem(body.getTEXTMARK().trim());/*前端為Unicode  轉帳附言欄*/
            }


            /*自繳15類*/
            String paycategory = null;
            if ("TZ".equals(getFeptxn().getFeptxnTxCode())) {
                paycategory = body.getPAYDATA().getPAYCATEGORY().trim();
                getFeptxn().setFeptxnPaytype(paycategory);/*繳款類別*/
                String organ = body.getPAYDATA().getORGAN().trim();
                getFeptxn().setFeptxnTaxUnit(organ);/*稽徵機關*/
                if (body.getPAYDATA().getCID().trim().length() == 11 || body.getPAYDATA().getCID().trim().length() == 8) {/*身分證(前兩碼為數字)/營利事業統一編號(8碼)，不須轉換*/
                    getFeptxn().setFeptxnIdno(body.getPAYDATA().getCID().trim());
                } else {
                    getFeptxn().setFeptxnIdno(TxHelper.parseCIDtoNum(body.getPAYDATA().getCID().trim()));//上送FISC用(英文需轉數字)
                }
                getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatFbkno());//轉入銀行代號=財金
                if (StringUtils.isBlank(body.getTRNSFRINACCNT().trim())) {//轉入帳號
                    getFeptxn().setFeptxnTrinActno(StringUtils.leftPad("", 16, '0'));
                }
            } else if ("TY".equals(getFeptxn().getFeptxnTxCode())) { /*核定 非15類*/
                paycategory = body.getPAYDATA().getPAYCATEGORY().trim();
                getFeptxn().setFeptxnPaytype(paycategory);/*繳款類別*/
                getFeptxn().setFeptxnReconSeqno(body.getPAYDATA().getPAYNO().trim());/*銷帳編號*/
                String payendDate = body.getPAYDATA().getPAYENDDATE().trim();
                if (StringUtils.isNotBlank(payendDate)) {
                    if (payendDate.length() == 8) {
                        getFeptxn().setFeptxnDueDate(payendDate);/* 繳款期限*/
                    } else if (payendDate.length() <= 7) {
                        String dueDate = CalendarUtil.rocStringToADString(payendDate);
                        getFeptxn().setFeptxnDueDate(dueDate);
                    }
                }
                getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatFbkno());//轉入銀行代號=財金
                getFeptxn().setFeptxnTrinActno(StringUtils.rightPad(getFeptxn().getFeptxnReconSeqno(), 16, '0'));//'轉入帳號=銷帳編號  /* 補滿 16位, 右補 0 */
            }

            if ("E".equals(getFeptxn().getFeptxnTxCode().substring(0, 1))) {            //全國性繳費
                getFeptxn().setFeptxnReconSeqno(body.getPAYDATA().getNPPAYNO().trim());    //銷帳編號
                getFeptxn().setFeptxnBusinessUnit(body.getPAYDATA().getNPOPID().trim()); //委託單位代號
                getFeptxn().setFeptxnPayno(body.getPAYDATA().getNPFEENO().trim());        //費用代號
                getFeptxn().setFeptxnFiscRrn(getFeptxn().getFeptxnBusinessUnit() + getFeptxn().getFeptxnPayno());
                getFeptxn().setFeptxnPaytype(body.getPAYDATA().getNPPAYTYPE().trim());        //繳款類別

                if (StringUtils.isNotBlank(body.getPAYDATA().getNPPAYENDDATE().trim())) {
                    getFeptxn().setFeptxnDueDate(body.getPAYDATA().getNPPAYENDDATE().trim());/*繳款期限*/
                } else {
                    getFeptxn().setFeptxnDueDate("");
                }
                getFeptxn().setFeptxnRemark(getFeptxn().getFeptxnPayno() + getFeptxn().getFeptxnReconSeqno() + getFeptxn().getFeptxnDueDate());

                if ("EA".equals(body.getFSCODE()) || "EW".equals(body.getFSCODE())) {
                    getFeptxn().setFeptxnIdno(body.getPAYDATA().getNPID().trim());//身分證/營利事業統一編號 (英文字不需轉換,左靠右補空白)
                    /* 委託單位+費用代號 */
                    getFeptxn().setFeptxnFiscRrn(body.getPAYDATA().getNPOPID().trim() + body.getPAYDATA().getNPFEENO().trim());
                }
            }

//            if ("TM".equals(body.getTRANSTYPEFLAG())) {
////			檢核 ATM_TITA.TrnsfrInBank(4:4) = ‘8999’
//                if ("8999".equals(body.getTRNSFRINBANK().substring(3, 7))) {
//                    getFeptxn().setFeptxnTrinBkno7(body.getTRNSFRINBANK().substring(0, 3) + "8999");
//                    getFeptxn().setFeptxnTelephone(body.getTRNSFRINACCNT());
//                    getFeptxn().setFeptxnMtp("Y");
//                } else {
//                    rtnCode = ATMReturnCode.OtherCheckError;
//                    this.logContext.setRemark(StringUtils.join("手機門號轉帳，轉入銀行代碼錯誤"));
//                    logMessage(Level.INFO, this.logContext);
//                    return rtnCode;
//                }
//            }
            getFeptxn().setFeptxnTxrust("0");//處理結果(初始預設值)
            defZone = zoneExtMapper.getDataByZonee("TWN");
            if (defZone != null && defZone.getZoneCbsMode() != null) {
                getFeptxn().setFeptxnTxnmode(defZone.getZoneCbsMode()); //ATM所在地區(MODE)
                getFeptxn().setFeptxnTbsdy(defZone.getZoneTbsdy()); //自行營業日
            }

            String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
            if((!"".equals(feptxn.getFeptxnTroutBkno()) && !sysstatHbkno.equals(feptxn.getFeptxnTroutBkno()))
                    || (!"".equals(feptxn.getFeptxnTrinBkno()) && !sysstatHbkno.equals(feptxn.getFeptxnTrinBkno()))) {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true));    //跨行記號
                getFeptxn().setFeptxnSubsys((short)1); //子系統
            } else {
                getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(false));     //自行記號
                getFeptxn().setFeptxnSubsys(mNBtxData.getMsgCtl().getMsgctlSubsys());    //系統別
            }

            //代理提款換日交易
            //判斷AA起始讀進的SYSSTAT_TBSDY_FISC = 現在的SYSSTAT_TBSDY_FISC
            if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().equals(mNBtxData.getTbsdyFISC())) {
                getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());            //財金營業日
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) &&
                        StringUtils.isNotBlank(getFeptxn().getFeptxnTxCode())
                ) {
                    getFeptxn().setFeptxnTbsdyFisc(mNBtxData.getTbsdyFISC());        //填入 AA起始讀進的SYSSTAT_TBSDY_FISC
                } else {
                    getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());        //財金營業日
                }
            }
            getFeptxn().setFeptxnTbsdyAct(getFeptxn().getFeptxnTbsdy());//卡片所在地區營業日

            if (DbHelper.toBoolean(mNBtxData.getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                // 判斷自行及 ATM 2 WAY Flag
                if (!DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && DbHelper.toBoolean(mNBtxData.getMsgCtl().getMsgctlAtm2way())) {
                    getFeptxn().setFeptxnWay((short) 2);
                } else {
                    getFeptxn().setFeptxnWay((short) 3);
                }
            }

            //跨行ID+ACCT全繳 2262
            if ("EW".equals(getFeptxn().getFeptxnTxCode())) {
                if (StringUtils.isBlank(feptxn.getFeptxnPcode())) {
                    getFeptxn().setFeptxnPcode("2262");
                }
                /* 轉入行必須為本行 */
                if ((StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno())
                        && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
                )) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }
                //跨行ID+ACCT全繳 2261
            } else if ("ED".equals(getFeptxn().getFeptxnTxCode())) /*全繳整批轉即時*/ {
                if (StringUtils.isBlank(feptxn.getFeptxnPcode())) {
                    getFeptxn().setFeptxnPcode("2261");
                }
                /* 轉出行必須為本行*/
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
                        && !StringUtils.equals(SysStatus.getPropertyValue().getSysstatHbkno(), getFeptxn().getFeptxnTroutBkno())) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }
            } else if ("EA".equals(getFeptxn().getFeptxnTxCode())) /*全繳整批轉即時*/ {
                if (StringUtils.isBlank(feptxn.getFeptxnPcode())) {
                    getFeptxn().setFeptxnPcode("2263");
                }
                /* 轉出行與轉出行相同 A行轉A行*/
//                if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())
//                        || (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())
//                        && !SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())
//                )) {
                if (!StringUtils.equals(getFeptxn().getFeptxnTrinBkno(), getFeptxn().getFeptxnTroutBkno())) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }
            } else if ("ER".equals(getFeptxn().getFeptxnTxCode())) {
                if (StringUtils.isBlank(feptxn.getFeptxnPcode())) {
                    getFeptxn().setFeptxnPcode("2264");
                }
                /* 轉出行 轉入行為他行 A行轉B行*/
                if (StringUtils.equals(getFeptxn().getFeptxnTrinBkno(), getFeptxn().getFeptxnTroutBkno())
                        && StringUtils.equals(getFeptxn().getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())
                        && StringUtils.equals(getFeptxn().getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                    rtnCode = ATMReturnCode.OtherCheckError;
                    return rtnCode;
                }
            }
            if ("2700".equals(body.getPCODE())) {
                getFeptxn().setFeptxnToName(body.getFXMLDATA().getTRINNAME());
                getFeptxn().setFeptxnToIdno(body.getTRNSFRINIDNO());
                getFeptxn().setFeptxnFromName(body.getFXMLDATA().getTROUTNAME());
                getFeptxn().setFeptxnFxmlMemo(body.getFXMLDATA().getFXMLMEMO());
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    public FEPReturnCode prepareInbk2160() {
        //+log
        String newStan = getStan();
        getLogContext().setRemark("START DO prepareInbk2160 STAN : " + newStan);
        logMessage(Level.INFO, getLogContext());
        try {
            setInbk2160(new Inbk2160());
            FEPReturnCode rtnCode = CommonReturnCode.Normal;
            // 1. FEPTXN欄位, 寫入INBK2160
            getInbk2160().setInbk2160TxDate(feptxn.getFeptxnTxDate()); /* 交易日期 */
            getInbk2160().setInbk2160Ejfno(feptxn.getFeptxnEjfno()); /*電子日誌序號*/
            getInbk2160().setInbk2160Bkno(SysStatus.getPropertyValue().getSysstatHbkno()); /*交易啟動銀行*/
            getInbk2160().setInbk2160TxTime(feptxn.getFeptxnTxTime()); /*交易時間*/
            /* 2025/7/4 修改 */
            getInbk2160().setInbk2160Atmno(feptxn.getFeptxnAtmno());
            //原電子日誌序號
            if (feptxn.getFeptxnTraceEjfno() != null) { //3-way 之Con EJFNO
                getInbk2160().setInbk2160OriEjfno(feptxn.getFeptxnTraceEjfno()); /*Con電子日誌序號*/
            } else {
                getInbk2160().setInbk2160OriEjfno(feptxn.getFeptxnEjfno()); /*Req電子日誌序號*/
            }

            /* 端末機代號 */
            if ("HCA".equals(getFeptxn().getFeptxnChannel().trim())) {
                getInbk2160().setInbk2160Atmno("T" + getFeptxn().getFeptxnBrno() + "HCE"); //前端電文的扣款掛帳分行
            }

            getInbk2160().setInbk2160MajorActno(getFeptxn().getFeptxnMajorActno());
            getInbk2160().setInbk2160TroutBkno(getFeptxn().getFeptxnTroutBkno());
            if ("ATM".equals(getFeptxn().getFeptxnChannel().trim())) {
                getInbk2160().setInbk2160TroutActno(getFeptxn().getFeptxnMajorActno());
            } else {
                getInbk2160().setInbk2160TroutActno(getFeptxn().getFeptxnTroutActno());
            }
            getInbk2160().setInbk2160OriTrinBkno(getFeptxn().getFeptxnTrinBkno());
            getInbk2160().setInbk2160TxAmt(getFeptxn().getFeptxnTxAmt());
            getInbk2160().setInbk2160Stan(newStan);
            getInbk2160().setInbk2160FiscFlag(getFeptxn().getFeptxnFiscFlag());
            getInbk2160().setInbk2160ReqRc("0000");
            getInbk2160().setInbk2160Subsys((short) 1);
            getInbk2160().setInbk2160Pcode(FISCPCode.PCode2160.getValueStr());
            /* 2025/7/4 修改 */
            if ("H".equals(getFeptxntcb().getFeptxntcbIcttypep())) {
                getInbk2160().setInbk2160Twmp("001");
            } else if("HCA".equals(getFeptxn().getFeptxnChannel().trim()) && "H".equals(getFeptxntcb().getFeptxntcbAtxchnnl())) {
                //HCA交易管道或支付型態為H，2026/6/11 修改
                getInbk2160().setInbk2160Twmp("001");
            }else{
                getInbk2160().setInbk2160Twmp("101");
            }
            getInbk2160().setInbk2160Chrem(getFeptxn().getFeptxnChrem());
            if (!"ATM".equals(getFeptxn().getFeptxnChannel().trim()) && !"FISC".equals(getFeptxn().getFeptxnChannel().trim())) { //外圍HCE
                if(getFeptxn().getFeptxnFiscFlag() == 0){ //自行
                    getInbk2160().setInbk2160OriPcode(getFeptxn().getFeptxnPcode().substring(0,3) + "0");
                }else{
                    getInbk2160().setInbk2160OriPcode(getFeptxn().getFeptxnPcode());
                }
            }else{
                getInbk2160().setInbk2160OriPcode(getFeptxn().getFeptxnPcode());
            }
            /* 2025/10/8 修改, for 消費扣款乘車碼 */
            if (feptxn.getFeptxnNoticeId() != null) {
                getInbk2160().setInbk2160OriBkno(getFeptxn().getFeptxnBkno() + feptxn.getFeptxnNoticeId());
            } else {
                getInbk2160().setInbk2160OriBkno(getFeptxn().getFeptxnBkno() + "0000");
            }
            getInbk2160().setInbk2160OriStan(getFeptxn().getFeptxnStan());

            /* 2025/7/9 修改,消費扣款交易紅利點數相關資料 */
            if (FISCPCode.PCode2525.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                /* 增加紅利點數或減少紅利點數 >0 ,平台附言欄(Bitmap 46) (100 BYTE),2160 Req電文將Bitmap 46設成 ON, 送財金 */
                if (getFeptxntcb().getFeptxntcbOAddPoint() > 0 || getFeptxntcb().getFeptxntcbODeductPoint() > 0) {
                    getInbk2160().setInbk2160TwmpChrem(
                            StringUtils.rightPad(StringUtils.defaultString(getFeptxntcb().getFeptxntcbORedeemState()), 1, ' ') +
                                    StringUtils.leftPad(Objects.toString(getFeptxntcb().getFeptxntcbOAddPoint(), ""), 10, '0') +
                                    StringUtils.leftPad(Objects.toString(getFeptxntcb().getFeptxntcbODeductPoint(), ""), 10, '0') +
                                    StringUtils.rightPad(StringUtils.defaultString(getFeptxntcb().getFeptxntcbOExtraAmtType()), 1, ' ') +
                                    StringUtils.leftPad(Objects.toString(getFeptxntcb().getFeptxntcbOExtraAmount(), ""), 10, '0') +
                                    StringUtils.rightPad(StringUtils.defaultString(getFeptxntcb().getFeptxntcbOExtraCurrCode()), 3, ' ') +
                                    StringUtils.leftPad(Objects.toString(getFeptxntcb().getFeptxntcbOFinalTxAmt(), ""), 13, '0') +
                                    StringUtils.rightPad(StringUtils.defaultString(getFeptxntcb().getFeptxntcbOFinalTxCurrCode()), 3, ' ') +
                                    StringUtils.rightPad(StringUtils.defaultString(getFeptxntcb().getFeptxntcbOBit46PotRfu()), 49, ' ')
                    );
                }
            }

            /*2025/08/19 調整所有服務 Inbk2160OriTxDate 給相同欄位 */
            if (StringUtils.isBlank(getFeptxn().getFeptxnTxDatetimeFisc()) ||
                    ("HCA".equals(getFeptxn().getFeptxnChannel().trim()) && "2500".equals(getFeptxn().getFeptxnPcode()) && getFeptxn().getFeptxnFiscFlag() == 0)) {
                getFeptxn().setFeptxnTxDatetimeFisc(StringUtils.leftPad("0", 14, "0"));
            }
            getInbk2160().setInbk2160OriTxDate(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
            getInbk2160().setInbk2160OriTxTime(getFeptxn().getFeptxnTxDatetimeFisc().substring(8, 14));

            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno7())) {
                getInbk2160().setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno7());
            } else {
                getInbk2160().setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno() + "0000");
            }
            getInbk2160().setInbk2160OriIcSeqno(getFeptxn().getFeptxnIcSeqno());

            /*FEPTXN卡片備註欄
            IF 第16位(1位) = X'4E' ,值= 1003 ELSE 值= 1001；
            FEPTXN_ICMARK 為 60位，取3231-323的值 */
            getInbk2160().setInbk2160OriIcdata("1001");

            /* 2025/10/9 修改 for 台灣pay顯示交易金額錯誤 */
            /* 2025/10/17 修改 for 2160 */
            if (FISCPCode.PCode2555.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2556.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                /* 2025/10/17修改 for 放(Bit Map 36）之消費者台幣支付金額 */
                getInbk2160().setInbk2160TxAmt(feptxn.getFeptxnTxAmtSet());
            }

            /* 2025/8/15 修改 */
            if (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2525.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                /* 2025/10/17 修改 for 2160 */
                if (FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode())
                        || FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                    /* 2025/10/17修改 for 放原交易BIT#3+台方手續費(BIT#35 23~29位) */
                    getInbk2160().setInbk2160TxAmt(feptxn.getFeptxnTxAmtAct().add(feptxn.getFeptxnCashAmt()));
                }
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnAtmType()) && getFeptxn().getFeptxnAtmType().length() >= 4 && "C".equals(getFeptxn().getFeptxnAtmType().substring(3, 4))) {
                    //一維條碼
                    if ("12006".equals(feptxn.getFeptxnTroutActno().substring(0, 5))) {
                        getInbk2160().setInbk2160OriIcdata("1003");
                    }
                } else { //非一維條碼
                    if ("4E".equals(feptxn.getFeptxnIcmark().substring(30, 32))) {
                        getInbk2160().setInbk2160OriIcdata("1003");
                    }
                }
            }

            if (feptxn.getFeptxnChannel() != null && feptxn.getFeptxnChannel().length() >= 3 &&
                    "HCA".equals(feptxn.getFeptxnChannel().substring(0, 3)) &&
                    feptxn.getFeptxnFiscFlag() != null && feptxn.getFeptxnFiscFlag() == 0) {
                getInbk2160().setInbk2160OriRepRc(getFeptxn().getFeptxnReplyCode());
            } else {
                getInbk2160().setInbk2160OriRepRc(getFeptxn().getFeptxnRepRc());
            }
            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno7())) {
                getInbk2160().setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno7());
            } else {
                if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())) {
                    getInbk2160().setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno() + "0000");
                }
            }
            getInbk2160().setInbk2160OriTrinActno(getFeptxn().getFeptxnTrinActno());
            if("HCA".equals(getFeptxn().getFeptxnChannel().trim()) && "001".equals(getInbk2160().getInbk2160Twmp())) {
                //HCA交易管道或支付型態為H，2026/6/11 修改
                getInbk2160().setInbk2160OriTrinBkno7(StringUtils.repeat(StringUtils.SPACE, 7));
                getInbk2160().setInbk2160OriTrinActno(StringUtils.repeat(StringUtils.SPACE, 16));
            }
            getInbk2160().setInbk2160OriAtmType(getFeptxn().getFeptxnAtmType());
            if (getFeptxn().getFeptxnFeeCustpay() != null) {
                /* 2025/10/17修改 for (2555/2556/2545/2546)原交易之使用者手續費放空白*/
                if (!(FISCPCode.PCode2555.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                        FISCPCode.PCode2556.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                        FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                        FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode()))) {
                    getInbk2160().setInbk2160OriFeeCustpay(getFeptxn().getFeptxnFeeCustpay());
                }
            }
            /* 修改特約商店代號長度 */
            if (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2555.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2556.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getInbk2160().setInbk2160OriMerchantId(getFeptxn().getFeptxnMerchantId().substring(0, 15));
            }
            /* 修改原交易之訂單編號 */
            if (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getInbk2160().setInbk2160OriOrderNo(getFeptxn().getFeptxnOrderNo());
            }
            /* 2022/10/31 增加手機條碼 */
            /* 2025/8/19 新增 2545/2546 */
            if (StringUtils.isNotBlank(getFeptxn().getFeptxnLuckyno())
                    && "/".equals(getFeptxn().getFeptxnLuckyno().substring(0, 1))
                    && (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode()))) {
                getInbk2160().setInbk2160OriBarcode(getFeptxn().getFeptxnLuckyno().substring(0, 8));
            }
            /* 修改原交易之外幣交易金額/原交易之外幣幣別 */
            if (FISCPCode.PCode2545.getValueStr().equals(getFeptxn().getFeptxnPcode()) ||
                    FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getInbk2160().setInbk2160OriTxnAmtCur(getFeptxn().getFeptxnTxAmt());
                Curcd curcd = curcdExtMapper.selectByPrimaryKey(getFeptxn().getFeptxnTxCur());
                getInbk2160().setInbk2160OriTxCur(curcd.getCurcdIsono3());
            }

//            getInbk2160().setInbk2160OriEjfno(getFeptxn().getFeptxnEjfno());
            getInbk2160().setInbk2160OriTxCode(getFeptxn().getFeptxnTxCode());
            getInbk2160().setInbk2160OriTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            getInbk2160().setInbk2160OriReqRc(getFeptxn().getFeptxnReqRc());
            getInbk2160().setInbk2160DesBkno("9430000"); /* 台灣PAY */
            rtnCode = this.insertINBK2160p();

            // 2. 組電文寫入 MQ
            /* 20250429討論決議後不寫入MQ，直接送FISC。 */
            if (rtnCode == FEPReturnCode.Normal) {
                // 組電文寫入 TWMPMQ  程式部份先用註解保留 //因物件未定義 先已串起來發送 如之後有定義物件再行更改
//                String returnStr = "2160" + getInbk2160().getInbk2160TxDate() + getInbk2160().getInbk2160Ejfno();
//                JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
//                JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
//                sender.sendQueue(configuration.getQueueNames().getTwmp().getDestination(), returnStr, null, null);
//                getLogContext().setMessage("DO SEND QUEUENAME xml :" + returnStr + " SEND TO QNmae :" + configuration.getQueueNames().getTwmp().getDestination());
//                logMessage(Level.INFO, getLogContext());
                //組電文CALL Send2160Handler
                Send2160Handler send2160Handler = new Send2160Handler();
                send2160Handler.setpCode("2160");
                send2160Handler.setTxDate(getInbk2160().getInbk2160TxDate());
                send2160Handler.setEjfNo(getInbk2160().getInbk2160Ejfno());
                String messageIn = "2160" + getInbk2160().getInbk2160TxDate() + getInbk2160().getInbk2160Ejfno();
                String messageOut = StringUtils.EMPTY;
                LogData logData = new LogData();
                logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                logData.setMessageFlowType(MessageFlow.Request);
                logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
                logData.setMessage(messageIn);
                logData.setRemark("Send2160Handler Receive Request:" + messageIn);
                logData.setEj(getInbk2160().getInbk2160OriEjfno()); //沿用交易EJ，不產生新EJ
                this.logMessage(logData);

                send2160Handler.setEj(logData.getEj());
                send2160Handler.setLogContext(logData);
                try {
                    messageOut = send2160Handler.dispatch(FEPChannel.Send2160Handler, messageIn);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                logData.setProgramName(StringUtils.join(ProgramName, ".processResponseData"));
                logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setMessage(messageOut);
                logData.setRemark("Send2160Handler Receive Response");
                this.logMessage(logData);
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    public String getStan() {
        StanGenerator stanGenerator = SpringBeanFactoryUtil.getBean(StanGenerator.class);
        return stanGenerator.generate();
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 VO
     */
    public String prepareVOResponseData(Object tota1) throws Exception {
        String rtnMessage = "";
        SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        try {

            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_VO_GeneralTrans_RS rs = new SEND_VO_GeneralTrans_RS();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body rsbody = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                rs.getBody().getRs().getSvcRs().setFISCFEE(feptxn.getFeptxnNpsFeeFisc());
                //應付他行手續費
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr());
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(StringUtils.defaultIfBlank(feptxn.getFeptxnStan(), " "));
            rs.getBody().getRs().getSvcRs().setCUSTOMERID(StringUtils.defaultIfBlank(feptxn.getFeptxnIdno(), " "));
            rs.getBody().getRs().getSvcRs().setTXNTYPE(StringUtils.defaultIfBlank(atmReq.getBody().getRq().getSvcRq().getTXNTYPE(), " "));
            rs.getBody().getRs().getSvcRs().setFSCODE(StringUtils.defaultIfBlank(feptxn.getFeptxnTxCode(), " "));
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }
            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(StringUtils.defaultString(feptxn.getFeptxnBrno(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO((this.getImsPropertiesValue(tota1, ImsMethodName.OUTID.getValue())));
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()))) { //轉出帳戶戶名
                rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(" ");
            }
            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(StringUtils.defaultString(feptxn.getFeptxnTroutBkno7(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(StringUtils.defaultString(feptxn.getFeptxnTroutActno(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(StringUtils.defaultString(feptxn.getFeptxnTrinBkno7(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(StringUtils.defaultString(feptxn.getFeptxnTrinActno(), " "));
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(StringUtils.defaultString(feptxn.getFeptxnTrinBrno(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(StringUtils.defaultString(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE(), " "));
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(StringUtils.defaultString(feptxn.getFeptxnPsbremFD(), " "));
            rs.getBody().getRs().getSvcRs().setOVTPT(this.getImsPropertiesValue(tota1, ImsMethodName.PWATXDAY.getValue()));
            rs.getBody().getRs().getSvcRs().setOVFEETY(this.getImsPropertiesValue(tota1, ImsMethodName.CHAFEE_TYPE.getValue()));
            rs.getBody().getRs().getSvcRs().setOVCIRCU(StringUtils.defaultString(feptxntcb.getFeptxntcbTrtocust(), " "));
            rs.getBody().getRs().getSvcRs().setOVOTHER(" ");

            getLogContext().setProgramName("prepareVOResponseData");
            logMessage(Level.INFO, getLogContext());

            rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NB
     */
    public String prepareNBResponseData(Object tota1, FEPChannel channel) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());
            rs.getBody().getRs().getSvcRs().setTCBRTNCODE(" ");  //自行回應結果，處理失敗才給值
            rs.getBody().getRs().getSvcRs().setCUSTOMERNATURE(" "); //轉入存戶性質別(個人0/非個人戶)

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("FEP");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                String fiscFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.FISC_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setFISCFEE(new BigDecimal(StringUtils.isBlank(fiscFeeStr) ? "0" : fiscFeeStr));
                //應付他行手續費
                String otherBankFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.OTHERBANK_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(new BigDecimal(StringUtils.isBlank(otherBankFeeStr) ? "0" : otherBankFeeStr));
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            if (StringUtils.equals(channel.getNameS(), FEPChannel.EOI.getNameS()))
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReq.getBody().getRq().getSvcRq().getCUSTCODE());
            else
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()) : " ";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota1, ImsMethodName.PAYEREMAIL.getValue()));

            getLogContext().setProgramName("prepareNBResponseData");
            logMessage(Level.INFO, getLogContext());


            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NBPYSelfIssue
     */
    public String prepareNBPYSelfResponseData(Object tota, FEPChannel channel, String AATxTYPE) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());
            rs.getBody().getRs().getSvcRs().setTCBRTNCODE(" ");  //自行回應結果，處理失敗才給值
            rs.getBody().getRs().getSvcRs().setCUSTOMERNATURE(" "); //轉入存戶性質別(個人0/非個人戶)

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
                if ("2".equals(AATxTYPE)) { //整批轉即時主機逾時沖正的ErrorCode
                    rs.getBody().getRs().getHeader().setSTATUSCODE("2999");
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE("P02");
                }
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                rs.getBody().getRs().getSvcRs().setFISCFEE(feptxn.getFeptxnNpsFeeFisc());
                //應付他行手續費
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr());
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());

            rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) { //交易金額
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno()); //轉出帳號帳務行代號
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO()); //轉出帳號統編
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()) : " ";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);  //轉出帳戶戶名

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7()); //轉出行
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno()); //轉出帳號
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7()); //轉入行
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno()); //轉入帳號
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno()); //轉入帳號帳務行代號
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE()); //給收款人訊息
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD()); //付款人自我備註
//            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota1, ImsMethodName.PAYEREMAIL.getValue()));

            getLogContext().setProgramName("prepareNBSelfResponseData");
            logMessage(Level.INFO, getLogContext());


            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NBPYOtherRequestA
     */
    public String prepareNBPYResponseData(Object tota, FEPChannel channel, String AATxTYPE) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());
            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
                if ( "2".equals(AATxTYPE) ) { //整批轉即時主機逾時沖正的ErrorCode
                    rs.getBody().getRs().getHeader().setSTATUSCODE("2999");
                    rs.getBody().getRs().getSvcRs().setTCBRTNCODE("P02");
                }
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                rs.getBody().getRs().getSvcRs().setFISCFEE(feptxn.getFeptxnNpsFeeFisc() == null ? BigDecimal.ZERO : feptxn.getFeptxnNpsFeeFisc());
                //應付他行手續費
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr() == null ? BigDecimal.ZERO : feptxn.getFeptxnNpsFeeRcvr());
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota, ImsMethodName.E_TRNSFROUTNAME.getValue()) : " ";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            String payerEmail = this.getImsPropertiesValue(tota, ImsMethodName.PAYEREMAIL.getValue());
            if (StringUtils.isNotBlank(payerEmail)) {  //繳汽燃費(由ABACCI01 下送電文取得)
                rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota, ImsMethodName.PAYEREMAIL.getValue()));
            }
            getLogContext().setProgramName("prepareNBPYResponseData");
            logMessage(Level.INFO, getLogContext());


            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NBTXOtherRequestA
     */
    public String prepareNBTXResponseData(Object tota1, FEPChannel channel) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());
            rs.getBody().getRs().getSvcRs().setTCBRTNCODE(" ");  //自行回應結果，處理失敗才給值
            rs.getBody().getRs().getSvcRs().setCUSTOMERNATURE(" "); //轉入存戶性質別(個人0/非個人戶)

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                //前端系統的交易代號(設定MCH的MQ通道使用)
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmt().add(feptxn.getFeptxnFeeCustpay()));
                //客戶應付手續費
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                rs.getBody().getRs().getSvcRs().setFISCFEE(feptxn.getFeptxnNpsFeeFisc() == null ? BigDecimal.ZERO : feptxn.getFeptxnNpsFeeFisc());
                //應付他行手續費
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(feptxn.getFeptxnNpsFeeRcvr() == null ? BigDecimal.ZERO : feptxn.getFeptxnNpsFeeRcvr());
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            if (StringUtils.equals(channel.getNameS(), FEPChannel.EOI.getNameS()))
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReq.getBody().getRq().getSvcRq().getCUSTCODE());
            else
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy())); //本行營業日
            //主機記帳狀態
            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }
            //主機沖帳狀態
            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()) : " ";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota1, ImsMethodName.PAYEREMAIL.getValue()));

            getLogContext().setProgramName("prepareNBTXResponseData");
            logMessage(Level.INFO, getLogContext());


            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NBTROtherRequestA
     */
    public String prepareNBTRResponseData(Object tota1, FEPChannel channel) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc()) && !"4001".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //客戶應付手續費
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                String fiscFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.FISC_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setFISCFEE(new BigDecimal(StringUtils.isBlank(fiscFeeStr) ? "0" : fiscFeeStr));
                //應付他行手續費
                String otherBankFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.OTHERBANK_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(new BigDecimal(StringUtils.isBlank(otherBankFeeStr) ? "0" : otherBankFeeStr));
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            if (StringUtils.equals(channel.getNameS(), FEPChannel.EOI.getNameS()))
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReq.getBody().getRq().getSvcRq().getCUSTCODE());
            else
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(" ");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(" ");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()) : " ";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota1, ImsMethodName.PAYEREMAIL.getValue()));

            getLogContext().setProgramName("prepareNBTRResponseData");
            logMessage(Level.INFO, getLogContext());


            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    /**
     * tota : 下送電文
     * channel : LOG CHANNL
     * 組回應電文回給 NBXOOtherRequestA
     */
    public String prepareNBXOResponseData(Object tota1, FEPChannel channel) throws Exception {
        String rtnMessage = "";
        try {
            RCV_NB_GeneralTrans_RQ atmReq = mNBtxData.getTxNbfepObject().getRequest();

            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
            SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS_Body();
            SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS_Body_MsgRs();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            rs.getBody().getRs().getHeader().setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            rs.getBody().getRs().getHeader().setCHANNEL(feptxn.getFeptxnChannel());
            rs.getBody().getRs().getHeader().setMSGID(atmReq.getBody().getRq().getHeader().getMSGID());
            rs.getBody().getRs().getHeader().setCLIENTDT(atmReq.getBody().getRq().getHeader().getCLIENTDT());

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MCH.getNameS())) {
                rs.getBody().getRs().getHeader().setTXNID(atmReq.getBody().getRq().getHeader().getTXNID());
            } else {
                rs.getBody().getRs().getHeader().setTXNID("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbOutrtc()) && !StringUtils.equalsAny(feptxntcb.getFeptxntcbOutrtc(), "4001", "000")) { //存款主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbOutrtc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbOutrtc());
                header.setSTATUSDESC("存款主機檢核錯誤:" + feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) { //財金回覆錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnRepRc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) { //主機錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                rs.getBody().getRs().getSvcRs().setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else if (!"A".equals(feptxn.getFeptxnTxrust())) { //FEP檢核錯誤
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE(feptxn.getFeptxnReplyCode());
                rs.getBody().getRs().getHeader().setSEVERITY("ERROR");
                header.setSTATUSDESC(feptxn.getFeptxnErrMsg());
            } else { //成功交易
                rs.getBody().getRs().getHeader().setSYSTEMID("ATM");
                rs.getBody().getRs().getHeader().setSTATUSCODE("4001");
                rs.getBody().getRs().getHeader().setSEVERITY("INFO");
                rs.getBody().getRs().getHeader().setSTATUSDESC("");
                //轉出帳號餘額
                rs.getBody().getRs().getSvcRs().setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                //轉出帳號可用餘額
                rs.getBody().getRs().getSvcRs().setTRANSOUTAVBL(feptxn.getFeptxnBala());
                //實際轉出金額(含手續費)
                rs.getBody().getRs().getSvcRs().setTRANSAMTOUT(feptxn.getFeptxnTxAmtAct());
                //客戶應付手續費
                rs.getBody().getRs().getSvcRs().setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
                //財金公司應收手續費
                String fiscFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.FISC_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setFISCFEE(new BigDecimal(StringUtils.isBlank(fiscFeeStr) ? "0" : fiscFeeStr));
                //應付他行手續費
                String otherBankFeeStr = this.getImsPropertiesValue(tota1, ImsMethodName.OTHERBANK_FEE.getValue());
                rs.getBody().getRs().getSvcRs().setOTHERBANKFEE(new BigDecimal(StringUtils.isBlank(otherBankFeeStr) ? "0" : otherBankFeeStr));
                //優惠手續費負擔分行
                rs.getBody().getRs().getSvcRs().setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
                //手續費減免金額
                rs.getBody().getRs().getSvcRs().setCHAFEEAMT(BigDecimal.valueOf(feptxntcb.getFeptxntcbChafeeamt()));
            }

            rs.getBody().getRs().getSvcRs().setOUTDATE(feptxn.getFeptxnTxDate());
            rs.getBody().getRs().getSvcRs().setOUTTIME(feptxn.getFeptxnTxTime());
            rs.getBody().getRs().getSvcRs().setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            rs.getBody().getRs().getSvcRs().setTXNSTAN(feptxn.getFeptxnStan());
            if (StringUtils.equals(channel.getNameS(), FEPChannel.EOI.getNameS()))
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(atmReq.getBody().getRq().getSvcRq().getCUSTCODE());
            else
                rs.getBody().getRs().getSvcRs().setCUSTOMERID(feptxn.getFeptxnIdno());
            rs.getBody().getRs().getSvcRs().setTXNTYPE(atmReq.getBody().getRq().getSvcRq().getTXNTYPE());
            rs.getBody().getRs().getSvcRs().setFSCODE(feptxn.getFeptxnTxCode());
            rs.getBody().getRs().getSvcRs().setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsacctFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG(feptxntcb.getFeptxntcbImsacctFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTACC_FLAG("");
            }

            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbImsrvsFlag())) {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG(feptxntcb.getFeptxntcbImsrvsFlag());
            } else {
                rs.getBody().getRs().getSvcRs().setHOSTRVS_FLAG("");
            }

            if (feptxn.getFeptxnTxAmt() != null) {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(new BigDecimal(feptxn.getFeptxnTxAmt().toString()));
            } else {
                rs.getBody().getRs().getSvcRs().setTRANSAMT(BigDecimal.ZERO.setScale(2));
            }

            rs.getBody().getRs().getSvcRs().setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTIDNO(atmReq.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
            String transfroutname = StringUtils.isNotBlank(this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue())) ? this.getImsPropertiesValue(tota1, ImsMethodName.E_TRNSFROUTNAME.getValue()) : "";
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNAME(transfroutname);

            rs.getBody().getRs().getSvcRs().setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            rs.getBody().getRs().getSvcRs().setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());
            rs.getBody().getRs().getSvcRs().setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            rs.getBody().getRs().getSvcRs().setTRNSFRINNOTE(atmReq.getBody().getRq().getSvcRq().getTRNSFRINNOTE());
            rs.getBody().getRs().getSvcRs().setTRNSFROUTNOTE(feptxn.getFeptxnPsbremFD());
            rs.getBody().getRs().getSvcRs().setPAYEREMAIL(this.getImsPropertiesValue(tota1, ImsMethodName.PAYEREMAIL.getValue()));

            getLogContext().setProgramName("prepareNBXOResponseData");
            logMessage(Level.INFO, getLogContext());

            if (StringUtils.equals(channel.getNameS(), FEPChannel.MBQ.getNameS()))
                rtnMessage = XmlUtil.toXML(rs).replace("+", " ");
            else
                rtnMessage = XmlUtil.toXML(rs);
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    public String rs16(String name, Integer len) {
        String message = "";
        if (StringUtils.isNotBlank(name)) {
            BigDecimal amt = new BigDecimal(name);
            name = String.format("%.0f", (Double.valueOf(name) * 100));
            if (amt.compareTo(BigDecimal.ZERO) >= 0) {
                message = "+" + StringUtils.leftPad(name, len - 1, '0').replace(".", "");
            } else {
                message = "-" + StringUtils.leftPad(name, len - 1, '0').replace(".", "").replace("-", "");
            }
        }
        return message;
    }

    public FEPReturnCode checkRequestFromOtherChannel(MessageBase txnData, String inTime) throws Exception {
        Channel defCHANNEL = new Channel();
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        String channel12 = getFeptxn().getFeptxnChannel().substring(0, 2);
        String txcode = getFeptxn().getFeptxnTxCode();
        defCHANNEL.setChannelNameS(getFeptxn().getFeptxnChannel());

        //1.	讀取 CHANNEL
        if (defCHANNEL.getChannelNameS().equals(getFeptxn().getFeptxnChannel())) {
            if (channelExtMapper.selectByChannelName(defCHANNEL.getChannelNameS()).size() == 0) {
                getLogContext().setRemark("檢核合法 Channel 失敗，CHANNEL_NAME=" + defCHANNEL.getChannelNameS());
                logMessage(Level.INFO, getLogContext());
                return FEPReturnCode.ChannelNameError;
            }
        }

        //
        Zone defZone = getZoneByZoneCode("TWN");
        if (defZone == null) {
            return IOReturnCode.ZONENotFound;
        }
        //2.	檢核自行還未換日時，需執行換日
        LocalDate zoneTbsdy = LocalDate.parse(defZone.getZoneTbsdy(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate sysstatTbsdyFisc = LocalDate.parse(SysStatus.getPropertyValue().getSysstatTbsdyFisc(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalTime currentTime = LocalTime.now();
        LocalTime targetTime = LocalTime.of(18, 0);
        if (zoneTbsdy.isBefore(sysstatTbsdyFisc) && currentTime.isAfter(targetTime) && DbHelper.toBoolean(defZone.getZoneChgday())) {
            rtnCode = ChangeCBSDate(defZone);
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }
        }

        //3.	檢核ATM 入帳日與地區檔的本營業日是否相符
        if (defZone != null && defZone.getZoneTbsdy() != null) {
//			取得 ATM 所在區域碼 (ZONE_CODE = "TWN")
            if (!Objects.equals(feptxn.getFeptxnTbsdy(), defZone.getZoneTbsdy())) {
                rtnCode = FEPReturnCode.TBSDYError; // "E079" //日期錯誤(TBSDYError)
                return rtnCode; // GO TO 5 /* 更新交易記錄 */
            }
        }


//		//3.	檢核若前端上送時間與FEP時間已超過90秒,該筆交易不處理(if need )*/
//		String timeStart="";
//		String timeEnd="";
//		 /*ID+ACCT 全繳不用檢核*/
//		if("NB".equals(channel12) || !"22".equals(feptxn.getFeptxnPcode())) {
//			if(StringUtils.isNotBlank(feptxn.getFeptxnTxDateAtm()) && StringUtils.isNotBlank(inTime)
//					&& StringUtils.isNotBlank(feptxn.getFeptxnTxDate()) && StringUtils.isNotBlank(feptxn.getFeptxnTxTime())) {
//				timeStart = feptxn.getFeptxnTxDateAtm()+inTime;
//				timeEnd = feptxn.getFeptxnTxDate()+feptxn.getFeptxnTxTime();
//
//				//不知為何要轉民國年
////				long srt = FormatUtil.parseDataTime(CalendarUtil.rocStringToADString14(timeStart), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
//				long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
//				long end = FormatUtil.parseDataTime(timeEnd, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
//				int diffseconds = (int)((end-srt)/1000);
//				/*回覆前端交易失敗0601, 合庫三碼代號: 126 */
//				//前端上送時間與FEP時間已超過90秒,該筆交易不處理
//				if(diffseconds > 90) {
//					getFeptxn().setFeptxnReplyCode("0601");
//					getFeptxn().setFeptxnCbsRc("126");
//					return FEPReturnCode.ConnectRemoteTimeOut;
//				}
//			}
//		}

        //4. 檢核前端通道是否提供服務
        Channel channel = this.getChannel(getFeptxn().getFeptxnChannel().toString());
        if (channel == null) {
            rtnCode = FEPReturnCode.CHANNELNotFound;
            // CHANNEL資料不存在
            getLogContext().setRemark("CHANNEL資料不存在, CHANNEL =" + getFeptxn().getFeptxnChannel());
            this.logMessage(getLogContext());
            return rtnCode;
        } else if (channel.getChannelEnable() == 0) {
            rtnCode = FEPReturnCode.ChannelServiceStop;
            // {FEPTXN_CHANNEL}暫停服務
            getLogContext().setRemark(getFeptxn().getFeptxnChannel() + "暫停服務");
            this.logMessage(getLogContext());
            return rtnCode;
        }

        //5. 檢核業務別交易是否提供服務
        if (txnData.getMsgCtl().getMsgctlStatus() == 0) {
            if (getFeptxn().getFeptxnFiscFlag() == 0) { // 自行交易
                //自行{MSGCTL_MSG_NAME }交易暫停服務
                getLogContext().setRemark("自行 " + txnData.getMsgCtl().getMsgctlMsgName() + " 交易暫停服務");
                this.logMessage(getLogContext());
                rtnCode = FEPReturnCode.IntraBankSingleServiceStop;
            } else {
                //代理行{MSGCTL_MSG_NAME }交易暫停服務
                getLogContext().setRemark("代理行 " + txnData.getMsgCtl().getMsgctlMsgName() + " 交易暫停服務");
                this.logMessage(getLogContext());
                rtnCode = FEPReturnCode.AgentBankSingleServiceStop;
            }
            return rtnCode;
        }

        //6 檢核外圍 EJ 是否重覆
        ///2025/7/11 外圍新增時原判斷唯一只有 FEPTXN_CHANNEL_EJFNO,改判維唯一要用 FEPTXN_CHANNEL_EJFNO + FEPTXN_CHANNEL
        rtnCode = this.checkChannelEJFNOAndChannel();
        if (rtnCode != FEPReturnCode.Normal) {
            return rtnCode;
        }

        //7 檢核財金及參加單位之系統狀態
        rtnCode = this.checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
        if (rtnCode != FEPReturnCode.Normal) {
            return rtnCode;
        }

        // 5 外圍 Channel 檢核交易連線狀態
//        if (!txnData.isTxStatus()) {
//            rtnCode = FEPReturnCode.InterBankServiceStop;
//            return rtnCode;
//        }

        // 8 檢核繳稅資料委託單位代號 或繳款類別
        if ("TY".equals(getFeptxn().getFeptxnTxCode())) {/*核定稅非15類*/
            if (StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno()) || StringUtils.isBlank(getFeptxn().getFeptxnDueDate())) {
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }
            /*自繳15類*/
        } else if ("TZ".equals(getFeptxn().getFeptxnTxCode())) {
            if (StringUtils.isBlank(getFeptxn().getFeptxnTaxUnit()) || StringUtils.isBlank(getFeptxn().getFeptxnIdno())) {
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            }
        }

        //9.	檢核汽燃料費、全國繳費
        if ("E".equals(txcode)) {
            if (StringUtils.isBlank(getFeptxn().getFeptxnBusinessUnit()) || StringUtils.isBlank(getFeptxn().getFeptxnPaytype()) || StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
                rtnCode = FEPReturnCode.OtherCheckError;
                return rtnCode;
            } else {
                // 檢核委託單位代號
                rtnCode = this.checkNpsunit(getFeptxn());
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }
                /* 檢核繳款類別 檢核INBKPARM*/
                rtnCode = this.checkPAYTYPE();
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }

            }
        }

        //10.	檢核單筆限額*/
        if (txnData.getMsgCtl().getMsgctlCheckLimit() != 0) {
            rtnCode = checkTransLimit(txnData.getMsgCtl());
            if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                // 檢核卡片狀況失敗,更新交易記錄
                return rtnCode;
            }
        }


        //11.	檢核銀行別及交易帳號
        //轉帳、繳稅、全繳
        if ("T".equals(txcode) || "E".equals(txcode)) {
            /* 檢核轉入/轉出帳號是否相同 */
            if (getFeptxn().getFeptxnTroutActno().equals(getFeptxn().getFeptxnTrinActno())
                    && getFeptxn().getFeptxnTroutBkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                rtnCode = FEPReturnCode.TranInACTNOSameAsTranOut; /// * 轉出入帳號相同 */
                return rtnCode;
            }

        }


        //12.	檢核中文附言欄不得超過20個中文字
        if (StringUtils.isNotBlank(getFeptxn().getFeptxnChrem()) && getFeptxn().getFeptxnChrem().length() > 20) {
            rtnCode = ATMReturnCode.OtherCheckError;
            getLogContext().setRemark("CheckBody-中文附言欄不得超過20個中文字  CHREM:" + getFeptxn().getFeptxnChrem());
            this.logMessage(getLogContext());
            return rtnCode;
        }

        //13.   轉帳交易檢核轉入帳號
        if("T".equals(txcode.substring(0,1)) && !"253".equals(getFeptxn().getFeptxnPcode().substring(0,3))){
            if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
                rtnCode = ATMReturnCode.TranInACTNOError;
                //轉入帳號狀況檢核錯誤, 轉入帳號={ FEPTXN_TRIN_ACTNO }*/
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }
        }


        return rtnCode;
    }

    /**
     * 1. 	組回應電文回給 HCE
     */
    public String prepareHCEResponseData(Object tota) throws Exception {
        String rtnMessage = "";
        SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        try {
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getHeader();
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getmHCEtxData().getTxObject().getRequest().getBody().getRq().getSvcRq();
            SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
            SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
            msgrs.setHeader(header);
            msgrs.setSvcRs(body);
            rsbody.setRs(msgrs);
            hceRs.setBody(rsbody);

            header.setCLIENTTRACEID(feptxn.getFeptxnChannelEjfno());
            header.setCHANNEL(feptxn.getFeptxnChannel());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());
            body.setTCBRTNCODE(feptxn.getFeptxnCbsRc());

            String errMsg;
            if (StringUtils.isNotBlank(feptxntcb.getFeptxntcbErrpgm())) {
                errMsg = feptxn.getFeptxnCbsRc();
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnErrMsg())) {
                errMsg = feptxn.getFeptxnErrMsg();
            } else {
                errMsg = "";
            }
            header.setSTATUSDESC(errMsg);

            if (StringUtils.isNotBlank(feptxn.getFeptxnCbsRc()) && !"000".equals(feptxn.getFeptxnCbsRc())) {
                header.setSTATUSCODE(feptxntcb.getFeptxntcbImsrc4Fisc());
                header.setSEVERITY("ERROR");
                body.setTCBRTNCODE(feptxntcb.getFeptxntcbImsrcTcb());
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc()) && !"4001".equals(feptxn.getFeptxnRepRc())) {
                header.setSTATUSCODE(String.valueOf(feptxn.getFeptxnRepRc()));
                header.setSEVERITY("ERROR");
            } else if (feptxn.getFeptxnTxrust() != "A") {
                //FEP檢核錯誤
                header.setSTATUSCODE(feptxn.getFeptxnReplyCode());
                header.setSEVERITY("ERROR");
            }else {
                header.setSTATUSCODE("4001");
                header.setSTATUSDESC("");
                header.setSEVERITY("INFO");
                body.setTRANSFROUTBAL(feptxn.getFeptxnBalb());
                body.setTRANSOUTAVBL(feptxn.getFeptxnBala());
                body.setCUSTPAYFEE(feptxn.getFeptxnFeeCustpay());
            }

            body.setOUTDATE(feptxn.getFeptxnTxDate());
            body.setOUTTIME(feptxn.getFeptxnTxTime());
            body.setFEP_EJNO(String.valueOf(feptxn.getFeptxnEjfno()));
            body.setTXNSTAN(feptxn.getFeptxnStan());
            body.setCUSTOMERID(feptxn.getFeptxnIdno());
            body.setTXNTYPE(atmReqbody.getTXNTYPE());
            body.setFSCODE(feptxn.getFeptxnTxCode());
            body.setACCTDATE(formatDate(feptxn.getFeptxnTbsdy()));

            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()))) {
                body.setHOSTACC_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue()));

            }
            if (StringUtils.isNotBlank(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()))) {
                body.setHOSTRVS_FLAG(this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue()));
            }
            if (feptxn.getFeptxnTxAmt() != null) {
                body.setTRANSAMT(feptxn.getFeptxnTxAmt());
            }
            body.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
            body.setTRNSFROUTACCNT(feptxn.getFeptxnTroutActno());
            body.setCLEANBRANCHOUT(feptxn.getFeptxnBrno());
            body.setCLEANBRANCHIN(feptxn.getFeptxnTrinBrno());
            body.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
            body.setTRNSFRINACCNT(feptxn.getFeptxnTrinActno());

            getLogContext().setProgramName("prepareHCEResponseData");
            logMessage(Level.INFO, getLogContext());

            rtnMessage = XmlUtil.toXML(hceRs);

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }

    public IVRData getmIVRtxData() {
        return mIVRtxData;
    }

    public void setmIVRtxData(IVRData mIVRtxData) {
        this.mIVRtxData = mIVRtxData;
    }

    public HCEData getmHCEtxData() {
        return mHCEtxData;
    }

    public void setmHCEtxData(HCEData mHCEtxData) {
        this.mHCEtxData = mHCEtxData;
    }

    public NBData getmNBtxData() {
        return mNBtxData;
    }

    public void setmNBtxData(NBData mNBtxData) {
        this.mNBtxData = mNBtxData;
    }

    public ATMData getmTxData() {
        return mTxData;
    }

    public void setmTxData(ATMData mTxData) {
        this.mTxData = mTxData;
    }

    public RCV_HCE_GeneralTrans_RQ getmHCEReq() {
        return mHCEReq;
    }

    public void setmHCEReq(RCV_HCE_GeneralTrans_RQ mHCEReq) {
        this.mHCEReq = mHCEReq;
    }

    public final FEPReturnCode ChangeCBSDate(Zone defZone) throws Exception {
        //1. 	主機換日處理
        ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);

        defZone.setZoneLlbsdy(defZone.getZoneLbsdy());
        defZone.setZoneLbsdy(defZone.getZoneTbsdy());
        defZone.setZoneTbsdy(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
        defZone.setZoneNbsdy(SysStatus.getPropertyValue().getSysstatNbsdyFisc());
        defZone.setZoneWeekno(defZone.getZoneWeekno());
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
        Integer s = Integer.parseInt(currentTime.format(formatter));
        defZone.setZoneChgdayTime(s);
        defZone.setZoneChgday((short) 0);
        defZone.setZoneCbsMode((short) 1);

        if (zoneExtMapper.updateByPrimaryKeySelective(defZone) < 1) {
            //更新失敗
            getLogContext().setpCode("5102");
            getLogContext().setDesBkno("950");
            getLogContext().setFiscRC("0000");
            getLogContext().setMessageGroup("1"); /* OPC */
            FEPReturnCode InfoRC = FEPReturnCode.CBSBusinessDateChangeToYMD;
            getLogContext().setMessageParm13("自行換日");
            getLogContext().setMessageParm14(defZone.getZoneTbsdy());
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
            sendEMS(getLogContext());
            return FEPReturnCode.UpdateFail;
        }

        //2. 	SendEMS
        /* 需顯示換日訊息於EMS, 為此LogData新增欄位如下*/
        getLogContext().setpCode("5102");
        getLogContext().setDesBkno("950");
        getLogContext().setFiscRC("0000");
        getLogContext().setMessageGroup("1"); /* OPC */
        FEPReturnCode InfoRC = FEPReturnCode.CBSBusinessDateChangeToYMD;
        getLogContext().setMessageParm13("自行換日");
        getLogContext().setMessageParm14(defZone.getZoneTbsdy());
        getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
        sendEMS(getLogContext());

        return FEPReturnCode.Normal;
    }

    public FEPReturnCode ChangeZoneDay(String IMSBUSINESS_DATE) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        Zone zone = getZoneByZoneCode(ZoneCode.TWN);
        if (zone == null) {
            return IOReturnCode.ZONENotFound;
        }

        //IMSBUSINESS_DATE > ZONE_TBSDY
        this.logContext.setMessage("IMSBUSINESS_DATE:" + IMSBUSINESS_DATE);
        this.logContext.setRemark("ZoneTbsdy:" + zone.getZoneTbsdy());
        logMessage(this.logContext);
        SimpleDateFormat sdformat = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);

        if (StringUtils.isNotBlank(IMSBUSINESS_DATE) &&
                sdformat.parse(IMSBUSINESS_DATE).compareTo(sdformat.parse(zone.getZoneTbsdy())) > 0) {
            zone.setZoneLlbsdy(zone.getZoneLbsdy()); /* 上營業日搬入上上營業日*/
            zone.setZoneLbsdy(zone.getZoneTbsdy()); /* 本營業日搬入上營業日*/
            zone.setZoneTbsdy(IMSBUSINESS_DATE);  /*自行本營業日*/
            zone.setZoneNbsdy(SysStatus.getPropertyValue().getSysstatNbsdyFisc()); /*取得財金次營業日*/
            zone.setZoneWeekno(Short.valueOf(String.valueOf(getWeekOfYear(zone.getZoneNbsdy()))));
            zone.setZoneChgdayTime(Integer.parseInt(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
            zone.setZoneChgday((short) 0);
            zone.setZoneCbsMode((short) 1);

            //update
            if (zoneMapper.updateByPrimaryKeySelective(zone) != 1) {
                rc = FEPReturnCode.UpdateFail;
            }
            /* 顯示換日訊息於EMS, 為此LogData新增欄位如下*/
            getLogContext().setpCode(feptxn.getFeptxnPcode());
            getLogContext().setDesBkno(feptxn.getFeptxnDesBkno());
            getLogContext().setFiscRC(feptxn.getFeptxnReqRc());
            getLogContext().setMessageGroup("1");/*OPC*/
            FEPReturnCode InfoRC = FEPReturnCode.CBSBusinessDateChangeToYMD;

            getLogContext().setMessageParm13("台灣自行");
            getLogContext().setMessageParm14(zone.getZoneTbsdy());
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
        }

        return rc;
    }

    private Integer getWeekOfYear(String dateStr) throws ParseException {
        Date date = new SimpleDateFormat(FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).parse(dateStr);
        GregorianCalendar g = new GregorianCalendar();
        g.setTime(date);
        return g.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 交易通知
     *
     * @return
     * @throws Exception
     */
    public void sendToNotify() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            String noticeType = feptxn.getFeptxnNoticeType();
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToNotify"));
            this.logContext.setRemark("noticeType:" + noticeType);
            logMessage(this.logContext);

            if (StringUtils.isNotBlank(noticeType) && "A".equals(feptxn.getFeptxnTxrust())) {
                switch (noticeType) {
                    case "E": /* Email */
                        rtnCode = SendMail(this.feptxntcb, this.feptxn);
                        break;
                    case "M": /* 簡訊 */
                        rtnCode = SendSMS(this.feptxntcb, this.feptxn);
                        break;
                    case "P": /* 送推播 */
                        rtnCode = SendPush(this.feptxntcb, this.feptxn);
                        break;
                }
                if (rtnCode != FEPReturnCode.Normal) {
                    this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToNotify"));
                    this.logContext.setRemark("send To Notify is error.");
                    sendEMS(getLogContext());
                } else {
                    this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToNotify"));
                    this.logContext.setRemark("send To Notify is ok.");
                    logMessage(this.logContext);
                }
            }

        } catch (Exception ex) {
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendToNotify"));
            sendEMS(this.logContext);
        }
    }

    /**
     * 6/11新增 主機回覆錯誤通知 AA3106 AA3107
     *
     * @return
     * @throws Exception
     */
    public void sendNotifyMail(String templateId, String to, String body) throws Exception {
        try {
            notifyHelper.sendSimpleMail(templateId, to, body, true);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendNotifyMail"));
            this.logContext.setRemark("Send Notify Mail succeed");
            this.logMessage(this.logContext);
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendNotifyMail"));
            this.logContext.setRemark("Send Notify Mail failed");
            sendEMS(this.logContext);
        }
    }


    public FEPReturnCode prepareUpdateIMSData(Object tota) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 2026.2.5 更新FEPTXN
            // 部分交易會轉換FSCODE(ex:繳費委託單位是1888888)
            String FSCODE = this.getImsPropertiesValue(tota, ImsMethodName.FSCODE.getValue());
            if (!StringUtils.equals(FSCODE, feptxn.getFeptxnTxCode().substring(0, 2))) {
                feptxntcb.setFeptxntcbFscode(FSCODE);
            }

            String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
            String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
            // 主機回應代碼
            if (feptxn.getFeptxnFiscFlag() == 0) {
                feptxn.setFeptxnCbsRc(IMSRC_TCB);
            } else {
                feptxn.setFeptxnCbsRc(IMSRC4_FISC);
            }

            // CBS 帳務日(民國年須轉西元年)
            String imsbusinessDate = this.getImsPropertiesValue(tota, ImsMethodName.IMSBUSINESS_DATE.getValue());
            if (StringUtils.isNotBlank(imsbusinessDate)) {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
                if (StringUtils.isNotBlank(imsbusinessDate)) {
                    feptxn.setFeptxnTbsdy(imsbusinessDate);
                }
            }

            String IMSACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue());
            String IMSRVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue());
            // 記帳類別
            if ("Y".equals(IMSACCT_FLAG)) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            }
            if ("Y".equals(IMSRVS_FLAG)) {
                feptxn.setFeptxnAccType((short) 2); // 已沖正
            }
            // 主機交易時間
            String IMS_TXN_TIME = this.getImsPropertiesValue(tota, ImsMethodName.IMS_TXN_TIME.getValue());
            feptxn.setFeptxnCbsTxTime(IMS_TXN_TIME);
            // 合庫轉出帳務分行
            String IMS_FMMBR = this.getImsPropertiesValue(tota, ImsMethodName.IMS_FMMBR.getValue());
            feptxn.setFeptxnBrno(IMS_FMMBR);
            // 合庫轉入帳務分行
            String IMS_TMMBR = this.getImsPropertiesValue(tota, ImsMethodName.IMS_TMMBR.getValue());
            feptxn.setFeptxnTrinBrno(IMS_TMMBR);
            // ATM清算分行別取合庫轉出帳務分行(第二開始取四位)
            feptxn.setFeptxnAtmBrno(feptxn.getFeptxnAtmno().substring(1, 5)); //[2:4]
            // 交易帳號掛帳行
            if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                feptxn.setFeptxnTxBrno(IMS_FMMBR);
            } else if (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                feptxn.setFeptxnTxBrno(IMS_TMMBR);
            }
            // 交易通知方式
            String NOTICE_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_TYPE.getValue());
            if (StringUtils.isNotBlank(NOTICE_TYPE)) {
                feptxn.setFeptxnNoticeType(NOTICE_TYPE);
            }
            // 傳送2160給財金
            String SEND_FISC2160 = this.getImsPropertiesValue(tota, ImsMethodName.SEND_FISC2160.getValue());
            if (StringUtils.isNotBlank(SEND_FISC2160)) {
                feptxn.setFeptxnSend2160(SEND_FISC2160);
                // 2160電文須帶原交易Response Code(自行以主機下送的四碼提供)
                if (StringUtils.isBlank(feptxn.getFeptxnRepRc())) {
                    feptxn.setFeptxnRepRc(IMSRC4_FISC);
                }
            }
            //卡片帳號
            String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
            if (StringUtils.isNotBlank(FROMACT)) {
                feptxn.setFeptxnTroutActno(StringUtils.leftPad(FROMACT.trim(), 16, "0"));
            }
            // 手續費
            String TXNCHARGE = this.getImsPropertiesValue(tota, ImsMethodName.TXNCHARGE.getValue());
            if (StringUtils.isNotBlank(TXNCHARGE) && !StringUtils.equals("0", TXNCHARGE)) {
                if ("US".equals(feptxn.getFeptxnTxCode()) || "JP".equals(feptxn.getFeptxnTxCode())) {
                    //手續費折算原幣別
                    feptxn.setFeptxnFeeCustpayAct(new BigDecimal(TXNCHARGE));
                    //提領當地手續費
                    if ("TWD".equals(feptxn.getFeptxnTxCur())) {
                        feptxn.setFeptxnFeeCustpay(new BigDecimal(TXNCHARGE));
                    } else {
                        // 外幣
                        feptxn.setFeptxnFeeCustpay(new BigDecimal(TXNCHARGE).divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP));
                    }
                } else {
                    feptxn.setFeptxnFeeCustpay(new BigDecimal(TXNCHARGE));
                }
            }
            //帳戶餘額及可用餘額(本行記帳或沖正且下送有值才須更新)
            if (Arrays.asList("I1", "I2").contains(feptxn.getFeptxnTxCode()) || Arrays.asList("1", "2").contains(String.valueOf(feptxn.getFeptxnAccType()))) {
                //帳戶餘額
                String ACTBALANCE = this.getImsPropertiesValue(tota, ImsMethodName.ACTBALANCE.getValue());
                if (StringUtils.isNotBlank(ACTBALANCE) && !StringUtils.equals("0", ACTBALANCE)) {
                    feptxn.setFeptxnBalb(new BigDecimal(ACTBALANCE));
                }
                //可用餘額
                String AVAILABLE_BALANCE = this.getImsPropertiesValue(tota, ImsMethodName.AVAILABLE_BALANCE.getValue());
                if (StringUtils.isNotBlank(AVAILABLE_BALANCE) && !StringUtils.equals("0", AVAILABLE_BALANCE)) {
                    feptxn.setFeptxnBala(new BigDecimal(AVAILABLE_BALANCE));
                }
            }
            // 帳戶補充資訊
            String O_ACT = this.getImsPropertiesValue(tota, ImsMethodName.O_ACT.getValue());
            if (StringUtils.isNotBlank(O_ACT)) {
                feptxn.setFeptxnAcctSup(O_ACT);
            }
            // 促銷應用訊息
            String LUCKYNO = this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue());
            if (StringUtils.isNotBlank(LUCKYNO)) {
                feptxn.setFeptxnLuckyno(LUCKYNO);
            }
            // 轉入帳號
            String TOACT = this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue());
            if (StringUtils.isNotBlank(TOACT)) {
                feptxntcb.setFeptxntcbToact(TOACT);
            }
            // 本行實際轉入帳號
            String HBKNOTOACT = this.getImsPropertiesValue(tota, ImsMethodName.HBKNO_TOACT.getValue());
            if (StringUtils.isNotBlank(HBKNOTOACT)) {
                if (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                    feptxn.setFeptxnTrinActnoActual(HBKNOTOACT);
                    feptxntcb.setFeptxntcbHbknoToact(HBKNOTOACT);
                }
                feptxntcb.setFeptxntcbToact(TOACT);
            }
            // 金融卡卡序號(CBP1TI001)
            String CARDSEQ = this.getImsPropertiesValue(tota, ImsMethodName.CARDSEQ.getValue());
            if (StringUtils.isNotBlank(CARDSEQ)) {
                feptxn.setFeptxnCardSeq(Short.valueOf(CARDSEQ));
            }
            // 磁條2軌資料
            String T2DATA = this.getImsPropertiesValue(tota, ImsMethodName.T2DATA.getValue());
            if (StringUtils.isNotBlank(T2DATA)) {
                feptxn.setFeptxnTrk2(T2DATA);
            }
            // 磁條3軌資料
            String T3DATA = this.getImsPropertiesValue(tota, ImsMethodName.T3DATA.getValue());
            if (StringUtils.isNotBlank(T3DATA)) {
                feptxn.setFeptxnTrk3(T3DATA);
            }
            // 主機失敗註記
            String IMS_ERRPGM = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ERRPGM.getValue());
            if (StringUtils.isNotBlank(IMS_ERRPGM)) {
                feptxn.setFeptxnErrMsg(IMS_ERRPGM);
            }

            // ATM 金融帳戶核驗
            String AEIPYTP = this.getImsPropertiesValue(tota, ImsMethodName.AEIPYTP.getValue());
            if (StringUtils.isNotBlank(AEIPYTP)) { //交易類別<2566-10>晶片卡核驗
                String AEILFRC1 = this.getImsPropertiesValue(tota, ImsMethodName.AEILFRC1.getValue()); //項目核驗結果
                String AEILFRC2 = this.getImsPropertiesValue(tota, ImsMethodName.AEILFRC2.getValue()); //帳號核驗結果
                String OPENACT = this.getImsPropertiesValue(tota, ImsMethodName.OPENACT.getValue()); //開戶狀態
                feptxn.setFeptxnRemark(AEILFRC1 + AEILFRC2 + OPENACT);
            }

            // 更新FEPTXNTCB
            // 交易分類
            if (feptxn.getFeptxnFiscFlag() == 0 || "D6".equals(feptxn.getFeptxnTxCode())) {
                feptxntcb.setFeptxntcbTxnflow("C"); // 自行
            } else {
                feptxntcb.setFeptxntcbTxnflow("A"); // 跨行
            }
            // 主機回應代碼
            feptxntcb.setFeptxntcbImsrc4Fisc(IMSRC4_FISC);
            // 合庫錯誤代碼
            feptxntcb.setFeptxntcbImsrcTcb(IMSRC_TCB);
            // 主機記帳狀態
            feptxntcb.setFeptxntcbImsacctFlag(IMSACCT_FLAG);
            // 主機沖帳狀態
            feptxntcb.setFeptxntcbImsrvsFlag(IMSRVS_FLAG);
            // 交易通知方式
            if (StringUtils.isNotBlank(NOTICE_TYPE)) {
                // 傳送通知代碼
                String NOTICE_NUMBER = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_NUMBER.getValue());
                if (StringUtils.isNotBlank(NOTICE_NUMBER)) {
                    feptxntcb.setFeptxntcbNoticeNumber(NOTICE_NUMBER);
                }
                // 客戶身分證統編
                String NOTICE_CUSID = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_CUSID.getValue());
                if (StringUtils.isNotBlank(NOTICE_CUSID)) {
                    feptxntcb.setFeptxntcbNoticeCusid(NOTICE_CUSID);
                }
                // 手機門號
                String NOTICE_MOBILENO = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_MOBILENO.getValue());
                if (StringUtils.isNotBlank(NOTICE_MOBILENO)) {
                    feptxntcb.setFeptxntcbNoticeMobileno(NOTICE_MOBILENO);
                }
                // EMAIL
                String NOTICE_EMAIL = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue());
                if (StringUtils.isNotBlank(NOTICE_EMAIL)) {
                    feptxntcb.setFeptxntcbNoticeEmail(NOTICE_EMAIL);
                }
            }
            //交易種類
            String TXNTYPE_CODE = this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue());
            if (StringUtils.isNotBlank(TXNTYPE_CODE)) {
                feptxntcb.setFeptxntcbTxntypeCode(TXNTYPE_CODE);
            }
            //轉出行
            String FROMBANK = this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue());
            if (StringUtils.isNotBlank(FROMBANK)) {
                feptxntcb.setFeptxntcbFrombank(FROMBANK);
            }
            //轉出帳號
            if (StringUtils.isNotBlank(FROMACT)) {
                feptxntcb.setFeptxntcbFromact(FROMACT);
            }
            //轉入行
            String TOBANK = this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue());
            if (StringUtils.isNotBlank(TOBANK)) {
                feptxntcb.setFeptxntcbTobank(TOBANK);
            }
            // 所屬帳務分行(HCE代理使用)
            String CLEANBRANCHOUT = this.getImsPropertiesValue(tota, ImsMethodName.CLEANBRANCHOUT.getValue());
            if (StringUtils.isNotBlank(CLEANBRANCHOUT)) {
                feptxntcb.setFeptxntcbCleanbranchout(CLEANBRANCHOUT);
            }
            // 手續費負擔別(EDI,EOI管道出報表使用)
            String FEEPAYMENTTYPE = this.getImsPropertiesValue(tota, ImsMethodName.FEEPAYMENTTYPE.getValue());
            if (StringUtils.isNotBlank(FEEPAYMENTTYPE)) {
                feptxntcb.setFeptxntcbFeepaymenttype(FEEPAYMENTTYPE);
            }
            // 優惠手續費負擔分行
            String CHAFEE_BRANCH = this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue());
            if (StringUtils.isNotBlank(CHAFEE_BRANCH)) {
                feptxntcb.setFeptxntcbChafeeBranch(CHAFEE_BRANCH);
            }
            // 手續費減免金額
            String CHAFEEAMT = this.getImsPropertiesValue(tota, ImsMethodName.CHAFEEAMT.getValue());
            if (StringUtils.isNotBlank(CHAFEEAMT)) {
                feptxntcb.setFeptxntcbChafeeamt(Integer.valueOf(CHAFEEAMT));
            }
            // 轉入帳號客戶性質
            String TRTOCUST = this.getImsPropertiesValue(tota, ImsMethodName.TRTOCUST.getValue());
            if (StringUtils.isNotBlank(TRTOCUST)) {
                feptxntcb.setFeptxntcbTrtocust(TRTOCUST);
            }
            // 轉出分行 (BBB+ 財稅代號，AB_TAX_O001)
            String TAX_FROM_BRANCH = this.getImsPropertiesValue(tota, ImsMethodName.TAX_FROM_BRANCH.getValue());
            if (StringUtils.isNotBlank(TAX_FROM_BRANCH)) {
                feptxntcb.setFeptxntcbTaxFromBranch(TAX_FROM_BRANCH);
            }
            // 外幣提款表單編號(AB_WD_O001)
            String FWDTMEX_NO = this.getImsPropertiesValue(tota, ImsMethodName.FWDTMEX_NO.getValue());
            if (StringUtils.isNotBlank(FWDTMEX_NO)) {
                feptxntcb.setFeptxntcbFwdtmexNo(FWDTMEX_NO);
            }
            // 授權金額(AB_WD_O001)
            String AUTH_AMT = this.getImsPropertiesValue(tota, ImsMethodName.AUTH_AMT.getValue());
            if (StringUtils.isNotBlank(AUTH_AMT)) {
                feptxntcb.setFeptxntcbAuthAmt(Integer.valueOf(AUTH_AMT));
            }
            // 國際卡號(AB_WQ_O001)
            String EMVCARD = this.getImsPropertiesValue(tota, ImsMethodName.EMVCARD.getValue());
            if (StringUtils.isNotBlank(EMVCARD)) {
                feptxntcb.setFeptxntcbEmvcard(EMVCARD);
            }
            // 手機簡訊驗證碼(CB_DX_O001)
            String DXTVC = this.getImsPropertiesValue(tota, ImsMethodName.DXTVC.getValue());
            if (StringUtils.isNotBlank(DXTVC)) {
                getFeptxntcb().setFeptxntcbDxtvc(DXTVC);
            }
            // 主機訊息描述(CB_FC1_O001)
            String ERRPGM = this.getImsPropertiesValue(tota, ImsMethodName.ERRPGM.getValue());
            if (StringUtils.isNotBlank(ERRPGM)) {
                feptxntcb.setFeptxntcbErrpgm(ERRPGM);
            }
            // 留置卡片註記(CB_FC1_O001)
            String PRCRDACT = this.getImsPropertiesValue(tota, ImsMethodName.PRCRDACT.getValue());
            if (StringUtils.isNotBlank(PRCRDACT)) {
                if (StringUtils.isBlank(feptxntcb.getFeptxntcbPbmcrd())) {  //有值時不更新
                    feptxntcb.setFeptxntcbPbmcrd(PRCRDACT);
                }
            }
            // 留置卡片註記(CB_FC1_O001)
            String PBMCRD = this.getImsPropertiesValue(tota, ImsMethodName.PBMCRD.getValue());
            if (StringUtils.isNotBlank(PBMCRD)) { //FC1前置交易
                feptxntcb.setFeptxntcbPbmcrd(PBMCRD);
            }
            // 交易日註記(AB_TAX_VO_O02)
            String OVTPT = this.getImsPropertiesValue(tota, ImsMethodName.OVTPT.getValue());
            if (StringUtils.isNotBlank(OVTPT)) {
                feptxntcb.setFeptxntcbOvtpt(OVTPT);
            }
            // 手續費優惠類別(AB_TAX_VO_O02)
            String OVFEETY = this.getImsPropertiesValue(tota, ImsMethodName.OVFEETY.getValue());
            if (StringUtils.isNotBlank(OVFEETY)) {
                feptxntcb.setFeptxntcbOvfeety(OVFEETY);
            }
            // 轉出存戶性質別(AB_TAX_VO_O02)
            String OVCIRCU = this.getImsPropertiesValue(tota, ImsMethodName.OVCIRCU.getValue());
            if (StringUtils.isNotBlank(OVCIRCU)) {
                feptxntcb.setFeptxntcbOvcircu(OVCIRCU);
            }
            // EAI交易類別(AB_VA_O001)
            String EIPYTP = this.getImsPropertiesValue(tota, ImsMethodName.EIPYTP.getValue());
            if (StringUtils.isNotBlank(EIPYTP)) {
                feptxntcb.setFeptxntcbAeipytp(EIPYTP);
            }
            // 行動電話異動核驗(AB_VA_O001)
            String MNO_CHANGE = this.getImsPropertiesValue(tota, ImsMethodName.MNO_CHANGE.getValue());
            if (StringUtils.isNotBlank(MNO_CHANGE)) {
                feptxntcb.setFeptxntcbMnoChange(MNO_CHANGE);
            }
            // 全繳帳務代理分行(AB_PY_O011)
            String PY_HOST_BRCH = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
            if (StringUtils.isNotBlank(PY_HOST_BRCH)) {
                feptxntcb.setFeptxntcbPyHostBrch(PY_HOST_BRCH);
            }
            // 業單位應付手續費(AB_PY_O011)
            String PY_HOST_CHARGE = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
            if (StringUtils.isNotBlank(PY_HOST_CHARGE)) {
                feptxntcb.setFeptxntcbPyHostCharge(Integer.valueOf(PY_HOST_CHARGE));
            }
            // 手續費註記(AB_PY_O011)
            String PY_CHARGE_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.PY_CHARGE_FLAG.getValue());
            if (StringUtils.isNotBlank(PY_CHARGE_FLAG)) {
                feptxntcb.setFeptxntcbPyChargeFlag(PY_CHARGE_FLAG);
            }
            // 信用卡卡別
            if (StringUtils.isNotBlank(feptxn.getFeptxnTrk2())) {
                if ("901".equals(feptxn.getFeptxnTrk2().substring(33, 36))) { //FEPTXN_TRK2[34:3]
                    if (StringUtils.equals(feptxn.getFeptxnTrinBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) {
                        feptxntcb.setFeptxntcbCardf("C"); // C:本行卡
                    } else {
                        feptxntcb.setFeptxntcbCardf("A"); //A :國內其它銀行卡片
                    }
                } else {
                    feptxntcb.setFeptxntcbCardf("I"); // I:國外卡
                }
            }
            // 轉帳註記
            String TR_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.TR_TYPE.getValue());
            if (StringUtils.isNotBlank(TR_TYPE)) {
                feptxntcb.setFeptxntcbTrtype(TR_TYPE);
            }
            // 轉帳註記"SA"的完整虛擬帳號
            String SA_ACT = this.getImsPropertiesValue(tota, ImsMethodName.SA_ACT.getValue());
            if (StringUtils.isNotBlank(SA_ACT)) {
                feptxntcb.setFeptxntcbSaact(SA_ACT);
            }
            // 設備支付類別
            String IMS_ICTTYPEP = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ICTTYPEP.getValue());
            if (StringUtils.isNotBlank(IMS_ICTTYPEP)) {
                feptxntcb.setFeptxntcbIcttypep(IMS_ICTTYPEP);
            }
            // 交易管道或支付型態
            String IMS_ATXCHNNL = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ATXCHNNL.getValue());
            if (StringUtils.isNotBlank(IMS_ATXCHNNL)) {
                feptxntcb.setFeptxntcbAtxchnnl(IMS_ATXCHNNL);
            }
        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareUpdateIMSData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }
        return rtnCode;
    }

    /**
     * 外圍更新FEPTXNTCB使用
     *
     * @param tota
     * @return
     */
    public FEPReturnCode other_prepareUpdateIMSData(Object tota) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 2026.2.4 LeYun 填入欄位調整
            // 更新FEPTXNTCB
            // 1.交易分類
            if (feptxn.getFeptxnFiscFlag() == 0) {
                feptxntcb.setFeptxntcbTxnflow("C"); // 自行
            } else {
                feptxntcb.setFeptxntcbTxnflow("A"); // 跨行
            }
            // 2.主機回應代碼
            String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
            feptxntcb.setFeptxntcbImsrc4Fisc(IMSRC4_FISC);
            // 3.合庫錯誤代碼
            String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
            feptxntcb.setFeptxntcbImsrcTcb(IMSRC_TCB);
            feptxn.setFeptxnCbsRc(IMSRC_TCB);
            // CBS 帳務日(民國年須轉西元年)
            String IMSBUSINESS_DATE = this.getImsPropertiesValue(tota, ImsMethodName.IMSBUSINESS_DATE.getValue());
            if (StringUtils.isNotBlank(IMSBUSINESS_DATE)) {
                feptxn.setFeptxnTbsdy(CalendarUtil.rocStringToADString14(IMSBUSINESS_DATE));
            }
            // 4.主機記帳狀態
            String IMSACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue());
            feptxntcb.setFeptxntcbImsacctFlag(IMSACCT_FLAG);
            if ("Y".equals(IMSACCT_FLAG)) {
                feptxn.setFeptxnAccType((short)1); //已記帳
            }
            // 5.主機沖帳狀態
            String IMSRVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue());
            feptxntcb.setFeptxntcbImsrvsFlag(IMSRVS_FLAG);
            if ("Y".equals(IMSRVS_FLAG)) {
                feptxn.setFeptxnAccType((short)2); //已沖正
            }
            // 主機交易時間
            String IMS_TXN_TIME = this.getImsPropertiesValue(tota, ImsMethodName.IMS_TXN_TIME.getValue());
            feptxn.setFeptxnCbsTxTime(IMS_TXN_TIME);
            // 合庫轉出帳務分行
            String IMS_FMMBR = this.getImsPropertiesValue(tota, ImsMethodName.IMS_FMMBR.getValue());
            feptxn.setFeptxnBrno(IMS_FMMBR);
            // 合庫轉入帳務分行
            String IMS_TMMBR = this.getImsPropertiesValue(tota, ImsMethodName.IMS_TMMBR.getValue());
            feptxn.setFeptxnTrinBrno(IMS_TMMBR);
            // 錯誤註記
            String IMS_ERRPGM = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ERRPGM.getValue());
            if (StringUtils.isNotBlank(IMS_ERRPGM)) {
                feptxntcb.setFeptxntcbImserrpgm(IMS_ERRPGM);
            }

            // 6.交易通知方式
            String NOTICE_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_TYPE.getValue());
            if (StringUtils.isNotBlank(NOTICE_TYPE)) {
                feptxn.setFeptxnNoticeType(NOTICE_TYPE);
                // 7.傳送通知代碼
                feptxntcb.setFeptxntcbNoticeNumber(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_NUMBER.getValue()));
                // 8.客戶身分證統編
                feptxntcb.setFeptxntcbNoticeCusid(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_CUSID.getValue()));
                // 9.手機門號
                feptxntcb.setFeptxntcbNoticeMobileno(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_MOBILENO.getValue()));
                // 10.EMAIL
                feptxntcb.setFeptxntcbNoticeEmail(this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue()));
            }
            String SEND_FISC2160 = this.getImsPropertiesValue(tota, ImsMethodName.SEND_FISC2160.getValue());
            if (StringUtils.isNotBlank(SEND_FISC2160)) {
                feptxn.setFeptxnSend2160(SEND_FISC2160);
            }

            //11.交易種類
            String TXNTYPE_CODE = this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue());
            if (StringUtils.isNotBlank(TXNTYPE_CODE)) {
                feptxntcb.setFeptxntcbTxntypeCode(TXNTYPE_CODE);
            }
            // 交易金額(下送空白不更新)
            String TXNAMT = this.getImsPropertiesValue(tota, ImsMethodName.TXNAMT.getValue());
            if (StringUtils.isNotBlank(TXNAMT) || "0".equals(TXNAMT)) {
                feptxn.setFeptxnTxAmt(new BigDecimal(TXNAMT));
                feptxn.setFeptxnTxAmtAct(new BigDecimal(TXNAMT));
            }
            // 手續費(下送空白不更新)
            String TXNCHARGE = this.getImsPropertiesValue(tota, ImsMethodName.TXNCHARGE.getValue());
            if (StringUtils.isNotBlank(TXNCHARGE) || "0".equals(TXNCHARGE)) {
                feptxn.setFeptxnFeeCustpay(new BigDecimal(TXNCHARGE));
                feptxn.setFeptxnFeeCustpayAct(new BigDecimal(TXNCHARGE));
            }

            //12.轉出行
            String FROMBANK = this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue());
            if (StringUtils.isNotBlank(FROMBANK)) {
                feptxntcb.setFeptxntcbFrombank(FROMBANK);
            }
            //13.轉出帳號
            String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
            if (StringUtils.isNotBlank(FROMACT)) {
                feptxntcb.setFeptxntcbFromact(FROMACT);
            }
            //14.轉入行
            String TOBANK = this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue());
            if (StringUtils.isNotBlank(TOBANK)) {
                feptxntcb.setFeptxntcbTobank(TOBANK);
            }
            //15.轉入帳號
            String TOACT = this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue());
            if (StringUtils.isNotBlank(TOACT)) {
                feptxntcb.setFeptxntcbToact(TOACT);
            }
            // 促銷應用訊息
            String LUCKYNO = this.getImsPropertiesValue(tota, ImsMethodName.LUCKYNO.getValue());
            if (StringUtils.isNotBlank(LUCKYNO)) {
                feptxn.setFeptxnLuckyno(LUCKYNO);
            }
            // 帳戶補充資訊
            String O_ACT = this.getImsPropertiesValue(tota, ImsMethodName.O_ACT.getValue());
            if (StringUtils.isNotBlank(O_ACT)) {
                feptxn.setFeptxnAcctSup(O_ACT);
            }
            // 21.轉出分行 (BBB+ 財稅代號，AB_TAX_O001)
            String TAX_FROM_BRANCH = this.getImsPropertiesValue(tota, ImsMethodName.TAX_FROM_BRANCH.getValue());
            if (StringUtils.isNotBlank(TAX_FROM_BRANCH)) {
                feptxntcb.setFeptxntcbTaxFromBranch(TAX_FROM_BRANCH);
            }
            //EAI管道傳送交易

            // 16.轉出帳號帳務行代號
            String CLEANBRANCHOUT = this.getImsPropertiesValue(tota, ImsMethodName.CLEANBRANCHOUT.getValue());
            if (StringUtils.isNotBlank(CLEANBRANCHOUT)) {
                feptxntcb.setFeptxntcbCleanbranchout(CLEANBRANCHOUT);
            }

            // 18.優惠手續費負擔分行
            String CHAFEE_BRANCH = this.getImsPropertiesValue(tota, ImsMethodName.CHAFEE_BRANCH.getValue());
            if (StringUtils.isNotBlank(CHAFEE_BRANCH)) {
                feptxntcb.setFeptxntcbChafeeBranch(CHAFEE_BRANCH);
            }
            // 17.手續費負擔別(EDI,EOI管道出報表使用)
            String FEEPAYMENTTYPE = this.getImsPropertiesValue(tota, ImsMethodName.FEEPAYMENTTYPE.getValue());
            if (StringUtils.isNotBlank(FEEPAYMENTTYPE)) {
                feptxntcb.setFeptxntcbFeepaymenttype(FEEPAYMENTTYPE);
            }
            // 優惠手續費負擔分行
            String CROSSFEE = this.getImsPropertiesValue(tota, ImsMethodName.CROSSFEE.getValue());
            if (StringUtils.isNotBlank(CROSSFEE)) {
                feptxntcb.setFeptxntcbChafeeBranch(CROSSFEE);
            }
            // 19.手續費減免金額
            String CHAFEEAMT = this.getImsPropertiesValue(tota, ImsMethodName.CHAFEEAMT.getValue()).trim();
            if (StringUtils.isNotBlank(CHAFEEAMT)) {
                feptxntcb.setFeptxntcbChafeeamt(Integer.valueOf(CHAFEEAMT));
            } else {
                feptxntcb.setFeptxntcbChafeeamt(0);
            }
            // .FXML手續費減免金額(2700資金調撥)
            String FXMLCHAFEEAMT = this.getImsPropertiesValue(tota, ImsMethodName.FXML_CHAFEEAMT.getValue()).trim();
            if (StringUtils.isNotBlank(FXMLCHAFEEAMT)) {
                feptxntcb.setFeptxntcbChafeeamt(Integer.valueOf(FXMLCHAFEEAMT));
            } else {
                feptxntcb.setFeptxntcbChafeeamt(0);
            }
            // 轉入帳號客戶性質
            String TRTOCIRCU = this.getImsPropertiesValue(tota, ImsMethodName.TRTOCIRCU.getValue()).trim();
            if (StringUtils.isNotBlank(TRTOCIRCU)) {
                feptxntcb.setFeptxntcbTrtocust(TRTOCIRCU);
            }
            // 合庫實際入帳帳號
            String TOACT006 = this.getImsPropertiesValue(tota, ImsMethodName.TOACT006.getValue());
            if (StringUtils.isNotBlank(TOACT006)) {
                feptxntcb.setFeptxntcbHbknoToact(TOACT006);
                feptxn.setFeptxnTrinActno(TOACT006);
            }
            // 轉帳註記
            String TR_ACT_006 = this.getImsPropertiesValue(tota, ImsMethodName.TR_ACT_006.getValue());
            if (StringUtils.isNotBlank(TR_ACT_006)) {
                feptxntcb.setFeptxntcbTrtype(TR_ACT_006);
            }
            // 轉帳註記"SA"的完整虛擬帳號
            String SA_ACT = this.getImsPropertiesValue(tota, ImsMethodName.SA_ACT.getValue()).trim();
            if (StringUtils.isNotBlank(SA_ACT)) {
                feptxntcb.setFeptxntcbSaact(SA_ACT);
            }

            // 37.全繳帳務代理分行
            String PY_HOST_BRCH = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
            if (StringUtils.isNotBlank(PY_HOST_BRCH)) {
                feptxntcb.setFeptxntcbPyHostBrch(PY_HOST_BRCH);
            }
            // 38.業單位應付手續費
            String PY_HOST_CHARGE = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
            if (StringUtils.isNotBlank(PY_HOST_CHARGE)) {
                feptxntcb.setFeptxntcbPyHostCharge(Integer.valueOf(PY_HOST_CHARGE));
            }
            // 39.手續費註記
            String PY_CHARGE_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE_FLAG.getValue());
            if (StringUtils.isNotBlank(PY_CHARGE_FLAG)) {
                feptxntcb.setFeptxntcbPyChargeFlag(PY_CHARGE_FLAG);
            }
            // 設備支付類別
            String IMS_ICTTYPEP = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ICTTYPEP.getValue());
            if (StringUtils.isNotBlank(IMS_ICTTYPEP)) {
                feptxntcb.setFeptxntcbIcttypep(IMS_ICTTYPEP);
            }
            // 交易管道或支付型態
            String IMS_ATXCHNNL = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ATXCHNNL.getValue());
            if (StringUtils.isNotBlank(IMS_ATXCHNNL)) {
                feptxntcb.setFeptxntcbAtxchnnl(IMS_ATXCHNNL);
            }

        } catch (Exception ex) { // 新增失敗
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareUpdateIMSData");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }

        return rtnCode;
    }

    public FEPReturnCode other_prepareFEPTXNTCB() {
        getFeptxntcb().setFeptxntcbTxDate(getFeptxn().getFeptxnTxDate());
        getFeptxntcb().setFeptxntcbEjfno(getFeptxn().getFeptxnEjfno());
        // 報表判斷條件，避免主機逾時無法更新，先給值
        if ( getFeptxn().getFeptxnFiscFlag()==0 ) {
            getFeptxntcb().setFeptxntcbTxnflow("C"); // 自行
        }else{
            getFeptxntcb().setFeptxntcbTxnflow("A"); // 跨行
        }

        return FEPReturnCode.Normal;
    }

    public FEPReturnCode UpdateFEPTXNTCB(Object tota) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            /* 2026/7/6 點掉, 在INSERT FEPTXNTCB 時寫入交易分類 */

            // 主機回應代碼
            String IMSRC4_FISC = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC4_FISC.getValue());
            feptxntcb.setFeptxntcbImsrc4Fisc(IMSRC4_FISC);

            // 合庫錯誤代碼
            String IMSRC_TCB = this.getImsPropertiesValue(tota, ImsMethodName.IMSRC_TCB.getValue());
            feptxntcb.setFeptxntcbImsrcTcb(IMSRC_TCB);

            // 主機記帳狀態
            String IMSACCT_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSACCT_FLAG.getValue());
            feptxntcb.setFeptxntcbImsacctFlag(IMSACCT_FLAG);

            // 主機沖帳狀態
            String IMSRVS_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.IMSRVS_FLAG.getValue());
            feptxntcb.setFeptxntcbImsrvsFlag(IMSRVS_FLAG);

            // 交易通知方式
            String NOTICE_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_TYPE.getValue());
            if (StringUtils.isNotBlank(NOTICE_TYPE)) {
                //傳送通知代碼
                String NOTICE_NUMBER = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_NUMBER.getValue());
                feptxntcb.setFeptxntcbNoticeNumber(NOTICE_NUMBER);

                //客戶身分證統編
                String NOTICE_CUSID = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_CUSID.getValue());
                feptxntcb.setFeptxntcbNoticeCusid(NOTICE_CUSID);

                //手機門號
                String NOTICE_MOBILENO = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_MOBILENO.getValue());
                feptxntcb.setFeptxntcbNoticeMobileno(NOTICE_MOBILENO);

                //EMAIL
                String NOTICE_EMAIL = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue());
                feptxntcb.setFeptxntcbNoticeEmail(NOTICE_EMAIL);
            }

            //交易種類
            String TXNTYPE_CODE = this.getImsPropertiesValue(tota, ImsMethodName.TXNTYPE_CODE.getValue());
            if (StringUtils.isNotBlank(TXNTYPE_CODE)) {
                feptxntcb.setFeptxntcbTxntypeCode(TXNTYPE_CODE);
            }

            //轉出行
            String FROMBANK = this.getImsPropertiesValue(tota, ImsMethodName.FROMBANK.getValue());
            if (StringUtils.isNotBlank(FROMBANK)) {
                feptxntcb.setFeptxntcbFrombank(FROMBANK);
            }

            // 轉出帳號
            String FROMACT = this.getImsPropertiesValue(tota, ImsMethodName.FROMACT.getValue());
            if (StringUtils.isNotBlank(FROMACT)) {
                feptxntcb.setFeptxntcbFromact(FROMACT);
            }

            // 轉入行
            String TOBANK = this.getImsPropertiesValue(tota, ImsMethodName.TOBANK.getValue());
            if (StringUtils.isNotBlank(TOBANK)) {
                feptxntcb.setFeptxntcbTobank(TOBANK);
            }

            // 轉入帳號
            String TOACT = this.getImsPropertiesValue(tota, ImsMethodName.TOACT.getValue());
            if (StringUtils.isNotBlank(TOACT)) {
                feptxntcb.setFeptxntcbToact(TOACT);
            }

            /* 2025/3/24 新增寫入 FEPTXNTCB */
            // 本行實際轉入帳號
            String HBKNO_TOACT = this.getImsPropertiesValue(tota, ImsMethodName.HBKNO_TOACT.getValue());
            if (StringUtils.equals(HBKNO_TOACT, SysStatus.getPropertyValue().getSysstatHbkno()) && StringUtils.isNotBlank(HBKNO_TOACT)) {
                feptxn.setFeptxnTrinActnoActual(HBKNO_TOACT);
                feptxntcb.setFeptxntcbHbknoToact(HBKNO_TOACT);
            }

            // 全繳帳務代理分行(IB_PY_O011)
            String PY_HOST_BRCH = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_BRCH.getValue());
            if (StringUtils.isNotBlank(PY_HOST_BRCH)) {
                feptxntcb.setFeptxntcbPyHostBrch(PY_HOST_BRCH);
            }

            // 業單位應付手續費(IB_PY_O011)
            String PY_HOST_CHARGE = this.getImsPropertiesValue(tota, ImsMethodName.PY_HOST_CHARGE.getValue());
            if (StringUtils.isNotBlank(PY_HOST_CHARGE) && StringUtils.isNumeric(PY_HOST_CHARGE)) {
                feptxntcb.setFeptxntcbPyHostCharge(Integer.valueOf(PY_HOST_CHARGE));
            }

            // 手續費註記(IB_PY_O011)
            String PY_CHARGE_FLAG = this.getImsPropertiesValue(tota, ImsMethodName.PY_CHARGE_FLAG.getValue());
            if (StringUtils.isNotBlank(PY_CHARGE_FLAG)) {
                feptxntcb.setFeptxntcbPyChargeFlag(PY_CHARGE_FLAG);
            }

            /* 2026/6/29 新增寫入 FEPTXNTCB */
            // 活動型態(IB_WD_O011)
            String ACTIVITY_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.ACTIVITY_TYPE.getValue());
            if (StringUtils.isNotBlank(ACTIVITY_TYPE)) {
                feptxntcb.setFeptxntcbActivityType(ACTIVITY_TYPE);
            }

            // 轉出分行 (BBB+ 財稅代號)
            String TAX_FROM_BRANCH = this.getImsPropertiesValue(tota, ImsMethodName.TAX_FROM_BRANCH.getValue());
            if (StringUtils.isNotBlank(TAX_FROM_BRANCH)) {
                feptxntcb.setFeptxntcbTaxFromBranch(TAX_FROM_BRANCH);
            }

            /* 2024/7/26 增加 消費扣款(2541)紅利點數相關欄位*/
            String O_REDEEM_STATE = this.getImsPropertiesValue(tota, ImsMethodName.O_Redeem_State.getValue());
            if (StringUtils.isNotBlank(O_REDEEM_STATE)) {
                feptxntcb.setFeptxntcbORedeemState(O_REDEEM_STATE);
            }

            String O_ADD_POINT = this.getImsPropertiesValue(tota, ImsMethodName.O_Add_Point.getValue());
            if (StringUtils.isNotBlank(O_ADD_POINT) && StringUtils.isNumeric(O_ADD_POINT)) {
                feptxntcb.setFeptxntcbOAddPoint(Integer.parseInt(O_ADD_POINT));
            }

            String O_DEDUCT_POINT = this.getImsPropertiesValue(tota, ImsMethodName.O_Deduct_Point.getValue());
            if (StringUtils.isNotBlank(O_DEDUCT_POINT) && StringUtils.isNumeric(O_DEDUCT_POINT)) {
                feptxntcb.setFeptxntcbODeductPoint(Integer.parseInt(O_DEDUCT_POINT));
            }

            String O_EXTRA_AMT_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.O_Extra_amt_type.getValue());
            if (StringUtils.isNotBlank(O_EXTRA_AMT_TYPE)) {
                feptxntcb.setFeptxntcbOExtraAmtType(O_EXTRA_AMT_TYPE);
            }

            String O_EXTRA_AMOUNT = this.getImsPropertiesValue(tota, ImsMethodName.O_Extra_Amount.getValue());
            if (StringUtils.isNotBlank(O_EXTRA_AMOUNT) && StringUtils.isNumeric(O_EXTRA_AMOUNT)) {
                feptxntcb.setFeptxntcbOExtraAmount(Integer.parseInt(O_EXTRA_AMOUNT));
            }

            String O_EXTRA_CURR_CODE = this.getImsPropertiesValue(tota, ImsMethodName.O_Extra_Curr_code.getValue());
            if (StringUtils.isNotBlank(O_EXTRA_CURR_CODE)) {
                feptxntcb.setFeptxntcbOExtraCurrCode(O_EXTRA_CURR_CODE);
            }

            String O_FINAL_TX_AMT = this.getImsPropertiesValue(tota, ImsMethodName.O_Final_TX_amt.getValue());
            if (StringUtils.isNotBlank(O_FINAL_TX_AMT) && StringUtils.isNumeric(O_FINAL_TX_AMT)) {
                feptxntcb.setFeptxntcbOFinalTxAmt(Integer.parseInt(O_FINAL_TX_AMT));
            }

            String O_FINAL_TX_CURR_CODE = this.getImsPropertiesValue(tota, ImsMethodName.O_Final_TX_Curr_code.getValue());
            if (StringUtils.isNotBlank(O_FINAL_TX_CURR_CODE)) {
                feptxntcb.setFeptxntcbOFinalTxCurrCode(O_FINAL_TX_CURR_CODE);
            }

            /* 2025/7/9 增加 for 消費扣款交易紅利點數相關資料 */
            String O_BIT46_POT_RFU = this.getImsPropertiesValue(tota, ImsMethodName.O_BIT46_POT_RFU.getValue());
            if (StringUtils.isNotBlank(O_BIT46_POT_RFU)) {
                feptxntcb.setFeptxntcbOBit46PotRfu(O_BIT46_POT_RFU);
            }

            /* 2024/8/19 增加 跨國提款(2410/2450)告誡註記*/
            String CIWAFLG = this.getImsPropertiesValue(tota, ImsMethodName.CIWAFLG.getValue());
            if (StringUtils.isNotBlank(CIWAFLG)) {
                feptxntcb.setFeptxntcbCiwaflg(CIWAFLG);
            }

            // 2024/8/30 增加 轉帳註記
            String TR_TYPE = this.getImsPropertiesValue(tota, ImsMethodName.TR_TYPE.getValue());
            if (StringUtils.isNotBlank(TR_TYPE)) {
                feptxntcb.setFeptxntcbTrtype(TR_TYPE);
            }

            // 2024/8/30 增加完整虛擬帳號
            String SA_ACT = this.getImsPropertiesValue(tota, ImsMethodName.SA_ACT.getValue());
            if (StringUtils.isNotBlank(SA_ACT)) {
                feptxntcb.setFeptxntcbSaact(SA_ACT);
            }
            // 設備支付類別
            String IMS_ICTTYPEP = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ICTTYPEP.getValue());
            if (StringUtils.isNotBlank(IMS_ICTTYPEP)) {
                feptxntcb.setFeptxntcbIcttypep(IMS_ICTTYPEP);
            }
            // 交易管道或支付型態
            String IMS_ATXCHNNL = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ATXCHNNL.getValue());
            if (StringUtils.isNotBlank(IMS_ATXCHNNL)) {
                feptxntcb.setFeptxntcbAtxchnnl(IMS_ATXCHNNL);
            }
            // 主機失敗註記
            String IMS_ERRPGM = this.getImsPropertiesValue(tota, ImsMethodName.IMS_ERRPGM.getValue());
            if (StringUtils.isNotBlank(IMS_ERRPGM)) {
                feptxntcb.setFeptxntcbImserrpgm(IMS_ERRPGM);
            }
            // c
            String PWATXDAY = this.getImsPropertiesValue(tota, ImsMethodName.PWATXDAY.getValue());
            if (StringUtils.isNotBlank(PWATXDAY)) {
                feptxntcb.setFeptxntcbOvtpt(PWATXDAY);
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".UpdateFEPTXNTCB");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }

        return rtnCode;
    }

    public Feptxntcb checkFeptxntcbData(Feptxn conFeptxn) {
        Feptxntcb dbfeptxntcb;
        try {

            dbfeptxntcb = feptxnDao.selectByPrimaryKeyForFeptxntcb(conFeptxn.getFeptxnTxDate(), conFeptxn.getFeptxnEjfno());

            if (dbfeptxntcb == null) {
                feptxntcb = null;
                return feptxntcb;
            }

            return dbfeptxntcb;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return null;
        }
    }

    public Channel getChannel(String channelName) {
        Channel channel = null;
        if (StringUtils.isNotBlank(channelName)) {
            List<Channel> channelList = channelExtMapper.selectByChannelName(channelName);
            if (channelList.size() > 0) {
                channel = channelList.get(0);
            }
        }
        return channel;
    }

    /**
     * AES-GCM加密
     *
     * @param id
     * @param issBank
     * @param stan
     * @param txDateTime
     * @return
     */
    public String encryptString(String id, String issBank, String stan, String txDateTime) {
        try {
            String key = issBank + stan + 17;
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, txDateTime.getBytes());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] result = cipher.doFinal(id.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return id;
        }
    }

    public String encryptSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 西元日期(yyyyMMdd)轉成民國日期(yyyMMdd)
     *
     * @param date
     * @return
     */
    private String formatDate(String date) {
        if (StringUtils.isBlank(date)) {
            return date;
        } else if (date.length() != 8) {
            date = StringUtils.leftPad(StringUtils.right(date, 8), 8, '0');
        }
        StringBuilder sb = new StringBuilder();
        date = CalendarUtil.adStringToROCString(date);
        int dateLength = date.length();
        String year = StringUtils.substring(date, 0, dateLength - 4);
        String month = StringUtils.substring(date, dateLength - 4, dateLength - 2);
        String day = StringUtils.substring(date, dateLength - 2);
        sb.append(year).append(month).append(day);
        return sb.toString();
    }

    private String defultEmptyString(String str) {
        String returnStr = " ";
        if (StringUtils.isBlank(str))
            returnStr = str;
        return returnStr;
    }
}
