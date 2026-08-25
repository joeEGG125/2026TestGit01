package com.syscom.fep.mybatis.ext.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 交易資料模型類別
 * @author Zonghao
 */
@Setter
@Getter
public class FeptxnMsgRsExt {
    // Getter 和 Setter 方法
    // 基本查詢資訊
    private String qryrc;           // 查詢結果 A:QrySuccess, F:QryFail, V:SecretKeyError
    private String stanNo;          // 交易序號(發動行+交易序號)
    private String txnDate;         // 交易日期(YYYYMMDD)
    private String txnTime;         // 交易時間(HH:MM:SS)
    private String tbsdy;           // 財金營業日
    private String bankNo;          // 代理銀行
    private String pcode;           // 財金PCODE
    private String fscode;          // TCB IFUNSEL
    private String atmNo;           // 交易地點
    private String atmType;         // 端末型態

    // 卡片資訊
    private String hceCardNo;       // HCE卡號
    private String troutCardNo;     // 卡片號碼
    private String cardFMT;         // 卡片種類

    // 交易資訊
    private String txnType;         // 交易類別
    private String atmSeqNo;        // 端末交易序號
    private String icSeqNo;         // IC卡交易序號
    private String txnCur;          // 交易幣別
    private String tcbFlow;         // 交易分類(CB、IB、AB)
    private String tcbtxnType;      // 交易類別(依本行分類)

    // 帳戶資訊
    private String troutBKNo;       // 轉出銀行
    private String troutACCNo;      // 轉出帳號
    private String trinBKNo;        // 轉入銀行
    private String trinACCNo;       // 轉入帳號

    // 金額資訊
    private String txnAmt;          // 交易金額
    private String txnFee;          // 交易手續費 or 2545台方手續費
    private BigDecimal txnRvsAmt;   // 沖正金額

    // 狀態資訊
    private String pbmcrd;          // 留置卡片
    private String acctStat;        // 記帳狀態
    private String rvsStat;         // 沖帳狀態
    private String troutBRNo;       // 轉出帳務分行
    private String trinBRNo;        // 轉入帳務分行
    private String txnRust;         // 交易狀態
    private String pending;         // 交易Pending
    private String aarc;            // AA回應碼
    private String accType;         // 記帳類別

    // 回應碼
    private String cbsrc3;          // 自行三碼回應碼
    private String cbsrc4;          // 自行四碼回應碼
    private String imsErrPgm;       // 主機失敗程式描述
    private String fiscrc;          // 跨行回應碼
    private String feprc;           // FEP回應端末代碼
    private String channel;         // 來源通道
    private String cbsTimeout;      // CBSTimeout
    private String fiscTimeout;     // FISCTimeout

    // 21XX、22XX、2573、2574、2549 原交易Data
    private String oriTxDate;       // 原交易日期
    private String oriTBSDY;        // 原營業日期
    private String oriPCODE;        // 原交易PCODE
    private String oriStanNo;       // 原交易的交易序號
    private String oriTxAmt;        // 原交易金額
    private String oriFISCRC;       // 跨行回應碼
    private String prcResult;       // 交易處理結果

    // 256X、226X、253X交易Data
    private String bussinessUnit;   // 委託單位代號
    private String payType;         // 繳費類別
    private String payNo;           // 費用代號
    private String reconSeqNo;      // 銷帳編號
    private String taxUNIT;         // 稽徵機關
    private String idNO;            // 統一編號 or FXML付款人身分證/統編(2270)


    // 2566 交易Data
    private String vaType;          // 交易類別
    private String vaCate;          // 業務類別
    private String vaAccNo;         // 核驗帳號
    private String vaItem;          // 核驗項目
    private String vaTxRust;        // 核驗結果
    private String vaAcRust;        // 帳號核驗結果
    private String vaAcStat;        // 開戶狀態
    private String vaTelRust;       // 行動電話核驗結果

    // 254X 交易Data
    private String merchantId;      // 特店代號
    private String deductPoint;     // 紅利折抵兌點
    private String redeemState;     // 紅利折抵開關
    private String extraAmt;        // 紅利折抵金額
    private String luckyNo;         // 活動代碼/非促銷金額
    private String txOriStan;       // 沖正原交易序號

