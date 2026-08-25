package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.ims.AB_TR_EAI_I001;
import com.syscom.fep.vo.text.ims.AB_TR_EAI_O001;
import com.syscom.fep.vo.text.ims.AB_TR_O001;
import com.syscom.fep.vo.text.ims.CB_TR_I001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;

public class ABTREAII001 extends ACBSAction {

    public ABTREAII001(MessageBase txType) {
        super(txType, new AB_TR_EAI_O001());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
//    private EaitxnMapper eaitxnMapper = SpringBeanFactoryUtil.getBean(EaitxnMapper.class);

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(CB_TR_I001)
        *HCE(cbsTita, txType)  呼叫同一支程式中的Methed
        */

        //2025/7/9 特殊電文追加  HCE
        if ("HCA".equals(feptxn.getFeptxnChannel()) || "HCE".equals(feptxn.getFeptxnChannel())) {
            CB_TR_I001 cbsTita = new CB_TR_I001();
            buildHCE(cbsTita, txType);
            feptxntcb.setFeptxntcbCardfmt(cbsTita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        } else {
            // Header
            AB_TR_EAI_I001 cbsTita = new AB_TR_EAI_I001();
            cbsTita.setIMS_TRANS(TxHelper.getIMSTRANSString(feptxn.getFeptxnChannel(), getLogContext().getMessageId()));
//            cbsTita.setIMS_TRANS("MFEPEA00");
            cbsTita.setSYSCODE("FEP");
            cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
            cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));

            //TXN FLOW
            if (0 == feptxn.getFeptxnFiscFlag()) {
                cbsTita.setTXN_FLOW("C"); // 自行
            } else {
                cbsTita.setTXN_FLOW("A"); // 代理
            }

            cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
            cbsTita.setPCODE(feptxn.getFeptxnPcode());
            cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());

            //PROCESS_TYPE
            if ("0".equals(txType)) { //查詢、檢核
                cbsTita.setPROCESS_TYPE("CHK");
            } else if ("1".equals(txType)) { //入扣帳
                cbsTita.setPROCESS_TYPE("ACCT");
                //帳戶補充資訊
                cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());  //財金response
            } else if ("2".equals(txType)) { //沖正
                cbsTita.setPROCESS_TYPE("RVS");
            } else if ("4".equals(txType)) { //解除優惠
                cbsTita.setPROCESS_TYPE("REL");
            }

            String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);//轉民國

            cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
            cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
            cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
            cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
            cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
            cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());

            if (feptxn.getFeptxnRepRc()!=null && StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
                cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
            } else {
                cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
            }

            //Detail
            cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());

            if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
                cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
            } else {
                cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
            }
            if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
                cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
            } else {
                cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
            }

            if (StringUtils.isNotBlank(feptxn.getFeptxnChrem())) {
                String txmemo = feptxn.getFeptxnChrem();
                String resizeTxmemo = "";
                this.getLogContext().setRemark("txmemo原始:>" + txmemo + "<");
                logMessage(this.logContext);
                //含全形字就全轉全形
                if(StringUtil.containsFullWidth(txmemo)){
                    txmemo = StringUtil.convertToFullwidth(txmemo);
                    this.getLogContext().setRemark("txmemo全轉全形後:>" + txmemo + "<");
                    logMessage(this.logContext);
                    //轉EBCDIC後函0E0F長度必須是80，所以以中文字計算最多19個中文字
                    resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 19);
                }else{
                    //都半形，長度放寬到40個半形字
                    resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 40);
                }
                //轉EBCDIC後不足長度補足空白Hex"40"
                String resizeTxmemoAfterChangeEbcdic = EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTxmemo, 80);
                cbsTita.setTXMEMO(resizeTxmemoAfterChangeEbcdic);  /*若是Unicode轉成EBCDIC*/ //前後已經包好0E0F
                this.logContext.setRemark("TXMEMO(getCbsTita)>" + resizeTxmemo + "<>" + resizeTxmemoAfterChangeEbcdic + "<");
                logMessage(this.logContext);
