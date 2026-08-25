package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Sharable
public class NettyTransmissionChannelInboundHandlerAdapterClientException<Configuration extends NettyTransmissionClientConfiguration, ProcessRequest extends NettyTransmissionChannelProcessRequestClient<Configuration>>
        extends NettyTransmissionChannelInboundHandlerAdapter<Configuration> {
    @Autowired
    private ProcessRequest processRequest;

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     * @param processRequest
     */
    public void initialization(Configuration configuration, ProcessRequest processRequest) {
        super.initialization(configuration);
        this.processRequest = processRequest;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 注意不要實作, NettyTransmissionChannelInboundHandlerAdapter中會實作
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.putMDC(ctx);
        LogData logData = null;
        if (processRequest != null && processRequest.getLogContext() != null)
            logData = processRequest.getLogContext();
        if (logData == null) {
            logData = new LogData();
        }
        logData.setProgramException(cause);
        logData.setProgramName(StringUtils.join(ProgramName, ".exceptionCaught"));
        logData.setRemark(StringUtils.join("Close the connection when an exception is raised", cause != null ? StringUtils.join(", ", cause.getMessage()) : StringUtils.EMPTY));
        FEPBase.sendEMS(logData);
        NettyTransmissionUtil.errorMessage(ctx.channel(), cause, "Close the connection when an exception is raised");
        notification.notifyConnStateChanged(this.configuration.getNettyTransmissionNotificationKey(), ctx.channel(), NettyTransmissionConnState.CLIENT_DISCONNECTED, cause);
        ctx.close().addListener(future -> {
            this.notifyForClose();
        });
    }
}
