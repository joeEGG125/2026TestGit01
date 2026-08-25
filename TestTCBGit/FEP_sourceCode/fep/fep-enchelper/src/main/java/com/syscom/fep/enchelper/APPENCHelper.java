package com.syscom.fep.enchelper;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.enchelper.vo.KeyIdentity;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Feptxn;

public class APPENCHelper extends FISCENCHelper {
	protected APPENCHelper(Feptxn feptxn, MessageBase txData) {
		super(feptxn, txData);
	}

	protected APPENCHelper(Feptxn feptxn) {
		super(feptxn);
	}

	protected APPENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq, MessageBase txData) {
		super(msgId, channel, subsys, ej, atmno, atmseq, txData);
	}
	protected APPENCHelper(FISCData txData) {
		super(txData);
	}

	/**
	 * For TSM
	 *
	 * @param msgId
	 * @param ej
	 */
	protected APPENCHelper(String msgId, int ej) {
		super(msgId, ej);
	}

	/**
	 * 2022-07-28 Richard add
	 *
	 * @param txData
	 */
	public APPENCHelper(MessageBase txData) {
		super(txData);
	}

	public FEPReturnCode checkAppTac(String atmType) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		String encFunc = "FN000412";
		FEPReturnCode rc = FEPReturnCode.Normal;
		int encrc = -1;
		try {
			StringBuilder sb = new StringBuilder();
			String tac = StringUtils.EMPTY;
			if (atmType.equalsIgnoreCase("A")) {
				if (!"2568".equals(this.feptxn.getFeptxnPcode())) {
					StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxDatetimeFisc(), 14, '0'));
					StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnIcSeqno(), 8, '0'));
					if ("TWD".equals(this.feptxn.getFeptxnTxCur())) {
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxAmt().toString(), 12, '0'));
					} else {
						// Fly 2019/08/01 調整抓取欄位
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnCashAmt().toString(), 12, '0'));
					}
					StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTroutActno(), 16, '0'));
					StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
					StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnAtmChk(), 8, '0'));
					StringUtil.append(sb, "000000");
				} else {
					// Fly 2019/12/17 豐錢包新增Paytax繳稅功能
					if (StringUtils.isNotBlank(this.feptxn.getFeptxnPaytype()) && this.feptxn.getFeptxnPaytype().length() >= 2 && "15".equals(this.feptxn.getFeptxnPaytype().substring(0, 2))) {
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxDatetimeFisc(), 14, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnIdno().trim(), 16, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxAmt().toString(), 12, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTroutActno(), 16, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnPaytype(), 5, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnAtmType(), 14, '0'));
						StringUtil.append(sb, "000");
					} else {
						String seqno = StringUtils.EMPTY;
						if (StringUtils.isNotBlank(this.feptxn.getFeptxnReconSeqno())) {
							seqno = this.feptxn.getFeptxnReconSeqno().trim();
						}
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxDatetimeFisc(), 14, '0'));
						StringUtil.append(sb, StringUtils.leftPad(seqno, 16, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxAmt().toString(), 12, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTroutActno(), 16, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnPaytype(), 5, '0'));
						StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnAtmType(), 14, '0'));
						StringUtil.append(sb, "000");
					}
				}
			} else if (atmType.equalsIgnoreCase("B") || atmType.equalsIgnoreCase("C")) {
				StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTxDatetimeFisc(), 14, '0'));
				StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnIcSeqno(), 8, '0'));
				StringUtil.append(sb, StringUtils.leftPad(StringUtils.EMPTY, 12, '0'));
				StringUtil.append(sb, StringUtils.leftPad(this.feptxn.getFeptxnTroutActno(), 16, '0'));
				StringUtil.append(sb, StringUtils.leftPad(StringUtils.EMPTY, 22, '0'));
			}
			String msg = getSHA256Hash(sb.toString());
			String tmp = getSHA256Hash(sb.toString());
			String div = tmp.substring(0, 48);
			String icv = tmp.substring(48, 64);
			if (atmType.equalsIgnoreCase("B") || atmType.equalsIgnoreCase("A")) {
				tac = StringUtil.fromHex(this.feptxn.getFeptxnIcTac());
				// Fly 2019/12/17 豐錢包新增Paytax繳稅功能
				String wKey = StringUtils.EMPTY;
				if (!"2568".equals(this.feptxn.getFeptxnPcode())) {
					wKey = "948";
				} else {
					wKey = "PAYTAX";
				}
				this.prepareAppTac(msg, icv, div, wKey, tac, keyId1, inputData1, inputData2);
				if (rc == FEPReturnCode.Normal) {
					ENCParameter param = new ENCParameter();
					param.setKeyIdentity(keyId1.get());
					param.setInputData1(inputData1.get());
					param.setInputData2(inputData2.get());
					ENCParameter[] encpar = new ENCParameter[] { param };
					String[] outputData = new String[] { StringUtils.EMPTY };
					encrc = this.encLib(encFunc, "checkAppTac", encpar, outputData);
					if (encrc != 0) {
						rc = ENCReturnCode.ENCCheckTACError;
					} else {
						rc = FEPReturnCode.Normal;
					}
				}
			} else if (atmType.equalsIgnoreCase("C")) {
				RefString outTac = new RefString(StringUtils.EMPTY);
				this.makeAppTac(msg, icv, div, "948", outTac);
				if (StringUtils.isNotBlank(this.feptxn.getFeptxnIcTac()) && this.feptxn.getFeptxnIcTac().length() >= 16 && outTac.get().length() >= 8) {
					if (!this.feptxn.getFeptxnIcTac().substring(8, 16).equals(StringUtil.toHex(outTac.substring(0, 4)))) {
						rc = ENCReturnCode.ENCCheckTACError;
					} else {
						rc = FEPReturnCode.Normal;
					}
				} else {
					rc = ENCReturnCode.ENCCheckTACError;
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}
		return rc;
	}

	public FEPReturnCode makeAppTac(String msg, String icv, String div, String wKeyid, RefString tac) {
		tac.set(StringUtils.EMPTY);
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		String encFunc = "FN000411";
		FEPReturnCode rc = FEPReturnCode.Normal;
		int encrc = -1;
		try {
			this.prepareAppTac(msg, icv, div, wKeyid, StringUtils.EMPTY, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "makeAppTac", encpar, outputData);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeTerminalAuthenError;
				} else {
					tac.set(StringUtil.toHex(outputData[0]));
					rc = FEPReturnCode.Normal;
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	/**
	 * ===============================================================================================================================================
	 * ==============================================================以下是共用Function===============================================================
	 * ===============================================================================================================================================
	 */

	private FEPReturnCode prepareAppTac(String msg, String icv, String div, String wkeyId, String tac, RefString keyId, RefString inputData1, RefString inputData2) {
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.API);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(wkeyId, 6, StringUtils.SPACE)); // Enc_subcode 右邊補空白至6位
		keyId.set(key.toString());

		inputData1.set(msg);
		if (StringUtils.isBlank(tac)) {
			inputData2.set(this.makeInputDataString(icv, div));
		} else {
			inputData2.set(this.makeInputDataString(icv, div, tac));
		}
		return FEPReturnCode.Normal;
	}
}
