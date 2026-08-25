package com.syscom.fep.gateway.ims.cbs.sender;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.ims.IMSTransmissionConnState;
import com.syscom.fep.gateway.ims.IMSTransmissionProcessRequest;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayStatistic;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@StackTracePointCut(caller = SvrConst.SVR_CBS_GATEWAY)
public class CBSGatewayClientSenderProcessRequest
        extends IMSTransmissionProcessRequest<CBSGatewayClientSenderConfiguration> {
    private final AtomicReference<Exception> lockProcessException = new AtomicReference<>(null);
    private PingIMSTimer pingIMSTimer;
    private final AtomicBoolean lockSendToIMS = new AtomicBoolean(false);
    private AtomicReference<CBSGatewayStatistic> statistics;

    public void setStatistics(AtomicReference<CBSGatewayStatistic> statistics) {
        this.statistics = statistics;
    }

    /**
     * 接收連線狀態發生變化
     *
     * @param configuration
     * @param state
     * @param t
     */
    @Override
    public void connStateChanged(CBSGatewayClientSenderConfiguration configuration, IMSTransmissionConnState state, Throwable t) {
        super.connStateChanged(configuration, state, t);
        if (state == IMSTransmissionConnState.CLIENT_CONNECTED) {
            // sender連成功就先啟動計時pingIMS
            this.scheduleToPingIMS(0);
        } else if (state == IMSTransmissionConnState.CLIENT_DISCONNECTING) {
            // 開始斷線時, 要將timer停止
            unscheduleToPingIMS();
        }
    }

    /**
     * 處理業務邏輯
     *
     * @param logData
     * @param connection
     * @throws Exception
     */
    @Override
    public void doProcess(LogData logData, Connection connection) throws Exception {
        // Sender不需要接收電文, 所以這裡等待sendToIMS是否有catch到異常
        // 如果有catch到異常, 則sendToIMS中將異常塞入this.lock中並進行notify
        // 這裡從this.lock中取出來丟出去, 就可以重新建立連線
        synchronized (this.lockProcessException) {
            try {
                this.lockProcessException.wait();
            } catch (InterruptedException e) {
                LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
            }
        }
        Exception ex = this.lockProcessException.getAndSet(null); // 取出來之後記得要再塞入null, 避免下次又丟一次
        if (ex != null) {
            throw ex;
        }
    }

    /**
     * 送出主機電文(Sender)-SendToIMS(String inputData)
     *
     * @param logData
     * @param inputData
     * @throws Exception
     */
    public void sendToIMS(LogData logData, String inputData) throws Exception {
        synchronized (lockSendToIMS) {
            // 鎖定當前的method
            lockSendToIMS.set(true);
            try {
                if (!isConnected()) {
                    throw ExceptionUtil.createException("[WAIT_TO_RETRY]Cannot Send to IMS cause disconnected, ", this.configuration.forLogging());
                }
                // 送出前若有timer先停止定時ping計時
                unscheduleToPingIMS();
                TmInteraction myTmInteraction;

                myTmInteraction = this.connection.createInteraction();

                // SENDONLY
                //為避免送出沒收到所以改SENDONLYACK
                myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_SENDONLYACK);
                myTmInteraction.setLtermOverrideName(this.configuration.getClientId());
                myTmInteraction.setImsDatastoreName(this.configuration.getDataStore());
                myTmInteraction.setImsConnectTimeout(this.configuration.getImsConnectTimeout());
                myTmInteraction.setInteractionTimeout(this.configuration.getImsConnectTimeout() + ApiProperties.TIMEOUT_5_SECONDS);
                // logger.debug(ApiProperties.INTERACTION_TYPE_DESC_SENDONLY, " set Timeout as ", myTmInteraction.getImsConnectTimeout(), " ms");
                myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
                myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);
                myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
                String tCode = "";
                // 依電文的PCODE取得Trancode
                // 財金電文前3個byte改trancode
                if (StringUtils.isNotBlank(inputData) && inputData.substring(0, 6).equals("000000")) {
                    String pCode = StringUtils.substring(inputData, 14, 22);
                    pCode = EbcdicConverter.fromHex(CCSID.English, pCode);
                    tCode = this.getTransCode(pCode) + " ";
                    // Trancode = StringUtils.rightPad(tCode, 8, " ");
                    // 財金電文前面3個byte改為TranCode + 空白
                    inputData = EbcdicConverter.toHex(CCSID.English, tCode.length(), tCode) + inputData.substring(6);
                }

                myTmInteraction.setTrancode("");
                // get InputMessage instance from myTMInteraction
                InputMessage inMsg = myTmInteraction.getInputMessage();
                // populate input message data with indata byte array
                byte[] inputData_ = ConvertUtil.hexToBytes(inputData);

                inMsg.setInputMessageData(inputData_);

                // byte[] _inputData = inMsg.getDataAsByteArray();

                // String fileContent = "Hex=";
                // for (int i = 0; i < _inputData.length; i++) {
                // fileContent += String.format("%02x", _inputData[i]);
                // }

                logData.setMessage(inputData);
                logData.setRemark("before Send data to IMS (Trancode:" + tCode + ",clientId:" + this.configuration.getClientId() + ")");
                FEPBase.logMessage(Level.INFO, logData);

                // execute the transaction
                myTmInteraction.execute();

                // get output from myTMInteraction
                OutputMessage outMsg = myTmInteraction.getOutputMessage();
                // get data from outMsg as a string
                //String outData = outMsg.getDataAsString();
                String outData = StringUtil.toHex(outMsg.getDataAsByteArray());
                logData.setProgramName(StringUtils.join(ProgramName, ".SendToIMS"));
                logData.setRemark("Send data to IMS Succeed, return code = " + myTmInteraction.getOutputMessage().getImsConnectReturnCode() + ",clientId:" + this.configuration.getClientId());
                logData.setMessage(outData);
                FEPBase.logMessage(Level.INFO, logData);

                // 開始計時pingIMS
                this.scheduleToPingIMS(this.configuration.getPingIMSInterval());
            } catch (Exception e) {
                // 這裡不管catch到什麼異常, 都塞入this.lock
                // 同時丟出去給呼叫這個方法的程式處理
                this.lockProcessException.set(e);
                synchronized (this.lockProcessException) {
                    this.lockProcessException.notifyAll();
                }
                throw e;
            } finally {
                // 釋放當前的method
                lockSendToIMS.set(false);
            }
        }
    }

    public boolean isLockSendToIMS() {
        boolean isLockSendToIMS = this.lockSendToIMS.get();
        if (isLockSendToIMS)
            LogHelperFactory.getTraceLogger().warn(this.configuration.getName(), " sendToIMS execute in processing..., ", this.configuration.forLogging());
        return isLockSendToIMS;
    }

    public boolean isPaused() {
        boolean paused = this.configuration.isPause();
        if (paused)
            LogHelperFactory.getTraceLogger().warn(this.configuration.getName(), " is paused, ", this.configuration.forLogging());
        return paused;
    }

    public boolean isReceiverConnected() {
        boolean receiverConnected = this.configuration.isReceiverConnected();
        if (!receiverConnected)
            LogHelperFactory.getTraceLogger().warn(this.configuration.getName(), " receiver is not connected, ", this.configuration.forLogging());
        return receiverConnected;
    }

    private String getTransCode(String pCode) {
        String transCode = "FG";

        if (StringUtils.trimToEmpty(pCode).length() != 4) {
            return transCode;
        }

        if (StringUtils.indexOf(pCode, "24") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "26") == 0) {
            transCode = "WW";
        } else if (StringUtils.indexOf(pCode, "252") == 0) {
            transCode = "TR";
        } else if (StringUtils.indexOf(pCode, "256") == 0 && !StringUtils.equals(pCode, "2566")) {
            transCode = "TX";
        } else {
            switch (pCode) {
                case "2510":
                    transCode = "WD";
                    break;
                case "2555":
                case "2556":
                    transCode = "TR";
                    break;
                case "2120":
                case "2130":
                case "2140":
                case "2150":
                case "2160":
                case "2270":
                case "2280":
                case "2290":
                case "2547":
                case "2549":
                case "2573":
                case "2574":
                    transCode = "RV";
                    break;
                case "2531":
                case "2532":
                case "2541":
                case "2542":
                case "2543":
                case "2551":
                case "2552":
                    transCode = "TX";
                    break;
                case "2261":
                case "2262":
                case "2263":
                case "2264":
                case "2566":
                    transCode = "PY";
                    break;
                case "2505":
                case "2545":
                case "2546":
                case "2571":
                case "2572":
                    transCode = "WW";
                    break;
            }
        }

        return transCode;
    }

    /**
     * 銷毀
     */
    @Override
    protected void destroy() {
        if (this.pingIMSTimer != null) {
            this.pingIMSTimer.destroy();
        }
        // 終止時, 這裡要notify一下, 避免thread一直處於wait狀態
        synchronized (this.lockProcessException) {
            this.lockProcessException.notifyAll();
        }
    }

    /**
     * 定時PING IMS, 從sendToIMS成功之後開始計時
     *
     * @param initialDelay
     */
    private void scheduleToPingIMS(long initialDelay) {
        if (pingIMSTimer == null) {
            pingIMSTimer = new PingIMSTimer(StringUtils.join(this.configuration.getName(), "-PingIMS"));
        }
        pingIMSTimer.scheduleAtFixedRate(initialDelay, this.configuration.getPingIMSInterval(), TimeUnit.MILLISECONDS);
    }

    private void unscheduleToPingIMS() {
        if (pingIMSTimer != null) {
            pingIMSTimer.cancel();
        }
    }

    private class PingIMSTimer extends AbstractScheduledTask {
        private final LogHelper logger = LogHelperFactory.getTraceLogger();
        private static final String PING_REQUEST = "PING IMS_CONNECT";
        private static final String PING_RESPONSE = "PING RESPONSE";

        public PingIMSTimer(String taskName) {
            super(taskName);
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            if (connection == null || !connection.isConnected() || !IMSTransmissionConnState.isClientConnected(currentConnState.get())) {
                logger.warn("[PingIMSTimer.execute]Connection is null or disconnected, cancel Ping IMS Timer");
                this.cancel();
                return;
            }
            try {
                logger.debug("[PingIMSTimer.execute]Begin Ping IMS");
                TmInteraction myTmInteraction;
                myTmInteraction = connection.createInteraction();
                myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_NOT_INCLUDE_LLZZ_AND_TRANCODE);
                myTmInteraction.setTrancode("");
                byte[] indata = PING_REQUEST.getBytes(ApiProperties.DEFAULT_IMS_CONNECT_CODEPAGE);
                // get InputMessage instance from myTMInteraction
                InputMessage inMsg = myTmInteraction.getInputMessage();
                // populate input message data with indata byte array
                inMsg.setInputMessageData(indata);
                myTmInteraction.execute();
                OutputMessage outMsg = myTmInteraction.getOutputMessage();
                String outStr = getOutputString(outMsg);
                logger.debug("[PingIMSTimer.execute]Get Ping Result:", outStr);
                if (outMsg.getImsConnectReturnCode() == 0 && StringUtils.contains(outStr, PING_RESPONSE)) {
                    // ping成功, 不做任何處理
                } else {
                    // PING失敗, 直接丟異常
                    throw ExceptionUtil.createException("PING Failed");
                }
            } catch (Exception e) {
                logger.error(e, "[PingIMSTimer.execute]Ping Exception:", e.getMessage());
                // 有catch到異常則直接斷線, 並且停止Timer
                lockProcessException.set(e);
                synchronized (lockProcessException) {
                    lockProcessException.notifyAll();
                }
                this.cancel();
            }
        }

        private String getOutputString(OutputMessage outMsg) throws ImsConnectApiException {
            String result;
            try {
                result = new String(outMsg.getDataAsByteArray(), ApiProperties.DEFAULT_IMS_CONNECT_CODEPAGE);
                int csmPos = result.indexOf("*CSMOKY*");
                if (csmPos < 0) {
                    return null;
                } else {
                    return result.substring(0, csmPos);
                }
            } catch (UnsupportedEncodingException e) {
                logger.warn(e, "[PingIMSTimer.getOutputString]UnsupportedEncodingException:", e.getMessage());
                return null;
            }
        }
    }

    /**
     * 更新統計數據
     *
     * @param accumulate
     */
    public void accumulateStatisticsTransactions(long accumulate) {
        if (this.statistics != null)
            this.statistics.updateAndGet(x -> CBSGatewayStatistic.accumulateTransactions(x, accumulate));
    }
}
