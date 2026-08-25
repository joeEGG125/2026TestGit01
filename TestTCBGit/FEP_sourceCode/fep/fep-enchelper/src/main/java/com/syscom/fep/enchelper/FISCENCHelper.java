package com.syscom.fep.enchelper;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.ENCReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.enums.ENCKeyFunction;
import com.syscom.fep.enchelper.enums.ENCKeyKind;
import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.vo.ENCParameter;
import com.syscom.fep.enchelper.vo.KeyId;
import com.syscom.fep.enchelper.vo.KeyIdentity;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.MathUtil;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.ATMZone;
import com.syscom.fep.vo.enums.FISCReturnCode;
import org.apache.commons.lang3.StringUtils;

public class FISCENCHelper extends ATMENCHelper {
	protected FISCENCHelper(Feptxn feptxn, MessageBase txData) {
		super(feptxn, txData);
	}

	protected FISCENCHelper(Feptxn feptxn) {
		super(feptxn);
	}

	protected FISCENCHelper(String msgId, FEPChannel channel, SubSystem subsys, int ej, String atmno, String atmseq,
							MessageBase txData) {
		super(msgId, channel, subsys, ej, atmno, atmseq, txData);
	}

	protected FISCENCHelper(FISCData txData) {
		super(txData);
	}

	/**
	 * For TSM
	 *
	 * @param msgId
	 * @param ej
	 */
	protected FISCENCHelper(String msgId, int ej) {
		super(msgId, ej);
	}

