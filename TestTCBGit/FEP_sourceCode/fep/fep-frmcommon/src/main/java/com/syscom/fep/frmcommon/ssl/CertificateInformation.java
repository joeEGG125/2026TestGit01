package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateInformation implements Serializable {
    private String fileName;
    private String alias;
    private Object cert;
    private boolean disabled;

    public CertificateInformation() {}

    public CertificateInformation(String fileName, String alias, Object cert, boolean disabled) {
        this.fileName = fileName;
        this.alias = alias;
        this.cert = cert; // 2024-10-17 Richard modified for【SSL Verification Bypass】
        this.disabled = disabled;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * 獲得憑證生效日期
     *
     * @return
     */
    public String getValidDateBegin() {
        if (cert == null)
            return StringUtils.EMPTY;
        Date beforedate = ((X509Certificate) cert).getNotBefore();
        return FormatUtil.dateFormat(beforedate);
    }

    /**
     * 獲得憑證失效日期
     *
     * @return
     */
    public String getValidDateEnd() {
        if (cert == null)
            return StringUtils.EMPTY;
        Date beforedate = ((X509Certificate) cert).getNotAfter();
        return FormatUtil.dateFormat(beforedate);
    }

    /**
     * 憑證頒發者
     *
     * @return
     */
    public String getIssuerDN() {
        if (cert == null)
            return StringUtils.EMPTY;
        return ((X509Certificate) cert).getIssuerDN().getName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Certificate File Name:").append(fileName).append("\r\n");
        sb.append("Certificate Alias Name:").append(alias).append("\r\n");
        // 獲得憑證版本
        String info = String.valueOf(((X509Certificate) cert).getVersion());
        sb.append("Certificate Version:").append(info).append("\r\n");
        // 獲得憑證序列號
        info = ((X509Certificate) cert).getSerialNumber().toString(16);
        sb.append("Certificate SerialNumber:").append(info).append("\r\n");
        // 獲得憑證有效期
        Date beforedate = ((X509Certificate) cert).getNotBefore();
        info = FormatUtil.dateFormat(beforedate);
        sb.append("Certificate Effective Date:").append(info).append("\r\n");
        Date afterdate = ((X509Certificate) cert).getNotAfter();
        info = FormatUtil.dateFormat(afterdate);
        sb.append("Certificate Expiration Date:").append(info).append("\r\n");
        // 獲得憑證主體信息
        info = ((X509Certificate) cert).getSubjectDN().getName();
        sb.append("Certificate Subject:").append(info).append("\r\n");
        // 獲得憑證頒發者信息
        info = ((X509Certificate) cert).getIssuerDN().getName();
        sb.append("Certificate Issuer:").append(info).append("\r\n");
        // 獲得憑證籤名算法名稱
        info = ((X509Certificate) cert).getSigAlgName();
        sb.append("Certificate Signature Algorithm Name:").append(info).append("\r\n");
        // 憑證是否啟用
        sb.append("Certificate Valid?:").append(disabled ? "No" : "Yes").append("\r\n");
        return sb.toString();
    }
}
