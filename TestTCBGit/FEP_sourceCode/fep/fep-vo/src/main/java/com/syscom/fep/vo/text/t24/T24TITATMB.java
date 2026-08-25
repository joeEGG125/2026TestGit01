package com.syscom.fep.vo.text.t24;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class T24TITATMB {
    // IsPassed Part-----------------------------------------

    // TMB.AC.BAL.DTL.LIST.ENQ (帳戶餘額總覽查詢)
    public boolean isENQ_CUST_IDPassed;
    public boolean isENQ_ACCT_IDPassed;
    public boolean isENQ_AC_TYPEPassed;
    public boolean isENQ_MAX_CNTPassed;


    // TMB.RMT.OUT

    public boolean isT_Br_SEQNOPassed;
    public boolean isT_Sn_FISC_NOPassed;
    public boolean isCREDIT_ACCT_NOPassed;
    public boolean isT_PYMT_MODEPassed;
    public boolean isT_FISC_TYPEPassed;
    public boolean isDEBIT_ACCT_NOPassed;
    public boolean isT_CRG_WAV_KEYPassed;
    public boolean isDEBIT_AMOUNTPassed;
    public boolean isCHARGES_ACCT_NOPassed;
    public boolean isT_RTN_CHARGEPassed;
    public boolean isCOMMISSION_AMTPassed;
    public boolean isAMOUNT_DEBITEDPassed;
    public boolean isDATE_TIMEPassed;
    public boolean isT_COLL_BRPassed;
    public boolean isT_BEN_BANKPassed;
    public boolean isT_BEN_ACCT_NOPassed;
    public boolean isT_BEN_CUSTOMERPassed;
    public boolean isT_TRN_PRT_IDPassed;
    public boolean isT_TRN_PRT_NAMEPassed;
    public boolean isT_TRN_PRT_PHNOPassed;
    public boolean isT_PSB_REM_F_CPassed;
    public boolean isT_TXN_TAX_NOPassed;
    public boolean isCO_CODEPassed;
    public boolean isT_KPI_BRPassed;
    public boolean isT_CR_KPI_BRPassed;
    public boolean isINPUTTERPassed;
    public boolean isAUTHORISERPassed;


    // TMB.LRM.BATCH

    public boolean isT_INP_BRPassed;
    public boolean isT_PSB_REM_F_DPassed;

    // TMB.RM.LCYIN.AUTO

    public boolean isT_OR_FISC_NOPassed;
    public boolean isT_PSB_MEMO_DPassed;
    public boolean isT_PSB_REM_S_DPassed;
    public boolean isT_PSB_RINF_DPassed;
    public boolean isT_PSB_MEMO_CPassed;
    public boolean isT_PSB_REM_S_CPassed;
    public boolean isT_PSB_RINF_CPassed;
    public boolean isORDERING_BANKPassed;

    // TMB.RMT.IN

    public boolean isVALUE_DATEPassed;
    public boolean isTRANSACTION_TYPEPassed;
    public boolean isDEBIT_CURRENCYPassed;

    // TMB.RMT.IN.RET
    public boolean isT_REV_REASONPassed;
    public boolean isDEBIT_THEIR_REFPassed;

    // TMB.RMT.OUT(FUNCTION="REVERSE")
    public boolean isTransactionIDPassed;

    //交易代號
    public boolean isT_TXN_CODEPassed;

    public boolean isFT_IDPassed;

    // TMBI.AC.OUTAC.ENQ
    public boolean isENQ_ACCT_NOPassed;

    // TMB.ALLAC.PSWD.CHG
    public boolean isT_MNEMONICPassed;
    public boolean isT_WTD_PSWDPassed;

    // T.ENOTE.SEQNO
    public boolean isT_ENOTE_SEQNOPassed;

    // Prviate part ----------------------------------------------------------

    // TMB.AC.BAL.DTL.LIST.ENQ (帳戶餘額總覽查詢)
    private String enqCustID; //客戶ID
    private String enqAcctID; //帳號
    private String enqAcType; //帳號種類
    private String enqMaxCnt; //最大筆數

    // TMB.RMT.OUT

    private String tBrSeqno;
    private String tSnFiscNo;
    private String creditAcctNo;
    private String tPymtMode;
    private String tFiscType;
    private String debitAcctNo;
    private String tCrgWavKey;
    private BigDecimal debitAmount = new BigDecimal(0);
    private String chargesAcctNo;
    private BigDecimal tRtnCharge = new BigDecimal(0);
    private BigDecimal commissionAmt = new BigDecimal(0);
    private BigDecimal amountDebited = new BigDecimal(0);
    private String dateTime;
    private String tCillBr;
    private String tBenBank;
    private String tBenAcctNo;
    private String tBenCustomer;
    private String tTrnPrtID;
    private String tTrnPrtName;
    private String tTrnPrtPhno;
    private HashMap<Integer, String> tPsbRemFC = new HashMap<Integer, String>();
    private String tTxnTaxNo;
    private String coCode;
    private String tKpiBr;
    private String tCrKpiBr;
    private String inputter;
    private String authoriser;


    // TMB.LRM.BATCH

    private String tInpBr;
    private String tPsbRemFD;

    // TMB.RM.LCYIN.AUTO

    private String tOrFiscNo;
    private String tPsbMemoD;
    private String tPsbRemSD;
    private HashMap<Integer, String> tPsbRinfD = new HashMap<Integer, String>();
    private String tPsbMemoC;
    private String tPsbRemSC;
    private String tPsbRinfC;
    private String orderingBank;

    // TMB.RMT.IN

    private String valueDate;
    private String debitCurrency;
    private String transactionType;

    // TMB.RMT.IN.RET

    private String tRevReason;
    private String debitTheirRef;

    // TMB.RMT.OUT(FUNCTION="REVERSE")

    private String transactionID;

    //交易代號
    public String tTxnCode;

    // TMBI.AC.OUTAC.ENQ
    private String enqAcctNo; //帳號

    // TMB.ALLAC.PSWD.CHG
    private String tMnemonic; //統編+檢碼0
    private String tWtdPswd; //壓密後的變更密碼

    // TMB.ENOTE.TFR.DETS
    private String tEnoteSeqno; //傳票編號

    // Property part -----------------------------------------------------------------

    //For R2401
    public String ftID;

    //TMB.AC.BAL.DTL.LIST.ENQ(帳戶餘額總覽查詢)
    /**
     客戶ID
     */
    public String getEnqCustId() {
        return enqCustID;
    }
    public void setEnqCustId(String value) {
        enqCustID = value;
        isENQ_CUST_IDPassed = true;
    }

    /**
     帳號
     */
    public String getEnqAcctId() {
        return enqAcctID;
    }
    public void setEnqAcctId(String value) {
        enqAcctID = value;
        isENQ_ACCT_IDPassed = true;
    }

    /**
     帳號種類
     */
    public String getEnqAcType() {
        return enqAcType;
    }
    public void setEnqAcType(String value) {
        enqAcType = value;
        isENQ_AC_TYPEPassed = true;
    }

    /**
     最大筆數
     */
    public String getEnqMaxCnt() {
        return enqMaxCnt;
    }
    public void setEnqMaxCnt(String value) {
        enqMaxCnt = value;
        isENQ_MAX_CNTPassed = true;
    }

    // TMB.RMT.OUT

    public String getTBrSeqno() {
        return tBrSeqno;
    }
    public void setTBrSeqno(String value) {
        tBrSeqno = value;
        isT_Br_SEQNOPassed = true;
    }

    public String getTSnFiscNo() {
        return tSnFiscNo;
    }
    public void setTSnFiscNo(String value) {
        tSnFiscNo = value;
        isT_Sn_FISC_NOPassed = true;
    }

    public String getCreditAcctNo() {
        return creditAcctNo;
    }
    public void setCreditAcctNo(String value) {
        creditAcctNo = value;
        isCREDIT_ACCT_NOPassed = true;
    }

    public String getTPymtMode() {
        return tPymtMode;
    }
    public void setTPymtMode(String value) {
        tPymtMode = value;
        isT_PYMT_MODEPassed = true;
    }

    public String getTFiscType() {
        return tFiscType;
    }
    public void setTFiscType(String value) {
        tFiscType = value;
        isT_FISC_TYPEPassed = true;
    }

    public String getDebitAcctNo() {
        return debitAcctNo;
    }
    public void setDebitAcctNo(String value) {
        debitAcctNo = value;
        isDEBIT_ACCT_NOPassed = true;
    }

    public String getTCrgWavKey() {
        return tCrgWavKey;
    }
    public void setTCrgWavKey(String value) {
        tCrgWavKey = value;
        isT_CRG_WAV_KEYPassed = true;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }
    public void setDebitAmount(BigDecimal value) {
        debitAmount = value;
        isDEBIT_AMOUNTPassed = true;
    }

    public String getChargesAcctNo() {
        return chargesAcctNo;
    }
    public void setChargesAcctNo(String value) {
        chargesAcctNo = value;
        isCHARGES_ACCT_NOPassed = true;
    }

    public BigDecimal getTRtnCharge() {
        return tRtnCharge;
    }
    public void setTRtnCharge(BigDecimal value) {
        tRtnCharge = value;
        isT_RTN_CHARGEPassed = true;
    }

    public BigDecimal getCommissionAmt() {
        return commissionAmt;
    }
    public void setCommissionAmt(BigDecimal value) {
        commissionAmt = value;
        isCOMMISSION_AMTPassed = true;
    }

    public BigDecimal getAmountDebited() {
        return amountDebited;
    }
    public void setAmountDebited(BigDecimal value) {
        amountDebited = value;
        isAMOUNT_DEBITEDPassed = true;
    }

    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String value) {
        dateTime = value;
        isDATE_TIMEPassed = true;
    }

    public String getTCollBr() {
        return tCillBr;
    }
    public void setTCollBr(String value) {
        tCillBr = value;
        isT_COLL_BRPassed = true;
    }

    public String getTBenBank() {
        return tBenBank;
    }
    public void setTBenBank(String value) {
        tBenBank = value;
        isT_BEN_BANKPassed = true;
    }

    public String getTBenAcctNo() {
        return tBenAcctNo;
    }
    public void setTBenAcctNo(String value) {
        tBenAcctNo = value;
        isT_BEN_ACCT_NOPassed = true;
    }

    public String getTBenCustomer() {
        return tBenCustomer;
    }
    public void setTBenCustomer(String value) {
        tBenCustomer = value;
        isT_BEN_CUSTOMERPassed = true;
    }

    public String getTTrnPrtId() {
        return tTrnPrtID;
    }
    public void setTTrnPrtId(String value) {
        tTrnPrtID = value;
        isT_TRN_PRT_IDPassed = true;
    }

    public String getTTrnPrtName() {
        return tTrnPrtName;
    }
    public void setTTrnPrtName(String value) {
        tTrnPrtName = value;
        isT_TRN_PRT_NAMEPassed = true;
    }

    public String getTTrnPrtPhno() {
        return tTrnPrtPhno;
    }
    public void setTTrnPrtPhno(String value) {
        tTrnPrtPhno = value;
        isT_TRN_PRT_PHNOPassed = true;
    }

    public HashMap<Integer, String> getTPsbRemFC() {
        return tPsbRemFC;
    }
    public void setTPsbRemFC(HashMap<Integer, String> value) {
        tPsbRemFC = value;
        isT_PSB_REM_F_CPassed = true;
    }

    public String getTTxnTaxNo() {
        return tTxnTaxNo;
    }
    public void setTTxnTaxNo(String value) {
        tTxnTaxNo = value;
        isT_TXN_TAX_NOPassed = true;
    }

    public String getCoCode() {
        return coCode;
    }
    public void setCoCode(String value) {
        coCode = value;
        isCO_CODEPassed = true;
    }

    public String getTKpiBr() {
        return tKpiBr;
    }
    public void setTKpiBr(String value) {
        tKpiBr = value;
        isT_KPI_BRPassed = true;
    }

    public String getTCrKpiBr() {
        return tCrKpiBr;
    }
    public void setTCrKpiBr(String value) {
        tCrKpiBr = value;
        isT_CR_KPI_BRPassed = true;
    }

    public String getINPUTTER() {
        return inputter;
    }
    public void setINPUTTER(String value) {
        inputter = value;
        isINPUTTERPassed = true;
    }

    public String getAUTHORISER() {
        return authoriser;
    }
    public void setAUTHORISER(String value) {
        authoriser = value;
        isAUTHORISERPassed = true;
    }

    // TMB.LRM.BATCH

    public String getTInpBr() {
        return tInpBr;
    }
    public void setTInpBr(String value) {
        tInpBr = value;
        isT_INP_BRPassed = true;
    }

    public String getTPsbRemFD() {
        return tPsbRemFD;
    }
    public void setTPsbRemFD(String value) {
        tPsbRemFD = value;
        isT_PSB_REM_F_DPassed = true;
    }

    // TMB.RM.LCYIN.AUTO

    public String getTOrFiscNo() {
        return tOrFiscNo;
    }
    public void setTOrFiscNo(String value) {
        tOrFiscNo = value;
        isT_OR_FISC_NOPassed = true;
    }

    public String getTPsbMemoD() {
        return tPsbMemoD;
    }
    public void setTPsbMemoD(String value) {
        tPsbMemoD = value;
        isT_PSB_MEMO_DPassed = true;
    }

    public String getTPsbRemSD() {
        return tPsbRemSD;
    }
    public void setTPsbRemSD(String value) {
        tPsbRemSD = value;
        isT_PSB_REM_S_DPassed = true;
    }

    public HashMap<Integer, String> getTPsbRinfD() {
        return tPsbRinfD;
    }
    public void setTPsbRinfD(HashMap<Integer, String> value) {
        tPsbRinfD = value;
        isT_PSB_RINF_DPassed = true;
    }

    public String getTPsbMemoC() {
        return tPsbMemoC;
    }
    public void setTPsbMemoC(String value) {
        tPsbMemoC = value;
        isT_PSB_MEMO_CPassed = true;
    }

    public String getTPsbRemSC() {
        return tPsbRemSC;
    }
    public void setTPsbRemSC(String value) {
        tPsbRemSC = value;
        isT_PSB_REM_S_CPassed = true;
    }



    public String getTPsbRinfC() {
        return tPsbRinfC;
    }
    public void setTPsbRinfC(String value) {
        tPsbRinfC = value;
        isT_PSB_RINF_CPassed = true;
    }
    public String getOrderingBank() {
        return orderingBank;
    }
    public void setOrderingBank(String value) {
        orderingBank = value;
        isORDERING_BANKPassed = true;
    }


    // TMB.RMT.IN

    public String getValueDate() {
        return valueDate;
    }
    public void setValueDate(String value) {
        valueDate = value;
        isVALUE_DATEPassed = true;
    }
    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String value) {
        transactionType = value;
        isTRANSACTION_TYPEPassed = true;
    }
    public String getDebitCurrency() {
        return debitCurrency;
    }
    public void setDebitCurrency(String value) {
        debitCurrency = value;
        isDEBIT_CURRENCYPassed = true;
    }

    // TMB.RMT.IN.RET

    public String getTRevReason() {
        return tRevReason;
    }
    public void setTRevReason(String value) {
        tRevReason = value;
        isT_REV_REASONPassed = true;
    }

    public String getDebitTheirRef() {
        return debitTheirRef;
    }
    public void setDebitTheirRef(String value) {
        debitTheirRef = value;
        isDEBIT_THEIR_REFPassed = true;
    }

    // TMB.RMT.OUT(FUNCTION="REVERSE")

    public String getTransactionID() {
        return transactionID;
    }
    public void setTransactionID(String value) {
        transactionID = value;
        isTransactionIDPassed = true;
    }

    //交易代號
    public String getTTxnCode() {
        return tTxnCode;
    }
    public void setTTxnCode(String value) {
        tTxnCode = value;
        isT_TXN_CODEPassed = true;
    }

    //FT.ID
    public String getFtId() {
        return ftID;
    }
    public void setFtId(String value) {
        ftID = value;
        isFT_IDPassed = true;
    }

    //TMBI.AC.OUTAC.ENQ
    /**
     帳號
     */
    public String getEnqAcctNo() {
        return enqAcctNo;
    }
    public void setEnqAcctNo(String value) {
        enqAcctNo = value;
        isENQ_ACCT_NOPassed = true;
    }

    // TMB.ALLAC.PSWD.CHG
    /**
     統編+檢碼0
     */
    public String getTMnemonic() {
        return tMnemonic;
    }
    public void setTMnemonic(String value) {
        tMnemonic = value;
        isT_MNEMONICPassed = true;
    }

    /**
     壓密後的變更密碼
     */
    public String getTWtdPswd() {
        return tWtdPswd;
    }
    public void setTWtdPswd(String value) {
        tWtdPswd = value;
        isT_WTD_PSWDPassed = true;
    }

    /**
     傳票編號
     */
    public String getTEnoteSeqno() {
        return tEnoteSeqno;
    }
    public void setTEnoteSeqno(String value) {
        tEnoteSeqno = value;
        isT_ENOTE_SEQNOPassed = true;
    }

    public boolean genDictionary(HashMap<String, String> TITABody) {
        //TMB.AC.BAL.DTL.LIST.ENQ(帳戶餘額總覽查詢)
        if (isENQ_CUST_IDPassed) {
            TITABody.put("ENQ.CUST.ID", getEnqCustId());
        }

        if (isENQ_ACCT_IDPassed) {
            TITABody.put("ENQ.ACCT.ID", getEnqAcctId());
        }

        if (isENQ_AC_TYPEPassed) {
            TITABody.put("ENQ.AC.TYPE", getEnqAcType());
        }

        if (isENQ_MAX_CNTPassed) {
            TITABody.put("ENQ.MAX.CNT", getEnqMaxCnt());
        }


        // TMB.RMT.OUT
        if (isT_Br_SEQNOPassed) {
            TITABody.put("T.BR.SEQNO", getTBrSeqno());
        }

        if (isT_Sn_FISC_NOPassed) {
            TITABody.put("T.SN.FISC.NO", getTSnFiscNo());
        }

        if (isCREDIT_ACCT_NOPassed) {
            TITABody.put("CREDIT.ACCT.NO", getCreditAcctNo());
        }

        if (isT_PYMT_MODEPassed) {
            TITABody.put("T.PYMT.MODE", getTPymtMode());
        }

        if (isT_FISC_TYPEPassed) {
            TITABody.put("T.FISC.TYPE", getTFiscType());
        }

        if (isDEBIT_ACCT_NOPassed) {
            TITABody.put("DEBIT.ACCT.NO", getDebitAcctNo());
        }

        if (isT_CRG_WAV_KEYPassed) {
            TITABody.put("T.CRG.WAV.KEY", getTCrgWavKey());
        }

        if (isDEBIT_AMOUNTPassed) {
            TITABody.put("DEBIT.AMOUNT", getDebitAmount().toString());
        }

        if (isCHARGES_ACCT_NOPassed) {
            TITABody.put("CHARGES.ACCT.NO", getChargesAcctNo());
        }

        if (isT_RTN_CHARGEPassed) {
            TITABody.put("T.RTN.CHARGE", getTRtnCharge().toString());
        }

        if (isCOMMISSION_AMTPassed) {
            TITABody.put("COMMISSION.AMT", getCommissionAmt().toString());
        }

        if (isAMOUNT_DEBITEDPassed) {
            TITABody.put("AMOUNT.DEBITED", getAmountDebited().toString());
        }

        if (isDATE_TIMEPassed) {
            TITABody.put("DATE.TIME", getDateTime());
        }

        if (isT_COLL_BRPassed) {
            TITABody.put("T.COLL.BR", getTCollBr());
        }

        if (isT_BEN_BANKPassed) {
            TITABody.put("T.BEN.BANK", getTBenBank());
        }

        if (isT_BEN_ACCT_NOPassed) {
            TITABody.put("T.BEN.ACCT.NO", getTBenAcctNo());
        }

        if (isTRANSACTION_TYPEPassed) {
            TITABody.put("TRANSACTION.TYPE", getTransactionType());
        }

        if (isDEBIT_CURRENCYPassed) {
            TITABody.put("DEBIT.CURRENCY", getDebitCurrency());
        }

        if (isT_TXN_CODEPassed) {
            TITABody.put("T.TXN.CODE", getTTxnCode());
        }

        if (isT_BEN_CUSTOMERPassed) {
            TITABody.put("T.BEN.CUSTOMER", getTBenCustomer());
        }

        if (isT_TRN_PRT_IDPassed) {
            TITABody.put("T.TRN.PRT.ID", getTTrnPrtId());
        }

        if (isT_TRN_PRT_NAMEPassed) {
            TITABody.put("T.TRN.PRT.NAME", getTTrnPrtName());
        }

        if (isT_TRN_PRT_PHNOPassed) {
            TITABody.put("T.TRN.PRT.PHNO", getTTrnPrtPhno());
        }

        if (!getTPsbRemFC().isEmpty()) {
            for (Map.Entry<Integer, String> item : getTPsbRemFC().entrySet()) {
                TITABody.put("T.PSB.REM.F.C_" + String.valueOf(item.getKey()), item.getValue().toString());
            }
        }

        if (isT_TXN_TAX_NOPassed) {
            TITABody.put("T.TXN.TAX.NO", getTTxnTaxNo());
        }

        if (isCO_CODEPassed) {
            TITABody.put("CO.CODE", getCoCode());
        }

        if (isT_KPI_BRPassed) {
            TITABody.put("T.KPI.BR", getTKpiBr());
        }

        if (isT_CR_KPI_BRPassed) {
            TITABody.put("T.CR.KPI.BR", getTCrKpiBr());
        }

        if (isINPUTTERPassed) {
            TITABody.put("INPUTTER", getINPUTTER());
        }

        if (isAUTHORISERPassed) {
            TITABody.put("AUTHORISER", getAUTHORISER());
        }

        // TMB.LRM.BATCH

        if (isT_INP_BRPassed) {
            TITABody.put("T.INP.BR", getTInpBr());
        }

        if (isT_PSB_REM_F_DPassed) {
            TITABody.put("T.PSB.REM.F.D", getTPsbRemFD());
        }

        // TMB.RM.LCYIN.AUTO
        if (isORDERING_BANKPassed) {
            TITABody.put("ORDERING.BANK", getOrderingBank());
        }
        if (isT_OR_FISC_NOPassed) {
            TITABody.put("T.OR.FISC.NO", getTOrFiscNo());
        }

        if (isT_PSB_MEMO_DPassed) {
            TITABody.put("T.PSB.MEMO.D", getTPsbMemoD());
        }

        if (isT_PSB_REM_S_DPassed) {
            TITABody.put("T.PSB.REM.S.D", getTPsbRemSD());
        }

        // Dictionary ---------------------------------------------------------------------
        //Modify by Jim, 2011/5/16, 改成判斷Dictionary的長度
        if (!getTPsbRinfD().isEmpty()) {
            for (Map.Entry<Integer, String> item : getTPsbRinfD().entrySet()) {
                TITABody.put("T.PSB.RINF.D_" + String.valueOf(item.getKey()), item.getValue().toString());
            }
        }

        if (isT_PSB_MEMO_CPassed) {
            TITABody.put("T.PSB.MEMO.C", getTPsbMemoC());
        }

        if (isT_PSB_REM_S_CPassed) {
            TITABody.put("T.PSB.REM.S.C", getTPsbRemSC());
        }

        if (isT_PSB_RINF_CPassed) {
            TITABody.put("T.PSB.RINF.C", getTPsbRinfC());
        }

        // TMB.RMT.IN

        if (isVALUE_DATEPassed) {
            TITABody.put("VALUE.DATE", getValueDate());
        }

        // TMB.RMT.IN.RET

        if (isT_REV_REASONPassed) {
            TITABody.put("T.REV.REASON", getTRevReason());
        }

        if (isDEBIT_THEIR_REFPassed) {
            TITABody.put("DEBIT.THEIR.REF", getDebitTheirRef());
        }

        // TMB.RMT.OUT(FUNCTION="REVERSE")

        if (isTransactionIDPassed) {
            TITABody.put("transactionId", getTransactionID());
        }

        if (isFT_IDPassed) {
            TITABody.put("FT.ID", getFtId());
        }

        // TMBI.AC.OUTAC.ENQ
        if (isENQ_ACCT_NOPassed) {
            TITABody.put("ENQ.ACCT.NO", getEnqAcctNo());
        }

        //TMBI.AC.OUTAC.ENQ
        if (isT_MNEMONICPassed) {
            TITABody.put("T.MNEMONIC", getTMnemonic());
        }
        if (isT_WTD_PSWDPassed) {
            TITABody.put("T.WTD.PSWD", getTWtdPswd());
        }

        //T.ENOTE.SEQNO
        if (isT_ENOTE_SEQNOPassed) {
            TITABody.put("T.ENOTE.SEQNO", getTEnoteSeqno());
        }
        return false;
    }

}
