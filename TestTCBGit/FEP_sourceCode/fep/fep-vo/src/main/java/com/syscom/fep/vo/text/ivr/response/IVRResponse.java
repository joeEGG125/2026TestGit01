package com.syscom.fep.vo.text.ivr.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.ivr.IVRGeneral;
import com.syscom.fep.vo.text.ivr.IVRTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class IVRResponse extends IVRTextBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private IVRRESSvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public IVRRESSvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(IVRRESSvcRs svcRs) {
		this.svcRs = svcRs;
	}

	@Override
	public String makeMessageFromGeneral(IVRGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@Override
	public void toGeneral(IVRGeneral general) throws Exception {
		// TODO 自動生成的方法存根

	}

	@XStreamAlias("SvcRs")
	public static class IVRRESSvcRs {
		@XStreamAlias("Rs")
		private IVRRESRs rs;

		public IVRRESRs getRs() {
			return rs;
		}

		public void setRs(IVRRESRs rs) {
			this.rs = rs;
		}
	}

	@XStreamAlias("Rs")
	public static class IVRRESRs {}
}
