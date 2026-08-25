package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.ssl.SslKeyTrustType;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.netty.*;
import com.syscom.fep.gateway.netty.atm.ctrl.ATMGatewayServerRestfulCtrl;
import com.syscom.fep.gateway.util.GatewayUtil;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList;
import com.syscom.fep.vo.communication.ToFEPATMCommuUpdateAtmstatBatch;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@StackTracePointCut(caller = SvrConst.SVR_ATM_GATEWAY)
public class ATMGatewayServer extends
        NettyTransmissionServer<ATMGatewayServerConfiguration, ATMGatewayServerChannelInboundHandlerAdapter, ATMGatewayServerRuleIpFilter, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> {
    // @Autowired
    // private GatewayCommuHelper commuHelper;
    @Autowired
    private GatewayUtil gatewayUtil;
    @Autowired
    private SpringConfigurationUtil springConfigurationUtil;
//    private Integer sslNumber = new Integer(0);
    private final List<X509KeyManager> keyManagerList = Collections.synchronizedList(new ArrayList<>());
    private final List<X509TrustManager> trustManagerList = Collections.synchronizedList(new ArrayList<>());
    //    private final Map<String, String> clientIpToKeyAliasMap = Collections.synchronizedMap(new HashMap<>());
    private final List<ToFEPATMCommuUpdateAtmstatBatch.ToFEPATMCommuUpdateAtmstatBatchData> atmstats = new ArrayList<>();

    @Override
    protected void initData() {
        super.initData();
// 2022-08-24 Richard mark start
// GW不能直接連DB, 所以改為從透過ATMService取資料
//		this.configuration.setBacklog(CMNConfig.getInstance().getListenBacklog());
//		this.configuration.setKeepAliveTime(CMNConfig.getInstance().getKeepAliveTime() * 1000);
//		this.configuration.setKeepAliveInterval(CMNConfig.getInstance().getKeepAliveInterval() * 1000);
//		this.configuration.setTimeout(GWConfig.getInstance().getAATimeout());
// 2022-08-24 Richard mark end
// 2023-01-03 Richard mark start
// 這幾項配置改為在配置檔中設定
//        ToGWCommuConfig toGWCommuConfig =
//                dbHelper.getConfigFromFEPATM(ToFEPCommuConfig.ConfigType.CMN.getValue() + ToFEPCommuConfig.ConfigType.GW.getValue(),
//                        gatewayUtil.getTimeout(this.configuration));
//        this.configuration.setBacklog(toGWCommuConfig.getCmn().getListenBacklog());
//        this.configuration.setKeepAliveTime(toGWCommuConfig.getCmn().getKeepAliveTime() * 1000);
//        this.configuration.setKeepAliveInterval(toGWCommuConfig.getCmn().getKeepAliveInterval() * 1000);
//        this.configuration.setTimeout(toGWCommuConfig.getGw().getAaTimeout());
// 2023-01-03 Richard mark end
        this.setReestablishConnectionAfterTerminateConnection(true);
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        super.connStateChanged(channel, state, t);
        if (state == NettyTransmissionConnState.SERVER_BOUND) {
            this.logContext.setSubSys(SubSystem.GW);
            this.logContext.setChannel(FEPChannel.ATM);
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setRemark(StringUtils.join("ATMGW Begin Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            // registerController
            SpringBeanFactoryUtil.registerController(ATMGatewayServerRestfulCtrl.class);
            // registerBean
            // SpringBeanFactoryUtil.registerBean(ATMGatewayServerToFEPATM.class);
            ATMGatewayServerToFEPATM.getInstance();
            SpringBeanFactoryUtil.registerBean(ATMGatewayServerBlackListHandler.class);
            SpringBeanFactoryUtil.registerBean(ATMGatewayServerClientIpToCertNoHandler.class);
            ATMGatewayServerLoadTestHandler.initialize(); // 2026-03-24 Richard add 壓測下先載入所需要的資料
        } else if (state == NettyTransmissionConnState.SERVER_SHUTTING_DOWN) {
            requestManager.handleAllProcessRequest(processRequest -> {
                processRequest.setClearAll(true);
                processRequest.setNeedUpdateAtmstat(false); // 2024-11-27 Richard add 這裡先設定為false, 避免一筆一筆透過ATM Service更新ATMSTAT檔, 會非常慢
                processRequest.closeConnection();
                Optional.ofNullable(processRequest.createToFEPATMCommuUpdateAtmstatBatchData(AtmStatus.Disconnected)).ifPresent(atmstats::add);
            });
            requestManager.clearAllProcessRequest();
            // unregisterController
            SpringBeanFactoryUtil.unregisterController(ATMGatewayServerRestfulCtrl.class);
            // unregisterBean
            SpringBeanFactoryUtil.unregisterBean(ATMGatewayServerBlackListHandler.class);
            SpringBeanFactoryUtil.unregisterBean(ATMGatewayServerClientIpToCertNoHandler.class);
        } else if (state == NettyTransmissionConnState.SERVER_SHUT_DOWN) {
            this.logContext.setMessageFlowType(MessageFlow.Request);
            this.logContext.setRemark(StringUtils.join("ATMGW Stop Listen IP:", this.configuration.getHost(), ",Port:", this.configuration.getPort()));
            this.logContext.setProgramFlowType(ProgramFlow.ATMGatewayIn);
            this.logContext.setProgramName(StringUtils.join(ProgramName, ".connStateChanged"));
            this.logMessage(this.logContext);
            // 2024-11-27 Richard add 透過ATM Service批量更新ATMSTAT檔,
            if (CollectionUtils.isNotEmpty(atmstats)) {
                ToFEPATMCommuUpdateAtmstatBatch request = new ToFEPATMCommuUpdateAtmstatBatch();
                request.setAtmstats(atmstats);
                ATMGatewayServerToFEPATM toFEPATM = ATMGatewayServerToFEPATM.getInstance(); // SpringBeanFactoryUtil.getBean(ATMGatewayServerToFEPATM.class);
                toFEPATM.updateAtmstatBatch(this.logContext, request, gatewayUtil.getTimeout(this.configuration));
                atmstats.clear(); // 這裡記得一定要清掉
            }
            // SpringBeanFactoryUtil.unregisterBean(ATMGatewayServerToFEPATM.class);
            ATMGatewayServerToFEPATM.getInstance().preDestroy();
            ATMGatewayServerLoadTestHandler.preDestroy(); // 2026-03-24 Richard add 壓測下清除資料
        }
    }

    @Override
    protected SslHandler getSslHandler(LogData logData, SocketChannel ch) throws Exception {
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        // 取出ClientIp, 判斷是否在BypassCheckAtmIp中, 如果有的話, 則不要需要憑證
        if (this.configuration.getBypassCheckAtmIp().contains(clientIp)) {
            NettyTransmissionUtil.infoMessage(ch, "[getSslHandler][BypassCheckAtmIp] Disabled SSL Handshake for ATM IP = [", clientIp, "]");
            return null;
        }
        loadSsl(logData, false, true);
        // 取出憑證檔Alias
        String certAlias = null;
        ATMGatewayServerClientIpToCertNoHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class, false);
        if (handler != null) {
            certAlias = handler.getCertAlias(clientIp);
            logData.setRemark(StringUtils.join("Get Certificate Succeed, clientIp:", clientIp, ", alias:", certAlias));
            logData.setProgramName(StringUtils.join(ProgramName, ".getSslHandler"));
            logMessage(logData);
        }
        // 2025-10-29 Richard modified 補上塞入timeout的設定
        SslHandler sslHandler = NettyTransmissionUtil.getSsHandler(keyManagerList, trustManagerList, certAlias, configuration.isSslNeedClientAuth(), false); // 服務端認證方式
        if (sslHandler != null) {
            sslHandler.setHandshakeTimeoutMillis(configuration.getHandshakeTimeoutMillis());
        }
        return sslHandler;
    }

    public void loadSsl(LogData logData, boolean clear, boolean throwException) throws Exception {
        if (clear) {
            this.keyManagerList.clear();
            this.trustManagerList.clear();
        }
        try {
            List<SslKeyTrust> list = configuration.getSslConfigs(false);
            if (this.keyManagerList.isEmpty()) {
                this.keyManagerList.addAll(SslContextFactory.getKeyManagerList(list));
                // 如果重新載入憑證檔, 則也要重新載入alias列表
                ATMGatewayServerClientIpToCertNoHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class, false);
                if (handler != null) {
                    List<String> aliasList = this.getAliasListSortedByValidityAfter(list);
                    handler.setCertAlias(aliasList);
                    if (logData != null) {
                        logData.setRemark(StringUtils.join("Load Certificate Alias List Succeed, alias list:", StringUtils.join(aliasList, ",")));
                        logData.setProgramName(StringUtils.join(ProgramName, ".loadSsl"));
                        logMessage(logData);
                    }
                }
            }
            if (this.trustManagerList.isEmpty()) {
                this.trustManagerList.addAll(SslContextFactory.getTrustManagerList(list));
            }
        } catch (Exception e) {
            if (throwException)
                throw e;
        }
    }

    /**
     * 按照憑證檔的有效期排序, 從大到小
     *
     * @param list
     * @return
     * @throws Exception
     */
    private List<String> getAliasListSortedByValidityAfter(List<SslKeyTrust> list) throws Exception {
        Map<String, X509Certificate> map = SslContextFactory.getAliasToX509CertificateMap(list);
        if (MapUtils.isNotEmpty(map))
            return map.entrySet().stream().sorted(Map.Entry.comparingByValue((certificate1, certificate2)
                    -> certificate2.getNotAfter().compareTo(certificate1.getNotAfter()))).map(Map.Entry::getKey).collect(Collectors.toList());
        return null;
    }

//    public SslKeyTrust sslKeySwitch(Integer index) throws Exception {
//        if (CollectionUtils.isEmpty(configuration.getSslConfigs()))
//            throw ExceptionUtil.createException("Cannot switch or change cause empty SSL Certificate list!!!");
//        if (index == null) {
//            synchronized (sslNumber) {
//                sslNumber++;
//                if (sslNumber == configuration.getSslConfigs().size())
//                    sslNumber = 0;
//                LogHelperFactory.getGeneralLogger().info("SSL Certificate has been switched, sslNumber = [", sslNumber, "]");
//                return configuration.getSslConfigs().get(sslNumber);
//            }
//        } else if (index < 0 || index >= configuration.getSslConfigs().size()) {
//            throw ExceptionUtil.createIllegalArgumentException("index must between 0 and ", configuration.getSslConfigs().size() - 1);
//        } else {
//            synchronized (sslNumber) {
//                sslNumber = index;
//                LogHelperFactory.getGeneralLogger().info("SSL Certificate has been switched, sslNumber = [", sslNumber, "]");
//                SslKeyTrust sslKeyTrust = configuration.getSslConfigs().get(sslNumber);
//                return sslKeyTrust;
//            }
//        }
//    }

    /**
     * 停用憑證檔
     *
     * @param logData
     * @param index
     * @return
     * @throws Exception
     */
    public synchronized String sslKeyDeactivated(LogData logData, Integer index) throws Exception {
        List<SslKeyTrust> list = configuration.getSslConfigs(false);
        if (CollectionUtils.isEmpty(list))
            throw ExceptionUtil.createException("Cannot deactivated cause empty SSL Certificate list!!!");
        if (index == null || index < 0 || index >= list.size()) {
            throw ExceptionUtil.createIllegalArgumentException("index must between 0 and ", list.size() - 1);
        } else {
            SslKeyTrust sslKeyTrust = list.get(index);
            String certificate = null;
            try {
                certificate = SslContextFactory.getCertificate(sslKeyTrust, false);
                if (StringUtils.isBlank(certificate)) {
                    return "Cannot read SSL Certificate, maybe incorrect password.";
                }
                return certificate;
            } catch (Exception e) {
                String message = StringUtils.join("Cannot read SSL Certificate, ", e.getMessage());
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivated"));
                logData.setRemark(message);
                return message;
            } finally {
                boolean needReloadSslConfiguration = false; // 是否最後需要重新載入憑證檔設定配置檔
                try {
                    if (!sslKeyTrust.isDeactivated()) {
                        sslKeyTrust.setDeactivated(true); // 不是真的移除掉, 只是設定deactivated
                        needReloadSslConfiguration = true; // 最後需要重新載入憑證檔設定配置檔
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.ATM);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivated"));
                        // 如果讀不到憑證檔信息
                        if (StringUtils.isBlank(certificate)) {
                            logData.setRemark(StringUtils.join("SSL Certificate has been deactivated, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "], but cannot read SSL Certification Information"));
                            logMessage(Level.WARN, logData);
                        } else {
                            logData.setRemark(StringUtils.join("SSL Certificate has been deactivated, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "]\r\n", certificate));
                            logMessage(logData);
                        }
                    }
                    // 如果已經deactivated, 則直接丟異常
                    else {
                        throw ExceptionUtil.createException("SSL Certificate already deactivated");
                    }
                } finally {
                    if (needReloadSslConfiguration) {
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.ATM);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivated"));
                        logData.setRemark("Begin to reload SSL Configuration");
                        logMessage(logData);
                        try {
                            // 重新載入ssl
                            loadSsl(logData, true, false);
                            // 記得更新配置檔
                            storeSslConfigs();
                            // logging
                            logData.setSubSys(SubSystem.GW);
                            logData.setChannel(FEPChannel.ATM);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivated"));
                            logData.setRemark("Succeed to reload SSL Configuration");
                            logMessage(logData);
                        } catch (Exception e) {
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivated"));
                            logData.setProgramException(e);
                            logData.setRemark("Reload SSL Configuration failed");
                            sendEMS(logData);
                        }
                    }
                }
            }
        }
    }

    /**
     * 啟用憑證檔
     *
     * @param logData
     * @param index
     * @return
     * @throws Exception
     */
    public synchronized String sslKeyActivated(LogData logData, Integer index) throws Exception {
        List<SslKeyTrust> list = configuration.getSslConfigs(true);
        if (CollectionUtils.isEmpty(list))
            throw ExceptionUtil.createException("Cannot activated cause empty SSL Certificate list!!!");
        if (index == null || index < 0 || index >= list.size()) {
            throw ExceptionUtil.createIllegalArgumentException("index must between 0 and ", list.size() - 1);
        } else {
            SslKeyTrust sslKeyTrust = list.get(index);
            String certificate = null;
            try {
                certificate = SslContextFactory.getCertificate(sslKeyTrust, false);
                if (StringUtils.isBlank(certificate)) {
                    return "Cannot read SSL Certificate, maybe incorrect password.";
                }
                return certificate;
            } catch (Exception e) {
                String message = StringUtils.join("Cannot read SSL Certificate, ", e.getMessage());
                logData.setProgramException(e);
                logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivated"));
                logData.setRemark(message);
                return message;
            } finally {
                boolean needReloadSslConfiguration = false; // 是否最後需要重新載入憑證檔設定配置檔
                try {
                    if (sslKeyTrust.isDeactivated()) {
                        sslKeyTrust.setDeactivated(false); // 設定deactivated=false
                        needReloadSslConfiguration = true; // 最後需要重新載入憑證檔設定配置檔
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.ATM);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivated"));
                        // 如果讀不到憑證檔信息
                        if (StringUtils.isBlank(certificate)) {
                            logData.setRemark(StringUtils.join("SSL Certificate has been enable, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "], but cannot read SSL Certification Information"));
                            logMessage(Level.WARN, logData);
                        } else {
                            logData.setRemark(StringUtils.join("SSL Certificate has been enable, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "]\r\n", certificate));
                            logMessage(logData);
                        }
                    }
                    // 如果已經enable, 則直接丟異常
                    else {
                        throw ExceptionUtil.createException("SSL Certificate already activated");
                    }
                } finally {
                    if (needReloadSslConfiguration) {
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.ATM);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivated"));
                        logData.setRemark("Begin to reload SSL Configuration");
                        logMessage(logData);
                        try {
                            // 重新載入ssl
                            loadSsl(logData, true, false);
                            // 記得更新配置檔
                            storeSslConfigs();
                            // logging
                            logData.setSubSys(SubSystem.GW);
                            logData.setChannel(FEPChannel.ATM);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivated"));
                            logData.setRemark("Succeed to reload SSL Configuration");
                            logMessage(logData);
                        } catch (Exception e) {
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivated"));
                            logData.setProgramException(e);
                            logData.setRemark("Reload SSL Configuration failed");
                            sendEMS(logData);
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加憑證檔
     *
     * @param logData
     * @param filename
     * @param sscode
     * @param sslKeyTrustType
     * @return
     * @throws Exception
     */
    public synchronized String sslKeyAdd(LogData logData, String filename, String sscode, SslKeyTrustType sslKeyTrustType) throws Exception {
        // 判斷傳入的憑證檔名不能是空白
        if (StringUtils.isBlank(filename)) {
            throw ExceptionUtil.createIllegalArgumentException("Filename cannot be blank");
        } else {
            // 取出所有的憑證檔信息, 包括deactivated的
            List<SslKeyTrust> list = configuration.getSslConfigs(null);
            if (list == null) {
                list = new ArrayList<>();
                configuration.setSslConfigs(list);
            }
            // 建立SslKeyTrust物件
            SslKeyTrust sslKeyTrust = new SslKeyTrust();
            sslKeyTrust.setIndex(list.size());
            sslKeyTrust.setSslKeyPath(filename);
            sslKeyTrust.setSslKeySscode(sscode);
            sslKeyTrust.setSslKeyType(sslKeyTrustType);
            // 先判斷是否已經存在
            int index = list.indexOf(sslKeyTrust);
            // 如果憑證檔已經存在設定
            if (index >= 0) {
                sslKeyTrust = list.get(index); // 取出已經存在的憑證檔設定
            }
            // 讀取憑證檔信息
            String certificate = null;
            // 讀取憑證檔catch到的異常
            Exception getCertificateException = null;
            try {
                certificate = SslContextFactory.getCertificate(sslKeyTrust, false);
            } catch (Exception e) {
                getCertificateException = e;
            } finally {
                boolean needReloadSslConfiguration = false; // 是否最後需要重新載入憑證檔設定配置檔
                try {
                    // 有讀取到憑證檔內容
                    if (StringUtils.isNotBlank(certificate)) {
                        // 如果憑證檔已經存在設定
                        if (index >= 0) {
                            // 判斷是否是deactivated, 如果是則設定deactivated=false
                            if (sslKeyTrust.isDeactivated()) {
                                sslKeyTrust.setDeactivated(false);
                                logData.setSubSys(SubSystem.GW);
                                logData.setChannel(FEPChannel.ATM);
                                logData.setMessageFlowType(MessageFlow.Request);
                                logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                                logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                                logData.setRemark(StringUtils.join("SSL Certificate has been enabled, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "]\r\n", certificate));
                                logMessage(logData);
                            }
                            // 如果已經enable, 則直接丟異常
                            else {
                                throw ExceptionUtil.createException("SSL Certificate already exists\r\n", certificate);
                            }
                        }
                        // 憑證檔不存在則添加到list中
                        else {
                            list.add(sslKeyTrust);
                            logData.setSubSys(SubSystem.GW);
                            logData.setChannel(FEPChannel.ATM);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                            logData.setRemark(StringUtils.join("SSL Certificate has been add, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "]\r\n", certificate));
                            logMessage(logData);
                        }
                        needReloadSslConfiguration = true; // 最後需要重新載入憑證檔設定配置檔
                    }
                    // 如果沒有讀取到憑證檔內容
                    else {
                        // 如果憑證檔設定已經存在
                        // if (index >= 0) {
                        //     sslKeyTrust.setDeactivated(true); // 不是真的移除掉, 只是設定deactivated
                        //     logData.setSubSys(SubSystem.GW);
                        //     logData.setChannel(FEPChannel.ATM);
                        //     logData.setMessageFlowType(MessageFlow.Request);
                        //     logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        //     logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                        //     logData.setRemark(StringUtils.join("SSL Certificate has been deactivated, index = [", sslKeyTrust.getIndex(), "], total = [", list.size(), "], but cannot read SSL Certification Information"));
                        //     logMessage(Level.WARN, logData);
                        //     needReloadSslConfiguration = true; // 最後需要重新載入憑證檔設定配置檔
                        // }
                        // 如果讀取憑證檔內容沒有異常, 這裡丟出自定義的異常
                        if (getCertificateException == null) {
                            // throw ExceptionUtil.createException("Cannot read SSL Certificate ", index >= 0 ? "and deactivate" : StringUtils.EMPTY, ", filename = [", filename, "], maybe incorrect password.");
                            throw ExceptionUtil.createException("Cannot read SSL Certificate, filename = [", filename, "], maybe incorrect password.");
                        } else {
                            throw getCertificateException;
                        }
                    }
                } finally {
                    // 最後重新載入憑證檔設定配置檔
                    if (needReloadSslConfiguration) {
                        logData.setSubSys(SubSystem.GW);
                        logData.setChannel(FEPChannel.ATM);
                        logData.setMessageFlowType(MessageFlow.Request);
                        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                        logData.setRemark("Begin to reload SSL Configuration");
                        logMessage(logData);
                        try {
                            // 重新載入ssl
                            loadSsl(logData, true, false);
                            // 記得更新配置檔
                            storeSslConfigs();
                            // logging
                            logData.setSubSys(SubSystem.GW);
                            logData.setChannel(FEPChannel.ATM);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                            logData.setRemark("Succeed to reload SSL Configuration");
                            logMessage(logData);
                        } catch (Exception e) {
                            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
                            logData.setProgramException(e);
                            logData.setRemark("Reload SSL Configuration failed");
                            sendEMS(logData);
                        }
                    }
                }
            }
            return certificate;
        }
    }

//    public String sslAlias(String action, String atmIp, String alias, String alias2, String alias3) throws Exception {
//        sslKeySwitch(2);
//        return "";
//    }

//    public String sslAlias(String action, String atmIp, String alias) throws Exception {
//        if ("set".equalsIgnoreCase(action) && (StringUtils.isBlank(atmIp) || StringUtils.isBlank(alias))) {
//            throw ExceptionUtil.createIllegalArgumentException("Both AtmIp and Alias cannot be empty!!");
//        } else if (("remove".equalsIgnoreCase(action) || "get".equalsIgnoreCase(action))
//                && !this.clientIpToKeyAliasMap.containsKey(atmIp)) {
//            throw ExceptionUtil.createIllegalArgumentException("ATM IP not exist, atmIp = \"", atmIp, "\"!!");
//        }
//        if ("set".equalsIgnoreCase(action)) {
//            this.clientIpToKeyAliasMap.put(atmIp, alias);
//            return StringUtils.join("alias \"", alias, "\" has been set for atmIp \"", atmIp, "\"\r\n");
//        } else if ("remove".equalsIgnoreCase(action)) {
//            String value = this.clientIpToKeyAliasMap.remove(atmIp);
//            return StringUtils.join("alias \"", value, "\" has been removed for atmIp \"", atmIp, "\"\r\n");
//        } else if ("get".equalsIgnoreCase(action)) {
//            String value = this.clientIpToKeyAliasMap.get(atmIp);
//            return StringUtils.join("alias \"", value, "\" was mapped for atmIp \"", atmIp, "\"\r\n");
//        } else if ("list".equalsIgnoreCase(action)) {
//            StringBuilder sb = new StringBuilder();
//            for (Map.Entry<String, String> entry : this.clientIpToKeyAliasMap.entrySet()) {
//                sb.append("atmIp:").append(entry.getKey()).append("\t\t").append("alias:").append(entry.getValue()).append("\r\n");
//            }
//            return sb.toString();
//        }
//        throw ExceptionUtil.createIllegalArgumentException("Invalid action, must be \"set\" or \"remove\" or \"get\"!!");
//    }

    /**
     * 將ssl憑證設定更新到指定的配置檔中
     *
     * @throws Exception
     */
    private void storeSslConfigs() throws Exception {
        Resource resource = new ClassPathResource(CleanPathUtil.cleanString(Const.PROP_FILENAME_ATMGW_SSL));
        File file = resource.getFile();
        // 如果更新配置檔成功, 則刷新配置
        if (configuration.storeSslConfigs(file)) {
            springConfigurationUtil.refreshManually();
            super.printConfiguration();
        }
    }

    public ToATMCommuAtmstatList getAtmstatList(AtmStatus atmStatus, boolean onlyFetchCount) throws Exception {
        ATMGatewayServerToFEPATM toFEPATM = ATMGatewayServerToFEPATM.getInstance(); // SpringBeanFactoryUtil.getBean(ATMGatewayServerToFEPATM.class);
        return toFEPATM.getAtmstatList(this.logContext, atmStatus, onlyFetchCount);
        // return this.commuHelper.getAtmstatList(this.logContext, atmStatus);
    }

    /**
     * 拒絕Client連入, 由子類覆寫
     *
     * @param ch
     * @return
     */
    @Override
    protected boolean channelRejected(SocketChannel ch) {
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        ATMGatewayServerBlackListHandler handler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class, false);
        return handler != null ? handler.isExceededFailuresCount(clientIp) : super.channelRejected(ch);
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    @Override
    protected void channelInitialization(SocketChannel ch) {
        // 取出ClientIp, 判斷是否在BypassCheckAtmIp中, 如果有的話, 則不要列印log
        String clientIp = ReflectUtil.envokeMethod(ch.remoteAddress().getAddress(), "getHostAddress", StringUtils.EMPTY);
        if (this.configuration.getBypassCheckAtmIp().contains(clientIp)) {
            NettyTransmissionUtil.setChannelLoggingDisable(ch, true);
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization][BypassCheckAtmIp] Disabled all logging for ATM IP = [", clientIp, "]");
        } else {
            NettyTransmissionUtil.infoMessage(ch, "[channelInitialization] Get ATM IP = [", clientIp, "]");
        }
    }

    /**
     * 處理異常的類
     *
     * @return
     */
    @Override
    protected NettyTransmissionChannelInboundHandlerAdapterServerException<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> getExceptionHandlerAdapter() {
        if (this.exceptionHandlerAdapter == null) {
            this.exceptionHandlerAdapter = new ATMGatewayServerChannelInboundHandlerAdapterException(this.configuration, this.requestManager);
        }
        return this.exceptionHandlerAdapter;
    }

    /**
     * 由子類去實作電文解碼器, 用來處理一些特殊的電文
     *
     * @return
     */
    @Override
    protected ByteToMessageDecoder getByteToMessageDecoder() {
        // 從ATM Service丟過來的電文, ATMGW在接收時可能會被截斷, 故這裡需要進行特殊解碼處理
        return new NettyTransmissionBaseCommuByteToMessageDecoder();
    }
}
