package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Eaitxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.ims.AB_ACC_I01;
import com.syscom.fep.vo.text.ims.AB_ACC_O01;
import com.syscom.fep.vo.text.ims.AB_ACC_O02;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;

public class ABACCI01 extends ACBSAction {

    public ABACCI01(MessageBase txType) {
        super(txType, new AB_ACC_O01());
    }

    RCV_NB_GeneralTrans_RQ tita = this.getNBRequest();
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
        /* TITA 請參考合庫主機電文規格(AB_ACC_I01) */
        // Header
        AB_ACC_I01 cbsTita = new AB_ACC_I01();

//        Eaitxn eaitxn = eaitxnMapper.selectByPrimaryKey(feptxn.getFeptxnTxDate(), feptxn.getFeptxnEjfno());

        String aa = cbsTita.makeMessage(cbsTita, "0");
        cbsTita.setINTRAN(StringUtils.rightPad("EAIA", 8, " "));
//        String inmsgid = feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0");
        String inmsgid = FormatUtil.dateTimeFormat(Calendar.getInstance(),FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN) + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0");
        cbsTita.setINDATE(feptxn.getFeptxnTxDate());
        cbsTita.setINTIME(feptxn.getFeptxnTxTime());
        if (txType.equals("4")) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND,2);
            inmsgid = FormatUtil.dateTimeFormat(calendar,FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN) + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0");
            cbsTita.setINDATE(FormatUtil.dateTimeFormat(Calendar.getInstance(),FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            cbsTita.setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(),FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
        }
//        cbsTita.setINMSGID(StringUtils.rightPad(inmsgid, 24, " "));

//        String msgid =  StringUtils.rightPad(tita.getBody().getRq().getHeader().getMSGID(),24, " ");
        cbsTita.setINMSGID(inmsgid);
        cbsTita.setMQIDCNV("N");
//        cbsTita.setINSTAN(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 10, "0"));
        cbsTita.setINSERV("FEP1");
        cbsTita.setINTD("61000004");
        cbsTita.setINAP(StringUtils.rightPad(feptxn.getFeptxnChannel().trim(), 5, " "));
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");

        //BODY
        if (txType.equals("0")) {
            cbsTita.setRECTYPE("A");
        } else {
            cbsTita.setRECTYPE("B");
        }
        //限額累計註記(端末上送)
        cbsTita.setTRNSFLAG(tita.getBody().getRq().getSvcRq().getTRNSFLAG());
        //財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setACCDATE(feptxnTbsdyFisc);
        cbsTita.setTRNSWAY("4");
        cbsTita.setBUSINESSTYPE(tita.getBody().getRq().getSvcRq().getBUSINESSTYPE().trim());
        String titaOutIdNo = tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO().trim().length() == 11 ? TxHelper.parseCIDtoChar(tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO().trim()) : tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO().trim();
        cbsTita.setTRNSFROUTIDNO(titaOutIdNo);
        cbsTita.setINID(titaOutIdNo.trim()); //使用者身分證號/統編
        if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
            cbsTita.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno7());
        } else {
            cbsTita.setTRNSFROUTBANK(feptxn.getFeptxnTroutBkno() + "0000");
        }
        cbsTita.setTRNSFROUTACCNT(StringUtils.leftPad(feptxn.getFeptxnTroutActno(), 16, "0"));
        cbsTita.setTRNSFRINIDNO(StringUtils.rightPad(tita.getBody().getRq().getSvcRq().getTRNSFRINIDNO().trim(), 10, " "));
        if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
            cbsTita.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno7());
        } else {
            cbsTita.setTRNSFRINBANK(feptxn.getFeptxnTrinBkno() + "0000");
        }
        cbsTita.setTRNSFRINACCNT(StringUtils.leftPad(feptxn.getFeptxnTrinActno().trim(), 16, "0"));
        cbsTita.setTRANS_AMT(feptxn.getFeptxnTxAmt());
        cbsTita.setBLOCK_AMT(new BigDecimal(0));
        cbsTita.setUNBLOCK_AMT(new BigDecimal(0));
        if (txType.equals("4")) {
            //原圈存MSGID
            cbsTita.setORIBLOCK_MSGID( feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0") );
            //原圈存應用系統代號
            cbsTita.setORIBLOCK_INAP(feptxn.getFeptxnChannel());
            //優惠手續費負擔分行
            cbsTita.setCHAFEE_BRANCH(feptxntcb.getFeptxntcbChafeeBranch());
            //手續費減免金額
            if(feptxntcb.getFeptxntcbChafeeamt()!=null){
                cbsTita.setCHAFEE_AMT(new BigDecimal(feptxntcb.getFeptxntcbChafeeamt()));
            }else {
                cbsTita.setCHAFEE_AMT(new BigDecimal("0"));
            }
            //網銀客戶手續費優惠註記
            cbsTita.setCHAFEE_TYPE(feptxntcb.getFeptxntcbOvfeety());
            //原扣帳交易的STAN
            cbsTita.setORI_INSTAN(feptxn.getFeptxnStan());
        } else {
            cbsTita.setORIBLOCK_MSGID(" ");
            cbsTita.setORIBLOCK_INAP(" ");
            cbsTita.setORI_INSTAN(" ");
        }
        cbsTita.setFEEPAYMENTTYPE(tita.getBody().getRq().getSvcRq().getFEEPAYMENTTYPE().trim());
        cbsTita.setCUSTPAY_FEE(feptxn.getFeptxnFeeCustpay());
        cbsTita.setFISC_FEE(tita.getBody().getRq().getSvcRq().getFISCFEE());
        cbsTita.setOTHERBANK_FEE(tita.getBody().getRq().getSvcRq().getOTHERBANKFEE());
//        cbsTita.setAPP_FIANCE_FLAG(" ");
//        cbsTita.setTRNSFRIN_NOTE(tita.getBody().getRq().getSvcRq().getTRNSFRINNOTE().trim());
//        cbsTita.setTRNSFROUT_NOTE(tita.getBody().getRq().getSvcRq().getTRNSFROUTNOTE().trim());
//        cbsTita.setTRNSFRINNAME(" ");
        cbsTita.setFAXFEE(tita.getBody().getRq().getSvcRq().getFAXFEE());
        cbsTita.setTRANSFEE(tita.getBody().getRq().getSvcRq().getTRANSFEE());

        //for EDI
        if (StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getAFFAIRSCODE().trim()))
            cbsTita.setAFFAIRSCODE(tita.getBody().getRq().getSvcRq().getAFFAIRSCODE().trim());
        if (StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getCUSTCODE().trim()))
            cbsTita.setCUSTCODE(tita.getBody().getRq().getSvcRq().getCUSTCODE().trim());
        if (StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getTRNSFROUTNAME().trim()))
            cbsTita.setTRNSFROUTNAME(tita.getBody().getRq().getSvcRq().getTRNSFROUTNAME().trim());
        // for 個銀
        if (StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getSSLTYPE().trim()))
            cbsTita.setSSLTYPE(tita.getBody().getRq().getSvcRq().getSSLTYPE().trim());
        if (StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getLIMITTYPE().trim()))
            cbsTita.setLIMITTYPE(tita.getBody().getRq().getSvcRq().getLIMITTYPE().trim());
        // 2026.2.2 LeYun 轉帳類別註記(TM: 手機門號轉帳)
        if(StringUtils.isNotBlank(tita.getBody().getRq().getSvcRq().getTRANSTYPEFLAG())
                && StringUtils.equals(tita.getBody().getRq().getSvcRq().getTRANSTYPEFLAG(), "TM"))
            cbsTita.setFUNCTIONTYPE("M");

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
        /* 電文內容格式請參照TOTA電文格式(AB_ACC_O01) */
        /* 拆解主機回應電文 */
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (CodeGenUtil.ebcdicToAsciiDefaultEmpty(cbsTota).substring(88, 92).equals("4001")) {
            AB_ACC_O01 tota = new AB_ACC_O01();
            //處理帳務主機回傳金額欄位第一位數為空白導致錯誤問題 By Robbie
            cbsTota = parseAmtFor4e(cbsTota);
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            /* 更新交易 */
            rtnCode = this.updateFEPTxn(tota, type);
        } else {
            AB_ACC_O02 tota = new AB_ACC_O02();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            /* 更新交易 */
            rtnCode = this.updateFEPTxn(tota, type);
        }

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }
    

    public FEPReturnCode insertEAITXN(AB_ACC_O01 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;

        Eaitxn InsertEaitxn = new Eaitxn();

        InsertEaitxn.setEaitxnTxDate(feptxn.getFeptxnTxDate());
        InsertEaitxn.setEaitxnTxTime(feptxn.getFeptxnTxTime());
        InsertEaitxn.setEaitxnEjfno(feptxn.getFeptxnEjfno());
        InsertEaitxn.setEaitxnStan(feptxn.getFeptxnStan());
        InsertEaitxn.setEaitxnMsgid(feptxn.getFeptxnMsgid());
        InsertEaitxn.setEaitxnServ("FEP1");
        InsertEaitxn.setEaitxnTd("61000004");
        InsertEaitxn.setEaitxnAp(feptxn.getFeptxnChannel());
        InsertEaitxn.setEaitxnIdno(feptxn.getFeptxnIdno());
        InsertEaitxn.setEaitxnOutrtc(cbsTota.getOUTRTC());
        InsertEaitxn.setEaitxnOutbill(cbsTota.getOUTBILL());
        InsertEaitxn.setEaitxnAccdate(cbsTota.getACCDATE());
        InsertEaitxn.setEaitxnTrnsfroutidno(cbsTota.getTRNSFRIDNO());
        InsertEaitxn.setEaitxnTrnsfroutbank(cbsTota.getTRNSFROUTBANK());
        InsertEaitxn.setEaitxnTrnsfroutaccnt(cbsTota.getTRNSFROUTACCNT());
        InsertEaitxn.setEaitxnTrnsfrinidno(cbsTota.getTRNSFRINIDNO());
        InsertEaitxn.setEaitxnTrnsfrinbank(cbsTota.getTRNSFRINBANK());
        InsertEaitxn.setEaitxnTrnsfrinaccnt(cbsTota.getTRNSFRINACCNT());
        InsertEaitxn.setEaitxnTxAmt(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANS_AMT(), "##0.00")));
        InsertEaitxn.setEaitxnFeepaymenttype(cbsTota.getFEEPAYMENTTYPE());
        InsertEaitxn.setEaitxnCustpayFee(cbsTota.getCUSTPAY_FEE());
        if (cbsTota.getFISC_FEE() != null) {
            InsertEaitxn.setEaitxnFiscFee(cbsTota.getFISC_FEE());
        }
        if (cbsTota.getOTHERBANK_FEE() != null) {
            InsertEaitxn.setEaitxnOtherbankFee(cbsTota.getOTHERBANK_FEE());
            InsertEaitxn.setEaitxnChafeeBranch(cbsTota.getCHAFEE_BRANCH());
        }
        if (cbsTota.getCHAFEE_AMT() != null) {
            InsertEaitxn.setEaitxnChafeeAmt(cbsTota.getCHAFEE_AMT());
        }
        InsertEaitxn.setEaitxnTransfroutbal(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSFROUTBAL(), "##0.00")));
        InsertEaitxn.setEaitxnTransamtout(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSAMTOUT(), "##0.00")));
        InsertEaitxn.setEaitxnTransamtin(new BigDecimal(FormatUtil.decimalFormat(cbsTota.getTRANSAMTIN(), "##0.00")));
        InsertEaitxn.setEaitxnCleanbranchOut(cbsTota.getCLEANBRANCH_OUT());
        InsertEaitxn.setEaitxnCleanbranchIn(cbsTota.getCLEANBRANCH_IN());
        InsertEaitxn.setEaitxnReceivename(cbsTota.getRECEIVENAME());
        InsertEaitxn.setEaitxnPayernote(cbsTota.getTRNSFRINNAME());
        InsertEaitxn.setEaitxnTrnsfrinname(cbsTota.getTRNSFRINNAME());
        InsertEaitxn.setEaitxnTrnsfroutname(cbsTota.getTRNSFROUTNAME());
        InsertEaitxn.setEaitxnPayeremail(cbsTota.getPAYEREMAIL());
        InsertEaitxn.setEaitxnPwatxday(cbsTota.getPWATXDAY());
        InsertEaitxn.setEaitxnChafeeType(cbsTota.getCHAFEE_TYPE());

        if (cbsTota.getFAXFEE() != null) {
            InsertEaitxn.setEaitxnFaxfee(cbsTota.getFAXFEE());
        }
        if (cbsTota.getTRANSFEE() != null) {
            InsertEaitxn.setEaitxnTransfee(cbsTota.getTRANSFEE());
            InsertEaitxn.setEaitxnTransoutcust(cbsTota.getTRANSOUTCUST());
            InsertEaitxn.setEaitxnTaxBranch(cbsTota.getTAX_BRANCH());
        }

