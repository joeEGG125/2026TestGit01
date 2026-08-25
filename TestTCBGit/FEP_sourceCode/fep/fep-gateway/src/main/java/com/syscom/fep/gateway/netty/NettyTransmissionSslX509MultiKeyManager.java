package com.syscom.fep.gateway.netty;

import com.syscom.fep.frmcommon.ssl.X509MultiKeyManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.util.List;

public class NettyTransmissionSslX509MultiKeyManager extends X509MultiKeyManager {
    private final String certAlias;
    private final Integer certIndex;

    public NettyTransmissionSslX509MultiKeyManager(List<X509KeyManager> x509KeyManagerList, String certAlias, Integer certIndex) {
        super(x509KeyManagerList);
        this.certAlias = certAlias;
        this.certIndex = certIndex;
    }

    /**
     * @param keyType the key algorithm type name
     * @param issuers the list of acceptable CA issuer subject names
     *                or null if it does not matter which issuers are used.
     * @return
     */
    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (StringUtils.isBlank(certAlias)) {
            return super.getServerAliases(keyType, issuers);
        }
        return new String[] {this.chooseEngineServerAlias(keyType, issuers, null)};
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
        if (StringUtils.isBlank(certAlias)) {
            return super.chooseServerAlias(keyType, issuers, socket);
        }
        return this.chooseEngineServerAlias(keyType, issuers, null);
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
        // 首先依據憑證檔Alias取
        if (StringUtils.isNotBlank(certAlias)) {
            return this.certAlias;
        }
        // 其次依據憑證檔序列取
        else if (certIndex != null && certIndex >= 0 && certIndex < this.x509KeyManagerList.size()) {
            if (CollectionUtils.isNotEmpty(this.x509KeyManagerList)) {
                String serverAlias = ((X509ExtendedKeyManager) this.x509KeyManagerList.get(certIndex)).chooseEngineServerAlias(keyType, issuers, engine);
                if (StringUtils.isNotBlank(serverAlias)) {
                    logger.info("enter chooseEngineServerAlias, index = [", certIndex, "], keyType = [", keyType, "], serverAlias = [", serverAlias, "]");
                    return serverAlias;
                }
            }
        }
        return super.chooseEngineServerAlias(keyType, issuers, engine);
    }
}
