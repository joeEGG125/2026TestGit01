package com.syscom.fep.gateway.netty;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import io.netty.channel.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NettyTransmissionNotification {
    private static final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final Map<String, List<NettyTransmissionConnStateListener>> connStateListenerMap = new ConcurrentHashMap<>();

    public void addConnStateListener(String nettyTransmissionNotificationKey, NettyTransmissionConnStateListener listener) {
        List<NettyTransmissionConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (list == null) {
            list = new ArrayList<NettyTransmissionConnStateListener>();
        }
        if (list.contains(listener)) return;
        list.add(listener);
        connStateListenerMap.put(nettyTransmissionNotificationKey, list);
        logger.info("[addConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], connStateListenerMap.size = [", connStateListenerMap.size(), "], list.size = [", list.size(), "]");
    }

    public void addConnStateListener(int index, String nettyTransmissionNotificationKey, NettyTransmissionConnStateListener listener) {
        List<NettyTransmissionConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (list == null) {
            list = new ArrayList<NettyTransmissionConnStateListener>();
        }
        if (list.contains(listener)) return;
        list.add(index, listener);
        connStateListenerMap.put(nettyTransmissionNotificationKey, list);
        logger.info("[addConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], connStateListenerMap.size = [", connStateListenerMap.size(), "], list.size = [", list.size(), "]");
    }

    public void removeConnStateListener(String nettyTransmissionNotificationKey, NettyTransmissionConnStateListener listener) {
        List<NettyTransmissionConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (CollectionUtils.isEmpty(list)) return;
        if (!list.contains(listener)) return;
        list.remove(listener);
        // 2024-06-17 Richard add start for 【Memory Leak】
        if (list.isEmpty())
            connStateListenerMap.remove(nettyTransmissionNotificationKey);
        // 2024-06-17 Richard add end for 【Memory Leak】
        logger.info("[removeConnStateListener]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], listener = [", listener, "], connStateListenerMap.size = [", connStateListenerMap.size(), "], list.size = [", list.size(), "]");
    }

    public void notifyConnStateChanged(String nettyTransmissionNotificationKey, Channel channel, NettyTransmissionConnState state, Throwable... t) {
        if (connStateListenerMap.isEmpty())
            return;
        List<NettyTransmissionConnStateListener> list = connStateListenerMap.get(nettyTransmissionNotificationKey);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        NettyTransmissionConnStateListener[] listeners = new NettyTransmissionConnStateListener[list.size()];
        list.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            logger.info("[notifyConnStateChanged]nettyTransmissionNotificationKey = [", nettyTransmissionNotificationKey, "], state = [", state, "], listeners.size = [", listeners.length, "]");
            if (ArrayUtils.isEmpty(t) || t[0] == null) {
                for (NettyTransmissionConnStateListener nettyTransmissionConnStateListener : listeners) {
                    nettyTransmissionConnStateListener.connStateChanged(channel, state, null);
                }
            } else {
                for (NettyTransmissionConnStateListener nettyTransmissionConnStateListener : listeners) {
                    nettyTransmissionConnStateListener.connStateChanged(channel, state, t[0]);
                }
            }
        }
    }
}
