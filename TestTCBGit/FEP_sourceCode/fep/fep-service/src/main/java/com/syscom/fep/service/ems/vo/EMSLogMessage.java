package com.syscom.fep.service.ems.vo;

import com.syscom.fep.frmcommon.log.LogbackMessage;
import com.syscom.fep.service.ems.vo.EMSLogMessage.MDC;

/**
 * EMSController接收的電文json內容
 * <p>
 * "timestamp": "1627264172203",
 * "level": "INFO",
 * "thread": "main",
 * "mdc": {
 * "errcode": "",
 * "linenumber": "31",
 * "ATMNo": "845",
 * "TxDate": "20210726",
 * "TroutActno": "554433",
 * "Ej": "",
 * "TxMessage": "Hello Richard",
 * "Channel": "HTTP",
 * "MessageGroup": "grp001",
 * "Stan": "54321",
 * "ATMSeq": "1212",
 * "Step": "1",
 * "Subsys": "ATMP",
 * "MessageFlow": "Request",
 * "ProgramFlow": "AAIn",
 * "ProgramName": "EMSLoggingTest",
 * "TrinActno": "334455",
 * "Bkno": "807",
 * "TroutBank": "905",
 * "TrinBank": "807",
 * "LogDate": "2021/07/26 09:49:32.168",
 * "MessageID": "1111111"
 * },
 * "logger": "FEPMessageLogger",
 * "message": "test2",
 * "context": "default",
 * "messageType": "log",
 * "messageTarget": "fep"
 *
 * @author Richard
 */
public class EMSLogMessage extends LogbackMessage<MDC> {
    private String messageType;
    private String messageTarget;

    public String getMessageType() {
        return messageType;
    }

    public String getMessageTarget() {
        return messageTarget;
    }

    public static class MDC {
        private String ATMNo;
        private String TxDate;
        private String TroutActno;
        private String Ej;
        private String TxMessage;
        private String Channel;
        private String MessageGroup;
        private String Stan;
        private String ATMSeq;
        private String Step;
        private String Subsys;
        private String MessageFlow;
        private String ProgramFlow;
        private String ProgramName;
        private String TrinActno;
        private String Bkno;
        private String TroutBank;
        private String TrinBank;
        private String LogDate;
        private String MessageID;
        private String errcode;
        private String linenumber;
        private String hostip;
        private String socketType;
        private String hostname;
        private String errdescription;
        private String app;
        private String profile;
        private String gateway;
        private String Sys;
        private String level;
        private String TxRquid;
        private String FuncNo;
        private String KeyId;
        private String Input1;
        private String Input2;
        private String Output1;
        private String Output2;
        private String SuipCommand;
        private String RC;
        private String CallObj;

        public String getATMNo() {
            return ATMNo;
        }

        public void setATMNo(String aTMNo) {
            ATMNo = aTMNo;
        }

        public String getTxDate() {
            return TxDate;
        }

        public void setTxDate(String txDate) {
            TxDate = txDate;
        }

        public String getTroutActno() {
            return TroutActno;
        }

        public void setTroutActno(String troutActno) {
            TroutActno = troutActno;
        }

        public String getEj() {
            return Ej;
        }

        public void setEj(String ej) {
            Ej = ej;
        }

        public String getTxMessage() {
            return TxMessage;
        }

        public void setTxMessage(String txMessage) {
            TxMessage = txMessage;
        }

        public String getChannel() {
            return Channel;
        }

        public void setChannel(String channel) {
            Channel = channel;
        }

        public String getMessageGroup() {
            return MessageGroup;
        }

        public void setMessageGroup(String messageGroup) {
            MessageGroup = messageGroup;
        }

        public String getStan() {
            return Stan;
        }

        public void setStan(String stan) {
            Stan = stan;
        }

        public String getATMSeq() {
            return ATMSeq;
        }

        public void setATMSeq(String aTMSeq) {
            ATMSeq = aTMSeq;
        }

        public String getStep() {
            return Step;
        }

        public void setStep(String step) {
            Step = step;
        }

        public String getSubsys() {
            return Subsys;
        }

        public void setSubsys(String subsys) {
            Subsys = subsys;
        }

        public String getMessageFlow() {
            return MessageFlow;
        }

        public void setMessageFlow(String messageFlow) {
            MessageFlow = messageFlow;
        }

        public String getProgramFlow() {
            return ProgramFlow;
        }

        public void setProgramFlow(String programFlow) {
            ProgramFlow = programFlow;
        }

        public String getProgramName() {
            return ProgramName;
        }

        public void setProgramName(String programName) {
            ProgramName = programName;
        }

        public String getTrinActno() {
            return TrinActno;
        }

        public void setTrinActno(String trinActno) {
            TrinActno = trinActno;
        }

        public String getBkno() {
            return Bkno;
        }

        public void setBkno(String bkno) {
            Bkno = bkno;
        }

        public String getTroutBank() {
            return TroutBank;
        }

        public void setTroutBank(String troutBank) {
            TroutBank = troutBank;
        }

        public String getTrinBank() {
            return TrinBank;
        }

        public void setTrinBank(String trinBank) {
            TrinBank = trinBank;
        }

        public String getLogDate() {
            return LogDate;
        }

        public void setLogDate(String logDate) {
            LogDate = logDate;
        }

        public String getMessageID() {
            return MessageID;
        }

        public void setMessageID(String messageID) {
            MessageID = messageID;
        }

        public String getErrcode() {
            return errcode;
        }

        public void setErrcode(String errcode) {
            this.errcode = errcode;
        }

        public String getLinenumber() {
            return linenumber;
        }

        public void setLinenumber(String linenumber) {
            this.linenumber = linenumber;
        }

        public String getHostip() {
            return hostip;
        }

        public void setHostip(String hostip) {
            this.hostip = hostip;
        }

        public String getSocketType() {
            return socketType;
        }

        public void setSocketType(String socketType) {
            this.socketType = socketType;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getErrdescription() {
            return errdescription;
        }

        public void setErrdescription(String errdescription) {
            this.errdescription = errdescription;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public String getSys() {
            return Sys;
        }

        public void setSys(String sys) {
            Sys = sys;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getTxRquid() {
            return TxRquid;
        }

        public void setTxRquid(String txRquid) {
            TxRquid = txRquid;
        }

        public String getFuncNo() {
            return FuncNo;
        }

        public void setFuncNo(String funcNo) {
            FuncNo = funcNo;
        }

        public String getKeyId() {
            return KeyId;
        }

        public void setKeyId(String keyId) {
            KeyId = keyId;
        }

        public String getInput1() {
            return Input1;
        }

        public void setInput1(String input1) {
            Input1 = input1;
        }

        public String getInput2() {
            return Input2;
        }

        public void setInput2(String input2) {
            Input2 = input2;
        }

        public String getOutput1() {
            return Output1;
        }

        public void setOutput1(String output1) {
            Output1 = output1;
        }

        public String getOutput2() {
            return Output2;
        }

        public void setOutput2(String output2) {
            Output2 = output2;
        }

        public String getSuipCommand() {
            return SuipCommand;
        }

        public void setSuipCommand(String suipCommand) {
            SuipCommand = suipCommand;
        }

        public String getRC() {
            return RC;
        }

        public void setRC(String RC) {
            this.RC = RC;
        }

        public String getCallObj() {
            return CallObj;
        }

        public void setCallObj(String callObj) {
            CallObj = callObj;
        }
    }
}