//            cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());
            }else{
                String Txmemo = StringUtils.rightPad("", 80, "40");
                cbsTita.setTXMEMO(Txmemo);
                this.logContext.setRemark(">" + Txmemo+ "<");
                logMessage(this.logContext);
            }

            RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getNBRequest().getBody().getRq().getSvcRq();
            if ("1".equals(txType) && feptxn.getFeptxnFiscFlag() != null && feptxn.getFeptxnFiscFlag() == 1) {
                cbsTita.setMSG_CAT("10");
            } else {
                cbsTita.setMSG_CAT(tita.getTXNTYPE().trim());
            }
            /*無卡交易，ICMEMO、ICTAC 放入空白*/
//                Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

            cbsTita.setCARDTYPE("N"); // 交易卡片型態
            feptxntcb.setFeptxntcbCardfmt(cbsTita.getCARDTYPE()); // 紀錄在FEPTXNTCB
            cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
            cbsTita.setTXNICCTAC("40404040404040404040");
            cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());

            //轉帳手續費(各管道客戶手續費/優惠後手續費)
            cbsTita.setCHANNEL_CHARGE(feptxn.getFeptxnFeeCustpay());
            //轉出帳號統編
            cbsTita.setAE_TRNSFROUTIDNO(feptxn.getFeptxnIdno());
            //限額累計註記
            cbsTita.setAE_TRNSFLAG(tita.getTRNSFLAG());
            //業務類別
            cbsTita.setAE_BUSINESSTYPE(tita.getBUSINESSTYPE());
            /* 手續費負擔別*/
            cbsTita.setAE_AEIEFEET(tita.getFEEPAYMENTTYPE());

            if (StringUtils.isNotBlank(feptxn.getFeptxnPsbremFD())) {
                String trnsfrOutNote = feptxn.getFeptxnPsbremFD();
                String resizeTrnsfroutnote = "";
                this.getLogContext().setRemark("trnsfrOutNote原始:>" + trnsfrOutNote + "<");
                logMessage(this.logContext);
                //含全形字就全轉全形
                if(StringUtil.containsFullWidth(trnsfrOutNote)) {
                    trnsfrOutNote = StringUtil.convertToFullwidth(trnsfrOutNote);
                    this.getLogContext().setRemark("trnsfrOutNote全轉全形:>" + trnsfrOutNote + "<");
                    logMessage(this.logContext);
                    //轉EBCDIC後函0E0F長度必須是36，所以以中文字計算最多8個中文字
                    resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 8);
                }else{
                    //都半形，長度放寬到18個半形字
                    resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 18);
                }
                //轉EBCDIC後不足長度補足空白Hex"40"
                String resizeTrnsfroutnoteAfterChangeEbcdic = EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTrnsfroutnote, 36);
                cbsTita.setAE_TRNSFROUTNOTE(resizeTrnsfroutnoteAfterChangeEbcdic);
                this.logContext.setRemark("AE_TRNSFROUTNOTE>" + resizeTrnsfroutnote + "<>" + resizeTrnsfroutnoteAfterChangeEbcdic + "<");
                logMessage(this.logContext);
            }else{
                String trnsfroutnote = StringUtils.rightPad("", 36, "40");
                cbsTita.setAE_TRNSFROUTNOTE(trnsfroutnote);
                this.logContext.setRemark(">" + trnsfroutnote+ "<");
                logMessage(this.logContext);
            }
            //安控機制
            cbsTita.setAE_AEISLLTY(tita.getSSLTYPE());
            //限額種類
            cbsTita.setAE_LIMITTYPE(tita.getLIMITTYPE());
            //傳真手續費
            cbsTita.setAE_AEIFIXFE(tita.getFAXFEE()==null?"00":StringUtils.leftPad(String.valueOf(tita.getFAXFEE()),2,"0"));
            //跨網手續費
            cbsTita.setAE_AEINETFE(tita.getTRANSFEE()==null?"00":StringUtils.leftPad(String.valueOf(tita.getTRANSFEE()),2,"0"));
            //EDI應付他行手續費
            cbsTita.setAE_AEIEDIFE(tita.getOTHERBANKFEE());
            //轉出存戶性質別
            cbsTita.setAE_AEICIRCU(tita.getCUSTCODE());
            //手續費優惠類別
            cbsTita.setAE_AEIDSTYP(feptxntcb.getFeptxntcbOvfeety());
            //手續費優惠主辦分行
            cbsTita.setAE_AEIDSBRH(feptxntcb.getFeptxntcbChafeeBranch());
            //手續費優惠金額
            cbsTita.setAE_AEIDSCRG(StringUtils.leftPad(String.valueOf(feptxntcb.getFeptxntcbChafeeamt()==null?"00":feptxntcb.getFeptxntcbChafeeamt()),2,"0"));
            //實際轉出金額
            cbsTita.setAE_TRANS_AMT_OUT(feptxn.getFeptxnTxAmt());
            //轉出帳號清算分行
            cbsTita.setAE_FMMBR(feptxn.getFeptxnBrno());
            //特殊轉帳類別註記
            cbsTita.setTR_SPECIAL_FLAG(tita.getTRANSTYPEFLAG());
            //轉出帳號
            cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
            //轉入帳號
            cbsTita.setTOACT(feptxn.getFeptxnTrinActno());

            this.setoTita(cbsTita);
            this.setTitaToString(cbsTita.makeMessage());
            this.setASCIItitaToString(cbsTita.makeMessageAscii());
        }

        return FEPReturnCode.Normal;

    }

    /**
     * 組合HCE
     *
     * @param cbsTita
     * @return
     * @throws Exception
     */
    private CB_TR_I001 buildHCE(CB_TR_I001 cbsTita, String txType) throws Exception {
//        cbsTita.setIMS_TRANS("MFEPEA00");
        cbsTita.setIMS_TRANS(TxHelper.getIMSTRANSString(feptxn.getFeptxnChannel(), getLogContext().getMessageId()));
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));

        //TXN FLOW
        if (0 == feptxn.getFeptxnFiscFlag()) {
            cbsTita.setTXN_FLOW("C"); // 自行
        } else {
            cbsTita.setTXN_FLOW("A"); // 代理
        }

        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());

        //PROCESS_TYPE
        if ("0".equals(txType)) { //查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
        } else if ("1".equals(txType)) { //入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
            //帳戶補充資訊
            cbsTita.setI_ACT(feptxn.getFeptxnAcctSup()); //財金response
        } else if ("2".equals(txType)) { //沖正
            cbsTita.setPROCESS_TYPE("RVS");
        }

        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc); //轉民國年

        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());

        /*第一道圈存為 “0000”，財金回覆後，上送 FEPTXN_REP_RC，扣帳或解圈 */
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        //Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());

        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
        } else {
            cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        }
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
        } else {
            cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        }

        if (StringUtils.isNotBlank(feptxn.getFeptxnChrem())) {
            String txmemo = feptxn.getFeptxnChrem();
            String resizeTxmemo = "";
            this.getLogContext().setRemark("txmemo初始:>" + txmemo + "<");
            logMessage(this.logContext);
            //含全形字就全轉全形
            if(StringUtil.containsFullWidth(txmemo)){
                txmemo = StringUtil.convertToFullwidth(txmemo);
                this.getLogContext().setRemark("txmemo全轉全形後:>" + txmemo + "<");
                logMessage(this.logContext);
                //轉EBCDIC後函0E0F長度必須是80，所以以中文字計算最多19個中文字
                resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 19);
            }else{
                //都半形，長度放寬到40個半形字
                resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 40);
            }
            //轉EBCDIC後不足長度補足空白Hex"40"
            String resizeTxmemoAfterChangeEbcdic = EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTxmemo, 80);
            cbsTita.setTXMEMO(resizeTxmemoAfterChangeEbcdic);  /*若是Unicode轉成EBCDIC*/ //前後已經包好0E0F
            this.logContext.setRemark("TXMEMO(buildHCE)>" + resizeTxmemo + "<>" + resizeTxmemoAfterChangeEbcdic + "<");
            logMessage(this.logContext);
