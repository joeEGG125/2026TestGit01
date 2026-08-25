package com.syscom.fep.gateway.entity;

import org.apache.commons.lang.StringUtils;

public enum Gateway {
    ATMGW, FISCGW, POSGW, CBSGW,
    SAMPLEGW;

    public String getNamePrefix() {
        String name = name();
        return StringUtils.replace(name, "GW", StringUtils.EMPTY).toLowerCase();
    }
}
