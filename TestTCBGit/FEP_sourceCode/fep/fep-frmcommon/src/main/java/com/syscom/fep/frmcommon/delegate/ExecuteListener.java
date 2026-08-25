package com.syscom.fep.frmcommon.delegate;

public interface ExecuteListener<T> {

    T execute(Object ...args) throws Exception;

}
