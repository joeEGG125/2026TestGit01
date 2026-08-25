package com.syscom.fep.gateway.netty.atm;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterServerException;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInformation;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.NotSslRecordException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import javax.net.ssl.SSLHandshakeException;
import java.net.InetSocketAddress;

@Sharable
public class ATMGatewayServerChannelInboundHandlerAdapterException extends NettyTransmissionChannelInboundHandlerAdapterServerException<ATMGatewayServerConfiguration, ATMGatewayServerProcessRequestManager, ATMGatewayServerProcessRequest> {

    public ATMGatewayServerChannelInboundHandlerAdapterException(ATMGatewayServerConfiguration configuration, ATMGatewayServerProcessRequestManager manager) {
        this.initialization(configuration, manager);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.putMDC(ctx);
        LogData logData = null;
        String atmNo = StringUtils.EMPTY, clientIP = StringUtils.EMPTY;
        int clientPort = 0;
        ATMGatewayServerProcessRequest processRequest = processRequestManager.getNettyChannelProcessRequest(ctx);
        // 如果checkATM主檔被拒絕連線並且有異常(例如SSL異常), 這裡得到的processRequest會是null
        if (processRequest == null) {
            logData = new LogData();
            InetSocketAddress remoteAddr = (InetSocketAddress) ctx.channel().remoteAddress();
            if (remoteAddr != null) {
                clientIP = ReflectUtil.envokeMethod(remoteAddr.getAddress(), "getHostAddress", StringUtils.EMPTY);
                clientPort = remoteAddr.getPort();
            }
        } else {
            logData = processRequest.getLogContext();
            atmNo = processRequest.getAtmNo();
            clientIP = processRequest.getClientIP();
            clientPort = processRequest.getClientPort();
        }
        logData.setProgramException(cause);
        logData.setProgramName(StringUtils.join(ProgramName, ".exceptionCaught"));
        logData.setRemark(StringUtils.join("Close the connection when an exception is raised", cause != null ? StringUtils.join(", ", cause.getMessage()) : StringUtils.EMPTY));
        // 憑證不正確
        boolean verifySslFailed = false;
        NotSslRecordException notSslRecordException = (NotSslRecordException) ExceptionUtil.find(cause, t -> t instanceof NotSslRecordException);
        if (notSslRecordException != null) {
            verifySslFailed = true;
            logData.setProgramException(null);
            logData.setReturnCode(FEPReturnCode.NOT_SSL_RECORD);
            logData.setRemark(StringUtils.join("ATM未使用憑證建立連線, ", notSslRecordException.getMessage(), ", ATM(NO:", atmNo, ", IP:", clientIP, ", Port:", clientPort, ")"));
        } else {
            SSLHandshakeException sslHandshakeException = (SSLHandshakeException) ExceptionUtil.find(cause, t -> t instanceof SSLHandshakeException);
            if (sslHandshakeException != null) {
                verifySslFailed = true;
                FEPReturnCode rtnCode = FEPReturnCode.INVALID_CERTIFICATE;
                String remark = "ATMGW驗證憑證不正確";
                if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "Received fatal alert: certificate_expired".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.CERTIFICATE_EXPIRED;
                    remark = "ATMGW驗證憑證已過期";
                } else if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "no cipher suites in common".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.INVALID_CERTIFICATE_ALIAS;
                    remark = "ATMGW驗證憑證ALIAS不正確";
                } else if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && "Received fatal alert: unknown_ca".equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.CERTIFICATE_NOT_MATCH;
                    remark = "ATMGW驗證憑證與ATM不一致";
                } else if (StringUtils.isNotBlank(sslHandshakeException.getMessage()) && FEPReturnCode.SSL_HANDSHAKE_NOT_COMPLETION.name().equals(sslHandshakeException.getMessage())) {
                    rtnCode = FEPReturnCode.SSL_HANDSHAKE_NOT_COMPLETION;
                    remark = "SSL Handshake Completion was not successful!!!";
                }
                logData.setProgramException(null);
                logData.setReturnCode(rtnCode);
                logData.setRemark(StringUtils.join(remark, " ", sslHandshakeException.getMessage(), ", ATM(NO:", atmNo, ", IP:", clientIP, ", Port:", clientPort, ")"));
            }
        }
        // 這裡要清掉ReturnCode否則會一直列印出來
        if (verifySslFailed) {
            // 這裡要判斷一下是否有嘗試過所有的憑證都不是有效的憑證, 則才送EMS Alert
            ATMGatewayServerClientIpToCertNoHandler atmGatewayServerClientIpToCertNoHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerClientIpToCertNoHandler.class, false);
            // 這裡判斷一下是否於有try所有的CertNo
            if (processRequest != null && atmGatewayServerClientIpToCertNoHandler != null && atmGatewayServerClientIpToCertNoHandler.isTryoutAllCert(clientIP)) {
                FEPBase.sendEMS(logData);
                // 將ATM主檔中憑證Alias欄位更新為空白
                processRequest.setCertAlias(StringUtils.EMPTY);
                processRequest.updateAtmmstr();
            } else {
                LogHelperFactory.getTraceLogger().error(cause, logData.getRemark());
                FEPBase.logMessage(Level.WARN, logData);
            }
            logData.setReturnCode(null);
            if (processRequest != null) {
                // 2025-12-15 Richard modified
                // 有時是憑證ssl timeout造成連不上, 不代表不給他連, 需要多try幾次, 如果一下就拉黑, 就又要等3分鐘了, 所以黑名單以check atmmstr不存在的為主 by Ashiang
                // 設定這個Channel需要拒絕
                // NettyTransmissionUtil.setChannelReject(ctx.channel(), true);
                // 更新失敗次數
                // ATMGatewayServerBlackListHandler atmGatewayServerBlackListHandler = SpringBeanFactoryUtil.getBean(ATMGatewayServerBlackListHandler.class, false);
                // if (atmGatewayServerBlackListHandler != null) {
                //     atmGatewayServerBlackListHandler.incrementFailedTimes(clientIP);
                // }
            }
        } else {
            FEPBase.sendEMS(logData);
        }
        NettyTransmissionUtil.errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised");
        Channel channel = ctx.channel();
        notification.notifyConnStateChanged(channel.id().asLongText(), channel, NettyTransmissionConnState.CLIENT_DISCONNECTED, cause);
        ctx.close();
    }
}
