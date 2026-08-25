package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_XI_I001;
import com.syscom.fep.vo.text.ims.IB_XI_O001;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存資金調撥交易Request電文
 *
 * @author Bruis
 */

public class IBXII001 extends ACBSAction {

    public IBXII001(MessageBase txData) {
        super(txData, new IB_XI_I001());
    }


    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        // 組CBS 原存交易Request電文, 電文內容格式請參照: D_I15_合庫FEP_主機電文規格-原存行企業付款交易V1.00(1150303).doc
        IB_XI_I001 cbsTita = new IB_XI_I001();

        // HEADER
        cbsTita.setIMS_TRANS("MFEPXML0"); // 主機業務別 長度8 2026/7/16 配合主機修改
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給0O */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
        cbsTita.setPROCESS_TYPE("CHK"); /* 帳務 */
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno()); // 設備代理行 長度3
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());// 端末機代號 長度8
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType()); // 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
        cbsTita.setCARDTYPE("X"); // 交易卡片型態 長度1 /* 未使用卡片 */
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
        cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白
        cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25

        /* CBS Request DETAIL */
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno()); // IC卡交易序號
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); // 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc()); // 交易日期時間
        if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
            cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
        }else{
            cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
        }
        if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
            cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
        }else{
            cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
        }
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(轉出帳號)
        // 20221006 改用財金INBK Requst電文
        FISC_INBK reqINBK = this.getInbkRequest();
        cbsTita.setTransferee_Bank_ID(reqINBK.getTrinBkno()); // 轉入行庫
        cbsTita.setTransferor_Bank_ID(reqINBK.getTroutBkno()); // 轉出行庫
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno()); // 轉入帳號
        cbsTita.setTR_SPECIAL_FLAG(StringUtils.repeat(" ", 2)); // 特殊轉帳類別註記 //二個空白

        /* 收款人姓名中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnToName初始 >"+ feptxn.getFeptxnToName() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnToName())) {
            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setTONAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnToName(),160) );
        }else{
            cbsTita.setTONAME(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("TONAME   >" + cbsTita.getTONAME() + "<");
        logMessage(this.logContext);

        /* 付款人姓名中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnFromName初始 >"+ feptxn.getFeptxnFromName() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnFromName())) {
            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setFROMNAME( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnFromName(),160) );
        }else{
            cbsTita.setFROMNAME(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("FROMNAME >" + cbsTita.getFROMNAME() + "<");
        logMessage(this.logContext);

        /* FXML附言欄中文轉碼處理 */
        getLogContext().setProgramName(ProgramName + ".getCbsTita");
        getLogContext().setRemark("FeptxnFxmlMemo初始 >"+ feptxn.getFeptxnFxmlMemo() +"<");
        logMessage(getLogContext());
        if(StringUtils.isNotBlank(feptxn.getFeptxnFxmlMemo())) {
            /* CALL 字霸執行中文轉碼, 由UTF8轉成 NHC */
            cbsTita.setFXMLMEMO( EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(feptxn.getFeptxnFxmlMemo(),160) );
        }else{
            cbsTita.setFXMLMEMO(StringUtils.rightPad("", 160, "40"));
        }
        this.logContext.setRemark("FXMLMEMO >" + cbsTita.getFXMLMEMO() + "<");
        logMessage(this.logContext);

        cbsTita.setFROMCID(feptxn.getFeptxnIdno()); //財金Req電文BITMAP #42
        cbsTita.setTOCID(feptxn.getFeptxnToIdno()); //財金Req電文BITMAP #47
        cbsTita.setDRVS(StringUtils.repeat(" ", 86)); // 保留欄位

        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS TOTA電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(IB_XI_O001) */
        /* 拆解主機回應電文 */
        IB_XI_O001 tota = new IB_XI_O001();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新FEPTXN */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新FEPTXN
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(IB_XI_O001 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;

        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
        feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
        feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
        if (cbsTota.getACT_BALANCE() != null) {
            feptxn.setFeptxnBala(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */
        }
        // 20221026 已取消不新增此欄位, AA回財金Response 電文直接用CBS_TOTA
        // 20221031 因要寫至INBK2160 故還是UPDATE
        feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO()); /* 促銷應用訊息 */
        feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());


        // 更新FEPTXN& FEPTXNTCB
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */

        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }

        /* 2024/7/1 更新 FEPTXNTCB */
        rtnCode = this.UpdateFEPTXNTCB(cbsTota);

        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            /* CBS回覆成功 */

            /* 主機回傳民國年須轉成西元年 */
            String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
            if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                    || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
                imsbusinessDate = "00000000";
            } else {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
            }
            feptxn.setFeptxnTbsdy(imsbusinessDate);
            // 寫入帳務分行
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
            } else {
                feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
            }
            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
            }
            rtnCode = FEPReturnCode.Normal;
        }
        // 兩個Table 同步更新，如有任何錯誤，請ROLLBACK & 寫EMS
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
        	int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
        	int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);
        	
			if (insertCount <= 0 || insertCount2 <= 0) {
                throw new Exception();
            }
            transactionManager.commit(txStatus);
        } catch (Exception ex) { 
        	transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }
        return rtnCode;
    }

}
