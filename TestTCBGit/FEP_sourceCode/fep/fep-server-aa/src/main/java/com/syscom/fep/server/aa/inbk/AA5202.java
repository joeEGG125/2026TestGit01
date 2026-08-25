package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class AA5202 extends INBKAABase{
    @SuppressWarnings("unused")
    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    /**
     AA的建構式,在這邊初始化及設定其他相關變數

     @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件


     */
    public AA5202(FISCData txnData) throws Exception {
        super(txnData);
    }


    @Override
    public String processRequestData() {
        try {

            //準備交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareFEPTXN();
            }

            //組送財金Request 電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }

            //新增交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().insertFEPTxn();
            }

            //送財金
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Request);
            }

            //處理財金Response電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processResponse();
            }

            //add by Maxine on 2011/10/12 for 不管是否收到response都應該紀錄FEPTXN
            _rtnCode = updateFEPTXN();

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName , ".processRequestData"));
            sendEMS(getLogContext());
        } finally {
            //fiscCLRRes.ResponseCode = Convert.ToInt32(_rtnCode).ToString("0000")
            getLogContext().setProgramName(ProgramName);
            if (_rtnCode != CommonReturnCode.Normal) {
                getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            } else {
                getTxData().getTxObject().setDescription(StringUtils.join(getFiscCLRRes().getResponseCode() , "-" , TxHelper.getMessageFromFEPReturnCode(getFiscCLRRes().getResponseCode(), FEPChannel.FISC, getLogContext())));
            }
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscCLRReq().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }

        return "";
    }

    /**
     "Prepare交易紀錄檔FEPTXN"

     @return

     */
    private FEPReturnCode prepareFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        rtnCode = getFiscBusiness().prepareFeptxnForUICommon("5202");
        getFiscBusiness().getFeptxn().setFeptxnApid(getFiscCLRReq().getAPID5());
        return rtnCode;
    }

    /**
     產生財金通知訊息

     @return

     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //Prepare Header & Body
        rtnCode = getFiscBusiness().prepareHeader("0500");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //Make Bitmap
        rtnCode = getFiscBusiness().makeBitmap(getFiscCLRReq().getMessageType(), getFiscCLRReq().getProcessingCode(), MessageFlow.Request);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscCLRReq().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    /**
     檢核財金Response電文 Header

     @return

     <modifier>HusanYin</modifier>
     <reason>修正Const RC</reason>
     <date>2010/11/25</date>
     <reason>修正如fiscCLRRes.ResponseCode 不是NormalRC.FISC_OK 不需再轉RC</reason>
     <date>2011/3/1</date>
     </modify>
     */
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            //檢核Header
            rtnCode = getFiscBusiness().checkHeader(getFiscCLRRes(), false);
            if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
                getFiscBusiness().sendGarbledMessage(getFiscCLRRes().getEj(), rtnCode, getFiscCLRRes());
                return rtnCode;
            }

            //'add by Maxine on 2011/08/18 for UpdateFEPTXN的使用新的rtncode,送EMS
            //Dim rtnCode1 As FEPReturnCode = fiscBusiness.UpdateTxData()
            //If rtnCode1 <> CommonReturnCode.Normal Then
            //    LogContext.ProgramName = ProgramName
            //    LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(rtnCode1, LogContext)
            //    LogContext.ProgramName = ProgramName
            //    LogMessage(LogLevel.Error, LogContext)
            //End If
            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName , ".processResponse"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = null;

        //modified by Maxine for 9/28 修改, 寫入處理結果
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); //AA Complete
        getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        if (_rtnCode == CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); //處理結果=成功
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscCLRRes().getResponseCode());
            //modified By Maxine on 2011/11/04 for 財金回應RC不用轉換
            if (!NormalRC.FISC_OK.equals(getFiscCLRRes().getResponseCode())) {
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
                //LogContext.ProgramName = ProgramName
                //LogContext.Remark = TxHelper.GetRCFromErrorCode(fiscCLRRes.ResponseCode, FEPChannel.FISC, FEPChannel.FEP, LogContext)
                //rtnCode = CType(TxHelper.GetRCFromErrorCode(fiscCLRRes.ResponseCode, FEPChannel.FISC, FEPChannel.FEP, LogContext), FEPReturnCode)
            }
        } else {
            getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //處理結果=Reject
        }

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            getLogContext().setProgramName(ProgramName);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode, getLogContext()));
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.ERROR, getLogContext());
        }
        //modified By Maxine on 2011/11/07
        if (_rtnCode != CommonReturnCode.Normal) {
            return _rtnCode;
        } else {
            return rtnCode;
        }
    }


}
