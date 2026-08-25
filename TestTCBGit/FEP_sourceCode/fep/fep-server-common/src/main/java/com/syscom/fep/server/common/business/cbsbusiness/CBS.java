package com.syscom.fep.server.common.business.cbsbusiness;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.mybatis.ext.mapper.CbspendExtMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.server.common.cbsprocess.ABACCI01;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class CBS extends BusinessBase {
    private Object tita = null;

    private ACBSAction action;

    private MessageBase txData;

    //private FeptxnExtMapper feptxnExtMapper = SpringBeanFactoryUtil.getBean(FeptxnExtMapper.class);

    private CbspendExtMapper cbspendExtMapper = SpringBeanFactoryUtil.getBean(CbspendExtMapper.class);

    /**
     * @param action (CBS電文AA)
     * @param txData
     */
    public CBS(ACBSAction action, MessageBase txData) {
        this.action = action;
        this.txData = txData;
        this.feptxn = txData.getFeptxn();
        this.feptxntcb = txData.getFeptxntcb();
        this.feptxnDao = txData.getFeptxnDao();
        this.logContext = txData.getLogContext();
        this.ej = txData.getEj();
        this.mINTLTXN = txData.getIntltxn();
    }

    /**
     * Bruce add 組送CBS主機原存交易電文
     *
     * @param txType FEP電子日誌序號  0:查詢, 1:入扣帳, 2:沖正, 3:授權, 4:解圏, 5.註記(自行提款用)
     * @return
     * @throws Exception
     */
    public FEPReturnCode sendToCBS(String txType, String TxRs) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.HostResponseTimeout;
        // 檢核CBS主機連線狀態
        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatCbs())) {
            if (this.feptxn.getFeptxnFiscFlag() == 1) {/*跨行 */
                /* 原存行交易 */
                if (StringUtils.isEmpty(this.feptxn.getFeptxnTxCode())) {
                    // 收信單位主機未在跨行作業運作狀態ReceiverBankOperationStop
                    return FEPReturnCode.ReceiverBankOperationStop;
                } else {/* 代理行交易 */
                    /*發信單位該項跨行業務停止SenderBankServiceStop*/
                    return FEPReturnCode.SenderBankServiceStop;
                }
            }
        }

        //IMSAdapter adapter = new IMSAdapter(this.txData);
        CBSAdapter adapter = new CBSAdapter(this.txData);
        if ("Y".equals(feptxn.getFeptxnCbsProc())) {            //3-1交易
            if ( "FISC".equals(feptxn.getFeptxnChannel()) ) {   //原存
                adapter.setCbsType(CBSType.FISC);
            }else{                                              //473X ATM交易(其他不走FEP)
                adapter.setCbsType(CBSType._473X);
            }
        }else { //3-2交易
            if ( "FISC".equals(feptxn.getFeptxnChannel()) ) {   //原存
                adapter.setCbsType(CBSType.FISCTCB);
            }else if ( StringUtils.equalsAny(feptxn.getFeptxnChannel(), "ATM", "EAT", "POS") ){  //473X代理交易
                adapter.setCbsType(CBSType.CBS);
            }else{                                              //其他外圍代理交易
                adapter.setCbsType(CBSType.NONATM);
            }
        }
        adapter.setCbsId(StringUtils.leftPad(txData.getEj().toString(), 8, '0'));
        // 組CBS 原存交易Request電文
        rtnCode = action.getCbsTita(txType);
        tita = action.getoTita();

        //本地測試暫時註解，正式環境需open
        if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIA") &&
                !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAII")) {
            RefString cbsmac = new RefString("");
            this.getLogContext().setMessage("tita:" + action.getTitaString());
            this.getLogContext().setRemark("tita 內容");
            this.logMessage(this.getLogContext());
            if (action.getTitaString().substring(30, 38).equals("F3F1F0F6") //3106 & 3107
                    || action.getTitaString().substring(30, 38).equals("F3F1F0F7")) {
//                rtnCode = this.makeCBSMacShort(cbsmac);  //不壓MAC
                this.getLogContext().setRemark("3106 & 3107 不壓CBS_MAC");
                this.logMessage(this.getLogContext());
            }
            else {
                rtnCode = this.makeCBSMac(cbsmac);
            }
            this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
            logMessage(this.logContext);
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

            StringBuilder resultBuilder = new StringBuilder(this.action.getTitaString());
            /* 更新Tita 電文第 55~59(ASCII) 位置值 */
            if (action.getTitaString().substring(30, 38).equals("F3F1F0F6") //3106 & 3107
                    || action.getTitaString().substring(30, 38).equals("F3F1F0F7")) {
                // 不壓MAC
            }/* 更新Tita 電文第 116~120(ASCII) 位置值 */
            else {
                resultBuilder.replace(232, 240, cbsmac.substring(0, 8));
            }
            this.action.setTitaToString(resultBuilder.toString());
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }
        //-----------------------------


        String processType = this.getImsPropertiesValue(tita, ImsMethodName.PROCESS_TYPE.getValue());
        switch (txType) {
            case "0": /*查詢，紀錄手續費優惠次數*/
                if (processType.equals("CHK")) {
                    this.feptxn.setFeptxnMsgflow("N1"); /* CBS CR REQUEST */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "1": /*入扣帳*/
                if (processType.equals("ACCT")) {
                    this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "2":
            case "5":   /*沖正, 註記*/
                this.feptxn.setFeptxnMsgflow("X1"); /*CBS EC Req*/
                break;
            case "4":  /*沖正手續費優惠次數*/
                this.feptxn.setFeptxnMsgflow("R1"); /*CBS 沖正手續費優惠次數*/
                break;
            case "3": /*授權*/
                this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                break;
            case "6":  /*確認*/
                this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                break;
        }
        if (rtnCode == FEPReturnCode.Normal) {
            if (StringUtils.isBlank(TxRs)) {
                //交易上送主機才需更新FEPTXN
                this.feptxn.setFeptxnCbsTimeout((short) 1); /* CBS逾時 FLAG */
                this.feptxn.setFeptxnAccType((short) 4);// 未明
                rtnCode = FEPReturnCode.HostResponseTimeout;
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToCBS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("MessageToIMS:" + adapter.getMessageToCBS());
                this.logContext.setRemark("Get CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                //adapter.setASCIIMessageToCBS(this.action.getASCIItitaToString());
                 this.logContext.setProgramName("CBS.SendToCBS");
                 this.logContext.setMessage("ASCII MessageToIMS:" + this.action.getASCIItitaToString());
                 this.logContext.setRemark("ASCII CBS LOG");
                 this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                 logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
            } else if (TxRs.equals("N")) {
                //adapter.setTxRs("N");
                adapter.setTimeout(0);
                //adapter.setTimeout(0);
                this.feptxn.setFeptxnMsgflow("E");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToCBS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("EndMessageToIMS:" + adapter.getMessageToCBS());
                this.logContext.setRemark("To CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                // adapter.setASCIIMessageToCBS(this.action.getASCIItitaToString());
                 this.logContext.setProgramName("CBS.SendToCBS");
                 this.logContext.setMessage("ASCII EndMessageToIMS:" + this.action.getASCIItitaToString());
                 this.logContext.setRemark("ASCII toCBS Text");
                 this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                 logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
                return FEPReturnCode.Normal;
            }
            //4. 	設定TIMER等待CBS主機回應訊息
            if (rtnCode == FEPReturnCode.HostResponseTimeout
                    || rtnCode == FEPReturnCode.ProgramException
                    || rtnCode == FEPReturnCode.CBSResponseError
                    || rtnCode == FEPReturnCode.CanNotConnectRemoteHost) {
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setRemark("GetMessageFromIMS Error");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);
                this.cbsPendProcess(txType);
                return rtnCode;
            }

            this.logContext.setProgramName("CBS.SendToCBS");
            this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
            this.logContext.setMessage("MessageFromCBS:" + adapter.getMessageFromCBS());
            this.logContext.setRemark("After Send to CBS, ReturnCode:" + rtnCode);
            logMessage(Level.INFO, logContext);
            if (StringUtils.isNotBlank(adapter.getMessageFromCBS())) {
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
                String ASCIIFromIMS = "" ;
                //判斷若3106/3107 直接轉ASCII
                if( "F3F1F0F6".equals(adapter.getMessageFromCBS().substring(68, 76)) || "F3F1F0F7".equals(adapter.getMessageFromCBS().substring(68, 76))
                        || "F9F1F0C1F0F0F3F7".equals(adapter.getMessageFromCBS().substring(106, 122))){
                    ASCIIFromIMS = EbcdicConverter.fromHex(adapter.getMessageFromCBS());
                }else {
                    ASCIIFromIMS = EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromCBS().substring(0, 192)) + adapter.getMessageFromCBS().substring(192, 200) + EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromCBS().substring(200));
                }
                this.logContext.setMessage("ASCII MessageFromCBS:" + ASCIIFromIMS);
                this.logContext.setRemark("ASCII fromCBS Text");
                logMessage(Level.INFO, logContext);
            }
            //5. 	收到CBS主機回應電文
            if (StringUtils.isNotBlank(adapter.getMessageFromCBS())) {//正常狀態
                rtnCode = this.action.processCbsTota(adapter.getMessageFromCBS(), txType);
                this.logContext.setRemark("after processCbsTota RC:" + rtnCode.toString());
                logMessage(this.logContext);
                //本地測試暫時註解，正式環境需open

                if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIA")
                        && !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAII")
                        && !"F3F1F0F6".equals(adapter.getMessageFromCBS().substring(68, 76))
                        && !"F3F1F0F7".equals(adapter.getMessageFromCBS().substring(68, 76)) ) {
                    String IMS_MAC = this.getImsPropertiesValue(this.action.getTota(), ImsMethodName.IMS_MAC.getValue());
                    this.logContext.setRemark("Tota IMS_MAC:" + IMS_MAC);
                    logMessage(this.logContext);
                    if (StringUtils.isBlank(IMS_MAC)) {
                        //寫EMS CheckCBSMacErr，繼續執行
                        this.logContext.setRemark("IMS_MAC is null or empty, ReturnCode:" + FEPReturnCode.ENCCheckMACError);
                        sendEMS(getLogContext());
                    } else {
                        this.logContext.setRemark("Before checkCBSMac RC:" + adapter.getMessageFromCBS().substring(192, 200));
                        logMessage(this.logContext);
                        FEPReturnCode rtnCode2 = this.checkCBSMac(adapter.getMessageFromCBS().substring(0, 136), adapter.getMessageFromCBS().substring(192, 200));

                        this.logContext.setRemark("after checkCBSMac RC:" + rtnCode2.toString());
                        logMessage(this.logContext);

                        if (rtnCode2 != FEPReturnCode.Normal) {
                            //寫EMS CheckCBSMacErr，繼續執行
                            this.logContext.setRemark("After checkCBSMac, ReturnCode:" + rtnCode2);
                            sendEMS(getLogContext());
                            rtnCode = rtnCode2;
                        }
                    }
                }
                //-----------------------------

                /* 變更交易記錄 */
                this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
            }
        }
        return rtnCode;
    }
    /**
     * Bruis add 組送IMS 主機跨行交易財金電文
     *
     *
     * @return FEPReturnCode
     * @throws Exception
     */
    public FEPReturnCode sendToIMS(String fISCMessage , FISCHeader fiscHeader,FISC_INBK fiscINBKRes ,FEPReturnCode rtnCode) throws Exception {
        this.logContext.setMessageFlowType(MessageFlow.Request);
        this.logContext.setProgramName(StringUtils.join(txData.getAaName(), ".process sendToIMS"));
        this.logContext.setMessage(txData.getTxRequestMessage());
        this.logContext.setRemark("Enter sendToIMS");
        logMessage(this.logContext);
        try {
            // 1. 更新交易記錄 (FEPTXN)
            feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); //訊息流程  ->  F1  =  FISC REQUEST
            feptxn.setFeptxnCbsTimeout((short) 1);              // FISC 逾時 FLAG
            feptxn.setFeptxnAaRc(Integer.valueOf("0601"));  // FEP Internal check error 預設為財金 TIMEOUT RC
            feptxn.setFeptxnPending((short) 0);                 // 交易狀況 0:正常 1:PENDING 2:解除PENDING
            feptxn.setFeptxnTxrust("S");                        // 處理結果 S:拒絕/不正常

            if (feptxnDao.updateByPrimaryKeySelective(feptxn) < 1) {
                getLogContext().setProgramName(ProgramName + ".sendToIMS");
                getLogContext().setRemark("FEPTXN UPDATE ERROR");
                sendEMS(getLogContext());
                return rtnCode;
            }

            // 2. 組送財金 Req 電文到 IMS
            String mMac;
            String conmacType = String.valueOf(txData.getMsgCtl().getMsgctlConmacType());
            CBSAdapter adapter = new CBSAdapter(txData);
            adapter.setCbsType(CBSType.FISC);
            adapter.setCbsId(StringUtils.leftPad(feptxn.getFeptxnStan(), 7, '0'));
            String apData = "";
            /* 財金Request電文MAC類別有值, 才押驗MAC結果給 IMS */
            if (StringUtils.isNotBlank(conmacType) ) {
                if (rtnCode == FEPReturnCode.Normal) {
                    mMac = "4001";
                } else {
                    mMac = "0302";
                }
                String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
                apData = fISCMessage;
                apData = apData.substring(0, (apData.length() - 8)) + mac;
                this.logContext.setRemark("to ims Message:" + apData + " ,new fisc mac :" + mac);
                logMessage(Level.DEBUG, this.logContext);
            }

            // 2024/6/21 新增 WAY 判斷
            feptxn.setFeptxnWay(txData.getMsgCtl().getMsgctlFisc2way()==(short)1 ? (short)2 : (short)3);

            // 發送交易至 IMS

            adapter.setMessageToCBS(apData);
            rtnCode = adapter.sendReceive();

            if (rtnCode != FEPReturnCode.Normal) {
                /* 主機TimeOut 不組回應電文給財金, 程式結束 */
                feptxn.setFeptxnAaRc(rtnCode.getValue());
                feptxn.setFeptxnTxrust("S");          //處理結果  S:拒絕/不正常  Reject-abnormal
                feptxn.setFeptxnAaComplete((short)1); //AA Close
                this.logContext.setProgramName("CBS.SendToIMS");
                this.logContext.setMessage("GetMessageFromIMS Error");
                this.logContext.setRemark("");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);
                if (feptxnDao.updateByPrimaryKeySelective(feptxn)<1) {
                    getLogContext().setProgramName(ProgramName + ".sendToIMS");
                    getLogContext().setRemark("組送財金 Req 電文到 IMS，FEPTXN UPDATE ERROR");
                    sendEMS(getLogContext());
                }
                return rtnCode; //程式結束
            }else{
                /* 將主機回應電文 RC, 寫入FEPTXN */
                this.logContext.setRemark("MessageToCBS Text");
                this.logContext.setMessage("MessageToCBS: " + adapter.getMessageToCBS() + ",CbsId:" + adapter.getCbsId());
                logMessage(this.logContext);
                this.logContext.setRemark("MessageFromCBS Text");
                this.logContext.setMessage("MessageFromCBS:" + adapter.getMessageFromCBS());
                logMessage(this.logContext);
                txData.setTxResponseMessage(adapter.getMessageFromCBS());

                fiscINBKRes = new FISC_INBK(adapter.getMessageFromCBS());
                fiscINBKRes.parseFISCMsg();
                fiscHeader.setFISCMessage(adapter.getMessageFromCBS());
                fiscHeader.parseFISCMsg();
                feptxn.setFeptxnRepRc(fiscINBKRes.getResponseCode());
                if (StringUtils.isNotBlank(fiscINBKRes.getBALA())) {
                    feptxn.setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA()));
                }
                if (StringUtils.isNotBlank(fiscINBKRes.getBALB())) {
                    feptxn.setFeptxnBalb(new BigDecimal(fiscINBKRes.getBALB()));
                }
            }

            // 3. 更新 FEPTXN
            feptxn.setFeptxnAaRc(rtnCode.getValue());
            this.logContext.setRemark("process updateFEPTXN");
            logMessage(Level.INFO, this.logContext);

            if (feptxn.getFeptxnWay() == 1) {
                //1 way
                feptxn.setFeptxnTxrust("A"); // 成功
            } else {
                //2 way or 3 way
                if ("4001".equals(feptxn.getFeptxnRepRc())) {
                    if ( txData.getMsgCtl().getMsgctlFisc2way() == (short)0 ) {
                        feptxn.setFeptxnPending((short)1); // Pending
                        feptxn.setFeptxnTxrust("B"); // Pending
                    } else {
                        feptxn.setFeptxnTxrust("A"); // 成功
                    }
                } else if ("0".equals(feptxn.getFeptxnTxrust())) { //初值:0
                    feptxn.setFeptxnTxrust("R"); // 拒絕-正常
                }
            }

            feptxn.setFeptxnMsgflow("F2"); // FISC Response
            feptxn.setFeptxnAaComplete((short) 1); // AA Close

            if (feptxnDao.updateByPrimaryKeySelective(feptxn)<1) {
                getLogContext().setProgramName(ProgramName + ".sendToIMS");
                getLogContext().setRemark("FEPTXN UPDATE ERROR");
                sendEMS(getLogContext());
                return rtnCode; // 若更新失敗則不送回應電文, 需人工處理
            }

            // 4. 將主機回覆電文傳回財金
            String message =txData.getTxResponseMessage();
            String mac = "";
            RefString refMac = new RefString(mac);

            this.logContext.setRemark("Send host response to FISC");
            logMessage(Level.INFO, this.logContext);

            if ( feptxn.getFeptxnWay() > 1 && rtnCode == FEPReturnCode.Normal ) {
                /* 財金Respone電文MAC類別有值, 才重押MAC */
                if ( txData.getMsgCtl().getMsgctlRepmacType() != null ) {
                    /*重押MAC, 置換主機回覆電文最後8碼的MAC，再轉給財金*/
                    mac = fiscINBKRes.getMAC();
                    refMac = new RefString(mac);
                    rtnCode = new ENCHelper( feptxn, txData ).makeFiscMac(fiscINBKRes.getMessageType(), refMac);
                    this.logContext.setRemark( "new fisc mac :" + refMac.get());
                    logMessage(Level.DEBUG, this.logContext);
                    if (rtnCode != FEPReturnCode.Normal) {
                        feptxn.setFeptxnAaRc(rtnCode.getValue());
                    } else {
                        message = message.substring(0, (message.length() - 8)) + refMac.get();
                        txData.setTxRequestMessage(message);
                    }
                }

                //財金Respone電文需置換 KEY SYNC
                switch (fiscINBKRes.getMessageType().substring(0, 2)) {
                    case "02": fiscINBKRes.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTcdsync()); break;
                    case "05": fiscINBKRes.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTrmsync()); break;
                    case "06":
                    case "08": fiscINBKRes.setSyncCheckItem(SysStatus.getPropertyValue().getSysstatTopcsync()); break;
                }
                fiscINBKRes.makeFISCMsg();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".sendToIMS");
            sendEMS(getLogContext());
        }
        return rtnCode;
    }

    /**
     * 設定TIMEOUT
     * @param txType
     * @param TxRs
     * @param timeout
     * @return
     * @throws Exception
     */
    public FEPReturnCode sendToCBSForSetTimeOut(String txType, String TxRs, int timeout,boolean checkCbsType,boolean insertCBSPend) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.HostResponseTimeout;
        // 檢核CBS主機連線狀態
        if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatCbs())) {
            if (this.feptxn.getFeptxnFiscFlag() == 1) {/*跨行 */
                /* 原存行交易 */
                if (StringUtils.isEmpty(this.feptxn.getFeptxnTxCode())) {
                    // 收信單位主機未在跨行作業運作狀態ReceiverBankOperationStop
                    return FEPReturnCode.ReceiverBankOperationStop;
                } else {/* 代理行交易 */
                    /*發信單位該項跨行業務停止SenderBankServiceStop*/
                    return FEPReturnCode.SenderBankServiceStop;
                }
            }
        }

        //IMSAdapter adapter = new IMSAdapter(this.txData);
        CBSAdapter adapter = new CBSAdapter(this.txData);
        adapter.setCbsType(CBSType.CBS);
        adapter.setCbsId(StringUtils.leftPad(txData.getEj().toString(), 8, '0'));
        if(insertCBSPend){
            adapter.setTimeout(timeout);
        }
        // 組CBS 原存交易Request電文
        rtnCode = action.getCbsTita(txType);
        tita = action.getoTita();
        //本地測試暫時註解，正式環境需open
        if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIA") &&
                !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAII")) {
            RefString cbsmac = new RefString("");
            if (action.getTitaString().substring(30, 38).equals("F3F1F0F6") //3106 & 3107
                    || action.getTitaString().substring(30, 38).equals("F3F1F0F7")) {
//                rtnCode = this.makeCBSMacShort(cbsmac); // 不壓MAC
            }
            else {
                rtnCode = this.makeCBSMac(cbsmac);
            }
            this.logContext.setRemark("make cbsmac EBCDIC:" + cbsmac.get());
            logMessage(this.logContext);
            if (rtnCode != FEPReturnCode.Normal) {
                return rtnCode;
            }

            StringBuilder resultBuilder = new StringBuilder(this.action.getTitaString());
            /* 更新Tita 電文第 55~59(ASCII) 位置值 */
            if (action.getTitaString().substring(30, 38).equals("F3F1F0F6") //3106 & 3107
                    || action.getTitaString().substring(30, 38).equals("F3F1F0F7")) {
//               // 不壓MAC
            }/* 更新Tita 電文第 116~120(ASCII) 位置值 */
            else {
                resultBuilder.replace(232, 240, cbsmac.substring(0, 8));
            }
            this.action.setTitaToString(resultBuilder.toString());
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }
        //-----------------------------


        String processType = this.getImsPropertiesValue(tita, ImsMethodName.PROCESS_TYPE.getValue());
        switch (txType) {
            case "0": /*查詢，紀錄手續費優惠次數*/
                if (processType.equals("CHK")) {
                    this.feptxn.setFeptxnMsgflow("N1"); /* CBS CR REQUEST */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "1": /*入扣帳*/
                if (processType.equals("ACCT")) {
                    this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                } else {
                    this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                }
                break;
            case "2":
            case "5":   /*沖正, 註記*/
                this.feptxn.setFeptxnMsgflow("X1"); /*CBS EC Req*/
                break;
            case "4":  /*沖正手續費優惠次數*/
                this.feptxn.setFeptxnMsgflow("R1"); /*CBS 沖正手續費優惠次數*/
                break;
            case "3": /*授權*/
                this.feptxn.setFeptxnMsgflow("I1"); /*CBS INQ Req*/
                break;
            case "6":  /*確認*/
                this.feptxn.setFeptxnMsgflow("H1"); /*CBS Req */
                break;
        }
        if (rtnCode == FEPReturnCode.Normal) {
            if (StringUtils.isBlank(TxRs)) {
                //交易上送主機才需更新FEPTXN
                this.feptxn.setFeptxnCbsTimeout((short) 1); /* CBS逾時 FLAG */
                this.feptxn.setFeptxnAccType((short) 4);// 未明
                rtnCode = FEPReturnCode.HostResponseTimeout;
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToCBS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("MessageToIMS:" + adapter.getMessageToCBS());
                this.logContext.setRemark("Get CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                //adapter.setASCIIMessageToCBS(this.action.getASCIItitaToString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("ASCII MessageToIMS:" + this.action.getASCIItitaToString());
                this.logContext.setRemark("ASCII CBS LOG");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
            } else if (TxRs.equals("N")) {
                //adapter.setTxRs("N");
                adapter.setTimeout(0);
                //adapter.setTimeout(0);
                this.feptxn.setFeptxnMsgflow("E");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
                if (this.feptxnDao.updateByPrimaryKeySelective(this.feptxn) <= 0) {// 回寫失敗
                    this.getLogContext().setRemark("回寫(FEPTXN)發生錯誤");
                    this.logMessage(this.getLogContext());
                    return FEPReturnCode.UpdateFail;
                }
                adapter.setMessageToCBS(this.action.getTitaString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("EndMessageToIMS:" + adapter.getMessageToCBS());
                this.logContext.setRemark("Get CBS Text");
                this.logContext.setProgramFlowType(ProgramFlow.CBSIn);
                logMessage(Level.INFO, logContext);

                //新增test log
                // adapter.setASCIIMessageToCBS(this.action.getASCIItitaToString());
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setMessage("ASCII EndMessageToIMS:" + this.action.getASCIItitaToString());
                this.logContext.setRemark("ASCII CBS LOG");
                this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                logMessage(Level.INFO, logContext);

                rtnCode = adapter.sendReceive();
                return FEPReturnCode.Normal;
            }
            //4. 	設定TIMER等待CBS主機回應訊息
            if (insertCBSPend) {
                if (rtnCode == FEPReturnCode.HostResponseTimeout
                        || rtnCode == FEPReturnCode.ProgramException
                        || rtnCode == FEPReturnCode.CBSResponseError) {
                    this.logContext.setProgramName("CBS.SendToCBS");
                    this.logContext.setMessage("GetMessageFromIMS Error");
                    this.logContext.setRemark("");
                    this.logContext.setProgramFlowType(ProgramFlow.CBSAscii);
                    logMessage(Level.INFO, logContext);
                    this.cbsPendProcess(txType);
                    return rtnCode;
                }
            }

            this.logContext.setProgramName("CBS.SendToCBS");
            this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
            this.logContext.setMessage("MessageFromCBS:" + adapter.getMessageFromCBS());
            this.logContext.setRemark("After Send to CBS, ReturnCode:" + rtnCode);
            logMessage(Level.INFO, logContext);
            if (StringUtils.isNotBlank(adapter.getMessageFromCBS())) {
                this.logContext.setProgramName("CBS.SendToCBS");
                this.logContext.setProgramFlowType(ProgramFlow.CBSOut);
                String ASCIIFromIMS = EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromCBS().substring(0, 192)) + adapter.getMessageFromCBS().substring(192, 200) + EbcdicConverter.fromHex(CCSID.English, adapter.getMessageFromCBS().substring(200));
                this.logContext.setMessage("ASCII MessageFromCBS:" + ASCIIFromIMS);
                this.logContext.setRemark("After Send to CBS, ReturnCode:" + rtnCode);
                logMessage(Level.INFO, logContext);
            }
            //5. 	收到CBS主機回應電文
            if (StringUtils.isNotBlank(adapter.getMessageFromCBS())) {//正常狀態
                rtnCode = this.action.processCbsTota(adapter.getMessageFromCBS(), txType);
                this.logContext.setRemark("after processCbsTota RC:" + rtnCode.toString());
                logMessage(this.logContext);
                //本地測試暫時註解，正式環境需open

                if (!EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAIA") &&
                        !EbcdicConverter.fromHex(CCSID.English, action.getTitaString()).startsWith("EAII")) {
                    String IMS_MAC = this.getImsPropertiesValue(this.action.getTota(), ImsMethodName.IMS_MAC.getValue());
                    this.logContext.setRemark("Tota IMS_MAC:" + IMS_MAC);
                    logMessage(this.logContext);
                    if (StringUtils.isBlank(IMS_MAC)) {
                        //寫EMS CheckCBSMacErr，繼續執行
                        this.logContext.setRemark("IMS_MAC is null or empty, ReturnCode:" + FEPReturnCode.ENCCheckMACError);
                        sendEMS(getLogContext());
                    } else {
                        this.logContext.setRemark("Before checkCBSMac RC:" + adapter.getMessageFromCBS().substring(192, 200));
                        logMessage(this.logContext);
                        FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
                        if(!"2".equals(txType) && checkCbsType){
                            rtnCode2 = this.checkCBSMac(adapter.getMessageFromCBS().substring(0, 136), adapter.getMessageFromCBS().substring(192, 200));
                        }
                        this.logContext.setRemark("after checkCBSMac RC:" + rtnCode2.toString());
                        logMessage(this.logContext);

                        if (rtnCode2 != FEPReturnCode.Normal) {
                            //寫EMS CheckCBSMacErr，繼續執行
                            this.logContext.setRemark("After checkCBSMac, ReturnCode:" + rtnCode2);
                            sendEMS(getLogContext());
                            rtnCode = rtnCode2;
                        }
                    }
                }
                //-----------------------------

                /* 變更交易記錄 */
                this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
                this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG
            }
        }
        return rtnCode;
    }

    private FEPReturnCode checkCBSMac(String TOTA, String IMS_MAC) {
        FEPReturnCode rtnCode = FEPReturnCode.ENCCheckMACError;
        try {

            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);

            String inputData1 = remainderToF0(TOTA);

            //oldmac
            String oldMAC = IMS_MAC;

            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            String newMAC = mac.get();
            this.logContext.setRemark("checkCBSMac newmac:" + newMAC + ", oldmac:" + oldMAC);
            logMessage(this.logContext);
            if (oldMAC.equals(newMAC))
                rtnCode = FEPReturnCode.Normal;
            else
                rtnCode = FEPReturnCode.ENCCheckMACError;
        } catch (Exception e) {
            this.logContext.setRemark("checkCBSMac newmac:" + "失敗");
            logMessage(this.logContext);
            return rtnCode = FEPReturnCode.ENCCheckMACError;
        }
        return rtnCode;
    }

    /**
     * 把字串補足至 8 的倍數 ，不足以F0補足
     *
     * @return
     */
    private String remainderToF0(String inputData) {
        String rtnStr = "";
        int instr = inputData.length();
        instr = instr / 2;
        if (instr % 8 != 0) {
            int remainder = instr % 8;
            remainder = 8 - remainder;
            rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
            rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
        }

        return inputData + rtnStr;
    }

    /**
     * 產生主機的MAC資料
     *
     * @param cbsmac
     * @return
     */
    private FEPReturnCode makeCBSMac(RefString cbsmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);
            String inputData = this.action.getTitaString().substring(0, 166);

            String inputData1 = inputData;
            inputData1 = remainderToF0(inputData1);
            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            // 4.若rtnCode=normal, 則cbsmac = mac
            if (rtnCode == FEPReturnCode.Normal) {
                cbsmac.set(mac.get());
            }
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
//			rtnCode = handleException(e, "makeCBSMac");
        }
        return rtnCode;
    }

    // 參加單位應用系統緊急停止後重新啟動通知交易
    private FEPReturnCode makeCBSMacShort(RefString cbsmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立ENCHelper物件
            ENCHelper encHelper = new ENCHelper(txData);
            String inputData = this.action.getTitaString().substring(0, 110);

            String inputData1 = inputData;
            inputData1 = remainderToF0(inputData1);
            // 3.呼叫
            RefString mac = new RefString(null);
            rtnCode = encHelper.makeCbsMacNew(inputData1, mac);
            // 4.若rtnCode=normal, 則cbsmac = mac
            if (rtnCode == FEPReturnCode.Normal) {
                cbsmac.set(mac.get());
            }
            this.logContext.setRemark("after makeCBSMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        } catch (Exception e) {
//			rtnCode = handleException(e, "makeCBSMac");
        }
        return rtnCode;
    }

    public FEPReturnCode sendToCBS(String txType) throws Exception {
        return sendToCBS(txType, "");
    }

    //沖正暫時不checkCBS  NBPYOtherRequestA/NBPYSelfIssue/NBPYSelfRequestA  專用
    public FEPReturnCode sendToCBSForNoCheckCBS(String txType) throws Exception {
        //2025/8/8 TimeOut時會寫CBSPEND..因為第一道己經寫了..這一道就不用再寫 否則會會造成DB ERROR
        return sendToCBSForSetTimeOut(txType, "", 0,false,false);
    }

    /**
     * 判斷及執行CBSPend處理
     *
     * @return
     * @throws Exception
     */
    private FEPReturnCode cbsPendProcess(String txType) throws Exception {
        if ("1".equals(txType) || "2".equals(txType)) {//1 :入扣帳,2 :沖正
            /* 入扣帳電文 TimeOut時,  才寫入 CBSPEND */
            Cbspend cbspend = new Cbspend();
            cbspend.setCbspendTxDate(this.feptxn.getFeptxnTxDate());
            cbspend.setCbspendZone("TWN");
            cbspend.setCbspendTxTime(this.feptxn.getFeptxnTxTime());
            cbspend.setCbspendEjfno(this.feptxn.getFeptxnEjfno());
            cbspend.setCbspendCbsTxCode(this.txData.getMsgCtl().getMsgctlTwcbstxid());
            if (txType.equals("1")) {
                cbspend.setCbspendReverseFlag((short) 0);
            } else {
                cbspend.setCbspendReverseFlag((short) 1);
            }
            cbspend.setCbspendSubsys(this.feptxn.getFeptxnSubsys());
            cbspend.setCbspendTbsdy(this.feptxn.getFeptxnTbsdy());
            cbspend.setCbspendTbsdyFisc(this.feptxn.getFeptxnTbsdyFisc());
            cbspend.setCbspendSuccessFlag((short) 0);//必須重送
            cbspend.setCbspendResendCnt((short) 0);
            cbspend.setCbspendAccType((short) 4);
            if (this.feptxn.getFeptxnTxAmtAct().compareTo(BigDecimal.ZERO) != 0) {
                cbspend.setCbspendTxAmt(this.feptxn.getFeptxnTxAmtAct());
            } else {
                cbspend.setCbspendTxAmt(this.feptxn.getFeptxnTxAmt());
            }
            if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTroutBkno())
                    || this.feptxn.getFeptxnPcode().startsWith("26")) {
                cbspend.setCbspendActno(this.feptxn.getFeptxnTroutActno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTrinBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTrinActno());
            } else if ( SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnTrinBkno())) {
                cbspend.setCbspendActno(this.feptxn.getFeptxnTrinActno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTroutBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTroutActno());
            } else {
                cbspend.setCbspendActno(this.feptxn.getFeptxnAtmno());
                cbspend.setCbspendIbBkno(this.feptxn.getFeptxnTroutBkno());
                cbspend.setCbspendIbActno(this.feptxn.getFeptxnTroutActno());
            }
            cbspend.setCbspendPcode(this.feptxn.getFeptxnPcode());
            cbspend.setCbspendTita(this.action.getTitaString());
            /* 2023/5/2 修改 for  CBS帳務分類 */
            cbspend.setCbspendBkno(this.feptxn.getFeptxnBkno());
            cbspend.setCbspendStan(this.feptxn.getFeptxnStan());
            cbspend.setCbspendChannel(this.feptxn.getFeptxnChannel());

            /*2025/7/29 修改　for 原存交易CBS帳務分類*/
            if(StringUtils.equals(this.feptxn.getFeptxnChannel(), FEPChannel.FISC.getNameS())){//原存交易
                if ( cbspend.getCbspendCbsTxCode().endsWith("1") ) {
                    cbspend.setCbspendCbsKind("A0");        /* 查詢交易結果(A0) */
                }else{ //CBSPEND_CBS_TX_CODE 最後一碼='2'
                    if ( StringUtils.equals(txType, "1") ) {    //入扣帳
                        cbspend.setCbspendCbsKind("A1");    /* 入帳交易(A1) */
                    }else{
                        cbspend.setCbspendCbsKind("A2");    /* 入帳交易(A2) */
                    }
                }
            } else { //代理交易
                if(StringUtils.equals(txType, "1")){
                    cbspend.setCbspendCbsKind("A3");
                } else {
                    cbspend.setCbspendCbsKind("A4");
                }
            }

