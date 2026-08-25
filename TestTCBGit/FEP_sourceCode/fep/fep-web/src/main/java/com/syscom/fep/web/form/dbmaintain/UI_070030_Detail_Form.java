package com.syscom.fep.web.form.dbmaintain;

import java.math.BigDecimal;

import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.form.inbk.UI_019040_Form;

public class UI_070030_Detail_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String INBKPARM_PCODE;
	private String INBKPARM_APID;
    private String INBKPARM_ACQ_FLAG;
    private String INBKPARM_CUR;
    private String INBKPARM_EFFECT_DATE;
    private BigDecimal INBKPARM_RANGE_FROM;
    private BigDecimal INBKPARM_RANGE_TO;
    private Short INBKPARM_FEE_TYPE;
    private BigDecimal INBKPARM_FEE_MBR_DR;
    private BigDecimal INBKPARM_FEE_MBR_CR;
    private BigDecimal INBKPARM_FEE_ASS_DR;
    private BigDecimal INBKPARM_FEE_ASS_CR;
    private BigDecimal INBKPARM_FEE_CUSTPAY;
    private String INBKPARM_PRNCRDB;
    private BigDecimal INBKPARM_FEE_MIN;
    private String btnType;


	public String getINBKPARM_PCODE() {
		return INBKPARM_PCODE;
	}


	public void setINBKPARM_PCODE(String iNBKPARM_PCODE) {
		INBKPARM_PCODE = iNBKPARM_PCODE;
	}


	public String getINBKPARM_APID() {
		return INBKPARM_APID;
	}


	public void setINBKPARM_APID(String iNBKPARM_APID) {
		INBKPARM_APID = iNBKPARM_APID;
	}

	public String getINBKPARM_CUR() {
		return INBKPARM_CUR;
	}


	public void setINBKPARM_CUR(String iNBKPARM_CUR) {
		INBKPARM_CUR = iNBKPARM_CUR;
	}


	public String getINBKPARM_EFFECT_DATE() {
		return INBKPARM_EFFECT_DATE;
	}


	public void setINBKPARM_EFFECT_DATE(String iNBKPARM_EFFECT_DATE) {
		INBKPARM_EFFECT_DATE = iNBKPARM_EFFECT_DATE;
	}


	public String getINBKPARM_ACQ_FLAG() {
		return INBKPARM_ACQ_FLAG;
	}


	public void setINBKPARM_ACQ_FLAG(String iNBKPARM_ACQ_FLAG) {
		INBKPARM_ACQ_FLAG = iNBKPARM_ACQ_FLAG;
	}


	public String getINBKPARM_PRNCRDB() {
		return INBKPARM_PRNCRDB;
	}


	public void setINBKPARM_PRNCRDB(String iNBKPARM_PRNCRDB) {
		INBKPARM_PRNCRDB = iNBKPARM_PRNCRDB;
	}


	public String getBtnType() {
		return btnType;
	}


	public void setBtnType(String btnType) {
		this.btnType = btnType;
	}


	public BigDecimal getINBKPARM_RANGE_FROM() {
		return INBKPARM_RANGE_FROM;
	}


	public void setINBKPARM_RANGE_FROM(BigDecimal iNBKPARM_RANGE_FROM) {
		INBKPARM_RANGE_FROM = iNBKPARM_RANGE_FROM;
	}


	public Short getINBKPARM_FEE_TYPE() {
		return INBKPARM_FEE_TYPE;
	}


	public void setINBKPARM_FEE_TYPE(Short iNBKPARM_FEE_TYPE) {
		INBKPARM_FEE_TYPE = iNBKPARM_FEE_TYPE;
	}


	public BigDecimal getINBKPARM_RANGE_TO() {
		return INBKPARM_RANGE_TO;
	}


	public void setINBKPARM_RANGE_TO(BigDecimal iNBKPARM_RANGE_TO) {
		INBKPARM_RANGE_TO = iNBKPARM_RANGE_TO;
	}


	public BigDecimal getINBKPARM_FEE_MBR_DR() {
		return INBKPARM_FEE_MBR_DR;
	}


	public void setINBKPARM_FEE_MBR_DR(BigDecimal iNBKPARM_FEE_MBR_DR) {
		INBKPARM_FEE_MBR_DR = iNBKPARM_FEE_MBR_DR;
	}


	public BigDecimal getINBKPARM_FEE_MBR_CR() {
		return INBKPARM_FEE_MBR_CR;
	}


	public void setINBKPARM_FEE_MBR_CR(BigDecimal iNBKPARM_FEE_MBR_CR) {
		INBKPARM_FEE_MBR_CR = iNBKPARM_FEE_MBR_CR;
	}


	public BigDecimal getINBKPARM_FEE_ASS_DR() {
		return INBKPARM_FEE_ASS_DR;
	}


	public void setINBKPARM_FEE_ASS_DR(BigDecimal iNBKPARM_FEE_ASS_DR) {
		INBKPARM_FEE_ASS_DR = iNBKPARM_FEE_ASS_DR;
	}


	public BigDecimal getINBKPARM_FEE_ASS_CR() {
		return INBKPARM_FEE_ASS_CR;
	}


	public void setINBKPARM_FEE_ASS_CR(BigDecimal iNBKPARM_FEE_ASS_CR) {
		INBKPARM_FEE_ASS_CR = iNBKPARM_FEE_ASS_CR;
	}


	public BigDecimal getINBKPARM_FEE_CUSTPAY() {
		return INBKPARM_FEE_CUSTPAY;
	}


	public void setINBKPARM_FEE_CUSTPAY(BigDecimal iNBKPARM_FEE_CUSTPAY) {
		INBKPARM_FEE_CUSTPAY = iNBKPARM_FEE_CUSTPAY;
	}


	public BigDecimal getINBKPARM_FEE_MIN() {
		return INBKPARM_FEE_MIN;
	}


	public void setINBKPARM_FEE_MIN(BigDecimal iNBKPARM_FEE_MIN) {
		INBKPARM_FEE_MIN = iNBKPARM_FEE_MIN;
	}



}
