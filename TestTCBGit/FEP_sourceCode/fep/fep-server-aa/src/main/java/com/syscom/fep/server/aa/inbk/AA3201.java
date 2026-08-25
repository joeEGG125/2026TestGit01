package com.syscom.fep.server.aa.inbk;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;

// ''' <summary>
// ''' 2 Way
// ''' 1. 財金換日通知交易(NOTICE ID = 2001)
// ''' 2. 金融帳戶開戶查詢通知交易(NOTICE ID = 4101)
// ''' 3. 金融帳戶開戶查詢異常處理狀況通知交易(NOTICE ID = 4103)
// ''' 本支負責處理電文如下
// ''' NOTICE ID = 2001
// ''' REQUEST ：OC005
// ''' RESPONSE：OC006
// ''' NOTICE ID = 4101
// ''' REQUEST ：OC005A
// ''' RESPONSE ：OC006A
// ''' NOTICE ID = 4103
// ''' REQUEST：OC005B
// ''' RESPONSE ：OC006B
// ''' </summary>
// ''' <remarks>
// '''AA程式撰寫原則:
// '''AA的程式主要為控制交易流程,Main為AA程式的進入點,在Main中的程式為控制交易的過程該如何進行
// '''請不要在Main中去撰寫實際的處理細節,儘可能將交易過程中的每一個"步驟",以副程式的方式來撰寫,
// '''而這些步驟,如果可以共用的話,請將該步驟寫在相關的Business物件中
// '''如果該步驟只有該AA會用到的話,再寫至自己AA中的類別中
// ''' </remarks>
// ''' <history>
// ''' <modify>
// ''' <modifier>Henny</modifier>
// ''' <reason>AA Template</reason>
// ''' <date>2010/3/25</date>
// ''' </modify>
// ''' </history>
public class AA3201 extends INBKAABase {
	private String strNoticeSource;
	private String strNoticeID;
	private String strNoticeID3201;
	private String strNoticeData;
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCodeReq = CommonReturnCode.Normal;

	// #Region "建構式"
	// ''' <summary>
	// ''' AA的建構式,在這邊初始化及設定其他相關變數
	// ''' </summary>
	// ''' <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
	// ''' <remarks>
	// ''' 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	// ''' ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	// ''' FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	// ''' </remarks>
	public AA3201(FISCData txnData) throws Exception {
		super(txnData);
	}

