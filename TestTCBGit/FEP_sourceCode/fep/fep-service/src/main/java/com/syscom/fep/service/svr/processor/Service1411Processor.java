package com.syscom.fep.service.svr.processor;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.FISC_Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.MsgoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.MsgouteMapper;
import com.syscom.fep.mybatis.mapper.Rmfiscout4Mapper;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Msgoute;
import com.syscom.fep.mybatis.model.Rmfiscout4;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.aa.rm.AA1511;
import com.syscom.fep.server.aa.rm.RMAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.service.svr.SvrProcessor;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;

@StackTracePointCut(caller = SvrConst.SVR_1411)
public class Service1411Processor extends SvrProcessor {


	private String ProgramName = Service1411Processor.class.getSimpleName() + ".";

	private FISC TxFISCBusiness;
	private FISC_RM FISCRMReq;
	private FISC_RM FISCRMRes;
	private FISCData TxFISCData;
	private FeptxnExt FepTxn;

	private FISC TxFISCBusiness1511;
	private FISC_RM FISCRMReq1511;
	private FISC_RM FISCRMRes1511;
	private FISCData TxFISCData1511;
	@SuppressWarnings("unused")
	private FeptxnExt FepTxn1511;

	private Msgout _defMSGOUT;
	private FEPReturnCode _rtnCode;
	public boolean StopFlag = false;

