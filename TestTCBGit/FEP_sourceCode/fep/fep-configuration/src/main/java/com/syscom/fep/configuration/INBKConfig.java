package com.syscom.fep.configuration;

import java.util.List;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.base.enums.Protocol;

public class INBKConfig {
    public static final int SubSystemNo = 1;
    private static INBKConfig _instance = new INBKConfig();
    private double _ATMFTFee;
    private int _ATMFTNoticeAmt;
    private int _ATMWDFee;
    private String _BknoChkDight;
    private String _CBSPEND;
    private String _ComboPINKey;
    private String _DEAD_FISC;
    private String _EBillT24ActNo;
    private String _EBillTrinActNo;
    private int _EMVAccessFee;
    private String _EMVACFeeFlag;
    private int _EMVMLimit;
    private double _FISCFee;
    private String _ForeignWithdrawDCnt;
    private String _FuelATMPAYNO3;
    private String _FuelNBPAYNO3;
    private String _FWDTxnRegister;
    private String _GIFCardBRNO;
    private String _HK209StopDate;
    private double _HK2410Fee;
    private double _HK2510Fee;
    private String _IBDEffectDate;
    private int _ICPCLimit;
    private String _ICPUEffectDate;
    private int _ICTxLimit;

    private int _INBKFTLimit;
    private int _INBKIDLimit;

    public int getADMLimit() {
        return _ADMLimit;
    }

    private int _ADMLimit;
    private int _INBKPCLimit;
    private int _INBKSALimit;
    private int _INBKTxLimit;
    private String _INSPayType = "";  //zk add 2021-06-25
    private String _JCBPPKType;
    private String _ListenIP;
    private String _MO209StopDate;
    private double _MO2410Fee;
    private double _MO2510Fee;
    private String _MOBinStopDate;
    private String _OBRMailTo;
    private String _OBRT24ErrCode;
    private int _OBTxLimit;
    private int _INBKTAXLimit;

    public int getINBKSDPLimit() {
        return _INBKSDPLimit;
    }

    public int getINBKODPLimit() {
        return _INBKODPLimit;
    }

    private int _INBKSDPLimit;
    private int _INBKODPLimit;

    private int _ODRDNLimit;
    private int _ODRDSLimit;
    private String _ODRTroutActNo;
    private int _QRPExpireTime;
    private String _RCV_FISC;
    private String _ReceiveIP;
    private int _ReceivePort;
    private boolean _RejectNCRINA;
    private String _REQ_FISC;
    private String _RETFRMailTo;
    private int _RETFRMaxThreads;
    private String _SECIntActno;
    private String _SECVirActno;
    private String _SenderIP;
    private int _SenderPort;
    private String _SingleRETFRSendT24MailTo;
    private String _TELVirActno;
    private String _TELVirBrno;
    private double _TFRBenefitTxAmt;
    private double _TFRFeeReductTxAmt;
    private double _TFRReductFee;
    private String _TFRSEffectDate;
    private String _TO_FISC;
    private String _WebSendCBSPEND;
    private String _TO_FISC_HOST;
    private int _TO_FISC_PORT;
    private Protocol _TO_FISC_PROTOCOL;
    private String _CBSPINKey;
    private String _ATMCertNo;
    private String _ATMCertNo_Old;
    private String _NPSUNITMFTPath;
    private String _MERCHANTMFTPath;
    private String _QRMERCHANTMFTPath;
    private String _UPBINMFTPath;
    private String _BSDYMFTPath;
    private String _NPSBatchInOnlineOutMFTPath;
    private String _FiscRecoveryMFTPath;
    private String _NPSUNITLocalPath;
    private String _MERCHANTLocalPath;
    private String _QRMERCHANTLocalPath;
    private String _UPBINLocalPath;
    private String _BSDYLocalPath;
    private String _NPSBatchInOnlineOutLocalPath;
    private String _ATMGrayListSecretKey;
    private String _GrayListCheckUrl;
    private String _ExCheckErrMail;


    public String getCBSPINKey() {
        return _CBSPINKey;
    }


    private INBKConfig() {
        fillDataToProperty();
    }

    /**
     * ATM代理轉帳手續費
     *
     * @return
     */
    public final double getATMFTFee() {
        return _ATMFTFee;
    }

    /**
     * ATM單筆轉帳通知設定金額
     *
     * @return
     */
    public final int getATMFTNoticeAmt() {
        return _ATMFTNoticeAmt;
    }

