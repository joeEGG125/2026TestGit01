package com.syscom.fep.vo.text.fisc;

import com.syscom.fep.base.configurer.FEPConfig;
import com.syscom.fep.base.enums.FISCEncoding;
import com.syscom.fep.frmcommon.ebcdic.CCSID;
import com.syscom.fep.frmcommon.ebcdic.EbcdicConverter;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.FunctionType;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.enums.PCodeSubSystem;

/**
 * 定義財金電文共用Header,所有財金電文必須繼承此類別
 */
public abstract class FISCHeader extends FEPBase {
	protected String FISCMessage;
	// System Supervisory Control Header
	private String systemSupervisoryControlHeader;
	// System Network Identifier
	private String systemNetworkIdentifier;
	// Adderss Control Field
	private String adderssControlField;
	// Message Type
	private String messageType;
	// Processing Code
	private String processingCode;
	// System Trace Audit #
	private String systemTraceAuditNo;
	// TXN Destination Institute Id
	private String txnDestinationInstituteId;
	// TXN Source Institute Id
	private String txnSourceInstituteId;
	// Txn Initiate Date And Time
	private String txnInitiateDateAndTime;
	// Response Code
	private String responseCode;
	// Sync Check Item
	private String syncCheckItem;
	// BitMap Configuration
	private String bitMapConfiguration;
	private String APData;
	private String messageId;
	private PCodeSubSystem PCodeType;
	private MessageFlow messageKind;
	private FunctionType functionType;
	private int totalLength;
	private boolean checkBitmap;
	private String FiscEncoding;

	public FISCHeader() {}

	public FISCHeader(String fiscFlatfile) {
		this.FISCMessage = fiscFlatfile;
		this.totalLength = (int) fiscFlatfile.length() / 2;
	}

	public abstract String getGetPropertyValue(int index);

	public abstract void setGetPropertyValue(int index, String value);

	public int getTotalLength() {
		return this.totalLength;
	}

	public String getFISCMessage() {
		return FISCMessage;
	}

	public void setFISCMessage(String fISCMessage) {
		FISCMessage = fISCMessage;
	}

	public String getSystemSupervisoryControlHeader() {
		return systemSupervisoryControlHeader;
	}

	public void setSystemSupervisoryControlHeader(String systemSupervisoryControlHeader) {
		this.systemSupervisoryControlHeader = systemSupervisoryControlHeader;
	}

	public String getSystemNetworkIdentifier() {
		return systemNetworkIdentifier;
	}

	public void setSystemNetworkIdentifier(String systemNetworkIdentifier) {
		this.systemNetworkIdentifier = systemNetworkIdentifier;
	}

	public String getAdderssControlField() {
		return adderssControlField;
	}

