package com.syscom.fep.server.aa.rm;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.MsginExtMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
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
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefInt;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.MsginMapper;
import com.syscom.fep.mybatis.mapper.MsgoutMapper;
import com.syscom.fep.mybatis.mapper.MsgouteMapper;
import com.syscom.fep.mybatis.mapper.Rmfiscin4Mapper;
import com.syscom.fep.mybatis.ext.mapper.RminsnoExtMapper;
import com.syscom.fep.mybatis.mapper.RmstatMapper;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Msgoute;
import com.syscom.fep.mybatis.model.Rmfiscin4;
import com.syscom.fep.mybatis.model.Rmfiscout4;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.MSGINStatus;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_RM;

public class AA1412 extends RMAABase {
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private String rtnMessage = "";
	//private FISCData txFISCData1411;
	private FISC_RM fISCRMRes1411;
	private Rmstat defRMSTAT = new Rmstat();
	private Msgout _defMSGOUT;
	private Msgin _defMSGIN = new Msgin();
	private int EJ1411;
	private String Stan1411;
	private FISC_RM FISCRMReq1411; // RM FISC Request電文
	private FISC_RM FISCRMRes1411; // RM FISC Response電文
	private FISCData TxFISCData1411;
	private FISCGeneral FISCGeneral1411; // RM FISC 電文通用物件
	private FISC FISCBusiness1411;

	public AA1412(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 * 
	 * @return Response電文
	 * 
	 */
	@Override
	public String processRequestData() throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatFbkno());
		try {
			// 1.檢核財金電文,若為Garble則組回覆訊息(SendGarbledMessage)，程式結束
			_rtnCode = checkFISCRMReq();

			// 2.判斷是否是Garbled Message
			if (_rtnCode != CommonReturnCode.Normal) {
				// 判斷是否是Garbled Message
				if (getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode).substring(0, 2).equals("10")) // Garble
				{
					getmFISCBusiness().sendGarbledMessage(getmFISCRMReq().getEj(), _rtnCode, getmFISCRMReq());
					return "";
				}
			}

			// 3.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
			if (_rtnCode == CommonReturnCode.Normal
					|| PolyfillUtil.isNumeric(getmFISCRMReq().getTxnInitiateDateAndTime().substring(0, 6))) {
				prepareAndInsertFEPTXN();
			}

			// 4.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
			RefString repMAC = new RefString("");
			RefInt wkREPK = new RefInt(0);
			RefString wkRC = new RefString("");
			RefString repUNITBANK = new RefString("");
			rtnCode = checkBusinessRule(repMAC, wkREPK, wkRC, repUNITBANK);
			if (rtnCode != CommonReturnCode.Normal) {
				if (_rtnCode == CommonReturnCode.Normal) {
					_rtnCode = rtnCode;
				}
				getmFISCBusiness().writeDebugLog("AA1412-Main", StringUtils.join("After CheckBusinessRule rtnCode="
						, _rtnCode.name() , StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));
			}

			// 5.Prepare一般通訊匯入主檔資料，新增一般通訊匯入主檔檔(MSGIN )
			RefString wkFEPNO = new RefString("");
			rtnCode = insertMSGIN(wkFEPNO);
			if (rtnCode != CommonReturnCode.Normal) {
				if (_rtnCode == CommonReturnCode.Normal) {
					_rtnCode = rtnCode;
				}
				getmFISCBusiness().writeDebugLog("AA1412-Main", StringUtils.join("After InsertMSGIN rtnCode=",  _rtnCode.name()
						, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));
			}

			// 6.準備回財金的相關資料並送回覆電文到財金(SendToFISC)
			_rtnCode = prepareAndSendForFISC(repMAC.get());

			getmFISCBusiness().writeDebugLog("AA1412-Main",
					StringUtils.join("After PrepareAndSendForFISC wkREPK=", String.valueOf(wkREPK.get())));