    /**
     * ATM代理提款手續費
     *
     * @return
     */
    public final int getATMWDFee() {
        return _ATMWDFee;
    }

    /**
     * 繳稅交易之銀行代號-含分行別檢查碼
     *
     * @return
     */
    public final String getBknoChkDight() {
        return _BknoChkDight;
    }

    /**
     * CBSPEND QUEUE
     *
     * @return
     */
    public final String getCBSPEND() {
        return _CBSPEND;
    }

    /**
     * COMBO國際卡 PIN KEY代號
     *
     * @return
     */
    public final String getComboPINKey() {
        return _ComboPINKey;
    }

    /**
     * FISCGW Timeout後移至此queue
     *
     * @return
     */
    public final String getDeadFisc() {
        return _DEAD_FISC;
    }

    /**
     * EBILL轉入T24帳號
     *
     * @return
     */
    public final String getEBillT24ActNo() {
        return _EBillT24ActNo;
    }

    /**
     * EBILL 轉入信用卡會科
     *
     * @return
     */
    public final String getEBillTrinActNo() {
        return _EBillTrinActNo;
    }

    /**
     * EMV交易處理費(台幣)
     *
     * @return
     */
    public final int getEMVAccessFee() {
        return _EMVAccessFee;
    }

    /**
     * 是否收取EMV交易處理費(Y:收, N:不收)
     *
     * @return
     */
    public final String getEMVACFeeFlag() {
        return _EMVACFeeFlag;
    }

    /**
     * EMV每卡跨國交易月限額
     *
     * @return
     */
    public final int getEMVMLimit() {
        return _EMVMLimit;
    }

    /**
     * 財金收取手續費
     *
     * @return
     */
    public final double getFISCFee() {
        return _FISCFee;
    }

    /**
     * 國外提款每日限制次數
     *
     * @return
     */
    public final String getForeignWithdrawDCnt() {
        return _ForeignWithdrawDCnt;
    }

    /**
     * 實體ATM汽燃費之費用代號前三碼
     *
     * @return
     */
    public final String getFuelATMPAYNO3() {
        return _FuelATMPAYNO3;
    }

    /**
     * 網路通路汽燃費之費用代號前三碼
     *
     * @return
     */
    public final String getFuelNBPAYNO3() {
        return _FuelNBPAYNO3;
    }

    /**
     * 預約交易檔目錄
     *
     * @return
     */
    public final String getFWDTxnRegister() {
        return _FWDTxnRegister;
    }

    /**
     * GIFT卡開戶行
     *
     * @return
     */
    public final String getGIFCardBRNO() {
        return _GIFCardBRNO;
    }

    /**
     * 香港當地非本行ATM國際提款拒絶交易生效日
     *
     * @return
     */
    public final String getHK209StopDate() {
        return _HK209StopDate;
    }

    /**
     * 香港分行-國際提款(2410)-應付財金跨行手續費 (港幣)
     *
     * @return
     */
    public final double getHK2410Fee() {
        return _HK2410Fee;
    }

    /**
     * 香港分行-跨行提款(2510)-應付財金跨行手續費 (港幣)
     *
     * @return
     */
    public final double getHK2510Fee() {
        return _HK2510Fee;
    }

    /**
     * 跨行存款優化生效日
     *
     * @return
     */
    public final String getIBDEffectDate() {
        return _IBDEffectDate;
    }

    /**
     * 晶片卡跨國消費扣款單筆限額
     *
     * @return
     */
    public final int getICPCLimit() {
        return _ICPCLimit;
    }

    /**
     * 消費扣款功能自動啟用生效日
     *
     * @return
     */
    public final String getICPUEffectDate() {
        return _ICPUEffectDate;
    }

    /**
     * 晶片卡跨國提款單筆限額
     *
     * @return
     */
    public final int getICTxLimit() {
        return _ICTxLimit;
    }

    /**
     * 跨行轉帳限額
     *
     * @return
     */
    public final int getINBKFTLimit() {
        return _INBKFTLimit;
    }

    /**
     * 跨行全國繳費ID+Account單筆限額
     *
     * @return
     */
    public final int getINBKIDLimit() {
        return _INBKIDLimit;
    }

    /**
     * 跨行消費扣款單筆限額
     *
     * @return
     */
    public final int getINBKPCLimit() {
        return _INBKPCLimit;
    }

    /**
     * 小額支付單筆限額
     *
     * @return
     */
    public final int getINBKSALimit() {
        return _INBKSALimit;
    }

