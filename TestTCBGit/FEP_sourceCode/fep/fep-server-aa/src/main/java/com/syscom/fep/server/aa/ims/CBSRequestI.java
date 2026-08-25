package com.syscom.fep.server.aa.ims;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.mchange.lang.IntegerUtils;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.ext.model.BinExt;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.BinMapper;
import com.syscom.fep.mybatis.model.Curcd;
import com.syscom.fep.mybatis.model.Inbkpend;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.text.fisc.FISCHeader;

public class CBSRequestI extends INBKAABase {
    private FEPReturnCode rtnCode = FEPReturnCode.Normal;
    private String rtnMessage = StringUtils.EMPTY;
    private String fiscTeleType = StringUtils.EMPTY;
    private FISCHeader fiscHeader;
    private Inbkpend defINBKPEND;
    private Short ReqmacType = null;
    private Short RepmacType = null;
    private List<String> uiPcodeList = new ArrayList<>();
    private Boolean pend;
    private Zone zone = new Zone();
    private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
    private BinMapper binMapper = SpringBeanFactoryUtil.getBean(BinMapper.class);

    private void setUiPcodeListData() {
        uiPcodeList.add("2130");
        uiPcodeList.add("2140");
        uiPcodeList.add("2280");
        uiPcodeList.add("2290");
    }

    public CBSRequestI(FISCData txnData) throws Exception {
        super(txnData);
        fiscTeleType = getTxData().getFiscTeleType().toString();
        switch (fiscTeleType) {
            case "CLR":
                fiscHeader = getFiscCLRReq();
                break;
            case "EMVIC":
                fiscHeader = getFiscEMVICReq();
                break;
            case "OPC":
                fiscHeader = getFiscOPCReq();
                break;
            case "INBK":
                fiscHeader = getFiscReq();
                break;
        }
        ReqmacType = this.getTxData().getMsgCtl().getMsgctlReqmacType();
        RepmacType = this.getTxData().getMsgCtl().getMsgctlRepmacType();
        setUiPcodeListData();
        if (uiPcodeList.contains(fiscHeader.getProcessingCode())) {
            pend = true;
        } else {
            pend = false;
        }
    }

