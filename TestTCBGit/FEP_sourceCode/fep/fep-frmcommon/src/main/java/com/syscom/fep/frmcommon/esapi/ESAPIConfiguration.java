package com.syscom.fep.frmcommon.esapi;

import org.owasp.esapi.reference.DefaultSecurityConfiguration;

public class ESAPIConfiguration {

    static {
        System.setProperty(DefaultSecurityConfiguration.DISCARD_LOGSPECIAL, Boolean.TRUE.toString());
    }

    private ESAPIConfiguration() {}

    public static void init() {}

}