    /**
     * 跨行提款單筆限額
     *
     * @return
     */
    public final int getINBKTxLimit() {
        return _INBKTxLimit;
    }

    /**
     * 保險費新繳費類別代號
     *
     * @return
     */
    public final String getINSPayType() {
        return _INSPayType;
    }

    /**
     * JCB卡之PPK TYPE, 只可輸入 S1 或 T2
     *
     * @return
     */
    public final String getJCBPPKType() {
        return _JCBPPKType;
    }

    /**
     * FISC Service服務的IP
     *
     * @return
     */
    public final String getListenIP() {
        return _ListenIP;
    }

    /**
     * 澳門當地非本行ATM國際提款拒絶交易生效日
     *
     * @return
     */
    public final String getMO209StopDate() {
        return _MO209StopDate;
    }

    /**
     * 澳門分行-國際提款(2410)-應付財金跨行手續費 (葡幣)
     *
     * @return
     */
    public final double getMO2410Fee() {
        return _MO2410Fee;
    }

    /**
     * 澳門分行-跨行提款(2510)-應付財金跨行手續費 (葡幣)
     *
     * @return
     */
    public final double getMO2510Fee() {
        return _MO2510Fee;
    }

    /**
     * 澳門卡PLUS BIN回收生效日
     *
     * @return
     */
    public final String getMOBinStopDate() {
        return _MOBinStopDate;
    }

    /**
     * 銷戶後退款通知Mail信箱
     *
     * @return
     */
    public final String getOBRMailTo() {
        return _OBRMailTo;
    }

    /**
     * 銷戶後退款T24回應代碼
     *
     * @return
     */
    public final String getOBRT24ErrCode() {
        return _OBRT24ErrCode;
    }


    public int getINBKTAXLimit() {
        return _INBKTAXLimit;
    }


    public void setINBKTAXLimit(int _INBKTAXLimit) {
        this._INBKTAXLimit = _INBKTAXLimit;
    }


    /**
     * 跨境電子支付單筆限額
     *
     * @return
     */
    public final int getOBTxLimit() {
        return _OBTxLimit;
    }

    /**
     * 跨行存款存入帳戶非卡片帳號每日限額(台幣)
     *
     * @return
     */
    public final int getODRDNLimit() {
        return _ODRDNLimit;
    }

    /**
     * 跨行存款存入帳戶為卡片帳號每日限額(台幣)
     *
     * @return
     */
    public final int getODRDSLimit() {
        return _ODRDSLimit;
    }

    /**
     * 跨行存款代表轉出帳號
     *
     * @return
     */
    public final String getODRTroutActNo() {
        return _ODRTroutActNo;
    }

    /**
     * 豐錢包消費扣款被掃交易逾時秒數
     *
     * @return
     */
    public final int getQRPExpireTime() {
        return _QRPExpireTime;
    }

    /**
     * AA Service接收財金送回Response的Queue
     *
     * @return
     */
    public final String getRcvFisc() {
        return _RCV_FISC;
    }

    /**
     * 財金的接收端IP
     *
     * @return
     */
    public final String getReceiveIP() {
        return _ReceiveIP;
    }

    /**
     * 財金的接收端Port
     *
     * @return
     */
    public final int getReceivePort() {
        return _ReceivePort;
    }

    /**
     * 是否拒絕NCR ATM INA電文(0:False 1:True)
     *
     * @return
     */
    public final boolean getRejectNCRINA() {
        return _RejectNCRINA;
    }

    /**
     * FISC Service接收財金發動交易的Queue
     *
     * @return
     */
    public final String getReqFisc() {
        return _REQ_FISC;
    }

    /**
     * 跨行預約轉帳交易失敗收件者Mail信箱
     *
     * @return
     */
    public final String getRETFRMailTo() {
        return _RETFRMailTo;
    }

    /**
     * 預約跨轉可處理交易的最大Thread數
     *
     * @return
     */
    public final int getRETFRMaxThreads() {
        return _RETFRMaxThreads;
    }

    /**
     * 永豐證券轉入帳戶
     *
     * @return
     */
    public final String getSECIntActno() {
        return _SECIntActno;
    }

    /**
     * 永豐證券虛擬帳號前五碼
     *
     * @return
     */
    public final String getSECVirActno() {
        return _SECVirActno;
    }

    /**
     * 財金的傳送端IP
     *
     * @return
     */
    public final String getSenderIP() {
        return _SenderIP;
    }

