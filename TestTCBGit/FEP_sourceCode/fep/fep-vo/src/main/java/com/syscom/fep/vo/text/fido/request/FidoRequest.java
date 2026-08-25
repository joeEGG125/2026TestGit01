package com.syscom.fep.vo.text.fido.request;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class FidoRequest {
	
	private String acqBank;
	
	private String issBank;

	private String stan;
	
	private String txDateTime;
	
	private String verifyType;
	
	private String verifyResult;
	
	private String accVerifyResult;
	
	private String accOpenStatus;
	
	private String terminalType;
	
	private String id;
	
	private String fidoApply;
	
	private String presentType;
	
	private Integer size;
	
	private String txAccount;
	
	private String machine;

	public String getAcqBank() {
		return acqBank;
	}

	public void setAcqBank(String acqBank) {
		this.acqBank = acqBank;
	}

	public String getIssBank() {
		return issBank;
	}

	public void setIssBank(String issBank) {
		this.issBank = issBank;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public String getTxDateTime() {
		return txDateTime;
	}

	public void setTxDateTime(String txDateTime) {
		this.txDateTime = txDateTime;
	}

	public String getVerifyType() {
		return verifyType;
	}

	public void setVerifyType(String verifyType) {
		this.verifyType = verifyType;
	}

	public String getVerifyResult() {
		return verifyResult;
	}

	public void setVerifyResult(String verifyResult) {
		this.verifyResult = verifyResult;
	}

	public String getAccVerifyResult() {
		return accVerifyResult;
	}

	public void setAccVerifyResult(String accVerifyResult) {
		this.accVerifyResult = accVerifyResult;
	}

	public String getAccOpenStatus() {
		return accOpenStatus;
	}

	public void setAccOpenStatus(String accOpenStatus) {
		this.accOpenStatus = accOpenStatus;
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFidoApply() {
		return fidoApply;
	}

	public void setFidoApply(String fidoApply) {
		this.fidoApply = fidoApply;
	}

	public String getPresentType() {
		return presentType;
	}

	public void setPresentType(String presentType) {
		this.presentType = presentType;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
	
	public String getTxAccount() {
		return txAccount;
	}

	public void setTxAccount(String txAccount) {
		this.txAccount = txAccount;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public String toJsonString() {
		HashMap<String, Object> fidoMap = new HashMap<>();
		if(StringUtils.isNotBlank(acqBank))
			fidoMap.put("acqBank", acqBank);
		if(StringUtils.isNotBlank(issBank))
			fidoMap.put("issBank", issBank);
		if(StringUtils.isNotBlank(stan))
			fidoMap.put("stan", stan);
		if(StringUtils.isNotBlank(txDateTime))
			fidoMap.put("txDateTime", txDateTime);
		if(StringUtils.isNotBlank(verifyType))
			fidoMap.put("verifyType", verifyType);
		if(StringUtils.isNotBlank(verifyResult))
			fidoMap.put("verifyResult", verifyResult);
		if(StringUtils.isNotBlank(accVerifyResult))
			fidoMap.put("accVerifyResult", accVerifyResult);
		if(StringUtils.isNotBlank(accOpenStatus))
			fidoMap.put("accOpenStatus", accOpenStatus);
		if(StringUtils.isNotBlank(terminalType))
			fidoMap.put("terminalType", terminalType);
		if(StringUtils.isNotBlank(id))
			fidoMap.put("id", id);
		if(StringUtils.isNotBlank(fidoApply))
			fidoMap.put("fidoApply", fidoApply);
		if(StringUtils.isNotBlank(presentType))
			fidoMap.put("presentType", presentType);
		if(null != size)
			fidoMap.put("size", size);
		if(StringUtils.isNotBlank(txAccount))
			fidoMap.put("txAccount", txAccount);
		if(StringUtils.isNotBlank(machine))
			fidoMap.put("machine", machine);
		
		return new JSONObject(fidoMap).toString();
	}

	@Override
	public String toString() {
		return "FidoRequest [acqBank=" + acqBank + ", issBank=" + issBank + ", stan=" + stan + ", txDateTime="
				+ txDateTime + ", verifyType=" + verifyType + ", verifyResult=" + verifyResult + ", accVerifyResult="
				+ accVerifyResult + ", accOpenStatus=" + accOpenStatus + ", terminalType=" + terminalType + ", id=" + id
				+ ", fidoApply=" + fidoApply + ", presentType=" + presentType + ", size=" + size + ", txAccount="
				+ txAccount + ", machine=" + machine + "]";
	}

}
