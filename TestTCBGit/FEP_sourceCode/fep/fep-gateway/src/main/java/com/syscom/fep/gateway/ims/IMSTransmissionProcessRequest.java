package com.syscom.fep.gateway.ims;

import com.ibm.ims.connect.Connection;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 處理IMS業務元件
 *
 * @param <Configuration>
 */
public abstract class IMSTransmissionProcessRequest<Configuration extends IMSTransmissionConfiguration> extends FEPBase implements IMSTransmissionConnStateListener<Configuration> {
    @Autowired
    protected IMSTransmissionNotification notification;
    @Autowired
    protected Configuration configuration;
    /**
     * 記錄當前的連線狀態
     */
    protected final AtomicReference<IMSTransmissionConnState> currentConnState = new AtomicReference<>(IMSTransmissionConnState.CLIENT_DISCONNECTED);
    /**
     * IMS Connection物件, 在IMSTransmission建立連線成功後塞入
     */
    protected Connection connection;

    protected void putMDC() {
        LogMDC.put(Const.MDC_PROFILE, configuration.getGateway().name());
    }

    @PostConstruct
    public void postConstruct() {
        this.putMDC();
        this.notification = SpringBeanFactoryUtil.getBean(IMSTransmissionNotification.class);
        this.notification.addConnStateListener(this.configuration, this);
    }

    @PreDestroy
    public void preDestroy() {
        this.putMDC();
        destroy();
        this.notification.removeConnStateListener(this.configuration, this);
    }

    /**
     * 專門用於非SpringBean模式下初始化
     *
     * @param configuration
     */
    public void initialization(Configuration configuration) {
        this.configuration = configuration;
        this.configuration.initialization();
        postConstruct();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 處理業務邏輯
     *
     * @param logData
     * @param connection
     * @throws Exception
     */
    public abstract void doProcess(LogData logData, Connection connection) throws Exception;

    /**
     * 銷毀
     */
    protected abstract void destroy();

    /**
     * 接收連線狀態發生變化
     *
     * @param configuration
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(Configuration configuration, IMSTransmissionConnState state, Throwable t) {
        this.putMDC();
        currentConnState.set(state);
    }

    /**
     * 取得當前連線狀態
     *
     * @return
     */
    public IMSTransmissionConnState getCurrentConnState() {
        return currentConnState.get();
    }

    /**
     * 傳入Connection物件
     *
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * 是否連線中
     *
     * @return
     */
    public boolean isConnected() {
        return this.connection != null && this.connection.isConnected();
    }
}