	// #Region "AA進入點主程式"
	// ''' <summary>
	// ''' 程式進入點
	// ''' </summary>
	// ''' <returns>Response電文</returns>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Husan</modifier>
	// ''' <reason>Connie spec change for 新增交易紀錄在checkBusinessRule之前作</reason>
	// ''' <date>2011/3/24</date>
	// ''' </modify>
	// ''' </history>
	// ''' <remarks></remarks>
	@Override
	public String processRequestData() {
		boolean needUpdateFEPTXN = false;
		try {
			// 拆解並檢核財金發動的Request電文Header
			_rtnCode = this.processRequestHeader();
			// 準備交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareFEPTXN();
			}
			// '2018/01/25 Modify by Ruling for 避免SendGarbledMessage後更新FEPTXN失敗
			// '新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.insertFEPTXN();
				needUpdateFEPTXN = true;
			}
			// 檢核財金發動的Request電文BODY
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				_rtnCodeReq = this.processRequestBody();
			}
			// 組送財金Response電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}
			// add by Maxine on 2011/07/12 for 需顯示交易成功訊息於EMS
			getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc() + ";");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());

			if (NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
				getLogContext().setpCode(getFeptxn().getFeptxnPcode());
				getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("1"); // OPC
				getLogContext().setMessageParm13(getFiscOPCReq().getNoticeData());
				getLogContext().setMessageParm14(getFiscOPCReq().getNoticeId());

				getLogContext().setProgramName(ProgramName);
				// Jim, 2012/7/17, 取消NoticeID=4101、40102為法務部金融帳戶開戶查詢，EMS自動發送
				if (!"4101".equals(getFiscOPCReq().getNoticeId()) && !"4102".equals(getFiscOPCReq().getNoticeId())) {
					getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.FISCNoticeCall, logContext));
				}
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
			}

			// 送財金
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendResponseToFISCOpc();
			}
			// 根據財金Request電文傳入的NOTICEID分別處理
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				// '2018/01/25 Modify by Ruling for 先記錄3201-NOTICEID，因 NOTICEID 4101 之 fiscBusiness.FepTxn物件會被重NEW for PCODE=3100使用，所以最後不能更新FEPTXN_TXRUST
				strNoticeID3201 = getFiscOPCReq().getNoticeId();
				_rtnCode = this.doBusiness();
			}

			// 2018/01/25 Modify by Ruling for 避免SendGarbledMessage後更新FEPTXN失敗及過濾NOTICEID 4101 因fiscBusiness.FepTxn物件會被重NEW for PCODE=3100使用，所以最後不能更新FEPTXN_TXRUST
			if (needUpdateFEPTXN && !"4101".equals(strNoticeID3201)) {
				updateFeptxnTxrust();
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return StringUtils.EMPTY;
	}

	// #Region "本支AA共用之sub routine"
	// ''' <summary>
	// ''' 拆解並檢核由財金發動的Request電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Husan</modifier>
	// ''' <reason>Connie spec change </reason>
	// ''' <date>2011/3/24</date>
	// ''' </modify>
	// ''' </history>

	private FEPReturnCode processRequestHeader() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		rtnCode = getFiscBusiness().checkHeader(getFiscOPCReq(), false);
		if (rtnCode == FISCReturnCode.MessageTypeError ||
				rtnCode == FISCReturnCode.TraceNumberDuplicate ||
				rtnCode == FISCReturnCode.OriginalMessageError ||
				rtnCode == FISCReturnCode.STANError ||
				rtnCode == FISCReturnCode.SenderIdError ||
				rtnCode == FISCReturnCode.CheckBitMapError) {
			getFiscBusiness().sendGarbledMessage(getFiscOPCReq().getEj(), rtnCode, getFiscOPCReq());
			return rtnCode;
		}
		if (rtnCode != CommonReturnCode.Normal) {
			this._rtnCodeReq = rtnCode;
			return CommonReturnCode.Normal;
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 準備交易記錄檔
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getFiscBusiness().getFeptxn().setFeptxnNoticeId(getFiscOPCReq().getNoticeId());
		getFiscBusiness().getFeptxn().setFeptxnRemark(getFiscOPCReq().getNoticeData());
		rtnCode = getFiscBusiness().prepareFeptxnFromHeader();

		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	// ''' <summary>
	//// ''' 新增交易記錄檔
	//// ''' </summary>
	//// ''' <returns></returns>
	//// ''' <remarks></remarks>
	//// ''' <history>
	//// ''' <modify>
	//// ''' <modifier>HusanYin</modifier>
	//// ''' <reason>修正Const RC</reason>
	//// ''' <date>2010/11/25</date>
	//// ''' <reason>connie spec change</reason>
	//// ''' <date>2011/03/24</date>
	//// ''' </modify>
	//// ''' </history>
	private FEPReturnCode insertFEPTXN() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (_rtnCodeReq == FEPReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_OK);
		} else {
			getLogContext().setProgramName(ProgramName);
			getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCodeReq, FEPChannel.FISC, getLogContext()));
		}
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCodeReq.getValue());
		rtnCode = getFiscBusiness().insertFEPTxn();

		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setEj(getFiscOPCReq().getEj());
			getLogContext().setReturnCode(rtnCode);
			getLogContext().setProgramName(ProgramName);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(rtnCode, logContext));
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 拆解並檢核由財金發動的Request電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processRequestBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());
		try {
			// 檢核MAC
			rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), getFiscOPCReq().getMAC());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			// 2018/02/12 Modify by Ruling for 顯示通知代碼
			getLogContext().setRemark("通知代碼=" + getFiscBusiness().getFeptxn().getFeptxnNoticeId());
			logMessage(Level.INFO, getLogContext());
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 組回傳財金Response電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>HusanYin</modifier>
	// ''' <reason>connie spec change</reason>
	// ''' <date>2011/03/24</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		FEPReturnCode rtnCodeUpdate = CommonReturnCode.Normal;
		// modify 20110324
		if (_rtnCodeReq == CommonReturnCode.Normal) {
			getFeptxn().setFeptxnRepRc(NormalRC.FISC_OK);
		} else {
			getLogContext().setProgramName(ProgramName);
			getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCodeReq, FEPChannel.FISC, getLogContext()));
		}

		rtnCode = getFiscBusiness().prepareHeader("0610");
		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark("PrepareForFISC - PrepareHeader:" + rtnCode.toString());
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			// 2018/01/17 Modify by Ruling for 修正改用PrepareHeader的結果來更新FEPTXN_AA_RC
			rtnCodeUpdate = updateFEPTXN_AA_RC(rtnCode);
			if (rtnCodeUpdate != CommonReturnCode.Normal) {
				return rtnCodeUpdate;
			} else {
				return rtnCode;
			}
		}

		rtnCode = this.prepareBody();
		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark("PrepareForFISC - PrepareBody:" + rtnCode.toString());
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			// '2018/01/17 Modify by Ruling for 修正改用PrepareBody的結果來更新FEPTXN_AA_RC
			rtnCodeUpdate = updateFEPTXN_AA_RC(rtnCode);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCodeUpdate;
			} else {
				return rtnCode;
			}
		}

		rtnCode = getFiscBusiness().makeBitmap(getFiscOPCRes().getMessageType(), getFiscOPCRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			getLogContext().setRemark("PrepareForFISC - MakeBitmap:" + rtnCode.toString());
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			// 2018/01/17 Modify by Ruling for 修正改用MakeBitmap的結果來更新FEPTXN_AA_RC
			rtnCodeUpdate = updateFEPTXN_AA_RC(rtnCode);
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCodeUpdate;
			} else {
				return rtnCode;
			}
		}
		rtnCode = getFiscOPCRes().makeFISCMsg();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 組財金電文Body部份
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());
		try {
			// NOTICE_ID
			getFiscOPCRes().setNoticeId(getFiscOPCReq().getNoticeId());
			// MAC
			RefString refMac = new RefString(getFiscOPCRes().getMAC());
			rtnCode = encHelper.makeOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), refMac);
			getFiscOPCRes().setMAC(refMac.get());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareBody"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// ''' <summary>
	// '''根據財金Request電文傳入的NOTICEID分別處理
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode doBusiness() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// '2018/01/17 Modify by Ruling for 更新3201交易結果：NOTICEID 4101要先更新，因後續fiscBusiness.FepTxn物件會被重NEW for PCODE=3100使用
		if ("4101".equals(getFiscOPCReq().getNoticeId())) {
			getFiscBusiness().getFeptxn().setFeptxnTxrust(FeptxnTxrust.Successed);
			rtnCode = getFiscBusiness().updateTxData();
		}
		switch (getFiscOPCReq().getNoticeId()) {
			case "2001": // 換日處理
				rtnCode = getFiscBusiness().changeDate("");
				break;
			case "4101": // 金融帳戶開戶查詢交易
				// 2018/01/17 Modify by Ruling for 接call Function NoticeID4101 回的 ReturnCode
				rtnCode = this.noticeID4101();
				break;
			case "4103": // 金融帳戶開戶查詢異常處理狀況通知交易
				// 2018/01/17 Modify by Ruling for 接call Function NoticeID4101 回的 ReturnCode
				rtnCode = this.noticeID4103();
				break;
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''金融帳戶開戶查詢交易(NOTICE_ID=4101)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Husan Yin</modifier>
	// ''' <reason>SPEC Change for 4103抓原本4101資料</reason>
	// ''' <date>2010/12/31</date>
	// ''' </modify>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>金融開戶查詢的StoreProcedure由CRMDB改抓ODS2
	// ''' PBMR、CKMR原先在CRMDB是兩個Table，改用ODS2後是同一個Table
	// ''' 故StoreProcedure只用到GetPBMRbyIDNO</reason>
	// ''' <date>2012/04/02</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode noticeID4101() {
		int i = 0;
		String strResult1 = null;
		String strResult2 = null;
		String strResult3 = null;
		String strResult = null;
		String IDNO = null;
		String STARTDAY = null;

		// '(1)設定變數初始值
		IDNO = getFiscOPCReq().getNoticeData().substring(12, 22);
		if (!"000000".equals(getFiscOPCReq().getNoticeData().substring(22, 28))) {
			STARTDAY = CalendarUtil.rocStringToADString(StringUtils.leftPad(getFiscCon().getTxnInitiateDateAndTime().substring(22, 28), 7, "0")); // '轉為西元年
		} else {
			STARTDAY = "";
		}
		i = this.getPBMRbyIDNO(IDNO, STARTDAY);
		if (i > 0) {
			strResult1 = "Y"; // (該特定人在查詢期間內於金融機構有開戶記錄)
		} else {
			strResult1 = "N"; // (該特定人在查詢期間內於金融機構無開戶記錄)
		}
		// '(2)讀IBTID(台北國際商銀合併前銷戶檔)
		// '若查詢開始日期超過民國百年則不需要讀取 IBTID 檔
		// '2012/05/28 Modify by Ruling for 改用ODS2
		i = this.getIBTIDbyIDNO(IDNO, STARTDAY);
		if (i > 0) {
			strResult2 = "Y"; // (該特定人在查詢期間內於金融機構有開戶記錄)
		} else {
			strResult2 = "N"; // (該特定人在查詢期間內於金融機構無開戶記錄)
		}
		// '2018/01/17 Modify by Ruling for 金融開戶查詢增加保管箱業務
		// '(3)保管箱
		i = this.getBoxbyIDNO(IDNO, STARTDAY);
		if (i > 0) {
			strResult3 = "Y"; // (該特定人在查詢期間內於金融機構有開戶記錄)
		} else {
			strResult3 = "N"; // (該特定人在查詢期間內於金融機構無開戶記錄)
		}
		// '2018/01/17 Modify by Ruling for 金融開戶查詢增加保管箱業務
		// '(4)判斷查詢結果
		strNoticeID = "4102";
		if ("Y".equals(strResult1) || "Y".equals(strResult2) || "Y".equals(strResult3)) {
			strResult = "Y";
		} else {
			strResult = "N";
		}
		strNoticeData = StringUtils.join(
				getFiscOPCReq().getNoticeData().substring(0, 12),
				getFiscOPCReq().getNoticeData().substring(12, 22),
				strResult,
				getFiscOPCReq().getTxnDestinationInstituteId(),
				StringUtils.leftPad("", 10, StringUtils.SPACE));

		if ("Y".equals(strResult1) && "N".equals(strResult2) && "N".equals(strResult3)) {
			strNoticeSource = "2"; // ＢＳＰ已開戶 (BSP CIF)
		}
		if ("N".equals(strResult1) && "Y".equals(strResult2) && "N".equals(strResult3)) {
			strNoticeSource = "1"; // ＩＢＴ已銷戶(IBTID)
		}
		if ("Y".equals(strResult1) && "Y".equals(strResult2) && "N".equals(strResult3)) {
			strNoticeSource = "3"; // ＢＳＰ已開戶 + ＩＢＴ已銷戶
		}
		if ("N".equals(strResult1) && "N".equals(strResult2) && "N".equals(strResult3)) {
			strNoticeSource = "0"; // 未開戶
		}
		// '2018/01/17 Modify by Ruling for 金融開戶查詢增加保管箱業務
		if ("N".equals(strResult1) && "N".equals(strResult2) && "Y".equals(strResult3)) {
			strNoticeSource = "4"; // 保管箱
		}
		if ("N".equals(strResult1) && "Y".equals(strResult2) && "Y".equals(strResult3)) {
			strNoticeSource = "5"; // 保管箱+ ＩＢＴ已銷戶
		}
		if ("Y".equals(strResult1) && "N".equals(strResult2) && "Y".equals(strResult3)) {
			strNoticeSource = "6"; // 保管箱 + ＢＳＰ已開戶
		}
		if ("Y".equals(strResult1) && "Y".equals(strResult2) && "Y".equals(strResult3)) {
			strNoticeSource = "7"; // '保管箱 + ＢＳＰ已開戶 + ＩＢＴ已銷戶
		}
		return noticeID4101_Last();
	}

	private FEPReturnCode noticeID4101_Last() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// '(4)準備交易記錄檔：PCODE=3100之交易紀錄的初始資料(新增另一筆)
		rtnCode = prepareFEPTXN3100();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		// (5)組送財金Response電文(OC007A)
		rtnCode = prepareRequestToFISC();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		// 新增交易記錄檔：PCODE=3100之的交易紀錄
		getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());
		rtnCode = getFiscBusiness().insertFEPTxn();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 送財金
		rtnCode = getFiscBusiness().sendRequestToFISCOpc();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// 處理財金Response電文(OC008)
		rtnCode = processRespons3100();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// add by Maxine on 2011/07/12 for 需顯示交易成功訊息於EMS
		getLogContext().setRemark("fiscBusiness.FepTxn.FEPTXN_REP_RC=" + getFiscBusiness().getFeptxn().getFeptxnRepRc() + ";");
		getLogContext().setProgramName(ProgramName);
		logMessage(Level.DEBUG, getLogContext());
		if (NormalRC.FISC_OK.equals(getFiscBusiness().getFeptxn().getFeptxnRepRc())) {
			getLogContext().setpCode(getFiscBusiness().getFeptxn().getFeptxnPcode());
			getLogContext().setDesBkno(getFiscBusiness().getFeptxn().getFeptxnDesBkno());
			getLogContext().setFiscRC(NormalRC.FISC_OK);
			getLogContext().setMessageId(getFiscBusiness().getFeptxn().getFeptxnMsgid());
			getLogContext().setMessageGroup("1"); // OPC

			getLogContext().setMessageParm13(getFiscOPCReq().getNoticeData());
			getLogContext().setMessageParm14(getFiscOPCReq().getNoticeId());
			getLogContext().setProgramName(ProgramName);

			// 'Jim, 2012/7/17, 取消NoticeID=4101、40102為法務部金融帳戶開戶查詢，EMS自動發送
			if (!"4101".equals(getFiscOPCReq().getNoticeId()) && "4102".equals(getFiscOPCReq().getNoticeId())) {
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FEPReturnCode.MBankNoticeCall, getLogContext()));
			}
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''金融帳戶開戶查詢異常處理狀況通知交易(NOTICE_ID=4103)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Husan Yin</modifier>
	// ''' <reason>SPEC Change for 4103</reason>
	// ''' <date>2010/12/31</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode noticeID4103() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// Dim oFeptxn As Tables.DefFEPTXN;
		Feptxn oFeptxn = new FeptxnExt();
		try {
			if ("0005".equals(getFiscOPCReq().getNoticeData().substring(10, 14))) {
				return rtnCode;
			}
			// 查詢FEP交易明細檔
			// oFeptxn = New Tables.DefFEPTXN;
			// Feptxn oFeptxn = new Feptxn();
			oFeptxn.setFeptxnBkno(getFiscOPCReq().getNoticeData().substring(0, 3));
			oFeptxn.setFeptxnStan(getFiscOPCReq().getNoticeData().substring(3, 10));
			getLogContext().setRemark("oFeptxn.FEPTXN_STAN : " + oFeptxn.getFeptxnStan() + ", oFeptxn.FEPTXN_BKNO : " + oFeptxn.getFeptxnBkno());
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());

			// oFeptxn = DBFepTxn.GetFEPTXNByStanAndBkno(oFeptxn)
			// oFeptxn = getFeptxn().getFeptxnBkno(oFeptxn);
			oFeptxn = feptxnDao.getFEPTXNByStanAndBkno(oFeptxn.getFeptxnStan(), oFeptxn.getFeptxnBkno());
			if (oFeptxn == null || !"3100".equals(oFeptxn.getFeptxnPcode()) || !"4102".equals(oFeptxn.getFeptxnNoticeId())) {
				return FEPReturnCode.FEPTXNReadError;
			}
			// 'modify by henny for spec change 20110602
			// '(4)UpdateFEPTXN：更新交易記錄
			getFiscBusiness().getFeptxn().setFeptxnOriStan(getFiscOPCReq().getNoticeData().substring(3, 10));
			getFiscBusiness().getFeptxn().setFeptxnExcpCode(getFiscOPCReq().getNoticeData().substring(10, 14));
			getFiscBusiness().getFeptxn().setFeptxnRemark(oFeptxn.getFeptxnRemark());
			rtnCode = getFiscBusiness().updateTxData();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".noticeID4103"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''PrepareFEPTXN：PCODE=3100之交易紀錄的初始資料
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Husan Yin</modifier>
	// ''' <reason>SPEC Change for 4103</reason>
	// ''' <date>2010/12/31</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode prepareFEPTXN3100() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getFiscBusiness().setFeptxn(new FeptxnExt());
		getTxData().setEj(TxHelper.generateEj());
		getTxData().setTxChannel(FEPChannel.FEP);

		rtnCode = getFiscBusiness().prepareFeptxnOpc("3100");
		getFiscBusiness().getFeptxn().setFeptxnMsgid("310000");
		getFiscBusiness().getFeptxn().setFeptxnNoticeId(strNoticeID);
		getFiscBusiness().getFeptxn().setFeptxnRemark(strNoticeData);
		getFiscBusiness().getFeptxn().setFeptxnRsCode(strNoticeSource);
		getFiscBusiness().getFeptxn().setFeptxnOriStan(getTxData().getStan()); // '3201之STAN(fiscdata之STAN)
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''PrepareRequestToFISC：組財金 Request電文(OC007A)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareRequestToFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());
		// (a)組財金電文Head部份
		rtnCode = getFiscBusiness().prepareHeader("0600");
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// (b)組財金電文Body部份
		getFiscOPCReq().setNoticeId(strNoticeID);
		getFiscOPCReq().setNoticeData(strNoticeData);
		getLogContext().setRemark("strNoticeID= : " + strNoticeID + ";strNoticeData=" + strNoticeData);
		getLogContext().setProgramName(ProgramName);
		logMessage(Level.DEBUG, getLogContext());

		// (c)MAC
		RefString refMac = new RefString(getFiscOPCReq().getMAC());
		rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), refMac);
		getFiscOPCReq().setMAC(refMac.get());
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// (d)產生Bitmap
		rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		// (e)產生財金Flatfile電文(HEAD+Bitmap+BODY)
		rtnCode = getFiscOPCReq().makeFISCMsg();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''財金 Response電文(OC008)處理
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processRespons3100() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());
		try {
			rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
			// 若為Garble交易則發出Garbled Message
			if (rtnCode != CommonReturnCode.Normal) {
				if (rtnCode == FISCReturnCode.MessageTypeError ||
						rtnCode == FISCReturnCode.TraceNumberDuplicate ||
						rtnCode == FISCReturnCode.OriginalMessageError ||
						rtnCode == FISCReturnCode.STANError ||
						rtnCode == FISCReturnCode.SenderIdError ||
						rtnCode == FISCReturnCode.CheckBitMapError) {
					getFiscBusiness().sendGarbledMessage(getFiscOPCReq().getEj(), rtnCode, getFiscOPCReq());
				}
			}

			// 檢核NOTICE_ID
			if (rtnCode == CommonReturnCode.Normal) {
				if (!getFiscOPCReq().getNoticeId().equals(getFiscOPCRes().getNoticeId())) {
					rtnCode = FISCReturnCode.OriginalMessageDataError; // '0401(訊息之MAPPING欄位資料與原交易相關訊息之欄位資料不相符)
				}
			}

			// 檢核MAC
			getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
			if (rtnCode == CommonReturnCode.Normal) {
				rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
				_rtnCodeReq = rtnCode;
			}

			// 更新交易記錄檔
			rtnCode = this.updateFEPTXN();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRespons3100"));
			sendEMS(getLogContext());
			rtnCode = CommonReturnCode.ProgramException;
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 更新交易記錄檔
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks>處理收到財金 3100 Response 電文時更新FEPTXN</remarks>
	private FEPReturnCode updateFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
		getFiscBusiness().getFeptxn().setFeptxnPending((short) 0);
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCodeReq.getValue());
		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response); // FISC RESPONSE

		// '2018/01/17 Modify by Ruling for 更新3100交易結果
		if (_rtnCodeReq == CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
		} else {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
		}
		rtnCode = getFiscBusiness().updateTxData();
		if (_rtnCodeReq != CommonReturnCode.Normal) {
			return _rtnCodeReq;
		} else {
			return rtnCode;
		}
	}

	// ''' <summary>
	// ''' 更新交易紀錄檔(FEPTXN_AA_RC)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks>組財金 3201 Response 電文失敗時更新FEPTXN_AA_RC</remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>修改由前端傳進來的rtnCode更新FEPTXN_AA_RC</reason>
	// ''' <date>2018/01/17</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode updateFEPTXN_AA_RC(FEPReturnCode rtnCode) {
		// 'Dim rtnCode As FEPReturnCode = CommonReturnCode.Normal
		getFiscBusiness().getFeptxn().setFeptxnAaRc(rtnCode.getValue());

		// 2018/01/17 Modify by Ruling for 更新3100交易結果
		if (_rtnCodeReq == CommonReturnCode.Normal) {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
		} else {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
		}
		rtnCode = this.updateFEPTXN();
		if (rtnCode != CommonReturnCode.Normal) {
			return FEPReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}

	// ''' <summary>
	// ''' 更新交易紀錄檔(FEPTXN_TXRUST)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks>PCODE=3201 更新FEPTXN_TXRUST</remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>新增FUNCTION</reason>
	// ''' <date>2018/01/24</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode updateFeptxnTxrust() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		if (_rtnCodeReq == CommonReturnCode.Normal) {
			if (_rtnCode == CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
			} else {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
				getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
			}
		} else {
			getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCodeReq.getValue());
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
		}
		rtnCode = this.updateFEPTXN();
		if (rtnCode != CommonReturnCode.Normal) {
			return FEPReturnCode.FEPTXNUpdateError;
		}
		return rtnCode;
	}

	public int getPBMRbyIDNO(String oIDNO, String oSTARTDAY) {
		int ret = 0;
		try {
			SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
			List<Map<String, Object>> resultList = spCaller.getPbmrByIdNo(oIDNO, oSTARTDAY);
			ret = CollectionUtils.isEmpty(resultList) ? 0 : resultList.size();
			this.logContext.setRemark(StringUtils.join("GetPBMRbyIDNO IDNO:", oIDNO, " oSTARTDAY:", oSTARTDAY, " ROWCOUNT:", ret));
			this.logContext.setProgramName(ProgramName);
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception exp) {
			this.logContext.setProgramException(exp);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".getPBMRbyIDNO"));
			sendEMS(getLogContext());
		}
		return ret;
	}

	public int getIBTIDbyIDNO(String oIDNO, String oSTARTDAY) {
		int ret = 0;
		try {
			SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
			List<Map<String, Object>> resultList = spCaller.queryIbtId(oIDNO, oSTARTDAY);
			ret = CollectionUtils.isEmpty(resultList) ? 0 : resultList.size();
			this.logContext.setRemark(StringUtils.join("GetIBTIDbyIDNO IDNO:", oIDNO, " oSTARTDAY:", oSTARTDAY, " ROWCOUNT:", ret));
			this.logContext.setProgramName(ProgramName);
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception exp) {
			this.logContext.setProgramException(exp);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".getIBTIDbyIDNO"));
			sendEMS(getLogContext());
		}
		return ret;
	}

	// ''' <summary>
	// ''' 查保管箱資料庫
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>新增FUNCTION for 金融開戶查詢增加保管箱業務</reason>
	// ''' <date>2018/01/17</date>
	// ''' </modify>
	// ''' </history>
	public int getBoxbyIDNO(String oIDNO, String oSTARTDAY) {
		int ret = 0;
		try {
			SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
			List<Map<String, Object>> resultList = spCaller.getBoxByIdNo(oIDNO, oSTARTDAY);
			if (CollectionUtils.isNotEmpty(resultList)) {
				Map<String, Object> map = resultList.get(0);
				if (MapUtils.isNotEmpty(map)) {
					Object value = map.get("HAVEBOXCNT");
					if (value != null) {
						ret = (int) value;
					}
				}
			}
			this.logContext.setRemark(StringUtils.join("GetBOXbyIDNO IDNO:", oIDNO, " oSTARTDAY:", oSTARTDAY, " BOXCOUNT:", ret));
			this.logContext.setProgramName(ProgramName);
			logMessage(Level.DEBUG, this.logContext);
		} catch (Exception exp) {
			this.logContext.setProgramException(exp);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".getBoxbyIDNO"));
			sendEMS(getLogContext());
		}
		return ret;
	}
}
