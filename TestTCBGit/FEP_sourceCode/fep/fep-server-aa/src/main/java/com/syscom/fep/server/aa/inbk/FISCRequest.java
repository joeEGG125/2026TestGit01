package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.mapper.SysstatMapper;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

// ''' <summary>
// ''' 2 Way
// ''' 由財金發動的Request電文檢核有錯誤仍要回Response電文給財金
// ''' 1. 財金公司押碼基碼同步通知交易(0101)
// ''' 2. 財金公司應用系統暫停通知交易(3205)
// ''' 3. 財金公司應用系統重新啟動服務交易(3206)
// ''' 4. 財金公司CD/ATM作業狀況查詢交易(3215)
// ''' 本支負責處理電文如下
// ''' 財金公司押碼基碼同步通知交易(0101)
// ''' REQUEST ：OC003
// ''' RESPONSE：OC004
// ''' 財金公司應用系統暫停通知交易(3205)
// ''' REQUEST ：OC031
// ''' RESPONSE：OC032
// ''' 財金公司應用系統重新啟動服務交易(3206)
// ''' REQUEST ：OC033
// ''' RESPONSE：OC034
// ''' 財金公司CD/ATM作業狀況查詢交易(3215)
// ''' REQUEST ：OC067
// ''' RESPONSE：OC068
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
// ''' <date>2010/3/22</date>
// ''' </modify>
// ''' </history>
public class FISCRequest extends INBKAABase {

	// "共用變數宣告"
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private FEPReturnCode _rtnCodeReq = CommonReturnCode.Normal;// 檢核財金發動Request電文之fep rtncode(檢核錯誤仍要送Response電文給財金)

	// "建構式"
	// ''' <summary>
	// ''' AA的建構式,在這邊初始化及設定其他相關變數
	// ''' </summary>
	// ''' <param name="txnData">AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件</param>
	// ''' <remarks>
	// ''' 初始化後,AA可以透過ATMBusiness變數取得Business.ATM物件,
	// ''' ATMRequest變數取得ATMGeneral中的Request物件,ATMResponse變數取得ATMGeneral中的Response物件
	// ''' FEPTxn變數取得本筆交易的DefFEPTxn物件(用來存放欄位值),DBFepTxn變數取得DBFepTxn物件(用來進行資料處理動作)
	// ''' </remarks>
	public FISCRequest(FISCData txnData) throws Exception {
		super(txnData);
	}

	// "AA進入點主程式"
	// ''' <summary>
	// ''' 程式進入點
	// ''' </summary>
	// ''' <returns>Response電文</returns>
	// ''' <remarks></remarks>
	@Override
	public String processRequestData() {
		try {
			// 1.拆解並檢核財金發動的Request電文Header
			_rtnCode = this.processRequestHeader();

			// 2.準備交易記錄檔＆新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareAndinsertFEPTXN();
			}

			// 3.檢核財金發動的Request電文BODY
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				_rtnCodeReq = this.processRequestBody();
			}

