package com.syscom.fep.server.aa.inbk;

import java.util.Calendar;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FundlogExtMapper;
import com.syscom.fep.mybatis.model.Fundlog;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class AA5313 extends INBKAABase {

    //"共用變數宣告"
    FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    Fundlog fundLog = new Fundlog();
    private FundlogExtMapper fundlogExtMapper = SpringBeanFactoryUtil.getBean(FundlogExtMapper.class);

    // "建構式"
    public AA5313(INBKData txnData) throws Exception {
        super(txnData);
    }

    //"AA進入點主程式"
    @Override
    public String processRequestData() {
        Boolean needUpdateFEPTXN = false;
        try{
            //QueryFUNDLOGByFGSeqno
            _rtnCode = getFundlogByFGSeqno();

            //基本檢核
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = checkData();
            }

            //更新跨行基金增減記錄檔 FUNDLOG
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = updateFundlog("P");
            }

            //Prepare 交易紀錄檔FEPTXN
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = getFiscBusiness().prepareFEPTXNfromFUNDLOG(fundLog);
            }

            //產生財金通知訊息
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = prepareForFISC();
            }

            //InsertFEPTXN
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = getFiscBusiness().insertFEPTxn();
            }

            //2015/07/14 Modify by Ruling for InsertFeptxn成功，此flag要改成true，否則會發生ProgramExeption
            if (_rtnCode == CommonReturnCode.Normal){
                needUpdateFEPTXN = true;
            }

            //將組好的財金電文送給財金
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = getFiscBusiness().sendMessageToFISC(MessageFlow.Request);
            }

            //檢核財金Response電文 Header
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = processResponse();
            }

            //更新交易記錄檔
            if (needUpdateFEPTXN){
                _rtnCode = updateFEPTXN();
            }

            //更新交易記錄(FEPTXN)/跨行基金增減記錄檔(FUNDLOG)
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = updateTxData();
            }

            //add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
            getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + feptxn.getFeptxnRepRc() + ";");
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());
            if (NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())){
                getLogContext().setpCode(feptxn.getFeptxnPcode());
                getLogContext().setDesBkno(feptxn.getFeptxnDesBkno());
                getLogContext().setFiscRC(NormalRC.FISC_OK);
                getLogContext().setMessageGroup("2"); //CLR
                getLogContext().setProgramName(ProgramName);
                //2015/07/14 Modify by Ruling for EMS增加顯示跨行基金減少金額
                getLogContext().setMessageParm13(StringUtils.join("減少金額:", MathUtil.toString(fundLog.getFundlogFgAmt(),"#,#.00")));
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankBalanceReduceRequest,getLogContext()));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG, getLogContext());
            }
        }catch (Exception ex){
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        }finally {
            if(_rtnCode != FEPReturnCode.Normal && _rtnCode != FEPReturnCode.RecordStatusError) {
                updateFundlog("N");
            }
            prepareUIfromFundlog();
            getINBKtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getINBKtxData().getLogContext().setMessage(getFiscCLRReq().getFISCMessage());
            getINBKtxData().getLogContext().setProgramName(this.aaName);
            getINBKtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(getINBKtxData().getLogContext());
        }
        return "";
    }

    //"本支AA共用之sub routine"
    private FEPReturnCode getFundlogByFGSeqno() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Fundlog fundLognew = new Fundlog();
        fundLognew.setFundlogFgSeqno(getINBKRequest().getFGSEQNO());
