package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * 收到ATMGW查詢Atmstat List請求電文
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPATMCommuAtmstatList extends BaseXmlCommu {
    @XStreamAlias("atmstatAtmnos")
    private List<String> atmstatAtmnoList;
    /**
     * 查詢by status
     */
    private int atmstatStatus = -1;
    /**
     * 只查詢筆數
     */
    private boolean onlyFetchCount;
    /**
     * ATMGW IP
     */
    private String atmAtmpIp;

    public List<String> getAtmstatAtmnoList() {
        return atmstatAtmnoList;
    }

    public void setAtmstatAtmnoList(List<String> atmstatAtmnoList) {
        this.atmstatAtmnoList = atmstatAtmnoList;
    }

    public int getAtmstatStatus() {
        return atmstatStatus;
    }

    public void setAtmstatStatus(int atmstatStatus) {
        this.atmstatStatus = atmstatStatus;
    }

    /**
     * 預設電文按照壓縮處理
     *
     * @return
     */
    @Override
    public final boolean isCompressed() {
        return !onlyFetchCount;
    }

    public boolean isOnlyFetchCount() {
        return onlyFetchCount;
    }

    public void setOnlyFetchCount(boolean onlyFetchCount) {
        this.onlyFetchCount = onlyFetchCount;
    }

    public String getAtmAtmpIp() {
        return atmAtmpIp;
    }

    public void setAtmAtmpIp(String atmAtmpIp) {
        this.atmAtmpIp = atmAtmpIp;
    }
}
