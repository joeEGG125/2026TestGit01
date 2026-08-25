package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.IB_WD_I001;
import com.syscom.fep.vo.text.ims.IB_WD_O001;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存提款Request交易電文
 *
 * @author bruce
 */
public class IBWDI001 extends ACBSAction {

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);

    public IBWDI001(MessageBase txData) {
        super(txData, new IB_WD_O001());
    }

    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格-原存行提款交易V1.0(111xxxx).doc
        IB_WD_I001 cbsTita = new IB_WD_I001();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPFWD0");// 主機業務別 長度8 2024/12/16 配合主機修改
        cbsTita.setSYSCODE("FEP");// 處理系統代號 長度4
        cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I");// 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2
        cbsTita.setSOURCE_CHANNEL("FIS");// CHANNEL或業務別 長度3
        cbsTita.setPCODE(this.feptxn.getFeptxnPcode());// 財金P-CODE 長度4
        cbsTita.setFSCODE(" ");// 合庫FS-CODE 長度2 原存行交易放空白
        cbsTita.setPROCESS_TYPE("ACCT");//交易處理類別 長度4
        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(this.feptxn.getFeptxnBkno());// 設備代理行 長度3
        cbsTita.setTXNSTAN(this.feptxn.getFeptxnStan());// 跨行交易序號 長度7
        cbsTita.setTERMINALID(this.feptxn.getFeptxnAtmno());// 端末機代號 長度8
        cbsTita.setTERMINAL_TYPE(this.feptxn.getFeptxnAtmType());// 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(this.feptxn.getFeptxnTroutBkno());// 發卡行/扣款行 長度3
        String atmType;
        if ("6071".equals(this.feptxn.getFeptxnAtmType())) {
            atmType = "X"; /* 無卡提款 */
        } else {
            atmType = "K"; /* 晶片卡 */
        }
        cbsTita.setCARDTYPE(atmType);// 交易卡片型態
        cbsTita.setRESPONSE_CODE(this.feptxn.getFeptxnReqRc());// 回應代號(RC) 長度4
        cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
        cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
        // DETAIL
        cbsTita.setICCHIPSTAN(this.feptxn.getFeptxnIcSeqno());// IC卡交易序號
        cbsTita.setTERM_CHECKNO(this.feptxn.getFeptxnAtmChk());// 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(this.feptxn.getFeptxnTxDatetimeFisc());// 交易日期時間
        if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
            cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
        }else{
            cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
        }
        // FEPTXN 為Varch(16), 財金規格B(V)(LL+DATA)合庫固定DATA為8位LL + X(08) , 必須把FEPTXN_IC_TAC 16 byte pack成8byte
        if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
            cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
        }else{
            cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
        }
        cbsTita.setTXNAMT(this.feptxn.getFeptxnTxAmt());// 交易金額
        cbsTita.setFROMACT(this.feptxn.getFeptxnTroutActno());// 卡片提款帳號(無卡提款序號)
        /* 2023/8/8 修改for 改用財金電文PINBLOCK欄位 */
        /* 2023/12/12 修改for 財金PINBLOCK欄位轉換至IMS主機 */
        if (StringUtils.isNotBlank(this.getInbkRequest().getPINBLOCK())) {
            RefString W_PINBLOCK = new RefString(null);
            this.logContext.setRemark("Start ConvertFISCPinToHost");
            logMessage(this.logContext);
            FEPReturnCode returnCode = FEPReturnCode.ENCPINBlockConvertError;
            ENCHelper encHelper = new ENCHelper(getGeneralData());
            returnCode = encHelper.ConvertFISCPinToHost(this.getInbkRequest().getPINBLOCK(), W_PINBLOCK,this.getFiscData().getMsgCtl().getMsgctlPcode());
            if (returnCode != FEPReturnCode.Normal) {
                return FEPReturnCode.ENCPINBlockConvertError;
            } else {
                cbsTita.setPINBLOCK(W_PINBLOCK.get());
            }
        }
        cbsTita.setDRVS(StringUtils.repeat(" ", 372)); // 保留欄位
        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS  TOTA電文 、更新feptxn並insertSmsmsg
     *
     * @param cbsTota
     * @param type
     * @return
     */
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(IB_WD_O001) */
        /* 拆解主機回應電文 */
        IB_WD_O001 tota = (IB_WD_O001) this.getTota();//取得tota物件
        tota.parseCbsTele(cbsTota);//拆解CBS  TOTA電文
        this.setTota(tota);//塞入拆解後的tota讓AA取得

        /* 更新FEPTXN */
        FEPReturnCode rtnCode = this.updateFepTxn(tota, type);

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新Feptxn
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFepTxn(IB_WD_O001 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        //變更交易記錄
        this.feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
        this.feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        this.feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
        this.feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */
        this.feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        this.feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());//帳戶手續費
        this.feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());//提領幣別手續費
        //身心障礙跨行提款手續費減免
        if (feptxn.getFeptxnFeeCustpayAct() != null
                && feptxn.getFeptxnFeeCustpayAct().compareTo(BigDecimal.ZERO) == 0) {
            /* 2025/12/23 修改, 活動型態存入 FEPTXNTCB */
            feptxntcb.setFeptxntcbActivityType(cbsTota.getACTIVITY_TYPE());
        }
        if (cbsTota.getAVAILABLE_BALANCE() != null) {
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE()); /* 可用餘額 */
        }
        if(cbsTota.getACT_BALANCE() != null){
            feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */
        }else{
            feptxn.setFeptxnBalb(BigDecimal.ZERO);
        }
        this.feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO());

        this.feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
        this.feptxn.setFeptxnCbsTimeout((short) 0);// CBS 逾時 FLAG

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
            /* 2022/11/21 寫入帳務分行 */
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
            } else {
                feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
            }
            //交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 2024/3/20 新增傳送通知代碼(CBS摘要) */
                feptxn.setFeptxnCbsDscpt(cbsTota.getNOTICE_NUMBER());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
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

    private void insertSMSMSG(IB_WD_O001 cbsTota) throws ParseException {
//        String txDate = feptxn.getFeptxnTxDate();
//        Integer ejfno = feptxn.getFeptxnEjfno();
//        Smsmsg smsmsg = smsmsgExtMapper.selectByPrimaryKey(txDate, ejfno);
//        // 檢核SMSMSG 資料是否存在，不存在才insert SMSMSG
//        // 以SMSMSG_TX_DATE及SMSMSG_EJFNO 為key讀取 Table
//        if (smsmsg == null) {
//            smsmsg = new Smsmsg();
//            smsmsg.setSmsmsgTxDate(txDate);
//            smsmsg.setSmsmsgEjfno(ejfno);
//            smsmsg.setSmsmsgStan(feptxn.getFeptxnStan());
//            smsmsg.setSmsmsgPcode(feptxn.getFeptxnPcode());
//            smsmsg.setSmsmsgTroutActno(feptxn.getFeptxnTroutActno());
//            smsmsg.setSmsmsgTxTime(feptxn.getFeptxnTxTime());
//            smsmsg.setSmsmsgZone("TWN");
//            smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//            smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//            smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//            smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//            smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            smsmsg.setSmsmsgNotifyFg("Y");
//            smsmsg.setSmsmsgSendType(cbsTota.getNOTICE_TYPE());
//            smsmsg.setSmsmsgChannel(feptxn.getFeptxnChannel());
//            Date datenow = (Date) new SimpleDateFormat("yyyy/MM/DD-HH.mm.ss.SSS").parse(
//                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_TAX));
//            smsmsg.setUpdateTime(datenow);
//            smsmsg.setSmsmsgNoticeNo(cbsTota.getNOTICE_NUMBER());
//
//            if (smsmsgExtMapper.insert(smsmsg) <= 0) {
//                getLogContext().setRemark("寫入簡訊資料檔(SMSMSG)發生錯誤");
//                this.logMessage(getLogContext());
//            }
//        }
    }
}
