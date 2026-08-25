package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.jms.*;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.jms.JmsMsgConfiguration;
import com.syscom.fep.jms.JmsMsgConstant;
import com.syscom.fep.jms.JmsMsgSimpleOperator;
import com.syscom.fep.jms.instance.map.MapQueueOperator;
import com.syscom.fep.jms.instance.map.pybatch.PyBatchQueueConfiguration;
import com.syscom.fep.jms.instance.map.pybatch.PyBatchQueueConfigurationProperties;
import com.syscom.fep.jms.queue.PYBatchQueueConsumers;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@StackTracePointCut(caller = SvrConst.SVR_PYBatch)
public class PYBatchReceiver extends FEPBase implements JmsReceiver<String> {

    public static final PyBatchQueueConfiguration NPAY_QUEUE_CONFIGURATION = SpringBeanFactoryUtil.getBean(PyBatchQueueConfiguration.class);

    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);

    public String getName() {
        return SvrConst.SVR_PYBatch;
    }

    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(PYBatchQueueConsumers.class).subscribe(this));
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
        InnerClass inner = new InnerClass();
        inner.message = payload;
        inner.maxThreads = CMNConfig.getInstance().getSECMaxThreads();
        Boolean result = false;
        LogData logData = new LogData();
        inner.setVip(inner.message.substring(57, 58));

        logData.setEj(TxHelper.generateEj());
        logData.setChannel(FEPChannel.parse(inner.message.substring(0, 8).trim()));
        logData.setMessageId(inner.message.substring(8, 21).trim());

        writLog(logData, "destination:(" + destination + "),payload:(" + payload + ")", "PYBatchReceiver", ".messageReceived");
        writLog(logData, "inner.maxThreads", "PYBatchReceiver", ".messageReceived");
        try {
            inner.W_EJFNO = TxHelper.generateEj();
            //檢核檔名，電文中前29碼為檔名資訊
            String tita = messageIn.substring(0, 29);
            String filename = tita.substring(0, 8).trim();

            // 1. 檢核檔名是否為 AMPAYFL
            if (!StringUtils.equals(filename, "AMPAYFL")) {
                inner.W_ERR_CODE = "K018";
                inner.W_ERR_MSG = "MQ傳送檔名非NB整批轉時即交易";
                writLog(logData, "", "電文檔名檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                ACKResponse(logData, inner, message);
                return;
            }

            String txDateROC = tita.substring(8, 15);
            String txDateAD = String.valueOf(Integer.parseInt(txDateROC) + 19110000);
            String batchNo = tita.substring(8, 21);
            inner.branch = messageIn.substring(29 + 22, 29 + 26); //首筆放行分行
            inner.npsdtlBatNo = tita.substring(0, 8) + txDateAD + batchNo;
            String sysDateAD = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            logData.setChannel(FEPChannel.parse(filename));
            logData.setTxDate(txDateAD);
            logData.setMessageId(batchNo);

            //交易電文
            inner.W_REC_LEN = tita.substring(21, 24); //扣款檔 180
            inner.W_REC_COUNT = StringUtils.leftPad(tita.substring(24, 29).trim(), 5, "0"); //傳送筆數
            inner.fileid = filename;
            inner.txdate = txDateAD;
            inner.batchNo = batchNo;

            // 檢核交易日期非本日
            if (!txDateAD.equals(sysDateAD)) {
                inner.W_ERR_CODE = "K002";
                inner.W_ERR_MSG = "交易日期非本日";
                writLog(logData, "", "電文檔名檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                ACKResponse(logData, inner, message);
                return;
            }

            // 查詢收檔紀錄 (檢核是否處理過)
            inner.npsbatch = npsbatchExtMapper.selectByPrimaryKey(inner.fileid, inner.txdate, inner.batchNo);
            if (inner.npsbatch != null) {
                if ("00".equals(inner.npsbatch.getNpsbatchResult())) {
                    inner.W_ERR_CODE = "K008";
                    inner.W_ERR_MSG = "該批交易已發送處理完成，交易處理中覆";
                } else if ("02".equals(inner.npsbatch.getNpsbatchResult())) {
                    inner.W_ERR_CODE = "K007";
                    inner.W_ERR_MSG = "該批交易處理完成，已回覆結果檔";
                } else if (StringUtils.isBlank(inner.npsbatch.getNpsbatchResult())) {
                    inner.W_ERR_CODE = "K008";
                    inner.W_ERR_MSG = "該批資料檢核中，尚未發送交易";
                } else if ("01".equals(inner.npsbatch.getNpsbatchResult())) {
                    inner.W_ERR_CODE = "K006";
                    inner.W_ERR_MSG = "該批資料檢核失敗，且已回覆";
                }
                writLog(logData, "", "電文檔名檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                ACKResponse(logData, inner, message);
                return;
            }

            // 2. 首筆檢核並寫入NPSBATCH整批轉即時批次檔
            if (!insertNpsbatch(logData, inner, message)) {
                return; // 內部若失敗已執行 ACKResponse，這邊直接結束
            }

            // 3. 檢核明細錄及尾筆
            if (!checkDetails(logData, inner, message)) {
                updateNpsbatch01(logData, inner, message, inner.W_ERR_MSG); // 更新為失敗
                ACKResponse(logData, inner, message);
                return;
            } else {
                inner.npsbatch.setNpsbatchTotCnt(inner.W_SEQ_NO); //交易總筆數
                inner.npsbatch.setNpsbatchTotAmt(inner.W_TOT_TX_AMT); //交易總金額
                if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                    inner.W_ERR_MSG = "檢核明細錄UPDATE NPSBATCH Error";
                    inner.W_ERR_CODE = "FXXX";
                    writLog(logData, "", "電文明細錄或尾筆檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                    ACKResponse(logData, inner, message);
                } else {
                    inner.W_ERR_CODE = ""; // 清空錯誤碼代表成功
                }
            }

            // 4. 檢核成功寫入NPSDTL批次轉即時扣款檔
            if (!insertAllDetails(logData, inner, message)) {
                return; // 寫入失敗直接返回 (錯誤已在方法內處理)
            }

            // 5. 逐筆讀取 NPSDTL,產生單筆Request電文, 寫入MQ(VIP)或MQ(NONVIP)
            if (!processSingleRequestMessages(logData, inner, message, filename, txDateAD, batchNo, inner.branch, messageIn)) {
                return;
            }


            // 回前端檢核成功發送扣款
            inner.W_ERR_MSG = "整批逐筆發送完成";
            inner.W_ERR_CODE = "0000";
            writLog(logData, "", "成功發送扣款：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

            //  6. 組ACK回應電文 (檢核錯誤時執行) 主流程不做
            ACKResponse(logData, inner, message);

        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, "messageReceived"));
            logData.setRemark(StringUtils.join(getName(), " process Queue Failed!!!"));
            sendEMS(logData);
        }
    }


    public static class InnerClass {
        private String W_ERR_CODE;
        private String W_ERR_MSG;
        private String W_REC_LEN;
        private BigDecimal W_TOT_TX_AMT = BigDecimal.ZERO;
        private String W_REC_COUNT;
        private Integer W_SEQ_NO = 0;
        private String message; //完整電文
        private Npsbatch npsbatch = new Npsbatch();
        private Npsdtl npsdtl = new Npsdtl();

        private String fileid;
        private String txdate;
        private String batchNo;
        private String npsdtlBatNo; //NPSDTL_BAT_NO -> 檔名(8)西元日期(8)批次號碼(13)
        private Integer maxThreads;
        private Integer threadNo;
        private BigDecimal charge;
        private String brch;
        private String chargeFlag;
        private String vip;
        private int W_EJFNO;
        private String branch;

        public BigDecimal getCharge() {
            return charge;
        }

        public void setCharge(BigDecimal charge) {
            this.charge = charge;
        }

        public String getBrch() {
            return brch;
        }

        public void setBrch(String brch) {
            this.brch = brch;
        }

        public String getChargeFlag() {
            return chargeFlag;
        }

        public void setChargeFlag(String chargeFlag) {
            this.chargeFlag = chargeFlag;
        }

        public String getVip() {
            return vip;
        }

        public void setVip(String vip) {
            this.vip = vip;
        }

        @Override
        public String toString() {
            return "InnerClass{" +
                    "W_ERR_CODE='" + W_ERR_CODE + '\'' +
                    ", W_ERR_MSG='" + W_ERR_MSG + '\'' +
                    ", W_REC_LEN='" + W_REC_LEN + '\'' +
                    ", W_TOT_TX_AMT=" + W_TOT_TX_AMT +
                    ", W_REC_COUNT='" + W_REC_COUNT + '\'' +
                    '}';
        }
    }

    private boolean SendQueueToVIPServerReceiver(LogData logData, String returnStr, Message messageIn) {
        PlatformTransactionManager tx = SpringBeanFactoryUtil.getBean(JmsMsgConstant.JMS_TRANSACTION_MANAGER);
        TransactionStatus txStatus = tx.getTransaction(new DefaultTransactionDefinition());
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getVip().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                    } catch (Exception e) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToVIPServerReceiver"));
                        logData.setRemark(StringUtils.join("Set JMS Property with Exception occur before Send Ack Queue, ", e.getMessage()));
                        logData.setProgramException(e);
                        sendEMS(logData);
                        throw new RuntimeException(e); // 拋出異常讓外層捕捉以觸發 Rollback
                    }
                }
            });
            writLog(logData, returnStr, "SendQueueToVIPServerReceiver ready to commit.", ".SendQueueToVIPServerReceiver");
            tx.commit(txStatus); // 發送成功 Commit
            writLog(logData, returnStr, "SendQueueToVIPServerReceiver commit successful.", ".SendQueueToVIPServerReceiver");
            return true;
        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToVIPServerReceiver"));
            logData.setRemark(StringUtils.join("SendQueueToVIPServerReceiver failed, ", ex.getMessage()));
            sendEMS(logData);

            try {
                tx.rollback(txStatus); // 發生錯誤 Rollback
                writLog(logData, returnStr, "Transaction rolled back due to error.", ".SendQueueToVIPServerReceiver");
            } catch (TransactionException te) {
                logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToVIPServerReceiver"));
                logData.setProgramException(te);
                sendEMS(logData);
            }
            return false;
        }
    }

    private boolean SendQueueToNONVIPServerReceiver(LogData logData, String returnStr, Message messageIn) {
        PlatformTransactionManager tx = SpringBeanFactoryUtil.getBean(JmsMsgConstant.JMS_TRANSACTION_MANAGER);
        TransactionStatus txStatus = tx.getTransaction(new DefaultTransactionDefinition());
        try {
            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
            sender.sendQueue(configuration.getQueueNames().getNonvip().getDestination(), returnStr, null, new JmsHandler() {
                @Override
                public void setPropertyOut(Message messageOut) throws JMSException {
                    try {
                        JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                    } catch (Exception e) {
                        logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToNONVIPServerReceiver"));
                        logData.setRemark(StringUtils.join("Set JMS Property with Exception occur before Send Ack Queue, ", e.getMessage()));
                        sendEMS(logData);
                        throw new RuntimeException(e); // 拋出異常讓外層捕捉以觸發 Rollback
                    }
                }
            });

            writLog(logData, returnStr, "SendQueueToNONVIPServerReceiver ready to commit.", ".SendQueueToNONVIPServerReceiver");
            tx.commit(txStatus); // 發送成功 Commit
            writLog(logData, returnStr, "SendQueueToNONVIPServerReceiver commit successful.", ".SendQueueToNONVIPServerReceiver");
            return true;

        } catch (Exception ex) {
            logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToNONVIPServerReceiver"));
            logData.setRemark(StringUtils.join("SendQueueToNONVIPServerReceiver failed, ", ex.getMessage()));
            sendEMS(logData);

            try {
                tx.rollback(txStatus); // 發生錯誤 Rollback
                writLog(logData, returnStr, "Transaction rolled back due to error.", ".SendQueueToNONVIPServerReceiver");
            } catch (TransactionException te) {
                logData.setProgramName(StringUtils.join(ProgramName, ".SendQueueToNONVIPServerReceiver"));
                logData.setProgramException(te);
                sendEMS(logData);
            }
            return false;
        }
    }

    public boolean updateNpsbatch01(LogData logData, InnerClass inner, Message message, String errMsg) {
        try {
            inner.npsbatch.setNpsbatchResult("01"); //整批剔退
            inner.npsbatch.setNpsbatchErrMsg(errMsg);

            if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = "UPDATE NPSBATCH Error";
                inner.W_ERR_CODE = "FXXX";
                writLog(logData, "", "電文明細錄或尾筆檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".updateNpsbatch01");

                ACKResponse(logData, inner, message);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void ACKResponse(LogData logData, InnerClass inner, Message messageIn) throws Exception {
        String REPLY_FILE_ID = StringUtils.rightPad(inner.message.substring(0, 8), 8, " ");//(1)MQ傳送檔名
        String REPLY_BATCH_NO = StringUtils.leftPad(inner.message.substring(8, 21), 13, "0");//(2)檔案批號
        String REPLY_REC_LEN  =  "100";//(3)MQ傳送檔REC-LEN
        String REPLY_REC_CNT  =  "00001";//(4)傳送筆數
        String FILLER1  =  "99";//(5)RECORD-TYPE

        String REPLY_KEY_1 = StringUtils.rightPad(inner.message.substring(8, 15), 7, " ");//(6)交易日期
        String REPLY_KEY_2 = StringUtils.rightPad(inner.message.substring(8, 21), 13, " ");//(7)批次號碼
        String ERR_CODE = StringUtils.rightPad(StringUtils.defaultString(inner.W_ERR_CODE), 4, " ");//(8)錯誤代碼
        String msg = StringUtils.isBlank(inner.W_ERR_MSG) ? "" : inner.W_ERR_MSG;
        String ERR_MSG = StringUtils.rightPad(msg, 60, " ");//(9)錯誤代碼說明

        String ack = REPLY_FILE_ID + REPLY_BATCH_NO + REPLY_REC_LEN + REPLY_REC_CNT + FILLER1
                + REPLY_KEY_1 + REPLY_KEY_2 + ERR_CODE + ERR_MSG + StringUtils.rightPad("", 14, " ");

        logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
        logData.setMessage("W_ERR_CODE='" + ERR_CODE + "',ERR_MSG='" + ERR_MSG + "',REPLY_KEY_1='" + REPLY_KEY_1 + "',REPLY_KEY_2='" + REPLY_KEY_2 + "'");
        this.logMessage(logData);
        MapQueueOperator<?, ?> operator = null;
        String replyTo = JmsFactory.getReplyTo(messageIn);
        if (StringUtils.isNotBlank(replyTo)) {
            operator = NPAY_QUEUE_CONFIGURATION.getMapQueueOperator(replyTo);
        } else {
            // 如果replyTo是空值.則拿第一組為default by Jack
            PyBatchQueueConfigurationProperties configProps = NPAY_QUEUE_CONFIGURATION.getProp().get(0);
            operator = NPAY_QUEUE_CONFIGURATION.getMapQueueOperator(configProps.getKey());
            replyTo = StringUtils.isNotBlank(configProps.getReplyTo())
                    ? configProps.getReplyTo()
                    : configProps.getKey();
        }

        if (operator != null) {
            JmsDefinition ackDefinition = NPAY_QUEUE_CONFIGURATION.getPybatchAck(replyTo);
            if (ackDefinition != null) {
                String ackQueueName = ackDefinition.getDestination();
                logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
                logData.setRemark(StringUtils.join("Ready to send Ack Queue, ackQueueName=", ackQueueName, ", replyTo=", replyTo));
                logData.setMessage(ack);
                this.logMessage(logData);
                operator.sendQueue(ackQueueName, ack, null, new JmsHandler() {
                    @Override
                    public void setPropertyOut(Message messageOut) throws JMSException {
                        try {
                            JmsFactory.setCorrelationID(messageOut, JmsFactory.getCorrelationIDAsBytes(messageIn));
                            // JmsFactory.setMessageId(messageOut, JmsFactory.getMessageId(messageIn).getBytes());
                        } catch (Exception e) {
                            logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
                            logData.setRemark(StringUtils.join("Set JMS Property with Exception occur before Send Ack Queue, ", e.getMessage()));
                            logData.setProgramException(e);
                            sendEMS(logData);
                        }
                    }
                });

                logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
                logData.setRemark(StringUtils.join("Send Ack Queue succeed, ackQueueName=", ackQueueName, ", replyTo=", replyTo));
                logData.setMessage(ack);
                this.logMessage(logData);
            } else {
                // 根據replyTo取不到ackDefinition則sendEMS
                logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
                logData.setRemark(StringUtils.join("Cannot send Ack Queue cause Ack Definition not exist, replyTo=", replyTo));
                sendEMS(logData);
            }
        } else {
            // 根據replyTo取不到MapQueueOperator則sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
            logData.setRemark(StringUtils.join("Cannot send Ack Queue cause MapQueueOperator not exist, replyTo=", replyTo));
            sendEMS(logData);
        }
        writLog(logData, inner.toString(), inner.npsdtlBatNo + "-ACKResponse done.", ".ACKResponse");
    }

    private void writLog(LogData logData, String msg, String mark, String progName) {
        logData.setMessage(msg);
        logData.setProgramName(StringUtils.join(ProgramName, progName));
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setRemark(mark);
        this.logMessage(logData);
    }

    /**
     * 2. 首筆檢核並寫入NPSBATCH整批轉即時批次檔
     */
    private boolean insertNpsbatch(LogData logData, InnerClass inner, Message message) {
        try {
            // 首筆資料起點 (MQ檔頭佔 29 bytes)
            String tita1 = inner.message.substring(29, 209);  //讀取第一筆(首筆”01”)資料
            String recType = tita1.substring(0, 2); //首筆”01”
            String txDateROC = tita1.substring(2, 9); //交易日期
            String batchNo = tita1.substring(9, 22); //批次號碼
//            String branch = tita1.substring(22, 26); //放行分行
            String VIP = inner.getVip(); //VIP註記
            String headMemo = tita1.substring(0, 180);

            if (!"01".equals(recType)) { //首筆代號
                inner.W_ERR_CODE = "K001";
                inner.W_ERR_MSG = "REC TYPE ERROR";
            } else if (!inner.txdate.equals(CalendarUtil.rocStringToADString(txDateROC))) {  //交易日期
                inner.W_ERR_CODE = "K002";
                inner.W_ERR_MSG = "首筆中的交易日與檔案日期不符";
            } else if (!inner.batchNo.equals(batchNo)) { //批次號碼
                inner.W_ERR_CODE = "K003";
                inner.W_ERR_MSG = "首筆中的批次號碼與檔案批號不符";
            } else if (StringUtils.isBlank(inner.branch) || "0000".equals(inner.branch)) { //放行分行
                inner.W_ERR_CODE = "K004";
                inner.W_ERR_MSG = "首筆中的放行分行為空白或0000";
            }

            //寫入NPSBATCH
            inner.npsbatch = new Npsbatch();
            inner.npsbatch.setNpsbatchFileId(inner.fileid);
            inner.npsbatch.setNpsbatchTxDate(inner.txdate);
            inner.npsbatch.setNpsbatchBatchNo(inner.batchNo);
            inner.npsbatch.setNpsbatchBranch(inner.branch);
            inner.npsbatch.setNpsbatchRcvTime(new Date());//(HHMMSS)
            inner.npsbatch.setNpsbatchVip(VIP);
            inner.npsbatch.setNpsbatchRtq(JmsFactory.getReplyTo(message));//紀錄收到RQ電文的MQ
            inner.npsbatch.setNpsbatchCorrelId(JmsFactory.getCorrelationID(message));
            inner.npsbatch.setNpsbatchHeadMemo(headMemo.trim());

            if (StringUtils.isNotBlank(inner.W_ERR_CODE)) { //首筆檢核錯誤
                inner.npsbatch.setNpsbatchResult("01"); //整批剔退
            }
            inner.npsbatch.setNpsbatchRspEjfno(inner.W_EJFNO);
            inner.npsbatch.setNpsbatch01Tita(tita1); //紀錄首筆電文(組結果檔用)

            if (npsbatchExtMapper.insertSelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = "INSERT NPSBATCH Error";
                inner.W_ERR_CODE = "FXXX";
                ACKResponse(logData, inner, message);
                return false;
            }

            writLog(logData, "", "npsbatchExtMapper insert done.", ".insertNpsbatch");

            if (StringUtils.isNotBlank(inner.W_ERR_CODE)) {
                writLog(logData, "", "電文首筆檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".insertNpsbatch");

                ACKResponse(logData, inner, message);
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 檢核明細錄及尾筆
     */
    private Boolean checkDetails(LogData logData, InnerClass inner, Message message) {
        try {
            int recLen = Integer.valueOf(inner.W_REC_LEN);
            String headerTxDateROC = inner.message.substring(8, 15);
            int currentPos = 29 + recLen; // 從第二筆 (明細) 開始，跳過 MQ 傳送指示與首筆
            int recordIndex = 1;
            boolean hasTrailer = false;

            inner.W_SEQ_NO = 0;
            inner.W_TOT_TX_AMT = BigDecimal.ZERO;

            while (currentPos + recLen <= inner.message.length()) {
                String recType = inner.message.substring(currentPos, currentPos + 2);

                // 1. 檢核明細錄或尾筆代號
                if (!"02".equals(recType) && !"09".equals(recType)) {
                    inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄REC TYPE ERROR：" + recType;
                    inner.W_ERR_CODE = "K001";
                    return false;
                }

                if ("02".equals(recType)) {
                    // 2. 交易日期檢核
                    String currentTxDateROC = inner.message.substring(currentPos + 2, currentPos + 9);
                    if (!currentTxDateROC.equals(headerTxDateROC)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄的交易日與檔案日期不符!";
                        inner.W_ERR_CODE = "K002";
                        return false;
                    }

                    // 3. 批次號碼檢核
                    String currentBatchNo = inner.message.substring(currentPos + 9, currentPos + 22);
                    if (!currentBatchNo.equals(inner.batchNo)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的批次號碼與檔案批號不符!";
                        inner.W_ERR_CODE = "K003";
                        return false;
                    }

                    // 4. 交易上傳序號
                    String seqNoStr = inner.message.substring(currentPos + 22, currentPos + 32);
                    if (!String.valueOf(inner.W_SEQ_NO + 1).equals(String.valueOf(Integer.valueOf(seqNoStr)))) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的序號未依序!";
                        inner.W_ERR_CODE = "K009";
                        return false;
                    }

                    // 5. 端末機代號( Txxxx NAM (第1位=T , 後3位=“NAM”) )
                    String termId = inner.message.substring(currentPos + 37, currentPos + 45);
                    if (!termId.startsWith("T") || !termId.endsWith("NAM")) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的端末機代號錯誤!";
                        inner.W_ERR_CODE = "K010";
                        return false;
                    }

                    // 6. 端末機代號( 第2~5位 = 放行分行 )
                    if (!inner.npsbatch.getNpsbatchBranch().equals(termId.substring(1, 5))) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中端末機代號分行代號與首筆不符";
                        inner.W_ERR_CODE = "K011";
                        return false;
                    }

                    // 7. 委託單位代號(8)為空白或全為0
                    String bizUnit = inner.message.substring(currentPos + 51, currentPos + 59);
                    if (StringUtils.isBlank(bizUnit) || "00000000".equals(bizUnit)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的委託單位代號錯誤!";
                        inner.W_ERR_CODE = "K012";
                        return false;
                    }

                    // 8. 繳費類別(5)為空白或全為0
                    String payType = inner.message.substring(currentPos + 59, currentPos + 64);
                    if (StringUtils.isBlank(payType) || "00000".equals(payType)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的繳費類別錯誤!";
                        inner.W_ERR_CODE = "K012";
                        return false;
                    }

                    // 9. 費用代號(4)為空白或全為0
                    String payNo = inner.message.substring(currentPos + 64, currentPos + 68);
                    if (StringUtils.isBlank(payNo) || "0000".equals(payNo)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的繳費代號錯誤!";
                        inner.W_ERR_CODE = "K012";
                        return false;
                    }

                    // 10. 交易金額非數字 或為0
                    String txAmtStr = inner.message.substring(currentPos + 68, currentPos + 79);
                    if (!PolyfillUtil.isNumeric(txAmtStr) || "00000000000".equals(txAmtStr)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的繳費金額錯誤!";
                        inner.W_ERR_CODE = "K013";
                        return false;
                    }
                    // 累計金額與尾筆(09)核對
                    inner.W_TOT_TX_AMT = inner.W_TOT_TX_AMT.add(BigDecimal.valueOf(Double.valueOf(txAmtStr) / 100));

                    // 11. 身分證統一編號 為空白或全為0
                    String idno = inner.message.substring(currentPos + 79, currentPos + 90);
                    if (StringUtils.isBlank(idno) || "00000000000".equals(idno)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的身分證統一編號錯誤!";
                        inner.W_ERR_CODE = "K019";
                        return false;
                    }

                    // 12. 轉出行為空 或為0
                    String troutBkno = inner.message.substring(currentPos + 106, currentPos + 113);
                    if (StringUtils.isBlank(troutBkno) || "0000000".equals(troutBkno)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的轉出行錯誤!";
                        inner.W_ERR_CODE = "K014";
                        return false;
                    }

                    // 13. 轉出帳號為空 或為0
                    String troutAccno = inner.message.substring(currentPos + 113, currentPos + 129);
                    if (StringUtils.isBlank(troutAccno) || "0000000000000000".equals(troutAccno)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的轉出帳號錯誤!";
                        inner.W_ERR_CODE = "K014";
                        return false;
                    }

                    // 14. 轉入行為空 或為0
                    String trinBkno = inner.message.substring(currentPos + 129, currentPos + 132);
                    if (StringUtils.isBlank(trinBkno) || "000".equals(trinBkno)) {
                        inner.W_ERR_MSG = "第" + recordIndex + "筆明細錄中的轉入行錯誤!";
                        inner.W_ERR_CODE = "K015";
                        return false;
                    }
                    

                    inner.W_SEQ_NO += 1;

                } else if ("09".equals(recType)) {

                    // 16. 尾筆交易日期
                    String currentTxDateROC = inner.message.substring(currentPos + 2, currentPos + 9);
                    if (!currentTxDateROC.equals(headerTxDateROC)) {
                        inner.W_ERR_MSG = "尾筆的交易日與檔案日期不符";
                        inner.W_ERR_CODE = "K002";
                        return false;
                    }

                    // 17. 尾筆批次號碼
                    String currentBatchNo = inner.message.substring(currentPos + 9, currentPos + 22);
                    if (!currentBatchNo.equals(inner.batchNo)) {
                        inner.W_ERR_MSG = "尾筆的批次號碼與檔案批號不符";
                        inner.W_ERR_CODE = "K003";
                        return false;
                    }

                    // 18. 尾筆總筆數與明細筆數核對
                    String totCntStr = inner.message.substring(currentPos + 22, currentPos + 28);
                    if (!inner.W_SEQ_NO.toString().equals(String.valueOf(Integer.valueOf(totCntStr)))) {
                        inner.W_ERR_MSG = "明細筆數與尾筆不符";
                        inner.W_ERR_CODE = "K016";
                        return false;
                    }

                    // 19. 尾筆總金額與累計金額核對
                    String totAmtStr = inner.message.substring(currentPos + 28, currentPos + 42);
                    if (inner.W_TOT_TX_AMT.compareTo(BigDecimal.valueOf(Double.valueOf(totAmtStr) / 100)) != 0) {
                        inner.W_ERR_MSG = "明細金額與尾筆不符";
                        inner.W_ERR_CODE = "K017";
                        return false;
                    }

                    hasTrailer = true;
                    break; // W_ENDFLAG = 1 (結束迴圈)
                }

                currentPos += recLen;
                recordIndex++;
            }

            if (!hasTrailer) {
                inner.W_ERR_MSG = "找不到尾筆(09)資料";
                inner.W_ERR_CODE = "FXXX";
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 4. 檢核成功寫入NPSDTL批次轉即時扣款檔
     */
    private boolean insertAllDetails(LogData logData, InnerClass inner, Message message) {
        try {
            int recLen = Integer.valueOf(inner.W_REC_LEN); //180
            int currentPos = 29 + recLen; // 從第二筆 (明細) 開始
            int recordIndex = 0;
            int insertNPSDTLCount = 0;

            while (currentPos + recLen <= inner.message.length()) {
                String recType = inner.message.substring(currentPos, currentPos + 2);
                if ("09".equals(recType)) {
                    String tita9 = inner.message.substring(currentPos, currentPos + recLen);
                    inner.npsbatch.setNpsbatch09Tita(tita9);
                    break; // 讀到尾筆結束寫檔
                }

                if ("02".equals(recType)) {
                    if (!InsertNPSDTL(logData, currentPos, recordIndex, inner, message)) {
                        return false;
                    }
                    insertNPSDTLCount++;
                    if(insertNPSDTLCount == inner.W_SEQ_NO){
                        String logRemark = "批號:" + inner.batchNo + "明細 npsdtl insert all done. 筆數共" + insertNPSDTLCount + "筆.";
                        writLog(logData, "", logRemark, ".insertAllDetails");
                    }
                }
                currentPos += recLen;
                recordIndex++;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 單筆 NPSDTL 寫入邏輯
     */
    private boolean InsertNPSDTL(LogData logData, int start, int recordIndex, InnerClass inner, Message message) {
        Npsdtl defNpsdtl = new Npsdtl();
        try {
            int recLen = Integer.parseInt(inner.W_REC_LEN);
            String tita2 = inner.message.substring(start, start + recLen);

            String NpsdtlTxDate = inner.message.substring(start + 2, start + 9);
            //NPSDTL_BAT_NO -> 檔名(8)西元日期(8)批次號碼(13)
            String NpsdtlBatchNo = inner.npsdtlBatNo;
            String NpsdtlSeqNo = inner.message.substring(start + 22, start + 32);
            String NpsdtlTerminalid = inner.message.substring(start + 37, start + 45);
            String NpsdtlBusinessUnit = inner.message.substring(start + 51, start + 59);
            String NpsdtlPaytype = inner.message.substring(start + 59, start + 64);
            String NpsdtlPayno = inner.message.substring(start + 64, start + 68);
            String NpsdtlTxAmt = inner.message.substring(start + 68, start + 79);
            String NpsdtlIdno = inner.message.substring(start + 79, start + 90);
            String NpsdtlNppayno = inner.message.substring(start + 90, start + 106);
            String NpsdtlTroutBkno7 = inner.message.substring(start + 106, start + 113);
            String NpsdtlTroutBkno = inner.message.substring(start + 106, start + 109);
            String NpsdtlTroutActno = inner.message.substring(start + 113, start + 129);
            String NpsdtlTrinBkno = inner.message.substring(start + 129, start + 132);
            String NpsdtlTrinActno = inner.message.substring(start + 132, start + 148);
            String NpsdtlAllowT1 = inner.message.substring(start + 148, start + 149); //允許次日帳
            String NpsdtlDueDate = inner.message.substring(start + 149, start + 157); //繳費期限
            //寫入NPSDTL
            defNpsdtl.setNpsdtlBatNo(NpsdtlBatchNo);
            defNpsdtl.setNpsdtlSeqNo(Integer.valueOf(NpsdtlSeqNo));
            defNpsdtl.setNpsdtlTroutBkno(NpsdtlTroutBkno);
            defNpsdtl.setNpsdtlTroutActno(NpsdtlTroutActno);
            defNpsdtl.setNpsdtlTroutBkno7(StringUtils.rightPad(NpsdtlTroutBkno7.trim(), 7, "0"));
            defNpsdtl.setNpsdtlTrinBkno(NpsdtlTrinBkno);
            defNpsdtl.setNpsdtlTrinActno(NpsdtlTrinActno);
            defNpsdtl.setNpsdtlAllowT1(NpsdtlAllowT1);
            defNpsdtl.setNpsdtlDueDate(NpsdtlDueDate);
            defNpsdtl.setNpsdtlTxAmt(BigDecimal.valueOf(Double.valueOf(NpsdtlTxAmt) / 100));
            defNpsdtl.setNpsdtlBusinessUnit(NpsdtlBusinessUnit);
            defNpsdtl.setNpsdtlPaytype(NpsdtlPaytype);
            defNpsdtl.setNpsdtlPayno(NpsdtlPayno);
            defNpsdtl.setNpsdtlIdno(NpsdtlIdno);
            defNpsdtl.setNpsdtlReconSeq(NpsdtlNppayno);
            defNpsdtl.setNpsdtlTerminalid(NpsdtlTerminalid);
            defNpsdtl.setNpsdtl02Tita(tita2); //紀錄明細錄電文(組結果檔用)

            String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
            defNpsdtl.setNpsdtlBkno(sysstatHbkno);
            defNpsdtl.setNpsdtlThreadNo(recordIndex % inner.maxThreads);

            // 判斷跨行/自行 PCODE [cite: 376-397]
            if (sysstatHbkno.equals(NpsdtlTroutBkno) && !sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2261"); defNpsdtl.setNpsdtlTxCode("ED");
            } else if (!sysstatHbkno.equals(NpsdtlTroutBkno) && sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2262"); defNpsdtl.setNpsdtlTxCode("EW");
            } else if (sysstatHbkno.equals(NpsdtlTroutBkno) && sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2263"); defNpsdtl.setNpsdtlTxCode("EA");
            } else if (!sysstatHbkno.equals(NpsdtlTroutBkno) && !sysstatHbkno.equals(NpsdtlTrinBkno) && NpsdtlTroutBkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2263"); defNpsdtl.setNpsdtlTxCode("EA");
            } else {
                defNpsdtl.setNpsdtlPcode("2264"); defNpsdtl.setNpsdtlTxCode("ER");
            }

            if (npsdtlExtMapper.insertSelective(defNpsdtl) < 1) {
                inner.W_ERR_MSG = "INSERT NPSDTL Error";
                inner.W_ERR_CODE = "FXXX";
                writLog(logData, "", "電文檢核成功，新增NPSDTL失敗：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".InsertNPSDTL");

                ACKResponse(logData, inner, message);
                return false;
            }
//            writLog(logData, "", "npsdtlExtMapper insert done.", ".InsertNPSDTL");
            return true;
        } catch (Exception e) {
            logData.setMessage(e.getMessage());
            logData.setRemark("InsertNPSDTL發生例外!");
            logData.setProgramName("InsertNPSDTL");
            this.logMessage(logData);
            sendEMS(logData);
        }
        return false;
    }

    /**
     * 5.逐筆讀取 NPSDTL,產生單筆Request電文, 寫入MQ(VIP)或MQ(NONVIP)
     */
    private boolean processSingleRequestMessages(LogData logData, InnerClass inner, Message message, String filename, String txDateAD, String batchNo, String branch, String messageIn) throws Exception {
        Npsbatch npsbatch = npsbatchExtMapper.selectNpsbatchWithNpsdtlbyVip(filename, txDateAD, batchNo, branch);
        if (npsbatch == null) {
            // 找不到資料，回應錯誤並結束
            inner.W_ERR_MSG = "產生單筆Request電文時NPSDTL DATA not Found";
            inner.W_ERR_CODE = "FXXX";
            ACKResponse(logData, inner, message);
            return false;
        }
        List<Npsdtl> dtlList = npsdtlExtMapper.GetNPSDTLByBATNOforAll(inner.npsdtlBatNo);
        if (dtlList == null || dtlList.isEmpty()) {
            inner.W_ERR_MSG = "產生單筆Request電文時NPSDTL DATA not Found";
            inner.W_ERR_CODE = "FXXX";
            ACKResponse(logData, inner, message);
            return false;
        }

        //判斷NPSBATCH. NPSBATCH_VIP是否為VIP/NONVIP往下述MQ通道傳送電文
        //VIP ：NPAY.FEP.VIP.RQ
        //NONVIP ：NPAY.FEP.NONVIP.RQ
        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        String sysstatTbsdyFisc = SysStatus.getPropertyValue().getSysstatTbsdyFisc();

        for (Npsdtl dtl : dtlList) { //累計交易總筆數及金額
            // 檢核是否允許次日帳
            if (!txDateAD.equals(sysstatTbsdyFisc) && "N".equals(dtl.getNpsdtlAllowT1())) { //不允許次日帳
                dtl.setNpsdtlResult("01");
                if(dtl.getNpsdtlTroutBkno().equals(sysstatHbkno) && dtl.getNpsdtlTrinBkno().equals(sysstatHbkno)){ //自行
                    dtl.setNpsdtlCbsRc("P0X");
                    dtl.setNpsdtlReplyCode("2999");
                }else{
                    dtl.setNpsdtlCbsRc("P01");
                    dtl.setNpsdtlReplyCode("2999");
                }
                dtl.setNpsdtlErrMsg("財金已換日，不允許逾時交易");
                npsdtlExtMapper.updateByPrimaryKeySelective(dtl);
                continue; //繼續處理下一筆交易
            }

            //組 NPAY電文, 並送系統設定之MQ執行交易
            //Class  RCV_NB_GeneralTrans_RQ
            RCV_NB_GeneralTrans_RQ rq = newXml();
            RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = rq.getBody().getRq().getHeader();
            RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq svcRq = rq.getBody().getRq().getSvcRq();

            String seqNoStr = StringUtils.leftPad(String.valueOf(dtl.getNpsdtlSeqNo()), 10, "0");
            header.setCLIENTTRACEID("NAMNB" + inner.npsdtlBatNo + seqNoStr);
            header.setCHANNEL("NAM");

            String msgid = dtl.getNpsdtlTxCode().trim() + dtl.getNpsdtlPcode();
            if (dtl.getNpsdtlPcode().endsWith("3") && dtl.getNpsdtlTroutBkno().equals(sysstatHbkno)) {
                msgid += "0";
            }
            header.setMSGID(msgid);
            header.setMSGKIND("G");
            header.setTXNID("1".equals(npsbatch.getNpsbatchVip()) ? "FEPVIPTEL" : "FEPNONVIPTEL");
            header.setBRANCHID(npsbatch.getNpsbatchBranch());
            header.setCLIENTDT(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS_SSS));

            svcRq.setINDATE(npsbatch.getNpsbatchTxDate());
            svcRq.setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            svcRq.setIPADDR("");
            svcRq.setTXNTYPE("RQ"); //入扣帳
            svcRq.setTERMINALID(dtl.getNpsdtlTerminalid());
            svcRq.setTERMINAL_TYPE("6588");
            svcRq.setTERMINAL_CHECKNO("00000000");
            svcRq.setFSCODE(dtl.getNpsdtlTxCode().trim());
            svcRq.setPCODE(dtl.getNpsdtlPcode());
            svcRq.setTRNSFLAG("2");
            svcRq.setBUSINESSTYPE("A");
            svcRq.setTRANSAMT(dtl.getNpsdtlTxAmt());
            svcRq.setTRANBRANCH(StringUtils.right(dtl.getNpsdtlTroutBkno7(), 4));

            if (StringUtils.containsAny(msgid, "2261", "2264")) {
                svcRq.setTRNSFROUTIDNO(dtl.getNpsdtlIdno());
            } else {
                svcRq.setTRNSFROUTIDNO("");
            }

            svcRq.setTRNSFROUTBANK(dtl.getNpsdtlTroutBkno7());
            svcRq.setTRNSFROUTACCNT(dtl.getNpsdtlTroutActno());
            svcRq.setTRNSFRINBANK(dtl.getNpsdtlTrinBkno() + "0000");
            svcRq.setTRNSFRINACCNT(dtl.getNpsdtlTrinActno());

            svcRq.setCUSTPAYFEE(BigDecimal.ZERO);
            svcRq.setFISCFEE(BigDecimal.ZERO);
            svcRq.setCHAFEE_AMT(BigDecimal.ZERO);
            svcRq.setFAXFEE(BigDecimal.ZERO);
            svcRq.setTRANSFEE(BigDecimal.ZERO);
            svcRq.setOTHERBANKFEE(BigDecimal.ZERO);
            svcRq.setSSLTYPE("N");

            svcRq.setTRNSFRINIDNO("");
            svcRq.setFEEPAYMENTTYPE("");
            svcRq.setCHAFEE_BRANCH("");
            svcRq.setTRNSFRINNOTE("");
            svcRq.setTRNSFROUTNOTE("");
            svcRq.setORITXSTAN("");
            svcRq.setLIMITTYPE("");
            svcRq.setTRANSTYPEFLAG("");
            svcRq.setTEXTMARK("");
            svcRq.setCUSTCODE("");
            svcRq.setAFFAIRSCODE("");
            svcRq.setCHAFEE_TYPE("");
            svcRq.setTRNSFROUTNAME("");

            svcRq.getPAYDATA().setNPOPID(dtl.getNpsdtlBusinessUnit());
            svcRq.getPAYDATA().setNPPAYTYPE(dtl.getNpsdtlPaytype());
            svcRq.getPAYDATA().setNPFEENO(dtl.getNpsdtlPayno());
            svcRq.getPAYDATA().setNPID(dtl.getNpsdtlIdno());
            svcRq.getPAYDATA().setNPPAYNO(dtl.getNpsdtlReconSeq());
            svcRq.getPAYDATA().setNPPAYENDDATE(dtl.getNpsdtlDueDate());
            svcRq.getPAYDATA().setNPBRANCH(npsbatch.getNpsbatchBranch());

            svcRq.getPAYDATA().setPAYCATEGORY("");
            svcRq.getPAYDATA().setPAYNO("");
            svcRq.getPAYDATA().setPAYENDDATE("");
            svcRq.getPAYDATA().setORGAN("");
            svcRq.getPAYDATA().setCID("");
            svcRq.getPAYDATA().setFILLER("");

            String xmlMessage = "";
            xmlMessage = XmlUtil.toXML(rq); // NPAY電文轉成XML 格式

            if ("1".equals(inner.getVip())) {
                SendQueueToVIPServerReceiver(logData, xmlMessage, message); // 送 VIP Q
            } else {
                SendQueueToNONVIPServerReceiver(logData, xmlMessage, message); // 送 NONVIP Q
            }
        }

        // 更新 NPSBATCH 狀態
        npsbatch.setNpsbatchResult("00"); //交易發送完成
        npsbatch.setNpsbatch09Tita(inner.npsbatch.getNpsbatch09Tita()); //紀錄尾筆電文(組結果檔用)
        if (npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch) < 1) {
            inner.W_ERR_MSG = " 產生單筆交易完成UPDATE NPSBATCH_RESULT Error";
            inner.W_ERR_CODE = "FXXX";

            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setRemark(StringUtils.join( inner.W_ERR_CODE , ":" , inner.W_ERR_MSG));
            logMessage(logData);
            sendEMS(logData);

            ACKResponse(logData, inner, message);
            return false;
        }
        return true;
    }

    private RCV_NB_GeneralTrans_RQ newXml() {
        RCV_NB_GeneralTrans_RQ nb = new RCV_NB_GeneralTrans_RQ();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Header rsheader = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Header();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body rsbody = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq msg = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header header = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq body = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA pay = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_PAYDATA();
        msg.setHeader(header);
        body.setPAYDATA(pay);
        msg.setSvcRq(body);
        rsbody.setRq(msg);
        nb.setBody(rsbody);
        nb.setHeader(rsheader);
        return nb;
    }
}
