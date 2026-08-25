package com.syscom.fep.server.aa.rm;

import com.syscom.fep.base.aa.AABase;
import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.aa.T24Data;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.vo.constant.RMTXCode;
import com.syscom.fep.vo.text.FEPMessage;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.vo.text.fisc.FISC_USDRM;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.syscom.fep.vo.text.rm.RMGeneralRequest;
import com.syscom.fep.vo.text.rm.RMGeneralResponse;
import com.syscom.fep.vo.text.t24.T24PreClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class RMAABase extends AABase {

    //RM AA資料物件
    private RMData mTxBRSData;
    //FISC AA資料物件
    private FISCData mTxFISCData;
    //T24 AA資料物件
    private T24Data mTxT24Data;

    private FISC mFISCBusiness;
    private RM mRMBusiness;

    //RM BRS 電文通用物件
    private RMGeneral mRMGeneral;
    //RM BRS Request電文
    private RMGeneralRequest mRMReq;
    //RM BRS Response電文
    private RMGeneralResponse mRMRes;
    //RM FISC 電文通用物件
    private FISCGeneral mFISCGeneral;
    //RM FISC Request電文
    private FISC_RM mFISCRMReq;
    //RM FISC Response電文
    private FISC_RM mFISCRMRes;
    //RM FISC Request電文
    private FISC_USDRM mFISCFCRMReq;
    //RM FISC Response電文
    private FISC_USDRM mFISCFCRMRes;
    //RM T24電文通用物件
    private T24PreClass mT24PreClass;

    public static final String  XMLHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    public static final String  allBan = "999";
    public static final Integer  XMLHeaderLength = 38;

    public RMAABase(RMData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mTxBRSData = txnData;
        this.setLogContext(txnData.getLogContext());
        this.setEj(txnData.getEj());
        this.mRMBusiness = new RM();
        this.mRMGeneral = mTxBRSData.getTxObject();
        this.mRMReq = mTxBRSData.getTxObject().getRequest();
        this.mRMRes = mTxBRSData.getTxObject().getResponse();

        getLogContext().setProgramFlowType(ProgramFlow.AAIn);
        getLogContext().setProgramName(mTxBRSData.getAaName());
        //寫文字檔LOG
        logMessage(Level.INFO, mTxBRSData.getLogContext());

        //準備FEPTxn相關物件
        this.feptxn = new FeptxnExt();
        // chenyang
        this.feptxnDao.setTableNameSuffix(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).substring(6, 8), "RMAABase");
        this.feptxn.setFeptxnEjfno(this.getEj());
        this.mTxBRSData.setFeptxn(this.feptxn);
        this.mTxBRSData.setFeptxnDao(this.feptxnDao);
        this.mTxBRSData.setEj(this.getEj());

        //建立RM Business物件,傳入RMData物件,將EJ,FepTxn及DBFepTxn也帶給Business
        this.mRMBusiness = new RM(mTxBRSData);
        this.mRMBusiness.setFeptxn(this.feptxn);
        this.mRMBusiness.setFeptxnDao(this.feptxnDao);
        this.mRMBusiness.setEj(this.getEj());
        this.mRMBusiness.setLogContext(getLogContext());

        //Modified by Jim, 2010/12/17, for R2300 send to T24
        if(txnData.getMessageID().equals(RMTXCode.R2300)){
            mTxT24Data = new T24Data();
            mTxT24Data.setEj(this.getEj());
            mTxT24Data.setMessageID(txnData.getMessageID());
            mTxT24Data.setTxChannel(txnData.getTxChannel());
            mTxT24Data.setMessageFlowType(txnData.getMessageFlowType());
            mTxT24Data.setLogContext(txnData.getLogContext());
            mTxT24Data.setMsgCtl(txnData.getMsgCtl());
            mTxT24Data.setFeptxn(txnData.getFeptxn());
            mTxT24Data.setFeptxnDao(txnData.getFeptxnDao());
            mTxT24Data.setTxObject(new T24PreClass());
        }
    }

    public RMAABase(FISCData txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mTxFISCData = txnData;
        this.setLogContext(mTxFISCData.getLogContext());
        this.setEj(txnData.getEj());

        this.mFISCGeneral = mTxFISCData.getTxObject();
        this.mFISCGeneral.setEJ(this.getEj());
        this.mFISCRMReq = mTxFISCData.getTxObject().getRMRequest();
        this.mFISCRMRes = mTxFISCData.getTxObject().getRMResponse();
        this.mFISCFCRMReq = mTxFISCData.getTxObject().getFCRMRequest();
        this.mFISCFCRMRes = mTxFISCData.getTxObject().getFCRMResponse();

        //準備FEPTxn相關物件
        this.setFeptxn(new FeptxnExt());

        if("16".equals(mTxFISCData.getMessageID().substring(0,2))){
            if(StringUtils.isBlank(mFISCFCRMReq.getTxnInitiateDateAndTime())){
                mFISCFCRMReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)).substring(1,7) + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            }
            if(mTxFISCData.getMessageFlowType() == MessageFlow.Request){
                this.feptxn.setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + mFISCFCRMReq.getTxnInitiateDateAndTime().substring(0,6)));
                this.mFISCFCRMReq.setEj(this.getEj());
            }else{
                this.feptxn.setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + mFISCFCRMRes.getTxnInitiateDateAndTime().substring(0,6)));
                this.mFISCFCRMReq.setEj(this.getEj());
            }
        }else{
            if(StringUtils.isBlank(mFISCRMReq.getTxnInitiateDateAndTime())){
                mFISCRMReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN)).substring(1,7) + FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            }
            if(mTxFISCData.getMessageFlowType() == MessageFlow.Request){
                this.feptxn.setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + mFISCRMReq.getTxnInitiateDateAndTime().substring(0,6)));
                this.mFISCRMReq.setEj(this.getEj());
            }else{
                this.feptxn.setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + mFISCRMRes.getTxnInitiateDateAndTime().substring(0,6)));
                this.mFISCRMReq.setEj(this.getEj());
            }
        }


        this.feptxnDao.setTableNameSuffix(new SimpleDateFormat("yyyyMMdd").format(new Date()).substring(6, 8),"RMAABase");

        //建立FISC Business物件
        this.mFISCBusiness = new FISC(mTxFISCData);
        this.mFISCBusiness.setFeptxn(this.feptxn);
        this.mFISCBusiness.setFeptxnDao(this.feptxnDao);
        this.mFISCBusiness.setEj(this.getEj());
        this.mFISCBusiness.setLogContext(this.getLogContext());

        //寫文字檔LOG
        getLogContext().setProgramFlowType(ProgramFlow.AAIn);
        getLogContext().setProgramName(mTxFISCData.getAaName());
        getLogContext().setChannel(FEPChannel.FISC);
        getLogContext().setSubSys(SubSystem.RM);
        getLogContext().setMessageFlowType(mTxFISCData.getMessageFlowType());
        getLogContext().setStan(mTxFISCData.getStan());
        getLogContext().setMessage(mTxFISCData.getTxRequestMessage());
        getLogContext().setEj(txnData.getEj());
        getLogContext().setTxDate(this.feptxn.getFeptxnTxDate());

        logMessage(Level.INFO, getLogContext());
    }

    public RMAABase(T24Data txnData) throws Exception {
        super();
        this.aaName = txnData.getAaName();
        this.mTxT24Data = txnData;
        this.setLogContext(txnData.getLogContext());
        this.setEj(txnData.getEj());

        //寫文字檔LOG
        this.mRMBusiness = new RM();
        this.mT24PreClass = txnData.getTxObject();

        mTxT24Data.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
        mTxT24Data.getLogContext().setProgramName(mTxT24Data.getAaName());
        //寫文字檔LOG
        logMessage(Level.INFO, mTxT24Data.getLogContext());

        //準備FEPTxn相關物件
        this.setFeptxn(new FeptxnExt());

        // chenyang
        //this.feptxnDao.setTableNameSuffix(FEPConfig., 0,new SimpleDateFormat("yyyyMMdd").format(new Date()).substring(6, 8));
        //this.getFeptxn().setFeptxnEjfno(this.getEj());
        mTxT24Data.setFeptxn(this.getFeptxn());

        //建立RM Business物件,傳入RMData物件,將EJ,FepTxn及DBFepTxn也帶給Business
        this.mRMBusiness = new RM(mTxT24Data);
        this.mRMBusiness.setFeptxn(this.getFeptxn());
        this.mRMBusiness.setFeptxnDao(this.feptxnDao);
        this.mRMBusiness.setEj(this.getEj());
        this.mRMBusiness.setLogContext(this.getLogContext());
    }

    public FEPReturnCode PrepareFEPMessageHeader(FEPMessage source,FEPMessage dest){
        return null;
    }

    public RMData getmTxBRSData() {
        return mTxBRSData;
    }

    public FISCData getmTxFISCData() {
        return mTxFISCData;
    }

    public T24Data getmTxT24Data() {
        return mTxT24Data;
    }

    public FISC getmFISCBusiness() {
        return mFISCBusiness;
    }

    public RM getmRMBusiness() {
        return mRMBusiness;
    }

    public RMGeneral getmRMGeneral() {
        return mRMGeneral;
    }

    public RMGeneralRequest getmRMReq() {
        return mRMReq;
    }

    public RMGeneralResponse getmRMRes() {
        return mRMRes;
    }

    public FISCGeneral getmFISCGeneral() {
        return mFISCGeneral;
    }

    public FISC_RM getmFISCRMReq() {
        return mFISCRMReq;
    }

    public FISC_RM getmFISCRMRes() {
        return mFISCRMRes;
    }

    public FISC_USDRM getmFISCFCRMReq() {
        return mFISCFCRMReq;
    }

    public FISC_USDRM getmFISCFCRMRes() {
        return mFISCFCRMRes;
    }

    public T24PreClass getmT24PreClass() {
        return mT24PreClass;
    }
}
