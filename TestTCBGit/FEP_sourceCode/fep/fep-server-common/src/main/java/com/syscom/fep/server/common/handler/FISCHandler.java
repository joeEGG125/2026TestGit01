package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.BitmapdefExtMapper;
import com.syscom.fep.mybatis.ext.mapper.DataattrExtMapper;
import com.syscom.fep.mybatis.model.Bitmapdef;
import com.syscom.fep.mybatis.model.Dataattr;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.*;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本類別為處理財金發動的交易,用來判斷該叫起何支AA
 * 1.寫MessageLog
 * 2.將財金電文拆解
 * 3.判斷要叫那一支AA
 * 4.呼叫AA
 * 5.將AA回傳值寫MessageLog
 *
 * @author Richard
 */
public class FISCHandler extends HandlerBase {
    private static final String ProgramName = StringUtils.join(FISCHandler.class.getSimpleName(), ".");
    private FISCGeneral general;
    private String RClientID;
    private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    private Feptxn feptxn;
    //臨時增加
    private static HashMap<String, List<Dataattr>> dataAttrList = new HashMap<String, List<Dataattr>>();
    private HashMap<String, Bitmapdef> bitmapdefMap = new HashMap<String, Bitmapdef>();
    private DataattrExtMapper dataattrExtMapper = SpringBeanFactoryUtil.getBean(DataattrExtMapper.class);
    private BitmapdefExtMapper bitmapdefExtMapper = SpringBeanFactoryUtil.getBean(BitmapdefExtMapper.class);
    
    public FISCHandler() {}

    @Override
    public String dispatch(FEPChannel channel, String data) {
        return StringUtils.EMPTY;
    }

