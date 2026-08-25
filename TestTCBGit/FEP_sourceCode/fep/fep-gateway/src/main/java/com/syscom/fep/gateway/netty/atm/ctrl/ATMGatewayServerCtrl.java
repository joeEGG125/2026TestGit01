package com.syscom.fep.gateway.netty.atm.ctrl;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.ssl.CertificateInformation;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.ssl.SslKeyTrust;
import com.syscom.fep.frmcommon.ssl.SslKeyTrustType;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.gateway.entity.AtmStatus;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.GatewayCodeConstant;
import com.syscom.fep.gateway.netty.NettyTransmissionServerMonitor;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServer;
import com.syscom.fep.gateway.netty.atm.ATMGatewayServerConfiguration;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList;
import com.syscom.fep.vo.communication.ToATMCommuAtmstatList.ToATMCommuAtmstat;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

public class ATMGatewayServerCtrl extends FEPBase {
// 2022-08-23 Richard marked start
// monhelper的程式可以都點掉, 目前ATMGW不會收ATMMon的資料
//    private static String hostName = FEPConfig.getInstance().getHostName();
//
//    @Autowired
//    private EjfnoGenerator ejfnoGenerator;
//
//    static {
//        try {
//            hostName = InetAddress.getLocalHost().getCanonicalHostName();
//        } catch (UnknownHostException e) {
//            hostName = "Unknown";
//        }
//    }
//
//    public void sendMessageToATM(String atmNo, String data) {
//        this.logContext = new LogData();
//        this.logContext.setSubSys(SubSystem.GW);
//        this.logContext.setChannel(FEPChannel.ATM);
//        this.logContext.setMessage(data);
//        this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendMessageToATM"));
//        this.logContext.setRemark(StringUtils.join("ATMGW Recv msg from ATMMON, ATMNO:", atmNo));
//        this.logMessage(this.logContext);
//        try {
//            if (hostName.equals(atmNo)) {
//                if (!StringUtils.isNotBlank(data)) {
//                    String[] cmd = data.split(",");
//                    String msgId = cmd[0];
//                    this.sendAtmGwCommand(msgId);
//                }
//            } else {
//                ATMGatewayServerProcessRequestManager manager = SpringBeanFactoryUtil.getBean(ATMGatewayServerProcessRequestManager.class);
//                ATMGatewayServerProcessRequest processRequest = manager.getATMGatewayServerProcessRequest(atmNo);
//                if (processRequest != null) {
//                    String msgId = StringUtils.EMPTY;
//                    String sendData = StringUtils.EMPTY;
//                    if (data.length() > 24) {
//                        msgId = data.substring(24);
//                        switch (msgId) {
//                            case "CHANGEFST":
//                                this.ej = ejfnoGenerator.generate();
//                                sendData = MessageFormat.format(GatewayCodeConstant.ToATMChangeFirst, PolyfillUtil.toString(this.ej, "0000000000").substring(4));
//                                processRequest.setNeedCheckMac(true);
//                                break;
//                            case "CHANGEKEY":
//                                this.ej = ejfnoGenerator.generate();
//                                sendData = MessageFormat.format(GatewayCodeConstant.ToATMChangeKey, PolyfillUtil.toString(this.ej, "0000000000").substring(4));
//                                processRequest.setNeedCheckMac(true);
//                                break;
//                            case "DISCONNECT":
//                                processRequest.setNeedUpdateAtmstat(true);
//                                processRequest.closeConnection();
//                                return;
//                            default:
//                                msgId = StringUtils.EMPTY;
//                                sendData = data;
//                                break;
//                        }
//                    } else {
//                        sendData = data;
//                    }
//                    processRequest.sendToATM(msgId, sendData);
//                    this.logContext.setMessage(sendData);
//                    this.logContext.setRemark(StringUtils.join("ATMGW Send msg ", msgId, " to ATM:", atmNo, " OK"));
//                    this.logMessage(this.logContext);
//                }
//            }
//        } catch (Exception e) {
//            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendMessageToATM"));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//    }
//
//    private void sendAtmGwCommand(String command) {
//        try {
//            ATMGatewayServerProcessRequestManager manager = SpringBeanFactoryUtil.getBean(ATMGatewayServerProcessRequestManager.class);
//            switch (command) {
//                case "CLS": // Close
//                case "OPN": // Open
//                case "RBT": // Reboot
//                case "SNS": // SNS
//                case "OEX": // OEX
//                    manager.handleAllProcessRequest(processRequest -> {
//                        String data = processRequest.makeAtmMessage(command);
//                        if (StringUtils.isNotBlank(data)) {
//                            processRequest.sendToATM(command, data);
//                            logContext.setMessage(data);
//                            logContext.setRemark(StringUtils.join("ATMGW Send msg ", command, " to ATM:", processRequest.getAtmNo(), " OK"));
//                            logMessage(logContext);
//                        }
//                    });
//                    break;
//                case "StopSvc": // Stop Service
//                    manager.handleAllProcessRequest(processRequest -> {
//                        processRequest.setDisableService(true);
//                        logContext.setRemark(StringUtils.join("ATMGW Send StopSvc to ATM ", processRequest.getAtmNo(), " OK"));
//                        logMessage(logContext);
//                    });
//                    break;
//                case "StartSvc": // Start Service
//                    manager.handleAllProcessRequest(processRequest -> {
//                        processRequest.setDisableService(true);
//                        logContext.setRemark(StringUtils.join("ATMGW Send StartSvc to ATM ", processRequest.getAtmNo(), " OK"));
//                        logMessage(logContext);
//                    });
//                    break;
//            }
//        } catch (Exception e) {
//            this.logContext.setProgramName(StringUtils.join(ProgramName, ".sendAtmGwCommand"));
//            this.logContext.setProgramException(e);
//            sendEMS(this.logContext);
//        }
//    }
// 2022-08-23 Richard marked end

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, Gateway.ATMGW.name());
    }

