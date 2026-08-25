package com.syscom.fep.vo.text.atm.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class EFT226XResponse extends ATMXmlBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private EFT226XSvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public EFT226XSvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(EFT226XSvcRs svcRs) {
		this.svcRs = svcRs;
	}


	/**
	 從 General 對應回來

	 <remark></remark>
	 */
	@Override
	public String makeMessageFromGeneral(ATMGeneral general) {
		if (this.rsHeader == null) {
			this.rsHeader = new FEPRsHeader();
		}
		if (this.svcRs == null) {
			this.svcRs = new EFT226XSvcRs();
		}
		/*
		this.rsHeader.setChlEJNo(general.getResponse().getChlEJNo());
		this.rsHeader.setChlEJNo(general.getResponse().getEJNo());
		this.rsHeader.setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
		this.rsHeader.getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
		this.rsHeader.getRsStat().getRsStatCode().setType(general.getResponse().getRsStatRsStateCodeType());
		this.rsHeader.getRsStat().getRsStatCode().setValue(general.getResponse().getRsStatRsStateCode());
		this.rsHeader.getRsStat().setDesc(general.getResponse().getRsStatDesc());
		this.rsHeader.setRqTime(general.getResponse().getRqTime());
		this.rsHeader.setRsTime(general.getResponse().getRsTime());
		if (general.getResponse().getOverrides() == null) {
			FEPRsHeader.FEPRsHeaderOverride[] ovr = new FEPRsHeader.FEPRsHeaderOverride[1];
			this.rsHeader.setOverrides(ovr);
		} else {
			this.rsHeader.setOverrides(general.getResponse().getOverrides());
		}
		this.svcRs.setRs(new EFT226XRs());
		this.svcRs.getRs().setHC(general.getResponse().getHC().toString()); //手續費-客戶負擔
		this.svcRs.getRs().setBKNO(general.getResponse().getBKNO()); //銀行別
		this.svcRs.getRs().setTXACT(general.getResponse().getTXACT()); //交易帳號
		this.svcRs.getRs().setDPBK(general.getResponse().getDPBK()); //轉入行
		this.svcRs.getRs().setDPACT(general.getResponse().getDPACT()); //轉入帳號
		this.svcRs.getRs().setTXAMT(general.getResponse().getTXAMT().toString()); //交易金額
		this.svcRs.getRs().setBAL11S(general.getResponse().getBal11S()); //正負號
		this.svcRs.getRs().setBAL11(general.getResponse().getBAL11().toString()); //可用餘額
		this.svcRs.getRs().setBAL12S(general.getResponse().getBal12S()); //正負號
		this.svcRs.getRs().setBAL12(general.getResponse().getBAL12().toString()); //帳戶餘額
		this.svcRs.getRs().setSTAN(general.getResponse().getSTAN()); //財金序號
		this.svcRs.getRs().setTBSDY(general.getResponse().getTBSDY()); //財金營業日
		this.svcRs.getRs().setMODEO(general.getResponse().getModeO()); //帳務別
		this.svcRs.getRs().setCBSTXID(general.getResponse().getCbsTxid()); //CBS交易序號

		return serializeToXml(this);
		*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根

	}

	@XStreamAlias("SvcRs")
	public static class EFT226XSvcRs {
		@XStreamAlias("Rs")
		private EFT226XRs rs;

		public EFT226XRs getRs() {
			return rs;
		}

		public void setRs(EFT226XRs rs) {
			this.rs = rs;
		}
	}

	@XStreamAlias("Rs")
	public static class EFT226XRs {
		//手續費-客戶負擔
		private String HC = "";

		//銀行別
		private String BKNO = "";

		//交易帳號
		private String TXACT = "";

		//轉入行
		private String DPBK = "";

		//轉入帳號
		private String DPACT = "";

		//交易金額
		private String TXAMT = "";

		//正負號
		private String BAL11_S = "";

		//可用餘額
		private String BAL11 = "";

		//正負號
		private String BAL12_S = "";

		//帳戶餘額
		private String BAL12 = "";

		//財金序號
		private String STAN = "";

		//財金營業日
		private String TBSDY = "";

		//帳務別
		private String MODE_O = "";

		//CBS交易序號
		private String CBS_TXID = "";


		public String getHC() {
			return HC;
		}

		public void setHC(String HC) {
			this.HC = HC;
		}

		public String getBKNO() {
			return BKNO;
		}

		public void setBKNO(String BKNO) {
			this.BKNO = BKNO;
		}

		public String getTXACT() {
			return TXACT;
		}

		public void setTXACT(String TXACT) {
			this.TXACT = TXACT;
		}

		public String getDPBK() {
			return DPBK;
		}

		public void setDPBK(String DPBK) {
			this.DPBK = DPBK;
		}

		public String getDPACT() {
			return DPACT;
		}

		public void setDPACT(String DPACT) {
			this.DPACT = DPACT;
		}

		public String getTXAMT() {
			return TXAMT;
		}

		public void setTXAMT(String TXAMT) {
			this.TXAMT = TXAMT;
		}

		public String getBAL11S() {
			return BAL11_S;
		}

		public void setBAL11S(String BAL11_S) {
			this.BAL11_S = BAL11_S;
		}

		public String getBAL11() {
			return BAL11;
		}

		public void setBAL11(String BAL11) {
			this.BAL11 = BAL11;
		}

		public String getBAL12S() {
			return BAL12_S;
		}

		public void setBAL12S(String BAL12_S) {
			this.BAL12_S = BAL12_S;
		}

		public String getBAL12() {
			return BAL12;
		}

		public void setBAL12(String BAL12) {
			this.BAL12 = BAL12;
		}

		public String getSTAN() {
			return STAN;
		}

		public void setSTAN(String STAN) {
			this.STAN = STAN;
		}

		public String getTBSDY() {
			return TBSDY;
		}

		public void setTBSDY(String TBSDY) {
			this.TBSDY = TBSDY;
		}

		public String getMODEO() {
			return MODE_O;
		}

		public void setMODEO(String MODE_O) {
			this.MODE_O = MODE_O;
		}

		public String getCBSTXID() {
			return CBS_TXID;
		}

		public void setCBSTXID(String CBS_TXID) {
			this.CBS_TXID = CBS_TXID;
		}
	}
}
