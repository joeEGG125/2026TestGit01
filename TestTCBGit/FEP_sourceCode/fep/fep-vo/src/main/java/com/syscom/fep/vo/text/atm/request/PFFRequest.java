package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.frmcommon.annotation.Field;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.syscom.fep.vo.text.atm.ATMTextBase;

public class PFFRequest extends ATMTextBase {
    //空白
    @Field(length = 4)
    private String _TA = "";

    //交易別
    @Field(length = 6)
    private String _TXCD = "";

    //新舊電文註記
    @Field(length = 2)
    private String _ATMVER_N = "";

    //ATM版本日期
    @Field(length = 16)
    private String _ATMVER = "";

    //ATM所屬分行
    @Field(length = 6)
    private String _BRNO = "";

    //ATM 代號
    @Field(length = 4)
    private String _WSNO = "";

    //鈔箱1狀態
    @Field(length = 2)
    private String _NOTES1 = "";

    //鈔箱2狀態
    @Field(length = 2)
    private String _NOTES2 = "";

    //鈔箱3狀態
    @Field(length = 2)
    private String _NOTES3 = "";

    //鈔箱4狀態
    @Field(length = 2)
    private String _NOTES4 = "";

    //序時紙捲狀態
    @Field(length = 2)
    private String _JOUS = "";

    //交易明細表狀態
    @Field(length = 2)
    private String _ADVS = "";

    //存款模組狀態
    @Field(length = 2)
    private String _DEPS = "";

    //磁條讀寫頭狀態
    @Field(length = 2)
    private String _MCRWS = "";

    //ENCRYPTOR狀態
    @Field(length = 2)
    private String _ENCRS = "";

    //對帳單模組狀態
    @Field(length = 2)
    private String _STAMS = "";

    //吐鈔模組狀態
    @Field(length = 2)
    private String _DISPEN = "";

    //
    @Field(length = 2)
    private String _FILLER1 = "";

    //ATM 服務記號
    @Field(length = 2)
    private String _SERVICE = "";

    //營業日MODE
    @Field(length = 2)
    private String _MODE = "";

    //入帳日
    @Field(length = 16)
    private String _DD = "";

    //信封存款MODE
    @Field(length = 2)
    private String _DEPMODE = "";

    //ATM 系統日
    @Field(length = 16)
    private String _ATMSEQ_1 = "";

    //ATM交易序號
    @Field(length = 16)
    private String _ATMSEQ_2 = "";

    //銀行別
    @Field(length = 6, optional = true)
    private String _BKNO = "";

    //卡片帳號
    @Field(length = 32, optional = true)
    private String _CHACT = "";

    //交易帳號
    @Field(length = 32, optional = true)
    private String _TXACT = "";

    //轉入銀行別
    @Field(length = 6, optional = true)
    private String _BKNO_D = "";

    //轉入帳號
    @Field(length = 32, optional = true)
    private String _ACT_D = "";

    //FOR MATED PIN BLOCK
    @Field(length = 32, optional = true)
    private String _FPB = "";

    //第三軌資料
    @Field(length = 208, optional = true)
    private String _TRACK3 = "";

    //交易金額
    @Field(length = 16, optional = true)
    private String _TXAMT = "";

    //吐鈔張數 1 (個拾位)
    @Field(length = 4, optional = true)
    private String _DSPCNT1 = "";

    //吐鈔張數 2 (個拾位)
    @Field(length = 4, optional = true)
    private String _DSPCNT2 = "";

    //吐鈔張數 3 (個拾位)
    @Field(length = 4, optional = true)
    private String _DSPCNT3 = "";

    //吐鈔張數 4 (個拾位)
    @Field(length = 4, optional = true)
    private String _DSPCNT4 = "";

    //繳款種類
    @Field(length = 10, optional = true)
    private String _CLASS = "";

    //銷帳編號
    @Field(length = 32, optional = true)
    private String _PAYCNO = "";

    //繳款到期日
    @Field(length = 16, optional = true)
    private String _DUEDATE = "";

    //稽徵機關別
    @Field(length = 6, optional = true)
    private String _UNIT = "";

    //身分證字號
    @Field(length = 22, optional = true)
    private String _IDNO = "";

    //APPLY
    @Field(length = 2, optional = true)
    private String _APPLY = "";

