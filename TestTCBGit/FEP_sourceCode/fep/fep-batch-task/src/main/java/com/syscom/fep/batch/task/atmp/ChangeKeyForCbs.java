package com.syscom.fep.batch.task.atmp;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.batch.base.task.Task;
import com.syscom.fep.server.common.adapter.*;
import com.syscom.fep.enchelper.ATMENCHelper;
import com.syscom.fep.mybatis.enc.ext.mapper.EnckeyExtMapper;
import com.syscom.fep.mybatis.enc.model.Enckey;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.EnvPropertiesUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.configuration.CMNConfig;
import com.syscom.fep.configuration.SysStatus;
import java.util.Calendar;
import com.syscom.fep.batch.base.enums.BatchReturnCode;

public class ChangeKeyForCbs extends FEPBase implements Task {

	//	private EnckeyExtMapper encKeyExtMapper;
	private String _programName = ChangeKeyForCbs.class.getSimpleName();
	private BatchJobLibrary _job = null;
	private String _BatchLogPath = StringUtils.EMPTY;
	private LogData _logData = null;
	private Boolean callBatchJob = true;
	private String _wsid = "AMFEP";
	private String _KeyType = "";
	private ATMData _atmData = new ATMData();
	private int _retryCount = 0;
	private int _retryInterval = 0;
	private EnckeyExtMapper endkeyExtMapper = SpringBeanFactoryUtil.getBean(EnckeyExtMapper.class);
	// 批次相關參數

	@Override
	public BatchReturnCode execute(String[] args) {
		boolean runOk = false;
		try {
			// System.out.println("ChangeKeyForCbs Start.......");
			// 初始化相關批次物件及拆解傳入參數
			initialBatch(args);
			// 2. 檢核批次參數是否正確, 若正確則啟動批次工作
			_job.writeLog("------------------------------------------------------------------");
			_job.writeLog(_programName + " begin");
			// 回報批次平台開始工作
			if (callBatchJob)
				_job.startTask();

			// 執行批次主要工作
			//增加retry功能

			for(int i = 0;i < _retryCount; i++) {
				try {
					if(_KeyType.equals("MAC")) {
						ProcessMAC();
						runOk = true;
						break;
					} else {
						ProcessPPK();
						runOk = true;
						break;
					}
				} catch (Exception ex) {
					//若失敗繼續重試
					Thread.sleep(_retryInterval);
					_job.writeLog("Begin retry changeKey-" + i + 1);
				}
			}

			// _job.writeLog(_programName + " end!");
			// 回報批次平台結束工作
			if (callBatchJob) {
				if(runOk)
					_job.endTask();
				else {
					_job.abortTask();
					_job.writeLog(_programName + " abort");
				}
			}

			return BatchReturnCode.Succeed;
		} catch (Exception ex) {
			if (_job == null) {
				// Send to System Event
//				System.out.println(ex.toString());
			} else {
				// 回報批次平台工作失敗,暫停後面流程
				if (callBatchJob) {
					try {
						_job.abortTask();
					} catch (Exception e) {
						_job.writeLog("Process Error, " + e.getMessage());
					}
				}

				_job.writeLog(_programName + " fail!");
				_job.writeLog(ex.toString());
				_logData.setProgramException(ex);
				// 不得直接呼叫FEPBase.SendEMS
				BatchJobLibrary.sendEMS(_logData);
			}
			return BatchReturnCode.ProgramException;
		} finally {
			if (_job != null) {
				_job.writeLog(_programName + " exit!!");
				_job.writeLog("------------------------------------------------------------------");
				_job.dispose();
				_job = null;
			}
			if (_logData != null) {
				_logData = null;
			}
		}
	}

