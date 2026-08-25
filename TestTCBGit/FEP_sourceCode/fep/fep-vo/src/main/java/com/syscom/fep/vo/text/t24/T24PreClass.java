package com.syscom.fep.vo.text.t24;

import static com.syscom.fep.vo.constant.T24Version.B0001;
import static com.syscom.fep.vo.constant.T24Version.B0002;
import static com.syscom.fep.vo.constant.T24Version.B0003;
import static com.syscom.fep.vo.constant.T24Version.B0005;
import static com.syscom.fep.vo.constant.T24Version.B4000;
import static com.syscom.fep.vo.constant.T24Version.B5000;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.T24TxType;

public class T24PreClass extends T24TextBase {
	private String _userName = "ATM.USER";
	private String _sscode = "123456";

	private String _company;
	private String _application = "TMB.CHL.FUNDS.TRANSFER";
	private String _version;
	// 配合T24電文GTS.CONTROL欄位改送NULL
	private String _operation = "PROCESS/NULL";
	// T24增加授權人數
	private String _auNumber = "0"; // 授權人數
	private String _function;
	private boolean _reverseTag;
	private String _transactionId;
	private String _processingStatus;
	@SuppressWarnings("unused")
	private String _CTS_CONTROL;

	private boolean _enquiryTag;
	private String _enquiryName;

	private boolean _routineTag;
	private String _routineName;
	private String _routineReqValue;
	private String _routineRspValue;

	private T24TITAHeader _titaHeader = new T24TITAHeader();
	private HashMap<String, String> _titaBody = new HashMap<String, String>();

	private T24TOTAHeader _totaHeaderRsp = new T24TOTAHeader();
	private HashMap<String, String> _totaBodyRsp = new HashMap<String, String>();

	private HashMap<String, String> _titaTransInput;
	private HashMap<String, String> _titaTransCondition;
	private HashMap<String, String> _titaTransReverse;

	private T24Class _t24req;
	private T24Class _t24rsp;
	private T24Class _t24reqForMQ;

	private boolean _totaTransSuccess;
	private HashMap<String, String> _totaHeader;
	private HashMap<String, String> _totaTransContent;
	private HashMap<String, String> _totaTransResult;
	private HashMap<String, String> _totaEnquiryContent;
	private ArrayList<HashMap<String, String>> _totaEnquiryContents;
	// private DataTable _totaRecords;
	private T24TxType _txType;

	/**
	 * T24電文UserName
	 * 
	 * @return String
	 *         <remark>T24電文之SecurityContext資訊</remark>
	 */
	public String getUserName() {
		return _userName;
	}

	public void setUserName(String value) {
		_userName = value;
	}

	/**
	 * T24電文Password
	 * 
	 * @return String <remark>T24電文之SecurityContext資訊</remark>
	 */
	public String getPassword() {
		return _sscode;
	}

	public void setPassword(String value) {
		_sscode = value;
	}

	/**
	 * T24電文Company資料
	 * 
	 * @return String <remark>T24電文之SecurityContext資訊</remark>
	 */
	public String getCompany() {
		return _company;
	}

	public void setCompany(String value) {
		_company = value;
	}

	/**
	 * T24電文ofsTransactionInput-Application資料
	 * 
	 * @return String <remark>T24電文之ofsTransactionInput資訊</remark>
	 */
	public String getApplication() {
		return _application;
	}

	public void setApplication(String value) {
		_application = value;
	}

	/**
	 * T24電文ofsTransactionInput-Version資料
	 * 
	 * @return String <remark>T24電文之ofsTransactionInput資訊</remark>
	 */
	public String getVersion() {
		return _version;
	}

	public void setVersion(String value) {
		_version = value;
	}

	/**
	 * T24 TITA common Header
	 * 
	 * @return T24TITAHeader <remark>將置於T24電文之ofsTransactionInput中</remark>
	 */
	public T24TITAHeader getTITAHeader() {
		return _titaHeader;
	}

	public void setTITAHeader(T24TITAHeader value) {
		_titaHeader = value;
	}

	/**
	 * T24 TITA Body
	 * 
	 * @return Dictionary <remark>將置於T24電文之ofsTransactionInput中</remark>
	 */
	public HashMap<String, String> getTITABody() {
		return _titaBody;
	}

	public void setTITABody(HashMap<String, String> value) {
		_titaBody = value;
	}

	/**
	 * T24電文ofsTransactionInput-Operation資料
	 * 
	 * @return String <remark>T24電文之ofsTransactionInput資訊</remark>
	 */
	public String getOperation() {
		return _operation;
	}

