package com.syscom.fep.server.common.business.host;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.CreditAdapter;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.enums.*;
import com.syscom.fep.vo.text.credit.CreditGeneral;
import com.syscom.fep.vo.text.credit.CreditGeneralResponse;
import com.syscom.fep.vo.text.credit.CreditTextBase;
import com.syscom.fep.vo.text.credit.request.*;
import com.syscom.fep.vo.text.credit.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.math.BigDecimal;

public class Credit extends HostBase {
	private CreditGeneralResponse ascRes;
	private MessageBase _txData;
	private static final String OpenCardTimeout = "Y002";
	private static final String ProgramName = Credit.class.getSimpleName();

	public Credit(MessageBase txData) {
		super(txData);
		_txData = txData;
	}

	/**
	 * ascRes屬性
	 * For AA 組response時使用
	 * 
	 * <value></value>
	 * 
	 * @return Credit.Response物件
	 * 
	 */
	public CreditGeneralResponse getCreditResponse() {
		return ascRes;
	}

	/**
	 * 送電文至信用卡主機
	 * 0.作PinBlockConvert和MAC壓碼動作
	 * 1.依據不同送主機電文編號，組送主機電文欄位
	 * 2.以SCAdapter作為介面和信用卡主機溝通
	 * 3.若主機回應成功，更新部分FEPTXN欄位
	 * 4.若Timeout，回傳錯誤FEPReturnCode
	 * 
	 * @param ascTxid 送信用卡電文代號
	 * @param txType 沖正記號: type=1:入扣帳，type=3:沖正
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2009/12/23</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode sendToCredit(String ascTxid, byte txType) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String tita = "";
		CreditAdapter adapter = new CreditAdapter(getGeneralData());
		CreditTXCD txcd = CreditTXCD.parse(ascTxid);
		ENCHelper encHelper = new ENCHelper(getFeptxn(), _txData);
		CreditGeneral ascGeneralForReq = new CreditGeneral();
		CreditGeneral ascGeneralForRes = new CreditGeneral();
		RefBase<CreditTextBase> reqClass = new RefBase<CreditTextBase>(null);
		RefBase<CreditTextBase> resClass = new RefBase<CreditTextBase>(null);

		try {
			// 2013/02/05 Modify by Ruling for 檢核永豐信用卡通道(SYSSTAT_ASC_CHANNEL)
			// 檢核永豐信用卡通道
			FEPCache.reloadCache(CacheItem.SYSSTAT);
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAscChannel())) {
//				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
//					// 跨行交易
//					if (StringUtils.isBlank(getFeptxn().getFeptxnTxCode())) {
//						// 原存行交易
//						getLogContext().setRemark("SendToCredit-暫停永豐信用卡通道(SYSSTAT_ASC_CHANNEL)服務");
//						this.logMessage(getLogContext());
//						return CommonReturnCode.InterBankServiceStop; // 原存行交易暫停服務(0205)
//					} else {
//						// 代理行交易
//						getLogContext().setRemark("SendToCredit-暫停永豐信用卡通道(SYSSTAT_ASC_CHANNEL)服務");
//						this.logMessage(getLogContext());
//						return FISCReturnCode.SenderBankServiceStop; // 發信單位該項跨行業務停止或暫停營業(0202)
//					}
//				} else {
//					// 自行交易
//					getLogContext().setRemark("SendToCredit-暫停永豐信用卡通道(SYSSTAT_ASC_CHANNEL)服務");
//					this.logMessage(getLogContext());
//					return CommonReturnCode.WithdrawServiceStop; // 提款暫停服務(E948)
//				}
//			}

			// 準備送信用卡主機電文
			rtnCode = prepareCreditRequest(ascTxid, txType, ascGeneralForReq, reqClass, resClass, encHelper);
			if (rtnCode != FEPReturnCode.Normal) {
				return rtnCode;
			}

			tita = reqClass.get().makeMessageFromGeneral(ascGeneralForReq);

			// 將組好的信用卡電文送往信用卡主機
			rtnCode = sendToCreditByAdapter(txType, adapter, tita);

			// 處理信用卡主機回應電文
			// 2010-04-27 modified by kyo for 移除多餘的rtncode的判斷，造成沒取得主機回應皆回成2999
			rtnCode = processCreditResponse(txcd, txType, adapter, rtnCode, ascGeneralForRes, resClass.get(), encHelper);

			return rtnCode;
		} catch (Exception e) {
			getLogContext().setProgramException(e);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 利用Adapter送信用卡主機，並做FEPTXN的更新
	 * 
	 * @return FEPReturnCode
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2010/2/25</date>
	 *         </modify>
	 *         </history>
	 */
	private FEPReturnCode sendToCreditByAdapter(byte txType, CreditAdapter adapter, String tita) throws Exception {

		FEPReturnCode rtn = CommonReturnCode.Normal;
		// 將組好的信用卡電文送往信用卡主機
		adapter.setChannel(getGeneralData().getTxChannel());
		adapter.setTimeout(CMNConfig.getInstance().getUnisysTimeout());
		adapter.setFEPSubSystem(getGeneralData().getTxSubSystem());
		adapter.setMessageToSC(tita);
		adapter.setEj(getEj());

		if (txType == (byte) 1) {
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ASC_TO_Resquest);
		} else if (txType == (byte) 3) {
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.ASC_CREC_TO_Resquest);
		}
		if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {// RC = L013
			return IOReturnCode.FEPTXNUpdateError;
		}

		rtn = adapter.sendReceive();
		return rtn;
	}

	/**
	 * 處理信用卡主機回應的電文
	 * 
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2010/2/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>bugReport(001B0238):進行以舊ic卡開啟新combo卡，發生「Syscom.FEP10.AA.ATMP.CashAdvance.Main並未將物件參考設定為物件的執行個體」</reason>
	 *         <date>2010/4/14</date>
	 *         </modify>
	 *         </history>
	 */
	private FEPReturnCode processCreditResponse(CreditTXCD txcd, byte txType, CreditAdapter adapter, FEPReturnCode rtnCode, CreditGeneral ascGeneralForRes, CreditTextBase resClass,
			ATMENCHelper encHelper)
			throws Exception {
		String tota = "";

		if (rtnCode == CommonReturnCode.Normal) {
			tota = adapter.getMessageFromSC();
			if (StringUtils.isNotBlank(tota)) {

				ascGeneralForRes = resClass.parseFlatfile(tota);

				if (txType == (byte) 1) {
					// 2024-11-05 Richard modified for 【Cleartext Submission of Sensitive Information】
					// ascGeneralForRes.getResponse().setCD3(String.valueOf(MessageFlow.Response.getValue()));
					ReflectUtil.setFieldValue(ascGeneralForRes.getResponse(), "_CD3", String.valueOf(MessageFlow.Response.getValue()));
				} else if (txType == (byte) 3) {
					// 2024-11-05 Richard modified for 【Cleartext Submission of Sensitive Information】
					// ascGeneralForRes.getResponse().setCD3(String.valueOf(MessageFlow.ResponseConfirmation.getValue()));
					ReflectUtil.setFieldValue(ascGeneralForRes.getResponse(), "_CD3", String.valueOf(MessageFlow.ResponseConfirmation.getValue()));
				}
				getFeptxn().setFeptxnAscTimeout(DbHelper.toShort(false));
				getFeptxn().setFeptxnAscRc(ascGeneralForRes.getResponse().getRC()); // 信用卡主機回應代號

				if (!CheckEXPCD(ascGeneralForRes.getResponse().getRC()) && ascGeneralForRes.getResponse().getRC().equals(rspCorrectStr)) {
					// 2012/07/19 將ELSE拿掉否則除了BO1外的信用卡電文回錯誤時都會轉成H003回給ATM
					// 2012/07/16 Modify by Ruling for B01電文信用卡回的不是正常的訊息或TimeOut，應回覆ATM 「Y002」
					// 2014/09/17 Modify by Ruling for COMBO優化：B01 Rep.RC=0028 視為信用卡開卡成功
					if (txcd == CreditTXCD.B01) {
						// COMBO卡開卡
						if (StringUtils.isNotBlank(ascGeneralForRes.getResponse().getRC()) &&
								!"0028".equals(StringUtils.leftPad(ascGeneralForRes.getResponse().getRC().trim(), 4, '0'))) {
							getFeptxn().setFeptxnReplyCode(OpenCardTimeout);
						}
						return CommonReturnCode.Normal;
					}

					// 2010-04-12 modified by kyo for destinationChannel 需要用adata.txchannel
					// 2010-04-19 modified by Jim for RC處理修改
					// 2010-09-27 by kyo for spec modify:/*9/21 Update By Connie*/
					if (txType == (byte) CreditTxType.Accounting.getValue() && txcd != CreditTXCD.B20) {
						// bugReport(001B0238):不應該直接return，這樣反而造成只要主機回應錯誤，ASCresponse就取不到值
						rtnCode = CommonReturnCode.CBSResponseError; // ASC_TOTA.RC
						if (getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
							/// *ATM發動交易需將 ASC RC 轉換為 ATM RC*/
//							getFeptxn().setFeptxnReplyCode(
//									TxHelper.getRCFromErrorCode(ascGeneralForRes.getResponse().getRC(), FEPChannel.SINOCARD, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // FEPReturnCode
																																																	// =
																																																	// ASC_TOTA.RC
						} else {
							/// *FISC發動交易需將 ASC RC 轉換為 FISC RC*/
							// modified By Maxine on 2011/11/17 for txhelper針對EBILL有做處理,不需要單獨處理了
							// 'modified by maxine on 2011/09/21 for Channel=EBILL時也應轉換成FISC RC
//							getFeptxn().setFeptxnRepRc(
//									TxHelper.getRCFromErrorCode(ascGeneralForRes.getResponse().getRC(), FEPChannel.SINOCARD, getGeneralData().getTxChannel(), getGeneralData().getLogContext())); // FEPReturnCode
																																																	// =
																																																	// ASC_TOTA.RC
						}
						// Else
						// Feptxn.FEPTXN_CON_REPLY_CODE = TxHelper.getRCFromErrorCode(ascGeneralForRes.Response.RC, FEPChannel.SPC, aData.TxChannel) 'FEPReturnCode = ASC_TOTA.RC
					}
					// 2010-06-15 by kyo for 移除重複的CODE
				} else {
					// 2019/11/14 Modify by Ruling for 線上比對主機回應電文帳號及金額資料：增加信用卡主機比對
					if ((txcd == CreditTXCD.B07 || txcd == CreditTXCD.B10 || txcd == CreditTXCD.B11 || txcd == CreditTXCD.B20) && txType == (byte) 1) {
						compareCreditTOTA(txcd, ascGeneralForRes);
					}

					/// * 9/3 修正 for Phase II */
					if (DbHelper.toBoolean(getGeneralData().getMsgCtl().getMsgctlCbsFlag())) {
						if (((ATMTXCD.CAV.name() + "").equals(getFeptxn().getFeptxnTxCode())) || ((ATMTXCD.CAM.name() + "").equals(getFeptxn().getFeptxnTxCode()))
								|| ((ATMTXCD.CAJ.name() + "").equals(getFeptxn().getFeptxnTxCode())) || ((ATMTXCD.CAA.name() + "").equals(getFeptxn().getFeptxnTxCode()))
								|| ((ATMTXCD.IWD.name() + "").equals(getFeptxn().getFeptxnTxCode())) || ((ATMTXCD.ACW.name() + "").equals(getFeptxn().getFeptxnTxCode()))) {
							if (txType == (byte) 1) {
								getFeptxn().setFeptxnAccType((short) AccountingType.Accounting.getValue());
							} else if (txType == (byte) 3) {
								getFeptxn().setFeptxnAccType((short) AccountingType.EC.getValue());
							}
						} else if (((ATMTXCD.IDR.name() + "").equals(getFeptxn().getFeptxnTxCode())) || ((ATMTXCD.CDR.name() + "").equals(getFeptxn().getFeptxnTxCode()))) {
							if (txType == (byte) 3) {
								getFeptxn().setFeptxnAccType((short) AccountingType.Accounting.getValue());
							}

						}

					}
				}

				getFeptxn().setFeptxnAuthcd(ascGeneralForRes.getResponse().getAUTHCD());
				/// * 2/11 修改 */
				/// * GIFT 卡加值(B20), 不需將信用卡傳回之額度寫入 FEPTXN */
				if (txcd != CreditTXCD.B20) {
					//getFeptxn().setFeptxnBala(new BigDecimal(ascGeneralForRes.getResponse().getAvbalS() + ascGeneralForRes.getResponse().getAVBAL().toString()));
					getFeptxn().setFeptxnBalb(new BigDecimal(ascGeneralForRes.getResponse().getActbalS() + ascGeneralForRes.getResponse().getACTBAL().toString()));
				}
				ascRes = ascGeneralForRes.getResponse(); // 塞給ATMBusiness的Global變數，因為在組ATM回應電文的時候需要用到

			}
		} else {// timeout
			if (txcd == CreditTXCD.B01) {
				getFeptxn().setFeptxnReplyCode(OpenCardTimeout);
				return CommonReturnCode.Normal;
			} else {
				return CommonReturnCode.HostResponseTimeout;
			}

		}

		if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) {
			return IOReturnCode.FEPTXNUpdateError; // L013
		}
		return rtnCode;
	}

	/**
	 * 交易資料與信用卡主機比對
	 * 
	 * 
	 * <history>
	 * <modify>
	 * <modifier>Ruling</modifier>
	 * <reason>Add for 線上比對主機回應電文帳號及金額資料：增加信用卡主機比對</reason>
	 * <date>2019/11/14</date>
	 * </modify>
	 * </history>
	 */
	private FEPReturnCode compareCreditTOTA(CreditTXCD txcd, CreditGeneral ascGeneralForRes) {
		BigDecimal txAmt = new BigDecimal(0);
		String bank_w = "";
		String actno_w = "";
		String bank_d = "";
		String actno_d = "";

		try {
			// 交易金額/轉出銀行別/轉入銀行別/轉入帳號
			switch (txcd) {
				case B07:
				case B10:
				case B11:
				case B20:
					// 交易金額
					txAmt = getFeptxn().getFeptxnTxAmt();

					// 轉出銀行別
					bank_w = StringUtils.rightPad(getFeptxn().getFeptxnTroutBkno(), 7, '0');

					// 轉入銀行別
					bank_d = StringUtils.rightPad(getFeptxn().getFeptxnTrinBkno(), 7, '0');

					// 轉入帳號
					actno_d = StringUtils.rightPad(getFeptxn().getFeptxnTrinActno(), 16, '0');
					break;
				default:
					break;
			}

			// 轉出帳號
			switch (txcd) {
				case B07:
					actno_w = StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0');

					break;
				case B10:
				case B20:
					// 2019/12/16 Modify by Ruling for 修正少補16位0造成現金加值Gift卡比對不符的問題
					actno_w = StringUtils.leftPad(getFeptxn().getFeptxnTroutActno(), 16, '0');

					break;
				case B11:
					actno_w = "00" + getFeptxn().getFeptxnIcmark().substring(0, 14);
					break;
				default:
					break;
			}

			// 比對交易金額
			if (!txAmt.equals(ascGeneralForRes.getResponse().getTXAMT())) {
				getLogContext().setRemark(String.format("Credit.TOTA欄位[TXAMT]比對不合, [信用卡]=%1$s,[FEP]=%2$s", ascGeneralForRes.getResponse().getTXAMT(), txAmt));
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[TXAMT]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對轉出銀行別
			if (!bank_w.equals(ascGeneralForRes.getResponse().getBankW())) {
				getLogContext().setRemark(String.format("Credit.TOTA欄位[BANK_W]比對不合, [信用卡]=%1$s,[FEP]=%2$s", ascGeneralForRes.getResponse().getBankW(), bank_w));
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[BANK_W]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對轉出帳號
			if (!actno_w.equals(ascGeneralForRes.getResponse().getActnoW())) {
				getLogContext().setRemark(String.format("Credit.TOTA欄位[ACTNO_W]比對不合, [信用卡]=%1$s,[FEP]=%2$s", ascGeneralForRes.getResponse().getActnoW(), actno_w));
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[ACTNO_W]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對轉入銀行別
			if (!bank_d.equals(ascGeneralForRes.getResponse().getBankD())) {
				getLogContext().setRemark(String.format("Credit.TOTA欄位[BANK_D]比對不合, [信用卡]=%1$s,[FEP]=%2$s", ascGeneralForRes.getResponse().getBankD(), bank_d));
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[BANK_D]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}

			// 比對轉入帳號
			if (!actno_d.equals(ascGeneralForRes.getResponse().getActnoD())) {
				getLogContext().setRemark(String.format("Credit.TOTA欄位[ACTNO_D]比對不合, [信用卡]=%1$s,[FEP]=%2$s", ascGeneralForRes.getResponse().getActnoD(), actno_d));
				this.logMessage(getLogContext()); // 寫Log
				getLogContext().setMessageParm13("[ACTNO_D]");
				TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.CompareTOTANotMatch, getLogContext()); // 寄EMail送EMS
				return FEPReturnCode.CompareTOTANotMatch;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".compareCreditTOTA");
			sendEMS(getLogContext());
			// TITA與TOTA比對有Exception，為了不影響交易仍回Normal
			return FEPReturnCode.Normal;
		}
		return null;
	}

	/**
	 * 準備送信用卡主機資料
	 * 
	 * @return
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>ATM Business</reason>
	 *         <date>2010/2/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>沖正時不需做PinBlockConvert</reason>
	 *         <date>2010/4/21</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Jim</modifier>
	 *         <reason>FPB欄位修改</reason>
	 *         <date>2010/4/22</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>BugReport(001B0264):FEPTXN_TXSEQ交易序號重覆取號ej=9940/9949；9942/9952；9946/9955</reason>
	 *         <date>2010/4/22</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>AUTHCD改用feptxn取值</reason>
	 *         <date>2010/5/31</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>Codeing Error:多搬移一次TripleDES且搬成defAtmStat.ATMSTAT_DES</reason>
	 *         <date>2010/06/04</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>電文定義已經左補零，不需要再自行塞值</reason>
	 *         <date>2010/06/25</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>1.coding omission:B03的邏輯遺漏，造成送ASC電文缺少卡號與序號
	 *         2.spec modify: 7/22 修正 -B16 才使用trk2 b20帶空白 ,STAN =10個0
	 *         3.補上B15,B01的問題</reason>
	 *         <date>2010/07/22</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>SPEC調整寫法，維護上更清楚;並修改tripleDES寫死為1</reason>
	 *         <date>2010/10/01</date>
	 *         </modify>
	 *         </history>
	 * @throws Exception
	 */
	private FEPReturnCode prepareCreditRequest(String ascTxid, byte txType, CreditGeneral ascGeneralForReq, RefBase<CreditTextBase> reqClass, RefBase<CreditTextBase> resClass, ATMENCHelper encHelper)
			throws Exception {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		@SuppressWarnings("unused")
		String trmno = StringUtils.EMPTY, txtno = StringUtils.EMPTY;
		// Tables.DBATMSTAT dbATMSTAT = new Tables.DBATMSTAT(FEPConfig.DBName); todo
		final String messageType = "A"; // 送ASC主機電文CD1欄位
		final String reviseMemno = "1002";
		final String chargeMemno = "0002";
		RefString refPinBlk1 = new RefString(StringUtils.EMPTY);
		RefString refPinBlk2 = new RefString(StringUtils.EMPTY);
		CreditTXCD txcd = CreditTXCD.parse(ascTxid);

		// ATM電文轉換PIN BLOCK
		// 2010-11-03 by kyo for spec update: /* FEPTXN_PINBLOCK <> ZEROS */
		if (StringUtils.isNotBlank(getFeptxn().getFeptxnPinblock()) && txType == (byte) CreditTxType.Accounting.getValue()
				&& !StringUtils.leftPad("0", getFeptxn().getFeptxnPinblock().length(), '0').equals(getFeptxn().getFeptxnPinblock())) { // 沖正不需做PinBlockConvert
			if (getFeptxn().getFeptxnBkno().equals(SysStatus.getPropertyValue().getSysstatHbkno())) {
				if (ATMTXCD.PNC.toString().equals(getFeptxn().getFeptxnTxCode())) {
					rtnCode = encHelper.pinBlockConvert(getGeneralData().getFpb2(), refPinBlk1);
					if (rtnCode == CommonReturnCode.Normal) { // 非normal會在下面檔掉
						rtnCode = encHelper.pinBlockConvert(getGeneralData().getFpb2New(), refPinBlk2);
					}

				} else {
					// modified by Maxine on 2011/09/14 for 新增B17信用卡電文
					if (txcd != CreditTXCD.B01 && txcd != CreditTXCD.B02 && txcd != CreditTXCD.B04 && txcd != CreditTXCD.B16 && txcd != CreditTXCD.B20 && txcd != CreditTXCD.B17) {
						// If txcd <> CreditTXCD.B01 AndAlso txcd <> CreditTXCD.B02 AndAlso txcd <> CreditTXCD.B04 AndAlso txcd <> CreditTXCD.B16 AndAlso txcd <> CreditTXCD.B20 Then
						rtnCode = encHelper.pinBlockConvert(getFeptxn().getFeptxnPinblock(), refPinBlk1);
					}
				}
			} else {// 被代理交易需轉換財金之 PIN BLOCK
				rtnCode = encHelper.pinBlockConvert(getFeptxn().getFeptxnPinblock(), refPinBlk1);
			}
		}
		String pinBlk1 = refPinBlk1.get();
		String pinBlk2 = refPinBlk2.get();

		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 如信用卡TITA電文有MAC欄位,須CALL DES取得MAC(B05,B15有欄位但暫不使用)
		if (txcd != CreditTXCD.B01 && txcd != CreditTXCD.B02 && txcd != CreditTXCD.B03 && txcd != CreditTXCD.B04 && txcd != CreditTXCD.B06 && txcd != CreditTXCD.B05 && txcd != CreditTXCD.B15) {
			RefString mac = new RefString();
			rtnCode = encHelper.makeScMac(txcd.toString(), mac);
			ascGeneralForReq.getRequest().setMAC(mac.get());
			if (rtnCode.getValue() != CommonReturnCode.Normal.getValue()) {
				ascGeneralForReq.getRequest().setMAC("");
			}
		}

		// 根據不同的主機電文代號呼叫不同的主機電文類別組電文
		// 2010-09-29 by kyo for spec modify:/* 9/30 修正 */
		/// * ASC_TITA HEADER */ /* 共同部份 */
		ascGeneralForReq.getRequest().setNUMBER("000000"); // 交易序號
		ascGeneralForReq.getRequest().setCD1(messageType); // 訊息型態
		ascGeneralForReq.getRequest().setCD2(String.valueOf(MsgSource.RequestFromBSP.getValue())); // 來源別
		ascGeneralForReq.getRequest().setCD3(String.valueOf(txType)); // MSG FLOW
		ascGeneralForReq.getRequest().setTXDATE(StringUtils.leftPad(CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()), 8, '0')); // 轉民國年
		/// *跨行-被代理交易之輸入行需用財金虛擬代號900 by Connie 10/20*/
		// modified by Maxine on 2011/09/16 for 判斷寫錯
		if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag()) && StringUtils.isNotBlank(getFeptxn().getFeptxnAtmnoVir())) {
			// If getFeptxn().getFeptxnFiscFlag() AndAlso Not String.IsNullOrEmpty(getFeptxn().getFeptxnAtmnoVir()) AndAlso String.IsNullOrEmpty(getFeptxn().getFeptxnAtmnoVir().Trim) Then
			ascGeneralForReq.getRequest().setKINBR(getFeptxn().getFeptxnAtmnoVir().substring(0, 3));
			ascGeneralForReq.getRequest().setWSNO(getFeptxn().getFeptxnAtmnoVir().substring(3, 5));
		} else {
			ascGeneralForReq.getRequest().setKINBR(getFeptxn().getFeptxnAtmno().substring(0, 3));
			ascGeneralForReq.getRequest().setWSNO(getFeptxn().getFeptxnAtmno().substring(3, 5));
		}

		// 2010-10-05 by kyo for spec modify:/* 10/1 修改 */
		if (txType == (byte) CreditTxType.Accounting.getValue()) {
			ascGeneralForReq.getRequest().setTXSEQ(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5, getFeptxn().getFeptxnTxseq().length()));
		} else if (txType == (byte) CreditTxType.EC.getValue()) {
			// 2010-11-04 by kyo for spec update: /* 11/4 修改 */
			if (StringUtils.isBlank(getFeptxn().getFeptxnConTxseq())) {
				ascGeneralForReq.getRequest().setTXSEQ(getFeptxn().getFeptxnTxseq().substring(getFeptxn().getFeptxnTxseq().length() - 5, getFeptxn().getFeptxnTxseq().length() - 5 + 5));
			} else {
				ascGeneralForReq.getRequest().setTXSEQ(getFeptxn().getFeptxnConTxseq().substring(getFeptxn().getFeptxnConTxseq().length() - 5, getFeptxn().getFeptxnConTxseq().length() - 5 + 5));
			}
		}
		/// *已與 Jennifer 確認跨行交易亦用自行營業日 by Connie 10/20*/
		ascGeneralForReq.getRequest().setTBSDY(StringUtils.rightPad(CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTbsdy()), 8, '0')); // 轉民國年
		ascGeneralForReq.getRequest().setMODE(getFeptxn().getFeptxnTxnmode().toString());
		ascGeneralForReq.getRequest().setTXCD(ascTxid);
		ascGeneralForReq.getRequest().setRC("    ");
		ascGeneralForReq.getRequest().setASPSTAN("000000");

		// 2017/08/31 Modify by Ruling for EMV卡檢核CVV失敗，增加代理現金送T24失敗，回沖時FEPTXN_CON_TX_CODE沒值，要塞CONFIRM電文代號回沖
		// 2010-11-11 by kyo for 沖正時需要待CONFIRM電文的交易代號 /* 11/11 修改 */
		if (txType == (byte) CreditTxType.EC.getValue() && (txcd == CreditTXCD.B07 || txcd == CreditTXCD.B10 || txcd == CreditTXCD.B11)) {
			/// * Req電文, 送信用卡沖正, 需轉換 APID */
			if (StringUtils.isBlank(getFeptxn().getFeptxnConTxCode())) {
				if ((ATMTXCD.CAV.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.CFV.name() + "");

				} else if ((ATMTXCD.CAM.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.CFM.name() + "");

				} else if ((ATMTXCD.CAA.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.CFA.name() + "");

				} else if ((ATMTXCD.CAJ.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.CFJ.name() + "");

				} else if ((ATMTXCD.ATF.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.AFF.name() + "");

				} else if ((ATMTXCD.BFT.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.BFF.name() + "");

				} else if ((ATMTXCD.ACW.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.ACF.name() + "");

				} else if ((ATMTXCD.IWD.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.IWF.name() + "");

				} else if ((ATMTXCD.IFT.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.IFF.name() + "");

				} else if ((ATMTXCD.EAV.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.EFV.name() + "");

				} else if ((ATMTXCD.EAM.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					getFeptxn().setFeptxnConTxCode(ATMTXCD.EFM.name() + "");

				}
			}
		}

		switch (txcd) {
			case B01: /// * COMBO卡開卡 */
				if (getFeptxn().getFeptxnTrk2().indexOf("=") == -1) {
					return FEPReturnCode.OtherCheckError;
				}

				// 2020/04/20 Modify by Ruling for 不用TRK3改以晶片卡卡號及序號組B01電文的金融卡帳號、金融卡號及金融卡序號
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(getFeptxn().getFeptxnMajorActno()); // 金融卡帳號
				// .ACCOUNT = "00" & getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad(getFeptxn().getFeptxnMajorActno(), 16, '0').substring(2, 16));
				// .ACTNO = getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0'));
				// .SEQNO = getFeptxn().FEPTXN_TRK3.Substring(59, 2) '金融卡序號

				/// * ASC_TITA DETAIL */
				// 2014/09/15 Modify by Ruling for COMBO優化：放入卡片效期(CashAdvance借用FEPTXN_NOTICE_ID)
				if (StringUtils.isBlank(getFeptxn().getFeptxnNoticeId())) {
					ascGeneralForReq.getRequest().setVALIDDT("0000"); // 卡片效期
				} else {
					ascGeneralForReq.getRequest().setVALIDDT(getFeptxn().getFeptxnNoticeId()); // 卡片效期
				}
				// .VALIDDT = "" '卡片效期
				ascGeneralForReq.getRequest().setBIRTHDAY(""); // 出生年月日
				ascGeneralForReq.getRequest().setACTION("1"); // 開卡
				ascGeneralForReq.getRequest().setASPSTOP(""); // 停卡理由

				reqClass.set(new B01_Request());
				resClass.set(new B01_Response());
				break;
			case B02: /// * COMBO卡停卡 */
				if (getFeptxn().getFeptxnTrk2().indexOf("=") == -1) {
					return FEPReturnCode.OtherCheckError;
				}

				// 2020/04/20 Modify by Ruling for 不用TRK3改以晶片卡卡號及序號組B02電文的金融卡帳號、金融卡號及金融卡序號
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(getFeptxn().getFeptxnMajorActno());
				// .ACCOUNT = "00" & getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad(getFeptxn().getFeptxnMajorActno(), 16, '0').substring(2, 16));
				// .ACTNO = getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, '0'));
				// .SEQNO = getFeptxn().FEPTXN_TRK3.Substring(59, 2) '金融卡序號

				/// * ASC_TITA DETAIL */
				ascGeneralForReq.getRequest().setVALIDDT(""); // 卡片效期
				ascGeneralForReq.getRequest().setBIRTHDAY(""); // 出生年月日
				ascGeneralForReq.getRequest().setACTION("1"); // 開卡
				ascGeneralForReq.getRequest().setASPSTOP("M"); // 停卡理由(強迫停卡/註銷)

				reqClass.set(new B01_Request());
				resClass.set(new B01_Response());
				break;
			case B03: /// *更改預借現金密碼/COMB國際卡密碼檢核 */
				/// * ASC_TITA HEADER */
				/// *Combo國際卡交易之卡號用Track2 by Connie 10/20*/
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
					ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTrk2().substring(0, 16)); // 卡號
					ascGeneralForReq.getRequest().setACCOUNT(getFeptxn().getFeptxnTroutActno()); // 金融卡帳號
					ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTrk2().substring(0, 16)); // 信用卡號
					ascGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnTroutActno().substring(2, 16)); // 金融卡號
					// 2011/11/29 Modify by Ruling for Combo卡國際卡提款，將卡號及序號寫入FEPTXN
					ascGeneralForReq.getRequest().setSEQNO(getFeptxn().getFeptxnCardSeq().toString()); // 金融卡序號
				} else {
					// 2012/07/15 Modify by Ruling for B03 電文點掉金融卡號
					ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
					// .ACCOUNT = "00" & getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡帳號
					ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
					// .ACTNO = getFeptxn().FEPTXN_TRK3.Substring(6, 14) '金融卡號
					// .SEQNO = getFeptxn().FEPTXN_TRK3.Substring(59, 2) '金融卡序號
				}

				/// * ASC_TITA DETAIL */
				ascGeneralForReq.getRequest().setTRACK2(getFeptxn().getFeptxnTrk2().substring(0, 37)); // 磁條二軌
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
					// 跨行
					ascGeneralForReq.getRequest().setTRACK1(getFeptxn().getFeptxnPinblock() + StringUtils.leftPad("", 60, ' '));
					ascGeneralForReq.getRequest().setOLDPINB(pinBlk1);
					ascGeneralForReq.getRequest().setNEWPINB(pinBlk1);
					ascGeneralForReq.getRequest().setACTION("1"); // 國際卡密碼檢核
					// modified by maxine on 12/7 修改 for COMB國際卡密碼檢核
					ascGeneralForReq.getRequest().setTripleDES("0"); // (0:Single DES, 1:Triple DES)
				} else {
					// 自行
					ascGeneralForReq.getRequest().setTRACK1(StringUtils.leftPad("", 76, ' '));
					ascGeneralForReq.getRequest().setOLDPINB(pinBlk1);
					ascGeneralForReq.getRequest().setNEWPINB(pinBlk2);
					ascGeneralForReq.getRequest().setACTION("0"); // 更改預借現金密碼
					// modified by maxine on 12/7 修改 for COMB國際卡密碼檢核
					ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)
				}
				// .TripleDES = "1" '(0:Single DES, 1:Triple DES)

				reqClass.set(new B03_Request());
				resClass.set(new B03_Response());
				break;
			case B05: /// * 帳務查詢類 */
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號

				/// * ASC_TITA DETAIL */
				if (!StringUtils.isBlank(getFeptxn().getFeptxnTrk2()) && getFeptxn().getFeptxnTrk2().length() > 36) {
					ascGeneralForReq.getRequest().setTrackIi(getFeptxn().getFeptxnTrk2().substring(0, 37));
				}
				ascGeneralForReq.getRequest().setFPB(pinBlk1);
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)

				reqClass.set(new B05_Request());
				resClass.set(new B05_Response());

				break;
			case B15: /// * 帳務查詢類 */
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT("00" + getFeptxn().getFeptxnIcmark().substring(0, 14)); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnIcmark().substring(0, 14)); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(getFeptxn().getFeptxnIcmark().substring(14, 16)); // 金融卡序號

				/// * ASC_TITA DETAIL */
				if (StringUtils.isNotBlank(getFeptxn().getFeptxnTrk2()) && getFeptxn().getFeptxnTrk2().length() > 36) {
					ascGeneralForReq.getRequest().setTrackIi(getFeptxn().getFeptxnTrk2().substring(0, 37));
				}
				ascGeneralForReq.getRequest().setFPB(pinBlk1);
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)

				reqClass.set(new B15_Request());
				resClass.set(new B15_Response());

				break;
			case B07: /// * ATM內部預借現金 */
				if (getFeptxn().getFeptxnTrk2().indexOf("=") == -1) {
					return FEPReturnCode.OtherCheckError;
				}
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號

				// 2017/08/31 Modify by Ruling for EMV卡檢核CVV失敗，信用卡REQ電文代號EMV晶片(5051)=EAV 磁條(2901,5801)=CAV；CON電文代號EMV晶片(5051)=EFV 磁條(2901,5801)=CFV，EAM類推
				// 2016/12/22 Modify by Ruling for EAV、EAM預借現金自行交易:EAV-->REQ電文代號=CAV;EAM-->REQ電文代號=CAM;EAV-->CON電文代號=CFV;EAM-->CON電文代號=CFM
				/// * ASC_TITA DETAIL */
				if (txType == (byte) CreditTxType.Accounting.getValue()) {
					if ((ATMTXCD.EAV.name() + "").equals(getFeptxn().getFeptxnTxCode())
							&& StringUtils.isNotBlank(getFeptxn().getFeptxnRemark()) && !getFeptxn().getFeptxnRemark().trim().equals("5051")) {
						ascGeneralForReq.getRequest().setAPID(ATMTXCD.CAV.name() + "");
					} else if ((ATMTXCD.EAM.name() + "").equals(getFeptxn().getFeptxnTxCode())
							&& StringUtils.isNotBlank(getFeptxn().getFeptxnRemark()) && !getFeptxn().getFeptxnRemark().trim().equals("5051")) {
						ascGeneralForReq.getRequest().setAPID(ATMTXCD.CAM.name() + "");
					} else {
						ascGeneralForReq.getRequest().setAPID(getFeptxn().getFeptxnTxCode()); // 交易種類
					}
				} else if (txType == (byte) CreditTxType.EC.getValue()) {
					if ((ATMTXCD.EAV.name() + "").equals(getFeptxn().getFeptxnTxCode())
							&& StringUtils.isNotBlank(getFeptxn().getFeptxnRemark()) && !getFeptxn().getFeptxnRemark().trim().equals("5051")) {
						ascGeneralForReq.getRequest().setAPID(ATMTXCD.CFV.name() + "");
					} else if ((ATMTXCD.EAM.name() + "").equals(getFeptxn().getFeptxnTxCode())
							&& StringUtils.isNotBlank(getFeptxn().getFeptxnRemark()) && !getFeptxn().getFeptxnRemark().trim().equals("5051")) {
						ascGeneralForReq.getRequest().setAPID(ATMTXCD.CFM.name() + "");
					} else {
						ascGeneralForReq.getRequest().setAPID(getFeptxn().getFeptxnConTxCode());// 交易種類
					}
				}
				ascGeneralForReq.getRequest().setBankW(getFeptxn().getFeptxnTroutBkno() + "0000"); // 轉出銀行別
				// 2010-08-30 by spec修正
				// .ACTNO_W = getFeptxn().getFeptxnTroutActno()
				ascGeneralForReq.getRequest().setActnoW(StringUtils.leftPad(getFeptxn().getFeptxnTrk2().substring(0, getFeptxn().getFeptxnTrk2().indexOf("=")), 16, '0')); // 轉出帳號
				ascGeneralForReq.getRequest().setBankD(getFeptxn().getFeptxnTrinBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoD(getFeptxn().getFeptxnTrinActno());
				ascGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
				ascGeneralForReq.getRequest().setFEE(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setID(getFeptxn().getFeptxnIdno());
				if (getFeptxn().getFeptxnTrk2().length() >= 5) {
					// modified By Maxine on 2011/11/07 for SPEC 11/07 修改 TRACK3=FEPTXN_TRK2(如不足 40位,右補空白)...
					ascGeneralForReq.getRequest().setTRACK3(StringUtils.rightPad(getFeptxn().getFeptxnTrk2(), 40, ' ')
							+ StringUtils.rightPad(getFeptxn().getFeptxnTrk2().substring(getFeptxn().getFeptxnTrk2().indexOf("=") + 1, 4), 104, ' '));
				}
				ascGeneralForReq.getRequest().setAvbalS("+");
				ascGeneralForReq.getRequest().setAVBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setCLASS(getFeptxn().getFeptxnPaytype());
				ascGeneralForReq.getRequest().setNOTICENO(getFeptxn().getFeptxnReconSeqno());
				ascGeneralForReq.getRequest().setICMARK(StringUtils.rightPad(" ", 30, ' '));
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)

				// 2017/10/19 Modify by Ruling for 配合信用卡拒絕自行預借現金FallBack交易
				if (!StringUtils.isBlank(getFeptxn().getFeptxnRemark())) {
					ascGeneralForReq.getRequest().setPOSENTRY(getFeptxn().getFeptxnRemark().trim());
				}

				if (txType == (byte) CreditTxType.EC.getValue()) { // 沖正電文
					// 2010-05-31 by kyo for spec修改:改用feptxn取值
					ascGeneralForReq.getRequest().setAUTHCD(getFeptxn().getFeptxnAuthcd());
					ascGeneralForReq.getRequest().setFPB(
							StringUtils.join(
									CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()).substring(1),
									StringUtils.leftPad(getFeptxn().getFeptxnAtmno().trim(), 5, '0'),
									getFeptxn().getFeptxnTxseq().substring(2)));
					ascGeneralForReq.getRequest().setMEMNO(reviseMemno);
				} else if (txType == (byte) CreditTxType.Accounting.getValue()) {// 入扣帳
					ascGeneralForReq.getRequest().setAUTHCD("0"); // 為原交易
					ascGeneralForReq.getRequest().setFPB(pinBlk1); // PinBlkConvert傳回值
					ascGeneralForReq.getRequest().setMEMNO(chargeMemno);
				}

				reqClass.set(new B07_Request());
				resClass.set(new B07_Response());

				break;
			case B09: /// * 錢卡非帳務性交易 */
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號

				/// * ASC_TITA DETAIL */
				ascGeneralForReq.getRequest().setAPID(getFeptxn().getFeptxnTxCode());
				ascGeneralForReq.getRequest().setFPB(pinBlk1);
				ascGeneralForReq.getRequest().setTRACK3(getFeptxn().getFeptxnTrk3());
				// 2010-06-24 by kyo for ACT_IDX/ACTBK1~15/ACTNO1~15 於電文定義已經左補零，不需要再自行塞值
				ascGeneralForReq.getRequest().setMEMNO(StringUtils.leftPad("", 4, ' '));
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)

				reqClass.set(new B09_Request());
				resClass.set(new B09_Response());

				break;
			case B10:
			case B11: /// * 錢卡帳務性交易 */
				// 2013/05/07 Modify by Ruling for COMBO卡預借現金B11電文的金融卡帳號(ACCOUNT)、金融卡號(ACTNO)、金融卡序號(SEQNO)要給值
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
				// .ACCOUNT = "0".PadLeft(16, "0"c) '金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
				if (txcd == CreditTXCD.B10) {
					ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
					ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
					ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號
				} else {
					// B11電文加上金融卡號及序號
					ascGeneralForReq.getRequest().setACCOUNT("00" + getFeptxn().getFeptxnIcmark().substring(0, 14)); // 金融卡帳號
					ascGeneralForReq.getRequest().setACTNO(getFeptxn().getFeptxnIcmark().substring(0, 14)); // 金融卡號
					ascGeneralForReq.getRequest().setSEQNO(getFeptxn().getFeptxnIcmark().substring(14, 16)); // 金融卡序號
				}

				/// * ASC_TITA DETAIL */
				if (txType == (byte) CreditTxType.Accounting.getValue()) {
					ascGeneralForReq.getRequest().setAPID(getFeptxn().getFeptxnTxCode());
				} else if (txType == (byte) CreditTxType.EC.getValue()) {
					ascGeneralForReq.getRequest().setAPID(getFeptxn().getFeptxnConTxCode());; // 交易種類
				}
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)
				ascGeneralForReq.getRequest().setBankW(getFeptxn().getFeptxnTroutBkno() + "0000");
				if (txcd == CreditTXCD.B11) {
					if (getFeptxn().getFeptxnIcmark().length() > 13) {
						ascGeneralForReq.getRequest().setActnoW("00" + getFeptxn().getFeptxnIcmark().substring(0, 14));
					}
				} else {
					ascGeneralForReq.getRequest().setActnoW(getFeptxn().getFeptxnTroutActno());
				}
				ascGeneralForReq.getRequest().setBankD(getFeptxn().getFeptxnTrinBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoD(getFeptxn().getFeptxnTrinActno());
				ascGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
				ascGeneralForReq.getRequest().setFEE(BigDecimal.valueOf(0));
				if (txType == (byte) CreditTxType.EC.getValue()) {
					// 2010-05-31 by kyo for spec修改:改用feptxn取值
					ascGeneralForReq.getRequest().setAUTHCD(getFeptxn().getFeptxnAuthcd());
					ascGeneralForReq.getRequest().setFPB(
							StringUtils.join(
									CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()).substring(1),
									StringUtils.leftPad(getFeptxn().getFeptxnAtmno(), 5, '0'),
									getFeptxn().getFeptxnTxseq().substring(2)));
				} else if (txType == (byte) CreditTxType.Accounting.getValue()) {
					ascGeneralForReq.getRequest().setAUTHCD("0");
					ascGeneralForReq.getRequest().setFPB(pinBlk1); // PinBlkConvert傳回值
				}
				ascGeneralForReq.getRequest().setID(getFeptxn().getFeptxnIdno());
				// 2010-07-22 by kyo for spec修改:7/21 修正B11不需搬移TRACK3
				// 2012/04/15 Modify by Ruling for COMBO卡預借現金B11電文代入有效期限
				if (txcd == CreditTXCD.B10) {
					ascGeneralForReq.getRequest().setTRACK3(getFeptxn().getFeptxnTrk3());
				} else {
					// 檢核CARD檔有效月年
					if (txType == (byte) CreditTxType.Accounting.getValue()) {
						// 入扣帳要帶效期(沖正沒讀Card，不用帶效期)
						if (StringUtils.isBlank(getCard().getCardCrEndMmyy())) {
							ascGeneralForReq.getRequest().setTRACK3(StringUtils.leftPad("", 40, ' ') + "9999" + StringUtils.leftPad("", 60, ' '));
						} else {
							if (getCard().getCardCrEndMmyy().substring(0, 2).compareTo("01") < 0 || getCard().getCardCrEndMmyy().substring(0, 2).compareTo("12") > 0) {
								getLogContext().setRemark("檢核CARD檔有效月年-信用卡有效月年(CARD_CR_END_MMYY)為NULL或空白或月份不合理, CARD_CR_END_MMYY=" + getCard().getCardCrEndMmyy().trim());
								this.logMessage(getLogContext());
								return FEPReturnCode.OtherCheckError;
							} else {
								ascGeneralForReq.getRequest().setTRACK3(StringUtils.leftPad("", 40, ' ') + getCard().getCardCrEndMmyy() + StringUtils.leftPad("", 60, ' '));
							}
						}
					}
				}
				ascGeneralForReq.getRequest().setAvbalS("+");
				ascGeneralForReq.getRequest().setAVBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setCLASS(getFeptxn().getFeptxnPaytype());
				ascGeneralForReq.getRequest().setNOTICENO(getFeptxn().getFeptxnReconSeqno());
				// 2010-07-22 by kyo for spec修改:7/21 財金序號 STAN=10個0 ATM 代號 ATMID= FEPTXN_ATMNO (5位)+3位空白
				ascGeneralForReq.getRequest().setATMID(getFeptxn().getFeptxnAtmno().substring(0, 5) + StringUtils.leftPad("", 3, ' '));
				ascGeneralForReq.getRequest().setSTAN(StringUtils.leftPad("0", 10, '0'));
				ascGeneralForReq.getRequest().setMEMNO(StringUtils.leftPad("", 4, ' '));
				// 2010-07-22 by kyo for spec修改:7/21 ICMARK搬移空白
				ascGeneralForReq.getRequest().setICMARK("");

				if (txcd == CreditTXCD.B10) {
					reqClass.set(new B10_Request());
					resClass.set(new B10_Response());
				} else {
					reqClass.set(new B11_Request());
					resClass.set(new B11_Response());
				}

				break;
			case B16: /// * GIFT卡餘額查詢 */
				/// * ASC_TITA HEADER */
				if ((ATMTXCD.GIQ.name() + "").equals(getFeptxn().getFeptxnTxCode())) {
					ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTroutActno()); // 卡號
					ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
					ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno()); // 信用卡號
					ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
					ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號
				} else {
					/// *跨行/自行加值 GIFT 卡用轉入帳號 by Connie 10/20*/
					ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTrinActno()); // 卡號
					ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
					ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTrinActno()); // 信用卡號
					ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
					ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號
				}

				/// * ASC_TITA DETAIL */
				if (ATMTXCD.GIQ.toString().equals(getFeptxn().getFeptxnTxCode())) {
					ascGeneralForReq.getRequest().setAPID(ATMTXCD.GIQ.toString());
				} else {
					ascGeneralForReq.getRequest().setAPID("GFI");
				}
				ascGeneralForReq.getRequest().setBankW(getFeptxn().getFeptxnTroutBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoW(getFeptxn().getFeptxnTroutActno());
				ascGeneralForReq.getRequest().setBankD(getFeptxn().getFeptxnTrinBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoD(getFeptxn().getFeptxnTrinActno());
				ascGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
				ascGeneralForReq.getRequest().setFEE(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setID(getFeptxn().getFeptxnIdno());
				// 2010-07-22 by kyo for spec modify: 7/22 修正 -B16 才使用trk2 b20帶空白 ,STAN =10個0
				ascGeneralForReq.getRequest().setTRACK3(getFeptxn().getFeptxnTrk2()); // 錢卡第三軌資料
				ascGeneralForReq.getRequest().setSTAN(StringUtils.leftPad("0", 10, '0'));
				ascGeneralForReq.getRequest().setAvbalS("+");
				ascGeneralForReq.getRequest().setAVBAL(BigDecimal.valueOf(0)); // 最高可用餘額
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0)); // 帳戶餘額2,3,4
				ascGeneralForReq.getRequest().setAvbal3S("+");
				ascGeneralForReq.getRequest().setAVBAL3(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setAvbal4S("+");
				ascGeneralForReq.getRequest().setAVBAL4(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setCLASS(getFeptxn().getFeptxnPaytype());
				ascGeneralForReq.getRequest().setNOTICENO(getFeptxn().getFeptxnReconSeqno());
				ascGeneralForReq.getRequest().setMEMNO(StringUtils.leftPad("", 4, ' '));
				ascGeneralForReq.getRequest().setICMARK(getFeptxn().getFeptxnIcmark());
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)
				if (txType == (byte) CreditTxType.EC.getValue()) {// 沖正電文
					// 2010-05-31 by kyo for spec修改:改用feptxn取值
					ascGeneralForReq.getRequest().setAUTHCD(getFeptxn().getFeptxnAuthcd());
					ascGeneralForReq.getRequest().setFPB(
							StringUtils.join(CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()).substring(1),
									StringUtils.leftPad(getFeptxn().getFeptxnAtmno().trim(), 5, '0'),
									getFeptxn().getFeptxnTxseq().substring(2)));
				} else if (txType == (byte) CreditTxType.Accounting.getValue()) {
					ascGeneralForReq.getRequest().setAUTHCD(StringUtils.leftPad("0", 6, '0'));
					ascGeneralForReq.getRequest().setFPB(""); // PinBlkConvert傳回值
				}

				reqClass.set(new B16_Request());
				resClass.set(new B16_Response());
				break;
			case B17: /// * GIFT卡餘額查詢 */
				// add by Maxine on 2011/09/14 for 查詢卡號或虛擬帳號是否為本人(B17)
				/// * ASC_TITA HEADER */
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnReconSeqno()); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnReconSeqno()); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號

				/// * ASC_TITA DETAIL */
				ascGeneralForReq.getRequest().setBankW(getFeptxn().getFeptxnTroutBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoW(getFeptxn().getFeptxnTroutActno());
				ascGeneralForReq.getRequest().setBankD(getFeptxn().getFeptxnTrinBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoD(getFeptxn().getFeptxnTrinActno());
				ascGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());

				// modified By Maxine on 2011/11/15 for spec change 11/15 修改
				// 2017/03/23 Modify by Ruling for 繳費網繳信用卡款
				if (getFeptxn().getFeptxnPcode().substring(0, 3).equals("256")) {
					ascGeneralForReq.getRequest().setID("");
				} else {
					if (StringUtils.isNotBlank(getFeptxn().getFeptxnIdno())) {
						if (getFeptxn().getFeptxnIdno().trim().length() == 11) {
							ascGeneralForReq.getRequest().setID(mappingFirstDigitIdno(getFeptxn().getFeptxnIdno().trim()));
						} else {
							ascGeneralForReq.getRequest().setID(getFeptxn().getFeptxnIdno().trim());
						}
					} else {
						ascGeneralForReq.getRequest().setID("");
					}
				}

				// 2017/03/23 Modify by Ruling for 繳費網繳信用卡款
				if (DbHelper.toBoolean(getFeptxn().getFeptxnFiscFlag())) {
					if (!StringUtils.isBlank(getFeptxn().getFeptxnStan())) {
						ascGeneralForReq.getRequest().setSTAN(getFeptxn().getFeptxnBkno() + getFeptxn().getFeptxnStan());
					} else {
						ascGeneralForReq.getRequest().setSTAN(StringUtils.rightPad("0", 10, '0'));
					}
					// .STAN = getFeptxn().getFeptxnBkno() & getFeptxn().getFeptxnStan()
				} else {
					ascGeneralForReq.getRequest().setSTAN(StringUtils.rightPad("0", 10, '0'));
				}

				if (getFeptxn().getFeptxnPcode().substring(0, 3).equals("226")) {
					ascGeneralForReq.getRequest().setCHKID("1");
				} else {
					ascGeneralForReq.getRequest().setCHKID("0");
				}

				ascGeneralForReq.getRequest().setTripleDES("1");

				reqClass.set(new B17_Request());
				resClass.set(new B17_Response());
				break;
			case B20: /// * GIFT卡餘額加值(B20) */
				/// * ASC_TITA HEADER */
				/// *轉帳加值 GIFT 卡一律用轉入帳號 by Connie 10/20*/
				ascGeneralForReq.getRequest().setCardNo(getFeptxn().getFeptxnTrinActno()); // 卡號
				ascGeneralForReq.getRequest().setACCOUNT(StringUtils.leftPad("0", 16, '0')); // 金融卡帳號
				ascGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTrinActno()); // 信用卡號
				ascGeneralForReq.getRequest().setACTNO(StringUtils.leftPad("0", 14, '0')); // 金融卡號
				ascGeneralForReq.getRequest().setSEQNO(StringUtils.leftPad("0", 2, '0')); // 金融卡序號

				/// * ASC_TITA DETAIL */
				ascGeneralForReq.getRequest().setAPID("ADD");
				ascGeneralForReq.getRequest().setBankW(getFeptxn().getFeptxnTroutBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoW(getFeptxn().getFeptxnTroutActno());
				ascGeneralForReq.getRequest().setBankD(getFeptxn().getFeptxnTrinBkno() + "0000");
				ascGeneralForReq.getRequest().setActnoD(getFeptxn().getFeptxnTrinActno());
				ascGeneralForReq.getRequest().setTXAMT(getFeptxn().getFeptxnTxAmt());
				ascGeneralForReq.getRequest().setFEE(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setID(getFeptxn().getFeptxnIdno());
				ascGeneralForReq.getRequest().setTRACK3(""); // 錢卡第三軌資料
				ascGeneralForReq.getRequest().setSTAN(StringUtils.leftPad("0", 10, '0'));
				ascGeneralForReq.getRequest().setAvbalS("+");
				ascGeneralForReq.getRequest().setAVBAL(BigDecimal.valueOf(0)); // 最高可用餘額
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0)); // 帳戶餘額2,3,4
				ascGeneralForReq.getRequest().setAvbal3S("+");
				ascGeneralForReq.getRequest().setAVBAL3(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setAvbal4S("+");
				ascGeneralForReq.getRequest().setAVBAL4(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setActbalS("+");
				ascGeneralForReq.getRequest().setACTBAL(BigDecimal.valueOf(0));
				ascGeneralForReq.getRequest().setCLASS(getFeptxn().getFeptxnPaytype());
				ascGeneralForReq.getRequest().setNOTICENO(getFeptxn().getFeptxnReconSeqno());
				ascGeneralForReq.getRequest().setMEMNO(StringUtils.leftPad("", 4, ' '));
				ascGeneralForReq.getRequest().setICMARK(getFeptxn().getFeptxnIcmark());
				ascGeneralForReq.getRequest().setTripleDES("1"); // (0:Single DES, 1:Triple DES)
				if (txType == (byte) CreditTxType.EC.getValue()) { // 沖正電文
					// 2010-05-31 by kyo for spec修改:改用feptxn取值
					ascGeneralForReq.getRequest().setAUTHCD(getFeptxn().getFeptxnAuthcd());
					ascGeneralForReq.getRequest().setFPB(
							StringUtils.join(
									CalendarUtil.adStringToROCString(getFeptxn().getFeptxnTxDate()).substring(1),
									StringUtils.leftPad(getFeptxn().getFeptxnAtmno().trim(), 5, '0'),
									getFeptxn().getFeptxnTxseq().substring(2)));
				} else if (txType == (byte) CreditTxType.Accounting.getValue()) {
					ascGeneralForReq.getRequest().setAUTHCD("0");
					ascGeneralForReq.getRequest().setFPB(""); // PinBlkConvert傳回值
				}

				reqClass.set(new B20_Request());
				resClass.set(new B20_Response());

				break;
			default:
				throw ExceptionUtil.createException("無法處理", txcd, "交易");

		}
		return rtnCode;
	}
	
	// Bruce Fly 2014/06/04 FOR COMBO批次優化新需求 增加B02電文處理
    // <summary>
    // 主流程
    // 1.檢核永豐信用卡通道
    // 2.組信用卡 TITA電文, 電文內容格式請參照: BSP_FEP_TO_ASP
    // 3.將組好的信用卡電文送往信用卡主機
    // </summary>
    // <param name="creditNo">信用卡卡號</param>
    // <param name="cardSeqNo">金融卡序號</param>
    // <param name="aspStop">停卡理由</param>
    // <returns></returns>
    // <remarks></remarks>
	public FEPReturnCode sendToCreditB02(String creditNo, String cardSeqNo, String aspStop) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		String tita = "";
		CreditGeneral ascGeneralForReq = new CreditGeneral();
		CreditGeneral ascGeneralForRes = new CreditGeneral();
		CreditTextBase reqClass = null;
		CreditTextBase resClass = null;
		try {
			// 1.檢核永豐信用卡通道
			FEPCache.reloadCache(CacheItem.SYSSTAT);
// 2024-03-06 Richard modified for SYSSTATE 調整
//			if (!DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAscChannel())) {
//				this.logContext.setRemark("SendToCredit-暫停永豐信用卡通道(SYSSTAT_ASC_CHANNEL)服務");
//				logMessage(Level.INFO, this.logContext);
//				return CommonReturnCode.WithdrawServiceStop; // '提款暫停服務(E948)
//			}

			// '2.組信用卡TITA電文，電文內容格式請參照：BSP_FEP_TO_ASP
			// '/* ASC_TITA HEADER - 共同部份*/
			// 交易序號
			ascGeneralForReq.getRequest().setNUMBER("000000");
			// 訊息型態
			ascGeneralForReq.getRequest().setCD1("A");
			// 來源別
			ascGeneralForReq.getRequest().setCD2(MsgSource.RequestFromBSP.toString());
			// 'MSG FLOW
			ascGeneralForReq.getRequest().setCD3("1");
			// '轉民國年
			ascGeneralForReq.getRequest().setTXDATE(
					StringUtils.leftPad(CalendarUtil.adStringToROCString(this.getFeptxn().getFeptxnTxDate()), 8, '0'));
			// 輸入行
			ascGeneralForReq.getRequest().setKINBR(this.getFeptxn().getFeptxnAtmno().substring(0, 3));
			// 櫃台機號
			ascGeneralForReq.getRequest().setWSNO(this.getFeptxn().getFeptxnAtmno().substring(3, 3 + 2));
			// 交易傳輸編號
			ascGeneralForReq.getRequest().setTXSEQ(
					this.getFeptxn().getFeptxnTxseq().substring(this.getFeptxn().getFeptxnTxseq().length() - 5, 5));
			// 轉民國年
			ascGeneralForReq.getRequest().setTBSDY(
					StringUtils.leftPad(CalendarUtil.adStringToROCString(this.getFeptxn().getFeptxnTbsdy()), 8, '0'));
			ascGeneralForReq.getRequest().setMODE(this.getFeptxn().getFeptxnTxnmode().toString());
			ascGeneralForReq.getRequest().setTXCD("B02");
			ascGeneralForReq.getRequest().setRC("    ");
			ascGeneralForReq.getRequest().setASPSTAN("000000");

			if (StringUtils.isBlank(creditNo)) {
				return FEPReturnCode.OtherCheckError;
			}

			// '2020/04/20 Modify by Ruling for 不用TRK3改以晶片卡卡號組B02電文的金融卡帳號、金融卡號
			// '/* ASC_TITA HEADER */
			// '卡號 
			ascGeneralForReq.getRequest().setCARDNO(StringUtils.leftPad(creditNo, 16, '0'));
			// '金融卡帳號
			ascGeneralForReq.getRequest().setACCOUNT(this.getFeptxn().getFeptxnMajorActno());
			// '信用卡號
			ascGeneralForReq.getRequest().setCardNo(StringUtils.leftPad(creditNo, 16, '0'));
			// '金融卡號 
			ascGeneralForReq.getRequest()
					.setACTNO(StringUtils.leftPad(this.getFeptxn().getFeptxnMajorActno(), 16, '0').substring(2, 14));
			// '金融卡序號
			ascGeneralForReq.getRequest().setSEQNO(cardSeqNo);

			// '/* ASC_TITA DETAIL */
			// '卡片效期
			ascGeneralForReq.getRequest().setVALIDDT("");
			// '出生年月日
			ascGeneralForReq.getRequest().setBIRTHDAY("");
			// '開卡
			ascGeneralForReq.getRequest().setACTION("2");
			// '停卡理由(強迫停卡/註銷)
			ascGeneralForReq.getRequest().setASPSTOP(aspStop);

			reqClass = new B02_Request();
//                resClass = new B02_Response();沒有這支 TODO

			// '3.將組好的信用卡電文送往信用卡主機
			tita = reqClass.makeMessageFromGeneral(ascGeneralForReq);
			CreditAdapter adapter = new CreditAdapter(this.getGeneralData());
			adapter.setChannel(this.getGeneralData().getTxChannel());
			adapter.setTimeout(CMNConfig.getInstance().getUnisysTimeout());
			adapter.setFEPSubSystem(this.getGeneralData().getTxSubSystem());
			adapter.setMessageToSC(tita);
			adapter.setEj(this.ej);
			adapter.setNoWait(true);// '不等回應

			rtnCode = adapter.sendReceive();

			// 'B02送信用卡主機失敗仍要回前端成功只寫LOG做紀錄
			if (rtnCode != CommonReturnCode.Normal) {
				this.logContext.setRemark("SendToCreditB02-送B02電文給信用卡失敗, rtnCode=" + rtnCode.toString());
				logMessage(Level.DEBUG, this.logContext);
			}

			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			this.logContext.setProgramException(ex);
			sendEMS(this.logContext);
			return CommonReturnCode.ProgramException;
		}
	}
}
