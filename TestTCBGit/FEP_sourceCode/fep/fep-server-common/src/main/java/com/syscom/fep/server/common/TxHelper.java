package com.syscom.fep.server.common;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.util.CleanPathUtil;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.CardtypeExtMapper;
import com.syscom.fep.mybatis.mapper.MsgfileMapper;
import com.syscom.fep.mybatis.model.Cardtype;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.mybatis.util.EjfnoGenerator;
import com.syscom.fep.vo.constant.AbnormalRC;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.List;

import static com.syscom.fep.vo.constant.AbnormalRC.External_Error;

public class TxHelper extends FEPBase {
//	@SuppressWarnings("unused")
//	private static final String ProgramName = TxHelper.class.getSimpleName();
	private static HashMap<Byte, Cardtype> cardTypeMap = null;
	private static final String mappingMessageNotFound = "回應代碼未建檔";

	private static final MsgfileMapper msgfileMapper = SpringBeanFactoryUtil.getBean(MsgfileMapper.class);

	/**
	 * 呼叫存儲過程獲取EJNO
	 * 
	 * @author Richard
	 * @return
	 */
	public static int generateEj() {
		EjfnoGenerator generator = SpringBeanFactoryUtil.getBean(EjfnoGenerator.class);
		return generator.generate();
	}

