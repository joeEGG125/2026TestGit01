package com.syscom.fep.server.aa.rm;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.response.C1100_Response;

/**
 * 匯出確認取消AA
 */
public class C1100 extends RMAABase{

    //共用變數宣告
    private Rmout _defRmout = new Rmout();
    @SuppressWarnings("unused")
    private Boolean needUpdateFEPTXN = false;
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
    TransactionStatus db = transactionManager.getTransaction(new DefaultTransactionDefinition());
    private RmoutExtMapper rmoutExtMapper = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
    private RmouttExtMapper rmouttExtMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);

    /**
     * 建構式 AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public C1100(RMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        String rtnMessage = "";

        try {
            //交易記錄初始資料
            _rtnCode = getmRMBusiness().prepareFEPTXNByBRS();

            if (_rtnCode.equals(CommonReturnCode.Normal)){
                //新增交易記錄
                _rtnCode = getmRMBusiness().addTXData();
            }

            //add by maxine on 2011/07/19 for 因RMOUT_STAT =04 也可確認取消,Transaction需要移到select之前

            if (_rtnCode.equals(CommonReturnCode.Normal)){
                //更新匯出主檔 'RMOUT及RMOUTT需設Transaction, 若RMOUTT  insert 失敗需rollback RMOUT
                _rtnCode = updateRMOUTAndRMOUTT();
            }
            //add by maxine on 2011/07/19 for 因RMOUT_STAT =04 也可確認取消,Transaction需要移到select之前
            if (_rtnCode.equals(CommonReturnCode.Normal)){
                transactionManager.commit(db);
            }else {
                transactionManager.rollback(db);
            }

            rtnCode = getmRMBusiness().updateFEPTxn(_rtnCode, FEPTxnMessageFlow.BRS_Response);
            if (_rtnCode.equals(CommonReturnCode.Normal) && !rtnCode.equals(CommonReturnCode.Normal)){
                _rtnCode = rtnCode;
            }
            rtnMessage = prepareRspData();
        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            logContext.setProgramException(ex);
            sendEMS(logContext);
            rtnMessage = prepareRspData();
            return rtnMessage;
        }finally {
            getmTxBRSData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxBRSData().getLogContext().setMessage(rtnMessage);
            getmTxBRSData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxBRSData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getmTxBRSData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode));
            logMessage(Level.INFO,logContext);
        }
        return rtnMessage;
    }

    /**
     * 檢核  相關邏輯檢核
     */
    @SuppressWarnings("unused")
    private FEPReturnCode checkBusinessRule(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        rtnCode =checkBody();
        if (rtnCode == CommonReturnCode.Normal){
            //檢核原匯出資料是否正確
            rtnCode = checkOriginRMOUT();
        }
        return rtnCode;
    }

    /**
     * 檢核BRS電文內容
     */
    private FEPReturnCode checkBody(){

        try {
            if (StringUtils.isBlank(getmRMReq().getRECNM()) || StringUtils.isBlank(getmRMReq().getREMNM())){
                //匯款人名稱, 收款人名稱不得空白
                return RMReturnCode.RemitNameReceiverNameNotBlank;
            }

            if (StringUtils.isBlank(getmRMReq().getSUPNO1().trim())){
                //此交易必須主管授權,主管代號不得空白
                return RMReturnCode.SuperintendentCodeNoBlank;
            }

        } catch (Exception ex) {
            getmTxBRSData().getLogContext().setProgramException(ex);
            getmTxBRSData().getLogContext().setRemark("Check Body發生例外");
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**
     *更新匯出主檔, 匯出暫存檔
     */
    private FEPReturnCode checkOriginRMOUT(){
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        //Dim dbRMOUT As New Tables.DBRMOUT(FEPConfig.DBName)

        try {
            _defRmout.setRmoutTxdate(getmRMReq().getREMDATE());
            _defRmout.setRmoutBrno(getmRMReq().getORGBRNO());
            _defRmout.setRmoutOriginal(RMOUT_ORIGINAL.Counter);
            //_defRMOUT.RMOUT_BATCHNO = "000"
            _defRmout.setRmoutFepno(StringUtils.leftPad(getmRMReq().getFEPNO(),7,'0'));
            _defRmout = rmoutExtMapper.queryByPrimaryKeyWithUpdLock(_defRmout.getRmoutTxdate(),
                    _defRmout.getRmoutBrno(),_defRmout.getRmoutOriginal(),_defRmout.getRmoutFepno());

            //modified by maxine on 2011/07/19 for 因RMOUT_STAT =04 也可確認取消, 所以要改用UPDLOCK, 才不會被Service11X1 讀取匯出財金
            if (_defRmout==null){
                return IOReturnCode.RMOUTNotFound;
            }

            //modified by maxine on 2011/07/19 for 2011-07-14 BU單位決定, 04也可確認取消
            if (getmTxBRSData().getTxObject().getRequest().getChlName().trim().equals(FEPChannel.BRANCH.toString())
                    && !RMOUTStatus.FISCReject.equals(_defRmout.getRmoutStat()) && !RMOUTStatus.Passed.equals(_defRmout.getRmoutStat())) {
                //紀錄狀態錯誤
                return RMReturnCode.RecordStatusError;
            }

            //Jim, 2012/1/11, 非分行發動的互,狀態99也可以做
            if (!getmTxBRSData().getTxObject().getRequest().getChlName().trim().equals(FEPChannel.BRANCH)
          &&  !RMOUTStatus.FISCReject.equals(_defRmout.getRmoutStat()) && !RMOUTStatus.Passed.equals(_defRmout.getRmoutStat())
        && !RMOUTStatus.SystemProblem.equals(_defRmout.getRmoutStat())){
                return RMReturnCode.RecordStatusError; //紀錄狀態錯誤
            }

            if(!getmRMReq().getREMAMT().equals(_defRmout.getRmoutTxamt())){
                //匯款金額與主檔不符
                return RMReturnCode.RemitAmoutNotMach;
            }

        } catch (Exception ex) {
            getmTxBRSData().getLogContext().setProgramException(ex);
            getmTxBRSData().getLogContext().setRemark("檢核原RMOUT發生例外");
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**資料處理
     *  更新匯出主檔, 匯出暫存檔
     */
    private FEPReturnCode updateRMOUTAndRMOUTT(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        Rmoutt defRmoutt = new Rmoutt();

        try {
            _defRmout.setRmoutTxdate(getmRMReq().getREMDATE());
            _defRmout.setRmoutBrno(getmRMReq().getORGBRNO());
            _defRmout.setRmoutOriginal(RMOUT_ORIGINAL.Counter);
            //Jim, 2012/3/27, 不應該塞000
            //defRMOUT.RMOUT_BATCHNO = "000"
            _defRmout.setRmoutFepno(StringUtils.leftPad(getmRMReq().getFEPNO(),7,'0'));
            //待放行
            _defRmout.setRmoutStat(RMOUTStatus.WaitForPass);
            _defRmout.setRmoutSupno1(getmRMReq().getSUPNO1());
            _defRmout.setRmoutApdate("");
            _defRmout.setRmoutAptime("");
            if (rmoutExtMapper.updateByPrimaryKeySelective(_defRmout)<1){
                return IOReturnCode.RMOUTUpdateError;
            }

            //if RMOUTT刪除失敗，rollback
            defRmoutt.setRmouttBatchno(_defRmout.getRmoutBatchno());
            defRmoutt.setRmouttFepno(_defRmout.getRmoutFepno());
            defRmoutt.setRmouttTxdate(_defRmout.getRmoutTxdate());
            defRmoutt.setRmouttBrno(_defRmout.getRmoutBrno());
            defRmoutt.setRmouttOriginal(_defRmout.getRmoutOriginal());
            //Modified by Jim, 2010/08/04, RMOUTT要改用刪除, 這樣事後RT1300主管確認才可執行OK
            if (rmouttExtMapper.deleteByPrimaryKey(defRmoutt)<1){
                // modified by maxine on 2011/07/19 for 因RMOUT_STAT =04 也可確認取消,Transaction需要移到select之前
                // db.RollbackTransaction()
                return IOReturnCode.DeleteFail;
            }
        } catch (Exception ex) {
            getmTxBRSData().getLogContext().setProgramException(ex);
            getmTxBRSData().getLogContext().setRemark("更新RMOUT,RMOUTT發生例外");
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 組回應電文   組回應BRS電文
     */
    private String prepareRspData(){
        String rspStr = "";
        C1100_Response c1100Res = new C1100_Response();

        if (_defRmout != null){
            getmRMRes().setFEPNO(_defRmout.getRmoutFepno());
        }
        getmRMBusiness().prepareResponseHeader(_rtnCode);
        try {
            rspStr = c1100Res.makeMessageFromGeneral(getmTxBRSData().getTxObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rspStr;
    }
}
