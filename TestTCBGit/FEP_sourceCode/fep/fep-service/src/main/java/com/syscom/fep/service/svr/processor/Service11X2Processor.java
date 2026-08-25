package com.syscom.fep.service.svr.processor;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.RminExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmintExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.CbspendMapper;
import com.syscom.fep.mybatis.model.Cbspend;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rmint;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.service.svr.SvrProcessor;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.T24TxType;
import com.syscom.fep.vo.text.rm.RMGeneral;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.CBS_Request;

@StackTracePointCut(caller = SvrConst.SVR_11X2)
public class Service11X2Processor extends SvrProcessor {
    private String ProgramName = Service11X2Processor.class.getSimpleName() + ".";

    private RMData TxRMData;
    private RM TxRMBusiness;

    private FeptxnDao DBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
    private Feptxn FepTxn;

    private Rmin mDefRMIN;

    private FEPReturnCode _rtnCode;
    private String PK = "";

    public boolean StopFlag = false;


    @Override
    protected void initialization() throws Exception {
        //紀錄Log
        setLogContext(new LogData());

        getLogContext().setProgramFlowType(ProgramFlow.AAServiceIn);
        getLogContext().setMessageFlowType(MessageFlow.Request);
        getLogContext().setSubSys(SubSystem.RM);
        getLogContext().setProgramName("Service11X2");
        getLogContext().setMessage("");
        getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        getLogContext().setChannel(FEPChannel.FISC);
        getLogContext().setRemark("Service11X2 Start");

        logMessage(Level.INFO, getLogContext());

        initBusiness("0");
    }

