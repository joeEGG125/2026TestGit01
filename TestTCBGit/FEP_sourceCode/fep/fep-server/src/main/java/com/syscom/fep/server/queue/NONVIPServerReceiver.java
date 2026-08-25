package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.UUIDUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.instance.map.MapQueueOperator;
import com.syscom.fep.jms.instance.map.nonvip.NONVIPQueueConfiguration;
import com.syscom.fep.jms.queue.NONVIPQueueConsumers;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.NONVIPHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Date;
import java.util.List;

@StackTracePointCut(caller = SvrConst.SVR_NONVIP)
public class NONVIPServerReceiver extends FEPBase implements JmsReceiver<String> {
    private String replyTo;
    private String batNo;
    private boolean isMft = false;
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(NONVIPQueueConsumers.class).subscribe(this));
    }
    @Override
    public void messageReceived(String destination, String payload, Message message) {
        String messageIn = payload;
        LogData logData = new LogData();
        logData.setTxRquid(UUIDUtil.randomUUID(true));
        try {
            MultiProcess(logData, messageIn);
            String MsgidFromXml = findCLIENTTRACEID(messageIn);
            JmsFactory.setCorrelationID(message, MsgidFromXml);
            String correlationID = JmsFactory.getCorrelationID(message);
            String Msgid = JmsFactory.getMessageId(message);
            logData.setProgramFlowType(ProgramFlow.RESTFulIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setMessage("Message: " + message);
            logData.setMessage("correlationID: " + correlationID + " ,Msgid: " + Msgid);
            logData.setRemark("NONVIPService Receive Request");
            logData.setServiceUrl("/nonvip/recv");
            this.logMessage(logData);
            // 交易狀態由AA回覆不在ServerReceiver處理 2026/05/12
//            String npsbatchFileId = correlationID.substring(5, 12);
//            String npsbatchTxDate = correlationID.substring(13, 21);
//            String npsbatchBatchNo = correlationID.substring(21, 34);
//            Npsbatch npsbatch = npsbatchExtMapper.selectByPrimaryKey(npsbatchFileId, npsbatchTxDate, npsbatchBatchNo);
////            String count =  npsdtlExtMapper.getNPSDTLByBATNOforCountBy02(batNo, "02");
////            boolean isAllDone = Integer.valueOf(count != null ? count : "0") == 0;
//            batNo = correlationID.substring(5, 34);
//            List<Npsdtl> npsdtlsList = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
//            boolean isAllDone = true;
//            for(Npsdtl dtl : npsdtlsList){
//                String result = dtl.getNpsdtlResult();
//                if(!StringUtils.equalsAny(result, "01","00")){
//                    isAllDone = false;
//                }
//            }
//            if (isAllDone) {
//                String returnStr = prepareReq(logData, npsbatch, npsdtlsList);
//                if (StringUtils.contains(MsgidFromXml, "IM") && StringUtils.isBlank(replyTo)){
//                    isMft = true;
//                    JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
//                    replyTo = configuration.getQueueNames().getMftAck().getDestination();
//                } else if (StringUtils.contains(MsgidFromXml, "NB") && StringUtils.isBlank(replyTo)){
//                    JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
//                    replyTo = configuration.getQueueNames().getPyBatchAck().getDestination();
//                }
//                if(StringUtils.isBlank(replyTo)){
//                    replyTo = npsbatch.getNpsbatchRtq().substring(npsbatch.getNpsbatchRtq().lastIndexOf("/")+1);
//                }
////                if(StringUtils.contains(MsgidFromXml, "IM"))
////                    returnStr = EbcdicConverter.toHex(returnStr);
//                FeeBackSuccess(logData, returnStr, message, replyTo);
//                updateNpsBatchforRspTime(npsbatch,logData);
//                logData.setProgramName(StringUtils.join(ProgramName, ".FeeBackSuccess"));
//                logData.setMessage("回傳前端訊息 STAN：" + logData.getStan() + ", EJ:" + logData.getEj());
//                logData.setRemark("replyTo : " + replyTo);
//                this.logMessage(logData);
//            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, "messageReceived"));
            logData.setRemark(StringUtils.join(getName(), " process Queue Failed!!!"));
            sendEMS(logData);
        }
    }

    public String getName() {
        return SvrConst.SVR_NONVIP;
    }

    @Override
    public void messageBatchReceived(String destination, List<String> payloadList, List<Message> messageList) {
        JmsReceiver.super.messageBatchReceived(destination, payloadList, messageList);
    }

    private String MultiProcess(LogData logData, String messageIn) {
        LogMDC.put(Const.MDC_PROFILE, this.getName());
        LogHelperFactory.getTraceLogger().trace(this.getName(), " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
        logData.setMessage(messageIn);
        logData.setRemark("NONVIPService Receive Request");
        logData.setServiceUrl("/nonvip/recv");
        logData.setEj(TxHelper.generateEj());
        this.logMessage(logData);
        try {
            if (StringUtils.isBlank(messageIn)) {
                throw ExceptionUtil.createException("收到空白電文");
            }
            NONVIPHandler nonvipHandler = new NONVIPHandler();
            nonvipHandler.setEj(logData.getEj());
            nonvipHandler.setLogContext(logData);
            messageOut = nonvipHandler.dispatch(FEPChannel.NONVIP, messageIn);
            replyTo = nonvipHandler.getReplyTo();
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setMessage(messageOut);
            logData.setRemark("NONVIPService Get Response from NONVIPHandler");
            this.logMessage(logData);
            return messageOut;
        } catch (Exception e) {
            logData.setMessage(messageIn);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendReceive"));
            logData.setProgramFlowType(ProgramFlow.RESTFulOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setRemark(e.getMessage());
            logData.setProgramException(e);
            logData.setServiceUrl("/nonvip/recv");
            this.logMessage(logData);
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

    private boolean FeeBackSuccess(LogData logData, String returnStr, Message messageIn, String replyTo) {
        try {
            NONVIPQueueConfiguration nonvipQueueConfiguration = SpringBeanFactoryUtil.getBean(NONVIPQueueConfiguration.class);
            MapQueueOperator<?, ?> operator = nonvipQueueConfiguration.getMapQueueOperator(replyTo);
            if (operator != null) {
                JmsDefinition ackDefinition = nonvipQueueConfiguration.getNonvipAck(replyTo);
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
                                if(isMft){
                                    JmsFactory.setCharacterSet(messageOut, 937);
                                }
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

    private String findCHANNEL(String str) {
        // 找到開始標籤和結束標籤的索引
        int startIndex = str.indexOf("<CHANNEL>");
        int endIndex = str.indexOf("</CHANNEL>");
        // 提取 CHANNEL 的值
        String clientTraceId = str.substring(startIndex + "<CHANNEL>".length(), endIndex);
        return clientTraceId;
    }

    private String prepareReq(LogData logData, Npsbatch npsbatch, List<Npsdtl> alld) {
        StringBuilder sLine = null;
        String queue = "";
        try {
//            List<Npsdtl> alld = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
            /*MQ檔案傳送指示*/
            queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchFileId().trim(), 8, " ");
            queue = queue + StringUtils.leftPad(npsbatch.getNpsbatchBatchNo(), 13, "0");
            queue = queue + "230";
            queue = queue + StringUtils.leftPad(npsbatch.getNpsbatchTotCnt().toString(), 5, "0");

            /* BODY */
            /* 首筆 */
            queue = queue + "11";
            String date = npsbatch.getNpsbatchTxDate();
            date = CalendarUtil.adStringToROCString(date);
            queue = queue + StringUtils.leftPad(date, 7, "0");
            queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchBatchNo(), 13, " ");
            queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchBranch(), 4, " ");
            queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchHeadMemo(), 154, " ");
            queue = queue + StringUtils.rightPad("", 50, " ");

            /* 明細筆 */
            for (int i = 0; i < alld.size(); i++) {
                queue += "12";
                String detaildate = npsbatch.getNpsbatchTxDate();
                detaildate = CalendarUtil.adStringToROCString(detaildate);
                queue = queue + StringUtils.leftPad(detaildate, 7, "0");
                queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchBatchNo(), 13, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlSeqNo().toString(), 10, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlAtmno(), 5, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlTerminalid(), 8, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTxTime(), 6, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlBusinessUnit(), 8, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPaytype(), 5, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPayno(), 4, " ");
                Double txAmt = Double.valueOf(alld.get(i).getNpsdtlTxAmt().toString()) * 100;
                int txAmtt = txAmt.intValue();
                queue = queue + StringUtils.leftPad(String.valueOf(txAmtt), 11, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlIdno(), 11, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlReconSeq(), 16, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTroutBkno7(), 7, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTroutActno(), 16, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTrinBkno(), 3, "0");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlTrinActno(), 16, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlAllowT1(), 1, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlDueDate(), 8, " ");
                queue = queue + StringUtils.rightPad("", 23, " ");
                queue = queue + StringUtils.rightPad("006" + alld.get(i).getNpsdtlStan(), 10, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlPcode(), 4, " ");
                if (alld.get(i).getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                        && alld.get(i).getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    queue = queue + "CB2W";
                } else {
                    queue = queue + "AB3W";
                }
                String datetbsdy = CalendarUtil.adStringToROCString(alld.get(i).getNpsdtlTbsdy()); // 西元轉民國
                queue = queue + StringUtils.leftPad(datetbsdy, 7, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlHostBrch(), 4, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlCbsRc(), 3, " ");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlReplyCode(), 4, " ");
                queue = queue + StringUtils.leftPad(alld.get(i).getNpsdtlHostCharge().toString(), 2, "0");
                queue = queue + StringUtils.rightPad(alld.get(i).getNpsdtlHostChargeFlag(), 1, " ");
                queue = queue + StringUtils.rightPad("", 11, " ");
            }

            /* 尾筆 */
            queue += "19";
            String endDate = npsbatch.getNpsbatchTxDate();
            endDate = CalendarUtil.adStringToROCString(endDate);
            queue = queue + StringUtils.leftPad(endDate, 7, "0");
            queue = queue + StringUtils.rightPad(npsbatch.getNpsbatchBatchNo(), 13, " ");
            queue = queue + StringUtils.leftPad(npsbatch.getNpsbatchTotCnt().toString(), 6, "0");
            Double tot = Double.valueOf(npsbatch.getNpsbatchTotAmt().toString()) * 100;
            int tott = tot.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(tott), 14, "0");
            queue = queue + StringUtils.leftPad(npsbatch.getNpsbatchOkCnt().toString(), 6, "0");
            Double ok = Double.valueOf(npsbatch.getNpsbatchOkAmt().toString()) * 100;
            int okt = ok.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(okt), 14, "0");
            queue = queue + StringUtils.leftPad(npsbatch.getNpsbatchFailCnt().toString(), 6, "0");
            Double fail = Double.valueOf(npsbatch.getNpsbatchFailAmt().toString()) * 100;
            int failt = fail.intValue();
            queue = queue + StringUtils.leftPad(String.valueOf(failt), 14, "0");
            queue = queue + StringUtils.repeat(" ", 148);

            return queue;
        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".prepareReq"));
            logData.setRemark(StringUtils.join("prepareReq failed, ", ex.getMessage()));
            logData.setProgramException(ex);
            sendEMS(logData);
            return null;
        }
    }

    private void updateNpsBatchforRspTime(Npsbatch npsbatch, LogData logData){
        try {
            npsbatch.setNpsbatchRspTime(new Date());
            npsbatch.setNpsbatchRspStan(logData.getStan());
            npsbatch.setNpsbatchRspEjfno(logData.getEj());
            if (npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch) < 1) {
                logContext.setRemark("更新npsbatch失敗");
                logMessage(Level.DEBUG, logData);
                sendEMS(logContext);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
