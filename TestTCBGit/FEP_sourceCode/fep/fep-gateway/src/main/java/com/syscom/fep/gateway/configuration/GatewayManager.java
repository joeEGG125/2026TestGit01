package com.syscom.fep.gateway.configuration;

import com.syscom.fep.gateway.ims.IMSTransmission;
import com.syscom.fep.gateway.netty.NettyTransmission;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GatewayManager {
    private final List<NettyTransmission<?, ?, ?>> nettyTransmissionList = new ArrayList<>();
    private final List<IMSTransmission<?, ?>> imsTransmissionList = new ArrayList<>();

    public void establishAllConnection() {
        // 執行NettyTransmission
        NettyTransmission<?, ?, ?>[] nettyTransmissions = null;
        synchronized (this.nettyTransmissionList) {
            nettyTransmissions = new NettyTransmission[this.nettyTransmissionList.size()];
            this.nettyTransmissionList.toArray(nettyTransmissions);
        }
        if (ArrayUtils.isNotEmpty(nettyTransmissions)) {
            for (NettyTransmission<?, ?, ?> transmission : nettyTransmissions) {
                transmission.run();
            }
        }
        // 執行IMSTransmission
        IMSTransmission<?, ?>[] imsTransmissions = null;
        synchronized (this.imsTransmissionList) {
            imsTransmissions = new IMSTransmission[this.imsTransmissionList.size()];
            this.imsTransmissionList.toArray(imsTransmissions);
        }
        if (ArrayUtils.isNotEmpty(imsTransmissions)) {
            for (IMSTransmission<?, ?> transmission : imsTransmissions) {
                transmission.start();
            }
        }
    }

    /**
     * 加入NettyTransmission物件
     *
     * @param transmission
     */
    public void addTransmission(NettyTransmission<?, ?, ?> transmission) {
        synchronized (this.nettyTransmissionList) {
            this.nettyTransmissionList.add(transmission);
        }
    }

    /**
     * 加入IMSTransmission物件
     *
     * @param transmission
     */
    public void addTransmission(IMSTransmission<?, ?> transmission) {
        synchronized (this.imsTransmissionList) {
            this.imsTransmissionList.add(transmission);
        }
    }

    /**
     * 移除NettyTransmission物件
     *
     * @param transmission
     */
    public void removeTransmission(NettyTransmission<?, ?, ?> transmission) {
        synchronized (this.nettyTransmissionList) {
            this.nettyTransmissionList.remove(transmission);
        }
    }

    /**
     * 移除IMSTransmission物件
     *
     * @param transmission
     */
    public void removeTransmission(IMSTransmission<?, ?> transmission) {
        synchronized (this.imsTransmissionList) {
            this.imsTransmissionList.remove(transmission);
        }
    }
}
