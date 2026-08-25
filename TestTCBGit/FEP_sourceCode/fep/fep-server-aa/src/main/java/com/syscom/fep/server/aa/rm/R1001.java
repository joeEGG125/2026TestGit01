package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.rm.response.R1001_Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 負責處理大批匯款上傳通知並啟動BTOutBatch程式
 */
public class R1001 extends RMAABase {

    @Autowired
    private BatchExtMapper batchExtMapper;

    @Autowired
    private TaskExtMapper taskExtMapper;

    // 共用變數宣告
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    /**
     * 建構式 AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception e
     */
    public R1001(RMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     * 程式進入點
     *
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";

        try {
            _rtnCode = getmRMBusiness().checkRmstatRmFlag();
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                // Modify by Jim, 2011/01/25, FCS說不用加附檔名了
                // rtnCode = RMBusiness.DownloadFCS(RMReq.BATCHNO & ".TPR")
                _rtnCode = getmRMBusiness().downloadFCS(getmRMReq().getBATCHNO());
                // Modify by Jim, 2011/03/28, 改由BTOutBatch裡增加一支Task call FTPUtility.exe 作Download
                //
                // StartBTOutBatch(RMReq.BATCHNO & ".TPR")
                // _rtnCode = StartBTOutBatch(RMReq.BATCHNO)
                _rtnCode = startOutBatch(getmRMReq().getBATCHNO());
            }

        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            logContext.setProgramException(ex);
            sendEMS(logContext);
            rtnMessage = prepareRspData();
            return rtnMessage;
        } finally {
            // 組ResponseData
            if (StringUtils.isBlank(rtnMessage)) {
                rtnMessage = prepareRspData();
            }
            getmTxBRSData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxBRSData().getLogContext().setMessage(rtnMessage);
            getmTxBRSData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxBRSData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getmTxBRSData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode));
            logMessage(Level.INFO, logContext);
        }
        return rtnMessage;
    }

    /**
     * 啟動批次程式(BTOutBatch)
     *
     * @param fCSFileName fCSFileName
     * @return FEPReturnCode
     */
    @SuppressWarnings("unused")
    private FEPReturnCode startBTOutBatch(String fCSFileName) {
        BatchJobLibrary job = null;
        String batchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
        Batch defBatch = new Batch();
        Task defTASK = new Task();
        try {
            // defTASK.TASK_NAME = defBatch.BATCH_NAME
            defTASK.setTaskName("DownloadFCSRMFile");
            defTASK.setTaskCommandargs("/FTPParameter:FTPServer4" +
                    " /dir:" + RMConfig.getInstance().getFCSInPathFTP() +
                    " /downloadfile:" + fCSFileName + " /localfile:" +
                    RMConfig.getInstance().getFCSInPath() + fCSFileName);

            int iRes = taskExtMapper.updateTASKByNAME(defTASK);
            defTASK = taskExtMapper.selectTaskforName(defTASK);
            if (iRes != 1) {
                logContext.setTableDescription(defTASK.getTaskDescription());
                logContext.setReturnCode(IOReturnCode.UpdateFail);
                logContext.setRemark("更新TASK:DownloadFCSRMFile的參數失敗");
                logMessage(logContext);
                return IOReturnCode.UpdateFail;
            }

            defTASK.setTaskName("RM_OutBatch");
            defTASK.setTaskCommandargs("/FCSInFile:" + fCSFileName);
            iRes = taskExtMapper.updateTASKByNAME(defTASK);
            defTASK = taskExtMapper.selectTaskforName(defTASK);
            if (iRes != 1) {
                logContext.setTableDescription(defTASK.getTaskDescription());
                logContext.setReturnCode(IOReturnCode.UpdateFail);
                logContext.setRemark("更新TASK:RM_OutBatch的參數失敗");
                logMessage(logContext);
                return IOReturnCode.UpdateFail;
            }

            defTASK.setTaskName("RemoveFCSRMFile");
            defTASK.setTaskCommandargs("/FTPParameter:FTPServer4" + " /dir:" + RMConfig.getInstance().getFCSInPathFTP() + " /deletefile:" + fCSFileName);
            iRes = taskExtMapper.updateTASKByNAME(defTASK);
            defTASK = taskExtMapper.selectTaskforName(defTASK);
            if (iRes != 1) {
                logContext.setTableDescription(defTASK.getTaskDescription());
                logContext.setReturnCode(IOReturnCode.UpdateFail);
                logContext.setRemark("更新TASK:RM_OutBatch的參數失敗");
                logMessage(logContext);
                return IOReturnCode.UpdateFail;
            }
            // 如果批次平台檔名修改記得也要一併修改程式
            defBatch.setBatchName("RM_OutBatch");
            Batch record = batchExtMapper.getSingleBATCHByDef(defBatch.getBatchName());
            if (record == null) {
                logContext.setRemark("查詢不到名稱為" + defBatch.getBatchName() + "的資料列 FROM BATCH Table");
                logContext.setReturnCode(IOReturnCode.QueryNoData);
                logMessage(Level.WARN, logContext);
                return IOReturnCode.QueryNoData;
            }
            defBatch = record;

            logContext.setRemark("R1001-Start Call BTOutBatch, DEFBATCH.BATCH_NAME="
                    + defBatch.getBatchName() + ",DEFTASK.TASK_COMMANDARGS=" + defTASK.getTaskCommandargs());
            logContext.setProgramFlowType(ProgramFlow.None);
            logContext.setMessageFlowType(MessageFlow.Request);
            logMessage(Level.DEBUG, logContext);
            job = new BatchJobLibrary();
            job.startBatch(
                    defBatch.getBatchExecuteHostName(),
                    defBatch.getBatchBatchid().toString(),
                    defBatch.getBatchStartjobid().toString());

            logContext.setRemark("R1001-After Call BTOutBatch");
            logMessage(Level.DEBUG, logContext);

        } catch (RuntimeException ex) {
            if (job != null) {
                job.writeLog(ex.toString());
                try {
                    job.abortTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            _rtnCode = CommonReturnCode.ProgramException;
            logContext.setProgramException(ex);
            sendEMS(logContext);
            return _rtnCode;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (job != null) {
                job.dispose();
                job = null;
            }
        }
        return CommonReturnCode.Normal;
    }

    private FEPReturnCode startOutBatch(String fCSFileName) {

        String[] args = {"/batchname:RM_OutBatch_" + fCSFileName, "/FCSInFile:" + fCSFileName};

        try {
            // If String.IsNullOrEmpty(batchLogPath) Then
            // TxBRSData.LogContext.ReturnCode = CommonReturnCode.Abnormal
            // TxBRSData.LogContext.Remark = "Batch Log目錄未設定"
            // LogMessage(LogLevel.Error, LogContext)
            // Return TxBRSData.LogContext.ReturnCode
            // End If

            // 1. 初始化BatchJob物件,傳入工作執行參數, 檢核Batch所需參數
            // job = New BatchJobLibrary(args, batchLogPath)

            // 2. 開始工作內容
            // job.StartTask()
            logContext.setRemark("Start to call BTOutBatch, FCS file name = " + fCSFileName);
            logMessage(Level.DEBUG, logContext);

            R1001_BTOutBatch bt = new R1001_BTOutBatch();
            bt.execute(args);

            logContext.setRemark("After BTOutBatch, FCS file name = " + fCSFileName);
            logMessage(Level.DEBUG, logContext);

            // Else
            // job.AbortTask()
            // TxBRSData.LogContext.ReturnCode = CommonReturnCode.Abnormal
            // TxBRSData.LogContext.Remark = "BTOutBatch執行錯誤"
            // LogMessage(LogLevel.Error, LogContext)
            // Return TxBRSData.LogContext.ReturnCode
            // End If

        } catch (RuntimeException ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            logContext.setProgramException(ex);
            logMessage(Level.ERROR, logContext);
            return _rtnCode;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 組回應電文 組回應BRS電文
     */
    private String prepareRspData() {
        String rspStr = "";
        R1001_Response r1001Res = new R1001_Response();

        // Modify by Jim, 2011/02/10, 新版產生XML回應電文用法
        getmRMBusiness().prepareResponseHeader(_rtnCode);
        try {
            rspStr = r1001Res.makeMessageFromGeneral(getmTxBRSData().getTxObject());
            // rspStr = SerializeToXml(R1001Res)
            // rspStr = RMBusiness.PrepareBRSResponse(Nothing, rspStr)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rspStr;
    }
}
