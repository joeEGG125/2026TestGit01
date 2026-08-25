package com.syscom.fep.notify.common.push;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.notify.common.SenderBase;
import com.syscom.fep.notify.common.config.Push001Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.syscom.fep.notify.cnst.NotifyConstant.*;

@Component
public class Push001<T> extends SenderBase<T> {
    @Autowired
    private Push001Config push001Config;
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Override
    //直接發MQ
    public void send(LogData logData, Map<String, T> content) {
        MQQueueManager queueManager = null;
        MQQueue putQueue = null;
        try {
            String hostname = push001Config.getHostname();
            String port = push001Config.getPort();
            String channel = push001Config.getChannel();
            String managerId = push001Config.getManagerId();
            String accessQueueId = push001Config.getAccessQueueId();
            String toMqUserID = push001Config.getToMqUserID();
            String characterSet = "1208";
            String appnameProperty = "PushNotify";
            //#Receiver, Title, Content, ViaAccount, PresentType
            //K220**6263,"您有一筆新的台幣轉帳交易!","您已於2020/02/14 14:22<br/>有扣款NT$ 1,000元 建議<br/>您到交易<br/>明細查<br/>詢",TCB_ACCOUNT_OTP,0
//            String mqContent = "#Receiver, Title, Content, ViaAccount, PresentType\n" + content.get(NOTIFY_PERSONID_PARM_NAME)
//                    + ",\"" + content.get(NOTIFY_MESSAGE_CONTENT_SUBJECT) + "\""
//                    + ",\"" + content.get(NOTIFY_MESSAGE_CONTENT_BODY) + "\""
//                    + "," + content.get(NOTIFY_MESSAGE_ACCOUNT)
//                    + "," + "0";
            String mqContent = content.get(NOTIFY_PERSONID_PARM_NAME) + ","
                    + content.get(NOTIFY_MESSAGE_CONTENT_SUBJECT) + ","
                    + "\"" + content.get(NOTIFY_MESSAGE_CONTENT_BODY) + "\"" + ","
                    + content.get(NOTIFY_MESSAGE_ACCOUNT) + ","
                    + "0";
            // logger.info("mqContent for Push is: " + mqContent);
            logData.setRemark("mqContent for Push is: " + mqContent);
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
            MQEnvironment.hostname = hostname;
            MQEnvironment.port = Integer.valueOf(port);
            MQEnvironment.channel = channel;
            MQEnvironment.userID = toMqUserID;
            queueManager = new MQQueueManager(managerId);
            MQEnvironment.properties.put(MQConstants.APPNAME_PROPERTY, appnameProperty);
            putQueue = queueManager.accessQueue(accessQueueId, CMQC.MQOO_OUTPUT);
            MQMessage myMessage = new MQMessage();
            myMessage.characterSet = Integer.valueOf(characterSet);
            myMessage.writeString(mqContent);
            myMessage.format = MQC.MQFMT_STRING;
            myMessage.setStringProperty("JMSType", "TextMessage");

            MQPutMessageOptions pmo = new MQPutMessageOptions();
            putQueue.put(myMessage, pmo);
            // logger.info("Push001--Finished Push to Queue!!");
            logData.setRemark("Push001--Finished Push to Queue");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logMessage(logData);
        } catch (Exception e) {
            // logger.error("Push001--Error!! ,  " + e);
            logData.setRemark("Push001--Push to Queue Error");
            logData.setProgramName(StringUtils.join(ProgramName, ".send"));
            logData.setProgramException(e);
            sendEMS(logData);
        } finally {
            try {
                if (putQueue != null) {
                    putQueue.close();
                }
            } catch (MQException e) {
                com.syscom.fep.common.log.LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
            try {
                if (queueManager != null) {
                    queueManager.disconnect();
                }
            } catch (MQException e) {
                com.syscom.fep.common.log.LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
    }
}
