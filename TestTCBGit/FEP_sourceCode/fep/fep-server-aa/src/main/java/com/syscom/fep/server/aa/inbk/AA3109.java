package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

/**
 * 接收 UI013106之APID，送出緊急停止跨行連線作業通知電文給財金
 * 本支負責處理電文如下
 * REQUEST   ：OC035
 * RESPONSE  ：OC036
 * AA程式撰寫原則:
 * AA的程式主要為控制交易流程,Main為AA程式的進入點,在Main中的程式為控制交易的過程該如何進行
 * 請不要在Main中去撰寫實際的處理細節,儘可能將交易過程中的每一個"步驟",以副程式的方式來撰寫,
 * 而這些步驟,如果可以共用的話,請將該步驟寫在相關的Business物件中
 * 如果該步驟只有該AA會用到的話,再寫至自己AA中的類別中
 *
 */
public class AA3109 extends INBKAABase{

    /**
     * 共用變數宣告
     */
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    /**
     * 建構式
     * AA的建構式,在這邊初始化及設定其他相關變數
     * 初始化後,AA可以透過INBKBusiness變數取得Business.INBK物件,
     * INBKRequest變數取得INBKGeneral中的Request物件,INBKResponse變數取得INBKGeneral中的Response物件
     *  FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception e
     */
    public AA3109(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     * 程式進入點
     *
     * @return Response電文
     * @throws Exception e
     */
    @Override
    public String processRequestData() throws Exception {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;


        try {
            // 1.準備交易記錄檔
            _rtnCode = getFiscBusiness().prepareFeptxnOpc("3109");
            // 2.新增交易記錄檔
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = getFiscBusiness().insertFEPTxn();
            }
            // 3.商業邏輯檢核
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = checkBusinessRule();
            }
            //4.組送財金Request 電文(OC045)
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = prepareForFISC();
            }
            //5.送財金
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = getFiscBusiness().sendRequestToFISCOpc();
            }
            //6.處理財金Response電文(OC046)
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = processResponse();
            }
            //7.更新交易記錄檔
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = updateFEPTXN();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        } finally {
            getLogContext().setProgramName(ProgramName);
            if (!CommonReturnCode.Normal.equals(_rtnCode)){
                getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode,logContext));
            }else {
                getTxData().getTxObject().setDescription(getFiscOPCRes().getResponseCode()+"-"+TxHelper.getMessageFromFEPReturnCode(getFiscOPCRes().getResponseCode(), FEPChannel.FISC,logContext));
            }
            logContext.setProgramFlowType(ProgramFlow.AAOut);
            logContext.setMessage(getFiscOPCRes().getFISCMessage());
            logContext.setProgramName(this.aaName);
            logContext.setMessageFlowType(MessageFlow.Response);
            logContext.setRemark(getTxData().getTxObject().getDescription());
            logMessage(Level.DEBUG,logContext);
        }
        return "";
    }

    /**
     * 3.商業邏輯檢核
     * OPC電文檢核相關邏輯
     * @return FEPReturnCode
     */
    private FEPReturnCode checkBusinessRule(){
        try {
            // 0 - 日終 HOUSE KEEPING 完成
            // 1 - FISC Wakeup Call 完成或M-BANK Wakeup Call完成
            // 2 - FISC Key-Syn Call完成
            // 3 - FISC Notice Call完成
            // 9 - 參加單位關機交易完成
            // D - 參加單位不營業
            // 檢核本行OP狀態，必須等於3，FISC NOTICE CALL完成才可執行本交易
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())){
                return FISCReturnCode.INBKStatusError;
            }
            return CommonReturnCode.Normal;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode prepareForFISC(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //電文Header
        rtnCode = getFiscBusiness().prepareHeader("0600");
        if (!CommonReturnCode.Normal.equals(rtnCode)){
            return rtnCode;
        }

        //電文Body
        rtnCode = prepareBody();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        //產生財金Flatfile電文
        rtnCode = getFiscOPCReq().makeFISCMsg();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }
        return rtnCode;
    }

    private FEPReturnCode prepareBody(){
        @SuppressWarnings("unused")
        String desReturn = StringUtils.EMPTY;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        // fiscOPCReq.APID已由UI傳入
        // fiscOPCReq.BKNO已由UI傳入
        // fiscOPCReq.CUR 已由UI傳入

        //產生MAC()
        RefString mac = new RefString(getFiscOPCReq().getMAC());
        _rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), mac);
        getFiscOPCReq().setMAC(mac.get());
        if (!_rtnCode.equals(CommonReturnCode.Normal)) {
            return _rtnCode;
        }

        // 產生Bitmap
        _rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
        if (!_rtnCode.equals(CommonReturnCode.Normal)) {
            return _rtnCode;
        }
        return CommonReturnCode.Normal;
    }
    private FEPReturnCode processResponse(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        String desReturnMac = StringUtils.EMPTY;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

        //檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC)
        rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
        if (rtnCode.equals(FISCReturnCode.MessageTypeError) ||
                rtnCode.equals(FISCReturnCode.TraceNumberDuplicate) ||
                rtnCode.equals(FISCReturnCode.OriginalMessageError) ||
                rtnCode.equals(FISCReturnCode.STANError) ||
                rtnCode.equals(FISCReturnCode.SenderIdError) ||
                rtnCode.equals(FISCReturnCode.CheckBitMapError)) {
            getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
            return rtnCode;
        }

        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        // 檢核APID
        if (!getFiscOPCReq().getAPID().equals(getFiscOPCRes().getAPID())) {
            // FISC RC=0401
            return FISCReturnCode.OriginalMessageDataError;
        }

        // 檢核MAC
        getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        return rtnCode;

    }

    private FEPReturnCode updateFEPTXN(){
        FEPReturnCode rtnCode;
        //fiscBusiness.FepTxn.FEPTXN_REP_RC = fiscOPCRes.ResponseCode
        getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());

        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
        /* 2026/05/12 2270 資金調撥服務 */
        if (getFiscBusiness().getFeptxn().getFeptxnRepRc().equals("0001")) {
            //處理結果=成功
            getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
        } else {
            //處理結果=Reject
            getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
        }

        rtnCode = getFiscBusiness().updateTxData();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        } else {
            return _rtnCode;
        }
    }
}
