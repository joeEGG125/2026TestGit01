package com.syscom.fep.frmcommon.jms;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.connection.JmsResourceHolder;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class JmsTemplateExt extends JmsTemplate {
    private final JmsTemplateResourceFactory transactionalResourceFactory = new JmsTemplateResourceFactory();
    private final JmsProperty property;

    public JmsTemplateExt(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        this(connectionFactory, messageConverter, null);
    }

    public JmsTemplateExt(ConnectionFactory connectionFactory, MessageConverter messageConverter, JmsProperty property) {
        super(connectionFactory);
        setMessageConverter(messageConverter);
        this.property = property;
    }

    @Override
    public <T> T execute(SessionCallback<T> action, boolean startConnection) throws JmsException {
        if (this.property != null && this.property.isForceToCreateSession()) {
            Assert.notNull(action, "Callback object must not be null");
            Connection con = null;
            Session session = null;
            try {
                con = createConnection();
                session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
                if (startConnection) {
                    con.start();
                }
                // if (logger.isDebugEnabled()) {
                //     logger.debug("Executing callback on JMS Session: " + session);
                // }
                return action.doInJms(session);
            } catch (JMSException ex) {
                throw convertJmsAccessException(ex);
            } finally {
                JmsUtils.closeSession(session);
                ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory(), startConnection);
            }
        } else {
            Connection conToClose = null;
            Session sessionToClose = null;
            try {
                Session sessionToUse = ConnectionFactoryUtils.doGetTransactionalSession(obtainConnectionFactory(), this.transactionalResourceFactory, startConnection);
                if (sessionToUse == null) {
                    conToClose = createConnection();
                    sessionToClose = createSession(conToClose);
                    if (startConnection) {
                        conToClose.start();
                    }
                    sessionToUse = sessionToClose;
                }
                // if (logger.isDebugEnabled()) {
                //     logger.debug("Executing callback on JMS Session: " + sessionToUse);
                // }
                return action.doInJms(sessionToUse);
            } catch (JMSException ex) {
                throw convertJmsAccessException(ex);
            } finally {
                JmsUtils.closeSession(sessionToClose);
                ConnectionFactoryUtils.releaseConnection(conToClose, getConnectionFactory(), startConnection);
            }
        }
    }

    private class JmsTemplateResourceFactory implements ConnectionFactoryUtils.ResourceFactory {

        @Override
        @Nullable
        public Connection getConnection(JmsResourceHolder holder) {
            return JmsTemplateExt.this.getConnection(holder);
        }

        @Override
        @Nullable
        public Session getSession(JmsResourceHolder holder) {
            return JmsTemplateExt.this.getSession(holder);
        }

        @Override
        public Connection createConnection() throws JMSException {
            return JmsTemplateExt.this.createConnection();
        }

        @Override
        public Session createSession(Connection con) throws JMSException {
            return JmsTemplateExt.this.createSession(con);
        }

        @Override
        public boolean isSynchedLocalTransactionAllowed() {
            return JmsTemplateExt.this.isSessionTransacted();
        }
    }
}