    // 24xx、26xx 交易Data
    private String intlTxAmt;       // 原幣金額
    private String intlTxPosMode;   // 終端設備型態
    private String intlTxRRN;       // 查閱RRN(追蹤序號)
    private String intlTxOriStan;   // 沖正原交易序號
    private String intlTxSetAmt;    // 清算金額(美金)

    // 2700 交易Data
    private String fxmlToName;      // FXML收款人
    private String fxmlToIdno;      // FXML收款人身分證/統編
    private String fxmlFromName;    // FXML付款人
    private String fxmlMemo;        // FXML附言

    // 預設建構子
    public FeptxnMsgRsExt() {
    }

    @Override
    public String toString() {
        return  "  \"QRYRC\": \"" + Objects.toString(qryrc, "") + "\",\n" +
                "  \"StanNo\": \"" + Objects.toString(stanNo, "") + "\",\n" +
                "  \"TxnDate\": \"" + Objects.toString(txnDate, "") + "\",\n" +
                "  \"TxnTime\": \"" + Objects.toString(txnTime, "") + "\",\n" +
                "  \"TBSDY\": \"" + Objects.toString(tbsdy, "") + "\",\n" +
                "  \"BankNo\": \"" + Objects.toString(bankNo, "") + "\",\n" +
                "  \"PCODE\": \"" + Objects.toString(pcode, "") + "\",\n" +
                "  \"FSCODE\": \"" + Objects.toString(fscode, "") + "\",\n" +
                "  \"ATMNo\": \"" + Objects.toString(atmNo, "") + "\",\n" +
                "  \"ATMType\": \"" + Objects.toString(atmType, "") + "\",\n" +
                "  \"HCECardNo\": \"" + Objects.toString(hceCardNo, "") + "\",\n" +
                "  \"TroutCardNo\": \"" + Objects.toString(troutCardNo, "") + "\",\n" +
                "  \"CardFMT\": \"" + Objects.toString(cardFMT, "") + "\",\n" +
                "  \"TxnType\": \"" + Objects.toString(txnType, "") + "\",\n" +
                "  \"ATMSeqNo\": \"" + Objects.toString(atmSeqNo, "") + "\",\n" +
                "  \"ICSeqNo\": \"" + Objects.toString(icSeqNo, "") + "\",\n" +
                "  \"TxnCur\": \"" + Objects.toString(txnCur, "") + "\",\n" +
                "  \"TCBFlow\": \"" + Objects.toString(tcbFlow, "") + "\",\n" +
                "  \"TCBTxnType\": \"" + Objects.toString(tcbtxnType, "") + "\",\n" +
                "  \"TroutBKNo\": \"" + Objects.toString(troutBKNo, "") + "\",\n" +
                "  \"TroutACCNo\": \"" + Objects.toString(troutACCNo, "") + "\",\n" +
                "  \"TrinBKNo\": \"" + Objects.toString(trinBKNo, "") + "\",\n" +
                "  \"TrinACCNo\": \"" + Objects.toString(trinACCNo, "") + "\",\n" +
                "  \"TxnAmt\": \"" + Objects.toString(txnAmt, "") + "\",\n" +
                "  \"TxnFee\": \"" + Objects.toString(txnFee, "") + "\",\n" +
                "  \"TxnRvsAmt\": \"" + Objects.toString(txnRvsAmt, "") + "\",\n" +
                "  \"PBMCRD\": \"" + Objects.toString(pbmcrd, "") + "\",\n" +
                "  \"AcctStat\": \"" + Objects.toString(acctStat, "") + "\",\n" +
                "  \"RvsStat\": \"" + Objects.toString(rvsStat, "") + "\",\n" +
                "  \"TroutBRNo\": \"" + Objects.toString(troutBRNo, "") + "\",\n" +
                "  \"TrinBRNo\": \"" + Objects.toString(trinBRNo, "") + "\",\n" +
                "  \"TxnRust\": \"" + Objects.toString(txnRust, "") + "\",\n" +
                "  \"Pending\": \"" + Objects.toString(pending, "") + "\",\n" +
                "  \"Aarc\": \"" + Objects.toString(aarc, "") + "\",\n" +
                "  \"AccType\": \"" + Objects.toString(accType, "") + "\",\n" +
                "  \"CBSRC3\": \"" + Objects.toString(cbsrc3, "") + "\",\n" +
                "  \"CBSRC4\": \"" + Objects.toString(cbsrc4, "") + "\",\n" +
                "  \"IMSERRPGM\": \"" + Objects.toString(imsErrPgm, "") + "\",\n" +
                "  \"FISCRC\": \"" + Objects.toString(fiscrc, "") + "\",\n" +
                "  \"FEPRC\": \"" + Objects.toString(feprc, "") + "\",\n" +
                "  \"CHANNEL\": \"" + Objects.toString(channel, "") + "\",\n" +
                "  \"CBSTimeout\": \"" + Objects.toString(cbsTimeout, "") + "\",\n" +
                "  \"FISCTimeout\": \"" + Objects.toString(fiscTimeout, "") + "\",\n" +
                "  \"OriTxDate\": \"" + Objects.toString(oriTxDate, "") + "\",\n" +
                "  \"OriTBSDY\": \"" + Objects.toString(oriTBSDY, "") + "\",\n" +
                "  \"OriPCODE\": \"" + Objects.toString(oriPCODE, "") + "\",\n" +
                "  \"OriStanNo\": \"" + Objects.toString(oriStanNo, "") + "\",\n" +
                "  \"OriTxAmt\": \"" + Objects.toString(oriTxAmt, "") + "\",\n" +
                "  \"OriFISCRC\": \"" + Objects.toString(oriFISCRC, "") + "\",\n" +
                "  \"PrcResult\": \"" + Objects.toString(prcResult, "") + "\",\n" +
                "  \"BussinessUnit\": \"" + Objects.toString(bussinessUnit, "") + "\",\n" +
                "  \"PayType\": \"" + Objects.toString(payType, "") + "\",\n" +
                "  \"PayNo\": \"" + Objects.toString(payNo, "") + "\",\n" +
                "  \"ReconSeqNo\": \"" + Objects.toString(reconSeqNo, "") + "\",\n" +
                "  \"TaxUNIT\": \"" + Objects.toString(taxUNIT, "") + "\",\n" +
                "  \"IDNO\": \"" + Objects.toString(idNO, "") + "\",\n" +
                "  \"VaType\": \"" + Objects.toString(vaType, "") + "\",\n" +
                "  \"VaCate\": \"" + Objects.toString(vaCate, "") + "\",\n" +
                "  \"VaAccNo\": \"" + Objects.toString(vaAccNo, "") + "\",\n" +
                "  \"VaItem\": \"" + Objects.toString(vaItem, "") + "\",\n" +
                "  \"VaTxRust\": \"" + Objects.toString(vaTxRust, "") + "\",\n" +
                "  \"VaAcRust\": \"" + Objects.toString(vaAcRust, "") + "\",\n" +
                "  \"VaAcStat\": \"" + Objects.toString(vaAcStat, "") + "\",\n" +
                "  \"VaTelRust\": \"" + Objects.toString(vaTelRust, "") + "\",\n" +
                "  \"MerchantId\": \"" + Objects.toString(merchantId, "") + "\",\n" +
                "  \"DeductPoint\": \"" + Objects.toString(deductPoint, "") + "\",\n" +
                "  \"RedeemState\": \"" + Objects.toString(redeemState, "") + "\",\n" +
                "  \"ExtraAmt\": \"" + Objects.toString(extraAmt, "") + "\",\n" +
                "  \"LuckyNo\": \"" + Objects.toString(luckyNo, "") + "\",\n" +
                "  \"TxOriStan\": \"" + Objects.toString(txOriStan, "") + "\",\n" +
                "  \"IntlTxAmt\": \"" + Objects.toString(intlTxAmt, "") + "\",\n" +
                "  \"IntlTxPosMode\": \"" + Objects.toString(intlTxPosMode, "") + "\",\n" +
                "  \"IntlTxRRN\": \"" + Objects.toString(intlTxRRN, "") + "\",\n" +
                "  \"IntlTxOriStan\": \"" + Objects.toString(intlTxOriStan, "") + "\",\n" +
                "  \"INTLTxSetAmt\": \"" + Objects.toString(intlTxSetAmt, "") + "\",\n" +
                "  \"FXMLToName\": \"" + Objects.toString(fxmlToName, "") + "\",\n" +
                "  \"FXMLToIDNO\": \"" + Objects.toString(fxmlToIdno, "") + "\",\n" +
                "  \"FXMLFromName\": \"" + Objects.toString(fxmlFromName, "") + "\",\n" +
                "  \"FXMLMemo\": \"" + Objects.toString(fxmlMemo, "") + "\"\n";
    }
}