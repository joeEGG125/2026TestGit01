package com.syscom.fep.web.form.common;

import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.web.configurer.WebSimpleNettyConfiguration.WebSimpleNettyServer;
import com.syscom.fep.web.form.BaseForm;

public class UI_080120_Form extends BaseForm {
    private WebSimpleNettyServer server;
    private NettyEventExecutorData eventExecutorData;
    private boolean offline;

    public WebSimpleNettyServer getServer() {
        return server;
    }

    public void setServer(WebSimpleNettyServer server) {
        this.server = server;
    }

    public NettyEventExecutorData getEventExecutorData() {
        return eventExecutorData;
    }

    public void setEventExecutorData(NettyEventExecutorData eventExecutorData) {
        this.eventExecutorData = eventExecutorData;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
