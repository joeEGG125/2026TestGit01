package com.syscom.fep.gateway.netty.fisc.client.receiver;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.entity.Gateway;
import com.syscom.fep.gateway.entity.SocketType;
import com.syscom.fep.gateway.netty.NettyTransmissionChannelInboundHandlerAdapterClient;
import com.syscom.fep.gateway.netty.NettyTransmissionUtil;
import com.syscom.fep.gateway.netty.fisc.client.FISCGatewayClientProcessRequest;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.List;

@Sharable
public class FISCGatewayClientReceiverChannelInboundHandlerAdapter extends NettyTransmissionChannelInboundHandlerAdapterClient<FISCGatewayClientReceiverConfiguration, FISCGatewayClientReceiverProcessRequest> {
    private static final int LL_LENGTH = 2; // 交易類電文長度欄位的長度
    private final RefBase<byte[]> lastDisassembledRemainingMessages = new RefBase<>(null);

    /**
     * 拆解電文
     *
     * @param ctx
     * @param bytes
     * @return
     */
    @Override
    protected List<byte[]> disassembleTransmissionMessage(ChannelHandlerContext ctx, byte[] bytes) {
        LogData logData = new LogData();
        // 如果上次有剩餘截取的電文, 則要與這次收進來的電文拼接在一起
        synchronized (lastDisassembledRemainingMessages) {
            byte[] remaining = lastDisassembledRemainingMessages.get();
            if (ArrayUtils.isNotEmpty(remaining)) {
                int receivedSize = bytes.length;
                bytes = ArrayUtils.addAll(remaining, bytes);
                log(ctx, remaining, logData, StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " combine the remaining data disassemble last time, received size:", receivedSize, ", remaining size:", remaining.length, ", combine size:", bytes.length));
                lastDisassembledRemainingMessages.set(null);
            }
        }
        int offset = 0, remainder, ll, messageCount = 0;
        List<byte[]> disassembledMessages = new ArrayList<>();
        while (true) {
            // 計算剩餘電文長度
            remainder = bytes.length - offset;
            if (remainder <= 0)
                break;
            // RSM電文
            ll = 20;
            if (remainder >= ll && FISCGatewayClientProcessRequest.RSM_ID.equals(StringUtil.toHex(ArrayUtils.subarray(bytes, offset + 4, offset + 12)))) {
                if (offset + ll > bytes.length) {
                    break;
                }
                messageCount++;
                addDisassembledMessages(ctx, disassembledMessages, ArrayUtils.subarray(bytes, offset, offset + ll), logData,
                        StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " disassemble the RSM data received From FISC, messageNo = ", messageCount));
                offset += ll;
                continue;
            }
            // 交易類電文
            if (remainder > LL_LENGTH) {
                // 取出電文長度
                ll = Integer.parseInt(StringUtil.toHex(ArrayUtils.subarray(bytes, offset, offset + LL_LENGTH)), 16);
                if (offset + ll > bytes.length) {
                    break;
                }
                messageCount++;
                addDisassembledMessages(ctx, disassembledMessages, ArrayUtils.subarray(bytes, offset, offset + ll), logData,
                        StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " disassemble the Transaction data received From FISC, messageNo = ", messageCount));
                offset += ll;
                continue;
            }
            // 上述都不符合要求, 則直接增加返回
            break;
        }
        // 最後一筆電文處理
        if (disassembledMessages.size() == 1)
            doProcess(ctx, processRequest, disassembledMessages.remove(0), messageCount != 1 && this.configuration.isAsyncDisassembled());
        // 最後的一筆電文長度不夠, 則保存剩餘電文
        if (remainder > 0) {
            synchronized (lastDisassembledRemainingMessages) {
                lastDisassembledRemainingMessages.set(ArrayUtils.subarray(bytes, offset, bytes.length));
                log(ctx, lastDisassembledRemainingMessages.get(), logData, StringUtils.join(Gateway.FISCGW, StringUtils.SPACE, SocketType.Receiver, " detected that there are also remaining data received From FISC, messageNo = ", messageCount + 1));
            }
        }
        return disassembledMessages;
    }

    private void addDisassembledMessages(ChannelHandlerContext ctx, List<byte[]> disassembledMessages, byte[] bytes, LogData logData, String remark) {
        // 如果disassembledMessages非空, 則每次處理前一筆電文, 再增加當前電文到disassembledMessages裡面
        if (!disassembledMessages.isEmpty()) {
            doProcess(ctx, processRequest, disassembledMessages.remove(0), this.configuration.isAsyncDisassembled());
            disassembledMessages.add(bytes);
            log(ctx, bytes, logData, remark);
            return;
        }
        disassembledMessages.add(bytes);
        log(ctx, bytes, logData, remark);
    }

    private void log(ChannelHandlerContext ctx, byte[] bytes, LogData logData, String remark) {
        String message = StringUtil.toHex(bytes);
        logData.setSubSys(SubSystem.GW);
        logData.setChannel(FEPChannel.FISC);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramFlowType(ProgramFlow.FISCGatewayIn);
        logData.setProgramName(StringUtils.join(ProgramName, ".log"));
        logData.setMessage(message);
        logData.setRemark(remark);
        FEPBase.logMessage(Level.WARN, logData);
        NettyTransmissionUtil.warnMessage(ctx.channel(), remark, ", message = [", message, "]");
    }
}