	/**
	 * 2022-07-28 Richard add
	 *
	 * @param txData
	 */
	public FISCENCHelper(MessageBase txData) {
		super(txData);
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC)
	 *
	 * @param mac 訊息押碼(MAC)
	 * @return FEPReturnCode
	 */
	public FEPReturnCode checkFiscMac(String msgType, String mac) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		String encFunc = "FN000302";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = this.prepareFiscMac(msgType, mac, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				encrc = this.encLib(encFunc, "checkFiscMac", encpar, null);
				rc = this.getENCReturnCode(encrc);
			}
		} catch (Exception e) {
			this.logData.setProgramException(e);
			this.logData.setRemark(this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCLibError;
		}
		return rc;
	}

	/**
	 * 檢核財金MAC & TAC
	 *
	 * @param mac     訊息押碼(MAC)
	 * @param tacType TAC類別
	 * @return FEPReturnCode
	 */
	public FEPReturnCode checkFiscMacAndTac(String mac, int tacType) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData11 = new RefString(StringUtils.EMPTY);
		RefString inputData12 = new RefString(StringUtils.EMPTY);
		RefString keyId2 = new RefString(StringUtils.EMPTY);
		RefString inputData21 = new RefString(StringUtils.EMPTY);
		RefString inputData22 = new RefString(StringUtils.EMPTY);
		String inputData1 = StringUtils.EMPTY;
		String encFunc = "FN000308";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = this.prepareFiscMac("0200", mac, keyId1, inputData11, inputData12);
			if (rc == FEPReturnCode.Normal) {
				rc = prepareAtmTac(tacType, keyId2, inputData21, inputData22);
			}

			if (rc == FEPReturnCode.Normal) {
				inputData1 = this.makeInputDataString("0002", keyId1.get(),
						PolyfillUtil.toString(inputData11.get().length(), "0000"),
						StringUtils.rightPad(inputData11.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData12.get().length(), "0000"),
						StringUtils.rightPad(inputData12.get(), 128, StringUtils.SPACE), keyId2.get(),
						PolyfillUtil.toString(inputData21.get().length(), "0000"),
						StringUtils.rightPad(inputData21.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData22.get().length(), "0000"),
						StringUtils.rightPad(inputData22.get(), 128, StringUtils.SPACE));
				ENCParameter parameter = new ENCParameter();
				parameter.setKeyIdentity(StringUtils.EMPTY);
				parameter.setInputData1(inputData1);
				parameter.setInputData2(StringUtils.EMPTY);
				ENCParameter[] encpar = new ENCParameter[] { parameter };
				encrc = this.encLib(encFunc, "checkFiscMacAndTac", encpar, null);
				if (encrc != 0) {
					if (encrc == 101) {
						rc = ENCReturnCode.ENCCheckMACError;
					} else if (encrc == 102) {
						rc = ENCReturnCode.ENCCheckTACError;
					} else {
						rc = ENCReturnCode.ENCCheckMACTACError;
					}
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1, StringUtils.EMPTY,
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}
		return rc;
	}

	/**
	 * 檢核財金MAC & PINBLOCK & 產生MAC(Response)
	 *
	 * @param mac 訊息押碼(MAC) <history> <modify> <modifier>Kyo</modifier>
	 *            <reason>RC102時 誤回</reason>
	 *            <date>2010/03/17</date> </modify> </history>
	 * @return FEPReturnCode
	 */
	public FEPReturnCode checkFiscMacPinAndMakeMac(String checkMAC, RefString makeMAC) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData11 = new RefString(StringUtils.EMPTY);
		RefString inputData12 = new RefString(StringUtils.EMPTY);
		RefString keyId2 = new RefString(StringUtils.EMPTY);
		RefString inputData21 = new RefString(StringUtils.EMPTY);
		RefString inputData22 = new RefString(StringUtils.EMPTY);
		RefString keyId3 = new RefString(StringUtils.EMPTY);
		RefString inputData31 = new RefString(StringUtils.EMPTY);
		RefString inputData32 = new RefString(StringUtils.EMPTY);
		String inputData1 = StringUtils.EMPTY;
		String encFunc = "FN000307";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFiscMac("0200", checkMAC, keyId1, inputData11, inputData12);
			if (rc == FEPReturnCode.Normal) {
				rc = preparePinData(this.feptxn.getFeptxnPinblock(), this.feptxn.getFeptxnTrk2().substring(28, 32),
						keyId2, inputData21, inputData22);
				if (rc == FEPReturnCode.Normal) {
					rc = prepareFiscMac("0210", makeMAC.get(), keyId3, inputData31, inputData32);
				}
			}

			if (rc == FEPReturnCode.Normal) {
				inputData1 = this.makeInputDataString("0003", keyId1.get(),
						PolyfillUtil.toString(inputData11.get().length(), "0000"),
						StringUtils.rightPad(inputData11.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData12.get().length(), "0000"),
						StringUtils.rightPad(inputData12.get(), 128, StringUtils.SPACE), keyId2.get(),
						PolyfillUtil.toString(inputData21.get().length(), "0000"),
						StringUtils.rightPad(inputData21.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData22.get().length(), "0000"),
						StringUtils.rightPad(inputData22.get(), 128, StringUtils.SPACE), keyId3.get(),
						PolyfillUtil.toString(inputData31.get().length(), "0000"),
						StringUtils.rightPad(inputData31.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData32.get().length(), "0000"),
						StringUtils.rightPad(inputData32.get(), 128, StringUtils.SPACE));
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(StringUtils.EMPTY);
				param.setInputData1(inputData1);
				param.setInputData2(StringUtils.EMPTY);
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "checkFiscMacPinAndMakeMac", encpar, outputData);
				if (encrc != 0) {
					if (encrc == 101) {
						rc = ENCReturnCode.ENCCheckMACError;
					} else if (encrc == 102) {
						rc = ENCReturnCode.ENCMakeMACError;
					} else if (encrc == 1 || encrc == 20 || encrc == 24) {
						rc = ENCReturnCode.ENCCheckPasswordError;
					} else {
						rc = ENCReturnCode.ENCLibError;
					}
				} else {
					makeMAC.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1, StringUtils.EMPTY,
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}
		return rc;
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC)
	 *
	 * @param pcode
	 * @param msgType 訊息類別
	 * @param mac     訊息押碼(MAC)
	 * @return
	 */
	public FEPReturnCode checkOpcMac(String pcode, String msgType, String mac) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		final String encFunc = "FN000302";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;

		try {
			rc = prepareOpcMac(pcode, msgType, mac, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				encrc = this.encLib(encFunc, "checkOpcMac", encpar, null);
				if (encrc != 0) {
					if (encrc == 101) {
						rc = ENCReturnCode.ENCCheckMACError;
					} else {
						rc = ENCReturnCode.ENCLibError;
					}
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}
		return rc;
	}

	/**
	 * 檢核OPC電文訊息押碼(MAC)與ChangeKey
	 *
	 * @param pcode
	 * @param msgType 訊息類別
	 * @param keyId
	 * @return
	 */
	public FEPReturnCode checkOpcCdKey(String pcode, String msgType, String keyId) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		final String encFunc = "FN000103";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = this.prepareOpcMacChangeKey(pcode, msgType, keyId, StringUtils.SPACE, StringUtils.SPACE, keyId1,
					inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				encrc = this.encLib(encFunc, "checkOpcCdKey", encpar, null);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCCheckMACError;
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}
		return rc;
	}

	/**
	 * 產生FISC電文訊息押碼(MAC)
	 *
	 * @param msgType 訊息類別
	 * @param mac     產生的MAC
	 * @return FEPReturnCode
	 */
	public FEPReturnCode makeFiscMac(String msgType, RefString mac) {
		String encFunc = "FN000301";
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		int encrc = -1;

		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			//2024-05-07 FISCENCHelper更改呼叫的方法為 prepareFISCMac BY 明祥
			rc = this.prepareFiscMac(msgType, mac.get(), keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
//				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "makeFiscMac", encpar, outputData);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeMACError;
				} else {
					mac.set(outputData[0]);
				}
			}
		} catch (Exception e) {
			this.logData.setProgramException(e);
			this.logData.setRemark(this.getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	/**
	 * 把字串補足至 8 的倍數 ，不足以F0補足
	 *
	 * @return
	 */
	private String remainderToF0(String inputData) {
		String rtnStr = "";
		int instr = inputData.length();
		instr = instr / 2;
		if (instr % 8 != 0) {
			int remainder = instr % 8;
			remainder = 8 - remainder;
			rtnStr = StringUtils.rightPad(rtnStr, remainder, '0');
			rtnStr = EbcdicConverter.toHex(CCSID.English, rtnStr.length(), rtnStr);
		}

		return inputData + rtnStr;
	}
	/**
	 * 產生OPC電文訊息押碼(MAC)
	 *
	 * @param pcode
	 * @param msgType 訊息類別
	 * @param mac     產生的MAC
	 * @return
	 */
	public FEPReturnCode makeOpcMac(String pcode, String msgType, RefString mac) {
		final String encFunc = "FN000301";
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = this.prepareOpcMac(pcode, msgType, mac.get(), keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "makeOpcMac", encpar, outputData);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeMACError;
				} else {
					mac.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	/**
	 * OPC換KEY
	 *
	 * * 請在呼叫前建立存放4個元素的字串陣列傳入,若執行結果為Normal時可透過此陣列取得結果
	 *
	 * @param pcode
	 * @param msgType
	 * @param keyId
	 * @param newKey
	 * @param randomNum
	 * @param returnData 4個元素的字串陣列
	 * @return
	 */
	public FEPReturnCode changeOpcCdKey(String pcode, String msgType, String keyId, String newKey, String randomNum,
										String[] returnData) {
		if (returnData == null || returnData.length != 4) {
			return FEPReturnCode.ENCArgumentError;
		}
		final String encFunc = "FN000101";
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = this.prepareOpcMacChangeKey(pcode, msgType, keyId, newKey, randomNum, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter param = new ENCParameter();
				param.setKeyIdentity(keyId1.get());
				param.setInputData1(inputData1.get());
				param.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { param };
				String[] outputData = new String[] { StringUtils.EMPTY, StringUtils.EMPTY };
				encrc = this.encLib(encFunc, "changeOpcCdKey", encpar, outputData);
				if (encrc != 0) {
					// 2012-06-01 Modify by Ruling for CDK ERROR RC回10送財金需轉為0301
					if (encrc == 10) {
						rc = FISCReturnCode.KeySyncError;
					} else {
						rc = ENCReturnCode.ENCLibError;
					}
				} else {
					returnData[0] = outputData[0].substring(0, 16);
					returnData[1] = outputData[0].substring(16, 32);
					returnData[2] = outputData[1].substring(0, 8);
					returnData[3] = outputData[1].substring(8, 16);
				}
			}
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}


	//轉換主機的pinblck成財金的pinblock
	public FEPReturnCode ConvertHostPinToFISC(String pin, RefString newPin) {
		final String encFunc = "FN000902";
		//String inputData = this.unPack(pin);
		String mode = "02";
		String inputData1 = mode + pin;
		String inputData2 = "";

		String keyIdentity = StringUtils.EMPTY;
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			KeyIdentity key = new KeyIdentity();
			key.setKeyQty(2);

			//第一把是IMS的PPK
			key.setKeyType1(ENCKeyType.T2);
			key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
			key.getKeyId1().setKeyFunction(ENCKeyFunction.IMS);
			key.getKeyId1().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));

			//第2把key,FISC 的PPK
			key.setKeyType2(ENCKeyType.T2);
			key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
			key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
			key.getKeyId2().setKeySubCode("950");

			keyIdentity = key.toString();
			ENCParameter param = new ENCParameter();
			param.setKeyIdentity(keyIdentity);
			param.setInputData1(inputData1);
			param.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { param };
			String[] outputData = new String[] { StringUtils.EMPTY };
			encrc = this.encLib(encFunc, "ConvertHostPinToFISC", encpar, outputData);
			if (encrc == 0) {
				//newPin.set(this.pack(outputData[0]));
				newPin.set(outputData[0]);
			} else {
				rc = ENCReturnCode.ENCPINBlockConvertError;
			}
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertHostPinToFISC"));
			this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logContext);
			rc = ENCReturnCode.ENCPINBlockConvertError;
		}
		return rc;

	}

	//轉換財金的pinblck成主機的pinblock
	public FEPReturnCode ConvertFISCPinToHost(String pin, RefString newPin,String pcode) {
		final String encFunc = "FN000902";
		//String inputData = this.unPack(pin);
		String mode = "00";
		String inputData1 = mode + pin;
		String inputData2 = "00000000";

		String keyIdentity = StringUtils.EMPTY;
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			KeyIdentity key = new KeyIdentity();
			key.setKeyQty(2);
			if(pcode.equals("2510")){
				//第1把key,FISC 的PPK
				key.setKeyType1(ENCKeyType.T2);
				key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
				key.getKeyId1().setKeySubCode("950");
			}else{
				//第1把key,FISC 的PPK
				key.setKeyType1(ENCKeyType.T2);
				key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.KBPK);
				key.getKeyId1().setKeySubCode("950");
			}


			//第2把是IMS的PPK
			key.setKeyType2(ENCKeyType.T2);
			key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
			key.getKeyId2().setKeyFunction(ENCKeyFunction.IMS);
			key.getKeyId2().setKeySubCode(StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), "IMS"));

			keyIdentity = key.toString();
			ENCParameter param = new ENCParameter();
			param.setKeyIdentity(keyIdentity);
			param.setInputData1(inputData1);
			param.setInputData2(inputData2);
			ENCParameter[] encpar = new ENCParameter[] { param };
			String[] outputData = new String[] { StringUtils.EMPTY };
			encrc = this.encLib(encFunc, "ConvertFISCPinToHost", encpar, outputData);
			if (encrc == 0) {
				//newPin.set(this.pack(outputData[0]));
				newPin.set(outputData[0]);
			} else {
				rc = ENCReturnCode.ENCPINBlockConvertError;
			}
		} catch (Exception e) {
			this.logContext.setProgramException(e);
			this.logContext.setProgramName(StringUtils.join(ProgramName, ".ConvertFISCPinToHost"));
			this.logContext.setRemark(this.getENCLogMessage(encFunc, keyIdentity, inputData1, inputData2,
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
			sendEMS(this.logContext);
			rc = ENCReturnCode.ENCPINBlockConvertError;
		}
		return rc;

	}

	/**
	 * OPC換Key Block PP Key
	 * 請在呼叫前建立存放4個元素的字串陣列傳入,若執行結果為Normal時可透過此陣列取得結果
	 * 國際金融卡業務PIN PROTECTION KEY交換基碼規格調整
	 * @param pcode
	 * @param msgType
	 * @param keyId
	 * @param kbpk
	 * @param returnData 1個元素的字串陣列
	 * @return
	 */
	public FEPReturnCode changeOPCKBPKKey(String pcode, String msgType, String keyId, String kbpk,
										  String[] returnData) {
		if (returnData == null || returnData.length != 4) {
			return FEPReturnCode.ENCArgumentError;
		}
		final String encFunc = "FN000107";
		String keyId1 = "";
		RefString inputData1 = new RefString(StringUtils.EMPTY);
		RefString inputData2 = new RefString(StringUtils.EMPTY);
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			// KeyIdentity key = new KeyIdentity() {
			// 	{
			// 		setKeyQty(1);
			// 		setKeyType1(ENCKeyType.T2);
			// 		setKeyId1(new KeyId() {
			// 			{
			// 				setKeyKind(ENCKeyKind.PPK);
			// 				setKeyFunction(ENCKeyFunction.KBPK);
			// 				setKeySubCode(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 6,
			// 						StringUtils.SPACE));
			// 			}
			// 		});
			// 	}

			// };
			KeyIdentity key = new KeyIdentity();
			key.setKeyQty(2);
			key.setKeyType1(ENCKeyType.T2);
			key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
			key.getKeyId1().setKeyFunction(ENCKeyFunction.KBPK);
			key.getKeyId1().setKeySubCode("950");
			key.setKeyType2(ENCKeyType.T2);
			key.getKeyId2().setKeyKind(ENCKeyKind.CDK);
			key.getKeyId2().setKeyFunction(ENCKeyFunction.KBPK);
			key.getKeyId2().setKeySubCode("950");

			keyId1 = key.toString();
			ENCParameter param = new ENCParameter();
			param.setKeyIdentity(keyId1);
			param.setInputData1(kbpk);
			param.setInputData2("00"); //mode=00, FISC
			// rc = this.prepareOpcMacChangeKey(pcode, msgType, keyId, kbpk, "", keyId1,
			// inputData1, inputData2);
			// if (rc == FEPReturnCode.Normal) {
			// ENCParameter param = new ENCParameter();
			// param.setKeyIdentity(keyId1.get());
			// param.setInputData1(inputData1.get());
			// param.setInputData2(inputData2.get());
			ENCParameter[] despar = new ENCParameter[] { param };
			String[] outputData = new String[] { StringUtils.EMPTY, StringUtils.EMPTY };
			encrc = this.encLib(encFunc, "changeOPCKBPKKey", despar, outputData);
			if (encrc != 0) {
				rc = ENCReturnCode.ENCLibError;
			} else {
				returnData[0] = outputData[1]; //KCV在output2
			}
			// }
		} catch (Exception ex) {
			this.logData.setProgramException(ex);
			this.logData.setRemark(getENCLogMessage(encFunc, keyId1, inputData1.get(), inputData2.get(),
					StringUtils.EMPTY, StringUtils.EMPTY, String.valueOf(encrc)));
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

	/**
	 * Prepare FISC ENC Input Data
	 *
	 * @param msgType
	 * @param mac
	 * @param keyIdentity
	 * @param inputData1
	 * @param inputData2
	 * @return
	 * @throws Exception
	 */
	private FEPReturnCode prepareFiscMac(String msgType, String mac, RefString keyIdentity, RefString inputData1,
										 RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		Short MACType = 0;
		String FISCrc = StringUtils.EMPTY;
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		if ("02".equals(msgType.substring(0, 2))) { // CD/ATM
			key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
		}
		else { // CLR
			key.getKeyId1().setKeyFunction(ENCKeyFunction.RMF);
		}
		// Enc_subcode 右邊補空白至8位
		key.getKeyId1().setKeySubCode(
				StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, StringUtils.SPACE));
		keyIdentity.set(key.toString());
		switch (msgType.substring(2, 4)) {
			case "00": // request
				MACType = this.txData.getMsgCtl().getMsgctlReqmacType();
				FISCrc = this.feptxn.getFeptxnReqRc();
				break;
			case "10": // response
				MACType = this.txData.getMsgCtl().getMsgctlRepmacType();
				if (StringUtils.isBlank(this.feptxn.getFeptxnRepRc())) {
					FISCrc = "4001";
				} else {
					FISCrc = this.feptxn.getFeptxnRepRc();
				}
				break;
			case "02": // confirm
				MACType = this.txData.getMsgCtl().getMsgctlConmacType();
				FISCrc = this.feptxn.getFeptxnConRc();
				break;
		}
		// 組 MAC DATA
		// 2010-07-14 by kyo for SPEC修改:若長度為8則轉換民國年，長度不為八則不轉換
		String icv = StringUtils.EMPTY;
		// modified By Maxine on FEPTXN_REQ_DATETIME[1:8]代替FEPTXN_TX_DATE
		if (this.feptxn.getFeptxnReqDatetime().length() == 14) {
			String rocStr = CalendarUtil.adStringToROCString(this.feptxn.getFeptxnReqDatetime().substring(0, 8));
			icv = rocStr.substring(rocStr.length() - 6, rocStr.length());
		} else {
			icv = this.feptxn.getFeptxnReqDatetime().substring(0, 6);
		}
		String inputData = StringUtils.EMPTY;
		String txAmt = MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00");
		String txAmt_act = MathUtil.toString(this.feptxn.getFeptxnTxAmtAct(), "0.00");
		inputData = this.makeInputDataString(this.feptxn.getFeptxnBkno(), this.feptxn.getFeptxnStan(),
				msgType.substring(2, 4), FISCrc);
		if (MACType != null) {
			switch (MACType) {
				case 2:
					 /* 跨行提領外幣、跨境支付(2555/2556) */
					String txCode = StringUtils.trimToEmpty(this.feptxn.getFeptxnTxCode());
					if ("US".equals(txCode) || "JP".equals(txCode) 
							|| "2555".equals(this.feptxn.getFeptxnPcode()) || "2556".equals(this.feptxn.getFeptxnPcode())) {
						inputData = this.makeInputDataString(inputData, StringUtils.leftPad(txAmt_act, 13, '0'), "000");

					} else {
						inputData = this.makeInputDataString(inputData, StringUtils.leftPad(txAmt, 13, '0'), "000");
					}
					break;
				case 3:
					if ("4001".equals(this.feptxn.getFeptxnRepRc())) {
						// 可用餘額 (NUMERIC DATA ) = FEPTXN_BALA (含二位小數)填補 3 個 '0'
						inputData = this.makeInputDataString(inputData,
								StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnBala().abs(), "0.00"), 13, '0'), "000");
						//StringUtils.leftPad(this.feptxn.getFeptxnBala(), 13, '0'), "000");
					} else {
						inputData = this.makeInputDataString(inputData, StringUtils.leftPad("0", 16, '0'));
						/*可用餘額 (NUMERIC DATA ) = 13 個 ‘0’  填補 3 個 ‘0’ */
					}
					break;
				case 4:
					inputData = this.makeInputDataString(inputData, StringUtils.leftPad(StringUtils.trim(this.feptxn.getFeptxnTrinActno()), 16, '0'));
					break;
				case 5:
					inputData = this.makeInputDataString(inputData, this.feptxn.getFeptxnPinblock());
					break;
				case 6:
					inputData = this.makeInputDataString(inputData, StringUtils.leftPad("0", 16, '0'));
					break;
				case 7:
					inputData = this.makeInputDataString(inputData, StringUtils.leftPad(txAmt, 14, '0'), "00");
					break;
				case 8:
					/* 2566 跨行金融帳戶核驗交易 */
					inputData = this.makeInputDataString(inputData, this.feptxn.getFeptxnTroutActno());
					break;
			}
		}
		/* pinkblock 不須 unpack */
		inputData1.set(this.makeInputDataString(icv, inputData));
		inputData2.set(mac);
		return rc;
	}

	private FEPReturnCode preparePinData(String sscode, String offset, RefString keyIdentity, RefString inputData1,
										 RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(2);
		// spec update by sarah: 信用卡Pin的Keytype為single
		key.setKeyType1(ENCKeyType.S1);
		key.getKeyId1().setKeyKind(ENCKeyKind.PVK);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
		key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());
		key.getKeyId2().setKeyKind(ENCKeyKind.PPK);
		key.getKeyId2().setKeyFunction(ENCKeyFunction.ATM);
		key.getKeyId2().setKeySubCode(DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag())
				? SysStatus.getPropertyValue().getSysstatFbkno()
				: StringUtils.join(SysStatus.getPropertyValue().getSysstatHbkno(), this.feptxn.getFeptxnAtmno()));
		// * 3/3 修改 */
		key.setKeyType2(DbHelper.toBoolean(this.feptxn.getFeptxnFiscFlag()) ? ENCKeyType.T2 : ENCKeyType.T3);
		keyIdentity.set(key.toString());
		if (StringUtils.isBlank(this.feptxn.getFeptxnZoneCode())) {
			throw ExceptionUtil.createException("FEPTXN_ZONE_CODE無值");
		}
		if (ATMZone.TWN.name().equals(this.feptxn.getFeptxnZoneCode())) {
			// 7/29 修改 for APP
			// 2011-05-09 by kyo 開卡流程新電文
			if (ATMTXCD.APP.name().equals(this.feptxn.getFeptxnTxCode())
					|| ATMTXCD.PNM.name().equals(this.feptxn.getFeptxnTxCode())
					|| ATMTXCD.PN0.name().equals(this.feptxn.getFeptxnTxCode())
					|| ATMTXCD.PNX.name().equals(this.feptxn.getFeptxnTxCode())) {
				// 2011-09-21 by YIN
				// * 修改 for 卡號發卡 */
				inputData1.set(StringUtils.EMPTY);
				if (StringUtils.isBlank(this.feptxn.getFeptxnIcmark())) {
					// 帳號發卡
					inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno(), "0000",
							this.feptxn.getFeptxnTrk3().substring(8, 20), offset));
				} else {
					if (SysStatus.getPropertyValue().getSysstatHbkno()
							.equals(this.feptxn.getFeptxnIcmark().substring(0, 3))) { // 卡號發卡
						inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno().substring(2, 16),
								StringUtils.leftPad(String.valueOf(this.feptxn.getFeptxnCardSeq()), 2, '0'), "0000",
								this.feptxn.getFeptxnTroutActno().substring(4, 16), offset));
					} else { // 帳號發卡
						// 2012-07-16 Modify by Ruling for PN0電文改抓帳號後12位
						inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno(), "0000",
								this.feptxn.getFeptxnTroutActno().substring(4, 16), offset));
					}
				}
			} else {
				// 2011-08-18 by YIN 由 CVV判斷發卡方式
				if (Integer.parseInt(this.feptxn.getFeptxnTrk2().substring(32, 35)) > 0) {
					inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno().substring(2, 16),
							this.feptxn.getFeptxnTrk2().substring(35, 37), "0000",
							this.feptxn.getFeptxnTrk2().substring(6, 18), offset));
				} else {
					inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno(), "0000",
							this.feptxn.getFeptxnTrk2().substring(6, 18), offset));
				}
			}
		} else {
			inputData1.set(this.makeInputDataString(this.feptxn.getFeptxnTroutActno().substring(2, 16),
					this.feptxn.getFeptxnTrk2().substring(35, 37), "0000", this.feptxn.getFeptxnTrk2().substring(6, 18),
					offset)); // 海外分行卡
		}
		inputData2.set(this.unPack(sscode));
		return rc;
	}

	private FEPReturnCode prepareOpcMac(String pcode, String msgType, String mac, RefString keyIdentity,
										RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		String FISCrc = StringUtils.EMPTY;

		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.OPC);
		key.getKeyId1().setKeySubCode(StringUtils
				.rightPad(StringUtils.join(SysStatus.getPropertyValue().getSysstatFbkno()), 6, StringUtils.SPACE));
		keyIdentity.set(key.toString());

		switch (msgType.substring(2, 4)) {
			case "00":
				// request
				FISCrc = feptxn.getFeptxnReqRc();
				break;
			case "10":
				// response
				FISCrc = feptxn.getFeptxnRepRc();
				break;
			case "02":
				// confirm
				FISCrc = feptxn.getFeptxnConRc();
				break;
		}
		;

		// 組 MAC DATA
		String icv = StringUtils.EMPTY;
		// modified By Maxine On FEPTXN_REQ_DATETIME[18]代替FEPTXN_TX_DATE;
		if (feptxn.getFeptxnReqDatetime().length() == 14) {
			String dateStr = CalendarUtil.adStringToROCString(this.feptxn.getFeptxnReqDatetime().substring(0, 8));
			icv = dateStr.substring(dateStr.length() - 6, dateStr.length());
		} else {
			icv = feptxn.getFeptxnReqDatetime().substring(0, 6);
		}

		String inputData = StringUtils.EMPTY;
		inputData = this.makeInputDataString(feptxn.getFeptxnBkno(), feptxn.getFeptxnStan(), msgType.substring(2, 4),
				FISCrc);

		switch (pcode) {
			// 2010-06-10 by kyo For SPEC修改補空白改為補零;
			case "0102":
				inputData = this.makeInputDataString(inputData, feptxn.getFeptxnRemark(),
						StringUtils.rightPad("0", 6, '0'));
				break;
			case "3100":
			case "3201":
				inputData = this.makeInputDataString(inputData, feptxn.getFeptxnNoticeId(),
						StringUtils.rightPad("0", 4, '0'));
				break;
			case "3101":
			case "3106":
			case "3107":
			case "3205":
			case "3206":
			case "3210":
			case "3211":
				inputData = this.makeInputDataString(inputData, feptxn.getFeptxnApid(), StringUtils.rightPad("0", 4, '0'));
				break;
			case "3109":
				inputData = this.makeInputDataString(inputData, feptxn.getFeptxnApid(), feptxn.getFeptxnRemark(), "0");
				break;
		}
		;
		inputData1.set(this.makeInputDataString(icv, inputData));
		inputData2.set(mac);
		return rc;
	}

	private FEPReturnCode prepareOpcMacChangeKey(String pcode, String msgType, String keyId, String ekcd, String eknew,
												 RefString keyIdentity, RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		String temp1 = StringUtils.EMPTY;
		String temp2 = StringUtils.EMPTY;
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		switch (pcode) {
			case "0103":
				if ("00".equals(msgType.substring(2, 4))) {
					key.setKeyQty(2);
				} else {
					key.setKeyQty(1);
				}
				key.setKeyType1(ENCKeyType.S1);
				break;
			case "0105":
			case "0107":
				// 2012-04-09 Modify by Ruling FN000101使用2把Key,FN000103使用1把Key
				if ("00".equals(msgType.substring(2, 4))) {
					key.setKeyQty(2);
				} else {
					key.setKeyQty(1);
				}
				key.setKeyType1(ENCKeyType.T2);
				break;
		}
		switch (keyId) {
			case "01":
			case "04":
				key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.OPC);
				break;
			case "02":
			case "05":
				key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
				break;
			case "03":
			case "06":
				key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.RMF);
				break;
			case "11":
			case "12":
				key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
				break;
			case "20":
				key.getKeyId1().setKeyKind(ENCKeyKind.PPK);
				key.getKeyId1().setKeyFunction(ENCKeyFunction.KBPK);
				break;
		}
		key.getKeyId1().setKeySubCode(
				StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 6, StringUtils.SPACE)); // Des_subcode
		// 右邊補空白至6位

		switch (msgType.substring(2, 4)) {
			case "00": // request
				if("0103".equals(pcode)||"0105".equals(pcode)){
					key.getKeyId2().setKeyKind(ENCKeyKind.CDK);
					key.getKeyId2().setKeyFunction(ENCKeyFunction.FISC);
					key.getKeyId2().setKeySubCode(
							StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 6, StringUtils.SPACE));
					temp1 = this.makeInputDataString(ekcd, eknew);
					// 2010-12-07 by kyo for spec update
					temp2 = this.makeInputDataString(
							StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, "0"),
							StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatHbkno(), 8, "0"));
				}else if("0107".equals(pcode)){
					key.getKeyId2().setKeyKind(ENCKeyKind.CDK);
					key.getKeyId2().setKeyFunction(ENCKeyFunction.KBPK);
					key.getKeyId2().setKeySubCode(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 6, StringUtils.SPACE));
					key.getKeyId2().setKeyVersion("1");
					temp1 = StringUtils.join("0080", ekcd);
				}
				keyIdentity.set(key.toString());
				inputData1.set(temp1);
				inputData2.set(temp2);
				break;
