package com.syscom.fep.notify.common;

import com.syscom.fep.notify.dto.request.NotifyRequestContent;

public abstract class CustomerRuleBase<T> {
    public abstract boolean process(NotifyRequestContent templateParmVars) throws Exception ;
}