    @Override
    protected FEPReturnCode doBusiness() throws Exception {
        List<Rmint> arrayRMINT = new ArrayList<>();

        RmintExtMapper dbRMINT = SpringBeanFactoryUtil.getBean(RmintExtMapper.class);
        Rmint defRMINT = new Rmint();

        @SuppressWarnings("unused")
		Cbspend defCBSPend = new Cbspend();
        @SuppressWarnings("unused")
        CbspendMapper dbCBSPend = SpringBeanFactoryUtil.getBean(CbspendMapper.class);

        getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8),StringUtils.join(ProgramName,"doBusiness"));

        @SuppressWarnings("unused")
        int i = 0;

        try {
            //Modify by Jim, 2011/06/01, 強迫快取重新整理
            FEPCache.reloadCache(CacheItem.SYSSTAT);

            //1.讀取匯入暫存檔RMINT
            defRMINT.setRmintTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            //Modify by Jim, 2011/06/15, 改成GetArrayListForService11X2裡面固定抓RMINT_STAT IN ('01','07')
            //defRMINT.RMINT_STAT = RMINStatus.AutoEnterAccount
            defRMINT.setRmintPending("P");
            defRMINT.setRmintFiscRtnCode(NormalRC.FISC_OK);

            arrayRMINT = dbRMINT.getArrayListForService11X2(defRMINT);

            LogHelperFactory.getTraceLogger().trace(StringUtils.join("Service11X2-DoBusiness - Query RMINT Count=" , arrayRMINT.size()));

            //以前項SELECT RMINT匯入暫存檔內容 LOOP
            for (Rmint objRMINT : arrayRMINT) {
                getLogContext().setStep(0);
                defRMINT = objRMINT;

                initBusiness("1");

                PK = StringUtils.join("RMINT-PK:(RMINT_TXDATE:" , defRMINT.getRmintTxdate() , "RMINT_BRNO:" , defRMINT.getRmintBrno() , "RMINT_FEPNO:" , defRMINT.getRmintFepno() , ")");
                getLogContext().setRemark(StringUtils.join("Begin Transaction: " , PK));
                logMessage(Level.INFO, getLogContext());

                //Modify by Jim, 2011/06/15, FEPTXN UPDATE及CheckCBSStatus順序對調
                //3.檢核CBS主機狀態
                _rtnCode = TxRMBusiness.checkCBSStatus();
                if (_rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark(StringUtils.join("檢核CBS主機狀態, rtnCode = " , _rtnCode.toString()));
                    getLogContext().setReturnCode(_rtnCode);
                    //20210818 只記log 不SEND EMS
                    logMessage(Level.INFO, getLogContext());
                    //TxHelper.GetRCFromErrorCode(_rtnCode, FEPChannel.FEP, LogContext)
                    break;
                }

                //4.更新交易記錄(FEPTXN)
                FepTxn.setFeptxnMsgflow(CBS_Request);
                _rtnCode = updateFEPTXN(defRMINT);
                if (_rtnCode != CommonReturnCode.Normal || FepTxn == null) {
                    getLogContext().setRemark(StringUtils.join("DoBusiness-UpdateFEPTXN error; " , PK));
                    getLogContext().setReturnCode(_rtnCode);
                    TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.RM, getLogContext());
                    continue;
                }

                //5.組送往 T24 自動入帳電文
                _rtnCode = updateRMINForEJNO2(defRMINT);
                if (_rtnCode != CommonReturnCode.Normal) {
                    if (_rtnCode != CommonReturnCode.ProgramException) {
                        getLogContext().setRemark(StringUtils.join("UpdateRMINForEJNO2 error, rtnCode=" , _rtnCode.toString() , "; " , PK));
                        getLogContext().setReturnCode(_rtnCode);
                        TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.RM, getLogContext());
                    }
                    continue;
                }

                //送往 T24 自動入帳電文
                _rtnCode = TxRMBusiness.sendToCBS("R1900", (byte) T24TxType.Accounting.getValue(), mDefRMIN, null, null);
                if (_rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark(StringUtils.join("SendToCBS error, rtnCode=" , _rtnCode.toString() , "; " , PK));
                    getLogContext().setReturnCode(_rtnCode);
                    TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.RM, getLogContext());
                    continue;
                }

                //6.更新匯款跨行匯兌日結檔(RMTOT)及匯兌統計日結檔(RMTOTAL) CBS主機自動入帳OK
                _rtnCode = TxRMBusiness.processRMTOTAndRMTOTAL("SV11X2", defRMINT.getRmintReceiverBank().substring(3, 6), "060", defRMINT.getRmintTxdate(), defRMINT.getRmintTxamt());

                //7.更新交易記錄(FEPTXN)
                //If _rtnCode <> CommonReturnCode.Normal Then
                //    FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
                //    FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString()
                //End If

                if (DBFEPTXN.updateByPrimaryKeySelective(FepTxn) != 1) {
                    getLogContext().setTableName("FEPTXN");
                    getLogContext().setPrimaryKeys(StringUtils.join("FEPTXN_TX_DATE=" , FepTxn.getFeptxnTxDate() , ";FEPTXN_EJFNO=" , FepTxn.getFeptxnEjfno()));
                    getLogContext().setRemark(StringUtils.join("Final UpdateFEPTXN <> 1; " , PK));
                    getLogContext().setReturnCode(IOReturnCode.FEPTXNUpdateError);
                    TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
                }

                getLogContext().setRemark(StringUtils.join("End Transaction: " , PK));
                logMessage(Level.INFO, getLogContext());
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            //Return CommonReturnCode.ProgramException
        } finally {
            LogHelperFactory.getTraceLogger().trace(StringUtils.join("Service11X2-doBusiness - Service11X2 Sleep=" , (RMConfig.getInstance().getService11X2INTERVAL() * 1000)));
            Thread.sleep(RMConfig.getInstance().getService11X2INTERVAL() * 1000);
            //If Not StopFlag Then
            //    doBusiness()
            //End If
        }

        return CommonReturnCode.Normal;
    }

    @Override
    protected void doPause() throws Exception {

    }

    @Override
    protected void doStop() throws Exception {
        //紀錄Log
        getLogContext().setProgramFlowType(ProgramFlow.AAServiceOut);
        getLogContext().setSubSys(SubSystem.RM);
        getLogContext().setProgramName("Service11X2");
        getLogContext().setMessage("");
        getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        getLogContext().setChannel(FEPChannel.FISC);
        getLogContext().setRemark("Service11X2 Stop");
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatFbkno());
        logMessage(Level.INFO, getLogContext());

        if (TxRMBusiness != null) {
            TxRMBusiness = null;
        }

        if (DBFEPTXN != null) {
            DBFEPTXN = null;
        }
    }

    private void initBusiness(String sFlag) throws Exception {

        int iNewEJ = 0;

        //準備FEPTXN對象
        FepTxn = new FeptxnExt();
        DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), StringUtils.join(ProgramName,"initBusiness"));

        if ("0".equals(sFlag)) {
            TxRMData = new RMData();
            TxRMData.setAaName("Service11X2");
            TxRMData.setMessageFlowType(MessageFlow.Request);
            TxRMData.setTxSubSystem(SubSystem.RM);
            TxRMData.setTxChannel(FEPChannel.FISC);
            TxRMData.setLogContext(getLogContext());
            TxRMData.setTxObject(new RMGeneral());
        } else {
            iNewEJ = TxHelper.generateEj();
            //TxRMData.MessageID = DefRMINT.RMINT_FISC_SND_CODE & "00"
            //TxRMData.MsgCtl = FEPCache.GetMSGCTRL()(TxRMData.MessageID)
            //If TxRMData.MsgCtl.MSGCTL_STATUS = 1 Then
            //    TxRMData.TxStatus = True
            //Else
            //    TxRMData.TxStatus = False
            //End If

        }

        getLogContext().setEj(iNewEJ);
        getLogContext().setMessageFlowType(MessageFlow.Request);
        getLogContext().setMessage("");
        getLogContext().setRemark("");
        getLogContext().setMessageGroup("4");
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatFbkno());

        TxRMData.setEj(iNewEJ);
        TxRMData.setFeptxn(FepTxn);
        TxRMData.setFeptxnDao(DBFEPTXN);

        TxRMBusiness = new RM(TxRMData);
        TxRMBusiness.setFeptxnDao(DBFEPTXN);
        TxRMBusiness.setFeptxn(FepTxn);
        TxRMBusiness.setEj(iNewEJ);
        TxRMBusiness.setLogContext(getLogContext());

    }

    private FEPReturnCode updateRMINForEJNO2(Rmint defRMINT) {
        RminExtMapper dbRMIN = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
        RmintExtMapper dbRMINT = SpringBeanFactoryUtil.getBean(RmintExtMapper.class);
        PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
        TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {

            defRMINT.setRmintEjno2(TxRMData.getEj());

            if (dbRMINT.updateByPrimaryKeySelective(defRMINT) != 1) {
                transactionManager.rollback(txStatus);
                return IOReturnCode.RMINUpdateError;
            }

            mDefRMIN = new Rmin();

            mDefRMIN.setRminTxdate(defRMINT.getRmintTxdate());
            mDefRMIN.setRminBrno(defRMINT.getRmintBrno());
            mDefRMIN.setRminFepno(defRMINT.getRmintFepno());

            mDefRMIN.setRminEjno2(defRMINT.getRmintEjno2());

            if (dbRMIN.updateByPrimaryKeySelective(mDefRMIN) != 1) {
                transactionManager.rollback(txStatus);
                return IOReturnCode.RMINUpdateError;
            }

            transactionManager.commit(txStatus);

            //查詢出defRMIN組T24電文使用
            Rmin rmin = dbRMIN.selectByPrimaryKey(mDefRMIN.getRminTxdate(),mDefRMIN.getRminBrno(),mDefRMIN.getRminFepno());
            if (rmin != null) {
                mDefRMIN = rmin;
                return CommonReturnCode.Normal;
            } else {
                return IOReturnCode.RMINNotFound;
            }
        } catch (Exception ex) {
            if (txStatus != null  && !txStatus.isCompleted()) {
                transactionManager.rollback(txStatus);
            }
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     更新FEPTXN(PK在function中指定,更新的內容在呼叫function之前指定)

     @param defRMINT 處理的defRMINT
     @return

     */
    private FEPReturnCode updateFEPTXN(Rmint defRMINT) {

        try {
            FepTxn.setFeptxnTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            //Modify by Jim, 2011/06/22, 改成用PK查詢
            int l = 0;
            boolean isOK = false;
            try{
                l = Integer.parseInt(defRMINT.getRmintEjno1().toString());
                isOK = true;
            }catch(Exception e){
                isOK = false;
            }
            if(isOK){
                FepTxn.setFeptxnEjfno(l);
            }
            //FepTxn.FEPTXN_BKNO = SysStatus.PropertyValue.SYSSTAT_FBKNO
            //FepTxn.FEPTXN_STAN = defRMINT.RMINT_STAN

            boolean foundFEPTXN = false;
            Feptxn feptxn = DBFEPTXN.selectByPrimaryKey(FepTxn.getFeptxnTxDate(),FepTxn.getFeptxnEjfno());
            if (feptxn != null) {
                FepTxn = feptxn;
                foundFEPTXN = true;
                TxRMBusiness.setFeptxn(FepTxn);
            }

            if (!foundFEPTXN) {
                getLogContext().setRemark("找不到FEPTXN");
                getLogContext().setTableName("FEPTXN");
                getLogContext().setPrimaryKeys(StringUtils.join("FEPTXN_TX_DATE=" , LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) , ";FEPTXN_EJFNO=" , FepTxn.getFeptxnEjfno()));
                TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.FEPTXNNotFound.getValue()), FEPChannel.FEP, getLogContext().getChannel(), getLogContext());
                FepTxn = new FeptxnExt();
                return IOReturnCode.UpdateFail;
            }

            if (DBFEPTXN.updateByPrimaryKeySelective(FepTxn) != 1) {
                getLogContext().setTableName("FEPTXN");
                getLogContext().setPrimaryKeys(StringUtils.join("FEPTXN_TX_DATE=" , FepTxn.getFeptxnTxDate() , ";FEPTXN_EJFNO=" , FepTxn.getFeptxnEjfno()));
                TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.UpdateFail.getValue()), FEPChannel.FEP, getLogContext().getChannel(), getLogContext());
                return IOReturnCode.UpdateFail;
            }

            return CommonReturnCode.Normal;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
