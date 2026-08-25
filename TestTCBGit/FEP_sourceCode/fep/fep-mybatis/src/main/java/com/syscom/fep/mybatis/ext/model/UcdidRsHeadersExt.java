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
public class UcdidRsHeadersExt {
    // Header
    private String qryrc;           // 查詢結果
    private String idNo;            // 身分證字號
    private String healthId;        // 健保卡號
    private String status2510;      // 領現狀態
    private String ucdrStatus;      // 沖正狀態
    private String actNo;           // 金融卡帳號
    private String bkNo;            // 發卡行
    private String atmNo;           // ATM代號
    private String atmBkNo;         // 設機行代號
    private String dateTime2510;    // 領取時間
    private String ucdrc;           // 領取訊息
    private String ucdrDateTime;    // 沖正時間(YYYY/MM/DD hh:mm:ss)
    private String ucdrrc;          // 沖正訊息(API接收的代碼及說明文字)
    private String ucdrChannel;     // 沖正管道(ATM／Web／批次)
    private String lastStatus;      // 最後交易狀態(領取／沖正)

    // 預設建構子
    public UcdidRsHeadersExt() {
    }

    @Override
    public String toString() {
        return "{\n" +
                "  \"QryRC\": \"" + Objects.toString(qryrc, "") + "\",\n" +
                "  \"IDNo\": \"" + Objects.toString(idNo, "") + "\",\n" +
                "  \"HealthId\": \"" + Objects.toString(healthId, "") + "\",\n" +
                "  \"Status2510\": \"" + Objects.toString(status2510, "") + "\",\n" +
                "  \"UCDRStatus\": \"" + Objects.toString(ucdrStatus, "") + "\",\n" +
                "  \"ActNo\": \"" + Objects.toString(actNo, "") + "\",\n" +
                "  \"BkNo\": \"" + Objects.toString(bkNo, "") + "\",\n" +
                "  \"ATMNo\": \"" + Objects.toString(atmNo, "") + "\",\n" +
                "  \"ATMBkNo\": \"" + Objects.toString(atmBkNo, "") + "\",\n" +
                "  \"DateTime2510\": \"" + Objects.toString(dateTime2510, "") + "\",\n" +
                "  \"UCDRC\": \"" + Objects.toString(ucdrc, "") + "\",\n" +
                "  \"UCDRDateTime\": \"" + Objects.toString(ucdrDateTime, "") + "\",\n" +
                "  \"UCDRRC\": \"" + Objects.toString(ucdrrc, "") + "\",\n" +
                "  \"UCDRChannel\": \"" + Objects.toString(ucdrChannel, "") + "\",\n" +
                "  \"LastStatus\": \"" + Objects.toString(lastStatus, "") + "\"\n" +
                "}";
    }
}