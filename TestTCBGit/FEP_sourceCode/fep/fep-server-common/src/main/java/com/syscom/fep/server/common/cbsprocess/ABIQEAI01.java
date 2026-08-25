package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.ImsMethodName;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.ims.CB_IQ_I001;
import com.syscom.fep.vo.text.ims.CB_IQ_O001;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.ParseException;
import java.util.Calendar;

public class ABIQEAI01 extends ACBSAction {

    public ABIQEAI01(MessageBase txType) {
        super(txType, new CB_IQ_I001());
    }

    //HCE沒有這個
//    ATMGeneralRequest atmReq = this.getAtmRequest();
    RCV_HCE_GeneralTrans_RQ hceReq = this.getHCERequest();
//    private SmsmsgExtMapper smsmsgExtMapper = SpringBeanFactoryUtil.getBean(SmsmsgExtMapper.class);

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq body = hceReq.getBody().getRq().getSvcRq();
        /* TITA 請參考合庫主機電文規格(AB_IQ_I001) */
        // Header
        CB_IQ_I001 cbsTita = new CB_IQ_I001();
        cbsTita.setIMS_TRANS(TxHelper.getIMSTRANSString(feptxn.getFeptxnChannel(), getHceData().getMessageID()));
//        cbsTita.setIMS_TRANS("MFEPEA00");
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        cbsTita.setTXN_FLOW("C"); // 自行
        cbsTita.setMSG_CAT(body.getTXNTYPE().substring(0,2));//上主機此欄位只有兩碼，只截取前兩馬上送。
        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        cbsTita.setPROCESS_TYPE("CHK");
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
        if ("HCA".equals(feptxn.getFeptxnChannel())) {
            cbsTita.setCARDTYPE("K"); // 交易卡片型態
        } else {
            cbsTita.setCARDTYPE("N"); // 交易卡片型態
        }
        feptxntcb.setFeptxntcbCardfmt(cbsTita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        cbsTita.setRESPONSE_CODE("0000"); // 正常才上送
        // Detail
        cbsTita.setICCHIPSTAN(feptxn.getFeptxnIcSeqno());
        cbsTita.setTERM_CHECKNO(feptxn.getFeptxnAtmChk());
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
        if (StringUtils.isNotBlank(body.getICMARK())) {
            cbsTita.setICMEMO(body.getICMARK());
        } else {
            cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        }
        // 合庫ATM代理:發卡行為006且交易卡片型態為”K，TAC須上送主機
        if (SysStatus.getPropertyValue().getSysstatHbkno().equals(feptxn.getFeptxnTroutBkno())
                && "K".equals(cbsTita.getCARDTYPE()) && "RQ".equals(body.getTXNTYPE())) { // 第一次上送CBS
            cbsTita.setTXNICCTAC(body.getIC_TAC_LEN() + body.getIC_TAC().substring(0,16));
            if ("HCA".equals(feptxn.getFeptxnChannel()) && StringUtils.isNotBlank(body.getIC_TAC_DATE())) {
                cbsTita.setTERMTXN_DATETIME(body.getIC_TAC_DATE() + body.getIC_TAC_TIME());
            }
        } else {
            cbsTita.setTXNICCTAC("4040404040404040404040404040404040404040");
        }
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); // 卡片帳號
        // I2(同ID其他帳戶查詢餘額)、I4(金融卡存款前置查詢的存入帳號)
        if ("I2".equals(feptxn.getFeptxnTxCode()) || "I4".equals(feptxn.getFeptxnTxCode())) {
            cbsTita.setINQELSEACT(feptxn.getFeptxnMajorActno());
            // I4交易要提供存款之存入銀行
            if ("I4".equals(feptxn.getFeptxnTxCode())) {
                cbsTita.setI4_CDMBANK(feptxn.getFeptxnTrinBkno());
            }
        }
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
        /* 電文內容格式請參照TOTA電文格式(AB_IQ_O001) */
        /* 拆解主機回應電文 */
        CB_IQ_O001 tota = new CB_IQ_O001();
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
    private FEPReturnCode updateFEPTxn(CB_IQ_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        if(StringUtils.isNotBlank(cbsTota.getSEND_FISC2160())){
            feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        }
        /* 變更FEPTXN交易記錄 */
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        String IMS_ERRPGM = this.getImsPropertiesValue(cbsTota, ImsMethodName.IMS_ERRPGM.getValue());
        if (StringUtils.isNotBlank(IMS_ERRPGM)) {
            feptxn.setFeptxnErrMsg(IMS_ERRPGM);
        }
        // IMSRC_TCB = "000" or empty表交易成功
        // IMSRC_FISC = "4001" 表交易成功
        if (!"000".equals(cbsTota.getIMSRC_TCB())) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            if("Y".equals(cbsTota.getIMSACCT_FLAG())){
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            }else {
                feptxn.setFeptxnAccType((short) 0); // 未記帳
            }
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
            rtnCode = FEPReturnCode.CBSCheckError;
        } else if (!StringUtils.equalsAny(cbsTota.getIMSRC4_FISC(), "4001")) {
            feptxn.setFeptxnCbsRc(cbsTota.getIMSRC_TCB());
            if("Y".equals(cbsTota.getIMSACCT_FLAG())){
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            }else {
                feptxn.setFeptxnAccType((short) 0); // 未記帳
            }
//            feptxn.setFeptxnErrMsg(cbsTota.getERR_MEMO());
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(NormalRC.CBS_OK);
            // CBS 帳務日(本行營業日,民國年須轉西元年)
            String imsbusinessDate = cbsTota.getIMSBUSINESS_DATE();
            if ("0000000".equals(imsbusinessDate) || "000000".equals(imsbusinessDate)
                    || (imsbusinessDate.length() != 6 && imsbusinessDate.length() != 7)) { // "0000000"不是日期格式，民國轉西元會轉成""，因此特殊處理
                imsbusinessDate = "00000000";
            } else {
                imsbusinessDate = CalendarUtil.rocStringToADString14(imsbusinessDate);
            }
            feptxn.setFeptxnTbsdy(imsbusinessDate);
            // 記帳類別
            if ("Y".equals(cbsTota.getIMSACCT_FLAG())) {
                feptxn.setFeptxnAccType((short) 1); // 已記帳
            } else {
                feptxn.setFeptxnAccType((short) 0);
            }
            // 主機交易時間
            feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
            // 可用餘額
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            // 帳戶餘額
            feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            // 手續費
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            // 交易通知方式
            if (StringUtils.isNotBlank(cbsTota.getNOTICE_TYPE())) {
                feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
                /* 寫入簡訊資料檔 */
                this.insertSMSMSG(cbsTota);
            }
            if (StringUtils.isNotBlank(cbsTota.getE_CLEANBRANCHOUT())) {
                feptxn.setFeptxnBrno(cbsTota.getE_CLEANBRANCHOUT());
            }

        }
        //更新TCB資料
        other_prepareUpdateIMSData(cbsTota);

        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
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
    private void insertSMSMSG(CB_IQ_O001 cbsTota) throws ParseException {
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
}
