package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.FcrmstatMapper;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class AA3101 extends INBKAABase {
    //共用變數宣告
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCode2 = CommonReturnCode.Normal;
    Fcrmstat defFCRMSTAT = new Fcrmstat();
    FcrmstatMapper dbFCRMSTAT = SpringBeanFactoryUtil.getBean(FcrmstatMapper.class);

    //建構式
    public AA3101(FISCData txnData) throws Exception {
        super(txnData);
    }

    //AA進入點主程式
    @Override
    public String processRequestData() {
        boolean needUpdateFEPTXN = false;
        try {
            //Modify by henny for 修改主流程AA一開始先INSERT FEPTXN後才檢核狀態並更新FEPTXN_AA_RC 20110610
            //2.準備交易記錄檔
            _rtnCode = getFiscBusiness().prepareFeptxnOpc("3101");

            //3.新增交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().insertFEPTxn();
                needUpdateFEPTXN = true;
            }
            //1.檢查狀態
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = checkData();
            }
            //4.組送財金Request電文(OC009)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }
            //5.送財金Request電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendRequestToFISCOpc();
            }
            //6.處理財金Respons電文(OC010)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processResponse();
            }
            //7.更新交易記錄檔
            if (needUpdateFEPTXN) {
                _rtnCode2 = updateFEPTXN(MessageFlow.Response.getValue());
            }
            if (_rtnCode2 != CommonReturnCode.Normal) {
                _rtnCode = _rtnCode2;
                return "";
            }
            //8.訊息檢查合法&財金回應RC="0001"需以AP-ID更新系統狀態，改為 AP CHECK IN中
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = doBusiness();
            }
            //9.組送財金Confirm電文(OC011)
            if (NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                _rtnCode = prepareConfirmForFISC();
                //10.送財金Confirm電文
                if (_rtnCode == CommonReturnCode.Normal) {
                    _rtnCode = getFiscBusiness().sendConfirmToFISCOpc();
                }
                //11.更新交易記錄檔
                _rtnCode2 = updateFEPTXN(MessageFlow.Confirmation.getValue());

                getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_CON_RC=" , getFeptxn().getFeptxnConRc() , ";"));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG,getLogContext());
                if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnConRc())) {
                    getLogContext().setpCode(getFeptxn().getFeptxnPcode());
                    getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
                    getLogContext().setFiscRC(NormalRC.FISC_OK);
                    getLogContext().setMessageGroup("1"); //OPC
                    switch (getFiscOPCReq().getAPID()) {
                        case "1000":
                            getLogContext().setMessageParm13("跨行匯款系統" + "(" + getFiscOPCReq().getAPID() + ")");
                            break;
                        case "1600":
                            getLogContext().setMessageParm13( "外幣匯款系統" + "(" + getFiscOPCReq().getAPID() + ")");
                            break;
                    }
                    getLogContext().setProgramName(ProgramName);
                    getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankCheckIn,getLogContext()));
                    logMessage(Level.DEBUG,getLogContext());
                }
            }
        }catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        }finally {
            getLogContext().setProgramName(ProgramName);
            if (_rtnCode != CommonReturnCode.Normal) {
                getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            }else if (_rtnCode2 != CommonReturnCode.Normal) {
                getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode2, getLogContext()));
            } else {
                getTxData().getTxObject().setDescription(getFiscOPCRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(getFiscOPCRes().getResponseCode(), FEPChannel.FISC,getLogContext()));
            }
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            //modified by maxine on 2011/07/11 for 避免送多次EMS
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            //LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    //"1.檢查狀態"
    //''' <summary>
    //''' 檢查狀態
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    //'''   <modify>
    //'''     <modifier>Ruling</modifier>
    //'''     <reason>修正未執行匯款批次（未清匯款狀態及序號）今日卻可執行匯款CHECKIN交易，
    //'''             多加SYSSTAT_AOCT_100不等於1的判斷來避免此問題的發生</reason>
    //'''     <date>2011/05/24</date>
    //'''   </modify>
    private FEPReturnCode checkData() throws Exception {
//        if ("1600".equals(getFiscOPCReq().getAPID())) {
//            defFCRMSTAT.setFcrmstatCurrency(getFiscOPCReq().getCUR());
//            defFCRMSTAT = dbFCRMSTAT.selectByPrimaryKey(defFCRMSTAT.getFcrmstatCurrency());
//        }

        //檢核本行OP狀態，必須為"3" (FISC NOTICE CALL完成) 才可執行本交易
        if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
            return FISCReturnCode.INBKStatusError;
        }

        //檢核AP系統是否在正常狀態
        switch (getFiscOPCReq().getAPID()){
            case "1000": //跨行匯款系統
                if (!"0".equals(SysStatus.getPropertyValue().getSysstatMbact1000()) || !"1".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                    return FISCReturnCode.INBKStatusError;
                }
                break;
            case "1600": //外幣匯款系統
                if (defFCRMSTAT.getFcrmstatMbactrm()==null || !"0".equals(defFCRMSTAT.getFcrmstatMbactrm())) {
                    return FISCReturnCode.INBKStatusError;
                }
                break;
        }
        return CommonReturnCode.Normal;
    }

    //4.組送財金Request  電文(OC009)"
    //''' <summary>
    //''' 組傳送財金Request電文
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        rtnCode = getFiscBusiness().prepareHeader("0600");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        RefString refMac = new RefString(getFiscOPCReq().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), refMac);
        getFiscOPCReq().setMAC(refMac.get());
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscOPCReq().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        return rtnCode;
    }

    //6.處理財金Response 電文(OC010)"
    //''' <summary>
    //''' 處理財金Response電文(OC008)
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            //檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC並結束程式)
            rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
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

            //檢核APID
            // if ( !getFiscOPCRes().getAPID().equals(getFiscOPCReq().getAPID()) || !getFiscOPCRes().getCUR().equals(getFiscOPCReq().getCUR())) {
            if ( !getFiscOPCRes().getAPID().equals(getFiscOPCReq().getAPID())) {
                return FISCReturnCode.OriginalMessageDataError;
            }

            //檢核MAC
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
            rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }
            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processResponse");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //9.組傳送財金Confirm電文(OC011)"
    //''' <summary>
    //''' 組傳送財金Confirm電文
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    //''' <modify>
    //'''     <modifier>HusanYin</modifier>
    //'''     <reason>修正Const RC</reason>
    //'''     <date>2010/11/25</date>
    //'''     <reason>修正FEPTXN_CON_RC傳入參數</reason>
    //'''     <date>2011/04/21</date>
    //''' </modify>
    private FEPReturnCode prepareConfirmForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        if (_rtnCode == CommonReturnCode.Normal) {
            getFiscBusiness().getFeptxn().setFeptxnConRc(NormalRC.FISC_OK);
        }else {
            getLogContext().setProgramName(ProgramName);
            getFiscBusiness().getFeptxn().setFeptxnConRc(TxHelper.getRCFromErrorCode(_rtnCode,FEPChannel.FISC,getLogContext()));
        }
        rtnCode = getFiscBusiness().prepareHeader("0602");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //APID
        getFiscOPCCon().setAPID(getFiscOPCReq().getAPID());

        //CUR
