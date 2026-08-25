package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

// 財金電文58XX BODY
public class FISC_USDCLR extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 幣別
	// N(3) 3
	// bitmap 位置: 2
	private String mCUR = StringUtils.EMPTY;

	// 銀行代號
	// N( 7 ) 7
	// bitmap 位置: 3
	private String mBKNO = StringUtils.EMPTY;

	// 跨行業務代號
	// N( 5 ) 5
	// bitmap 位置: 4
	private String mAPID5 = StringUtils.EMPTY;

	// 轉出單位代號
	// N( 7 ) 7
	// bitmap 位置: 5
	private String mTROUT_BKNO = StringUtils.EMPTY;

	// 轉入單位代號
	// N( 7 ) 7
	// bitmap 位置: 6
	private String mTRIN_BKNO = StringUtils.EMPTY;

	// 轉出帳號
	// N( 16 ) 16
	// bitmap 位置: 7
	private String mTROUT_ACTNO = StringUtils.EMPTY;

	// 轉入帳號
	// N(16 ) 16
	// bitmap 位置: 8
	private String mTRIN_ACTNO = StringUtils.EMPTY;

	// 保留欄位 9
	//
	// bitmap 位置: 9
	private String mReserve9 = StringUtils.EMPTY;

	// 保留欄位 10
	//
	// bitmap 位置: 10
	private String mReserve10 = StringUtils.EMPTY;

	// 合計應收(借方)筆數
	// N( 6 ) 6
	// bitmap 位置: 11
	private String mSUM_CNT_DR = StringUtils.EMPTY;

	// 合計應收(借方)金額
	// NS( 16 ) 16
	// bitmap 位置: 12
	private String mSUM_AMT_DR = StringUtils.EMPTY;

	// 合計應付(貸方)筆數
	// N( 6 ) 6
	// bitmap 位置: 13
	private String mSUM_CNT_CR = StringUtils.EMPTY;

	// 合計應付(貸方)金額
	// NS( 16 ) 16
	// bitmap 位置: 14
	private String mSUM_AMT_CR = StringUtils.EMPTY;

	// 通匯應收(借方)筆數
	// N( 6 ) 6
	// bitmap 位置: 15
	private String mRM_CNT_DR = StringUtils.EMPTY;

	// 通匯應收(借方)金額
	// NS( 16 ) 16
	// bitmap 位置: 16
	private String mRM_AMT_DR = StringUtils.EMPTY;

	// 通匯應付(貸方)筆數
	// N( 6 ) 6
	// bitmap 位置: 17
	private String mRM_CNT_CR = StringUtils.EMPTY;

	// 通匯應付(貸方)金額
	// NS( 16 ) 16
	// bitmap 位置: 18
	private String mRM_AMT_CR = StringUtils.EMPTY;

	// 撥轉應收( 貸方 )筆數
	// N( 6 ) 6
	// bitmap 位置: 19
	private String mFG_CNT_DR = StringUtils.EMPTY;

	// 撥轉應收( 貸方 )金額
	// NS( 16) 16
	// bitmap 位置: 20
	private String mFG_AMT_DR = StringUtils.EMPTY;

	// 撥轉應付( 借方 )筆數
	// N( 6 ) 6
	// bitmap 位置: 21
	private String mFG_CNT_CR = StringUtils.EMPTY;

	// 撥轉應付( 借方 )金額
	// NS( 16 ) 16
	// bitmap 位置: 22
	private String mFG_AMT_CR = StringUtils.EMPTY;

	// 待解筆數
	// N( 6 ) 6
	// bitmap 位置: 23
	private String mREMAIN_CNT = StringUtils.EMPTY;

	// 待解金額
	// NS( 16 ) 16
	// bitmap 位置: 24
	private String mREMAIN_AMT = StringUtils.EMPTY;

	// 保留欄位 25
	//
	// bitmap 位置: 25
	private String mReserve25 = StringUtils.EMPTY;

	// 保留欄位 26
	//
	// bitmap 位置: 26
	private String mReserve26 = StringUtils.EMPTY;

	// 保留欄位 27
	//
	// bitmap 位置: 27
	private String mReserve27 = StringUtils.EMPTY;

	// 保留欄位 28
	//
	// bitmap 位置: 28
	private String mReserve28 = StringUtils.EMPTY;

	// 保留欄位 29
	//
	// bitmap 位置: 29
	private String mReserve29 = StringUtils.EMPTY;

	// 保留欄位 30
	//
	// bitmap 位置: 30
	private String mReserve30 = StringUtils.EMPTY;

	// 保留欄位 31
	//
	// bitmap 位置: 31
	private String mReserve31 = StringUtils.EMPTY;

	// 保留欄位 32
	//
	// bitmap 位置: 32
	private String mReserve32 = StringUtils.EMPTY;

	// 保留欄位 33
	//
	// bitmap 位置: 33
	private String mReserve33 = StringUtils.EMPTY;

	// 保留欄位 34
	//
	// bitmap 位置: 34
	private String mReserve34 = StringUtils.EMPTY;

	// 傳送總筆數
	// N( 3 ) 3
	// bitmap 位置: 35
	private String mReserve35 = StringUtils.EMPTY;

	// 總應收/應付淨額
	// NS( 16) 16
	// bitmap 位置: 36
	private String mReserve36 = StringUtils.EMPTY;

	// 回撥前餘額
	// NS( 16 ) 16
	// bitmap 位置: 37
	private String mReserve37 = StringUtils.EMPTY;

	// 撥回調撥專戶金額
	// NS( 16 ) 16
	// bitmap 位置: 38
	private String mREVOL_AMT = StringUtils.EMPTY;

	// 參加單位間交換基碼同步查核欄
	// B( 32 ) 4
	// bitmap 位置: 39
	private String mINBK_SYNC = StringUtils.EMPTY;

	// 參加單位間訊息押碼
	// B( 32 ) 4
	// bitmap 位置: 40
	private String mINBK_MAC = StringUtils.EMPTY;

	// 跨行預留基金
	// NS( 12 ) 12
	// bitmap 位置: 41
	private String mACT_BAL = StringUtils.EMPTY;

	// 跨行業務基金低限
	// NS( 12 ) 12
	// bitmap 位置: 42
	private String mReserve42 = StringUtils.EMPTY;

	// 轉帳時點
	// AN( 1 ) 1
	// bitmap 位置: 43
	private String mFG_PERIOD = StringUtils.EMPTY;

	// 轉帳日期
	// N( 6 ) 6
	// bitmap 位置: 44
	private String mFG_DATE = StringUtils.EMPTY;

	// 交易性質
	// AN( 2 ) 2
	// bitmap 位置: 45
	private String mTX_TYPE = StringUtils.EMPTY;

	// 參加單位透支額度
	// NS( 16 ) 16
	// bitmap 位置: 46
	private String mOD_LIMIT = StringUtils.EMPTY;

	// 已動用透支額度
	// NS( 16 ) 16
	// bitmap 位置: 47
	private String mOD_AMT = StringUtils.EMPTY;

	// 轉入單位調撥專戶餘額
	// NS( 16 ) 16
	// bitmap 位置: 48
	private String mFG_TRIN_BAL = StringUtils.EMPTY;

	// 保留欄位 49
	//
	// bitmap 位置: 49
	private String mReserve49 = StringUtils.EMPTY;

	// 調撥類別
	// AN( 1 ) 1
	// bitmap 位置: 50
	private String mFG_TYPE = StringUtils.EMPTY;

	// 調撥金額
	// NS(16 ) 16
	// bitmap 位置: 51
	private String mFG_AMT = StringUtils.EMPTY;

	// 清算行跨行基金
	// NS(16 ) 16
	// bitmap 位置: 52
	private String mCLRBK_FUND_BAL = StringUtils.EMPTY;

	// 財金公司跨行基金
	// NS(16 ) 16
	// bitmap 位置: 53
	private String mFISC_FUND_BAL = StringUtils.EMPTY;

	// 調撥專戶餘額
	// NS(16 ) 16
	// bitmap 位置: 54
	private String mFG_TROUT_BAL = StringUtils.EMPTY;

	// 調撥序號
	// N(7 ) 7
	// bitmap 位置: 55
	private String mFG_SEQNO = StringUtils.EMPTY;

	// 原交易序號
	// N(10 ) 10
	// bitmap 位置: 56
	private String mORI_STAN = StringUtils.EMPTY;

	// 處理結果
	// AN( 2 ) 2
	// bitmap 位置: 57
	private String mRESULT = StringUtils.EMPTY;

	// 保留欄位 58
	//
	// bitmap 位置: 58
	private String mReserve58 = StringUtils.EMPTY;

	// 保留欄位 59
	//
	// bitmap 位置: 59
	private String mReserve59 = StringUtils.EMPTY;

	// 保留欄位 60
	//
	// bitmap 位置: 60
	private String mReserve60 = StringUtils.EMPTY;

	// 保留欄位 61
	//
	// bitmap 位置: 61
	private String mReserve61 = StringUtils.EMPTY;

	// 保留欄位 62
	//
	// bitmap 位置: 62
	private String mReserve62 = StringUtils.EMPTY;

	// 保留欄位 63
	//
	// bitmap 位置: 63
	private String mReserve63 = StringUtils.EMPTY;

	public FISC_USDCLR() {
		super();
	}

	public FISC_USDCLR(String fiscFlatfile) {
		super(fiscFlatfile);
	}

	// 參加單位與財金公司間訊息押碼
	// B( 32 ) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

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
	 * 幣別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 2
	 * </remark>
	 */
	public String getCUR() {
		return mCUR;
	}

	public void setCUR(String value) {
		mCUR = value;
		mIndexValue[1] = value;
	}

	/**
	 * 銀行代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 3
	 * </remark>
	 */
	public String getBKNO() {
		return mBKNO;
	}

	public void setBKNO(String value) {
		mBKNO = value;
		mIndexValue[2] = value;
	}

	/**
	 * 跨行業務代號
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 4
	 * </remark>
	 */
	public String getAPID5() {
		return mAPID5;
	}

	public void setAPID5(String value) {
		mAPID5 = value;
		mIndexValue[3] = value;
	}

	/**
	 * 轉出單位代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 5
	 * </remark>
	 */
	public String getTroutBkno() {
		return mTROUT_BKNO;
	}

	public void setTroutBkno(String value) {
		mTROUT_BKNO = value;
		mIndexValue[4] = value;
	}

	/**
	 * 轉入單位代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 6
	 * </remark>
	 */
	public String getTrinBkno() {
		return mTRIN_BKNO;
	}

	public void setTrinBkno(String value) {
		mTRIN_BKNO = value;
		mIndexValue[5] = value;
	}

	/**
	 * 轉出帳號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 7
	 * </remark>
	 */
	public String getTroutActno() {
		return mTROUT_ACTNO;
	}

	public void setTroutActno(String value) {
		mTROUT_ACTNO = value;
		mIndexValue[6] = value;
	}

	/**
	 * 轉入帳號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 8
	 * </remark>
	 */
	public String getTrinActno() {
		return mTRIN_ACTNO;
	}

	public void setTrinActno(String value) {
		mTRIN_ACTNO = value;
		mIndexValue[7] = value;
	}

	/**
	 * 保留欄位 9
	 * 
	 * <remark>
	 * ; bitmap 位置: 9
	 * </remark>
	 */
	public String getReserve9() {
		return mReserve9;
	}

	public void setReserve9(String value) {
		mReserve9 = value;
		mIndexValue[8] = value;
	}

	/**
	 * 保留欄位 10
	 * 
	 * <remark>
	 * ; bitmap 位置: 10
	 * </remark>
	 */
	public String getReserve10() {
		return mReserve10;
	}

	public void setReserve10(String value) {
		mReserve10 = value;
		mIndexValue[9] = value;
	}

	/**
	 * 合計應收(借方)筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 11
	 * </remark>
	 */
	public String getSumCntDr() {
		return mSUM_CNT_DR;
	}

	public void setSumCntDr(String value) {
		mSUM_CNT_DR = value;
		mIndexValue[10] = value;
	}

	/**
	 * 合計應收(借方)金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 12
	 * </remark>
	 */
	public String getSumAmtDr() {
		return mSUM_AMT_DR;
	}

	public void setSumAmtDr(String value) {
		mSUM_AMT_DR = value;
		mIndexValue[11] = value;
	}

	/**
	 * 合計應付(貸方)筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getSumCntCr() {
		return mSUM_CNT_CR;
	}

	public void setSumCntCr(String value) {
		mSUM_CNT_CR = value;
		mIndexValue[12] = value;
	}

	/**
	 * 合計應付(貸方)金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 14
	 * </remark>
	 */
	public String getSumAmtCr() {
		return mSUM_AMT_CR;
	}

	public void setSumAmtCr(String value) {
		mSUM_AMT_CR = value;
		mIndexValue[13] = value;
	}

	/**
	 * 通匯應收(借方)筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 15
	 * </remark>
	 */
	public String getRmCntDr() {
		return mRM_CNT_DR;
	}

	public void setRmCntDr(String value) {
		mRM_CNT_DR = value;
		mIndexValue[14] = value;
	}

	/**
	 * 通匯應收(借方)金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 16
	 * </remark>
	 */
	public String getRmAmtDr() {
		return mRM_AMT_DR;
	}

	public void setRmAmtDr(String value) {
		mRM_AMT_DR = value;
		mIndexValue[15] = value;
	}

	/**
	 * 通匯應付(貸方)筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 17
	 * </remark>
	 */
	public String getRmCntCr() {
		return mRM_CNT_CR;
	}

	public void setRmCntCr(String value) {
		mRM_CNT_CR = value;
		mIndexValue[16] = value;
	}

	/**
	 * 通匯應付(貸方)金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 18
	 * </remark>
	 */
	public String getRmAmtCr() {
		return mRM_AMT_CR;
	}

	public void setRmAmtCr(String value) {
		mRM_AMT_CR = value;
		mIndexValue[17] = value;
	}

	/**
	 * 撥轉應收( 貸方 )筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 19
	 * </remark>
	 */
	public String getFgCntDr() {
		return mFG_CNT_DR;
	}

	public void setFgCntDr(String value) {
		mFG_CNT_DR = value;
		mIndexValue[18] = value;
	}

	/**
	 * 撥轉應收( 貸方 )金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 20
	 * </remark>
	 */
	public String getFgAmtDr() {
		return mFG_AMT_DR;
	}

	public void setFgAmtDr(String value) {
		mFG_AMT_DR = value;
		mIndexValue[19] = value;
	}

	/**
	 * 撥轉應付( 借方 )筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 21
	 * </remark>
	 */
	public String getFgCntCr() {
		return mFG_CNT_CR;
	}

	public void setFgCntCr(String value) {
		mFG_CNT_CR = value;
		mIndexValue[20] = value;
	}

	/**
	 * 撥轉應付( 借方 )金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 22
	 * </remark>
	 */
	public String getFgAmtCr() {
		return mFG_AMT_CR;
	}

	public void setFgAmtCr(String value) {
		mFG_AMT_CR = value;
		mIndexValue[21] = value;
	}

	/**
	 * 待解筆數
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 23
	 * </remark>
	 */
	public String getRemainCnt() {
		return mREMAIN_CNT;
	}

	public void setRemainCnt(String value) {
		mREMAIN_CNT = value;
		mIndexValue[22] = value;
	}

	/**
	 * 待解金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getRemainAmt() {
		return mREMAIN_AMT;
	}

	public void setRemainAmt(String value) {
		mREMAIN_AMT = value;
		mIndexValue[23] = value;
	}

	/**
	 * 保留欄位 25
	 * 
	 * <remark>
	 * ; bitmap 位置: 25
	 * </remark>
	 */
	public String getReserve25() {
		return mReserve25;
	}

	public void setReserve25(String value) {
		mReserve25 = value;
		mIndexValue[24] = value;
	}

	/**
	 * 保留欄位 26
	 * 
	 * <remark>
	 * ; bitmap 位置: 26
	 * </remark>
	 */
	public String getReserve26() {
		return mReserve26;
	}

	public void setReserve26(String value) {
		mReserve26 = value;
		mIndexValue[25] = value;
	}

	/**
	 * 保留欄位 27
	 * 
	 * <remark>
	 * ; bitmap 位置: 27
	 * </remark>
	 */
	public String getReserve27() {
		return mReserve27;
	}

	public void setReserve27(String value) {
		mReserve27 = value;
		mIndexValue[26] = value;
	}

	/**
	 * 保留欄位 28
	 * 
	 * <remark>
	 * ; bitmap 位置: 28
	 * </remark>
	 */
	public String getReserve28() {
		return mReserve28;
	}

	public void setReserve28(String value) {
		mReserve28 = value;
		mIndexValue[27] = value;
	}

	/**
	 * 保留欄位 29
	 * 
	 * <remark>
	 * ; bitmap 位置: 29
	 * </remark>
	 */
	public String getReserve29() {
		return mReserve29;
	}

	public void setReserve29(String value) {
		mReserve29 = value;
		mIndexValue[28] = value;
	}

	/**
	 * 保留欄位 30
	 * 
	 * <remark>
	 * ; bitmap 位置: 30
	 * </remark>
	 */
	public String getReserve30() {
		return mReserve30;
	}

	public void setReserve30(String value) {
		mReserve30 = value;
		mIndexValue[29] = value;
	}

	/**
	 * 保留欄位 31
	 * 
	 * <remark>
	 * ; bitmap 位置: 31
	 * </remark>
	 */
	public String getReserve31() {
		return mReserve31;
	}

	public void setReserve31(String value) {
		mReserve31 = value;
		mIndexValue[30] = value;
	}

	/**
	 * 保留欄位 32
	 * 
	 * <remark>
	 * ; bitmap 位置: 32
	 * </remark>
	 */
	public String getReserve32() {
		return mReserve32;
	}

	public void setReserve32(String value) {
		mReserve32 = value;
		mIndexValue[31] = value;
	}

	/**
	 * 保留欄位 33
	 * 
	 * <remark>
	 * ; bitmap 位置: 33
	 * </remark>
	 */
	public String getReserve33() {
		return mReserve33;
	}

	public void setReserve33(String value) {
		mReserve33 = value;
		mIndexValue[32] = value;
	}

	/**
	 * 保留欄位 34
	 * 
	 * <remark>
	 * ; bitmap 位置: 34
	 * </remark>
	 */
	public String getReserve34() {
		return mReserve34;
	}

	public void setReserve34(String value) {
		mReserve34 = value;
		mIndexValue[33] = value;
	}

	/**
	 * 傳送總筆數
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 35
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
	 * 總應收/應付淨額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 36
	 * </remark>
	 */
	public String getReserve36() {
		return mReserve36;
	}

	public void setReserve36(String value) {
		mReserve36 = value;
		mIndexValue[35] = value;
	}

	/**
	 * 回撥前餘額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 37
	 * </remark>
	 */
	public String getReserve37() {
		return mReserve37;
	}

	public void setReserve37(String value) {
		mReserve37 = value;
		mIndexValue[36] = value;
	}

	/**
	 * 撥回調撥專戶金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 38
	 * </remark>
	 */
	public String getRevolAmt() {
		return mREVOL_AMT;
	}

	public void setRevolAmt(String value) {
		mREVOL_AMT = value;
		mIndexValue[37] = value;
	}

	/**
	 * 參加單位間交換基碼同步查核欄
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 39
	 * </remark>
	 */
	public String getInbkSync() {
		return mINBK_SYNC;
	}

	public void setInbkSync(String value) {
		mINBK_SYNC = value;
		mIndexValue[38] = value;
	}

	/**
	 * 參加單位間訊息押碼
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 40
	 * </remark>
	 */
	public String getInbkMac() {
		return mINBK_MAC;
	}

	public void setInbkMac(String value) {
		mINBK_MAC = value;
		mIndexValue[39] = value;
	}

	/**
	 * 跨行預留基金
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 41
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
	 * 跨行業務基金低限
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 42
	 * </remark>
	 */
	public String getReserve42() {
		return mReserve42;
	}

	public void setReserve42(String value) {
		mReserve42 = value;
		mIndexValue[41] = value;
	}

	/**
	 * 轉帳時點
	 * 
	 * <remark>
	 * 1 ; bitmap 位置: 43
	 * </remark>
	 */
	public String getFgPeriod() {
		return mFG_PERIOD;
	}

	public void setFgPeriod(String value) {
		mFG_PERIOD = value;
		mIndexValue[42] = value;
	}

	/**
	 * 轉帳日期
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 44
	 * </remark>
	 */
	public String getFgDate() {
		return mFG_DATE;
	}

	public void setFgDate(String value) {
		mFG_DATE = value;
		mIndexValue[43] = value;
	}

	/**
	 * 交易性質
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 45
	 * </remark>
	 */
	public String getTxType() {
		return mTX_TYPE;
	}

	public void setTxType(String value) {
		mTX_TYPE = value;
		mIndexValue[44] = value;
	}

	/**
	 * 參加單位透支額度
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 46
	 * </remark>
	 */
	public String getOdLimit() {
		return mOD_LIMIT;
	}

	public void setOdLimit(String value) {
		mOD_LIMIT = value;
		mIndexValue[45] = value;
	}

	/**
	 * 已動用透支額度
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 47
	 * </remark>
	 */
	public String getOdAmt() {
		return mOD_AMT;
	}

	public void setOdAmt(String value) {
		mOD_AMT = value;
		mIndexValue[46] = value;
	}

	/**
	 * 轉入單位調撥專戶餘額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 48
	 * </remark>
	 */
	public String getFgTrinBal() {
		return mFG_TRIN_BAL;
	}

	public void setFgTrinBal(String value) {
		mFG_TRIN_BAL = value;
		mIndexValue[47] = value;
	}

	/**
	 * 保留欄位 49
	 * 
	 * <remark>
	 * ; bitmap 位置: 49
	 * </remark>
	 */
	public String getReserve49() {
		return mReserve49;
	}

	public void setReserve49(String value) {
		mReserve49 = value;
		mIndexValue[48] = value;
	}

	/**
	 * 調撥類別
	 * 
	 * <remark>
	 * 1 ; bitmap 位置: 50
	 * </remark>
	 */
	public String getFgType() {
		return mFG_TYPE;
	}

	public void setFgType(String value) {
		mFG_TYPE = value;
		mIndexValue[49] = value;
	}

	/**
	 * 調撥金額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 51
	 * </remark>
	 */
	public String getFgAmt() {
		return mFG_AMT;
	}

	public void setFgAmt(String value) {
		mFG_AMT = value;
		mIndexValue[50] = value;
	}

	/**
	 * 清算行跨行基金
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 52
	 * </remark>
	 */
	public String getClrbkFundBal() {
		return mCLRBK_FUND_BAL;
	}

	public void setClrbkFundBal(String value) {
		mCLRBK_FUND_BAL = value;
		mIndexValue[51] = value;
	}

	/**
	 * 財金公司跨行基金
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 53
	 * </remark>
	 */
	public String getFiscFundBal() {
		return mFISC_FUND_BAL;
	}

	public void setFiscFundBal(String value) {
		mFISC_FUND_BAL = value;
		mIndexValue[52] = value;
	}

	/**
	 * 調撥專戶餘額
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 54
	 * </remark>
	 */
	public String getFgTroutBal() {
		return mFG_TROUT_BAL;
	}

	public void setFgTroutBal(String value) {
		mFG_TROUT_BAL = value;
		mIndexValue[53] = value;
	}

	/**
	 * 調撥序號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 55
	 * </remark>
	 */
	public String getFgSeqno() {
		return mFG_SEQNO;
	}

	public void setFgSeqno(String value) {
		mFG_SEQNO = value;
		mIndexValue[54] = value;
	}

	/**
	 * 原交易序號
	 * 
	 * <remark>
	 * 10 ; bitmap 位置: 56
	 * </remark>
	 */
	public String getOriStan() {
		return mORI_STAN;
	}

	public void setOriStan(String value) {
		mORI_STAN = value;
		mIndexValue[55] = value;
	}

	/**
	 * 處理結果
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 57
	 * </remark>
	 */
	public String getRESULT() {
		return mRESULT;
	}

	public void setRESULT(String value) {
		mRESULT = value;
		mIndexValue[56] = value;
	}

	/**
	 * 保留欄位 58
	 * 
	 * <remark>
	 * ; bitmap 位置: 58
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
	 * 保留欄位 59
	 * 
	 * <remark>
	 * ; bitmap 位置: 59
	 * </remark>
	 */
	public String getReserve59() {
		return mReserve59;
	}

	public void setReserve59(String value) {
		mReserve59 = value;
		mIndexValue[58] = value;
	}

	/**
	 * 保留欄位 60
	 * 
	 * <remark>
	 * ; bitmap 位置: 60
	 * </remark>
	 */
	public String getReserve60() {
		return mReserve60;
	}

	public void setReserve60(String value) {
		mReserve60 = value;
		mIndexValue[59] = value;
	}

	/**
	 * 保留欄位 61
	 * 
	 * <remark>
	 * ; bitmap 位置: 61
	 * </remark>
	 */
	public String getReserve61() {
		return mReserve61;
	}

	public void setReserve61(String value) {
		mReserve61 = value;
		mIndexValue[60] = value;
	}

	/**
	 * 保留欄位 62
	 * 
	 * <remark>
	 * ; bitmap 位置: 62
	 * </remark>
	 */
	public String getReserve62() {
		return mReserve62;
	}

	public void setReserve62(String value) {
		mReserve62 = value;
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
					// 幣別
					mCUR = value;
					break;
				case 2:
					// 銀行代號
					mBKNO = value;
					break;
				case 3:
					// 跨行業務代號
					mAPID5 = value;
					break;
				case 4:
					// 轉出單位代號
					mTROUT_BKNO = value;
					break;
				case 5:
					// 轉入單位代號
					mTRIN_BKNO = value;
					break;
				case 6:
					// 轉出帳號
					mTROUT_ACTNO = value;
					break;
				case 7:
					// 轉入帳號
					mTRIN_ACTNO = value;
					break;
				case 8:
					// 保留欄位 9
					mReserve9 = value;
					break;
				case 9:
					// 保留欄位 10
					mReserve10 = value;
					break;
				case 10:
					// 合計應收(借方)筆數
					mSUM_CNT_DR = value;
					break;
				case 11:
					// 合計應收(借方)金額
					mSUM_AMT_DR = value;
					break;
				case 12:
					// 合計應付(貸方)筆數
					mSUM_CNT_CR = value;
					break;
				case 13:
					// 合計應付(貸方)金額
					mSUM_AMT_CR = value;
					break;
				case 14:
					// 通匯應收(借方)筆數
					mRM_CNT_DR = value;
					break;
				case 15:
					// 通匯應收(借方)金額
					mRM_AMT_DR = value;
					break;
				case 16:
					// 通匯應付(貸方)筆數
					mRM_CNT_CR = value;
					break;
				case 17:
					// 通匯應付(貸方)金額
					mRM_AMT_CR = value;
					break;
				case 18:
					// 撥轉應收( 貸方 )筆數
					mFG_CNT_DR = value;
					break;
				case 19:
					// 撥轉應收( 貸方 )金額
					mFG_AMT_DR = value;
					break;
				case 20:
					// 撥轉應付( 借方 )筆數
					mFG_CNT_CR = value;
					break;
				case 21:
					// 撥轉應付( 借方 )金額
					mFG_AMT_CR = value;
					break;
				case 22:
					// 待解筆數
					mREMAIN_CNT = value;
					break;
				case 23:
					// 待解金額
					mREMAIN_AMT = value;
					break;
				case 24:
					// 保留欄位 25
					mReserve25 = value;
					break;
				case 25:
					// 保留欄位 26
					mReserve26 = value;
					break;
				case 26:
					// 保留欄位 27
					mReserve27 = value;
					break;
				case 27:
					// 保留欄位 28
					mReserve28 = value;
					break;
				case 28:
					// 保留欄位 29
					mReserve29 = value;
					break;
				case 29:
					// 保留欄位 30
					mReserve30 = value;
					break;
				case 30:
					// 保留欄位 31
					mReserve31 = value;
					break;
				case 31:
					// 保留欄位 32
					mReserve32 = value;
					break;
				case 32:
					// 保留欄位 33
					mReserve33 = value;
					break;
				case 33:
					// 保留欄位 34
					mReserve34 = value;
					break;
				case 34:
					// 傳送總筆數
					mReserve35 = value;
					break;
				case 35:
					// 總應收/應付淨額
					mReserve36 = value;
					break;
				case 36:
					// 回撥前餘額
					mReserve37 = value;
					break;
				case 37:
					// 撥回調撥專戶金額
					mREVOL_AMT = value;
					break;
				case 38:
					// 參加單位間交換基碼同步查核欄
					mINBK_SYNC = value;
					break;
				case 39:
					// 參加單位間訊息押碼
					mINBK_MAC = value;
					break;
				case 40:
					// 跨行預留基金
					mACT_BAL = value;
					break;
				case 41:
					// 跨行業務基金低限
					mReserve42 = value;
					break;
				case 42:
					// 轉帳時點
					mFG_PERIOD = value;
					break;
				case 43:
					// 轉帳日期
					mFG_DATE = value;
					break;
				case 44:
					// 交易性質
					mTX_TYPE = value;
					break;
				case 45:
					// 參加單位透支額度
					mOD_LIMIT = value;
					break;
				case 46:
					// 已動用透支額度
					mOD_AMT = value;
					break;
				case 47:
					// 轉入單位調撥專戶餘額
					mFG_TRIN_BAL = value;
					break;
				case 48:
					// 保留欄位 49
					mReserve49 = value;
					break;
				case 49:
					// 調撥類別
					mFG_TYPE = value;
					break;
				case 50:
					// 調撥金額
					mFG_AMT = value;
					break;
				case 51:
					// 清算行跨行基金
					mCLRBK_FUND_BAL = value;
					break;
				case 52:
					// 財金公司跨行基金
					mFISC_FUND_BAL = value;
					break;
				case 53:
					// 調撥專戶餘額
					mFG_TROUT_BAL = value;
					break;
				case 54:
					// 調撥序號
					mFG_SEQNO = value;
					break;
				case 55:
					// 原交易序號
					mORI_STAN = value;
					break;
				case 56:
					// 處理結果
					mRESULT = value;
					break;
				case 57:
					// 保留欄位 58
					mReserve58 = value;
					break;
				case 58:
					// 保留欄位 59
					mReserve59 = value;
					break;
				case 59:
					// 保留欄位 60
					mReserve60 = value;
					break;
				case 60:
					// 保留欄位 61
					mReserve61 = value;
					break;
				case 61:
					// 保留欄位 62
					mReserve62 = value;
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