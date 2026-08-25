package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.jms.JmsHandler;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.jms.entity.PlainTextMessage;
import com.syscom.fep.frmcommon.transaction.TransactionWrapper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.TestQueueConsumers;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.mapper.AccountMapper;
import com.syscom.fep.mybatis.model.Account;
import com.syscom.fep.mybatis.model.Feptxn;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 僅用於測試程式參考
 */
public class TestReceiver extends FEPBase implements JmsReceiver<PlainTextMessage> {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private BatchExtMapper batchExtMapper;

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(TestQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, PlainTextMessage payload, Message message) {
        LogHelperFactory.getTraceLogger().info(Const.MESSAGE_IN, payload.getPayload());
        // SendQueueToVIPServerReceiver(this.logContext, payload.getPayload(), message);
        // SendQueueToNONVIPServerReceiver(this.logContext, payload.getPayload(), message);
        // testDataSourceTransaction();
        // testDataSourceTransaction2(payload.getPayload());
        // testDataSourceTransaction3(payload.getPayload());
        testDataSourceTransaction4();
        testDataSourceTransaction5();
    }

    private boolean SendQueueToVIPServerReceiver(LogData logData, String returnStr, Message messageIn) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getVip().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                        //                        JmsFactory.setMessageId(messages, JmsFactory.getMessageId(message).getBytes());
                    } catch (Exception e) {
                        return;
                    }
                }
            });
            writLog(logData, returnStr, "SendQueueToVIPServerReceiver done.", ".SendQueueToVIPServerReceiver");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean SendQueueToNONVIPServerReceiver(LogData logData, String returnStr, Message messageIn) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getNonvip().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                        //                        JmsFactory.setMessageId(messages, JmsFactory.getMessageId(message).getBytes());
                    } catch (Exception e) {
                        return;
                    }
                }
            });
            writLog(logData, returnStr, "SendQueueToNONVIPServerReceiver done.", ".SendQueueToNONVIPServerReceiver");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void writLog(LogData logData, String msg, String mark, String progName) {
        logData.setMessage(msg);
        logData.setProgramName(StringUtils.join(ProgramName, progName));
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setRemark(mark);
        this.logMessage(logData);
    }

    private void testDataSourceTransaction() {
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務
            wrapper.beginTransaction(() -> {
                // 建立entity
                Account account = new Account(StringUtils.leftPad(Long.toString(System.currentTimeMillis()), 14, "0"), "2", "20250902");
                // insert
                accountMapper.insert(account);
                // update
                batchExtMapper.updateBatchResult(1127, "1");
                // commit by manual
                wrapper.commit();
            });
        } catch (Exception e) {
            // TODO handle Exception
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
    }

    private void testDataSourceTransaction2(String recv) {
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務, 並設置autoCommit為false
            wrapper.beginTransaction(() -> {
                // 建立entity
                Account account = new Account(StringUtils.leftPad(Long.toString(System.currentTimeMillis()), 14, "0"), "2", "20250902");
                // insert
                accountMapper.insert(account);
                // update
                batchExtMapper.updateBatchResult(1127, "1");
            }, false);

            // TODO Something

            // commit by manual by condition
            if ("1".equals(recv))
                wrapper.commit();
        } catch (Exception e) {
            // TODO handle Exception
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
    }

    private void testDataSourceTransaction3(String recv) {
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務
            wrapper.beginTransaction(() -> {
                // 建立entity
                Account account = new Account(StringUtils.leftPad(Long.toString(System.currentTimeMillis()), 14, "0"), "2", "20250902");
                // insert
                accountMapper.insert(account);
                // update
                batchExtMapper.updateBatchResult(1127, "1");

                if ("11".equals(recv))
                    // throw Exception by condition, then it will be auto rollback finally
                    throw new IllegalArgumentException("Illegal Argument");
                else
                    // commit by manual
                    wrapper.commit();
            });
        } catch (Exception e) {
            // TODO handle Exception
            LogHelperFactory.getTraceLogger().error(e, e.getMessage());
        }
    }

    private void testDataSourceTransaction4() {
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper, 注意TransactionWrapper必須寫進try(...)中
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務
            wrapper.beginTransaction(() -> {
                // 建立entity
                Account account = new Account(StringUtils.leftPad(Long.toString(System.currentTimeMillis()), 14, "0"), "2", "20250902");
                // insert
                int ret = accountMapper.insert(account);
                if (ret <= 0) {
                    // rollback by manual
                    wrapper.rollback();
                    return;
                }
                // update
                ret = batchExtMapper.updateBatchResult(1127, "1");
                if (ret <= 0) {
                    // rollback by manual
                    wrapper.rollback();
                    return;
                }
                // commit by manual
                wrapper.commit();
            });
        } catch (Exception e) {
            // TODO 處理異常, 並且call sendEMS(logData)
        }
    }

    private FEPReturnCode testDataSourceTransaction5() {
        // 先透過指定的BeanName取得DataSource Transaction Manager SpringBean Object
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        // 將上面取到的transactionManager包進TransactionWrapper, 注意TransactionWrapper必須寫進try(...)中
        try (TransactionWrapper wrapper = new TransactionWrapper(transactionManager)) {
            // 呼叫doTransaction方法開始事務
            wrapper.beginTransaction();
            // 建立entity
            Account account = new Account(StringUtils.leftPad(Long.toString(System.currentTimeMillis()), 14, "0"), "2", "20250902");
            // insert
            int ret = accountMapper.insert(account);
            if (ret <= 0) {
                // rollback by manual
                wrapper.rollback();
                return FEPReturnCode.InsertFail; // TransactionWrapper預設會自動回滾
            }
            // update
            ret = batchExtMapper.updateBatchResult(1127, "1");
            if (ret <= 0) {
                // rollback by manual
                wrapper.rollback();
                return FEPReturnCode.UpdateFail; // TransactionWrapper預設會自動回滾
            }
            // commit by manual
            wrapper.commit();
            // return
            return FEPReturnCode.Normal;
        } catch (Exception e) {
            // TODO 處理異常, 並且call sendEMS(logData)
            return FEPReturnCode.ProgramException;
        }
    }
}
