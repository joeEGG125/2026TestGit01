package com.syscom.fep.gateway.netty;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.frmcommon.delegate.ActionListener;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil.Scope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyTransmissionChannelProcessRequestServerManager<Configuration extends NettyTransmissionServerConfiguration, ProcessRequest extends NettyTransmissionChannelProcessRequestServer<Configuration>> extends FEPBase {
    @Autowired
    protected Configuration configuration;
    private final Map<String, ProcessRequest> channelIdToNettyChannelProcessRequestMap = new ConcurrentHashMap<>();
    private final Class<ProcessRequest> processRequestClass = GenericTypeUtil.getGenericSuperClass(this.getClass(), 1);
    private NettyTransmissionServerMonitor<Configuration> transmissionServerMonitor;

    @PostConstruct
    public void initialization() {}

    @PreDestroy
    public void destroy() {}

    public void setTransmissionServerMonitor(NettyTransmissionServerMonitor<Configuration> transmissionServerMonitor) {
        this.transmissionServerMonitor = transmissionServerMonitor;
    }

    public ProcessRequest addNettyChannelProcessRequest(ChannelHandlerContext ctx) {
        boolean flag = false;
        try {
            Channel channel = ctx.channel();
            String channelId = channel.id().asLongText();
            ProcessRequest channelProcessRequest = this.channelIdToNettyChannelProcessRequestMap.get(channelId);
            if (channelProcessRequest == null) {
                try {
                    channelProcessRequest = this.createProcessRequest();
                    channelProcessRequest.setChannelHandlerContext(ctx);
                    this.channelIdToNettyChannelProcessRequestMap.put(channelId, channelProcessRequest);
                    NettyTransmissionUtil.infoMessage(channel, "Add channelProcessRequest to manager succeed, channelProcessRequest = [", channelProcessRequest, "]");
                    flag = true;
                } catch (Exception e) {
                    NettyTransmissionUtil.errorMessage(channel, e, "Add channelProcessRequest to manager failed with exception occur");
                }
            } else {
                NettyTransmissionUtil.warnMessage(channel, "ChannelProcessRequest exist in manager, channelProcessRequest = [", channelProcessRequest, "]");
            }
            return channelProcessRequest;
        } finally {
            if (this.transmissionServerMonitor != null) {
                // this.transmissionServerMonitor.setConnections(this.channelIdToNettyChannelProcessRequestMap.size());
                if (flag) {
                    this.transmissionServerMonitor.setLatestActiveDateTime(Calendar.getInstance());
                }
            }
        }
    }

    public ProcessRequest removeNettyChannelProcessRequest(ChannelHandlerContext ctx) {
        try {
            Channel channel = ctx.channel();
            String channelId = channel.id().asLongText();
            ProcessRequest channelProcessRequest = this.channelIdToNettyChannelProcessRequestMap.remove(channelId);
            if (channelProcessRequest != null) {
                channelProcessRequest.closeConnection();
            }
            NettyTransmissionUtil.infoMessage(channel, "Remove channelProcessRequest from manager succeed, channelProcessRequest = [", channelProcessRequest, "]");
            return channelProcessRequest;
        } finally {
            // if (this.transmissionServerMonitor != null) {
            //     this.transmissionServerMonitor.setConnections(this.channelIdToNettyChannelProcessRequestMap.size());
            // }
        }
    }

    public boolean handleAllProcessRequest(ActionListener<ProcessRequest> listener) {
        for (ProcessRequest processRequest : this.channelIdToNettyChannelProcessRequestMap.values()) {
            listener.actionPerformed(processRequest);
        }
        return !this.channelIdToNettyChannelProcessRequestMap.isEmpty();
    }

    public void clearAllProcessRequest() {
        this.channelIdToNettyChannelProcessRequestMap.clear();
        this.transmissionServerMonitor.setConnections(0);
    }

    public ProcessRequest getNettyChannelProcessRequest(ChannelHandlerContext ctx) {
        String channelId = ctx.channel().id().asLongText();
        ProcessRequest channelProcessRequest = this.channelIdToNettyChannelProcessRequestMap.get(channelId);
        return channelProcessRequest;
    }

    /**
     * create一個ProcessRequest物件, default採用SpringBean Prototype方式註冊
     *
     * @return
     */
    protected ProcessRequest createProcessRequest() {
        return SpringBeanFactoryUtil.registerBean(processRequestClass, Scope.Prototype);
    }

    /**
     * 獲取所有的ProcessRequest物件
     *
     * @return
     */
    protected List<ProcessRequest> getNettyChannelProcessRequests() {
        return new ArrayList<>(this.channelIdToNettyChannelProcessRequestMap.values());
    }
}