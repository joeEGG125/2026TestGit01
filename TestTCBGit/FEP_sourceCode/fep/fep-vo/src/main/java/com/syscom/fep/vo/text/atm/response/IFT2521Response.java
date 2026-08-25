package com.syscom.fep.vo.text.atm.response;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderOverride;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStat;
import com.syscom.fep.vo.text.FEPRsHeader.FEPRsHeaderRsStatRsStatCode;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralResponse;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class IFT2521Response extends ATMXmlBase {

	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private IFT2521SvcRs svcRs;
	public FEPRsHeader getRsHeader()
	{
		return rsHeader;
	}
	public void setRsHeader(FEPRsHeader value)
	{
		rsHeader = value;
	}

	public IFT2521SvcRs getSvcRs()
	{
		return svcRs;
	}
	public void setSvcRs(IFT2521SvcRs value)
	{
		svcRs = value;
	}
	
	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		if (this.rsHeader == null) {
			this.rsHeader = new FEPRsHeader();
		}
		if (this.svcRs == null) {
			this.svcRs = new IFT2521SvcRs();
		}
		/*
		ATMGeneralResponse response = general.getResponse();
		this.rsHeader.setChlEJNo(response.getChlEJNo());
		this.rsHeader.setEJNo(response.getEJNo());
		this.rsHeader.setRsStat(new FEPRsHeaderRsStat());
		this.rsHeader.getRsStat().setRsStatCode(new FEPRsHeaderRsStatRsStatCode());
		this.rsHeader.getRsStat().getRsStatCode().setType(response.getRsStatRsStateCodeType());
		this.rsHeader.getRsStat().getRsStatCode().setValue(response.getRsStatRsStateCode());
		this.rsHeader.getRsStat().setDesc(response.getRsStatDesc());
		this.rsHeader.setRqTime(response.getRqTime());
		this.rsHeader.setRsTime(response.getRsTime());
		if (response.getOverrides() == null) {
			this.rsHeader.setOverrides(new FEPRsHeaderOverride[0]);
		} else {
			this.rsHeader.setOverrides(response.getOverrides());
		}
		this.svcRs.setRs(new IFT2521Rs());
		this.svcRs.getRs().setHC(response.getHC().toString()); // 手續費-客戶負擔
		this.svcRs.getRs().setBKNO(response.getBKNO()); // 銀行別
		this.svcRs.getRs().setTXACT(response.getTXACT()); // 交易帳號		
		this.svcRs.getRs().setDPBK(response.getDPBK()); // 轉入行
		this.svcRs.getRs().setDPACT(response.getDPACT().toString());    //轉入帳號		
		this.svcRs.getRs().setTXAMT(response.getTXAMT().toString());    //交易金額
		this.svcRs.getRs().setBAL11S(response.getBal11S()); //正負號
		this.svcRs.getRs().setBAL11(response.getBAL11().toString()); // 可用餘額
		this.svcRs.getRs().setBAL12S(response.getBal12S()); //正負號
		this.svcRs.getRs().setBAL12(response.getBAL12().toString()); // 可用餘額
		this.svcRs.getRs().setSTAN(response.getSTAN()); // 財金序號
		this.svcRs.getRs().setTBSDY(response.getTBSDY()); // 財金營業日
		this.svcRs.getRs().setModeO(response.getModeO()); // 帳務別
		*/
		return this.serializeToXml(this);
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根

	}
	
	@XStreamAlias("SvcRs")
	public static class IFT2521SvcRs {
		@XStreamAlias("Rs")
		private IFT2521Rs rs;

		public IFT2521Rs getRs() {
			return rs;
		}

		public void setRs(IFT2521Rs rs) {
			this.rs = rs;
		}
	}

	@XStreamAlias("Rs")
	public static class IFT2521Rs {
		// 手續費-客戶負擔
		private String HC = StringUtils.EMPTY;
		// 銀行別
		private String BKNO = StringUtils.EMPTY;
		// 交易帳號
		private String TXACT = StringUtils.EMPTY;
	    // 轉入行
		private String DPBK = StringUtils.EMPTY;
	    // 轉入帳號
		private String DPACT = StringUtils.EMPTY;            
	    // '交易金額
		private String TXAMT = StringUtils.EMPTY;		
	    // 正負號
	    private String BAL11S  = StringUtils.EMPTY;
	    // 可用餘額
	    private String BAL11 = StringUtils.EMPTY;
	    // 正負號
		private String BAL12S = StringUtils.EMPTY;
	    // 帳戶餘額
		private String BAL12 = StringUtils.EMPTY;
		// 財金序號
		private String STAN = StringUtils.EMPTY;
		// 財金營業日
		private String TBSDY = StringUtils.EMPTY;
		// 帳務別
		private String MODE_O = StringUtils.EMPTY;

		/**
		 * 手續費-客戶負擔
		 */
		public String getHC() {
			return this.HC;
		}

		public void setHC(String value) {
			this.HC = value;
		}

		/**
		 * 銀行別
		 */
		public String getBKNO() {
			return this.BKNO;
		}

		public void setBKNO(String value) {
			this.BKNO = value;
		}

		/**
		 * 交易帳號
		 */
		public String getTXACT() {
			return this.TXACT;
		}

		public void setTXACT(String value) {
			this.TXACT = value;
		}

		/**
		 * 轉入行
		 */
		public String getDPBK() {
			return this.DPBK;
		}

		public void setDPBK(String value) {
			this.DPBK = value;
		}
		
		/**
		 * 轉入帳號
		 */
		public String getDPACT() {
			return this.DPACT;
		}

		public void setDPACT(String value) {
			this.DPACT = value;
		}
		
		/**
		 * 交易金額
		 */
		public String getTXAMT() {
			return this.TXAMT;
		}

		public void setTXAMT(String value) {
			this.TXAMT = value;
		}

		/**
		 * 正負號
		 */
		public String getBAL11S() {
			return this.BAL11S;
		}

		public void setBAL11S(String value) {
			this.BAL11S = value;
		}

		/**
		 * 可用餘額
		 */
		public String getBAL11() {
			return this.BAL11;
		}

		public void setBAL11(String value) {
			this.BAL11 = value;
		}

		/**
		 * 正負號
		 */
		public String getBAL12S() {
			return this.BAL12S;
		}

		public void setBAL12S(String value) {
			this.BAL12S = value;
		}

		/**
		 * 可用餘額
		 */
		public String getBAL12() {
			return this.BAL12;
		}

		public void setBAL12(String value) {
			this.BAL12 = value;
		}
		
		/**
		 * 財金序號
		 */
		public String getSTAN() {
			return this.STAN;
		}

		public void setSTAN(String value) {
			this.STAN = value;
		}

		/**
		 * 財金營業日
		 */
		public String getTBSDY() {
			return this.TBSDY;
		}

		public void setTBSDY(String value) {
			this.TBSDY = value;
		}

		/**
		 * 帳務別
		 * 
		 * 1:本日帳(3:30之前,財金未換日)
		 * 2:次日帳(3:30之後,財金已換日)
		 */
		public String getModeO() {
			return this.MODE_O;
		}

		public void setModeO(String value) {
			this.MODE_O = value;
		}
	}
}
