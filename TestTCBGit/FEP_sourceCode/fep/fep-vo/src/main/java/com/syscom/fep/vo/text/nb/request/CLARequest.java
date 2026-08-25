package com.syscom.fep.vo.text.nb.request;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralRequest;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class CLARequest extends NBTextBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private CLASvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader value) {
		rqHeader = value;
	}

	public CLASvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(CLASvcRq value) {
		svcRq = value;
	}

	/**
	 * 從 General 對應回來
	 * 
	 * @param general
	 * @return
	 */
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
		this.getSvcRq().getRq().setIDNO(request.getIDNO()); // 身分證字號
		this.getSvcRq().getRq().setAcct(request.getAcct()); // 提款帳號
		this.getSvcRq().getRq().setSecurityCode(request.getSecurityCode()); // 提款序號
		this.getSvcRq().getRq().setAmount(request.getAmount()); // 提款金額
		this.getSvcRq().getRq().setCurrency(request.getCurrency()); // 幣別
		this.getSvcRq().getRq().setATMNO(request.getATMNO()); // ATM機台號碼
		this.getSvcRq().getRq().setFee(request.getFee()); // 手續費
		this.getSvcRq().getRq().setMemo(request.getMemo()); // 備註
		this.getSvcRq().getRq().setTxnDate(request.getTxnDate()); // 申請日期時間
		this.getSvcRq().getRq().setTransactionID(request.getTransactionID()); // 交易序號
		return this.serializeToXml(this);
	}

	@Override
	public void toGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
	}

	@XStreamAlias("SvcRq")
	public static class CLASvcRq {
		@XStreamAlias("Rq")
		private CLARq rq;

		public CLARq getRq() {
			return this.rq;
		}

		public void setRq(CLARq value) {
			this.rq = value;
		}
	}

	@XStreamAlias("Rq")
	public static class CLARq {
		// 金融代號
		private String BankCode = StringUtils.EMPTY;
		// 身分證字號
		private String IDNO = StringUtils.EMPTY;
		// 提款帳號
		private String Acct = StringUtils.EMPTY;
		// 提款序號
		private String SecurityCode = StringUtils.EMPTY;
		// 提款金額
		private BigDecimal Amount = new BigDecimal(0);
		// 幣別
		private String Currency = StringUtils.EMPTY;
		// ATM機台號碼
		private String ATMNO = StringUtils.EMPTY;
		// 手續費
		private BigDecimal Fee = new BigDecimal(0);
		// 備註
		private String Memo = StringUtils.EMPTY;
		// 申請日期時間
		private String TxnDate = StringUtils.EMPTY;
		// 交易序號
		private String TransactionID = StringUtils.EMPTY;

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
		 * 提款帳號
		 * 
		 * <remark></remark>
		 */
		public String getAcct() {
			return Acct;
		}

		public void setAcct(String value) {
			Acct = value;
		}

		/**
		 * 提款序號
		 * 
		 * <remark>需檢核是否為16碼數字</remark>
		 */
		public String getSecurityCode() {
			return SecurityCode;
		}

		public void setSecurityCode(String value) {
			SecurityCode = value;
		}

		/**
		 * 提款金額
		 * 
		 * <remark>檢核是否與預約之提款金額一致</remark>
		 */
		public BigDecimal getAmount() {
			return Amount;
		}

		public void setAmount(BigDecimal value) {
			Amount = value;
		}

		/**
		 * 幣別
		 * 
		 * <remark>檢核是否與預約之提款幣別一致</remark>
		 */
		public String getCurrency() {
			return Currency;
		}

		public void setCurrency(String value) {
			Currency = value;
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

		/**
		 * 手續費
		 * 
		 * <remark></remark>
		 */
		public BigDecimal getFee() {
			return Fee;
		}

		public void setFee(BigDecimal value) {
			Fee = value;
		}

		/**
		 * 備註
		 * 
		 * <remark></remark>
		 */
		public String getMemo() {
			return Memo;
		}

		public void setMemo(String value) {
			Memo = value;
		}

		/**
		 * 申請日期時間
		 * 
		 * <remark>必輸申請日期時間</remark>
		 */
		public String getTxnDate() {
			return TxnDate;
		}

		public void setTxnDate(String value) {
			TxnDate = value;
		}

		/**
		 * 交易序號
		 * 
		 * <remark>ATM提領成功時的交易序號</remark>
		 */
		public String getTransactionID() {
			return TransactionID;
		}

		public void setTransactionID(String value) {
			TransactionID = value;
		}
	}
}