    /**
     * @param channel
     * @param atmNo
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) throws Exception {
        return null;
    }

    /**
     * 財金發動交易專用
     *
     * @param subSystem
     * @param fiscSubSystem
     * @param channel
     * @param data
     * @return
     */
    public String dispatch(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        FISCData fData;
        general = new FISCGeneral();
        general.setSubSystem(fiscSubSystem);
        String fiscRes = StringUtils.EMPTY;
        // 2023-11-13 Richard add start
        // 前端沒帶進來就取, 有就直接用
        if (this.getEj() == 0) {
            this.setEj(TxHelper.generateEj());
        }
        if (StringUtils.isBlank(this.txRquid)) {
            this.txRquid = UUIDUtil.randomUUID(true);
        }
        // 2023-11-13 Richard add end
        // 記MessageLog
        LogData logData = new LogData();
        logData.setSubSys(subSystem);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        logData.setEj(this.getEj());
        logData.setTxRquid(this.txRquid);
        logData.setMessage(data);
        logMessage(Level.DEBUG, logData);
        try {
            // 拆財金電文Header
            RefBase<FISCHeader> refMsgReq = new RefBase<FISCHeader>(null);
            FEPReturnCode rtn = this.getFISCRequestHeader(fiscSubSystem, data, refMsgReq);
            FISCHeader msgReq = refMsgReq.get();

            // 準備財金交易通用物件
            fData = this.prepareFISCData(subSystem, fiscSubSystem, channel, data);
            fData.setLogContext(logData);
            fData.setMessageFlowType(msgReq.getMessageKind());

            // Get MsgID & MsgCtl
            String msgId = this.getMsgId(msgReq);
            LogHelperFactory.getTraceLogger().trace("FISCHandler GetMsgId ", msgId);

            fData.setMessageID(msgId);
            fData.setStan(msgReq.getSystemTraceAuditNo());
            logData.setMessageId(msgId);
            logData.setStan(msgReq.getSystemTraceAuditNo());

            // add by maxine on 2011/07/12 for 拆解財金電文 Header將 LogData.Stan 給值, 請同時將 LogData.PCODE & LogData.DesBKNO 給值.
            logData.setpCode(msgReq.getProcessingCode());
            logData.setDesBkno(StringUtils.rightPad(msgReq.getTxnDestinationInstituteId(), 7, ' ').substring(0, 3));

            if (StringUtils.isNotBlank(msgReq.getTxnSourceInstituteId()) && msgReq.getTxnSourceInstituteId().length() >= 3) {
                logData.setBkno(msgReq.getTxnSourceInstituteId().substring(0, 3));
            }

            // 2019-11-14 Modify by Ruling for 處理EJ為零時送EMS
            if (this.getEj() == 0) {
                logData.setMessage(data);
                logData.setReturnCode(FEPReturnCode.EJFNOTakeNumberError);
                logData.setExternalCode("EF2217");
                logData.setRemark("EJFNO取號有誤, EJ = 0");
                sendEMS(logData);
                return StringUtils.EMPTY;
            }

            this.messageId = msgId;
            fData.setMsgCtl(FEPCache.getMsgctrl(msgId));
            if (fData.getMsgCtl() == null) {
                fData.setMsgCtl(FEPCache.getMsgctrl("GARBLED"));
            }

            this.setChannel(fData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            String aaName = fData.getMsgCtl().getMsgctlAaName();
            String msgctlCbsProc = fData.getMsgCtl().getMsgctlCbsProc();
            String msgType = msgReq.getMessageType();

            if (rtn == FEPReturnCode.Normal) {
                if(StringUtils.isNotBlank(msgctlCbsProc)
                        && "02".equals(msgType.substring(msgType.length() - 2))
                        && "2".equals(this.general.getINBKConfirm().getProcessingCode().substring(0,1))){
                    checkBitmap(msgReq,fiscSubSystem,msgReq.getAPData());
                    FeptxnDao db = SpringBeanFactoryUtil.getBean("feptxnDao");
                    Calendar wk_TX_Date = CalendarUtil.rocStringToADDate("0" + msgReq.getTxnInitiateDateAndTime().substring(0, 6));
                    feptxn = db.getFEPTXNByReqDateAndStan(FormatUtil.dateTimeFormat(wk_TX_Date, FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN), msgReq.getTxnSourceInstituteId().substring(0, 3),
                            msgReq.getSystemTraceAuditNo());
                    if (feptxn != null && StringUtils.equalsIgnoreCase(feptxn.getFeptxnCbsProc(), "Y")) {
                        aaName = "FISCToIMSConfirmI";
                        fData.setAaName(aaName);
                        logData.setProgramName(fData.getAaName());
                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                        this.runYAA(msgId, msgReq, fData);
                    }else {
                        fData.setAaName(aaName);
                        logData.setProgramName(fData.getAaName());
                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                        this.runYAA(msgId, msgReq, fData);
                    }
                }
                else if(StringUtils.isNotBlank(msgctlCbsProc) && "Y".equals(msgctlCbsProc)){
                    /* 2024/8/5 點掉清算電文 */
                    if ("00".equals(msgType.substring(msgType.length() - 2))) {
                        // isFisc = false;
                        aaName = "FISCToIMSRequestI";
                        fData.setAaName(aaName);
                        logData.setProgramName(fData.getAaName());
                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                        this.runYAA(msgId, msgReq, fData);
                        /* 2024/8/30 點掉, 移至上面判斷 */
//                    }else if( "02".equals(msgType.substring(msgType.length() - 2))) {
//                        aaName = "FISCToIMSConfirmI";
//                        fData.setAaName(aaName);
//                        logData.setProgramName(fData.getAaName());
//                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
//                        this.runYAA(msgId, msgReq, fData);

                       /* 2025/1/7 點掉, 移至下面判斷(0699-3209)  */
//                    } else if ("99".equals(msgType.substring(msgType.length() - 2))) {
//                        /* 2024/8/5 修改 for  Gabeled Message(0699-3209)  */
//                        aaName = "FISCToIMSRequestI";
//                        fData.setAaName(aaName);
//                        logData.setProgramName(fData.getAaName());
//                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
//                        this.runYAA(msgId, msgReq, fData);
                    }
                    /* 2025/1/7 修改 for  Gabeled Message(0699-3209)  */
                }else if(StringUtils.isNotBlank(msgctlCbsProc) && "G".equals(msgctlCbsProc)){
                    /* 判斷原交易決定送主機或FEP 處理 */
                    feptxn = GetGarbledOriFEPTxn();
                    if (feptxn != null){
                        if (StringUtils.equalsIgnoreCase(feptxn.getFeptxnCbsProc(), "N")) {
                            /* 分PCODE已上線交易->送FEP處理 */
                            aaName = "AA3209";
                            fData.setAaName(aaName);
                            logData.setProgramName(fData.getAaName());
                            fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                            this.runYAA(msgId, msgReq, fData);
                        }else {
                            aaName = "FISCToIMSRequestI";
                            fData.setAaName(aaName);
                            logData.setProgramName(fData.getAaName());
                            fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                            this.runYAA(msgId, msgReq, fData);
                        }
                    }else{
                        /* 2024/11/26 修改for 查無原交易, 送 EMS */
                        logData.setMessage(data);
                        logData.setRemark("Garbled 訊息查無原交易");
                        sendEMS(logData);
                        return StringUtils.EMPTY; /*程式結束*/
                    }
                }  else if (StringUtils.isNotBlank(msgctlCbsProc) && "P".equals(msgctlCbsProc)){
                    checkBitmap(msgReq,fiscSubSystem,msgReq.getAPData());
                    feptxn = checkoriFEPTXN();
                    if (feptxn != null){
                        if (StringUtils.equalsIgnoreCase(feptxn.getFeptxnCbsProc(), "N")) {
                            /* 分PCODE已上線交易->送FEP處理 */
                            aaName = "PendingAskFromFISC";
                            fData.setAaName(aaName);
                            logData.setProgramName(fData.getAaName());
                            fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                            this.runYAA(msgId, msgReq, fData);
                        }else{
                            aaName = "FISCToIMSRequestI";
                            fData.setAaName(aaName);
                            logData.setProgramName(fData.getAaName());
                            fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                            this.runYAA(msgId, msgReq, fData);
                        }
                    }else{
                        /* 2025/04/26 修改for 查無原交易, 送IMS */
                        logData.setMessage(data);
                        logData.setRemark("Pending交易在FEP查無原交易");
                        logMessage(Level.DEBUG, logData);

                        aaName = "FISCToIMSRequestI";
                        fData.setAaName(aaName);
                        logData.setProgramName(fData.getAaName());
                        fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                        this.runYAA(msgId, msgReq, fData);
                    }
                }else if (StringUtils.isNotBlank(msgctlCbsProc) && "N".equals(msgctlCbsProc)){
                    fData.setAaName(aaName);
                    logData.setProgramName(fData.getAaName());
                    fData.setTxStatus(fData.getMsgCtl().getMsgctlStatus() == 1);
                    // modified by maxine for 不同系統呼叫不同的RunAA
                    switch (fiscSubSystem) {
                        case INBK:
                        case EMVIC:
                            fiscRes = this.runAA(msgId, msgReq, fData);
                            break;
                        case CLR:
                        case FCCLR:
                        case OPC:
                            fiscRes = this.runAA(msgId, msgReq, fData);
                            break;
                        case RM:
                            fiscRes = this.runRMAA(msgId, msgReq, fData);
                            break;
                        default:
                            break;
                    }
                }

            } else {
                return StringUtils.EMPTY;
            }

            return fiscRes;
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
            return fiscRes;
        } finally {
            // 記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setMessage(fiscRes);
            logMessage(Level.DEBUG, logData);
        }
    }

    /**
     * @throws Exception
     */
    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    /**
     * add By Maxine on 2011/09/02 for UI_015313 logData加上TxUser
     *
     * @param channel
     * @param data
     * @param txcd
     * @param txuser
     * @return
     */
    public boolean dispatch(FEPChannel channel, INBKGeneral data, String txcd, String txuser) {
        INBKData txINBKData;
        @SuppressWarnings("unused")
        String rtnMsg = StringUtils.EMPTY;
        this.ej = TxHelper.generateEj();
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(channel);
        logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
        logData.setEj(this.ej);
        logData.setTxRquid(this.txRquid);
        try {
            txINBKData = new INBKData();
            txINBKData.setEj(this.ej);
            txINBKData.setTxRquid(this.txRquid);
            txINBKData.setTxObject(data);
            txINBKData.setTxChannel(channel);
            txINBKData.setTxSubSystem(SubSystem.INBK);
            txINBKData.setTxRequestMessage(StringUtils.EMPTY);
            logData.setMessageId(txcd);
            logData.setTxUser(txuser);

            txINBKData.setLogContext(logData);
            txINBKData.setMessageID(txcd);
            txINBKData.setMsgCtl(FEPCache.getMsgctrl(txcd));
            this.setChannel(txINBKData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            txINBKData.setEj(this.ej);
            txINBKData.setTxRquid(this.txRquid);
            txINBKData.setAaName(txINBKData.getMsgCtl().getMsgctlAaName());
            txINBKData.setTxStatus(txINBKData.getMsgCtl().getMsgctlStatus() == 1);
            rtnMsg = this.runINBKAA(txcd, txINBKData);
        } catch (Throwable e) {
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
            logData.setMessage(StringUtils.EMPTY);
            logMessage(Level.DEBUG, logData);
        }
        return true;
    }

    @SuppressWarnings("unused")
    private FISCHeader getFISCRequestHeader(FISCSubSystem fiscSubSystem, String data) {
        switch (fiscSubSystem) {
            case INBK:
                this.general.setINBKRequest(new FISC_INBK(data));
                return this.general.getINBKRequest();
            case OPC:
                this.general.setOPCRequest(new FISC_OPC(data));
                this.general.setOPCResponse(new FISC_OPC());
                return this.general.getOPCRequest();
            case RM:
                this.general.setRMRequest(new FISC_RM(data));
                this.general.setRMResponse(new FISC_RM());
                return this.general.getRMRequest();

            default:
                return null;
        }
    }

    private FEPReturnCode getFISCRequestHeader(FISCSubSystem fiscSubSystem, String data, RefBase<FISCHeader> msgReq) {
        FISC_RM tmpFISC_RM;
        FISC_INBK tmpFISC_INBK;
        FISC_OPC tmpFISC_OPC;
        FISC_CLR tmpFISC_CLR;
        FISC_USDCLR tmpFISC_FCCLR;
        FISC_EMVIC tmpFISC_EMVIC;
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        switch (fiscSubSystem) {
            case INBK:
                tmpFISC_INBK = new FISC_INBK(data);
                rtnCode = tmpFISC_INBK.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Request) {
                        general.setINBKRequest(tmpFISC_INBK);
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKRequest());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Response) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(tmpFISC_INBK);
                        general.setINBKConfirm(new FISC_INBK());
                        msgReq.set(general.getINBKResponse());
                    }
                    if (tmpFISC_INBK.getMessageKind() == MessageFlow.Confirmation) {
                        general.setINBKRequest(new FISC_INBK());
                        general.setINBKResponse(new FISC_INBK());
                        general.setINBKConfirm(tmpFISC_INBK);
                        msgReq.set(general.getINBKConfirm());
                    }
                }
                break;
            case EMVIC:
                tmpFISC_EMVIC = new FISC_EMVIC(data);
                rtnCode = tmpFISC_EMVIC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Request) {
                        general.setEMVICRequest(tmpFISC_EMVIC);
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICRequest());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Response) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(tmpFISC_EMVIC);
                        general.setEMVICConfirm(new FISC_EMVIC());
                        msgReq.set(general.getEMVICResponse());
                    }
                    if (tmpFISC_EMVIC.getMessageKind() == MessageFlow.Confirmation) {
                        general.setEMVICRequest(new FISC_EMVIC());
                        general.setEMVICResponse(new FISC_EMVIC());
                        general.setEMVICConfirm(tmpFISC_EMVIC);
                        msgReq.set(general.getEMVICConfirm());
                    }
                }
                break;
            case OPC:
                tmpFISC_OPC = new FISC_OPC(data);
                rtnCode = tmpFISC_OPC.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Request) {
                        general.setOPCRequest(tmpFISC_OPC);
                        general.setOPCResponse(new FISC_OPC());
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCRequest());
                    }
                    if (tmpFISC_OPC.getMessageKind() == MessageFlow.Response) {
                        general.setOPCRequest(new FISC_OPC());
                        general.setOPCResponse(tmpFISC_OPC);
                        general.setOPCConfirm(new FISC_OPC());
                        msgReq.set(general.getOPCResponse());
                    }
                }
                break;
            case RM:
                tmpFISC_RM = new FISC_RM(data);
                rtnCode = tmpFISC_RM.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_RM.getMessageKind() == MessageFlow.Request) {
                        general.setRMRequest(tmpFISC_RM);
                        general.setRMResponse(new FISC_RM());
                        msgReq.set(general.getRMRequest());
                    }
                    if (tmpFISC_RM.getMessageKind() == MessageFlow.Response) {
                        general.setRMRequest(new FISC_RM());
                        general.setRMResponse(tmpFISC_RM);
                        msgReq.set(general.getRMResponse());
                    }
                }
                break;
            case CLR:
                tmpFISC_CLR = new FISC_CLR(data);
                rtnCode = tmpFISC_CLR.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Request) {
                        general.setCLRRequest(tmpFISC_CLR);
                        general.setCLRResponse(new FISC_CLR());
                        msgReq.set(general.getCLRRequest());
                    }
                    if (tmpFISC_CLR.getMessageKind() == MessageFlow.Response) {
                        general.setCLRRequest(new FISC_CLR());
                        general.setCLRResponse(tmpFISC_CLR);
                        msgReq.set(general.getCLRResponse());
                    }
                }
                break;
            case FCCLR:
                tmpFISC_FCCLR = new FISC_USDCLR(data);
                rtnCode = tmpFISC_FCCLR.parseFISCMsg();
                if (rtnCode == CommonReturnCode.Normal) {
                    if (tmpFISC_FCCLR.getMessageKind() == MessageFlow.Request) {
                        general.setFCCLRRequest(tmpFISC_FCCLR);
                        general.setFCCLRResponse(new FISC_USDCLR());
                        msgReq.set(general.getFCCLRRequest());
                    }
                    if (tmpFISC_FCCLR.getMessageKind() == MessageFlow.Response) {
                        general.setFCCLRRequest(new FISC_USDCLR());
                        general.setFCCLRResponse(tmpFISC_FCCLR);
                        msgReq.set(general.getFCCLRResponse());
                    }
                }
                break;
            default:
                rtnCode = FEPReturnCode.Abnormal;
                break;
        }
        return rtnCode;
    }

    private FISCData prepareFISCData(SubSystem subSystem, FISCSubSystem fiscSubSystem, FEPChannel channel, String data) {
        FISCData fData = new FISCData();
        fData.setTxObject(this.general);
        fData.setTxChannel(channel);
        fData.setTxSubSystem(subSystem);
        fData.setFiscTeleType(fiscSubSystem);
        fData.setTxRequestMessage(data);
        fData.setEj(this.getEj());
        fData.setTxRquid(this.txRquid);
        fData.setRClientID(this.RClientID);
        return fData;
    }

    private String getMsgId(FISCHeader msgReq) {
        // 讀出交易控制檔本筆交易資料
        // INBK MsgID=PCODE+MsgType後2碼
        String msgId = StringUtils.join(msgReq.getProcessingCode(), msgReq.getMessageType().substring(2));
        if ("0202".equals(msgReq.getMessageType()) && "2000".equals(msgReq.getProcessingCode())) {
            msgId = msgReq.getSyncCheckItem();
        }
        return msgId;
    }

    private String runAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runYAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRequestYData", FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runRMAA(String msgId, FISCHeader msgReq, FISCData fData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processRmRequestData", String.class, FISCHeader.class, FISCData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, msgReq, fData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private String runINBKAA(String msgId, INBKData txINBKData) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", String.class, INBKData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, msgId, txINBKData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private Feptxn checkoriFEPTXN() throws Exception {
        String pCode = this.general.getINBKRequest().getProcessingCode();
        String dueDate = this.general.getINBKRequest().getDueDate();
        //for select
        String oriStan = this.general.getINBKRequest().getOriStan();
        String stan = null;
        String bkno = null;
        String txDate = null;
        if (StringUtils.isNotBlank(oriStan)) {
        	bkno = oriStan.substring(0, 3);
            stan = oriStan.substring(3, 10);
            txDate = CalendarUtil.rocStringToADString(StringUtils.leftPad(String.valueOf(dueDate), 7, '0')); //民國轉西元
        }
        switch (pCode) {
            case "2120":
            case "2150":
                if (!StringUtils.equals(dueDate, "000000")) {
//                    feptxn = oriDBFEPTXN.getoriFEPTXNData2120(bkno, stan, txDate);
                    feptxn = oriDBFEPTXN.getFeptxnByStan(txDate, bkno, stan);
                }

                break;
            case "2270":
                if (!StringUtils.equals(dueDate, "000000")) {
                    String tbsdyfisc = null;
                    tbsdyfisc = SysStatus.getPropertyValue().getSysstatTbsdyFisc();
                    feptxn = oriDBFEPTXN.getoriFEPTXNData2270(tbsdyfisc, bkno, stan);
                    if (feptxn == null) {
                        tbsdyfisc = SysStatus.getPropertyValue().getSysstatLbsdyFisc();
                        feptxn = oriDBFEPTXN.getoriFEPTXNData2270(tbsdyfisc, bkno, stan);
                    }
                }
                break;

        }
        return feptxn;
    }

    private Feptxn GetGarbledOriFEPTxn() throws Exception {
        /* 拆解 ReqOPC.PCODE = ‘3209’ */
        String W_REMARK = CodeGenUtil.ebcdicToAsciiDefaultEmpty(this.general.getOPCRequest().getAPData());

        String dueDate = null;
        String stan = null;
        String bkno = null;
        String txDate = null;
        if (StringUtils.isNotBlank(W_REMARK)) {
            bkno = W_REMARK.substring(22, 25);
            stan = W_REMARK.substring(8, 15);
            dueDate = W_REMARK.substring(29, 35);
            txDate = CalendarUtil.rocStringToADString(StringUtils.leftPad(String.valueOf(dueDate), 7, '0')); //民國轉西元
        }
        if (!StringUtils.equals(dueDate, "000000")) {
            feptxn = oriDBFEPTXN.getFeptxnByStan(txDate, bkno, stan);
        }
        return feptxn;
    }

    public String getRClientID() {
        return RClientID;
    }

    public void setRClientID(String RClientID) {
        this.RClientID = RClientID;
    }

    public FEPReturnCode checkBitmap(FISCHeader fiscHeader, FISCSubSystem fiscSubSystem, String apdata) {
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
                    switch (fiscSubSystem) {
                        case INBK:
                            fiscINBK = this.general.getINBKRequest();;
                            break;
                        case OPC:
                            fiscOPC = this.general.getOPCRequest();
                            break;
                        case CLR:
                            fiscCLR = this.general.getCLRRequest();
                            break;
                        case FCCLR:
                            fiscFCCLR = this.general.getFCCLRRequest();
                            break;
                        case EMVIC:
                            fiscEMVIC = this.general.getEMVICRequest();
                            break;
                        default:
                        	break;
                    }
                    break;
                case "02":
                    switch (fiscSubSystem) {
                        case INBK:
                            fiscINBK = this.general.getINBKConfirm();
                            break;
                        case OPC:
                            fiscOPC = this.general.getOPCConfirm();
                            break;
                        case EMVIC:
                            fiscEMVIC = this.general.getEMVICConfirm();
                            break;
                        default:
                        	break;
                    }
                    break;
                case "10":
                    switch (fiscSubSystem) {
                        case INBK:
                            fiscINBK = this.general.getINBKRequest();;
                            break;
                        case OPC:
                            fiscOPC = this.general.getOPCResponse();
                            break;
                        case CLR:
                            fiscCLR = this.general.getCLRResponse();
                            break;
                        case FCCLR:
                            fiscFCCLR = this.general.getFCCLRResponse();
                            break;
                        case EMVIC:
                            fiscEMVIC = this.general.getEMVICResponse();
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
                                getLogContext().setRemark("ChkBitmap(TAC) 第" + (i + "位訊息格式錯誤,TAC長度錯誤!"));
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
                                getLogContext().setRemark("ChkBitmap第" + (i + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (fiscSubSystem) {
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
                                getLogContext().setRemark("ChkBitmap第" + (i + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (fiscSubSystem) {
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
                                getLogContext().setRemark("ChkBitmap第" + (i + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (fiscSubSystem) {
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
                                getLogContext().setRemark("ChkBitmap第" + (i + "位訊息格式錯誤(" + tmpAscii + ";" + tmpHex + ")"));
                                getLogContext().setProgramName(ProgramName + Thread.currentThread().getStackTrace()[1].getMethodName());
                                getLogContext().setReturnCode(FISCReturnCode.MessageFormatError); // 2021-06-09 Richard add 必須要塞入ReturnCode否則後面會有空指針異常
                                logMessage(Level.ERROR, getLogContext());
                                rtnCode = FISCReturnCode.MessageFormatError;
                            } else {
                                /* 欄位拆解正確, 搬入系統別財金電文 */
                                switch (fiscSubSystem) {
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
                                    switch (fiscSubSystem) {
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
                                    switch (fiscSubSystem) {
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
                                switch (fiscSubSystem) {
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
    
    private List<Dataattr> getDataAttributeDataByType(String attrType) {
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
    
    private Bitmapdef getBitmapData(String msgTypePcode) {
        // Fly 2016/11/25 避免多執行續同時對BitmapData做操作，增加LOCK的範圍
        synchronized (bitmapdefMap) {
            if (bitmapdefMap.size() == 0) {
                this.loadBitmapData();
            }
            return bitmapdefMap.get(msgTypePcode);
        }
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
}
