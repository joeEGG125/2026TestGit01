package com.syscom.fep.vo.text.rm;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;

public class RMGeneralResponse {
	private String _ChlEJNo = StringUtils.EMPTY;
	private String _EJNo = StringUtils.EMPTY;
	private String _RqTime = StringUtils.EMPTY;
	private String _RsTime = StringUtils.EMPTY;
	private FEPRsHeaderOverride[] _Overrides;
	private String _RsStat_RsStateCode = StringUtils.EMPTY;
	private String _RsStat_RsStateCode_type = StringUtils.EMPTY;
	private String _RsStat_Desc = StringUtils.EMPTY;
	private String _T24Application = StringUtils.EMPTY;
	private String _T24Version = StringUtils.EMPTY;
	private String _T24Operation = StringUtils.EMPTY;
	private String _T24RspCode = StringUtils.EMPTY;
	private String _FEPNO = StringUtils.EMPTY;
	private String _REMBANK = StringUtils.EMPTY;
	private String _REMBKNM = StringUtils.EMPTY;
	private String _REMTYPE = StringUtils.EMPTY;
	private BigDecimal _REMAMT = new BigDecimal(0);
	private BigDecimal _RECFEE = new BigDecimal(0);
	private BigDecimal _ACTFEE = new BigDecimal(0);
	private String _REMDATE = StringUtils.EMPTY;
	private String _RECBANK = StringUtils.EMPTY;
	private String _RECBKNM = StringUtils.EMPTY;
	private String _RECCIF = StringUtils.EMPTY;
	private String _RECNM = StringUtils.EMPTY;
	private String _REMCIF = StringUtils.EMPTY;
	private String _REMNM = StringUtils.EMPTY;
	private String _REMTEL = StringUtils.EMPTY;
	private String _REMARKFISC = StringUtils.EMPTY;
	private String _TAXNO = StringUtils.EMPTY;
	private String _REMTIME = StringUtils.EMPTY;
	private String _OUTSTAT = StringUtils.EMPTY;
	private String _FEPNO1 = StringUtils.EMPTY;
	private String _RECTIME = StringUtils.EMPTY;
	private String _INSTAT = StringUtils.EMPTY;
	private String _REMCIFNO = StringUtils.EMPTY;
	private String _RECCIFNO = StringUtils.EMPTY;
	private String _REMARK = StringUtils.EMPTY;
	private String _REMTXTP = StringUtils.EMPTY;
	private String _HCTXTP = StringUtils.EMPTY;
	private String _ERRCD = StringUtils.EMPTY;
	private String _ORGMDATE = StringUtils.EMPTY;
	private String _ORGFEPNO = StringUtils.EMPTY;
	private String _ORGTYPE = StringUtils.EMPTY;
	private String _FISCRMNO = StringUtils.EMPTY;
	private String _FISCRC = StringUtils.EMPTY;
	private String _REMSUPNO = StringUtils.EMPTY;
	private String _ENTTLRNO = StringUtils.EMPTY;
	private String _CURRENCY = StringUtils.EMPTY;
	private String _NATIONAL_CODE = StringUtils.EMPTY;
	private String _KIND = StringUtils.EMPTY;
	private String _SUBKIND = StringUtils.EMPTY;
	private String _ID_STATUS = StringUtils.EMPTY;
	private String _ADDRESS = StringUtils.EMPTY;
	private String _BANKNO = StringUtils.EMPTY;
	private String _ALIASNAME = StringUtils.EMPTY;
	private String _ADDR = StringUtils.EMPTY;
	private String _OUT_ACCID_NO = StringUtils.EMPTY;
	private String _DTLCNT = StringUtils.EMPTY;
	private String _SENDTIME = StringUtils.EMPTY;
	private String _STAT = StringUtils.EMPTY;
	private String _RECBRNO = StringUtils.EMPTY;
	private String _ACCBRNO = StringUtils.EMPTY;
	private String _ENTSEQ = StringUtils.EMPTY;
	private String _CHNMSG = StringUtils.EMPTY;
	private String _ENGMSG = StringUtils.EMPTY;
	private String _ORGRMSNO = StringUtils.EMPTY;
	private String _PRTCNT = StringUtils.EMPTY;
	private String _RECDATE = StringUtils.EMPTY;
	private String _REMIT_NO = StringUtils.EMPTY;
	private String _MEMO = StringUtils.EMPTY;
	private String _ACTNO = StringUtils.EMPTY;
	private String _ENTSNO = StringUtils.EMPTY;
	private String _FISCSNO = StringUtils.EMPTY;
	private String _STAN = StringUtils.EMPTY;
	private String _CBSNO = StringUtils.EMPTY;
	private String _REGTLRNO = StringUtils.EMPTY;
	private String _SUPNO = StringUtils.EMPTY;
	private String _PRE_TLRNO = StringUtils.EMPTY;
	private String _ORGREMDATE = StringUtils.EMPTY;
	private String _ORGREMTYPE = StringUtils.EMPTY;
	private String _ORGSENDTIME = StringUtils.EMPTY;
	private String _ORGREMTXTP = StringUtils.EMPTY;
	private String _ORGREMAMT = StringUtils.EMPTY;
	private String _ORGHCTXTP = StringUtils.EMPTY;
	private String _ORGRECFEE = StringUtils.EMPTY;
	private String _ORGACTFEE = StringUtils.EMPTY;
	private String _ORGORIGINAL = StringUtils.EMPTY;
	private String _IBSNO = StringUtils.EMPTY;
	private String _SENDBANK = StringUtils.EMPTY;
	private String _RCVBANK = StringUtils.EMPTY;
	private String _MSG1 = StringUtils.EMPTY;
	private String _MSG2 = StringUtils.EMPTY;
	private String _RECCOUNT = StringUtils.EMPTY;
	private BigDecimal _TOTAMT = new BigDecimal(0);
	private String _SENDBKNM = StringUtils.EMPTY;
	private String _RCVBKNNM = StringUtils.EMPTY;
	private String _REGDATE = StringUtils.EMPTY;
	private String _REGTIME = StringUtils.EMPTY;
	private String _RMSTAT = StringUtils.EMPTY;
	private String _RECCIFNO2 = StringUtils.EMPTY;
	private String _ORGBRSNO = StringUtils.EMPTY;
	private String _ORIGINAL = StringUtils.EMPTY;
	private String _REMSUPNO2 = StringUtils.EMPTY;
	private String _ORGDATE = StringUtils.EMPTY;
	private String _ORGTIME = StringUtils.EMPTY;
	private String _ACT_INACTNO = StringUtils.EMPTY;
	private String _CIF = StringUtils.EMPTY;
	private String _BATCHNO = StringUtils.EMPTY;
	private String _FEDIMK = StringUtils.EMPTY;
	private String _ORGKINBR = StringUtils.EMPTY;
	private String _MACTNO = StringUtils.EMPTY;
	private String _CIFNAME = StringUtils.EMPTY;
	private String _IN_TEXT = StringUtils.EMPTY;
	private String _CIFKEY = StringUtils.EMPTY;
	private String _TLRNO = StringUtils.EMPTY;
	private String _BRNO = StringUtils.EMPTY;
	private String _CBS_NO = StringUtils.EMPTY;
	private String _REMAGENTID = StringUtils.EMPTY;
	private String _REMAGENTNAME = StringUtils.EMPTY;
	private String _REMITTERID = StringUtils.EMPTY;
	private String _MACNO = StringUtils.EMPTY;
	private String _ENOTE_SEQNO = StringUtils.EMPTY;
	private String _AMLSTAT = StringUtils.EMPTY;
	private String _AMLBypass = StringUtils.EMPTY;
	private String _RMIN_STAT = StringUtils.EMPTY;
	private String _USE_BAL = StringUtils.EMPTY;

