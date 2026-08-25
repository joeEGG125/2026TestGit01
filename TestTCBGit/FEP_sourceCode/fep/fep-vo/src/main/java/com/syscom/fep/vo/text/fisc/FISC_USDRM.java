package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

// 財金電文16XX BODY
public class FISC_USDRM extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 帳號( PRIMARY ACCOUNT NUMBER )
	// N( 16 ) 16
	// bitmap 位置: 2
	private String mIN_ACTNO = StringUtils.EMPTY;

	// 交易金額( AMOUNT，TXN. )
	// NS( 14 ) 14
	// bitmap 位置: 3
	private String mTX_AMT = StringUtils.EMPTY;

	// 保留欄位 4
	//
	// bitmap 位置: 4
	private String mReserve4 = StringUtils.EMPTY;

	// 保留欄位 5
	//
	// bitmap 位置: 5
	private String mReserve5 = StringUtils.EMPTY;

	// 保留欄位 6
	//
	// bitmap 位置: 6
	private String mReserve6 = StringUtils.EMPTY;

	// 保留欄位 7
	//
	// bitmap 位置: 7
	private String mReserve7 = StringUtils.EMPTY;

	// 保留欄位 8
	//
	// bitmap 位置: 8
	private String mReserve8 = StringUtils.EMPTY;

	// 通匯序號
	// N( 7 ) 7
	// bitmap 位置: 9
	private String mBANK_NO = StringUtils.EMPTY;

	// 保留欄位 10
	//
	// bitmap 位置: 10
	private String mReserve10 = StringUtils.EMPTY;

	// 保留欄位 11
	//
	// bitmap 位置: 11
	private String mReserve11 = StringUtils.EMPTY;

	// 原通匯序號
	// N( 7 ) 7
	// bitmap 位置: 12
	private String mORG_BANK_NO = StringUtils.EMPTY;

	// 發信行代號
	// N( 7 ) 7
	// bitmap 位置: 13
	private String mSENDER_BANK = StringUtils.EMPTY;

	// 收信行代號( RECEIVING BRANCH-ID )
	// N( 7 ) 7
	// bitmap 位置: 14
	private String mRECEIVER_BANK = StringUtils.EMPTY;

	// 保留欄位 15
	//
	// bitmap 位置: 15
	private String mReserve15 = StringUtils.EMPTY;

	// 保留欄位 16
	//
	// bitmap 位置: 16
	private String mReserve16 = StringUtils.EMPTY;

	// 狀況代號
	// N( 2 ) 2
	// bitmap 位置: 17
	private String mSTATUS = StringUtils.EMPTY;

	// 保留欄位 18
	//
	// bitmap 位置: 18
	private String mReserve18 = StringUtils.EMPTY;

	// 參加單位間交換基碼同步查核欄
	// B( 32 ) 4
	// bitmap 位置: 19
	private String mMB_SYNC = StringUtils.EMPTY;

	// 參加單位間訊息押碼
	// B( 32 ) 4
	// bitmap 位置: 20
	private String mMB_MAC = StringUtils.EMPTY;

	// 保留欄位 21
	//
	// bitmap 位置: 21
	private String mReserve21 = StringUtils.EMPTY;

	// 收款人姓名
	// DATA(70) 70
	// bitmap 位置: 22
	private String mIN_NAME = StringUtils.EMPTY;

	// 匯款人姓名及地址
	// DATA(140) 140
	// bitmap 位置: 23
	private String mOUT_NAME = StringUtils.EMPTY;

	// 附言
	// DATA(70) 70
	// bitmap 位置: 24
	private String mCHINESE_MEMO = StringUtils.EMPTY;

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

	// 外幣匯款別
	// N(3) 3
	// bitmap 位置: 31
	private String mFC_SUBPCODE = StringUtils.EMPTY;

	// 保留欄位 32
	//
	// bitmap 位置: 32
	private String mReserve32 = StringUtils.EMPTY;

	// 匯款日期
	// N( 6 ) 6
	// bitmap 位置: 33
	private String mTX_DATE = StringUtils.EMPTY;

	// 原外幣匯款別
	// N(3) 3
	// bitmap 位置: 34
	private String mORG_SUBPCODE = StringUtils.EMPTY;

	// 原交易類別代碼
	// N( 4 ) 4
	// bitmap 位置: 35
	private String mORG_PCODE = StringUtils.EMPTY;

	// 保留欄位 36
	//
	// bitmap 位置: 36
	private String mReserve36 = StringUtils.EMPTY;

	// 保留欄位 37
	//
	// bitmap 位置: 37
	private String mReserve37 = StringUtils.EMPTY;

	// 發電銀行編號
	// AN(16) 16
	// bitmap 位置: 38
	private String mSENDER_BANK_SEQNO = StringUtils.EMPTY;

	// 保留欄位 39
	//
	// bitmap 位置: 39
	private String mReserve39 = StringUtils.EMPTY;

	// 保留欄位 40
	//
	// bitmap 位置: 40
	private String mReserve40 = StringUtils.EMPTY;

	// 外幣匯款幣別
	// N(3) 3
	// bitmap 位置: 41
	private String mCURRENCY = StringUtils.EMPTY;

	// 保留欄位 42
	//
	// bitmap 位置: 42
	private String mReserve42 = StringUtils.EMPTY;

	// 保留欄位 43
	//
	// bitmap 位置: 43
	private String mReserve43 = StringUtils.EMPTY;

	// 保留欄位 44
	//
	// bitmap 位置: 44
	private String mReserve44 = StringUtils.EMPTY;

	// 保留欄位 45
	//
	// bitmap 位置: 45
	private String mReserve45 = StringUtils.EMPTY;

	// 保留欄位 46
	//
	// bitmap 位置: 46
	private String mReserve46 = StringUtils.EMPTY;

	// 保留欄位 47
	//
	// bitmap 位置: 47
	private String mReserve47 = StringUtils.EMPTY;

	// 外幣匯款附言欄
	// AN(23) 23
	// bitmap 位置: 48
	private String mFC_MEMO = StringUtils.EMPTY;

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

	// 保留欄位 53
	//
	// bitmap 位置: 53
	private String mReserve53 = StringUtils.EMPTY;

	// 保留欄位 54
	//
	// bitmap 位置: 54
	private String mReserve54 = StringUtils.EMPTY;

	// 保留欄位 55
	//
	// bitmap 位置: 55
	private String mReserve55 = StringUtils.EMPTY;

	// 保留欄位 56
	//
	// bitmap 位置: 56
	private String mReserve56 = StringUtils.EMPTY;

	// 保留欄位 57
	//
	// bitmap 位置: 57
	private String mReserve57 = StringUtils.EMPTY;

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

	// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
	// B( 32 ) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

	public FISC_USDRM() {
		super();
	}

	public FISC_USDRM(String fiscFlatfile) {
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
	 * 帳號( PRIMARY ACCOUNT NUMBER )
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 2
	 * </remark>
	 */
	public String getInActno() {
		return mIN_ACTNO;
	}

	public void setInActno(String value) {
		mIN_ACTNO = value;
		mIndexValue[1] = value;
	}

	/**
	 * 交易金額( AMOUNT，TXN. )
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 3
	 * </remark>
	 */
	public String getTxAmt() {
		return mTX_AMT;
	}

	public void setTxAmt(String value) {
		mTX_AMT = value;
		mIndexValue[2] = value;
	}

	/**
	 * 保留欄位 4
	 * 
	 * <remark>
	 * ; bitmap 位置: 4
	 * </remark>
	 */
	public String getReserve4() {
		return mReserve4;
	}

	public void setReserve4(String value) {
		mReserve4 = value;
		mIndexValue[3] = value;
	}

	/**
	 * 保留欄位 5
	 * 
	 * <remark>
	 * ; bitmap 位置: 5
	 * </remark>
	 */
	public String getReserve5() {
		return mReserve5;
	}

	public void setReserve5(String value) {
		mReserve5 = value;
		mIndexValue[4] = value;
	}

	/**
	 * 保留欄位 6
	 * 
	 * <remark>
	 * ; bitmap 位置: 6
	 * </remark>
	 */
	public String getReserve6() {
		return mReserve6;
	}

	public void setReserve6(String value) {
		mReserve6 = value;
		mIndexValue[5] = value;
	}

	/**
	 * 保留欄位 7
	 * 
	 * <remark>
	 * ; bitmap 位置: 7
	 * </remark>
	 */
	public String getReserve7() {
		return mReserve7;
	}

	public void setReserve7(String value) {
		mReserve7 = value;
		mIndexValue[6] = value;
	}

	/**
	 * 保留欄位 8
	 * 
	 * <remark>
	 * ; bitmap 位置: 8
	 * </remark>
	 */
	public String getReserve8() {
		return mReserve8;
	}

	public void setReserve8(String value) {
		mReserve8 = value;
		mIndexValue[7] = value;
	}

	/**
	 * 通匯序號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 9
	 * </remark>
	 */
	public String getBankNo() {
		return mBANK_NO;
	}

	public void setBankNo(String value) {
		mBANK_NO = value;
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
	 * 原通匯序號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 12
	 * </remark>
	 */
	public String getOrgBankNo() {
		return mORG_BANK_NO;
	}

	public void setOrgBankNo(String value) {
		mORG_BANK_NO = value;
		mIndexValue[11] = value;
	}

	/**
	 * 發信行代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getSenderBank() {
		return mSENDER_BANK;
	}

	public void setSenderBank(String value) {
		mSENDER_BANK = value;
		mIndexValue[12] = value;
	}

	/**
	 * 收信行代號( RECEIVING BRANCH-ID )
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 14
	 * </remark>
	 */
	public String getReceiverBank() {
		return mRECEIVER_BANK;
	}

	public void setReceiverBank(String value) {
		mRECEIVER_BANK = value;
		mIndexValue[13] = value;
	}

	/**
	 * 保留欄位 15
	 * 
	 * <remark>
	 * ; bitmap 位置: 15
	 * </remark>
	 */
	public String getReserve15() {
		return mReserve15;
	}

	public void setReserve15(String value) {
		mReserve15 = value;
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
	 * 狀況代號
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 17
	 * </remark>
	 */
	public String getSTATUS() {
		return mSTATUS;
	}

	public void setSTATUS(String value) {
		mSTATUS = value;
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
	 * 參加單位間交換基碼同步查核欄
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 19
	 * </remark>
	 */
	public String getMbSync() {
		return mMB_SYNC;
	}

	public void setMbSync(String value) {
		mMB_SYNC = value;
		mIndexValue[18] = value;
	}

	/**
	 * 參加單位間訊息押碼
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 20
	 * </remark>
	 */
	public String getMbMac() {
		return mMB_MAC;
	}

	public void setMbMac(String value) {
		mMB_MAC = value;
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
	 * 收款人姓名
	 * 
	 * <remark>
	 * 70 ; bitmap 位置: 22
	 * </remark>
	 */
	public String getInName() {
		return mIN_NAME;
	}

	public void setInName(String value) {
		mIN_NAME = value;
		mIndexValue[21] = value;
	}

	/**
	 * 匯款人姓名及地址
	 * 
	 * <remark>
	 * 140 ; bitmap 位置: 23
	 * </remark>
	 */
	public String getOutName() {
		return mOUT_NAME;
	}

	public void setOutName(String value) {
		mOUT_NAME = value;
		mIndexValue[22] = value;
	}

	/**
	 * 附言
	 * 
	 * <remark>
	 * 70 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getChineseMemo() {
		return mCHINESE_MEMO;
	}

	public void setChineseMemo(String value) {
		mCHINESE_MEMO = value;
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
	 * 外幣匯款別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 31
	 * </remark>
	 */
	public String getFcSubpcode() {
		return mFC_SUBPCODE;
	}

	public void setFcSubpcode(String value) {
		mFC_SUBPCODE = value;
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
	 * 匯款日期
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 33
	 * </remark>
	 */
	public String getTxDate() {
		return mTX_DATE;
	}

	public void setTxDate(String value) {
		mTX_DATE = value;
		mIndexValue[32] = value;
	}

	/**
	 * 原外幣匯款別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 34
	 * </remark>
	 */
	public String getOrgSubpcode() {
		return mORG_SUBPCODE;
	}

	public void setOrgSubpcode(String value) {
		mORG_SUBPCODE = value;
		mIndexValue[33] = value;
	}

	/**
	 * 原交易類別代碼
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 35
	 * </remark>
	 */
	public String getOrgPcode() {
		return mORG_PCODE;
	}

	public void setOrgPcode(String value) {
		mORG_PCODE = value;
		mIndexValue[34] = value;
	}

	/**
	 * 保留欄位 36
	 * 
	 * <remark>
	 * ; bitmap 位置: 36
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
	 * 發電銀行編號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 38
	 * </remark>
	 */
	public String getSenderBankSeqno() {
		return mSENDER_BANK_SEQNO;
	}

	public void setSenderBankSeqno(String value) {
		mSENDER_BANK_SEQNO = value;
		mIndexValue[37] = value;
	}

	/**
	 * 保留欄位 39
	 * 
	 * <remark>
	 * ; bitmap 位置: 39
	 * </remark>
	 */
	public String getReserve39() {
		return mReserve39;
	}

	public void setReserve39(String value) {
		mReserve39 = value;
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
	 * 外幣匯款幣別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 41
	 * </remark>
	 */
	public String getCURRENCY() {
		return mCURRENCY;
	}

	public void setCURRENCY(String value) {
		mCURRENCY = value;
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
	 * 保留欄位 44
	 * 
	 * <remark>
	 * ; bitmap 位置: 44
	 * </remark>
	 */
	public String getReserve44() {
		return mReserve44;
	}

	public void setReserve44(String value) {
		mReserve44 = value;
		mIndexValue[43] = value;
	}

	/**
	 * 保留欄位 45
	 * 
	 * <remark>
	 * ; bitmap 位置: 45
	 * </remark>
	 */
	public String getReserve45() {
		return mReserve45;
	}

	public void setReserve45(String value) {
		mReserve45 = value;
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
	 * 外幣匯款附言欄
	 * 
	 * <remark>
	 * 23 ; bitmap 位置: 48
	 * </remark>
	 */
	public String getFcMemo() {
		return mFC_MEMO;
	}

	public void setFcMemo(String value) {
		mFC_MEMO = value;
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
	 * 保留欄位 53
	 * 
	 * <remark>
	 * ; bitmap 位置: 53
	 * </remark>
	 */
	public String getReserve53() {
		return mReserve53;
	}

	public void setReserve53(String value) {
		mReserve53 = value;
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
	 * 保留欄位 56
	 * 
	 * <remark>
	 * ; bitmap 位置: 56
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
	 * 保留欄位 57
	 * 
	 * <remark>
	 * ; bitmap 位置: 57
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
					// 帳號( PRIMARY ACCOUNT NUMBER )
					mIN_ACTNO = value;
					break;
				case 2:
					// 交易金額( AMOUNT，TXN. )
					mTX_AMT = value;
					break;
				case 3:
					// 保留欄位 4
					mReserve4 = value;
					break;
				case 4:
					// 保留欄位 5
					mReserve5 = value;
					break;
				case 5:
					// 保留欄位 6
					mReserve6 = value;
					break;
				case 6:
					// 保留欄位 7
					mReserve7 = value;
					break;
				case 7:
					// 保留欄位 8
					mReserve8 = value;
					break;
				case 8:
					// 通匯序號
					mBANK_NO = value;
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
					// 原通匯序號
					mORG_BANK_NO = value;
					break;
				case 12:
					// 發信行代號
					mSENDER_BANK = value;
					break;
				case 13:
					// 收信行代號( RECEIVING BRANCH-ID )
					mRECEIVER_BANK = value;
					break;
				case 14:
					// 保留欄位 15
					mReserve15 = value;
					break;
				case 15:
					// 保留欄位 16
					mReserve16 = value;
					break;
				case 16:
					// 狀況代號
					mSTATUS = value;
					break;
				case 17:
					// 保留欄位 18
					mReserve18 = value;
					break;
				case 18:
					// 參加單位間交換基碼同步查核欄
					mMB_SYNC = value;
					break;
				case 19:
					// 參加單位間訊息押碼
					mMB_MAC = value;
					break;
				case 20:
					// 保留欄位 21
					mReserve21 = value;
					break;
				case 21:
					// 收款人姓名
					mIN_NAME = value;
					break;
				case 22:
					// 匯款人姓名及地址
					mOUT_NAME = value;
					break;
				case 23:
					// 附言
					mCHINESE_MEMO = value;
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
					// 外幣匯款別
					mFC_SUBPCODE = value;
					break;
				case 31:
					// 保留欄位 32
					mReserve32 = value;
					break;
				case 32:
					// 匯款日期
					mTX_DATE = value;
					break;
				case 33:
					// 原外幣匯款別
					mORG_SUBPCODE = value;
					break;
				case 34:
					// 原交易類別代碼
					mORG_PCODE = value;
					break;
				case 35:
					// 保留欄位 36
					mReserve36 = value;
					break;
				case 36:
					// 保留欄位 37
					mReserve37 = value;
					break;
				case 37:
					// 發電銀行編號
					mSENDER_BANK_SEQNO = value;
					break;
				case 38:
					// 保留欄位 39
					mReserve39 = value;
					break;
				case 39:
					// 保留欄位 40
					mReserve40 = value;
					break;
				case 40:
					// 外幣匯款幣別
					mCURRENCY = value;
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
					// 保留欄位 44
					mReserve44 = value;
					break;
				case 44:
					// 保留欄位 45
					mReserve45 = value;
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
					// 外幣匯款附言欄
					mFC_MEMO = value;
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
					// 保留欄位 53
					mReserve53 = value;
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
					// 保留欄位 56
					mReserve56 = value;
					break;
				case 56:
					// 保留欄位 57
					mReserve57 = value;
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
					// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
					mMAC = value;
					break;
			}
			mIndexValue[index] = value;
		}
	}
}
