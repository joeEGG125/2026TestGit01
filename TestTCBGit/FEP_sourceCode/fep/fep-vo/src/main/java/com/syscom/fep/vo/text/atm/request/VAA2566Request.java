package com.syscom.fep.vo.text.atm.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMGeneralRequest;
import com.syscom.fep.vo.text.atm.ATMXmlBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class VAA2566Request extends ATMXmlBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private VAA2566SvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public VAA2566SvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(VAA2566SvcRq svcRq) {
		this.svcRq = svcRq;
	}

	@Override
	public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
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
		request.setMODE(this.svcRq.getRq().getMODE()); // 帳務別
		request.setVATYPE(this.svcRq.getRq().getVATYPE()); // 交易類別
		request.setVAITEM(this.svcRq.getRq().getVAITEM()); // 核驗項目
		request.setIDNO(this.svcRq.getRq().getIDNO()); // 身份證號
		request.setMOBILE(this.svcRq.getRq().getMOBILE()); // 行動電話號碼
		request.setBIRTHDAY(this.svcRq.getRq().getBIRTHDAY()); // 出生年月日
		request.setHPHONE(this.svcRq.getRq().getHPHONE()); // 住家電話號碼
		request.setBRANCH(this.svcRq.getRq().getBRANCH()); // 帳戶績效行
		request.setDEPT(this.svcRq.getRq().getDEPT()); // 績效單位
		*/
	}

	@XStreamAlias("SvcRq")
	public static class VAA2566SvcRq {
		@XStreamAlias("Rq")
		private VAA2566Rq rq;

		public VAA2566Rq getRq() {
			return this.rq;
		}

		public void setRq(VAA2566Rq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class VAA2566Rq {
		// 銀行別
		private String BKNO = StringUtils.EMPTY;
		// 交易帳號
		private String TXACT = StringUtils.EMPTY;
		// 帳務別
		private String MODE = StringUtils.EMPTY;
		// 交易類別
		private String VATYPE = StringUtils.EMPTY;
		// 核驗項目
		private String VAITEM = StringUtils.EMPTY;
		// 身份證號
		private String IDNO = StringUtils.EMPTY;
		// 行動電話號碼
		private String MOBILE = StringUtils.EMPTY;
		// 出生年月日
		private String BIRTHDAY = StringUtils.EMPTY;
		// 住家電話號碼
		private String HPHONE = StringUtils.EMPTY;
		// 帳戶績效行
		private String BRANCH = StringUtils.EMPTY;
		// 績效單位
		private String DEPT = StringUtils.EMPTY;

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
		 * 帳務別
		 * 
		 * <remark>1:本日帳(3:30之前,財金未換日) 2:次日帳(3:30之後,財金已換日) FEP 會檢核與財金換日註記是否符合, 不合則回應錯誤 </remark>
		 */
		public String getMODE() {
			return this.MODE;
		}

		public void setMODE(String value) {
			this.MODE = value;
		}

		/**
		 * 交易類別
		 * 
		 * <remark>00:晶片金融卡核驗 01:台幣無卡核驗 02:外幣無卡核驗 放'01'</remark>
		 */
		public String getVATYPE() {
			return this.VATYPE;
		}

		public void setVATYPE(String value) {
			this.VATYPE = value;
		}

		/**
		 * 核驗項目
		 */
		public String getVAITEM() {
			return this.VAITEM;
		}

		public void setVAITEM(String value) {
			this.VAITEM = value;
		}

		/**
		 * 身份證號
		 */
		public String getIDNO() {
			return this.IDNO;
		}

		public void setIDNO(String value) {
			this.IDNO = value;
		}

		/**
		 * 行動電話號碼
		 */
		public String getMOBILE() {
			return this.MOBILE;
		}

		public void setMOBILE(String value) {
			this.MOBILE = value;
		}

		/**
		 * 出生年月日
		 */
		public String getBIRTHDAY() {
			return this.BIRTHDAY;
		}

		public void setBIRTHDAY(String value) {
			this.BIRTHDAY = value;
		}

		/**
		 * 住家電話號碼
		 */
		public String getHPHONE() {
			return this.HPHONE;
		}

		public void setHPHONE(String value) {
			this.HPHONE = value;
		}

		/**
		 * 帳戶績效行
		 * 
		 * <remark>數位帳戶申請的績效行,請勿放虛擬分行</remark>
		 */
		public String getBRANCH() {
			return this.BRANCH;
		}

		public void setBRANCH(String value) {
			this.BRANCH = value;
		}

		/**
		 * 績效單位
		 */
		public String getDEPT() {
			return this.DEPT;
		}

		public void setDEPT(String value) {
			this.DEPT = value;
		}
	}
}
