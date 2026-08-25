package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.mapper.ClrtotalMapper;
import com.syscom.fep.mybatis.model.Clrtotal;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

public class AA5201 extends INBKAABase {
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private Clrtotal CLRTOTAL = new Clrtotal();
    private ClrtotalMapper dbCLRTOTAL = SpringBeanFactoryUtil.getBean(ClrtotalMapper.class);

    /**
     AA的建構式,在這邊初始化及設定其他相關變數

     @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件

     初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
     ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
     FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)

     */
    public AA5201(FISCData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        boolean needUpdateFEPTXN = false;
        try {
            //準備交易記錄檔
            _rtnCode = getFiscBusiness().prepareFeptxnForUICommon("5201");

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

            needUpdateFEPTXN = true;

            //處理財金Response電文
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processResponse();
                //modified by Maxine on 2011/10/12 for 不管是否有財金回應都應該更新FEPTXN
                //needUpdateFEPTXN = True
            }

            //更新交易記錄檔
            if (needUpdateFEPTXN) {
                _rtnCode = updateFEPTXN();
            }

            //準備跨行交易日結清算總計檔(CLRTOTAL)
            if (_rtnCode == CommonReturnCode.Normal) {
                RefBase<Clrtotal> clrtotalRefBase = new RefBase<>(CLRTOTAL);
                _rtnCode = getFiscBusiness().prepareClrtotal(clrtotalRefBase, MessageFlow.Response);
                CLRTOTAL = clrtotalRefBase.get();
            }

            //新增跨行交易日結清算總計檔(CLRTOTAL)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = insertCLRTOTAL();
            }

            //add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
            getLogContext().setRemark(StringUtils.join("FepTxn.FEPTXN_REP_RC=" , getFeptxn().getFeptxnRepRc() , ";"));
            getLogContext().setProgramName(ProgramName);
            logMessage(Level.DEBUG, getLogContext());
            if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
                getLogContext().setFiscRC(NormalRC.FISC_OK);
                getLogContext().setMessageGroup("2"); //CLR
                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankCloseBalanceInquery, getLogContext()));

                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG, getLogContext());
            }
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
                getTxData().getTxObject().setDescription(StringUtils.join(getFiscCLRRes().getResponseCode() , "-" , TxHelper.getMessageFromFEPReturnCode(getFiscCLRRes().getResponseCode(), FEPChannel.FISC,getLogContext())));
            }
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(getFiscCLRReq().getFISCMessage());
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            //modified by maxin on 2011/07/06 for _rtnCode <> CommonReturnCode.Normal時只送一次EMS
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            //LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";

    }

    /**
     組傳送財金Request電文

     @return

     */
    private FEPReturnCode prepareForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        rtnCode = getFiscBusiness().prepareHeader("0500");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

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
     檢核財金Response電文

     @return

     <modifier>HusanYin</modifier>
     <reason>修正Const RC</reason>
     <date>2010/11/25</date>
     </modify>
     */
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            //檢核Header
            rtnCode = getFiscBusiness().checkHeader(getFiscCLRRes(), true);

            if (rtnCode == FISCReturnCode.MessageTypeError || rtnCode == FISCReturnCode.TraceNumberDuplicate || rtnCode == FISCReturnCode.OriginalMessageError || rtnCode == FISCReturnCode.STANError || rtnCode == FISCReturnCode.CheckBitMapError || rtnCode == FISCReturnCode.SenderIdError) {
                getFiscBusiness().sendGarbledMessage(getFiscCLRReq().getEj(), rtnCode, getFiscCLRRes());
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName , ".processResponse"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

    }

    /**
     更新FEPTXN

     @return

     */
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
                //rtnCode = CType(TxHelper.GetRCFromErrorCode(fiscCLRRes.ResponseCode, FEPChannel.FISC, FEPChannel.FEP, LogContext), FEPReturnCode)
            }
        } else {
            getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //處理結果=Reject
        }

        rtnCode = getFiscBusiness().updateTxData();
        if (_rtnCode != CommonReturnCode.Normal) {
            return _rtnCode;
        } else {
            return rtnCode;
        }
    }

    /**
     InsertCLRTOTAL

     @return

     */
    private FEPReturnCode insertCLRTOTAL() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        int i = 0;
        Clrtotal clrtotal =  dbCLRTOTAL.selectByPrimaryKey(CLRTOTAL.getClrtotalStDate(), CLRTOTAL.getClrtotalCur(), CLRTOTAL.getClrtotalSource());
        if (clrtotal != null) {
            i = dbCLRTOTAL.updateByPrimaryKeySelective(CLRTOTAL);
        } else {
            i = dbCLRTOTAL.insertSelective(CLRTOTAL);
        }
        if (i < 1) {
            rtnCode = IOReturnCode.InsertFail;
        }
        return rtnCode;
    }

}
