package com.syscom.fep.server.common.handler;

import com.google.gson.Gson;
import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStat;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStatRsStatCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.request.*;
import com.syscom.fep.vo.text.atm.response.EFT226XResponse;
import com.syscom.fep.vo.text.atm.response.IFT2521Response;
import com.syscom.fep.vo.text.atm.response.IPY2532Response;
import com.syscom.fep.vo.text.atm.response.VAA2566Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * 繳費網PHASEI:增加拆解226X電文
 *
 * @author Richard
 */
public class NBHandler extends HandlerBase {
    private static final String ProgramName = NBHandler.class.getSimpleName();
    private String atmSeq;
    private RMData rmData;
    private ATMData atmData;
    private LogData logData;

    private static final String R0100MSGID = "R0100";
    private static final String RT0011MSGID = "RT0011";
    private static final String R0230MSGID = "R0230";
    private static final String IFT2521ReqMSGID = "IFT2521";
    private static final String IPY2532ReqMSGID = "IPY2532";
    private static final String IFT2520ReqMSGID = "IFT2520";
    private static final String GIQ2500ReqMSGID = "GIQ2500";
    private static final String EFT226XReqMSGID = "EFT226X";
    private static final String EFT2260ReqMSGID = "EFT2260";
    private static final String EFT2261ReqMSGID = "EFT2261";
    @SuppressWarnings("unused")
    private static final String EFT2262ReqMSGID = "EFT2262";
    private static final String EFT2263ReqMSGID = "EFT2263";
    private static final String EFT2264ReqMSGID = "EFT2264";
    private static final String CHKAPIMACReqMSGID = "CHKAPIMAC";
    private static final String MAKAPIMACReqMSGID = "MAKAPIMAC";
    private static final String NWRReqMSGID = "NWR";
    private static final String NWQReqMSGID = "NWQ";
    private static final String NWSReqMSGID = "NWS";
    private static final String NWCReqMSGID = "NWC";
    private static final String VAA2566ReqMSGID = "VAA2566";
    private static final String MAKAPPTACReqMSGID = "MAKAPPTAC";
    private static final String ENCRAPIReqMSGID = "ENCRAPI";
    private static final String TRENAPIReqMSGID = "TRENAPI";
    private static final String ENCPTAXReqMSGID = "ENCPTAX";
    private static final String DECPTAXReqMSGID = "DECPTAX";
    private static final String NWAReqMSGID = "NWA";
    private static final String CHKMTPMACReqMSGID = "CHKMTPMAC";
    private static final String MAKMTPMACReqMSGID = "MAKMTPMAC";

    // private FEPRequest fepReq = null; // Request電文物件
    // private FEPResponse fepRsp = null; // Response電文物件
    private IFT2521Request ift2521Req = null; // Request電文物件
    private IFT2521Response ift2521Rsp = null; // Response電文物件
    private IPY2532Request ipy2532Req = null; //Request電文物件
    private IPY2532Response ipy2532Rsp = null; // Response電文物件
    // private IFT2520_Request IFT2520_Req = null; //Request電文物件
    // private IFT2520_Response IFT2520_Rsp = null; //Response電文物件
    // private GIQ2500_Request GIQ2500_Req = null; //Request電文物件
    // private GIQ2500_Response GIQ2500_Rsp = null; //Response電文物件
    private EFT226XRequest eft226xReq = null; //Request電文物件
    private EFT226XResponse eft226xRsp = null; // Response電文物件
    // private CHKAPIMAC_Request CHKAPIMAC_Req = null; //Request電文物件
    // private CHKAPIMAC_Response CHKAPIMAC_Rsp = null; //Response電文物件
    // private MAKAPIMAC_Request MAKAPIMAC_Req = null; //Request電文物件
    // private MAKAPIMAC_Response MAKAPIMAC_Rsp = null; //Response電文物件
    // private NWR_Request NWR_Req = null; //Request電文物件
    // private NWR_Response NWR_Rsp = null; //Response電文物件
    // private NWQ_Request NWQ_Req = null; //Request電文物件
    // private NWQ_Response NWQ_Rsp = null; //Response電文物件
    // private NWS_Request NWS_Req = null; //Request電文物件
    // private NWS_Response NWS_Rsp = null; //Response電文物件
    // private NWC_Request NWC_Req = null; //Request電文物件
    // private NWC_Response NWC_Rsp = null; //Response電文物件
    private VAA2566Request vaa2566Req = null; // Request電文物件
    private VAA2566Response vaa2566Rsp = null; // Response電文物件
    // private MAKAPPTAC_Request MAKAPPTAC_Req = null; //Request電文物件
    // private MAKAPPTAC_Response MAKAPPTAC_Rsp = null; //Response電文物件
    // private ENCRAPI_Request ENCRAPI_Req = null; //Request電文物件
    // private ENCRAPI_Response ENCRAPI_Rsp = null; //Response電文物件
    // private TRENAPI_Request TRENAPI_Req = null; //Request電文物件
    // private TRENAPI_Response TRENAPI_Rsp = null; //Response電文物件
    // private NWA_Request NWA_Req = null; //Request電文物件
    // private NWA_Response NWA_Rsp = null; //Response電文物件
    // private ENCPTAX_Request ENCPTAX_Req = null; //Request電文物件
    // private ENCPTAX_Response ENCPTAX_Rsp = null; //Response電文物件
    // private DECPTAX_Request DECPTAX_Req = null; //Request電文物件
    // private DECPTAX_Response DECPTAX_Rsp = null; //Response電文物件
    private CHKMTPMACRequest chkmtpmacReq = null; // Request電文物件
    // private CHKMTPMAC_Response CHKMTPMAC_Rsp = null; //Response電文物件
    private MAKMTPMACRequest makmtpmacReq = null; // Request電文物件
    // private MAKMTPMAC_Response MAKMTPMAC_Rsp = null; //Response電文物件

