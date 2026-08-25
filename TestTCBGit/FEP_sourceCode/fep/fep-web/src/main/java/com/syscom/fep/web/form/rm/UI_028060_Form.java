package com.syscom.fep.web.form.rm;

import com.syscom.fep.web.form.BaseForm;

public class UI_028060_Form extends BaseForm {
    /**
     * 調整項目
     */
    private String kind;
    /**
     * 調整交易類別
     */
    private String txKind;

    private String fISCO_FLAG1 = UI_028060_Form.FISCO_FLAG1.Y.toString();
    private String fISCI_FLAG1 = UI_028060_Form.FISCI_FLAG1.Y.toString();
    private String fISCO_FLAG4 = UI_028060_Form.FISCO_FLAG4.Y.toString();
    private String fISCI_FLAG4 = UI_028060_Form.FISCI_FLAG4.Y.toString();

    public String getKind() { return kind; }

    public void setKind(String kind) { this.kind = kind; }

    public String getTxKind() { return txKind; }

    public void setTxKind(String txKind) { this.txKind = txKind; }

    public String getfISCO_FLAG1() { return fISCO_FLAG1; }

    public void setfISCO_FLAG1(String fISCO_FLAG1) { this.fISCO_FLAG1 = fISCO_FLAG1; }

    public String getfISCI_FLAG1() { return fISCI_FLAG1; }

    public void setfISCI_FLAG1(String fISCI_FLAG1) { this.fISCI_FLAG1 = fISCI_FLAG1; }

    public String getfISCO_FLAG4() { return fISCO_FLAG4; }

    public void setfISCO_FLAG4(String fISCO_FLAG4) { this.fISCO_FLAG4 = fISCO_FLAG4; }

    public String getfISCI_FLAG4() { return fISCI_FLAG4; }

    public void setfISCI_FLAG4(String fISCI_FLAG4) { this.fISCI_FLAG4 = fISCI_FLAG4; }

    public static enum FISCO_FLAG1 {
        /**
         * 可匯出/入 匯款  送往財金
         */
        Y,
        /**
         * 暫停匯出/入 匯款  送往財金
         */
        N,
    }

    public static enum FISCI_FLAG1 {
        /**
         * 可匯出/入    匯款  來自財金
         */
        Y,
        /**
         * 暫停匯出/入  匯款  來自財金
         */
        N,
    }

    public static enum FISCO_FLAG4 {
        /**
         * 可匯出/入 一般通訊  送往財金
         */
        Y,
        /**
         * 暫停匯出/入 一般通訊  送往財金
         */
        N,
    }


    public static enum FISCI_FLAG4 {
        /**
         * 可匯出/入 一般通訊  來自財金
         */
        Y,
        /**
         * 暫停匯出/入 一般通訊  來自財金
         */
        N,
    }
}
