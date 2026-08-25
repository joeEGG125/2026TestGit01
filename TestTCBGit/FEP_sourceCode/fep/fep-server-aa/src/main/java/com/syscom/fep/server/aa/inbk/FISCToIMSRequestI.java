package com.syscom.fep.server.aa.inbk;

import com.mchange.lang.IntegerUtils;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.InbkpendExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.CBSAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.text.fisc.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

public class FISCToIMSRequestI extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private FEPReturnCode rtnCode2 = FEPReturnCode.Normal;
    private String rtnMessage = StringUtils.EMPTY;
    private String fiscTeleType = StringUtils.EMPTY;
    private FISCHeader fiscHeader;
    private FISC_INBK fiscINBKRes;
    private FISC_OPC fiscOPCRes;
    private FISC_EMVIC fiscEMVICRes;
    private FISC_CLR fiscCLRRes;
    private Short ReqmacType = null;
    private Short RepmacType = null;
    private String pCode = null;
    private Zone zone = new Zone();
    private Inbkpend defINBKPEND;
    private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private InbkpendExtMapper dbINBKPEND = SpringBeanFactoryUtil.getBean(InbkpendExtMapper.class);
    private SysconfExtMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

    public FISCToIMSRequestI(FISCData txnData) throws Exception {
        super(txnData);
        fiscTeleType = getTxData().getFiscTeleType().toString();
        switch (fiscTeleType) {
            case "CLR":
                fiscHeader = getFiscCLRReq();
                fiscCLRRes = getFiscCLRRes();
                break;
            case "EMVIC":
                fiscHeader = getFiscEMVICReq();
                fiscEMVICRes = getFiscEMVICRes();
                break;
            case "OPC":
                fiscHeader = getFiscOPCReq();
                fiscOPCRes = getFiscOPCRes();
                break;
            case "INBK":
                fiscHeader = getFiscReq();
                fiscINBKRes = getFiscRes();
                break;
        }
        ReqmacType = this.getTxData().getMsgCtl().getMsgctlReqmacType();
        RepmacType = this.getTxData().getMsgCtl().getMsgctlRepmacType();
        pCode = fiscHeader.getProcessingCode();
    }

    @Override
    public String processRequestData() throws Exception {
        try {
            // 記錄LOG
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage(this.getTxData().getTxRequestMessage());
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);

            //1.    檢核財金電文 Header
            if (StringUtils.equalsAny(pCode, "3201", "3209")){
                rtnCode = getFiscBusiness().checkHeader(fiscHeader, false);
            }else {
                rtnCode = getFiscBusiness().checkHeader(fiscHeader, true);
            }
            String sFiscRc = TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.FISC, getLogContext());
            if ("10".equals(sFiscRc.substring(0, 2))) {
                getFiscBusiness().setFeptxn(null);
                getFiscBusiness().sendGarbledMessage(fiscHeader.getEj(), rtnCode, fiscHeader);
                return StringUtils.EMPTY; /*程式結束*/
            }

            // 2. 	AddTxData:新增交易記錄(FEPTXN)
            //(2.1) 	Prepare() 交易記錄初始資料
            /* 2024/8/2修改 for  OPC 電文 */
            pCode = fiscHeader.getProcessingCode();
            if (StringUtils.equalsAny(pCode, "3201", "3205", "3206")) {
                if (StringUtils.isNotBlank(getFiscOPCReq().getNoticeId()))
                    getFiscBusiness().getFeptxn().setFeptxnNoticeId(getFiscOPCReq().getNoticeId());
                if (StringUtils.isNotBlank(getFiscOPCReq().getNoticeData()))
                    getFiscBusiness().getFeptxn().setFeptxnRemark(getFiscOPCReq().getNoticeData());
                rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
            } else if (StringUtils.equalsAny(pCode, "2120", "2150", "2270")) {
                rtnCode = this.prepareINBKPEND();
            } else {
                rtnCode = this.prepareFEPTXN();
            }
            if (rtnCode != FEPReturnCode.Normal )
            {
                /*程式結束*/
                getLogContext().setProgramName(ProgramName + ".prepareTxData");
                getLogContext().setRemark("TxData PREPARE ERROR");
                getLogContext().setMessage(this.getTxData().getTxRequestMessage());
                sendEMS(getLogContext());
                return StringUtils.EMPTY;
            }
            if (StringUtils.equalsAny(pCode, "2555", "2556")){
                getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(getFiscBusiness().getFeptxn().getFeptxnTxAmt());
            }


            //(2.2) 	新增交易記錄
            getFiscBusiness().getFeptxn().setFeptxnMsgflow("CR");   //訊息流程  ->  CR  =  CBS Request for 分Pcode
            getFiscBusiness().getFeptxn().setFeptxnCbsProc("Y");    //主機交易  ->  Y:主機交易,N:FEP交易
            /* 2025/11/25 註解未在SPEC的部分   */
