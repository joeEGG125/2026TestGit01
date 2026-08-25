package com.syscom.fep.vo.text.fisc;

import org.apache.commons.lang3.StringUtils;

// 財金電文3XXX BODY
public class FISC_OPC extends FISCHeader {
	// 保留欄位 1
	//
	// bitmap 位置: 1
	private String mReserve1 = StringUtils.EMPTY;

	// 通知代碼( Notice ID )
	// N( 4 ) 4
	// bitmap 位置: 2
	private String mNOTICE_ID = StringUtils.EMPTY;

	// 通知資料( Notice Data )
	// AN( 40 ) 40
	// bitmap 位置: 3
	private String mNOTICE_DATA = StringUtils.EMPTY;

	// 應用系統代碼( AP ID )
	// N( 4 ) 4
	// bitmap 位置: 4
	private String mAPID = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-1( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 5
	private String mReserve5 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-2(SYNC. CHECK ITEM-CD/ATM押碼基碼)
	// B( 32 ) 4
	// bitmap 位置: 6
	private String mSYNC_ATM = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-3( SYNC. CHECK ITEM-通匯押碼基碼 )
	// B( 32 ) 4
	// bitmap 位置: 7
	private String mSYNC_RM = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-4( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 8
	private String mReserve8 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-5( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 9
	private String mReserve9 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-6( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 10
	private String mReserve10 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-7( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 11
	private String mReserve11 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-8( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 12
	private String mReserve12 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-9( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 13
	private String mReserve13 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-10( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 14
	private String mReserve14 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-11( SYNC. CHECK ITEM-CD/ATM PP KEY )
	// B( 32 ) 4
	// bitmap 位置: 15
	private String mSYNC_PPKEY = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-12( SYNC. CHECK ITEM -CD/ATM TRIPLE DES PP KEY )
	// B( 32 ) 4
	// bitmap 位置: 16
	private String mSYNC_PPKEY_3DES = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-13( SYNC. CHECK ITEM -CD/ATM TRIPLE DES MAC KEY )
	// B( 32 ) 4
	// bitmap 位置: 17
	private String mSYNC_ATM_3DES = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-14( SYNC. CHECK ITEM -RMT TRIPLE DES MAC KEY )
	// B( 32 ) 4
	// bitmap 位置: 18
	private String mSYNC_RM_3DES = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-15( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 19
	private String mReserve19 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-16( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 20
	private String mReserve20 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-17( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 21
	private String mReserve21 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-18( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 22
	private String mReserve22 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-19( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 23
	private String mReserve23 = StringUtils.EMPTY;

	// 押碼基碼同步查核欄-20( SYNC. CHECK ITEM )
	// B( 32 ) 4
	// bitmap 位置: 24
	private String mReserve24 = StringUtils.EMPTY;

	// 跨行交易追蹤序號( TXN. TRACE NO. )
	// N( 10 ) 10
	// bitmap 位置: 25
	private String mReserve25 = StringUtils.EMPTY;

	// 應用系統基本文首( BASIC HEADER )
	// N( 49 ) 49
	// bitmap 位置: 26
	private String mBASIC_HEADER = StringUtils.EMPTY;

	// 查詢狀態( INQUIRY STATUS )
	// N( 4 ) 4
	// bitmap 位置: 27
	private String mReserve27 = StringUtils.EMPTY;

	// 基碼代號( KEY ID )
	// N( 2 ) 2
	// bitmap 位置: 28
	private String mKEYID = StringUtils.EMPTY;

	// 新押碼基碼 ( ENCRYPTED NEW KEY ；Ecd (NEW WORKING KEY))
	// B( 64 ) 8
	// bitmap 位置: 29
	private String mNEW_KEY = StringUtils.EMPTY;

	// 參加單位代碼( BANK-ID )
	// N( 3 ) 3
	// bitmap 位置: 30
	private String mBKNO = StringUtils.EMPTY;

	// 亂碼化隨機數( ENCRYPTED RANDOM NUMBER )
	// B( 64 ) 8
	// bitmap 位置: 31
	private String mRANDOM_NUM = StringUtils.EMPTY;

	// CD/ATM 沖正應付( 貸方 )金額
	// NS( 15 ) 15
	// bitmap 位置: 32
	private String mReserve32 = StringUtils.EMPTY;

	// 保留欄位 33
	//
	// bitmap 位置: 33
	private String mReserve33 = StringUtils.EMPTY;

	// CD/ATM 作業狀況
	// AN(LL+Data ) 14
	// bitmap 位置: 34
	private String mATM_STATUS = StringUtils.EMPTY;

	// 新押碼基碼 ( ENCRYPTED TRIPLE DES NEW KEY ；Ecd (NEW WORKING KEY))
	// B( 128 ) 16
	// bitmap 位置: 35
	private String mNEW_KEY_3DES = StringUtils.EMPTY;

	// 新押碼基碼(ANSI X9 TR-31 key block key)
	// AN( 80 ) 80
	// bitmap 位置: 36
	private String mNEW_KEY_KEYBLOCK = StringUtils.EMPTY;

	// Key Check Value
	// AN( 6 ) 6
	// bitmap 位置: 37
	private String mNEW_KEY_CHECKVALUE = StringUtils.EMPTY;

	// 保留欄位 38
	//
	// bitmap 位置: 38
	private String mReserve38 = StringUtils.EMPTY;

	// 保留欄位 39
	//
	// bitmap 位置: 39
	private String mReserve39 = StringUtils.EMPTY;

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

	// 參加單位與財金公司間訊息押碼( MAC )
	// B( 32 ) 4
	// bitmap 位置: 64
	private String mMAC = StringUtils.EMPTY;

	// 索引變數宣告
	private String[] mIndexValue = new String[65];

	public FISC_OPC() {
		super();
	}

	public FISC_OPC(String fiscFlatfile) {
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
	 * 通知代碼( Notice ID )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 2
	 * </remark>
	 */
	public String getNoticeId() {
		return mNOTICE_ID;
	}

	public void setNoticeId(String value) {
		mNOTICE_ID = value;
		mIndexValue[1] = value;
	}

	/**
	 * 通知資料( Notice Data )
	 * 
	 * <remark>
	 * 40 ; bitmap 位置: 3
	 * </remark>
	 */
	public String getNoticeData() {
		return mNOTICE_DATA;
	}

	public void setNoticeData(String value) {
		mNOTICE_DATA = value;
		mIndexValue[2] = value;
	}

	/**
	 * 應用系統代碼( AP ID )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 4
	 * </remark>
	 */
	public String getAPID() {
		return mAPID;
	}

	public void setAPID(String value) {
		mAPID = value;
		mIndexValue[3] = value;
	}

	/**
	 * 押碼基碼同步查核欄-1( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 5
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
	 * 押碼基碼同步查核欄-2(SYNC. CHECK ITEM-CD/ATM押碼基碼)
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 6
	 * </remark>
	 */
	public String getSyncAtm() {
		return mSYNC_ATM;
	}

	public void setSyncAtm(String value) {
		mSYNC_ATM = value;
		mIndexValue[5] = value;
	}

	/**
	 * 押碼基碼同步查核欄-3( SYNC. CHECK ITEM-通匯押碼基碼 )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 7
	 * </remark>
	 */
	public String getSyncRm() {
		return mSYNC_RM;
	}

	public void setSyncRm(String value) {
		mSYNC_RM = value;
		mIndexValue[6] = value;
	}

	/**
	 * 押碼基碼同步查核欄-4( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 8
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
	 * 押碼基碼同步查核欄-5( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 9
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
	 * 押碼基碼同步查核欄-6( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 10
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
	 * 押碼基碼同步查核欄-7( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 11
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
	 * 押碼基碼同步查核欄-8( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 12
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
	 * 押碼基碼同步查核欄-9( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 13
	 * </remark>
	 */
	public String getReserve13() {
		return mReserve13;
	}

	public void setReserve13(String value) {
		mReserve13 = value;
		mIndexValue[12] = value;
	}

	/**
	 * 押碼基碼同步查核欄-10( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 14
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
	 * 押碼基碼同步查核欄-11( SYNC. CHECK ITEM-CD/ATM PP KEY )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 15
	 * </remark>
	 */
	public String getSyncPpkey() {
		return mSYNC_PPKEY;
	}

	public void setSyncPpkey(String value) {
		mSYNC_PPKEY = value;
		mIndexValue[14] = value;
	}

	/**
	 * 押碼基碼同步查核欄-12( SYNC. CHECK ITEM -CD/ATM TRIPLE DES PP KEY )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 16
	 * </remark>
	 */
	public String getSyncPpkey_3des() {
		return mSYNC_PPKEY_3DES;
	}

	public void setSyncPpkey_3des(String value) {
		mSYNC_PPKEY_3DES = value;
		mIndexValue[15] = value;
	}

	/**
	 * 押碼基碼同步查核欄-13( SYNC. CHECK ITEM -CD/ATM TRIPLE DES MAC KEY )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 17
	 * </remark>
	 */
	public String getSyncAtm_3des() {
		return mSYNC_ATM_3DES;
	}

	public void setSyncAtm_3des(String value) {
		mSYNC_ATM_3DES = value;
		mIndexValue[16] = value;
	}

	/**
	 * 押碼基碼同步查核欄-14( SYNC. CHECK ITEM -RMT TRIPLE DES MAC KEY )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 18
	 * </remark>
	 */
	public String getSyncRm_3des() {
		return mSYNC_RM_3DES;
	}

	public void setSyncRm_3des(String value) {
		mSYNC_RM_3DES = value;
		mIndexValue[17] = value;
	}

	/**
	 * 押碼基碼同步查核欄-15( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 19
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
	 * 押碼基碼同步查核欄-16( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 20
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
	 * 押碼基碼同步查核欄-17( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 21
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
	 * 押碼基碼同步查核欄-18( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 22
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
	 * 押碼基碼同步查核欄-19( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 23
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
	 * 押碼基碼同步查核欄-20( SYNC. CHECK ITEM )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 24
	 * </remark>
	 */
	public String getReserve24() {
		return mReserve24;
	}

	public void setReserve24(String value) {
		mReserve24 = value;
		mIndexValue[23] = value;
	}

	/**
	 * 跨行交易追蹤序號( TXN. TRACE NO. )
	 * 
	 * <remark>
	 * 10 ; bitmap 位置: 25
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
	 * 應用系統基本文首( BASIC HEADER )
	 * 
	 * <remark>
	 * 49 ; bitmap 位置: 26
	 * </remark>
	 */
	public String getBasicHeader() {
		return mBASIC_HEADER;
	}

	public void setBasicHeader(String value) {
		mBASIC_HEADER = value;
		mIndexValue[25] = value;
	}

	/**
	 * 查詢狀態( INQUIRY STATUS )
	 * 
	 * <remark>
	 * 4 ; bitmap 位置: 27
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
	 * 基碼代號( KEY ID )
	 * 
	 * <remark>
	 * 2 ; bitmap 位置: 28
	 * </remark>
	 */
	public String getKEYID() {
		return mKEYID;
	}

	public void setKEYID(String value) {
		mKEYID = value;
		mIndexValue[27] = value;
	}

	/**
	 * 新押碼基碼 ( ENCRYPTED NEW KEY ；Ecd (NEW WORKING KEY))
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 29
	 * </remark>
	 */
	public String getNewKey() {
		return mNEW_KEY;
	}

	public void setNewKey(String value) {
		mNEW_KEY = value;
		mIndexValue[28] = value;
	}

	/**
	 * 參加單位代碼( BANK-ID )
	 * 
	 * <remark>
	 * 3 ; bitmap 位置: 30
	 * </remark>
	 */
	public String getBKNO() {
		return mBKNO;
	}

	public void setBKNO(String value) {
		mBKNO = value;
		mIndexValue[29] = value;
	}

	/**
	 * 亂碼化隨機數( ENCRYPTED RANDOM NUMBER )
	 * 
	 * <remark>
	 * 8 ; bitmap 位置: 31
	 * </remark>
	 */
	public String getRandomNum() {
		return mRANDOM_NUM;
	}

	public void setRandomNum(String value) {
		mRANDOM_NUM = value;
		mIndexValue[30] = value;
	}

	/**
	 * CD/ATM 沖正應付( 貸方 )金額
	 * 
	 * <remark>
	 * 15 ; bitmap 位置: 32
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
	 * CD/ATM 作業狀況
	 * 
	 * <remark>
	 * 14 ; bitmap 位置: 34
	 * </remark>
	 */
	public String getAtmStatus() {
		return mATM_STATUS;
	}

	public void setAtmStatus(String value) {
		mATM_STATUS = value;
		mIndexValue[33] = value;
	}

	/**
	 * 新押碼基碼 ( ENCRYPTED TRIPLE DES NEW KEY ；Ecd (NEW WORKING KEY))
	 * 
	 * <remark>
	 * 16 ; bitmap 位置: 35
	 * </remark>
	 */
	public String getNewKey_3des() {
		return mNEW_KEY_3DES;
	}

	public void setNewKey_3des(String value) {
		mNEW_KEY_3DES = value;
		mIndexValue[34] = value;
	}

	/**
	 * 新押碼基碼(ANSI X9 TR-31 key block key)
	 * 
	 * <remark>
	 * 80 ; bitmap 位置: 36
	 * </remark>
	 */
	public String getNewKeyKeyBlock() {
		return mNEW_KEY_KEYBLOCK;
	}

	public void setNewKeyKeyBlock(String value) {
		mNEW_KEY_KEYBLOCK = value;
		mIndexValue[35] = value;
	}

	/**
	 * Key Check Value
	 * 
	 * <remark>
	 * 6 ; bitmap 位置: 37
	 * </remark>
	 */
	public String getNewKeyCheckValue() {
		return mNEW_KEY_CHECKVALUE;
	}

	public void setNewKeyCheckValue(String value) {
		mNEW_KEY_CHECKVALUE = value;
		mIndexValue[36] = value;
	}

	/**
	 * 保留欄位 38
	 * 
	 * <remark>
	 * ; bitmap 位置: 38
	 * </remark>
	 */
	public String getReserve38() {
		return mReserve38;
	}

	public void setReserve38(String value) {
		mReserve38 = value;
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
	 * 參加單位與財金公司間訊息押碼( MAC )
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
					// 通知代碼( Notice ID )
					mNOTICE_ID = value;
					break;
				case 2:
					// 通知資料( Notice Data )
					mNOTICE_DATA = value;
					break;
				case 3:
					// 應用系統代碼( AP ID )
					mAPID = value;
					break;
				case 4:
					// 押碼基碼同步查核欄-1( SYNC. CHECK ITEM )
					mReserve5 = value;
					break;
				case 5:
					// 押碼基碼同步查核欄-2(SYNC. CHECK ITEM-CD/ATM押碼基碼)
					mSYNC_ATM = value;
					break;
				case 6:
					// 押碼基碼同步查核欄-3( SYNC. CHECK ITEM-通匯押碼基碼 )
					mSYNC_RM = value;
					break;
				case 7:
					// 押碼基碼同步查核欄-4( SYNC. CHECK ITEM )
					mReserve8 = value;
					break;
				case 8:
					// 押碼基碼同步查核欄-5( SYNC. CHECK ITEM )
					mReserve9 = value;
					break;
				case 9:
					// 押碼基碼同步查核欄-6( SYNC. CHECK ITEM )
					mReserve10 = value;
					break;
				case 10:
					// 押碼基碼同步查核欄-7( SYNC. CHECK ITEM )
					mReserve11 = value;
					break;
				case 11:
					// 押碼基碼同步查核欄-8( SYNC. CHECK ITEM )
					mReserve12 = value;
					break;
				case 12:
					// 押碼基碼同步查核欄-9( SYNC. CHECK ITEM )
					mReserve13 = value;
					break;
				case 13:
					// 押碼基碼同步查核欄-10( SYNC. CHECK ITEM )
					mReserve14 = value;
					break;
				case 14:
					// 押碼基碼同步查核欄-11( SYNC. CHECK ITEM-CD/ATM PP KEY )
					mSYNC_PPKEY = value;
					break;
				case 15:
					// 押碼基碼同步查核欄-12( SYNC. CHECK ITEM -CD/ATM TRIPLE DES PP KEY )
					mSYNC_PPKEY_3DES = value;
					break;
				case 16:
					// 押碼基碼同步查核欄-13( SYNC. CHECK ITEM -CD/ATM TRIPLE DES MAC KEY )
					mSYNC_ATM_3DES = value;
					break;
				case 17:
					// 押碼基碼同步查核欄-14( SYNC. CHECK ITEM -RMT TRIPLE DES MAC KEY )
					mSYNC_RM_3DES = value;
					break;
				case 18:
					// 押碼基碼同步查核欄-15( SYNC. CHECK ITEM )
					mReserve19 = value;
					break;
				case 19:
					// 押碼基碼同步查核欄-16( SYNC. CHECK ITEM )
					mReserve20 = value;
					break;
				case 20:
					// 押碼基碼同步查核欄-17( SYNC. CHECK ITEM )
					mReserve21 = value;
					break;
				case 21:
					// 押碼基碼同步查核欄-18( SYNC. CHECK ITEM )
					mReserve22 = value;
					break;
				case 22:
					// 押碼基碼同步查核欄-19( SYNC. CHECK ITEM )
					mReserve23 = value;
					break;
				case 23:
					// 押碼基碼同步查核欄-20( SYNC. CHECK ITEM )
					mReserve24 = value;
					break;
				case 24:
					// 跨行交易追蹤序號( TXN. TRACE NO. )
					mReserve25 = value;
					break;
				case 25:
					// 應用系統基本文首( BASIC HEADER )
					mBASIC_HEADER = value;
					break;
				case 26:
					// 查詢狀態( INQUIRY STATUS )
					mReserve27 = value;
					break;
				case 27:
					// 基碼代號( KEY ID )
					mKEYID = value;
					break;
				case 28:
					// 新押碼基碼 ( ENCRYPTED NEW KEY ；Ecd (NEW WORKING KEY))
					mNEW_KEY = value;
					break;
				case 29:
					// 參加單位代碼( BANK-ID )
					mBKNO = value;
					break;
				case 30:
					// 亂碼化隨機數( ENCRYPTED RANDOM NUMBER )
					mRANDOM_NUM = value;
					break;
				case 31:
					// CD/ATM 沖正應付( 貸方 )金額
					mReserve32 = value;
					break;
				case 32:
					// 保留欄位 33
					mReserve33 = value;
					break;
				case 33:
					// CD/ATM 作業狀況
					mATM_STATUS = value;
					break;
				case 34:
					// 新押碼基碼 ( ENCRYPTED TRIPLE DES NEW KEY ；Ecd (NEW WORKING KEY))
					mNEW_KEY_3DES = value;
					break;
				case 35:
					// 新押碼基碼(ANSI X9 TR-31 key block key)
					mNEW_KEY_KEYBLOCK = value;
					break;
				case 36:
					// Key Check Value
					mNEW_KEY_CHECKVALUE = value;
					break;
				case 37:
					// 保留欄位 38
					mReserve38 = value;
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
					// 參加單位與財金公司間訊息押碼( MAC )
					mMAC = value;
					break;
			}
			mIndexValue[index] = value;
		}
	}
}
