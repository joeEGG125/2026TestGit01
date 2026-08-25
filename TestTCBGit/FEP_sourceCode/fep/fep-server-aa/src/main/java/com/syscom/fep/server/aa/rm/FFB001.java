package com.syscom.fep.server.aa.rm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fcs.FCSHead;

/**
 * 負責處理大批匯款回饋通知程式(由ServiceOutBatch呼叫本程式)
 * 重新命名, 修改組送FCS電文格式
 */
public class FFB001 extends RMAABase {


    //共用變數宣告
    @SuppressWarnings("unused")
    private Object _FFBRsp = new Object();

    /**
     * 建構式 AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception
     */
    public FFB001(RMData txnData) throws Exception {
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
        FEPReturnCode rtnCode = CommonReturnCode.Abnormal;
        RefBase<String> rc = new RefBase<>("");
        RefBase<String> rcMsg = new RefBase<>("");
        try {
            rtnCode = parseFCSFile(rc, rcMsg);

            if (rtnCode == CommonReturnCode.Normal) {
                rtnCode = prepareAndSendFCSService(rc.get(), rcMsg.get());
            }

        } catch (RuntimeException ex) {
            rtnCode = CommonReturnCode.ProgramException;
            logContext.setProgramException(ex);
            sendEMS(logContext);
            getmTxBRSData().getTxObject().getResponse().setRsStatRsStateCode("ERROR");
            return rtnMessage;
        } finally {

            if (rtnCode == CommonReturnCode.Normal) {
                getmTxBRSData().getTxObject().getResponse().setRsStatRsStateCode(NormalRC.External_OK);
            } else {
                getmTxBRSData().getTxObject().getResponse().setRsStatRsStateCode(TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.BRANCH));
                getmTxBRSData().getTxObject().getResponse().setRsStatDesc(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            }

            getmTxBRSData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxBRSData().getLogContext().setMessage(rtnMessage);
            getmTxBRSData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxBRSData().getLogContext().setMessageFlowType(MessageFlow.Response);
            getmTxBRSData().getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode));
            logMessage(Level.INFO, logContext);
        }
        return rtnMessage;
    }

    private FEPReturnCode parseFCSFile(RefBase<String> rc, RefBase<String> rcMsg) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        FCSHead fcsFileHead = null;
        try {
            File file = new File(CleanPathUtil.cleanString(RMConfig.getInstance().getFCSOutPath() + getmRMReq().getBATCHNO()));
            if (file.exists()) {
                String line = "";
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "Big5");
                try (BufferedReader sr = new BufferedReader(read)) {
                     fcsFileHead = new FCSHead();
                     line = sr.readLine();
                     fcsFileHead.parse(line);
                     rc.set(fcsFileHead.getFepRc());
                     rcMsg.set(fcsFileHead.getErrMsg());
                } catch (Exception e) {
                     throw e;
                }
            } else {
                rtnCode = IOReturnCode.FileNotExist;
                logContext.setReturnCode(IOReturnCode.FileNotExist);
                logContext.setRemark("檔案: " + RMConfig.getInstance().getFCSOutPath() + getmRMReq().getBATCHNO() + " 不存在");
                sendEMS(logContext);
            }
            return rtnCode;
        } catch (Exception ex) {
            logContext.setRemark("FFB001-ParseFCSFile exception:" + ex.toString());
            logContext.setReturnCode(CommonReturnCode.ProgramException);
            sendEMS(logContext);
        }
        return rtnCode;
    }

    private FEPReturnCode prepareAndSendFCSService(String rc, String rcMsgs) {

        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {

            rtnCode = getmRMBusiness().sendRequestToFCS(getmRMReq().getBATCHNO(), rc, rcMsgs);

        } catch (RuntimeException ex) {
            logContext.setProgramException(ex);
            logContext.setProgramName(ProgramName);
            logContext.setRemark("FFB001--PrepareAndSendFCSService--exception");
            sendEMS(logContext);
            return CommonReturnCode.ProgramException;
        }
        return rtnCode;
    }
    /**
     * 組回應BRS電文
     */
    @SuppressWarnings("unused")
    private String prepareRspData()
    {
        String rspStr = "";
        //Dim ffb001Rsp As New FFB001_Response
        //ffb001Rsp.RsHeader.
        //rspStr = SerializeToXml(CType(_FFBRsp, FFB001_Response))
        return rspStr;
    }

}
