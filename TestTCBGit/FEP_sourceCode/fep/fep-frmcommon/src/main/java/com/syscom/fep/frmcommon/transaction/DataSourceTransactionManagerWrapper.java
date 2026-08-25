package com.syscom.fep.frmcommon.transaction;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

public class DataSourceTransactionManagerWrapper extends DataSourceTransactionManager implements AutoCloseable {
    private final TransactionWrapper wrapper;

    public DataSourceTransactionManagerWrapper() {
        super();
        wrapper = new TransactionWrapper(this);
    }

    public DataSourceTransactionManagerWrapper(DataSource dataSource) {
        super(dataSource);
        wrapper = new TransactionWrapper(this);
    }

    public TransactionWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public void close() throws Exception {
        wrapper.close();
    }
}
