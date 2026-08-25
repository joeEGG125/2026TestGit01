package com.syscom.fep.server.aa.inbk;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.SysstatMapper;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;

// ''' <summary>
// ''' 3 Way
// ''' 由財金發動的Request電文檢核有錯誤仍要回Response電文給財金
// ''' 1. 財金公司變更押碼基碼通知交易：財金公司通知參加單位更改雙方之約定基碼
// ''' （包括：OPC MAC Key、CD/ATM MAC Key、Remittance MAC-Key 及 PIN Protection Key）
// ''' 2. 財金公司變更TRIPLE-DES押碼基碼通知交易：財金公司通知參加單位更改雙方之約定基碼
// ''' (包括：OPC 3-DES MAC Key、CD/ATM 3-DES MAC Key、Remittance 3-DES MAC-Key 及 3-DES PIN Protection Key )
// ''' 本支負責處理電文如下
// ''' 財金公司變更押碼基碼通知交易
// ''' REQUEST ：OC024
// ''' RESPONSE：OC025
// ''' CONFIRM ：OC026
// ''' 財金公司變更TRIPLE-DES押碼基碼通知交易
// ''' REQUEST ：OC069
// ''' RESPONSE：OC070
// ''' CONFIRM ：OC071
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
// ''' <date>2010/3/23</date>
// ''' </modify>
// ''' </history>
public class FISCChangeKey extends INBKAABase {
	// "共用變數宣告"
	private FEPReturnCode _rtnCodeReq = CommonReturnCode.Normal; // 記錄財金發動Request電文檢核錯誤之fep rtncode(檢查錯誤仍要送Response電文給財金)
	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private String strEKN1 = "";
	private String strEKN2 = "";
	private String strFROMSYNC = "";
	private String strTOSYNC = "";

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
	public FISCChangeKey(FISCData txnData) throws Exception {
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
		boolean needUpdateSYSSTAT = false;

		try {
			// 1.拆解並檢核財金發動的Request電文Header
			_rtnCode = this.processRequestHeader();

			// 2.準備交易記錄檔＆新增交易記錄檔
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.prepareAndInsertFEPTXN();
			}

			// 3.檢核財金發動的Request電文BODY
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				_rtnCodeReq = this.processRequestBody();
			}

			// 4.依據ReqOPC.KEYID更新SYSSTAT檔案中KEY的狀態為換KEY中
			if (_rtnCode == CommonReturnCode.Normal && _rtnCodeReq == CommonReturnCode.Normal) {
				// 2014/08/25 Modify by Ruling 避免重複寄Mail
				_rtnCode = this.updateSysstat(true, false);
				needUpdateSYSSTAT = true;
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

			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = getFiscBusiness().sendResponseToFISCOpc();
			}

			// 7.處理財金Confirm 電文
			if (_rtnCode == CommonReturnCode.Normal) {
				_rtnCode = this.processConfirm();
			}

