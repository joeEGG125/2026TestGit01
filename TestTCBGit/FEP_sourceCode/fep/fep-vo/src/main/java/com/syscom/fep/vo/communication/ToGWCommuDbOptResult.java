package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回應給GW資料庫insert/update/delete的結果
 *
 * @author Richard
 */
@XStreamAlias("response")
public class ToGWCommuDbOptResult extends BaseXmlCommu {
    private int result;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
