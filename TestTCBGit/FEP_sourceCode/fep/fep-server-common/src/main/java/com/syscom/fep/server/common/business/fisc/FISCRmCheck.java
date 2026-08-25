package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.Rmfiscin1ExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.mapper.Rmfiscin4Mapper;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.mapper.RmstatMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class FISCRmCheck extends FISCFcRmCheck{
    private static final LogHelper TRACELOGGER = LogHelperFactory.getTraceLogger();

    protected FISCRmCheck(){
        super();
    }

    protected FISCRmCheck(FISCData fiscMessage){
        super(fiscMessage);
    }

    /**
     * 負責處理更換通匯押碼基碼此程式為 AA\RM\ 1412之副程式
     *
     * @param refWkRepk
     * @param refWkRc
     * @return
     */
    public FEPReturnCode checkAndChangeCDKey(RefInt refWkRepk, RefString refWkRc) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFISCTxData());
        Rminsno defRminsno = null;
        Rmoutsno defRmoutsno = null;
        RminsnoExtMapper rminsnoExtMapper = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
        RmoutsnoMapper rmoutsnoExtMapper = SpringBeanFactoryUtil.getBean(RmoutsnoMapper.class);
        try {
            TRACELOGGER.info(StringUtils.join("into CheckAndChangeCDKey ENGLISH_MEMO=", fiscRMReq.getEnglishMemo()));

            if ("CHG".equals(fiscRMReq.getEnglishMemo().substring(0, 3))) {
                // 2. 回應匯出通匯押基碼(CHGK)處理
                if ("CHGK".equals(fiscRMReq.getEnglishMemo().substring(0, 4))) {
                    refWkRepk.set(1);
                }

                if ("CHG3".equals(fiscRMReq.getEnglishMemo().substring(0, 4))) {
                    refWkRepk.set(3);
                }

                // Default CHGK OK
                refWkRc.set(NormalRC.FISC_OK);

                TRACELOGGER.info(StringUtils.join("CheckAndChangeCDKey SENDER_BANK=", fiscRMReq.getSenderBank()));
                TRACELOGGER.info(StringUtils.join("CheckAndChangeCDKey RECEIVER_BANK=", fiscRMReq.getReceiverBank()));

                defRminsno = rminsnoExtMapper.selectByPrimaryKey(fiscRMReq.getSenderBank().substring(0, 3), fiscRMReq.getReceiverBank().substring(0, 3));
                if (defRminsno != null) {
                    // 換key中
                    if ("1".equals(defRminsno.getRminsnoChgk()) || "1".equals(defRminsno.getRminsnoCdkeyFlag())) {
                        // CHANGE KEY CALL 正進行中
                        refWkRc.set("3302");
                        getLogContext().setRemark(StringUtils.join("CheckAndChangeCDKey, RMINSNO_CHGK=1 or RMINSNO_CDKEY_FLAG=1, wkRC=", refWkRc.get()));
                        logMessage(getLogContext());
                        return rtnCode;
                    }
                }

                TRACELOGGER.info(StringUtils.join("CheckAndChangeCDKey before CheckRMCDKey wkRC=", refWkRc.get()));

                // Modify by Jim, 2011/9/15, 不用rtnCode接CheckRMCDKey的return code, 因為CheckRMCDKey的錯誤跟財金無關
                encHelper.checkRMCDKey(refWkRc);

                TRACELOGGER.info(StringUtils.join("CheckAndChangeCDKey after CheckRMCDKey wkRC=", refWkRc.get()));

                if (NormalRC.FISC_OK.equals(refWkRc.get())) {
                    // 'Modify by Jim, 2011/9/14, CheckRMCDKey OK 之後才可以更新RMINSNO
                    defRminsno.setRminsnoChgk("1");
                    defRminsno.setRminsnoCdkeyFlag("1");
                    rminsnoExtMapper.updateByPrimaryKey(defRminsno);
                }
            }

            // 3.收到他行回覆本行換Key (REPK) 處理, 檢核OK , CDKey才生效
            if (("REP3".equals(fiscRMReq.getEnglishMemo().substring(0, 4))
                    || "REPK".equals(fiscRMReq.getEnglishMemo().substring(0, 4))
                    && "0000".equals(fiscRMReq.getSenderBank().substring(3, 7))
                    && "0000".equals(fiscRMReq.getReceiverBank().substring(3, 7)))) {
                // 回覆OK
                if (NormalRC.FISC_OK.equals(fiscRMReq.getEnglishMemo().substring(11, 15))) {
                    // TO_DO: Test
                    TRACELOGGER.info(StringUtils.join("into CheckAndChangeCDKey IN REPK Routine ENGLISH_MEMO=", fiscRMReq.getEnglishMemo()));
                    encHelper.changeRMCDKey("");

                    if (rtnCode == CommonReturnCode.Normal) {
                        defRmoutsno = rmoutsnoExtMapper.selectByPrimaryKey(fiscRMReq.getSenderBank().substring(0, 3), fiscRMReq.getReceiverBank().substring(0, 3));
                        if (defRmoutsno != null) {
                            defRmoutsno.setRmoutsnoChgk("0");
                            defRmoutsno.setRmoutsnoChgkTimes(defRmoutsno.getRmoutsnoChgkTimes() + 1);
                            rmoutsnoExtMapper.updateByPrimaryKey(defRmoutsno);
                        }
                    }
                }
                // 對方行庫回應換key error" & ReqFISCData .ENGLISH_MEMO[ 12: 4] 之訊息至 LSMonitorConsole
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }

    }

    /**
     * 負責處理 FISC 跨行交易電文 Basic Check此程式為 AA\RM\11X2 , 1412, 1512之副程式
     *
     * @param wkPend
     * @param refRepMac
     * @param refWkRepk
     * @param refWkRc
     * @param refRepUnitBank
     * @return
     */
    public FEPReturnCode checkBody(String wkPend, RefString refRepMac, RefInt refWkRepk, RefString refWkRc, RefString refRepUnitBank) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFISCTxData());
        try {
            // 1. 檢核匯款行/解款行是否參加財金或是否營業
            if ("1112,1122,1132,1172,1182,1192,1412".indexOf(fiscRMReq.getProcessingCode()) > -1) {
                rtnCode = checkALLBANK(fiscRMReq.getSenderBank(), fiscRMReq.getReceiverBank(), refRepUnitBank);
                if (CommonReturnCode.Normal != rtnCode) {
                    return rtnCode;
                }
            }

            // 2. 檢核財金MAC & Prepare MAC data
            // TO_DO: Test
            if ("1512".equals(fiscRMReq.getProcessingCode())) {
                rtnCode = encHelper.checkRMFISCMAC();
            } else {
                if (!"1".equals(wkPend)) {
                    rtnCode = encHelper.checkRMFISCMACAndMakeMAC(refRepMac);
                }
            }

            if (CommonReturnCode.Normal != rtnCode) {
                return rtnCode;
            }

            // 3. 檢核財金電文序號
            // 2011-06-08 add modify by Candy PEND TX.Not CHECK
            if (!"1".equals(wkPend)) {
                if (("1112".equals(fiscRMReq.getProcessingCode())
                        || "1122".equals(fiscRMReq.getProcessingCode())
                        || "1132".equals(fiscRMReq.getProcessingCode())
                        || "1172".equals(fiscRMReq.getProcessingCode())
                        || "1182".equals(fiscRMReq.getProcessingCode())
                        || "1192".equals(fiscRMReq.getProcessingCode())
                        || "1412".equals(fiscRMReq.getProcessingCode())
                        && "0".equals(wkPend))) {
                    rtnCode = checkFISCSeqno();
                    if (CommonReturnCode.Normal != rtnCode) {
                        return rtnCode;
                    }
                }

                TRACELOGGER.info(
                        StringUtils.join("ProcessingCode=", fiscRMReq.getProcessingCode(), " wkPEND=", wkPend));

                // 4. 檢核是否為對方行換KEY要求或對方行回覆本行換KEY
                if ("1412".equals(fiscRMReq.getProcessingCode()) && "0".equals(wkPend)) {
                    TRACELOGGER.info(
                            StringUtils.join("CheckBody ENGLISH_MEMO=", fiscRMReq.getEnglishMemo(), " RECEIVER_BANK=", fiscRMReq.getReceiverBank(), " SENDER_BANK=", fiscRMReq.getSenderBank()));
                    if (("CHGK".equals(fiscRMReq.getEnglishMemo().substring(0,4))
                            || "CHG3".equals(fiscRMReq.getEnglishMemo().substring(0,4))
                            || "REPK".equals(fiscRMReq.getEnglishMemo().substring(0,4))
                            || "REP3".equals(fiscRMReq.getEnglishMemo().substring(0,4)))
                            && "0000".equals(fiscRMReq.getReceiverBank().substring(3, 7))
                            && "0000".equals(fiscRMReq.getSenderBank().substring(3, 7))) {
                        TRACELOGGER.info(StringUtils.join("wkREPK=", refWkRepk.get(), " wkRC=", refWkRc.get()));
                        rtnCode = checkAndChangeCDKey(refWkRepk, refWkRc);
                        TRACELOGGER.info(StringUtils.join("After CheckAndChangeCDKey rtnCode=", String.valueOf(rtnCode.getValue()), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, "0")));
                        if (CommonReturnCode.Normal != rtnCode) {
                            return rtnCode;
                        }
                    }
                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }
        return rtnCode;
    }

    /**
     * FISC_RM_CHECK
     *
     */
    /**
     * 負責處理 FISC 跨行交易電文 Basic Check(對方行檢核)此程式為 AA\RM\11X2 , 1412, 1512之副程式
     *
     * @param repUnitBank
     * @param wkPend
     * @param autoBack
     * @param wkBackReason
     * @return
     */
    public FEPReturnCode checkBodyByMBank(RminsnoExtMapper rminsnoExtMapper, String repUnitBank, String wkPend, boolean autoBack, String wkBackReason) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFISCTxData());
        Rminsno rminsno = new Rminsno();
        boolean newKEYCheckOK = false;

        try {
            // 1.檢核對方行MAC & Sync data
            rminsno.setRminsnoReceiverBank(fiscRMReq.getReceiverBank().substring(0, 3));
            rminsno.setRminsnoSenderBank(repUnitBank);

            getLogContext().setRemark(StringUtils.join("checkBodyByMBank, Before dbRMINSNO.QueryByPrimaryKeyWithUpdLock, FISCNO=", fiscRMReq.getFiscNo(), ", BANKNO=", fiscRMReq.getBankNo()));
            logMessage(Level.INFO, getLogContext());

            Rminsno defRminsno = rminsnoExtMapper.queryByPrimaryKeyWithUpdLock(rminsno);
            if (defRminsno == null) {
                defRminsno = new Rminsno();
                defRminsno.setRminsnoReceiverBank(fiscRMReq.getReceiverBank().substring(0, 3));
                defRminsno.setRminsnoSenderBank(repUnitBank);
                defRminsno.setRminsnoCdkeyFlag("0");
            }

            getLogContext().setRemark(StringUtils.join("checkBodyByMBank, Before dbRMINSNO.QueryByPrimaryKeyWithUpdLock, FISCNO=", fiscRMReq.getFiscNo(), ", BANKNO=", fiscRMReq.getBankNo(),
                    ", RMINSNO_NO=", defRminsno.getRminsnoNo()));
            logMessage(Level.INFO, getLogContext());

            RefBoolean refAutoBack = new RefBoolean(autoBack);
            RefString refBackReason = new RefString(wkBackReason);
            RefString refCdKeyFlag = new RefString(rminsno.getRminsnoCdkeyFlag());
            RefBoolean refNewKeyCheckOK = new RefBoolean(newKEYCheckOK);

            rtnCode = encHelper.checkRMMBankMACAndSync(repUnitBank, refAutoBack, refBackReason, refCdKeyFlag, refNewKeyCheckOK);

            autoBack = refAutoBack.get();
            wkBackReason = refBackReason.get();
            rminsno.setRminsnoCdkeyFlag(refCdKeyFlag.get());
            newKEYCheckOK = refNewKeyCheckOK.get();

            if (CommonReturnCode.Normal != rtnCode) {
                return rtnCode;
            }

            // AP要多執行KEY 生效
            if ("1".equals(defRminsno.getRminsnoCdkeyFlag()) && newKEYCheckOK) {
                rtnCode = encHelper.changeRMCDKey(repUnitBank);
                if (CommonReturnCode.Normal != rtnCode) {
                    return rtnCode;
                }

                defRminsno.setRminsnoCdkeyFlag("0");

                if (rminsnoExtMapper.updateByPrimaryKeySelective(defRminsno) < 0) {
                    getLogContext().setTableName("RMINSNO");
                    getLogContext().setPrimaryKeys(StringUtils.join("RMINSNO_RECEIVER_BANK:", defRminsno.getRminsnoReceiverBank(), ";RMINSNO_SENDER_BANK", defRminsno.getRminsnoSenderBank()));

                    getLogContext().setReturnCode(IOReturnCode.UpdateFail);
                    return IOReturnCode.UpdateFail;
                }
            }

            // 2.檢核對方行通匯序號
            rtnCode = encHelper.checkRMMBankMACAndSync(repUnitBank, refAutoBack, refBackReason, refCdKeyFlag, refNewKeyCheckOK);
            autoBack = refAutoBack.get();
            wkBackReason = refBackReason.get();
            rminsno.setRminsnoCdkeyFlag(refCdKeyFlag.get());
            newKEYCheckOK = refNewKeyCheckOK.get();

            if (("1112".equals(fiscRMReq.getProcessingCode())
                    || "1122".equals(fiscRMReq.getProcessingCode())
                    || "1132".equals(fiscRMReq.getProcessingCode())
                    || "1172".equals(fiscRMReq.getProcessingCode())
                    || "1182".equals(fiscRMReq.getProcessingCode())
                    || "1192".equals(fiscRMReq.getProcessingCode())
                    && "0".equals(wkPend))) {
                if(defRminsno.getRminsnoNo()!=null){
                    if (new BigDecimal(fiscRMReq.getBankNo()).compareTo(BigDecimal.valueOf(defRminsno.getRminsnoNo() + 1)) != 0) {
                        autoBack = true;
                        wkBackReason = "01";
                    }
                }
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }

    }

    /**
     * 檢核ALLBANK並查詢repUNITBANK
     *
     * @param sSenderBank
     * @param sReceiverBank
     * @param refRepUnitBank
     * @return
     */
    public FEPReturnCode checkALLBANK(String sSenderBank, String sReceiverBank, RefString refRepUnitBank) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Allbank defAllbank = null;
        AllbankExtMapper allbankExtMapper = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
        try {
            // I. 以財金電文之SENDER_BANK讀取ALLBANK
            defAllbank = allbankExtMapper.selectByPrimaryKey(sSenderBank.substring(0,3), "000");
            if (defAllbank != null) {
                if ("2".equals(fiscRMReq.getProcessingCode().substring(3,4))) {
                    refRepUnitBank.set(defAllbank.getAllbankUnitBank());
                }
            } else {
                // 發(收)信單位與發(收)信行代號不符(2106)
                rtnCode = RMReturnCode.SenderBankWithAcceptCodeNotMatch;
                return rtnCode;
            }

            // II. 以財金電文之RECEIVER_BANK讀取ALLBANK
            defAllbank = allbankExtMapper.selectByPrimaryKey(sReceiverBank.substring(0,3), "000");
            if (defAllbank != null) {
                if ("1".equals(fiscRMReq.getProcessingCode().substring(3,4))) {
                    refRepUnitBank.set(defAllbank.getAllbankUnitBank());
                }
            } else {
                // 2113 =解款單位無此分行
                rtnCode = RMReturnCode.ReciverBankNotBranch;
                return rtnCode;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }
        return rtnCode;
    }

    /**
     * 檢核財金電文序號
     *
     * @return
     */
    public FEPReturnCode checkFISCSeqno() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Rmfiscin1ExtMapper rmfiscin1Mapper = SpringBeanFactoryUtil.getBean(Rmfiscin1ExtMapper.class);
        Rmfiscin4Mapper rmfiscin4Mapper = SpringBeanFactoryUtil.getBean(Rmfiscin4Mapper.class);
        Rmfiscin1 defRmfiscin1 = null;
        Rmfiscin4 defRmfiscin4 = null;

        try {
            if ("1112,1122,1132,1172,1182,1192".indexOf(fiscRMReq.getProcessingCode()) > -1) {
                // 以"950", 財金電文之RECEIVER_BANK[1:3]讀取RMFISCIN1
                defRmfiscin1 = rmfiscin1Mapper.selectByPrimaryKey("950", fiscRMReq.getReceiverBank().substring(0, 3));
                if (defRmfiscin1 != null) {
                	if (defRmfiscin1.getRmfiscin1No() + 1 != Integer.parseInt(fiscRMReq.getFiscNo())) {
                        rtnCode = RMReturnCode.FISCNumberError;
                        getLogContext().setRemark(StringUtils.join("CheckFISCSeqno Error; RMFISCIN1_NO+1=", defRmfiscin1.getRmfiscin1No() + 1, ", fiscRMReq.FISC_NO=", fiscRMReq.getFiscNo()));
                        getLogContext().setReturnCode(rtnCode);
                        return rtnCode;
                    }
                } else {
                    // 2101 = 電文序號不符
                    rtnCode = RMReturnCode.FISCNumberError;
                    return rtnCode;
                }
            } else {
                // 以"950", 財金電文之RECEIVER_BANK[1:3]讀取RMFISCIN1
                defRmfiscin4 = rmfiscin4Mapper.selectByPrimaryKey("950", fiscRMReq.getReceiverBank().substring(0, 3));
                if (defRmfiscin4 != null) {
                    if (defRmfiscin4.getRmfiscin4No() + 1 != Integer.parseInt(fiscRMReq.getFiscNo())) {
                        rtnCode = RMReturnCode.FISCNumberError;
                        return rtnCode;
                    }
                } else {
                    // 2101 = 電文序號不符
                    rtnCode = RMReturnCode.FISCNumberError;
                    return rtnCode;
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
        }
        return rtnCode;
    }

    /**
     檢核通匯序號

     @param autoBack
     @param backReason
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     </history>
     */
    @SuppressWarnings("unused")
	private FEPReturnCode checkRMSeqno(RefBoolean autoBack, RefString backReason, String reqUNITBANK) {
        RminsnoExtMapper dbRMINSNO = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
        Rminsno defRMINSNO = new Rminsno();

        try {
            //Modify by Jim, 2011/11/9, RMINSNO_SENDER_BANK改用UNIT_BANK的值
            //以財金電文之SENDER_BANK[1:3], RECEIVER_BANK[1:3]讀取RMINSNO
            //defRMINSNO.RMINSNO_SENDER_BANK = Mid(fiscRMReq.SENDER_BANK, 1, 3)
            defRMINSNO.setRminsnoSenderBank(reqUNITBANK);
            defRMINSNO.setRminsnoReceiverBank(fiscRMReq.getReceiverBank().substring(0, 3));

            TRACELOGGER.info(StringUtils.join("CheckRMSeqno RMINSNO_SENDER_BANK=",defRMINSNO.getRminsnoSenderBank(), " RMINSNO_RECEIVER_BANK=", defRMINSNO.getRminsnoReceiverBank()));
            defRMINSNO = dbRMINSNO.selectByPrimaryKey(defRMINSNO.getRminsnoSenderBank(),defRMINSNO.getRminsnoReceiverBank());
            if (defRMINSNO != null) {
                TRACELOGGER.info(StringUtils.join("CheckRMSeqno RMINSNO_NO=",defRMINSNO.getRminsnoNo(), " BANK_NO=", fiscRMReq.getBankNo()));

                if (Integer.parseInt(fiscRMReq.getBankNo()) != (defRMINSNO.getRminsnoNo() + 1)) {
                    autoBack.set(true);
                    backReason.set("01");
                }
            } else {
                autoBack.set(true);
                backReason.set("01");
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 負責處理 FISC 電文HEADER檢核
     *
     * @param chkFeptxn
     * @param msgFunction
     * @return
     */
    public FEPReturnCode checkRMHeader(boolean chkFeptxn, MessageFlow msgFunction) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            if ("16".equals(getFISCTxData().getMessageID().substring(0, 2))) {
                if (msgFunction == MessageFlow.Request) {
                    fiscHeader = fiscFCRMReq;
                } else {
                    fiscHeader = fiscFCRMRes;
                }
            } else {
                if (msgFunction == MessageFlow.Request) {
                    fiscHeader = fiscRMReq;
                } else {
                    fiscHeader = fiscRMRes;
                }
            }

            TRACELOGGER.info(
                    StringUtils.join("CheckRMHeader: msgFunction=", msgFunction,
                            " && FISCTxData.MessageFlowType=", getFISCTxData().getMessageFlowType()));

            // 檢查STAN 非數字
            if (!PolyfillUtil.isNumeric(fiscHeader.getSystemTraceAuditNo())) {
                return FISCReturnCode.STANError;
            }

            if (fiscHeader.getMessageKind() != msgFunction) {
                return FISCReturnCode.MessageTypeError;
            }

            // 檢查無效之訊息類別代碼( MESSAGE TYPE )或交易類別代碼( PROCESSING CODE )
            if ("0210,0200".indexOf(fiscHeader.getMessageType()) < 0) {
                return FISCReturnCode.MessageTypeError;
            }

            // 檢查訊息發送者ID.與訊息內之TXN.SOURCEINSTITUTE(ID.或TXN.DESTINATIONID.不符)
            if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnSourceInstituteId().substring(0, 3))
                    && !SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnDestinationInstituteId().substring(0, 3))) {
                return FISCReturnCode.SenderIdError;
            }

            // 檢查SYS HEADER部分
            if (!"00".equals(fiscHeader.getSystemSupervisoryControlHeader())
                    || !"00".equals(fiscHeader.getSystemNetworkIdentifier())
                    || !"00".equals(fiscHeader.getAdderssControlField())) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (FISCReturnCode.CheckBitMapError.equals(checkBitmap(fiscHeader.getAPData()))) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                // 訊息格式或內容編輯錯誤
                return FISCReturnCode.MessageFormatError;
            }

            // 檢查日期時間格式
            if (!PolyfillUtil.isNumeric(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6))) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (FISCReturnCode.CheckBitMapError.equals(checkBitmap(fiscHeader.getAPData()))) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                return FISCReturnCode.MessageFormatError;
            }

            Calendar wk_TX_Date = CalendarUtil.rocStringToADDate("0" + StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, "0"));
            if (wk_TX_Date == null) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (FISCReturnCode.CheckBitMapError.equals(checkBitmap(fiscHeader.getAPData()))) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                return FISCReturnCode.MessageFormatError;
            }

            getLogContext().setRemark("Before CheckFEPTXN");
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.INFO, getLogContext());

            // 檢核STAN
            if (chkFeptxn) {
                // Fly 2016/02/16 匯入交易改查詢FEPTSNMSTR 避免資料量太大而過慢
                Feptxn defmstr = feptxnDao.getFEPTXNMSTRByStan(FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), fiscHeader.getTxnSourceInstituteId().substring(0, 3),
                        fiscHeader.getSystemTraceAuditNo());
                TRACELOGGER.info(
                        StringUtils.join("CheckRMHeader--getFEPTXNMSTRByStan: txDate=", FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                                ", bkno=", fiscHeader.getTxnSourceInstituteId().substring(0, 3),
                                " ,stan=", fiscHeader.getSystemTraceAuditNo()));
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (defmstr != null) {
                        return FISCReturnCode.TraceNumberDuplicate;
                    }
                } else {
                    if (defmstr == null) {
                        return FISCReturnCode.OriginalMessageError;
                    }

                    if (!fiscHeader.getProcessingCode().equals(defmstr.getFeptxnPcode())) {
                        return FISCReturnCode.MessageTypeError;
                    }

                    if (fiscHeader.getMessageKind() == MessageFlow.Response && !FEPTxnMessageFlow.FISC_Request.equals(defmstr.getFeptxnMsgflow())) {
                        return FISCReturnCode.OriginalMessageError;
                    }
                }
            }

            TRACELOGGER.info("after chkFEPTXN");
            getLogContext().setRemark("After CheckFEPTXN");
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.INFO, getLogContext());

            // 檢核押碼基碼
            if (!fiscHeader.getSyncCheckItem().equalsIgnoreCase(SysStatus.getPropertyValue().getSysstatFrmsync())) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    rtnCode = checkBitmap(fiscHeader.getAPData());
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                }
                TRACELOGGER.info(
                        StringUtils.join("CheckRMHeader: fiscHeader.SyncCheckItem=", fiscHeader.getSyncCheckItem(), " && SYSSTAT_FRMSYNC=", SysStatus.getPropertyValue().getSysstatFrmsync()));
                return FISCReturnCode.KeySyncError;
            }

            // Modify by Jim, 2011/06/10, Orelse -> Andalso, 把CheckBitMap移到CheckRMStatus前面
            // Modify by Jim, 2011/06/13, 不能把 Orelse -> Andalso，因為這樣的話匯出會跑不到CheckBitmap
            // Check Bitmap
            if (fiscHeader.getMessageKind() == MessageFlow.Request || rtnCode == CommonReturnCode.Normal) {
                rtnCode = checkBitmap(fiscHeader.getAPData());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
                TRACELOGGER.info(StringUtils.join("After CheckBitmap, rtnCode = ", String.valueOf(rtnCode.getValue())));
            }

            if (fiscHeader.getTxnDestinationInstituteId().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                if ("1611".equals(fiscHeader.getProcessingCode())) {
                    rtnCode = checkFCRMStatus(fiscHeader.getProcessingCode(), fiscFCRMReq.getCURRENCY(), false);
                } else {
                    rtnCode = checkRMStatus(fiscHeader.getProcessingCode(), false);
                }
            } else {
                if ("1611".equals(fiscHeader.getProcessingCode())) {
                    rtnCode = checkFCRMStatus(fiscHeader.getProcessingCode(), fiscFCRMReq.getCURRENCY(), true);
                } else {
                    rtnCode = checkRMStatus(fiscHeader.getProcessingCode(), true);
                }
            }

            TRACELOGGER.info(StringUtils.join("After CheckRMStatus rtnCode=", rtnCode.name(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, "0")));
            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (CommonReturnCode.Normal != rtnCode && CommonReturnCode.ProgramException != rtnCode) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
            LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkRMHeader][", fiscHeader.getClass().getSimpleName(), "]FISC Data : ", fiscHeader.toJSON());
        }
    }

    /**
     * 負責處理跨行外幣通匯交易狀態檢核
     *
     * @param pcode
     * @param isSender
     * @return
     */
    public FEPReturnCode checkRMStatus(String pcode, Boolean isSender) {
        FEPReturnCode rc_MBOCT = null;
        FEPReturnCode rc_MBACT = null;
        String wk_AOCT = null;
        String wk_MBACT = null;

        Rmstat defRMSTAT = new Rmstat();
        RmstatMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatMapper.class);
        try {
            // Modify by Jim, 2011/06/09, 檢核RMSTAT相關FLAG是否許可
            defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
            Rmstat rmstat = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
            if (rmstat == null) {
                TRACELOGGER.info("FISCBusiness.CheckRMStatus, RMSTAT Query no data"); // ZK ADD
                getLogContext().setRemark("FISCBusiness.CheckRMStatus, RMSTAT Query no data");
                getLogContext().setReturnCode(IOReturnCode.RMSTATNotFound);
                sendEMS(getLogContext());
                return IOReturnCode.RMSTATNotFound;
            }

            switch (pcode.substring(0, 2)) {
                case "11":
                    if (isSender) {
                    	if (rmstat.getRmstatFiscoFlag1().equalsIgnoreCase("N")) {
                            return FISCReturnCode.SenderBankServiceStop;
                        }
                    } else {
                    	if (rmstat.getRmstatFisciFlag1().equalsIgnoreCase("N")) {
                            return FISCReturnCode.ReceiverBankOperationStop;
                        }
                    }
                    break;
                default:
                    if (isSender) {
                        if (rmstat.getRmstatFiscoFlag4().equalsIgnoreCase("N")) {
                            return FISCReturnCode.SenderBankServiceStop;
                        }
                    } else {
                    	if (rmstat.getRmstatFisciFlag4().equalsIgnoreCase("N")) {
                            return FISCReturnCode.ReceiverBankOperationStop;
                        }
                    }
                    break;
            }
            // End Modify

            // 2. *** 依匯出或是匯入之 flag 設定不同 RC 以回應財金
            if (isSender) {// 匯出'
                rc_MBOCT = FISCReturnCode.SenderBankOperationStop; // 0203發信單位主機未在跨行作業運作狀態
                rc_MBACT = FISCReturnCode.SenderBankServiceStop; // 0202發信單位該項跨行業務停止或暫停營業
            } else {
                rc_MBOCT = FISCReturnCode.ReceiverBankOperationStop; // 0206收信單位主機未在跨行作業運作狀態
                rc_MBACT = FISCReturnCode.ReceiverBankServiceStop; // 0205收信單位該項跨行業務停止或暫停營業
            }

            // 3. *** Check M-BANK 及 FISC 連線狀態
            // (1) *** Check M-BANK OP status /* 回應 0206 或 0203*/
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
                return rc_MBOCT;
            }

            // (2) *** Check FISC OP status /* 回應 0702 */
            if (!"1".equals(SysStatus.getPropertyValue().getSysstatSoct())) {
                return FISCReturnCode.FISCOperationStop;
            }

            // (3) *** 判斷交易功能:匯款類/一般通訊
            switch (pcode.substring(0, 2)) {
                case "11":
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct1100();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact1100();
                    break;
                case "14":
                    wk_AOCT = SysStatus.getPropertyValue().getSysstatAoct1400();
                    wk_MBACT = SysStatus.getPropertyValue().getSysstatMbact1400();
                    break;
                default:
                    return CommonReturnCode.Normal;
            }

            // Modify by Jim, 2011/04/21, 只要沒有Check IN成功就不能做
            // (4) *** 需先 Check 共用子系統 (M-BANK 及 FISC AP STATUS)
            if ("1".equals(pcode.substring(0, 1))) {
                if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
                    return rc_MBACT; // 跨行業務停止或暫停營業
                }
                if (isSender) {
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                        return FISCReturnCode.FISCServiceStop; // 0701 財金公司該項跨行業務停止或暫停營業
                    }
                } else {
                    // Modify by Jim, 2011/9/20, 匯入PreCheckout時還是可以做
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct1000())
                            && !"4".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                        return FISCReturnCode.FISCServiceStop; // 0701 財金公司該項跨行業務停止或暫停營業
                    }
                }
            }

            // (5) *** Check M-BANK AP status /* 回應 0205 或 0202 */
            if (!wk_MBACT.equals("1")) {
                return rc_MBACT; // 跨行業務停止或暫停營業
            }

            // (6) *** Check FISC AP STATUS /* 回應 0701 */
            if (isSender) {
                if (!wk_AOCT.equals("1")) {
                    return FISCReturnCode.FISCServiceStop; // 0701 財金公司該項跨行業務停止或暫停營業
                }
            } else {
                // Modify by Jim, 2011/9/20, 匯入PreCheckout時還是可以做
                if (!wk_AOCT.equals("1") && !wk_AOCT.equals("4")) {
                    return FISCReturnCode.FISCServiceStop; // 0701 財金公司該項跨行業務停止或暫停營業
                }
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    public FEPReturnCode checkRMHeaderByPEND(boolean chkFEPTXN, boolean chkTele, MessageFlow msgFunction) {
        FEPReturnCode rtnCode = null;

        try {
            if ("16".equals(getFISCTxData().getMessageID().substring(0, 2))) {
                if (msgFunction == MessageFlow.Request) {
                    fiscHeader = fiscFCRMReq;
                } else {
                    fiscHeader = fiscFCRMRes;
                }
            } else {
                if (msgFunction == MessageFlow.Request) {
                    fiscHeader = fiscRMReq;
                } else {
                    fiscHeader = fiscRMRes;
                }
            }
            TRACELOGGER.info(StringUtils.join("CheckRMHeader: msgFunction=",msgFunction, " && FISCTxData.MessageFlowType=", getFISCTxData().getMessageFlowType()));

            //檢查STAN 非數字
            if (!PolyfillUtil.isNumeric(fiscHeader.getSystemTraceAuditNo())) {
                return FISCReturnCode.STANError;
            }

            if (fiscHeader.getMessageKind() != msgFunction) {
                return FISCReturnCode.MessageTypeError;
            }

            //檢查無效之訊息類別代碼( MESSAGE TYPE )或交易類別代碼( PROCESSING CODE )
            if ("0210,0200".indexOf(fiscHeader.getMessageType()) < 0) {
                return FISCReturnCode.MessageTypeError;
            }

            //檢查訊息發送者ID.與訊息內之TXN.SOURCEINSTITUTE(ID.或TXN.DESTINATIONID.不符)
            if (!(SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnSourceInstituteId().substring(0, 3))) && !(SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnDestinationInstituteId().substring(0, 3)))) {
                return FISCReturnCode.SenderIdError;
            }

            //檢查SYS HEADER部分
            if (!"00".equals(fiscHeader.getSystemSupervisoryControlHeader()) || !"00".equals(fiscHeader.getSystemNetworkIdentifier()) || !"00".equals(fiscHeader.getAdderssControlField())) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (checkBitmap(fiscHeader.getAPData()) == FISCReturnCode.CheckBitMapError) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                return FISCReturnCode.MessageFormatError; //**訊息格式或內容編輯錯誤
            }

            //檢查日期時間格式
            if (!PolyfillUtil.isNumeric(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6))) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (checkBitmap(fiscHeader.getAPData()) == FISCReturnCode.CheckBitMapError) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                return FISCReturnCode.MessageFormatError;
            }
            boolean dateFlag = true;
            try {
                LocalDate.parse(CalendarUtil.rocStringToADString("0" + StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }catch (DateTimeParseException | NullPointerException e){
                dateFlag = false;
            }
            LocalDate wk_TX_Date = null;
            if(dateFlag){
                wk_TX_Date = LocalDate.parse(CalendarUtil.rocStringToADString("0" + StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, '0')), DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            if (LocalDate.MIN.equals(wk_TX_Date)) {
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (checkBitmap(fiscHeader.getAPData()) == FISCReturnCode.CheckBitMapError) {
                        return FISCReturnCode.MessageFormatError;
                    }
                }
                return FISCReturnCode.MessageFormatError;
            }


            //檢核STAN
            if (chkFEPTXN || chkTele) {
            	if(wk_TX_Date != null && fiscHeader != null) {
                    setOriginalFEPTxn(feptxnDao.getFeptxnByStan(wk_TX_Date.toString().replace("-",""), fiscHeader.getTxnSourceInstituteId().substring(0, 3), fiscHeader.getSystemTraceAuditNo()));
                    if (getOriginalFEPTxn() == null) {
                        setOriginalFEPTxn(feptxnDao.getFeptxnByStan(wk_TX_Date.toString().replace("-",""), fiscHeader.getTxnSourceInstituteId().substring(0, 3), fiscHeader.getSystemTraceAuditNo()));
                    }           		
            	}
                if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                    if (getOriginalFEPTxn() != null) {
                        return FISCReturnCode.TraceNumberDuplicate;
                    }
                } else {//response or confirm
                    if (getOriginalFEPTxn() == null) {
                        return FISCReturnCode.OriginalMessageError;
                    }

                    if (!getOriginalFEPTxn().getFeptxnPcode().equals(fiscHeader.getProcessingCode())) {
                        return FISCReturnCode.MessageTypeError;
                    }
                    if (fiscHeader.getMessageKind() == MessageFlow.Response && !(FEPTxnMessageFlow.FISC_Request.equals(getOriginalFEPTxn().getFeptxnMsgflow()))) {
                        return FISCReturnCode.OriginalMessageError;
                    }
                }

            }
            TRACELOGGER.info("after chkFEPTXN");

            //檢核押碼基碼
            //2011-06-08 Modify by Candy PENDING Tx.not CHECK
            if (chkTele) {
                if (!SysStatus.getPropertyValue().getSysstatFrmsync().equalsIgnoreCase(fiscHeader.getSyncCheckItem())) {
                    if (fiscHeader.getMessageKind() == MessageFlow.Request) {
                        rtnCode = checkBitmap(fiscHeader.getAPData());
                        if (rtnCode != CommonReturnCode.Normal) {
                            return rtnCode;
                        }
                    }
                    TRACELOGGER.info(StringUtils.join("CheckRMHeader: fiscHeader.SyncCheckItem=",fiscHeader.getSyncCheckItem(), " && SYSSTAT_FRMSYNC=", SysStatus.getPropertyValue().getSysstatFrmsync()));
                    return FISCReturnCode.KeySyncError;
                }

                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscHeader.getTxnDestinationInstituteId().substring(0, 3))) {
                    if ("1611".equals(fiscHeader.getProcessingCode())) {
                        rtnCode = checkFCRMStatus(fiscHeader.getProcessingCode(), fiscFCRMReq.getCURRENCY(), false);
                    } else {
                        rtnCode = checkRMStatus(fiscHeader.getProcessingCode(), false);
                    }
                } else {
                    if ("1611".equals(fiscHeader.getProcessingCode())) {
                        rtnCode = checkFCRMStatus(fiscHeader.getProcessingCode(), fiscFCRMReq.getCURRENCY(), true);
                    } else {
                        rtnCode = checkRMStatus(fiscHeader.getProcessingCode(), true);
                    }
                }
            }
            if(rtnCode != null)
            TRACELOGGER.info(StringUtils.join("After CheckRMStatus rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

            //Check Bitmap
            if (fiscHeader.getMessageKind() == MessageFlow.Request || rtnCode == CommonReturnCode.Normal) {
                rtnCode = checkBitmap(fiscHeader.getAPData());
                //If CheckBitmap(fiscHeader.APData) = FISCReturnCode.CheckBitMapError Then
                //    Return FISCReturnCode.MessageFormatError
                //End If
                TRACELOGGER.info(StringUtils.join("After CheckBitmap, rtnCode = " + rtnCode.toString()));
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        } finally {
            if (rtnCode != null && rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
                TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
            }
            LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".checkRMHeaderByPEND][", fiscHeader.getClass().getSimpleName(), "]FISC Data : ", fiscHeader.toJSON());
        }
    }

    /**
     * 負責處理 匯出資料 Basic Check 此程式為 Service11X1, Service1411,之副程式
     *
     * @param defRMOUTT
     * @param repUNITBANK
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
    public FEPReturnCode checkOutDataByRMOUTT(Rmoutt defRMOUTT, RefString repUNITBANK) {
        FEPReturnCode rtnCode = null;
        try {
            // 檢核匯款行/解款行是否參加財金或是否營業
            rtnCode = checkALLBANK(defRMOUTT.getRmouttSenderBank(), defRMOUTT.getRmouttReceiverBank(), repUNITBANK);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 負責處理 匯出資料 Basic Check 此程式為 Service11X1, Service1411,之副程式
     *
     * @param defMSGOUT
     * @param repUNITBANK
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
    public FEPReturnCode checkOutDataByMSGOUT(Msgout defMSGOUT, RefString repUNITBANK) {
        FEPReturnCode rtnCode = null;
        try {
            // 檢核匯款行/解款行是否參加財金或是否營業
            rtnCode = checkALLBANK(defMSGOUT.getMsgoutSenderBank(), defMSGOUT.getMsgoutReceiverBank(), repUNITBANK);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    // 負責檢核財金 Response 電文
    public FEPReturnCode checkResponseFromFISC() {
        FEPReturnCode rtnCode = null;
        String sFiscRc = null;
        getLogContext().setProgramName(StringUtils.join(ProgramName, "CheckResponseFromFISC"));

        try {
            // Jim, 2012/9/4, add Pcode 1513~1515不需檢核FEPTXN
            // 1.檢核財金電文 Header
            if (RMPCode.PCode1511.equals(fiscRMRes.getProcessingCode()) || "1513".equals(fiscRMRes.getProcessingCode())
                    || "1514".equals(fiscRMRes.getProcessingCode()) || "1515".equals(fiscRMRes.getProcessingCode())) {
                rtnCode = checkRMHeader(false, MessageFlow.Response);
            } else {
                rtnCode = checkRMHeader(true, MessageFlow.Response);
            }
            TRACELOGGER.info(StringUtils.join("After CheckRMHeader rtnCode=", rtnCode.name(),
                    StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

            if (rtnCode != CommonReturnCode.Normal) {
                sFiscRc = TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
                        FEPChannel.FISC, getFISCTxData().getLogContext());
            } else {
                sFiscRc = NormalRC.FISC_OK;
            }

            if ("10".equals(sFiscRc.substring(0, 2))) {
                sendGarbledMessage(fiscRMReq.getEj(), rtnCode, fiscRMRes);
                return rtnCode;
            }

            // Jim, 2012/9/3, new pcode: 1513,1514,1515
            if (!"1511".equals(fiscRMRes.getProcessingCode()) && !"1513".equals(fiscRMRes.getProcessingCode())
                    && !"1514".equals(fiscRMRes.getProcessingCode()) && !"1515".equals(fiscRMRes.getProcessingCode())) {
                // 2. 更新交易狀態欄位(FEPTXN)
                feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); /// *FISC RESPONSE*/
                feptxn.setFeptxnRepRc(fiscRMRes.getResponseCode());
                // FepTxn.FEPTXN_REP_TIME = Now

                rtnCode = updateFepTxnForRM(feptxn);
                TRACELOGGER.info(StringUtils.join("After UpdateFepTxnForRM rtnCode=", rtnCode.name(),
                        StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

                if (rtnCode == CommonReturnCode.Normal) {/// * 檢核財金電文 Header 正常*/
                    if (!NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
                        return FEPReturnCode.parse(fiscRMRes.getResponseCode());
                    }
                } else {
                    return rtnCode;
                }

                getLogContext().setRemark("");

                // 3.檢核原交易 MAPPING 欄位
                String wk_TX_Date = CalendarUtil.rocStringToADString(
                        "0" + StringUtils.leftPad(fiscRMRes.getTxnInitiateDateAndTime().substring(0, 6), 7, '0'));
                if (!wk_TX_Date.equals(feptxn.getFeptxnTxDate())
                        || !fiscRMRes.getTxnInitiateDateAndTime().substring(6, 12).equals(feptxn.getFeptxnTxTime())
                        || !fiscRMRes.getTxnDestinationInstituteId().substring(0, 3).equals(feptxn.getFeptxnDesBkno())
                        || (!"".equals(fiscRMRes.getFiscNo()) && !fiscRMRes.getFiscNo()
                        .equals(StringUtils.leftPad(feptxn.getFeptxnFiscsno(), 7, '0')))
                        || (!"".equals(fiscRMRes.getBankNo())
                        && !fiscRMRes.getBankNo().equals(StringUtils.leftPad(feptxn.getFeptxnRmsno(), 7, '0')))
                        || (!"".equals(fiscRMRes.getSenderBank())
                        && !fiscRMRes.getSenderBank().equals(feptxn.getFeptxnSenderBank()))
                        || (!"".equals(fiscRMRes.getReceiverBank())
                        && !fiscRMRes.getReceiverBank().equals(feptxn.getFeptxnReceiverBank()))
                        || (!"".equals(fiscRMRes.getTxAmt()) && !"0".equals(fiscRMRes.getTxAmt())
                        && feptxn.getFeptxnTxAmt().doubleValue() != Double.parseDouble(fiscRMRes.getTxAmt()))) {
                    TRACELOGGER.info(StringUtils.join(
                            "wk_TX_Date :", wk_TX_Date,
                            ", FeptxnTxDate :", feptxn.getFeptxnTxDate(),
                            ". fiscRMRes.TxnInitiateDateAndTime().substring(6, 12) :",fiscRMRes.getTxnInitiateDateAndTime().substring(6, 12),
                            ", FeptxnTxTime :", feptxn.getFeptxnTxTime(),
                            ". fiscRMRes.TxnDestinationInstituteId.substring(0, 3) :", fiscRMRes.getTxnDestinationInstituteId().substring(0, 3),
                            ", FeptxnDesBkno :", feptxn.getFeptxnDesBkno(),
                            ". fiscRMRes.FiscNo :", fiscRMRes.getFiscNo(),
                            ", FeptxnFiscsno :", StringUtils.leftPad(feptxn.getFeptxnFiscsno(), 7, '0'),
                            ". fiscRMRes.BankNo :", fiscRMRes.getBankNo(),
                            ", FeptxnRmsno :", StringUtils.leftPad(feptxn.getFeptxnRmsno(), 7, '0'),
                            ". fiscRMRes.SenderBank :", fiscRMRes.getSenderBank(),
                            ", FeptxnSenderBank :", feptxn.getFeptxnSenderBank(),
                            ". fiscRMRes.ReceiverBank :", fiscRMRes.getReceiverBank(),
                            ", FeptxnReceiverBank :", feptxn.getFeptxnReceiverBank(),
                            ". fiscRMRes.TxAmt :", fiscRMRes.getTxAmt(),
                            ", FeptxnTxAmt :", feptxn.getFeptxnTxAmt().doubleValue()
                    ));
                    rtnCode = FISCReturnCode.OriginalMessageDataError; // 0401訊息之 Mapping 欄位與與原交易不符
                    return rtnCode;
                }

                // 4.Call DES 驗證 MAC
                // TODO:{Jim} Performance 測試，暫時先點掉
                getLogContext().setRemark(StringUtils.join("Before CheckRMFISCMAC, fiscRMReq.ProcessingCode=",
                        fiscRMReq.getProcessingCode()));
                logMessage(Level.INFO, getLogContext());

            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