	/**
	 * FEP內部檢核錯誤(FEPReturn Code)對應出各通道的RC
	 * 
	 * @param fepInternalCode FEP訊息列舉
	 * @param mapChannel 訊息來源通道
	 * @return 各通道的RC
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/15</date>
	 *         <reason>New</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getRCFromErrorCode(FEPReturnCode fepInternalCode, FEPChannel mapChannel) {
		return getRCFromErrorCode(String.valueOf(fepInternalCode.getValue()), FEPChannel.FEP, mapChannel);
	}

	/**
	 * FEP內部檢核錯誤(FEPReturn Code)對應出各通道的RC
	 * 
	 * @param fepInternalCode FEP訊息列舉
	 * @param mapChannel 訊息來源通道
	 * @param logData SendEMS log data
	 * @return 各通道的RC
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/15</date>
	 *         <reason>New</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getRCFromErrorCode(FEPReturnCode fepInternalCode, FEPChannel mapChannel, LogData logData) {
		return getRCFromErrorCode(String.valueOf(fepInternalCode.getValue()), FEPChannel.FEP, mapChannel, logData);
	}

	/**
	 * 各通道錯誤代碼對應
	 * 
	 * @param errCode 訊息代碼
	 * @param sourceChannel 訊息來源通道
	 * @param destinationChannel 訊息轉換通道
	 * @return 各通道的RC
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/15</date>
	 *         <reason>New</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getRCFromErrorCode(String errCode, FEPChannel sourceChannel, FEPChannel destinationChannel) {
		LogData log = new LogData();
		log.setSubSys(SubSystem.CMN);
		log.setChannel(sourceChannel);
		log.setProgramName("getRCFromErrorCode");
		return getRCFromErrorCode(errCode, sourceChannel, destinationChannel, log);
	}

	/**
	 * 各通道錯誤代碼對應
	 * 
	 * @param errCode 訊息代碼
	 * @param sourceChannel 訊息來源通道
	 * @param destinationChannel 訊息轉換通道
	 * @param logData SendEMS log data
	 * @return
	 * 
	 *         1. 若RC為正常則直接回對應Channel的正常Code
	 *         2. From Cache Table找出該筆錯誤訊息
	 *         3. 設定logData中的ExternalCode及ResponseMessage
	 *         4. 若找不到對應Channel Error Code 應該回每個Channel固定Error Code(Unknown error)
	 *         5. 判斷是否要送EMS
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/15</date>
	 *         <reason>New</reason>
	 *         </modify>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2011/11/17</date>
	 *         <reason>特殊Channel轉換(EBILL)</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getRCFromErrorCode(String errCode, FEPChannel sourceChannel, FEPChannel destinationChannel, LogData logData) {
		LogHelperFactory.getTraceLogger().trace(
				"TXHelper.getRCFromErrorCode: SourceChannel=", sourceChannel,
				", ErrCode=", FEPReturnCode.toString(errCode),
				", destinationChannel= ", destinationChannel);
		String returnCode = StringUtils.EMPTY;

		// Add by David Tai on 2011-08-19 for NCR ATM需回覆4個空白代表正常回覆
		// 回覆正常
		if (StringUtils.isBlank(errCode) || errCode.trim().equals("0") || NormalRC.FISC_ATM_OK.equals(errCode) || NormalRC.FEP_OK.equals(errCode) || NormalRC.CBS_OK.equals(errCode)) {
			switch (destinationChannel) {
				case ATM:
				case EAT:
				case POS:
					return StringUtils.EMPTY;
				case HCE:
				case HCA:
				case NETBANK:
					return NormalRC.ATM_OK;
				case FISC:
					return NormalRC.FISC_ATM_OK;
				case BRANCH:
					return NormalRC.FEP_OK;
//				case UATMP:
//					return NormalRC.UATMP_OK;
//				case SINOCARD:
//					return NormalRC.SINOCARD_OK;
				case MBQ:
				case NBP:
				case NBQ:
				case NBB:
					return NormalRC.FISC_ATM_OK;
				default:
					return NormalRC.FEP_OK;
			}
		}

		if (logData != null) {
			logData.setExternalCode(StringUtils.EMPTY);
			logData.setResponseMessage(StringUtils.EMPTY);
		} else {
			logData = new LogData();
			logData.setSubSys(SubSystem.CMN);
			logData.setChannel(sourceChannel);
			logData.setProgramName("getRCFromErrorCode");
		}

		// Modify by David Tai on 2011-11-17 for 特殊Channel轉換(EBILL)
		switch (destinationChannel) {
			case EAT:
			case POS:
				destinationChannel = FEPChannel.ATM;
				break;
//			case EBILL:
//				destinationChannel = FEPChannel.FISC;
//				break;
			default:
				break;
		}

		// 處理T24的Error Code : 可能有Error Code 加訊息
		// T24的錯誤訊息格式A類 CREDIT.ACCT.NO:1:1=E12302CREDIT ACCOUNT NOT FOUND
		// B類(B0001,B0003,B4000)E04536EB-NO RECORDS RETURNED BY ROUTINE BASED
		// OVERRIDE有2種格式
		// OVERRIDE:1:1=X00114AC-Ac 00900491006569 Today (此格式符合A類錯誤)
		// OVERRIDE:24:1=POSTING.RESTRICT}X00808PO-Account....
		if (sourceChannel == FEPChannel.T24) {
			int pos1 = errCode.indexOf("=");
			// A類錯誤
			if (pos1 > -1 && PolyfillUtil.isNumeric(errCode.substring(pos1 + 2, pos1 + 2 + 5))) {
				logData.setResponseMessage(errCode.substring(pos1 + 7));
				errCode = errCode.substring(pos1 + 1, pos1 + 1 + 6);
				// B類錯誤
			} else if (errCode.length() > 6 && "E".equals(errCode.substring(0, 1))) {
				// Add by David Tai on 2011-06-01 for T24錯誤訊息
				logData.setResponseMessage(errCode.substring(6));
				errCode = errCode.substring(0, 6);
			} else if (errCode.toUpperCase().indexOf("OVERRIDE") > -1) {
				// Add by David Tai on 2012-1-5 for T24 override錯誤訊息
				int t = errCode.toUpperCase().indexOf("}");
				if (t > -1) {
					logData.setResponseMessage(errCode.substring(t + 7));
					errCode = errCode.substring(t + 1, t + 1 + 6);
				} else {
					logData.setResponseMessage(errCode);
				}
			} else {
				// T24未帶Error Code
				logData.setResponseMessage(errCode);
				logData.setExternalCode(External_Error);
				// Modify by Jim, 2011/01/12, MapErrorCode裡面已經有包含依照destinationChannel決定回復內容的邏輯
				return mapErrorCode(destinationChannel);
			}
		}
		LogHelperFactory.getTraceLogger().trace("Get ", sourceChannel ," ErrorCode=", FEPReturnCode.toString(errCode));
		Msgfile msgfile = msgfileMapper.selectByPrimaryKey(sourceChannel.getCode(), errCode);
		if (msgfile != null) {
			LogHelperFactory.getTraceLogger().trace("TXHelper Query MSGFILE(Channel=", sourceChannel, ",ErrCode=", FEPReturnCode.toString(errCode), ") OK");
			// 轉換Error Code
			returnCode = convertRCWithDr(errCode, sourceChannel, destinationChannel, logData, msgfile);
		} else {
			// 轉換Error Code
			returnCode = convertRCNoDr(errCode, sourceChannel, destinationChannel, logData);
		}
		return returnCode.trim();
	}

	private static String convertRCWithDr(String errCode, FEPChannel sourceChannel, FEPChannel destinationChannel, LogData logData, Msgfile msgfile) {
		String rc = StringUtils.EMPTY;

		// 回FISC一律要轉
		if (destinationChannel.equals(FEPChannel.FISC)) {
			rc = checkRC(msgfile.getMsgfileFisc());
		} else {
			switch (sourceChannel) {
				case FISC:
					switch (destinationChannel) {
						case ATM:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
						default:
							rc = checkRC(msgfile.getMsgfileExternal());
							break;
					}
					break;
				case FEP:
					switch (destinationChannel) {
//						case UATMP:
//							rc = checkRC(msgfile.getMsgfileUatmp());
//							break;
						case ATM:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
//						case SINOCARD:
//							rc = checkRC(msgfile.getMsgfileCredit());
//							break;
						case T24:
							rc = checkRC(msgfile.getMsgfileT24());
							// Add by David Tai on 2014-02-06 for MMA悠遊Debit
							break;
						case SVCS:
							rc = checkRCSVCS(msgfile.getMsgfileSvcs());
							break;
						case NBQ:
						case NBP:
						case MBQ:
						case EDI:
						case EOI:
						case HCA:
						case HCE:
						case EAT:
						case VO:
						case NBB:
						case MQQ:
						case MSQ:
						case FID:
						case SSO:
						case ONL:
						case DIG:
						case BIZ:
						case HLN:
						case EIP:
						case NAM:
						case OPN:
						case VIP:
						case NONVIP:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
						default:
							rc = checkRC(msgfile.getMsgfileExternal());
							break;
					}
					break;
				case CBS:
					switch (destinationChannel) {
						case ATM:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
//						case SINOCARD:
//							rc = checkRC(msgfile.getMsgfileCredit());
//							// Add by David Tai on 2014-02-06 for MMA悠遊Debit
//							break;
						case SVCS:
							rc = checkRCSVCS(msgfile.getMsgfileSvcs());
							break;
						default:
							rc = errCode;
							break;
					}
					break;
//				case UATMP:
//					switch (destinationChannel) {
//						case ATM:
//							rc = errCode;
//							break;
//						case SINOCARD:
//							rc = errCode;
//							break;
//						default:
//							rc = checkRC(msgfile.getMsgfileExternal());
//							break;
//					}
//					break;
//				case SINOCARD:
//					switch (destinationChannel) {
//						case ATM:
//							rc = errCode;
//							break;
//						default:
//							rc = checkRC(msgfile.getMsgfileExternal());
//							break;
//					}
//					break;
				case IVR:
					switch (destinationChannel) {
						case ATM:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
						default:
							rc = checkRC(msgfile.getMsgfileExternal());
							break;
					}
					// Add by David Tai on 2014-02-06 for MMA悠遊Debit
					break;
				case SVCS:
					switch (destinationChannel) {
						case BRANCH:
							rc = checkRCSVCS(msgfile.getMsgfileExternal());
							break;
						case FEP:
							rc = errCode;
							break;
						default:
							rc = checkRCSVCS(msgfile.getMsgfileExternal());
							break;
					}
					break;
				case FID:
					switch (destinationChannel) {
						case ATM:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
						case FISC:
							rc = checkRC(msgfile.getMsgfileFisc());
							break;
						default:
							rc = checkRC(msgfile.getMsgfileAtm());
							break;
					}
					break;
				default:
					rc = checkRC(msgfile.getMsgfileExternal());
					break;
			}
		}

		// 設定logData中的ExternalCode及ResponseMessage
		logData.setExternalCode(StringUtils.isBlank(msgfile.getMsgfileExternal()) ? StringUtils.EMPTY : msgfile.getMsgfileExternal().trim());
		logData.setResponsible(StringUtils.isBlank(msgfile.getMsgfileResponsible()) ? StringUtils.EMPTY : msgfile.getMsgfileResponsible().trim());
		logData.setNotification(DbHelper.toBoolean(msgfile.getMsgfileNotify()));
		// Add by David Tai on 2011-06-15 for T24回覆錯誤則LogData.ResponseMessage以T24傳回為準
		if (sourceChannel != FEPChannel.T24) {
			logData.setResponseMessage(
					FormatUtil.messageFormat(
							msgfile.getMsgfileShortmsg().trim(),
							logData.getTableName(),
							logData.getTableDescription(),
							logData.getIoMethd(),
							logData.getPrimaryKeys(),
							logData.getAtmNo(),
							logData.getAtmSeq(),
							logData.getTxDate(),
							logData.getEj(),
							logData.getStan(),
							logData.getMessageId(),
							logData.getTroutActno(),
							logData.getTrinActno(),
							logData.getChact(),
							logData.getMessageParm13(),
							logData.getMessageParm14()));
			// Modify by David Tai on 2011-12-05 for 容許AA自組訊息
			if (StringUtils.isBlank(logData.getRemark())) {
				logData.setRemark(logData.getResponseMessage());
			}
		}
		LogHelperFactory.getTraceLogger().trace(
				"TxHelper Set logdata=>ExternalCode:", logData.getExternalCode(),
				",Responsible:", logData.getResponsible(),
				",Notification:", logData.isNotification());
		// 若rc為空代表找不到對應或對應值為NULL(MSGFILE未建立)
		if (StringUtils.isBlank(rc)) {
			// 若找不到對應Channel Error Code 應該回每個Channel固定Error Code(Unknown error)
			rc = mapErrorCode(destinationChannel);
		}

		// Modify by David Tai on 2011-07-01 for EMS處理方式改為獨立Sub(GetMessage也可使用)
		// Modify by David Tai on 2011-07-12 for LogData 的Channel是在Handler決定, 代表交易來源, 所以送EMS時不用轉換
		emsProcess(msgfile, logData);
		return rc;
	}

	/**
	 * Modify by David Tai on 2011-07-12 for LogData 的Channel是在Handler決定, 代表交易來源, 所以送EMS時不用轉換
	 * 
	 * @param msgfile
	 * @param emsLogData
	 */
	private static void emsProcess(Msgfile msgfile, LogData emsLogData) {
		LogHelperFactory.getTraceLogger().trace(
				"TXHelper.emsProcess : SourceChannel=", emsLogData.getChannel(),
				", ErrCode=", FEPReturnCode.toString(emsLogData.getReturnCode()),
				", MSGFILE_SEVERITY=", msgfile.getMsgfileSeverity(),
				", MSGFILE_SEND_EMS=", msgfile.getMsgfileSendEms());
		// 先判斷SEND_EMS, 若為False則以SEVERITY欄位判斷要不要送EMS
		if (DbHelper.toBoolean(msgfile.getMsgfileSendEms())
				|| "error".equalsIgnoreCase(msgfile.getMsgfileSeverity())
				|| "fatal".equalsIgnoreCase(msgfile.getMsgfileSeverity())) {
			if (StringUtils.isNotBlank(msgfile.getMsgfileFepname())) {
				emsLogData.setReturnCode(FEPReturnCode.parse(msgfile.getMsgfileFepname()));
			} else {
				emsLogData.setReturnCode(FEPReturnCode.Abnormal);
			}

			// Add by David Tai on 2012-06-04 for 修改送EMS LogData.Remark紀錄內容
			// Marked by David Tai on 2011-07-08 for 先判斷是否有值
			String formatMessage = StringUtils.join(
					"來源:", FEPChannel.fromCode(msgfile.getMsgfileChannel().intValue()),
					", 代號:" + msgfile.getMsgfileErrorcode(), ", ", msgfile.getMsgfileMsgdscpt() + "!");
			if (StringUtils.isBlank(emsLogData.getRemark())) {
				emsLogData.setRemark(formatMessage);
				// Modify by David Tai on 2012-07-31 for AP另加說明必須一起顯示
			} else {
				emsLogData.setRemark(StringUtils.join(formatMessage, "[", emsLogData.getRemark(), "]"));
			}

			// Modify by David Tai on 2011-07-01 for 改用SendEMS
			// Modify by David Tai on 2011-07-08 for 依據MSGFILE的MSGFILE_SEVERITY傳入SendEMS參數

			LogHelperFactory.getTraceLogger().trace(
					"TxHelper SourceChannel=", emsLogData.getChannel(),
					", ErrCode=", FEPReturnCode.toString(emsLogData.getReturnCode()), ", Call SendEMS");
			// Add by David Tai on 2011-12-12 for Info 要發Mail
			emsLogData.setResponsible(msgfile.getMsgfileResponsible() == null ? StringUtils.EMPTY : msgfile.getMsgfileResponsible().trim());
			if (!emsLogData.isNotification()) {
				emsLogData.setNotification(DbHelper.toBoolean(msgfile.getMsgfileNotify()));
			}

			// Add by David Tai on 2012-07-18 for 特定財金Error Code只要RM送Mail, ATM不用
			switch (msgfile.getMsgfileErrorcode()) {
				case "0204":
				case "0205":
				case "0206":
				case "0207":
					// Jim, 2012/7/30, 改成子系統非RM，就不發mail
					// Add by David Tai on 2012-07-26 for 改為負向判斷，只要RM送Mail, 其他不用
//					if (emsLogData.getSubSys().getValue() != SubSystem.RM.getValue()) {
//						emsLogData.setNotification(false);
//					}
					break;
			}

			// 2025-08-13 Richard add
			// 另外修改TxHelper在EMSProcess, 將MSGFILE_NOTIFYPHONE的值傳入emsLogData.NotifyPhone屬性(新增) by Ashiang
			emsLogData.setNotifyPhone(msgfile.getMsgfileNotifyphone());

			try {
				LogLevel emsLevel = LogLevel.parse(msgfile.getMsgfileSeverity().trim());
				switch (emsLevel) {
					case Debug:
						sendEMS(Level.DEBUG, emsLogData);
						break;
					case Error:
					case Fatal:
						sendEMS(Level.ERROR, emsLogData);
						break;
					case Info:
						sendEMS(Level.INFO, emsLogData);
						break;
					case Warning:
						sendEMS(Level.WARN, emsLogData);
						break;
					default:
						break;
				}
			} catch (IllegalArgumentException e) {
				sendEMS(Level.ERROR, emsLogData);
			}
		}
	}