	public void setAdderssControlField(String adderssControlField) {
		this.adderssControlField = adderssControlField;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getProcessingCode() {
		return processingCode;
	}

	public void setProcessingCode(String processingCode) {
		this.processingCode = processingCode;
	}

	public String getSystemTraceAuditNo() {
		return systemTraceAuditNo;
	}

	public void setSystemTraceAuditNo(String systemTraceAuditNo) {
		this.systemTraceAuditNo = systemTraceAuditNo;
	}

	public String getTxnDestinationInstituteId() {
		return txnDestinationInstituteId;
	}

	public void setTxnDestinationInstituteId(String txnDestinationInstituteId) {
		this.txnDestinationInstituteId = txnDestinationInstituteId;
	}

	public String getTxnSourceInstituteId() {
		return txnSourceInstituteId;
	}

	public void setTxnSourceInstituteId(String txnSourceInstituteId) {
		this.txnSourceInstituteId = txnSourceInstituteId;
	}

	public String getTxnInitiateDateAndTime() {
		return txnInitiateDateAndTime;
	}

	public void setTxnInitiateDateAndTime(String txnInitiateDateAndTime) {
		this.txnInitiateDateAndTime = txnInitiateDateAndTime;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getSyncCheckItem() {
		return syncCheckItem;
	}

	public void setSyncCheckItem(String syncCheckItem) {
		this.syncCheckItem = syncCheckItem;
	}

	public String getBitMapConfiguration() {
		return bitMapConfiguration;
	}

	public void setBitMapConfiguration(String bitMapConfiguration) {
		this.bitMapConfiguration = bitMapConfiguration;
	}

	public String getAPData() {
		return APData;
	}

	public void setAPData(String APData) {
		this.APData = APData;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public PCodeSubSystem getPCodeType() {
		return PCodeType;
	}

	public void setPCodeType(PCodeSubSystem pCodeType) {
		PCodeType = pCodeType;
	}

	public MessageFlow getMessageKind() {
		return messageKind;
	}

	public void setMessageKind(MessageFlow messageKind) {
		this.messageKind = messageKind;
	}

	public FunctionType getFunctionType() {
		return functionType;
	}

	public void setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
	}

	/**
	 * 拆解財金電文到Class各屬性中
	 * 
	 * @return
	 * 
	 */
	public FEPReturnCode parseFISCMsg() {
		// 先拆Header
		// 從MessageType ~Response code先轉ASCII
		try {
			String header="";
			if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
				header = EbcdicConverter.fromHex(CCSID.English,this.FISCMessage.substring(6, 96));
			}else{
				header = StringUtil.fromHex(this.FISCMessage.substring(6, 96));
			}
			this.systemSupervisoryControlHeader = this.FISCMessage.substring(0, 2);
			this.systemNetworkIdentifier = this.FISCMessage.substring(2, 4);
			this.adderssControlField = this.FISCMessage.substring(4, 6);
			this.messageType = header.substring(0, 4);
			// 根據MessageType後2碼判斷是Request or Response or confirm
			switch (getMessageType().substring(2)) {
				case "00":
				case "81":
				case "99":
					setMessageKind(MessageFlow.Request);
					break;
				case "01":
					setMessageKind(MessageFlow.RequestRepeat);
					break;
				case "02":
					setMessageKind(MessageFlow.Confirmation);
					break;
				case "03":
					setMessageKind(MessageFlow.ConfirmationRepeat);
					break;
				case "10":
					setMessageKind(MessageFlow.Response);
					break;
				case "12":
					setMessageKind(MessageFlow.ResponseConfirmation);
					break;
				default:
					break;
			}
			this.processingCode = header.substring(4, 8);
			this.systemTraceAuditNo = header.substring(8, 15);
			this.txnDestinationInstituteId = header.substring(15, 22);
			this.txnSourceInstituteId = header.substring(22, 29);
			this.txnInitiateDateAndTime = header.substring(29, 41);
			this.responseCode = header.substring(41, 45);
			this.syncCheckItem = this.FISCMessage.substring(96, 104); // 抓Hex
			this.bitMapConfiguration = this.FISCMessage.substring(104, 120); // 抓Hex
			this.APData = this.FISCMessage.substring(120);
			this.debugMessage("Finish Parse Message:", this.FISCMessage);
			return FEPReturnCode.Normal;
		} catch (Exception ex) {
			return FEPReturnCode.MessageFormatError;
		}
	}

	/**
	 * 將財金電文定義組成財金電文
	 * 
	 * @return
	 */
	public FEPReturnCode makeFISCMsg() {
		String hex = "";
		if(FEPConfig.getInstance().getFiscencoding() == FISCEncoding.ebcdic){
			hex = EbcdicConverter.toHex(CCSID.English,
					StringUtils.join(
							this.messageType,
							this.processingCode,
							this.systemTraceAuditNo,
							this.txnDestinationInstituteId,
							this.txnSourceInstituteId,
							this.txnInitiateDateAndTime,
							this.responseCode).length(),
					StringUtils.join(
							this.messageType,
							this.processingCode,
							this.systemTraceAuditNo,
							this.txnDestinationInstituteId,
							this.txnSourceInstituteId,
							this.txnInitiateDateAndTime,
							this.responseCode));
		}else{
			hex = StringUtil.toHex(
					StringUtils.join(
							this.messageType,
							this.processingCode,
							this.systemTraceAuditNo,
							this.txnDestinationInstituteId,
							this.txnSourceInstituteId,
							this.txnInitiateDateAndTime,
							this.responseCode));
		}
		this.FISCMessage = StringUtils.join(
				this.systemSupervisoryControlHeader,
				this.systemNetworkIdentifier,
				this.adderssControlField,
				hex,
				this.syncCheckItem,
				this.bitMapConfiguration,
				this.APData);
		LogHelperFactory.getTraceLogger().debug("[FISCHeader.makeFISCMsg][", this.getClass().getSimpleName(), "]FISC Data : ", this.toJSON());
		return FEPReturnCode.Normal;
	}

	protected void setMessageId() {
		switch (this.processingCode) {
			case "2110": // 查詢(磁條卡)
				this.messageId = "CDI005";
				break;
			case "2500": // 查詢(晶片卡)
				this.messageId = "ICIQ04";
				break;
			case "2100": // 提款(磁條卡)
				this.messageId = "CDW008";
				break;
			case "2510": // 提款(晶片卡)
				this.messageId = "ICWT08";
				break;
			case "2210": // 轉入(磁條卡)
				this.messageId = "CDTD06";
				break;
			case "2521": // 轉入(晶片卡)
				this.messageId = "ICTF06";
				break;
			case "2220": // 轉出(磁條卡)
				this.messageId = "CDTW06";
				break;
			case "2522": // 轉出(晶片卡)
				this.messageId = "ICTF16";
				break;
			case "2230": // 自行轉帳(磁條卡)
				this.messageId = "CDTA06";
				break;
			case "2523": // 自行轉帳(晶片卡)
				this.messageId = "ICTF26";
				break;
			case "2240": // 跨行轉帳(磁條卡)
				break;
			case "2524": // 跨行轉帳(晶片卡)
				if (this.messageKind == MessageFlow.Request) {
					if (this.FISCMessage.length() >= 212 * 2 && this.FISCMessage.length() <= 332 * 2) {
						this.messageId = "ICTF36"; // REQUEST
					} else {
						this.messageId = "ICTF41"; // 跨行轉帳REQUEST
					}
				}
				if (this.messageKind == MessageFlow.Response) {
					if ("4001".equals(this.responseCode)) {
						this.messageId = "ICTF38"; // NEGATIVE RESPONSE
					} else if (this.FISCMessage.length() == 134 * 2) {
						this.messageId = "ICTF37"; // POSITIVE RESPONSE
					}
				}
				if (this.messageKind == MessageFlow.Confirmation) {
					if ("4001".equals(this.responseCode)) {
						this.messageId = "ICTF40"; // POSITIVE RESPONSE
					} else if (this.FISCMessage.length() == 134 * 2) {
						this.messageId = "ICTF39"; // NEGATIVE RESPONSE
					}
				}
				break;
			case "2525": // 付費轉帳(晶片卡)
				this.messageId = "ICTF51";
				break;
			case "2250": // 跨行繳款(磁條卡)
				this.messageId = "CDTY06";
				break;
			case "2531": // 跨行繳款(晶片卡)
				this.messageId = "ICTP06";
				break;
			default:
				break;
		}
	}

	public String toJSON() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public boolean isCheckBitmap() {
		return checkBitmap;
	}

	public void setCheckBitmap(boolean checkBitmap) {
		this.checkBitmap = checkBitmap;
	}

	public String getFiscEncoding() {return FiscEncoding;}

	public void setFiscEncoding(String fiscEncoding) {FiscEncoding = fiscEncoding;}
}