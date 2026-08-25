package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.text.fisc.FISC_INBK;
import com.syscom.fep.vo.text.ims.IB_PEND_I001;
import com.syscom.fep.vo.text.ims.IB_PEND_O001;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Objects;

/**
 * 組送CBS主機次日PENDING查詢交易結果電文
 *
 *
 *
 */
public class IBPENDI001 extends ACBSAction {

    public IBPENDI001(MessageBase txData) {
        super(txData, new IB_PEND_I001());
    }

    FISC_INBK inbkReq = this.getInbkRequest();

    /**
     * 1. 	組CBS Request電文
     * @param txtype
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txtype) throws Exception {
        //組CBS 原存交易Request電文, 電文內容格式請參照: D_I1_合庫FEP_主機電文規格-原存行跨國晶片提款國際提款交易V1.0(111xxxx).doc
        IB_PEND_I001 cbsTita = new IB_PEND_I001();
        // HEADER
        cbsTita.setIMS_TRANS("MFEPMS00"); // 主機業務別 長度8   //MFEPMS00
        cbsTita.setSYSCODE("FEP"); // 處理系統代號 長度4
        cbsTita.setSYS_DATETIME( //系統時間 長度14 格式:YYYYMMDDHHMMSS
                FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN));
        cbsTita.setFEP_EJNO(StringUtils.leftPad(Objects.toString(feptxn.getFeptxnEjfno()),8,"0")); // FEP電子日誌序號 長度8
        cbsTita.setTXN_FLOW("I"); // 交易分類 長度1
        cbsTita.setMSG_CAT("AF");// 電文訊息來源類別 長度2 //放: “AF”
        cbsTita.setSOURCE_CHANNEL(" "); // CHANNEL或業務別 長度3  //放空白
        cbsTita.setPCODE("2290"); // 財金P-CODE 長度4  //固定放2290
        cbsTita.setFSCODE(" "); // 合庫FS-CODE 長度2 原存行交易放空白  //放空白
        cbsTita.setPROCESS_TYPE("PND "); /* 帳務 */    //固定放PND
        cbsTita.setBUSINESS_DATE(" "); // 財金營業日(西元年須轉民國年) //放空白
        cbsTita.setACQUIRER_BANK(SysStatus.getPropertyValue().getSysstatHbkno()); // 設備代理行 長度3  //(FEP發動2290)
        cbsTita.setTXNSTAN(feptxn.getFeptxnStan()); // 跨行交易序號 長度7  //2290交易之交易序號
        cbsTita.setTERMINALID(" ");// 端末機代號 長度8  //放空白
        cbsTita.setTERMINAL_TYPE(" "); // 端末設備型態 長度4
        cbsTita.setCARDISSUE_BANK(" "); // 發卡行/扣款行 長度3
        cbsTita.setCARDTYPE(" "); // 交易卡片型態 長度1 /* K:晶片卡 */
        cbsTita.setRESPONSE_CODE("0001"); // 回應代號(RC) 長度4
        cbsTita.setATMTRANSEQ(" "); // ATM機器交易序號
        cbsTita.setHRVS(StringUtils.repeat(" ", 25)); // 保留欄位 長度25 2024/5/15修改
        /* CBS Request DETAIL */
        cbsTita.setORG_TX_AMT(feptxn.getFeptxnTxAmt()); // 原交易交易金額
        cbsTita.setORG_TX_TERMINALID(feptxn.getFeptxnAtmno()); // 原交易 端末機代號
        cbsTita.setORG_TX_STAN(inbkReq.getOriStan()); // 原交易 跨行交易序號
        cbsTita.setTRANS_RESULT(inbkReq.getRsCode()); // pending交易處理結果
        cbsTita.setI_ACT_BIT57(feptxn.getFeptxnAcctSup());// 帳戶補充資訊
        cbsTita.setDRVS(StringUtils.repeat(" ", 429)); // 保留欄位 429個空白

        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 2. 	拆解CBS  TOTA電文
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 拆解主機回應電文 */
        IB_PEND_O001 tota = new IB_PEND_O001();
        tota.parseCbsTele(cbsTota);
        this.setTota(tota);

        /* 更新FEPTXN */
        FEPReturnCode rtnCode = this.updateFEPTxn(tota, type);

        return rtnCode;
    }

    /**
     * 3. 	更新FEPTXN
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    private FEPReturnCode updateFEPTxn(IB_PEND_O001 cbsTota, String type) throws Exception {
        FEPReturnCode rtnCode;
        feptxn.setFeptxnCbsTxTime(cbsTota.getIMS_TXN_TIME());
//        feptxn.setFeptxnRepRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnCbsRc(cbsTota.getIMSRC4_FISC());
        feptxn.setFeptxnTxCode(cbsTota.getFSCODE()); /* 2024/6/14將主機下送FSCODE 存入 FEPTXN_TX_CODE */

        this.feptxn.setFeptxnMsgflow(this.feptxn.getFeptxnMsgflow().substring(0, 1) + "2");
        feptxn.setFeptxnCbsTimeout((short) 0); /* CBS 逾時 FLAG */
        //IMSRC_TCB = "000" or empty表交易成功
        if (StringUtils.isBlank(cbsTota.getIMSRC_TCB())) {
            cbsTota.setIMSRC_TCB("000");
        }

        if (!cbsTota.getIMSRC_TCB().equals("000")) {
            feptxn.setFeptxnAccType((short) 0); //未記帳
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            /* CBS回覆成功 */
            /* 2025/7/30 修改 */
            if ("N".equals(cbsTota.getORG_TX_ACCT_FLAG())) { // 主機記帳狀況
                feptxn.setFeptxnAccType((short) 0); //未記帳
            } else {
                /* 2025/7/10 修改 */
                if ("Y".equals(cbsTota.getORG_TX_RVS_FLAG())) {
                    feptxn.setFeptxnAccType((short) 2); //已更正
                } else {
                    feptxn.setFeptxnAccType((short) 1);  //已記帳
                }
            }
            this.feptxnDao.updateByPrimaryKeySelective(feptxn); // 更新
            rtnCode = FEPReturnCode.Normal;
        }
        return rtnCode;
    }
}
