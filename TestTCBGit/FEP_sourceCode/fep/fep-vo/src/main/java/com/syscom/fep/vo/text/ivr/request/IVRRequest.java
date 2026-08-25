package com.syscom.fep.vo.text.ivr.request;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.ivr.IVRGeneral;
import com.syscom.fep.vo.text.ivr.IVRTextBase;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class IVRRequest extends IVRTextBase {
	@XStreamAlias("RqHeader")
	private FEPRqHeader rqHeader;
	@XStreamAlias("SvcRq")
	private IVRREQSvcRq svcRq;

	public FEPRqHeader getRqHeader() {
		return rqHeader;
	}

	public void setRqHeader(FEPRqHeader rqHeader) {
		this.rqHeader = rqHeader;
	}

	public IVRREQSvcRq getSvcRq() {
		return svcRq;
	}

	public void setSvcRq(IVRREQSvcRq svcRq) {
		this.svcRq = svcRq;
	}

	@Override
	public String makeMessageFromGeneral(IVRGeneral general) throws Exception {
		return null;
	}

	@Override
	public void toGeneral(IVRGeneral general) throws Exception {}

	@XStreamAlias("SvcRq")
	public static class IVRREQSvcRq {
		@XStreamAlias("Rq")
		private IVRREQRq rq;

		public IVRREQRq getRq() {
			return rq;
		}

		public void setRq(IVRREQRq rq) {
			this.rq = rq;
		}
	}

	@XStreamAlias("Rq")
	public static class IVRREQRq {
		// 驗密方式
		private String Dependency = StringUtils.EMPTY;
		// 身份證號/統編,含檢查碼
		private String IDNo = StringUtils.EMPTY;
		// 帳號
		private String AccountNo = StringUtils.EMPTY;
		// 語音密碼
		private String PINBlock = StringUtils.EMPTY;
		// MAC value
		private String MAC = StringUtils.EMPTY;

		/**
		 * 驗密方式
		 * 
		 * <remark>1:驗密BY ID 2:驗密BY A/C</remark>
		 */
		public String getDependency() {
			return Dependency;
		}

		public void setDependency(String value) {
			Dependency = value;
		}

		/**
		 * 身份證號/統編,含檢查碼
		 * 
		 * <remark>[optional]</remark>
		 */
		public String getIDNo() {
			return IDNo;
		}

		public void setIDNo(String value) {
			IDNo = value;
		}

		/**
		 * 帳號
		 * 
		 * <remark>[optional]</remark>
		 */
		public String getAccountNo() {
			return AccountNo;
		}

		public void setAccountNo(String value) {
			AccountNo = value;
		}

		/**
		 * 語音密碼
		 * 
		 * <remark></remark>
		 */
		public String getPINBlock() {
			return PINBlock;
		}

		public void setPINBlock(String value) {
			PINBlock = value;
		}

		/**
		 * MAC value
		 * 
		 * <remark></remark>
		 */
		public String getMAC() {
			return MAC;
		}

		public void setMAC(String value) {
			MAC = value;
		}
	}
}