	public String getChlEJNo() {
		return _ChlEJNo;
	}

	public void setChlEJNo(String value) {
		_ChlEJNo = value;
	}

	public String getEJNo() {
		return _EJNo;
	}

	public void setEJNo(String value) {
		_EJNo = value;
	}

	public String getRqTime() {
		return _RqTime;
	}

	public void setRqTime(String value) {
		_RqTime = value;
	}

	public String getRsTime() {
		return _RsTime;
	}

	public void setRsTime(String value) {
		_RsTime = value;
	}

	// VB TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
	// ORIGINAL LINE: <System.Xml.Serialization.XmlArrayItemAttribute("Override", IsNullable:=True)> Public Property Overrides() As FEPRsHeaderOverride()
	public FEPRsHeaderOverride[] getOverrides() {
		return _Overrides;
	}

	public void setOverrides(FEPRsHeaderOverride[] value) {
		_Overrides = value;
	}

	public String getRsStatRsStateCode() {
		return _RsStat_RsStateCode;
	}

	public void setRsStatRsStateCode(String value) {
		_RsStat_RsStateCode = value;
	}

	public String getRsStatRsStateCodeType() {
		return _RsStat_RsStateCode_type;
	}

	public void setRsStatRsStateCodeType(String value) {
		_RsStat_RsStateCode_type = value;
	}

