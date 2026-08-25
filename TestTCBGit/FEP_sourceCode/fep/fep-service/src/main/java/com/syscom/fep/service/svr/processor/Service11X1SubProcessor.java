package com.syscom.fep.service.svr.processor;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.FISC_Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.RmouteMapper;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Rmbtch;
import com.syscom.fep.mybatis.model.Rmfiscout1;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoute;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.server.aa.rm.AA1511;
import com.syscom.fep.server.aa.rm.RMAABase;
import com.syscom.fep.server.aa.rm.SyncFEDI;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.service.svr.SvrProcessor;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMCategory;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.constant.RMTXCode;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;
import com.syscom.fep.vo.text.rm.RMGeneral;

@StackTracePointCut(caller = SvrConst.SVR_11X1)
public class Service11X1SubProcessor extends SvrProcessor {
	private static final String ProgramName = Service11X1SubProcessor.class.getSimpleName() + ".";

	private FISC TxFISCBusiness;
	private FISCData TxFISCData;
	private FISC_RM FISCRMReq;
	private FISC_RM FISCRMRes;
	private Feptxn FepTxn;

	private Rmoutt mDefRMOUTT;

	private FISC TxFISCBusiness1511;
	private FISCData TxFISCData1511;
	private FISC_RM FISCRMReq1511;
	private FISC_RM FISCRMRes1511;
	@SuppressWarnings("unused")
	private FeptxnExt FepTxn1511;

	private String wkPCODE = "";
	private String PK = "";
	private String SEQNO = "";
	private String FISCRC = "";
	private String FISCRCMsg = "";

	private String repUNITBANK = "";

	private FeptxnDao DBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	@Autowired
	private FeptxnDao DBFEPTXN1511;

	/**
	 * .NET版程式的constructor翻寫到此方法中
	 */
	@Override
	protected void initialization() throws Exception {
		SERVICELOGGER.info("initialization...");
	}

