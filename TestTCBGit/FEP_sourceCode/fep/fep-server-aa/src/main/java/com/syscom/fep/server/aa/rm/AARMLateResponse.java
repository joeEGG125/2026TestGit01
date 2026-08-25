package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class AARMLateResponse extends RMAABase{
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    public AARMLateResponse(FISCData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        try {

            //1.檢核財金REP電文
            _rtnCode = getmFISCBusiness().checkRMHeader(true, MessageFlow.Response);

            //2.Prepare() 交易記錄, 更新交易記錄(FEPTXN )
            if (_rtnCode == CommonReturnCode.Normal || PolyfillUtil.isNumeric(getmFISCRMReq().getTxnInitiateDateAndTime().substring(0, 6)))
            {
                _rtnCode = prepareAndUpdateFEPTXN();
            }

        }
        catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
        } finally {
            getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getmTxFISCData().getLogContext().setMessage("");
            getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO, getLogContext());
        }

        return "";
    }

    private FEPReturnCode prepareAndUpdateFEPTXN() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            getFeptxn().setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + getmFISCRMRes().getTxnInitiateDateAndTime().substring(0, 6)));
            getFeptxn().setFeptxnStan(getmFISCRMRes().getSystemTraceAuditNo());
            getFeptxn().setFeptxnBkno(getmFISCRMRes().getTxnSourceInstituteId().substring(0, 3));

            getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
            getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
            getFeptxn().setFeptxnAaRc(FISCReturnCode.OriginalMessageError.getValue());
            getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(String.valueOf(FISCReturnCode.OriginalMessageError.getValue()), FEPChannel.FEP, FEPChannel.FISC, getmTxFISCData().getLogContext()));
            //FepTxn.FEPTXN_REP_TIME = Now

            //FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
            //FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString().PadLeft(4, "0"c)

            //更新交易記錄(FEPTXN )
            if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) != 1) {
                return IOReturnCode.FEPTXNUpdateError;
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }

        return rtnCode;

    }
}
