package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.INBKData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.model.Npsunit;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.inbk.response.S0710Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AA0011 extends INBKAABase{
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private Npsunit NPSUNIT = new Npsunit();
    private ArrayList<Npsunit>  npsunits = new ArrayList<>();
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private S0710Response S0710Res = new S0710Response();
    private String SuccessTag = "SUCCESS";

    public AA0011(INBKData txnData) throws Exception {
        super(txnData);
    }

    @Override
    public String processRequestData() {
        String rtnMessage = "";
        try {
            //.讀取委託單位檔()
            _rtnCode = queryNPSUNITbyUNITNO();

            //將多筆(N)資料傳回分行系統UI
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareResponse();
            }

        } catch (Exception ex) {
            getINBKResponse().setRESULT(MathUtil.toString(BigDecimal.valueOf(CommonReturnCode.ProgramException.getValue()), "0000"));
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName,".processRequestData"));
            _rtnCode = CommonReturnCode.ProgramException;
            sendEMS(getLogContext());
        } finally {
            //回分行UI
            prepareTOTAHeader(_rtnCode);
            rtnMessage = S0710Res.makeMessageFromGeneral(getINBKtxData().getTxObject());
            //rtnMessage = SerializeToXml(gA0011_Response).Substring(38)
            getINBKtxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            //INBKtxData.LogContext.Message = INBKtxData.TxRequestMessage
            getINBKtxData().getLogContext().setMessage(rtnMessage);
            getINBKtxData().getLogContext().setProgramName(this.aaName);
            getINBKtxData().getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.DEBUG, getLogContext());
        }
        return rtnMessage;
    }

    /**
     "以委託單位代號讀取多筆資料"

     @return

     */
    private FEPReturnCode queryNPSUNITbyUNITNO() {
        try {
            //檢核Header
            NPSUNIT.setNpsunitNo(getINBKRequest().getUNITNO());
            npsunits = dbNPSUNIT.queryNPSUNITbyUNITNO(NPSUNIT.getNpsunitNo());
            if (npsunits.size() == 0) {
                getINBKResponse().setRESULT(MathUtil.toString(BigDecimal.valueOf(IOReturnCode.NPSUNITNotFound.getValue()), "0000")); //& TxHelper.GetMessageFromFEPReturnCode(IOReturnCode.NPSUNITNotFound)
                return IOReturnCode.NPSUNITNotFound;
            }
        } catch (Exception ex) {
            getINBKResponse().setRESULT(MathUtil.toString(BigDecimal.valueOf(IOReturnCode.NPSUNITNotFound.getValue()), "0000"));
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName,".queryNPSUNITbyUNITNO"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**
     "以委託單位代號讀取多筆資料"

     @return

     */
    private FEPReturnCode prepareResponse() {
        int i = 0;

        try {
            getINBKResponse().setUNITNO(getINBKRequest().getUNITNO());
            getINBKResponse().setALIASNAME(npsunits.get(0).getNpsunitName()); //取第一筆
            getINBKResponse().setRECCOUNT(String.valueOf(npsunits.size()));

            getINBKtxData().getTxObject().setS0710SvcRs(new S0710Response.S0710SvcRs());
            getINBKtxData().getTxObject().getS0710SvcRs().setRs(new S0710Response.S0710Rs());
            S0710Response.S0710RsRcd[] rcdArrary = new S0710Response.S0710RsRcd[npsunits.size()];
            getINBKtxData().getTxObject().getS0710SvcRs().getRs().setRcds(rcdArrary);
            //將讀到之多筆 NPSUNIT 逐筆搬至 UI 之對應欄位
            for (Npsunit dt : npsunits) {
                rcdArrary[i] = new S0710Response.S0710RsRcd();
                rcdArrary[i].setPAYTYPE(dt.getNpsunitPaytype());
                rcdArrary[i].setFEENO(dt.getNpsunitFeeno());
                rcdArrary[i].setPAYNAME(dt.getNpsunitPayName());
                i += 1;
            }

            getINBKtxData().getTxObject().getS0710SvcRs().getRs().setUNITNO(getINBKResponse().getUNITNO());
            getINBKtxData().getTxObject().getS0710SvcRs().getRs().setALIASNAME(getINBKResponse().getALIASNAME());
            getINBKtxData().getTxObject().getS0710SvcRs().getRs().setRECCOUNT(getINBKResponse().getRECCOUNT());

        } catch (Exception ex) {
            getINBKResponse().setRESULT(MathUtil.toString(BigDecimal.valueOf(CommonReturnCode.ProgramException.getValue()), "0000"));
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName,".prepareResponse"));
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }


    private void prepareTOTAHeader(FEPReturnCode rtnCode) {
        getINBKtxData().getTxObject().getResponse().setRsTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.fff")));
        getINBKtxData().getTxObject().getResponse().setEJNo(StringUtils.join(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),StringUtils.leftPad(String.valueOf(getEj()), 12, '0')));
        if (rtnCode == FEPReturnCode.Normal) {
            getINBKtxData().getTxObject().getResponse().setRsStatRsStateCode(SuccessTag);
            getINBKtxData().getTxObject().getResponse().setRsStatDesc("");
        } else {
            getLogContext().setProgramName(ProgramName);
            getINBKtxData().getTxObject().getResponse().setRsStatRsStateCode(TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getINBKtxData().getTxChannel(), getLogContext()));

            getLogContext().setProgramName(ProgramName);
            getINBKtxData().getTxObject().getResponse().setRsStatDesc(TxHelper.getMessageFromFEPReturnCode(rtnCode, getLogContext()));
        }
    }
}
