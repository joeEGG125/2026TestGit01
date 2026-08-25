package com.syscom.fep.server.common.business.rm;

import com.syscom.fep.base.aa.RMData;
import com.syscom.fep.base.aa.T24Data;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.RMConfig;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.configuration.DataSourceConstant;
import com.syscom.fep.mybatis.ext.mapper.*;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.BusinessBase;
import com.syscom.fep.vo.constant.*;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.vo.text.fcs.FCSDetail;
import com.syscom.fep.vo.text.fcs.FCSFoot;
import com.syscom.fep.vo.text.fcs.FCSHead;
import com.syscom.fep.vo.text.rm.RMGeneralRequest;
import com.syscom.fep.vo.text.rm.RMGeneralResponse;
import com.syscom.fep.vo.text.t24.T24PreClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.StringBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class RMCommon extends BusinessBase {
	@SuppressWarnings("unused")
	private StringBuilders RMResponseBody;
	@SuppressWarnings("unused")
	private StringBuilders RMResponseHeader;
	@SuppressWarnings("unused")
	private String MSGID = "0001";
	@SuppressWarnings("unused")
	private String MSGTEXT = "";
	protected RMGeneralRequest rmReq;
	protected RMGeneralResponse rmRes;
	private T24Data t24Data;
	public static final String XMLHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	public static final String T24RC_Normal = "0000";
	public static String CBSRC = "";
	private static final String SuccessTag = NormalRC.External_OK;
	public String FCSFTPServer = RMConfig.getInstance().getFCSFTPServer();
	public String FCSFTPPort = RMConfig.getInstance().getFCSFTPPort();
	public String FCSInPath_FTP = RMConfig.getInstance().getFCSInPathFTP();
	public String FCSOutPath_FTP = RMConfig.getInstance().getFCSOutPathFTP();
	public String FCSDealFilePath_FTP = RMConfig.getInstance().getFCSDealFilePathFTP();
	public String FCSFTPUserId = RMConfig.getInstance().getFCSFTPUserId();
	public String FCSFTPSscode = RMConfig.getInstance().getFCSFTPSscode();
	public String FCSInPath = RMConfig.getInstance().getFCSInPath();
	public String FCSOutPath = RMConfig.getInstance().getFCSOutPath();

	public static final String FEP = "FEP";
	public static final String BankIdToT24 = "TW8070";

	// 如果是入帳有RECORD.STATUS此欄位表示T24交易未完成；如果是沖正，含有RECORD.STATUS=REVE時代表沖正成功
	// '此flag是用來判斷以上邏輯的
	protected boolean isT24Finished = false;
	public String seeEJ = "";
	public String company = "";
	public T24PreClass txT24Message;
	public String CBSErrorMessage = "";
	public String noteSeq = "";

	protected RMCommon() {
		super();
	}

	protected RMCommon(RMData rmTxMsg) {
		setmRMData(rmTxMsg);
		rmReq = getmRMData().getTxObject().getRequest();
		rmRes = getmRMData().getTxObject().getResponse();
	}

	protected RMCommon(T24Data t24Msg) {
		t24Data = t24Msg;
	}

	public void prepareTOTAHeader(FEPReturnCode rtnCode) {

	}

	/**
	 * 組Response Header
	 * 
	 * @param rtnCode 串回應電文, 新版用法
	 */
	public void prepareResponseHeader(FEPReturnCode rtnCode) {
		getmRMData().getTxObject().getResponse().setChlEJNo(getmRMData().getTxObject().getRequest().getChlEJNo());
		// Modify by Jim, 2011/05/04, EJ補滿8位，總共要16位
		String sysDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
		String sEJ = StringUtils.leftPad(String.valueOf(getEj()), 8, '0');
		getmRMData().getTxObject().getResponse().setEJNo(sysDate + sEJ);
		getmRMData().getTxObject().getResponse().setRqTime(getmRMData().getTxObject().getRequest().getChlSendTime());
		getmRMData().getTxObject().getResponse().setRsTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_T_HH_MM_SS_SSS));

		if (StringUtils.isNotBlank(CBSRC) && CBSRC.equals(T24RC_Normal)) {
			TxHelper.getMessageFromFEPReturnCode(CommonReturnCode.CBSResponseError, getLogContext());
			getmRMData().getTxObject().getResponse().setRsStatRsStateCode(getLogContext().getExternalCode());
			getmRMData().getTxObject().getResponse().setRsStatDesc("主機檢核錯誤:" + CBSErrorMessage);
		} else if (CommonReturnCode.Normal.equals(rtnCode) || (CBSRC.equals(T24RC_Normal) && isT24Finished)) {
			getmRMData().getTxObject().getResponse().setRsStatRsStateCode(SuccessTag);
		} else {
			if ("".equals(getmRMData().getTxObject().getResponse().getRsStatDesc())) {
				getmRMData().getTxObject().getResponse().setRsStatDesc(TxHelper.getMessageFromFEPReturnCode(rtnCode, getLogContext()));
			}
			getmRMData().getTxObject().getResponse().setRsStatRsStateCode(getLogContext().getExternalCode());
		}
	}

	/**
	 * 負責準備APTOT/RMTOT內容此程式為 AA-RM-R2300,R1600,C1200之副程式
	 * 
	 * @param sTxCode
	 * @param sBrNo
	 * @param sKind
	 * @param sTxDate
	 * @param decTxAmt
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
	public FEPReturnCode processRMTOTAndRMTOTAL(String sTxCode, String sBrNo, String sKind, String sTxDate, BigDecimal decTxAmt) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmtotExtMapper dbRMTOT = SpringBeanFactoryUtil.getBean(RmtotExtMapper.class);
		Rmtot defRMTOT = new Rmtot();

		RmtotalExtMapper dbRMTOTAL = SpringBeanFactoryUtil.getBean(RmtotalExtMapper.class);
		Rmtotal defRMTOTAL = new Rmtotal();
		PlatformTransactionManager transactionManager = SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER);
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {

			// 依txCode更新RMTOT不同欄位
			defRMTOT.setRmtotTxdate(sTxDate);
			defRMTOT.setRmtotBrno("000");

			switch (sTxCode) {
				case RMTXCode.R1600:
				case RMTXCode.R1000:
				case RMTXCode.RT1301:
					defRMTOT.setRmtotRegCount(1);
					defRMTOT.setRmtotRegAmt(decTxAmt);
					break;
				case RMTXCode.RT1101:
					defRMTOT.setRmtotRegCount(-1);
					defRMTOT.setRmtotRegAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));;
					break;
				case RMTXCode.C1200:
					defRMTOT.setRmtotCbsOutCount(1);
					defRMTOT.setRmtotCbsOutAmt(decTxAmt);
					break;
				case RMTXCode.R2300:
				case RMTXCode.RT2301:
				case RMTXCode.SV11X2:
					defRMTOT.setRmtotCbsInCount(1);
					defRMTOT.setRmtotCbsInAmt(decTxAmt);
					break;
				case RMTXCode.R2400:
					defRMTOT.setRmtotCbsInCount(-1);
					defRMTOT.setRmtotCbsInAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					break;
			}

			if (dbRMTOT.updateForProcessRMTOT(defRMTOT) == 0) {
				dbRMTOT.insertSelective(defRMTOT);
			}

			// 依txCode更新RMTOTAL不同欄位()
			defRMTOTAL.setRmtotalDate(sTxDate);
			defRMTOTAL.setRmtotalBrno(sBrNo);

			switch (sTxCode) {
				case RMTXCode.R1600:
				case RMTXCode.R1000:
				case RMTXCode.RT1301:
					if (REMTXTP.Cash.equals(sKind)) {// 現金
						defRMTOTAL.setRmtotalFROCashNo(1);
						defRMTOTAL.setRmtotalFROCashAmt(decTxAmt);
					}
					if (REMTXTP.Transfer.equals(sKind) || REMTXTP.SeriesOfTransfer.equals(sKind)) {// 轉帳, 轉帳連動
						defRMTOTAL.setRmtotalFROTransfNo(1);
						defRMTOTAL.setRmtotalFROTransfAmt(decTxAmt);
					}
					break;
				case RMTXCode.RT1101:
					if (REMTXTP.Cash.equals(sKind)) {// 現金
						defRMTOTAL.setRmtotalFROCashNo(-1);
						defRMTOTAL.setRmtotalFROCashAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					}
					if (REMTXTP.Transfer.equals(sKind) || REMTXTP.SeriesOfTransfer.equals(sKind)) {// 轉帳, 轉帳連動
						defRMTOTAL.setRmtotalFROTransfNo(-1);
						defRMTOTAL.setRmtotalFROTransfAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					}
					break;
				case RMTXCode.C1200:
					defRMTOTAL.setRmtotalFRORTPayNoT(1);
					defRMTOTAL.setRmtotalFRORTPayAmtT(decTxAmt);
					break;
				case RMTXCode.R2300:
				case RMTXCode.RT2301:
					defRMTOTAL.setRmtotalFRIRTransfNo(1);
					defRMTOTAL.setRmtotalFRIRTransfAmt(decTxAmt);
					break;
				case RMTXCode.R2400:
					defRMTOTAL.setRmtotalFRIRTransfNo(-1);
					defRMTOTAL.setRmtotalFRIRTransfAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					break;
				case RMTXCode.SV11X2:
					defRMTOTAL.setRmtotalFRINo(1);
					defRMTOTAL.setRmtotalFRIAmt(decTxAmt);
					break;
			}

			if (dbRMTOTAL.updateForProcessRMTOTAL(defRMTOTAL) == 0) {
				dbRMTOTAL.insertSelective(defRMTOTAL);
			}
			transactionManager.commit(txStatus);
		} catch (Exception ex) {
			transactionManager.rollback(txStatus);
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			if (!txStatus.isCompleted()) {
				transactionManager.rollback(txStatus);
			}
		}

		return rtnCode;
	}

	/**
	 * 負責準備APTOT/RMTOT內容此程式為 BTOutBatch之副程式
	 * 
	 * @param sTxCode
	 * @param sBrNo
	 * @param sKind
	 * @param sTxDate
	 * @param decTxAmt
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
	public FEPReturnCode processRMTOTAndRMTOTALWithTrans(String sTxCode, String sBrNo, String sKind, String sTxDate, BigDecimal decTxAmt) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;

		RmtotExtMapper dbRMTOT = SpringBeanFactoryUtil.getBean(RmtotExtMapper.class);
		Rmtot defRMTOT = new Rmtot();

		RmtotalExtMapper dbRMTOTAL = SpringBeanFactoryUtil.getBean(RmtotalExtMapper.class);
		Rmtotal defRMTOTAL = new Rmtotal();
		try {
			// 依txCode更新RMTOT不同欄位
			defRMTOT.setRmtotTxdate(sTxDate);
			defRMTOT.setRmtotBrno("000");

			switch (sTxCode) {
				case RMTXCode.R1600:
				case RMTXCode.R1000:
				case RMTXCode.RT1301:
					defRMTOT.setRmtotRegCount(1);
					defRMTOT.setRmtotRegAmt(decTxAmt);
					break;
				case RMTXCode.RT1101:
					defRMTOT.setRmtotRegCount(-1);
					defRMTOT.setRmtotRegAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					break;
				case RMTXCode.C1200:
					defRMTOT.setRmtotCbsOutCount(1);
					defRMTOT.setRmtotCbsOutAmt(decTxAmt);
					break;
				case RMTXCode.R2300:
				case RMTXCode.RT2301:
				case RMTXCode.SV11X2:
					defRMTOT.setRmtotCbsInCount(1);
					defRMTOT.setRmtotCbsInAmt(decTxAmt);
					break;
				case RMTXCode.RT2302:
					defRMTOT.setRmtotCbsInCount(-1);
					defRMTOT.setRmtotCbsInAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					break;
			}

			if (dbRMTOT.updateForProcessRMTOT(defRMTOT) == 0) {
				dbRMTOT.insertSelective(defRMTOT);
			}

			// 依txCode更新RMTOTAL不同欄位()
			defRMTOTAL.setRmtotalDate(sTxDate);
			defRMTOTAL.setRmtotalBrno(sBrNo);

			switch (sTxCode) {
				case RMTXCode.R1600:
				case RMTXCode.R1000:
				case RMTXCode.RT1301:
					if (REMTXTP.Cash.equals(sKind)) {// 現金
						defRMTOTAL.setRmtotalFROCashNo(1);
						defRMTOTAL.setRmtotalFROCashAmt(decTxAmt);
					}
					if (REMTXTP.Transfer.equals(sKind) || REMTXTP.SeriesOfTransfer.equals(sKind)) {// 轉帳, 轉帳連動
						defRMTOTAL.setRmtotalFROTransfNo(1);
						defRMTOTAL.setRmtotalFROTransfAmt(decTxAmt);
					}
					break;
				case RMTXCode.RT1101:
					if (REMTXTP.Cash.equals(sKind)) {// 現金
						defRMTOTAL.setRmtotalFROCashNo(-1);
						defRMTOTAL.setRmtotalFROCashAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					}
					if (REMTXTP.Transfer.equals(sKind) || REMTXTP.SeriesOfTransfer.equals(sKind)) {// 轉帳, 轉帳連動
						defRMTOTAL.setRmtotalFROTransfNo(-1);
						defRMTOTAL.setRmtotalFROTransfAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					}
					break;
				case RMTXCode.C1200:
					defRMTOTAL.setRmtotalFRORTPayNoT(1);
					defRMTOTAL.setRmtotalFRORTPayAmtT(decTxAmt);
					break;
				case RMTXCode.R2300:
				case RMTXCode.RT2301:
					defRMTOTAL.setRmtotalFRIRTransfNo(1);
					defRMTOTAL.setRmtotalFRIRTransfAmt(decTxAmt);
					break;
				case RMTXCode.RT2302:
					defRMTOTAL.setRmtotalFRIRTransfNo(-1);
					defRMTOTAL.setRmtotalFRIRTransfAmt(BigDecimal.valueOf(-1 * decTxAmt.doubleValue()));
					break;
				case RMTXCode.SV11X2:
					defRMTOTAL.setRmtotalFRINo(1);
					defRMTOTAL.setRmtotalFRIAmt(decTxAmt);
					break;
			}

			if (dbRMTOTAL.updateForProcessRMTOTAL(defRMTOTAL) == 0) {
				dbRMTOTAL.insertSelective(defRMTOTAL);
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	/**
	 * 負責準備T24發動交易明細檔內容
	 * 
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
	public FEPReturnCode prepareFEPTXNByT24() {
		FEPReturnCode RC = CommonReturnCode.Normal;

		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
		Allbank defALLBANK = new Allbank();
		AllbankExtMapper dbALLBANK = SpringBeanFactoryUtil.getBean(AllbankExtMapper.class);

		@SuppressWarnings("unused")
		String wkISO3_CUR = "";
		HashMap<String, String> t24ReqData = null;

		try {
			t24ReqData = t24Data.getTxObject().getTITATransCondition();

			getFeptxn().setFeptxnTxDate(t24ReqData.get("DATE.TIME"));
			getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			getFeptxn().setFeptxnEjfno(getEj()); // 電子日誌序號'
			getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
			if (t24ReqData.get("T.SN.FISC.NO").length() >= 16) {
				getFeptxn().setFeptxnBrno(t24ReqData.get("T.SN.FISC.NO").substring(6, 9));
			}
			defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			}
			getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue());
			getFeptxn().setFeptxnChannel(FEPChannel.T24.name()); /// * 通道別 */
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行) 跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request); // (CBS REQUEST)
			getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
			// .FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
			if ("TMB.RMT.IN.RET".equals(t24Data.getTxObject().getVersion())) {// 退匯
				if (t24ReqData.get("T.OR.FISC.NO").length() >= 18) {
					getFeptxn().setFeptxnOrgrmsno(t24ReqData.get("T.OR.FISC.NO").substring(11, 18)); // 原通匯序號
					getFeptxn().setFeptxnOrgdate(t24ReqData.get("T.OR.FISC.NO").substring(0, 8)); // 原匯款日期
				}
			}

			if ("TMB.RMT.OUT".equals(t24Data.getTxObject().getVersion())) {// 匯出登錄
				getFeptxn().setFeptxnTxAmt(new BigDecimal(t24ReqData.get("DEBIT.AMOUNT")));

				defALLBANK.setAllbankBkno(SysStatus.getPropertyValue().getSysstatHbkno());
				if (t24ReqData.get("CREDIT.ACCT.NO").length() >= 16) {
					defALLBANK.setAllbankBrno(t24ReqData.get("CREDIT.ACCT.NO").substring(13, 16));
				}
				defALLBANK = dbALLBANK.selectByPrimaryKey(defALLBANK.getAllbankBkno(), defALLBANK.getAllbankBrno());
				if (defALLBANK != null) {
					getFeptxn().setFeptxnSenderBank(defALLBANK.getAllbankBkno() + defALLBANK.getAllbankBrno() + defALLBANK.getAllbankBrnoChkcode());
				}
				getFeptxn().setFeptxnReceiverBank(t24ReqData.get("T.BEN.BANK")); // 解款行
			} else {
				getFeptxn().setFeptxnTxAmt(new BigDecimal(t24ReqData.get("DEBIT.AMOUNT")));
			}

			// If t24Data.TxObject.Version.Substring(0, 7) = "TMB.RMT" Then
			// RC = MappingIOS3CUR(t24ReqData.Item("CURRENCY"), wkISO3_CUR)
			// If RC <> CommonReturnCode.Normal Then
			// .FEPTXN_TX_CUR_ACT = wkISO3_CUR
			// .FEPTXN_TX_CUR = wkISO3_CUR
			// Else
			// .FEPTXN_TX_CUR_ACT = wkISO3_CUR
			// .FEPTXN_TX_CUR = wkISO3_CUR
			// End If
			// .FEPTXN_FISC_CUR_MEMO = t24ReqData.Item("CURRENCY")
			// End If
			if ("TMB.RMT.OUT".equals(t24Data.getTxObject().getVersion())) {// 匯出登錄
				getFeptxn().setFeptxnTrinActno("00" + t24ReqData.get("T.BEN.ACCT.NO"));
				getFeptxn().setFeptxnTroutActno("00" + t24ReqData.get("DEBIT.ACCT.NO"));
			}

		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return RC;
	}

	/**
	 * 負責準備FEP交易明細檔內容此程式為櫃員發動AA之副程式
	 * 
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
	 *         <reason>modify the value of FEPTXN_TX_DATE</reason>
	 *         <date>2010/5/10</date>
	 *         </modify>
	 *         <modify>
	 *         <modifier>Kyo</modifier>
	 *         <reason>syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值</reason>
	 *         <date>2010/03/16</date>
	 *         </modify>
	 *         </history>
	 */
	public FEPReturnCode prepareFEPTXNByBRS() {
		FEPReturnCode RC = CommonReturnCode.Normal;
		Rmstat defRMSTAT = new Rmstat();
		RmstatExtMapper dbRMSTAT = SpringBeanFactoryUtil.getBean(RmstatExtMapper.class);
		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);
		String wkISO3_CUR = "";

		try {
			defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			defRMSTAT = dbRMSTAT.selectByPrimaryKey(defRMSTAT.getRmstatHbkno());
			if (defRMSTAT != null) {
				if (defRMSTAT.getRmstatRmFlag().equalsIgnoreCase("N")) {
					return CommonReturnCode.ChannelServiceStop;
				}
			}

			getFeptxn().setFeptxnTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			getFeptxn().setFeptxnEjfno(getEj()); // 電子日誌序號'
			getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
			getFeptxn().setFeptxnBrno(rmReq.getKINBR());

			defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			}
			getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue());
			getFeptxn().setFeptxnChannel(FEPChannel.BRANCH.name()); /// * 通道別 */
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行) 跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.BRS_Request); // (BRS REQUEST)
			getFeptxn().setFeptxnMsgid(getmRMData().getMessageID());
			getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
			// .FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
			if ("RF".equals(getmRMData().getMessageID().substring(0, 2))) {
				RefString refString = new RefString(wkISO3_CUR);
				if (mappingIOS3CUR(rmReq.getCURRENCY(), refString) != CommonReturnCode.Normal) {
					getFeptxn().setFeptxnTxCurAct("");
					getFeptxn().setFeptxnTxCur("");
				} else {
					wkISO3_CUR = refString.get();
					getFeptxn().setFeptxnTxCurAct(wkISO3_CUR);
					getFeptxn().setFeptxnTxCur(wkISO3_CUR);
				}
				getFeptxn().setFeptxnFiscCurMemo(rmReq.getCURRENCY());
				getFeptxn().setFeptxnTrinActno(rmReq.getRECCIF());
				// Delete by Jim, 2010/7/22, 刪除.FEPTXN_TROUT_ACTNO = rmReq.MACNO,
			} else {
				if (RMTXCode.R1600.equals(getmRMData().getMessageID()) || RMTXCode.R1000.equals(getmRMData().getMessageID()) || RMTXCode.RT1301.equals(getmRMData().getMessageID())) {
					getFeptxn().setFeptxnTrinActno(rmReq.getRECCIF());
					getFeptxn().setFeptxnTroutActno(rmReq.getMACTNO());
				}
				if (RMTXCode.R2300.equals(getmRMData().getMessageID())) {
					// .FEPTXN_TRIN_ACTNO = rmReq.MACTNO
				}
			}

			getFeptxn().setFeptxnTxAmt(rmReq.getREMAMT());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return RC;
	}

	/**
	 * 負責準備FEP交易明細檔內容此程式為 1611Out之副程式
	 * 
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
	public FEPReturnCode prepareFEPTXNByRMBTCHMTR(Rmbtchmtr defRMBTCHMTR) {
		FEPReturnCode RC = CommonReturnCode.Normal;

		Bsdays defBSDAYS = new Bsdays();
		BsdaysExtMapper dbBSDAYS = SpringBeanFactoryUtil.getBean(BsdaysExtMapper.class);

		try {
			getFeptxn().setFeptxnTxDate(defRMBTCHMTR.getRmbtchmtrRemdate());
			getFeptxn().setFeptxnTxTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
			getFeptxn().setFeptxnEjfno(getEj()); // 電子日誌序號'
			getFeptxn().setFeptxnBkno(SysStatus.getPropertyValue().getSysstatHbkno());
			getFeptxn().setFeptxnBrno(defRMBTCHMTR.getRmbtchmtrKinbr());

			defBSDAYS.setBsdaysZoneCode(ZoneCode.TWN);
			defBSDAYS.setBsdaysDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			defBSDAYS = dbBSDAYS.selectByPrimaryKey(defBSDAYS.getBsdaysZoneCode(), defBSDAYS.getBsdaysDate());
			if (defBSDAYS != null) {
				getFeptxn().setFeptxnTbsdyFisc(defBSDAYS.getBsdaysStDateRm()); // 財金營業日'
				getFeptxn().setFeptxnTbsdy(defBSDAYS.getBsdaysStDateRm()); // 本行營業日
			} else {
				// “無法取得日曆檔(BSDAYS)之RM清算日,請查明原因” 之訊息至 EMS
				getFeptxn().setFeptxnTbsdyFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
				getFeptxn().setFeptxnTbsdy(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
			}
			getFeptxn().setFeptxnSubsys((short) SubSystem.RM.getValue());
			getFeptxn().setFeptxnChannel(FEPChannel.FEP.name()); /// * 通道別 */
			getFeptxn().setFeptxnFiscFlag(DbHelper.toShort(true)); // (跨行) 跨行記號
			getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.CBS_Request); // (CBS REQUEST)
			// .FEPTXN_TX_CODE = RMTXCode.R1001
			getFeptxn().setFeptxnMsgid(RMTXCode.R1001);
			getFeptxn().setFeptxnReqRc(NormalRC.FISC_REQ_RC);
			// .FEPTXN_REQ_TIME = Now
			getFeptxn().setFeptxnTxDatetimeFisc(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return RC;
	}

	/**
	 * T24幣別轉成FEP幣別
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
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

		return rtnCode;
	}

	public String prepareBRSResponse(String bodyHeader) {
		String rtnStr = "";
		return rtnStr;
	}

	/**
	 * 取得原匯出資料
	 * 
	 * @param rtnCode
	 * @param msgFlow
	 * @return
	 */
	public FEPReturnCode updateFEPTxn(FEPReturnCode rtnCode, String msgFlow) {
		getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		try {
			if (CommonReturnCode.Normal.equals(rtnCode)) {
				getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
			} else {
				getFeptxn().setFeptxnTxrust(FeptxnTxrust.Reverse);
			}
			getFeptxn().setFeptxnMsgflow(msgFlow);
			rtnCode = updateTxData();
		} catch (Exception ex) {
			return IOReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}

	public Integer getRMNOCTL_NO() {
		try {

			return -1;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			sendEMS(getLogContext());
			return -1;
		}
	}

	public Rmoutt copyDataToRMOUTT(Rmout srcTable) {
		Rmoutt defRmoutt = new Rmoutt();
		defRmoutt.setRmouttActfee(srcTable.getRmoutActfee());
		defRmoutt.setRmouttAmtType(srcTable.getRmoutAmtType());
		defRmoutt.setRmouttApdate(srcTable.getRmoutApdate());
		defRmoutt.setRmouttBackReason(srcTable.getRmoutBackReason());
		defRmoutt.setRmouttBatchno(srcTable.getRmoutBatchno());
		defRmoutt.setRmouttBrno(srcTable.getRmoutBrno());
		defRmoutt.setRmouttBrsno(srcTable.getRmoutBrsno());
		defRmoutt.setRmouttCanceldate(srcTable.getRmoutCanceldate());
		defRmoutt.setRmouttCanceltime(srcTable.getRmoutCanceltime());
		defRmoutt.setRmouttCategory(srcTable.getRmoutCategory());
		defRmoutt.setRmouttCbsRc(srcTable.getRmoutCbsRc());
		defRmoutt.setRmouttCif(srcTable.getRmoutCif());
		defRmoutt.setRmouttEjno1(srcTable.getRmoutEjno1());
		defRmoutt.setRmouttEjno2(srcTable.getRmoutEjno2());
		defRmoutt.setRmouttEjno3(srcTable.getRmoutEjno3());
		defRmoutt.setRmouttFepno(srcTable.getRmoutFepno());
		defRmoutt.setRmouttFepsubno(srcTable.getRmoutFepsubno());
		defRmoutt.setRmouttFiscRtnCode(srcTable.getRmoutFiscRtnCode());
		defRmoutt.setRmouttFiscSndCode(srcTable.getRmoutFiscSndCode());
		defRmoutt.setRmouttFiscsno(srcTable.getRmoutFiscsno());
		defRmoutt.setRmouttGlUnit1(srcTable.getRmoutGlUnit1());
		defRmoutt.setRmouttGlUnit1a(srcTable.getRmoutGlUnit1a());
		defRmoutt.setRmouttGlUnit2(srcTable.getRmoutGlUnit2());
		defRmoutt.setRmouttGlUnit2a(srcTable.getRmoutGlUnit2a());
		defRmoutt.setRmouttInAccIdNo(srcTable.getRmoutInAccIdNo());
		defRmoutt.setRmouttInName(srcTable.getRmoutInName());
		defRmoutt.setRmouttInOrgAccIdNo(srcTable.getRmoutInOrgAccIdNo());
		defRmoutt.setRmouttMacno(srcTable.getRmoutMacno());
		defRmoutt.setRmouttMemo(srcTable.getRmoutMemo());
		defRmoutt.setRmouttOrderdate(srcTable.getRmoutOrderdate());
		defRmoutt.setRmouttOrgdate(srcTable.getRmoutOrgdate());
		defRmoutt.setRmouttOrgregFepno(srcTable.getRmoutOrgregFepno());
		defRmoutt.setRmouttOrgremtype(srcTable.getRmoutOrgremtype());
		defRmoutt.setRmouttOrgrmsno(srcTable.getRmoutOrgrmsno());
		defRmoutt.setRmouttOrgStat(srcTable.getRmoutOrgStat());
		defRmoutt.setRmouttOriginal(srcTable.getRmoutOriginal());
		defRmoutt.setRmouttOutName(srcTable.getRmoutOutName());
		defRmoutt.setRmouttOwpriority(srcTable.getRmoutOwpriority());
		defRmoutt.setRmouttPending(srcTable.getRmoutPending());
		defRmoutt.setRmouttPostamt(srcTable.getRmoutPostamt());
		defRmoutt.setRmouttRecbrno(srcTable.getRmoutRecbrno());
		defRmoutt.setRmouttReceiverBank(srcTable.getRmoutReceiverBank());
		defRmoutt.setRmouttRecfee(srcTable.getRmoutRecfee());
		defRmoutt.setRmouttRegdate(srcTable.getRmoutRegdate());
		defRmoutt.setRmouttRegtime(srcTable.getRmoutRegtime());
		defRmoutt.setRmouttRegTlrno(srcTable.getRmoutRegTlrno());
		defRmoutt.setRmouttRemagentid(srcTable.getRmoutRemagentid());
		defRmoutt.setRmouttRemagentname(srcTable.getRmoutRemagentname());
		defRmoutt.setRmouttRemcif(srcTable.getRmoutRemcif());
		defRmoutt.setRmouttRemitterid(srcTable.getRmoutRemitterid());
		defRmoutt.setRmouttRemtel(srcTable.getRmoutRemtel());
		defRmoutt.setRmouttRemtype(srcTable.getRmoutRemtype());
		defRmoutt.setRmouttRmsno(srcTable.getRmoutRmsno());
		defRmoutt.setRmouttSenddate(srcTable.getRmoutSenddate());
		defRmoutt.setRmouttSenderBank(srcTable.getRmoutSenderBank());
		defRmoutt.setRmouttSendtime(srcTable.getRmoutSendtime());
		defRmoutt.setRmouttServamtType(srcTable.getRmoutServamtType());
		defRmoutt.setRmouttStan(srcTable.getRmoutStan());
		defRmoutt.setRmouttStanBkno(srcTable.getRmoutStanBkno());
		defRmoutt.setRmouttStat(srcTable.getRmoutStat());
		defRmoutt.setRmouttSupno1(srcTable.getRmoutSupno1());
		defRmoutt.setRmouttSupno2(srcTable.getRmoutSupno2());
		defRmoutt.setRmouttT24No(srcTable.getRmoutT24No());
		defRmoutt.setRmouttTaxno(srcTable.getRmoutTaxno());
		defRmoutt.setRmouttTxamt(srcTable.getRmoutTxamt());
		defRmoutt.setRmouttTxdate(srcTable.getRmoutTxdate());
		defRmoutt.setRmouttUseBal(srcTable.getRmoutUseBal());
		defRmoutt.setUpdateTime(srcTable.getUpdateTime());
		defRmoutt.setUpdateUser(srcTable.getUpdateUser());
		defRmoutt.setUpdateUserid(srcTable.getUpdateUserid());
		return defRmoutt;
	}

	public void copyRMINToRMINE(Rmin defRmin, Rmine defRmine) {
		defRmine.setRmineTxdate(defRmin.getRminTxdate());
		defRmine.setRmineBrno(defRmin.getRminBrno());
		defRmine.setRmineFepno(defRmin.getRminFepno());
		defRmine.setRmineFepsubno(defRmin.getRminFepsubno());
		defRmine.setRmineTxamt(defRmin.getRminTxamt());
		defRmine.setRmineCategory(defRmin.getRminCategory());
		defRmine.setRmineSenderBank(defRmin.getRminSenderBank());
		defRmine.setRmineReceiverBank(defRmin.getRminReceiverBank());
		defRmine.setRmineStan(defRmin.getRminStan());
		defRmine.setRmineFiscsno(defRmin.getRminFiscsno());

		defRmine.setRmineRmsno(defRmin.getRminRmsno());
		defRmine.setRmineStat(defRmin.getRminStat());
		defRmine.setRmineCbsNo(defRmin.getRminCbsNo());
		defRmine.setRmineSenddate(defRmin.getRminSenddate());
		defRmine.setRmineSendtime(defRmin.getRminSendtime());
		defRmine.setRmineOrgrmsno(defRmin.getRminOrgrmsno());
		defRmine.setRmineOrgdate(defRmin.getRminOrgdate());
		defRmine.setRmineOrgregNo(defRmin.getRminOrgregNo());
		defRmine.setRmineOrgStat(defRmin.getRminOrgStat());
		defRmine.setRmineBackReason(defRmin.getRminBackReason());

		defRmine.setRmineFiscRtnCode(defRmin.getRminFiscRtnCode());
		defRmine.setRmineOutName(defRmin.getRminOutName());
		defRmine.setRmineInName(defRmin.getRminInName());
		defRmine.setRmineMemo(defRmin.getRminMemo());
		defRmine.setRmineId(defRmin.getRminId());
		defRmine.setRmineInOrgAccIdNo(defRmin.getRminInOrgAccIdNo());
		defRmine.setRmineInAccIdNo(defRmin.getRminInAccIdNo());
		defRmine.setRmineSupno1(defRmin.getRminSupno1());
		defRmine.setRmineSupno2(defRmin.getRminSupno2());
		defRmine.setRmineRegTlrno(defRmin.getRminRegTlrno());

		defRmine.setRmineFiscSndCode(defRmin.getRminFiscSndCode());
		defRmine.setRmineCbsRc(defRmin.getRminCbsRc());
		defRmine.setRminePending(defRmin.getRminPending());
		defRmine.setRmineEjno1(defRmin.getRminEjno1());
		defRmine.setRmineEjno2(defRmin.getRminEjno2());
		defRmine.setRmineEjno3(defRmin.getRminEjno3());
		defRmine.setRmineBrsno(defRmin.getRminBrsno());
		defRmine.setRmineRecdate(defRmin.getRminRecdate());
		defRmine.setRmineRectime(defRmin.getRminRectime());
		defRmine.setRmineRttlrno(defRmin.getRminRttlrno());

		defRmine.setRminePrtcnt(defRmin.getRminPrtcnt());
		defRmine.setRmineOrgremtype(defRmin.getRminOrgremtype());
		defRmine.setRmineDmyactno(defRmin.getRminDmyactno());
		defRmine.setRmineActno(defRmin.getRminActno());
		defRmine.setRmineActno2(defRmin.getRminActno2());
		defRmine.setRmineCifname(defRmin.getRminCifname());
		defRmine.setRmineTmpStat(defRmin.getRminTmpStat());
		defRmine.setRmineTmpRc(defRmin.getRminTmpRc());
		defRmine.setRmineActInactno(defRmin.getRminActInactno());
		defRmine.setRmineGlUnit1(defRmin.getRminGlUnit1());

		defRmine.setRmineGlUnit2(defRmin.getRminGlUnit2());
		defRmine.setRmineGlUnit1a(defRmin.getRminGlUnit1a());
		defRmine.setRmineGlUnit2a(defRmin.getRminGlUnit2a());
		defRmine.setUpdateUserid(defRmine.getUpdateUserid());
		defRmine.setUpdateTime(defRmine.getUpdateTime());
		defRmine.setRmineStanBkno(defRmine.getRmineStanBkno());
		defRmine.setRmineKinbr(defRmine.getRmineKinbr());
	}

	public void initialRMOUTTFieldStatus(Boolean bPassed, Rmoutt defRmoutt) {

	}

	public void writeDebugLog(String sFunctionName, String msg) {

	}

	/**
	 * 下載FCS檔案
	 * 
	 * @param fileName
	 * @return
	 */
	public FEPReturnCode downloadFCS(String fileName) {

//		try {
//
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			sendEMS(getLogContext());
//			return CommonReturnCode.ProgramException;
//		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 上傳FCS檔案
	 * 
	 * @param fileName
	 * @return
	 */
	public FEPReturnCode upLoadFCS(String fileName) {
//		try {
//
//		} catch (Exception ex) {
//			getLogContext().setProgramException(ex);
//			sendEMS(getLogContext());
//			return CommonReturnCode.ProgramException;
//		}
		return CommonReturnCode.Normal;
	}

	/**
	 * 判斷FTP路徑是否存在
	 * 
	 * @param FTPServer
	 * @param Directory
	 * @return
	 */
	public boolean ftpDirectoryExist(String FTPServer, String Directory) {
		return true;
	}

	/**
	 * 建立路徑
	 * 
	 * @param FTPServer
	 * @param Directory
	 * @return
	 */
	public boolean makeFTPDirectory(String FTPServer, String Directory) {
		return true;
	}

	/**
	 * 移動FTP上的檔案
	 * 
	 * @param SrcFile
	 * @param DesDirectory
	 * @param DesFileName
	 * @return
	 */
	public boolean moveFTPFile(String SrcFile, String DesDirectory, String DesFileName) {
		return true;
	}

	/**
	 * Parse FCS File
	 * 
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public FEPReturnCode readAndParseFCSFile(String filePath, RefBase<FCSHead> head, RefBase<ArrayList> detailList, RefBase<FCSFoot> foot, Boolean isCheck) {
		try (BufferedReader sr = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "Big5"))) {
			int iSendCNT = 0;
			BigDecimal decSendAmt = new BigDecimal(0);
			int iTotalCNT = 0;
			BigDecimal decTotalAmt = new BigDecimal(0);
			FCSDetail objFCSData = null;
			// Dim objFCSHead As FCSHead
			// Dim objFCSFoot As FCSFoot
			ArrayList _FCSDataS = null;
			String line = "";

			// 讀取header
			// Head = New FCSHead
			line = sr.readLine();
			head.get().parse(line);

			_FCSDataS = new ArrayList();
			line = sr.readLine();
			// 2011-12-26 modified by KK for 更改原先資料間只能空一行的設計

			while ("".equals(line.trim())) {
				line = sr.readLine();
			}
			// If line.Trim = "" Then
			// line = sr.ReadLine()
			// End If
			do {
				objFCSData = new FCSDetail();
				if (objFCSData.get_TotalLength().compareTo(new BigDecimal(ConvertUtil.toBytes(line, PolyfillUtil.toCharsetName("big5")).length)) != 0) {
					break;
				}
				objFCSData.parse(line);
				_FCSDataS.add(detailList);

				if (objFCSData.get_DATA_FLAG().equalsIgnoreCase("V")) {
					iSendCNT = iSendCNT + 1;
					decSendAmt = decSendAmt.add(objFCSData.get_REMAMT());
				}

				iTotalCNT = iTotalCNT + 1;
				decTotalAmt = decSendAmt.add(objFCSData.get_REMAMT());

				line = sr.readLine();
				// 2011-12-26 modified by KK for 更改原先資料間只能空一行的設計
				while ("".equals(line.trim())) {
					line = sr.readLine();
				}
				// If line.Trim = "" Then
				// line = sr.ReadLine()
				// End If
			} while (sr.readLine() != null || !"".equals(sr.readLine().trim()));

			// 讀取Foot
			FCSFoot newFoot = new FCSFoot();
			foot.set(newFoot);
			foot.get().parse(line);
			sr.close();
			if (isCheck) {
				if (!FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN).equals(head.get().getRemDate())) {
					// wkRtnCode = RMReturnCode.RemitDateError
					return RMReturnCode.RemitDateError;
				}
				if (iSendCNT != foot.get().get_SUCESS_CNT() || decSendAmt.compareTo(foot.get().get_SUCESS_AMT()) != 0
						|| iTotalCNT != foot.get().get_CNT() || decTotalAmt.compareTo(foot.get().get_AMT()) != 0) {
					// wkRtnCode = RMReturnCode.TotalamtNotMatch
					return RMReturnCode.TotalamtNotMatch;
				}
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setReturnCode(CommonReturnCode.ProgramException);
			getLogContext().setRemark("RMBusiness.ReadAndParseFCSFile Exception-" + ex.getMessage());
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}

	}

	/**
	 * 送電文至FCS Server
	 * 
	 * @param FileName
	 * @param APReturnCode
	 * @param APErrorMsg
	 * @return
	 */
	public FEPReturnCode sendRequestToFCS(String FileName, String APReturnCode, String APErrorMsg) {
		return CommonReturnCode.Normal;
	}

	/**
	 * 對應MSGOUT_STAT的中文
	 * 
	 * @param msgoutStat
	 * @return
	 */
	public String mapMSGOUTStat(String msgoutStat) {
		switch (msgoutStat) {
			case "01":
				return "01-未發送";
			case "02":
				return "02-已發送";
			case "03":
				return "03-已取消";
			case "04":
				return "04-傳送中";
			case "05":
				return "05-已傳送";
			case "06":
				return "06-財金拒絕";
			case "07":
				return "07-FEP取消";
			default:
				return "99-計算機問題";
		}
	}

	/**
	 * 對應MSGOUT_STAT的中文
	 * 
	 * @param msginStat
	 * @return
	 */
	public String mapMSGINStat(String msginStat) {
		switch (msginStat) {
			case MSGINStatus.Received:
				return "01-已收訊";
			case MSGINStatus.FEPCheckError:
				return "09-FEP檢核有誤";
			default:
				return msginStat;
		}
	}

	/**
	 * 對應RMIN_STAT的中文
	 * 
	 * @param rminStat
	 * @return
	 */
	public String mapRMINStat(String rminStat) {
		switch (rminStat) {
			case RMINStatus.FEPCheckError:
				return "09-FEP檢核有誤";
			case RMINStatus.AutoEnterAccount:
				return "01-自動入戶";
			case RMINStatus.WaitRemit:
				return "02-待解款";
			case RMINStatus.Remited:
				return "03-已解款";
			case RMINStatus.AutoBackRemit:
				return "04-自動退匯";
			case RMINStatus.BackRemitWaitPass:
				return "05-匯入款退匯待放行";
			case RMINStatus.BackRemitPassed:
				return "06-匯入款退匯";
			case RMINStatus.RMInStoreByHand:
				return "07-滯留自動入戶";
			case RMINStatus.HitAML:
				return "08-疑似 HIT AML";
			case RMINStatus.TransferringAML:
				return "98-傳送中(AML)";
			case RMINStatus.Transferring:
				return "99-傳送中(CBS)";
			default:
				return "";
		}
	}

	/**
	 * 對應RMIN_TMP_STAT的中文
	 * 
	 * @param rminStat
	 * @return
	 */
	public String mapRMINTMPStat(String rminStat) {
		switch (rminStat) {
			case RMIN_TMP_STATUS.UnProcess:
				return "A-未處理";
			case RMIN_TMP_STATUS.Processing:
				return "B-處理中";
			case RMIN_TMP_STATUS.WaitForPass:
				return "C-待放行";
			case RMIN_TMP_STATUS.Passed:
				return "D-已放行";
			case RMIN_TMP_STATUS.BackRemit:
				return "E-退匯";
			case RMIN_TMP_STATUS.NoProcess:
				return "F-不予處理";
			case RMIN_TMP_STATUS.GrantMoney:
				return "G-已解款";
			case RMIN_TMP_STATUS.InAccountByHandFail:
				return "H-人工入帳失敗";
			case RMIN_TMP_STATUS.FEPCheckError:
				return "I-FEP檢核錯誤";
			case RMIN_TMP_STATUS.WaitForReCheck:
				return "J-待覆核";
			case RMIN_TMP_STATUS.All:
				return "Z-全部";
			default:
				return "";
		}
	}

	public String mapRMPending(String input) {
		switch (input) {
			case RMPending.Fail:
				return "R-不明";
			case RMPending.Pending:
				return "P-CBS Pending";
			case RMPending.Normal:
				return "0-正常";
			default:
				return input;
		}
	}

	/**
	 * 對應RMIN_BACKREASON的中文
	 * 
	 * @param reason
	 * @return
	 */
	public String mapRMINBackReason(String reason) {
		switch (reason) {
			case RMBackReason.Normal:
				return "00-正常";
			case RMBackReason.RMSNOError:
				return "01-通匯序號錯誤";
			case RMBackReason.MACError:
				return "02-押碼不符";
			case RMBackReason.ReceiverNoError:
				return "03-收款人帳號錯誤";
			case RMBackReason.ReceiverNameError:
				return "04-收款人姓名錯誤";
			case RMBackReason.ElseError:
				return "99-其他錯誤理由";
			default:
				return reason;
		}
	}

	/**
	 * 對應RMOUT_STAT的中文
	 * 
	 * @param stat
	 * @return
	 */
	public String mapRMOUTStat(String stat) {
		switch (stat) {
			case RMOUTStatus.Wait:
				return "02-待補登";
			case RMOUTStatus.WaitForPass:
				return "03-待放行";
			case RMOUTStatus.Passed:
				return "04-已放行";
			case RMOUTStatus.Transfered:
				return "05-傳送中";
			case RMOUTStatus.GrantMoney:
				return "06-已解款";
			case RMOUTStatus.FISCReject:
				return "07-財金拒絕";
			case RMOUTStatus.BackExchange:
				return "08-匯出退匯";
			case RMOUTStatus.DeletedNotUpdateAccount:
				return "09-已刪除未更帳";
			case RMOUTStatus.DeleteAlreadyUpdateAccount:
				return "10-已刪除已更帳";
			case RMOUTStatus.DiskBatchRMOutFail:
				return "11-磁片整批匯出失敗";
			case RMOUTStatus.DeleteEmergencyTransfer:
				return "12-緊急匯款已刪除";
			case RMOUTStatus.Deleted:
				return "13-已刪除";
			case RMOUTStatus.SystemProblem:
				return "99-系統問題";
			default:
				return stat;
		}
	}

	/**
	 * 對應RMOUT_REMTYPE的中文
	 * 
	 * @param remtype
	 * @return
	 */
	public String mapRMOUTRemtype(String remtype) {
		switch (remtype) {
			case RMOUT_REMTYPE.InterBankRM:
				return "11-跨行電匯";
			case RMOUT_REMTYPE.PublicRM:
				return "12-公庫匯款";
			case RMOUT_REMTYPE.BusinessRM:
				return "13-同業匯款";
			case RMOUT_REMTYPE.ReturnRMIN:
				return "17-退還匯款";
			case RMOUT_REMTYPE.StockRM:
				return "18-證券匯款";
			case RMOUT_REMTYPE.TicketRM:
				return "19-票券匯款";
			default:
				return remtype;
		}
	}

	/**
	 * 對應RMOUT_ORIGINAL的中文
	 * 
	 * @param original
	 * @return
	 */
	public String mapRMOUTOriginal(String original) {
		switch (original) {
			case RMOUT_ORIGINAL.Counter:
				return "0-臨櫃";
			case RMOUT_ORIGINAL.FCS:
				return "1-FCS";
			case RMOUT_ORIGINAL.FEDI:
				return "2-FEDI";
			case RMOUT_ORIGINAL.FISC:
				return "3-FISC";
			case RMOUT_ORIGINAL.MMAB2B:
				return "4-MMAB2B";
			case RMOUT_ORIGINAL.EmergencyTransfer:
				return "5-緊急匯款";
			case RMOUT_ORIGINAL.ReturnRMIN:
				return "6-匯入款退匯";
			default:
				return original;
		}
	}

	/**
	 * 根據BKNO.BRNO,ALLBANK_TYPE計算BRNO_CHKCODE
	 * 
	 * @param sALLBANK_BKNO
	 * @param sALLBANK_BRNO
	 * @param sALLBANK_TYPE
	 * @return
	 * 
	 */
	public String getBankDigit(RefString sALLBANK_BKNO, RefString sALLBANK_BRNO, RefString sALLBANK_TYPE) {
		int digit = 0;
		if (StringUtils.isBlank(sALLBANK_BKNO.get()) || StringUtils.isBlank(sALLBANK_BRNO.get()) || StringUtils.isBlank(sALLBANK_TYPE.get())) {
			return "";
		}
		// modify by Candy 2013-03-05 銀行種類彰銀固定為=3
		if ("009".equals(sALLBANK_BKNO.get())) {
			sALLBANK_TYPE.set("3");
		}
		if (!"3".equals(sALLBANK_TYPE.get())) {
			digit = (Integer.parseInt(sALLBANK_BKNO.get().substring(0, 1)) * 3 + Integer.parseInt(sALLBANK_BKNO.get().substring(1, 2)) * 7 + Integer.parseInt(sALLBANK_BKNO.get().substring(2, 3)) * 9
					+ Integer.parseInt(sALLBANK_BRNO.get().substring(0, 1)) * 3 + Integer.parseInt(sALLBANK_BRNO.get().substring(1, 2)) * 7 + Integer.parseInt(sALLBANK_BRNO.get().substring(2, 3)) * 9)
					% 10;
			if ("0".equals(sALLBANK_TYPE.get()) || "1".equals(sALLBANK_TYPE.get())) {
				return String.valueOf(10 - digit).substring(String.valueOf(10 - digit).length() - 1);
			} else {
				return (String.valueOf(digit).substring(String.valueOf(digit).length() - 1));
			}
		} else {
			digit = (Integer.parseInt(sALLBANK_BRNO.get().substring(0, 1)) * 1 + Integer.parseInt(sALLBANK_BRNO.get().substring(1, 2)) * 4 + Integer.parseInt(sALLBANK_BRNO.get().substring(2, 3)) * 7)
					% 10;
			return (String.valueOf(digit).substring(String.valueOf(digit).length() - 1));
		}
	}

}