	public void setOperation(String value) {
		_operation = value;
	}

	/**
	 * Function：S(See), I(Input), A(Authorize), D(Delete), R(Reverse), H(History
	 * Restore), V(Verify)
	 */
	public String getT24Function() {
		return _function;
	}

	public void setT24Function(String value) {
		_function = value;
	}

	/**
	 * 是否使用ofsTransactionReverse功能
	 * 
	 * @return Boolean <remark>T24TransactionReverse功能</remark>
	 */
	public boolean getReverseTag() {
		return _reverseTag;
	}

	public void setReverseTag(boolean value) {
		_reverseTag = value;
	}

	/**
	 * 是否使用ofsStandardEnquiryt功能
	 * 
	 * @return Boolean <remark>T24StandardEnquiry功能</remark>
	 */
	public boolean getEnquiryTag() {
		return _enquiryTag;
	}

	public void setEnquiryTag(boolean value) {
		_enquiryTag = value;
	}

	/**
	 * 是否使用ofsStandardRoutine功能
	 * 
	 * @return Boolean <remark>T24StandardRoutine功能</remark>
	 */
	public boolean getRoutineTag() {
		return _routineTag;
	}

	public void setRoutineTag(boolean value) {
		_routineTag = value;
	}

	/**
	 * ofsTransactionReverse功能所需之TransactionId
	 * 
	 * @return String <remark>T24TransactionReverse功能所需之TransactionId</remark>
	 */
	public String getTransactionId() {
		return _transactionId;
	}

	public void setTransactionId(String value) {
		_transactionId = value;
	}

	/**
	 * ofsTransactionReverse功能所需之TransactionId
	 * 
	 * @return String <remark>T24TransactionReverse功能所需之TransactionId</remark>
	 */
	public String getProcessingStatus() {
		return _processingStatus;
	}

	public void setProcessingStatus(String value) {
		_processingStatus = value;
	}

	/**
	 * ofsStandardEnquiry功能所需之Name
	 * 
	 * @return String <remark>T24StandardEnquiry功能所需之Name</remark>
	 */
	public String getEnquiryName() {
		return _enquiryName;
	}

	public void setEnquiryName(String value) {
		_enquiryName = value;
	}

	/**
	 * ofsStandardRoutine功能所需之Name
	 * 
	 * @return String <remark>T24StandardRoutine功能所需之Name</remark>
	 */
	public String getRoutineName() {
		return _routineName;
	}

	public void setRoutineName(String value) {
		_routineName = value;
	}

	/**
	 * ofsStandardRoutine功能查詢所需之Value
	 * 
	 * @return String <remark>T24StandardRoutine功能查詢所需之Value</remark>
	 */
	public String getRoutineReqValue() {
		return _routineReqValue;
	}

	public void setRoutineReqValue(String value) {
		_routineReqValue = value;
	}

	/**
	 * ofsStandardRoutine功能查詢結果所需之Value
	 * 
	 * @return String <remark>T24StandardRoutine功能查詢結果所需之Value</remark>
	 */
	public String getRoutineRspValue() {
		return _routineRspValue;
	}

	public void setRoutineRspValue(String value) {
		_routineRspValue = value;
	}

	/**
	 * Parsing request電文的ofsTransactionInput資料
	 * 
	 * @return Dictionary
	 *         <remark>內含application/version/operation/transactionID</remark>
	 */
	public HashMap<String, String> getTITATransInput() {
		return _titaTransInput;
	}

	public void setTITATransInput(HashMap<String, String> value) {
		_titaTransInput = value;
	}

	/**
	 * Parsing request電文的ofsTransactionInput資料
	 * 
	 * @return Dictionary <remark>Header/Body</remark>
	 */
	public HashMap<String, String> getTITATransCondition() {
		return _titaTransCondition;
	}

	public void setTITATransCondition(HashMap<String, String> value) {
		_titaTransCondition = value;
	}

	public HashMap<String, String> getTITATransReverse() {
		return _titaTransReverse;
	}

	public void setTITATransReverse(HashMap<String, String> value) {
		_titaTransReverse = value;
	}

	/**
	 * T24 response instance
	 * 
	 * @return T24 <remark>response</remark>
	 */
	public T24Class getT24Rsp() {
		return _t24rsp;
	}

	public void setT24Rsp(T24Class value) {
		_t24rsp = value;
	}

	/**
	 * T24 request instance
	 * 
	 * @return T24 <remark>request</remark>
	 */
	public T24Class getT24Req() {
		return _t24req;
	}

