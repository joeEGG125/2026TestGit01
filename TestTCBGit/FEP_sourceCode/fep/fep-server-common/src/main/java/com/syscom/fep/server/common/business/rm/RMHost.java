package com.syscom.fep.server.common.business.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.aa.T24Data;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.*;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.mapper.CbspendMapper;
import com.syscom.fep.mybatis.mapper.RmbtchmtrMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.T24Adapter;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.t24.T24PreClass;
import com.syscom.fep.vo.text.t24.T24TITATMB;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.syscom.fep.vo.constant.FEPTxnMessageFlow.CBS_Request;

public class RMHost extends RMCommon {
	protected RMHost() {
		super();
	}

	protected RMHost(RMData rmTxMsg) {
		super(rmTxMsg);
	}

	protected RMHost(T24Data t24Msg) {
		super(t24Msg);
	}

	public FEPReturnCode sendToCBS(String CBSTxid, byte txType, Rmin defRMIN, Rmout defRMOUT, Rmbtchmtr defRMBTCHMTR) {
		return sendToCBS(CBSTxid, txType, defRMIN, defRMOUT, defRMBTCHMTR, null);
	}

	public FEPReturnCode sendToCBS(String CBSTxid, byte txType, Rmin defRMIN, Rmout defRMOUT, Rmbtchmtr defRMBTCHMTR, Cbspend defCBSPEND) {

		FEPReturnCode rtnCode = CommonReturnCode.ProgramException;

		// If CMNConfig.Instance.Phase = 1 Then
		// rtnCode = SendToUnisys(CBSTxid, txType, defRMIN, defRMOUT, defRMBTCHMTR)
		// Return rtnCode
		// End If

		isT24Finished = false; // 用來判斷是否有RECORD.STATUS這個欄位，沒有的話才會設定成true
		getLogContext().setProgramName(ProgramName + ".SendToCBS");
		@SuppressWarnings("unused")
		T24TxType t24Type = T24TxType.parse(String.valueOf(txType));
		T24PreClass t24Message = null;
		T24Adapter adapter = new T24Adapter(getmRMData());
		adapter.setFEPSubSystem(SubSystem.RM);
		String txDate = "";
		try {

			if ("".equals(company) || !CBSTxid.equals(RMCBSTxid.R1001)) {
				company = getT24Company(CBSTxid, defRMIN, defRMOUT, defRMBTCHMTR);
			} else {
				// 代表CBSTimeoutRerun已經有直接設定了(R1001)
			}
			getLogContext().setRemark(StringUtils.join("Begin To SendToCBS, CBSTxid=", CBSTxid, ", txType=", txType));
			getLogContext().setMessageFlowType(MessageFlow.Request);
			logMessage(Level.DEBUG, getLogContext());

			t24Message = new T24PreClass(company, getT24Version(CBSTxid), false, "", "PROCESS");
			txT24Message = t24Message;
			t24Message.setApplication("FUNDS.TRANSFER");
			t24Message.setUserName(RMConfig.getInstance().getT24UserName());
			t24Message.setPassword(RMConfig.getInstance().getT24SSCode());
			RefBase<T24Adapter> t24AdapterRefBase = new RefBase<>(adapter);
			RefBase<T24PreClass> t24PreClassRefBase = new RefBase<>(t24Message);
			if (txType == 3) {
				rtnCode = makeT24MessageBySee(t24PreClassRefBase, CBSTxid, defRMIN, defRMOUT);
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				}
			} else if (defCBSPEND != null) {
				// 代表是TimeourRerun發動的交易，直接用CBSPEND的電文就好
				adapter.setMessageToT24(defCBSPEND.getCbspendTita());
			} else {
				switch (CBSTxid) {
					case RMCBSTxid.R1900: // 自動入帳
						makeT24MessageBy11X2(defRMIN, t24PreClassRefBase);
						txDate = defRMIN.getRminTxdate();
						break;
					case RMCBSTxid.R2300: // 分行發動之人工解款
						makeT24MessageByR2300(defRMIN, t24PreClassRefBase);
						txDate = defRMIN.getRminTxdate();
						break;
					case RMCBSTxid.R2301: // 作業中心發動人工解款
						makeT24MessageByR2301(defRMIN, t24PreClassRefBase);
						txDate = defRMIN.getRminTxdate();
						break;
					case RMCBSTxid.R2400: // 解款狀態變更 */
						makeT24MessageByR2400(defRMIN, t24PreClassRefBase);
						txDate = defRMIN.getRminTxdate();
						break;
					case RMCBSTxid.R2401:
						makeT24MessageByR2401(defRMIN, t24PreClassRefBase);
						txDate = defRMIN.getRminTxdate();
						break;
					case RMCBSTxid.R8888: // 匯出更正
						makeT24MessageByRT1101(defRMOUT, t24PreClassRefBase);
						txDate = defRMOUT.getRmoutTxdate();
						break;
					case RMCBSTxid.R1001: // 大批匯款
						makeT24MessageBYRMBTCHMTR(defRMBTCHMTR, t24PreClassRefBase);
						txDate = defRMBTCHMTR.getRmbtchmtrRemdate();
						break;
					case RMCBSTxid.R1600: // 緊急匯款
						makeT24MessageByR1600(defRMOUT, t24PreClassRefBase);
						txDate = defRMOUT.getRmoutTxdate();
						return prepareAndInsertCBSPEND(t24Message, txDate);
					case RMCBSTxid.ENQUIRY:
						makeT24MessageForEnquiry(defRMIN, defCBSPEND, t24PreClassRefBase);
						break;
					case "RESET":
						rtnCode = makeT24MassageByRESET(t24AdapterRefBase, defRMIN.getRminTxdate());
						break;
					case "PENDING":
						// Fly 2014/09/16 For ServiceRMMoniter
						makeT24MessageByPENDING(t24AdapterRefBase);
						break;
					default:

						break;
				}
			}
			adapter = t24AdapterRefBase.get();
			t24Message = t24PreClassRefBase.get();
			rtnCode = FEPReturnCode.HostResponseTimeout;

			// 如果是做RESET不用更新FEPTXN
			// Fly 2014/09/16 查PENDING也不需更新FEPTXN
			if (!"RESET".equals(CBSTxid) && !"PENDING".equals(CBSTxid)) {
				// 5. 將組好的T24電文送往24主機
				// 送T24主機前, 先更新交易記錄
				getFeptxn().setFeptxnMsgflow(CBS_Request);
				getFeptxn().setFeptxnNeedSendCbs((short) txType);
				getFeptxn().setFeptxnCbsTxCode(CBSTxid);
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true));

