package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.slf4j.event.Level;

/**
 * 1 Way
 * 財金公司通知參加單位，財金公司已由系統故障狀態恢復，可再繼續提供跨行作業
 * 本支負責處理電文如下
 * REQUEST   ：OC041
 */
public class AA3207 extends INBKAABase {

    /**
     * 共用變數宣告
     */
    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    private SysstatExtMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);

    /**
     * "建構式"
     *  AA的建構式,在這邊初始化及設定其他相關變數
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception e
     */
    public AA3207(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     * 程式進入點
     * @return Response電文
     * @throws Exception e
     */
    @Override
    public String processRequestData() throws Exception {
        try {
            //1.拆解並檢核財金發動的Request電文Header
            _rtnCode = getFiscBusiness().checkHeader(getFiscOPCReq(),false);
            //2.判斷是否為Garbled Message
            if (CommonReturnCode.Normal!=_rtnCode){
                getLogContext().setProgramName(ProgramName);
                //Garble
                if ("10".equals(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC,getLogContext()).substring(0,2))){
                    getFiscBusiness().sendGarbledMessage(getFiscOPCReq().getEj(),_rtnCode,getFiscOPCReq());
                    return "";
                }
            }
            //3.準備交易記錄檔＆新增交易記錄檔
            _rtnCode = prepareAndInsertFEPTXN();
            //4.檢核財金Request電文(OC041)
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = checkRequest();
            }
            //5.更新SYSSTAT
            if (CommonReturnCode.Normal.equals(_rtnCode)){
                _rtnCode = doBusiness();
            }
            //6.更新交易記錄檔
            _rtnCode = updateFEPTXN();
        } catch (Exception ex) {
            _rtnCode = CommonReturnCode.ProgramException;
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
        } finally {
            logContext.setProgramFlowType(ProgramFlow.AAOut);
            logContext.setMessage(rtnMessage);
            logContext.setProgramName(this.aaName);
            logContext.setMessageFlowType(MessageFlow.Request);
            logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode,getLogContext()));
            logContext.setProgramName(ProgramName);
            logMessage(Level.DEBUG,logContext);
        }
        return "";
    }

    /**
     * 3.準備交易記錄檔＆新增交易記錄檔
     * 準備交易記錄檔＆新增交易記錄檔
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareAndInsertFEPTXN(){
        FEPReturnCode rtnCode;

        rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
        if (!CommonReturnCode.Normal.equals(rtnCode)){
            return rtnCode;
        }else {
            rtnCode = getFiscBusiness().insertFEPTxn();
            if (CommonReturnCode.Normal.equals(rtnCode)){
                return _rtnCode;
            }else {
                return rtnCode;
            }
        }
    }

    /**
     * 4.檢核財金Request電文(OC041)
     * 檢核財金Request電文(OC041)
     * @return FEPReturnCode
     */
    private FEPReturnCode checkRequest(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        //MAC
        rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(),getFiscOPCReq().getMessageType(),getFiscOPCReq().getMAC());
        if(!CommonReturnCode.Normal.equals(rtnCode)){
            return rtnCode;
        }
        return rtnCode;
    }

    /**
     * 5.更新SYSSTAT
     * 檢核財金Request電文(OC041)
     * @return FEPReturnCode
     * @throws Exception ex
     */
    private FEPReturnCode doBusiness() throws Exception {
        Sysstat defSYSSTAT = new Sysstat();
        //modify 新增DBSYSSTAT的參數  20110412 by Husan
        defSYSSTAT.setLogAuditTrail(true);
        //dbSYSSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name
        SysStatus.getPropertyValue().setSysstatSoct("1");
        if (dbSYSSTAT.updateByPrimaryKey(SysStatus.getPropertyValue())<=0){
            return IOReturnCode.SYSSTATUpdateError;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 6.更新交易記錄檔
     * @return FEPReturnCode
     */
    private FEPReturnCode updateFEPTXN(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());

            rtnCode = getFiscBusiness().updateTxData();
            if (!CommonReturnCode.Normal.equals(rtnCode)){
                return rtnCode;
            }else {
                return _rtnCode;
            }
        }catch (Exception ex){
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }
}