	private void initialBatch(String[] args) throws Exception {
		_logData = new LogData();
		_logData.setChannel(FEPChannel.BATCH);
		_logData.setEj(0);
		_logData.setProgramName(_programName);
		_atmData.setLogContext(_logData);
		// 初始化BatchJob物件,傳入工作執行參數
		_BatchLogPath = CMNConfig.getInstance().getBatchLogPath().trim();
		if (StringUtils.isBlank(_BatchLogPath)) {
//			System.out.println("Batch Log Path undefined.");
			return;
		}

		_job = new BatchJobLibrary(this, args, _BatchLogPath);
		// 顯示help說明
		// if (_job.getArguments().containsKey("?")) {
		// // DisplayUsage();
		// return;
		// }
		// 拆解傳入的參數並存入變數
		if (_job.getArguments().containsKey("WSID")) {
			_wsid = _job.getArguments().get("WSID");
			// } //else {
			// throw new Exception("必須傳入參數WSID");

		}
		if (_job.getArguments().containsKey("KeyType")) {
			_KeyType = _job.getArguments().get("KeyType");
		} else {
			throw new Exception("必須傳入參數KeyType");
		}

		_retryCount = Integer.valueOf(EnvPropertiesUtil.getProperty("fep.batch.task.changeKeyForCbs.retryCount", "10"));
		_retryInterval = Integer.valueOf(EnvPropertiesUtil.getProperty("fep.batch.task.changeKeyForCbs.retryInterval", "1000"));

		if (_job.getArguments().containsKey("CallBatchJob")) {
			callBatchJob = Boolean.parseBoolean(_job.getArguments().get("CallBatchJob"));
		}
	}