    private String dataMsgID;

    @Override
    public String dispatch(FEPChannel channel, String data) {
        data = StringUtils.replace(data, "\r\n", StringUtils.EMPTY);
        String atmRes = StringUtils.EMPTY;
        String stepstr = StringUtils.EMPTY;
        int headMsg = data.indexOf("<MsgID>") + "<MsgID>".length();
        int tailMsg = data.indexOf("</MsgID>");
        this.dataMsgID = data.substring(headMsg, tailMsg);
        try {
            if (this.ej == 0) {
                this.ej = TxHelper.generateEj();
            }
            if (this.logContext == null) {
                this.logData = new LogData();
                this.logData.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                this.logData.setSubSys(SubSystem.ATMP);
                this.logData.setChannel(channel);
                this.logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                this.logData.setMessageFlowType(MessageFlow.Request);
                this.logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
                this.logData.setMessage(data);
                this.logData.setEj(this.ej);
            } else {
                this.logData = this.logContext;
            }
            if (R0100MSGID.equals(dataMsgID)
                    || RT0011MSGID.equals(dataMsgID)
                    || R0230MSGID.equals(dataMsgID)) {
                rmData = new RMData();
                if (StringUtils.isNotBlank(data)) {
                    int head = data.indexOf("<MsgID>") + "<MsgID>".length();
                    int tail = data.indexOf("</MsgID>");
                    rmData.setMessageID(data.substring(head, tail));
                    if (StringUtils.isBlank(rmData.getMessageID())) {
                        return StringUtils.EMPTY;
                    }
                } else {
                    return StringUtils.EMPTY;
                }
                this.logData.setEj(this.ej);
                this.logData.setChannel(channel);
                this.logData.setMessage(data);
                this.logData.setMessageFlowType(MessageFlow.Request);
                this.logData.setMessageId(rmData.getMessageID());
                this.logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                this.logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
                logMessage(Level.DEBUG, this.logData);
                this.logData.setSubSys(SubSystem.RM);
                atmRes = this.dispatchRMAA(data, FEPChannel.NETBANK);
            } else {
                if (IFT2521ReqMSGID.equals(this.dataMsgID)) {
                    stepstr = "拆解電文錯誤";
                    atmData = new ATMData();
                    atmData.setMessageID(IFT2521ReqMSGID);
                    ift2521Rsp = new IFT2521Response();
                    ift2521Req = deserializeFromXml(data, IFT2521Request.class);
                    if (ift2521Req.getSvcRq().getRq() == null) {
                        throw new RuntimeException("MSGID：" + atmData.getMessageID() + stepstr);
                    }
                    stepstr = "";
                    logData.setSubSys(SubSystem.ATMP);
                    logData.setEj(ej);
                    // modified by Maxine for 拆解IFT2521_Request Header <ChlName>, 寫入 Channel
                    // 2014-04-09 Modify by Ruling for
                    // 豐掌櫃：前端為NETBANK、MMAB2C、MOBILBANK依RqHeader.ChlName轉為Enum寫入logData.Channel;其它預設為NETBANK
                    // 2020-09-09 Modify by Ruling for 新增MDAWHO Channel
                    // Code：增加MMAB2B、MDAWHO、B2CAPP三個Channel
                    if (FEPChannel.NETBANK.name().equals(ift2521Req.getRqHeader().getChlName())
//							|| FEPChannel.MMAB2C.name().equals(ift2521Req.getRqHeader().getChlName())
                            || FEPChannel.MOBILBANK.name().equals(ift2521Req.getRqHeader().getChlName())
//							|| FEPChannel.MMAB2B.name().equals(ift2521Req.getRqHeader().getChlName())
//							|| FEPChannel.MDAWHO.name().equals(ift2521Req.getRqHeader().getChlName())
//							|| FEPChannel.B2CAPP.name().equals(ift2521Req.getRqHeader().getChlName())
                    ) {
                        logData.setChannel(FEPChannel.valueOf(ift2521Req.getRqHeader().getChlName()));
                    } else {
                        logData.setChannel(channel); // 預設NETBANK
                    }
                    logData.setMessage(data);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setMessageId(ift2521Req.getRqHeader().getMsgID());
                    logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                    logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
                    // 解析電文之前先記一次
                    logMessage(Level.DEBUG, logData);
                    atmRes = dispatchATMAA(data, logData.getChannel());
                    return atmRes;
                } else if (IPY2532ReqMSGID.equals(this.dataMsgID)) {

                    stepstr = "拆解電文錯誤";
                    atmData = new ATMData();
                    atmData.setMessageID(IPY2532ReqMSGID);
                    ipy2532Rsp = new IPY2532Response();
                    ipy2532Req = deserializeFromXml(data, IPY2532Request.class);
                    if (ipy2532Req.getSvcRq().getRq() == null) {
                        throw new RuntimeException("MSGID：" + atmData.getMessageID() + stepstr);
                    }
                    stepstr = "";
                    logData.setSubSys(SubSystem.ATMP);
                    logData.setEj(ej);
                    // 2014-04-09 Modify by Ruling for
                    // 豐掌櫃：前端為NETBANK、MMAB2C、MOBILBANK依RqHeader.ChlName轉為Enum寫入logData.Channel;其它預設為NETBANK
                    if (FEPChannel.NETBANK.name().equals(ipy2532Req.getRqHeader().getChlName())
//							|| FEPChannel.MMAB2C.name().equals(ipy2532Req.getRqHeader().getChlName())
                            || FEPChannel.MOBILBANK.name().equals(ipy2532Req.getRqHeader().getChlName())) {
                        logData.setChannel(FEPChannel.valueOf(ipy2532Req.getRqHeader().getChlName()));
                    } else {
                        logData.setChannel(channel); // 預設NETBANK
                    }
                    // logData.Channel = channel;
                    logData.setMessage(data);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setMessageId(ipy2532Req.getRqHeader().getMsgID());
                    logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                    logData.setProgramName(StringUtils.join(ProgramName, "dispatch"));
                    // 解析電文之前先記一次
                    logMessage(Level.DEBUG, logData);
                    // 2014-04-09 Modify by Ruling for 豐掌櫃：寫法改為和IFT2521一致
                    atmRes = dispatchATMAA(data, logData.getChannel());
                    return atmRes;
                } else if (IFT2520ReqMSGID.equals(this.dataMsgID)
                        || GIQ2500ReqMSGID.equals(this.dataMsgID)
                        || EFT226XReqMSGID.equals(this.dataMsgID)) {
                    //2013-06-21 Modify by Ruling for Mobile Bank GIFT
                    //2015-10-22 Modify by Ruling for 繳費網PHASEI
                    int head = data.indexOf("<MsgID>") + (new String("<MsgID>")).length();
                    int tail = data.indexOf("</MsgID>");
                    atmData = new ATMData();
                    atmData.setMessageID(data.substring(head, tail));
                    if ("".equals(atmData.getMessageID().trim())) {
                        return "";
                    }

                    stepstr = "拆解電文錯誤";
                    logData.setSubSys(SubSystem.ATMP);
                    logData.setEj(getEj());
                    logData.setMessage(data);
                    logData.setMessageFlowType(MessageFlow.Request);
                    logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                    logData.setProgramName(ProgramName + "dispatch");

                    switch (atmData.getMessageID()) {
                        case IFT2520ReqMSGID:
                            // TODO
                            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatch() for [", this.dataMsgID, "] 方法未實作!!!");
                        case GIQ2500ReqMSGID:
                            // TODO
                            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatch() for [", this.dataMsgID, "] 方法未實作!!!");
                        case EFT226XReqMSGID:
                            eft226xRsp = new EFT226XResponse();
                            eft226xReq = deserializeFromXml(data, EFT226XRequest.class);
                            stepstr = "";
                            logData.setChannel(FEPChannel.valueOf(eft226xReq.getRqHeader().getChlName()));
                            logData.setMessageId(eft226xReq.getRqHeader().getMsgID());
                            break;
                    }

                    //解析電文之前先記一次
                    logMessage(Level.DEBUG, logData);
                    atmRes = dispatchATMAA(data, logData.getChannel());
                    return atmRes;
                } else if (CHKAPIMACReqMSGID.equals(this.dataMsgID)
                        || MAKAPIMACReqMSGID.equals(this.dataMsgID)
                        || MAKAPPTACReqMSGID.equals(this.dataMsgID)
                        || ENCRAPIReqMSGID.equals(this.dataMsgID)
                        || TRENAPIReqMSGID.equals(this.dataMsgID)
                        || ENCPTAXReqMSGID.equals(this.dataMsgID)
                        || DECPTAXReqMSGID.equals(this.dataMsgID)
                        || CHKMTPMACReqMSGID.equals(this.dataMsgID)
                        || MAKMTPMACReqMSGID.equals(this.dataMsgID)) {
                    // TODO
                    throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatch() for [", this.dataMsgID, "] 方法未實作!!!");
                } else if (NWQReqMSGID.equals(this.dataMsgID)
                        || NWSReqMSGID.equals(this.dataMsgID)
                        || NWCReqMSGID.equals(this.dataMsgID)
                        || NWRReqMSGID.equals(this.dataMsgID)
                        || NWAReqMSGID.equals(this.dataMsgID)) {
                    // Fly 2019/02/23 企業無卡提款
                    // TODO
                    throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatch() for [", this.dataMsgID, "] 方法未實作!!!");
                } else if (VAA2566ReqMSGID.equals(this.dataMsgID)) {
                    // Fly 2018/12/17 For 約定核驗 增加VAA2566
                    int head = data.indexOf("<MsgID>") + "<MsgID>".length();
                    int tail = data.indexOf("</MsgID>");
                    this.atmData = new ATMData();
                    this.atmData.setMessageID(data.substring(head, tail));
                    if (StringUtils.isBlank(this.atmData.getMessageID())) {
                        return StringUtils.EMPTY;
                    }
                    stepstr = "拆解電文錯誤";
                    this.logData.setSubSys(SubSystem.ATMP);
                    this.logData.setEj(this.ej);
                    this.logData.setMessage(data);
                    this.logData.setMessageFlowType(MessageFlow.Request);
                    this.logData.setProgramFlowType(ProgramFlow.MsgHandlerIn);
                    this.logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
                    vaa2566Rsp = new VAA2566Response();
                    vaa2566Req = deserializeFromXml(data, VAA2566Request.class);
                    stepstr = StringUtils.EMPTY;
                    // Fly 2019/09/17 VAA2566允許其他Channel進來
//					this.logData.setChannel(FEPChannel.APPLYBIZ);
                    this.logData.setMessageId(vaa2566Req.getRqHeader().getMsgID());

                    // 解析電文之前先記一次
                    logMessage(Level.DEBUG, logData);
                    atmRes = this.dispatchATMAA(data, logData.getChannel());
                    return atmRes;
                } else {
                    throw ExceptionUtil.createIllegalArgumentException("沒有此電文");
                }
            }
            this.logData.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            logMessage(Level.DEBUG, logData);
        } catch (Throwable e) {
            this.logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            this.logData.setMessageFlowType(MessageFlow.Response);
            this.logData.setMessage(e.getMessage());
            this.logData.setProgramException(e);
            this.logData.setRemark(StringUtils.join(ProgramName, " get exception"));
            logMessage(Level.DEBUG, this.logData);
            // 2015-10-22 Modify by Ruling for 繳費網PHASEI:增加讀取NPSUNIT無資料時回EF1488
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("讀取失敗")) {
                atmRes = this.getResStr(e.getMessage(), "EX2999");
            } else if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("資料不存在於委託單位檔(NPSUNIT)內")) {
                atmRes = this.getResStr(e.getMessage(), "EX4806");
            } else {
                if (StringUtils.isBlank(stepstr)) {
                    atmRes = this.getResStr(TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.ProgramException, logData), "EX2999");
                } else {
                    atmRes = this.getResStr(stepstr, "EX2999");
                }
            }
            this.logData.setProgramException(e);
            this.logData.setRemark(StringUtils.join(ProgramName, " get exception"));
            sendEMS(this.logData);
        } finally {
            // 離開時記MessageLog
            this.logData.setProgramFlowType(ProgramFlow.MsgHandlerOut);
            this.logData.setMessageFlowType(MessageFlow.Response);
            this.logData.setProgramName(StringUtils.join(ProgramName, ".dispatch"));
            this.logData.setMessage(atmRes);
            logMessage(Level.DEBUG, this.logData);
        }
        return atmRes;
    }

    private String dispatchRMAA(String data, FEPChannel netbank) {
        // TODO
        throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchRMAA()方法未實作!!!");
    }

    private String dispatchATMAA(String data, FEPChannel channel) throws Throwable {
        Bin bin = null;
        ATMGeneral gal = new ATMGeneral();
        boolean selfFlag = false;
        // 2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
        String msgid = StringUtils.EMPTY;
        if (CHKMTPMACReqMSGID.equals(this.dataMsgID)) {
            // Fly 2020-06-22 手機門號轉帳
            selfFlag = true;
            this.chkmtpmacReq.toGeneral(gal);
            this.atmData.setMessageCorrelationID(this.messageCorrelationId);
            this.atmData.setTxChannel(channel);
            this.setMessageId(this.atmData.getMessageID());
            this.atmData.setLogContext(this.logData);
            this.atmData.setTxSubSystem(SubSystem.ATMP);
            this.atmData.setMessageFlowType(MessageFlow.Request);
            this.atmData.setAtmSeq(this.atmSeq);
            this.atmData.setTxObject(gal);
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXCD(ATMTXCD.API.name());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setChlEJNo(this.chkmtpmacReq.getRqHeader().getChlEJNo());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setBRNO(this.chkmtpmacReq.getRqHeader().getBranchID().substring(this.chkmtpmacReq.getRqHeader().getBranchID().length() - 3));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setWSNO(this.chkmtpmacReq.getRqHeader().getTermID());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXDATETM(this.chkmtpmacReq.getSvcRq().getRq().getTXDATETM());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setMSG(this.chkmtpmacReq.getSvcRq().getRq().getMSG());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setDIV(this.chkmtpmacReq.getSvcRq().getRq().getDIV());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setMAC(this.chkmtpmacReq.getSvcRq().getRq().getMAC());
            this.atmData.setTxRequestMessage(data);
            this.atmData.setEj(this.ej);
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setCHLCODE(this.chkmtpmacReq.getRqHeader().getChlName());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setBknoD(SysStatus.getPropertyValue().getSysstatFbkno());
            //ben20221118  this.atmData.getTxObject().getResponse().setEJNo(String.valueOf(this.ej));
            //--ben-20220922-//this.atmData.getTxObject().getResponse().setChlEJNo(this.atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  this.atmData.getTxObject().getResponse().setRqTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        } else if (MAKMTPMACReqMSGID.equals(this.dataMsgID)) {
            selfFlag = true;
            this.makmtpmacReq.toGeneral(gal);
            this.atmData.setMessageCorrelationID(this.messageCorrelationId);
            this.atmData.setTxChannel(channel);
            this.setMessageId(this.atmData.getMessageID());
            this.atmData.setLogContext(this.logData);
            this.atmData.setTxSubSystem(SubSystem.ATMP);
            this.atmData.setMessageFlowType(MessageFlow.Request);
            this.atmData.setAtmSeq(this.atmSeq);
            this.atmData.setTxObject(gal);
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXCD(ATMTXCD.API.name());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setChlEJNo(this.makmtpmacReq.getRqHeader().getChlEJNo());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setBRNO(this.makmtpmacReq.getRqHeader().getBranchID().substring(this.makmtpmacReq.getRqHeader().getBranchID().length() - 3));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setWSNO(this.makmtpmacReq.getRqHeader().getTermID());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXDATETM(this.makmtpmacReq.getSvcRq().getRq().getTXDATETM());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setMSG(this.makmtpmacReq.getSvcRq().getRq().getMSG());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setDIV(this.makmtpmacReq.getSvcRq().getRq().getDIV());
            this.atmData.setTxRequestMessage(data);
            this.atmData.setEj(this.ej);
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setCHLCODE(this.makmtpmacReq.getRqHeader().getChlName());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setBknoD(SysStatus.getPropertyValue().getSysstatFbkno());
            //ben20221118  this.atmData.getTxObject().getResponse().setEJNo(String.valueOf(this.ej));
            //--ben-20220922-//this.atmData.getTxObject().getResponse().setChlEJNo(this.atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  this.atmData.getTxObject().getResponse().setRqTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        } else if (IFT2521ReqMSGID.equals(this.dataMsgID)) {
            // FEP不會檢核此欄位, 轉出銀行別由 FEP 自行產生
            ift2521Req.getSvcRq().getRq().setBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
            ift2521Req.toGeneral(gal);
            // atmData = new ATMData();
            // CheckBin
            bin = checkBinForATM(gal);
            atmData.setBin(bin);
            atmData.setMessageCorrelationID(this.messageCorrelationId);
            atmData.setTxChannel(channel);
            // atmData.MessageID = IFT2521_Req.RqHeader.MsgID;
            this.setMessageId(atmData.getMessageID());
            atmData.setLogContext(logData);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(getAtmSeq());
            atmData.setTxObject(gal);
            // 2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
            msgid = ift2521Req.getRqHeader().getMsgID();
            //--ben-20220922-//atmData.getTxObject().getRequest().setTXCD(msgid.substring(0, 3));
            // atmData.TxObject.Request.TXCD = IFT2521_Req.RqHeader.MsgID.Substring(0, 3);
            //--ben-20220922-//atmData.getTxObject().getRequest().setChlEJNo(ift2521Req.getRqHeader().getChlEJNo());
            //--ben-20220922-//atmData.getTxObject().getRequest().setBRNO(ift2521Req.getRqHeader().getBranchID()
            //--ben-20220922-//		.substring(ift2521Req.getRqHeader().getBranchID().length() - 3));
            //--ben-20220922-//atmData.getTxObject().getRequest().setWSNO(ift2521Req.getRqHeader().getTermID().toString());
            // 轉入轉出帳號補滿16位
            //--ben-20220922-//atmData.getTxObject().getRequest()
            //--ben-20220922-//		.setTXACT(StringUtils.leftPad(atmData.getTxObject().getRequest().getTXACT(), 16, '0'));
            //--ben-20220922-//atmData.getTxObject().getRequest()
            //--ben-20220922-//		.setActD(StringUtils.leftPad(atmData.getTxObject().getRequest().getActD(), 16, '0'));
            atmData.setTxRequestMessage(data);
            atmData.setEj(ej);
            // modify 20110216
            // 2014-04-09 Modify by Ruling for 豐掌櫃：CHLCODE(前端系統)依RqHeader.ChlName帶入實際的值
            //--ben-20220922-//atmData.getTxObject().getRequest().setCHLCODE(ift2521Req.getRqHeader().getChlName());
            // atmData.TxObject.Request.CHLCODE = channel.ToString();
            //--ben-20220922-//atmData.getTxObject().getRequest().setBknoD(ift2521Req.getSvcRq().getRq().getBknoD());
            //ben20221118  atmData.getTxObject().getResponse().setEJNo(ej.toString());
            //--ben-20220922-//atmData.getTxObject().getResponse().setChlEJNo(atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  atmData.getTxObject().getResponse().setRqTime(
            //ben20221118  		FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            // Fly 2019/03/06 For 中文附言欄
            //--ben-20220922-//atmData.getTxObject().getRequest().setCHREM(ift2521Req.getSvcRq().getRq().getCHREM());
        } else if (IPY2532ReqMSGID.equals(this.dataMsgID)) {
            // FEP不會檢核此欄位, 轉出銀行別由 FEP 自行產生
            ipy2532Req.getSvcRq().getRq().setBKNO(SysStatus.getPropertyValue().getSysstatHbkno());
            ipy2532Req.toGeneral(gal);
            atmData = new ATMData();
            // CheckBin
            bin = checkBinForATM(gal);
            atmData.setBin(bin);
            atmData.setMessageCorrelationID(this.messageCorrelationId);
            atmData.setTxChannel(channel);
            atmData.setMessageID(ipy2532Req.getRqHeader().getMsgID());
            this.messageId = atmData.getMessageID();
            atmData.setLogContext(logData);
            atmData.setTxSubSystem(SubSystem.ATMP);
            atmData.setMessageFlowType(MessageFlow.Request);
            atmData.setAtmSeq(getAtmSeq());
            atmData.setTxObject(gal);
            // 2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
            msgid = ipy2532Req.getRqHeader().getMsgID();
            //--ben-20220922-//atmData.getTxObject().getRequest().setTXCD(msgid.substring(0, 3));
            // atmData.TxObject.Request.TXCD = IPY2532_Req.RqHeader.MsgID.Substring(0, 3);
            //--ben-20220922-//atmData.getTxObject().getRequest().setChlEJNo(ipy2532Req.getRqHeader().getChlEJNo());
            //--ben-20220922-//atmData.getTxObject().getRequest().setBRNO(ipy2532Req.getRqHeader().getBranchID()
            //--ben-20220922-//		.substring(ipy2532Req.getRqHeader().getBranchID().length() - 3));
            //--ben-20220922-//atmData.getTxObject().getRequest().setWSNO(ipy2532Req.getRqHeader().getTermID().toString());
            // 轉入轉出帳號補滿16位
            //--ben-20220922-//atmData.getTxObject().getRequest()
            //--ben-20220922-//		.setTXACT(StringUtils.leftPad(atmData.getTxObject().getRequest().getTXACT(), 16, '0'));
            //--ben-20220922-//atmData.getTxObject().getRequest()
            //--ben-20220922-//		.setActD(StringUtils.leftPad(atmData.getTxObject().getRequest().getActD(), 16, '0'));
            atmData.setTxRequestMessage(data);
            atmData.setEj(ej);
            // modify 20110216
            // 2014-04-09 Modify by Ruling for 豐掌櫃：CHLCODE(前端系統)依RqHeader.ChlName帶入實際的值
            //--ben-20220922-//atmData.getTxObject().getRequest().setCHLCODE(ipy2532Req.getRqHeader().getChlName());
            // atmData.TxObject.Request.CHLCODE = channel.ToString();
            //--ben-20220922-//atmData.getTxObject().getRequest().setBknoD(SysStatus.getPropertyValue().getSysstatFbkno());
            //ben20221118  atmData.getTxObject().getResponse().setEJNo(ej.toString());
            //--ben-20220922-//atmData.getTxObject().getResponse().setChlEJNo(atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  atmData.getTxObject().getResponse().setRqTime(
            //ben20221118  		FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        } else if (IFT2520ReqMSGID.equals(this.dataMsgID) || GIQ2500ReqMSGID.equals(this.dataMsgID) || EFT226XReqMSGID.equals(this.dataMsgID)) {
            // 2013-06-21 Modify by Ruling for Mobile Bank GIFT
            // 2015-10-22 Modify by Ruling for 繳費網PHASEI
            selfFlag = true;
            this.atmData.setLogContext(this.logData);
            switch (this.atmData.getMessageID()) {
                case IFT2520ReqMSGID:
                    // TODO
                    throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
                    // break; 實作後這個注釋放開
                case GIQ2500ReqMSGID:
                    // TODO
                    throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
                    // break; 實作後這個注釋放開
                case EFT226XReqMSGID:
                    selfFlag = false;
                    eft226xReq.toGeneral(gal);
                    atmData.setTxObject(gal);
                    //2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
                    msgid = eft226xReq.getRqHeader().getMsgID();
                    //--ben-20220922-//atmData.getTxObject().getRequest().setTXCD(msgid.substring(0, 3));
                    //atmData.TxObject.Request.TXCD = EFT226X_Req.RqHeader.MsgID.Substring(0, 3);
                    //--ben-20220922-//atmData.getTxObject().getRequest().setChlEJNo(eft226xReq.getRqHeader().getChlEJNo());
                    //--ben-20220922-//atmData.getTxObject().getRequest().setBRNO(eft226xReq.getRqHeader().getBranchID().substring(eft226xReq.getRqHeader().getBranchID().length() - 3));
                    //--ben-20220922-//atmData.getTxObject().getRequest().setWSNO(eft226xReq.getRqHeader().getTermID());
                    if (StringUtils.isBlank(eft226xReq.getSvcRq().getRq().getMENO())) {
                        //--ben-20220922-//atmData.getTxObject().getRequest().setMENO(StringUtils.join(eft226xReq.getSvcRq().getRq().getPAYID().trim() , StringUtils.leftPad(eft226xReq.getSvcRq().getRq().getPAYCNO(),16, '0')));
                    } else {
                        //--ben-20220922-//atmData.getTxObject().getRequest().setMENO(StringUtils.rightPad(eft226xReq.getSvcRq().getRq().getMENO(),40, ' '));
                    }

                    if ("04".equals(StringUtils.leftPad(eft226xReq.getSvcRq().getRq().getPTYPE(), 2, '0'))) {
                        Npsunit defNPSUNIT = new Npsunit();
                        NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
                        defNPSUNIT.setNpsunitNo(eft226xReq.getSvcRq().getRq().getVPID().trim());
                        defNPSUNIT.setNpsunitPaytype(eft226xReq.getSvcRq().getRq().getCLASS().trim());
                        defNPSUNIT.setNpsunitFeeno(eft226xReq.getSvcRq().getRq().getPAYID().trim());
                        defNPSUNIT = dbNPSUNIT.selectByPrimaryKey(defNPSUNIT.getNpsunitNo(), defNPSUNIT.getNpsunitPaytype(), defNPSUNIT.getNpsunitFeeno());
                        if (defNPSUNIT == null) {
                            throw new RuntimeException("資料不存在於委託單位檔(NPSUNIT)內");
                        } else {
                            //2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
                            String trin_bkno = "";
                            trin_bkno = defNPSUNIT.getNpsunitTrinBkno();
                            //--ben-20220922-//atmData.getTxObject().getRequest().setBknoD(trin_bkno.substring(0, 3));
                            //atmData.TxObject.Request.BKNO_D = defNPSUNIT.NPSUNIT_TRIN_BKNO.Substring(0, 3);
                        }
                        //--ben-20220922-//atmData.getTxObject().getRequest().setActD(eft226xReq.getSvcRq().getRq().getPAYCNO());
                    }
                    break;
            }
            this.messageId = this.atmData.getMessageID();
            // CheckBin
            bin = this.checkBinForATM(gal);
            this.atmData.setBin(bin);
            this.atmData.setMessageCorrelationID(this.messageCorrelationId);
            this.atmData.setTxChannel(channel);
            this.atmData.setTxSubSystem(SubSystem.ATMP);
            this.atmData.setMessageFlowType(MessageFlow.Request);
            this.atmData.setAtmSeq(this.atmSeq);

            this.atmData.setTxRequestMessage(data);
            this.atmData.setEj(this.ej);
            // 轉入轉出帳號補滿16位
            // 2016-12-05 Modify by Ruling for 修正交易帳號有空白送T24時會回錯誤訊的問題
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXACT(StringUtils.leftPad(this.atmData.getTxObject().getRequest().getTXACT().trim(), 16, '0'));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setActD(StringUtils.leftPad(this.atmData.getTxObject().getRequest().getActD(), 16, '0'));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setCHLCODE(channel.name());
            //ben20221118  this.atmData.getTxObject().getResponse().setEJNo(String.valueOf(this.ej));
            //--ben-20220922-//this.atmData.getTxObject().getResponse().setChlEJNo(this.atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  this.atmData.getTxObject().getResponse().setRqTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            // 2015-10-22 Modify by Ruling for 繳費網PHASEI:判斷自跨行和將EFT226X拆為EFT2260、EFT2261、EFT226X、EFT2263、EFT2264讀MSGCTL
            if (EFT226XReqMSGID.equals(this.atmData.getMessageID())) {
                // 轉出行=807
                //--ben-20220922-//if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.atmData.getTxObject().getRequest().getBKNO())) {
                //--ben-20220922-//	if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.atmData.getTxObject().getRequest().getBknoD())) {
                //--ben-20220922-//		this.atmData.setMessageID(EFT2260ReqMSGID); // 自行
                //--ben-20220922-//		selfFlag = true;
                //--ben-20220922-//	} else {
                //--ben-20220922-//		this.atmData.setMessageID(EFT2261ReqMSGID); // 跨行
                //--ben-20220922-//	}
                //--ben-20220922-//}
                // 轉出行<>807
                //--ben-20220922-//else {
                // 他行轉入本行(如103行轉807行，代理行807)；為了與證券整批轉即時區別改用EFT226X讀MSGCTL
                //--ben-20220922-//if (SysStatus.getPropertyValue().getSysstatHbkno().equals(this.atmData.getTxObject().getRequest().getBknoD())) {
                //--ben-20220922-//	this.atmData.setMessageID(EFT226XReqMSGID);
                //--ben-20220922-//}
                // 他行轉入 (如103行轉103行，代理行807)
                //--ben-20220922-//else if (this.atmData.getTxObject().getRequest().getBKNO().equals(this.atmData.getTxObject().getRequest().getBknoD())) {
                //--ben-20220922-//	this.atmData.setMessageID(EFT2263ReqMSGID);
                //--ben-20220922-//}
                // 他行轉入他行(如103行轉021行，代理行807)
                //--ben-20220922-//else {
                //--ben-20220922-//	this.atmData.setMessageID(EFT2264ReqMSGID);
                //--ben-20220922-//	}
                //--ben-20220922-//}
            }
        } else if (CHKAPIMACReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (MAKAPIMACReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (NWRReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (NWQReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (NWSReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (NWCReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (VAA2566ReqMSGID.equals(this.dataMsgID)) {
            // Fly 2018/12/17 For 約定核驗 增加VAA2566
            this.vaa2566Req.toGeneral(gal);
            this.atmData.setMessageCorrelationID(this.messageCorrelationId);
            this.atmData.setTxChannel(channel);
            this.setMessageId(this.atmData.getMessageID());
            this.atmData.setLogContext(this.logData);
            this.atmData.setTxSubSystem(SubSystem.ATMP);
            this.atmData.setMessageFlowType(MessageFlow.Request);
            this.atmData.setAtmSeq(this.atmSeq);
            this.atmData.setTxObject(gal);
            // 2020-10-07 Modify by Ruling for Fortify：修正Null Dereference問題
            msgid = vaa2566Req.getRqHeader().getMsgID();
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXCD(msgid.substring(0, 3));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setChlEJNo(vaa2566Req.getRqHeader().getChlEJNo());
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setBRNO(vaa2566Req.getRqHeader().getBranchID().substring(vaa2566Req.getRqHeader().getBranchID().length() - 3));
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setWSNO(vaa2566Req.getRqHeader().getTermID());
            // Fly 2019/10/15 轉入帳號補滿16位
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setTXACT(StringUtils.leftPad(vaa2566Req.getSvcRq().getRq().getTXACT(), 16, '0'));
            this.atmData.setTxRequestMessage(data);
            this.atmData.setEj(this.ej);
            //--ben-20220922-//this.atmData.getTxObject().getRequest().setCHLCODE(vaa2566Req.getRqHeader().getChlName());
            //ben20221118  this.atmData.getTxObject().getResponse().setEJNo(String.valueOf(this.ej));
            //--ben-20220922-//this.atmData.getTxObject().getResponse().setChlEJNo(this.atmData.getTxObject().getRequest().getChlEJNo());
            //ben20221118  this.atmData.getTxObject().getResponse().setRqTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        } else if (MAKAPPTACReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (ENCRAPIReqMSGID.equals(this.dataMsgID)) {
            // TODO 繳費網信用卡資訊加密需求
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (TRENAPIReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (NWAReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (ENCPTAXReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        } else if (DECPTAXReqMSGID.equals(this.dataMsgID)) {
            // TODO
            throw ExceptionUtil.createNotImplementedException(ProgramName, ".dispatchATMAA() for [", this.dataMsgID, "] 方法未實作!!!");
        }

        // 為了方便check問題，這裡列印一下
        LogHelperFactory.getTraceLogger().debug("[", ProgramName, ".dispatchATMAA]ATM Request Data : ", new Gson().toJson(this.atmData.getTxObject().getRequest()));

        // 讀出交易控制檔本筆交易資料
        this.atmData.setMsgCtl(FEPCache.getMsgctrl(this.atmData.getMessageID()));
        if (this.atmData.getMsgCtl() == null) {
            throw ExceptionUtil.createException("讀不到MsgID:" + this.atmData.getMessageID());
        }

        this.setChannel(atmData, channel); // 2024-04-01 Richard add在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
        this.atmData.setAaName(this.atmData.getMsgCtl().getMsgctlAaName());
        this.atmData.setTxStatus(this.atmData.getMsgCtl().getMsgctlStatus() == 1);

        if (bin != null) {
            // 檢核信用卡主機連線狀態
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAsc())) {
//				this.atmData.getMsgCtl().setMsgctlStatus((short) 0); // 暫停此交易
//				this.atmData.setTxStatus(false);
//			} else {
            // COMBO卡信用卡, 檢核永豐晶片錢卡連線狀態
            if (BINPROD.Combo.equals(bin.getBinProd())) {
// 2024-03-06 Richard modified for SYSSTATE 調整
//					if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAscmd())) {
//						this.atmData.getMsgCtl().setMsgctlStatus((short) 0); // 暫停此交易
//						this.atmData.setTxStatus(false);
//					}
            }
//			}
        }

        // 2013-06-21 Modify by Ruling for Mobile Bank GIFT:用selfFlag來判斷是自行或跨行NEW不同的AABase
        // call AA
        if (!selfFlag) {
            return this.runATMAA();
        } else {
            return this.runATMAAforSelf();
        }
    }

    /**
     * 2015-10-22 Modify by Ruling for 繳費網PHASEI:呼叫新AA EFTNBRequestA
     *
     * @return
     * @throws Exception
     */
    private String runATMAA() throws Throwable {
        try {
            // call AA
            Object aaBaseFactory = SpringBeanFactoryUtil.getBean("aaBaseFactory");
            Method method = ReflectionUtils.findMethod(aaBaseFactory.getClass(), "processInbkRequestData", ATMData.class);
            // 呼叫AA將ATMData傳進去
            ReflectionUtils.invokeMethod(method, aaBaseFactory, this.atmData);
            String atmRes = StringUtils.EMPTY;
            if (IFT2521ReqMSGID.equals(atmData.getMessageID())) {
                atmRes = ift2521Rsp.makeMessageFromGeneral(atmData.getTxObject());
            } else if (IPY2532ReqMSGID.equals(atmData.getMessageID())) {
                atmRes = ipy2532Rsp.makeMessageFromGeneral(atmData.getTxObject());
            } else if (EFT2261ReqMSGID.equals(atmData.getMessageID())
                    || EFT226XReqMSGID.equals(atmData.getMessageID())
                    || EFT2263ReqMSGID.equals(atmData.getMessageID())
                    || EFT2264ReqMSGID.equals(atmData.getMessageID())) {
                atmRes = eft226xRsp.makeMessageFromGeneral(atmData.getTxObject());
            } else if (VAA2566ReqMSGID.equals(atmData.getMessageID())) {
                // Fly 2018/12/17 For 約定核驗 增加VAA2566
                atmRes = vaa2566Rsp.makeMessageFromGeneral(atmData.getTxObject());
            }
            return atmRes;
        } catch (Exception e) {
            throw ExceptionUtil.reflectionInvokeExceptionOccur(e);
        }
    }

    /**
     * 2013-06-21 Modify by Ruling for Mobile Bank GIFT
     * 2015-10-22 Modify by Ruling for 繳費網PHASEI
     * 2016-06-30 Modify by Ruling for 繳費網PHASEII
     * 2019-04-18 Modify by Ruling for 豐錢包APP綁定本行帳號安控機制：增加電文MAKAPPTACReq
     *
     * @return
     */
    private String runATMAAforSelf() {
        // TODO 自動生成的方法存根
        throw ExceptionUtil.createNotImplementedException(ProgramName, ".runATMAAforSelf()方法未實作!!!");
    }

    @Override
    public boolean dispatch(FEPChannel channel, Object data) throws Exception {
        return true;
    }

    /**
     * 組INBKAA 在建構式發生EXCEPTION處理
     *
     * @param message
     * @param errorcode
     * @return
     */
    private String getResStr(String message, String errorcode) {
        String atmRes = StringUtils.EMPTY;
        switch (atmData.getMessageID()) {
            case IFT2521ReqMSGID:
                ift2521Rsp.setRsHeader(new FEPRsHeader());
                ift2521Rsp.getRsHeader().setRsStat(new FEPRsHeaderRsStat());
                ift2521Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
                ift2521Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(errorcode);
                ift2521Rsp.getRsHeader().getRsStat().setDesc(message);
                ift2521Rsp.getRsHeader().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
                atmRes = serializeToXml(ift2521Rsp);
                atmRes = StringUtils.replace(atmRes, "&lt;", "<");
                atmRes = StringUtils.replace(atmRes, "&gt;", ">");
                break;
            case IPY2532ReqMSGID:
                ipy2532Rsp.setRsHeader(new FEPRsHeader());
                ipy2532Rsp.getRsHeader().setRsStat(new FEPRsHeaderRsStat());
                ipy2532Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
                ipy2532Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(errorcode);
                ipy2532Rsp.getRsHeader().getRsStat().setDesc(message);
                ipy2532Rsp.getRsHeader().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
                atmRes = serializeToXml(ipy2532Rsp);
                atmRes = StringUtils.replace(atmRes, "&lt;", "<");
                atmRes = StringUtils.replace(atmRes, "&gt;", ">");
                break;
            case IFT2520ReqMSGID:
                // 2013-06-21 Modify by Ruling for Mobile Bank GIFT
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case GIQ2500ReqMSGID:
                // 2013-06-21 Modify by Ruling for Mobile Bank GIFT
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case EFT226XReqMSGID:
                // 2015-10-22 Modify by Ruling for 繳費網PHASEI
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case CHKAPIMACReqMSGID:
                // Fly 2016-06-22 繳費網PHASEII
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case MAKAPIMACReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case NWRReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case NWCReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case NWSReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case NWQReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case VAA2566ReqMSGID:
                // Fly 2019/03/26 For 約定核驗 增加VAA2566
                vaa2566Rsp.setRsHeader(new FEPRsHeader());
                vaa2566Rsp.getRsHeader().setRsStat(new FEPRsHeaderRsStat());
                vaa2566Rsp.getRsHeader().getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
                vaa2566Rsp.getRsHeader().getRsStat().getRsStatCode().setValue(errorcode);
                vaa2566Rsp.getRsHeader().getRsStat().setDesc(message);
                vaa2566Rsp.getRsHeader().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
                atmRes = serializeToXml(vaa2566Rsp);
                atmRes = StringUtils.replace(atmRes, "&lt;", "<");
                atmRes = StringUtils.replace(atmRes, "&gt;", ">");
                break;
            case MAKAPPTACReqMSGID:
                // 2019-04-18 Modify by Ruling for 豐錢包APP綁定本行帳號安控機制：增加電文MAKAPPTACReq
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case ENCRAPIReqMSGID:
                // Fly 2019/02/23 繳費網信用卡資訊加密需求
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case TRENAPIReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case NWAReqMSGID:
                // Fly 2019/02/23 企業無卡提款
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            default:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case ENCPTAXReqMSGID:
                // Fly 2019/12/24 豐錢包新增Paytax繳稅功能
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case DECPTAXReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case CHKMTPMACReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
            case MAKMTPMACReqMSGID:
                // TODO
                throw ExceptionUtil.createNotImplementedException(ProgramName, ".getResStr() for [", this.dataMsgID, "] 方法未實作!!!");
                // break; 實作後這個注釋放開
        }
        return atmRes;
    }

    public String getAtmSeq() {
        return atmSeq;
    }

    public void setAtmSeq(String atmSeq) {
        this.atmSeq = atmSeq;
    }

    @Override
    public String dispatch(FEPChannel channel, String atmNo, String data) {
        // TODO Auto-generated method stub
        return null;
    }
}
