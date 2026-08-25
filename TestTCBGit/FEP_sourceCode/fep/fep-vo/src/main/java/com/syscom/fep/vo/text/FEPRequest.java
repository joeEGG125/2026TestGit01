package com.syscom.fep.vo.text;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class FEPRequest {
	@XStreamAlias("RqHeader")
	private FEPRequestRqHeader rqHeader = new FEPRequestRqHeader();
	@XStreamAlias("SvcRq")
	private FEPRequestSvcRq svcRq = new FEPRequestSvcRq();

	public FEPRequestRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRequestRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public FEPRequestSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(FEPRequestSvcRq svcRq) {
		this.svcRq = svcRq;
	}

	@XStreamAlias("RqHeader")
	public static class FEPRequestRqHeader {
		private String MsgID;
		private String MsgType;
		private String ChlName;
		private String ChlEJNo;
		private String ChlSendTime;
		private String TxnID;
		private String BranchID;
		private String TermID;
		private String UserID;
		private String SignID;

		public String getMsgID() {
			return MsgID;
		}

		public void setMsgID(String msgID) {
			this.MsgID = msgID;
		}

		public String getMsgType() {
			return MsgType;
		}

		public void setMsgType(String msgType) {
			this.MsgType = msgType;
		}

		public String getChlName() {
			return ChlName;
		}

		public void setChlName(String chlName) {
			this.ChlName = chlName;
		}

		public String getChlEJNo() {
			return ChlEJNo;
		}

		public void setChlEJNo(String chlEJNo) {
			this.ChlEJNo = chlEJNo;
		}

		public String getChlSendTime() {
			return ChlSendTime;
		}

		public void setChlSendTime(String chlSendTime) {
			this.ChlSendTime = chlSendTime;
		}

		public String getTxnID() {
			return TxnID;
		}

		public void setTxnID(String txnID) {
			this.TxnID = txnID;
		}

		public String getBranchID() {
			return BranchID;
		}

		public void setBranchID(String branchID) {
			this.BranchID = branchID;
		}

		public String getTermID() {
			return TermID;
		}

		public void setTermID(String termID) {
			this.TermID = termID;
		}

		public String getUserID() {
			return UserID;
		}

		public void setUserID(String userID) {
			this.UserID = userID;
		}

		public String getSignID() {
			return SignID;
		}

		public void setSignID(String signID) {
			this.SignID = signID;
		}
	}

	@XStreamAlias("SvcRq")
	public static class FEPRequestSvcRq {
		private String Rq;

		public String getRq() {
			return Rq;
		}

		public void setRq(String rq) {
			this.Rq = rq;
		}
	}
}
