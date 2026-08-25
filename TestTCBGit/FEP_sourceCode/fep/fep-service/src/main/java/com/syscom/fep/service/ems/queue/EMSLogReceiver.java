package com.syscom.fep.service.ems.queue;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.jms.JmsReceiver;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.jms.instance.ems.receiver.EmsQueueReceiverConsumers;
import com.syscom.fep.service.ems.parser.EMSLogMessageParser;
import com.syscom.fep.service.ems.vo.EMSLogMessage;
import jakarta.annotation.PostConstruct;
import jakarta.jms.Message;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class EMSLogReceiver extends FEPBase implements JmsReceiver<Serializable> {
    private static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
    private EMSLogMessageParser parser;
    private final static Gson gson = new Gson();

    @PostConstruct
    public void initialization() {
        parser = SpringBeanFactoryUtil.registerBean(EMSLogMessageParser.class);
        SpringBeanFactoryUtil.registerBean(EmsQueueReceiverConsumers.class).subscribe(this);
    }

    /**
     * 接收訊息
     *
     * @param destination
     * @param payload
     * @param message
     */
    @Override
    public void messageReceived(String destination, Serializable payload, Message message) {
        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_EMS);
        List<EMSLogMessage> emsLogMessageList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new StringReader((String) payload))) {
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                try {
                    EMSLogMessage emsLogMessage = gson.fromJson(readLine, EMSLogMessage.class);
                    SERVICELOGGER.debug("EMS Web service Message Type:", emsLogMessage.getMessageType(), ", MessageTarget=", emsLogMessage.getMessageTarget(), ",message=", readLine);
                    emsLogMessageList.add(emsLogMessage);
                } catch (Exception e) {
                    SERVICELOGGER.exceptionMsg(e, "Parse JSON failed, json str = [", readLine, "]");
                }
            }
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, e.getMessage());
        }
        try {
            parser.parseLog(emsLogMessageList);
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, e.getMessage());
        }
    }

//    /**
//     * 接收訊息
//     *
//     * @param destination
//     * @param payload
//     * @param message
//     */
//    @Override
//    public void messageReceived(String destination, PlainTextMessage payload, Message message) {
//        this.messageBatchReceived(destination, Collections.singletonList(payload), Collections.singletonList(message));
//    }
//
//    /**
//     * 批量接收訊息
//     *
//     * @param destination
//     * @param payloadList
//     * @param messageList
//     */
//    @Override
//    public void messageBatchReceived(String destination, List<PlainTextMessage> payloadList, List<Message> messageList) {
//        LogMDC.put(Const.MDC_PROFILE, SvrConst.SVR_EMS);
//        List<String> jsonStrList = new ArrayList<>();
//        for (PlainTextMessage payload : payloadList) {
//            String message = payload.getPayload();
//            try (BufferedReader bufferedReader = new BufferedReader(new StringReader(message))) {
//                String readLine;
//                while ((readLine = bufferedReader.readLine()) != null) {
//                    jsonStrList.add(readLine);
//                }
//            } catch (Exception e) {
//                SERVICELOGGER.exceptionMsg(e, e.getMessage());
//            }
//        }
//        if (!jsonStrList.isEmpty()) {
//            List<EMSLogMessage> emsLogMessageList = new ArrayList<>();
//            Gson gson = new Gson();
//            for (String jsonStr : jsonStrList) {
//                try {
//                    EMSLogMessage emsLogMessage = gson.fromJson(jsonStr, EMSLogMessage.class);
//                    SERVICELOGGER.debug("EMS Web service Message Type:", emsLogMessage.getMessageType(), ", MessageTarget=", emsLogMessage.getMessageTarget(), ",message=", emsLogMessage.toString());
//                    emsLogMessageList.add(emsLogMessage);
//                } catch (Exception e) {
//                    SERVICELOGGER.exceptionMsg(e, "Parse JSON failed, json str = [", jsonStr, "]");
//                }
//            }
//            try {
//                parser.parseLog(emsLogMessageList);
//            } catch (Exception e) {
//                SERVICELOGGER.exceptionMsg(e, e.getMessage());
//            }
//        }
//    }
}
