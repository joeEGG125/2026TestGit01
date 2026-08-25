package com.syscom.fep.jms.instance.ems.sender;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.roundrobin.RoundRobin;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.ems.EMSLogMessageType;
import com.syscom.fep.jms.instance.ems.EmsQueueConfigurationProperties;
import org.apache.commons.collections.CollectionUtils;

public class EmsQueueSender implements EmsQueueSenderConstant {
    private static final LogHelper logger = LogHelperFactory.getJmsLogger();
    private static EmsQueueSenderConfiguration emsQueueSenderConfiguration;
    private static RoundRobin<EmsQueueSenderOperator> emsQueueSenderLogOperatorList;
    private static RoundRobin<EmsQueueSenderOperator> emsQueueSenderAlertOperatorList;

    static {
        EmsQueueSenderConfiguration configuration = SpringBeanFactoryUtil.getBean(EmsQueueSenderConfiguration.class, false);
        if (configuration != null) {
            emsQueueSenderConfiguration = new EmsQueueSenderConfiguration();
            emsQueueSenderConfiguration.setLog(configuration.getLog());
            emsQueueSenderConfiguration.setAlert(configuration.getAlert());
            emsQueueSenderConfiguration.setDeliveryMode(configuration.getDeliveryMode());
            // log
            try {
                emsQueueSenderLogOperatorList = emsQueueSenderConfiguration.emsQueueSenderLogOperatorList();
            } catch (Exception e) {
                logger.exceptionMsg(e, "[EmsQueueSender]init emsQueueSenderLogOperatorList failed, ", e.getMessage());
            }
            // alert
            try {
                emsQueueSenderAlertOperatorList = emsQueueSenderConfiguration.emsQueueSenderAlertOperatorList();
            } catch (Exception e) {
                logger.exceptionMsg(e, "[EmsQueueSender]init emsQueueSenderAlertOperatorList failed, ", e.getMessage());
            }
        }
    }

    private EmsQueueSender() {}

    public static void sendEMS(String message, EMSLogMessageType messageType) throws Exception {
        RoundRobin<EmsQueueSenderOperator> emsQueueSenderOperatorList = null;
        if (messageType == EMSLogMessageType.log) {
            emsQueueSenderOperatorList = emsQueueSenderLogOperatorList;
        } else if (messageType == EMSLogMessageType.alert) {
            emsQueueSenderOperatorList = emsQueueSenderAlertOperatorList;
        } else {
            throw ExceptionUtil.createUnsupportedOperationException("Cannot send EMS message, EMSLogMessageType: ", messageType);
        }
        // 如果emsQueueSenderOperatorList還是空的, 算了, 就不送了
        if (emsQueueSenderOperatorList == null) {
            logger.warn("Cannot send EMS message, cause empty emsQueueSenderOperatorList, EMSLogMessageType:", messageType);
            return;
        }
        // 靜態模式下, 優先送第一台, 如果送第一台失敗, 則再送第二台, 如果兩台都送失敗則丟異常
        if (emsQueueSenderConfiguration.getDeliveryMode() == EmsQueueSenderConfiguration.DeliveryMode.Static) {
            int errorCount = 0;
            for (int i = 0; i < emsQueueSenderOperatorList.size(); i++) {
                EmsQueueSenderOperator operator = emsQueueSenderOperatorList.get(i);
                if (sendEMS(messageType, operator, message)) {
                    // 跳出for
                    break;
                } else {
                    // 異常次數累加
                    errorCount++;
                }
            }
            // 如果有異常, 則丟出去
            if (errorCount == emsQueueSenderOperatorList.size())
                throw ExceptionUtil.createException("[EmsQueueSender]send EMS Queue failed!!! EMSLogMessageType: ", messageType);
        } else {
            // RoundRobin方式丟EMS Queue
            EmsQueueSenderOperator operator = emsQueueSenderOperatorList.select();
            sendEMS(messageType, operator, message);
        }
    }

    private static boolean sendEMS(EMSLogMessageType messageType, EmsQueueSenderOperator operator, String payload) {
        String destination = operator.getEmsQueueName();
        try {
            operator.sendQueue(destination, payload, null, null);
            return true;
        } catch (Exception e) {
            EmsQueueConfigurationProperties properties = operator.getProperties();
            logger.error("[EmsQueueSender]send EMS Queue failed!!!",
                    " EMSLogMessageType: ", messageType,
                    ", QueueManager:", properties.getQueueManager(),
                    ", Channel:", properties.getChannel(),
                    ", ConnName:", properties.getConnName(),
                    ", User:", properties.getUser(),
                    ", Destination:", destination,
                    ", Payload:", payload);
            return false;
        }
    }

    public static void destroy() {
        if (emsQueueSenderConfiguration != null)
            emsQueueSenderConfiguration.destroy();
    }
}
