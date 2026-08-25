package com.syscom.fep.vo.text.rm;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class RMGeneralRequest {
	private String _MsgID = StringUtils.EMPTY;
	private String _MsgType = StringUtils.EMPTY;
	private String _ChlName = StringUtils.EMPTY;
	private String _ChlEJNo = StringUtils.EMPTY;
	private String _ChlSendTime = StringUtils.EMPTY;
	private String _TxnID = StringUtils.EMPTY;
	private String _BranchID = StringUtils.EMPTY;
	private String _TermID = StringUtils.EMPTY;
	private String _UserID = StringUtils.EMPTY;
	private String _SignID = StringUtils.EMPTY;
	private String _ORGBRNO = StringUtils.EMPTY;
	private String _FEPNO = StringUtils.EMPTY;
	private String _REMDATE = StringUtils.EMPTY;
	private BigDecimal _REMAMT = new BigDecimal(0);
	private String _REMBANK = StringUtils.EMPTY;
	private String _REMTXTP = StringUtils.EMPTY;
	private String _REMTYPE = StringUtils.EMPTY;
	private String _HCTXTP = StringUtils.EMPTY;
	private BigDecimal _RECFEE = new BigDecimal(0);
	private BigDecimal _ACTFEE = new BigDecimal(0);
	private String _CIFKEY = StringUtils.EMPTY;
	private String _RECBANK = StringUtils.EMPTY;
	private String _RECCIF = StringUtils.EMPTY;
	private String _RECNM = StringUtils.EMPTY;
	private String _REMCIF = StringUtils.EMPTY;
	private String _REMNM = StringUtils.EMPTY;
	private String _REMTEL = StringUtils.EMPTY;
	private String _REMARK = StringUtils.EMPTY;
	private String _TAXNO = StringUtils.EMPTY;
	private String _MAC = StringUtils.EMPTY;
	private String _ORGFEPNO = StringUtils.EMPTY;
	private String _ORGREMDATE = StringUtils.EMPTY;
	private BigDecimal _ORGREMAMT = new BigDecimal(0);
	private String _ERRCD = StringUtils.EMPTY;
	private String _REMARKFISC = StringUtils.EMPTY;
	private String _KINBR = StringUtils.EMPTY;
	private String _TRMSEQ = StringUtils.EMPTY;
	private String _BRSNO = StringUtils.EMPTY;
	private String _ENTTLRNO = StringUtils.EMPTY;
	private String _SUPNO1 = StringUtils.EMPTY;
	private String _SUPNO2 = StringUtils.EMPTY;
	private String _TBSDY = StringUtils.EMPTY;
	private String _TIME = StringUtils.EMPTY;
	private String _IN_TEXT = StringUtils.EMPTY;
	private String _INQDATE = StringUtils.EMPTY;
	private String _INQDATE_B = StringUtils.EMPTY;
	private String _INQDATE_E = StringUtils.EMPTY;
	private List<String> _INQBANK;
	private String _KIND = StringUtils.EMPTY;
	private BigDecimal _TXAMT = new BigDecimal(0);
	private String _OUTSTAT = StringUtils.EMPTY;
	private String _BATCHNO = StringUtils.EMPTY;
	private String _SENDFLAG = StringUtils.EMPTY;
	private List<String> _RECBANK_S;
	private String _TXAMT_KIND = StringUtils.EMPTY;
	private String _INSTAT = StringUtils.EMPTY;
	private String _TLRNO = StringUtils.EMPTY;
	private String _BANKNO = StringUtils.EMPTY;
	private String _REMTIME = StringUtils.EMPTY;
	private String _MACTNO = StringUtils.EMPTY;
	private String _CBSNO = StringUtils.EMPTY;
	private String _ACBRNO = StringUtils.EMPTY;
	private String _DEPBRNO = StringUtils.EMPTY;
	private String _RECBRNO = StringUtils.EMPTY;
	private String _SENDTIME = StringUtils.EMPTY;
	private String _RC = StringUtils.EMPTY;
	private String _ERRMSG = StringUtils.EMPTY;
	private String _SENDBANK = StringUtils.EMPTY;
	private String _RCVBANK = StringUtils.EMPTY;
	private String _MSG1 = StringUtils.EMPTY;
	private String _MSG2 = StringUtils.EMPTY;
	private BigDecimal _TOTALAMT = new BigDecimal(0);
	private String _CIFNAME = StringUtils.EMPTY;
	private String _MEMO = StringUtils.EMPTY;
	private String _RECDATE = StringUtils.EMPTY;
	private String _SENDTIME_B = StringUtils.EMPTY;
	private String _SENDTIME_E = StringUtils.EMPTY;
	private String _INQBK = StringUtils.EMPTY;
	private String _REMBK = StringUtils.EMPTY;
	private BigDecimal _TXAMT_B = new BigDecimal(0);
	private BigDecimal _TXAMT_E = new BigDecimal(0);
	private String _CURRENCY = StringUtils.EMPTY;
	private String _RMIOFLG = StringUtils.EMPTY;
	private String _INQTIME_B = StringUtils.EMPTY;
	private String _INQTIME_E = StringUtils.EMPTY;
	private String _ID = StringUtils.EMPTY;
	private String _INQBRNO_B = StringUtils.EMPTY;
	private String _INQBRNO_E = StringUtils.EMPTY;
	private String _PRTMK = StringUtils.EMPTY;
	private String _INDATE = StringUtils.EMPTY;
	private String _INBRNO = StringUtils.EMPTY;
	private String _STATUS = StringUtils.EMPTY;
	private String _ORGT24NO = StringUtils.EMPTY;
	private String _REMSEQ = StringUtils.EMPTY;
	private String _TXCODE = StringUtils.EMPTY;
	private String _ORGTXCODE = StringUtils.EMPTY;
	private String _ORGORIGINAL = StringUtils.EMPTY;
	private String _ORGBATCHNO = StringUtils.EMPTY;
	private String _ORGBRSNO = StringUtils.EMPTY;
	private String _T24NO = StringUtils.EMPTY;
	private String _ORGCHLEJNO = StringUtils.EMPTY;
	private String _FISCRC = StringUtils.EMPTY;
	private String _CHLRC = StringUtils.EMPTY;
	private String _CHLMSG = StringUtils.EMPTY;
	private String _ORIGINAL = StringUtils.EMPTY;
	private String _SUBJECT = StringUtils.EMPTY;
	private String _REMAGENTID = StringUtils.EMPTY;
	private String _REMAGENTNAME = StringUtils.EMPTY;
	private String _REMITTERID = StringUtils.EMPTY;
	private String _MACNO = StringUtils.EMPTY;
	private String _ENOTE_SEQNO = StringUtils.EMPTY;

	public String getMsgID() {
		return _MsgID;
	}

	public void setMsgID(String value) {
		_MsgID = value;
	}

	public String getMsgType() {
		return _MsgType;
	}

	public void setMsgType(String value) {
		_MsgType = value;
	}

	public String getChlName() {
		return _ChlName;
	}

	public void setChlName(String value) {
		_ChlName = value;
	}

	public String getChlEJNo() {
		return _ChlEJNo;
	}

	public void setChlEJNo(String value) {
		_ChlEJNo = value;
	}

	public String getChlSendTime() {
		return _ChlSendTime;
	}

	public void setChlSendTime(String value) {
		_ChlSendTime = value;
	}

	public String getTxnID() {
		return _TxnID;
	}

	public void setTxnID(String value) {
		_TxnID = value;
	}

	public String getBranchID() {
		return _BranchID;
	}

	public void setBranchID(String value) {
		_BranchID = value;
	}

	public String getTermID() {
		return _TermID;
	}

	public void setTermID(String value) {
		_TermID = value;
	}

	public String getUserID() {
		return _UserID;
	}

	public void setUserID(String value) {
		_UserID = value;
	}

	public String getSignID() {
		return _SignID;
	}

	public void setSignID(String value) {
		_SignID = value;
	}

	/**
	 * 登錄分行
	 */
	public String getORGBRNO() {
		return _ORGBRNO;
	}

	public void setORGBRNO(String value) {
		_ORGBRNO = value;
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
	 * 匯款日期
	 */
	public String getREMDATE() {
		return _REMDATE;
	}

	public void setREMDATE(String value) {
		_REMDATE = value;
	}

	/**
	 * 匯款金額
	 * 
	 * <remark>系統須檢視登錄序號與主管key入匯款金額是否一致(FEP檢核)，一致再帶出下列資料。</remark>
	 */
	public BigDecimal getREMAMT() {
		return _REMAMT;
	}

	public void setREMAMT(BigDecimal value) {
		_REMAMT = value;
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
	 * 匯款帳務別
	 */
	public String getREMTXTP() {
		return _REMTXTP;
	}

	public void setREMTXTP(String value) {
		_REMTXTP = value;
	}

	/**
	 * 匯款種類
	 */
	public String getREMTYPE() {
		return _REMTYPE;
	}

	public void setREMTYPE(String value) {
		_REMTYPE = value;
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
	 * 客戶ID
	 */
	public String getCIFKEY() {
		return _CIFKEY;
	}

	public void setCIFKEY(String value) {
		_CIFKEY = value;
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
	 * 匯款人代號
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
	public String getREMARK() {
		return _REMARK;
	}

	public void setREMARK(String value) {
		_REMARK = value;
	}

	/**
	 * 稅單編號
	 */
	public String getTAXNO() {
		return _TAXNO;
	}

	public void setTAXNO(String value) {
		_TAXNO = value;
	}

	/**
	 * 匯出押碼
	 * 
	 * <remark>押碼欄位 : ICV0-交易日期(N6)+ FEP登錄序號(N7)+ 匯款金額(N13) + 收款人帳號N(14) + 匯款行銀行代號(N3) + 解款行銀行代號(N3) + B'0'(10) . 以匯出基碼押碼</remark>
	 */
	public String getMAC() {
		return _MAC;
	}

	public void setMAC(String value) {
		_MAC = value;
	}

	/**
	 * 原FEP登錄序號
	 */
	public String getORGFEPNO() {
		return _ORGFEPNO;
	}

	public void setORGFEPNO(String value) {
		_ORGFEPNO = value;
	}

	/**
	 * 原匯入日期
	 */
	public String getORGREMDATE() {
		return _ORGREMDATE;
	}

	public void setORGREMDATE(String value) {
		_ORGREMDATE = value;
	}

	/**
	 * 匯款金額
	 */
	public BigDecimal getORGREMAMT() {
		return _ORGREMAMT;
	}

	public void setORGREMAMT(BigDecimal value) {
		_ORGREMAMT = value;
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
	 * 附言
	 */
	public String getREMARKFISC() {
		return _REMARKFISC;
	}

	public void setREMARKFISC(String value) {
		_REMARKFISC = value;
	}

	/**
	 * 輸入行
	 * 
	 * <remark>行員資訊-分行</remark>
	 */
	public String getKINBR() {
		return _KINBR;
	}

	public void setKINBR(String value) {
		_KINBR = value;
	}

	/**
	 * 櫃台機號
	 * 
	 * <remark>行員資訊-機台號</remark>
	 */
	public String getTRMSEQ() {
		return _TRMSEQ;
	}

	public void setTRMSEQ(String value) {
		_TRMSEQ = value;
	}

	/**
	 * 分行登錄序號
	 * 
	 * <remark>分行系統自編</remark>
	 */
	public String getBRSNO() {
		return _BRSNO;
	}

	public void setBRSNO(String value) {
		_BRSNO = value;
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
	 * 主管代號1
	 * 
	 * <remark>視交易才需要放值 RT1010(緊急匯款登錄): 匯款金額超過50萬需主管A刷卡覆核 RT1101(更正)/RT1300(匯出確認)/RT1600確認取消: 不論金額皆需主管A刷卡覆核</remark>
	 */
	public String getSUPNO1() {
		return _SUPNO1;
	}

	public void setSUPNO1(String value) {
		_SUPNO1 = value;
	}

	/**
	 * 主管代號2
	 * 
	 * <remark>視交易才需要放值 臨櫃放行RT1300(匯出確認)/ /FEDI轉通匯RT1301: 匯款金額超過1000萬需二位主管A,B刷卡覆核(主管代號1,2不能相同) 大批匯款放行需第二位主管覆核規定如下: l 126分行不論金額 l 作業中心整批總金額超過1000萬 l 一般分行單筆金額超過1000萬</remark>
	 */
	public String getSUPNO2() {
		return _SUPNO2;
	}

	public void setSUPNO2(String value) {
		_SUPNO2 = value;
	}

	/**
	 * 登錄日期
	 */
	public String getTBSDY() {
		return _TBSDY;
	}

	public void setTBSDY(String value) {
		_TBSDY = value;
	}

	/**
	 * 登錄時間
	 */
	public String getTIME() {
		return _TIME;
	}

	public void setTIME(String value) {
		_TIME = value;
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
	 * 匯出日期
	 */
	public String getINQDATE() {
		return _INQDATE;
	}

	public void setINQDATE(String value) {
		_INQDATE = value;
	}

	/**
	 * 匯出日期-起日
	 */
	public String getInqdateB() {
		return _INQDATE_B;
	}

	public void setInqdateB(String value) {
		_INQDATE_B = value;
	}

	/**
	 * 匯出日期-迄日
	 */
	public String getInqdateE() {
		return _INQDATE_E;
	}

	public void setInqdateE(String value) {
		_INQDATE_E = value;
	}

	/**
	 * 交易行別
	 */
	public List<String> getINQBANK() {
		return _INQBANK;
	}

	public void setINQBANK(List<String> value) {
		_INQBANK = value;
	}

	/**
	 * 執行方式
	 * 
	 * <remark>1:單筆 2.整批 9 : 全部</remark>
	 */
	public String getKIND() {
		return _KIND;
	}

	public void setKIND(String value) {
		_KIND = value;
	}

	/**
	 * 查詢金額
	 * 
	 * <remark>0=全部</remark>
	 */
	public BigDecimal getTXAMT() {
		return _TXAMT;
	}

	public void setTXAMT(BigDecimal value) {
		_TXAMT = value;
	}

	/**
	 * 匯出狀態
	 * 
	 * <remark>02.待補登03.待放行04.已放行05.傳送中06.已解款07.財金拒絕08.退匯09.已刪除未更帳 10.已刪除已更帳11.磁片整批匯出失敗99.全部</remark>
	 */
	public String getOUTSTAT() {
		return _OUTSTAT;
	}

	public void setOUTSTAT(String value) {
		_OUTSTAT = value;
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
	 * 發信記號
	 * 
	 * <remark>1.「0.未發送」表示該筆通訊櫃員已登錄，但未經主管放行。 2. 「1.已發送」表示該筆通訊已經主管放行。 3. 「2.已取消」表示該筆通訊已經主管取消發送。 4. 「3.傳送中」表示該筆通訊已準備下傳傳送予財金。 5. 「4.已收訊」表示該筆通訊財金已收到。 6. 「5.已拒絕」表示該筆通訊已遭財金拒絕。 7. 「6.全部」即包含查詢所有狀況。
	 * 99.計算機問題</remark>
	 */
	public String getSENDFLAG() {
		return _SENDFLAG;
	}

	public void setSENDFLAG(String value) {
		_SENDFLAG = value;
	}

	/**
	 * 解款行
	 */
	public List<String> getRecbankS() {
		return _RECBANK_S;
	}

	public void setRecbankS(List<String> value) {
		_RECBANK_S = value;
	}

	/**
	 * 查詢金額
	 * 
	 * <remark>1.100萬(含)以上, 2.100萬以下, 3.全部</remark>
	 */
	public String getTxamtKind() {
		return _TXAMT_KIND;
	}

	public void setTxamtKind(String value) {
		_TXAMT_KIND = value;
	}

	/**
	 * 匯入狀態
	 * 
	 * <remark>10.匯出被退,11.自動退匯,16.待解款,18.已解款,21.自動入帳,24.滯留交易已入帳,25 滯留交易尚未入帳</remark>
	 */
	public String getINSTAT() {
		return _INSTAT;
	}

	public void setINSTAT(String value) {
		_INSTAT = value;
	}

	/**
	 * 櫃員
	 * 
	 * <remark>空白=全部</remark>
	 */
	public String getTLRNO() {
		return _TLRNO;
	}

	public void setTLRNO(String value) {
		_TLRNO = value;
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
	 * 匯款時間
	 * 
	 * <remark>DATETIME[7:6]</remark>
	 */
	public String getREMTIME() {
		return _REMTIME;
	}

	public void setREMTIME(String value) {
		_REMTIME = value;
	}

	/**
	 * 扣款帳號
	 * 
	 * <remark>DEBIT.ACCT.NO</remark>
	 */
	public String getMACTNO() {
		return _MACTNO;
	}

	public void setMACTNO(String value) {
		_MACTNO = value;
	}

	/**
	 * 中心主機交易序號
	 * 
	 * <remark>transactionId</remark>
	 */
	public String getCBSNO() {
		return _CBSNO;
	}

	public void setCBSNO(String value) {
		_CBSNO = value;
	}

	/**
	 * 扣帳行
	 * 
	 * <remark>CO.CODE</remark>
	 */
	public String getACBRNO() {
		return _ACBRNO;
	}

	public void setACBRNO(String value) {
		_ACBRNO = value;
	}

	/**
	 * 入帳行
	 * 
	 * <remark>T.KPI.BR</remark>
	 */
	public String getDEPBRNO() {
		return _DEPBRNO;
	}

	public void setDEPBRNO(String value) {
		_DEPBRNO = value;
	}

	/**
	 * 收件行
	 * 
	 * <remark>T.COLL.BR</remark>
	 */
	public String getRECBRNO() {
		return _RECBRNO;
	}

	public void setRECBRNO(String value) {
		_RECBRNO = value;
	}

	/**
	 * 通知時間
	 */
	public String getSENDTIME() {
		return _SENDTIME;
	}

	public void setSENDTIME(String value) {
		_SENDTIME = value;
	}

	/**
	 * 處理結果
	 * 
	 * <remark>"FE0000": 處理成功 其它處理失敗</remark>
	 */
	public String getRC() {
		return _RC;
	}

	public void setRC(String value) {
		_RC = value;
	}

	/**
	 * 錯誤訊息
	 * 
	 * <remark>處理結果之中文說明</remark>
	 */
	public String getERRMSG() {
		return _ERRMSG;
	}

	public void setERRMSG(String value) {
		_ERRMSG = value;
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
	 * 金額合計
	 */
	public BigDecimal getTOTALAMT() {
		return _TOTALAMT;
	}

	public void setTOTALAMT(BigDecimal value) {
		_TOTALAMT = value;
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
	 * 附言
	 * 
	 * <remark>退匯理由為99時必須有值</remark>
	 */
	public String getMEMO() {
		return _MEMO;
	}

	public void setMEMO(String value) {
		_MEMO = value;
	}

	/**
	 * 解款日期
	 */
	public String getRECDATE() {
		return _RECDATE;
	}

	public void setRECDATE(String value) {
		_RECDATE = value;
	}

	/**
	 * 匯出時間(起)
	 */
	public String getSendtimeB() {
		return _SENDTIME_B;
	}

	public void setSendtimeB(String value) {
		_SENDTIME_B = value;
	}

	/**
	 * 匯出時間(迄)
	 */
	public String getSendtimeE() {
		return _SENDTIME_E;
	}

	public void setSendtimeE(String value) {
		_SENDTIME_E = value;
	}

	/**
	 * 交易行別
	 */
	public String getINQBK() {
		return _INQBK;
	}

	public void setINQBK(String value) {
		_INQBK = value;
	}

	/**
	 * 匯款行
	 */
	public String getREMBK() {
		return _REMBK;
	}

	public void setREMBK(String value) {
		_REMBK = value;
	}

	/**
	 * 查詢金額(起)
	 * 
	 * <remark>0=全部</remark>
	 */
	public BigDecimal getTxamtB() {
		return _TXAMT_B;
	}

	public void setTxamtB(BigDecimal value) {
		_TXAMT_B = value;
	}

	/**
	 * 查詢金額(迄)
	 * 
	 * <remark>0=全部</remark>
	 */
	public BigDecimal getTxamtE() {
		return _TXAMT_E;
	}

	public void setTxamtE(BigDecimal value) {
		_TXAMT_E = value;
	}

	/**
	 * 幣別
	 * 
	 * <remark>001-USD</remark>
	 */
	public String getCURRENCY() {
		return _CURRENCY;
	}

	public void setCURRENCY(String value) {
		_CURRENCY = value;
	}

	/**
	 * 查詢方式
	 * 
	 * <remark>1.匯出 2.匯入</remark>
	 */
	public String getRMIOFLG() {
		return _RMIOFLG;
	}

	public void setRMIOFLG(String value) {
		_RMIOFLG = value;
	}

	/**
	 * 匯入時間(起)
	 */
	public String getInqtimeB() {
		return _INQTIME_B;
	}

	public void setInqtimeB(String value) {
		_INQTIME_B = value;
	}

	/**
	 * 匯入時間(迄)
	 */
	public String getInqtimeE() {
		return _INQTIME_E;
	}

	public void setInqtimeE(String value) {
		_INQTIME_E = value;
	}

	/**
	 * 客戶ID
	 */
	public String getID() {
		return _ID;
	}

	public void setID(String value) {
		_ID = value;
	}

	/**
	 * 查詢分行(起)
	 */
	public String getInqbrnoB() {
		return _INQBRNO_B;
	}

	public void setInqbrnoB(String value) {
		_INQBRNO_B = value;
	}

	/**
	 * 查詢分行(迄)
	 */
	public String getInqbrnoE() {
		return _INQBRNO_E;
	}

	public void setInqbrnoE(String value) {
		_INQBRNO_E = value;
	}

	/**
	 * 列印記號
	 * 
	 * <remark>9-全部</remark>
	 */
	public String getPRTMK() {
		return _PRTMK;
	}

	public void setPRTMK(String value) {
		_PRTMK = value;
	}

	/**
	 * 收訊日期
	 */
	public String getINDATE() {
		return _INDATE;
	}

	public void setINDATE(String value) {
		_INDATE = value;
	}

	/**
	 * 轉入分行
	 */
	public String getINBRNO() {
		return _INBRNO;
	}

	public void setINBRNO(String value) {
		_INBRNO = value;
	}

	/**
	 * 狀態
	 * 
	 * <remark>18-待解人工入帳</remark>
	 */
	public String getSTATUS() {
		return _STATUS;
	}

	public void setSTATUS(String value) {
		_STATUS = value;
	}

	/**
	 * T24交易序號
	 */
	public String getORGT24NO() {
		return _ORGT24NO;
	}

	public void setORGT24NO(String value) {
		_ORGT24NO = value;
	}

	/**
	 * 通訊登錄序號
	 */
	public String getREMSEQ() {
		return _REMSEQ;
	}

	public void setREMSEQ(String value) {
		_REMSEQ = value;
	}

	/**
	 * 交易代號
	 */
	public String getTXCODE() {
		return _TXCODE;
	}

	public void setTXCODE(String value) {
		_TXCODE = value;
	}

	/**
	 * 原交易代號
	 */
	public String getORGTXCODE() {
		return _ORGTXCODE;
	}

	public void setORGTXCODE(String value) {
		_ORGTXCODE = value;
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
	 * 原批號
	 */
	public String getORGBATCHNO() {
		return _ORGBATCHNO;
	}

	public void setORGBATCHNO(String value) {
		_ORGBATCHNO = value;
	}

	/**
	 * 原分行交易序號
	 */
	public String getORGBRSNO() {
		return _ORGBRSNO;
	}

	public void setORGBRSNO(String value) {
		_ORGBRSNO = value;
	}

	/**
	 * T24交易序號
	 */
	public String getT24NO() {
		return _T24NO;
	}

	public void setT24NO(String value) {
		_T24NO = value;
	}

	/**
	 * 原外圍EJNO
	 */
	public String getORGCHLEJNO() {
		return _ORGCHLEJNO;
	}

	public void setORGCHLEJNO(String value) {
		_ORGCHLEJNO = value;
	}

	/**
	 * 財金回應訊息代號
	 */
	public String getFISCRC() {
		return _FISCRC;
	}

	public void setFISCRC(String value) {
		_FISCRC = value;
	}

	/**
	 * 回覆外圍系統回應代碼
	 */
	public String getCHLRC() {
		return _CHLRC;
	}

	public void setCHLRC(String value) {
		_CHLRC = value;
	}

	/**
	 * 回覆外圍系統回應訊息
	 */
	public String getCHLMSG() {
		return _CHLMSG;
	}

	public void setCHLMSG(String value) {
		_CHLMSG = value;
	}

	/**
	 * 來源別
	 * 
	 * <remark>2：FEDI 4：MMAB2B</remark>
	 */
	public String getORIGINAL() {
		return _ORIGINAL;
	}

	public void setORIGINAL(String value) {
		_ORIGINAL = value;
	}

	/**
	 * 科目
	 * 
	 * <remark>匯入主檔之解款人帳號一之第4至6位</remark>
	 */
	public String getSUBJECT() {
		return _SUBJECT;
	}

	public void setSUBJECT(String value) {
		_SUBJECT = value;
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
}