//            String wDate = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, '0'));
//            getFiscBusiness().getFeptxn().setFeptxnTxDate(wDate);
//            getFiscBusiness().getFeptxn().setFeptxnTxTime(fiscHeader.getTxnInitiateDateAndTime().substring(6, 12));
            rtnCode = getFiscBusiness().insertFEPTxn();             //檔名SEQ為 FEPTXN_TBSDY_FISC[7:2] 財金營業日
            if (StringUtils.equalsAny(pCode, "2120", "2150", "2270")){
                dbINBKPEND.insertSelective(defINBKPEND);
            }
            if (rtnCode != FEPReturnCode.Normal )
            {
                getLogContext().setProgramName(ProgramName + ".insert TxData");
                getLogContext().setRemark("TxData insert ERROR");
                getLogContext().setMessage(this.getTxData().getTxRequestMessage());
                sendEMS(getLogContext());
                return StringUtils.EMPTY; /*程式結束*/
            }

            //(2.3) 	檢核 MAC
            checkMac();
            //檢核MAC
            if (rtnCode != FEPReturnCode.Normal )
            {
                getLogContext().setProgramName(ProgramName + ".check Mac");
                getLogContext().setRemark("checkMac ERROR");
                sendEMS(getLogContext());
            }




            // 3. 	更新交易記錄(FEPTXN)
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);     //訊息流程  ->  F1  =  FISC REQUEST
            getFiscBusiness().getFeptxn().setFeptxnFiscTimeout((short) 1);                      // FISC 逾時 FLAG  ->  0:正常 1:逾時
            getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue()); //FEP內部錯誤代碼  FEP Internal check error 之代碼  預設為財金 TIMEOUT 之 RC
            getFiscBusiness().getFeptxn().setFeptxnTxrust("S");                                 //處理結果  S:拒絕/不正常  Reject-abnormal
            /* 2025/2/27 修改, 點掉, 移至下面處理 */
