package com.syscom.fep.server.aa.inbk;

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
import com.syscom.fep.mybatis.ext.mapper.FcrmstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class FISCCheckoutCall extends INBKAABase {
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private FEPReturnCode _rtnCodeReq = CommonReturnCode.Normal; //記錄財金發動Request電文檢核錯誤之fep rtncode(檢查錯誤仍要送Response電文給財金
    private FcrmstatExtMapper dbFCRMSTAT = SpringBeanFactoryUtil.getBean(FcrmstatExtMapper.class);
    private Fcrmstat defFCRMSTAT = new Fcrmstat();

    public FISCCheckoutCall(FISCData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() throws Exception {
        try {
            //1.拆解並檢核財金發動的Request電文Header
            _rtnCode = processRequestHeader();

            //2.準備交易記錄檔＆新增交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareAndInsertFEPTXN();
            }

            //3.檢核財金發動的Request電文BODY
            if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
                _rtnCodeReq = processRequestBody();
            }

            //4.DoBusiness 依財金Request電文傳入之APID更新SYSSTAT3.
            if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
                _rtnCode = doBusiness();
            }

            //5.組送財金Response電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareForFISC();
            }

            //6.送給財金
            if (_rtnCodeReq != CommonReturnCode.Normal) {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCodeReq.getValue());
            } else {
                getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
            }

            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendResponseToFISCOpc();
            }

            //'7.更新交易記錄檔
            //_rtnCode = UpdateFEPTXN()
            //add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
            getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REP_RC=", getFeptxn().getFeptxnRepRc(), ";"));
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());
            if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
                FEPReturnCode InfoRC = null;
                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
                getLogContext().setFiscRC(NormalRC.FISC_OK);
                getLogContext().setMessageGroup("1"); //OPC
                if ("3210".equals(getFeptxn().getFeptxnPcode())) {
                    InfoRC = FISCReturnCode.FISCCheckOut;
                } else {
                    InfoRC = FISCReturnCode.FISCPreCheckOut;
                }
                switch (getFiscOPCReq().getAPID()) {
                    case "1000":
                        getLogContext().setMessageParm13(StringUtils.join("通匯各類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "1100":
                        getLogContext().setMessageParm13(StringUtils.join("匯款類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "1200":
                        getLogContext().setMessageParm13(StringUtils.join("代收款項類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "1300":
                        getLogContext().setMessageParm13(StringUtils.join("代繳代發類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "1400":
                        getLogContext().setMessageParm13(StringUtils.join("一般通信類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "1600":
                        getLogContext().setMessageParm13(StringUtils.join("外幣通匯各類子系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2000":
                        getLogContext().setMessageParm13(StringUtils.join("CD/ATM 共用系統提款作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2200":
                        getLogContext().setMessageParm13(StringUtils.join("CD/ATM 共用系統轉帳作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2500":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡共用系統", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2510":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡提款作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2520":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡轉帳作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2530":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡繳款作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2540":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡消費扣款作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2550":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡預先授權作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "2560":
                        getLogContext().setMessageParm13(StringUtils.join("晶片卡全國繳費作業", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                    case "7100":
                        getLogContext().setMessageParm13(StringUtils.join("轉帳退款類交易", "(", getFiscOPCReq().getAPID(), ")"));
                        break;
                }
                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(InfoRC, getLogContext()));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG, getLogContext());
            }

            //2015/11/19 Modify by Ruling for 人工執行批次改為收到財金CheckOut電文(PCODE=3210)時，執行匯款批次GLDaily
            //7.收到財金CheckOut電文(PCODE=3210)時，執行匯款批次GLDaily
            if ("3210".equals(getFeptxn().getFeptxnPcode())) {
                _rtnCode = startBatchJob("RM_GLDaily");
            }

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        } finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }

        return "";

    }

    /**
     * 拆解並檢核財金發動的Request電文Header
     *
     * @return
     */
    private FEPReturnCode processRequestHeader() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //拆解並檢核財金發動的Request電文Header
        rtnCode = getFiscBusiness().checkHeader(getFiscOPCReq(), false);

        //判斷是否為Garbled Message
        if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.SenderIdError || rtnCode == FISCReturnCode.CheckBitMapError) {
            getFiscBusiness().sendGarbledMessage(getFiscOPCReq().getEj(), rtnCode, getFiscOPCReq());
            return rtnCode;
        }

        //除了以上錯誤外其它錯誤仍要組回應電文給財金
        if (rtnCode != CommonReturnCode.Normal) {
            _rtnCodeReq = rtnCode;
            rtnCode = CommonReturnCode.Normal;
        }

        return rtnCode;

    }

    /**
     * 拆解並檢核財金發動的Request電文Header
     *
     * @return
     */
    private FEPReturnCode prepareAndInsertFEPTXN() {
        FEPReturnCode rtnCode = null;

        try {
            rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            } else {
                getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getAPID());
                getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
                rtnCode = getFiscBusiness().insertFEPTxn();
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                } else {
                    return _rtnCode;
                }
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareAndInsertFEPTXN");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 檢核財金發動的Request電文BODY
     *
     * @return
     */
    private FEPReturnCode processRequestBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
//            if (StringUtils.isNotBlank(getFiscOPCReq().getCUR())) {
//                defFCRMSTAT.setFcrmstatCurrency(getFiscOPCReq().getCUR());
//                defFCRMSTAT = dbFCRMSTAT.selectByPrimaryKey(defFCRMSTAT.getFcrmstatCurrency());
//                if (defFCRMSTAT == null) {
//                    return CommonReturnCode.ProgramException;
//                }
//            }

            //檢核財金電文APID
            if ("2000".equals(getFiscOPCReq().getAPID())) {//共用系統提款作業
                rtnCode = FISCReturnCode.INBKStatusUpdateAlready;
                return rtnCode;
            } else {
                //通匯或外幣通匯
                if (!"1000".equals(getFiscOPCReq().getAPID()) && !"1600".equals(getFiscOPCReq().getAPID())) {
                    rtnCode = FISCReturnCode.APIDError; //FISC: 3002 交易訊息內之代碼(ID)欄錯誤
                    return rtnCode;
                }
            }

            switch (getFiscOPCReq().getProcessingCode()) {
                case "3210":
                    switch (getFiscOPCReq().getAPID()) {
                        case "1000": //通匯
                            if ("7".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                                rtnCode = FISCReturnCode.INBKStatusUpdateAlready;
                                return rtnCode;
                            }
                            if (!"4".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && !"D".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                                rtnCode = FISCReturnCode.CheckOutTimingError;
                                return rtnCode;
                            }
                            break;
                        case "1600": //外幣通匯
                            if ("7".equals(defFCRMSTAT.getFcrmstatAoctrm())) {
                                rtnCode = FISCReturnCode.INBKStatusUpdateAlready;
                                return rtnCode;
                            }
                            if (!"4".equals(defFCRMSTAT.getFcrmstatAoctrm()) && !"D".equals(defFCRMSTAT.getFcrmstatAoctrm())) {
                                rtnCode = FISCReturnCode.CheckOutTimingError;
                                return rtnCode;
                            }
                            break;
                    }
                    break;
                case "3211":
                    switch (getFiscOPCReq().getAPID()) {
                        case "1000": //通匯
                            if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && !"2".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && !"A".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && !"B".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                                rtnCode = FISCReturnCode.PrecheckOutNotAllowed;
                                return rtnCode;
                            }
                            break;
                        case "1600": //外幣通匯
                            if (!"1".equals(defFCRMSTAT.getFcrmstatAoctrm()) && !"2".equals(defFCRMSTAT.getFcrmstatAoctrm()) && !"A".equals(defFCRMSTAT.getFcrmstatAoctrm()) && !"B".equals(defFCRMSTAT.getFcrmstatAoctrm())) {
                                rtnCode = FISCReturnCode.PrecheckOutNotAllowed;
                                return rtnCode;
                            }
                            break;
                    }
                    break;
            }

            //比較MAC
            rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), getFiscOPCReq().getMAC());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestBody");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * DoBusiness
     *
     * @return
     */
    private FEPReturnCode doBusiness() {
        SysstatExtMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
        //modify 新增DBSYSSTAT的參數  20110412 by Husan

        try {
            switch (getFiscOPCReq().getProcessingCode()) {
                case "3210":
                    switch (getFiscOPCReq().getAPID()) {
                        case "1000": //通匯
                            SysStatus.getPropertyValue().setSysstatAoct1000("7");
                            SysStatus.getPropertyValue().setSysstatAoct1100("7");
                            SysStatus.getPropertyValue().setSysstatAoct1200("7");
                            SysStatus.getPropertyValue().setSysstatAoct1300("7");
                            SysStatus.getPropertyValue().setSysstatAoct1400("7");
                            if (dbSYSSTAT.updateByPrimaryKeySelective(SysStatus.getPropertyValue()) <= 0) {
                                return FEPReturnCode.SYSSTATUpdateError;
                            }
                            break;
//                        case "1600": //外幣通匯
//                            if (dbFCRMSTAT.updateAOCTRMByCUR(getFiscOPCReq().getCUR()) <= 0) {
//                                return FEPReturnCode.FCRMSTATUpdateError;
//                            }
//                            break;
                    }
                    break;
                case "3211":
                    switch (getFiscOPCReq().getAPID()) {
                        case "1000": //通匯
                            if ("1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) || "A".equals(SysStatus.getPropertyValue().getSysstatAoct1000())) {
                                SysStatus.getPropertyValue().setSysstatAoct1000("4");
                                SysStatus.getPropertyValue().setSysstatAoct1100("4");
                                SysStatus.getPropertyValue().setSysstatAoct1200("4");
                                SysStatus.getPropertyValue().setSysstatAoct1300("4");
                                SysStatus.getPropertyValue().setSysstatAoct1400("4");
                            } else {
                                SysStatus.getPropertyValue().setSysstatAoct1000("D");
                                SysStatus.getPropertyValue().setSysstatAoct1100("D");
                                SysStatus.getPropertyValue().setSysstatAoct1200("D");
                                SysStatus.getPropertyValue().setSysstatAoct1300("D");
                                SysStatus.getPropertyValue().setSysstatAoct1400("D");
                            }
                            if (dbSYSSTAT.updateByPrimaryKeySelective(SysStatus.getPropertyValue()) <= 0) {
                                return FEPReturnCode.SYSSTATUpdateError;
                            } else {
                                //jim, 2012/8/21, 同步EAINET
                                FEPReturnCode rtnCode = getFiscBusiness().updateEAINETForCheckInOut("N");
                                getLogContext().setRemark(StringUtils.join("FISCCheckoutCall, ProcessingCode=", getFiscOPCReq().getProcessingCode(), ", 同步更新EAINET.RM_MODE = N, rtnCode=", rtnCode.toString()));
                                logMessage(Level.DEBUG, getLogContext());
                            }
                            break;
//                        case "1600": //外幣通匯
//                            if ("1".equals(defFCRMSTAT.getFcrmstatAoctrm()) || "A".equals(defFCRMSTAT.getFcrmstatAoctrm())) {
//                                if (dbFCRMSTAT.updateAOCTRM1AndRM4ByCUR(getFiscOPCReq().getCUR(), "4") <= 0) {
//                                    return FEPReturnCode.FCRMSTATUpdateError;
//                                }
//                            } else {
//                                if (dbFCRMSTAT.updateAOCTRM1AndRM4ByCUR(getFiscOPCReq().getCUR(), "D") <= 0) {
//                                    return FEPReturnCode.FCRMSTATUpdateError;
//                                }
//                            }
//                            break;
                    }
                    break;
            }

            return CommonReturnCode.Normal;

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".doBusiness");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     * 組送財金Response電文
     *
     * @return <modify>
     * <modifier>HusanYin</modifier>
     * <reason>修正Const RC</reason>
     * <date>2010/11/25</date>
     * </modify>
     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

        if (_rtnCodeReq != CommonReturnCode.Normal) {
            getLogContext().setProgramName(ProgramName);
            getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCodeReq, FEPChannel.FISC, getLogContext()));
        } else {
            getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_OK);
        }

        //電文Header
        rtnCode = getFiscBusiness().prepareHeader("0610");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //電文Body
        rtnCode = prepareBody();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //產生財金Flatfile電文
        rtnCode = getFiscOPCRes().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        return rtnCode;

    }

    /**
     * 準備財的APData
     *
     * @return
     */
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        getFiscOPCRes().setAPID(getFiscOPCReq().getAPID());
//        if ("1600".equals(getFiscOPCReq().getAPID())) {
//            getFiscOPCRes().setCUR(getFiscOPCReq().getCUR());
//        }
        RefString refMac = new RefString(getFiscOPCRes().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), refMac);
        getFiscOPCRes().setMAC(refMac.get());
        if (rtnCode != CommonReturnCode.Normal) {
            getFiscOPCRes().setMAC("00000000");
            _rtnCodeReq = rtnCode;
        }

        //產生Bitmap
        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCRes().getMessageType(), getFiscOPCRes().getProcessingCode(), MessageFlow.Response);
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        return rtnCode;

    }

    //    ''' <summary>
    //    ''' 更新交易記錄檔
    //    ''' </summary>
    //    ''' <returns></returns>
    //    ''' <remarks></remarks>
    //    Private Function UpdateFEPTXN() As FEPReturnCode
    //        Dim rtnCode As FEPReturnCode

    //        fiscBusiness.FepTxn.FEPTXN_AA_RC = _rtnCode

    //        rtnCode = fiscBusiness.UpdateTxData()
    //        If rtnCode <> CommonReturnCode.Normal Then
    //            Return rtnCode
    //        Else
    //            Return _rtnCode
    //        End If

    //    End Function

    /**
     * 執行匯款批次GLDaily
     *
     * @return FEPReturnCode
     *
     * <history>
     * <modify>
     * <modifier>Ruling</modifier>
     * <reason>人工執行批次改為收到財金CheckOut電文(PCODE=3210)時，執行匯款批次GLDaily</reason>
     * <date>2015/11/19</date>
     * </modify>
     * </history>
     * @throws Exception
     */
    private FEPReturnCode startBatchJob(String batchName) throws Exception {
        // 2022-07-19 Richard modified
        // Candy: AA FISCCheckoutCall裡有call 批次RM_GLDaily這部分先點掉, 因合庫沒有匯款, 而上海商銀應不會有這支批次, 再麻煩修改,謝謝!!
        return CommonReturnCode.Normal;

//        BatchJobLibrary batchLib = null;
//        BatchExtMapper dbBatch = null;
//        @SuppressWarnings("unused")
//		String par = "";
//        try {
//            batchLib = new BatchJobLibrary();
//            dbBatch = SpringBeanFactoryUtil.getBean(BatchExtMapper.class);
//            List<Batch> batches = dbBatch.queryBatchByName(batchName);
//            if (batches.size() > 0) {
//                batchLib.startBatch(batches.get(0).getBatchBatchid().toString(), batches.get(0).getBatchStartjobid().toString(),"");
//                getLogContext().setRemark(StringUtils.join("啟動批次[",batches.get(0).getBatchBatchid(),"-",batchName,"]成功!"));
//                getLogContext().setProgramName(ProgramName + ".startBatchJob");
//                getLogContext().setMessage(StringUtils.join("BATCH_BATCHID=[",batches.get(0).getBatchBatchid(), "], BATCH_STARTJOBID=[", batches.get(0).getBatchStartjobid(),"]!"));
//                logMessage(Level.INFO, getLogContext());
//            } else {
//                getLogContext().setRemark(StringUtils.join("查詢不到批次",batchName,"定義, 批次啟動失敗!"));
//                getLogContext().setReturnCode(IOReturnCode.QueryNoData);
//                getLogContext().setProgramName(ProgramName + ".startBatchJob");
//                logMessage(Level.ERROR, getLogContext());
//                return IOReturnCode.QueryNoData;
//            }
//        } catch (Exception ex) {
//            if (batchLib != null) {
//                batchLib.writeLog(ex.toString());
//                batchLib.abortTask();
//            }
//            getLogContext().setProgramName(ProgramName + ".startBatchJob");
//            throw ex;
//        }
//        return CommonReturnCode.Normal;
    }


}
