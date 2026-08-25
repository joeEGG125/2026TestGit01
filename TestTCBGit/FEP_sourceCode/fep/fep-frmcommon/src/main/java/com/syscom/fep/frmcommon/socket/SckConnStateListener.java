package com.syscom.fep.frmcommon.socket;

import java.util.EventListener;

public interface SckConnStateListener extends EventListener {

    /**
     * 通知連線狀態發生改變
     *
     * @param state
     * @param t
     */
    void connStateChanged(SckConnState state, Throwable t);

}
