package com.syscom.fep.vo.communication;

import com.syscom.fep.base.enums.Protocol;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.Getter;
import lombok.Setter;

/**
 * 發送給FISCGW的請求電文
 *
 * @author Richard
 */
@XStreamAlias("response")
@Getter
@Setter
public class ToSendQueryAccountCommu extends BaseXmlCommu {
    private String idNo;
    private String mp;
    private String bankCode;
    private int timeout;
    private int step;
    @XStreamOmitField
    private Protocol protocol;
    @XStreamOmitField
    private String restfulUrl;

    public ToSendQueryAccountCommu() {
        this.idNo = idNo;
        this.mp = mp;
        this.bankCode = bankCode;
    }

    public ToSendQueryAccountCommu(String message, String stan, int ej, int timeout, String messageId) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("idNo=").append(this.idNo).append("&");
        sb.append("mobilePhone=").append(this.mp).append("&");
        sb.append("bankCode=").append(this.bankCode).append("&");
        sb.append("timeout=").append(this.timeout).append("&");
        return sb.toString();
    }
}