	public String getRsStatDesc() {
		return _RsStat_Desc;
	}

	public void setRsStatDesc(String value) {
		_RsStat_Desc = value;
	}

	public String getT24Application() {
		return _T24Application;
	}

	public void setT24Application(String value) {
		_T24Application = value;
	}

	public String getT24Version() {
		return _T24Version;
	}

	public void setT24Version(String value) {
		_T24Version = value;
	}

	public String getT24Operation() {
		return _T24Operation;
	}

	public void setT24Operation(String value) {
		_T24Operation = value;
	}

	public String getT24RspCode() {
		return _T24RspCode;
	}

	public void setT24RspCode(String value) {
		_T24RspCode = value;
	}

	/**
	 * FEP登錄序號
	 */
	public String getFEPNO() {
		return _FEPNO;
	}

	public void setFEPNO(String value) {
		_FEPNO = value;
	}

	/**
	 * 匯款行
	 */
	public String getREMBANK() {
		return _REMBANK;
	}

	public void setREMBANK(String value) {
		_REMBANK = value;
	}

	/**
	 * 匯款行名稱
	 */
	public String getREMBKNM() {
		return _REMBKNM;
	}

	public void setREMBKNM(String value) {
		_REMBKNM = value;
	}

	/**
	 * 摘要
	 * 
	 * <remark>11：跨行入戶; 12：公庫匯款; 13：同業匯款; 18：證券匯款; 19：票券匯款</remark>
	 */
	public String getREMTYPE() {
		return _REMTYPE;
	}

	public void setREMTYPE(String value) {
		_REMTYPE = value;
	}

	/**
	 * 匯款金額9(11)V99
	 */
	public BigDecimal getREMAMT() {
		return _REMAMT;
	}

	public void setREMAMT(BigDecimal value) {
		_REMAMT = value;
	}

	/**
	 * 應收手續費
	 */
	public BigDecimal getRECFEE() {
		return _RECFEE;
	}

	public void setRECFEE(BigDecimal value) {
		_RECFEE = value;
	}

	/**
	 * 實收手續費
	 */
	public BigDecimal getACTFEE() {
		return _ACTFEE;
	}

	public void setACTFEE(BigDecimal value) {
		_ACTFEE = value;
	}

	/**
	 * 匯款日期
	 */
	public String getREMDATE() {
		return _REMDATE;
	}

	public void setREMDATE(String value) {
		_REMDATE = value;
	}

	/**
	 * 解款行
	 */
	public String getRECBANK() {
		return _RECBANK;
	}

	public void setRECBANK(String value) {
		_RECBANK = value;
	}

	/**
	 * 解款行名稱
	 */
	public String getRECBKNM() {
		return _RECBKNM;
	}

	public void setRECBKNM(String value) {
		_RECBKNM = value;
	}

	/**
	 * 收款人帳號
	 */
	public String getRECCIF() {
		return _RECCIF;
	}

	public void setRECCIF(String value) {
		_RECCIF = value;
	}

	/**
	 * 收款人姓名
	 */
	public String getRECNM() {
		return _RECNM;
	}

	public void setRECNM(String value) {
		_RECNM = value;
	}

	/**
	 * 匯款人帳號
	 */
	public String getREMCIF() {
		return _REMCIF;
	}

	public void setREMCIF(String value) {
		_REMCIF = value;
	}

	/**
	 * 匯款人姓名
	 */
	public String getREMNM() {
		return _REMNM;
	}

	public void setREMNM(String value) {
		_REMNM = value;
	}

	/**
	 * 匯款人電話
	 */
	public String getREMTEL() {
		return _REMTEL;
	}

