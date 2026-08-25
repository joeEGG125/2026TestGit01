package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.IntltxnExtMapper;
import com.syscom.fep.mybatis.model.Intltxn;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.IOReturnCode;

public class SendConfirmByManual extends INBKAABase  {
    //"共用變數宣告"
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    //"建構式"
    public SendConfirmByManual(FISCData txnData) throws Exception {
        super(txnData);
    }

    //"AA進入點主程式"
    @Override
    public String processRequestData() {
        try {
            //讀取交易記錄檔
            _rtnCode = searchFeptxn();
            //更新交易記錄檔(FEPTXN/INTLTXN)
            if (_rtnCode == CommonReturnCode.Normal){
                _rtnCode = updateTxdata();
            }
            //沖轉跨行代收付
            if (_rtnCode == CommonReturnCode.Normal){
//                _rtnCode = processAptot(); //2025.08.28先關UPDATE APTOT
            }
            //送Confirm電文至FISC
            if (_rtnCode == CommonReturnCode.Normal){
                //Fly 2016/08/30 修改 for EMV 國際卡交易
                if ("26".equals(getFiscBusiness().getFeptxn().getFeptxnPcode().substring(0,2))){
                    _rtnCode = getFiscBusiness().sendConfirmToFISCEMV();
                }else {
                    _rtnCode = getFiscBusiness().sendConfirmToFISC();
                }
            }
        }catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        }finally {
            getLogContext().setProgramName(this.aaName);
            getTxData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            if ( FISCSubSystem.EMVIC.equals(getFiscBusiness().getFISCTxData().getFiscTeleType())) {
                getLogContext().setMessage(getFiscEMVICCon().getFISCMessage());
            }else {
                getLogContext().setMessage(getFiscCon().getFISCMessage());
            }
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            getLogContext().setProgramName(this.aaName);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    //"本支AA共用之sub routine"
    //準備交易記錄檔
    private FEPReturnCode searchFeptxn(){
        try {
            if (getFiscBusiness().getFISCTxData().getFiscTeleType() == FISCSubSystem.EMVIC) {
                if ("".equals(getFiscEMVICReq().getTxnInitiateDateAndTime())){
                    getFiscBusiness().getFeptxn().setFeptxnTxDate(CalendarUtil.rocStringToADString(getFiscEMVICReq().getTxnInitiateDateAndTime()));
                }else {
                    getFiscBusiness().getFeptxn().setFeptxnTxDate(CalendarUtil.rocStringToADString(getFiscEMVICReq().getTxnInitiateDateAndTime().substring(0,7)));
                }
                getFiscBusiness().getFeptxn().setFeptxnEjfno(getFiscEMVICReq().getEj());
                feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
                feptxnDao.setTableNameSuffix(getFiscEMVICReq().getOriTxDate().substring(6, 8),StringUtils.join(ProgramName, "searchFeptxn"));
//                this.feptxnDao.setTableNameSuffix(getFiscEMVICReq().getOriTxDate().substring(6, 8), StringUtils.join(ProgramName, "searchFeptxn"));
            }else {
                if (!"".equals(getFiscReq().getTxnInitiateDateAndTime())){
                    getFiscBusiness().getFeptxn().setFeptxnTxDate(CalendarUtil.rocStringToADString(getFiscReq().getTxnInitiateDateAndTime().substring(0,7)));
                }
                getFiscBusiness().getFeptxn().setFeptxnEjfno(getFiscReq().getEj());
                feptxnDao = SpringBeanFactoryUtil.getBean("feptxnDao");
                feptxnDao.setTableNameSuffix(getFiscReq().getTxDatetimeFisc().substring(6, 8), StringUtils.join(ProgramName, "searchFeptxn"));
//                this.feptxnDao.setTableNameSuffix(getFiscReq().getTxDatetimeFisc().substring(6, 8), StringUtils.join(ProgramName, "searchFeptxn"));
            }
            // 2012/06/11 Modify by Ruling 因要找原交易所在的FEPTXN需重取DBFepTxn，但沒同步到fiscBusiness.DBFepTxn再換日後SendConfirmToFISC時會更新原交易的FEPTXN失敗
            getFiscBusiness().setFeptxnDao(this.feptxnDao);
            try {
                getFiscBusiness().setFeptxn(this.feptxnDao.selectByPrimaryKey(getFiscBusiness().getFeptxn().getFeptxnTxDate(), getFiscBusiness().getFeptxn().getFeptxnEjfno()));
            } catch (Exception e) {
                e.printStackTrace();
                return IOReturnCode.FEPTXNReadError;
            }
            if (getFiscBusiness().getFeptxn() == null){
                return IOReturnCode.FEPTXNNotFound;
            }else {
                String rscode;
                if (FISCSubSystem.EMVIC.equals(getFiscBusiness().getFISCTxData().getFiscTeleType())){
                    rscode = getFiscEMVICReq().getAuthCode();
                }else {
                    rscode = getFiscReq().getRsCode();
                }
                switch (rscode){
                    case "1": //Positive Confirm(RC=4001)
                        getFiscBusiness().getFeptxn().setFeptxnConRc(NormalRC.FISC_ATM_OK);
                        break;
                    case "2": //Negative Confirm(RC=0501)
                        getFiscBusiness().getFeptxn().setFeptxnConRc("0501");
                        //2014/09/22 Modify by Ruling for 修改會發生轉型的錯誤
                        getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.TerminalError.getValue());
                        //fiscBusiness.FepTxn.FEPTXN_AA_RC = Integer.Parse(fiscReq.ResponseCode)
                        break;
                    case "3": //Negative Confirm(RC=0601)
                        getFiscBusiness().getFeptxn().setFeptxnConRc("0601");
                        //2014/09/22 Modify by Ruling for 修改會發生轉型的錯誤
                        getFiscBusiness().getFeptxn().setFeptxnAaRc(FISCReturnCode.FISCTimeout.getValue());
                        //fiscBusiness.FepTxn.FEPTXN_AA_RC = Integer.Parse(fiscReq.ResponseCode)
                        break;
                }
                return CommonReturnCode.Normal;
            }
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".searchFeptxn");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }finally {
            //清除UI借用的電文欄位
            if (FISCSubSystem.EMVIC.equals(getFiscBusiness().getFISCTxData().getFiscTeleType())){
                getFiscEMVICReq().setTxnInitiateDateAndTime(null);
                getFiscEMVICReq().setEj(null);
                getFiscEMVICReq().setAuthCode(null);
                getFiscEMVICReq().setResponseCode(null);
                getFiscEMVICReq().setOriTxDate(null);
                getFiscEMVICReq().setSyncCheckItem(null);
            }else {
                getFiscReq().setTxnInitiateDateAndTime(null);
                getFiscReq().setEj(null);
                getFiscReq().setRsCode(null);
                getFiscReq().setResponseCode(null);
                getFiscReq().setTxDatetimeFisc(null);
                getFiscReq().setSyncCheckItem(null);
            }
        }
    }

    //更新交易記錄(FEPTXN/INTLTXN)
    private FEPReturnCode updateTxdata() {
        Intltxn aIntltxn = new Intltxn();
        IntltxnExtMapper intltxnExtMapper = SpringBeanFactoryUtil.getBean(IntltxnExtMapper.class);
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        @SuppressWarnings("unused")
        Intltxn intltxn = new Intltxn();
        try {
            int i = this.feptxnDao.updateByPrimaryKeySelective(getFiscBusiness().getFeptxn());
            if (i < 1) {
                transactionManager.rollback(txStatus);
                return IOReturnCode.FEPTXNUpdateError;
            }
            //Fly 2016/08/30 修改 for EMV 國際卡交易
            if ("24".equals(getFiscBusiness().getFeptxn().getFeptxnPcode().substring(0,2)) || "26".equals(getFiscBusiness().getFeptxn().getFeptxnPcode().substring(0,2))){
                aIntltxn.setIntltxnConRc(getFiscBusiness().getFeptxn().getFeptxnConRc());
                aIntltxn.setIntltxnTxDate(getFiscBusiness().getFeptxn().getFeptxnTxDate());
                aIntltxn.setIntltxnEjfno(getFiscBusiness().getFeptxn().getFeptxnEjfno());
                int k = intltxnExtMapper.updateByPrimaryKeySelective(aIntltxn);
                if (k != 1) {
                    transactionManager.rollback(txStatus);
                    return IOReturnCode.UpdateFail;
                }
            }
            transactionManager.commit(txStatus);
            return CommonReturnCode.Normal;
        }catch (Exception ex) {
            transactionManager.rollback(txStatus);
            ex.printStackTrace();
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".updateTxdata");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //沖轉跨行代收付
    private FEPReturnCode processAptot() {
        if (NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc()) && !NormalRC.FISC_ATM_OK.equals(getFiscBusiness().getFeptxn().getFeptxnConRc())) {
            return getFiscBusiness().processAptot(true);
        }
        return CommonReturnCode.Normal;
    }
}
