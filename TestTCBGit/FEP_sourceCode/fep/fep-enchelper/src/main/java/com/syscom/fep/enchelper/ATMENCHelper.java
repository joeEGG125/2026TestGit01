package com.syscom.fep.enchelper;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.INBKConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.enchelper.vo.KeyId;
import com.syscom.fep.enchelper.vo.KeyIdentity;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.ATMZone;
import com.syscom.fep.vo.enums.CreditTXCD;
import com.syscom.fep.vo.enums.TxType;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

public class ATMENCHelper extends CBSENCHelper {

    protected ATMENCHelper(Feptxn feptxn, MessageBase txData) {
        super(feptxn, txData);
    }

    protected ATMENCHelper(Feptxn feptxn) {
        super(feptxn);
    }

    protected ATMENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq,
                           MessageBase txData) {
        super(msgId, channel, subsys, ej, atmno, atmseq, txData);
    }

    protected ATMENCHelper(FISCData txData) {
        super(txData);
    }

    /**
     * For TSM
     *
     * @param msgId
     * @param ej
     */
    protected ATMENCHelper(String msgId, int ej) {
        super(msgId, ej);
    }

    /**
     * 2022-07-28 Richard add
     *
     * @param txData
     */
    public ATMENCHelper(MessageBase txData) {
        super(txData);
    }

    /**
     * 檢核ATM電文訊息押碼(MAC)
     *
     * @param mac 訊息押碼(MAC)
     * @return
     */
    public FEPReturnCode checkAtmMac(String atmReq, String mac) {
        RefString refKeyId1 = new RefString(StringUtils.EMPTY);
        RefString refInputData1 = new RefString(StringUtils.EMPTY);
        RefString refInputData2 = new RefString(StringUtils.EMPTY);
        final String encFunc = "FN000302";
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            mac = this.unPack(mac);
            String atmReqData = StringUtils.leftPad(Integer.toString(atmReq.length()), 4, '0') + atmReq;
            rc = this.prepareAtmMac(TxType.TITA, atmReqData, mac, refKeyId1, refInputData1, refInputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(refKeyId1.get());
                param.setInputData1(refInputData1.get());
                param.setInputData2(refInputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{param};
                encrc = this.encLib(encFunc, "checkAtmMac", encpar, null);
                rc = this.getENCReturnCode(encrc);
            }
        } catch (Exception ex) {
            this.logData.setProgramException(ex);
            this.logData.setRemark(this.getENCLogMessage(encFunc, refKeyId1.get(), refInputData1.get(),
                    refInputData2.get(), StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckMACError;
        }
        return rc;
    }

    public FEPReturnCode makeCbsMac(String inputData, RefString mac) {
        return makeCbsMacData(inputData, false, mac);
    }

    public FEPReturnCode makeCbsMac(String inputData, Boolean isPending, RefString mac) {
        return makeCbsMacData(inputData, isPending, mac);
    }

    /**
     * 檢核交易驗證碼(TAC)
     *
     * @param tacType SGCTL_TAC_TYPE(TAC類別)
     * @return
     */
    // public FEPReturnCode checkAtmTac(int tacType) {
    //     RefString keyId1 = new RefString(StringUtils.EMPTY);
    //     RefString inputData1 = new RefString(StringUtils.EMPTY);
    //     RefString inputData2 = new RefString(StringUtils.EMPTY);
    //     final String encFunc = "FN000402";
    //     FEPReturnCode rc = FEPReturnCode.Normal;
    //     int encrc = -1;
    //     try {
    //         rc = this.prepareAtmTac(tacType, keyId1, inputData1, inputData2);
    //         if (rc == FEPReturnCode.Normal) {
    //             ENCParameter param = new ENCParameter();
    //             param.setKeyIdentity(keyId1.get());
    //             param.setInputData1(inputData1.get());
    //             param.setInputData2(this.makeInputDataString("4649534343415244", inputData2.get())); // BugReport(001B0341):encHelper.dll修改FN402
    //                                                                                                  // 需要補上FISCCARD的HEX字串於inputData2前面
    //             ENCParameter[] encpar = new ENCParameter[] { param };
    //             encrc = this.encLib(encFunc, "checkAtmTac", encpar, null);
    //             rc = this.getENCReturnCode(encrc);
    //         }
    //     } catch (Exception ex) {
    //         this.logData.setProgramException(ex);
    //         this.logData.setRemark(this.getDESLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
    //                 StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
    //         sendEMS(this.logData);
    //         rc = ENCReturnCode.ENCCheckMACError;
    //     }
    //     return rc;
    // }

    /**
     * @param atmNo
     * @param atmtxcd
     * @param txAct
     * @param atmSeq
     * @param expCd
     * @param ymd
     * @param mac
     * @return
     * @throws Exception
     */
    public FEPReturnCode checkConMac(String atmNo, ATMTXCD atmtxcd, String txAct, String atmSeq, String expCd,
                                     String ymd, String mac) throws Exception {
        String keyIdentity = StringUtils.EMPTY;
        String inputData1 = StringUtils.EMPTY;
        String inputData2 = StringUtils.EMPTY;
        String encFunc = "FN000302";
        FEPReturnCode rc = FEPReturnCode.Normal;
        this.atmNo = atmNo;
        this.atmSeq = atmSeq;
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3);
        key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), atmNo));
        keyIdentity = key.toString();
        String wTxcd = this.getTxcd(atmtxcd);
        String wExpcd = this.getExpcd(expCd);
        // 將上行電文內拒絕理由第二~四位放入
        // 2010-04-14 modified by kyo for expCd先TRIM掉補滿4位0 避開空字串
        expCd = StringUtils.leftPad(expCd.trim(), 4, '0');
        String wExpno = expCd.substring(1, 4).trim();
        // 拒絕理由為SPACES時，則放0
        if (StringUtils.isBlank(wExpno)) {
            wExpno = "000";
        }
        String icv = ymd; /* ATM上行電文內押碼日期(民國年) */
        atmSeq = atmSeq.length() > 4 ? atmSeq.substring(atmSeq.length() - 4) : StringUtils.leftPad(atmSeq, 4, '0');
        inputData1 = this.makeInputDataString(icv, atmNo, atmSeq, wTxcd, wExpcd, wExpno, txAct);
        inputData2 = this.unPack(mac);
        int encrc = -1;
        try {
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyIdentity);
                param.setInputData1(inputData1);
                param.setInputData2(inputData2);
                ENCParameter[] encpar = new ENCParameter[]{param};
                encrc = this.encLib(encFunc, "checkConMac", encpar, null);
                rc = this.getENCReturnCode(encrc);
            }
        } catch (Exception ex) {
            this.logData.setProgramException(ex);
            this.logData.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckMACError;
        }
        return rc;
    }

    /**
     * 檢核無卡提款密碼
     * <p>
     * * 目前只有原存行交易CommonRequestI在使用
     *
     * @param pinBlock PIN BLOCK
     * @param offset   OFFSET
     * @return FEPReturnCode
     */
    public FEPReturnCode checkNcPassword(String pinBlock, String keykind, String offset) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        String encFunc = "FN000206";
        FEPReturnCode rc = FEPReturnCode.Normal;

        int encrc = -1;
        try {
            rc = this.prepareNcPinData(pinBlock, offset, keykind, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyId1.get());
                param.setInputData1(inputData1.get());
                param.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{param};
                String[] rtnAry = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
                encrc = encLib(encFunc, "CheckNCPassword", encpar, rtnAry);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCCheckPasswordError; // ENC ERROR
                }
            }
        } catch (Exception ex) {
            this.logData.setProgramException(ex);
            this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckPasswordError;
        }
        return rc;
    }

    /**
     * 檢核香港臨櫃變更國際提款密碼
     * <p>
     * * 目前只有原存行交易CommonRequestI在使用
     *
     * @param pinBlock PIN BLOCK
     * @param offset   OFFSET
     * @return FEPReturnCode
     */
    public FEPReturnCode checkHkAuth(String pinBlock, String offset) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        String encFunc = "FN000208";
        FEPReturnCode rc = FEPReturnCode.Normal;

        int encrc = -1;
        try {
            rc = this.prepareHkAuthData("00", pinBlock, offset, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyId1.get());
                param.setInputData1(inputData1.get());
                param.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{param};
                String[] rtnAry = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
                encrc = this.encLib(encFunc, "CheckHKAuth", encpar, rtnAry);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCCheckPasswordError;
                }
            }
        } catch (Exception ex) {
            this.logData.setProgramException(ex);
            this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckPasswordError;
        }
        return rc;
    }

    /**
     * 產生ATM電文訊息押碼(MAC)
     *
     * @param mac 產生的MAC
     * @return FEPReturnCode
     */
    public FEPReturnCode makeAtmMac(String atmRes, RefString mac) {
        String encFunc = "FN000301";
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        int encrc = -1;

        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            String atmResData = StringUtils.leftPad(Integer.toString(atmRes.length()), 4, '0') + atmRes;
            rc = prepareAtmMac(TxType.TOTA, atmResData, StringUtils.EMPTY, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyId1.get());
                param.setInputData1(inputData1.get());
                param.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{param};
                String[] outputData = new String[]{StringUtils.EMPTY};
                encrc = this.encLib(encFunc, "makeAtmMac", encpar, outputData);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCMakeMACError;
                } else {
                    mac.set(this.pack(outputData[0]));
                }
            }
        } catch (Exception ex) {
            this.logData.setProgramException(ex);
            this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    /**
     * 產生ATM Confirm電文MAC
     *
     * @param atmNo
     * @param atmtxcd
     * @param txAct
     * @param atmSeq
     * @param rejCd
     * @param ymd
     * @param txamt
     * @return
     * @throws Exception
     */
    public FEPReturnCode makeConMac(String atmNo, ATMTXCD atmtxcd, String txAct, String atmSeq, String rejCd,
                                    String ymd, String txamt, RefString mac) throws Exception {
        String keyIdentity = StringUtils.EMPTY;
        String inputData1 = StringUtils.EMPTY;
        String inputData2 = StringUtils.EMPTY;
        String encFunc = "FN000301";
        FEPReturnCode rc = FEPReturnCode.Normal;
        this.atmNo = atmNo;
        this.atmSeq = atmSeq;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3);
        key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), atmNo));
        keyIdentity = key.toString();
        String wTxcd = this.getTxcd(atmtxcd);
        String wExpcd = this.getExpcd(rejCd);
        // 將上行電文內拒絕理由第二~四位放入
        // bugReport(001B0233):2010-04-14 modified by kyo for rejCd先TRIM掉補滿4位0 避開空字串
        rejCd = StringUtils.leftPad(rejCd.trim(), 4, '0');
        String wExpno = rejCd.substring(1, 4).trim();
        // 拒絕理由為SPACES時，則放0
        if (StringUtils.isBlank(wExpno)) {
            wExpno = "000";
        }
        String icv = ymd; // ATM上行電文內押碼日期(民國年)
        atmSeq = atmSeq.length() > 4 ? atmSeq.substring(atmSeq.length() - 4) : StringUtils.leftPad(atmSeq, 4, '0');
        // 2010-04-13 modified by kyo for txamt要左補0滿8位
        inputData1 = this.makeInputDataString(icv, atmNo, atmSeq, wTxcd, wExpcd, wExpno,
                StringUtils.leftPad(txamt, 8, '0'));
        int encrc = -1;
        try {
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyIdentity);
                param.setInputData1(inputData1);
                param.setInputData2(inputData2);
                ENCParameter[] encpar = new ENCParameter[]{param};
                String[] outputData = new String[]{StringUtils.EMPTY};
                encrc = this.encLib(encFunc, "makeConMac", encpar, outputData);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCMakeMACError;
                } else {
                    mac.set(this.pack(outputData[0]));
                }
            }
        } catch (Exception e) {
            this.logData.setProgramException(e);
            this.logData.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    /**
     * ATM電文轉換 PIN BLOCK至信用卡電文
     * <p>
     * *原存行交易也會call這個function
     *
     * @param originalPinBlock ATM電文原始PIN BLOCK char(16)
     * @param newPinBlock      轉換後的PIN BLOCK char(16)
     * @return FEPReturnCode
     * @throws Exception
     */
    public FEPReturnCode pinBlockConvert(String originalPinBlock, RefString newPinBlock) throws Exception {
        final String encFunc = "FN000204";
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        /* 2/18 修改, 傳入二組 key_type */
        // key.KeyType = ENCKeyType.T2;
        key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        // 2010-09-06 by kyo for Connie's spec modify:跨行邏輯
        if (!DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag())) {
            key.setKeyType1(ENCKeyType.T3);
            key.getKeyId1().setKeySubCode(StringUtils.rightPad(
                    StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), this.feptxn.getFeptxnAtmno()), 8,
                    StringUtils.SPACE));
            // 2016/06/21 Modify by Ruling for COMBO開卡作業優化
            // Fly 2020/10/22 修改for 掌靜脈交易
            if (ATMTXCD.G51.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.G50.name().equals(this.feptxn.getFeptxnTxCode()) ||
                    ATMTXCD.PWD.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.PFT.name().equals(this.feptxn.getFeptxnTxCode()) ||
                    ATMTXCD.PDR.name().equals(this.feptxn.getFeptxnTxCode())
                    || ATMTXCD.PCR.name().equals(this.feptxn.getFeptxnTxCode())) {
                /* IVR PIN KEY */
                key.setKeyType2(ENCKeyType.T3);
                key.getKeyId2().setKeySubCode(
                        StringUtils.rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                                ATMPConfig.getInstance().getIVRPINKey()), 8, StringUtils.SPACE));
            } else {
                /* 信用卡 PIN KEY */
                key.setKeyType2(ENCKeyType.T2);
                key.getKeyId2().setKeySubCode(
                        StringUtils.rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                                ATMPConfig.getInstance().getCreditPINKey()), 8, StringUtils.SPACE));
            }
        } else if (!SysStatus.getPropertyValue().getSysstatHbkno().equals(this.feptxn.getFeptxnBkno())) {
            /* FISC PPK 轉至 Credit Card PPK */
            key.setKeyType1(ENCKeyType.T2);
            // modified by Maxine for 12/7 修改 for 78202 為 Single Key
            key.setKeyType2(ENCKeyType.T2);
            // key.KeyType2 = ENCKeyType.T2;
            key.getKeyId1().setKeySubCode(
                    StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, StringUtils.SPACE));
            /* 11/10 修改, 抓取系統參數檔 */
            /* 11/29 修改 for Combo國際卡交易(KEY:80778202) */
            /*
             * 20220820修改 for 國際卡/無卡提款交易CBS主機(KEY:XXXXXX)
             * ENC.key_identify.key_id2.key_sub_code = SYSSTAT_HBKNO +以系統
             * 代碼( 1)及變數名稱(CBSPINKey)為KEY, 讀取系統參數檔
             * (SYSCONF), 取得變數值(SYSCONF_VALUE) EX: 006+CBS01
             */
            key.getKeyId2()
                    .setKeySubCode(StringUtils.rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                            INBKConfig.getInstance().getCBSPINKey()), 8, StringUtils.SPACE));
        } else {
            /* ATM PPK 轉至FISC PPK */
            key.setKeyType1(ENCKeyType.T3);
            if ("2480".equals(this.feptxn.getFeptxnPcode())) {
                /* JCB卡讀取 SYSCONF判斷使用1-ENC PPK或3-ENC PPK */
                key.setKeyType2(ENCKeyType.parse(INBKConfig.getInstance().getJCBPPKType()));
            } else {
                key.setKeyType2(ENCKeyType.T2);
            }
            key.getKeyId1().setKeySubCode(StringUtils.rightPad(
                    StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), this.feptxn.getFeptxnAtmno()), 8,
                    StringUtils.SPACE));
            // 2011-11-08 Modify by Ruling for BFT 修改PinBlock 方式
            // 2020-10-30 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈(PFT2521)
            ATMTXCD txcd = ATMTXCD.parse(this.feptxn.getFeptxnTxCode());
            switch (txcd) {
                case BFT:
                    key.getKeyId2()
                            .setKeySubCode(StringUtils
                                    .rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                                            ATMPConfig.getInstance().getCreditPINKey()), 8, StringUtils.SPACE));
                    break;
                case PFT:
                    key.setKeyType2(ENCKeyType.T3);
                    key.getKeyId2()
                            .setKeySubCode(StringUtils
                                    .rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                                            ATMPConfig.getInstance().getIVRPINKey()), 8, StringUtils.SPACE));
                    break;
                default:
                    key.getKeyId2().setKeySubCode(
                            StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, StringUtils.SPACE));
                    break;
            }
        }
        String keyIdentity = key.toString();
        String inputData1 = this.unPack(originalPinBlock);
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            param.setInputData2(StringUtils.EMPTY);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "pinBlockConvert", encpar, outputData);
            if (encrc != 0) {
                rc = ENCReturnCode.ENCPINBlockConvertError;
            } else {
                newPinBlock.set(this.pack(outputData[0]));
            }
        } catch (Exception e) {
            this.logData.setProgramException(e);
            this.logData.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, StringUtils.EMPTY,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCPINBlockConvertError;
        }
        return rc;
    }

    /**
     * 產生Credit電文訊息押碼(MAC)
     *
     * @param scTxid 信用卡交易代號
     * @param mac    產生的MAC
     * @return
     */
    public FEPReturnCode makeScMac(String scTxid, RefString mac) {
        String encFunc = "FN000311";
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            rc = this.prepareCreditMac(TxType.TITA, scTxid, StringUtils.EMPTY, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter param = new ENCParameter();
                param.setKeyIdentity(keyId1.get());
                param.setInputData1(inputData1.get());
                param.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{param};
                String[] outputData = new String[]{StringUtils.EMPTY};
                encrc = this.encLib(encFunc, "makeScMac", encpar, outputData);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCMakeMACError;
                } else {
                    mac.set(this.pack(outputData[0]));
                }
            }
        } catch (Exception e) {
            this.logData.setProgramException(e);
            this.logData.setRemark(this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    /**
     * ===============================================================================================================================================
     * ==============================================================以下是共用Function===============================================================
     * ===============================================================================================================================================
     */

    protected FEPReturnCode prepareAtmTac(int tacType, RefString keyIdentity, RefString inputData1,
                                          RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        StringBuilder tacInputData = new StringBuilder();
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3);
        // 2014-12-05 Modify by Ruling for PSP TSM
        if (DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) && StringUtils.isBlank(this.feptxn.getFeptxnTxCode())) {
            // 原存交易
            switch (this.feptxn.getFeptxnIcmark().substring(28, 29)) {
                case "3": // 行動金融卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.TWMP);
                    if ("3".equals(this.feptxn.getFeptxnIcmark().substring(29, 30))) {
                        // 1003檔-近端交易
                        // 2014-12-18 Modified By ChenLi for 小額支付限額改成讀資料庫共用參數
                        if (MathUtil.compareTo(this.feptxn.getFeptxnTxAmt(),
                                INBKConfig.getInstance().getINBKSALimit()) > 0) { // 交易金額> 3000 用C6 Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                        } else { // 交易金額<=3000 用C8 Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C8);
                        }
                    } else {
                        // 1001檔-遠端交易
                        key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    }
                    break;
                case "4": // 感應式金融卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
                    if ("3".equals(this.feptxn.getFeptxnIcmark().substring(29, 30))) {
                        // 1003檔-近端交易
                        // 2014-12-18 Modified By ChenLi for 小額支付限額改成讀資料庫共用參數
                        if (this.feptxn.getFeptxnTxAmt().intValue() > INBKConfig.getInstance().getINBKSALimit()) { // 交易金額>
                            // 3000
                            // 用C6
                            // Key
                            // 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                        } else { // 交易金額<=3000 用C8 Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C8);
                        }
                    } else {
                        // 1001檔-遠端交易
                        key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    }
                    break;
                default:// 一般金融卡、Combo卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
                    key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    break;
            }
        } else {
            // 自行、代理跨行交易
            key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
        }
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());
        keyIdentity.set(key.toString());
        // Prepare InputData
        switch (tacType) {
            case 1:
                // for ‘2510’, ‘2561’, ‘2562’, ‘2563’, ‘2564’ 提款, 全國繳費
                // for ‘IWD’, ‘IFW’, ‘ACW’, ‘EFT’ 提款, 全國繳費
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 2:
                // for ‘2521’,‘2522’, ‘2523’, ‘2524’, ‘2525’ 轉帳
                // for "IFT" ,"ATF", "BFT","IPA", "IDR" 轉帳/預約轉帳/存款
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTrinActno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 3:
                // for ‘2541’, ‘2552’ 消費扣款, 授權完成
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData,
                        StringUtils.rightPad(this.feptxn.getFeptxnMerchantId(), 30, StringUtils.SPACE));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 4:
                // for ‘2551’ 預先授權
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqnoPreauth());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(this.feptxn.getFeptxnTxAmtPreauth().toString(), 12, '0'));
                StringUtil.append(tacInputData, "00");
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimePreauth());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 5:
                // for ‘2531’ , ‘2532’, ‘2567’, ‘2568’ 跨行繳款,自行繳款,全國繳稅
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPaytype());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                if (!"15".equals(this.feptxn.getFeptxnPaytype().substring(0, 2))) {// Not核定繳稅
                    // 核定繳稅交易已將銷帳編號放入 TRIN_ACTNO, 且右補0
                    // StringUtil.append(tacInputData,
                    // this.feptxn.FEPTXN_TRIN_ACTNO.PadRight(16,'0')); /*銷帳編號*/
                    // 若銷帳編號不滿16位需右補空白, 且長度以16計算
                    StringUtil.append(tacInputData,
                            StringUtils.rightPad(this.feptxn.getFeptxnReconSeqno(), 16, StringUtils.SPACE));
                } else {
                    StringUtil.append(tacInputData, "00000");
                    // 2011/07/07 modify by Ruling IDNO不滿11位需右補空白, 且長度以11計算
                    StringUtil.append(tacInputData,
                            StringUtils.rightPad(this.feptxn.getFeptxnIdno(), 11, StringUtils.SPACE));
                }
                break;
            case 6:
                // for ‘2500’ 查詢
                // for "IIQ", "IFE", "AIN" 查詢
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 7:
                // Fly 2018/08/09 for 2555 跨境電子支付
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnCashAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData,
                        StringUtils.rightPad(this.feptxn.getFeptxnMerchantId(), 15, StringUtils.SPACE));
                break;
            case 8:
                // Fly 2019/01/03 for 2566 金融帳戶資訊核驗
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                // Fly 2019/01/24 應取34位
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTrk3().substring(2, 36));
                break;
        }
        // personal information 1 + personal information 2 + personal information 3
        String divData = this.makeInputDataString(
                this.feptxn.getFeptxnIcmark().substring(0, 16),
                this.feptxn.getFeptxnIcmark().substring(0, 14),
                "00",
                "00000000000000",
                this.feptxn.getFeptxnIcmark().substring(14, 16));
        inputData1.set(tacInputData.toString());
        inputData2.set(this.makeInputDataString(divData, unPack(this.feptxn.getFeptxnIcTac())));
        return rc;
    }

    private FEPReturnCode prepareAtmMac(TxType txType, String strData, String mac, RefString keyIdentity,
                                        RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;

        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3); // Triple ENC
        key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(txData.getFeptxn().getFeptxnAtmno());
        key.getKeyId1().setKeyVersion("1"); // 會自動補滿兩位數
        keyIdentity.set(key.toString());

        switch (txType) {
            case TITA: // ATM上行電文Request & Confirm
                inputData1.set(strData);
                if (StringUtils.isNotBlank(mac)) {
                    inputData2.set(mac);
                }
                break;
            case TOTA: // ATM下行電文Response
                inputData1.set(strData);
                inputData2.set("");
                break;
        }
        return rc;
    }

    private FEPReturnCode prepareCreditMac(TxType txType, String sctxid, String mac, RefString keyIdentity,
                                           RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.S1);
        key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        /* 11/10 修改抓取系統參數檔 */
        key.getKeyId1()
                .setKeySubCode(StringUtils.rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                        ATMPConfig.getInstance().getCreditMACKey()), 8, StringUtils.SPACE));
        keyIdentity.set(key.toString());
        String icv;
        String input = StringUtils.EMPTY;
        String wValid = StringUtils.EMPTY;
        String wTxseq = StringUtils.EMPTY;
        String wTrinActno = StringUtils.EMPTY;
        CreditTXCD txid = CreditTXCD.parse(sctxid);
        // TO-信用卡電文, 組 MAC INPUT DATA */
        if (txType == TxType.TITA) {
            // 2010-04-22 modified by kyo for 修改信用卡組inputData1邏輯=>Confirm時要取CON_TXSEQ
            if (StringUtils.isBlank(this.feptxn.getFeptxnConTxseq())) {
                if (this.feptxn.getFeptxnTxseq().length() < 5) {
                    wTxseq = StringUtils.leftPad(this.feptxn.getFeptxnTxseq(), 5, '0');
                } else {
                    wTxseq = this.feptxn.getFeptxnTxseq().substring(this.feptxn.getFeptxnTxseq().length() - 5);
                }
            } else if (this.feptxn.getFeptxnTxseq().length() < 5) {
                wTxseq = StringUtils.leftPad(this.feptxn.getFeptxnConTxseq(), 5, '0');
            } else {
                wTxseq = this.feptxn.getFeptxnConTxseq().substring(this.feptxn.getFeptxnTxseq().length() - 5);
            }
            if (StringUtils.isBlank(this.feptxn.getFeptxnYymmdd()))
                if (DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag())) {
                    // 跨行交易, 取FEPTXN_ATMNO_VIR 第1~5碼
                    icv = this.makeInputDataString(this.feptxn.getFeptxnAtmnoVir().substring(0, 5), wTxseq,
                            CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).substring(1, 7));
                } else {
                    icv = this.makeInputDataString(this.feptxn.getFeptxnAtmno().substring(0, 5), wTxseq,
                            CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).substring(1, 7));
                }
            else {
                icv = this.makeInputDataString(this.feptxn.getFeptxnAtmno().substring(0, 5), wTxseq,
                        this.feptxn.getFeptxnYymmdd());
            }
            // 2010-11-03 by kyo for update:/* 11/2 修改, B16/B20/C07 不須取得卡片有效月年 */
            if (txid == CreditTXCD.B07 || txid == CreditTXCD.B09 || txid == CreditTXCD.B10 || txid == CreditTXCD.B11
                    || txid == CreditTXCD.B12) {
                if (StringUtils.isNotBlank(this.feptxn.getFeptxnTrk2())) {
                    int indexOf = this.feptxn.getFeptxnTrk2().indexOf('=');
                    wValid = this.feptxn.getFeptxnTrk2().substring(indexOf + 1, indexOf + 1 + 4);
                } else {
                    if (StringUtils.isNotBlank(this.feptxn.getFeptxnTrk3())) {
                        // 2010-08-30 by kyo for spec修改:/* 8/30 修正-卡片有效月年 */抓trk3第二個"="的前六位的前四位
                        String[] strSplit = this.feptxn.getFeptxnTrk3().split("="); // 用"="分割
                        wValid = strSplit[1].substring(strSplit[1].length() - 6, strSplit[1].length() - 6 + 4); // 取第二段的前六位的前四位
                    }
                }
            }
            // wValid 給預設值，預防下段做Substring時發生Error
            if (StringUtils.isBlank(wValid)) {
                wValid = "0000";
            }
            // wValid 應該要填入月年 而不是年月 因此需要年月互換
            wValid = StringUtils.join(wValid.substring(2, 4), wValid.substring(0, 2));
            switch (txid) {
                case C07:
                    // 2011-07-28 by kyo for SPEC修改/* 7/28 配合永豐, 修改 C07 產生 MAC 方式 */
                    // 2013-11-25 Modify by Ruling for 現金儲值卡自動加值
                    if (StringUtils.isBlank(this.feptxn.getFeptxnTrinActno())) {
                        wTrinActno = "0000000000000000";
                    } else {
                        wTrinActno = this.feptxn.getFeptxnTrinActno();
                    }
                    icv = this.makeInputDataString(
                            StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 5, StringUtils.SPACE).substring(0, 5),
                            wTxseq,
                            CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).substring(1, 7));
                    input = this.makeInputDataString(
                            this.feptxn.getFeptxnTroutActno(),
                            wTrinActno,
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            StringUtils.rightPad(this.feptxn.getFeptxnIdno(), 8, StringUtils.SPACE).substring(2, 8));
                    break;
                case C22:
                    // 2013-11-25 Modify by Ruling for 現金儲值卡自動加值
                    if (StringUtils.isBlank(this.feptxn.getFeptxnTrinActno())) {
                        wTrinActno = "0000000000000000";
                    } else {
                        wTrinActno = this.feptxn.getFeptxnTrinActno();
                    }
                    wTxseq = StringUtils.leftPad(this.feptxn.getFeptxnTxseq(), 5, '0');// 因借用FEPTXN_CON_TXSEQ取得的wTxseq值不對，所以再指定使用FEPTXN_TXSEQ
                    icv = this.makeInputDataString(
                            StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 5, StringUtils.SPACE).substring(0, 5),
                            wTxseq,
                            CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).substring(1, 7));
                    input = this.makeInputDataString(
                            wTrinActno,
                            this.feptxn.getFeptxnTroutActno(),
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            StringUtils.rightPad(this.feptxn.getFeptxnIdno(), 8, StringUtils.SPACE).substring(2, 8));
                    break;
                case B07:
                    // 2011-11-08 Modify by Ruling for BFT有帳號要使用FEPTXN_TRIN_ACTNO其餘電文補16位0
                    if (ATMTXCD.BFT.name().equals(this.feptxn.getFeptxnTxCode())) {
                        // 2010-08-27 by kyo for /* 8/30 修正- 信用卡號 */
                        // 西松分行NCR送的電文帳號不對，需要統一改抓FEPTXN_TRK2.Substring(0,16)
                        input = this.makeInputDataString(
                                StringUtils.leftPad(this.feptxn.getFeptxnTrk2().substring(0,
                                        this.feptxn.getFeptxnTrk2().indexOf("=")), 16, '0'),
                                StringUtils.leftPad(
                                        MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10,
                                        '0'),
                                wValid,
                                "00",
                                this.feptxn.getFeptxnTrinActno());
                    } else {
                        // 2010-08-27 by kyo for /* 8/30 修正- 信用卡號 */
                        // 西松分行NCR送的電文帳號不對，需要統一改抓FEPTXN_TRK2.Substring(0,16)
                        input = this.makeInputDataString(
                                StringUtils.leftPad(this.feptxn.getFeptxnTrk2().substring(0,
                                        this.feptxn.getFeptxnTrk2().indexOf("=")), 16, '0'),
                                StringUtils.leftPad(
                                        MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10,
                                        '0'),
                                wValid,
                                "00",
                                StringUtils.rightPad("0", 16, '0'));
                    }
                    break;
                case B09:
                    input = this.makeInputDataString(this.feptxn.getFeptxnTroutActno(), wValid, "000000000000");
                    break;
                case B10:
                case B11:
                case B12:
                    String wActno;
                    ATMTXCD atxcd = ATMTXCD.parse(this.feptxn.getFeptxnTxCode());
                    switch (atxcd) {
                        case ATF:
                        case AFF:
                        case IFT:
                        case IFF:
                            wActno = this.feptxn.getFeptxnTrinActno();
                            break;
                        case APY:
                        case APF:
                        case IPY:
                        case IPF:
                            wActno = StringUtils.leftPad(this.feptxn.getFeptxnReconSeqno(), 16, '0');
                            break;
                        default:
                            wActno = StringUtils.leftPad("0", 16, '0');
                            break;
                    }
                    // BugReport(001B0615):2010-06-03 by kyo for SPEC修改:B10,B11,B12電文中
                    // wValid若TRK2,TRK3皆沒有值時需要帶 "0000"來檢核MAC
                    if (StringUtils.isBlank(wValid)) {
                        wValid = "0000";
                    }
                    input = this.makeInputDataString(
                            this.feptxn.getFeptxnTroutActno(),
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            wValid,
                            "00",
                            wActno);
                    break;
                case B16:
                case B17:
                case B20:
                    // modified by Maxine on 2011/11/15 for spec 11/15 修改 B17 押 MAC 方式
                    if (DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag())) {
                        // 跨行交易, STAN 取第2~7碼
                        input = this.makeInputDataString(
                                this.feptxn.getFeptxnTroutActno(),
                                StringUtils.leftPad(
                                        MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10,
                                        '0'),
                                StringUtils.leftPad(this.feptxn.getFeptxnStan(), 7, '0').substring(1, 7));

                    } else {
                        // 自行交易
                        input = this.makeInputDataString(
                                this.feptxn.getFeptxnTroutActno(),
                                StringUtils.leftPad(
                                        MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10,
                                        '0'),
                                StringUtils.leftPad("0", 6, '0'));
                    }
                    break;
                default:
                    break;
            }
            inputData1.set(icv);
            inputData2.set(input);
        }
        // * RCV-信用卡電文, 組 MAC INPUT DATA */
        else {
            // 2010-11-03 by kyo for update:/* 11/2 修改, B16/B20/C07 不須取得卡片有效月年 */
            if (txid == CreditTXCD.B07 || txid == CreditTXCD.B10) {
                if (StringUtils.isNotBlank(this.feptxn.getFeptxnTrk2())) {
                    int indexOf = this.feptxn.getFeptxnTrk2().indexOf('=');
                    wValid = this.feptxn.getFeptxnTrk2().substring(indexOf + 1, indexOf + 1 + 4);
                } else {
                    if (StringUtils.isNotBlank(this.feptxn.getFeptxnTrk3())) {
                        // 2010-08-30 by kyo for spec修改:/* 8/30 修正-卡片有效月年 */抓trk3第二個"="的前六位的前四位
                        String[] strSplit = this.feptxn.getFeptxnTrk3().split("="); // 用"="分割
                        wValid = strSplit[1].substring(strSplit[1].length() - 6, strSplit[1].length() - 6 + 4); // 取第二段的前六位的前四位
                    }
                }
            }
            // wValid 給預設值，預防下段做Substring時發生Error
            if (StringUtils.isBlank(wValid)) {
                wValid = "0000";
            }
            // wValid 應該要填入月年 而不是年月 因此需要年月互換
            wValid = StringUtils.join(wValid.substring(2, 4), wValid.substring(0, 2));
            // for ICV, InputData
            switch (txid) {
                case C07:
                    icv = this.feptxn.getFeptxnTroutActno();
                    input = this.makeInputDataString(
                            this.feptxn.getFeptxnTrinActno(),
                            this.feptxn.getFeptxnMajorActno(),
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            this.feptxn.getFeptxnIdno().substring(1, 7));
                    break;
                case C22:
                    // 2013-11-25 Modify by Ruling for 現金儲值卡自動加值
                    icv = this.feptxn.getFeptxnTrinActno();
                    input = this.makeInputDataString(
                            this.feptxn.getFeptxnCbsIntActno(),
                            this.feptxn.getFeptxnTroutActno(),
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            this.feptxn.getFeptxnIdno().substring(1, 7));
                    break;
                default:
                    icv = this.makeInputDataString(
                            this.feptxn.getFeptxnAtmno().substring(0, 5),
                            StringUtils.leftPad(this.feptxn.getFeptxnConTxseq(), 5, '0'),
                            StringUtils.leftPad(this.feptxn.getFeptxnAuthcd(), 6, '0'));
                    input = this.makeInputDataString(
                            this.feptxn.getFeptxnTroutActno(),
                            StringUtils.leftPad(
                                    MathUtil.replace(this.feptxn.getFeptxnTxAmt(), ".00", StringUtils.EMPTY), 10, '0'),
                            wValid,
                            "00");
                    break;
            }
            inputData1.set(this.makeInputDataString(icv, mac));
            inputData2.set(input);
        }
        return rc;
    }

    /**
     * 2017-01-26 Modify by Ruling for 無卡提款(NWD、NWF、NCS)
     *
     * @param txcd
     * @return
     */
    private String getTxcd(ATMTXCD txcd) {
        String wTxcd;
        switch (txcd) {
            case CWD:
            case FWD:
            case FAW:
            case ACW:
            case IWD:
            case IFW:
                // 2013-01-29 Modify by Ruling for 硬幣提款
            case ICW:
            case NWD:
                // Fly 2018/10/11 For 外幣無卡提款
            case NFW:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PWD:
                wTxcd = "001";
                break;
            case DEP:
            case CDR:
            case IDP:
            case IDR:
                // 2012-08-10 Modify by Ruling for 企業入金
            case BDR:
                // 2013-02-08 Modify by Ruling for 企業入金機入帳
            case BDF:
                // 2012-09-07 Modify by Ruling for 硬幣存款
            case ICR:
            case CCR:
                // 2016-07-28 Modify by Ruling for ATM新功能-跨行存款和現金捐款
            case ODR:
            case DDR:
                // Fly 2018/10/12 For OKI硬幣機 增加CFT
            case CFT:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PDR:
            case PCR:
                wTxcd = "002";
                break;
            case TFR:
            case PAU:
            case ATF:
            case IFT:
            case IPA:
            case EFT:
                // 2011-08-25 modified by Ruling for 新增電文
            case BFT:
                // 2016-07-28 Modify by Ruling for ATM新功能-繳汽燃費
            case OFT:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PFT:
                wTxcd = "003";
                break;
            case INQ:
            case IQ2:
            case B05:
            case APP:
                // 2011-05-20 by kyo for 開卡流程新電文AP1
            case AP1:
            case AIN:
            case IIQ:
            case B15:
            case GIQ:
                // 2011-09-27 by maxine for 國際卡餘額查詢
            case IQV:
            case IQM:
                // 補上銀聯卡
            case IQC:
                // 2012-08-10 Modify by Ruling for 企業入金
            case INI:
                // Fly 2016/01/22 新增 for EMV晶片卡
            case EQC:
            case EQP:
            case EQU:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PAC:
                wTxcd = "004";
                break;
            case AVB:
                wTxcd = "005";
                break;
            case PAY:
            case APY:
            case IPY:
                wTxcd = "006";
                break;
            case CWF:
            case FWF:
            case FAF:
            case ACF:
            case IWF:
            case IFC:
                // ChenLi, 2015/03/02, 新增 for 跨行外幣提款
            case FAC:
            case NWF:
                // Fly 2018/10/11 For 外幣無卡提款
            case NFF:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PWF:
                wTxcd = "007";
                break;
            case INF:
                wTxcd = "008";
                break;
            case DPF:
            case CDF:
            case IDC:
            case DEX:
            case CDX:
                // 2016-07-28 Modify by Ruling for ATM新功能-跨行存款和現金捐款
            case ODF:
            case DDF:
                // Fly 2018/10/12 For OKI硬幣機 增加CCF
            case CCF:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PDF:
            case PCF:
                wTxcd = "009";
                break;
            case TFF:
            case PAF:
            case AFF:
            case APF:
            case IFF:
            case IAF:
            case IPF:
            case EFF:
                // 2011-08-25 modified by Ruling for 新增電文
            case BFF:
                // 2016-07-28 Modify by Ruling for ATM新功能-繳汽燃費
            case OFF:
                // 2020-10-06 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
            case PFF:
                wTxcd = "010";
                break;
            case NDF:
                wTxcd = "011";
                break;
            case NPF:
                wTxcd = "012";
                break;
            case CWV:
            case CWM:
            case CAV:
            case CAM:
            case CAA:
            case CAJ:
                // 補上銀聯卡
            case CUP:
                // Fly 2016/01/22 新增 for EMV晶片卡
            case EWV:
            case EWM:
            case EAV:
            case EAM:
            case EUP:
                wTxcd = "013";
                break;
            case CFP:
            case CFC:
            case CFV:
            case CFM:
            case CFA:
            case CFJ:
                // 補上銀聯卡
            case CFU:
                // Fly 2016/01/22 新增 for EMV晶片卡
            case EFP:
            case EFC:
            case EFV:
            case EFM:
            case EFU:
                wTxcd = "014";
                break;
            // 2016/06/15 Modify by Ruling for COMBO開卡作業優化增加G50
            case G50:
            case G51:
            case NCS:
                wTxcd = "015";
                break;
            default:
                wTxcd = "000";
                break;
        }
        return wTxcd;
    }

    private String getExpcd(String expCode) {
        expCode = StringUtils.rightPad(expCode, 4, StringUtils.SPACE);
        String wExpcd;
        switch (expCode.substring(0, 1)) {
            case StringUtils.SPACE:
            case "W":
                wExpcd = "0";
                break;
            case "A":
                wExpcd = "1";
                break;
            case "B":
                wExpcd = "2";
                break;
            case "C":
                wExpcd = "3";
                break;
            case "D":
                wExpcd = "4";
                break;
            case "E":
                wExpcd = "5";
                break;
            case "F":
                wExpcd = "6";
                break;
            case "G":
                wExpcd = "7";
                break;
            case "H":
                wExpcd = "8";
                break;
            case "I":
                wExpcd = "9";
                break;
            default:
                wExpcd = expCode.substring(0, 1);
                break;
        }
        return wExpcd;
    }

    private FEPReturnCode prepareNcPinData(String pinBlock, String offset, String keyKind, RefString keyIdentity,
                                           RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        key.setKeyType1(ENCKeyType.T2);
        key.getKeyId1().setKeyKind(ENCKeyKind.valueOf(keyKind));
        // key.KeyId1.KeyKind = ENCKeyKind.PVK;
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());

        key.setKeyType2(DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) ? ENCKeyType.T2 : ENCKeyType.T3);
        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2().setKeySubCode(
                DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) ? SysStatus.getPropertyValue().getSysstatFbkno()
                        : StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                        this.feptxn.getFeptxnAtmno()));
        keyIdentity.set(key.toString());

        inputData1.set(StringUtils.EMPTY);
        if (StringUtils.isBlank(this.feptxn.getFeptxnZoneCode())) {
            throw ExceptionUtil.createException("FEPTXN_ZONE_CODE無值");
        }

        if (ATMZone.TWN.name().equals(this.feptxn.getFeptxnZoneCode())) {
            inputData1.set(this.makeInputDataString(
                    this.unPack(pinBlock),
                    this.feptxn.getFeptxnMajorActno(),
                    "0000",
                    this.feptxn.getFeptxnTroutActno().substring(4, 16),
                    offset));
        }
        inputData2.set(StringUtils.EMPTY);
        return rc;
    }

    /**
     * 組檢核香港臨櫃變更PIN BLOCK資料
     *
     * @param method      00-驗證OFFSET, 01-產生OFFSET
     * @param pinBlock    PIN BLOCK
     * @param offset      OLD OFFSET
     * @param keyIdentity KEY DATA
     * @param inputData1  inputData1
     * @param inputData2  inputData2
     * @return FEPReturnCode
     * @throws Exception
     */
    private FEPReturnCode prepareHkAuthData(String method, String pinBlock, String offset, RefString keyIdentity,
                                            RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        key.setKeyType1(ENCKeyType.S1);
        key.getKeyId1().setKeyKind(ENCKeyKind.PVK);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());

        key.setKeyType2(DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) ? ENCKeyType.T2 : ENCKeyType.T3);
        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2().setKeySubCode(
                DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) ? SysStatus.getPropertyValue().getSysstatFbkno()
                        : StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),
                        this.feptxn.getFeptxnAtmno()));
        keyIdentity.set(key.toString());

        inputData1.set(StringUtils.EMPTY);
        inputData2.set(StringUtils.EMPTY);
        if ("00".equals(method)) {
            // 驗證OFFSET
            inputData1.set(this.makeInputDataString(
                    method,
                    this.feptxn.getFeptxnTroutActno().substring(2, 16),
                    this.feptxn.getFeptxnTrk2().substring(35, 37),
                    "0000",
                    this.feptxn.getFeptxnTrk2().substring(6, 18), offset));
            inputData2.set(unPack(pinBlock));
        } else if ("01".equals(method)) {
            // 產生OFFSET
            inputData1.set(this.makeInputDataString(
                    method,
                    this.feptxn.getFeptxnTroutActno().substring(2, 16),
                    this.feptxn.getFeptxnTrk2().substring(35, 37),
                    "0000",
                    this.feptxn.getFeptxnTrk2().substring(6, 18),
                    "0000FFFFFFFF"));
            inputData2.set(unPack(pinBlock));
        }
        return rc;
    }

    /**
     * zk add
     */
    public FEPReturnCode prepareATMICTac(int tacType, String txCur, String country, RefString keyIdentity,
                                         RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        StringBuilder tacInputData = new StringBuilder();
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3);
        key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());
        keyIdentity.set(key.toString());

        // Prepare InputData
        switch (tacType) {
            case 1:
                // for 2571 晶片卡跨國提款
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                if ("2572".equals(this.feptxn.getFeptxnPcode())) {
                    StringUtil.append(tacInputData, "2571");
                } else {
                    StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                }
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, txCur);
                StringUtil.append(tacInputData, country);
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk().substring(6, 8));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
            case 3:
                // for 2545 晶片卡跨國消費扣款
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                if ("2546".equals(this.feptxn.getFeptxnPcode())) {
                    StringUtil.append(tacInputData, "2545");
                } else {
                    StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                }
                StringUtil.append(tacInputData,
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
                StringUtil.append(tacInputData, txCur);
                StringUtil.append(tacInputData, country);
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk().substring(6, 8));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnMerchantId());
                break;
            case 6:
                // for 2505 晶片卡跨國餘額查詢
                StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
                StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
                StringUtil.append(tacInputData, txCur);
                StringUtil.append(tacInputData, country);
                StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk().substring(6, 8));
                StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
                break;
        }

        // personal information 1 + personal information 2 + personal information 3
        String divData = StringUtils.join(this.feptxn.getFeptxnIcmark().substring(0, 16),
                this.feptxn.getFeptxnIcmark().substring(0, 14), "00", "00000000000000",
                this.feptxn.getFeptxnIcmark().substring(14, 16));
        inputData1.set(tacInputData.toString());
        inputData2.set(StringUtils.join(divData, unPack(this.feptxn.getFeptxnIcTac())));
        return rc;
    }

    /**
     * 變更香港臨櫃變更國際提款密碼
     *
     * @param oldPinBlock
     * @param newPinBlock
     * @param offset
     * @param returndata
     * @return
     */
    public FEPReturnCode changeHKAuth(String oldPinBlock, String newPinBlock, String offset, RefString returndata) {
        returndata.set("");
        FEPReturnCode rc = FEPReturnCode.Normal;
        // 檢核舊密碼
        rc = checkHKAuth(oldPinBlock, offset);
        if (rc != FEPReturnCode.Normal) {
            return rc;
        }

        // 產生新AUTH
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        final String encFunc = "FN000208";
        int encrc = -1;
        try {
            rc = prepareHKAuthData("01", newPinBlock, "", keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter tempVar = new ENCParameter();
                tempVar.setKeyIdentity(keyId1.get());
                tempVar.setInputData1(inputData1.get());
                tempVar.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{tempVar};
                String[] rtnAry = new String[]{"", ""};
                encrc = encLib(encFunc, "changeHKAuth", encpar, rtnAry);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCChangePasswordError;
                } else {
                    returndata.set(rtnAry[0]);
                }
            }
        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCChangePasswordError;
        }
        return rc;
    }

    /**
     * 變更ATM密碼
     *
     * @param oldSscode  舊密碼
     * @param newSscode  新密碼
     * @param returnData ReturnData{1} : NewOffset(H4)
     * @return FEPReturnCode <history> <modify> <modifier>Kyo</modifier>
     * <reason>SPEC修改:新增PNM邏輯</reason> <date>2010/05/27</date> </modify>
     * </history>
     * @throws Exception
     */
    public FEPReturnCode changePassword(String oldSscode, String newSscode, String offset, RefString returnData)
            throws Exception {
        returnData.set("");
        String inputData1 = "";
        String inputData2 = "";
        final String encFunc = "FN000213";
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        key.setKeyType1(ENCKeyType.S1);
        key.getKeyId1().setKeyKind(ENCKeyKind.PVK);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());

        key.setKeyType2(ENCKeyType.T3);
        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2().setKeySubCode(
                StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), feptxn.getFeptxnAtmno()));
        String keyIdentity = key.toString();
        oldSscode = unPack(oldSscode);
        newSscode = unPack(newSscode);

        if (StringUtils.isBlank(feptxn.getFeptxnZoneCode().trim())) {
            throw new RuntimeException("FEPTXN_ZONE_CODE無值");
        }

        if (ATMZone.TWN.name().equals(feptxn.getFeptxnZoneCode())) {
            // 2012-07-17 Modfify by Ruling for PN0 電文傳入值新密碼
            if (ATMTXCD.PN0.name().equals(feptxn.getFeptxnTxCode())) {
                if (feptxn.getFeptxnIcmark().substring(0, 3).equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    /* 卡號發卡 */
                    inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                            StringUtils.leftPad(String.valueOf(feptxn.getFeptxnCardSeq()), 2, '0'), "0000",
                            feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                } else {
                    /* 帳號發卡 */
                    inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                            feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                }
            } else {
                if (StringUtils.isBlank(feptxn.getFeptxnTrk2())) {
                    if (feptxn.getFeptxnIcmark().substring(0, 3)
                            .equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                        /* 卡號發卡 */
                        inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                                StringUtils.leftPad(String.valueOf(feptxn.getFeptxnCardSeq()), 2, '0'), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                    } else {
                        /* 帳號發卡 */
                        inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                    }
                } else {
                    // 2011-09-14 by YIN 由 CVV判斷發卡方式
                    if (Integer.parseInt(feptxn.getFeptxnTrk2().substring(32, 35)) > 0) {
                        inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                                feptxn.getFeptxnTrk2().substring(35, 37), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                    } else {
                        inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), oldSscode, offset);
                    }
                }
            }
        } else {
            inputData1 = StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                    feptxn.getFeptxnTrk2().substring(35, 37), "0000", feptxn.getFeptxnTrk2().substring(6, 18),
                    oldSscode, offset); // 海外分行卡
        }
        inputData2 = newSscode;
        int encrc = -1;
        try {
            ENCParameter tempVar = new ENCParameter();
            tempVar.setKeyIdentity(keyIdentity);
            tempVar.setInputData1(inputData1);
            tempVar.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{tempVar};
            String[] rtnAry = new String[]{"", ""};
            encrc = encLib(encFunc, "changePassword", encpar, rtnAry);
            if (encrc != 0) {
                rc = ENCReturnCode.ENCChangePasswordError;
            } else {
                returnData.set(StringUtils.isNotBlank(rtnAry[0]) ? pack(rtnAry[0]) : pack(rtnAry[1]));
            }
        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2, StringUtils.EMPTY,
                    StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCChangePasswordError;
        }
        return rc;
    }

    /**
     * 檢核ATM密碼
     *
     * @param sscode     密碼
     * @param offset     OFFSET
     * @param returnData NewOffset(H4)
     * @return FEPReturnCode
     */
    public FEPReturnCode checkPassword(String sscode, String offset, RefString returnData) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        returnData.set("");
        final String encFunc = "FN000203";
        FEPReturnCode rc = FEPReturnCode.Normal;

        int encrc = -1;
        try {
            rc = preparePinData(sscode, offset, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter tempVar = new ENCParameter();
                tempVar.setKeyIdentity(keyId1.get());
                tempVar.setInputData1(inputData1.get());
                tempVar.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{tempVar};
                String[] rtnAry = new String[]{"", ""};
                encrc = encLib(encFunc, "checkPassword", encpar, rtnAry);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCCheckPasswordError;
                }
                returnData.set(StringUtils.isNotBlank(rtnAry[0]) ? pack(rtnAry[0]) : pack(rtnAry[1]));
            }

        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckPasswordError;
        }
        return rc;
    }

    /**
     * 檢核香港臨櫃變更國際提款密碼
     *
     * @param pinBlock PIN BLOCK
     * @param offset   OFFSET
     * @return FEPReturnCode
     */
    public FEPReturnCode checkHKAuth(String pinBlock, String offset) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        // final String encFunc = "FN000208";
        FEPReturnCode rc = FEPReturnCode.Normal;

        int encrc = -1;
        try {
            rc = prepareHKAuthData("00", pinBlock, offset, keyId1, inputData1, inputData2);
            if (rc == FEPReturnCode.Normal) {
                ENCParameter tempVar = new ENCParameter();
                tempVar.setKeyIdentity(keyId1.get());
                tempVar.setInputData1(inputData1.get());
                tempVar.setInputData2(inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{tempVar};
                String[] rtnAry = new String[]{"", ""};
                encrc = encLib("FN000208", "checkHKAuth", encpar, rtnAry);
                if (encrc != 0) {
                    rc = ENCReturnCode.ENCCheckPasswordError;
                }
            }
        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage("FN000208", keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckPasswordError;
        }
        return rc;
    }

    /**
     * 檢核交易驗證碼(TAC)
     *
     * @param tacType MSGCTL_TAC_TYPE(TAC類別)
     * @return FEPReturnCode <modify> <modifier>Kyo</modifier>
     * <reason>BugReport(001B0341):由WEB ATM進行交易，ENC RC=102，無法進行交易</reason>
     * <date>2010/4/27</date> </modify>
     */
    public FEPReturnCode checkATMTAC(int tacType) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData1 = new RefString(StringUtils.EMPTY);
        RefString inputData2 = new RefString(StringUtils.EMPTY);
        final String encFunc = "FN000402";
        FEPReturnCode rc = FEPReturnCode.Normal;
        int encrc = -1;
        try {
            rc = prepareATMTac(tacType, keyId1, inputData1, inputData2);

            if (rc == FEPReturnCode.Normal) {
                ENCParameter tempVar = new ENCParameter();
                tempVar.setKeyIdentity(keyId1.get());
                tempVar.setInputData1(inputData1.get());
                tempVar.setInputData2("4649534343415244" + inputData2.get());
                ENCParameter[] encpar = new ENCParameter[]{tempVar};
                // string[] outputData= new[] {""};
                encrc = encLib(encFunc, "checkATMTAC", encpar, null);
                rc = getENCReturnCode(encrc);
            }
        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckTACError;
        }
        return rc;
    }

    /**
     * 檢核 ATM 電文訊息押碼(MAC)及交易驗證碼(TAC)
     *
     * @param mac     訊息押碼(MAC)
     * @param tacType MSGCTL_TAC_TYPE(TAC類別)
     * @return FEPReturnCode
     */
    public FEPReturnCode checkATMMACAndTAC(String mac, int tacType) {
        RefString keyId1 = new RefString(StringUtils.EMPTY);
        RefString inputData11 = new RefString(StringUtils.EMPTY);
        RefString inputData12 = new RefString(StringUtils.EMPTY);
        RefString keyId2 = new RefString(StringUtils.EMPTY);
        RefString inputData21 = new RefString(StringUtils.EMPTY);
        RefString inputData22 = new RefString(StringUtils.EMPTY);
        String inputData1 = "";
        final String encFunc = "FN000308";
        FEPReturnCode rc = FEPReturnCode.Normal;
        int encrc = -1;
        try {
            mac = unPack(mac);

            rc = prepareAtmMac(TxType.TITA, "", mac, keyId1, inputData11, inputData12);

            if (rc == FEPReturnCode.Normal) {

                rc = prepareATMTac(tacType, keyId2, inputData21, inputData22);

            }

            inputData1 = StringUtils.join("0002", keyId1.get(),
                    PolyfillUtil.toString(inputData11.get().length(), "0000"),
                    StringUtils.rightPad(inputData11.get(), 128, ' '),
                    PolyfillUtil.toString(inputData12.get().length(), "0000"),
                    StringUtils.rightPad(inputData12.get(), 128, ' '), keyId2.get(),
                    PolyfillUtil.toString(inputData21.get().length(), "0000"),
                    StringUtils.rightPad(inputData21.get(), 128, ' '),
                    PolyfillUtil.toString(inputData22.get().length(), "0000"),
                    StringUtils.rightPad(inputData22.get(), 128, ' '));
            if (rc == FEPReturnCode.Normal) {
                ENCParameter tempVar = new ENCParameter();
                tempVar.setKeyIdentity("");
                tempVar.setInputData1(inputData1);
                tempVar.setInputData2("");
                ENCParameter[] encpar = new ENCParameter[]{tempVar};
                // string[] outputData= new[] {""};
                encrc = encLib(encFunc, "checkATMMACAndTAC", encpar, null);
                if (encrc != 0) {
                    if (encrc == 101) {
                        rc = ENCReturnCode.ENCCheckMACError;
                    } else if (encrc == 102) {
                        rc = ENCReturnCode.ENCCheckTACError;
                    } else {
                        rc = ENCReturnCode.ENCCheckMACTACError;
                    }
                }
            }
        } catch (Exception ex) {
            logData.setProgramException(ex);
            logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1, StringUtils.EMPTY, StringUtils.EMPTY,
                    StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logData);
            rc = ENCReturnCode.ENCCheckMACTACError;
        }

        return rc;
    }

    /**
     * 組檢核香港臨櫃變更PIN BLOCK資料
     *
     * @param method      00-驗證OFFSET, 01-產生OFFSET
     * @param pinBlock    PIN BLOCK
     * @param offset      OLD OFFSET
     * @param keyIdentity KEY DATA
     * @param inputData1  inputData1
     * @param inputData2  inputData2
     * @return FEPReturnCode
     * @throws Exception
     */
    private FEPReturnCode prepareHKAuthData(String method, String pinBlock, String offset, RefString keyIdentity,
                                            RefString inputData1, RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        key.setKeyType1(ENCKeyType.S1);
        key.getKeyId1().setKeyKind(ENCKeyKind.PVK);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());

        key.setKeyType2(DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) ? ENCKeyType.T2 : ENCKeyType.T3);
        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2()
                .setKeySubCode(StringUtils.join(
                        DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) ? SysStatus.getPropertyValue().getSysstatFbkno()
                                : SysStatus.getPropertyValue().getSysstatHbkno(),
                        feptxn.getFeptxnAtmno()));
        ;
        keyIdentity.set(key.toString());

        inputData1.set("");
        inputData2.set("");
        switch (method) {
            case "00":
                // 驗證OFFSET
                inputData1.set(StringUtils.join(method, feptxn.getFeptxnTroutActno().substring(2, 16),
                        feptxn.getFeptxnTrk2().substring(35, 37), "0000", feptxn.getFeptxnTrk2().substring(6, 18),
                        offset));
                inputData2.set(unPack(pinBlock));
                break;
            case "01":
                // 產生OFFSET
                inputData1.set(StringUtils.join(method, feptxn.getFeptxnTroutActno().substring(2, 16),
                        feptxn.getFeptxnTrk2().substring(35, 37), "0000", feptxn.getFeptxnTrk2().substring(6, 18),
                        "0000FFFFFFFF"));
                inputData2.set(unPack(pinBlock));
                break;
        }
        return rc;
    }

    private FEPReturnCode preparePinData(String sscode, String offset, RefString keyIdentity, RefString inputData1,
                                         RefString inputData2) throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(2);
        // spec update by sarah: 信用卡Pin的Keytype為single
        key.setKeyType1(ENCKeyType.S1);
        key.getKeyId1().setKeyKind(ENCKeyKind.PVK);
        key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());

        key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
        key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
        key.getKeyId2()
                .setKeySubCode(StringUtils.join(
                        DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) ? SysStatus.getPropertyValue().getSysstatFbkno()
                                : SysStatus.getPropertyValue().getSysstatHbkno(),
                        feptxn.getFeptxnAtmno()));
        // * 3/3 修改 */
        key.setKeyType2(DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) ? ENCKeyType.T2 : ENCKeyType.T3);
        keyIdentity.set(key.toString());
        // inputData1 = _fepTxn.getFeptxnTroutActno() + "0000" +
        // _fepTxn.getFeptxnTroutActno().Substring(2, 12) + offset;
        if (StringUtils.isBlank(feptxn.getFeptxnZoneCode())) {
            throw new RuntimeException("FEPTXN_ZONE_CODE無值");
        }

        if (ATMZone.TWN.name().equals(feptxn.getFeptxnZoneCode())) {
            // 7/29 修改 for APP
            // 2011-05-09 by kyo 開卡流程新電文
            if (ATMTXCD.APP.name().equals(feptxn.getFeptxnTxCode())
                    || ATMTXCD.PNM.name().equals(feptxn.getFeptxnTxCode())
                    || ATMTXCD.PN0.name().equals(feptxn.getFeptxnTxCode())
                    || ATMTXCD.PNX.name().equals(feptxn.getFeptxnTxCode())) {
                // 2011-09-21 by YIN
                // * 修改 for 卡號發卡 */
                inputData1.set("");
                if (StringUtils.isBlank(feptxn.getFeptxnIcmark())) {
                    /* 帳號發卡 */
                    inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                            feptxn.getFeptxnTrk3().substring(8, 20), offset));
                } else {
                    if (feptxn.getFeptxnIcmark().substring(0, 3)
                            .equals(SysStatus.getPropertyValue().getSysstatHbkno())) { // 卡號發卡
                        inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                                StringUtils.leftPad(String.valueOf(feptxn.getFeptxnCardSeq()), 2, '0'), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), offset));
                    } else { // 帳號發卡
                        // 2012-07-16 Modify by Ruling for PN0電文改抓帳號後12位
                        inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                                feptxn.getFeptxnTroutActno().substring(4, 16), offset));
                        // inputData1 = _fepTxn.getFeptxnTroutActno() + "0000" +
                        // _fepTxn.FEPTXN_TRK3.Substring(8, 12) + offset;
                    }
                }
            } else {
                // 2011-08-18 by YIN 由 CVV判斷發卡方式
                if (Integer.parseInt(feptxn.getFeptxnTrk2().substring(32, 35)) > 0) {
                    inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                            feptxn.getFeptxnTrk2().substring(35, 37), "0000", feptxn.getFeptxnTrk2().substring(6, 18),
                            offset));
                } else {
                    inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno(), "0000",
                            feptxn.getFeptxnTrk2().substring(6, 18), offset));
                }
            }

        } else {
            inputData1.set(StringUtils.join(feptxn.getFeptxnTroutActno().substring(2, 16),
                    feptxn.getFeptxnTrk2().substring(35, 37), "0000", feptxn.getFeptxnTrk2().substring(6, 18), offset));// 海外分行卡
        }

        inputData2.set(unPack(sscode));

        return rc;
    }

    private FEPReturnCode prepareATMTac(int tacType, RefString keyIdentity, RefString inputData1, RefString inputData2)
            throws Exception {
        FEPReturnCode rc = FEPReturnCode.Normal;
        StringBuilder tacInputData = new StringBuilder();
        // Prepare Key Identity
        KeyIdentity key = new KeyIdentity();
        key.setKeyQty(1);
        key.setKeyType1(ENCKeyType.T3);
        // 2014-12-05 Modify by Ruling for PSP TSM
        if (DbHelper.toBoolean(feptxn.getFeptxnFiscFlag()) && StringUtils.isBlank(feptxn.getFeptxnTxCode())) {
            // 原存交易
            switch (feptxn.getFeptxnIcmark().substring(28, 29)) {
                case "3": // 行動金融卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.TWMP);
                    if (feptxn.getFeptxnIcmark().substring(29, 30).equals("3")) {
                        // 1003檔-近端交易
                        // 2014-12-18 Modified By ChenLi for 小額支付限額改成讀資料庫共用參數
                        if (feptxn.getFeptxnTxAmt()
                                .compareTo(new BigDecimal(INBKConfig.getInstance().getINBKSALimit())) > 0) { // 交易金額>
                            // 3000 用C6
                            // Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                        } else { // 交易金額<=3000 用C8 Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C8);
                        }
                    } else {
                        // 1001檔-遠端交易
                        key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    }
                    break;
                case "4": // 感應式金融卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
                    if (feptxn.getFeptxnIcmark().substring(29, 30).equals("3")) {
                        // 1003檔-近端交易
                        // 2014-12-18 Modified By ChenLi for 小額支付限額改成讀資料庫共用參數
                        if (feptxn.getFeptxnTxAmt()
                                .compareTo(new BigDecimal((INBKConfig.getInstance().getINBKSALimit()))) > 0) { // 交易金額>
                            // 3000
                            // 用C6
                            // Key
                            // 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                        } else { // 交易金額<=3000 用C8 Key 驗TAC
                            key.getKeyId1().setKeyFunction(ENCKeyFunction.C8);
                        }
                    } else {
                        // 1001檔-遠端交易
                        key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    }
                    break;
                default: // 一般金融卡、Combo卡
                    key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
                    key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
                    break;
            }
        } else {
            // 自行、代理跨行交易
            key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
            key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
        }
        // key.KeyId1.KeyKind = ENCKeyKind.ICC;
        // key.KeyId1.KeyFunction = ENCKeyFunction.C6;
        key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());
        keyIdentity.set(key.toString());
        // Prepare InputData
        switch (tacType) {
            case 1:
                /* for '2510', '2561', '2562', '2563', '2564' 提款, 全國繳費 */
                /* for 'IWD', 'IFW', 'ACW', 'EFT' 提款, 全國繳費 */
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData
                        .append(StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                tacInputData.append(feptxn.getFeptxnTroutActno());
                break;
            case 2:
                /* for '2521','2522', '2523', '2524', '2525' 轉帳 */
                /* for "IFT" ,"ATF", "BFT","IPA", "IDR" 轉帳/預約轉帳/存款 */
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData
                        .append(StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                tacInputData.append(feptxn.getFeptxnTrinActno());
                tacInputData.append(feptxn.getFeptxnTroutActno());
                break;
            case 3:
                /* for '2541', '2552' 消費扣款, 授權完成 */
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData
                        .append(StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnMerchantId(), 30, ' '));
                tacInputData.append(feptxn.getFeptxnTroutActno());
                break;
            case 4:
                /* for '2551' 預先授權 */
                tacInputData.append(feptxn.getFeptxnIcSeqnoPreauth());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(StringUtils.leftPad(feptxn.getFeptxnTxAmtPreauth().toString(), 12, '0'));
                tacInputData.append("00");
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimePreauth());
                tacInputData.append(feptxn.getFeptxnTroutActno());
                break;

            case 5:
                /* for '2531' , '2532', '2567', '2568' 跨行繳款,自行繳款,全國繳稅 */
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData
                        .append(StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                tacInputData.append(feptxn.getFeptxnPaytype());
                tacInputData.append(feptxn.getFeptxnTroutActno());
                if (!feptxn.getFeptxnPaytype().substring(0, 2).equals("15")) // Not核定繳稅
                {
                    /* 核定繳稅交易已將銷帳編號放入 TRIN_ACTNO, 且右補0 */
                    // tacInputData.Append(_fepTxn.FEPTXN_TRIN_ACTNO.PadRight(16,'0')); /*銷帳編號*/
                    /* 若銷帳編號不滿16位需右補空白, 且長度以16計算 */
                    tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnReconSeqno(), 16, ' '));
                } else {
                    tacInputData.append("00000");
                    // 2011/07/07 modify by Ruling IDNO不滿11位需右補空白, 且長度以11計算
                    tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnIdno(), 11, ' '));
                }
                break;
            case 6:
                /* for '2500' 查詢 */
                /* for "IIQ", "IFE", "AIN" 查詢 */
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTroutActno());
                break;
            case 7:
                // Fly 2018/08/09 for 2555 跨境電子支付
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData.append(
                        StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnCashAmt(), "0.00"), 14, '0'));
                tacInputData.append(feptxn.getFeptxnTroutActno());
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnAtmno(), 8, '0'));
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                tacInputData.append(StringUtils.rightPad(feptxn.getFeptxnMerchantId(), 15, ' '));
                break;
            case 8:
                // Fly 2019/01/03 for 2566 金融帳戶資訊核驗
                tacInputData.append(feptxn.getFeptxnIcSeqno());
                tacInputData.append(feptxn.getFeptxnPcode());
                tacInputData
                        .append(StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
                tacInputData.append(feptxn.getFeptxnTroutActno());
                tacInputData.append(feptxn.getFeptxnAtmChk());
                tacInputData.append(feptxn.getFeptxnTxDatetimeFisc());
                // Fly 2019/01/24 應取34位
                tacInputData.append(feptxn.getFeptxnTrk3().substring(2, 36));
                break;
        }

        // personal information 1 + personal information 2 + personal information 3
        String divData = feptxn.getFeptxnIcmark().substring(0, 16) + feptxn.getFeptxnIcmark().substring(0, 14) + "00"
                + "00000000000000" + feptxn.getFeptxnIcmark().substring(14, 16);
        inputData1.set(tacInputData.toString());
        inputData2.set(divData + unPack(feptxn.getFeptxnIcTac()));
        return rc;
    }

    /**
     * 2021-11-11 Richard add for ATM Gateway
     *
     * @param atmno
     * @param mac
     * @param randomNumber
     * @return
     */
    public FEPReturnCode checkAtmSessionMac(String atmno, String mac, String randomNumber) {
        final String encFunc = "FN000803";
        String keyId1 = StringUtils.EMPTY;
        String inputData1 = randomNumber;
        String inputData2 = mac;
        int encrc = -1;
        FEPReturnCode rc = ENCReturnCode.ENCCheckMACError;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.T3);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.ATM);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), atmno));
                        }
                    });
                }
            };
            // 1T3PPK ATM 12224
            final String keyIdentity = keyId.toString();
            ENCParameter[] encpar = new ENCParameter[]{
                    new ENCParameter() {
                        {
                            setKeyIdentity(keyIdentity);
                            setInputData1(inputData1);
                            setInputData2(inputData2);
                        }
                    }
            };
            encrc = this.encLib(encFunc, "checkAtmSessionMac", encpar, null);
            if (encrc == 0) {
                rc = FEPReturnCode.Normal;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkAtmSessionMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyId1, inputData1, inputData2, StringUtils.EMPTY,
                    StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCLibError;
        }
        return rc;
    }

    /**
     * 2021-11-11 Richard add for ATM Gateway
     * <p>
     * 產生ATM電文訊息押碼(MAC)
     *
     * @param atmno
     * @param mac
     * @return
     */
    public FEPReturnCode makeAtmSessionMac(String atmno, RefString mac) {
        final String encFunc = "FN000802";
        String keyId1 = StringUtils.EMPTY;
        String inputData1 = StringUtils.EMPTY;
        String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.T3);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.ATM);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), atmno));
                        }
                    });
                }
            };
            // 1T3MAC ATM 12224
            final String keyIdentity = keyId.toString();
            ENCParameter[] encpar = new ENCParameter[]{
                    new ENCParameter() {
                        {
                            setKeyIdentity(keyIdentity);
                            setInputData1(inputData1);
                            setInputData2(inputData2);
                        }
                    }
            };
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "makeAtmSessionMac", encpar, outputData);
            if (encrc != 0) {
                rc = ENCReturnCode.ENCMakeMACError;
            } else {
                mac.set(outputData[0]);
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeAtmSessionMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyId1, inputData1, inputData2, StringUtils.EMPTY,
                    StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCLibError;
        }
        return rc;
    }

    /**
     * 2021-11-11 Richard add for ATM Gateway
     * <p>
     * 產生ATM機台連線用的的CD Key CDK_KCV, MAC Key
     *
     * @param atmno
     * @param cdKey
     * @param macKey
     * @return
     */
    public FEPReturnCode makeAtmSessionKey(String atmno, RefString cdKey, RefString macKey) {
        final String encFunc = "FN000801";
        String inputData1 = StringUtils.EMPTY;
        String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.S1);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.PPK);
                            setKeyFunction(ENCKeyFunction.ATM);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), atmno));
                        }
                    });
                }
            };
            // 1T3PPK ATM 12224
            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "makeAtmSessionKey", encpar, outputData);
            if (encrc != 0) {
                rc = ENCReturnCode.ENCLibError;
            } else {
                cdKey.set(outputData[0]);
                macKey.set(outputData[1]);
            }

        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeAtmSessionKey"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCLibError;
        }
        return rc;
    }

    /**
     * 檢核ATM電文的MAC資料
     *
     * @param atmNo
     * @param inputData
     * @param mac
     * @return
     */
    public FEPReturnCode checkAtmMac(String atmNo, String inputData, String mac) {
        final String encFunc = "FN000313";
        String inputData1 = inputData;
        // String inputData2 = this.unPack(mac);
        String inputData2 = mac;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        if (!FEPConfig.getInstance().isCheckAtmMac()) {
            return FEPReturnCode.Normal;
        }
        FEPReturnCode rc = ENCReturnCode.ENCCheckMACError;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.S1);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.ATM);
                            setKeySubCode(atmNo);
                            // setKeySubCode(atmNo);
                        }
                    });
                }
            };
            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData);
            // param.setInputData2(this.unPack(EbcdicConverter.fromHex(CCSID.English,
            // mac)));
            // param.setInputData2(EbcdicConverter.fromHex(CCSID.English, mac));
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "checkAtmMac", encpar, outputData);
            if (encrc == 0) {
                String newmac = outputData[0].substring(0, 16);
                //atm上來mac是ebcdic
                newmac = EbcdicConverter.toHex(CCSID.English, 8, newmac.substring(0, 8));
                this.logContext.setRemark("checkAtmMac newmac:" + newmac + ", oldmac:" + mac);
                logMessage(this.logContext);
                if (newmac.equals(mac))
                    rc = FEPReturnCode.Normal;
                else
                    rc = FEPReturnCode.ENCCheckMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkAtmMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCCheckMACError;
        }
        return rc;
    }

    /**
     * 檢核ATM電文的MAC資料
     *
     * @param atmNo
     * @param inputData
     * @param mac
     * @return
     */
    public FEPReturnCode checkAtmMacNew(String atmNo, String inputData, String mac) {

        final String encFunc = "FN000313";
        String inputData1 = TrimSpace(inputData);
        inputData1 = remainderToF0(inputData1);
        // String inputData2 = this.unPack(mac);
        String inputData2 = mac;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        if (!FEPConfig.getInstance().isCheckAtmMac()) {
            return FEPReturnCode.Normal;
        }
        FEPReturnCode rc = ENCReturnCode.ENCCheckMACError;
        try {
            KeyIdentity keyId;
            if (logData.getChannel().toString().equals("EAT")) {
            	String txdataR = this.txData.getTxRequestMessage();
            	inputData1 = StringUtils.substring(txdataR, 146, 150) + StringUtils.substring(txdataR, 34, 36)
            					+ StringUtils.substring(txdataR, 36, 40) + StringUtils.substring(txdataR, 40, 64)
            					+ StringUtils.substring(txdataR, 598, 612) + StringUtils.substring(txdataR, 326, 358)
            					+ StringUtils.substring(txdataR, 238, 260) + StringUtils.substring(txdataR, 126, 142)
            					+ StringUtils.substring(txdataR, 64, 76) + StringUtils.substring(txdataR, 378, 394)
            					+ StringUtils.substring(txdataR, 198, 230)
            					;
            	 inputData1 = remainderToF0(inputData1);
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.T2);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode("NEATM001");
                                // setKeySubCode(atmNo);
                            }
                        });
                    }
                };
            } else if(logData.getChannel().toString().equals("POS")) {
            	String txdataR = this.txData.getTxRequestMessage();
            	inputData1 = StringUtils.substring(txdataR, 146, 150) + StringUtils.substring(txdataR, 34, 36)
				+ StringUtils.substring(txdataR, 36, 40) + StringUtils.substring(txdataR, 40, 64)
				+ StringUtils.substring(txdataR, 598, 612) + StringUtils.substring(txdataR, 326, 358)
				+ StringUtils.substring(txdataR, 238, 260) + StringUtils.substring(txdataR, 126, 142)
				+ StringUtils.substring(txdataR, 64, 76) + StringUtils.substring(txdataR, 378, 394)
				+ StringUtils.substring(txdataR, 198, 230)
				;
            	inputData1 = remainderToF0(inputData1);
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.T2);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode("POS00001");
                                // setKeySubCode(atmNo);
                            }
                        });
                    }
                };
            } else {
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.S1);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode(atmNo);
                                // setKeySubCode(atmNo);
                            }
                        });
                    }
                };
            }

            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            // param.setInputData2(this.unPack(EbcdicConverter.fromHex(CCSID.English,
            // mac)));
            // param.setInputData2(EbcdicConverter.fromHex(CCSID.English, mac));
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "checkAtmMacNew", encpar, outputData);
            if (encrc == 0) {
                //本機測試open
//                return FEPReturnCode.Normal;
                //-----------------------------

                //本機測試暫時註解，正式環境需open
                String newmac = outputData[0].substring(0, 8);
                this.logContext.setRemark("checkAtmMac newmac:" + newmac + ", oldmac:" + mac);
                logMessage(this.logContext);
                if (newmac.equals(mac))
                    rc = FEPReturnCode.Normal;
                else
                    rc = FEPReturnCode.ENCCheckMACError;
                //-----------------------------

            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkAtmMacNew"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCCheckMACError;
        }
        return rc;
    }

    private String TrimSpace(String data) {
        String result = "";

        for (int i = 0; i < data.length(); i += 2) {

            String tmp = data.substring(i, i + 2);
            //System.out.println(tmp);
            if (i == 0) {
                result += tmp;
            } else {
                if (tmp.equals("40")) {
                    if (!result.substring(result.length() - 2, result.length()).equals("40")) {
                        result += tmp;
                    }
                } else {
                    result += tmp;
                }
            }
        }
        return result;
    }

    private FEPReturnCode makeCbsMacData(String inputData, Boolean isPending, RefString mac) {
        final String encFunc = "FN000313";
        // String inputData1 = "0000000000000000";
        // String inputData2 = inputData;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.T2);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.IMS);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
                        }
                    });
                }
            };
            if (isPending) {
                keyId.getKeyId1().setKeyVersion("00");
            }
            // 1T3PPK ATM 12224
            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData);
            // param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "makeCbsMac", encpar, outputData);
            if (encrc == 0) {
                String newmac = "";
                // 把 16BYTE ASCII 擷取前 8 BYTE
                if (outputData != null && StringUtils.isNotBlank(outputData[0])) {
                    newmac = outputData[0].substring(0, 8);
                }
                mac.set(newmac);
            } else {
                rc = ENCReturnCode.ENCMakeMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeCbsMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData, "",
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    /**
     * 產生主機的MAC資料
     *
     * @param inputData
     * @param mac
     * @return
     */
//    public FEPReturnCode makeCbsMac(String inputData, RefString mac) {
//        return makeCbsMacData(inputData, false, mac);
//    }
//
//    public FEPReturnCode makeCbsMac(String inputData, Boolean isPending, RefString mac) {
//        return makeCbsMacData(inputData, isPending, mac);
//    }
    public FEPReturnCode changeKeyForCbs(String inputData1, String inputData2, RefString newKey, RefString mac) {
        final String encFunc = "FN000901";
        // String inputData1 = inputData;
        // String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(2);
                    setKeyType1(ENCKeyType.T2);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.IMS);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
                        }
                    });
                    setKeyType2(ENCKeyType.T2);
                    setKeyId2(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.CDK);
                            setKeyFunction(ENCKeyFunction.IMS);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
                        }
                    });
                }
            };

            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "changeKeyForCbs", encpar, outputData);
            if (encrc == 0) {
                String keys = outputData[0]; // 新901回2把KEY , 第1把PPK, 第2把TMK
                // String ppk = keys.substring(0, 32);
                String tmk = keys.substring(32, 64);
                newKey.set(tmk);
                mac.set(outputData[1]);
            } else {
                rc = ENCReturnCode.ENCMakeMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".changeKeyForCbs"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, "",
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    public FEPReturnCode changePPKeyForCbs(String ppk, RefString newKey, RefString kek) {
        final String encFunc = "FN000107";
        // String inputData1 = inputData;
        // String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.T2);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.KBPK);
                            setKeyFunction(ENCKeyFunction.IMS);
                            setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
                        }
                    });
                }
            };

            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(ppk);
            param.setInputData2("01");
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "changePPKeyForCbs", encpar, outputData);
            if (encrc == 0) {
                newKey.set(outputData[0]);
                kek.set(outputData[1]);
            } else {
                rc = ENCReturnCode.ENCMakeMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".changePPKeyForCbs"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, ppk, "",
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
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
     * 產生ATM回應電文的MAC資料
     *
     * @param atmNo
     * @param inputData
     * @param mac
     * @return
     */
    public FEPReturnCode makeAtmMac(String atmNo, String inputData, RefString mac) {
        final String encFunc = "FN000313";
        String inputData1 = remainderToF0(inputData);
        String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        
        try {
            KeyIdentity keyId;
            if (txData.getTxChannel() == FEPChannel.ATM) {
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.S1);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode(atmNo);
                            }
                        });
                    }
                };            
            } else {
                String keySubCode;
                if(txData.getTxChannel() == FEPChannel.EAT){
                    keySubCode = "NEATM001";
                }
                else if(txData.getTxChannel() == FEPChannel.POS) {
                    keySubCode = "POS00001";
                }
                else {
                    keySubCode = "";
                }
                
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.T2);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode(keySubCode);
                            }
                        });
                    }
                };
            }

            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            // param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "makeAtmMac", encpar, outputData);
            if (encrc == 0) {
                String newmac = "";
                // 把 16BYTE ASCII 擷取前 8 BYTE
                if (outputData != null && StringUtils.isNotBlank(outputData[0])) {
                    newmac = outputData[0].substring(0, 8);
                }
                mac.set(newmac);
            } else {
                rc = ENCReturnCode.ENCMakeMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeAtmMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }
    
    /**
     * 產生ATM回應電文的MAC資料 三期
     *
     * @param atmNo
     * @param inputData
     * @param mac
     * @return
     */
    public FEPReturnCode makeAtmMacP3(String atmNo, String inputData, RefString mac) {
        final String encFunc = "FN000313";
        String _inputData;
        // "D7D5"= "PN"，"C9" = "I"
        if("ATM".equals(logData.getChannel().toString())) {
        	 if(inputData.substring(18, 22).equals("D7D5") && inputData.substring(60, 62).equals("C9")) {
             	_inputData = inputData.substring(18, 286);
             }else {
             	_inputData = inputData.substring(18, 60);
             }
        }else {
        	_inputData = inputData;
        }
        
        String inputData1 = remainderToF0(_inputData);
        String inputData2 = StringUtils.EMPTY;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity keyId;
            if (logData.getChannel().toString().equals("EAT")) {
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.T2);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode("NEATM001");
                            }
                        });
                    }
                };
            } else if(logData.getChannel().toString().equals("POS")) {
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.T2);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode("POS00001");
                            }
                        });
                    }
                };
            } else {
                keyId = new KeyIdentity() {
                    {
                        setKeyQty(1);
                        setKeyType1(ENCKeyType.S1);
                        setKeyId1(new KeyId() {
                            {
                                setKeyKind(ENCKeyKind.MAC);
                                setKeyFunction(ENCKeyFunction.ATM);
                                setKeySubCode(atmNo);
                            }
                        });
                    }
                };
            }

            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            // param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "makeAtmMac", encpar, outputData);
            if (encrc == 0) {
                String newmac = "";
                // 把 16BYTE ASCII 擷取前 8 BYTE
                if (outputData != null && StringUtils.isNotBlank(outputData[0])) {
                    newmac = outputData[0].substring(0, 8);
                }
                mac.set(newmac);
            } else {
                rc = ENCReturnCode.ENCMakeMACError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".makeAtmMacP3"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCMakeMACError;
        }
        return rc;
    }

    /**
     * ATM與FEP換MAC KEY及PPKEY<br>
     * SPEC：TCB-FEP-SPC_Common_ENCHelper(處理送ENC資料) Version:00.10
     *
     * @param atmNo
     * @param returnData
     * @return
     */
    public FEPReturnCode ChangeKeyForATM(String atmNo, List<String> returnData) {
        final String encFunc = "FN000108";
        String inputData1 = atmNo;
        String inputData2 = StringUtils.EMPTY;
        String keyIdentity = null;
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity key = new KeyIdentity();
            key.setKeyQty(1);
            key.setKeyType1(ENCKeyType.T2);
            key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
            key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
            key.getKeyId1().setKeySubCode(atmNo);
            keyIdentity = key.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(null);
            param.setInputData2(null);
            ENCParameter[] encpar = new ENCParameter[]{param};
            // ArrayList<String> list = new ArrayList<String>();
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "ChangeKeyForATM", encpar, outputData);
            if (encrc == 0) {
                // returnData.add(pack(outputData[0].substring(0, 16))); //single enc PPK
                // returnData.add(pack(outputData[0].substring(16, 32))); //single enc PPK sync
                // returnData.add(pack(outputData[0].substring(32, 80))); //3des PPK
                // returnData.add(pack(outputData[0].substring(80, 96))); //3des PPK sync
                // returnData.add(pack(outputData[0].substring(96, 144))); //3des MAC key
                // returnData.add(pack(outputData[0].substring(144, 160))); //3des MAC key sync
                returnData.add(outputData[0].substring(0, 16)); // single enc PPK
                returnData.add(outputData[0].substring(16, 32)); // single enc PPK sync
                returnData.add(outputData[0].substring(32, 80)); // 3des PPK
                returnData.add(outputData[0].substring(80, 96)); // 3des PPK sync
                // returnData.add(outputData[0].substring(96, 144)); // 3des MAC key
                // returnData.add(outputData[0].substring(144, 160)); // 3des MAC key sync
            } else {
                rc = ENCReturnCode.ENCChangePPKeyError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".ChangeKeyForATM"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCChangePPKeyError;
        }
        return rc;
    }

    public FEPReturnCode ChangeKeyForEATM(String atmNo, List<String> returnData) {
        final String encFunc = "FN000805";
        String inputData1 = atmNo;
        String inputData2 = StringUtils.EMPTY;
        String keyIdentity = null;
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity key = new KeyIdentity();
            key.setKeyQty(1);
            key.setKeyType1(ENCKeyType.T2);
            key.getKeyId1().setKeyKind(ENCKeyKind.TPK);
            key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
            key.getKeyId1().setKeySubCode(atmNo);
            keyIdentity = key.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(null);
            param.setInputData2(null);
            ENCParameter[] encpar = new ENCParameter[]{param};
            // ArrayList<String> list = new ArrayList<String>();
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "ChangeKeyForEATM", encpar, outputData);
            if (encrc == 0) {
                
                returnData.add(outputData[0].substring(0, 32)); // doublie length key
                returnData.add(outputData[0].substring(32, 48)); //key sync
                
            } else {
                rc = ENCReturnCode.ENCChangePPKeyError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".ChangeKeyForEATM"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCChangePPKeyError;
        }
        return rc;
    }

    public FEPReturnCode ChangeInitialKeyForATM(String atmNo, String keyType, List<String> returnData) {
        final String encFunc = "FN000801";
        String inputData1 = atmNo;
        String inputData2 = StringUtils.EMPTY;
        String keyIdentity = null;
        int encrc = -1;
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            KeyIdentity key = new KeyIdentity();
            key.setKeyQty(1);
            if (keyType.equals("M")) {
                key.setKeyType1(ENCKeyType.T2);
                key.getKeyId1().setKeyKind(ENCKeyKind.IMK);
            } else {
                key.setKeyType1(ENCKeyType.S1);
                key.getKeyId1().setKeyKind(ENCKeyKind.ICK);
            }

            key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
            key.getKeyId1().setKeySubCode(atmNo);
            keyIdentity = key.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(null);
            param.setInputData2(null);
            ENCParameter[] encpar = new ENCParameter[]{param};
            // ArrayList<String> list = new ArrayList<String>();
            String[] outputData = new String[]{"", ""};
            encrc = this.encLib(encFunc, "ChangeInitialKeyForATM", encpar, outputData);
            if (encrc == 0) {
                if (keyType.equals("M")) {
                    returnData.add(outputData[0].substring(0, 32)); // triple enc Master Key
                } else {
                    returnData.add(outputData[0].substring(0, 16)); // single des Mac Key
                }

                returnData.add(outputData[1].substring(0, 16)); // single enc Master Key sync or Mac Key Sync

            } else {
                rc = ENCReturnCode.ENCChangePPKeyError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".ChangeInitialKeyForATM"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCChangePPKeyError;
        }
        return rc;
    }
    
    
    /**
     * ATM的PIN換成主機KEY壓的新 PIN
     * @return
     */
	public FEPReturnCode ConvertATMPinToFISC(String pin, RefString newPin) {
    	this.logContext.setRemark("ConvertATMPinToFISC start. pin:" + pin);
		logMessage(this.logContext);
		FEPReturnCode rc = FEPReturnCode.Normal;
		final String encFunc = "FN000902";
		String mode = "02";
		String inputData1 = mode + pin;
		String inputData2 = "00000000";
		
		String keyIdentity = StringUtils.EMPTY;
		int encrc = -1;
		KeyIdentity key = new KeyIdentity();
		try {
			if (StringUtils.equals(feptxn.getFeptxnBkno(), SysStatus.getPropertyValue().getSysstatHbkno())) { //代理行交易
				key.setKeyQty(2);
				// 第一把是 ATM 的PPK
				key.setKeyType1(ENCKeyType.T2);
				key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.IMS);
				key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno() + "IMS");
	
				// 第2把key,FISC 的PPK
				key.setKeyType2(ENCKeyType.T2);
				key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
				//EMV國際卡交易用TR31 KEY
				if (StringUtils.equals("26", feptxn.getFeptxnPcode().substring(0, 2))) {
					// 國際卡及信用卡交易抓取財金KBPK PP Key
					key.getKeyId2().setKeyFunction(ENCKeyFunction.KBPK);
				} else {
					key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
				}
				key.getKeyId2().setKeySubCode(SysStatus.getPropertyValue().getSysstatFbkno());
				keyIdentity = key.toString();
			}

			ENCParameter param = new ENCParameter();
			param.setKeyIdentity(keyIdentity);
			param.setInputData1(inputData1);
			param.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { param };
			String[] outputData = new String[] { StringUtils.EMPTY };
			encrc = this.encLib(encFunc, "ConvertATMPinToFISC", encpar, outputData);
			if (encrc == 0) {
				newPin.set(outputData[0]);
			} else {
				rc = ENCReturnCode.ENCPINBlockConvertError;
			}
		} catch (Exception e) {
             this.logContext.setProgramException(e);
             this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertATMPinToFISC"));
             this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                     StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
             sendEMS(this.logContext);
             rc = ENCReturnCode.ENCChangePPKeyError;
         }
    	 return rc;
    }
	
	public FEPReturnCode ConvertATMPinToFISCForCBS(String pinBlock, String atmNo, RefString newPin) {
    	this.logContext.setRemark("ConvertATMPinToFISCForCBS start. pinBlock:" + pinBlock + ",atmNo=" + atmNo);
		logMessage(this.logContext);
		FEPReturnCode rc = FEPReturnCode.Normal;
		final String encFunc = "FN000902";
		String mode = "02";
		String inputData1 = mode + pinBlock;
		String inputData2 = "00000000";
		
		
		String keyIdentity = StringUtils.EMPTY;
		int encrc = -1;
		KeyIdentity key = new KeyIdentity();
		try {
			key.setKeyQty(2);
			// 第一把是 ATM 的PPK
			key.setKeyType1(ENCKeyType.T2);
			key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
			key.getKeyId1().setKeyFunction(ENCKeyFunction.IMS);
			key.getKeyId1().setKeySubCode(atmNo);

			// 第2把key,FISC 的PPK
			key.setKeyType2(ENCKeyType.T2);
			key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
			String pcode02 = feptxn.getFeptxnPcode().substring(0, 2);
			if (StringUtils.equals("24", pcode02) || StringUtils.equals("26", pcode02)) {
				// 國際卡及信用卡交易抓取財金KBPK PP Key
				key.getKeyId2().setKeyFunction(ENCKeyFunction.KBPK);
			} else {
				key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
			}
			key.getKeyId2().setKeySubCode(SysStatus.getPropertyValue().getSysstatFbkno());
			keyIdentity = key.toString();

			ENCParameter param = new ENCParameter();
			param.setKeyIdentity(keyIdentity);
			param.setInputData1(inputData1);
			param.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { param };
			String[] outputData = new String[] { StringUtils.EMPTY };
			encrc = this.encLib(encFunc, "ConvertATMPinToFISCForCBS", encpar, outputData);
			if (encrc == 0) {
				newPin.set(outputData[0]);
			} else {
				rc = ENCReturnCode.ENCPINBlockConvertError;
			}
		} catch (Exception e) {
             this.logContext.setProgramException(e);
             this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertATMPinToFISCForCBS"));
             this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                     StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
             sendEMS(this.logContext);
             rc = ENCReturnCode.ENCChangePPKeyError;
         }
    	 return rc;
    }

    /**
     * ATMHelper >>>將產生後的新電文, 呼叫makeMac押出mac放回電文
     *
     * @param atmNo
     * @param inputData
     * @return
     */
    public String makeATMMac(String atmNo, String inputData) {
        String newmac = StringUtils.EMPTY;
        final String encFunc = "FN000313";
        String inputData1 = inputData;
        int encrc = -1;
        String keyIdentity = StringUtils.EMPTY;
        FEPReturnCode rc = ENCReturnCode.ENCCheckMACError;
        try {
            KeyIdentity keyId = new KeyIdentity() {
                {
                    setKeyQty(1);
                    setKeyType1(ENCKeyType.S1);
                    setKeyId1(new KeyId() {
                        {
                            setKeyKind(ENCKeyKind.MAC);
                            setKeyFunction(ENCKeyFunction.ATM);
                            setKeySubCode(atmNo);
                            // setKeySubCode(atmNo);
                        }
                    });
                }
            };
            keyIdentity = keyId.toString();
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "checkAtmMac", encpar, outputData);
            if (encrc == 0) {
                newmac = outputData[0].substring(0, 16);
                //atm上來mac是ebcdic
                newmac = EbcdicConverter.toHex(CCSID.English, 8, newmac.substring(0, 8));
                this.logContext.setRemark("checkAtmMac newmac:" + newmac);
                logMessage(this.logContext);
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".checkAtmMac"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, newmac,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
        }
        return newmac;
    }
}
