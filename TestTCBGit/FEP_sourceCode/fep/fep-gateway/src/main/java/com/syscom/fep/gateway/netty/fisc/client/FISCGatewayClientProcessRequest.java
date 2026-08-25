package com.syscom.fep.gateway.netty.fisc.client;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Direction;
import com.syscom.fep.gateway.entity.SendType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelProcessRequestClient;
import com.syscom.fep.gateway.netty.NettyTransmissionConnState;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.fisc.FISCGatewayUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;

@StackTracePointCut(caller = SvrConst.SVR_FISC_GATEWAY)
public abstract class FISCGatewayClientProcessRequest<Configuration extends FISCGatewayClientConfiguration> extends NettyTransmissionChannelProcessRequestClient<Configuration> {
    // 表示Confirm
    protected String flag3_Confirm = StringUtils.EMPTY;
    // 表示Confirm+ Cancel Client ID
    protected String flag3_Cancel = StringUtils.EMPTY;
    // 傳送交易資料
    protected String flag4_S = StringUtils.EMPTY;
    // 送出RESUME TPIPE的需求
    protected String flag4_R = StringUtils.EMPTY;
    // 送出acknowledgement (ACK)訊息通知財金公司端資料已收到
    protected String flag4_A = StringUtils.EMPTY;
    // 送出negative acknowledgement(NACK)訊息通知財金公司端交易資料不完整
    protected String flag4_N = StringUtils.EMPTY;
    // 送出Cancel Timer需求，通知財金公司端清除因為不正常斷線
    protected String flag4_C = StringUtils.EMPTY;
    // Private Shared encType As String = StringUtils.EMPTY
    protected String trancode = StringUtils.EMPTY;
    protected String datastore = StringUtils.EMPTY;
    protected String lterm = StringUtils.EMPTY;
    protected String racfuid = StringUtils.EMPTY;
    protected String racfgnm = StringUtils.EMPTY;
    protected String passticket = StringUtils.EMPTY;
    // ClientID重覆的狀態
    protected boolean clientIdRepeat;
    // *REQSTS*
    public static String RSM_ID = "";
    protected String outQueueName = StringUtils.EMPTY;
    protected String fiscId = "";


    protected abstract void doProcess(ChannelHandlerContext ctx, LogData logData, String message) throws Exception;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    @Override
    public void initialization(Configuration configuration) {
        super.initialization(configuration);
        this.initFISCGatewayClientProcessRequest();
    }

    @PostConstruct
    public void initFISCGatewayClientProcessRequest() {
        if ("ascii".equals(configuration.getEncoding())) {
            flag3_Confirm = "01";
            flag3_Cancel = "81";
            flag4_S = "53";
            flag4_R = "52";
            flag4_A = "41";
            flag4_N = "4E";
            flag4_C = "43";
            trancode = "2020202020202020";
            datastore = "2020202020202020";
            lterm = "2020202020202020";
            racfuid = "2020202020202020";
            racfgnm = "2020202020202020";
            passticket = "2020202020202020";
            fiscId = "4649534349434F4E";
            RSM_ID = "2A5245515354532A";
            this.configuration.setClientIdHex(StringUtil.toHex(this.configuration.getClientId()));
            this.configuration.setCheckCodeHex(StringUtil.toHex(this.configuration.getCheckCode()));
        } else {
            flag3_Confirm = "01";
            flag3_Cancel = "81";
            flag4_S = "E2";
            flag4_R = "D9";
            flag4_A = "C1";
            flag4_N = "D5";
            flag4_C = "C3";
            trancode = "4040404040404040";
            datastore = "4040404040404040";
            lterm = "4040404040404040";
            racfuid = "4040404040404040";
            racfgnm = "4040404040404040";
            passticket = "4040404040404040";
            fiscId = "C6C9E2C3C9C3D6D5";
            RSM_ID = "5CD9C5D8E2E3E25C";
            this.configuration.setClientIdHex(EbcdicConverter.toHex(CCSID.English, this.configuration.getClientId().length(), this.configuration.getClientId()));
            this.configuration.setCheckCodeHex(EbcdicConverter.toHex(CCSID.English, this.configuration.getCheckCode().length(), this.configuration.getCheckCode()));
        }
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        String message = StringUtil.toHex(bytes);
        LogData logData = new LogData();
        logData.setSubSys(SubSystem.INBK);
        logData.setChannel(FEPChannel.FISC);
        logData.setMessage(message);
        logData.setProgramName(StringUtils.join(ProgramName, ".doProcess"));
        logData.setRemark("Socket Received message from FISC");
        this.logMessage(logData);
        this.doProcess(ctx, logData, message);
        LogHelperFactory.getTraceLogger().info("Socket Receive Event finished.");
    }