	/**
	 * MSGFILE未建檔
	 * 
	 * @param errCode
	 * @param sourceChannel
	 * @param destinationChannel
	 * @param logData
	 * @return
	 */
	private static String convertRCNoDr(String errCode, FEPChannel sourceChannel, FEPChannel destinationChannel, LogData logData) {
		String rc = StringUtils.EMPTY;

		// 回FISC一律要轉
		if (destinationChannel.equals(FEPChannel.FISC)) {
			rc = AbnormalRC.FISC_Error;
		} else {
			switch (sourceChannel) {
				case FISC:
					switch (destinationChannel) {
						case ATM:
							rc = errCode;
							break;
//						case SINOCARD:
//							rc = errCode;
//							break;
						default:
							rc = mapErrorCode(destinationChannel);
							break;
					}
					break;
				case FEP:
					rc = mapErrorCode(destinationChannel);
					break;
				case T24:
					switch (destinationChannel) {
						case ATM:
							rc = AbnormalRC.ATM_Error;
							break;
//						case SINOCARD:
//							rc = AbnormalRC.SINOCARD_Error;
//							// Add by David Tai on 2014-02-06 for MMA悠遊Debit
//							break;
						case SVCS:
							rc = AbnormalRC.SVCS_Error;
							break;
						default:
							rc = errCode;
							break;
					}
					break;
//				case UATMP:
//					switch (destinationChannel) {
//						case ATM:
//							rc = errCode;
//							break;
//						case SINOCARD:
//							rc = errCode;
//							break;
//						default:
//							rc = mapErrorCode(destinationChannel);
//							break;
//					}
//					break;
//				case SINOCARD:
//					switch (destinationChannel) {
//						case ATM:
//							rc = errCode;
//							break;
//						default:
//							rc = mapErrorCode(destinationChannel);
//							break;
//					}
//					break;
				case ATM:
					switch (destinationChannel) {
//						case SINOCARD:
//							rc = errCode;
//							break;
						default:
							rc = mapErrorCode(destinationChannel);
							break;
					}
					break;
				default:
					rc = mapErrorCode(destinationChannel);
					break;
			}
		}

		// MSGFILE未建資料
		logData.setExternalCode(errCode); // 放原始錯誤代碼
		// Modify by David Tai on 2011-06-01 for T24錯誤代碼對應不到時直接回T24回覆錯誤訊息
		if (sourceChannel != FEPChannel.T24) {
			logData.setResponseMessage(mappingMessageNotFound);
		}
		return rc;
	}