	public void setREMTEL(String value) {
		_REMTEL = value;
	}

	/**
	 * 附言
	 */
	public String getREMARKFISC() {
		return _REMARKFISC;
	}

	public void setREMARKFISC(String value) {
		_REMARKFISC = value;
	}

	/**
	 * 稅單號碼
	 */
	public String getTAXNO() {
		return _TAXNO;
	}

	public void setTAXNO(String value) {
		_TAXNO = value;
	}

	/**
	 * 匯出時間
	 */
	public String getREMTIME() {
		return _REMTIME;
	}

	public void setREMTIME(String value) {
		_REMTIME = value;
	}

	/**
	 * 匯出狀態
	 * 
	 * <remark>04.已放行05.傳送中06.已解款 07.財金拒絕08.退匯</remark>
	 */
	public String getOUTSTAT() {
		return _OUTSTAT;
	}

	public void setOUTSTAT(String value) {
		_OUTSTAT = value;
	}

	/**
	 * 匯入序號
	 */
	public String getFEPNO1() {
		return _FEPNO1;
	}

	public void setFEPNO1(String value) {
		_FEPNO1 = value;
	}

	/**
	 * 匯入時間
	 */
	public String getRECTIME() {
		return _RECTIME;
	}

	public void setRECTIME(String value) {
		_RECTIME = value;
	}

	/**
	 * 匯入狀態
	 * 
	 * <remark>10.匯出被退11.自動退匯16.待解款 18.已解款 21.自動入帳24.滯留交易已入帳25 滯留交易尚未入帳</remark>
	 */
	public String getINSTAT() {
		return _INSTAT;
	}

	public void setINSTAT(String value) {
		_INSTAT = value;
	}

	/**
	 * 匯款人代號
	 */
	public String getREMCIFNO() {
		return _REMCIFNO;
	}

	public void setREMCIFNO(String value) {
		_REMCIFNO = value;
	}

	/**
	 * 收款人帳號
	 */
	public String getRECCIFNO() {
		return _RECCIFNO;
	}

	public void setRECCIFNO(String value) {
		_RECCIFNO = value;
	}

	/**
	 * 附言
	 */
	public String getREMARK() {
		return _REMARK;
	}

	public void setREMARK(String value) {
		_REMARK = value;
	}

	/**
	 * 匯款帳務別
	 */
	public String getREMTXTP() {
		return _REMTXTP;
	}

	public void setREMTXTP(String value) {
		_REMTXTP = value;
	}

	/**
	 * 手續費帳務別
	 */
	public String getHCTXTP() {
		return _HCTXTP;
	}

	public void setHCTXTP(String value) {
		_HCTXTP = value;
	}

	/**
	 * 退匯理由
	 */
	public String getERRCD() {
		return _ERRCD;
	}

	public void setERRCD(String value) {
		_ERRCD = value;
	}

	/**
	 * 原匯入日
	 */
	public String getORGMDATE() {
		return _ORGMDATE;
	}

	public void setORGMDATE(String value) {
		_ORGMDATE = value;
	}

	/**
	 * 原匯入序號
	 */
	public String getORGFEPNO() {
		return _ORGFEPNO;
	}

	public void setORGFEPNO(String value) {
		_ORGFEPNO = value;
	}

	/**
	 * 原匯款種類
	 */
	public String getORGTYPE() {
		return _ORGTYPE;
	}

	public void setORGTYPE(String value) {
		_ORGTYPE = value;
	}

	/**
	 * 跨行通匯序號
	 */
	public String getFISCRMNO() {
		return _FISCRMNO;
	}

	public void setFISCRMNO(String value) {
		_FISCRMNO = value;
	}

	/**
	 * 財金回應代碼
	 */
	public String getFISCRC() {
		return _FISCRC;
	}

	public void setFISCRC(String value) {
		_FISCRC = value;
	}

	/**
	 * 匯出主管
	 */
	public String getREMSUPNO() {
		return _REMSUPNO;
	}

	public void setREMSUPNO(String value) {
		_REMSUPNO = value;
	}

	/**
	 * 登錄櫃員
	 */
	public String getENTTLRNO() {
		return _ENTTLRNO;
	}

	public void setENTTLRNO(String value) {
		_ENTTLRNO = value;
	}

