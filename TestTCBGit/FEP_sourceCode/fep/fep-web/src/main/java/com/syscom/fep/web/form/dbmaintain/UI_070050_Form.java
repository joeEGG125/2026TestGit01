package com.syscom.fep.web.form.dbmaintain;

import java.math.BigDecimal;

import com.syscom.fep.web.form.BaseForm;
import com.syscom.fep.web.form.inbk.UI_019040_Form;

public class UI_070050_Form extends BaseForm {
	private static final long serialVersionUID = 1L;
	private String cbspendZone;
	private String cbspendTxDate;
	private Short cbspendSuccessFlag;
	private Short cbspendSubsys;
	private String cbspendCbsTxCode;
	private String cbspendTxTime;
	private String cbspendTbsdyFisc;
	private Integer cbspendEjfno;
	private String cbspendActno;
	private BigDecimal cbspendTxAmt;
    private Short cbspendResendCnt;
    private String cbspendCbsRc;
    private String cbspendIbBkno;
    private String cbspendIbActno;
    private Short cbspendAccType;
    private String cbspendPcode;
    private String cbspendCbsRrn;
    private String cbspendTbsdy;
    private Long totalCNT;
    private BigDecimal totalAMT;
    private BigDecimal sumOfCbspendTxAmt;
    private Boolean btntmp;
    private Boolean txtt;
    
	public String getCbspendZone() {
		return cbspendZone;
	}
	public void setCbspendZone(String cbspendZone) {
		this.cbspendZone = cbspendZone;
	}
	public String getCbspendTxDate() {
		return cbspendTxDate;
	}
	public void setCbspendTxDate(String cbspendTxDate) {
		this.cbspendTxDate = cbspendTxDate;
	}
	public Short getCbspendSubsys() {
		return cbspendSubsys;
	}
	public void setCbspendSubsys(Short cbspendSubsys) {
		this.cbspendSubsys = cbspendSubsys;
	}
	public Short getCbspendSuccessFlag() {
		return cbspendSuccessFlag;
	}
	public void setCbspendSuccessFlag(Short cbspendSuccessFlag) {
		this.cbspendSuccessFlag = cbspendSuccessFlag;
	}
	public String getCbspendCbsTxCode() {
		return cbspendCbsTxCode;
	}
	public void setCbspendCbsTxCode(String cbspendCbsTxCode) {
		this.cbspendCbsTxCode = cbspendCbsTxCode;
	}
	public String getCbspendTxTime() {
		return cbspendTxTime;
	}
	public void setCbspendTxTime(String cbspendTxTime) {
		this.cbspendTxTime = cbspendTxTime;
	}
	public String getCbspendTbsdyFisc() {
		return cbspendTbsdyFisc;
	}
	public void setCbspendTbsdyFisc(String cbspendTbsdyFisc) {
		this.cbspendTbsdyFisc = cbspendTbsdyFisc;
	}
	
	public String getCbspendActno() {
		return cbspendActno;
	}
	public void setCbspendActno(String cbspendActno) {
		this.cbspendActno = cbspendActno;
	}
	public BigDecimal getCbspendTxAmt() {
		return cbspendTxAmt;
	}
	public void setCbspendTxAmt(BigDecimal cbspendTxAmt) {
		this.cbspendTxAmt = cbspendTxAmt;
	}
	public Short getCbspendResendCnt() {
		return cbspendResendCnt;
	}
	public void setCbspendResendCnt(Short cbspendResendCnt) {
		this.cbspendResendCnt = cbspendResendCnt;
	}
	public String getCbspendCbsRc() {
		return cbspendCbsRc;
	}
	public void setCbspendCbsRc(String cbspendCbsRc) {
		this.cbspendCbsRc = cbspendCbsRc;
	}
	public String getCbspendIbBkno() {
		return cbspendIbBkno;
	}
	public void setCbspendIbBkno(String cbspendIbBkno) {
		this.cbspendIbBkno = cbspendIbBkno;
	}
	public String getCbspendIbActno() {
		return cbspendIbActno;
	}
	public void setCbspendIbActno(String cbspendIbActno) {
		this.cbspendIbActno = cbspendIbActno;
	}
	public Short getCbspendAccType() {
		return cbspendAccType;
	}
	public void setCbspendAccType(Short cbspendAccType) {
		this.cbspendAccType = cbspendAccType;
	}
	public String getCbspendPcode() {
		return cbspendPcode;
	}
	public void setCbspendPcode(String cbspendPcode) {
		this.cbspendPcode = cbspendPcode;
	}
	public String getCbspendCbsRrn() {
		return cbspendCbsRrn;
	}
	public void setCbspendCbsRrn(String cbspendCbsRrn) {
		this.cbspendCbsRrn = cbspendCbsRrn;
	}
	public String getCbspendTbsdy() {
		return cbspendTbsdy;
	}
	public void setCbspendTbsdy(String cbspendTbsdy) {
		this.cbspendTbsdy = cbspendTbsdy;
	}
	public BigDecimal getTotalAMT() {
		return totalAMT;
	}
	public void setTotalAMT(BigDecimal totalAMT) {
		this.totalAMT = totalAMT;
	}
	public BigDecimal getSumOfCbspendTxAmt() {
		return sumOfCbspendTxAmt;
	}
	public void setSumOfCbspendTxAmt(BigDecimal sumOfCbspendTxAmt) {
		this.sumOfCbspendTxAmt = sumOfCbspendTxAmt;
	}
	public Long getTotalCNT() {
		return totalCNT;
	}
	public void setTotalCNT(Long totalCNT) {
		this.totalCNT = totalCNT;
	}
	public Boolean getBtntmp() {
		return btntmp;
	}
	public void setBtntmp(Boolean btntmp) {
		this.btntmp = btntmp;
	}
	public Boolean getTxtt() {
		return txtt;
	}
	public void setTxtt(Boolean txtt) {
		this.txtt = txtt;
	}
	public Integer getCbspendEjfno() {
		return cbspendEjfno;
	}
	public void setCbspendEjfno(Integer cbspendEjfno) {
		this.cbspendEjfno = cbspendEjfno;
	}


}
