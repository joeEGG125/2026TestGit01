package com.syscom.fep.server.aa.inbk;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class AA3107L extends INBKAABase {
    //共用變數宣告
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    //建構式
    public AA3107L(FISCData txnData) throws Exception {
        super(txnData);
    }

    //AA進入點主程式
    @Override
    public String processRequestData() {
        try {
            //1.處理財金Response電文(OC039)
            _rtnCode = processResponse();

            //2.更新原交易紀錄檔
            _rtnCode = updateFEPTXN(MessageFlow.Response.getValue());

            //3.組送財金Confirm 電文(OC040)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                _rtnCode = prepareConForFISC();
            }

            //4.送財金(Confirm)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                _rtnCode = getFiscBusiness().sendConfirmToFISCOpc();
            }
        }catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName);
            sendEMS(getLogContext());
        }finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscOPCReq().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    //1.拆解並檢核由財金回覆的Response電文
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

        //檢核Header
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

        //檢核MAC
        getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //2.更新FEPTXN
    private FEPReturnCode updateFEPTXN(Integer flow) {
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
        switch (MessageFlow.fromValue(flow)) {
            case Response:
                getFiscBusiness().getFeptxn().setFeptxnPending((short)2);
                getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
                getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
                break;
            case Confirmation:
                getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCReq().getResponseCode());
                getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
                break;
            default:
                break;
        }

        //拆解RESPONSE電文成功,FEPTXN_AA_RC仍應記錄0601
        if (_rtnCode == CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
        }else {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        }

        getFiscBusiness().getFeptxn().setFeptxnEjfno(getTxData().getEj());

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //3.組傳送財金Confirm電文(OC040)
    private FEPReturnCode prepareConForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

        getLogContext().setProgramName(ProgramName);
        getFiscBusiness().getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(FISCReturnCode.FISCTimeout, FEPChannel.FISC,getLogContext()));

        //電文Header
        rtnCode = getFiscBusiness().prepareHeader("0602");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //電文Body
        rtnCode = prepareConBody();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //產生財金Flatfile電文
        rtnCode = getFiscOPCCon().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //更新FEPTXN
        rtnCode = updateFEPTXN(MessageFlow.Confirmation.getValue());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //ATM電文檢核相關邏輯
    private FEPReturnCode prepareConBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

        getFiscOPCCon().setAPID(getFiscOPCRes().getAPID());
        // getFiscOPCCon().setCUR(getFiscOPCRes().getCUR());

        //產生MAC
        RefString refMac = new RefString(getFiscOPCCon().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), refMac);
        getFiscOPCCon().setMAC(refMac.get());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //產生Bitmap
        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCCon().getMessageType(), getFiscOPCCon().getProcessingCode(), MessageFlow.Confirmation);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }
}