    /**
     * 財金的傳送端Port
     *
     * @return
     */
    public final int getSenderPort() {
        return _SenderPort;
    }

    /**
     * 單筆跨行預約轉帳交易通知T24電文失敗收件者Mail信箱
     *
     * @return
     */
    public final String getSingleRETFRSendT24MailTo() {
        return _SingleRETFRSendT24MailTo;
    }

    /**
     * 手機門號跨行轉帳前8碼
     *
     * @return
     */
    public final String getTELVirActno() {
        return _TELVirActno;
    }

    /**
     * 手機門號跨行轉帳轉入行之分行代號
     *
     * @return
     */
    public final String getTELVirBrno() {
        return _TELVirBrno;
    }

    /**
     * 跨行轉帳手續費每日減免(0元)之交易金額
     *
     * @return
     */
    public final double getTFRBenefitTxAmt() {
        return _TFRBenefitTxAmt;
    }

    /**
     * 跨行轉帳手續費優惠(10元)之交易金額
     *
     * @return
     */
    public final double getTFRFeeReductTxAmt() {
        return _TFRFeeReductTxAmt;
    }

    /**
     * 跨行轉帳優惠手續費(10元)
     *
     * @return
     */
    public final double getTFRReductFee() {
        return _TFRReductFee;
    }

    /**
     * 跨行轉帳小額交易手續費調降生效日
     *
     * @return
     */
    public final String getTFRSEffectDate() {
        return _TFRSEffectDate;
    }

    /**
     * AA Service送交易給財金用的Queue
     *
     * @return
     */
    public final String getToFisc() {
        return _TO_FISC;
    }

    /**
     * 由FEPWEB補CBSPEND QUEUE(FOR ATM)
     *
     * @return
     */
    public final String getWebSendCBSPEND() {
        return _WebSendCBSPEND;
    }

    public String getToFiscHost() {
        return _TO_FISC_HOST;
    }

    public int getToFiscPort() {
        return _TO_FISC_PORT;
    }

    public Protocol getToFiscProtocol() {
        return _TO_FISC_PROTOCOL;
    }

    public String getATMCertNo() {
        return _ATMCertNo;
    }

    public String getATMCertNoOld() {
        return _ATMCertNo_Old;
    }

    public static INBKConfig getInstance() {
        return _instance;
    }

    public String getImportNPSUNITMFTPath() {
        return _NPSUNITMFTPath;
    }

    public String getImportMERCHANTMFTPath() {
        return _MERCHANTMFTPath;
    }
    public String getImportQRMERCHANTMFTPath() {
        return _QRMERCHANTMFTPath;
    }

    public String getImportUPBINMFTPath() {
        return _UPBINMFTPath;
    }

    public String getImportBSDYMFTPath() {
        return _BSDYMFTPath;
    }

    public String getNPSBatchInOnlineOutMFTPath() {
        return _NPSBatchInOnlineOutMFTPath;
    }
    public String getNPSUNITLocalPath() {
        return _NPSUNITLocalPath;
    }

    public String getMERCHANTLocalPath() {
        return _MERCHANTLocalPath;
    }
    public String getQRMERCHANTLocalPath() {
        return _QRMERCHANTLocalPath;
    }

    public String getUPBINLocalPath() {
        return _UPBINLocalPath;
    }

    public String getBSDYLocalPath() {
        return _BSDYLocalPath;
    }

    public String getNPSBatchInOnlineOutLocalPath() {
        return _NPSBatchInOnlineOutLocalPath;
    }

    public String getFiscRecoveryMFTPath() {
        return _FiscRecoveryMFTPath;
    }

    public String getATMGrayListSecretKey() {
        return _ATMGrayListSecretKey;
    }

    public String getGrayListCheckUrl() {
        return _GrayListCheckUrl;
    }
    public String getExCheckErrMail() {
        return _ExCheckErrMail;
    }

