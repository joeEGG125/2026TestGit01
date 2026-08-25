package com.syscom.fep.vo.text.t24;

public class T24TITAHeader {
	//交易來源系統代號(TI_CHNN_CODE_S)
		private String _TI_CHNN_CODE_S;
		//處理系統代號(TI_CHNN_CODE)
		private String _TI_CHNN_CODE;
		//工作站帳務分行(TRM_BRANCH)
		private String _TRM_BRANCH;
		//工作站代號(TRMNO)
		private String _TRMNO;
		//FEP電子日誌序號(EJFNO)
		private String _EJFNO;
		//FEP營業日期(FISC_DATE)
		private String _FISC_DATE;
		//FEP交易日期(VALUE_DATE)
		//Private _VALUE_DATE As String
		//交易摘要(FEP_TXN_CODE)
		private String _FEP_TXN_CODE;
		//預約記號(REG_FLAG)
		private String _REG_FLAG;
		//FEP使用者代號(FEP_USER_ID)
		private String _FEP_USER_ID;
		private String _FEP_SSCODE;

		public final String getTiChnnCodeS()
		{
			return _TI_CHNN_CODE_S;
		}
		public final void setTiChnnCodeS(String value)
		{
			_TI_CHNN_CODE_S = value;
		}

		public final String getTiChnnCode()
		{
			return _TI_CHNN_CODE;
		}
		public final void setTiChnnCode(String value)
		{
			_TI_CHNN_CODE = value;
		}

		public final String getTrmBranch()
		{
			return _TRM_BRANCH;
		}
		public final void setTrmBranch(String value)
		{
			_TRM_BRANCH = value;
		}

		public final String getTRMNO()
		{
			return _TRMNO;
		}
		public final void setTRMNO(String value)
		{
			_TRMNO = value;
		}

		public final String getEJFNO()
		{
			return _EJFNO;
		}
		public final void setEJFNO(String value)
		{
			_EJFNO = value;
		}

		public final String getFiscDate()
		{
			return _FISC_DATE;
		}
		public final void setFiscDate(String value)
		{
			_FISC_DATE = value;
		}

		public final String getFeptxnCode()
		{
			return _FEP_TXN_CODE;
		}
		public final void setFeptxnCode(String value)
		{
			_FEP_TXN_CODE = value;
		}

		public final String getRegFlag()
		{
			return _REG_FLAG;
		}
		public final void setRegFlag(String value)
		{
			_REG_FLAG = value;
		}

		public final String getFepUserId()
		{
			return _FEP_USER_ID;
		}
		public final void setFepUserId(String value)
		{
			_FEP_USER_ID = value;
		}

		public final String getFepPassword()
		{
			return _FEP_SSCODE;
		}
		public final void setFepPassword(String value)
		{
			_FEP_SSCODE = value;
		}

}
