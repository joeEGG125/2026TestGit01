package com.syscom.fep.vo.communication;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 沒有請求的參數, 只是單純從FEP獲取資料
 *
 * @author Richard
 */
@XStreamAlias("request")
public class ToFEPCommuAction extends BaseXmlCommu {
    private ActionType action;

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public static enum ActionType {
        /**
         * 取EJFNO
         */
        GENERATE_EJFNO;
    }
}
