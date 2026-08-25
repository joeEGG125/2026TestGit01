package com.syscom.fep.vo.text.atm.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class CHKMTPMACRequest extends ATMXmlBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private CHKMTPMACSvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public CHKMTPMACSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(CHKMTPMACSvcRq svcRq) {
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
	public static class CHKMTPMACSvcRq {
		@XStreamAlias("Rq")
		private CHKMTPMACRq rq;

		public CHKMTPMACRq getRq() {
			return rq;
		}

		public void setRq(CHKMTPMACRq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class CHKMTPMACRq {
		// 交易日期時間
		private String TXDATETM = StringUtils.EMPTY;
		// 交易訊息
		private String MSG = StringUtils.EMPTY;
		// 基碼多樣化資料
		private String DIV = StringUtils.EMPTY;
		// MAC值
		private String MAC = StringUtils.EMPTY;
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
			TXDATETM = value;
		}

		/**
		 * 交易訊息
		 * 
		 * @return
		 */
		public String getMSG() {
			return MSG;
		}

		public void setMSG(String value) {
			MSG = value;
		}

		/**
		 * 基碼多樣化資料
		 * 
		 * @return
		 */
		public String getDIV() {
			return DIV;
		}

		public void setDIV(String value) {
			DIV = value;
		}

		/**
		 * MAC值
		 * 
		 * @return
		 */
		public String getMAC() {
			return MAC;
		}

		public void setMAC(String value) {
			MAC = value;
		}

		/**
		 * 基碼代號
		 * 
		 * @return
		 */
		public String getKeyId() {
			return KEY_ID;
		}

		public void setKeyId(String value) {
			KEY_ID = value;
		}
	}
}