    //預約轉帳入賬日期
    @Field(length = 16, optional = true)
    private String _DATE = "";

    //端末設備查核碼
    @Field(length = 16, optional = true)
    private String _ATMCHK = "";

    //YYYYMMDDHHMMSS
    @Field(length = 28, optional = true)
    private String _ICDTTM = "";

    //晶片主帳號
    @Field(length = 32, optional = true)
    private String _ICACT = "";

    //晶片卡REMARK欄位資料
    @Field(length = 60, optional = true)
    private String _ICMARK = "";

    //晶片卡交易序號
    @Field(length = 16, optional = true)
    private String _ICTXSEQ = "";

    //晶片卡交易驗證碼
    @Field(length = 32, optional = true)
    private String _ICTAC = "";

    //原REQ訊息中心交易序號
    @Field(length = 14, optional = true)
    private String _ORI_TXSEQ = "";

    //原REQ訊息ATM系統日
    @Field(length = 16, optional = true)
    private String _ORI_ATMSEQ_1 = "";

    //原REQ訊息ATM交易序號
    @Field(length = 16, optional = true)
    private String _ORI_ATMSEQ_2 = "";

    //拒絕理由
    @Field(length = 8, optional = true)
    private String _EXPCD = "";

    //本營業日 (DD)
    @Field(length = 4, optional = true)
    private String _ATMDD = "";

    //壓碼日期
    @Field(length = 12, optional = true)
    private String _YYMMDD = "";

    //訊息押碼
    @Field(length = 16, optional = true)
    private String _MAC = "";

    //幣別
    @Field(length = 4, optional = true)
    private String _CURCD = "";

    //調帳號記號
    @Field(length = 2, optional = true)
    private String _ACTCNT = "";

    //匯率
    @Field(length = 20, optional = true)
    private String _RATE = "";

    //折合台幣現幣
    @Field(length = 16, optional = true)
    private String _NTAMT = "";

    //台幣中價匯率
    @Field(length = 20, optional = true)
    private String _ACRAT = "";

    //匯差
    @Field(length = 20, optional = true)
    private String _DISRAT = "";

    //手續費 (台幣)
    @Field(length = 26, optional = true)
    private String _CHARGE = "";

    //手續費折原幣
    @Field(length = 26, optional = true)
    private String _EXP = "";

    //掛牌匯率
    @Field(length = 20, optional = true)
    private String _SCASH = "";

    //信封存款種類
    @Field(length = 2, optional = true)
    private String _DEP_TYPE = "";

    //存款信封序號
    @Field(length = 8, optional = true)
    private String _DEP_DEPNO = "";

    //吐鈔張數 1 (千百位)
    @Field(length = 4, optional = true)
    private String _DSPCNT1T = "";

    //吐鈔張數 2 (千百位)
    @Field(length = 4, optional = true)
    private String _DSPCNT2T = "";

    //吐鈔張數 3 (千百位)
    @Field(length = 4, optional = true)
    private String _DSPCNT3T = "";

    //吐鈔張數 4 (千百位)
    @Field(length = 4, optional = true)
    private String _DSPCNT4T = "";

    //委託單位代號
    @Field(length = 16, optional = true)
    private String _VPID = "";

    //費用代號
    @Field(length = 8, optional = true)
    private String _PAYID = "";

    //附言欄
    @Field(length = 80, optional = true)
    private String _MENO = "";

    //吐鈔張數 5 (千百個拾)
    @Field(length = 8, optional = true)
    private String _DSPCNT5 = "";

    //吐鈔張數 6 (千百個拾)
    @Field(length = 8, optional = true)
    private String _DSPCNT6 = "";

    //吐鈔張數 7 (千百個拾)
    @Field(length = 8, optional = true)
    private String _DSPCNT7 = "";

    //吐鈔張數 8 (千百個拾)
    @Field(length = 8, optional = true)
    private String _DSPCNT8 = "";

    //折合扣帳幣別金額
    @Field(length = 20, optional = true)
    private String _TXAMTDB = "";

    //扣帳幣別，海外卡專用
    @Field(length = 4, optional = true)
    private String _BAL_CUR = "";

    //手續費幣別，搭配EXP
    @Field(length = 4, optional = true)
    private String _HC_CUR = "";

