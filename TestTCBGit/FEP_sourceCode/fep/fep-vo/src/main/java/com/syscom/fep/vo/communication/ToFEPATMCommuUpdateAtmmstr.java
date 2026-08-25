package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 接收來自ATMGW請求更新ATM IP和Port
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommuUpdateAtmmstr extends BaseXmlCommu {
    private String atmAtmno;
    private String atmAtmpIp;
    private String atmAtmpPort;
    private String atmCertalias;

    public String getAtmAtmno() {
        return atmAtmno;
    }

    public void setAtmAtmno(String atmAtmno) {
        this.atmAtmno = atmAtmno;
    }

    public String getAtmAtmpIp() {
        return atmAtmpIp;
    }

    public void setAtmAtmpIp(String atmAtmpIp) {
        this.atmAtmpIp = atmAtmpIp;
    }

    public String getAtmAtmpPort() {
        return atmAtmpPort;
    }

    public void setAtmAtmpPort(String atmAtmpPort) {
        this.atmAtmpPort = atmAtmpPort;
    }

    public String getAtmCertalias() {
        return atmCertalias;
    }

    public void setAtmCertalias(String atmCertalias) {
        this.atmCertalias = atmCertalias;
    }
}
