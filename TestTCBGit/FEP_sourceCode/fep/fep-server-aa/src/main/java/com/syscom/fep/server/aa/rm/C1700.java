package com.syscom.fep.server.aa.rm;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.response.C1700_Response;

/**
 * 負責處理緊急匯出確認取消交易
 */
public class C1700 extends RMAABase {
    //共用變數宣告
    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
    TransactionStatus db = transactionManager.getTransaction(new DefaultTransactionDefinition());
    private Rmout _defRmout;
    private RmoutExtMapper rmoutExtMapper = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
    private RmouttExtMapper rmouttExtMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
    /**
     * 建構式 AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public C1700(RMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        try {
            //2. Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN )
            _rtnCode = getmRMBusiness().prepareFEPTXNByBRS();

            //新增一筆FEPTXN
            if (CommonReturnCode.Normal.equals(_rtnCode)) {
                _rtnCode = getmRMBusiness().addTXData();
            }

            //3. 檢核BRS電文內容
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = checkBody();
            }
            //add by maxine on 2011/07/19 for 因RMOUT_STAT =04 也可確認取消,Transaction需要移到select之前
            // db.BeginTransaction()
            //4.檢核原匯出資料是否正確
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = checkRMOUT();
            }
            //5. 更新匯出主檔(RMOUT)
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = updateRMOUTAndRMOUTT();
            }
            //add by maxine on 2011/07/21 for 因RMOUT_STAT =04 也可確認取消,Transaction需要移到select之前
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                transactionManager.commit(db);
            }else {
                transactionManager.rollback(db);
            }
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
            rtnMessage = prepareRspData();
            return rtnMessage;
        }finally {
            //更新FEPTxn
            getmRMBusiness().updateFEPTxn(_rtnCode, FEPTxnMessageFlow.BRS_Response);

            //組ResponseData
            if (StringUtils.isBlank(rtnMessage)){
                rtnMessage = prepareRspData();
            }
            getmTxBRSData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxBRSData().getLogContext().setMessage(rtnMessage);
            getmTxBRSData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxBRSData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO,logContext);
        }
        return rtnMessage;
    }

    /**
     * 3. 檢核BRS電文內容 RT1012 電文檢核
     */
    private FEPReturnCode checkBody() {
        //收款人,匯款人姓名不可為空白
        if (StringUtils.isBlank(getmRMReq().getRECNM()) || StringUtils.isBlank(getmRMReq().getREMNM())) {
            return RMReturnCode.RemitNameReceiverNameNotBlank;
        }

        if (StringUtils.isBlank(getmRMReq().getSUPNO1())) {
            //此交易必須主管授權,主管代號不得空白
            return RMReturnCode.SuperintendentCodeNoBlank;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 4. 檢核原匯出資料是否正確
     */
    private FEPReturnCode checkRMOUT() {
        try {
            _defRmout = new Rmout();

            _defRmout.setRmoutTxdate(getmRMReq().getREMDATE());
            _defRmout.setRmoutBrno(getmRMReq().getORGBRNO());
            _defRmout.setRmoutOriginal(RMOUT_ORIGINAL.EmergencyTransfer);
            _defRmout.setRmoutFepno(StringUtils.leftPad(getmRMReq().getFEPNO(), 7, '0'));

            //modified by maxine on 2011/07/21 for 因RMOUT_STAT =04 也可確認取消, 所以要改用UPDLOCK, 才不會被Service11X1 讀取匯出財金
            _defRmout = rmoutExtMapper.queryByPrimaryKeyWithUpdLock(_defRmout.getRmoutTxdate(),
                    _defRmout.getRmoutBrno(), _defRmout.getRmoutOriginal(), _defRmout.getRmoutFepno());
            if (_defRmout == null) {
                return IOReturnCode.RMOUTNotFound;
            }

            //Modify by Jim, 2011/04/21, BU單位改為只要STAT=03待放行07:財金拒絕,04-已放行,才可確認取消
            if (!_defRmout.getRmoutStat().equals(RMOUTStatus.WaitForPass)
                    && !_defRmout.getRmoutStat().equals(RMOUTStatus.FISCReject)
                    && !_defRmout.getRmoutStat().equals(RMOUTStatus.Passed)) {
                //記錄狀態錯誤
                return RMReturnCode.RecordStatusError;
            }

            if (!_defRmout.getRmoutTxamt().equals(getmRMReq().getREMAMT())) {
                //匯款金額與主檔不符
                return RMReturnCode.RemitAmoutNotMach;
            }
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
            rtnMessage = prepareRspData();
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 5. 更新匯出主檔(RMOUT)
     */
    private FEPReturnCode updateRMOUTAndRMOUTT(){
        Rmoutt defRmoutt = new Rmoutt();

        String orgStat = "";

        try {
            orgStat = _defRmout.getRmoutStat();
            //12=緊急匯款已刪除
            _defRmout.setRmoutStat(RMOUTStatus.DeleteEmergencyTransfer);
            _defRmout.setRmoutSupno1(getmRMReq().getSUPNO1());
            _defRmout.setRmoutApdate("");
            _defRmout.setRmoutAptime("");

            //已經送過財金的才會有RMOUTT
            if (orgStat.equals(RMOUTStatus.FISCReject) || orgStat.equals(RMOUTStatus.Passed)){
                defRmoutt.setRmouttTxdate(_defRmout.getRmoutTxdate());
                defRmoutt.setRmouttBrno(_defRmout.getRmoutBrno());
                defRmoutt.setRmouttFepno(_defRmout.getRmoutFepno());
                defRmoutt.setRmouttOriginal(_defRmout.getRmoutOriginal());

                defRmoutt.setRmouttStat(_defRmout.getRmoutStat());
                defRmoutt.setRmouttSupno1(_defRmout.getRmoutSupno1());
                defRmoutt.setRmouttApdate("");
                defRmoutt.setRmouttAptime("");

                if (rmouttExtMapper.updateByPrimaryKeySelective(defRmoutt)!=1){
                    return IOReturnCode.RMOUTUpdateError;
                }
            }
        } catch (Exception ex) {
           logContext.setProgramException(ex);
            sendEMS(logContext);
            rtnMessage = prepareRspData();
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }
    /**
     * 組回應電文   組回應BRS電文
     */
    private String prepareRspData() {
        String rspStr = "";
        C1700_Response c1700Res = new C1700_Response();

        getmTxBRSData().getTxObject().getResponse().setFEPNO(StringUtils.leftPad(getmRMReq().getFEPNO(),7,'0'));
        getmRMBusiness().prepareResponseHeader(_rtnCode);
        try {
            rspStr = c1700Res.makeMessageFromGeneral(getmTxBRSData().getTxObject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rspStr;
    }
}
