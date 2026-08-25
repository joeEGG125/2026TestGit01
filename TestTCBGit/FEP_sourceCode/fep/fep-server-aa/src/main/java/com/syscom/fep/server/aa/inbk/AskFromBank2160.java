package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.Inbk2160ExtMapper;
import com.syscom.fep.mybatis.mapper.Inbk2160Mapper;
import com.syscom.fep.mybatis.model.Inbk2160;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vincent
 */
public class AskFromBank2160 extends INBKAABase {
    private String fiscResRC; // 財金RESPONSE電文的RC + Description
    private Inbk2160ExtMapper dbINBK2160 = SpringBeanFactoryUtil.getBean(Inbk2160ExtMapper.class);
    private Object tota = null;
    private FISC_INBK fiscINBKReq; // INBK REQ電文物件
    private List<Inbk2160> dsInbk2160 = new ArrayList<>();
    private Inbk2160Mapper inbk2160Mapper = SpringBeanFactoryUtil.getBean(Inbk2160Mapper.class);
    private ENCHelper encHelper;
    private Inbk2160ExtMapper inbk2160ExtMapper = SpringBeanFactoryUtil.getBean(Inbk2160ExtMapper.class);
    private Inbk2160 defINBK2160;
    private Inbk2160 requestInbk2160;
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;

    public AskFromBank2160(INBKData txnData) throws Exception {
        super(txnData, "2160");
    }

