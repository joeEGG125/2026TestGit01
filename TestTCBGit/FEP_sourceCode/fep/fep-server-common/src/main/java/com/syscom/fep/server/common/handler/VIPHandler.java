package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.NBData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.*;
import com.syscom.fep.mybatis.ext.mapper.NpsbatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsdtlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.model.Npsbatch;
import com.syscom.fep.mybatis.model.Npsdtl;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import com.syscom.fep.vo.text.nb.NBFEPGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.SEND_NB_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VIPHandler extends HandlerBase {
    private NBData nbData;
    private String fscode;
    private LogData logData;
    private String FROMDATA;
    private String CHANNEL;
    private String batNo;
    private int threadNo;
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private NpsdtlExtMapper npsdtlExtMapper = SpringBeanFactoryUtil.getBean(NpsdtlExtMapper.class);
    private NpsbatchExtMapper npsbatchExtMapper = SpringBeanFactoryUtil.getBean(NpsbatchExtMapper.class);

    private BigDecimal charge;
    private String brch;
    private String chargeFlag;
    private String replyTo;

    private String uploadFileName;

    public String getChargeFlag() {
        return chargeFlag;
    }

    public void setChargeFlag(String chargeFlag) {
        this.chargeFlag = chargeFlag;
    }

    public String getBrch() {
        return brch;
    }

    public void setBrch(String brch) {
        this.brch = brch;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) throws Exception {
        return null;
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);
        String res = "";
        RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = new RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header();
        RCV_NB_GeneralTrans_RQ req = new RCV_NB_GeneralTrans_RQ();
        try {
            data = data.substring(data.indexOf("<"));

            //取得FEP電子日誌序號
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
            JSONObject header = new JSONObject(json3.get("Header").toString());

            //判斷電文屬於IMS或NB，以供後續處理用。
            FROMDATA = header.get("CLIENTTRACEID").toString().substring(3, 5);
            CHANNEL = header.get("CHANNEL").toString();
            fscode = json.get("FSCODE").toString();
            brch = nbData.getBrch();
            charge = nbData.getCharge();
            chargeFlag = nbData.getChargeFlag();
            String timeStart = "";
            req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
            RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = req.getBody().getRq().getSvcRq();

            //2.  檢核已被停止的交易，不需再處理
            if (StringUtils.equalsAny(CHANNEL, FEPChannel.NAM.getNameS())) {
                String W_BATCHNO = header.get("CLIENTTRACEID").toString().substring(5, 34);
                int W_SEQNO = Integer.parseInt(header.get("CLIENTTRACEID").toString().substring(34, 44));
                logData.setRemark("W_BATCHNO： "+ W_BATCHNO + " ,W_SEQNO： " + W_SEQNO );
                logMessage(logData);
                //交易處理結果：'':單筆交易已發送，00:交易成功，01:交易失敗，11:停止交易
                List<Npsdtl> npsdtl1 = npsdtlExtMapper.GetNPSDTLByBATNOResult(W_BATCHNO, W_SEQNO);
                if ( npsdtl1.isEmpty() ){
                    logData.setRemark("NPSDTL 狀態非已發送，CLIENTTRACEID："+ header.get("CLIENTTRACEID"));
                    logMessage(logData);
                    return null; //交易結束
                }
            }

            if (StringUtils.equalsAny(CHANNEL, FEPChannel.NETBANK.getNameS(), FEPChannel.NAM.getNameS())
                    || StringUtils.equals(FROMDATA, "NB")) {
                timeStart = atmReqbody.getINDATE() + atmReqbody.getINTIME();

                long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
                // 2024-11-21 Richard modified for 【Integer Overflow】
                // int diffseconds = (int) ((end - srt) / 1000);
                int diffseconds = MathUtil.safeGet((int) ((end - srt) / 1000));

                // 前端上送時間與FEP時間已超過90秒,該筆交易不處理
//                if (diffseconds > 90) {
//                    SEND_NB_GeneralTrans_RS rs = new SEND_NB_GeneralTrans_RS();
//                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body rsbody = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body();
//                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs();
//                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header nbheader = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_Header();
//                    SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_NB_GeneralTrans_RS.SEND_NB_GeneralTrans_RS_Body_MsgRs_SvcRs();
//                    msgrs.setHeader(nbheader);
//                    msgrs.setSvcRs(body);
//                    rsbody.setRs(msgrs);
//                    rs.setBody(rsbody);
//
//                    nbheader.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
//                    nbheader.setCHANNEL(atmReqheader.getCHANNEL());
//                    nbheader.setMSGID(atmReqheader.getMSGID());
//                    nbheader.setCLIENTDT(atmReqheader.getCLIENTDT());
//                    nbheader.setSYSTEMID("FEP");
//                    nbheader.setSTATUSCODE("0601");
//                    nbheader.setSEVERITY("ERROR");
//                    nbheader.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");
//
//                    res = XmlUtil.toXML(rs);
//
//                } else {

                nbData.setEj(getEj());
                String txntype = "";
                // Parse電文
                nbData.setTxNbfepObject(parseFlatfile(data));
                this.setNBMsgid();
                nbData.setMsgCtl(FEPCache.getMsgctrl(nbData.getMessageID()));
                if (nbData.getMsgCtl() == null) {
                    this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
                    sendEMS(this.logContext);
                    return StringUtils.EMPTY;
                }

                this.setChannel(nbData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
                logData.setMessageId(nbData.getMessageID());
                logData.setChannel(channel);
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
                LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
                // 根據MessageID決定要New那支AA
                res = this.runAA(nbData.getMessageID());
            }
            return null;
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
            res = "";

            res = getResNBStr(atmReqheader);

            return res;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(res);
            logMessage(Level.DEBUG, logData);

            //判斷是否已經全部處理完成
//            String count =  npsdtlExtMapper.getNPSDTLByBATNOforCountBy02(batNo,"02");
//            boolean isAllDone = Integer.valueOf(count != null ? count : "0") == 0;
//            List<Npsdtl> npsdtlsList = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
//            boolean isAllDone = true;
//            for(Npsdtl dtl : npsdtlsList){
//                String result = dtl.getNpsdtlResult();
//                if(StringUtils.equals(result, "02")){
//                    isAllDone = false;
//                }
//            }
//            if (isAllDone) {
//                if (StringUtils.equalsAny(FROMDATA, "NB", "IM")) {
//                    Npsbatch npsbatchforupd = npsbatchExtMapper.selectByPrimaryKey(npsbatchFileId, npsbatchTxDate, npsbatchBatchNo);
//                    if(npsbatchforupd.getNpsbatchRspTime() == null){
//                        Npsdtl npsdtlnew  = npsdtlExtMapper.GetNPSDTLByBATNO(batNo, threadNo).get(0);
//                        String stan = getLogContext().getStan();
//                        String ejno = npsdtlnew.getNpsdtlEjfno();
//                        this.updateNpsBatchforRspTime(npsbatchforupd,stan,ejno);
//                        logData.setMessage("回傳前端訊息 STAN：" + stan + ", EJ:" + ejno);
//                        logMessage(Level.DEBUG, logData);
//                        res = prepareReq(logData, npsbatch);
//                        return res;
//                    }
//                }  else {
//                    logData.setMessage("NOT NB OR IMS . Can't reply to NB RS or IMS.");
//                    logMessage(Level.DEBUG, logData);
//                }
//            }
        }
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return false;
    }

    private String getResNBStr(RCV_NB_GeneralTrans_RQ.RCV_NB_GeneralTrans_RQ_Body_MsgRq_Header nbheader) {

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

        header.setCLIENTTRACEID(nbheader.getCLIENTTRACEID());
        header.setCHANNEL(nbheader.getCHANNEL());
        header.setMSGID(nbheader.getMSGID());
        header.setCLIENTDT(nbheader.getCLIENTDT());
        SysconfExtMapper sysconfExtMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        header.setSYSTEMID(sysconfExtMapper.selectByPrimaryKey((short)1,"NATM_SYSTEMID_FEP").getSysconfValue());
        header.setSTATUSCODE("T099");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");

        ResStr = XmlUtil.toXML(ivrRs);
        return ResStr;
    }

    private void setNBMsgid() throws Exception {
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
            }
        }
    }

    public NBFEPGeneral parseFlatfile(String data) throws Exception {
        RCV_NB_GeneralTrans_RQ req = deserializeFromXml(data, RCV_NB_GeneralTrans_RQ.class);
        nbData.setTxNbfepObject(new NBFEPGeneral());
        nbData.getTxNbfepObject().setRequest(req);
        nbData.getTxNbfepObject().setResponse(null);

        return nbData.getTxNbfepObject();
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

    private boolean updateNPSDTL(Npsdtl npsdtl, Npsbatch npsbatch, String repXml) {
        try {
            SEND_NB_GeneralTrans_RS nbDataRes = deserializeFromXml(repXml, SEND_NB_GeneralTrans_RS.class);

            String hostaccFlag = nbDataRes.getBody().getRs().getSvcRs().getHOSTACC_FLAG();
            if (nbDataRes.getBody().getRs().getHeader().getSEVERITY().equals("INFO")
                    && StringUtils.isNotBlank(hostaccFlag)
                    && hostaccFlag.equals("Y")) {
                npsdtl.setNpsdtlResult("00"); // 成功
            } else {
                npsdtl.setNpsdtlResult("01"); // 失敗
            }
            npsdtl.setNpsdtlTbsdy(nbDataRes.getBody().getRs().getSvcRs().getACCTDATE());
            npsdtl.setNpsdtlReplyCode(nbDataRes.getBody().getRs().getHeader().getSTATUSCODE());
            if (StringUtils.isBlank(npsdtl.getNpsdtlErrMsg())) {
                npsdtl.setNpsdtlErrMsg(nbDataRes.getBody().getRs().getHeader().getSTATUSDESC());
            }
            npsdtl.setNpsdtlEjfno(nbDataRes.getBody().getRs().getSvcRs().getFEP_EJNO());
            npsdtl.setNpsdtlTbsdyFisc(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
            npsdtl.setNpsdtlStan(nbDataRes.getBody().getRs().getSvcRs().getTXNSTAN());
            if (nbDataRes.getBody().getRs().getSvcRs().getCUSTPAYFEE() != null) {
                npsdtl.setNpsdtlFee(nbDataRes.getBody().getRs().getSvcRs().getCUSTPAYFEE());
            }

            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ") && charge != null) {
                npsdtl.setNpsdtlHostCharge(charge.intValue());
            } else {
                npsdtl.setNpsdtlHostCharge(0);
            }
            if (nbDataRes.getBody().getRs().getSvcRs().getTXNTYPE().equals("RQ") && StringUtils.isNotBlank(brch)) {
                npsdtl.setNpsdtlHostBrch(brch);
            } else {
                npsdtl.setNpsdtlHostBrch(npsbatch.getNpsbatchBranch());
            }

            npsdtl.setNpsdtlCbsRc(nbDataRes.getBody().getRs().getSvcRs().getTCBRTNCODE());
            if (StringUtils.isNotBlank(chargeFlag)) {
                npsdtl.setNpsdtlHostChargeFlag(chargeFlag);
            }
            //            npsdtl.setNpsdtlThreadNo(threadNo);
            if (npsdtlExtMapper.updateByPrimaryKeySelective(npsdtl) < 1) {
                logContext.setRemark("更新NPSDTL失敗");
                logMessage(Level.DEBUG, logData);
                sendEMS(logContext);
                return false;
            } else {
                int okcnt = npsbatch.getNpsbatchOkCnt();
                int feecnt = npsbatch.getNpsbatchFeeCnt();
                int doTOTcnt = npsbatch.getNpsbatchFeeCnt();
                int Failcnt = npsbatch.getNpsbatchFailCnt();
                BigDecimal okamt = npsbatch.getNpsbatchOkAmt();
                BigDecimal failamt = npsbatch.getNpsbatchFailAmt();
                BigDecimal doTOTamt = npsbatch.getNpsbatchDoTotAmt();
                BigDecimal feeamt = npsbatch.getNpsbatchFeeAmt();

                if (npsdtl.getNpsdtlResult().equals("00")) {
                    okcnt += 1;
                    feecnt += 1;
                    doTOTcnt += 1;
                    okamt = okamt.add(npsdtl.getNpsdtlTxAmt());
                    doTOTamt = doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                    feeamt = feeamt.add(npsdtl.getNpsdtlFee());
                    npsbatch.setNpsbatchOkCnt(okcnt);
                    npsbatch.setNpsbatchFeeCnt(feecnt);
                    npsbatch.setNpsbatchDoTotCnt(doTOTcnt);
                    npsbatch.setNpsbatchOkAmt(okamt);
                    npsbatch.setNpsbatchDoTotAmt(doTOTamt);
                    npsbatch.setNpsbatchFeeAmt(feeamt);
                } else {
                    Failcnt += 1;
                    doTOTcnt += 1;
                    failamt = failamt.add(npsdtl.getNpsdtlTxAmt());
                    doTOTamt = doTOTamt.add(npsdtl.getNpsdtlTxAmt());
                    npsbatch.setNpsbatchFailCnt(Failcnt);
                    npsbatch.setNpsbatchDoTotCnt(doTOTcnt);
                    npsbatch.setNpsbatchFailAmt(failamt);
                    npsbatch.setNpsbatchDoTotAmt(doTOTamt);
                }
                if (npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch) < 1) {
                    logContext.setRemark("更新npsbatch失敗");
                    logMessage(Level.DEBUG, logData);
                    sendEMS(logContext);
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean updateNpsBatchforRspTime(Npsbatch npsbatch, String stan, String ej){
        try {
            npsbatch.setNpsbatchRspTime(new Date());
            npsbatch.setNpsbatchRspStan(stan);
            npsbatch.setNpsbatchRspEjfno(Integer.valueOf(ej));
            if (npsbatchExtMapper.updateByPrimaryKeySelective(npsbatch) < 1) {
                logContext.setRemark("更新npsbatch失敗");
                logMessage(Level.DEBUG, logData);
                sendEMS(logContext);
                return false;
            }
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String prepareReq(LogData logData, Npsbatch npsbatch) {
        StringBuilder sLine = null;
        String queue = "";
        try {
            List<Npsdtl> alld = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);
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
            return null;
        }
    }


    private Boolean prepareTxtFile(LogData logData, Npsbatch npsbatch) {
        //9. 產生整批轉即時回饋文字檔
        StringBuilder sLine = null;
        try {
            List<Npsdtl> alld = npsdtlExtMapper.GetNPSDTLByBATNOforAll(batNo);

            uploadFileName = npsbatch.getNpsbatchFileId().substring(0, npsbatch.getNpsbatchFileId().length() - 5) + "R.txt";
            String uploadFilePath = CMNConfig.getInstance().getBatchOutputPath() + uploadFileName;
            File file = new File(uploadFilePath);
            if (file.exists()) {
                file.delete();
            }
            OutputStream os = new FileOutputStream(uploadFilePath);
            OutputStreamWriter writer = null;
            writer = new OutputStreamWriter(os, "Big5");

            /* 首筆 */
            sLine = new StringBuilder("");
            sLine.append("11");
            String date = npsbatch.getNpsbatchTxDate();
            date = CalendarUtil.adStringToROCString(date);
            sLine.append(date);
            sLine.append(StringUtils.leftPad(npsbatch.getNpsbatchBatchNo(), 13, "0"));
            sLine.append(StringUtils.leftPad(npsbatch.getNpsbatchBranch(), 4, "0"));
            sLine.append(StringUtils.repeat(" ", 204));
            sLine.append(System.getProperty("line.separator"));
            writer.write(sLine.toString());
            writer.flush();
            sLine = null;


            /* 明細筆 */
            for (int i = 0; i < alld.size(); i++) {
                writer = new OutputStreamWriter(os, "Big5");
                sLine = new StringBuilder("");
                sLine.append("12");
                String detaildate = npsbatch.getNpsbatchTxDate();
                detaildate = CalendarUtil.adStringToROCString(detaildate);
                sLine.append(detaildate);
                sLine.append(StringUtils.rightPad(npsbatch.getNpsbatchBatchNo(), 13, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlSeqNo().toString(), 10, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlAtmno(), 5, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlTerminalid(), 8, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTxTime(), 6, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlBusinessUnit(), 8, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPaytype(), 5, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPayno(), 4, " "));
                Double txAmt = Double.valueOf(alld.get(i).getNpsdtlTxAmt().toString()) * 100;
                int txAmtt = txAmt.intValue();
                sLine.append(StringUtils.leftPad(String.valueOf(txAmtt), 11, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlIdno(), 11, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlReconSeq(), 16, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTroutBkno7(), 7, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTroutActno(), 16, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTrinBkno(), 3, "0"));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlTrinActno(), 16, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlAllowT1(), 1, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlDueDate(), 8, " "));
                sLine.append(StringUtils.rightPad("", 23, " "));
                sLine.append(StringUtils.rightPad("006" + alld.get(i).getNpsdtlStan(), 10, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlPcode(), 4, " "));
                if (alld.get(i).getNpsdtlTroutBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())
                        && alld.get(i).getNpsdtlTrinBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
                    sLine.append("CB2W");
                } else {
                    sLine.append("AB3W");
                }
                String datetbsdy = CalendarUtil.adStringToROCString(alld.get(i).getNpsdtlTbsdy()); //西元轉民國
                sLine.append(StringUtils.leftPad(datetbsdy, 7, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlHostBrch(), 4, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlCbsRc(), 3, " "));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlReplyCode(), 4, " "));
                sLine.append(StringUtils.leftPad(alld.get(i).getNpsdtlHostCharge().toString(), 2, "0"));
                sLine.append(StringUtils.rightPad(alld.get(i).getNpsdtlHostChargeFlag(), 1, " "));
                sLine.append(StringUtils.rightPad("", 11, " "));
                sLine.append(System.getProperty("line.separator"));


                writer.write(sLine.toString());
                writer.flush();
                sLine = null;
            }

            npsbatch.setUpdateTime(new Date());
            npsbatch.setNpsbatchResult("00");


            /* 尾筆 */
            writer = new OutputStreamWriter(os, "Big5");
            sLine = new StringBuilder("");
            sLine.append("19");
            String endDate = npsbatch.getNpsbatchTxDate();
            endDate = CalendarUtil.adStringToROCString(endDate);
            sLine.append(endDate);
            sLine.append(StringUtils.rightPad(npsbatch.getNpsbatchBatchNo(), 13, " "));
            sLine.append(StringUtils.leftPad(npsbatch.getNpsbatchTotCnt().toString(), 6, "0"));
            Double tot = Double.valueOf(npsbatch.getNpsbatchTotAmt().toString()) * 100;
            int tott = tot.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(tott), 14, "0"));
            sLine.append(StringUtils.leftPad(npsbatch.getNpsbatchOkCnt().toString(), 6, "0"));
            Double ok = Double.valueOf(npsbatch.getNpsbatchOkAmt().toString()) * 100;
            int okt = ok.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(okt), 14, "0"));
            sLine.append(StringUtils.leftPad(npsbatch.getNpsbatchFailCnt().toString(), 6, "0"));
            Double fail = Double.valueOf(npsbatch.getNpsbatchFailAmt().toString()) * 100;
            int failt = fail.intValue();
            sLine.append(StringUtils.leftPad(String.valueOf(failt), 14, "0"));
            sLine.append(StringUtils.repeat(" ", 148));
            sLine.append(System.getProperty("line.separator"));
            writer.write(sLine.toString());
            writer.flush();
            sLine = null;


            //10-1. 上傳檔案產生FEP通知主機收檔_全繳扣款結果檔電文
            String fileName = uploadFileName;
            String shellScript = "/mft/recvfile.sh";
            String remotePath = "D:\\TCBFTP\\AP1T\\PU\\";
            String activation = "put";
            String[] command = {"sudo", "-u", "mftuser", shellScript, fileName, remotePath, activation};

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logData.setMessage(line);
                    logMessage(Level.DEBUG, logData);
                }

                // 等待執行結果
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    logData.setProgramName(this.ProgramName);
                    logData.setMessage("upload fail!");
                    logMessage(Level.DEBUG, logData);
                    return false;
                }
            } catch (Exception e) {
                logData.setProgramName(this.ProgramName);
                logData.setMessage(e.toString());
                logMessage(Level.DEBUG, logData);
                return false;
            }
            return true;
        } catch (Exception ex) {
            logData.setProgramName(this.ProgramName);
            logData.setMessage(ex.toString());
            logMessage(Level.DEBUG, logData);
            return false;
        }
    }

    private String prepareforReq(Npsbatch npsbatch) {
        StringBuilder sLine = null;
        String response = "";
        try {
            //HEADER (LEN=77)
            response = response + StringUtils.rightPad("", 2, " ");    //LL
            response = response + StringUtils.rightPad("", 2, " ");     //ZZ
            response = response + "IPUF";   //TRAN-CODE: IPUF
            response = response + "PUT1";   //交易代號TD-CODE: IPUF
            response = response + StringUtils.rightPad("", 4, " ");     //分行代號: 4空白
            response = response + StringUtils.rightPad("", 6, " ");     //交易序號: 6空白
            response = response + StringUtils.rightPad("", 10, " ");    //交易查詢序號: 10空白
            response = response + "0000";       //交易處理結果: 0000
            response = response + "Y";          //控制碼: Y
            response = response + StringUtils.rightPad("", 3, " ");     //RESOURCE CHANNEL: 空白
            Date date = new Date();
            String ROCdate = new SimpleDateFormat("yyyyMMdd").format(date);
            response = response + (Integer.parseInt(ROCdate.substring(0, 4)) - 1911) + ROCdate.substring(4, 6) + ROCdate.substring(6, 8);         //交易日期: 系統日期民國年YYYMMMDD
            response = response + new SimpleDateFormat("HHmmss").format(date);         //交易時間:
            response = response + StringUtils.rightPad("", 24, " ");          //MQ MSG-ID: 24空白

            response = response + "1";         //交易種類: "1"
            response = response + uploadFileName;         //送檔檔名: 檔名
            response = response + StringUtils.leftPad(String.valueOf(npsbatch.getNpsbatchOkCnt()), 10, "0");         //成功總筆數: NPSBATCH_OK_CNT
            Double ok = Double.valueOf(npsbatch.getNpsbatchOkAmt().toString()) * 100;
            int oklt = ok.intValue();
            response = response + StringUtils.leftPad(String.valueOf(oklt), 16, "0");         //成功總金額: NPSBATCH_OK_ATM
            response = response + StringUtils.leftPad(String.valueOf(npsbatch.getNpsbatchFailCnt()), 10, "0");         //失敗總筆數: NPSBATCH_FAIL_CNT
            Double fail = Double.valueOf(npsbatch.getNpsbatchFailAmt().toString()) * 100;
            int failt = fail.intValue();
            response = response + StringUtils.leftPad(String.valueOf(failt), 16, "0");         //失敗總金額: NPSBATCH_FAIL_ATM


            response = response + StringUtils.rightPad("", 27, " ");         //FILLER 保留欄位


            return response;
        } catch (Exception ex) {
            return null;
        }
    }


}
