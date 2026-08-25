package com.syscom.fep.enchelper;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.enclib.ENCLib;
import com.syscom.fep.enclib.vo.ENCLogData;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

public abstract class CommonENCHelper extends FEPBase {
    protected Feptxn feptxn;
    private FEPChannel channel;
    private SubSystem subSys;
    protected String atmNo;
    protected String atmSeq;
    private String msgId;
    protected MessageBase txData;
    protected LogData logData;
    protected FISCData rmData;
    @SuppressWarnings("unused")
    private String libName = StringUtils.EMPTY;
    @SuppressWarnings("unused")
    private String callAssembly;

    /**
     * For ATM交易專用
     *
     * @param feptxn
     * @param txData
     */
    protected CommonENCHelper(Feptxn feptxn, MessageBase txData) {
        this(feptxn);
        this.txData = txData;
        this.logData = txData.getLogContext();
    }

    /**
     * ENCHelper Constructor
     *
     * @param feptxn 含交易資料的FEPTxn物件
     */
    protected CommonENCHelper(Feptxn feptxn) {
        this.ej = feptxn.getFeptxnEjfno() != null ? feptxn.getFeptxnEjfno().intValue() : 0;
        this.channel = StringUtils.isBlank(feptxn.getFeptxnChannel()) ? FEPChannel.Unknown : FEPChannel.parse(feptxn.getFeptxnChannel());
        this.subSys = feptxn.getFeptxnSubsys() != null ? StringUtils.isBlank(feptxn.getFeptxnSubsys().toString()) ? SubSystem.None : SubSystem.parse(feptxn.getFeptxnSubsys()) : SubSystem.None;
        this.feptxn = feptxn;
        this.atmNo = feptxn.getFeptxnAtmno();
        this.atmSeq = feptxn.getFeptxnAtmSeqno();
        this.msgId = feptxn.getFeptxnMsgid();
        if (this.logData == null) {
            this.logData = new LogData();
            this.logData.setEj(getEj());
            this.logData.setSubSys(subSys);
            this.logData.setChannel(channel);
            this.logData.setMessageFlowType(MessageFlow.Request);
            this.logData.setAtmNo(atmNo);
            this.logData.setAtmSeq(atmSeq);
            this.logData.setMessageId(msgId);
        }
    }