    // 半自助模式記號
    @Field(length = 2, optional = true)
    private String  _BAL_PFL = "";

    // 核身完成記號
    @Field(length = 2, optional = true)
    private String  _HC_CFL = "";
    
    //FILLER2
    @Field(length = 12, optional = true)
    private String _FILLER2 = "";

    //鈔箱5狀態
    @Field(length = 2, optional = true)
    private String _NOTES5 = "";

    //鈔箱6狀態
    @Field(length = 2, optional = true)
    private String _NOTES6 = "";

    //鈔箱7狀態
    @Field(length = 2, optional = true)
    private String _NOTES7 = "";

    //鈔箱8狀態
    @Field(length = 2, optional = true)
    private String _NOTES8 = "";

    //
    @Field(length = 116, optional = true)
    private String _FILLER3 = "";

    private static final int _TotalLength = 720;


    public String getTA() {
        return _TA;
    }

    public void setTA(String TA) {
        this._TA = TA;
    }

    public String getTXCD() {
        return _TXCD;
    }

    public void setTXCD(String _TXCD) {
        this._TXCD = _TXCD;
    }

    public String getATMVER_N() {
        return _ATMVER_N;
    }

    public void setATMVER_N(String _ATMVER_N) {
        this._ATMVER_N = _ATMVER_N;
    }

    public String getATMVER() {
        return _ATMVER;
    }

    public void setATMVER(String _ATMVER) {
        this._ATMVER = _ATMVER;
    }

    public String getBRNO() {
        return _BRNO;
    }

    public void setBRNO(String _BRNO) {
        this._BRNO = _BRNO;
    }

    public String getWSNO() {
        return _WSNO;
    }

    public void setWSNO(String _WSNO) {
        this._WSNO = _WSNO;
    }

    public String getNOTES1() {
        return _NOTES1;
    }

    public void setNOTES1(String _NOTES1) {
        this._NOTES1 = _NOTES1;
    }

    public String getNOTES2() {
        return _NOTES2;
    }

    public void setNOTES2(String _NOTES2) {
        this._NOTES2 = _NOTES2;
    }

    public String getNOTES3() {
        return _NOTES3;
    }

    public void setNOTES3(String _NOTES3) {
        this._NOTES3 = _NOTES3;
    }

    public String getNOTES4() {
        return _NOTES4;
    }

    public void setNOTES4(String _NOTES4) {
        this._NOTES4 = _NOTES4;
    }

    public String getJOUS() {
        return _JOUS;
    }

    public void setJOUS(String _JOUS) {
        this._JOUS = _JOUS;
    }

    public String getADVS() {
        return _ADVS;
    }

    public void setADVS(String _ADVS) {
        this._ADVS = _ADVS;
    }

    public String getDEPS() {
        return _DEPS;
    }

    public void setDEPS(String _DEPS) {
        this._DEPS = _DEPS;
    }

    public String getMCRWS() {
        return _MCRWS;
    }

    public void setMCRWS(String _MCRWS) {
        this._MCRWS = _MCRWS;
    }

    public String getENCRS() {
        return _ENCRS;
    }

    public void setENCRS(String _ENCRS) {
        this._ENCRS = _ENCRS;
    }

    public String getSTAMS() {
        return _STAMS;
    }

    public void set_STAMS(String _STAMS) {
        this._STAMS = _STAMS;
    }

    public String getDISPEN() {
        return _DISPEN;
    }

    public void setDISPEN(String _DISPEN) {
        this._DISPEN = _DISPEN;
    }

    public String getFILLER1() {
        return _FILLER1;
    }

    public void setFILLER1(String _FILLER1) {
        this._FILLER1 = _FILLER1;
    }

    public String getSERVICE() {
        return _SERVICE;
    }

    public void setSERVICE(String _SERVICE) {
        this._SERVICE = _SERVICE;
    }

    public String getMODE() {
        return _MODE;
    }

    public void setMODE(String _MODE) {
        this._MODE = _MODE;
    }

    public String getDD() {
        return _DD;
    }

    public void setDD(String _DD) {
        this._DD = _DD;
    }

    public String getDEPMODE() {
        return _DEPMODE;
    }

    public void setDEPMODE(String _DEPMODE) {
        this._DEPMODE = _DEPMODE;
    }

