package com.syscom.fep.vo.text.nb.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralResponse;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class PSNResponse extends NBTextBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private PSNSvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader value) {
		rsHeader = value;
	}

	public PSNSvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(PSNSvcRs value) {
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
	}

	@Override
	public String makeMessageFromGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@XStreamAlias("SvcRs")
	public static class PSNSvcRs {
		@XStreamAlias("Rs")
		private PSNRs rs;

		public PSNRs getRs() {
			return this.rs;
		}

		public void setRs(PSNRs value) {
			this.rs = value;
		}
	}

	@XStreamAlias("Rs")
	public static class PSNRs {}
}
