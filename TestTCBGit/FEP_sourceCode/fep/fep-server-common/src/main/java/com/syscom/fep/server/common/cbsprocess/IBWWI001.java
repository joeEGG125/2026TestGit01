package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.enums.CurrencyType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.text.ims.IB_WW_I001;
import com.syscom.fep.vo.text.ims.IB_WW_O001;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS 主機原存行跨國提款Request交易電文
 *
 * @author Joseph
 */

public class IBWWI001 extends ACBSAction {

    public IBWWI001(MessageBase txData) {
        super(txData, new IB_WW_I001());
    }


    /**
     * 組CBS 原存交易Request電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        // 組CBS 原存交易Request電文, 電文內容格式請參照: D_I9_T1_合庫FEP_主機電文規格-原存行-跨國提款交易V1.0(1111114).doc
        IB_WW_I001 cbsTita = new IB_WW_I001();
        Intltxn intltxn = this.getFiscData().getIntlTxn();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPFWD0"); // 主機業務別 長度8  2024/12/16 配合主機修改
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()), 8, "0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("00");// 電文訊息來源類別 長度2 // FISC 電文之MSGTYPE /* 20220920 電文不共用, 給0O */
        cbsTita.setSOURCE_CHANNEL("FIS"); // CHANNEL或業務別 長度3
        cbsTita.setPCODE(feptxn.getFeptxnPcode()); // 財金P-CODE 長度4
        cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白
        cbsTita.setPROCESS_TYPE("ACCT"); /* 帳務 */
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
        cbsTita.setTERMINAL_TYPE(intltxn.getIntltxnPosMode()); // 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(feptxn.getFeptxnTroutBkno()); // 發卡行/扣款行 長度3
        cbsTita.setCARDTYPE("2"); // 交易卡片型態 長度1 /* 跨國磁條24XX */
        cbsTita.setRESPONSE_CODE(feptxn.getFeptxnReqRc()); // 回應代號(RC) 長度4
        cbsTita.setATM_TRANSEQ(StringUtils.repeat(" ", 4)); // ATM機器交易序號 原存交易未使用放空白 2024/5/15修改
        cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
        /* CBS Request DETAIL */
        cbsTita.setICCHIPSTAN(StringUtils.repeat("0", 1)); // IC卡交易序號
        cbsTita.setTERM_CHECKNO(StringUtils.repeat(" ", 8)); // 端末設備查核碼
        cbsTita.setTERMTXN_DATETIME(StringUtils.repeat("0", 14)); // 交易日期時間
        cbsTita.setICMEMO(StringUtils.repeat(" ", 30)); // IC卡備註欄
        cbsTita.setTXNICCTAC(StringUtils.repeat(" ", 10)); // 交易驗證碼
        cbsTita.setTXNAMT(BigDecimal.ZERO); // 交易金額
        /* 2024/8/23 修改 */
        if ( "601517".equals(feptxn.getFeptxnTrk2().substring(0,6))) {
            feptxn.setFeptxnTroutActno("000" + feptxn.getFeptxnTrk2().substring(6,19));
        }else{  /* PCODE=2410/2450 */
            feptxn.setFeptxnTroutActno(feptxn.getFeptxnTrk2().substring(0,16));  // COMBO卡/VISA卡號
        }
        cbsTita.setFROMACT(feptxn.getFeptxnTroutActno()); //卡片提款帳號
        cbsTita.setTRK2(intltxn.getIntltxnTrk2()); // 磁軌資料
        /* 2023/8/8 修改for 改用財金電文PINBLOCK欄位 */
        /* 2023/12/12 修改for 財金PINBLOCK欄位轉換至IMS主機 */
        if (StringUtils.isNotBlank(this.getInbkRequest().getPINBLOCK())) {
            RefString W_PINBLOCK = new RefString(null);
            this.logContext.setRemark("Start ConvertFISCPinToHost");
            logMessage(this.logContext);
            FEPReturnCode returnCode = FEPReturnCode.ENCPINBlockConvertError;
            ENCHelper encHelper = new ENCHelper(getGeneralData());
            returnCode = encHelper.ConvertFISCPinToHost(this.getInbkRequest().getPINBLOCK(), W_PINBLOCK, feptxn.getFeptxnPcode());
            if (returnCode != FEPReturnCode.Normal) {
                return FEPReturnCode.ENCPINBlockConvertError;
            } else {
                cbsTita.setPINBLOCK(W_PINBLOCK.get());
            }
        }
        /* 2024/8/21 修改 */
        cbsTita.setBIT36_OGSLAMT(intltxn.getIntltxnSetAmt()); // 清算金額
        cbsTita.setBIT36_OGTXAMT(new BigDecimal(this.getInbkRequest().getOriData().substring(42,54))); // 原始交易金額
        cbsTita.setBIT36_OGTXCCD(this.getInbkRequest().getOriData().substring(66,69)); // 原始交易幣別碼
        cbsTita.setBIT36_OGSLCCD(this.getInbkRequest().getOriData().substring(69,72)); // 清算幣別碼
        cbsTita.setBIT36_OGSLCRT(this.getInbkRequest().getOriData().substring(72,80)); // 清算匯率
        cbsTita.setBIT36_RPSLAMT(new BigDecimal(this.getInbkRequest().getOriData().substring(148,160))); // 實際完成之交易清算金額
        /* 2024/8/22 修改 */
        /* 2025/12/22 修改for 合庫增加 ARQC 欄位 */
        if(FISCPCode.PCode2410.getValueStr().equals(feptxn.getFeptxnPcode())) {
            cbsTita.setBIT36_CURRN(this.getInbkRequest().getOriData().substring(160, 172)); // RRN
            /* 2026/01/02 修改for 2410 RRN 寫入 INTLTXN */
            intltxn.setIntltxnRrn(this.getInbkRequest().getOriData().substring(160, 172));
            /*取得原始電文TRK2位置[143:4]再由EBCDIC 轉成 ASCII*/
            String TRK2_EBCDIC = this.getInbkRequest().getFISCMessage().substring(148,356);
            cbsTita.setARQC( EbcdicConverter.fromHex(TRK2_EBCDIC.substring(142,146)) );

            getLogContext().setRemark("TRK2_EBCDIC:"+TRK2_EBCDIC);
            logMessage(getLogContext());
            getLogContext().setRemark("ARQC_EBCDIC:"+TRK2_EBCDIC.substring(142,146)+",ARQC:"+ cbsTita.getARQC());
            logMessage(getLogContext());
        }else {
            cbsTita.setBIT36_CURRN(StringUtils.repeat(" ", 12)); // RRN
            /*取得原始財金電文TRK2位置[127 :16]再由EBCDIC 轉成 ASCII*/
            String TRK2_EBCDIC = this.getInbkRequest().getFISCMessage().substring(148,356);
            cbsTita.setARQC(  EbcdicConverter.fromHex(TRK2_EBCDIC.substring(126,142)) );

            getLogContext().setRemark("TRK2_EBCDIC:"+TRK2_EBCDIC);
            logMessage(getLogContext());
            getLogContext().setRemark("ARQC_EBCDIC:"+TRK2_EBCDIC.substring(126,142)+",ARQC:"+ cbsTita.getARQC());
            logMessage(getLogContext());
        }

