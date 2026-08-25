package com.syscom.fep.gateway.ims;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IMSTransmissionNotification {
    private static final LogHelper logger = LogHelperFactory.getTraceLogger();
    private final Map<String, List<IMSTransmissionConnStateListener>> connStateListenerMap = new ConcurrentHashMap<>();

    public void addConnStateListener(IMSTransmissionConfiguration configuration, IMSTransmissionConnStateListener listener) {
        this.addConnStateListener(configuration, listener, -1);
    }

    public void addConnStateListener(IMSTransmissionConfiguration configuration, IMSTransmissionConnStateListener listener, int index) {
        String connStateListenerKey = this.getConnStateListenerKey(configuration);
        List<IMSTransmissionConnStateListener> list = connStateListenerMap.get(connStateListenerKey);
        if (list == null) {
            list = new ArrayList<IMSTransmissionConnStateListener>();
        }
        if (list.contains(listener)) return;
        if (index == -1) {
            list.add(listener);
            index = list.size() - 1;
        } else {
            list.add(index, listener);
        }
        connStateListenerMap.put(connStateListenerKey, list);
        logger.info("[addConnStateListener]connStateListenerKey = [", connStateListenerKey, "], listener = [", listener, "], index = [", index, "], list.size = [", list.size(), "]");
    }

    public void removeConnStateListener(IMSTransmissionConfiguration configuration, IMSTransmissionConnStateListener listener) {
        String connStateListenerKey = this.getConnStateListenerKey(configuration);
        List<IMSTransmissionConnStateListener> list = connStateListenerMap.get(connStateListenerKey);
        if (CollectionUtils.isEmpty(list)) return;
        if (!list.contains(listener)) return;
        list.remove(listener);
        // 2024-06-20 Richard add start for 【Memory Leak】
        if (list.isEmpty())
            connStateListenerMap.remove(connStateListenerKey);
        // 2024-06-20 Richard add end for 【Memory Leak】
        logger.info("[removeConnStateListener]connStateListenerKey = [", connStateListenerKey, "], listener = [", listener, "], list.size = [", list.size(), "]");
    }

    public void notifyConnStateChanged(IMSTransmissionConfiguration configuration, IMSTransmissionConnState state, Throwable... t) {
        if (connStateListenerMap.isEmpty())
            return;
        String connStateListenerKey = this.getConnStateListenerKey(configuration);
        List<IMSTransmissionConnStateListener> list = connStateListenerMap.get(connStateListenerKey);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        IMSTransmissionConnStateListener[] listeners = new IMSTransmissionConnStateListener[list.size()];
        list.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            logger.info("[notifyConnStateChanged]connStateListenerKey = [", connStateListenerKey, "], state = [", state, "], listeners.size = [", listeners.length, "]");
            if (ArrayUtils.isEmpty(t) || t[0] == null) {
                for (IMSTransmissionConnStateListener listener : listeners) {
                    listener.connStateChanged(configuration, state, null);
                }
            } else {
                for (IMSTransmissionConnStateListener listener : listeners) {
                    listener.connStateChanged(configuration, state, t[0]);
                }
            }
        }
    }

    private String getConnStateListenerKey(IMSTransmissionConfiguration configuration) {
        return configuration.getClientId();
    }
}
