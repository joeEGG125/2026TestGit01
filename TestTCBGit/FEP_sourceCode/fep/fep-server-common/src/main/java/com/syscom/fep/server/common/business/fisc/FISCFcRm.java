package com.syscom.fep.server.common.business.fisc;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.BsdaysExtMapper;
import com.syscom.fep.mybatis.ext.mapper.CurcdExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FcaptotExtMapper;
import com.syscom.fep.mybatis.ext.mapper.FcrmtotExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.FISCAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class FISCFcRm extends FISCRm {

	protected FISCFcRm() {
		super();
	}

	protected FISCFcRm(FISCData fiscMessage) {
		super(fiscMessage);
	}

	/**
	 * 負責處理產生財金外幣匯款 REQUEST 電文並送出等待回應
	 * 
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
	public FEPReturnCode sendFCRMRequestToFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		FISCAdapter fiscAdapter = new FISCAdapter(getFISCTxData());
		Bitmapdef oBitMap = getBitmapData(fiscRMReq.getMessageType() + fiscRMReq.getProcessingCode());

		try {
			// 2.更新交易記錄(FEPTXN)
			getFeptxn().setFeptxnStan(StringUtils.leftPad(fiscFCRMReq.getSystemTraceAuditNo(), 7, '0'));
			getFeptxn().setFeptxnRmsno(fiscFCRMReq.getBankNo());
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request);
			getFeptxn().setFeptxnDesBkno(fiscFCRMReq.getTxnDestinationInstituteId().substring(0, 3));
			getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(true));
			getFeptxn().setFeptxnReqRc(fiscFCRMReq.getResponseCode());
			// .FEPTXN_REQ_TIME = Now
			// David modify on 2010-04-12
			// FEPTXN_AA_RC 本來就是FEP內部錯誤代碼，直接用FEPReturnCode即可
			getFeptxn().setFeptxnAaRc(FEPReturnCode.FISCTimeout.getValue());
			// .FEPTXN_AA_RC = TxHelper.GetRCFromErrorCode(FEPReturnCode.FISCTimeout, FEPChannel.FISC).PadLeft(4, "0"c)

			rtnCode = updateFepTxnForRM(getFeptxn());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 3. 送 REQ 電文至財金若為外幣匯款類則不等待回應(由另一支接收回應訊息), 其他則等待回應
			fiscRMReq.makeFISCMsg();
			// 準備送至財金的物件
			fiscAdapter.setFEPSubSystem(getFISCTxData().getTxSubSystem());
			fiscAdapter.setChannel(getFISCTxData().getTxChannel());
			fiscAdapter.setEj(getFISCTxData().getEj());
			fiscAdapter.setStan(getFISCTxData().getStan());
			fiscAdapter.setMessageToFISC(fiscFCRMReq.getFISCMessage());

			if ("1611".equals(getFeptxn().getFeptxnPcode())) {
				fiscAdapter.setNoWait(true);
			} else {
				fiscAdapter.setNoWait(false);
				fiscAdapter.setTimeout(oBitMap.getBitmapdefTimeout().intValue());
			}

			rtnCode = fiscAdapter.sendReceive();
			if (rtnCode == CommonReturnCode.Normal) {
				if (!"1611".equals(getFeptxn().getFeptxnPcode())) {
					fiscFCRMRes.setFISCMessage(fiscAdapter.getMessageFromFISC());

					if (fiscRMRes.getFISCMessage() == null) {
						rtnCode = FISCReturnCode.FISCTimeout;
					} else {
						fiscRMRes.parseFISCMsg();

						getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
						// FepTxn.FEPTXN_REP_TIME = Date.Now
						getFeptxn().setFeptxnAaRc(rtnCode.getValue());

						rtnCode = updateFepTxnForRM(getFeptxn());
						if (rtnCode != CommonReturnCode.Normal) {
							return rtnCode;
						}
					}
				} else {
					getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
					// FepTxn.FEPTXN_REP_TIME = Date.Now
					getFeptxn().setFeptxnAaRc(rtnCode.getValue());

					rtnCode = updateFepTxnForRM(getFeptxn());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}

			} else {
				rtnCode = FISCReturnCode.FISCTimeout;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 負責準備FCAPTOT \ FCRMTOT內容此程式為 AA\RM\1611之副程式
	 * 
	 * @param sKind
	 * @param defFEPTXN
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Maxine</modifier>
	 *         <reason>add</reason>
	 *         <date>2010/5/10</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
	 *         <date>2010/03/16</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode processFCAPTOTAndFCRMTOT(String sKind, Feptxn defFEPTXN) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		FcaptotExtMapper dbFCAPTOT = SpringBeanFactoryUtil.getBean(FcaptotExtMapper.class);
		Fcaptot defFCAPTOT = new Fcaptot();

		FcrmtotExtMapper dbFCRMTOT = SpringBeanFactoryUtil.getBean(FcrmtotExtMapper.class);
		Fcrmtot defFCRMTOT = new Fcrmtot();

		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {

			if ("1611".equals(fiscFCRMReq.getProcessingCode()) && SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscFCRMReq.getTxnDestinationInstituteId())) {
				defFCAPTOT.setFcaptotCntDr(1);
				defFCAPTOT.setFcaptotAmtDr(defFEPTXN.getFeptxnTxAmt());
				defFCAPTOT.setFcaptotBrno(fiscFCRMReq.getReceiverBank().substring(0, 3));
			} else {
				defFCAPTOT.setFcaptotCntCr(1);
				defFCAPTOT.setFcaptotAmtCr(defFEPTXN.getFeptxnTxAmt());
				defFCAPTOT.setFcaptotBrno(fiscFCRMReq.getSenderBank().substring(0, 3));
			}
			defFCAPTOT.setFcaptotApid(fiscFCRMReq.getFcSubpcode());
			defFCAPTOT.setFcaptotStDate(defFEPTXN.getFeptxnTbsdy());
			defFCAPTOT.setFcaptotCurrency(fiscFCRMReq.getCURRENCY());

			if (dbFCAPTOT.updateForProcessFCAPTOT(defFCAPTOT) == 0) {
				dbFCAPTOT.insertSelective(defFCAPTOT);
			}

			if ("1611".equals(fiscFCRMReq.getProcessingCode()) && SysStatus.getPropertyValue().getSysstatHbkno().equals(fiscFCRMReq.getTxnDestinationInstituteId())) {
				defFCRMTOT.setFcrmtotFiscInCount(1);
				defFCRMTOT.setFcrmtotFiscInAmt(defFEPTXN.getFeptxnTxAmt());
			} else {
				defFCRMTOT.setFcrmtotFiscOutCount(1);
				defFCRMTOT.setFcrmtotFiscOutAmt(defFEPTXN.getFeptxnTxAmt());
			}

			defFCRMTOT.setFcrmtotBrno("000");
			defFCRMTOT.setFcrmtotCurrency(fiscFCRMReq.getCURRENCY());
			defFCRMTOT.setFcrmtotTxdate(defFEPTXN.getFeptxnTbsdy());

			if (dbFCRMTOT.updateForProcessFCRMTOT(defFCRMTOT) == 0) {
				dbFCRMTOT.insertSelective(defFCRMTOT);
			}

			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	/**
	 * 負責準備FCRMIN外幣匯入主檔內容此程式為 AA\RM\1611之副程式
	 * 
	 * @param wkPEND
	 * @param defFCRMIN
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
	public FEPReturnCode prepareFCRMIN(String wkPEND, RefBase<Fcrmin> defFCRMIN) {
		Fcrmnoctl defFCRMNOCTL = new Fcrmnoctl();
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		try {
			defFCRMIN.get().setFcrminTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defFCRMIN.get().setFcrminBrno("900"); // 匯入之分行別固定為虛擬分行900

			// 取FEP登錄序號, 若是被LOCK retry 直到ok '
			defFCRMNOCTL.setFcrmnoctlBrno(defFCRMIN.get().getFcrminBrno());
			defFCRMNOCTL.setFcrmnoctlCategory("02");
			defFCRMIN.get().setFcrminFepno(StringUtils.leftPad(spCaller.getFCRMNO(defFCRMNOCTL).toString(), 7, '0'));
			defFCRMIN.get().setFcrminFepsubno("00");
			defFCRMIN.get().setFcrminTxamt(new BigDecimal(fiscFCRMReq.getTxAmt()));
			if ("1161".equals(fiscFCRMReq.getProcessingCode())) {
				if ("0".equals(wkPEND)) {
					defFCRMIN.get().setFcrminCategory("12"); // 匯入類(滯留磁片交易)
				} else {
					defFCRMIN.get().setFcrminCategory("10"); // 匯入類
				}
			}
			if ("117".equals(fiscFCRMReq.getFcSubpcode())) {
				defFCRMIN.get().setFcrminCategory("11"); // 退匯類
			}
			defFCRMIN.get().setFcrminSenderBank(fiscFCRMReq.getSenderBank());
			defFCRMIN.get().setFcrminReceiverBank(fiscFCRMReq.getReceiverBank());
			defFCRMIN.get().setFcrminStan(fiscFCRMReq.getSystemTraceAuditNo());
			defFCRMIN.get().setFcrminRmsno(fiscFCRMReq.getBankNo());
			if ("1161".equals(fiscFCRMReq.getProcessingCode())) {
				if ("1".equals(wkPEND)) {
					if ("5017".equals(fiscFCRMReq.getResponseCode())) {
						defFCRMIN.get().setFcrminStat("25"); // 匯入STORE' (滯留交易, 人工記帳)
					} else {
						defFCRMIN.get().setFcrminStat("24"); // 匯入STORE(滯留交易,需送CBS記帳)
					}
				} else {
					defFCRMIN.get().setFcrminStat("21"); // 匯入STORE
				}
			}
			if ("1172".equals(fiscFCRMReq.getProcessingCode())) {
				defFCRMIN.get().setFcrminStat("10"); // 退匯匯入
			}
			defFCRMIN.get().setFcrminSenddate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defFCRMIN.get().setFcrminSendtime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			if ("117".equals(fiscFCRMReq.getFcSubpcode())) {
				defFCRMIN.get().setFcrminOrgrmsno(fiscFCRMReq.getOrgBankNo());
				defFCRMIN.get().setFcrminOrgdate(fiscFCRMReq.getTxDate());
				defFCRMIN.get().setFcrminOrgregNo(fiscFCRMReq.getOrgBankNo());
				defFCRMIN.get().setFcrminBackReason(fiscFCRMReq.getSTATUS());
			}
			defFCRMIN.get().setFcrminFiscRtnCode(fiscFCRMReq.getResponseCode());
			defFCRMIN.get().setFcrminOutName(fiscFCRMReq.getOutName().substring(0, 70).trim());
			defFCRMIN.get().setFcrminAddress(fiscFCRMReq.getOutName().substring(70, 140).trim());
			defFCRMIN.get().setFcrminInName(fiscFCRMReq.getInName());
			defFCRMIN.get().setFcrminMemo(fiscFCRMReq.getChineseMemo());
			defFCRMIN.get().setFcrminInAccIdNo(fiscFCRMReq.getInActno());
			defFCRMIN.get().setFcrminFiscSndCode(fiscFCRMReq.getProcessingCode());
			defFCRMIN.get().setFcrminPending("P");
			defFCRMIN.get().setFcrminEjno(fiscFCRMReq.getEj());
			defFCRMIN.get().setFcrminIbsno(fiscFCRMReq.getSenderBankSeqno());
			defFCRMIN.get().setFcrminCurrency(fiscFCRMReq.getCURRENCY());
			defFCRMIN.get().setFcrminFcSubpcode(fiscFCRMReq.getFcSubpcode());
			defFCRMIN.get().setFcrminNationalCode(fiscFCRMReq.getFcMemo().substring(0, 2));
			defFCRMIN.get().setFcrminKind(fiscFCRMReq.getFcMemo().substring(2, 5));
			defFCRMIN.get().setFcrminSubkind(fiscFCRMReq.getFcMemo().substring(5, 6));
			defFCRMIN.get().setFcrminIdStatus(fiscFCRMReq.getFcMemo().substring(6, 7));
			defFCRMIN.get().setFcrminOutAccidNo(fiscFCRMReq.getFcMemo().substring(7, 23));

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 負責準備FEP交易明細檔內容此程式為1611之副程式
	 * 
	 * @param defMSGCTRL
	 * @param wkREPK
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
	public FEPReturnCode prepareFEPTXNByFCRM(Msgctl defMSGCTRL, String wkREPK) {
		FEPReturnCode RC = CommonReturnCode.Normal;

		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);

		String wkISO3_CUR = "";

		try {
			getFeptxn().setFeptxnTxDate(CalendarUtil.rocStringToADString("0" + fiscFCRMReq.getTxnInitiateDateAndTime().substring(0, 6)));
			getFeptxn().setFeptxnTxTime(fiscFCRMReq.getTxnInitiateDateAndTime().substring(6, 12));
			getFeptxn().setFeptxnEjfno(fiscFCRMReq.getEj()); // 電子日誌序號* /
			getFeptxn().setFeptxnStan(StringUtils.leftPad(fiscFCRMReq.getSystemTraceAuditNo(), 7, '0')); // 財金交易序號*/
			getFeptxn().setFeptxnBkno(fiscFCRMReq.getTxnSourceInstituteId().substring(0, 3));

			defBSDAYS.setBsdaysZoneCode("TWN");
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.BSDAYSNotFound.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			}

			getFeptxn().setFeptxnSubsys(defMSGCTRL.getMsgctlSubsys()); // 系統別
			getFeptxn().setFeptxnChannel(defMSGCTRL.getMsgctlChannel()); // 通道別
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行)跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
			getFeptxn().setFeptxnPcode(fiscFCRMReq.getProcessingCode());
			getFeptxn().setFeptxnDesBkno(fiscFCRMReq.getTxnDestinationInstituteId().substring(0, 3));
			getFeptxn().setFeptxnReqRc(fiscFCRMReq.getResponseCode());
			// FepTxn.FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(getFeptxn().getFeptxnTxDate() + fiscFCRMReq.getTxnInitiateDateAndTime().substring(6, 12));

			if (StringUtils.isNotBlank(fiscFCRMReq.getInActno())) {
				getFeptxn().setFeptxnTrinActno("00" + fiscFCRMReq.getInActno());
			}

			if ("1112,1122,1132,1172,1182,1192,1611".indexOf(fiscRMReq.getProcessingCode()) >= 0) {
				getFeptxn().setFeptxnTxAmt(new BigDecimal(fiscFCRMReq.getTxAmt())); // 交易金額
			}
			FEPReturnCode rtn = CommonReturnCode.Normal;
			RefString refwkISO3_CUR = new RefString("");
			switch (fiscFCRMReq.getProcessingCode()) {
				case "1611":
					getFeptxn().setFeptxnClrType((short) 1); // 1-跨行清算 '
					getFeptxn().setFeptxnRmsno(fiscFCRMReq.getBankNo()); // 通匯序號 '
					getFeptxn().setFeptxnSenderBank(fiscFCRMReq.getSenderBank()); // 匯款行'
					getFeptxn().setFeptxnReceiverBank(fiscFCRMReq.getReceiverBank()); // 解款行'

					refwkISO3_CUR.set(wkISO3_CUR);
					rtn = mappingIOS3CUR(fiscFCRMReq.getCURRENCY(), refwkISO3_CUR);
					wkISO3_CUR = refwkISO3_CUR.get();
					if (rtn != CommonReturnCode.Normal) {
						getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
						getFeptxn().setFeptxnTxCur(wkISO3_CUR);
					} else {
						getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
						getFeptxn().setFeptxnTxCur(wkISO3_CUR);
					}
					break;
				case "1641":
					if (wkREPK.equals("1") || wkREPK.equals("3")) {// 以1412電文重組1411故匯款行解款行要對調
						getFeptxn().setFeptxnSenderBank(fiscFCRMReq.getReceiverBank()); // 匯款行*/
						getFeptxn().setFeptxnReceiverBank(fiscFCRMReq.getSenderBank()); // 解款行*/
					} else {
						getFeptxn().setFeptxnSenderBank(fiscFCRMReq.getSenderBank()); // 匯款行*/
						getFeptxn().setFeptxnReceiverBank(fiscFCRMReq.getReceiverBank()); // 解款行*/
					}
					refwkISO3_CUR.set(wkISO3_CUR);
					rtn = mappingIOS3CUR(fiscFCRMReq.getCURRENCY(), refwkISO3_CUR);
					wkISO3_CUR = refwkISO3_CUR.get();
					if (rtn != CommonReturnCode.Normal) {
						getFeptxn().setFeptxnTxCurAct("");
						getFeptxn().setFeptxnTxCur("");
					} else {
						getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
						getFeptxn().setFeptxnTxCur(wkISO3_CUR);
					}
					break;
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return RC;
	}

	/**
	 * T24 CUR->FEP CUR
	 * 
	 * @param sCurrency
	 * @param wkISO3_CUR
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
	private FEPReturnCode mappingIOS3CUR(String sCurrency, RefString wkISO3_CUR) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		CurcdExtMapper dbCURCD = SpringBeanFactoryUtil.getBean(CurcdExtMapper.class);
		Curcd defCURCD = new Curcd();

		try {

			defCURCD.setCurcdFiscCur(sCurrency);
			defCURCD = dbCURCD.selectByPrimaryKey(defCURCD.getCurcdAlpha3());
			if (defCURCD != null) {
				wkISO3_CUR.set(defCURCD.getCurcdAlpha3());
			} else {
				rtnCode = FEPReturnCode.CURCDNotFound;
				return rtnCode;
			}

		} catch (Exception ex) {
			if (MessageFlow.Request.equals(getFISCTxData().getMessageFlowType())) {
				getLogContext().setMessageFlowType(MessageFlow.Request);
			} else {
				getLogContext().setMessageFlowType(MessageFlow.Response);
			}
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());

			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	/**
	 * 負責準備FEP交易明細檔內容此程式為 AA1611Out之副程式
	 * 
	 * @param defFCRMOUT
	 * @param iEJ
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
	public FEPReturnCode prepareFEPTXNBy1611(Fcrmout defFCRMOUT, int iEJ) {
		FEPReturnCode RC = CommonReturnCode.Normal;

		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);

		String wkISO3_CUR = "";

		try {
			getFeptxn().setFeptxnTxDate(defFCRMOUT.getFcrmoutTxdate());
			getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			getFeptxn().setFeptxnEjfno(iEJ); // 電子日誌序號* /
			getFeptxn().setFeptxnStan(StringUtils.leftPad(getStan(), 7, '0')); // 財金交易序號*/
			getFeptxn().setFeptxnBkno(defFCRMOUT.getFcrmoutSenderBank().substring(0, 3));

			defBSDAYS.setBsdaysZoneCode("TWN");
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.BSDAYSNotFound.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			}

			getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue()); // 系統別
			getFeptxn().setFeptxnChannel(FEPChannel.FISC.toString()); // 通道別
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行)跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
			getFeptxn().setFeptxnPcode("1611");
			getFeptxn().setFeptxnDesBkno("950");
			getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
			// FepTxn.FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
			getFeptxn().setFeptxnClrType((short) 1); // 1-跨行清算
			RefString refwkISO3_CUR = new RefString(wkISO3_CUR);
			if (mappingIOS3CUR(defFCRMOUT.getFcrmoutCurrency(), refwkISO3_CUR) != CommonReturnCode.Normal) {
				getFeptxn().setFeptxnTxCurAct("");
				getFeptxn().setFeptxnTxCur("");
			} else {
				wkISO3_CUR = refwkISO3_CUR.get();
				getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
				getFeptxn().setFeptxnTxCur(wkISO3_CUR);
			}
			getFeptxn().setFeptxnTrinActno(defFCRMOUT.getFcrmoutInAccIdNo());
			if (defFCRMOUT.getFcrmoutOutAccidNo().length() > 10) {
				getFeptxn().setFeptxnTroutActno(defFCRMOUT.getFcrmoutOutAccidNo());
			}
			getFeptxn().setFeptxnFiscCurMemo(defFCRMOUT.getFcrmoutCurrency());
			getFeptxn().setFeptxnTxFeeDr(BigDecimal.valueOf((defFCRMOUT.getFcrmoutActfee().doubleValue() - defFCRMOUT.getFcrmoutPostamt().doubleValue()))); // 應收手續費*/
			if (getFeptxn().getFeptxnTxFeeDr().doubleValue() < 0) {
				getFeptxn().setFeptxnTxFeeDr(BigDecimal.valueOf(0));
				getFeptxn().setFeptxnTxFeeCr(BigDecimal.valueOf(defFCRMOUT.getFcrmoutPostamt().doubleValue() - defFCRMOUT.getFcrmoutActfee().doubleValue())); // 應付手續費*/
			}
			if (defFCRMOUT.getFcrmoutActfee().doubleValue() > 0) {
				getFeptxn().setFeptxnFeeCustpay(defFCRMOUT.getFcrmoutActfee()); // 扣客戶手續費 */
			}
			getFeptxn().setFeptxnRmsno(defFCRMOUT.getFcrmoutRmsno()); // 通匯序號 */
			if ("17".equals(defFCRMOUT.getFcrmoutRemtype())) {// 退匯*/
				getFeptxn().setFeptxnOrgrmsno(defFCRMOUT.getFcrmoutOrgrmsno()); // 原通匯序號 */
				getFeptxn().setFeptxnOrgdate(defFCRMOUT.getFcrmoutOrgdate()); // 原匯款日期*/
			}
			getFeptxn().setFeptxnSenderBank(defFCRMOUT.getFcrmoutSenderBank()); // 匯款行*/
			getFeptxn().setFeptxnReceiverBank(defFCRMOUT.getFcrmoutReceiverBank()); // 解款行*/

			if (defFCRMOUT.getFcrmoutTxamt() != null) {
				getFeptxn().setFeptxnTxAmt(defFCRMOUT.getFcrmoutTxamt()); // 扣客戶手續費
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return RC;
	}

	/**
	 * 負責準備FEP交易明細檔內容此程式為 Service1641之副程式
	 * 
	 * @param defFCMSGOUT
	 * @param iEJ
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
	public FEPReturnCode prepareFEPTXNBy1641(Fcmsgout defFCMSGOUT, int iEJ) {
		FEPReturnCode RC = CommonReturnCode.Normal;

		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
		@SuppressWarnings("unused")
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		String wkISO3_CUR = "";

		try {
			getFeptxn().setFeptxnTxDate(defFCMSGOUT.getFcmsgoutTxdate());
			getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			getFeptxn().setFeptxnEjfno(iEJ); // 電子日誌序號* /
			getFeptxn().setFeptxnStan(StringUtils.leftPad(getStan(), 7, '0')); // 財金交易序號*/
			getFeptxn().setFeptxnBkno(defFCMSGOUT.getFcmsgoutSenderBank().substring(0, 3));

			defBSDAYS.setBsdaysZoneCode("TWN");
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				TxHelper.getRCFromErrorCode(String.valueOf(FEPReturnCode.BSDAYSNotFound.getValue()), FEPChannel.FEP, getFISCTxData().getTxChannel(), getFISCTxData().getLogContext());
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			}

			getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue()); // 系統別
			getFeptxn().setFeptxnChannel(FEPChannel.FISC.toString()); // 通道別
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行)跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Request); // (FISC REQUEST)
			getFeptxn().setFeptxnPcode("1641");
			getFeptxn().setFeptxnDesBkno("950");
			getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
			// FepTxn.FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
			getFeptxn().setFeptxnClrType((short) 1); // 1-跨行清算
			RefString refwkISO3_CUR = new RefString(wkISO3_CUR);
			if (mappingIOS3CUR(defFCMSGOUT.getFcmsgoutCurrency(), refwkISO3_CUR) != CommonReturnCode.Normal) {
				getFeptxn().setFeptxnTxCurAct("");
				getFeptxn().setFeptxnTxCur("");
			} else {
				wkISO3_CUR = refwkISO3_CUR.get();
				getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
				getFeptxn().setFeptxnTxCur(wkISO3_CUR);
			}
			getFeptxn().setFeptxnSenderBank(defFCMSGOUT.getFcmsgoutSenderBank()); // 匯款行*/
			getFeptxn().setFeptxnReceiverBank(defFCMSGOUT.getFcmsgoutReceiverBank()); // 解款行*/
			getFeptxn().setFeptxnFiscCurMemo(defFCMSGOUT.getFcmsgoutCurrency());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return RC;
	}

	/**
	 * 負責準備FCRMOUT匯出主檔內容此程式為 AA\RM\1611之副程式
	 * 
	 * @param wkFEPNO
	 * @param wkBackReason
	 * @param defFCRMOUT
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
	public FEPReturnCode prepareFCRMOUT(String wkFEPNO, String wkBackReason, RefBase<Fcrmout> defFCRMOUT) {
		Fcrmnoctl defFCRMNOCTL = new Fcrmnoctl();

		Fcrmoutsno defFCRMOUTSNO = new Fcrmoutsno();
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		try {
			defFCRMOUT.get().setFcrmoutTxdate(getFeptxn().getFeptxnTxDate());
			defFCRMOUT.get().setFcrmoutBrno("900"); // 匯入之分行別固定為虛擬分行900 */
			// 取FEP登錄序號, 若是被LOCK retry 直到ok '
			defFCRMNOCTL.setFcrmnoctlBrno(defFCRMOUT.get().getFcrmoutBrno());
			defFCRMNOCTL.setFcrmnoctlCategory("01");
			defFCRMOUT.get().setFcrmoutFepno(StringUtils.leftPad(spCaller.getFCRMNO(defFCRMNOCTL).toString(), 7, '0'));
			defFCRMOUT.get().setFcrmoutFepsubno("00");
			defFCRMOUT.get().setFcrmoutRemtype("17");
			defFCRMOUT.get().setFcrmoutTxamt(new BigDecimal(fiscFCRMReq.getTxAmt()));

			defFCRMOUT.get().setFcrmoutAmtType("002"); // 轉帳
			defFCRMOUT.get().setFcrmoutServamtType("002"); // 轉帳 */
			defFCRMOUT.get().setFcrmoutCategory("06"); // 06=自動退匯匯出類 */
			defFCRMOUT.get().setFcrmoutSenderBank(fiscFCRMReq.getReceiverBank());
			defFCRMOUT.get().setFcrmoutReceiverBank(fiscFCRMReq.getSenderBank());

			defFCRMOUTSNO.setFcrmoutsnoReceiverBank(defFCRMOUT.get().getFcrmoutReceiverBank());
			defFCRMOUTSNO.setFcrmoutsnoSenderBank(defFCRMOUT.get().getFcrmoutSenderBank());
			defFCRMOUT.get().setFcrmoutRmsno(StringUtils.leftPad(spCaller.getFCRMOUTSNO(defFCRMOUTSNO).toString(), 7, '0'));
			defFCRMOUT.get().setFcrmoutStan(getStan());
			defFCRMOUT.get().setFcrmoutStat("06"); // 06'=匯出自退(匯入自動退匯) */
			defFCRMOUT.get().setFcrmoutApdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defFCRMOUT.get().setFcrmoutAptime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			defFCRMOUT.get().setFcrmoutSenddate(defFCRMOUT.get().getFcrmoutApdate());
			defFCRMOUT.get().setFcrmoutSendtime(defFCRMOUT.get().getFcrmoutAptime());
			defFCRMOUT.get().setFcrmoutOrgrmsno(fiscFCRMReq.getOrgBankNo());
			defFCRMOUT.get().setFcrmoutOrgdate(fiscFCRMReq.getTxDate());
			defFCRMOUT.get().setFcrmoutOrgregFepno(wkFEPNO);
			defFCRMOUT.get().setFcrmoutBackReason(wkBackReason);
			defFCRMOUT.get().setFcrmoutOutName(fiscFCRMReq.getOutName());
			defFCRMOUT.get().setFcrmoutInName(fiscFCRMReq.getInName());
			defFCRMOUT.get().setFcrmoutMemo(fiscFCRMReq.getChineseMemo());
			defFCRMOUT.get().setFcrmoutInAccIdNo(fiscFCRMReq.getInActno());
			defFCRMOUT.get().setFcrmoutFiscSndCode(fiscFCRMReq.getProcessingCode());
			defFCRMOUT.get().setFcrmoutPending("P");
			defFCRMOUT.get().setFcrmoutIbsno(fiscFCRMReq.getSenderBankSeqno());
			defFCRMOUT.get().setFcrmoutCurrency(fiscFCRMReq.getCURRENCY());
			defFCRMOUT.get().setFcrmoutFcSubpcode(fiscFCRMReq.getFcSubpcode());
			defFCRMOUT.get().setFcrmoutNationalCode(fiscFCRMReq.getFcMemo().substring(0, 2));
			defFCRMOUT.get().setFcrmoutKind(fiscFCRMReq.getFcMemo().substring(2, 5));
			defFCRMOUT.get().setFcrmoutSubkind(fiscFCRMReq.getFcMemo().substring(5, 6));
			defFCRMOUT.get().setFcrmoutIdStatus(fiscFCRMReq.getFcMemo().substring(6, 7));
			defFCRMOUT.get().setFcrmoutOutAccidNo(fiscFCRMReq.getFcMemo().substring(7, 23));

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 負責準備FCRMOUT匯出主檔內容此程式為 AA\RM\1611之副程式
	 * 
	 * @param T24TITAData
	 * @param I_EJFNO
	 * @param wkFEPNO
	 * @param defFCRMOUT
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
	public FEPReturnCode prepareFCRMOUTByT24(HashMap<String, String> T24TITAData, int I_EJFNO, String wkFEPNO, RefBase<Fcrmout> defFCRMOUT) {
		Fcrmnoctl defFCRMNOCTL = new Fcrmnoctl();

		Fcrmoutsno defFCRMOUTSNO = new Fcrmoutsno();
		SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
		try {
			defFCRMOUT.get().setFcrmoutTxdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defFCRMOUT.get().setFcrmoutBrno(T24TITAData.get("SEDNER_BANK").substring(2, 5));
			// 取FEP登錄序號, 若是被LOCK retry 直到ok '
			defFCRMNOCTL.setFcrmnoctlBrno(defFCRMOUT.get().getFcrmoutBrno());
			defFCRMNOCTL.setFcrmnoctlCategory("01");
			defFCRMOUT.get().setFcrmoutFepno(StringUtils.leftPad(spCaller.getFCRMNO(defFCRMNOCTL).toString(), 7, '0'));
			defFCRMOUT.get().setFcrmoutFepsubno("00");
			defFCRMOUT.get().setFcrmoutRemtype(T24TITAData.get("REMTYPE"));
			defFCRMOUT.get().setFcrmoutTxamt(new BigDecimal(T24TITAData.get("TX_AMT")));

			defFCRMOUT.get().setFcrmoutAmtType(T24TITAData.get("AMT_TYPE"));
			defFCRMOUT.get().setFcrmoutServamtType(T24TITAData.get("SERVAMT_TYPE"));
			defFCRMOUT.get().setFcrmoutCategory("01"); // 01=當日匯款匯出類
			defFCRMOUT.get().setFcrmoutSenderBank(T24TITAData.get("RECEIVER_BANK"));
			defFCRMOUT.get().setFcrmoutReceiverBank(T24TITAData.get("SENDER_BANK"));

			defFCRMOUTSNO.setFcrmoutsnoReceiverBank(defFCRMOUT.get().getFcrmoutReceiverBank());
			defFCRMOUTSNO.setFcrmoutsnoSenderBank(defFCRMOUT.get().getFcrmoutSenderBank());
			defFCRMOUT.get().setFcrmoutRmsno(StringUtils.leftPad(spCaller.getFCRMOUTSNO(defFCRMOUTSNO).toString(), 7, '0'));
			defFCRMOUT.get().setFcrmoutStan(getStan());
			defFCRMOUT.get().setFcrmoutStat("02"); // 02=放行 */
			defFCRMOUT.get().setFcrmoutApdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defFCRMOUT.get().setFcrmoutAptime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			defFCRMOUT.get().setFcrmoutSenddate(defFCRMOUT.get().getFcrmoutApdate());
			defFCRMOUT.get().setFcrmoutSendtime(defFCRMOUT.get().getFcrmoutAptime());
			defFCRMOUT.get().setFcrmoutOrgrmsno(T24TITAData.get("ORGBANK_NO"));
			defFCRMOUT.get().setFcrmoutOrgdate(T24TITAData.get("ORGDATE"));
			if ("117".equals(T24TITAData.get("SUBPCODE "))) {// 匯入款退匯
				defFCRMOUT.get().setFcrmoutOrgregFepno(wkFEPNO);
				defFCRMOUT.get().setFcrmoutBackReason(T24TITAData.get("REASON"));
			}
			defFCRMOUT.get().setFcrmoutOutName(T24TITAData.get("OUT_NAME"));
			defFCRMOUT.get().setFcrmoutInName(T24TITAData.get("IN_NAME"));
			defFCRMOUT.get().setFcrmoutMemo(T24TITAData.get("CHINESE_MEMO"));
			defFCRMOUT.get().setFcrmoutInAccIdNo(T24TITAData.get("IN_ACTNO"));
			defFCRMOUT.get().setFcrmoutFiscSndCode("1611");
			defFCRMOUT.get().setFcrmoutPending("P");
			defFCRMOUT.get().setFcrmoutEjno(I_EJFNO);
			defFCRMOUT.get().setFcrmoutIbsno(T24TITAData.get("SENDER_BANK_SEQNO"));
			defFCRMOUT.get().setFcrmoutCurrency(T24TITAData.get("CURRENCY"));
			defFCRMOUT.get().setFcrmoutFcSubpcode(T24TITAData.get("SUBPCODE"));
			defFCRMOUT.get().setFcrmoutNationalCode(T24TITAData.get("NATIONAL_CODE"));
			defFCRMOUT.get().setFcrmoutKind(T24TITAData.get("KIND"));
			defFCRMOUT.get().setFcrmoutSubkind(T24TITAData.get("SUBKIND"));
			defFCRMOUT.get().setFcrmoutIdStatus(T24TITAData.get("ID_STATUS"));
			defFCRMOUT.get().setFcrmoutOutAccidNo(T24TITAData.get("OUT_ACCID_NO"));
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