	public void setT24Req(T24Class value) {
		_t24req = value;
	}

	/**
	 * T24 request instance
	 * 
	 * @return T24 <remark>request</remark>
	 */
	public T24Class getT24ReqForMQ() {
		return _t24reqForMQ;
	}

	public void setT24ReqForMQ(T24Class value) {
		_t24reqForMQ = value;
	}

	/**
	 * 取得交易是否成功
	 * 
	 * @return Boolean <remark>交易成功標示</remark>
	 */
	public boolean getTOTATransSuccess() {
		return _totaTransSuccess;
	}

	public void setTOTATransSuccess(boolean value) {
		_totaTransSuccess = value;
	}

	/**
	 * T24電文ofsmlHeader內含資訊
	 * 
	 * @return Dictionary <remark>ofsmlHeader</remark>
	 */
	public HashMap<String, String> getTOTAHeader() {
		return _totaHeader;
	}

	public void setTOTAHeader(HashMap<String, String> value) {
		_totaHeader = value;
	}

	/**
	 * T24電文ofsTransactionProcessed內含資訊
	 * 
	 * @return Dictionary
	 *         <remark>Application/version/processingStatus/function/operation</remark>
	 */
	public HashMap<String, String> getTOTATransContent() {
		return _totaTransContent;
	}

	public void setTOTATransContent(HashMap<String, String> value) {
		_totaTransContent = value;
	}

	/**
	 * T24電文ofsTransactionProcessed內含資訊
	 * 
	 * @return Dictionary
	 *         <remark>Application/version/processingStatus/function/operation</remark>
	 */
	public ArrayList<HashMap<String, String>> getTotaEnquiryContents() {
		return _totaEnquiryContents;
	}

	public void setTotaEnquiryContents(ArrayList<HashMap<String, String>> value) {
		_totaEnquiryContents = value;
	}

	/**
	 * T24電文ofsTransactionProcessed內含資訊
	 * 
	 * @return Dictionary <remark>Header/Body</remark>
	 */
	public HashMap<String, String> getTOTATransResult() {
		return _totaTransResult;
	}

	public void setTOTATransResult(HashMap<String, String> value) {
		_totaTransResult = value;
	}

	/**
	 * T24電文ofsStandardEnquiry內含資訊
	 * 
	 * @return Dictionary <remark>name/stauts</remark>
	 */
	public HashMap<String, String> getTOTAEnquiryContent() {
		return _totaEnquiryContent;
	}

	public void setTOTAEnquiryContent(HashMap<String, String> value) {
		_totaEnquiryContent = value;
	}

	// /**
	// T24電文ofsStandardEnquiry內含資訊
	//
	// @return DataTable
	// <remark>List data</remark>
	// */
	// public DataTable getTOTARecords()
	// {
	// return _totaRecords;
	// }
	// public void setTOTARecords(DataTable value)
	// {
	// _totaRecords = value;
	// }
	// TODO
	/**
	 * T24電文ofsmlHeader內含資訊
	 * 
	 * @return Dictionary <remark>ofsmlHeader</remark>
	 */
	public T24TOTAHeader getTOTAHeaderRsp() {
		return _totaHeaderRsp;
	}

	public void setTOTAHeaderRsp(T24TOTAHeader value) {
		_totaHeaderRsp = value;
	}

	/**
	 * T24 TOTA Body
	 * 
	 * @return Dictionary <remark>將置於T24電文之ofsTransactionProcessed中</remark>
	 */
	public HashMap<String, String> getTOTABodyRsp() {
		return _totaBodyRsp;
	}

	public void setTOTABodyRsp(HashMap<String, String> value) {
		_totaBodyRsp = value;
	}

	public T24TxType getTxType() {
		return _txType;
	}

	public void setTxType(T24TxType value) {
		_txType = value;
	}

	public T24PreClass() {

	}

	public T24PreClass(String company, String version, Boolean reverseTag, String transactionId, String operation) {
		// 針對request建立相關instance
		_company = company;
		_version = version;
		_reverseTag = reverseTag;
		_transactionId = transactionId;
		// 沒傳值預設為PROCESS，有傳值以傳入的值為主
		if (StringUtils.isNotBlank(operation)) {
			_operation = operation;
		}
		_titaHeader = new T24TITAHeader();
		_titaBody = new HashMap<String, String>();
	}

