package com.syscom.fep.web.form.inbk;


import com.syscom.fep.mybatis.model.Obtltxn;
import com.syscom.fep.web.form.BaseForm;

public class UI_019070_FormMain extends BaseForm {
	
	private static final long serialVersionUID = 1L;

	private Obtltxn obtltxn;
	private String dttxDATE;
	private String dttxDATEe;
	private String txtTxAMT;
	private String txtOrderNO;
	private String txtMerchantId;
	private String txtTroutBkno;
	private String txtTroutActno;
	private String txtBkno;
	private String txtStan;
	
	private String obtltxnTxDate;
	private String obtltxnTxTime;
	private String obtltxnPcode;
	private String obtltxnTxrust;
	private String obtltxnOrderNo; 
	private String obtltxnMerchantId;
    private String obtltxnTotTwdAmt;
    private String obtltxnTotTwdFee;
    private String obtltxnTotForAmt;
    private String obtltxnTotForFee;
    private String obtltxnSetAmt;
    private String obtltxnTwnFee;
    private String obtltxnExrate;
    private String obtltxnEjfno;
    private String obtltxnStan;
    private String obtltxnOriStan;
    
    
	public String getDttxDATE() {
		return dttxDATE;
	}
	public void setDttxDATE(String dttxDATE) {
		this.dttxDATE = dttxDATE;
	}
	public String getDttxDATEe() {
		return dttxDATEe;
	}
	public void setDttxDATEe(String dttxDATEe) {
		this.dttxDATEe = dttxDATEe;
	}
	public String getTxtTxAMT() {
		return txtTxAMT;
	}
	public void setTxtTxAMT(String txtTxAMT) {
		this.txtTxAMT = txtTxAMT;
	}
	public String getTxtOrderNO() {
		return txtOrderNO;
	}
	public void setTxtOrderNO(String txtOrderNO) {
		this.txtOrderNO = txtOrderNO;
	}
	public String getTxtMerchantId() {
		return txtMerchantId;
	}
	public void setTxtMerchantId(String txtMerchantId) {
		this.txtMerchantId = txtMerchantId;
	}
	public String getTxtTroutBkno() {
		return txtTroutBkno;
	}
	public void setTxtTroutBkno(String txtTroutBkno) {
		this.txtTroutBkno = txtTroutBkno;
	}
	public String getTxtTroutActno() {
		return txtTroutActno;
	}
	public void setTxtTroutActno(String txtTroutActno) {
		this.txtTroutActno = txtTroutActno;
	}
	public String getTxtBkno() {
		return txtBkno;
	}
	public void setTxtBkno(String txtBkno) {
		this.txtBkno = txtBkno;
	}
	public String getTxtStan() {
		return txtStan;
	}
	public void setTxtStan(String txtStan) {
		this.txtStan = txtStan;
	}
	public String getObtltxnTxDate() {
		return obtltxnTxDate;
	}
	public void setObtltxnTxDate(String obtltxnTxDate) {
		this.obtltxnTxDate = obtltxnTxDate;
	}
	public String getObtltxnTxTime() {
		return obtltxnTxTime;
	}
	public void setObtltxnTxTime(String obtltxnTxTime) {
		this.obtltxnTxTime = obtltxnTxTime;
	}
	public String getObtltxnPcode() {
		return obtltxnPcode;
	}
	public void setObtltxnPcode(String obtltxnPcode) {
		this.obtltxnPcode = obtltxnPcode;
	}
	public String getObtltxnTxrust() {
		return obtltxnTxrust;
	}
	public void setObtltxnTxrust(String obtltxnTxrust) {
		this.obtltxnTxrust = obtltxnTxrust;
	}
	public String getObtltxnOrderNo() {
		return obtltxnOrderNo;
	}
	public void setObtltxnOrderNo(String obtltxnOrderNo) {
		this.obtltxnOrderNo = obtltxnOrderNo;
	}
	public String getObtltxnMerchantId() {
		return obtltxnMerchantId;
	}
	public void setObtltxnMerchantId(String obtltxnMerchantId) {
		this.obtltxnMerchantId = obtltxnMerchantId;
	}
	public String getObtltxnTotTwdAmt() {
		return obtltxnTotTwdAmt;
	}
	public void setObtltxnTotTwdAmt(String obtltxnTotTwdAmt) {
		this.obtltxnTotTwdAmt = obtltxnTotTwdAmt;
	}
	public String getObtltxnTotTwdFee() {
		return obtltxnTotTwdFee;
	}
	public void setObtltxnTotTwdFee(String obtltxnTotTwdFee) {
		this.obtltxnTotTwdFee = obtltxnTotTwdFee;
	}
	public String getObtltxnTotForAmt() {
		return obtltxnTotForAmt;
	}
	public void setObtltxnTotForAmt(String obtltxnTotForAmt) {
		this.obtltxnTotForAmt = obtltxnTotForAmt;
	}
	public String getObtltxnTotForFee() {
		return obtltxnTotForFee;
	}
	public void setObtltxnTotForFee(String obtltxnTotForFee) {
		this.obtltxnTotForFee = obtltxnTotForFee;
	}
	public String getObtltxnSetAmt() {
		return obtltxnSetAmt;
	}
	public void setObtltxnSetAmt(String obtltxnSetAmt) {
		this.obtltxnSetAmt = obtltxnSetAmt;
	}
	public String getObtltxnTwnFee() {
		return obtltxnTwnFee;
	}
	public void setObtltxnTwnFee(String obtltxnTwnFee) {
		this.obtltxnTwnFee = obtltxnTwnFee;
	}
	public String getObtltxnExrate() {
		return obtltxnExrate;
	}
	public void setObtltxnExrate(String obtltxnExrate) {
		this.obtltxnExrate = obtltxnExrate;
	}
	public String getObtltxnEjfno() {
		return obtltxnEjfno;
	}
	public void setObtltxnEjfno(String obtltxnEjfno) {
		this.obtltxnEjfno = obtltxnEjfno;
	}
	
	public Obtltxn getObtltxn() {
		return obtltxn;
	}
	public void setObtltxn(Obtltxn obtltxn) {
		this.obtltxn = obtltxn;
	}
	public String getObtltxnStan() {
		return obtltxnStan;
	}
	public void setObtltxnStan(String obtltxnStan) {
		this.obtltxnStan = obtltxnStan;
	}
	public String getObtltxnOriStan() {
		return obtltxnOriStan;
	}
	public void setObtltxnOriStan(String obtltxnOriStan) {
		this.obtltxnOriStan = obtltxnOriStan;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	

}
