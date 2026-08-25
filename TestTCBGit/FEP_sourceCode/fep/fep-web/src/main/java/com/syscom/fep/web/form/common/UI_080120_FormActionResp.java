package com.syscom.fep.web.form.common;

import com.syscom.fep.frmcommon.netty.NettyEventExecutorData;
import com.syscom.fep.web.resp.BaseResp;

public class UI_080120_FormActionResp extends BaseResp<NettyEventExecutorData> {
    private boolean offline;

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