	/**
	 * 幣別
	 * 
	 * <remark>001-美元</remark>
	 */
	public String getCURRENCY() {
		return _CURRENCY;
	}

	public void setCURRENCY(String value) {
		_CURRENCY = value;
	}

	/**
	 * 匯款人國別
	 * 
	 * <remark>TW:本國台灣XA:OBU-Offshore Banking Unit(境外金融中心)</remark>
	 */
	public String getNationalCode() {
		return _NATIONAL_CODE;
	}

	public void setNationalCode(String value) {
		_NATIONAL_CODE = value;
	}

	/**
	 * 匯出行匯款分類-用途
	 */
	public String getKIND() {
		return _KIND;
	}

	public void setKIND(String value) {
		_KIND = value;
	}

	/**
	 * 匯出行匯款分類-細分類
	 */
	public String getSUBKIND() {
		return _SUBKIND;
	}

	public void setSUBKIND(String value) {
		_SUBKIND = value;
	}

	/**
	 * 匯款人身分別
	 * 
	 * <remark>1 = 政府、2 = 公營事業 3 = 民間</remark>
	 */
	public String getIdStatus() {
		return _ID_STATUS;
	}

	public void setIdStatus(String value) {
		_ID_STATUS = value;
	}

	/**
	 * 匯出入主檔之匯款人地址
	 */
	public String getADDRESS() {
		return _ADDRESS;
	}

	public void setADDRESS(String value) {
		_ADDRESS = value;
	}

	/**
	 * 代號
	 */
	public String getBANKNO() {
		return _BANKNO;
	}

	public void setBANKNO(String value) {
		_BANKNO = value;
	}

	/**
	 * 銀行之簡稱
	 */
	public String getALIASNAME() {
		return _ALIASNAME;
	}

	public void setALIASNAME(String value) {
		_ALIASNAME = value;
	}

	/**
	 * 匯款人地址
	 */
	public String getADDR() {
		return _ADDR;
	}

	public void setADDR(String value) {
		_ADDR = value;
	}

	/**
	 * 匯出帳號或匯款人身份證統編
	 */
	public String getOutAccidNo() {
		return _OUT_ACCID_NO;
	}

	public void setOutAccidNo(String value) {
		_OUT_ACCID_NO = value;
	}

	/**
	 * 明細筆數
	 */
	public String getDTLCNT() {
		return _DTLCNT;
	}

	public void setDTLCNT(String value) {
		_DTLCNT = value;
	}

	/**
	 * 匯出時間
	 */
	public String getSENDTIME() {
		return _SENDTIME;
	}

	public void setSENDTIME(String value) {
		_SENDTIME = value;
	}

	/**
	 * 匯款狀態
	 */
	public String getSTAT() {
		return _STAT;
	}

	public void setSTAT(String value) {
		_STAT = value;
	}

	/**
	 * 放行分行
	 */
	public String getRECBRNO() {
		return _RECBRNO;
	}

	public void setRECBRNO(String value) {
		_RECBRNO = value;
	}

	/**
	 * 帳務分行
	 */
	public String getACCBRNO() {
		return _ACCBRNO;
	}

	public void setACCBRNO(String value) {
		_ACCBRNO = value;
	}

	/**
	 * 登錄序號
	 */
	public String getENTSEQ() {
		return _ENTSEQ;
	}

	public void setENTSEQ(String value) {
		_ENTSEQ = value;
	}

	/**
	 * 中文收訊內容
	 */
	public String getCHNMSG() {
		return _CHNMSG;
	}

	public void setCHNMSG(String value) {
		_CHNMSG = value;
	}

	/**
	 * 英文收訊內容
	 */
	public String getENGMSG() {
		return _ENGMSG;
	}

	public void setENGMSG(String value) {
		_ENGMSG = value;
	}

	/**
	 * 原通匯序號
	 */
	public String getORGRMSNO() {
		return _ORGRMSNO;
	}

	public void setORGRMSNO(String value) {
		_ORGRMSNO = value;
	}

	/**
	 * 列印次數
	 */
	public String getPRTCNT() {
		return _PRTCNT;
	}

	public void setPRTCNT(String value) {
		_PRTCNT = value;
	}

	/**
	 * 收訊日期
	 */
	public String getRECDATE() {
		return _RECDATE;
	}

	public void setRECDATE(String value) {
		_RECDATE = value;
	}