//            rtnCode = getFiscBusiness().updateTxData();
//            if (rtnCode != FEPReturnCode.Normal) {
//                getLogContext().setProgramName(ProgramName + ".AddTxData");
//                getLogContext().setMessage("FEPTXN UPDATE ERROR");
//                sendEMS(getLogContext());
//            }

            // 4.   FEP換日處理
            if(StringUtils.equals(pCode, "3201")
                    && StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnNoticeId(), "2001")){
                rtnCode = getFiscBusiness().changeDate("");
            }

            // 5. 	SendToIMS送財金Req電文到主機
            sendToCBS();
            if (feptxn.getFeptxnWay()!=1) {
                if (rtnCode == FEPReturnCode.Normal) {
                    switch (fiscTeleType) {
                        case "CLR":
                            rtnMessage = CLRResponse();
                            break;
                        case "EMVIC":
                            rtnMessage = EMVICResponse();
                            break;
                        case "OPC":
                            rtnMessage = OPCResponse();
                            break;
                        case "INBK":
                            rtnMessage = INBKResponse();
                            break;
                    }
                } else {
                    /* 主機TimeOut 不組回應電文給財金, 程式結束 */
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("S");  /*Reject-abnormal*/
                    getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); /*AA Close*/
                    getFiscBusiness().updateTxData();//更新交易記錄
                    return StringUtils.EMPTY;
                }
            }



            // 6. 	更新FEPTXN
            rtnCode2 = updateFEPTXN();
            if (rtnCode2 != FEPReturnCode.Normal) {
                /*若更新失敗則不送回應電文, 人工處理*/
                getLogContext().setProgramName(ProgramName + ".updateFEPTXN");
                getLogContext().setRemark("FEPTXN UPDATE ERROR");
                sendEMS(getLogContext());
                return StringUtils.EMPTY; /*程式結束*/
            }



            // 7. 	將主機回覆電文傳回財金
            if (rtnCode == FEPReturnCode.Normal && feptxn.getFeptxnWay()> 1) {
                this.makeFiscMac();
                rtnCode = getFiscBusiness().sendToFISCFromCBSRespons();
            }

            // 8.   2120/2150/2270 還原財金營業日
            /* 2025/4/30 修改 for  2120/2150/2270 還原財金營業日*/
            if (StringUtils.equalsAny(pCode, "2120","2150","2270")) {
                getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc( getFiscBusiness().getFeptxn().getFeptxnTbsdyAct() );
                rtnCode = getFiscBusiness().updateTxData();
            }

        } catch (Exception ex) {
            this.rtnCode = FEPReturnCode.ProgramException;
            this.logContext.setProgramException(ex);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
            sendEMS(this.logContext);
        } finally {
            this.getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            this.getTxData().getLogContext().setMessage(rtnMessage);
            this.getTxData().getLogContext().setProgramName(this.aaName);
            this.getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, this.logContext);
        }
        return rtnMessage;
    }

    private void checkMac() {
        String messageType = "";
        String mac = "";
        switch (fiscTeleType) {
            case "CLR":
                messageType = getFiscCLRReq().getMessageType();
                mac = getFiscCLRReq().getMAC();
                break;
            case "EMVIC":
                messageType = getFiscEMVICReq().getMessageType();
                mac = getFiscEMVICReq().getMAC();
                break;
            case "OPC":
                messageType = getFiscOPCReq().getMessageType();
                mac = getFiscOPCReq().getMAC();
                break;
            case "INBK":
                messageType = getFiscReq().getMessageType();
                mac = getFiscReq().getMAC();
                break;
        }
        //(3) 	檢核 MAC ‘2505’,’2571’,’2572’,’2545’,’2546’
        String type = "";
        if (getTxData().getMsgCtl().getMsgctlReqmacType() != null){
            type = String.valueOf(getTxData().getMsgCtl().getMsgctlReqmacType());
            if (StringUtils.equalsAny(pCode, "3201", "3205", "3206")) {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkOpcMac(pCode, messageType, mac);
            } else if (StringUtils.equalsAny(pCode, "2120", "2150", "2270")) {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkFISCMACPend(messageType.substring(2, 4), mac);
            } else {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkFiscMac(messageType, mac);

            }
        }

    }

    private void sendToCBS() throws Exception {
        String mMac;
        String mac = null;
        CBSAdapter adapter = new CBSAdapter(this.getTxData());
        adapter.setCbsType(CBSType.FISC);
        adapter.setCbsId(StringUtils.leftPad(this.getTxData().getStan(), 7, '0'));
        if (getTxData().getMsgCtl().getMsgctlReqmacType() != null) {
            if (rtnCode == FEPReturnCode.Normal) {
                //ebcdic的HEX 值 重押成功訊息(SucessCode：4001)在 BIT 64的MAC，再轉給IMS (HEX :F4F0F0F1)
                mMac = "4001";
            } else {
                //ebcdic的HEX 值,重押錯誤訊息(SucessCode：0302)在 BIT 64的MAC，再轉給IMS (HEX :F0F3F0F2)
                mMac = "0302";
            }
            mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
        }
        switch (fiscTeleType) {
        case "CLR":
        	getFiscCLRReq().setMAC(mac);
            break;
        case "EMVIC":
        	getFiscEMVICReq().setMAC(mac);
            break;
        case "OPC":
        	getFiscOPCReq().setMAC(mac);
            break;
        case "INBK":
        	getFiscReq().setMAC(mac);
            break;
        }

        if (StringUtils.equals(fiscTeleType, "INBK") && StringUtils.isNotBlank(getFiscReq().getPINBLOCK())){
            /* 檢核 SYNC_PPKEY 錯誤, 放在 MAC 值送給IMS 主機 */
            if ( StringUtils.equals(pCode, "2510") && !StringUtils.equals(SysStatus.getPropertyValue().getSysstatF3dessync(),this.getFiscReq().getSyncPpkey())) {
                mMac = "0302";
                mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
                getFiscReq().setMAC(mac);
            }
            /*將財金PINBLOCK轉換成IMS PINBLOCK(newPIN) 置換在 Bitmap 5 客戶密碼(PINBLOCK) 再轉給IMS*/
            RefString W_PINBLOCK = new RefString(null);
            new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).ConvertFISCPinToHost(getFiscReq().getPINBLOCK(), W_PINBLOCK, pCode);
            getFiscReq().setPINBLOCK(W_PINBLOCK.get());
        }

        /* 2024/6/21 新增 WAY 判斷 */
        if (StringUtils.equals(pCode.substring(0,1),"2"))   {
            /* 2025/4/28 修改 for  財金公司入帳資料傳送交易(PCODE 2252) */
            if (StringUtils.equals(pCode,"2252")){
                if ( "01".equals(getFiscReq().getRsCode()) ) {
                    /* 明細資料送主機不等回應 */
                    getFeptxn().setFeptxnWay((short) 1);
                } else {
                    /* INBKReq.RS_CODE = ‘02’*/
                    /* 總筆數資料送主機等回應 */
                    getFeptxn().setFeptxnWay((short) 2);

                    int W_MS = 20000;
                    Sysconf sysconf = sysconfMapper.selectByPrimaryKey( (short) 1, "2252SleepInterval");
                    if (sysconf == null) {
                        getLogContext().setProgramName(ProgramName + ".sendToCBS");
                        getLogContext().setRemark("找不到 SYSCONF 2252 傳送總筆數等候間隔");
                        sendEMS(getLogContext());
                    }else{
                        W_MS = Integer.parseInt(sysconf.getSysconfValue());
                    }
                    Thread.sleep(W_MS);
                    this.logContext.setRemark("SLEEP "+ W_MS/1000 + "s" ); //最後一筆2252 等待W_MS所有2252都送主機後再送主機
                    logMessage(this.logContext);
                }
            }else {
                if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                    getFeptxn().setFeptxnWay((short) 2);
                } else {
                    getFeptxn().setFeptxnWay((short) 3);
                }
            }
        }else {
            /* 非第一碼為2 PCODE */
            if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFeptxn().setFeptxnWay((short) 2);
            } else {
                getFeptxn().setFeptxnWay((short) 1);
            }
        }
        if (feptxn.getFeptxnWay()==1) {
            adapter.setTimeout(0);//不須接收回應
        }

        rtnCode = getFiscBusiness().makeBitmap(fiscHeader.getMessageType(), fiscHeader.getProcessingCode(),MessageFlow.Request);
        rtnCode = fiscHeader.makeFISCMsg();
        adapter.setMessageToCBS(fiscHeader.getFISCMessage());
        rtnCode = adapter.sendReceive();
        this.logContext.setRemark("MessageToCBS");
        this.logContext.setMessage("MessageToCBS: " + adapter.getMessageToCBS() + ",CbsId:" + adapter.getCbsId());
        logMessage(this.logContext);
        this.logContext.setRemark("MessageFromCBS");
        this.logContext.setMessage("MessageFromCBS:" + adapter.getMessageFromCBS());
        logMessage(this.logContext);
        getTxData().setTxResponseMessage(adapter.getMessageFromCBS());
        switch (fiscTeleType) {
            case "CLR":
                fiscCLRRes.setFISCMessage(adapter.getMessageFromCBS());
                fiscCLRRes.parseFISCMsg();
                break;
            case "EMVIC":
                fiscEMVICRes.setFISCMessage(adapter.getMessageFromCBS());
                fiscEMVICRes.parseFISCMsg();
                break;
            case "OPC":
                fiscOPCRes.setFISCMessage(adapter.getMessageFromCBS());
                fiscOPCRes.parseFISCMsg();
                break;
            case "INBK":
                fiscINBKRes.setFISCMessage(adapter.getMessageFromCBS());
                fiscINBKRes.parseFISCMsg();
                break;
        }
        fiscHeader.setFISCMessage(adapter.getMessageFromCBS());
        fiscHeader.parseFISCMsg();
    }

    private FEPReturnCode prepareFEPTXN() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        switch (fiscTeleType) {
            case "CLR":
                switch (fiscHeader.getProcessingCode()) {
                    case "5201":
                    case "5202":
                        rtnCode = getFiscBusiness().prepareFeptxnForUICommon(fiscHeader.getProcessingCode());
                        break;
                    case "5312":
                    case "5314":
                    case "5102":
                        rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
                        break;
                    default:
                        rtnCode = FEPReturnCode.FEPTXNNotFound;
                        break;
                }
                break;
            case "EMVIC":
                rtnCode = getFiscBusiness().prepareFEPTXN_EMV();
                break;
            case "OPC":
                rtnCode = getFiscBusiness().prepareFeptxnOpc(fiscHeader.getProcessingCode());
                break;
            case "INBK":
                rtnCode = getFiscBusiness().prepareFEPTXN();
                break;
            default:
                rtnCode = FEPReturnCode.FEPTXNNotFound;
                break;
        }
        return rtnCode;
    }

    private FEPReturnCode prepareINBKPEND() {
        // PrepareINBKPEND: 跨行延遲交易檔初始資料(INBKPEND)
        defINBKPEND = new Inbkpend();
        try {
            String TX_DATETIME = getFiscReq().getTxnInitiateDateAndTime();
            if (IntegerUtils.parseInt(TX_DATETIME, 0) < 90) {
                defINBKPEND.setInbkpendTxDate(String.valueOf(20110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
            } else {
                defINBKPEND.setInbkpendTxDate(String.valueOf(19110000+Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
            }
            defINBKPEND.setInbkpendTxTime(StringUtils.substring(TX_DATETIME, 6, 12)); // 交易時間
            defINBKPEND.setInbkpendEjfno(getTxData().getEj()); // 電子日誌序號
            defINBKPEND.setInbkpendStan(getFiscReq().getSystemTraceAuditNo()); // 財金交易序號
            defINBKPEND.setInbkpendBkno(getFiscReq().getTxnSourceInstituteId().substring(0, 3)); // 交易啟動銀行
            defINBKPEND.setInbkpendDesBkno(getFiscReq().getTxnDestinationInstituteId().substring(0, 3)); // 交易接收銀行
            defINBKPEND.setInbkpendAtmno(getFiscReq().getATMNO()); // 櫃員機代號
            defINBKPEND.setInbkpendSubsys(getTxData().getMsgCtl().getMsgctlSubsys()); // 系統別
            defINBKPEND.setInbkpendMsgflow(FEPTxnMessageFlow.FISC_Request); // ‘F1’ (FISC REQUEST)
            defINBKPEND.setInbkpendPcode(getFiscReq().getProcessingCode());
            defINBKPEND.setInbkpendReqRc(getFiscReq().getResponseCode());
            if (StringUtils.isNotBlank(getFiscReq().getTxAmt())) {
                defINBKPEND.setInbkpendTxAmt(new BigDecimal(getFiscReq().getTxAmt())); // 交易金額
            }
            if (StringUtils.isNotBlank(getFiscReq().getOriStan())) { // 原始交易序號
                defINBKPEND.setInbkpendOriBkno(getFiscReq().getOriStan().substring(0, 3));
                defINBKPEND.setInbkpendOriStan(getFiscReq().getOriStan().substring(3, 10));
            }
            if (StringUtils.isNotBlank(getFiscReq().getTroutBkno())) {
                defINBKPEND.setInbkpendTroutbkno(getFiscReq().getTroutBkno().substring(0, 3)); // 原交易存款單位代號
            }
            defINBKPEND.setInbkpendTroutActno(getFiscReq().getTrinActno());
            /* PS:原交易帳號/卡號存於 INBK.TRIN_ACTNO */
            if (StringUtils.isNotBlank(getFiscReq().getDueDate())) {
                /* 若.DUE_DATE 為0 則不需轉西元年 */
                if ("000000".equals(getFiscReq().getDueDate())) {
                    defINBKPEND.setInbkpendOriTxDate(StringUtils.repeat( '0',8));
                } else {
                    defINBKPEND.setInbkpendOriTxDate(
                            CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0'))); // 轉西元年)
                }
            }
            // 如果欄位有值才取值
            if (StringUtils.isNotBlank(getFiscReq().getRsCode()))
                defINBKPEND.setInbkpendPrcResult(getFiscReq().getRsCode()); // 處理結果
            if (StringUtils.isNotBlank(getFiscReq().getCOUNT()))
                defINBKPEND.setInbkpendCount(Integer.parseInt(getFiscReq().getCOUNT())); // 件數
            if (StringUtils.isNotBlank(getFiscReq().getMODE()))
                defINBKPEND.setInbkpendEcInstruction(getFiscReq().getMODE()); // 沖正指示
            /* 2021/1/13 應永豐要求, 增加寫入 FEPTXN */
            String inbkpendTxDate = defINBKPEND.getInbkpendTxDate(); // 交易日期(西元年)
            String inbkpendTxTime = defINBKPEND.getInbkpendTxTime(); // 交易時間
            getFiscBusiness().getFeptxn().setFeptxnTxDate(inbkpendTxDate);
            getFiscBusiness().getFeptxn().setFeptxnTxTime(inbkpendTxTime);
            getFiscBusiness().getFeptxn().setFeptxnReqDatetime(inbkpendTxDate + inbkpendTxTime);
            getFiscBusiness().getFeptxn().setFeptxnBkno(defINBKPEND.getInbkpendBkno());
            getFiscBusiness().getFeptxn().setFeptxnStan(defINBKPEND.getInbkpendStan());
            getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
            /* 2025/4/30 修改by Bruis for  2120/2150/2270 還原財金營業日*/
            getFiscBusiness().getFeptxn().setFeptxnTbsdyAct(getFiscBusiness().getFeptxn().getFeptxnTbsdyFisc());
            getFiscBusiness().getFeptxn().setFeptxnAtmno(defINBKPEND.getInbkpendAtmno());
            getFiscBusiness().getFeptxn().setFeptxnTxAmt(defINBKPEND.getInbkpendTxAmt());
            getFiscBusiness().getFeptxn().setFeptxnEjfno(defINBKPEND.getInbkpendEjfno());
            getFiscBusiness().getFeptxn().setFeptxnPcode(defINBKPEND.getInbkpendPcode());
            getFiscBusiness().getFeptxn().setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
            getFiscBusiness().getFeptxn().setFeptxnRemark(defINBKPEND.getInbkpendEcInstruction());
            getFiscBusiness().getFeptxn().setFeptxnRsCode(defINBKPEND.getInbkpendPrcResult());
            // 如果InbkpendOriTxDate有值才取值
            if (StringUtils.isNotBlank(defINBKPEND.getInbkpendOriTxDate()))
                getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate());
            getFiscBusiness().getFeptxn().setFeptxnOriStan(defINBKPEND.getInbkpendOriStan());
            getFiscBusiness().getFeptxn().setFeptxnDesBkno(defINBKPEND.getInbkpendOriBkno());
            getFiscBusiness().getFeptxn().setFeptxnSubsys((short) 1);
            getFiscBusiness().getFeptxn().setFeptxnFiscFlag((short) 1);
            // (條件ZONE_CODE = “TWN”)
            zone = zoneExtMapper.selectByPrimaryKey("TWN");
            getFiscBusiness().getFeptxn().setFeptxnTbsdy(zone.getZoneTbsdy());
            getFiscBusiness().getFeptxn().setFeptxnChannel(getFiscBusiness().getFISCTxData().getTxChannel().name()); // 通道別
            getFiscBusiness().getFeptxn().setFeptxnMsgid(getTxData().getMsgCtl().getMsgctlMsgid());
            getFiscBusiness().getFeptxn().setFeptxnTxrust("0");
            return FEPReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareINBKPEND"));
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateFEPTXN() {
        getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false)); // FISC 逾時 FLAG  ->  0:正常 1:逾時
        getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue()); // FEP內部錯誤代碼  FEP Internal check error 之代碼
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            if (StringUtils.equalsAny(pCode, "2120", "2150", "2270", "2574", "3201", "3205", "3206")){
                if (StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc(), "0001")){
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
                }
            } else if ( StringUtils.equals(pCode, "2252") ) {
                /* 2025/4/28 修改 for  財金公司入帳資料傳送交易(PCODE 2252) */
                if (getFiscBusiness().getFeptxn().getFeptxnWay() == 1){
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                }else{
                    if (StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc(), "0001")) {
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 處理結果=成功
                    } else {
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); // 處理結果=Reject
                    }
                }
            } else {
                if (getFiscBusiness().getFeptxn().getFeptxnWay() == 1){
                    /*  1 way */
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); // 成功
                }else{
                    if(StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc(),"4001")){
                        if (!DbHelper.toBoolean(getFiscBusiness().getFISCTxData().getMsgCtl().getMsgctlFisc2way())) {
                            /* 3 way */
                            getFiscBusiness().getFeptxn().setFeptxnPending(new Short("1")); //Pending
                            getFiscBusiness().getFeptxn().setFeptxnTxrust("B"); //Pending
                        } else {
                            /* 2 way */
                            getFiscBusiness().getFeptxn().setFeptxnTxrust("A");  // 成功
                        }
                    } else if (StringUtils.equals(getFiscBusiness().getFeptxn().getFeptxnTxrust(), "0")) { //初值:0
                        getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //拒絕-正常
                    }
                }
            }
            if (getFiscBusiness().getFeptxn().getFeptxnWay() > 1) {
                getFiscBusiness().getFeptxn().setFeptxnMsgflow("F2"); //FISC Response
            }
            getFiscBusiness().getFeptxn().setFeptxnAaComplete(new Short("1")); // FISC Response
            rtnCode = getFiscBusiness().updateTxData();

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    private void makeFiscMac() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        String message = this.getTxData().getTxResponseMessage();
        String mac = "";
        RefString refMac = new RefString(mac);
        if (StringUtils.equalsAny(pCode, "3201", "3205", "3206")) {
            mac = fiscOPCRes.getMAC();
            refMac = new RefString(mac);
            rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeOpcMac(pCode, fiscOPCRes.getMessageType(), refMac);
        } else {
            if (getTxData().getMsgCtl().getMsgctlReqmacType() != null) {
                if (StringUtils.equalsAny(pCode, "2120", "2150", "2270")) {
                    mac = fiscINBKRes.getMAC();
                    refMac = new RefString(mac);
                    rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFISCMACPend(fiscINBKRes.getMessageType().substring(2, 4), refMac);
                } else {
                    mac = fiscINBKRes.getMAC();
                    refMac = new RefString(mac);
                    rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscINBKRes.getMessageType(), refMac);
                }
            }
        }

        this.logContext.setRemark(fiscTeleType + " new fisc mac :" + refMac.get());
        logMessage(Level.DEBUG, this.logContext);

        if (rtnCode != FEPReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        } else {
            //4/30 2252 不換 MAC
            if( !StringUtils.equals( pCode , "2252")) {
                message = message.substring(0, (message.length() - 8)) + refMac.get();
            }
            this.getTxData().setTxRequestMessage(message);
        }
    }

    private String CLRResponse() {
        String rtnMessage = "";
        if(getFiscCLRRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscCLRRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBSToIMS(getFiscCLRRes(), getFiscCLRRes().getAPData());

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);
        return rtnMessage;
    }

    private String EMVICResponse() {
        String rtnMessage = "";
        if(getFiscEMVICRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscEMVICRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBSToIMS(getFiscEMVICRes(), getFiscEMVICRes().getAPData());

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);
        return rtnMessage;
    }

    private String OPCResponse() {
        String rtnMessage = "";
        if(getFiscOPCRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBSToIMS(getFiscOPCRes(), getFiscOPCRes().getAPData());

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);
        return rtnMessage;
    }

    private String INBKResponse() {
        String rtnMessage = "";
        if(getFiscRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBSToIMS(getFiscRes(), getFiscRes().getAPData());
        
        if (StringUtils.isNotBlank(fiscINBKRes.getBALA())) {
            getFiscBusiness().getFeptxn().setFeptxnBala(new BigDecimal(fiscINBKRes.getBALA()));
        }
        if (StringUtils.isNotBlank(fiscINBKRes.getBALB())) {
            getFiscBusiness().getFeptxn().setFeptxnBalb(new BigDecimal(fiscINBKRes.getBALB()));
        }

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);

        return rtnMessage;
    }
}