//        if (eaitxnMapper.insertSelective(InsertEaitxn) < 1) {
//            return FEPReturnCode.InsertFail;
//        }
        return rtnCode;
    }

//    public FEPReturnCode insertEAITXN(AB_ACC_O02 cbsTota) throws Exception {
//        FEPReturnCode rtnCode = FEPReturnCode.Normal;
//
//        Eaitxn InsertEaitxn = new Eaitxn();
//
//        InsertEaitxn.setEaitxnTxDate(feptxn.getFeptxnTxDate());
//        InsertEaitxn.setEaitxnTxTime(feptxn.getFeptxnTxTime());
//        InsertEaitxn.setEaitxnEjfno(feptxn.getFeptxnEjfno());
//        InsertEaitxn.setEaitxnStan(feptxn.getFeptxnStan());
//        InsertEaitxn.setEaitxnMsgid(feptxn.getFeptxnMsgid());
//        InsertEaitxn.setEaitxnServ("FEP1");
//        InsertEaitxn.setEaitxnTd("61000004");
//        InsertEaitxn.setEaitxnAp(feptxn.getFeptxnChannel());
//        InsertEaitxn.setEaitxnIdno(feptxn.getFeptxnIdno());
//        InsertEaitxn.setEaitxnOutrtc(cbsTota.getOUTRTC());
//        InsertEaitxn.setEaitxnOutbill(cbsTota.getOUTBILL());
//
//        if (eaitxnMapper.insertSelective(InsertEaitxn) < 1) {
//            return FEPReturnCode.InsertFail;
//        }
//        return rtnCode;
//    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxn(AB_ACC_O01 cbsTota, String txType) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
//        feptxn.setFeptxnAscRc(NormalRC.FISC_ATM_OK);
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_OUT())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_OUT());
        }
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_IN())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_IN());
        }
        //主機交易時間
        feptxn.setFeptxnCbsTxTime(cbsTota.getOUTTIME());
        //手續費
        if (cbsTota.getCUSTPAY_FEE() != null) {
            feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
//            if ("TWD".equals(feptxn.getFeptxnTxCur())) { //台幣手續費
//                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
//            } else { //外幣手續費
//                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE().divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP));
//            }
        }
        if (StringUtils.isNotBlank(cbsTota.getPAYERNOTE())) {
            feptxn.setFeptxnPsbremFD(cbsTota.getPAYERNOTE());
        }
        if (StringUtils.isNotBlank(cbsTota.getRECEIVENAME())) {
            feptxn.setFeptxnPsbremFC(cbsTota.getRECEIVENAME());
        }
        if (StringUtils.isNotBlank(cbsTota.getTAX_BRANCH())) {
            feptxn.setFeptxnTroutBkno7(feptxn.getFeptxnTroutBkno() + cbsTota.getTAX_BRANCH());
        }
        feptxn.setFeptxnAtmno("T" + cbsTota.getCLEANBRANCH_OUT() + feptxn.getFeptxnChannel());

