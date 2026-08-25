package com.syscom.fep.notify.common.rule;

import com.syscom.fep.notify.common.CustomerRuleBase;
import com.syscom.fep.notify.dto.request.NotifyRequestContent;
import org.springframework.stereotype.Component;

@Component
public class CustomerRule001<T> extends CustomerRuleBase<T> {
    public  boolean process(NotifyRequestContent templateParmVars) throws Exception{

        return true;
    }
}
