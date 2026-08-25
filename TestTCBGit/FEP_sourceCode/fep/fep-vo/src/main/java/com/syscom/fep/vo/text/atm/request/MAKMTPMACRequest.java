package com.syscom.fep.vo.text.atm.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class MAKMTPMACRequest extends ATMXmlBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private MAKMTPMACSvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public MAKMTPMACSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(MAKMTPMACSvcRq svcRq) {
		this.svcRq = svcRq;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根
	}

	@XStreamAlias("SvcRq")
	public static class MAKMTPMACSvcRq {
		@XStreamAlias("Rq")
		private MAKMTPMACRq rq;

		public MAKMTPMACRq getRq() {
			return rq;
		}

		public void setRq(MAKMTPMACRq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class MAKMTPMACRq {
		// 交易日期時間
		private String TXDATETM = StringUtils.EMPTY;

		// 交易訊息
		private String MSG = StringUtils.EMPTY;

		// 基碼多樣化資料
		private String DIV = StringUtils.EMPTY;

		// 基碼代號
		private String KEY_ID = StringUtils.EMPTY;

		/**
		 * 交易日期時間
		 * 
		 * <remark>YYYYMMDDhhmmss 西元年</remark>
		 */
		public String getTXDATETM() {
			return TXDATETM;
		}

		public void setTXDATETM(String value) {
			this.TXDATETM = value;
		}

		/**
		 * 交易訊息
		 */
		public String getMSG() {
			return MSG;
		}

		public void setMSG(String value) {
			this.MSG = value;
		}

		/**
		 * 基碼多樣化資料
		 */
		public String getDIV() {
			return DIV;
		}

		public void setDIV(String value) {
			this.DIV = value;
		}

		/**
		 * 基碼代號
		 */
		public String getKeyId() {
			return KEY_ID;
		}

		public void setKeyId(String value) {
			this.KEY_ID = value;
		}
	}
}
