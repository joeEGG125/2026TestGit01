package com.syscom.fep.web.form.common;

import com.syscom.fep.frmcommon.jms.entity.JmsInfoConcurrency;
import com.syscom.fep.web.resp.BaseResp;

public class UI_080110_FormActionResp extends BaseResp<JmsInfoConcurrency> {
    private boolean offline;

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
