package com.syscom.fep.vo.text.nb.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.NBGeneralRequest;
import com.syscom.fep.vo.text.nb.NBTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NETBANK")
public class ANBRequest extends NBTextBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private ANBSvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeaderField) {
		this.rqHeader = rqHeaderField;
	}

	public ANBSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(ANBSvcRq svcRqField) {
		this.svcRq = svcRqField;
	}

	@Override
	public String makeMessageFromGeneral(NBGeneral general) {
		NBGeneralRequest request = general.getRequest();
		this.getRqHeader().setChlName(request.getChlName());
		this.getRqHeader().setChlEJNo(request.getChlEJNo());
		this.getRqHeader().setBranchID(request.getBranchID());
		this.getRqHeader().setChlSendTime(request.getChlSendTime());
		this.getRqHeader().setMsgID(request.getMsgID());
		this.getRqHeader().setMsgType(request.getMsgType());
		this.getRqHeader().setTermID(request.getTermID());
		this.getRqHeader().setTxnID(request.getTxnID());
		this.getRqHeader().setUserID(request.getUserID());
		this.getRqHeader().setSignID(request.getSignID());	
		this.getSvcRq().getRq().setCustId(request.getCUSTID()); 
		this.getSvcRq().getRq().setCardNo(request.getCARDNO()); 
		return this.serializeToXml(this);
	}

	@Override
	public void toGeneral(NBGeneral general) throws Exception {
		// TODO 自動生成的方法存根
	}

	@XStreamAlias("SvcRq")
	public static class ANBSvcRq {
		@XStreamAlias("Rq")
		private ANBRq rq;

		public ANBRq getRq() {
			return this.rq;
		}

		public void setRq(ANBRq value) {
			this.rq = value;
		}
	}

	@XStreamAlias("Rq")
	public static class ANBRq {

		private String custId = StringUtils.EMPTY;

		private String cardNo = StringUtils.EMPTY;
	

		public String getCustId() {
			return custId;
		}

		public void setCustId(String value) {
			custId = value;
		}

		public String getCardNo() {
			return cardNo;
		}

		public void setCardNo(String value) {
			cardNo = value;
		}
	}
}
