package com.syscom.fep.frmcommon.delegate;

public interface ExecuteListener2<T, PARAM1, PARAM2> {

    T execute(PARAM1 param1, PARAM2 param2) throws Exception;

}