	private static String mapErrorCode(FEPChannel mapChannel) {
		switch (mapChannel) {
			case ATM:
				return AbnormalRC.ATM_Error;
			case FISC:
				return AbnormalRC.FISC_Error;
			case T24:
				return AbnormalRC.T24_Error;
			case CBS:
				return AbnormalRC.CBS_Error;
//			case UATMP:
//				return AbnormalRC.UATMP_Error;
//			case SINOCARD:
//				return AbnormalRC.SINOCARD_Error;
			// Add by David Tai on 2014-02-06 for MMA悠遊Debit
			case SVCS:
				return AbnormalRC.SVCS_Error;
			default:
				return AbnormalRC.External_Error;
		}

	}

	private static String checkRC(String rtnCode) {
		if (StringUtils.isBlank(rtnCode)) {
			return AbnormalRC.ATM_Error;
		} else {
			return rtnCode;
		}
	}

	// Add by Ruling on 2014-02-13 for MMA悠遊Debit
	private static String checkRCSVCS(String rtnCode) {
		if (StringUtils.isBlank(rtnCode)) {
			return AbnormalRC.SVCS_Error;
		} else {
			return rtnCode;
		}
	}

	/**
	 * 用FEPReturn Code去取得錯誤訊息
	 * 
	 * @param rtnCode 訊息代碼
	 * @return
	 *         Channel為FEP
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/7/26</date>
	 *         <reason>Msgfile change schema(Primay key change)</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getMessageFromFEPReturnCode(FEPReturnCode rtnCode) {
		return getMessageFromFEPReturnCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP);
	}

	/**
	 * 用某Channel的Error Code去取得錯誤訊息
	 * 
	 * @param errCode 訊息代碼
	 * @param sourceChannel 訊息來源通道
	 * @return
	 *         若FEPReturnCode對應不到則回"回應代碼未建檔"
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/7/26</date>
	 *         <reason>Msgfile change schema(Primay key change)</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getMessageFromFEPReturnCode(String errCode, FEPChannel sourceChannel) {
		LogHelperFactory.getTraceLogger().trace(
				"TXHelper.GetMessageFromFEPReturnCode : sourceChannel=", sourceChannel,
				", errCode=", FEPReturnCode.toString(errCode));
		Msgfile msgfile = msgfileMapper.selectByPrimaryKey(sourceChannel.getCode(), errCode);
		if (msgfile != null) {
			LogHelperFactory.getTraceLogger().trace("TXHelper Query MSGFILE(Channel=", sourceChannel, ",ErrCode=", FEPReturnCode.toString(errCode), ") OK");
			return msgfile.getMsgfileMsgdscpt();
		} else {
			LogHelperFactory.getTraceLogger().trace("TXHelper.GetMessageFromFEPReturnCode : sourceChannel=", sourceChannel, ", errCode=", FEPReturnCode.toString(errCode), ", 回應代碼未建檔");
			// 若FEPReturnCode對應不到則回"回應代碼未建檔"
			return mappingMessageNotFound + "(" + sourceChannel.toString() + "-" + FEPReturnCode.toString(errCode) + ")";
		}
	}

	/**
	 * 用FEPReturn Code去取得錯誤訊息，並以LogData Value取代訊息中變數
	 * 
	 * @param rtnCode 訊息代碼
	 * @param logData Log物件
	 * @return
	 * 
	 *         Channel為FEP
	 *         0:TableName 1:TableDescription 2:IOMethod 3:PrimaryKeys 4:ATMNo 5:ATMSeq 6:TxDate 7:EJ 8:Stan 9:MessageId 10:TroutActno 11:TrinActno 12:Chact
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/11</date>
	 *         <reason>New interface</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getMessageFromFEPReturnCode(FEPReturnCode rtnCode, LogData logData) {
		return getMessageFromFEPReturnCode(String.valueOf(rtnCode.getValue()), FEPChannel.FEP, logData);
	}

	/**
	 * 用某Channel的Error Code去取得錯誤訊息，並以LogData Value取代訊息中變數
	 * 
	 * @param errCode 訊息代碼
	 * @param sourceChannel 訊息來源通道
	 * @param logData Log物件
	 * @return 此Function回傳未格式化的訊息(MSGFILE_MSGDSCPT), LogData.ResponseMessage回傳格式化的訊息(MSGFILE_SHORTMSG)
	 * 
	 *         1. 格式化訊息變數Index如下:
	 *         0:TableName 1:TableDescription 2:IOMethod 3:PrimaryKeys 4:ATMNo 5:ATMSeq 6:TxDate 7:EJ 8:Stan 9:MessageId 10:TroutActno 11:TrinActno 12:Chact
	 *         2. 若FEPReturnCode對應不到則回"回應代碼未建檔"
	 * 
	 *         <history>
	 *         <modify>
	 *         <modifier>David></modifier>
	 *         <date>2010/10/11</date>
	 *         <reason>New interface</reason>
	 *         </modify>
	 *         </history>
	 */
	public static String getMessageFromFEPReturnCode(String errCode, FEPChannel sourceChannel, LogData logData) {
		LogHelperFactory.getTraceLogger().trace(
				"TXHelper.getMessageFromFEPReturnCode : sourceChannel=", sourceChannel,
				", errCode=", FEPReturnCode.toString(errCode));
		String rtnMessage = StringUtils.EMPTY;
		Msgfile msgfile = msgfileMapper.selectByPrimaryKey(sourceChannel.getCode(), errCode);
		if (msgfile != null) {
			rtnMessage = msgfile.getMsgfileMsgdscpt().trim();
			// 判斷LogData是否有值, 若為Nothing則回傳原訊息
			if (logData != null) {
				logData.setExternalCode(msgfile.getMsgfileExternal());
				logData.setResponseMessage(
						FormatUtil.messageFormat(
								msgfile.getMsgfileShortmsg().trim(),
								logData.getTableName(),
								logData.getTableDescription(),
								logData.getIoMethd(),
								logData.getPrimaryKeys(),
								logData.getAtmNo(),
								logData.getAtmSeq(),
								logData.getTxDate(),
								String.valueOf(logData.getEj()),
								logData.getStan(),
								logData.getMessageId(),
								logData.getTroutActno(),
								logData.getTrinActno(),
								logData.getChact(),
								logData.getMessageParm13(),
								logData.getMessageParm14()));
				logData.setRemark(logData.getResponseMessage());
				LogHelperFactory.getTraceLogger().trace("TXHelper.GetMessageFromFEPReturnCode : logData.Remark=", logData.getRemark());

				// Add by David Tai on 2011-07-01 for GetMessage也要送EMS
				// Modify by David Tai on 2011-07-12 for LogData 的Channel是在Handler決定, 代表交易來源, 所以送EMS時不用轉換
				emsProcess(msgfile, logData);
			}
		} else {
			LogHelperFactory.getTraceLogger().trace("TXHelper.GetMessageFromFEPReturnCode : sourceChannel=", sourceChannel, ", errCode=", FEPReturnCode.toString(errCode), ", 回應代碼未建檔");
			// 若Error Code對應不到則回"回應代碼未建檔"
			return StringUtils.join(mappingMessageNotFound, "(", sourceChannel.toString(), "-", FEPReturnCode.toString(errCode), ")");
		}
		return rtnMessage;
	}