    @Override
    public String processRequestData() {
        try {
            //記錄LOG
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage(this.getTxData().getTxRequestMessage());
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);

            // 1. 拆解財經電文
            if (!StringUtils.equals("CLR", fiscTeleType)) {
                rtnCode = getFiscBusiness().checkBitmapFromCBS(fiscHeader, fiscHeader.getAPData());
            }

            // 2.AddTxData:新增交易記錄(FEPTXN)
            if (rtnCode == FEPReturnCode.Normal) {
                //(1) 	Prepare() 交易記錄初始資料
                if (rtnCode == FEPReturnCode.Normal) {
                    rtnCode = this.prepareFEPTXN();
                }

                if (rtnCode == FEPReturnCode.Normal) {
                    //(2) 	新增交易記錄
                    getFiscBusiness().getFeptxn().setFeptxnMsgflow("CR");
                    getFiscBusiness().getFeptxn().setFeptxnCbsProc("Y");
                    String wDate = CalendarUtil.rocStringToADString(StringUtils.leftPad(fiscHeader.getTxnInitiateDateAndTime().substring(0, 6), 7, '0'));
                    getFiscBusiness().getFeptxn().setFeptxnTxDate(wDate);
                    getFiscBusiness().getFeptxn().setFeptxnTxTime(fiscHeader.getTxnInitiateDateAndTime().substring(6, 12));
                    rtnCode = getFiscBusiness().insertFEPTxn();

                    if (ReqmacType != null && rtnCode == CommonReturnCode.Normal) {
                        //(3) 	產生 MAC
                        rtnCode = this.makeFiscMac();
                    } else if (StringUtils.equals(fiscTeleType, "OPC") && rtnCode == CommonReturnCode.Normal) {
                        char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(this.getFiscOPCReq().getBitMapConfiguration(), 16, 2, 64).toCharArray();
                        if (bitMapFromFisc[63] == '1') {
                            rtnCode = this.makeFiscMac();
                        }
                    }
                }
            }

            // 3. 	更新交易記錄(FEPTXN)
            if (rtnCode == FEPReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
                getFiscBusiness().getFeptxn().setFeptxnFiscTimeout((short) 1);
                getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
                getFiscBusiness().getFeptxn().setFeptxnPending((short) 1);
                getFiscBusiness().getFeptxn().setFeptxnTxrust("S");
                rtnCode = getFiscBusiness().updateTxData();
            }

            // 4. SendToFISC送電文到財金
            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = getFiscBusiness().sendToFISCFromCBS();

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
                }

            }

            //6. 更新 FEPTXN
            rtnCode = updateFEPTXN();

        } catch (Exception e) {
            this.rtnCode = CommonReturnCode.ProgramException;
            this.logContext.setProgramException(e);
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

    private FEPReturnCode prepareFEPTXN() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        switch (fiscTeleType) {
            case "CLR":
                rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
                break;
            case "EMVIC":
                rtnCode = this.prepareFEPTXN_EMV();
                break;
            case "OPC":
                rtnCode = getFiscBusiness().prepareFeptxnOpc(fiscHeader.getProcessingCode());
                break;
            case "INBK":
                if (pend) {
                    rtnCode = this.prepareINBKPEND();
                } else {
                    rtnCode = getFiscBusiness().prepareFEPTXN();
                }
                break;
            default:
                rtnCode = FEPReturnCode.FEPTXNNotFound;
                break;
        }
        return rtnCode;
    }

    //7. 更新 FEPTXN
    private FEPReturnCode updateFEPTXN() {
        getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
        getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            if (this.rtnCode == FEPReturnCode.Normal) {
                if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                        || NormalRC.FISC_REQ_RC.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                        || NormalRC.FISC_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                        || NormalRC.FISC_CLR_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())
                ) {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A");  /* 處理結果=成功 */
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 0);
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); /* 拒絕-正常 */
                    getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);

                }
            }
            getFiscBusiness().getFeptxn().setFeptxnAaComplete((short) 1); /* AA Close */
            rtnCode = getFiscBusiness().updateTxData();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //3. MAC
    private FEPReturnCode makeFiscMac() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        String message = this.getTxData().getTxRequestMessage();
        String pinBlock = "";
        String atmno = "006IMS";
        RefString refMac = new RefString("");
        RefString refPinBlock = new RefString("");
        switch (fiscTeleType) {
            case "CLR":
                return rtnCode;
            case "EMVIC":
                pinBlock = getFiscEMVICReq().getPINBLOCK();
                if (StringUtils.isBlank(pinBlock)) {
                    rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
                    getFiscEMVICReq().setMAC(refMac.get());
                }
                break;
            case "OPC":
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeOpcMac(fiscHeader.getProcessingCode(), fiscHeader.getMessageType(), refMac);
                getFiscOPCReq().setMAC(refMac.get());
                break;
            case "INBK":
                pinBlock = getFiscReq().getPINBLOCK();
                if (StringUtils.isBlank(pinBlock)) {
                    if (pend) {
                        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFISCMACPend(fiscHeader.getMessageType().substring(2, 4), refMac);
                        getFiscReq().setMAC(refMac.get());
                    } else {
                        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
                        getFiscReq().setMAC(refMac.get());
                    }
                }
                break;
        }
        if (StringUtils.isBlank(pinBlock)) {
            this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + " new fisc mac :" + refMac.get());
            logMessage(Level.DEBUG, this.logContext);
        }

        if (rtnCode != CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
        } else {
            if (StringUtils.isNotBlank(pinBlock)) {
                getFiscBusiness().getFeptxn().setFeptxnPinblock(pinBlock);
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).ConvertATMPinToFISCForCBS(pinBlock, atmno, refPinBlock);
                if (rtnCode != CommonReturnCode.Normal) {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    return rtnCode;
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnPinblock(refPinBlock.get());
                    switch (fiscTeleType) {
                        case "EMVIC":
                            getFiscEMVICReq().setPINBLOCK(refPinBlock.get());
                            break;
                        case "INBK":
                            getFiscReq().setPINBLOCK(refPinBlock.get());
                            break;
                    }
                }
            }

            rtnCode = getFiscBusiness().makeBitmap(fiscHeader.getMessageType(), fiscHeader.getProcessingCode(), MessageFlow.Request);
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + " after makeBitmap RC:" + rtnCode.getValue());
                logMessage(Level.DEBUG, this.logContext);
                return rtnCode;
            }

            rtnCode = fiscHeader.makeFISCMsg();
            if (rtnCode != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + " after makeFISCMsg RC:" + rtnCode.getValue());
                logMessage(Level.DEBUG, this.logContext);
                return rtnCode;
            }

            if (StringUtils.isNotBlank(pinBlock)) {
                switch (fiscTeleType) {
                    case "EMVIC":
                        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
                        getFiscEMVICReq().setMAC(refMac.get());
                        break;
                    case "INBK":
                        rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).makeFiscMac(fiscHeader.getMessageType(), refMac);
                        getFiscReq().setMAC(refMac.get());
                        break;
                }

                this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + ", after pinblock, new fisc mac :" + refMac.get());
                logMessage(Level.DEBUG, this.logContext);
                if (rtnCode != CommonReturnCode.Normal) {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    return rtnCode;
                }

                rtnCode = getFiscBusiness().makeBitmap(fiscHeader.getMessageType(), fiscHeader.getProcessingCode(), MessageFlow.Request);
                if (rtnCode != CommonReturnCode.Normal) {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + ", after pinblock, after makeBitmap RC:" + rtnCode.getValue());
                    logMessage(Level.DEBUG, this.logContext);
                    return rtnCode;
                }

                rtnCode = fiscHeader.makeFISCMsg();
                if (rtnCode != CommonReturnCode.Normal) {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                    this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + ", after pinblock, after makeFISCMsg RC:" + rtnCode.getValue());
                    logMessage(Level.DEBUG, this.logContext);
                    return rtnCode;
                }
            }

            message = fiscHeader.getFISCMessage();

            this.logContext.setRemark("fisc Tele Type:" + fiscTeleType + ", new message :" + message);
            logMessage(Level.DEBUG, this.logContext);

            this.getTxData().setTxRequestMessage(message);
        }
        return rtnCode;
    }

    private String INBKResponse() {
        String rtnMessage = "";
        if (getFiscRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscRes(), getFiscRes().getAPData());
        this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);

        //將值存入 feptxn 壓驗 mac用
        if (rtnCode == FEPReturnCode.Normal) {
            rtnCode = getFiscBusiness().setResINBKToFeptxnForIMS();
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
            this.logContext.setRemark("after set Response data To Feptxn RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }

        //檢查SysstatSync
        if (rtnCode == FEPReturnCode.Normal) {
            rtnCode = getFiscBusiness().checkSysstatSyncForIMS();
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
            this.logContext.setRemark("check SysstatSync RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }

        // 5. 將財金回覆電文傳回主機
        if (rtnCode == FEPReturnCode.Normal) {
            this.logContext.setRemark("after checkFiscMac FiscRes Mac:" + getFiscRes().getMAC());
            logMessage(this.logContext);

            if (pend) {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).checkFISCMACPend(getFiscRes().getMessageType().substring(2, 4), getFiscRes().getMAC());
            } else {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData()).checkFiscMac(getFiscRes().getMessageType(), getFiscRes().getMAC());
            }

            this.logContext.setRemark("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }

        char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(this.getFiscRes().getBitMapConfiguration(), 16, 2, 64).toCharArray();
        if (bitMapFromFisc[63] == '1') {
            String mMac;
            if (rtnCode == FEPReturnCode.Normal) {
                mMac = "4001";
            } else if (rtnCode == FEPReturnCode.KeySyncError) {
                mMac = "0301";
            } else {
                mMac = "0302";
            }

            String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
            String apData = this.getFiscRes().getAPData();
            apData = apData.substring(0, (apData.length() - 8)) + mac;
            this.getFiscRes().setAPData(apData);
            getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscRes().getResponseCode());
            this.getFiscRes().makeFISCMsg();
        }
        rtnMessage = this.getFiscRes().getFISCMessage();
        return rtnMessage;
    }

    private String OPCResponse() {
        String rtnMessage = "";
        if (getFiscOPCRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscOPCRes(), getFiscOPCRes().getAPData());

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);

        //檢查SysstatSync
        if (rtnCode == FEPReturnCode.Normal) {
            if (StringUtils.equals(fiscHeader.getProcessingCode(), "3114") || StringUtils.equals(fiscHeader.getProcessingCode(), "3115")) {
                this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
                this.logContext.setRemark("PCODE:" + fiscHeader.getProcessingCode() + " not check SysstatSync.");
                logMessage(this.logContext);
            } else {
                rtnCode = getFiscBusiness().checkSysstatSyncForIMS();
                this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
                this.logContext.setRemark("check SysstatSync RC:" + rtnCode.toString());
                logMessage(this.logContext);
            }
        }

        char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(this.getFiscOPCRes().getBitMapConfiguration(), 16, 2, 64).toCharArray();
        if (bitMapFromFisc[63] == '1') {
            // 5. 將財金回覆電文傳回主機
            if (rtnCode == FEPReturnCode.Normal) {
                rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                        .checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());

                this.logContext.setRemark("after checkOpcMac RC:" + rtnCode.toString());
                logMessage(this.logContext);
            }

            String mMac;
            if (rtnCode == FEPReturnCode.Normal) {
                mMac = "4001";
            } else if (rtnCode == FEPReturnCode.KeySyncError) {
                mMac = "0301";
            } else {
                mMac = "0302";
            }

            String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
            String apData = this.getFiscOPCRes().getAPData();
            apData = apData.substring(0, (apData.length() - 8)) + mac;
            this.getFiscOPCRes().setAPData(apData);
            getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscOPCRes().getResponseCode());
            this.getFiscOPCRes().makeFISCMsg();
        }

        rtnMessage = this.getFiscOPCRes().getFISCMessage();
        return rtnMessage;
    }

    private String CLRResponse() {
        String rtnMessage = "";
        if (getFiscCLRRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscCLRRes().getResponseCode());
        }
        //會議上說不處理
        rtnMessage = this.getFiscCLRRes().getFISCMessage();
        return rtnMessage;
    }

    private String EMVICResponse() {
        String rtnMessage = "";
        if (getFiscEMVICRes() != null) {
            getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscEMVICRes().getResponseCode());
        }
        //拆回傳電文
        rtnCode = getFiscBusiness().checkBitmapFromCBS(getFiscEMVICRes(), getFiscEMVICRes().getAPData());

        this.logContext.setRemark("after checkBitmapFromCBS RC:" + rtnCode.toString());
        logMessage(this.logContext);

        //檢查SysstatSync
        if (rtnCode == FEPReturnCode.Normal) {
            rtnCode = getFiscBusiness().checkSysstatSyncForIMS();
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".INBKResponse"));
            this.logContext.setRemark("check SysstatSync RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }

        // 5. 將財金回覆電文傳回主機
        if (ReqmacType != null && rtnCode == FEPReturnCode.Normal) {
            rtnCode = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData())
                    .checkFiscMac(getFiscEMVICRes().getMessageType(), getFiscEMVICRes().getMAC());

            this.logContext.setRemark("after checkFiscMac RC:" + rtnCode.toString());
            logMessage(this.logContext);
        }
        char[] bitMapFromFisc = StringUtil.convertFromAnyBaseString(this.getFiscEMVICRes().getBitMapConfiguration(), 16, 2, 64).toCharArray();
        if (RepmacType != null && bitMapFromFisc[63] == '1') {
            String mMac;
            if (rtnCode == FEPReturnCode.Normal) {
                mMac = "4001";
            } else if (rtnCode == FEPReturnCode.KeySyncError) {
                mMac = "0301";
            } else {
                mMac = "0302";
            }

            String mac = EbcdicConverter.toHex(CCSID.English, mMac.length(), mMac);
            String apData = this.getFiscEMVICRes().getAPData();
            apData = apData.substring(0, (apData.length() - 8)) + mac;
            this.getFiscEMVICRes().setAPData(apData);
            getFiscBusiness().getFeptxn().setFeptxnConRc(this.getFiscEMVICRes().getResponseCode());
            this.getFiscEMVICRes().makeFISCMsg();
        }
        rtnMessage = this.getFiscEMVICRes().getFISCMessage();
        return rtnMessage;
    }

    private FEPReturnCode prepareINBKPEND() {
        FEPReturnCode _rtnCode = FEPReturnCode.Normal;
        defINBKPEND = new Inbkpend();
        try {
            String TX_DATETIME = getFiscReq().getTxnInitiateDateAndTime();
            if (IntegerUtils.parseInt(TX_DATETIME, 0) < 90) {
                defINBKPEND.setInbkpendTxDate(String.valueOf(20110000 + Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
            } else {
                defINBKPEND.setInbkpendTxDate(String.valueOf(19110000 + Integer.valueOf(StringUtils.substring(TX_DATETIME, 0, 6))));
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
                    defINBKPEND.setInbkpendOriTxDate(StringUtils.repeat('0', 8));
                } else {
                    defINBKPEND.setInbkpendOriTxDate(
                            CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscReq().getDueDate(), 7, '0'))); // 轉西元年)
                }
            } else {
                /* 調整因 2280 沒紀錄財金營業日*/
                defINBKPEND.setInbkpendOriTxDate(SysStatus.getPropertyValue().getSysstatTbsdyFisc()); // 財金營業日
            }
            // 如果欄位有值才取值
            if (StringUtils.isNotBlank(getFiscReq().getRsCode()))
                defINBKPEND.setInbkpendPrcResult(getFiscReq().getRsCode()); // 處理結果
            if (StringUtils.isNotBlank(getFiscReq().getCOUNT()))
                defINBKPEND.setInbkpendCount(Integer.parseInt(getFiscReq().getCOUNT())); // 件數
            if (StringUtils.isNotBlank(getFiscReq().getMODE()))
                defINBKPEND.setInbkpendEcInstruction(getFiscReq().getMODE()); // 沖正指示
            String inbkpendTxDate = defINBKPEND.getInbkpendTxDate(); // 交易日期(西元年)
            String inbkpendTxTime = defINBKPEND.getInbkpendTxTime(); // 交易時間
            getFiscBusiness().getFeptxn().setFeptxnTxDate(inbkpendTxDate);
            getFiscBusiness().getFeptxn().setFeptxnTxTime(inbkpendTxTime);
            getFiscBusiness().getFeptxn().setFeptxnReqDatetime(inbkpendTxDate + inbkpendTxTime);
            getFiscBusiness().getFeptxn().setFeptxnBkno(defINBKPEND.getInbkpendBkno());
            getFiscBusiness().getFeptxn().setFeptxnStan(defINBKPEND.getInbkpendStan());
            getFiscBusiness().getFeptxn().setFeptxnAtmno(defINBKPEND.getInbkpendAtmno());
            getFiscBusiness().getFeptxn().setFeptxnTxAmt(defINBKPEND.getInbkpendTxAmt());
            getFiscBusiness().getFeptxn().setFeptxnEjfno(defINBKPEND.getInbkpendEjfno());
            getFiscBusiness().getFeptxn().setFeptxnPcode(defINBKPEND.getInbkpendPcode());
            getFiscBusiness().getFeptxn().setFeptxnReqRc(defINBKPEND.getInbkpendReqRc());
            getFiscBusiness().getFeptxn().setFeptxnRemark(defINBKPEND.getInbkpendEcInstruction());
            getFiscBusiness().getFeptxn().setFeptxnRsCode(defINBKPEND.getInbkpendPrcResult());
            getFiscBusiness().getFeptxn().setFeptxnTbsdyFisc(defINBKPEND.getInbkpendOriTxDate()); // 為了mac特別調整
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
        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareINBKPEND"));
            sendEMS(getLogContext());
        }
        return _rtnCode;
    }

    public FEPReturnCode prepareFEPTXN_EMV() {
        final String ATMNO_VIR = "90000";
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Zone twnZone = new Zone();
        Curcd curcd = new Curcd();
        String tempString = null;

        try {
            twnZone = getFiscBusiness().getZoneByZoneCode(ZoneCode.TWN);
            if (twnZone == null) {
                return IOReturnCode.ZONENotFound;
            }

            rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
            if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
                return rtnCode;
            }

            getFiscBusiness().getFeptxn().setFeptxnAtmno(getFiscEMVICReq().getATMNO()); // 櫃員機代號
            getFiscBusiness().getFeptxn().setFeptxnAtmnoVir(ATMNO_VIR);
            getFiscBusiness().getFeptxn().setFeptxnAtmod(twnZone.getZoneCbsMode());
            getFiscBusiness().getFeptxn().setFeptxnTxnmode(twnZone.getZoneCbsMode());
            tempString = StringUtils.leftPad(getFiscBusiness().getFeptxn().getFeptxnEjfno().toString(), 5, '0'); // spec change 2010-10-21
            getFiscBusiness().getFeptxn().setFeptxnTxseq(tempString.substring(tempString.length() - 5, tempString.length()));
            getFiscBusiness().getFeptxn().setFeptxnChannel(getTxData().getTxChannel().toString()); // 通道別
            getFiscBusiness().getFeptxn().setFeptxnMerchantId(getFiscEMVICReq().getMerchantId()); // 商家代號

            // 交易幣別
            curcd = getFiscBusiness().getAlpha3ByIsono3(getFiscEMVICReq().getCURRENCY());
            if (curcd != null) {
                getFiscBusiness().getFeptxn().setFeptxnTxCur(curcd.getCurcdAlpha3());
            } else {
                getFeptxn().setFeptxnTxCur(CurrencyType.OTH.name());
            }

            // 交易金額
            if (!StringUtils.isBlank(getFiscEMVICReq().getTxAmt()) && PolyfillUtil.isNumeric(getFiscEMVICReq().getTxAmt())) {
                //含小數故除100
                getFiscBusiness().getFeptxn().setFeptxnTxAmt(new BigDecimal(getFiscEMVICReq().getTxAmt()).divide(new BigDecimal("100")));
            }

            if (FISCPCode.PCode2633.getValueStr().equals(getFeptxn().getFeptxnPcode())) {
                getFiscBusiness().getFeptxn().setFeptxnTxAmtAct(new BigDecimal(getFiscEMVICReq().getSetRpamt())); // 實際完成交易金額
                getFiscBusiness().getFeptxn().setFeptxnMajorActno(getFiscEMVICReq().getPanNo().trim()); // 卡號
            }

            getFiscBusiness().getFeptxn().setFeptxnTxDatetimeFisc(getFiscEMVICReq().getTxnInitiateDateAndTime()); // 交易日期時間

            // IC卡序號
            if (!StringUtils.isBlank(getFiscEMVICReq().getCardSeq())) {
                getFiscBusiness().getFeptxn().setFeptxnIcSeqno(getFiscEMVICReq().getCardSeq());
            }

            getFiscBusiness().getFeptxn().setFeptxnPinblock(getFiscEMVICReq().getPINBLOCK()); // PINBLOCK
            getFiscBusiness().getFeptxn().setFeptxnMsgid(getTxData().getMsgCtl().getMsgctlMsgid());
            getFiscBusiness().getFeptxn().setFeptxnTxrust("0");

            // 原交易序號
            if (!StringUtils.isBlank(getFiscEMVICReq().getOriStan())) {
                getFiscBusiness().getFeptxn().setFeptxnOriStan(getFiscEMVICReq().getOriStan().substring(3, 10));
            }

            // 國際卡
            getFiscBusiness().getFeptxn().setFeptxnTroutBkno(getFiscEMVICReq().getTxnDestinationInstituteId().substring(0, 3)); // 支付錢的銀行
            if (!StringUtils.isBlank(getFiscEMVICReq().getTRK2())) {
                getFiscBusiness().getFeptxn().setFeptxnTrk2(getFiscEMVICReq().getTRK2());
                String track2 = getFiscBusiness().getFeptxn().getFeptxnTrk2();
                BinExt binExt = getBin(track2.substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
                if (binExt == null || (binExt != null && StringUtils.isBlank(binExt.getBinProd()))) {
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTroutKind(binExt.getBinProd());
                    // 2020/11/09 Modify by Ruling for 多幣DEBIT卡
                    if (FISCPCode.PCode2630.getValueStr().equals(getFiscBusiness().getFeptxn().getFeptxnPcode()) && binExt.isBinMulticur()) {
                        ((FeptxnExt) getFiscBusiness().getFeptxn()).setFeptxnMulticur("Y"); // 多幣Debit卡跨國提款註記
                    }
                }

                int end1 = track2.indexOf("D");
                int end2 = track2.indexOf("=");
                String accno;
                if (end1 > -1) {
                    if (end1 <= 12) {
                        accno = StringUtils.leftPad(track2.substring(0, end1), 12, '0');
                    } else {
                        accno = track2.substring(end1 - 13, end1 - 1);
                    }
                } else if (end2 > -1) {
                    if (end2 <= 12) {
                        accno = StringUtils.leftPad(track2.substring(0, end2), 12, '0');
                    } else {
                        accno = track2.substring(end2 - 13, end2 - 1);
                    }
                } else {
                    accno = track2.substring(3, 15);
                }
                getFiscBusiness().getFeptxn().setFeptxnTroutActno(accno);
            }

            getFiscBusiness().getFeptxn().setFeptxnZoneCode(ZoneCode.TWN); // 預設值
            getFiscBusiness().getFeptxn().setFeptxnTxCurAct(CurrencyType.TWD.name()); // 預設值
            getFiscBusiness().getFeptxn().setFeptxnTbsdy(twnZone.getZoneTbsdy());

            if (DbHelper.toBoolean(getTxData().getMsgCtl().getMsgctlFisc2way())) {
                getFiscBusiness().getFeptxn().setFeptxnWay((short) 2);
            } else {
                getFiscBusiness().getFeptxn().setFeptxnWay((short) 3);
            }

            getLogContext().setAtmNo(getFiscBusiness().getFeptxn().getFeptxnAtmno());
            getLogContext().setTrinActno(getFiscBusiness().getFeptxn().getFeptxnTrinActno());
            getLogContext().setTrinBank(getFiscBusiness().getFeptxn().getFeptxnTrinBkno());
            getLogContext().setTroutActno(getFiscBusiness().getFeptxn().getFeptxnTroutActno());
            getLogContext().setTroutBank(getFiscBusiness().getFeptxn().getFeptxnBkno());
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareFEPTXN_EMV");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    private BinExt getBin(String no, String bkno) {
        return new BinExt(binMapper.selectByPrimaryKey(no, bkno));
    }
}
