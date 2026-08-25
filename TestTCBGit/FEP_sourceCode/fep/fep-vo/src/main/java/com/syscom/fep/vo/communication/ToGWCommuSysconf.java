package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回應給GW查詢Sysconf表資料的電文
 */
@XStreamAlias("response")
public class ToGWCommuSysconf extends BaseXmlCommu {
    private String sysconfValue;

    public void setSysconfValue(String sysconfValue) {
        this.sysconfValue = sysconfValue;
    }

    public String getSysconfValue() {
        return sysconfValue;
    }
}