    public String getATMSEQ_1() {
        return _ATMSEQ_1;
    }

    public void setATMSEQ_1(String _ATMSEQ_1) {
        this._ATMSEQ_1 = _ATMSEQ_1;
    }

    public String getATMSEQ_2() {
        return _ATMSEQ_2;
    }

    public void setATMSEQ_2(String _ATMSEQ_2) {
        this._ATMSEQ_2 = _ATMSEQ_2;
    }

    public String getBKNO() {
        return _BKNO;
    }

    public void setBKNO(String _BKNO) {
        this._BKNO = _BKNO;
    }

    public String getCHACT() {
        return _CHACT;
    }

    public void setCHACT(String _CHACT) {
        this._CHACT = _CHACT;
    }

    public String getTXACT() {
        return _TXACT;
    }

    public void setTXACT(String _TXACT) {
        this._TXACT = _TXACT;
    }

    public String getBKNO_D() {
        return _BKNO_D;
    }

    public void setBKNO_D(String _BKNO_D) {
        this._BKNO_D = _BKNO_D;
    }

    public String getACT_D() {
        return _ACT_D;
    }

    public void setACT_D(String _ACT_D) {
        this._ACT_D = _ACT_D;
    }

    public String get_FPB() {
        return _FPB;
    }

    public void setFPB(String _FPB) {
        this._FPB = _FPB;
    }

    public String getTRACK3() {
        return _TRACK3;
    }

    public void setTRACK3(String _TRACK3) {
        this._TRACK3 = _TRACK3;
    }

    public String getTXAMT() {
        return _TXAMT;
    }

    public void setTXAMT(String _TXAMT) {
        this._TXAMT = _TXAMT;
    }

    public String getDSPCNT1() {
        return _DSPCNT1;
    }

    public void setDSPCNT1(String _DSPCNT1) {
        this._DSPCNT1 = _DSPCNT1;
    }

    public String getDSPCNT2() {
        return _DSPCNT2;
    }

    public void setDSPCNT2(String _DSPCNT2) {
        this._DSPCNT2 = _DSPCNT2;
    }

    public String getDSPCNT3() {
        return _DSPCNT3;
    }

    public void setDSPCNT3(String _DSPCNT3) {
        this._DSPCNT3 = _DSPCNT3;
    }

    public String getDSPCNT4() {
        return _DSPCNT4;
    }

    public void setDSPCNT4(String _DSPCNT4) {
        this._DSPCNT4 = _DSPCNT4;
    }

    public String getCLASS() {
        return _CLASS;
    }

    public void setCLASS(String _CLASS) {
        this._CLASS = _CLASS;
    }

    public String getPAYCNO() {
        return _PAYCNO;
    }

    public void setPAYCNO(String _PAYCNO) {
        this._PAYCNO = _PAYCNO;
    }

    public String getDUEDATE() {
        return _DUEDATE;
    }

    public void setDUEDATE(String _DUEDATE) {
        this._DUEDATE = _DUEDATE;
    }

    public String getUNIT() {
        return _UNIT;
    }

    public void setUNIT(String _UNIT) {
        this._UNIT = _UNIT;
    }

    public String getIDNO() {
        return _IDNO;
    }

    public void setIDNO(String _IDNO) {
        this._IDNO = _IDNO;
    }

    public String getAPPLY() {
        return _APPLY;
    }

    public void setAPPLY(String _APPLY) {
        this._APPLY = _APPLY;
    }

    public String getDATE() {
        return _DATE;
    }

    public void setDATE(String _DATE) {
        this._DATE = _DATE;
    }

    public String getATMCHK() {
        return _ATMCHK;
    }

    public void setATMCHK(String _ATMCHK) {
        this._ATMCHK = _ATMCHK;
    }

    public String getICDTTM() {
        return _ICDTTM;
    }

    public void setICDTTM(String _ICDTTM) {
        this._ICDTTM = _ICDTTM;
    }

    public String getICACT() {
        return _ICACT;
    }

    public void setICACT(String _ICACT) {
        this._ICACT = _ICACT;
    }

    public String getICMARK() {
        return _ICMARK;
    }

    public void setICMARK(String _ICMARK) {
        this._ICMARK = _ICMARK;
    }

    public String getICTXSEQ() {
        return _ICTXSEQ;
    }

