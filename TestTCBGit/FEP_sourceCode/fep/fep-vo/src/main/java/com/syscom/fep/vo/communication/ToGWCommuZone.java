package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 從FEP獲取Zone
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToGWCommuZone extends BaseXmlCommu {
    private short zoneCbsMode;
    private String zoneTbsdy;

    public short getZoneCbsMode() {
        return zoneCbsMode;
    }

    public void setZoneCbsMode(short zoneCbsMode) {
        this.zoneCbsMode = zoneCbsMode;
    }

    public String getZoneTbsdy() {
        return zoneTbsdy;
    }

    public void setZoneTbsdy(String zoneTbsdy) {
        this.zoneTbsdy = zoneTbsdy;
    }
}