//        //TCB欄位紀錄
//        feptxntcb.setFeptxntcbImsrc4Fisc(NormalRC.FISC_ATM_OK);
        //存款主機下送結果
        feptxntcb.setFeptxntcbOutrtc(cbsTota.getOUTRTC());
        if (!txType.equals("4")) { //解除優惠時不能將存款主機的狀態覆蓋入扣帳主機狀態
            feptxn.setFeptxnCbsRc(cbsTota.getOUTRTC());
        }

        //手續費負擔別
        if (StringUtils.isNotBlank(cbsTota.getFEEPAYMENTTYPE())) {
            feptxntcb.setFeptxntcbFeepaymenttype(cbsTota.getFEEPAYMENTTYPE());
        }
        // 優惠手續費負擔分行
        if (StringUtils.isNotBlank(cbsTota.getCHAFEE_BRANCH())) {
            feptxntcb.setFeptxntcbChafeeBranch(cbsTota.getCHAFEE_BRANCH());
        }
        // 手續費減免金額
        if (cbsTota.getCHAFEE_AMT() != null) {
            feptxntcb.setFeptxntcbChafeeamt(cbsTota.getCHAFEE_AMT().intValue());
        }
        //本/次日帳註記
        if (StringUtils.isNotBlank(cbsTota.getPWATXDAY())) {
            feptxntcb.setFeptxntcbOvtpt(cbsTota.getPWATXDAY());
        }
        // 網銀客戶手續費優惠註記
        if (StringUtils.isNotBlank(cbsTota.getCHAFEE_TYPE())) {
            feptxntcb.setFeptxntcbOvfeety(cbsTota.getCHAFEE_TYPE());
        }
        // 轉出帳客戶性質
        if (StringUtils.isNotBlank(cbsTota.getTRANSOUTCUST())) {
            feptxntcb.setFeptxntcbTransoutcust(cbsTota.getTRANSOUTCUST());
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
            int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

            if (insertCount <= 0 || insertCount2 <= 0) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                transactionManager.rollback(txStatus);
            } else {
                transactionManager.commit(txStatus);
            }
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
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxnForNBSelf(AB_ACC_O01 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        /* 變更FEPTXN交易記錄 */
        feptxn.setFeptxnAscRc(NormalRC.FISC_ATM_OK);
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_OUT())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_OUT());
        }
        if (StringUtils.isNotBlank(cbsTota.getCLEANBRANCH_IN())) {
            feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_IN());
        }
        //主機交易時間
        feptxn.setFeptxnCbsTxTime(cbsTota.getOUTTIME());
        //手續費
        if (cbsTota.getCUSTPAY_FEE() != null) {
            feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
            if ("TWD".equals(feptxn.getFeptxnTxCur())) { //台幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE());
            } else { //外幣手續費
                feptxn.setFeptxnFeeCustpay(cbsTota.getCUSTPAY_FEE().divide(feptxn.getFeptxnExrate(), 2, RoundingMode.HALF_UP));
            }
        }
        if (StringUtils.isNotBlank(cbsTota.getPAYERNOTE())) {
            feptxn.setFeptxnPsbremFD(cbsTota.getPAYERNOTE());
        }
        if (StringUtils.isNotBlank(cbsTota.getRECEIVENAME())) {
            feptxn.setFeptxnPsbremFC(cbsTota.getRECEIVENAME());
        }
        if (StringUtils.isNotBlank(cbsTota.getTAX_BRANCH())) {
            feptxn.setFeptxnTroutBkno7(feptxn.getFeptxnTroutBkno() + cbsTota.getTAX_BRANCH());
        }
        feptxn.setFeptxnAtmno(new StringBuilder(feptxn.getFeptxnAtmno()).replace(1,5, cbsTota.getCLEANBRANCH_OUT()).toString());

        feptxn.setFeptxnBrno(cbsTota.getCLEANBRANCH_OUT());
        if (feptxn.getFeptxnAtmno().length() == 7){
            feptxn.setFeptxnAtmno(feptxn.getFeptxnAtmno().substring(0,1) + cbsTota.getCLEANBRANCH_OUT() +feptxn.getFeptxnAtmno().substring(5,7));
        }
        else{
            feptxn.setFeptxnAtmno(feptxn.getFeptxnAtmno().substring(0,1) + cbsTota.getCLEANBRANCH_OUT() +feptxn.getFeptxnAtmno().substring(5,8));
        }

        //TCB欄位紀錄
        feptxntcb.setFeptxntcbImsrc4Fisc(NormalRC.FISC_ATM_OK);

        //借用TCB欄位 存資料 FOR RS使用
        //PWATXDAY交易日註記
        if (StringUtils.isNotBlank(cbsTota.getPWATXDAY())) {
            //FEPTXNTCB_IMSACCT_FLAG
            feptxntcb.setFeptxntcbImsacctFlag(cbsTota.getPWATXDAY());
        }
        //TRANSOUTCUST轉出存戶性質別
        if (StringUtils.isNotBlank(cbsTota.getTRANSOUTCUST())) {
            //FEPTXNTCB_TRTOCUST
            feptxntcb.setFeptxntcbTrtocust(cbsTota.getTRANSOUTCUST());
        }

        String PY_CHARGE_FLAG = this.getImsPropertiesValue(cbsTota, "CHAFEE_TYPE");
        if (StringUtils.isNotBlank(PY_CHARGE_FLAG)) {
            feptxntcb.setFeptxntcbPyChargeFlag(PY_CHARGE_FLAG);
        }
        String CHAFEE_AMT = this.getImsPropertiesValue(cbsTota, "CHAFEE_AMT");
        if (StringUtils.isNotBlank(CHAFEE_AMT)) {
            feptxntcb.setFeptxntcbChafeeamt(Integer.parseInt(CHAFEE_AMT));
        }
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
            int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

            if (insertCount <= 0 || insertCount2 <= 0) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                transactionManager.rollback(txStatus);
            } else {
                transactionManager.commit(txStatus);
            }
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }
        return rtnCode;
    }

    public FEPReturnCode updateFEPTxn(AB_ACC_O02 cbsTota, String txType) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.CBSCheckError;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        feptxn.setFeptxnErrMsg(cbsTota.getMEMO());
        feptxn.setFeptxnCbsTxTime(cbsTota.getOUTTIME());
        //TCB欄位紀錄
        feptxntcb.setFeptxntcbOutrtc(cbsTota.getOUTRTC()); //存款主機下送結果
        if (!txType.equals("4")) { //解除優惠時不能將存款主機的狀態覆蓋入扣帳主機狀態
            feptxn.setFeptxnCbsRc(cbsTota.getOUTRTC());
        }

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            int insertCount = this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
            int insertCount2 = this.feptxnDao.updateByPrimaryKeySelective(feptxntcb);

            if (insertCount <= 0 || insertCount2 <= 0) {
                rtnCode = FEPReturnCode.FEPTXNUpdateError;
                transactionManager.rollback(txStatus);
            } else {
                transactionManager.commit(txStatus);
            }
        } catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateFEPTxn");
            sendEMS(getLogContext());
            rtnCode = FEPReturnCode.FEPTXNUpdateError;
        }

        return rtnCode;
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

    private String parseAmtFor4e(String tota) {
        String TRANS_AMT = tota.substring(382, 384);
        String BLOCK_AMT = tota.substring(416, 418);
        String UNBLOCK_AMT = tota.substring(450, 452);
        String TRANSFROUTBAL = tota.substring(552, 554);
        String TRANSAMTOUT = tota.substring(586, 588);
        String TRANSAMTIN = tota.substring(620, 622);
        StringBuilder sb = new StringBuilder(tota);
        if (StringUtils.equals(TRANS_AMT, "40"))
            sb.replace(382, 384, "4E");

        if (StringUtils.equals(BLOCK_AMT, "40"))
            sb.replace(416, 418, "4E");

        if (StringUtils.equals(UNBLOCK_AMT, "40"))
            sb.replace(450, 452, "4E");

        if (StringUtils.equals(TRANSFROUTBAL, "40"))
            sb.replace(552, 554, "4E");

        if (StringUtils.equals(TRANSAMTOUT, "40"))
            sb.replace(586, 588, "4E");

        if (StringUtils.equals(TRANSAMTIN, "40"))
            sb.replace(620, 622, "4E");

        return sb.toString();
    }
}