//            cbspend.setUpdateTime(new Date());
            cbspend.setUpdateUserid(0);
            //寫入 CBSPEND
            if (cbspendExtMapper.insertSelective(cbspend) < 1) {// 寫入CBSPEND失敗
                this.getLogContext().setRemark("CBSPendProcess-Insert CBSPEND Error");
                this.getLogContext().setReturnCode(FEPReturnCode.CBSPENDInsertError);
                logMessage(Level.INFO, logContext);
                return FEPReturnCode.Normal;
            }
            this.logContext.setProgramName("CBS.cbsPendProcess");
            this.logContext.setMessage("CBSPendProcess-Insert CBSPEND success");
            this.logContext.setRemark("SendCBSPend");
            logMessage(Level.INFO, logContext);
            //寫入Queue 暫時關閉
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            String message = this.feptxn.getFeptxnTxDate() + ":" + cbspend.getCbspendEjfno(); // 讓代理可以送CON EJ
            sender.sendQueue(configuration.getQueueNames().getCbspend().getDestination(), message, null, null);
        }
        return FEPReturnCode.Normal;
    }

    public boolean isOtherChannel(FEPChannel channel){
        switch (channel){
            case HCE:
            case NBQ:
            case NBP:
            case NBB:
            case MBQ:
            case MQQ:
            case MSQ:
            case EOI:
            case NAM:
            case EIP:
            case MFT:
            case FID:
            case SSO:
            case ONL:
            case DIG:
            case BIZ:
            case HCA:
            case VO :
            case MCH:
            case EDI:
            case HLN:
            case VIP:
            case OPN:
            case IVR:
                return  true;
            default:
                return false;
        }
    }
}
