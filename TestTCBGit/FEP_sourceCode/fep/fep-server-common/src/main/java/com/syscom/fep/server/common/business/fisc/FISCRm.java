package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.AptotExtMapper;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class FISCRm extends FISCCommon{
    public String fiscNo = "";
    public String rmNo = "";
    public String bankNo = "";
    public boolean isAdapterOut = false;
    public static final String CategoryPending = "07";
    private static final LogHelper TRACELOGGER = LogHelperFactory.getTraceLogger();

    protected FISCRm(){
        super();
    }
    protected FISCRm(FISCData fiscMessage){
        super(fiscMessage);
    }

    /**
     負責準備APTOT\ RMTOT\ RMTOTAL內容此程式為 AA\RM\11X2, Service11X1之副程式

     @param sKind
     @param defFEPTXN
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     <modify>
     <modifier>Jim</modifier>
     <reason>大批匯款時，在每日的前幾筆有可能發生在第一筆還未Insert時，其他筆試圖Update失敗，要執行Insert動作時，第一筆確已經Insert成功了，就會造成PK Violation，因此要Retry Update。</reason>
     <date>2011/3/9</date>
     </modify>
     </history>
     */
    public FEPReturnCode processAPTOTAndRMTOTAndRMTOTAL(String sKind, Feptxn defFEPTXN) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        getLogContext().setProgramName("ProcessAPTOTAndRMTOTAndRMTOTAL");
        AptotExtMapper aptotMapper = SpringBeanFactoryUtil.getBean(AptotExtMapper.class);
        Aptot aptot = new Aptot();
        @SuppressWarnings("unused")
        Rmtot rmtot = new Rmtot();
        @SuppressWarnings("unused")
        Rmtotal rmtotal = new Rmtotal();

        String sPCode = fiscRMReq.getProcessingCode();
      /*  @SuppressWarnings("unused")
        String sReceiverBank = fiscRMReq.getReceiverBank().substring(0, 3);
        @SuppressWarnings("unused")
        String sSenderBank = fiscRMReq.getSenderBank().substring(0, 3);*/

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {

            // 依Pcode不同更新APTOT不同欄位
            aptot.setAptotStDate(getFeptxn().getFeptxnTbsdy());
            aptot.setAptotApid(sPCode);
            aptot.setAptotAscFlag((DbHelper.toShort(false)));
            switch (sPCode) {
                case "1112":
                case "1122":
                case "1132":
                case "1172":
                case "1182":
                case "1192":
                    aptot.setAptotBrno(getFeptxn().getFeptxnReceiverBank().substring(3, 6));
                    aptot.setAptotCntDr(1);
                    aptot.setAptotAmtDr(getFeptxn().getFeptxnTxAmt());
                    break;
                default:
                    aptot.setAptotBrno(getFeptxn().getFeptxnSenderBank().substring(3, 6));
                    aptot.setAptotCntCr(1);
                    aptot.setAptotAmtCr(getFeptxn().getFeptxnTxAmt());
                    aptot.setAptotFeeCntCr(1);
                    aptot.setAptotFeeAmtCr(getFeptxn().getFeptxnTxFeeCr());
            }

            if (aptotMapper.updateForRMProcessAPTOT(aptot) == 0) {
                try {
                    if (aptotMapper.insertSelective(aptot) < 0) {
                        getLogContext().setRemark("processAPTOTAndRMTOTAndRMTOTAL-Insert APTOT 0 筆");
                        sendEMS(getLogContext());
                    }
                } catch (Exception e) {
                    SQLException exSql = (SQLException) e.getCause();
                    getLogContext().setRemark(StringUtils.join("processAPTOTAndRMTOTAndRMTOTAL-After Insert APTOT exception, exSql=", exSql.getSQLState(), ", exSql.number=", exSql.getErrorCode()));
                    sendEMS(getLogContext());

                    if (2627 == exSql.getErrorCode()) {
                        // Modify by Jim, 2011/5/19, 呼叫完Insert之後會把passed都設成false，所以再給一次值
                        aptot.setAptotStDate(getFeptxn().getFeptxnTbsdy());
                        aptot.setAptotApid(sPCode);
                        aptot.setAptotAscFlag((DbHelper.toShort(false)));

                        switch (sPCode) {
                            case "1112":
                            case "1122":
                            case "1132":
                            case "1172":
                            case "1182":
                            case "1192":
                                aptot.setAptotBrno(getFeptxn().getFeptxnReceiverBank().substring(3, 6));
                                aptot.setAptotCntDr(1);
                                aptot.setAptotAmtDr(getFeptxn().getFeptxnTxAmt());
                                break;
                            default:
                                aptot.setAptotBrno(getFeptxn().getFeptxnSenderBank().substring(3, 6));
                                aptot.setAptotCntCr(1);
                                aptot.setAptotAmtCr(getFeptxn().getFeptxnTxAmt());
                                aptot.setAptotFeeCntCr(1);
                                aptot.setAptotFeeAmtCr(getFeptxn().getFeptxnTxFeeCr());
                        }

                        if (aptotMapper.updateForRMProcessAPTOT(aptot) == 0) {
                            getLogContext().setRemark(
                                    "processAPTOTAndRMTOTAndRMTOTAL-After Insert APTOT PK Violation, Update 0 筆");
                            sendEMS(getLogContext());
                        } else {
                            getLogContext().setRemark(
                                    "processAPTOTAndRMTOTAndRMTOTAL-After Insert APTOT PK Violation, Update 成功");
                            sendEMS(getLogContext());
                        }
                    } else {
                        throw e;
                    }
                }
            }
            transactionManager.commit(txStatus);
        } catch (Exception ex) {
            if (transactionManager != null) {
                transactionManager.rollback(txStatus);
            }
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    public FEPReturnCode sendRMRequestToFISC() {
        return this.sendRMRequestToFISC(StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public FEPReturnCode sendRMRequestToFISC(String STAN, String MsgToFISC) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        isAdapterOut = false;

        TRACELOGGER.info(StringUtils.join("Start SendRMRequestToFISC, ProcessingCode=", fiscRMReq.getProcessingCode(),
                ", STAN = ", STAN, ", MsgToFISC = ", MsgToFISC, ", FISCTxData.LogContext=", getFISCTxData().getLogContext().getStan()));
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
        Bitmapdef oBitMap = getBitmapData(fiscRMReq.getMessageType() + fiscRMReq.getProcessingCode());
        getLogContext().setProgramName(StringUtils.join(ProgramName, "SendRMRequestToFISC"));

        try {
            // Jim, 2012/9/3, new pcode: 1513,1514,1515
            if (!RMPCode.PCode1511.equals(fiscRMReq.getProcessingCode())
                    && !"1513".equals(fiscRMReq.getProcessingCode()) && !"1514".equals(fiscRMReq.getProcessingCode())
                    && !"1515".equals(fiscRMReq.getProcessingCode())) {
                // 2.更新交易記錄(FEPTXN)
                if (StringUtils.isNotBlank(STAN)) {
                    feptxn.setFeptxnStan(STAN);
                } else {
                    feptxn.setFeptxnStan(getFISCTxData().getStan());
                }
                feptxn.setFeptxnFiscsno(fiscRMReq.getFiscNo());
                feptxn.setFeptxnRmsno(fiscRMReq.getBankNo());
                feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
                feptxn.setFeptxnDesBkno(fiscRMReq.getTxnDestinationInstituteId().substring(0, 3));
                // Modify by Jim, 2011/12/20, 初始值設成正常避免user一直用1511查
                feptxn.setFeptxnFiscTimeout(DbHelper.toShort(false));
                feptxn.setFeptxnReqRc(fiscRMReq.getResponseCode());
                // .FEPTXN_REQ_TIME = Now
                // David modify on 2010-04-12
                // FEPTXN_AA_RC 本來就是FEP內部錯誤代碼，直接用FEPReturnCode即可
                feptxn.setFeptxnAaRc(CommonReturnCode.Normal.getValue());
                // .FEPTXN_AA_RC = TxHelper.GetRCFromErrorCode(FEPReturnCode.FISCTimeout,
                // FEPChannel.FISC).PadLeft(4, "0"c)

                rtnCode = updateFepTxnForRM(feptxn);
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            // 3. 送 REQ 電文至財金並等待回應
            if (StringUtils.isBlank(MsgToFISC)) {
                rtnCode = fiscRMReq.makeFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    MsgToFISC = fiscRMReq.getFISCMessage();
                } else {
                    return CommonReturnCode.ParseTelegramError;
                }
            }
            // 準備送至財金的物件
            if (StringUtils.isNotBlank(STAN)) {
                fiscAdapter.setStan(STAN);
            } else {
                fiscAdapter.setStan(getFISCTxData().getStan());
            }
            if (StringUtils.isNotBlank(fiscNo) && PolyfillUtil.isNumeric(fiscNo)) {// Service11X1多筆匯出會用到
                // FiscAdapter判斷如果FiscNo屬性有傳入,則會進入一個while迴圈,
                // 檢查這個FiscNo是否等於前一筆成功送出之電文序號 + 1,若是則予以放行送出,
                // 若否則sleep(50)後再重新判斷,直到時間Timeout為止, 而可放行之交易在電文送至Queue後,
                // 將FiscNo回寫至前一筆成功送出之電文序號變數並記錄目前該銀行之RMNo,
                // 如此可確保前端Multi(Thread呼叫FiscAdapter送出的交易一定會照FiscNo順序送至FiscGW)
                fiscAdapter.setFiscNo(fiscNo);
                fiscAdapter.setRmNo(rmNo);
                fiscAdapter.setBankNo(bankNo);
            }
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setMessageToFISC(MsgToFISC);
            fiscAdapter.setNoWait(false);
            fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());

            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode == CommonReturnCode.Normal && fiscAdapter.getMessageFromFISC() != null) {
                fiscRMRes.setFISCMessage(fiscAdapter.getMessageFromFISC());

                fiscRMRes.parseFISCMsg();
                // Jim, 2012/9/3, new pcode: 1513,1514,1515
                if (!RMPCode.PCode1511.equals(fiscRMReq.getProcessingCode())
                        && !"1513".equals(fiscRMReq.getProcessingCode())
                        && !"1514".equals(fiscRMReq.getProcessingCode())
                        && !"1515".equals(fiscRMReq.getProcessingCode())) {
                    feptxn.setFeptxnFiscTimeout(DbHelper.toShort(false));
                    // FepTxn.FEPTXN_REP_TIME = Date.Now
                    feptxn.setFeptxnAaRc(rtnCode.getValue());

                    // Jim, 2012/6/21, 應該直接以fiscAdapter.SendReceive的return code回給前端
                    FEPReturnCode tmpRtnCode = updateFepTxnForRM(feptxn);
                    if (tmpRtnCode != CommonReturnCode.Normal) {
                        TxHelper.getRCFromErrorCode(String.valueOf(tmpRtnCode.getValue()), FEPChannel.FEP,
                                getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
                    }
                }
            } else {
                // Jim, 2012/6/21, 應該直接以fiscAdapter.SendReceive的return code回給前端
                if (!RMPCode.PCode1511.equals(fiscRMReq.getProcessingCode())) {
                    feptxn.setFeptxnFiscTimeout(DbHelper.toShort(true));
                    feptxn.setFeptxnAaRc(rtnCode.getValue());
                    FEPReturnCode tmpRtnCode = updateFepTxnForRM(feptxn);
                    if (tmpRtnCode != CommonReturnCode.Normal) {
                        TxHelper.getRCFromErrorCode(String.valueOf(tmpRtnCode.getValue()), FEPChannel.FEP,
                                getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // 負責處理產生財金 RESPONSE 電文並送出
    public FEPReturnCode sendRMResponseToFISC(RefString resMessage) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());

        try {
            resMessage.set(fiscRMRes.getFISCMessage());
            // 準備送至財金的物件
            fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
            fiscAdapter.setChannel(getFISCTxData().getTxChannel());
            fiscAdapter.setEj(getFISCTxData().getEj());
            fiscAdapter.setStan(getFISCTxData().getStan());
            fiscAdapter.setMessageToFISC(fiscRMRes.getFISCMessage());
            fiscAdapter.setNoWait(true);
            fiscAdapter.setTimeout(0);
            rtnCode = fiscAdapter.sendReceive();
            if (rtnCode != CommonReturnCode.Normal) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                        getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode updateFepTxnForRM(Feptxn defFEPTXN) {
        try {
            if (feptxnDao.updateByPrimaryKeySelective(defFEPTXN) > 0) {
                return CommonReturnCode.Normal;
            } else {
                return IOReturnCode.FEPTXNUpdateError;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return IOReturnCode.FEPTXNUpdateError;
        }
    }

    // 負責準備MSGIN一般通訊匯入主檔內容此程式為 AA\RM\1412之副程式
    public FEPReturnCode prepareMSGIN(Msgin defMSGIN,SpCaller spCaller) {
        Rmnoctl defRMNOCTL = new Rmnoctl();
        try {
            defMSGIN.setMsginTxdate(feptxn.getFeptxnTxDate());
            defMSGIN.setMsginBrno("900");// 匯入之分行別固定為虛擬分行900 */
            // 取FEP登錄序號, 若是被LOCK retry 直到ok '
            defRMNOCTL.setRmnoctlBrno(defMSGIN.getMsginBrno());
            defRMNOCTL.setRmnoctlCategory(RMCategory.MediaReservedRMOut); // "04"
            defMSGIN.setMsginFepno(StringUtils.leftPad(spCaller.getRMNO(defRMNOCTL).toString(), 7, '0'));
            defMSGIN.setMsginFepsubno("00");
            defMSGIN.setMsginCategory(RMCategory.MSGIn);// "21" '/*一般通訊匯入類*/
            defMSGIN.setMsginSenderBank(fiscRMReq.getSenderBank());
            defMSGIN.setMsginReceiverBank(fiscRMReq.getReceiverBank());
            defMSGIN.setMsginStat(fiscRMReq.getSystemTraceAuditNo());
            defMSGIN.setMsginFiscsno(fiscRMReq.getFiscNo());
            // .MSGIN_RMSNO = fiscRMReq.BANK_NO
            defMSGIN.setMsginStat(MSGINStatus.Received); // 已收信
            Calendar now = Calendar.getInstance();

            defMSGIN.setMsginSenddate(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defMSGIN.setMsginSendtime(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            defMSGIN.setMsginFiscRtnCode(fiscRMReq.getResponseCode());
            defMSGIN.setMsginChnmemo(fiscRMReq.getChineseMemo());
            defMSGIN.setMsginEngmemo(fiscRMReq.getEnglishMemo());
            defMSGIN.setMsginFiscSndCode(fiscRMReq.getProcessingCode());
            defMSGIN.setMsginEjno(fiscRMReq.getEj());
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 負責準備RMIN匯入主檔內容此程式為 AA\RM\11X2之副程式
     *
     * @param wkPend
     * @param refDefRmin
     * @param refDefRmint
     * @param orgFepno
     * @return
     */
    public FEPReturnCode prepareRMIN(String wkPend, RefBase<Rmin> refDefRmin, RefBase<Rmint> refDefRmint, SpCaller spCaller, String orgFepno) {
        Rmnoctl rmnoctl = new Rmnoctl();
        try {
            refDefRmin.get().setRminTxdate(getFeptxn().getFeptxnTxDate());
            // 匯入之分行別固定為虛擬分行900
            refDefRmin.get().setRminBrno("900");
            // 取FEP登錄序號, 若是被LOCK retry 直到ok
            rmnoctl.setRmnoctlBrno(refDefRmin.get().getRminBrno());
            // 2011-06-10 Modify by Candy
            // defRMNOCTL.RMNOCTL_CATEGORY = RMCategory.RMOutNBSDY '"02"
            // 'PEND TX
            if ("1".equals(wkPend)) {
                // 07=滯留交易匯入匯款
                rmnoctl.setRmnoctlCategory("07");
            } else {
                // 02=一般匯入匯款
                rmnoctl.setRmnoctlCategory("02");
            }

            refDefRmin.get().setRminFepno(StringUtils.leftPad(spCaller.getRMNO(rmnoctl).toString(), 7, "0"));
            refDefRmin.get().setRminFepsubno("00");
            refDefRmin.get().setRminTxamt(new BigDecimal(fiscRMReq.getTxAmt()));
            if (RMPCode.PCode1112.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1122.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1132.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1182.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1192.equals(fiscRMReq.getProcessingCode())) {
                if ("1".equals(wkPend)) {
                    // "12" 匯入類(滯留磁片交易)
                    refDefRmin.get().setRminCategory(RMCategory.RMInPending);
                } else {
                    // "10" '匯入類
                    refDefRmin.get().setRminCategory(RMCategory.RMInOverCounterTele);
                }
            }

            if (RMPCode.PCode1172.equals(fiscRMReq.getProcessingCode())) {
                // "11" ' 退匯類'
                refDefRmin.get().setRminCategory(RMCategory.BackExchange);
                // Modify by Jim, 2011/03/01, 退匯需要記錄原匯出序號的FEPNO
                refDefRmin.get().setRminOrgrmsno(orgFepno);
                refDefRmin.get().setRminOrgrmsno(fiscRMReq.getOrgBankNo());
                refDefRmin.get().setRminOrgdate(CalendarUtil.rocStringToADString("0" + fiscRMReq.getTxDate()));
                refDefRmin.get().setRminBackReason(fiscRMReq.getSTATUS());
                getLogContext().setRemark(StringUtils.join("收到1172退匯交易, RMIN_ORGREG_NO=", orgFepno, ", RMIN_ORGRMSNO=", fiscRMReq.getOrgBankNo(), ", RMIN_ORGDATE=",
                        CalendarUtil.rocStringToADString("0" + fiscRMReq.getTxDate()), ", RMIN_BACK_REASON=", fiscRMReq.getSTATUS()));
                sendEMS(getLogContext());
            }

            refDefRmin.get().setRminSenderBank(fiscRMReq.getSenderBank());
            refDefRmin.get().setRminReceiverBank(fiscRMReq.getReceiverBank());
            refDefRmin.get().setRminFiscsno(fiscRMReq.getFiscNo());
            refDefRmin.get().setRminRmsno(fiscRMReq.getBankNo());
            if (RMPCode.PCode1112.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1122.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1132.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1182.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1192.equals(fiscRMReq.getProcessingCode())) {
                if ("1".equals(wkPend)) {
                    // 2011-06-14 BU單位決定一律送CBS入帳
                    // 07.滯留交易自動入戶
                    refDefRmin.get().setRminStat(RMINStatus.RMInStoreByHand);
                } else {
                    // Fly 2019/04/01 台幣匯入款檢核AML
                    RmstatExtMapper rmstatExtMapper = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
                    Rmstat rmstat = new Rmstat();
                    rmstat.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
                    Rmstat resultRmstat = rmstatExtMapper.selectByPrimaryKey(rmstat.getRmstatHbkno());
                    if (resultRmstat != null) {
                        rmstat = resultRmstat;
                    }
                    if ("Y".equals(rmstat.getRmstatAmlflag()) || "R".equals(rmstat.getRmstatAmlflag())) {
                        refDefRmin.get().setRminStat(RMINStatus.TransferringAML);
                    } else {
                        refDefRmin.get().setRminStat(RMINStatus.Transferring);
                    }
                }
            }

            if (RMPCode.PCode1172.equals(fiscRMReq.getProcessingCode())) {
                // 匯出款被他行退之退匯匯入併至02待解款
                refDefRmin.get().setRminStat(RMINStatus.WaitRemit);
            }

            if ("1".equals(wkPend)) {
                refDefRmin.get().setRminStanBkno("000");
                refDefRmin.get().setRminStan("0000000");
                refDefRmin.get().setRminSenddate(CalendarUtil.rocStringToADString("0" + fiscRMReq.getTxnInitiateDateAndTime().substring(0, 6)));
                refDefRmin.get().setRminFiscRtnCode(NormalRC.FISC_OK);
            } else {
                refDefRmin.get().setRminStanBkno(SysStatus.getPropertyValue().getSysstatFbkno());
                refDefRmin.get().setRminStan(fiscRMReq.getSystemTraceAuditNo());
                refDefRmin.get().setRminSenddate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                refDefRmin.get().setRminFiscRtnCode(fiscRMReq.getResponseCode());
            }
            refDefRmin.get().setRminSendtime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            refDefRmin.get().setRminOutName(fiscRMReq.getOutName());
            refDefRmin.get().setRminInName(fiscRMReq.getInName());
            refDefRmin.get().setRminMemo(fiscRMReq.getChineseMemo());
            refDefRmin.get().setRminInAccIdNo(fiscRMReq.getInActno());
            refDefRmin.get().setRminFiscSndCode(fiscRMReq.getProcessingCode());
            refDefRmin.get().setRminPending(RMPending.Pending);
            refDefRmin.get().setRminEjno1(fiscRMReq.getEj());

            this.copyRMINToRMINT(refDefRmin.get(), refDefRmint);

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * copy RMIN->RMINT
     *
     * @param defRmin
     * @param refDefRmint
     */
    public void copyRMINToRMINT(Rmin defRmin, RefBase<Rmint> refDefRmint) {
        refDefRmint.get().setRmintTxdate(defRmin.getRminTxdate());
        refDefRmint.get().setRmintBrno(defRmin.getRminBrno());
        refDefRmint.get().setRmintFepno(defRmin.getRminFepno());
        refDefRmint.get().setRmintFepsubno(defRmin.getRminFepsubno());
        refDefRmint.get().setRmintTxamt(defRmin.getRminTxamt());
        refDefRmint.get().setRmintCategory(defRmin.getRminCategory());
        refDefRmint.get().setRmintSenderBank(defRmin.getRminSenderBank());
        refDefRmint.get().setRmintReceiverBank(defRmin.getRminReceiverBank());
        refDefRmint.get().setRmintStan(defRmin.getRminStan());
        refDefRmint.get().setRmintFiscsno(defRmin.getRminFiscsno());

        refDefRmint.get().setRmintRmsno(defRmin.getRminRmsno());
        refDefRmint.get().setRmintStat(defRmin.getRminStat());
        refDefRmint.get().setRmintCbsNo(defRmin.getRminCbsNo());
        refDefRmint.get().setRmintSenddate(defRmin.getRminSenddate());
        refDefRmint.get().setRmintSendtime(defRmin.getRminSendtime());
        refDefRmint.get().setRmintOrgrmsno(defRmin.getRminOrgrmsno());
        refDefRmint.get().setRmintOrgdate(defRmin.getRminOrgdate());
        refDefRmint.get().setRmintOrgregNo(defRmin.getRminOrgregNo());
        refDefRmint.get().setRmintOrgStat(defRmin.getRminOrgStat());
        refDefRmint.get().setRmintBackReason(defRmin.getRminBackReason());

        refDefRmint.get().setRmintFiscRtnCode(defRmin.getRminFiscRtnCode());
        refDefRmint.get().setRmintOutName(defRmin.getRminOutName());
        refDefRmint.get().setRmintInName(defRmin.getRminInName());
        refDefRmint.get().setRmintMemo(defRmin.getRminMemo());
        refDefRmint.get().setRmintId(defRmin.getRminId());
        refDefRmint.get().setRmintInOrgAccIdNo(defRmin.getRminInOrgAccIdNo());
        refDefRmint.get().setRmintInAccIdNo(defRmin.getRminInAccIdNo());
        refDefRmint.get().setRmintSupno1(defRmin.getRminSupno1());
        refDefRmint.get().setRmintSupno2(defRmin.getRminSupno2());
        refDefRmint.get().setRmintRegTlrno(defRmin.getRminRegTlrno());

        refDefRmint.get().setRmintFiscSndCode(defRmin.getRminFiscSndCode());
        refDefRmint.get().setRmintCbsRc(defRmin.getRminCbsRc());
        refDefRmint.get().setRmintPending(defRmin.getRminPending());
        refDefRmint.get().setRmintEjno1(defRmin.getRminEjno1());
        refDefRmint.get().setRmintEjno2(defRmin.getRminEjno2());
        refDefRmint.get().setRmintEjno3(defRmin.getRminEjno3());
        refDefRmint.get().setRmintBrsno(defRmin.getRminBrsno());
        refDefRmint.get().setRmintRecdate(defRmin.getRminRecdate());
        refDefRmint.get().setRmintRectime(defRmin.getRminRectime());
        refDefRmint.get().setRmintRttlrno(defRmin.getRminRttlrno());

        refDefRmint.get().setRmintPrtcnt(defRmin.getRminPrtcnt());
        refDefRmint.get().setRmintOrgremtype(defRmin.getRminOrgremtype());
        refDefRmint.get().setRmintDmyactno(defRmin.getRminDmyactno());
        refDefRmint.get().setRmintActno(defRmin.getRminActno());
        refDefRmint.get().setRmintActno2(defRmin.getRminActno2());
        refDefRmint.get().setRmintCifname(defRmin.getRminCifname());
        refDefRmint.get().setRmintTmpStat(defRmin.getRminTmpStat());
        refDefRmint.get().setRmintTmpRc(defRmin.getRminTmpRc());
        refDefRmint.get().setRmintActInactno(defRmin.getRminActInactno());
        refDefRmint.get().setRmintGlUnit1(defRmin.getRminGlUnit1());

        refDefRmint.get().setRmintGlUnit2(defRmin.getRminGlUnit2());
        refDefRmint.get().setRmintGlUnit1a(defRmin.getRminGlUnit1a());
        refDefRmint.get().setRmintGlUnit2a(defRmin.getRminGlUnit2a());
        refDefRmint.get().setUpdateUserid(defRmin.getUpdateUserid());
        refDefRmint.get().setUpdateTime(defRmin.getUpdateTime());
        refDefRmint.get().setRmintStanBkno(defRmin.getRminStanBkno());
        refDefRmint.get().setRmintKinbr(defRmin.getRminKinbr());
        refDefRmint.get().setRmintAmlstat(defRmin.getRminAmlstat());
        refDefRmint.get().setRmintAmlbypass(defRmin.getRminAmlbypass());
        refDefRmint.get().setRmintEjnoAml(defRmin.getRminEjnoAml());
    }

    // 負責準備MSGOUT一般通訊匯出主檔內容此程式為 AA\RM\1142之副程式
    public FEPReturnCode prepareMSGOUT(Integer wkREPK, String wkRC, String I_Stan, Integer I_EJ, Msgout defMSGOUT,
                                       SpCaller spCaller, Boolean wkFISCO_FLAG4) {
        Rmnoctl defRMNOCTL = new Rmnoctl();
        Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();

        try {
            defMSGOUT.setMsgoutTxdate(feptxn.getFeptxnTxDate());
            defMSGOUT.setMsgoutBrno("900"); // 匯入之分行別固定為虛擬分行900 */
            // 取FEP登錄序號, 若是被LOCK retry 直到ok '
            defRMNOCTL.setRmnoctlBrno(defMSGOUT.getMsgoutBrno());
            defRMNOCTL.setRmnoctlCategory(RMCategory.MediaRMOutTBSDY); // "03"
            defMSGOUT.setMsgoutFepno(StringUtils.leftPad(spCaller.getRMNO(defRMNOCTL).toString(), 7, '0'));
            defMSGOUT.setMsgoutFepsubno("00");
            defMSGOUT.setMsgoutCategory(RMCategory.MSGOut); // "20" '/*一般通訊匯入類*/
            defMSGOUT.setMsgoutSenderBank(fiscRMReq.getReceiverBank());
            defMSGOUT.setMsgoutReceiverBank(fiscRMReq.getSenderBank());
            defMSGOUT.setMsgoutStan(I_Stan);

            if (wkFISCO_FLAG4) {
                defRMFISCOUT4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRMFISCOUT4.setRmfiscout4ReceiverBank("950");
                defMSGOUT.setMsgoutFiscsno(
                        StringUtils.leftPad(String.valueOf(spCaller.getRMFISCOUT4NO(defRMFISCOUT4)), 7, '0'));
            } else {
                defMSGOUT.setMsgoutFiscsno("0000000");
            }
            Calendar now = Calendar.getInstance();
            // .MSGOUT_RMSNO = fiscRMReq.BANK_NO
            defMSGOUT.setMsgoutStat(MSGOUTStatus.Send); // "02" '已發信
            defMSGOUT.setMsgoutSenddate(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defMSGOUT.setMsgoutSendtime(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            defMSGOUT.setMsgoutFiscRtnCode(NormalRC.FISC_REQ_RC);
            defMSGOUT.setMsgoutChnmemo(fiscRMReq.getChineseMemo());
            if (wkREPK == 1) {
                defMSGOUT
                        .setMsgoutEngmemo(StringUtils.join("REPK", fiscRMReq.getEnglishMemo().substring(28, 35), wkRC));
            }
            if (wkREPK == 3) {
                defMSGOUT
                        .setMsgoutEngmemo(StringUtils.join("REP3", fiscRMReq.getEnglishMemo().substring(44, 51), wkRC));
            }
            defMSGOUT.setMsgoutFiscSndCode(RMPCode.PCode1411);
            defMSGOUT.setMsgoutEjno(I_EJ);
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 負責準備RMOUT匯出主檔內容此程式為 AA\RM\11X2之副程式
     *
     * @param wkFepno
     * @param wkBackReason
     * @param refDefRmout
     * @param refDefRmoutt
     * @return
     */
    public FEPReturnCode prepareRMOUT(String wkFepno, String wkBackReason, RefBase<Rmout> refDefRmout, RefBase<Rmoutt> refDefRmoutt, SpCaller spCaller) {
        try {
            Rmnoctl rmnoctl = new Rmnoctl();
            refDefRmout.get().setRmoutTxdate(getFeptxn().getFeptxnTxDate());
            // 3=FISC
            refDefRmout.get().setRmoutOriginal(RMOUT_ORIGINAL.FISC);

            // 取FEP登錄序號, 若是被LOCK retry 直到ok
            rmnoctl.setRmnoctlBrno(refDefRmout.get().getRmoutBrno());
            // "01"
            rmnoctl.setRmnoctlCategory(RMCategory.RMOutTBSDY);

            refDefRmout.get().setRmoutFepno(StringUtils.leftPad(spCaller.getRMNO(rmnoctl).toString(), 7, "0"));
            refDefRmout.get().setRmoutFepsubno("00");
            refDefRmout.get().setRmoutRemtype("17");
            refDefRmout.get().setRmoutTxamt(new BigDecimal(fiscRMReq.getTxAmt()));

            // '轉帳
            refDefRmout.get().setRmoutServamtType("002");
            // "06" ' 06=自動退匯匯出類
            refDefRmout.get().setRmoutCategory(RMCategory.RMOutBackExchangeAuto);
            refDefRmout.get().setRmoutSenderBank(fiscRMReq.getReceiverBank());
            refDefRmout.get().setRmoutReceiverBank(fiscRMReq.getSenderBank());
            // "08"
            refDefRmout.get().setRmoutStat(RMOUTStatus.Passed);
            refDefRmout.get().setRmoutApdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            refDefRmout.get().setRmoutAptime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            refDefRmout.get().setRmoutRegdate(refDefRmout.get().getRmoutApdate());
            refDefRmout.get().setRmoutRegtime(refDefRmout.get().getRmoutAptime());
            refDefRmout.get().setRmoutOrgrmsno(fiscRMReq.getBankNo());
            refDefRmout.get().setRmoutRegdate(CalendarUtil.rocStringToADString("0" + fiscRMReq.getTxDate()));
            refDefRmout.get().setRmoutOrgremtype(fiscRMReq.getProcessingCode().substring(1, 3));
            refDefRmout.get().setRmoutOrgregFepno(wkFepno);
            refDefRmout.get().setRmoutBackReason(wkBackReason);
            refDefRmout.get().setRmoutOutName(fiscRMReq.getOutName());
            refDefRmout.get().setRmoutInName(fiscRMReq.getInName());
            refDefRmout.get().setRmoutMemo(fiscRMReq.getChineseMemo());
            refDefRmout.get().setRmoutInAccIdNo(fiscRMReq.getInActno());
            refDefRmout.get().setRmoutFiscSndCode(RMPCode.PCode1171);
            refDefRmout.get().setRmoutPending(RMPending.Pending);
            // avoid null
            refDefRmout.get().setRmoutPostamt(0);
            refDefRmout.get().setRmoutActfee(0);
            refDefRmout.get().setRmoutRecfee(0);

            this.copyRMOUTToRMOUTT(refDefRmout.get(), refDefRmoutt);
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * copy RMOUT->RMOUTT
     *
     * @param defRmout
     * @param refDefRmoutt
     */
    public void copyRMOUTToRMOUTT(Rmout defRmout, RefBase<Rmoutt> refDefRmoutt) {
        refDefRmoutt.get().setRmouttTxdate(defRmout.getRmoutTxdate());
        refDefRmoutt.get().setRmouttBrno(defRmout.getRmoutBrno());
        refDefRmoutt.get().setRmouttOriginal(defRmout.getRmoutOriginal());
        refDefRmoutt.get().setRmouttBatchno(defRmout.getRmoutBatchno());
        refDefRmoutt.get().setRmouttFepno(defRmout.getRmoutFepno());
        refDefRmoutt.get().setRmouttFepsubno(defRmout.getRmoutFepsubno());
        refDefRmoutt.get().setRmouttBrsno(defRmout.getRmoutBrsno());
        refDefRmoutt.get().setRmouttRemtype(defRmout.getRmoutRemtype());
        refDefRmoutt.get().setRmouttTxamt(defRmout.getRmoutTxamt());
        refDefRmoutt.get().setRmouttPostamt(defRmout.getRmoutPostamt());

        refDefRmoutt.get().setRmouttRecfee(defRmout.getRmoutRecfee());
        refDefRmoutt.get().setRmouttActfee(defRmout.getRmoutActfee());
        refDefRmoutt.get().setRmouttAmtType(defRmout.getRmoutAmtType());
        refDefRmoutt.get().setRmouttServamtType(defRmout.getRmoutServamtType());
        refDefRmoutt.get().setRmouttCategory(defRmout.getRmoutCategory());
        refDefRmoutt.get().setRmouttSenderBank(defRmout.getRmoutSenderBank());
        refDefRmoutt.get().setRmouttReceiverBank(defRmout.getRmoutReceiverBank());
        refDefRmoutt.get().setRmouttStan(defRmout.getRmoutStan());
        refDefRmoutt.get().setRmouttFiscsno(defRmout.getRmoutFiscsno());
        refDefRmoutt.get().setRmouttRmsno(defRmout.getRmoutRmsno());

        refDefRmoutt.get().setRmouttStat(defRmout.getRmoutStat());
        refDefRmoutt.get().setRmouttT24No(defRmout.getRmoutT24No());
        refDefRmoutt.get().setRmouttOwpriority(defRmout.getRmoutOwpriority());
        refDefRmoutt.get().setRmouttRegdate(defRmout.getRmoutRegdate());
        refDefRmoutt.get().setRmouttRegtime(defRmout.getRmoutRegtime());
        refDefRmoutt.get().setRmouttApdate(defRmout.getRmoutApdate());
        refDefRmoutt.get().setRmouttAptime(defRmout.getRmoutAptime());
        refDefRmoutt.get().setRmouttCanceldate(defRmout.getRmoutCanceldate());
        refDefRmoutt.get().setRmouttCanceltime(defRmout.getRmoutCanceltime());
        refDefRmoutt.get().setRmouttOrderdate(defRmout.getRmoutOrderdate());

        refDefRmoutt.get().setRmouttSenddate(defRmout.getRmoutSenddate());
        refDefRmoutt.get().setRmouttSendtime(defRmout.getRmoutSendtime());
        refDefRmoutt.get().setRmouttOrgrmsno(defRmout.getRmoutOrgrmsno());
        refDefRmoutt.get().setRmouttOrgdate(defRmout.getRmoutOrgdate());
        refDefRmoutt.get().setRmouttOrgremtype(defRmout.getRmoutOrgremtype());
        refDefRmoutt.get().setRmouttOrgregFepno(defRmout.getRmoutOrgregFepno());
        refDefRmoutt.get().setRmouttOrgStat(defRmout.getRmoutOrgStat());
        refDefRmoutt.get().setRmouttBackReason(defRmout.getRmoutBackReason());
        refDefRmoutt.get().setRmouttFiscRtnCode(defRmout.getRmoutFiscRtnCode());
        refDefRmoutt.get().setRmouttOutName(defRmout.getRmoutOutName());

        refDefRmoutt.get().setRmouttInName(defRmout.getRmoutInName());
        refDefRmoutt.get().setRmouttMemo(defRmout.getRmoutMemo());
        refDefRmoutt.get().setRmouttInOrgAccIdNo(defRmout.getRmoutInOrgAccIdNo());
        refDefRmoutt.get().setRmouttInAccIdNo(defRmout.getRmoutInAccIdNo());
        refDefRmoutt.get().setRmouttSupno1(defRmout.getRmoutSupno1());
        refDefRmoutt.get().setRmouttSupno2(defRmout.getRmoutSupno2());
        refDefRmoutt.get().setRmouttRegTlrno(defRmout.getRmoutRegTlrno());
        refDefRmoutt.get().setRmouttFiscSndCode(defRmout.getRmoutFiscSndCode());
        refDefRmoutt.get().setRmouttCbsRc(defRmout.getRmoutCbsRc());
        refDefRmoutt.get().setRmouttPending(defRmout.getRmoutPending());

        refDefRmoutt.get().setRmouttEjno1(defRmout.getRmoutEjno1());
        refDefRmoutt.get().setRmouttEjno2(defRmout.getRmoutEjno2());
        refDefRmoutt.get().setRmouttEjno3(defRmout.getRmoutEjno3());
        refDefRmoutt.get().setRmouttRecbrno(defRmout.getRmoutRecbrno());
        refDefRmoutt.get().setRmouttMacno(defRmout.getRmoutMacno());
        refDefRmoutt.get().setRmouttRemcif(defRmout.getRmoutRemcif());
        refDefRmoutt.get().setRmouttRemtel(defRmout.getRmoutRemtel());
        refDefRmoutt.get().setRmouttTaxno(defRmout.getRmoutTaxno());
        refDefRmoutt.get().setRmouttOrgremtype(defRmout.getRmoutOrgremtype());
        refDefRmoutt.get().setRmouttCif(defRmout.getRmoutCif());

        refDefRmoutt.get().setRmouttGlUnit1(defRmout.getRmoutGlUnit1());
        refDefRmoutt.get().setRmouttGlUnit2(defRmout.getRmoutGlUnit2());
        refDefRmoutt.get().setRmouttGlUnit1a(defRmout.getRmoutGlUnit1a());
        refDefRmoutt.get().setRmouttGlUnit2a(defRmout.getRmoutGlUnit2a());
        refDefRmoutt.get().setUpdateUserid(defRmout.getUpdateUserid());
        refDefRmoutt.get().setUpdateTime(defRmout.getUpdateTime());
        refDefRmoutt.get().setRmouttStanBkno(defRmout.getRmoutStanBkno());

    }

    /**
     * Copy RMOUTT->RMOUTE
     *
     * @param defRMOUTT
     * @param defRMOUTE
     * @return
     *
     */
    public FEPReturnCode copyRMOUTTToRMOUTE(Rmoutt defRMOUTT, RefBase<Rmoute> defRMOUTE) {
        defRMOUTE.get().setRmouteTxdate(defRMOUTT.getRmouttTxdate());
        defRMOUTE.get().setRmouteBrno(defRMOUTT.getRmouttBrno());
        defRMOUTE.get().setRmouteOriginal(defRMOUTT.getRmouttOriginal());
        defRMOUTE.get().setRmouteBatchno(defRMOUTT.getRmouttBatchno());
        defRMOUTE.get().setRmouteFepno(defRMOUTT.getRmouttFepno());
        defRMOUTE.get().setRmouteFepsubno(defRMOUTT.getRmouttFepsubno());
        defRMOUTE.get().setRmouteBrsno(defRMOUTT.getRmouttBrsno());
        defRMOUTE.get().setRmouteRemtype(defRMOUTT.getRmouttRemtype());
        defRMOUTE.get().setRmouteTxamt(defRMOUTT.getRmouttTxamt());
        defRMOUTE.get().setRmoutePostamt(defRMOUTT.getRmouttPostamt());
        defRMOUTE.get().setRmouteRecfee(defRMOUTT.getRmouttRecfee());
        defRMOUTE.get().setRmouteActfee(defRMOUTT.getRmouttActfee());
        defRMOUTE.get().setRmouteAmtType(defRMOUTT.getRmouttAmtType());
        defRMOUTE.get().setRmouteServamtType(defRMOUTT.getRmouttServamtType());
        defRMOUTE.get().setRmouteCategory(defRMOUTT.getRmouttCategory());
        defRMOUTE.get().setRmouteSenderBank(defRMOUTT.getRmouttSenderBank());
        defRMOUTE.get().setRmouteReceiverBank(defRMOUTT.getRmouttReceiverBank());
        defRMOUTE.get().setRmouteStan(defRMOUTT.getRmouttStan());
        defRMOUTE.get().setRmouteFiscsno(defRMOUTT.getRmouttFiscsno());
        defRMOUTE.get().setRmouteRmsno(defRMOUTT.getRmouttRmsno());
        defRMOUTE.get().setRmouteStat(defRMOUTT.getRmouttStat());
        defRMOUTE.get().setRmouteT24No(defRMOUTT.getRmouttT24No());
        defRMOUTE.get().setRmouteOwpriority(defRMOUTT.getRmouttOwpriority());
        defRMOUTE.get().setRmouteRegdate(defRMOUTT.getRmouttRegdate());
        defRMOUTE.get().setRmouteRegtime(defRMOUTT.getRmouttRegtime());
        defRMOUTE.get().setRmouteApdate(defRMOUTT.getRmouttApdate());
        defRMOUTE.get().setRmouteAptime(defRMOUTT.getRmouttAptime());
        defRMOUTE.get().setRmouteCanceldate(defRMOUTT.getRmouttCanceldate());
        defRMOUTE.get().setRmouteCanceltime(defRMOUTT.getRmouttCanceltime());
        defRMOUTE.get().setRmouteOrderdate(defRMOUTT.getRmouttOrderdate());
        defRMOUTE.get().setRmouteSenddate(defRMOUTT.getRmouttSenddate());
        defRMOUTE.get().setRmouteSendtime(defRMOUTT.getRmouttSendtime());
        defRMOUTE.get().setRmouteOrgrmsno(defRMOUTT.getRmouttOrgrmsno());
        defRMOUTE.get().setRmouteOrgdate(defRMOUTT.getRmouttOrgdate());
        defRMOUTE.get().setRmouteOrgregFepno(defRMOUTT.getRmouttOrgregFepno());
        defRMOUTE.get().setRmouteOrgStat(defRMOUTT.getRmouttOrgStat());
        defRMOUTE.get().setRmouteBackReason(defRMOUTT.getRmouttBackReason());
        defRMOUTE.get().setRmouteFiscRtnCode(defRMOUTT.getRmouttFiscRtnCode());
        defRMOUTE.get().setRmouteOutName(defRMOUTT.getRmouttOutName());
        defRMOUTE.get().setRmouteInName(defRMOUTT.getRmouttInName());
        defRMOUTE.get().setRmouteMemo(defRMOUTT.getRmouttMemo());
        defRMOUTE.get().setRmouteInOrgAccIdNo(defRMOUTT.getRmouttInOrgAccIdNo());
        defRMOUTE.get().setRmouteInAccIdNo(defRMOUTT.getRmouttInAccIdNo());
        defRMOUTE.get().setRmouteSupno1(defRMOUTT.getRmouttSupno1());
        defRMOUTE.get().setRmouteSupno2(defRMOUTT.getRmouttSupno2());
        defRMOUTE.get().setRmouteRegTlrno(defRMOUTT.getRmouttRegTlrno());
        defRMOUTE.get().setRmouteFiscSndCode(defRMOUTT.getRmouttFiscSndCode());
        defRMOUTE.get().setRmouteCbsRc(defRMOUTT.getRmouttCbsRc());
        defRMOUTE.get().setRmoutePending(defRMOUTT.getRmouttPending());
        defRMOUTE.get().setRmouteEjno1(defRMOUTT.getRmouttEjno1());
        defRMOUTE.get().setRmouteEjno2(defRMOUTT.getRmouttEjno2());
        defRMOUTE.get().setRmouteEjno3(defRMOUTT.getRmouttEjno3());
        defRMOUTE.get().setRmouteRecbrno(defRMOUTT.getRmouttRecbrno());
        defRMOUTE.get().setRmouteMacno(defRMOUTT.getRmouttMacno());
        defRMOUTE.get().setRmouteRemcif(defRMOUTT.getRmouttRemcif());
        defRMOUTE.get().setRmouteRemtel(defRMOUTT.getRmouttRemtel());
        defRMOUTE.get().setRmouteTaxno(defRMOUTT.getRmouttTaxno());
        defRMOUTE.get().setRmouteOrgremtype(defRMOUTT.getRmouttOrgremtype());
        defRMOUTE.get().setRmouteCif(defRMOUTT.getRmouttCif());
        defRMOUTE.get().setRmouteGlUnit1(defRMOUTT.getRmouttGlUnit1());
        defRMOUTE.get().setRmouteGlUnit2(defRMOUTT.getRmouttGlUnit2());
        defRMOUTE.get().setRmouteGlUnit1a(defRMOUTT.getRmouttGlUnit1a());
        defRMOUTE.get().setRmouteGlUnit2a(defRMOUTT.getRmouttGlUnit2a());
        defRMOUTE.get().setRmouteStanBkno(defRMOUTT.getRmouttStanBkno());
        defRMOUTE.get().setUpdateUserid(defRMOUTT.getUpdateUserid());

        return CommonReturnCode.Normal;
    }

    // 負責準備FEP交易明細檔內容此程式為 AA\RM\11X2, 1412 , 1512之副程式
    public FEPReturnCode prepareFEPTXNByRM(Msgctl defMSGCTRL, String wkREPK) {
        BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
        Bsdays defBSDAYS = new Bsdays();
        TRACELOGGER.info("into PrepareFEPTXNByRM");

        try {
            // Modify by Jim, 2011/06/21,
            Calendar now = Calendar.getInstance();
            feptxn.setFeptxnTxDate(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            feptxn.setFeptxnTxTime(fiscRMReq.getTxnInitiateDateAndTime().substring(6, 12));
            feptxn.setFeptxnEjfno(fiscRMReq.getEj());// 電子日誌序號* /
            feptxn.setFeptxnStan(StringUtils.leftPad(fiscRMReq.getSystemTraceAuditNo(), 7, '0')); // 財金交易序號*/
            feptxn.setFeptxnBkno(fiscRMReq.getTxnSourceInstituteId().substring(0, 3));

            defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
            defBSDAYS.setBsdaysDate(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            Bsdays bsdays = bsdaysExtMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
            if (bsdays != null) {
                feptxn.setFeptxnTbsdyFisc(bsdays.getBsdaysStDateRm()); // 財金營業日'
                feptxn.setFeptxnTbsdy(bsdays.getBsdaysStDateRm()); // 本行營業日
            } else {
                // “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
                TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.BSDAYSNotFound.getValue()), FEPChannel.FEP,
                        getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
                feptxn.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                feptxn.setFeptxnTbsdy(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            }

            feptxn.setFeptxnSubsys(defMSGCTRL.getMsgctlSubsys()); // 系統別
            feptxn.setFeptxnChannel(defMSGCTRL.getMsgctlChannel()); // 通道別
            feptxn.setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行)跨行記號
            feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
            feptxn.setFeptxnPcode(fiscRMReq.getProcessingCode());
            feptxn.setFeptxnDesBkno(fiscRMReq.getTxnDestinationInstituteId().substring(0, 3));
            feptxn.setFeptxnReqRc(fiscRMReq.getResponseCode());
            // FepTxn.FEPTXN_REQ_TIME = Now
            feptxn.setFeptxnTxDatetimeFisc(
                    StringUtils.join(feptxn.getFeptxnTxDate(), fiscRMReq.getTxnInitiateDateAndTime().substring(6, 12)));

            if (StringUtils.isNotBlank(fiscRMReq.getInActno())) {
                feptxn.setFeptxnTrinActno(StringUtils.join("00", fiscRMReq.getInActno()));
            }

            if (RMPCode.PCode1112.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1122.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1132.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1172.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1182.equals(fiscRMReq.getProcessingCode())
                    || RMPCode.PCode1192.equals(fiscRMReq.getProcessingCode())) {
                // 若為空值或空白，則補0
                if (StringUtils.isBlank(fiscRMReq.getTxAmt().trim())) {
                    fiscRMReq.setTxAmt("0");
                }
                feptxn.setFeptxnTxAmt(new BigDecimal(fiscRMReq.getTxAmt())); // 交易金額
            }

            switch (fiscRMReq.getProcessingCode()) {
                case RMPCode.PCode1112:
                case RMPCode.PCode1122:
                case RMPCode.PCode1132:
                case RMPCode.PCode1172:
                case RMPCode.PCode1182:
                case RMPCode.PCode1192:
                    feptxn.setFeptxnClrType((short) 1);// 1-跨行清算 */
                    feptxn.setFeptxnFiscsno(fiscRMReq.getFiscNo()); // 電文序號 */
                    feptxn.setFeptxnRmsno(fiscRMReq.getBankNo()); // 通匯序號 */
                    feptxn.setFeptxnSenderBank(fiscRMReq.getSenderBank()); // 匯款行*/
                    feptxn.setFeptxnReceiverBank(fiscRMReq.getReceiverBank()); // 解款行*/
                    break;
                case RMPCode.PCode1412:
                    if ("1".equals(wkREPK) || "3".equals(wkREPK)) {// 以1412電文重組1411故匯款行解款行要對調
                        feptxn.setFeptxnSenderBank(fiscRMReq.getReceiverBank()); // 匯款行*/
                        feptxn.setFeptxnReceiverBank(fiscRMReq.getSenderBank()); // 解款行*/
                    } else {
                        feptxn.setFeptxnSenderBank(fiscRMReq.getSenderBank()); // 匯款行*/
                        feptxn.setFeptxnReceiverBank(fiscRMReq.getReceiverBank()); // 解款行*/
                    }
                    break;
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            TRACELOGGER.info("leave PrepareFEPTXNByRM");
        }
    }

    /**
     * 負責準備FEP交易明細檔內容此程式為 Service11X1之副程式
     *
     * @param defRMOUTT
     * @return
     *
     *         <history>
     *         <modify>
     *         <modifier>Maxine</modifier>
     *         <reason>add</reason>
     *         <date>2010/5/10</date>
     *         </modify>
     *         </history>
     */
    public FEPReturnCode prepareFEPTXNBy11X1(Rmoutt defRMOUTT) {
        BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
        Bsdays defBSDAYS = new Bsdays();
        try {
            getFeptxn().setFeptxnTxDate(defRMOUTT.getRmouttTxdate());
            getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
            getFeptxn().setFeptxnEjfno(getFISCTxData().getEj()); // 電子日誌序號'
            getFeptxn().setFeptxnStan(StringUtils.leftPad(getFISCTxData().getStan(), 7, '0')); // 財金交易序號*/
            getFeptxn().setFeptxnBkno(defRMOUTT.getRmouttSenderBank().substring(0, 3)); // 交易啟動銀行

            defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
            defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            defBSDAYS = bsdaysExtMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
            if (defBSDAYS != null) {
                getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
                getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
            } else {
                // “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
                getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            }
            getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue());
            getFeptxn().setFeptxnChannel(FEPChannel.FISC.toString()); /// * 通道別 */
            getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行) 跨行記號
            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
            getFeptxn().setFeptxnPcode(StringUtils.join("1", defRMOUTT.getRmouttRemtype(), "1"));
            getFeptxn().setFeptxnDesBkno("950");
            getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            // .FEPTXN_REQ_TIME = Now
            getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            getFeptxn().setFeptxnFiscsno(defRMOUTT.getRmouttFiscsno()); // 電文序號
            getFeptxn().setFeptxnRmsno(defRMOUTT.getRmouttRmsno()); // 通匯序號
            if (defRMOUTT.getRmouttRemtype().equals("17")) {// 退匯
                getFeptxn().setFeptxnOrgrmsno(defRMOUTT.getRmouttOrgrmsno()); // 原通匯序號
                getFeptxn().setFeptxnOrgdate(defRMOUTT.getRmouttOrgdate()); // 原匯款日期
            }
            getFeptxn().setFeptxnClrType((short) 1); // 1-跨行清算
            getFeptxn().setFeptxnTrinActno(StringUtils.join("00", defRMOUTT.getRmouttInAccIdNo()));
            getFeptxn().setFeptxnTroutActno(StringUtils.join("00", defRMOUTT.getRmouttMacno()));
            if (defRMOUTT.getRmouttPostamt() != null) {
                getFeptxn().setFeptxnTxFeeCr(BigDecimal.valueOf(defRMOUTT.getRmouttPostamt().doubleValue()));
            }

            if (defRMOUTT.getRmouttActfee() != null) {
                if (defRMOUTT.getRmouttActfee() > 0) {
                    getFeptxn().setFeptxnFeeCustpay(BigDecimal.valueOf(defRMOUTT.getRmouttActfee().doubleValue())); // 扣客戶手續費
                }
            }
            getFeptxn().setFeptxnSenderBank(defRMOUTT.getRmouttSenderBank()); // 匯款行
            getFeptxn().setFeptxnReceiverBank(defRMOUTT.getRmouttReceiverBank()); // 解款行

            if (defRMOUTT.getRmouttTxamt() != null) {
                getFeptxn().setFeptxnTxAmt(defRMOUTT.getRmouttTxamt()); // 扣客戶手續費
            }

            getFeptxn().setFeptxnTxBrno(defRMOUTT.getRmouttGlUnit1()); // 交易帳號掛帳行*/
            getFeptxn().setFeptxnTxDept(defRMOUTT.getRmouttGlUnit2()); // *交易帳號績效單位 */
            getFeptxn().setFeptxnTxActno(defRMOUTT.getRmouttMacno()); // 交易帳號 */
            if (defRMOUTT.getRmouttActfee() - defRMOUTT.getRmouttPostamt() > 0) {
                getFeptxn().setFeptxnActProfit(BigDecimal.valueOf(defRMOUTT.getRmouttActfee().intValue() - defRMOUTT.getRmouttPostamt().intValue()));
                // 帳號行手續費收入*/
            } else {
                getFeptxn().setFeptxnActLoss(BigDecimal.valueOf(defRMOUTT.getRmouttPostamt().intValue() - defRMOUTT.getRmouttActfee().intValue()));
                // 帳號行手續費支出 */
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    public FEPReturnCode prepareFEPTXNBy1411(Msgout defMSGOUT, Integer iEJ, String sStan) {
        BsdaysExtMapper bsdaysExtMapper = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
        Bsdays defBSDAYS = new Bsdays();
        try {
            Calendar now = Calendar.getInstance();
            feptxn.setFeptxnTxDate(defMSGOUT.getMsgoutTxdate());
            feptxn.setFeptxnTxTime(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            feptxn.setFeptxnEjfno(iEJ); // 電子日誌序號'
            feptxn.setFeptxnStan(StringUtils.leftPad(sStan, 7, '0')); // 財金交易序號*/
            feptxn.setFeptxnBkno(defMSGOUT.getMsgoutSenderBank().substring(0, 3)); // 交易啟動銀行

            defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
            defBSDAYS.setBsdaysDate(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            Bsdays bsdays = bsdaysExtMapper.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
            if (bsdays != null) {
                feptxn.setFeptxnTbsdyFisc(bsdays.getBsdaysStDateRm()); // 財金營業日'
                feptxn.setFeptxnTbsdy(bsdays.getBsdaysStDateRm());// 本行營業日
            } else {
                // “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
                feptxn.setFeptxnTbsdyFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                feptxn.setFeptxnTbsdy(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            }
            feptxn.setFeptxnSubsys((short) SubSystem.RM.getValue());
            // TODO BY WJ
            feptxn.setFeptxnChannel(FEPChannel.FISC.name()); /// * 通道別 */
            feptxn.setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行) 跨行記號
            feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
            feptxn.setFeptxnPcode(RMPCode.PCode1411);
            feptxn.setFeptxnDesBkno("950");
            feptxn.setFeptxnReqRc(NormalRC.FISC_REQ_RC);
            // .FEPTXN_REQ_TIME = Now

            feptxn.setFeptxnTxDatetimeFisc(FormatUtil.dateTimeFormat(now, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            feptxn.setFeptxnFiscsno(defMSGOUT.getMsgoutFiscsno()); // 電文序號
            feptxn.setFeptxnSenderBank(defMSGOUT.getMsgoutSenderBank()); // 匯款行
            feptxn.setFeptxnReceiverBank(defMSGOUT.getMsgoutReceiverBank()); // 解款行
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode prepareResponseForRM(RefString repMAC) {
        return this.prepareResponseForRM(repMAC, StringUtils.EMPTY);
    }

    // 準備回財金的RESPONSE電文
    public FEPReturnCode prepareResponseForRM(RefString repMAC, String wkSTATUS) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = null;
        try {

            rtnCode = prepareResHeaderForRM();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            // add BY candy 20100601, CALL ENCLIB RMRes.STATUS Must Prepare
            if (RMPCode.PCode1512.equals(fiscRMReq.getProcessingCode())) {
                fiscRMRes.setSTATUS(wkSTATUS);
            }
            encHelper = new ENCHelper(getFISCTxData());

            if (RMPCode.PCode1512.equals(fiscRMRes.getProcessingCode())
                    || !NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {// 重新產生MAC
                // TO_DO: Test
                rtnCode = encHelper.makeRMFISCMAC(repMAC);
                if (rtnCode != CommonReturnCode.Normal) {
                    feptxn.setFeptxnRepRc(getFISCRCFromReturnCode(rtnCode));
                }
                TRACELOGGER.info(StringUtils.join("after call des MakeRMFISCMAC repMAC=", repMAC.get()));
            }

            // 電文Body
            rtnCode = prepareResBodyForRM(repMAC.get(), wkSTATUS);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            TRACELOGGER.info(String
                    .format("after call PrepareResBodyForRM fiscRMRes.SyncCheckItem=" + fiscRMRes.getSyncCheckItem()));
            // (3)Make Bit Map
            rtnCode = makeBitmap(fiscRMRes.getMessageType(), fiscRMRes.getProcessingCode(), MessageFlow.Response);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            // 產生財金Flatfile電文
            TRACELOGGER.info(
                    StringUtils.join("after call MakeBitmap fiscRMRes.SyncCheckItem=", fiscRMRes.getSyncCheckItem()));

            rtnCode = fiscRMRes.makeFISCMsg();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                        getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }
    }

    /**
     * 組回財金電文的header部分
     *
     * @return <history> <modify> <modifier>Maxine</modifier> <reason>add</reason>
     *         <date>2010/5/10</date> </modify> </history>
     */
    private FEPReturnCode prepareResHeaderForRM() {
        try {
            // 開始組財金電文
            // fiscRMRes = New FISC_RM
            // Header部分
            fiscRMRes.setSystemSupervisoryControlHeader("00");
            fiscRMRes.setSystemNetworkIdentifier("00");
            fiscRMRes.setAdderssControlField("00");
            fiscRMRes.setMessageType("0210");
            fiscRMRes.setProcessingCode(feptxn.getFeptxnPcode());
            fiscRMRes.setSystemTraceAuditNo(feptxn.getFeptxnStan());
            fiscRMRes.setTxnDestinationInstituteId(StringUtils.rightPad(feptxn.getFeptxnDesBkno(), 7, '0'));
            fiscRMRes.setTxnSourceInstituteId(StringUtils.rightPad(feptxn.getFeptxnBkno(), 7, '0'));
            TRACELOGGER.info(StringUtils.join("FepTxn.FEPTXN_TX_DATE=", feptxn.getFeptxnTxDate(),
                    " after ADStringToROCString=", CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDate())));
            TRACELOGGER.info(StringUtils.join("FepTxn.FEPTXN_TX_TIME=", feptxn.getFeptxnTxTime()));
            // Modify by Jim, 2011/9/5, FEPTXN_TX_TIME改成用系統時間,
            // 但回財金的時間需要跟Request一樣，所以改成直接用財金的Request電文
            fiscRMRes.setTxnInitiateDateAndTime(
                    StringUtils.join(CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDate()).substring(1, 7),
                            fiscRMReq.getTxnInitiateDateAndTime().substring(6, 12))); // (轉成民國年)
            fiscRMRes.setResponseCode(feptxn.getFeptxnRepRc());
            fiscRMRes.setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(), 8, ' '));
            TRACELOGGER.info(StringUtils.join("fiscRMRes.SyncCheckItem=", fiscRMRes.getSyncCheckItem()));
            TRACELOGGER.info("after PrepareResHeaderForRM");
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 準備財的APData
     *
     * @return
     *
     *         <history>
     *         <modify>
     *         <modifier>Maxine</modifier>
     *         <reason>add</reason>
     *         <date>2010/5/10</date>
     *         </modify>
     *         </history>
     */
    private FEPReturnCode prepareResBodyForRM(String repMAC, String wkSTATUS) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        switch (fiscRMReq.getProcessingCode()) {
            case RMPCode.PCode1112:
            case RMPCode.PCode1122:
            case RMPCode.PCode1132:
            case RMPCode.PCode1172:
            case RMPCode.PCode1182:
            case RMPCode.PCode1192:
                fiscRMRes.setTxAmt(fiscRMReq.getTxAmt());
                fiscRMRes.setFiscNo(fiscRMReq.getFiscNo());
                fiscRMRes.setMAC(StringUtils.leftPad(repMAC, 8, '0'));
                break;
            case RMPCode.PCode1412:
                fiscRMRes.setFiscNo(fiscRMReq.getFiscNo());
                fiscRMRes.setMAC(StringUtils.leftPad(repMAC, 8, '0'));
                break;
            case RMPCode.PCode1512:
                fiscRMRes.setFiscNo(fiscRMReq.getFiscNo());
                fiscRMRes.setSTATUS(wkSTATUS);
                fiscRMRes.setOrgPcode(fiscRMReq.getOrgPcode());
                fiscRMRes.setMAC(StringUtils.leftPad(repMAC, 8, '0'));
                break;
        }
        return rtnCode;
    }

    /**
     * FEP CODE->財金RC
     *
     * @param rtnRtn
     * @return
     */
    public String getFISCRCFromReturnCode(FEPReturnCode rtnRtn) {
        String strFiscRc = NormalRC.FISC_REQ_RC;
        if (rtnRtn == CommonReturnCode.Normal) {
            strFiscRc = NormalRC.FISC_OK;
        } else {
            strFiscRc = TxHelper.getRCFromErrorCode(String.valueOf(rtnRtn.getValue()), FEPChannel.FEP, FEPChannel.FISC, getFISCTxData().getLogContext());
            if (StringUtils.isBlank(strFiscRc)) {
                strFiscRc = AbnormalRC.FISCRC_EXCEPTION;
            }
        }
        return StringUtils.leftPad(strFiscRc, 4, '0');
    }

    public void writeDebugLog(String sFunctionName, String msg) {
        LogData myLogContext = new LogData();

        myLogContext.setRemark(StringUtils.join(sFunctionName , "--" , msg));
        myLogContext.setProgramName(ProgramName);
        //SendEMS(myLogContext)
        logMessage(Level.INFO, myLogContext);

        LogHelperFactory.getTraceLogger().trace(myLogContext.getRemark());
    }

}
