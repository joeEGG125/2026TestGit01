package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.aa.IVRData;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.ivr.IVRGeneral;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IVRHandler extends HandlerBase {
    private String ProgramName = StringUtils.join(this.getClass().getSimpleName(), ".");
    private IVRData ivrData;
    private String _timeFormat = "yyyy/MM/ddTHH:mm:ss:sss";
    private LogData logData;
    private String atmSeq;

    public IVRHandler() {
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    @Override
    public String dispatch(FEPChannel channel, String data) {
        // data = data.Replace("\r\n", string.Empty);

        RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader = null;
        String ivrRes = "";

        try {
            // 前面多了奇怪的符號 ex:�<?xml version="1.0" encoding="utf-8"?><SOAP-ENV:Envelope><SOAP-ENV:Header/>
            data = data.substring(data.indexOf("<"));

            LogHelperFactory.getTraceLogger().trace("Dispatch rcv " + data);

            // FEPResponse FEPRsp = null;
            if (getEj() == 0) {
                setEj(TxHelper.generateEj());
            }

            if (getLogContext() == null) {
                LogData tempVar = new LogData();
                tempVar.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                tempVar.setSubSys(SubSystem.IVR);
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
            logData.setSubSys(SubSystem.IVR);
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

            ivrData = new IVRData();
            RCV_VO_GeneralTrans_RQ req = deserializeFromXml(data, RCV_VO_GeneralTrans_RQ.class);
            atmReqheader = req.getBody().getRq().getHeader();
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_SvcRq atmReqbody = req.getBody().getRq().getSvcRq();
            RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_PAYDATA atmReqpay = req.getBody().getRq().getSvcRq().getPAYDATA();
            // //3.	檢核若前端上送時間與FEP時間已超過90秒,該筆交易不處理(if need )*/
            // String timeStart = "";
            // /*ID+ACCT 全繳不用檢核*/
            // if (FEPChannel.IVR.equals(channel)) {
            //     if (StringUtils.isNotBlank(atmReqbody.getINDATE())
            //             && StringUtils.isNotBlank(atmReqbody.getINTIME())) {
            //         timeStart = atmReqbody.getINDATE() + atmReqbody.getINTIME();
            //
            //         long srt = FormatUtil.parseDataTime(timeStart, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
            //         long end = FormatUtil.parseDataTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN).getTime();
            //         int diffseconds = (int) ((end - srt) / 1000);
            //         /*回覆前端交易失敗0601, 合庫三碼代號: 126 */
            //         //前端上送時間與FEP時間已超過90秒,該筆交易不處理
            //         SEND_VO_GeneralTrans_RS ivrRs = new SEND_VO_GeneralTrans_RS();
            //         SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body rsbody = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body();
            //         SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs msgrs = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs();
            //         SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header header = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_Header();
            //         SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs body = new SEND_VO_GeneralTrans_RS.SEND_VO_GeneralTrans_RS_Body_MsgRs_SvcRs();
            //         if (diffseconds < 90) {
            //             msgrs.setHeader(header);
            //             msgrs.setSvcRs(body);
            //             rsbody.setRs(msgrs);
            //             ivrRs.setBody(rsbody);
            //
            //             header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            //             header.setCHANNEL(atmReqheader.getCHANNEL());
            //             header.setMSGID(atmReqheader.getMSGID());
            //             header.setCLIENTDT(atmReqheader.getCLIENTDT());
            //             header.setSYSTEMID("FEP");
            //             header.setSTATUSCODE("0601");
            //             header.setSEVERITY("ERROR");
            //             header.setSTATUSDESC("前端上送時間與FEP時間已超過90秒,該筆交易不處理");
            //
            //             ivrRes = XmlUtil.toXML(ivrRes);
            //
            //         } else {
            //             if (!data.contains("<CHANNEL>") || !data.contains("<MSGID>") || !data.contains("<MSGID>") || !data.contains("<MSGID>")) {
            //
            //                 msgrs.setHeader(header);
            //                 msgrs.setSvcRs(body);
            //                 rsbody.setRs(msgrs);
            //                 ivrRs.setBody(rsbody);
            //
            //                 header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
            //                 header.setCHANNEL(atmReqheader.getCHANNEL());
            //                 header.setMSGID(atmReqheader.getMSGID());
            //                 header.setCLIENTDT(atmReqheader.getCLIENTDT());
            //                 header.setSYSTEMID("FEP");
            //                 header.setSTATUSCODE("2999");
            //                 header.setSEVERITY("ERROR");
            //                 header.setSTATUSDESC("前端上送格式錯誤");
            //
            //                 ivrRes = XmlUtil.toXML(ivrRes);
            //             } else {
            //                 ivrData.setEj(getEj());
            //                 // Parse電文
            //                 ivrData.setTxObject(parseFlatfile(data));
            //
            //                 if ("RQ".equals(atmReqbody.getTXNTYPE().trim())) {
            //                     if (SysStatus.getPropertyValue().getSysstatHbkno().equals(atmReqbody.getTRNSFRINBANK())) {
            //                         ivrData.setMessageID(atmReqbody.getFSCODE() + atmReqbody.getPCODE() + "0");
            //                     } else {
            //                         ivrData.setMessageID(atmReqbody.getFSCODE() + atmReqbody.getPCODE());
            //                     }
            //                 }
            //
            //                 ivrData.setMsgCtl(FEPCache.getMsgctrl(ivrData.getMessageID()));
            //                 if (ivrData.getMsgCtl() == null) {
            //                     this.logContext.setReturnCode(IOReturnCode.MSGCTLNotFound);
            //                     sendEMS(this.logContext);
            //                     return StringUtils.EMPTY;
            //                 }
            //
            //                 this.setChannel(ivrData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
            //                 ATMGeneral atmGeneral = new ATMGeneral();
            //                 ivrData.setAtmtxObject(atmGeneral);
            //                 ivrData.getAtmtxObject().getRequest().setPICCBI55("202020202020202020202020202020");
            //                 ivrData.setAaName(ivrData.getMsgCtl().getMsgctlAaName());
            //                 ivrData.setTxStatus(ivrData.getMsgCtl().getMsgctlStatus() == 1);
            //                 ivrData.setTxRequestMessage(data);
            //                 ivrData.setTxChannel(channel);
            //                 ivrData.setMessageFlowType(MessageFlow.Request);
            //                 ivrData.setLogContext(logData);
            //                 logData.setMessageId(ivrData.getMessageID());
            //                 LogHelperFactory.getTraceLogger().trace("根據MessageID決定要New那支AA");
            //                 // 根據MessageID決定要New那支AA
            //                 ivrRes = this.runAA(ivrData.getMessageID());
            //             }
            //         }
            //     }
            // }
            //header
            String AAAAA = atmReqheader.getCLIENTTRACEID();//jsonHeader.get("CLIENTTRACEID").toString();
            String BBBBB = atmReqheader.getCHANNEL();//jsonHeader.get("CHANNEL").toString();
            String CCCCC = atmReqheader.getMSGID();//jsonHeader.get("MSGID").toString();
            String DDDDD = atmReqheader.getCLIENTDT();//jsonHeader.get("CLIENTDT").toString();
            //SvcRs
            String EEEEE = atmReqbody.getFSCODE();//json.get("FSCODE").toString();
            String FFFFF = atmReqbody.getTRANSAMT().toString();//json.get("TRANSAMT").toString();
            String GGGGG = atmReqbody.getTRNSFROUTIDNO();//json.get("TRNSFROUTIDNO").toString();
            String HHHHH = atmReqbody.getTRNSFROUTBANK();//json.get("TRNSFROUTBANK").toString();
            String IIIII = atmReqbody.getTRNSFROUTACCNT();//json.get("TRNSFROUTACCNT").toString();
            String JJJJJ = atmReqbody.getTRNSFRINBANK();//json.get("TRNSFRINBANK").toString();
            String KKKKK = atmReqbody.getTRNSFRINACCNT();//json.get("TRNSFRINACCNT").toString();
            String LLLLL = atmReqbody.getCUSTPAYFEE().toString();//json.get("CUSTPAYFEE").toString();
            String MMMMM = atmReqbody.getFISCFEE().toString();//json.get("FISCFEE").toString();
            String NNNNN = atmReqbody.getOTHERBANKFEE().toString();//json.get("OTHERBANKFEE").toString();
            String OOOOO = atmReqbody.getCHAFEE_BRANCH();//json.get("CHAFEE_BRANCH").toString();
            String PPPPP = atmReqbody.getTRNSFRINNOTE();//json.get("TRNSFRINNOTE").toString();
            String QQQQQ = atmReqbody.getTRNSFROUTNOTE();//json.get("TRNSFROUTNOTE").toString();
            String YYYYMMDD = atmReqbody.getINDATE();//json.get("YYYYMMDD").toString();
            String HHMMSS = atmReqbody.getINTIME();//json.get("HHMMSS").toString();

            String rsp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Header></soap:Header><soap:Body><NS1:MsgRs xmlns:NS1=\"http://www.ibm.com.tw/esb\"><Header><CLIENTTRACEID>TITA-CLIENTTRACEID(AAAAA)</CLIENTTRACEID><CHANNEL>TITA-CHANNEL(BBBBB)</CHANNEL><MSGID>TITA-MSGID(CCCCC)</MSGID><CLIENTDT>TITA-CLIENTDT(DDDDD)</CLIENTDT><SYSTEMID>FEP</SYSTEMID><STATUSCODE></STATUSCODE><SEVERITY></SEVERITY><STATUSDESC></STATUSDESC></Header>    <SvcRs ><OUTDATE>YYYYMMDD</OUTDATE><OUTTIME>HHMMSS</OUTTIME><FEP_EJNO></FEP_EJNO><TXNSTAN></TXNSTAN><CUSTOMERID></CUSTOMERID><TXNTYPE>RQ</TXNTYPE><HOSTACC_FLAG>Y</HOSTACC_FLAG><HOSTRVS_FLAG>N</HOSTRVS_FLAG><FSCODE>TITA-FSCODE(EEEEE)</FSCODE><ACCTDATE></ACCTDATE><TRANSAMT>TITA-TRANSAMT(FFFFF)</TRANSAMT><TRANSFROUTBAL></TRANSFROUTBAL><TRANSOUTAVBL></TRANSOUTAVBL><TRANSAMTOUT></TRANSAMTOUT><TRNSFROUTIDNO>TITA-TRNSFROUTIDNO(GGGGG)</TRNSFROUTIDNO><TRNSFROUTNAME></TRNSFROUTNAME><TRNSFROUTBANK>TITA-TRNSFROUTBANK(HHHHH)</TRNSFROUTBANK><TRNSFROUTACCNT>TITA-TRNSFROUTACCNT(IIIII)</TRNSFROUTACCNT><TRNSFRINBANK>TITA-TRNSFRINBANK(JJJJJ)</TRNSFRINBANK><TRNSFRINACCNT>TITA-TRNSFRINACCNT(KKKKK)</TRNSFRINACCNT><CLEANBRANCHOUT></CLEANBRANCHOUT><CLEANBRANCHIN></CLEANBRANCHIN><CUSTPAYFEE>TITA-CUSTPAYFE(LLLLL)</CUSTPAYFEE><FISCFEE>TITA-FISCFEE(MMMMM)</FISCFEE><OTHERBANKFEE>TITA-OTHERBANKFEE(NNNNN)</OTHERBANKFEE><CHAFEE_BRANCH>TITA-CHAFEE_BRANCH(OOOOO)</CHAFEE_BRANCH><CHAFEEAMT></CHAFEEAMT><TRNSFRINNOTE>TITA-TRNSFRINNOTE(PPPPP)</TRNSFRINNOTE><TRNSFROUTNOTE>TITA-TRNSFROUTNOTE(QQQQQ)</TRNSFROUTNOTE><OVTPT></OVTPT><OVFEETY></OVFEETY><OVCIRCU></OVCIRCU><OVOTHER></OVOTHER><CUSTOMERNATURE></CUSTOMERNATURE><TCBRTNCODE></TCBRTNCODE></SvcRs></NS1:MsgRs></soap:Body></soap:Envelope>";
            rsp = rsp.replace("TITA-CLIENTTRACEID(AAAAA)", AAAAA)
                    .replace("TITA-CHANNEL(BBBBB)", BBBBB)
                    .replace("TITA-MSGID(CCCCC)", CCCCC)
                    .replace("TITA-CLIENTDT(DDDDD)", DDDDD)
                    .replace("TITA-FSCODE(EEEEE)", EEEEE)
                    .replace("TITA-TRANSAMT(FFFFF)", FFFFF)
                    .replace("TITA-TRNSFROUTIDNO(GGGGG)", GGGGG)
                    .replace("TITA-TRNSFROUTBANK(HHHHH)", HHHHH)
                    .replace("TITA-TRNSFROUTACCNT(IIIII)", IIIII)
                    .replace("TITA-TRNSFRINBANK(JJJJJ)", JJJJJ)
                    .replace("TITA-TRNSFRINACCNT(KKKKK)", KKKKK)
                    .replace("TITA-CUSTPAYFE(LLLLL)", LLLLL)
                    .replace("TITA-FISCFEE(MMMMM)", MMMMM)
                    .replace("TITA-OTHERBANKFEE(NNNNN)", NNNNN)
                    .replace("TITA-CHAFEE_BRANCH(OOOOO)", OOOOO)
                    .replace("TITA-TRNSFRINNOTE(PPPPP)", PPPPP)
                    .replace("TITA-TRNSFROUTNOTE(QQQQQ)", QQQQQ)
                    .replace("YYYYMMDD", YYYYMMDD)
                    .replace("HHMMSS", HHMMSS);
            ivrRes = rsp;
            return ivrRes;
        } catch (Throwable ex) {
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.toString());
            LogHelperFactory.getTraceLogger().trace("Dispatch發生例外 : " + ex.getStackTrace());
            // logData = new LogData();
            logData.setChannel(channel);
            logData.setMessage(data);
            logData.setMessageFlowType(MessageFlow.Request);
            if (ivrData != null) {
                logData.setMessageId(ivrData.getMessageID());
            }

            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setSubSys(SubSystem.IVR);
            logData.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            logData.setProgramException(ex);
            sendEMS(logData);
            ivrRes = getResStr(FEPReturnCode.ParseTelegramError, ivrData.getMessageID(), atmReqheader);
            return ivrRes;
        } finally {
            // 離開時記MessageLog
            logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            logData.setMessageFlowType(MessageFlow.Response);
            logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            logData.setMessage(ivrRes);
            logMessage(Level.DEBUG, logData);
        }
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    public IVRGeneral parseFlatfile(String data) throws Exception {
        RCV_VO_GeneralTrans_RQ req = deserializeFromXml(data, RCV_VO_GeneralTrans_RQ.class);
        ivrData.setTxObject(new IVRGeneral());
        ivrData.getTxObject().setRequest(req);
        ivrData.getTxObject().setResponse(null);

        return ivrData.getTxObject();
    }

    private String runAA(String msgId) throws Throwable {
        try {
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processIVRInbkRequestData", IVRData.class);
            return (String) ReflectionUtils.invokeMethod(method, aaBaseFactory, ivrData);
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    // 組INBKAA 在建構式發生EXCEPTION處理
    private String getResStr(FEPReturnCode code, String MSGID, RCV_VO_GeneralTrans_RQ.RCV_VO_GeneralTrans_RQ_Body_MsgRq_Header atmReqheader) {
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

        header.setCLIENTTRACEID(atmReqheader.getCLIENTTRACEID());
        header.setCHANNEL(atmReqheader.getCHANNEL());
        header.setMSGID(atmReqheader.getMSGID());
        header.setCLIENTDT(atmReqheader.getCLIENTDT());
        header.setSYSTEMID("FEP");
        header.setSTATUSCODE("0601");
        header.setSEVERITY("ERROR");
        header.setSTATUSDESC("發生exception");

        ResStr = XmlUtil.toXML(ivrRs);
        return ResStr;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}
