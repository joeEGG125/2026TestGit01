package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("response")
public class ToGWCommuAction extends BaseXmlCommu {
    /**
     * 取ejfno
     */
    private int ejfno;

    public int getEjfno() {
        return ejfno;
    }

    public void setEjfno(int ejfno) {
        this.ejfno = ejfno;
    }
}
