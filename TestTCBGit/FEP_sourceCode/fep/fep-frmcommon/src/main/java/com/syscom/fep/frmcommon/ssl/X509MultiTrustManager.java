package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class X509MultiTrustManager extends X509ExtendedTrustManager {
    private LogHelper logger = new LogHelper();
    protected List<X509TrustManager> x509TrustManagerList;

    public X509MultiTrustManager(List<X509TrustManager> x509TrustManagerList) {
        this.x509TrustManagerList = x509TrustManagerList;
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the authentication type based on the client certificate
     * @throws CertificateException
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    trustManager.checkClientTrusted(chain, authType);
                    logger.info("enter checkClientTrusted..., index = [", index, "], authType= [", authType, "]");
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the key exchange algorithm used
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    trustManager.checkServerTrusted(chain, authType);
                    logger.info("enter checkServerTrusted..., index = [", index, "], authType= [", authType, "]");
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }

    /**
     * @return
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        logger.info("enter getAcceptedIssuers...");
        X509Certificate[] acceptedIssuers = new X509Certificate[0];
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                acceptedIssuers = ArrayUtils.addAll(acceptedIssuers, trustManager.getAcceptedIssuers());
            }
        }
        return acceptedIssuers;
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the key exchange algorithm used
     * @param socket   the socket used for this connection. This parameter
     *                 can be null, which indicates that implementations need not check
     *                 the ssl parameters
     * @throws CertificateException
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    ((X509ExtendedTrustManager) trustManager).checkClientTrusted(chain, authType, socket);
                    logger.info("enter checkClientTrusted..., index = [", index, "]");
                    if (socket != null) {
                        logger.info("socket hostName = [", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostName", StringUtils.EMPTY), "], hostAddress =[", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostAddress", StringUtils.EMPTY), "]");
                    }
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the key exchange algorithm used
     * @param socket   the socket used for this connection. This parameter
     *                 can be null, which indicates that implementations need not check
     *                 the ssl parameters
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    ((X509ExtendedTrustManager) trustManager).checkServerTrusted(chain, authType, socket);
                    logger.info("enter checkServerTrusted..., index = [", index, "]");
                    if (socket != null) {
                        logger.info("socket hostName = [", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostName", StringUtils.EMPTY), "], hostAddress =[", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostAddress", StringUtils.EMPTY), "]");
                    }
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the key exchange algorithm used
     * @param engine   the engine used for this connection. This parameter
     *                 can be null, which indicates that implementations need not check
     *                 the ssl parameters
     * @throws CertificateException
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    ((X509ExtendedTrustManager) trustManager).checkClientTrusted(chain, authType, engine);
                    logger.info("enter checkClientTrusted..., index = [", index, "]");
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }

    /**
     * @param chain    the peer certificate chain
     * @param authType the key exchange algorithm used
     * @param engine   the engine used for this connection. This parameter
     *                 can be null, which indicates that implementations need not check
     *                 the ssl parameters
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
        if (CollectionUtils.isNotEmpty(this.x509TrustManagerList)) {
            CertificateException certificateException = null;
            int index = -1;
            for (X509TrustManager trustManager : this.x509TrustManagerList) {
                index++;
                try {
                    ((X509ExtendedTrustManager) trustManager).checkServerTrusted(chain, authType, engine);
                    logger.info("enter checkServerTrusted..., index = [", index, "]");
                    return;
                } catch (CertificateException e) {
                    certificateException = e;
                }
            }
            if (certificateException != null) {
                throw certificateException;
            }
        }
    }
}
