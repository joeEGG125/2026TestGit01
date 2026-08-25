package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.JmsMonitorController;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.queue.TWMPQueueConsumers;
import com.syscom.fep.server.common.TxHelper;
//handler尚未完成，先註解
//import com.syscom.fep.server.common.handler.TWMPHandler;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.jms.Message;

@StackTracePointCut(caller = SvrConst.SVR_NB)
public class TWMPServerReceiver extends FEPBase implements JmsReceiver<String> {

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(TWMPQueueConsumers.class).subscribe(this));
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
        String messageIn = "";
        messageIn = payload;
        LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_TWMP, " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.TWMPGWIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
        logData.setMessage(messageIn);
        logData.setRemark("TWMPService Receive Request");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
//            handler尚未完成，先註解
//            TWMPHandler TWMPHandler = new TWMPHandler();
//            TWMPHandler.setEj(logData.getEj());
//            TWMPHandler.setLogContext(logData);
//            messageOut = TWMPHandler.dispatch(FEPChannel.TWMP, messageIn);

//            logData.setProgramName(StringUtils.join(ProgramName, ".processResponseData"));
//            logData.setProgramFlowType(ProgramFlow.TWMPGWOut);
//            logData.setMessageFlowType(MessageFlow.Response);
//            logData.setMessage(messageOut);
//            logData.setRemark("TWMPService Receive Response");
//            this.logMessage(logData);
//
//            if (StringUtils.isNotBlank(messageOut)) {
//                FeeBackSuccess(messageOut,logData);
//            }

            return;
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, "messageReceived"));
            logData.setRemark(StringUtils.join(getName(), " process Queue Failed!!!"));
            sendEMS(logData);
        } finally {
            if (StringUtils.isNotBlank(messageOut)) {
                LogHelperFactory.getTraceLogger().trace(this.getName(), " Send msg:", messageOut);
                logContext.setMessage(StringUtils.join(this.getName(), " Send msg:", messageOut));
                logMessage(logContext);
            }
        }
    }

    public String getName() {
        return SvrConst.SVR_TWMP;
    }

    private boolean FeeBackSuccess(String returnStr, LogData logData) {
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getTwmpAck().getDestination(), returnStr, null, null);
            return true;
        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".sendQueue"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(ex.getMessage());
            logData.setProgramException(ex);
            this.logMessage(logData);
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendQueue"));
            logData.setProgramException(ex);
            sendEMS(logData);
            return false;
        }
    }

}
