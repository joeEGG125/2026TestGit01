package com.syscom.fep.base.exception;

import com.syscom.fep.base.enums.FEPReturnCode;
import org.apache.commons.lang3.StringUtils;

public class FEPBaseException extends Exception {
    private FEPReturnCode rtnCode;

    public FEPBaseException() {}

    public FEPBaseException(Object... message) {
        super(StringUtils.join(message));
    }

    public FEPBaseException(Throwable cause, Object... message) {
        super(StringUtils.join(message), cause);
    }

    public FEPBaseException(Throwable cause) {
        super(cause);
    }

    public FEPBaseException(Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object... message) {
        super(StringUtils.join(message), cause, enableSuppression, writableStackTrace);
    }

    public FEPBaseException(FEPReturnCode rtnCode, Object... message) {
        super(StringUtils.join(message));
        this.rtnCode = rtnCode;
    }

    public FEPBaseException(FEPReturnCode rtnCode, Throwable cause, Object... message) {
        super(StringUtils.join(message), cause);
        this.rtnCode = rtnCode;
    }

    public FEPReturnCode getRtnCode() {
        return rtnCode;
    }

    public void setRtnCode(FEPReturnCode rtnCode) {
        this.rtnCode = rtnCode;
    }
}
