package com.syscom.fep.frmcommon.jms.vendor;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.CCSID;
import com.syscom.fep.frmcommon.jms.JmsConfigurationProperties;
import com.syscom.fep.frmcommon.jms.JmsConstant;
import com.syscom.fep.frmcommon.jms.JmsFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;

public class MQFactory implements JmsConstant {
    private static final LogHelper logger = new LogHelper();

    public static String receiveQueue(JmsConfigurationProperties properties, String queueName, String correlationId, int waitInterval) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options = gmo.options + MQConstants.MQGMO_SYNCPOINT;
        gmo.options = gmo.options + MQConstants.MQGMO_FAIL_IF_QUIESCING;
        gmo.options = gmo.options + MQConstants.MQGMO_WAIT;
        gmo.waitInterval = waitInterval;
        // MQQueueManager
        MQQueueManager queueManager = null;
        MQQueue getQueue = null;
        long currentTimeMillis = -1L;
        try {
            queueManager = JmsFactory.createMQQueueManager(properties.getHostNameFromConnName(), properties.getPortFromConnName(), properties.getQueueManager(), properties.getChannel(), properties.getUser(), properties.getPassword());
            getQueue = queueManager.accessQueue(queueName, CMQC.MQOO_INPUT_AS_Q_DEF);
            MQMessage mqMessage = new MQMessage();
            mqMessage.correlationId = correlationId.getBytes();
            currentTimeMillis = System.currentTimeMillis();
            getQueue.get(mqMessage, gmo);
            int length = mqMessage.getMessageLength();
            byte[] bytes = new byte[length];
            mqMessage.readFully(bytes);
            String payload = null;
            try {
                payload = new String(bytes, CCSID.getCodepage(mqMessage.characterSet));
            } catch (UnsupportedEncodingException e) {
                payload = new String(bytes);
            }
            logger.info(MESSAGE_IN, "queueName = [", queueName, "], payload = [", payload, "]");
            return payload;
        } catch (MQException e) {
            if (e.getCompCode() == MQConstants.MQCC_FAILED && e.getReason() == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                long expiredTime = System.currentTimeMillis() - currentTimeMillis;
                if (expiredTime > waitInterval) {
                    throw ExceptionUtil.createSocketTimeoutException(StringUtils.join("receive from Queue timeout, queueName:", queueName, ", correlationId:", correlationId, ", waitInterval:", waitInterval, ",expiredTime:", expiredTime));
                }
            }
            throw e;
        } finally {
            try {
                if (getQueue != null) {
                    getQueue.close();
                }
            } catch (MQException e) {
                logger.warn(e, e.getMessage());
            }
            try {
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            } catch (MQException e) {
                logger.warn(e, e.getMessage());
            }
        }
    }
}