			// 8.更新交易記錄檔
			_rtnCode = this.updateFeptxn();
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			if (needUpdateSYSSTAT) {
				// 2014/08/25 Modify by Ruling 避免重複寄Mail
				this.updateSysstat(false, true);
			}
			getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getLogContext().setMessage(getFiscOPCRes().getFISCMessage());
			getLogContext().setProgramName(this.aaName);
			getLogContext().setMessageFlowType(MessageFlow.Confirmation);
			getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(_rtnCode, getLogContext()));
			logMessage(Level.DEBUG, getLogContext());
		}
		return StringUtils.EMPTY;
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
			return this._rtnCode;
		}

		// 除了以上錯誤外其它錯誤仍要組回應電文給財金
		if (rtnCode != CommonReturnCode.Normal) {
			this._rtnCodeReq = rtnCode;
			rtnCode = CommonReturnCode.Normal;
		}
		return rtnCode;
	}

	// 2.準備交易記錄檔＆新增交易記錄檔"
	// ''' <summary>
	// ''' 拆解並檢核財金發動的Request電文Header
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareAndInsertFEPTXN() {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			rtnCode = getFiscBusiness().prepareFeptxnFromHeader();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			} else {
				if (StringUtils.isNotBlank(getFiscOPCReq().getKEYID())) {
					getFiscBusiness().getFeptxn().setFeptxnRemark(getFiscOPCReq().getKEYID());

					switch (getFiscOPCReq().getKEYID()) {
						case "04":
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "OPC"));
							break;
						case "05":
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "ATM"));
							break;
						case "06":
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "RM"));
							break;
						case "11":
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "PPK"));
							break;
						case "12":
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "PPK"));
							break;
						case "20":
							//2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
							getFiscBusiness().getFeptxn().setFeptxnApid(StringUtils.join(getFiscOPCReq().getKEYID(), "KBP"));
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
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".prepareAndInsertFEPTXN"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 3.檢核財金發動的Request電文BODY"
	//
	// ''' <summary>
	// ''' 檢核財金發動的Request電文BODY
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processRequestBody() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());
		String[] desReturnMac = new String[] { StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY };

		try {
			// 依不同的MessageID檢核財金電文的keyID
			rtnCode = getFiscBusiness().checkKeyID(getTxData().getMessageID(), getFiscOPCReq().getKEYID());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			// 依財金電文的keyID檢核系統狀態
			rtnCode = checkKeyIDStatus(getFiscOPCReq().getKEYID());
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}

			//產生亂碼化隨機數( ENCRYPTED RANDOM NUMBER )
			//2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
			switch(getFiscOPCReq().getProcessingCode()){
				case "0103":
					rtnCode = encHelper.changeOpcCdKey(
							getFiscOPCReq().getProcessingCode(),
							getFiscOPCReq().getMessageType(),
							getFiscOPCReq().getKEYID(),
							getFiscOPCReq().getNewKey(),
							getFiscOPCReq().getRandomNum(),
							desReturnMac);
					break;
				case "0105":
					rtnCode = encHelper.changeOpcCdKey(
							getFiscOPCReq().getProcessingCode(),
							getFiscOPCReq().getMessageType(),
							getFiscOPCReq().getKEYID(),
							getFiscOPCReq().getNewKey_3des(),
							getFiscOPCReq().getRandomNum(),
							desReturnMac);
					break;
				case "0107":
					rtnCode = encHelper.changeOPCKBPKKey(
							getFiscOPCReq().getProcessingCode(),
							getFiscOPCReq().getMessageType(),
							getFiscOPCReq().getKEYID(),
							getFiscOPCReq().getNewKeyKeyBlock(),
							desReturnMac);
					break;
			}

			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode; // CHANGE KEY CALL 訊息內之RAN-DOM NUMBER 錯誤
			}

			switch(getFiscOPCReq().getProcessingCode()){
				case "0103":
				case "0105":
					strEKN1 = desReturnMac[0];
					strEKN2 = desReturnMac[1];
					strFROMSYNC = desReturnMac[2];
					strTOSYNC = desReturnMac[3];
					break;
				case "0107":
					//Key Check Value
					strEKN1 = desReturnMac[0];
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

	// ''' <summary>
	// ''' 檢核財金電文APDATA各項KEYID的系統狀態
	// ''' </summary>
	// ''' <remarks></remarks>
	private FEPReturnCode checkKeyIDStatus(String strKeyID) {
		try {
			// 2014/03/05 Modify by Ruling for 修改PPKey換key失敗的問題
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			switch (strKeyID) {
				case "01":
				case "04":
					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatOpckeyst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
				case "02":
				case "05":
					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatAtmkeyst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
				case "03":
				case "06":

					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatRmkeyst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
				case "11":
					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatPpkeyst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
				//2024/05/17 確認欄位為 Sysstat3enckeyst
				case "12":
					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstat3enckeyst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
				case "20":
					//2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整：換key前狀態要為正常
					if (DbHelper.toBoolean(SysStatus.getPropertyValue().getSysstatKeyblockst())) {
						return FISCReturnCode.ChangeKeyCallIsProcessing;
					}
					break;
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			return FISCReturnCode.ChangeKeyCallIsProcessing;
		}
	}

	// 4.依據ReqOPC.KEYID更新SYSSTAT檔案中KEY的狀態為換KEY中"
	// ''' <summary>
	// ''' 依據ReqOPC.KEYID更新SYSSTAT檔案中KEY的狀態為換KEY中
	// ''' </summary>
	// ''' <param name="stat">TRUE:換KEY中 FALSE:可換KEY</param>
	// ''' <param name="sendMail">TRUE:寄Mail FALSE:不寄Mail(FISCKeyChangeCall)</param>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <history>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>修改PPKey換key失敗的問題</reason>
	// ''' <date>2014/03/05</date>
	// ''' </modify>
	// ''' </history>
	private FEPReturnCode updateSysstat(boolean stat, boolean sendMail) {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		SysstatExtMapper sysstatMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
		try {
			Sysstat sysstat = new Sysstat();
			sysstat.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			// Sysstat sysstat = sysstatMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno());
			// if (sysstat == null) {
			// 	return IOReturnCode.SYSSTATNotFound;
			// }
			switch (getFiscOPCReq().getKEYID()) {
				case "01":
				case "04":
					// 'OPC MAC(3-DES MAC) KEY
					sysstat.setSysstatOpckeyst(DbHelper.toShort(stat));
					break;
				case "02":
				case "05":
					// CD/ATM MAC(3-DES MAC) KEY
					sysstat.setSysstatAtmkeyst(DbHelper.toShort(stat));
					break;
				case "03":
				case "06":
					// 'RM MAC(3-DES) KEY
					sysstat.setSysstatRmkeyst(DbHelper.toShort(stat));
					break;
				case "11":
					// PP KEY
					sysstat.setSysstatPpkeyst(DbHelper.toShort(stat));
					break;
				//2024/05/17 確認欄位為 Sysstat3enckeyst
				case "12":
					// 3-DES PP KEY
					sysstat.setSysstat3enckeyst(DbHelper.toShort(stat));
					break;
				case "20":
					// 2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
					//sysstat.setSysstat3enckeyst(DbHelper.toShort(stat));
					sysstat.setSysstatKeyblockst(DbHelper.toShort(stat));
					break;
			}
			// 2025-4-27 Modify by Ashiang 只更新需要更新的欄位
			//if (sysstatMapper.updateByPrimaryKey(sysstat) <= 0) {
			if (sysstatMapper.updateKeySyncForChangeKey(sysstat) <= 0) {
				return IOReturnCode.SYSSTATUpdateError;
			} else {
				if (sendMail) {
					getLogContext().setpCode(getFeptxn().getFeptxnPcode());
					getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
					getLogContext().setFiscRC(NormalRC.FISC_OK);
					getLogContext().setMessageGroup("1"); // OPC
					switch (getFiscOPCReq().getKEYID()) {
						case "01":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(OPC MAC KEY)");
							break;
						case "02":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(CD/ATM MAC KEY)");
							break;
						case "03":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(RM MAC KEY)");
							break;
						case "04":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(OPC 3-DES MAC KEY)");
							break;
						case "05":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(CD/ATM 3-DES MAC KEY)");
							break;
						case "06":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(RM 3-DES MAC KEY)");
							break;
						case "11":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(PP KEY)");
							break;
						case "12":
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(3-DES PPKEY)");
							break;
						case "20":
							//2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
							getLogContext().setMessageParm13(getFiscOPCReq().getKEYID() + "(Key Block PP Key)");
							break;
					}
					getLogContext().setProgramName(ProgramName);
					/* 24-05-23 依Sarah,FISCReturnCode改用FISCKeyChangeCall */
					getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.FISCKeyChangeCall, getLogContext()));
					logMessage(Level.DEBUG, getLogContext());
				}
				return rtnCode;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".updateSysstat");
			sendEMS(getLogContext());
			return IOReturnCode.SYSSTATUpdateError;
		}
	}

	// 5.組送財金Response電文"
	// ''' <summary>
	// ''' 組送財金Response電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareForFISC() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

		if (_rtnCodeReq != CommonReturnCode.Normal) {
			getLogContext().setProgramName(ProgramName);
			getFiscBusiness().getFeptxn().setFeptxnRepRc(TxHelper.getRCFromErrorCode(_rtnCodeReq, FEPChannel.FISC, getLogContext()));
		} else {
			getFiscBusiness().getFeptxn().setFeptxnRepRc(NormalRC.FISC_OK);
		}

		rtnCode = getFiscBusiness().prepareHeader("0810");
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		getFiscOPCRes().setSyncCheckItem("00000000");

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
	// ''' 組回傳財金Response電文-Body部份(+Request)
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode prepareBody() {
		try {
			// 此交易有分POSITIVE / NEGTIVE RESPONSE
			switch (getTxData().getMessageID()) {
				case "010300":
					// POSITIVE RESPONSE
					if (_rtnCodeReq == CommonReturnCode.Normal) {
						getFiscOPCRes().setKEYID(getFiscOPCReq().getKEYID());
						getFiscOPCRes().setRandomNum(strEKN1);
					}
					break;
				case "010500":
					// POSITIVE RESPONSE
					if (_rtnCodeReq == CommonReturnCode.Normal) {
						getFiscOPCRes().setKEYID(getFiscOPCReq().getKEYID());
						getFiscOPCRes().setRandomNum(strEKN1);
					}
					break;
				case "010700":
					// 2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
					if (_rtnCodeReq == CommonReturnCode.Normal) {
						getFiscOPCRes().setKEYID(getFiscOPCReq().getKEYID());
						getFiscOPCRes().setNewKeyCheckValue(strEKN1);
					}
					break;
			}
			return CommonReturnCode.Normal;
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(ProgramName + ".prepareBody");
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		}
	}

	// 7.處理財金Confirm 電文"
	// ''' <summary>
	// ''' 處理財金Confirm 電文
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	private FEPReturnCode processConfirm() {
		FEPReturnCode rtnCode = CommonReturnCode.Normal;
		ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn());

		// 財金Confirm電文沒逾時更新FEPTXN TIMEOUT狀態
		if (StringUtils.isNotBlank(getFiscOPCCon().getFISCMessage())) {
			getFiscBusiness().getFeptxn().setFeptxnFiscTimeout(DbHelper.toShort(false));
			getFiscBusiness().getFeptxn().setFeptxnPending((short) 2);
			getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
		}
		rtnCode = getFiscBusiness().checkHeader(getFiscOPCCon(), false);
		if (rtnCode == FISCReturnCode.MessageTypeError ||
				rtnCode == FISCReturnCode.TraceNumberDuplicate ||
				rtnCode == FISCReturnCode.OriginalMessageError ||
				rtnCode == FISCReturnCode.STANError ||
				rtnCode == FISCReturnCode.SenderIdError ||
				rtnCode == FISCReturnCode.CheckBitMapError) {
			getFiscBusiness().sendGarbledMessage(getFiscOPCCon().getEj(), rtnCode, getFiscOPCCon());
		}
		// 除了以上錯誤外其它錯誤仍要組回應電文給財金
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		}

		switch (getTxData().getMessageID()){
			case "010300":
			case "010500":
				// 檢核RANDOM NUMBER
				if (!strEKN2.equals(getFiscOPCCon().getRandomNum())) {
					return FEPReturnCode.RandomNumberError; // 3303:CHANGE KEY CALL 訊息內之 RAN-DOM NUMBER 錯誤
				} else {
					// 檢核財金電文ApData的MAC
					rtnCode = encHelper.checkOpcCdKey(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), getFiscOPCReq().getKEYID());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
				break;
			case "010700":
				//檢核Key Check Value
				if (!strEKN1.equals(getFiscOPCCon().getNewKeyCheckValue())) {
					return FEPReturnCode.RandomNumberError; // 3303:CHANGE KEY CALL 訊息內之 RAN-DOM NUMBER 錯誤
				}else{
					//CALL FN103使 KEY 生效
					rtnCode = encHelper.checkOpcCdKey(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), getFiscOPCReq().getKEYID());
					if (rtnCode != CommonReturnCode.Normal) {
						return rtnCode;
					}
				}
				break;
		}

		//2023/04/20 Modify by Ruling for 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整：0107無KEYSYNC不更新
		if (NormalRC.FISC_OK.equals(getFiscOPCCon().getResponseCode())
				&& ("010300".equals(getTxData().getMessageID())||"010500".equals(getTxData().getMessageID()))) {
			rtnCode = this.doBusiness();
			if (rtnCode != CommonReturnCode.Normal) {
				return rtnCode;
			}
		}
		return rtnCode;
	}

	// ''' <summary>
	// '''依據ReqOPC.KEYID更新SYSSTAT檔案中KEY的狀態改為日終 House Keeping
	// ''' </summary>
	// ''' <returns></returns>
	// ''' <remarks></remarks>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>修改PPKey換key失敗的問題</reason>
	// ''' <date>2014/03/05</date>
	// ''' </modify>
	// ''' <modify>
	// ''' <modifier>Ruling</modifier>
	// ''' <reason>1.增加LOG詳細紀錄更新前後 2.換完KEY後Reload SYSSTAT</reason>
	// ''' <date>2014/08/25</date>
	// ''' </modify>
	private FEPReturnCode doBusiness() {
		SysstatExtMapper sysstatMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
		String strMsg = "";
		String msgBefore = "";
		String msgAfter = "";

		// 'modify 新增DBSYSSTAT的參數 20110412 by Husan
		try {
			FEPCache.reloadCache(CacheItem.SYSSTAT);
			//Sysstat sysstate = sysstatMapper.selectByPrimaryKey(SysStatus.getPropertyValue().getSysstatHbkno());
			Sysstat sysstate = new Sysstat();
			sysstate.setSysstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
			// if (sysstate == null) {
			// 	return IOReturnCode.SYSSTATNotFound;
			// }
			switch (getFiscOPCReq().getKEYID()) {
				case "01":
				case "04": // OPC MAC(3-DES MAC) KEY
					sysstate.setSysstatOpckeyst(DbHelper.toShort(false)); // (日終House Keeping)
					sysstate.setSysstatFopcsync(strFROMSYNC);
					sysstate.setSysstatTopcsync(strTOSYNC);
					msgBefore = " 更新前SYSSTAT_OPCKEYST=" + SysStatus.getPropertyValue().getSysstatOpckeyst() + ",SYSSTAT_FOPCSYNC=" + SysStatus.getPropertyValue().getSysstatFopcsync()
							+ ",SYSSTAT_TOPCSYNC=" + SysStatus.getPropertyValue().getSysstatTopcsync();
					msgAfter = " 更新後SYSSTAT_OPCKEYST=False,SYSSTAT_FOPCSYNC=" + strFROMSYNC + ",SYSSTAT_TOPCSYNC=" + strTOSYNC;
					break;
				case "02":
				case "05": // CD/ATM MAC(3-DES MAC) KEY
					sysstate.setSysstatAtmkeyst(DbHelper.toShort(false));
					sysstate.setSysstatFcdsync(strFROMSYNC);
					sysstate.setSysstatTcdsync(strTOSYNC);
					msgBefore = " 更新前SYSSTAT_ATMKEYST=" + SysStatus.getPropertyValue().getSysstatAtmkeyst() + ",SYSSTAT_FCDSYNC=" + SysStatus.getPropertyValue().getSysstatFcdsync()
							+ ",SYSSTAT_TCDSYNC=" + SysStatus.getPropertyValue().getSysstatTcdsync();
					msgAfter = " 更新後SYSSTAT_ATMKEYST=False,SYSSTAT_FCDSYNC=" + strFROMSYNC + ",SYSSTAT_TCDSYNC=" + strTOSYNC;
					break;
				case "03":
				case "06": // RM MAC(3-DES MAC) KEY
					sysstate.setSysstatRmkeyst(DbHelper.toShort(false));
					sysstate.setSysstatFrmsync(strFROMSYNC);
					sysstate.setSysstatTrmsync(strTOSYNC);
					msgBefore = " 更新前SYSSTAT_RMKEYST=" + SysStatus.getPropertyValue().getSysstatRmkeyst() + ",SYSSTAT_FRMSYNC=" + SysStatus.getPropertyValue().getSysstatFrmsync() + ",SYSSTAT_TRMSYNC="
							+ SysStatus.getPropertyValue().getSysstatTrmsync();
					msgAfter = " 更新後SYSSTAT_RMKEYST=False,SYSSTAT_FRMSYNC=" + strFROMSYNC + ",SYSSTAT_TRMSYNC=" + strTOSYNC;
					break;
				case "11": // PP KEY
					sysstate.setSysstatPpkeyst(DbHelper.toShort(false));
					sysstate.setSysstatFppsync(strFROMSYNC);
					sysstate.setSysstatTppsync(strTOSYNC);
					msgBefore = " 更新前SYSSTAT_PPKEYST=" + SysStatus.getPropertyValue().getSysstatPpkeyst() + ",SYSSTAT_FPPSYNC=" + SysStatus.getPropertyValue().getSysstatFppsync() + ",SYSSTAT_TPPSYNC="
							+ SysStatus.getPropertyValue().getSysstatTppsync();
					msgAfter = " 更新後SYSSTAT_PPKEYST=False,SYSSTAT_FPPSYNC=" + strFROMSYNC + ",SYSSTAT_TPPSYNC=" + strTOSYNC;
					break;
				case "12": // 3-DES PP KEY
					//2024/05/17 確認欄位為 Sysstat3enckeyst
					sysstate.setSysstat3enckeyst(DbHelper.toShort(false));
					sysstate.setSysstatF3dessync(strFROMSYNC);
					sysstate.setSysstatT3dessync(strTOSYNC);
					msgBefore = " 更新前SYSSTAT_3ENCKEYST=" + SysStatus.getPropertyValue().getSysstat3enckeyst() + ",SYSSTAT_F3DESSYNC=" + SysStatus.getPropertyValue().getSysstatF3dessync()
							+ ",SYSSTAT_T3DESSYNC=" + SysStatus.getPropertyValue().getSysstatT3dessync();
					msgAfter = " 更新後SYSSTAT_3ENCKEYST=False,SYSSTAT_F3DESSYNC=" + strFROMSYNC + ",SYSSTAT_T3DESSYNC=" + strTOSYNC;
					break;
					//2025/5/8 update by Ashiang 補更新keyblockst欄位
				case "20":
					sysstate.setSysstatKeyblockst(DbHelper.toShort(false));
					msgBefore = " 更新前SYSSTAT_KEYBLOCKST=" + SysStatus.getPropertyValue().getSysstat3enckeyst() + ",SYSSTAT_F3DESSYNC=" + SysStatus.getPropertyValue().getSysstatF3dessync()
							+ ",SYSSTAT_T3DESSYNC=" + SysStatus.getPropertyValue().getSysstatT3dessync();
					msgAfter = " 更新後SYSSTAT_3ENCKEYST=False,SYSSTAT_F3DESSYNC=" + strFROMSYNC + ",SYSSTAT_T3DESSYNC=" + strTOSYNC;
					break;

			}

			switch (getFiscOPCReq().getKEYID()) {
				case "01":
					strMsg = getFiscOPCReq().getKEYID() + "(OPC MAC KEY)";
					break;
				case "02":
					strMsg = getFiscOPCReq().getKEYID() + "(CD/ATM MAC KEY)";
					break;
				case "03":
					strMsg = getFiscOPCReq().getKEYID() + "(RM MAC KEY)";
					break;
				case "04":
					strMsg = getFiscOPCReq().getKEYID() + "(OPC 3-DES MAC KEY)";
					break;
				case "05":
					strMsg = getFiscOPCReq().getKEYID() + "(CD/ATM 3-DES MAC KEY)";
					break;
				case "06":
					strMsg = getFiscOPCReq().getKEYID() + "(RM 3-DES MAC KEY)";
					break;
				case "11":
					strMsg = getFiscOPCReq().getKEYID() + "(PP KEY)";
					break;
				case "12":
					strMsg = getFiscOPCReq().getKEYID() + "(3-DES PPKEY)";
					break;
			}
			getLogContext().setRemark(strMsg + msgBefore);
			logMessage(Level.INFO, getLogContext());

			// 2025-4-27 Modify by Ashiang 只更新需要更新的欄位
			//if (sysstatMapper.updateByPrimaryKey(sysstate) <= 0) {
			if (sysstatMapper.updateKeySyncForChangeKey(sysstate) <= 0) {
				return IOReturnCode.SYSSTATUpdateError;
			} else {
				getLogContext().setRemark(strMsg + msgAfter);
				logMessage(Level.INFO, getLogContext());
				return CommonReturnCode.Normal;
			}
		} catch (Exception ex) {
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".doBusiness"));
			sendEMS(getLogContext());
			return CommonReturnCode.ProgramException;
		} finally {
			// 更新完後重抓SYSSTAT
			try {
				FEPCache.reloadCache(CacheItem.SYSSTAT);
			} catch (Exception e) {
				LogHelperFactory.getTraceLogger().warn(e, "reloadCache SYSSTAT failed");
			}
		}
	}

	// "8.更新交易記錄檔"
	private FEPReturnCode updateFeptxn() {

		FEPReturnCode rtnCode = FEPReturnCode.Abnormal;
		getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCCon().getResponseCode());
		getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
		/* 10/17 修改, 寫入處理結果 */
		getFiscBusiness().getFeptxn().setFeptxnAaComplete((short)1);

		if (_rtnCode != CommonReturnCode.Normal){
			getFiscBusiness().getFeptxn().setFeptxnTxrust("R");   /* 處理結果=Reject */
		}else {
			getFiscBusiness().getFeptxn().setFeptxnTxrust("A");  /* 處理結果=成功 */
		}
		rtnCode = getFiscBusiness().updateTxData();
		if (rtnCode != CommonReturnCode.Normal) {
			return rtnCode;
		} else {
			return _rtnCode;
		}
	}
}