//    protected String sslKeySwitch(Integer index) throws Exception {
//        putMDC();
//        try {
//            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
//            SslKeyTrust sslKeyTrust = atmGatewayServer.sslKeySwitch(index);
//            return SslContextFactory.getCertificate(sslKeyTrust);
//        } catch (Exception e) {
//            LogData logData = new LogData();
//            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeySwitch"));
//            logData.setProgramException(e);
//            sendEMS(logData);
//            throw e;
//        }
//    }

//    protected String getCurrentSslKey() throws Exception {
//        putMDC();
//        try {
//            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
//            SslKeyTrust sslKeyTrust = atmGatewayServer.getCurrentSslKeyTrust();
//            if (sslKeyTrust == null) {
//                return null;
//            }
//            return SslContextFactory.getCertificate(sslKeyTrust);
//        } catch (Exception e) {
//            LogData logData = new LogData();
//            logData.setProgramName(StringUtils.join(ProgramName, ".getCurrentSslKey"));
//            logData.setProgramException(e);
//            sendEMS(logData);
//            throw e;
//        }
//    }

    protected synchronized String listSslKey(LogData logData, String deactivated) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".listSslKey"));
        logData.setRemark("Ready to list SSL Certification");
        logMessage(logData);
        StringBuilder sb = new StringBuilder();
        try {
            ATMGatewayServerConfiguration atmGatewayServerConfiguration = SpringBeanFactoryUtil.getBean(ATMGatewayServerConfiguration.class);
            List<SslKeyTrust> list = atmGatewayServerConfiguration.getSslConfigs(StringUtils.isBlank(deactivated) ? null : Boolean.parseBoolean(deactivated));
            if (CollectionUtils.isNotEmpty(list)) {
                for (SslKeyTrust sslKeyTrust : list) {
                    sb.append(SslContextFactory.getCertificate(sslKeyTrust, true));
                }
            }
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".listSslKey"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
        return sb.toString();
    }

    protected synchronized String listSslKeyShort(LogData logData, String deactivated) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".listSslKeyShort"));
        logData.setRemark("Ready to list SSL Certification");
        logMessage(logData);
        StringBuilder sb = new StringBuilder();
        try {
            ATMGatewayServerConfiguration atmGatewayServerConfiguration = SpringBeanFactoryUtil.getBean(ATMGatewayServerConfiguration.class);
            List<SslKeyTrust> list = atmGatewayServerConfiguration.getSslConfigs(StringUtils.isBlank(deactivated) ? null : Boolean.parseBoolean(deactivated));
            if (CollectionUtils.isNotEmpty(list)) {
                for (SslKeyTrust sslKeyTrust : list) {
                    List<CertificateInformation> certificateInformationList = SslContextFactory.getCertificateInformationList(sslKeyTrust);
                    if (CollectionUtils.isNotEmpty(certificateInformationList)) {
                        for (CertificateInformation information : certificateInformationList) {
                            sb.append(sslKeyTrust.getIndex()).append("|")
                                    .append(information.getFileName()).append("|")
                                    .append(sslKeyTrust.isDeactivated()).append("|")
                                    .append(information.getAlias()).append("|")
                                    .append(information.getValidDateBegin()).append("|")
                                    .append(information.getValidDateEnd()).append("|")
                                    .append(information.getIssuerDN())
                                    .append("\r\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".listSslKeyShort"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
        return sb.toString();
    }

    protected String sslKeyDeactivate(LogData logData, Integer index) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivate"));
        logData.setRemark("Ready to deactivate SSL Certification");
        logMessage(logData);
        try {
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
            return atmGatewayServer.sslKeyDeactivated(logData, index);
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyDeactivate"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
    }

    protected String sslKeyActivate(LogData logData, Integer index) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivate"));
        logData.setRemark("Ready to activate SSL Certification");
        logMessage(logData);
        try {
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
            return atmGatewayServer.sslKeyActivated(logData, index);
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyActivate"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
    }

    protected String sslKeyAdd(LogData logData, String filename, String sscode, SslKeyTrustType sslKeyTrustType) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
        logData.setRemark("Ready to add SSL Certification");
        logMessage(logData);
        try {
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
            return atmGatewayServer.sslKeyAdd(logData, filename, sscode, sslKeyTrustType);
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
    }

//    protected String sslAlias(String action, String atmIp, String alias) throws Exception {
//        putMDC();
//        try {
//            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
//            return atmGatewayServer.sslAlias(action, atmIp, alias);
//        } catch (Exception e) {
//            LogData logData = new LogData();
//            logData.setProgramName(StringUtils.join(ProgramName, ".sslKeyAdd"));
//            logData.setProgramException(e);
//            sendEMS(logData);
//            throw e;
//        }
//    }

    protected String getMonitorData(boolean listClient) {
        putMDC();
        ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
        NettyTransmissionServerMonitor<ATMGatewayServerConfiguration> monitorData = atmGatewayServer.getTransmissionServerMonitor();
        StringBuilder sb = new StringBuilder();
        sb.append(monitorData.getServiceName()).append("(").append(FEPConfig.getInstance().getHostIp()).append(")").append(":\r\n");
        sb.append("\tLocal:").append(monitorData.getHostIp()).append(":").append(monitorData.getHostPort()).append("\r\n");
        // 2023-04-07 Richard modified 這裡查詢連線數以DB為準
        try {
            long currentConnections = 0; // monitorData.getConnections();
            ToATMCommuAtmstatList toATMCommuAtmstatList = atmGatewayServer.getAtmstatList(AtmStatus.Connected, true); // 只取Count數
            // if (toATMCommuAtmstatList.getAtmstatList() != null) {
            //     currentConnections = toATMCommuAtmstatList.getAtmstatList().stream().filter(t -> t.getAtmstatStatus() == AtmStatus.Connected.getValue()).count();
            // }
            if (toATMCommuAtmstatList.getCount() >= 0) {
                currentConnections = toATMCommuAtmstatList.getCount();
            }
            if (monitorData.getConnections() != currentConnections) {
                monitorData.setConnections(currentConnections);
            }
            sb.append("\tCurrent Connections:").append(FormatUtil.longFormat(currentConnections)).append("\r\n");
        } catch (Exception e) {
            sb.append("\tCurrent Connections:Get failed with exception occur, ").append(e.getMessage()).append("\r\n");
        }
        sb.append("\tLatest Active Date Time:")
                .append(monitorData.getLatestActiveDateTime() == null ? StringUtils.EMPTY
                        : FormatUtil.dateTimeFormat(monitorData.getLatestActiveDateTime().getTime())).append("\r\n");
        if (listClient) {
            sb.append(this.clientList(AtmStatus.Disconnected, "\t"));
        }
        return sb.toString();
    }

    /**
     * 獲取ATM Client的列表
     *
     * @param atmStatus
     * @param tab
     * @return
     */
    protected String clientList(AtmStatus atmStatus, String tab) {
        putMDC();
        ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
        StringBuilder sb = new StringBuilder();
        try {
            ToATMCommuAtmstatList toATMCommuAtmstatList = atmGatewayServer.getAtmstatList(atmStatus, false);
            if (toATMCommuAtmstatList.getAtmstatList() != null) {
                sb.append(tab).append("ATM Client List(").append(toATMCommuAtmstatList.getAtmstatList().size()).append(" Total");
                long disconnectedCount = 0, connectedCount = 0;
                if (atmStatus == null || atmStatus == AtmStatus.Disconnected) {
                    disconnectedCount = toATMCommuAtmstatList.getAtmstatList().stream().filter(t -> t.getAtmstatStatus() == AtmStatus.Disconnected.getValue()).count();
                }
                if (atmStatus == null || atmStatus == AtmStatus.Connected) {
                    connectedCount = toATMCommuAtmstatList.getAtmstatList().stream().filter(t -> t.getAtmstatStatus() == AtmStatus.Connected.getValue()).count();
                }
                if (disconnectedCount > 0) {
                    sb.append(", ").append(disconnectedCount).append(" Disconnected");
                }
                if (connectedCount > 0) {
                    sb.append(", ").append(connectedCount).append(" Connected");
                }
                sb.append("):\r\n");
                for (ToATMCommuAtmstat atmstat : toATMCommuAtmstatList.getAtmstatList()) {
                    sb.append(tab).append("\tATM ").append(atmstat.getAtmstatAtmno()).
                            append(StringUtils.SPACE).
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus())).
                            append(StringUtils.SPACE).
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus()).getValue() == 1 ? "" : "at").
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus()).getValue() == 1 ? "" : StringUtils.SPACE).
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus()).getValue() == 1 ? "" : atmstat.getAtmmstrAtmpIp()).
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus()).getValue() == 1 ? "" : StringUtils.SPACE).
                            append("on").
                            append(StringUtils.SPACE).
                            append(AtmStatus.fromValue(atmstat.getAtmstatStatus()).getValue() == 1 ? atmstat.getAtmstatLastClose() : atmstat.getAtmstatLastOpen()).
                            append("!\r\n");
                }
            }
        } catch (Exception e) {
            sb.append(tab).append("Get ATM Client list with exception occur, ").append(e.getMessage()).append("\r\n");
        }
        return sb.toString();
    }

    /**
     * 將application-gateway-atm-ssl.properties檔從備份的application-gateway-atm-ssl.properties.bak恢復
     *
     * @param logData
     * @return
     * @throws Exception
     */
    public synchronized String sslRestore(LogData logData) throws Exception {
        putMDC();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.ATM);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.ATMGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".sslRestore"));
        logData.setRemark("Ready to restore SSL Certification");
        logMessage(logData);
        try {
            Resource resource = new ClassPathResource(CleanPathUtil.cleanString(Const.PROP_FILENAME_ATMGW_SSL));
            File file = resource.getFile();
            File backup = new File(StringUtils.join(file.getAbsolutePath(), GatewayCodeConstant.EXTENSION_BAK));
            if (!backup.exists()) {
                throw ExceptionUtil.createException("backup file was not exist!!!");
            }
            FileUtils.copyFile(backup, file);
            SpringConfigurationUtil springConfigurationUtil = SpringBeanFactoryUtil.getBean(SpringConfigurationUtil.class);
            springConfigurationUtil.refreshManually();
            Thread.sleep(1000L); // 這裡sleep一下, 等待重新載入配置檔到configuration物件中
            ATMGatewayServerConfiguration atmGatewayServerConfiguration = SpringBeanFactoryUtil.getBean(ATMGatewayServerConfiguration.class);
            LogHelperFactory.getGeneralLogger().info(atmGatewayServerConfiguration);
            ATMGatewayServer atmGatewayServer = SpringBeanFactoryUtil.getBean(ATMGatewayServer.class);
            atmGatewayServer.loadSsl(logData, true, false);
            return this.listSslKey(logData, Boolean.FALSE.toString());
        } catch (Exception e) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sslRestore"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw e;
        }
    }
}