	public String getGenT24ReqOFS() {
		StringBuilder sb = new StringBuilder();
		String result = "";
		try {
			// Dim securityContext As New T24ServiceRequestSecurityContext
			// _t24req.ServiceRequest.SecurityContext = securityContext

			// _t24req.ServiceRequest.SecurityContext.company = _company

			// OPERATION,OPTIONS,USER INFORMATION,ID INFORMATION,DATA

			// 增加 reverse 的功能
			if (_reverseTag && getTxType().getValue() == T24TxType.EC.getValue()) {
				// OPERATION
				sb.append(_application + ",");
				// OPTIONS
				// 2010/11/18 by Ed for 組電文格式有誤
				sb.append("CHL." + _version + "/R/" + _operation + "/" + _auNumber + ",");
				// USER INFORMATION
				sb.append(_userName + "/" + _sscode + "/" + _company + ",");
				// ID INFORMATION
				sb.append(_transactionId);
				result = sb.toString();
			} else if (_routineTag) {
				sb.append(_routineName + ",,");
				sb.append(_userName + "/" + _sscode + "/" + _company + ",,");
				sb.append(_routineReqValue);
				result = sb.toString();
			} else {
				if (_version.substring(0, 1).equals("A")) {
					// OPERATION
					sb.append(_application + ",");
					// OPTIONS
					sb.append(_version + "/I/" + _operation + "/" + _auNumber + ",");
					// USER INFORMATION
					sb.append(_userName + "/" + _sscode + "/" + _company + ",");
					// ID INFORMATION
					sb.append(_transactionId + ",");
					// DATA
					sb.append("TI.CHNN.CODE::=" + _titaHeader.getTiChnnCode() + ",");
					sb.append("TI.CHNN.CODE.S::=" + _titaHeader.getTiChnnCodeS() + ",");
					// sb.Append("TRM.BRANCH::=" + _titaHeader.TRM_BRANCH + ",")
					sb.append("TRMNO::=" + _titaHeader.getTRMNO() + ",");
					sb.append("EJFNO::=" + _titaHeader.getEJFNO() + ",");
					sb.append("FISC.DATE::=" + _titaHeader.getFiscDate() + ",");
					sb.append("REG.FLAG::=" + _titaHeader.getRegFlag() + ",");
					// sb.Append("FEP.USER.ID::=" + _titaHeader.FEP_USER_ID + ",")

					String[] strArray = null;
					if (_titaBody.size() > 0) {
						for (String key : _titaBody.keySet()) {
							if (key.indexOf('_') != -1) {
								strArray = key.split("_");
								sb.append(strArray[0] + ":" + strArray[1] + ":=" + _titaBody.get(key) + ",");
							} else {
								sb.append(key + "::=" + _titaBody.get(key) + ",");
							}
						}
						result = sb.substring(0, sb.toString().length() - 1);
					}

				} else {
					// 2018/11/15 Modify by Ruling for 2566約定及核驗服務類別10，增加組B0005(跨行金融帳戶資訊核驗(2566))電文
					switch (_version) {
						case B0001:
						case B0002:
						case B0005:
							// ENQUIRY.SELECT,,ATM.USER/123456/TW8070778,TMB.ENQ.CHL.ACCT.BAL,ACCT.NO:EQ=00600300000155,CURRENCY:EQ=TWD
							sb.append("ENQUIRY.SELECT,,");
							sb.append(_userName + "/" + _sscode + "/" + _company + ",");
							sb.append(_enquiryName + ",");
							if (_titaBody.size() > 0) {
								for (String key : _titaBody.keySet()) {
									sb.append(key + ":EQ=" + _titaBody.get(key) + ",");
								}
								result = sb.substring(0, sb.toString().length() - 1);
							}
							break;
						case B0003:
							// ENQUIRY.SELECT,,ATM.USER/123456/TW8070778,TMB.ENQ.CHL.ACCT.REG,ACCT.NO:EQ=00600300000155
							sb.append("ENQUIRY.SELECT,,");
							sb.append(_userName + "/" + _sscode + "/" + _company + ",");
							sb.append(_enquiryName + ",");
							if (_titaBody.size() > 0) {
								for (String key : _titaBody.keySet()) {
									sb.append(key + ":EQ=" + _titaBody.get(key) + ",");
								}
								result = sb.substring(0, sb.toString().length() - 1);
							}
							break;
						case B4000:
							// Lock
							// AC.LOCKED.EVENTS,B4000/I/PROCESS,ATM.USER/123456/TW8070778,,ACCOUNT.NUMBER=12100300911372,LOCKED.AMOUNT=1000,DESCRIPTION=ATM TEST LOCK
							// UnLock
							// AC.LOCKED.EVENTS,B4000/R/PROCESS/2,ATM.USER/123456/TW8070778,ACLK1112457002
							sb.append("AC.LOCKED.EVENTS,");
							if (getTxType() == T24TxType.Authorized) {
								sb.append("TMB." + _version + "/I/" + _operation + "/" + _auNumber + ",");
								sb.append(_userName + "/" + _sscode + "/" + _company + ",,");
								if (_titaBody.size() > 0) {
									for (String key : _titaBody.keySet()) {
										sb.append(key + "=" + _titaBody.get(key) + ",");
									}
									result = sb.substring(0, sb.toString().length() - 1);
								}
							} else {
								sb.append("TMB." + _version + "/R/" + _operation + "/" + _auNumber + ",");
								sb.append(_userName + "/" + _sscode + "/" + _company + ",");
								sb.append(_transactionId);
								result = sb.toString();
							}
							break;
						case B5000:
							// TMB.FWD.TXN.REGISTER,TMB.TXN.INP/I/PROCESS/2/0,ATM.USER/123456/TW8070778,,XXXXXXXXXXXXXXXXXXXXXXXXXXXX
							sb.append("TMB.FWD.TXN.REGISTER,TMB.TXN.INP/I/" + _operation + "/" + _auNumber + ",");
							sb.append(_userName + "/" + _sscode + "/" + _company + ",");
							sb.append(_enquiryName + ",");
							sb.append("TI.CHNN.CODE::=" + _titaHeader.getTiChnnCode() + ",");
							sb.append("TI.CHNN.CODE.S::=" + _titaHeader.getTiChnnCodeS() + ",");
							if (_titaBody.size() > 0) {
								// For Each pair In _titaBody
								// sb.Append(pair.Key + "::=" + pair.Value + ",")
								// Next
								String[] strArray = null;
								for (String key : _titaBody.keySet()) {
									if (key.indexOf('_') != -1) {
										strArray = key.split("_");
										sb.append(strArray[0] + ":" + strArray[1] + ":=" + _titaBody.get(key) + ",");
									} else {
										sb.append(key + "::=" + _titaBody.get(key) + ",");
									}
								}

								result = sb.substring(0, sb.toString().length() - 1);
							}
							break;
					}

				}
			}
			return result;
		} catch (Exception ex) {
			LogHelperFactory.getTraceLogger().exceptionMsg(ex, ex.getMessage());
			return null;
		}
	}

