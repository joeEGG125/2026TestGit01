package com.syscom.fep.vo.text.nb.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralRequest;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class PSNRequest extends NBTextBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private PSNSvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeaderField) {
		this.rqHeader = rqHeaderField;
	}

	public PSNSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(PSNSvcRq svcRqField) {
		this.svcRq = svcRqField;
	}

	@Override
	public String makeMessageFromGeneral(NBGeneral general) {
		NBGeneralRequest request = general.getRequest();
		this.getRqHeader().setChlName(request.getChlName());
		this.getRqHeader().setChlEJNo(request.getChlEJNo());
		this.getRqHeader().setBranchID(request.getBranchID());
		this.getRqHeader().setChlSendTime(request.getChlSendTime());
		this.getRqHeader().setMsgID(request.getMsgID());
		this.getRqHeader().setMsgType(request.getMsgType());
		this.getRqHeader().setTermID(request.getTermID());
		this.getRqHeader().setTxnID(request.getTxnID());
		this.getRqHeader().setUserID(request.getUserID());
		this.getRqHeader().setSignID(request.getSignID());
		this.getSvcRq().getRq().setBankCode(request.getBankCode()); // 金融代號
		this.getSvcRq().getRq().setSendMailToo(request.getSendMailToo()); // 身分證字號
		this.getSvcRq().getRq().setIDNO(request.getIDNO()); // 身分證字號
		this.getSvcRq().getRq().setATMNO(request.getATMNO()); // ATM機台號碼
		return this.serializeToXml(this);
	}

	@Override
	public void toGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
	}

	@XStreamAlias("SvcRq")
	public static class PSNSvcRq {
		@XStreamAlias("Rq")
		private PSNRq rq;

		public PSNRq getRq() {
			return this.rq;
		}

		public void setRq(PSNRq value) {
			this.rq = value;
		}
	}

	@XStreamAlias("Rq")
	public static class PSNRq {
		// 金融代號
		private String BankCode = StringUtils.EMPTY;
		// 是否同時寄送郵件
		private String SendMailToo = StringUtils.EMPTY;
		// 身分證字號
		private String IDNO = StringUtils.EMPTY;
		// ATM機台號碼
		private String ATMNO = StringUtils.EMPTY;

		/**
		 * 金融代號
		 * 
		 * <remark></remark>
		 */
		public String getBankCode() {
			return BankCode;
		}

		public void setBankCode(String value) {
			BankCode = value;
		}

		/**
		 * 是否同時寄送郵件
		 * 
		 * <remark>true=同時寄送郵件 false=不寄送郵件</remark>
		 */
		public String getSendMailToo() {
			return SendMailToo;
		}

		public void setSendMailToo(String value) {
			SendMailToo = value;
		}

		/**
		 * 身分證字號
		 * 
		 * <remark></remark>
		 */
		public String getIDNO() {
			return IDNO;
		}

		public void setIDNO(String value) {
			IDNO = value;
		}

		/**
		 * ATM機台號碼
		 * 
		 * <remark></remark>
		 */
		public String getATMNO() {
			return ATMNO;
		}

		public void setATMNO(String value) {
			ATMNO = value;
		}
	}
}
