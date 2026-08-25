package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.enchelper.RMENCHelper;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMPCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.RMGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

/**
 * 參加單位查詢交易處理狀況交易
 * 本支負責處理電文如下
 */
public class  AA1514 extends RMAABase{

    @Autowired
    RmouttExtMapper rmouttExtMapper;

    private RmoutExtMapper dbRmout = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
    private MsgoutExtMapper dbMsgout = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);
    private AllbankExtMapper dbAllBank = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);
    private static final LogHelper TRACELOGGER = LogHelperFactory.getTraceLogger();

    /**
     * 共用變數宣告
     */
    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    @SuppressWarnings("unused")
    private Rmout _defRMOUT = new Rmout();
    @SuppressWarnings("unused")
    private Msgout _defMSGOUT = new Msgout();

    /**
     * AA的建構式,在這邊初始化及設定其他相關變數
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件)
     * @throws Exception
     */
    public AA1514(FISCData txnData) throws Exception {
        super(txnData);
    }

    /*
     * AA進入點主程式
     */

    /**
     * 程式進入點
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        @SuppressWarnings("unused")
        String repMac = "";
        String reqStat = "";
        String reqRmSno = "";
        String reqReceiverBank = "";
        @SuppressWarnings("unused")
        String repUnitBank = "";
        @SuppressWarnings("unused")
        String wkStatus = "";
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        try {
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                _rtnCode = checkOutData(reqStat,reqRmSno,reqReceiverBank);
            }
            getLogContext().setRemark( "After CheckOutData, rtnCode=" + _rtnCode.toString() + ", reqStat=" +
                    reqStat + ", reqRMSNO=" + reqRmSno + ", reqRECEIVER_BANK=" + reqReceiverBank);
            logMessage(Level.DEBUG, getLogContext());

            //3.組送往 FISC 之 Request 電文並等待財金之 Response
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                _rtnCode = prepareAndSendForFiSc();
            }
            //4.CheckResponseFromFISC:檢核回應電文是否正確
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                _rtnCode = getmFISCBusiness().checkResponseFromFISC();
            }

            if (getmFISCRMRes() != null && getmFISCRMRes().getSTATUS() != null){
                getLogContext().setRemark("After CheckResponseFromFISC, rtnCode=" + _rtnCode.toString() + ", FISCRMRes.ResponseCode=" +
                         getmFISCRMRes().getResponseCode() + ", FISCRMRes.STATUS=" + getmFISCRMRes().getSTATUS());
            }else {
                getLogContext().setRemark("After CheckResponseFromFISC, rtnCode=" + _rtnCode.toString() + ", FISCRMRes.ResponseCode=" +
                        getmFISCRMRes().getResponseCode());
            }
            logMessage(Level.DEBUG, getLogContext());

            /*
             'Modify by Candy 2013-02-22 因財金回應電文是解款行相關資料, 故不能UPDATE匯出相關Table
            ''5.更新電文序號/通匯序號/匯出主檔狀態及送更新多筆匯出狀態訊息至11X1-INQueue
            'If _rtnCode = CommonReturnCode.Normal Then _rtnCode = UpdateByFISCRes(reqSTAT, reqRECEIVER_BANK, reqRMSNO)
            'Trace.WriteLine(String.Format("After UpdateREPNOAndSTATAndSend11X1INQueue rtnCode={0}{1}", _rtnCode.ToString(), CType(_rtnCode, String).PadLeft(4, "0"c)))

            ''6.UpdateTxData: 更新交易記錄(FEPTXN )
            'UpdateFEPTXN()
             */

            if (!_rtnCode.equals(CommonReturnCode.Normal)){
                // 'Modify by Jim, 2010/12/07, 不應更動FISC的回應
                // 'FISCRMRes.ResponseCode = CType(_rtnCode, String).PadLeft(4, "0"c)
                getmFISCRMRes().setSTATUS("");
            }
        }catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getmTxFISCData().getLogContext().setReturnCode(_rtnCode);
            getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxFISCData().getLogContext().setMessage(rtnMessage);
            getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO,getLogContext());
            //'For UI028022
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                getmTxFISCData().getTxObject().setDescription(NormalRC.External_OK);
            }else {
                getmTxFISCData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            }
        }
        return "";
    }

    /**
     * 1.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
     * @return FEPReturnCode
     */
    @SuppressWarnings("unused")
    private FEPReturnCode prepareAndInsertFepTxn(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            //'2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
            rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(), "0");
            //'新增交易記錄(FEPTXN )
            if (rtnCode==CommonReturnCode.Normal){
                rtnCode = getmFISCBusiness().insertFEPTxn();
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 2.CheckOutData: 匯出主檔(RMOUT)或一般通訊匯出主檔(MSGOUT)檢核
     * @param reqStat
     * @param reqRmSno
     * @param reqReceiverBank
     * @return
     */
    private FEPReturnCode checkOutData(String reqStat,String reqRmSno,String reqReceiverBank){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Rmout defRmOut = new Rmout();
        Msgout defMsgout = new Msgout();
        Allbank defAllbank = new Allbank();
        try{
            if (!"".equals(getmFISCRMReq().getOrgPcode().trim())){
                // If "1111,1121,1131,1171,1181,1191".IndexOf(FISCRMReq.ORG_PCODE) > -1 Then
                if ("1111,1121,1131,1171,1181,1191".contains(getmFISCRMReq().getOrgPcode())){
                    defRmOut.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                    defRmOut.setRmoutSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                    defRmOut.setRmoutFiscsno(StringUtils.leftPad(getmFISCRMReq().getFiscNo(), 7, '0'));
                    defRmOut = dbRmout.getRMOUTForCheckOutData(defRmOut);
                    if (defRmOut==null){
                        rtnCode = IOReturnCode.RMOUTNotFound;
                    }else {
                        _defRMOUT = defRmOut;
                        if (StringUtils.isNotEmpty(defRmOut.getRmoutRemtype())) {
                            if (!getmFISCRMReq().getOrgPcode().substring(1, 3).equals(defRmOut.getRmoutRemtype())) {
                                rtnCode = RMReturnCode.TradePcodeAndHostNotMatch;
                            } else {
                                reqStat = defRmOut.getRmoutStat();
                                reqRmSno = defRmOut.getRmoutRmsno();
                                //  'Jim, 2012/1/30, 改成用RMOUT_RECEIVER_BANK[1:3] + "000" =>ALLBANK PKEY查該筆之ALLBAK_UNIT_BANK
                                //  'reqRECEIVER_BANK = Mid(defRMOUT.RMOUT_RECEIVER_BANK, 1, 3)
                                defAllbank.setAllbankBkno(defRmOut.getRmoutReceiverBank().substring(0, 3));
                                defAllbank.setAllbankBrno("000");
                                if (dbAllBank.queryByPrimaryKey(defAllbank).size() > 0) {
                                    reqReceiverBank = defAllbank.getAllbankUnitBank();
                                } else {
                                    reqReceiverBank = defRmOut.getRmoutReceiverBank().substring(0, 3);
                                }
                            }
                        }
                    }
                }else {
                    defMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                    defMsgout.setMsgoutSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                    defMsgout.setMsgoutFiscsno(StringUtils.leftPad(getmFISCRMReq().getFiscNo(), 7, '0'));

                    Msgout msgout = dbMsgout.getMsgOutForCheckOutData(defMsgout);
                    if (msgout==null){
                        rtnCode = IOReturnCode.MSGOUTNotFound;
                    }else {
                        _defMSGOUT = msgout;
                        if (!getmFISCRMReq().getOrgPcode().equals(RMPCode.PCode1411)){
                            rtnCode = RMReturnCode.TradePcodeAndHostNotMatch;
                        }else {
                            reqStat = msgout.getMsgoutStat();
                            // reqRMSNO = defMSGOUT.MSGOUT_RMSNO
                            reqReceiverBank = msgout.getMsgoutReceiverBank().substring(0,3);
                        }
                    }
                }
            }else {
                return CommonReturnCode.Abnormal;
            }
            return rtnCode;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 3.組送往 FISC 之 Request 電文並等待財金之 Response
     * @return rtnCode
     */
    private FEPReturnCode prepareAndSendForFiSc(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        //(1) 準備回財金的相關資料
        rtnCode =prepareFiScReq();
        if (!rtnCode.equals(CommonReturnCode.Normal)){
            getLogContext().setRemark("After PrepareForFISC rtnCode="+_rtnCode.toString());
            sendEMS(getLogContext());
            return rtnCode;
        }
        //送1511 Req電文到財金(SendToFISC) 並等待回復
        rtnCode = getmFISCBusiness().sendRMRequestToFISC();

        return rtnCode;
    }

    private FEPReturnCode prepareFiScReq(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        RefString reqMac = new RefString("");
        RMENCHelper encHelper;
        try {
            //組header()
            getmFISCRMReq().setSystemSupervisoryControlHeader("00");
            getmFISCRMReq().setSystemNetworkIdentifier("00");
            getmFISCRMReq().setAdderssControlField("00");
            getmFISCRMReq().setMessageType("0200");
            getmFISCRMReq().setProcessingCode("1514");
            getmFISCRMReq().setSystemTraceAuditNo(getmFISCBusiness().getStan());
            getmTxFISCData().setStan(getmFISCRMReq().getSystemTraceAuditNo());
            getmFISCRMReq().setTxnDestinationInstituteId(StringUtils.rightPad("950",7,'0'));
            getmFISCRMReq().setTxnSourceInstituteId(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatHbkno(),7,'0'));
            //(轉成民國年)
            getmFISCRMReq().setTxnInitiateDateAndTime((CalendarUtil.adStringToROCString(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)).substring(1,7)+
                    FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN)));
            getmFISCRMReq().setResponseCode(NormalRC.FISC_REQ_RC);
            getmFISCRMReq().setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatHbkno(),8,' '));

            // 組Body 呼叫AA的時候就將FISC_NO/ORG_PCODE賦值 產生MAC
            encHelper = new ENCHelper(getmTxFISCData());
            rtnCode = encHelper.makeRMFISCMAC(reqMac);
            if (!rtnCode.equals(CommonReturnCode.Normal)){
                return rtnCode;
            }
            getmFISCRMReq().setMAC(StringUtils.leftPad(reqMac.get(),8,'0'));

            //MakeBitmap
            rtnCode = getmFISCBusiness().makeBitmap(getmFISCRMReq().getMessageType(),getmFISCRMReq().getProcessingCode(), MessageFlow.Request);
