package com.syscom.fep.invoker.netty;

import com.syscom.fep.frmcommon.netty.NettyEventExecutorDataHandler;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.GenericTypeUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.apache.commons.lang3.ArrayUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public abstract class SimpleNettyBase<Configuration extends SimpleNettyConfiguration, MessageIn, MessageOut> extends SimpleNettyBaseMethod implements SimpleNettyConnStateListener, NettyEventExecutorDataHandler {
    protected Class<Configuration> configurationCls = GenericTypeUtil.getGenericSuperClass(this.getClass(), 0);
    protected Configuration configuration;
    protected SimpleNettyNotification notification = new SimpleNettyNotification();

    @PostConstruct
    public void initSimpleNetty() {
        this.putMDC(null);
        this.run();
    }

    /**
     * 非SpringBoot下初始化
     *
     * @param configuration
     */
    public void initSimpleNetty(Configuration configuration) {
        this.putMDC(null);
        this.configuration = configuration;
        this.initialization();
    }

    /**
     * run程式
     */
    public void run() {
        this.run(true);
    }

    /**
     * run程式
     *
     * @param establishConnection
     */
    public void run(boolean establishConnection) {
        this.run(SpringBeanFactoryUtil.registerBean(configurationCls), establishConnection);
    }

    /**
     * run程式並指定Configuration
     *
     * @param configuration
     */
    public void run(Configuration configuration) {
        this.run(configuration, true);
    }

    /**
     * run程式並指定Configuration
     *
     * @param configuration
     * @param establishConnection
     */
    public void run(Configuration configuration, boolean establishConnection) {
        this.putMDC(null);
        this.configuration = configuration;
        // this.infoMessage(configuration.toString());
        this.initialization();
        if (establishConnection)
            this.establishConnection();
    }

    /**
     * 初始化動作
     */
    protected abstract void initialization();

    /**
     * 建立連線
     */
    protected abstract void establishConnection();

    /**
     * 斷開連線
     */
    protected abstract void closeConnection();

    /**
     * 終止
     */
    @PreDestroy
    protected abstract void terminateConnection();

    /**
     * 接收遠端的訊息進來
     *
     * @param bytes
     * @return
     */
    protected abstract MessageIn bytesToMessageIn(byte[] bytes);

    /**
     * 回應訊息給遠端
     *
     * @param messageOut
     * @return
     */
    protected abstract byte[] messageOutToBytes(MessageOut messageOut);

    /**
     * 定義SslHandler用於憑證連線
     *
     * @param channel
     * @param configuration
     * @param useClientMode
     * @return
     * @throws Exception
     */
    protected SslHandler getSslHandler(Channel channel, Configuration configuration, boolean useClientMode) throws Exception {
        this.putMDC(channel);
        SSLEngine sslEngine = SslContextFactory.getSSLEngine(configuration, configuration.isSslNeedClientAuth(), configuration.isSslWantClientAuth(), useClientMode);
        SslHandler sslHandler = null;
        if (sslEngine != null) {
            sslHandler = new SslHandler(sslEngine);
            sslHandler.setHandshakeTimeoutMillis(configuration.getHandshakeTimeoutMillis());
        }
        return sslHandler;
    }

    /**
     * 發生SSL Handshake時列印憑證訊息
     *
     * @param ctx
     * @param sslHandler
     * @param evt
     */
    protected void sslHandshakeCompletionEventTriggered(ChannelHandlerContext ctx, SslHandler sslHandler, SslHandshakeCompletionEvent evt) {
        this.putMDC(ctx.channel());
        if (!evt.isSuccess()) {
            return;
        }
        try {
            SSLSession session = sslHandler.engine().getSession();
            Certificate[] certificates = this.configuration.isSslNeedClientAuth() ? session.getPeerCertificates() : session.getLocalCertificates();
            if (ArrayUtils.isEmpty(certificates)) {
                return;
            }
            // 2024-10-17 Richard modified for【SSL Verification Bypass】
            // X509Certificate cert = (X509Certificate) certificates[0];
            // 獲得憑證版本
            String info = String.valueOf(((X509Certificate) certificates[0]).getVersion());
            infoMessage(ctx.channel(), "憑證版本:", info);
            // 獲得憑證序列號
            info = ((X509Certificate) certificates[0]).getSerialNumber().toString(16);
            infoMessage(ctx.channel(), "憑證序列號:", info);
            // 獲得憑證有效期
            Date beforedate = ((X509Certificate) certificates[0]).getNotBefore();
            info = FormatUtil.dateFormat(beforedate);
            infoMessage(ctx.channel(), "憑證生效日期:", info);
            Date afterdate = ((X509Certificate) certificates[0]).getNotAfter();
            info = FormatUtil.dateFormat(afterdate);
            infoMessage(ctx.channel(), "憑證失效日期:", info);
            // 獲得憑證主體信息
            info = ((X509Certificate) certificates[0]).getSubjectDN().getName();
            infoMessage(ctx.channel(), "憑證擁有者:", info);
            // 獲得憑證頒發者信息
            info = ((X509Certificate) certificates[0]).getIssuerDN().getName();
            infoMessage(ctx.channel(), "憑證頒發者:", info);
            // 獲得憑證籤名算法名稱
            info = ((X509Certificate) certificates[0]).getSigAlgName();
            infoMessage(ctx.channel(), "憑證籤名算法:", info);
        } catch (SSLPeerUnverifiedException e) {
            warnMessage(ctx.channel(), e, e.getMessage());
        }
    }

    /**
     * 由子類去實作電文解碼器, 用來處理一些特殊的電文
     *
     * @return
     */
    protected ByteToMessageDecoder getByteToMessageDecoder() {
        return null;
    }

    /**
     * 有新的Channel時, 初始化動作
     *
     * @param ch
     */
    protected void channelInitialization(SocketChannel ch) {}
}
