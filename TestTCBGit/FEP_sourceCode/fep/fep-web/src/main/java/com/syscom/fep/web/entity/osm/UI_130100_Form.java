package com.syscom.fep.web.entity.osm;

import java.math.BigDecimal;

import com.syscom.fep.web.form.BaseForm;

public class UI_130100_Form extends BaseForm {
	
	private static final long serialVersionUID = 1L;

    private String tbxTX_MM;
    private String ddlSEQ_NO;
    private boolean check_INTRA;
    private boolean check_OUT;
    
    private String atmfeeTxMm;
    private String atmfeeSeqNo;
    private String atmfeeName;
    private String atmfeeCur;
    private BigDecimal atmfeeFee;
    private String atmfeeFiscFlag;
    private String atmfeePcode;
    private String btnType;
    
    
	public String getTbxTX_MM() {
		return tbxTX_MM;
	}
	public void setTbxTX_MM(String tbxTX_MM) {
		this.tbxTX_MM = tbxTX_MM;
	}
	public String getDdlSEQ_NO() {
		return ddlSEQ_NO;
	}
	public void setDdlSEQ_NO(String ddlSEQ_NO) {
		this.ddlSEQ_NO = ddlSEQ_NO;
	}
	public boolean isCheck_INTRA() {
		return check_INTRA;
	}
	public void setCheck_INTRA(boolean check_INTRA) {
		this.check_INTRA = check_INTRA;
	}
	public boolean isCheck_OUT() {
		return check_OUT;
	}
	public void setCheck_OUT(boolean check_OUT) {
		this.check_OUT = check_OUT;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getAtmfeeTxMm() {
		return atmfeeTxMm;
	}
	public void setAtmfeeTxMm(String atmfeeTxMm) {
		this.atmfeeTxMm = atmfeeTxMm;
	}
	public String getAtmfeeSeqNo() {
		return atmfeeSeqNo;
	}
	public void setAtmfeeSeqNo(String atmfeeSeqNo) {
		this.atmfeeSeqNo = atmfeeSeqNo;
	}
	public String getAtmfeeName() {
		return atmfeeName;
	}
	public void setAtmfeeName(String atmfeeName) {
		this.atmfeeName = atmfeeName;
	}
	public String getAtmfeeCur() {
		return atmfeeCur;
	}
	public void setAtmfeeCur(String atmfeeCur) {
		this.atmfeeCur = atmfeeCur;
	}
	public BigDecimal getAtmfeeFee() {
		return atmfeeFee;
	}
	public void setAtmfeeFee(BigDecimal atmfeeFee) {
		this.atmfeeFee = atmfeeFee;
	}
	public String getAtmfeeFiscFlag() {
		return atmfeeFiscFlag;
	}
	public void setAtmfeeFiscFlag(String atmfeeFiscFlag) {
		this.atmfeeFiscFlag = atmfeeFiscFlag;
	}
	public String getAtmfeePcode() {
		return atmfeePcode;
	}
	public void setAtmfeePcode(String atmfeePcode) {
		this.atmfeePcode = atmfeePcode;
	}
	public String getBtnType() {
		return btnType;
	}
	public void setBtnType(String btnType) {
		this.btnType = btnType;
	}
    
    
}