    /**
     * BugReport(001B0682):新增atmno,atmseq參數
     *
     * @param msgId
     * @param channel
     * @param subsys
     * @param ej
     * @param atmno
     * @param atmseq
     * @param txData
     */
    protected CommonENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq, MessageBase txData) {
        this.channel = channel == null ? FEPChannel.Unknown : channel;
        this.subSys = subsys;
        this.msgId = msgId;
        this.setEj(ej);
        if (this.txData == null) {
            this.logData = new LogData();
            this.logData.setEj(getEj());
            this.logData.setSubSys(subsys);
            this.logData.setChannel(channel);
            this.logData.setMessageFlowType(MessageFlow.Request);
            this.logData.setAtmNo(atmno);
            this.logData.setAtmSeq(atmseq);
            this.logData.setMessageId(msgId);
        } else {
            this.txData = txData;
            this.logData = txData.getLogContext();
        }
    }

    /**
     * For財金交易及RM交易專用
     *
     * @param txData
     */
    protected CommonENCHelper(FISCData txData) {
        this.rmData = txData;
        this.channel = txData.getTxChannel() == null ? FEPChannel.Unknown : txData.getTxChannel();
        this.subSys = txData.getTxSubSystem();
        this.msgId = txData.getMessageID();
        this.setEj(txData.getEj());
        this.logData = txData.getLogContext();
    }

    /**
     * For TSM
     *
     * @param msgId
     * @param ej
     */
    protected CommonENCHelper(String msgId, int ej) {
        this.channel = FEPChannel.Unknown;
        this.subSys = SubSystem.Card;
        this.msgId = msgId;
        this.ej = ej;
        this.logData = new LogData();
        this.logData.setEj(ej);
        this.logData.setSubSys(this.subSys);
        this.logData.setChannel(this.channel);
        this.logData.setMessageFlowType(MessageFlow.Request);
        this.logData.setMessageId(msgId);
    }

    /**
     * 2022-07-28 Richard add
     *
     * @param txData
     */
    public CommonENCHelper(MessageBase txData) {
        this.txData = txData;
        this.channel = txData.getTxChannel() == null ? FEPChannel.Unknown : txData.getTxChannel();
        this.subSys = txData.getTxSubSystem();
        this.msgId = txData.getMessageID();
        this.setEj(txData.getEj());
        this.logData = txData.getLogContext();
    }

    /**
     * ENC公用處理函式
     *
     * @param encFunc
     * @param method
     * @param inputData
     * @param outputData
     * @return
     */
    protected int encLib(String encFunc, String method, ENCParameter[] inputData, String[] outputData) {
        // 2013-11-07 Modify by Ruling for ENCLib整合FEP和MRM
        int encRetryCount = CMNConfig.getInstance().getENCLibRetryCount();
        int encLibRetryInterval = CMNConfig.getInstance().getENCLibRetryInterval();
        String suipAddress = CMNConfig.getInstance().getSuipAddress();
        int suipTimeout = CMNConfig.getInstance().getSuipTimeout();
        String keyid = inputData[0].getKeyIdentity();
        String inputStr1 = inputData[0].getInputData1();
        if (StringUtils.isNotBlank(inputStr1)) {
            // 複合功能時長度已在組inputdata時補進去了
            if (StringUtils.isNotBlank(keyid)) {
                inputStr1 = StringUtils.join(StringUtils.leftPad(Integer.toString(inputStr1.length()), 4, '0'), inputStr1);
            }
        }
        String inputStr2 = inputData[0].getInputData2();
        if (StringUtils.isNotBlank(inputStr2)) {
            inputStr2 = StringUtils.join(StringUtils.leftPad(Integer.toString(inputStr2.length()), 4, '0'), inputStr2);
        }
        RefString o1 = new RefString();
        RefString o2 = new RefString();
        int rtn = -1;
        try {
            this.writeENCLogMessage(encFunc, method, keyid, inputStr1, inputStr2, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, ProgramFlow.ENCIn);
            try {
                ENCLogData encLog = new ENCLogData();
                BeanUtils.copyProperties(this.logData, encLog);
                encLog.setChannel(channel == null ? FEPChannel.Unknown.name() : channel.name()); // 這裡塞入Channel, 目的是為了在callSuip中判斷是否需要進行socket連線獲取資料
                encLog.setSystemId(FEPConfig.getInstance().getSystemId()); // 塞入systemId
                encLog.setHostIp(FEPConfig.getInstance().getHostIp());
                encLog.setHostName(FEPConfig.getInstance().getHostName());
                encLog.setApp(FEPConfig.getInstance().getApplicationName());
                ENCLib encLib = new ENCLib(encLog, encRetryCount, encLibRetryInterval, suipAddress, suipTimeout);
                rtn = encLib.callSuip(encFunc, keyid, inputStr1, inputStr2, o1, o2);
            } catch (Exception e) {
                this.logData.setProgramException(e);
                this.logData.setRemark(this.getENCLogMessage(encFunc, keyid, inputStr1, inputStr2, o1.get(), o2.get(), String.valueOf(rtn)));
                sendEMS(this.logData);
                return rtn;
            }
            this.writeENCLogMessage(encFunc, method, keyid, inputStr1, inputStr2, o1.get(), o2.get(), String.valueOf(rtn), ProgramFlow.ENCOut);
//            if ("FN000301".equals(encFunc)) {
//                outputData[0] = "FFFFFFFF";
//            }
            if (rtn == 0) {
                if (ArrayUtils.isNotEmpty(outputData)) {
                    if (o1.isNotBlank()) {
                        outputData[0] = o1.substring(4); // 去掉前4個byte長度;
                    }
                    if (outputData.length > 1 && o2.isNotBlank()) {
                        outputData[1] = o2.substring(4); // 去掉前4個byte長度;
                    }
                }
            } else {
                // 2011-02-18 by kyo for 203 不論RC對錯皆會回應outPutData
                if ("FN000203".equals(encFunc)) {
                    if (ArrayUtils.isNotEmpty(outputData)) {
                        if (o1.isNotBlank()) {
                            outputData[0] = o1.substring(4); // 去掉前4個byte長度;
                        }
                        if (outputData.length > 1 && o2.isNotBlank()) {
                            outputData[1] = o2.substring(4); // 去掉前4個byte長度;
                        }
                    }
                }
                // 2012-07-24 by Ashiang: 當 RC>1 才送EMS
                if (rtn > 1) {
                    // 2010-08-16 by kyo for 當RC非0就送EMS顯示給OP看
                    this.logData.setRemark(this.getENCLogMessage(encFunc, keyid, inputStr1, inputStr2, o1.get(), o2.get(), String.valueOf(rtn)));
                    Exception encEx = ExceptionUtil.createException(this.logData.getRemark());
                    this.logData.setProgramException(encEx);
                    // 2011-01-05 by ed for enc check error時加上programflow和messageflow
                    this.logData.setProgramFlowType(ProgramFlow.ENCOut);
                    this.logData.setMessageFlowType(MessageFlow.Response);
                    sendEMS(this.logData);
                }
            }
            return rtn;
        } catch (Exception e) {
            this.logData.setProgramException(e);
            this.logData.setRemark(this.getENCLogMessage(encFunc, keyid, inputStr1, inputStr2, StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(rtn)));
            sendEMS(this.logData);
            return rtn;
        }
    }

    protected String pack(String data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            String x = data.substring(i, i + 1);
            switch (x) {
                case "A":
                    sb.append(":");
                    break;
                case "B":
                    sb.append(";");
                    break;
                case "C":
                    sb.append("<");
                    break;
                case "D":
                    sb.append("=");
                    break;
                case "E":
                    sb.append(">");
                    break;
                case "F":
                    sb.append("?");
                    break;
                default:
                    sb.append(x);
                    break;
            }
        }
        return sb.toString();
    }

    protected String unPack(String data) {
        // 特殊字元不UNPACK (: ; < = > ?)
        if (!StringUtils.containsAny(data, ':', ';', '<', '=', '>', '?')) {
            return data;
        }
        data = StringUtil.toHex(data);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i += 2) {
            String x = data.substring(i, i + 2);
            int dec = Integer.parseInt(x, 16);
            char ascS;
            if (dec >= 48 && dec <= 57) {
                ascS = (char) dec;
            } else {
                ascS = (char) (dec + 7);
            }
            sb.append(ascS);
        }
        return sb.toString();
    }

    protected String getENCLogMessage(String encFunc, String keyid, String inputStr1, String inputStr2, String outString1, String outString2, String rc) {
        // 2010-08-17 by kyo FOR SENDEMS顯示時RC顯示在第一位
        return StringUtils.join("RC:", rc, ",FUNC:", encFunc, ",KEYID:", keyid, ",INPUTSTR1:", inputStr1, ",INPUTSTR2:", inputStr2, ",OUTPUT1:", outString1, ",OUTPUT2:", outString2);
    }

    private void writeENCLogMessage(String encFunc, String method, String keyid, String inputStr1, String inputStr2, String outString1, String outString2, String rc, ProgramFlow programFlow) {
        String encMsg = this.getENCLogMessage(encFunc, keyid, inputStr1, inputStr2, outString1, outString2, rc);
        this.logData.setProgramFlowType(programFlow);
        this.logData.setProgramName(StringUtils.join(this.ProgramName, ".", method));
        this.logData.setMessage(encMsg);
        if (programFlow == ProgramFlow.ENCIn) {
            this.logData.setMessageFlowType(MessageFlow.Request);
            this.logData.setRemark(StringUtils.join("Begin call suip ", encFunc));
        } else {
            this.logData.setMessageFlowType(MessageFlow.Response);
            this.logData.setRemark(StringUtils.join("After call suip ", encFunc, "RC:", rc));
        }
        this.logMessage(this.logData);
    }

    protected FEPReturnCode getENCReturnCode(int encrc) {
        switch (encrc) {
            case 0:
                return FEPReturnCode.Normal;
            case 101:
                return FEPReturnCode.ENCCheckMACError;
            case 102:
                return FEPReturnCode.ENCCheckTACError;
            default:
                return FEPReturnCode.ENCLibError;
        }
    }

    protected String getSHA256Hash(String input) {
        return DigestUtils.sha256Hex(input).toUpperCase();
    }

    protected String makeInputDataString(String... parts) {
        if (ArrayUtils.isEmpty(parts)) {
            return StringUtils.EMPTY;
        }
        int repeat = 2;
        String format = StringUtils.repeat("0", String.valueOf(parts.length).length());
        StringBuilder log = new StringBuilder();
        log.append("Total [").append(parts.length).append("] parts of InputData String: \r\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            // 注意這裡一定要判斷一下, .NET用+符號拼接字串, 如果字串為null, 則以空白字串來拼接
            String part = (parts[i] == null ? StringUtils.EMPTY : parts[i]);
            log.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                    .append("Part ")
                    .append(PolyfillUtil.toString(i + 1, format))
                    .append(" --> length = [")
                    .append(StringUtils.leftPad(String.valueOf(part.length()), 4, StringUtils.SPACE))
                    .append("], content = [")
                    .append(part).append(parts[i] == null ? "(parts is null)" : StringUtils.EMPTY)
                    .append("]\r\n");
            sb.append(part);
        }
        String inputDataStr = sb.toString();
        log.append(StringUtils.repeat(StringUtils.SPACE, repeat))
                .append("Whole")
                .append(StringUtils.repeat(StringUtils.SPACE, format.length()))
                .append(" --> length = [")
                .append(StringUtils.leftPad(String.valueOf(inputDataStr.length()), 4, StringUtils.SPACE))
                .append("], content = [")
                .append(inputDataStr)
                .append("]\r\n");
        LogHelperFactory.getTraceLogger().info(log.delete(log.length() - 2, log.length()).toString());
        return inputDataStr;
    }

    public void setFeptxn(Feptxn feptxn) {
        this.feptxn = feptxn;
    }
}
