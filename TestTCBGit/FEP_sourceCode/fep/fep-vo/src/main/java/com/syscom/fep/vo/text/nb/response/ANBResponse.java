package com.syscom.fep.vo.text.nb.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralResponse;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class ANBResponse extends NBTextBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private ANBSvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader value) {
		rsHeader = value;
	}

	public ANBSvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(ANBSvcRs value) {
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
		response.setCUSTID(this.getSvcRs().getRs().getCustId());
		response.setUSERCODE(getSvcRs().getRs().getUserCode());		
		response.setPASSWORD(this.getSvcRs().getRs().getPassword());
		response.setRETRY(getSvcRs().getRs().getRetry());
		response.setAPPLYLIMITDATE(this.getSvcRs().getRs().getApplyLimitDate());
		
	}

	@Override
	public String makeMessageFromGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@XStreamAlias("SvcRs")
	public static class ANBSvcRs {
		@XStreamAlias("Rs")
		private ANBRs rs;

		public ANBRs getRs() {
			return this.rs;
		}

		public void setRs(ANBRs value) {
			this.rs = value;
		}
	}

	@XStreamAlias("Rs")
	public static class ANBRs {
		private String custId = StringUtils.EMPTY;
		private String userCode = StringUtils.EMPTY;
		private String password = StringUtils.EMPTY;
		private String retry = StringUtils.EMPTY;
		private String applyLimitDate = StringUtils.EMPTY;
		public String getCustId() {
			return custId;
		}
		
		public void setCustId(String value) {
			this.custId = value;
		}
		
		public String getUserCode() {
			return userCode;
		}
		
		public void setUserCode(String value) {
			this.userCode = value;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(String value) {
			this.password = value;
		}
		
		public String getRetry() {
			return retry;
		}
		
		public void setRetry(String value) {
			this.retry = value;
		}
		
		public String getApplyLimitDate() {
			return applyLimitDate;
		}
		
		public void setApplyLimitDate(String value) {
			this.applyLimitDate = value;
		}

	}
}