	private void ProcessMAC() throws Exception {
		String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), "yyMMdd");
		String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), "HHmmss");
		String seq = time.substring(0, 4);
		// 1.組上主機電文
		String rqkTita = GetIMSTitaForMK(date, time, "01", seq, "AA", "IN", "00000000");
		_job.writeLog("Prepare First Tita(ascii):" + rqkTita);
		String tita = EbcdicConverter.toHex(CCSID.English, rqkTita.length(), rqkTita);
		_job.writeLog("Prepare First Tita(Ebcdic):" + tita);
		// System.out.println(rqkTita);
		// 2. 上送主機
		try {

			IMSAdapter adapter = new IMSAdapter(_atmData);
			adapter.setCBSTxid("ChangeKey");
			adapter.setMessageToIMS(tita);

			FEPReturnCode rtnCode = adapter.sendReceive();
			if (rtnCode != FEPReturnCode.Normal) {
				_job.writeLog("Send First IMS Fail,FEPReturnCode:" + rtnCode);
				throw new Exception("Send First IMS Fail");
				//return;
			}

			// 3.拆解第一道主機回應電文
			String response = adapter.getMessageFromIMS();
			// String response =
			// "C1D4C6C5D7F0F140C6D7D5F2F3F0F1F3F0F0F9F5F1F5F2F0F0F0F1F0F1F4F0F0F0F0C3F3F7F0C1C3F6F1C6C2F5F6C3C2C3F5F6F4F2F3F7C3F3C6F4F6F7F2C2C2F4F840404040404040404040404040404040F1F8F8F9F8F7C4F0";
			_job.writeLog("Get First IMS Response:" + response);
			if (response != null && !response.isEmpty()) {
				_job.writeLog("First Response ASCII:" + EbcdicConverter.fromHex(CCSID.English, response));
				if (!response.startsWith("C1D4C6C5D7")) {
					throw new Exception("IMS Response Error");
				}
			}
			String msgType = "";
			String rcCode = "";
			if(StringUtils.isNotBlank(response)) {
				msgType = response.substring(18, 22);
				rcCode = response.substring(60, 68);
			}

			String macKey = "";
			String macCode = "";
			if (msgType.equals("D7D5") && rcCode.equals("F0F0F0F0")) { // FPN & 0000 //
				macKey = EbcdicConverter.fromHex(CCSID.English, response.substring(68, 132)); // double length
				macCode = EbcdicConverter.fromHex(CCSID.English, response.substring(164, 180));
				_job.writeLog("Get Data from IMS, Key:" + macKey + ", mac:" + macCode);
				String inputData2 = "0060000000000000";
				RefString newKey = new RefString(null);
				if (changeKey(macKey, inputData2, macCode, newKey) != FEPReturnCode.Normal) {
					throw new Exception("Check First IMS MAC Error,Abort");
				}
				// 第一道回應keyid 先暫時寫IMS for checkmak及壓confirm mac
				String bankId = StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS");
				UpdateKey(bankId, "T2", "MAC", newKey.get(), true);
				// String inputData = EbcdicConverter.toHex(CCSID.English, 16,
				// "0060000000000000");

				// 組第2道Confirm
				date = FormatUtil.dateTimeFormat(Calendar.getInstance(), "yyMMdd");
				time = FormatUtil.dateTimeFormat(Calendar.getInstance(), "HHmmss");
				RefString newMac = new RefString(null);
				String inputData = StringUtils.rightPad("MKFSN" + date + time, 24, "0");
				rtnCode = makeIMSMac(bankId, inputData, newMac);
				if (rtnCode != FEPReturnCode.Normal) {
					throw new Exception("Make Second IMS MAC Error");
				}
				// seq = StringUtils.rightPad(Integer.toString(Integer.parseInt(seq) + 1), 4,
				// "0");
				String rqkTita2 = GetIMSTitaForMK(date, time, "02", seq, "SN", "NN", newMac.get());
				_job.writeLog("Prepare Second Tita(ascii):" + rqkTita2);
				String tita2 = EbcdicConverter.toHex(CCSID.English, rqkTita2.length(),
						rqkTita2);
				_job.writeLog("Prepare Second Tita(Ebcdic):" + tita2);
				IMSAdapter adapter2 = new IMSAdapter(_atmData);
				adapter2.setCBSTxid("ChangeKey");
				adapter2.setMessageToIMS(tita2);
				// 送第2道Confirm
				rtnCode = adapter2.sendReceive();
				if (rtnCode != FEPReturnCode.Normal) {
					throw new Exception("Send Second IMS Fail,FEPReturnCode:" + rtnCode);
				}
				response = adapter2.getMessageFromIMS();
				_job.writeLog("Get Second IMS Response:" + response);
				if (response != null && !response.isEmpty() &&
						response.startsWith("C1D4C6C5D7")) {
					String resAsc = EbcdicConverter.fromHex(CCSID.English, response);
					_job.writeLog("Second Response ASCII:" + resAsc);
					// response = StringUtil.fromHex(response);
					msgType = response.substring(18, 22);
					String tdseg = response.substring(54, 58); // 第2道
					rcCode = response.substring(60, 68);
					if (msgType.equals("D7C3") &&
							tdseg.equals("F0F2") &&
							rcCode.equals("F0F0F0F0")) { // PC & 02 & 0000 //Confirm
						// Response (FPC)交易成功
						_job.writeLog("IMS Confirm Response OK, MSGTYPE=" + resAsc.substring(9, 11) + ",RC="
								+ resAsc.substring(30, 34));
						// // 主機回成功更新原IMS KEY
						UpdateKey(bankId, "T2", "MAC", "", false);
					} else {
						_job.writeLog(
								"IMS Confirm Response Error, MSGTYPE=" + resAsc.substring(9, 11) + ",RC="
										+ resAsc.substring(30, 34));
						//return;
						throw new Exception("IMS Confirm Response Error");
					}
				} else {
					_job.writeLog("IMS Confirm Response Error");
					throw new Exception("IMS Confirm Response Error");
					//return;
				}

			} else if (msgType.equals("D7C3")) { // Response (FPC)交易失敗
				_job.writeLog("IMS Response Error, MSGTYPE=PC," + rcCode);
				//return;
				throw new Exception("IMS Response Error");
			} else {
				_job.writeLog("IMS Response Error, rcCode=" + rcCode);
				throw new Exception("IMS Response Error");
				//return;
			}
		} catch (Exception e) {
			_job.writeLog("ProcessMAC Error, " + e.getMessage());
			_logData.setProgramException(e);
			// 不得直接呼叫FEPBase.SendEMS
			BatchJobLibrary.sendEMS(_logData);
			throw e;
		}
	}

	private void ProcessPPK() throws Exception {
		String date = FormatUtil.dateTimeFormat(Calendar.getInstance(), "yyMMdd");
		String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), "HHmmss");
		String seq = time.substring(0, 4);
		// 1.組上主機電文
		String rqkTita = GetIMSTitaForP3(date, time, "01", seq, "AA", "IN", "00000000", "000000");
		_job.writeLog("Prepare First Tita(ascii):" + rqkTita);
		String tita = EbcdicConverter.toHex(CCSID.English, rqkTita.length(), rqkTita);
		_job.writeLog("Prepare First Tita(Ebcdic):" + tita);
		// System.out.println(rqkTita);
		// 2. 上送主機
		try {

			IMSAdapter adapter = new IMSAdapter(_atmData);
			adapter.setCBSTxid("ChangeKey");
			adapter.setMessageToIMS(tita);

			FEPReturnCode rtnCode = adapter.sendReceive();
			if (rtnCode != FEPReturnCode.Normal) {
				_job.writeLog("Send First IMS Fail,FEPReturnCode:" + rtnCode);
				throw new Exception("Send First IMS Fail");
				//return;
			}

			// 3.拆解第一道主機回應電文
			String response = adapter.getMessageFromIMS();
			// String response =
			// "C1D4C6C5D7F0F140C6D7D5F2F3F0F1F3F0F0F9F5F1F5F2F0F0F0F1F0F1F4F0F0F0F0C3F3F7F0C1C3F6F1C6C2F5F6C3C2C3F5F6F4F2F3F7C3F3C6F4F6F7F2C2C2F4F840404040404040404040404040404040F1F8F8F9F8F7C4F0";
			_job.writeLog("Get First IMS Response:" + response);
			if (response != null && !response.isEmpty()) {
				_job.writeLog("First Response ASCII:" + EbcdicConverter.fromHex(CCSID.English, response));
				if (!response.startsWith("C1D4C6C5D7")) {
					throw new Exception("IMS Response Error");
				}
			}
			String msgType = "";
			String rcCode = "";
			if(StringUtils.isNotBlank(response)) {
				msgType = response.substring(18, 22);
				rcCode = response.substring(60, 68);
			}

			String ppKey = "";
			String macCode = "";
			String kek = "";
			if (msgType.equals("D7D5") && rcCode.equals("F0F0F0F0")) { // FPN & 0000 //
				kek = EbcdicConverter.fromHex(CCSID.English, response.substring(68, 80));
				ppKey = EbcdicConverter.fromHex(CCSID.English, response.substring(80, 240)); // double length
				macCode = EbcdicConverter.fromHex(CCSID.English, response.substring(280, 296));
				_job.writeLog("Get Data from IMS, KeyCheck:" + kek + ", PPK:" + ppKey + ", mac:" + macCode);
				//String inputData2 = "0060000000000000";
				RefString newKey = new RefString(null);
				if (changePPKey(ppKey, kek, newKey) != FEPReturnCode.Normal) {
					throw new Exception("Check First IMS MAC Error,Abort");
				}
				// 第一道回應keyid 先暫時寫IMS for checkmak及壓confirm mac
				String bankId = StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS");
				UpdateKey(bankId, "T2", "PPK", newKey.get(), true);
				// String inputData = EbcdicConverter.toHex(CCSID.English, 16,
				// "0060000000000000");

				// 組第2道Confirm
				date = FormatUtil.dateTimeFormat(Calendar.getInstance(), "yyMMdd");
				time = FormatUtil.dateTimeFormat(Calendar.getInstance(), "HHmmss");
				RefString newMac = new RefString(null);
				String inputData = StringUtils.rightPad("P3FSN" + date + time, 24, "0");
				rtnCode = makeIMSMac(bankId, inputData, newMac);
				if (rtnCode != FEPReturnCode.Normal) {
					throw new Exception("Make Second IMS MAC Error");
				}

				String rqkTita2 = GetIMSTitaForP3(date, time, "02", seq, "SN", "NN", newMac.get(), kek);
				_job.writeLog("Prepare Second Tita(ascii):" + rqkTita2);
				String tita2 = EbcdicConverter.toHex(CCSID.English, rqkTita2.length(), rqkTita2);
				_job.writeLog("Prepare Second Tita(Ebcdic):" + tita2);
				IMSAdapter adapter2 = new IMSAdapter(_atmData);
				adapter2.setCBSTxid("ChangeKey");
				adapter2.setMessageToIMS(tita2);
				// 送第2道Confirm
				rtnCode = adapter2.sendReceive();
				if (rtnCode != FEPReturnCode.Normal) {
					throw new Exception("Send Second IMS Fail,FEPReturnCode:" + rtnCode);
				}
				response = adapter2.getMessageFromIMS();
				_job.writeLog("Get Second IMS Response:" + response);
				if (response != null && !response.isEmpty() &&
						response.startsWith("C1D4C6C5D7")) {
					String resAsc = EbcdicConverter.fromHex(CCSID.English, response);
					_job.writeLog("Second Response ASCII:" + resAsc);
					// response = StringUtil.fromHex(response);
					msgType = response.substring(18, 22);
					String tdseg = response.substring(54, 58); // 第2道
					rcCode = response.substring(60, 68);
					if (msgType.equals("D7C3") &&
							tdseg.equals("F0F2") &&
							rcCode.equals("F0F0F0F0")) { // PC & 02 & 0000 //Confirm
						// Response (FPC)交易成功
						_job.writeLog("IMS Confirm Response OK, MSGTYPE=" + resAsc.substring(9, 11) + ",RC="
								+ resAsc.substring(30, 34));
						// // 主機回成功更新原IMS KEY
						UpdateKey(bankId,"T2", "PPK", "", false);
					} else {
						_job.writeLog(
								"IMS Confirm Response Error, MSGTYPE=" + resAsc.substring(9, 11) + ",RC="
										+ resAsc.substring(30, 34));
						//return;
						throw new Exception("IMS Confirm Response Error");
					}
				} else {
					_job.writeLog("IMS Confirm Response Error");
					//return;
					throw new Exception("IMS Confirm Response Error");
				}

			} else if (msgType.equals("D7C3")) { // Response (FPC)交易失敗
				_job.writeLog("IMS Response Error, MSGTYPE=PC," + rcCode);
				//return;
				throw new Exception("IMS Response Error");
			} else {
				_job.writeLog("IMS Response Error, rcCode=" + rcCode);
				throw new Exception("IMS Response Error");
				//return;
			}
		} catch (Exception e) {
			_job.writeLog("ProcessPPK Error, " + e.getMessage());
			_logData.setProgramException(e);
			// 不得直接呼叫FEPBase.SendEMS
			BatchJobLibrary.sendEMS(_logData);
			_job.writeLog("ProcessPPK Error, " + e.getMessage());
			throw e;
		}

	}

	private FEPReturnCode changeKey(String inputData1, String inputData2, String macCode, RefString newKey) {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			_job.writeLog("Begin Check IMS MAC:" + macCode);
			// 1.建立ENCHelper物件
			ATMENCHelper encHelper = new ATMENCHelper(_atmData);

			// inputData2 = EbcdicConverter.toHex(CCSID.English, inputData2.length(),
			// inputData2);
			// 3.呼叫
			RefString mac = new RefString(null);
			rtnCode = encHelper.changeKeyForCbs(inputData1, inputData2, newKey, mac);

			// 4.若rtnCode=normal
			if (rtnCode == FEPReturnCode.Normal) {

				// macCode = EbcdicConverter.fromHex(CCSID.English, macCode);
				if (mac.get().equals(macCode))
					return FEPReturnCode.Normal;
				else {
					_job.writeLog("compare mac error, origin mac:[" + macCode + "], calc mac:[" + mac.get() + "]");
					return FEPReturnCode.ENCCheckMACError;
				}
			}
		} catch (Exception e) {
			// rtnCode = handleException(e, "makeATMMac");
			_job.writeLog("changeKey Error, " + e.getMessage());
			rtnCode = FEPReturnCode.ENCLibError;
		}
		return rtnCode;
	}

	private FEPReturnCode changePPKey(String ppk, String kek,  RefString newKey) {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {
			//_job.writeLog("Begin Check IMS MAC:" + macCode);
			// 1.建立ENCHelper物件
			ATMENCHelper encHelper = new ATMENCHelper(_atmData);

			// inputData2 = EbcdicConverter.toHex(CCSID.English, inputData2.length(),
			// inputData2);
			// 3.呼叫
			RefString newKek = new RefString(null);
			rtnCode = encHelper.changePPKeyForCbs(ppk, newKey, newKek);

			// 4.若rtnCode=normal
			if (rtnCode == FEPReturnCode.Normal) {

				// macCode = EbcdicConverter.fromHex(CCSID.English, macCode);
				if (newKek.get().equals(kek))
					return FEPReturnCode.Normal;
				else {
					_job.writeLog("compare key check value error, origin KeyCheckValue:[" + kek + "], new KeyCheckValue:[" + newKek.get() + "]");
					return FEPReturnCode.ENCChangePPKeyError;
				}

			}
		} catch (Exception e) {
			// rtnCode = handleException(e, "makeATMMac");
			_job.writeLog("changePPKey Error, " + e.getMessage());
			rtnCode = FEPReturnCode.ENCLibError;
		}
		return rtnCode;
	}

	private FEPReturnCode makeIMSMac(String bankId, String inputData, RefString mac) {
		FEPReturnCode rtnCode = FEPReturnCode.Normal;
		try {

			ATMENCHelper encHelper = new ATMENCHelper(_atmData);
			inputData = EbcdicConverter.toHex(CCSID.English, inputData.length(), inputData);
			// inputData = StringUtil.toHex(inputData);
			rtnCode = encHelper.makeCbsMac(inputData, true, mac);
			if (rtnCode == FEPReturnCode.Normal) {
				_job.writeLog("Make IMS MAC OK, mac:" + mac.get());
				// String macData = mac.get().substring(4);
				rtnCode = FEPReturnCode.Normal;
			} else {
				rtnCode = FEPReturnCode.ENCLibError;
			}

			return rtnCode;

		} catch (Exception e) {
			// rtnCode = handleException(e, "makeATMMac");
			_job.writeLog("checkIMSMac Error, " + e.getMessage());
			rtnCode = FEPReturnCode.ENCLibError;
		}
		return rtnCode;
	}

	private String GetIMSTitaForMK(String reqDate, String reqTime, String TDRSEG, String TRANSEQ,
								   String MSGTYP, String status, String mac) {

		String rqkTita = "IBPBM000" + // APTRAN
				" " + //
				_wsid + // WSID
				"0" + // MACMODE
				"1" + // RECFMT
				" " + // AppUse
				"F" + // MSGCAT
				MSGTYP + // MSGTYP
				reqDate +
				reqTime +
				TRANSEQ + // TRANSEQ
				TDRSEG + // TDRSEG第幾道
				status + "0000000000000" + // STATUS
				"00" + // PTRIES
				StringUtils.rightPad(" ", 16) + // EPIN
				"01" + // LANGID
				"MK" + // FSCODE
				"  " + // FACODE
				StringUtils.rightPad(" ", 20) + // FADATA
				"  " + // TACODE
				StringUtils.rightPad(" ", 20) + // TADATA
				"00000000000" + // AMTNOND
				"0000000000" + // AMTDISP
				" " + // DOCLASS
				"9" + // CARDFMT
				StringUtils.rightPad("", 117, '0') + // CARDDATA
				"00" + mac; // Reserved

		// // For MAC
		// if (TDRSEG == "01")
		// rqkTita += "00000000";
		// else {
		// String newMac = "";

		// rqkTita += newMac;
		// }
		return rqkTita;
	}

	private String GetIMSTitaForP3(String reqDate, String reqTime, String TDRSEG, String TRANSEQ,
								   String MSGTYP, String status, String mac, String checkValue) {

		String rqkTita = "IBPBM000" + // APTRAN
				" " + //
				_wsid + // WSID
				"0" + // MACMODE
				"1" + // RECFMT
				" " + // AppUse
				"F" + // MSGCAT
				MSGTYP + // MSGTYP
				reqDate +
				reqTime +
				TRANSEQ + // TRANSEQ
				TDRSEG + // TDRSEG第幾道
				status + "0000000000000" + // STATUS
				"00" + // PTRIES
				StringUtils.rightPad(" ", 16) + // EPIN
				"01" + // LANGID
				"P3" + // FSCODE
				"  " + // FACODE
				StringUtils.rightPad(" ", 20) + // FADATA
				"  " + // TACODE
				StringUtils.rightPad(" ", 20) + // TADATA
				"00000000000" + // AMTNOND
				"0000000000" + // AMTDISP
				" " + // DOCLASS
				"9" + // CARDFMT
				StringUtils.rightPad("", 114, '0') +  // DUMMY
				checkValue +
				mac;

		rqkTita = StringUtils.rightPad(rqkTita, 500, ' ') + "AMFEP00";

		return rqkTita;
	}

	private void UpdateKey(String bankId, String keytype, String keykind, String newKey, boolean isPending) {
		//String keytype = "T2";
		//String keykind = "MAC";
		String keyfn = "IMS";
		Enckey enckey = endkeyExtMapper.selectByPrimaryKey(bankId, keytype, keykind, keyfn);
		if (enckey != null) {
			if (isPending) {
				enckey.setPendingkey(newKey);
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);
				enckey.setBegindate(FormatUtil.dateTimeFormat(cal, "yyyyMMdd"));
				enckey.setUpdatedate(new Date());
				endkeyExtMapper.updateByPrimaryKey(enckey);
				_job.writeLog("Update ENCKEY Pending OK,bankId=" + bankId);
			} else {
				// 從pending搬到current
				enckey.setCurkey(enckey.getPendingkey());
				enckey.setBegindate("");
				enckey.setUpdatedate(new Date());
				endkeyExtMapper.updateByPrimaryKey(enckey);
				_job.writeLog("Update ENCKEY Curkey from Pendingkey OK,bankId=" + bankId);
			}

		} else {
			enckey = new Enckey();
			enckey.setBankid(bankId);
			enckey.setKeytype(keytype);
			enckey.setKeykind(keykind);
			enckey.setKeyfn(keyfn);
			enckey.setUpdatedate(new Date());
			//enckey.setPendingkey(newKey);
			if (isPending)
				enckey.setPendingkey(newKey);
			else
				enckey.setCurkey(newKey);
			endkeyExtMapper.insertSelective(enckey);
			_job.writeLog("Insert ENCKEY OK,bankId=" + bankId);
		}
	}
}
