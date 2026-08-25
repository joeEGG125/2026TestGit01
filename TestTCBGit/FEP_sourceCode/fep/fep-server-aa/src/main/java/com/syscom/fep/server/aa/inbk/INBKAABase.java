package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.*;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.NpsunitExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.NpscustMapper;
import com.syscom.fep.mybatis.mapper.NpsunitMapper;
import com.syscom.fep.mybatis.model.Feptxntcb;
import com.syscom.fep.mybatis.model.Paytype;
import com.syscom.fep.server.common.business.atm.ATM;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.fcs.FCSGeneral;
import com.syscom.fep.vo.text.fcs.FCSGeneralRequest;
import com.syscom.fep.vo.text.fcs.FCSGeneralResponse;
import com.syscom.fep.vo.text.fisc.*;
import com.syscom.fep.vo.text.hce.HCEGeneral;
import com.syscom.fep.vo.text.hce.RCV_HCE_GeneralTrans_RQ;
import com.syscom.fep.vo.text.hce.SEND_HCE_GeneralTrans_RS;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.vo.text.inbk.INBKGeneralRequest;
import com.syscom.fep.vo.text.inbk.INBKGeneralResponse;
import com.syscom.fep.vo.text.ivr.IVRGeneral;
import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.ivr.SEND_VO_GeneralTrans_RS;
import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.mft.MFTGeneralRequest;
import com.syscom.fep.vo.text.nb.NBFEPGeneral;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_VA_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.VAFEPGeneral;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

public abstract class INBKAABase extends AABase {
    // FISC電文相關物件
    private FISCData mtxData; // 財金Request 資料物件
    private ATMData mATMtxData; // ATM Request 資料物件
    private INBKData mINBKtxData; // 分行Request 資料物件
    private FCSData mFCStxData; // 分行Request 資料物件
    private FISC_INBK mfiscReq; // 財金Request 電文處理物件
    private FISC_INBK mfiscRes; // 財金Response電文處理物件
    private FISC_INBK mfiscCon; // 財金Confirm 電文處理物件
    private FISC_OPC mfiscOPCReq; // OPC Request 電文處理物件
    private FISC_OPC mfiscOPCRes; // OPC Response電文處理物件
    private FISC_OPC mfiscOPCCon; // OPC Confirm 電文處理物件
    private FISC_CLR mfiscCLRReq; // CLR Request 電文處理物件
    private FISC_CLR mfiscCLRRes; // CLR Response電文處理物件
    private FISC_USDCLR mfiscFCCLRReq; // FCCLR Request 電文處理物件
    private FISC_USDCLR mfiscFCCLRRes; // FCCLR Response電文處理物件
    private FISC_EMVIC mfiscEMVICReq; // EMVIC Request 電文處理物件
    private FISC_EMVIC mfiscEMVICRes; // EMVIC Response電文處理物件
    private FISC_EMVIC mfiscEMVICCon; // EMVIC Confirm 電文處理物件
    private ATMGeneralRequest mATMReq; // ATM Request 電文處理物件
    private ATMGeneralResponse mATMRes; // ATM Response電文處理物件
    private INBKGeneralRequest mINBKReq; // 分行 Request 電文處理物件
    private INBKGeneralResponse mINBKRes; // 分行 Response電文處理物件
    private FCSGeneralRequest mFCSReq; // 分行 Response電文處理物件
    private FCSGeneralResponse mFCSRes; // 分行 Response電文處理物件
    private FISC mfiscBusiness; // FISC 邏輯處理物件
    private ATM mATMBusiness; // ATM 邏輯處理物件
    private FISCGeneral mfiscGeneral; // ATM 電文通用物件
    private ATMGeneral mATMGeneral; // ATM 電文通用物件
    private INBKGeneral mINBKGeneral; // 分行電文通用物件
    private FCSGeneral mFCSGeneral; // FCS電文通用物件
    private ATMTXCD txCode; // ATM交易代號
    private RCV_VO_GeneralTrans_RQ mNBVOReq; // IRV Request 電文處理物件
    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    private HCEData mHCEtxData; // HCE資料物件
    private IVRData mIVRtxData; // IVR資料物件

    private NBData nBData;
    private MFTData mftData;
    private NBFEPData mNBFEPData; // HCE資料物件

    private HCEGeneral mHCEGeneral; // HCE電文通用物件

