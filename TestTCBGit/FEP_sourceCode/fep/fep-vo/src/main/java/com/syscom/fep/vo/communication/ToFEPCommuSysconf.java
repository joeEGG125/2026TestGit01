package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接收來自GW請求查詢Sysconf表資料的電文
 */
@XStreamAlias("request")
public class ToFEPCommuSysconf extends BaseXmlCommu {
    private Short sysconfSubsysno;
    private String sysconfName;

    public Short getSysconfSubsysno() {
        return sysconfSubsysno;
    }

    public void setSysconfSubsysno(Short sysconfSubsysno) {
        this.sysconfSubsysno = sysconfSubsysno;
    }

    public String getSysconfName() {
        return sysconfName;
    }

    public void setSysconfName(String sysconfName) {
        this.sysconfName = sysconfName;
    }
}
