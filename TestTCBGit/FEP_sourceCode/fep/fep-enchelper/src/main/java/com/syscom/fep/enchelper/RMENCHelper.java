package com.syscom.fep.enchelper;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.enchelper.vo.KeyIdentity;
import com.syscom.fep.frmcommon.ref.RefBoolean;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.mybatis.model.Feptxn;

/**
 * !!!注意, 這裡繼承的ENCHelper類必須是最後一XXXENCHelper類
 * 
 * !!!目前的繼承順序是ENCHelper-->APPENCHelper-->FISCENCHelper-->ATMENCHelper-->CommonENCHelper
 * 
 * !!!並且除了ENCHelper類之外, 其他XXXENCHelper類的構建函數必須是protected, 也就是說ENCHelper類是入口
 * 
 * @author Richard
 *
 */
public class RMENCHelper extends APPENCHelper {

	protected RMENCHelper(Feptxn feptxn, MessageBase txData) {
		super(feptxn, txData);
	}

	protected RMENCHelper(Feptxn feptxn) {
		super(feptxn);
	}

	protected RMENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq,
			MessageBase txData) {
		super(msgId, channel, subsys, ej, atmno, atmseq, txData);
	}

	protected RMENCHelper(FISCData txData) {
		super(txData);
	}

	/**
	 * For TSM
	 *
	 * @param msgId
	 * @param ej
	 */
	protected RMENCHelper(String msgId, int ej) {
		super(msgId, ej);
	}

	/**
	 * 2022-07-28 Richard add
	 *
	 * @param txData
	 */
    public RMENCHelper(MessageBase txData) {
        super(txData);
    }

    /// <summary>
	/// 產生ATM電文訊息押碼(MAC)
	/// </summary>
	/// <param name="mac">產生的MAC</param>
	/// <returns>FEPReturnCode</returns>
	public FEPReturnCode makeRMFISCMAC(RefString mac) {
		String encFunc = "";
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		String _type = "";
		int encrc = -1;
		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();
		if ("1111".equals(pcode) || "1121".equals(pcode) || "1131".equals(pcode) || "1171".equals(pcode) ||  "1181".equals(pcode)
				|| "1191".equals(pcode) || "1611".equals(pcode)  || "1112".equals(pcode)  || "1122".equals(pcode)
				|| "1172".equals(pcode)  || "1182".equals(pcode)  || "1192".equals(pcode) ) {
			encFunc = "FN000301";
		} else {
			encFunc = "FN000303";
		}

		// Jim, 2012/9/3, add new Pcode: 1513,1514,1515
		if ("1111".equals(pcode)  || "1121".equals(pcode)  || "1131".equals(pcode)  || "1171".equals(pcode)  || "1181".equals(pcode)
				|| "1191".equals(pcode) || "1611".equals(pcode)  || "1411".equals(pcode)  || "1511".equals(pcode)  || "1641".equals(pcode) 
				|| "1651".equals(pcode)  || "1513".equals(pcode)  || "1514".equals(pcode)  || "1515".equals(pcode) )
			_type = "1";
		else
			_type = "3";

		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareRMFISCMac(_type, mac.get(), keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "makeRMFISCMAC", encpar, outputData);

				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeMACError;
				} else {
					mac.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	private FEPReturnCode prepareRMFISCMac(String macType, String mac, RefString keyIdentity, RefString inputData1,
			RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;

		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		// 2011-03-14 by kyo ,spec update by Candy:若只給S1都改用T2, 若有判斷S1或T2的不用改
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.RMF);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, ' '));
		keyIdentity.set(key.toString());

		inputData1.set(prepareInputData1(macType));
		inputData2.set(mac);
		return rc;
	}

	/// <modify>
	/// <modifier>Kyo</modifier>
	/// <reason>修正pcode的case，並補上default值的處理</reason>
	/// <date>2010/03/29</date>
	/// </modify>
	private String prepareInputData1(String macType) throws Exception {
		String inputData = "";
		String icv_fcrm = "";
		String icv_rm = "";
		if (rmData.getTxObject().getFCRMRequest() != null
				&& rmData.getTxObject().getFCRMRequest().getTxnInitiateDateAndTime().length() > 6) {
			icv_fcrm = rmData.getTxObject().getFCRMRequest().getTxnInitiateDateAndTime().substring(0, 6);
		}
		if (rmData.getTxObject().getRMRequest() != null
				&& rmData.getTxObject().getRMRequest().getTxnInitiateDateAndTime().length() > 6) {
			icv_rm = rmData.getTxObject().getRMRequest().getTxnInitiateDateAndTime().substring(0, 6);
		}

		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();
		String rmreq_txamt = "";
		String fcrmreq_txamt = "";

		if (rmData.getTxObject().getRMRequest() != null
				&& StringUtils.isNotBlank(rmData.getTxObject().getRMRequest().getTxAmt()))
			rmreq_txamt = StringUtils.leftPad(
					MathUtil.toString(new BigDecimal(rmData.getTxObject().getRMRequest().getTxAmt()), "0.00"), 13, '0');

		if (rmData.getTxObject().getFCRMRequest() != null
				&& StringUtils.isNotBlank(rmData.getTxObject().getFCRMRequest().getTxAmt()))
			fcrmreq_txamt = StringUtils.leftPad(
					MathUtil.toString(new BigDecimal(rmData.getTxObject().getFCRMRequest().getTxAmt()), "0.00"), 13,
					'0');

		if (macType.equals("1")) {
			switch (pcode) {
			case "1111":
			case "1121":
			case "1131":
			case "1171":
			case "1181":
			case "1191":
				inputData = StringUtils.join(icv_rm, StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
						, rmreq_txamt, rmData.getTxObject().getRMRequest().getSenderBank().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1611":
				if (SysStatus.getPropertyValue().getSysstatFbkno()
						.equals(rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3))) {
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
							, fcrmreq_txamt , rmData.getTxObject().getFCRMRequest().getReceiverBank().substring(0, 3)
							, rmData.getTxObject().getFCRMRequest().getCURRENCY() , StringUtils.rightPad("0", 6, '0'));
				} else {
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
							, fcrmreq_txamt , rmData.getTxObject().getFCRMRequest().getSenderBank().substring(0, 3)
							, rmData.getTxObject().getFCRMRequest().getCURRENCY() , StringUtils.rightPad("0", 6, '0'));
				}
				break;
			case "1112":
			case "1122":
			case "1132":
			case "1172":
			case "1182":
			case "1192":
				inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
						, rmreq_txamt , rmData.getTxObject().getRMRequest().getReceiverBank().substring(0, 3)
						, rmData.getTxObject().getRMRequest().getResponseCode() , StringUtils.rightPad("0", 5, '0'));
				break;
			case "1411":
				inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1641":
				if (SysStatus.getPropertyValue().getSysstatFbkno()
						.equals(rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3))) {
					inputData = StringUtils.join(icv_fcrm
							, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
							, StringUtils.rightPad("0", 13, '0')
							, rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3)
							, StringUtils.rightPad("0", 9, '0'));
				} else {
					inputData = StringUtils.join(icv_fcrm
							, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
							, StringUtils.rightPad("0", 13, '0')
							, rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3)
							, StringUtils.rightPad("0", 9, '0'));
				}
				break;
			// Jim, 2012/8/30, 新增的pcode
			case "1513":
			case "1514":
			case "1515":
				inputData = StringUtils.join(icv_rm
						, StringUtils.leftPad(rmData.getTxObject().getRMRequest().getSystemTraceAuditNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1511":
				inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1651":
				inputData = StringUtils.join(icv_fcrm , StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getOrgBankNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1412":
			case "1512":
				inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getRMRequest().getTxnDestinationInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			case "1652":
				inputData = StringUtils.join(icv_fcrm , StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getOrgBankNo(), 7, '0')
						, StringUtils.rightPad("0", 13, '0')
						, rmData.getTxObject().getFCRMRequest().getTxnDestinationInstituteId().substring(0, 3)
						, StringUtils.rightPad("0", 9, '0'));
				break;
			// 2011-03-29 modify by kyo for 補上case未成立時丟出訊息
			default:
				throw new Exception("偵測到未處理的pcode:" + pcode);
			}
		} else if ("2".equals(macType)) {
			switch (pcode) {
			// 2011-03-29 modify by kyo for case寫錯個案，修正
			case "1111":
			case "1121":
			case "1131":
			case "1171":
			case "1181":
			case "1191":
			case "1112":
			case "1122":
			case "1132":
			case "1172":
			case "1182":
			case "1192":
				inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getBankNo(), 7, '0')
						, rmreq_txamt , rmData.getTxObject().getRMRequest().getInActno()
						, rmData.getTxObject().getRMRequest().getSenderBank().substring(0, 3)
						, rmData.getTxObject().getRMRequest().getReceiverBank().substring(0, 3)
						, StringUtils.rightPad("0", 8, '0'));
				break;
			case "1611":
				inputData = StringUtils.join(icv_fcrm , StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
						, fcrmreq_txamt , rmData.getTxObject().getFCRMRequest().getInActno()
						, rmData.getTxObject().getFCRMRequest().getSenderBank().substring(0, 3)
						, rmData.getTxObject().getFCRMRequest().getReceiverBank().substring(0, 3)
						, rmData.getTxObject().getFCRMRequest().getCURRENCY() , StringUtils.rightPad("0", 3, '0'));
				break;
			// 2011-03-29 modify by kyo for 補上case未成立時丟出訊息
			default:
				throw new Exception("偵測到未處理的pcode:" + pcode);
			}
		} else {
			// macType==3時 使用repFISCData的RC
			if (rmData.getTxObject().getRMResponse() != null) {
				switch (pcode) {
				case "1111":
				case "1121":
				case "1131":
				case "1171":
				case "1181":
				case "1191":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
							, rmreq_txamt , rmData.getTxObject().getRMRequest().getSenderBank().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1611":
					if (SysStatus.getPropertyValue().getSysstatFbkno()
							.equals(rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3))) {
						inputData = StringUtils.join(icv_fcrm
								, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
								, fcrmreq_txamt
								, rmData.getTxObject().getFCRMRequest().getReceiverBank().substring(0, 3)
								, rmData.getTxObject().getFCRMResponse().getResponseCode()
								, StringUtils.rightPad("0", 5, '0'));
					} else {
						inputData = StringUtils.join(icv_fcrm
								, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
								, fcrmreq_txamt , rmData.getTxObject().getFCRMRequest().getSenderBank().substring(0, 3)
								, rmData.getTxObject().getFCRMResponse().getResponseCode()
								, StringUtils.rightPad("0", 5, '0'));
					}
					break;
				case "1112":
				case "1122":
				case "1132":
				case "1172":
				case "1182":
				case "1192":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
							, rmreq_txamt , rmData.getTxObject().getRMRequest().getReceiverBank().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1411":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
							, StringUtils.rightPad("0", 13, '0')
							, rmData.getTxObject().getRMRequest().getTxnSourceInstituteId().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1641":
					if (SysStatus.getPropertyValue().getSysstatFbkno()
							.equals(rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3))) {
						inputData = StringUtils.join(icv_fcrm
								, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
								, StringUtils.rightPad("0", 13, '0')
								, rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3)
								, rmData.getTxObject().getRMResponse().getResponseCode()
								, StringUtils.rightPad("0", 9, '0'));
					} else {
						inputData = StringUtils.join(icv_fcrm
								, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getBankNo(), 7, '0')
								, StringUtils.rightPad("0", 13, '0')
								, rmData.getTxObject().getFCRMRequest().getTxnSourceInstituteId().substring(0, 3)
								, rmData.getTxObject().getFCRMResponse().getResponseCode()
								, StringUtils.rightPad("0", 9, '0'));
					}
					break;
				case "1511":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMResponse().getFiscNo(), 7, '0')
							, rmData.getTxObject().getRMResponse().getSTATUS()
							, StringUtils.leftPad(rmData.getTxObject().getRMResponse().getOrgPcode(), 4, '0')
							, StringUtils.rightPad("0", 7, '0')
							, rmData.getTxObject().getRMResponse().getTxnSourceInstituteId().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1651":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getFCRMResponse().getBankNo(), 7, '0')
							, rmData.getTxObject().getFCRMResponse().getSTATUS()
							, StringUtils.leftPad(rmData.getTxObject().getFCRMResponse().getOrgPcode(), 4, '0')
							, StringUtils.rightPad("0", 7, '0')
							, rmData.getTxObject().getFCRMResponse().getTxnSourceInstituteId().substring(0, 3)
							, rmData.getTxObject().getFCRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1412":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
							,StringUtils.rightPad("0", 13, '0')
							, rmData.getTxObject().getRMRequest().getTxnDestinationInstituteId().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1512":
					inputData = StringUtils.join(icv_rm , StringUtils.leftPad(rmData.getTxObject().getRMRequest().getFiscNo(), 7, '0')
							, rmData.getTxObject().getRMResponse().getSTATUS()
							, StringUtils.leftPad(rmData.getTxObject().getRMRequest().getOrgPcode(), 4, '0')
							, StringUtils.rightPad("0", 7, '0')
							, rmData.getTxObject().getRMRequest().getTxnDestinationInstituteId().substring(0, 3)
							, rmData.getTxObject().getRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				case "1652":
					inputData = StringUtils.join(icv_fcrm
							, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getOrgBankNo(), 7, '0')
							, rmData.getTxObject().getFCRMResponse().getSTATUS()
							, StringUtils.leftPad(rmData.getTxObject().getFCRMRequest().getOrgPcode(), 4, '0')
							, StringUtils.rightPad("0", 7, '0')
							, rmData.getTxObject().getFCRMRequest().getTxnDestinationInstituteId().substring(0, 3)
							, rmData.getTxObject().getFCRMResponse().getResponseCode()
							, StringUtils.rightPad("0", 5, '0'));
					break;
				// 2011-03-29 modify by kyo for 補上case未成立時丟出訊息
				default:
					throw new Exception("偵測到未處理的pcode:" + pcode);
				}
			} else {
				throw new Exception("RMResponse電文為Nothing");
			}
		}

		return inputData;
	}

	/**
	 * 檢核RMBank電文訊息押碼(MAC)以及產生Sync
	 * @param unitBank
	 * @param refAutoBack
	 * @param refBackReason
	 * @param refCdKeyFlag
	 * @param refNewKeyCheckOK
	 * @return
	 */
	public FEPReturnCode checkRMMBankMACAndSync(String unitBank, RefBoolean refAutoBack, RefString refBackReason, RefString refCdKeyFlag, RefBoolean refNewKeyCheckOK){
		FEPReturnCode rc = FEPReturnCode.Normal;

		RefString keyId1 = new RefString("");
		RefString inputData1= new RefString("");
		RefString inputData2= new RefString("");

		Boolean newKey = false;
		//例外或錯誤需要退匯，因此先設定需要退匯
		refAutoBack.set(true);
		//例外或錯誤理由為02，因此先設定需要理由為02
		refBackReason.set("02");

		final String encFunc = "FN000122";
		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();
		//若非以下 PCODE 則 Return True
		if("1112".equals(pcode)
				|| "1122".equals(pcode)
				|| "1132".equals(pcode)
				|| "1172".equals(pcode)
				|| "1182".equals(pcode)
				|| "1192".equals(pcode)){
			int encrc = -1;

			try{
				rc = checkRMMBankMAC(unitBank, newKey);

				if(rc == FEPReturnCode.Normal){
					rc = prepareRMFISCSync(unitBank, newKey, keyId1, inputData1, inputData2);
				}else{
					if("1".equals(refCdKeyFlag.get())){
						newKey = true;
						rc = checkRMMBankMAC(unitBank, newKey);
						if(rc != FEPReturnCode.Normal){
							refNewKeyCheckOK.set(false);
							return rc;
						}else{
							//AP要多執行KEY 生效
							refNewKeyCheckOK.set(true);
							rc = prepareRMFISCSync(unitBank, newKey, keyId1, inputData1, inputData2);
						}
					}
				}

				if(rc == FEPReturnCode.Normal){
					ENCParameter tempVar = new ENCParameter();
					tempVar.setKeyIdentity(keyId1.get());
					tempVar.setInputData1(inputData1.get());
					tempVar.setInputData2(inputData2.get());
					ENCParameter[] encpar = new ENCParameter[] { tempVar };
					String[] rtnAry = new String[] { "", "" };
					encrc = encLib(encFunc, "checkRMMBankMACAndSync", encpar, rtnAry);
					if (encrc != 0) {
						rc = ENCReturnCode.ENCCheckMACError;
						refAutoBack.set(true);
						refBackReason.set( "02");
					} else {
						rc = FEPReturnCode.Normal;
						refAutoBack.set(false);
						refBackReason.set( "");
					}
				}

				if(rc != FEPReturnCode.Normal){
					if("1".equals(refCdKeyFlag.get()) && newKey == false){
						newKey = true;
						rc = prepareRMFISCSync(unitBank, newKey, keyId1, inputData1, inputData2);
						if(rc == FEPReturnCode.Normal){
							ENCParameter tempVar = new ENCParameter();
							tempVar.setKeyIdentity(keyId1.get());
							tempVar.setInputData1(inputData1.get());
							tempVar.setInputData2(inputData2.get());
							ENCParameter[] encpar = new ENCParameter[] { tempVar };
							String[] rtnAry = new String[] { "", "" };
							encrc = encLib(encFunc, "checkRMMBankMACAndSync", encpar, rtnAry);
							if (encrc != 0) {
								rc = ENCReturnCode.ENCCheckMACError;
								refAutoBack.set(true);
								refBackReason.set( "02");
							} else {
								rc = FEPReturnCode.Normal;
								refAutoBack.set(false);
								refBackReason.set( "");
							}
						}

						if(rc != FEPReturnCode.Normal){
							refNewKeyCheckOK.set(false);
							return rc;
						}else{
							//AP要多執行KEY 生效
							refNewKeyCheckOK.set(true);
						}
					}
				}

			}catch (Exception ex) {
				logData.setProgramException(ex);
				logData.setRemark(
						this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "", String.valueOf(encrc)));
				sendEMS(logData);
				rc = ENCReturnCode.ENCMakeMACError;
			}

		}
		return rc;
	}

	/**
	 * RM換 CDKEY
	 * @param unitBank
	 * @return
	 */
	public FEPReturnCode changeRMCDKey(String unitBank){
		FEPReturnCode rc = FEPReturnCode.Normal;

		String keyId1 = "";
		String inputData1 = "";
		String inputData2 = "";
		final String encFunc = "FN000103";
		KeyIdentity key = new KeyIdentity();
		int encrc = 0;

		key.setKeyQty(1);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);

		if("11".equals(rmData.getTxObject().getRMRequest().getProcessingCode().substring(0, 2)) && "2".equals(rmData.getTxObject().getRMRequest().getProcessingCode().substring(3))){
			key.setKeyType1(ENCKeyType.T2);
		}else if("REPK".equals(rmData.getTxObject().getRMRequest().getEnglishMemo().substring(0,4))){
			key.setKeyType1(ENCKeyType.S1);
		}else{
			key.setKeyType1(ENCKeyType.T2);
		}

		if("1411".equals(rmData.getTxObject().getRMRequest().getProcessingCode())){
			key.getKeyId1().setKeyFunction(ENCKeyFunction.RMR);
			key.getKeyId1().setKeySubCode(StringUtils.rightPad(StringUtils.join(rmData.getTxObject().getRMRequest().getReceiverBank(), " "),3," "));
		}else if("11".equals(rmData.getTxObject().getRMRequest().getProcessingCode().substring(0,2)) && "2".equals(rmData.getTxObject().getRMRequest().getProcessingCode().substring(3))){
			key.getKeyId1().setKeyFunction(ENCKeyFunction.RMR);
			if("".equals(unitBank)){
				key.getKeyId1().setKeySubCode(StringUtils.rightPad(StringUtils.join(rmData.getTxObject().getRMRequest().getSenderBank(), " "),3," "));
			}else{
				key.getKeyId1().setKeySubCode(StringUtils.rightPad(StringUtils.join(unitBank, " "),3," "));
			}
		}else{
			key.getKeyId1().setKeyFunction(ENCKeyFunction.RMR);
			key.getKeyId1().setKeySubCode(StringUtils.rightPad(StringUtils.join(rmData.getTxObject().getRMRequest().getSenderBank(), " "),3," "));
		}
		keyId1 = key.toString();

		try{
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(keyId1);
			tempVar.setInputData1(inputData1);
			tempVar.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			encrc = encLib(encFunc, "changeRMCDKey", encpar, null);
			if(encrc != 0){
				return ENCReturnCode.ENCLibError;
			}

		}catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1, inputData1, inputData2, "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCChangePPKeyError;
		}

		return rc;
	}

	/**
	 *檢核ATM電文訊息押碼(MAC)
	 * @return
	 */
	public FEPReturnCode checkRMFISCMAC(){

		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");

		String encFunc = "";
		String mac = "";

		if(StringUtils.isNotBlank(rmData.getTxObject().getRMResponse().getMessageType())){
			if("0210".equals(rmData.getTxObject().getRMResponse().getMessageType().substring(0,4))){
				mac = rmData.getTxObject().getRMResponse().getMAC();
			}
		}else{
			mac = rmData.getTxObject().getRMRequest().getMAC();
		}

		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();
		if("1111".equals(pcode)
				|| "1121".equals(pcode)
				|| "1131".equals(pcode)
				|| "1171".equals(pcode)
				|| "1181".equals(pcode)
				|| "1191".equals(pcode)
				|| "1611".equals(pcode)
				|| "1112".equals(pcode)
				|| "1122".equals(pcode)
				|| "1172".equals(pcode)
				|| "1182".equals(pcode)
				|| "1192".equals(pcode)){
			encFunc = "FN000302";
		}else{
			encFunc = "FN000304";
		}


		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try{

			if("1512".equals(pcode) || "1652".equals(pcode)){
				rc = prepareRMFISCMac("1", mac, keyId1, inputData1, inputData2);
			}else{
				rc = prepareRMFISCMac("3", mac, keyId1, inputData1, inputData2);
			}
			if (rc == FEPReturnCode.Normal){
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				encrc = encLib(encFunc, "checkRMFISCMAC", encpar, null);
				if(encrc != 0){
					return ENCReturnCode.ENCLibError;
				}
			}

		}catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCChangePPKeyError;
		}

		return rc;
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC)以及產生ATM電文訊息押碼(MAC)
	 * @param refMakeMAC 壓出來的MAC
	 * @return
	 */
	public FEPReturnCode checkRMFISCMACAndMakeMAC(RefString refMakeMAC){

		RefString keyId1 = new RefString("");
		RefString inputData11 = new RefString("");
		RefString inputData12 = new RefString("");

		RefString keyId2 = new RefString("");
		RefString inputData21 = new RefString("");
		RefString inputData22 = new RefString("");

		String inputData1 = "";
		String encFunc = "";
		String checkMAC = rmData.getTxObject().getRMRequest().getMAC();
		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();

		if("1112".equals(pcode)
				|| "1122".equals(pcode)
				|| "1132".equals(pcode)
				|| "1172".equals(pcode)
				|| "1182".equals(pcode)
				|| "1192".equals(pcode)
				|| "1611".equals(pcode)){
			encFunc = "FN000309";
		}else{
			encFunc = "FN000321";
		}

		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try{


			rc = prepareRMFISCMac("1", checkMAC, keyId1, inputData11, inputData12);
			if(rc == FEPReturnCode.Normal){
				//reqFISCData.RC = “0001 Default RC =0001產生MAC,若之後有誤, 需再重新MakeFISCMAC
				rmData.getTxObject().getRMResponse().setResponseCode("0001");
				rc = prepareRMFISCMac("3", refMakeMAC.get(), keyId2, inputData21, inputData22);
			}

			if (rc == FEPReturnCode.Normal){
				inputData1 = StringUtils.join("0002", keyId1.get(),
						PolyfillUtil.toString(inputData11.get().length(), "0000"),
						StringUtils.rightPad(inputData11.get(), 128, ' '),
						PolyfillUtil.toString(inputData12.get().length(), "0000"),
						StringUtils.rightPad(inputData12.get(), 128, ' '), keyId2.get(),
						PolyfillUtil.toString(inputData21.get().length(), "0000"),
						StringUtils.rightPad(inputData21.get(), 128, ' '),
						PolyfillUtil.toString(inputData22.get().length(), "0000"),
						StringUtils.rightPad(inputData22.get(), 128, ' '));
				if (rc == FEPReturnCode.Normal) {
					ENCParameter tempVar = new ENCParameter();
					tempVar.setKeyIdentity("");
					tempVar.setInputData1(inputData1);
					tempVar.setInputData2("");
					ENCParameter[] encpar = new ENCParameter[] { tempVar };
					String[] outputData= new String[] {""};
					encrc = encLib(encFunc, "checkRMFISCMACAndMakeMAC", encpar, outputData);
					if (encrc != 0) {
						if (encrc == 101) {
							rc = ENCReturnCode.ENCCheckMACError;
						} else if (encrc == 102) {
							rc = ENCReturnCode.ENCMakeMACError;
						} else {
							rc = ENCReturnCode.ENCCheckMACError;
						}
					}else{
						refMakeMAC.set(outputData[0]);
					}
				}
			}

		}catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1.get(), inputData1, "", "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCChangePPKeyError;
		}


		return rc;
	}

	/**
	 * RM檢核CDKEY
	 * @param refFiscRC
	 * @return
	 */
	public FEPReturnCode checkRMCDKey(RefString refFiscRC){
		FEPReturnCode rc = FEPReturnCode.Normal;

		String keyId1 = "";
		String inputData1 = "";
		String inputData2 = "";
		final String encFunc = "FN000104";
		KeyIdentity key = new KeyIdentity();
		int encrc = 0;

		key.setKeyQty(1);
		key.getKeyId1().setKeyKind(ENCKeyKind.CDK);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.RMR);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(StringUtils.join(rmData.getTxObject().getRMRequest().getSenderBank().substring(0,3)," "),3," "));

		if("CHGK".equals(rmData.getTxObject().getRMRequest().getEnglishMemo().substring(0,4))){
			inputData1 = rmData.getTxObject().getRMRequest().getEnglishMemo().substring(4, 28);
			key.setKeyType1(ENCKeyType.S1);
		}else{
			inputData1 = rmData.getTxObject().getRMRequest().getEnglishMemo().substring(4, 44);
			key.setKeyType1(ENCKeyType.T2);
		}
		keyId1 = key.toString();
		inputData2 = StringUtils.join(rmData.getTxObject().getRMRequest().getSenderBank().substring(0,3) ,rmData.getTxObject().getRMRequest().getReceiverBank().substring(0,3));

		try{
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(keyId1);
			tempVar.setInputData1(inputData1);
			tempVar.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			encrc = encLib(encFunc, "checkRMCDKey", encpar, null);
			if(encrc != 0){
				//CHANGE KEY CALL 訊息內之RAN-DOM NUMBER 錯誤
				refFiscRC.set("3303");
				return ENCReturnCode.ENCLibError;
			}

			if("CHGK".equals(rmData.getTxObject().getRMRequest().getEnglishMemo().substring(0,4))){
				if(!PolyfillUtil.isNumeric(rmData.getTxObject().getRMRequest().getEnglishMemo().substring(28,34))){
					refFiscRC.set("0101");
				}
			}else{
				if(!PolyfillUtil.isNumeric(rmData.getTxObject().getRMRequest().getEnglishMemo().substring(44,50))){
					refFiscRC.set("0101");
				}
			}

		}catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1, inputData1, inputData2, "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCChangePPKeyError;
		}

		return rc;
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC)
	 * @param unitBank
	 * @param newKey
	 * @return
	 */
	public FEPReturnCode checkRMMBankMAC(String unitBank,boolean newKey){
		FEPReturnCode rc = FEPReturnCode.Normal;

		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		final String encFunc = "FN000302";
		String mac = rmData.getTxObject().getRMRequest().getMbMac();
		int encrc = -1;

		try{
			rc = prepareRMMBankMac(ENCKeyFunction.RMR, "2", unitBank, mac, newKey, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal){
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				encrc = encLib(encFunc, "checkRMMBankMAC", encpar, null);
				if(encrc != 0){
					return ENCReturnCode.ENCCheckMACError;
				}
			}
		}catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(
					this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCChangePPKeyError;
		}

		return rc;
	}

	public FEPReturnCode prepareRMFISCSync(String unitBank, boolean newKey, RefString keyIdentity, RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;

		//Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);

		//2011-03-14 by kyo ,spec update by Candy:若只給S1都改用T2, 若有判斷S1或T2的不用改
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.RMR);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(unitBank,8," "));

		if (newKey == true){
			/* NEW KEY */
			key.getKeyId1().setKeyVersion("02");
		}else{
			/* OLD KEY */
			key.getKeyId1().setKeyVersion("01");
		}
		keyIdentity.set(key.toString());
		inputData1.set(rmData.getTxObject().getRMRequest().getMbSync());
		inputData2.set(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),unitBank));
		return rc;
	}

	@SuppressWarnings("unused")
	private FEPReturnCode prepareRMMBankMac(ENCKeyFunction funType, String macType, String unitBank, String mac, Boolean newKey, RefString keyIdentity, RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		//Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);

		//2011-03-14 by kyo ,spec update by Candy:若只給S1都改用T2, 若有判斷S1或T2的不用改
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(funType);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(unitBank,8," "));

		if (newKey == true){
			/* NEW KEY */
			key.getKeyId1().setKeyVersion("02");
		}else{
			/* OLD KEY */
			key.getKeyId1().setKeyVersion("01");
		}
		keyIdentity.set(key.toString());
		inputData1.set(prepareInputData1(macType));
		inputData2.set(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(),unitBank));
		return rc;
	}

	/**
	 產生ATM電文訊息押碼(MAC)

	 @param mac 產生的MAC
	 @return FEPReturnCode
	 */
	public FEPReturnCode makeRMMBankMACAndSync(String unitBank, RefString mac, RefString sync) {
		String encFunc = "";
		String keyId1 = "";
		String inputData1 = "";
		String inputData2 = "";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;

		String pcode = rmData.getTxObject().getRMRequest().getProcessingCode();
		if ("1111".equals(pcode) || "1121".equals(pcode) ||
				"1131".equals(pcode) || "1171".equals(pcode) ||
				"1181".equals(pcode) || "1191".equals(pcode) ||
				"1611".equals(pcode) || "1112".equals(pcode) ||
				"1122".equals(pcode) || "1172".equals(pcode) ||
				"1182".equals(pcode) || "1192".equals(pcode)) {
			try {
				rc = makeRMMBankMAC(unitBank, mac);
				if (rc == FEPReturnCode.Normal) {
					rc = makeRMMBankSync(unitBank, sync);
				}
			} catch (Exception ex) {
				logData.setProgramException(ex);
				logData.setRemark(getENCLogMessage(encFunc, keyId1, inputData1, inputData2, "", "", String.valueOf(encrc)));
				sendEMS(logData);
				rc = ENCReturnCode.ENCMakeMACError;
			}
		}
		return rc;
	}

	/**
	 產生RM電文Sync

	 @param mac 產生的MAC
	 @return FEPReturnCode
	 */
	public FEPReturnCode makeRMMBankMAC(String unitBank, RefString mac) {
		final String encFunc = "FN000301";
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		int encrc = -1;

		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareRMMBankMac(ENCKeyFunction.RMS, "2", unitBank, mac.get(), false, keyId1, inputData1, inputData2);
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(keyId1.get());
			tempVar.setInputData1(inputData1.get());
			tempVar.setInputData2(inputData2.get());
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			String[] outputData = new String[] { "" };
			encrc = encLib(encFunc, "MakeRMMBankMAC", encpar, outputData);
			if (encrc != 0) {
				rc = ENCReturnCode.ENCMakeMACError;
			} else {
				mac.set(outputData[0]);
			}

		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	private FEPReturnCode prepareRMMBankMac(ENCKeyFunction funType, String macType, String unitBank, String mac, boolean newKey, RefString keyIdentity, RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;

		//Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		//2011-03-14 by kyo ,spec update by Candy:若只給S1都改用T2, 若有判斷S1或T2的不用改
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(funType);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(unitBank,8, ' '));
		if (newKey) {
			key.getKeyId1().setKeyVersion("02"); // NEW KEY
		} else {
			key.getKeyId1().setKeyVersion("01"); // OLDKEY
		}
		keyIdentity.set(key.toString());

		inputData1.set(prepareInputData1(macType));
		inputData2.set(mac);
		return rc;
	}
	
	/**
	 * 產生RM電文Sync
	 * @param unitBank
	 * @param sync
	 * @return
	 * @throws Exception
	 */
	public FEPReturnCode makeRMMBankSync(String unitBank, RefString sync) throws Exception {
		final String encFunc = "FN000121";
		String keyId1 = "";
		String inputData1 = "";
		final String inputData2 = "";
		int encrc = -1;
		//Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		//2011-03-14 by kyo ,spec update by Candy:若只給S1都改用T2, 若有判斷S1或T2的不用改
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.RMS);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(unitBank,8, ' '));
		//2011-9-28 by Jim, spec update by Candy for input_data_1
		inputData1 = SysStatus.getPropertyValue().getSysstatHbkno() + unitBank;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(key.toString());
			tempVar.setInputData1(inputData1);
			tempVar.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			String[] outputData = new String[] { "" };
			encrc = encLib(encFunc, "MakeRMMBankSync", encpar, outputData);
			if (encrc != 0) {
				rc = ENCReturnCode.ENCLibError;
			} else {
				sync.set(outputData[0]);
			}

		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1, inputData1, inputData2, "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	/**
	 產生RM電文CDKey

	 @param mac 產生的MAC
	 @return FEPReturnCode
	 */
	public FEPReturnCode makeRMCDKey(String wBkno, String encType, RefString mac, RefString sync) {
		FEPReturnCode rc = FEPReturnCode.Normal;

		final String encFunc = "FN000102";
		String keyId1 = "";
		String inputData1 = "";
		final String inputData2 = "";
		int encrc = -1;
		//Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(2);
		if ("1".equals(encType)) {
			key.setKeyType1(ENCKeyType.S1);
		} else {
			key.setKeyType1(ENCKeyType.T2);
		}
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.RMS);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(wBkno,6, ' '));
		key.getKeyId2().setKeyKind(ENCKeyKind.CDK);
		key.getKeyId2().setKeyFunction(ENCKeyFunction.RMS);
		key.getKeyId2().setKeySubCode(StringUtils.rightPad(wBkno,6, ' '));
		keyId1 = key.toString();


		try {
			inputData1 = StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno() , wBkno);
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(keyId1);
			tempVar.setInputData1(inputData1);
			tempVar.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			String[] outputData = new String[] { "" };
			encrc = encLib(encFunc, "MakeRMCDKey", encpar, outputData);
			if (encrc != 0) {
				rc = ENCReturnCode.ENCLibError;
			} else {
				if ("1".equals(encType)) {
					mac.set(outputData[0].substring(0, 16));
					sync.set(outputData[0].substring(16, 24));
				} else {
					mac.set(outputData[0].substring(0, 32));
					sync.set(outputData[0].substring(32, 40));
				}
			}

		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1, inputData1, inputData2, "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}
}
