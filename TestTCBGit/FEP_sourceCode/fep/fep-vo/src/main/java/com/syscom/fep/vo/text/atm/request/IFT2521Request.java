package com.syscom.fep.vo.text.atm.request;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class IFT2521Request extends ATMXmlBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private IFT2521SvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public IFT2521SvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(IFT2521SvcRq svcRq) {
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
		request.setSignID(this.rqHeader.getSignID());
		request.setBKNO(this.svcRq.getRq().getBKNO()); // 銀行別
		request.setTXACT(this.svcRq.getRq().getTXACT()); // 交易帳號

		request.setBknoD(this.svcRq.getRq().getBknoD()); // 轉入銀行別
		request.setActD(this.svcRq.getRq().getActD()); // 轉入帳號

		try {
			request.setTXAMT(new BigDecimal(this.svcRq.getRq().getTXAMT())); // 交易金額
		} catch (NumberFormatException e) {
		}
		request.setMODE(this.svcRq.getRq().getMODE());// 帳務別
		request.setPsbmemoD(this.svcRq.getRq().getPsbmemoD()); // 存摺摘要_借方
		request.setPsbmemoC(this.svcRq.getRq().getPsbmemoC()); // 存摺摘要_貸方
		request.setPsbremSD(this.svcRq.getRq().getPsbremSD()); // 存摺備註_借方
		request.setPsbremSC(this.svcRq.getRq().getPsbremSC()); // 存摺備註_貸方
		request.setPsbremFD(this.svcRq.getRq().getPsbremFD()); // 往來明細_借方
		request.setPsbremFC(this.svcRq.getRq().getPsbremFC()); // 往來明細_貸方
		request.setRegTfrType(this.svcRq.getRq().getRegTfrType());// 約定轉帳記號
		request.setIPADDR(this.svcRq.getRq().getIPADDR()); // 使用者登入IP
		request.setCHREM(this.svcRq.getRq().getCHREM()); // 中文備註
		request.setMTP(this.svcRq.getRq().getMTP()); // 手機門號轉帳記號
		*/
	}

	@XStreamAlias("SvcRq")
	public static class IFT2521SvcRq {
		@XStreamAlias("Rq")
		private IFT2521Rq rq;

		public IFT2521Rq getRq() {
			return rq;
		}

		public void setRq(IFT2521Rq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class IFT2521Rq {
		// 銀行別
		private String BKNO = StringUtils.EMPTY;
		// 交易帳號
		private String TXACT = StringUtils.EMPTY;
		// 轉入銀行別
		private String BKNO_D = StringUtils.EMPTY;
		// 轉入帳號
		private String ACT_D = StringUtils.EMPTY;
		// 交易金額
		private String TXAMT = StringUtils.EMPTY;
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
		// 約定轉帳記號
		private String REG_TFR_TYPE = StringUtils.EMPTY;
		// 使用者登入IP
		private String IPADDR = StringUtils.EMPTY;
		// 中文備註
		private String CHREM = StringUtils.EMPTY;
		// 手機門號轉帳記號
		private String MTP = StringUtils.EMPTY;

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
		 * 轉入銀行別
		 */
		public String getBknoD() {
			return BKNO_D;
		}

		public void setBknoD(String value) {
			this.BKNO_D = value;
		}

		/**
		 * 轉入帳號
		 */
		public String getActD() {
			return ACT_D;
		}

		public void setActD(String value) {
			this.ACT_D = value;
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
		 * 約定轉帳記號
		 * 
		 * <remark>NN:不檢核約定帳號,不維護限額 NR:不檢核約定帳號,維護約定限額 NS:不檢核約定帳號,檢核全國性繳費約定 NW:不檢核約定帳號,維護現金提取限額 CR :限定約定帳號才可做交易</remark>
		 */
		public String getRegTfrType() {
			return REG_TFR_TYPE;
		}

		public void setRegTfrType(String value) {
			this.REG_TFR_TYPE = value;
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

		/**
		 * 中文備註
		 */
		public String getCHREM() {
			return CHREM;
		}

		public void setCHREM(String value) {
			this.CHREM = value;
		}

		/**
		 * 手機門號轉帳記號
		 */
		public String getMTP() {
			return MTP;
		}

		public void setMTP(String value) {
			this.MTP = value;
		}
	}
}
