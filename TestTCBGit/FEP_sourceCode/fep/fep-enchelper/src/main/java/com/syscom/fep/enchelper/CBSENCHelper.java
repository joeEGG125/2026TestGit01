package com.syscom.fep.enchelper;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.enchelper.vo.KeyId;
import com.syscom.fep.enchelper.vo.KeyIdentity;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Feptxn;
import org.apache.commons.lang3.StringUtils;

public class CBSENCHelper extends CommonENCHelper {

    protected CBSENCHelper(Feptxn feptxn, MessageBase txData) {
        super(feptxn, txData);
    }

    protected CBSENCHelper(Feptxn feptxn) {
        super(feptxn);
    }

    protected CBSENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq,
                           MessageBase txData) {
        super(msgId, channel, subsys, ej, atmno, atmseq, txData);
    }

    protected CBSENCHelper(FISCData txData) {
        super(txData);
    }

    /**
     * For TSM
     *
     * @param msgId
     * @param ej
     */
    protected CBSENCHelper(String msgId, int ej) {
        super(msgId, ej);
    }

    /**
     * 2022-07-28 Richard add
     *
     * @param txData
     */
    public CBSENCHelper(MessageBase txData) {
        super(txData);
    }

    /**
     * 產生主機的MAC資料
     *
     * @param inputData
     * @param mac
     * @return
     */
    public FEPReturnCode makeCbsMacNew(String inputData, RefString mac) {
        return makeCbsMacData(inputData, false, mac);
    }

    public FEPReturnCode makeCbsMac(String inputData, Boolean isPending, RefString mac) {
        return makeCbsMacData(inputData, isPending, mac);
    }

    private FEPReturnCode makeCbsMacData(String inputData, Boolean isPending, RefString mac) {
        final String encFunc = "FN000313";
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
                rc = ENCReturnCode.ENCLibError;
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
     * 將ATM送上來的Pin用主機的KEY轉換成給主機的Pin<br>
     * SPEC：TCB-FEP-SPC_Common_ENCHelper(處理送ENC資料) Version:00.10
     * 二期用
     *
     * @param keyType
     * @param atmNo
     * @param pin
     * @param newPin
     * @return
     */
    public FEPReturnCode ConvertATMPinToIMSforP1(ENCKeyType keyType, String mode, String atmNo, String atmSeqNo, String accno, String pin, RefString newPin) throws Exception {
        final String encFunc = "FN000902";
        //String inputData = this.unPack(pin);
        //mode + pinblock + atmseqno
        String inputData1 = mode + pin;
        String inputData2 = atmSeqNo;
        if (mode.equals("01") || mode.equals("03")) {
            inputData2 += accno;
        }
        String keyIdentity = StringUtils.EMPTY;
        int encrc = -1;
        KeyIdentity key = new KeyIdentity();
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            key.setKeyQty(3);
            if (mode == "02") {  //IMS to ATM
                //第一把是IMS的PPK
                key.setKeyType1(ENCKeyType.T2);
                key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId1().setKeyFunction(ENCKeyFunction.IMS);
                key.getKeyId1().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));

                //第2把key,ATM的PPK
                key.setKeyType2(keyType);
                key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
                key.getKeyId2().setKeySubCode(atmNo);
            } else {
                //第一把是ATM的PPK
                key.setKeyType1(keyType);
                key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
                key.getKeyId1().setKeySubCode(atmNo);
                //第2把key, IMS的PPK
                key.setKeyType2(ENCKeyType.T2);
                key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId2().setKeyFunction(ENCKeyFunction.IMS);
                key.getKeyId2().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
            }
            //第3把key, ATM的MAC KEY
            key.setKeyType3(ENCKeyType.S1);
            key.getKeyId3().setKeyKind(ENCKeyKind.MAC);
            key.getKeyId3().setKeyFunction(ENCKeyFunction.ATM);
            key.getKeyId3().setKeySubCode(atmNo);
            keyIdentity = key.toString();
            
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "ConvertATMPinToIMSforP1", encpar, outputData);
            if (encrc == 0) {
                newPin.set(outputData[0]);
            } else {
                rc = ENCReturnCode.ENCPINBlockConvertError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertATMPinToIMSforP1"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCPINBlockConvertError;
        }
        return rc;
    }
    
    /**
     * 將ATM送上來的Pin用主機的KEY轉換成給主機的Pin<br>
     * 三期用
     *
     * @param keyType
     * @param atmNo
     * @param pin
     * @param newPin
     * @return
     */
    public FEPReturnCode ConvertATMPinToIMS(ENCKeyType keyType, String mode, String atmNo, String atmSeqNo, String accno, String pin, RefString newPin) throws Exception {
    	this.logContext.setRemark("ConvertATMPinToIMS start. keyType:" + keyType + ",pin:" + pin + ",accno=" + accno + ",atmSeqNo=" + atmSeqNo + ",mode=" + mode);
		logMessage(this.logContext);
    	final String encFunc = "FN000902";
        String inputData1 = mode + pin;
        String inputData2 = atmSeqNo;
        if (mode.equals("01") || mode.equals("03")) {
            inputData2 += accno;
        }
        String keyIdentity = StringUtils.EMPTY;
        int encrc = -1;
        KeyIdentity key = new KeyIdentity();
        FEPReturnCode rc = FEPReturnCode.Normal;
        try {
            key.setKeyQty(3);
            if (mode == "02") {  //IMS to ATM
                //第一把是IMS的PPK
                key.setKeyType1(ENCKeyType.T2);
                key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId1().setKeyFunction(ENCKeyFunction.IMS);
                key.getKeyId1().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));

                //第2把key,ATM的PPK
                key.setKeyType2(keyType);
                key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
                key.getKeyId2().setKeySubCode(atmNo);
            } else {
                //第一把是ATM的PPK
                key.setKeyType1(keyType);
                key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
                key.getKeyId1().setKeySubCode(atmNo);
                //第2把key, IMS的PPK
                key.setKeyType2(ENCKeyType.T2);
                key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
                key.getKeyId2().setKeyFunction(ENCKeyFunction.IMS);
                key.getKeyId2().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));
            }
            //第3把key, ATM的MAC KEY
            key.setKeyType3(ENCKeyType.S1);
            key.getKeyId3().setKeyKind(ENCKeyKind.MAC);
            key.getKeyId3().setKeyFunction(ENCKeyFunction.ATM);
            key.getKeyId3().setKeySubCode(atmNo);
            keyIdentity = key.toString();
            
            ENCParameter param = new ENCParameter();
            param.setKeyIdentity(keyIdentity);
            param.setInputData1(inputData1);
            param.setInputData2(inputData2);
            ENCParameter[] encpar = new ENCParameter[]{param};
            String[] outputData = new String[]{StringUtils.EMPTY};
            encrc = this.encLib(encFunc, "ConvertATMPinToIMS", encpar, outputData);
            if (encrc == 0) {
                newPin.set(outputData[0]);
            } else {
                rc = ENCReturnCode.ENCPINBlockConvertError;
            }
        } catch (Exception e) {
            this.logContext.setProgramException(e);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertATMPinToIMS"));
            this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
                    StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
            sendEMS(this.logContext);
            rc = ENCReturnCode.ENCPINBlockConvertError;
        }
        return rc;
    }
}
