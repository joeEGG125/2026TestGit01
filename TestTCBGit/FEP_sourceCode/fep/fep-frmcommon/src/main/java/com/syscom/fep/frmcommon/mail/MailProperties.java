package com.syscom.fep.frmcommon.mail;

public class MailProperties {
    private String smtp;
    private int port;
    private String account;
    private String sscode;
    private String charset = "UTF-8";
    private boolean sslOnConnect = false;
    private int socketTimeout;
    private int socketConnectionTimeout;

    public String getSmtp() {
        return smtp;
    }

    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSscode() {
        return sscode;
    }

    public void setSscode(String sscode) {
        this.sscode = sscode;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isSslOnConnect() {
        return sslOnConnect;
    }

    public void setSslOnConnect(boolean sslOnConnect) {
        this.sslOnConnect = sslOnConnect;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getSocketConnectionTimeout() {
        return socketConnectionTimeout;
    }

    public void setSocketConnectionTimeout(int socketConnectionTimeout) {
        this.socketConnectionTimeout = socketConnectionTimeout;
    }
}
