package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 從FEP獲取Zone
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPCommuZone extends BaseXmlCommu {
    private String zoneCode;

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }
}
