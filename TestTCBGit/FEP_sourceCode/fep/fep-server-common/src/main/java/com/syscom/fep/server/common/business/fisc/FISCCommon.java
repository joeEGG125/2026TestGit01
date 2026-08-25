package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.converter.SyscomConverter;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.frmcommon.astarloadutils.AStarLoadUtils;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.mybatis.util.StanGenerator;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.adapter.UnisysAdapter;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.fisc.*;
import com.syscom.fep.vo.text.fisc.FISC_INBK.DefIC_DATA;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FISCCommon extends BusinessBase {
    private FISC_INBK fiscINBKReq; // INBK REQ電文物件
    private FISC_INBK fiscINBKRes; // INBK RES電文物件
    private FISC_OPC fiscOPCReq;
    private FISC_CLR fiscCLRRes;
    private FISC_USDCLR fiscFCCLRRes; // FCCLR RES電文物件
    private FISC_EMVIC fiscEMVICRes; // EMVIC RES電文物件
    private FISC_CLR fiscCLRReq; // CLR REQ電文物件
    private FISC_USDCLR fiscFCCLRReq; // FCCLR REQ的電文物件
    private FISC_EMVIC fiscEMVICReq; // EMVIC REQ的電文物件
    private FISC_INBK fiscINBKCon; // INBK CON電文物件
    private FISC_OPC fiscOPCCon; // OPC CON電文物件
    private FISC_EMVIC fiscEMVICCon; // EMVIC RES電文物件
    private FISC_OPC fiscOPCRes; // OPC RES電文物件
    protected FISC_RM fiscRMReq; // RM REQ電文物件
    private static final Map<String, String> hUnicodeToCns11643 = new HashMap<>();
    protected FISC_USDRM fiscFCRMReq; // FCRM REQ電文物件
    protected FISC_RM fiscRMRes; // RM RES電文物件
    protected FISC_USDRM fiscFCRMRes; // FCRM RES電文物件
    protected FISCHeader fiscHeader;
    @SuppressWarnings("unused")
    private Feptxn moriginalFEPTxn; // Response or Confirm時用來存放原始交易的FEPTxn記錄
    private Inbkpend mINBKPEND;
    private HashMap<String, Bitmapdef> bitmapdefMap = new HashMap<String, Bitmapdef>();
    @SuppressWarnings("unused")
    private String keyIdentify1, keyIdentify2, inputData1, inputData2;
    private static HashMap<String, List<Dataattr>> dataAttrList = new HashMap<String, List<Dataattr>>();

    private SysstatMapper sysstatMapper = SpringBeanFactoryUtil.getBean(SysstatMapper.class);
    private IntltxnExtMapper intltxnExtMapper = SpringBeanFactoryUtil.getBean(IntltxnExtMapper.class);
    private EmvcExtMapper emvcExtMapper = SpringBeanFactoryUtil.getBean(EmvcExtMapper.class);
    private NpsunitMapper npsunitMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    private BinMapper binMapper = SpringBeanFactoryUtil.getBean(BinMapper.class);

    private Inbk2160Mapper inbk2160Mapper = SpringBeanFactoryUtil.getBean(Inbk2160Mapper.class);
    private MerchantMapper merchantMapper = SpringBeanFactoryUtil.getBean(MerchantMapper.class);
    private InbkparmExtMapper inbkparmExtMapper = SpringBeanFactoryUtil.getBean(InbkparmExtMapper.class);
    private IctltxnExtMapper ictltxnExtMapper = SpringBeanFactoryUtil.getBean(IctltxnExtMapper.class);
    private AtmmstrMapper atmmstrMapper = SpringBeanFactoryUtil.getBean(AtmmstrMapper.class);
    private CbactExtMapper cbactExtMapper = SpringBeanFactoryUtil.getBean(CbactExtMapper.class);
    private BsdaysMapper bsdaysMapper = SpringBeanFactoryUtil.getBean(BsdaysMapper.class);
    private UpbinExtMapper upbinExtMapper = SpringBeanFactoryUtil.getBean(UpbinExtMapper.class);
    private CardtsmExtMapper cardtsmExtMapper = SpringBeanFactoryUtil.getBean(CardtsmExtMapper.class);
    private VisabinMapper visabinMapper = SpringBeanFactoryUtil.getBean(VisabinMapper.class);
    private BitmapdefExtMapper bitmapdefExtMapper = SpringBeanFactoryUtil.getBean(BitmapdefExtMapper.class);
    private DglimitMapper dglimitMapper = SpringBeanFactoryUtil.getBean(DglimitMapper.class);
    private ObtltxnExtMapper obtltxnExtMapper = SpringBeanFactoryUtil.getBean(ObtltxnExtMapper.class);
    private McbinExtMapper mcbinExtMapper = SpringBeanFactoryUtil.getBean(McbinExtMapper.class);
//    private SmsmsgMapper smsmsgMapper = SpringBeanFactoryUtil.getBean(SmsmsgMapper.class);
//    private SmlparmMapper smlparmMapper = SpringBeanFactoryUtil.getBean(SmlparmMapper.class);
    private QrptxnExtMapper qrptxnExtMapper = SpringBeanFactoryUtil.getBean(QrptxnExtMapper.class);
    private OwlimitMapper owlimitMapper = SpringBeanFactoryUtil.getBean(OwlimitMapper.class);
    private DataattrExtMapper dataattrExtMapper = SpringBeanFactoryUtil.getBean(DataattrExtMapper.class);
    @SuppressWarnings("unused")
    private BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
    private VatxnMapper vatxnMapper = SpringBeanFactoryUtil.getBean(VatxnMapper.class);
    private static final LogHelper TRACELOGGER = LogHelperFactory.getTraceLogger();

    protected FISCCommon() {
        super();
    }

    /**
     * FISCBusiness為處理財金交易相關邏輯之共用程式,故需要傳入FISCData初始化
     *
     * @param fiscMessage 財金通用處理物件
     */
    protected FISCCommon(FISCData fiscMessage) {
        super(fiscMessage);
        setFISCTxData(fiscMessage);
        setGeneralData(getFISCTxData()); // 2010/9/15 add by Danile for SendtoCBS in BusinessBase
        fiscINBKReq = getFISCTxData().getTxObject().getINBKRequest();
        fiscINBKRes = getFISCTxData().getTxObject().getINBKResponse();
        fiscINBKCon = getFISCTxData().getTxObject().getINBKConfirm();
        fiscOPCReq = getFISCTxData().getTxObject().getOPCRequest();
        fiscOPCRes = getFISCTxData().getTxObject().getOPCResponse();
        fiscOPCCon = getFISCTxData().getTxObject().getOPCConfirm();
        fiscRMReq = getFISCTxData().getTxObject().getRMRequest();
        fiscRMRes = getFISCTxData().getTxObject().getRMResponse();
        fiscFCRMReq = getFISCTxData().getTxObject().getFCRMRequest();
        fiscFCRMRes = getFISCTxData().getTxObject().getFCRMRequest();
        fiscCLRReq = getFISCTxData().getTxObject().getCLRRequest();
        fiscCLRRes = getFISCTxData().getTxObject().getCLRResponse();
        fiscFCCLRReq = getFISCTxData().getTxObject().getFCCLRRequest();
        fiscFCCLRRes = getFISCTxData().getTxObject().getFCCLRResponse();
        fiscEMVICReq = getFISCTxData().getTxObject().getEMVICRequest();
        fiscEMVICRes = getFISCTxData().getTxObject().getEMVICResponse();
        fiscEMVICCon = getFISCTxData().getTxObject().getEMVICConfirm();
        switch (getFISCTxData().getMessageID().substring(0, 1)) {
            case "0":
            case "3":
                if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                    fiscHeader = fiscOPCReq;
                } else {
                    fiscHeader = fiscOPCRes;
                }
                break;
            case "1":
                if (getFISCTxData().getMessageID().substring(0, 2).equals("16")) {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscFCRMReq;
                    } else {
                        fiscHeader = fiscFCRMRes;
                    }
                } else {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscRMReq;
                    } else {
                        fiscHeader = fiscRMRes;
                    }
                }
                break;
            case "2":
                if (getFISCTxData().getMessageID().substring(1, 2).equals("6")) {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscEMVICReq;
                    } else {
                        fiscHeader = fiscEMVICRes;
                    }
                } else {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscINBKReq;
                    } else {
                        fiscHeader = fiscINBKRes;
                    }
                }
                break;
            case "5":
                if (getFISCTxData().getMessageID().substring(0, 2).equals("58")) {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscFCCLRReq;
                    } else {
                        fiscHeader = fiscFCCLRRes;
                    }
                } else {
                    if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                        fiscHeader = fiscCLRReq;
                    } else {
                        fiscHeader = fiscCLRRes;
                    }
                }
                break;
        }

    }

    /**
     * 產生財金 REQUEST 電文並送出等待回應
     *
     * @param ATMReq ATM.Request電文
     *
     *
     *               <history>
     *               <modify>
     *               <modifier>Ruling</modifier>
     *               <reason>FISC REQUEST BODY 補交易金額、ATMNO、客戶亂碼基碼同步查核欄..等</reason>
     *               <date>2010/8/27</date>
     *               </modify>
     *               <modify>
     *               <modifier>Husan</modifier>
     *               <reason>FISC REQUEST BODY 指定提示日期 必須為中國年</reason>
     *               <date>2010/10/06</date>
     *               <modifier>Husan</modifier>
     *               <reason>MSGCTL Schema修改 MSGCTL_2WAY變MSGCTL_FISC_2WAY</reason>
     *               <date>2010/10/07</date>
     *               <modifier>Husan</modifier>
     *               <reason>connie spec change</reason>
     *               <date>2010/10/29</date>
     *               <modifier>Husan</modifier>
     *               <reason>connie spec change 刪除欄位 1. FEPTXN_REQ_TIME 2. FEPTXN_REP_TIME 3. FEPTXN_CON_TIME</reason>
     *               <date>2010/11/05</date>
     *               <modifier>Husan</modifier>
     *               <reason>修正 Feptxn_REQ_RC,Feptxn_REP_RC,Feptxn_CON_RC 改用列舉值</reason>
     *               <date>2010/11/25</date>
     *               <modifier>Husan</modifier>
     *               <reason>修正 產生INBKREQ的BODY 轉出行代號欄位</reason>
     *               <date>2010/11/29</date>
     *               <modifier>Husan</modifier>
     *               <reason>spec change 組回應電文的body-ATMNO右補0八位</reason>
     *               <date>2011/01/06</date>
     *               <modifier>Husan</modifier>
     *               <reason>客戶亂碼基碼同步查核欄修正</reason>
     *               <date>2011/01/21</date>
     *               </modify>
     *               <modify>
     *               <modifier>Ruling</modifier>
     *               <reason>JCB卡跨行預借現金(CAJ2480)是用1-DES PPK，故讀取 SYSCONF 判斷使用1-DES PPK或3-DES PPK</reason>
     *               <date>2011/3/14</date>
     *               </modify>
     *               <modify>
     *               <modifier>Ruling</modifier>
     *               <reason>檢核電文欄位內容有誤，應由CardReturnCode.CheckFieldError改用FISCReturnCode.MessageFormatError(0101:訊息格式或內容編輯錯誤)</reason>
     *               <date>2014/10/21</date>
     *               </modify>
     *               </history>
     * @return 送電文至FISC
     */

    public FEPReturnCode sendRequestToFISC(ATMGeneralRequest ATMReq) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        @SuppressWarnings("unused")
        String desReturnMac = "";
        String wk_BITMAP = null;
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        Bitmapdef oBitMap = null;
//        AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
//        SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
        try {
            if (StringUtils.isNotBlank(getFISCTxData().getMsgCtl().getMsgctlVirBkno())) {
                getFeptxn().setFeptxnDesBkno(getFISCTxData().getMsgCtl().getMsgctlVirBkno().substring(0, 3));
            } else if (StringUtils.isNotBlank(getFeptxn().getFeptxnDesBkno())) {
            	 //不異動值
            } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                getFeptxn().setFeptxnDesBkno(getFeptxn().getFeptxnTroutBkno());
            } else {
                getFeptxn().setFeptxnDesBkno(getFeptxn().getFeptxnTrinBkno());
            }

            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());

            //判斷是否檢核換日
            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlChkChgday())) {
                Zone twnZone = getZoneByZoneCode(ZoneCode.TWN);
                if (twnZone == null) {
                    return IOReturnCode.ZONENotFound;
                }
                if (DbHelper.toBoolean(twnZone.getZoneChgday())) {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC_ChangeDate);
                } else {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
                }
            } else {
                getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            }

            if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
                getFeptxn().setFeptxnStan(getStan());
                //避免之後Exception沒顯示stan，這邊先補
                if (StringUtils.isBlank(getLogContext().getStan())) {
                    getLogContext().setStan(getFeptxn().getFeptxnStan());
                }
            }

            //for 增加檢核組財金電文 HEADER，若HEADER搬入值為空值則 Return RC=MessageFormatError
            if (StringUtils.isBlank(getFeptxn().getFeptxnDesBkno())) {
                getLogContext().setRemark("組財金Req電文Header-TxnDestinationInstituteId無值");
                this.logMessage(getLogContext());
                rtnCode = FISCReturnCode.MessageFormatError;
                return rtnCode;
            }

            //產生財金電文Header(ReqHEAD)
            rtnCode = prepareHeader("0200");
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            //產生財金電文Body(ReqINBK)
            if (StringUtils.isBlank(getFeptxn().getFeptxnPaytype())) {
                //非跨行繳綜合所得稅結算申報稅款
                wk_BITMAP = getFISCTxData().getMsgCtl().getMsgctlBitmap1();
            } else {
                if (!getFeptxn().getFeptxnPaytype().substring(0, 2).equals("15")) {
                    wk_BITMAP = getFISCTxData().getMsgCtl().getMsgctlBitmap1();
                } else {
                    wk_BITMAP = getFISCTxData().getMsgCtl().getMsgctlBitmap2();
                }
            }

            //for跨行轉帳小額交易手續費調降
            if (FISCPCode.PCode2522.getValueStr().equals(getFeptxn().getFeptxnPcode()) && getFeptxn().getFeptxnTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                    && !StringUtils.isBlank(getFeptxn().getFeptxnAcctSup())) {
                wk_BITMAP = wk_BITMAP.substring(0, 56) + "1" + wk_BITMAP.substring(57);
            }

            //for中文附言欄
//            if ((FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2522.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2523.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2524.getValueStr().equals(getFeptxn().getFeptxnPcode()))
//                    && !StringUtils.isBlank(getFeptxn().getFeptxnChrem())) {
//                wk_BITMAP = wk_BITMAP.substring(0, 44) + "1" + wk_BITMAP.substring(45);
//            }
            if (StringUtils.equalsAny(getFeptxn().getFeptxnPcode(),
                    FISCPCode.PCode2521.getValueStr(),
                    FISCPCode.PCode2522.getValueStr(),
                    FISCPCode.PCode2523.getValueStr(),
                    FISCPCode.PCode2524.getValueStr())
            && StringUtils.isNotBlank(getFeptxn().getFeptxnChrem())){
                wk_BITMAP = wk_BITMAP.substring(0, 44) + "1" + wk_BITMAP.substring(45);
            }

            //for 2566代理行交易
            if (FISCPCode.PCode2566.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    && getFeptxn().getFeptxnTrk3().substring(88, 90).equals("01")) {
                wk_BITMAP = wk_BITMAP.substring(0, 8) + "1"
                        + wk_BITMAP.substring(9, 54) + "11"
                        + wk_BITMAP.substring(56);
                if( !StringUtils.equals(getFeptxn().getFeptxnChannel(), FEPChannel.FID.getNameS())
                        && !StringUtils.equals(getFeptxn().getFeptxnChannel(), FEPChannel.ATM.getNameS())) {
                    String trk3 = getFeptxn().getFeptxnTrk3();
                    StringBuffer str = new StringBuffer();
                    str.append(trk3);
                    str.setCharAt(88, ' ');
                    str.setCharAt(89, ' ');
                    getFeptxn().setFeptxnTrk3(str.toString());
                }
            }

            //2532 判斷 15類 非15類組不同BITMAP
            //TZ 15類
            StringBuffer bufBitmap = new StringBuffer(wk_BITMAP);
            if(StringUtils.startsWithIgnoreCase(getFeptxn().getFeptxnMsgid(), "TZ")
            && StringUtils.equals(getFeptxn().getFeptxnPcode(), FISCPCode.PCode2532.getValueStr()) ){
                //15類 開啟 41 42 關閉 30 54
                bufBitmap.setCharAt(40, '1');
                bufBitmap.setCharAt(41, '1');
                bufBitmap.setCharAt(29, '0');
                bufBitmap.setCharAt(53, '0');
                wk_BITMAP = bufBitmap.toString();
            } else if (StringUtils.equals(getFeptxn().getFeptxnPcode(), FISCPCode.PCode2532.getValueStr()) && StringUtils.startsWithIgnoreCase(getFeptxn().getFeptxnMsgid(), "TY")){
                //非15類 開 30 54 關 41 42
                bufBitmap.setCharAt(40, '0');
                bufBitmap.setCharAt(41, '0');
                bufBitmap.setCharAt(29, '1');
                bufBitmap.setCharAt(53, '1');
                wk_BITMAP = bufBitmap.toString();
            }

            /**
             * 2566-02(00)約定時
             * BITMPAP應該是04A0201000041301
             * 這個交易BIT #9, 30,31有ON
             *
             * 2566-02(01)/(03)   撤銷/撤銷通知
             * BITMPAP應該是0420201000041001
             * 這個交易BIT #9, 30,31沒有ON
             */
            if(StringUtils.startsWithIgnoreCase(getFeptxn().getFeptxnMsgid(), "LE")
                    && StringUtils.equals(getFeptxn().getFeptxnPcode(), FISCPCode.PCode2566.getValueStr())){
                Vatxn vatxn = vatxnMapper.selectByPrimaryKey(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnEjfno());
                if(vatxn != null){
                    if(StringUtils.equals(vatxn.getVatxnType(), "00")){
                        bufBitmap.setCharAt(8, '1');
                        bufBitmap.setCharAt(54, '1');
                        bufBitmap.setCharAt(55, '1');
                        wk_BITMAP = bufBitmap.toString();
                    }else if(StringUtils.equalsAny(vatxn.getVatxnType(), "01", "03")){
                        bufBitmap.setCharAt(8, '0');
                        bufBitmap.setCharAt(54, '0');
                        bufBitmap.setCharAt(55, '0');
                        wk_BITMAP = bufBitmap.toString();
                    }
                }
            }

            //BIP MAP位置
            for (int i = 2; i <= 63; i++) {
                if (wk_BITMAP.substring(i, i + 1).equals("1")) {
                    //依據wk_BITMAP 判斷是否搬值
                    switch (i) {
                        case 2: //交易金額
                            if (getFeptxn().getFeptxnTxAmt().intValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:getFeptxnTxAmt() = 0");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            //for跨行提領外幣
                            if (ATMTXCD.US.name().equals(getFeptxn().getFeptxnTxCode())
                                    || ATMTXCD.JP.name().equals(getFeptxn().getFeptxnTxCode())
                                    || ATMTXCD.DA.name().equals(getFeptxn().getFeptxnTxCode())
                                    || ATMTXCD.DC.name().equals(getFeptxn().getFeptxnTxCode())) {
                                fiscINBKReq.setTxAmt(getFeptxn().getFeptxnTxAmtAct().toString());
                            } else {
                                fiscINBKReq.setTxAmt(getFeptxn().getFeptxnTxAmt().toString());
                            }

                            //for EMV加收ACCESS FEEE，合庫無2420交易
                            if (ATMTXCD.PC.name().equals(getFeptxn().getFeptxnTxCode()) //金融卡( PLUS )跨國提款交易
                                    && FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode())
                                    && getFeptxn().getFeptxnRsCode().trim().equals("Y")) {
                                //交易金額=提領金額+ACCESS FEE
                                fiscINBKReq.setTxAmt(String.valueOf(getFeptxn().getFeptxnTxAmt().doubleValue() + getFeptxn().getFeptxnFeeCustpay().doubleValue()));
                            }
                            break;
                        case 3: //TRK2 磁軌資料內容
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTrk2())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TRK2 is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTRK2(getFeptxn().getFeptxnTrk2());
                            break;
                        case 4: // 客戶密碼
                            if (StringUtils.isBlank(getFeptxn().getFeptxnPinblock())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_PINBLOCK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setPINBLOCK(getFeptxn().getFeptxnPinblock());
                            break;
                        case 5: // 代付單位CD/ATM代號，因ATM壓TAC已右補0
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmno())) {
                                getLogContext().setRemark("MessageFormatError:setFeptxnAtmno() is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setATMNO(StringUtils.rightPad(getFeptxn().getFeptxnAtmno(), 8, '0'));
                            break;
                        case 7: // 預先授權交易金額
                            if (getFeptxn().getFeptxnTxAmtPreauth().doubleValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TX_AMT_PREAUTH = 0 ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTxAmtPreauth(getFeptxn().getFeptxnTxAmtPreauth().toString());
                            break;
                        case 8: // IC卡交易序號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnIcSeqno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_IC_SEQNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setIcSeqno(getFeptxn().getFeptxnIcSeqno());
                            break;
                        case 9: // 代收編號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnFiscRrn())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_FISC_RRN is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setFiscRrn(getFeptxn().getFeptxnFiscRrn());
                            break;
                        case 10: // 端末設備查核碼
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmChk())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ATM_CHK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setAtmChk(getFeptxn().getFeptxnAtmChk());
                            break;
                        case 11: // 預先授權IC卡交易序號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnIcSeqnoPreauth())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_IC_SEQNO_PREAUTH is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setIcSeqnoPreauth(getFeptxn().getFeptxnIcSeqnoPreauth());
                            break;
                        case 12: // 轉入銀行代號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTrinBkno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TRIN_BKNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            if(StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno7())){
                                fiscINBKReq.setTrinBkno(getFeptxn().getFeptxnTrinBkno7());
                            } else {
                                fiscINBKReq.setTrinBkno(getFeptxn().getFeptxnTrinBkno() + "0000");
                            }
                            break;
                        case 13: // 轉出銀行代號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTroutBkno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_Trout_BKNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno7())) {
                            	fiscINBKReq.setTroutBkno(getFeptxn().getFeptxnTroutBkno7());
                            } else {
                                fiscINBKReq.setTroutBkno(getFeptxn().getFeptxnTroutBkno() + "0000");
                            }
                            break;
                        case 16: // 狀況代號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnRsCode())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_RS_CODE is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setRsCode(getFeptxn().getFeptxnRsCode());
                            break;
                        case 18: // 交易日期時間
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTxDatetimeFisc())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TX_DATETIME_FISC is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTxDatetimeFisc(getFeptxn().getFeptxnTxDatetimeFisc());
                            break;
                        case 19: // 預先授權交易日期時間
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTxDatetimePreauth())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TX_DATETIME_PREAUTH is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTxDatetimePreauth(getFeptxn().getFeptxnTxDatetimePreauth());
                            break;
                        case 20: // 訂單號碼
                            if (StringUtils.isBlank(getFeptxn().getFeptxnOrderNo())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ORDER_NO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setOrderNo(getFeptxn().getFeptxnOrderNo());
                            break;
                        case 23: // 收款人姓名
                            if (StringUtils.isBlank(getFeptxn().getFeptxnToName())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TO_NAME is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            RefString to_name = new RefString("");
                            getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
                            getLogContext().setRemark("送財金收款人姓名初始 >" + getFeptxn().getFeptxnToName() + "<");
                            this.logMessage(getLogContext());
                            //中文部分 給財金的電文 合庫決議改用字霸UniCode轉CNS，長度超過160截斷，0E0F要成對，截斷後若有不成對則取代末2位成0F
                            convertFiscEncodebyAstar(getFeptxn().getFeptxnToName(), to_name, 160); //轉完電文長度160
                            fiscINBKReq.setTO_NAME(to_name.get());
                            getLogContext().setRemark("收款人姓名轉CNS >" + fiscINBKReq.getTO_NAME() + "<");
                            this.logMessage(getLogContext());
                            break;
                        case 26: // 營利事業統一編號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnBusinessUnit())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_BUSINESS_UNIT is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setBusinessUnit(getFeptxn().getFeptxnBusinessUnit());
                            break;
                        case 27: // 端末設備型態
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmType())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ATM_TYPE is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setAtmType(getFeptxn().getFeptxnAtmType());
                            break;
                        case 28: // 付款人姓名
                            if (StringUtils.isBlank(getFeptxn().getFeptxnFromName())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_FROM_NAME is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            RefString from_name = new RefString("");
                            getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
                            getLogContext().setRemark("送財金付款人姓名初始 >" + getFeptxn().getFeptxnFromName() + "<");
                            this.logMessage(getLogContext());
                            //中文部分 給財金的電文 合庫決議改用字霸UniCode轉CNS，長度超過160截斷，0E0F要成對，截斷後若有不成對則取代末2位成0F
                            convertFiscEncodebyAstar(getFeptxn().getFeptxnFromName(), from_name,160); //轉完電文長度160
                            fiscINBKReq.setFROM_NAME(from_name.get());
                            getLogContext().setRemark("付款人姓名轉CNS >" + fiscINBKReq.getFROM_NAME() + "<");
                            this.logMessage(getLogContext());
                            break;
                        case 29: // 指定提示日期
                            if (StringUtils.isBlank(getFeptxn().getFeptxnDueDate())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_DUE_DATE is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            String dueDate = StringUtils.leftPad(CalendarUtil.adStringToROCString(getFeptxn().getFeptxnDueDate()), 7) ;
                            fiscINBKReq.setDueDate(dueDate.substring(1, 7)); //轉民國年
                            break;
                        case 30: // 附言
                            if (StringUtils.isBlank(getFeptxn().getFeptxnFxmlMemo())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_FXML_MEMO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            RefString fxml_memo = new RefString("");
                            getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
                            getLogContext().setRemark("送財金FXML附言初始 >" + getFeptxn().getFeptxnFxmlMemo() + "<");
                            this.logMessage(getLogContext());
                            //中文部分 給財金的電文 合庫決議改用字霸UniCode轉CNS，長度超過160截斷，0E0F要成對，截斷後若有不成對則取代末2位成0F
                            convertFiscEncodebyAstar(getFeptxn().getFeptxnFxmlMemo(), fxml_memo,160); //轉完電文長度160
                            fiscINBKReq.setFXML_MEMO(fxml_memo.get());
                            getLogContext().setRemark("FXML附言轉CNS >" + fiscINBKReq.getFXML_MEMO() + "<");
                            this.logMessage(getLogContext());
                            break;
                        case 35: // Original Data
                            //for EMV拒絶磁條卡交易：加收ACCESS FEEE
                            if (((String.valueOf(ATMTXCD.PC).equals(getFeptxn().getFeptxnTxCode()) && FISCPCode.PCode2410.getValueStr().equals(getFeptxn().getFeptxnPcode()))
                                    && getFeptxn().getFeptxnRsCode().trim().equals("Y"))) {
                                //ORI_DATA 81~89 位
                                fiscINBKReq.setOriData(StringUtils.leftPad("0", 80, '0') + "D" + StringUtils.leftPad(String.valueOf(getFeptxn().getFeptxnFeeCustpay().doubleValue() * 100), 8, '0')
                                        + StringUtils.leftPad("0", 106, '0'));
                            } else {
                                fiscINBKReq.setOriData(StringUtils.leftPad("0", 195, '0'));
                            }
                            break;
                        case 38: /* 客戶亂碼基碼同步查核欄 */
                            /* JCB卡跨行預借現金 */
                            if (getFeptxn().getFeptxnPcode().equals("2480")) {
                                //JCB卡讀取 SYSCONF判斷使用1-DES PPK或3-DES PPK 以SYSCONF_SUBSYSNO ="1" 及,SYSCONF_NAME= "JCBPPKType" 為條件,讀取 SYSCONF, 取得變數值(SYSCONF_VALUE)
                                if (String.valueOf(ENCKeyType.S1.getValue()).equals(INBKConfig.getInstance().getJCBPPKType())) {
                                    //Single DES
                                    fiscINBKReq.setSyncPpkey(SysStatus.getPropertyValue().getSysstatTppsync());
                                } else {
                                    fiscINBKReq.setSyncPpkey(SysStatus.getPropertyValue().getSysstatT3dessync());
                                }
                            } else {
                                /*其他國際卡或信用卡使用3-DES PPK*/
                                fiscINBKReq.setSyncPpkey(SysStatus.getPropertyValue().getSysstatT3dessync());
                            }
                            if (StringUtils.isBlank(fiscINBKReq.getSyncPpkey())) {
                                getLogContext().setRemark("MessageFormatError:fiscINBKReq.SYNC_PPKEY is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            break;
                        case 39: // 繳款類別
                            if (StringUtils.isBlank(getFeptxn().getFeptxnPaytype())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_PAYTYPE is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setPAYTYPE(getFeptxn().getFeptxnPaytype());
                            break;
                        case 40: // 稽徵機關
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTaxUnit())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TAX_UNIT is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTaxUnit(getFeptxn().getFeptxnTaxUnit());
                            break;
                        case 41: // 身份証號／營利事業統一編號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnIdno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_IDNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            // for 全國繳費整批轉即時
                            if (getFeptxn().getFeptxnPcode().substring(0, 3).equals("226")) {
                                fiscINBKReq.setIDNO(mappingIDNO(getFeptxn().getFeptxnIdno()));
                            } else {
                                fiscINBKReq.setIDNO(getFeptxn().getFeptxnIdno());
                            }
                            break;
                        case 43: // 特店代碼Merchant ID
                            if (StringUtils.isBlank(getFeptxn().getFeptxnMerchantId())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_MERCHANT_ID is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setMerchantId(getFeptxn().getFeptxnMerchantId());
                            break;
                        case 44: // 中文附言欄
                            if (StringUtils.isBlank(getFeptxn().getFeptxnChrem())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_CHREM is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            RefString chrem = new RefString("");
//                            convertFiscEncode(getFeptxn().getFeptxnChrem(), chrem);
                            getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
                            getLogContext().setRemark("送財金中文附言欄初始 >" + getFeptxn().getFeptxnChrem() + "<");
                            this.logMessage(getLogContext());
                            //2026-04-10 中文部分 給財金的電文 合庫決議改用字霸UniCode轉CNS，長度超過80截斷，0E0F要成對，截斷後若有不成對則取代末2位成0F
                            convertFiscEncodebyAstar(getFeptxn().getFeptxnChrem(), chrem, 80); //轉完電文長度80
                            fiscINBKReq.setCHREM(chrem.get());
                            getLogContext().setRemark("附言欄轉CNS >" + fiscINBKReq.getCHREM() + "<");
                            this.logMessage(getLogContext());
                            break;
                        // 修改 for 跨行金融帳戶資訊核驗
                        case 45: // 附言欄
                            // 借用 FEPTXN_TRK3 存放附言欄
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTrk3())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TRK3 is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setMEMO(getFeptxn().getFeptxnTrk3());
                            break;
                        case 46: // 收款人統一編號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnToIdno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TO_IDNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTO_IDNO(getFeptxn().getFeptxnToIdno());
                            break;
                        case 47: // 附言欄
                            if (StringUtils.isBlank(getFeptxn().getFeptxnRemark())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_REMARK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setREMARK(getFeptxn().getFeptxnRemark());
                            break;
                        case 48: // 繳費作業手續費
                            if (getFeptxn().getFeptxnNpsFeeCustpay().doubleValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_NPS_FEE_CUSTPAY = 0 ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setNpsFee(getFeptxn().getFeptxnNpsFeeCustpay().toString());
                            break;
                        case 49: // 該單位應收手續費
                            if (getFeptxn().getFeptxnNpsFeeRcvr().doubleValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_NPS_FEE_RCVR = 0 ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setNpsFeeAll(String.valueOf(getFeptxn().getFeptxnNpsFeeRcvr()));
                            break;
                        case 50: // 轉入帳號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TRIN_ACTNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTrinActno(StringUtils.leftPad(StringUtils.trim(getFeptxn().getFeptxnTrinActno()), 16, '0'));
                            break;
                        case 51: // 轉出帳號
                            /* 全繳線上約定，搬入卡號 */
                            Vatxn vatxn = vatxnMapper.selectByPrimaryKey(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnEjfno());
                            if("2566".equals(getFeptxn().getFeptxnPcode())
                                    && vatxn != null
                                    && StringUtils.equals(vatxn.getVatxnCate(), "02")
                                    && StringUtils.equals(vatxn.getVatxnType(), "00")){
                                fiscINBKReq.setTroutActno(getFeptxn().getFeptxnMajorActno());
                            } else if (StringUtils.isBlank(getFeptxn().getFeptxnTroutActno())
                                    || getFeptxn().getFeptxnTroutActno().equals("0000000000000000")) { //增加轉出帳號防呆處理
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TROUT_ACTNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTroutActno(getFeptxn().getFeptxnTroutActno());
                            break;
                        case 52: // 原交易序號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnOriStan())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ORI_STAN is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setOriStan(getFeptxn().getFeptxnOriStan());
                            break;
                        case 53: // 銷帳編號
                            if (StringUtils.isBlank(getFeptxn().getFeptxnReconSeqno())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_RECON_SEQNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setReconSeqno(getFeptxn().getFeptxnReconSeqno());
                            break;
                        case 54: // IC卡備註欄
                            if (StringUtils.isBlank(getFeptxn().getFeptxnIcmark())) {
                                getLogContext().setRemark("MessageFormatError:FeptxnIcmark is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setICMARK(getFeptxn().getFeptxnIcmark());

                            /* 跨行存款交易 ICMARK不轉ASCII */
                            break;
                        case 55: // 交易驗證碼
                            if (StringUtils.isBlank(ATMReq.getPICCTAC())) {
                                getLogContext().setRemark("MessageFormatError:ATMReq.PICCTAC is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setTAC(ATMReq.getPICCTAC().substring(4)); //因財金電文解開時，沒有長度資料，固先不取長度
                            break;
                        case 56: // 帳戶補充資訊
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAcctSup())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ACCT_SUP is null or Empty ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscINBKReq.setAcctSup(getFeptxn().getFeptxnAcctSup());
                            break;
                        case 60: // ＩＣ卡卡號或卡片國際組織網路識別資料
                            fiscINBKReq.setNetwkData(StringUtils.leftPad("0", 22, '0'));
                            break;
                    }
                }
            }

            // MAC
            RefString refMac = new RefString(fiscINBKReq.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscINBKReq.getMessageType(), refMac);
            fiscINBKReq.setMAC(refMac.get());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            rtnCode = makeBitmap(fiscINBKReq.getMessageType(), fiscINBKReq.getProcessingCode(), MessageFlow.Request);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
            getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue()); // 先預設財金TIMEOUT之RC
            getFeptxn().setFeptxnPending((short) 1);
            getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal

            rtnCode = updateTxData(); //檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            fiscINBKReq.makeFISCMsg();

            // 準備送至財金的物件
            oBitMap = getBitmapData(fiscINBKReq.getMessageType() + fiscINBKReq.getProcessingCode());

            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
            fiscAdapter.setMessageToFISC(fiscINBKReq.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
                    rtnCode = FISCReturnCode.FISCTimeout;
                } else {
                    fiscINBKRes.parseFISCMsg();
                    getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
                    getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                }
            } else {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
            sendEMS(getLogContext());
            rtnCode = CommonReturnCode.ProgramException;
            return rtnCode;
        } finally {
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                getLogContext().setProgramName(ProgramName + ".sendRequestToFISC");
                getLogContext().setReturnCode(rtnCode);
                this.logMessage(getLogContext());
                getLogContext().setRemark(StringUtils.EMPTY);
            }
        }
    }

    /**
     * 負責檢核 FISC 跨行交易回應電文
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * <reason>connie spec change</reason>
     * <date>2010/10/20</date>
     * <reason>connie spec change add Feptxn.FEPTXN_PENDING = 2</reason>
     * <date>2010/11/24</date>
     * <modifier>Husan</modifier>
     * <reason>修正Const RC</reason>
     * <date>2010/11/25</date>
     * <reason>檢核MAPPING 欄位</reason>
     * <date>2010/12/13</date>
     * <reason>檢核MAPPING 欄位</reason>
     * <date>2011/01/07</date>
     * <reason>wk_TX_DATE移除民國百年判斷</reason>
     * <date>2011/01/12</date>
     * <reason>修正 SPEC for 256X 之他行卡片未帶回手續費問題</reason>
     * <date>2011/01/17</date>
     * <reason>把更新跨行代收付,移到AA裡面分別作</reason>
     * <date>2011/02/18</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkResponseMessage() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        String sFiscRc = null;
        @SuppressWarnings("unused")
        String wk_TX_DATE = null;
        @SuppressWarnings("unused")
        BigDecimal wk_TX_AMT = new BigDecimal(0);

        try {
            rtnCode = checkHeader(fiscINBKRes, true);
            getLogContext().setProgramName(ProgramName);
            sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
            if ("10".equals(sFiscRc.substring(0, 2))) {
                sendGarbledMessage(fiscINBKReq.getEj(), rtnCode, fiscINBKRes);
                getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                return rtnCode;
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); //FISC RESPONSE
            getFeptxn().setFeptxnRepRc(fiscINBKRes.getResponseCode());

            // 檢核財金電文 Header 正常
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                //失敗
                if (!getFeptxn().getFeptxnRepRc().equals(NormalRC.FISC_ATM_OK)) {
                    return rtnCode;
				} else {
					if (StringUtils.isNotBlank(fiscINBKRes.getTrinBkno())) {
						// 2532 轉入行代號
						getFeptxn().setFeptxnTrinBkno7(fiscINBKRes.getTrinBkno());
					}
					if (StringUtils.isNotBlank(fiscINBKRes.getTrinActno())) {
						// 2532 轉入帳號
						getFeptxn().setFeptxnTrinActno(fiscINBKRes.getTrinActno());
					}
					if (StringUtils.isNotBlank(fiscINBKRes.getTroutBkno())) {
						// 2531 轉出行代號
						getFeptxn().setFeptxnTroutBkno7(fiscINBKRes.getTroutBkno());
					}
                    if (!StringUtils.isBlank(fiscINBKRes.getBALA())) {
                        //getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA())); //可用餘額
                        getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA())); //可用餘額
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getBALB())) {
                        getFeptxn().setFeptxnBalb(new BigDecimal(fiscINBKRes.getBALB())); //帳戶餘額
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getFeeAmt())) {
                        //跨行手續費
                        getFeptxn().setFeptxnFeeCustpay(new BigDecimal(fiscINBKRes.getFeeAmt()));
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getNpsFeeAll())) {
                        // 繳費作業手續費
                        getFeptxn().setFeptxnNpsFeeCustpay(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFee()) / 10));

                        if (!getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {//他行卡
                            getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
                        }

                        getFeptxn().setFeptxnNpsFeeRcvr(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(0, 4)) / 10)); // 收信單位應收手續費
                        getFeptxn().setFeptxnNpsFeeAgent(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(4, 8)) / 10)); // 代理單位應收手續費
                        getFeptxn().setFeptxnNpsFeeTrout(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(8, 12)) / 10)); // 轉出單位應收手續費
                        getFeptxn().setFeptxnNpsFeeTrin(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(12, 16)) / 10)); // 轉入單位應收手續費
                        getFeptxn().setFeptxnNpsFeeFisc(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(16, 20)) / 10)); // 財金公司應收手續費
                    }

                    /* BIT22 促銷應用訊息 */
                    if (StringUtils.isNotBlank(fiscINBKRes.getPromMsg())) {
                        /* for 無卡提款 */
                        if (ATMTXCD.W2.name().equals(feptxn.getFeptxnTxCode())) {
                            /* 發行卡回傳實際扣款帳號 */
                            feptxn.setFeptxnTroutActno(fiscINBKRes.getPromMsg());
                        } else {
                            /* 由發卡行視需求，透過跨行交易回應訊息提供持卡人運用參考 */
                            feptxn.setFeptxnLuckyno(fiscINBKRes.getPromMsg());
                        }
                    }
                    // for 跨行轉帳小額交易手續費調降
                    if (!StringUtils.isBlank(fiscINBKRes.getAcctSup())) {
                        //帳戶補充資訊
                        getFeptxn().setFeptxnAcctSup(fiscINBKRes.getAcctSup());
                    }
                }
                // for 2566-02
                if(StringUtils.isNotBlank(fiscINBKRes.getMEMO())){
                    Vatxn vatxn = vatxnMapper.selectByPrimaryKey(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnEjfno());
                    if("2566".equals(feptxn.getFeptxnPcode()) && vatxn != null && StringUtils.equals(vatxn.getVatxnCate(), "02") && StringUtils.equals(vatxn.getVatxnType(), "00")){
                        feptxn.setFeptxnTroutActno(fiscINBKRes.getMEMO().substring(66,82));
                    }
                }
            } else {
                return rtnCode;
            }

            rtnCode = updateTxData();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            // 設初值避免在「檢核MAPPING(欄位)」的邏輯會出現EXCEPTION
            if (StringUtils.isBlank(fiscINBKRes.getTxAmt())) {
                fiscINBKRes.setTxAmt("0");
            }

            if (StringUtils.isBlank(fiscINBKRes.getTxAmtPreauth())) {
                fiscINBKRes.setTxAmtPreauth("0");
            }

            wk_TX_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')); //轉為西元年
            //因為送財金之 ATMNO 已右補0, 故不再判斷ATMNO 是否相同

            //for 跨行外幣提款(US/JP)
            if (String.valueOf(ATMTXCD.US).equals(getFeptxn().getFeptxnTxCode()) || String.valueOf(ATMTXCD.JP).equals(getFeptxn().getFeptxnTxCode())) {
                //跨行外幣提款(US/JP), 判斷折合台幣金額
                wk_TX_AMT = getFeptxn().getFeptxnTxAmtAct();
            } else {
                //非外幣提款交易, 判斷交易金額
                wk_TX_AMT = getFeptxn().getFeptxnTxAmt();
            }

            if (fiscINBKRes.getMEMO().length() == 100) {
                getFeptxn().setFeptxnRemark(fiscINBKRes.getMEMO().substring(90,96));
            }

            if (!getFeptxn().getFeptxnReqDatetime().substring(0, 8).equals(wk_TX_DATE)
                    || !fiscINBKRes.getTxnInitiateDateAndTime().substring(6, 12).equals(getFeptxn().getFeptxnReqDatetime().substring(8, 14))
                    || (Double.valueOf(fiscINBKRes.getTxAmt()) != 0 && Double.valueOf(fiscINBKRes.getTxAmt()) != wk_TX_AMT.doubleValue())) {
                getLogContext().setRemark("檢核MAPPING(欄位)不合：TX_DATE=" + getFeptxn().getFeptxnReqDatetime().substring(0, 8) + ", FiscRep.TxDate=" + wk_TX_DATE + ", TX_TIME=" + getFeptxn().getFeptxnReqDatetime().substring(8, 14)
                        + ", FiscRep.TxTime=" + fiscINBKRes.getTxnInitiateDateAndTime().substring(6, 12) + ", TX_AMT=" + wk_TX_AMT.toString() + ", FiscRep.TxAMT" + fiscINBKRes.getTxAmt());
                this.logMessage(getLogContext());
                rtnCode = FISCReturnCode.OriginalMessageDataError;
                //0401:訊息之 Mapping 欄位與與原交易不符
                return rtnCode;
            }

            // 已換日，更新財金營業日
            if (Double.parseDouble(getFeptxn().getFeptxnTbsdyFisc()) < Double.parseDouble(CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0')).isEmpty() ? "0"
                    : CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0')))) {
                // 更新財金營業日
                String wk_TBSDY_FISC = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0'));
                feptxn.setFeptxnTbsdyFisc(wk_TBSDY_FISC);
                FEPReturnCode rtnCode1 = changeDate(wk_TBSDY_FISC);
                if (rtnCode1 != FEPReturnCode.Normal){
                    getLogContext().setRemark("更新財金營業日失敗");
                    getLogContext().setReturnCode(rtnCode1);
                    getLogContext().setProgramName(ProgramName + ".changeDate");
                    sendEMS(getLogContext());
                }
            }

            // CALL DES 驗證 MAC
            return encHelper.checkFiscMac(fiscINBKRes.getMessageType(), fiscINBKRes.getMAC());
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkResponseMessage");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkResponseMessageForHce() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        String sFiscRc = null;
        @SuppressWarnings("unused")
        String wk_TX_DATE = null;
        @SuppressWarnings("unused")
        BigDecimal wk_TX_AMT = new BigDecimal(0);

        try {
            rtnCode = checkHeader(fiscINBKRes, true);
            getLogContext().setProgramName(ProgramName);
            sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
            if ("10".equals(sFiscRc.substring(0, 2))) {
                sendGarbledMessage(fiscINBKReq.getEj(), rtnCode, fiscINBKRes);
                getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                return rtnCode;
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); //FISC RESPONSE
            getFeptxn().setFeptxnRepRc(fiscINBKRes.getResponseCode());

            // 檢核財金電文 Header 正常
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                //失敗
                if (!getFeptxn().getFeptxnRepRc().equals(NormalRC.FISC_ATM_OK)) {
                    return rtnCode;
                } else {
                    if (!StringUtils.isBlank(fiscINBKRes.getBALA())) {
                        //getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA())); //可用餘額
                        getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA())); //可用餘額
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getBALB())) {
                        getFeptxn().setFeptxnBalb(new BigDecimal(fiscINBKRes.getBALB())); //帳戶餘額
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getFeeAmt())) {
                        //跨行手續費
                        getFeptxn().setFeptxnFeeCustpay(new BigDecimal(fiscINBKRes.getFeeAmt()));
                    }

                    if (!StringUtils.isBlank(fiscINBKRes.getNpsFeeAll())) {
                        // 繳費作業手續費
                        getFeptxn().setFeptxnNpsFeeCustpay(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFee()) / 10));

                        if (!getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {//他行卡
                            getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
                        }

                        getFeptxn().setFeptxnNpsFeeRcvr(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(0, 4)) / 10)); // 收信單位應收手續費
                        getFeptxn().setFeptxnNpsFeeAgent(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(4, 8)) / 10)); // 代理單位應收手續費
                        getFeptxn().setFeptxnNpsFeeTrout(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(8, 12)) / 10)); // 轉出單位應收手續費
                        getFeptxn().setFeptxnNpsFeeTrin(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(12, 16)) / 10)); // 轉入單位應收手續費
                        getFeptxn().setFeptxnNpsFeeFisc(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(16, 20)) / 10)); // 財金公司應收手續費
                    }

                    // for 無卡提款
                    if (!StringUtils.isBlank(fiscINBKRes.getPromMsg()) && String.valueOf(ATMTXCD.W2).equals(getFeptxn().getFeptxnTxCode())) {
                        // 發卡行回傳實際扣款帳號
                        getFeptxn().setFeptxnTroutActno(fiscINBKRes.getPromMsg().trim());
                    }
                    /* BIT22 促銷應用訊息 */
                    if (StringUtils.isNotBlank(fiscINBKRes.getPromMsg())) {
                        /* for 無卡提款 */
                        if (ATMTXCD.W2.name().equals(feptxn.getFeptxnTxCode())) {
                            /* 發行卡回傳實際扣款帳號 */
                            feptxn.setFeptxnTroutActno(fiscINBKRes.getPromMsg());
                        } else {
                            /* 由發卡行視需求，透過跨行交易回應訊息提供持卡人運用參考 */
                            feptxn.setFeptxnLuckyno(fiscINBKRes.getPromMsg());
                        }
                    }
                    // for 跨行轉帳小額交易手續費調降
                    if (!StringUtils.isBlank(fiscINBKRes.getAcctSup())) {
                        //帳戶補充資訊
                        getFeptxn().setFeptxnAcctSup(fiscINBKRes.getAcctSup());
                    }
                }
                // for 2566-02
                if(StringUtils.isNotBlank(fiscINBKRes.getMEMO())){
                    Vatxn vatxn = vatxnMapper.selectByPrimaryKey(getFeptxn().getFeptxnTxDate(), getFeptxn().getFeptxnEjfno());
                    if("2566".equals(feptxn.getFeptxnPcode()) && vatxn != null && StringUtils.equals(vatxn.getVatxnCate(), "02") && StringUtils.equals(vatxn.getVatxnType(), "00")){
                        feptxn.setFeptxnTroutActno(fiscINBKRes.getMEMO().substring(66,82));
                    }
                }
            } else {
                return rtnCode;
            }

            rtnCode = updateTxData();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            // 設初值避免在「檢核MAPPING(欄位)」的邏輯會出現EXCEPTION
            if (StringUtils.isBlank(fiscINBKRes.getTxAmt())) {
                fiscINBKRes.setTxAmt("0");
            }

            if (StringUtils.isBlank(fiscINBKRes.getTxAmtPreauth())) {
                fiscINBKRes.setTxAmtPreauth("0");
            }

            wk_TX_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')); //轉為西元年
            //因為送財金之 ATMNO 已右補0, 故不再判斷ATMNO 是否相同

            //for 跨行外幣提款(US/JP)
            if (String.valueOf(ATMTXCD.US).equals(getFeptxn().getFeptxnTxCode()) || String.valueOf(ATMTXCD.JP).equals(getFeptxn().getFeptxnTxCode())) {
                //跨行外幣提款(US/JP), 判斷折合台幣金額
                wk_TX_AMT = getFeptxn().getFeptxnTxAmt();
            } else {
                //非外幣提款交易, 判斷交易金額
                wk_TX_AMT = getFeptxn().getFeptxnTxAmt();
            }

            if (fiscINBKRes.getMEMO().length() == 100) {
                getFeptxn().setFeptxnRemark(fiscINBKRes.getMEMO().substring(90,96));
            }

            // 檢核財金是否換日
            if (Double.parseDouble(getFeptxn().getFeptxnTbsdyFisc()) < Double.parseDouble(CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0')).equals("") ? "0"
                    : CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0')))) {
                //財金營業日已更換(E961)
                rtnCode = CommonReturnCode.FISCBusinessDateChanged;
                return rtnCode;
            }

            // CALL DES 驗證 MAC
            return encHelper.checkFiscMac(fiscINBKRes.getMessageType(), fiscINBKRes.getMAC());
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkResponseMessage");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 準備國際卡的資料處理物件
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareIntltxn(RefBase<Intltxn> intlTxn, RefBase<Intltxn> oriINTLTXN, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Curcd curcd = new Curcd();
        // Tables.DBINTLTXN dbINTLTXN = new Tables.DBINTLTXN(FEPConfig.DBName);
        FISC_INBK fiscINBK = null;
        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscINBK = fiscINBKReq;
        } else {
            fiscINBK = fiscINBKRes;
        }
        try {
            intlTxn.get().setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
            intlTxn.get().setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
            intlTxn.get().setIntltxnBkno(getFeptxn().getFeptxnBkno());
            intlTxn.get().setIntltxnStan(getFeptxn().getFeptxnStan());
            intlTxn.get().setIntltxnPcode(getFeptxn().getFeptxnPcode());
            intlTxn.get().setIntltxnAtmno(getFeptxn().getFeptxnAtmno());
            intlTxn.get().setIntltxnTxTime(getFeptxn().getFeptxnTxTime());
            intlTxn.get().setIntltxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            intlTxn.get().setIntltxnReqRc(getFeptxn().getFeptxnReqRc());
            intlTxn.get().setIntltxnRepRc(getFeptxn().getFeptxnRepRc());
            intlTxn.get().setIntltxnConRc(getFeptxn().getFeptxnConRc());
            intlTxn.get().setIntltxnTxrust(getFeptxn().getFeptxnTxrust());
            intlTxn.get().setIntltxnTrk2(getFeptxn().getFeptxnTrk2());
            intlTxn.get().setIntltxnNetwkData(fiscINBK.getNetwkData());
            intlTxn.get().setIntltxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            intlTxn.get().setIntltxnTroutActno(getFeptxn().getFeptxnTroutActno());
            intlTxn.get().setIntltxnTroutKind(getFeptxn().getFeptxnTroutKind());
            intlTxn.get().setIntltxnZoneCode(getFeptxn().getFeptxnZoneCode());
            intlTxn.get().setIntltxnBrno(getFeptxn().getFeptxnBkno());
            intlTxn.get().setIntltxnZoneCode(getFeptxn().getFeptxnZoneCode());
            if (StringUtils.isBlank(fiscINBK.getOriData())) {
                return FISCReturnCode.OriginalMessageError;
            } else {
                rtnCode = checkORI_DATA(msgFlow);
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }
            }

            /* 拆解INBK.ORI_DATA 電文欄位 */
            intlTxn.get().setIntltxnOriMsgtype(fiscINBK.getORIDATA().getOriMsgtype());
            intlTxn.get().setIntltxnOriVisaStan(fiscINBK.getORIDATA().getOriVisaStan());
            intlTxn.get().setIntltxnOriTxMmddtime(fiscINBK.getORIDATA().getOriTxDatetime());
            intlTxn.get().setIntltxnOriAcq(fiscINBK.getORIDATA().getOriAcq());
            intlTxn.get().setIntltxnOriFwdInst(fiscINBK.getORIDATA().getOriFwdInst());
            intlTxn.get().setIntltxnAtmCur(fiscINBK.getORIDATA().getTxCur());
            intlTxn.get().setIntltxnAtmAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getORIDATA().getTxAmt()) / 100));
            intlTxn.get().setIntltxnSetCur(fiscINBK.getORIDATA().getSetCur());
            intlTxn.get().setIntltxnSetAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getORIDATA().getSetAmt()) / 100)); // (Decimal.Parse(fiscINBK.getORIDATA().SET_AMT) / 100)
            intlTxn.get().setIntltxnSetExrate(fiscINBK.getORIDATA().getSetExrate());
            intlTxn.get().setIntltxnSetFee(fiscINBK.getORIDATA().getSetFee());
            intlTxn.get().setIntltxnProcFee(fiscINBK.getORIDATA().getProcFee());
            intlTxn.get().setIntltxnLocDatetime(fiscINBK.getORIDATA().getLocDatetime());
            intlTxn.get().setIntltxnSetDateMmdd(fiscINBK.getORIDATA().getSetDateMmdd());
            intlTxn.get().setIntltxnCovDateMmdd(fiscINBK.getORIDATA().getCovDateMmdd());
            intlTxn.get().setIntltxnBilAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getORIDATA().getBilAmt()) / 100)); // (Decimal.Parse(fiscINBK.getORIDATA().BIL_AMT) / 100)
            intlTxn.get().setIntltxnBillRate(fiscINBK.getORIDATA().getBillRate());
            intlTxn.get().setIntltxnTxRpamt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getORIDATA().getTxRpamt()) / 100)); // (Decimal.Parse(fiscINBK.getORIDATA().TX_RPAMT) / 100)
            intlTxn.get().setIntltxnSetRpamt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getORIDATA().getSetRpamt()) / 100)); // (Decimal.Parse(fiscINBK.getORIDATA().SET_RPAMT) / 100)
            intlTxn.get().setIntltxnBilCur(fiscINBK.getORIDATA().getBilCur());
            intlTxn.get().setIntltxnIccr(fiscINBK.getORIDATA().getICCR());
            intlTxn.get().setIntltxnMccr(fiscINBK.getORIDATA().getMCCR());
            intlTxn.get().setIntltxnPosMode(fiscINBK.getORIDATA().getPosMode());
            intlTxn.get().setIntltxnAcqCntry(fiscINBK.getORIDATA().getAcqCntry());

            if (StringUtils.isBlank(intlTxn.get().getIntltxnTrk2())) {
                // for沖銷交易 2430, 2470
                /* 9/26 修改 for 國際卡提款(2470)部份沖銷 */
                /* 判斷實際完成之原始交易金額<>0 且 */
                /*     實際完成之交易清算金額<>0, 則不沖銷原交易 */
                /* 2024/12/31 點掉, 檢核國際卡提款(2470)部份沖銷 */
//                if (intlTxn.get().getIntltxnTxRpamt().doubleValue() != 0 && intlTxn.get().getIntltxnSetRpamt().doubleValue() != 0) {
//                    // 完成之原始交易金額/交易清算金額<>0為部份沖銷，則不沖銷原交易
//                    getLogContext().setRemark("完成之原始交易金額/交易清算金額<>0, TX_RPAMT = " + intlTxn.get().getIntltxnTxRpamt() + " SET_RPAMT = " + intlTxn.get().getIntltxnSetRpamt());
//                    this.logMessage(getLogContext());
//                    return FISCReturnCode.OriginalMessageDataError;
//                }

                if (FISCPCode.PCode2430.getValueStr().equals(getFeptxn().getFeptxnPcode())) { /* PLUS國際卡沖銷 */
                    oriINTLTXN.get().setIntltxnOriMsgtype(intlTxn.get().getIntltxnOriMsgtype());
                    oriINTLTXN.get().setIntltxnOriVisaStan(intlTxn.get().getIntltxnOriVisaStan());
                    oriINTLTXN.get().setIntltxnOriTxMmddtime(intlTxn.get().getIntltxnOriTxMmddtime());
                    oriINTLTXN.get().setIntltxnOriAcq(intlTxn.get().getIntltxnOriAcq());
                    oriINTLTXN.set(intltxnExtMapper.queryByOriData(oriINTLTXN.get()));
                } else { /* 2470 :  CIRRUS國際卡沖銷 */
                    oriINTLTXN.get().setIntltxnOriMsgtype(intlTxn.get().getIntltxnOriMsgtype());
                    oriINTLTXN.get().setIntltxnOriVisaStan(intlTxn.get().getIntltxnOriVisaStan());
                    oriINTLTXN.get().setIntltxnOriTxMmddtime(intlTxn.get().getIntltxnOriTxMmddtime());
                    oriINTLTXN.get().setIntltxnOriAcq(intlTxn.get().getIntltxnOriAcq());
                    oriINTLTXN.get().setIntltxnOriFwdInst(intlTxn.get().getIntltxnOriFwdInst());
                    oriINTLTXN.set(intltxnExtMapper.queryByOriData(oriINTLTXN.get()));
                }
                /* 將原交易資料搬入 INTLTXN */
                if (oriINTLTXN.get() != null) {
                    intlTxn.get().setIntltxnOriStan(oriINTLTXN.get().getIntltxnStan());
                    intlTxn.get().setIntltxnTrk2(oriINTLTXN.get().getIntltxnTrk2());
                    intlTxn.get().setIntltxnTroutActno(oriINTLTXN.get().getIntltxnTroutActno());
                    intlTxn.get().setIntltxnTroutKind(oriINTLTXN.get().getIntltxnTroutKind());
                    intlTxn.get().setIntltxnZoneCode(oriINTLTXN.get().getIntltxnZoneCode());
                    intlTxn.get().setIntltxnBrno(oriINTLTXN.get().getIntltxnBrno());
                    intlTxn.get().setIntltxnTxCurAct(oriINTLTXN.get().getIntltxnTxCurAct());
                    intlTxn.get().setIntltxnTxAmtAct(oriINTLTXN.get().getIntltxnTxAmtAct());
                    intlTxn.get().setIntltxnExrate(oriINTLTXN.get().getIntltxnExrate());

                    getFeptxn().setFeptxnOriStan(oriINTLTXN.get().getIntltxnStan());
                    getFeptxn().setFeptxnTrk2(oriINTLTXN.get().getIntltxnTrk2());
                    getFeptxn().setFeptxnTroutActno(oriINTLTXN.get().getIntltxnTroutActno());
                    getFeptxn().setFeptxnDueDate(oriINTLTXN.get().getIntltxnTxDate());
                    getFeptxn().setFeptxnTraceEjfno(oriINTLTXN.get().getIntltxnEjfno());
                } else {
                    getLogContext().setRemark("找不到原交易INTLTXN的資料");
                    this.logMessage(getLogContext());
                    /* 2025/2/19 修改 for 跨國提款沖正(2430/2470)查無原交易, 送IMS主機 */
                    getFeptxn().setFeptxnCbsProc("Y"); //主機交易  ->  Y:主機交易,N:FEP交易
                    return FISCReturnCode.OriginalMessageDataError; // MAPPING 欄位資料不符
                }
            }

            // 國際卡交易需回寫 FEPTXN 清算幣別及清算金額以便折算帳戶金額
            if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnBkno())) { /* 原存行交易 */
                /* 交易幣別 - 財金 ISO 幣別代號轉成文字幣別三碼 */
                curcd = getAlpha3ByIsono3(intlTxn.get().getIntltxnAtmCur());
                if (curcd != null) {
                    getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
                } else { /* 2025/8/25 修改 for 交易幣別不存在,改放交易幣別代碼  */
                    getFeptxn().setFeptxnTxCur(intlTxn.get().getIntltxnAtmCur());
                }
                /* 9/3 修改, 客戶提領幣別為日幣/韓元/越南盾時, 交易金額無小數點 */
                /* 5/5 修改 for 跨國提款沖銷(2430/2470) */
                if (CurrencyType.JPY.name().equals(getFeptxn().getFeptxnTxCur()) || getFeptxn().getFeptxnTxCur().equals("KRW") || getFeptxn().getFeptxnTxCur().equals("VND")) {
                    /* 7/30修改 for 跨國提款沖銷(2430/2470), 上面已*100 */
                    if (!FISCPCode.PCode2430.getValueStr().equals(getFeptxn().getFeptxnPcode()) && !FISCPCode.PCode2470.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                        intlTxn.get().setIntltxnAtmAmt(BigDecimal.valueOf(intlTxn.get().getIntltxnAtmAmt().doubleValue() * 100));
                    }
                }
                getFeptxn().setFeptxnTxAmt(intlTxn.get().getIntltxnAtmAmt()); /* 交易金額 */
            }
            /* 清算幣別 - 財金 ISO 幣別代號轉成文字幣別三碼 */
            curcd = getAlpha3ByIsono3(intlTxn.get().getIntltxnSetCur());
            if (curcd != null) {
                getFeptxn().setFeptxnTxCurSet(curcd.getCurcdAlpha3());
                getFeptxn().setFeptxnTxAmtSet(intlTxn.get().getIntltxnSetAmt()); /* 清算金額 */
            }

            /* 1/2 修改 for 國際卡交易, 判斷國際卡清算月份及財金營業日月份 */
            /* ORI_DATA交易清算日的月份為 12月且財金營業日的月份為1月 */
            String tbsdyFisc;
            tbsdyFisc = intlTxn.get().getIntltxnTbsdyFisc().substring(0, 4) + "/" + intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6) + "/" + intlTxn.get().getIntltxnTbsdyFisc().substring(6, 8);
            if ("12".equals(intlTxn.get().getIntltxnSetDateMmdd().substring(0, 2)) && "01".equals(intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6))) {
                /* 國際清算日為財金營業日的年度往前減一年 */
                getFeptxn().setFeptxnTbsdyIntl((Integer.parseInt(tbsdyFisc.substring(0, 4)) - 1) + "" + intlTxn.get().getIntltxnSetDateMmdd());
            } else if ("12".equals(intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6)) && "01".equals(intlTxn.get().getIntltxnSetDateMmdd().substring(0, 2))) {
                /* 國際清算日為財金營業日的年度往後加一年 */
                getFeptxn().setFeptxnTbsdyIntl((Integer.parseInt(tbsdyFisc.substring(0, 4)) + 1) + "" + intlTxn.get().getIntltxnSetDateMmdd());
            } else {
                /* 國際清算日為財金營業日的年度 */
                getFeptxn().setFeptxnTbsdyIntl(intlTxn.get().getIntltxnTbsdyFisc().substring(0, 4) + intlTxn.get().getIntltxnSetDateMmdd());
            }
            intlTxn.get().setIntltxnTbsdyIntl(getFeptxn().getFeptxnTbsdyIntl());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareIntltxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
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
    @Override
    public FEPReturnCode checkINBKStatus(String pcode, boolean isAcquirer) {
        FEPReturnCode rc_MBOCT = null;
        FEPReturnCode rc_MBACT = null;
        String wk_AOCT = null;
        String wk_MBACT = null;
        String wk_LOG = "";

        try {
            //2.依代理或是被代理之 flag 設定不同 RC 以回應財金
            if (isAcquirer) { // 代理行
                rc_MBOCT = FEPReturnCode.SenderBankOperationStop; /*發信單位主機未在跨行作業運作狀態*/
                rc_MBACT = FEPReturnCode.SenderBankServiceStop; /*發信單位該項跨行業務停止或暫停營業*/
            } else { //原存行
                rc_MBOCT = FEPReturnCode.ReceiverBankOperationStop; /*收信單位主機未在跨行作業運作狀態*/
                rc_MBACT = FEPReturnCode.ReceiverBankServiceStop; /*收信單位該項跨行業務停止或暫停營業*/
            }

            //3.檢核本行系統及財金系統連線狀態
            //(3-1)檢核本行系統連線狀態    /* 回應 0206 或 0203*/
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
                getLogContext().setRemark("CheckINBKStatus-本行系統主機未在跨行作業運作狀態");
                this.logMessage(getLogContext());
                return rc_MBOCT;
            }

            //(3-2)檢核財金系統連線狀態    /* 回應 0702 */
            if (!"1".equals(SysStatus.getPropertyValue().getSysstatSoct())) {
                getLogContext().setRemark("CheckINBKStatus-財金公司主機未在跨行作業運作狀態");
                this.logMessage(getLogContext());
                return FEPReturnCode.FISCOperationStop; /*財金公司主機未在跨行作業運作狀態*/
            }

            //(3-3)依交易類別(提款/轉帳/繳款..),檢核財金及本行各作業連線狀態
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
                case "256": // 晶片卡 全國性繳費
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
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2200();
                    wk_LOG = "CD/ATM 共用系統轉帳作業(2200)";
                    break;
                case "270": // 資金調撥交易
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct2700();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact2700();
                    wk_LOG = "資金調撥作業(2700)";
                    break;
                default:
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

            //(3-4)晶片卡需先 Check 25XX子系統 (M-BANK 及 FISC AP STATUS)
            if ("251".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2510())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統提款作業(2510) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2510())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統提款作業(2501) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務暫停或停止營業
                }
            }

            if ("252".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2520())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統轉帳作業(2520) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2520())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統轉帳作業(2520) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            if ("253".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2530())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統繳款作業(2530) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2530())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統繳款作業(2530) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            if ("254".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2540())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統消費扣款作業(2540) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2540())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統消費扣款作業(2540) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            if ("255".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2550())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統預先授權作業(2550) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2550())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統預先授權作業(2550) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            if ("256".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2560())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統全國繳費作業(2560) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2560())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統全國繳費作業(2560) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            if ("257".equals(pcode.substring(0, 3))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2570())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統跨國提款作業(2570) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2570())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統跨國提款作業(2570) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }

            //(3-5)
            if ("22".equals(pcode.substring(0, 2))) {
                if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2500())) {
                    getLogContext().setRemark("CheckINBKStatus-本行晶片卡共用系統(2500) AP EX-CHECK-OUT");
                    this.logMessage(getLogContext());
                    return rc_MBACT; //XXXX該項跨行業務停止或暫停營業
                }
                if ("2".equals(SysStatus.getPropertyValue().getSysstatAoct2500())) {
                    getLogContext().setRemark("CheckINBKStatus-財金晶片卡共用系統(2500) AP停止作業");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.FISCServiceStop; //財金公司該項跨行業務停止或暫停營業
                }
            }


            //(3-6)檢核本行AP連線狀態 /* 回應 0205 或 0202 */
            if ("2".equals(wk_MBACT)) {
                getLogContext().setRemark("本行" + wk_LOG + " AP EX-CHECK-OUT");
                this.logMessage(getLogContext());
                return rc_MBACT; //發信單位該項跨行業務停止或暫停營業
            }

            //(3-7)檢核財金AP連線狀態  /* 回應 0701 */
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
     * 拆解ORI_DATA
     *
     * @param msgFunction MessageFlow
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Henny</modifier>
     *                    <reason></reason>
     *                    <date>2010/6/28</date>
     *                    </modify>
     *                    </history>
     * @return
     */
    public FEPReturnCode checkORI_DATA(MessageFlow msgFunction) {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_INBK fiscINBK = null;
        try {
            switch (msgFunction) {
                case Request:
                    fiscINBK = fiscINBKReq;
                    break;
                case Response:
                    fiscINBK = fiscINBKRes;
                    break;
                default:
                    fiscINBK = fiscINBKCon;
                    break;
            }
            fiscINBK.setORIDATA(new FISC_INBK.DefORI_DATA());
            if (fiscINBK.getOriData().length() != 195) {
                return FISCReturnCode.LengthError;
            }
            fiscINBK.getORIDATA().setOriMsgtype(fiscINBK.getOriData().substring(0, 4));
            fiscINBK.getORIDATA().setOriVisaStan(fiscINBK.getOriData().substring(4, 10));
            fiscINBK.getORIDATA().setOriTxDatetime(fiscINBK.getOriData().substring(10, 20));
            fiscINBK.getORIDATA().setOriAcq(fiscINBK.getOriData().substring(20, 31));
            fiscINBK.getORIDATA().setOriFwdInst(fiscINBK.getOriData().substring(31, 42));
            fiscINBK.getORIDATA().setTxAmt(fiscINBK.getOriData().substring(42, 54));
            fiscINBK.getORIDATA().setSetAmt(fiscINBK.getOriData().substring(54, 66));
            fiscINBK.getORIDATA().setTxCur(fiscINBK.getOriData().substring(66, 69));
            fiscINBK.getORIDATA().setSetCur(fiscINBK.getOriData().substring(69, 72));
            fiscINBK.getORIDATA().setSetExrate(fiscINBK.getOriData().substring(72, 80));
            fiscINBK.getORIDATA().setSetFee(fiscINBK.getOriData().substring(80, 89));
            fiscINBK.getORIDATA().setProcFee(fiscINBK.getOriData().substring(89, 98));
            fiscINBK.getORIDATA().setLocDatetime(fiscINBK.getOriData().substring(98, 108));
            fiscINBK.getORIDATA().setSetDateMmdd(fiscINBK.getOriData().substring(108, 112));
            fiscINBK.getORIDATA().setCovDateMmdd(fiscINBK.getOriData().substring(112, 116));
            fiscINBK.getORIDATA().setBilAmt(fiscINBK.getOriData().substring(116, 128));
            fiscINBK.getORIDATA().setBillRate(fiscINBK.getOriData().substring(128, 136));
            fiscINBK.getORIDATA().setTxRpamt(fiscINBK.getOriData().substring(136, 148));
            fiscINBK.getORIDATA().setSetRpamt(fiscINBK.getOriData().substring(148, 160));
            fiscINBK.getORIDATA().setICCR(fiscINBK.getOriData().substring(160, 168));
            fiscINBK.getORIDATA().setMCCR(fiscINBK.getOriData().substring(168, 180));
            fiscINBK.getORIDATA().setBilCur(fiscINBK.getOriData().substring(180, 183));
            fiscINBK.getORIDATA().setPosMode(fiscINBK.getOriData().substring(183, 187));
            fiscINBK.getORIDATA().setAcqCntry(fiscINBK.getOriData().substring(187, 190));
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            return FISCReturnCode.CheckBitMapError;
        } finally {
            if (fiscINBK != null)
                LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkORI_DATA][", fiscINBK.getClass().getSimpleName(), "]ORIDATA Data : ", fiscINBK.getORIDATA().toJSON());
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
    @Override
    public FEPReturnCode checkNpsunit(Feptxn feptxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Npsunit npsunit = new Npsunit();
        try {
            if ("10000081".equals(feptxn.getFeptxnBusinessUnit()) && "00001".equals(feptxn.getFeptxnPaytype()) && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnBkno())) {
                //代理交易的這個8+5不在的委託單位檔內，不須檢核
                
            } else if (!RRN30000Trans.equals(feptxn.getFeptxnFiscRrn()) || !PAYTYPE30000Trans.equals(feptxn.getFeptxnPaytype())) {// 非ATM移轉
                /* 3/3 修改, 移至前面檢核 */
                /* 以檢核委託單位代號、繳款類別、費用代號, 讀取 NPSUNIT */
                npsunit.setNpsunitNo(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
                npsunit.setNpsunitPaytype(feptxn.getFeptxnPaytype()); // 繳款類別
                npsunit.setNpsunitFeeno(feptxn.getFeptxnPayno()); // 費用代號
                npsunit = npsunitMapper.selectByPrimaryKey(npsunit.getNpsunitNo(), npsunit.getNpsunitPaytype(), npsunit.getNpsunitFeeno());
                if (npsunit == null) {
                    if ( !SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnBkno()) ) {
                        /* 2025/5/19 應合庫修改,原存交易找不到全繳委託單位檔回RC:4508 */
                        return FISCReturnCode.NotApplyTransferActno; //非約定之轉帳帳戶
                    }else{
                        return FISCReturnCode.NPSNotFound; // 委託單位代號錯誤(NPSNotFound)
                    }
                }

                /* 2020/4/7 修改, 將帳務代理行手續費寫入 FEPTXN */
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(StringUtils.leftPad(npsunit.getNpsunitBkno(), 7, '0').substring(0, 3))) {
                    feptxn.setFeptxnNpsAgbFee(npsunit.getNpsunitOtherFee1());
                }

                /* 2023/11/24 ATM 繳汽燃費處理規則已由前端完成，FEP不需檢核 */
                
                /* 10/11 修改, for 全國性繳費-帳務代理交易 */
                /* 2/9 修改 */
                /* 2025/09/16 點掉, FEP不需檢核 */
//                if ((FISCPCode.PCode2263.getValueStr().equals(feptxn.getFeptxnPcode())
//                        || FISCPCode.PCode2264.getValueStr().equals(feptxn.getFeptxnPcode())
//                        || FISCPCode.PCode2563.getValueStr().equals(feptxn.getFeptxnPcode())
//                        || FISCPCode.PCode2564.getValueStr().equals(feptxn.getFeptxnPcode()))
//                        && FEPChannel.FISC.name().equals(feptxn.getFeptxnChannel())) {
//                    /* 全國性繳費-帳務代理交易 */
//                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(npsunit.getNpsunitBkno().substring(0, 3))) {
////                        feptxn.setFeptxnChannel(FEPChannel.EBILL.name());
//                        /* 2/10 修改 for 全國性繳費帳務代理(2263/2563/2264/2564) */
//                        feptxn.setFeptxnReconSeqno(StringUtils.rightPad(feptxn.getFeptxnRemark(), 20, ' ').substring(4, 20)); /* 銷帳編號 */
//                        /* 2020/6/11 修改 for 全國性繳費帳務代理(2263/2264) */
//                        if (FISCPCode.PCode2264.getValueStr().equals(feptxn.getFeptxnPcode())
//                                && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) {
//                            /* 2264 交易轉入行為本行, 身份證號由附言欄拆出 */
//                            feptxn.setFeptxnIdno(feptxn.getFeptxnRemark().substring(28, 39));
//                            if (StringUtils.isBlank(feptxn.getFeptxnIdno())) {
//                                getLogContext().setRemark("身份證號及統編之值為空或NULL");
//                                this.logMessage(getLogContext());
//                                return FISCReturnCode.CheckIDNOError; //身分證號有誤(4806)
//                            }
//                        }
//                    }
//                }

                /* 2020/10/28 修改for 全國性繳費業務之保險費扣款強化 */
                /* 2025/09/16 點掉, FEP不需檢核 */
//                if ("226".equals(feptxn.getFeptxnPcode().substring(0, 3))
//                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
//                    String insPayType = INBKConfig.getInstance().getINSPayType();
//                    if (StringUtils.isNotBlank(insPayType)) {
//                        String[] insPayTypes = insPayType.split(";");
//                        if (ArrayUtils.isNotEmpty(insPayTypes)) {
//                            for (int i = 0; i < insPayTypes.length; i++) {
//                                if (insPayTypes[i] != null && insPayTypes[i].equals(feptxn.getFeptxnPaytype())) {
//                                    /* 檢核保險識別編號(Bitmap 48)是否存在 */
//                                    if (feptxn.getFeptxnRemark().length() < 38 || StringUtils.isBlank(feptxn.getFeptxnRemark().substring(28, 38))) {
//                                        getLogContext().setRemark("保險識別編號(Bitmap48)欄位長度小於38, 或值為空白");
//                                        this.logMessage(getLogContext());
//                                        return FISCReturnCode.MessageFormatError; //訊息格式或內容編輯錯誤(0101)
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        LogHelperFactory.getTraceLogger().info("SYSCONF無此資料, SubSystemNo = 1 and SysconfName = \"INSPayType\"");
//                    }
//                }
                getFISCTxData().setNpsunit(npsunit);
            }

            /* 3/3 修改, 移至前面 */
            if ("Y".equals(npsunit.getNpsunitMonthlyFg())) { /*月結*/
                feptxn.setFeptxnNpsMonthlyFg(DbHelper.toShort(true));
            }

            //繳費類別(00000~49999)為委託單位付費
            if (Double.valueOf(feptxn.getFeptxnPaytype()) >= 0 && Double.valueOf(feptxn.getFeptxnPaytype()) <= 49999) { // 委託單位付費
                if ("1".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //繳費作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) {
                    /* 轉入單位代理清算應付手續費 */
                    /* 3/3 修改 for 月結處理 */
                    feptxn.setFeptxnNpsClr((short) 2); /*手續費清算單位為轉入行*/
                } else if ("2".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) /* 發放作業 */
                        && feptxn.getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    /* 轉出單位代理清算應付手續費 */
                    /* 3/3 修改 for 月結處理 */
                    feptxn.setFeptxnNpsClr((short) 1); /*手續費清算單位為轉出行*/
                }
                /* 繳費類別(50000~99999)為使用者付費*/
            } else if (Double.valueOf(feptxn.getFeptxnPaytype()) >= 50000 && Double.valueOf(feptxn.getFeptxnPaytype()) <= 99999) { // 使用者付費
                if ("1".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //繳費作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())) {
                    /*轉出單位代理清算應付手續費*/
                    feptxn.setFeptxnNpsClr((short) 1); //手續費清算單位為轉出行
                } else if ("2".equals(feptxn.getFeptxnBusinessUnit().substring(0, 1)) //發放作業
                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTrinBkno())) {
                    /* 轉入單位代理清算應付手續費 */
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
     * 檢核身份證號及統一編號
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>NEW Function for 整批轉即時(2262)</reason>
     * <date>2014/04/21</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkIDNO(String idno) {
        try {
            if (StringUtils.isBlank(idno)) {
                getLogContext().setRemark("CheckIDNO-身份證號及統編之值為空或NULL");
                this.logMessage(getLogContext());
                return FISCReturnCode.CheckIDNOError;
            }
            idno = idno.trim();
            if (Pattern.compile("^[A-Z]\\d{9}$").matcher(idno).find()) { // 本國人身分證號格式
                return CommonReturnCode.Normal;
            } else {
                if (Pattern.compile("^\\d{8}$").matcher(idno).find()) { // 本國人統一編號格式

                    return CommonReturnCode.Normal;
                } else {
                    if (Pattern.compile("^[A-Z][A-Z]\\d{8}$").matcher(idno).find() || Pattern.compile("^\\d{8}[A-Z][A-Z]$").matcher(idno).find()) { // 外僑及大陸人士

                        return CommonReturnCode.Normal;
                    } else {
                        getLogContext().setRemark("CheckIDNO-身份證號及統編之格式錯誤");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.CheckIDNOError;
                    }
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkIDNO");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FISC_INBK getFiscINBKRes(){
        return fiscINBKRes;
    }

    public FEPReturnCode sendInbk2160RequestToFISC(RefBase<Inbk2160> DefINBK2160) {
//        fiscINBKReq = fisc;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        @SuppressWarnings("unused")
        String result = "";
        Bitmapdef oBitMap = getBitmapData(fiscINBKReq.getMessageType() + fiscINBKReq.getProcessingCode());
        try {

            fiscINBKReq.makeFISCMsg();
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(DefINBK2160.get().getInbk2160Stan()); // getFISCTxData().Stan
            fiscAdapter.setMessageToFISC(fiscINBKReq.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            // 2021-09-18 Richard modified start
            if (rtnCode == null) {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            // 2021-09-18 Richard modified end
            else if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                fiscINBKRes =new FISC_INBK();
                fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
                    rtnCode = FISCReturnCode.FISCTimeout;
                } else {
                    fiscINBKRes.parseFISCMsg();
                    DefINBK2160.get().setInbk2160Msgflow(FEPTxnMessageFlow.FISC_Response); // F2
                    DefINBK2160.get().setInbk2160Pending((short) 2); // 解除 PENDING
                    DefINBK2160.get().setInbk2160FiscTimeout(DbHelper.toShort(false));
                    DefINBK2160.get().setInbk2160AaRc(String.valueOf(CommonReturnCode.Normal.getValue()));
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendPendingRequestToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核EMV卡跨國交易月限額
     *
     * @return FEPReturnCode
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>New Function for EMV拒絶磁條卡交易</reason>
     * <date>2015/08/24</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkEMVMLimit() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        Emvc defEMVC = new Emvc();
        String pan = "";

        try {
            // 讀取系統參數檔
            if (StringUtils.isBlank(String.valueOf(INBKConfig.getInstance().getEMVMLimit())) || INBKConfig.getInstance().getEMVMLimit() == 0) {
                rtnCode = FEPReturnCode.QueryNoData;
                getLogContext().setRemark("CheckEMVMLimit-SYSCONF_VALUE='EMVMLimit'的值為NULL或0或空白，其值為=" + INBKConfig.getInstance().getEMVMLimit());
                this.logMessage(getLogContext());
                return rtnCode;
            }

            // 累計EMV卡限額
            pan = StringUtils.leftPad(getFeptxn().getFeptxnTrk2(), 40, ' ').substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")).trim();
            // 2015/12/27 Modify by Ruling for 改為相對交易日前30天(不含)的累計金額，不得超過5萬元
            Calendar txDate = Calendar.getInstance();
            txDate.add(Calendar.DAY_OF_MONTH, -30);
            BigDecimal emvcTxAmt = emvcExtMapper.getEmvcByMonth(
                    FormatUtil.dateTimeFormat(txDate, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                    pan);
            if (emvcTxAmt != null && emvcTxAmt.intValue() > 0) {
                if (getFeptxn().getFeptxnTxAmt().add(emvcTxAmt).intValue() > INBKConfig.getInstance().getEMVMLimit()) {
                    // 2015/12/27 Modify by Fly for 超過EMV限額時改回給ATM E229
                    rtnCode = CommonReturnCode.OverEMVLimit;
                    // rtnCode = CommonReturnCode.OverLimit
                    getLogContext().setRemark(StringUtils.join(
                            "CheckEMVMLimit-超過", INBKConfig.getInstance().getEMVMLimit(),
                            "限額，本次交易金額=", getFeptxn().getFeptxnTxAmt().toString(),
                            "，本月累計金額=", emvcTxAmt.toString()));
                    this.logMessage(getLogContext());
                    return rtnCode;
                }
            } else {
                rtnCode = FEPReturnCode.QueryNoData;
                getLogContext().setRemark("CheckEMVMLimit-查不到累計資料");
                this.logMessage(getLogContext());
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkEMVMLimit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 新增國際卡檔
     *
     * @param intlTxn
     * @return <history>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>新增</reason>
     * <date>2010/10/04</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode insertINTLTxn(Intltxn intlTxn) {
        try {
            if (intltxnExtMapper.insertSelective(intlTxn) <= 0) {
                return IOReturnCode.FEPTXNInsertError;
            } else {
                return CommonReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".insertINTLTxn"));
            sendEMS(getLogContext());
            return IOReturnCode.FEPTXNInsertError;
        }
    }

    /**
     * 產生財金 REQUEST 電文並送出等待回應
     *
     * @param ATMReq ATM.Request電文
     *
     *
     *               <history>
     *               <modify>
     *               <modifier>Ruling</modifier>
     *               <reason>跨行無卡提款</reason>
     *               <date>2018/02/12</date>
     *               </modify>
     *               </history>
     * @return 送電文至FISC
     */
    public FEPReturnCode sendNCRequestToFISC(ATMGeneralRequest ATMReq) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        String wk_BITMAP = null;
        Bitmapdef oBitMap = null;

        try {
            //判斷交易接收銀行
            if (StringUtils.isNotBlank(getFISCTxData().getMsgCtl().getMsgctlVirBkno())) {
                //虛擬財金代號
                getFeptxn().setFeptxnDesBkno(getFISCTxData().getMsgCtl().getMsgctlVirBkno().substring(0, 3));
            } else if (!getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                getFeptxn().setFeptxnDesBkno(getFeptxn().getFeptxnTroutBkno());
            } else {
                getFeptxn().setFeptxnDesBkno(getFeptxn().getFeptxnTrinBkno());
            }

            // 為了發生Timeout時Email沒顯示DesBkno，在取得正確的FEPTXN_DES_BKNO後須塞回給LogContext
            getLogContext().setDesBkno(getFeptxn().getFeptxnReqRc());

            //判斷是否檢核換日
            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlChkChgday())) {
                Zone twnZone = getZoneByZoneCode(ZoneCode.TWN);
                if (twnZone == null) {
                    return IOReturnCode.ZONENotFound;
                }
                if (DbHelper.toBoolean(twnZone.getZoneChgday())) {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC_ChangeDate);
                } else {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
                }
            } else {
                getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            }

            if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
                getFeptxn().setFeptxnStan(getStan());
            }

            rtnCode = prepareHeader("0200");
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            wk_BITMAP = getFISCTxData().getMsgCtl().getMsgctlBitmap1();

            // BIT MAP位置
            for (int i = 2; i <= 63; i++) {
                if (wk_BITMAP.substring(i, i + 1).equals("1")) {
                    //依據wk_BITMAP 判斷是否搬值
                    //依照 ‘財金電文整理_Request_201.xls’ 搬值 (參考FEPTXN MAPPING 欄位)
                    //若搬入值為空白(文字欄位) 或是 0(數字欄位) 則 Return RC=CheckFieldError
                    //欄位特別說明如下:
                    switch (i) {
                        case 2: // 交易金額
                            if (getFeptxn().getFeptxnTxAmt().doubleValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:getFeptxnTxAmt() = 0");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            fiscINBKReq.setTxAmt(getFeptxn().getFeptxnTxAmt().toString());

                            break;
                        case 4: // 無卡提款密碼
                            if (StringUtils.isBlank(getFeptxn().getFeptxnPinblock())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_PINBLOCK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }

                            fiscINBKReq.setPINBLOCK(getFeptxn().getFeptxnPinblock()); 

                            break;
                        case 5:
                            //代付單位 CD/ATM 代號,因 ATM 壓 TAC已右補0
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmno())) {
                                getLogContext().setRemark("MessageFormatError:setFeptxnAtmno() is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            fiscINBKReq.setATMNO(StringUtils.rightPad(getFeptxn().getFeptxnAtmno(), 8, '0'));

                            break;
                        case 8:
                            // IC卡交易序號
                            fiscINBKReq.setIcSeqno("00000000");
                            break;
                        case 10:
                            // 端末設備查核碼
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmChk())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_ATM_CHK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            fiscINBKReq.setAtmChk(getFeptxn().getFeptxnAtmChk());

                            break;
                        case 18:
                            //交易日期時間
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTxDatetimeFisc())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TX_DATETIME_FISC is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            fiscINBKReq.setTxDatetimeFisc(getFeptxn().getFeptxnTxDatetimeFisc());
                            break;
                        case 27:
                            //端末設備型態
                            getFeptxn().setFeptxnAtmType("6071"); // 無實體卡片
                            fiscINBKReq.setAtmType(getFeptxn().getFeptxnAtmType());
                            break;
                        case 38:
                            //客戶亂碼基碼同步查核欄
                            fiscINBKReq.setSyncPpkey(SysStatus.getPropertyValue().getSysstatT3dessync());
                            if (StringUtils.isBlank(fiscINBKReq.getSyncPpkey())) {
                                getLogContext().setRemark("MessageFormatError:fiscINBKReq.SYNC_PPKEY is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            break;
                        case 51:
                            //提款序號
                            if (StringUtils.isBlank(ATMReq.getFADATA().substring(2, 18))) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TROUT_ACTNO is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
                            }
                            fiscINBKReq.setTroutActno(ATMReq.getFADATA().substring(2, 18));
                            break;
                        case 54:
                            //IC卡備註欄
							if (StringUtils.isBlank(getFeptxn().getFeptxnIcmark())) {
								getLogContext().setRemark("MessageFormatError:FeptxnIcmark is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
							}
                            fiscINBKReq.setICMARK(getFeptxn().getFeptxnIcmark());
                            break;
                        case 55:
                            //交易驗證碼
                        	if (StringUtils.isBlank(ATMReq.getPICCTAC())) {
								getLogContext().setRemark("MessageFormatError:PICCTAC is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return rtnCode;
							}
                            fiscINBKReq.setTAC(ATMReq.getPICCTAC().substring(4));//因財金電文解開時，沒有長度資料，固先不取長度
                            break;
                    }
                }
            }

            // MAC
            RefString refMac = new RefString(fiscINBKReq.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscINBKReq.getMessageType(), refMac);
            fiscINBKReq.setMAC(refMac.get());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            rtnCode = makeBitmap(fiscINBKReq.getMessageType(), fiscINBKReq.getProcessingCode(), MessageFlow.Request);
            //注意為第七組Bitmap
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);// FISC REQUEST
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
            getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue()); //預設為財金TIMEOUT之RC
            getFeptxn().setFeptxnPending((short) 1);
            getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal
            rtnCode = updateTxData(); //檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            fiscINBKReq.makeFISCMsg();

            // 準備送至財金的物件
            oBitMap = getBitmapData(fiscINBKReq.getMessageType() + fiscINBKReq.getProcessingCode());

            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
            fiscAdapter.setMessageToFISC(fiscINBKReq.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
                    rtnCode = FISCReturnCode.FISCTimeout; //TIMEOUT
                } else {
                    fiscINBKRes.parseFISCMsg();
                    getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
                    getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                }
            } else {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendNCRequestToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                getLogContext().setProgramName(ProgramName + ".sendNCRequestToFISC");
                getLogContext().setReturnCode(rtnCode);
                logMessage(Level.DEBUG, getLogContext());
                getLogContext().setRemark("");
            }
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
//    	        SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
//    	        Stan stan = spCaller.getStan();
//    	        return StringUtils.leftPad(String.valueOf(stan.getStan()), 7, '0');
        StanGenerator stanGenerator = SpringBeanFactoryUtil.getBean(StanGenerator.class);
        return stanGenerator.generate();
    }

    /**
     * 準備財金資料HEADER
     *
     * @param msgType
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareHeader(String msgType) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_OPC fiscOPC = null;
        FISC_INBK fiscINBK = null;
        FISC_CLR fiscCLR = null;
        FISC_USDCLR fiscFCCLR = null;
        FISC_EMVIC fiscEMVIC = null;
        try {
            switch (msgType.substring(2, 4)) {
                case "00":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKReq;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCReq;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRReq;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRReq;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICReq;
                            break;
                        default:
                            break;
                    }
                    break;
                case "02":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKCon;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCCon;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICCon;
                            break;
                        default:
                            break;
                    }
                    break;
                case "10":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKRes;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCRes;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRRes;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRRes;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICRes;
                            break;
                        default:
                            break;
                    }
                    break;
            }

            // Header部分
            switch (getFISCTxData().getFiscTeleType()) {
                case INBK: { // CD/ATM
                    if (fiscINBK != null) {
                        fiscINBK.setSystemSupervisoryControlHeader("00");
                        fiscINBK.setSystemNetworkIdentifier("00");
                        fiscINBK.setAdderssControlField("00");
                        fiscINBK.setMessageType(msgType);
                        fiscINBK.setProcessingCode(getFeptxn().getFeptxnPcode());
                        fiscINBK.setSystemTraceAuditNo(getFeptxn().getFeptxnStan());
                        if ("10".equals(msgType.substring(2, 4))) {
                            //Response 電文抓取 Request 電文 DES/SRC BKNO
                            fiscINBK.setTxnDestinationInstituteId(fiscINBKReq.getTxnDestinationInstituteId());
                            fiscINBK.setTxnSourceInstituteId(fiscINBKReq.getTxnSourceInstituteId());
                        } else {
                            fiscINBK.setTxnDestinationInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnDesBkno(), 7, '0'));
                            fiscINBK.setTxnSourceInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnBkno(), 7, '0'));
                        }
                        // 統一抓取財金電文 Request 日期/時間
                        if (getFeptxn().getFeptxnReqDatetime().length() == 14) {
                            fiscINBK.setTxnInitiateDateAndTime(
                                    CalendarUtil.adStringToROCString(getFeptxn().getFeptxnReqDatetime().substring(0, 8)).substring(1, 7) + getFeptxn().getFeptxnReqDatetime().substring(8, 14)); // (轉成民國年)
                        } else {
                            fiscINBK.setTxnInitiateDateAndTime(getFeptxn().getFeptxnReqDatetime());
                        }
                        //填入回應代碼(RC)
                        switch (msgType.substring(2, 4)) {
                            case "00": // Request
                                fiscINBK.setResponseCode(getFeptxn().getFeptxnReqRc());
                                break;
                            case "10": // Response
                                fiscINBK.setResponseCode(getFeptxn().getFeptxnRepRc());
                                break;
                            case "02": // Confirm
                                fiscINBK.setResponseCode(getFeptxn().getFeptxnConRc());
                                break;
                        }

                        // 填入 KEY SYNC
                        Sysstat defSYSSTAT = new Sysstat();
                        defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                        defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
                        try {
                            if (defSYSSTAT != null) {
                                // 有找到讀DB
                                fiscINBK.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
                            } else {
                                // 沒找到Return ERROR
                                getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第一次讀SYSSTAT Not Found");
                                this.logMessage(getLogContext());
                                return FEPReturnCode.SYSSTATNotFound;
                            }
                        } catch (Exception ex) {
                            getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第一次讀SYSSTAT失敗:" + ex.toString());
                            this.logMessage(getLogContext());
                            // 發生Exception再讀一次
                            try {
                                defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                                defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
                                if (defSYSSTAT != null) {
                                    // 有找到讀DB，rtnCode=Normal
                                    fiscINBK.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
                                    rtnCode = CommonReturnCode.Normal;
                                } else {
                                    // 沒找到，rtnCode=FEPReturnCode.SYSSTATNotFound
                                    getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第二次讀SYSSTAT Not Found");
                                    this.logMessage(getLogContext());
                                    rtnCode = FEPReturnCode.SYSSTATNotFound;
                                }
                            } catch (Exception ex2) {
                                getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第二次讀SYSSTAT發生例外錯誤:" + ex2.toString());
                                this.logMessage(getLogContext());
                                rtnCode = FEPReturnCode.SYSSTATNotFound;
                            }
                            // 第二次失敗時Return ERROR
                            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                                return FEPReturnCode.SYSSTATNotFound;
                            }
                        }
                    } else {
                        getLogContext().setRemark("組財金物件(fiscINBK)為Nothing");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.MessageFormatError;
                    }
                    break;
                }
                case CLR: {// CLR
                    if (fiscCLR != null) {
                        fiscCLR.setSystemSupervisoryControlHeader("00");
                        fiscCLR.setSystemNetworkIdentifier("00");
                        fiscCLR.setAdderssControlField("00");
                        fiscCLR.setMessageType(msgType);
                        fiscCLR.setProcessingCode(getFeptxn().getFeptxnPcode());
                        fiscCLR.setSystemTraceAuditNo(getFeptxn().getFeptxnStan());
                        fiscCLR.setTxnDestinationInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnDesBkno(), 7, '0'));
                        fiscCLR.setTxnSourceInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnBkno(), 7, '0'));
                        if (getFeptxn().getFeptxnReqDatetime().length() == 14) {
                            fiscCLR.setTxnInitiateDateAndTime(
                                    CalendarUtil.adStringToROCString(getFeptxn().getFeptxnReqDatetime().substring(0, 8)).substring(1, 7) + getFeptxn().getFeptxnReqDatetime().substring(8, 14)); // (轉成民國年)
                        } else {
                            fiscCLR.setTxnInitiateDateAndTime(getFeptxn().getFeptxnReqDatetime());
                        }
                        switch (msgType.substring(2, 4)) {
                            case "00": // Request
                                fiscCLR.setResponseCode(getFeptxn().getFeptxnReqRc());
                                break;
                            case "10": // Response
                                fiscCLR.setResponseCode(getFeptxn().getFeptxnRepRc());
                                break;
                            case "02": // Confirm
                                fiscCLR.setResponseCode(getFeptxn().getFeptxnConRc());
                                break;
                        }
                        fiscCLR.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTrmsync());
                    } else {
                        getLogContext().setRemark("組財金物件(fiscCLR)為Nothing");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.MessageFormatError;
                    }
                    break;
                }
                case FCCLR: { // FCCLR
                    if (fiscFCCLR != null) {
                        fiscFCCLR.setSystemSupervisoryControlHeader("00");
                        fiscFCCLR.setSystemNetworkIdentifier("00");
                        fiscFCCLR.setAdderssControlField("00");
                        fiscFCCLR.setMessageType(msgType);
                        fiscFCCLR.setProcessingCode(getFeptxn().getFeptxnPcode());
                        fiscFCCLR.setSystemTraceAuditNo(getFeptxn().getFeptxnStan());
                        fiscFCCLR.setTxnDestinationInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnDesBkno(), 7, '0'));
                        fiscFCCLR.setTxnSourceInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnBkno(), 7, '0'));
                        if (getFeptxn().getFeptxnReqDatetime().length() == 14) {
                            fiscFCCLR.setTxnInitiateDateAndTime(
                                    CalendarUtil.adStringToROCString(getFeptxn().getFeptxnReqDatetime().substring(0, 8)).substring(1, 7) + getFeptxn().getFeptxnReqDatetime().substring(8, 14)); // (轉成民國年)
                        } else {
                            fiscFCCLR.setTxnInitiateDateAndTime(getFeptxn().getFeptxnReqDatetime());
                        }
                        switch (msgType.substring(2, 4)) {
                            case "00": // Request
                                fiscFCCLR.setResponseCode(getFeptxn().getFeptxnReqRc());
                                break;
                            case "10": // Response
                                fiscFCCLR.setResponseCode(getFeptxn().getFeptxnRepRc());
                                break;
                            case "02": // Confirm
                                fiscFCCLR.setResponseCode(getFeptxn().getFeptxnConRc());
                                break;
                        }
                        fiscFCCLR.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTrmsync());
                    } else {
                        getLogContext().setRemark("組財金物件(fiscFCCLR)為Nothing");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.MessageFormatError;
                    }
                    break;
                }
                case OPC: {// OPC
                    if (fiscOPC != null) {
                        fiscOPC.setSystemSupervisoryControlHeader("00");
                        fiscOPC.setSystemNetworkIdentifier("00");
                        fiscOPC.setAdderssControlField("00");
                        fiscOPC.setMessageType(msgType);
                        fiscOPC.setProcessingCode(getFeptxn().getFeptxnPcode());
                        fiscOPC.setSystemTraceAuditNo(getFeptxn().getFeptxnStan());
                        fiscOPC.setTxnDestinationInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnDesBkno(), 7, '0'));
                        fiscOPC.setTxnSourceInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnBkno(), 7, '0'));
                        if (getFeptxn().getFeptxnReqDatetime().length() == 14) {
                            fiscOPC.setTxnInitiateDateAndTime(
                                    CalendarUtil.adStringToROCString(getFeptxn().getFeptxnReqDatetime().substring(0, 8)).substring(1, 7) + getFeptxn().getFeptxnReqDatetime().substring(8, 14)); // (轉成民國年)
                        } else {
                            fiscOPC.setTxnInitiateDateAndTime(getFeptxn().getFeptxnReqDatetime());
                        }
                        switch (msgType.substring(2, 4)) {
                            case "00": // Request
                                fiscOPC.setResponseCode(getFeptxn().getFeptxnReqRc());
                                break;
                            case "10": // Response
                                fiscOPC.setResponseCode(getFeptxn().getFeptxnRepRc());
                                break;
                            case "02": // Confirm
                                fiscOPC.setResponseCode(getFeptxn().getFeptxnConRc());
                                break;
                        }
                        if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlChecksync())) {
                            fiscOPC.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTopcsync());
                        } else {
                            fiscOPC.setSyncCheckItem("00000000");
                        }
                    } else {
                        getLogContext().setRemark("組財金物件(fiscOPC)為Nothing");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.MessageFormatError;
                    }
                    break;
                }
                case EMVIC: { // EMVIC
                    if (fiscEMVIC != null) {
                        fiscEMVIC.setSystemSupervisoryControlHeader("00");
                        fiscEMVIC.setSystemNetworkIdentifier("00");
                        fiscEMVIC.setAdderssControlField("00");
                        fiscEMVIC.setMessageType(msgType);
                        fiscEMVIC.setProcessingCode(getFeptxn().getFeptxnPcode());
                        fiscEMVIC.setSystemTraceAuditNo(getFeptxn().getFeptxnStan());
                        if ("10".equals(msgType.substring(2, 4))) {
                            fiscEMVIC.setTxnDestinationInstituteId(fiscEMVICReq.getTxnDestinationInstituteId());
                            fiscEMVIC.setTxnSourceInstituteId(fiscEMVICReq.getTxnSourceInstituteId());
                        } else {
                            fiscEMVIC.setTxnDestinationInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnDesBkno(), 7, '0'));
                            fiscEMVIC.setTxnSourceInstituteId(StringUtils.rightPad(getFeptxn().getFeptxnBkno(), 7, '0'));
                        }
                        if (getFeptxn().getFeptxnReqDatetime().length() == 14) {
                            fiscEMVIC.setTxnInitiateDateAndTime(
                                    CalendarUtil.adStringToROCString(getFeptxn().getFeptxnReqDatetime().substring(0, 8)).substring(1, 7) + getFeptxn().getFeptxnReqDatetime().substring(8, 14)); // (轉成民國年)
                        } else {
                            fiscEMVIC.setTxnInitiateDateAndTime(getFeptxn().getFeptxnReqDatetime());
                        }
                        switch (msgType.substring(2, 4)) {
                            case "00": // Request
                                fiscEMVIC.setResponseCode(getFeptxn().getFeptxnReqRc());
                                break;
                            case "10": // Response
                                fiscEMVIC.setResponseCode(getFeptxn().getFeptxnRepRc());
                                break;
                            case "02": // Confirm
                                fiscEMVIC.setResponseCode(getFeptxn().getFeptxnConRc());
                                break;
                        }

                        Sysstat defSYSSTAT = new Sysstat();
                        defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                        defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
                        try {
                            if (defSYSSTAT != null) {
                                // 有找到讀DB
                                fiscEMVIC.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
                            } else {
                                // 沒找到Return Error
                                getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第一次讀SYSSTAT Not Found");
                                this.logMessage(getLogContext());
                                return FEPReturnCode.SYSSTATNotFound;
                            }
                        } catch (Exception ex) {
                            getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第一次讀SYSSTAT失敗:" + ex.toString());
                            this.logMessage(getLogContext());
                            // 發生Exception再讀一次
                            try {
                                defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                                defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
                                if (defSYSSTAT != null) {
                                    // 有找到讀DB，rtnCode=Normal
                                    fiscEMVIC.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
                                    rtnCode = CommonReturnCode.Normal;
                                } else {
                                    // 沒找到，rtnCode=FEPReturnCode.SYSSTATNotFound
                                    getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第二次讀SYSSTAT Not Found");
                                    this.logMessage(getLogContext());
                                    rtnCode = FEPReturnCode.SYSSTATNotFound;
                                }
                            } catch (Exception ex2) {
                                getLogContext().setRemark("PrepareHeader-(INBK)跨行交易第二次讀SYSSTAT發生例外錯誤:" + ex2.toString());
                                this.logMessage(getLogContext());
                                rtnCode = FEPReturnCode.SYSSTATNotFound;
                            }
                            // 第二次失敗時Return Error
                            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                                return FEPReturnCode.SYSSTATNotFound;
                            }
                        }
                    } else {
                        getLogContext().setRemark("組財金物件(fiscEMVIC)為Nothing");
                        this.logMessage(getLogContext());
                        return FISCReturnCode.MessageFormatError;
                    }
                    break;
                }
                default:
                    break;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * UNPACK PINBLOCK
     *
     *
     * <history>
     * <modify>
     * <modifier>HusanYin</modifier>
     * <reason></reason>
     * <date>2010/09/24</date>
     * </modify>
     * </history>
     */
    private static String unPack(String data) {

        // 特殊字元不UNPACK (: ; < = > ?)
        if (StringUtils.indexOfAny(data, new char[]{':', ';', '<', '=', '>', '?'}) == -1) {
            return data;
        }
        data = StringUtil.toHex(data);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (i = 0; i < data.length(); i += 2) {
            String x = data.substring(i, i + 2);
            int dec = Integer.parseInt(x, 16);
            char ascS = '0';
            if (dec >= 48 && dec <= 57) {
                ascS = (char) dec;
            } else {
                ascS = (char) (dec + 7);
            }
            sb.append(ascS);
        }

        return sb.toString();
    }

    /**
     * 將欄位中的值組bitmap
     */
    public FEPReturnCode makeBitmap() {
        return null;
    }

    public void setfisc(FISC_INBK fisc) {

        this.fiscINBKReq = fisc;
    }

    /**
     * 將欄位中的值組bitmap
     *
     *
     * <modify>
     * <modifier>HusanYin</modifier>
     * <reason>新增datatype D</reason>
     * <date>2011/03/07</date>
     * </modify>
     */
    public FEPReturnCode makeBitmap(String MessageType, String ProcessingCode, MessageFlow msgFunction) {
        // 讀取BITMAP定義
        Bitmapdef oBitMap = getBitmapData(MessageType + ProcessingCode);
        char[] bitMapDefine = new char[64];

        //讀取財金電文AP DATA ELEMENT 定義
        List<Dataattr> dvAttr = getDataAttributeDataByType(oBitMap.getBitmapdefType().toString());

        StringBuilder sData = new StringBuilder();
        String sBitMapConfiguration = null;
        int i = 0;
        String tmp = null;
        try {
            /* BIT MAP 位置 */
            for (i = 0; i < bitMapDefine.length; i++) {
                /* 依電文類別, 不同系統之財金電文, 放入 BitMap */
                tmp = null;
                switch (msgFunction) {
                    case Request:
                        switch (getFISCTxData().getFiscTeleType()) {
                            case INBK:
                                tmp = fiscINBKReq.getGetPropertyValue(i);
                                break;
                            case OPC:
                                tmp = fiscOPCReq.getGetPropertyValue(i);
                                break;
                            case CLR:
                                tmp = fiscCLRReq.getGetPropertyValue(i);
                                break;
                            case FCCLR:
                                tmp = fiscFCCLRReq.getGetPropertyValue(i);
                                break;
                            case EMVIC:
                                tmp = fiscEMVICReq.getGetPropertyValue(i);
                                break;
                            default:
                          	  break;
                        }
                        break;
                    case Confirmation:
                        switch (getFISCTxData().getFiscTeleType()) {
                            case INBK:
                                tmp = fiscINBKCon.getGetPropertyValue(i);
                                break;
                            case OPC:
                                tmp = fiscOPCCon.getGetPropertyValue(i);
                                break;
                            case EMVIC:
                                tmp = fiscEMVICCon.getGetPropertyValue(i);
                                break;
                            default:
                                break;
                        }
                        break;
                    case Response:
                        switch (getFISCTxData().getFiscTeleType()) {
                            case INBK:
                                tmp = fiscINBKRes.getGetPropertyValue(i);
                                break;
                            case OPC:
                                tmp = fiscOPCRes.getGetPropertyValue(i);
                                break;
                            case CLR:
                                tmp = fiscCLRRes.getGetPropertyValue(i);
                                break;
                            case FCCLR:
                                tmp = fiscFCCLRRes.getGetPropertyValue(i);
                                break;
                            case EMVIC:
                                tmp = fiscEMVICRes.getGetPropertyValue(i);
                                break;
                            default:
                          	  break;
                        }
                        break;
                    case None:
                        tmp = fiscOPCReq.getGetPropertyValue(i);
                        break;
                    default:
                        break;
                }
                if (tmp != null) {
                    bitMapDefine[i] = '1'; /* 有值時,BitMap On */
                    int dataLen = dvAttr.get(i).getDataattrHexLen().intValue();
                    switch (dvAttr.get(i).getDataattrDatatype()) {
                        case "C": /* 資料型態=中文欄位 */
                            if (DbHelper.toBoolean(dvAttr.get(i).getDataattrEncoding())) {
                                // 將電文欄位轉成CNS11643
                                getLogContext().setRemark("tmp:" + tmp);
                                getLogContext().setProgramName(ProgramName);
                                logMessage(Level.DEBUG, getLogContext());
                            }
                            break;
                        case "B":  /* 資料型態=Binary */
                            tmp = StringUtil.convertFromAnyBaseString(String.valueOf((tmp.length() / 2) + 2), 10, 16, 4) + tmp;
                            break;
                        case "9":
                        case "A":
                        case "D": /* 資料型態=數字或日期 */
                            if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                                tmp = EbcdicConverter.toHex(CCSID.English,StringUtils.leftPad(tmp, (dataLen / 2), '0').length(),StringUtils.leftPad(tmp, (dataLen / 2), '0'));
                            }else{
                                tmp = StringUtil.toHex(StringUtils.leftPad(tmp, (dataLen / 2), '0'));
                            }
                            break;
                        case "2": /* 資料型態=數字(兩位小數) */
                        	if(StringUtils.isBlank(tmp)) {
                        		 if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                                     tmp = EbcdicConverter.toHex(CCSID.English, StringUtils.leftPad("0", (dataLen / 2), '0').length(), StringUtils.leftPad("0", (dataLen / 2), '0'));
                                 }else{
                                     tmp = StringUtil.toHex(StringUtils.leftPad("0", (dataLen / 2), '0'));
                                 }
                        	}else {
								tmp = StringUtils.leftPad(String.valueOf(String.format("%.2f", (Double.parseDouble(tmp) * 100))), (dataLen / 2), '0');
								if (FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic) {
									tmp = EbcdicConverter.toHex(CCSID.English, tmp.length(), tmp);
								} else {
									tmp = StringUtil.toHex(tmp);
								}
                        	}
                            break;
                        case "M":  /* 資料型態=金額 */
                            BigDecimal amt = new BigDecimal(tmp);
                            // 先將金額欄位取絕對值, 再放入正負符號
                            tmp = StringUtils.leftPad(String.valueOf(Math.abs(amt.multiply(new BigDecimal(100)).longValue())), (dataLen / 2) - 1, '0');
                            // tmp = Convert.ToInt64(amt * 100).ToString.PadLeft(CInt(dataLen / 2) - 1, "0"c)
                            // Fly 2020/02/19 值為0時改用+號
                            if (amt.longValue() >= 0) {
                                tmp = "+" + tmp;
                            } else {
                                tmp = "-" + tmp;
                            }
                            // Bruis 2024/04/11 判斷並轉成Ebcdic(HEX)
                            if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                                tmp = EbcdicConverter.toHex(tmp);
                            }else {
                                tmp = StringUtil.toHex(tmp);
                            }

                            break;
                        case "X":  /* 資料型態=文數字 */
                            
                            if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                                if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                                    tmp = EbcdicConverter.toHex(CCSID.English, StringUtils.rightPad(tmp, dataLen / 2, ' ').length(),
                                                StringUtils.rightPad(tmp, dataLen / 2, ' '));
                                }
                                else {
                                    tmp = StringUtil.toHex(StringUtils.rightPad(tmp, dataLen / 2, ' '));
                                }
                                
                            } else {
                                tmp = StringUtils.rightPad(tmp, dataLen, ' ');
                            }
                            break;
                    }

                    sData.append(tmp);
                } else {
                    bitMapDefine[i] = '0'; // 未給值bitmap塞0
                }
            }

            // 將 BitMap 放入財金電文
            sBitMapConfiguration = StringUtil.convertFromAnyBaseString(new String(bitMapDefine), 2, 16, 16);
            /* 依電文類別, 不同系統之財金電文, 放入 BitMap */
            switch (msgFunction) {
                case Request: /* Request 電文 */
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBKReq.setBitMapConfiguration(sBitMapConfiguration);
                            fiscINBKReq.setAPData(sData.toString());
                            break;
                        case OPC:
                            fiscOPCReq.setBitMapConfiguration(sBitMapConfiguration);
                            fiscOPCReq.setAPData(sData.toString());
                            break;
                        case CLR:
                            fiscCLRReq.setBitMapConfiguration(sBitMapConfiguration);
                            fiscCLRReq.setAPData(sData.toString());
                            break;
                        case FCCLR:
                            fiscFCCLRReq.setBitMapConfiguration(sBitMapConfiguration);
                            fiscFCCLRReq.setAPData(sData.toString());
                            break;
                        case EMVIC:
                            fiscEMVICReq.setBitMapConfiguration(sBitMapConfiguration);
                            fiscEMVICReq.setAPData(sData.toString());
                            break;
                        default:
                      	  break;
                    }
                    break;
                case Confirmation:  /* Confirm 電文*/
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBKCon.setBitMapConfiguration(sBitMapConfiguration);
                            fiscINBKCon.setAPData(sData.toString());
                            break;
                        case OPC:
                            fiscOPCCon.setBitMapConfiguration(sBitMapConfiguration);
                            fiscOPCCon.setAPData(sData.toString());
                            break;
                        case EMVIC:
                            fiscEMVICCon.setBitMapConfiguration(sBitMapConfiguration);
                            fiscEMVICCon.setAPData(sData.toString());
                            break;
                        default:
                            break;
                    }
                    break;
                case Response:  /* Response 電文*/
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBKRes.setBitMapConfiguration(sBitMapConfiguration);
                            fiscINBKRes.setAPData(sData.toString());
                            break;
                        case OPC:
                            fiscOPCRes.setBitMapConfiguration(sBitMapConfiguration);
                            fiscOPCRes.setAPData(sData.toString());
                            break;
                        case CLR:
                            fiscCLRRes.setBitMapConfiguration(sBitMapConfiguration);
                            fiscCLRRes.setAPData(sData.toString());
                            break;
                        case FCCLR:
                            fiscFCCLRRes.setBitMapConfiguration(sBitMapConfiguration);
                            fiscFCCLRRes.setAPData(sData.toString());
                            break;
                        case EMVIC:
                            fiscEMVICRes.setBitMapConfiguration(sBitMapConfiguration);
                            fiscEMVICRes.setAPData(sData.toString());
                            break;
                        default:
                      	  break;
                    }
                    break;
                case None:
                    fiscOPCReq.setBitMapConfiguration(sBitMapConfiguration);
                    fiscOPCReq.setAPData(sData.toString());
                    break;
                default:
                    break;
            }

            getLogContext().setRemark("BitMapConfiguration:" + sBitMapConfiguration + ",APPDATA:" + sData.toString());
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".makeBitmap");
            sendEMS(getLogContext());
            return FISCReturnCode.CheckBitMapError;
        }
    }

    public Bitmapdef getBitmapData(String msgTypePcode) {
        // Fly 2016/11/25 避免多執行續同時對BitmapData做操作，增加LOCK的範圍
        synchronized (bitmapdefMap) {
            if (bitmapdefMap.size() == 0) {
                this.loadBitmapData();
            }
            return bitmapdefMap.get(msgTypePcode);
        }
    }

    public List<Dataattr> getDataAttributeDataByType(String attrType) {
        synchronized (dataAttrList) {
            if (dataAttrList.size() == 0) {
                List<Dataattr> dtDataAttr = dataattrExtMapper.queryAllData("");
                for (int i = 1; i <= 7; i++) {
                    short temp = (short) i;
                    List<Dataattr> filteredAndSortedList = dtDataAttr.stream().filter(t -> t.getDataattrType() == temp).sorted(new Comparator<Dataattr>() {
                        @Override
                        public int compare(Dataattr o1, Dataattr o2) {
                            return o1.getDataattrBitoffset().compareTo(o2.getDataattrBitoffset());
                        }
                    }).collect(Collectors.toList());
                    dataAttrList.put(String.valueOf(i), filteredAndSortedList);
                }
            }
        }
        return dataAttrList.get(attrType);
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
    @Override
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
            if (FEPChannel.ATM.name().equals(getFeptxn().getFeptxnChannel())
                    || FEPChannel.EAT.name().equals(getFeptxn().getFeptxnChannel())) {
                // 檢核實體ATM-汽燃費費用代號前三碼
                if (!getFeptxn().getFeptxnPayno().substring(0, 3).equals(INBKConfig.getInstance().getFuelATMPAYNO3())) {
                    getLogContext().setRemark("CheckFuelPayType-實體/WEB ATM汽燃費費用代號前三碼<>" + INBKConfig.getInstance().getFuelATMPAYNO3() + ", FEPTXN_PAYNO=" + getFeptxn().getFeptxnPayno());
                    this.logMessage(getLogContext());
                    rtnCode = FEPReturnCode.OtherCheckError;
                    return rtnCode;
                }

                // 檢核汽燃費之轉入銀行
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
            } else {
                // 檢核網路通路-汽燃費費用代號前三碼
                if (!getFeptxn().getFeptxnPayno().substring(0, 3).equals(INBKConfig.getInstance().getFuelNBPAYNO3())) {
                    getLogContext().setRemark("CheckFuelPayType-網路通路汽燃費費用代號前三碼<>" + INBKConfig.getInstance().getFuelNBPAYNO3() + ", FEPTXN_PAYNO=" + getFeptxn().getFeptxnPayno());
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
     * 負責處理 FISC 跨行交易電文 Basic Check
     *
     * @param checkFEPTXN 是否要檢核FEPTxn資料
     * @return <history>
     * <modify>
     * <modifier>Ashiang</modifier>
     * <reason></reason>
     * <date>2009/12/01</date>
     * <modifier>HusanYin</modifier>
     * <reason>connie spec change</reason>
     * <date>2010/10/20</date>
     * <reason>connie spec change the logic of check_stan for lateResponse differ other AA</reason>
     * <date>2010/11/23</date>
     * <reason>修正Const RC</reason>
     * <date>2010/11/25</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkHeader(FISCHeader mfiscHeader, boolean checkFEPTXN) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FEPReturnCode rtnCode2 = null;
        try {
            fiscHeader = mfiscHeader;
            // 檢查SYS HEADER
            if (!"00".equals(mfiscHeader.getSystemSupervisoryControlHeader()) || !"00".equals(mfiscHeader.getSystemNetworkIdentifier()) || !"00".equals(mfiscHeader.getAdderssControlField())) {
                return FISCReturnCode.MessageFormatError; // 訊息格式或內容編輯錯誤
            }

            // 檢核無效之訊息類別(MSGTYPE)
            if (INBKMessageType.indexOf(mfiscHeader.getMessageType()) < 0) {
                return FISCReturnCode.MessageTypeError;
            }

            // 檢核交易類別(PCODE) 是否為空白
            if (StringUtils.isBlank(getFISCTxData().getMsgCtl().getMsgctlPcode())) {
                return FISCReturnCode.MessageTypeError; // 無效之訊息類別代碼( MESSAGE TYPE )或交易類別代碼( PROCESSING CODE )
            }


            // 檢核日期欄位是否正確
            if (!PolyfillUtil.isNumeric(mfiscHeader.getTxnInitiateDateAndTime())) {
                rtnCode = FISCReturnCode.MessageFormatError;
            } else {
                Calendar wk_TX_Date = CalendarUtil.rocStringToADDate("0" + mfiscHeader.getTxnInitiateDateAndTime().substring(0, 6));
                if (wk_TX_Date == null) {
                    rtnCode = FISCReturnCode.MessageFormatError;
                } else {
                    if (mfiscHeader.getTxnInitiateDateAndTime().substring(6, 8).compareTo("23") > 0 || mfiscHeader.getTxnInitiateDateAndTime().substring(8, 10).compareTo("59") > 0
                            || mfiscHeader.getTxnInitiateDateAndTime().substring(10, 12).compareTo("59") > 0) {
                        rtnCode = FISCReturnCode.MessageFormatError;
                    }
                }

                if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                    // 檢核STAN
                    if (checkFEPTXN) {
                        setOriginalFEPTxn(
                                feptxnDao.getFEPTXNByReqDateAndStan(FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), mfiscHeader.getTxnSourceInstituteId().substring(0, 3),
                                        mfiscHeader.getSystemTraceAuditNo()));
                        if (mfiscHeader.getMessageKind() == MessageFlow.Request) {
                            if (getOriginalFEPTxn() != null) {
                                return FISCReturnCode.TraceNumberDuplicate; /* 1002-STAN 重覆 */
                            }
                            if ("00".equals(mfiscHeader.getMessageType().substring(2)) && !mfiscHeader.getResponseCode().equals(NormalRC.FISC_REQ_RC)
                                    && !mfiscHeader.getResponseCode().equals(NormalRC.FISC_REQ_RC_ChangeDate)) {
                                rtnCode = FISCReturnCode.InvalidResponseCode; /* 0102-無效之回應代碼 */
                            }
                            /* 檢核收信單位(DES_BKNO)及發信單位(SRC_BKNO) */
//                            if (mfiscHeader.getTxnSourceInstituteId().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())
//                                    || !(mfiscHeader.getTxnDestinationInstituteId().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno()))) {
//                                return FISCReturnCode.SenderIdError;
//                            }
                        } else {
                            if (getOriginalFEPTxn() == null) {
                                FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
                                db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, ".checkHeader"));
                                setOriginalFEPTxn(db.getFEPTXNByReqDateAndStan(FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                                        mfiscHeader.getTxnSourceInstituteId().substring(0, 3),
                                        mfiscHeader.getSystemTraceAuditNo()));

                                if (getOriginalFEPTxn() == null) {
                                    return FISCReturnCode.OriginalMessageError; /* 1003-訊息與原跨行交易之執行狀態不符*/
                                }
                            }
                            /* 3-2 FEP處理的交易才做以下檢核 */
                            if ( "N".equals(getOriginalFEPTxn().getFeptxnCbsProc()) ) {
                                if (mfiscHeader.getMessageKind().getValue() == MessageFlow.Response.getValue() && (!getOriginalFEPTxn().getFeptxnMsgflow().equals(FEPTxnMessageFlow.FISC_Request))
                                        && (DbHelper.toBoolean(getOriginalFEPTxn().getFeptxnAaComplete()))
                                        || (mfiscHeader.getMessageKind().getValue() == MessageFlow.Confirmation.getValue()
                                        && (!getOriginalFEPTxn().getFeptxnMsgflow().equals(FEPTxnMessageFlow.FISC_Response)))) {
                                    return FISCReturnCode.OriginalMessageError; /* 1003-訊息與原跨行交易之執行狀態不符 */
                                }
                            }
                            /* 檢核交易類別(PCODE) */
                            if (!mfiscHeader.getProcessingCode().equals(getOriginalFEPTxn().getFeptxnPcode())) {
                                /* 5/11 修改 for STAN 重複 */
                                if (mfiscHeader.getTxnSourceInstituteId().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())
                                        && mfiscHeader.getProcessingCode().substring(0, 1).equals("2")
                                        && mfiscHeader.getMessageKind().getValue() == MessageFlow.Response.getValue()) {
                                    FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
                                    db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, ".checkHeader"));
                                    setOriginalFEPTxn(db.getFEPTXNByReqDateAndStan(FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                                            mfiscHeader.getTxnSourceInstituteId().substring(0, 3),
                                            mfiscHeader.getSystemTraceAuditNo()));

                                    if (getOriginalFEPTxn() == null || mfiscHeader.getProcessingCode().equals(getOriginalFEPTxn().getFeptxnPcode())) {
                                        return FISCReturnCode.MessageTypeError;  /* 1001-無效之訊息類別代碼( MESSAGE TYPE )或交易類別代碼( PROC CODE ) */
                                    }
                                }
                            }
                            /* 檢核收信單位(DES_BKNO)及發信單位(SRC_BKNO) */
//                            if (getOriginalFEPTxn() != null && (!(mfiscHeader.getTxnSourceInstituteId().substring(0, 3).equals(getOriginalFEPTxn().getFeptxnBkno()))
//                                    || !(mfiscHeader.getTxnDestinationInstituteId().substring(0, 3).equals(getOriginalFEPTxn().getFeptxnDesBkno())))) {
//                                return FISCReturnCode.SenderIdError;
//                            }
                        }

                    }

                }
                getLogContext().setRemark("CheckHeader 檢查日期時間格式rtnCode=" + rtnCode);
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG, getLogContext());
                if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                    // 檢核押碼基碼
                    if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlChecksync())) {
                        if (("02".equals(mfiscHeader.getMessageType().substring(0, 2)) && !SysStatus.getPropertyValue().getSysstatFcdsync().equals(mfiscHeader.getSyncCheckItem()))
                                || ("05".equals(mfiscHeader.getMessageType().substring(0, 2)) && !SysStatus.getPropertyValue().getSysstatFrmsync().equals(mfiscHeader.getSyncCheckItem()))
                                || (("06".equals(mfiscHeader.getMessageType().substring(0, 2)) || "08".equals(mfiscHeader.getMessageType().substring(0, 2)))
                                && !SysStatus.getPropertyValue().getSysstatFopcsync().equals(mfiscHeader.getSyncCheckItem()))) {
                            /*　3/19 修改 for 押碼基碼(KEY)不同步 */
                            if ("02".equals(mfiscHeader.getMessageType().substring(0, 2))) {
                                FEPCache.reloadCache(CacheItem.SYSSTAT);
                                if (!mfiscHeader.getSyncCheckItem().equals(SysStatus.getPropertyValue().getSysstatFcdsync())) {
                                    rtnCode = FISCReturnCode.KeySyncError; /* 0301-押碼基碼( KEY )不同步*/
                                    getLogContext().setRemark(StringUtils.join(
                                            "押碼基碼( KEY )不同步(11031),",
                                            " mfiscHeader.getSyncCheckItem() = [", mfiscHeader.getSyncCheckItem(), "]",
                                            " SysStatus.getPropertyValue().getSysstatFcdsync() = [", SysStatus.getPropertyValue().getSysstatFcdsync(), "]"));
                                    getLogContext().setProgramName(ProgramName);
                                    logMessage(Level.WARN, getLogContext());
                                }
                            } else {
                                rtnCode = FISCReturnCode.KeySyncError;  /* 0301-押碼基碼( KEY )不同步*/
                                getLogContext().setRemark(StringUtils.join(
                                        "押碼基碼( KEY )不同步(11031),",
                                        " mfiscHeader.getMessageType().substring(0, 2) = [", mfiscHeader.getMessageType().substring(0, 2), "]",
                                        " which was not equal to = [02]"));
                                getLogContext().setProgramName(ProgramName);
                                logMessage(Level.WARN, getLogContext());
                            }
                            // rtnCode = FISCReturnCode.KeySyncError
                        }
                    } else {
                        if (!mfiscHeader.getSyncCheckItem().equals("00000000")) {
                            rtnCode = FISCReturnCode.MessageFormatError; // 訊息格式或內容編輯錯誤
                        }
                    }
                }
                if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                    // 檢核財金及參加單位之系統狀態
                    if ("02".equals(mfiscHeader.getMessageType().substring(0, 2))) { /* CD/ATM */
                        if (!getFISCTxData().isTxStatus()) {
                            /* 8/3 修改,原存行及代理行交易暫停服務 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {
                                rtnCode = CommonReturnCode.InterBankServiceStop; /* 原存行交易暫停服務(0205) */
                            } else {
                                rtnCode = FISCReturnCode.SenderBankServiceStop; /* 發信單位該項跨行業務停止或暫停營業(0202) */
                            }

                        } else {
                            if (mfiscHeader.getMessageType().substring(2, 4).equals("00") || mfiscHeader.getMessageType().substring(2, 4).equals("02")) {
                                rtnCode = checkINBKStatus(mfiscHeader.getProcessingCode(), PolyfillUtil.ctype(FISCTxType.Acquire.getValue()));
                            } else {
                                rtnCode = checkINBKStatus(mfiscHeader.getProcessingCode(), PolyfillUtil.ctype(FISCTxType.Issue.getValue()));
                            }
                        }
                    }
                }
            }

            // 拆解財金 Body 電文
            if (mfiscHeader.getMessageKind().getValue() == MessageFlow.Request.getValue() || rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                String APData = "";
                switch (getFISCTxData().getFiscTeleType()) {
                    case INBK:
                        APData = fiscINBKReq.getAPData();
                        break;
                    case OPC:
                        APData = fiscOPCReq.getAPData();
                        break;
                    case CLR:
                        APData = fiscCLRReq.getAPData(); // CLR
                        break;
                    case FCCLR:
                        APData = fiscFCCLRReq.getAPData(); // CLR
                        break;
                    case EMVIC:
                        APData = fiscEMVICReq.getAPData(); // EMVIC
                        break;
                    default:
                        break;
                }
                if (mfiscHeader.getMessageKind().getValue() == MessageFlow.Response.getValue()) {
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            APData = fiscINBKRes.getAPData();
                            break;
                        case OPC:
                            APData = fiscOPCRes.getAPData();
                            break;
                        case CLR:
                            APData = fiscCLRRes.getAPData(); // CLR
                            break;
                        case FCCLR:
                            APData = fiscFCCLRRes.getAPData(); // CLR
                            break;
                        case EMVIC:
                            APData = fiscEMVICRes.getAPData(); // EMVIC
                            break;
                        default:
                            break;
                    }
                }
                if (mfiscHeader.getMessageKind().getValue() == MessageFlow.Confirmation.getValue()) {
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            APData = fiscINBKCon.getAPData();
                            break;
                        case OPC:
                            APData = fiscOPCCon.getAPData();
                            break;
                        case EMVIC:
                            APData = fiscEMVICCon.getAPData();
                            break;
                        default:
                            break;
                    }
                }
                rtnCode2 = checkBitmap(APData);
                if (rtnCode2.getValue() == FISCReturnCode.CheckBitMapError.getValue()) {
                    return FISCReturnCode.CheckBitMapError;
                }
            }
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            if (rtnCode2 != null && rtnCode2.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode2;
            }

            // 檢核是否需作換日作業
            switch (mfiscHeader.getMessageType().substring(0, 2)) { /* CD/ATM */
                case "02": // 只有INBK才要判斷
                    if (getFISCTxData().getFiscTeleType().getValue() == FISCSubSystem.INBK.getValue()) {
                        String mBUSINESS_DATE = null;
                        if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                            mBUSINESS_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKReq.getBusinessDate(), 7, '0'));
                        } else {
                            mBUSINESS_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBKRes.getBusinessDate(), 7, '0'));
                        }
                        if (SysStatus.getPropertyValue().getSysstatTbsdyFisc().compareTo(mBUSINESS_DATE) < 0) {
                            rtnCode = FEPReturnCode.FISCBusinessDateChanged;
                            return  rtnCode;
                        }
                    }
                    break;
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            if (MessageFlow.Request.equals(mfiscHeader.getMessageKind())) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkHeader][", mfiscHeader.getClass().getSimpleName(), "]FISC Data : ", mfiscHeader.toJSON());
        }
    }

    /**
     * 負責處理FISC 跨行交易電文Body Check
     *
     * @param intlTxn 國際卡資料
     * @return
     */
    public FEPReturnCode checkRequestBody(Intltxn intlTxn) {
        try {
            return CommonReturnCode.Normal;
        } catch (Exception ex) {

        }
        return null;
    }

    @SuppressWarnings("unused")
    private FEPReturnCode checkPcode(String pcode) {
        switch (pcode) {
            case "2500":
            case "2410":
            case "2450":
            case "2430":
            case "2470":
            case "2440":
            case "2441":
            case "2445":
            case "2541":
            case "2542":
            case "2543":
            case "2551":
            case "2552":
                getFISCTxData().setMessageFunctionType(FunctionType.Inquire);
                break;
            case "2510":
                getFISCTxData().setMessageFunctionType(FunctionType.Withdraw);
                break;
            case "2521":
            case "2561":
            case "2261":
            case "2210":
                getFISCTxData().setMessageFunctionType(FunctionType.TransferIn);
                break;
            case "2522":
            case "2562":
            case "2262":
            case "2523":
            case "2563":
            case "2263":
            case "2525":
                getFISCTxData().setMessageFunctionType(FunctionType.TransferOut);
                break;
            case "2524":
                if (fiscINBKReq.getTotalLength() == 138) {
                    getFISCTxData().setMessageFunctionType(FunctionType.TransferIn);
                } else {
                    getFISCTxData().setMessageFunctionType(FunctionType.TransferOut);
                }
                break;
            case "2531":
                getFISCTxData().setMessageFunctionType(FunctionType.Payment);
                break;
            case "3201":
                getFISCTxData().setMessageFunctionType(FunctionType.Other);
                break;
            default:

                return FISCReturnCode.MessageTypeError;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 檢核是否為 GiftCard
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>AE卡要取2~7碼讀BIN檔資料</reason>
     * <date>2011/8/16</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkGiftCard() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Bin bin = new BinExt();
        String binNo = null;

        try {
            // add by Maxine on 2011/08/22 for spec 8/17 修改, 檢核轉入帳號 */
            if (StringUtils.isBlank(getFeptxn().getFeptxnTrinActno())) {
                return FEPReturnCode.TranInACTNOError; // 轉入帳號錯誤
            }

            if ("0".equals(getFeptxn().getFeptxnTrinActno().substring(0, 1))) {
                // AE卡號15位，帳號第1碼為0，讀取帳號第2~7碼
                binNo = getFeptxn().getFeptxnTrinActno().substring(1, 7);
            } else {
                // 帳號第1碼不為0，直接讀取帳號前6碼
                binNo = getFeptxn().getFeptxnTrinActno().substring(0, 6);
            }

            // modified by Maxine on 2012/03/01 for 讀取 BIN 方式由 Cache 改為讀 DB
            bin = getBin(binNo, getFeptxn().getFeptxnTrinBkno());

            if (bin != null && StringUtils.isNotBlank(bin.getBinNo())) {

                // add by Maxine on 2011/08/18 for spec 8/17 修改 for 檢核轉入信用卡檢查碼 */
                rtnCode = checkDigit(getFeptxn().getFeptxnTrinActno());
                if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                    return rtnCode;
                }

                // 2018-05-24 Modify by Ruling for MASTER DEBIT加悠遊
                getFeptxn().setFeptxnTrinKind(bin.getBinProd());
                switch (getFeptxn().getFeptxnTrinKind()) {
                    case BINPROD.Gift:
                        // add by maxine 3/21 修改 for 繳費交易(256X)不得轉入 GIFT卡 */
                        if (!"252".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
                            getLogContext().setRemark("繳費交易(256X)不得轉入GIFT卡");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.TranInACTNOError; /// * 轉入帳號錯誤(E074) */
                        }
                        // modified by 3/21 修改for 繳費交易(256X)不得轉入 GIFT卡*/
// 2024-03-06 Richard modified for SYSSTATE 調整
//                        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc()) || !DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatGcard())) {// /*轉帳交易才可進行GIFT卡加值*/{
//                            // If Not SysStatus.PropertyValue.SYSSTAT_ASC OrElse Not SysStatus.PropertyValue.SYSSTAT_GCARD OrElse Feptxn.FEPTXN_PCODE.Substring(0, 3) <> "252" Then '
//                            // /*轉帳交易才可進行GIFT卡加值*/
//                            // 3/21 修改, 和 ATMP 錯誤訊息一致
//                            getLogContext().setRemark(
//                                    "提款暫停服務, 信用卡線路(SYSSTAT_ASC)=" + SysStatus.getPropertyValue().getSysstatAsc() + ", Gift卡線路(SYSSTAT_GCARD)=" + SysStatus.getPropertyValue().getSysstatGcard());
//                            this.logMessage(getLogContext());
//                            return CommonReturnCode.WithdrawServiceStop; // 提款暫停服務(E948)
//                        }
                        break;
                    case BINPROD.Debit:
                        getLogContext().setRemark("轉入帳號為本行簽帳金融卡(DEBIT卡)");
                        this.logMessage(getLogContext());
                        return FEPReturnCode.TranInACTNOError; // 轉入帳號錯誤(E074)
                }
            }

            // 2012/07/31 Modify by Ruling for 修改轉入交易檢核虛擬帳號
            rtnCode = checkVirActno();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            if (getFISCTxData().getMessageFlowType().getValue() == MessageFlow.Request.getValue()) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkGiftCard");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkBitmap(String apdata) {
        fiscHeader.setCheckBitmap(true); // 2021-10-18 Richard add
        // 讀取財金電文BITMAP定義
        Bitmapdef oBitMap = getBitmapData(fiscHeader.getMessageType() + fiscHeader.getProcessingCode());
        if (oBitMap.getBitmapdefBitmapList().indexOf(fiscHeader.getBitMapConfiguration()) < 0) {
            getLogContext().setRemark(
                    "BitMapConfiguration不正確(fiscHeader.BitMapConfiguration='" + fiscHeader.getBitMapConfiguration() + "';BitMap.BITMAPDEF_BITMAP_LIST='" + oBitMap.getBitmapdefBitmapList() + "')");
            getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
            getLogContext().setReturnCode(FISCReturnCode.CheckBitMapError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
            logMessage(Level.ERROR, getLogContext());
            return FISCReturnCode.CheckBitMapError;
        }
        // 讀取財金電文AP DATA ELEMENT定義
        List<Dataattr> dvAttr = getDataAttributeDataByType(oBitMap.getBitmapdefType().toString());

        int k = 0;
        FISC_OPC fiscOPC = new FISC_OPC();
        FISC_INBK fiscINBK = new FISC_INBK();
        FISC_CLR fiscCLR = new FISC_CLR();
        FISC_USDCLR fiscFCCLR = new FISC_USDCLR();
        FISC_EMVIC fiscEMVIC = new FISC_EMVIC();
        FEPReturnCode rtnCode = CommonReturnCode.Normal; // add by henny for 紀錄rc
        int i = 0;

        try {
            // 將財金Bitmap Hex轉2進位
            char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(fiscHeader.getBitMapConfiguration(), 16, 2, 64).toCharArray();
            switch (fiscHeader.getMessageType().substring(2, 4)) {
                case "00": /* Request 電文 */
                case "81":
                case "99":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKReq;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCReq;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRReq;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRReq;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICReq;
                            break;
                        default:
                      	  break;
                    }
                    break;
                case "02":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKCon;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCCon;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICCon;
                            break;
                        default:
                      	  break;
                    }
                    break;
                case "10":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKRes;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCRes;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRRes;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRRes;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICRes;
                            break;
                        default:
                      	  break;
                    }
                    break;
            }
            // 依AP DATA ELEMENT 定義拆解財金電文
            for (i = 0; i < bitMapFromFisc.length; i++) {
                if (bitMapFromFisc[i] == '1') {
                    // Bitmap on 開始處理
                    int tmplen = 0;
                    String tmpHex = "";
                    String tmpAscii = "";
                    String dataType = dvAttr.get(i).getDataattrDatatype().toString();
                    /* 判斷資料型態, 抓取欄位長度 */
                    if (dataType.equalsIgnoreCase("C") || dataType.equalsIgnoreCase("B")) {
                        /* 變動長度欄位 */
                        /* 變動長度從電文取前2個BYTE為此欄位長度L(2) */
                        /* 拆解欄位內容(tmpHex)= DATA */
                        tmplen = Integer.parseInt(StringUtil.convertFromAnyBaseString(apdata.substring(k, k + 4), 16, 10, 0)) * 2;
                        tmpHex = apdata.substring(k + 4, k + 4 + tmplen - 4);
                    } else {
                        /* 固定長度欄位 */
                        tmplen = dvAttr.get(i).getDataattrHexLen().intValue();
                        tmpHex = apdata.substring(k, k + tmplen);
                    }

                    /*  判斷是否需要 Hex轉成 ASCII  */
                    if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                        if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                            tmpAscii = EbcdicConverter.fromHex(CCSID.English,tmpHex);
                        }else{
                            tmpAscii = StringUtil.fromHex(tmpHex);
                        }
                    }

                    k = k + tmplen;

                    /* 晶片卡交易(25XX), 需檢核TAC長度要大於等於10,小於等於130 */
                    if ("25".equals(fiscHeader.getProcessingCode().substring(0, 2))) {
                        if (i == 56) {  /* 交易驗證碼 */
                            if (tmplen < 10 || tmplen > 130) {
                                getLogContext().setRemark("ChkBitmap(TAC) 第" + (i+1 + "位訊息格式錯誤,TAC長度錯誤!"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.CheckBitMapError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                return FISCReturnCode.CheckBitMapError;
                            }
                        }
                    }

                    /* 依資料型態(DATATTR_DATATYPE), 拆解及檢核電文欄位 */
                    switch (dataType) {
                        case "9": /* 資料型態=數字 */
                            if (!PolyfillUtil.isNumeric(tmpAscii)) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "M": /* 資料型態=金額 */
                            if (!PolyfillUtil.isNumeric(tmpAscii) || "+-".indexOf(tmpAscii.substring(0, 1)) < 0) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        // fiscINBK.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscINBK.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case OPC:
                                        // fiscOPC.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscOPC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case CLR:
                                        // fiscCLR.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case FCCLR:
                                        // fiscFCCLR.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscFCCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case EMVIC:
                                        // fiscEMVIC.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscEMVIC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "A": /* 資料型態=日期 */
                            if (CalendarUtil.adStringToADDate(tmpAscii) == null) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "D": /* 資料型態=民國日期 */
                            if (CalendarUtil.rocStringToADDate("0" + tmpAscii) == null) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        default: /* 資料型態=一般文字 */
                            if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                                /* 需要 Hex轉成 ASCII  */
                                if (DbHelper.toBoolean(dvAttr.get(i).getDataattrEncoding())) {
                                    /* 需要中文轉碼, 轉CNS11643 */
                                    switch (getFISCTxData().getFiscTeleType()) {
                                        case INBK:
                                            fiscINBK.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case OPC:
                                            fiscOPC.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case CLR:
                                            fiscCLR.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case FCCLR:
                                            fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case EMVIC:
                                            fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                            break;
                                        default:
                                      	  break;
                                    }
                                } else {
                                    /* DATAATTR_ENCODING = False,不需要中文轉碼 */
                                    switch (getFISCTxData().getFiscTeleType()) {
                                        case INBK:
                                            fiscINBK.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case OPC:
                                            fiscOPC.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case CLR:
                                            fiscCLR.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case FCCLR:
                                            fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case EMVIC:
                                            fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        default:
                                      	  break;
                                    }
                                }
                            } else { /* DATAATTR_TRANSFERFLAG=’N’ */
                                /* 不需要 Hex轉成 ASCII  */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setRemark("拆解第" + (i + "位發生異常,Exception:" + ex.getMessage()));
            getLogContext().setProgramName(ProgramName + ".checkBitmap");
            logMessage(Level.ERROR, getLogContext());
            sendEMS(getLogContext());
            return FISCReturnCode.CheckBitMapError;
        }
    }


    public FEPReturnCode checkBitmapFromCBSForFiscToIMS(String apdata) {
        // 讀取財金電文BITMAP定義
        Bitmapdef oBitMap = getBitmapData(fiscHeader.getMessageType() + fiscHeader.getProcessingCode());
        // 讀取財金電文AP DATA ELEMENT定義
    	List<Dataattr> dvAttr = getDataAttributeDataByType(oBitMap.getBitmapdefType().toString());

        int k = 0;
        FISC_OPC fiscOPC = new FISC_OPC();
        FISC_INBK fiscINBK = new FISC_INBK();
        FISC_CLR fiscCLR = new FISC_CLR();
        FISC_USDCLR fiscFCCLR = new FISC_USDCLR();
        FISC_EMVIC fiscEMVIC = new FISC_EMVIC();
        FEPReturnCode rtnCode = CommonReturnCode.Normal; // add by henny for 紀錄rc
        int i = 0;

        try {
            // 將財金Bitmap Hex轉2進位
            char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(fiscHeader.getBitMapConfiguration(), 16, 2, 64).toCharArray();
            switch (fiscHeader.getMessageType().substring(2, 4)) {
                case "00": /* Request 電文 */
                case "81":
                case "99":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKReq;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCReq;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRReq;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRReq;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICReq;
                            break;
                        default:
                      	  break;
                    }
                    break;
                case "02":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKCon;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCCon;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICCon;
                            break;
                        default:
                      	  break;
                    }
                    break;
                case "10":
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            fiscINBK = fiscINBKRes;
                            break;
                        case OPC:
                            fiscOPC = fiscOPCRes;
                            break;
                        case CLR:
                            fiscCLR = fiscCLRRes;
                            break;
                        case FCCLR:
                            fiscFCCLR = fiscFCCLRRes;
                            break;
                        case EMVIC:
                            fiscEMVIC = fiscEMVICRes;
                            break;
                        default:
                      	  break;
                    }
                    break;
            }
            // 依AP DATA ELEMENT 定義拆解財金電文
            for (i = 0; i < bitMapFromFisc.length; i++) {
                if (bitMapFromFisc[i] == '1') {
                    // Bitmap on 開始處理
                    int tmplen = 0;
                    String tmpHex = "";
                    String tmpAscii = "";
                    String dataType = dvAttr.get(i).getDataattrDatatype().toString();
                    /* 判斷資料型態, 抓取欄位長度 */
                    if (dataType.equalsIgnoreCase("C") || dataType.equalsIgnoreCase("B")) {
                        /* 變動長度欄位 */
                        /* 變動長度從電文取前2個BYTE為此欄位長度L(2) */
                        /* 拆解欄位內容(tmpHex)= DATA */
                        tmplen = Integer.parseInt(StringUtil.convertFromAnyBaseString(apdata.substring(k, k + 4), 16, 10, 0)) * 2;
                        tmpHex = apdata.substring(k + 4, k + 4 + tmplen - 4);
                    } else {
                        /* 固定長度欄位 */
                        tmplen = dvAttr.get(i).getDataattrHexLen().intValue();
                        tmpHex = apdata.substring(k, k + tmplen);
                    }

                    /*  判斷是否需要 Hex轉成 ASCII  */
                    if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                        if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                            tmpAscii = EbcdicConverter.fromHex(CCSID.English,tmpHex);
                        }else{
                            tmpAscii = StringUtil.fromHex(tmpHex);
                        }
                    }

                    k = k + tmplen;

                    /* 晶片卡交易(25XX), 需檢核TAC長度要大於等於10,小於等於130 */
                    if ("25".equals(fiscHeader.getProcessingCode().substring(0, 2))) {
                        if (i == 56) {  /* 交易驗證碼 */
                            if (tmplen < 10 || tmplen > 130) {
                                getLogContext().setRemark("ChkBitmap(TAC) 第" + (i+1 + "位訊息格式錯誤,TAC長度錯誤!"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.CheckBitMapError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                return FISCReturnCode.CheckBitMapError;
                            }
                        }
                    }

                    /* 依資料型態(DATATTR_DATATYPE), 拆解及檢核電文欄位 */
                    switch (dataType) {
                        case "9": /* 資料型態=數字 */
                            if (!PolyfillUtil.isNumeric(tmpAscii)) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "M": /* 資料型態=金額 */
                            if (!PolyfillUtil.isNumeric(tmpAscii) || "+-".indexOf(tmpAscii.substring(0, 1)) < 0) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        // fiscINBK.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscINBK.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case OPC:
                                        // fiscOPC.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscOPC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case CLR:
                                        // fiscCLR.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case FCCLR:
                                        // fiscFCCLR.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscFCCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    case EMVIC:
                                        // fiscEMVIC.setGetPropertyValue(i, String.valueOf(Double.parseDouble(tmpAscii) / 100));
                                        // 2021-09-30 Richard modified
                                        // 改用BigDecimal來處理, 確保精度一致性
                                        fiscEMVIC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "A": /* 資料型態=日期 */
                            if (CalendarUtil.adStringToADDate(tmpAscii) == null) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        case "D": /* 資料型態=民國日期 */
                            if (CalendarUtil.rocStringToADDate("0" + tmpAscii) == null) {
                                getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                        default: /* 資料型態=一般文字 */
                            if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                                /* 需要 Hex轉成 ASCII  */
                                if (DbHelper.toBoolean(dvAttr.get(i).getDataattrEncoding())) {
                                    /* 需要中文轉碼, 轉CNS11643 */
                                    switch (getFISCTxData().getFiscTeleType()) {
                                        case INBK:
                                            fiscINBK.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case OPC:
                                            fiscOPC.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case CLR:
                                            fiscCLR.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case FCCLR:
                                            fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                            break;
                                        case EMVIC:
                                            fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                            break;
                                        default:
                                      	  break;
                                    }
                                } else {
                                    /* DATAATTR_ENCODING = False,不需要中文轉碼 */
                                    switch (getFISCTxData().getFiscTeleType()) {
                                        case INBK:
                                            fiscINBK.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case OPC:
                                            fiscOPC.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case CLR:
                                            fiscCLR.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case FCCLR:
                                            fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        case EMVIC:
                                            fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                            break;
                                        default:
                                      	  break;
                                    }
                                }
                            } else { /* DATAATTR_TRANSFERFLAG=’N’ */
                                /* 不需要 Hex轉成 ASCII  */
                                switch (getFISCTxData().getFiscTeleType()) {
                                    case INBK:
                                        fiscINBK.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case OPC:
                                        fiscOPC.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case CLR:
                                        fiscCLR.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case FCCLR:
                                        fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                        break;
                                    case EMVIC:
                                        fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                        break;
                                    default:
                                  	  break;
                                }
                            }
                            break;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setRemark("拆解第" + (i + "位發生異常,Exception:" + ex.getMessage()));
            getLogContext().setProgramName(ProgramName + ".checkBitmap");
            logMessage(Level.ERROR, getLogContext());
            sendEMS(getLogContext());
            return FISCReturnCode.CheckBitMapError;
        }
    }

    /**
     * SendGarbledMessage
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * <modify>
     * <modifier>Kyo</modifier>
     * <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
     * <date>2010/03/16</date>
     * </modify>
     * </history>
     */
    public boolean sendGarbledMessage(Integer ejfno, FEPReturnCode rc, FISCHeader mfiscHeader) {
        Feptxn m_Feptxn = new FeptxnExt();
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        FISC_OPC fiscOPCReqGbl = new FISC_OPC();
        try {
            m_Feptxn.setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            m_Feptxn.setFeptxnEjfno(TxHelper.generateEj());
            m_Feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
            m_Feptxn.setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
            m_Feptxn.setFeptxnDesBkno(SysStatus.getPropertyValue().getSysstatFbkno()); // 950
            m_Feptxn.setFeptxnStan(getStan());
            m_Feptxn.setFeptxnTxTime(new SimpleDateFormat("HHmmss").format(new Date()));
            m_Feptxn.setFeptxnPcode("3113");
            // add by Maxine on 2011/10/19 for 10/18 寫入 getFeptxnTbsdyFisc()
            m_Feptxn.setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
            m_Feptxn.setFeptxnFiscFlag(DbHelper.toShort(true)); // 跨行記號 */

            m_Feptxn.setFeptxnTraceEjfno((ejfno == null ? 0 : ejfno));
            // modified by Maxine on 2011/07/13 for 增加傳入LogContext
            getLogContext().setProgramName(ProgramName);
            m_Feptxn.setFeptxnReqRc(TxHelper.getRCFromErrorCode(rc, FEPChannel.FISC, getLogContext()));
            // m_Feptxn.getFeptxnReqRc() = TxHelper.getRCFromErrorCode(rc, FEPChannel.FISC)

            // 2024-06-13 Richard modified start
            if (FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic) {
                m_Feptxn.setFeptxnRemark(EbcdicConverter.fromHex(CCSID.English, mfiscHeader.getFISCMessage().substring(6, 96)) + mfiscHeader.getFISCMessage().substring(96, 104));
            } else {
                m_Feptxn.setFeptxnRemark(StringUtil.fromHex(mfiscHeader.getFISCMessage().substring(6, 96)) + mfiscHeader.getFISCMessage().substring(96, 104));
            }
            if (m_Feptxn.getFeptxnRemark().length() > 55) {
                m_Feptxn.setFeptxnRemark(StringUtils.substring(m_Feptxn.getFeptxnRemark(), 0, 55));
            }
            // 2024-06-13 Richard modified end

            m_Feptxn.setFeptxnSubsys((short) 1);
            this.feptxnDao.insertSelective(m_Feptxn);

            fiscOPCReqGbl.setSystemSupervisoryControlHeader("00"); // 24個Bit‘0’
            fiscOPCReqGbl.setSystemNetworkIdentifier("00");
            fiscOPCReqGbl.setAdderssControlField("00");
            fiscOPCReqGbl.setMessageType("0699");
            fiscOPCReqGbl.setProcessingCode(m_Feptxn.getFeptxnPcode());
            fiscOPCReqGbl.setSystemTraceAuditNo(m_Feptxn.getFeptxnStan());
            fiscOPCReqGbl.setTxnDestinationInstituteId(StringUtils.rightPad(m_Feptxn.getFeptxnDesBkno(), 7, '0'));
            fiscOPCReqGbl.setTxnSourceInstituteId(StringUtils.rightPad(m_Feptxn.getFeptxnBkno(), 7, '0'));
            fiscOPCReqGbl.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(m_Feptxn.getFeptxnTxDate()).substring(1, 7) + m_Feptxn.getFeptxnTxTime()); // 轉成民國年
            fiscOPCReqGbl.setResponseCode(m_Feptxn.getFeptxnReqRc());
            fiscOPCReqGbl.setSyncCheckItem("00000000"); // 32個 Bit‘0’
            fiscOPCReqGbl.setBasicHeader(mfiscHeader.getFISCMessage().substring(6, 104));
            fiscOPCReq = fiscOPCReqGbl;
            getLogContext().setRemark("SendGarbledMessage " + m_Feptxn.getFeptxnRemark());
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());

            FEPReturnCode rtnCode;
            rtnCode = makeBitmap(fiscOPCReq.getMessageType(), fiscOPCReq.getProcessingCode(), MessageFlow.None);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return false;
            }

            fiscOPCReq.makeFISCMsg();
            // 將財金電文轉成 String (GarbledMessage)
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFISCTxData().getStan());
            fiscAdapter.setMessageToFISC(fiscOPCReq.getFISCMessage());
            // .Timeout = FISCTxData.FISCTimeout

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendGarbledMessage");
            sendEMS(getLogContext());
            return false;
        }
    }

    /**
     * 產生財金 Confirm 電文並送出
     *
     * @return 送電文至FISC
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * <modifier>HusanYin</modifier>
     * <reason>for confirmFISC used 傳入所需fiscMsg</reason>
     * <date>2010/8/13</date>
     * <modifier>HusanYin</modifier>
     * <reason>connie spec change
     * 刪除欄位
     * 1. FEPTXN_REQ_TIME
     * 2. FEPTXN_REP_TIME
     * 3. FEPTXN_CON_TIME
     * </reason>
     * <date>2010/8/13</date>
     * <reason>*因 ATM 壓 TAC已右補0*,所以組財經電文要自己補滿八位</reason>
     * <date>2011/01/07</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendConfirmToFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        try {
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // FISC Confirm
            int i = this.feptxnDao.updateByPrimaryKeySelective(getFeptxn()); //檔名SEQ為 FEPTXN_TBSDY_FISC(7:2)
            if (i < 1) {
                return FEPReturnCode.FEPTXNUpdateError;
            }

            rtnCode = prepareHeader("0202");
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            if (!"24".equals(getFeptxn().getFeptxnPcode().substring(0, 2))) { // 非國際卡{
                //for 跨行提領外幣
                if (ATMTXCD.US.name().equals(getFeptxn().getFeptxnTxCode().trim())
                        || ATMTXCD.JP.name().equals(getFeptxn().getFeptxnTxCode().trim())) {
                    fiscINBKCon.setTxAmt(getFeptxn().getFeptxnTxAmtAct().toString());
                } else if (!"2566".equals(getFeptxn().getFeptxnPcode())) { // 2566 Confirm 電文無交易金額
                    fiscINBKCon.setTxAmt(getFeptxn().getFeptxnTxAmt().toString());
                }
            }

            // for 整批轉即時(2262)
            // for 繳汽燃費
            if (!"226".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
                fiscINBKCon.setATMNO(StringUtils.rightPad(getFeptxn().getFeptxnAtmno(), 8, '0')); // 右補0補滿8位(因ATM壓TAC需右補0)
            }

            RefString refMac = new RefString(fiscINBKCon.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscINBKCon.getMessageType(), refMac);
            fiscINBKCon.setMAC(refMac.get());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            rtnCode = makeBitmap(fiscINBKCon.getMessageType(), fiscINBKCon.getProcessingCode(), MessageFlow.Confirmation);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            fiscINBKCon.makeFISCMsg();
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
            fiscAdapter.setMessageToFISC(fiscINBKCon.getFISCMessage());
            // .Timeout = FISCTxData.FISCTimeout

            rtnCode = fiscAdapter.sendReceive();

            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendConfirmToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // add by Maxine on 2012/03/01 for 讀取 BIN 方式由 Cache 改為讀 DB
    private BinExt getBin(String no, String bkno) {
        return new BinExt(binMapper.selectByPrimaryKey(no, bkno));
    }

    @Override
    public FEPReturnCode checkDigit(String actno) {
        String strWeight = "212121212121212";
        int ModValue = 10;
        int checkSum = 0;
        String checkNo = "";
        String TempStr = "";
        int i = 0;
        try {
            if (!PolyfillUtil.isNumeric(actno) || actno.length() != 16) {
                return FEPReturnCode.TranInACTNOError; // 轉入帳號錯誤 TranInACTNOError
            }

            for (i = 0; i < strWeight.length(); i++) {
                TempStr = (Double.parseDouble(actno.substring(i, i + 1)) * Double.parseDouble(strWeight.substring(i, i + 1))) + "";
                if (TempStr.length() > 1) {
                    TempStr = (Double.parseDouble(TempStr.substring(0, 1)) + Double.parseDouble(TempStr.substring(1, 2))) + "";
                }
                checkSum += Double.parseDouble(TempStr);
            }
            checkNo = (ModValue - (checkSum % ModValue)) + "";
            // modify by husan checkNo取最右邊一位
            if (!actno.substring(actno.length() - 1).equals(checkNo.substring(checkNo.length() - 1))) {
                getLogContext().setRemark("信用卡號檢查碼錯誤, 正確 = " + checkNo.substring(checkNo.length() - 1));
                this.logMessage(getLogContext());
                return FEPReturnCode.TranInACTNOError; /// * 轉入帳號錯誤 TranInACTNOError */
            }

            return CommonReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkDigit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 轉入交易檢核虛擬帳號
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>Function</reason>
     * <date>2012/07/31</date>
     * </modify>
     * </history>
     */
    @Override
    public FEPReturnCode checkVirActno() {
        try {
            // 檢核16位轉入帳號是否為虛擬帳號(前二碼<>'00')
            if (getFeptxn().getFeptxnTrinActno().trim().length() < 16) {
                return FEPReturnCode.TranInACTNOError; // 轉入帳號不存在(E074)
            }

            // 判斷轉入帳號第一碼
            switch (getFeptxn().getFeptxnTrinActno().substring(0, 1)) {
                case "0":
                    if (!"3".equals(getFeptxn().getFeptxnTrinActno().substring(1, 2))) {
                        return FEPReturnCode.TranInACTNOError;
                    } else {
                        // 檢核是否為本行AE卡BIN
                        if (!"C".equals(getFeptxn().getFeptxnTrinKind())) {
                            return FEPReturnCode.TranInACTNOError;
                        }
                    }
                    break;
                case "1":
                    return FEPReturnCode.TranInACTNOError;
                case "2":
                    // 轉入帳號第二、三碼必須為 87
                    if (!"87".equals(getFeptxn().getFeptxnTrinActno().substring(1, 3))) {
                        return FEPReturnCode.TranInACTNOError;
                    }
                    break;
                case "3":
                case "4":
                case "5":
                case "6":
                    if (StringUtils.isBlank(getFeptxn().getFeptxnTrinKind())) {
                        return FEPReturnCode.TranInACTNOError;
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

    /**
     * 檢核是否為 MERCHANT ID
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkMerchant() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            String merchantId = null;
            if (StringUtils.isNotBlank(getFeptxn().getFeptxnMerchantId()) && getFeptxn().getFeptxnMerchantId().length() >= 15) {
                merchantId = getFeptxn().getFeptxnMerchantId().substring(0, 15);
            } else {
                merchantId = getFeptxn().getFeptxnMerchantId();
            }
            Merchant result = merchantMapper.selectByPrimaryKey(merchantId);
            if (result == null) {
                return FISCReturnCode.MerchentIDNotFound;
            }
            getFISCTxData().setMerchant(result);
            return rtnCode;
        } catch (Exception ex) {
            if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkMerchant"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 負責檢核EMV卡財金跨行交易回應電文
     *
     * @return <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <reason>EMV</reason>
     * <date>2016/01/20</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkEMVResponseMessage() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        String sFiscRc = null;
        String wk_TX_DATE = null;
        BigDecimal wk_TX_AMT = new BigDecimal(0);

        try {
            rtnCode = checkHeader(fiscEMVICRes, true);
            getLogContext().setProgramName(ProgramName);
            sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
            if ("10".equals(sFiscRc.substring(0, 2))) {
                sendGarbledMessage(fiscEMVICReq.getEj(), rtnCode, fiscEMVICRes);
                getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                return rtnCode;
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFeptxn().setFeptxnRepRc(fiscEMVICRes.getResponseCode());
            //授權碼
            if (!StringUtils.isBlank(fiscEMVICRes.getAuthCode())) {
                getFeptxn().setFeptxnAuthcd(fiscEMVICRes.getAuthCode());
            }

            //更新餘額
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) { /* 檢核財金電文 Header 正常*/
                if (!NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc()) && !"4007".equals(getFeptxn().getFeptxnRepRc())) {  /*失敗*/
                    return rtnCode;
                } else {
                    if (StringUtils.isNotBlank(fiscEMVICRes.getBALA())) {
                        //getFeptxn().setFeptxnBala(new BigDecimal(fiscEMVICRes.getBALA())); /* 存戶可用餘額 */
                        getFeptxn().setFeptxnBala(new BigDecimal(fiscEMVICRes.getBALA())); /* 存戶可用餘額 */
                    }
                    if (StringUtils.isNotBlank(fiscEMVICRes.getBALB())) {
                        getFeptxn().setFeptxnBalb(new BigDecimal(fiscEMVICRes.getBALB())); /* 帳戶餘額 */
                    }
                }
            } else {
                return rtnCode;
            }

            rtnCode = updateTxData();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            // 設初值避免在「檢核MAPPING(欄位)」的邏輯會出現EXCEPTION
            if (StringUtils.isBlank(fiscEMVICRes.getTxAmt())) {
                fiscEMVICRes.setTxAmt("0");
            }

            // 檢核MAPPING(欄位)
            wk_TX_DATE = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscEMVICRes.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')); // 必須先轉為西元年
            /// *因為送財金之 ATMNO 已右補0, 故不再判斷ATMNO 是否相同*/
            if (!wk_TX_DATE.equals(getFeptxn().getFeptxnReqDatetime().substring(0, 8))|| !fiscEMVICRes.getTxnInitiateDateAndTime().substring(6, 12).equals(getFeptxn().getFeptxnReqDatetime().substring(8, 14))
                    || (Double.valueOf(fiscEMVICRes.getTxAmt()) != 0 && !(new BigDecimal(fiscEMVICRes.getTxAmt()).equals(wk_TX_AMT)))) {
                getLogContext().setRemark("檢核MAPPING(欄位)不合：TX_DATE=" + getFeptxn().getFeptxnTxDate() + ", FiscRep.TxDate=" + wk_TX_DATE + ", TX_TIME=" + getFeptxn().getFeptxnTxTime()
                        + ", FiscRep.TxTime=" + fiscEMVICRes.getTxnInitiateDateAndTime().substring(6, 12) + ", TX_AMT=" + wk_TX_AMT.toString() + ", FiscRep.TxAMT" + fiscEMVICRes.getTxAmt());
                this.logMessage(getLogContext());
                rtnCode = FISCReturnCode.OriginalMessageDataError;
                return rtnCode;
            }

            // CALL DES 驗證 MAC
            rtnCode = encHelper.checkFiscMac(fiscEMVICRes.getMessageType(), fiscEMVICRes.getMAC());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkEMVResponseMessage");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 送交易電文給FISC
     *
     * @return 送電文至FISC
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Ashiang</modifier>
     * <reason></reason>
     * <date>2009/12/01</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendToFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        @SuppressWarnings("unused")
        String result = "";
        try {
            // 更新本筆相關FEPTXN
            updateFEPTxn();

            // 更新本筆相關INTLTXN
            if (StringUtils.isNotBlank(fiscINBKReq.getOriData())) {
                updateINTLTXN();
            }

            // 判斷是否需更新原始交易
            if (StringUtils.isNotBlank(fiscINBKReq.getOriStan())) {
                // 更新原始交易的FEPTxn
                updateFEPTxn(getOriginalFEPTxn());

                // 國際卡交易需同時更新 INTLTXN

            }

            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFISCTxData().getStan());
            fiscAdapter.setMessageToFISC(getFISCTxData().getTxObject().getINBKResponse().getFISCMessage());
            // .Timeout = FISCTxData.FISCTimeout

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                // 如果需要接收財金回應電文則在此處理
                // fiscINBKRes = fiscAdapter.INBKFromFISC

            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * <history>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>MSGCTL Schema修改 MSGCTL_2WAY變MSGCTL_FISC_2WAY</reason>
     * <date>2010/10/07</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>刪除FEPTXN_REP_TIME</reason>
     * <date>2010/11/08</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>刪除FEPTXN_RETCARD</reason>
     * <date>2010/11/09</date>
     * </modify>
     * </history>
     */
    private FEPReturnCode updateFEPTxn() {
        return updateFEPTxn(null);
    }

    private FEPReturnCode updateFEPTxn(Feptxn originalFeptxn) {
        Feptxn uFepTx = null;
        byte wk_TXRUST = 0;
        byte wk_PENDING = 0;

        if (originalFeptxn == null) {
            // Update本筆交易
            uFepTx = getFeptxn();
            if (!DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                if (fiscINBKRes.getResponseCode().equals(BusinessBase.FISCRCNormal)) {
                    wk_TXRUST = 1;
                    wk_PENDING = 1;
                } else {
                    wk_PENDING = 0;
                }
            } else {
                wk_PENDING = 0;
                if (fiscINBKRes.getResponseCode().equals(BusinessBase.FISCRCNormal)) {
                    wk_TXRUST = 1;
                }
            }
            uFepTx.setFeptxnRepRc(StringUtils.leftPad(fiscINBKRes.getResponseCode(), 4, '0'));
            uFepTx.setFeptxnAaRc(Integer.parseInt(fiscINBKRes.getResponseCode()));
            uFepTx.setFeptxnPending((short) wk_PENDING);
            uFepTx.setFeptxnTxrust(String.valueOf(wk_TXRUST));
        } else {
            // Update原始交易
            uFepTx = originalFeptxn;
        }

        try {
            int i = this.feptxnDao.updateByPrimaryKey(uFepTx);
            if (i > 0) {
                return CommonReturnCode.Normal;
            } else {
                return FEPReturnCode.FEPTXNUpdateError;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            return FEPReturnCode.FEPTXNUpdateError;
        }
    }

//    public FEPReturnCode prepareInbk2160() {
//        try {
//            FEPReturnCode rtnCode = CommonReturnCode.Normal;
//            // FEPTXN欄位, 寫入INBK2160
//            Inbk2160 inbk2160 = new Inbk2160();
//            inbk2160.setInbk2160TxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)); /* 交易日期 */
//            inbk2160.setInbk2160Ejfno(getEj()); /*電子日誌序號*/
//            inbk2160.setInbk2160Bkno(SysStatus.getPropertyValue().getSysstatHbkno()); /*交易啟動銀行*/
//            inbk2160.setInbk2160TxTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)); /*交易時間*/
//            inbk2160.setInbk2160Atmno(getFeptxn().getFeptxnAtmno()); /* 端末機代號 */
//            inbk2160.setInbk2160MajorActno(getFeptxn().getFeptxnMajorActno());
//            inbk2160.setInbk2160TroutBkno(getFeptxn().getFeptxnTroutBkno());
//            inbk2160.setInbk2160TroutActno(getFeptxn().getFeptxnTroutActno());
//            inbk2160.setInbk2160OriTrinBkno(getFeptxn().getFeptxnTrinBkno());
//            inbk2160.setInbk2160TxAmt(getFeptxn().getFeptxnTxAmt());
//            inbk2160.setInbk2160Stan(getStan());
//            inbk2160.setInbk2160FiscFlag(getFeptxn().getFeptxnFiscFlag());
//            inbk2160.setInbk2160ReqRc("0000");
//            inbk2160.setInbk2160Subsys((short) 1);
//            inbk2160.setInbk2160Pcode(FISCPCode.PCode2160.getValueStr());
//            inbk2160.setInbk2160Twmp("001");
//            inbk2160.setInbk2160Chrem(getFeptxn().getFeptxnChrem());
//            inbk2160.setInbk2160OriPcode(getFeptxn().getFeptxnPcode());
//            inbk2160.setInbk2160OriBkno(getFeptxn().getFeptxnBkno());
//            inbk2160.setInbk2160OriStan(getFeptxn().getFeptxnStan());
//            inbk2160.setInbk2160OriTxDate(getFeptxn().getFeptxnTxDate());
//            inbk2160.setInbk2160OriTxTime(getFeptxn().getFeptxnTxTime());
//            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTroutBkno7())) {
//                inbk2160.setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno7());
//            } else {
//                inbk2160.setInbk2160OriTroutBkno7(getFeptxn().getFeptxnTroutBkno() + "0000");
//            }
//            inbk2160.setInbk2160OriIcSeqno(getFeptxn().getFeptxnIcSeqno());
//            if (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
//                inbk2160.setInbk2160OriIcdata("1001");
//            } else {
//                inbk2160.setInbk2160OriIcdata("0000");
//            }
//            inbk2160.setInbk2160OriRepRc(getFeptxn().getFeptxnRepRc());
//            if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno7())) {
//                inbk2160.setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno7());
//            } else {
//                if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrinBkno())) {
//                    inbk2160.setInbk2160OriTrinBkno7(getFeptxn().getFeptxnTrinBkno() + "0000");
//                }
//            }
//            inbk2160.setInbk2160OriTrinActno(getFeptxn().getFeptxnTrinActno());
//            inbk2160.setInbk2160OriAtmType(getFeptxn().getFeptxnAtmType());
//            inbk2160.setInbk2160OriFeeCustpay(getFeptxn().getFeptxnFeeCustpay());
//            inbk2160.setInbk2160OriMerchantId(getFeptxn().getFeptxnOrderNo());
//            inbk2160.setInbk2160OriOrderNo(getFeptxn().getFeptxnOrderNo());
//            /* 2022/10/31 增加手機條碼 */
//            if (StringUtils.isNotBlank(getFeptxn().getFeptxnLuckyno())
//                    && "/".equals(getFeptxn().getFeptxnLuckyno().substring(0, 1))
//                    && (FISCPCode.PCode2541.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2542.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2543.getValueStr().equals(getFeptxn().getFeptxnPcode()))) {
//                inbk2160.setInbk2160OriBarcode(getFeptxn().getFeptxnLuckyno().substring(0, 8));
//            }
//            if (!"TWD".equals(getFeptxn().getFeptxnTxCur())) {
//                inbk2160.setInbk2160OriTxnAmtCur(getFeptxn().getFeptxnTxAmt());
//                inbk2160.setInbk2160OriTxCur(getFeptxn().getFeptxnTxCur());
//            }
//            inbk2160.setInbk2160OriEjfno(getFeptxn().getFeptxnEjfno());
//            inbk2160.setInbk2160OriTxCode(getFeptxn().getFeptxnTxCode());
//            inbk2160.setInbk2160OriTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
//            inbk2160.setInbk2160OriReqRc(getFeptxn().getFeptxnReqRc());
//            inbk2160.setInbk2160DesBkno("9430000"); /* 台灣PAY */
//            this.insertINBK2160(inbk2160);
//
//            return rtnCode;
//        } catch (Exception ex) {
//            getLogContext().setProgramException(ex);
//            sendEMS(getLogContext());
//            return FEPReturnCode.ProgramException;
//        }
//    }

    public FEPReturnCode insertINBK2160(Inbk2160 inbk2160) {
        try {
            int insertINBK2160 = inbk2160Mapper.insertSelective(inbk2160);
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

    /**
     * 更新國際卡檔
     *
     * @param intlTxn
     * @return
     */
    public FEPReturnCode updateINTLTxn(Intltxn intlTxn) {
        return updateINTLTxn(intlTxn, null);
    }

    public FEPReturnCode updateINTLTxn(Intltxn intlTxn, String db) { // DBHelper db){
        return null;
    }

    public FEPReturnCode updateINTLTXN() {
        Intltxn intlTxn = new Intltxn();
        byte wk_TXRUST = 0;

        intlTxn.setIntltxnRepRc(fiscINBKRes.getResponseCode());
        if (fiscINBKRes.getResponseCode().equals(BusinessBase.FISCRCNormal)) {
            wk_TXRUST = 1;
        } else {
            wk_TXRUST = 0;
        }
        intlTxn.setIntltxnTxrust(String.valueOf(wk_TXRUST));
        try {
            int i = intltxnExtMapper.updateByPrimaryKey(intlTxn);
            if (i > 0) {
                return CommonReturnCode.Normal;
            } else {
                return FEPReturnCode.FEPTXNUpdateError;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateINTLTXN");
            sendEMS(getLogContext());
            return FEPReturnCode.FEPTXNUpdateError;
        }

    }

    /**
     * 送交易電文給FISC
     *
     * @param msgFunction 送給FISC的電文種類Request Or Response
     *
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Henny</modifier>
     *                    <reason></reason>
     *                    <date>2010/4/13</date>
     *                    </modify>
     *                    </history>
     * @return 送電文至FISC
     */
    public FEPReturnCode sendMessageToFISC(MessageFlow msgFunction) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        String MessageToFISC = "";
        String MessageType = "";
        String Pcode = "";
        Bitmapdef oBitMap = new Bitmapdef();

        try {
            switch (msgFunction) {
                case Request:
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            MessageToFISC = fiscINBKReq.getFISCMessage();
                            MessageType = fiscINBKReq.getMessageType();
                            Pcode = fiscINBKReq.getProcessingCode();
                            break;
                        case CLR:
                            MessageToFISC = fiscCLRReq.getFISCMessage();
                            MessageType = fiscCLRReq.getMessageType();
                            Pcode = fiscCLRReq.getProcessingCode();
                            break;
                        case FCCLR:
                            MessageToFISC = fiscFCCLRReq.getFISCMessage();
                            MessageType = fiscFCCLRReq.getMessageType();
                            Pcode = fiscFCCLRReq.getProcessingCode();
                            break;
                        default:
                            break;
                    }
                    break;
                case Response:
                    switch (getFISCTxData().getFiscTeleType()) {
                        case INBK:
                            MessageToFISC = fiscINBKRes.getFISCMessage();
                            MessageType = fiscINBKRes.getMessageType();
                            Pcode = fiscINBKRes.getProcessingCode();
                            break;
                        case EMVIC:
                            MessageToFISC = fiscEMVICRes.getFISCMessage();
                            MessageType = fiscEMVICRes.getMessageType();
                            Pcode = fiscEMVICRes.getProcessingCode();
                            break;
                        case CLR:
                            MessageToFISC = fiscCLRRes.getFISCMessage();
                            MessageType = fiscCLRRes.getMessageType();
                            Pcode = fiscCLRRes.getProcessingCode();
                            break;
                        case FCCLR:
                            MessageToFISC = fiscFCCLRRes.getFISCMessage();
                            MessageType = fiscFCCLRRes.getMessageType();
                            Pcode = fiscFCCLRRes.getProcessingCode();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
            oBitMap = getBitmapData(MessageType + Pcode);
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());

            // 2012/04/25 Modify by Ruling for Pending 交易回財金RESPONSE由LogContext.STAN傳入
            // 2012/06/05 Modify by Ruling for Pending 交易回財金RESPONSE由LogContext.STAN傳入for PCODE=2270
            if (getFeptxn() == null) {
                fiscAdapter.setStan(getLogContext().getStan()); // FISCTxData.Stan
            } else {
                if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
                    fiscAdapter.setStan(getLogContext().getStan()); // FISCTxData.Stan
                } else {
                    fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
                }
            }

            fiscAdapter.setMessageToFISC(MessageToFISC);
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());
            rtnCode = fiscAdapter.sendReceive();

            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                if (StringUtils.isBlank(fiscAdapter.getMessageFromFISC())) {
                    if (oBitMap.getBitmapdefTimeout() == 0) {
                        rtnCode = CommonReturnCode.Normal;
                    } else {
                        rtnCode = FISCReturnCode.FISCTimeout;
                    }
                } else {
                    switch (msgFunction) {
                        case Request:
                            switch (getFISCTxData().getFiscTeleType()) {
                                case INBK:
                                    fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                    fiscINBKRes.parseFISCMsg();
                                    break;
                                case CLR:
                                    fiscCLRRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                    fiscCLRRes.parseFISCMsg();
                                    break;
                                case FCCLR:
                                    fiscFCCLRRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                    fiscFCCLRRes.parseFISCMsg();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Response:
                            switch (getFISCTxData().getFiscTeleType()) {
                                case INBK:
                                    fiscINBKCon.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                    fiscINBKCon.parseFISCMsg();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // F2
                    getFeptxn().setFeptxnPending((short) 2); // 解除 PENDING
                    getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
                    getFeptxn().setFeptxnAaRc(CommonReturnCode.Normal.getValue());
                }
            } else {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendMessageToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 產生財金EMV Confirm 電文並送出
     *
     * @return 送電文至FISC
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <reason>EMV</reason>
     * <date>2016/01/22</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendConfirmToFISCEMV() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        try {
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); // FISC Confirm

            int i = this.feptxnDao.updateByPrimaryKeySelective(getFeptxn());
            if (i < 1) {
                return FEPReturnCode.FEPTXNUpdateError;
            }

        } catch (Exception ex) {
            return FEPReturnCode.FEPTXNUpdateError;
        }

        try {
            rtnCode = prepareHeader("0202");
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            fiscEMVICCon.setATMNO(StringUtils.rightPad(getFeptxn().getFeptxnAtmno(), 8, '0')); // 右補0補滿8位(因ATM壓TAC需右補0)

            RefString refMac = new RefString(fiscEMVICCon.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscEMVICCon.getMessageType(), refMac);
            fiscEMVICCon.setMAC(refMac.get());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            // Fly 2016/09/02 修改 for EMV VISA卡(2620&2622) 卡片驗證失敗(Con RC:8120)
            // Fly 2016/10/18 先檢核ATM是否有送IC_CHECKRESULT
            if ((getFeptxn().getFeptxnPcode().equals("2620") || getFeptxn().getFeptxnPcode().equals("2622")) && getFeptxn().getFeptxnConRc().equals("8120")
                    && StringUtils.isNotBlank(getFeptxn().getFeptxnTrk3())) {
                fiscEMVICCon.setIcCheckresult(getFeptxn().getFeptxnTrk3());
            }

            rtnCode = makeBitmap(fiscEMVICCon.getMessageType(), fiscEMVICCon.getProcessingCode(), MessageFlow.Confirmation);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            fiscEMVICCon.makeFISCMsg();
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan());
            fiscAdapter.setMessageToFISC(fiscEMVICCon.getFISCMessage());

            rtnCode = fiscAdapter.sendReceive();

            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendConfirmToFISCEMV");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 產生EMV晶片卡 REQUEST 電文並送至財金並等待回應
     *
     * @param ATMReq ATM.Request電文
     *
     *
     *               <history>
     *               <modify>
     *               <modifier>Fly</modifier>
     *               <reason>For EMV</reason>
     *               <date>2015/12/25</date>
     *               </modify>
     *               </history>
     * @return FEPReturnCode
     */
    public FEPReturnCode sendEMVRequestToFISC(ATMGeneralRequest ATMReq) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        String wk_BITMAP = null;
        ENCHelper encHelper = new ENCHelper(getFeptxn(), getFISCTxData());
        Bitmapdef oBitMap = null;
        try {
            /* 判斷交易接收銀行 */
            if (StringUtils.isNotBlank(getFISCTxData().getMsgCtl().getMsgctlVirBkno())) {
                getFeptxn().setFeptxnDesBkno(getFISCTxData().getMsgCtl().getMsgctlVirBkno().substring(0, 3)); /*虛擬財金代號 */
            }
            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());

            /* 判斷是否檢核換日 */
            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlChkChgday())) {
                Zone twnZone = getZoneByZoneCode(ZoneCode.TWN);
                if (twnZone == null) {
                    return IOReturnCode.ZONENotFound;
                }
                if (DbHelper.toBoolean(twnZone.getZoneChgday())) {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC_ChangeDate);
                } else {
                    getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
                }
            } else {
                getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            }

            if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
                getFeptxn().setFeptxnStan(getStan());
            }

            //3.產生 REQUEST 電文訊息如下
            //(1)產生財金電文Header (ReqHEAD)
            rtnCode = prepareHeader("0200");
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            //(2)產生財金電文Body (ReqEMV)
            wk_BITMAP = getFISCTxData().getMsgCtl().getMsgctlBitmap1();

            /* BIT MAP 位置 */
            // 依據wk_BITMAP判斷是否搬值，組FIS REQUEST BODY
            for (int i = 2; i <= 63; i++) {
                if (wk_BITMAP.substring(i, i + 1).equals("1")) {
                    /*依據wk_BITMAP 判斷是否搬值*/
                    /*依照 ‘財金電文整理_Request_201.xls’ 搬值 (參考FEPTXN MAPPING 欄位)*/
                    /*若搬入值為空白(文字欄位) 或是 0(數字欄位) 則 Return RC=CheckFieldError*/
                    switch (i) {
                        case 3:  /* TRK2 磁軌資料內容 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTrk2())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_TRK2 is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscEMVICReq.setTRK2(getFeptxn().getFeptxnTrk2().substring(0, 37));
                            break;
                        case 4:  /* 客戶密碼 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnPinblock())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_PINBLOCK is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }

                            fiscEMVICReq.setPINBLOCK(getFeptxn().getFeptxnPinblock());
                            break;
                        case 5: /* 代付單位 CD/ATM 代號 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmno())) {
                                getLogContext().setRemark("MessageFormatError:setFeptxnAtmno() is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscEMVICReq.setATMNO(StringUtils.rightPad(getFeptxn().getFeptxnAtmno(), 8, '0'));
                            break;
                        case 7: /* 交易金額 */
                            if (getFeptxn().getFeptxnTxAmt().doubleValue() == 0) {
                                getLogContext().setRemark("MessageFormatError:getFeptxnTxAmt() = 0 ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscEMVICReq.setTxAmt((getFeptxn().getFeptxnTxAmt().multiply(new BigDecimal("100"))).toString());
                            break;
                        case 8: /* 幣別 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnTxCur())) {
                                getLogContext().setRemark("MessageFormatError:setFeptxnTxCur() is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            fiscEMVICReq.setCURRENCY("901");
                            break;
                        case 14: /* 帳戶類別 */
                            fiscEMVICReq.setActType(getFeptxntcb().getFeptxntcbFacode());
                            break;
                        case 23: /* Network Code */
                            /* 國內為代理單位時, 此欄位以零填補 */
                            fiscEMVICReq.setNetworkCode("00");
                            break;
                        case 27: /* 交易處理費 */
                            if (StringUtils.isBlank(getFeptxn().getFeptxnFeeCustpay().toString())) {
                                getLogContext().setRemark("MessageFormatError:FEPTXN_FEE_CUSTPAY is null or WhiteSpace ");
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            /* 代理行加收交易處理費, 台幣金額8位, 含2位小數 */
                            fiscEMVICReq.setProcFee("D" + StringUtils.leftPad(String.valueOf(getFeptxn().getFeptxnFeeCustpay().longValue() * 100), 8, '0'));
                            break;
                        case 35: /* Original Data */
                        	fiscEMVICReq.setOriData(StringUtils.repeat("0", 140));
                        	// POS ENTRY MODE  
                            if (StringUtils.isBlank(getFeptxntcb().getFeptxntcbPiposent())) {
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            // 端末設備型態
                            if (StringUtils.isBlank(getFeptxn().getFeptxnAtmType())) {
                                rtnCode = FISCReturnCode.MessageFormatError;
                                return FISCReturnCode.MessageFormatError;
                            }
                            StringBuilder ori = new StringBuilder(fiscEMVICReq.getOriData());
                            // POST ENTRY MODE
                            ori.replace(29, 33, getFeptxntcb().getFeptxntcbPiposent());
                            // 端末設備型態
                            ori.replace(36, 40, getFeptxn().getFeptxnAtmType());
                            String oridata = ori.toString();
                            oridata = EbcdicConverter.toHex(CCSID.English, oridata.length(), oridata);
                            fiscEMVICReq.setOriData(oridata);
                            break;
                        case 38: /* 客戶亂碼基碼同步查核欄 */
                        	//EMV國際卡交易使用TR31 KEY不檢核，給8個0
                        	fiscEMVICReq.setSyncPpkey("00000000");
                            break;
                        case 60: /* IC卡序號 */
                            if (StringUtils.isNotBlank(getFeptxntcb().getFeptxntcbPicdseq())) {
                            	fiscEMVICReq.setCardSeq(getFeptxntcb().getFeptxntcbPicdseq());
                            }
                            break;
                        case 62: // IC 卡驗證資料
							/* 直接用ATM送的Request電文 */
							if (StringUtils.isBlank(ATMReq.getPIARQC())) {
								rtnCode = FISCReturnCode.MessageFormatError;
								return FISCReturnCode.MessageFormatError;
							}
							// 262X交易IC卡驗證資料須轉換，請參考合庫D_A9_附件_國際卡代理(EMV)_傳送財金電文參考v1.0(1111124)
							int w_dataln = Integer.parseInt(StringUtils.trim(ATMReq.getPIARQCLN()), 16);
							if(w_dataln > 0) {
								String IcCheckdata = "";
								if(StringUtils.equals(getFeptxn().getFeptxnPcode().substring(0, 3), "262")
										&& StringUtils.equals(ATMReq.getPITK2FRM(), "C")) {
									String w_ln = StringUtils.upperCase(StringUtils.leftPad(Integer.toHexString(w_dataln - 2), 4, "0")); //補滿4位
									IcCheckdata = "01" + w_ln;
								}
								w_dataln = (w_dataln - 2) * 2;
								IcCheckdata = IcCheckdata + ATMReq.getPIARQC().substring(0, w_dataln);
								fiscEMVICReq.setIcCheckdata(IcCheckdata);
							}
                            break;
                        default:
                            break;
                    }
                }
            }

            //(3)產生MAC
            RefString refMac = new RefString(fiscEMVICReq.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscEMVICReq.getMessageType(), refMac);
            fiscEMVICReq.setMAC(refMac.get());
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            //(4)產生Bit Map
            rtnCode = makeBitmap(fiscEMVICReq.getMessageType(), fiscEMVICReq.getProcessingCode(), MessageFlow.Request);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            //4.更新交易記錄(FEPTXN)
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
            getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue()); // 先預設財金 TIMEOUT
            getFeptxn().setFeptxnPending((short) 1);
            getFeptxn().setFeptxnTxrust("S"); // Reject-abnormal

            rtnCode = updateTxData();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            fiscEMVICReq.makeFISCMsg();

            //5.送EMV晶片卡REQ 電文至財金並等待回應
            oBitMap = getBitmapData(fiscEMVICReq.getMessageType() + fiscEMVICReq.getProcessingCode());

            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // getFISCTxData().Stan
            fiscAdapter.setMessageToFISC(fiscEMVICReq.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                fiscEMVICRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                if (StringUtils.isBlank(fiscEMVICRes.getFISCMessage())) {
                    rtnCode = FISCReturnCode.FISCTimeout;
                } else {
                    fiscEMVICRes.parseFISCMsg();
                    getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
                    getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                }
            } else {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendEMVRequestToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            // dbATMMSTR.Dispose();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                getLogContext().setProgramName(ProgramName + ".sendEMVRequestToFISC");
                getLogContext().setReturnCode(rtnCode);
                logMessage(Level.DEBUG, getLogContext());
                getLogContext().setRemark("");
            }
        }
    }

    /**
     * 產生財金 REQUEST 電文並送出等待回應(Pending)
     *
     * @param DefINBKPEND DefINBKPEND
     *
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Henny</modifier>
     *                    <reason></reason>
     *                    <date>2010/4/13</date>
     *                    </modify>
     *                    </history>
     * @return 送電文至FISC
     */
    public FEPReturnCode sendPendingRequestToFISC(RefBase<Inbkpend> DefINBKPEND) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        @SuppressWarnings("unused")
        String result = "";
        Bitmapdef oBitMap = getBitmapData(fiscINBKReq.getMessageType() + fiscINBKReq.getProcessingCode());
        try {

            fiscINBKReq.makeFISCMsg();
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(DefINBKPEND.get().getInbkpendStan()); // getFISCTxData().Stan
            fiscAdapter.setMessageToFISC(fiscINBKReq.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            // 2021-09-18 Richard modified start
            if (rtnCode == null) {
                rtnCode = FISCReturnCode.FISCTimeout;
            }
            // 2021-09-18 Richard modified end
            else if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
                    rtnCode = FISCReturnCode.FISCTimeout;
                } else {
                    fiscINBKRes.parseFISCMsg();
                    DefINBKPEND.get().setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Response); // F2
                    DefINBKPEND.get().setInbkpendPending((short) 2); // 解除 PENDING
                    DefINBKPEND.get().setInbkpendFiscTimeout(DbHelper.toShort(false));
                    DefINBKPEND.get().setInbkpendAaRc(String.valueOf(CommonReturnCode.Normal.getValue()));
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendPendingRequestToFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 送交易電文給FISC(OPC)
     *
     * @return 送電文至FISC(OPC)
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendResponseToFISCOpc() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        Bitmapdef oBitMap = getBitmapData(fiscOPCRes.getMessageType() + fiscOPCRes.getProcessingCode());

        try {
            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnPending((short) 0);
            } else {
                getFeptxn().setFeptxnPending((short) 1);
                getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
            }

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

            // 2021-07-01 Richard modified
            if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {
                return FEPReturnCode.FEPTXNUpdateError;
            }

            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // getFISCTxData().Stan
            fiscAdapter.setMessageToFISC(fiscOPCRes.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() == CommonReturnCode.Normal.getValue() && !StringUtils.isBlank(fiscAdapter.getMessageFromFISC())) {
                // 3Way 財金回Comfirm電文
                fiscOPCCon.setFISCMessage(fiscAdapter.getMessageFromFISC());
                rtnCode = fiscOPCCon.parseFISCMsg();
            } else {// 2Way
                if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                    return rtnCode;
                } else {
                    return FISCReturnCode.FISCTimeout;
                }
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendResponseToFISCOpc");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 送交易電文給FISC(OPC)
     *
     * @return 送電文至FISC(OPC)
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendRequestToFISCOpc() {
        return sendRequestToFISCOpc(1);
    }

    public FEPReturnCode sendRequestToFISCOpc(int type) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        Bitmapdef oBitMap = getBitmapData(fiscOPCReq.getMessageType() + fiscOPCReq.getProcessingCode());
        @SuppressWarnings("unused")
        int i = 0;

        try {
            if (type == 0) {
                // 不先更新FEPTXN TIMEOUT狀態為逾時
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
                if (feptxnDao.updateByPrimaryKey(getFeptxn()) <= 0) {
                    return FEPReturnCode.FEPTXNInsertError;
                }

                // 準備送至財金的物件
                fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
                fiscAdapter.setChannel(getFISCTxData().getTxChannel());
                fiscAdapter.setEj(getFISCTxData().getEj());
                fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // getFISCTxData().Stan
                fiscAdapter.setMessageToFISC(fiscOPCReq.getFISCMessage());
                fiscAdapter.setNoWait(false);
                fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

                rtnCode = fiscAdapter.sendReceive();
                if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                    return FISCReturnCode.FISCTimeout;
                }
            } else {
                // 先更新FEPTXN TIMEOUT狀態為逾時
                getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
                getFeptxn().setFeptxnPending((short) 1);
                getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);

                if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {
                    return FEPReturnCode.FEPTXNInsertError;
                }

                // 準備送至財金的物件
                fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
                fiscAdapter.setChannel(getFISCTxData().getTxChannel());
                fiscAdapter.setEj(getFISCTxData().getEj());
                fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // getFISCTxData().Stan
                fiscAdapter.setMessageToFISC(fiscOPCReq.getFISCMessage());
                fiscAdapter.setNoWait(false);
                fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

                rtnCode = fiscAdapter.sendReceive();
                if (rtnCode.getValue() == CommonReturnCode.Normal.getValue() && !StringUtils.isBlank(fiscAdapter.getMessageFromFISC())) {
                    fiscOPCRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                    fiscOPCRes.parseFISCMsg();
                    getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
                    getFeptxn().setFeptxnPending((short) 2);
                    getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
                } else {
                    return FISCReturnCode.FISCTimeout;
                }
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendRequestToFISCOpc");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 送Confirm交易電文給FISC(OPC)
     *
     * @return 送電文至FISC(OPC)
     *
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/5/7</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendConfirmToFISCOpc() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        Bitmapdef oBitMap = getBitmapData(fiscOPCCon.getMessageType() + fiscOPCCon.getProcessingCode());

        try {
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // getFISCTxData().Stan
            fiscAdapter.setMessageToFISC(fiscOPCCon.getFISCMessage());
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
                return FISCReturnCode.FISCTimeout;
            }

            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendConfirmToFISCOpc()");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 產生財金電文 Pending 交易 Header
     *
     * @param msgType
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode preparePendHeader(String msgType, Inbkpend defInbkpend) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            // 開始組財金電文
            // Header部分
            mINBKPEND = defInbkpend;
            switch (msgType.substring(2, 4)) {
                case "00": // Request
                    fiscINBKReq.setSystemSupervisoryControlHeader("00");
                    fiscINBKReq.setSystemNetworkIdentifier("00");
                    fiscINBKReq.setAdderssControlField("00");
                    fiscINBKReq.setMessageType(msgType);
                    fiscINBKReq.setProcessingCode(mINBKPEND.getInbkpendPcode());
                    fiscINBKReq.setSystemTraceAuditNo(mINBKPEND.getInbkpendStan());
                    fiscINBKReq.setTxnDestinationInstituteId(StringUtils.rightPad(mINBKPEND.getInbkpendDesBkno(), 7, '0'));
                    fiscINBKReq.setTxnSourceInstituteId(StringUtils.rightPad(mINBKPEND.getInbkpendBkno(), 7, '0'));
                    fiscINBKReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(mINBKPEND.getInbkpendTxDate()).substring(1, 7) + mINBKPEND.getInbkpendTxTime()); // (轉成民國年)
                    fiscINBKReq.setResponseCode(mINBKPEND.getInbkpendReqRc());
                    fiscINBKReq.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync());
                    break;
                case "10": // Response
                    fiscINBKRes.setSystemSupervisoryControlHeader("00");
                    fiscINBKRes.setSystemNetworkIdentifier("00");
                    fiscINBKRes.setAdderssControlField("00");
                    fiscINBKRes.setMessageType(msgType);
                    fiscINBKRes.setProcessingCode(mINBKPEND.getInbkpendPcode());
                    fiscINBKRes.setSystemTraceAuditNo(mINBKPEND.getInbkpendStan());
                    fiscINBKRes.setTxnDestinationInstituteId(StringUtils.rightPad(mINBKPEND.getInbkpendDesBkno(), 7, '0'));
                    fiscINBKRes.setTxnSourceInstituteId(StringUtils.rightPad(mINBKPEND.getInbkpendBkno(), 7, '0'));
                    fiscINBKRes.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(mINBKPEND.getInbkpendTxDate()).substring(1, 7) + mINBKPEND.getInbkpendTxTime()); // (轉成民國年)
                    fiscINBKRes.setResponseCode(mINBKPEND.getInbkpendRepRc());
                    fiscINBKRes.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync());
                    break;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 準備回財金的資料
     *
     * @param rtnCode
     * @return
     * @throws Exception
     */
    public String prepareForFISC(FEPReturnCode rtnCode) throws Exception {
        String fiscRC = null;
        // 查出該回給財金的Return Code

        if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
            fiscRC = BusinessBase.FISCRCNormal;
        } else {
            getLogContext().setProgramName(ProgramName);
            fiscRC = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
        }

        // 財金return code如果非正常則要call des產生MAC
        if (!fiscRC.equals(BusinessBase.FISCRCNormal)) {

        }

        // 開始組財金回應電文
        // Header部分
        fiscINBKRes.setSystemSupervisoryControlHeader("00");
        fiscINBKRes.setSystemNetworkIdentifier("00");
        fiscINBKRes.setAdderssControlField("00");
        fiscINBKRes.setMessageType(fiscINBKReq.getMessageType());
        fiscINBKRes.setProcessingCode(fiscINBKReq.getProcessingCode());
        fiscINBKRes.setSystemTraceAuditNo(fiscINBKReq.getSystemTraceAuditNo());
        fiscINBKRes.setTxnDestinationInstituteId(fiscINBKReq.getTxnDestinationInstituteId());
        fiscINBKRes.setTxnSourceInstituteId(fiscINBKReq.getTxnSourceInstituteId());
        fiscINBKRes.setTxnInitiateDateAndTime(fiscINBKReq.getTxnInitiateDateAndTime());
        fiscINBKRes.setResponseCode(fiscRC);
        fiscINBKRes.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync());

        // Body部分
        fiscINBKRes.setTxAmt(fiscINBKReq.getTxAmt());

        // 產生Bitmap
        makeBitmap();

        // 產生財金Flatfile電文
        fiscINBKRes.makeFISCMsg();

        return fiscINBKRes.getFISCMessage();

    }

    /**
     * 拆解ORI_DATA
     *
     * @param msgFunction MessageFlow
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Fly</modifier>
     *                    <reason></reason>
     *                    <date>2016/03/08</date>
     *                    </modify>
     *                    </history>
     * @return
     */
    public FEPReturnCode checkORI_DATA_EMV(MessageFlow msgFunction) {
        try {
            FISC_EMVIC fiscEMVIC = null;
            switch (msgFunction) {
                case Request:
                    fiscEMVIC = fiscEMVICReq;
                    break;
                case Response:
                    fiscEMVIC = fiscEMVICRes;
                    break;
                default:
                    fiscEMVIC = fiscEMVICCon;
                    break;
            }
            fiscEMVIC.setORIDATA(new FISC_EMVIC.DefORI_DATA_EMVIC());
            if (fiscEMVIC.getOriData().length() != 140 && fiscEMVIC.getOriData().length() != 280) {
            	getLogContext().setProgramName(ProgramName + ".checkORI_DATA_EMV");
                getLogContext().setRemark("Length ERROR  fiscEMVIC.ORI_DATA.Length = " + fiscEMVIC.getOriData().length());
                this.logMessage(getLogContext());
                return FISCReturnCode.LengthError;
            }
            String hexORIDATA = fiscEMVIC.getOriData();
            fiscEMVIC.setOriData(EbcdicConverter.fromHex(CCSID.English, fiscEMVIC.getOriData()));
            getLogContext().setProgramName(ProgramName + ".checkORI_DATA_EMV");
            getLogContext().setRemark("ASCII OriData = " + fiscEMVIC.getOriData());
            logMessage(getLogContext());
            fiscEMVIC.getORIDATA().setRRN(fiscEMVIC.getOriData().substring(0, 12));
            fiscEMVIC.getORIDATA().setOriVisaStan(fiscEMVIC.getOriData().substring(12, 18));
            fiscEMVIC.getORIDATA().setOriAcq(fiscEMVIC.getOriData().substring(18, 29));
            fiscEMVIC.getORIDATA().setPosMode(fiscEMVIC.getOriData().substring(29, 33));
            fiscEMVIC.getORIDATA().setAcqCntry(fiscEMVIC.getOriData().substring(33, 36));
            fiscEMVIC.getORIDATA().setMerchantType(fiscEMVIC.getOriData().substring(36, 40));
            fiscEMVIC.getORIDATA().setOriTxDatetime(fiscEMVIC.getOriData().substring(40, 50));
            fiscEMVIC.getORIDATA().setLocDatetime(fiscEMVIC.getOriData().substring(50, 60));
            fiscEMVIC.getORIDATA().setSetDateMmdd(fiscEMVIC.getOriData().substring(60, 64));
            fiscEMVIC.getORIDATA().setSetAmt(fiscEMVIC.getOriData().substring(64, 76));
            fiscEMVIC.getORIDATA().setSetExrate(fiscEMVIC.getOriData().substring(76, 84));
            fiscEMVIC.getORIDATA().setSetCur(fiscEMVIC.getOriData().substring(84, 87));
            fiscEMVIC.getORIDATA().setBillAmt(fiscEMVIC.getOriData().substring(87, 99));
            fiscEMVIC.getORIDATA().setBillRate(fiscEMVIC.getOriData().substring(99, 107));
            fiscEMVIC.getORIDATA().setBillCur(fiscEMVIC.getOriData().substring(107, 110));
            fiscEMVIC.getORIDATA().setRsCode(fiscEMVIC.getOriData().substring(110, 112));
            fiscEMVIC.getORIDATA().setCityName(fiscEMVIC.getOriData().substring(112, 124));
            fiscEMVIC.getORIDATA().setOriTranstype(fiscEMVIC.getOriData().substring(124, 125));
            fiscEMVIC.setOriData(hexORIDATA);
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            return FISCReturnCode.CheckBitMapError;
        }
    }

    /// #Region "資料庫處理"

    /**
     * 準備FEPTXN物件
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>MSGCTL Schema修改 MSGCTL_2WAY變MSGCTL_FISC_2WAY</reason>
     * <date>2010/10/07</date>
     * </modify>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>connie spec change 判斷bin檔是否為國際卡規則</reason>
     * <date>2010/10/19</date>
     * </modify>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>connie spec change FEPTXN_TXSEQ = I_EJFNO (取後5位, 若不足5位左補0)</reason>
     * <date>2010/10/21</date>
     * </modify>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>connie spec change 銷帳編號搬至後段作</reason>
     * <date>2010/11/05</date>
     * <reason>使用者付費for 2568,2569</reason>
     * <date>2010/12/15</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareFEPTXN() {
        SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Zone TWNZONE = new Zone();
        Bin bin = new BinExt();
        String tempString = null;
        try {
            TWNZONE = getZoneByZoneCode(ZoneCode.TWN);
            if (TWNZONE == null) {
                return IOReturnCode.ZONENotFound;
            }
            rtnCode = prepareFeptxnFromHeader();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            getFeptxn().setFeptxnAtmno(fiscINBKReq.getATMNO()); // 櫃員機代號
            getFeptxn().setFeptxnAtmnoVir(ATMNO_VIR);
            getFeptxn().setFeptxnAtmChk(fiscINBKReq.getAtmChk()); // 端末設備查核碼
            getFeptxn().setFeptxnAtmType(fiscINBKReq.getAtmType()); // 端末設備型態
            getFeptxn().setFeptxnAtmod(TWNZONE.getZoneCbsMode());
            getFeptxn().setFeptxnTxnmode(TWNZONE.getZoneCbsMode());
            /* 2025/2/17 點掉寫入 FEPTXN_TXSEQ */
//            tempString = StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 5, '0'); // spec change 2010-10-21  (取後5位, 若不足5位左補0)
//            getFeptxn().setFeptxnTxseq(tempString.substring(tempString.length() - 5, tempString.length()));
            getFeptxn().setFeptxnChannel(getFISCTxData().getTxChannel().toString());
            getFeptxn().setFeptxnPaytype(fiscINBKReq.getPAYTYPE()); // 繳款類別
            getFeptxn().setFeptxnTaxUnit(fiscINBKReq.getTaxUnit()); // 稽徵機關
            getFeptxn().setFeptxnIdno(fiscINBKReq.getIDNO()); // 身份証號
            /*2025/11/5 修正BY Bruis for 2531交易*/
            getFeptxn().setFeptxnDueDate(fiscINBKReq.getDueDate());// 繳款期限(民國年)
            getFeptxn().setFeptxnMerchantId(fiscINBKReq.getMerchantId()); // 商家代號
            if(StringUtils.isNotBlank(fiscINBKReq.getOrderNo())) {
            	 getFeptxn().setFeptxnOrderNo(fiscINBKReq.getOrderNo()); // 訂單號碼
            }
            getFeptxn().setFeptxnRsCode(fiscINBKReq.getRsCode()); // 沖銷原因 or 全國性繳稅付費單位區分
            getFeptxn().setFeptxnRemark(fiscINBKReq.getREMARK()); // 附言 for 全國性繳費(稅)
            getFeptxn().setFeptxnTxDatetimeFisc(fiscINBKReq.getTxDatetimeFisc()); // 交易日期時間
            getFeptxn().setFeptxnFiscRrn(fiscINBKReq.getFiscRrn()); // 國際簽帳金融卡購物RRN
            getFeptxn().setFeptxnBusinessUnit(fiscINBKReq.getBusinessUnit()); // 委託單位代號
            /* 2018/12/10 修改 for 中文附言欄 */
            /* 2021/3/17 修改 for 中文附言欄剔除前二碼為 0F */
//            if (StringUtils.isBlank(fiscINBKReq.getCHREM()) && !fiscINBKReq.getCHREM().equals(StringUtils.repeat("0", 80))
//                    && !fiscINBKReq.getCHREM().equals("OF" + StringUtils.repeat("0", 78))) {
//                getLogContext().setRemark("轉換前中文附言欄:" + fiscINBKReq.getCHREM());
//                this.logMessage(getLogContext());
//                String decStr = fiscINBKReq.getCHREM().trim();
//                // Fly 2020/10/14 修正中文附言欄轉碼問題，只轉換後面的00字元
//                while (!StringUtils.isBlank(decStr)) {
//                    if (decStr.substring(decStr.length() - 2).equals("00")) {
//                        decStr = decStr.substring(0, decStr.length() - 2);
//                    } else {
//                        break;
//                    }
//                }
//                /* 2019/07/12 修改 for 中文附言欄剔除半型文字及40個 0 */
//                if (!StringUtils.isBlank(decStr)) {
//                    RefString tempChrem = new RefString();
//                    convertFiscDecode(decStr, tempChrem);
//                    getLogContext().setRemark("轉換後中文附言欄:" + tempChrem.get());
//                    this.logMessage(getLogContext());
//                    byte[] tempByte = tempChrem.get().getBytes();
//                    if (tempChrem.get().length() * 2 != tempByte.length) {
//                        getLogContext().setRemark("該字串含半形字元，不寫入FEPTXN_CHREM欄位");
//                        this.logMessage(getLogContext());
//                    } else {
//                        getFeptxn().setFeptxnChrem(tempChrem.get());
//                    }
//                }
//            }
            /* 2026/2/6 新增寫入中文附言欄 */
            /* 2026/3/26 抓取分PCODE上線後交易, 才執行中文轉碼 */
            if ( "N".equals(getFISCTxData().getMsgCtl().getMsgctlCbsProc()) ) {
                if (fiscINBKReq.getCHREM() != null && !StringUtils.isBlank(fiscINBKReq.getCHREM())) {
                    getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                    getLogContext().setRemark("CHREM初始 >"+ fiscINBKReq.getCHREM() +"<");
                    logMessage(getLogContext());
                    String cleanCNS = fiscINBKReq.getCHREM().trim().replaceAll("(00)+$", ""); //財金Req電文尾段排除"00"
                    if ( !StringUtils.isBlank(cleanCNS)) {
                        /* CALL 字霸執行中文轉碼, 由CNS轉成 UTF8 */
                        String W_CHREM = AStarLoadUtils.convertCNSStrToUnicodeStr(cleanCNS);
                        getFeptxn().setFeptxnChrem(W_CHREM);
                    }
                }

                /* 2026/3/27 修改 for 資金調撥交易PCode(2700) – 收款人姓名  */
                if (fiscINBKReq.getTO_NAME() != null && !StringUtils.isBlank(fiscINBKReq.getTO_NAME())) {
                    getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                    getLogContext().setRemark("TO_NAME初始 >"+ fiscINBKReq.getTO_NAME() +"<");
                    logMessage(getLogContext());
                    String cleanCNS = fiscINBKReq.getTO_NAME().trim().replaceAll("(00)+$", ""); //財金Req電文尾段排除"00"
                    if ( !StringUtils.isBlank(cleanCNS)) {
                        /* CALL 字霸執行中文轉碼, 由CNS轉成 UTF8 */
                        String W_CHREM = AStarLoadUtils.convertCNSStrToUnicodeStr(cleanCNS);
                        getFeptxn().setFeptxnToName(W_CHREM);
                    }
                }

                /* 2026/3/27 修改 for 資金調撥交易PCode(2700) – 付款人姓名*/
                if (fiscINBKReq.getFROM_NAME() != null && !StringUtils.isBlank(fiscINBKReq.getFROM_NAME())) {
                    getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                    getLogContext().setRemark("FROM_NAME初始 >"+ fiscINBKReq.getFROM_NAME() +"<");
                    logMessage(getLogContext());
                    String cleanCNS = fiscINBKReq.getFROM_NAME().trim().replaceAll("(00)+$", ""); //財金Req電文尾段排除"00"
                    if ( !StringUtils.isBlank(cleanCNS)) {
                        /* CALL 字霸執行中文轉碼, 由CNS轉成 UTF8 */
                        String W_CHREM = AStarLoadUtils.convertCNSStrToUnicodeStr(cleanCNS);
                        getFeptxn().setFeptxnFromName(W_CHREM);
                    }
                }

                /* 2026/3/27 修改 for 資金調撥交易PCode(2700) – FXML附言 */
                if (fiscINBKReq.getFXML_MEMO() != null && !StringUtils.isBlank(fiscINBKReq.getFXML_MEMO())) {
                    getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
                    getLogContext().setRemark("FXML_MEMO初始 >"+ fiscINBKReq.getFXML_MEMO() +"<");
                    logMessage(getLogContext());
                    String cleanCNS = fiscINBKReq.getFXML_MEMO().trim().replaceAll("(00)+$", ""); //財金Req電文尾段排除"00"
                    if ( !StringUtils.isBlank(cleanCNS)) {
                        /* CALL 字霸執行中文轉碼, 由CNS轉成 UTF8 */
                        String W_CHREM = AStarLoadUtils.convertCNSStrToUnicodeStr(cleanCNS);
                        getFeptxn().setFeptxnFxmlMemo(W_CHREM);
                    }
                }
                /* 2026/3/27 修改 for 資金調撥交易PCode(2700) – 收款人身分證/統編 */
                getFeptxn().setFeptxnToIdno(fiscINBKReq.getTO_IDNO());
            }

            if (FISCPCode.PCode2261.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2262.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2263.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2264.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getFeptxn().setFeptxnPayno(StringUtils.isNotBlank(fiscINBKReq.getREMARK())&& fiscINBKReq.getREMARK().length()>=4 ?fiscINBKReq.getREMARK().substring(0, 4):fiscINBKReq.getREMARK());
            } else if (FISCPCode.PCode2561.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2562.getValueStr().equals(getFeptxn().getFeptxnPcode())
                    || FISCPCode.PCode2563.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2564.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getFeptxn().setFeptxnBusinessUnit(fiscINBKReq.getFiscRrn().substring(0, 8));
                getFeptxn().setFeptxnPayno(StringUtils.isNotBlank(fiscINBKReq.getFiscRrn()) && fiscINBKReq.getFiscRrn().length()>=12 ?fiscINBKReq.getFiscRrn().substring(8, 12):fiscINBKReq.getFiscRrn());
            }

            if (!StringUtils.isBlank(fiscINBKReq.getNpsFee())) {
                getFeptxn().setFeptxnNpsFeeCustpay(BigDecimal.valueOf(Long.parseLong(fiscINBKReq.getNpsFee()) / 10));// 繳費作業手續費
            }

            if (!StringUtils.isBlank(fiscINBKReq.getNpsFeeAll())) {
                getFeptxn().setFeptxnNpsFeeRcvr(BigDecimal.valueOf(Double.parseDouble(fiscINBKReq.getNpsFeeAll().substring(0, 4)) / 10)); //收信單位應收手續費
                getFeptxn().setFeptxnNpsFeeAgent(BigDecimal.valueOf(Double.parseDouble(fiscINBKReq.getNpsFeeAll().substring(4, 8)) / 10)); //代理單位應收手續費
                getFeptxn().setFeptxnNpsFeeTrout(BigDecimal.valueOf(Double.parseDouble(fiscINBKReq.getNpsFeeAll().substring(8, 12)) / 10)); //轉出單位應收手續費
                getFeptxn().setFeptxnNpsFeeTrin(BigDecimal.valueOf(Double.parseDouble(fiscINBKReq.getNpsFeeAll().substring(12, 16)) / 10)); //轉入單位應收手續費
                getFeptxn().setFeptxnNpsFeeFisc(BigDecimal.valueOf(Double.parseDouble(fiscINBKReq.getNpsFeeAll().substring(16, 20)) / 10)); //財金公司應收手續費
                if ("01".equals(fiscINBKReq.getRsCode())) { // 使用者付費for 2568,2569
                    getFeptxn().setFeptxnNpsClr((short) 1); // 手續費清算單位-轉出
                }
            }

            /* 2025/7/4修改, 儲存財金電文 ICMARK */
            if ( !StringUtils.isBlank(fiscINBKReq.getICMARK()) ){
                getFeptxn().setFeptxnIcmark(fiscINBKReq.getICMARK());
            }

//            if (StringUtils.isNotBlank(fiscINBKReq.getICMARK())) {
//                /* 1/3 修改 for 跨行存款 ICMARK 不轉 ASCII */
//                if (FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                        && (!StringUtils.isBlank(fiscINBKReq.getTroutBkno()) && !fiscINBKReq.getTroutBkno().substring(3, 7).equals("0000"))) {
//                    // 跨行存款
//                    Sysconf defSYSCONF = new Sysconf();
//                    defSYSCONF.setSysconfSubsysno((short) 1);
//                    defSYSCONF.setSysconfName("IBDEffectDate");
//                    Sysconf sysconf = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
//                    if (sysconf != null) {
//                        getLogContext().setRemark("跨行存款優化生效日為" + sysconf.getSysconfValue());
//                        logMessage(Level.DEBUG, getLogContext());
//                        // 有讀到跨行存款生效日
//                        if (Double.parseDouble(getFeptxn().getFeptxnTxDate()) >= Double.parseDouble(sysconf.getSysconfValue())) {
//                            // 有生效，借用 FEPTXN_NOTICE_ID 欄位存放分行代號
//                            getFeptxn().setFeptxnNoticeId(fiscINBKReq.getTroutBkno().substring(3, 7)); // 轉出行
//                            getFeptxn().setFeptxnIcmark(fiscINBKReq.getICMARK().substring(0, 30)); // IC卡備註欄
//                            getFeptxn().setFeptxnMajorActno(fiscINBKReq.getICMARK().substring(4, 20));
//                        }
//                    } else {
//                        // 讀不到跨行存款生效日
//                        getLogContext().setRemark(("讀不到跨行存款優化生效日(SYSCONF), SYSCONF_SUBSYSNO=" + defSYSCONF.getSysconfSubsysno() + " SYSCONF_NAME=" + defSYSCONF.getSysconfName()));
//                        this.logMessage(getLogContext());
//                        return FEPReturnCode.OtherCheckError;
//                    }
//                } else {
//                    // 非跨行存款
//                    getFeptxn().setFeptxnIcmark(StringUtil.fromHex(fiscINBKReq.getICMARK())); // IC卡備註欄
//                    if (StringUtils.isNotBlank(getFeptxn().getFeptxnIcmark())) {
//                        getFeptxn().setFeptxnMajorActno("00" + getFeptxn().getFeptxnIcmark().substring(0, 14)); // 主帳號 */
//                    }
//                }
//            }

            getFeptxn().setFeptxnIcSeqno(fiscINBKReq.getIcSeqno()); // IC 卡交易序號
            /* 2019/03/06 修改 for 跨行轉帳小額交易手續費調降 */
            if (StringUtils.isNotBlank(fiscINBKReq.getAcctSup())) {
                getFeptxn().setFeptxnAcctSup(fiscINBKReq.getAcctSup());
            }

            getFeptxn().setFeptxnPinblock(fiscINBKReq.getPINBLOCK()); // PINBLOCK
            getFeptxn().setFeptxnTxCur(CURTWD); // 交易幣別

            if (!StringUtils.isBlank(fiscINBKReq.getTxAmt()) && PolyfillUtil.isNumeric(fiscINBKReq.getTxAmt())) {
                getFeptxn().setFeptxnTxAmt(new BigDecimal(fiscINBKReq.getTxAmt())); // 交易金額
            }

            getFeptxn().setFeptxnTxDatetimePreauth(fiscINBKReq.getTxDatetimePreauth()); // 預先授權日期時間
            getFeptxn().setFeptxnIcSeqnoPreauth(fiscINBKReq.getIcSeqnoPreauth()); // 預先授權IC卡序號

            if (!StringUtils.isBlank(fiscINBKReq.getTxAmtPreauth())) {
                getFeptxn().setFeptxnTxAmtPreauth(new BigDecimal(fiscINBKReq.getTxAmtPreauth())); // 預先授權交易金額
            }

            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());
            getFeptxn().setFeptxnTxrust("0");

            /* 預先授權交易(2551) */
            if (FISCPCode.PCode2551.getValueStr().equals(fiscINBKReq.getProcessingCode())) {
                getFeptxn().setFeptxnIcSeqno(fiscINBKReq.getIcSeqnoPreauth()); // 預先授權IC卡序號
                getFeptxn().setFeptxnTxAmt(new BigDecimal(fiscINBKReq.getTxAmtPreauth())); // 預先授權交易金額
            }
            if (StringUtils.isNotBlank(fiscINBKReq.getOriStan())) {
                getFeptxn().setFeptxnOriStan(fiscINBKReq.getOriStan().substring(3, 10));
            }
            if (StringUtils.isNotBlank(fiscINBKReq.getTroutBkno())) {
                getFeptxn().setFeptxnTroutBkno7(fiscINBKReq.getTroutBkno()); /* 2026/4/28 修改 for  跨行存款 */
                getFeptxn().setFeptxnTroutBkno(fiscINBKReq.getTroutBkno().substring(0, 3)); // 轉出行
            } else {
                getFeptxn().setFeptxnTroutBkno(fiscINBKReq.getTxnDestinationInstituteId().substring(0, 3)); // 支付錢的銀行
            }

            if (!fiscINBKReq.getProcessingCode().substring(0, 2).equals("24")) {
                // 非國際卡
            	/* 2025/3/4 修改 for 報表系統寫入帳戶交易金額 */
            	getFeptxn().setFeptxnTxAmtAct(getFeptxn().getFeptxnTxAmt());
                getFeptxn().setFeptxnTroutActno(fiscINBKReq.getTroutActno()); // 轉出帳號
                /* 2025/3/13 修改 for 報表系統寫入卡片帳號 */
                getFeptxn().setFeptxnMajorActno(getFeptxn().getFeptxnTroutActno()); //卡片帳號
                if (StringUtils.isNotBlank(fiscINBKReq.getTrinBkno())) {
                    getFeptxn().setFeptxnTrinBkno7(fiscINBKReq.getTrinBkno()); /* 2026/4/28 修改 for  跨行存款 */
                    getFeptxn().setFeptxnTrinBkno(fiscINBKReq.getTrinBkno().substring(0, 3)); // 轉入行
                }
                getFeptxn().setFeptxnTrinActno(fiscINBKReq.getTrinActno()); // 轉入帳號

                if (FISCPCode.PCode2531.getValueStr().equals(fiscINBKReq.getProcessingCode())
                        || FISCPCode.PCode2568.getValueStr().equals(fiscINBKReq.getProcessingCode())
                        || FISCPCode.PCode2569.getValueStr().equals(fiscINBKReq.getProcessingCode())) {
                    getFeptxn().setFeptxnReconSeqno(fiscINBKReq.getReconSeqno()); // 銷帳編號
                    /* 2025/11/5 依spec點掉以下判斷 */
//                    getFeptxn().setFeptxnTrinBkno(SysStatus.getPropertyValue().getSysstatFbkno());
//
//                    /* 8/26 修改, 依據財金規格 */
//                    /*  2568/2569交易, 判斷繳款類別前二碼 */
//                    if (!FISCPCode.PCode2531.getValueStr().equals(fiscINBKReq.getProcessingCode()) && "15".equals(getFeptxn().getFeptxnPaytype().substring(0, 2))) {
//                        /* 繳款類別=自繳綜合所得稅 */
//                        getFeptxn().setFeptxnTrinActno(fiscINBKReq.getTrinActno()); // 轉入帳號
//                    } else {
//                        /* 2531將銷帳編號放入轉入帳號 */
//                        /* 2568/2569交易繳款類別前二碼<>自繳綜合所得稅 */
//                        getFeptxn().setFeptxnTrinActno(StringUtils.rightPad(fiscINBKReq.getReconSeqno(), 16, '0')); // 若不滿16位補0，轉入帳號＝銷帳編號
//                    }
                }
                /* 2024/9/11 點掉 */
//                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()) && StringUtils.isNotBlank(getFeptxn().getFeptxnTroutActno())
//                        && !"00".equals(getFeptxn().getFeptxnTroutActno().substring(0, 2))) {
//                    /* 3/1 修改 for AE卡 */
//                    /* AE 卡號15位, 帳號第1碼為 ’0’, 讀取帳號第2~7碼*/
//                    if ("0".equals(getFeptxn().getFeptxnTroutActno().substring(0, 1))) {
//                        bin.setBinNo(getFeptxn().getFeptxnTroutActno().substring(1, 7));
//                    } else { /* 帳號第1碼不為0, 直接讀取帳號前6碼 */
//                        bin.setBinNo(getFeptxn().getFeptxnTroutActno().substring(0, 6));
//                    }
//                    // modified by Maxine on 2012/03/01 for 讀取 BIN 方式由 Cache 改為讀 DB
//                    bin = getBin(bin.getBinNo(), SysStatus.getPropertyValue().getSysstatHbkno());
//                    if (bin != null && StringUtils.isNotBlank(bin.getBinProd())) {
//                        getFeptxn().setFeptxnTroutKind(bin.getBinProd());
//                    }
//                }
            } else {
                // 國際卡
                if (StringUtils.isNotBlank(fiscINBKReq.getTRK2())) {
                    getFeptxn().setFeptxnTrk2(fiscINBKReq.getTRK2());
                    /* 2024/7/11 點掉 */
//                    if("2410".equals(getFeptxn().getFeptxnPcode()) ||"2430".equals(getFeptxn().getFeptxnPcode())){
//                        /* PLUS 2410  VISA卡號 */
//                        getFeptxn().setFeptxnTroutActno(getFeptxn().getFeptxnTrk2().substring(0,16));
//                    }
//                    if("2450".equals(getFeptxn().getFeptxnPcode()) ||"2470".equals(getFeptxn().getFeptxnPcode())){
//                        /* CIRRUS 2450  帳號  */
//                        getFeptxn().setFeptxnTroutActno("000"+getFeptxn().getFeptxnTrk2().substring(6,19));
//                    }
//                    bin = getBin(getFeptxn().getFeptxnTrk2().substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
//
//                    if (bin == null || (bin != null && StringUtils.isBlank(bin.getBinProd()))) {
//                        getFeptxn().setFeptxnTroutActno("00" + getFeptxn().getFeptxnTrk2().substring(6, 9) + "0" + getFeptxn().getFeptxnTrk2().substring(9, 19));
//                    } else {
//                        getFeptxn().setFeptxnTroutActno(getFeptxn().getFeptxnTrk2().substring(0, 16));
//                        getFeptxn().setFeptxnTroutKind(bin.getBinProd());
//                    }
                }

            }

            /* 2020/12/18 手機門號跨行轉帳, 轉入銀行代號放8078999 */
            if ((FISCPCode.PCode2521.getValueStr().equals(fiscINBKReq.getProcessingCode())
                    || FISCPCode.PCode2522.getValueStr().equals(fiscINBKReq.getProcessingCode())
                    || FISCPCode.PCode2523.getValueStr().equals(fiscINBKReq.getProcessingCode())
                    || FISCPCode.PCode2524.getValueStr().equals(fiscINBKReq.getProcessingCode()))
                    && fiscINBKReq.getTrinBkno().substring(3,7).equals(INBKConfig.getInstance().getTELVirBrno())) {
                 getFeptxn().setFeptxnMtp("Y");
            }

            /* 2020/3/24 修改 for 轉帳繳納口罩費用免手續費 */
//            if ((FISCPCode.PCode2522.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2524.getValueStr().equals(getFeptxn().getFeptxnPcode()))
//                    && getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//                /* 檢核口罩轉入行及轉入帳號 */
//                List<Cbact> cbactList = cbactExtMapper.queryCbactForMask(getFeptxn().getFeptxnTrinBkno(), getFeptxn().getFeptxnTrinActno());
//                if (CollectionUtils.isNotEmpty(cbactList)) {
//                    getFeptxn().setFeptxnBenefit("M");
//                }
//            }

            /* 2021/5/24 修改 for 疫情跨手續費, 剔除跨行存款(2521) */
//            if ((FISCPCode.PCode2521.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2522.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2523.getValueStr().equals(getFeptxn().getFeptxnPcode())
//                    || FISCPCode.PCode2524.getValueStr().equals(getFeptxn().getFeptxnPcode()))
//                    && ("9999".equals(getFeptxn().getFeptxnNoticeId()) || "9998".equals(getFeptxn().getFeptxnNoticeId()))
//                    && !"M".equals(getFeptxn().getFeptxnBenefit())) {
//                getFeptxn().setFeptxnCovid19("Y");

//                String covTFRSEffectDate;
//                String covTFRSEndDate;
//                Sysconf defSYSCONF = new Sysconf();
//                defSYSCONF.setSysconfSubsysno((short) 1);
//                defSYSCONF.setSysconfName("COVTFRSEffectDate");
//                Sysconf sysconf = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
//                if (sysconf != null) {
//                    covTFRSEffectDate = sysconf.getSysconfValue();
//                } else {
//                    getLogContext().setRemark(("讀不到疫情跨行轉帳交易手續費調降生效日(SYSCONF), SYSCONF_SUBSYSNO=" + defSYSCONF.getSysconfSubsysno() + " SYSCONF_NAME=" + defSYSCONF.getSysconfName()));
//                    this.logMessage(getLogContext());
//                    return IOReturnCode.QueryNoData;
//                }
//                defSYSCONF.setSysconfSubsysno((short) 1);
//                defSYSCONF.setSysconfName("COVTFRSEndDate");
//                sysconf = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
//                if (sysconf != null) {
//                    covTFRSEndDate = sysconf.getSysconfValue();
//                } else {
//                    getLogContext().setRemark(("讀不到疫情跨行轉帳交易手續費調降截止日(SYSCONF), SYSCONF_SUBSYSNO=" + defSYSCONF.getSysconfSubsysno() + " SYSCONF_NAME=" + defSYSCONF.getSysconfName()));
//                    this.logMessage(getLogContext());
//                    return IOReturnCode.QueryNoData;
//                }
//                if ((Integer.parseInt(getFeptxn().getFeptxnTxDate()) >= Integer.parseInt(covTFRSEffectDate))
//                        && (Integer.parseInt(getFeptxn().getFeptxnTxDate()) <= Integer.parseInt(covTFRSEndDate))) {
//                    getFeptxn().setFeptxnCovid19("Y");
//                    getLogContext().setRemark("疫情跨行轉帳交易手續費調降起迄日[" + covTFRSEffectDate + "-" + covTFRSEndDate + "]");
//                    this.logMessage(getLogContext());
//                }
//            }

            getFeptxn().setFeptxnZoneCode(ZoneCode.TWN); // 預設值
            getFeptxn().setFeptxnTxCurAct(CURTWD); // 預設值
            getFeptxn().setFeptxnTbsdy(TWNZONE.getZoneTbsdy());
            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                getFeptxn().setFeptxnWay((short) 3);
            }

            getFeptxn().setFeptxnCbsProc(getFISCTxData().getMsgCtl().getMsgctlCbsProc());

            // 2012/07/03 Modify by Ruling for 文字檔記錄的轉入行值有誤正確要為FEPTXN_TRIN_BKNO
            // add by Maxine On 2011/08/24 for 增加給LogData的ATMNo,TrinActno,TrinBank,TroutActno,TroutBank複值
            getLogContext().setAtmNo(getFeptxn().getFeptxnAtmno());
            getLogContext().setTrinActno(getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFeptxn().getFeptxnTrinBkno());
            getLogContext().setTroutActno(getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFeptxn().getFeptxnBkno());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    public FEPReturnCode prepareFEPTXNTCB() {
    	try {
	    	getFeptxntcb().setFeptxntcbTxDate(getFeptxn().getFeptxnTxDate());
	    	getFeptxntcb().setFeptxntcbEjfno(getFeptxn().getFeptxnEjfno());
            /* 2026/7/6 新增時寫入交易分類 */
            getFeptxntcb().setFeptxntcbTxnflow("I");  // 原存交易
	    	return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXNTCB");
            getLogContext().setRemark("prepare FEPTXNTCB Error.");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    	
    }
    
    /**
     * 收到財金發動之電文訊息時將 Header 及共用欄位寫入 FEPTXN
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * <modifier>Husan</modifier>
     * <reason>connie spec change
     * 刪除欄位
     * 1. FEPTXN_REQ_TIME
     * 2. FEPTXN_REP_TIME
     * 3. FEPTXN_CON_TIME
     * </reason>
     * <date>2010/11/05</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareFeptxnFromHeader() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            Calendar wk_TX_Date = CalendarUtil.rocStringToADDate("0" + fiscHeader.getTxnInitiateDateAndTime().substring(0, 6));
            /* 9/9 修改, 放AP Server 系統日期及時間 */
            getFeptxn().setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            getFeptxn().setFeptxnTxTime(new SimpleDateFormat("HHmmss").format(new Date()));
            if (wk_TX_Date == null) {
                getFeptxn().setFeptxnReqDatetime(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6));
            } else {
                getFeptxn().setFeptxnReqDatetime(CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')));
            }
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnReqDatetime() + fiscHeader.getTxnInitiateDateAndTime().substring(6, 12));
            getFeptxn().setFeptxnEjfno(getFISCTxData().getEj()); // 電子日誌序號
            getFeptxn().setFeptxnBkno(fiscHeader.getTxnSourceInstituteId().substring(0, 3)); // 交易啟動銀行
            getFeptxn().setFeptxnStan(fiscHeader.getSystemTraceAuditNo()); // 財金交易序號
            getFeptxn().setFeptxnPcode(fiscHeader.getProcessingCode());
            getFeptxn().setFeptxnDesBkno(fiscHeader.getTxnDestinationInstituteId().substring(0, 3));
            getFeptxn().setFeptxnReqRc(fiscHeader.getResponseCode());

            /* 2025/10/8 修改 for  2160 電文 */
            if (getFeptxn().getFeptxnPcode() != null && getFeptxn().getFeptxnPcode().startsWith("2")) {
                /* 借用 FEPTXN_NOTICE_ID 存放交易啟動銀行後4碼 */
                if (fiscHeader.getTxnSourceInstituteId() != null && fiscHeader.getTxnSourceInstituteId().length() >= 7) {
                    String last4 = fiscHeader.getTxnSourceInstituteId().substring(3, 7); // 交易啟動銀行後4碼
                    getFeptxn().setFeptxnNoticeId(last4);
                }
            }

            /* 6/17 修改 for 繳費網發動的全繳API交易 */
            if ("948".equals(getFeptxn().getFeptxnBkno())) {
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnSourceInstituteId().substring(3, 6))) {
                    /* 借用欄位寫入全繳API交易發動行(807) */
                    getFeptxn().setFeptxnVirBrno(SysStatus.getPropertyValue().getSysstatHbkno());
                }
            }
            getFeptxn().setFeptxnSubsys(getFISCTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // 跨行記號
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
            getFeptxn().setFeptxnChannel(getFISCTxData().getTxChannel().toString()); // 通道別
            getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc()); // 財金營業日
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());

            // add by Maxine On 2011/08/24 for 增加給LogData的STAN,DesBKNO,PCODE複值
            getLogContext().setChannel(getFISCTxData().getTxChannel());
            getLogContext().setStan(getFeptxn().getFeptxnStan());
            getLogContext().setpCode(getFeptxn().getFeptxnPcode());
            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
            getLogContext().setEj(getFeptxn().getFeptxnEjfno().intValue());
            getLogContext().setBkno(getFeptxn().getFeptxnBkno());
            getLogContext().setMessageId(getFeptxn().getFeptxnMsgid());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFeptxnFromHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 組財金電文給財金時需寫入FEPTXN
     *
     * @param pCode 交易代號
     *
     *              <history>
     *              <modify>
     *              <modifier>Henny</modifier>
     *              <reason></reason>
     *              <date>2010/4/13</date>
     *              <modifier>Husan</modifier>
     *              <reason>修改Const RC</reason>
     *              <date>2010/11/25</date>
     *              </modify>
     *              <modify>
     *              <modifier>Ruling</modifier>
     *              <reason>1.Prepare FEPTXN時不需先塞值給FEPTXN_PENDING、FEPTXN_MSGFLOW
     *              2.補寫入FEPTXN_MSGID
     *              </reason>
     *              <date>2011/3/17</date>
     *              </modify>
     *              </history>
     * @return
     */
    public FEPReturnCode prepareFeptxnOpc(String pCode) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            FcrmstatMapper fcrmstatMapper = SpringBeanFactoryUtil.getBean(FcrmstatMapper.class);
            Fcrmstat fcrmStat = new Fcrmstat();
            // Tables.DBFCRMSTAT dbFcrmStat = new Tables.DBFCRMSTAT(FEPConfig.DBName);
            getFeptxn().setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // 交易日期(西元年)
            getFeptxn().setFeptxnEjfno(getFISCTxData().getEj()); // 電子日誌序號
            getFeptxn().setFeptxnDesBkno(SysStatus.getPropertyValue().getSysstatFbkno()); // 交易接收銀行
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnTxTime(new SimpleDateFormat("HHmmss").format(new Date())); // 交易時間
            // add by maxine for 9/9 修改, WEB 發動OPC交易
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // 跨行記號
            if(FEPChannel.CBS == getFISCTxData().getTxChannel() && StringUtils.isNotBlank(getFISCTxData().getStan())) {
            	getFeptxn().setFeptxnStan(getFISCTxData().getStan());
            }else {
            	getFeptxn().setFeptxnStan(getStan()); // 追蹤序號
            }
            getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC); // REQ回應代碼
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true)); // FISC逾時FLAG
            getFeptxn().setFeptxnSubsys(Short.valueOf(String.valueOf(SubSystem.INBK.getValue()))); // 子系統
            getFeptxn().setFeptxnChannel(getFISCTxData().getTxChannel().toString()); // 通道別
            getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc()); // 財金營業日
            getFeptxn().setFeptxnPcode(pCode); // 交易代號
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());

            if (!StringUtils.isBlank(fiscOPCReq.getAPID())) {
                getFeptxn().setFeptxnApid(fiscOPCReq.getAPID()); // 財金APID
            }

            if (!StringUtils.isBlank(fiscOPCReq.getNoticeId())) {
                getFeptxn().setFeptxnNoticeId(fiscOPCReq.getNoticeId()); // 通知代碼 for 3100
                getFeptxn().setFeptxnRemark(fiscOPCReq.getNoticeData()); // 訊息通知內容
            }

//            if (!StringUtils.isBlank(fiscOPCReq.getCUR())) {
//                fcrmStat.setFcrmstatCurrency(fiscOPCReq.getCUR());
//                fcrmStat = fcrmstatMapper.selectByPrimaryKey(fcrmStat.getFcrmstatCurrency());
//                if (fcrmStat != null) {
//                    getFeptxn().setFeptxnTxCur(fcrmStat.getFcrmstatCurrencyMemo()); // 交易幣別(ISO幣別文字碼)
//                }
//                getFeptxn().setFeptxnFiscCurMemo(fiscOPCReq.getCUR()); // 財金外幣匯款幣別
//            }

            // 借放到FEPTXN_REMARK for DES
            switch (pCode) {
                case "0102":
                    getFeptxn().setFeptxnRemark(fiscOPCReq.getKEYID());
                    break;
                case "3109":
                    getFeptxn().setFeptxnRemark(fiscOPCReq.getBKNO());
                    break;
            }

            // 借放到FEPTXN_APID for UI_019020 查詢時能顯示KEYID+英文
            if (!StringUtils.isBlank(fiscOPCReq.getKEYID())) {
                switch (fiscOPCReq.getKEYID()) {
                    case "04":
                        getFeptxn().setFeptxnApid(fiscOPCReq.getKEYID() + "OPC");
                        break;
                    case "05":
                        getFeptxn().setFeptxnApid(fiscOPCReq.getKEYID() + "ATM");
                        break;
                    case "06":
                        getFeptxn().setFeptxnApid(fiscOPCReq.getKEYID() + "RM");
                        break;
                    case "11":
                        getFeptxn().setFeptxnApid(fiscOPCReq.getKEYID() + "PPK");
                        break;
                    case "12":
                        getFeptxn().setFeptxnApid(fiscOPCReq.getKEYID() + "PPK");
                        break;
                }
            }

            // add by Maxine On 2011/07/13 for 增加給LogData的STAN,DesBKNO,PCODE複值
            getLogContext().setStan(getFeptxn().getFeptxnStan());
            getLogContext().setpCode(getFeptxn().getFeptxnPcode());
            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
            getLogContext().setEj(getFeptxn().getFeptxnEjfno().intValue());
            getLogContext().setBkno(getFeptxn().getFeptxnBkno());
            getLogContext().setChannel(getFISCTxData().getTxChannel());
            getLogContext().setMessageId(getFeptxn().getFeptxnMsgid());
            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFeptxnOpc");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * UI 發動交易將共用欄位寫入 FEPTXN
     *
     * @param pCode 交易代號
     *
     *              <history>
     *              <modify>
     *              <modifier>Henny</modifier>
     *              <reason></reason>
     *              <date>2010/4/13</date>
     *              <modifier>Husan</modifier>
     *              <reason>修改Const RC</reason>
     *              <date>2010/11/25</date>
     *              </modify>
     *              </history>
     * @return
     */
    public FEPReturnCode prepareFeptxnForUICommon(String pCode) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getFeptxn().setFeptxnTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // 交易日期
            // modified by Maxine on 2011/11/04 for 不重新取EJ
            getFeptxn().setFeptxnEjfno(getFISCTxData().getEj());
            // .getFeptxnEjfno() = TxHelper.GetEJ() '電子日誌序號
            getFeptxn().setFeptxnTxTime(new SimpleDateFormat("HHmmss").format(new Date())); // 交易時間
            // add by maxine for 9/9 修改, WEB 發動CLR交易
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            if(FEPChannel.CBS == getFISCTxData().getTxChannel() && StringUtils.isNotBlank(getFISCTxData().getStan())) {
            	getFeptxn().setFeptxnStan(getFISCTxData().getStan());
            }else {
            	getFeptxn().setFeptxnStan(getStan()); // 追蹤序號
            }
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnDesBkno(SysStatus.getPropertyValue().getSysstatFbkno()); // 交易接收銀行
            getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            getFeptxn().setFeptxnSubsys(getFISCTxData().getMsgCtl().getMsgctlSubsys()); // 子系統
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // 跨行記號
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
            getFeptxn().setFeptxnChannel(FEPChannel.FEP.name()); // 通道別
            getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc()); // 財金營業日
            getFeptxn().setFeptxnPending((short) 1); // PENDING
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true)); // 預設FISC TIMEOUT
            getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
            getFeptxn().setFeptxnPcode(pCode); // 交易代號
            // spec change 10/05/29
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());

            // add by Maxine On 2011/07/13 for 增加給LogData的STAN,DesBKNO,PCODE複值
            getLogContext().setStan(getFeptxn().getFeptxnStan());
            getLogContext().setpCode(getFeptxn().getFeptxnPcode());
            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
            getLogContext().setEj(getFeptxn().getFeptxnEjfno().intValue());
            getLogContext().setBkno(getFeptxn().getFeptxnBkno());
            getLogContext().setChannel(FEPChannel.FEP);
            getLogContext().setMessageId(getFeptxn().getFeptxnMsgid());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFeptxnForUICommon");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 將 FUNDLOG 相關欄位寫入 FEPTXN
     *
     * @param FUNDLOG DefFUNDLOG
     *
     *                <history>
     *                <modify>
     *                <modifier>Henny</modifier>
     *                <reason></reason>
     *                <date>2010/4/13</date>
     *                <modifier>Husan</modifier>
     *                <reason>修改Const RC</reason>
     *                <date>2010/11/25</date>
     *                </modify>
     *                </history>
     * @return
     */
    public FEPReturnCode prepareFEPTXNfromFUNDLOG(Fundlog FUNDLOG) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getFeptxn().setFeptxnTxDate(FUNDLOG.getFundlogTxDate()); // 交易日期
            getFeptxn().setFeptxnEjfno(getFISCTxData().getEj()); // 電子日誌序號
            getFeptxn().setFeptxnTxTime(new SimpleDateFormat("HHmmss").format(new Date())); // 交易時間
            // add by maxine for 9/9 修改, WEB 發動CLR交易
            getFeptxn().setFeptxnReqDatetime(getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime());
            getFeptxn().setFeptxnStan(getStan());
            getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno()); // 交易啟動銀行
            getFeptxn().setFeptxnDesBkno(SysStatus.getPropertyValue().getSysstatFbkno()); // 交易接收銀行
            getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            getFeptxn().setFeptxnPcode(FUNDLOG.getFundlogPcode());
            getFeptxn().setFeptxnTxAmt(FUNDLOG.getFundlogFgAmt()); // 撥轉金額
            getFeptxn().setFeptxnOrderNo(FUNDLOG.getFundlogFgSeqno()); // 撥轉序號
            getFeptxn().setFeptxnFiscCurMemo(FUNDLOG.getFundlogCur());
            getFeptxn().setFeptxnTroutBkno(FUNDLOG.getFundlogTroutBkno()); // 外幣清算單位
            getFeptxn().setFeptxnTroutActno(FUNDLOG.getFundlogTroutActno()); // 外幣清算帳號
            getFeptxn().setFeptxnTroutKind(FUNDLOG.getFundlogFgType().toString());
            getFeptxn().setFeptxnTrinBkno(FUNDLOG.getFundlogTrinBkno());
            getFeptxn().setFeptxnTrinActno(FUNDLOG.getFundlogTrinActno());
            getFeptxn().setFeptxnSubsys(getFISCTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // 跨行記號
            getFeptxn().setFeptxnChannel(FEPChannel.FEP.name()); // 通道別
            getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc()); // 財金營業日
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // FISC REQUEST
            getFeptxn().setFeptxnPending((short) 1); // PENDING
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true)); // 預設TIMEOUT
            getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
            // spec change 10/05/29
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());

            // add by Maxine On 2011/07/13 for 增加給LogData的STAN,DesBKNO,PCODE複值
            getLogContext().setStan(getFeptxn().getFeptxnStan());
            getLogContext().setpCode(getFeptxn().getFeptxnPcode());
            getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
            getLogContext().setEj(getFeptxn().getFeptxnEjfno().intValue());
            getLogContext().setBkno(getFeptxn().getFeptxnBkno());
            getLogContext().setChannel(FEPChannel.FEP);
            getLogContext().setMessageId(getFeptxn().getFeptxnMsgid());

            // add by Maxine On 2011/08/24 for 增加給LogData的TrinActno,TrinBank,TroutActno,TroutBank複值
            // 2015/07/24 Modify by Ruling for 由UI發動的交易轉出行應為發動行
            getLogContext().setTrinActno(getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFeptxn().getFeptxnTrinBkno());
            // getLogContext().TrinBank = .FEPTXN_BKNO
            getLogContext().setTroutActno(getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFeptxn().getFeptxnBkno());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXNfromFUNDLOG");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 準備國際卡的資料處理物件 for EMV
     *
     * @return <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <reason></reason>
     * <date>2016/03/08</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareIntltxnEMV(RefBase<Intltxn> intlTxn, RefBase<Intltxn> oriINTLTXN, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Curcd curcd = new Curcd();
        FISC_EMVIC fiscEMVIC = null;
        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscEMVIC = fiscEMVICReq;
        } else {
            fiscEMVIC = fiscEMVICRes;
        }
        try {
            intlTxn.get().setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
            intlTxn.get().setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
            intlTxn.get().setIntltxnBkno(getFeptxn().getFeptxnBkno());
            intlTxn.get().setIntltxnStan(getFeptxn().getFeptxnStan());
            intlTxn.get().setIntltxnPcode(getFeptxn().getFeptxnPcode());
            intlTxn.get().setIntltxnAtmno(getFeptxn().getFeptxnAtmno());
            intlTxn.get().setIntltxnTxTime(getFeptxn().getFeptxnTxTime());
            intlTxn.get().setIntltxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            intlTxn.get().setIntltxnReqRc(getFeptxn().getFeptxnReqRc());
            intlTxn.get().setIntltxnRepRc(getFeptxn().getFeptxnRepRc());
            intlTxn.get().setIntltxnConRc(getFeptxn().getFeptxnConRc());
            intlTxn.get().setIntltxnTxrust(getFeptxn().getFeptxnTxrust());
            intlTxn.get().setIntltxnTrk2(getFeptxn().getFeptxnTrk2());
            // Fly 2017/5/16 (週二) 上午 11:28 修改 for 拒絕 EMV Mastero卡交易
            intlTxn.get().setIntltxnNetwkData(fiscEMVIC.getNetworkCode());
            intlTxn.get().setIntltxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            intlTxn.get().setIntltxnTroutActno(getFeptxn().getFeptxnTroutActno());
            intlTxn.get().setIntltxnTroutKind(getFeptxn().getFeptxnTroutKind());
            intlTxn.get().setIntltxnBrno(getFeptxn().getFeptxnBrno());
            intlTxn.get().setIntltxnZoneCode(getFeptxn().getFeptxnZoneCode());
            // Fly 2016/05/16 改抓財金電文
            intlTxn.get().setIntltxnAtmCur(fiscEMVICReq.getCURRENCY());
            // .INTLTXN_ATM_CUR = getFeptxn().setFeptxnTxCur()
            intlTxn.get().setIntltxnAtmAmt(getFeptxn().getFeptxnTxAmt());
            // 2016/04/20 Modify by Ruling for EMV原存交易增加ORISTAN
            intlTxn.get().setIntltxnOriStan(getFeptxn().getFeptxnOriStan());
            // Fly 2016/04/07 for EMV 專案 增加AID
            if (getFeptxn().getFeptxnTrk3() != null) {
                intlTxn.get().setIntltxnAid(getFeptxn().getFeptxnTrk3().trim());
            }
            if (StringUtils.isBlank(fiscEMVIC.getOriData())) {
                return FISCReturnCode.OriginalMessageError;
            } else {
                rtnCode = checkORI_DATA_EMV(msgFlow);
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }
            }

            intlTxn.get().setIntltxnRrn(fiscEMVIC.getORIDATA().getRRN());
            intlTxn.get().setIntltxnMerchantType(fiscEMVIC.getORIDATA().getMerchantType());
            intlTxn.get().setIntltxnCityName(fiscEMVIC.getORIDATA().getCityName());
            intlTxn.get().setIntltxnTranstype(fiscEMVIC.getORIDATA().getOriTranstype());
            intlTxn.get().setIntltxnOriVisaStan(fiscEMVIC.getORIDATA().getOriVisaStan()); /* 原始交易查詢序號 */
            // 2017/09/25 Modify by Ruling for 少寫入國際傳輸時間
            intlTxn.get().setIntltxnOriTxMmddtime(fiscEMVIC.getORIDATA().getOriTxDatetime());
            intlTxn.get().setIntltxnOriAcq(fiscEMVIC.getORIDATA().getOriAcq());
            intlTxn.get().setIntltxnPosMode(fiscEMVIC.getORIDATA().getPosMode());
            // 2016/07/26 Modify by Ruling for ORIDATA.POS_MODE為空或NULL時改由FEPTXN_REMARK寫入
            if (StringUtils.isBlank(intlTxn.get().getIntltxnPosMode())) {
            	intlTxn.get().setIntltxnPosMode(getFeptxntcb().getFeptxntcbPiposent());
            }
            intlTxn.get().setIntltxnAcqCntry(fiscEMVIC.getORIDATA().getAcqCntry());
            intlTxn.get().setIntltxnLocDatetime(fiscEMVIC.getORIDATA().getLocDatetime());
            intlTxn.get().setIntltxnSetDateMmdd(fiscEMVIC.getORIDATA().getSetDateMmdd());
            intlTxn.get().setIntltxnSetAmt(BigDecimal.valueOf(Double.parseDouble(fiscEMVIC.getORIDATA().getSetAmt()) / 100));
            intlTxn.get().setIntltxnSetExrate(fiscEMVIC.getORIDATA().getSetExrate());
            intlTxn.get().setIntltxnSetCur(fiscEMVIC.getORIDATA().getSetCur());
            intlTxn.get().setIntltxnBilAmt(BigDecimal.valueOf(Double.parseDouble(fiscEMVIC.getORIDATA().getBillAmt()) / 100));
            intlTxn.get().setIntltxnBillRate(fiscEMVIC.getORIDATA().getBillRate());
            intlTxn.get().setIntltxnBilCur(fiscEMVIC.getORIDATA().getBillCur());

            if(fiscEMVICRes!= null) {
            	//VISA 交易ARPC(bit #60 IC 卡驗證結果資料)
            	//銀聯交易 (bit #63 IC 卡驗證資料)
            	if(StringUtils.isNotBlank(fiscEMVICRes.getIcCheckresult())) { //FISC 0210 bit 60
            		intlTxn.get().setIntltxnArpc(fiscEMVICRes.getIcCheckresult());
            	}else if(StringUtils.isNotBlank(fiscEMVICRes.getIcCheckdata())) { //FISC 0210 bit 63
            		intlTxn.get().setIntltxnArpc(fiscEMVICRes.getIcCheckdata());
            	}
            }
            
            if (StringUtils.isBlank(intlTxn.get().getIntltxnTrk2())) {
                // for 2633沖銷交易
                intlTxn.get().setIntltxnSetRpamt(new BigDecimal(fiscEMVIC.getSetRpamt()));

                if (intlTxn.get().getIntltxnSetRpamt().doubleValue() != 0) {
                	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    getLogContext().setRemark("完成之原始交易清算金額<>0, SET_RPAMT = " + intlTxn.get().getIntltxnSetRpamt());
                    this.logMessage(getLogContext());
                    return FISCReturnCode.OriginalMessageDataError;
                }

                // 2016/04/20 Modify by Ruling for EMV晶片卡原存交易增加ORSTAN
                oriINTLTXN.get().setIntltxnOriStan(getFeptxn().getFeptxnOriStan());
                oriINTLTXN.get().setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
                Intltxn record = intltxnExtMapper.queryByOriDataEmv(oriINTLTXN.get());
                if (record != null) {
                    oriINTLTXN.set(record);
                    if ("392".equals(intlTxn.get().getIntltxnAtmCur()) || "410".equals(intlTxn.get().getIntltxnAtmCur()) || "704".equals(intlTxn.get().getIntltxnAtmCur())) {
                        intlTxn.get().setIntltxnAtmAmt(BigDecimal.valueOf(intlTxn.get().getIntltxnAtmAmt().doubleValue() * 100));
                    }
                    if (oriINTLTXN.get().getIntltxnAtmAmt().compareTo(intlTxn.get().getIntltxnAtmAmt()) != 0 || !oriINTLTXN.get().getIntltxnAtmCur().equals(intlTxn.get().getIntltxnAtmCur())) {
                    	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    	getLogContext().setRemark("檢查原交易與沖銷資料之交易金額及幣別不符," + " 原ATM_AMT=" + oriINTLTXN.get().getIntltxnAtmAmt() + " 沖銷ATM_AMT=" + intlTxn.get().getIntltxnAtmAmt() + " 原ATM_CUR="
                                + oriINTLTXN.get().getIntltxnAtmCur() + " 沖銷ATM_CUR=" + intlTxn.get().getIntltxnAtmCur());
                        this.logMessage(getLogContext());
                        return FISCReturnCode.OriginalMessageDataError;
                    }
                    if (oriINTLTXN.get().getIntltxnSetAmt().compareTo(intlTxn.get().getIntltxnSetAmt()) != 0 || !oriINTLTXN.get().getIntltxnSetCur().equals(intlTxn.get().getIntltxnSetCur())) {
                    	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    	getLogContext().setRemark("檢查原交易與沖銷資料之清算金額及幣別不符," + " 原SET_AMT=" + oriINTLTXN.get().getIntltxnSetAmt() + " 沖銷SET_AMT=" + intlTxn.get().getIntltxnSetAmt() + " 原SET_CUR="
                                + oriINTLTXN.get().getIntltxnSetCur() + " 沖銷SET_CUR=" + intlTxn.get().getIntltxnSetCur());
                        this.logMessage(getLogContext());
                        return FISCReturnCode.OriginalMessageDataError;
                    }
                    if (!oriINTLTXN.get().getIntltxnSetDateMmdd().equals(intlTxn.get().getIntltxnSetDateMmdd())) {
                    	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    	getLogContext().setRemark("檢查原交易與沖銷資料之清算及匯率日期不符," + " 原SET_DATE_MMDD=" + oriINTLTXN.get().getIntltxnSetDateMmdd() + " 沖銷SET_DATE_MMDD=" + intlTxn.get().getIntltxnSetDateMmdd()
                                + " 原COV_DATE_MMDD=" + oriINTLTXN.get().getIntltxnCovDateMmdd() + " 沖銷COV_DATE_MMDD=" + intlTxn.get().getIntltxnCovDateMmdd());
                        this.logMessage(getLogContext());
                        return FISCReturnCode.OriginalMessageDataError;
                    }
                    if (!oriINTLTXN.get().getIntltxnSetExrate().equals(intlTxn.get().getIntltxnSetExrate())) {
                    	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    	getLogContext().setRemark("檢查原交易與沖銷資料之清算匯率不符," + " 原SET_EXRATE=" + oriINTLTXN.get().getIntltxnSetExrate() + " 沖銷SET_EXRATE=" + intlTxn.get().getIntltxnSetExrate());
                        this.logMessage(getLogContext());
                        return FISCReturnCode.OriginalMessageDataError;
                    }
                    intlTxn.get().setIntltxnOriStan(oriINTLTXN.get().getIntltxnStan());
                    intlTxn.get().setIntltxnTrk2(oriINTLTXN.get().getIntltxnTrk2());
                    intlTxn.get().setIntltxnTroutActno(oriINTLTXN.get().getIntltxnTroutActno());
                    intlTxn.get().setIntltxnTroutKind(oriINTLTXN.get().getIntltxnTroutKind());
                    intlTxn.get().setIntltxnZoneCode(oriINTLTXN.get().getIntltxnZoneCode());
                    intlTxn.get().setIntltxnBrno(oriINTLTXN.get().getIntltxnBrno());
                    intlTxn.get().setIntltxnTxCurAct(oriINTLTXN.get().getIntltxnTxCurAct());
                    intlTxn.get().setIntltxnTxAmtAct(oriINTLTXN.get().getIntltxnTxAmtAct());
                    intlTxn.get().setIntltxnExrate(oriINTLTXN.get().getIntltxnExrate());

                    getFeptxn().setFeptxnOriStan(oriINTLTXN.get().getIntltxnStan());
                    getFeptxn().setFeptxnTrk2(oriINTLTXN.get().getIntltxnTrk2());
                    getFeptxn().setFeptxnTroutActno(oriINTLTXN.get().getIntltxnTroutActno());
                    getFeptxn().setFeptxnTroutKind(oriINTLTXN.get().getIntltxnTroutKind());
                    getFeptxn().setFeptxnZoneCode(oriINTLTXN.get().getIntltxnZoneCode());
                    getFeptxn().setFeptxnBrno(oriINTLTXN.get().getIntltxnBrno());
                    getFeptxn().setFeptxnTxCurAct(oriINTLTXN.get().getIntltxnTxCurAct());
                    getFeptxn().setFeptxnTxAmtAct(oriINTLTXN.get().getIntltxnTxAmtAct());
                    getFeptxn().setFeptxnExrate(oriINTLTXN.get().getIntltxnExrate());
                    getFeptxn().setFeptxnDueDate(oriINTLTXN.get().getIntltxnTxDate());
                    getFeptxn().setFeptxnTraceEjfno(oriINTLTXN.get().getIntltxnEjfno());
                } else {
                	getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
                    getLogContext().setRemark("找不到原交易INTLTXN的資料");
                    this.logMessage(getLogContext());
                    return FISCReturnCode.OriginalMessageDataError;
                }
            }

            // 國際卡交易需回寫 FEPTXN 清算幣別及清算金額以便折算帳戶金額
            if (getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                // 客戶提領幣別為日幣/韓元/越南盾時，交易金額無小數
                if (String.valueOf(CurrencyType.JPY.name()).equals(getFeptxn().getFeptxnTxCur()) || getFeptxn().getFeptxnTxCur().equals("KRW") || getFeptxn().getFeptxnTxCur().equals("VND")) {
                    if (!FISCPCode.PCode2633.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                        intlTxn.get().setIntltxnAtmAmt(BigDecimal.valueOf(intlTxn.get().getIntltxnAtmAmt().doubleValue() * 100));
                    }
                }
                getFeptxn().setFeptxnTxAmt(intlTxn.get().getIntltxnAtmAmt()); /* 交易金額 */
            }
            
            /* 清算幣別 - 財金 ISO 幣別代號轉成文字幣別三碼 */
            curcd = getAlpha3ByIsono3(intlTxn.get().getIntltxnSetCur());
            // Fly 2016/12/29 (週四) 下午 04:21 修改 for 代理行國內EMV VISA/MASTEA卡預借現金, 清算幣別為’000’
            if (intlTxn.get().getIntltxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                    && ("2622".equals(intlTxn.get().getIntltxnPcode()) || "2632".equals(intlTxn.get().getIntltxnPcode()))
                    && "000".equals(intlTxn.get().getIntltxnSetCur())) {
                getFeptxn().setFeptxnTxCurSet("TWD");
                getFeptxn().setFeptxnTxAmtSet(intlTxn.get().getIntltxnSetAmt());
            } else if (curcd != null) {
                getFeptxn().setFeptxnTxCurSet(curcd.getCurcdAlpha3());
                getFeptxn().setFeptxnTxAmtSet(intlTxn.get().getIntltxnSetAmt());
            }

            String tbsdyFisc;
            tbsdyFisc = intlTxn.get().getIntltxnTbsdyFisc().substring(0, 4) + "/" + intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6) + "/" + intlTxn.get().getIntltxnTbsdyFisc().substring(6, 8);
            if ("12".equals(intlTxn.get().getIntltxnSetDateMmdd().substring(0, 2)) && "01".equals(intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6))) {
                // ORI_DATA交易清算日的月份為12月且財金營業日的月份為1月
                // 國際清算日為財金營業日的年度往前減一年
                getFeptxn().setFeptxnTbsdyIntl((Integer.parseInt(tbsdyFisc.substring(0, 4)) - 1) + "" + intlTxn.get().getIntltxnSetDateMmdd());
            } else if ("12".equals(intlTxn.get().getIntltxnTbsdyFisc().substring(4, 6)) && "01".equals(intlTxn.get().getIntltxnSetDateMmdd().substring(0, 2))) {
                // 財金營業日的月份為12月且ORI_DATA交易清算日的月份為1月
                // 國際清算日為財金營業日的年度往後加一年
                getFeptxn().setFeptxnTbsdyIntl((Integer.parseInt(tbsdyFisc.substring(0, 4)) + 1) + "" + intlTxn.get().getIntltxnSetDateMmdd());
            } else {
                // 國際清算日為財金營業日的年度
                getFeptxn().setFeptxnTbsdyIntl(intlTxn.get().getIntltxnTbsdyFisc().substring(0, 4) + intlTxn.get().getIntltxnSetDateMmdd());
            }

            intlTxn.get().setIntltxnTbsdyIntl(getFeptxn().getFeptxnTbsdyIntl());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareIntltxnEMV");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 新增FEPTXN
     *
     * @return
     */
    public FEPReturnCode insertFEPTxn() {

        try {
            if (feptxnDao.insertSelective(getFeptxn()) <= 0) {
                return FEPReturnCode.FEPTXNInsertError;
            } else {
                return CommonReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".insertFEPTxn");
            sendEMS(getLogContext());
            return IOReturnCode.FEPTXNInsertError;
        }

    }
    
    public FEPReturnCode insertFEPTXNTCB() {
        try {
            if (feptxnDao.insertSelective(getFeptxntcb()) <= 0) {
                return FEPReturnCode.FEPTXNInsertError;
            } else {
                return CommonReturnCode.Normal;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".insertFEPTXNTCB");
            sendEMS(getLogContext());
            return IOReturnCode.FEPTXNInsertError;
        }

    }

    /**
     * 新增跨行代收付檔
     *
     * @return
     */
    public FEPReturnCode insertAptot() {
        return null;
    }

    /**
     * 記錄跨行帳務檔(APTOT)-區分各類業務或是APTOT再多一欄位業務別
     *
     * @return
     */
    public FEPReturnCode updateAptot() {
        return null;
    }

    /**
     * 統計財金跨行代收付
     *
     * @return
     * @param isEC true沖正(-CON)交易,false非沖正(-CON)交易
     *
     *        <history>
     *        <modify>
     *        <modifier>Henny</modifier>
     *        <reason></reason>
     *        <date>2010/4/13</date>
     *        </modify>
     *        <modify>
     *        <modifier>Husan</modifier>
     *        <reason>connie spec change</reason>
     *        <date>2010/10/27</date>
     *        </modify>
     *        <modify>
     *        <modifier>Husan</modifier>
     *        <reason>connie spec change</reason>
     *        <date>2010/10/28</date>
     *        <reason>/*修正for 2568,2569 全國性繳稅, 因其無委託單位代號, 改為判斷 PCODE
     */
    /**
     * </reason>
     * <date>2010/12/15</date>
     * <reason>修正 CUP 跨國交易共用一個 APID, 因手續費不同, 故以 PCODE 建 INBKPARM, 但以實際之 APID 建 APTOT</reason>
     * <date>2011/2/18</date>
     * <reason>修正for 2568,2569,2541,542,2543 </reason>
     * <date>2011/3/07</date>
     * <reason>修正for 2400,2401 </reason>
     * <date>2011/05/11</date>
     * </modify>
     * <modify>
     * <modifier>henny</modifier>
     * <reason>connie spec change</reason>
     * <date>2011/05/31</date>
     * </modify>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>整批轉即時(2262)</reason>
     * <date>2014/04/21</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode processAptot(boolean isEC) {
        AptotExtMapper aptotMapper = SpringBeanFactoryUtil.getBean(AptotExtMapper.class);
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        String checkASC = "0";
        int cnt_DR = 0;
        int cnt_CR = 0;
        int fee_Cnt_DR = 0;
        int fee_Cnt_CR = 0;
        @SuppressWarnings("unused")
        BigDecimal txAmt = new BigDecimal(0);
        BigDecimal amt_DR = new BigDecimal(0);
        BigDecimal amt_CR = new BigDecimal(0);
        BigDecimal fee_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Amt_CR = new BigDecimal(0);
        BigDecimal fee_Differ = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_CR = new BigDecimal(0);
        BigDecimal wk_PROFIT_LOSS = new BigDecimal(0);
        BigDecimal fee_Amt_CR_Original = new BigDecimal(0);
        int icount = 0;
        try {
            txAmt = new BigDecimal(0);
            cnt_DR = 0;
            cnt_CR = 0;
            fee_Cnt_DR = 0;
            fee_Cnt_CR = 0;
            amt_DR = new BigDecimal(0);
            amt_CR = new BigDecimal(0);
            fee_Amt_DR = new BigDecimal(0);
            fee_Amt_CR = new BigDecimal(0);
            fee_Differ = new BigDecimal(0);
            fee_Oth_Amt_DR = new BigDecimal(0);
            fee_Oth_Amt_CR = new BigDecimal(0);
            wk_PROFIT_LOSS = new BigDecimal(0);

            rtnCode = getInbkparmByPK();
            if (rtnCode != CommonReturnCode.Normal) {
                setInbkparm(null);
                return rtnCode;
            }

            if ("D".equals(getInbkparm().getInbkparmPrncrdb())) { // 應收-借方
                cnt_DR = 1;
                if ("TWD".equals(getFeptxn().getFeptxnTxCur())) {
                    /* 跨行台幣交易 */
                    amt_DR = getFeptxn().getFeptxnTxAmt();
                } else {
                    /*外幣交易*/
                    amt_DR = getFeptxn().getFeptxnTxAmtAct(); //應收-借方
                }
            } else if ("C".equals(getInbkparm().getInbkparmPrncrdb())) { // 應付-貸方
                cnt_CR = 1;
                if ("TWD".equals(getFeptxn().getFeptxnTxCur())) {
                    /* 跨行台幣交易 */
                    amt_CR = getFeptxn().getFeptxnTxAmt();
                } else {
                    /*外幣交易*/
                    amt_CR = getFeptxn().getFeptxnTxAmtAct();  // 應付-貸方
                }
            } else {
                /*不需記代收付帳者亦需記筆數 for 財金*/
                if ("I".equals(getInbkparm().getInbkparmAcqFlag())) {
                    cnt_CR = 1; /* 原存交易記應付筆數 */
                } else {
                    cnt_DR = 1; /* 代理交易記應收筆數 */
                }
            }

            // 更正 APTOT
            Aptot aptot = new Aptot();
            // DBAPTOT dbAPTOT = new DBAPTOT(FEPConfig.DBName);
            String wkBRNO = null;
            String wk_APID = null;

            wk_APID = getFeptxn().getFeptxnApid();

            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                wkBRNO = getFeptxn().getFeptxnBrno(); //扣款掛帳分行
            } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                wkBRNO = getFeptxn().getFeptxnTrinBrno(); //入帳掛帳分行
            } else {
                wkBRNO = getFeptxn().getFeptxnAtmBrno(); //ATM 帳務分行
            }

            aptot.setAptotStDate(getFeptxn().getFeptxnTbsdyFisc());
            aptot.setAptotApid(wk_APID);
            aptot.setAptotBrno(wkBRNO);
            aptot.setAptotAscFlag(DbHelper.toShort(false));

            if (!isEC) {
                // 非沖正(-CON)交易
                getFeptxn().setFeptxnClrType((short) 1); // 跨行清算
                // 直接更新(APTOT) 欄位及條件如下, 不先查詢
                aptot.setAptotCntDr(cnt_DR);
                aptot.setAptotAmtDr(amt_DR);
                aptot.setAptotCntCr(cnt_CR);
                aptot.setAptotAmtCr(amt_CR);
            } else {
                // 沖正(-CON)交易，借貸別相反
                getFeptxn().setFeptxnClrType((short) 2); // REVERSE
                // 直接更新(APTOT) 欄位及條件如下, 不先查詢
                aptot.setAptotEcCntDr(cnt_CR);
                aptot.setAptotEcAmtDr(amt_CR);
                aptot.setAptotEcCntCr(cnt_DR);
                aptot.setAptotEcAmtCr(amt_DR);
            }
            icount = aptotMapper.updateForRMProcessAPTOT(aptot);
            // modify 20110218
            if (icount < 1) {
                // 2013/12/20 Modify by Ruling for Insert Duplicate，要包Catch Exception並判斷SQL錯誤代碼為2627才需再次Update APTOT
                try {
                    if (aptotMapper.insertSelective(aptot) < 1) {
                        getLogContext().setRemark("ProcessAptot-Insert APTOT 0 筆");
                        sendEMS(getLogContext());
                    }
                } catch (Exception ex) {
                    SQLException exSql = (SQLException) ex.getCause();
                    if (2627 == exSql.getErrorCode()) {
                        if (aptotMapper.updateForRMProcessAPTOT(aptot) < 1) {
                            getLogContext().setRemark("ProcessAptot-After Insert APTOT PK Violation, Update 0 筆");
                            sendEMS(getLogContext());
                        } else {
                            getLogContext().setRemark("ProcessAptot-After Insert APTOT PK Violation, Update 成功");
                            this.logMessage(getLogContext());
                        }
                    } else {
                        getLogContext().setRemark("wkBRNO: " + wkBRNO);
                        this.logMessage(getLogContext());
                        throw ex;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processAptot");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 以日期搜尋 FEPTXN
     *
     * @param bkno   FEPTXN_BKNO
     * @param stan   FEPTXN_STAN
     * @param txDate txDate
     *
     *               <history>
     *               <modify>
     *               <modifier>Henny</modifier>
     *               <reason></reason>
     *               <date>2010/4/13</date>
     *               </modify>
     *               </history>
     * @return
     */
    public FEPReturnCode searchFeptxn(String txDate, String bkno, String stan) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
        // Tables.DBFEPTXN db = new Tables.DBFEPTXN(FEPConfig.DBName, 0, SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 8));
        Bsdays aBSDAYS = new Bsdays();
        // Tables.DBBSDAYS dbBSDAYS = new Tables.DBBSDAYS(FEPConfig.DBName);
        String wk_TBSDY = null;
        String wk_NBSDY = "";
        try {
            db.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatLbsdyFisc().substring(6, 6 + 2), StringUtils.join(ProgramName, ".searchFeptxn"));
            getFeptxn().setFeptxnBkno(bkno);
            getFeptxn().setFeptxnStan(stan);
            setFeptxn(db.getFEPTXNByStanAndBkno(getFeptxn().getFeptxnStan(), getFeptxn().getFeptxnBkno()));
            if (getFeptxn() == null) {
                aBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
                aBSDAYS.setBsdaysDate(txDate);
                aBSDAYS = bsdaysMapper.selectByPrimaryKey(aBSDAYS.getBsdaysZoneCode(), aBSDAYS.getBsdaysDate());
                if (aBSDAYS == null) {
                    return FEPReturnCode.BSDAYSNotFound;
                }

                if (DbHelper.toBoolean(aBSDAYS.getBsdaysWorkday())) {// 工作日
                    wk_TBSDY = aBSDAYS.getBsdaysDate();
                    wk_NBSDY = aBSDAYS.getBsdaysNbsdy();
                } else {
                    wk_TBSDY = aBSDAYS.getBsdaysNbsdy();
                }
                if (wk_TBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
                    db.setTableNameSuffix(wk_TBSDY.substring(6, 2 + 6), StringUtils.join(ProgramName, ".searchFeptxn"));
                    setFeptxn(new FeptxnExt());
                    getFeptxn().setFeptxnBkno(bkno);
                    getFeptxn().setFeptxnStan(stan);
                    setFeptxn(db.getFEPTXNByStanAndBkno(getFeptxn().getFeptxnStan(), getFeptxn().getFeptxnBkno()));
                    if (getFeptxn() == null) {
                        if (!StringUtils.isBlank(wk_NBSDY) && wk_NBSDY.compareTo(SysStatus.getPropertyValue().getSysstatLbsdyFisc()) < 0) {
                            db.setTableNameSuffix(wk_NBSDY.substring(6, 2 + 6), StringUtils.join(ProgramName, ".searchFeptxn"));
                            setFeptxn(new FeptxnExt());
                            getFeptxn().setFeptxnBkno(bkno);
                            getFeptxn().setFeptxnStan(stan);
                            setFeptxn(db.getFEPTXNByStanAndBkno(getFeptxn().getFeptxnStan(), getFeptxn().getFeptxnBkno()));
                            if (getFeptxn() == null) {
                                rtnCode = FEPReturnCode.FEPTXNNotFound;
                            }
                        } else {
                            rtnCode = FEPReturnCode.FEPTXNNotFound;
                        }
                    }
                } else {
                    rtnCode = FEPReturnCode.FEPTXNNotFound;
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".searchFeptxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /// #Region "主機相關動作"

    /**
     * 組上主機電文
     *
     * @return
     */
    public FEPReturnCode prepareOPCToCBS() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        return rtnCode;
    }

    /**
     * 上主機查詢資料
     *
     * @return
     */
    public FEPReturnCode sendOPCToCBS() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String hostTxId = getFISCTxData().getMsgCtl().getMsgctlTwcbstxid1();
        String tita = "";
        UnisysAdapter adapter = new UnisysAdapter(getFISCTxData());
        try {

            // 根據不同的主機電文代號呼叫不同的主機電文類別組電文
            switch (Integer.parseInt(hostTxId)) {

            }

            adapter.setChannel(getFISCTxData().getTxChannel());
            adapter.setTxId(hostTxId);
            adapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            adapter.setMessageToUnisys(tita);

            rtnCode = adapter.sendReceive();

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendOPCToCBS");
            sendEMS(getLogContext());
            return rtnCode;
        }
        return rtnCode;
    }

    private void loadBitmapData() {
        List<Bitmapdef> bitmapdefsList = bitmapdefExtMapper.queryAllData(StringUtils.EMPTY);
        synchronized (bitmapdefMap) {
            bitmapdefMap.clear();
            for (Bitmapdef bitmapdef : bitmapdefsList) {
                bitmapdefMap.put(StringUtils.join(bitmapdef.getBitmapdefMsgtype(), bitmapdef.getBitmapdefPcode()), bitmapdef);
            }
        }
    }

    /// #Region "Function(CLR)"

    /**
     * 收到財金(5102)之電文訊息時需寫入 CLRTOTAL
     *
     * @param DefCLRTOTAL DefCLRTOTAL
     * @param msgFlow     電文種類Request or Response
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Henny</modifier>
     *                    <reason></reason>
     *                    <date>2010/4/13</date>
     *                    </modify>
     *                    <modify>
     *                    <modifier>HusanYin</modifier>
     *                    <reason>修正CLRTOTAL_ATM_AMT_DR</reason>
     *                    <date>2011/03/07</date>
     *                    </modify>
     *                    </history>
     * @return FEPReturnCode
     */
    public FEPReturnCode prepareClrtotal(RefBase<Clrtotal> DefCLRTOTAL, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_CLR fiscCLR = null;
        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscCLR = fiscCLRReq;
        } else {
            fiscCLR = fiscCLRRes;
        }
        try {
            DefCLRTOTAL.get().setClrtotalStDate(getFeptxn().getFeptxnTxDate());
            DefCLRTOTAL.get().setClrtotalCur("000");
            if (getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                DefCLRTOTAL.get().setClrtotalSource((short) 1); // 查詢財金
            } else {
                DefCLRTOTAL.get().setClrtotalSource((short) 2); // 財金結帳
            }
            DefCLRTOTAL.get().setClrtotalTxTime(getFeptxn().getFeptxnTxTime());
            DefCLRTOTAL.get().setClrtotalBkno(getFeptxn().getFeptxnBkno());
            DefCLRTOTAL.get().setClrtotalStan(getFeptxn().getFeptxnStan());
            DefCLRTOTAL.get().setClrtotalReqRc(getFeptxn().getFeptxnReqRc());
            DefCLRTOTAL.get().setClrtotalRepRc(getFeptxn().getFeptxnRepRc());
            DefCLRTOTAL.get().setClrtotalPcode(getFeptxn().getFeptxnPcode());

            // 其他欄位請參考 財金電文整理_Request.xls(Class:CLR；PCODE 5102)
            DefCLRTOTAL.get().setClrtotalSumAmtDr(StringUtils.isBlank(fiscCLR.getSumAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getSumAmtDr()));
            DefCLRTOTAL.get().setClrtotalSumAmtCr(StringUtils.isBlank(fiscCLR.getSumAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getSumAmtCr()));
            DefCLRTOTAL.get().setClrtotalRmCntDr(StringUtils.isBlank(fiscCLR.getRmCntDr()) ? 0 : Integer.parseInt(fiscCLR.getRmCntDr()));
            DefCLRTOTAL.get().setClrtotalRmAmtDr(StringUtils.isBlank(fiscCLR.getRmAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getRmAmtDr()));
            DefCLRTOTAL.get().setClrtotalRmCntCr(StringUtils.isBlank(fiscCLR.getRmCntCr()) ? 0 : Integer.parseInt(fiscCLR.getRmCntCr()));
            DefCLRTOTAL.get().setClrtotalRmAmtCr(StringUtils.isBlank(fiscCLR.getRmAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getRmAmtCr()));
            DefCLRTOTAL.get().setClrtotalAtmCntDr(StringUtils.isBlank(fiscCLR.getAtmCntDr()) ? 0 : Integer.parseInt(fiscCLR.getAtmCntDr()));
            DefCLRTOTAL.get().setClrtotalAtmAmtDr(StringUtils.isBlank(fiscCLR.getAtmAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getAtmAmtDr()));
            DefCLRTOTAL.get().setClrtotalAtmCntCr(StringUtils.isBlank(fiscCLR.getAtmCntCr()) ? 0 : Integer.parseInt(fiscCLR.getAtmCntCr()));
            DefCLRTOTAL.get().setClrtotalAtmAmtCr(StringUtils.isBlank(fiscCLR.getAtmAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getAtmAmtCr()));
            DefCLRTOTAL.get().setClrtotalFeeAmtDr(StringUtils.isBlank(fiscCLR.getFeeAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFeeAmtDr()));
            DefCLRTOTAL.get().setClrtotalFeeAmtCr(StringUtils.isBlank(fiscCLR.getFeeAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFeeAmtCr()));
            DefCLRTOTAL.get().setClrtotalOddsDr(StringUtils.isBlank(fiscCLR.getOddsDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getOddsDr()));
            DefCLRTOTAL.get().setClrtotalOddsCr(StringUtils.isBlank(fiscCLR.getOddsCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getOddsCr()));
            DefCLRTOTAL.get().setClrtotalRemainCnt(StringUtils.isBlank(fiscCLR.getRemainCnt()) ? 0 : Integer.parseInt(fiscCLR.getRemainCnt()));
            DefCLRTOTAL.get().setClrtotalRemainAmt(StringUtils.isBlank(fiscCLR.getRemainAmt()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getRemainAmt()));
            DefCLRTOTAL.get().setClrtotalAtmEcCntDr(StringUtils.isBlank(fiscCLR.getAtmEcCntDr()) ? 0 : Integer.parseInt(fiscCLR.getAtmEcCntDr()));
            DefCLRTOTAL.get().setClrtotalAtmEcAmtDr(StringUtils.isBlank(fiscCLR.getAtmEcAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getAtmEcAmtDr()));
            // 2011/12/13 Modify by KK for CLRTOTAL_ATM_EC_CNT_CR應塞入貸方金額
            DefCLRTOTAL.get().setClrtotalAtmEcCntCr(StringUtils.isBlank(fiscCLR.getAtmEcCntCr()) ? 0 : Integer.parseInt(fiscCLR.getAtmEcCntCr()));
            DefCLRTOTAL.get().setClrtotalAtmEcAmtCr(StringUtils.isBlank(fiscCLR.getAtmEcAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getAtmEcAmtCr()));
            DefCLRTOTAL.get().setClrtotalFeeEcAmtDr(StringUtils.isBlank(fiscCLR.getFeeEcAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFeeEcAmtDr()));
            DefCLRTOTAL.get().setClrtotalFeeEcAmtCr(StringUtils.isBlank(fiscCLR.getFeeEcAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFeeEcAmtCr()));
            DefCLRTOTAL.get().setClrtotalFgCntDr(StringUtils.isBlank(fiscCLR.getFgCntDr()) ? 0 : Integer.parseInt(fiscCLR.getFgCntDr()));
            DefCLRTOTAL.get().setClrtotalFgAmtDr(StringUtils.isBlank(fiscCLR.getFgAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFgAmtDr()));
            DefCLRTOTAL.get().setClrtotalFgCntCr(StringUtils.isBlank(fiscCLR.getFgCntCr()) ? 0 : Integer.parseInt(fiscCLR.getFgCntCr()));
            DefCLRTOTAL.get().setClrtotalFgAmtCr(StringUtils.isBlank(fiscCLR.getFgAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFgAmtCr()));
            DefCLRTOTAL.get().setClrtotalRevolAmt(StringUtils.isBlank(fiscCLR.getRevolAmt()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getRevolAmt()));
            DefCLRTOTAL.get().setClrtotalActBal(StringUtils.isBlank(fiscCLR.getActBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getActBal()));
            DefCLRTOTAL.get().setClrtotalPosCntDr(StringUtils.isBlank(fiscCLR.getPosCntDr()) ? 0 : Integer.parseInt(fiscCLR.getPosCntDr()));
            DefCLRTOTAL.get().setClrtotalPosAmtDr(StringUtils.isBlank(fiscCLR.getPosAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getPosAmtDr()));
            DefCLRTOTAL.get().setClrtotalPosCntCr(StringUtils.isBlank(fiscCLR.getPosCntCr()) ? 0 : Integer.parseInt(fiscCLR.getPosCntCr()));
            DefCLRTOTAL.get().setClrtotalPosAmtCr(StringUtils.isBlank(fiscCLR.getPosAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getPosAmtCr()));
            DefCLRTOTAL.get().setClrtotalFediCntDr(StringUtils.isBlank(fiscCLR.getFediCntDr()) ? 0 : Integer.parseInt(fiscCLR.getFediCntDr()));
            DefCLRTOTAL.get().setClrtotalFediAmtDr(StringUtils.isBlank(fiscCLR.getFediAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFediAmtDr()));
            DefCLRTOTAL.get().setClrtotalFediCntCr(StringUtils.isBlank(fiscCLR.getFediCntCr()) ? 0 : Integer.parseInt(fiscCLR.getFediCntCr()));
            DefCLRTOTAL.get().setClrtotalFediAmtCr(StringUtils.isBlank(fiscCLR.getFediAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscCLR.getFediAmtCr()));
            // 2013/07/25 Modify by Ruling for 財金調整清算電文(5102/5201)
            if (!StringUtils.isBlank(fiscCLR.getAtmCnt2Dr()) && Integer.parseInt(fiscCLR.getAtmCnt2Dr()) > 0) {
                DefCLRTOTAL.get().setClrtotalAtmCntDr(Integer.parseInt(fiscCLR.getAtmCnt2Dr()));
            }
            if (!StringUtils.isBlank(fiscCLR.getAtmCnt2Cr()) && Integer.parseInt(fiscCLR.getAtmCnt2Cr()) > 0) {
                DefCLRTOTAL.get().setClrtotalAtmCntCr(Integer.parseInt(fiscCLR.getAtmCnt2Cr()));
            }
            if (!StringUtils.isBlank(fiscCLR.getRmCnt2Dr()) && Integer.parseInt(fiscCLR.getRmCnt2Dr()) > 0) {
                DefCLRTOTAL.get().setClrtotalRmCntDr(Integer.parseInt(fiscCLR.getRmCnt2Dr()));
            }
            if (!StringUtils.isBlank(fiscCLR.getRmCnt2Cr()) && Integer.parseInt(fiscCLR.getRmCnt2Cr()) > 0) {
                DefCLRTOTAL.get().setClrtotalRmCntCr(Integer.parseInt(fiscCLR.getRmCnt2Cr()));
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareClrtotal");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 收到財金外幣匯款清算之電文訊息時寫入 CLRTOTAL
     *
     * @param DefCLRTOTAL DefCLRTOTAL
     * @param msgFlow     電文種類Request or Response
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Henny</modifier>
     *                    <reason></reason>
     *                    <date>2010/4/13</date>
     *                    </modify>
     *                    </history>
     * @return FEPReturnCode
     */
    public FEPReturnCode prepareClrtotalByFCur(RefBase<Clrtotal> DefCLRTOTAL, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_USDCLR fiscFCCLR = null;
        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscFCCLR = fiscFCCLRReq;
        } else {
            fiscFCCLR = fiscFCCLRRes;
        }
        try {
            DefCLRTOTAL.get().setClrtotalStDate(getFeptxn().getFeptxnTxDate());
            if (getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                DefCLRTOTAL.get().setClrtotalSource((short) 1); // 查詢財金
            } else {
                DefCLRTOTAL.get().setClrtotalSource((short) 2); // 財金結帳
            }
            DefCLRTOTAL.get().setClrtotalTxTime(getFeptxn().getFeptxnTxTime());
            DefCLRTOTAL.get().setClrtotalBkno(getFeptxn().getFeptxnBkno());
            DefCLRTOTAL.get().setClrtotalStan(getFeptxn().getFeptxnStan());
            DefCLRTOTAL.get().setClrtotalReqRc(getFeptxn().getFeptxnReqRc());
            DefCLRTOTAL.get().setClrtotalRepRc(getFeptxn().getFeptxnRepRc());
            DefCLRTOTAL.get().setClrtotalPcode(getFeptxn().getFeptxnPcode());

            // 其他欄位請參考 財金電文整理_Response.xls(Class:USD_CLR；PCODE 5802(+)）
            DefCLRTOTAL.get().setClrtotalCur(fiscFCCLR.getCUR());
            DefCLRTOTAL.get().setClrtotalSumAmtDr(StringUtils.isBlank(fiscFCCLR.getSumAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getSumAmtDr()));
            DefCLRTOTAL.get().setClrtotalSumAmtCr(StringUtils.isBlank(fiscFCCLR.getSumAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getSumAmtCr()));
            DefCLRTOTAL.get().setClrtotalRmCntDr(StringUtils.isBlank(fiscFCCLR.getRmCntDr()) ? 0 : Integer.parseInt(fiscFCCLR.getRmCntDr()));
            DefCLRTOTAL.get().setClrtotalRmAmtDr(StringUtils.isBlank(fiscFCCLR.getRmAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getRmAmtDr()));
            DefCLRTOTAL.get().setClrtotalRmCntCr(StringUtils.isBlank(fiscFCCLR.getRmCntCr()) ? 0 : Integer.parseInt(fiscFCCLR.getRmCntCr()));
            DefCLRTOTAL.get().setClrtotalRmAmtCr(StringUtils.isBlank(fiscFCCLR.getRmAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getRmAmtCr()));
            DefCLRTOTAL.get().setClrtotalFgCntDr(StringUtils.isBlank(fiscFCCLR.getFgCntDr()) ? 0 : Integer.parseInt(fiscFCCLR.getFgCntDr()));
            DefCLRTOTAL.get().setClrtotalFgAmtDr(StringUtils.isBlank(fiscFCCLR.getFgAmtDr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getFgAmtDr()));
            DefCLRTOTAL.get().setClrtotalFgCntCr(StringUtils.isBlank(fiscFCCLR.getFgCntCr()) ? 0 : Integer.parseInt(fiscFCCLR.getFgCntCr()));
            DefCLRTOTAL.get().setClrtotalFgAmtCr(StringUtils.isBlank(fiscFCCLR.getFgAmtCr()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getFgAmtCr()));
            DefCLRTOTAL.get().setClrtotalRemainCnt(StringUtils.isBlank(fiscFCCLR.getRemainCnt()) ? 0 : Integer.parseInt(fiscFCCLR.getRemainCnt()));
            DefCLRTOTAL.get().setClrtotalRemainAmt(StringUtils.isBlank(fiscFCCLR.getRemainAmt()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getRemainAmt()));
            DefCLRTOTAL.get().setClrtotalRevolAmt(StringUtils.isBlank(fiscFCCLR.getRevolAmt()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getRevolAmt()));
            DefCLRTOTAL.get().setClrtotalActBal(StringUtils.isBlank(fiscFCCLR.getActBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getActBal()));
            DefCLRTOTAL.get().setClrtotalClrbkFundBal(StringUtils.isBlank(fiscFCCLR.getClrbkFundBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getClrbkFundBal()));
            DefCLRTOTAL.get().setClrtotalFiscFundBal(StringUtils.isBlank(fiscFCCLR.getFiscFundBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getFiscFundBal()));

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareClrtotalByFCur");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 財金撥轉交易之電文訊息時需寫入 FUNDLOG
     *
     * @param DefFUNDLOG DefFUNDLOG
     *
     *                   <history>
     *                   <modify>
     *                   <modifier>Henny</modifier>
     *                   <reason></reason>
     *                   <date>2010/4/13</date>
     *                   <modifier>Husan</modifier>
     *                   <reason>修正Const RC</reason>
     *                   <date>2010/11/25</date>
     *                   </modify>
     *                   </history>
     */
    public FEPReturnCode prepareFundlog(RefBase<Fundlog> DefFUNDLOG) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            DefFUNDLOG.get().setFundlogTxDate(getFeptxn().getFeptxnTxDate());
            DefFUNDLOG.get().setFundlogEjfno(getFeptxn().getFeptxnEjfno());
            DefFUNDLOG.get().setFundlogPcode(getFeptxn().getFeptxnPcode());
            switch (DefFUNDLOG.get().getFundlogPcode()) {
                case "5312": // 增加跨行基金通知交易
                    if (getFeptxn().getFeptxnReqRc().equals(NormalRC.FISC_REQ_RC)) {
                        DefFUNDLOG.get().setFundlogFgType((short) 1); // 增加基金
                        DefFUNDLOG.get().setFundlogFgInd("C"); // 貸方
                    } else if ("0002".equals(getFeptxn().getFeptxnReqRc())) { // 沖正
                        DefFUNDLOG.get().setFundlogFgType((short) 4); // 取消增加
                        DefFUNDLOG.get().setFundlogFgInd("D"); // 借方
                    }
                    break;
                case "5314": // 減少跨行基金通知交易
                    DefFUNDLOG.get().setFundlogFgType((short) 2); // 減少基金
                    DefFUNDLOG.get().setFundlogFgInd("D"); // 借方
                    break;
            }
            DefFUNDLOG.get().setFundlogCur("000");
            DefFUNDLOG.get().setFundlogFgAmt(getFeptxn().getFeptxnTxAmt());
            DefFUNDLOG.get().setFundlogTxTime(getFeptxn().getFeptxnTxTime());
            DefFUNDLOG.get().setFundlogBkno(getFeptxn().getFeptxnBkno());
            DefFUNDLOG.get().setFundlogStan(getFeptxn().getFeptxnStan());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFundlog");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 收到財金外幣撥轉交易(5812,5815)之電文訊息時需寫入 FUNDLOG
     *
     * @param DefFUNDLOG DefFUNDLOG
     * @param msgFlow    電文種類Request or Response
     *
     *                   <history>
     *                   <modify>
     *                   <modifier>Henny</modifier>
     *                   <reason></reason>
     *                   <date>2010/4/13</date>
     *                   </modify>
     *                   </history>
     */
    public FEPReturnCode prepareFundlogByFCur(RefBase<Fundlog> DefFUNDLOG, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_USDCLR fiscFCCLR = null;
        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscFCCLR = fiscFCCLRReq;
        } else {
            fiscFCCLR = fiscFCCLRRes;
        }
        try {
            DefFUNDLOG.get().setFundlogTxDate(getFeptxn().getFeptxnTxDate());
            DefFUNDLOG.get().setFundlogEjfno(getFeptxn().getFeptxnEjfno());
            DefFUNDLOG.get().setFundlogPcode(getFeptxn().getFeptxnPcode());
            DefFUNDLOG.get().setFundlogTroutBkno(getFeptxn().getFeptxnTroutBkno());
            DefFUNDLOG.get().setFundlogTrinBkno(getFeptxn().getFeptxnTrinBkno());
            switch (DefFUNDLOG.get().getFundlogPcode()) {
                case "5812": // 外幣匯款增加跨行基金結果交易
                    DefFUNDLOG.get().setFundlogFgType((short) 1); // 增加基金
                    DefFUNDLOG.get().setFundlogFgInd("C"); // 貸方
                    break;
                case "5815": // 外幣匯款調撥專戶轉帳處理結果交易
                    if (SysStatus.getPropertyValue().getSysstatHbkno().equals(DefFUNDLOG.get().getFundlogTroutBkno())) {
                        // 轉出行
                        DefFUNDLOG.get().setFundlogFgType((short) 3); // 調撥基金
                        DefFUNDLOG.get().setFundlogFgInd("D"); // 借方
                    } else {
                        // 轉入行
                        DefFUNDLOG.get().setFundlogFgType((short) 1); // 增加基金
                        DefFUNDLOG.get().setFundlogFgInd("C"); // 貸方
                    }
                    break;
            }
            DefFUNDLOG.get().setFundlogCur(getFeptxn().getFeptxnFiscCurMemo());
            DefFUNDLOG.get().setFundlogFgAmt(getFeptxn().getFeptxnTxAmt());
            DefFUNDLOG.get().setFundlogTxTime(getFeptxn().getFeptxnTxTime());
            DefFUNDLOG.get().setFundlogBkno(getFeptxn().getFeptxnBkno());
            DefFUNDLOG.get().setFundlogStan(getFeptxn().getFeptxnStan());
            DefFUNDLOG.get().setFundlogFgPeriod(StringUtils.isBlank(fiscFCCLR.getFgPeriod()) ? 0 : Short.valueOf(fiscFCCLR.getFgPeriod()));
            DefFUNDLOG.get().setFundlogTxType(StringUtils.isBlank(fiscFCCLR.getTxType()) ? 0 : Short.valueOf(fiscFCCLR.getTxType()));
            DefFUNDLOG.get().setFundlogOriBkno(fiscFCCLR.getOriStan().substring(0, 3));
            DefFUNDLOG.get().setFundlogOriStan(fiscFCCLR.getOriStan().substring(3));

            if (DefFUNDLOG.get().getFundlogTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                DefFUNDLOG.get().setFundlogFgBal(StringUtils.isBlank(fiscFCCLR.getFgTroutBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getFgTroutBal()));
            } else {
                DefFUNDLOG.get().setFundlogFgBal(StringUtils.isBlank(fiscFCCLR.getFgTrinBal()) ? BigDecimal.valueOf(0) : new BigDecimal(fiscFCCLR.getFgTrinBal()));
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFundlogByFCur");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 依MSGID檢核APID
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkAPId(String strMSGID, String strAPId) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            switch (strMSGID) {
                case "320600":
                case "320500":
                    switch (strAPId) {
                        case "1000":
                        case "1001":
                        case "1002":
                        case "1100":
                        case "1101":
                        case "1102":
                        case "1200":
                        case "1201":
                        case "1202":
                        case "1300":
                        case "1301":
                        case "1302":
                        case "1400":
                        case "1401":
                        case "1402":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        case "1600":
                        case "1601":
                        case "1602":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        case "2000":
                        case "2200":
                        case "2500":
                        case "2510":
                        case "2520":
                        case "2530":
                        case "2540":
                        case "2550":
                        case "2560":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        case "4000":
                        case "4100":
                        case "4200":
                        case "4300":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        case "7100":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        default:
                            rtnCode = FISCReturnCode.APIDError;
                            break;
                    }
                    break;
            }
            return rtnCode;
        } catch (Exception ex) {
            return FISCReturnCode.APIDError;
        }
    }

    /**
     * 依MSGID檢核KEYID
     *
     *
     * <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkKeyID(String strMSGID, String strKeyID) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            switch (strMSGID) {
                case "010300":
                    switch (strKeyID) {
                        case "01":
                        case "02":
                        case "03":
                        case "04":
                        case "05":
                        case "06":
                        case "07":
                        case "08":
                        case "09":
                        case "10":
                        case "11":
                        case "12":
                        case "13":
                        case "14":
                        case "15":
                        case "16":
                        case "17":
                        case "18":
                        case "19":
                        case "20":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        default:
                            rtnCode = FISCReturnCode.APIDError;
                            break;
                    }
                    break;
                case "010500":
                    switch (strKeyID) {
                        case "04":
                        case "05":
                        case "06":
                        case "12":
                            rtnCode = CommonReturnCode.Normal;
                            break;
                        default:
                            rtnCode = FISCReturnCode.APIDError;
                            break;
                    }
                    break;
            }

            return rtnCode;
        } catch (Exception ex) {
            return FISCReturnCode.APIDError;
        }
    }

    /// #Region "TMO處理"

    /**
     * TMO處理
     *
     * @param ATM_TITA_EXPCD ATM 電文字段塞給FEPTXN_EXCP_CODE
     *
     *                       <history>
     *                       <modify>
     *                       <modifier>Henny</modifier>
     *                       <reason></reason>
     *                       <date>2010/4/13</date>
     *                       </modify>
     *                       </history>
     */
    public FEPReturnCode processTMO(String ATM_TITA_EXPCD) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Intltxn defINTLTXN = new Intltxn();
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 判斷原交易是否已經結束()
            if (!DbHelper.toBoolean(getFeptxn().getFeptxnAaComplete())) {
                // AA Initital
                return rtnCode; // 程式結束，不回應前端
            }

            // 判斷原交易是否需要沖正()
            if (getFeptxn().getFeptxnRepRc().equals(NormalRC.FISC_ATM_OK) || StringUtils.isBlank(getFeptxn().getFeptxnConRc()) || getFeptxn().getFeptxnWay() != 3) {
                return rtnCode;
            }

            // 更新(getFeptxn() & INTLTXN)
            try {
                getFeptxn().setFeptxnAaComplete(DbHelper.toShort(false)); // AA Initital
                getFeptxn().setFeptxnConRc(StringUtils.leftPad(String.valueOf(FISCReturnCode.FISCTimeout), 4, '0')); // TIMEOUT
                getFeptxn().setFeptxnTmoFlag(DbHelper.toShort(true)); // 收到 TMO 訊息
                getFeptxn().setFeptxnExcpCode(ATM_TITA_EXPCD);
                // 檔名SEQ為 getFeptxnTbsdyFisc()(7:2)
                int i = feptxnDao.updateByPrimaryKey(getFeptxn());
                if (i < 1) {
                    // DBFeptxn.Database.RollbackTransaction();
                    transactionManager.rollback(txStatus);
                    return FEPReturnCode.FEPTXNUpdateError;
                }
                if ("24".equals(getFeptxn().getFeptxnPcode().substring(0, 2))) { // 國際卡交易
                    defINTLTXN.setIntltxnConRc(getFeptxn().getFeptxnConRc());
                    defINTLTXN.setIntltxnTxDate(getFeptxn().getFeptxnTxDate());
                    defINTLTXN.setIntltxnEjfno(getFeptxn().getFeptxnEjfno());
                    if (intltxnExtMapper.updateByPrimaryKey(defINTLTXN) < 1) {
                        transactionManager.rollback(txStatus);
                        // DBFeptxn.Database.RollbackTransaction();
                        return FEPReturnCode.UpdateFail;
                    }
                }
                transactionManager.commit(txStatus);
            } catch (Exception ex) {
                transactionManager.rollback(txStatus);
                ex.printStackTrace();
                return FEPReturnCode.UpdateFail;
            }

            // 送 –Confirm 至財金
            rtnCode = sendConfirmToFISC();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            // 沖轉跨行代收付
            rtnCode = processAptot(true);
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFundlogByFCur");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkUPBin() {
        List<Upbin> upbins = null;
        boolean W_FLAG = false;
        String WK_IN_ACNO = "";
        try {
            upbins = upbinExtMapper.queryAllData(" UPBIN_BIN_LENGTH, UPBIN_BIN");

            for (Upbin dr : upbins) {
                // 以BIN長度讀取 ATM電文 TRK2 欄位 */
                WK_IN_ACNO = getFeptxn().getFeptxnTrk2().substring(0, dr.getUpbinBinLength().intValue());
                // 如ATM電文 TRK2 欄位 BIN 與銀聯卡BIN相符 */
                if (dr.getUpbinBin().trim().equals(WK_IN_ACNO)) {
                    W_FLAG = true;
                    break;
                }
            }

            if (!W_FLAG) {
                return FISCReturnCode.UPBinError;
            }

            return CommonReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processTMO");
            sendEMS(getLogContext());
            return FISCReturnCode.UPBinError;
        }
    }

    public FEPReturnCode checkCCard(String W_ACTNO) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        // Dim defBin As Tables.DefBIN
        // Dim dbBin As New Tables.DBBIN(FEPConfig.DBName)
        @SuppressWarnings("unused")
        Credit host = new Credit(getGeneralData());
        try {
            // modified by maxine on 12/26 修改, 判斷帳號前五碼=虛擬帳號(‘00598’)
            if (W_ACTNO.substring(0, 5).equals(CMNConfig.getInstance().getVirtualActno()) && "226".equals(getFeptxn().getFeptxnPcode().substring(0, 3))) {
                // 全國性繳費ID+ACC(2261~2264)交易輸入虛擬帳號時, 後面的身份證號
                // 前二位數字編碼(A:01, B:02, C:03, … 以此類推)
                String W_IDNO1 = mappingFirstDigit(W_ACTNO.substring(5, 16));
                // 將身份證號欄位前二碼轉成文字(A~Z) 前二位數字編碼(A:10, B:11, 依財金編碼)
                String W_IDNO2 = mappingFirstDigitIdno(getFeptxn().getFeptxnIdno());
                // 比對身份證號與00598之後的身份證號是否相符
                if (!W_IDNO1.trim().equals(W_IDNO2.trim())) {
                    return FISCReturnCode.CheckIDNOError;
                }
            }

            // 2013/01/29 Modify by Ruling for 帳務代理交易檢核信用卡主機狀態
            FEPCache.reloadCache(CacheItem.SYSSTAT);
// 2024-03-06 Richard modified for SYSSTATE 調整
//            if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())) {
//                getLogContext().setRemark("暫停永豐信用卡(SYSSTAT_ASC)服務");
//                this.logMessage(getLogContext());
//                return FISCReturnCode.ReceiverBankServiceStop;
//            }

            // 組信用卡電文(B17) */
            // 2021/10/09 Kai Modify 不送信用卡中心 依據Candy提供的“20211001-20211007-TEST.docx”
            // rtnCode = host.sendToCredit(getGeneralData().getMsgCtl().getMsgctlAsctxid(), (byte) 1);
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkCCard");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 負責處理Check in/out時要更新EAINET
     *
     * @return FEPReturnCode
     * <history>
     * <modify>
     * <modifier>Jim</modifier>
     * <reason></reason>
     * <date>2012/08/21</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode updateEAINETForCheckInOut(String mode) {
        // TODO not implement yet
        return CommonReturnCode.Normal;
    }

    /**
     * 檢核行動金融卡狀態
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>NEW Function for PSP TSM</reason>
     * <date>2014/12/18</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkTSMCard() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Cardtsm defCARDTSM = new Cardtsm();

        try {
            // 1.檢核行動金融卡狀態
            // 2014/12/22 Modify by Ruling for 判斷卡檔不存在的方式，調整到和CheckCardStatus的方式一致
            defCARDTSM = this.getCardTsmByPK();
            if (defCARDTSM == null) {
                rtnCode = ATMReturnCode.ICCardNotFound;
                return rtnCode;
            }

            getLogContext().setChact(defCARDTSM.getCardActno());
            getFeptxn().setFeptxnMajorActno(defCARDTSM.getCardActno());
            getFeptxn().setFeptxnCardSeq(defCARDTSM.getCardCardSeq());
            getFeptxn().setFeptxnTxCurAct(TXCUR.TWD); // 主帳號幣別
            getFeptxn().setFeptxnZoneCode(ZoneCode.TWN); // 卡片所在地區

            if (defCARDTSM.getCardStatus().doubleValue() < ATMCardStatus.Start.getValue()) {
                getLogContext().setRemark("卡片狀態 < 4, CardTSM.CARD_STATUS = " + defCARDTSM.getCardStatus().toString());
                this.logMessage(getLogContext());
                rtnCode = ATMReturnCode.CardNotEffective;
                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                rtnCode = codeRefBase.get();
                return rtnCode;
            }

            if (defCARDTSM.getCardStatus().doubleValue() == ATMCardStatus.Lose.getValue()) {
                getLogContext().setRemark("卡片狀態 = 5, CardTSM.CARD_STATUS = " + defCARDTSM.getCardStatus().toString());
                this.logMessage(getLogContext());
                rtnCode = ATMReturnCode.CardCancelled;
                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                rtnCode = codeRefBase.get();
                return rtnCode;
            }

            if (defCARDTSM.getCardStatus().doubleValue() == ATMCardStatus.Cancel.getValue()) {
                getLogContext().setRemark("卡片狀態 = 6, CardTSM.CARD_STATUS = " + defCARDTSM.getCardStatus().toString());
                this.logMessage(getLogContext());
                rtnCode = ATMReturnCode.CardLoseEfficacy;
                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                rtnCode = codeRefBase.get();
                return rtnCode;
            }

            if (defCARDTSM.getCardStatus().doubleValue() > ATMCardStatus.Cancel.getValue()) {
                getLogContext().setRemark("卡片狀態 > 6, CardTSM.CARD_STATUS = " + defCARDTSM.getCardStatus().toString());
                this.logMessage(getLogContext());
                rtnCode = ATMReturnCode.ACTNOError;
                RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                rtnCode = codeRefBase.get();
                return rtnCode;
            }

            if (StringUtils.leftPad(getFeptxn().getFeptxnIcmark(), 30, '0').substring(29, 30).equals("3") && getFeptxn().getFeptxnTxAmt().doubleValue() <= INBKConfig.getInstance().getINBKSALimit()) {
                // 行動金融卡及1003小額支付，存入不同交易序號
                if (Integer.parseInt(getFeptxn().getFeptxnIcSeqno()) == defCARDTSM.getCardSatxseq()) {
                    getLogContext().setRemark(
                            "小額支付交易序號重複(FEPTXN_IC_SEQNO = Card.CARD_SATXSEQ), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_SATXSEQ = " + defCARDTSM.getCardSatxseq().toString());
                    this.logMessage(getLogContext());
                    rtnCode = ATMReturnCode.ICSeqNoDuplicate; // 小額支付交易序號重複
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                    updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                    rtnCode = codeRefBase.get();
                    return rtnCode;
                }
                if (Double.valueOf(getFeptxn().getFeptxnIcSeqno()) < (double) defCARDTSM.getCardSatxseq() + 1) {
                    getLogContext().setRemark("小額支付交易序號錯誤(FEPTXN_IC_SEQNO < Card.CARD_SATXSEQ + 1), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_SATXSEQ = "
                            + defCARDTSM.getCardSatxseq().toString());
                    this.logMessage(getLogContext());
                    rtnCode = ATMReturnCode.ICSeqNoError; // 小額支付交易序號錯誤
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                    updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                    rtnCode = codeRefBase.get();
                    return rtnCode;
                }
                // 檢核正確，將 IC 卡交易序號寫入 CARDTSM 檔
                defCARDTSM.setCardSatxseq(Integer.parseInt(getFeptxn().getFeptxnIcSeqno()));
            } else {
                // 行動金融卡及1001檔
                if (Integer.parseInt(getFeptxn().getFeptxnIcSeqno()) == defCARDTSM.getCardIctxseq()) {
                    getLogContext().setRemark(
                            "IC卡交易序號重複(FEPTXN_IC_SEQNO = Card.CARD_ICTXSEQ), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_ICTXSEQ = " + defCARDTSM.getCardIctxseq().toString());
                    this.logMessage(getLogContext());
                    rtnCode = ATMReturnCode.ICSeqNoDuplicate; // IC卡交易序號重複
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                    updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                    rtnCode = codeRefBase.get();
                    return rtnCode;
                }
                if (Double.valueOf(getFeptxn().getFeptxnIcSeqno()) < (double) defCARDTSM.getCardIctxseq() + 1) {
                    getLogContext().setRemark("IC卡交易序號錯誤(FEPTXN_IC_SEQNO < Card.CARD_ICTXSEQ + 1), FEPTXN_IC_SEQNO = " + getFeptxn().getFeptxnIcSeqno() + ", Card.CARD_ICTXSEQ = "
                            + defCARDTSM.getCardIctxseq().toString());
                    this.logMessage(getLogContext());
                    rtnCode = ATMReturnCode.ICSeqNoError; // IC卡交易序號錯誤
                    RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
                    updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
                    rtnCode = codeRefBase.get();
                    return rtnCode;
                }
                // 檢核正確，將 IC 卡交易序號寫入 CARDTSM 檔
                defCARDTSM.setCardIctxseq(Integer.parseInt(getFeptxn().getFeptxnIcSeqno()));
            }

            // 2.更新卡片檔
            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtnCode);
            updateCardTSM(defCARDTSM, codeRefBase); // Update CardTSM
            rtnCode = codeRefBase.get();
            // 2015/09/02 Modify by Ruling for 行動金融卡轉帳交易-組T24電文會使用到約定轉帳記號
            // 3.同步txdata的Property，組T24電文會使用到約定轉帳記號
            getFISCTxData().setTfrFlag(defCARDTSM.getCardTfrFlag().toString());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkTSMCard");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 檢核卡檔後更新卡檔(CARDTSM)
     *
     * @param defCARDTSM 要更新的卡檔物件
     *
     *                   <history>
     *                   <modify>
     *                   <modifier>Ruling</modifier>
     *                   <reason>NEW Function for PSP TSM</reason>
     *                   <date>2014/12/18</date>
     *                   </modify>
     *                   </history>
     */
    public void updateCardTSM(Cardtsm defCARDTSM, RefBase<FEPReturnCode> rtnCode) throws Exception {
        try {
            // 將本次交易資料班制上次交易資料
            defCARDTSM.setCardTxDateLast(defCARDTSM.getCardTxDate());
            defCARDTSM.setCardTxAtmidLast(defCARDTSM.getCardTxAtmid());
            defCARDTSM.setCardTxBankLast(defCARDTSM.getCardTxBank());
            defCARDTSM.setCardTxStanLast(defCARDTSM.getCardTxStan());
            defCARDTSM.setCardTxRcLast(defCARDTSM.getCardTxRcN());
            defCARDTSM.setCardTxCavLast(defCARDTSM.getCardTxCav());
            defCARDTSM.setCardTxConLast(defCARDTSM.getCardTxCon());
            defCARDTSM.setCardTxDate(getFeptxn().getFeptxnTxDate());
            defCARDTSM.setCardTxAtmid(getFeptxn().getFeptxnAtmno());
            defCARDTSM.setCardTxBank(getFeptxn().getFeptxnBkno());
            defCARDTSM.setCardTxStan(getFeptxn().getFeptxnStan());
            if (rtnCode.get().getValue() == FEPReturnCode.Normal.getValue()) {
                defCARDTSM.setCardTxRcN(NormalRC.ATM_OK);
            } else {
                if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
                    defCARDTSM.setCardTxRcN(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.get().getValue()), FEPChannel.FEP, FEPChannel.FISC, getLogContext()));
                } else {
                    defCARDTSM.setCardTxRcN(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.get().getValue()), FEPChannel.FEP, FEPChannel.ATM, getLogContext()));
                }
            }
            if (cardtsmExtMapper.updateByPrimaryKey(defCARDTSM) <= 0) {
                @SuppressWarnings("unused")
                FEPReturnCode rtn = FEPReturnCode.CARDUpdateError;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 透過FEPTXN取得卡片狀態
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>NEW Function for PSP TSM</reason>
     * <date>2014/12/22</date>
     * </modify>
     * </history>
     */
    public Cardtsm getCardTsmByPK() {
        try {
            String cardCardno = StringUtils.leftPad(getFeptxn().getFeptxnIcmark(), 30, '0').substring(0, 16);
            Cardtsm cardtsm = cardtsmExtMapper.getSingleCard(cardCardno);
            if (cardtsm == null) {
                throw ExceptionUtil.createException("讀不到卡檔(CARDTSM)資料 (卡號: ", cardCardno, ")");
            }
            return cardtsm;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".getCardTSMByPK"));
            sendEMS(getLogContext());
            return null;
        }
    }

    /**
     * 檢核是否為歐洲VISA BIN
     *
     * @param acfee ATMREQ.ACFEE
     * @return FEPReturnCode
     *
     * <history>
     * <modify>
     * <modifier>Fly</modifier>
     * <reason>New Function for EMV拒絶磁條卡交易</reason>
     * <date>2015/09/02</date>
     * </modify>
     * </history>
     * @throws Exception
     */
    public FEPReturnCode checkEUVISABIN(String bin, RefBase<BigDecimal> acfee, RefString chargfg) throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfMapper.class);
        Sysconf defSYSCONF = new Sysconf();

        // 檢核EMV處理費FLAG
        defSYSCONF.setSysconfName("EMVACFeeFlag");
        defSYSCONF.setSysconfSubsysno((short) 1);
        defSYSCONF = sysconfMapper.selectByPrimaryKey(defSYSCONF.getSysconfSubsysno(), defSYSCONF.getSysconfName());
        if (defSYSCONF == null) {
            getLogContext().setRemark("SYSCONF-EMVACFeeFlag 查不到!!");
            this.logMessage(getLogContext());
            return FEPReturnCode.OtherCheckError;
        }
        if (defSYSCONF.getSysconfValue().trim().equals("N")) {
            getLogContext().setRemark("EMVACFeeFlag 值為 N  不收ACCESS FEE");
            this.logMessage(getLogContext());
            chargfg.set("N");
            acfee.set(new BigDecimal(0));
            getLogContext().setRemark("是否收取處理費=" + chargfg.get() + "  處理費金額=" + acfee.get());
            this.logMessage(getLogContext());
            return rtnCode;
        }

        // 2016/12/28 Modify by Ruling for 檢核是否為永豐VISA信用卡BIN
        // 檢核是否為永豐VISA信用卡BIN
        // Tables.DBBIN dbBIN = new Tables.DBBIN(FEPConfig.DBName);
        Bin defBIN = new BinExt();
        defBIN.setBinNo(bin);
        defBIN.setBinBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        defBIN = binMapper.selectByPrimaryKey(defBIN.getBinNo(), defBIN.getBinBkno());
        if (defBIN != null) {
            if (defBIN.getBinOrg().trim().equals("VISA")) {
                getLogContext().setRemark("永豐VISA信用卡");
                this.logMessage(getLogContext());
                chargfg.set("N");
                acfee.set(new BigDecimal(0));
                getLogContext().setRemark("是否收取處理費=" + chargfg.get() + "  處理費金額=" + acfee.get());
                this.logMessage(getLogContext());
                return rtnCode;
            } else {
                getLogContext().setRemark("非永豐VISA信用卡BIN, BIN_ORG=" + defBIN.getBinOrg());
                this.logMessage(getLogContext());
                return FEPReturnCode.OtherCheckError;
            }
        }

        // 檢核EMV BIN
        // DBVISABIN dbVISABIN = new DBVISABIN(FEPConfig.DBName);
        Visabin defVISABIN = new Visabin();
        getLogContext().setRemark("bin = " + bin);
        this.logMessage(getLogContext());
        defVISABIN.setVisabinBin(bin);
        defVISABIN = visabinMapper.selectByPrimaryKey(defVISABIN.getVisabinBin());
        if (defVISABIN != null) {
            if ("3".equals(defVISABIN.getVisabinRegion()) || "TW".equals(defVISABIN.getVisabinCountry())) {
                getLogContext().setRemark("CheckEUVISABIN-VISA 歐洲卡及國內卡 不收ACCESS FEE");
                this.logMessage(getLogContext());
                chargfg.set("N");
                acfee.set(new BigDecimal(0));
                getLogContext().setRemark("是否收取處理費=" + chargfg.get() + "  處理費金額=" + acfee.get());
                this.logMessage(getLogContext());
            } else {
                getLogContext().setRemark("CheckEUVISABIN-檢核OK");
                this.logMessage(getLogContext());
                chargfg.set("Y");
                acfee.set(BigDecimal.valueOf(INBKConfig.getInstance().getEMVAccessFee()));
                getLogContext().setRemark("是否收取處理費=" + chargfg.get() + "  處理費金額=" + acfee.get());
                this.logMessage(getLogContext());
            }
        } else {
            getLogContext().setRemark("CheckEUVISABIN-VISA BIN不存在");
            this.logMessage(getLogContext());
            return FEPReturnCode.OtherCheckError;
        }
        return rtnCode;
    }

    /**
     * 負責處理拆解MASTER EMV晶片卡驗證資料
     *
     * @return FEPReturnCode
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason></reason>
     * <date>2016/03/01</date>
     * </modify>
     * </history>
     */
    public String check_IC_CHECKDATA(String W_ARQCLN, String W_ARQC) {
        ArrayList<String[]> parts = new ArrayList<String[]>();
        String tagName = null;
        String tagName1 = null;
        String tagValue = null;
        int tagLen = 0;
        int tagNameIndex = 0;
        int tagLenIndex = 0;
        int tagValueIndex = 0;

        try {
            // Add parts to the list.(TagName, TagLen, TagValue)
            parts.add(new String[]{"9F26", "0", ""});
            parts.add(new String[]{"9F27", "0", ""});
            parts.add(new String[]{"9F10", "0", ""});
            parts.add(new String[]{"9F37", "0", ""});
            parts.add(new String[]{"9F36", "0", ""});
            parts.add(new String[]{"95", "0", ""});
            parts.add(new String[]{"9A", "0", ""});
            parts.add(new String[]{"9C", "0", ""});
            parts.add(new String[]{"9F02", "0", ""});
            parts.add(new String[]{"5F2A", "0", ""});
            parts.add(new String[]{"82", "0", ""});
            parts.add(new String[]{"9F1A", "0", ""});
            parts.add(new String[]{"9F03", "0", ""});
            parts.add(new String[]{"9F34", "0", ""});
            parts.add(new String[]{"9F33", "0", ""});
            parts.add(new String[]{"9F35", "0", ""});
            parts.add(new String[]{"9F1E", "0", ""});
            parts.add(new String[]{"9F53", "0", ""});
            parts.add(new String[]{"84", "0", ""});
            parts.add(new String[]{"9F09", "0", ""});
            parts.add(new String[]{"9F41", "0", ""});
            parts.add(new String[]{"91", "0", ""});

            int i = 0;
            while (W_ARQC.length() > 0) {
                if ("95,9A,9C,82,84,91,9F,5F,71,72".indexOf(W_ARQC.substring(i, i + 2)) > -1) {
                    if (W_ARQC.length() >= 2) {
                        tagName = W_ARQC.substring(i, i + 2);
                    }

                    if (W_ARQC.length() >= 4) {
                        tagName1 = W_ARQC.substring(i + 2, i + 2 + 2);
                    }

                    if ("5F2A,9F26,9F27,9F10,9F37,9F36,9F02,5F2A,9F1A,9F03,9F34,9F33,9F35,9F1E,9F53,9F09,9F41".indexOf(tagName + tagName1) > -1) {
                        // 2Byte Tag Name
                        tagName = tagName + tagName1;
                    } else {
                        // 1Byte Tag Name
                    }

                    // Tag之長度
                    if (tagName != null) {
                        tagLenIndex = tagNameIndex + tagName.length();
                        try {
                            tagLen = Integer.parseInt(StringUtil.convertFromAnyBaseString(W_ARQC.substring(tagLenIndex, tagLenIndex + 2), 16, 10, 0));
                        } catch (NumberFormatException e) {
                        }
                        // Tag之內容
                        tagValueIndex = tagNameIndex + tagName.length() + 2;
                        tagValue = W_ARQC.substring(tagValueIndex, tagValueIndex + tagLen * 2);
                        // 拆出來的長度和內容放入list中
                        for (String[] li : parts) {
                            if (tagName.equals(li[0])) {
                                li[1] = String.valueOf(tagLen * 2);
                                li[2] = tagValue;
                                break;
                            }
                        }
                        // icCheckData去掉已取的Tag
                        W_ARQC = W_ARQC.substring(tagName.length() + 2 + tagValue.length());
                    } else {
                        getLogContext().setRemark("Check_IC_CHECKDATA-拆解的TagName為Nothing)");
                        this.logMessage(getLogContext());
                    }
                } else {
                    // FORMAT ERROR
                    // 2016/10/05 Modify by Ruling for 1Byte Tag Name 找不到時要 Return ErrorCode
                    getLogContext().setRemark("Check_IC_CHECKDATA-Tag內容拆解錯誤");
                    this.logMessage(getLogContext());
                }
            }
            String tag95Value = "";
            String tag9F10Value = "";
            String tag95Len = "";
            String tag9F10Len = "";
            for (String[] part : parts) {
                String Name = part[0];
                String Len = part[1];
                String Value = part[2];

                if ("95".equals(Name)) {
                    tag95Value = tagValue;
                    tag95Len = Len;
                    break;  // 如果找到了符合條件的 Tag 值，就退出循環
                }
                if ("9F10".equals(Name)) {
                    tag9F10Value = tagValue;
                    tag9F10Len = Len;
                    break;
                }
            }


            String W_ICCHECKRESULT = tag95Len + "01" + tag95Value + tag9F10Len + "01" + tag9F10Value;
            return W_ICCHECKRESULT;


            // 9F10 分6Byte和4Byte
//            String wCVR = "";
//            switch (parts.get(2)[2].length()) {
//                case 36:
//                    // M/CHIP4 規格->6 Byte
//                    wCVR = parts.get(2)[2].substring(4, 16) + "80";
//                    ARC.set("0012");
//                    getLogContext().setRemark("M/CHIP4");
//                    this.logMessage(getLogContext());
//                    break;
//                case 18:
//                    // M/CHIP2.0.5 規格->4 Byte
//                    wCVR = parts.get(2)[2].substring(6, 14) + "800000";
//                    ARC.set("3030");
//                    getLogContext().setRemark("M/CHIP2.0.5");
//                    this.logMessage(getLogContext());
//                    break;
//                case 16:
//                    // M/CHIP2.1 規格->4 Byte
//                    wCVR = parts.get(2)[2].substring(4, 12) + "800000";
//                    ARC.set("3030");
//                    getLogContext().setRemark("M/CHIP2.1");
//                    this.logMessage(getLogContext());
//                    break;
//            }
//
//            // 9F03 沒值需補滿12個0
//            if (StringUtils.isBlank(parts.get(12)[2])) {
//                parts.get(12)[2] = StringUtils.leftPad("0", 12, '0');
//            }
//
//            ARQC.set(parts.get(0)[2]);
//            ATC.set(parts.get(4)[2]);
//            UN.set(parts.get(3)[2]);
//            InputData.set(parts.get(8)[2] + parts.get(12)[2] + parts.get(11)[2] + parts.get(5)[2] + parts.get(9)[2] + parts.get(6)[2]
//                    + parts.get(7)[2] + parts.get(3)[2] + parts.get(10)[2] + parts.get(4)[2] + StringUtils.rightPad(wCVR, 14, '0'));
//            ARPC.set(parts.get(21)[2]);

//            getLogContext().setRemark("FISC ARQC=" + ARQC + ", InputData=" + InputData + ", ATC=" + ATC + ", ARPC=" + ARPC);
//            this.logMessage(getLogContext());

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".check_IC_CHECKDATA");
            sendEMS(getLogContext());
            return null;
        }
    }
    
    /**
     * 負責處理拆解VISA EMV晶片卡驗證資料
     * @param W_ARQCLN
     * @param W_ARQC
     * @return
     */
    public String MAKEICCHECKDATA(String W_ARQCLN, String W_ARQC) {
        ArrayList<String[]> parts = new ArrayList<String[]>();
        String tagName = null;
        String tagName1 = null;
        String tagValue = null;
        int tagLen = 0;
        int tagNameIndex = 0;
        int tagLenIndex = 0;
        int tagValueIndex = 0;

        try {
            // Add parts to the list.(TagName, TagLen, TagValue)
        	parts.add(new String[]{"82", "0", ""});
        	parts.add(new String[]{"95", "0", ""});
        	parts.add(new String[]{"9A", "0", ""});
        	parts.add(new String[]{"9C", "0", ""});
        	parts.add(new String[]{"5F2A", "0", ""});
        	parts.add(new String[]{"9F02", "0", ""});
        	parts.add(new String[]{"9F10", "0", ""});
        	parts.add(new String[]{"9F1A", "0", ""});
        	parts.add(new String[]{"9F26", "0", ""});
        	parts.add(new String[]{"9F33", "0", ""});
        	parts.add(new String[]{"9F36", "0", ""});
        	parts.add(new String[]{"9F37", "0", ""});
        	parts.add(new String[]{"91", "0", ""});
        	parts.add(new String[]{"71", "0", ""});
        	parts.add(new String[]{"72", "0", ""});

            int i = 0;
            while (W_ARQC.length() > 0) {
                if ("82,95,9A,9C,5F,9F,91,71,72".indexOf(W_ARQC.substring(i, i + 2)) > -1) {
                    if (W_ARQC.length() >= 2) {
                        tagName = W_ARQC.substring(i, i + 2);
                    }

                    if (W_ARQC.length() >= 4) {
                        tagName1 = W_ARQC.substring(i + 2, i + 2 + 2);
                    }

                    if ("5F2A,9F02,9F10,9F1A,9F26,9F33,9F36,9F37".indexOf(tagName + tagName1) > -1) {
                        // 2Byte Tag Name
                        tagName = tagName + tagName1;
                    } else {
                        // 1Byte Tag Name
                    }

                    // Tag之長度
                    if (tagName != null) {
                        tagLenIndex = tagNameIndex + tagName.length();
                        try {
                            tagLen = Integer.parseInt(StringUtil.convertFromAnyBaseString(W_ARQC.substring(tagLenIndex, tagLenIndex + 2), 16, 10, 0));
                        } catch (NumberFormatException e) {
                        }
                        // Tag之內容
                        tagValueIndex = tagNameIndex + tagName.length() + 2;
                        tagValue = W_ARQC.substring(tagValueIndex, tagValueIndex + tagLen * 2);
                        // 拆出來的長度和內容放入list中
                        for (String[] li : parts) {
                            if (tagName.equals(li[0])) {
                                li[1] = String.valueOf(tagLen * 2);
                                li[2] = tagValue;
                                break;
                            }
                        }
                        // icCheckData去掉已取的Tag
                        W_ARQC = W_ARQC.substring(tagName.length() + 2 + tagValue.length());
                    } else {
                        getLogContext().setRemark("Check_IC_CHECKDATA-拆解的TagName為Nothing)");
                        this.logMessage(getLogContext());
                    }
                } else {
                    // FORMAT ERROR
                    // 2016/10/05 Modify by Ruling for 1Byte Tag Name 找不到時要 Return ErrorCode
                    getLogContext().setRemark("Check_IC_CHECKDATA-Tag內容拆解錯誤");
                    this.logMessage(getLogContext());
                }
            }
            String tag95Value = "";
            String tag9F10Value = "";
            String tag95Len = "";
            String tag9F10Len = "";
            for (String[] part : parts) {
                String Name = part[0];
                String Len = part[1];

                if ("95".equals(Name)) {
                    tag95Value = tagValue;
                    tag95Len = Len;
                    break;  // 如果找到了符合條件的 Tag 值，就退出循環
                }
                if ("9F10".equals(Name)) {
                    tag9F10Value = tagValue;
                    tag9F10Len = Len;
                    break;
                }
            }

            String W_DATA = tag95Len + "01" + tag95Value + tag9F10Len + "01" + tag9F10Value;
            String W_LN = StringUtils.upperCase(StringUtils.leftPad(Integer.toHexString(W_DATA.length()), 4, "0")); //補滿4位
            String W_ICCHECKRESULT = "01" + W_LN + W_DATA;
            return W_ICCHECKRESULT;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".check_IC_CHECKDATA");
            sendEMS(getLogContext());
            return null;
        }
    }

    /**
     * 準備FEPTXN物件for EMV
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason></reason>
     * <date>2016/04/15</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareFEPTXN_EMV() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Zone twnZone = new Zone();
        Curcd curcd = new Curcd();
        String tempString = null;

        try {
            twnZone = getZoneByZoneCode(ZoneCode.TWN);
            if (twnZone == null) {
                return IOReturnCode.ZONENotFound;
            }

            rtnCode = prepareFeptxnFromHeader();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            getFeptxn().setFeptxnAtmno(fiscEMVICReq.getATMNO()); // 櫃員機代號
            getFeptxn().setFeptxnAtmnoVir(ATMNO_VIR);
            getFeptxn().setFeptxnAtmod(twnZone.getZoneCbsMode());
            getFeptxn().setFeptxnTxnmode(twnZone.getZoneCbsMode());
            tempString = StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 5, '0'); // spec change 2010-10-21
            getFeptxn().setFeptxnTxseq(tempString.substring(tempString.length() - 5, tempString.length()));
            getFeptxn().setFeptxnChannel(getFISCTxData().getTxChannel().toString()); // 通道別
            getFeptxn().setFeptxnMerchantId(fiscEMVICReq.getMerchantId()); // 商家代號

            // 交易幣別
            curcd = getAlpha3ByIsono3(fiscEMVICReq.getCURRENCY());
            if (curcd != null) {
                getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
            } else {
                getFeptxn().setFeptxnTxCur(CurrencyType.OTH.name());
            }

            // 交易金額
            if (!StringUtils.isBlank(fiscEMVICReq.getTxAmt()) && PolyfillUtil.isNumeric(fiscEMVICReq.getTxAmt())) {
                getFeptxn().setFeptxnTxAmt(new BigDecimal(fiscEMVICReq.getTxAmt()));
            }

            if (FISCPCode.PCode2633.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getFeptxn().setFeptxnTxAmtAct(new BigDecimal(fiscEMVICReq.getSetRpamt())); // 實際完成交易金額
                getFeptxn().setFeptxnMajorActno(fiscEMVICReq.getPanNo().trim()); // 卡號
            }

            getFeptxn().setFeptxnTxDatetimeFisc(fiscEMVICReq.getTxnInitiateDateAndTime()); // 交易日期時間

            // IC卡序號
            if (!StringUtils.isBlank(fiscEMVICReq.getCardSeq())) {
                getFeptxn().setFeptxnIcSeqno(fiscEMVICReq.getCardSeq());
            }

            getFeptxn().setFeptxnPinblock(fiscEMVICReq.getPINBLOCK()); // PINBLOCK
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());
            getFeptxn().setFeptxnTxrust("0");

            // 原交易序號
            if (!StringUtils.isBlank(fiscEMVICReq.getOriStan())) {
                getFeptxn().setFeptxnOriStan(fiscEMVICReq.getOriStan().substring(3, 10));
            }

            // 國際卡
            getFeptxn().setFeptxnTroutBkno(fiscEMVICReq.getTxnDestinationInstituteId().substring(0, 3)); // 支付錢的銀行
            if (!StringUtils.isBlank(fiscEMVICReq.getTRK2())) {
                getFeptxn().setFeptxnTrk2(fiscEMVICReq.getTRK2());
                BinExt binExt = getBin(getFeptxn().getFeptxnTrk2().substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
                if (binExt == null || (binExt != null && StringUtils.isBlank(binExt.getBinProd()))) {
                    getFeptxn().setFeptxnTroutActno("00" + getFeptxn().getFeptxnTrk2().substring(6, 9) + "0" + getFeptxn().getFeptxnTrk2().substring(9, 19));
                } else {
                    getFeptxn().setFeptxnTroutActno(getFeptxn().getFeptxnTrk2().substring(0, 16));
                    getFeptxn().setFeptxnTroutKind(binExt.getBinProd());
                    // 2020/11/09 Modify by Ruling for 多幣DEBIT卡
                    if (FISCPCode.PCode2630.getValueStr().equals(getFeptxn().getFeptxnPcode()) && binExt.isBinMulticur()) {
                        ((FeptxnExt) getFeptxn()).setFeptxnMulticur("Y"); // 多幣Debit卡跨國提款註記
                    }
                }
            }

            getFeptxn().setFeptxnZoneCode(ZoneCode.TWN); // 預設值
            getFeptxn().setFeptxnTxCurAct(CurrencyType.TWD.name()); // 預設值
            getFeptxn().setFeptxnTbsdy(twnZone.getZoneTbsdy());

            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                getFeptxn().setFeptxnWay((short) 3);
            }

            getLogContext().setAtmNo(getFeptxn().getFeptxnAtmno());
            getLogContext().setTrinActno(getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFeptxn().getFeptxnTrinBkno());
            getLogContext().setTroutActno(getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFeptxn().getFeptxnBkno());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXN_EMV");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 檢核MASTER卡BIN
     *
     * @return FEPReturnCode
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>New Function for MASTER CARD 2 BIN</reason>
     * <date>2017/01/04</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkMCBIN() {
        List<Mcbin> mcbins;
        boolean isMCBIN = false;
        String atcno = "";
        String bin = "";

        try {
            if (!getFeptxn().getFeptxnTrk2().contains("=")) {
                getLogContext().setRemark("CheckMCBIN-TRK2沒找到等於的符號，值為=" + getFeptxn().getFeptxnTrk2());
                this.logMessage(getLogContext());
                return FISCReturnCode.UPBinError;
            }

            atcno = StringUtils.rightPad(getFeptxn().getFeptxnTrk2(), 40, ' ').substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")).trim();
            bin = StringUtils.leftPad(atcno, 16, '0').substring(0, 6);

            mcbins = mcbinExtMapper.queryAllData("");
            if (mcbins != null && mcbins.size() > 0) {
                for (Mcbin dr : mcbins) {
                    if (bin.compareTo(dr.getMcbinFromBin()) >= 0 && bin.compareTo(dr.getMcbinToBin()) <= 0) {
                        isMCBIN = true;
                    }
                }
            } else {
                getLogContext().setRemark("CheckMCBIN-MCBIN檔查無資料");
                this.logMessage(getLogContext());
                return FEPReturnCode.QueryNoData;
            }

            if (isMCBIN) {
                getLogContext().setRemark("CheckMCBIN-此卡為MASTER BIN");
                this.logMessage(getLogContext());
                return FISCReturnCode.UPBinError;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkMCBIN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 準備FEPTXN物件-晶片金融卡跨國
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>新增晶片金融卡跨國提款及消費扣款交易</reason>
     * <date>2017/06/08</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareFEPTXN_IC() {
        FEPReturnCode rtncode = CommonReturnCode.Normal;
        Zone TWNZONE = new Zone();
        Bin bin = new BinExt();
        String tempString = null;

        try {
            TWNZONE = getZoneByZoneCode(ZoneCode.TWN);
            if (TWNZONE == null) {
                return IOReturnCode.ZONENotFound;
            }

            rtncode = prepareFeptxnFromHeader();
            if (rtncode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtncode;
            }
            getFeptxn().setFeptxnAtmno(fiscINBKReq.getATMNO()); // 櫃員機代號
            getFeptxn().setFeptxnAtmnoVir("90000"); // 虛擬櫃員機代號
            getFeptxn().setFeptxnAtmChk(fiscINBKReq.getAtmChk()); // 端末設備查核碼
            getFeptxn().setFeptxnAtmType(fiscINBKReq.getAtmType()); // 端末設備型態
            getFeptxn().setFeptxnAtmod(TWNZONE.getZoneCbsMode());
            getFeptxn().setFeptxnTxnmode(TWNZONE.getZoneCbsMode());

            /* 2025/2/17 點掉寫入 FEPTXN_TXSEQ */
//            tempString = StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 5, '0');
//            getFeptxn().setFeptxnTxseq(tempString.substring(tempString.length() - 5, 5));
            getFeptxn().setFeptxnChannel(getFISCTxData().getTxChannel().toString()); //通道別
            getFeptxn().setFeptxnMerchantId(fiscINBKReq.getMerchantId()); // 商家代號
            getFeptxn().setFeptxnOrderNo(fiscINBKReq.getOrderNo()); // 訂單號碼
            getFeptxn().setFeptxnRsCode(fiscINBKReq.getRsCode()); // 沖銷原因 or 全國性繳稅付費單位區分
            getFeptxn().setFeptxnTxDatetimeFisc(fiscINBKReq.getTxDatetimeFisc()); // 交易日期時間
            getFeptxn().setFeptxnFiscRrn(fiscINBKReq.getFiscRrn()); // 國際簽帳金融卡購物RRN

            if (!StringUtils.isBlank(fiscINBKReq.getTxAmt()) && PolyfillUtil.isNumeric(fiscINBKReq.getTxAmt())) {
                getFeptxn().setFeptxnTxAmtAct(new BigDecimal(fiscINBKReq.getTxAmt())); // 財金交易金額=結算金額
            }

            /* 2025/7/4修改, 儲存財金電文 ICMARK */
            if ( !StringUtils.isBlank(fiscINBKReq.getICMARK()) ){
                getFeptxn().setFeptxnIcmark(fiscINBKReq.getICMARK());
            }
//            // IC卡備註欄
//            if (StringUtils.isNotBlank(fiscINBKReq.getICMARK())) {
//                getFeptxn().setFeptxnIcmark(StringUtil.fromHex(fiscINBKReq.getICMARK()));
//                if (getFeptxn().getFeptxnIcmark().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
//                    getFeptxn().setFeptxnMajorActno(getFeptxn().getFeptxnIcmark().substring(0, 16));
//                } else {
//                    getFeptxn().setFeptxnMajorActno("00" + getFeptxn().getFeptxnIcmark().substring(0, 14));
//                }
//            }

            getFeptxn().setFeptxnIcSeqno(fiscINBKReq.getIcSeqno()); // IC卡序號
//            getFeptxn().setFeptxnIcTac(fiscINBKReq.getTAC()); // (原電文前4位是長度，已在 CheckBitMap 移除)
            getFeptxn().setFeptxnMsgid(getFISCTxData().getMsgCtl().getMsgctlMsgid());
            getFeptxn().setFeptxnTxrust("0");

            if (StringUtils.isNotBlank(fiscINBKReq.getOriStan())) {
                getFeptxn().setFeptxnOriStan(fiscINBKReq.getOriStan().substring(3, 10));
            }

            if (StringUtils.isNotBlank(fiscINBKReq.getTroutBkno())) {
                getFeptxn().setFeptxnTroutBkno(fiscINBKReq.getTroutBkno().substring(0, 3)); // 轉出行
            } else {
                getFeptxn().setFeptxnTroutBkno(fiscINBKReq.getTxnDestinationInstituteId().substring(0, 3)); // 支付錢的銀行
            }

            getFeptxn().setFeptxnTroutActno(fiscINBKReq.getTroutActno()); //轉出帳號
            /* 2025/3/13 修改 for 報表系統寫入卡片帳號 */
            getFeptxn().setFeptxnMajorActno(getFeptxn().getFeptxnTroutActno()); //卡片帳號

            /* 2024/9/11 點掉 */
//            if (getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno()) && !"00".equals(getFeptxn().getFeptxnTroutActno().substring(0, 2))) {
//                if ("0".equals(getFeptxn().getFeptxnTroutActno().substring(0, 1))) {
//                    // AE卡號15位, 帳號第1碼為"0", 讀取帳號第2~7碼
//                    bin.setBinNo(getFeptxn().getFeptxnTroutActno().substring(1, 7));
//                } else {
//                    // 其餘卡號第1碼不為0, 直接讀取帳號前6碼
//                    bin.setBinNo(getFeptxn().getFeptxnTroutActno().substring(0, 6));
//                }
//
//                bin = getBin(bin.getBinNo(), SysStatus.getPropertyValue().getSysstatHbkno());
//                if (bin != null && StringUtils.isNotBlank(bin.getBinProd())) {
//                    getFeptxn().setFeptxnTroutKind(bin.getBinProd());
//                }
//            }

            getFeptxn().setFeptxnZoneCode(ZoneCode.TWN); // 預設值
            getFeptxn().setFeptxnTxCurAct(CurrencyType.TWD.name()); // 預設值
            getFeptxn().setFeptxnTbsdy(TWNZONE.getZoneTbsdy());

            if (DbHelper.toBoolean(getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                getFeptxn().setFeptxnWay((short) 3);
            }

            getFeptxn().setFeptxnCbsProc(getFISCTxData().getMsgCtl().getMsgctlCbsProc());

            return rtncode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXN_IC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 準備國際卡的資料處理物件
     *
     * @return <history>
     * <modify>
     * <modifier>Henny</modifier>
     * <reason></reason>
     * <date>2010/4/13</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareIctltxn(RefBase<Ictltxn> ictlTxn, RefBase<Ictltxn> oriICTLTXN, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Curcd curcd = new Curcd();
        FISC_INBK fiscINBK = null;
        String wSetExrate = "";

        if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
            fiscINBK = fiscINBKReq;
        } else {
            fiscINBK = fiscINBKRes;
        }

        try {
            ictlTxn.get().setIctltxnTxDate(getFeptxn().getFeptxnTxDate());
            ictlTxn.get().setIctltxnEjfno(getFeptxn().getFeptxnEjfno());
            ictlTxn.get().setIctltxnBkno(getFeptxn().getFeptxnBkno());
            ictlTxn.get().setIctltxnStan(getFeptxn().getFeptxnStan());
            ictlTxn.get().setIctltxnPcode(getFeptxn().getFeptxnPcode());
            ictlTxn.get().setIctltxnAtmno(getFeptxn().getFeptxnAtmno());
            ictlTxn.get().setIctltxnTxTime(getFeptxn().getFeptxnTxTime());
            ictlTxn.get().setIctltxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            ictlTxn.get().setIctltxnReqRc(getFeptxn().getFeptxnReqRc());
            ictlTxn.get().setIctltxnRepRc(getFeptxn().getFeptxnRepRc());
            ictlTxn.get().setIctltxnConRc(getFeptxn().getFeptxnConRc());
            ictlTxn.get().setIctltxnTxrust(getFeptxn().getFeptxnTxrust());
            ictlTxn.get().setIctltxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            ictlTxn.get().setIctltxnTroutActno(getFeptxn().getFeptxnTroutActno());
            ictlTxn.get().setIctltxnTroutKind(getFeptxn().getFeptxnTroutKind());
            ictlTxn.get().setIctltxnBrno(getFeptxn().getFeptxnBrno());
            ictlTxn.get().setIctltxnZoneCode(getFeptxn().getFeptxnZoneCode());
            /* 2019/01/11 新增 for 晶片卡跨國提款/消費扣款沖正(2573/2549) */
            ictlTxn.get().setIctltxnTxDatetimeFisc(getFeptxn().getFeptxnTxDatetimeFisc());
            ictlTxn.get().setIctltxnMerchantId(getFeptxn().getFeptxnMerchantId());

            /* 拆解INBK.IC_DATA 電文欄位 */
            if (StringUtils.isBlank(fiscINBK.getIcData())) {
                return FISCReturnCode.OriginalMessageError;
            } else {
                rtnCode = checkIC_DATA(msgFlow);
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }
            }
            ictlTxn.get().setIctltxnAcqno(fiscINBK.getICDATA().getAcqNo());
            ictlTxn.get().setIctltxnAcqCntry(fiscINBK.getICDATA().getAcqCntry());
            ictlTxn.get().setIctltxnTxCur(fiscINBK.getICDATA().getTxCur());
            ictlTxn.get().setIctltxnSetExrate(fiscINBK.getICDATA().getSetExrate());

            if (!"0000000".equals(ictlTxn.get().getIctltxnSetExrate())) {
                int pointRight = Integer.parseInt(ictlTxn.get().getIctltxnSetExrate().trim().substring(0, 1)); // 第一位表示後續7位之小數位
                BigDecimal sWord = new BigDecimal(ictlTxn.get().getIctltxnSetExrate().trim().substring(1)); // 第二位～第八位表示要拆解的數值
                BigDecimal tenN = BigDecimal.valueOf(Math.pow(10, pointRight)); // 10的N次方
                wSetExrate = String.valueOf(sWord.divide(tenN));
                getFeptxn().setFeptxnExrate(new BigDecimal(wSetExrate));
            }

            ictlTxn.get().setIctltxnTwdFee(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getICDATA().getTwdFee()) / 100)); // 台方手續費
            ictlTxn.get().setIctltxnAcqFee(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getICDATA().getProcFee()) / 100)); // 收單方手續費
            ictlTxn.get().setIctltxnSetAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getICDATA().getSetAmt()) / 100)); // 台幣結算金額
            /* 2023/07/28 新增 for 韓國跨國消費扣款(2545) */
            ictlTxn.get().setIctltxnTxAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getICDATA().getTxAmt()) / 100));

            if (ictlTxn.get().getIctltxnTxCur().equals("840")) {
                getFeptxn().setFeptxnTxAmt(BigDecimal.valueOf(Double.valueOf(getFeptxn().getFeptxnMerchantId().substring(19, 27))));
                String W_TX_CUR = getFeptxn().getFeptxnMerchantId().substring(27, 30);
                curcd = getAlpha3ByIsono3(W_TX_CUR);
                if (curcd != null) {
                    getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
                }else {
                    getFeptxn().setFeptxnTxCur(ictlTxn.get().getIctltxnTxCur());
                }

            } else {
                curcd = getAlpha3ByIsono3(ictlTxn.get().getIctltxnTxCur());
                if (curcd != null) {
                    getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
                }else {
                    getFeptxn().setFeptxnTxCur(ictlTxn.get().getIctltxnTxCur());
                }
                getFeptxn().setFeptxnTxAmt(ictlTxn.get().getIctltxnTxAmt());
            }

            ictlTxn.get().setIctltxnIcStan(fiscINBK.getICDATA().getIcStan()); // 端末交易序號

            /* 借用欄位 for  T24主機電文往來明細 */
            getFeptxn().setFeptxnRemark(ictlTxn.get().getIctltxnAcqCntry() + ictlTxn.get().getIctltxnAcqno() + getFeptxn().getFeptxnAtmno());

            getFeptxn().setFeptxnFeeCur(getFeptxn().getFeptxnTxCurAct());
            getFeptxn().setFeptxnFeeCustpay(ictlTxn.get().getIctltxnAcqFee());

            if (FISCPCode.PCode2571.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2572.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getFeptxn().setFeptxnFeeCustpayAct(ictlTxn.get().getIctltxnTwdFee());  /* 台方手續費 */

            }
            getFeptxn().setFeptxnTxAmtSet(ictlTxn.get().getIctltxnSetAmt());
            getFeptxn().setFeptxnTxCurSet(getFeptxn().getFeptxnTxCurAct());
            /* 6/29 借用 FEPTXN_CASH_AMT 放台方手續費 */
            getFeptxn().setFeptxnCashAmt(ictlTxn.get().getIctltxnTwdFee());

            if (FISCPCode.PCode2572.getValueStr().equals(getFeptxn().getFeptxnPcode()) || FISCPCode.PCode2546.getValueStr().equals(getFeptxn().getFeptxnPcode())) { /* for跨國沖銷交易 */
                oriICTLTXN.get().setIctltxnTxDate(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
                oriICTLTXN.get().setIctltxnStan(getFeptxn().getFeptxnOriStan());
                Ictltxn ictltxnResult = ictltxnExtMapper.queryByOriData(oriICTLTXN.get().getIctltxnTxDate(), oriICTLTXN.get().getIctltxnStan());
                if (ictltxnResult == null) {
                    getLogContext().setRemark("找不到原交易ICTLTXN的資料");
                    this.logMessage(getLogContext());
                    /* 2025/2/19 修改for 晶片跨國沖正(2546/2572)查無原交易,送IMS主機 */
                    getFeptxn().setFeptxnCbsProc("Y");  //主機交易 -> Y:主機交易,N:FEP交易
                    return FISCReturnCode.OriginalMessageDataError;
                }
                oriICTLTXN.set(ictltxnResult);
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareIctltxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public String checkIC_CHECKDATA(String W_ARQCLN, String W_ARQC) {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String W_ICCHECKRESULT = "";
        try {
            if (String.valueOf(W_ARQC.length()).equals(W_ARQCLN)) {
                EMVTagData objEmvTag = new EMVTagData();
                objEmvTag = GetEachEMVTag(W_ARQC, objEmvTag);
                if (objEmvTag == null) {
                    return null;
                }
                int t95len = objEmvTag.getT95().length();
                int t9f10len = objEmvTag.getT9F10().length();
                W_ICCHECKRESULT = String.valueOf(t95len) + "01" + objEmvTag.getT95()
                        + String.valueOf(t9f10len) + "01" + objEmvTag.getT9F10();
            } else {
                return null;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkIC_DATA"));
            sendEMS(getLogContext());
        }
        return W_ICCHECKRESULT;
    }

    private EMVTagData GetEachEMVTag(String emvtag, EMVTagData objEmvTag) {
        int tagLength = 2;
        int emvTagPos = 0;
        String tag;
        int lengthLength;
        int dataLength = 0;
        String length = "";
        String data = "";
        RefInt refdataLength = new RefInt(dataLength);
        while (emvTagPos <= emvtag.length()) {
            tagLength = GetTagLength(emvtag.substring(emvTagPos));
            tag = emvtag.substring(emvTagPos, emvTagPos + tagLength);
            emvTagPos += tagLength;
            lengthLength = GetLengthLength(emvtag.substring(emvTagPos), refdataLength);
            dataLength = refdataLength.get();
            length = emvtag.substring(emvTagPos, emvTagPos + lengthLength);
            emvTagPos += lengthLength;
            data = emvtag.substring(emvTagPos, emvTagPos + dataLength);
            emvTagPos += dataLength;
            objEmvTag = ProcessEmvTagData(tag, data, objEmvTag);
            if (emvTagPos + 2 >= emvtag.length()) {
                return objEmvTag;
            }
        }
        return objEmvTag;
    }

    private EMVTagData ProcessEmvTagData(String tag, String data, EMVTagData objEmvTag) {
        switch (tag.toUpperCase()) {
            case "82":
                objEmvTag.setT82(data);
                break;
            case "95":
                objEmvTag.setT95(data);
                break;
            case "9A":
                objEmvTag.setT9A(data);
                break;
            case "9C":
                objEmvTag.setT9C(data);
                break;
            case "5F2A":
                objEmvTag.setT5F2A(data);
                break;
            case "9F02":
                objEmvTag.setT9F02(data);
                break;
            case "9F10":
                objEmvTag.setT9F10(data);
                break;
            case "9F1A":
                objEmvTag.setT9F1A(data);
                break;
            case "9F26":
                objEmvTag.setT9F26(data);
                break;
            case "9F33":
                objEmvTag.setT9F33(data);
                break;
            case "9F36":
                objEmvTag.setT9F36(data);
                break;
            case "9F37":
                objEmvTag.setT9F37(data);
                break;
            case "91":
                objEmvTag.setT91(data);
                break;
            case "71":
                objEmvTag.setT71(data);
                break;
            case "72":
                objEmvTag.setT72(data);
                break;
            default:
                break;
        }
        return objEmvTag;
    }

    private int GetLengthLength(String emvTag, RefInt dataLength) {
        int pos = 0;
        int lengthLength = 0;
        String fristLength = emvTag.substring(pos, pos + 2);
        byte fristLengthHex = ConvertUtil.intToByte(fristLength);
        if (fristLengthHex < 0x80) {
            lengthLength = 2;
            int data = Integer.valueOf(emvTag.substring(pos, pos + lengthLength), 16) * 2;
            dataLength.set(data);
        } else if (fristLengthHex == 0x81) {
            lengthLength = 2 + 2;
            int data = Integer.valueOf(emvTag.substring(pos, pos + lengthLength), 16) * 2;
            dataLength.set(data);
        } else if (fristLengthHex == 0x82) {
            lengthLength = 2 + 2 + 2;
            int data = Integer.valueOf(emvTag.substring(pos, pos + lengthLength), 16) * 2;
            dataLength.set(data);
        } else {
            lengthLength = 0;
            throw new RuntimeException("EMVTag Length:" + emvTag);
        }
        return lengthLength;
    }

    private int GetTagLength(String emvTag) {
        int pos = 0;
        int tagLength = 2;
        String tag = emvTag.substring(pos, pos + 2);
        byte tagHex = ConvertUtil.intToByte(tag);
        if ((tagHex & 0x1f) == 0x1f) {
            tagLength += 2;
            pos += 2;
            tag = emvTag.substring(pos, pos + 2);
            tagHex = ConvertUtil.intToByte(tag);
            while ((tagHex & 0x80) == 0x80) {
                tagLength += 2;
                pos += 2;
                tag = emvTag.substring(pos, pos + 2);
                tagHex = ConvertUtil.intToByte(tag);
                if (tagLength > 8) {
                    return 0;
                }
            }
        }
        return tagLength;
    }


    public class EMVTagData {
        private String T82;

        public String getT82() {
            return T82;
        }

        public void setT82(String t82) {
            T82 = t82;
        }

        public String getT95() {
            return T95;
        }

        public void setT95(String t95) {
            T95 = t95;
        }

        public String getT9A() {
            return T9A;
        }

        public void setT9A(String t9A) {
            T9A = t9A;
        }

        public String getT9C() {
            return T9C;
        }

        public void setT9C(String t9C) {
            T9C = t9C;
        }

        public String getT5F2A() {
            return T5F2A;
        }

        public void setT5F2A(String t5F2A) {
            T5F2A = t5F2A;
        }

        public String getT9F02() {
            return T9F02;
        }

        public void setT9F02(String t9F02) {
            T9F02 = t9F02;
        }

        public String getT9F10() {
            return T9F10;
        }

        public void setT9F10(String t9F10) {
            T9F10 = t9F10;
        }

        public String getT9F1A() {
            return T9F1A;
        }

        public void setT9F1A(String t9F1A) {
            T9F1A = t9F1A;
        }

        public String getT9F26() {
            return T9F26;
        }

        public void setT9F26(String t9F26) {
            T9F26 = t9F26;
        }

        public String getT9F33() {
            return T9F33;
        }

        public void setT9F33(String t9F33) {
            T9F33 = t9F33;
        }

        public String getT9F36() {
            return T9F36;
        }

        public void setT9F36(String t9F36) {
            T9F36 = t9F36;
        }

        public String getT9F37() {
            return T9F37;
        }

        public void setT9F37(String t9F37) {
            T9F37 = t9F37;
        }

        public String getT91() {
            return T91;
        }

        public void setT91(String t91) {
            T91 = t91;
        }

        public String getT71() {
            return T71;
        }

        public void setT71(String t71) {
            T71 = t71;
        }

        public String getT72() {
            return T72;
        }

        public void setT72(String t72) {
            T72 = t72;
        }

        private String T95;
        private String T9A;
        private String T9C;
        private String T5F2A;
        private String T9F02;
        private String T9F10;
        private String T9F1A;
        private String T9F26;
        private String T9F33;
        private String T9F36;
        private String T9F37;
        private String T91;
        private String T71;
        private String T72;
    }


    /**
     * 拆解IC_DATA
     *
     * @param msgFunction MessageFlow
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Ruling</modifier>
     *                    <reason></reason>
     *                    <date>2017/06/09</date>
     *                    </modify>
     *                    </history>
     * @return
     */
    public FEPReturnCode checkIC_DATA(MessageFlow msgFunction) {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_INBK fiscINBK = null;
        try {
            switch (msgFunction) {
                case Request:
                    fiscINBK = fiscINBKReq;
                    break;
                case Response:
                    fiscINBK = fiscINBKRes;
                    break;
                default:
                    fiscINBK = fiscINBKCon;
                    break;
            }
            fiscINBK.setICDATA(new DefIC_DATA());
            if (fiscINBK.getIcData().length() != 70) {
                return FISCReturnCode.LengthError;
            }
            fiscINBK.getICDATA().setAcqNo(fiscINBK.getIcData().substring(0, 8));
            fiscINBK.getICDATA().setAcqCntry(fiscINBK.getIcData().substring(8, 11));
            fiscINBK.getICDATA().setTxCur(fiscINBK.getIcData().substring(11, 14));
            fiscINBK.getICDATA().setSetExrate(fiscINBK.getIcData().substring(14, 22));
            fiscINBK.getICDATA().setTwdFee(fiscINBK.getIcData().substring(22, 29));
            fiscINBK.getICDATA().setProcFee(fiscINBK.getIcData().substring(29, 36));
            fiscINBK.getICDATA().setSetAmt(fiscINBK.getIcData().substring(36, 50));
            fiscINBK.getICDATA().setTxAmt(fiscINBK.getIcData().substring(50, 64));
            fiscINBK.getICDATA().setIcStan(fiscINBK.getIcData().substring(64, 70));
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkIC_DATA"));
            sendEMS(getLogContext());
            return FISCReturnCode.CheckBitMapError;
        } finally {
            if (fiscINBK != null)
                LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkIC_DATA][", fiscINBK.getClass().getSimpleName(), "]ICDATA Data : ", fiscINBK.getICDATA().toJSON());
        }
    }

    /**
     * 統計晶片卡跨國交易財金跨行代收付
     *
     * @param isEC 是否沖正
     *
     *             <history>
     *             <modify>
     *             <modifier>Ruling</modifier>
     *             <reason></reason>
     *             <date>2017/06/28</date>
     *             </modify>
     *             </history>
     * @return
     */
    public FEPReturnCode processICAptot(boolean isEC) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        AptotExtMapper aptotMapper = SpringBeanFactoryUtil.getBean(AptotExtMapper.class);
        int cnt_DR = 0;
        int cnt_CR = 0;
        int fee_Cnt_DR = 0;
        int fee_Cnt_CR = 0;
        @SuppressWarnings("unused")
        BigDecimal txAmt = new BigDecimal(0);
        BigDecimal amt_DR = new BigDecimal(0);
        BigDecimal amt_CR = new BigDecimal(0);
        BigDecimal fee_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Amt_CR = new BigDecimal(0);
        BigDecimal fee_Differ = new BigDecimal(0);
        BigDecimal fee_Differ1 = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_CR = new BigDecimal(0);
        @SuppressWarnings("unused")
        BigDecimal wk_PROFIT_LOSS = new BigDecimal(0);
        int icount = 0;

        txAmt = new BigDecimal(0);
        cnt_DR = 0;
        cnt_CR = 0;
        fee_Cnt_DR = 0;
        fee_Cnt_CR = 0;
        amt_DR = new BigDecimal(0);
        amt_CR = new BigDecimal(0);
        fee_Amt_DR = new BigDecimal(0);
        fee_Amt_CR = new BigDecimal(0);
        fee_Differ = new BigDecimal(0);
        fee_Differ1 = new BigDecimal(0);
        fee_Oth_Amt_DR = new BigDecimal(0);
        fee_Oth_Amt_CR = new BigDecimal(0);
        wk_PROFIT_LOSS = new BigDecimal(0);

        try {
            // 1 INBKPARM -- 跨行清算參數檔(包含手續費及本金清算設定)
            if (this.getInbkparm() == null) {
                this.setInbkparm(new Inbkparm());
                this.getInbkparm().setInbkparmCur(getFeptxn().getFeptxnTxCur());
                this.getInbkparm().setInbkparmApid(getFeptxn().getFeptxnPcode());
                this.getInbkparm().setInbkparmAcqFlag("I"); // 原存
                this.getInbkparm().setInbkparmEffectDate(getFeptxn().getFeptxnTbsdyFisc());
                this.getInbkparm().setInbkparmPcode(StringUtils.EMPTY);
                Inbkparm record = inbkparmExtMapper.queryByPK(this.getInbkparm());
                if (record == null) {
                    getLogContext().setRemark(StringUtils.join(
                            "CUR=", getInbkparm().getInbkparmCur(),
                            " APID=", getInbkparm().getInbkparmApid(),
                            " ACQ_FLAG=", getInbkparm().getInbkparmAcqFlag(),
                            " EFFECT_DATE=", getInbkparm().getInbkparmEffectDate(),
                            " PCODE=", getInbkparm().getInbkparmPcode()));
                    this.logMessage(getLogContext());
                    return FEPReturnCode.INBKPARMNotFound;
                }
                this.setInbkparm(record);
            }

            // 2 以下所有筆數或金額欄位預設值為0
            // 統計筆數金額
            switch (getInbkparm().getInbkparmPrncrdb()) {
                case "D": // 應收-借方
                    cnt_DR = 1;
                    amt_DR = getFeptxn().getFeptxnTxAmtAct();
                    break;
                case "C": // 應付-貸方
                    cnt_CR = 1;
                    amt_CR = getFeptxn().getFeptxnTxAmtAct();
                    break;
                default: // 不需記代收付帳者亦需記筆數 for 財金
                    if (getInbkparm().getInbkparmAcqFlag().equals("I")) {
                        cnt_CR = 1; // 原存交易記應付筆數
                    } else {
                        cnt_DR = 1; // 代理交易記應收筆數
                    }
                    break;
            }

            /* 2025/09/19 點掉手續費計算 */
//             手續費
//            if (!DbHelper.toBoolean(getInbkparm().getInbkparmFeeType())) {
//                // 固定金額
//                if (getInbkparm().getInbkparmFeeCustpay().doubleValue() > 0) {
//                    switch (getInbkparm().getInbkparmPrncrdb()) {
//                        case "C": // 應付財金手續費
//                            fee_Cnt_CR = 1;
//                            getFeptxn().setFeptxnActProfit(BigDecimal.valueOf(getInbkparm().getInbkparmFeeCustpay().doubleValue() * 20 / 100)); // 發卡行手續費收入100 * 20%
//                            getFeptxn().setFeptxnActProfit(MathUtil.roundUp(getFeptxn().getFeptxnActProfit(), 1)); // (小數第1位以下四捨五入)
//                            fee_Amt_CR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActProfit().doubleValue());
//                            break;
//                        case "D": // 應收財金手續費
//                            fee_Cnt_DR = 1;
//                            getFeptxn().setFeptxnActLoss(BigDecimal.valueOf(getInbkparm().getInbkparmFeeCustpay().doubleValue() * 20 / 100)); // 發卡行手續費支出100 * 20%
//                            getFeptxn().setFeptxnActLoss(MathUtil.roundUp(getFeptxn().getFeptxnActLoss(), 1)); // (小數第1位以下四捨五入)
//                            fee_Amt_DR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActLoss().doubleValue());
//                            break;
//                    }
//                }
//            } else {
//                // 2545
//                if (getInbkparm().getInbkparmFeeMbrDr().doubleValue() > 0) {
//                    fee_Cnt_CR = 1;
//                    // 發卡行手續費收入=台方手續費(台幣)*0.48/0.6
//                    getFeptxn().setFeptxnActProfit(MathUtil.roundUp((getFeptxn().getFeptxnCashAmt().doubleValue() * 48 / 100) * 100 / 60, 1)); // (小數第1位以下四捨五入)
//                    // 應付財金手續費
//                    fee_Amt_CR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActProfit().doubleValue());
//                }
//
//                // 2546
//                if (getInbkparm().getInbkparmFeeMbrCr().doubleValue() > 0) {
//                    fee_Cnt_DR = 1;
//                    // 發卡行手續費支出=台方手續費(台幣)*0.48/0.6
//                    getFeptxn().setFeptxnActLoss(MathUtil.roundUp((getFeptxn().getFeptxnCashAmt().doubleValue() * 48 / 100) * 100 / 60, 1)); // (小數第1位以下四捨五入)
//                    // 應收財金手續費
//                    fee_Amt_DR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActLoss().doubleValue());
//                }
//
//                // 2571
//                if (getInbkparm().getInbkparmFeeCustpay().doubleValue() > 0) {
//                    switch (getInbkparm().getInbkparmPrncrdb()) {
//                        case "C": {
//                            fee_Cnt_CR = 1;
//                            BigDecimal w_TOT_FEE = MathUtil.roundFloor((getFeptxn().getFeptxnTxAmt().doubleValue() * 8 / 1000) + 150, 0);
//                            if (w_TOT_FEE.compareTo(BigDecimal.valueOf(390)) < 0) {
//                                w_TOT_FEE = new BigDecimal(390);
//                            }
//                            fee_Differ = MathUtil.roundFloor(w_TOT_FEE.multiply(BigDecimal.valueOf(60 / 100.0)), 0); // 無條件捨去至元
//                            fee_Differ = MathUtil.roundUp(fee_Differ.multiply(getFeptxn().getFeptxnExrate()), 0); // 四捨五入至元
//                            fee_Differ1 = MathUtil.roundUp(w_TOT_FEE.multiply(getFeptxn().getFeptxnExrate()), 0); // 四捨五入至元
//                            // 發卡行手續費收入
//                            getFeptxn().setFeptxnActProfit(MathUtil.roundUp(fee_Differ1.subtract(fee_Differ).multiply(BigDecimal.valueOf(50 / 100.0)).doubleValue(), 1));
//                            // 應付財金手續費
//                            fee_Amt_CR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActProfit().doubleValue());
//                            break;
//                        }
//                        case "D": {
//                            fee_Cnt_DR = 1;
//                            BigDecimal w_TOT_FEE = MathUtil.roundFloor((getFeptxn().getFeptxnTxAmt().doubleValue() * 8 / 1000) + 150, 0);
//                            if (w_TOT_FEE.compareTo(BigDecimal.valueOf(390)) < 0) {
//                                w_TOT_FEE = new BigDecimal(390);
//                            }
//                            fee_Differ = MathUtil.roundFloor(w_TOT_FEE.multiply(BigDecimal.valueOf(60 / 100.0)), 0); // 無條件捨去至元
//                            fee_Differ = MathUtil.roundUp(fee_Differ.multiply(getFeptxn().getFeptxnExrate()), 0); // 四捨五入至元
//                            fee_Differ1 = MathUtil.roundUp(w_TOT_FEE.multiply(getFeptxn().getFeptxnExrate()), 0); // 四捨五入至元
//                            // 發卡行手續費支出
//                            getFeptxn().setFeptxnActLoss(MathUtil.roundUp(fee_Differ1.subtract(fee_Differ).multiply(BigDecimal.valueOf(50 / 100.0)).doubleValue(), 1));
//                            // 應收財金手續費
//                            fee_Amt_DR = BigDecimal.valueOf(getFeptxn().getFeptxnCashAmt().doubleValue() - getFeptxn().getFeptxnActLoss().doubleValue());
//                            break;
//                        }
//                    }
//                }
//            }

            /* 2025/09/19 點掉手續費計算 */
//            // 3 回寫 FEPTXN 手續費欄位
//            getFeptxn().setFeptxnTxFeeDr(fee_Amt_DR);
//            getFeptxn().setFeptxnTxFeeCr(fee_Amt_CR);
//            getFeptxn().setFeptxnTxFeeMbnkDr(fee_Oth_Amt_DR);
//            getFeptxn().setFeptxnTxFeeMbnkCr(fee_Oth_Amt_CR);
//            updateTxData();

            // 4 更正 APTOT
            Aptot aptot = new Aptot();
            // DBAPTOT dbAPTOT = new DBAPTOT(FEPConfig.DBName);
            String wkBRNO = null;
            String wk_APID;

            wk_APID = getInbkparm().getInbkparmApid();
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                wkBRNO = getFeptxn().getFeptxnBrno();
            } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                wkBRNO = getFeptxn().getFeptxnTrinBrno();
            } else {
                getFeptxn().getFeptxnAtmBrno();
            }

            aptot.setAptotStDate(getFeptxn().getFeptxnTbsdyFisc());
            aptot.setAptotApid(wk_APID);
            aptot.setAptotBrno(wkBRNO);
            aptot.setAptotAscFlag(DbHelper.toShort(false));

            if (!isEC) {
                // 非沖正(-CON)交易
                getFeptxn().setFeptxnClrType((short) 1); // 跨行清算
                // 更新(APTOT) 欄位及條件如下
                aptot.setAptotCntDr(cnt_DR);
                aptot.setAptotAmtDr(amt_DR);
                aptot.setAptotCntCr(cnt_CR);
                aptot.setAptotAmtCr(amt_CR);
                /* 2025/09/19 點掉手續費計算 */
//                aptot.setAptotFeeCntDr(fee_Cnt_DR);
//                aptot.setAptotFeeAmtDr(fee_Amt_DR.add(fee_Oth_Amt_DR));
//                aptot.setAptotFeeCntCr(fee_Cnt_CR);
//                aptot.setAptotFeeAmtCr(fee_Amt_CR.add(fee_Oth_Amt_CR));
            } else {
                // 沖正(-CON)交易，借貸別相反
                getFeptxn().setFeptxnClrType((short) 2); // REVERSE
                // 更新(APTOT) 欄位及條件如下
                aptot.setAptotEcCntDr(cnt_CR);
                aptot.setAptotEcAmtDr(amt_CR);
                aptot.setAptotEcCntCr(cnt_DR);
                aptot.setAptotEcAmtCr(amt_DR);
                /* 2025/09/19 點掉手續費計算 */
//                aptot.setAptotEcFeeCntDr(fee_Cnt_CR);
//                aptot.setAptotEcFeeAmtDr(fee_Amt_CR.add(fee_Oth_Amt_CR));
//                aptot.setAptotEcFeeCntCr(fee_Cnt_DR);
//                aptot.setAptotEcFeeAmtCr(fee_Amt_DR.add(fee_Oth_Amt_DR));
            }

            icount = aptotMapper.updateForRMProcessAPTOT(aptot);
            if (icount < 1) {
                // 包Catch Exception並判斷SQL錯誤代碼為2627才需再次Update APTOT
                try {
                    if (aptotMapper.insertSelective(aptot) < 1) {
                        getLogContext().setRemark("processICAptot-Insert APTOT 0 筆");
                        sendEMS(getLogContext());
                    }
                } catch (Exception ex) {
                    SQLException exSql = (SQLException) ex.getCause();
                    if (2627 == exSql.getErrorCode()) {
                        if (aptotMapper.updateForRMProcessAPTOT(aptot) < 1) {
                            getLogContext().setRemark("processICAptot-After Insert APTOT PK Violation, Update 0 筆");
                            sendEMS(getLogContext());
                        } else {
                            getLogContext().setRemark("processICAptot-After Insert APTOT PK Violation, Update 成功");
                            this.logMessage(getLogContext());
                        }
                    } else {
                        throw ex;
                    }

                }

            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processICAptot");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // Fly 2018/05/10 for 國外提款提醒及管控機制
    public FEPReturnCode checkOWDCount(String txdate, String actno, String time) throws Exception {
        List<Intltxn> intltxnList = intltxnExtMapper.selectForCheckOwdCount(txdate, time, SysStatus.getPropertyValue().getSysstatHbkno(), actno);
        if (CollectionUtils.isNotEmpty(intltxnList) && intltxnList.size() + 1 > Integer.parseInt(INBKConfig.getInstance().getForeignWithdrawDCnt())) {
            getLogContext().setRemark("國外提款超過當日累計限制次數");
            this.logMessage(getLogContext());
            prepareCARDTXN(getCard());
            FEPReturnCode rtn = null;

            getCard().setCardOwdGp("1");
            getCard().setCardOwdlimitDate(getFeptxn().getFeptxnTxDate());
            getCard().setCardOwdlimitTime(getFeptxn().getFeptxnTxTime());
            RefBase<FEPReturnCode> codeRefBase = new RefBase<>(rtn);
            updateCard(getCard(), codeRefBase); // Update Card
            rtn = codeRefBase.get();

            Owlimit defOWL = new Owlimit();
            defOWL.setOwlimitActno(getCard().getCardActno());
            defOWL.setOwlimitCardSeq(getCard().getCardCardSeq());
            defOWL.setOwlimitDate(getFeptxn().getFeptxnTxDate());
            owlimitMapper.insertSelective(defOWL);
            return FEPReturnCode.OverOWDCnt;
        }
        return FEPReturnCode.Normal;
    }

    /**
     * 收到財金發動之跨境電子支付資料寫入OBTLTXN
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>CASH OUTBOUND</reason>
     * <date>2018/07/24</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareObtltxn(RefBase<Obtltxn> obtlTxn, RefBase<Obtltxn> oriOBTLTXN, MessageFlow msgFlow) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_INBK fiscINBK = null;
        String wSetExrate = "";
        Curcd curcd = new Curcd();

        try {
            if (msgFlow.getValue() == MessageFlow.Request.getValue()) {
                fiscINBK = fiscINBKReq;
            } else {
                fiscINBK = fiscINBKRes;
            }

            obtlTxn.get().setObtltxnTxDate(getFeptxn().getFeptxnTxDate());
            obtlTxn.get().setObtltxnEjfno(getFeptxn().getFeptxnEjfno());
            obtlTxn.get().setObtltxnBkno(getFeptxn().getFeptxnBkno());
            obtlTxn.get().setObtltxnStan(getFeptxn().getFeptxnStan());
            obtlTxn.get().setObtltxnPcode(getFeptxn().getFeptxnPcode());
            obtlTxn.get().setObtltxnAtmno(getFeptxn().getFeptxnAtmno());
            obtlTxn.get().setObtltxnTxTime(getFeptxn().getFeptxnTxTime());
            obtlTxn.get().setObtltxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            obtlTxn.get().setObtltxnReqRc(getFeptxn().getFeptxnReqRc());
            obtlTxn.get().setObtltxnRepRc(getFeptxn().getFeptxnRepRc());
            obtlTxn.get().setObtltxnConRc(getFeptxn().getFeptxnConRc());
            obtlTxn.get().setObtltxnTxrust(getFeptxn().getFeptxnTxrust());
            obtlTxn.get().setObtltxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            obtlTxn.get().setObtltxnTroutActno(getFeptxn().getFeptxnTroutActno());
            obtlTxn.get().setObtltxnTroutKind(getFeptxn().getFeptxnTroutKind());
            obtlTxn.get().setObtltxnBrno(getFeptxn().getFeptxnBrno());
            obtlTxn.get().setObtltxnZoneCode(getFeptxn().getFeptxnZoneCode());
            obtlTxn.get().setObtltxnIcSeqno(getFeptxn().getFeptxnIcSeqno());
            obtlTxn.get().setObtltxnTxDatetimeFisc(getFeptxn().getFeptxnTxDatetimeFisc());

            /* 2019/7/17 增加寫入端末設備型態 */
            obtlTxn.get().setObtltxnTxDateFisc(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
            obtlTxn.get().setObtltxnAtmType(getFeptxn().getFeptxnAtmType());

            // 拆解跨境電子支付平台作業資料(bitmap36)
            if (StringUtils.isBlank(fiscINBK.getOriData())) {
                getLogContext().setRemark("檢查跨境電子支付平台作業資料(bitmap36)為NULL或空白");
                this.logMessage(getLogContext());
                return FISCReturnCode.OriginalMessageError;
            } else {
                rtnCode = checkOB_DATA(msgFlow);
                if (rtnCode.getValue() != FEPReturnCode.Normal.getValue()) {
                    return rtnCode;
                }
            }
            /* 拆解INBK.OB_DATA 電文欄位 */
            obtlTxn.get().setObtltxnMerchantId(fiscINBK.getOBDATA().getMerchantId()); // 特店代號
            obtlTxn.get().setObtltxnClosingDate(fiscINBK.getOBDATA().getClosingDate()); // 境外機構結帳日
            obtlTxn.get().setObtltxnTotTwdAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getTotTwdAmt()) / 100)); // 消費者台幣支付金額
            obtlTxn.get().setObtltxnTwnFee(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getTwnFee()) / 100)); // 台方手續費
            obtlTxn.get().setObtltxnSetAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getSetAmt()) / 100)); // 委託劃付銀行結算金額(外幣)
            obtlTxn.get().setObtltxnSetCur(fiscINBK.getOBDATA().getSetCur()); // 結算幣別碼
            obtlTxn.get().setObtltxnSetExrate(fiscINBK.getOBDATA().getSetExrate()); // 結算匯率

            if (!obtlTxn.get().getObtltxnSetExrate().equals("0000000")) {
                int pointRight = Integer.parseInt(obtlTxn.get().getObtltxnSetExrate().trim().substring(0, 1)); // 第一位表示後續7位之小數位
                BigDecimal sWord = new BigDecimal(obtlTxn.get().getObtltxnSetExrate().trim().substring(1)); // 第二位～第八位表示要拆解的數值
                BigDecimal tenN = BigDecimal.valueOf(Math.pow(10, pointRight)); // 10的N次方
                wSetExrate = String.valueOf(sWord.divide(tenN));
                getFeptxn().setFeptxnExrate(new BigDecimal(wSetExrate));
                obtlTxn.get().setObtltxnExrate(getFeptxn().getFeptxnExrate());
            }

            obtlTxn.get().setObtltxnOrderNo(fiscINBK.getOBDATA().getOrderNo()); // 訂單編號
            obtlTxn.get().setObtltxnRrn(fiscINBK.getOBDATA().getRRN()); // 追蹤序號
            /* 8/28修改, 退款交易時才寫入 */
            if (FISCPCode.PCode2556.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                obtlTxn.get().setObtltxnOriStan(fiscINBK.getOBDATA().getOriStan().substring(3, 10)); // 原交易序號
                obtlTxn.get().setObtltxnOriTxDate(CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscINBK.getOBDATA().getOriBusinessDate(), 7, '0'))); // 原營業日
                obtlTxn.get().setObtltxnOriOrderNo(fiscINBK.getOBDATA().getOriOrderNo()); // 原訂單編號
                getFeptxn().setFeptxnOriStan(obtlTxn.get().getObtltxnOriStan());
                getFeptxn().setFeptxnDueDate(obtlTxn.get().getObtltxnOriTxDate()); //轉成西元年
            }

            obtlTxn.get().setObtltxnTotTwdFee(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getTotTwdFee()) / 100)); // 台幣總手續費
            obtlTxn.get().setObtltxnTotForAmt(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getTotForAmt()) / 100)); // 外幣總金額
            obtlTxn.get().setObtltxnTotForFee(BigDecimal.valueOf(Double.parseDouble(fiscINBK.getOBDATA().getTotForFee()) / 100)); // 外幣總手續
            obtlTxn.get().setObtltxnYesfg(fiscINBK.getOBDATA().getYESFG()); // 使用者同意註記

            getFeptxn().setFeptxnMerchantId(obtlTxn.get().getObtltxnMerchantId());
//            getFeptxn().setFeptxnOrderNo(obtlTxn.get().getObtltxnOrderNo());
            getFeptxn().setFeptxnTxAmtSet(obtlTxn.get().getObtltxnTotTwdAmt()); // 消費者支付台幣總金額(含手續費)

            curcd = getAlpha3ByIsono3(obtlTxn.get().getObtltxnSetCur());
            if (curcd != null) {
                getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
            } else { /* 2025/8/25 修改 for 交易幣別不存在,改放交易幣別代碼  */
                getFeptxn().setFeptxnTxCur(obtlTxn.get().getObtltxnSetCur());
            }

            getFeptxn().setFeptxnTxAmtAct(getFeptxn().getFeptxnTxAmt());
            getFeptxn().setFeptxnTxAmt(obtlTxn.get().getObtltxnSetAmt()); // 委託劃付銀行結算金額(外幣)
            getFeptxn().setFeptxnFeeCur(CurrencyType.TWD.name());
            getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(obtlTxn.get().getObtltxnTotForAmt().intValue() - obtlTxn.get().getObtltxnSetAmt().intValue()));
            getFeptxn().setFeptxnFeeCustpayAct(obtlTxn.get().getObtltxnTwnFee());

            /* 6/29 借用 FEPTXN_CASH_AMT 放消費者支付台幣總金額(含手續費) */
            getFeptxn().setFeptxnCashAmt(obtlTxn.get().getObtltxnTotTwdAmt());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareObtltxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 拆解跨境電子支付平台作業資料(Bitmap 36)
     *
     * @param msgFunction MessageFlow
     *
     *                    <history>
     *                    <modify>
     *                    <modifier>Ruling</modifier>
     *                    <reason></reason>
     *                    <date>2018/07/24</date>
     *                    </modify>
     *                    </history>
     * @return
     */
    public FEPReturnCode checkOB_DATA(MessageFlow msgFunction) {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISC_INBK fiscINBK = null;
        try {
            switch (msgFunction) {
                case Request:
                    fiscINBK = fiscINBKReq;
                    break;
                case Response:
                    fiscINBK = fiscINBKRes;
                    break;
                default:
                    fiscINBK = fiscINBKCon;
                    break;
            }

            fiscINBK.setOBDATA(new FISC_INBK.DefOB_DATA());
            if (fiscINBK.getOriData().length() != 195) {
                getLogContext().setRemark("檢查跨境電子支付平台作業資料(bitmap36)總長度不足195位");
                this.logMessage(getLogContext());
                return FISCReturnCode.LengthError;
            }
            fiscINBK.getOBDATA().setMerchantId(fiscINBK.getOriData().substring(0, 15));
            fiscINBK.getOBDATA().setClosingDate(fiscINBK.getOriData().substring(15, 23));
            fiscINBK.getOBDATA().setTotTwdAmt(fiscINBK.getOriData().substring(23, 37));
            fiscINBK.getOBDATA().setTwnFee(fiscINBK.getOriData().substring(37, 44));
            fiscINBK.getOBDATA().setSetAmt(fiscINBK.getOriData().substring(44, 56));
            fiscINBK.getOBDATA().setSetCur(fiscINBK.getOriData().substring(56, 59));
            fiscINBK.getOBDATA().setSetExrate(fiscINBK.getOriData().substring(59, 67));
            fiscINBK.getOBDATA().setOrderNo(fiscINBK.getOriData().substring(67, 87));
            fiscINBK.getOBDATA().setRRN(fiscINBK.getOriData().substring(87, 99));
            fiscINBK.getOBDATA().setOriStan(fiscINBK.getOriData().substring(99, 109));
            fiscINBK.getOBDATA().setOriBusinessDate(fiscINBK.getOriData().substring(109, 115));
            fiscINBK.getOBDATA().setOriOrderNo(fiscINBK.getOriData().substring(115, 135));
            fiscINBK.getOBDATA().setTotTwdFee(fiscINBK.getOriData().substring(135, 142));
            fiscINBK.getOBDATA().setTotForAmt(fiscINBK.getOriData().substring(142, 154));
            fiscINBK.getOBDATA().setTotForFee(fiscINBK.getOriData().substring(154, 161));
            fiscINBK.getOBDATA().setYESFG(fiscINBK.getOriData().substring(161, 162));
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkOB_DATA"));
            sendEMS(getLogContext());
            return FISCReturnCode.CheckBitMapError;
        } finally {
            if (fiscINBK != null)
                LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkOB_DATA][", fiscINBK.getClass().getSimpleName(), "]OBDATA Data : ", fiscINBK.getOBDATA().toJSON());
        }
    }

    /**
     * 身分驗證狀況
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>CASH OUTBOUND</reason>
     * <date>2018/07/24</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode sendToP33() {
        getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(true));
        @SuppressWarnings("unused")
        String remoteSP = "LinkedCashOutbound.CashOutbound.dbo.uapi_GetIdentifyStatus";
        @SuppressWarnings("unused")
        int rcode = 0;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            getLogContext().setRemark("P33身分驗證開始");
            this.logMessage(getLogContext());
            @SuppressWarnings("unused")
            SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);

            try {
                getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(true));
            } catch (Exception ex) {
                if (ex.getMessage().indexOf("Timeout") >= 0) {
                    getFeptxn().setFeptxnRepRc("2999");
                    rtnCode = CommonReturnCode.HostResponseTimeout;
                    getLogContext().setRemark("P33身分驗證結束, 呼叫SP發生Timeout, FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc());
                    this.logMessage(getLogContext());
                } else {
                    getFeptxn().setFeptxnRepRc("4514");
                    rtnCode = CommonReturnCode.P33Maintain;
                    getLogContext().setRemark("P33身分驗證結束, 呼叫SP發生錯誤, FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc());
                    this.logMessage(getLogContext());
                }
                getLogContext().setProgramException(ex);
                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                sendEMS(getLogContext());
                return rtnCode;
            }

            getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(false));
//            getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(getFeptxn().getFeptxnAscRc(), FEPChannel.P33, FEPChannel.FISC, getLogContext()));
            this.logMessage(getLogContext());

            if (NormalRC.FISC_ATM_OK.equals(getFeptxn().getFeptxnRepRc())) {
                rtnCode = CommonReturnCode.Normal;
            } else {
                rtnCode = CommonReturnCode.CBSResponseError;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setRemark("P33身分驗證結束, 程式發生例外!!");
            this.logMessage(getLogContext());
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToP33");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 統計跨境電子支付交易財金跨行代收付
     *
     * @param isEC 是否沖正
     *
     *             <history>
     *             <modify>
     *             <modifier>Ruling</modifier>
     *             <reason></reason>
     *             <date>2018/08/09</date>
     *             </modify>
     *             </history>
     * @return
     */
    public FEPReturnCode processOBAptot(boolean isEC) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        int cnt_DR = 0;
        int cnt_CR = 0;
        int fee_Cnt_DR = 0;
        int fee_Cnt_CR = 0;
        @SuppressWarnings("unused")
        BigDecimal txAmt = new BigDecimal(0);
        BigDecimal amt_DR = new BigDecimal(0);
        BigDecimal amt_CR = new BigDecimal(0);
        BigDecimal fee_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Amt_CR = new BigDecimal(0);
        @SuppressWarnings("unused")
        BigDecimal fee_Differ = new BigDecimal(0);
        @SuppressWarnings("unused")
        BigDecimal fee_Differ1 = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_DR = new BigDecimal(0);
        BigDecimal fee_Oth_Amt_CR = new BigDecimal(0);
        @SuppressWarnings("unused")
        BigDecimal wk_PROFIT_LOSS = new BigDecimal(0);
        int icount = 0;

        txAmt = new BigDecimal(0);
        cnt_DR = 0;
        cnt_CR = 0;
        fee_Cnt_DR = 0;
        fee_Cnt_CR = 0;
        amt_DR = new BigDecimal(0);
        amt_CR = new BigDecimal(0);
        fee_Amt_DR = new BigDecimal(0);
        fee_Amt_CR = new BigDecimal(0);
        fee_Differ = new BigDecimal(0);
        fee_Differ1 = new BigDecimal(0);
        fee_Oth_Amt_DR = new BigDecimal(0);
        fee_Oth_Amt_CR = new BigDecimal(0);
        wk_PROFIT_LOSS = new BigDecimal(0);
        AptotExtMapper aptotMapper = SpringBeanFactoryUtil.getBean(AptotExtMapper.class);
        try {
            // 1 INBKPARM -- 跨行清算參數檔(包含手續費及本金清算設定)
            if (this.getInbkparm() == null) {
                this.setInbkparm(new Inbkparm());
                this.getInbkparm().setInbkparmCur(getFeptxn().getFeptxnTxCurAct());
                this.getInbkparm().setInbkparmApid(getFeptxn().getFeptxnPcode());
                this.getInbkparm().setInbkparmAcqFlag("I"); // 原存
                this.getInbkparm().setInbkparmEffectDate(getFeptxn().getFeptxnTbsdyFisc());
                this.getInbkparm().setInbkparmPcode(StringUtils.EMPTY);
                Inbkparm record = inbkparmExtMapper.queryByPK(this.getInbkparm());
                if (record == null) {
                    getLogContext().setRemark(StringUtils.join(
                            "CUR=", getInbkparm().getInbkparmCur(),
                            " APID=", getInbkparm().getInbkparmApid(),
                            " ACQ_FLAG=", getInbkparm().getInbkparmAcqFlag(),
                            " EFFECT_DATE=", getInbkparm().getInbkparmEffectDate(),
                            " PCODE=", getInbkparm().getInbkparmPcode()));
                    this.logMessage(getLogContext());
                    return FEPReturnCode.INBKPARMNotFound;
                }
                this.setInbkparm(record);
            }

            // 2 以下所有筆數或金額欄位預設值為0
            // 統計筆數金額
            switch (getInbkparm().getInbkparmPrncrdb()) {
                case "D": // 應收-借方
                    cnt_DR = 1;
                    amt_DR = getFeptxn().getFeptxnTxAmtAct();
                    break;
                case "C": // 應付-貸方
                    cnt_CR = 1;
                    amt_CR = getFeptxn().getFeptxnTxAmtAct();
                    break;
                default: // 不需記代收付帳者亦需記筆數 for 財金
                    if (getInbkparm().getInbkparmAcqFlag().equals("I")) {
                        cnt_CR = 1; // 原存交易記應付筆數
                    } else {
                        cnt_DR = 1; // 代理交易記應收筆數
                    }
                    break;
            }

            /* 2025/09/19 點掉手續費計算 */
//            // 手續費
//            if (DbHelper.toBoolean(getInbkparm().getInbkparmFeeType())) {
//                // 固定金額
//                // 應付財金手續費(2555:40%)
//                if (getInbkparm().getInbkparmFeeMbrDr().intValue() > 0) {
//                    fee_Cnt_CR = 1;
//                    // 發卡行手續費收入=台方手續費(台幣)*0.4/0.6
//                    getFeptxn().setFeptxnActProfit(MathUtil.roundUp((getFeptxn().getFeptxnFeeCustpayAct().doubleValue() * 40 / 100) * 100 / 60, 1)); // (小數第1位以下四捨五入)
//
//                    // 代扣營所稅=台方總手續費/6
//                    fee_Oth_Amt_CR = MathUtil.roundUp(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / 6, 1); // (小數第1位以下四捨五入)
//
//                    // 應付財金手續費=台方手續費-發卡行手續費收入
//                    fee_Amt_CR = BigDecimal.valueOf(getFeptxn().getFeptxnFeeCustpayAct().intValue() - getFeptxn().getFeptxnActProfit().intValue());
//                }
//
//                // 應收財金手續費(2556:40%)
//                if (getInbkparm().getInbkparmFeeMbrCr().intValue() > 0) {
//                    fee_Cnt_DR = 1;
//                    // 發卡行手續費支出=台方手續費(台幣)*0.4/0.6
//                    getFeptxn().setFeptxnActLoss(MathUtil.roundUp((getFeptxn().getFeptxnFeeCustpayAct().doubleValue() * 40 / 100) * 100 / 60, 1)); // (小數第1位以下四捨五入)
//
//                    // 代扣營所稅=台方總手續費/6
//                    fee_Amt_CR = MathUtil.roundUp(getFeptxn().getFeptxnFeeCustpayAct().doubleValue() / 6, 1); // (小數第1位以下四捨五入)
//
//                    // 應收財金手續費=台方手續費-發卡行手續費支出
//                    fee_Amt_DR = BigDecimal.valueOf(getFeptxn().getFeptxnFeeCustpayAct().intValue() - getFeptxn().getFeptxnActLoss().intValue());
//                }
//            } else {
//                // 無
//            }

            /* 2025/09/19 點掉手續費計算 */
//            // 3 回寫 FEPTXN 手續費欄位
//            getFeptxn().setFeptxnTxFeeDr(fee_Amt_DR);
//            getFeptxn().setFeptxnTxFeeCr(fee_Amt_CR);
//            getFeptxn().setFeptxnTxFeeMbnkDr(fee_Oth_Amt_DR);
//            getFeptxn().setFeptxnTxFeeMbnkCr(fee_Oth_Amt_CR);
            updateTxData();

            // 4 更正 APTOT
            Aptot aptot = new Aptot();
            String wkBRNO = null;
            String wk_APID;

            wk_APID = getInbkparm().getInbkparmApid();
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                wkBRNO = getFeptxn().getFeptxnBrno(); // 扣款掛帳分行
            } else if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno())) {
                wkBRNO = getFeptxn().getFeptxnTrinBrno(); // 入帳掛帳分行
            } else {
                getFeptxn().getFeptxnAtmBrno(); // ATM帳務分行
            }

            aptot.setAptotStDate(getFeptxn().getFeptxnTbsdyFisc());
            aptot.setAptotApid(wk_APID);
            aptot.setAptotBrno(wkBRNO);
            aptot.setAptotAscFlag(DbHelper.toShort(false));

            if (!isEC) {
                // 非沖正(-CON)交易
                getFeptxn().setFeptxnClrType((short) 1); // 跨行清算
                // 更新(APTOT) 欄位及條件如下
                aptot.setAptotCntDr(cnt_DR);
                aptot.setAptotAmtDr(amt_DR);
                aptot.setAptotCntCr(cnt_CR);
                aptot.setAptotAmtCr(amt_CR);
                /* 2025/09/19 點掉手續費計算 */
//                aptot.setAptotFeeCntDr(fee_Cnt_DR);
//                aptot.setAptotFeeAmtDr(fee_Amt_DR.add(fee_Oth_Amt_DR));
//                aptot.setAptotFeeCntCr(fee_Cnt_CR);
//                aptot.setAptotFeeAmtCr(fee_Amt_CR.add(fee_Oth_Amt_CR));
            } else {
                // 沖正(-CON)交易，借貸別相反
                getFeptxn().setFeptxnClrType((short) 2); // REVERSE
                // 更新(APTOT) 欄位及條件如下
                aptot.setAptotEcCntDr(cnt_CR);
                aptot.setAptotEcAmtDr(amt_CR);
                aptot.setAptotEcCntCr(cnt_DR);
                aptot.setAptotEcAmtCr(amt_DR);
                /* 2025/09/19 點掉手續費計算 */
//                aptot.setAptotEcFeeCntDr(fee_Cnt_CR);
//                aptot.setAptotEcFeeAmtDr(fee_Amt_CR.add(fee_Oth_Amt_CR));
//                aptot.setAptotEcFeeCntCr(fee_Cnt_DR);
//                aptot.setAptotEcFeeAmtCr(fee_Amt_DR.add(fee_Oth_Amt_DR));
            }

            icount = aptotMapper.updateForRMProcessAPTOT(aptot);
            if (icount < 1) {
                // 包Catch Exception並判斷SQL錯誤代碼為2627才需再次Update APTOT
                try {
                    if (aptotMapper.insertSelective(aptot) < 1) {
                        getLogContext().setRemark("ProcessAptot-Insert APTOT 0 筆");
                        sendEMS(getLogContext());
                    }
                } catch (Exception ex) {
                    SQLException exSql = (SQLException) ex.getCause();
                    if (2627 == exSql.getErrorCode()) {
                        if (aptotMapper.updateForRMProcessAPTOT(aptot) < 1) {
                            getLogContext().setRemark("processOBAptot-After Insert APTOT PK Violation, Update 0 筆");
                            sendEMS(getLogContext());
                        } else {
                            getLogContext().setRemark("processOBAptot-After Insert APTOT PK Violation, Update 成功");
                            this.logMessage(getLogContext());
                        }
                    } else {
                        throw ex;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processOBAptot");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // Fly 2018/08/06 For跨境電子支付 MAIL
    public FEPReturnCode prepareOBEMAIL(int msgType) {
//        Smsmsg def = new Smsmsg();
//        Smlparm defSML = new Smlparm();
//        def.setSmsmsgTxDate(getFeptxn().getFeptxnTxDate());
//        def.setSmsmsgEjfno(getFeptxn().getFeptxnEjfno());
//        def = smsmsgMapper.selectByPrimaryKey(def.getSmsmsgTxDate(), def.getSmsmsgEjfno());
//        if (def == null) {
//            getLogContext().setRemark("SMSMSG查無資料");
//            this.logMessage(getLogContext());
//            return FEPReturnCode.Normal;
//        }
//
//        if (StringUtils.isBlank(def.getSmsmsgEmail())) {
//            getLogContext().setRemark("無EMAIL");
//            this.logMessage(getLogContext());
//            return FEPReturnCode.Normal;
//        }
//
//        try {
//            def.setSmsmsgSend("Y");
//            if (smsmsgMapper.updateByPrimaryKey(def) <= 0) {
//                getLogContext().setRemark("更新SMSMSG異常，不送SMS");
//                this.logMessage(getLogContext());
//                return FEPReturnCode.Normal;
//            }
//        } catch (Exception ex) {
//            getLogContext().setRemark("更新SMSMSG異常，不送SMS  " + ex.toString());
//            this.logMessage(getLogContext());
//            return FEPReturnCode.Normal;
//        }
//
//        defSML.setSmlparmType("M");
//        defSML.setSmlparmSeqno(msgType);
//        Smlparm smlparm = smlparmMapper.selectByPrimaryKey(defSML.getSmlparmType(), defSML.getSmlparmSeqno());
//        if (smlparm != null) {
//            defSML = smlparm;
//        }
//        String mailBody = defSML.getSmlparmContent();
//        String subject = defSML.getSmlparmSubject();
//        String actno = def.getSmsmsgTroutActno().substring(2);
//        actno = actno.substring(0, 8) + "***" + actno.substring(11, 13) + "*";
//        StringBuilder sSQL = new StringBuilder(def.getSmsmsgTxDate() + def.getSmsmsgTxTime());
//        String time = sSQL.insert(12, ":").insert(10, ":").insert(8, " ").insert(6, "/").insert(4, "/").toString();
//        mailBody = mailBody.replace("[PARM1]", actno);
//        mailBody = mailBody.replace("[PARM2]", time);
//        mailBody = mailBody.replace("[PARM3]", FormatUtil.longFormat(def.getSmsmsgTxAmtAct().intValue(), "#,##0"));
//
//        TxHelper.sendMailHunter(defSML.getSmlparmProj(), def.getSmsmsgEmail(), defSML.getSmlparmFromname(), defSML.getSmlparmFromemail(), subject, mailBody, defSML.getSmlparmChannel(),
//                defSML.getSmlparmPgcode(), def.getSmsmsgIdno(), defSML.getSmlparmPriority().toString(), getLogContext());
        return FEPReturnCode.Normal;
    }

    // Fly 2018/08/20 For跨境電子支付 MAIL
    public FEPReturnCode prepareOBNotifyMAIL(String DateTime, String Stan, BigDecimal txAmount) {
        @SuppressWarnings("unused")
        String mailBody = null;
        @SuppressWarnings("unused")
        String subject = "【永豐跨境付款】待退款入戶";
        // EmailHandler mail = new EmailHandler();
        StringBuilder dateTime = new StringBuilder(DateTime);
        try {
            DateTime = dateTime.insert(12, ":").insert(10, ":").insert(8, " ").insert(6, "/").insert(4, "/").substring(0, 16);
            // Fly 2019/1/22 (週二) 上午 11:12 調整退貨MAIL格式
            mailBody = "親愛的帳務同仁您好," + "\r\n" + "【" + DateTime + "】有一筆「跨境付款」之退款交易，其財金交易序號為【" + Stan + "】，金額為新台幣" + FormatUtil.longFormat(txAmount.intValue(), "#,##0") + "元，請協助進行退款。";
            // todo mail.SendEMail(CMNConfig.Instance().getMailServerIP(), "", "", ATMPConfig.getInstance().getATMAlertMailFrom(), INBKConfig.Instance().getOBRMailTo(), "",
            // subject,mailBody.toString(), MailPriority.High);
            getLogContext().setRemark("銷戶後退款，寄送通知 EMail 成功");
            this.logMessage(getLogContext());
        } catch (Exception ex) {
            getLogContext().setRemark("銷戶後退款，寄送通知 EMail 失敗");
            this.logMessage(getLogContext());
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareOBNotifyMAIL");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return null;
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
    public FEPReturnCode prepareVATXN(RefBase<Vatxn> vatxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            vatxn.get().setVatxnTxDate(getFeptxn().getFeptxnTxDate());
            vatxn.get().setVatxnEjfno(getFeptxn().getFeptxnEjfno());
            vatxn.get().setVatxnBkno(getFeptxn().getFeptxnBkno());
            vatxn.get().setVatxnStan(getFeptxn().getFeptxnStan());
            vatxn.get().setVatxnPcode(getFeptxn().getFeptxnPcode());
            vatxn.get().setVatxnAtmno(getFeptxn().getFeptxnAtmno());
            vatxn.get().setVatxnTxTime(getFeptxn().getFeptxnTxTime());
            vatxn.get().setVatxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            vatxn.get().setVatxnReqRc(getFeptxn().getFeptxnReqRc());
            vatxn.get().setVatxnRepRc(getFeptxn().getFeptxnRepRc());
            vatxn.get().setVatxnConRc(getFeptxn().getFeptxnConRc());
            vatxn.get().setVatxnTxrust(getFeptxn().getFeptxnTxrust());
            vatxn.get().setVatxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            vatxn.get().setVatxnTroutActno(getFeptxn().getFeptxnTroutActno());
            vatxn.get().setVatxnTroutKind(null == getFeptxn().getFeptxnTroutKind() ? StringUtils.SPACE : getFeptxn().getFeptxnTroutKind());
            vatxn.get().setVatxnBrno(getFeptxn().getFeptxnBrno());
            vatxn.get().setVatxnZoneCode(getFeptxn().getFeptxnZoneCode());
            vatxn.get().setUpdateUserid(0);

            if (StringUtils.isBlank(fiscINBKReq.getMEMO())) {
                getLogContext().setRemark("缺少MEMO欄位");
                this.logMessage(getLogContext());
                return FEPReturnCode.MessageFormatError;
            }
            vatxn.get().setVatxnCate(fiscINBKReq.getMEMO().substring(0, 2)); /* 業務類別 */
            vatxn.get().setVatxnType(fiscINBKReq.getMEMO().substring(2, 4));  /* 交易類別 */
            getFeptxn().setFeptxnNoticeId(vatxn.get().getVatxnCate() + vatxn.get().getVatxnType());
            /* 借用 FEPTXN_NOTICE_ID 存入業務類別及交易類別 */

            switch (vatxn.get().getVatxnCate()) {
                case "01": /*業務類別(01):境內電子支付約定連結申請 */
                    vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(4, 14));
                    vatxn.get().setVatxnBusino(fiscINBKReq.getMEMO().substring(14, 22));
                    vatxn.get().setVatxnBusinessUnit(fiscINBKReq.getMEMO().substring(22, 30));
                    vatxn.get().setVatxnPaytype(fiscINBKReq.getMEMO().substring(30, 35));
                    vatxn.get().setVatxnFeeno(fiscINBKReq.getMEMO().substring(35, 39));
                    vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(39, 49));
                    vatxn.get().setVatxnPactno(fiscINBKReq.getMEMO().substring(49, 65));
                    vatxn.get().setVatxnActno(fiscINBKReq.getMEMO().substring(65, 81));
                    break;
                case "02": /*業務類別(02): 線上約定繳費 */
                    vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(4, 14));
                    vatxn.get().setVatxnBusino(fiscINBKReq.getMEMO().substring(14, 22));
                    vatxn.get().setVatxnBusinessUnit(fiscINBKReq.getMEMO().substring(22, 30));
                    vatxn.get().setVatxnPaytype(fiscINBKReq.getMEMO().substring(30, 35));
                    vatxn.get().setVatxnFeeno(fiscINBKReq.getMEMO().substring(35, 39));
                    vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(39, 49));
                    vatxn.get().setVatxnPactno(fiscINBKReq.getMEMO().substring(49, 65));
                    vatxn.get().setVatxnActno(fiscINBKReq.getMEMO().substring(65, 81));
                    vatxn.get().setVatxnInsno(fiscINBKReq.getMEMO().substring(81, 91));
                    /* 2025/7/10 修改, 將VATXN委託單位資料寫入 FEPTXN */
                    getFeptxn().setFeptxnBusinessUnit(vatxn.get().getVatxnBusinessUnit());
                    getFeptxn().setFeptxnPaytype(vatxn.get().getVatxnPaytype());
                    getFeptxn().setFeptxnPayno(vatxn.get().getVatxnFeeno());
                    break;
                case "10": /* 業務類別(10): 跨行金融帳戶資訊核驗 */
                    vatxn.get().setVatxnItem(fiscINBKReq.getMEMO().substring(4, 6)); /* 核驗項目 */
                    //20221020 ADD BY Candy 新增欄位
                    vatxn.get().setVatxnUse(fiscINBKReq.getMEMO().substring(88, 90)); // 2026/02/02 修正取值長度
                    switch (vatxn.get().getVatxnItem()) {
                        case "01": // 身份證號或外國人統一編號
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            break;
                        case "02": // 持卡人之行動電話號碼
                            vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(6, 16));
                            break;
                        case "03": // 持卡人之出生年月日
                            vatxn.get().setVatxnBirthday(fiscINBKReq.getMEMO().substring(6, 14));
                            break;
                        case "04": // 持卡人之住家電話號碼
                            vatxn.get().setVatxnHphone(fiscINBKReq.getMEMO().substring(6, 16));
                            break;
                        case "11": // 持卡人之身分證號及行動電話號碼
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(16, 26));
                            break;
                        case "12": // 持卡人之身分證號、行動電話號碼及出生年月
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(16, 26));
                            vatxn.get().setVatxnBirthday(fiscINBKReq.getMEMO().substring(26, 34));
                            break;
                        case "13": // 持卡人之身分證號、行動電話號碼及住家電話
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(16, 26));
                            vatxn.get().setVatxnHphone(fiscINBKReq.getMEMO().substring(26, 36));
                            break;
                        case "14": // 持卡人之身分證號、行動電話號碼、出生年月日及住家電話號碼
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            vatxn.get().setVatxnMobile(fiscINBKReq.getMEMO().substring(16, 26));
                            vatxn.get().setVatxnBirthday(fiscINBKReq.getMEMO().substring(26, 34));
                            vatxn.get().setVatxnHphone(fiscINBKReq.getMEMO().substring(34, 44));
                            break;
                        /* 2021/7/6 配合財金規格調整, 增加核驗項目 15 */
                        case "15": /* 持卡人之身分證號或營利事業統一編號 */
                            vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                            break;
                    }
                    if (!"VAA".equals(getFeptxn().getFeptxnTxCode())) {
                        /* 原存金融帳戶資訊核驗交易 */
                        /* 2019/01/24 增加晶片金融卡驗TAC */
                        if ("00".equals(vatxn.get().getVatxnType())) {
                            getFeptxn().setFeptxnTrk3(fiscINBKReq.getMEMO());
                        }
                    } else {
                        /* 代理金融帳戶資訊核驗交易, 將財金回應電文存入 */
                        getFeptxn().setFeptxnTrk3(fiscINBKRes.getMEMO());
                        vatxn.get().setVatxnResult(fiscINBKRes.getMEMO().substring(90, 92));
                        vatxn.get().setVatxnAcresult(fiscINBKRes.getMEMO().substring(92, 94));
                        vatxn.get().setVatxnAcstat(fiscINBKRes.getMEMO().substring(94, 96));
                        /* 2019/1/3 修改, 將財金回應結果寫入 */
                        getFeptxn().setFeptxnRemark(vatxn.get().getVatxnResult() + vatxn.get().getVatxnAcresult() + vatxn.get().getVatxnAcstat());
                    }
                    break;
                case "11": /* 業務類別(11): 統一發票中獎獎金入帳帳號核驗 */
                    vatxn.get().setVatxnItem(fiscINBKReq.getMEMO().substring(4, 6));
                    vatxn.get().setVatxnIdno(fiscINBKReq.getMEMO().substring(6, 16));
                    getFeptxn().setFeptxnFeeCustpayAct(BigDecimal.valueOf(0)); /* 無手續費 */
                    break;
                case "20": /*業務類別(20): 線上申辦金融服務 */
                    vatxn.get().setVatxnApitem(fiscINBKReq.getMEMO().substring(4, 7)); /* 申辦類型 */
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
                case "01": // 業務類別(01):境內電子支付約定連結申請
                case "02": // 業務類別(02): 線上約定繳費
                    vatxn.get().setVatxnIdno(tita.getBody().getRq().getSvcRq().getTAXIDNO());
                    vatxn.get().setVatxnBusino(tita.getBody().getRq().getSvcRq().getSENDDATA().getCLCPYCI());
                    vatxn.get().setVatxnBusinessUnit(tita.getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
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
                            getFeptxn().setFeptxnTrk3(fiscINBKReq.getMEMO());
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

    /**
     * 收到財金發動之QRP被掃交易資料寫入QRPTXN
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>豐錢包APP直連財金QRP安控機制：收到財金發動之QRP被掃交易資料寫入QRPTXN</reason>
     * <date>2019/07/22</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode prepareQRPTXN(RefBase<Qrptxn> qrpTxn) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            qrpTxn.get().setQrptxnTxDate(getFeptxn().getFeptxnTxDate());
            qrpTxn.get().setQrptxnEjfno(getFeptxn().getFeptxnEjfno());
            qrpTxn.get().setQrptxnTxDateFisc(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
            qrpTxn.get().setQrptxnIcSeqno(getFeptxn().getFeptxnIcSeqno());
            qrpTxn.get().setQrptxnTxDatetimeFisc(getFeptxn().getFeptxnTxDatetimeFisc());
            qrpTxn.get().setQrptxnBkno(getFeptxn().getFeptxnBkno());
            qrpTxn.get().setQrptxnStan(getFeptxn().getFeptxnStan());
            qrpTxn.get().setQrptxnPcode(getFeptxn().getFeptxnPcode());
            qrpTxn.get().setQrptxnAtmno(getFeptxn().getFeptxnAtmno());
            qrpTxn.get().setQrptxnTxTime(getFeptxn().getFeptxnTxTime());
            qrpTxn.get().setQrptxnTbsdyFisc(getFeptxn().getFeptxnTbsdyFisc());
            qrpTxn.get().setQrptxnReqRc(getFeptxn().getFeptxnReqRc());
            qrpTxn.get().setQrptxnRepRc(getFeptxn().getFeptxnRepRc());
            qrpTxn.get().setQrptxnConRc(getFeptxn().getFeptxnConRc());
            qrpTxn.get().setQrptxnTxrust(getFeptxn().getFeptxnTxrust());
            qrpTxn.get().setQrptxnTroutBkno(getFeptxn().getFeptxnTroutBkno());
            qrpTxn.get().setQrptxnTroutActno(getFeptxn().getFeptxnTroutActno());
            qrpTxn.get().setQrptxnTroutKind(getFeptxn().getFeptxnTroutKind());
            qrpTxn.get().setQrptxnBrno(getFeptxn().getFeptxnBrno());
            qrpTxn.get().setQrptxnZoneCode(getFeptxn().getFeptxnZoneCode());
            qrpTxn.get().setQrptxnMerchantId(getFeptxn().getFeptxnMerchantId()); // 特店代號
            qrpTxn.get().setQrptxnOrderNo(getFeptxn().getFeptxnOrderNo()); // 訂單編號
            qrpTxn.get().setQrptxnAtmType(getFeptxn().getFeptxnAtmType()); // 端末設備型態
            qrpTxn.get().setQrptxnTxCur(getFeptxn().getFeptxnTxCur());
            qrpTxn.get().setQrptxnTxAmt(getFeptxn().getFeptxnTxAmt());
            qrpTxn.get().setQrptxnTxAmtAct(getFeptxn().getFeptxnTxAmtAct());

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareQRPTXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核消費扣款被掃交易是否交易逾時
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>豐錢包APP直連財金QRP安控機制：增加檢核消費扣款被掃交易是否交易逾時</reason>
     * <date>2019/07/10</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkQRPTimeOut() {
        try {
            String txDteTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            String fDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            String txdate = null;
            String txdate_fisc = null;

            // 檢核[財金交易日期時間]格式
            if (getFeptxn().getFeptxnTxDatetimeFisc().length() == 14) {
                txdate_fisc = getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 4) + "/" + getFeptxn().getFeptxnTxDatetimeFisc().substring(4, 6) + "/"
                        + getFeptxn().getFeptxnTxDatetimeFisc().substring(6, 8) + " " + getFeptxn().getFeptxnTxDatetimeFisc().substring(8, 10) + ":"
                        + getFeptxn().getFeptxnTxDatetimeFisc().substring(10, 12) + ":" + getFeptxn().getFeptxnTxDatetimeFisc().substring(12, 14);
            } else {
                getLogContext().setRemark("消費扣款被掃交易，財金交易日期時間長度不足14位, 財金交易日期時間=" + getFeptxn().getFeptxnTxDatetimeFisc());
                this.logMessage(getLogContext());
                return FEPReturnCode.CheckFieldError;
            }
            if (!CalendarUtil.validateDateTime(txdate_fisc, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS)) {
                getLogContext().setRemark("消費扣款被掃交易，轉換財金交易日期時間格式有誤, 財金交易日期時間=" + getFeptxn().getFeptxnTxDatetimeFisc());
                this.logMessage(getLogContext());
                return FEPReturnCode.CheckFieldError;
            }
            fDateTime = txdate_fisc;
            // 檢核[交易日期時間]格式
            if ((getFeptxn().getFeptxnTxDate() + getFeptxn().getFeptxnTxTime()).length() == 14) {
                txdate = getFeptxn().getFeptxnTxDate().substring(0, 4) + "/" + getFeptxn().getFeptxnTxDate().substring(4, 6) + "/" + getFeptxn().getFeptxnTxDate().substring(6, 8) + " "
                        + getFeptxn().getFeptxnTxTime().substring(0, 2) + ":" + getFeptxn().getFeptxnTxTime().substring(2, 4) + ":" + getFeptxn().getFeptxnTxTime().substring(4, 6);
            } else {
                getLogContext().setRemark("消費扣款被掃交易，交易日期時間長度不足14位, 交易日期=" + getFeptxn().getFeptxnTxDate() + " 交易時間=" + getFeptxn().getFeptxnTxTime());
                this.logMessage(getLogContext());
                return FEPReturnCode.CheckFieldError;
            }
            if (!CalendarUtil.validateDateTime(txdate, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS)) {
                getLogContext().setRemark("消費扣款被掃交易，轉換交易日期時間格式有誤, 交易日期=" + getFeptxn().getFeptxnTxDate() + " 交易時間=" + getFeptxn().getFeptxnTxTime());
                this.logMessage(getLogContext());
                return FEPReturnCode.CheckFieldError;
            }
            txDteTime = txdate;
            // 檢核交易逾時
            long difSecond = Math.abs(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(fDateTime).getTime() - new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(txDteTime).getTime());
            if (difSecond > INBKConfig.getInstance().getQRPExpireTime()
                    || (Double.parseDouble(getFeptxn().getFeptxnTxDatetimeFisc()) > (Double.parseDouble(getFeptxn().getFeptxnTxDate()) + Double.parseDouble(getFeptxn().getFeptxnTxTime())))) {
                getLogContext().setRemark("購貨時間已超過" + INBKConfig.getInstance().getQRPExpireTime() + "秒或財金交易日期時間大於交易日期時間，交易日期=" + getFeptxn().getFeptxnTxDate() + ", 交易時間="
                        + getFeptxn().getFeptxnTxTime() + ", 財金交易日期時間=" + getFeptxn().getFeptxnTxDatetimeFisc() + ", 差" + difSecond + "秒");
                this.logMessage(getLogContext());
                return FISCReturnCode.MerchantDateError;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkQRPTimeOut");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核電子支付QRP被掃交易序號是否重複
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>豐錢包APP直連財金QRP安控機制：檢核電子支付QRP被掃交易序號是否重複</reason>
     * <date>2019/07/22</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkQRPSeqDup() {
        try {
            Qrptxn qrptxn = new Qrptxn();
            qrptxn.setQrptxnTxDateFisc(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
            qrptxn.setQrptxnIcSeqno(getFeptxn().getFeptxnIcSeqno());
            List<Qrptxn> record = qrptxnExtMapper.getQrptxnByIcSeqno(qrptxn.getQrptxnTxDateFisc(), qrptxn.getQrptxnIcSeqno());
            if (CollectionUtils.isNotEmpty(record)) {
                getLogContext().setRemark("CheckQRPSeqDup-晶片卡交易序號重複");
                this.logMessage(getLogContext());
                return FEPReturnCode.ICSeqNoDuplicate; // 8104:晶片卡交易序號重複
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkQRPSeqDup"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 檢核電子支付QRP交易是否重複
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>豐錢包APP直連財金QRP安控機制：檢核電子支付QRP交易是否重複</reason>
     * <date>2019/08/19</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkOBQRPDup() {
        Obtltxn defOBTLTXN = new Obtltxn();
        try {
            defOBTLTXN.setObtltxnTxDateFisc(getFeptxn().getFeptxnTxDatetimeFisc().substring(0, 8));
            defOBTLTXN.setObtltxnAtmType(getFeptxn().getFeptxnAtmType());
            defOBTLTXN.setObtltxnTroutActno(getFeptxn().getFeptxnTroutActno());
            defOBTLTXN.setObtltxnIcSeqno(getFeptxn().getFeptxnIcSeqno());
            defOBTLTXN.setObtltxnOrderNo(getFeptxn().getFeptxnOrderNo());

            List<Obtltxn> obtltxns = obtltxnExtMapper.getQrptxnByIcSeqno(
                    defOBTLTXN.getObtltxnTxDateFisc(),
                    defOBTLTXN.getObtltxnAtmType(),
                    defOBTLTXN.getObtltxnTroutActno(),
                    defOBTLTXN.getObtltxnIcSeqno(),
                    defOBTLTXN.getObtltxnOrderNo());
            if (CollectionUtils.isNotEmpty(obtltxns)) {
                getLogContext().setRemark(StringUtils.join(
                        "CheckOBQRPDup-晶片卡交易序號重複, 財金交易日期=", defOBTLTXN.getObtltxnTxDateFisc(),
                        ", 端末設備型態=", defOBTLTXN.getObtltxnAtmType(),
                        ", QRP交易序號=", defOBTLTXN.getObtltxnIcSeqno(),
                        ", 訂單編號=", defOBTLTXN.getObtltxnOrderNo()));
                this.logMessage(getLogContext());
                return FEPReturnCode.ICSeqNoDuplicate; // 8104:晶片卡交易序號重複
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkOBQRPDup");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 中文->財金電文
     *
     * @param strChineseCode
     * @param strVfiscDecode
     * @return <history>
     * <modify>
     * <modifier>Maxine</modifier>
     * <reason>add</reason>
     * <date>2010/5/10</date>
     * </modify>
     * </history>
     */
    public boolean convertFiscEncode(String strChineseCode, RefString strVfiscDecode) {
        SyscomConverter.EncodingType sourceEnc = null;
        byte[] sb = null;

        try {
            TRACELOGGER.info(
                    StringUtils.join("FISC_RM-CONVERT_FiscEncode, Decoded Chinese string = [", strChineseCode, "]"));
            // Jim, 2012/5/11, 用永豐的編碼(配合新的SyscomConverter)
            sourceEnc = SyscomConverter.EncodingType.Unicode; // 2026/01/28 從永豐編碼修正成Unicode
            sb = ConvertUtil.toBytes(strChineseCode, PolyfillUtil.toCharsetName("Unicode"));
            strVfiscDecode.set(SyscomConverter.convertToHexString(sourceEnc, SyscomConverter.EncodingType.Cns11643, sb));
            TRACELOGGER.info(
                    StringUtils.join("FISC_RM-CONVERT_FiscEncode, chinese HexString = [", strVfiscDecode.get(), "]"));
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(getLogContext());
            return false;
        }

        return true;
    }
    /**
     * 中文->財金電文 使用字霸方法
     *
     * @param strChineseCode
     * @param strVfiscDecode
     */
    public boolean convertFiscEncodebyAstar(String strChineseCode, RefString strVfiscDecode, int cnsLength) {
        byte[] sb = null;
        byte[] resultBytes;
        int byteLimit = cnsLength / 2; // 因為回傳的是 Hex String，所以 Byte 長度減半

        try {
            // Joe 根據合庫需求，改使用字霸方法轉CNS
            sb = AStarLoadUtils.convertUnicodeStrToCNSStr(strChineseCode);

            if (sb.length > byteLimit) {
                StringBuilder hexBuilderSb = new StringBuilder();
                for (byte b : sb) {
                    hexBuilderSb.append(String.format("%02X", b));
                }
                resultBytes = new byte[byteLimit];
                System.arraycopy(sb, 0, resultBytes, 0, byteLimit);
                // 檢查狀態：如果在截斷點還有未閉合的 0E (Shift-Out)
                boolean isOpen = false;
                for (int i = 0; i < byteLimit; i++) {
                    if (resultBytes[i] == 0x0E) isOpen = true;
                    if (resultBytes[i] == 0x0F) isOpen = false;
                }
                if (isOpen) {
                    // 最後一個字沒閉合會導致亂碼，強制補上 0F，並處理前一個 Byte 避免中斷半個中文字
                    resultBytes[byteLimit - 1] = 0x0F;
                }
            } else {
                resultBytes = sb;
            }
            StringBuilder strChinesehex = new StringBuilder();
            for (byte b : resultBytes) {
                strChinesehex.append(String.format("%02X", b));
            }
            strVfiscDecode.set(StringUtils.rightPad(strChinesehex.toString(), cnsLength, "0"));
        } catch (Exception ex) {
            strVfiscDecode.set(StringUtils.repeat("0", cnsLength));
            return true;
        }
        return true;
    }

    /**
     * 檢核數位帳戶單筆限額
     *
     * @return <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>數位帳戶上線後調整</reason>
     * <date>2019/08/19</date>
     * </modify>
     * </history>
     */
    public FEPReturnCode checkDGLimit(String digiType) {
        Dglimit defDGLIMIT = new Dglimit();
        // Tables.DBDGLIMIT dbDGLIMIT = new Tables.DBDGLIMIT(FEPConfig.DBName);
        try {
            defDGLIMIT.setDglimitDgfg(digiType);
            defDGLIMIT.setDglimitPcode(getFeptxn().getFeptxnPcode());
            defDGLIMIT = dglimitMapper.selectByPrimaryKey(defDGLIMIT.getDglimitDgfg(), defDGLIMIT.getDglimitPcode());
            if (defDGLIMIT == null) {
                getLogContext().setRemark("跨境付款交易，未設定數位帳戶單筆限額");
                this.logMessage(getLogContext());
                return FEPReturnCode.OtherCheckError;
            } else {
                if (getFeptxn().getFeptxnTxAmtSet().doubleValue() > defDGLIMIT.getDglimitTxlimit().doubleValue()) {
                    getLogContext().setRemark("CheckDGLimit-數" + digiType + "帳戶，跨境付款交易單筆不得超過" + defDGLIMIT.getDglimitTxlimit().toString() + "元");
                    this.logMessage(getLogContext());
                    return FEPReturnCode.OverLimit; // 4201:超過單筆限額
                }
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkDGLimit");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 財金電文->中文
     *
     * @param strChineseCode
     * @param strVfiscDecode
     * @return
     * @author Richard
     */
    public boolean convertFiscDecode(String strChineseCode, RefString strVfiscDecode) {
        SyscomConverter.EncodingType sourceEnc = null;
        byte[] sb = null;
        String tmpstr = null;
        try {
            TRACELOGGER.info(
                    StringUtils.join("FISC_RM-CONVERT_FiscDecode, input chinese HexString = [", strChineseCode, "]"));

            sourceEnc = SyscomConverter.EncodingType.Cns11643;
            sb = SyscomConverter.getBytesFromHexString(strChineseCode);
            // Jim, 2012/5/11, 用永豐的編碼(配合新的SyscomConverter)
            tmpstr = SyscomConverter.convertToHexString(sourceEnc, SyscomConverter.EncodingType.BSP, sb);
            strVfiscDecode.set(ConvertUtil.toString(SyscomConverter.getBytesFromHexString(tmpstr),
                    PolyfillUtil.toCharsetName("Unicode")));
            // Fly 2016/05/23 小優DB長度限制為39的全形字
            strVfiscDecode.set(TxHelper.subStr(strVfiscDecode.get(), 0, 78));
            TRACELOGGER.info(
                    StringUtils.join("FISC_RM-CONVERT_FiscDecode, Decoded Chinese string = [", strVfiscDecode.get(), "]"));
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(getLogContext());
            return false;
        }
        return true;
    }
    
	public FEPReturnCode checkBitmapFromCBS(FISCHeader mfiscHeader, String apdata) {
		fiscHeader = mfiscHeader;
		if(!"10".equals(fiscHeader.getMessageType().substring(2, 4)) ) {
			return checkBitmapFromCBS(apdata);
		}
		return checkBitmap(apdata);
	}
	
	public FEPReturnCode checkBitmapFromCBS(String apdata) {
	  String pCode = fiscHeader.getProcessingCode();
      // 讀取財金電文BITMAP定義
      Bitmapdef oBitMap = getBitmapData(fiscHeader.getMessageType() + pCode);
      // 讀取財金電文AP DATA ELEMENT定義
      List<Dataattr> dvAttr = getDataAttributeDataByType(oBitMap.getBitmapdefType().toString());
      //手動記錄要跳過檢查交易時間的 pcode
      List<String> noCheckPcodeList = new ArrayList<>();
      noCheckPcodeList.add("2521");
      noCheckPcodeList.add("2532");

      int k = 0;
      FISC_OPC fiscOPC = new FISC_OPC();
      FISC_INBK fiscINBK = new FISC_INBK();
      FISC_CLR fiscCLR = new FISC_CLR();
      FISC_USDCLR fiscFCCLR = new FISC_USDCLR();
      FISC_EMVIC fiscEMVIC = new FISC_EMVIC();
      FEPReturnCode rtnCode = CommonReturnCode.Normal; // add by henny for 紀錄rc
      int i = 0;

      try {
          // 將財金Bitmap Hex轉2進位
          char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(fiscHeader.getBitMapConfiguration(), 16, 2, 64).toCharArray();
          switch (fiscHeader.getMessageType().substring(2, 4)) {
              case "00": /* Request 電文 */
              case "81":
              case "99":
                  switch (getFISCTxData().getFiscTeleType()) {
                      case INBK:
                          fiscINBK = fiscINBKReq;
                          break;
                      case OPC:
                          fiscOPC = fiscOPCReq;
                          break;
                      case CLR:
                          fiscCLR = fiscCLRReq;
                          break;
                      case FCCLR:
                          fiscFCCLR = fiscFCCLRReq;
                          break;
                      case EMVIC:
                          fiscEMVIC = fiscEMVICReq;
                          break;
                      default:
                    	  break;
                  }
                  break;
              case "02":
                  switch (getFISCTxData().getFiscTeleType()) {
                      case INBK:
                          fiscINBK = fiscINBKCon;
                          break;
                      case OPC:
                          fiscOPC = fiscOPCCon;
                          break;
                      case EMVIC:
                          fiscEMVIC = fiscEMVICCon;
                          break;
                      default:
                    	  break;
                  }
                  break;
              case "10":
                  switch (getFISCTxData().getFiscTeleType()) {
                      case INBK:
                          fiscINBK = fiscINBKRes;
                          break;
                      case OPC:
                          fiscOPC = fiscOPCRes;
                          break;
                      case CLR:
                          fiscCLR = fiscCLRRes;
                          break;
                      case FCCLR:
                          fiscFCCLR = fiscFCCLRRes;
                          break;
                      case EMVIC:
                          fiscEMVIC = fiscEMVICRes;
                          break;
                      default:
                    	  break;
                  }
                  break;
          }
          // 依AP DATA ELEMENT 定義拆解財金電文
          for (i = 0; i < bitMapFromFisc.length; i++) {
              if (bitMapFromFisc[i] == '1') {
                  // Bitmap on 開始處理
                  int tmplen = 0;
                  String tmpHex = "";
                  String tmpAscii = "";
                  String dataType = dvAttr.get(i).getDataattrDatatype().toString();
                  /* 判斷資料型態, 抓取欄位長度 */
                  if (dataType.equalsIgnoreCase("C") || dataType.equalsIgnoreCase("B")) {
                      /* 變動長度欄位 */
                      /* 變動長度從電文取前2個BYTE為此欄位長度L(2) */
                      /* 拆解欄位內容(tmpHex)= DATA */
                      tmplen = Integer.parseInt(StringUtil.convertFromAnyBaseString(apdata.substring(k, k + 4), 16, 10, 0)) * 2;
                      tmpHex = apdata.substring(k + 4, k + 4 + tmplen - 4);
                  } else {
                      /* 固定長度欄位 */
                      tmplen = dvAttr.get(i).getDataattrHexLen().intValue();
                      tmpHex = apdata.substring(k, k + tmplen);
                  }

                  /*  判斷是否需要 Hex轉成 ASCII  */
                  if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                      if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
                    	  //EMVIC TRK2 特別處理
                    	  if(i == 3 && getFISCTxData().getFiscTeleType().equals(FISCSubSystem.EMVIC)) {
                    		  int trk2Length = tmpHex.length();
                    		  String newStr = "";
                    		  for(int start = 0; start < trk2Length;  start += 2) {
                    			  String ns = tmpHex.substring(start, start + 2);
                    			  if(StringUtils.equals(ns, "FD")) {
                    				  ns = "7E";
                    			  }
                    			  newStr += ns;
                    		  }
                    		  tmpHex = newStr;
                    	  }
                          tmpAscii = EbcdicConverter.fromHex(CCSID.English, tmpHex);
                      }else{
                          tmpAscii = StringUtil.fromHex(tmpHex);
                      }
                  }

                  k = k + tmplen;

                  /* 晶片卡交易(25XX), 需檢核TAC長度要大於等於10,小於等於130 */
                  if ("25".equals(pCode.substring(0, 2))) {
                      if (i == 56) {  /* 交易驗證碼 */
                          if (tmplen < 10 || tmplen > 130) {
                              getLogContext().setRemark("ChkBitmap(TAC) 第" + (i+1 + "位訊息格式錯誤,TAC長度錯誤!"));
                              getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                              getLogContext().setReturnCode(FISCReturnCode.CheckBitMapError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                              logMessage(Level.ERROR, getLogContext());
                              return FISCReturnCode.CheckBitMapError;
                          }
                      }
                  }

                  /* 依資料型態(DATATTR_DATATYPE), 拆解及檢核電文欄位 */
                  switch (dataType) {
                      case "9": /* 資料型態=數字 */
                          if (!PolyfillUtil.isNumeric(tmpAscii)) {
                              getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                              getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                              getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                              logMessage(Level.ERROR, getLogContext());
                              rtnCode = FISCReturnCode.MessageFormatError;
                          } else {
                              /* 欄位拆解正確, 搬入系統別財金電文 */
                              switch (getFISCTxData().getFiscTeleType()) {
                                  case INBK:
                                      fiscINBK.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case OPC:
                                      fiscOPC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case CLR:
                                      fiscCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case FCCLR:
                                      fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case EMVIC:
                                      fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  default:
                                	  break;
                              }
                          }
                          break;
                      case "M": /* 資料型態=金額 */
                          if (!PolyfillUtil.isNumeric(tmpAscii) || "+-".indexOf(tmpAscii.substring(0, 1)) < 0) {
                              getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                              getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                              getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                              logMessage(Level.ERROR, getLogContext());
                              rtnCode = FISCReturnCode.MessageFormatError;
                          } else {
                              /* 欄位拆解正確, 搬入系統別財金電文 */
                              switch (getFISCTxData().getFiscTeleType()) {
                                  case INBK:
                                      fiscINBK.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                      break;
                                  case OPC:
                                      fiscOPC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                      break;
                                  case CLR:
                                      fiscCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                      break;
                                  case FCCLR:
                                      fiscFCCLR.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                      break;
                                  case EMVIC:
                                      fiscEMVIC.setGetPropertyValue(i, new BigDecimal(tmpAscii).divide(BigDecimal.valueOf(100)).toString());
                                      break;
                                  default:
                                	  break;
                              }
                          }
                          break;
                      case "A": /* 資料型態=日期 */
                          if (CalendarUtil.adStringToADDate(tmpAscii) == null && !noCheckPcodeList.contains(pCode)) {
                              getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                              getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                              getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                              logMessage(Level.ERROR, getLogContext());
                              rtnCode = FISCReturnCode.MessageFormatError;
                          } else {
                              /* 欄位拆解正確, 搬入系統別財金電文 */
                              switch (getFISCTxData().getFiscTeleType()) {
                                  case INBK:
                                      fiscINBK.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case OPC:
                                      fiscOPC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case CLR:
                                      fiscCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case FCCLR:
                                      fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case EMVIC:
                                      fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  default:
                                	  break;
                              }
                          }
                          break;
                      case "D": /* 資料型態=民國日期 */
                          if (CalendarUtil.rocStringToADDate("0" + tmpAscii) == null) {
                              getLogContext().setRemark("ChkBitmap第" + (i+1 + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                              getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                              getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                              logMessage(Level.ERROR, getLogContext());
                              rtnCode = FISCReturnCode.MessageFormatError;
                          } else {
                              /* 欄位拆解正確, 搬入系統別財金電文 */
                              switch (getFISCTxData().getFiscTeleType()) {
                                  case INBK:
                                      fiscINBK.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case OPC:
                                      fiscOPC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case CLR:
                                      fiscCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case FCCLR:
                                      fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  case EMVIC:
                                      fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                      break;
                                  default:
                                	  break;
                              }
                          }
                          break;
                      default: /* 資料型態=一般文字 */
                          if (dvAttr.get(i).getDataattrTransferflag().equalsIgnoreCase("Y")) {
                              /* 需要 Hex轉成 ASCII  */
                              if (DbHelper.toBoolean(dvAttr.get(i).getDataattrEncoding())) {
                                  /* 需要中文轉碼, 轉CNS11643 */
                                  switch (getFISCTxData().getFiscTeleType()) {
                                      case INBK:
                                          fiscINBK.setGetPropertyValue(i, tmpHex);
                                          break;
                                      case OPC:
                                          fiscOPC.setGetPropertyValue(i, tmpHex);
                                          break;
                                      case CLR:
                                          fiscCLR.setGetPropertyValue(i, tmpHex);
                                          break;
                                      case FCCLR:
                                          fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                          break;
                                      case EMVIC:
                                          fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                          break;
                                      default:
                                    	  break;
                                  }
                              } else {
                                  /* DATAATTR_ENCODING = False,不需要中文轉碼 */
                                  switch (getFISCTxData().getFiscTeleType()) {
                                      case INBK:
                                          fiscINBK.setGetPropertyValue(i, tmpAscii);
                                          break;
                                      case OPC:
                                          fiscOPC.setGetPropertyValue(i, tmpAscii);
                                          break;
                                      case CLR:
                                          fiscCLR.setGetPropertyValue(i, tmpAscii);
                                          break;
                                      case FCCLR:
                                          fiscFCCLR.setGetPropertyValue(i, tmpAscii);
                                          break;
                                      case EMVIC:
                                          fiscEMVIC.setGetPropertyValue(i, tmpAscii);
                                          break;
                                      default:
                                    	  break;
                                  }
                              }
                          } else { /* DATAATTR_TRANSFERFLAG=’N’ */
                              /* 不需要 Hex轉成 ASCII  */
                              switch (getFISCTxData().getFiscTeleType()) {
                                  case INBK:
                                      fiscINBK.setGetPropertyValue(i, tmpHex);
                                      break;
                                  case OPC:
                                      fiscOPC.setGetPropertyValue(i, tmpHex);
                                      break;
                                  case CLR:
                                      fiscCLR.setGetPropertyValue(i, tmpHex);
                                      break;
                                  case FCCLR:
                                      fiscFCCLR.setGetPropertyValue(i, tmpHex);
                                      break;
                                  case EMVIC:
                                      fiscEMVIC.setGetPropertyValue(i, tmpHex);
                                      break;
                                  default:
                                	  break;
                              }
                          }
                          break;
                  }
              }
          }
          return rtnCode;
      } catch (Exception ex) {
          getLogContext().setProgramException(ex);
          getLogContext().setRemark("拆解第" + (i + "位發生異常,Exception:" + ex.getMessage()));
          getLogContext().setProgramName(ProgramName + ".checkBitmapFromCBS");
          logMessage(Level.ERROR, getLogContext());
          sendEMS(getLogContext());
          return FISCReturnCode.CheckBitMapError;
      }
  }
	
    public FEPReturnCode checkBitmapFromCBSToIMS(FISCHeader mfiscHeader, String apdata) {
        fiscHeader = mfiscHeader;
        return checkBitmapFromCBSForFiscToIMS(apdata);
    }
	
	public FEPReturnCode sendToFISCFromCBS() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		String messageToFISC = getFISCTxData().getTxRequestMessage();
		String fiscTeleType = getFISCTxData().getFiscTeleType().toString();
		Bitmapdef oBitMap = null;
		String MessageType = "";
		String Pcode = "";
		
		try {
            Sysstat defSYSSTAT = new Sysstat();
            defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
            defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
			
			if(getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                switch (fiscTeleType) {
                    case "INBK":
                        MessageType = fiscINBKReq.getMessageType();
        				Pcode = fiscINBKReq.getProcessingCode();
        				if (defSYSSTAT != null) {
        					//SYSSTATT3DESSYNC
        					fiscINBKReq.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
        					if(StringUtils.isNotBlank(fiscINBKReq.getPINBLOCK())) {
        						if(StringUtils.equals("2510", Pcode)) {
        							fiscINBKReq.setSyncPpkey(defSYSSTAT.getSysstatT3dessync());
        						}
        					}
        					makeBitmap(fiscINBKReq.getMessageType(), fiscINBKReq.getProcessingCode(), MessageFlow.Request);
        					fiscINBKReq.makeFISCMsg();
        					messageToFISC = fiscINBKReq.getFISCMessage();
        					//messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                    case "OPC":
                        MessageType = fiscOPCReq.getMessageType();
        				Pcode = fiscOPCReq.getProcessingCode();
        				if (defSYSSTAT != null) {
        					messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTopcsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                    case "CLR":
                        MessageType = fiscCLRReq.getMessageType();
        				Pcode = fiscCLRReq.getProcessingCode();
                        break;
                    case "EMVIC":
                        MessageType = fiscEMVICReq.getMessageType();
        				Pcode = fiscEMVICReq.getProcessingCode();
        				if (defSYSSTAT != null) {
        					fiscEMVICReq.setSyncCheckItem(defSYSSTAT.getSysstatTcdsync());
        					makeBitmap(fiscEMVICReq.getMessageType(), fiscEMVICReq.getProcessingCode(), MessageFlow.Request);
        					fiscEMVICReq.makeFISCMsg();
        					messageToFISC = fiscEMVICReq.getFISCMessage();
        					//messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                }
			}else if(getFISCTxData().getMessageFlowType() == MessageFlow.Confirmation) {
                switch (fiscTeleType) {
                    case "INBK":
                        MessageType = fiscINBKCon.getMessageType();
        				Pcode = fiscINBKCon.getProcessingCode();
        				if (defSYSSTAT != null) {
        					messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                    case "OPC":
                        MessageType = fiscOPCCon.getMessageType();
        				Pcode = fiscOPCCon.getProcessingCode();
        				if (defSYSSTAT != null) {
        					messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTopcsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                    case "EMVIC":
                        MessageType = fiscEMVICCon.getMessageType();
        				Pcode = fiscEMVICCon.getProcessingCode();
        				if (defSYSSTAT != null) {
        					messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
        	            } else {
        	                getLogContext().setRemark("SYSSTAT Not Found");
        	                this.logMessage(getLogContext());
        	                return FEPReturnCode.SYSSTATNotFound;
        	            }
                        break;
                }
            }
			
			oBitMap = getBitmapData(MessageType + Pcode);
			FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
			// 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            
			if (getFeptxn() == null) {
				return FISCReturnCode.FISCTimeout;
			} else {
				if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
					return FISCReturnCode.FISCTimeout;
				} else {
					fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
				}
			}
			fiscAdapter.setMessageToFISC(messageToFISC);
			fiscAdapter.setNoWait(false);
			fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout());
			rtnCode = fiscAdapter.sendReceive();
			if(rtnCode != FEPReturnCode.Normal) {
				rtnCode = FISCReturnCode.FISCTimeout;
			}else {
				// this.logContext.setMessage("MessageFlowType:" + getFISCTxData().getMessageFlowType());
		        // logMessage(this.logContext);
				if(getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
					if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
						// this.logContext.setMessage("Message From FISC:" + fiscAdapter.getMessageFromFISC());
				        // logMessage(this.logContext);
				        
				        switch (fiscTeleType) {
	                    case "INBK":
	                    	fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
			                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
			                	if (oBitMap.getBitmapdefTimeout() == 0) {
			                        rtnCode = CommonReturnCode.Normal;
			                    } else {
			                        rtnCode = FISCReturnCode.FISCTimeout;
			                    }
			                } else {
			                    fiscINBKRes.parseFISCMsg();
			                }
	                        break;
	                    case "OPC":
	                    	fiscOPCRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
			                if (StringUtils.isBlank(fiscOPCRes.getFISCMessage())) {
			                	if (oBitMap.getBitmapdefTimeout() == 0) {
			                        rtnCode = CommonReturnCode.Normal;
			                    } else {
			                        rtnCode = FISCReturnCode.FISCTimeout;
			                    }
			                } else {
			                	fiscOPCRes.parseFISCMsg();
			                }
	                        break;
	                    case "CLR":
	                    	fiscCLRRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
			                if (StringUtils.isBlank(fiscCLRRes.getFISCMessage())) {
			                	if (oBitMap.getBitmapdefTimeout() == 0) {
			                        rtnCode = CommonReturnCode.Normal;
			                    } else {
			                        rtnCode = FISCReturnCode.FISCTimeout;
			                    }
			                } else {
			                	fiscCLRRes.parseFISCMsg();
			                }
	                        break;
	                    case "EMVIC":
	                    	fiscEMVICRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
			                if (StringUtils.isBlank(fiscEMVICRes.getFISCMessage())) {
			                	if (oBitMap.getBitmapdefTimeout() == 0) {
			                        rtnCode = CommonReturnCode.Normal;
			                    } else {
			                        rtnCode = FISCReturnCode.FISCTimeout;
			                    }
			                } else {
			                	fiscEMVICRes.parseFISCMsg();
			                }
	                        break;
				        }
						
					}
				}else {
					 getFeptxn().setFeptxnAaRc(rtnCode.getValue());
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToFISCFromCBS"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

    public FEPReturnCode sendToFISCFromCBSRespons() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        String messageToFISC = getFISCTxData().getTxRequestMessage();
        String fiscTeleType = getFISCTxData().getFiscTeleType().toString();
        Bitmapdef oBitMap = null;
        String MessageType = "";
        String Pcode = "";

        try {
            Sysstat defSYSSTAT = new Sysstat();
            defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
            defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());

            if(getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                switch (fiscTeleType) {
                    case "INBK":
                        MessageType = fiscINBKReq.getMessageType();
                        Pcode = fiscINBKReq.getProcessingCode();
                        if (defSYSSTAT != null) {
                            if ( !StringUtils.equals(Pcode, "2252") ) {
                                messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
                            }
                        } else {
                            getLogContext().setRemark("SYSSTAT Not Found");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.SYSSTATNotFound;
                        }
                        break;
                    case "OPC":
                        MessageType = fiscOPCReq.getMessageType();
                        Pcode = fiscOPCReq.getProcessingCode();
                        if (defSYSSTAT != null) {
                            messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTopcsync() + messageToFISC.substring(104);
                        } else {
                            getLogContext().setRemark("SYSSTAT Not Found");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.SYSSTATNotFound;
                        }
                        break;
                    case "CLR":
                        MessageType = fiscCLRReq.getMessageType();
                        Pcode = fiscCLRReq.getProcessingCode();
                        if (defSYSSTAT != null) {
                            messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTrmsync() + messageToFISC.substring(104);
                        } else {
                            getLogContext().setRemark("SYSSTAT Not Found");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.SYSSTATNotFound;
                        }
                        break;
                    case "EMVIC":
                        MessageType = fiscEMVICReq.getMessageType();
                        Pcode = fiscEMVICReq.getProcessingCode();
                        break;
                }
            }else if(getFISCTxData().getMessageFlowType() == MessageFlow.Confirmation) {
                switch (fiscTeleType) {
                    case "INBK":
                        MessageType = fiscINBKCon.getMessageType();
                        Pcode = fiscINBKCon.getProcessingCode();
                        if (defSYSSTAT != null) {
                            messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
                        } else {
                            getLogContext().setRemark("SYSSTAT Not Found");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.SYSSTATNotFound;
                        }
                        break;
                    case "OPC":
                        MessageType = fiscOPCCon.getMessageType();
                        Pcode = fiscOPCCon.getProcessingCode();
                        break;
                    case "EMVIC":
                        MessageType = fiscEMVICCon.getMessageType();
                        Pcode = fiscEMVICCon.getProcessingCode();
                        if (defSYSSTAT != null) {
                            messageToFISC = messageToFISC.substring(0, 96) + defSYSSTAT.getSysstatTcdsync() + messageToFISC.substring(104);
                        } else {
                            getLogContext().setRemark("SYSSTAT Not Found");
                            this.logMessage(getLogContext());
                            return FEPReturnCode.SYSSTATNotFound;
                        }
                        break;
                }
            }

            oBitMap = getBitmapData(MessageType + Pcode);
            FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());

            if (getFeptxn() == null) {
                return FISCReturnCode.FISCTimeout;
            } else {
                if (StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
                    return FISCReturnCode.FISCTimeout;
                } else {
                    fiscAdapter.setStan(getFeptxn().getFeptxnStan()); // FISCTxData.Stan
                }
            }
            fiscAdapter.setMessageToFISC(messageToFISC);
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout());
            rtnCode = fiscAdapter.sendReceive();
            if(rtnCode != FEPReturnCode.Normal) {
                rtnCode = FISCReturnCode.FISCTimeout;
            }else {
                this.logContext.setRemark("MessageFlowType:" + getFISCTxData().getMessageFlowType());
                logMessage(this.logContext);
                if(getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                    if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
                        this.logContext.setRemark("Message From FISC:" + fiscAdapter.getMessageFromFISC());
                        logMessage(this.logContext);

                        switch (fiscTeleType) {
                            case "INBK":
                                fiscINBKRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                if (StringUtils.isBlank(fiscINBKRes.getFISCMessage())) {
                                    rtnCode = FISCReturnCode.FISCTimeout;
                                } else {
                                    fiscINBKRes.parseFISCMsg();
                                }
                                break;
                            case "OPC":
                                fiscOPCRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                if (StringUtils.isBlank(fiscOPCRes.getFISCMessage())) {
                                    rtnCode = FISCReturnCode.FISCTimeout;
                                } else {
                                    fiscOPCRes.parseFISCMsg();
                                }
                                break;
                            case "CLR":
                                fiscCLRRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                if (StringUtils.isBlank(fiscCLRRes.getFISCMessage())) {
                                    rtnCode = FISCReturnCode.FISCTimeout;
                                } else {
                                    fiscCLRRes.parseFISCMsg();
                                }
                                break;
                            case "EMVIC":
                                fiscEMVICRes.setFISCMessage(fiscAdapter.getMessageFromFISC());
                                if (StringUtils.isBlank(fiscEMVICRes.getFISCMessage())) {
                                    rtnCode = FISCReturnCode.FISCTimeout;
                                } else {
                                    fiscEMVICRes.parseFISCMsg();
                                }
                                break;
                        }

                    }
                }else {
                    getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToFISCFromCBS"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
	
	public FEPReturnCode setResINBKToFeptxnForIMS() {
		try {
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); //FISC RESPONSE
            getFeptxn().setFeptxnRepRc(fiscINBKRes.getResponseCode());
			
			if (!StringUtils.isBlank(fiscINBKRes.getBALA())) {
				getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA())); // 可用餘額
			}

			if (!StringUtils.isBlank(fiscINBKRes.getBALB())) {
				getFeptxn().setFeptxnBalb(new BigDecimal(fiscINBKRes.getBALB())); // 帳戶餘額
			}

			if (!StringUtils.isBlank(fiscINBKRes.getFeeAmt())) {
				// 跨行手續費
				getFeptxn().setFeptxnFeeCustpay(new BigDecimal(fiscINBKRes.getFeeAmt()));
			}

			if (!StringUtils.isBlank(fiscINBKRes.getNpsFeeAll())) {
				// 繳費作業手續費
				getFeptxn()
						.setFeptxnNpsFeeCustpay(BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFee()) / 10));

				if (!getFeptxn().getFeptxnTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {// 他行卡
					getFeptxn().setFeptxnFeeCustpay(getFeptxn().getFeptxnNpsFeeCustpay());
				}

				getFeptxn().setFeptxnNpsFeeRcvr(
						BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(0, 4)) / 10)); // 收信單位應收手續費
				getFeptxn().setFeptxnNpsFeeAgent(
						BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(4, 8)) / 10)); // 代理單位應收手續費
				getFeptxn().setFeptxnNpsFeeTrout(
						BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(8, 12)) / 10)); // 轉出單位應收手續費
				getFeptxn().setFeptxnNpsFeeTrin(
						BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(12, 16)) / 10)); // 轉入單位應收手續費
				getFeptxn().setFeptxnNpsFeeFisc(
						BigDecimal.valueOf(Double.parseDouble(fiscINBKRes.getNpsFeeAll().substring(16, 20)) / 10)); // 財金公司應收手續費
			}

			// for 無卡提款
			if (!StringUtils.isBlank(fiscINBKRes.getPromMsg())
					&& String.valueOf(ATMTXCD.W2).equals(getFeptxn().getFeptxnTxCode())) {
				// 發卡行回傳實際扣款帳號
				getFeptxn().setFeptxnTroutActno(fiscINBKRes.getPromMsg().trim());
			}
			/* BIT22 促銷應用訊息 */
			if (StringUtils.isNotBlank(fiscINBKRes.getPromMsg())) {
				/* for 無卡提款 */
				if (ATMTXCD.W2.name().equals(feptxn.getFeptxnTxCode())) {
					/* 發行卡回傳實際扣款帳號 */
					feptxn.setFeptxnTroutActno(fiscINBKRes.getPromMsg());
				} else {
					/* 由發卡行視需求，透過跨行交易回應訊息提供持卡人運用參考 */
					feptxn.setFeptxnLuckyno(fiscINBKRes.getPromMsg());
				}
			}
			// for 跨行轉帳小額交易手續費調降
			if (!StringUtils.isBlank(fiscINBKRes.getAcctSup())) {
				// 帳戶補充資訊
				getFeptxn().setFeptxnAcctSup(fiscINBKRes.getAcctSup());
			}
			// 轉入帳號
			if (!StringUtils.isBlank(fiscINBKRes.getTrinActno())) {
				getFeptxn().setFeptxnTrinActno(fiscINBKRes.getTrinActno());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return FEPReturnCode.Normal;
	}

	public FEPReturnCode checkSysstatSyncForIMS() {
		String fiscTeleType = getFISCTxData().getFiscTeleType().toString();
		try {
			Sysstat defSYSSTAT = new Sysstat();
			defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
			switch (fiscTeleType) {
				case "INBK":
					if (defSYSSTAT != null) {
						if (!StringUtils.equals(defSYSSTAT.getSysstatFcdsync(), fiscINBKRes.getSyncCheckItem())) {
							return FEPReturnCode.KeySyncError;
						}
					} else {
						getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
						getLogContext().setRemark("SYSSTAT Not Found");
						this.logMessage(getLogContext());
						return FEPReturnCode.SYSSTATNotFound;
					}
					break;
				case "OPC":
                    if (defSYSSTAT != null) {
                        if (!StringUtils.equals(defSYSSTAT.getSysstatFopcsync(), fiscOPCRes.getSyncCheckItem())) {
                            return FEPReturnCode.KeySyncError;
                        }
                    } else {
                        getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
                        getLogContext().setRemark("SYSSTAT Not Found");
                        this.logMessage(getLogContext());
                        return FEPReturnCode.SYSSTATNotFound;
                    }
                    break;
				case "CLR":
					if (defSYSSTAT != null) {
						if (!StringUtils.equals(defSYSSTAT.getSysstatFrmsync(), fiscCLRRes.getSyncCheckItem())) {
							return FEPReturnCode.KeySyncError;
						}
					} else {
						getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
						getLogContext().setRemark("SYSSTAT Not Found");
						this.logMessage(getLogContext());
						return FEPReturnCode.SYSSTATNotFound;
					}
					break;
				case "EMVIC":
					if (defSYSSTAT != null) {
						if (!StringUtils.equals(defSYSSTAT.getSysstatFcdsync(), fiscEMVICRes.getSyncCheckItem())) {
							return FEPReturnCode.KeySyncError;
						}
					} else {
						getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
						getLogContext().setRemark("SYSSTAT Not Found");
						this.logMessage(getLogContext());
						return FEPReturnCode.SYSSTATNotFound;
					}
					break;
			}
		} catch (Exception ex) {
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
			getLogContext().setProgramException(ex);
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return FEPReturnCode.Normal;
		
	}

    public FEPReturnCode checkSysstatSyncForFISC() {
        String fiscTeleType = getFISCTxData().getFiscTeleType().toString();
        try {
            Sysstat defSYSSTAT = new Sysstat();
            defSYSSTAT.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
            defSYSSTAT = sysstatMapper.selectByPrimaryKey(defSYSSTAT.getSysstatHbkno());
            switch (fiscTeleType) {
                case "INBK":
                    if (defSYSSTAT != null) {
                        if (!StringUtils.equals(defSYSSTAT.getSysstatFcdsync(), fiscINBKReq.getSyncCheckItem())) {
                            return FEPReturnCode.KeySyncError;
                        }
                    } else {
                        getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
                        getLogContext().setRemark("SYSSTAT Not Found");
                        this.logMessage(getLogContext());
                        return FEPReturnCode.SYSSTATNotFound;
                    }
                    break;
                case "CLR":
                    if (defSYSSTAT != null) {
                        if (!StringUtils.equals(defSYSSTAT.getSysstatFrmsync(), fiscCLRReq.getSyncCheckItem())) {
                            return FEPReturnCode.KeySyncError;
                        }
                    } else {
                        getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
                        getLogContext().setRemark("SYSSTAT Not Found");
                        this.logMessage(getLogContext());
                        return FEPReturnCode.SYSSTATNotFound;
                    }
                    break;
                case "EMVIC":
                    if (defSYSSTAT != null) {
                        if (!StringUtils.equals(defSYSSTAT.getSysstatFcdsync(), fiscEMVICReq.getSyncCheckItem())) {
                            return FEPReturnCode.KeySyncError;
                        }
                    } else {
                        getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
                        getLogContext().setRemark("SYSSTAT Not Found");
                        this.logMessage(getLogContext());
                        return FEPReturnCode.SYSSTATNotFound;
                    }
                    break;
            }
        } catch (Exception ex) {
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".checkSysstatSyncForIMS"));
            getLogContext().setProgramException(ex);
            getLogContext().setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return FEPReturnCode.Normal;

    }
    public void setFiscINBKReq(FISC_INBK fiscINBKReq) {
        this.fiscINBKReq = fiscINBKReq;
    }

    
    /**
     * 負責檢核財金發動原存交易電文
     *               <modify>
     *               <modifier>Bruis</modifier>
     *               <date>2025/9/24</date>
     *               <reason>負責檢核財金發動原存交易電文</reason>
     * @return FEPReturnCode
     */
    public FEPReturnCode CheckRequestFromFISC () {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            //(1) 	檢核轉入帳號
            if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTrinBkno()) ) {
                if ( getFeptxn().getFeptxnTrinActno().trim().length() < 16 ) {
                    getLogContext().setProgramName(ProgramName + ".CheckRequestFromFISC");
                    getLogContext().setMessage("FeptxnTrinBkno : \""+getFeptxn().getFeptxnTrinBkno().trim()+"\"");
                    getLogContext().setRemark("轉出帳號格式錯誤");
                    sendEMS(getLogContext());
                    rtnCode = FEPReturnCode.MessageFormatError;/* 0101:訊息格式或內容編輯錯誤 */
                    return rtnCode;
                }
            }
            //(2) 	檢核轉出帳號
            if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno()) ) {
                if ( getFeptxn().getFeptxnTroutActno().trim().length() < 16 ) {
                    getLogContext().setProgramName(ProgramName + ".CheckRequestFromFISC");
                    getLogContext().setMessage("FeptxnTroutBkno : \""+getFeptxn().getFeptxnTroutBkno().trim()+"\"");
                    getLogContext().setRemark("轉出帳號格式錯誤");
                    sendEMS(getLogContext());
                    rtnCode = FEPReturnCode.MessageFormatError;/* 0101:訊息格式或內容編輯錯誤 */
                    return rtnCode;
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".CheckRequestFromFISC");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }
}
