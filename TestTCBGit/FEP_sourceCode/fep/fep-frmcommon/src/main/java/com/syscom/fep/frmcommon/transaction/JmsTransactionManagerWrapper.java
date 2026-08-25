package com.syscom.fep.frmcommon.transaction;

import jakarta.jms.ConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;

public class JmsTransactionManagerWrapper extends JmsTransactionManager implements AutoCloseable {
    private final TransactionWrapper wrapper;

    public JmsTransactionManagerWrapper() {
        super();
        wrapper = new TransactionWrapper(this);
    }

    public JmsTransactionManagerWrapper(ConnectionFactory connectionFactory) {
        super(connectionFactory);
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