    public void setICTXSEQ(String _ICTXSEQ) {
        this._ICTXSEQ = _ICTXSEQ;
    }

    public String getICTAC() {
        return _ICTAC;
    }

    public void setICTAC(String _ICTAC) {
        this._ICTAC = _ICTAC;
    }

    public String getORI_TXSEQ() {
        return _ORI_TXSEQ;
    }

    public void setORI_TXSEQ(String _ORI_TXSEQ) {
        this._ORI_TXSEQ = _ORI_TXSEQ;
    }

    public String getORI_ATMSEQ_1() {
        return _ORI_ATMSEQ_1;
    }

    public void setORI_ATMSEQ_1(String _ORI_ATMSEQ_1) {
        this._ORI_ATMSEQ_1 = _ORI_ATMSEQ_1;
    }

    public String getORI_ATMSEQ_2() {
        return _ORI_ATMSEQ_2;
    }

    public void setORI_ATMSEQ_2(String _ORI_ATMSEQ_2) {
        this._ORI_ATMSEQ_2 = _ORI_ATMSEQ_2;
    }

    public String getEXPCD() {
        return _EXPCD;
    }

    public void setEXPCD(String _EXPCD) {
        this._EXPCD = _EXPCD;
    }

    public String getATMDD() {
        return _ATMDD;
    }

    public void setATMDD(String _ATMDD) {
        this._ATMDD = _ATMDD;
    }

    public String getYYMMDD() {
        return _YYMMDD;
    }

    public void setYYMMDD(String _YYMMDD) {
        this._YYMMDD = _YYMMDD;
    }

    public String getMAC() {
        return _MAC;
    }

    public void setMAC(String _MAC) {
        this._MAC = _MAC;
    }

    public String getCURCD() {
        return _CURCD;
    }

    public void setCURCD(String _CURCD) {
        this._CURCD = _CURCD;
    }

    public String getACTCNT() {
        return _ACTCNT;
    }

    public void setACTCNT(String _ACTCNT) {
        this._ACTCNT = _ACTCNT;
    }

    public String getRATE() {
        return _RATE;
    }

    public void setRATE(String _RATE) {
        this._RATE = _RATE;
    }

    public String getNTAMT() {
        return _NTAMT;
    }

    public void setNTAMT(String _NTAMT) {
        this._NTAMT = _NTAMT;
    }

    public String getACRAT() {
        return _ACRAT;
    }

    public void setACRAT(String _ACRAT) {
        this._ACRAT = _ACRAT;
    }

    public String getDISRAT() {
        return _DISRAT;
    }

    public void setDISRAT(String _DISRAT) {
        this._DISRAT = _DISRAT;
    }

    public String getCHARGE() {
        return _CHARGE;
    }

    public void setCHARGE(String _CHARGE) {
        this._CHARGE = _CHARGE;
    }

    public String getEXP() {
        return _EXP;
    }

    public void setEXP(String _EXP) {
        this._EXP = _EXP;
    }

    public String getSCASH() {
        return _SCASH;
    }

    public void setSCASH(String _SCASH) {
        this._SCASH = _SCASH;
    }

    public String getDEP_TYPE() {
        return _DEP_TYPE;
    }

    public void setDEP_TYPE(String _DEP_TYPE) {
        this._DEP_TYPE = _DEP_TYPE;
    }

    public String getDEP_DEPNO() {
        return _DEP_DEPNO;
    }

    public void setDEP_DEPNO(String _DEP_DEPNO) {
        this._DEP_DEPNO = _DEP_DEPNO;
    }

    public String getDSPCNT1T() {
        return _DSPCNT1T;
    }

    public void setDSPCNT1T(String _DSPCNT1T) {
        this._DSPCNT1T = _DSPCNT1T;
    }

    public String getDSPCNT2T() {
        return _DSPCNT2T;
    }

    public void setDSPCNT2T(String _DSPCNT2T) {
        this._DSPCNT2T = _DSPCNT2T;
    }

    public String getDSPCNT3T() {
        return _DSPCNT3T;
    }

    public void setDSPCNT3T(String _DSPCNT3T) {
        this._DSPCNT3T = _DSPCNT3T;
    }

    public String getDSPCNT4T() {
        return _DSPCNT4T;
    }

