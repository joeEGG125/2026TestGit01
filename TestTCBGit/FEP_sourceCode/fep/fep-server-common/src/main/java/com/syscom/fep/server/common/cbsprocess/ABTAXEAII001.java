package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.AB_TAX_EAI_I001;
import com.syscom.fep.vo.text.ims.AB_TAX_EAI_O001;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.text.ParseException;
import java.util.Calendar;

public class ABTAXEAII001 extends ACBSAction {

    public ABTAXEAII001(MessageBase txType) {
        super(txType, new AB_TAX_EAI_O001());
    }

    RCV_NB_GeneralTrans_RQ tita = this.getNBRequest();

    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(AB_TAX_EAI_I001) */
        // Header
        AB_TAX_EAI_I001 cbsTita = new AB_TAX_EAI_I001();
//        cbsTita.setIMS_TRANS("MFEPEA00");
        cbsTita.setIMS_TRANS(TxHelper.getIMSTRANSString(feptxn.getFeptxnChannel(), getLogContext().getMessageId()));
        cbsTita.setSYSCODE("FEP");
        cbsTita.setSYS_DATETIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN)); //格式:YYYYMMDDHHMMSS
        cbsTita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
        //TXN_FLOW
        cbsTita.setTXN_FLOW("A"); //代理

        if ("1".equals(txType)) { //入扣帳
            cbsTita.setMSG_CAT("10");
        }else{
            cbsTita.setMSG_CAT(tita.getBody().getRq().getSvcRq().getTXNTYPE());
        }

        cbsTita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
        cbsTita.setPCODE(feptxn.getFeptxnPcode());
        cbsTita.setFSCODE(feptxn.getFeptxnTxCode().trim());
        //PROCESS_TYPE
        if ("0".equals(txType)) { //查詢、檢核
            cbsTita.setPROCESS_TYPE("CHK");
            cbsTita.setRESPONSE_CODE("0000");
        } else if ("1".equals(txType)) { //入扣帳
            cbsTita.setPROCESS_TYPE("ACCT");
            cbsTita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
        }

        // 財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxn.getFeptxnTbsdyFisc());
        cbsTita.setBUSINESS_DATE(feptxnTbsdyFisc);
        cbsTita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan());
        cbsTita.setTERMINALID(feptxn.getFeptxnAtmno());
        cbsTita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
        cbsTita.setCARDTYPE("N"); // 交易卡片型態(EAIJ網銀)
        cbsTita.setTERMTXN_DATETIME(feptxn.getFeptxnReqDatetime()); // 交易日期時間
        feptxntcb.setFeptxntcbCardfmt(cbsTita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        cbsTita.setRESPONSE_CODE("0000"); // 正常才上送

        cbsTita.setTXNAMT(feptxn.getFeptxnTxAmt());
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno());

        // 2026.2.9 解決溢位問題 上送電文需要
        cbsTita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
        cbsTita.setTXNICCTAC("40404040404040404040");

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
        cbsTita.setTOACT(feptxn.getFeptxnTrinActno()); // 轉入帳號
        cbsTita.setTAX_TYPE(feptxn.getFeptxnPaytype()); // 繳款類別
        // 轉出帳號統編
        cbsTita.setAE_TRNSFROUTIDNO(tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO().length() == 11 ? tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO() : tita.getBody().getRq().getSvcRq().getTRNSFROUTIDNO());
        cbsTita.setAE_TRNSFLAG(tita.getBody().getRq().getSvcRq().getTRNSFLAG()); // 限額累計註記
        cbsTita.setAE_BUSINESSTYPE(tita.getBody().getRq().getSvcRq().getBUSINESSTYPE());
        // 業務類別
        /* 手續費負擔別 */
        if (StringUtils.equals(String.valueOf(feptxn.getFeptxnNpsClr()), "2")) {
            cbsTita.setAE_AEIEFEET("13");
        } else if (StringUtils.equals(String.valueOf(feptxn.getFeptxnNpsClr()), "3")) {
            cbsTita.setAE_AEIEFEET("14");
        } else if (StringUtils.equals(String.valueOf(feptxn.getFeptxnNpsClr()), "1")) {
            cbsTita.setAE_AEIEFEET("15");
        }

        // Detail
        if (StringUtils.isNotBlank(feptxn.getFeptxnPsbremFD())) {
            String trnsfrOutNote = feptxn.getFeptxnPsbremFD();
            String resizeTrnsfroutnote = "";
            //含全形字就全轉全形
            if(StringUtil.containsFullWidth(trnsfrOutNote)) {
                trnsfrOutNote = StringUtil.convertToFullwidth(trnsfrOutNote);
                //轉EBCDIC後函0E0F長度必須是36，所以以中文字計算最多8個中文字
                resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 8);
            }else{
                resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 18);
            }
            //轉EBCDIC後不足長度補足空白Hex"40"
            String resizeTrnsfroutnoteAfterChangeEbcdic = EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTrnsfroutnote, 36);
            cbsTita.setAE_TRNSFROUTNOTE(resizeTrnsfroutnoteAfterChangeEbcdic);
        }else{
            String trnsfroutnote = StringUtils.rightPad("", 36, "40");
            cbsTita.setAE_TRNSFROUTNOTE(trnsfroutnote);
        }
        // 以下欄位取端末上送值
        cbsTita.setAE_AEISLLTY(tita.getBody().getRq().getSvcRq().getSSLTYPE()); // 安控機制
        cbsTita.setAE_LIMITTYPE(tita.getBody().getRq().getSvcRq().getLIMITTYPE()); // 限額種類
        cbsTita.setAE_AEIFIXFE(tita.getBody().getRq().getSvcRq().getFAXFEE()); // 傳真手續費
        cbsTita.setAE_AEINETFE(tita.getBody().getRq().getSvcRq().getTRANSFEE()); // 跨網手續費
        // EDI應付他行手續費
        cbsTita.setAE_AEIEDIFE(tita.getBody().getRq().getSvcRq().getOTHERBANKFEE());
        cbsTita.setAE_AEICIRCU(tita.getBody().getRq().getSvcRq().getCUSTCODE()); // EDI用戶代碼
        cbsTita.setAE_FMMBR(feptxn.getFeptxnBrno()); // 轉出帳號清算分行

        if ("TY".equals(feptxn.getFeptxnTxCode())) { /* 核定稅非15類 */
            //String date = String.valueOf(Integer.parseInt(feptxn.getFeptxnDueDate()) - 19110000);
            String dueDate = StringUtils.leftPad(CalendarUtil.adStringToROCString(feptxn.getFeptxnDueDate()), 7) ; // 轉民國年
            cbsTita.setTAX_END_DATE(dueDate.substring(1, 7)); // 繳納截止日
            cbsTita.setTAX_BILL_NO(feptxn.getFeptxnReconSeqno()); // 銷帳編號
        } else if ("TZ".equals(feptxn.getFeptxnTxCode())) { /* 自繳稅15類 */
            cbsTita.setTAX_ORGAN(feptxn.getFeptxnTaxUnit()); // 稽徵機關
            cbsTita.setTAX_CID(feptxn.getFeptxnIdno()); // 身分證統編
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
        /* 電文內容格式請參照TOTA電文格式(AB_TAX_EAI_O001) */
        /* 拆解主機回應電文 */
        AB_TAX_EAI_O001 tota = new AB_TAX_EAI_O001();
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
    public FEPReturnCode updateFEPTxn(AB_TAX_EAI_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);
        if(StringUtils.isNotBlank(cbsTota.getSEND_FISC2160())){
            feptxn.setFeptxnSend2160(cbsTota.getSEND_FISC2160());
        }
        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
        // 2026.2.4 LeYun 使用other_prepareUpdateIMSData填入欄位
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

        //本行帳戶繳稅
        if ("2532".equals(feptxn.getFeptxnPcode())) {
            //帳戶餘額
            if (cbsTota.getACTBALANCE() != null && cbsTota.getACTBALANCE().signum() != 0) {
                feptxn.setFeptxnBalb(cbsTota.getACTBALANCE());
            }
            //可用餘額
            if (cbsTota.getAVAILABLE_BALANCE() != null && cbsTota.getAVAILABLE_BALANCE().signum() != 0) {
                feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE());
            }
        }
        if ("2532".equals(feptxn.getFeptxnPcode()) && "RQ".equals(tita.getBody().getRq().getSvcRq().getTXNTYPE())) { // 自行繳稅
            feptxn.setFeptxnTroutBkno7(cbsTota.getTAX_FROM_BRANCH());
        }

        //更新TCB資料
        other_prepareUpdateIMSData(getTota());

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
    private void insertSMSMSG(AB_TAX_EAI_O001 cbsTota) throws ParseException {
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
