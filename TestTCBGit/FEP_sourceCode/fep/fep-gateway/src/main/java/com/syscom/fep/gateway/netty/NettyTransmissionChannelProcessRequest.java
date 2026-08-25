package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;

public abstract class NettyTransmissionChannelProcessRequest<Configuration extends NettyTransmissionConfiguration> extends FEPBase implements NettyTransmissionConnStateListener {
    @Autowired
    protected Configuration configuration;
    protected ChannelHandlerContext channelHandlerContext;
    protected NettyTransmissionChannelInformation channelInformation = new NettyTransmissionChannelInformation();
    protected AtomicReference<NettyTransmissionConnState> currentConnState = new AtomicReference<NettyTransmissionConnState>(NettyTransmissionConnState.CLIENT_DISCONNECTED);
    @Autowired
    protected NettyTransmissionNotification notification;

    /**
     * 處理Client進來的電文
     *
     * @param ctx
     * @param bytes
     * @throws Exception
     */
    public abstract void doProcess(ChannelHandlerContext ctx, byte[] bytes) throws Exception;

    /**
     * 關閉與Client的連線
     */
    public abstract void closeConnection();

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    public void initialization(Configuration configuration) {
        this.notification = SpringBeanFactoryUtil.getBean(NettyTransmissionNotification.class);
        this.configuration = configuration;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
        NettyTransmissionUtil.getChannelInformation(this.channelInformation, channelHandlerContext.channel());
    }

    @Override
    public void connStateChanged(Channel channel, NettyTransmissionConnState state, Throwable t) {
        putMDC();
        this.currentConnState.set(state);
        NettyTransmissionUtil.infoMessage(channel, "connection state changed, listener = [", this.ProgramName, "], status = [", state, "], exception message = [", (t != null ? t.getMessage() : StringUtils.EMPTY), "]");
        if (state == NettyTransmissionConnState.CLIENT_CONNECTED) {
            channelInformation.setOnlineDateTime(Calendar.getInstance()); // 客戶端開始連線的時間
        } else if (state == NettyTransmissionConnState.CLIENT_DISCONNECTED) {
            channelInformation.setOfflineDateTime(Calendar.getInstance()); // 客戶端斷線的時間
        } else if (state == NettyTransmissionConnState.CLIENT_MESSAGE_INCOMING || state == NettyTransmissionConnState.SERVER_MESSAGE_INCOMING) {
            channelInformation.setLastActiveDateTime(Calendar.getInstance()); // 最後一次活躍的時間
        }
    }

    protected void putMDC() {
        NettyTransmissionUtil.putMDC(this.channelHandlerContext, this.configuration);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public NettyTransmissionConnState getCurrentConnState() {
        return currentConnState.get();
    }
}
