package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class AA3101L extends INBKAABase {
    //共用變數宣告
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    //建構式
    public AA3101L(FISCData txnData) throws Exception {
        super(txnData);
    }

    //AA進入點主程式
    @Override
    public String processRequestData() {
        try {
            //處理財金Response電文(OC010)
            _rtnCode = processResponse();

            //更新原交易紀錄檔
            _rtnCode = updateFEPTXN();

            //組送財金Confirm 電文(OC011)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                _rtnCode = prepareForFISC();
            }

            //送財金(Confirm)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                _rtnCode = getFiscBusiness().sendRequestToFISCOpc();
            }
        }catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName);
            sendEMS(getLogContext());
        }finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscOPCCon().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    //本支AA共用之sub routine"
    //''' <summary>
    //''' 處理財金Response電文(OC010)
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

        rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(),false);
        if (rtnCode == FISCReturnCode.MessageTypeError ||
                rtnCode == FISCReturnCode.TraceNumberDuplicate ||
                rtnCode == FISCReturnCode.OriginalMessageError ||
                rtnCode == FISCReturnCode.STANError ||
                rtnCode == FISCReturnCode.SenderIdError ||
                rtnCode == FISCReturnCode.CheckBitMapError) {
            getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
            return rtnCode;
        }
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        //檢核MAC
        getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    // ''' <summary>
    //''' 更新原交易紀錄檔
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String txdate = "";
        String bkno = "";
        String stan = "";

        //讀取原交易記錄檔
        txdate = CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscOPCRes().getTxnInitiateDateAndTime().substring(0,6),7,"0"));
        bkno = getFiscOPCRes().getTxnSourceInstituteId().substring(0,3);
        stan = getFiscOPCRes().getSystemTraceAuditNo();
        getFiscBusiness().setFeptxn(feptxnDao.getFEPTXNByReqDateAndStan(txdate,bkno,stan));
        if (getFiscBusiness().getFeptxn() == null) {
            return IOReturnCode.FEPTXNUpdateNotFound;
        }

        //更新原交易記錄檔
        getFiscBusiness().getFeptxn().setFeptxnPending((short)2);
        getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());

        //拆解RESPONSE電文成功,FEPTXN_AA_RC仍應記錄0601
        if (_rtnCode == CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
        }else {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        }

        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
        getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //''' <summary>
    //''' 組傳送財金Confirm電文
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    //''' <modify>
    //'''     <modifier>HusanYin</modifier>
    //'''     <reason>修正Const RC</reason>
    //'''     <date>2010/11/25</date>
    //''' </modify>
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

        getLogContext().setProgramName(ProgramName);
        getFiscBusiness().getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(FISCReturnCode.FISCTimeout, FEPChannel.FISC,getLogContext()));

        rtnCode = getFiscBusiness().prepareHeader("0602");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = prepareBody();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        RefString refMac = new RefString(getFiscOPCCon().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), refMac);
        getFiscOPCCon().setMAC(refMac.get());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCCon().getMessageType(), getFiscOPCCon().getProcessingCode(), MessageFlow.Confirmation);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscOPCCon().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //更新原交易記錄檔
        getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCCon().getResponseCode());
        getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
        getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());
        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //''' <summary>
    //''' 電文Body
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getFiscOPCCon().setAPID(getFeptxn().getFeptxnApid());

//            if ("1600".equals(getFeptxn().getFeptxnApid())) {
//                getFiscOPCCon().setCUR("001");
//            }
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processResponse");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
