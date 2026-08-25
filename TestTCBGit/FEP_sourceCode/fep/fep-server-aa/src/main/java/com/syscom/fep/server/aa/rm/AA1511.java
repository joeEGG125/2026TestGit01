package com.syscom.fep.server.aa.rm;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.FISC_Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
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
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.invoker.FEPInvoker;
import com.syscom.fep.mybatis.ext.mapper.AllbankExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmbtchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmoutExtMapper;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.mapper.Rmfiscout1Mapper;
import com.syscom.fep.mybatis.mapper.Rmfiscout4Mapper;
import com.syscom.fep.mybatis.mapper.RmoutsnoMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.mybatis.model.Rmbtch;
import com.syscom.fep.mybatis.model.Rmfiscout1;
import com.syscom.fep.mybatis.model.Rmfiscout4;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Rmoutt;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.constant.RMOUT_ORIGINAL;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.rm.RMGeneral;

public class AA1511 extends RMAABase {
	private String rtnMessage = "";
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private Rmout _defRMOUT;
	@SuppressWarnings("unused")
	private Msgout _defMSGOUT;

	public AA1511(FISCData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * 程式進入點
	 * 
	 * @return Response電文
	 * 
	 */
	@Override
	public String processRequestData() {
		@SuppressWarnings("unused")
		String repMAC = "";
		String reqSTAT = "";
		String reqRMSNO = "";
		String reqRECEIVER_BANK = "";
		@SuppressWarnings("unused")
		String repUNITBANK = "";
		@SuppressWarnings("unused")
		String wkSTATUS = "";

		try {
			getLogContext().setBkno(SysStatus.getPropertyValue().getSysstatHbkno());
			// '1. Prepare() 交易記錄初始資料，新增交易記錄(FEPTXN )
			// _rtnCode = PrepareAndInsertFEPTXN()
			// DebugMessage(String.Format("After PrepareAndInsertFEPTXN rtnCode={0}{1}", _rtnCode.ToString(), CType(_rtnCode, String).PadLeft(4, "0"c)))

			// 2.CheckOutData: 匯出主檔(RMOUT)或一般通訊匯出主檔(MSGOUT)檢核
			if (_rtnCode == CommonReturnCode.Normal) {
				RefString refStat = new RefString(reqSTAT);
				RefString refRmsno = new RefString(reqRMSNO);
				RefString refReceiverBank = new RefString(reqRECEIVER_BANK);
				_rtnCode = checkOutData(refStat, refRmsno, refReceiverBank);
				reqSTAT = refStat.get();
				reqRMSNO = refRmsno.get();
				reqRECEIVER_BANK = refReceiverBank.get();
			}
			getLogContext().setRemark(StringUtils.join("After CheckOutData, rtnCode=", _rtnCode.toString(), ", reqStat=", reqSTAT, ", reqRMSNO=", reqRMSNO, ", reqRECEIVER_BANK=", reqRECEIVER_BANK));
			logMessage(Level.DEBUG, getLogContext());

			// 3.組送往 FISC 之 Request 電文並等待財金之 Response
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = prepareAndSendForFISC();
			}
			LogHelperFactory.getTraceLogger().trace(StringUtils.join("after PrepareAndSendForFISC, rtnCode=", _rtnCode.toString()));

			// 4.CheckResponseFromFISC:檢核回應電文是否正確
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getmFISCBusiness().checkResponseFromFISC();
			}
			if (getmFISCRMRes() != null && getmFISCRMRes().getSTATUS() != null) {
				getLogContext().setRemark(StringUtils.join("After CheckResponseFromFISC, rtnCode=", _rtnCode.toString(), ", FISCRMRes.ResponseCode=", getmFISCRMRes().getResponseCode(),
						", FISCRMRes.STATUS=", getmFISCRMRes().getSTATUS()));
			} else {
				getLogContext().setRemark(StringUtils.join("After CheckResponseFromFISC, rtnCode=", _rtnCode.toString(), ", FISCRMRes.ResponseCode=", getmFISCRMRes().getResponseCode()));
			}
			logMessage(Level.DEBUG, getLogContext());

			// 5.更新電文序號/通匯序號/匯出主檔狀態及送更新多筆匯出狀態訊息至11X1-INQueue
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = updateREPNOAndSTATAndSend11X1INQueue(reqSTAT, reqRECEIVER_BANK, reqRMSNO);
			}
			LogHelperFactory.getTraceLogger()
					.trace(StringUtils.join("After UpdateREPNOAndSTATAndSend11X1INQueue rtnCode=", _rtnCode.toString(), StringUtils.leftPad(String.valueOf(_rtnCode.getValue()), 4, '0')));

			// '6.UpdateTxData: 更新交易記錄(FEPTXN )
			// UpdateFEPTXN()

			if (_rtnCode != CommonReturnCode.Normal) {
				// Modify by Jim, 2010/12/07, 不應更動FISC的回應
				// FISCRMRes.ResponseCode = CType(_rtnCode, String).PadLeft(4, "0"c)
				getmFISCRMRes().setSTATUS("");
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
		} finally {
			getmTxFISCData().getLogContext().setReturnCode(_rtnCode);
			getmTxFISCData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getmTxFISCData().getLogContext().setMessage(rtnMessage);
			getmTxFISCData().getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
			getmTxFISCData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.INFO, getLogContext());
			if (_rtnCode == CommonReturnCode.Normal) {// For UI028020
				getmTxFISCData().getTxObject().setDescription("處理成功");
			} else {
				getmTxFISCData().getTxObject().setDescription(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			}
		}

