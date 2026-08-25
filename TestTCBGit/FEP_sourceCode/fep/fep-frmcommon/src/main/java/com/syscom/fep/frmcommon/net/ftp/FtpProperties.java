package com.syscom.fep.frmcommon.net.ftp;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * 定義FTP常用的屬性
 *
 * @author Richard
 */
public class FtpProperties {
    /**
     * FTP連線使用者名
     */
    private String username;
    /**
     * FTP連線密碼
     */
    private String password;
    /**
     * FTP主機名稱, 例如localhost
     */
    private String host;
    /**
     * FTP連線端口, 預設21
     */
    private int port = 21;
    /**
     * FTP傳輸模式, 預設為被動模式Local Passive
     * 注意, SFTP不需要設置此屬性
     */
    private int clientMode = FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;
    /**
     * FTP使用的字符編碼, 預設UTF-8, 注意參考.NET要用PolyfillUtil.toCharsetName方法來取
     */
    private String controlEncoding = "UTF-8";
    /**
     * FTP讀寫檔案buffer, 預設2048 byte
     * 注意, SFTP不需要設置此屬性
     */
    private int bufferSize = 2048;
    /**
     * FTP傳輸的檔案類型, 預設是二進制形式傳輸
     * 注意, SFTP不需要設置此屬性
     */
    private int fileType = FTP.BINARY_FILE_TYPE;
    /**
     * FTPlink池個數, 預設是5
     */
    private int poolSize = 5;
    /**
     * FTP連線超時時間, 預設60000毫秒
     */
    private int connectTimeout = 60000;
    /**
     * FTP預設超時時間, 預設0即不超時一直等待
     * 注意, SFTP不需要設置此屬性
     */
    private int defaultTimeout = 0;
    /**
     * FTP資料傳輸超時時間, 預設-1即不超時一直等待
     * 注意, SFTP不需要設置此屬性
     */
    private int dataTimeout = -1;
    /**
     * FTP session超時時間, 預設Long.MAX_VALUE即不超時
     */
    private long sessionWaitTimeout = Long.MAX_VALUE;
    /**
     * SFTP私匙
     */
    private String sftpPrivateKey;
    /**
     * SFTP私匙的密碼
     */
    private String sftpPrivateKeyPassphrase;
    /**
     * SFTP根目錄, 小心會有權限的問題
     */
    private String sftpRootDirectory;
    /**
     * FTP Protocol, 預設FTP
     */
    private FtpProtocol protocol = FtpProtocol.SFTP;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getClientMode() {
        return clientMode;
    }

    public void setClientMode(int passiveMode) {
        this.clientMode = passiveMode;
    }

    public String getControlEncoding() {
        return controlEncoding;
    }

    public void setControlEncoding(String encoding) {
        this.controlEncoding = encoding;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int clientTimeout) {
        this.connectTimeout = clientTimeout;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int transferFileType) {
        this.fileType = transferFileType;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getSessionWaitTimeout() {
        return sessionWaitTimeout;
    }

    public void setSessionWaitTimeout(long sessionWaitTimeout) {
        this.sessionWaitTimeout = sessionWaitTimeout;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getDataTimeout() {
        return dataTimeout;
    }

    public void setDataTimeout(int dataTimeout) {
        this.dataTimeout = dataTimeout;
    }

    public String getSftpPrivateKey() {
        return sftpPrivateKey;
    }

    public void setSftpPrivateKey(String privateKey) {
        this.sftpPrivateKey = privateKey;
    }

    public String getSftpPrivateKeyPassphrase() {
        return sftpPrivateKeyPassphrase;
    }

    public void setSftpPrivateKeyPassphrase(String sftpPrivateKeyPassphrase) {
        this.sftpPrivateKeyPassphrase = sftpPrivateKeyPassphrase;
    }

    public FtpProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(FtpProtocol protocol) {
        this.protocol = protocol;
    }

    public String getSftpRootDirectory() {
        return sftpRootDirectory;
    }

    public void setSftpRootDirectory(String sftpRootDirectory) {
        this.sftpRootDirectory = sftpRootDirectory;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}