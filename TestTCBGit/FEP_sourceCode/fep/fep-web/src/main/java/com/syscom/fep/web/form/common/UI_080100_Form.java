package com.syscom.fep.web.form.common;

import com.syscom.fep.vo.monitor.ServiceMonitoring;
import com.syscom.fep.web.form.BaseForm;

import java.io.Serializable;

public class UI_080100_Form implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 系統日
     */
    private String systemDate;

    /**
     * 監控信息
     */
    private ServiceMonitoring monitor;

    public String getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(String systemDate) {
        this.systemDate = systemDate;
    }

    public ServiceMonitoring getMonitor() {
        return monitor;
    }

    public void setMonitor(ServiceMonitoring monitor) {
        this.monitor = monitor;
    }

}