	/**
	 * Substring for Double Length String
	 * 
	 * 以UTF8處理, 須避免中文字折半, 若最後一個字為中文取半則此字不回
	 * 
	 * @param utf8String 中英文字串
	 * @param start 開始位置
	 * @param len 回傳字串的固定長度
	 * @return 固定長度的字串
	 */
	public static String subStr(String utf8String, int start, int len) {
		byte[] byteChar = null; // UTF8字串中的字元Byte()
		String rtnString = ""; // 回傳字串
		int countChar = 0; // 累計字元數
		// 檢查開始位置是否超過字串長度, 若超過則回空字串
		if (utf8String.getBytes().length <= start) {
			return rtnString;
		}
		char[] chars = utf8String.toCharArray();
		for (int i = start; i < chars.length; i++) {
			// 將UTF8字串中的每個字元轉成Byte()
			byteChar = ConvertUtil.toBytes(String.valueOf(chars[i]), PolyfillUtil.toCharsetName("Unicode"));
			// 判斷是否為中文(Unicode為2 Bytes, 第2個Byte>0表示中文)
			if (byteChar[1] > 0) {
				countChar += 2;
			} else {
				countChar += 1;
			}
			if (countChar > len) {
				break;
			} else {
				rtnString = StringUtils.join(rtnString, ConvertUtil.toString(byteChar, PolyfillUtil.toCharsetName("Unicode")));
			}
		}
		return rtnString;
	}