	/**
	 * 匯款編號
	 */
	public String getRemitNo() {
		return _REMIT_NO;
	}

	public void setRemitNo(String value) {
		_REMIT_NO = value;
	}

	/**
	 * 附言
	 */
	public String getMEMO() {
		return _MEMO;
	}

	public void setMEMO(String value) {
		_MEMO = value;
	}

	/**
	 * 正確匯入帳號
	 */
	public String getACTNO() {
		return _ACTNO;
	}

	public void setACTNO(String value) {
		_ACTNO = value;
	}

	/**
	 * 匯入序號
	 */
	public String getENTSNO() {
		return _ENTSNO;
	}

	public void setENTSNO(String value) {
		_ENTSNO = value;
	}

	/**
	 * 跨行電文序號
	 */
	public String getFISCSNO() {
		return _FISCSNO;
	}

	public void setFISCSNO(String value) {
		_FISCSNO = value;
	}

	/**
	 * 交易序號
	 */
	public String getSTAN() {
		return _STAN;
	}

	public void setSTAN(String value) {
		_STAN = value;
	}

	/**
	 * T24中心序號
	 */
	public String getCBSNO() {
		return _CBSNO;
	}

	public void setCBSNO(String value) {
		_CBSNO = value;
	}

	/**
	 * 櫃員
	 */
	public String getREGTLRNO() {
		return _REGTLRNO;
	}

	public void setREGTLRNO(String value) {
		_REGTLRNO = value;
	}

	/**
	 * 主管
	 */
	public String getSUPNO() {
		return _SUPNO;
	}

	public void setSUPNO(String value) {
		_SUPNO = value;
	}

	/**
	 * 前置處理櫃員
	 */
	public String getPreTlrno() {
		return _PRE_TLRNO;
	}

	public void setPreTlrno(String value) {
		_PRE_TLRNO = value;
	}

	/**
	 * 原匯出日
	 */
	public String getORGREMDATE() {
		return _ORGREMDATE;
	}

	public void setORGREMDATE(String value) {
		_ORGREMDATE = value;
	}

	/**
	 * 原匯款種類
	 */
	public String getORGREMTYPE() {
		return _ORGREMTYPE;
	}

	public void setORGREMTYPE(String value) {
		_ORGREMTYPE = value;
	}

	/**
	 * 原匯出時間
	 */
	public String getORGSENDTIME() {
		return _ORGSENDTIME;
	}

	public void setORGSENDTIME(String value) {
		_ORGSENDTIME = value;
	}

	/**
	 * 原匯款帳務別
	 */
	public String getORGREMTXTP() {
		return _ORGREMTXTP;
	}

	public void setORGREMTXTP(String value) {
		_ORGREMTXTP = value;
	}

	/**
	 * 原匯款金額
	 */
	public String getORGREMAMT() {
		return _ORGREMAMT;
	}

	public void setORGREMAMT(String value) {
		_ORGREMAMT = value;
	}

	/**
	 * 原手續費帳務別
	 */
	public String getORGHCTXTP() {
		return _ORGHCTXTP;
	}

	public void setORGHCTXTP(String value) {
		_ORGHCTXTP = value;
	}

	/**
	 * 原應收手續費
	 */
	public String getORGRECFEE() {
		return _ORGRECFEE;
	}

	public void setORGRECFEE(String value) {
		_ORGRECFEE = value;
	}

	/**
	 * 原實收手續費
	 */
	public String getORGACTFEE() {
		return _ORGACTFEE;
	}

	public void setORGACTFEE(String value) {
		_ORGACTFEE = value;
	}

	/**
	 * 原交易來源
	 */
	public String getORGORIGINAL() {
		return _ORGORIGINAL;
	}

	public void setORGORIGINAL(String value) {
		_ORGORIGINAL = value;
	}

	/**
	 * 原IBS交易序號
	 */
	public String getIBSNO() {
		return _IBSNO;
	}

	public void setIBSNO(String value) {
		_IBSNO = value;
	}

	/**
	 * 發訊行
	 */
	public String getSENDBANK() {
		return _SENDBANK;
	}

	public void setSENDBANK(String value) {
		_SENDBANK = value;
	}

	/**
	 * 收訊行
	 */
	public String getRCVBANK() {
		return _RCVBANK;
	}