    public void send(Channel channel, LogData sendData, SendType sendType) throws Exception {
        this.putMDC();
        Channel ch = channel == null ? this.channelHandlerContext.channel() : channel;
        // 這裡加上一個判斷, 如果當前斷線中, 則不能送訊息到財經, 直接丟異常出去
        if (!NettyTransmissionConnState.isClientConnected(this.currentConnState.get())) {
            String errorMessage = "Disconnect from FISC socket server, cannot send any message!!!";
            if (this.channelHandlerContext != null) {
                NettyTransmissionUtil.warnMessage(ch, errorMessage, "sendType = [", sendType, "]");
            }
            throw ExceptionUtil.createException(errorMessage);
        }
        String sChannel = StringUtils.EMPTY;
        String logString = StringUtils.EMPTY;
        String msg = StringUtils.EMPTY;
        switch (sendType) {
            case A:
                sChannel = "FISCGW";
                msg = this.getAck();
                logString = configuration.getSocketType() + " Send Ack OK ";
                break;
            case N:
                sChannel = "FISCGW";
                logString = configuration.getSocketType() + " Send Nack OK ";
                break;
            case R:
                sChannel = "FISCGW";
                msg = getRipe();
                logString = configuration.getSocketType() + " Send Ripe OK ";
                break;
            case S:
                sChannel = outQueueName;
                // 980214 FISC新規定
                // 當參加單位端之Sender收到Duplicate ClientID之RSM(08/38)後，財金公司端會主動斷線
                // 重新連結後，若於Sender端則發送Send Message + Cancel Client ID
                if (clientIdRepeat) {
                    clientIdRepeat = false;
                    msg = getSendData(sendData.getMessage(), flag3_Cancel);
                    logString = configuration.getSocketType() + " Send Tx Data + Cancel Client ID OK ";
                } else {
                    msg = getSendData(sendData.getMessage(), flag3_Confirm);
                    logString = configuration.getSocketType() + " Send Tx Data OK ";
                }
                // 交易電文記錄到檔名為fep-gateway-fisc-FISCGW-transaction-yyyy-MM-dd
                FISCGatewayUtil.logTxMessage(this.configuration.getClientId(), Direction.Send, sendData.getMessage());
                break;
            case CS:
                sChannel = "FISCGW";
                msg = getCancel(flag4_S);
                logString = configuration.getSocketType() + " Send Cancel Client ID OK ";
                break;
            case CR:
                sChannel = "FISCGW";
                msg = getCancel(flag4_R);
                logString = configuration.getSocketType() + " Send Ripe + Cancel Client ID OK ";
                break;
        }
        try {
            NettyTransmissionUtil.sendHexMessage(this, this.configuration, ch, msg);
            sendData.setStep(sendData.getStep());
            sendData.setStan(sendData.getStan());
            sendData.setMessageId(sendData.getMessageId());
            sendData.setEj(sendData.getEj());
            sendData.setProgramName(StringUtils.join(ProgramName, ".send"));
            sendData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            sendData.setMessageFlowType(MessageFlow.Response);
            sendData.setMessage(msg);
            sendData.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            ",From:", sChannel, "] ",
                            logString));
            this.logMessage(sendData);
        } catch (Exception e) {
            sendData.setStep(sendData.getStep());
            sendData.setStan(sendData.getStan());
            sendData.setMessageId(sendData.getMessageId());
            sendData.setEj(sendData.getEj());
            sendData.setProgramName(StringUtils.join(ProgramName, ".send"));
            sendData.setProgramFlowType(ProgramFlow.FISCGatewayOut);
            sendData.setMessageFlowType(MessageFlow.Response);
            sendData.setMessage(msg);
            sendData.setRemark(
                    StringUtils.join("[", configuration.getSocketType(),
                            " IP:", configuration.getHost(),
                            ",Port:", configuration.getPort(),
                            " Send Data error"));
            sendData.setProgramException(e);
            sendEMS(sendData);
        }
    }

    // 財金交易電文=LLLL + IRM + LLZZData + 00040000
    private String getSendData(String fiscData, String flag3) {
        String LL = StringUtils.leftPad(Integer.toHexString(fiscData.length() / 2 + 4).toUpperCase(), 4, '0');
        String ZZ = "0000";
        String data = StringUtils.join(getIRM(configuration.getClientIdHex(), flag3, flag4_S), LL, ZZ, fiscData, "00040000");
        data = StringUtils.join(StringUtils.leftPad(Integer.toHexString(data.length() / 2 + 4).toUpperCase(), 8, '0'), data);
        return data;
    }

    private String getAck() {
        // ripe=LLLL + IRM + LLZZ + 00040000
        String ack = StringUtils.join(getIRM(configuration.getClientIdHex(), flag3_Confirm, flag4_A), "0004", "0000", "00040000");
        ack = StringUtils.join(StringUtils.leftPad(Integer.toHexString(ack.length() / 2 + 4).toUpperCase(), 8, '0'), ack);
        return ack;
    }

    private String getRipe() {
        // ripe=LLLL + IRM + LLZZ + 00040000
        String ripe = StringUtils.join(getIRM(configuration.getClientIdHex(), flag3_Confirm, flag4_R), "0004", "0000", "00040000");
        ripe = StringUtils.join(StringUtils.leftPad(Integer.toHexString(ripe.length() / 2 + 4).toUpperCase(), 8, '0'), ripe);
        return ripe;
    }

    private String getCancel(String flag4) {
        // ripe=LLLL + IRM + LLZZ + 00040000
        String cancel = StringUtils.join(getIRM(configuration.getClientIdHex(), flag3_Cancel, flag4), "0004", "0000", "00040000");
        cancel = StringUtils.join(StringUtils.leftPad(Integer.toHexString(cancel.length() / 2 + 4).toUpperCase(), 8, '0'), cancel);
        return cancel;
    }

    private String getIRM(String clientId, String flag3, String flag4) {
        String irm = StringUtils.join(
                "0058",
                "0000",
                fiscId,
                "00000000",
                "02",
                "64",
                "10",
                "00",
                clientId,
                "00",
                "40",
                flag3,
                flag4,
                trancode,
                datastore,
                lterm,
                racfuid,
                racfgnm,
                configuration.getCheckCodeHex(),
                passticket);
        return irm;
    }
}