	public static void sendSMSDB(String number, String msg, byte priority, String brno, String pcode, String rrn, String idno, String company, String channel, LogData LogContext) {
		// TODO
		LogHelperFactory.getTraceLogger().info("寫入簡訊資料庫(SMS)暫時無法實作");
	}

	// Fly 2018/06/13 將發送EMAIL移至TxHelper
	public static void sendMailHunter(String proj, String email, String fromname, String fromemail, String subject, String mailBody, String channel, String pcode, String idno, String priority,
			LogData LogContext) {
		// TODO
		LogHelperFactory.getTraceLogger().info("MailHunter暫時無法實作");
	}

	public static Cardtype getCardType(byte cardType) {
		synchronized (cardTypeMap) {
			if (cardTypeMap == null) {
				cardTypeMap = new HashMap<Byte, Cardtype>();
				CardtypeExtMapper cardtypeExtMapper = SpringBeanFactoryUtil.getBean(CardtypeExtMapper.class);
				List<Cardtype> cardtypeList = cardtypeExtMapper.queryAllData();
				for (Cardtype cardtype : cardtypeList) {
					cardTypeMap.put(cardtype.getCardtype().byteValue(), cardtype);
				}
			}
			Cardtype cardtype = cardTypeMap.get(cardType);
			if (cardtype != null) {
				return cardtype;
			} else {
				return new Cardtype();
			}
		}
	}

