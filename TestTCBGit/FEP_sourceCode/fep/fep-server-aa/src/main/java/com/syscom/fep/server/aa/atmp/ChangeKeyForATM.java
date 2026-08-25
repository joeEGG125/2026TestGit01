package com.syscom.fep.server.aa.atmp;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.vo.text.webatm.RCV_EATM_GeneralTrans_RQ;
import com.syscom.fep.vo.text.webatm.SEND_EATM_FAA_CC1APC;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * P1階段負責接收ATMGW來之ATM交易, 檢核及重壓MAC後送至主機,取得主機回應後, 再用ATM KEY重壓mac後回給ATM
 *
 * @author Richard
 */
public class ChangeKeyForATM extends ATMPAABase {
    // 1.宣告private變數
    private FEPReturnCode _rtnCode = FEPReturnCode.ProgramException;

    public ChangeKeyForATM(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 處理電文
     *
     * @return
     */
    @Override
    public String processRequestData() throws Exception {
        RefString response = new RefString(null);
        try {
            // 2.記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(
                    StringUtils.join(this.getTxData().getAaName(), ".ChangeKeyForATM.processRequestData"));
            this.logContext.setMessage(this.getTxData().getTxRequestMessage());
            this.logContext.setRemark(StringUtils.join("Enter ", this.getTxData().getAaName()));
            logMessage(this.logContext);
            // 3.檢核交易MAC
            // _rtnCode = FEPReturnCode.Normal;
            // _rtnCode = this.checkMac();
            // 4. 呼叫ChangeKey
            RefString keyData = new RefString(null);
            String header = EbcdicConverter.fromHex(CCSID.English,
                getTxData().getTxRequestMessage().substring(0, 70));
            String keyType = header.substring(34, 35);
            // if (_rtnCode == FEPReturnCode.Normal) {
            _rtnCode = changeKey(keyType, keyData);
            // }
            // 5. 組Response電文
            if (_rtnCode == FEPReturnCode.Normal) {
                String resStr;
                if (getTxData().getTxChannel() == FEPChannel.ATM)
                {
                    resStr = getTxData().getTxRequestMessage().substring(0, 22)
                        + EbcdicConverter.toHex(CCSID.English, "FPN".length(), "FPN")
//                        + getTxData().getTxRequestMessage().substring(28, 52)
                        + EbcdicConverter.toHex(CCSID.English,12,
                                FormatUtil.dateTimeFormat(Calendar.getInstance(),
                                        FormatUtil.FORMAT_DATE_YYMMDDHHMMSS_PLAIN))
                        // 原本的BYTE:26-30，ebcdic所以要抓成52-60
                        + getTxData().getTxRequestMessage().substring(52, 60)
                        + EbcdicConverter.toHex(CCSID.English, ("02CK" + keyType).length(), "02CK" + keyType);
                } else {
                    //後20240311與eatm決定全部為ascii
                	if (getTxData().getTxChannel().toString().equals("POS")) {
                		resStr = header.substring(0, 11)
                                + "FPN"                        
                                + header.substring(14, 30)
                                + "02CK" + keyType;
                		resStr = EbcdicConverter.toHex(CCSID.English, resStr.length(), resStr);
                	}else {
                        resStr = StringUtil.toHex(header.substring(0, 11)
                                + "FPN"                        
                                + header.substring(14, 30)
                                + "02CK" + keyType);
                	}
                }
                
                
                if (keyType.equals("C")) { // Working Key
                    // 抓Ebcdic的值，所以要乘以2，0-16
//                    resStr += EbcdicConverter.toHex(CCSID.English, keyData.get().length(), keyData.get());
                    //送回給ATM的KEY跟MAC, 都不轉EBCDIC, ascii轉成hex 送給主機
                	if (getTxData().getTxChannel().toString().equals("POS")) {
                		//resStr += StringUtil.toHex(keyData.get());
                		resStr += EbcdicConverter.toHex(CCSID.English, keyData.get().length(), keyData.get());
                	} else {
                    	 resStr += StringUtil.toHex(keyData.get());
                	}
                    response.set(resStr);
                } else {
                    // Master KEY 回應32 byte key + 16 byte kcv + 104 空白
//                    resStr += EbcdicConverter.toHex(CCSID.English, keyData.get().length() + 104,
//                            keyData.get() + StringUtils.rightPad("", 104));
                    //送回給ATM的KEY跟MAC, 都不轉EBCDIC, ascii轉成hex 送給主機
                    // if (getTxData().getTxChannel().toString().equals("EAT")) {
                    //     resStr += EbcdicConverter.toHex(CCSID.English, keyData.get().length(), keyData.get());
                    // } else {
                    resStr += StringUtil.toHex(keyData.get() + StringUtils.rightPad("", 104));
                    //}
                    response.set(resStr);
                }
            }

            // 6.用ATM KEY重壓Response電文MAC
            if (keyType.equals("C")) {
                RefString atmmac = new RefString(null);
                if (_rtnCode == FEPReturnCode.Normal) {
                    if (getTxData().getTxChannel() == FEPChannel.ATM) {
                        _rtnCode = this.makeATMMac(response.get(), atmmac);
                    }
                    else {
                        _rtnCode = this.makeEATMMac(atmmac);
                    }
                    
                }
                // 7.回傳Response及ReturnCode
                if (_rtnCode == FEPReturnCode.Normal) {
                    // 壓mac回傳舉例o1:92180F09
//                    response.set(StringUtils.join(response,
//                            EbcdicConverter.toHex(CCSID.English, atmmac.get().length(), atmmac.get())));
                    //送回給ATM的KEY跟MAC, 都不轉EBCDIC, ascii轉成hex 送給主機
                	if (getTxData().getTxChannel().toString().equals("POS")) {
                		response.set(StringUtils.join(response, EbcdicConverter.toHex(CCSID.English, atmmac.get().length(), atmmac.get())));
                	} else {
                        //20240305:統一改mac不轉ebcdic
                		response.set(StringUtils.join(response, StringUtil.toHex(atmmac.get())));
                	}
                }
            }

            // 判斷是否為EAT 是需組XML電文
            if (getTxData().getTxChannel().toString().equals("EAT")) {
                String str = eatmResponse(response.get());
                response.set(str);
            }

            this.logContext.setProgramFlowType(ProgramFlow.AAIn);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setProgramName(
                    StringUtils.join(this.getTxData().getAaName(), ".ChangeKeyForATM.processRequestData"));
            this.logContext.setMessage("ChangeKeyForATM rtnCode:" + _rtnCode);
            this.logContext.setRemark("ChangeKeyForATM rtnCode:" + _rtnCode);
            logMessage(this.logContext);
        } catch (Exception e) {
            logContext.setProgramException(e);
            logMessage(this.logContext);
            logMessage(Level.DEBUG, this.logContext);
            _rtnCode = handleException(e, "processRequestData");
        } finally {
            // 記錄FEPLOG內容
            this.logContext.setProgramFlowType(ProgramFlow.AAOut);
            this.logContext.setMessageFlowType(MessageFlow.Response);
            this.logContext.setProgramName(StringUtils.join(this.getTxData().getAaName(), ".processRequestData"));
            this.logContext.setMessage(response.get());
            this.logContext.setRemark(StringUtils.join("Exit ", this.getTxData().getAaName()));
            logMessage(this.logContext);
        }
        return response.get();
    }