	public void setRCVBANK(String value) {
		_RCVBANK = value;
	}

	/**
	 * 中文訊息
	 */
	public String getMSG1() {
		return _MSG1;
	}

	public void setMSG1(String value) {
		_MSG1 = value;
	}

	/**
	 * 英文訊息
	 */
	public String getMSG2() {
		return _MSG2;
	}

	public void setMSG2(String value) {
		_MSG2 = value;
	}

	/**
	 * 明細筆數
	 */
	public String getRECCOUNT() {
		return _RECCOUNT;
	}

	public void setRECCOUNT(String value) {
		_RECCOUNT = value;
	}

	/**
	 * 總金額
	 * 
	 * <remark>若Request有輸入解款人帳號時此欄位才需要加總明細之匯款金額否則放0</remark>
	 */
	public BigDecimal getTOTAMT() {
		return _TOTAMT;
	}

	public void setTOTAMT(BigDecimal value) {
		_TOTAMT = value;
	}

	/**
	 * 發訊行名稱
	 */
	public String getSENDBKNM() {
		return _SENDBKNM;
	}

	public void setSENDBKNM(String value) {
		_SENDBKNM = value;
	}

	/**
	 * 收訊行名稱
	 */
	public String getRCVBKNNM() {
		return _RCVBKNNM;
	}

	public void setRCVBKNNM(String value) {
		_RCVBKNNM = value;
	}

	/**
	 * 登錄日期
	 */
	public String getREGDATE() {
		return _REGDATE;
	}

	public void setREGDATE(String value) {
		_REGDATE = value;
	}

	/**
	 * 登錄時間
	 * 
	 * <remark>最後修改時間</remark>
	 */
	public String getREGTIME() {
		return _REGTIME;
	}

	public void setREGTIME(String value) {
		_REGTIME = value;
	}

	/**
	 * 匯出/入狀態
	 * 
	 * <remark>匯出時 : 02.待補登03.待放行04.已放行05.傳送中06.已解款07.財金拒絕08.退匯09.已刪除未更帳 10.已刪除已更帳11.磁片整批匯出失敗12.緊急匯款已刪除99-系統問題 匯入時: 01.自動入戶02.待解款03.已解款04.自動退匯(基碼/通匯序號不符)05.匯入退匯─他行匯入款，經本行退匯。 </remark>
	 */
	public String getRMSTAT() {
		return _RMSTAT;
	}

	public void setRMSTAT(String value) {
		_RMSTAT = value;
	}

	/**
	 * 解款人帳號二
	 */
	public String getRECCIFNO2() {
		return _RECCIFNO2;
	}

	public void setRECCIFNO2(String value) {
		_RECCIFNO2 = value;
	}

	/**
	 * 週邊交易序號
	 */
	public String getORGBRSNO() {
		return _ORGBRSNO;
	}

	public void setORGBRSNO(String value) {
		_ORGBRSNO = value;
	}

	/**
	 * 交易來源
	 * 
	 * <remark>0-臨櫃, 1-FCS 2-FEID,3-FISC,4-MMAB2B</remark>
	 */
	public String getORIGINAL() {
		return _ORIGINAL;
	}

	public void setORIGINAL(String value) {
		_ORIGINAL = value;
	}

	/**
	 * 匯出主管2
	 */
	public String getREMSUPNO2() {
		return _REMSUPNO2;
	}

	public void setREMSUPNO2(String value) {
		_REMSUPNO2 = value;
	}

	/**
	 * 原匯入日
	 */
	public String getORGDATE() {
		return _ORGDATE;
	}

	public void setORGDATE(String value) {
		_ORGDATE = value;
	}

	/**
	 * 原匯入時間
	 */
	public String getORGTIME() {
		return _ORGTIME;
	}

	public void setORGTIME(String value) {
		_ORGTIME = value;
	}

	/**
	 * 實際入帳帳號
	 */
	public String getActInactno() {
		return _ACT_INACTNO;
	}

	public void setActInactno(String value) {
		_ACT_INACTNO = value;
	}

	/**
	 * 優惠客戶ID
	 */
	public String getCIF() {
		return _CIF;
	}

	public void setCIF(String value) {
		_CIF = value;
	}

	/**
	 * 批號
	 */
	public String getBATCHNO() {
		return _BATCHNO;
	}