	/**
	 * Fly 2018/06/19 Fortify修正:Path Manipulation
	 * 
	 * @param param
	 * @return
	 */
	public static String filterParams(String param) {
		return CleanPathUtil.cleanString(param);
	}

	/**
	 * 傳入CID 例:10123456789 前面兩位轉為字母回傳
	 * @param CID
	 * @return
	 */
	public static String parseCIDtoChar(String CID){
		String code = CID;
		String alpha = CID.substring(0, 2);
		int[] locationNumber = {10, 11, 12, 13, 14, 15, 16, 17, 34, 18, 19, 20, 21, 22, 35, 23, 24, 25, 26, 27, 28, 29, 32, 30, 31, 33};
		String[] locationChar = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for (int i = 0; i < locationChar.length; i++) {
			if (locationNumber[i] == Integer.parseInt(alpha)) {
				code = locationChar[i] + code.substring(2);
			}
		}
		return code;
	}

	/**
	 * 英文轉數字
	 * @param CID
	 * @return
	 */
	public static String parseCIDtoNum(String CID){
		String code = CID;
		String alpha = CID.substring(0, 1);
		int[] locationNumber = {10, 11, 12, 13, 14, 15, 16, 17, 34, 18, 19, 20, 21, 22, 35, 23, 24, 25, 26, 27, 28, 29, 32, 30, 31, 33};
		String[] locationChar = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for (int i = 0; i < locationChar.length; i++) {
			if (StringUtils.equals(alpha, locationChar[i])) {
				code = locationNumber[i] + code.substring(1);
			}
		}
		return code;
	}

	public static String getIMSTRANSString(String channel,String msgid){
		String rtn = "MFEPEA00";
		if (StringUtils.equals(FEPChannel.HCE.getNameS(), channel) || StringUtils.contains(msgid,"2566"))
			rtn = "MFEPAF00";
		if (StringUtils.equals(FEPChannel.NAM.getNameS(), channel))
			rtn = "MFEPNPY0";
		return rtn;
	}
}