//            cbsTita.setI_ACT(feptxn.getFeptxnAcctSup());
        }else{
            String Txmemo = StringUtils.rightPad("", 80, "40");
            cbsTita.setTXMEMO(Txmemo);
            this.logContext.setRemark(">" + Txmemo+ "<");
            logMessage(this.logContext);
        }

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getHCERequest().getBody().getRq().getSvcRq();
        if ("1".equals(txType) && feptxn.getFeptxnFiscFlag() != null && feptxn.getFeptxnFiscFlag() == 1) {
            cbsTita.setMSG_CAT("10");
        } else {
            cbsTita.setMSG_CAT(tita.getTXNTYPE().trim());
        }

        cbsTita.setCARDTYPE("K"); // 交易卡片型態

        if (StringUtils.isNotBlank(tita.getICMARK())) {
            cbsTita.setICMEMO(tita.getICMARK());
        } else {
            cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        }

        // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno()) || "K".equals(cbsTita.getCARDTYPE()) || "RQ".equals(tita.getTXNTYPE())) { //第一次上送CBS
            cbsTita.setTXNICCTAC(tita.getIC_TAC_LEN() + tita.getIC_TAC().substring(0, 16)); //LL+DATA
            cbsTita.setTERMTXN_DATETIME(tita.getIC_TAC_DATE() + tita.getIC_TAC_TIME());
        } else {
            cbsTita.setTXNICCTAC("40404040404040404040");
        }

        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        cbsTita.setTR_SPECIAL_FLAG(tita.getTRANSTYPEFLAG());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());   //轉出帳號
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());      //轉入帳號