    public void setDSPCNT4T(String _DSPCNT4T) {
        this._DSPCNT4T = _DSPCNT4T;
    }

    public String getVPID() {
        return _VPID;
    }

    public void setVPID(String _VPID) {
        this._VPID = _VPID;
    }

    public String getPAYID() {
        return _PAYID;
    }

    public void setPAYID(String _PAYID) {
        this._PAYID = _PAYID;
    }

    public String getMENO() {
        return _MENO;
    }

    public void setMENO(String _MENO) {
        this._MENO = _MENO;
    }

    public String getDSPCNT5() {
        return _DSPCNT5;
    }

    public void setDSPCNT5(String _DSPCNT5) {
        this._DSPCNT5 = _DSPCNT5;
    }

    public String getDSPCNT6() {
        return _DSPCNT6;
    }

    public void setDSPCNT6(String _DSPCNT6) {
        this._DSPCNT6 = _DSPCNT6;
    }

    public String getDSPCNT7() {
        return _DSPCNT7;
    }

    public void setDSPCNT7(String _DSPCNT7) {
        this._DSPCNT7 = _DSPCNT7;
    }

    public String getDSPCNT8() {
        return _DSPCNT8;
    }

    public void setDSPCNT8(String _DSPCNT8) {
        this._DSPCNT8 = _DSPCNT8;
    }

    public String getTXAMTDB() {
        return _TXAMTDB;
    }

    public void setTXAMTDB(String _TXAMTDB) {
        this._TXAMTDB = _TXAMTDB;
    }

    public String getBAL_CUR() {
        return _BAL_CUR;
    }

    public void setBAL_CUR(String _BAL_CUR) {
        this._BAL_CUR = _BAL_CUR;
    }

    public String getHC_CUR() {
        return _HC_CUR;
    }

    public void setHC_CUR(String _HC_CUR) {
        this._HC_CUR = _HC_CUR;
    }

    public String getBALPFL() {
        return _BAL_PFL;
    }

    public void setBALPFL(String _BALPFL) {
        this._BAL_PFL = _BALPFL;
    }
    
    public String getHCCFL() {
        return _HC_CFL;
    }

    public void setHCCFL(String _HCCFL) {
        this._HC_CFL = _HCCFL;
    }
    
    public String getFILLER2() {
        return _FILLER2;
    }

    public void setFILLER2(String _FILLER2) {
        this._FILLER2 = _FILLER2;
    }

    public String getNOTES5() {
        return _NOTES5;
    }

    public void setNOTES5(String _NOTES5) {
        this._NOTES5 = _NOTES5;
    }

    public String getNOTES6() {
        return _NOTES6;
    }

    public void setNOTES6(String _NOTES6) {
        this._NOTES6 = _NOTES6;
    }

    public String getNOTES7() {
        return _NOTES7;
    }

    public void setNOTES7(String _NOTES7) {
        this._NOTES7 = _NOTES7;
    }

    public String getNOTES8() {
        return _NOTES8;
    }

    public void setNOTES8(String _NOTES8) {
        this._NOTES8 = _NOTES8;
    }

    public String getFILLER3() {
        return _FILLER3;
    }

    public void setFILLER3(String _FILLER3) {
        this._FILLER3 = _FILLER3;
    }


    @Override
    public ATMGeneral parseFlatfile(String flatfile) throws Exception {
        return this.parseFlatfile(this.getClass(), flatfile);
    }

    @Override
    public String makeMessageFromGeneral(ATMGeneral general) throws Exception {
        return null;
    }


