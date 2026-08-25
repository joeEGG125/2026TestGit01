package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

// 財金電文5XXX BODY
public class FISC_CLR extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 跨行預留基金
	// NS( 12 ) 12
	// bitmap 位置: 2
	private String mFUND_BAL = StringUtils.EMPTY;

	// 跨行業務基金低限
	// NS( 12 ) 12
	// bitmap 位置: 3
	private String mFUND_LOW_BAL = StringUtils.EMPTY;

	// 跨行預留基金可用餘額
	// NS( 14 ) 14
	// bitmap 位置: 4
	private String mFUND_AVAIL = StringUtils.EMPTY;

	// 跨行業務代號
	// N( 5 ) 5
	// bitmap 位置: 5
	private String mAPID5 = StringUtils.EMPTY;

	// 合計應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 6
	private String mSUM_CNT_DR = StringUtils.EMPTY;

	// 合計應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 7
	private String mSUM_AMT_DR = StringUtils.EMPTY;

	// 合計應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 8
	private String mSUM_CNT_CR = StringUtils.EMPTY;

	// 合計應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 9
	private String mSUM_AMT_CR = StringUtils.EMPTY;

	// 通匯應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 10
	private String mRM_CNT_DR = StringUtils.EMPTY;

	// 通匯應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 11
	private String mRM_AMT_DR = StringUtils.EMPTY;

	// 通匯應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 12
	private String mRM_CNT_CR = StringUtils.EMPTY;

	// 通匯應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 13
	private String mRM_AMT_CR = StringUtils.EMPTY;

	// CD/ATM 應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 14
	private String mATM_CNT_DR = StringUtils.EMPTY;

	// CD/ATM 應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 15
	private String mATM_AMT_DR = StringUtils.EMPTY;

	// CD/ATM 應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 16
	private String mATM_CNT_CR = StringUtils.EMPTY;

	// CD/ATM 應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 17
	private String mATM_AMT_CR = StringUtils.EMPTY;

	// 手續費應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 18
	private String mFEE_CNT_DR = StringUtils.EMPTY;

	// 手續費應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 19
	private String mFEE_AMT_DR = StringUtils.EMPTY;

	// 手續費應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 20
	private String mFEE_CNT_CR = StringUtils.EMPTY;

	// 手續費應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 21
	private String mFEE_AMT_CR = StringUtils.EMPTY;

	// 應收差額
	// NS( 15 ) 15
	// bitmap 位置: 22
	private String mODDS_DR = StringUtils.EMPTY;

	// 應付差額
	// NS( 15 ) 15
	// bitmap 位置: 23
	private String mODDS_CR = StringUtils.EMPTY;

	// 被查詢參加單位代號
	// N( 3 ) 3
	// bitmap 位置: 24
	private String mTROUT_BKNO = StringUtils.EMPTY;

	// 跨行基金增減金額
	// NS( 15 ) 15
	// bitmap 位置: 25
	private String mFG_AMT = StringUtils.EMPTY;

	// 支票號碼／查詢序號／銀行代號＋分支機構代號
	// N( 7 ) 7
	// bitmap 位置: 26
	private String mCHEQUE_NO = StringUtils.EMPTY;

	// 待解筆數
	// N( 5 ) 5
	// bitmap 位置: 27
	private String mREMAIN_CNT = StringUtils.EMPTY;

	// 待解金額
	// NS( 15 ) 15
	// bitmap 位置: 28
	private String mREMAIN_AMT = StringUtils.EMPTY;

	// CD/ATM 沖正應收( 借方 )筆數
	// N( 15 ) 5
	// bitmap 位置: 29
	private String mATM_EC_CNT_DR = StringUtils.EMPTY;

	// CD/ATM 沖正應收( 借方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 30
	private String mATM_EC_AMT_DR = StringUtils.EMPTY;

	// CD/ATM 沖正應付( 貸方 )筆數
	// N( 15 ) 5
	// bitmap 位置: 31
	private String mATM_EC_CNT_CR = StringUtils.EMPTY;

	// CD/ATM 沖正應付( 貸方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 32
	private String mATM_EC_AMT_CR = StringUtils.EMPTY;

	// 沖正手續費應收( 借方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 33
	private String mFEE_EC_AMT_DR = StringUtils.EMPTY;

	// 沖正手續費應收( 貸方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 34
	private String mFEE_EC_AMT_CR = StringUtils.EMPTY;

	// 清算行結帳或清算行查詢資料
	// AN( 375 ) 375
	// bitmap 位置: 35
	private String mReserve35 = StringUtils.EMPTY;

	// 撥轉應收( 貸方 )筆數
	// N( 5 ) 5
	// bitmap 位置: 36
	private String mFG_CNT_DR = StringUtils.EMPTY;

	// 撥轉應收( 貸方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 37
	private String mFG_AMT_DR = StringUtils.EMPTY;

	// 撥轉應付( 借方 )筆數
	// N( 5 ) 5
	// bitmap 位置: 38
	private String mFG_CNT_CR = StringUtils.EMPTY;

	// 撥轉應付( 借方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 39
	private String mFG_AMT_CR = StringUtils.EMPTY;

	// 撥回週轉金戶金額
	// NS( 15 ) 15
	// bitmap 位置: 40
	private String mREVOL_AMT = StringUtils.EMPTY;

	// 留存金資戶金額
	// NS( 15 ) 15
	// bitmap 位置: 41
	private String mACT_BAL = StringUtils.EMPTY;

	// POS 應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 42
	private String mPOS_CNT_DR = StringUtils.EMPTY;

	// POS 應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 43
	private String mPOS_AMT_DR = StringUtils.EMPTY;

	// POS 應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 44
	private String mPOS_CNT_CR = StringUtils.EMPTY;

	// POS 應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 45
	private String mPOS_AMT_CR = StringUtils.EMPTY;

	// 撥轉序號
	// N( 7 ) 7
	// bitmap 位置: 46
	private String mFG_SEQNO = StringUtils.EMPTY;

	// FEDI 應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 47
	private String mFEDI_CNT_DR = StringUtils.EMPTY;

	// FEDI 應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 48
	private String mFEDI_AMT_DR = StringUtils.EMPTY;

	// FEDI 應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 49
	private String mFEDI_CNT_CR = StringUtils.EMPTY;

	// FEDI 應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 50
	private String mFEDI_AMT_CR = StringUtils.EMPTY;

	// CD/ATM 應收(借方)筆數二
	// N( 7 ) 7
	// bitmap 位置: 51
	private String mATM_CNT2_DR = StringUtils.EMPTY;

	// 通匯應收筆數(二)
	// N( 7 ) 7
	// bitmap 位置: 52
	private String mRM_CNT2_DR = StringUtils.EMPTY;

	// CD/ATM 應付(貸方)筆數二
	// N( 7 ) 7
	// bitmap 位置: 53
	private String mATM_CNT2_CR = StringUtils.EMPTY;

	// 通匯應付筆數(二)
	// N( 7 ) 7
	// bitmap 位置: 54
	private String mRM_CNT2_CR = StringUtils.EMPTY;

	// FXML 應收(借方)筆數
	// N( 5 ) 5
	// bitmap 位置: 55
	private String mReserve55 = StringUtils.EMPTY;

	// FXML 應收(借方)金額
	// NS( 15 ) 15
	// bitmap 位置: 56
	private String mReserve56 = StringUtils.EMPTY;

	// FXML 應付(貸方)筆數
	// N( 5 ) 5
	// bitmap 位置: 57
	private String mReserve57 = StringUtils.EMPTY;

	// FXML 應付(貸方)金額
	// NS( 15 ) 15
	// bitmap 位置: 58
	private String mReserve58 = StringUtils.EMPTY;

	// 代繳代發轉帳類別/消費扣款共用平台清算業務代號
	// AN( 5 ) 5
	// bitmap 位置: 59
	private String mPAYTYPE = StringUtils.EMPTY;

	// 跨行預留基金可用餘額(二)
	// NS( 18 ) 18
	// bitmap 位置: 60
	private String mFUND_AVAIL2 = StringUtils.EMPTY;

	// 合計應收(借方)筆數(二)
	// N( 7 ) 7
	// bitmap 位置: 61
	private String mSUM_CNT2_DR = StringUtils.EMPTY;

	// 合計應付(貸方)筆數(二)
	// N( 7 ) 7
	// bitmap 位置: 62
	private String mSUM_CNT2_CR = StringUtils.EMPTY;

	// 保留欄位 63
	//
	// bitmap 位置: 63
	private String mReserve63 = StringUtils.EMPTY;

	// 參加單位與財金公司間訊息押碼
	// B( 32 ) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

	public FISC_CLR() {
		super();
	}

	public FISC_CLR(String fiscFlatfile) {
		super(fiscFlatfile);
	}

	/**
	 * 保留欄位 1
	 * 
	 * <remark>
	 * ; bitmap 位置: 1
	 * </remark>
	 */
	public String getReserve1() {
		return mReserve1;
	}

	public void setReserve1(String value) {
		mReserve1 = value;
		mIndexValue[0] = value;
	}

	/**
	 * 跨行預留基金
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 2
	 * </remark>
	 */
	public String getFundBal() {
		return mFUND_BAL;
	}

	public void setFundBal(String value) {
		mFUND_BAL = value;
		mIndexValue[1] = value;
	}

	/**
	 * 跨行業務基金低限
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 3
	 * </remark>
	 */
	public String getFundLowBal() {
		return mFUND_LOW_BAL;
	}

	public void setFundLowBal(String value) {
		mFUND_LOW_BAL = value;
		mIndexValue[2] = value;
	}

	/**
	 * 跨行預留基金可用餘額
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 4
	 * </remark>
	 */
	public String getFundAvail() {
		return mFUND_AVAIL;
	}

	public void setFundAvail(String value) {
		mFUND_AVAIL = value;
		mIndexValue[3] = value;
	}

	/**
	 * 跨行業務代號
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 5
	 * </remark>
	 */
	public String getAPID5() {
		return mAPID5;
	}

	public void setAPID5(String value) {
		mAPID5 = value;
		mIndexValue[4] = value;
	}

	/**
	 * 合計應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 6
	 * </remark>
	 */
	public String getSumCntDr() {
		return mSUM_CNT_DR;
	}

	public void setSumCntDr(String value) {
		mSUM_CNT_DR = value;
		mIndexValue[5] = value;
	}

	/**
	 * 合計應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 7
	 * </remark>
	 */
	public String getSumAmtDr() {
		return mSUM_AMT_DR;
	}

	public void setSumAmtDr(String value) {
		mSUM_AMT_DR = value;
		mIndexValue[6] = value;
	}

	/**
	 * 合計應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 8
	 * </remark>
	 */
	public String getSumCntCr() {
		return mSUM_CNT_CR;
	}

	public void setSumCntCr(String value) {
		mSUM_CNT_CR = value;
		mIndexValue[7] = value;
	}

	/**
	 * 合計應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 9
	 * </remark>
	 */
	public String getSumAmtCr() {
		return mSUM_AMT_CR;
	}

	public void setSumAmtCr(String value) {
		mSUM_AMT_CR = value;
		mIndexValue[8] = value;
	}

	/**
	 * 通匯應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 10
	 * </remark>
	 */
	public String getRmCntDr() {
		return mRM_CNT_DR;
	}

	public void setRmCntDr(String value) {
		mRM_CNT_DR = value;
		mIndexValue[9] = value;
	}

	/**
	 * 通匯應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 11
	 * </remark>
	 */
	public String getRmAmtDr() {
		return mRM_AMT_DR;
	}

	public void setRmAmtDr(String value) {
		mRM_AMT_DR = value;
		mIndexValue[10] = value;
	}

	/**
	 * 通匯應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 12
	 * </remark>
	 */
	public String getRmCntCr() {
		return mRM_CNT_CR;
	}

	public void setRmCntCr(String value) {
		mRM_CNT_CR = value;
		mIndexValue[11] = value;
	}

	/**
	 * 通匯應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getRmAmtCr() {
		return mRM_AMT_CR;
	}

	public void setRmAmtCr(String value) {
		mRM_AMT_CR = value;
		mIndexValue[12] = value;
	}

	/**
	 * CD/ATM 應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 14
	 * </remark>
	 */
	public String getAtmCntDr() {
		return mATM_CNT_DR;
	}

	public void setAtmCntDr(String value) {
		mATM_CNT_DR = value;
		mIndexValue[13] = value;
	}

	/**
	 * CD/ATM 應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 15
	 * </remark>
	 */
	public String getAtmAmtDr() {
		return mATM_AMT_DR;
	}

	public void setAtmAmtDr(String value) {
		mATM_AMT_DR = value;
		mIndexValue[14] = value;
	}

	/**
	 * CD/ATM 應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 16
	 * </remark>
	 */
	public String getAtmCntCr() {
		return mATM_CNT_CR;
	}

	public void setAtmCntCr(String value) {
		mATM_CNT_CR = value;
		mIndexValue[15] = value;
	}

	/**
	 * CD/ATM 應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 17
	 * </remark>
	 */
	public String getAtmAmtCr() {
		return mATM_AMT_CR;
	}

	public void setAtmAmtCr(String value) {
		mATM_AMT_CR = value;
		mIndexValue[16] = value;
	}

	/**
	 * 手續費應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 18
	 * </remark>
	 */
	public String getFeeCntDr() {
		return mFEE_CNT_DR;
	}

	public void setFeeCntDr(String value) {
		mFEE_CNT_DR = value;
		mIndexValue[17] = value;
	}

	/**
	 * 手續費應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 19
	 * </remark>
	 */
	public String getFeeAmtDr() {
		return mFEE_AMT_DR;
	}

	public void setFeeAmtDr(String value) {
		mFEE_AMT_DR = value;
		mIndexValue[18] = value;
	}

	/**
	 * 手續費應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 20
	 * </remark>
	 */
	public String getFeeCntCr() {
		return mFEE_CNT_CR;
	}

	public void setFeeCntCr(String value) {
		mFEE_CNT_CR = value;
		mIndexValue[19] = value;
	}

	/**
	 * 手續費應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 21
	 * </remark>
	 */
	public String getFeeAmtCr() {
		return mFEE_AMT_CR;
	}

	public void setFeeAmtCr(String value) {
		mFEE_AMT_CR = value;
		mIndexValue[20] = value;
	}

	/**
	 * 應收差額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 22
	 * </remark>
	 */
	public String getOddsDr() {
		return mODDS_DR;
	}

	public void setOddsDr(String value) {
		mODDS_DR = value;
		mIndexValue[21] = value;
	}

	/**
	 * 應付差額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 23
	 * </remark>
	 */
	public String getOddsCr() {
		return mODDS_CR;
	}

	public void setOddsCr(String value) {
		mODDS_CR = value;
		mIndexValue[22] = value;
	}

	/**
	 * 被查詢參加單位代號
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getTroutBkno() {
		return mTROUT_BKNO;
	}

	public void setTroutBkno(String value) {
		mTROUT_BKNO = value;
		mIndexValue[23] = value;
	}

	/**
	 * 跨行基金增減金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 25
	 * </remark>
	 */
	public String getFgAmt() {
		return mFG_AMT;
	}

	public void setFgAmt(String value) {
		mFG_AMT = value;
		mIndexValue[24] = value;
	}

	/**
	 * 支票號碼／查詢序號／銀行代號＋分支機構代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 26
	 * </remark>
	 */
	public String getChequeNo() {
		return mCHEQUE_NO;
	}

	public void setChequeNo(String value) {
		mCHEQUE_NO = value;
		mIndexValue[25] = value;
	}

	/**
	 * 待解筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 27
	 * </remark>
	 */
	public String getRemainCnt() {
		return mREMAIN_CNT;
	}

	public void setRemainCnt(String value) {
		mREMAIN_CNT = value;
		mIndexValue[26] = value;
	}

	/**
	 * 待解金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 28
	 * </remark>
	 */
	public String getRemainAmt() {
		return mREMAIN_AMT;
	}

	public void setRemainAmt(String value) {
		mREMAIN_AMT = value;
		mIndexValue[27] = value;
	}

	/**
	 * CD/ATM 沖正應收( 借方 )筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 29
	 * </remark>
	 */
	public String getAtmEcCntDr() {
		return mATM_EC_CNT_DR;
	}

	public void setAtmEcCntDr(String value) {
		mATM_EC_CNT_DR = value;
		mIndexValue[28] = value;
	}

	/**
	 * CD/ATM 沖正應收( 借方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 30
	 * </remark>
	 */
	public String getAtmEcAmtDr() {
		return mATM_EC_AMT_DR;
	}

	public void setAtmEcAmtDr(String value) {
		mATM_EC_AMT_DR = value;
		mIndexValue[29] = value;
	}

	/**
	 * CD/ATM 沖正應付( 貸方 )筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 31
	 * </remark>
	 */
	public String getAtmEcCntCr() {
		return mATM_EC_CNT_CR;
	}

	public void setAtmEcCntCr(String value) {
		mATM_EC_CNT_CR = value;
		mIndexValue[30] = value;
	}

	/**
	 * CD/ATM 沖正應付( 貸方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 32
	 * </remark>
	 */
	public String getAtmEcAmtCr() {
		return mATM_EC_AMT_CR;
	}

	public void setAtmEcAmtCr(String value) {
		mATM_EC_AMT_CR = value;
		mIndexValue[31] = value;
	}

	/**
	 * 沖正手續費應收( 借方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 33
	 * </remark>
	 */
	public String getFeeEcAmtDr() {
		return mFEE_EC_AMT_DR;
	}

	public void setFeeEcAmtDr(String value) {
		mFEE_EC_AMT_DR = value;
		mIndexValue[32] = value;
	}

	/**
	 * 沖正手續費應收( 貸方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 34
	 * </remark>
	 */
	public String getFeeEcAmtCr() {
		return mFEE_EC_AMT_CR;
	}

	public void setFeeEcAmtCr(String value) {
		mFEE_EC_AMT_CR = value;
		mIndexValue[33] = value;
	}

	/**
	 * 清算行結帳或清算行查詢資料
	 * 
	 * <remark>
	 * 375 ; bitmap 位置: 35
	 * </remark>
	 */
	public String getReserve35() {
		return mReserve35;
	}

	public void setReserve35(String value) {
		mReserve35 = value;
		mIndexValue[34] = value;
	}

	/**
	 * 撥轉應收( 貸方 )筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 36
	 * </remark>
	 */
	public String getFgCntDr() {
		return mFG_CNT_DR;
	}

	public void setFgCntDr(String value) {
		mFG_CNT_DR = value;
		mIndexValue[35] = value;
	}

	/**
	 * 撥轉應收( 貸方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 37
	 * </remark>
	 */
	public String getFgAmtDr() {
		return mFG_AMT_DR;
	}

	public void setFgAmtDr(String value) {
		mFG_AMT_DR = value;
		mIndexValue[36] = value;
	}

	/**
	 * 撥轉應付( 借方 )筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 38
	 * </remark>
	 */
	public String getFgCntCr() {
		return mFG_CNT_CR;
	}

	public void setFgCntCr(String value) {
		mFG_CNT_CR = value;
		mIndexValue[37] = value;
	}

	/**
	 * 撥轉應付( 借方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 39
	 * </remark>
	 */
	public String getFgAmtCr() {
		return mFG_AMT_CR;
	}

	public void setFgAmtCr(String value) {
		mFG_AMT_CR = value;
		mIndexValue[38] = value;
	}

	/**
	 * 撥回週轉金戶金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 40
	 * </remark>
	 */
	public String getRevolAmt() {
		return mREVOL_AMT;
	}

	public void setRevolAmt(String value) {
		mREVOL_AMT = value;
		mIndexValue[39] = value;
	}

	/**
	 * 留存金資戶金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 41
	 * </remark>
	 */
	public String getActBal() {
		return mACT_BAL;
	}

	public void setActBal(String value) {
		mACT_BAL = value;
		mIndexValue[40] = value;
	}

	/**
	 * POS 應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 42
	 * </remark>
	 */
	public String getPosCntDr() {
		return mPOS_CNT_DR;
	}

	public void setPosCntDr(String value) {
		mPOS_CNT_DR = value;
		mIndexValue[41] = value;
	}

	/**
	 * POS 應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 43
	 * </remark>
	 */
	public String getPosAmtDr() {
		return mPOS_AMT_DR;
	}

	public void setPosAmtDr(String value) {
		mPOS_AMT_DR = value;
		mIndexValue[42] = value;
	}

	/**
	 * POS 應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 44
	 * </remark>
	 */
	public String getPosCntCr() {
		return mPOS_CNT_CR;
	}

	public void setPosCntCr(String value) {
		mPOS_CNT_CR = value;
		mIndexValue[43] = value;
	}

	/**
	 * POS 應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 45
	 * </remark>
	 */
	public String getPosAmtCr() {
		return mPOS_AMT_CR;
	}

	public void setPosAmtCr(String value) {
		mPOS_AMT_CR = value;
		mIndexValue[44] = value;
	}

	/**
	 * 撥轉序號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 46
	 * </remark>
	 */
	public String getFgSeqno() {
		return mFG_SEQNO;
	}

	public void setFgSeqno(String value) {
		mFG_SEQNO = value;
		mIndexValue[45] = value;
	}

	/**
	 * FEDI 應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 47
	 * </remark>
	 */
	public String getFediCntDr() {
		return mFEDI_CNT_DR;
	}

	public void setFediCntDr(String value) {
		mFEDI_CNT_DR = value;
		mIndexValue[46] = value;
	}

	/**
	 * FEDI 應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 48
	 * </remark>
	 */
	public String getFediAmtDr() {
		return mFEDI_AMT_DR;
	}

	public void setFediAmtDr(String value) {
		mFEDI_AMT_DR = value;
		mIndexValue[47] = value;
	}

	/**
	 * FEDI 應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 49
	 * </remark>
	 */
	public String getFediCntCr() {
		return mFEDI_CNT_CR;
	}

	public void setFediCntCr(String value) {
		mFEDI_CNT_CR = value;
		mIndexValue[48] = value;
	}

	/**
	 * FEDI 應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 50
	 * </remark>
	 */
	public String getFediAmtCr() {
		return mFEDI_AMT_CR;
	}

	public void setFediAmtCr(String value) {
		mFEDI_AMT_CR = value;
		mIndexValue[49] = value;
	}

	/**
	 * CD/ATM 應收(借方)筆數二
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 51
	 * </remark>
	 */
	public String getAtmCnt2Dr() {
		return mATM_CNT2_DR;
	}

	public void setAtmCnt2Dr(String value) {
		mATM_CNT2_DR = value;
		mIndexValue[50] = value;
	}

	/**
	 * 通匯應收筆數(二)
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 52
	 * </remark>
	 */
	public String getRmCnt2Dr() {
		return mRM_CNT2_DR;
	}

	public void setRmCnt2Dr(String value) {
		mRM_CNT2_DR = value;
		mIndexValue[51] = value;
	}

	/**
	 * CD/ATM 應付(貸方)筆數二
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 53
	 * </remark>
	 */
	public String getAtmCnt2Cr() {
		return mATM_CNT2_CR;
	}

	public void setAtmCnt2Cr(String value) {
		mATM_CNT2_CR = value;
		mIndexValue[52] = value;
	}

	/**
	 * 通匯應付筆數(二)
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 54
	 * </remark>
	 */
	public String getRmCnt2Cr() {
		return mRM_CNT2_CR;
	}

	public void setRmCnt2Cr(String value) {
		mRM_CNT2_CR = value;
		mIndexValue[53] = value;
	}

	/**
	 * FXML 應收(借方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 55
	 * </remark>
	 */
	public String getReserve55() {
		return mReserve55;
	}

	public void setReserve55(String value) {
		mReserve55 = value;
		mIndexValue[54] = value;
	}

	/**
	 * FXML 應收(借方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 56
	 * </remark>
	 */
	public String getReserve56() {
		return mReserve56;
	}

	public void setReserve56(String value) {
		mReserve56 = value;
		mIndexValue[55] = value;
	}

	/**
	 * FXML 應付(貸方)筆數
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 57
	 * </remark>
	 */
	public String getReserve57() {
		return mReserve57;
	}

	public void setReserve57(String value) {
		mReserve57 = value;
		mIndexValue[56] = value;
	}

	/**
	 * FXML 應付(貸方)金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 58
	 * </remark>
	 */
	public String getReserve58() {
		return mReserve58;
	}

	public void setReserve58(String value) {
		mReserve58 = value;
		mIndexValue[57] = value;
	}

	/**
	 * 代繳代發轉帳類別/消費扣款共用平台清算業務代號
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 59
	 * </remark>
	 */
	public String getPAYTYPE() {
		return mPAYTYPE;
	}

	public void setPAYTYPE(String value) {
		mPAYTYPE = value;
		mIndexValue[58] = value;
	}

	/**
	 * 跨行預留基金可用餘額(二)
	 * 
	 * <remark>
	 * 18 ; bitmap 位置: 60
	 * </remark>
	 */
	public String getFundAvail2() {
		return mFUND_AVAIL2;
	}

	public void setFundAvail2(String value) {
		mFUND_AVAIL2 = value;
		mIndexValue[59] = value;
	}

	/**
	 * 合計應收(借方)筆數(二)
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 61
	 * </remark>
	 */
	public String getSumCnt2Dr() {
		return mSUM_CNT2_DR;
	}

	public void setSumCnt2Dr(String value) {
		mSUM_CNT2_DR = value;
		mIndexValue[60] = value;
	}

	/**
	 * 合計應付(貸方)筆數(二)
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 62
	 * </remark>
	 */
	public String getSumCnt2Cr() {
		return mSUM_CNT2_CR;
	}

	public void setSumCnt2Cr(String value) {
		mSUM_CNT2_CR = value;
		mIndexValue[61] = value;
	}

	/**
	 * 保留欄位 63
	 * 
	 * <remark>
	 * ; bitmap 位置: 63
	 * </remark>
	 */
	public String getReserve63() {
		return mReserve63;
	}

	public void setReserve63(String value) {
		mReserve63 = value;
		mIndexValue[62] = value;
	}

	/**
	 * 參加單位與財金公司間訊息押碼
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 64
	 * </remark>
	 */
	public String getMAC() {
		return mMAC;
	}

	public void setMAC(String value) {
		mMAC = value;
		mIndexValue[63] = value;
	}

	/**
	 * 以bitmap讀取/設定Property的值
	 * 
	 * @param index
	 *        bitmap位置
	 * 
	 *        <value>
	 *        要賦予的值
	 *        </value>
	 * @return
	 *         該欄位的數值
	 * 
	 *         <remark></remark>
	 */
	@Override
	public String getGetPropertyValue(int index) {
		if ((index > 63) || (index < 0)) {
			index = 0;
		}
		return mIndexValue[index];
	}

	@Override
	public void setGetPropertyValue(int index, String value) {
		if ((index < 64) && (index >= 0)) {
			switch (index) {
				case 0:
					// 保留欄位 1
					mReserve1 = value;
					break;
				case 1:
					// 跨行預留基金
					mFUND_BAL = value;
					break;
				case 2:
					// 跨行業務基金低限
					mFUND_LOW_BAL = value;
					break;
				case 3:
					// 跨行預留基金可用餘額
					mFUND_AVAIL = value;
					break;
				case 4:
					// 跨行業務代號
					mAPID5 = value;
					break;
				case 5:
					// 合計應收(借方)筆數
					mSUM_CNT_DR = value;
					break;
				case 6:
					// 合計應收(借方)金額
					mSUM_AMT_DR = value;
					break;
				case 7:
					// 合計應付(貸方)筆數
					mSUM_CNT_CR = value;
					break;
				case 8:
					// 合計應付(貸方)金額
					mSUM_AMT_CR = value;
					break;
				case 9:
					// 通匯應收(借方)筆數
					mRM_CNT_DR = value;
					break;
				case 10:
					// 通匯應收(借方)金額
					mRM_AMT_DR = value;
					break;
				case 11:
					// 通匯應付(貸方)筆數
					mRM_CNT_CR = value;
					break;
				case 12:
					// 通匯應付(貸方)金額
					mRM_AMT_CR = value;
					break;
				case 13:
					// CD/ATM 應收(借方)筆數
					mATM_CNT_DR = value;
					break;
				case 14:
					// CD/ATM 應收(借方)金額
					mATM_AMT_DR = value;
					break;
				case 15:
					// CD/ATM 應付(貸方)筆數
					mATM_CNT_CR = value;
					break;
				case 16:
					// CD/ATM 應付(貸方)金額
					mATM_AMT_CR = value;
					break;
				case 17:
					// 手續費應收(借方)筆數
					mFEE_CNT_DR = value;
					break;
				case 18:
					// 手續費應收(借方)金額
					mFEE_AMT_DR = value;
					break;
				case 19:
					// 手續費應付(貸方)筆數
					mFEE_CNT_CR = value;
					break;
				case 20:
					// 手續費應付(貸方)金額
					mFEE_AMT_CR = value;
					break;
				case 21:
					// 應收差額
					mODDS_DR = value;
					break;
				case 22:
					// 應付差額
					mODDS_CR = value;
					break;
				case 23:
					// 被查詢參加單位代號
					mTROUT_BKNO = value;
					break;
				case 24:
					// 跨行基金增減金額
					mFG_AMT = value;
					break;
				case 25:
					// 支票號碼／查詢序號／銀行代號＋分支機構代號
					mCHEQUE_NO = value;
					break;
				case 26:
					// 待解筆數
					mREMAIN_CNT = value;
					break;
				case 27:
					// 待解金額
					mREMAIN_AMT = value;
					break;
				case 28:
					// CD/ATM 沖正應收( 借方 )筆數
					mATM_EC_CNT_DR = value;
					break;
				case 29:
					// CD/ATM 沖正應收( 借方 )金額
					mATM_EC_AMT_DR = value;
					break;
				case 30:
					// CD/ATM 沖正應付( 貸方 )筆數
					mATM_EC_CNT_CR = value;
					break;
				case 31:
					// CD/ATM 沖正應付( 貸方 )金額
					mATM_EC_AMT_CR = value;
					break;
				case 32:
					// 沖正手續費應收( 借方 )金額
					mFEE_EC_AMT_DR = value;
					break;
				case 33:
					// 沖正手續費應收( 貸方 )金額
					mFEE_EC_AMT_CR = value;
					break;
				case 34:
					// 清算行結帳或清算行查詢資料
					mReserve35 = value;
					break;
				case 35:
					// 撥轉應收( 貸方 )筆數
					mFG_CNT_DR = value;
					break;
				case 36:
					// 撥轉應收( 貸方 )金額
					mFG_AMT_DR = value;
					break;
				case 37:
					// 撥轉應付( 借方 )筆數
					mFG_CNT_CR = value;
					break;
				case 38:
					// 撥轉應付( 借方 )金額
					mFG_AMT_CR = value;
					break;
				case 39:
					// 撥回週轉金戶金額
					mREVOL_AMT = value;
					break;
				case 40:
					// 留存金資戶金額
					mACT_BAL = value;
					break;
				case 41:
					// POS 應收(借方)筆數
					mPOS_CNT_DR = value;
					break;
				case 42:
					// POS 應收(借方)金額
					mPOS_AMT_DR = value;
					break;
				case 43:
					// POS 應付(貸方)筆數
					mPOS_CNT_CR = value;
					break;
				case 44:
					// POS 應付(貸方)金額
					mPOS_AMT_CR = value;
					break;
				case 45:
					// 撥轉序號
					mFG_SEQNO = value;
					break;
				case 46:
					// FEDI 應收(借方)筆數
					mFEDI_CNT_DR = value;
					break;
				case 47:
					// FEDI 應收(借方)金額
					mFEDI_AMT_DR = value;
					break;
				case 48:
					// FEDI 應付(貸方)筆數
					mFEDI_CNT_CR = value;
					break;
				case 49:
					// FEDI 應付(貸方)金額
					mFEDI_AMT_CR = value;
					break;
				case 50:
					// CD/ATM 應收(借方)筆數二
					mATM_CNT2_DR = value;
					break;
				case 51:
					// 通匯應收筆數(二)
					mRM_CNT2_DR = value;
					break;
				case 52:
					// CD/ATM 應付(貸方)筆數二
					mATM_CNT2_CR = value;
					break;
				case 53:
					// 通匯應付筆數(二)
					mRM_CNT2_CR = value;
					break;
				case 54:
					// FXML 應收(借方)筆數
					mReserve55 = value;
					break;
				case 55:
					// FXML 應收(借方)金額
					mReserve56 = value;
					break;
				case 56:
					// FXML 應付(貸方)筆數
					mReserve57 = value;
					break;
				case 57:
					// FXML 應付(貸方)金額
					mReserve58 = value;
					break;
				case 58:
					// 代繳代發轉帳類別/消費扣款共用平台清算業務代號
					mPAYTYPE = value;
					break;
				case 59:
					// 跨行預留基金可用餘額(二)
					mFUND_AVAIL2 = value;
					break;
				case 60:
					// 合計應收(借方)筆數(二)
					mSUM_CNT2_DR = value;
					break;
				case 61:
					// 合計應付(貸方)筆數(二)
					mSUM_CNT2_CR = value;
					break;
				case 62:
					// 保留欄位 63
					mReserve63 = value;
					break;
				case 63:
					// 參加單位與財金公司間訊息押碼
					mMAC = value;
					break;
			}
			mIndexValue[index] = value;
		}
	}
}