package com.syscom.fep.gateway.ims;

import com.ibm.ims.connect.Connection;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.frmcommon.util.SocketUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * IMS連線狀態的偵測器
 * 但是目前尚不清楚如何使用IMS Connection API來診測連線狀態, 所以取出socket物件這樣診測會引發問題, 所以暫時不要用
 *
 * @param <Configuration>
 */
public class IMSTransmissionConnectionDetector<Configuration extends IMSTransmissionConfiguration> extends AbstractScheduledTask {
    private final Configuration configuration;
    private Socket socket;

    public IMSTransmissionConnectionDetector(Configuration configuration) {
        super(StringUtils.join("ConnectionDetector-", configuration.getName()));
        this.configuration = configuration;
    }

    public void startDetect(Connection connection) {
        this.cancel();
        this.socket = null;
        if (connection != null) {
            this.socket = ReflectUtil.getFieldValue(connection, "socket", null);
        }
        if (socket != null) {
            this.scheduleAtFixedRate(0L, this.configuration.getConnectionDetectInterval(), TimeUnit.MILLISECONDS);
        }
    }

    public void stopDetect() {
        this.cancel();
        this.socket = null;
    }

    @Override
    public void execute() {
        // TODO 這裡這樣診測有問題
        boolean isConnected = SocketUtil.isAvailable(this.socket);
        if (isConnected) {
            try {
                this.socket.sendUrgentData(0);
            } catch (IOException e) {
                isConnected = false;
            }
        }
        IMSTransmissionNotification notification = SpringBeanFactoryUtil.getBean(IMSTransmissionNotification.class, false);
        if (notification != null) {
            notification.notifyConnStateChanged(this.configuration, isConnected ? IMSTransmissionConnState.CLIENT_CONNECTED : IMSTransmissionConnState.CLIENT_DISCONNECTED);
        }
    }
}