			// 4.DoBusiness
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				_rtnCodeReq = this.doBusiness();
			}

			// 5.組送財金Response電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareForFISC();
			}

			// 6.送財金
			if (_rtnCodeReq != CommonReturnCode.Normal) {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCodeReq.getValue());
			} else {
				getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
			}

			// add by Maxine on 2011/07/12 for 需顯示交易成功訊息於EMS
			getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + getFeptxn().getFeptxnRepRc() + ";FepTxn.FEPTXN_PCODE=" + getFeptxn().getFeptxnPcode() + ";");
			getLogContext().setProgramName(ProgramName);
			logMessage(Level.DEBUG, getLogContext());
			if ("0101".equals(getFeptxn().getFeptxnPcode()) && NormalRC.FISC_OK.equals(getFeptxn().getFeptxnRepRc())) {
				// 20110621 需顯示成功訊息於EMS, 為此LogData新增欄位如下
				getLogContext().setpCode(getFeptxn().getFeptxnPcode());
				getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
				getLogContext().setFiscRC(NormalRC.FISC_OK);
				getLogContext().setMessageGroup("1"); // OPC
				getLogContext().setProgramName(ProgramName);
				getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.FISCKeySyncCall, getLogContext()));
				getLogContext().setProgramName(ProgramName);
				logMessage(Level.DEBUG, getLogContext());
			}

			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendResponseToFISCOpc();
			}

		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestData");
			sendEMS(getLogContext());
		} finally {
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Response);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode));
			// logHelper.debug(null, getLogContext());
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}

	// 1.拆解並檢核財金發動的Request電文Header"
	// ''' <summary>
	// ''' 拆解並檢核財金發動的Request電文Header
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
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
		// 除了以上錯誤外其它錯誤仍要組回應電文給財金
		if (rtnCode != CommonReturnCode.Normal) {
			_rtnCodeReq = rtnCode;
			rtnCode = CommonReturnCode.Normal;
		}
		return rtnCode;
	}

	// 2.準備交易記錄檔＆新增交易記錄檔"
	// ''' <summary>
	// ''' 準備交易記錄檔＆新增交易記錄檔
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareAndinsertFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		try {
			rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			} else {
				if (StringUtils.isNotBlank(getFiscOPCReq().getAPID())) {
					getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getAPID());
				}
				if (StringUtils.isNotBlank(getFiscOPCReq().getKEYID())) {
					getFiscBusiness().getFeptxn().setFeptxnRemark(getFiscOPCReq().getKEYID());

					switch (getFiscOPCReq().getKEYID()) { // for UI_019020 查詢時能顯示KEYID+英文
						case "04":
							getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getKEYID() + "OPC");
							break;
						case "05":
							getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getKEYID() + "ATM");
							break;
						case "06":
							getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getKEYID() + "RM");
							break;
						case "11":
							getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getKEYID() + "PPK");
							break;
						case "12":
							getFiscBusiness().getFeptxn().setFeptxnApid(getFiscOPCReq().getKEYID() + "PPK");
							break;
					}
				}
				rtnCode = getFiscBusiness().insertFEPTxn();
				if (rtnCode != CommonReturnCode.Normal) {
					return rtnCode;
				} else {
					return _rtnCode;
				}
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareAndinsertFEPTXN");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	/**
	 * 3.檢核財金發動的Request電文BODY"
	 * ''' <summary>
	 * ''' 檢核財金發動的Request電文BODY
	 * ''' </summary>
	 * ''' <returns></returns>
	 * ''' <remarks></remarks>
	 * ''' <modify>
	 * ''' <modifier>HusanYin</modifier>
	 * ''' <reason>修正Const RC</reason>
	 * ''' <date>2010/11/25</date>
	 * ''' </modify>
	 */
	private FEPReturnCode processRequestBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

		try {
			switch (getFiscOPCReq().getProcessingCode()) {
				case "0101":
					rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), getFiscOPCReq().getMAC());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
					break;
				case "3205":
					// 檢核APID
					rtnCode = getFiscBusiness().checkAPId(getTxData().getMessageID(), getFiscOPCReq().getAPID());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}

					// 根據財金電文之APID，檢核SYSSTAT之AP狀態
					switch (getFiscOPCReq().getAPID()) {
						case "1000":
							if ("1,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1000()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1100":
							if ("1,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1100()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1200":
							if ("1,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1200()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1300":
							if ("1,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1300()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1400":
							if ("1,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1400()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2000":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2000())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2200":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2200())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2500":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2500())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2510":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2510())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2520":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2520())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2530":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2530())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2540":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2540())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2550":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2550())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2560":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct2560())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "7100":
							if (!"1".equals(SysStatus.getPropertyValue().getSysstatAoct7100())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
					}
					// MAC
					rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), getFiscOPCReq().getMAC());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
					break;
				case "3206":
					// 檢核APID
					rtnCode = getFiscBusiness().checkAPId(getTxData().getMessageID(), getFiscOPCReq().getAPID());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}

					// 根據財金電文之APID，檢核SYSSTAT之AP狀態
					switch (getFiscOPCReq().getAPID()) {
						case "1000":
							if ("2,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1000()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1100":
							if ("2,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1100()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1200":
							if ("2,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1200()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1300":
							if ("2,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1300()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "1400":
							if ("2,A,B".indexOf(SysStatus.getPropertyValue().getSysstatAoct1400()) < 0) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2000":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2000())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2200":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2200())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2500":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2500())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2510":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2510())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2520":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2520())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2530":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2530())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2540":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2540())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2550":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2550())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "2560":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct2560())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
						case "7100":
							if (!"2".equals(SysStatus.getPropertyValue().getSysstatAoct7100())) {
								return FISCReturnCode.StopServiceToFISCNotAllowed;
							}
							break;
					}

					// MAC
					rtnCode = encHelper.checkOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), getFiscOPCReq().getMAC());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
					break;
				case "3215":
					// 檢核SYSSTAT狀態
					if ("2".equals(SysStatus.getPropertyValue().getSysstatMbact2000())) {
						return FISCReturnCode.ReceiverBankServiceStop;
					}
					if (!NormalRC.FISC_REQ_RC.equals(getFiscOPCReq().getResponseCode())) {
						return FISCReturnCode.InvalidResponseCode; // 無效之回應代碼(INVALID RESPONSE CODE)
					}
					break;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".processRequestBody");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 4.DoBusiness"
	// ''' <summary>
	// ''' DoBusiness
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>0101組Response電文的BODY從PrepareBody()搬移至DoBusiness()
	// ''' 若key不同步時要先將不同步的Key值塞回相對應的Response電文中，再將3301的RC塞回Response電文的RC</reason>
	// ''' <date>2011/05/20</date>
	// ''' </modify>
	// ''' </history>

	private FEPReturnCode doBusiness() {
		SysstatMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean(SysstatMapper.class);
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		// modify 新增DBSYSSTAT的參數 20110412 by Husan
		try {
			switch (getFiscOPCReq().getProcessingCode()) {
				case "0101":
					// KEY ID=02 比對財金 CD/ATM MAC KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncAtm()) && !SysStatus.getPropertyValue().getSysstatFcdsync().equals(getFiscOPCReq().getSyncAtm())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncAtm(SysStatus.getPropertyValue().getSysstatTcdsync());
					}
					// KEY ID=03 比對財金 RMT MAC KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncRm()) && !SysStatus.getPropertyValue().getSysstatFrmsync().equals(getFiscOPCReq().getSyncRm())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncRm(SysStatus.getPropertyValue().getSysstatTrmsync());
					}
					// KEY ID=11 比對財金 CD/ATM PP KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncPpkey()) && !SysStatus.getPropertyValue().getSysstatFppsync().equals(getFiscOPCReq().getSyncPpkey())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncPpkey(SysStatus.getPropertyValue().getSysstatTppsync());
					}
					// KEY ID=12 比對財金 CD/ATM 3-DES PP KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncPpkey_3des()) && !SysStatus.getPropertyValue().getSysstatF3dessync().equals(getFiscOPCReq().getSyncPpkey_3des())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncPpkey_3des(SysStatus.getPropertyValue().getSysstatT3dessync());
					}
					// KEY ID=05 比對財金 CD/ATM 3-DES MAC KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncAtm_3des()) && !SysStatus.getPropertyValue().getSysstatFcdsync().equals(getFiscOPCReq().getSyncAtm_3des())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncAtm_3des(SysStatus.getPropertyValue().getSysstatTcdsync());
					}
					// KEY ID=06 比對財金 RMT 3-DES MAC KEY Sync值與系統Sync值不符
					if (!"".equals(getFiscOPCReq().getSyncRm_3des()) && !SysStatus.getPropertyValue().getSysstatFrmsync().equals(getFiscOPCReq().getSyncRm_3des())) {
						rtnCode = FISCReturnCode.KeySyncNotMatch;
						getFiscOPCRes().setSyncRm_3des(SysStatus.getPropertyValue().getSysstatTrmsync());
					}
					break;
				case "3205":
					// 依據APID，更新SYSSTAT
					switch (getFiscOPCReq().getAPID()) {
						case "1000":
							SysStatus.getPropertyValue().setSysstatAoct1000("2");
							break;
						case "1100":
							SysStatus.getPropertyValue().setSysstatAoct1100("2");
							break;
						case "1200":
							SysStatus.getPropertyValue().setSysstatAoct1200("2");
							break;
						case "1300":
							SysStatus.getPropertyValue().setSysstatAoct1300("2");
							break;
						case "1400":
							SysStatus.getPropertyValue().setSysstatAoct1400("2");
							break;
						case "2000":
							SysStatus.getPropertyValue().setSysstatAoct2000("2");
							break;
						case "2200":
							SysStatus.getPropertyValue().setSysstatAoct2200("2");
							break;
						case "2500":
							SysStatus.getPropertyValue().setSysstatAoct2500("2");
							break;
						case "2510":
							SysStatus.getPropertyValue().setSysstatAoct2510("2");
							break;
						case "2520":
							SysStatus.getPropertyValue().setSysstatAoct2520("2");
							break;
						case "2530":
							SysStatus.getPropertyValue().setSysstatAoct2530("2");
							break;
						case "2540":
							SysStatus.getPropertyValue().setSysstatAoct2540("2");
							break;
						case "2550":
							SysStatus.getPropertyValue().setSysstatAoct2550("2");
							break;
						case "2560":
							SysStatus.getPropertyValue().setSysstatAoct2560("2");
							break;
						case "7100":
							SysStatus.getPropertyValue().setSysstatAoct7100("2");
							break;
					}
					if (dbSYSSTAT.updateByPrimaryKey(SysStatus.getPropertyValue()) <= 0) {
						rtnCode = IOReturnCode.SYSSTATUpdateError;
					}
					break;
				case "3206":
					// 依據APID，更新 SYSSTAT
					switch (getFiscOPCReq().getAPID()) {
						case "1000":
							SysStatus.getPropertyValue().setSysstatAoct1000("1");
							break;
						case "1100":
							SysStatus.getPropertyValue().setSysstatAoct1100("1");
							break;
						case "1200":
							SysStatus.getPropertyValue().setSysstatAoct1200("1");
							break;
						case "1300":
							SysStatus.getPropertyValue().setSysstatAoct1300("1");
							break;
						case "1400":
							SysStatus.getPropertyValue().setSysstatAoct1400("1");
							break;
						case "2000":
							SysStatus.getPropertyValue().setSysstatAoct2000("1");
							break;
						case "2200":
							SysStatus.getPropertyValue().setSysstatAoct2200("1");
							break;
						case "2500":
							SysStatus.getPropertyValue().setSysstatAoct2500("1");
							break;
						case "2510":
							SysStatus.getPropertyValue().setSysstatAoct2510("1");
							break;
						case "2520":
							SysStatus.getPropertyValue().setSysstatAoct2520("1");
							break;
						case "2530":
							SysStatus.getPropertyValue().setSysstatAoct2530("1");
							break;
						case "2540":
							SysStatus.getPropertyValue().setSysstatAoct2540("1");
							break;
						case "2550":
							SysStatus.getPropertyValue().setSysstatAoct2550("1");
							break;
						case "2560":
							SysStatus.getPropertyValue().setSysstatAoct2560("1");
							break;
						case "7100":
							SysStatus.getPropertyValue().setSysstatAoct7100("1");
							break;
					}
					if (dbSYSSTAT.updateByPrimaryKey(SysStatus.getPropertyValue()) <= 0) {
						rtnCode = IOReturnCode.SYSSTATUpdateError;
					}
					break;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".doBusiness"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 5.組送財金Response電文"
	// ''' <summary>
	// ''' 組送財金Response電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <modify>
	// ''' <modifier>HusanYin</modifier>
	// ''' <reason>修正Const RC</reason>
	// ''' <date>2010/11/25</date>
	// ''' </modify>

	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

		if (_rtnCodeReq != CommonReturnCode.Normal) {
			// -RESPONSE
			getLogContext().setProgramName(ProgramName);
			getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCodeReq, FEPChannel.FISC, getLogContext()));
		} else {
			// +RESPONSE
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_OK);
		}

		switch (getFiscOPCReq().getProcessingCode()) {
			case "0101":
				getFiscOPCRes().setMessageType("0810");
				break;
			case "3205":
			case "3206":
			case "3215":
				getFiscOPCRes().setMessageType("0610");
				break;
		}
		rtnCode = getFiscBusiness().prepareHeader(getFiscOPCRes().getMessageType());
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		rtnCode = this.prepareBody();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		rtnCode = getFiscBusiness().makeBitmap(getFiscOPCRes().getMessageType(), getFiscOPCRes().getProcessingCode(), MessageFlow.Response);
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
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
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		try {
			switch (getFiscOPCReq().getProcessingCode()) {
				case "0101":
					// MAC
					RefString refMac = new RefString(getFiscOPCRes().getMAC());
					rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCRes().getMessageType(), refMac);
					getFiscOPCRes().setMAC(refMac.get());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
					break;
				case "3205":
				case "3206":
					// APID
					getFiscOPCRes().setAPID(getFiscOPCReq().getAPID());
					// MAC
					refMac = new RefString(getFiscOPCRes().getMAC());
					rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCRes().getMessageType(), refMac);
					getFiscOPCRes().setMAC(refMac.get());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
					break;
				case "3215":
					getFiscOPCRes().setAtmStatus(StringUtil.convertFromAnyBaseString(String.valueOf((getFiscOPCReq().getAtmStatus().length() + 2)),10,16,4) +  StringUtil.toHex(getFiscOPCReq().getAtmStatus()));
					break;
			}
			return rtnCode;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareBody");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}
}
