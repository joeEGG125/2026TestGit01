package com.syscom.fep.gateway.ims.cbs.receiver;

import com.ibm.ims.connect.*;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.CBSType;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiver;
import com.syscom.fep.frmcommon.delegate.MessageAsynchronousWaitReceiverManager;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.gateway.ims.IMSTransmissionProcessRequest;
import com.syscom.fep.gateway.ims.cbs.CBSGatewayManager;
import com.syscom.fep.vo.communication.ToFEPCBSCommu;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

@StackTracePointCut(caller = SvrConst.SVR_CBS_GATEWAY)
public class CBSGatewayClientReceiverProcessRequest
        extends IMSTransmissionProcessRequest<CBSGatewayClientReceiverConfiguration> {
    /**
     * 處理業務邏輯
     *
     * @param logData
     * @param connection
     * @throws Exception
     */
    @Override
    public void doProcess(LogData logData, Connection connection) throws Exception {
        // 開始等待接收資料(Receiver)- resumeClearTpipe
        resumeClearTpipe(logData, connection);
    }

    private void resumeClearTpipe(LogData logData, Connection myConn) throws Exception {
        boolean isTxData = false;
        try {
            // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            // logData.setRemark("begin resumeClearTpipe -" + this.configuration.getClientId());
            // FEPBase.logMessage(Level.INFO, logData);
            // LogHelperFactory.getTraceLogger().debug("begin resumeClearTpipe:" + this.configuration.getClientId() +
            //         ", Timeout:" + this.configuration.getResumeInterval());

            TmInteraction myTmInteraction = myConn.createInteraction();

            //first interaction send Resume TPIPE
            String result = resumeTPipe(logData, myTmInteraction, this.configuration.getClientId(),
                    this.configuration.getTranCode());
            isTxData = isTxData(result);
            checkData(result, logData);
            while (true) {

                if (myTmInteraction.isAckNakNeeded()) {
                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(StringUtils.join("Before send Ack and Receive -", this.configuration.getClientId()));
                    // logMessage(logData);
                    // myTmInteraction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_SINGLE_WAIT);
                    // myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);
                    // myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_500_MILLISECONDS);
                    // myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_1_SECOND);
                    // myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);
                    // String intType;
                    // if ((myTmInteraction.getOutputMessage()
                    //         .getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS)) {
                    //intType = ApiProperties.INTERACTION_TYPE_DESC_ACK;
                    // } else {
                    //     intType = ApiProperties.INTERACTION_TYPE_DESC_NAK;
                    // }
                    myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_ACK);
                    // Execute ack interaction and Receive next TX
                    myTmInteraction.execute();

                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(StringUtils.join("After send ack -", this.configuration.getClientId() +
                    //         ", returnCode=" + myTmInteraction.getOutputMessage().getImsConnectReturnCode() +
                    //         ", reasonCode=" + myTmInteraction.getOutputMessage().getImsConnectReasonCode()));
                    // logMessage(logData);
                }
                //IRM Timeout or 其他的returnCode, 重新resume TPIPE
                if (!(myTmInteraction.getOutputMessage().getImsConnectReturnCode() == ApiProperties.IMS_CONNECT_RETURN_CODE_SUCCESS &&
                        myTmInteraction.getImsConnectReasonCode() == ApiProperties.IMS_CONNECT_REASON_CODE_SUCCESS)) {
                    break;
                } else {
                    // 檢查收到內容是否符合下送格式
                    OutputMessage outMsg = myTmInteraction.getOutputMessage();
                    result = StringUtil.toHex(outMsg.getDataAsByteArray());
                    // 2026-08-11 Richard modified 實際有收到data還是要記log by Ashiang
                    if (isTxData(result)) {
                        isTxData = true;
                        logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                        logData.setMessage(result);
                        logData.setRemark(StringUtils.join("Get result at ", this.configuration.getClientId()));
                        logMessage(Level.INFO, logData);
                    }
                    checkData(result, logData);
                    // if (StringUtils.isNotBlank(req)) {
                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(
                    // StringUtils.join("before sendResponseToFEP - ",
                    // this.configuration.getClientId()));
                    // logMessage(logData);
                    // CBSGatewayManager manager =
                    // SpringBeanFactoryUtil.getBean(CBSGatewayManager.class);
                    // manager.execute(() -> {
                    // this.sendResponseToFEP(logData, req);
                    // });
                    // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                    // logData.setRemark(
                    // StringUtils.join("after sendResponseToFEP - ",
                    // this.configuration.getClientId()));
                    // logMessage(logData);
                    // }
                }
            }
        } catch (Exception e) {
            logData.setProgramException(e);
            logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
            logData.setRemark(e.getMessage());
            FEPBase.sendEMS(logData);
            throw e;
        } finally {
            if (isTxData) {
                // logData.setProgramName(StringUtils.join(ProgramName, ".resumeClearTpipe"));
                // logData.setRemark("exit resumeClearTpipe:" + this.configuration.getClientId());
                // FEPBase.logMessage(Level.INFO, logData);
                LogHelperFactory.getTraceLogger().debug("exit resumeClearTpipe:" + this.configuration.getClientId());
            }
        }
    }

    private String resumeTPipe(LogData logData, TmInteraction myTmInteraction, String clientId, String _trancode)
            throws Exception {
        // logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        // logData.setRemark(StringUtils.join("Begin resumeTPipe -" + this.configuration.getClientId()));
        // FEPBase.logMessage(Level.INFO, logData);

        byte[] emptyByteArray = {};
        myTmInteraction.setImsDatastoreName(this.configuration.getDataStore());
        myTmInteraction.setLtermOverrideName(this.configuration.getClientId());
        myTmInteraction.setInputMessageDataSegmentsIncludeLlzzAndTrancode(
                ApiProperties.INPUT_MESSAGE_DATA_SEGMENTS_DO_INCLUDE_LLZZ_AND_TRANCODE);
        myTmInteraction.setInteractionTypeDescription(ApiProperties.INTERACTION_TYPE_DESC_RESUMETPIPE);
        myTmInteraction.setCommitMode(ApiProperties.COMMIT_MODE_0);
        // 當connectionTimeout > interactionTimeout時, 會引發receive Timeout例外
        // 若未超過, 則到connectionTimeout時間未收到資料會getImsConnectReturnCode=40的rc
        // myTmInteraction.setImsConnectTimeout(ApiProperties.TIMEOUT_1_MINUTE);
        // myTmInteraction.setInteractionTimeout(ApiProperties.TIMEOUT_1_MINUTE + ApiProperties.TIMEOUT_1_SECOND);
        myTmInteraction.setImsConnectTimeout(this.configuration.getResumeInterval());
        myTmInteraction.setInteractionTimeout(this.configuration.getResumeInterval() + ApiProperties.TIMEOUT_5_SECONDS);
        // + ApiProperties.TIMEOUT_5_SECONDS);//直到收到資料再往下
        myTmInteraction.setResumeTpipeAlternateClientId(clientId);
        myTmInteraction.setTrancode("");
        myTmInteraction.setResumeTpipeProcessing(ApiProperties.RESUME_TPIPE_AUTO);
        myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_OR_ASYNC_MESSAGE);
        // myTmInteraction.setResumeTpipeRetrievalType(ApiProperties.RETRIEVE_SYNC_MESSAGE_ONLY);
        myTmInteraction.setAckNakProvider(ApiProperties.CLIENT_ACK_NAK);

        InputMessage inMsg = (InputMessage) myTmInteraction.getInputMessage();
        inMsg.setInputMessageData(emptyByteArray);

        myTmInteraction.execute();

        OutputMessage outMsg = myTmInteraction.getOutputMessage();
        String result = StringUtil.toHex(outMsg.getDataAsByteArray());

        if (isTxData(result)) {
            LogHelperFactory.getTraceLogger().debug(StringUtils.join("resumeTPipe get outData,ImsConnectReturnCode():",
                    myTmInteraction.getOutputMessage().getImsConnectReturnCode(),
                    ", clientId:" + this.configuration.getClientId(),
                    " =>" + result));
        }
        // logData.setProgramName(StringUtils.join(ProgramName, ".resumeTPipe"));
        // logData.setMessage(result);
        // logData.setRemark(StringUtils.join("resumeTPipe get outData,getImsConnectReturnCode():",
        //         myTmInteraction.getOutputMessage().getImsConnectReturnCode(), ", clientId=" + this.configuration.getClientId()));
        // FEPBase.logMessage(Level.INFO, logData);
        return result;

    }

    private void checkData(String result, LogData logData) {
        //*CSMOKY*
        int csmPos = fetchTxDataIndex(result);
        // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
        // csmPos: " + csmPos);
        //int dfs2082 = result.indexOf("C4C6E2");
        // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
        // dfs2082: " + dfs2082);
        // System.out.println("Have *CSMOKY*:"+csmPos);
        // this.setMessageFromIMS(result);
        // if (dfs2082 >= 0) {
        // // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
        // // dfs2082 >= 0 return null ");
        // return "";
        // } else {
        if (csmPos >= 0) {
            // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
            // csmPos >= 0 return result.substring(0, csmPos) end ");
            String req = result.substring(0, csmPos);

            logData.setProgramName(StringUtils.join(ProgramName, ".checkData"));
            logData.setMessage(req);
            logData.setRemark(
                    StringUtils.join("before sendResponseToFEP - ", this.configuration.getClientId()));
            logMessage(logData);
            CBSGatewayManager manager = SpringBeanFactoryUtil.getBean(CBSGatewayManager.class);
            manager.execute(() -> {
                this.sendResponseToFEP(logData, req);
            });
            logData.setProgramName(StringUtils.join(ProgramName, ".checkData"));
            logData.setRemark(
                    StringUtils.join("after sendResponseToFEP - ", this.configuration.getClientId()));
            logMessage(logData);

        } else {
            // LogHelperFactory.getGeneralLogger().info(" [MethodName:getOutputString]
            // csmPos < 0 return null ");
            //return "";
        }
        // }
    }

    /**
     * 從receiver收到電文, 根據clientId取出電文的CBSID送回前端
     *
     * @param logData
     * @param result
     */
    private void sendResponseToFEP(LogData logData, String result) {
        this.putMDC();
        // 根據目前的teleType, 決定如何取出CBSId欄位值，規則如下
        String cbsId = StringUtils.EMPTY;
        CBSType cbsType = CBSType.fromCode(this.configuration.getCbsType());
        switch (cbsType) {
            // 新主機電文格式以SYSDATETIME+EJ做為CBSId
            case CBS:
            case NONATM:
            case FISCTCB:
                String all = EbcdicConverter.fromHex(CCSID.English, result);
                if (StringUtils.contains(all, "FEP161000004")) { // 外圍格式
                    String stan = result.substring(28, 44);
                    cbsId = EbcdicConverter.fromHex(CCSID.English, stan);
                } else if (StringUtils.contains(all, "FEP1910A0037")) { //cbstimoutrerun &　pendingAskFromFisc 格式
                    String ej = result.substring(28, 44);
                    cbsId = EbcdicConverter.fromHex(CCSID.English, ej);
                } else {
                    // String dt = result.substring(24, 52);
                    String ej = result.substring(52, 68);
                    cbsId = EbcdicConverter.fromHex(CCSID.English, ej);
                }
                break;
            // 財金電文格式以STAN做為CBSId
            case FISC:
                String stan = result.substring(22, 36);
                cbsId = EbcdicConverter.fromHex(CCSID.English, stan);
                break;
            // 473X電文格式以WSID+ATMSEQ做為CBSId
            case _473X:
                String wsid = result.substring(0, 10);
                String atmSeq = result.substring(46, 58);
                // cbsId = EbcdicConverter.fromHex(CCSID.English, wsid + atmSeq);
                cbsId = wsid + atmSeq;
                break;
        }
        logData.setMessageId(cbsId);
        logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToFEP"));
        logData.setRemark(StringUtils.join("Parse IMS Response Message, get cbsId = [", cbsId, "], ",
                this.configuration.forLogging()));
        logData.setMessage(result);
        this.logMessage(logData);
        // 以cbsId為key,從dictionary中取callback reference, 將result電文 call back回處理
        MessageAsynchronousWaitReceiver<String, ToFEPCBSCommu> callback = MessageAsynchronousWaitReceiverManager
                .unsubscribe(this, cbsId, ToFEPCBSCommu.class);
        if (callback != null) {
            logData.setMessageId(cbsId);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToFEP"));
            logData.setRemark(
                    StringUtils.join("Callback message, cbsId = [", cbsId, "]", this.configuration.forLogging()));
            logData.setMessage(result);
            this.logMessage(logData);
            // prepare message
            ToFEPCBSCommu toFEPCBSCommu = new ToFEPCBSCommu();
            toFEPCBSCommu.setMessage(result);
            toFEPCBSCommu.setCbsId(cbsId);
            toFEPCBSCommu.setClientID(this.configuration.getClientId());
            // 包成ToFEPCBSCommu callback
            callback.messageArrived(this, toFEPCBSCommu);
        } else {
            logData.setMessageId(cbsId);
            logData.setProgramName(StringUtils.join(ProgramName, ".sendResponseToFEP"));
            logData.setRemark(StringUtils.join("Cannot callback message cause cbsId = [", cbsId, "] not exist, ",
                    this.configuration.forLogging()));
            logData.setMessage(result);
            sendEMS(logData);
        }
    }

    /**
     * 銷毀
     */
    @Override
    protected void destroy() {}

    /**
     * 檢查是否為交易資料
     *
     * @param result
     * @return
     */
    private boolean isTxData(String result) {
        return fetchTxDataIndex(result) >= 0;
    }

    /**
     * 取得交易資料索引
     *
     * @param result
     * @return
     */
    private int fetchTxDataIndex(String result) {
        return result.indexOf("5CC3E2D4D6D2E85C");
    }
}
