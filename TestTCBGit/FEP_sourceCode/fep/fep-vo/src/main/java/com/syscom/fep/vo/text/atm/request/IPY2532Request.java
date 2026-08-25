package com.syscom.fep.vo.text.atm.request;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class IPY2532Request extends ATMXmlBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private IPY2532SvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public IPY2532SvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(IPY2532SvcRq svcRq) {
		this.svcRq = svcRq;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
		// TODO 自動生成的方法存根
		return null;
	}

	@Override
	public void toGeneral(ATMGeneral general) throws Exception {
		/*
		ATMGeneralRequest request = general.getRequest();
		request.setChlName(this.rqHeader.getChlName());
		request.setChlEJNo(this.rqHeader.getChlEJNo());
		request.setBranchID(this.rqHeader.getBranchID());
		request.setChlSendTime(this.rqHeader.getChlSendTime());
		request.setMsgID(this.rqHeader.getMsgID());
		request.setMsgType(this.rqHeader.getMsgType());
		request.setTermID(this.rqHeader.getTermID());
		request.setTxnID(this.rqHeader.getTxnID());
		request.setUserID(this.rqHeader.getUserID());
		request.setBKNO(this.svcRq.getRq().getBKNO()); // 銀行別
		request.setTXACT(this.svcRq.getRq().getTXACT()); // 交易帳號
		try {
			request.setTXAMT(new BigDecimal(this.svcRq.getRq().getTXAMT())); // 交易金額
		} catch (NumberFormatException e) {
		}

		request.setCLASS(this.svcRq.getRq().getCLASS()); // 繳款種類
		request.setPAYCNO(this.svcRq.getRq().getPAYCNO()); // 銷帳編號
		request.setDUEDATE(this.svcRq.getRq().getDUEDATE());// 繳款到期日
		request.setUNIT(this.svcRq.getRq().getUNIT()); // 稽徵機關別
		request.setIDNO(this.svcRq.getRq().getIDNO());// 身分證字號
		request.setMODE(this.svcRq.getRq().getMODE());// 帳務別
		request.setPsbmemoD(this.svcRq.getRq().getPsbmemoD()); // 存摺摘要_借方
		request.setPsbmemoC(this.svcRq.getRq().getPsbmemoC()); // 存摺摘要_貸方
		request.setPsbremSD(this.svcRq.getRq().getPsbremSD()); // 存摺備註_借方
		request.setPsbremSC(this.svcRq.getRq().getPsbremSC()); // 存摺備註_貸方
		request.setPsbremFD(this.svcRq.getRq().getPsbremFD()); // 往來明細_借方
		request.setPsbremFC(this.svcRq.getRq().getPsbremFC()); // 往來明細_貸方
		request.setIPADDR(this.svcRq.getRq().getIPADDR()); // 使用者登入IP
		*/
	}

	@XStreamAlias("SvcRq")
	public static class IPY2532SvcRq {
		@XStreamAlias("Rq")
		private IPY2532Rq rq;

		public IPY2532Rq getRq() {
			return rq;
		}

		public void setRq(IPY2532Rq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class IPY2532Rq {
		// 銀行別
		private String BKNO = StringUtils.EMPTY;
		// 交易帳號
		private String TXACT = StringUtils.EMPTY;
		// 交易金額
		private String TXAMT = StringUtils.EMPTY;		
		// 繳款種類
		private String CLASS = StringUtils.EMPTY;
	    // 銷帳編號
		private String PAYCNO = StringUtils.EMPTY;
	    // 繳款到期日
		private String DUEDATE = StringUtils.EMPTY;
	    // 稽徵機關別
		private String UNIT = StringUtils.EMPTY;
	    // 身分證字號
		private String IDNO = StringUtils.EMPTY;		
		// 帳務別
		private String MODE = StringUtils.EMPTY;
		// 存摺摘要_借方
		private String PSBMEMO_D = StringUtils.EMPTY;
		// 存摺摘要_貸方
		private String PSBMEMO_C = StringUtils.EMPTY;
		// 存摺備註_借方
		private String PSBREM_S_D = StringUtils.EMPTY;
		// 存摺備註_貸方
		private String PSBREM_S_C = StringUtils.EMPTY;
		// 往來明細_借方
		private String PSBREM_F_D = StringUtils.EMPTY;
		// 往來明細_貸方
		private String PSBREM_F_C = StringUtils.EMPTY;		
		// 使用者登入IP
		private String IPADDR = StringUtils.EMPTY;

		/**
		 * 銀行別
		 */
		public String getBKNO() {
			return BKNO;
		}

		public void setBKNO(String value) {
			this.BKNO = value;
		}

		/**
		 * 交易帳號
		 */
		public String getTXACT() {
			return TXACT;
		}

		public void setTXACT(String value) {
			this.TXACT = value;
		}

		/**
		 * 交易金額
		 */
		public String getTXAMT() {
			return TXAMT;
		}

		public void setTXAMT(String value) {
			this.TXAMT = value;
		}

		/**
		 * 繳款種類
		 */
		public String getCLASS() {
			return CLASS;
		}

		public void setCLASS(String value) {
			this.CLASS = value;
		}	
		
		/**
		 * 銷帳編號
		 */
		public String getPAYCNO() {
			return PAYCNO;
		}

		public void setPAYCNO(String value) {
			this.PAYCNO = value;
		}	
		
		/**
		 * 繳款到期日
		 */
		public String getDUEDATE() {
			return DUEDATE;
		}

		public void setDUEDATE(String value) {
			this.DUEDATE = value;
		}	
		
		/**
		 * 稽徵機關別
		 */
		public String getUNIT() {
			return UNIT;
		}

		public void setUNIT(String value) {
			this.UNIT = value;
		}	
		
		/**
		 * 身分證字號
		 */
		public String getIDNO() {
			return IDNO;
		}

		public void setIDNO(String value) {
			this.IDNO = value;
		}	
		
		/**
		 * 帳務別
		 * 
		 * <remark>1:本日帳(3:30之前,財金未換日) 2:次日帳(3:30之後,財金已換日) FEP 會檢核與財金換日註記是否符合, 不合則回應錯誤 </remark>
		 */
		public String getMODE() {
			return MODE;
		}

		public void setMODE(String value) {
			this.MODE = value;
		}

		/**
		 * 存摺摘要_借方
		 */
		public String getPsbmemoD() {
			return PSBMEMO_D;
		}

		public void setPsbmemoD(String value) {
			this.PSBMEMO_D = value;
		}

		/**
		 * 存摺摘要_貸方
		 */
		public String getPsbmemoC() {
			return PSBMEMO_C;
		}

		public void setPsbmemoC(String value) {
			this.PSBMEMO_C = value;
		}

		/**
		 * 存摺備註_借方
		 */
		public String getPsbremSD() {
			return PSBREM_S_D;
		}

		public void setPsbremSD(String value) {
			this.PSBREM_S_D = value;
		}

		/**
		 * 存摺備註_貸方
		 */
		public String getPsbremSC() {
			return PSBREM_S_C;
		}

		public void setPsbremSC(String value) {
			this.PSBREM_S_C = value;
		}

		/**
		 * 往來明細_借方
		 */
		public String getPsbremFD() {
			return PSBREM_F_D;
		}

		public void setPsbremFD(String value) {
			this.PSBREM_F_D = value;
		}

		/**
		 * 往來明細_貸方
		 */
		public String getPsbremFC() {
			return PSBREM_F_C;
		}

		public void setPsbremFC(String value) {
			this.PSBREM_F_C = value;
		}

		/**
		 * 使用者登入IP
		 */
		public String getIPADDR() {
			return IPADDR;
		}

		public void setIPADDR(String value) {
			this.IPADDR = value;
		}
	}
}
