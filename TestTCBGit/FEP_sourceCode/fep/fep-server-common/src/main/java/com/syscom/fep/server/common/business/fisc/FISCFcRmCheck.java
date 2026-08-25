package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FcmsginExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FcrminExtMapper;
import com.syscom.fep.mybatis.mapper.FcallbankMapper;
import com.syscom.fep.mybatis.mapper.FcrminsnoMapper;
import com.syscom.fep.mybatis.mapper.FcrmstatMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.vo.enums.FISCReturnCode;

public class FISCFcRmCheck extends FISCFcRm{

    protected FISCFcRmCheck(){
        super();
    }
    protected FISCFcRmCheck(FISCData fiscMessage){
        super(fiscMessage);
    }

    /**
     * 負責處理跨行外幣通匯交易狀態檢核
     *
     * @param pcode
     * @param currency
     * @param isSender
     * @return
     */
    public FEPReturnCode checkFCRMStatus(String pcode, String currency, boolean isSender) {
        FEPReturnCode rc_MBOCT = null;
        FEPReturnCode rc_MBACT = null;
        String wk_AOCT = null;
        String wk_MBACT = null;
        FcrmstatMapper fcrmstatMapper = SpringBeanFactoryUtil.getBean(FcrmstatMapper.class);
        Fcrmstat defFCRMSTAT = new Fcrmstat();
        try {
            // 2. *** 依匯出或是匯入之 flag 設定不同 RC 以回應財金
            // 匯出
            if (isSender) {
                // 0203發信單位主機未在跨行作業運作狀態
                rc_MBOCT = FEPReturnCode.SenderBankOperationStop;
                // 0202發信單位該項跨行業務停止或暫停營業
                rc_MBACT = FEPReturnCode.SenderBankServiceStop;
            } else {
                // 0206收信單位主機未在跨行作業運作狀態
                rc_MBOCT = FEPReturnCode.ReceiverBankOperationStop;
                // 0205收信單位該項跨行業務停止或暫停營業
                rc_MBACT = FEPReturnCode.ReceiverBankServiceStop;
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
            defFCRMSTAT.setFcrmstatCurrency(currency);
            if (fcrmstatMapper.selectByPrimaryKey(currency) != null) {
                return FEPReturnCode.FCRMSTATNotFound;
            }
            switch (pcode.substring(0, 2)) {
                case "11":
                    wk_AOCT = defFCRMSTAT.getFcrmstatAoctrm1();
                    wk_MBACT = defFCRMSTAT.getFcrmstatMbactrm1();
                    break;
                case "14":
                    wk_AOCT = defFCRMSTAT.getFcrmstatAoctrm4();
                    wk_MBACT = defFCRMSTAT.getFcrmstatMbactrm4();
                    break;
                default:
                    return CommonReturnCode.Normal;
            }
            // (4) *** 需先 Check 共用子系統 (M-BANK 及 FISC AP STATUS)
            if ("1".equals(pcode.substring(0, 1))) {
                if ("2".equals(defFCRMSTAT.getFcrmstatMbactrm())) {
                    return rc_MBACT;
                }
                if ("2".equals(defFCRMSTAT.getFcrmstatAoctrm())) {
                    return FISCReturnCode.FISCServiceStop;
                }
            }

            // (5) *** Check M-BANK AP status /* 回應 0205 或 0202 */
            if ("2".equals(wk_MBACT)) {
                return rc_MBACT;
            }

            // (6) *** Check FISC AP STATUS /* 回應 0701 */
            if ("2".equals(wk_AOCT)) {
                return FISCReturnCode.FISCServiceStop;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {

            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     負責處理 FISC 外幣匯款跨行交易電文 Basic Check此程式為 AA\RM\1611,1641,1652之副程式

     @param repMAC
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     </history>
     */
    public FEPReturnCode checkFCRMBody(RefString repMAC) {

        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFISCTxData());

        RefString repUNITBANK = new RefString("");
        @SuppressWarnings("unused")
		String sKind = "";

        try {
            //1. 檢核匯款行/解款行是否參加財金或是否營業

            rtnCode = checkFCALLBANK(fiscFCRMReq.getSenderBank(), fiscFCRMReq.getReceiverBank(), repUNITBANK);

            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //2. 檢核財金MAC  & Prepare MAC data
            if ("1611".equals(fiscFCRMReq.getProcessingCode())) {
                sKind = "1"; //CALL encHelper 使用不同Function */
            } else {
                sKind = "2";
            }

            rtnCode = encHelper.checkRMFISCMACAndMakeMAC(repMAC);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     負責處理 FISC 外幣匯款跨行交易電文 Basic Check(對方行檢核)此程式為 AA\RM\1611,1641,1652之副程式

     @param repUNITBANK
     @param wkPEND
     @param autoBack
     @param wkBACK_REASON
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     </history>
     */
    public FEPReturnCode checkFCRMBodyByMbank(String repUNITBANK, String wkPEND, RefBoolean autoBack, RefString wkBACK_REASON) {

        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFISCTxData());

        FcrminsnoMapper dbFCRMINSNO = SpringBeanFactoryUtil.getBean(FcrminsnoMapper.class);
        Fcrminsno defFCRMINSNO = new Fcrminsno();
        boolean NewKEYCheckOK = false;

        try {

            //1. 	檢核對方行MAC & Sync data
            defFCRMINSNO.setFcrminsnoReceiverBank(fiscRMReq.getReceiverBank().substring(0, 3));
            defFCRMINSNO.setFcrminsnoSenderBank(fiscRMReq.getSenderBank().substring(0, 3));
            defFCRMINSNO = dbFCRMINSNO.selectByPrimaryKey(defFCRMINSNO.getFcrminsnoSenderBank(),defFCRMINSNO.getFcrminsnoReceiverBank());
            if (defFCRMINSNO == null) {
                //defFCRMINSNO.FCRMINSNO_CDKEY_FLAG = "0"
            }

            if ("1611".equals(fiscFCRMReq.getProcessingCode())) {
                RefString refCdKeyFlag = new RefString("0");
                RefBoolean refNewKEYCheckOK = new RefBoolean(NewKEYCheckOK);
                rtnCode = encHelper.checkRMMBankMACAndSync(repUNITBANK, autoBack, wkBACK_REASON, refCdKeyFlag, refNewKEYCheckOK);
                NewKEYCheckOK = refNewKEYCheckOK.get();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }

                if (NewKEYCheckOK) {//AP要多執行KEY 生效
                    rtnCode = encHelper.changeRMCDKey(repUNITBANK);
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }

                }
            }



            //2.檢核對方行通匯序號
            if (("1611".equals(fiscRMReq.getProcessingCode()) || "1641".equals(fiscRMReq.getProcessingCode())) && "0".equals(wkPEND)) {

                rtnCode = checkFCRMSeqno(autoBack, wkBACK_REASON);

                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     檢核FCALLBANK並查詢repUNITBANK

     @param sSenderBank
     @param sReceiverBank
     @param repUNITBANK
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     <modify>
     <modifier>Kyo</modifier>
     <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
     <date>2010/03/16</date>
     </modify>
     </history>
     */
    private FEPReturnCode checkFCALLBANK(String sSenderBank, String sReceiverBank, RefString repUNITBANK) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        FcallbankMapper dbFCALLBANK = SpringBeanFactoryUtil.getBean(FcallbankMapper.class);
        Fcallbank defFCALLBANK = new Fcallbank();

        try {
            //I.	以財金電文之SENDER_BANK讀取ALLBANK
            defFCALLBANK.setFcallbankBkno(sSenderBank.substring(0, 3));
            defFCALLBANK.setFcallbankBrno("000");
            defFCALLBANK = dbFCALLBANK.selectByPrimaryKey(defFCALLBANK.getFcallbankCurrency(),defFCALLBANK.getFcallbankBkno(),defFCALLBANK.getFcallbankBrno());
            if (defFCALLBANK != null) {
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscFCRMReq.getTxnDestinationInstituteId().substring(0, 3))) {
                    repUNITBANK.set(defFCALLBANK.getFcallbankUnitBank());
                }
            } else {// 發(收)信單位與發(收)信行代號不符(2106)
                rtnCode = FEPReturnCode.SenderBankWithAcceptCodeNotMatch;
                return rtnCode;
            }

            //II.	以財金電文之RECEIVER_BANK讀取ALLBANK
            defFCALLBANK.setFcallbankBkno(sReceiverBank.substring(0, 3));
            defFCALLBANK.setFcallbankBrno("000");
            defFCALLBANK = dbFCALLBANK.selectByPrimaryKey(defFCALLBANK.getFcallbankCurrency(),defFCALLBANK.getFcallbankBkno(),defFCALLBANK.getFcallbankBrno());
            if (defFCALLBANK != null) {
                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscFCRMReq.getTxnSourceInstituteId().substring(0, 3))) {
                    repUNITBANK.set(defFCALLBANK.getFcallbankUnitBank());
                }
            } else {// 2113 =解款單位無此分行
                rtnCode = FEPReturnCode.ReciverBankNotBranch;
                return rtnCode;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

        return rtnCode;
    }

    /**
     檢核FCRM通匯序號

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
    private FEPReturnCode checkFCRMSeqno(RefBoolean autoBack, RefString backReason) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        FcrminsnoMapper dbFCRMINSNO = SpringBeanFactoryUtil.getBean(FcrminsnoMapper.class);
        Fcrminsno defFCRMINSNO = new Fcrminsno();
        FcrminExtMapper dbFCRMIN = SpringBeanFactoryUtil.getBean(FcrminExtMapper.class);
        Fcrmin defFCRMIN = new Fcrmin();
        FcmsginExtMapper dbFCMSGIN = SpringBeanFactoryUtil.getBean(FcmsginExtMapper.class);
        Fcmsgin defFCMSGIN = new Fcmsgin();
        boolean bCheckRemitNumber = false;

        try {
            //以財金電文之SENDER_BANK[1:3], RECEIVER_BANK[1:3]讀取RMINSNO
            defFCRMINSNO.setFcrminsnoSenderBank(fiscFCRMReq.getSenderBank().substring(0, 3));
            defFCRMINSNO.setFcrminsnoReceiverBank(fiscFCRMReq.getReceiverBank().substring(0, 3));
            defFCRMINSNO = dbFCRMINSNO.selectByPrimaryKey(defFCRMINSNO.getFcrminsnoSenderBank(),defFCRMINSNO.getFcrminsnoReceiverBank());
            if (defFCRMINSNO == null) {
                autoBack.set(true);
                backReason.set("01");
                return rtnCode;
            }

            if ("1611".equals(fiscFCRMReq.getProcessingCode())) {
                defFCRMIN.setFcrminTxdate(CalendarUtil.rocStringToADString("0" + fiscFCRMReq.getTxnInitiateDateAndTime().substring(0, 6)));
                defFCRMIN.setFcrminSenderBank(fiscFCRMReq.getSenderBank());
                defFCRMIN.setFcrminReceiverBank(fiscFCRMReq.getReceiverBank());
                bCheckRemitNumber = dbFCRMIN.checkRemitNumberExist(defFCRMIN.getFcrminTxdate(), defFCRMIN.getFcrminSenderBank(), defFCRMIN.getFcrminReceiverBank()) > 0 ? true : false;
            } else if ("1641".equals(fiscFCRMReq.getProcessingCode())) {
                defFCMSGIN.setFcmsginTxdate(CalendarUtil.rocStringToADString("0" + fiscFCRMReq.getTxnInitiateDateAndTime().substring(0, 6)));
                defFCMSGIN.setFcmsginSenderBank(fiscFCRMReq.getSenderBank());
                defFCMSGIN.setFcmsginReceiverBank(fiscFCRMReq.getReceiverBank());
                bCheckRemitNumber = dbFCMSGIN.checkRemitNumberExist(defFCMSGIN.getFcmsginTxdate(), defFCMSGIN.getFcmsginSenderBank(), defFCMSGIN.getFcmsginReceiverBank()) > 0 ? true : false;
            }

            if (bCheckRemitNumber) {
                return FEPReturnCode.RemitsNumberRepeated;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

        return rtnCode;
    }

    /**
     負責處理外幣匯出資料 Basic Check此程式為 AA1611Out, Service1641之副程式

     @param defFCRMOUT
     @param defFCMSGOUT
     @param repUNITBANK
     @return

     <history>
     <modify>
     <modifier>Maxine</modifier>
     <reason>add</reason>
     <date>2010/5/10</date>
     </modify>
     </history>
     */
    public FEPReturnCode checkFCRMOutData(Fcrmout defFCRMOUT, Fcmsgout defFCMSGOUT, RefString repUNITBANK) {
        FEPReturnCode rtnCode = null;
        try {
            if ("111,113,117,119".indexOf(defFCRMOUT.getFcrmoutRemtype()) > 0) {
                rtnCode = checkFCALLBANK(defFCRMOUT.getFcrmoutSenderBank(), defFCRMOUT.getFcrmoutReceiverBank(), repUNITBANK);
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            } else if (defFCMSGOUT != null) {
                rtnCode = checkFCALLBANK(defFCMSGOUT.getFcmsgoutSenderBank(), defFCMSGOUT.getFcmsgoutReceiverBank(), repUNITBANK);
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }
            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            if (getFISCTxData().getMessageFlowType() == MessageFlow.Request) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

}
