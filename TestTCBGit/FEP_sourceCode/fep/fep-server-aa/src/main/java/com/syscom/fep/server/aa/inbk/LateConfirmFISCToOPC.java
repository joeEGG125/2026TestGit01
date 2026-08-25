package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

//''' <summary>
//''' 財金公司不明訊息通知交易係財金公司接到參加單位傳送來之訊息
//''' ( 包括Response, Confirmation,Confirmation Repeat ,Confirmation Response )
//''' 無法與原先收 (送)之訊息( Request/Rep MSG )匹配，
//''' 或參加單位送來之通知訊息無法為財金公司所辨認時，財金公司即對參加單位，送出"不明訊息通知交易 "
//''' 本支負責處理電文如下
//''' REQUEST   ：OC057
//''' </summary>
//''' <remarks>
//''' </remarks>
//''' <history>
//'''   <modify>
//'''     <modifier>Steven</modifier>
//'''     <reason>add</reason>
//'''     <date>2010/3/17</date>
//'''   </modify>
//''' </history>
public class LateConfirmFISCToOPC extends INBKAABase {
    //"共用變數宣告"
    FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    //"建構式"
    //''' <summary>
    //''' AA的建構式,在這邊初始化及設定其他相關變數
    //''' </summary>
    //''' <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
    //''' <remarks>
    //''' </remarks>
    public LateConfirmFISCToOPC(FISCData txnData) throws Exception {
        super(txnData);
    }

    // "AA進入點主程式"
    //    ''' <summary>
    //    ''' 程式進入點
    //    ''' </summary>
    //    ''' <returns>Response電文</returns>
    //    ''' <remarks></remarks>
    @Override
    public String processRequestData() {
        try {
            //1.拆解並檢核財金發動的Request電文Header
            _rtnCode = getFiscBusiness().checkHeader(getFiscOPCCon(),false);
            //2.判斷是否為Garbled Message
            if (_rtnCode == FISCReturnCode.MessageTypeError ||
                    _rtnCode==FISCReturnCode.TraceNumberDuplicate ||
                    _rtnCode==FISCReturnCode.OriginalMessageError ||
                    _rtnCode==FISCReturnCode.STANError ||
                    _rtnCode==FISCReturnCode.SenderIdError ||
                    _rtnCode==FISCReturnCode.CheckBitMapError) {
                getFiscBusiness().sendGarbledMessage(getFiscOPCCon().getEj(),_rtnCode,getFiscOPCCon());
                return "";
            }
            //3.更新交易紀錄檔
            _rtnCode = this.updateFeptxn();
        }
        catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName);
            sendEMS(getLogContext());
        }
        finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscOPCCon().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Request);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG,getLogContext());
        }
        return "";
    }

    //"本支AA共用之sub routine"
    //    ''' <summary>
    //    ''' 更新交易紀錄檔
    //    ''' </summary>
    //    ''' <returns></returns>
    //    ''' <remarks></remarks>
    private FEPReturnCode updateFeptxn() throws Exception {
        getFiscBusiness().getFeptxn().setFeptxnPending((short)2); //正常
        getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCCon().getResponseCode());
        if (_rtnCode == CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
        }
        else {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        }
        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
        getFiscBusiness().getFeptxn().setFeptxnTraceEjfno(getTxData().getEj());

        //modified by Maxine on 2011/10/17 for FEPTXN_REQ_DATETIME[1:8]代替FEPTXN_TX_DATE
        getFiscBusiness().getFeptxn().setFeptxnReqDatetime(CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscOPCCon().getTxnInitiateDateAndTime().substring(0,6),7,"0")));
        //fiscBusiness.FepTxn.FEPTXN_TX_DATE = DateLib.ROCStringToADString(fiscOPCCon.TxnInitiateDateAndTime.Substring(0, 6).PadLeft(7, "0"c))
        getFiscBusiness().getFeptxn().setFeptxnBkno(getFiscOPCCon().getTxnSourceInstituteId().substring(0,3));
        getFiscBusiness().getFeptxn().setFeptxnStan(getFiscOPCCon().getSystemTraceAuditNo());
        if (this.feptxnDao.updateByPrimaryKey(getFiscBusiness().getFeptxn()) < 0) {
            //If DBFepTxn.UpdateFEPTXNByDateBknoStan(fiscBusiness.FepTxn) < 0 Then
            return IOReturnCode.FEPTXNDeleteNotFound;
        }
        return FEPReturnCode.Normal;
    }
}
