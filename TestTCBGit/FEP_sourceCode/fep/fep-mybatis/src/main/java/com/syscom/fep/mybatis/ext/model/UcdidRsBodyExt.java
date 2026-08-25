package com.syscom.fep.mybatis.ext.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 交易資料模型類別
 * @author Zonghao
 */
@Setter
@Getter
public class UcdidRsBodyExt {
    // Body
    private String txType;          // 交易類型
    private String txStan;          // 交易序號(2566、2510)
    private String txRust;          // 驗證結果(2566)/提領結果(2510)
    private String bkTbsdy;         // 帳務日(2566、2510)
    private String ucdapiSeqNo;     // 財金API交易序號(API1/API2)
    private String ucdTxnNo;        // 財金回覆處理序號(API1/API2)
    private String ucdRespTime;     // 財金回覆時間(API1/API2)
    private String ucdrcCode;       // 財金回覆代碼(API1/API2)

    // 預設建構子
    public UcdidRsBodyExt() {
    }

    @Override
    public String toString() {
        return "{\n" +
                "  \"TxType\": \"" + Objects.toString(txType, "") + "\",\n" +
                "  \"TxStan\": \"" + Objects.toString(txStan, "") + "\",\n" +
                "  \"TxRust\": \"" + Objects.toString(txRust, "") + "\",\n" +
                "  \"BkTbsdy\": \"" + Objects.toString(bkTbsdy, "") + "\",\n" +
                "  \"UCDAPISeqNo\": \"" + Objects.toString(ucdapiSeqNo, "") + "\",\n" +
                "  \"UCDTxnNo\": \"" + Objects.toString(ucdTxnNo, "") + "\",\n" +
                "  \"UCDRespTime\": \"" + Objects.toString(ucdRespTime, "") + "\",\n" +
                "  \"UCDRC\": \"" + Objects.toString(ucdrcCode, "") + "\"\n" +
                "}";
    }
}