				if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
					TxHelper.getRCFromErrorCode(IOReturnCode.FEPTXNUpdateError, FEPChannel.FEP, getLogContext());
					return IOReturnCode.FEPTXNUpdateError;
				}
			}

			if ("".equals(adapter.getMessageToT24()) || adapter.getMessageToT24() == null) {
				// ofs part
				adapter.setMessageToT24(t24Message.genT24ReqOFSForRM());
			}

			rtnCode = adapter.sendReceive();

			// 設定TIMER等待T24主機回應訊息
			if (rtnCode == CommonReturnCode.HostResponseTimeout || rtnCode == CommonReturnCode.ProgramException || rtnCode == CommonReturnCode.CBSResponseError) {
				// 一律回前端HostResponseTimeout
				rtnCode = CommonReturnCode.HostResponseTimeout;
				// Modify by Jim, 2011/01/05, R8888(RT1101呼叫)不須寫CBSPEND
				if ("R1900,R1001".indexOf(CBSTxid) > -1) {
					FEPReturnCode rtnCode1 = prepareAndInsertCBSPEND(t24Message, txDate);
					if (rtnCode1 != CommonReturnCode.Normal) {
						rtnCode = rtnCode1;
					}
				}

				switch (CBSTxid) {
					case RMCBSTxid.R1900: // 自動入帳
						updateRMINByT24Res(defRMIN, RMPending.Fail, "", null, CBSTxid, txType);
						break;
					case RMCBSTxid.R2300: // 分行發動之人工解款
						updateRMINByT24Res(defRMIN, RMPending.Fail, "", null, CBSTxid, txType);
						break;
					case RMCBSTxid.R2301: // 作業中心發動人工解款
						updateRMINByT24Res(defRMIN, RMPending.Fail, "", null, CBSTxid, txType);
						break;
					case RMCBSTxid.R2400: // 解款狀態變更
						updateRMINByT24Res(defRMIN, RMPending.Fail, "", null, CBSTxid, txType);
						break;
					case RMCBSTxid.R8888: // 匯出更正
						updateRMOUTByT24Res(defRMOUT, RMPending.Fail, CBSTxid);
						break;
					case RMCBSTxid.R1001: // 大批匯款
						updateRMBTCHMTRByT24Res(defRMBTCHMTR, RMPending.Fail, null);
						break;
				}
				return rtnCode;
			}

			// Modify by Jim, 2011/01/05, 由於T24回應電文的格式會有所不同，需要分Case處理
			if (txType == 3) {
				// Modify by Jim, 2011/05/16, Function=See 的Response處理
				rtnCode = processT24ResForSee(CBSTxid, txType, adapter.getMessageFromT24(), t24Message, defRMIN, defRMOUT, defRMBTCHMTR);
			} else {
				switch (CBSTxid) {
					case RMCBSTxid.R1900:
					case RMCBSTxid.R2300:
					case RMCBSTxid.R2301:
					case RMCBSTxid.R2400:
					case RMCBSTxid.R1001:
					case RMCBSTxid.R1600:
					case "R2401":
						rtnCode = processT24ResForOFS(CBSTxid, txType, adapter.getMessageFromT24(), t24Message, defRMIN, defRMOUT, defRMBTCHMTR);
						break;
					case RMCBSTxid.R8888:
						rtnCode = processT24ResForUnlock(CBSTxid, adapter.getMessageFromT24(), t24Message, defRMOUT);
						break;
					case RMCBSTxid.ENQUIRY:
						rtnCode = processT24ResForENQUIRY(CBSTxid, adapter.getMessageFromT24(), defRMIN, defCBSPEND);
						break;
					case "RESET":
						// 直接判斷是不是成功就好了
						CBSRC = adapter.getMessageFromT24();
						if (adapter.getMessageFromT24().toUpperCase().contains("ERROR") || adapter.getMessageFromT24().toUpperCase().contains("OFFLINE")) {
							rtnCode = CommonReturnCode.CBSResponseError;
						} else {
							rtnCode = CommonReturnCode.Normal;
						}
						break;
					case "PENDING":
						// Fly 2014/09/16 For ServiceRMMonitor
						t24Message.parseT24RspOfsForBType(adapter.getMessageFromT24(), "B0001");
						break;
				}
			}

			// 如果是做R5100平衡匯入款交易狀態, 不需要回非程式異常的error
			if (rmReq != null && rmReq.getMsgID() != null && RMTXCode.R5100.equals(rmReq.getMsgID())) {
				if (rtnCode != CommonReturnCode.ProgramException) {
					rtnCode = CommonReturnCode.Normal;
				}
			}

			getLogContext().setRemark(StringUtils.join("In RMBusiness.SendToCBS, After ProcessResForXXX, rtnCode=", rtnCode.toString(), ", CBSRC=", CBSRC));
			getLogContext().setProgramFlowType(ProgramFlow.None);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());

			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			// Modify by Jim, 2011/05/26, 每一次執行完需要將此兩個屬性清掉。
			seeEJ = "";
			company = "";
		}
	}

	private String getT24Company(String CBSTxid, Rmin defRMIN, Rmout defRMOUT, Rmbtchmtr defRMBTCHMTR) {
		String brPC = String.valueOf(ATMPConfig.getInstance().getProcessCenter());
		Company defCOMPANY = new Company();
		CompanyExtMapper dbCOMPANY = SpringBeanFactoryUtil.getBean(CompanyExtMapper.class);
		String wkRSCOMPANY = BankIdToT24 + brPC;

		if ("R1900".equals(CBSTxid) || "R2300".equals(CBSTxid) || "R2301".equals(CBSTxid) || "R2400".equals(CBSTxid)) {
			defCOMPANY.setRecid(StringUtils.join("TW8070", defRMIN.getRminReceiverBank().substring(3, 6)));
			defCOMPANY = dbCOMPANY.selectByPrimaryKey(defCOMPANY.getRecid());
			// Fly 2017/1/10 (週二) 下午 03:00 中正、西門(簡)分行整併
			if (defCOMPANY != null) {
				if (StringUtils.isBlank(defCOMPANY.getRseffdate())
						|| Integer.parseInt(defCOMPANY.getRseffdate()) > Integer.parseInt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))) {
					wkRSCOMPANY = defCOMPANY.getRecid();
				} else {
					wkRSCOMPANY = defCOMPANY.getRscompany();
				}
			} else {
				wkRSCOMPANY = BankIdToT24 + brPC;
			}
		}

		switch (CBSTxid) {
			case "R1900": // 自動入帳
				return wkRSCOMPANY;
			case "R2300": // 分行發動之人工解款
				return wkRSCOMPANY;
			case "R2301": // 作業中心發動人工解款
				return wkRSCOMPANY;
			case "R2400": // 解款狀態變更 */
				return wkRSCOMPANY;
			case "R8888": // 匯出更正
				return StringUtils.join(BankIdToT24, defRMOUT.getRmoutBrno());
			case "R1001": // 大批匯款,緊急匯款
				return StringUtils.join(BankIdToT24, defRMBTCHMTR.getRmbtchmtrKinbr());
			case "R1600": // 緊急匯款
				return StringUtils.join(BankIdToT24, defRMOUT.getRmoutSenderBank().substring(3, 6));
			default:
				return "";
		}
	}

	private String getT24Version(String CBSTxid) {
		switch (CBSTxid) {
			case "R1000": // 匯出匯款登錄
				return "TMB.RMT.OUT(ofsTransactionInput)";
			case "R1900": // 自動入帳
				// Return "TMB.TEST.IN"
				return "TMB.RM.LCYIN.AUTO";
			case "R2300": // 分行發動之人工解款
				return "TMB.RMT.IN";
			case "R2301": // 作業中心發動人工解款
				return "TMB.RMT.IN";
			case "R1009": // 匯入款退匯
				return "TMB.RMT.IN.RET";
			case "R1001":
			case "R1600": // 大批匯款,緊急匯款
				return "TMB.LRM.BATCH";
			case "R8888": // 匯出更正
				return "TMB.OFS.LRM.REMIT.OUT.REV";
			// Return "ofsRoutine"
			case "R2400": // 解款狀態變更 */
				// Return "TMB.RMT.IN(ofsTransactionReverse)"
				return "TMB.RMT.IN"; // modify by Jim, 2010/12/22, 友弘mail說
			case "R2401":
				return "TMBI.C.EJ.NO.RSV.S"; // 刪除已更正之T.BR.SEQNO
			case "ENQUIRY":
				return "ENQUIRY.SELECT";
			default:
				return "";
		}
	}

	/**
	 * 查詢
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>Enquiry T24回應已入帳, 但無Response內容可update RMIN, 故改用原電文 FUNCTION=Input, 改成See作查詢才可update虛擬帳號之實體帳號, 績效行…等欄位</reason>
	 * <date>2011/5/11</date>
	 * </modify>
	 * </history>
	 */
	public FEPReturnCode makeT24MessageBySee(RefBase<T24PreClass> t24ReqMessage, String CBSTxId, Rmin defRMIN, Rmout defRMOUT) {
		T24TITATMB titaTMB = new T24TITATMB();
		try {
			// sample
			// FUNDS.TRANSFER,TMB.RM.LCYIN.AUTO/S/PROCESS/2/0,ATM.USER/123456/TW8070121,FEP2011050903558202

			// RMBusiness.SendToCBS.doc SPEC說明如下:
			// Function 由I改成S(See),
			// T.BR.SEQNO = SYSTEMDATE & I_EJFNO(左補8位0)
			// Process/2/0,之後加上原交易之T.BR.SEQNO

			t24ReqMessage.get().setT24Function("S"); // SEE

			// If CBSTxId = RMCBSTxid.R1600 OrElse CBSTxId = RMCBSTxid.R1001 Then
			// titaTMB.T_BR_SEQNO = seeEJ '在CBSTimeoutRerun已經組好
			// Else
			// titaTMB.T_BR_SEQNO = FEP & Now.ToString("yyyyMMdd") & seeEJ.PadLeft(7, "0"c)
			// End If

			switch (CBSTxId) {
				case RMCBSTxid.R1900:
					// Jim, 2012/6/28, 因R5100有可能查非當日的資料，因此直接抓RMIN_TXDATE就好。
					titaTMB.setTBrSeqno(StringUtils.join(FEP + defRMIN.getRminTxdate() + StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
					break;
				case RMCBSTxid.R2300:
				case RMCBSTxid.R2301:
				case RMCBSTxid.R2400:
					titaTMB.setTBrSeqno(StringUtils.join(FEP, defRMIN.getRminTxdate(), StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
					break;
				case RMCBSTxid.R1600:
					titaTMB.setTBrSeqno(StringUtils.join(FEP, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2), defRMOUT.getRmoutBrno(), defRMOUT.getRmoutOriginal(),
							defRMOUT.getRmoutFepno()));
					break;
				case RMCBSTxid.R1001:
					titaTMB.setTBrSeqno(seeEJ);
					break;
			}

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramName("makeT24MessageBySee");
			getLogContext().setProgramException(ex);
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	private FEPReturnCode prepareAndInsertCBSPEND(T24PreClass reqT24Message, String txDate) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Cbspend defCBSPEND = new Cbspend();
		CbspendMapper dbCBSPEND = SpringBeanFactoryUtil.getBean(CbspendMapper.class);
		// MessageQueue CBSPENDQueue = new MessageQueue(RMConfig.getInstance().getRmCbspend());

		try {
			// Jim, 2012/5/9, 將程式改成不需要優利電文的寫法
			RefBase<Cbspend> Recoverable = new RefBase<>(defCBSPEND);
			rtnCode = prepareCBSPEND(reqT24Message, Recoverable);
			defCBSPEND = Recoverable.get();
			if (rtnCode == CommonReturnCode.Normal) {
				if (dbCBSPEND.insertSelective(defCBSPEND) == 1) {
					// 寫入 Queue
					/*
					 * Message msg = new Message(); zk add
					 * msg.Recoverable = true;
					 * msg.Label = "CBSPEND";
					 * msg.Body = StringUtils.join(txDate , ":" , reqT24Message.getTITAHeader().getEJFNO().substring(8));
					 * CBSPENDQueue.send(msg);
					 */

				} else {
					getLogContext().setRemark("PrepareAndInsertCBSPEND--Insert CBSPEND Error");
					getLogContext().setReturnCode(IOReturnCode.CBSPENDInsertError);
					TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
					return getLogContext().getReturnCode();
				}
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 負責準備CBSPEND上傳主機逾時明細檔內容
	 * 
	 * @param reqT24Message
	 * @param defCBSPEND
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/5/10</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>將程式改成不需要優利電文的寫法</reason>
	 *         <date>2012/5/9</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode prepareCBSPEND(T24PreClass reqT24Message, RefBase<Cbspend> defCBSPEND) {
		try {
			if (reqT24Message != null) {
				defCBSPEND.get().setCbspendTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defCBSPEND.get().setCbspendEjfno(Integer.parseInt(reqT24Message.getTITAHeader().getEJFNO().substring(8)));
				// .CBSPEND_CBS_TX_CODE = reqT24Message.TITAHeader.FEP_TXN_CODE
				defCBSPEND.get().setCbspendTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defCBSPEND.get().setCbspendTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				defCBSPEND.get().setCbspendSuccessFlag((short) 0); // 0:必須重送
				defCBSPEND.get().setCbspendResendCnt((short) 0);
				defCBSPEND.get().setCbspendReverseFlag(DbHelper.toShort(reqT24Message.getReverseTag()));
				defCBSPEND.get().setCbspendSubsys((short) SubSystem.RM.getValue());

				switch (reqT24Message.getTITAHeader().getFeptxnCode()) {
					case RMCBSTxid.R1900:
						defCBSPEND.get().setCbspendCbsTxCode("R3000");
						break;
					case RMCBSTxid.R1001:
						defCBSPEND.get().setCbspendCbsTxCode("R1300");
						break;
					default:
						defCBSPEND.get().setCbspendCbsTxCode(reqT24Message.getTITAHeader().getFeptxnCode());
						break;
				}

				switch (reqT24Message.getTITAHeader().getFeptxnCode()) {
					case "R1900":
					case "R2300":
					case "R2301":
						defCBSPEND.get().setCbspendActno(reqT24Message.getTITABody().get("CREDIT.ACCT.NO"));
						defCBSPEND.get().setCbspendIbBkno("0");
						defCBSPEND.get().setCbspendIbActno(reqT24Message.getTITABody().get("DEBIT.ACCT.NO"));
						break;
					case "R1001":
					case "R1600":
						defCBSPEND.get().setCbspendActno(reqT24Message.getTITABody().get("DEBIT.ACCT.NO"));
						defCBSPEND.get().setCbspendIbBkno("0");
						defCBSPEND.get().setCbspendIbActno(reqT24Message.getTITABody().get("CREDIT.ACCT.NO"));
						break;
					default:
						defCBSPEND.get().setCbspendActno("0");
						defCBSPEND.get().setCbspendIbBkno("0");
						defCBSPEND.get().setCbspendIbActno("0");
						break;
				}
				defCBSPEND.get().setCbspendTxAmt(new BigDecimal(reqT24Message.getTITABody().get("DEBIT.AMOUNT").replace(CurrencyType.TWD.toString(), "")));
				defCBSPEND.get().setCbspendTita(reqT24Message.genT24ReqOFSForRM());
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理ENQUIRY格式
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>為了處理多種不同格式的T24回應，拆成多個Function</reason>
	 * <date>2011/01/05</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode processT24ResForENQUIRY(String CBSTxid, String MsgFromT24, Rmin defRMIN, Cbspend defCBSPEND) {

		FEPReturnCode rtnCode = CommonReturnCode.Abnormal; // 如果Response電文有transactionId才算是成功
		getLogContext().setProgramName(ProgramName + "processT24ResForENQUIRY");

		// OUTPUT: 有資料
		// ,@ID::@ID/TRANSACTION.TYPE::TRANSACTION.TYPE/DEBIT.ACCT.NO::DEBIT.ACCT.NO/MAILING::MAILING," FT111360TRF2" "AC " "TWD1100700010001" " "
		// OUTPUT: 沒資料
		// ,@ID::@ID/TRANSACTION.TYPE::TRANSACTION.TYPE/DEBIT.ACCT.NO::DEBIT.ACCT.NO/MAILING::MAILING,"No records were found that matched the selection criteria","","SSELECT FBNK.FUNDS.TRANSFER WITH
		// T.BR.SEQNO = ",""2011010401220601"
		String[] rspArray = MsgFromT24.split("[,]", -1);
		getLogContext().setRemark(StringUtils.join("T24 ENQUIRY, MsgFromT24=", MsgFromT24, ",rspArray.length=", rspArray.length));
		logMessage(Level.DEBUG, getLogContext());

		for (String str : rspArray) {

			if (str.contains("\"")) {// MAILING,後面的訊息
				if (str.contains("No records were found")) {
					// 查無資料
					getLogContext().setReturnCode(IOReturnCode.QueryNoData);
					getLogContext().setRemark("送T24 ENQUIRY查無資料");
					logMessage(Level.DEBUG, getLogContext());
					return IOReturnCode.QueryNoData;
				} else {
					// Modify by Jim, 2011/03/17, 回應電文如下:,@ID::@ID/TRANSACTION.TYPE::TRANSACTION.TYPE/DEBIT.ACCT.NO::DEBIT.ACCT.NO/MAILING::MAILING," FT11136Z53Y5" "ACTF " "TWD1103900000001" " "
					// TransactionId的前面會有很多空白，所以要trim掉
					String[] content = str.split("[\"]", -1);
					for (String rsp : content) {
						if (rsp.contains("FT")) {
							rtnCode = CommonReturnCode.Normal;
							getLogContext().setRemark(StringUtils.join("T24 ENQUIRY, 查詢結果, TransactionId=", rsp.trim()));
							getLogContext().setReturnCode(rtnCode);
							logMessage(Level.DEBUG, getLogContext());
							if (defCBSPEND != null) {
								defCBSPEND.setCbspendCbsRc(T24RC_Normal);
								defCBSPEND.setCbspendCbsRrn(rsp.trim());
							} else {
								defRMIN.setRminCbsRc(T24RC_Normal);
								defRMIN.setRminCbsNo(rsp.trim());
							}
							break;
						}
					}
				}
			}
		}

		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark("ENQUIRY Response Error or Query No Data");
			getLogContext().setReturnCode(rtnCode);
			sendEMS(getLogContext());
		}

		getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Response); // H2
		getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時 FLAG設為False
		// FepTxn.FEPTXN_CBS_RC = t24Message.TOTATransResult.Item("EB.ERROR")

		return rtnCode;
	}

	/**
	 * 負責處理對原匯出交易進行解鎖(R8888)格式
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>為了處理多種不同格式的T24回應，拆成多個Function</reason>
	 * <date>2011/01/05</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode processT24ResForUnlock(String CBSTxid, String MsgFromT24, T24PreClass t24Message, Rmout defRMOUT) {

		FEPReturnCode rtnCode = CommonReturnCode.Abnormal;
		getLogContext().setProgramName(ProgramName + "ProcessT24ResForUnlock");

		getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Response); // H2
		getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時 FLAG設為False

		// 回應錯誤sample: FAIL:RECORD NOT FOUND
		if (MsgFromT24.contains("OK")) {
			getFeptxn().setFeptxnCbsRc("OK");
			CBSRC = T24RC_Normal;
			defRMOUT.setRmoutStat(RMOUTStatus.DeletedNotUpdateAccount);
			rtnCode = updateRMOUTByT24Res(defRMOUT, RMPending.Normal, CBSTxid);
			if (rtnCode != CommonReturnCode.Normal) {
				getLogContext().setRemark("T24 Unlock OK, Update RMOUT,RMOUTT,FEPTXN Error");
				getLogContext().setReturnCode(rtnCode);
				logMessage(Level.DEBUG, getLogContext());
			}
		} else {
			getLogContext().setRemark("T24 Unlock Fail");
			logMessage(Level.INFO, getLogContext());
			getFeptxn().setFeptxnCbsRc(MsgFromT24);
			CBSRC = MsgFromT24;
			CBSErrorMessage = MsgFromT24;
			rtnCode = updateRMOUTByT24Res(defRMOUT, RMPending.Normal, CBSTxid);
			if (rtnCode != CommonReturnCode.Normal) {
				getLogContext().setRemark("T24 Unlock Fail, Update RMOUT,RMOUTT,FEPTXN Error");
				getLogContext().setReturnCode(rtnCode);
				logMessage(Level.DEBUG, getLogContext());
			}
			return CommonReturnCode.CBSResponseError;
		}

		return rtnCode;
	}

	private FEPReturnCode updateRMOUTByT24Res(Rmout defRMOUT, String pending, String CBSTxid) {
		RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);

		RmouttExtMapper dbRMOUTT = SpringBeanFactoryUtil.getBean(RmouttExtMapper.class);
		Rmoutt defRMOUTT = new Rmoutt();
		@SuppressWarnings("unused")
		String originStat = defRMOUT.getRmoutStat();
		String PK = StringUtils.join("RMOUT_TXDATE:", defRMOUT.getRmoutTxdate(), ";", "RMOUT_BRNO:", defRMOUT.getRmoutBrno(), ";", "RMOUT_ORIGINAL:", defRMOUT.getRmoutOriginal(), ";", "RMOUT_FEPNO:",
				defRMOUT.getRmoutFepno(), ";");
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {

			if (CBSTxid.equals(RMCBSTxid.R8888)) {
				if (pending.equals(RMPending.Fail)) {
					defRMOUT.setRmoutPending(RMPending.Fail); // "R"
					getLogContext().setRemark(StringUtils.join("UpdateRMOUTByT24Res,Unlock Fail ,RMOUT_STAT = ", defRMOUT.getRmoutStat()));
					logMessage(Level.INFO, getLogContext());
				} else {
					defRMOUT.setRmoutPending(RMPending.Normal); // "0"
					defRMOUT.setRmoutCbsRc(getFeptxn().getFeptxnCbsRc());
					defRMOUT.setRmoutStat(RMOUTStatus.DeletedNotUpdateAccount);
				}

				// If dbRMOUT.UpdateByPrimaryKey(defRMOUT) <> 1 Then
				// db.RollbackTransaction()
				// LogContext.TableDescription = dbRMOUT.TableDescription
				// LogContext.PrimaryKeys = PK
				// LogContext.Remark = "送完T24更新RMOUT錯誤; PK=" & PK
				// LogContext.ReturnCode = IOReturnCode.UpdateFail
				// TxHelper.GetRCFromErrorCode(LogContext.ReturnCode, FEPChannel.RM, LogContext)
				// Return IOReturnCode.UpdateFail
				// End If

				// Modify by Jim, 2011/03/22, 狀態=07才會有RMOUTT檔
				// If originStat = RMOUTStatus.FISCReject Then
				// defRMOUTT.RMOUTT_TXDATE = defRMOUT.RMOUT_TXDATE
				// defRMOUTT.RMOUTT_BRNO = defRMOUT.RMOUT_BRNO
				// defRMOUTT.RMOUTT_FEPNO = defRMOUT.RMOUT_FEPNO
				// defRMOUTT.RMOUTT_ORIGINAL = defRMOUT.RMOUT_ORIGINAL

				// defRMOUTT.RMOUTT_PENDING = defRMOUT.RMOUT_PENDING
				// defRMOUTT.RMOUTT_CBS_RC = defRMOUT.RMOUT_CBS_RC
				// defRMOUTT.RMOUTT_STAT = defRMOUT.RMOUT_STAT

				// If dbRMOUTT.UpdateByPrimaryKey(defRMOUTT) <> 1 Then
				// db.RollbackTransaction()
				// LogContext.TableDescription = dbRMOUTT.TableDescription
				// LogContext.PrimaryKeys = PK
				// LogContext.Remark = "送完T24更新RMOUTT錯誤; PK=" & PK
				// LogContext.ReturnCode = IOReturnCode.UpdateFail
				// TxHelper.GetRCFromErrorCode(LogContext.ReturnCode, FEPChannel.RM, LogContext)
				// Return IOReturnCode.UpdateFail
				// End If
				// End If

			} else if (CBSTxid.equals(RMCBSTxid.R1600)) {
				// 設定匯出暫存檔PK
				defRMOUTT.setRmouttTxdate(defRMOUT.getRmoutTxdate());
				defRMOUTT.setRmouttBrno(defRMOUT.getRmoutBrno());
				defRMOUTT.setRmouttFepno(defRMOUT.getRmoutFepno());
				defRMOUTT.setRmouttOriginal(defRMOUT.getRmoutOriginal());

				// 緊急匯款 R1600
				if (pending.equals(RMPending.Fail)) {
					defRMOUT.setRmoutPending(RMPending.Fail); // "R"
					defRMOUTT.setRmouttPending(RMPending.Fail);
				} else {
					defRMOUT.setRmoutPending(RMPending.Normal); // "0"
					defRMOUT.setRmoutCbsRc(getFeptxn().getFeptxnCbsRc());
					// defRMOUT.RMOUT_EJNO2 = EJ
					defRMOUTT.setRmouttPending(RMPending.Normal); // "0"
					defRMOUTT.setRmouttCbsRc(getFeptxn().getFeptxnCbsRc());
				}
				if (dbRMOUT.updateByPrimaryKeySelective(defRMOUT) != 1) {
					transactionManager.rollback(txStatus);
					getLogContext().setTableDescription("匯出主檔");
					getLogContext().setPrimaryKeys(PK);
					getLogContext().setRemark(StringUtils.join("送完T24更新RMOUT錯誤; PK=" + PK));
					getLogContext().setReturnCode(IOReturnCode.UpdateFail);
					TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
					return IOReturnCode.UpdateFail;
				}
				if (dbRMOUTT.updateByPrimaryKeySelective(defRMOUTT) != 1) {
					transactionManager.rollback(txStatus);
					getLogContext().setTableDescription("匯出主檔");
					getLogContext().setPrimaryKeys(PK);
					getLogContext().setRemark(StringUtils.join("送完T24更新RMOUTT錯誤; PK=", PK));
					getLogContext().setReturnCode(IOReturnCode.UpdateFail);
					TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
					return IOReturnCode.UpdateFail;
				}
			}

			transactionManager.commit(txStatus);

			if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {// 更新後不清掉
				getLogContext().setReturnCode(IOReturnCode.FEPTXNUpdateError);
				getLogContext().setRemark(StringUtils.join("SendCBS最後Update FEPTXN失敗; FEPTXN_TX_DATE=" + getFeptxn().getFeptxnTxDate() + ", FEPTXN_EJFNO=" + getFeptxn().getFeptxnEjfno()));
				return IOReturnCode.FEPTXNUpdateError;
			}

			return CommonReturnCode.Normal;

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
	 * 負責處理OFS格式
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>為了處理多種不同格式的T24回應，拆成多個Function</reason>
	 * <date>2011/01/05</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode processT24ResForOFS(String CBSTxid, byte txType, String MsgFromT24, T24PreClass t24Message, Rmin defRMIN, Rmout defRMOUT, Rmbtchmtr defRMBTCHMTR) {

		FEPReturnCode rtnCode = CommonReturnCode.Abnormal;
		getLogContext().setProgramName(ProgramName + "ProcessT24ResForOFS");

		if (!t24Message.parseT24RspOFS(MsgFromT24)) {
			RefBase<Rmin> rminRefBase = new RefBase<>(defRMIN);
			rtnCode = prepareRMINForFail(rminRefBase, "0", CBSTxid, txType);
			defRMIN = rminRefBase.get();
			if (rtnCode == CommonReturnCode.Normal) {
				if (CBSTxid.equals(RMCBSTxid.R2300) || CBSTxid.equals(RMCBSTxid.R2301) || CBSTxid.equals(RMCBSTxid.R2400)) {
					RminExtMapper tmpDBRMIN = SpringBeanFactoryUtil.getBean(RminExtMapper.class);
					if (tmpDBRMIN.updateByPrimaryKeySelective(defRMIN) < 1) {
						getLogContext().setRemark("拆解電文Error, Update RMIN 0筆");
						logMessage(Level.ERROR, getLogContext());
					}
				}
			}
			// Modify by Jim, 2011/04/14, show給前端T24回應的錯誤
			CBSRC = MsgFromT24;
			getLogContext().setRemark("送主機後拆解電文 ParseT24RspOFS 失敗");
			getLogContext().setReturnCode(CommonReturnCode.ParseTelegramError);
			TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
			return CommonReturnCode.ParseTelegramError;
		}

		// Modify by Jim, 2011/05/18, 用TxHelper來對應T24錯誤代碼
		if (!NormalRC.T24RC_OK.equals(t24Message.getTOTATransResult().get("EB.ERROR"))) {
			CBSRC = TxHelper.getRCFromErrorCode(t24Message.getTOTATransResult().get("EB.ERROR"), FEPChannel.T24, FEPChannel.FEP, getLogContext());
			CBSErrorMessage = t24Message.getTOTATransResult().get("EB.ERROR");
		} else {
			CBSRC = NormalRC.T24RC_OK;
		}

		switch (CBSTxid) {
			case RMCBSTxid.R2401:
				getFeptxn().setFeptxnCbsRc(CBSRC);
				if (NormalRC.T24RC_OK.equals(CBSRC)) {
					return CommonReturnCode.Normal;
				} else {
					return CommonReturnCode.CBSResponseError;
				}
			default:
				// Modify by Jim, 2011/03/04, 如果是入帳有RECORD.STATUS此欄位表示T24交易未完成；如果是沖正，含有RECORD.STATUS=REVE時代表沖正成功
				if (!t24Message.getTOTATransResult().containsKey("RECORD.STATUS")) {
					isT24Finished = true;
				} else {
					// Modify by Jim, 2011/03/10, 如果是沖正，含有RECORD.STATUS=REVE時代表沖正成功
					if (t24Message.getReverseTag()) {
						if ("REVE".equals(t24Message.getTOTATransResult().get("RECORD.STATUS"))) {
							isT24Finished = true;
						} else {
							getLogContext().setRemark("沖正回應電文含有 RECORD.STATUS且不等於REVE，算 [ 沖正失敗 ]");
							logMessage(Level.WARN, getLogContext());
						}
					} else {
						getLogContext().setRemark("入帳回應電文含有 RECORD.STATUS，算 [ 入帳失敗 ]");
						logMessage(Level.WARN, getLogContext());
					}
				}

				getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Response); // H2
				getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false)); // 收到回應正常CBS逾時 FLAG設為False
				getFeptxn().setFeptxnCbsRc(CBSRC);

				if (!T24RC_Normal.equals(t24Message.getTOTATransResult().get("EB.ERROR")) || !isT24Finished) {
					// T24回ERROR
					switch (CBSTxid) {
						case RMCBSTxid.R1900:
						case RMCBSTxid.R2300:
						case RMCBSTxid.R2301: // T24 version A類 - 帳務類交易 - 入帳類交易
							getFeptxn().setFeptxnAccType((short) AccountingType.ECFail.getValue()); // 3:入帳失敗
							break;
						case RMCBSTxid.R2400:
						case RMCBSTxid.R8888:
						case RMCBSTxid.R1600:
							getFeptxn().setFeptxnAccType((short) AccountingType.UnAccounting.getValue()); // 0:未記帳
							break;
					}

					switch (CBSTxid) {
						case RMCBSTxid.R1900:
						case RMCBSTxid.R2300:
						case RMCBSTxid.R2301:
						case RMCBSTxid.R2400:
							rtnCode = updateRMINByT24Res(defRMIN, RMPending.Normal, "", t24Message.getTOTATransResult(), CBSTxid, txType);
							break;
						case RMCBSTxid.R1001: // 大批匯款
							rtnCode = updateRMBTCHMTRByT24Res(defRMBTCHMTR, RMPending.Normal, t24Message.getTOTATransResult());
							break;
						case RMCBSTxid.R1600:
							// rtnCode = UpdateRMOUTByT24Res(defRMOUT, RMPending.Normal, CBSTxid)
							break;
					}

					if (rtnCode == CommonReturnCode.Normal) {
						rtnCode = CommonReturnCode.CBSResponseError;
					} else {
						getLogContext().setRemark(StringUtils.join("CBSTxid=", CBSTxid, "Update RM Table By T24 Res Error"));
					}
					getLogContext().setReturnCode(rtnCode);
					logMessage(Level.DEBUG, getLogContext());
					return rtnCode;
				} else {
					// T24回正常
					if ("R".equals(CBSTxid.substring(0, 1))) {// 帳務類交易
						getFeptxn().setFeptxnCbsRrn(t24Message.getTOTATransContent().get("transactionId"));
						// SPEC修改, 以下二欄已移到FEPXNDTL, 所以不UPDATE
						// FepTxn.FEPTXN_CBS_VALUE_DATE = t24Message.TOTATransResult.Item("CREDIT.VALUE.DATE") '起息日*/
						// FepTxn.FEPTXN_CBS_TX_TIME = t24ReqMessage.TOTATransResult.Item("T24.TXTIME")
					}

					// Modify by Jim, 2011/05/27, SPEC修改, 更新FEPTXN_ACC_TYPE欄位
					if (CBSTxid.equals(RMCBSTxid.R2400) || CBSTxid.equals(RMCBSTxid.R8888)) {
						getFeptxn().setFeptxnAccType((short) AccountingType.EC.getValue()); // 沖正
					} else {
						getFeptxn().setFeptxnAccType((short) AccountingType.Accounting.getValue()); // 已記帳
					}

					if ("R1900,R2300,R2301".indexOf(CBSTxid) > -1) {
						if (MsgFromT24.indexOf("T.CR.KPI.BR") > 0) {
							// Modify by Jim, 2010/11/11
							getFeptxn().setFeptxnTxDept(t24Message.getTOTATransResult().get("T.CR.KPI.BR")); // 交易帳號績效單位
						}
						// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
						getFeptxn().setFeptxnTxBrno(company.substring(company.length() - 3)); // 交易帳號掛帳行
						getFeptxn().setFeptxnTxActno(defRMIN.getRminActno()); // 交易帳號
					}

					// 依交易類別更新不同的主檔
					switch (CBSTxid) {
						case RMCBSTxid.R1900:
						case RMCBSTxid.R2300:
						case RMCBSTxid.R2301:
						case RMCBSTxid.R2400:
							rtnCode = updateRMINByT24Res(defRMIN, RMPending.Normal, t24Message.getTOTATransContent().get("transactionId"), t24Message.getTOTATransResult(), CBSTxid, txType);
							break;
						case RMCBSTxid.R1001: // 大批匯款
							rtnCode = updateRMBTCHMTRByT24Res(defRMBTCHMTR, RMPending.Normal, t24Message.getTOTATransResult());
							break;
						case RMCBSTxid.R1600:
							// rtnCode = UpdateRMOUTByT24Res(defRMOUT, RMPending.Normal, CBSTxid)
							break;
					}
				}

				if (isT24Finished) {
					return rtnCode;
				} else {
					return CommonReturnCode.CBSResponseError;
				}
		}

	}

	private FEPReturnCode updateRMBTCHMTRByT24Res(Rmbtchmtr defRMBTCHMTR, String pending, HashMap<String, String> t24RepBody) {

		RmbtchmtrMapper dbRMBTCHMTR = SpringBeanFactoryUtil.getBean(RmbtchmtrMapper.class);

		try {
			// Modify by Jim, 2011/05/16, 可能是nothing, 沒更新沒關係
			if (defRMBTCHMTR == null) {
				return CommonReturnCode.Normal;
			}

			if (pending.equals(RMPending.Fail)) {
				defRMBTCHMTR.setRmbtchmtrSendCbs(RMPending.Fail);
			} else {
				defRMBTCHMTR.setRmbtchmtrSendCbs("1");
				defRMBTCHMTR.setRmbtchmtrCbsRc(getFeptxn().getFeptxnCbsRc());
			}

			if (dbRMBTCHMTR.updateByPrimaryKeySelective(defRMBTCHMTR) != 1) {
				getLogContext().setPrimaryKeys(StringUtils.join("RMBTCHMTR_SDN:", defRMBTCHMTR.getRmbtchmtrSdn(), ";", "RMBTCHMTR_TIMES:", defRMBTCHMTR.getRmbtchmtrTimes(), ";", "RMBTCHMTR_REMDATE:",
						defRMBTCHMTR.getRmbtchmtrRemdate(), ";"));
				getLogContext().setTableName("RMBTCHMTR");
				getLogContext().setReturnCode(IOReturnCode.UpdateFail);
				TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
				return IOReturnCode.UpdateFail;
			}

			if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
				getLogContext().setReturnCode(IOReturnCode.FEPTXNUpdateError);
				getLogContext().setRemark("SendCBS最後Update FEPTXN失敗");
				return IOReturnCode.FEPTXNUpdateError;
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 當主機Timeout或是拆解電文錯誤時，更新相關RMIN欄位
	 * 
	 * @param defRMIN DEFRMIN物件
	 * @param pending 是否為timeout, 若為timeout傳R, 拆解電文錯誤傳0
	 * @param CBSTxid 交易代號
	 * @param txType 交易類型
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode prepareRMINForFail(RefBase<Rmin> defRMIN, String pending, String CBSTxid, byte txType) {

		if (pending.equals(RMPending.Fail)) {
			defRMIN.get().setRminPending(RMPending.Fail); // "R"
			if (CBSTxid.equals(RMCBSTxid.R2300) || CBSTxid.equals(RMCBSTxid.R2400)) {
				defRMIN.get().setRminBrsno(rmReq.getBRSNO());
			}
		} else {

		}
		getLogContext().setRemark(StringUtils.join("更新RMIN_PENDING為", RMPending.Fail));
		// Modify by Jim, 2011/7/6, 如果T24 timeout也需要更新RMIN_TMP_STAT
		switch (CBSTxid) {
			case RMCBSTxid.R1900:
				break;
			case RMCBSTxid.R2300:
				if ("1".equals(defRMIN.get().getRminRttlrno())) {
					defRMIN.get().setRminTmpStat(RMIN_TMP_STATUS.FEPCheckError);
				} else {
					defRMIN.get().setRminTmpStat("");
				}
				defRMIN.get().setRminRegTlrno(rmReq.getENTTLRNO());
				defRMIN.get().setRminSupno1(rmReq.getSUPNO1());
				defRMIN.get().setRminKinbr(rmReq.getKINBR());
				break;
			case RMCBSTxid.R2301:
				defRMIN.get().setRminTmpStat(RMIN_TMP_STATUS.FEPCheckError);
				Rtmr defRTMR = new Rtmr();
				RtmrExtMapper dbRTMR = SpringBeanFactoryUtil.getBean(RtmrExtMapper.class);
				defRTMR.setRemdate(defRMIN.get().getRminTxdate());
				// Modify by Jim, 2011/11/3, 改用FEPNO條件查RTMR
				defRTMR.setEntseq(defRMIN.get().getRminFepno());
				defRTMR.setFiscrmsno(null);
				Rtmr rtmr = dbRTMR.getSingleRTMR(defRTMR.getRemdate(), defRTMR.getFiscrmsno(), defRTMR.getEntseq());
				if (rtmr == null) {
					getLogContext().setRemark(StringUtils.join("T24 Timeout, CBSTxid=R2301, Query RTMR no data, defRTMR.REMDATE=", defRTMR.getRemdate(), ", defRTMR.ENTSEQ=", defRTMR.getEntseq()));
					getLogContext().setReturnCode(IOReturnCode.QueryNoData);
					getLogContext().setTableDescription("小優利匯入解款檔");
					getLogContext().setIoMethd(DBIOMethod.Query);
					sendEMS(getLogContext());
					return IOReturnCode.QueryNoData;
				} else {
					defRMIN.get().setRminRegTlrno(rtmr.getChkempno());
					defRMIN.get().setRminPreTlrno(rtmr.getEmpno());
					defRMIN.get().setRminSupno1(rtmr.getSupno());
				}
				defRMIN.get().setRminKinbr(String.valueOf(ATMPConfig.getInstance().getProcessCenter()));
				break;
			case RMCBSTxid.R2400:
				defRMIN.get().setRminRegTlrno(rmReq.getENTTLRNO());
				defRMIN.get().setRminSupno1(rmReq.getSUPNO1());
				defRMIN.get().setRminKinbr(rmReq.getKINBR());
				break;
		}

		return CommonReturnCode.Normal;
	}

	private FEPReturnCode updateRMINByT24Res(Rmin defRMIN, String pending, String transactionId, HashMap<String, String> totaTransResult, String CBSTxid, byte txType) {
		@SuppressWarnings("unused")
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// Dim db As New DataAccess.DBHelper
		RminExtMapper dbRMIN = SpringBeanFactoryUtil.getBean(RminExtMapper.class);

		RmintExtMapper dbRMINT = SpringBeanFactoryUtil.getBean(RmintExtMapper.class);
		Rmint defRMINT = new Rmint();

		try {
			// db.BeginTransaction()

			if (pending.equals(RMPending.Fail)) {
				RefBase<Rmin> rminRefBase = new RefBase<>(defRMIN);
				rtnCode = prepareRMINForFail(rminRefBase, pending, CBSTxid, txType);
				defRMIN = rminRefBase.get();
			} else {
				// =====非主機Timeout的正常更新邏輯===================================================
				defRMIN.setRminPending(RMPending.Normal); // "0"
				// Modify by Jim, 2011/01/27, 如果T24回正常不需要更新CBS_RC欄位
				if (!T24RC_Normal.equals(totaTransResult.get("EB.ERROR"))) {
					defRMIN.setRminCbsRc(CBSRC);
					defRMIN.setRminTmpErrmsg(totaTransResult.get("EB.ERROR")); // RMIN_TMP_ERRMSG存完整的T24錯誤
				} else if (!isT24Finished && totaTransResult.containsKey("RECORD.STATUS")) {// for unlock
					defRMIN.setRminCbsRc(TxHelper.getRCFromErrorCode(CommonReturnCode.CBSResponseError, FEPChannel.T24, getLogContext()));
					// defRMIN.RMIN_TMP_ERRMSG = TxHelper.GetMessageFromFEPReturnCode(CommonReturnCode.CBSResponseError, LogContext)
					defRMIN.setRminTmpErrmsg(StringUtils.join("RECORD.STATUS=", totaTransResult.get("RECORD.STATUS")));
				} else {
					defRMIN.setRminCbsRc(NormalRC.T24RC_OK);
					defRMIN.setRminTmpErrmsg("");
				}

				switch (CBSTxid) {
					case RMCBSTxid.R1900: // 自動入帳
						if (T24RC_Normal.equals(totaTransResult.get("EB.ERROR")) && isT24Finished) {
							defRMIN.setRminRecdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
							defRMIN.setRminRectime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
							// Modify by Jim, 2011/05/09, SPEC修改，應更新入帳實體帳號
							defRMIN.setRminActInactno(totaTransResult.get("CREDIT.ACCT.NO"));
							// Modify by Jim, 2011/07/15, 此時才要更新RMIN_STAT=01自動入戶
							defRMIN.setRminStat(RMINStatus.AutoEnterAccount);
							if (txType == 3) {
								// DATE.TIME:1:1=1107261204
								defRMIN.setRminRectime(totaTransResult.get("DATE.TIME").substring(totaTransResult.get("DATE.TIME").length() - 4) + "00");
								// Jim, 2012/6/26, R1900自動入戶不需要更新 RMIN_ACTNO
								// If totaTransResult.ContainsKey("T.TX.O.ACCT.C") Then
								// defRMIN.RMIN_ACTNO = totaTransResult.Item("T.TX.O.ACCT.C")
								// Else
								// defRMIN.RMIN_ACTNO = totaTransResult.Item("CREDIT.ACCT.NO")
								// End If
							}
						} else {
							defRMIN.setRminStat(RMINStatus.WaitRemit);
						}
						defRMIN.setRminCbsNo(transactionId);

						break;
					case RMCBSTxid.R2300: // 分行發動之人工解款
						// Modify by Jim, 2011/06/30, 與小優利討論決定, RMIN_TMP_STAT UPDATE ‘D’時也同時UPDATE RMIN_RTTLRNO=’1’ , 因主管放行有可能沒執行到BTInbatch 就被R2300先解款, 前置櫃員可能沒值, 故改用RMIN_RTTLRNO

						if (totaTransResult.get("EB.ERROR").equals(T24RC_Normal) && isT24Finished) {
							defRMIN.setRminStat(RMINStatus.Remited);
							defRMIN.setRminRecdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
							// Modify by Jim, 2011/05/09, SPEC修改，應更新入帳實體帳號
							defRMIN.setRminActInactno(totaTransResult.get("CREDIT.ACCT.NO"));
							defRMIN.setRminCifname(rmReq.getCIFNAME()); // Candy Add Update

							// Modify by Jim, 2011/06/23, 解款成功才更新Transaction ID
							defRMIN.setRminCbsNo(transactionId);
							if (txType == 3) {
								defRMIN.setRminRectime(totaTransResult.get("DATE.TIME").substring(totaTransResult.get("DATE.TIME").length() - 4) + "00");
								defRMIN.setRminAmtType(totaTransResult.get("T.PYMT.MODE"));
								if (totaTransResult.containsKey("T.TX.O.ACCT.C")) {
									defRMIN.setRminActno(totaTransResult.get("T.TX.O.ACCT.C"));
								} else {
									defRMIN.setRminActno(totaTransResult.get("CREDIT.ACCT.NO"));
								}
							} else {
								defRMIN.setRminRectime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
								defRMIN.setRminBrsno(rmReq.getBRSNO());
								defRMIN.setRminActno(rmReq.getMACTNO());
								defRMIN.setRminEjno3(getEj());
								defRMIN.setRminAmtType(rmReq.getREMTXTP());
								defRMIN.setRminRegTlrno(rmReq.getENTTLRNO());
								defRMIN.setRminSupno1(rmReq.getSUPNO1());
								defRMIN.setRminKinbr(rmReq.getKINBR());
								// Jim, 2012/3/22, 正常的話要將TMP_RC清空
								defRMIN.setRminTmpRc("");
							}

							// Fly 2018/12/11 ENOTE無紙化 增加傳票編號
							if (totaTransResult.containsKey("T.ENOTE.SEQNO")) {
								noteSeq = totaTransResult.get("T.ENOTE.SEQNO");
							}

						} else {}

						if ("1".equals(defRMIN.getRminRttlrno())) {
							// 表示小優利已有處理解款, 需回寫FEP檢核錯誤
							defRMIN.setRminTmpStat(RMIN_TMP_STATUS.FEPCheckError);
						} else {
							// 必須清空, 若此次T24回失敗下次才可重送, 成功時清空才會和影響小優利的狀態
							defRMIN.setRminTmpStat("");
						}

						break;
					case RMCBSTxid.R2301: // 作業中心發動人工解款
						if (totaTransResult.get("EB.ERROR").equals(T24RC_Normal) && isT24Finished) {
							defRMIN.setRminStat(RMINStatus.Remited);
							defRMIN.setRminAmtType(REMTXTP.SeriesOfTransfer);
							defRMIN.setRminRecdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
							// Modify by Jim, 2011/05/09, SPEC修改，應更新入帳實體帳號
							defRMIN.setRminActInactno(totaTransResult.get("CREDIT.ACCT.NO"));
							defRMIN.setRminTmpStat(RMIN_TMP_STATUS.GrantMoney); // Only For SmallUnisys
							// Modify by Jim, 2011/06/23, 解款成功才更新Transaction ID
							defRMIN.setRminCbsNo(transactionId);
							if (txType == 3) {
								defRMIN.setRminRectime(totaTransResult.get("DATE.TIME").substring(totaTransResult.get("DATE.TIME").length() - 4) + "00");
								if (totaTransResult.containsKey("T.TX.O.ACCT.C")) {
									defRMIN.setRminActno(totaTransResult.get("T.TX.O.ACCT.C"));
								} else {
									defRMIN.setRminActno(totaTransResult.get("CREDIT.ACCT.NO"));
								}
							} else {
								defRMIN.setRminRectime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
								defRMIN.setRminKinbr(String.valueOf(ATMPConfig.getInstance().getProcessCenter()));
							}
						} else {
							defRMIN.setRminActno("");
							defRMIN.setRminActno2("");
							defRMIN.setRminTmpRc(CBSRC); // 小優利的才需要
							defRMIN.setRminTmpStat(RMIN_TMP_STATUS.InAccountByHandFail); // H:人工入帳失敗 '
						}
						break;
					case RMCBSTxid.R2400: // 解款狀態變更
						if (T24RC_Normal.equals(totaTransResult.get("EB.ERROR")) && isT24Finished) {

							defRMIN.setRminStat(RMINStatus.WaitRemit);
							defRMIN.setRminRecdate("0");
							defRMIN.setRminRectime("");
							defRMIN.setRminBrsno(rmReq.getBRSNO());
							defRMIN.setRminRegTlrno(rmReq.getENTTLRNO());
							defRMIN.setRminSupno1(rmReq.getSUPNO1());
							defRMIN.setRminActno("");
							defRMIN.setRminActInactno("");
							defRMIN.setRminKinbr(rmReq.getKINBR());
							if (RMIN_TMP_STATUS.GrantMoney.equals(defRMIN.getRminTmpStat())) {
								defRMIN.setRminTmpStat(RMIN_TMP_STATUS.ReverseMoneyByHandOK);
							}
						} else {
							if (RMIN_TMP_STATUS.GrantMoney.equals(defRMIN.getRminTmpStat())) {
								defRMIN.setRminTmpStat(RMIN_TMP_STATUS.ReverseMoneyByHandFail);
							}
							// Jim, 2012/4/2, 可能R5100時送錯CBSTXid再doubleCheck T24 是否已經沒有這筆資料(RECORD MISSING)
							// Jim, 2012/4/2, R2400 T24TOTA.CBSRC <>0000 表示R2400沖正失敗要恢復成已解款
							if (txType == 3 && !T24RC_Normal.equals(totaTransResult.get("EB.ERROR"))) {
								if (totaTransResult.get("EB.ERROR").contains("RECORD MISSING")) {
									defRMIN.setRminStat(RMINStatus.WaitRemit);
								} else {
									defRMIN.setRminStat(RMINStatus.Remited);
								}
							}
						}
						defRMIN.setRminEjno4(getEj());
						break;
				}
			}

			// Fly 2015/10/06 若11X2連資料庫失敗則再Retry一次
			int updateResult = 0;
			Rmin tempRMIN = new Rmin();
			tempRMIN = defRMIN;
			try {
				updateResult = dbRMIN.updateByPrimaryKeySelective(defRMIN);
			} catch (Exception ex) {
				if (CBSTxid.equals(RMCBSTxid.R1900)) {
					// 11X2失敗再Retry一次
					getLogContext().setRemark("dbRMIN.UpdateByPrimaryKey 失敗  嘗試Retry");
					logMessage(Level.DEBUG, getLogContext());
					updateResult = dbRMIN.updateByPrimaryKeySelective(tempRMIN);
				} else {
					// 其他類交易直接拋出錯誤
					throw ex;
				}
			}
			if (updateResult != 1) {
				// If dbRMIN.UpdateByPrimaryKey(defRMIN) <> 1 Then
				// db.RollbackTransaction()
				getLogContext().setRemark(
						StringUtils.join("Update RMIN Error; RMIN_TXDATE:", defRMIN.getRminTxdate(), ";", "RMIN_BRNO:", defRMIN.getRminBrno(), ";", "RMIN_FEPNO:", defRMIN.getRminFepno(), ";"));
				getLogContext().setReturnCode(IOReturnCode.UpdateFail);
				TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
				return IOReturnCode.UpdateFail;
			}

			logMessage(Level.DEBUG, getLogContext());

			// Modify by Jim, 2011/02/17, R2400也會解非當天的款，所以會發生沒有RMINT的情況, 因此R2400不需更新RMINT
			if (CBSTxid.equals(RMCBSTxid.R1900)) {
				defRMINT.setRmintTxdate(defRMIN.getRminTxdate());
				defRMINT.setRmintBrno(defRMIN.getRminBrno());
				defRMINT.setRmintFepno(defRMIN.getRminFepno());

				defRMINT.setRmintPending(defRMIN.getRminPending());
				defRMINT.setRmintCbsRc(defRMIN.getRminCbsRc());
				defRMINT.setRmintStat(defRMIN.getRminStat());
				defRMINT.setRmintTmpStat(defRMIN.getRminTmpStat());
				defRMINT.setRmintTmpRc(defRMIN.getRminTmpRc());
				defRMINT.setRmintRecdate(defRMIN.getRminRecdate());
				defRMINT.setRmintActInactno(defRMIN.getRminActInactno());

				// Fly 2015/10/06 若11X2連資料庫失敗則再Retry一次
				Rmint tempRMINT = new Rmint();
				tempRMINT = defRMINT;
				try {
					updateResult = dbRMINT.updateByPrimaryKeySelective(defRMINT);
				} catch (Exception ex) {
					if (RMCBSTxid.R1900.equals(CBSTxid)) {
						// 11X2失敗再Retry一次
						getLogContext().setRemark("dbRMINT.UpdateByPrimaryKey 失敗  嘗試Retry");
						logMessage(Level.DEBUG, getLogContext());
						updateResult = dbRMINT.updateByPrimaryKeySelective(tempRMINT);
					} else {
						// 其他類交易直接拋出錯誤
						throw ex;
					}
				}
				if (updateResult != 1) {
					// If dbRMINT.UpdateByPrimaryKey(defRMINT) <> 1 Then
					// db.RollbackTransaction()
					getLogContext().setRemark(StringUtils.join("FEP匯入序號=", defRMINT.getRmintFepno(), ", 送主機後更新RMINT資料錯誤, 跨行帳務可能有誤, 請查明原因"));
					getLogContext().setReturnCode(RMReturnCode.SendRMINTX);
					TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
					// Return IOReturnCode.UpdateFail
				}
			}

			// db.CommitTransaction()

			if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) < 1) {
				getLogContext().setReturnCode(IOReturnCode.FEPTXNUpdateError);
				getLogContext().setRemark("SendCBS最後Update FEPTXN失敗");
				return IOReturnCode.FEPTXNUpdateError;
			}

			return CommonReturnCode.Normal;

		} catch (Exception ex) {
			// If db.Transaction IsNot Nothing Then db.RollbackTransaction()
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 負責處理SEE function的Response格式
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>為了處理多種不同格式的T24回應，拆成多個Function</reason>
	 * <date>2011/01/05</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode processT24ResForSee(String CBSTxid, byte txType, String MsgFromT24, T24PreClass t24Message, Rmin defRMIN, Rmout defRMOUT, Rmbtchmtr defRMBTCHMTR) {

		FEPReturnCode rtnCode = CommonReturnCode.Abnormal;
		getLogContext().setProgramName(ProgramName + "ProcessT24ResForOFS");
		// 2011-05-31 Begin Modify by Candy , SEE要再判斷RECORD.STAT是否有送
		// isT24Finished = True
		// 2011-05-31 End Modify by Candy , SEE要再判斷RECORD.STAT是否有送

		if (!t24Message.parseT24RspOFS(MsgFromT24)) {
			// Modify by Jim, 2011/04/14, show給前端T24回應的錯誤
			CBSRC = MsgFromT24;
			getLogContext().setRemark("送主機後拆解電文 ParseT24RspOFS 失敗");
			getLogContext().setReturnCode(CommonReturnCode.ParseTelegramError);
			TxHelper.getRCFromErrorCode(getLogContext().getReturnCode(), FEPChannel.RM, getLogContext());
			return CommonReturnCode.ParseTelegramError;
		}

		// 2011-05-31 Begin Modify by Candy , SEE要再判斷RECORD.STAT是否有送
		if (!t24Message.getTOTATransResult().containsKey("RECORD.STATUS")) {
			isT24Finished = true;
		} else {
			// Modify by Jim, 2011/03/10, 如果是沖正，含有RECORD.STATUS=REVE時代表沖正成功
			if (t24Message.getReverseTag()) {
				if ("REVE".equals(t24Message.getTOTATransResult().get("RECORD.STATUS"))) {
					isT24Finished = true;
				} else {
					getLogContext().setRemark("沖正回應電文含有 RECORD.STATUS且不等於REVE，算 [ 沖正失敗 ]");
					logMessage(Level.DEBUG, getLogContext());
				}
			} else {
				// Jim, 2012/7/24, 如果RECORD.STATUS = MAT, 是入帳資料已經調至歷史區，但還是代表入帳有成功
				if (!"MAT".equals(t24Message.getTOTATransResult().get("RECORD.STATUS"))) {
					isT24Finished = false;
					getLogContext().setRemark(StringUtils.join("入帳回應電文含有 RECORD.STATUS=", t24Message.getTOTATransResult().get("RECORD.STATUS"), ", 算 [ 入帳失敗 ]"));
					logMessage(Level.DEBUG, getLogContext());
				} else {
					isT24Finished = true;
				}
			}
		}

		// 2011-05-31 End Modify by Candy , SEE要再判斷RECORD.STAT是否有送
		// 2011-05-31 Modify by Candy , SEE要再判斷RECORD.STATUS是否有值才能
		// Jim, 2012/4/9, 如果是R5100做R2400 see, T24回RECORD MISSING時要往下更新RMIN狀態=02待解
		if (txType == 3 && CBSTxid.equals(RMCBSTxid.R2400)) {
			// R5100做的R2400 see
		} else {

			// Jim, 2012/6/26, 修改判斷T24 return code，決定是否回查無資料的邏輯(see查無資料return code=E04852)
			CBSRC = TxHelper.getRCFromErrorCode(t24Message.getTOTATransResult().get("EB.ERROR"), FEPChannel.T24, FEPChannel.FEP, getLogContext());
			if (!T24RC_Normal.equals(t24Message.getTOTATransResult().get("EB.ERROR")) && CBSRC.equals("E04852")) {
				// 如果是call SEE function, 查不到資料要回QueryNoData給前端AA做其他處理
				return IOReturnCode.QueryNoData;
			} else if (!T24RC_Normal.equals(t24Message.getTOTATransResult().get("EB.ERROR")) || !isT24Finished) {
				return CommonReturnCode.CBSResponseError;
			}
			if ("R".equals(CBSTxid.substring(0, 1))) {// 帳務類交易
				getFeptxn().setFeptxnCbsRrn(t24Message.getTOTATransContent().get("transactionId"));
				getFeptxn().setFeptxnCbsValueDate(t24Message.getTOTATransResult().get("CREDIT.VALUE.DATE")); // 起息日*/
				// FepTxn.FEPTXN_CBS_TX_TIME = t24ReqMessage.TOTATransResult.Item("T24.TXTIME")
			}
			if ("R1900,R2300,R2301".indexOf(CBSTxid) > -1) {
				if (MsgFromT24.indexOf("T.CR.KPI.BR") > 0) {
					// Modify by Jim, 2010/11/11
					getFeptxn().setFeptxnTxDept(t24Message.getTOTATransResult().get("T.CR.KPI.BR")); // 交易帳號績效單位
				}
				// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
				getFeptxn().setFeptxnTxBrno(company.substring(company.length() - 3)); // 交易帳號掛帳行
				getFeptxn().setFeptxnTxActno(defRMIN.getRminActno()); // 交易帳號
			}
		}

		LogHelperFactory.getTraceLogger().trace("RMHost_4");

		switch (CBSTxid) {
			case RMCBSTxid.R1900:
			case RMCBSTxid.R2300:
			case RMCBSTxid.R2301:
			case RMCBSTxid.R2400:
				if (NormalRC.T24RC_OK.equals(t24Message.getTOTATransResult().get("EB.ERROR"))) {
					rtnCode = updateRMINByT24Res(defRMIN, RMPending.Normal, t24Message.getTOTATransContent().get("transactionId"), t24Message.getTOTATransResult(), CBSTxid, txType);
				} else {
					rtnCode = updateRMINByT24Res(defRMIN, RMPending.Normal, "", t24Message.getTOTATransResult(), CBSTxid, txType);
				}
				break;
			case RMCBSTxid.R1600:
				// rtnCode = UpdateRMOUTByT24Res(defRMOUT, RMPending.Normal, CBSTxid)
				break;
			case RMCBSTxid.R1001: // 大批匯款
				// R1001呼叫SEE不需要更新RMBTCHMTR(透過CBSTimeoutRerun)
				rtnCode = CommonReturnCode.Normal;
				break;
		}

		return rtnCode;
	}

	/**
	 * 查T24的PENDING資料
	 * 
	 * @param adapter t24 adapter
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <add>
	 *         <adder>Fly</adder>
	 *         <reason>For ServiceRMMoniter</reason>
	 *         <date>2014/09/16</date>
	 *         </add>
	 *         </history>
	 */
	private FEPReturnCode makeT24MessageByPENDING(RefBase<T24Adapter> adapter) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String str = "";
		try {
			// sample: ENQUIRY.SELECT,,INPUTT/123456/TW8079999,TMB.ENQ.LRM.PENDING,SEND.TIMES:EQ=3
			str = StringUtils.join("ENQUIRY.SELECT,,", RMConfig.getInstance().getT24UserName(), "/", RMConfig.getInstance().getT24SSCode(), ",TMB.ENQ.LRM.PENDING,SEND.TIMES:EQ=3");
			adapter.get().setMessageToT24(str);
		} catch (Exception ex) {
			rtnCode = CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 
	 * 
	 * @param adapter t24 adapter
	 * @return FEPReturnCode
	 * 
	 */
	private FEPReturnCode makeT24MassageByRESET(RefBase<T24Adapter> adapter, String queryDate) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String str = "";
		try {
			// sample: ENQUIRY.SELECT,,ATM.USER/123456,TMB.ENQ.LRM.RESET,T24.DATE:EQ=20120314
			str = StringUtils.join("ENQUIRY.SELECT,,", RMConfig.getInstance().getT24UserName(), "/", RMConfig.getInstance().getT24SSCode(), ",TMB.ENQ.LRM.RESET,T24.DATE:EQ=", queryDate);
			adapter.get().setMessageToT24(str);
		} catch (Exception ex) {
			rtnCode = CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	/**
	 * 給CBSTimeoutRerun call,timeout rerun要先作查詢
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>add</reason>
	 * <date>2011/1/3</date>
	 * </modify>
	 * </history>
	 */
	public FEPReturnCode makeT24MessageForEnquiry(Rmin defRMIN, Cbspend defCBSPEND, RefBase<T24PreClass> t24ReqMessage) {
		// Add by Jim, 2011/1/3, 給CBSTimeoutRerun call,timeout rerun要先作查詢, 不然也不知道T24是否CHECK EJ重覆, 直接重送會重覆入帳/記帳
		// TimeoutRerun的不會有TransactionId
		try {

			// .TITAHeader.TI_CHNN_CODE = "FEP" 'source ID
			// .TITAHeader.TI_CHNN_CODE_S = "BRANCH" 'process ID
			// .TITAHeader.TRM_BRANCH = defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			// .TITAHeader.TRMNO = defRMIN.RMIN_BRNO & "81"
			// .TITAHeader.EJFNO = defCBSPEND.CBSPEND_TBSDY_FISC & defCBSPEND.CBSPEND_EJFNO
			// .TITAHeader.FISC_DATE = defCBSPEND.CBSPEND_TBSDY_FISC

			// .TITAHeader.FEP_USER_ID = ""

			t24ReqMessage.get().setEnquiryTag(true);
			t24ReqMessage.get().setEnquiryName("T.BR.SEQNO");
			if (defCBSPEND != null && !"".equals(defCBSPEND.getCbspendTbsdyFisc())) {
				t24ReqMessage.get().getTITABody().put("ID", FEP + defCBSPEND.getCbspendTbsdyFisc() + StringUtils.leftPad(defCBSPEND.getCbspendEjfno().toString(), 8, '0'));
			} else {
				// Modify by Jim, 2011/05/23, SPEC修改, 改用FEPNO當做送T24的序號
				t24ReqMessage.get().getTITABody().put("ID", StringUtils.join(defRMIN.getRminTxdate(), StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
			}
		} catch (Exception ex) {
			getLogContext().setProgramName("RM_Host.vb-MakeT24MessageForEnquiry");
			getLogContext().setProgramException(ex);
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理帳務主機狀態檢核
	 */
	public FEPReturnCode checkCBSStatus() {

		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		Rmstat defRMSTAT = new Rmstat();
		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);

		try {
			// Jim, 2012/6/19, SPEC修改移掉check
			// If SysStatus.PropertyValue.SYSSTAT_CBS = False Then
			// rtnCode = CommonReturnCode.CBSStopService '帳務主機暫停服務
			// Return rtnCode
			// End If

			defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMSTAT = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
			if (defRMSTAT != null) {
				if (defRMSTAT.getRmstatRmFlag().equalsIgnoreCase("N")) {
					return CommonReturnCode.ChannelServiceStop;
				}
				if (defRMSTAT.getRmstatCbsInFlag().equalsIgnoreCase("N")) {
					return CommonReturnCode.CBSStopService;
				}
			} else {}

			return rtnCode;

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 負責處理組送T24 RT1010 - R1001 緊急匯款
	 * 
	 * @param defRMOUT
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/8/04</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageByR1600(Rmout defRMOUT, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();

		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("BRANCH");
			// t24ReqMessage.TITAHeader.TRM_BRANCH = defRMOUT.RMOUT_BRNO
			// t24ReqMessage.TITAHeader.TRMNO = defRMOUT.RMOUT_BRNO & "99"
			t24ReqMessage.get().getTITAHeader().setEJFNO(StringUtils.join(defRMOUT.getRmoutTxdate(), StringUtils.leftPad(getmRMData().getEj().toString(), 8, '0')));
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMOUT.RMOUT_TXDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMCBSTxid.R1600);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			titaTMB.setOrderingBank(StringUtils.join(BankIdToT24, defRMOUT.getRmoutBrno()));
			titaTMB.setTInpBr(StringUtils.join(BankIdToT24, defRMOUT.getRmoutBrno()));
			// Modify by Jim, 2011/04/26, DEBIT_AMOUNT欄位需包含手續費!
			titaTMB.setDebitAmount(BigDecimal.valueOf(defRMOUT.getRmoutTxamt().doubleValue() + defRMOUT.getRmoutActfee()));
			titaTMB.setDebitAcctNo(StringUtils.join(getmRMData().getMsgCtl().getMsgctlCbsFeeActno().trim().substring(0, 8), "0", defRMOUT.getRmoutBrno(), "0", defRMOUT.getRmoutBrno()));
			titaTMB.setCreditAcctNo(StringUtils.join(getmRMData().getMsgCtl().getMsgctlCbsSupActno().trim(), "0", defRMOUT.getRmoutSenderBank().substring(3, 6)));
			titaTMB.setTPsbRemFD(StringUtils.join("緊急匯款", defRMOUT.getRmoutBrno(), defRMOUT.getRmoutFepno()));
			// Modify by Jim, 2011/05/24, 改成用RMOUT的PK當做送T24的序號
			// Modify by Jim, 2011/05/26, SPEC修改, 送T24序號規則修改(配合T24的此欄位長度只有20位)
			titaTMB.setTBrSeqno(StringUtils.join(FEP, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2), defRMOUT.getRmoutBrno(), defRMOUT.getRmoutOriginal(),
					defRMOUT.getRmoutFepno()));

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R1001 大批匯款
	 * 
	 * @param defRMBTCHMTR
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/6/21</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageBYRMBTCHMTR(Rmbtchmtr defRMBTCHMTR, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();

		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);

			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.TRM_BRANCH = defRMBTCHMTR.RMBTCHMTR_SDN
			// t24ReqMessage.TITAHeader.TRMNO = defRMBTCHMTR.RMBTCHMTR_SDN & "99"
			t24ReqMessage.get().getTITAHeader().setEJFNO(defRMBTCHMTR.getRmbtchmtrRemdate() + StringUtils.leftPad(getmRMData().getEj().toString(), 8, '0'));
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMBTCHMTR.RMBTCHMTR_REMDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMTXCode.R1001);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			// Modify by Jim, 2011/04/26, SPEC修改: 用批號和日期去撈RMOUT資料，並將RMOUT_ACTFEE加總的值一起加在DEBIT_AMOUT欄位
			Rmout txRMOUT = new Rmout();
			RmoutExtMapper dbRMOUT = SpringBeanFactoryUtil.getBean(RmoutExtMapper.class);
			List<Rmout> dtRMOUT = new ArrayList<>();
			BigDecimal totalACTFEE = new BigDecimal(0);
			txRMOUT.setRmoutTxdate(defRMBTCHMTR.getRmbtchmtrRemdate());
			txRMOUT.setRmoutBatchno(defRMBTCHMTR.getRmbtchmtrTimes());
			txRMOUT.setRmoutFiscRtnCode(NormalRC.FISC_OK);
			dtRMOUT = dbRMOUT.getRmoutByDef(txRMOUT);
			@SuppressWarnings("unused")
			Integer sumActfee = 0;
			if (dtRMOUT.size() < 1) {
				TxHelper.getRCFromErrorCode(IOReturnCode.RMOUTNotFound, FEPChannel.RM);
			} else {
				// Modify by Jim, 2011/06/02, 只能抓財金return code = 0001的
				totalACTFEE = BigDecimal.valueOf(dtRMOUT.stream().filter(item -> item.getRmoutActfee() != null && "0001".equals(item.getRmoutFiscRtnCode())).mapToInt(Rmout::getRmoutActfee).sum());
			}

			titaTMB.setOrderingBank(StringUtils.join(BankIdToT24, defRMBTCHMTR.getRmbtchmtrKinbr()));
			titaTMB.setTInpBr(StringUtils.join(BankIdToT24, defRMBTCHMTR.getRmbtchmtrKinbr()));
			// Fly 2021/09/13 避免送T24有小數位 將值4捨5入
			// titaTMB.DEBIT_AMOUNT = Convert.ToDecimal(defRMBTCHMTR.RMBTCHMTR_SUCESS_AMT) + totalACTFEE
			titaTMB.setDebitAmount(BigDecimal.valueOf(Math.round(defRMBTCHMTR.getRmbtchmtrSucessAmt().doubleValue() + totalACTFEE.doubleValue())));
			titaTMB.setDebitAcctNo(
					StringUtils.join(getmRMData().getMsgCtl().getMsgctlCbsFeeActno().trim().substring(0, 8), "0", defRMBTCHMTR.getRmbtchmtrKinbr(), "0", defRMBTCHMTR.getRmbtchmtrKinbr()));
			titaTMB.setCreditAcctNo(StringUtils.join(getmRMData().getMsgCtl().getMsgctlCbsSupActno().trim(), "0", defRMBTCHMTR.getRmbtchmtrSdn()));
			titaTMB.setTPsbRemFD(defRMBTCHMTR.getRmbtchmtrTimes());
			// Modify by Jim, 2011/05/23, SPEC修改, 改用defRMBTCHMTR的PK當做送T24的序號
			// Modify by Jim, 2011/05/26, SPEC修改, 送T24序號規則修改(配合T24的此欄位長度只有20位)
			titaTMB.setTBrSeqno(StringUtils.join(FEP, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).substring(2),
					defRMBTCHMTR.getRmbtchmtrTimes().substring(defRMBTCHMTR.getRmbtchmtrTimes().length() - 10)));

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R8888 匯出更正
	 * 
	 * @param defRMOUT
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/8/04</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageByRT1101(Rmout defRMOUT, RefBase<T24PreClass> t24ReqMessage) {
		@SuppressWarnings("unused")
		T24TITATMB titaTMB = new T24TITATMB();

		try {
			// t24ReqMessage.TITAHeader.TI_CHNN_CODE = "FEP"
			// t24ReqMessage.TITAHeader.TI_CHNN_CODE_S = "BRANCH"
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMOUT.RMOUT_TXDATE
			// t24ReqMessage.TITAHeader.FEP_TXN_CODE = "R8888"
			// t24ReqMessage.TITAHeader.EJFNO = defRMOUT.RMOUT_TXDATE & RMTxData.EJ.ToString().PadLeft(8, "0"c)
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			// t24ReqMessage.TITAHeader.FEP_USER_ID = ""

			// t24ReqMessage.ReverseTag = True
			t24ReqMessage.get().setTransactionId(defRMOUT.getRmoutT24No().trim());

			// titaTMB.T_SN_FISC_NO = "TW807" + "0" + defRMOUT.RMOUT_BRNO + defRMOUT.RMOUT_FEPNO
			// titaTMB.T_SN_FISC_NO = _BankIdToT24 + defRMOUT.RMOUT_BRNO + defRMOUT.RMOUT_FEPNO
			// titaTMB.T_BR_SEQNO = t24ReqMessage.TITAHeader.EJFNO
			// titaTMB.GenDictionary(t24ReqMessage.TITABody)

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 刪除已更正之T.BR.SEQNO
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Jim</modifier>
	 * <reason>add</reason>
	 * <date>2011/6/30</date>
	 * </modify>
	 * </history>
	 */
	public FEPReturnCode makeT24MessageByR2401(Rmin defRMIN, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();

		// Sample
		// TMBI.C.EJ.NO.RSV.S,/I/PROCESS,ATM.USER/123456,,T.BR.SEQNO:1:=FEP201106300000001,FT.ID:1:=FT1121500ML8
		// (1)Request: (T.BR.SEQNO正確 FT.ID不正確):TMBI.C.EJ.NO.RSV.S,/S/PROCESS,ATM.USER/123456,,T.BR.SEQNO:1:= FEP201106300000001,FT.ID:1:=FT1111800001
		// Response: FEP201106300000001//-1,NO,EB-FT.REC.INCONST
		// (2)Request: (T.BR (T.BR.SEQNO不正確 FT.ID正確):TMBI.C.EJ.NO.RSV.S,/I/PROCESS,ATM.USER/123456,,T.BR.SEQNO:1:= FEP201106300000001,FT.ID:1:=FT1121500ML8
		// Response:FEP201106300000001//-1,NO,E12696EB-EB-NO.RECORD?E12696EB-截至目前為止無交易紀錄
		// (3)Request (T.BR.SEQNO正確 FT.ID正確): TMBI.C.EJ.NO.RSV.S,/I/PROCESS,ATM.USER/123456,,T.BR.SEQNO:1:= FEP201106300000001,FT.ID:1:=FT1121500ML8
		// Response(交易成功): FEP201106300000001//1/T.BR.SEQNO:1:= FEP201106300000001,FT.ID:1:=FT1121500ML8

		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMIN.RMIN_RECDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMTXCode.R2400);
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			titaTMB.setTBrSeqno(StringUtils.join(FEP, defRMIN.getRminTxdate(), defRMIN.getRminFepno()));
			titaTMB.setFtId(defRMIN.getRminCbsNo().trim());
			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R2400 匯出更正
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Maxine</modifier>
	 * <reason>add</reason>
	 * <date>2010/8/04</date>
	 * </modify>
	 * </history>
	 */
	public FEPReturnCode makeT24MessageByR2400(Rmin defRMIN, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();

		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMIN.RMIN_RECDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMTXCode.R2400);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			t24ReqMessage.get().setReverseTag(true);
			t24ReqMessage.get().setTransactionId(defRMIN.getRminCbsNo().trim());
			titaTMB.setTOrFiscNo(StringUtils.join(defRMIN.getRminTxdate() + defRMIN.getRminBrno() + defRMIN.getRminFepno()));

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R2301 作業中心發動人工解款
	 * 
	 * @param defRMIN
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/8/04</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>小優利虛擬帳號UPDATE在RMIN_ACTNO2 , 故要先判斷此欄有值時用此送T24</reason>
	 *         <date>2011/5/18</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageByR2301(Rmin defRMIN, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();
		String outName = "";
		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			// Modify by Jim, 2011/06/29, TI_CHNN_CODE_S 改成 RM
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.TRM_BRANCH = defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			// t24ReqMessage.TITAHeader.TRMNO = defRMIN.RMIN_BRNO & "81"
			t24ReqMessage.get().getTITAHeader().setEJFNO(StringUtils.join(defRMIN.getRminTxdate(), StringUtils.leftPad(getmRMData().getEj().toString(), 8, '0')));
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMIN.RMIN_TXDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMCBSTxid.R2301);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			// Jim, 2012/3/28, 需要先將RMIN_IN_NAME, RMOUT_OUT_NAME內某些字元轉成T24的規格
			outName = convertSpecialCharForT24(defRMIN.getRminOutName());

			// Modify by Jim, 2011/05/23, SPEC修改, 改用FEPNO當做送T24的序號
			titaTMB.setTBrSeqno(StringUtils.join(FEP, defRMIN.getRminTxdate(), StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
			titaTMB.setTOrFiscNo(StringUtils.join(defRMIN.getRminTxdate(), defRMIN.getRminBrno(), defRMIN.getRminFepno()));

			// Modify by Jim, 2011/05/18, 小優利虛擬帳號UPDATE在RMIN_ACTNO2 , 故要先判斷此欄有值時用此送T24
			if (StringUtils.isBlank(defRMIN.getRminActno2()) || "".equals(defRMIN.getRminActno2())) {
				titaTMB.setCreditAcctNo(defRMIN.getRminActno()); // 小優利修改後之正確帳號 */
			} else {
				titaTMB.setCreditAcctNo(defRMIN.getRminActno2()); // 小優利修改後之正確帳號 */
			}
			titaTMB.setTPymtMode(REMTXTP.SeriesOfTransfer); // 轉帳連動 */
			// Fly 2014/07/14 調整成四捨五入
			titaTMB.setDebitAmount(BigDecimal.valueOf(Math.round(defRMIN.getRminTxamt().doubleValue())));
			// titaTMB.DEBIT_AMOUNT = CType(defRMIN.RMIN_TXAMT, Int64)
			// Jim, 2012/3/22, SPEC modify
			// titaTMB.T_BEN_CUSTOMER = defRMIN.RMIN_CIFNAME '小優利修改後之正確戶名'
			titaTMB.setTBenCustomer("");
			// titaTMB.T_BEN_BANK = "TW8070" + defRMIN.RMIN_RECEIVER_BANK.Substring(0, 3)
			// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
			// titaTMB.T_BEN_BANK = BankIdToT24 + defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			titaTMB.setTBenBank(StringUtils.join(BankIdToT24, company.substring(company.length() - 3)));
			titaTMB.setOrderingBank(StringUtils.join(BankIdToT24, defRMIN.getRminReceiverBank().substring(3, 6)));
			// Jim, 2012/3/22, SPEC modify
			// titaTMB.T_TRN_PRT_NAME = defRMIN.RMIN_OUT_NAME
			titaTMB.setTTrnPrtName("");
			titaTMB.setDebitAcctNo(StringUtils.join(FEPCache.getMsgctrl(defRMIN.getRminFiscSndCode() + "00").getMsgctlCbsSupActno().trim(), "0", company.substring(company.length() - 3)));
			titaTMB.setTPsbMemoD("台幣匯款");

			// Modify by Jim, 20111/06/15, 改分隔符號和帳號欄位改成用RMIN_IN_ACC_ID_NO
			titaTMB.getTPsbRinfD().put(1, StringUtils.join(defRMIN.getRminTxdate(), "^", defRMIN.getRminFepno(), "^", defRMIN.getRminInAccIdNo()));
			titaTMB.getTPsbRinfD().put(2, StringUtils.join(titaTMB.getTBenBank(), "^", titaTMB.getDebitAmount().toString()));
			// Begin Modify by Candy 2014-11-27 永豐PCR修改:當匯入款，其匯款種類為17時，
			// T.PSB.MEMO.C>>原放”台幣匯款”改放”匯出退匯”
			// T.PSB.REM.S.C、T.PSB.REM.F.C >>原放匯款人資料改放解款人資料
			// titaTMB.T_PSB_MEMO_C = "台幣匯款"
			// 'Jim, 2012/4/2, 取長度不用分中英文，直接用substring就好
			// 'Jim, 2012/3/22, SPEC修改, T.PSB.REM.F.C 裁60位送
			// 'titaTMB.T_PSB_REM_F_C = titaTMB.T_TRN_PRT_NAME
			// 'Dim sb() As Byte = Encoding.GetEncoding("Unicode").GetBytes(outName)
			// 'Modified by Jim, 2010/08/24, 避免長度不足的exception
			// If outName.Length >= 12 Then
			// titaTMB.T_PSB_REM_S_C = outName.Substring(0, 12)
			// Else
			// titaTMB.T_PSB_REM_S_C = outName
			// End If
			// If outName.Length > 65 Then
			// titaTMB.T_PSB_REM_F_C = outName.Substring(0, 65)
			// Else
			// titaTMB.T_PSB_REM_F_C = outName
			// End If
			String inName = "";
			inName = convertSpecialCharForT24(defRMIN.getRminInName());
			if ("1172".equals(defRMIN.getRminFiscSndCode())) {
				titaTMB.setTPsbMemoC("匯出退匯");
				if (inName.length() > 12) {
					titaTMB.setTPsbRemSC(inName.substring(0, 12));
				} else {
					titaTMB.setTPsbRemSC(inName);
				}
				if (inName.length() > 65) {
					titaTMB.getTPsbRemFC().put(1, inName.substring(0, 65));
				} else {
					titaTMB.getTPsbRemFC().put(1, inName);
				}
			} else {
				titaTMB.setTPsbMemoC("台幣匯款");
				if (outName.length() > 12) {
					titaTMB.setTPsbRemSC(outName.substring(0, 12));
				} else {
					titaTMB.setTPsbRemSC(outName);
				}
				if (outName.length() > 65) {
					titaTMB.getTPsbRemFC().put(1, outName.substring(0, 65));
				} else {
					titaTMB.getTPsbRemFC().put(1, outName);
				}
			}
			// end Modify2014-11-27

			if (outName.length() > 50) {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName.substring(0, 50), "^"));
			} else {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName, "^"));
			}
			// titaTMB.T_PSB_RINF_C = "FUT" + "^" + defRMIN.RMIN_SENDER_BANK + "^^" + titaTMB.T_TRN_PRT_NAME + "^"

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());

			// Modify by Jim, 2011/04/19, SPEC修改
			// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
			// titaTMB.T_COLL_BR = BankIdToT24 & defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			titaTMB.setTCollBr(StringUtils.join(BankIdToT24, company.substring(company.length() - 3)));
			// Modify by Jim, 2011/04/22, T.INP.BR = TW8070 + ProcessCenter
			titaTMB.setTInpBr(StringUtils.join(BankIdToT24, ATMPConfig.getInstance().getProcessCenter()));

			// Fly 2017/08/01 增加送附言欄至T24長備註第二組
			// Fly 2017/10/17 增加特殊字元轉換
			// Fly 2017/11/24 特殊字元轉換改成全轉全形
			if (defRMIN.getRminMemo().length() > 60) {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo().substring(0, 60), PolyfillUtil.VbStrConv.Wide, 1028));
			} else {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo(), PolyfillUtil.VbStrConv.Wide, 1028));
			}

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R2300 分行發動之人工解款 電文
	 * 
	 * @param defRMIN
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/8/04</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageByR2300(Rmin defRMIN, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();
		String inName = "";
		String outName = "";
		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			// Modify by Jim, 2011/06/29, channel 改成 RM
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.TRM_BRANCH = defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			// t24ReqMessage.TITAHeader.TRMNO = defRMIN.RMIN_BRNO & "81"
			t24ReqMessage.get().getTITAHeader().setEJFNO(StringUtils.join(defRMIN.getRminTxdate(), StringUtils.leftPad(getmRMData().getEj().toString(), 8, '0')));
			// t24ReqMessage.TITAHeader.FISC_DATE = defRMIN.RMIN_TXDATE
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMTXCode.R2300);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			// Jim, 2012/3/28, 需要先將RMIN_IN_NAME, RMOUT_OUT_NAME內某些字元轉成T24的規格
			inName = convertSpecialCharForT24(defRMIN.getRminOutName());
			outName = convertSpecialCharForT24(defRMIN.getRminOutName());

			// Modify by Jim, 2011/06/02, 因為不一定解當天的款，所以要用RMIN_TXDATE
			// Modify by Jim, 2011/05/23, SPEC修改, 改用FEPNO當做送T24的序號
			titaTMB.setTBrSeqno(StringUtils.join(FEP, defRMIN.getRminTxdate(), StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
			titaTMB.setTOrFiscNo(StringUtils.join(defRMIN.getRminTxdate() + defRMIN.getRminBrno() + defRMIN.getRminFepno()));
			titaTMB.setCreditAcctNo(rmReq.getMACTNO());
			// titaTMB.T_PYMT_MODE = REMTXTP.SeriesOfTransfer '轉帳連動

			titaTMB.setTPymtMode(rmReq.getREMTXTP()); // RMIN NOT FILED Change RMReq.REMTXTP
			// Fly 2014/07/14 調整成四捨五入
			titaTMB.setDebitAmount(BigDecimal.valueOf(Math.round(defRMIN.getRminTxamt().doubleValue())));
			// titaTMB.DEBIT_AMOUNT = CType(defRMIN.RMIN_TXAMT, Int64)
			// Jim, 2012/3/22, SPEC修改
			// titaTMB.T_BEN_CUSTOMER = rmReq.CIFNAME
			titaTMB.setTBenCustomer("");
			// titaTMB.T_BEN_BANK = "TW8070" + defRMIN.RMIN_RECEIVER_BANK.Substring(0, 3)
			// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
			// titaTMB.T_BEN_BANK = BankIdToT24 + defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			titaTMB.setTBenBank(StringUtils.join(BankIdToT24, company.substring(company.length() - 3)));
			titaTMB.setOrderingBank(BankIdToT24 + defRMIN.getRminReceiverBank().substring(3, 6));
			// titaTMB.T_TRN_PRT_NAME = defRMIN.RMIN_OUT_NAME
			titaTMB.setTTrnPrtName("");
			titaTMB.setDebitAcctNo(StringUtils.join(FEPCache.getMsgctrl(defRMIN.getRminFiscSndCode() + "00").getMsgctlCbsSupActno().trim(), "0", company.substring(company.length() - 3)));
			titaTMB.setTPsbMemoD("台幣匯款");
			// Modify by Jim, 20111/06/15, 改分隔符號和帳號欄位改成用RMIN_IN_ACC_ID_NO
			titaTMB.getTPsbRinfD().put(1, StringUtils.join(defRMIN.getRminTxdate(), "^", defRMIN.getRminFepno(), "^", defRMIN.getRminInAccIdNo()));
			titaTMB.getTPsbRinfD().put(2, StringUtils.join(titaTMB.getTBenBank(), "^", titaTMB.getDebitAmount().toString()));
			// Jim, 2012/4/2, 取長度不用分中英文，直接用substring就好
			// Begin Modify by Candy 2014-11-27 永豐PCR修改:當匯入款，其匯款種類為17時，
			// T.PSB.MEMO.C>>原放”台幣匯款”改放”匯出退匯”
			// T.PSB.REM.S.C、T.PSB.REM.F.C >>原放匯款人資料改放解款人資料
			// titaTMB.T_PSB_MEMO_C = "台幣匯款"
			// If outName.Length > 12 Then
			// titaTMB.T_PSB_REM_S_C = outName.Substring(0, 12)
			// Else
			// titaTMB.T_PSB_REM_S_C = outName
			// End If
			// 'Jim, 2012/3/22, SPEC修改, T.PSB.REM.F.C 裁60位送
			// 'titaTMB.T_PSB_REM_F_C = titaTMB.T_TRN_PRT_NAME
			// 'Dim sb() As Byte = Encoding.GetEncoding("Unicode").GetBytes(outName)
			// If outName.Length > 65 Then
			// titaTMB.T_PSB_REM_F_C = outName.Substring(0, 65)
			// Else
			// titaTMB.T_PSB_REM_F_C = outName
			// End If
			if ("1172".equals(defRMIN.getRminFiscSndCode())) {
				titaTMB.setTPsbMemoC("匯出退匯");
				if (inName.length() > 12) {
					titaTMB.setTPsbRemSC(inName.substring(0, 12));
				} else {
					titaTMB.setTPsbRemSC(inName);
				}
				if (inName.length() > 65) {
					titaTMB.getTPsbRemFC().put(1, inName.substring(0, 65));
				} else {
					titaTMB.getTPsbRemFC().put(1, inName);
				}
			} else {
				titaTMB.setTPsbMemoC("台幣匯款");
				if (outName.length() > 12) {
					titaTMB.setTPsbRemSC(outName.substring(0, 12));
				} else {
					titaTMB.setTPsbRemSC(outName);
				}
				if (outName.length() > 65) {
					titaTMB.getTPsbRemFC().put(1, outName.substring(0, 65));
				} else {
					titaTMB.getTPsbRemFC().put(1, outName);
				}
			}
			// end Modify2014-11-27

			if (outName.length() > 50) {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName.substring(0, 50), "^"));
			} else {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName, "^"));
			}

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());
			// Modify by Jim, 2011/04/19, SPEC修改
			titaTMB.setTTxnCode(RMTXCode.R2300);
			// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
			// titaTMB.T_COLL_BR = BankIdToT24 & defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3) '收件行
			titaTMB.setTCollBr(StringUtils.join(BankIdToT24, company.substring(company.length() - 3)));
			// Modify by Jim, 2011/06/07, T_INP_BR = 輸入行 (EX.TW8070001)
			titaTMB.setTInpBr(StringUtils.join(BankIdToT24, rmReq.getBranchID().substring(rmReq.getBranchID().length() - 3)));

			// Fly 2017/08/01 增加送附言欄至T24長備註第二組
			// Fly 2017/10/17 增加特殊字元轉換
			// Fly 2017/11/24 特殊字元轉換改成全轉全形
			if (defRMIN.getRminMemo().length() > 60) {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo().substring(0, 60), PolyfillUtil.VbStrConv.Wide, 1028));
			} else {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo(), PolyfillUtil.VbStrConv.Wide, 1028));
			}

			// Fly 2018/12/11 ENOTE無紙化 增加傳票編號
			titaTMB.setTEnoteSeqno(noteSeq);

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	/**
	 * 負責處理組送T24 R1900 自動入帳 電文
	 * 
	 * @param defRMIN
	 * @param t24ReqMessage
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/5/10</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode makeT24MessageBy11X2(Rmin defRMIN, RefBase<T24PreClass> t24ReqMessage) {
		T24TITATMB titaTMB = new T24TITATMB();
		String inName = "";
		String outName = "";

		try {
			t24ReqMessage.get().getTITAHeader().setTiChnnCode(FEP);
			// Modify by Jim, 2011/06/29, channel 改成 RM
			t24ReqMessage.get().getTITAHeader().setTiChnnCodeS("RM");
			// t24ReqMessage.TITAHeader.TRM_BRANCH = defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			// t24ReqMessage.TITAHeader.TRMNO = defRMIN.RMIN_BRNO & "81"
			t24ReqMessage.get().getTITAHeader().setEJFNO(StringUtils.join(defRMIN.getRminTxdate(), StringUtils.leftPad(defRMIN.getRminEjno2().toString(), 8, '0')));
			t24ReqMessage.get().getTITAHeader().setFiscDate(defRMIN.getRminTxdate());
			t24ReqMessage.get().getTITAHeader().setFeptxnCode(RMCBSTxid.R1900);
			// t24ReqMessage.TITAHeader.REG_FLAG = ""
			t24ReqMessage.get().getTITAHeader().setFepUserId("");
			t24ReqMessage.get().getTITAHeader().setFepPassword("");

			// Jim, 2012/3/28, 需要先將RMIN_IN_NAME, RMOUT_OUT_NAME內某些字元轉成T24的規格
			inName = convertSpecialCharForT24(defRMIN.getRminInName());
			outName = convertSpecialCharForT24(defRMIN.getRminOutName());

			titaTMB.setTOrFiscNo(StringUtils.join(defRMIN.getRminTxdate(), defRMIN.getRminBrno(), defRMIN.getRminFepno()));
			titaTMB.setCreditAcctNo(defRMIN.getRminInAccIdNo());
			titaTMB.setTPymtMode(REMTXTP.SeriesOfTransfer); // 轉帳連動
			// Fly 2014/07/14 調整成四捨五入
			titaTMB.setDebitAmount(BigDecimal.valueOf(Math.round(defRMIN.getRminTxamt().doubleValue())));
			// titaTMB.DEBIT_AMOUNT = CType(defRMIN.RMIN_TXAMT, Int64)

			// Jim, 2012/3/8, 去掉全形和半形, 否則T24會回錯誤
			// titaTMB.T_BEN_CUSTOMER = defRMIN.RMIN_IN_NAME.Replace(" ", "").Replace(" ", "")
			titaTMB.setTBenCustomer(inName.replace(" ", "").replace("　", ""));
			// titaTMB.T_BEN_BANK = "TW8070" + defRMIN.RMIN_RECEIVER_BANK.Substring(0, 3)
			// Fly 2017/4/11 (週二) 下午 01:16 中正、西門(簡)分行整併
			// titaTMB.T_BEN_BANK = BankIdToT24 + defRMIN.RMIN_RECEIVER_BANK.Substring(3, 3)
			titaTMB.setTBenBank(StringUtils.join(BankIdToT24, company.substring(company.length() - 3)));
			titaTMB.setOrderingBank(StringUtils.join(BankIdToT24, defRMIN.getRminReceiverBank().substring(3, 6)));
			// Jim, 2012/3/22, SPEC修改, T.TRN.PRT.NAME送空，T.PSB.REM.F.C 裁60位送
			// titaTMB.T_TRN_PRT_NAME = defRMIN.RMIN_OUT_NAME
			titaTMB.setTTrnPrtName("");
			titaTMB.setDebitAcctNo(StringUtils.join(FEPCache.getMsgctrl(defRMIN.getRminFiscSndCode() + "00").getMsgctlCbsSupActno().trim(), "0", company.substring(company.length() - 3)));
			// titaTMB.DEBIT_ACCT_NO = RMTxData.MsgCtl.MSGCTL_CBS_SUP_ACTNO
			titaTMB.setTPsbMemoD("台幣匯款");

			// Modify by Jim, 20111/06/15, 改分隔符號和帳號欄位改成用RMIN_IN_ACC_ID_NO
			titaTMB.getTPsbRinfD().put(1, StringUtils.join(defRMIN.getRminTxdate(), "^", defRMIN.getRminFepno(), "^", defRMIN.getRminInAccIdNo()));
			titaTMB.getTPsbRinfD().put(2, StringUtils.join(titaTMB.getTBenBank(), "^", titaTMB.getDebitAmount().toString()));
			// Modify by Jim, 2011/05/23, SPEC修改, 改用FEPNO當做送T24的序號
			titaTMB.setTBrSeqno(StringUtils.join(FEP, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StringUtils.leftPad(defRMIN.getRminFepno(), 7, '0')));
			titaTMB.setTPsbMemoC("台幣匯款");

			// Jim, 2012/4/2, 取長度不用分中英文，直接用substring就好
			if (outName.length() > 12) {
				titaTMB.setTPsbRemSC(outName.substring(0, 12));
			} else {
				titaTMB.setTPsbRemSC(outName);
			}
			// Jim, 2012/3/22, SPEC修改, T.TRN.PRT.NAME送空，T.PSB.REM.F.C 裁60位送
			// titaTMB.T_PSB_REM_F_C = titaTMB.T_TRN_PRT_NAME
			// Dim sb() As Byte = Encoding.GetEncoding("Unicode").GetBytes(outName)
			// Modify by Candy 2014-11-27 永豐PCR修改:當匯入款，其匯款種類為17時，
			// T.PSB.MEMO.C>>原放”台幣匯款”改放”匯出退匯”
			// T.PSB.REM.S.C、T.PSB.REM.F.C >>原放匯款人資料改放解款人資料
			if (outName.length() > 65) {
				titaTMB.getTPsbRemFC().put(1, outName.substring(0, 65));
			} else {
				titaTMB.getTPsbRemFC().put(1, outName);
			}
			if (outName.length() > 50) {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName.substring(0, 50), "^"));
			} else {
				titaTMB.setTPsbRinfC(StringUtils.join("FUT", "^", defRMIN.getRminSenderBank(), "^^", outName, "^"));
			}

			// titaTMB.T_PSB_RINF_C = "FUT" + "^" + defRMIN.RMIN_SENDER_BANK + "^^" + titaTMB.T_TRN_PRT_NAME + "^^"

			// Modify by Jim, 2011/01/20, 新增送T24欄位
			titaTMB.setTransactionType("ACTF");
			titaTMB.setDebitCurrency(CurrencyType.TWD.toString());

			// Fly 2017/08/01 增加送附言欄至T24長備註第二組
			// Fly 2017/10/17 增加特殊字元轉換
			// Fly 2017/11/24 特殊字元轉換改成全轉全形
			if (defRMIN.getRminMemo().length() > 60) {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo().substring(0, 60), PolyfillUtil.VbStrConv.Wide, 1028));
			} else {
				titaTMB.getTPsbRemFC().put(2, PolyfillUtil.strConv(defRMIN.getRminMemo(), PolyfillUtil.VbStrConv.Wide, 1028));
			}

			titaTMB.genDictionary(t24ReqMessage.get().getTITABody());

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return CommonReturnCode.Normal;
	}

	private String convertSpecialCharForT24(String str) {
		// Fly 2017/10/31 增加轉換 - => －
		str = str.replace(",", "?").replace("\"", "|").replace("/", "^").replace("_", "'_'").replace("-", "－");
		return str;
	}

}