    private IVRGeneral mIVRGeneral;
    private RCV_HCE_GeneralTrans_RQ mHCEReq;
    private SEND_HCE_GeneralTrans_RS mHCERes;
    private RCV_VO_GeneralTrans_RQ mIVRReq;
    private SEND_VO_GeneralTrans_RS mIVRRes;
    private Paytype paytype; // add by henny 20100902
    public static final String RRN30000Trans = "188888889999";
    public static final String PAYTYPE30000Trans = "59999";
    private NpsunitMapper npsunitMapper = SpringBeanFactoryUtil.getBean(NpsunitMapper.class);
    private NpscustMapper npscustMapper = SpringBeanFactoryUtil.getBean(NpscustMapper.class);
    private NpsunitExtMapper dbNPSUNIT = SpringBeanFactoryUtil.getBean(NpsunitExtMapper.class);
    private BigDecimal charge;
    private String brch;
    private String chargeFlag;

    private NBFEPData mNBFEPtxData;
    private NBFEPGeneral mNBFEPGeneral;
    private MFTGeneral mftGeneral;
    private VAFEPGeneral mVAFEPGeneral;
    private RCV_NB_GeneralTrans_RQ mNBReq;
    private MFTGeneralRequest mftGeneralRequest;
    private RCV_VA_GeneralTrans_RQ mVAReq;
    private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);

    public INBKAABase(FISCData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mtxData = txnData;
        this.logContext = this.mtxData.getLogContext();
        this.ej = txnData.getEj();

        this.mfiscGeneral = this.mtxData.getTxObject();
        this.mfiscGeneral.setEJ(this.ej);
        this.mfiscReq = mtxData.getTxObject().getINBKRequest();
        this.mfiscRes = mtxData.getTxObject().getINBKResponse();
        this.mfiscCon = mtxData.getTxObject().getINBKConfirm();
        this.mfiscOPCReq = mtxData.getTxObject().getOPCRequest();
        this.mfiscOPCRes = mtxData.getTxObject().getOPCResponse();
        this.mfiscOPCCon = mtxData.getTxObject().getOPCConfirm();
        this.mfiscCLRReq = mtxData.getTxObject().getCLRRequest();
        this.mfiscCLRRes = mtxData.getTxObject().getCLRResponse();
        this.mfiscFCCLRReq = mtxData.getTxObject().getFCCLRRequest();
        this.mfiscFCCLRRes = mtxData.getTxObject().getFCCLRResponse();
        // 2016/04/21 Modify by Ruling for EMV晶片卡原存交易
        this.mfiscEMVICReq = mtxData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = mtxData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = mtxData.getTxObject().getEMVICConfirm();

        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        // 2011-05-18 by kyo for 抓Cache不同步的BUG，抓到後可以刪除

        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(FISCData)"));

        this.mtxData.setFeptxn(this.feptxn);
        this.mtxData.setFeptxntcb(this.feptxntcb);
        this.mtxData.setFeptxnDao(this.feptxnDao);
        this.mtxData.setEj(this.ej);

        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mtxData.getAaName());
        logMessage(Level.DEBUG, this.logContext);

        // 建立FISC Business物件
        this.mfiscBusiness = new FISC(this.mtxData);
    }

    public INBKAABase(ATMData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mATMtxData = txnData;
        this.setLogContext(mATMtxData.getLogContext());
        this.setEj(mATMtxData.getEj());

        // 將ATMData中的Request及Response物件取出存放在內部變數中
        this.mATMGeneral = mATMtxData.getTxObject();
        this.mATMReq = mATMtxData.getTxObject().getRequest();
        this.mATMRes = mATMtxData.getTxObject().getResponse();
        //--ben-20220922-//this.txCode = ATMTXCD.parse(this.mATMReq.getTXCD());

        // 寫文字檔LOG
        //--ben-20220922-//getLogContext().setTxDate(this.mATMReq.getAtmseq_1());
        //--ben-20220922-//getLogContext().setTrinBank(this.mATMReq.getBknoD());
        //--ben-20220922-//getLogContext().setTrinActno(this.mATMReq.getActD());
        //--ben-20220922-//getLogContext().setTroutBank(this.mATMReq.getBKNO());
        //--ben-20220922-//getLogContext().setTroutActno(this.mATMReq.getTXACT());
        //--ben-20220922-//getLogContext().setChact(this.mATMReq.getCHACT());
        getLogContext().setProgramFlowType(ProgramFlow.AAIn);
        getLogContext().setProgramName(this.mATMtxData.getAaName());
        logMessage(Level.DEBUG, this.logContext);

        // 準備FEPTxn相關物件
        // 2014/12/27 Modify by Ruling for 避免寫入換日前的FEPTXN，但FEPTXN_TBSDY_FISC卻為換日後，導致GL沒抓到該筆交易而帳務不合的問題
        this.mATMtxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(this.mATMtxData.getTbsdyFISC().substring(6, 8), StringUtils.join(ProgramName, "(ATMData)"));
        this.feptxn.setFeptxnEjfno(this.getEj());
        this.mATMtxData.setFeptxn(this.feptxn);
        this.mATMtxData.setFeptxntcb(this.feptxntcb);
        this.mATMtxData.setFeptxnDao(this.feptxnDao);
        this.mATMtxData.setEj(this.getEj());

        // 建立ATM Business物件,傳入ATMData物件,將EJ,Feptxn及DBFeptxn也帶給Business
        if(mATMtxData.getTxChannel().toString().equals("EAT")){
            this.mATMBusiness = new ATM(this.mATMtxData, "eatm");
        } else if (mATMtxData.getTxChannel().toString().equals("POS")){
            this.mATMBusiness = new ATM(this.mATMtxData, mATMtxData.getTxChannel().toString());
        }else {
            this.mATMBusiness = new ATM(this.mATMtxData);
        }

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
        fData.setTxChannel(FEPChannel.ATM);
        fData.setTxSubSystem(SubSystem.INBK);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(this.mATMtxData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(this.mATMtxData.getMsgCtl());
        fData.setEj(this.mATMtxData.getEj());
        fData.setTxStatus(this.mATMtxData.isTxStatus());
        fData.setTxAccount(this.mATMtxData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        this.mfiscBusiness = new FISC(fData);

        // ATMP的AA在New時就先UpdateATMStatus
        this.mATMBusiness.updateATMStatus();
    }

    /**
     * Fly 2019/09/17 VAA2566允許其他Channel進來
     *
     * @param txnData
     * @param api
     * @throws Exception
     */
    public INBKAABase(ATMData txnData, String api) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mATMtxData = txnData;
        this.logContext = this.mATMtxData.getLogContext();
        this.ej = this.mATMtxData.getEj();

        this.mATMGeneral = this.mATMtxData.getTxObject();
        this.mATMReq = this.mATMtxData.getTxObject().getRequest();
        this.mATMRes = this.mATMtxData.getTxObject().getResponse();
        //--ben-20220922-//this.txCode = ATMTXCD.parse(mATMReq.getTXCD());

        //--ben-20220922-//this.logContext.setTxDate(this.mATMReq.getAtmseq_1());
        //--ben-20220922-//this.logContext.setTrinBank(this.mATMReq.getBknoD());
        //--ben-20220922-//this.logContext.setTrinActno(this.mATMReq.getActD());
        //--ben-20220922-//this.logContext.setTroutBank(this.mATMReq.getBKNO());
        //--ben-20220922-//this.logContext.setTroutActno(this.mATMReq.getTXACT());
        //--ben-20220922-//this.logContext.setChact(this.mATMReq.getCHACT());
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mATMtxData.getAaName());
        logMessage(Level.DEBUG, this.logContext);
        this.feptxntcb = new Feptxntcb();

        this.mATMtxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
        this.feptxn = new FeptxnExt();
        this.feptxnDao.setTableNameSuffix(this.mATMtxData.getTbsdyFISC().substring(6, 8), StringUtils.join(ProgramName, "(ATMData,String)"));
        this.feptxn.setFeptxnEjfno(this.ej);
        this.mATMtxData.setFeptxn(this.feptxn);
        this.mATMtxData.setFeptxntcb(this.feptxntcb);
        this.mATMtxData.setFeptxnDao(this.feptxnDao);
        this.mATMtxData.setEj(this.getEj());
        this.mATMBusiness = new ATM(this.mATMtxData, api);

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
        fData.setTxChannel(FEPChannel.ATM);
        fData.setTxSubSystem(SubSystem.INBK);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(this.mATMtxData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(this.mATMtxData.getMsgCtl());
        fData.setEj(this.mATMtxData.getEj());
        fData.setTxStatus(this.mATMtxData.isTxStatus());
        fData.setTxAccount(this.mATMtxData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        this.mfiscBusiness = new FISC(fData);
    }

    public INBKAABase(INBKData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mINBKtxData = txnData;
        this.logContext = this.mINBKtxData.getLogContext();
        this.ej = this.mINBKtxData.getEj();

        this.mINBKGeneral = this.mINBKtxData.getTxObject();
        this.mINBKReq = this.mINBKtxData.getTxObject().getRequest();
        this.mINBKRes = this.mINBKtxData.getTxObject().getResponse();

        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(INBKData)"));

        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mINBKtxData.getAaName());
        logMessage(Level.DEBUG, this.logContext);

        // 建立FISC Business物件
        this.mtxData = new FISCData();
        this.mtxData.setFeptxn(this.feptxn);
        this.mtxData.setFeptxntcb(this.feptxntcb);
        this.mtxData.setFeptxnDao(this.feptxnDao);
        this.mtxData.setFiscTeleType(FISCSubSystem.CLR);
        this.mtxData.setMessageID(this.mINBKtxData.getMessageID());
        this.mtxData.setMsgCtl(this.mINBKtxData.getMsgCtl());
        this.mtxData.setLogContext(this.mINBKtxData.getLogContext());
        this.mtxData.setEj(this.mINBKtxData.getEj());
        this.mtxData.setTxObject(new FISCGeneral());
        this.mtxData.getTxObject().setCLRRequest(new FISC_CLR());
        this.mtxData.getTxObject().setCLRResponse(new FISC_CLR());
        this.mfiscCLRReq = this.mtxData.getTxObject().getCLRRequest();
        this.mfiscCLRRes = this.mtxData.getTxObject().getCLRResponse();
        this.mfiscBusiness = new FISC(this.mtxData);
        // mfiscBusiness.FepTxn = FepTxn
        // mfiscBusiness.DBFepTxn = DBFepTxn
        // mfiscBusiness.LogContext = LogContext

    }
    public INBKAABase(INBKData txnData,String inbk) throws Exception {
        super();
        this.mINBKtxData = txnData;
        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(INBKData)"));

        // 建立FISC Business物件
        this.mtxData = new FISCData();
        this.mtxData.setFeptxn(this.feptxn);
        this.mtxData.setFeptxntcb(this.feptxntcb);
        this.mtxData.setFeptxnDao(this.feptxnDao);
        this.mtxData.setFiscTeleType(FISCSubSystem.INBK);
        this.mtxData.setMessageID(this.mINBKtxData.getMessageID());
        this.mtxData.setTxSubSystem(this.mINBKtxData.getTxSubSystem());
        this.mtxData.setTxStatus(this.mINBKtxData.isTxStatus());
        this.mtxData.setTxChannel(this.mINBKtxData.getTxChannel());
        this.mtxData.setMsgCtl(this.mINBKtxData.getMsgCtl());
        this.mtxData.setLogContext(this.mINBKtxData.getLogContext());
        this.mtxData.setEj(this.mINBKtxData.getEj());
        this.mtxData.setTxObject(new FISCGeneral());
        this.mfiscBusiness = new FISC(this.mtxData);
    }

    public INBKAABase(MFTData txnData) throws Exception {
        this.aaName = txnData.getAaName();
        this.mftData = txnData;
        this.logContext = this.mftData.getLogContext();
        this.ej = this.mftData.getEj();

        this.mftGeneral = this.mftData.getTxMFTObject();
        this.mftGeneralRequest = this.mftData.getTxMFTObject().getRequest();


        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(NBFEPData)"));
        this.feptxn.setFeptxnEjfno(this.ej);
        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mftData.getAaName());
        logMessage(Level.DEBUG, this.logContext);

        this.mftData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());

        txnData.setFeptxn(feptxn);

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
//        fData.setTxChannel(FEPChannel.MFT);
        fData.setTxSubSystem(SubSystem.MFT);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(mftData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(mftData.getMsgCtl());
        fData.setEj(mftData.getEj());
        fData.setTxStatus(mftData.isTxStatus());
        fData.setTxAccount(mftData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        // 建立FISC Business物件
        this.mfiscBusiness = new FISC(this.mtxData);
        this.mfiscBusiness.setMftData(txnData);
    }

    public INBKAABase(HCEData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mHCEtxData = txnData;
        this.logContext = this.mHCEtxData.getLogContext();
        this.mATMGeneral = this.mHCEtxData.getAtmtxObject();
        this.mATMReq = mHCEtxData.getAtmtxObject().getRequest();
        this.mATMRes = mHCEtxData.getAtmtxObject().getResponse();
        this.ej = this.mHCEtxData.getEj();
        this.mHCEGeneral = this.mHCEtxData.getTxObject();
        this.mHCEReq = this.mHCEtxData.getTxObject().getRequest();
        this.mHCERes = this.mHCEtxData.getTxObject().getResponse();
        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(HCEData)"));
        this.feptxn.setFeptxnEjfno(this.ej);
        this.mHCEtxData.setFeptxnDao(this.feptxnDao);
        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mHCEtxData.getAaName());
        txnData.setFeptxn(feptxn);
        txnData.setFeptxntcb(feptxntcb);
        txnData.setFeptxnDao(feptxnDao);
        logMessage(Level.DEBUG, this.logContext);

        this.mHCEtxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
        setFeptxn(feptxn);

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
        fData.setTxChannel(FEPChannel.HCA);
        fData.setTxSubSystem(SubSystem.INBK);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(txnData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(txnData.getMsgCtl());
        fData.setEj(txnData.getEj());
        fData.setTxStatus(txnData.isTxStatus());
        fData.setTxAccount(txnData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        // 建立FISC Business物件
        this.mfiscBusiness = new FISC(this.mtxData);
        this.mfiscBusiness.setmHCEtxData(txnData);
    }

    public INBKAABase(IVRData ivrData) throws Exception {
        super();
        this.aaName = ivrData.getAaName();
        this.mIVRtxData = ivrData;
        this.logContext = this.mIVRtxData.getLogContext();
        this.mATMReq = mIVRtxData.getAtmtxObject().getRequest();
        this.mATMRes = mIVRtxData.getAtmtxObject().getResponse();
        this.ej = this.mIVRtxData.getEj();
        this.mIVRGeneral = this.mIVRtxData.getTxObject();
        this.mIVRReq = this.mIVRtxData.getTxObject().getRequest();
        this.mIVRRes = this.mIVRtxData.getTxObject().getResponse();
        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(IVRData)"));
        this.feptxn.setFeptxnEjfno(this.ej);
        this.mIVRtxData.setFeptxnDao(this.feptxnDao);
        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.mIVRtxData.getAaName());
        ivrData.setFeptxn(feptxn);
        ivrData.setFeptxnDao(feptxnDao);
        logMessage(Level.DEBUG, this.logContext);

        this.mIVRtxData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());
        setFeptxn(feptxn);

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
        fData.setTxChannel(FEPChannel.IVR);
        fData.setTxSubSystem(SubSystem.INBK);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(ivrData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(ivrData.getMsgCtl());
        fData.setEj(ivrData.getEj());
        fData.setTxStatus(ivrData.isTxStatus());
        fData.setTxAccount(ivrData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        // 建立FISC Business物件
        this.mfiscBusiness = new FISC(this.mtxData);
        this.mfiscBusiness.setmIVRtxData(ivrData);
    }


    public INBKAABase(NBData txnData) throws Exception {
        this.aaName = txnData.getAaName();
        this.nBData = txnData;
        this.logContext = this.nBData.getLogContext();
        this.ej = this.nBData.getEj();

        //Aster調整需求 多了一個va
        if (this.nBData.getTxNbfepObject() != null) {
            this.mNBFEPGeneral = this.nBData.getTxNbfepObject();
            this.mNBReq = this.nBData.getTxNbfepObject().getRequest();
        } else {
            this.mVAFEPGeneral = this.nBData.getTxVafepObject();
            this.mVAReq = this.nBData.getTxVafepObject().getRequest();
        }

        this.mATMReq = nBData.getAtmtxObject().getRequest();

        // 準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        this.feptxntcb = new Feptxntcb();
        this.feptxnDao.setTableNameSuffix(SysStatus.getPropertyValue().getSysstatTbsdyFisc().substring(6, 8), StringUtils.join(ProgramName, "(NBFEPData)"));
        this.feptxn.setFeptxnEjfno(this.ej);
        this.nBData.setFeptxnDao(this.feptxnDao);
        // 寫文字檔LOG
        this.logContext.setProgramFlowType(ProgramFlow.AAIn);
        this.logContext.setProgramName(this.nBData.getAaName());
        logMessage(Level.DEBUG, this.logContext);

        this.nBData.setTbsdyFISC(SysStatus.getPropertyValue().getSysstatTbsdyFisc());

        txnData.setFeptxn(feptxn);
        txnData.setFeptxntcb(feptxntcb);

        FISCData fData = new FISCData();
        FISCGeneral general = new FISCGeneral();

        fData.setTxObject(general);
        fData.setTxChannel(FEPChannel.NETBANK);
        fData.setTxSubSystem(SubSystem.INBK);
        fData.setFiscTeleType(FISCSubSystem.INBK);
        fData.setTxRequestMessage(StringUtils.EMPTY);
        fData.setEj(this.getEj());
        fData.setLogContext(this.getLogContext());
        fData.getTxObject().setINBKRequest(new FISC_INBK());
        fData.getTxObject().setINBKResponse(new FISC_INBK());
        fData.getTxObject().setINBKConfirm(new FISC_INBK());
        fData.getTxObject().setEMVICRequest(new FISC_EMVIC());
        fData.getTxObject().setEMVICResponse(new FISC_EMVIC());
        fData.getTxObject().setEMVICConfirm(new FISC_EMVIC());

        fData.setMessageID(nBData.getMsgCtl().getMsgctlMsgid());
        fData.setMsgCtl(nBData.getMsgCtl());
        fData.setEj(nBData.getEj());
        fData.setTxStatus(nBData.isTxStatus());
        fData.setTxAccount(nBData.getTxAccount());

        this.mtxData = fData;
        this.mfiscGeneral = fData.getTxObject();
        this.mfiscGeneral.setEJ(this.getEj());
        this.mfiscReq = fData.getTxObject().getINBKRequest();
        this.mfiscRes = fData.getTxObject().getINBKResponse();
        this.mfiscCon = fData.getTxObject().getINBKConfirm();
        this.mfiscEMVICReq = fData.getTxObject().getEMVICRequest();
        this.mfiscEMVICRes = fData.getTxObject().getEMVICResponse();
        this.mfiscEMVICCon = fData.getTxObject().getEMVICConfirm();

        fData.setFeptxn(this.feptxn);
        fData.setFeptxntcb(this.feptxntcb);
        fData.setFeptxnDao(this.feptxnDao);

        // 建立FISC Business物件
        this.mfiscBusiness = new FISC(this.mtxData);
        this.mfiscBusiness.setmNBtxData(txnData);
    }

    public FISC_EMVIC getMfiscEMVICRes() {
        return mfiscEMVICRes;
    }

    protected ATM getATMBusiness() {
        return mATMBusiness;
    }

    protected FISC getFiscBusiness() {
        return mfiscBusiness;
    }

    public ATMGeneralRequest getATMRequest() {
        return mATMReq;
    }

    public ATMGeneralResponse getATMResponse() {
        return mATMRes;
    }

    public INBKGeneralRequest getINBKRequest() {
        return mINBKReq;
    }

    public INBKGeneralResponse getINBKResponse() {
        return mINBKRes;
    }

    public FISC_INBK getFiscReq() {
        return mfiscReq;
    }

    public FISC_INBK getFiscRes() {
        return mfiscRes;
    }

    public void setFiscRes(FISC_INBK fiscRes) {
        this.mfiscRes = fiscRes;
    }

    public FISC_INBK getFiscCon() {
        return mfiscCon;
    }

    public FISC_OPC getFiscOPCReq() {
        return mfiscOPCReq;
    }

    public FISC_OPC getFiscOPCRes() {
        return mfiscOPCRes;
    }

    public FISC_OPC getFiscOPCCon() {
        return mfiscOPCCon;
    }

    public FISC_CLR getFiscCLRReq() {
        return mfiscCLRReq;
    }

    public FISC_CLR getFiscCLRRes() {
        return mfiscCLRRes;
    }

    public FISC_USDCLR getFiscFCCLRReq() {
        return mfiscFCCLRReq;
    }

    public FISC_USDCLR getFiscFCCLRRes() {
        return mfiscFCCLRRes;
    }

    public FISC_EMVIC getFiscEMVICReq() {
        return mfiscEMVICReq;
    }

    public FISC_EMVIC getFiscEMVICRes() {
        return mfiscEMVICRes;
    }

    public FISC_EMVIC getFiscEMVICCon() {
        return mfiscEMVICCon;
    }

    public FCSGeneralRequest getFCSReq() {
        return mFCSReq;
    }

    public FCSGeneralResponse getFCSRes() {
        return mFCSRes;
    }

    public ATMData getATMtxData() {
        return mATMtxData;
    }

    public FISCData getTxData() {
        return mtxData;
    }

    public INBKData getINBKtxData() {
        return mINBKtxData;
    }

    public FCSData getFCStxData() {
        return mFCStxData;
    }

    public ATMGeneral getATMGeneral() {
        return mATMGeneral;
    }

    public FISCGeneral getFISCGeneral() {
        return mfiscGeneral;
    }

    public INBKGeneral getINBKGeneral() {
        return mINBKGeneral;
    }

    public FCSGeneral getFCSGeneral() {
        return mFCSGeneral;
    }

    /**
     * 組ATM回應電文
     * <p>
     * <p>
     * 組ATM回應電文,Response物件的值已經在AA中MapGeneralResponseFromGeneralRequest搬好Header的值了
     * 這裏只要處理Response的body的欄位值即可
     *
     * <history>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>把手續費移到LoadCommonATMResponse()執行</reason>
     * <date>2010/11/23</date>
     * <reason>新增組回應電文給NB</reason>
     * <date>2010/12/02</date>
     * <reason>組回應電文NB move to nbhandler,在這裡搬所需欄位(RsHeader)</reason>
     * <date>2010/12/06</date>
     * <reason>重新抓一次header放到response中防止沒同步 呼叫mATMBusiness.MapResponseFromRequest</reason>
     * <date>2010/12/08</date>
     * <reason>將Feptnx_Channel轉型成Enum FEPChannel</reason>
     * <date>2011/1/04</date>
     * <reason>由於rtncode已經在SelfIssueRequestA的SendToConfirm作通道轉換過所以不需要轉換,</reason>
     * <reason>同時訊息已經存在LogContext裡面 </reason>
     * <date>2011/1/05</date>
     * <reason>補上銀聯卡提款,銀聯卡查詢 </reason>
     * <date>2011/1/21</date>
     * <reason>LoadCommonATMResponse 裡面已經把IVR,NETBANK channel需要的TOTA資訊 組好 </reason>
     * <date>2011/1/26</date>
     * </modify>
     * </history>
     */
    public String prepareATMResponseData() {
        //ben20221118
        return "";
    }


    /**
     * 準備吐鈔相關回應電文
     * <p>
     * <p>
     * IIQ,IFT,IWD 共同的部分
     */
    private void loadATMResponseForCash() {
        //--ben-20220922-//getATMResponse().setDSPCNT1(getATMRequest().getDSPCNT1());
        //--ben-20220922-//getATMResponse().setDSPCNT2(getATMRequest().getDSPCNT2());
        //--ben-20220922-//getATMResponse().setDSPCNT3(getATMRequest().getDSPCNT3());
        //--ben-20220922-//getATMResponse().setDSPCNT4(getATMRequest().getDSPCNT4());
        //--ben-20220922-//getATMResponse().setDSPCNT5(getATMRequest().getDSPCNT5());
        //--ben-20220922-//getATMResponse().setDSPCNT6(getATMRequest().getDSPCNT6());
        //--ben-20220922-//getATMResponse().setDSPCNT7(getATMRequest().getDSPCNT7());
        //--ben-20220922-//getATMResponse().setDSPCNT8(getATMRequest().getDSPCNT8());
        //--ben-20220922-//getATMResponse().setDSPCNT1T(getATMRequest().getDSPCNT1T());
        //--ben-20220922-//getATMResponse().setDSPCNT2T(getATMRequest().getDSPCNT2T());
        //--ben-20220922-//getATMResponse().setDSPCNT3T(getATMRequest().getDSPCNT3T());
        //--ben-20220922-//getATMResponse().setDSPCNT4T(getATMRequest().getDSPCNT4T());
    }

    /**
     * 負責檢核 ATM 跨行交易通知電文
     *
     * @param txnData ATMData
     *
     *                <history>
     *                <modify>
     *                <modifier>Henny</modifier>
     *                <reason></reason>
     *                <date>2010/4/13</date>
     *                <modifier>Husan</modifier>
     *                <reason>modify call constructor of ENCHelper</reason>
     *                <date>2011/2/18</date>
     *                </modify>
     *                </history>
     * @return
     **/
    public FEPReturnCode checkRequestFromATM(ATMData txnData) {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        try {
            rtnCode = mATMBusiness.CheckATMData();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //檢核財金及本行之系統連線狀態
            rtnCode = mfiscBusiness.checkINBKStatus(getFeptxn().getFeptxnPcode(), true);
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //檢核委託單位代號或繳款類別
            if (StringUtils.isNotBlank(getFeptxn().getFeptxnBusinessUnit())) {
                rtnCode = mfiscBusiness.checkNpsunit(getFeptxn());
                if (rtnCode != CommonReturnCode.Normal) {
                    return rtnCode;
                }
            }

            // 檢核單筆限額
            if (SysStatus.getPropertyValue().getSysstatHbkno().equals(getFeptxn().getFeptxnTroutBkno())) {
                if (txnData.getMsgCtl().getMsgctlCheckLimit() != 0) {
                    rtnCode = mATMBusiness.checkTransLimit(txnData.getMsgCtl());
                    if (rtnCode != CommonReturnCode.Normal) {
                        return rtnCode;
                    }
                }
            }
            return rtnCode;
        } catch (Exception ex) {
            if (mtxData.getMessageFlowType() == MessageFlow.Request) {
                getLogContext().setMessageFlowType(MessageFlow.Request);
            } else {
                getLogContext().setMessageFlowType(MessageFlow.Response);
            }
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + "checkRequestFromATM");
            getLogContext().setMessage(ex.getMessage());
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    /**
     * 組ATM回應電文(共同)
     * <p>
     * 回應電文共同的部分
     * <history>
     * <modify>
     * <modifier>Husan</modifier>
     * <reason>Connie spec change modify for 外圍系統</reason>
     * <date>2011/1/26</date>
     * <reason>modfiy call constructor of ENCHelper </reason>
     * <date>2011/2/18</date>
     * </modify>
     * </history>
     */

    private void loadCommonATMResponse() throws DecoderException {
        //ben20221118
    }

    public RCV_VO_GeneralTrans_RQ getmNBVOReq() {
        return mNBVOReq;
    }

    public void setmNBVOReq(RCV_VO_GeneralTrans_RQ mNBVOReq) {
        this.mNBVOReq = mNBVOReq;
    }

    public RCV_VO_GeneralTrans_RQ getmIVRReq() {
        return mIVRReq;
    }

    public void setmIVRReq(com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ mIVRReq) {
        this.mIVRReq = mIVRReq;
    }

    public RCV_HCE_GeneralTrans_RQ getmHCEReq() {
        return mHCEReq;
    }

    public void setmHCEReq(RCV_HCE_GeneralTrans_RQ mHCEReq) {
        this.mHCEReq = mHCEReq;
    }

    public RCV_NB_GeneralTrans_RQ getmNBReq() {
        return mNBReq;
    }

    public void setmNBReq(RCV_NB_GeneralTrans_RQ mNBReq) {
        this.mNBReq = mNBReq;
    }

    public HCEData getmHCEtxData() {
        return mHCEtxData;
    }

    public void setmHCEtxData(HCEData mHCEtxData) {
        this.mHCEtxData = mHCEtxData;
    }

    public Paytype getPaytype() {
        return paytype;
    }

    public void setPaytype(Paytype paytype) {
        this.paytype = paytype;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public String getBrch() {
        return brch;
    }

    public void setBrch(String brch) {
        this.brch = brch;
    }

    public String getChargeFlag() {
        return chargeFlag;
    }

    public void setChargeFlag(String chargeFlag) {
        this.chargeFlag = chargeFlag;
    }

    public NBData getnBData() {
        return nBData;
    }

    public IVRData getIVRData() {
        return mIVRtxData;
    }
    public void setnBData(NBData nBData) {
        this.nBData = nBData;
    }

    public RCV_VA_GeneralTrans_RQ getmVAReq() {
        return mVAReq;
    }

    public void setmVAReq(RCV_VA_GeneralTrans_RQ mVAReq) {
        this.mVAReq = mVAReq;
    }
}
