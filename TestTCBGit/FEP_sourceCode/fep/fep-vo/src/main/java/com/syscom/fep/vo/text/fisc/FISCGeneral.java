package com.syscom.fep.vo.text.fisc;

import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.enums.FISCTxType;

public class FISCGeneral {
	private FISC_INBK mINBKRequest;
	private FISC_INBK mINBKResponse;
	private FISC_INBK mINBKConfirm;
	private FISC_OPC mOPCRequest;
	private FISC_OPC mOPCResponse;
	private FISC_OPC mOPCConfirm;
	private FISC_RM mRMRequest;
	private FISC_RM mRMResponse;
	private FISC_USDRM mFCRMRequest;
	private FISC_USDRM mFCRMResponse;
	private FISC_CLR mCLRRequest;
	private FISC_CLR mCLRResponse;
	private FISC_USDCLR mFCCLRRequest;
	private FISC_USDCLR mFCCLRResponse;
	private FISC_EMVIC mEMVICRequest;
	private FISC_EMVIC mEMVICResponse;
	private FISC_EMVIC mEMVICConfirm;
	@SuppressWarnings("unused")
	private FISCTxType _txType;
	private FISCSubSystem _subSystem;
	private int _ej;
	private String _description;

	public int getEJ() {
		return _ej;
	}

	public void setEJ(int value) {
		_ej = value;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String value) {
		_description = value;
	}

	public FISCSubSystem getSubSystem() {
		return _subSystem;
	}

	public void setSubSystem(FISCSubSystem value) {
		_subSystem = value;
	}

	public FISC_INBK getINBKRequest() {
		return mINBKRequest;
	}

	public void setINBKRequest(FISC_INBK value) {
		mINBKRequest = value;
	}

	public FISC_INBK getINBKResponse() {
		return mINBKResponse;
	}

	public void setINBKResponse(FISC_INBK value) {
		mINBKResponse = value;
	}

	public FISC_INBK getINBKConfirm() {
		return mINBKConfirm;
	}

	public void setINBKConfirm(FISC_INBK value) {
		mINBKConfirm = value;
	}

	public FISC_OPC getOPCRequest() {
		return mOPCRequest;
	}

	public void setOPCRequest(FISC_OPC value) {
		mOPCRequest = value;
	}

	public FISC_OPC getOPCResponse() {
		return mOPCResponse;
	}

	public void setOPCResponse(FISC_OPC value) {
		mOPCResponse = value;
	}

	public FISC_OPC getOPCConfirm() {
		return mOPCConfirm;
	}

	public void setOPCConfirm(FISC_OPC value) {
		mOPCConfirm = value;
	}

	public FISC_RM getRMRequest() {
		return mRMRequest;
	}

	public void setRMRequest(FISC_RM value) {
		mRMRequest = value;
	}

	public FISC_RM getRMResponse() {
		return mRMResponse;
	}

	public void setRMResponse(FISC_RM value) {
		mRMResponse = value;
	}

	public FISC_USDRM getFCRMRequest() {
		return mFCRMRequest;
	}

	public void setFCRMRequest(FISC_USDRM value) {
		mFCRMRequest = value;
	}

	public FISC_USDRM getFCRMResponse() {
		return mFCRMResponse;
	}

	public void setFCRMResponse(FISC_USDRM value) {
		mFCRMResponse = value;
	}

	public FISC_CLR getCLRRequest() {
		return mCLRRequest;
	}

	public void setCLRRequest(FISC_CLR value) {
		mCLRRequest = value;
	}

	public FISC_CLR getCLRResponse() {
		return mCLRResponse;
	}

	public void setCLRResponse(FISC_CLR value) {
		mCLRResponse = value;
	}

	public FISC_USDCLR getFCCLRRequest() {
		return mFCCLRRequest;
	}

	public void setFCCLRRequest(FISC_USDCLR value) {
		mFCCLRRequest = value;
	}

	public FISC_USDCLR getFCCLRResponse() {
		return mFCCLRResponse;
	}

	public void setFCCLRResponse(FISC_USDCLR value) {
		mFCCLRResponse = value;
	}

	public FISC_EMVIC getEMVICRequest() {
		return mEMVICRequest;
	}

	public void setEMVICRequest(FISC_EMVIC value) {
		mEMVICRequest = value;
	}

	public FISC_EMVIC getEMVICResponse() {
		return mEMVICResponse;
	}

	public void setEMVICResponse(FISC_EMVIC value) {
		mEMVICResponse = value;
	}

	public FISC_EMVIC getEMVICConfirm() {
		return mEMVICConfirm;
	}

	public void setEMVICConfirm(FISC_EMVIC value) {
		mEMVICConfirm = value;
	}
}