	/**
	 * .NET版程式的constructor翻寫到此方法中
	 */
	public void initialization(FISCData txFISCData, FeptxnExt fepTxn, Rmoutt _defRMOUTT, String repUNITBANK) throws Exception {
		this.repUNITBANK = repUNITBANK;
		// Modify by Jim, 2011/01/27, 多執行緒，FepTxn 和 TxFISCData 物件會受到MainProcess的影響!
		// FepTxn = p_FEPTXN
		// TxFISCData = p_TxFISCData
		FISCRMReq = new FISC_RM();
		FISCRMReq.setFiscNo(txFISCData.getTxObject().getRMRequest().getFiscNo());
		FISCRMReq.setBankNo(txFISCData.getTxObject().getRMRequest().getBankNo());
		FISCRMReq.setProcessingCode(txFISCData.getTxObject().getRMRequest().getProcessingCode());
		FISCRMReq.setMessageType(txFISCData.getTxObject().getRMRequest().getMessageType());
		FISCRMReq.setTxnDestinationInstituteId(txFISCData.getTxObject().getRMRequest().getTxnDestinationInstituteId());
		FISCRMReq.setResponseCode(txFISCData.getTxObject().getRMRequest().getResponseCode());
		FISCRMRes = new FISC_RM();
		TxFISCData = new FISCData();
		TxFISCData.setTxObject(new FISCGeneral());
		TxFISCData.getTxObject().setRMRequest(FISCRMReq);
		TxFISCData.getTxObject().setRMResponse(FISCRMRes);
		TxFISCData.setStan(txFISCData.getStan());
		TxFISCData.setEj(txFISCData.getEj());
		TxFISCData.setMsgCtl(txFISCData.getMsgCtl());
		TxFISCData.setMessageID(txFISCData.getMessageID());
		TxFISCData.setTxRequestMessage(txFISCData.getTxRequestMessage());
		TxFISCData.setTxResponseMessage("");
		TxFISCData.setTxChannel(FEPChannel.FISC);
		TxFISCData.setTxSubSystem(SubSystem.RM);
		TxFISCData.setFiscTeleType(FISCSubSystem.RM);
		TxFISCData.setMessageFlowType(MessageFlow.Request);

		FepTxn = new FeptxnExt();
		DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), StringUtils.join(ProgramName, "initialization"));
		FepTxn.setFeptxnTxDate(fepTxn.getFeptxnTxDate());
		FepTxn.setFeptxnEjfno(fepTxn.getFeptxnEjfno());
		FepTxn = DBFEPTXN.selectByPrimaryKey(FepTxn.getFeptxnTxDate(), FepTxn.getFeptxnEjfno());

		mDefRMOUTT = _defRMOUTT;
		PK = StringUtils.join("RMOUTT PK(RMOUTT_TXDATE = [", mDefRMOUTT.getRmouttTxdate(), "], RMOUTT_BRNO = [", mDefRMOUTT.getRmouttBrno(), "], RMOUTT_ORIGINAL = [", mDefRMOUTT.getRmouttOriginal(),
				"], RMOUTT_FEPNO = [", mDefRMOUTT.getRmouttFepno(), "])");

		// 紀錄Log對象
		setLogContext(new LogData());

		getLogContext().setProgramFlowType(ProgramFlow.AAServiceIn);
		getLogContext().setSubSys(SubSystem.RM);
		getLogContext().setProgramName(ProgramName);
		// LogContext.Message = TxFISCData.TxRequestMessage
		getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		// Jim, 2012/5/29, channel改成FEP
		getLogContext().setChannel(FEPChannel.FEP);
		getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
		getLogContext().setMessageGroup("4");
		wkPCODE = StringUtils.join("1", mDefRMOUTT.getRmouttRemtype(), "1");
		getLogContext().setMessageId(wkPCODE + "00");
		getLogContext().setEj(TxFISCData.getEj());
		getLogContext().setStan(TxFISCData.getStan());
		getLogContext().setMessageFlowType(MessageFlow.Request);

		TxFISCData.setLogContext(getLogContext());

		// FISCRMReq = TxFISCData.TxObject.RMRequest
		// FISCRMRes = TxFISCData.TxObject.RMResponse

		// 建立FISC Business物件
		TxFISCBusiness = new FISC(TxFISCData);
		TxFISCBusiness.setFeptxn(FepTxn);
		TxFISCBusiness.setFeptxnDao(DBFEPTXN);
		TxFISCBusiness.setEj(TxFISCData.getEj());
		TxFISCBusiness.fiscNo = FISCRMReq.getFiscNo();
		TxFISCBusiness.bankNo = FISCRMReq.getBankNo();
		TxFISCBusiness.rmNo = mDefRMOUTT.getRmouttRmsno();

		TxFISCBusiness.setLogContext(getLogContext());

		SEQNO = StringUtils.join("STAN = [", getLogContext().getStan(), "], FISC_NO = [", FISCRMReq.getFiscNo(), "], BANK_NO = [", FISCRMReq.getBankNo(), "]", "], PCODE= [",
				FISCRMReq.getProcessingCode(), "]");

		getLogContext().setRemark(StringUtils.join("SubProcess New() ;", SEQNO + "; ", PK));
		logMessage(Level.DEBUG, getLogContext());

		FISCRC = "";
		FISCRCMsg = "";
		SERVICELOGGER.info(StringUtils.join("Service11X1--New SubProcess, Logcontext.stan=", getLogContext().getStan(), ", FISCData.stan=", TxFISCData.getStan()));

	}

	/**
	 * .NET版doBusiness()方法翻寫到此方法中
	 */
	@Override
	protected FEPReturnCode doBusiness() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String name = ProgramName + "doSubBusiness";
		try {
			SERVICELOGGER.info(StringUtils.join("Service11X1--SubProcess doSubBusiness, Logcontext.stan=", getLogContext().getStan(), ", FISCData.stan=", TxFISCData.getStan()));
			// (1).送往 FISC 之 Request 電文

			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = TxFISCBusiness.sendRMRequestToFISC(getLogContext().getStan(), TxFISCData.getTxRequestMessage());
			}

			getLogContext().setRemark(StringUtils.join("SubProcess doSubBusiness() After SendRMRequestToFISC, rtnCode=", rtnCode.toString(), "; IsAdapterOut=", TxFISCBusiness.isAdapterOut,
					"; FISCTxData.TxObject.RMResponse=", TxFISCBusiness.getFISCTxData().getTxObject().getRMResponse().getResponseCode(), "; ", SEQNO + "; ", PK));
			getLogContext().setProgramFlowType(ProgramFlow.None);
			getLogContext().setMessageFlowType(MessageFlow.None);
			logMessage(Level.DEBUG, getLogContext());

			if (rtnCode == FISCReturnCode.FISCTimeout) {
				callAA1511();
			} else if (!TxFISCBusiness.isAdapterOut && rtnCode != CommonReturnCode.Normal) {
				// Modify by Jim, 2011/02/17, 如果還沒呼叫到SendReceive就發生錯誤，就不做後續處理
			} else {

				getLogContext().setMessageFlowType(MessageFlow.Response);

				// (2). CheckResponseFromFISC:檢核回應電文是否正確
				if (rtnCode == CommonReturnCode.Normal) {
					rtnCode = TxFISCBusiness.checkResponseFromFISC();

					getLogContext().setProgramName(name); // ADD
					getLogContext()
							.setRemark(StringUtils.join("After TxFISCBusiness.CheckResponseFromFISC, rtnCode=", rtnCode.toString(), "; FISC_RC=", FISCRMRes.getResponseCode(), "; ", SEQNO, ";", PK));
					getLogContext().setMessageFlowType(MessageFlow.Response);
					getLogContext().setProgramFlowType(ProgramFlow.None);
					logMessage(Level.DEBUG, getLogContext());
				}

				// (3). 更新一般通訊匯出主檔之狀態04-已匯出或恢復為02-已發訊
				if (rtnCode == CommonReturnCode.Normal || StringUtils.isNotBlank(FISCRMRes.getResponseCode())) {
					// FISCRMRes.ResponseCode is not empty if FISC already response
					getLogContext().setMessageGroup("4");
					getLogContext().setRemark(StringUtils.join("匯出FISCSNO=", mDefRMOUTT.getRmouttFiscsno(), ",RMSNO=", mDefRMOUTT.getRmouttRmsno(), ",SENDBANK", mDefRMOUTT.getRmouttSenderBank(),
							",RECEIVERBANK=", mDefRMOUTT.getRmouttReceiverBank()));
					FISCRC = TxHelper.getRCFromErrorCode(FISCRMRes.getResponseCode(), FEPChannel.FISC, FEPChannel.BRANCH, getLogContext());
					FISCRCMsg = getLogContext().getResponseMessage();
					rtnCode = updateRMOUTTByRes();

					// LogContext.ProgramName = name
					// LogContext.Remark = "After UpdateRMOUTTByRes, rtnCode=" & rtnCode.ToString + ";" + SEQNO + ";" + PK
					// LogMessage(LogLevel.Info, LogContext)
				}

				// (4) 更新Memory資料
				updateMemory();
				// LogContext.ProgramName = name
				// LogContext.Remark = "After UpdateMemory, rtnCode=" & rtnCode.ToString + ";" + SEQNO + ";" + PK
				// LogMessage(LogLevel.Info, LogContext)

				// (5) 更新交易記錄(FEPTXN)
				if (rtnCode == CommonReturnCode.Normal || StringUtils.isNotBlank(FISCRMRes.getResponseCode())) {
					rtnCode = updateFEPTXN();

					// LogContext.ProgramName = name
					// LogContext.Remark = "After UpdateFEPTXN, rtnCode=" & rtnCode.ToString + ";" + SEQNO + ";" + PK
					// LogMessage(LogLevel.Info, LogContext)
				}

				// (6).Prepare匯出主檔及匯出暫存檔資料, 新增匯出主檔及匯出暫存檔
				if (NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode())) {
					rtnCode = TxFISCBusiness.processAPTOTAndRMTOTAndRMTOTAL("", FepTxn);
					// LogContext.ProgramName = name
					// LogContext.Remark = "After ProcessAPTOTAndRMTOTAndRMTOTAL, rtnCode=" & rtnCode.ToString + ";" + SEQNO + ";" + PK
					// LogMessage(LogLevel.Info, LogContext)
				}

				// (7).大批匯款匯出狀態至大批匯款回饋檔
				if ((rtnCode == CommonReturnCode.Normal || StringUtils.isNotBlank(FISCRMRes.getResponseCode())) && RMCategory.MediaRMOutTBSDY.equals(mDefRMOUTT.getRmouttCategory())) {
					updateRMBTCH();
					// LogContext.ProgramName = name
					// LogContext.Remark = "After UpdateRMBTCH, rtnCode=" + rtnCode.ToString + ";" + SEQNO + ";" + PK
					// LogMessage(LogLevel.Info, LogContext)
				}

				// (8).FEDI轉通匯匯出狀態回饋
				// If (rtnCode = CommonReturnCode.Normal OrElse Not String.IsNullOrEmpty(FISCRMRes.ResponseCode)) AndAlso
				// (mDefRMOUTT.RMOUTT_ORIGINAL = RMOUT_ORIGINAL.FEDI OrElse mDefRMOUTT.RMOUTT_ORIGINAL = RMOUT_ORIGINAL.MMAB2B) Then
				// CallSYNCFEDI()
				// 'LogContext.ProgramName = name
				// 'LogContext.Remark = "After CallSYNCFEDI; " + SEQNO + ";" + PK
				// 'LogMessage(LogLevel.Info, LogContext)
				// End If
				// 2021/02/04 Modify by Candy for PSP PCR
				// If (rtnCode = CommonReturnCode.Normal OrElse Not String.IsNullOrEmpty(FISCRMRes.ResponseCode)) AndAlso
				// (mDefRMOUTT.RMOUTT_ORIGINAL = RMOUT_ORIGINAL.FEDI OrElse mDefRMOUTT.RMOUTT_ORIGINAL = RMOUT_ORIGINAL.MMAB2B Then
				// CallSYNCFEDI()
				// End If
				if ((rtnCode == CommonReturnCode.Normal || StringUtils.isNotBlank(FISCRMRes.getResponseCode())) && (RMOUT_ORIGINAL.FEDI.equals(mDefRMOUTT.getRmouttOriginal())
						|| RMOUT_ORIGINAL.MMAB2B.equals(mDefRMOUTT.getRmouttOriginal()) || RMOUT_ORIGINAL.PSP.equals(mDefRMOUTT.getRmouttOriginal()))) {
					callSYNCFEDI();
				}
				getLogContext().setRemark("End Transaction, " + PK);
				getLogContext().setProgramFlowType(ProgramFlow.AAOut);
				getLogContext().setChannel(FEPChannel.FISC);
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			// 每次只執行一次, 這裡要設置businessFlag為false, 表示業務執行完畢, 否則下面的stop方法中會一直wait
			this.doBusinessFinished();
			super.stop();
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * .NET版StopService()方法翻寫到此方法中
	 */
	@Override
	protected void doStop() throws Exception {
		if (TxFISCBusiness != null) {
			TxFISCBusiness = null;
		}

		if (TxFISCBusiness1511 != null) {
			TxFISCBusiness1511 = null;
		}

		if (DBFEPTXN != null) {
			DBFEPTXN = null;
		}

		if (DBFEPTXN1511 != null) {
			DBFEPTXN1511 = null;
		}

		FepTxn = null;
		FepTxn1511 = null;
	}

	/**
	 * 此方法暫時不用實作
	 */
	@Override
	protected void doPause() throws Exception {}

	private FEPReturnCode callAA1511() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		RMAABase objAA1511 = null;
		@SuppressWarnings("unused")
		String fiscRes = null;

		try {
			FISCRMReq1511 = new FISC_RM();
			FISCRMRes1511 = new FISC_RM();

			FISCRMReq1511.setFiscNo(mDefRMOUTT.getRmouttFiscsno());
			FISCRMReq1511.setOrgPcode(mDefRMOUTT.getRmouttFiscSndCode());

			TxFISCData1511 = new FISCData();
			TxFISCData1511.setTxObject(new FISCGeneral());
			TxFISCData1511.getTxObject().setRMRequest(FISCRMReq1511);
			TxFISCData1511.getTxObject().setRMResponse(FISCRMRes1511);
			TxFISCData1511.setTxChannel(FEPChannel.FISC);
			TxFISCData1511.setTxSubSystem(SubSystem.RM);
			TxFISCData1511.setFiscTeleType(FISCSubSystem.RM);
			TxFISCData1511.setTxRequestMessage("");
			TxFISCData1511.setEj(TxHelper.generateEj());
			TxFISCData1511.setStan(TxFISCBusiness.getStan());
			TxFISCData1511.setMessageFlowType(MessageFlow.Request);

			// Get MsgID & MsgCtl
			TxFISCData1511.setMessageID("151100");
			TxFISCData1511.setMsgCtl(FEPCache.getMsgctrl(TxFISCData1511.getMessageID()));
			TxFISCData1511.setAaName(RMTXCode.AA1511);
			if (TxFISCData1511.getMsgCtl().getMsgctlStatus() == 1) {
				TxFISCData1511.setTxStatus(true);
			} else {
				TxFISCData1511.setTxStatus(false);
			}

			TxFISCData1511.setLogContext(new LogData());

			TxFISCData1511.getLogContext().setStan(TxFISCData1511.getStan());
			TxFISCData1511.getLogContext().setEj(TxFISCData1511.getEj());
			TxFISCData1511.getLogContext().setMessageId(TxFISCData1511.getMessageID());
			// Modify By Jim, 2011/02/08, 加LOG
			TxFISCData1511.getLogContext().setProgramName("Service11X1-CallAA1511");
			TxFISCData1511.getLogContext().setRemark(StringUtils.join("Service11X1 Call AA1511, Stan=", TxFISCData1511.getStan()));
			TxFISCData1511.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			TxFISCData1511.getLogContext().setMessageFlowType(MessageFlow.Request);
			logMessage(Level.DEBUG, TxFISCData1511.getLogContext());

			objAA1511 = new AA1511(TxFISCData1511);
			fiscRes = objAA1511.processRequestData();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	private FEPReturnCode updateRMOUTTByRes() {
		getLogContext().setProgramName(ProgramName + "UpdateRMOUTTByRes");
		getLogContext().setMessageFlowType(MessageFlow.Response);
		getLogContext().setProgramFlowType(ProgramFlow.None);

		Rmoutt defRMOUTT = new Rmoutt();
		RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);

		Rmoute defRMOUTE = new Rmoute();
		RmouteMapper dbRMOUTE = SpringBeanFactoryUtil.getBean(RmouteMapper.class);

		Rmout defRMOUT = new Rmout();
		RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);

		Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
		Rmfiscout1ExtMapper dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1ExtMapper.class);

		Rmoutsno defRMOUTSNO = new Rmoutsno();
		RmoutsnoExtMapper dbRMOUTSNO = SpringBeanFactoryUtil.getBean(RmoutsnoExtMapper.class);

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatusRM = transactionManager.getTransaction(new DefaultTransactionDefinition()); // 2021-12-27 Richard modified
		TransactionStatus txStatusNO = null; // 2021-12-27 Richard add
		try {

			defRMOUTT.setRmouttTxdate(mDefRMOUTT.getRmouttTxdate());
			defRMOUTT.setRmouttBrno(mDefRMOUTT.getRmouttBrno());
			defRMOUTT.setRmouttOriginal(mDefRMOUTT.getRmouttOriginal());
			defRMOUTT.setRmouttFepno(mDefRMOUTT.getRmouttFepno());
			defRMOUTT = dbRMOUTT.selectByPrimaryKey(defRMOUTT.getRmouttTxdate(), defRMOUTT.getRmouttBrno(), defRMOUTT.getRmouttOriginal(), defRMOUTT.getRmouttFepno());
			if (defRMOUTT == null) {
				SERVICELOGGER.info(StringUtils.join("RMOUTT Query no data, ", PK)); // ZK ADD
				getLogContext().setRemark(StringUtils.join("RMOUTT Query no data, ", PK));
				return IOReturnCode.QueryNoData;
			}

			defRMOUTT.setRmouttFiscRtnCode(FISCRMRes.getResponseCode());

			if (NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode())) {
				defRMOUTT.setRmouttStat(RMOUTStatus.GrantMoney); /// *06-已解款 */
			} else {
				// Modify by Jim, 2011/05/16, copy RMOUTE時把它MOVE到RMOUTT 已經更新欄位了

				updateRMSTAT("N");

				defRMOUTT.setRmouttFepsubno(StringUtils.leftPad(defRMOUTT.getRmouttFepsubno(), 2, '0'));
				// Modify by Jim, 2011/12/21, SPEC修改，更新RMOUT狀態邏輯修改
				if ("02".equals(FISCRMRes.getResponseCode().substring(0, 2))) {
					// Jim, 2012/01/11, 0201~0203要更新為系統問題
					if ("0201".equals(FISCRMRes.getResponseCode()) || "0202".equals(FISCRMRes.getResponseCode()) || "0203".equals(FISCRMRes.getResponseCode())) {
						defRMOUTT.setRmouttStat(RMOUTStatus.SystemProblem);
					} else {
						if (RMCategory.MediaRMOutTBSDY.equals(defRMOUTT.getRmouttCategory())) {// 整批匯款
							defRMOUTT.setRmouttStat(RMOUTStatus.DiskBatchRMOutFail); // 11’ /* 磁片整批匯出失敗 */
						} else {
							defRMOUTT.setRmouttStat(RMOUTStatus.FISCReject); // 07’ /* 財金拒絕 */
						}
					}
				} else {
					if (("5001".equals(FISCRMRes.getResponseCode()) || "0".equals(FISCRMRes.getResponseCode().substring(0, 1)) || "6".equals(FISCRMRes.getResponseCode().substring(0, 1)))
							&& !"0101".equals(FISCRMRes.getResponseCode())) {
						defRMOUTT.setRmouttStat(RMOUTStatus.SystemProblem);
					} else {
						if (RMCategory.MediaRMOutTBSDY.equals(mDefRMOUTT.getRmouttCategory())) {
							defRMOUTT.setRmouttStat(RMOUTStatus.DiskBatchRMOutFail); // 11.磁片整批匯出失敗 */
						} else {
							defRMOUTT.setRmouttStat(RMOUTStatus.FISCReject); // 07.財金拒絕 */
						}
					}
				}
				RefBase<Rmoute> rmouteRefBase = new RefBase<>(defRMOUTE);
				TxFISCBusiness.copyRMOUTTToRMOUTE(defRMOUTT, rmouteRefBase);
				defRMOUTE = rmouteRefBase.get();
				if (dbRMOUTE.insertSelective(defRMOUTE) < 1) {
					SERVICELOGGER.info(StringUtils.join("RMOUTE Insert fail, ", PK)); // ZK ADD
					getLogContext().setRemark(StringUtils.join("RMOUTE Insert fail, ", PK));
					return IOReturnCode.InsertFail;
				}

			}

			if (dbRMOUTT.updateByPrimaryKeySelective(defRMOUTT) < 1) {
				SERVICELOGGER.info(StringUtils.join("RMOUTT Update fail, ", PK)); // ZK ADD
				getLogContext().setRemark(StringUtils.join("RMOUTT Update fail, ", PK));
				transactionManager.rollback(txStatusRM); // 2021-12-27 Richard modified
				rtnCode = IOReturnCode.RMOUTUpdateError;
				return rtnCode;
			}

			mDefRMOUTT.setRmouttFepsubno(defRMOUTT.getRmouttFepsubno());
			mDefRMOUTT.setRmouttStat(defRMOUTT.getRmouttStat());
			mDefRMOUTT.setRmouttFiscRtnCode(defRMOUTT.getRmouttFiscRtnCode());

			defRMOUT.setRmoutTxdate(mDefRMOUTT.getRmouttTxdate());
			defRMOUT.setRmoutBrno(mDefRMOUTT.getRmouttBrno());
			defRMOUT.setRmoutOriginal(mDefRMOUTT.getRmouttOriginal());
			defRMOUT.setRmoutFepno(mDefRMOUTT.getRmouttFepno());

			defRMOUT.setRmoutStat(defRMOUTT.getRmouttStat());
			defRMOUT.setRmoutFiscRtnCode(defRMOUTT.getRmouttFiscRtnCode());
			defRMOUT.setRmoutFepsubno(defRMOUTT.getRmouttFepsubno());

			if (dbRMOUT.updateByPrimaryKeySelective(defRMOUT) < 1) {
				SERVICELOGGER.info(StringUtils.join("RMOUT Update fail, ", PK)); // ZK ADD
				getLogContext().setRemark(StringUtils.join("RMOUT Update fail, ", PK));
				transactionManager.rollback(txStatusRM); // 2021-12-27 Richard modified
				rtnCode = IOReturnCode.RMOUTUpdateError;
				return rtnCode;
			}

			transactionManager.commit(txStatusRM); // 2021-12-27 Richard modified

			getLogContext().setRemark(StringUtils.join("SubProcess.UpdateROUTTByRes dbRM.CommitTransaction, FISC_RC = ", FISCRMRes.getResponseCode(), ", Update RMOUTT_STAT = ",
					defRMOUTT.getRmouttStat(), "; ", SEQNO, "; ", PK));
			logMessage(Level.DEBUG, getLogContext());

			// 2021-12-27 Richard marked start
			// transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
			// txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
			// 2021-12-27 Richard marked end

			if (NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode())) {

				// 2021-12-27 Richard add start
				txStatusNO = transactionManager.getTransaction(new DefaultTransactionDefinition());
				// 2021-12-27 Richard add end

				defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				// defRMFISCOUT1.RMFISCOUT1_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_FBKNO
				// Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
				defRMFISCOUT1.setRmfiscout1ReceiverBank("999");

				// Modify by Jim, 2011/12/15, 直接寫成Update RMFISCOUT1 where RMFISCOUT1_REP_NO < FISCRMRes.FISC_NO
				// Jim, 2012/6/19, 改成直接用reqeust的電文序號來更新
				defRMFISCOUT1.setRmfiscout1RepNo(Integer.parseInt(FISCRMReq.getFiscNo()));
				int result = dbRMFISCOUT1.updateRMFISCOUT1byFISCResNO(defRMFISCOUT1);
				getLogContext().setRemark(StringUtils.join("After update RMFISCOUT1, result=", result, ", RMFISCOUT1_REP_NO=", defRMFISCOUT1.getRmfiscout1RepNo().toString()));
				logMessage(Level.DEBUG, getLogContext());

				// Modify by Jim, 2011/05/05,
				// 因為會發生相鄰的序號回來的順序不依序, 可能造成序號小的又把序號大的更新掉, 造程序號錯誤, 所以更新前先檢查財金回覆序號是否大於資料庫的序號
				// If dbRMFISCOUT1.QueryByPrimaryKey(defRMFISCOUT1) > 0 Then
				// LogContext.Remark = "Before update RMFISCOUT1, RMFISCOUT1_NO=" & defRMFISCOUT1.RMFISCOUT1_NO.ToString & ", RMFISCOUT1_REP_NO=" & defRMFISCOUT1.RMFISCOUT1_REP_NO.ToString
				// LogMessage(LogLevel.Info, LogContext)

				// If defRMFISCOUT1.RMFISCOUT1_REP_NO < Decimal.Parse(FISCRMRes.FISC_NO) Then

				// defRMFISCOUT1.RMFISCOUT1_REP_NO = Decimal.Parse(FISCRMRes.FISC_NO)
				// If dbRMFISCOUT1.UpdateByPrimaryKey(defRMFISCOUT1) <> 1 Then
				// LogContext.Remark = "RMFISCOUT1 Update fail, " & PK
				// dbNO.RollbackTransaction()
				// rtnCode = IOReturnCode.RMFISCOUT1UpdateError
				// LogContext.ReturnCode = rtnCode
				// TxHelper.GetRCFromErrorCode(rtnCode, FEPChannel.RM, LogContext)
				// Return rtnCode
				// End If
				// Else
				// LogContext.Remark = "SubProcess-In UpdateRMOUTTByRes(RMFISCOUT1), RMFISCOUT1_REP_NO=" & defRMFISCOUT1.RMFISCOUT1_REP_NO &
				// ", FISCRMRes.FISC_NO=" & FISCRMRes.FISC_NO & ", Don't Update RMFISCOUT1, " & PK
				// LogMessage(LogLevel.Info, LogContext)
				// End If

				// LogContext.Remark = "After update RMFISCOUT1, RMFISCOUT1_NO=" & defRMFISCOUT1.RMFISCOUT1_NO.ToString & ", RMFISCOUT1_REP_NO=" & defRMFISCOUT1.RMFISCOUT1_REP_NO.ToString
				// LogMessage(LogLevel.Info, LogContext)
				// Else
				// LogContext.Remark = "RMFISCOUT1 Not Found, " & PK
				// dbNO.RollbackTransaction()
				// rtnCode = IOReturnCode.RMFISCOUT1NotFound
				// LogContext.ReturnCode = rtnCode
				// TxHelper.GetRCFromErrorCode(rtnCode, FEPChannel.RM, LogContext)
				// Return rtnCode
				// End If

				defRMOUTSNO.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				// Modify by Jim, 2011/11/9, 抓RMOUTSNO要用UNIT_BANK
				// defRMOUTSNO.RMOUTSNO_RECEIVER_BANK = mDefRMOUTT.RMOUTT_RECEIVER_BANK.Substring(0, 3)
				defRMOUTSNO.setRmoutsnoReceiverBank(repUNITBANK);
				defRMOUTSNO.setRmoutsnoRepNo(Integer.parseInt(FISCRMReq.getBankNo()));
				result = dbRMOUTSNO.updateRMOUTSNObyFISCResNO(defRMOUTSNO);
				getLogContext().setRemark(StringUtils.join("After update RMOUTSNO, RECEIVER_BANK=", defRMOUTSNO.getRmoutsnoReceiverBank(), ", result=", result, ", RMOUTSNO_REP_NO=",
						defRMOUTSNO.getRmoutsnoRepNo().toString()));
				logMessage(Level.DEBUG, getLogContext());

				// If dbRMOUTSNO.QueryByPrimaryKey(defRMOUTSNO) > 0 Then
				// LogContext.Remark = "Before update RMOUTSNO, RMOUTSNO_NO=" & defRMOUTSNO.RMOUTSNO_NO.ToString & ", RMOUTSNO_REP_NO=" & defRMOUTSNO.RMOUTSNO_REP_NO.ToString
				// LogMessage(LogLevel.Info, LogContext)

				// If defRMOUTSNO.RMOUTSNO_REP_NO < Decimal.Parse(FISCRMReq.BANK_NO) Then

				// defRMOUTSNO.RMOUTSNO_REP_NO = Decimal.Parse(FISCRMReq.BANK_NO)

				// If dbRMOUTSNO.UpdateByPrimaryKey(defRMOUTSNO) <> 1 Then
				// LogContext.Remark = "RMOUTSNO Update fail, " & PK
				// dbNO.RollbackTransaction()
				// rtnCode = IOReturnCode.RMOUTSNOUPDATEOERROR
				// LogContext.ReturnCode = rtnCode
				// TxHelper.GetRCFromErrorCode(rtnCode, FEPChannel.RM, LogContext)
				// Return rtnCode
				// End If
				// Else
				// LogContext.Remark = "SubProcess-In UpdateRMOUTTByRes(RMOUTSNO), RMOUTSNO_REP_NO=" & defRMOUTSNO.RMOUTSNO_REP_NO &
				// ", FISCRMReq.BANK_NO=" & FISCRMReq.BANK_NO & ", Don't Update RMOUTSNO, " & PK
				// LogMessage(LogLevel.Info, LogContext)
				// End If

				// LogContext.Remark = "After update RMOUTSNO, RMOUTSNO_NO=" & defRMOUTSNO.RMOUTSNO_NO.ToString & ", RMOUTSNO_REP_NO=" & defRMOUTSNO.RMOUTSNO_REP_NO.ToString
				// LogMessage(LogLevel.Info, LogContext)
				// Else
				// LogContext.Remark = "RMOUTSNO Not Found, " & PK
				// dbNO.RollbackTransaction()
				// rtnCode = IOReturnCode.RMOUTSNONotFound
				// LogContext.ReturnCode = rtnCode
				// TxHelper.GetRCFromErrorCode(rtnCode, FEPChannel.RM, LogContext)
				// Return rtnCode
				// End If

				transactionManager.commit(txStatusNO); // 2021-12-27 Richard modified
			}

			// LogContext.Remark = "SubProcess-In UpdateRMOUTTByRes, After dbNO CommitTransaction"
			// LogMessage(LogLevel.Info, LogContext)
		} catch (Exception ex) {
			// 2021-12-27 Richard modified start
			if (!txStatusRM.isCompleted()) {
				transactionManager.rollback(txStatusRM);
			}
			if (txStatusNO != null) {
				transactionManager.rollback(txStatusNO);
			}
			// 2021-12-27 Richard modified end
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			// 2021-12-27 Richard marked start
			// if (!txStatus.isCompleted()) {
			// transactionManager.rollback(txStatus);
			// }
			// 2021-12-27 Richard marked end

			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				SERVICELOGGER.info(StringUtils.join("defRMOUTT Update Error, ", PK)); // ZK ADD
				getLogContext().setRemark(StringUtils.join("defRMOUTT Update Error, ", PK));
				getLogContext().setReturnCode(rtnCode);
				sendEMS(getLogContext());
			}
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateMemory() {

		synchronized (Service11X1MainProcessor.CurrentSendingData) {
			if (Service11X1MainProcessor.CurrentSendingData.contains(mDefRMOUTT.getRmouttFiscsno())) {
				Service11X1MainProcessor.CurrentSendingData.remove(mDefRMOUTT.getRmouttFiscsno());
				Service11X1MainProcessor.CurrentSendingCnt.decrementAndGet();
				Service11X1MainProcessor.CurrentSendingAmt.getAndAdd(mDefRMOUTT.getRmouttTxamt().longValue() * (-1));
			}
		}
		// Dim drSelectArray As DataRow()
		// Dim iIndex As Integer
		// SyncLock MainProcess._Memory
		// drSelectArray = MainProcess._Memory.Select("FISCSNO='" & mDefRMOUTT.RMOUTT_FISCSNO & "'")

		// If drSelectArray.Length > 0 Then
		// iIndex = MainProcess._Memory.Rows.IndexOf(drSelectArray(0))
		// MainProcess._Memory.Rows.RemoveAt(iIndex)
		// 'MainProcess._CurrentSendingCnt = MainProcess._CurrentSendingCnt - 1
		// Interlocked.Decrement(MainProcess._CurrentSendingCnt)
		// End If
		// Trace.WriteLine(String.Format("SubBusiness After Update _CurrentSendingCnt={0}", MainProcess._CurrentSendingCnt.ToString()))
		// End SyncLock
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateFEPTXN() {
		if (NormalRC.FISC_OK.equals(FepTxn.getFeptxnRepRc())) {
			FepTxn.setFeptxnTxrust(FeptxnTxrust.Successed);
		} else {
			FepTxn.setFeptxnTxrust(FeptxnTxrust.Reverse);
		}
		FepTxn.setFeptxnMsgflow(FISC_Response); // FISC Response
		// FepTxn.FEPTXN_REP_TIME = Now

		FepTxn.setFeptxnAaRc(FEPReturnCode.Normal.getValue());
		// If rtnCode <> CommonReturnCode.Normal Then
		// FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
		// FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString().PadLeft(4, "0"c)
		// End If

		FEPReturnCode rtnCode = TxFISCBusiness.updateFepTxnForRM(FepTxn);
		if (rtnCode != CommonReturnCode.Normal) {
			TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			return rtnCode;
		}

		return rtnCode;
	}

	private FEPReturnCode updateRMSTAT(String flag) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
		Rmstat defRMSTAT = new Rmstat();

		try {
			defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMSTAT.setRmstatFiscoFlag1(flag);

			if (dbRMSTAT.updateByPrimaryKeySelective(defRMSTAT) < 1) {
				rtnCode = IOReturnCode.RMSTATUpdateError;
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
			getLogContext().setRemark("Service11X1-UpdateRMSTAT, UPDATE RMSTAT_FISCO_FLAG1=N");
			logMessage(Level.DEBUG, getLogContext());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	private FEPReturnCode updateRMBTCH() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmbtchExtMapper dbRMBTCH = SpringBeanFactoryUtil.getBean(RmbtchExtMapper.class);
		Rmbtch defRMBTCH = new Rmbtch();

		try {
			defRMBTCH.setRmbtchSenderBank(mDefRMOUTT.getRmouttSenderBank());
			defRMBTCH.setRmbtchRemdate(mDefRMOUTT.getRmouttTxdate());
			defRMBTCH.setRmbtchTimes(mDefRMOUTT.getRmouttBatchno());
			defRMBTCH.setRmbtchFepno(mDefRMOUTT.getRmouttFepno());

			if (NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode())) {
				defRMBTCH.setRmbtchFepRc(NormalRC.RMBTCH_FEPRC_OK);
				defRMBTCH.setRmbtchErrmsg(NormalRC.RMBTCH_FEPRC_OKMSG);
			} else {
				defRMBTCH.setRmbtchFepRc(FISCRC);
				// defRMBTCH.RMBTCH_ERRMSG = TxFISCBusiness.SubStrBig5(TxHelper.GetMessageFromFEPReturnCode(CType(FISCRMRes.ResponseCode, String), FEPChannel.FISC, LogContext), 0, 80)
				defRMBTCH.setRmbtchErrmsg(FISCRCMsg);
			}
			SERVICELOGGER.info(StringUtils.join("SubProcess.UpdateRMBTCH; RMBTCH_FEP_RC= [", defRMBTCH.getRmbtchFepRc(), "], RMBTCH_ERRMSG= [", defRMBTCH.getRmbtchErrmsg(), "]; ", PK, "; ", SEQNO)); // ZK
																																																		// ADD
			getLogContext()
					.setRemark(StringUtils.join("SubProcess.UpdateRMBTCH; RMBTCH_FEP_RC= [", defRMBTCH.getRmbtchFepRc(), "], RMBTCH_ERRMSG= [", defRMBTCH.getRmbtchErrmsg(), "]; ", PK, "; ", SEQNO));
			logMessage(Level.DEBUG, getLogContext());

			if (dbRMBTCH.updateByPrimaryKeySelective(defRMBTCH) != 1) {
				rtnCode = IOReturnCode.RMBTCHUpdateError;
				SERVICELOGGER.info("更新RMBTCH筆數 <> 1"); // ZK
				getLogContext().setRemark("更新RMBTCH筆數 <> 1");
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	private FEPReturnCode callSYNCFEDI() {
		RMGeneral rmGeneral = new RMGeneral();
		RMData txData = new RMData();
		List<Msgctl> msgctls = FEPCache.getMsgctlList();
		HashMap<String, Msgctl> msgctlMap = new HashMap<>();
		RMAABase aa = null;

		try {
			rmGeneral.getRequest().setKINBR(mDefRMOUTT.getRmouttBrno());
			rmGeneral.getRequest().setTRMSEQ("99");
			rmGeneral.getRequest().setBRSNO(mDefRMOUTT.getRmouttBrsno());
			rmGeneral.getRequest().setORGCHLEJNO(mDefRMOUTT.getRmouttBrsno());
			rmGeneral.getRequest().setENTTLRNO("99");
			rmGeneral.getRequest().setSUPNO1("");
			rmGeneral.getRequest().setSUPNO2("");
			rmGeneral.getRequest().setTBSDY(mDefRMOUTT.getRmouttTxdate());
			rmGeneral.getRequest().setTIME(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			rmGeneral.getRequest().setFEPNO(mDefRMOUTT.getRmouttFepno());
			rmGeneral.getRequest().setREMDATE(mDefRMOUTT.getRmouttTxdate());
			rmGeneral.getRequest().setFISCRC(mDefRMOUTT.getRmouttFiscRtnCode());
			rmGeneral.getRequest().setCHLRC(FISCRC);
			rmGeneral.getRequest().setCHLMSG(FISCRCMsg);
			rmGeneral.getRequest().setSTATUS(mDefRMOUTT.getRmouttStat());
			rmGeneral.getRequest().setORIGINAL(mDefRMOUTT.getRmouttOriginal());

			txData.setEj(TxHelper.generateEj());
			txData.setTxObject(rmGeneral);
			txData.setTxChannel(TxFISCData.getTxChannel());
			txData.setTxSubSystem(SubSystem.RM);
			txData.setTxRequestMessage(serializeToXml(rmGeneral.getRequest()).replace("&lt;", "<").replace("&gt;", ">"));
			txData.setMessageID("SYNCFEDI");
			for (int i = 0; i < msgctls.size(); i++) {
				msgctlMap.put(msgctls.get(i).getMsgctlMsgid(), msgctls.get(i));
			}
			txData.setMsgCtl(msgctlMap.get("SYNCFEDI"));

			txData.setLogContext(new LogData());
			txData.getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			txData.getLogContext().setSubSys(getLogContext().getSubSys());
			txData.getLogContext().setChannel(getLogContext().getChannel());
			txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			txData.getLogContext().setEj(txData.getEj());
			txData.getLogContext().setMessage(txData.getTxRequestMessage());

			aa = new SyncFEDI(txData);
			getLogContext().setRemark("Start to call AA SyncFEDI");
			logMessage(Level.DEBUG, getLogContext());

			String resStr = aa.processRequestData();
			getLogContext().setRemark(StringUtils.join("After SyncFEDI, res=", resStr));
			logMessage(Level.DEBUG, getLogContext());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	@Override
	protected String getMDCProfileName() {
		return "Service11X1";
	}
}