    /**
     * AA進入點主程式
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";

        try {
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setEj(getTxData().getEj());
            this.logContext.setpCode("2160");
            this.logContext.setMessageId("216000");
            this.logContext.setProgramName(StringUtils.join(this.aaName, ".AskFromBank2160"));
            this.logContext.setRemark("EJ:"+getTxData().getEj());
            logMessage(this.logContext);

            defINBK2160 = new Inbk2160();

            // 將收到的電文拆解放入物件中
            requestInbk2160 = new Inbk2160();
            String requestMessage = getINBKtxData().getTxRequestMessage();
            requestInbk2160.setInbk2160Pcode(requestMessage.substring(0, 4));
            requestInbk2160.setInbk2160TxDate(requestMessage.substring(4, 12));
            requestInbk2160.setInbk2160Ejfno(Integer.valueOf(requestMessage.substring(12)));

            // 1. 檢核財金跨行狀態
            LogHelperFactory.getTraceLogger().info("執行檢核財金跨行狀態");
            rtnCode = getFiscBusiness().checkINBKStatus("2160", true);

            // 2. 讀取交易紀錄檔(INBK2160)
            LogHelperFactory.getTraceLogger().info("執行讀取交易紀錄檔(INBK2160)");
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = readInbk2160();
            }

            // 3. 將INBK2160逐筆讀出並送至財金
            if (rtnCode == CommonReturnCode.Normal) {
                LogHelperFactory.getTraceLogger().info("將INBK2160逐筆讀出並送至財金");
                rtnCode = readDataAndSendToFISC();
            }

            // 8. 更新 INBK2160
            if (rtnCode == CommonReturnCode.Normal){
                LogHelperFactory.getTraceLogger().info(" 更新 INBK2160");
                if (rtnCode == CommonReturnCode.Normal) {
                    defINBK2160.setInbk2160AaRc("0");
                } else {
                    defINBK2160.setInbk2160AaRc(rtnCode.toString());
                }
                defINBK2160.setInbk2160Pending((short) 2); /*取消Pending*/
                defINBK2160.setInbk2160Msgflow("F6"); /*已發動2160*/
                if ("00".equals(defINBK2160.getInbk2160PrcResult())) { /*成功*/
                    defINBK2160.setInbk2160Txrust("A"); /* 處理結果=成功 */
                } else { /*處理結果-失敗*/
                    defINBK2160.setInbk2160Txrust("R"); /*交易Reverse*/
                }
                Integer res = dbINBK2160.updateByPrimaryKeySelective(defINBK2160);
                if (res < 1) { /*更新INBK2160失敗*/
                    LogHelperFactory.getTraceLogger().info(" 更新INBK2160失敗");
                    rtnCode = IOReturnCode.UpdateFail;
                }
            }

        } catch (Exception ex) {
            rtnMessage = "";
            rtnCode = FEPReturnCode.ProgramException;
            getFiscBusiness().getFeptxn().setFeptxnReplyCode(AbnormalRC.ATM_Error);
            logContext.setProgramException(ex);
            sendEMS(logContext);
            return rtnMessage;
        } finally {
            getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getTxData().getLogContext().setMessage(rtnMessage);
            getTxData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    /**
     * 2. 讀取交易紀錄檔(INBK2160)
     * @return
     */
    private FEPReturnCode readInbk2160() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            dsInbk2160 = inbk2160ExtMapper.selectOne(SysStatus.getPropertyValue().getSysstatHbkno(), requestInbk2160.getInbk2160TxDate(), requestInbk2160.getInbk2160Ejfno());
            if (dsInbk2160 != null) {
                return FEPReturnCode.Normal;
            } else {
                rtnCode = FEPReturnCode.FileNotExist;
                return rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName + ".readInbk2160"));
            sendEMS(getLogContext());
            return IOReturnCode.QueryNoData;
        }
    }

    /**
     * 3. 將INBK2160逐筆讀出並送至財金
     * @return
     */
    private FEPReturnCode readDataAndSendToFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        int i = 0;
        int Inbk2160Count = 0;
        try {
            Inbk2160Count = dsInbk2160.size();

            for (i = 0; i < Inbk2160Count; i++) {
                setInbk2160(dsInbk2160.get(i));
                getTxData().getLogContext().setEj(dsInbk2160.get(i).getInbk2160OriEjfno());
                if (getInbk2160() == null) {
                    break;
                }

                // 產生 2160 財金通知電文
                LogHelperFactory.getTraceLogger().info("產生 2160 財金通知電文");
                rtnCode = prepareForFISC();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                // 異動交易記錄INBK2160
                rtnCode = updateTxDate();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
                defINBK2160 = getInbk2160();

                // 送Request電文至財金並等待回應
                RefBase<Inbk2160> inbk2160RefBase = new RefBase<Inbk2160>(defINBK2160);
                rtnCode = getFiscBusiness().sendInbk2160RequestToFISC(inbk2160RefBase);
                defINBK2160 = inbk2160RefBase.get();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                // 4. 檢核財金Response電文Header
                setFiscRes(getFiscBusiness().getFiscINBKRes());
                LogHelperFactory.getTraceLogger().info("檢核財金Response電文Header");
                rtnCode = getFiscBusiness().checkHeader(getFiscRes(), false);

                if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError
                        || rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
                    // Garbled Message
                    getFiscBusiness().sendGarbledMessage(fiscINBKReq.getEj(), rtnCode, getFiscRes());
                } else {
                    if (rtnCode != CommonReturnCode.Normal) {
                        defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        dbINBK2160.updateByPrimaryKeySelective(defINBK2160);
                    } else {
                        defINBK2160.setInbk2160RepRc(getFiscRes().getResponseCode());
                        defINBK2160.setInbk2160PrcResult("00"); /* 處理結果 */
                        if (dbINBK2160.updateByPrimaryKeySelective(defINBK2160) < 1) {
                            rtnCode = IOReturnCode.UpdateFail;
                            break;
                        }

                        // 5. 檢核訊息押碼(MAC)
                        // Prepare FEPTXN for DES
                        LogHelperFactory.getTraceLogger().info("檢核訊息押碼(MAC)");
                        rtnCode = encHelper.checkFiscMac(fiscINBKReq.getMessageType(), fiscINBKReq.getMAC());
                        if (rtnCode != CommonReturnCode.Normal) {
                            defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        }
                        // 6. 檢核 ORI_STAN
                        LogHelperFactory.getTraceLogger().info("檢核 ORI_STAN");
                        if (!fiscINBKReq.getOriStan().equals(getFiscRes().getOriStan())) {
                            rtnCode = FISCReturnCode.OriginalMessageDataError; // 欄位 MAPPING 不符
                            defINBK2160.setInbk2160AaRc(StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0'));
                        }
                        // 7. 檢核財金回應 RC
                        LogHelperFactory.getTraceLogger().info("檢核財金回應 RC");
                        if (!NormalRC.FISC_OK.equals(getFiscRes().getResponseCode())) { // -REP
                            rtnCode = FEPReturnCode.parse(fiscINBKReq.getResponseCode());
                        } else {
                            rtnCode = CommonReturnCode.Normal;
                        }
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".readDataAndSendToFISC"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 異動交易記錄INBK2160
     *
     * @return
     */
    private FEPReturnCode updateTxDate() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getInbk2160().setInbk2160Pending((short) 1);
            getInbk2160().setInbk2160Msgflow("F1");
            getInbk2160().setInbk2160AaRc("0601");
            getInbk2160().setInbk2160FiscTimeout((short) 1);
            getInbk2160().setInbk2160Txrust("S");
            Inbk2160 update2160 = getInbk2160();

            if (inbk2160Mapper.updateByPrimaryKey(update2160) < 1) {
                return IOReturnCode.FEPTXNUpdateError;
            } else {
                return rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 產生 2160 財金通知電文
     *
     * @return
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        fiscINBKReq = new FISC_INBK();
        try {
            // Prepare Header
            fiscINBKReq.setSystemSupervisoryControlHeader("00");
            fiscINBKReq.setSystemNetworkIdentifier("00");
            fiscINBKReq.setAdderssControlField("00");
            fiscINBKReq.setMessageType("0200");
            if ("006".equals(getInbk2160().getInbk2160TroutBkno()) && "006".equals(getInbk2160().getInbk2160OriTrinBkno())){
                fiscINBKReq.setProcessingCode(getInbk2160().getInbk2160Pcode().substring(0,3)+"0");
            }else {
                fiscINBKReq.setProcessingCode(getInbk2160().getInbk2160Pcode());
            }
            fiscINBKReq.setSystemTraceAuditNo(getInbk2160().getInbk2160Stan());
            fiscINBKReq.setTxnDestinationInstituteId(StringUtils.rightPad(getInbk2160().getInbk2160DesBkno(), 7, '0'));
            fiscINBKReq.setTxnSourceInstituteId(StringUtils.rightPad(getInbk2160().getInbk2160Bkno(), 7, '0'));
            fiscINBKReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(getInbk2160().getInbk2160TxDate()).substring(1, 7) + getInbk2160().getInbk2160TxTime()); // (轉成民國年)
            fiscINBKReq.setResponseCode(getInbk2160().getInbk2160ReqRc());
            fiscINBKReq.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync());

            // Application Data Elements(ReqINBK)
            fiscINBKReq.setTxAmt(getInbk2160().getInbk2160TxAmt().toString());
            fiscINBKReq.setATMNO(getInbk2160().getInbk2160Atmno());
            String oriData = prepareOriDate(); // 組成送往財金電文
            fiscINBKReq.setOriData(oriData);
            fiscINBKReq.setTaxUnit(getInbk2160().getInbk2160Twmp());
            //2025/5/15 Jack  inbk2160的chrem是附言欄 暫時注解
//            if (StringUtils.isNotBlank(getInbk2160().getInbk2160Chrem())) {
//                fiscINBKReq.setCHREM(StringUtils.rightPad(getInbk2160().getInbk2160Chrem(), 80, '0'));
//            } else {
//                fiscINBKReq.setCHREM(StringUtils.rightPad("", 80, '0'));
//            }
            fiscINBKReq.setTroutActno(getInbk2160().getInbk2160TroutActno());
            /* 2025/7/16 修改, 消費扣款交易紅利點數相關資料 */
            String TwmpChrem = inbk2160.getInbk2160TwmpChrem();
            if (StringUtils.isNotBlank(TwmpChrem)) {
                fiscINBKReq.setMEMO(getInbk2160().getInbk2160TwmpChrem());
            }

            // 產生訊息押碼(MAC)依財金規格書 共計32個byte
            getFeptxn().setFeptxnTxDate(getInbk2160().getInbk2160TxDate());
            getFeptxn().setFeptxnBkno(getInbk2160().getInbk2160Bkno());
            getFeptxn().setFeptxnStan(getInbk2160().getInbk2160Stan());
            getFeptxn().setFeptxnDesBkno(getInbk2160().getInbk2160DesBkno());
            getFeptxn().setFeptxnOriStan(getInbk2160().getInbk2160OriStan());
            getFeptxn().setFeptxnReqRc(getInbk2160().getInbk2160ReqRc());
            //2025/5/27 by xingyun txAmt_act 是  跨行提領外幣、跨境支付(2555/2556) （FISCENCHelper Line748）改爲 setFeptxnTxAmt
            getFeptxn().setFeptxnTxAmt(getInbk2160().getInbk2160TxAmt());
            getFeptxn().setFeptxnReqDatetime(getInbk2160().getInbk2160TxDate() + getInbk2160().getInbk2160TxTime());
            String txAmt = "0000000000000";
            if (getInbk2160().getInbk2160TxAmt() != null){
                txAmt = StringUtils.leftPad(String.valueOf((getInbk2160().getInbk2160TxAmt().multiply(new BigDecimal("100"))).intValue()),13,"0") ;
            }
            fiscINBKReq.setMAC(getInbk2160().getInbk2160Bkno()+getInbk2160().getInbk2160Stan()+"00"
                    + txAmt +"000");
            encHelper = new ENCHelper(this.getFiscBusiness().getFeptxn(), this.getTxData());
            RefString mac = new RefString(fiscINBKReq.getMAC());
            rtnCode = encHelper.makeFiscMac(fiscINBKReq.getMessageType(), mac);
            fiscINBKReq.setMAC(mac.get());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            // Make Bit Map
            getFiscBusiness().setfisc(fiscINBKReq);
            rtnCode = getFiscBusiness().makeBitmap(fiscINBKReq.getMessageType(), fiscINBKReq.getProcessingCode(), MessageFlow.Request);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
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
     * 組成送往財金電文
     *
     * @return
     * @throws Exception
     */
    private String prepareOriDate() throws Exception {
        String OriDate = StringUtils.repeat(" ", 195);
        StringBuilder ori = new StringBuilder(OriDate);
        try {
            // (1:4) ORI_PCODE → 右靠左補0
            ori.replace(0, 4, StringUtils.leftPad(StringUtils.defaultString(
                    getInbk2160().getInbk2160OriPcode()), 4, '0'));

            /* 2025/10/8 修改, for 消費扣款乘車碼 */
            // (5:7) ORI_BKNO
            ori.replace(4, 11, StringUtils.rightPad(
                    getInbk2160().getInbk2160OriBkno() , 7, ' '));

            // (12:7) ORI_STAN → 右靠左補0
            ori.replace(11, 18, StringUtils.leftPad(
                    StringUtils.defaultString(getInbk2160().getInbk2160OriStan()), 7, '0'));

            // (19:14) ORI_TX_DATE + ORI_TX_TIME
            if (StringUtils.isNotBlank(getInbk2160().getInbk2160OriTxDate()) &&
                    StringUtils.isNotBlank(getInbk2160().getInbk2160OriTxTime())) {
                ori.replace(18, 32,
                        StringUtils.leftPad(getInbk2160().getInbk2160OriTxDate() +
                                getInbk2160().getInbk2160OriTxTime(), 14, '0'));
            } else {
                ori.replace(18, 32, StringUtils.repeat("0", 14));
            }

            // (33:7) ORI_TROUT_BKNO7
            if (StringUtils.isNotBlank(getInbk2160().getInbk2160OriTroutBkno7())) {
                ori.replace(32, 39,
                        StringUtils.leftPad(getInbk2160().getInbk2160OriTroutBkno7(), 7, '0'));
            } else {
                ori.replace(32, 39,
                        StringUtils.leftPad(getInbk2160().getInbk2160TroutBkno() + "0000", 7, '0'));
            }

            // (40:8) ORI_IC_SEQNO
            String icSeqno = getInbk2160().getInbk2160OriIcSeqno();
            if (icSeqno != null && !icSeqno.trim().isEmpty()) {
                ori.replace(39, 47, StringUtils.leftPad( icSeqno.trim(), 8, '0'));
            }else{
                ori.replace(39, 47, "00000000");
            }

            // (48:4) ORI_ICDATA
            String icData = getInbk2160().getInbk2160OriIcdata();
            if (icData != null && !icData.trim().isEmpty()) {
                ori.replace(47, 51, StringUtils.leftPad(icData.trim(), 4, '0'));
            } else {
                ori.replace(47, 51, "1001");
            }

            // (52:4) ORI_REP_RC → 無值=0000
            String oriRepRc = getInbk2160().getInbk2160OriRepRc();
            if (oriRepRc != null && !oriRepRc.trim().isEmpty()) {
                ori.replace(51, 55, StringUtils.leftPad(oriRepRc.trim(), 4, '0'));
            } else {
                ori.replace(51, 55, "0000");
            }

            // (56:4) REP_RC → 無值=空白
            String repRc = getInbk2160().getInbk2160RepRc();
            if (repRc != null && !repRc.trim().isEmpty()) {
                ori.replace(55, 59, StringUtils.leftPad(repRc.trim(), 4, '0'));
            } else {
                ori.replace(55, 59, StringUtils.repeat(" ", 4));
            }

            // (60:7) ORI_TRIN_BKNO7
            String trinBkno7 = getInbk2160().getInbk2160OriTrinBkno7();
            if (trinBkno7 != null && !trinBkno7.trim().isEmpty()) {
                ori.replace(59, 66, StringUtils.leftPad(trinBkno7.trim(), 7, '0')); // 補0
            } else {
                ori.replace(59, 66, StringUtils.rightPad("", 7, ' ')); // 全空白
            }

            // (67:16) ORI_TRIN_ACTNO
            String trinActno = getInbk2160().getInbk2160OriTrinActno();
            if (trinActno != null && !trinActno.trim().isEmpty()) {
                ori.replace(66, 82, StringUtils.leftPad(trinActno.trim(), 16, '0')); // 補0
            } else {
                ori.replace(66, 82, StringUtils.rightPad("", 16, ' ')); // 全空白
            }

            // (83:4) ORI_ATM_TYPE → 左靠右補空白
            String atmType = getInbk2160().getInbk2160OriAtmType();
            ori.replace(82, 86, StringUtils.rightPad(StringUtils.defaultString(atmType), 4, ' '));

            // (87:4) ORI_FEE_CUSTPAY
            BigDecimal feeCustpay = getInbk2160().getInbk2160OriFeeCustpay();
            if (feeCustpay != null) {
                // 取整數，補足 4 碼
                String feeStr = feeCustpay.setScale(0, RoundingMode.DOWN).toPlainString();
                ori.replace(86, 90, StringUtils.leftPad(feeStr, 4, '0'));
            } else {
                ori.replace(86, 90, StringUtils.rightPad("", 4, ' ')); // 全空白
            }

            /* 2025/8/13 修改 for 跨國消費扣款(2545/2546) */
            if (getInbk2160().getInbk2160OriPcode()!=null &&
                    ( "2541".equals(getInbk2160().getInbk2160OriPcode())
                    || "2542".equals(getInbk2160().getInbk2160OriPcode())
                    || "2543".equals(getInbk2160().getInbk2160OriPcode())
                    || "2545".equals(getInbk2160().getInbk2160OriPcode())
                    || "2546".equals(getInbk2160().getInbk2160OriPcode()) )) {
                ori.replace(90, 105, StringUtils.rightPad(StringUtils.defaultString(getInbk2160().getInbk2160OriMerchantId()), 15, " "));
                ori.replace(105, 121, StringUtils.rightPad(StringUtils.defaultString(getInbk2160().getInbk2160OriOrderNo()), 16, " "));
                ori.replace(121, 129, StringUtils.rightPad(StringUtils.defaultString(getInbk2160().getInbk2160OriBarcode()), 8, " "));

                /* 2025/9/19 修改 for 跨國消費扣款(2545/2546) */
                if (StringUtils.isNotBlank(getInbk2160().getInbk2160OriTxCur())) {
                    String amtStr = "";
                    if ( getInbk2160().getInbk2160OriTxnAmtCur() != null) {
                        amtStr = getInbk2160().getInbk2160OriTxnAmtCur().setScale(2, RoundingMode.DOWN).movePointRight(2).toPlainString();
                    }
                    amtStr = StringUtils.leftPad(amtStr, 14, '0');
                    if ( getInbk2160().getInbk2160OriTxnAmtCur() != null && getInbk2160().getInbk2160OriTxnAmtCur().signum() < 0) {
                        amtStr = "-" + amtStr.substring(1); // 保留負號在第一碼
                    } else {
                        amtStr = "+" + amtStr.substring(1); // 明確補正號
                    }
                    ori.replace(129, 143, amtStr );
                    ori.replace(143, 146, StringUtils.leftPad(StringUtils.defaultString(getInbk2160().getInbk2160OriTxCur()), 3, " "));
                } else {
                    ori.replace(129, 143, StringUtils.repeat(" ", 14));
                    ori.replace(143, 146, StringUtils.repeat(" ", 3));
                }
            }

            /* 2025/10/9 修改 for 跨境電子支付(2555/2556) */
            if ( getInbk2160().getInbk2160OriPcode()!=null && ( "2555".equals(getInbk2160().getInbk2160OriPcode())
                    || "2556".equals(getInbk2160().getInbk2160OriPcode())) ){
                ori.replace(90, 105, StringUtils.rightPad(StringUtils.defaultString(getInbk2160().getInbk2160OriMerchantId()), 15, " "));
            }

            return ori.toString();
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".preparePendHeader");
            sendEMS(getLogContext());
            return ori.toString();
        }
    }
}