    @Override
    public void toGeneral(ATMGeneral general) throws Exception {
    	/*
        general.getRequest().setTA(this.toAscii(_TA)); //空白
        general.getRequest().setTXCD(this.toAscii(_TXCD)); //交易別
        general.getRequest().setAtmverN(this.toAscii(_ATMVER_N)); //新舊電文註記
        general.getRequest().setATMVER(this.toAscii(_ATMVER)); //ATM版本日期
        general.getRequest().setBRNO(this.toAscii(_BRNO)); //ATM所屬分行
        general.getRequest().setWSNO(this.toAscii(_WSNO)); //ATM 代號
        general.getRequest().setNOTES1(this.toAscii(_NOTES1)); //鈔箱1狀態
        general.getRequest().setNOTES2(this.toAscii(_NOTES2)); //鈔箱2狀態
        general.getRequest().setNOTES3(this.toAscii(_NOTES3)); //鈔箱3狀態
        general.getRequest().setNOTES4(this.toAscii(_NOTES4)); //鈔箱4狀態
        general.getRequest().setJOUS(this.toAscii(_JOUS)); //序時紙捲狀態
        general.getRequest().setADVS(this.toAscii(_ADVS)); //交易明細表狀態
        general.getRequest().setDEPS(this.toAscii(_DEPS)); //存款模組狀態
        general.getRequest().setMCRWS(this.toAscii(_MCRWS)); //磁條讀寫頭狀態
        general.getRequest().setENCRS(this.toAscii(_ENCRS)); //ENCRYPTOR狀態
        general.getRequest().setSTAMS(this.toAscii(_STAMS)); //對帳單模組狀態
        general.getRequest().setDISPEN(this.toAscii(_DISPEN)); //吐鈔模組狀態
        general.getRequest().setFILLER1(this.toAscii(_FILLER1));
        general.getRequest().setSERVICE(this.toAscii(_SERVICE)); //ATM 服務記號
        general.getRequest().setMODE(this.toAscii(_MODE)); //營業日MODE
        general.getRequest().setDD(this.toAscii(_DD)); //入帳日
        general.getRequest().setDEPMODE(this.toAscii(_DEPMODE)); //信封存款MODE
        general.getRequest().setAtmseq_1(this.toAscii(_ATMSEQ_1)); //ATM 系統日
        general.getRequest().setAtmseq_2(this.toAscii(_ATMSEQ_2)); //ATM交易序號
        general.getRequest().setBKNO(this.toAscii(_BKNO)); //銀行別
        general.getRequest().setCHACT(this.toAscii(_CHACT)); //卡片帳號
        general.getRequest().setTXACT(this.toAscii(_TXACT)); //交易帳號
        general.getRequest().setBknoD(this.toAscii(_BKNO_D)); //轉入銀行別
        general.getRequest().setActD(this.toAscii(_ACT_D)); //轉入帳號
        general.getRequest().setFPB(this.toAscii(_FPB)); //FOR MATED PIN BLOCK
        general.getRequest().setTRACK3(this.toAscii(_TRACK3)); //第三軌資料
        general.getRequest().setTXAMT(this.toBigDecimal(_TXAMT));  //交易金額
        general.getRequest().setDSPCNT1(this.toBigDecimal(_DSPCNT1));//吐鈔張數 1 (個拾位)
        general.getRequest().setDSPCNT2(this.toBigDecimal(_DSPCNT2));//吐鈔張數 2 (個拾位)
        general.getRequest().setDSPCNT3(this.toBigDecimal(_DSPCNT3));//吐鈔張數 3 (個拾位)
        general.getRequest().setDSPCNT4(this.toBigDecimal(_DSPCNT4));//吐鈔張數 4 (個拾位)
        general.getRequest().setCLASS(this.toAscii(_CLASS)); //繳款種類
        general.getRequest().setPAYCNO(this.toAscii(_PAYCNO)); //銷帳編號
        general.getRequest().setDUEDATE(this.toAscii(_DUEDATE)); //繳款到期日
        general.getRequest().setUNIT(this.toAscii(_UNIT)); //稽徵機關別
        general.getRequest().setIDNO(this.toAscii(_IDNO)); //身分證字號
        general.getRequest().setAPPLY(this.toAscii(_APPLY)); //APPLY
        general.getRequest().setDATE(this.toAscii(_DATE)); //預約轉帳入賬日期
        general.getRequest().setATMCHK(this.toAscii(_ATMCHK)); //端末設備查核碼
        general.getRequest().setICDTTM(this.toAscii(_ICDTTM)); //YYYYMMDDHHMMSS
        general.getRequest().setICACT(this.toAscii(_ICACT)); //晶片主帳號
        general.getRequest().setICMARK(_ICMARK); //晶片卡REMARK欄位資料
        general.getRequest().setICTXSEQ(this.toAscii(_ICTXSEQ)); //晶片卡交易序號
        general.getRequest().setICTAC(this.toAscii(_ICTAC)); //晶片卡交易驗證碼
        general.getRequest().setOriTxseq(this.toAscii(_ORI_TXSEQ)); //原REQ訊息中心交易序號
        general.getRequest().setOriAtmseq_1(this.toAscii(_ORI_ATMSEQ_1)); //原REQ訊息ATM系統日
        general.getRequest().setOriAtmseq_2(this.toAscii(_ORI_ATMSEQ_2)); //原REQ訊息ATM交易序號
        general.getRequest().setEXPCD(this.toAscii(_EXPCD)); //拒絕理由
        general.getRequest().setATMDD(this.toAscii(_ATMDD)); //本營業日 (DD)
        general.getRequest().setYYMMDD(this.toAscii(_YYMMDD)); //壓碼日期
        general.getRequest().setMAC(this.toAscii(_MAC)); //訊息押碼
        general.getRequest().setCURCD(this.toAscii(_CURCD)); //幣別
        general.getRequest().setACTCNT(this.toAscii(_ACTCNT)); //調帳號記號
        general.getRequest().setRATE(this.toBigDecimal(_RATE,5)); //匯率
        general.getRequest().setNTAMT(this.toBigDecimal(_NTAMT));//折合台幣現幣
        general.getRequest().setACRAT(this.toBigDecimal(_ACRAT,5));//台幣中價匯率
        general.getRequest().setDISRAT(this.toBigDecimal(_DISRAT,5));//匯差
        general.getRequest().setCHARGE(this.toBigDecimal(_CHARGE,2));//手續費 (台幣)
        general.getRequest().setEXP(this.toBigDecimal(_EXP,2));//手續費折原幣
        general.getRequest().setSCASH(this.toBigDecimal(_SCASH,5));//掛牌匯率
        general.getRequest().setDepType(this.toAscii(_DEP_TYPE)); //信封存款種類
        general.getRequest().setDepDepno(this.toAscii(_DEP_DEPNO)); //存款信封序號
        general.getRequest().setDSPCNT1T(this.toBigDecimal(_DSPCNT1T));//吐鈔張數 1 (千百位)
        general.getRequest().setDSPCNT2T(this.toBigDecimal(_DSPCNT2T));//吐鈔張數 2 (千百位)
        general.getRequest().setDSPCNT3T(this.toBigDecimal(_DSPCNT3T));//吐鈔張數 3 (千百位)
        general.getRequest().setDSPCNT4T(this.toBigDecimal(_DSPCNT4T));//吐鈔張數 4 (千百位)
        general.getRequest().setVPID(this.toAscii(_VPID)); //委託單位代號
        general.getRequest().setPAYID(this.toAscii(_PAYID)); //費用代號
        general.getRequest().setMENO(this.toAscii(_MENO)); //附言欄
        general.getRequest().setDSPCNT5(this.toBigDecimal(_DSPCNT5));
        general.getRequest().setDSPCNT6(this.toBigDecimal(_DSPCNT6));
        general.getRequest().setDSPCNT7(this.toBigDecimal(_DSPCNT7));
        general.getRequest().setDSPCNT8(this.toBigDecimal(_DSPCNT8));
        general.getRequest().setTXAMTDB(this.toBigDecimal(_TXAMTDB,2));//折合扣帳幣別金額
        general.getRequest().setBalCur(this.toAscii(_BAL_CUR)); //扣帳幣別，海外卡專用
        general.getRequest().setHcCur(this.toAscii(_HC_CUR)); //手續費幣別，搭配EXP      
        general.getRequest().setBalPfl(this.toAscii(_BAL_PFL));// 半自助模式記號
        general.getRequest().setHcCfl(this.toAscii(_HC_CFL));// 核身完成記號
        general.getRequest().setFILLER2(this.toAscii(_FILLER2));
        general.getRequest().setNOTES5(this.toAscii(_NOTES5)); //鈔箱5狀態
        general.getRequest().setNOTES6(this.toAscii(_NOTES6)); //鈔箱6狀態
        general.getRequest().setNOTES7(this.toAscii(_NOTES7)); //鈔箱7狀態
        general.getRequest().setNOTES8(this.toAscii(_NOTES8)); //鈔箱8狀態
        general.getRequest().setFILLER3(this.toAscii(_FILLER3));
        */
    }

    @Override
    public int getTotalLength() {
        return _TotalLength;
    }
}
