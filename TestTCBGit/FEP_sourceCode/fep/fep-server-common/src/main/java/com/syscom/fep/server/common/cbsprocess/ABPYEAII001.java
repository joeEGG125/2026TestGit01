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
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.ims.AB_PY_EAI_I001;
import com.syscom.fep.vo.text.ims.AB_PY_EAI_I002;
import com.syscom.fep.vo.text.ims.AB_PY_EAI_O001;
import com.syscom.fep.vo.text.ims.AB_PY_EAI_O002;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 * 代理全繳交易上送主機
 *
 * @author vincent
 */

public class ABPYEAII001 extends ACBSAction {

    public ABPYEAII001(MessageBase txData) {
        super(txData, new AB_PY_EAI_O001());
    }

    RCV_NB_GeneralTrans_RQ atmReq = this.getNBRequest();
    private NpsunitExtMapper npsunitExtMapper = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);


    /**
     * 組CBS TITA電文
     * 電文內容格式請參照TITA電文格式(AB_-PY_-EAI_-I001)
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        if ("2561".equals(feptxn.getFeptxnPcode()) && "EF".equals(feptxn.getFeptxnTxCode().substring(0, 2))) {
            // 汽燃費 分開撰寫
            // 電文內容格式請參照TOTA電文格式(AB_PY_EAI_I002)
            // Header
            AB_PY_EAI_I002 cbstita = new AB_PY_EAI_I002();
            if ("NAM".equals(feptxn.getFeptxnChannel())) { //整批轉即時
                cbstita.setIMS_TRANS("MFEPNPY0");
            } else {
                cbstita.setIMS_TRANS("MFEPEA00");
            }

            cbstita.setSYSCODE("FEP");
            cbstita.setSYS_DATETIME(
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
            // TXN_FLOW
            if (feptxn.getFeptxnFiscFlag() == 0) {
                cbstita.setTXN_FLOW("C"); // 自行
            } else {
                cbstita.setTXN_FLOW("A"); // 代理
            }
            if("1".equals(txType) && feptxn.getFeptxnFiscFlag() == 1){
                cbstita.setMSG_CAT("10"); // 取財金回覆電文MSGID後兩碼
            }else if( "NAM".equals(feptxn.getFeptxnChannel()) ){
                cbstita.setMSG_CAT("BA");
            }else{
                cbstita.setMSG_CAT(atmReq.getBody().getRq().getSvcRq().getTXNTYPE().trim());
            }
            cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
            cbstita.setPCODE(feptxn.getFeptxnPcode());
            cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
            // PROCESS TYPE
            if ("0".equals(txType)) { // 查詢、檢核
                cbstita.setPROCESS_TYPE("CHK");
            } else if ("1".equals(txType)) { // 入扣帳
                cbstita.setPROCESS_TYPE("ACCT");
            } else if ("2".equals(txType)) { // 沖正
                if ("NAM".equals(this.feptxn.getFeptxnChannel())){
                    cbstita.setPROCESS_TYPE("PYRV");
                }else {
                    cbstita.setPROCESS_TYPE("RVS");
                }
            } else if ("4".equals(txType)) { // 解圈
                cbstita.setPROCESS_TYPE("REL");
            }
            // 財金營業日(西元年須轉民國年)
            String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);

            cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
            cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
            cbstita.setTXNSTAN(feptxn.getFeptxnStan());
            cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
            cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
            cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
            if ("NAM".equals(feptxn.getFeptxnChannel())) { //NPAY
                cbstita.setCARDTYPE("I");
            } else {
                cbstita.setCARDTYPE("N");
            }

            if ("2".equals(txType) && "NAM".equals(feptxn.getFeptxnChannel())) { //沖正
                cbstita.setRESPONSE_CODE(feptxn.getFeptxnCbsRc()); //主機逾時在AA給2999
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
                cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
            } else {
                cbstita.setRESPONSE_CODE("0000"); // 正常才上送
            }

            // Detail
            cbstita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
            cbstita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
            cbstita.setTXNICCTAC("40404040404040404040");
            cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
            cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
            cbstita.setTOACT(feptxn.getFeptxnTrinActno());
            if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
                cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
            } else {
                cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
            }
            if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
                cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
            } else {
                cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
            }
            if ("256".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 3)) // 繳費移轉計畫
                    && "18888888".equals(feptxn.getFeptxnBusinessUnit()) && "59999".equals(feptxn.getFeptxnPaytype())
                    && "9999".equals(feptxn.getFeptxnPayno()) && "99991231".equals(feptxn.getFeptxnDueDate())) {
                cbstita.setPY_SPECIAL_FLAG("ET");
            }

            /* 取得財金全繳委託單位檔之「帳務代理銀行」
               若為合庫自行(C)或代理(A)交易:
               委託單位代號="10000081"且繳費類別="00001"
               =>「帳務代理銀行代號」需改為”006”  =>   D-12欄位固定上送:  0060000
            */
            if ("10000081".equals(feptxn.getFeptxnBusinessUnit()) && "00001".equals(feptxn.getFeptxnPaytype())) {
                cbstita.setPY_HOST_BRANCH("0060000");
            } else {
                Npsunit npsunit = npsunitExtMapper.selectByPrimaryKey(
                        feptxn.getFeptxnBusinessUnit(),
                        feptxn.getFeptxnPaytype(),
                        feptxn.getFeptxnPayno());
                cbstita.setPY_HOST_BRANCH(npsunit != null ? StringUtils.defaultIfBlank(npsunit.getNpsunitBkno(), "") : "");
            }
            cbstita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
            cbstita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
            cbstita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
            cbstita.setPY_IDNO(feptxn.getFeptxnIdno());
            cbstita.setPY_PAYTXNOL(feptxn.getFeptxnReconSeqno()); // 銷帳編號
            cbstita.setPY_PAYDDATE(feptxn.getFeptxnDueDate()); // 繳款期限
            if ("A".equals(cbstita.getTXN_FLOW()) // 代理記帳須提供
                    && "ACCT".equals(cbstita.getPROCESS_TYPE())) {
                //繳費作業手續費
                DecimalFormat df = new DecimalFormat("0000");
                cbstita.setPY_CHARGCUS(new BigDecimal(df.format(feptxn.getFeptxnNpsFeeCustpay())));
                //組合成財金response Data bit#50
                cbstita.setPY_CHARGUNT(new BigDecimal(
                        df.format(feptxn.getFeptxnNpsFeeRcvr().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeAgent().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeTrout().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeTrin().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeFisc().multiply(new BigDecimal("10")))));
            }
            //汽燃費
            cbstita.setAE_TRNSFROUTIDNO(feptxn.getFeptxnIdno()); //轉出帳號統編
            cbstita.setAE_TRNSFLAG(atmReq.getBody().getRq().getSvcRq().getTRNSFLAG()); //限額累計註記
            cbstita.setAE_BUSINESSTYPE(atmReq.getBody().getRq().getSvcRq().getBUSINESSTYPE()); //業務類別
            cbstita.setAE_AEIEFEET(atmReq.getBody().getRq().getSvcRq().getFEEPAYMENTTYPE()); /* 手續費負擔別*/
            if (StringUtils.isNotBlank(feptxn.getFeptxnPsbremFD())) { //付款人自我備註
                String trnsfrOutNote = feptxn.getFeptxnPsbremFD();
                String resizeTrnsfroutnote = "";
                //含全形字就全轉全形
                if(StringUtil.containsFullWidth(trnsfrOutNote)){
                    trnsfrOutNote = StringUtil.convertToFullwidth(trnsfrOutNote);
                    //轉EBCDIC後函0E0F長度必須是36，所以以中文字計算最多8個中文字
                    resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 8);
                }else{
                    resizeTrnsfroutnote = StringUtil.adjustChineseStringLength(trnsfrOutNote, 18);
                }
                //轉EBCDIC後不足長度補足空白Hex"40"
                String resizeTrnsfroutnoteAfterChangeEbcdic = EbcdicConverter.toTraditionalChineseHexbyZibaFillSpace(resizeTrnsfroutnote, 36);
                cbstita.setAE_TRNSFROUTNOTE(resizeTrnsfroutnoteAfterChangeEbcdic);
            }else{
                String trnsfroutnote = StringUtils.rightPad("", 36, "40");
                cbstita.setAE_TRNSFROUTNOTE(trnsfroutnote);
            }

            cbstita.setAE_LIMITTYPE(atmReq.getBody().getRq().getSvcRq().getLIMITTYPE()); //限額種類
            cbstita.setAE_PYAQBRH(feptxn.getFeptxnBrno()); //汽燃費設備代理分行
            this.setoTita(cbstita);
            this.setTitaToString(cbstita.makeMessage());
            this.setASCIItitaToString(cbstita.makeMessageAscii());
            feptxntcb.setFeptxntcbCardfmt(cbstita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        } else {
            // 電文內容格式請參照TOTA電文格式(AB_PY_EAI_I001)
            // Header
            AB_PY_EAI_I001 cbstita = new AB_PY_EAI_I001();
            if ("NAM".equals(feptxn.getFeptxnChannel())) { //整批轉即時
                cbstita.setIMS_TRANS("MFEPNPY0");
            } else {
                cbstita.setIMS_TRANS("MFEPEA00");
            }

            cbstita.setSYSCODE("FEP");
            cbstita.setSYS_DATETIME(
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
            cbstita.setFEP_EJNO(StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()),8,"0"));
            // TXN_FLOW
            if (feptxn.getFeptxnFiscFlag() == 0) {
                cbstita.setTXN_FLOW("C"); // 自行
            } else {
                cbstita.setTXN_FLOW("A"); // 代理
            }
            if("1".equals(txType) && feptxn.getFeptxnFiscFlag() == 1){
                cbstita.setMSG_CAT("10"); // 取財金回覆電文MSGID後兩碼
            }else if( "NAM".equals(feptxn.getFeptxnChannel()) ){
                cbstita.setMSG_CAT("BA");
            }else{
                cbstita.setMSG_CAT(atmReq.getBody().getRq().getSvcRq().getTXNTYPE().trim());
            }
            cbstita.setSOURCE_CHANNEL(feptxn.getFeptxnChannel());
            cbstita.setPCODE(feptxn.getFeptxnPcode());
            cbstita.setFSCODE(feptxn.getFeptxnTxCode().trim());
            // PROCESS TYPE
            if ("0".equals(txType)) { // 查詢、檢核
                cbstita.setPROCESS_TYPE("CHK");
            } else if ("1".equals(txType)) { // 入扣帳
                cbstita.setPROCESS_TYPE("ACCT");
            } else if ("2".equals(txType)) { // 沖正
                if ("NAM".equals(this.feptxn.getFeptxnChannel())){
                    cbstita.setPROCESS_TYPE("PYRV");
                }else {
                    cbstita.setPROCESS_TYPE("RVS");
                }
            } else if ("4".equals(txType)) { // 解圈
                cbstita.setPROCESS_TYPE("REL");
            }
            // 財金營業日(西元年須轉民國年)
            String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);

            cbstita.setBUSINESS_DATE(feptxnTbsdyFisc);
            cbstita.setACQUIRER_BANK(feptxn.getFeptxnBkno());
            cbstita.setTXNSTAN(feptxn.getFeptxnStan());
            cbstita.setTERMINALID(feptxn.getFeptxnAtmno());
            cbstita.setTERMINAL_TYPE(feptxn.getFeptxnAtmType());
            cbstita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno());
            if ("NAM".equals(feptxn.getFeptxnChannel())) { //NPAY
                cbstita.setCARDTYPE("I");
            } else {
                cbstita.setCARDTYPE("N");
            }

            if ("2".equals(txType) && "NAM".equals(feptxn.getFeptxnChannel())) { //沖正
                cbstita.setRESPONSE_CODE(feptxn.getFeptxnCbsRc()); //主機逾時在AA給2999
            } else if (StringUtils.isNotBlank(feptxn.getFeptxnRepRc())) {
                cbstita.setRESPONSE_CODE(feptxn.getFeptxnRepRc());
            } else {
                cbstita.setRESPONSE_CODE("0000"); // 正常才上送
            }

            // Detail
            cbstita.setTERMTXN_DATETIME(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime());
            cbstita.setICMEMO("404040404040404040404040404040404040404040404040404040404040");
            cbstita.setTXNICCTAC("40404040404040404040");
            cbstita.setTXNAMT(feptxn.getFeptxnTxAmt());
            cbstita.setFROMACT(feptxn.getFeptxnTroutActno());
            cbstita.setTOACT(feptxn.getFeptxnTrinActno());
            if (StringUtils.isNotBlank(feptxn.getFeptxnTrinBkno7())) {
                cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno7());
            } else {
                cbstita.setTRIN_BANKNO(feptxn.getFeptxnTrinBkno() + "0000");
            }
            if (StringUtils.isNotBlank(feptxn.getFeptxnTroutBkno7())) {
                cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno7());
            } else {
                cbstita.setTROUT_BANKNO(feptxn.getFeptxnTroutBkno() + "0000");
            }
            if ("256".equals(StringUtils.substring(feptxn.getFeptxnPcode(), 0, 3)) // 繳費移轉計畫
                    && "18888888".equals(feptxn.getFeptxnBusinessUnit()) && "59999".equals(feptxn.getFeptxnPaytype())
                    && "9999".equals(feptxn.getFeptxnPayno()) && "99991231".equals(feptxn.getFeptxnDueDate())) {
                cbstita.setPY_SPECIAL_FLAG("ET");
            }
            /* 取得財金全繳委託單位檔之「帳務代理銀行」
               若為合庫自行(C)或代理(A)交易:
               委託單位代號="10000081"且繳費類別="00001"
               =>「帳務代理銀行代號」需改為”006”  =>   D-12欄位固定上送:  0060000
            */
            if ("10000081".equals(feptxn.getFeptxnBusinessUnit()) && "00001".equals(feptxn.getFeptxnPaytype())) {
                cbstita.setPY_HOST_BRANCH("0060000");
            } else {
                Npsunit npsunit = npsunitExtMapper.selectByPrimaryKey(
                        feptxn.getFeptxnBusinessUnit(),
                        feptxn.getFeptxnPaytype(),
                        feptxn.getFeptxnPayno());
                cbstita.setPY_HOST_BRANCH(npsunit != null ? StringUtils.defaultIfBlank(npsunit.getNpsunitBkno(), "") : "");
            }
            cbstita.setPY_PAYUNTNO(feptxn.getFeptxnBusinessUnit()); // 委託單位代號
            cbstita.setPY_TAXTYPE(feptxn.getFeptxnPaytype()); // 繳費類別
            cbstita.setPY_PAYFEENO(feptxn.getFeptxnPayno()); // 費用代號
            cbstita.setPY_IDNO(feptxn.getFeptxnIdno());
            cbstita.setPY_PAYTXNOL(feptxn.getFeptxnReconSeqno()); // 銷帳編號
            cbstita.setPY_PAYDDATE(feptxn.getFeptxnDueDate()); // 繳款期限
            if ("A".equals(cbstita.getTXN_FLOW()) // 代理記帳須提供
                    && "ACCT".equals(cbstita.getPROCESS_TYPE())) {
                //繳費作業手續費
                DecimalFormat df = new DecimalFormat("0000");
                cbstita.setPY_CHARGCUS(new BigDecimal(df.format(feptxn.getFeptxnNpsFeeCustpay())));
                //組合成財金response Data bit#50
                cbstita.setPY_CHARGUNT(new BigDecimal(
                        df.format(feptxn.getFeptxnNpsFeeRcvr().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeAgent().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeTrout().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeTrin().multiply(new BigDecimal("10"))) +
                                df.format(feptxn.getFeptxnNpsFeeFisc().multiply(new BigDecimal("10")))));
            }

            cbstita.setChannel_Charge(BigDecimal.valueOf(0));
            cbstita.setAE_PYAQBRH(feptxn.getFeptxnBrno()); //全繳設備代理分行(轉出帳號清算分行)
            cbstita.setPY_SSLTYPE(atmReq.getBody().getRq().getSvcRq().getSSLTYPE()); //安控機制
            this.setoTita(cbstita);
            this.setTitaToString(cbstita.makeMessage());
            this.setASCIItitaToString(cbstita.makeMessageAscii());
            feptxntcb.setFeptxntcbCardfmt(cbstita.getCARDTYPE()); // 紀錄在FEPTXNTCB
        }

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
        /* 拆解主機回應電文 */
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if ("2561".equals(feptxn.getFeptxnPcode()) && "EF".equals(feptxn.getFeptxnTxCode().substring(0, 2))) {
            //汽燃費
            /* 電文內容格式請參照TOTA電文格式(AB_PY_O002) */
            AB_PY_EAI_O002 tota = new AB_PY_EAI_O002();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            /* 更新交易 */
            rtnCode = this.updateFEPTxn(tota);
        } else {
            /* 電文內容格式請參照TOTA電文格式(AB_PY_O001) */
            AB_PY_EAI_O001 tota = new AB_PY_EAI_O001();
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
     * 更新交易
     * AB_PY_EAI_O001
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxn(AB_PY_EAI_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);

        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
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
        //20260514 修改轉出方為本行時取主機下送手續費
        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        if ("NAM".equals(feptxn.getFeptxnChannel())) {
            //整批轉即時交易，取主機下送的事業單位應付手續費
            feptxn.setFeptxnFeeCustpay(cbsTota.getPY_HOST_CHARGE());
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getPY_HOST_CHARGE());
        } else if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), sysstatHbkno)) {
            // 轉出行為本行，取主機下送的手續費
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
        } else if (feptxn.getFeptxnPaytype().compareTo("00001") >= 0 && feptxn.getFeptxnPaytype().compareTo("49999") <= 0) {
            // 轉出行為他行，判斷繳費類別範圍內手續費為0，範圍外取財金手續費(REP時已記錄)
            feptxn.setFeptxnFeeCustpay(BigDecimal.valueOf(0));
            feptxn.setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
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
        // 更新
        return rtnCode;
    }

    /**
     * 更新交易
     * AB_PY_EAI_O002
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxn(AB_PY_EAI_O002 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        feptxn.setFeptxnMsgflow(StringUtils.substring(feptxn.getFeptxnMsgflow(), 0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0);

        /* 變更FEPTXN交易記錄 */
        // IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }
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
        //20260514 修改轉出方為本行時取主機下送手續費
        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        if ("NAM".equals(feptxn.getFeptxnChannel())) {
            //整批轉即時交易，取主機下送的事業單位應付手續費
            feptxn.setFeptxnFeeCustpay(cbsTota.getPY_HOST_CHARGE());
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getPY_HOST_CHARGE());
        } else if (StringUtils.equals(feptxn.getFeptxnTroutBkno(), sysstatHbkno)) {
            // 轉出行為本行，取主機下送的手續費
            feptxn.setFeptxnFeeCustpay(cbsTota.getTXNCHARGE());
            feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE());
        } else if (feptxn.getFeptxnPaytype().compareTo("00001") >= 0 && feptxn.getFeptxnPaytype().compareTo("49999") <= 0) {
            // 轉出行為他行，判斷繳費類別範圍內手續費為0，範圍外取財金手續費(REP時已記錄)
            feptxn.setFeptxnFeeCustpay(BigDecimal.valueOf(0));
            feptxn.setFeptxnFeeCustpayAct(BigDecimal.valueOf(0));
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
        // 更新
        return rtnCode;
    }

    /**
     * 新增交易通知
     *
     * @param cbsTota
     * @throws ParseException
     */
    private void insertSMSMSG(AB_PY_EAI_O001 cbsTota) throws ParseException {
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
//            smsmsg.setSmsmsgZone("« TWN »");
//            smsmsg.setSmsmsgEmail(cbsTota.getNOTICE_EMAIL());
//            smsmsg.setSmsmsgIdno(cbsTota.getNOTICE_CUSID());
//            smsmsg.setSmsmsgTxCur(feptxn.getFeptxnTxCur());
//            smsmsg.setSmsmsgTxAmt(feptxn.getFeptxnTxAmt());
//            smsmsg.setSmsmsgTxCurAct(feptxn.getFeptxnTxCurAct());
//            smsmsg.setSmsmsgTxAmtAct(feptxn.getFeptxnTxAmtAct());
//            smsmsg.setSmsmsgSmsPhone(cbsTota.getNOTICE_MOBILENO());
//            smsmsg.setSmsmsgNotifyFg("« Y »");
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