//        dbFUNDLOG.getFundlogByFGSeqno(fundLog);
        fundLog = fundlogExtMapper.getFUNDLOGByFGSeqno(fundLognew.getFundlogFgSeqno());
        return rtnCode;
    }

    private FEPReturnCode checkData(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        if (!FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).equals(fundLog.getFundlogTxDate()) ||
           !"N".equals(fundLog.getFundlogStatus())) {
            return FEPReturnCode.RecordStatusError;
        }
        return rtnCode;
    }

    //UpdateFUNDLOG
    private FEPReturnCode updateFundlog(String STATUS) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        fundLog.setFundlogStatus(STATUS);  ///*已送出放行,但PENDING中*/
        if (fundlogExtMapper.updateByPrimaryKeySelective(fundLog) < 0) {
            return IOReturnCode.UpdateFail;
        }
        return rtnCode;
    }

    //組傳送財金Request電文
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(),getINBKtxData());
        try {
            rtnCode = getFiscBusiness().prepareHeader("0500");
            if (rtnCode != CommonReturnCode.Normal){
                return rtnCode;
            }
            getFiscCLRReq().setFgAmt(fundLog.getFundlogFgAmt().toString());
            getFiscCLRReq().setFgSeqno(fundLog.getFundlogFgSeqno());
            //產生MAC
            RefString mac = new RefString(getFiscCLRReq().getMAC());
            rtnCode = encHelper.makeFiscMac(getFiscCLRReq().getMessageType(),mac);
            mac.set(mac.get());
            getFiscCLRReq().setMAC(mac.get());
            if (rtnCode != CommonReturnCode.Normal){
                return rtnCode;
            }
            //Make Bit Map
            rtnCode = getFiscBusiness().makeBitmap(getFiscCLRReq().getMessageType(),getFiscCLRReq().getProcessingCode(),MessageFlow.Request);
            if (rtnCode != CommonReturnCode.Normal){
                return rtnCode;
            }

            rtnCode = getFiscCLRReq().makeFISCMsg();
            if (rtnCode != CommonReturnCode.Normal){
                return rtnCode;
            }
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareForFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    //檢核財金Response電文
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());
        try {
            //檢核Header
            rtnCode = getFiscBusiness().checkHeader(getFiscCLRRes(),false);
            if (rtnCode == FISCReturnCode.MessageTypeError ||
                    rtnCode == FISCReturnCode.TraceNumberDuplicate ||
                    rtnCode == FISCReturnCode.OriginalMessageError ||
                    rtnCode == FISCReturnCode.STANError ||
                    rtnCode == FISCReturnCode.SenderIdError) {
                getFiscBusiness().sendGarbledMessage(getFiscCLRReq().getEj(), rtnCode, getFiscCLRRes());
                return rtnCode;
            }
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processResponse");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //更新FEPTXN
    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode = null;
        //modified by Maxine for 9/28 修改, 寫入處理結果
        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true)); //AA Complete
        getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        if(_rtnCode != CommonReturnCode.Normal){
            getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //處理結果=Reject
        }else {
            //modified By Maxine on 2011/11/07 for 財金回應error FEPTXN_TXRUST = "R"
            if (NormalRC.FISC_OK.equals(getFiscCLRRes().getResponseCode())) {
                getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
            }else {
                getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
            }
            //fiscBusiness.FepTxn.FEPTXN_TXRUST = "A" '處理結果=成功
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscCLRRes().getResponseCode());
        }
        rtnCode = getFiscBusiness().updateTxData();
        if (_rtnCode != CommonReturnCode.Normal) {
            return _rtnCode;
        }else {
            return rtnCode;
        }
    }

    //更新交易記錄(FEPTXN/FUNDLOG)
    private FEPReturnCode updateTxData() {
        FEPReturnCode rtnCode = null;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //dbFUNDLOG = New Tables.DBFUNDLOG(DBFepTxn.Database)
            //DBFepTxn.Database.BeginTransaction()
            //fiscBusiness.FepTxn.FEPTXN_REP_RC = fiscCLRRes.ResponseCode
            rtnCode = getFiscBusiness().updateTxData();
            if (rtnCode != CommonReturnCode.Normal){
                return _rtnCode;
            }
            ///*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]) */
            if (NormalRC.FISC_OK.equals(getFiscCLRRes().getResponseCode())){
                fundLog.setFundlogStatus("Y"); //已放行
            }else {
                fundLog.setFundlogStatus("N"); //已放行
            }
            fundLog.setFundlogTxTime(getFiscBusiness().getFeptxn().getFeptxnTxTime());
            fundLog.setFundlogBkno(getFiscBusiness().getFeptxn().getFeptxnBkno());
            fundLog.setFundlogStan(getFiscBusiness().getFeptxn().getFeptxnStan());
            fundLog.setFundlogSupno(getINBKRequest().getSUPID());
            if (fundlogExtMapper.updateByPrimaryKeySelective(fundLog) < 0) {
                return IOReturnCode.UpdateFail;
            }
            transactionManager.commit(txStatus);
            return CommonReturnCode.Normal;
        }catch (Exception ex) {
            transactionManager.rollback(txStatus);
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //將財金回應RC傳回 UI
    private FEPReturnCode prepareUIfromFundlog() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        getINBKResponse().setTXDATE(fundLog.getFundlogTxDate());
        getINBKResponse().setFGAMT(fundLog.getFundlogFgAmt());
        getINBKResponse().setSTAN(fundLog.getFundlogStan());
        getINBKResponse().setSTATUS(fundLog.getFundlogStatus());
        getINBKResponse().setTLRID(fundLog.getFundlogTlrno());
        getINBKResponse().setSUPID(fundLog.getFundlogSupno());
        getLogContext().setProgramName(ProgramName);
        if (_rtnCode != CommonReturnCode.Normal) {
            getINBKResponse().setRESULT(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
        }else {
            getINBKResponse().setRESULT(getFiscCLRRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(getFiscCLRRes().getResponseCode(), FEPChannel.FISC,getLogContext()));
        }
        return rtnCode;
    }
}