			// 11.若為他行換KEY要求(wkREPK=1 Or 3), 需另組回覆匯出通匯押基碼交易至財金公司(組PCODE=1411電文)
			if ((wkREPK.get() == 1 || wkREPK.get() == 3)
					&& NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
				try {
					_rtnCode = initBusiness1411();

					// (2)檢核一般通訊匯出狀態
					if (_rtnCode == CommonReturnCode.Normal) {
						_rtnCode = FISCBusiness1411.checkRMStatus("1411", true);
					}
					FISCBusiness1411.writeDebugLog("AA1412-Main", StringUtils.join("After CheckRMStatus rtnCode="
							, _rtnCode.name() , StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

					RefBoolean wkFISCO_FLAG4 = new RefBoolean(true);
					if ("Y".equals(defRMSTAT.getRmstatFiscoFlag4()) && _rtnCode == CommonReturnCode.Normal) {
						updateRMSTAT();
						// (4)Prepare一般通訊匯出主檔資料，新增一般通訊匯出主檔檔(MSGOUT)
						// (5)Prepare 交易記錄初始資料，AddTxData: 新增交易記錄(FEPTXN)
						_rtnCode = insertFEPTXNAndMSGOUT(wkREPK.get(), wkRC.get(), wkFISCO_FLAG4);
						FISCBusiness1411.writeDebugLog("AA1412-Main",
								StringUtils.join("After InsertFEPTXNAndMSGOUT rtnCode=" , _rtnCode.name()
										, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

						// (6)組送往 FISC 之 Request 電文並等待財金之 Response
						if (_rtnCode == CommonReturnCode.Normal) {
							_rtnCode = prepareAndSend1411ToFisc();
						}

						// (7) CheckResponseFromFISC:檢核回應電文是否正確
						if (_rtnCode == CommonReturnCode.Normal) {
							_rtnCode = FISCBusiness1411.checkResponseFromFISC();
						}

						FISCBusiness1411.writeDebugLog("AA1412-Main",
								StringUtils.join("After CheckResponseFromFISC rtnCode=" , _rtnCode.name()
										, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

						// (8)更新一般通訊匯出主檔之狀態06-已匯出或恢復為02-已發訊
						if (_defMSGOUT != null) {
							_rtnCode = updateMSGOUTBy1411Res();
						}
						FISCBusiness1411.writeDebugLog("AA1412-Main",
								StringUtils.join("After UpdateMSGOUTBy1411Res rtnCode=" , _rtnCode.name()
										, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

						if (_rtnCode != CommonReturnCode.Normal) {
							TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.BRANCH);
							// “一般通訊匯出暫停, 需人工處理REPK “之訊息至LSMonitorConsole
							updateRMSTAT();
						}

						// (9)更新匯入通匯序號檔(RMINSNO)之CHKG為換KEY 成功, 及換KEY次數
						FISCBusiness1411.writeDebugLog("AA1412-Main",
								StringUtils.join("Before UpdateRMINSNO rtnCode=" , _rtnCode.name()
										, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0') , " RepRC="
										, fISCRMRes1411.getResponseCode()));
						if (_rtnCode == CommonReturnCode.Normal
								&& NormalRC.FISC_OK.equals(fISCRMRes1411.getResponseCode())) {
							_rtnCode = updateRMINSNO();
						}
						FISCBusiness1411.writeDebugLog("AA1412-Main", StringUtils.join("After UpdateRMINSNO rtnCode="
								, _rtnCode.name() , StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

						// (10)更新交易記錄(FEPTXN)
						_rtnCode = updateFEPTXN1411();
						FISCBusiness1411.writeDebugLog("AA1412-Main", StringUtils.join("After UpdateFEPTXN1411 rtnCode="
								, _rtnCode.name() , StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));
					} else {
						wkFISCO_FLAG4 = new RefBoolean(false);
						_rtnCode = insertFEPTXNAndMSGOUT(wkREPK.get(), wkRC.get(), wkFISCO_FLAG4);
						FISCBusiness1411.writeDebugLog("AA1412-Main",
								StringUtils.join("After InsertFEPTXNAndMSGOUT rtnCode=" , _rtnCode.name()
										, StringUtils.leftPad(String.valueOf(_rtnCode), 4, '0')));
					}

				} catch (Exception ex) {
					getLogContext().setProgramException(ex);
					sendEMS(getLogContext());
				} finally {
					TxFISCData1411.getLogContext().setProgramFlowType(ProgramFlow.MsgOut1411);
					TxFISCData1411.getLogContext().setMessage(rtnMessage);
					TxFISCData1411.getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
					TxFISCData1411.getLogContext().setMessageFlowType(MessageFlow.Response);
					logMessage(Level.INFO, getLogContext());
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		} finally {
			getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getmTxFISCData().getLogContext().setMessage(rtnMessage);
			getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.INFO, getLogContext());
		}

		return "";
	}

	private FEPReturnCode checkFISCRMReq() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			// (2) 檢核財金電文 Header
			rtnCode = getmFISCBusiness().checkRMHeader(true, MessageFlow.Request);

			// (3) 財金中文轉換
			// CHINESE_MEMO非必要欄位，不一定會有值
			if (getmFISCRMReq().getChineseMemo() != null && !getmFISCRMReq().getChineseMemo().equals("")) {
				RefString refChineseMemo = new RefString(getmFISCRMReq().getChineseMemo());
				boolean falg = getmFISCBusiness().convertFiscDecode(getmFISCRMReq().getChineseMemo(),refChineseMemo);
				getmFISCRMReq().setChineseMemo(refChineseMemo.get());
				if (!falg) {
					getmFISCRMReq().setChineseMemo("ＸＸＸＸＸ");
				}
			}
			RefString refALIASNAME = new RefString(getmFISCRMReq().getALIASNAME());
			boolean falg = getmFISCBusiness().convertFiscDecode(getmFISCRMReq().getALIASNAME(),refALIASNAME);
			getmFISCRMReq().setALIASNAME(refALIASNAME.get());
			if (!falg) {
				getmFISCRMReq().setALIASNAME("ＸＸＸＸＸ");
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
	private FEPReturnCode prepareAndInsertFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			// 2.Prepare() 交易記錄初始資料, 新增交易記錄(FEPTXN)
			rtnCode = getmFISCBusiness().prepareFEPTXNByRM(getmTxFISCData().getMsgCtl(), "0");

			// 新增交易記錄(FEPTXN )
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = getmFISCBusiness().insertFEPTxn();
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	// 4.CheckBusinessRule:商業邏輯檢核 & 電文Body檢核
	private FEPReturnCode checkBusinessRule(RefString repMAC, RefInt wkREPK, RefString wkRC, RefString repUNITBANK) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		try {
			rtnCode = getmFISCBusiness().checkBody("0", repMAC, wkREPK, wkRC, repUNITBANK);
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	// 5.Prepare一般通訊匯入主檔資料，新增一般通訊匯入主檔檔(MSGIN )
	private FEPReturnCode insertMSGIN(RefString wkFEPNO) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		MsginExtMapper dbMSGIN = SpringBeanFactoryUtil.getBean(MsginExtMapper.class);
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		//RmnoctlExtMapper dbRMNOCTL = SpringBeanFactoryUtil.getBean(RmnoctlExtMapper.class);
		try {
			rtnCode = getmFISCBusiness().prepareMSGIN(_defMSGIN, spCaller);

			if (rtnCode == CommonReturnCode.Normal) {
				dbMSGIN.insertSelective(_defMSGIN);
			}

			if (rtnCode == CommonReturnCode.Normal) {
				transactionManager.commit(txStatus);
				wkFEPNO.set(_defMSGIN.getMsginFepno());
			} else {
				transactionManager.rollback(txStatus);
				_defMSGIN = null;
			}

			return rtnCode;

		} catch (Exception ex) {

			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 6.準備回財金的相關資料並送回覆電文到財金(SendToFISC)
	private FEPReturnCode prepareAndSendForFISC(String repMAC) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// (1) 準備回財金的相關資料
		rtnCode = prepareForFISC(repMAC);
		if (rtnCode != CommonReturnCode.Normal) {
			debugMessage(StringUtils.join("After PrepareForFISC rtnCode=" , _rtnCode.name()
					, StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));
			return rtnCode;
		}

		// (2) UpdateTxData: 更新交易記錄(FEPTXN )
		updateFEPTXN();

		// 送回覆電文到財金(SendToFISC)
		RefString refMessage = new RefString(rtnMessage);
		getmFISCBusiness().sendRMResponseToFISC(refMessage);
		rtnMessage = refMessage.get();
		// 回寫MSGIN
		if (_defMSGIN != null) {
			updateMSGIN();
		}
		return rtnCode;
	}

	// 初始化1411對象
	private FEPReturnCode initBusiness1411() {
		LogData logData = new LogData();

		_rtnCode = CommonReturnCode.Normal;

		EJ1411 = TxHelper.generateEj();
		Stan1411 = getmFISCBusiness().getStan();

		// 紀錄Log
		logData.setProgramFlowType(ProgramFlow.MsgIn1411);
		logData.setProgramName(StringUtils.join(ProgramName, "initBusiness1411"));
		logData.setChannel(FEPChannel.FISC);
		logData.setSubSys(SubSystem.RM);
		logData.setMessageFlowType(MessageFlow.Request);
		logData.setMessage("");
		logData.setEj(EJ1411);
		logData.setStan(Stan1411);
		logData.setMessageId("141100");
		logData.setTxDate(getmTxFISCData().getLogContext().getTxDate());
		logContext = logData;

		FISCRMReq1411 = new FISC_RM();
		FISCRMRes1411 = new FISC_RM();

		TxFISCData1411 = new FISCData();
		TxFISCData1411.setTxObject(new FISCGeneral());
		TxFISCData1411.setTxChannel(getmTxFISCData().getTxChannel());
		TxFISCData1411.setTxSubSystem(getmTxFISCData().getTxSubSystem());
		TxFISCData1411.setFiscTeleType(getmTxFISCData().getFiscTeleType());
		TxFISCData1411.setTxRequestMessage("");
		TxFISCData1411.getTxObject().setRMRequest(FISCRMReq1411);
		TxFISCData1411.getTxObject().setRMResponse(FISCRMRes1411);
		TxFISCData1411.setEj(EJ1411);
		TxFISCData1411.setStan(Stan1411);
		TxFISCData1411.setMessageFlowType(MessageFlow.Request);
		TxFISCData1411.setLogContext(logData);

		// Get MsgID & MsgCtl
		TxFISCData1411.setMessageID("141100");
		TxFISCData1411.setStan(Stan1411);
		TxFISCData1411.setMsgCtl(getmTxFISCData().getMsgCtl());
		TxFISCData1411.setAaName(getmTxFISCData().getAaName());
		TxFISCData1411.setTxStatus(getmTxFISCData().isTxStatus());

		FISCGeneral1411 = TxFISCData1411.getTxObject();
		FISCGeneral1411.setEJ(EJ1411);
		FISCRMReq1411 = TxFISCData1411.getTxObject().getRMRequest();
		FISCRMRes1411 = TxFISCData1411.getTxObject().getRMResponse();
		FISCRMReq1411.setEj(EJ1411);
		// 建立FISC Business物件
		FISCBusiness1411 = new FISC(TxFISCData1411);
		FISCBusiness1411.setFeptxn(getFeptxn());
		FISCBusiness1411.setFeptxnDao(this.feptxnDao);
		FISCBusiness1411.setEj(TxFISCData1411.getEj());
		FISCBusiness1411.setLogContext(logData);

		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateRMSTAT() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		RmstatMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatMapper.class);
		Rmstat defRMSTAT = new Rmstat();

		try {
			defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMSTAT.setRmstatFiscoFlag4("N");

			if (dbRMSTAT.updateByPrimaryKey(defRMSTAT) != 1) {
				rtnCode = IOReturnCode.RMSTATUpdateError;
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
						TxFISCData1411.getTxChannel(), TxFISCData1411.getLogContext());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// 新增1411交易資料FEPTXN及MSGOUT
	private FEPReturnCode insertFEPTXNAndMSGOUT(int wkREPK, String wkRC, RefBoolean wkFISCO_FLAG4) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		MsgoutMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutMapper.class);
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		try {
			_defMSGOUT = new Msgout();
			rtnCode = getmFISCBusiness().prepareMSGOUT(wkREPK, wkRC, Stan1411, EJ1411, _defMSGOUT,
					spCaller, wkFISCO_FLAG4.get());

			if (rtnCode != CommonReturnCode.Normal) {
				transactionManager.rollback(txStatus);
				_defMSGOUT = null;
				return rtnCode;
			}

			dbMSGOUT.insertSelective(_defMSGOUT);

			transactionManager.commit(txStatus);

			// Candy Modify 2011-07-11, RMSTAT_FISCO_FLAG = 'N' Not insert FEPTXN,
			// Service1411 INSERT
			if ("Y".equals(defRMSTAT.getRmstatFisciFlag4())) {
				insertFEPTXN1411();
			}

			return rtnCode;

		} catch (Exception ex) {
			if(!txStatus.isCompleted()){
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 準備1411電文並送財金
	private FEPReturnCode prepareAndSend1411ToFisc() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 更新一般通訊匯出主檔之狀態匯出中
			_defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferring);
			rtnCode = updateMSGOUT_STAT();

			debugMessage(StringUtils.join("After UpdateMSGOUT_STAT rtnCode=" , rtnCode.toString()
					, StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			// 組1411Req電文
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = prepareRM1411Req();
			}

			debugMessage(StringUtils.join("After PrepareRM1411Req rtnCode=" , rtnCode.name()
					, StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			// 送1411電文至財金
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = FISCBusiness1411.sendRMRequestToFISC();
			}

			debugMessage(StringUtils.join("After SendRMRequestToFISC rtnCode=" , rtnCode.name()
					, StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			if (rtnCode != CommonReturnCode.Normal) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
						getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
				return rtnCode;
			} else {
				rtnMessage = FISCRMRes1411.getFISCMessage();
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (rtnCode != CommonReturnCode.Normal && rtnCode != CommonReturnCode.ProgramException) {
				TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
						getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
			}
		}
		return rtnCode;
	}

	// 更新一般通訊匯出主檔之狀態06-已傳送或恢復為02-已發訊
	private FEPReturnCode updateMSGOUTBy1411Res() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		Msgout defMSGOUT = new Msgout();
		MsgoutMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutMapper.class);
		Msgoute defMSGOUTE = new Msgoute();
		MsgouteMapper dbMSGOUTE = SpringBeanFactoryUtil.getBean(MsgouteMapper.class);
		Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		try {
			defMSGOUT.setMsgoutTxdate(_defMSGOUT.getMsgoutTxdate());
			defMSGOUT.setMsgoutBrno(_defMSGOUT.getMsgoutBrno());
			defMSGOUT.setMsgoutFepno(_defMSGOUT.getMsgoutFepno());
			defMSGOUT = dbMSGOUT.selectByPrimaryKey(defMSGOUT.getMsgoutTxdate(), defMSGOUT.getMsgoutBrno(), defMSGOUT.getMsgoutFepno());
			_defMSGOUT.setMsgoutFiscRtnCode(FISCRMRes1411.getResponseCode());
			defMSGOUT.setMsgoutFiscRtnCode(FISCRMRes1411.getResponseCode());

			if (NormalRC.FISC_OK.equals(FISCRMRes1411.getResponseCode())) {
				_defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferred);
				defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferred);
			} else {

				copyMSGOUTToMSGOUTE(_defMSGOUT, defMSGOUTE);

				dbMSGOUTE.insert(defMSGOUTE);

				_defMSGOUT.setMsgoutFepsubno(String.valueOf(Double.parseDouble(_defMSGOUT.getMsgoutFepsubno()) + 1));
				if ("0202".equals(FISCRMRes1411.getResponseCode())) // FISC CHECKOUT
				{
					_defMSGOUT.setMsgoutStat(MSGOUTStatus.FISCRefuse);// 07 財金拒絕
				} else {
					if ("5001".equals(FISCRMRes1411.getResponseCode())
							|| "0".equals(FISCRMRes1411.getResponseCode().substring(0, 1))
							|| "6".equals(FISCRMRes1411.getResponseCode().substring(0, 1))) {
						_defMSGOUT.setMsgoutStat(MSGOUTStatus.SystemProblem);
					} else {
						_defMSGOUT.setMsgoutStat(MSGOUTStatus.FISCRefuse); // 07
					}
				}

				defRMFISCOUT4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMFISCOUT4.setRmfiscout4ReceiverBank("950");
				spCaller.decrementRMFISCOUT4NO(defRMFISCOUT4);
				defMSGOUT.setMsgoutFepsubno(_defMSGOUT.getMsgoutFepsubno());
				defMSGOUT.setMsgoutStat(_defMSGOUT.getMsgoutStat());
			}

			if (dbMSGOUT.updateByPrimaryKeySelective(defMSGOUT) != 1) {
				transactionManager.rollback(txStatus);
				TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.MSGOUTUpdateError.getValue()), FEPChannel.FEP,
						TxFISCData1411.getTxChannel(), TxFISCData1411.getLogContext());
				return IOReturnCode.MSGOUTUpdateError;
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	// 更新匯入通匯序號檔(RMINSNO)之CHKG為換KEY 成功, 及換KEY次數
	private FEPReturnCode updateRMINSNO() {
		Rminsno defRMINSNO = new Rminsno();
		RminsnoExtMapper dbRMINSNO = SpringBeanFactoryUtil.getBean(RminsnoExtMapper.class);
		int i = 0;
		try {
			defRMINSNO.setRminsnoSenderBank(_defMSGIN.getMsginSenderBank().substring(0, 3));
			defRMINSNO.setRminsnoReceiverBank(_defMSGIN.getMsginReceiverBank().substring(0, 3));
			defRMINSNO.setRminsnoChgk("0");

			debugMessage(StringUtils.join("into UpdateRMINSNO RMINSNO_SENDER_BANK=" , defRMINSNO.getRminsnoSenderBank()));
			debugMessage(
					StringUtils.join("into UpdateRMINSNO RMINSNO_RECEIVER_BANK=" , defRMINSNO.getRminsnoReceiverBank()));
			debugMessage(StringUtils.join("into UpdateRMINSNO RMINSNO_CHGK=" , defRMINSNO.getRminsnoChgk()));
			i = dbRMINSNO.updateRMINSNOforCHKG(defRMINSNO);
			debugMessage(StringUtils.join("into UpdateRMINSNO iRet=" , String.valueOf(i)));
			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 更新交易記錄(FEPTXN)
	private FEPReturnCode updateFEPTXN1411() {
		FeptxnExt defFEPTXN = new FeptxnExt();

		try {
			defFEPTXN.setFeptxnTxDate(feptxn.getFeptxnTxDate());
			defFEPTXN.setFeptxnEjfno(feptxn.getFeptxnEjfno());
			defFEPTXN.setFeptxnAaRc(_rtnCode.getValue());
			defFEPTXN.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);
			feptxnDao.updateByPrimaryKey(defFEPTXN);
			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 組送財金Request
	 */
	private FEPReturnCode prepareForFISC(String repMAC) {
		// (1) 判斷 RC
		getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		getFeptxn().setFeptxnRepRc(getmFISCBusiness().getFISCRCFromReturnCode(_rtnCode));

		// (2) 產生 RESPONSE 電文訊息
		RefString refMAC = new RefString(repMAC);
		return getmFISCBusiness().prepareResponseForRM(refMAC);
	}

	/**
	 * 更新FEPTXN
	 * 
	 * @return
	 */
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;

		if (NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
			feptxn.setFeptxnTxrust(FeptxnTxrust.Successed);
		} else {
			feptxn.setFeptxnTxrust(FeptxnTxrust.Reverse);
		}
		feptxn.setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // FISC Response

		rtnCode = getmFISCBusiness().updateFepTxnForRM(feptxn);
		if (rtnCode != CommonReturnCode.Normal) {
			TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP,
					getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
			return rtnCode;
		}

		return rtnCode;
	}

	// 回寫MSGIN 紀錄MSGIN_FISC_RTN_CODE,MSGIN_STAT
	private FEPReturnCode updateMSGIN() {
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		Msgin defMSGIN = new Msgin();
		MsginMapper dbMSGIN = SpringBeanFactoryUtil.getBean(MsginMapper.class);
		Rmfiscin4 defRMFISCIN4 = new Rmfiscin4();
		Rmfiscin4Mapper dbRMFISCIN4 = SpringBeanFactoryUtil.getBean(Rmfiscin4Mapper.class);
		try {
			defMSGIN.setMsginTxdate(_defMSGIN.getMsginTxdate());
			defMSGIN.setMsginBrno(_defMSGIN.getMsginBrno());
			defMSGIN.setMsginFepno(_defMSGIN.getMsginFepno());
			defMSGIN = dbMSGIN.selectByPrimaryKey(defMSGIN.getMsginTxdate(), defMSGIN.getMsginBrno(), defMSGIN.getMsginFepno());
			if (defMSGIN != null) {
				defMSGIN.setMsginFiscRtnCode(feptxn.getFeptxnRepRc());
				if (!NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
					defMSGIN.setMsginStat(MSGINStatus.FEPCheckError);
				}
				dbMSGIN.updateByPrimaryKeySelective(defMSGIN);
			}
		
			if (NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
				defRMFISCIN4.setRmfiscin4SenderBank(SysStatus.getPropertyValue().getSysstatFbkno());
				defRMFISCIN4.setRmfiscin4ReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMFISCIN4.setRmfiscin4No(Integer.parseInt(_defMSGIN.getMsginFiscsno()));
				if (dbRMFISCIN4.updateByPrimaryKeySelective(defRMFISCIN4) != 1) {
					transactionManager.rollback(txStatus);
					TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.RMFISCIN4UpdateError.getValue()),
							FEPChannel.FEP, getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
				}
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	private FEPReturnCode insertFEPTXN1411() {
		try {
			if (FISCBusiness1411.prepareFEPTXNBy1411(_defMSGOUT, EJ1411, Stan1411) == CommonReturnCode.Normal) {
				this.feptxnDao.setTableNameSuffix(feptxn.getFeptxnTbsdy().substring(6, 8),
						StringUtils.join(ProgramName, "insertFEPTXN1411"));
				FISCBusiness1411.setFeptxnDao(feptxnDao);
				feptxnDao.insert(feptxn);
			}
			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode updateMSGOUT_STAT() {
		Msgout defMSGOUT = new Msgout();
		MsgoutMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutMapper.class);
		try {
			defMSGOUT.setMsgoutTxdate(_defMSGOUT.getMsgoutTxdate());
			defMSGOUT.setMsgoutBrno(_defMSGOUT.getMsgoutBrno());
			defMSGOUT.setMsgoutFepno(_defMSGOUT.getMsgoutFepno());			
			defMSGOUT = dbMSGOUT.selectByPrimaryKey(defMSGOUT.getMsgoutTxdate(), defMSGOUT.getMsgoutBrno(), defMSGOUT.getMsgoutFepno());
			if (defMSGOUT != null) {
				defMSGOUT.setMsgoutStat(_defMSGOUT.getMsgoutStat());
				if (dbMSGOUT.updateByPrimaryKeySelective(defMSGOUT) == 1) {
					return CommonReturnCode.Normal;
				} else {
					TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.MSGOUTUpdateError.getValue()), FEPChannel.FEP,
							getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
					return IOReturnCode.MSGOUTUpdateError;
				}
			} else {
				TxHelper.getRCFromErrorCode(String.valueOf(IOReturnCode.MSGOUTUpdateError.getValue()), FEPChannel.FEP,
						getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
				return IOReturnCode.MSGOUTUpdateError;
			}
			
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode prepareRM1411Req() {
		RefString reqMAC = new RefString("");
		ENCHelper encHelper = null;
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		try {
			// 組header
			FISCRMReq1411.setSystemSupervisoryControlHeader("00");
			FISCRMReq1411.setSystemNetworkIdentifier("00");
			FISCRMReq1411.setAdderssControlField("00");
			FISCRMReq1411.setMessageType("0200");
			FISCRMReq1411.setProcessingCode("1411");
			debugMessage("111111111111111111");
			FISCRMReq1411.setSystemTraceAuditNo(feptxn.getFeptxnStan());
			debugMessage(StringUtils.join("FepTxn.FEPTXN_STAN=" , feptxn.getFeptxnStan()));
			FISCRMReq1411.setTxnDestinationInstituteId(StringUtils.rightPad("950", 7, '0'));
			FISCRMReq1411.setTxnSourceInstituteId(
					StringUtils.rightPad(_defMSGOUT.getMsgoutSenderBank().substring(0, 3), 7, '0'));
			debugMessage(StringUtils.join("_defMSGOUT.MSGOUT_SENDER_BANK=" , _defMSGOUT.getMsgoutSenderBank()));
			FISCRMReq1411.setTxnInitiateDateAndTime(
					CalendarUtil.adStringToROCString(feptxn.getFeptxnTxDate()).substring(1, 7)
							+ feptxn.getFeptxnTxTime()); // (轉成民國年)
			debugMessage(StringUtils.join("FepTxn.FEPTXN_TX_DATE=" , feptxn.getFeptxnTxDate()));
			FISCRMReq1411.setResponseCode(NormalRC.FISC_REQ_RC);
			FISCRMReq1411
					.setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(), 8, ' '));
			debugMessage(StringUtils.join(
					"SysStatus.PropertyValue.SYSSTAT_TRMSYNC=" , SysStatus.getPropertyValue().getSysstatTrmsync()));
			// 組Body
			FISCRMReq1411.setFiscNo(_defMSGOUT.getMsgoutFiscsno());
			debugMessage(StringUtils.join("_defMSGOUT.MSGOUT_FISCSNO=" , _defMSGOUT.getMsgoutFiscsno()));

			FISCRMReq1411.setSenderBank(_defMSGOUT.getMsgoutSenderBank());
			debugMessage(StringUtils.join("FISCRMReq1411.SENDER_BANK=" , FISCRMReq1411.getSenderBank()));
			FISCRMReq1411.setReceiverBank(_defMSGOUT.getMsgoutReceiverBank());
			debugMessage(StringUtils.join("FISCRMReq1411.RECEIVER_BANK=" , FISCRMReq1411.getReceiverBank()));
			RefString memo = new RefString(FISCRMReq1411.getChineseMemo());
			if (getmFISCBusiness().convertFiscEncode(_defMSGOUT.getMsgoutChnmemo(), memo)) {
				FISCRMReq1411.setChineseMemo(
						StringUtil.convertFromAnyBaseString(String.valueOf((memo.get().length() + 4) / 2.0), 10, 16, 4)
								+ memo.get());
			} else {
				return RMReturnCode.ChineseRowError;
			}
			debugMessage(StringUtils.join(
					"FISCRMReq1411.CHINESE_MEMO=" , _defMSGOUT.getMsgoutChnmemo() , FISCRMReq1411.getChineseMemo()));
			FISCRMReq1411.setEnglishMemo("0011" + StringUtil.toHex(_defMSGOUT.getMsgoutEngmemo()));

			debugMessage(StringUtils.join(
					"FISCRMReq1411.ENGLISH_MEMO=" , _defMSGOUT.getMsgoutEngmemo() , FISCRMReq1411.getEnglishMemo()));

			// 產生MAC
			encHelper = new ENCHelper(TxFISCData1411);
			rtnCode = encHelper.makeRMFISCMAC(reqMAC);

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			FISCRMReq1411.setMAC(StringUtils.leftPad(reqMAC.get(), 8, '0'));

			// MakeBitmap
			rtnCode = FISCBusiness1411.makeBitmap(FISCRMReq1411.getMessageType(), FISCRMReq1411.getProcessingCode(),
					MessageFlow.Request);
			debugMessage(String.format("after MakeBitmap rtnCode=" + rtnCode.name()
					+ StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			if (rtnCode != CommonReturnCode.Normal) {
				TxHelper.getRCFromErrorCode(String.valueOf(CommonReturnCode.Abnormal.getValue()), FEPChannel.FEP,
						TxFISCData1411.getTxChannel(), TxFISCData1411.getLogContext());
				updateRMSTAT();
				return rtnCode;
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	private FEPReturnCode copyMSGOUTToMSGOUTE(Msgout defMSGOUT, Msgoute defMSGOUTE) {
		defMSGOUTE.setMsgouteTxdate(defMSGOUT.getMsgoutTxdate());
		defMSGOUTE.setMsgouteBrno(defMSGOUT.getMsgoutBrno());
		defMSGOUTE.setMsgouteFepno(defMSGOUT.getMsgoutFepno());
		defMSGOUTE.setMsgouteFepsubno(defMSGOUT.getMsgoutFepsubno());
		defMSGOUTE.setMsgouteCategory(defMSGOUT.getMsgoutCategory());
		defMSGOUTE.setMsgouteSenderBank(defMSGOUT.getMsgoutSenderBank());
		defMSGOUTE.setMsgouteReceiverBank(defMSGOUT.getMsgoutReceiverBank());
		defMSGOUTE.setMsgouteStan(defMSGOUT.getMsgoutStan());
		defMSGOUTE.setMsgouteFiscsno(defMSGOUT.getMsgoutFiscsno());
		// defMSGOUTE.MSGOUTE_RMSNO = defMSGOUT.MSGOUT_RMSNO
		defMSGOUTE.setMsgouteStat(defMSGOUT.getMsgoutStat());
		defMSGOUTE.setMsgouteOwpriority(defMSGOUT.getMsgoutOwpriority());
		defMSGOUTE.setMsgouteRegdate(defMSGOUT.getMsgoutRegdate());
		defMSGOUTE.setMsgouteRegtime(defMSGOUT.getMsgoutRegtime());
		defMSGOUTE.setMsgouteApdate(defMSGOUT.getMsgoutApdate());
		defMSGOUTE.setMsgouteAptime(defMSGOUT.getMsgoutAptime());
		defMSGOUTE.setMsgouteOrderdate(defMSGOUT.getMsgoutOrderdate());
		defMSGOUTE.setMsgouteSenddate(defMSGOUT.getMsgoutSenddate());
		defMSGOUTE.setMsgouteSendtime(defMSGOUT.getMsgoutSendtime());
		defMSGOUTE.setMsgouteFiscRtnCode(defMSGOUT.getMsgoutFiscRtnCode());
		defMSGOUTE.setMsgouteChnmemo(defMSGOUT.getMsgoutChnmemo());
		defMSGOUTE.setMsgouteEngmemo(defMSGOUT.getMsgoutEngmemo());
		defMSGOUTE.setMsgouteSupno1(defMSGOUT.getMsgoutSupno1());
		defMSGOUTE.setMsgouteSupno2(defMSGOUT.getMsgoutSupno2());
		defMSGOUTE.setMsgouteRegTlrno(defMSGOUT.getMsgoutRegTlrno());
		defMSGOUTE.setMsgouteFiscSndCode(defMSGOUT.getMsgoutFiscSndCode());
		defMSGOUTE.setMsgouteEjno(defMSGOUT.getMsgoutEjno());
		defMSGOUTE.setUpdateUserid(defMSGOUT.getUpdateUserid());
		defMSGOUTE.setUpdateTime(defMSGOUT.getUpdateTime());

		return CommonReturnCode.Normal;
	}
}
