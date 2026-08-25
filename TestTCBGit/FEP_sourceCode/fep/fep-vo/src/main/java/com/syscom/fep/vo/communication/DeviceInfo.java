package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回應給GW資料庫insert/update/delete的結果
 *
 * @author Richard
 */
public class DeviceInfo {
    private String status;
    private String msg;
    private DeviceInfo resultData;

}
