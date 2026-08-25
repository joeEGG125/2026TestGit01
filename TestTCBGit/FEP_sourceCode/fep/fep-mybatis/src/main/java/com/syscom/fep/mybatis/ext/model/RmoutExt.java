package com.syscom.fep.mybatis.ext.model;

import com.syscom.fep.mybatis.model.Rmout;

import java.math.BigDecimal;
import java.util.Date;

public class RmoutExt extends Rmout {
	private static final long serialVersionUID = 1L;
	private String tabSrc;

	public RmoutExt() {
		super();
	}

	public RmoutExt(String rmoutTxdate, String rmoutBrno, String rmoutOriginal, String rmoutFepno, String rmoutFepsubno, String rmoutBatchno, String rmoutBrsno, String rmoutRemtype,
			BigDecimal rmoutTxamt, Integer rmoutPostamt, Integer rmoutRecfee, Integer rmoutActfee, String rmoutAmtType, String rmoutServamtType, String rmoutCategory, String rmoutSenderBank,
			String rmoutReceiverBank, String rmoutStanBkno, String rmoutStan, String rmoutFiscsno, String rmoutRmsno, String rmoutStat, String rmoutT24No, String rmoutOwpriority, String rmoutRegdate,
			String rmoutRegtime, String rmoutApdate, String rmoutAptime, String rmoutCanceldate, String rmoutCanceltime, String rmoutOrderdate, String rmoutSenddate, String rmoutSendtime,
			String rmoutOrgrmsno, String rmoutOrgdate, String rmoutOrgregFepno, String rmoutOrgStat, String rmoutBackReason, String rmoutFiscRtnCode, String rmoutOutName, String rmoutInName,
			String rmoutMemo, String rmoutInOrgAccIdNo, String rmoutInAccIdNo, String rmoutSupno1, String rmoutSupno2, String rmoutRegTlrno, String rmoutFiscSndCode, String rmoutCbsRc,
			String rmoutPending, Integer rmoutEjno1, Integer rmoutEjno2, Integer rmoutEjno3, String rmoutRecbrno, String rmoutMacno, String rmoutRemcif, String rmoutRemtel, String rmoutTaxno,
			String rmoutOrgremtype, String rmoutCif, String rmoutGlUnit1, String rmoutGlUnit2, String rmoutGlUnit1a, String rmoutGlUnit2a, Integer updateUserid, Date updateTime,
			String rmoutRemagentid, String rmoutRemagentname, String rmoutRemitterid, BigDecimal rmoutUseBal, String tabSrc) {
		super(rmoutTxdate, rmoutBrno, rmoutOriginal, rmoutFepno, rmoutFepsubno, rmoutBatchno, rmoutBrsno, rmoutRemtype, rmoutTxamt, rmoutPostamt, rmoutRecfee, rmoutActfee, rmoutAmtType, rmoutServamtType,
				rmoutCategory, rmoutSenderBank, rmoutReceiverBank, rmoutStanBkno, rmoutStan, rmoutFiscsno, rmoutRmsno, rmoutStat, rmoutT24No, rmoutOwpriority, rmoutRegdate, rmoutRegtime, rmoutApdate,
				rmoutAptime, rmoutCanceldate, rmoutCanceltime, rmoutOrderdate, rmoutSenddate, rmoutSendtime, rmoutOrgrmsno, rmoutOrgdate, rmoutOrgregFepno, rmoutOrgStat, rmoutBackReason, rmoutFiscRtnCode,
				rmoutOutName, rmoutInName, rmoutMemo, rmoutInOrgAccIdNo, rmoutInAccIdNo, rmoutSupno1, rmoutSupno2, rmoutRegTlrno, rmoutFiscSndCode, rmoutCbsRc, rmoutPending, rmoutEjno1, rmoutEjno2,
				rmoutEjno3, rmoutRecbrno, rmoutMacno, rmoutRemcif, rmoutRemtel, rmoutTaxno, rmoutOrgremtype, rmoutCif, rmoutGlUnit1, rmoutGlUnit2, rmoutGlUnit1a, rmoutGlUnit2a, updateUserid, updateTime,
				rmoutRemagentid, rmoutRemagentname, rmoutRemitterid, rmoutUseBal);
		this.tabSrc = tabSrc;
	}

	public RmoutExt(String tabSrc) {
		this.tabSrc = tabSrc;
	}

	public String getTabSrc() {
		return tabSrc;
	}

	public void setTabSrc(String tabSrc) {
		this.tabSrc = tabSrc;
	}
}
