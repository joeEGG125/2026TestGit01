package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;

// 財金電文2XXX BODY
public class FISC_INBK extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 保留欄位
	// N( 14 ) 14
	// bitmap 位置: 2
	private String mReserve2 = StringUtils.EMPTY;

	// 交易金額( AMOUNT，TXN. )
	// NS( 14 ) 14
	// bitmap 位置: 3
	private String mTX_AMT = StringUtils.EMPTY;

	// 磁軌資料內容 ( TRACK 1 、 2 、 3 DATA )
	// AN( 104 ) 104
	// bitmap 位置: 4
	private String mTRK2 = StringUtils.EMPTY;

	// 客戶密碼( PIN，PERSONAL IDENTIFICATION NUMBER )
	// B( 64 ) 8
	// bitmap 位置: 5
	private String mPINBLOCK = StringUtils.EMPTY;

	// 代付單位 CD/ATM 代號( CARD ACCEPTOR TERMINAL ID. )
	// AN( 8 ) 8
	// bitmap 位置: 6
	private String mATMNO = StringUtils.EMPTY;

	// 存戶可用餘額( AMOUNT，AVAILABLE BALANCE )
	// NS( 14 ) 14
	// bitmap 位置: 7
	private String mBALA = StringUtils.EMPTY;

	// 預先授權交易金額
	// NS(14 ) 14
	// bitmap 位置: 8
	private String mTX_AMT_PREAUTH = StringUtils.EMPTY;

	// IC卡交易序號
	// N( 8 ) 8
	// bitmap 位置: 9
	private String mIC_SEQNO = StringUtils.EMPTY;

	// 代收編號
	// N( 12 ) 12
	// bitmap 位置: 10
	private String mFISC_RRN = StringUtils.EMPTY;

	// 端末設備查核碼
	// AN(8 ) 8
	// bitmap 位置: 11
	private String mATM_CHK = StringUtils.EMPTY;

	// 預先授權IC卡交易序號
	// N(8 ) 8
	// bitmap 位置: 12
	private String mIC_SEQNO_PREAUTH = StringUtils.EMPTY;

	// 發信行代號
	// N( 7 ) 7
	// bitmap 位置: 13
	private String mTRIN_BKNO = StringUtils.EMPTY;

	// 收信行代號( RECEIVING BRANCH-ID )
	// N( 7 ) 7
	// bitmap 位置: 14
	private String mTROUT_BKNO = StringUtils.EMPTY;

	// 跨行手續費( HANDLING CHARGE )
	// N( 4 ) 4
	// bitmap 位置: 15
	private String mFEE_AMT = StringUtils.EMPTY;

	// 件數
	// N( 7 ) 7
	// bitmap 位置: 16
	private String mCOUNT = StringUtils.EMPTY;

	// 狀況代號
	// N( 2 ) 2
	// bitmap 位置: 17
	private String mRS_CODE = StringUtils.EMPTY;

	// 轉帳類別／繳納款項類別
	// N( 3 ) 3
	// bitmap 位置: 18
	private String mMODE = StringUtils.EMPTY;

	// 交易日期時間
	// N( 14 ) 14
	// bitmap 位置: 19
	private String mTX_DATETIME_FISC = StringUtils.EMPTY;

	// 預先授權交易日期時間
	// N( 14 ) 14
	// bitmap 位置: 20
	private String mTX_DATETIME_PREAUTH = StringUtils.EMPTY;

	// 訂單號碼
	// AN(16) 16
	// bitmap 位置: 21
	private String mORDER_NO = StringUtils.EMPTY;

	// 促銷應用訊息
	// AN(16) 16
	// bitmap 位置: 22
	private String mPROM_MSG = StringUtils.EMPTY;

	// 有效日期/活動型態
	// N(4) 4
	// bitmap 位置: 23
	private String mACT_TYPE = StringUtils.EMPTY;

	// 收款人姓名
	// B640 80
	// bitmap 位置: 24
	private String mTO_NAME = StringUtils.EMPTY;

	// 電子商務指標(ElectronicCommerceIndicator)
	// AN1(1) 1
	// bitmap 位置: 25
	private String mECI = StringUtils.EMPTY;

	// 身份証統一編號( ID. NUMBER )
	// AN(10) 10
	// bitmap 位置: 26
	private String mReserve26 = StringUtils.EMPTY;

	// 營利事業統一編號
	// N( 8 ) 8
	// bitmap 位置: 27
	private String mBUSINESS_UNIT = StringUtils.EMPTY;

	// 端末設備型態
	// AN( 4 ) 4
	// bitmap 位置: 28
	private String mATM_TYPE = StringUtils.EMPTY;

	// 付款人姓名
	// B640 80
	// bitmap 位置: 29
	private String mFROM_NAME = StringUtils.EMPTY;

	// 指定提示日期
	// N( 6 ) 6
	// bitmap 位置: 30
	private String mDUE_DATE = StringUtils.EMPTY;

	// 附言
	// B640 80
	// bitmap 位置: 31
	private String mFXML_MEMO = StringUtils.EMPTY;

	// 入／扣帳日
	// N( 6 ) 6
	// bitmap 位置: 32
	private String mBUSINESS_DATE = StringUtils.EMPTY;

	// 保留欄位 33
	//
	// bitmap 位置: 33
	private String mReserve33 = StringUtils.EMPTY;

	// 保留欄位 34
	//
	// bitmap 位置: 34
	private String mReserve34 = StringUtils.EMPTY;

	// 晶片金融卡跨國作業資料
	// AN( 70 ) 70
	// bitmap 位置: 35
	private String mIC_DATA = StringUtils.EMPTY;

	// CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
	// AN( 195 ) 195
	// bitmap 位置: 36
	private String mORI_DATA = StringUtils.EMPTY;

	// 國際信用卡授權交易之原始資料
	// AN( 61 ) 61
	// bitmap 位置: 37
	private String mReserve37 = StringUtils.EMPTY;

	// 存戶帳戶餘額 ( AMOUNT，A/C BALANCE ) 或 CD/ATM 實際交易付款金額
	// NS( 14 ) 14
	// bitmap 位置: 38
	private String mBALB = StringUtils.EMPTY;

	// 客戶亂碼基碼同步查核欄( PP-KEY SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 39
	private String mSYNC_PPKEY = StringUtils.EMPTY;

	// 繳款類別
	// AN (5) 5
	// bitmap 位置: 40
	private String mPAYTYPE = StringUtils.EMPTY;

	// 稽徵機關
	// AN (3) 3
	// bitmap 位置: 41
	private String mTAX_UNIT = StringUtils.EMPTY;

	// 身份証號／營利事業統一編號(IDN/BAN)
	// AN (11) 11
	// bitmap 位置: 42
	private String mIDNO = StringUtils.EMPTY;

	// REC
	// AN (400) 400
	// bitmap 位置: 43
	private String mReserve43 = StringUtils.EMPTY;

	// 特店代碼Merchant ID
	// AN (30) 30
	// bitmap 位置: 44
	private String mMERCHANT_ID = StringUtils.EMPTY;

	// 中文附言欄
	// AN (40) 40
	// bitmap 位置: 45
	private String mCHREM = StringUtils.EMPTY;

	// 附言欄
	// DATA 200
	// bitmap 位置: 46
	private String mMEMO = StringUtils.EMPTY;

	// 收款人身分證/統編
	// AN (12) 11
	// bitmap 位置: 47
	private String mTO_IDNO = StringUtils.EMPTY;

	// 附言欄
	// AN (40) 40
	// bitmap 位置: 48
	private String mREMARK = StringUtils.EMPTY;

	// 繳費作業手續費
	// N (4) 4
	// bitmap 位置: 49
	private String mNPS_FEE = StringUtils.EMPTY;

	// 該單位應收手續費
	// N (20) 20
	// bitmap 位置: 50
	private String mNPS_FEE_ALL = StringUtils.EMPTY;

	// 轉入帳號
	// AN(16) 16
	// bitmap 位置: 51
	private String mTRIN_ACTNO = StringUtils.EMPTY;

	// 轉出帳號
	// AN(16) 16
	// bitmap 位置: 52
	private String mTROUT_ACTNO = StringUtils.EMPTY;

	// 原交易序號
	// N(10) 10
	// bitmap 位置: 53
	private String mORI_STAN = StringUtils.EMPTY;

	// 銷帳編號
	// AN(16) 16
	// bitmap 位置: 54
	private String mRECON_SEQNO = StringUtils.EMPTY;

	// IC卡備註欄
	// B(240) 30
	// bitmap 位置: 55
	private String mICMARK = StringUtils.EMPTY;

	// 交易驗證碼
	// B(V)(LL+Data)
	// bitmap 位置: 56
	private String mTAC = StringUtils.EMPTY;

	// 帳戶補充資訊
	// N12 12
	// bitmap 位置: 57
	private String mACCT_SUP = StringUtils.EMPTY;

	// 圈存金額
	// N(8) 8
	// bitmap 位置: 58
	private String mReserve58 = StringUtils.EMPTY;

	// 交易累計金額
	// N(8) 8
	// bitmap 位置: 59
	private String mReserve59 = StringUtils.EMPTY;

	// 交易金額(二)
	// N(8) 8
	// bitmap 位置: 60
	private String mReserve60 = StringUtils.EMPTY;

	// ＩＣ卡卡號或卡片國際組織網路識別資料
	// AN(22) 22
	// bitmap 位置: 61
	private String mNETWK_DATA = StringUtils.EMPTY;

	// 交易序號
	// N(6) 6
	// bitmap 位置: 62
	private String mReserve62 = StringUtils.EMPTY;

	// 授權碼
	// AN(8) 8
	// bitmap 位置: 63
	private String mAUTH_CODE = StringUtils.EMPTY;

	// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
	// B( 32 ) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

	private DefORI_DATA _OriData;
	private DefIC_DATA _IcData;
	private DefOB_DATA _ObData;

	public FISC_INBK() {
		super();
	}

	public FISC_INBK(String fiscFlatfile) {
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
	 * 磁軌資料內容 ( TRACK 1 、 2 、 3 DATA )
	 * 
	 * <remark>
	 * 104 ; bitmap 位置: 4
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
	 * 客戶密碼( PIN，PERSONAL IDENTIFICATION NUMBER )
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
	 * 代付單位 CD/ATM 代號( CARD ACCEPTOR TERMINAL ID. )
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
	 * 存戶可用餘額( AMOUNT，AVAILABLE BALANCE )
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
	 * 預先授權交易金額
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 8
	 * </remark>
	 */
	public String getTxAmtPreauth() {
		return mTX_AMT_PREAUTH;
	}

	public void setTxAmtPreauth(String value) {
		mTX_AMT_PREAUTH = value;
		mIndexValue[7] = value;
	}

	/**
	 * IC卡交易序號
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 9
	 * </remark>
	 */
	public String getIcSeqno() {
		return mIC_SEQNO;
	}

	public void setIcSeqno(String value) {
		mIC_SEQNO = value;
		mIndexValue[8] = value;
	}

	/**
	 * 代收編號
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 10
	 * </remark>
	 */
	public String getFiscRrn() {
		return mFISC_RRN;
	}

	public void setFiscRrn(String value) {
		mFISC_RRN = value;
		mIndexValue[9] = value;
	}

	/**
	 * 端末設備查核碼
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 11
	 * </remark>
	 */
	public String getAtmChk() {
		return mATM_CHK;
	}

	public void setAtmChk(String value) {
		mATM_CHK = value;
		mIndexValue[10] = value;
	}

	/**
	 * 預先授權IC卡交易序號
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 12
	 * </remark>
	 */
	public String getIcSeqnoPreauth() {
		return mIC_SEQNO_PREAUTH;
	}

	public void setIcSeqnoPreauth(String value) {
		mIC_SEQNO_PREAUTH = value;
		mIndexValue[11] = value;
	}

	/**
	 * 發信行代號
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getTrinBkno() {
		return mTRIN_BKNO;
	}

	public void setTrinBkno(String value) {
		mTRIN_BKNO = value;
		mIndexValue[12] = value;
	}

	/**
	 * 收信行代號( RECEIVING BRANCH-ID )
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 14
	 * </remark>
	 */
	public String getTroutBkno() {
		return mTROUT_BKNO;
	}

	public void setTroutBkno(String value) {
		mTROUT_BKNO = value;
		mIndexValue[13] = value;
	}

	/**
	 * 跨行手續費( HANDLING CHARGE )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 15
	 * </remark>
	 */
	public String getFeeAmt() {
		return mFEE_AMT;
	}

	public void setFeeAmt(String value) {
		mFEE_AMT = value;
		mIndexValue[14] = value;
	}

	/**
	 * 件數
	 * 
	 * <remark>
	 * 7 ; bitmap 位置: 16
	 * </remark>
	 */
	public String getCOUNT() {
		return mCOUNT;
	}

	public void setCOUNT(String value) {
		mCOUNT = value;
		mIndexValue[15] = value;
	}

	/**
	 * 狀況代號
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 17
	 * </remark>
	 */
	public String getRsCode() {
		return mRS_CODE;
	}

	public void setRsCode(String value) {
		mRS_CODE = value;
		mIndexValue[16] = value;
	}

	/**
	 * 轉帳類別／繳納款項類別
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 18
	 * </remark>
	 */
	public String getMODE() {
		return mMODE;
	}

	public void setMODE(String value) {
		mMODE = value;
		mIndexValue[17] = value;
	}

	/**
	 * 交易日期時間
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 19
	 * </remark>
	 */
	public String getTxDatetimeFisc() {
		return mTX_DATETIME_FISC;
	}

	public void setTxDatetimeFisc(String value) {
		mTX_DATETIME_FISC = value;
		mIndexValue[18] = value;
	}

	/**
	 * 預先授權交易日期時間
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 20
	 * </remark>
	 */
	public String getTxDatetimePreauth() {
		return mTX_DATETIME_PREAUTH;
	}

	public void setTxDatetimePreauth(String value) {
		mTX_DATETIME_PREAUTH = value;
		mIndexValue[19] = value;
	}

	/**
	 * 訂單號碼
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 21
	 * </remark>
	 */
	public String getOrderNo() {
		return mORDER_NO;
	}

	public void setOrderNo(String value) {
		mORDER_NO = value;
		mIndexValue[20] = value;
	}

	/**
	 * 促銷應用訊息
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 22
	 * </remark>
	 */
	public String getPromMsg() {
		return mPROM_MSG;
	}

	public void setPromMsg(String value) {
		mPROM_MSG = value;
		mIndexValue[21] = value;
	}

	/**
	 * 有效日期/活動型態
	 * 
	 * <remark>
	 * ; bitmap 位置: 23
	 * </remark>
	 */
	public String getActType() {
		return mACT_TYPE;
	}

	public void setActType(String value) {
		mACT_TYPE = value;
		mIndexValue[22] = value;
	}

	/**
	 * 收款人姓名
	 * 
	 * <remark>
	 * 80 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getTO_NAME() {
		return mTO_NAME;
	}

	public void setTO_NAME(String value) {
		mTO_NAME = value;
		mIndexValue[23] = value;
	}

	/**
	 * 電子商務指標(ElectronicCommerceIndicator)
	 * 
	 * <remark>
	 * 1 ; bitmap 位置: 25
	 * </remark>
	 */
	public String getECI() {
		return mECI;
	}

	public void setECI(String value) {
		mECI = value;
		mIndexValue[24] = value;
	}

	/**
	 * 身份証統一編號( ID. NUMBER )
	 * 
	 * <remark>
	 * 10 ; bitmap 位置: 26
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
	 * 營利事業統一編號
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 27
	 * </remark>
	 */
	public String getBusinessUnit() {
		return mBUSINESS_UNIT;
	}

	public void setBusinessUnit(String value) {
		mBUSINESS_UNIT = value;
		mIndexValue[26] = value;
	}

	/**
	 * 端末設備型態
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 28
	 * </remark>
	 */
	public String getAtmType() {
		return mATM_TYPE;
	}

	public void setAtmType(String value) {
		mATM_TYPE = value;
		mIndexValue[27] = value;
	}

	/**
	 * 付款人姓名
	 * 
	 * <remark>
	 * 80 ; bitmap 位置: 29
	 * </remark>
	 */
	public String getFROM_NAME() {
		return mFROM_NAME;
	}

	public void setFROM_NAME(String value) {
		mFROM_NAME = value;
		mIndexValue[28] = value;
	}

	/**
	 * 指定提示日期
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 30
	 * </remark>
	 */
	public String getDueDate() {
		return mDUE_DATE;
	}

	public void setDueDate(String value) {
		mDUE_DATE = value;
		mIndexValue[29] = value;
	}

	/**
	 * 附言
	 * 
	 * <remark>
	 * 80 ; bitmap 位置: 31
	 * </remark>
	 */
	public String getFXML_MEMO() {
		return mFXML_MEMO;
	}

	public void setFXML_MEMO(String value) {
		mFXML_MEMO = value;
		mIndexValue[30] = value;
	}

	/**
	 * 入／扣帳日
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 32
	 * </remark>
	 */
	public String getBusinessDate() {
		return mBUSINESS_DATE;
	}

	public void setBusinessDate(String value) {
		mBUSINESS_DATE = value;
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
	 * 晶片金融卡跨國作業資料
	 * 
	 * <remark>
	 * 70 ; bitmap 位置: 35
	 * </remark>
	 */
	public String getIcData() {
		return mIC_DATA;
	}

	public void setIcData(String value) {
		mIC_DATA = value;
		mIndexValue[34] = value;
	}

	/**
	 * CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
	 * 
	 * <remark>
	 * 195 ; bitmap 位置: 36
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
	 * 國際信用卡授權交易之原始資料
	 * 
	 * <remark>
	 * 61 ; bitmap 位置: 37
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
	 * 存戶帳戶餘額 ( AMOUNT，A/C BALANCE ) 或 CD/ATM 實際交易付款金額
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
	 * 繳款類別
	 * 
	 * <remark>
	 * 5 ; bitmap 位置: 40
	 * </remark>
	 */
	public String getPAYTYPE() {
		return mPAYTYPE;
	}

	public void setPAYTYPE(String value) {
		mPAYTYPE = value;
		mIndexValue[39] = value;
	}

	/**
	 * 稽徵機關
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 41
	 * </remark>
	 */
	public String getTaxUnit() {
		return mTAX_UNIT;
	}

	public void setTaxUnit(String value) {
		mTAX_UNIT = value;
		mIndexValue[40] = value;
	}

	/**
	 * 身份証號／營利事業統一編號(IDN/BAN)
	 * 
	 * <remark>
	 * 11 ; bitmap 位置: 42
	 * </remark>
	 */
	public String getIDNO() {
		return mIDNO;
	}

	public void setIDNO(String value) {
		mIDNO = value;
		mIndexValue[41] = value;
	}

	/**
	 * REC
	 * 
	 * <remark>
	 * 400 ; bitmap 位置: 43
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
	 * 30 ; bitmap 位置: 44
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
	 * 中文附言欄
	 * 
	 * <remark>
	 * 40 ; bitmap 位置: 45
	 * </remark>
	 */
	public String getCHREM() {
		return mCHREM;
	}

	public void setCHREM(String value) {
		mCHREM = value;
		mIndexValue[44] = value;
	}

	/**
	 * 附言欄
	 * 
	 * <remark>
	 * 200 ; bitmap 位置: 46
	 * </remark>
	 */
	public String getMEMO() {
		return mMEMO;
	}

	public void setMEMO(String value) {
		mMEMO = value;
		mIndexValue[45] = value;
	}

	/**
	 * 收款人身分證/統編
	 * 
	 * <remark>
	 * 11 ; bitmap 位置: 47
	 * </remark>
	 */
	public String getTO_IDNO() {
		return mTO_IDNO;
	}

	public void setTO_IDNO(String value) {
		mTO_IDNO = value;
		mIndexValue[46] = value;
	}

	/**
	 * 附言欄
	 * 
	 * <remark>
	 * 40 ; bitmap 位置: 48
	 * </remark>
	 */
	public String getREMARK() {
		return mREMARK;
	}

	public void setREMARK(String value) {
		mREMARK = value;
		mIndexValue[47] = value;
	}

	/**
	 * 繳費作業手續費
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 49
	 * </remark>
	 */
	public String getNpsFee() {
		return mNPS_FEE;
	}

	public void setNpsFee(String value) {
		mNPS_FEE = value;
		mIndexValue[48] = value;
	}

	/**
	 * 該單位應收手續費
	 * 
	 * <remark>
	 * 20 ; bitmap 位置: 50
	 * </remark>
	 */
	public String getNpsFeeAll() {
		return mNPS_FEE_ALL;
	}

	public void setNpsFeeAll(String value) {
		mNPS_FEE_ALL = value;
		mIndexValue[49] = value;
	}

	/**
	 * 轉入帳號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 51
	 * </remark>
	 */
	public String getTrinActno() {
		return mTRIN_ACTNO;
	}

	public void setTrinActno(String value) {
		mTRIN_ACTNO = value;
		mIndexValue[50] = value;
	}

	/**
	 * 轉出帳號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 52
	 * </remark>
	 */
	public String getTroutActno() {
		return mTROUT_ACTNO;
	}

	public void setTroutActno(String value) {
		mTROUT_ACTNO = value;
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
	 * 銷帳編號
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 54
	 * </remark>
	 */
	public String getReconSeqno() {
		return mRECON_SEQNO;
	}

	public void setReconSeqno(String value) {
		mRECON_SEQNO = value;
		mIndexValue[53] = value;
	}

	/**
	 * IC卡備註欄
	 * 
	 * <remark>
	 * 30 ; bitmap 位置: 55
	 * </remark>
	 */
	public String getICMARK() {
		return mICMARK;
	}

	public void setICMARK(String value) {
		mICMARK = value;
		mIndexValue[54] = value;
	}

	/**
	 * 交易驗證碼
	 * 
	 * <remark>
	 * ; bitmap 位置: 56
	 * </remark>
	 */
	public String getTAC() {
		return mTAC;
	}

	public void setTAC(String value) {
		mTAC = value;
		mIndexValue[55] = value;
	}

	/**
	 * 帳戶補充資訊
	 * 
	 * <remark>
	 * 12 ; bitmap 位置: 57
	 * </remark>
	 */
	public String getAcctSup() {
		return mACCT_SUP;
	}

	public void setAcctSup(String value) {
		mACCT_SUP = value;
		mIndexValue[56] = value;
	}

	/**
	 * 圈存金額
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 58
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
	 * 交易累計金額
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 59
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
	 * 交易金額(二)
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 60
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
	 * ＩＣ卡卡號或卡片國際組織網路識別資料
	 * 
	 * <remark>
	 * 22 ; bitmap 位置: 61
	 * </remark>
	 */
	public String getNetwkData() {
		return mNETWK_DATA;
	}

	public void setNetwkData(String value) {
		mNETWK_DATA = value;
		mIndexValue[60] = value;
	}

	/**
	 * 交易序號
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 62
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
	 * 授權碼
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 63
	 * </remark>
	 */
	public String getAuthCode() {
		return mAUTH_CODE;
	}

	public void setAuthCode(String value) {
		mAUTH_CODE = value;
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
					// 交易金額( AMOUNT，TXN. )
					mTX_AMT = value;
					break;
				case 3:
					// 磁軌資料內容 ( TRACK 1 、 2 、 3 DATA )
					mTRK2 = value;
					break;
				case 4:
					// 客戶密碼( PIN，PERSONAL IDENTIFICATION NUMBER )
					mPINBLOCK = value;
					break;
				case 5:
					// 代付單位 CD/ATM 代號( CARD ACCEPTOR TERMINAL ID. )
					mATMNO = value;
					break;
				case 6:
					// 存戶可用餘額( AMOUNT，AVAILABLE BALANCE )
					mBALA = value;
					break;
				case 7:
					// 預先授權交易金額
					mTX_AMT_PREAUTH = value;
					break;
				case 8:
					// IC卡交易序號
					mIC_SEQNO = value;
					break;
				case 9:
					// 代收編號
					mFISC_RRN = value;
					break;
				case 10:
					// 端末設備查核碼
					mATM_CHK = value;
					break;
				case 11:
					// 預先授權IC卡交易序號
					mIC_SEQNO_PREAUTH = value;
					break;
				case 12:
					// 發信行代號
					mTRIN_BKNO = value;
					break;
				case 13:
					// 收信行代號( RECEIVING BRANCH-ID )
					mTROUT_BKNO = value;
					break;
				case 14:
					// 跨行手續費( HANDLING CHARGE )
					mFEE_AMT = value;
					break;
				case 15:
					// 件數
					mCOUNT = value;
					break;
				case 16:
					// 狀況代號
					mRS_CODE = value;
					break;
				case 17:
					// 轉帳類別／繳納款項類別
					mMODE = value;
					break;
				case 18:
					// 交易日期時間
					mTX_DATETIME_FISC = value;
					break;
				case 19:
					// 預先授權交易日期時間
					mTX_DATETIME_PREAUTH = value;
					break;
				case 20:
					// 訂單號碼
					mORDER_NO = value;
					break;
				case 21:
					// 促銷應用訊息
					mPROM_MSG = value;
					break;
				case 22:
					// 有效日期/活動型態
					mACT_TYPE = value;
					break;
				case 23:
					// 收款人姓名
					mTO_NAME = value;
					break;
				case 24:
					// 電子商務指標(ElectronicCommerceIndicator)
					mECI = value;
					break;
				case 25:
					// 身份証統一編號( ID. NUMBER )
					mReserve26 = value;
					break;
				case 26:
					// 營利事業統一編號
					mBUSINESS_UNIT = value;
					break;
				case 27:
					// 端末設備型態
					mATM_TYPE = value;
					break;
				case 28:
					// 付款人姓名
					mFROM_NAME = value;
					break;
				case 29:
					// 指定提示日期
					mDUE_DATE = value;
					break;
				case 30:
					// 附言
					mFXML_MEMO = value;
					break;
				case 31:
					// 入／扣帳日
					mBUSINESS_DATE = value;
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
					// 晶片金融卡跨國作業資料
					mIC_DATA = value;
					break;
				case 35:
					// CD/ATM 國際化交易之原始資料( ORIGINAL DATA )
					mORI_DATA = value;
					break;
				case 36:
					// 國際信用卡授權交易之原始資料
					mReserve37 = value;
					break;
				case 37:
					// 存戶帳戶餘額 ( AMOUNT，A/C BALANCE ) 或 CD/ATM 實際交易付款金額
					mBALB = value;
					break;
				case 38:
					// 客戶亂碼基碼同步查核欄( PP-KEY SYNC. CHECK ITEM )
					mSYNC_PPKEY = value;
					break;
				case 39:
					// 繳款類別
					mPAYTYPE = value;
					break;
				case 40:
					// 稽徵機關
					mTAX_UNIT = value;
					break;
				case 41:
					// 身份証號／營利事業統一編號(IDN/BAN)
					mIDNO = value;
					break;
				case 42:
					// REC
					mReserve43 = value;
					break;
				case 43:
					// 特店代碼Merchant ID
					mMERCHANT_ID = value;
					break;
				case 44:
					// 中文附言欄
					mCHREM = value;
					break;
				case 45:
					// 附言欄
					mMEMO = value;
					break;
				case 46:
					// 收款人身分證/統編
					mTO_IDNO = value;
					break;
				case 47:
					// 附言欄
					mREMARK = value;
					break;
				case 48:
					// 繳費作業手續費
					mNPS_FEE = value;
					break;
				case 49:
					// 該單位應收手續費
					mNPS_FEE_ALL = value;
					break;
				case 50:
					// 轉入帳號
					mTRIN_ACTNO = value;
					break;
				case 51:
					// 轉出帳號
					mTROUT_ACTNO = value;
					break;
				case 52:
					// 原交易序號
					mORI_STAN = value;
					break;
				case 53:
					// 銷帳編號
					mRECON_SEQNO = value;
					break;
				case 54:
					// IC卡備註欄
					mICMARK = value;
					break;
				case 55:
					// 交易驗證碼
					mTAC = value;
					break;
				case 56:
					// 帳戶補充資訊
					mACCT_SUP = value;
					break;
				case 57:
					// 圈存金額
					mReserve58 = value;
					break;
				case 58:
					// 交易累計金額
					mReserve59 = value;
					break;
				case 59:
					// 交易金額(二)
					mReserve60 = value;
					break;
				case 60:
					// ＩＣ卡卡號或卡片國際組織網路識別資料
					mNETWK_DATA = value;
					break;
				case 61:
					// 交易序號
					mReserve62 = value;
					break;
				case 62:
					// 授權碼
					mAUTH_CODE = value;
					break;
				case 63:
					// 參加單位與財金公司間訊息押碼 ( NODE TO NODE MAC )
					mMAC = value;
					break;
			}
			mIndexValue[index] = value;
		}
	}

	public DefORI_DATA getORIDATA() {
		return _OriData;
	}

	public void setORIDATA(DefORI_DATA value) {
		_OriData = value;
	}

	public DefIC_DATA getICDATA() {
		return _IcData;
	}

	public void setICDATA(DefIC_DATA value) {
		_IcData = value;
	}

	public DefOB_DATA getOBDATA() {
		return _ObData;
	}

	public void setOBDATA(DefOB_DATA value) {
		_ObData = value;
	}

	public static class DefORI_DATA {
		// 原始交易訊息類別代碼
		private String mORI_MSGTYPE = StringUtils.EMPTY;
		// 原始交易查詢序號
		private String mORI_VISA_STAN = StringUtils.EMPTY;
		// 原始交易傳輸日期時間
		private String mORI_TX_DATETIME = StringUtils.EMPTY;
		// 原始交易代理單位代號
		private String mORI_ACQ = StringUtils.EMPTY;
		// 原始交易啟動者代號
		private String mORI_FWD_INST = StringUtils.EMPTY;
		// 交易金額
		private String mTX_AMT = StringUtils.EMPTY;
		// 清算金額
		private String mSET_AMT = StringUtils.EMPTY;
		// 交易幣別
		private String mTX_CUR = StringUtils.EMPTY;
		// 清算幣別
		private String mSET_CUR = StringUtils.EMPTY;
		// 清算匯率
		private String mSET_EXRATE = StringUtils.EMPTY;
		// 交易回傭費
		private String mSET_FEE = StringUtils.EMPTY;
		// 交易處理費
		private String mPROC_FEE = StringUtils.EMPTY;
		// 當地交易日期/時間
		private String mLOC_DATETIME = StringUtils.EMPTY;
		// 交易清算日期
		private String mSET_DATE_MMDD = StringUtils.EMPTY;
		// 交易匯率轉換日
		private String mCOV_DATE_MMDD = StringUtils.EMPTY;
		// 持卡人扣帳金額
		private String mBIL_AMT = StringUtils.EMPTY;
		// 持卡人扣帳匯率
		private String mBILL_RATE = StringUtils.EMPTY;
		// 實際完成之原始交易金額
		private String mTX_RPAMT = StringUtils.EMPTY;
		// 實際完成之交易清算金額
		private String mSET_RPAMT = StringUtils.EMPTY;
		// 發卡行加收費用
		private String mICCR = StringUtils.EMPTY;
		// 國際組織匯率轉換費
		private String mMCCR = StringUtils.EMPTY;
		// 持卡人扣帳幣別
		private String mBIL_CUR = StringUtils.EMPTY;
		// 終端機代號
		private String mPOS_MODE = StringUtils.EMPTY;
		// 代理單位國別碼
		private String mACQ_CNTRY = StringUtils.EMPTY;
		// 索引變數宣告
		private Object[] mIndexValue = new Object[24];

		/**
		 * 原始交易訊息類別代碼
		 * 
		 * <remark>
		 * 長度4
		 * </remark>
		 */
		public String getOriMsgtype() {
			return mORI_MSGTYPE;
		}

		public void setOriMsgtype(String value) {
			mORI_MSGTYPE = value;
			mIndexValue[0] = value;
		}

		/**
		 * 原始交易查詢序號
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
		 * 原始交易傳輸日期時間
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
			mIndexValue[2] = value;
		}

		/**
		 * 原始交易代理單位代號
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
			mIndexValue[3] = value;
		}

		/**
		 * 原始交易啟動者代號
		 * 
		 * <remark>
		 * 長度11
		 * </remark>
		 */
		public String getOriFwdInst() {
			return mORI_FWD_INST;
		}

		public void setOriFwdInst(String value) {
			mORI_FWD_INST = value;
			mIndexValue[4] = value;
		}

		/**
		 * 交易金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getTxAmt() {
			return mTX_AMT;
		}

		public void setTxAmt(String value) {
			mTX_AMT = value;
			mIndexValue[5] = value;
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
			mIndexValue[6] = value;
		}

		/**
		 * 交易幣別
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getTxCur() {
			return mTX_CUR;
		}

		public void setTxCur(String value) {
			mTX_CUR = value;
			mIndexValue[7] = value;
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
			mIndexValue[8] = value;
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
			mIndexValue[9] = value;
		}

		/**
		 * 交易回傭費
		 * 
		 * <remark>
		 * 長度9
		 * </remark>
		 */
		public String getSetFee() {
			return mSET_FEE;
		}

		public void setSetFee(String value) {
			mSET_FEE = value;
			mIndexValue[10] = value;
		}

		/**
		 * 交易處理費
		 * 
		 * <remark>
		 * 長度9
		 * </remark>
		 */
		public String getProcFee() {
			return mPROC_FEE;
		}

		public void setProcFee(String value) {
			mPROC_FEE = value;
			mIndexValue[11] = value;
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
			mIndexValue[12] = value;
		}

		/**
		 * 交易清算日期
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
			mIndexValue[13] = value;
		}

		/**
		 * 交易匯率轉換日
		 * 
		 * <remark>
		 * 長度4
		 * </remark>
		 */
		public String getCovDateMmdd() {
			return mCOV_DATE_MMDD;
		}

		public void setCovDateMmdd(String value) {
			mCOV_DATE_MMDD = value;
			mIndexValue[14] = value;
		}

		/**
		 * 持卡人扣帳金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getBilAmt() {
			return mBIL_AMT;
		}

		public void setBilAmt(String value) {
			mBIL_AMT = value;
			mIndexValue[15] = value;
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
			mIndexValue[16] = value;
		}

		/**
		 * 實際完成之原始交易金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getTxRpamt() {
			return mTX_RPAMT;
		}

		public void setTxRpamt(String value) {
			mTX_RPAMT = value;
			mIndexValue[17] = value;
		}

		/**
		 * 實際完成之交易清算金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getSetRpamt() {
			return mSET_RPAMT;
		}

		public void setSetRpamt(String value) {
			mSET_RPAMT = value;
			mIndexValue[18] = value;
		}

		/**
		 * 發卡行加收費用
		 * 
		 * <remark>
		 * 長度8
		 * </remark>
		 */
		public String getICCR() {
			return mICCR;
		}

		public void setICCR(String value) {
			mICCR = value;
			mIndexValue[19] = value;
		}

		/**
		 * 國際組織匯率轉換費
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getMCCR() {
			return mMCCR;
		}

		public void setMCCR(String value) {
			mMCCR = value;
			mIndexValue[20] = value;
		}

		/**
		 * 持卡人扣帳幣別
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getBilCur() {
			return mBIL_CUR;
		}

		public void setBilCur(String value) {
			mBIL_CUR = value;
			mIndexValue[21] = value;
		}

		/**
		 * 終端機代號
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
			mIndexValue[22] = value;
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
			mIndexValue[23] = value;
		}

		public String toJSON() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	public static class DefIC_DATA {
		// 代理行單位代號
		private String mACQ_NO = StringUtils.EMPTY;
		// 代理單位國別碼
		private String mACQ_CNTRY = StringUtils.EMPTY;
		// 收單幣別碼
		private String mTX_CUR = StringUtils.EMPTY;
		// 結算匯率
		private String mSET_EXRATE = StringUtils.EMPTY;
		// 台方手續費
		private String mTWD_FEE = StringUtils.EMPTY;
		// 收單方手續費
		private String mPROC_FEE = StringUtils.EMPTY;
		// 台幣結算金額
		private String mSET_AMT = StringUtils.EMPTY;
		// 提領/消費金額
		private String mTX_AMT = StringUtils.EMPTY;
		// 端末交易序號
		private String mIC_STAN = StringUtils.EMPTY;
		// 索引變數宣告
		private Object[] mIndexValue = new Object[9];

		/**
		 * 代理行單位代號
		 * 
		 * <remark>
		 * 長度8
		 * </remark>
		 */
		public String getAcqNo() {
			return mACQ_NO;
		}

		public void setAcqNo(String value) {
			mACQ_NO = value;
			mIndexValue[0] = value;
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
			mIndexValue[1] = value;
		}

		/**
		 * 收單幣別碼
		 * 
		 * <remark>
		 * 長度3
		 * </remark>
		 */
		public String getTxCur() {
			return mTX_CUR;
		}

		public void setTxCur(String value) {
			mTX_CUR = value;
			mIndexValue[2] = value;
		}

		/**
		 * 結算匯率
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
			mIndexValue[3] = value;
		}

		/**
		 * 台方手續費
		 * 
		 * <remark>
		 * 長度7
		 * </remark>
		 */
		public String getTwdFee() {
			return mTWD_FEE;
		}

		public void setTwdFee(String value) {
			mTWD_FEE = value;
			mIndexValue[4] = value;
		}

		/**
		 * 收單方手續費
		 * 
		 * <remark>
		 * 長度7
		 * </remark>
		 */
		public String getProcFee() {
			return mPROC_FEE;
		}

		public void setProcFee(String value) {
			mPROC_FEE = value;
			mIndexValue[5] = value;
		}

		/**
		 * 台幣結算金額
		 * 
		 * <remark>
		 * 長度14
		 * </remark>
		 */
		public String getSetAmt() {
			return mSET_AMT;
		}

		public void setSetAmt(String value) {
			mSET_AMT = value;
			mIndexValue[6] = value;
		}

		/**
		 * 提領/消費金額
		 * 
		 * <remark>
		 * 長度14
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
		 * 端末交易序號
		 * 
		 * <remark>
		 * 長度6
		 * </remark>
		 */
		public String getIcStan() {
			return mIC_STAN;
		}

		public void setIcStan(String value) {
			mIC_STAN = value;
			mIndexValue[8] = value;
		}

		public String toJSON() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	public static class DefOB_DATA {
		// 特店代號
		private String mMERCHANT_ID = StringUtils.EMPTY;
		// 境外機構結帳日
		private String mCLOSING_DATE = StringUtils.EMPTY;
		// 消費者台幣支付金額
		private String mTOT_TWD_AMT = StringUtils.EMPTY;
		// 消費者台幣支付金額
		private String mTWN_FEE = StringUtils.EMPTY;
		// 委託劃付銀行結算金額
		private String mSET_AMT = StringUtils.EMPTY;
		// 結算幣別碼
		private String mSET_CUR = StringUtils.EMPTY;
		// 結算匯率
		private String mSET_EXRATE = StringUtils.EMPTY;
		// 訂單編號
		private String mORDER_NO = StringUtils.EMPTY;
		// 追蹤序號
		private String mRRN = StringUtils.EMPTY;
		// 原交易序號
		private String mORI_STAN = StringUtils.EMPTY;
		// 原營業日
		private String mORI_BUSINESS_DATE = StringUtils.EMPTY;
		// 原訂單編號
		private String mORI_ORDER_NO = StringUtils.EMPTY;
		// 台幣總手續費
		private String mTOT_TWD_FEE = StringUtils.EMPTY;
		// 外幣總金額
		private String mTOT_FOR_AMT = StringUtils.EMPTY;
		// 外幣總手續費
		private String mTOT_FOR_FEE = StringUtils.EMPTY;
		// 使用者同意註記
		private String mYESFG = StringUtils.EMPTY;
		// 索引變數宣告
		private Object[] mIndexValue = new Object[16];

		/**
		 * 特店代號
		 * 
		 * <remark>
		 * 長度15
		 * </remark>
		 */
		public String getMerchantId() {
			return mMERCHANT_ID;
		}

		public void setMerchantId(String value) {
			mMERCHANT_ID = value;
			mIndexValue[0] = value;
		}

		/**
		 * 境外機構結帳日
		 * 
		 * <remark>
		 * 長度8
		 * </remark>
		 */
		public String getClosingDate() {
			return mCLOSING_DATE;
		}

		public void setClosingDate(String value) {
			mCLOSING_DATE = value;
			mIndexValue[1] = value;
		}

		/**
		 * 消費者台幣支付金額
		 * 
		 * <remark>
		 * 長度14
		 * </remark>
		 */
		public String getTotTwdAmt() {
			return mTOT_TWD_AMT;
		}

		public void setTotTwdAmt(String value) {
			mTOT_TWD_AMT = value;
			mIndexValue[2] = value;
		}

		/**
		 * 台方手續費
		 * 
		 * <remark>
		 * 長度7
		 * </remark>
		 */
		public String getTwnFee() {
			return mTWN_FEE;
		}

		public void setTwnFee(String value) {
			mTWN_FEE = value;
			mIndexValue[3] = value;
		}

		/**
		 * 委託劃付銀行結算金額
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
			mIndexValue[4] = value;
		}

		/**
		 * 結算幣別碼
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
			mIndexValue[5] = value;
		}

		/**
		 * 結算匯率
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
			mIndexValue[6] = value;
		}

		/**
		 * 訂單編號
		 * 
		 * <remark>
		 * 長度20
		 * </remark>
		 */
		public String getOrderNo() {
			return mORDER_NO;
		}

		public void setOrderNo(String value) {
			mORDER_NO = value;
			mIndexValue[7] = value;
		}

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
			mIndexValue[8] = value;
		}

		/**
		 * 原交易序號
		 * 
		 * <remark>
		 * 長度10
		 * </remark>
		 */
		public String getOriStan() {
			return mORI_STAN;
		}

		public void setOriStan(String value) {
			mORI_STAN = value;
			mIndexValue[9] = value;
		}

		/**
		 * 原交易序號
		 * 
		 * <remark>
		 * 長度6
		 * </remark>
		 */
		public String getOriBusinessDate() {
			return mORI_BUSINESS_DATE;
		}

		public void setOriBusinessDate(String value) {
			mORI_BUSINESS_DATE = value;
			mIndexValue[10] = value;
		}

		/**
		 * 原訂單編號
		 * 
		 * <remark>
		 * 長度20
		 * </remark>
		 */
		public String getOriOrderNo() {
			return mORI_ORDER_NO;
		}

		public void setOriOrderNo(String value) {
			mORI_ORDER_NO = value;
			mIndexValue[11] = value;
		}

		/**
		 * 台幣總手續費
		 * 
		 * <remark>
		 * 長度7
		 * </remark>
		 */
		public String getTotTwdFee() {
			return mTOT_TWD_FEE;
		}

		public void setTotTwdFee(String value) {
			mTOT_TWD_FEE = value;
			mIndexValue[12] = value;
		}

		/**
		 * 外幣總金額
		 * 
		 * <remark>
		 * 長度12
		 * </remark>
		 */
		public String getTotForAmt() {
			return mTOT_FOR_AMT;
		}

		public void setTotForAmt(String value) {
			mTOT_FOR_AMT = value;
			mIndexValue[13] = value;
		}

		/**
		 * 外幣總手續費
		 * 
		 * <remark>
		 * 長度7
		 * </remark>
		 */
		public String getTotForFee() {
			return mTOT_FOR_FEE;
		}

		public void setTotForFee(String value) {
			mTOT_FOR_FEE = value;
			mIndexValue[14] = value;
		}

		/**
		 * 使用者同意註記
		 * 
		 * <remark>
		 * 長度1
		 * </remark>
		 */
		public String getYESFG() {
			return mYESFG;
		}

		public void setYESFG(String value) {
			mYESFG = value;
			mIndexValue[15] = value;
		}
		

		public String toJSON() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}
}