	public boolean parseT24RspOfsForBType(String rsp, String version) {
		// B0001,B0003
		// ,RESULT::RESULT/ACT.NO::ACT.NO/CURRENCY::CURRENCY/WORKING.BAL::WORKING.BAL/AVAILABLE.BAL::AVAILABLE.BAL,"0000
		// " "0000600300000155" "TWD" " 9770122.18" " 9770122.18"
		// B4000
		// ACLK1112457002//1,ACCOUNT.NUMBER=12100300911372:1:1,DESCRIPTION=ATM TEST
		// LOCK:1:1,FROM.DATE=20110504:1:1,LOCKED.AMOUNT=1000:1:1,CURR.NO=1:1:1,INPUTTER=6770_ATMUSER__OFS_GCS:1:1,DATE.TIME=1010221802:1:1,AUTHORISER=6770_ATMUSER_OFS_GCS:1:1,CO.CODE=TW8070778:1:1,DEPT.CODE=16:1:1
		// B8888
		// 1. NOT.FOUND
		// 2. LFT1112300P90MLJL;20110503;20110503

		String[] strRsp = null;
		String[] strTrans = null;
		_totaTransResult = new HashMap<String, String>();
		HashMap<String, String> content = null;
		_totaEnquiryContents = new ArrayList<HashMap<String, String>>();
		try {
			// 拆解TMBI.AC.OUTAC.ENQ(T24查詢帳號狀況)
			// 2566約定及核驗服務類別10，增加拆解B0005(跨行金融帳戶資訊核驗(2566))電文
			switch (version) {
				case "B0001":
				case "B0003":
				case "B0005":
				case "TMB.AC.BAL.DTL.LIST.ENQ":
				case "B0002":
				case "TMBI.AC.OUTAC.ENQ":
					strRsp = rsp.split(",");
					// 改>1以避免下一行直接抓不出陣列索引值的錯誤
					if (strRsp.length > 1) {
						String[] header = strRsp[1].split("/");
						for (int j = 2; j < strRsp.length; j++) {
							// 2012/6/27 update by ashiang,增加T24回中文訊息的判斷
							if (strRsp[j].toUpperCase().indexOf("No Record".toUpperCase()) > -1
									|| strRsp[j].indexOf("沒有符合的記錄") > -1) {
								_totaTransResult.put("EB.ERROR", strRsp[j + 1].replace("\"", ""));
								// 2013/06/28 Modify by Ruling for 港澳NCB:B001電文回No
								// Record時要將EB.ERROR的值拆解到FEPTXN_CBS_RC
								if ("B0001".equals(version)) {
									return true;
								} else {
									return false;
								}
							}
							// 2021-06-15 Richard modified start
							String data = strRsp[j].substring(1, strRsp[j].length() - 1);
							StringTokenizer token = new StringTokenizer(data, "\"\t\"");
							content = new HashMap<String, String>();
							int i = 0;
							while (token.hasMoreTokens()) {
								content.put(
										header[i].substring(0, header[i].indexOf("::") == -1 ? 0 : header[i].indexOf("::")),
										token.nextToken().trim());
								i++;
							}
							// 2021-06-15 Richard modified end
							_totaEnquiryContents.add(content);
						}
						_totaTransResult.put("EB.ERROR", NormalRC.FEP_OK);
					} else {
						_totaTransResult.put("EB.ERROR", FEPReturnCode.CBSResponseError.toString());
						return false;
					}
					break;
				case "B4000":
					// 2011/09/27 Modify by Ruling for ADD B4000 拆解下行電文
					// 2013/08/23 Modify by Ruling for B4000 回應電文拆正確解錯誤代碼
					strRsp = rsp.split(",");
					if (strRsp.length > 0) {
						strTrans = strRsp[0].split("/");
						if (strTrans.length > 2) {
							switch (strTrans[2]) {
								// 表交易成功
								case "1":
									int tidSep = strRsp[0].indexOf("/");
									if (tidSep > 0) {
										content = new HashMap<String, String>();
										_totaTransResult.put("transactionId", strRsp[0].substring(0, tidSep));
										for (int i = 1; i < strRsp.length; i++) {
											String[] data = strRsp[i].split("=");
											int dataSep = data[1].indexOf(":");
											if (dataSep > -1) {
												content.put(data[0], data[1].substring(0, dataSep));
											} else {
												content.put(data[0], data[1]);
											}
										}
										_totaEnquiryContents.add(content);
										_totaTransResult.put("EB.ERROR", NormalRC.FEP_OK);
									} else {
										_totaTransResult.put("EB.ERROR", rsp);
										return false;
									}
									break;
								default:
									StringBuffer sb = new StringBuffer();
									for (int i = 0; i < strRsp.length; i++) {
										sb.append(strRsp[i]);
										if (i < strRsp.length - 1) {
											sb.append(",");
										}
									}
									String erMsg = sb.toString();
									_totaTransResult.put("EB.ERROR", erMsg);
									break;
							}
						}
					}
					break;
				case "B8888":
					if (rsp.equals("NOT.FOUND")) {
						_totaTransResult.put("EB.ERROR", NormalRC.FEP_OK);
						_totaTransResult.put("Result1", "NOT.FOUND");
					} else {
						if (rsp.indexOf(";") == -1) {
							_totaTransResult.put("EB.ERROR", FEPReturnCode.CBSResponseError.toString());
							return false;
						}
						_totaTransResult.put("EB.ERROR", NormalRC.FEP_OK);
						strRsp = rsp.split(";");

						_totaTransResult.put("transactionId", strRsp[0]);
						_totaTransResult.put("Result1", "");
					}
					break;
			}

			return true;
		} catch (Exception ex) {
			LogHelperFactory.getTraceLogger().exceptionMsg(ex, ex.getMessage());
			return false;
		}

	}

