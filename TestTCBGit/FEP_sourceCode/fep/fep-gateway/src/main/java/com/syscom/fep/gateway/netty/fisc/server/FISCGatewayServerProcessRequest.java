package com.syscom.fep.gateway.netty.fisc.server;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.gateway.entity.Direction;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SendType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestServer;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayUtil;
import com.syscom.fep.gateway.netty.fisc.client.sender.FISCGatewayClientSenderProcessRequest;
import com.syscom.fep.vo.communication.ToFEPFISCCommu;
import com.syscom.fep.vo.communication.ToFISCCommu;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public class FISCGatewayServerProcessRequest extends NettyTransmissionChannelProcessRequestServer<FISCGatewayServerConfiguration> {
    private FISCGatewayClientSenderProcessRequest senderProcessRequest;

    public void initialization(FISCGatewayServerConfiguration configuration, FISCGatewayClientSenderProcessRequest senderProcessRequest) {
        super.initialization(configuration);
        this.senderProcessRequest = senderProcessRequest;
    }

    /**
     * 處理Client進來的電文
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        String message = ConvertUtil.toString(bytes, StandardCharsets.UTF_8);
        // logging
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.FISC);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessage(message);
        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
        logData.setRemark(StringUtils.join(Gateway.FISCGW, " received message from FISCAdapter"));
        this.logMessage(logData);
        try {
            ToFISCCommu toFISCCommu = ToFISCCommu.fromXML(message, ToFISCCommu.class);
            // 交易電文記錄到檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd, 收FEP顯示的收PORT, FISCGW收進來為Receive
            FISCGatewayUtil.logTxMessage(Integer.toString(this.configuration.getPort()), Direction.Receive, toFISCCommu.getMessage());
            // 將電文透過Sender送到FISC
            ToFEPFISCCommu toFEPFISCCommu = this.receiveAndSend(logData, toFISCCommu);
            // 2024-01-18 Richard modified FISCAdapter改為收Queue, 故不用等回應, 所以這裡也就不用回應了
            // String response = StringUtils.EMPTY;
            // if (toFEPFISCCommu != null) {
            //     response = toFEPFISCCommu.toString();
            // }
            // NettyTransmissionUtil.sendPlainMessage(this, this.configuration, ctx.channel(), response);
            // // logging
            // logData.setSubSys(SubSystem.GW);
            // logData.setChannel(FEPChannel.FISC);
            // logData.setMessageFlowType(MessageFlow.Response);
            // logData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            // logData.setMessage(response);
            // logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            // logData.setRemark(StringUtils.join(Gateway.FISCGW, " send message to FISCAdapter"));
            // this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
            sendEMS(logData);
        }
    }

    /**
     * 從FEP收到的財經請求電文, 轉發給財經, 並async/wait得到財經的回應
     * <p>
     * 2024-01-18 Richard modified 收到FISCAdapter電文後, 直接送給財金, 取消原來解析電文取stan register callback的動作
     *
     * @param logData
     * @param toFISCCommu
     * @return
     * @throws Exception
     */
    private ToFEPFISCCommu receiveAndSend(LogData logData, ToFISCCommu toFISCCommu) throws Exception {
        this.putMDC();
        // logging
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.FISC);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setMessage(toFISCCommu.toString());
        logData.setRemark(StringUtils.join(Gateway.FISCGW, " received message from FISCAdapter, timeout:", toFISCCommu.getTimeout()));
        logData.setStep(toFISCCommu.getStep());
        logData.setEj(toFISCCommu.getEj());
        logData.setStan(toFISCCommu.getStan());
        logData.setMessageId(toFISCCommu.getMessageId());
        logData.setTxRquid(toFISCCommu.getTxRquid());
        this.logMessage(logData);
        ToFEPFISCCommu toFEPFISCCommu = null;
        String response = StringUtils.EMPTY;
        try {
            // 2024-01-18 Richard modified 收到FISCAdapter電文後, 直接送給財金, 取消原來解析電文取stan register callback的動作
            // MessageAsynchronousWaitReceiver<String, ToFEPFISCCommu> callback = new MessageAsynchronousWaitReceiver<String, ToFEPFISCCommu>(toFISCCommu.getStan());
            // MessageAsynchronousWaitReceiverManager.subscribeRepeatedly(this, callback);
            logData.setMessage(toFISCCommu.getMessage());
            this.send(logData);
            this.putMDC();
            // if (toFISCCommu.getTimeout() > 0) {
            //     // 多等30秒讓前端timeout, 晚回的可以落入dead queue
            //     if (!callback.waitMessage(toFISCCommu.getTimeout() * 1000L + 10000)) {
            //         logData.setRemark(StringUtils.join(Gateway.FISCGW, " wait send message to FISCAdapter timeout, stan:", toFISCCommu.getStan()));
            //         logData.setMessage(StringUtils.EMPTY);
            //         this.logMessage(logData);
            //         MessageAsynchronousWaitReceiverManager.unsubscribe(this, toFISCCommu.getStan());
            //         return toFEPFISCCommu;
            //     }
            // }
            // 不需要等待財經回應電文
            // toFEPFISCCommu = callback.getMessage();
            // if (toFEPFISCCommu != null) {
            //     response = toFEPFISCCommu.toString();
            // }
            // logData.setRemark(StringUtils.join(Gateway.FISCGW, " send message to FISCAdapter, stan:", toFISCCommu.getStan()));
            // logData.setMessage(response);
            // this.logMessage(logData);
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".receiveAndSend"));
            sendEMS(logData);
        }
        return toFEPFISCCommu;
    }

    /**
     * 電文轉發給財經
     *
     * @param logData
     * @throws Exception
     */
    private void send(LogData logData) throws Exception {
        if (senderProcessRequest != null) {
            senderProcessRequest.send(null, logData, SendType.S);
        }
    }
}
