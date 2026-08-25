package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.server.common.adapter.FEDIAdapter;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.rm.request.SyncFEDIRequest;
import com.syscom.fep.vo.text.rm.response.SyncFEDIResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class SyncFEDI extends RMAABase {

    private static final String programName = SyncFEDI.class.getSimpleName();
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private Rmout defRmout = new Rmout();
    private RmoutExtMapper rmoutMapper = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);

    public SyncFEDI(RMData txBrsData) throws Exception {
        super(txBrsData);
    }

    @Override
    public String processRequestData() throws Exception {
        String rtnMessage = "";
        try{
            //讀取匯款(匯入、匯出檔)資料
            _rtnCode = getRmData();

            if(CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = sendFedi();
            }

            rtnMessage = prepareRspData();

        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(rtnMessage);
            getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO, this.logContext);
        }

        return rtnMessage;
    }

    private FEPReturnCode getRmData(){
        defRmout.setRmoutTxdate(getmRMReq().getREMDATE());
        defRmout.setRmoutOriginal(getmRMReq().getORIGINAL());
        defRmout.setRmoutBrno(getmRMReq().getKINBR());
        defRmout.setRmoutFepno(getmRMReq().getFEPNO());
        defRmout = rmoutMapper.getSingleRMOUT(defRmout);
        if(defRmout ==null){
            getLogContext().setProgramName(StringUtils.join(programName,".processRequestData"));
            getLogContext().setRemark(StringUtils.join("已匯出財金中但匯款主檔無此資料",getmRMReq().getSENDBANK(),getmRMReq().getRCVBANK(),getmRMReq().getBRSNO()));
            sendEMS(getLogContext());
            defRmout.setRmoutStat("");
            return IOReturnCode.RMOUTNotFound;
        }
        return CommonReturnCode.Normal;
    }

    private FEPReturnCode sendFedi(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        SyncFEDIRequest syncFEDIReq =new SyncFEDIRequest();
        @SuppressWarnings("unused")
        SyncFEDIResponse syncFEDIRes =new SyncFEDIResponse();
        String serverIP = "";
        try{

            FEDIAdapter fediAdapter = new FEDIAdapter(getmTxBRSData());
            if(RMOUT_ORIGINAL.FEDI.equals(defRmout.getRmoutOriginal())){
                serverIP = CMNConfig.getInstance().getFEDIServiceUrl();
            }else{
                serverIP = CMNConfig.getInstance().getMMAB2BServiceUrl();
            }

            getmRMReq().setMsgID("SyncFEDI");
            getmRMReq().setTxnID("SyncFEDI");
            getmRMReq().setBranchID(StringUtils.join("8070",getmRMReq().getKINBR()));
            getmRMReq().setChlName("FEP");
            getmRMReq().setMsgType("R");
            getmRMReq().setChlEJNo(defRmout.getRmoutEjno1().toString());
            getmRMReq().setChlSendTime(defRmout.getRmoutSendtime());
            getmRMReq().setUserID("111");
            getmRMReq().setSignID("999");

            syncFEDIReq.setRqHeader(new FEPRqHeader());
            syncFEDIReq.setSvcRq(new SyncFEDIRequest.SyncFEDISvcRq());
            syncFEDIReq.getSvcRq().setRq(new SyncFEDIRequest.SyncFEDIRq());
            fediAdapter.setMessageToFedi(syncFEDIReq.makeMessageFromGeneral(getmTxBRSData().getTxObject()));
            fediAdapter.sendReceive(serverIP);
            if(CommonReturnCode.Normal.equals(rtnCode)){

            }


        } catch (Exception ex) {
            rtnCode = FEPReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
        }
        return rtnCode;
    }

    private String prepareRspData(){
        String rspStr ="";
        SyncFEDIResponse syncFEDIRes =new SyncFEDIResponse();
        getmRMRes().setFEPNO(StringUtils.leftPad(getmRMReq().getFEPNO(),7,"0"));
        getmRMBusiness().prepareResponseHeader(_rtnCode);
        rspStr = syncFEDIRes.makeMessageFromGeneral(getmTxBRSData().getTxObject());
        return rspStr;
    }
}
