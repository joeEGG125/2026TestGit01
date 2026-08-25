package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.HCEData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.hce.HCEGeneral;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ.RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS.SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HCEHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private HCEData hceData;
    private String _timeFormat = "yyyy/MM/ddTHH:mm:ss:sss";
    private LogData logData;
    private Calendar receivedTime;
    private SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    public void setReceivedTime(Calendar receivedTime) {
        this.receivedTime = receivedTime;
    }

    public HCEHandler() {
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        // data = data.Replace("\r\n", string.Empty);

        RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = null;
        String hceRes = "";

        try {
            //前面多了奇怪的符號 ex:�<?xml version="1.0" encoding="utf-8"?><SOAP-ENV:Envelope><SOAP-ENV:Header/>
            data = data.substring(data.indexOf("<"));

            LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
            ATMGeneral atmGeneral = new ATMGeneral();


            // FEPResponse FEPRsp = null;
            if (getEj() == 0) {
                setEj(TxHelper.generateEj());
            }

            if (getLogContext() == null) {
                LogData tempVar = new LogData();
                tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                tempVar.setSubSys(SubSystem.HCE);
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
            logData.setSubSys(SubSystem.HCE);
            logData.setChannel(channel);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(data);
            logData.setEj(getEj());
            logData.setChannel(channel);
            logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
            logData.setMessageFlowType(MessageFlow.Request);
            logData.setMessage(data);
            logMessage(Level.DEBUG, logData);


            hceData = new HCEData();
            RCV_HCE_GeneralTrans_RQ req = deserializeFromXml(data, RCV_HCE_GeneralTrans_RQ.class);
            atmReqheader = req.getBody().getRq().getHeader();
            RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = req.getBody().getRq().getSvcRq();
            String timeStart = "";
            SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
            SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
            SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
            if (FEPChannel.HCE.equals(channel) || FEPChannel.HCA.equals(channel)) {
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
                    //前端上送時間與FEP時間已超過90秒,該筆交易不處理
                    if ((diffseconds > 90 )  && "RQ".equals(atmReqbody.getTXNTYPE().trim())) {
                        //SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
                        //SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
                        //SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
                        //SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
                        //SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
                        msgrs.setHeader(header);
                        msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                        rsbody.setRs(msgrs);
                        hceRs.setBody(rsbody);

                        header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                        header.setCHANNEL(atmReqheader.getCHANNEL());
                        header.setMSGID(atmReqheader.getMSGID());
                        header.setCLIENTDT(atmReqheader.getCLIENTDT());
                        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                        header.setSTATUSCODE("T126");
                        header.setSEVERITY("ERROR");
                        header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");

                        hceRes = XmlUtil.toXML(hceRs);
                        return hceRes;

                    } else {
                        if (!data.contains("<CHANNEL>") || !data.contains("<MSGID>") || !data.contains("<MSGID>") || !data.contains("<MSGID>")) {
                            msgrs.setHeader(header);
                            msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                            rsbody.setRs(msgrs);
                            hceRs.setBody(rsbody);

                            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                            header.setCHANNEL(atmReqheader.getCHANNEL());
                            header.setMSGID(atmReqheader.getMSGID());
                            header.setCLIENTDT(atmReqheader.getCLIENTDT());
                            header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                            header.setSTATUSCODE("T001");
                            header.setSEVERITY("ERROR");
                            header.setSTATUSDESC("前端上送格式錯誤");

                            hceRes = XmlUtil.toXML(hceRs);
                            return hceRes;
                        } else {
                            String TRNSFROUTACCNT = "";
                            if (atmReqbody!=null && atmReqbody.getTRNSFROUTACCNT() != null) {
                                TRNSFROUTACCNT = atmReqbody.getTRNSFROUTACCNT();
                            }
                            String TRNSFRINACCNT = "";
                            if (atmReqbody.getTRNSFRINACCNT()!=null) {
                                TRNSFRINACCNT = atmReqbody.getTRNSFRINACCNT();
                            }
                            if(TRNSFROUTACCNT.length()>16 || TRNSFRINACCNT.length()>16) {
                                msgrs.setHeader(header);
                                msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                                rsbody.setRs(msgrs);
                                hceRs.setBody(rsbody);

                                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                                header.setCHANNEL(atmReqheader.getCHANNEL());
                                header.setMSGID(atmReqheader.getMSGID());
                                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                                header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                                header.setSTATUSCODE("T001"); ////處理失敗
                                header.setSEVERITY("ERROR");
                                header.setSTATUSDESC("前端上送格式錯誤");

                                hceRes = XmlUtil.toXML(hceRs);
                                return hceRes;
                            } else {
                                hceData.setEj(getEj());
                                // Parse電文
                                hceData.setTxObject(parseFlatfile(data));

                                if (FEPChannel.HCE.equals(channel) || FEPChannel.HCA.equals(channel)) {
                                    RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header headers = hceData.getTxObject().getRequest().getBody().getRq().getHeader();
                                    RCV_HCE_GeneralTrans_RQ_Body_MsgRq_SvcRq bodys = hceData.getTxObject().getRequest().getBody().getRq().getSvcRq();
//RCV_HCE_GeneralTrans_RQ hcereq = deserializeFromXml(StringUtil.fromHex(req.getSvcRq().getRq()), RCV_HCE_GeneralTrans_RQ.class);

                                    if("IQ".equals(bodys.getTXNTYPE().trim()) || "RV".equals(bodys.getTXNTYPE().trim())) {
                                        //RUN  HCEINQ
                                        hceData.setMessageID("HCEINQ");
                                    } else {
                                        if("RQ".equals(bodys.getTXNTYPE().trim())) {
                                            if ("2500".equals(bodys.getPCODE())) {
                                                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bodys.getTRNSFROUTBANK().substring(0, 3))) {
                                                    //自行
                                                    hceData.setMessageID(headers.getMSGID() + "0"); /* FSCODE + PCODE+ “0” */
                                                } else {
                                                    hceData.setMessageID(headers.getMSGID());
                                                }
                                            } else {
                                                if (SysStatus.getPropertyValue().getSysstatHbkno().equals(bodys.getTRNSFRINBANK().substring(0, 3))
                                                        && SysStatus.getPropertyValue().getSysstatHbkno().equals(bodys.getTRNSFROUTBANK().substring(0, 3))) {
                                                    //自行
                                                    hceData.setMessageID(headers.getMSGID() + "0"); /* FSCODE + PCODE+ “0” */
                                                } else {
                                                    hceData.setMessageID(headers.getMSGID());
                                                }
                                            }
                                        }else {
                                            msgrs.setHeader(header);
                                            msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                                            rsbody.setRs(msgrs);
                                            hceRs.setBody(rsbody);

                                            header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                                            header.setCHANNEL(atmReqheader.getCHANNEL());
                                            header.setMSGID(atmReqheader.getMSGID());
                                            header.setCLIENTDT(atmReqheader.getCLIENTDT());
                                            header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                                            header.setSTATUSCODE("T001"); ////處理失敗
                                            header.setSEVERITY("ERROR");
                                            header.setSTATUSDESC("前端上送格式錯誤");

                                            hceRes = XmlUtil.toXML(hceRs);
                                            return hceRes;
                                        }
                                    }
                                }

                                hceData.setMsgCtl(FEPCache.getMsgctrl(hceData.getMessageID()));
                                this.setChannel(hceData, channel);
                                // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                                if (hceData.getMsgCtl() == null) {
                                    this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                                    sendEMS(this.logContext);
                                    return StringUtils.EMPTY;
                                }

                                hceData.setAaName(hceData.getMsgCtl().getMsgctlAaName());
                                hceData.setTxStatus(hceData.getMsgCtl().getMsgctlStatus() == 1);
                                hceData.setTxRequestMessage(data);
                                hceData.setTxChannel(channel);
                                hceData.setMessageFlowType(MessageFlow.Request);
                                hceData.setLogContext(logData);
                                hceData.setAtmtxObject(atmGeneral);

                                logData.setMessageId(hceData.getMessageID());

                                String IC_TAC = hceData.getTxObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC();
                                String IC_TAC_LEN = hceData.getTxObject().getRequest().getBody().getRq().getSvcRq().getIC_TAC_LEN();
                                String PICCTAC = IC_TAC_LEN + (IC_TAC.length() >= 16 ? IC_TAC.substring(0, 16) : IC_TAC);

                                hceData.getAtmtxObject().getRequest().setPICCBI55(hceData.getTxObject().getRequest().getBody().getRq().getSvcRq().getICMARK());
                                hceData.getAtmtxObject().getRequest().setPICCTAC(PICCTAC);

                                LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                                // 根據MessageID決定要New那支AA
                                hceRes = this.runAA(hceData.getMessageID());
                            }
                        }
                    }
                } else {
                    //INDATE,INTIME = NULL
                    msgrs.setHeader(header);
                    msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                    rsbody.setRs(msgrs);
                    hceRs.setBody(rsbody);

                    header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                    header.setCHANNEL(atmReqheader.getCHANNEL());
                    header.setMSGID(atmReqheader.getMSGID());
                    header.setCLIENTDT(atmReqheader.getCLIENTDT());
                    header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                    header.setSTATUSCODE("T001"); ////處理失敗
                    header.setSEVERITY("ERROR");
                    header.setSTATUSDESC("前端上送格式錯誤");

                    hceRes = XmlUtil.toXML(hceRs);
                    return hceRes;
                }
            } else {
                //channel != HCE
                msgrs.setHeader(header);
                msgrs.setSvcRs(setBody(atmReqbody.getTXNTYPE()));
                rsbody.setRs(msgrs);
                hceRs.setBody(rsbody);

                header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
                header.setCHANNEL(atmReqheader.getCHANNEL());
                header.setMSGID(atmReqheader.getMSGID());
                header.setCLIENTDT(atmReqheader.getCLIENTDT());
                header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
                header.setSTATUSCODE("T001"); ////處理失敗
                header.setSEVERITY("ERROR");
                header.setSTATUSDESC("前端上送格式錯誤");

                hceRes = XmlUtil.toXML(hceRs);
                return hceRes;
            }

            return hceRes;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (hceData != null) {
                logData.setMessageId(hceData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.HCE);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            hceRes = getResStr(FEPReturnCode.ParseTelegramError, hceData.getMessageID(), atmReqheader);
            return hceRes;
        }
        finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(hceRes);
            logMessage(Level.DEBUG, logData);
        }
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    public HCEGeneral parseFlatfile(String data) throws Exception {
        RCV_HCE_GeneralTrans_RQ req = deserializeFromXml(data, RCV_HCE_GeneralTrans_RQ.class);
        hceData.setTxObject(new HCEGeneral());
        hceData.getTxObject().setRequest(req);
        hceData.getTxObject().setResponse(null);

        return hceData.getTxObject();
    }

    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processHceRequestData", HCEData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, hceData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }


    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResStr(FEPReturnCode code, String MSGID, RCV_HCE_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader) {
        String ResStr = "";
        SEND_HCE_GeneralTrans_RS hceRs = new SEND_HCE_GeneralTrans_RS();
        SEND_HCE_GeneralTrans_RS_Body rsbody = new SEND_HCE_GeneralTrans_RS_Body();
        SEND_HCE_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_HCE_GeneralTrans_RS_Body_MsgRs();
        SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_Header();
        msgrs.setHeader(header);
        msgrs.setSvcRs(setBody("RQ"));
        rsbody.setRs(msgrs);
        hceRs.setBody(rsbody);

        header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
        header.setCHANNEL(atmReqheader.getCHANNEL());
        header.setMSGID(atmReqheader.getMSGID());
        header.setCLIENTDT(atmReqheader.getCLIENTDT());
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");

        ResStr = XmlUtil.toXML(hceRs);
        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }

    private SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs setBody(String txnType) {
        SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_HCE_GeneralTrans_RS_Body_MsgRs_SvcRs();
        body.setOUTDATE(" ");
        body.setOUTTIME(" ");
        body.setFEP_EJNO(" ");
        body.setTXNSTAN(" ");
        body.setCUSTOMERID(" ");
        body.setTXNTYPE(txnType);
        body.setFSCODE(" ");
        body.setACCTDATE(" ");
        body.setHOSTACC_FLAG(" ");
        body.setHOSTRVS_FLAG(" ");
        body.setTRANSAMT(new BigDecimal("0"));
        body.setTRANSFROUTBAL(new BigDecimal("0"));
        body.setTRANSOUTAVBL(new BigDecimal("0"));
        body.setTRNSFROUTBANK(" ");
        body.setTRNSFROUTACCNT(" ");
        body.setCLEANBRANCHOUT(" ");
        body.setTRNSFRINBANK(" ");
        body.setTRNSFRINACCNT(" ");
        body.setCLEANBRANCHIN(" ");
        body.setCUSTPAYFEE(new BigDecimal("0"));
        body.setTCBRTNCODE(" ");
        return body;
    }

}