//      Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());

        return cbsTita;
    }

    /**
     * 拆解CBS回應電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* HCA交易電文內容格式請參照TOTA電文格式(AB_TR_O001) 2026-07-15 修正*/
        /* 拆解主機回應電文 */
        FEPReturnCode rtnCode;
        if ("HCA".equals(feptxn.getFeptxnChannel()) || "HCE".equals(feptxn.getFeptxnChannel())) {
            AB_TR_O001 tota = new AB_TR_O001();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            /* 更新交易 */
            rtnCode = this.updateFEPTxnHCE(tota);
        } else {
        /* 其他外圍通道電文內容格式請參照TOTA電文格式(AB_TR_EAI_O001) */
        /* 拆解主機回應電文 */
            AB_TR_EAI_O001 tota = new AB_TR_EAI_O001();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            /* 更新交易 */
            rtnCode = this.updateFEPTxn(tota);
        }

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * HCE更新交易, AB_TR_O001
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxnHCE(AB_TR_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
//        String IMS_ERRPGM = this.getImsPropertiesValue(cbsTota, ImsMethodName.IMS_ERRPGM.getValue());
//        if (StringUtils.isNotBlank(IMS_ERRPGM)) {
//            feptxn.setFeptxnErrMsg(IMS_ERRPGM);
//        }
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "4001" 表交易成功

        rtnCode = other_prepareUpdateIMSData(cbsTota);
        if(rtnCode != FEPReturnCode.Normal) {
            // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            getLogContext().setRemark("UPDATE FEPTXN ERROR");
            sendEMS(getLogContext());
        } else if(!StringUtils.equals(cbsTota.getIMSRC_TCB(), "000")
                || (!StringUtils.equalsAny(cbsTota.getIMSRC4_FISC(), "4001", "4002", "4007") && StringUtils.isNotEmpty(cbsTota.getIMSRC4_FISC()))) {
            rtnCode = FEPReturnCode.CBSCheckError;
        }

        //帳戶餘額
        if (cbsTota.getACTBALANCE() != null && cbsTota.getACTBALANCE().signum() != 0) {
            feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
        }
           //可用餘額
        if (cbsTota.getAVAILABLE_BALANCE() != null && cbsTota.getAVAILABLE_BALANCE().signum() != 0) {
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

//        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        try {
            int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
            int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

            if (insertCount <= 0 || insertCount2 <= 0) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                transactionManager.rollback(txStatus);
            }else {
                transactionManager.commit(txStatus);
            }
        } catch (Exception ex){
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }

        return rtnCode;
    }

    /**
     * 更新交易,AB_TR_EAI_O001
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxn(AB_TR_EAI_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
//        String IMS_ERRPGM = this.getImsPropertiesValue(cbsTota, ImsMethodName.IMS_ERRPGM.getValue());
//        if (StringUtils.isNotBlank(IMS_ERRPGM)) {
//            feptxn.setFeptxnErrMsg(IMS_ERRPGM);
//        }
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "4001" 表交易成功

        rtnCode = other_prepareUpdateIMSData(cbsTota);
        if(rtnCode != FEPReturnCode.Normal) {
            // 將 ERROR MSG 送 EVENT MONITOR SYSTEM
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            getLogContext().setRemark("UPDATE FEPTXN ERROR");
            sendEMS(getLogContext());
        } else if(!StringUtils.equals(cbsTota.getIMSRC_TCB(), "000")
                || (!StringUtils.equalsAny(cbsTota.getIMSRC4_FISC(), "4001", "4002", "4007") && StringUtils.isNotEmpty(cbsTota.getIMSRC4_FISC()))) {
            rtnCode = FEPReturnCode.CBSCheckError;
        }

        // 2026.2.5 帳戶餘額、實際轉出金額(含手續費)，一扣多入(FSCODE：TG)無此值
        if ( !"TG".equals(feptxn.getFeptxnTxCode().substring(0, 2))) {
            //帳戶餘額
            if (cbsTota.getACTBALANCE() != null && cbsTota.getACTBALANCE().signum() != 0) {
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }
            //可用餘額
            if (cbsTota.getAVAILABLE_BALANCE() != null && cbsTota.getAVAILABLE_BALANCE().signum() != 0) {
                feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            }

            if (cbsTota.getTRANSAMTOUT() != null
                    && StringUtils.isNotBlank(cbsTota.getTRANSAMTOUT().toString())) {
                feptxn.setFeptxnTxAmtAct(cbsTota.getTRANSAMTOUT());
            }

            // 轉出帳號帳務行代號
            if (StringUtils.isNotBlank(cbsTota.getAEHOBRMR())) {
                feptxntcb.setFeptxntcbAehobrmr(cbsTota.getAEHOBRMR());
            }
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

//        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        try {
            int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
            int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

            if (insertCount <= 0 || insertCount2 <= 0) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                transactionManager.rollback(txStatus);
            }else {
                transactionManager.commit(txStatus);
            }
        } catch (Exception ex){
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }

        return rtnCode;
    }

    /**
     * 新增交易通知
     *
     * @param cbsTota
     * @throws ParseException
     */
    private void insertSMSMSG(AB_TR_EAI_O001 cbsTota) throws ParseException {
//        Smsmsg defSMSMSG = new Smsmsg();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(this.feptxn.getFeptxnTxDate(), this.feptxn.getFeptxnEjfno());
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        // 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//        if (smsmsg == null) {
//            defSMSMSG.setSmsmsgTxDate(this.feptxn.getFeptxnTxDate());
//            defSMSMSG.setSmsmsgEjfno(this.feptxn.getFeptxnEjfno());
//            defSMSMSG.setSmsmsgStan(this.feptxn.getFeptxnStan());
//            defSMSMSG.setSmsmsgPcode(this.feptxn.getFeptxnPcode());
//            defSMSMSG.setSmsmsgTroutActno(this.feptxn.getFeptxnTroutActno());
//            defSMSMSG.setSmsmsgTxTime(this.feptxn.getFeptxnTxTime());
//            defSMSMSG.setSmsmsgZone("TWN");
//            defSMSMSG.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            defSMSMSG.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            defSMSMSG.setSmsmsgTxCur(this.feptxn.getFeptxnTxCur());
//            defSMSMSG.setSmsmsgTxAmt(this.feptxn.getFeptxnTxAmt());
//            defSMSMSG.setSmsmsgTxCurAct(this.feptxn.getFeptxnTxCurAct());
//            defSMSMSG.setSmsmsgTxAmtAct(this.feptxn.getFeptxnTxAmtAct());
//            defSMSMSG.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            defSMSMSG.setSmsmsgNotifyFg("Y");
//            defSMSMSG.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//            defSMSMSG.setSmsmsgChannel(this.feptxn.getFeptxnChannel());
//            Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//            defSMSMSG.setUpdateTime(datenow);
//            defSMSMSG.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//            if (smsmsgExtMapper.insert(defSMSMSG) <= 0) {
//                getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//                this.logMessage(getLogContext());
//            }
//        }
    }

    public Object getInstanceObject(String cbsProcessName, MessageBase atmData) {
        Class<?> c = null;
        //java.lang.reflect.Field[] fields = null;
        //String imsPackageName = "";
        //Class<?> imsClassName = null;
        Class<?>[] params = {MessageBase.class};
        Object o = null;
        try {
            //獲得cbsProcessn物件名稱
            c = Class.forName("com.syscom.fep.server.common.cbsprocess." + cbsProcessName);
            //獲得cbsProcessn物件裡所有欄位名稱
            //fields = c.getDeclaredFields();
            //使用cbsProcessn以獲得ims電文物件名稱
            //imsPackageName = fields[0].toString().split(" ")[1];
            //獲得ims電文物件名稱
            //imsClassName = Class.forName(imsPackageName);
            //獲得ims實例化電文物件
            o = c.getConstructor(params).newInstance(atmData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return o;
    }
}