	public boolean parseT24RspOFS(String rsp) {
		String[] strRsp = null;
		String[] strTrans = null;
		String[] strData = null;
		String[] strKeyValue = null;
		String strTmpValue = null;
		int i = 0;

		try {
			// 目前僅整理Transaction
			_totaTransResult = new HashMap<String, String>();
			_totaTransContent = new HashMap<String, String>();
			strRsp = rsp.split(",");
			if (strRsp.length > 0) {
				strTrans = strRsp[0].split("/");
				if (strTrans.length > 2) {
					switch (strTrans[2]) {
						// 表交易成功
						case "1":
							// 將後續資料轉至Dictionary
							for (i = 1; i < strRsp.length; i++) {
								strKeyValue = strRsp[i].split("=");
								strData = strKeyValue[0].split(":");
								if (strData[1].equals("1") || StringUtils.isBlank(strData[1])) {
									_totaTransResult.put(strData[0], strKeyValue[1]);
								} else {
									if (_totaTransResult.containsKey(strData[0])) {
										strTmpValue = _totaTransResult.get(strData[0]);
										_totaTransResult.remove(strData[0]);
										_totaTransResult.put(strData[0] + "_1", strTmpValue);
									}
									_totaTransResult.put(strData[0] + "_" + strData[1], strKeyValue[1]);
								}
							}
							// _totaTransResult.Add("transactionId", strTrans(0))
							_totaTransContent.put("transactionId", strTrans[0]);
							_totaTransResult.put("EB.ERROR", NormalRC.FEP_OK);
							break;
						default:
							String erMsg = StringUtils.join(ArrayUtils.subarray(strRsp, 1, strRsp.length), ',');
							_totaTransResult.put("EB.ERROR", erMsg);
							break;
					}
				} else {
					// 此時一定是StandardRoutine
					strData = strRsp[0].split(";");
					if (strData.length > 1) {
						_totaTransResult.put("transactionId", strData[0]);
						_totaTransResult.put("Result1", strData[1]);
						_totaTransResult.put("Result2", strData[2]);
						return true;
					} else {
						_totaTransResult.put("Result1", strData[0]);
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			if (e instanceof ArrayIndexOutOfBoundsException) {
				LogHelperFactory.getTraceLogger().exceptionMsg(e, "解讀strRsp[", i, "]=", strRsp[i], "出現異常, ", e.getMessage());
			} else {
				LogHelperFactory.getTraceLogger().exceptionMsg(e, e.getMessage());
			}
			return false;
		}
	}

	/**
	 組T24上行電文(單筆跨行預約轉帳結果通知)

	 @return T24上行電文
	 <remark>本函式只能處理單筆跨行預約轉帳結果通知電文</remark>
	 <history>
	 <modify>
	 <modifier>Ruling</modifier>
	 <reason>New Function for 預約跨轉增加UI019141(預約跨轉單筆重發處理)</reason>
	 <date>2017/10/13</date>
	 </modify>
	 </history>
	 */
	public String genT24ReqOFSForRETFR() {
		String result = "";
		StringBuilder sb = new StringBuilder();

		try {
			sb.append("TMB.OF.CHG.DB,,");
			sb.append(_userName + "/" + _sscode + "/" + _company + ",,TMB.FWD.TXN.REG.LOG,*CHG.DB**," + _company + "," + getTransactionId() + ",");
			if (_titaBody.size() > 0) {
				for (HashMap.Entry<String, String> entry : _titaBody.entrySet()) {
					sb.append(entry.getKey() + "::=" + entry.getValue() + ",");
				}
				result = sb.substring(0, sb.toString().length() - 1);
			}
			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	public String genT24ReqOFSForRM() {
		StringBuilder sb = new StringBuilder();
		String result = "";
		try {
			//Dim securityContext As New T24ServiceRequestSecurityContext
			//_t24req.ServiceRequest.SecurityContext = securityContext

			//_t24req.ServiceRequest.SecurityContext.company = _company

			// OPERATION,OPTIONS,USER INFORMATION,ID INFORMATION,DATA

			// 增加 reverse 的功能
			if (_version.equals("TMB.OFS.LRM.REMIT.OUT.REV")) {//RT1101 UnLock
				//Add by Jim, 2011/01/04, 由張玉青Mail提供Sample: TMB.OFS.LRM.REMIT.OUT.REV,,ANITA02/123456,,FUNDS.TRANSFER-FT111241YVC8
				//Modify by Jim, 2011/03/10, 友弘說: 有version就需要加上/2/0
				sb.append(_version + ",,");
				sb.append(_userName + "/" + _sscode + ",,");
				sb.append(_application + "-" + _transactionId);
				result = sb.toString();
			} else if (_version.equals("TMBI.C.EJ.NO.RSV.S")) {
				//sample: TMBI.C.EJ.NO.RSV.S,/I/PROCESS,ATM.USER/123456,,T.BR.SEQNO:1:= FEP201106300000001,FT.ID:1:=FT1121500ML8
				sb.append(_version + ",/I/" + _operation + ",");
				// USER INFORMATION
				sb.append(_userName + "/" + _sscode + ",,");
				if (_titaBody.size() > 0) {
					for (Map.Entry<String, String> entry : _titaBody.entrySet()) {
						sb.append(entry.getKey() + "::=" + entry.getValue() + ",");
					}
					result = sb.toString().substring(0, sb.toString().length() - 1);
				}
			} else if (_reverseTag) {
				// OPERATION
				sb.append(_application + ",");
				// OPTIONS
				// 2010/11/18 by Ed for 組電文格式有誤
				sb.append(_version + "/R/" + _operation + "/NULL/0,");
				// USER INFORMATION
				sb.append(_userName + "/" + _sscode + "/" + _company + ",");
				// ID INFORMATION
				sb.append(_transactionId);
				result = sb.toString();
			} else if (_routineTag) {
				sb.append(_routineName + ",,");
				sb.append(_userName + "/" + _sscode + "/" + _company + ",,");
				sb.append(_routineReqValue);
				result = sb.toString();
			} else if (_enquiryTag) {
				//Add by Jim, 2011/1/3, 給CBSTimeoutRerun call,timeout rerun要先作查詢, 不然也不知道T24是否CHECK EJ重覆, 直接重送會重覆入帳/記帳
				//sample: ENQUIRY.SELECT,,ATM.USER/123456,%FUNDS.TRANSFER,T.BR.SEQNO:EQ=2011010401220600
				sb.append(_version + ",,");
				sb.append(_userName + "/" + _sscode + ",");
				sb.append("%" + _application + ",");
				sb.append(_enquiryName + ":");
				if (_titaBody.size() > 0) {
					for (Map.Entry<String, String> entry : _titaBody.entrySet()) {
						sb.append("EQ=" + entry.getValue() + ",");
					}
					result = sb.toString().substring(0, sb.toString().length() - 1);
				}
			} else if ("S".equals(_function)) {
				//FUNDS.TRANSFER,TMB.RM.LCYIN.AUTO/S/PROCESS/Null/0,ATM.USER/123456/TW8070121,FEP2011050903558202
				sb.append(_application + ",");
				sb.append(_version + "/S/" + _operation + "/NULL/0,");
				sb.append(_userName + "/" + _sscode + "/" + _company + ",");
				sb.append(_titaBody.get("T.BR.SEQNO"));
				result = sb.toString();
			} else {
				// OPERATION
				sb.append(_application + ",");
				// OPTIONS
				//Modify by Jim, 2011/03/04, Header加上兩個欄位: GTS.CONTROL/NO.OF.AUTH (說明如下)
				//格式說明: VERSION name/Function/Process type/GTS.CONTROL/NO.OF.AUTH
				//Function：S(See), I(Input), A(Authorize), D(Delete), R(Reverse), H(History Restore), V(Verify)
				//Process(Type) : Process, VALIDATE
				//GTS.CONTROL：NULL(default VERSION>GTS.CONTROL), 1, 2, 3, 4
				// NO.OF.AUTH：NULL(default VERSION>NO.OF.AUTH -> COMPANY>DEFAULT.NO.OF.AUTH), 0, 1, 2
				sb.append(_version + "/I/" + _operation + "/NULL/0,");
				// USER INFORMATION
				sb.append(_userName + "/" + _sscode + "/" + _company + ",");
				// ID INFORMATION
				sb.append(_transactionId + ",");
				// DATA
				sb.append("TI.CHNN.CODE::=" + _titaHeader.getTiChnnCode() + ",");
				sb.append("TI.CHNN.CODE.S::=" + _titaHeader.getTiChnnCodeS() + ",");
				//sb.Append("TRM.BRANCH::=" + _titaHeader.TRM_BRANCH + ",")
				//sb.Append("TRMNO::=" + _titaHeader.TRMNO + ",")
				//sb.Append("EJFNO::=" + _titaHeader.EJFNO + ",")
				//sb.Append("FISC.DATE::=" + _titaHeader.FISC_DATE + ",")
				//sb.Append("REG.FLAG::=" + _titaHeader.REG_FLAG + ",")
				//sb.Append("FEP.USER.ID::=" + _titaHeader.FEP_USER_ID + ",")

				String[] strArray = null;
				if (_titaBody.size() > 0) {
					for (Map.Entry<String, String> pair : _titaBody.entrySet()) {
						if (pair.getKey().indexOf('_') != -1) {
							strArray = pair.getKey().split("[_]", -1);
							sb.append(strArray[0] + ":" + strArray[1] + ":=" + pair.getValue() + ",");
						} else {
							sb.append(pair.getKey() + "::=" + pair.getValue() + ",");
						}
					}
					result = sb.toString().substring(0, sb.toString().length() - 1);
				}
			}
			return result;
		} catch (Exception ex) {
			return null;
		}
	}

}
