package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

// 財金電文26XX BODY
public class FISC_EMVIC extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 保留欄位
	// N(14) 14
	// bitmap 位置: 2
	private String mReserve2 = StringUtils.EMPTY;

	// 保留欄位 3
	//
	// bitmap 位置: 3
	private String mReserve3 = StringUtils.EMPTY;

	// Track 2 Data
	// AN(37) 37
	// bitmap 位置: 4
	private String mTRK2 = StringUtils.EMPTY;

	// 客戶密碼
	// B(64) 8
	// bitmap 位置: 5
	private String mPINBLOCK = StringUtils.EMPTY;

	// 代付單位終端機代號
	// AN( 8 ) 8
	// bitmap 位置: 6
	private String mATMNO = StringUtils.EMPTY;

	// 可用餘額
	// NS(14) 14
	// bitmap 位置: 7
	private String mBALA = StringUtils.EMPTY;

	// 交易金額
	// N(12) 12
	// bitmap 位置: 8
	private String mTX_AMT = StringUtils.EMPTY;

	// 交易幣別
	// N(3) 3
	// bitmap 位置: 9
	private String mCURRENCY = StringUtils.EMPTY;

	// 保留欄位 10
	//
	// bitmap 位置: 10
	private String mReserve10 = StringUtils.EMPTY;

	// 保留欄位 11
	//
	// bitmap 位置: 11
	private String mReserve11 = StringUtils.EMPTY;

	// 保留欄位 12
	//
	// bitmap 位置: 12
	private String mReserve12 = StringUtils.EMPTY;

	// POS 狀況代碼
	// N(2) 2
	// bitmap 位置: 13
	private String mPOS_TYPE = StringUtils.EMPTY;

	// 保留欄位 14
	//
	// bitmap 位置: 14
	private String mReserve14 = StringUtils.EMPTY;

	// 帳戶類別
	// N(2) 2
	// bitmap 位置: 15
	private String mACT_TYPE = StringUtils.EMPTY;

	// 保留欄位 16
	//
	// bitmap 位置: 16
	private String mReserve16 = StringUtils.EMPTY;

	// 保留欄位 17
	//
	// bitmap 位置: 17
	private String mReserve17 = StringUtils.EMPTY;

	// 保留欄位 18
	//
	// bitmap 位置: 18
	private String mReserve18 = StringUtils.EMPTY;

	// 保留欄位 19
	//
	// bitmap 位置: 19
	private String mReserve19 = StringUtils.EMPTY;

	// 保留欄位 20
	//
	// bitmap 位置: 20
	private String mReserve20 = StringUtils.EMPTY;

	// 保留欄位 21
	//
	// bitmap 位置: 21
	private String mReserve21 = StringUtils.EMPTY;

	// 保留欄位 22
	//
	// bitmap 位置: 22
	private String mReserve22 = StringUtils.EMPTY;

	// 保留欄位 23
	//
	// bitmap 位置: 23
	private String mReserve23 = StringUtils.EMPTY;

	// NETWORK CODE
	// AN(2) 2
	// bitmap 位置: 24
	private String mNETWORK_CODE = StringUtils.EMPTY;

	// 卡號
	// AN(19) 19
	// bitmap 位置: 25
	private String mPAN_NO = StringUtils.EMPTY;

	// 保留欄位 26
	//
	// bitmap 位置: 26
	private String mReserve26 = StringUtils.EMPTY;

	// 保留欄位 27
	//
	// bitmap 位置: 27
	private String mReserve27 = StringUtils.EMPTY;

	// 交易處理費
	// AN(1)+N(8) 9
	// bitmap 位置: 28
	private String mPROC_FEE = StringUtils.EMPTY;

	// 保留欄位 29
	//
	// bitmap 位置: 29
	private String mReserve29 = StringUtils.EMPTY;

	// 原始交易日期
	// N(6) 6
	// bitmap 位置: 30
	private String mORI_TX_DATE = StringUtils.EMPTY;

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

	// 保留欄位 35
	//
	// bitmap 位置: 35
	private String mReserve35 = StringUtils.EMPTY;

	// CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
	// AN(140) 140
	// bitmap 位置: 36
	private String mORI_DATA = StringUtils.EMPTY;

	// 保留欄位 37
	//
	// bitmap 位置: 37
	private String mReserve37 = StringUtils.EMPTY;

	// 帳戶餘額
	// NS(14) 14
	// bitmap 位置: 38
	private String mBALB = StringUtils.EMPTY;

	// 客戶亂碼基碼同步查核欄( PP-KEY SYNC. CHECK ITEM )
	// B(32) 4
	// bitmap 位置: 39
	private String mSYNC_PPKEY = StringUtils.EMPTY;

	// 保留欄位 40
	//
	// bitmap 位置: 40
	private String mReserve40 = StringUtils.EMPTY;

	// 保留欄位 41
	//
	// bitmap 位置: 41
	private String mReserve41 = StringUtils.EMPTY;

	// 保留欄位 42
	//
	// bitmap 位置: 42
	private String mReserve42 = StringUtils.EMPTY;

	// 保留欄位 43
	//
	// bitmap 位置: 43
	private String mReserve43 = StringUtils.EMPTY;

	// 特店代碼Merchant ID
	// AN (15) 15
	// bitmap 位置: 44
	private String mMERCHANT_ID = StringUtils.EMPTY;

	// 實際完成交易金額
	// N(36) 36
	// bitmap 位置: 45
	private String mSET_RPAMT = StringUtils.EMPTY;

	// 保留欄位 46
	//
	// bitmap 位置: 46
	private String mReserve46 = StringUtils.EMPTY;

	// 保留欄位 47
	//
	// bitmap 位置: 47
	private String mReserve47 = StringUtils.EMPTY;

	// 保留欄位 48
	//
	// bitmap 位置: 48
	private String mReserve48 = StringUtils.EMPTY;

	// 保留欄位 49
	//
	// bitmap 位置: 49
	private String mReserve49 = StringUtils.EMPTY;

	// 保留欄位 50
	//
	// bitmap 位置: 50
	private String mReserve50 = StringUtils.EMPTY;

	// 保留欄位 51
	//
	// bitmap 位置: 51
	private String mReserve51 = StringUtils.EMPTY;

	// 保留欄位 52
	//
	// bitmap 位置: 52
	private String mReserve52 = StringUtils.EMPTY;

	// 原交易序號
	// AN(10) 10
	// bitmap 位置: 53
	private String mORI_STAN = StringUtils.EMPTY;

	// 保留欄位 54
	//
	// bitmap 位置: 54
	private String mReserve54 = StringUtils.EMPTY;

	// 保留欄位 55
	//
	// bitmap 位置: 55
	private String mReserve55 = StringUtils.EMPTY;

	// 發卡行晶片驗證資料
	// LL+DATA 18
	// bitmap 位置: 56
	private String mISS_CHECKDATA = StringUtils.EMPTY;

	// MASTERCARD驗證結果
	// AN(8) 8
	// bitmap 位置: 57
	private String mIC_CHECKRESULT_M = StringUtils.EMPTY;

	// VISA國際組織驗證結果
	// AN(2) 2
	// bitmap 位置: 58
	private String mIC_CHECKRESULT_V = StringUtils.EMPTY;

	// 保留欄位 59
	//
	// bitmap 位置: 59
	private String mReserve59 = StringUtils.EMPTY;

	// IC卡驗證結果資料
	// B(V)(LL+Data) 257
	// bitmap 位置: 60
	private String mIC_CHECKRESULT = StringUtils.EMPTY;

	// ＩＣ卡序號
	// N(3) 3
	// bitmap 位置: 61
	private String mCARD_SEQ = StringUtils.EMPTY;

	// 授權碼
	// AN(6) 6
	// bitmap 位置: 62
	private String mAUTH_CODE = StringUtils.EMPTY;

	// IC 卡驗證資料
	// B(V)(LL+Data) 187
	// bitmap 位置: 63
	private String mIC_CHECKDATA = StringUtils.EMPTY;

	// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
	// B(32) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

	private DefORI_DATA_EMVIC _OriData;

	public FISC_EMVIC() {
		super();
	}

	public FISC_EMVIC(String fiscFlatfile) {
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
	 * 保留欄位
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 2
	 * </remark>
	 */
	public String getReserve2() {
		return mReserve2;
	}

	public void setReserve2(String value) {
		mReserve2 = value;
		mIndexValue[1] = value;
	}

	/**
	 * 保留欄位 3
	 * 
	 * <remark>
	 * ; bitmap 位置: 3
	 * </remark>
	 */
	public String getReserve3() {
		return mReserve3;
	}

	public void setReserve3(String value) {
		mReserve3 = value;
		mIndexValue[2] = value;
	}

	/**
	 * Track 2 Data
	 * 
	 * <remark>
	 * 37 ; bitmap 位置: 4
	 * </remark>
	 */
	public String getTRK2() {
		return mTRK2;
	}

	public void setTRK2(String value) {
		mTRK2 = value;
		mIndexValue[3] = value;
	}

	/**
	 * 客戶密碼
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 5
	 * </remark>
	 */
	public String getPINBLOCK() {
		return mPINBLOCK;
	}

	public void setPINBLOCK(String value) {
		mPINBLOCK = value;
		mIndexValue[4] = value;
	}

	/**
	 * 代付單位終端機代號
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 6
	 * </remark>
	 */
	public String getATMNO() {
		return mATMNO;
	}

	public void setATMNO(String value) {
		mATMNO = value;
		mIndexValue[5] = value;
	}

	/**
	 * 可用餘額
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 7
	 * </remark>
	 */
	public String getBALA() {
		return mBALA;
	}

	public void setBALA(String value) {
		mBALA = value;
		mIndexValue[6] = value;
	}

	/**
	 * 交易金額
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 8
	 * </remark>
	 */
	public String getTxAmt() {
		return mTX_AMT;
	}

	public void setTxAmt(String value) {
		mTX_AMT = value;
		mIndexValue[7] = value;
	}

	/**
	 * 交易幣別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 9
	 * </remark>
	 */
	public String getCURRENCY() {
		return mCURRENCY;
	}

	public void setCURRENCY(String value) {
		mCURRENCY = value;
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
	 * 保留欄位 11
	 * 
	 * <remark>
	 * ; bitmap 位置: 11
	 * </remark>
	 */
	public String getReserve11() {
		return mReserve11;
	}

	public void setReserve11(String value) {
		mReserve11 = value;
		mIndexValue[10] = value;
	}

	/**
	 * 保留欄位 12
	 * 
	 * <remark>
	 * ; bitmap 位置: 12
	 * </remark>
	 */
	public String getReserve12() {
		return mReserve12;
	}

	public void setReserve12(String value) {
		mReserve12 = value;
		mIndexValue[11] = value;
	}

	/**
	 * POS 狀況代碼
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getPosType() {
		return mPOS_TYPE;
	}

	public void setPosType(String value) {
		mPOS_TYPE = value;
		mIndexValue[12] = value;
	}

	/**
	 * 保留欄位 14
	 * 
	 * <remark>
	 * ; bitmap 位置: 14
	 * </remark>
	 */
	public String getReserve14() {
		return mReserve14;
	}

	public void setReserve14(String value) {
		mReserve14 = value;
		mIndexValue[13] = value;
	}

	/**
	 * 帳戶類別
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 15
	 * </remark>
	 */
	public String getActType() {
		return mACT_TYPE;
	}

	public void setActType(String value) {
		mACT_TYPE = value;
		mIndexValue[14] = value;
	}

	/**
	 * 保留欄位 16
	 * 
	 * <remark>
	 * ; bitmap 位置: 16
	 * </remark>
	 */
	public String getReserve16() {
		return mReserve16;
	}

	public void setReserve16(String value) {
		mReserve16 = value;
		mIndexValue[15] = value;
	}

	/**
	 * 保留欄位 17
	 * 
	 * <remark>
	 * ; bitmap 位置: 17
	 * </remark>
	 */
	public String getReserve17() {
		return mReserve17;
	}

	public void setReserve17(String value) {
		mReserve17 = value;
		mIndexValue[16] = value;
	}

	/**
	 * 保留欄位 18
	 * 
	 * <remark>
	 * ; bitmap 位置: 18
	 * </remark>
	 */
	public String getReserve18() {
		return mReserve18;
	}

	public void setReserve18(String value) {
		mReserve18 = value;
		mIndexValue[17] = value;
	}

	/**
	 * 保留欄位 19
	 * 
	 * <remark>
	 * ; bitmap 位置: 19
	 * </remark>
	 */
	public String getReserve19() {
		return mReserve19;
	}

	public void setReserve19(String value) {
		mReserve19 = value;
		mIndexValue[18] = value;
	}

	/**
	 * 保留欄位 20
	 * 
	 * <remark>
	 * ; bitmap 位置: 20
	 * </remark>
	 */
	public String getReserve20() {
		return mReserve20;
	}

	public void setReserve20(String value) {
		mReserve20 = value;
		mIndexValue[19] = value;
	}

	/**
	 * 保留欄位 21
	 * 
	 * <remark>
	 * ; bitmap 位置: 21
	 * </remark>
	 */
	public String getReserve21() {
		return mReserve21;
	}

	public void setReserve21(String value) {
		mReserve21 = value;
		mIndexValue[20] = value;
	}

	/**
	 * 保留欄位 22
	 * 
	 * <remark>
	 * ; bitmap 位置: 22
	 * </remark>
	 */
	public String getReserve22() {
		return mReserve22;
	}

	public void setReserve22(String value) {
		mReserve22 = value;
		mIndexValue[21] = value;
	}

	/**
	 * 保留欄位 23
	 * 
	 * <remark>
	 * ; bitmap 位置: 23
	 * </remark>
	 */
	public String getReserve23() {
		return mReserve23;
	}

	public void setReserve23(String value) {
		mReserve23 = value;
		mIndexValue[22] = value;
	}

	/**
	 * NETWORK CODE
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getNetworkCode() {
		return mNETWORK_CODE;
	}

	public void setNetworkCode(String value) {
		mNETWORK_CODE = value;
		mIndexValue[23] = value;
	}

	/**
	 * 卡號
	 * 
	 * <remark>
	 * 19 ; bitmap 位置: 25
	 * </remark>
	 */
	public String getPanNo() {
		return mPAN_NO;
	}

	public void setPanNo(String value) {
		mPAN_NO = value;
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
	 * 交易處理費
	 * 
	 * <remark>
	 * 9 ; bitmap 位置: 28
	 * </remark>
	 */
	public String getProcFee() {
		return mPROC_FEE;
	}

	public void setProcFee(String value) {
		mPROC_FEE = value;
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
	 * 原始交易日期
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 30
	 * </remark>
	 */
	public String getOriTxDate() {
		return mORI_TX_DATE;
	}

	public void setOriTxDate(String value) {
		mORI_TX_DATE = value;
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
	 * 保留欄位 35
	 * 
	 * <remark>
	 * ; bitmap 位置: 35
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
	 * CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
	 * 
	 * <remark>
	 * 140 ; bitmap 位置: 36
	 * </remark>
	 */
	public String getOriData() {
		return mORI_DATA;
	}

	public void setOriData(String value) {
		mORI_DATA = value;
		mIndexValue[35] = value;
	}

	/**
	 * 保留欄位 37
	 * 
	 * <remark>
	 * ; bitmap 位置: 37
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
	 * 帳戶餘額
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 38
	 * </remark>
	 */
	public String getBALB() {
		return mBALB;
	}

	public void setBALB(String value) {
		mBALB = value;
		mIndexValue[37] = value;
	}

	/**
	 * 客戶亂碼基碼同步查核欄( PP-KEY SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 39
	 * </remark>
	 */
	public String getSyncPpkey() {
		return mSYNC_PPKEY;
	}

	public void setSyncPpkey(String value) {
		mSYNC_PPKEY = value;
		mIndexValue[38] = value;
	}

	/**
	 * 保留欄位 40
	 * 
	 * <remark>
	 * ; bitmap 位置: 40
	 * </remark>
	 */
	public String getReserve40() {
		return mReserve40;
	}

	public void setReserve40(String value) {
		mReserve40 = value;
		mIndexValue[39] = value;
	}

	/**
	 * 保留欄位 41
	 * 
	 * <remark>
	 * ; bitmap 位置: 41
	 * </remark>
	 */
	public String getReserve41() {
		return mReserve41;
	}

	public void setReserve41(String value) {
		mReserve41 = value;
		mIndexValue[40] = value;
	}

	/**
	 * 保留欄位 42
	 * 
	 * <remark>
	 * ; bitmap 位置: 42
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
	 * 保留欄位 43
	 * 
	 * <remark>
	 * ; bitmap 位置: 43
	 * </remark>
	 */
	public String getReserve43() {
		return mReserve43;
	}

	public void setReserve43(String value) {
		mReserve43 = value;
		mIndexValue[42] = value;
	}

	/**
	 * 特店代碼Merchant ID
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 44
	 * </remark>
	 */
	public String getMerchantId() {
		return mMERCHANT_ID;
	}

	public void setMerchantId(String value) {
		mMERCHANT_ID = value;
		mIndexValue[43] = value;
	}

	/**
	 * 實際完成交易金額
	 * 
	 * <remark>
	 * 36 ; bitmap 位置: 45
	 * </remark>
	 */
	public String getSetRpamt() {
		return mSET_RPAMT;
	}

	public void setSetRpamt(String value) {
		mSET_RPAMT = value;
		mIndexValue[44] = value;
	}

	/**
	 * 保留欄位 46
	 * 
	 * <remark>
	 * ; bitmap 位置: 46
	 * </remark>
	 */
	public String getReserve46() {
		return mReserve46;
	}

	public void setReserve46(String value) {
		mReserve46 = value;
		mIndexValue[45] = value;
	}

	/**
	 * 保留欄位 47
	 * 
	 * <remark>
	 * ; bitmap 位置: 47
	 * </remark>
	 */
	public String getReserve47() {
		return mReserve47;
	}

	public void setReserve47(String value) {
		mReserve47 = value;
		mIndexValue[46] = value;
	}

	/**
	 * 保留欄位 48
	 * 
	 * <remark>
	 * ; bitmap 位置: 48
	 * </remark>
	 */
	public String getReserve48() {
		return mReserve48;
	}

	public void setReserve48(String value) {
		mReserve48 = value;
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
	 * 保留欄位 50
	 * 
	 * <remark>
	 * ; bitmap 位置: 50
	 * </remark>
	 */
	public String getReserve50() {
		return mReserve50;
	}

	public void setReserve50(String value) {
		mReserve50 = value;
		mIndexValue[49] = value;
	}

	/**
	 * 保留欄位 51
	 * 
	 * <remark>
	 * ; bitmap 位置: 51
	 * </remark>
	 */
	public String getReserve51() {
		return mReserve51;
	}

	public void setReserve51(String value) {
		mReserve51 = value;
		mIndexValue[50] = value;
	}

	/**
	 * 保留欄位 52
	 * 
	 * <remark>
	 * ; bitmap 位置: 52
	 * </remark>
	 */
	public String getReserve52() {
		return mReserve52;
	}

	public void setReserve52(String value) {
		mReserve52 = value;
		mIndexValue[51] = value;
	}

	/**
	 * 原交易序號
	 * 
	 * <remark>
	 * 10 ; bitmap 位置: 53
	 * </remark>
	 */
	public String getOriStan() {
		return mORI_STAN;
	}

	public void setOriStan(String value) {
		mORI_STAN = value;
		mIndexValue[52] = value;
	}

	/**
	 * 保留欄位 54
	 * 
	 * <remark>
	 * ; bitmap 位置: 54
	 * </remark>
	 */
	public String getReserve54() {
		return mReserve54;
	}

	public void setReserve54(String value) {
		mReserve54 = value;
		mIndexValue[53] = value;
	}

	/**
	 * 保留欄位 55
	 * 
	 * <remark>
	 * ; bitmap 位置: 55
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
	 * 發卡行晶片驗證資料
	 * 
	 * <remark>
	 * 18 ; bitmap 位置: 56
	 * </remark>
	 */
	public String getIssCheckdata() {
		return mISS_CHECKDATA;
	}

	public void setIssCheckdata(String value) {
		mISS_CHECKDATA = value;
		mIndexValue[55] = value;
	}

	/**
	 * MASTERCARD驗證結果
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 57
	 * </remark>
	 */
	public String getIcCheckresultM() {
		return mIC_CHECKRESULT_M;
	}

	public void setIcCheckresultM(String value) {
		mIC_CHECKRESULT_M = value;
		mIndexValue[56] = value;
	}

	/**
	 * VISA國際組織驗證結果
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 58
	 * </remark>
	 */
	public String getIcCheckresultV() {
		return mIC_CHECKRESULT_V;
	}

	public void setIcCheckresultV(String value) {
		mIC_CHECKRESULT_V = value;
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
	 * IC卡驗證結果資料
	 * 
	 * <remark>
	 * 257 ; bitmap 位置: 60
	 * </remark>
	 */
	public String getIcCheckresult() {
		return mIC_CHECKRESULT;
	}

	public void setIcCheckresult(String value) {
		mIC_CHECKRESULT = value;
		mIndexValue[59] = value;
	}

	/**
	 * ＩＣ卡序號
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 61
	 * </remark>
	 */
	public String getCardSeq() {
		return mCARD_SEQ;
	}

	public void setCardSeq(String value) {
		mCARD_SEQ = value;
		mIndexValue[60] = value;
	}

	/**
	 * 授權碼
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 62
	 * </remark>
	 */
	public String getAuthCode() {
		return mAUTH_CODE;
	}

	public void setAuthCode(String value) {
		mAUTH_CODE = value;
		mIndexValue[61] = value;
	}

	/**
	 * IC 卡驗證資料
	 * 
	 * <remark>
	 * 187 ; bitmap 位置: 63
	 * </remark>
	 */
	public String getIcCheckdata() {
		return mIC_CHECKDATA;
	}

	public void setIcCheckdata(String value) {
		mIC_CHECKDATA = value;
		mIndexValue[62] = value;
	}

	/**
	 * 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
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
					// 保留欄位
					mReserve2 = value;
					break;
				case 2:
					// 保留欄位 3
					mReserve3 = value;
					break;
				case 3:
					// Track 2 Data
					mTRK2 = value;
					break;
				case 4:
					// 客戶密碼
					mPINBLOCK = value;
					break;
				case 5:
					// 代付單位終端機代號
					mATMNO = value;
					break;
				case 6:
					// 可用餘額
					mBALA = value;
					break;
				case 7:
					// 交易金額
					mTX_AMT = value;
					break;
				case 8:
					// 交易幣別
					mCURRENCY = value;
					break;
				case 9:
					// 保留欄位 10
					mReserve10 = value;
					break;
				case 10:
					// 保留欄位 11
					mReserve11 = value;
					break;
				case 11:
					// 保留欄位 12
					mReserve12 = value;
					break;
				case 12:
					// POS 狀況代碼
					mPOS_TYPE = value;
					break;
				case 13:
					// 保留欄位 14
					mReserve14 = value;
					break;
				case 14:
					// 帳戶類別
					mACT_TYPE = value;
					break;
				case 15:
					// 保留欄位 16
					mReserve16 = value;
					break;
				case 16:
					// 保留欄位 17
					mReserve17 = value;
					break;
				case 17:
					// 保留欄位 18
					mReserve18 = value;
					break;
				case 18:
					// 保留欄位 19
					mReserve19 = value;
					break;
				case 19:
					// 保留欄位 20
					mReserve20 = value;
					break;
				case 20:
					// 保留欄位 21
					mReserve21 = value;
					break;
				case 21:
					// 保留欄位 22
					mReserve22 = value;
					break;
				case 22:
					// 保留欄位 23
					mReserve23 = value;
					break;
				case 23:
					// NETWORK CODE
					mNETWORK_CODE = value;
					break;
				case 24:
					// 卡號
					mPAN_NO = value;
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
					// 交易處理費
					mPROC_FEE = value;
					break;
				case 28:
					// 保留欄位 29
					mReserve29 = value;
					break;
				case 29:
					// 原始交易日期
					mORI_TX_DATE = value;
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
					// 保留欄位 35
					mReserve35 = value;
					break;
				case 35:
					// CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
					mORI_DATA = value;
					break;
				case 36:
					// 保留欄位 37
					mReserve37 = value;
					break;
				case 37:
					// 帳戶餘額
					mBALB = value;
					break;
				case 38:
					// 客戶亂碼基碼同步查核欄( PP-KEY SYNC. CHECK ITEM )
					mSYNC_PPKEY = value;
					break;
				case 39:
					// 保留欄位 40
					mReserve40 = value;
					break;
				case 40:
					// 保留欄位 41
					mReserve41 = value;
					break;
				case 41:
					// 保留欄位 42
					mReserve42 = value;
					break;
				case 42:
					// 保留欄位 43
					mReserve43 = value;
					break;
				case 43:
					// 特店代碼Merchant ID
					mMERCHANT_ID = value;
					break;
				case 44:
					// 實際完成交易金額
					setSetRpamt(value);
					break;
				case 45:
					// 保留欄位 46
					mReserve46 = value;
					break;
				case 46:
					// 保留欄位 47
					mReserve47 = value;
					break;
				case 47:
					// 保留欄位 48
					mReserve48 = value;
					break;
				case 48:
					// 保留欄位 49
					mReserve49 = value;
					break;
				case 49:
					// 保留欄位 50
					mReserve50 = value;
					break;
				case 50:
					// 保留欄位 51
					mReserve51 = value;
					break;
				case 51:
					// 保留欄位 52
					mReserve52 = value;
					break;
				case 52:
					// 原交易序號
					mORI_STAN = value;
					break;
				case 53:
					// 保留欄位 54
					mReserve54 = value;
					break;
				case 54:
					// 保留欄位 55
					mReserve55 = value;
					break;
				case 55:
					// 發卡行晶片驗證資料
					mISS_CHECKDATA = value;
					break;
				case 56:
					// MASTERCARD驗證結果
					mIC_CHECKRESULT_M = value;
					break;
				case 57:
					// VISA國際組織驗證結果
					mIC_CHECKRESULT_V = value;
					break;
				case 58:
					// 保留欄位 59
					mReserve59 = value;
					break;
				case 59:
					// IC卡驗證結果資料
					mIC_CHECKRESULT = value;
					break;
				case 60:
					// ＩＣ卡序號
					mCARD_SEQ = value;
					break;
				case 61:
					// 授權碼
					mAUTH_CODE = value;
					break;
				case 62:
					// IC 卡驗證資料
					mIC_CHECKDATA = value;
					break;
				case 63:
					// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
					mMAC = value;
					break;
			}
			mIndexValue[index] = value;
		}
	}

	public DefORI_DATA_EMVIC getORIDATA() {
		return _OriData;
	}

	public void setORIDATA(DefORI_DATA_EMVIC value) {
		_OriData = value;
	}

	public static class DefORI_DATA_EMVIC {
		// 追蹤序號
		private String mRRN = StringUtils.EMPTY;
		// 交易序號
		private String mORI_VISA_STAN = StringUtils.EMPTY;
		// 代理單位代號
		private String mORI_ACQ = StringUtils.EMPTY;
		// 終端機設備代號
		private String mPOS_MODE = StringUtils.EMPTY;
		// 代理單位國別碼
		private String mACQ_CNTRY = StringUtils.EMPTY;
		// 端末設備型態
		private String mMERCHANT_TYPE = StringUtils.EMPTY;
		// 交易傳輸日期時間
		private String mORI_TX_DATETIME = StringUtils.EMPTY;
		// 當地交易日期/時間
		private String mLOC_DATETIME = StringUtils.EMPTY;
		// 清算日期
		private String mSET_DATE_MMDD = StringUtils.EMPTY;
		// 清算金額
		private String mSET_AMT = StringUtils.EMPTY;
		// 清算匯率
		private String mSET_EXRATE = StringUtils.EMPTY;
		// 清算幣別
		private String mSET_CUR = StringUtils.EMPTY;
		// 持卡人扣帳金額
		private String mBILL_AMT = StringUtils.EMPTY;
		// 持卡人扣帳匯率
		private String mBILL_RATE = StringUtils.EMPTY;
		// 持卡人扣帳幣別
		private String mBILL_CUR = StringUtils.EMPTY;
		// 狀況代碼
		private String mRS_CODE = StringUtils.EMPTY;
		// 城市名稱
		private String mCITY_NAME = StringUtils.EMPTY;
		//預借現金交易類別
		private String mORI_TRANSTYPE = StringUtils.EMPTY;
		// 索引變數宣告
		private Object[] mIndexValue = new Object[18];

		/**
		 * 追蹤序號
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getRRN() {
			return mRRN;
		}

		public void setRRN(String value) {
			mRRN = value;
			mIndexValue[0] = value;
		}

		/**
		 * 交易序號
		 * 
		 * <remark>
		 * 長度6
		 * </remark>
		 */
		public String getOriVisaStan() {
			return mORI_VISA_STAN;
		}

		public void setOriVisaStan(String value) {
			mORI_VISA_STAN = value;
			mIndexValue[1] = value;
		}

		/**
		 * 代理單位代號
		 * 
		 * <remark>
		 * 長度11
		 * </remark>
		 */
		public String getOriAcq() {
			return mORI_ACQ;
		}

		public void setOriAcq(String value) {
			mORI_ACQ = value;
			mIndexValue[2] = value;
		}

		/**
		 * 終端機設備代號
		 * 
		 * <remark>
		 * 長度4
		 * </remark>
		 */
		public String getPosMode() {
			return mPOS_MODE;
		}

		public void setPosMode(String value) {
			mPOS_MODE = value;
			mIndexValue[3] = value;
		}

		/**
		 * 代理單位國別碼
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getAcqCntry() {
			return mACQ_CNTRY;
		}

		public void setAcqCntry(String value) {
			mACQ_CNTRY = value;
			mIndexValue[4] = value;
		}

		/**
		 * 端末設備型態
		 * 
		 * <remark>
		 * 長度4
		 * </remark>
		 */
		public String getMerchantType() {
			return mMERCHANT_TYPE;
		}

		public void setMerchantType(String value) {
			mMERCHANT_TYPE = value;
			mIndexValue[5] = value;
		}

		/**
		 * 交易傳輸日期時間
		 * 
		 * <remark>
		 * 長度10
		 * </remark>
		 */
		public String getOriTxDatetime() {
			return mORI_TX_DATETIME;
		}

		public void setOriTxDatetime(String value) {
			mORI_TX_DATETIME = value;
			mIndexValue[6] = value;
		}

		/**
		 * 當地交易日期/時間
		 * 
		 * <remark>
		 * 長度10
		 * </remark>
		 */
		public String getLocDatetime() {
			return mLOC_DATETIME;
		}

		public void setLocDatetime(String value) {
			mLOC_DATETIME = value;
			mIndexValue[7] = value;
		}

		/**
		 * 清算日期
		 * 
		 * <remark>
		 * 長度4
		 * </remark>
		 */
		public String getSetDateMmdd() {
			return mSET_DATE_MMDD;
		}

		public void setSetDateMmdd(String value) {
			mSET_DATE_MMDD = value;
			mIndexValue[8] = value;
		}

		/**
		 * 清算金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getSetAmt() {
			return mSET_AMT;
		}

		public void setSetAmt(String value) {
			mSET_AMT = value;
			mIndexValue[9] = value;
		}

		/**
		 * 清算匯率
		 * 
		 * <remark>
		 * 長度8
		 * </remark>
		 */
		public String getSetExrate() {
			return mSET_EXRATE;
		}

		public void setSetExrate(String value) {
			mSET_EXRATE = value;
			mIndexValue[10] = value;
		}

		/**
		 * 清算幣別
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getSetCur() {
			return mSET_CUR;
		}

		public void setSetCur(String value) {
			mSET_CUR = value;
			mIndexValue[11] = value;
		}

		/**
		 * 持卡人扣帳金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getBillAmt() {
			return mBILL_AMT;
		}

		public void setBillAmt(String value) {
			mBILL_AMT = value;
			mIndexValue[12] = value;
		}

		/**
		 * 持卡人扣帳匯率
		 * 
		 * <remark>
		 * 長度8
		 * </remark>
		 */
		public String getBillRate() {
			return mBILL_RATE;
		}

		public void setBillRate(String value) {
			mBILL_RATE = value;
			mIndexValue[13] = value;
		}

		/**
		 * 持卡人扣帳幣別
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getBillCur() {
			return mBILL_CUR;
		}

		public void setBillCur(String value) {
			mBILL_CUR = value;
			mIndexValue[14] = value;
		}

		/**
		 * 狀況代碼
		 * 
		 * <remark>
		 * 長度2
		 * </remark>
		 */
		public String getRsCode() {
			return mRS_CODE;
		}

		public void setRsCode(String value) {
			mRS_CODE = value;
			mIndexValue[15] = value;
		}

		/**
		 * 城市名稱
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getCityName() {
			return mCITY_NAME;
		}

		public void setCityName(String value) {
			mCITY_NAME = value;
			mIndexValue[16] = value;
		}
		
		/**
		 * 預借現金交易類別
		 * 
		 * <remark>
		 * 長度1
		 * </remark>
		 */
		public String getOriTranstype() {
			return mORI_TRANSTYPE;
		}
		
		public void setOriTranstype(String value) {
			mORI_TRANSTYPE = value;
			mIndexValue[17] = value;
		}
	}
}