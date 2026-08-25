package com.syscom.fep.server.aa.rm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.Rmfiscout1Mapper;
import com.syscom.fep.mybatis.mapper.Rmfiscout4Mapper;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmfiscout1;
import com.syscom.fep.mybatis.model.Rmfiscout4;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMPCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class AA1513 extends RMAABase {
    private Rmfiscout1Mapper rmfiscout1Mapper = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);
    private Rmfiscout4Mapper rmfiscout4Mapper = SpringBeanFactoryUtil.getBean(Rmfiscout4Mapper.class);
    @SuppressWarnings("unused")
    private RmoutsnoMapper rmoutsnoMapper = SpringBeanFactoryUtil.getBean(RmoutsnoMapper.class);

    //"共用變數宣告"
    String rtnMessage = "";
    FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    @SuppressWarnings("unused")
    private Msgout _defMSGOUT = new Msgout();

    //"建構式"
    public AA1513(FISCData txnData) throws Exception {
        super(txnData);
    }

    //"AA進入點主程式"
    public String processRequestData() throws Exception {
        @SuppressWarnings("unused")
        String repMAC = "";
        String reqRmSNO = "";
        @SuppressWarnings("unused")
        String repUnitBank = "";
        @SuppressWarnings("unused")
        String wkStatus = "";
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        try {
            //組送往 FISC 之 Request 電文並等待財金之 Response
            _rtnCode = prepareAndSendForFISC();
            //CheckResponseFromFISC:檢核回應電文是否正確
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getmFISCBusiness().checkResponseFromFISC();
            }
            //更新電文序號/通匯序號/匯出主檔狀態及送更新多筆匯出狀態訊息至11X1-INQueue
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = updateByFISCRes(reqRmSNO);
            }

            if (_rtnCode != CommonReturnCode.Normal){
                //Modify by Jim, 2010/12/07, 不應更動FISC的回應
                //FISCRMRes.ResponseCode = CType(_rtnCode, String).PadLeft(4, "0"c)
                getmFISCRMRes().setSTATUS("");
            }
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
        }finally {
            getmTxFISCData().getLogContext().setReturnCode(_rtnCode);
            getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxFISCData().getLogContext().setMessage(rtnMessage);
            getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO,getLogContext());
            if (_rtnCode == CommonReturnCode.Normal) { //'For UI028021
                getmTxFISCData().getTxObject().setDescription(NormalRC.External_OK);
            }else {
                getmTxFISCData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            }
        }
        return "";
    }

    //"1.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )"
    @SuppressWarnings("unused")
    private FEPReturnCode prepareandInsertFeptxn() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            //2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
            rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(),"0");

            //新增交易記錄(FEPTXN )
            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = getmFISCBusiness().insertFEPTxn();
            }
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            getLogContext().setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    //3.組送往 FISC 之 Request 電文並等待財金之 Response
    private FEPReturnCode prepareAndSendForFISC() {
        FEPReturnCode rtnCode  = CommonReturnCode.Normal;
        //(1) 準備回財金的相關資料
        rtnCode = prepareFiscReq();
        if (rtnCode != CommonReturnCode.Normal){
            getLogContext().setRemark( StringUtils.join("After PrepareForFISC rtnCode=" , _rtnCode.toString()));
            sendEMS(getLogContext());
            return rtnCode;
        }
        //送1511 Req電文到財金(SendToFISC) 並等待回復
        rtnCode = getmFISCBusiness().sendRMRequestToFISC();
        return rtnCode;
    }

    private FEPReturnCode prepareFiscReq() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String reqMAC = "";
        ENCHelper encHelper = null;
        try {
            //組header()
            getmFISCRMReq().setSystemSupervisoryControlHeader("00");
            getmFISCRMReq().setSystemNetworkIdentifier("00");
            getmFISCRMReq().setAdderssControlField("00");
            getmFISCRMReq().setMessageType("0200");
            getmFISCRMReq().setProcessingCode("1513");
            getmFISCRMReq().setSystemTraceAuditNo(getmFISCBusiness().getStan());
            getmTxFISCData().setStan(getmFISCRMReq().getSystemTraceAuditNo());
            getmFISCRMReq().setTxnDestinationInstituteId(StringUtils.rightPad("950",7,'0'));
            getmFISCRMReq().setTxnSourceInstituteId(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatHbkno(),7,'0'));
            getmFISCRMReq().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).substring(1, 7) +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))); //(轉成民國年)
            getmFISCRMReq().setResponseCode(NormalRC.FISC_REQ_RC);
            getmFISCRMReq().setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(),8,' '));

            //組Body
            //呼叫AA的時候就將FISC_NO/ORG_PCODE賦值

            //產生MAC
            encHelper = new ENCHelper(getmTxFISCData());
            RefString refMac = new RefString(reqMAC);
            rtnCode = encHelper.makeRMFISCMAC(refMac);
            reqMAC = refMac.get();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            getmFISCRMReq().setMAC(StringUtils.leftPad(reqMAC,8,'0'));

            //MakeBitmap
            rtnCode = getmFISCBusiness().makeBitmap(getmFISCRMReq().getMessageType(),getmFISCRMReq().getProcessingCode(),MessageFlow.Request);
            LogHelperFactory.getTraceLogger().trace(StringUtils.join("after MakeBitmap rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));
//            LogHelperFactory.getTraceLogger().trace(String.format("after MakeBitmap rtnCode={0}{1}",rtnCode.toString(),StringUtils.leftPad(String.valueOf(rtnCode), 4, "0")));
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //"5.更新電文序號/通匯序號/匯出主檔狀態及送更新多筆匯出狀態訊息至11X1-INQueue"
    private FEPReturnCode updateByFISCRes(String repRMSNO) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            //FISCRMRes.STATUS=01-財金未收到, 02-財金收但對方行未收到, 03-對方行已收到, 04-對方行已回訊財金

            //Modify by Jim, 2011/8/11, 改成先更新RMOUT，因為更新FISCOUTSNO和RMOUTSNO的方式改成查詢 RMOUT_FISC_RTN_CODE = 0001 且序號最大的那一筆來更新
            //(3) 更新匯款狀態
            if (RMPCode.PCode1411.equals(getmFISCRMReq().getOrgPcode())) {
                if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
                    //更新電文序號
                    rtnCode = updateRmFiscOut4();
                }
            }else {
                if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
                    //不用判斷狀態都要更新FISCOUTSNO, RMOUTSNO
                    //(1) 更新電文序號
                    rtnCode = updateRmFiscOut1();

                    //(2) 更新通匯序號
                    if (rtnCode == CommonReturnCode.Normal){
                        rtnCode = updateRmOutSno();
                    }
                }
            }
            if (rtnCode == CommonReturnCode.Normal) {
                transactionManager.commit(txStatus);
            }else {
                getLogContext().setRemark(StringUtils.join("AA1513-UpdateREPNOAndSTATAndSend11X1INQueue Rollback, rtnCode=" , rtnCode.toString()));
                logMessage(Level.DEBUG,getLogContext());
                transactionManager.rollback(txStatus);
            }
            return rtnCode;
        }catch (Exception ex) {
            if(transactionManager != null) {
                transactionManager.rollback(txStatus);
            }
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateRmFiscOut1() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Rmfiscout1 defRmFiscOut1 = new Rmfiscout1();

        Rmfiscout1Mapper dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);
        try {
            defRmFiscOut1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRmFiscOut1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());

            defRmFiscOut1 = dbRMFISCOUT1.selectByPrimaryKey(defRmFiscOut1.getRmfiscout1SenderBank(),defRmFiscOut1.getRmfiscout1ReceiverBank());
            if (getmFISCRMRes().getFiscNo() != null && StringUtils.isNumeric(getmFISCRMRes().getFiscNo())) {
                if (defRmFiscOut1 == null) {
                    rtnCode = IOReturnCode.RMFISCOUT1NotFound;
                }else {
                    getLogContext().setRemark(StringUtils.join("AA1513, Before UpdateRMFISCOUT1, RMFISCOUT1_NO=" , defRmFiscOut1.getRmfiscout1No() , ", RMFISCOUT1_REP_NO=" ,
                            defRmFiscOut1.getRmfiscout1RepNo()));
                    logMessage(Level.DEBUG,getLogContext());

                    if(!defRmFiscOut1.getRmfiscout1RepNo().equals(defRmFiscOut1.getRmfiscout1No())) {
                        defRmFiscOut1.setRmfiscout1No(new BigDecimal(getmFISCRMRes().getFiscNo()).intValue());
                        if (rmfiscout1Mapper.updateByPrimaryKeySelective(defRmFiscOut1) < 1) {
                            rtnCode = IOReturnCode.RMFISCOUT1UpdateError;
                            getLogContext().setRemark("AA1513, 更新RMFISCOUT1 0 筆");
                            getLogContext().setReturnCode(rtnCode);
                            sendEMS(getLogContext());
                        }else {
                            getLogContext().setRemark(StringUtils.join("AA1513, After UpdateRMFISCOUT1, RMFISCOUT1_NO=RMFISCOUT1_REP_NO=" , defRmFiscOut1.getRmfiscout1No()));
                            logMessage(Level.DEBUG,getLogContext());
                        }
                    }
                }
            }
            else {
                getLogContext().setRemark("FISCRMRes.FISC_NO is nothing or not number, do no update");
                logMessage(Level.DEBUG,getLogContext());
            }
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateRmFiscOut4() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Rmfiscout4 defRmFiscOut4 = new Rmfiscout4();
        try {
            defRmFiscOut4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRmFiscOut4.setRmfiscout4ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());

            if (getmFISCRMRes().getFiscNo() != null && StringUtils.isNumeric(getmFISCRMRes().getFiscNo())) {
                defRmFiscOut4.setRmfiscout4No(new BigDecimal(getmFISCRMRes().getFiscNo()).intValue());
            }else {
                getLogContext().setRemark("FISCRMRes.BANK_NO is nothing or not number, do no update");
                logMessage(Level.DEBUG,getLogContext());
            }

            if (rmfiscout4Mapper.updateByPrimaryKeySelective(defRmFiscOut4) < 1) {
                getLogContext().setRemark("AA1513 更新RMFISCOUT4 0 筆");
                getLogContext().setReturnCode(IOReturnCode.RMFISCOUT4UpdateError);
                sendEMS(getLogContext());
                rtnCode = IOReturnCode.RMFISCOUT4UpdateError;
            }
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    private FEPReturnCode updateRmOutSno() {
        RmoutsnoMapper dbRmOutSNO = SpringBeanFactoryUtil.getBean(RmoutsnoMapper.class);
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Rmoutsno defRmOutSNO = new Rmoutsno();
        try {

            if (getmFISCRMRes().getFiscNo() != null && StringUtils.isNumeric(getmFISCRMRes().getFiscNo()) && getmFISCRMRes().getReceiverBank() != null &&
                getmFISCRMRes().getReceiverBank().length() > 6) {
                defRmOutSNO.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRmOutSNO.setRmoutsnoReceiverBank(getmFISCFCRMRes().getReceiverBank().substring(0,3));
                defRmOutSNO = dbRmOutSNO.selectByPrimaryKey(defRmOutSNO.getRmoutsnoSenderBank(),defRmOutSNO.getRmoutsnoReceiverBank());
                if (defRmOutSNO == null) {
                    getLogContext().setReturnCode(IOReturnCode.RMOUTSNONotFound);
                    getLogContext().setRemark("AA1513-UpdateRMOUTSNO, RMOUTSNO not found");
                    TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.BRANCH,getLogContext());
                    return IOReturnCode.RMOUTSNONotFound;
                }else {
                    getLogContext().setRemark("AA1513, Before UpdateRMOUTSNO, RMOUTSNO_NO=" + defRmOutSNO.getRmoutsnoNo() + ", RMOUTSNO_REP_NO=" + defRmOutSNO.getRmoutsnoRepNo());
                    logMessage(Level.DEBUG,getLogContext());
                    defRmOutSNO.setRmoutsnoNo(Integer.valueOf(getmFISCRMRes().getBankNo()));

                    if (dbRmOutSNO.updateByPrimaryKeySelective(defRmOutSNO) < 1) {
                        rtnCode = IOReturnCode.UpdateFail;
                        getLogContext().setRemark("AA1513, 更新RMOUTSNO 0 筆");
                        getLogContext().setReturnCode(rtnCode);
                        TxHelper.getRCFromErrorCode(rtnCode,FEPChannel.BRANCH,getLogContext());
                        return rtnCode;
                    }else {
                        getLogContext().setRemark("AA1513, After UpdateRMOUTSNO, RMOUTSNO_NO=RMOUTSNO_REP_NO=" + defRmOutSNO.getRmoutsnoNo());
                        logMessage(Level.DEBUG,getLogContext());
                    }
                }
            }else {
                getLogContext().setRemark("FISCRMRes.BANK_NO is nothing or not number, do no update");
                logMessage(Level.DEBUG,getLogContext());
            }
            return rtnCode;
        }catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
