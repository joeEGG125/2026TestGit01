package com.syscom.fep.server.aa.rm;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsginExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminExtMapper;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;

/**
 * 本支負責處理電文如下
 * @author ChenYang
 * @create 2021/9/28
 */
public class AA1512 extends RMAABase {

    private String rtnMessage = "";
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;

    private RminExtMapper rminMapper = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
    private MsginExtMapper msginMapper = SpringBeanFactoryUtil.getBean(MsginExtMapper.class);

    public AA1512(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     */
    /**
     * 程式進入點
     * @return Response電文
     */
    @Override
    public String processRequestData() throws Exception {
        String repMac = "";
        Integer wkRepk = 0;
        String wkRc = "";
        String repUnitBank = "";
        String wkStatus = "";
        getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        try{
            //1.檢核財金電文,若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
            _rtnCode = checkFISCRMReq();

            if(_rtnCode != CommonReturnCode.Normal){
                //Garble
                if("10".equals(getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode).substring(0,2))){
                    getmFISCBusiness().sendGarbledMessage(getmFISCRMReq().getEj(),_rtnCode,getmFISCRMReq());
                }
            }

            //2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN )
            if(_rtnCode == CommonReturnCode.Normal || PolyfillUtil.isNumeric(getmFISCRMReq().getTxnInitiateDateAndTime().substring(0,6))){
                _rtnCode = this.prepareAndInsertFEPTXN();
            }

            //3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
            RefString refRepMac = new RefString(repMac);
            RefInt refWkRepk = new RefInt(wkRepk);
            RefString refWkRc = new RefString(wkRc);
            RefString refRepUnitBank = new RefString(repUnitBank);
            RefString refWkStatus = new RefString(wkStatus);

            _rtnCode = checkBusinessRule(refRepMac,refWkRepk,refWkRc,refRepUnitBank,refWkStatus);

            repMac = refRepMac.get();
            wkRepk = refWkRepk.get();
            wkRc = refWkRc.get();
            repUnitBank = refRepUnitBank.get();
            wkStatus = refWkStatus.get();

            //4.準備回財金的相關資料
            _rtnCode = prepareForFISC(repMac, wkStatus);

            //5 UpdateTxData: 更新交易記錄(FepTxn)
            if(_rtnCode == CommonReturnCode.Normal){
                updateFEPTXN();
            }

            //6.送回覆電文到財金(SendToFISC)
            if(_rtnCode == CommonReturnCode.Normal){
                RefString refRtnMessage = new RefString(rtnMessage);
                getmFISCBusiness().sendRMResponseToFISC(refRtnMessage);
                rtnMessage = refRtnMessage.get();
            }

        }catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(logContext);
        } finally {
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            getLogContext().setMessage(rtnMessage);
            getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getLogContext().setMessageFlowType(MessageFlow.Response);
            logMessage(Level.INFO, this.logContext);
        }


        return "";
    }

    /**
     * 1.檢核財金Req電文
     * @return
     */
    public FEPReturnCode checkFISCRMReq(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try{
            //(2) 	檢核財金電文 Header
            rtnCode = getmFISCBusiness().checkRMHeader(true,MessageFlow.Request);
        }catch(Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 2.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
     * @return
     */
    public FEPReturnCode prepareAndInsertFEPTXN(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try{
            //2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
            rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(),"0");

            //新增交易記錄(FEPTXN )
            if(rtnCode == CommonReturnCode.Normal){
                rtnCode = getmFISCBusiness().insertFEPTxn();
            }
        }catch(Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    /**
     * 3.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
     * @param refRepMac
     * @param refWkRepk
     * @param refWkRc
     * @param refRepUnitBank
     * @param refWkStatus
     * @return
     */
    public FEPReturnCode checkBusinessRule(RefString refRepMac, RefInt refWkRepk, RefString refWkRc, RefString refRepUnitBank, RefString refWkStatus){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try{
            //2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
            rtnCode = getmFISCBusiness().checkBody("0",refRepMac,refWkRepk,refWkRc,refRepUnitBank);

            //新增交易記錄(FEPTXN )
            if(rtnCode == CommonReturnCode.Normal){
                rtnCode = checkINData(refWkStatus);
            }
        }catch(Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }
        return rtnCode;
    }

    public FEPReturnCode checkINData(RefString refWkStatus){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        Rmin defRmin = new Rmin();
        Msgin defMsgin = new Msgin();

        try{

            if("1112,1122,1132,1172,1182,1192".indexOf(getmFISCRMReq().getOrgPcode())>-1){
                defRmin.setRminTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                defRmin.setRminReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRmin.setRminFiscsno(getmFISCRMReq().getFiscNo());
                defRmin.setRminFiscRtnCode(NormalRC.FISC_OK);

                defRmin = rminMapper.getRMINByCheckINData(defRmin);

                if(defRmin!=null){
                    refWkStatus.set("01");
                }else{
                    refWkStatus.set("00");
                }
            }else{
                defMsgin.setMsginTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                defMsgin.setMsginReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defMsgin.setMsginFiscsno(getmFISCRMReq().getFiscNo());
                defMsgin.setMsginFiscRtnCode(NormalRC.FISC_OK);

                defMsgin = msginMapper.getMSGINByCheckINData(defMsgin);

                if(defMsgin!=null){
                    refWkStatus.set("01");
                }else{
                    refWkStatus.set("00");
                }
            }

        }catch(Exception ex){
            getLogContext().setProgramException(ex);
            sendEMS(getLogContext());
            return FEPReturnCode.ProgramException;
        }

        return rtnCode;
    }

    /**
     * 組送財金Request 電文
     * @param repMac
     * @param wkStatus
     * @return
     */
    public FEPReturnCode prepareForFISC(String repMac, String wkStatus){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        //(1) 判斷 RC
        getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        getFeptxn().setFeptxnRepRc(getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode));
        //(2) 產生 RESPONSE 電文訊息
        RefString refRepMac = new RefString(repMac);
        getmFISCBusiness().prepareResponseForRM(refRepMac,wkStatus);
        repMac = refRepMac.get();
        if(rtnCode!=CommonReturnCode.Normal){
            return rtnCode;
        }
        return rtnCode;
    }

    /**
     * 更新FEPTXN
     * @return
     */
    public FEPReturnCode updateFEPTXN(){
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        if(NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())){
            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
        }else{
            getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);
        }
        rtnCode = getmFISCBusiness().updateFepTxnForRM(getFeptxn());
        if(rtnCode!=CommonReturnCode.Normal){
            TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
            return rtnCode;
        }

        return rtnCode;
    }
}
