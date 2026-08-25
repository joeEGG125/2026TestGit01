package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_PY_I011;
import com.syscom.fep.vo.text.ims.IB_PY_O011;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存全國繳費Request交易電文
 *
 * @author Joseph
 */

public class IBPYI011 extends ACBSAction {

    public IBPYI011(MessageBase txData) {
        super(txData, new IB_PY_I011());
    }

//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);
    private NpsunitMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    private SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    FISC_INBK inbkReq = this.getInbkRequest();

    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格_原存行全國繳費交易V1.0(111xxxx).doc
        IB_PY_I011 cbsTita = new IB_PY_I011();
        // HEADER
        if(feptxn.getFeptxnPcode().substring(0, 3).equals("226")){ // 2024/12/16 配合主機修改 */
            cbsTita.setIMS_TRANS("MFEPFPY0"); // 主機業務別 長度8
        }else{
            cbsTita.setIMS_TRANS("MFEPFG00"); // 主機業務別 長度8
        }
        cbsTita.setSYSCODE("FEP "); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給0O */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(StringUtils.repeat(" ", 2)); // 合庫FS-CODE 長度2 原存行交易放空白
        if ("1".equals(txType)) {
            cbsTita.setPROCESS_TYPE("ACCT"); /* 帳務 */
        } else {
            cbsTita.setPROCESS_TYPE("CHK"); /* 檢核 */
        }
        // 財金營業日(西元年須轉民國年)
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
        if (feptxn.getFeptxnPcode().substring(0, 3).equals("226") || "2569".equals(feptxn.getFeptxnPcode())) {
            cbsTita.setCARDTYPE("I"); // 交易卡片型態
        } else {
            cbsTita.setCARDTYPE("K"); // 交易卡片型態
        }
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
        cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
        cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
        /* CBS Request DETAIL */
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno()); // IC卡交易序號
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk()); // 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDatetimeFisc()); // 交易日期時間
        if(StringUtils.isNotBlank(this.getInbkRequest().getICMARK())){
            cbsTita.setICMEMO(this.getInbkRequest().getICMARK()); // IC卡備註欄
        }else{
            cbsTita.setICMEMO(StringUtils.repeat("40",30)); // IC卡備註欄
        }
        /*
         * FEPTXN 為Varch(16), 財金規格B(V)(LL+DATA)合庫固定DATA為8位LL + X(08) , 必須把FEPTXN_IC_TAC
         * 16 byte pack成8byte
         */
        if (StringUtils.isNotBlank(this.getInbkRequest().getTAC())) {
            cbsTita.setTXNICCTAC("000A" + this.getInbkRequest().getTAC()); // 交易驗證碼
        }else{
            cbsTita.setTXNICCTAC(StringUtils.repeat("40",10)); // 交易驗證碼
        }
        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt()); // 交易金額
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片提款帳號(轉出帳號)
        //20221006 改用財金INBK Requst電文
        cbsTita.setTransferee_Bank_ID(inbkReq.getTrinBkno()); // 轉入行庫
        cbsTita.setTransferor_Bank_ID(inbkReq.getTroutBkno()); // 轉出行庫
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno()); // 轉入帳號
        if (("2561".equals(feptxn.getFeptxnPcode()) || "2562".equals(feptxn.getFeptxnPcode())
                || "2563".equals(feptxn.getFeptxnPcode()) || "2564".equals(feptxn.getFeptxnPcode()))
                && "18888888".equals(feptxn.getFeptxnBusinessUnit()) //委託單位代號: 18888888
                && "59999".equals(feptxn.getFeptxnPaytype()) //繳費類別: 59999
                && "9999".equals(feptxn.getFeptxnPayno()) //費用代號: 9999
                ) {
            cbsTita.setPY_SPECIAL_FLAG("ET"); /*繳費轉移計劃*/
        } else {
            cbsTita.setPY_SPECIAL_FLAG(StringUtils.repeat(" ", 2)); //兩個空白
        }
        //CheckNPSUNIT時已讀取NPSUNIT Table, 必須Keep
        Npsunit npsunit = new Npsunit();
        npsunit.setNpsunitNo(feptxn.getFeptxnBusinessUnit()); /*委託單位代號*/
        npsunit.setNpsunitPaytype(feptxn.getFeptxnPaytype()); /*繳款類別*/
        npsunit.setNpsunitFeeno(feptxn.getFeptxnPayno()); /*費用代號*/
        Npsunit rc = npsunitExtMapper.selectByPrimaryKey(npsunit.getNpsunitNo(), npsunit.getNpsunitPaytype(), npsunit.getNpsunitFeeno());
        if (rc != null)
            cbsTita.setPY_HOST_Branch(rc.getNpsunitBkno()); // 全國繳費帳務代理行
        else
            cbsTita.setPY_HOST_Branch(StringUtils.repeat(" ", 7)); // 全國繳費帳務代理行  7 個空白
        cbsTita.setPY_IDNO(feptxn.getFeptxnIdno()); // 身份証/營利事業統一編號
        cbsTita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
        cbsTita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
        cbsTita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
        if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())){
            cbsTita.setPY_PAYTXNOL(feptxn.getFeptxnRemark().substring(4,20)); // 銷帳編號 第5~20位(16位)
        }
        if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())) {
            feptxn.setFeptxnReconSeqno(feptxn.getFeptxnRemark().substring(4, 20));  //2025/6/12 修改, 寫入銷帳編號
        }
        if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())) {
            cbsTita.setPY_PAYDDATE(feptxn.getFeptxnRemark().substring(20, 28));  // 第21~28位(8位) 繳款期限
        }
        if(StringUtils.isNotBlank(feptxn.getFeptxnRemark())) {
            feptxn.setFeptxnDueDate(feptxn.getFeptxnRemark().substring(20, 28)); //2025/6/12 修改, 寫入繳款期限
        }
        cbsTita.setPY_PAYMEMO_FILL(feptxn.getFeptxnRemark().substring(28, 40)); // 附言欄保留欄位
        cbsTita.setPY_CHARGCUS(inbkReq.getNpsFee()); // 繳費作業手續費
        cbsTita.setPY_CHARGUNT(inbkReq.getNpsFeeAll()); // 該單位應收手續費
        cbsTita.setPY_PAYTXNOL1(StringUtils.repeat(" ", 24));
        cbsTita.setDRVS(StringUtils.repeat(" ", 229)); // 保留欄位
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
        /* 電文內容格式請參照TOTA電文格式(IB_PY_O011) */
        /* 拆解主機回應電文 */
        IB_PY_O011 tota = new IB_PY_O011();
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
    private FEPReturnCode updateFEPTxn(IB_PY_O011 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
        feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 帳戶手續費 */
        feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE()); /* 提領幣別手續費 */
        if (cbsTota.getAVAILABLE_BALANCE() != null) {
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE()); /* 可用餘額 */
        }
        feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */

        feptxn.setFeptxnLuckyno(cbsTota.getLUCKYNO()); /* 促銷應用訊息 */

        feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160()); // 待增加
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
            /* 2022/11/21 寫入帳務分行 */
            feptxn.setFeptxnBrno(cbsTota.getIMS_FMMBR());
            feptxn.setFeptxnTrinBrno(cbsTota.getIMS_TMMBR());
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 1); /* 已記帳 */
            } else {
                feptxn.setFeptxnAccType((short) 0); /* 未記帳 */
            }
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
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

    /**
     * 新增交易通知
     *
     * @param cbsTota
     * @throws ParseException
     */
    private void insertSMSMSG(IB_PY_O011 cbsTota) throws ParseException {
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