        /* 2025/12/22 修改 for 合庫增加 POS ENTRY MODE 欄位*/
        cbsTita.setBIT36_POSENTRYMODE(this.getInbkRequest().getOriData().substring(183,187));
        /* 2025/12/22 修改 for 合庫增加2個欄位 */
        cbsTita.setDRVS(StringUtils.repeat(" ", 194)); // 保留欄位

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
        /* 電文內容格式請參照TOTA電文格式(IB_WW_O001) */
        /* 拆解主機回應電文 */
        IB_WW_O001 tota = new IB_WW_O001();
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
    private FEPReturnCode updateFEPTxn(IB_WW_O001 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME()); /* 主機交易時間 */
        feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
        /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */
        feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); 
        feptxn.setFeptxnNoticeType(cbsTota.getNOTICE_TYPE());
        feptxn.setFeptxnBalb(cbsTota.getACT_BALANCE()); /* 帳戶餘額 */
        if (cbsTota.getAVAILABLE_BALANCE() != null) {
            feptxn.setFeptxnBala(cbsTota.getAVAILABLE_BALANCE()); /* 可用餘額 */
        }
        feptxn.setFeptxnAuthcd(cbsTota.getAUTHCD()); /* 授權碼 */
        /* 2025/1/17 新增, 寫入以下欄位 */
        feptxn.setFeptxnTxCurAct(CurrencyType.TWD.name());
        feptxn.setFeptxnTxAmtAct(cbsTota.getTXNAMT());  /* 實際扣客戶金額(轉台幣) */
        feptxn.setFeptxnFeeCur(CurrencyType.TWD.name());
        feptxn.setFeptxnFeeCustpayAct(cbsTota.getTXNCHARGE()); /* 跨國提款-客戶手續費 */
        /* 2025/3/24將主機下送FROMACT  寫入轉出帳號 */
        feptxn.setFeptxnTroutActno(cbsTota.getFROMACT()); /* 卡號轉換為實體帳號 */
        /* 2025/4/17將主機下送FROMACT  寫入卡片帳號 */
        feptxn.setFeptxnMajorActno(cbsTota.getFROMACT());
        /* 2025/8/25 修改 for 主機回應交易成功, 才寫入合庫美元匯率 */
        if ( "4001".equals(feptxn.getFeptxnRepRc()) ) {
            feptxn.setFeptxnExrate(cbsTota.getB006RATE());  /* 合庫美元匯率 */
        }
        /* 2025/2/11 新增沖銷使用欄位, 寫入以下欄位 */
        feptxn.setFeptxnMerchantId(cbsTota.getRVTXNUSE()); /*沖銷使用欄位 */
        /* 變更交易記錄 */
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
            }
            rtnCode = FEPReturnCode.Normal;
        }
        
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