//            Trace.WriteLine(String.Format("after MakeBitmap rtnCode={0}{1}", rtnCode.ToString(), CType(rtnCode, String).PadLeft(4, "0"c)))
            TRACELOGGER.info(
                    StringUtils.join("after MakeBitmap rtnCode={0}{1}", rtnCode.toString(),StringUtils.leftPad(rtnCode.toString(),4,"0")));

            return rtnCode;
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
    }

    /**
     * 5.更新電文序號/通匯序號/匯出主檔狀態及送更新多筆匯出狀態訊息至11X1-INQueue
     */
    /*private FEPReturnCode updateByFISCRes(String reqStat,String reqReceiverBank,String repRmSno){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
//      DBHelper db = new DBHelper(FEPConfig.DB Name);
        '    Dim dbRMFISCOUT4 As New DBRMFISCOUT4(db)
        '    Dim dbRMFISCOUT1 As New DBRMFISCOUT1(db)
        '    Dim dbRMOUTSNO As New DBRMOUTSNO(db)
        '    Dim dbRMOUT As New DBRMOUT(db)
        '    Dim dbRMOUTT As New DBRMOUTT(db)
        '    Dim dbRMBTCH As New DBRMBTCH(db)

        '    Dim dbMSGOUT As New DBMSGOUT(db)

        '    Dim INQueue11X1 As MessageQueue

        '    Dim msg11X1 As New Queue11X1

        '    Try
        '        'FISCRMRes.STATUS=01-財金未收到, 02-財金收但對方行未收到, 03-對方行已收到, 04-對方行已回訊財金
        '        db.BeginTransaction()

        '        'Modify by Jim, 2011/8/11, 改成先更新RMOUT，因為更新FISCOUTSNO和RMOUTSNO的方式改成查詢 RMOUT_FISC_RTN_CODE = 0001 且序號最大的那一筆來更新
        '        '(3) 更新匯款狀態
        '        If FISCRMReq.ORG_PCODE = PCode1411 Then
        '            rtnCode = UpdateMSGOUTByFISCSRes(dbMSGOUT, reqSTAT)

        '            If FISCRMRes.ResponseCode = NormalRC.FISC_OK Then
        '                'Modify by Jim, 2011/10/25, 財金回OK時STATUS才會有值
        '                If FISCRMRes.STATUS = "01" Then
        '                    '(1) 更新電文序號
        '                    rtnCode = UpdateRMFISCOUT4(dbRMFISCOUT4, reqSTAT, dbMSGOUT)
        '                    Trace.WriteLine("UpdateRMFISCOUT4 rtnCode=" & _rtnCode.ToString())
        '                End If
        '            End If
        '        Else
        '            If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then

        '                rtnCode = UpdateRMOUTByFISCRes(_defRMOUT, dbRMOUT, dbRMOUTT, dbRMBTCH, reqSTAT)

        '                'Modify by Jim, 2011/12/20, 還需要判斷是否有更新到RMOUT & RMOUTT (確保此時狀態仍然是傳送中)才能再做ProcessAPTOTAndRMTOTAndRMTOTAL
        '                If rtnCode = CommonReturnCode.Normal Then
        '                    Select Case FISCRMRes.STATUS
        '                        Case "02", "03", "04"
        '                            FepTxn.FEPTXN_TX_AMT = CType(_defRMOUT.RMOUT_TXAMT, Decimal)
        '                            FepTxn.FEPTXN_TBSDY = Now.ToString("yyyyMMdd")
        '                            FepTxn.FEPTXN_SENDER_BANK = _defRMOUT.RMOUT_SENDER_BANK
        '                            FepTxn.FEPTXN_RECEIVER_BANK = _defRMOUT.RMOUT_RECEIVER_BANK
        '                            FepTxn.FEPTXN_TX_FEE_CR = CType(_defRMOUT.RMOUT_POSTAMT, Decimal)
        '                            FepTxn.FEPTXN_PCODE = FISCRMReq.ORG_PCODE
        '                            FISCRMReq.ProcessingCode = FISCRMReq.ORG_PCODE
        '                            'Modify by CANDY  2011-09-27 PCODE =1171 RMOUT_AMT_TYPE IS NULL
        '                            If _defRMOUT.RMOUT_FISC_SND_CODE = "1171" OrElse _defRMOUT.RMOUT_AMT_TYPE Is Nothing Then
        '                                rtnCode = FISCBusiness.ProcessAPTOTAndRMTOTAndRMTOTAL("002", FepTxn)
        '                            Else
        '                                rtnCode = FISCBusiness.ProcessAPTOTAndRMTOTAndRMTOTAL(_defRMOUT.RMOUT_AMT_TYPE, FepTxn)
        '                            End If

        '                            LogContext.Remark = "AA1511, After ProcessAPTOTAndRMTOTAndRMTOTAL, rtnCode=" & rtnCode.ToString & ", FISCRMRes.ResponseCode=" & FISCRMRes.ResponseCode & ", FISCRMReq.ORG_PCODE=" & FISCRMReq.ORG_PCODE & ", reqStat=" & reqSTAT
        '                            LogMessage(LogLevel.Debug, LogContext)
        '                        Case Else
        '                            '不記帳
        '                    End Select
        '                End If

        '                'Jim, 2012/2/21, 改成要判斷RMOUT & RMOUTT是否更新成功才要更新FISCOUTSNO, RMOUTSNO
        '                'Jim, 2012/2/21, 更新序號前先檢查是不是有狀態為傳送中的資料
        '                If rtnCode = CommonReturnCode.Normal Then
        '                    Dim tmpRMOUT As New DefRMOUT
        '                    tmpRMOUT.RMOUT_TXDATE = Now.ToString("yyyyMMdd")
        '                    tmpRMOUT.RMOUT_STAT = RMOUTStatus.Transfered
        '                    Dim tmpDt As DataTable = dbRMOUT.GetRMOUTByDef(tmpRMOUT)
        '                    If tmpDt.Rows.Count > 0 Then
        '                        LogContext.Remark = "查到狀態為傳送中的資料" & tmpDt.Rows.Count & "筆, 不自動更新電文序號和通匯序號"
        '                        LogMessage(LogLevel.Debug, LogContext)
        '                    Else
        '                        'Modify by Jim, 改成不用判斷狀態都要更新FISCOUTSNO, RMOUTSNO
        '                        '(1) 更新電文序號
        '                        rtnCode = UpdateRMFISCOUT1(dbRMFISCOUT1, reqSTAT, dbRMOUTT)

        '                        '(2) 更新通匯序號
        '                        If rtnCode = CommonReturnCode.Normal Then
        '                            rtnCode = UpdateRMOUTSNO(dbRMOUTSNO, reqSTAT, reqRECEIVER_BANK, repRMSNO, dbRMOUTT, db)
        '                        End If
        '                    End If
        '                End If
        '            End If
        '        End If

        '        If rtnCode = CommonReturnCode.Normal Then
        '            db.CommitTransaction()
        '        Else
        '            LogContext.Remark = "AA1511-UpdateREPNOAndSTATAndSend11X1INQueue Rollback, rtnCode=" & rtnCode.ToString
        '            LogMessage(LogLevel.Debug, LogContext)
        '            db.RollbackTransaction()
        '        End If

        '        If FISCRMReq.ORG_PCODE <> PCode1411 Then
        '            INQueue11X1 = New MessageQueue(RMConfig.Instance().INQueue11X1)
        '            'Modify by Jim, 2011/10/25, 改成用FISCRMReq的FISC_NO避免回應電文的欄位是null
        '            msg11X1.MSG_FISCNO = FISCRMReq.FISC_NO
        '            'Modify by Jim, 2011/10/25, 財金回OK時STATUS才會有值
        '            If FISCRMRes.ResponseCode = NormalRC.FISC_OK Then
        '                Select Case FISCRMRes.STATUS
        '                    Case "01"
        '                        If FISCRMReq.ORG_PCODE = PCode1171 Then
        '                            msg11X1.MSG_STAT = RMOUTStatus.BackExchange '退匯
        '                        Else
        '                            msg11X1.MSG_STAT = RMOUTStatus.Passed '(已放行)
                '                        End If
        '                    Case "02", "03", "04"
        '                        msg11X1.MSG_STAT = RMOUTStatus.GrantMoney
        '                    Case Else
        '                        msg11X1.MSG_STAT = ""
        '                End Select
        '            Else
        '                msg11X1.MSG_STAT = RMOUTStatus.Passed '(已放行)
                '            End If

        '            INQueue11X1.Send(msg11X1.MSG_FISCNO.PadLeft(7, "0"c) & msg11X1.MSG_STAT.PadLeft(2, "0"c))
        '            LogContext.Remark = "AA1511--UpdateREPNOAndSTATAndSend11X1INQueue, Send Message to INQueue11X1, queue.FISCSNO=" & msg11X1.MSG_FISCNO & ", queue.STAT=" & msg11X1.MSG_STAT
        '            LogMessage(LogLevel.Debug, LogContext)
        '        End If

        '        If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso FISCRMReq.ORG_PCODE.Substring(0, 2) = "11" AndAlso
        '            reqSTAT = "05" AndAlso (_defRMOUT.RMOUT_ORIGINAL = RMOUT_ORIGINAL.FEDI OrElse _defRMOUT.RMOUT_ORIGINAL = RMOUT_ORIGINAL.MMAB2B) Then
        '            '組AASYNCFEDI  Request 電文/ MSGCTL_MSGID=”SYNCFEDI”
        '            rtnCode = CallSYNCFEDI(_defRMOUT)
        '        End If

        '        Return rtnCode
        '    Catch ex As Exception
        '        If db.Transaction IsNot Nothing Then db.RollbackTransaction()
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    Finally
        '        db.Dispose()
        '        dbRMFISCOUT1.Dispose()
        '        dbRMFISCOUT4.Dispose()
        '        dbRMOUTSNO.Dispose()
        '        dbRMOUT.Dispose()
        '        dbRMOUT.Dispose()
        '        dbMSGOUT.Dispose()
        '    End Try
        'End Function

        ' '''   <modify>
                ' '''     <modifier>Kyo</modifier>
                ' '''     <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
                ' '''     <date>2010/03/16</date>
                ' '''   </modify>
                'Private Function UpdateRMFISCOUT1(ByVal dbRMFISCOUT1 As DBRMFISCOUT1, ByVal reqSTAT As String, ByVal dbRMOUTT As DBRMOUTT) As FEPReturnCode
        '    Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal

        '    Dim defRMFISCOUT1 As New DefRMFISCOUT1

        '    Try
        '        'Modify by Jim, 2011/12/20, 此判斷移到外面
        '        'If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then

        '        defRMFISCOUT1.RMFISCOUT1_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        '        defRMFISCOUT1.RMFISCOUT1_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_FBKNO

        '        If dbRMFISCOUT1.QueryByPrimaryKey(defRMFISCOUT1) < 1 Then
        '            rtnCode = IOReturnCode.RMFISCOUT1NotFound
        '        Else
        '            LogContext.Remark = "AA1511, Before UpdateRMFISCOUT1, RMFISCOUT1_NO=" & defRMFISCOUT1.RMFISCOUT1_NO & ", RMFISCOUT1_REP_NO=" & defRMFISCOUT1.RMFISCOUT1_REP_NO
        '            LogMessage(LogLevel.Debug, LogContext)

        '            If defRMFISCOUT1.RMFISCOUT1_NO <> defRMFISCOUT1.RMFISCOUT1_REP_NO Then
        '                'Modify by Jim, 2011/8/10, 多筆匯出因REP_NO UPDATE不一致, 改直接抓取匯出成功的最大電文序號
        '                Dim defRMOUTT As New Tables.DefRMOUTT
        '                defRMOUTT.RMOUTT_TXDATE = Now.ToString("yyyyMMdd")
        '                defRMOUTT.RMOUTT_FISC_RTN_CODE = NormalRC.FISC_OK
        '                Dim dtRMOUTT As DataTable = dbRMOUTT.GetRMOUTTByDef(defRMOUTT)
        '                If dtRMOUTT.Rows.Count < 1 Then
        '                    'rtnCode = IOReturnCode.RMOUTNotFound
        '                    'LogContext.Remark = "AA1511 更新RMFISCOUT1時查詢RMOUT查無資料"
        '                    'LogContext.ReturnCode = IOReturnCode.RMOUTNotFound
        '                    'SendEMS(LogContext)
        '                    'Return rtnCode
        '                    '如果找不到成功的RMOUT資料，則更新成0
        '                    defRMFISCOUT1.RMFISCOUT1_NO = 0
        '                    defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
        '                Else
        '                    Dim drRMOUT As DataRow() = dtRMOUTT.Select("", "RMOUTT_FISCSNO DESC")
        '                    If drRMOUT.Count > 0 Then
        '                        Dim maxFISCSNO As String = drRMOUT(0).Item("RMOUTT_FISCSNO").ToString
        '                        defRMFISCOUT1.RMFISCOUT1_NO = Decimal.Parse(maxFISCSNO)
        '                        defRMFISCOUT1.RMFISCOUT1_REP_NO = defRMFISCOUT1.RMFISCOUT1_NO
        '                    Else
        '                        '如果找不到成功的RMOUT資料，則更新成0
        '                        defRMFISCOUT1.RMFISCOUT1_NO = 0
        '                        defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
        '                    End If
        '                End If

        '                If dbRMFISCOUT1.UpdateByPrimaryKey(defRMFISCOUT1, False) < 1 Then
        '                    rtnCode = IOReturnCode.RMFISCOUT1UpdateError
        '                    LogContext.Remark = "AA1511, 更新RMFISCOUT1 0 筆"
        '                    LogContext.ReturnCode = rtnCode
        '                    SendEMS(LogContext)
        '                Else
        '                    LogContext.Remark = "AA1511, After UpdateRMFISCOUT1, RMFISCOUT1_NO=RMFISCOUT1_REP_NO=" & defRMFISCOUT1.RMFISCOUT1_NO
        '                    LogMessage(LogLevel.Debug, LogContext)
        '                End If
        '            End If
        '        End If
        '        'End If
        '        Return rtnCode
        '    Catch ex As Exception
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    End Try

        'End Function

        ' '''   <modify>
                ' '''     <modifier>Kyo</modifier>
                ' '''     <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
                ' '''     <date>2010/03/16</date>
                ' '''   </modify>
                'Private Function UpdateRMFISCOUT4(ByVal dbRMFISCOUT4 As DBRMFISCOUT4, ByVal reqSTAT As String, ByVal dbMSGOUT As DBMSGOUT) As FEPReturnCode
        '    Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal

        '    Dim defRMFISCOUT4 As New DefRMFISCOUT4
        '    Try
        '        If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = MSGOUTStatus.Transferring Then
        '            defRMFISCOUT4.RMFISCOUT4_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        '            defRMFISCOUT4.RMFISCOUT4_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_FBKNO

        '            Dim dtMSGOUT As New DataTable
        '            Dim defMSGOUT As New DefMSGOUT
        '            defMSGOUT.MSGOUT_TXDATE = Now.ToString("yyyyMMdd")
        '            dtMSGOUT = dbMSGOUT.GetMSGOUTByDef(defMSGOUT)
        '            Dim drMSGOUT As DataRow() = dtMSGOUT.Select("MSGOUT_FISC_RTN_CODE='0001'", "MSGOUT_FISCSNO DESC")
        '            If drMSGOUT.Count > 0 AndAlso Not IsDBNull(drMSGOUT(0)("MSGOUT_FISCSNO")) Then
        '                defRMFISCOUT4.RMFISCOUT4_NO = Decimal.Parse(drMSGOUT(0)("MSGOUT_FISCSNO").ToString)
        '            Else
        '                defRMFISCOUT4.RMFISCOUT4_NO = 0
        '            End If

        '            If dbRMFISCOUT4.UpdateByPrimaryKey(defRMFISCOUT4) < 1 Then
        '                LogContext.Remark = "AA1511 更新RMFISCOUT4 0 筆"
        '                LogContext.ReturnCode = IOReturnCode.RMFISCOUT4UpdateError
        '                SendEMS(LogContext)
        '                rtnCode = IOReturnCode.RMFISCOUT4UpdateError
        '            End If
        '        End If
        '        Return rtnCode
        '    Catch ex As Exception
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    End Try

        'End Function

        ' '''   <modify>
                ' '''     <modifier>Kyo</modifier>
                ' '''     <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
                ' '''     <date>2010/03/16</date>
                ' '''   </modify>
                'Private Function UpdateRMOUTSNO(ByVal dbRMOUTSNO As DBRMOUTSNO, ByVal reqSTAT As String, ByVal reqRECEIVER_BANK As String, ByVal repRMSNO As String, ByVal dbRMOUTT As DBRMOUTT, ByVal db As DBHelper) As FEPReturnCode
        '    Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal

        '    Dim defRMOUTSNO As New DefRMOUTSNO

        '    Try
        '        'Modify by Jim, 2011/12/20, 此判斷移到外面
        '        'If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then
        '        defRMOUTSNO.RMOUTSNO_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        '        defRMOUTSNO.RMOUTSNO_RECEIVER_BANK = reqRECEIVER_BANK
        '        If dbRMOUTSNO.QueryByPrimaryKey(defRMOUTSNO) < 1 Then
        '            LogContext.ReturnCode = IOReturnCode.RMOUTSNONotFound
        '            LogContext.Remark = "AA1511-UpdateRMOUTSNO, RMOUTSNO not found"
        '            TxHelper.GetRCFromErrorCode(LogContext.ReturnCode, FEPChannel.BRANCH, LogContext)
        '            Return IOReturnCode.RMOUTSNONotFound
        '        Else
        '            LogContext.Remark = "AA1511, Before UpdateRMOUTSNO, RMOUTSNO_NO=" & defRMOUTSNO.RMOUTSNO_NO & ", RMOUTSNO_REP_NO=" & defRMOUTSNO.RMOUTSNO_REP_NO
        '            LogMessage(LogLevel.Debug, LogContext)

        '            Dim updateNO As Integer = 0
        '            Dim defRMOUTT As New Tables.DefRMOUTT
        '            'Dim dtRMOUT As New DataTable
        '            'Dim drRMOUT() As DataRow

        '            'Jim, 2012/1/18, 修改更新邏輯，要考慮共用中心的case
        '            defRMOUTT.RMOUTT_TXDATE = Now.ToString("yyyyMMdd")
        '            defRMOUTT.RMOUTT_FISC_RTN_CODE = NormalRC.FISC_OK
        '            'updateNO = dbRMOUTT.GetRMSNOForUpdateRMOUTSNO(defRMOUTT, reqRECEIVER_BANK)
        '            updateNO = GetRMSNOForUpdateRMOUTSNO(defRMOUTT, reqRECEIVER_BANK, db)
        '            If updateNO >= 0 Then
        '                defRMOUTSNO.RMOUTSNO_NO = updateNO
        '                defRMOUTSNO.RMOUTSNO_REP_NO = updateNO
        '            Else
        '                Return CommonReturnCode.Normal
        '            End If
        '            'dtRMOUT = dbRMOUT.GetRMOUTByDef(defRMOUT)
        '            'drRMOUT = dtRMOUT.Select("RMOUT_FISC_RTN_CODE='0001' AND SUBSTRING(RMOUT_RECEIVER_BANK,1,3)='" & reqRECEIVER_BANK & "'", "RMOUT_RMSNO DESC")
        '            'If drRMOUT.Count = 0 Then
        '            '    defRMOUTSNO.RMOUTSNO_NO = 0
        '            '    defRMOUTSNO.RMOUTSNO_REP_NO = 0
        '            'Else
        '            '    Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_NO)
        '            '    Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_REP_NO)
        '            'End If

        '            If dbRMOUTSNO.UpdateByPrimaryKey(defRMOUTSNO) < 1 Then
        '                rtnCode = IOReturnCode.UpdateFail
        '                LogContext.Remark = "AA1511, 更新RMOUTSNO 0 筆"
        '                LogContext.ReturnCode = rtnCode
        '                TxHelper.GetRCFromErrorCode(rtnCode, FEPChannel.BRANCH, LogContext)
        '                Return rtnCode
        '            Else
        '                LogContext.Remark = "AA1511, After UpdateRMOUTSNO, RMOUTSNO_NO=RMOUTSNO_REP_NO=" & defRMOUTSNO.RMOUTSNO_NO
        '                LogMessage(LogLevel.Debug, LogContext)
        '            End If
        '        End If

        '        'End If

        '        Return rtnCode
        '    Catch ex As Exception
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    Finally

        '    End Try

        'End Function

        'Private Function UpdateRMOUTByFISCRes(ByVal defRMOUT As DefRMOUT, ByVal dbRMOUT As DBRMOUT, ByVal dbRMOUTT As DBRMOUTT, ByVal dbRMBTCH As DBRMBTCH, ByVal reqSTAT As String) As FEPReturnCode
        '    Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal

        '    Dim defRMOUTT As New DefRMOUTT

        '    Try
        '        'Modify by Jim, 2011/12/20, 此判斷移到外面
        '        'If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then

        '        defRMOUT.RMOUT_FISCSNO = FISCRMRes.FISC_NO
        '        defRMOUT.RMOUT_TXDATE = Now.ToString("yyyyMMdd")
        '        '1514新增的財金回應電文, 可做為更新時的條件
        '        defRMOUT.RMOUT_RECEIVER_BANK = FISCRMRes.RECEIVER_BANK
        '        defRMOUT.RMOUT_STAN = FISCRMRes.ORG_STAN
        '        defRMOUT.RMOUT_RMSNO = FISCRMRes.BANK_NO

        '        Select Case FISCRMRes.STATUS
        '            Case "01"
        '                'Modify by Jim, 2011/05/16, SPEC修改
        '                'If FISCRMReq.ORG_PCODE = "1171" Then
        '                '    defRMOUT.RMOUT_STAT = RMOUTStatus.BackExchange '退匯
        '                'Else
        '                defRMOUT.RMOUT_STAT = RMOUTStatus.Passed '(已放行)
                '                'End If
        '            Case "02", "03", "04"

        '                'Modify by Jim, 2011/9/15, RMBTCH也需要同時更新
        '                If dbRMOUT.GetSingleRMOUT(defRMOUT) > 0 Then
        '                    If defRMOUT.RMOUT_ORIGINAL = RMOUT_ORIGINAL.FCS Then
        '                        Dim defRMBTCH As New DefRMBTCH
        '                        defRMBTCH.RMBTCH_SENDER_BANK = defRMOUT.RMOUT_SENDER_BANK
        '                        defRMBTCH.RMBTCH_REMDATE = defRMOUT.RMOUT_TXDATE
        '                        defRMBTCH.RMBTCH_TIMES = defRMOUT.RMOUT_BATCHNO
        '                        defRMBTCH.RMBTCH_FEPNO = defRMOUT.RMOUT_FEPNO
        '                        '以下為要更新的欄位
        '                        defRMBTCH.RMBTCH_FEP_RC = NormalRC.RMBTCH_FEPRC_OK
        '                        defRMBTCH.RMBTCH_ERRMSG = "匯出成功"
        '                        If dbRMBTCH.UpdateByPrimaryKey(defRMBTCH) <> 1 Then
        '                            rtnCode = IOReturnCode.RMBTCHUpdateError
        '                            Return rtnCode
        '                        End If
        '                    End If
        '                Else
        '                    rtnCode = IOReturnCode.RMOUTNotFound
        '                    Return rtnCode
        '                End If

        '                defRMOUT.RMOUT_STAT = RMOUTStatus.GrantMoney
        '                defRMOUT.RMOUT_FISC_RTN_CODE = NormalRC.FISC_OK
        '                defRMOUTT.RMOUTT_FISC_RTN_CODE = NormalRC.FISC_OK
        '            Case Else

        '        End Select

        '        'where RMOUT_FISCSNO = @RMOUT_FISCSNO AND RMOUT_TXDATE = @RMOUT_TXDATE AND RMOUT_STAT = '05'
        '        If dbRMOUT.UpdateRMOUTBy1514(defRMOUT) <> 1 Then
        '            rtnCode = IOReturnCode.RMOUTUpdateError
        '            Return rtnCode
        '        End If

        '        defRMOUTT.RMOUTT_STAT = defRMOUT.RMOUT_STAT
        '        defRMOUTT.RMOUTT_FISCSNO = defRMOUT.RMOUT_FISCSNO
        '        defRMOUTT.RMOUTT_TXDATE = defRMOUT.RMOUT_TXDATE
        '        '1514新增的財金回應電文, 可做為更新時的條件
        '        defRMOUTT.RMOUTT_RECEIVER_BANK = FISCRMRes.RECEIVER_BANK
        '        defRMOUTT.RMOUTT_STAN = FISCRMRes.ORG_STAN
        '        defRMOUTT.RMOUTT_RMSNO = FISCRMRes.ORG_BANK_NO

        '        'where RMOUTT_FISCSNO = @RMOUTT_FISCSNO AND RMOUTT_TXDATE = @RMOUTT_TXDATE AND RMOUTT_STAT = '05'
        '        If dbRMOUTT.UpdateRMOUTTBy1514(defRMOUTT) <> 1 Then
        '            rtnCode = IOReturnCode.RMOUTUpdateError
        '        End If

        '        'End If

        '        Return rtnCode
        '    Catch ex As Exception
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    Finally
        '        LogContext.Remark = "After Update RMOUT and RMOUTT ByFISCNO, rtnCode=" & rtnCode.ToString & ", FISCRMRes.FISC_NO=" & FISCRMRes.FISC_NO & ", FISCRMRes.STATUS=" & FISCRMRes.STATUS
        '        LogMessage(LogLevel.Debug, LogContext)
        '    End Try

        'End Function

        'Private Function UpdateMSGOUTByFISCSRes(ByVal dbMSGOUT As DBMSGOUT, ByVal reqSTAT As String) As FEPReturnCode
        '    Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal

        '    Dim defMSGOUT As New DefMSGOUT

        '    Try
        '        If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = MSGOUTStatus.Transferring Then

        '            Select Case FISCRMRes.STATUS
        '                Case "01"
        '                    defMSGOUT.MSGOUT_STAT = MSGOUTStatus.Send '02(已發訊)
                '                Case "02", "03", "04"
        '                    defMSGOUT.MSGOUT_STAT = MSGOUTStatus.Transferred
        '                    defMSGOUT.MSGOUT_FISC_RTN_CODE = NormalRC.FISC_OK
        '            End Select


        '            defMSGOUT.MSGOUT_FISCSNO = FISCRMRes.FISC_NO
        '            defMSGOUT.MSGOUT_TXDATE = Now.ToString("yyyyMMdd")
        '            '1514新增的財金回應電文, 可做為更新時的條件
        '            defMSGOUT.MSGOUT_RECEIVER_BANK = FISCRMRes.RECEIVER_BANK
        '            defMSGOUT.MSGOUT_STAN = FISCRMRes.ORG_STAN

        '            If dbMSGOUT.UpdateMSGOUTBy1514(defMSGOUT) <> 1 Then
        '                rtnCode = IOReturnCode.MSGOUTUpdateError
        '                LogContext.ReturnCode = rtnCode
        '                SendEMS(LogContext)
        '            End If
        '        End If

        '        Return rtnCode
        '    Catch ex As Exception
        '        LogContext.ProgramException = ex
        '        SendEMS(LogContext)
        '        Return CommonReturnCode.ProgramException
        '    Finally

        '    End Try

        'End Function
    }
     */

    /**
     * 6.更新FEPTXN
     * @return FEPReturnCode
     */
    @SuppressWarnings("unused")
    private FEPReturnCode updateFepTxn(){
        FEPReturnCode rtnCode;
        if (getFeptxn().getFeptxnRepRc().equals(NormalRC.FISC_OK)){
            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
        }
        //'FISC Response
        getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
//        'FepTxn.FEPTXN_REP_TIME = Now
//
//        'If _rtnCode <> CommonReturnCode.Normal Then
//        '    FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
//        '    FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString().PadLeft(4, "0"c)
//        'End If
        rtnCode = getmFISCBusiness().updateFepTxnForRM(getFeptxn());
        if (!rtnCode.equals(CommonReturnCode.Normal)){
            TxHelper.getRCFromErrorCode(rtnCode.toString(),FEPChannel.FEP,getmTxFISCData().getTxChannel(),getmTxFISCData().getLogContext());
            return rtnCode;
        }
        return rtnCode;
    }

    /**
     * (8).FEDI轉通匯匯出狀態回饋
     * @param mDefRmOut mDefRmOut
     * @return FEPReturnCode
     */
    @SuppressWarnings("unused")
    private FEPReturnCode callSyncFeDi(Rmout mDefRmOut){
        RMGeneral rmGeneral = new RMGeneral();
        RMData txData = new RMData();
        List<Msgctl> msgCtlTable = FEPCache.getMsgctlList();
        RMAABase aa = null;
        try {
            rmGeneral.getRequest().setKINBR(mDefRmOut.getRmoutBrno());
            rmGeneral.getRequest().setTRMSEQ("99");
            rmGeneral.getRequest().setBRSNO(mDefRmOut.getRmoutBrsno());
            rmGeneral.getRequest().setORGCHLEJNO(mDefRmOut.getRmoutBrsno());
            rmGeneral.getRequest().setENTTLRNO("99");
            rmGeneral.getRequest().setSUPNO1("");
            rmGeneral.getRequest().setSUPNO2("");
            rmGeneral.getRequest().setTBSDY(mDefRmOut.getRmoutTxdate());
            rmGeneral.getRequest().setTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            rmGeneral.getRequest().setFEPNO(mDefRmOut.getRmoutFepno());
            rmGeneral.getRequest().setREMDATE(mDefRmOut.getRmoutTxdate());
            rmGeneral.getRequest().setFISCRC(mDefRmOut.getRmoutFiscRtnCode());
            rmGeneral.getRequest().setCHLRC(TxHelper.getRCFromErrorCode(mDefRmOut.getRmoutFiscRtnCode(),FEPChannel.FEP,FEPChannel.BRANCH,getmTxFISCData().getLogContext()));
            rmGeneral.getRequest().setCHLMSG(TxHelper.getMessageFromFEPReturnCode(mDefRmOut.getRmoutFiscRtnCode(),FEPChannel.FEP,getmTxFISCData().getLogContext()));
            rmGeneral.getRequest().setSTATUS(mDefRmOut.getRmoutStat());
            rmGeneral.getRequest().setORGORIGINAL(mDefRmOut.getRmoutOriginal());

            txData.setEj(TxHelper.generateEj());
            txData.setTxObject(rmGeneral);
            txData.setTxChannel(getmTxFISCData().getTxChannel());
            txData.setTxSubSystem(SubSystem.RM);
            txData.setTxRequestMessage(serializeToXml(rmGeneral.getRequest()).replace("&lt;", "<").replace("&gt;", ">"));
            txData.setMessageID("SYNCFEDI");
//            txData.setMsgCtl(new Msgctl());   AA11x2  line 671

            txData.setLogContext(new LogData());
            txData.getLogContext().setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            txData.getLogContext().setSubSys(getLogContext().getSubSys());
            txData.getLogContext().setChannel(getLogContext().getChannel());
            txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
            txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            txData.getLogContext().setEj(txData.getEj());
            txData.getLogContext().setMessage(txData.getTxRequestMessage());

            aa = new SyncFEDI(txData);

            aa.processRequestData();
            if (!txData.getTxObject().getResponse().getRsStatRsStateCode().equals(NormalRC.External_OK)){
                getLogContext().setMessageFlowType(MessageFlow.Response);
                getLogContext().setRemark("AA1511-FEDI轉通匯匯出狀態回饋失敗, RMOUT_PKEY=[ TXDATE="+
                        mDefRmOut.getRmoutTxdate()+ ", BRNO="+mDefRmOut.getRmoutBrno() +"ORIGINAL="+
                        mDefRmOut.getRmoutOriginal()+", BATCHNO="+mDefRmOut.getRmoutBatchno()+" ]");
                sendEMS(getLogContext());
                return CommonReturnCode.Abnormal;
            }
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    @SuppressWarnings("unused")
    private Integer getRMSNOForUpdateRMOUTSNO(Rmout defRmOut, String bkNo){
        try {
            Rmoutt rmoutts = rmouttExtMapper.getRMSNOForUpdateRMOUTSNO(defRmOut.getRmoutTxdate(),defRmOut.getRmoutFiscRtnCode(),bkNo);
            if (rmoutts!= null){
                return Integer.parseInt(rmoutts.getRmouttRmsno());
            }else {
                return 0;
            }
        }catch (Exception ex) {
            //發生錯誤
            return -1;
        }
    }
}
