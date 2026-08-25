package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MCHHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private NBData nbData;
    private String _timeFormat = "yyyy/MM/dd HH:mm:ss:sss";
    private LogData logData;
    private String fscode;
    private Calendar receivedTime;
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);

    public void setReceivedTime(Calendar receivedTime) {
        this.receivedTime = receivedTime;
    }

    public MCHHandler() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        // data = data.Replace("\r\n", string.Empty);
        LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
        String res = "";
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header vaatmReqheader = new RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ req = new RCV_NB_GeneralTrans_RQ();
        RCV_VA_GeneralTrans_RQ reqva = new RCV_VA_GeneralTrans_RQ();

        try {
            // 前面多了奇怪的符號 ex:�<?xml version="1.0" encoding="utf-8"?><SOAP-ENV:Envelope><SOAP-ENV:Header/>
            data = data.substring(data.indexOf("<"));

            if (getEj() == 0) {
                setEj(TxHelper.generateEj());
            }

            if (getLogContext() == null) {
                LogData tempVar = new LogData();
                tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                tempVar.setSubSys(SubSystem.NB);
                tempVar.setChannel(channel);
                tempVar.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                tempVar.setMessageFlowType(MessageFlow.Request);
                tempVar.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
                tempVar.setMessage(data);
                tempVar.setEj(getEj());
                logData = tempVar;
            } else {
                logData = getLogContext();
            }

            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setSubSys(SubSystem.NB);
            logData.setChannel(channel);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(data);
            logData.setEj(getEj());
            logMessage(Level.DEBUG, logData);

            nbData = new NBData();

            // 因為電文進來時有可能是NB或是VA，沒先判斷過的話，無法直接使用deserializeFromXml轉入格式物件裡
            // 所以這邊先用JSONObject抓欄位來判斷，確認過是哪種電文之後才用deserializeFromXml轉入格式物件
            JSONObject jsonobj = XML.toJSONObject(data);
            JSONObject json1 = new JSONObject(jsonobj.get("SOAP-ENV:Envelope").toString());
            JSONObject json2 = new JSONObject(json1.get("SOAP-ENV:Body").toString());
            JSONObject json3 = new JSONObject(json2.get("esb:MsgRq").toString());
            JSONObject json = new JSONObject(json3.get("SvcRq").toString());

            fscode = json.get("FSCODE").toString();
            String timeStart = "";

            req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
            //BeanUtils.copyProperties(req, reqva);

            atmReqheader = req.getBody().getRq().getHeader();
            RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = req.getBody().getRq().getSvcRq();
            /*ID+ACCT 全繳不用檢核*/
            if (StringUtils.isNotBlank(atmReqbody.getINDATE())
                    && StringUtils.isNotBlank(atmReqbody.getINTIME())) {
                timeStart = atmReqbody.getINDATE() + atmReqbody.getINTIME();

                long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                long receive = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(receivedTime.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                // 2024-11-21 Richard modified for 【Integer Overflow】
                // int diffseconds = (int) ((end - srt) / 1000);
                int diffseconds = MathUtil.safeGet((int) ((end - srt) / 1000));
                int receivedTimeDiffseconds = MathUtil.safeGet((int) ((end - receive) / 1000));
                /*回覆前端交易失敗0601, 合庫三碼代號: 126 */
                // 前端上送時間與FEP時間已超過90秒,該筆交易不處理
                if ((diffseconds > 90 )  && "RQ".equals(atmReqbody.getTXNTYPE().trim())) {
                    SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                    msgrs.setHeader(header);
                    msgrs.setSvcRs(setNbBody(atmReqbody));
                    rsbody.setRs(msgrs);
                    rs.setBody(rsbody);

                    header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                    header.setCHANNEL(atmReqheader.getCHANNEL());
                    header.setMSGID(atmReqheader.getMSGID());
                    header.setCLIENTDT(atmReqheader.getCLIENTDT());
                    header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
                    header.setSTATUSCODE("T126");
                    header.setSEVERITY("ERROR");
                    header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");

                    res = XmlUtil.toXML(rs);
                    return res;

                } else {
                    String TRNSFROUTACCNT = "";
                    if (atmReqbody.getTRNSFROUTACCNT() != null) {
                        TRNSFROUTACCNT = atmReqbody.getTRNSFROUTACCNT();
                    }
                    String TRNSFRINACCNT = "";
                    if (atmReqbody.getTRNSFRINACCNT() != null) {
                        TRNSFRINACCNT = atmReqbody.getTRNSFRINACCNT();
                    }
                    String NPPAYNO = "";
                    if (atmReqbody.getPAYDATA() != null && atmReqbody.getPAYDATA().getNPPAYNO() != null) {
                        NPPAYNO = atmReqbody.getPAYDATA().getNPPAYNO();
                    }
                    if (TRNSFROUTACCNT.length() > 16 || TRNSFRINACCNT.length() > 16 || NPPAYNO.length() > 16) {
                        SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
                        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
                        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                        msgrs.setHeader(header);
                        msgrs.setSvcRs(setNbBody(atmReqbody));
                        rsbody.setRs(msgrs);
                        rs.setBody(rsbody);

                        header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                        header.setCHANNEL(atmReqheader.getCHANNEL());
                        header.setMSGID(atmReqheader.getMSGID());
                        header.setCLIENTDT(atmReqheader.getCLIENTDT());
                        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
                        header.setSTATUSCODE("T001");
                        header.setSEVERITY("ERROR");
                        header.setSTATUSDESC("前端上送格式錯誤");

                        res = XmlUtil.toXML(rs);
                        return res;
                    } else {
                        if (!data.contains("<CHANNEL>") || !data.contains("<MSGID>") || !data.contains("<MSGID>") || !data.contains("<MSGID>")) {
                            SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                            SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
                            SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
                            SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                            msgrs.setHeader(header);
                            msgrs.setSvcRs(setNbBody(atmReqbody));
                            rsbody.setRs(msgrs);
                            rs.setBody(rsbody);

                            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                            header.setCHANNEL(atmReqheader.getCHANNEL());
                            header.setMSGID(atmReqheader.getMSGID());
                            header.setCLIENTDT(atmReqheader.getCLIENTDT());
                            header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
                            header.setSTATUSCODE("T001");
                            header.setSEVERITY("ERROR");
                            header.setSTATUSDESC("前端上送格式錯誤");

                            res = XmlUtil.toXML(rs);
                            return res;
                        } else {
                            nbData.setEj(getEj());
                            String txntype = "";
                            // Parse電文
                            nbData.setTxNbfepObject(parseFlatfile(data));
                            if(this.setNBMsgid(atmReqheader)){
                                SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
                                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
                                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                                msgrs.setHeader(header);
                                msgrs.setSvcRs(setNbBody(atmReqbody));
                                rsbody.setRs(msgrs);
                                rs.setBody(rsbody);

                                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                                header.setCHANNEL(atmReqheader.getCHANNEL());
                                header.setMSGID(atmReqheader.getMSGID());
                                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                                header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
                                header.setSTATUSCODE("T001");
                                header.setSEVERITY("ERROR");
                                header.setSTATUSDESC("前端上送格式錯誤");

                                res = XmlUtil.toXML(rs);
                                return res;
                            }
                            nbData.setMsgCtl(FEPCache.getMsgctrl(nbData.getMessageID()));
                            this.setChannel(nbData, channel);
                            // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                            logData.setMessageId(nbData.getMessageID());
                            logData.setChannel(channel);
                            if (nbData.getMsgCtl() == null) {
                                this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                                sendEMS(this.logContext);
                                return StringUtils.EMPTY;
                            }

                            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                            logData.setMessageFlowType(MessageFlow.Request);
                            logData.setMessage(data);
                            nbData.setAaName(nbData.getMsgCtl().getMsgctlAaName());
                            nbData.setTxStatus(nbData.getMsgCtl().getMsgctlStatus() == 1);
                            nbData.setTxRequestMessage(data);
                            nbData.setTxChannel(channel);
                            nbData.setMessageFlowType(MessageFlow.Request);
                            nbData.setLogContext(logData);
                            nbData.setEj(getEj());
                            ATMGeneral atmGeneral = new ATMGeneral();
                            nbData.setAtmtxObject(atmGeneral);
//nbData.getAtmtxObject().getRequest().setPICCBI55(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getICMARK());
//nbData.getAtmtxObject().getRequest().setPICCTACL(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC_LEN());
//nbData.getAtmtxObject().getRequest().setPICCTAC(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC());

                            LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                            // 根據MessageID決定要New那支AA
                            res = this.runAA(nbData.getMessageID());
                        }
                    }
                }
            } else {
                //INDATE,INTIME=NULL
                SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
                SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
                msgrs.setHeader(header);
                msgrs.setSvcRs(setNbBody(atmReqbody));
                rsbody.setRs(msgrs);
                rs.setBody(rsbody);

                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(atmReqheader.getCHANNEL());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
                header.setSTATUSCODE("T001");
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC("前端上送格式錯誤");

                res = XmlUtil.toXML(rs);
                return res;
            }

            return res;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (nbData != null) {
                logData.setMessageId(nbData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.NB);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            if ("LE".equals(fscode) || "LF".equals(fscode)) {
                res = getResVAStr(vaatmReqheader);
            } else {
                res = getResNBStr(atmReqheader);
            }
//            res = getResStr(FEPReturnCode.ParseTelegramError, nbData.getMessageID());
            return res;
        }
        finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(res);
            logMessage(Level.DEBUG, logData);
        }
    }

    public NBFEPGeneral parseFlatfile(String data) throws Exception {
        RCV_NB_GeneralTrans_RQ req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
        nbData.setTxNbfepObject(new NBFEPGeneral());
        nbData.getTxNbfepObject().setRequest(req);
        nbData.getTxNbfepObject().setResponse(null);

        return nbData.getTxNbfepObject();
    }

    public VAFEPGeneral parseFlatfileva(String data) throws Exception {
        RCV_VA_GeneralTrans_RQ req = deserializeFromXml(data, RCV_VA_GeneralTrans_RQ.class);
        nbData.setTxVafepObject(new VAFEPGeneral());
        nbData.getTxVafepObject().setRequest(req);
        nbData.getTxVafepObject().setResponse(null);

        return nbData.getTxVafepObject();
    }

    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processNbRequestData", NBData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, nbData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResNBStr(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {

        String ResStr = "";
        SEND_NB_GeneralTrans_RS ivrRs = new SEND_NB_GeneralTrans_RS();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        ivrRs.setBody(rsbody);

        header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
        header.setCHANNEL(nbheader.getCHANNEL());
        header.setMSGID(nbheader.getMSGID());
        header.setCLIENTDT(nbheader.getCLIENTDT());
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("NB發生exception");
        header.setTXNID(nbheader.getTXNID());

        ResStr = XmlUtil.toXML(ivrRs);
        return ResStr;
    }

    private String getResVAStr(RCV_VA_GeneralTrans_RQ.RCV_VA_GeneralTrans_RQ_Body_MsgRq_Header vaHeader) {

        String ResStr = "";
        SEND_VO_GeneralTrans_RS ivrRs = new SEND_VO_GeneralTrans_RS();
        SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body rsbody = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body();
        SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs();
        SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header();
        SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs();
        msgrs.setHeader(header);
        msgrs.setSvcRs(body);
        rsbody.setRs(msgrs);
        ivrRs.setBody(rsbody);

        header.setCLIENTTRACEID(vaHeader.getCLIENTTRACEID());
        header.setCHANNEL(vaHeader.getCHANNEL());
        header.setMSGID(vaHeader.getMSGID());
        header.setCLIENTDT(vaHeader.getCLIENTDT());
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short) 1, "NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("VA發生exception");

        ResStr = XmlUtil.toXML(ivrRs);
        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    private String runVA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processVARequestData", NBData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, nbData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    private boolean setNBMsgid(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader) throws Exception {
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header nbHeader = nbData.getTxNbfepObject().getRequest().getBody().getRq().getHeader();
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq nbSvcRq = nbData.getTxNbfepObject().getRequest().getBody().getRq().getSvcRq();
        String txntype = nbSvcRq.getTXNTYPE().trim();
        String sysstatHbkno = SysStatus.getPropertyValue().getSysstatHbkno();
        if ("IQ".equals(txntype) || "RV".equals(txntype)) {
            // RUN  NBINQ
            nbData.setMessageID("NBINQ");
            if ("LE".equals(fscode) || "LF".equals(fscode)) {
                // RUN  VAINQ
                nbData.setMessageID("VAINQ");
            }
        } else {
            if ("RQ".equals(txntype)) {
                switch (fscode) {
//                    case "LE":
//                        if ("03".equals(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getAEIPYTP())) {
//                            Npsunit npsunits = dbNPSUNIT.selectByPrimaryKey(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYUNTNO(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getTAXTYPE(), nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getSENDDATA().getPAYFEENO());
//                            if (npsunits != null) {
//                                String bkno = npsunits.getNpsunitBkno().substring(0, 3);
//                                if (StringUtils.equals(bkno,sysstatHbkno)) {
//                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
//                                } else {
//                                    nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
//                                }
//                            }
//                        } else {
//                            if (StringUtils.equals(aeipcrbk,sysstatHbkno)) {
//                                // 自行
//                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getFSCODE() + nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getPCODE() + "0");
//                            } else {
//                                nbData.setMessageID(nbData.getTxVafepObject().getRequest().getBody().getRq().getHeader().getMSGID());
//                            }
//                        }
//                        nbData.getAtmtxObject().getRequest().setPICCBI55(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getICMARK());
//                        nbData.getAtmtxObject().getRequest().setPICCTAC(nbData.getTxVafepObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC());
//                        break;
                    case "LF":
                        nbData.setMessageID(nbHeader.getMSGID());
                        break;
                    default:
                        String outbank = nbSvcRq.getTRNSFROUTBANK().length() > 3 ? nbSvcRq.getTRNSFROUTBANK().substring(0, 3) : nbSvcRq.getTRNSFROUTBANK();
                        String inbank = nbSvcRq.getTRNSFRINBANK().length() > 3 ? nbSvcRq.getTRNSFRINBANK().substring(0, 3) : nbSvcRq.getTRNSFRINBANK();
//                        nbData.getAtmtxObject().getRequest().setPICCBI55("202020202020202020202020202020");
                        if (StringUtils.equals(outbank, sysstatHbkno) && StringUtils.equals(outbank, inbank)) {
                            nbData.setMessageID(nbSvcRq.getFSCODE() + nbSvcRq.getPCODE() + "0");
                        } else {
                            nbData.setMessageID(nbHeader.getMSGID());
                        }
                }
            } else {
                //!=RQ
                return true;
            }
        }
        return false;
    }

    private SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs setNbBody(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq tita) {
        SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE(tita.getTXNTYPE());
        body.setHOSTACC_FLAG(" ");
        body.setTRANSAMT(new BigDecimal("0"));
        body.setTRANSFROUTBAL(new BigDecimal("0"));
        body.setTRANSOUTAVBL(new BigDecimal("0"));
        body.setTRANSAMTOUT(new BigDecimal("0"));
        body.setTRNSFROUTIDNO(" ");
        body.setTRNSFROUTNAME(" ");
        body.setTRNSFROUTBANK(" ");
        body.setTRNSFROUTACCNT(" ");
        body.setTRNSFRINBANK(" ");
        body.setTRNSFRINACCNT(" ");
        body.setCLEANBRANCHOUT(" ");
        body.setCLEANBRANCHIN(" ");
        body.setCUSTPAYFEE(new BigDecimal("0"));
        body.setFISCFEE(new BigDecimal("0"));
        body.setOTHERBANKFEE(new BigDecimal("0"));
        body.setCHAFEE_BRANCH(" ");
        body.setCHAFEEAMT(new BigDecimal("0"));
        body.setTRNSFRINNOTE(" ");
        body.setTRNSFROUTNOTE(" ");
        body.setPAYEREMAIL(" ");
        body.setCUSTOMERNATURE(" ");
        body.setTCBRTNCODE(" ");
        return body;
    }
}