	private FeptxnDao DBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	@Autowired
	@SuppressWarnings("unused")
	private FeptxnDao DBFEPTXN1511;
	/**
	 * .NET版程式的constructor翻寫到此方法中
	 */
	@Override
	protected void initialization() throws Exception {
		//紀錄Log
		setLogContext(new LogData());

		getLogContext().setProgramFlowType(ProgramFlow.AAServiceIn);
		getLogContext().setSubSys(SubSystem.RM);
		getLogContext().setProgramName("Service1411MainProcessor");
		getLogContext().setRemark("Service1411MainProcessor Start");
		getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		//Jim, 2012/5/29, channel改成FEP
		getLogContext().setChannel(FEPChannel.FEP);
		getLogContext().setMessageId("141100");
		getLogContext().setMessage("");
		getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
		logMessage(Level.INFO, getLogContext());

		//準備FEPTXN對象
		FepTxn = new FeptxnExt();
		DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8), StringUtils.join(ProgramName,"initialization"));

		initBusiness("0");
	}

	/**
	 * .NET版doBusiness()方法翻寫到此方法中
	 */
	@Override
	protected FEPReturnCode doBusiness() throws Exception {
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);

		String repUNITBANK = "";

		ArrayList<Msgout> arrayMSGOUT = null;

		try {
			//Modify by Jim, 2011/06/01, 強迫快取重新整理
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			getLogContext().setMessageGroup("4");
			getLogContext().setRemark("");
			getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

			//1.檢核財金及參加單位之系統狀態
			_rtnCode = checkSYSSTATAndRMSTAT();
			if (_rtnCode != CommonReturnCode.Normal) {
				SERVICELOGGER.info(StringUtils.join("Service1141-DoBusiness--checkSYSSTATAndRMSTAT _rtnCode=" , _rtnCode.toString())); // ZK ADD
				return _rtnCode;
			}

			//2.讀取 一般通訊匯出檔MSGOUT
			_defMSGOUT = new Msgout();

			_defMSGOUT.setMsgoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			_defMSGOUT.setMsgoutOwpriority("9");

			arrayMSGOUT = dbMSGOUT.getArrayListForService1411(_defMSGOUT.getMsgoutTxdate(),_defMSGOUT.getMsgoutOwpriority());

			SERVICELOGGER.info(StringUtils.join("Service1141-DoBusiness--Query MSGOUT Count=" , String.valueOf(arrayMSGOUT.size())));

			//以前項SELECT 一般通訊匯出檔MSGOUT LOOP
			//For Each dr In dtMSGOUT.Rows
			if(arrayMSGOUT.size() > 0){
				for (Msgout objMSGOUT : arrayMSGOUT) {
					getLogContext().setStep(0);
					getLogContext().setMessageFlowType(MessageFlow.Request);
					//紀錄PK

					_defMSGOUT = objMSGOUT;

					String pkStr = StringUtils.join("MSGOUT PK(MSGOUT_TXDATE=" , _defMSGOUT.getMsgoutTxdate() , "; MSGOUT_BRNO=" , _defMSGOUT.getMsgoutBrno() , "; MSGOUT_FEPNO=" , _defMSGOUT.getMsgoutFepno() , ")");

					getLogContext().setRemark(StringUtils.join("-------------------Begin Transaction, " , pkStr , "-----------------------"));
					logMessage(Level.DEBUG, getLogContext());

					//(2). 初始化TxFISCData,TxFiscBusiness
					_rtnCode = initBusiness("1");

					//(3). CheckBusinessRule: 商業邏輯檢核
					FISCRMReq.setProcessingCode("1411");
					if (_rtnCode == CommonReturnCode.Normal){
						RefString tempRef_repUNITBANK = new RefString(repUNITBANK);
						_rtnCode = checkBusinessRule(tempRef_repUNITBANK);
						repUNITBANK = tempRef_repUNITBANK.get();
					}
					if (_rtnCode != CommonReturnCode.Normal) {
						SERVICELOGGER.info(StringUtils.join(pkStr , ", After CheckBusinessRule rtn=" , _rtnCode.toString())); // ZK ADD
						getLogContext().setRemark(StringUtils.join(pkStr , ", After CheckBusinessRule rtn=" , _rtnCode.toString()));
						logMessage(Level.DEBUG, getLogContext());
						break;
					}

					//(4). Prepare : 交易記錄初始資料，AddTxData: 新增交易記錄(FEPTXN)
					_rtnCode = prepareAndInsertFEPTXN();
					if (_rtnCode != CommonReturnCode.Normal) {
						SERVICELOGGER.info(StringUtils.join(pkStr , ", After PrepareAndInsertFEPTXN rtn=" , _rtnCode.toString())); // ZK ADD
						getLogContext().setRemark(StringUtils.join(pkStr , ", After PrepareAndInsertFEPTXN rtn=" , _rtnCode.toString()));
						logMessage(Level.DEBUG, getLogContext());
						continue;
					}

					//(5). 組送往 FISC 之 Request 電文並等待財金之 Response
					_rtnCode = prepareAndSendToFisc();

					getLogContext().setMessageFlowType(MessageFlow.Response);

					if (_rtnCode == FEPReturnCode.FISCTimeout) {//若Timeout時需再組 1511-查詢交易處理狀況 電文查詢, call另一AA (1511.dll)發動查詢
						SERVICELOGGER.info(StringUtils.join(pkStr , ",PrepareAndSendToFisc FISCTimeout, call AA1511")); // ZK ADD
						getLogContext().setRemark(StringUtils.join(pkStr , ",PrepareAndSendToFisc FISCTimeout, call AA1511"));
						logMessage(Level.DEBUG, getLogContext());
						callAA1511();
					}

					if (_rtnCode == CommonReturnCode.Normal) {
						//(6). CheckResponseFromFISC:檢核回應電文是否正確
						_rtnCode = TxFISCBusiness.checkResponseFromFISC();
						SERVICELOGGER.info(StringUtils.join(pkStr , ", After TxFISCBusiness.CheckResponseFromFISC rtn=" , _rtnCode.toString() , ", FISCRMRes.ResponseCode=" , FISCRMRes.getResponseCode())); // ZK ADD
						getLogContext().setRemark(StringUtils.join(pkStr , ", After TxFISCBusiness.CheckResponseFromFISC rtn=" , _rtnCode.toString() , ", FISCRMRes.ResponseCode=" , FISCRMRes.getResponseCode()));
						logMessage(Level.DEBUG, getLogContext());
						SERVICELOGGER.info(StringUtils.join("匯出FISCSNO=" , _defMSGOUT.getMsgoutFiscsno() , ",SENDBANK" , _defMSGOUT.getMsgoutSenderBank() , ",RECEIVERBANK=" , _defMSGOUT.getMsgoutReceiverBank())); // ZK ADD
						getLogContext().setRemark(StringUtils.join("匯出FISCSNO=" , _defMSGOUT.getMsgoutFiscsno() , ",SENDBANK" , _defMSGOUT.getMsgoutSenderBank() , ",RECEIVERBANK=" , _defMSGOUT.getMsgoutReceiverBank()));
						TxHelper.getRCFromErrorCode(FISCRMRes.getResponseCode(), FEPChannel.FISC, FEPChannel.BRANCH, getLogContext());
						//(7). 更新一般通訊匯出主檔之狀態04-已匯出或恢復為02-已發訊
						//If _rtnCode = CommonReturnCode.Normal Then
						_rtnCode = updateMSGOUTByRes();
						//Else
						//End If
					}

					if (_rtnCode != CommonReturnCode.Normal) {
						updateRMSTAT();
					}

					//(8) 更新匯入通匯序號檔(RMINSNO)之CHKG為換KEY 成功, 及換KEY次數
					//TxFISCBusiness.WriteDebugLog("Service1141-DoBusiness", String.Format("Before UpdateRMINSNO rtnCode={0}{1} RepRC={2} ENGMEMO={3}", _rtnCode.ToString(), CType(_rtnCode, String).PadLeft(4, "0"c), FISCRMRes.ResponseCode, _defMSGOUT.MSGOUT_ENGMEMO.Substring(0, 3)))
					if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode()) && _defMSGOUT.getMsgoutEngmemo().length() >= 3 && "REP".equals(_defMSGOUT.getMsgoutEngmemo().substring(0, 3))) {
						_rtnCode = updateRMINSNO();
						SERVICELOGGER.info(StringUtils.join(pkStr , ", After UpdateRMINSNO rtn=" , _rtnCode.toString())); // ZK ADD
						getLogContext().setRemark(StringUtils.join(pkStr , ", After UpdateRMINSNO rtn=" , _rtnCode.toString()));
						logMessage(Level.DEBUG, getLogContext());
					}

					//(9).更新交易記錄(FEPTXN)
					_rtnCode = updateFEPTXN();
				}
			}

		} catch (Exception ex) {
			updateRMSTAT();
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			//Return CommonReturnCode.ProgramException
		} finally {
			SERVICELOGGER.info(StringUtils.join("Service1411-doBusiness--Service1411 Sleep=" , RMConfig.getInstance().getService1411INTERVAL() * 1000));
			Thread.sleep(RMConfig.getInstance().getService1411INTERVAL() * 1000);
			//If Not StopFlag Then
			//    doBusiness()
			//End If
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * .NET版StopService()方法翻寫到此方法中
	 */
	@Override
	protected void doStop() throws Exception {
		//紀錄Log
		getLogContext().setProgramFlowType(ProgramFlow.AAServiceOut);
		getLogContext().setSubSys(SubSystem.RM);
		getLogContext().setProgramName("Service1411MainProcessor");
		getLogContext().setRemark("Service1411MainProcessor Stop");
		getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		//Jim, 2012/5/29, channel改成FEP
		getLogContext().setChannel(FEPChannel.FEP);
		getLogContext().setMessageId("141100");
		getLogContext().setMessage("");

		logMessage(Level.INFO, getLogContext());

		if (TxFISCBusiness != null) {
			TxFISCBusiness = null;
		}

		if (TxFISCBusiness1511 != null) {
			TxFISCBusiness1511 = null;
		}

		FepTxn = null;
		FepTxn1511 = null;

	}

	/**
	 * 此方法暫時不用實作
	 */
	@Override
	protected void doPause() throws Exception {}

	/**
	 * 1.檢核財金及參加單位之系統狀態-CheckSYSSTATAndRMSTAT()
	 */
	private FEPReturnCode checkSYSSTATAndRMSTAT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
		Rmstat defRMSTAT = new Rmstat();
		try {
			rtnCode = TxFISCBusiness.checkRMStatus("1411", true);

			if (rtnCode == CommonReturnCode.Normal) {

				defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMSTAT = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
				if (defRMSTAT != null) {

					if ("N".equals(defRMSTAT.getRmstatFiscoFlag4())) {
						SERVICELOGGER.info("CheckSYSSTATAndRMSTAT--RMSTAT_FISCO_FLAG4 = N");
						rtnCode = CommonReturnCode.Abnormal;
					}
				} else {
					SERVICELOGGER.info("CheckSYSSTATAndRMSTAT--RMSTATNotFound");
					rtnCode = FEPReturnCode.RMSTATNotFound;
				}
			} else {
				SERVICELOGGER.info(StringUtils.join("CheckSYSSTATAndRMSTAT--CheckRMStatus rtn =" , rtnCode));
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			rtnCode = CommonReturnCode.ProgramException;
		} finally {
			defRMSTAT = null;
		}
		return rtnCode;
	}

	/**
	 * (2). 初始化TxFISCData,TxFiscBusiness
	 */
	private FEPReturnCode initBusiness(String sFlag) throws Exception {
		int iNewEJ = 0;
		String sNewSTAN = "0000000";

		if (sFlag.equals("0")) {
			TxFISCData = new FISCData();
			TxFISCData.setTxChannel(FEPChannel.FISC);
			TxFISCData.setTxSubSystem(SubSystem.RM);
			TxFISCData.setFiscTeleType(FISCSubSystem.RM);
			TxFISCData.setTxRequestMessage("");
			TxFISCData.setMessageFlowType(MessageFlow.Request);
			TxFISCData.setLogContext(getLogContext());

			//Get MsgID & MsgCtl
			TxFISCData.setMessageID("141100");
			TxFISCData.setMsgCtl(FEPCache.getMsgctrl(TxFISCData.getMessageID()));
			TxFISCData.setAaName("Service1411MainProcessor");
			if (TxFISCData.getMsgCtl().getMsgctlStatus() == 1) {
				TxFISCData.setTxStatus(true);
			} else {
				TxFISCData.setTxStatus(false);
			}
		} else {
			iNewEJ = TxHelper.generateEj();
			sNewSTAN = TxFISCBusiness.getStan();

			//Modify by Jim, 2011/02/17, 避免如果Service開著過營業日相關日期會錯誤，需要重新取
			getLogContext().setStep(0);
			getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			DBFEPTXN.setTableNameSuffix(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(6, 8) ,StringUtils.join(ProgramName,"initBusiness"));
		}

		getLogContext().setEj(iNewEJ);
		getLogContext().setStan(sNewSTAN);
		getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());

		FepTxn = new FeptxnExt();
		FISCRMReq = new FISC_RM();
		FISCRMRes = new FISC_RM();

		TxFISCData.setTxObject(new FISCGeneral());
		TxFISCData.getTxObject().setRMRequest(FISCRMReq);
		TxFISCData.getTxObject().setRMResponse(FISCRMRes);
		TxFISCData.setEj(iNewEJ);
		TxFISCData.setStan(sNewSTAN);

		FISCRMReq.setEj(iNewEJ);

		//建立FISC Business物件
		TxFISCBusiness = new FISC(TxFISCData);
		TxFISCBusiness.setFeptxn(FepTxn);
		TxFISCBusiness.setFeptxnDao(DBFEPTXN);
		TxFISCBusiness.setEj(iNewEJ);
		TxFISCBusiness.setLogContext(getLogContext());

		return CommonReturnCode.Normal;
	}

	/**
	 * (3). CheckBusinessRule: 商業邏輯檢核
	 */
	private FEPReturnCode checkBusinessRule(RefString repUNITBANK) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			repUNITBANK.set("");

			//(1) 	檢核一般通訊匯出狀態/檢核是否可傳送財金
			rtnCode = checkSYSSTATAndRMSTAT();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			//(2) 	檢核匯出資料
			rtnCode = TxFISCBusiness.checkOutDataByMSGOUT(_defMSGOUT, repUNITBANK);
			if (rtnCode != CommonReturnCode.Normal) {
				TxFISCBusiness.writeDebugLog("CheckBusinessRule", "CheckOutDataByMSGOUT rtn=" + rtnCode);
				return rtnCode;
			}

			//(4) 	取STAN,電文序號及更新到一般通訊匯出主檔
			rtnCode = updateMSGOUTForReq();
			if (rtnCode != CommonReturnCode.Normal) {
				TxFISCBusiness.writeDebugLog("CheckBusinessRule", "UpdateMSGOUTForReq rtn=" + rtnCode);
				return rtnCode;
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	//取STAN,電文序號及更新到一般通訊匯出主檔
	private FEPReturnCode updateMSGOUTForReq() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();
		@SuppressWarnings("unused")
		Rmfiscout4Mapper dbRMFISCOUT4 = SpringBeanFactoryUtil.getBean(Rmfiscout4Mapper.class);
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);
		try {
			defRMFISCOUT4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMFISCOUT4.setRmfiscout4ReceiverBank("950");
			SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
			Integer rmfiscout4NO = spCaller.getRMFISCOUT4NO(defRMFISCOUT4);
			defRMFISCOUT4.setRmfiscout4No(rmfiscout4NO);
			_defMSGOUT.setMsgoutFiscsno(StringUtils.leftPad(rmfiscout4NO.toString(), 7, '0'));

			_defMSGOUT.setMsgoutStan(TxFISCData.getStan());
			_defMSGOUT.setMsgoutEjno(TxFISCData.getEj());

			if (dbMSGOUT.updateByPrimaryKeySelective(_defMSGOUT) != 1) {
				rtnCode = IOReturnCode.MSGOUTUpdateError;
				updateRMSTAT();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
		}
		return rtnCode;
	}

	/**
	 * (4). Prepare : 交易記錄初始資料，AddTxData: 新增交易記錄(FEPTXN)
	 */
	private FEPReturnCode prepareAndInsertFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			rtnCode = TxFISCBusiness.prepareFEPTXNBy1411(_defMSGOUT, TxFISCData.getEj(), TxFISCData.getStan());
			if (rtnCode == CommonReturnCode.Normal) {

				if (DBFEPTXN.insertSelective(FepTxn) != 1) {
					rtnCode = IOReturnCode.FEPTXNInsertError;
				}

			}
			return rtnCode;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
		}
	}

	/**
	 * (5). 組送往 FISC 之 Request 電文並等待財金之 Response
	 */
	private FEPReturnCode prepareAndSendToFisc() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);

		try {
			//更新一般通訊匯出主檔之狀態匯出中
			_defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferring);
			_defMSGOUT.setMsgoutFiscSndCode("1411");

			_defMSGOUT.setMsgoutSenddate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			_defMSGOUT.setMsgoutSendtime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));

			if (dbMSGOUT.updateByPrimaryKeySelective(_defMSGOUT) != 1) {
				rtnCode = IOReturnCode.MSGOUTUpdateError;
			}

			SERVICELOGGER.info(StringUtils.join("After UpdateMSGOUT STAT rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			//組1411Req電文
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = prepareRM1411Req();
			}

			SERVICELOGGER.info(StringUtils.join("After PrepareRM1411Req rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			//送1411電文至財金
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = TxFISCBusiness.sendRMRequestToFISC();
			}

			SERVICELOGGER.info(StringUtils.join("After SendRMRequestToFISC rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
		}
		return rtnCode;
	}

	private FEPReturnCode prepareRM1411Req() {
		String reqMAC = "";
		ENCHelper encHelper = null;
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			//組header
			FISCRMReq.setSystemSupervisoryControlHeader("00");
			FISCRMReq.setSystemNetworkIdentifier("00");
			FISCRMReq.setAdderssControlField("00");
			FISCRMReq.setMessageType("0200");
			FISCRMReq.setProcessingCode("1411");
			FISCRMReq.setSystemTraceAuditNo(FepTxn.getFeptxnStan());
			FISCRMReq.setTxnDestinationInstituteId(StringUtils.rightPad("950", 7, '0'));
			FISCRMReq.setTxnSourceInstituteId(StringUtils.rightPad(_defMSGOUT.getMsgoutSenderBank().substring(0, 3), 7, '0'));
			FISCRMReq.setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(FepTxn.getFeptxnTxDate()).substring(1, 7) + FepTxn.getFeptxnTxTime()); //(轉成民國年)
			FISCRMReq.setResponseCode(NormalRC.FISC_REQ_RC);
			FISCRMReq.setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(),8,' '));

			//組Body
			FISCRMReq.setFiscNo(_defMSGOUT.getMsgoutFiscsno());
			FISCRMReq.setSenderBank(_defMSGOUT.getMsgoutSenderBank());
			FISCRMReq.setReceiverBank(_defMSGOUT.getMsgoutReceiverBank());
			RefString refChinese = new RefString(FISCRMReq.getChineseMemo());
			if (TxFISCBusiness.convertFiscEncode(_defMSGOUT.getMsgoutChnmemo(), refChinese)) {
				FISCRMReq.setChineseMemo(refChinese.get());
				FISCRMReq.setChineseMemo(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getChineseMemo().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getChineseMemo());
			} else {
				return RMReturnCode.ChineseRowError;
			}
			if (_defMSGOUT.getMsgoutEngmemo().length() >= 3 && "CHG".equals(_defMSGOUT.getMsgoutEngmemo().substring(0, 3))) {
				FISCRMReq.setEnglishMemo(StringUtil.toHex(_defMSGOUT.getMsgoutEngmemo() + StringUtils.leftPad(_defMSGOUT.getMsgoutFiscsno(),7, '0')));
			} else {
				FISCRMReq.setEnglishMemo(StringUtil.toHex(_defMSGOUT.getMsgoutEngmemo()));
			}
			SERVICELOGGER.info(StringUtils.join("FISCRMReq.ENGLISH_MEMO=", FISCRMReq.getEnglishMemo()));
			FISCRMReq.setEnglishMemo(StringUtil.convertFromAnyBaseString(String.valueOf((FISCRMReq.getEnglishMemo().length() + 4) / 2), 10, 16, 4) + FISCRMReq.getEnglishMemo());
			SERVICELOGGER.info(StringUtils.join("Hex FISCRMReq.ENGLISH_MEMO=", FISCRMReq.getEnglishMemo()));

			//產生MAC
			encHelper = new ENCHelper(TxFISCData);
			RefString refReq = new RefString(reqMAC);
			rtnCode = encHelper.makeRMFISCMAC(refReq);
			reqMAC = refReq.get();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			FISCRMReq.setMAC(StringUtils.leftPad(reqMAC, 8, '0'));

			//MakeBitmap
			if (rtnCode.getValue() == CommonReturnCode.Normal.getValue()) {
				rtnCode = TxFISCBusiness.makeBitmap(FISCRMReq.getMessageType(), FISCRMReq.getProcessingCode(), MessageFlow.Request);
			}
			SERVICELOGGER.info(StringUtils.join("after MakeBitmap rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * (7). 更新一般通訊匯出主檔之狀態04-已匯出或恢復為02-已發訊
	 */
	private FEPReturnCode updateMSGOUTByRes() {

		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);

		Msgoute defMSGOUTE = new Msgoute();
		MsgouteMapper dbMSGOUTE = SpringBeanFactoryUtil.getBean(MsgouteMapper.class);

		@SuppressWarnings("unused")
		Rmfiscout4Mapper dbRMFISCOUT4 = SpringBeanFactoryUtil.getBean(Rmfiscout4Mapper.class);
		Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {

			_defMSGOUT.setMsgoutFiscRtnCode(FISCRMRes.getResponseCode());

			if (NormalRC.FISC_OK.equals(FISCRMRes.getResponseCode())) {
				_defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferred);
			} else {
				RefBase<Msgoute> refDefMSGOUTE = new RefBase<>(defMSGOUTE);
				copyMSGOUTToMSGOUTE(refDefMSGOUTE);
				defMSGOUTE = refDefMSGOUTE.get();
				//Jim, 2012/5/3, 如果更新不到資料再insert, 不然可能會發生PK重覆
				if (dbMSGOUTE.updateByPrimaryKeySelective(defMSGOUTE) < 1) {
					if (dbMSGOUTE.insertSelective(defMSGOUTE) != 1) {
						//db.RollbackTransaction()
						TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.MSGOUTINSERTTERROR.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
						//Return IOReturnCode.MSGOUTINSERTTERROR
					}
				}

				defRMFISCOUT4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMFISCOUT4.setRmfiscout4ReceiverBank("950");
				SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);

				spCaller.decrementRMFISCOUT4NO(defRMFISCOUT4);

				_defMSGOUT.setMsgoutFepsubno(String.valueOf(Double.parseDouble(_defMSGOUT.getMsgoutFepsubno()) + 1));
				if (("5001".equals(FISCRMRes.getResponseCode()) || "0".equals(FISCRMRes.getResponseCode().substring(0, 1)) || "6".equals(FISCRMRes.getResponseCode().substring(0, 1))) && !"0202".equals(FISCRMRes.getResponseCode())) {
					_defMSGOUT.setMsgoutStat(MSGOUTStatus.SystemProblem);
				}
				else {
					_defMSGOUT.setMsgoutStat(MSGOUTStatus.FISCRefuse); //07
				}
				_defMSGOUT.setMsgoutFiscRtnCode(FISCRMRes.getResponseCode());
			}

			if (dbMSGOUT.updateByPrimaryKeySelective(_defMSGOUT) != 1) {
				transactionManager.rollback(txStatus);
				TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.MSGOUTUpdateError.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
				SERVICELOGGER.info("dbMSGOUT.UpdateByPrimaryKey <> 1"); // ZK ADD
				getLogContext().setRemark("dbMSGOUT.UpdateByPrimaryKey <> 1");
				sendEMS(getLogContext());
				return IOReturnCode.MSGOUTUpdateError;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			if(!txStatus.isCompleted()){
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}finally {
			if(!txStatus.isCompleted()){
				transactionManager.commit(txStatus);
			}
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode copyMSGOUTToMSGOUTE(RefBase<Msgoute> defMSGOUTE) {
		defMSGOUTE.get().setMsgouteTxdate(_defMSGOUT.getMsgoutTxdate());
		defMSGOUTE.get().setMsgouteBrno(_defMSGOUT.getMsgoutBrno());
		defMSGOUTE.get().setMsgouteFepno(_defMSGOUT.getMsgoutFepno());
		defMSGOUTE.get().setMsgouteFepsubno(_defMSGOUT.getMsgoutFepsubno());
		defMSGOUTE.get().setMsgouteCategory(_defMSGOUT.getMsgoutCategory());
		defMSGOUTE.get().setMsgouteSenderBank(_defMSGOUT.getMsgoutSenderBank());
		defMSGOUTE.get().setMsgouteReceiverBank(_defMSGOUT.getMsgoutReceiverBank());
		defMSGOUTE.get().setMsgouteStan(_defMSGOUT.getMsgoutStan());
		defMSGOUTE.get().setMsgouteFiscsno(_defMSGOUT.getMsgoutFiscsno());
		//defMSGOUTE.MSGOUTE_RMSNO = defMSGOUT.MSGOUT_RMSNO
		defMSGOUTE.get().setMsgouteStat(_defMSGOUT.getMsgoutStat());
		defMSGOUTE.get().setMsgouteOwpriority(_defMSGOUT.getMsgoutOwpriority());
		defMSGOUTE.get().setMsgouteRegdate(_defMSGOUT.getMsgoutRegdate());
		defMSGOUTE.get().setMsgouteRegtime(_defMSGOUT.getMsgoutRegtime());
		defMSGOUTE.get().setMsgouteApdate(_defMSGOUT.getMsgoutApdate());
		defMSGOUTE.get().setMsgouteAptime(_defMSGOUT.getMsgoutAptime());
		defMSGOUTE.get().setMsgouteOrderdate(_defMSGOUT.getMsgoutOrderdate());
		defMSGOUTE.get().setMsgouteSenddate(_defMSGOUT.getMsgoutSenddate());
		defMSGOUTE.get().setMsgouteSendtime(_defMSGOUT.getMsgoutSendtime());
		defMSGOUTE.get().setMsgouteFiscRtnCode(_defMSGOUT.getMsgoutFiscRtnCode());
		defMSGOUTE.get().setMsgouteChnmemo(_defMSGOUT.getMsgoutChnmemo());
		defMSGOUTE.get().setMsgouteEngmemo(_defMSGOUT.getMsgoutEngmemo());
		defMSGOUTE.get().setMsgouteSupno1(_defMSGOUT.getMsgoutSupno1());
		defMSGOUTE.get().setMsgouteSupno2(_defMSGOUT.getMsgoutSupno2());
		defMSGOUTE.get().setMsgouteRegTlrno(_defMSGOUT.getMsgoutRegTlrno());
		defMSGOUTE.get().setMsgouteFiscSndCode(_defMSGOUT.getMsgoutFiscSndCode());
		defMSGOUTE.get().setMsgouteEjno(_defMSGOUT.getMsgoutEjno());
		defMSGOUTE.get().setUpdateUserid(_defMSGOUT.getUpdateUserid());
		defMSGOUTE.get().setUpdateTime(_defMSGOUT.getUpdateTime());

		return CommonReturnCode.Normal;
	}

	/**
	 * (8) 更新匯入通匯序號檔(RMINSNO)之CHKG為換KEY 成功, 及換KEY次數
	 */
	private FEPReturnCode updateRMINSNO() {
		Rminsno defRMINSNO = new Rminsno();
		RminsnoExtMapper dbRMINSNO = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			defRMINSNO.setRminsnoSenderBank(_defMSGOUT.getMsgoutReceiverBank().substring(0, 3));
			defRMINSNO.setRminsnoReceiverBank(_defMSGOUT.getMsgoutSenderBank().substring(0, 3));
			defRMINSNO.setRminsnoChgk("0");

			SERVICELOGGER.info(StringUtils.join("into UpdateRMINSNO RMINSNO_SENDER_BANK=", defRMINSNO.getRminsnoSenderBank()));
			SERVICELOGGER.info(StringUtils.join("into UpdateRMINSNO RMINSNO_RECEIVER_BANK=", defRMINSNO.getRminsnoReceiverBank()));
			SERVICELOGGER.info(StringUtils.join("into UpdateRMINSNO RMINSNO_CHGK=", defRMINSNO.getRminsnoChgk()));

			if (dbRMINSNO.updateRMINSNOforCHKG(defRMINSNO) != 1) {
				rtnCode = IOReturnCode.RMINSNONotFound;
			}

			return rtnCode;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
		}
	}

	/**
	 * (9).更新交易記錄(FEPTXN)
	 */
	private FEPReturnCode updateFEPTXN() {
		FeptxnExt defFEPTXN = new FeptxnExt();
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			defFEPTXN.setFeptxnTxDate(FepTxn.getFeptxnTxDate());
			defFEPTXN.setFeptxnEjfno(FepTxn.getFeptxnEjfno());
			defFEPTXN.setFeptxnAaRc(_rtnCode.getValue());
			defFEPTXN.setFeptxnMsgflow(FISC_Response);

			if (NormalRC.FISC_OK.equals(FepTxn.getFeptxnRepRc())) {
				FepTxn.setFeptxnTxrust(FeptxnTxrust.Successed);
			} else {
				FepTxn.setFeptxnTxrust(FeptxnTxrust.Reverse);
			}


			FepTxn.setFeptxnMsgflow(FISC_Response);
			FepTxn.setFeptxnAaRc(_rtnCode.getValue());

			//If _rtnCode <> CommonReturnCode.Normal Then
			//    defFEPTXN.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
			//    defFEPTXN.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString()
			//End If

			if (DBFEPTXN.updateByPrimaryKeySelective(defFEPTXN) != 1) {
				rtnCode = IOReturnCode.FEPTXNUpdateNotFound;
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
			}
		}
		return rtnCode;
	}

	/**
	 * 更新RMSTAT可傳送財金標誌為FALSE
	 */
	private FEPReturnCode updateRMSTAT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
		Rmstat defRMSTAT = new Rmstat();

		try {
			//Fly 2016/03/23 非Check in 狀態，不更新RMSTAT
			if ("1".equals(SysStatus.getPropertyValue().getSysstatAoct1000()) && "1".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
				defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMSTAT.setRmstatFiscoFlag4("N");

				if (dbRMSTAT.updateByPrimaryKeySelective(defRMSTAT) != 1) {
					rtnCode = IOReturnCode.RMSTATUpdateError;
					TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, TxFISCData.getTxChannel(), getLogContext());
				}
				TxFISCBusiness.writeDebugLog("Service1411-UpdateRMSTAT", " UPDATE RMSTAT_FISCO_FLAG4='N'");
			} else {
				SERVICELOGGER.info("Service1411-非Check in 狀態，不更新RMSTAT"); // ZK ADD
				getLogContext().setRemark("Service1411-非Check in 狀態，不更新RMSTAT");
				logMessage(Level.DEBUG, getLogContext());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	/**
	 * 若1411Req Timeout時需再組 1511-查詢交易處理狀況 電文查詢, call另一AA (1511.dll)發動查詢
	 */
	private FEPReturnCode callAA1511() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		RMAABase objAA1511 = null;
		@SuppressWarnings("unused")
		String fiscRes = null;

		try {
			FISCRMReq1511 = new FISC_RM();
			FISCRMRes1511 = new FISC_RM();

			//FISCRMReq1511.SystemSupervisoryControlHeader = "00"
			//FISCRMReq1511.SystemNetworkIdentifier = "00"
			//FISCRMReq1511.AdderssControlField = "00"
			//FISCRMReq1511.MessageType = "0200"
			//FISCRMReq1511.ProcessingCode = "1511"
			//FISCRMReq1511.SystemTraceAuditNo = TxFISCBusiness.GetStan()
			//FISCRMReq1511.TxnDestinationInstituteId = "950".PadRight(7, CChar("0"))
			//FISCRMReq1511.TxnSourceInstituteId = SysStatus.PropertyValue.SYSSTAT_HBKNO.PadRight(7, CChar("0"))
			//FISCRMReq1511.TxnInitiateDateAndTime = DateLib.ADStringToROCString(Now.ToString("yyyyMMdd")).Substring(1, 6) & Now.ToString("HHmmss") '(轉成民國年)
			//FISCRMReq1511.ResponseCode = FEPBase.FISCRC_INIT
			//FISCRMReq1511.SyncCheckItem = SysStatus.PropertyValue.SYSSTAT_TRMSYNC.PadLeft(8, CChar(" "))

			FISCRMReq1511.setFiscNo(_defMSGOUT.getMsgoutFiscsno());
			FISCRMReq1511.setOrgPcode(_defMSGOUT.getMsgoutFiscSndCode());

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

			//Get MsgID & MsgCtl
			TxFISCData1511.setMessageID("151100");
			TxFISCData1511.setMsgCtl(FEPCache.getMsgctrl(TxFISCData1511.getMessageID()));
			TxFISCData1511.setAaName("AA1511");
			if (TxFISCData1511.getMsgCtl().getMsgctlStatus() == 1) {
				TxFISCData1511.setTxStatus(true);
			} else {
				TxFISCData1511.setTxStatus(false);
			}

			TxFISCData1511.setLogContext(new LogData());

			TxFISCData1511.getLogContext().setStan(TxFISCData1511.getStan());
			TxFISCData1511.getLogContext().setEj(TxFISCData1511.getEj());

			objAA1511 = new AA1511(TxFISCData1511);
			fiscRes = objAA1511.processRequestData();
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

}
