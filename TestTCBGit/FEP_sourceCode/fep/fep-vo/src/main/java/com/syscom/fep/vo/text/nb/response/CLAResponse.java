package com.syscom.fep.vo.text.nb.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralResponse;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class CLAResponse extends NBTextBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private CLASvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader value) {
		rsHeader = value;
	}

	public CLASvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(CLASvcRs value) {
		svcRs = value;
	}

	/**
	 * 對應到General
	 */
	public void toGeneral(NBGeneral general) {
		NBGeneralResponse response = general.getResponse();
		response.setChlEJNo(this.getRsHeader().getChlEJNo());
		response.setEJNo(this.getRsHeader().getEJNo());
		response.setRqTime(this.getRsHeader().getRqTime());
		response.setRsStatRsStateCode(this.getRsHeader().getRsStat().getRsStatCode().getValue());
		response.setRsStatRsStateCodeType(this.getRsHeader().getRsStat().getRsStatCode().getType());
		response.setRsStatDesc(this.getRsHeader().getRsStat().getDesc());
		response.setRsTime(this.getRsHeader().getRsTime());
		response.setOverrides(this.getRsHeader().getOverrides());
		response.setAcct(this.getSvcRs().getRs().getAcct()); // 提款帳號
		response.setSecurityCode(getSvcRs().getRs().getSecurityCode()); // 提款序號
	}

	@Override
	public String makeMessageFromGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@XStreamAlias("SvcRs")
	public static class CLASvcRs {
		@XStreamAlias("Rs")
		private CLARs rs;

		public CLARs getRs() {
			return this.rs;
		}

		public void setRs(CLARs value) {
			this.rs = value;
		}
	}

	@XStreamAlias("Rs")
	public static class CLARs {
		// 提款帳號
		private String Acct = StringUtils.EMPTY;
		// 提款序號
		private String SecurityCode = StringUtils.EMPTY;

		/**
		 * 提款帳號
		 */
		public String getAcct() {
			return this.Acct;
		}

		public void setAcct(String value) {
			this.Acct = value;
		}

		/**
		 * 提款序號
		 */
		public String getSecurityCode() {
			return this.SecurityCode;
		}

		public void setSecurityCode(String value) {
			this.SecurityCode = value;
		}
	}
}
