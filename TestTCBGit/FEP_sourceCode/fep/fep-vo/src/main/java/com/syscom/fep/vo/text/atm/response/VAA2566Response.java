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
public class VAA2566Response extends ATMXmlBase {
	@XStreamAlias("RsHeader")
	private FEPRsHeader rsHeader;
	@XStreamAlias("SvcRs")
	private VAA2566SvcRs svcRs;

	public FEPRsHeader getRsHeader() {
		return rsHeader;
	}

	public void setRsHeader(FEPRsHeader rsHeader) {
		this.rsHeader = rsHeader;
	}

	public VAA2566SvcRs getSvcRs() {
		return svcRs;
	}

	public void setSvcRs(VAA2566SvcRs svcRs) {
		this.svcRs = svcRs;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		if (this.rsHeader == null) {
			this.rsHeader = new FEPRsHeader();
		}
		if (this.svcRs == null) {
			this.svcRs = new VAA2566SvcRs();
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
		this.svcRs.setRs(new VAA2566Rs());
		this.svcRs.getRs().setHC(response.getHC().toString()); // 手續費-客戶負擔
		this.svcRs.getRs().setBKNO(response.getBKNO()); // 銀行別
		this.svcRs.getRs().setTXACT(response.getTXACT()); // 交易帳號
		this.svcRs.getRs().setRESULT(response.getRESULT()); // 核驗結果
		this.svcRs.getRs().setACRESULT(response.getACRESULT()); // 帳號核驗結果
		this.svcRs.getRs().setACCSTAT(response.getACCSTAT()); // 開戶狀態
		this.svcRs.getRs().setSTAN(response.getSTAN()); // 財金序號
		this.svcRs.getRs().setTBSDY(response.getTBSDY()); // 財金營業日
		this.svcRs.getRs().setModeO(response.getModeO()); // 帳務別
		return this.serializeToXml(this);
		*/return "";
    }

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根
	}

	@XStreamAlias("SvcRs")
	public static class VAA2566SvcRs {
		@XStreamAlias("Rs")
		private VAA2566Rs rs;

		public VAA2566Rs getRs() {
			return rs;
		}

		public void setRs(VAA2566Rs rs) {
			this.rs = rs;
		}
	}

	@XStreamAlias("Rs")
	public static class VAA2566Rs {
		// 手續費-客戶負擔
		private String HC = StringUtils.EMPTY;
		// 銀行別
		private String BKNO = StringUtils.EMPTY;
		// 交易帳號
		private String TXACT = StringUtils.EMPTY;
		// 核驗結果
		private String RESULT = StringUtils.EMPTY;
		// 帳號核驗結果
		private String ACRESULT = StringUtils.EMPTY;
		// 開戶狀態
		private String ACCSTAT = StringUtils.EMPTY;
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
		 * 核驗結果
		 */
		public String getRESULT() {
			return this.RESULT;
		}

		public void setRESULT(String value) {
			this.RESULT = value;
		}

		/**
		 * 帳號核驗結果
		 */
		public String getACRESULT() {
			return this.ACRESULT;
		}

		public void setACRESULT(String value) {
			this.ACRESULT = value;
		}

		/**
		 * 開戶狀態
		 */
		public String getACCSTAT() {
			return this.ACCSTAT;
		}

		public void setACCSTAT(String value) {
			this.ACCSTAT = value;
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