		return "";

	}

	/*
	 * 1.Prepare交易記錄初始資料, 新增交易記錄(FEPTXN )
	 */
	@SuppressWarnings("unused")
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
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;

	}

	/*
	 * 2.CheckOutData: 匯出主檔(RMOUT)或一般通訊匯出主檔(MSGOUT)檢核
	 */
	private FEPReturnCode checkOutData(RefString reqSTAT, RefString reqRMSNO, RefString reqRECEIVER_BANK) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Rmout defRMOUT = new Rmout();
		RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);

		Msgout defMSGOUT = new Msgout();
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);

		Allbank defALLBANK = new Allbank();
		AllbankExtMapper dbALLBANK = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);

		try {
			if ("1111,1121,1131,1171,1181,1191".indexOf(getmFISCRMReq().getOrgPcode()) > -1) {
				defRMOUT.setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defRMOUT.setRmoutSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMOUT.setRmoutFiscsno(getmFISCRMReq().getFiscNo());
				defRMOUT = dbRMOUT.getRMOUTForCheckOutData(defRMOUT);
				if (defRMOUT == null) {
					rtnCode = IOReturnCode.RMOUTNotFound;
				} else {
					_defRMOUT = defRMOUT;

					if (!getmFISCRMReq().getOrgPcode().substring(1, 3).equals(defRMOUT.getRmoutRemtype())) {
						rtnCode = RMReturnCode.TradePcodeAndHostNotMatch;
					} else {
						reqSTAT.set(defRMOUT.getRmoutStat());
						reqRMSNO.set(defRMOUT.getRmoutRmsno());
						// Jim, 2012/1/30, 改成用RMOUT_RECEIVER_BANK[1:3] + "000" =>ALLBANK PKEY查該筆之ALLBAK_UNIT_BANK
						// reqRECEIVER_BANK = Mid(defRMOUT.RMOUT_RECEIVER_BANK, 1, 3)
						defALLBANK.setAllbankBkno(defRMOUT.getRmoutReceiverBank().substring(0, 3));
						defALLBANK.setAllbankBrno("000");
						defALLBANK = dbALLBANK.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
						if (defALLBANK != null) {
							reqRECEIVER_BANK.set(defALLBANK.getAllbankUnitBank());
						} else {
							reqRECEIVER_BANK.set(defRMOUT.getRmoutReceiverBank().substring(0, 3));
						}
					}
				}
			} else {
				defMSGOUT.setMsgoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defMSGOUT.setMsgoutSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defMSGOUT.setMsgoutFiscsno(getmFISCRMReq().getFiscNo());
				defMSGOUT = dbMSGOUT.getMsgOutForCheckOutData(defMSGOUT);
				if (defMSGOUT == null) {
					rtnCode = IOReturnCode.MSGOUTNotFound;
				} else {
					_defMSGOUT = defMSGOUT;
					if (!getmFISCRMReq().getOrgPcode().equals("1411")) {
						rtnCode = RMReturnCode.TradePcodeAndHostNotMatch;
					} else {
						reqSTAT.set(defMSGOUT.getMsgoutStat());
						// reqRMSNO = defMSGOUT.MSGOUT_RMSNO
						reqRECEIVER_BANK.set(defMSGOUT.getMsgoutReceiverBank().substring(0, 3));
					}
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/*
	 * 3.組送往 FISC 之 Request 電文並等待財金之 Response
	 */
	private FEPReturnCode prepareAndSendForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		// (1) 準備回財金的相關資料
		rtnCode = prepareRMReq1511();
		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark(StringUtils.join("After PrepareForFISC rtnCode=", _rtnCode.toString()));
			sendEMS(getLogContext());
			return rtnCode;
		}

		// 送1511 Req電文到財金(SendToFISC) 並等待回復
		rtnCode = getmFISCBusiness().sendRMRequestToFISC();

		return rtnCode;
	}

	private FEPReturnCode prepareRMReq1511() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		String reqMAC = "";
		ENCHelper encHelper = null;

		try {
			// 組header()
			getmFISCRMReq().setSystemSupervisoryControlHeader("00");
			getmFISCRMReq().setSystemNetworkIdentifier("00");
			getmFISCRMReq().setAdderssControlField("00");
			getmFISCRMReq().setMessageType("0200");
			getmFISCRMReq().setProcessingCode("1511");
			getmFISCRMReq().setSystemTraceAuditNo(getmFISCBusiness().getStan());
			getmTxFISCData().setStan(getmFISCRMReq().getSystemTraceAuditNo());
			getmFISCRMReq().setTxnDestinationInstituteId(StringUtils.rightPad("950", 7, '0'));
			getmFISCRMReq().setTxnSourceInstituteId(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatHbkno(), 7, '0'));
			getmFISCRMReq().setTxnInitiateDateAndTime(CalendarUtil.adStringToROCString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).substring(1, 7)
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))); // (轉成民國年)
			getmFISCRMReq().setResponseCode(NormalRC.FISC_REQ_RC);
			getmFISCRMReq().setSyncCheckItem(StringUtils.leftPad(SysStatus.getPropertyValue().getSysstatTrmsync(), 8, ' '));

			// 組Body
			// 呼叫AA的時候就將FISC_NO/ORG_PCODE賦值

			// 產生MAC
			encHelper = new ENCHelper(getmTxFISCData());
			RefString refMac = new RefString(reqMAC);
			rtnCode = encHelper.makeRMFISCMAC(refMac);
			reqMAC = refMac.get();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			getmFISCRMReq().setMAC(StringUtils.leftPad(reqMAC, 8, '0'));

			// MakeBitmap
			rtnCode = getmFISCBusiness().makeBitmap(getmFISCRMReq().getMessageType(), getmFISCRMReq().getProcessingCode(), MessageFlow.Request);
			LogHelperFactory.getTraceLogger().trace(StringUtils.join("after MakeBitmap rtnCode=", rtnCode.toString(), StringUtils.leftPad(String.valueOf(rtnCode.getValue()), 4, '0')));

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	private FEPReturnCode updateREPNOAndSTATAndSend11X1INQueue(String reqSTAT, String reqRECEIVER_BANK, String repRMSNO) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);

		Queue11X1 msg11X1 = new Queue11X1();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// FISCRMRes.STATUS=01-財金未收到, 02-財金收但對方行未收到, 03-對方行已收到, 04-對方行已回訊財金

			// Modify by Jim, 2011/8/11, 改成先更新RMOUT，因為更新FISCOUTSNO和RMOUTSNO的方式改成查詢 RMOUT_FISC_RTN_CODE = 0001 且序號最大的那一筆來更新
			// (3) 更新匯款狀態
			if ("1411".equals(getmFISCRMReq().getOrgPcode())) {
				rtnCode = updateMSGOUTByFISCSNO(reqSTAT);

				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
					// Modify by Jim, 2011/10/25, 財金回OK時STATUS才會有值
					if ("01".equals(getmFISCRMRes().getSTATUS())) {
						// (1) 更新電文序號
						rtnCode = updateRMFISCOUT4(reqSTAT);
						LogHelperFactory.getTraceLogger().trace(StringUtils.join("UpdateRMFISCOUT4 rtnCode=", _rtnCode.toString()));
					}
				}
			} else {
				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode()) && RMOUTStatus.Transfered.equals(reqSTAT)) {

					rtnCode = updateRMOUTByFISCSNO(_defRMOUT, reqSTAT);

					// Modify by Jim, 2011/12/20, 還需要判斷是否有更新到RMOUT & RMOUTT (確保此時狀態仍然是傳送中)才能再做ProcessAPTOTAndRMTOTAndRMTOTAL
					if (rtnCode == CommonReturnCode.Normal) {
						switch (getmFISCRMRes().getSTATUS()) {
							case "02":
							case "03":
							case "04":
								getFeptxn().setFeptxnTxAmt(_defRMOUT.getRmoutTxamt());
								getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
								getFeptxn().setFeptxnSenderBank(_defRMOUT.getRmoutSenderBank());
								getFeptxn().setFeptxnReceiverBank(_defRMOUT.getRmoutReceiverBank());
								getFeptxn().setFeptxnTxFeeCr(BigDecimal.valueOf(_defRMOUT.getRmoutPostamt()));
								getFeptxn().setFeptxnPcode(getmFISCRMReq().getOrgPcode());
								getmFISCRMReq().setProcessingCode(getmFISCRMReq().getOrgPcode());
								// Modify by CANDY 2011-09-27 PCODE =1171 RMOUT_AMT_TYPE IS NULL
								if ("1171".equals(_defRMOUT.getRmoutFiscSndCode()) || _defRMOUT.getRmoutAmtType() == null) {
									rtnCode = getmFISCBusiness().processAPTOTAndRMTOTAndRMTOTAL("002", getFeptxn());
								} else {
									rtnCode = getmFISCBusiness().processAPTOTAndRMTOTAndRMTOTAL(_defRMOUT.getRmoutAmtType(), getFeptxn());
								}

								getLogContext().setRemark(StringUtils.join("AA1511, After ProcessAPTOTAndRMTOTAndRMTOTAL, rtnCode=", rtnCode.toString(), ", FISCRMRes.ResponseCode=",
										getmFISCRMRes().getResponseCode(), ", FISCRMReq.ORG_PCODE=", getmFISCRMReq().getOrgPcode(), ", reqStat=", reqSTAT));
								logMessage(Level.DEBUG, getLogContext());
								break;
							default:
								// 不記帳
								break;
						}
					}

					// Jim, 2012/2/21, 改成要判斷RMOUT & RMOUTT是否更新成功才要更新FISCOUTSNO, RMOUTSNO
					// Jim, 2012/2/21, 更新序號前先檢查是不是有狀態為傳送中的資料
					if (rtnCode == CommonReturnCode.Normal) {
						Rmout tmpRMOUT = new Rmout();
						tmpRMOUT.setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
						tmpRMOUT.setRmoutStat(RMOUTStatus.Transfered);
						List<Rmout> tmpDt = dbRMOUT.getRmoutByDef(tmpRMOUT);
						if (tmpDt.size() > 0) {
							getLogContext().setRemark(StringUtils.join("查到狀態為傳送中的資料", tmpDt.size(), "筆, 不自動更新電文序號和通匯序號"));
							logMessage(Level.DEBUG, getLogContext());
						} else {
							// Modify by Jim, 改成不用判斷狀態都要更新FISCOUTSNO, RMOUTSNO
							// (1) 更新電文序號
							rtnCode = updateRMFISCOUT1(reqSTAT);

							// (2) 更新通匯序號
							if (rtnCode == CommonReturnCode.Normal) {
								rtnCode = updateRMOUTSNO(reqSTAT, reqRECEIVER_BANK, repRMSNO);
							}
						}
					}
				}
			}

			if (rtnCode == CommonReturnCode.Normal) {
				transactionManager.commit(txStatus);
			} else {
				getLogContext().setRemark(StringUtils.join("AA1511-UpdateREPNOAndSTATAndSend11X1INQueue Rollback, rtnCode=", rtnCode.toString()));
				logMessage(Level.DEBUG, getLogContext());
				transactionManager.rollback(txStatus);
			}

			if (!"1411".equals(getmFISCRMReq().getOrgPcode())) {
				// modified by 榮升 2020/07/31 改用Syscom.FEP10.Common.Config.RMConfig
				// INQueue11X1 = New MessageQueue(RMConfig.Instance().INQueue11X1)
				// Modify by Jim, 2011/10/25, 改成用FISCRMReq的FISC_NO避免回應電文的欄位是null
				msg11X1.setMsgFISCNO(getmFISCRMReq().getFiscNo());
				// Fly 2020/11/17 跨行業務基金與大額資金通報管理機制
				msg11X1.setMsgAMT(_defRMOUT.getRmoutTxamt().longValue());
				// Modify by Jim, 2011/10/25, 財金回OK時STATUS才會有值
				if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode())) {
					switch (getmFISCRMRes().getSTATUS()) {
						case "01":
							if ("1171".equals(getmFISCRMReq().getOrgPcode())) {
								msg11X1.setMsgSTAT(RMOUTStatus.BackExchange); // 退匯
							} else {
								msg11X1.setMsgSTAT(RMOUTStatus.Passed); // (已放行)
							}
							break;
						case "02":
						case "03":
						case "04":
							msg11X1.setMsgSTAT(RMOUTStatus.GrantMoney);
							break;
						default:
							msg11X1.setMsgSTAT("");
							break;
					}
				} else {
					msg11X1.setMsgSTAT(RMOUTStatus.Passed); // (已放行)
				}
				FEPInvoker invoker = SpringBeanFactoryUtil.getBean(FEPInvoker.class);
				invoker.sendReceiveToInQueue11X1(
						StringUtils.join(StringUtils.leftPad(msg11X1.getMsgFISCNO(), 7, '0'), StringUtils.leftPad(msg11X1.getMsgSTAT(), 2, '0'), msg11X1.getMsgAMT()),
						FEPConfig.getInstance().getRestfulTimeout());
				getLogContext().setRemark(StringUtils.join("AA1511--UpdateREPNOAndSTATAndSend11X1INQueue, Send Message to INQueue11X1, queue.FISCSNO=", msg11X1.getMsgFISCNO(), ", queue.STAT=",
						msg11X1.getMsgSTAT(), ", queue.MSG_AMT=", msg11X1.getMsgAMT()));
				logMessage(Level.DEBUG, getLogContext());
			}

			// 2021/02/04 Modify by Candy for PSP PCR
			// If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso FISCRMReq.ORG_PCODE.Substring(0, 2) = "11" AndAlso
			// reqSTAT = "05" AndAlso (_defRMOUT.RMOUT_ORIGINAL = RMOUT_ORIGINAL.FEDI OrElse _defRMOUT.RMOUT_ORIGINAL = RMOUT_ORIGINAL.MMAB2B) Then
			// '組AASYNCFEDI Request 電文/ MSGCTL_MSGID=”SYNCFEDI”
			// rtnCode = CallSYNCFEDI(_defRMOUT)
			// End If
			if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode()) && "11".equals(getmFISCRMReq().getOrgPcode().substring(0, 2)) && "05".equals(reqSTAT)
					&& (RMOUT_ORIGINAL.FEDI.equals(_defRMOUT.getRmoutOriginal()) || RMOUT_ORIGINAL.MMAB2B.equals(_defRMOUT.getRmoutOriginal())
							|| RMOUT_ORIGINAL.PSP.equals(_defRMOUT.getRmoutOriginal()))) {
				// 組AASYNCFEDI Request 電文/ MSGCTL_MSGID=”SYNCFEDI”
				rtnCode = callSYNCFEDI(_defRMOUT);
			}

			return rtnCode;
		} catch (Exception ex) {
			if (!txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (!txStatus.isCompleted()) {
				transactionManager.commit(txStatus);
			}
		}
	}

	/**
	 * <modify>
	 * <modifier>Kyo</modifier>
	 * <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
	 * <date>2010/03/16</date>
	 * </modify>
	 */
	private FEPReturnCode updateRMFISCOUT1(String reqSTAT) {
		Rmfiscout1Mapper dbRMFISCOUT1 = SpringBeanFactoryUtil.getBean(Rmfiscout1Mapper.class);
		RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
		// Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
		Rmfiscout1 defRMFISCOUT1_999 = new Rmfiscout1();

		try {
			// Modify by Jim, 2011/12/20, 此判斷移到外面
			// If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then

			defRMFISCOUT1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMFISCOUT1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());

			defRMFISCOUT1_999.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMFISCOUT1_999.setRmfiscout1ReceiverBank("999");
			defRMFISCOUT1 = dbRMFISCOUT1.selectByPrimaryKey(defRMFISCOUT1.getRmfiscout1SenderBank(), defRMFISCOUT1.getRmfiscout1ReceiverBank());
			defRMFISCOUT1_999 = dbRMFISCOUT1.selectByPrimaryKey(defRMFISCOUT1_999.getRmfiscout1SenderBank(), defRMFISCOUT1_999.getRmfiscout1ReceiverBank());
			if (defRMFISCOUT1 == null || defRMFISCOUT1_999 == null) {
				rtnCode = IOReturnCode.RMFISCOUT1NotFound;
			} else {
				getLogContext().setRemark(
						StringUtils.join("AA1511, Before UpdateRMFISCOUT1, RMFISCOUT1_NO=", defRMFISCOUT1.getRmfiscout1No(), ", RMFISCOUT1_999_REP_NO=", defRMFISCOUT1_999.getRmfiscout1RepNo()));
				logMessage(Level.DEBUG, getLogContext());

				if (!defRMFISCOUT1_999.getRmfiscout1RepNo().equals(defRMFISCOUT1.getRmfiscout1No())) {
					// Modify by Jim, 2011/8/10, 多筆匯出因REP_NO UPDATE不一致, 改直接抓取匯出成功的最大電文序號
					Rmoutt defRMOUTT = new Rmoutt();
					defRMOUTT.setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
					defRMOUTT.setRmouttFiscRtnCode(NormalRC.FISC_OK);
					defRMOUTT.setRmouttFepno(null);
					List<Rmoutt> dtRMOUTT = dbRMOUTT.getRMOUTTByDef(defRMOUTT);
					LogHelperFactory.getTraceLogger().info("dtRMOUTT.size:", dtRMOUTT.size());
					if (dtRMOUTT.size() < 1) {
						// rtnCode = IOReturnCode.RMOUTNotFound
						// LogContext.Remark = "AA1511 更新RMFISCOUT1時查詢RMOUT查無資料"
						// LogContext.ReturnCode = IOReturnCode.RMOUTNotFound
						// SendEMS(LogContext)
						// Return rtnCode
						// 如果找不到成功的RMOUT資料，則更新成0
						defRMFISCOUT1.setRmfiscout1No(0);
						// defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
						defRMFISCOUT1_999.setRmfiscout1RepNo(0);
					} else {
						List<Rmoutt> drRMOUT = dtRMOUTT.stream().sorted(Comparator.comparing(Rmoutt::getRmouttFiscsno).reversed()).collect(Collectors.toList());
						LogHelperFactory.getTraceLogger().info("drRMOUT.size:", drRMOUT.size());
						if (drRMOUT.size() > 0) {
							String maxFISCSNO = drRMOUT.get(0).getRmouttFiscsno();
							defRMFISCOUT1.setRmfiscout1No(new BigDecimal(maxFISCSNO).intValue());
							// defRMFISCOUT1.RMFISCOUT1_REP_NO = defRMFISCOUT1.RMFISCOUT1_NO
							defRMFISCOUT1_999.setRmfiscout1RepNo(defRMFISCOUT1.getRmfiscout1No());
						} else {
							// 如果找不到成功的RMOUT資料，則更新成0
							defRMFISCOUT1.setRmfiscout1No(0);
							// defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
							defRMFISCOUT1_999.setRmfiscout1RepNo(0);
						}
					}

					if (dbRMFISCOUT1.updateByPrimaryKeySelective(defRMFISCOUT1) < 1) {
						rtnCode = IOReturnCode.RMFISCOUT1UpdateError;
						getLogContext().setRemark("AA1511, 更新RMFISCOUT1 0 筆");
						getLogContext().setReturnCode(rtnCode);
						sendEMS(getLogContext());
					} else {
						if (dbRMFISCOUT1.updateByPrimaryKeySelective(defRMFISCOUT1_999) < 1) {
							rtnCode = IOReturnCode.RMFISCOUT1UpdateError;
							getLogContext().setRemark("AA1511, 更新RMFISCOUT1_999 0 筆");
							getLogContext().setReturnCode(rtnCode);
							sendEMS(getLogContext());
						} else {
							getLogContext().setRemark(StringUtils.join("AA1511, After UpdateRMFISCOUT1, RMFISCOUT1_NO=", defRMFISCOUT1.getRmfiscout1No(), "RMFISCOUT1_999_REP_NO=",
									defRMFISCOUT1_999.getRmfiscout1RepNo()));
							logMessage(Level.DEBUG, getLogContext());
						}
					}
				}
			}
			// End If
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * <modify>
	 * <modifier>Kyo</modifier>
	 * <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
	 * <date>2010/03/16</date>
	 * </modify>
	 */
	private FEPReturnCode updateRMFISCOUT4(String reqSTAT) {
		Rmfiscout4Mapper dbRMFISCOUT4 = SpringBeanFactoryUtil.getBean(Rmfiscout4Mapper.class);
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();
		try {
			if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode()) && MSGOUTStatus.Transferring.equals(reqSTAT)) {
				defRMFISCOUT4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
				defRMFISCOUT4.setRmfiscout4ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());

				Msgout defMSGOUT = new Msgout();
				defMSGOUT.setMsgoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				List<Msgout> dtMSGOUT = dbMSGOUT.getMsgOutByDef(defMSGOUT);
				dtMSGOUT = dtMSGOUT.stream().filter(item -> item.getMsgoutFiscRtnCode().equals("0001")).sorted(Comparator.comparing(Msgout::getMsgoutFiscsno).reversed()).collect(Collectors.toList());
				if (dtMSGOUT.size() > 0 && StringUtils.isNotBlank(dtMSGOUT.get(0).getMsgoutFiscsno())) {
					defRMFISCOUT4.setRmfiscout4No(Integer.parseInt(dtMSGOUT.get(0).getMsgoutFiscsno()));
				} else {
					defRMFISCOUT4.setRmfiscout4No(0);
				}

				if (dbRMFISCOUT4.updateByPrimaryKeySelective(defRMFISCOUT4) < 1) {
					getLogContext().setRemark("AA1511 更新RMFISCOUT4 0 筆");
					getLogContext().setReturnCode(IOReturnCode.RMFISCOUT4UpdateError);
					sendEMS(getLogContext());
					rtnCode = IOReturnCode.RMFISCOUT4UpdateError;
				}
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * <modify>
	 * <modifier>Kyo</modifier>
	 * <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
	 * <date>2010/03/16</date>
	 * </modify>
	 */
	private FEPReturnCode updateRMOUTSNO(String reqSTAT, String reqRECEIVER_BANK, String repRMSNO) {
		RmoutsnoMapper dbRMOUTSNO = SpringBeanFactoryUtil.getBean(RmoutsnoMapper.class);
		@SuppressWarnings("unused")
		RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Rmoutsno defRMOUTSNO = new Rmoutsno();

		try {
			// Modify by Jim, 2011/12/20, 此判斷移到外面
			// If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then
			defRMOUTSNO.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMOUTSNO.setRmoutsnoReceiverBank(reqRECEIVER_BANK);
			defRMOUTSNO = dbRMOUTSNO.selectByPrimaryKey(defRMOUTSNO.getRmoutsnoSenderBank(), defRMOUTSNO.getRmoutsnoReceiverBank());
			if (defRMOUTSNO == null) {
				getLogContext().setReturnCode(IOReturnCode.RMOUTSNONotFound);
				getLogContext().setRemark("AA1511-UpdateRMOUTSNO, RMOUTSNO not found");
				TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.BRANCH, getLogContext());
				return IOReturnCode.RMOUTSNONotFound;
			} else {
				getLogContext().setRemark(StringUtils.join("AA1511, Before UpdateRMOUTSNO, RMOUTSNO_NO=", defRMOUTSNO.getRmoutsnoNo(), ", RMOUTSNO_REP_NO=", defRMOUTSNO.getRmoutsnoRepNo()));
				logMessage(Level.DEBUG, getLogContext());

				int updateNO = 0;
				Rmoutt defRMOUTT = new Rmoutt();
				// Dim dtRMOUT As New DataTable
				// Dim drRMOUT() As DataRow

				// Jim, 2012/1/18, 修改更新邏輯，要考慮共用中心的case
				defRMOUTT.setRmouttTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defRMOUTT.setRmouttFiscRtnCode(NormalRC.FISC_OK);
				// updateNO = dbRMOUTT.GetRMSNOForUpdateRMOUTSNO(defRMOUTT, reqRECEIVER_BANK)
				updateNO = getRMSNOForUpdateRMOUTSNO(defRMOUTT, reqRECEIVER_BANK);
				if (updateNO >= 0) {
					defRMOUTSNO.setRmoutsnoNo(updateNO);
					defRMOUTSNO.setRmoutsnoRepNo(updateNO);
				} else {
					return CommonReturnCode.Normal;
				}
				// dtRMOUT = dbRMOUT.GetRMOUTByDef(defRMOUT)
				// drRMOUT = dtRMOUT.Select("RMOUT_FISC_RTN_CODE='0001' AND SUBSTRING(RMOUT_RECEIVER_BANK,1,3)='" & reqRECEIVER_BANK & "'", "RMOUT_RMSNO DESC")
				// If drRMOUT.Count = 0 Then
				// defRMOUTSNO.RMOUTSNO_NO = 0
				// defRMOUTSNO.RMOUTSNO_REP_NO = 0
				// Else
				// Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_NO)
				// Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_REP_NO)
				// End If

				if (dbRMOUTSNO.updateByPrimaryKeySelective(defRMOUTSNO) < 1) {
					rtnCode = IOReturnCode.UpdateFail;
					getLogContext().setRemark("AA1511, 更新RMOUTSNO 0 筆");
					getLogContext().setReturnCode(rtnCode);
					TxHelper.getRCFromErrorCode(rtnCode, FEPChannel.BRANCH, getLogContext());
					return rtnCode;
				} else {
					getLogContext().setRemark(StringUtils.join("AA1511, After UpdateRMOUTSNO, RMOUTSNO_NO=RMOUTSNO_REP_NO=", defRMOUTSNO.getRmoutsnoNo()));
					logMessage(Level.DEBUG, getLogContext());
				}
			}

			// End If

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	private FEPReturnCode updateRMOUTByFISCSNO(Rmout defRMOUT, String reqSTAT) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
		RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
		RmbtchExtMapper dbRMBTCH = SpringBeanFactoryUtil.getBean(RmbtchExtMapper.class);
		Rmoutt defRMOUTT = new Rmoutt();

		try {
			// Modify by Jim, 2011/12/20, 此判斷移到外面
			// If FISCRMRes.ResponseCode = NormalRC.FISC_OK AndAlso reqSTAT = RMOUTStatus.Transfered Then

			defRMOUT.setRmoutFiscsno(getmFISCRMRes().getFiscNo());
			defRMOUT.setRmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

			switch (getmFISCRMRes().getSTATUS()) {
				case "01":
					// Modify by Jim, 2011/05/16, SPEC修改
					// If FISCRMReq.ORG_PCODE = "1171" Then
					// defRMOUT.RMOUT_STAT = RMOUTStatus.BackExchange '退匯
					// Else
					defRMOUT.setRmoutStat(RMOUTStatus.Passed); // (已放行)
					// End If
					break;
				case "02":
				case "03":
				case "04":

					// Modify by Jim, 2011/9/15, RMBTCH也需要同時更新
					defRMOUT = dbRMOUT.getSingleRMOUT(defRMOUT);
					if (defRMOUT != null) {
						if (RMOUT_ORIGINAL.FCS.equals(defRMOUT.getRmoutOriginal())) {
							Rmbtch defRMBTCH = new Rmbtch();
							defRMBTCH.setRmbtchSenderBank(defRMOUT.getRmoutSenderBank());
							defRMBTCH.setRmbtchRemdate(defRMOUT.getRmoutTxdate());
							defRMBTCH.setRmbtchTimes(defRMOUT.getRmoutBatchno());
							defRMBTCH.setRmbtchFepno(defRMOUT.getRmoutFepno());
							// 以下為要更新的欄位
							defRMBTCH.setRmbtchFepRc(NormalRC.RMBTCH_FEPRC_OK);
							defRMBTCH.setRmbtchErrmsg("匯出成功");
							if (dbRMBTCH.updateByPrimaryKeySelective(defRMBTCH) != 1) {
								rtnCode = IOReturnCode.RMBTCHUpdateError;
								return rtnCode;
							}
						}
					} else {
						rtnCode = IOReturnCode.RMOUTNotFound;
						return rtnCode;
					}

					defRMOUT.setRmoutStat(RMOUTStatus.GrantMoney);
					defRMOUT.setRmoutFiscRtnCode(NormalRC.FISC_OK);
					defRMOUTT.setRmouttFiscRtnCode(NormalRC.FISC_OK);
					break;
				default:

					break;
			}

			// where RMOUT_FISCSNO = @RMOUT_FISCSNO AND RMOUT_TXDATE = @RMOUT_TXDATE AND RMOUT_STAT = '05'
			if (dbRMOUT.updateRMOUTByFISCNO(defRMOUT) != 1) {
				rtnCode = IOReturnCode.RMOUTUpdateError;
				return rtnCode;
			}

			defRMOUTT.setRmouttStat(defRMOUT.getRmoutStat());
			defRMOUTT.setRmouttFiscsno(defRMOUT.getRmoutFiscsno());
			defRMOUTT.setRmouttTxdate(defRMOUT.getRmoutTxdate());

			// where RMOUTT_FISCSNO = @RMOUTT_FISCSNO AND RMOUTT_TXDATE = @RMOUTT_TXDATE AND RMOUTT_STAT = '05'
			if (dbRMOUTT.updateRMOUTTByFISCNO(defRMOUTT) != 1) {
				rtnCode = IOReturnCode.RMOUTUpdateError;
			}

			// End If

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			getLogContext().setRemark(StringUtils.join("After Update RMOUT and RMOUTT ByFISCNO, rtnCode=", rtnCode.toString(), ", FISCRMRes.FISC_NO=", getmFISCRMRes().getFiscNo(),
					", FISCRMRes.STATUS=", getmFISCRMRes().getSTATUS()));
			logMessage(Level.DEBUG, getLogContext());
		}

	}

	private FEPReturnCode updateMSGOUTByFISCSNO(String reqSTAT) {
		MsgoutExtMapper dbMSGOUT = SpringBeanFactoryUtil.getBean(MsgoutExtMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		Msgout defMSGOUT = new Msgout();

		try {
			if (NormalRC.FISC_OK.equals(getmFISCRMRes().getResponseCode()) && MSGOUTStatus.Transferring.equals(reqSTAT)) {

				switch (getmFISCRMRes().getSTATUS()) {
					case "01":
						defMSGOUT.setMsgoutStat(MSGOUTStatus.Send); // 02(已發訊)
						break;
					case "02":
					case "03":
					case "04":
						defMSGOUT.setMsgoutStat(MSGOUTStatus.Transferred);
						defMSGOUT.setMsgoutFiscRtnCode(NormalRC.FISC_OK);
						break;
				}

				defMSGOUT.setMsgoutFiscsno(getmFISCRMRes().getFiscNo());
				defMSGOUT.setMsgoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

				if (dbMSGOUT.updateMSGOUTByFISCNO(defMSGOUT) != 1) {
					rtnCode = IOReturnCode.MSGOUTUpdateError;
					getLogContext().setReturnCode(rtnCode);
					sendEMS(getLogContext());
				}
			}

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 更新FEPTXN
	 * 
	 * @return
	 * 
	 */
	@SuppressWarnings("unused")
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = null;

		if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
			getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
		}
		getFeptxn().setFeptxnMsgflow(FISC_Response); // FISC Response
		// FepTxn.FEPTXN_REP_TIME = Now

		// If _rtnCode <> CommonReturnCode.Normal Then
		// FepTxn.FEPTXN_ERR_MF = FepTxn.FEPTXN_MSGFLOW
		// FepTxn.FEPTXN_ERR_RC = FepTxn.FEPTXN_AA_RC.ToString().PadLeft(4, "0"c)
		// End If

		rtnCode = getmFISCBusiness().updateFepTxnForRM(getFeptxn());
		if (rtnCode != CommonReturnCode.Normal) {
			TxHelper.getRCFromErrorCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, getmTxFISCData().getTxChannel(), getmTxFISCData().getLogContext());
			return rtnCode;
		}

		return rtnCode;
	}

	private FEPReturnCode callSYNCFEDI(Rmout mDefRMOUT) {
		RMGeneral rmGeneral = new RMGeneral();
		RMData txData = new RMData();
		List<Msgctl> msgCtlTable = FEPCache.getMsgctlList();
		RMAABase aa = null;

		try {
			rmGeneral.getRequest().setKINBR(mDefRMOUT.getRmoutBrno());
			rmGeneral.getRequest().setTRMSEQ("99");
			rmGeneral.getRequest().setBRSNO(mDefRMOUT.getRmoutBrsno());
			rmGeneral.getRequest().setORGCHLEJNO(mDefRMOUT.getRmoutBrsno());
			rmGeneral.getRequest().setENTTLRNO("99");
			rmGeneral.getRequest().setSUPNO1("");
			rmGeneral.getRequest().setSUPNO2("");
			rmGeneral.getRequest().setTBSDY(mDefRMOUT.getRmoutTxdate());
			rmGeneral.getRequest().setTIME(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			rmGeneral.getRequest().setFEPNO(mDefRMOUT.getRmoutFepno());
			rmGeneral.getRequest().setREMDATE(mDefRMOUT.getRmoutTxdate());
			rmGeneral.getRequest().setFISCRC(mDefRMOUT.getRmoutFiscRtnCode());
			rmGeneral.getRequest().setCHLRC(TxHelper.getRCFromErrorCode(mDefRMOUT.getRmoutFiscRtnCode(), FEPChannel.FISC, FEPChannel.BRANCH, getmTxFISCData().getLogContext()));
			rmGeneral.getRequest().setCHLMSG(TxHelper.getMessageFromFEPReturnCode(mDefRMOUT.getRmoutFiscRtnCode(), FEPChannel.FISC, getmTxFISCData().getLogContext()));
			rmGeneral.getRequest().setSTATUS(mDefRMOUT.getRmoutStat());
			rmGeneral.getRequest().setORIGINAL(mDefRMOUT.getRmoutOriginal());

			txData.setEj(TxHelper.generateEj());
			txData.setTxObject(rmGeneral);
			txData.setTxChannel(getmTxFISCData().getTxChannel());
			txData.setTxSubSystem(SubSystem.RM);
			txData.setTxRequestMessage(serializeToXml(rmGeneral.getRequest()).replace("&lt;", "<").replace("&gt;", ">"));
			txData.setMessageID("SYNCFEDI");
			Map<String, Msgctl> map = msgCtlTable.stream().collect(Collectors.toMap(Msgctl::getMsgctlMsgid, msgctl -> msgctl));
			txData.setMsgCtl(map.get("SYNCFEDI"));

			txData.setLogContext(new LogData());
			txData.getLogContext().setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			txData.getLogContext().setSubSys(getLogContext().getSubSys());
			txData.getLogContext().setChannel(getLogContext().getChannel());
			txData.getLogContext().setProgramFlowType(ProgramFlow.AAIn);
			txData.getLogContext().setMessageFlowType(MessageFlow.Request);
			txData.getLogContext().setEj(txData.getEj());
			txData.getLogContext().setMessage(txData.getTxRequestMessage());

			aa = new SyncFEDI(txData);

			aa.processRequestData();

			if (!NormalRC.External_OK.equals(txData.getTxObject().getResponse().getRsStatRsStateCode())) {
				getLogContext().setMessageFlowType(MessageFlow.Response);
				getLogContext().setRemark(StringUtils.join("AA1511-FEDI轉通匯匯出狀態回饋失敗, RMOUT_PKEY=[ TXDATE=", mDefRMOUT.getRmoutTxdate(), ", BRNO=", mDefRMOUT.getRmoutBrno(), "ORIGINAL=",
						mDefRMOUT.getRmoutOriginal(), ", BATCHNO=", mDefRMOUT.getRmoutBatchno(), " ]"));
				sendEMS(getLogContext());
				return CommonReturnCode.Abnormal;
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 查詢正確的RMSNO for 更新RMOUTSNO
	 * 
	 * @return 要更新的值
	 * 
	 */
	private int getRMSNOForUpdateRMOUTSNO(Rmoutt defRMOUTT, String bkno) {
		RmouttExtMapper rmouttExtMapper = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
		try {
			Rmoutt rmoutts = rmouttExtMapper.getRMSNOForUpdateRMOUTSNO(defRMOUTT.getRmouttTxdate(), defRMOUTT.getRmouttFiscRtnCode(), bkno);
			if (rmoutts != null) {
				return Integer.parseInt(rmoutts.getRmouttRmsno());
			} else {
				return 0;
			}

		} catch (Exception ex) {
			// 發生錯誤
			return -1;
		}
	}

	public static class Queue11X1 {
		public String msgFISCNO;
		public String msgSTAT;
		public long msgAMT;

		public String getMsgFISCNO() {
			return msgFISCNO;
		}

		public void setMsgFISCNO(String msgFISCNO) {
			this.msgFISCNO = msgFISCNO;
		}

		public String getMsgSTAT() {
			return msgSTAT;
		}

		public void setMsgSTAT(String msgSTAT) {
			this.msgSTAT = msgSTAT;
		}

		public long getMsgAMT() {
			return msgAMT;
		}

		public void setMsgAMT(long msgAMT) {
			this.msgAMT = msgAMT;
		}
	}
}