    /**
     * 檢核ATM電文的MAC資料
     *
     * @return
     */
    // private FEPReturnCode checkMac() {
    //     FEPReturnCode rtnCode = FEPReturnCode.Normal;
    //     try {
    //         // 1.建立DESHelper物件
    //         ENCHelper encHelper = new ENCHelper(getTxData());
    //         // 2.取出ATM代號, 壓mac內容及mac值
    //         String inputData = getTxData().getTxRequestMessage().substring(22, 70);
    //         String mac = getTxData().getTxRequestMessage().substring(70, 86);

    //         String atmNo = getTxData().getAtmNo();
    //         // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
    //         // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
    //         // 8, ' ');
    //         mac = EbcdicConverter.fromHex(CCSID.English, mac);
    //         rtnCode = encHelper.checkAtmMac(atmNo, inputData, mac);
    //     } catch (Exception e) {
    //         rtnCode = handleException(e, "checkMac");
    //     }
    //     return rtnCode;
    // }

    /**
     * 產生ATM回應電文的MAC資料
     *
     * @param response
     * @param atmmac
     * @return
     */
    private FEPReturnCode makeATMMac(String response, RefString atmmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立DESHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            // 2
            // 從MSGCAT~KEYTYPE
            String inputData = response.substring(22, 22 + 48);
            // 3.呼叫
            RefString mac = new RefString(null);
            // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
            // 8, ' ');
            String atmNo = getTxData().getAtmNo();
            // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
            rtnCode = encHelper.makeAtmMac(atmNo, inputData, mac);
            // 4.若rtnCode=normal
            if (rtnCode == FEPReturnCode.Normal) {
                atmmac.set(mac.get());
            }
        } catch (Exception e) {
            _rtnCode = handleException(e, "makeATMMac");
        }
        return rtnCode;
    }

    private FEPReturnCode makeEATMMac(RefString atmmac) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立DESHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            // 2
            // 從MSGCAT~KEYTYPE
            String inputData = "0060000000000000";
            // 3.呼叫
            RefString mac = new RefString(null);
            // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
            // 8, ' ');
            String atmNo = getTxData().getAtmNo();
            // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
            rtnCode = encHelper.makeAtmMac(atmNo, inputData, mac);
            // 4.若rtnCode=normal
            if (rtnCode == FEPReturnCode.Normal) {
                atmmac.set(mac.get());
            }
        } catch (Exception e) {
            _rtnCode = handleException(e, "makeEATMMac");
        }
        return rtnCode;
    }

    private FEPReturnCode changeKey(String keyType, RefString keyData) {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        try {
            // 1.建立DESHelper物件
            ENCHelper encHelper = new ENCHelper(getTxData());
            String atmNo = getTxData().getAtmNo();

            // String atmSno = getATMBusiness().getAtmStr().getAtmSno();
            // String atmSno = StringUtils.leftPad(getATMBusiness().getAtmStr().getAtmSno(),
            // 8, ' ');
            List<String> newKey = new ArrayList<>();
            List<String> newCKey = new ArrayList<>();
            if (keyType.equals(("M"))) { // M: MasterKey

                rtnCode = encHelper.ChangeInitialKeyForATM(atmNo, keyType, newKey);
                if (rtnCode == FEPReturnCode.Normal) {
                    if (newKey.size() == 2) {
                        for (String str : newKey) {
                            keyData.set(StringUtils.join(keyData.get(), str));
                        }
                    }
                }
            } else {
                //換C key
                if (getTxData().getTxChannel().toString().equals("ATM")) {
                    rtnCode = encHelper.ChangeKeyForATM(atmNo, newKey);
                    if (rtnCode == FEPReturnCode.Normal) {
                        if (newKey.size() >= 4) {
                            // for (String str : newKey) {
                            // keyData.set(StringUtils.join(keyData.get(), str));
                            // }
                            // 只抓Single PPKEY, kcv及3DES PPKEY,kcv
                            for (int i = 0; i < 4; i++) {
                                keyData.set(StringUtils.join(keyData.get(), newKey.get(i)));
                            }
                        }
                    }
                    else {
                        return rtnCode;
                    }
                    // mac key改由initial change key保護
                    rtnCode = encHelper.ChangeInitialKeyForATM(atmNo, "C", newCKey);
                    if (rtnCode == FEPReturnCode.Normal) {
                        if (newCKey.size() == 2) {
                            for (String str : newCKey) {
                                keyData.set(StringUtils.join(keyData.get(), str));
                            }
                        }
                    }
                } else {
                    rtnCode = encHelper.ChangeKeyForEATM(atmNo, newKey);
                    if (rtnCode == FEPReturnCode.Normal) {
                        if (newKey.size() == 2) {
                            for (String str : newKey) {
                                keyData.set(StringUtils.join(keyData.get(), str));
                            }
                        }
                    }

                   // keyData.set(StringUtils.join(keyData.get(), StringUtils.rightPad("", 96, " ")));
                }
                
            }

        } catch (Exception e) {
            logContext.setProgramException(e);
            logMessage(this.logContext);
            logMessage(Level.DEBUG, this.logContext);
            rtnCode = handleException(e, "changeKey");
        }
        return rtnCode;
    }

    /**
     * 組EATM 回應電文
     *
     * @param outdata
     * @return
     * @throws Exception
     */
    private String eatmResponse(String outdata) throws Exception {
        String rtnMessage = "";
        try {
            RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getHeader();
            RCV_EATM_GeneralTrans_RQ.RCV_EATM_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = this.getTxData().getTxObject().getEatmrequest().getBody().getRq().getSvcRq();
            SEND_EATM_FAA_CC1APC rs = new SEND_EATM_FAA_CC1APC();
            SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body rsbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body();
            SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs msgrs = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_NS1MsgRs();
            SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header header = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_Header();
            SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs msgbody = new SEND_EATM_FAA_CC1APC.SEND_EATM_FAA_CC1APC_Body_MsgRs_SvcRs();
            msgrs.setSvcRq(msgbody);
            msgrs.setHeader(header);
            rsbody.setRs(msgrs);
            rs.setBody(rsbody);

            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            header.setCHANNEL(atmReqheader.getCHANNEL());
            header.setMSGID(atmReqheader.getMSGID());
            header.setCLIENTDT(atmReqheader.getCLIENTDT());

            if (_rtnCode == FEPReturnCode.Normal) {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE("4001");
                header.setSEVERITY("INFO");
            } else {
                header.setSYSTEMID("FEP");
                header.setSTATUSCODE("EF1031");
                header.setSEVERITY("ERROR");
            }
            msgbody.setOUTDATA(outdata);
            rtnMessage = XmlUtil.toXML(rs);

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return "";
        }
        return rtnMessage;
    }
}