//        if ("1600".equals(getFiscOPCCon().getAPID())) {
//            getFiscOPCCon().setCUR(getFiscOPCReq().getCUR());
//        }

        RefString refMac = new RefString(getFiscOPCCon().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), refMac);
        getFiscOPCCon().setMAC(refMac.get());
        if (rtnCode != CommonReturnCode.Normal) {
            //DES失敗
            getLogContext().setProgramName(ProgramName);
            getFiscOPCCon().setResponseCode(TxHelper.getRCFromErrorCode(rtnCode,FEPChannel.FISC,getLogContext()));
            getFiscOPCCon().setMAC("00000000");
        }

        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCCon().getMessageType(), getFiscOPCCon().getProcessingCode(), MessageFlow.Confirmation);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        rtnCode = getFiscOPCCon().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //8.DoBusiness
    private FEPReturnCode doBusiness() throws Exception {
        SysstatExtMapper db = SpringBeanFactoryUtil.getBean("sysstatExtMapper");
        //modify 新增DBSYSSTAT的參數  20110412 by Husan
//        db.LogAuditTrail = True
//        db.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name
        if (NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode()) || "3001".equals(getFiscOPCRes().getResponseCode())) {
            if ("1000".equals(getFiscOPCRes().getAPID())) {
                SysStatus.getPropertyValue().setSysstatMbact1000("1");
                SysStatus.getPropertyValue().setSysstatMbact1100("1");
                SysStatus.getPropertyValue().setSysstatMbact1200("1");
                SysStatus.getPropertyValue().setSysstatMbact1300("1");
                SysStatus.getPropertyValue().setSysstatMbact1400("1");
                if (db.updateByPrimaryKeySelective(SysStatus.getPropertyValue()) < 0) {
                    return IOReturnCode.SYSSTATUpdateError;
                }
            }else if ("1600".equals(getFiscOPCRes().getAPID())) {
                defFCRMSTAT.setFcrmstatMbactrm("1");
                //modify 新增DBFCRMSTAT的參數  20110412 by Husan
                //dbFCRMSTAT.LogAuditTrail = True
                //dbFCRMSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name

                if (dbFCRMSTAT.updateByPrimaryKeySelective(defFCRMSTAT) < 0) {
                    return IOReturnCode.FCRMINUpdateError;
                }
            }
        }
        return CommonReturnCode.Normal;
    }

    //更新交易記錄檔
    //''' <summary>
    //''' 更新FEPTXN(Response)
    //''' </summary>
    //''' <modify>
    //''' <modifier>Husan</modifier>
    //''' <Date>2011/04/22</Date>
    //''' <reason>調整FEPTXN_AA_RC傳入的value</reason>
    //''' </modify>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode updateFEPTXN(Integer flow) {
        FEPReturnCode rtnCode = null;
        switch (MessageFlow.fromValue(flow)) {
            case Response:
                getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
                break;
            case Confirmation:
                if (getFiscBusiness().getFeptxn().getFeptxnAaRc() == FEPReturnCode.Normal.getValue()) {
                    getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
                }else {
                    _rtnCode = FEPReturnCode.parse(getFiscBusiness().getFeptxn().getFeptxnAaRc().toString());
                }
                getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCCon().getResponseCode());
                getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm); //FISC CONFIRM
                break;
            default:
                break;
        }
        rtnCode = getFiscBusiness().updateTxData();
        return rtnCode;
    }
}