    private void fillDataToProperty() {
        List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
        String sysconfValue = null;
        for (Sysconf sysconf : sysconfList) {
            sysconfValue = sysconf.getSysconfValue();
            switch (sysconf.getSysconfName()) {
                case "ATMFTFee":
                    _ATMFTFee = DbHelper.toDouble(sysconfValue);
                    break;
                case "ATMFTNoticeAmt":
                    _ATMFTNoticeAmt = DbHelper.toInteger(sysconfValue);
                    break;
                case "ATMWDFee":
                    _ATMWDFee = DbHelper.toInteger(sysconfValue);
                    break;
                case "BknoChkDight":
                    _BknoChkDight = DbHelper.toString(sysconfValue);
                    break;
                case "CBSPEND":
                    _CBSPEND = DbHelper.toString(sysconfValue);
                    break;
                case "ComboPINKey":
                    _ComboPINKey = DbHelper.toString(sysconfValue);
                    break;
                case "DEAD_FISC":
                    _DEAD_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "EBillT24ActNo":
                    _EBillT24ActNo = DbHelper.toString(sysconfValue);
                    break;
                case "EBillTrinActNo":
                    _EBillTrinActNo = DbHelper.toString(sysconfValue);
                    break;
                case "EMVAccessFee":
                    _EMVAccessFee = DbHelper.toInteger(sysconfValue);
                    break;
                case "EMVACFeeFlag":
                    _EMVACFeeFlag = DbHelper.toString(sysconfValue);
                    break;
                case "EMVMLimit":
                    _EMVMLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "FISCFee":
                    _FISCFee = DbHelper.toDouble(sysconfValue);
                    break;
                case "ForeignWithdrawDCnt":
                    _ForeignWithdrawDCnt = DbHelper.toString(sysconfValue);
                    break;
                case "FuelATMPAYNO3":
                    _FuelATMPAYNO3 = DbHelper.toString(sysconfValue);
                    break;
                case "FuelNBPAYNO3":
                    _FuelNBPAYNO3 = DbHelper.toString(sysconfValue);
                    break;
                case "FWDTxnRegister":
                    _FWDTxnRegister = DbHelper.toString(sysconfValue);
                    break;
                case "GIFCardBRNO":
                    _GIFCardBRNO = DbHelper.toString(sysconfValue);
                    break;
                case "HK209StopDate":
                    _HK209StopDate = DbHelper.toString(sysconfValue);
                    break;
                case "HK2410Fee":
                    _HK2410Fee = DbHelper.toDouble(sysconfValue);
                    break;
                case "HK2510Fee":
                    _HK2510Fee = DbHelper.toDouble(sysconfValue);
                    break;
                case "IBDEffectDate":
                    _IBDEffectDate = DbHelper.toString(sysconfValue);
                    break;
                case "ICPCLimit":
                    _ICPCLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "ICPUEffectDate":
                    _ICPUEffectDate = DbHelper.toString(sysconfValue);
                    break;
                case "ICTxLimit":
                    _ICTxLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKFTLimit":
                    _INBKFTLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKIDLimit":
                    _INBKIDLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKPCLimit":
                    _INBKPCLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKSALimit":
                    _INBKSALimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKTxLimit":
                    _INBKTxLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "INBKSDPLimit":
                	_INBKSDPLimit = DbHelper.toInteger(sysconfValue);
                	break;    
                case "INBKODPLimit":
                	_INBKODPLimit = DbHelper.toInteger(sysconfValue);
                	break;
                case "INSPayType":
                    _INSPayType = DbHelper.toString(sysconfValue);
                    break;
                case "JCBPPKType":
                    _JCBPPKType = DbHelper.toString(sysconfValue);
                    break;
                case "ListenIP":
                    _ListenIP = DbHelper.toString(sysconfValue);
                    break;
                case "MO209StopDate":
                    _MO209StopDate = DbHelper.toString(sysconfValue);
                    break;
                case "MO2410Fee":
                    _MO2410Fee = DbHelper.toDouble(sysconfValue);
                    break;
                case "MO2510Fee":
                    _MO2510Fee = DbHelper.toDouble(sysconfValue);
                    break;
                case "MOBinStopDate":
                    _MOBinStopDate = DbHelper.toString(sysconfValue);
                    break;
                case "OBRMailTo":
                    _OBRMailTo = DbHelper.toString(sysconfValue);
                    break;
                case "OBRT24ErrCode":
                    _OBRT24ErrCode = DbHelper.toString(sysconfValue);
                    break;
                case "OBTxLimit":
                    _OBTxLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "ODRDNLimit":
                    _ODRDNLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "ODRDSLimit":
                    _ODRDSLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "ODRTroutActNo":
                    _ODRTroutActNo = DbHelper.toString(sysconfValue);
                    break;
                case "QRPExpireTime":
                    _QRPExpireTime = DbHelper.toInteger(sysconfValue);
                    break;
                case "RCV_FISC":
                    _RCV_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "ReceiveIP":
                    _ReceiveIP = DbHelper.toString(sysconfValue);
                    break;
                case "ReceivePort":
                    _ReceivePort = DbHelper.toInteger(sysconfValue);
                    break;
                case "RejectNCRINA":
                    _RejectNCRINA = DbHelper.toBoolean(sysconfValue);
                    break;
                case "REQ_FISC":
                    _REQ_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "RETFRMailTo":
                    _RETFRMailTo = DbHelper.toString(sysconfValue);
                    break;
                case "RETFRMaxThreads":
                    _RETFRMaxThreads = DbHelper.toInteger(sysconfValue);
                    break;
                case "SECIntActno":
                    _SECIntActno = DbHelper.toString(sysconfValue);
                    break;
                case "SECVirActno":
                    _SECVirActno = DbHelper.toString(sysconfValue);
                    break;
                case "SenderIP":
                    _SenderIP = DbHelper.toString(sysconfValue);
                    break;
                case "SenderPort":
                    _SenderPort = DbHelper.toInteger(sysconfValue);
                    break;
                case "SingleRETFRSendT24MailTo":
                    _SingleRETFRSendT24MailTo = DbHelper.toString(sysconfValue);
                    break;
                case "TELVirActno":
                    _TELVirActno = DbHelper.toString(sysconfValue);
                    break;
                case "TELVirBrno":
                    _TELVirBrno = DbHelper.toString(sysconfValue);
                    break;
                case "TFRBenefitTxAmt":
                    _TFRBenefitTxAmt = DbHelper.toDouble(sysconfValue);
                    break;
                case "TFRFeeReductTxAmt":
                    _TFRFeeReductTxAmt = DbHelper.toDouble(sysconfValue);
                    break;
                case "TFRReductFee":
                    _TFRReductFee = DbHelper.toDouble(sysconfValue);
                    break;
                case "TFRSEffectDate":
                    _TFRSEffectDate = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC":
                    _TO_FISC = DbHelper.toString(sysconfValue);
                    break;
                case "WebSendCBSPEND":
                    _WebSendCBSPEND = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC_HOST":
                    _TO_FISC_HOST = DbHelper.toString(sysconfValue);
                    break;
                case "TO_FISC_PORT":
                    _TO_FISC_PORT = DbHelper.toInteger(sysconfValue);
                    break;
                case "TO_FISC_PROTOCOL":
                    _TO_FISC_PROTOCOL = Enum.valueOf(Protocol.class, DbHelper.toString(sysconfValue).toLowerCase());
                    break;
                case "CBSPINKey":
                    _CBSPINKey = DbHelper.toString(sysconfValue);
                    break;
                case "INBKTAXLimit":
                    _INBKTAXLimit = DbHelper.toInteger(sysconfValue);
                    break;
                case "ATMCertNo":
                    _ATMCertNo = DbHelper.toString(sysconfValue);
                    break;
                case "ATMCertNo_Old":
                    _ATMCertNo_Old = DbHelper.toString(sysconfValue);
                    break;
                case "NPSUNITMFTPath":
                    _NPSUNITMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "MERCHANTMFTPath":
                    _MERCHANTMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "QRMERCHANTMFTPath":
                    _QRMERCHANTMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "UPBINMFTPath":
                    _UPBINMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "BSDYMFTPath":
                    _BSDYMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "NPSBatchInOnlineOutMFTPath":
                    _NPSBatchInOnlineOutMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "NPSUNITLocalPath":
                    _NPSUNITLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "MERCHANTLocalPath":
                    _MERCHANTLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "QRMERCHANTLocalPath":
                    _QRMERCHANTLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "UPBINLocalPath":
                    _UPBINLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "BSDYLocalPath":
                    _BSDYLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "NPSBatchInOnlineOutLocalPath":
                    _NPSBatchInOnlineOutLocalPath = DbHelper.toString(sysconfValue);
                    break;
                case "FiscRecoveryMFTPath":
                    _FiscRecoveryMFTPath = DbHelper.toString(sysconfValue);
                    break;
                case "ATMGrayListSecretKey":
                    _ATMGrayListSecretKey = DbHelper.toString(sysconfValue);
                    break;
                case "GrayListCheckUrl":
                    _GrayListCheckUrl = DbHelper.toString(sysconfValue);
                    break;
                case "ExCheckErrMail":
                    _ExCheckErrMail = DbHelper.toString(sysconfValue);
                    break;
            }
        }
    }
}
