package com.syscom.fep.notify.common;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;

import java.util.Map;

public abstract class SenderBase<T> extends FEPBase {
    public abstract void send(LogData logData, Map<String, T> content) throws Exception ;
}
