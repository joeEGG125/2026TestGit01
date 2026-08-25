package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.AB_XO_I001;
import com.syscom.fep.vo.text.ims.AB_XO_O001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Calendar;

public class ABXOI001 extends ACBSAction {

    public ABXOI001(MessageBase txData) {
        super(txData, new AB_XO_O001());
    }


    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(AB_XO_I001)*/
        // Header
        AB_XO_I001 cbsTita = new AB_XO_I001();
        cbsTita.setIMS_TRANS("MFEPFXML");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        cbsTita.setTXN_FLOW("A"); // 付款方
        cbsTita.setMSG_CAT("10"); // FISC MesaageType後兩碼
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        cbsTita.setPROCESS_TYPE("ACCT"); //帳務
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
        cbsTita.setCARDTYPE("N"); // 交易卡片型態(網銀)
        feptxntcb.setFeptxntcbCardfmt(cbsTita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        } else {
            cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        }

        // Detail
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita = this.getNBRequest().getBody().getRq().getSvcRq();

        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); //FEP給固定值 "12345678"
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
        cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        cbsTita.setTXNICCTAC("40404040404040404040");
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmtAct());
        //轉出帳號
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());
        cbsTita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
        cbsTita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
        //轉入帳號
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno());

        /* 收款人姓名中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnToName初始 >"+ feptxn.getFeptxnToName() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnToName())) {
            String toname = feptxn.getFeptxnToName();
            String resizeToname = "";
            // 含全形字就全轉全形
            if (StringUtil.containsFullWidth(toname)) {
                toname = StringUtil.convertToFullwidth(toname);
                // 轉EBCDIC後含0E0F長度必須是320，所以以中文字計算最多79個中文字
                resizeToname = StringUtil.adjustChineseStringLength(toname, 79);
            } else {
                // 都半形，長度放寬到80個半形字
                resizeToname = StringUtil.adjustChineseStringLength(toname, 80);
            }

            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setTONAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeToname,160) );
        }else{
            cbsTita.setTONAME(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("TONAME	>" + cbsTita.getTONAME() + "<");
        logMessage(this.logContext);

        /* 付款人姓名中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnFromName初始 >"+ feptxn.getFeptxnFromName() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnFromName())) {
            String fromname = feptxn.getFeptxnFromName();
            String resizeFromname = "";
            // 含全形字就全轉全形
            if (StringUtil.containsFullWidth(fromname)) {
                fromname = StringUtil.convertToFullwidth(fromname);
                // 轉EBCDIC後含0E0F長度必須是320，所以以中文字計算最多79個中文字
                resizeFromname = StringUtil.adjustChineseStringLength(fromname, 79);
            } else {
                // 都半形，長度放寬到80個半形字
                resizeFromname = StringUtil.adjustChineseStringLength(fromname, 80);
            }
            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setFROMNAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeFromname,160) );
        }else{
            cbsTita.setFROMNAME(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("FROMNAME	>" + cbsTita.getFROMNAME() + "<");
        logMessage(this.logContext);

        /* FXML附言欄中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnFxmlMemo初始 >"+ feptxn.getFeptxnFxmlMemo() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnFxmlMemo())) {
            String txmemo = feptxn.getFeptxnFxmlMemo();
            String resizeTxmemo = "";
            if (StringUtil.containsFullWidth(txmemo)) {
                txmemo = StringUtil.convertToFullwidth(txmemo);
                resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 79);
            } else {
                resizeTxmemo = StringUtil.adjustChineseStringLength(txmemo, 80);
            }
            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setFXML_MEMO( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTxmemo,160) );
        }else{
            cbsTita.setFXML_MEMO(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("FXMLMEMO	>" + cbsTita.getFXML_MEMO() + "<");
        logMessage(this.logContext);

        cbsTita.setFROMCID(feptxn.getFeptxnIdno()); //財金Req電文BITMAP #42
        cbsTita.setTOCID(feptxn.getFeptxnToIdno()); //財金Req電文BITMAP #47
        //FXML手續費(客戶應付手續費)
        cbsTita.setFXML_CHARGE(feptxn.getFeptxnFeeCustpay());
        //轉出存戶性質別
        cbsTita.setAE_AEICIRCU(tita.getCUSTCODE());
        //限額累計註記(XG交易未使用)
        if ("XO".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setAE_TRNSFLAG(tita.getTRNSFLAG());
        }
        //業務類別
        cbsTita.setAE_BUSINESSTYPE(tita.getBUSINESSTYPE());
        /* 手續費負擔別*/
        cbsTita.setAE_AEIEFEET(tita.getFEEPAYMENTTYPE());
        //付款人自我備註(XG交易未使用)
        if ("XO".equals(feptxn.getFeptxnTxCode()) && StringUtils.isNotBlank(feptxn.getFeptxnPsbremFD())) {
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
                resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 9);
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
        //限額種類(XG交易未使用)
        if ("XO".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setAE_LIMITTYPE(tita.getLIMITTYPE());
        }
        //手續費優惠類別
        cbsTita.setAE_AEIDSTYP(feptxntcb.getFeptxntcbOvfeety());
        //手續費優惠主辦分行
        cbsTita.setAE_AEIDSBRH(feptxntcb.getFeptxntcbChafeeBranch());
        //FXML手續費優惠金額
        cbsTita.setFXML_DSCHARGE(StringUtils.leftPad(String.valueOf(feptxntcb.getFeptxntcbChafeeamt()==null?"00":feptxntcb.getFeptxntcbChafeeamt()),2,"0"));
        //實際轉出金額
        cbsTita.setAE_TRANS_AMT_OUT(feptxn.getFeptxnTxAmt());
        //轉出帳號清算分行
        cbsTita.setAE_FMMBR(feptxn.getFeptxnBrno());

        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
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
        /* 電文內容格式請參照TOTA電文格式(AB_XO_O001) */
        /* 拆解主機回應電文 */
        AB_XO_O001 tota = new AB_XO_O001();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新交易 */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(AB_XO_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        rtnCode = prepareUpdateIMSData(getTota());
        if(rtnCode != FEPReturnCode.Normal) {
    		getLogContext().setMessage("after prepare Update IMS Data, RC:" + rtnCode.toString());
    		sendEMS(getLogContext());
    	}else if (!cbsTota.getIMSRC_TCB().equals("000")
    			|| (!cbsTota.getIMSRC4_FISC().equals("4001") && !cbsTota.getIMSRC4_FISC().equals("4002")
                && !cbsTota.getIMSRC4_FISC().equals("4007") && StringUtils.isNotBlank(cbsTota.getIMSRC4_FISC()))) {
			rtnCode = FEPReturnCode.CBSCheckError;
		}

        //帳戶餘額
        if (cbsTota.getACTBALANCE() != null && cbsTota.getACTBALANCE().compareTo(BigDecimal.ZERO) != 0) {
            feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
        }
        //實際轉出金額(含手續費)，XG交易未使用
        if ("XO".equals(feptxn.getFeptxnTxCode()) && StringUtils.isNotBlank(cbsTota.getTRANSAMTOUT()) && !"0".equals(cbsTota.getTRANSAMTOUT())) {
            BigDecimal transAmtOut = new BigDecimal(cbsTota.getTRANSAMTOUT().trim());
            if (transAmtOut.compareTo(BigDecimal.ZERO) != 0) {
                feptxn.setFeptxnTxAmtAct(transAmtOut);
            }
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        	int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);
        	
			if (insertCount <= 0 || insertCount2 <= 0) {
				rtnCode = FEPReturnCode.FEPTXNUpdateError;
				transactionManager.rollback(txStatus);
			}else {
				transactionManager.commit(txStatus);
			}
        } catch (Exception ex) { // 新增失敗
        	transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }
        return rtnCode;
    }

}
