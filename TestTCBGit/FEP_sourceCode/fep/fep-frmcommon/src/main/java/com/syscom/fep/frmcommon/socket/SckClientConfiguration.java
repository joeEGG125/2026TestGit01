package com.syscom.fep.frmcommon.socket;

import org.apache.commons.lang.StringUtils;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SckClientConfiguration {
    private SckClientConfiguration() {
    }

    public static SSLSocketFactory createSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null,
                        (x509Certificates, authType) -> true).build();
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return sslSocketFactory;
    }

    public static SSLSocketFactory createSSLSocketFactory(InputStream ssl, String store, String type) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(StringUtils.isBlank(type) ? KeyStore.getDefaultType() : type);
        trustStore.load(ssl, store.toCharArray());
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore,
                        (x509Certificates, authType) -> true).build();
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return sslSocketFactory;
    }

    public static SSLServerSocketFactory createSSLServerSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null,
                        (x509Certificates, authType) -> true).build();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        return sslServerSocketFactory;
    }

    public static SSLServerSocketFactory createSSLServerSocketFactory(InputStream ssl, String store, String type) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(StringUtils.isBlank(type) ? KeyStore.getDefaultType() : type);
        trustStore.load(ssl, store.toCharArray());
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore,
                        (x509Certificates, authType) -> true).build();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        return sslServerSocketFactory;
    }
}
