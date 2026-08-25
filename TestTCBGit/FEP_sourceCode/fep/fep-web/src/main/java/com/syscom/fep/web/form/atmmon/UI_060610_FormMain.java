package com.syscom.fep.web.form.atmmon;

import java.util.List;

import com.syscom.fep.web.form.BaseForm;

public class UI_060610_FormMain extends BaseForm {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 交易日期
     */
    private String transactDate;

    /**
     * 交易時間起
     */
    private String feptxnTxTimeBegin;

    /**
     * 交易時間起(畫面)
     */
    private String feptxnTxTimeBeginTxt;

    /**
     * 交易時間訖
     */
    private String feptxnTxTimeEnd;

    /**
     * 交易時間訖(畫面)
     */
    private String feptxnTxTimeEndTxt;

    /**
     * Atm代號
     */
    private String txtAtmNo;

    /**
     * Atm序號
     */
    private String atmSeqTxt;

    /**
     * 訊息ID
     */
    private String txtMessageId;

    /**
     * 轉出帳號
     */
    private String txtTroutActNo;

    /**
     * EJ序號
     */
    private String ejNoUc;

    /**
     * EJ轉成陣列
     */
    private List<String> ejfnoList;

    /**
     * 通道
     */
    private String channelUc;

    /**
     * 訊息流程
     */
    private String msgFlowUc2;

    /**
     * 財金 STAN
     */
    private String txtBkNo;

    /**
     * STAN
     */
    private String stanTxt;

    /**
     * 下載log的種類
     */
    private String logType;

    /**
     * 選擇主機
     */
    private String server;

    /**
     * 查詢主機
     */
    private String hostname;

    /**
     * 選擇下載的具體GZ LOG
     */
    private String selectGZLog;

    /**
     * 選擇下載的通道
     */
    private String channelDownload;

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getFeptxnTxTimeBeginTxt() {
        return feptxnTxTimeBeginTxt;
    }

    public void setFeptxnTxTimeBeginTxt(String feptxnTxTimeBeginTxt) {
        this.feptxnTxTimeBeginTxt = feptxnTxTimeBeginTxt;
    }

    public String getFeptxnTxTimeEndTxt() {
        return feptxnTxTimeEndTxt;
    }

    public void setFeptxnTxTimeEndTxt(String feptxnTxTimeEndTxt) {
        this.feptxnTxTimeEndTxt = feptxnTxTimeEndTxt;
    }

    public String getTransactDate() {
        return transactDate;
    }

    public void setTransactDate(String transactDate) {
        this.transactDate = transactDate;
    }

    public String getFeptxnTxTimeBegin() {
        return feptxnTxTimeBegin;
    }

    public void setFeptxnTxTimeBegin(String feptxnTxTimeBegin) {
        this.feptxnTxTimeBegin = feptxnTxTimeBegin;
    }

    public String getFeptxnTxTimeEnd() {
        return feptxnTxTimeEnd;
    }

    public void setFeptxnTxTimeEnd(String feptxnTxTimeEnd) {
        this.feptxnTxTimeEnd = feptxnTxTimeEnd;
    }

    public String getTxtAtmNo() {
        return txtAtmNo;
    }

    public void setTxtAtmNo(String txtAtmNo) {
        this.txtAtmNo = txtAtmNo;
    }

    public String getAtmSeqTxt() {
        return atmSeqTxt;
    }

    public void setAtmSeqTxt(String atmSeqTxt) {
        this.atmSeqTxt = atmSeqTxt;
    }

    public String getTxtMessageId() {
        return txtMessageId;
    }

    public void setTxtMessageId(String txtMessageId) {
        this.txtMessageId = txtMessageId;
    }

    public String getTxtTroutActNo() {
        return txtTroutActNo;
    }

    public void setTxtTroutActNo(String txtTroutActNo) {
        this.txtTroutActNo = txtTroutActNo;
    }

    public String getEjNoUc() {
        return ejNoUc;
    }

    public void setEjNoUc(String ejNoUc) {
        this.ejNoUc = ejNoUc;
    }

    public List<String> getEjfnoList() {
        return ejfnoList;
    }

    public void setEjfnoList(List<String> ejfnoList) {
        this.ejfnoList = ejfnoList;
    }

    public String getChannelUc() {
        return channelUc;
    }

    public void setChannelUc(String channelUc) {
        this.channelUc = channelUc;
    }

    public String getMsgFlowUc2() {
        return msgFlowUc2;
    }

    public void setMsgFlowUc2(String msgFlowUc2) {
        this.msgFlowUc2 = msgFlowUc2;
    }

    public String getTxtBkNo() {
        return txtBkNo;
    }

    public void setTxtBkNo(String txtBkNo) {
        this.txtBkNo = txtBkNo;
    }

    public String getStanTxt() {
        return stanTxt;
    }

    public void setStanTxt(String stanTxt) {
        this.stanTxt = stanTxt;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getSelectGZLog() {
        return selectGZLog;
    }

    public void setSelectGZLog(String selectGZLog) {
        this.selectGZLog = selectGZLog;
    }

    public String getChannelDownload() {
        return channelDownload;
    }

    public void setChannelDownload(String channelDownload) {
        this.channelDownload = channelDownload;
    }
}
