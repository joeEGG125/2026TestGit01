package com.syscom.fep.frmcommon.ssl;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

public class X509MultiKeyManager extends X509ExtendedKeyManager {
    protected final LogHelper logger = new LogHelper();
    protected List<X509KeyManager> x509KeyManagerList;

    public X509MultiKeyManager(List<X509KeyManager> x509KeyManagerList) {
        this.x509KeyManagerList = x509KeyManagerList;
    }

    /**
     * @param keyType the key algorithm type name
     * @param issuers the list of acceptable CA issuer subject names,
     *                or null if it does not matter which issuers are used.
     * @return
     */
    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String[] clientAliases = keyManager.getClientAliases(keyType, issuers);
                if (ArrayUtils.isNotEmpty(clientAliases)) {
                    logger.info("enter getClientAliases, index = [", index, "], keyType = [", keyType, "], clientAliases = [", StringUtils.join(clientAliases, ','), "]");
                    return clientAliases;
                }
            }
        }
        return new String[0];
    }

    /**
     * @param keyType the key algorithm type name(s), ordered
     *                with the most-preferred key type first.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param socket  the socket to be used for this connection.  This
     *                parameter can be null, which indicates that
     *                implementations are free to select an alias applicable
     *                to any socket.
     * @return
     */
    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String clientAlias = keyManager.chooseClientAlias(keyType, issuers, socket);
                if (StringUtils.isNotBlank(clientAlias)) {
                    logger.info("enter chooseClientAlias..., index = [", index, "], clientAlias = [", clientAlias, "]");
                    if (socket != null) {
                        logger.info("socket hostName = [", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostName", StringUtils.EMPTY), "], hostAddress =[", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostAddress", StringUtils.EMPTY), "]");
                    }
                    return clientAlias;
                }
            }
        }
        return null;
    }

    /**
     * @param keyType the key algorithm type name
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @return
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String[] serverAliases = keyManager.getServerAliases(keyType, issuers);
                if (ArrayUtils.isNotEmpty(serverAliases)) {
                    logger.info("enter getServerAliases, index = [", index, "], keyType = [", keyType, "], serverAliases = [", StringUtils.join(serverAliases, ','), "]");
                    return serverAliases;
                }
            }
        }
        return new String[0];
    }

    /**
     * @param keyType the key algorithm type name.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param socket  the socket to be used for this connection.  This
     *                parameter can be null, which indicates that
     *                implementations are free to select an alias applicable
     *                to any socket.
     * @return
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String serverAlias = keyManager.chooseServerAlias(keyType, issuers, socket);
                if (StringUtils.isNotBlank(serverAlias)) {
                    logger.info("enter chooseServerAlias..., index = [", index, "], serverAlias = [", serverAlias, "]");
                    if (socket != null) {
                        logger.info("socket hostName = [", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostName", StringUtils.EMPTY), "], hostAddress =[", ReflectUtil.envokeMethod(socket.getInetAddress(), "getHostAddress", StringUtils.EMPTY), "]");
                    }
                    return serverAlias;
                }
            }
        }
        return null;
    }

    /**
     * @param alias the alias name
     * @return
     */
    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                // 2024-10-17 Richard modified for【SSL Verification Bypass】
                // X509Certificate[] certificateChain = keyManager.getCertificateChain(alias);
                // if (ArrayUtils.isNotEmpty(certificateChain)) {
                //     logger.info("enter getCertificateChain, index = [", index, "], alias = [", alias, "]");
                //     return certificateChain;
                // }
                if (ArrayUtils.isNotEmpty(keyManager.getCertificateChain(alias))) {
                    logger.info("enter getCertificateChain, index = [", index, "], alias = [", alias, "]");
                    return keyManager.getCertificateChain(alias);
                }
            }
        }
        return new X509Certificate[0];
    }

    /**
     * @param alias the alias name
     * @return
     */
    @Override
    public PrivateKey getPrivateKey(String alias) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                PrivateKey privateKey = keyManager.getPrivateKey(alias);
                if (privateKey != null) {
                    logger.info("enter getPrivateKey, index = [", index, "], alias = [", alias, "]");
                    return privateKey;
                }
            }
        }
        return null;
    }

    /**
     * @param keyType the key algorithm type name(s), ordered
     *                with the most-preferred key type first.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param engine  the <code>SSLEngine</code> to be used for this
     *                connection.  This parameter can be null, which indicates
     *                that implementations of this interface are free to
     *                select an alias applicable to any engine.
     * @return
     */
    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String clientAlias = ((X509ExtendedKeyManager) keyManager).chooseEngineClientAlias(keyType, issuers, engine);
                if (StringUtils.isNotBlank(clientAlias)) {
                    logger.info("enter chooseEngineClientAlias, index = [", index, "], keyType = [", StringUtils.join(keyType, ','), "], clientAlias = [", clientAlias, "]");
                    return clientAlias;
                }
            }
        }
        return null;
    }

    /**
     * @param keyType the key algorithm type name.
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @param engine  the <code>SSLEngine</code> to be used for this
     *                connection.  This parameter can be null, which indicates
     *                that implementations of this interface are free to
     *                select an alias applicable to any engine.
     * @return
     */
    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
            int index = -1;
            for (X509KeyManager keyManager : this.x509KeyManagerList) {
                index++;
                String serverAlias = ((X509ExtendedKeyManager) keyManager).chooseEngineServerAlias(keyType, issuers, engine);
                if (StringUtils.isNotBlank(serverAlias)) {
                    logger.info("enter chooseEngineServerAlias, index = [", index, "], keyType = [", keyType, "], serverAlias = [", serverAlias, "]");
                    return serverAlias;
                }
            }
        }
        return null;
    }
}