	public void setBATCHNO(String value) {
		_BATCHNO = value;
	}

	/**
	 * FEDI轉通匯記號
	 * 
	 * <remark>0=非FEDI轉通匯,1=FEDI轉通匯</remark>
	 */
	public String getFEDIMK() {
		return _FEDIMK;
	}

	public void setFEDIMK(String value) {
		_FEDIMK = value;
	}

	/**
	 * 登錄分行
	 */
	public String getORGKINBR() {
		return _ORGKINBR;
	}

	public void setORGKINBR(String value) {
		_ORGKINBR = value;
	}

	/**
	 * 匯入帳號
	 * 
	 * <remark>可輸入客戶帳號或虛擬帳號 1.分行別(3)：default交易行，可修改。 2.科目別(3)：限001、003、004、031、033。 3.帳號(7)：不滿 7位數，自動補0。 4.檢查碼(1)：系統檢視檢查碼的正確性，若有誤於交易訊息欄顯示可能的檢查碼號碼。 5. 系統自動帶出帳號所屬客戶姓名。 </remark>
	 */
	public String getMACTNO() {
		return _MACTNO;
	}

	public void setMACTNO(String value) {
		_MACTNO = value;
	}

	/**
	 * 客戶戶名
	 * 
	 * <remark>匯入帳號之戶名</remark>
	 */
	public String getCIFNAME() {
		return _CIFNAME;
	}

	public void setCIFNAME(String value) {
		_CIFNAME = value;
	}

	/**
	 * 
	 */
	public String getInText() {
		return _IN_TEXT;
	}

	public void setInText(String value) {
		_IN_TEXT = value;
	}

	/**
	 * 客戶ID
	 */
	public String getCIFKEY() {
		return _CIFKEY;
	}

	public void setCIFKEY(String value) {
		_CIFKEY = value;
	}

	/**
	 * 櫃員
	 */
	public String getTLRNO() {
		return _TLRNO;
	}

	public void setTLRNO(String value) {
		_TLRNO = value;
	}

	/**
	 * 交易分行
	 * 
	 * <remark>匯出主檔之登錄行</remark>
	 */
	public String getBRNO() {
		return _BRNO;
	}

	public void setBRNO(String value) {
		_BRNO = value;
	}

	/**
	 * 帳務主機交易序號
	 */
	public String getCbsNo() {
		return _CBS_NO;
	}

	public void setCbsNo(String value) {
		_CBS_NO = value;
	}

	/**
	 * 匯款代理人ID
	 */
	public String getREMAGENTID() {
		return _REMAGENTID;
	}

	public void setREMAGENTID(String value) {
		_REMAGENTID = value;
	}

	/**
	 * 匯款代理人姓名
	 */
	public String getREMAGENTNAME() {
		return _REMAGENTNAME;
	}

	public void setREMAGENTNAME(String value) {
		_REMAGENTNAME = value;
	}

	/**
	 * 匯款人ID
	 */
	public String getREMITTERID() {
		return _REMITTERID;
	}

	public void setREMITTERID(String value) {
		_REMITTERID = value;
	}

	/**
	 * 扣款帳號
	 */
	public String getMACNO() {
		return _MACNO;
	}

	public void setMACNO(String value) {
		_MACNO = value;
	}

	/**
	 * 傳票編號
	 */
	public String getEnoteSeqno() {
		return _ENOTE_SEQNO;
	}

	public void setEnoteSeqno(String value) {
		_ENOTE_SEQNO = value;
	}

	/**
	 * AML狀態
	 */
	public String getAMLSTAT() {
		return _AMLSTAT;
	}

	public void setAMLSTAT(String value) {
		_AMLSTAT = value;
	}

	/**
	 * AMLBypass
	 */
	public String getAMLBypass() {
		return _AMLBypass;
	}

	public void setAMLBypass(String value) {
		_AMLBypass = value;
	}

	/**
	 * 匯入狀態
	 */
	public String getRminStat() {
		return _RMIN_STAT;
	}

	public void setRminStat(String value) {
		_RMIN_STAT = value;
	}

	/**
	 * 跨行業務基金可用餘額
	 */
	public String getUseBal() {
		return _USE_BAL;
	}

	public void setUseBal(String value) {
		_USE_BAL = value;
	}
}
