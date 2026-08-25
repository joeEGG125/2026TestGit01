package com.syscom.fep.server.common.parse;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.parse.StringToFieldAnnotationParser;
import com.syscom.fep.vo.text.atm.ATMTextBase;
import com.syscom.fep.vo.text.atm.request.*;

import java.util.Locale;

public class ATMTextParser {
	private static ATMTextParser instance = new ATMTextParser();

	private ATMTextParser() {}

	public static ATMTextParser getInstance() {
		return instance;
	}

	/**
	 * TODO 解析ATM電文
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public ATMTextBase parse(String data) throws Exception {
		// 電文的hex值應該是不分大小寫的, 我測試工具已經先改成以大寫送出, 但我們無法保證未來ATM送出的是大寫還是小寫, 所以你最好也改一下不分大小寫來比較.
		// 2021-07-26 by Ashiang suggestion
		String sCode = data.substring(4, 10).toUpperCase(Locale.ENGLISH);
		switch (sCode) {
			// 將上行電文Request搬到大Class
			case "494951": // IIQ
				return new StringToFieldAnnotationParser<IIQRequest>(IIQRequest.class).readIn(data);
			case "495744": // IWD
				return new StringToFieldAnnotationParser<IWDRequest>(IWDRequest.class).readIn(data);
			case "495746": // IWF
				return new StringToFieldAnnotationParser<IWFRequest>(IWFRequest.class).readIn(data);
			case "435746": // CWF
				return new StringToFieldAnnotationParser<CWFRequest>(CWFRequest.class).readIn(data);
			case "494646": // IFF
				return new StringToFieldAnnotationParser<IFFRequest>(IFFRequest.class).readIn(data);
			case "494654": // IFT
				return new StringToFieldAnnotationParser<IFTRequest>(IFTRequest.class).readIn(data);
			case "495059": // IPY
				return new StringToFieldAnnotationParser<IPYRequest>(IPYRequest.class).readIn(data);
			case "495046": // IPF
				return new StringToFieldAnnotationParser<IPFRequest>(IPFRequest.class).readIn(data);
			case "454654": // EFT
				return new StringToFieldAnnotationParser<EFTRequest>(EFTRequest.class).readIn(data);
			case "454646": // EFF
				return new StringToFieldAnnotationParser<EFFRequest>(EFFRequest.class).readIn(data);
			case "4F4445": // ODE
				return new StringToFieldAnnotationParser<ODERequest>(ODERequest.class).readIn(data);
			case "4F4452": // ODR
				return new StringToFieldAnnotationParser<ODRRequest>(ODRRequest.class).readIn(data);
			case "4F4446": // ODF
				return new StringToFieldAnnotationParser<ODFRequest>(ODFRequest.class).readIn(data);
			case "4F4654": // OFT
				return new StringToFieldAnnotationParser<OFTRequest>(OFTRequest.class).readIn(data);
			case "435550": // CUP
				return new StringToFieldAnnotationParser<CUPRequest>(CUPRequest.class).readIn(data);
			case "495143": // IQC
				return new StringToFieldAnnotationParser<IQCRequest>(IQCRequest.class).readIn(data);
			case "435756": // CWV
				return new StringToFieldAnnotationParser<CWVRequest>(CWVRequest.class).readIn(data);
			case "495156": // IQV
				return new StringToFieldAnnotationParser<IQVRequest>(IQVRequest.class).readIn(data);
			case "43574D": // CWM
				return new StringToFieldAnnotationParser<CWMRequest>(CWMRequest.class).readIn(data);
			case "49514D": // IQM
				return new StringToFieldAnnotationParser<IQMRequest>(IQMRequest.class).readIn(data);
			case "434156": // CAV
				return new StringToFieldAnnotationParser<CAVRequest>(CAVRequest.class).readIn(data);
			case "43414D": // CAM
				return new StringToFieldAnnotationParser<CAMRequest>(CAMRequest.class).readIn(data);
			case "43414A": // CAJ
				return new StringToFieldAnnotationParser<CAJRequest>(CAJRequest.class).readIn(data);
			case "4F4646": // OFF
				return new StringToFieldAnnotationParser<OFFRequest>(OFFRequest.class).readIn(data);
			case "434655": // CFU
				return new StringToFieldAnnotationParser<CFURequest>(CFURequest.class).readIn(data);
			case "434650": // CFP
				return new StringToFieldAnnotationParser<CFPRequest>(CFPRequest.class).readIn(data);
			case "434643": // CFC
				return new StringToFieldAnnotationParser<CFCRequest>(CFCRequest.class).readIn(data);
			case "434656": // CFV
				return new StringToFieldAnnotationParser<CFVRequest>(CFVRequest.class).readIn(data);
			case "43464D": // CFM
				return new StringToFieldAnnotationParser<CFMRequest>(CFMRequest.class).readIn(data);
			case "43464A": // CFJ
				return new StringToFieldAnnotationParser<CFJRequest>(CFJRequest.class).readIn(data);
			case "455550": // EUP
				return new StringToFieldAnnotationParser<EUPRequest>(EUPRequest.class).readIn(data);
			case "454655": // EFU
				return new StringToFieldAnnotationParser<EFURequest>(EFURequest.class).readIn(data);
			case "455155": // EQU
				return new StringToFieldAnnotationParser<EQURequest>(EQURequest.class).readIn(data);
			case "455756": // EWV
				return new StringToFieldAnnotationParser<EWVRequest>(EWVRequest.class).readIn(data);
			case "454650": // EFP
				return new StringToFieldAnnotationParser<EFPRequest>(EFPRequest.class).readIn(data);
			case "455150": // EQP
				return new StringToFieldAnnotationParser<EQPRequest>(EQPRequest.class).readIn(data);
			case "45574D": // EWM
				return new StringToFieldAnnotationParser<EWMRequest>(EWMRequest.class).readIn(data);
			case "454643": // EFC
				return new StringToFieldAnnotationParser<EFCRequest>(EFCRequest.class).readIn(data);
			case "455143": // EQC
				return new StringToFieldAnnotationParser<EQCRequest>(EQCRequest.class).readIn(data);
			case "454156": // EAV
				return new StringToFieldAnnotationParser<EAVRequest>(EAVRequest.class).readIn(data);
			case "454656": // EFV
				return new StringToFieldAnnotationParser<EFVRequest>(EFVRequest.class).readIn(data);
			case "45414D": // EAM
				return new StringToFieldAnnotationParser<EAMRequest>(EAMRequest.class).readIn(data);
			case "45464D": // EFM
				return new StringToFieldAnnotationParser<EFMRequest>(EFMRequest.class).readIn(data);
			case "504654": // PFT
				return new StringToFieldAnnotationParser<PFTRequest>(PFTRequest.class).readIn(data);
			case "504646": // PFF
				return new StringToFieldAnnotationParser<PFFRequest>(PFFRequest.class).readIn(data);
			// TODO
			default:
				// 這裡列印一下log, 方便check漏加case
				LogHelperFactory.getGeneralLogger().error("Parser not implement, sCode = [", sCode, "]");
		}
		return null;
	}
}