//			case "02": // Confirm
//				key.setKeyId2(null);
//				key.setKeyId3(null);
//				inputData1.set(StringUtils.EMPTY);
//				inputData2.set(StringUtils.EMPTY);
		}
		keyIdentity.set(key.toString());
		inputData1.set(temp1);
		inputData2.set(temp2);
		return rc;
	}

	/**
	 * 檢核財金電文MAC &IC TAC
	 *
	 * @param checkMAC 訊息押碼(MAC)
	 * @param txAmt    提領金額
	 * @param txCur    幣別碼
	 * @param country  國別碼
	 * @param tacType  TAC類別
	 * @return FEPReturnCode <history> <modify> <modifier>Fly</modifier>
	 *         <reason>新增晶片金融卡跨國提款及消費扣款交易</reason> <date>2017/06/09</date> </modify>
	 *         </history> zk addd
	 */
	public FEPReturnCode checkFISCMACICTAC(String checkMAC, String txCur, String country, int tacType) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData11 = new RefString(StringUtils.EMPTY);
		RefString inputData12 = new RefString(StringUtils.EMPTY);
		RefString keyId2 = new RefString(StringUtils.EMPTY);
		RefString inputData21 = new RefString(StringUtils.EMPTY);
		RefString inputData22 = new RefString(StringUtils.EMPTY);
		String inputData1 = "";
		final String encFunc1 = "FN000308";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFISCICMac("0200", checkMAC, keyId1, inputData11, inputData12);
			if (rc == FEPReturnCode.Normal) {
				rc = prepareATMICTac(tacType, txCur, country, keyId2, inputData21, inputData22);
			}

			if (rc == FEPReturnCode.Normal) {
				inputData1 = StringUtils.join("0002", keyId1.get(),
						PolyfillUtil.toString(inputData11.get().length(), "0000"),
						StringUtils.rightPad(inputData11.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData12.get().length(), "0000"),
						StringUtils.rightPad(inputData12.get(), 128, StringUtils.SPACE), keyId2.get(),
						PolyfillUtil.toString(inputData21.get().length(), "0000"),
						StringUtils.rightPad(inputData21.get(), 128, StringUtils.SPACE),
						PolyfillUtil.toString(inputData22.get().length(), "0000"),
						StringUtils.rightPad(inputData22.get(), 128, StringUtils.SPACE));
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(StringUtils.EMPTY);
				tempVar.setInputData1(inputData1);
				tempVar.setInputData2(StringUtils.EMPTY);
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc1, "CheckFISCMACICTAC", encpar, outputData);
				rc = getENCReturnCode(encrc);
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc1, keyId1.get(), inputData1, "", "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}

		return rc;
	}

	/*
	 * zk add
	 */
	private FEPReturnCode prepareFISCICMac(String msgType, String mac, RefString keyIdentity, RefString inputData1,
										   RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		Byte MACType = 0;
		String FISCrc = "";

		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		if ("02".equals(msgType.substring(0, 2))) { // CD/ATM
			key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
		} else { // CLR
			key.getKeyId1().setKeyFunction(ENCKeyFunction.RMF);
		}
		// Enc_subcode 右邊補空白至8位
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 8, ' '));
		keyIdentity.set(key.toString());
		if ("00".equals(msgType.substring(2, 4))) { // request{
			MACType = txData.getMsgCtl().getMsgctlReqmacType().byteValue();
			FISCrc = feptxn.getFeptxnReqRc();
		} else if ("10".equals(msgType.substring(2, 4))) {// response
			MACType = txData.getMsgCtl().getMsgctlRepmacType().byteValue();
			if (StringUtils.isBlank(feptxn.getFeptxnRepRc())) {
				FISCrc = "4001";
			} else {
				FISCrc = feptxn.getFeptxnRepRc();
			}
		} else if ("02".equals(msgType.substring(2, 4))) {// confirm
			MACType = txData.getMsgCtl().getMsgctlConmacType().byteValue();
			FISCrc = feptxn.getFeptxnConRc();
		}

		// 組 MAC DATA
		String icv = "";
		if (feptxn.getFeptxnReqDatetime().length() == 14) {
			icv = CalendarUtil.adStringToROCString(feptxn.getFeptxnReqDatetime().substring(0, 8)).substring(
					CalendarUtil.adStringToROCString(feptxn.getFeptxnReqDatetime().substring(0, 8)).length() - 6,
					CalendarUtil.adStringToROCString(feptxn.getFeptxnReqDatetime().substring(0, 8)).length() - 6 + 6);
		} else {
			icv = feptxn.getFeptxnReqDatetime().substring(0, 6);
		}
		String inputData = "";
		inputData = StringUtils.join(feptxn.getFeptxnBkno(), feptxn.getFeptxnStan(), msgType.substring(2, 4), FISCrc);

		switch (MACType) {
			case 2:
				inputData = this.makeInputDataString(inputData,
						StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmtAct(), "0.00"), 13, '0'), "000");
				break;
			case 3:
				if ("4001".equals(feptxn.getFeptxnRepRc())) {
					if ("FISC".equals(feptxn.getFeptxnChannel())) {
						inputData = this.makeInputDataString(inputData,
								StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnBalb().abs(), "0.00"), 13, '0'),
								"000"); // 可用餘額 (NUMERIC DATA ) = FEPTXN_BALB (含二位小數)填補 3 個 ‘0’
					} else {
						inputData = this.makeInputDataString(inputData,
								StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnBala().abs(), "0.00"), 13, '0'),
								"000"); // 可用餘額 (NUMERIC DATA ) = FEPTXN_BALA (含二位小數)填補 3 個 ‘0’
						//inputData = this.makeInputDataString(inputData,
						//		StringUtils.leftPad(this.feptxn.getFeptxnBala(), 13, '0'),
						//		"000"); // 可用餘額 (NUMERIC DATA ) = FEPTXN_BALA (含二位小數)填補 3 個 ‘0’
					}
				} else {
					inputData = StringUtils.join(inputData, StringUtils.leftPad("0", 16, '0'));// 可用餘額 (NUMERIC DATA ) = 13
					// 個 ‘0’填補 3 個 ‘0’
				}
				break;
		}
