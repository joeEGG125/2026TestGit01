package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.IOUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.X509Certificate;
import java.util.*;

public class SslContextFactory {
    private static final LogHelper logger = new LogHelper();
    public static final String PROTOCOL = "TLSv1.2";

    static {
        // System.setProperty("javax.net.debug", "ssl,handshake,record");
    }

    private SslContextFactory() {}

    public static SSLEngine getSSLEngine(SslKeyTrust sslKeyTrust, boolean needClientAuth, boolean wantClientAuth, boolean useClientMode) throws Exception {
        if (verify(sslKeyTrust)) {
            SSLContext sslContext = getSSLContext(sslKeyTrust);
            if (sslContext != null) {
                try {
                    SSLEngine sslEngine = sslContext.createSSLEngine();
                    sslEngine.setNeedClientAuth(needClientAuth);
                    sslEngine.setWantClientAuth(wantClientAuth);
                    sslEngine.setUseClientMode(useClientMode);
                    return sslEngine;
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        return null;
    }

    public static String getCertificate(SslKeyTrust sslKeyTrust, boolean printIndex) throws Exception {
        try (InputStream inPk = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            String infoTrust = getCertificate(inTrust, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslTrustPath())),
                    printIndex ? sslKeyTrust.getIndex() : -1, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType(), sslKeyTrust.isDeactivated());
            if (StringUtils.isNotBlank(infoTrust)) {
                return infoTrust;
            }
            String infoPk = getCertificate(inPk, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslKeyPath())),
                    printIndex ? sslKeyTrust.getIndex() : -1, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType(), sslKeyTrust.isDeactivated());
            return infoPk;
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    public static String getCertificate(InputStream in, String filename, int index, String store, SslKeyTrustType type, boolean disabled) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            List<CertificateInformation> list = getCertificateInformationList(in, filename, index, store, type, disabled);
            for (CertificateInformation information : list) {
                sb.append("======================================The Certificate ").append(index == -1 ? StringUtils.EMPTY : StringUtils.join("SEQ[", index, "] ")).append("is as shown bellow======================================\r\n");
                sb.append(information.toString());
                sb.append("======================================The Certificate ").append(index == -1 ? StringUtils.EMPTY : StringUtils.join("SEQ[", index, "] ")).append("is as shown above ======================================\r\n");
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
        return sb.toString();
    }

    public static List<CertificateInformation> getCertificateInformationList(SslKeyTrust sslKeyTrust) throws Exception {
        try (InputStream inPk = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            List<CertificateInformation> infoTrustList = getCertificateInformationList(inTrust, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslTrustPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType(), sslKeyTrust.isDeactivated());
            if (CollectionUtils.isNotEmpty(infoTrustList)) {
                return infoTrustList;
            }
            List<CertificateInformation> infoPkList = getCertificateInformationList(inPk, FilenameUtils.getName(CleanPathUtil.cleanString(sslKeyTrust.getSslKeyPath())),
                    sslKeyTrust.getIndex(), sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType(), sslKeyTrust.isDeactivated());
            return infoPkList;
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    public static List<CertificateInformation> getCertificateInformationList(InputStream in, String filename, int index, String store, SslKeyTrustType type, boolean disabled) throws Exception {
        List<CertificateInformation> list = new ArrayList<>();
        try {
            KeyStore ks = loadKeyStore(in, store, type);
            if (ks != null) {
                // 2024-09-11 Richard modified for 【Unchecked Input for Loop Condition】
                // for (Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
                Enumeration<String> e = ks.aliases();
                if (e != null) {
                    while (e.hasMoreElements()) {
                        String alias = e.nextElement();
                        list.add(new CertificateInformation(filename, alias, ks.getCertificate(alias), disabled));
                    }
                }
            }
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
        return list;
    }

    public static X509Certificate[] getX509TrustCertificates(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509Certificate> certificateList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
                    KeyStore ts = loadKeyStore(in, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
                    if (ts != null) {
                        for (Enumeration<String> e = ts.aliases(); e.hasMoreElements(); ) {
                            String alias = e.nextElement();
                            // 2024-10-17 Richard modified for【SSL Verification Bypass】
                            // X509Certificate cert = (X509Certificate) ts.getCertificate(alias);
                            // certificateList.add(cert);
                            certificateList.add((X509Certificate) ts.getCertificate(alias));
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        // 2024-10-25 Richard modified for 【SSL Verification Bypass】
        // X509Certificate[] certificates = new X509Certificate[certificateList.size()];
        // certificateList.toArray(certificates);
        // return certificates;
        return certificateList.toArray(new X509Certificate[0]);
    }

    public static List<X509KeyManager> getKeyManagerList(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509KeyManager> x509KeyManagerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyManager[] keyManagers = getKeyManagers(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ArrayUtils.isNotEmpty(keyManagers)) {
                        for (KeyManager keyManager : keyManagers) {
                            if (keyManager instanceof X509KeyManager) {
                                x509KeyManagerList.add((X509KeyManager) keyManager);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        return x509KeyManagerList;
    }

    public static List<X509TrustManager> getTrustManagerList(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<X509TrustManager> x509TrustManagerList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
                    TrustManager[] trustManagers = getTrustManagers(in, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
                    if (ArrayUtils.isNotEmpty(trustManagers)) {
                        for (TrustManager trustManager : trustManagers) {
                            if (trustManager instanceof X509TrustManager) {
                                x509TrustManagerList.add((X509TrustManager) trustManager);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        return x509TrustManagerList;
    }

    public static List<String> getAliasList(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        List<String> aliasList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyStore ks = loadKeyStore(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ks != null) {
                        Enumeration<String> e = ks.aliases();
                        while (e.hasMoreElements()) {
                            aliasList.add(e.nextElement());
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        return aliasList;
    }

    public static Map<String, X509Certificate> getAliasToX509CertificateMap(List<SslKeyTrust> sslKeyTrustList) throws Exception {
        Map<String, X509Certificate> aliasToX509CertificateMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyStore ks = loadKeyStore(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ks != null) {
                        Enumeration<String> e = ks.aliases();
                        while (e.hasMoreElements()) {
                            String alias = e.nextElement();
                            aliasToX509CertificateMap.put(alias, (X509Certificate) ks.getCertificate(alias));
                        }
                    }
                } catch (Exception e) {
                    logger.exceptionMsg(e, e.getMessage());
                    throw e;
                }
            }
        }
        return aliasToX509CertificateMap;
    }

    private static boolean verify(SslKeyTrust sslKeyTrust) {
        if (sslKeyTrust == null) {
            return false;
        } else if (StringUtils.isBlank(sslKeyTrust.getSslKeyPath()) && StringUtils.isBlank(sslKeyTrust.getSslTrustPath())) {
            return false;
        } else if (StringUtils.isNotBlank(sslKeyTrust.getSslKeyPath()) && sslKeyTrust.getSslKeySscode() == null) {
            return false;
        } else if (StringUtils.isNotBlank(sslKeyTrust.getSslTrustPath()) && sslKeyTrust.getSslTrustSscode() == null) {
            return false;
        }
        return true;
    }

    private static SSLContext getSSLContext(SslKeyTrust sslKeyTrust) throws Exception {
        try (InputStream inPk = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode());
             InputStream inTrust = getInputStream(sslKeyTrust.getSslTrustPath(), sslKeyTrust.getSslTrustSscode())) {
            return getSSLContext(inPk, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType(), inTrust, sslKeyTrust.getSslTrustSscode(), sslKeyTrust.getSslTrustType());
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    private static SSLContext getSSLContext(InputStream pk, String pkStore, SslKeyTrustType pkType, InputStream trust, String trustStore, SslKeyTrustType trustType) throws Exception {
        try {
            // key manager factory
            KeyManager[] km = getKeyManagers(pk, pkStore, pkType);
            // trust manager factory
            TrustManager[] tm = getTrustManagers(trust, trustStore, trustType);
            // Initialize the SSLContext to work with our key managers.
            return getSslContext(km, tm);
        } catch (Exception e) {
            logger.exceptionMsg(e, e.getMessage());
            throw e;
        }
    }

    public static SSLContext getSslContext(KeyManager[] km, TrustManager[] tm) throws Exception {
        if (km != null || tm != null) {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(km, tm, null);
            return sslContext;
        }
        return null;
    }

    public static KeyManager[] getKeyManagers(InputStream pk, String pkStore, SslKeyTrustType pkType) throws Exception {
        // keystore
        KeyStore ks = loadKeyStore(pk, pkStore, pkType);
        if (ks != null) {
            // Set up key manager factory to use our key store
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, pkStore.toCharArray());
            // key manager factory
            KeyManager[] km = kmf.getKeyManagers();
            return km;
        }
        return null;
    }

    public static TrustManager[] getTrustManagers(InputStream trust, String trustStore, SslKeyTrustType trustType) throws Exception {
        // truststore
        KeyStore ts = loadKeyStore(trust, trustStore, trustType);
        if (ts != null) {
            // set up trust manager factory to use our trust store
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            // trust manager factory
            TrustManager[] tm = tmf.getTrustManagers();
            return tm;
        }
        return null;
    }

    public static KeyStore loadKeyStore(InputStream in, String store, SslKeyTrustType type) throws Exception {
        if (in != null && store != null) {
            KeyStore ks = KeyStore.getInstance(type.name());
            ks.load(in, store.toCharArray());
            return ks;
        }
        return null;
    }


    public static InputStream getInputStream(String ssl, String store) throws Exception {
        InputStream in = null;
        if (StringUtils.isNotBlank(ssl) && store != null) {
            in = IOUtil.openInputStream(ssl);
        }
        return in;
    }

    public static SSLEngine getSSLEngine(List<SslKeyTrust> sslKeyTrustList, boolean needClientAuth, boolean useClientMode) throws Exception {
        if (CollectionUtils.isNotEmpty(sslKeyTrustList)) {
            List<Builder> builderList = new ArrayList<>();
            for (SslKeyTrust sslKeyTrust : sslKeyTrustList) {
                try (InputStream in = getInputStream(sslKeyTrust.getSslKeyPath(), sslKeyTrust.getSslKeySscode())) {
                    KeyStore ks = loadKeyStore(in, sslKeyTrust.getSslKeySscode(), sslKeyTrust.getSslKeyType());
                    if (ks != null) {
                        builderList.add(Builder.newInstance(ks, new PasswordProtection(sslKeyTrust.getSslKeySscode().toCharArray())));
                    }
                }
            }
            if (!builderList.isEmpty()) {
                ManagerFactoryParameters ksParams = new KeyStoreBuilderParameters(builderList);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
                kmf.init(ksParams);
                SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
                sslContext.init(kmf.getKeyManagers(), null, null);
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setNeedClientAuth(needClientAuth);
                sslEngine.setUseClientMode(useClientMode);
                return sslEngine;
            }
        }
        return null;
    }
}
