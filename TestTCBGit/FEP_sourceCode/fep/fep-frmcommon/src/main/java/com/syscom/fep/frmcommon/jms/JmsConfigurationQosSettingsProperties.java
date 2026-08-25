package com.syscom.fep.frmcommon.jms;

import org.springframework.jms.support.QosSettings;

public class JmsConfigurationQosSettingsProperties extends QosSettings {
    private boolean enable = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
