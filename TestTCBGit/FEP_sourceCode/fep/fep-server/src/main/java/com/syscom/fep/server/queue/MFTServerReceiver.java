package com.syscom.fep.server.queue;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
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
import com.syscom.fep.jms.instance.map.mft.MFTQueueConfiguration;
import com.syscom.fep.jms.instance.map.mft.MFTQueueConfigurationProperties;
import com.syscom.fep.jms.queue.MFTQueueConsumers;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.handler.MFTHandler;
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
import java.util.Map;

@StackTracePointCut(caller = SvrConst.SVR_MFT)
public class MFTServerReceiver extends FEPBase implements JmsReceiver<String> {
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);
    public static final MFTQueueConfiguration MFT_QUEUE_CONFIGURATION = SpringBeanFactoryUtil.getBean(MFTQueueConfiguration.class);

    private NBData nbData;
    @PostConstruct
    public void initialization() {
        JmsMonitorController.addNotifier(ProgramName, SpringBeanFactoryUtil.registerBean(MFTQueueConsumers.class).subscribe(this));
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
        LogHelperFactory.getTraceLogger().trace(SvrConst.SVR_MFT, " Recv msg:", messageIn);
        String messageOut = StringUtils.EMPTY;
        LogData logData = new LogData();
        logData.setEj(TxHelper.generateEj());
        writLog(logData, "destination:(" + destination + "),payload:(" + payload + ")", "MFTReceiver", ".messageReceived");

        InnerClass inner = new InnerClass();
        inner.message = payload;
        inner.maxThreads = CMNConfig.getInstance().getSECMaxThreads();
        inner.W_EJNO = logData.getEj();

        Boolean result = false;
        try {
            //檢核檔名，電文中前29碼為檔名
            String tita = messageIn.substring(0, 29);
            String filename = tita.substring(0, 8).trim(); //檔案名稱

            if (StringUtils.equalsAny(filename, "PUIPOS", "PUJPMPS")) {
                filename = filename.substring(0,filename.length() - 1); //查詢電文，拿掉最後一碼S
                String txDate = tita.substring(8, 15); //批號前7碼 EX. 1121222000200
                String txDateAD = String.valueOf(Integer.parseInt(payload.substring(8, 15)) + 19110000);
                String sysDateAD = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                String batchNo = tita.substring(8, 21);
                inner.npsdtlBatNo = StringUtils.rightPad(filename,8," ") + txDateAD + batchNo;//不含S
                inner.fileid = filename;
                inner.txdate = txDate;
                inner.batchNo = batchNo;
                //查詢批次處理結果電文
                Npsbatch checkNpsbatch = npsbatchExtMapper.selectByPrimaryKey(filename, txDateAD, batchNo);
                if (checkNpsbatch == null) {
                    inner.W_ERR_CODE = "FXXX";
                    inner.W_ERR_MSG = "=查詢失敗";
                    writLog(logData, "", "查無批號資料：" + tita + "," + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                    // 執行 6 組ACK回應電文
                    ACKResponse(logData, inner, message);
                } else {
                    writLog(logData, "","開始組批號:" + inner.npsdtlBatNo + "結果檔.", ".messageReceived");
                    inner.npsbatch = checkNpsbatch;
                    // 執行 7 組結果檔回覆前端(未執行的交易視為失敗交易)
                    waitAndBuildResponseData(logData, inner, message, filename, txDateAD, txDate, batchNo, tita);

                }
            } else if (StringUtils.equalsAny(filename, "AMPAYFL", "PUIPO", "PUJPMP")) {
                String txDate = tita.substring(8, 15); //批號前7碼 EX. 1121222000200
                String txDateAD = String.valueOf(Integer.parseInt(payload.substring(8, 15)) + 19110000);
                String sysDateAD = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
                String batchNo = tita.substring(8, 21);
                inner.branch = messageIn.substring(29 + 22, 29 + 26); //首筆放行分行
                inner.npsdtlBatNo = tita.substring(0, 8) + txDateAD + batchNo;
                //交易電文
                inner.W_REC_LEN = tita.substring(21, 24); //扣款檔 180
                inner.W_REC_COUNT = StringUtils.leftPad(tita.substring(24, 29).trim(), 5, "0"); //傳送筆數
                // 擷取 VIP 註記 (首筆資料的第 29 位，對應字串位置 57)
                inner.setVip(inner.message.substring(57, 58));

                inner.fileid = filename;
                inner.txdate = txDateAD;
                inner.batchNo = batchNo;
                //檢核交易日期
                if (!StringUtils.equals(txDateAD, sysDateAD)) {
                    inner.W_ERR_CODE = "K002";
                    inner.W_ERR_MSG = "交易日期非本日";
                    writLog(logData, "", "電文檔名檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                    ACKResponse(logData, inner, message);
                    return; // 結束處理
                }
                //查詢收檔紀錄
                // 批號狀態：00:交易發送完成，01:整批剔退，02:已回結果檔
                Npsbatch npsbatch = npsbatchExtMapper.selectByPrimaryKey(filename, txDateAD, batchNo);
                if (npsbatch != null) {
                    // 有紀錄，依狀態直接回覆，不再往下執行
                    if ("00".equals(npsbatch.getNpsbatchResult())) {
                        inner.W_ERR_CODE = "K008";
                        inner.W_ERR_MSG = "該批交易已發送處理完成，交易處理中覆";
                    } else if ("02".equals(npsbatch.getNpsbatchResult())) {
                        inner.W_ERR_CODE = "K007";
                        inner.W_ERR_MSG = "該批交易處理完成，已回覆結果檔";
                    } else if (StringUtils.isBlank(npsbatch.getNpsbatchResult())) {
                        inner.W_ERR_CODE = "K008";
                        inner.W_ERR_MSG = "該批資料檢核中，尚未發送交易";
                    } else if ("01".equals(npsbatch.getNpsbatchResult())) {
                        inner.W_ERR_CODE = "K006";
                        inner.W_ERR_MSG = "該批資料檢核失敗，且已回覆";
                    }

                    writLog(logData, "", "電文檔名檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");

                    ACKResponse(logData, inner, message);
                    return; // 結束處理
                }

                // 2.首筆檢核並寫入 NPSBATCH
                result = insertNpsbatch(logData, inner, message);
                if (!result) {
                    return;
                }

                // 3.檢核明細錄及尾筆
                result = check(logData, inner, message);
                if (!result) { // 檢核錯誤，更新NPSBATCH(01)
                    updateNpsbatch01(logData, inner, message, inner.W_ERR_MSG);
//                    ACKResponse(logData, inner, message);
                    return;
                } else {
                    // 檢核成功，更新 NPSBATCH
                    inner.npsbatch.setNpsbatchTotCnt(inner.W_SEQ_NO);
                    inner.npsbatch.setNpsbatchTotAmt(inner.W_TOT_TX_AMT.divide(new BigDecimal(100)));
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
                boolean isInsertDetailSuccess = true;
                int currentPos = 29; // 略過檔頭 29 bytes
                int recLen = Integer.valueOf(inner.W_REC_LEN);
                int recordIndex = 0; // 計算 ThreadNo 分流
                int insertNPSDTLCount = 0;

                while (currentPos + recLen <= inner.message.length()) {
                    String recType = inner.message.substring(currentPos, currentPos + 2);

                    if ("02".equals(recType)) {
                        //讀取明細錄”02”資料寫入NPSDTL
                        if (!InsertNPSDTL(logData, currentPos, recordIndex, inner, message)) {
                            isInsertDetailSuccess = false;
                            break; // 新增失敗跳出迴圈
                        }
                        insertNPSDTLCount++;
                        if(insertNPSDTLCount == inner.W_SEQ_NO){
                            String logRemark = "批號:" + tita.substring(8, 21) + "明細 npsdtl insert all done. 筆數共" + insertNPSDTLCount + "筆.";
                            writLog(logData, "", logRemark, ".messageReceived");
                        }
                    } else if ("09".equals(recType)) {
                        String tita9 = inner.message.substring(currentPos, currentPos + recLen);
                        inner.npsbatch.setNpsbatch09Tita(tita9);
                        break; // 讀到尾筆，代表明細寫入完畢，結束迴圈
                    }

                    currentPos += recLen;
                    recordIndex++;
                }

                if (!isInsertDetailSuccess) return;

                // 5. 逐筆讀取 NPSDTL,產生單筆Request電文, 寫入MQ(VIP)或MQ(NONVIP)
                if (!processSingleRequestMessages(logData, inner, message, filename, txDateAD, batchNo, inner.branch, messageIn)) {
                    return;
                }

                //  6. 組ACK回應電文(檢核錯誤時執行)
                ACKResponse(logData, inner, message);

                // 7. 組結果檔回覆前端(未執行的交易視為失敗交易) 主流程不做
//                waitAndBuildResponseData(logData, inner, message, filename, txDateAD, txDate, batchNo, tita);

            } else {
                //回應非整批轉即時電文，透過Handler Call AA：MFTNotice (做5102結帳(IBAF5102開頭電文) / 啟動收檔批次通知(IBAFRCV1開頭電文))
                MFTHandler mftHandler = new MFTHandler();
                mftHandler.setEj(logData.getEj());
                mftHandler.setLogContext(logData);
                messageOut = mftHandler.dispatch(FEPChannel.MFT, messageIn);
                logData.setProgramName(StringUtils.join(ProgramName, ".processResponseData"));
                logData.setProgramFlowType(ProgramFlow.MFTGWOut);
                logData.setMessageFlowType(MessageFlow.Response);
                logData.setMessage(messageOut);
                logData.setRemark("MFTService Receive Response");
                this.logMessage(logData);
                //20260428 基本上目前mftHandler完成之後回傳的messageOut都會是""，所以沒有機會走到 FeeBackSuccess，但還是保留程式
                if (StringUtils.isNotBlank(messageOut)) {
                    FeeBackSuccess(logData, messageOut, message);
                }
            }

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
        return SvrConst.SVR_MFT;
    }

    private boolean FeeBackSuccess(LogData logData, String returnStr, Message messageIn) {
        try {
//            JmsMsgConfiguration configuration = SpringBeanFactoryUtil.getBean(JmsMsgConfiguration.class);
//            JmsMsgSimpleOperator sender = SpringBeanFactoryUtil.getBean(JmsMsgSimpleOperator.class);
//            sender.sendQueue(configuration.getQueueNames().getMftAck().getDestination(), returnStr, null, null);
            String replyTo = JmsFactory.getReplyTo(messageIn);
            MFTQueueConfiguration mftQueueConfiguration = SpringBeanFactoryUtil.getBean(MFTQueueConfiguration.class);
            MapQueueOperator<?, ?> operator = null;

            if (StringUtils.isNotBlank(replyTo)) {
                operator = mftQueueConfiguration.getMapQueueOperator(replyTo);
            } else {
                // 沒有 ReplyTo 時使用預設值
                MFTQueueConfigurationProperties configurationProperties = mftQueueConfiguration.getProp().get(0);
                operator = mftQueueConfiguration.getMapQueueOperator(configurationProperties.getKey());
                replyTo = StringUtils.isNotBlank(configurationProperties.getReplyTo())
                        ? configurationProperties.getReplyTo()
                        : configurationProperties.getKey();
            }

            if (operator != null) {
                JmsDefinition ackDefinition = mftQueueConfiguration.getMftAck(replyTo);
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
                                JmsFactory.setCharacterSet(messageOut, 937);
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
            // sendEMS
            logData.setProgramName(StringUtils.join(ProgramName, ".sendQueue"));
            logData.setRemark(StringUtils.join("Send Ack Queue with Exception occur, ", ex.getMessage()));
            logData.setProgramException(ex);
            sendEMS(logData);
            return false;
        }
    }

    public static class InnerClass {
        private String W_ERR_CODE;
        private String W_ERR_MSG;
        private String W_REC_LEN;
        private int W_EJNO;
        private BigDecimal W_TOT_TX_AMT = BigDecimal.ZERO;
        private String W_REC_COUNT;
        private int Failcnt = 0;
        private int doTOTcnt = 0;
        private BigDecimal failamt = BigDecimal.ZERO;
        private BigDecimal doTOTamt = BigDecimal.ZERO;
        private int okcnt = 0;
        private int feecnt = 0;
        private BigDecimal okamt = BigDecimal.ZERO;
        private BigDecimal feeamt = BigDecimal.ZERO;
        private Integer W_SEQ_NO = 0;
        private String message; //完整電文
        private Npsbatch npsbatch = new Npsbatch();
//        private Npsdtl npsdtl = new Npsdtl();
        private Zone zone;
        private String fileid;
        private String txdate;
        private String batchNo;
        private String npsdtlBatNo; //NPSDTL_BAT_NO -> 檔名(8)西元日期(8)批次號碼(13)
        private Integer maxThreads;
        private String vip;
        private String branch;

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

    private void writLog(LogData logData, String msg, String mark, String progName) {
        logData.setMessage(msg);
        logData.setProgramName(StringUtils.join(ProgramName, progName));
        logData.setProgramFlowType(ProgramFlow.RESTFulIn);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setRemark(mark);
        this.logMessage(logData);
    }

    public void ACKResponse(LogData logData, InnerClass inner, Message messageIn) throws Exception {
//        inner.zone = zoneExtMapper.selectByPrimaryKey("TWN");
        String REPLY_FILE_ID = StringUtils.rightPad(inner.message.substring(0, 7), 7, " ") +"R"; //(1)MQ傳送檔名
        String REPLY_BATCH_NO = StringUtils.rightPad(inner.message.substring(8, 21), 13, " "); //(2)檔案批號
        String REPLY_REC_LEN = "100"; //(3)MQ傳送檔REC-LEN
        String REPLY_REC_CNT = "00001"; //(4)傳送筆數
        String FILLER1 = "99"; //(5)RECORD-TYPE

        // ：REPLY_KEY_1 帶入收到的 TITA.TX_DATE
        String REPLY_KEY_1 = StringUtils.rightPad(inner.message.substring(8, 15), 7, " "); //(6)交易日期
        String REPLY_KEY_2 = StringUtils.rightPad(inner.message.substring(8, 21), 13, " "); //(7)批次號碼
        String ERR_CODE = StringUtils.rightPad(StringUtils.defaultString(inner.W_ERR_CODE), 4, " "); //(8)錯誤代碼
        String msg = StringUtils.isBlank(inner.W_ERR_MSG) ? "" : inner.W_ERR_MSG; //(9)錯誤代碼說明

        /**因為MFT傳送中文會導致亂碼 先回傳空白 20250114**/
//        String ERR_MSG = StringUtils.rightPad("", 60, " ");
        String ERR_MSG = StringUtils.rightPad(msg, 60, " ");
        String FILLER2 = StringUtils.rightPad("", 14, " ");
        String ack = REPLY_FILE_ID + REPLY_BATCH_NO + REPLY_REC_LEN + REPLY_REC_CNT + FILLER1 + REPLY_KEY_1 + REPLY_KEY_2
                + ERR_CODE + ERR_MSG + FILLER2;

        logData.setProgramName(StringUtils.join(ProgramName, ".ACKResponse"));
        logData.setMessage("W_ERR_CODE='" + ERR_CODE + "',ERR_MSG='" + ERR_MSG + "',REPLY_KEY_1='" + REPLY_KEY_1 + "',REPLY_KEY_2='" + REPLY_KEY_2 + "',ACK='" + ack + "'");
        this.logMessage(logData);
        MapQueueOperator<?, ?> operator = null;
        String replyTo = JmsFactory.getReplyTo(messageIn);
        MFTQueueConfiguration mftQueueConfiguration = SpringBeanFactoryUtil.getBean(MFTQueueConfiguration.class);
        if (StringUtils.isNotBlank(replyTo)) {
            operator = mftQueueConfiguration.getMapQueueOperator(replyTo);
        } else {
            // 如果replyTo是空值.則拿第一組為default by Jack
            MFTQueueConfigurationProperties configurationProperties = mftQueueConfiguration.getProp().get(0);
            operator = mftQueueConfiguration.getMapQueueOperator(configurationProperties.getKey());
//            replyTo = configurationProperties.getReplyTo();
            replyTo = StringUtils.isNotBlank(configurationProperties.getReplyTo())
                    ? configurationProperties.getReplyTo()
                    : configurationProperties.getKey();
        }
        if (operator != null) {
            JmsDefinition ackDefinition = mftQueueConfiguration.getMftAck(replyTo);
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
                            JmsFactory.setCharacterSet(messageOut, 937);
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
        writLog(logData, inner.toString(), inner.npsdtlBatNo +"-ACKResponse done.", ".ACKResponse");
    }

    /**
     * INSERT NPSBATCH
     *
     * @param logData
     * @param inner
     * @param message
     * @return
     */
    public boolean insertNpsbatch(LogData logData, InnerClass inner, Message message) {
        try {
            // 首筆資料起點 (MQ檔頭佔 29 bytes)
            String tita1 = inner.message.substring(29, 209);  //讀取第一筆(首筆”01”)資料
            String recType = tita1.substring(0, 2); //首筆”01”
            String txDateROC = tita1.substring(2, 9); //交易日期
            String batchNo = tita1.substring(9, 22); //批次號碼
//            String branch = tita1.substring(22, 26); //放行分行
            String VIP = inner.getVip(); //VIP註記

            // 1. 首筆資料檢核
            if (!"01".equals(recType)) {
                inner.W_ERR_CODE = "K001";
                inner.W_ERR_MSG = "REC TYPE ERROR";
            } else if (!inner.txdate.equals(CalendarUtil.rocStringToADString(txDateROC))) {
                inner.W_ERR_CODE = "K002";
                inner.W_ERR_MSG = "首筆中的交易日與檔案日期不符";
            } else if (!inner.batchNo.equals(batchNo)) {
                inner.W_ERR_CODE = "K003";
                inner.W_ERR_MSG = "首筆中的批次號碼與檔案批號不符";
            } else if (StringUtils.isBlank(inner.branch) || "0000".equals(inner.branch)) {
                inner.W_ERR_CODE = "K004";
                inner.W_ERR_MSG = "首筆中的放行分行為空白或0000";
            }

            // 2. 準備寫入 NPSBATCH 的實體
            inner.npsbatch = new Npsbatch();
            inner.npsbatch.setNpsbatchFileId(inner.fileid);
            inner.npsbatch.setNpsbatchTxDate(inner.txdate);
            inner.npsbatch.setNpsbatchBatchNo(inner.batchNo);
            inner.npsbatch.setNpsbatchBranch(inner.branch);
            inner.npsbatch.setNpsbatchRcvTime(new Date()); //(HHMMDD)
            inner.npsbatch.setNpsbatchVip(VIP);
            String replyTo = JmsFactory.getReplyTo(message);
            inner.npsbatch.setNpsbatchRtq(replyTo);
            String CorrelationID = JmsFactory.getCorrelationID(message);
            inner.npsbatch.setNpsbatchCorrelId(CorrelationID);
            inner.npsbatch.setNpsbatchHeadMemo(tita1.trim());

            //首筆檢核錯誤
            if (StringUtils.isNotBlank(inner.W_ERR_CODE)) {
                inner.npsbatch.setNpsbatchResult("01"); //整批剔退
            }
            inner.npsbatch.setNpsbatchRspEjfno(inner.W_EJNO);
            inner.npsbatch.setNpsbatch01Tita(tita1); //紀錄首筆電文(組結果檔用)
            // 3. 寫入資料庫
            if (npsbatchExtMapper.insertSelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = "INSERT NPSBATCH Error";
                inner.W_ERR_CODE = "FXXX";
                ACKResponse(logData, inner, message);
                return false;
            }

            writLog(logData, "", "npsbatchExtMapper insert done.", ".insertNpsbatch");

            // 執行 6 組ACK回應電文
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

    private Boolean check(LogData logData, InnerClass inner, Message message) {
        try {
            int recLen = Integer.valueOf(inner.W_REC_LEN);
            String headerTxDateROC = inner.message.substring(8, 15); // 檔頭交易日

            int currentPos = 29 + recLen; // 從第一筆明細的位置開始
            int recordIndex = 0; // 總行數計數 (首、明細、尾)
            int detailCount = 0; // 明細筆數紀錄
            boolean hasTrailer = false;

            inner.W_SEQ_NO = 0;
            inner.W_TOT_TX_AMT = BigDecimal.ZERO;

            // 動態依字串長度掃描，直到遇到尾筆為止
            while (currentPos + recLen <= inner.message.length()) {
                String recType = inner.message.substring(currentPos, currentPos + 2);

                String errorPrefix = "09".equals(recType) ? "尾筆" : "第" + (detailCount + 1) + "筆明細錄";

                // 交易日期檢核 (Spec: 2~9)
                String currentTxDateROC = inner.message.substring(currentPos + 2, currentPos + 9);
                if (!currentTxDateROC.equals(headerTxDateROC)) {
                    inner.W_ERR_MSG = errorPrefix + "交易日與檔案日期不符!";
                    inner.W_ERR_CODE = "K002";
                    ACKResponse(logData, inner, message);
                    return false;
                }

                // 批號錯誤檢核 (Spec: 9~22)
                if (!inner.message.substring(currentPos + 9, currentPos + 22).equals(inner.batchNo)) {
                    inner.W_ERR_MSG = errorPrefix + "批號錯誤!";
                    inner.W_ERR_CODE = "K003";
                    ACKResponse(logData, inner, message);
                    return false;
                }

                switch (recType) {
                    case "02": /* 明細資料 */
                        detailCount++;

                        //端末機代號(TxxxxNAM  (第1位=T , 後3位=“NAM”))
                        String termId = inner.message.substring(currentPos + 37, currentPos + 45); // Spec: 37~45
                        if (!termId.startsWith("T") || !termId.endsWith("NAM")) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的端末資料錯誤!";
                            inner.W_ERR_CODE = "K010";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //端末機代號(TxxxxNAM  (第2~5位 = 放行分行))
                        if (!inner.npsbatch.getNpsbatchBranch().equals(termId.substring(1, 5))) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中端末機代號分行代號與首筆不符!";
                            inner.W_ERR_CODE = "K011";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //委託單位代號(8)為空白或全為0
                        String bizUnit = inner.message.substring(currentPos + 51, currentPos + 59); // Spec: 51~59
                        if (StringUtils.isBlank(bizUnit) || "00000000".equals(bizUnit)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的委託單位代號錯誤!";
                            inner.W_ERR_CODE = "K012";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //繳費類別(5)為空白或全為0
                        String payType = inner.message.substring(currentPos + 59, currentPos + 64); // Spec: 59~64
                        if (StringUtils.isBlank(payType) || "00000".equals(payType)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的繳費類別錯誤!";
                            inner.W_ERR_CODE = "K012";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //費用代號(4)為空白或全為0
                        String payNo = inner.message.substring(currentPos + 64, currentPos + 68); // Spec: 64~68
                        if (StringUtils.isBlank(payNo) || "0000".equals(payNo)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的繳費代號錯誤!";
                            inner.W_ERR_CODE = "K012";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //交易金額非數字或為0
                        String txAmtStr = inner.message.substring(currentPos + 68, currentPos + 79); // Spec: 68~79
                        if (!PolyfillUtil.isNumeric(txAmtStr) || new BigDecimal(txAmtStr).compareTo(BigDecimal.ZERO) == 0) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的繳費金額錯誤!";
                            inner.W_ERR_CODE = "K013";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //身分證統一編號為空白或全為0
                        String idno = inner.message.substring(currentPos + 79, currentPos + 90).trim(); // Spec: 79~90
                        if (StringUtils.isBlank(idno) || idno.matches("^0+$")) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的身分證統一編號錯誤!";
                            inner.W_ERR_CODE = "K019";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //轉出行為空或為0
                        String troutBkno = inner.message.substring(currentPos + 106, currentPos + 113); // Spec: 106~113
                        if (StringUtils.isBlank(troutBkno) || "0000000".equals(troutBkno)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的轉出行錯誤!";
                            inner.W_ERR_CODE = "K014";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //轉出帳號為空或為0
                        String troutAccno = inner.message.substring(currentPos + 113, currentPos + 129); // Spec: 113~129
                        if (StringUtils.isBlank(troutAccno) || "0000000000000000".equals(troutAccno)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的轉出帳號錯誤!";
                            inner.W_ERR_CODE = "K014";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        //轉入行為空或為0
                        String trinBkno = inner.message.substring(currentPos + 129, currentPos + 132); // Spec: 129~132
                        if (StringUtils.isBlank(trinBkno) || "000".equals(trinBkno)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的轉入行錯誤!";
                            inner.W_ERR_CODE = "K015";
                            ACKResponse(logData, inner, message);
                            return false;
                        }


                        //交易上傳序號
                        String seqNoStr = inner.message.substring(currentPos + 22, currentPos + 32); // Spec: 22~32
                        if ((inner.W_SEQ_NO + 1) != Integer.parseInt(seqNoStr)) {
                            inner.W_ERR_MSG = "第" + detailCount + "筆明細錄中的序號未依序:";
                            inner.W_ERR_CODE = "K009";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        inner.W_SEQ_NO += 1;
                        inner.W_TOT_TX_AMT = inner.W_TOT_TX_AMT.add(new BigDecimal(txAmtStr));
                        break;

                    case "09": /* 尾筆 */
                        String totCntStr = inner.message.substring(currentPos + 22, currentPos + 28); // Spec: 22~28
                        if (inner.W_SEQ_NO != Integer.parseInt(totCntStr)) {
                            inner.W_ERR_MSG = "明細筆數與尾筆不符";
                            inner.W_ERR_CODE = "K016";
                            ACKResponse(logData, inner, message);
                            return false;
                        }

                        String totAmtStr = inner.message.substring(currentPos + 28, currentPos + 42); // Spec: 28~42
                        if (inner.W_TOT_TX_AMT.compareTo(new BigDecimal(totAmtStr)) != 0) {
                            inner.W_ERR_MSG = "明細金額與尾筆不符";
                            inner.W_ERR_CODE = "K017";
                            ACKResponse(logData, inner, message);
                            return false;
                        }
                        hasTrailer = true;
                        break;

                    default:
                        inner.W_ERR_MSG = "資料別錯誤";
                        inner.W_ERR_CODE = "K001";
                        ACKResponse(logData, inner, message);
                        return false;
                }

                // 已經讀到尾筆，就可以提早跳出迴圈
                if (hasTrailer) {
                    break;
                }

                currentPos += recLen;
                recordIndex++;
            }

            // 防呆：如果跑完整個檔案都沒看到 "09" 尾筆
            if (!hasTrailer) {
                inner.W_ERR_MSG = "檔案檢核失敗，找不到尾筆(09)資料";
                inner.W_ERR_CODE = "FXXX";
                ACKResponse(logData, inner, message);
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateNpsbatch01(LogData logData, InnerClass inner, Message message, String errMsg) {
        try {

            inner.npsbatch.setNpsbatchResult("01"); //整批剔退
            inner.npsbatch.setNpsbatchErrMsg(errMsg);

            if (npsbatchExtMapper.updateByPrimaryKeySelective(inner.npsbatch) < 1) {
                inner.W_ERR_MSG = "檢核明細錄UPDATE NPSBATCH Error";
                inner.W_ERR_CODE = "FXXX";
                writLog(logData, "", "電文明細錄或尾筆檢核錯誤：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".updateNpsbatch01");
                ACKResponse(logData, inner, message);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean InsertNPSDTL(LogData logData, int start, int recordIndex, InnerClass inner, Message message) {
        Npsdtl defNpsdtl = new Npsdtl();
        try {
            int recLen = Integer.parseInt(inner.W_REC_LEN);
            String tita2 = inner.message.substring(start, start + recLen);

            String NpsdtlTxdate = inner.message.substring(start + 2, start + 9); //交易日期
            //NPSDTL_BAT_NO -> 檔名(8)西元日期(8)批次號碼(13)
            String NpsdtlBatchNo = inner.npsdtlBatNo;
            String NpsdtlSeqNo = inner.message.substring(start + 22, start + 32); //交易上傳序號
            String NpsdtlTerminalid = inner.message.substring(start + 37, start + 45); //端末機代號
            String NpsdtlBusinessUnit = inner.message.substring(start + 51, start + 59); //委託單位代號
            String NpsdtlPaytype = inner.message.substring(start + 59, start + 64); //繳費類別
            String NpsdtlPayno = inner.message.substring(start + 64, start + 68); //費用代號
            String NpsdtlTxAmt = inner.message.substring(start + 68, start + 79); //交易金額
            String NpsdtlIdno = inner.message.substring(start + 79, start + 90); //身分證統一編號
            String NpsdtlNppayno = inner.message.substring(start + 90, start + 106); //銷帳編號
            String NpsdtlTroutBkno7 = inner.message.substring(start + 106, start + 113); //轉出銀行
            String NpsdtlTroutBkno = inner.message.substring(start + 106, start + 109); //轉出銀行(3碼)
            String NpsdtlTroutActno = inner.message.substring(start + 113, start + 129); //轉出帳號
            String NpsdtlTrinBkno = inner.message.substring(start + 129, start + 132); //轉入銀行
            String NpsdtlTrinActno = inner.message.substring(start + 132, start + 148); //轉入帳號

            String NpsdtlAllowT1 = inner.message.substring(start + 148, start + 149); //允許次日帳
            String NpsdtlDueDate = inner.message.substring(start + 149, start + 157); //繳費期限
//            String NpsdtlAtmno = inner.message.substring(start + 32, start + 37);
//            String NpsdtlTxTime = inner.message.substring(start + 45, start + 51);

            defNpsdtl.setNpsdtlBatNo(NpsdtlBatchNo);
            defNpsdtl.setNpsdtlSeqNo(Integer.valueOf(NpsdtlSeqNo));
            defNpsdtl.setNpsdtlTroutBkno(NpsdtlTroutBkno);
            defNpsdtl.setNpsdtlTroutActno(NpsdtlTroutActno);
            defNpsdtl.setNpsdtlTroutBkno7(NpsdtlTroutBkno7);
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

            if (sysstatHbkno.equals(NpsdtlTroutBkno) && !sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2261");
                defNpsdtl.setNpsdtlTxCode("ED");
            } else if (!sysstatHbkno.equals(NpsdtlTroutBkno) && sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2262");
                defNpsdtl.setNpsdtlTxCode("EW");
            } else if (sysstatHbkno.equals(NpsdtlTroutBkno) && sysstatHbkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2263");
                defNpsdtl.setNpsdtlTxCode("EA");
            } else if (!sysstatHbkno.equals(NpsdtlTroutBkno) && !sysstatHbkno.equals(NpsdtlTrinBkno) && NpsdtlTroutBkno.equals(NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2263");
                defNpsdtl.setNpsdtlTxCode("EA");
            } else if (!sysstatHbkno.equals(NpsdtlTroutBkno) && !sysstatHbkno.equals(NpsdtlTrinBkno) && !StringUtils.equals(NpsdtlTroutBkno, NpsdtlTrinBkno)) {
                defNpsdtl.setNpsdtlPcode("2264");
                defNpsdtl.setNpsdtlTxCode("ER");
            }
            if (npsdtlExtMapper.insertSelective(defNpsdtl) < 1) {
                inner.W_ERR_MSG = "INSERT NPSDTL Error";
                inner.W_ERR_CODE = "FXXX";
                writLog(logData, "", NpsdtlBatchNo + "電文檢核成功，新增NPSDTL失敗：" + inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".InsertNPSDTL");

                ACKResponse(logData, inner, message);
                return false;
            }
            //writLog(logData, "", "npsdtlExtMapper insert done.", ".InsertNPSDTL");
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

    // 組明細錄資料(第n筆明細錄)
    private void appendMessage12(StringBuilder message12, Npsdtl dtl, String sysstatHbkno) {
        String wTxtype = (sysstatHbkno.equals(dtl.getNpsdtlTroutBkno()) && sysstatHbkno.equals(dtl.getNpsdtlTrinBkno())) ? "CB2W" : "AB3W";

        String tbsdyROC = "";
        if (StringUtils.isNotBlank(dtl.getNpsdtlTbsdy()) && dtl.getNpsdtlTbsdy().length() >= 8) {
            tbsdyROC = CalendarUtil.adStringToROCString(dtl.getNpsdtlTbsdy());
        }
        tbsdyROC = StringUtils.leftPad(tbsdyROC, 7, "0");

        String hostChargeStr = dtl.getNpsdtlHostCharge() != null ? StringUtils.leftPad(dtl.getNpsdtlHostCharge().toString(), 2, "0") : "00";
        if (hostChargeStr.length() > 2) hostChargeStr = hostChargeStr.substring(hostChargeStr.length() - 2);

        // 取原明細的 178 bytes (避開前面的 "02")
        String originDtl = "";
        originDtl = dtl.getNpsdtl02Tita().substring(2); //db撈出來長度就是180

        StringBuilder m12 = new StringBuilder();
        m12.append("12");        // 0~2: RECORD-TYPE
        m12.append(originDtl);   // 2~180: TITA[3,178]
        m12.append(StringUtils.rightPad(dtl.getNpsdtlBkno() != null ? dtl.getNpsdtlBkno() : "", 3, " "));
        m12.append(StringUtils.rightPad(dtl.getNpsdtlStan() != null ? dtl.getNpsdtlStan() : "", 7, " "));
        m12.append(StringUtils.rightPad(dtl.getNpsdtlPcode() != null ? dtl.getNpsdtlPcode() : "", 4, " "));
        m12.append(StringUtils.rightPad(wTxtype, 4, " "));
        m12.append(StringUtils.leftPad(tbsdyROC, 7, "0"));

        m12.append(StringUtils.rightPad(dtl.getNpsdtlHostBrch() != null ? dtl.getNpsdtlHostBrch() : "", 4, " "));
        m12.append(StringUtils.rightPad(dtl.getNpsdtlCbsRc() != null ? dtl.getNpsdtlCbsRc() : "", 3, " "));
        m12.append(StringUtils.rightPad(dtl.getNpsdtlReplyCode() != null ? dtl.getNpsdtlReplyCode() : "", 4, " "));
        m12.append(StringUtils.leftPad(hostChargeStr, 2, "0"));
        m12.append(StringUtils.rightPad(dtl.getNpsdtlHostChargeFlag() != null ? dtl.getNpsdtlHostChargeFlag() : "", 1, " "));

        // m12 字串後補空白滿230位
        message12.append(StringUtils.rightPad(m12.toString(), 230, " "));
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
            header.setCLIENTTRACEID("NAMIM" + inner.npsdtlBatNo + seqNoStr);
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
            header.setTERMID("");

            svcRq.setINDATE(npsbatch.getNpsbatchTxDate());
            svcRq.setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            svcRq.setIPADDR("");
            svcRq.setTXNTYPE("RQ");
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
        inner.W_ERR_MSG = "整批逐筆發送完成";
        inner.W_ERR_CODE = "0000";
        return true;
    }

    /**
     * 7.組結果檔回覆前端(未執行的交易視為失敗交易)
     */
    private void waitAndBuildResponseData(LogData logData, InnerClass inner, Message message, String filename, String txDateAD, String txDate, String batchNo, String tita) throws Exception {
        // 主機查詢批號，依現狀回覆結果檔，未處理之交易視為失敗，更新NPSDTL_RESULT
        // 批號狀態：00:交易發送完成，01:整批剔退，02:已回結果檔
        Npsbatch npsbatchToUpdate = inner.npsbatch;
        List<Npsdtl> resultList = npsdtlExtMapper.GetNPSDTLByBATNOforAll(inner.npsdtlBatNo);

        if (resultList == null || resultList.isEmpty() || npsbatchToUpdate == null) {
            inner.W_ERR_MSG = "組結果檔電文時DATA not Found";
            inner.W_ERR_CODE = "FXXX";
            writLog(logData, "", inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".waitAndBuildResponseData");
            ACKResponse(logData, inner, message); // 執行 6 組ACK 回應電文
            return;
        }

        //檢核批號狀態，除01:整批剔退，其他狀態續組結果檔取第一筆資料
        if ("01".equals(npsbatchToUpdate.getNpsbatchResult())) {
            inner.W_ERR_MSG = "此批號資料檢核有誤，已整批剔退!!";
            inner.W_ERR_CODE = "FXXX";
            writLog(logData, "", inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".waitAndBuildResponseData");
            ACKResponse(logData, inner, message); // 執行 6 組ACK 回應電文
            return;
        }

        int wOkCnt = 0;
        BigDecimal wOkAmt = BigDecimal.ZERO;
        int wFailCnt = 0;
        BigDecimal wFailAmt = BigDecimal.ZERO;
        int wTotCnt = 0;
        BigDecimal wTotAmt = BigDecimal.ZERO;
        int wFeeCnt = 0;
        BigDecimal wFeeAmt = BigDecimal.ZERO;

        //組messageOut檔名：長度29
        String fileId = StringUtils.rightPad(StringUtils.trimToEmpty(npsbatchToUpdate.getNpsbatchFileId()), 8, ' ');
        String messageFile = fileId + npsbatchToUpdate.getNpsbatchBatchNo() + "230" + StringUtils.leftPad(String.valueOf(npsbatchToUpdate.getNpsbatchTotCnt()),5,"0");
        //組首筆
//        String headerTita = tita.substring(2, 29); // 從原始 tita 擷取偏移量 2~28 的字串
        String tita01 = inner.npsbatch.getNpsbatch01Tita().substring(2); //db撈出來長度就是180
        String message11 = StringUtils.rightPad("11" + tita01, 230, ' ');

        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        StringBuilder message12 = new StringBuilder();
        for (Npsdtl dtl : resultList) {
            //交易處理結果：'':單筆交易已發送，00:交易成功，01:交易失敗，11:停止交易
            if ("".equals(dtl.getNpsdtlResult())) { //已送出XML但尚未執行的交易
                dtl.setNpsdtlResult("11"); //停止交易
                dtl.setNpsdtlErrMsg("收到查詢電文，未處理之交易視為失敗");
                dtl.setNpsdtlCbsRc("XXX");
                dtl.setNpsdtlReplyCode("XXXX");
                npsdtlExtMapper.updateByPrimaryKeySelective(dtl);
            }
            //組明細錄資料(第n筆明細錄)
            appendMessage12(message12, dtl, sysstatHbkno);
            //累計交易總筆數及金額
            wTotCnt++;
            BigDecimal txAmt = dtl.getNpsdtlTxAmt() != null ? dtl.getNpsdtlTxAmt() : BigDecimal.ZERO;
            wTotAmt = wTotAmt.add(txAmt);

            if ("00".equals(dtl.getNpsdtlResult())) { //累計成功筆數及金額
                wOkCnt++;
                wOkAmt = wOkAmt.add(txAmt);
                BigDecimal fee = dtl.getNpsdtlFee() != null ? dtl.getNpsdtlFee() : BigDecimal.ZERO;
                if (fee.compareTo(BigDecimal.ZERO) > 0) {
                    wFeeCnt++;
                    wFeeAmt = wFeeAmt.add(fee);
                }
            } else { //累計失敗筆數及金額
                wFailCnt++;
                wFailAmt = wFailAmt.add(txAmt);
            }
        }

        //更新 NPSBATCH
        if (npsbatchToUpdate != null) {
            npsbatchToUpdate.setNpsbatchRspTime(new Date());
            npsbatchToUpdate.setNpsbatchResult("02"); //已回結果檔
            npsbatchToUpdate.setNpsbatchOkCnt(wOkCnt);
            npsbatchToUpdate.setNpsbatchOkAmt(wOkAmt);
            npsbatchToUpdate.setNpsbatchFailCnt(wFailCnt);
            npsbatchToUpdate.setNpsbatchFailAmt(wFailAmt);
            npsbatchToUpdate.setNpsbatchFeeCnt(wFeeCnt);
            npsbatchToUpdate.setNpsbatchFeeAmt(wFeeAmt);
            npsbatchToUpdate.setNpsbatchDoTotCnt(wTotCnt);
            npsbatchToUpdate.setNpsbatchDoTotAmt(wTotAmt);
            if (npsbatchExtMapper.updateByPrimaryKeySelective(npsbatchToUpdate) < 1) {
                inner.W_ERR_MSG = "組扣帳結果檔明細錄後 Update NPSBATCH Error: " + filename + batchNo;
                inner.W_ERR_CODE = "FXXX";
                writLog(logData, "", inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");
                ACKResponse(logData, inner, message);
                return;
            }
        } else {
            inner.W_ERR_MSG = "組扣帳結果檔時查無 NPSBATCH 資料";
            inner.W_ERR_CODE = "FXXX";
            writLog(logData, "", inner.W_ERR_CODE + ":" + inner.W_ERR_MSG, ".messageReceived");
            ACKResponse(logData, inner, message);
            return;
        }

        //組尾筆
        String message19 = "19" +
                txDate + //擋頭民國年交易日期
                StringUtils.rightPad(batchNo, 13, " ") +
                StringUtils.leftPad(String.valueOf(wTotCnt), 6, "0") +
                StringUtils.leftPad(String.valueOf(wTotAmt.multiply(new BigDecimal(100)).intValue()), 14, "0") +
                StringUtils.leftPad(String.valueOf(wOkCnt), 6, "0") +
                StringUtils.leftPad(String.valueOf(wOkAmt.multiply(new BigDecimal(100)).intValue()), 14, "0") +
                StringUtils.leftPad(String.valueOf(wFailCnt), 6, "0") +
                StringUtils.leftPad(String.valueOf(wFailAmt.multiply(new BigDecimal(100)).intValue()), 14, "0");
        message19 = StringUtils.rightPad(message19, 230, " "); // 補滿230

        String messageOut = messageFile + message11 + message12.toString() + message19;
        //寫 Response Log
        logData.setProgramFlowType(ProgramFlow.MFTGWOut);
        logData.setMessageFlowType(MessageFlow.Response);
        logData.setProgramName("MFTServerReceiver.processResponseData");
        logData.setRemark("MFTService Receive Response");
        logData.setMessage(messageOut);
        logMessage(logData);

        if (StringUtils.isNotBlank(messageOut)) {
            FeeBackSuccess(logData, messageOut, message);
        }

        // 回前端檢核成功發送扣款
        inner.W_ERR_MSG = "已回覆查詢電文結果檔";
        inner.W_ERR_CODE = "0000";
        writLog(logData, "", "批號" + inner.npsdtlBatNo + inner.W_ERR_MSG, ".messageReceived");

    }
}
