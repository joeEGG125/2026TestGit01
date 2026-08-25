package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.Calendar;

public class AA1515 extends RMAABase {

    //共用變數宣告
    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private static final LogHelper TRACELOGGER = LogHelperFactory.getTraceLogger();

    /**
     * 建構式 AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public AA1515(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        LogData logContext = new LogData();
        logContext.setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        try {
            _rtnCode = prepareAndSendForFisc();

            //CheckResponseFromFISC:檢核回應電文是否正確
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                _rtnCode = getmFISCBusiness().checkResponseFromFISC();
            }
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
        }finally {
            getmTxFISCData().getLogContext().setReturnCode(_rtnCode);
            getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxFISCData().getLogContext().setMessage(rtnMessage);
            getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO,logContext);
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                // For UI028023
                getmTxFISCData().getTxObject().setDescription(NormalRC.External_OK);
            }else {
                getmTxFISCData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode,logContext));
            }
        }
        return "";
    }

    /**
     * 1.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
     *
     * @return FEPReturnCode
     */
    @SuppressWarnings("unused")
    private FEPReturnCode prepareAndInsertFeptxn() {
        LogData logContext = new LogData();
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
            rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(), "0");

            //新增交易記錄(FEPTXN )
            if (rtnCode.equals(CommonReturnCode.Normal)) {
                rtnCode = getmFISCBusiness().insertFEPTxn();
            }
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            logContext.setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }
        return _rtnCode;
    }

    /**
     * 3.組送往 FISC 之 Request 電文並等待財金之 Response
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareAndSendForFisc(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        LogData logContext = new LogData();
        //(1) 準備回財金的相關資料
        rtnCode = prepareFiscReq();
        if (!rtnCode.equals(CommonReturnCode.Normal)){
            logContext.setRemark("After PrepareForFISC rtnCode=" + _rtnCode.toString());
            sendEMS(logContext);
            return rtnCode;
        }

        //送1511 Req電文到財金(SendToFISC) 並等待回復
        rtnCode=getmFISCBusiness().sendRMRequestToFISC();

        return rtnCode;
    }

    private FEPReturnCode prepareFiscReq(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        LogData logContext = new LogData();
        RefString reqMac = new RefString();
        ENCHelper encHelper;
        try {
            //組header()
            getmFISCRMReq().setSystemSupervisoryControlHeader("00");
            getmFISCRMReq().setSystemNetworkIdentifier("00");
            getmFISCRMReq().setAdderssControlField("00");
            getmFISCRMReq().setMessageType("0200");
            getmFISCRMReq().setProcessingCode("1515");
            getmFISCRMReq().setSystemTraceAuditNo(getmFISCBusiness().getStan());
            getmTxFISCData().setStan(getmFISCRMReq().getSystemTraceAuditNo());
            getmFISCRMReq().setTxnDestinationInstituteId(StringUtils.rightPad("950",7,'0'));
            getmFISCRMReq().setTxnSourceInstituteId(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatHbkno(),7,'0'));
            //轉成民國年
            getmFISCRMReq().setTxnInitiateDateAndTime((CalendarUtil.adStringToROCString(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)).substring(1,7)+
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
            getmFISCRMReq().setResponseCode(NormalRC.FISC_REQ_RC);
            getmFISCRMReq().setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(),8,' '));
            //組Body   '呼叫AA的時候就將FISC_NO/ORG_PCODE賦值

            //產生MAC
            encHelper = new ENCHelper(getmTxFISCData());
            rtnCode = encHelper.makeRMFISCMAC(reqMac);
            if (!rtnCode.equals(CommonReturnCode.Normal)){
                return rtnCode;
            }

            getmFISCRMReq().setMAC(StringUtils.leftPad(reqMac.get(),8,'0'));
            //1515 Request new field
            getmFISCRMReq().setOrgPcode(getmTxFISCData().getTxObject().getRMRequest().getOrgPcode());

            //MakeBitmap
            rtnCode = getmFISCBusiness().makeBitmap(getmFISCRMReq().getMessageType(),getmFISCRMReq().getProcessingCode(), MessageFlow.Request);
//          Trace.WriteLine(String.Format("after MakeBitmap rtnCode={0}{1}", rtnCode.ToString(), CType(rtnCode, String).PadLeft(4, "0"c)))
            TRACELOGGER.info(
                    StringUtils.join("after MakeBitmap rtnCode={0}{1}", rtnCode.toString(),StringUtils.leftPad(rtnCode.toString(),4,"0")));
            return rtnCode;
        }catch (Exception ex){
           logContext.setProgramException(ex);
           sendEMS(logContext);
           return CommonReturnCode.ProgramException;
        }
    }
}