//		String data =StringUtils.join(icv, unPack(inputData));
//		String data1 = EbcdicConverter.toHex(CCSID.English,data.length(),data);
//		inputData1.set(remainderToF0(data1));

		inputData1.set(StringUtils.join(icv, unPack(inputData)));

//		inputData1.set(remainderToF0(StringUtils.join(icv, unPack(inputData))));
		inputData2.set(mac);
		return rc;
	}

	/**
	 * 產生FISC電文訊息押碼(MAC)
	 *
	 * @param msgType 訊息類別
	 * @param mac     產生的MAC
	 * @return FEPReturnCode
	 *
	 *         zk add
	 */
	public FEPReturnCode makeFISCICMAC(String msgType, RefString mac) {
		final String encFunc = "FN000301";
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		int encrc = -1;

		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFISCICMac(msgType, mac.get(), keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc, "MakeFISCICMAC", encpar, outputData);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeMACError;
				} else {
					mac.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC) for 晶片金融卡跨國提款及消費扣款交易
	 *
	 * @param mac 訊息押碼(MAC)
	 * @return FEPReturnCode <history> <modify> <modifier>Ruling</modifier>
	 *         <reason>新增晶片金融卡跨國提款及消費扣款交易</reason> <date>2017/06/29</date> </modify>
	 *         </history> zk add
	 */
	public FEPReturnCode checkFISCICMAC(String msgType, String mac) {
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		final String encFunc = "FN000302";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFISCICMac(msgType, mac, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				// string[] outputData = new[] { "" };
				encrc = encLib(encFunc, "checkFISCICMAC", encpar, null);
				rc = getENCReturnCode(encrc);

			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCLibError;
		}

		return rc;
	}

	/**
	 * 檢核MASTER EMV卡ARQC並產生ARPC
	 *
	 * @param arqc      ARQC
	 * @param arc       ARC
	 * @param atc       ATC
	 * @param un        UN
	 * @param inputData INPUTDATA
	 * @param binkey    信用卡BIN基碼
	 * @param arpc      ARPC
	 * @return FEPReturnCode <history> <modify> <modifier>Ruling</modifier>
	 *         <reason>依信用卡bin抓哪把Key，增加傳入參數binKey</reason> <date>2016/06/24</date>
	 *         </modify> </history>
	 */
	public FEPReturnCode checkARQCAndMakeARPC(String arqc, String arc, String atc, String un, String inputData,
											  String binkey, RefString arpc) {
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		final String encFunc = "FN000501";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareARQCData(arqc, arc, atc, un, inputData, binkey, keyId1, inputData1, inputData2);

			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc, "checkARQCAndMakeARPC", encpar, outputData);
				if (encrc != 0) {
					rc = FEPReturnCode.ENCCheckTACError;
				} else {
					arpc.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}

		return rc;
	}

	private FEPReturnCode prepareARQCData(String arqc, String arc, String atc, String un, String inputData,
										  String binkey, RefString keyIdentity, RefString inputData1, RefString inputData2) {
		FEPReturnCode rc = FEPReturnCode.Normal;

		// 2016-06-30 Modify by Ruling for 依不同BIN抓不同的ENC Key來驗ARQC
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MKAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.MC);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(binkey, 6, ' ')); // Enc_subcode 右邊補空白至6位
		keyIdentity.set(key.toString());

		// Prepare InputData
		int icSeqNo = 0;
		String sIcSeqNo;
		try {
			icSeqNo = Integer.parseInt(feptxn.getFeptxnIcSeqno());
		} catch (Exception ex) {}
		sIcSeqNo = StringUtils.leftPad(String.valueOf(icSeqNo), 3, '0');
		sIcSeqNo = sIcSeqNo.substring(sIcSeqNo.length() - 2, sIcSeqNo.length());
		inputData1.set(this.makeInputDataString(
				"1",
				StringUtils.leftPad(feptxn.getFeptxnTrk2(), 16, '0').substring(2, 16),
				sIcSeqNo,
				StringUtils.leftPad(atc, 4, '0').substring(0, 4),
				StringUtils.leftPad(un, 8, '0'),
				StringUtils.leftPad(arqc, 16, ' ').substring(0, 16),
				StringUtils.leftPad(arc, 4, '0').substring(0, 4)));
		if ((inputData.trim().length() % 8) == 0) {
			inputData2.set(inputData.trim());
		} else {
			// 補滿8的倍數
			inputData2.set(StringUtils.rightPad(inputData, ((inputData.trim().length() / 8) + 1) * 8, '0'));
		}
		return rc;
	}

	/**
	 * 檢核ATM電文訊息押碼(MAC)以及產生ATM電文訊息押碼(MAC)
	 *
	 * @param mac 訊息押碼(MAC)
	 * @return FEPReturnCode
	 */
	public FEPReturnCode checkFISCMACAndMakeMAC(String checkMAC, RefString makeMAC) {
		RefString keyId1 = new RefString(StringUtils.EMPTY);
		RefString inputData11 = new RefString(StringUtils.EMPTY);
		RefString inputData12 = new RefString(StringUtils.EMPTY);
		RefString keyId2 = new RefString(StringUtils.EMPTY);
		RefString inputData21 = new RefString(StringUtils.EMPTY);
		RefString inputData22 = new RefString(StringUtils.EMPTY);
		String inputData1 = "";
		final String encFunc = "FN000309";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {

			rc = prepareFiscMac("0200", checkMAC, keyId1, inputData11, inputData12);
			if (rc == FEPReturnCode.Normal) {
				rc = prepareFiscMac("0210", makeMAC.get(), keyId2, inputData21, inputData22);
			}

			if (rc == FEPReturnCode.Normal) {
				inputData1 = "0002" + keyId1.get() + PolyfillUtil.toString(inputData11.get().length(), "0000")
						+ StringUtils.rightPad(inputData11.get(), 128, ' ')
						+ PolyfillUtil.toString(inputData12.get().length(), "0000")
						+ StringUtils.rightPad(inputData12.get(), 128, ' ') + keyId2.get()
						+ PolyfillUtil.toString(inputData21.get().length(), "0000")
						+ StringUtils.rightPad(inputData21.get(), 128, ' ')
						+ PolyfillUtil.toString(inputData22.get().length(), "0000")
						+ StringUtils.rightPad(inputData22.get(), 128, ' ');

				// 複合encFunc皆放在inputData1
				// key_identify =N/A
				// Input_data_1：
				// input_length_1(N4)=2(2個Function依序執行，檢核錯誤時，即回傳錯誤代碼)
				// input_string_1_1(X327)=值，input_string(X327)=key_identify(X63)+input_data1(X132)+input_data2(X132)
				// input_string_1_2(X327)=值...
				// Input_data_2 =N/A
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity("");
				tempVar.setInputData1(inputData1);
				tempVar.setInputData2("");
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc, "checkFISCMACAndMakeMAC", encpar, outputData);
				if (encrc == 0) {
					makeMAC.set(outputData[0]);
				}
				rc = getENCReturnCode(encrc);
			}

		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1, "", "", "", String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}

		return rc;
	}

	/**
	 * 檢核財金Pending MAC
	 *
	 * @param msgFunc 00:request,10:response,02:confirm
	 * @param mac     訊息押碼(MAC)
	 * @return FEPReturnCode
	 */
	public FEPReturnCode checkFISCMACPend(String msgFunc, String mac) {
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		final String encFunc = "FN000302";
		int encrc = -1;
		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFISCMacPend(msgFunc, mac, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc, "CheckFISCMACPend", encpar, outputData);
				rc = getENCReturnCode(encrc);
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCCheckMACError;
		}

		return rc;
	}

	/**
	 * 產生財金外幣清算與結帳MAC
	 *
	 * @param msgFunc     00:request,10:response,02:confirm
	 * @param flagCLRBank 0:與財金間之MAC, 1:與清算行間之MAC
	 * @param mac         產生的MAC
	 * @return FEPReturnCode
	 */
	public FEPReturnCode makeFISCMACPend(String msgFunc, RefString mac) {
		final String encFunc = "FN000301";
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		int encrc = -1;

		FEPReturnCode rc = FEPReturnCode.Normal;
		try {
			rc = prepareFISCMacPend(msgFunc, mac.get(), keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				encrc = encLib(encFunc, "MakeFISCMACPend", encpar, outputData);
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeMACError;
				} else {
					mac.set(outputData[0]);
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(), inputData2.get(), "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeMACError;
		}
		return rc;
	}

	private FEPReturnCode prepareFISCMacPend(String msgFunc, String mac, RefString keyIdentity, RefString inputData1,
											 RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		Byte MACType = 0;
		String FISCrc = "";

		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T2);
		key.getKeyId1().setKeyKind(ENCKeyKind.MAC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.ATM);
		key.getKeyId1().setKeySubCode(StringUtils.rightPad(SysStatus.getPropertyValue().getSysstatFbkno(), 6, ' '));
		keyIdentity.set(key.toString());
		if ("00".equals(msgFunc)) {// request
			MACType = this.txData.getMsgCtl().getMsgctlReqmacType().byteValue();
			FISCrc = this.feptxn.getFeptxnReqRc();
		} else if ("10".equals(msgFunc)) {// response
			MACType = this.txData.getMsgCtl().getMsgctlRepmacType().byteValue();
			FISCrc = this.feptxn.getFeptxnRepRc();
		}

		// 組 MAC DATA
		String icv = "";
		if (this.feptxn.getFeptxnTxDate().length() == 8) {
			icv = CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).substring(
					CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).length() - 6,
					CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTxDate()).length() - 6 + 6);
		} else {
			icv = this.feptxn.getFeptxnTxDate();
		}

		// MACType=11時_fepTxn.FEPTXN_TBSDY_FISC為空
		String tdsdy_fisc = "";
		if (MACType != 11) {
			if ("00000000".equals(this.feptxn.getFeptxnTbsdyFisc())) {// "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
				tdsdy_fisc = "000000";
			} else {
				tdsdy_fisc = CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTbsdyFisc()).substring(
						CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTbsdyFisc()).length() - 6,
						CalendarUtil.adStringToROCString(this.feptxn.getFeptxnTbsdyFisc()).length() - 6 + 6);
			}
		}
		String inputData = "";
		String oriSeq = StringUtils.join(this.feptxn.getFeptxnDesBkno(), this.feptxn.getFeptxnOriStan());
		inputData = StringUtils.join(this.feptxn.getFeptxnBkno(), this.feptxn.getFeptxnStan(), msgFunc, FISCrc);

		switch (MACType) {
			case 11:
				inputData = StringUtils.join(inputData, StringUtils.rightPad(oriSeq, 16, '0'));
				break;
			case 12:
				inputData = StringUtils.join(inputData, tdsdy_fisc, oriSeq);
				break;
			case 13:
				inputData = StringUtils.join(inputData, tdsdy_fisc, oriSeq,
						StringUtils.rightPad(this.feptxn.getFeptxnRsCode(), 16, '0'));
				break;
			case 14:
				inputData = StringUtils.join(inputData, tdsdy_fisc, oriSeq,
						StringUtils.rightPad(this.feptxn.getFeptxnRemark(), 16, '0'));
				break;
			case 15:
				inputData = StringUtils.join(inputData, tdsdy_fisc, oriSeq, StringUtils
						.rightPad(StringUtils.join(this.feptxn.getFeptxnRemark(), this.feptxn.getFeptxnRsCode()), 16, '0'));
				break;
		}

		inputData1.set(StringUtils.join(icv, inputData));

		if (StringUtils.isNotBlank(mac)) {
			inputData2.set(mac);
		} else {
			inputData2.set("");
		}

		return rc;
	}
	/**
	 *
	 * 產生交易驗證碼(TAC)
	 * @param tacType MSGCTL_TAC_TYPE(TAC類別)
	 * @param pack
	 * @param tac
	 * @return
	 */
	public FEPReturnCode makeATMTACForSimu(int tacType, boolean pack, RefString tac) {
		final String encFunc = "FN000401";
		FEPReturnCode rc = FEPReturnCode.Normal;
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		int encrc = -1;


		try {
			rc = prepareATMTacForSimu(tacType, keyId1, inputData1, inputData2);
			if (rc == FEPReturnCode.Normal) {
				ENCParameter tempVar = new ENCParameter();
				tempVar.setKeyIdentity(keyId1.get());
				tempVar.setInputData1(inputData1.get());
				tempVar.setInputData2(inputData2.get());
				ENCParameter[] encpar = new ENCParameter[] { tempVar };
				String[] outputData = new String[] { "" };
				RefBase<String[]> refBaseOutPut = new RefBase<>(outputData);
				encrc = this.encLib(encFunc, "MakeATMTACForSimu", encpar,refBaseOutPut.get());
				outputData = refBaseOutPut.get();
				if (encrc != 0) {
					rc = ENCReturnCode.ENCMakeTerminalAuthenError;
				} else {
					if (pack){
						tac.set(pack(outputData[0]));
					}else {
						tac.set(outputData[0]);
					}
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			logData.setRemark(getENCLogMessage(encFunc, keyId1.get(), inputData1.get(),"", "", "",
					String.valueOf(encrc)));
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeTerminalAuthenError;
		}
		return rc;
	}

	public String pack(String data){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length(); i++){
			String x = data.substring(i, i+1);
			switch (x)
			{
				case "A":
					sb.append(":");
					break;
				case "B":
					sb.append(";");
					break;
				case "C":
					sb.append("<");
					break;
				case "D":
					sb.append("=");
					break;
				case "E":
					sb.append(">");
					break;
				case "F":
					sb.append("?");
					break;
				default:
					sb.append(x);
					break;
			}
		}
		//return Utility.StringLib.ToString(sb.ToString());
		return sb.toString();
	}
	public FEPReturnCode makeEBILLTAC(RefString tac) {
		final String encFunc = "FN000311";
		FEPReturnCode rc = FEPReturnCode.Normal;
		RefString keyId1 = new RefString("");
		RefString inputData1 = new RefString("");
		RefString inputData2 = new RefString("");
		int encrc = -1;
		try {
			String wMSG = "";
			String wDIV = "";
			String wICV = "";
			String wTMP = "";
			String wTAC = "";
			//#region " 2. Prepare TAC DATA for Check "
			//                //交易訊息
			//                //wMSG = _fepTxn.FEPTXN_TROUT_ACTNO.PadLeft(16, '0').Substring(11, 4) +      //不足16位, 左補0取末4碼
			//                //        _fepTxn.FEPTXN_TX_DATETIME_FISC.PadLeft(14, '0').Substring(6, 8) +  //後八碼(ddHHmmss)
			//                //        "0815";
			//                //2020/02/11 調整wMSG內容
			wMSG = "00" + StringUtils.leftPad(this.feptxn.getFeptxnTxDatetimeFisc(), 14, '0');
			////wMSG = GetSHA256Hash(wMSG);     //sha256 hash後轉大寫
			//                //wTMP = GetSHA256Hash(wMSG);     //將<MSG>再次進行sha256 hash後轉大寫
			//                //wDIV = wTMP.Substring(0, 48);   //基碼多樣化資料，取W_TMP前48位
			//                //wICV = wTMP.Substring(48, 16);  //取W_TMP後16位
			//                #endregion
			//
			//                #region " 3. Call DES 檢核EBILL交易電文TAC "
			//                //wTAC = StringLib.HexToASCIIString(_fepTxn.FEPTXN_IC_TAC);
			//                rc = PrepareEBILLTAC(wMSG, out keyId1, out inputData1, out inputData2);
			//                //if (rc != FEPReturnCode.Normal)
			//                //    return rc;
			//                //CALL DES LIB
			ENCParameter tempVar = new ENCParameter();
			tempVar.setKeyIdentity(keyId1.get());
			tempVar.setInputData1(inputData1.get());
			tempVar.setInputData2(inputData2.get());
			ENCParameter[] encpar = new ENCParameter[] { tempVar };
			String[] outputData = new String[] { "" ,""};
			RefBase<String[]> refBaseOutPut = new RefBase<>(outputData);
			encrc = this.encLib(encFunc, "CheckEBILLTAC", encpar,refBaseOutPut.get());
			outputData = refBaseOutPut.get();
			if (encrc != 0) {
				rc = ENCReturnCode.ENCMakeTerminalAuthenError;
			} else {
				if (!StringUtils.isBlank(outputData[1])){
					tac.set(pack(outputData[1]));
					rc = FEPReturnCode.Normal;
				}else {
					rc = ENCReturnCode.ENCMakeTerminalAuthenError;
				}
			}
		} catch (Exception ex) {
			logData.setProgramException(ex);
			sendEMS(logData);
			rc = ENCReturnCode.ENCMakeTerminalAuthenError;
		}
		return rc;
	}
	public FEPReturnCode prepareATMTacForSimu(int tacType, RefString keyIdentity, RefString inputData1, RefString inputData2) throws Exception {
		FEPReturnCode rc = FEPReturnCode.Normal;
		StringBuilder tacInputData = new StringBuilder();
		// Prepare Key Identity
		KeyIdentity key = new KeyIdentity();
		key.setKeyQty(1);
		key.setKeyType1(ENCKeyType.T3);
		key.getKeyId1().setKeyKind(ENCKeyKind.ICC);
		key.getKeyId1().setKeyFunction(ENCKeyFunction.C6);
		key.getKeyId1().setKeySubCode(SysStatus.getPropertyValue().getSysstatHbkno());
		keyIdentity.set(key.toString());

		// Prepare InputData
		switch (tacType) {
			case 1:
				/*for ‘2510’, ‘2561’, ‘2562’, ‘2563’, ‘2564’  提款, 全國繳費*/
				/*for ‘IWD’, ‘IFW’, ‘ACW’, ‘EFT’ 提款, 全國繳費*/
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				break;
			case 2:
				/* for ‘2521’,‘2522’, ‘2523’, ‘2524’, ‘2525’ 轉帳 */
				/* for "IFT" ,"ATF", "BFT","IPA", "IDR" 轉帳/預約轉帳/存款 */
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTrinActno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				break;
			case 3:
				/*for ‘2541’, ‘2552’  消費扣款, 授權完成 */
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
				StringUtil.append(tacInputData,  StringUtils.rightPad(this.feptxn.getFeptxnMerchantId(), 30, '0'));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				break;
			case 4:
				/*for ‘2551’  預先授權 */
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqnoPreauth());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnTxAmtPreauth()==null?"":this.feptxn.getFeptxnTxAmtPreauth().toString(),12, '0'));
				StringUtil.append(tacInputData, "00");
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimePreauth());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				break;
			case 5:
				/*for ‘2531’ , ‘2532’, ‘2567’, ‘2568’ 跨行繳款,自行繳款,全國繳稅 */
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, StringUtils.leftPad(MathUtil.toString(this.feptxn.getFeptxnTxAmt(), "0.00"), 14, '0'));
				StringUtil.append(tacInputData, StringUtils.rightPad(this.feptxn.getFeptxnAtmno(), 8, '0'));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTxDatetimeFisc());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPaytype());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				if (feptxn.getFeptxnPaytype() !=null && feptxn.getFeptxnPaytype().length()>=2 && !"15".equals(feptxn.getFeptxnPaytype().substring(0,2))){
					StringUtil.append(tacInputData, this.feptxn.getFeptxnReconSeqno());
				}else {
					StringUtil.append(tacInputData, "0000");
					StringUtil.append(tacInputData, this.feptxn.getFeptxnIdno());
				}
				break;
			case 6:
				/*for ‘2500’    查詢*/
				/*for  "IIQ", "IFE", "AIN"  查詢*/
				StringUtil.append(tacInputData, this.feptxn.getFeptxnIcSeqno());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnPcode());
				StringUtil.append(tacInputData, this.feptxn.getFeptxnAtmChk().substring(6, 8));
				StringUtil.append(tacInputData, this.feptxn.getFeptxnTroutActno());
				break;
		}

		//personal information 1 + personal information 2 + personal information 3
		String divData = unPack(this.makeInputDataString(
				this.feptxn.getFeptxnIcmark().substring(0, 12),
				this.feptxn.getFeptxnIcmark().substring(0, 12)));
		inputData1.set(tacInputData.toString());
		// inputData2 = divData + UnPack(_fepTxn.FEPTXN_IC_TAC);
		//inputData2 = StringLib.ToString("FISCCARD") + divData;
		inputData2.set(StringUtils.join("FISCCARD", divData));
		return rc;
	}

}

