package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.jms.instance.map.MapQueueOperator;
import com.syscom.fep.jms.instance.map.sso.SSOQueueConfiguration;
import com.syscom.fep.jms.queue.SSOQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.SSOHandler;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_SSO)
public class SSOServerReceiver extends FEPBase implements JmsReceiver<String> {

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(SSOQueueConsumers.class).subscribe(this));
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        String messageIn = payload;
        LogData logData = new LogData();
        logData.setTxRquid(UUIDUtil.randomUUID(true));
        try {
            String returnStr = MultiProcess(logData, messageIn, message);
            String MsgidFromXml = findCLIENTTRACEID(messageIn);
            JmsFactory.setCorrelationID(message, MsgidFromXml);
            String correlationID = JmsFactory.getCorrelationID(message);
            String Msgid = JmsFactory.getMessageId(message);

            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid);
            logData.setRemark("SSO Service Receive Request");
            this.logMessage(logData);
            if (StringUtils.isNotBlank(returnStr)) {
                FeeBackSuccess(logData, returnStr, message);
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, "messageReceived"));
            logData.setRemark(StringUtils.join(getName(), " process Queue Failed!!!"));
            sendEMS(logData);
        }
    }

    public String getName() {
        return SvrConst.SVR_SSO;
    }

    private String MultiProcess(LogData logData, String messageIn, Message message) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        logData.setMessage(messageIn);
        logData.setRemark("SSO Service Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            SSOHandler handler = new SSOHandler();
            handler.setEj(logData.getEj());
            handler.setLogContext(logData);
            handler.setReceivedTime(JmsFactory.getTimestamp(message));
            messageOut = handler.dispatch(FEPChannel.SSO, messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("SSOService Get Response from SSOHandler");
            this.logMessage(logData);
            return messageOut;
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/sso/recv");
            this.logMessage(this.logContext);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramException(e);
            sendEMS(logData);
            throw ExceptionUtil.createRuntimeException(e);
        } finally {
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send msg:", messageOut);
                logContext.setMessage(StringUtils.join(this.getName(), " Send msg:", messageOut));
                logMessage(logContext);
            }
        }
    }

    private boolean FeeBackSuccess(LogData logData, String returnStr, Message messageIn) {
        try {
//            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
//            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
//            sender.sendQueue(configuration.getQueueNames().getSsoAck().getDestination(), returnStr, null, new JmsHandler() {
//                @Override
//                public void setPropertyOut(Message messageOut) throws JMSException {
//                    try {
//                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
////                        JmsFactory.setMessageId(messages, JmsFactory.getMessageId(message).getBytes());
//                    } catch (Exception e) {
//                        return;
//                    }
//                }
//            });
            String replyTo = JmsFactory.getReplyTo(messageIn);
            SSOQueueConfiguration ssoQueueConfiguration = SpringBeanFactoryUtil.getBean(SSOQueueConfiguration.class);
            MapQueueOperator<?, ?> operator = ssoQueueConfiguration.getMapQueueOperator(replyTo);
            if (operator != null) {
                JmsDefinition ackDefinition = ssoQueueConfiguration.getSSOAck(replyTo);
                if (ackDefinition != null) {
                    String ackQueueName = ackDefinition.getDestination();
                    logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
                    logData.setRemark(StringUtils.join("Ready to send Ack Queue, ackQueueName=", ackQueueName, ", replyTo=", replyTo));
                    logData.setMessage(returnStr);
                    this.logMessage(logData);
                    operator.sendQueue(ackQueueName, returnStr, null, new JmsHandler() {
                        @Override
                        public void setPropertyOut(Message messageOut) throws JMSException {
                            try {
                                JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                                // JmsFactory.setMessageId(messageOut, JmsFactory.getMessageId(messageIn).getBytes());
                            } catch (Exception e) {
                                logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
                                logData.setRemark(StringUtils.join("Set JMS Property with Exception occur before Send Ack Queue, ", e.getMessage()));
                                logData.setProgramException(e);
                                sendEMS(logData);
                            }
                        }
                    });
                    logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
                    logData.setRemark(StringUtils.join("Send Ack Queue succeed, ackQueueName=", ackQueueName, ", replyTo=", replyTo));
                    logData.setMessage(returnStr);
                    this.logMessage(logData);
                } else {
                    // 根據replyTo取不到ackDefinition則sendEMS
                    logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
                    logData.setRemark(StringUtils.join("Cannot send Ack Queue cause Ack Definition not exist, replyTo=", replyTo));
                    sendEMS(logData);
                }
            } else {
                // 根據replyTo取不到MapQueueOperator則sendEMS
                logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
                logData.setRemark(StringUtils.join("Cannot send Ack Queue cause MapQueueOperator not exist, replyTo=", replyTo));
                sendEMS(logData);
            }
            return true;
        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
            logData.setRemark(StringUtils.join("Send Ack Queue with Exception occur, ", ex.getMessage()));
            logData.setProgramException(ex);
            sendEMS(logData);
            return false;
        }
    }

    private String findCLIENTTRACEID(String str) {
        // 找到開始標籤和結束標籤的索引
        int startIndex = str.indexOf("<CLIENTTRACEID>");
        int endIndex = str.indexOf("</CLIENTTRACEID>");
        // 提取 CLIENTTRACEID 的值
        String clientTraceId = str.substring(startIndex + "<CLIENTTRACEID>".length(), endIndex);
        return clientTraceId;
    }
}
