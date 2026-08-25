package com.syscom.fep.vo.constant;

public interface NormalRC {
    public static final String ATM_OK = "    ";
    public static final String ATM_G51_OK = "G051";
    public static final String FEP_OK = "0000";
    public static final String Unisys_OK = "0000";
    public static final String UATMP_OK = "    ";
    public static final String T24_OK = "1";
    public static final String T24RC_OK = "0000";
    public static final String FCSFile_OK = "000000";
    //2014-02-06 Modify by Ruling for MMA悠遊Debit
    public static final String SVCS_OK = "00";

    //SINOCARD RC
    public static final String SINOCARD_OK = "    ";
    public static final String SINOCARD_OTHER_OK = "    ";
    public static final String SINOCARD_FISC_OK = "4001";

    //外部Channel
    public static final String External_OK = "SUCCESS";

    //FISC RC
    public static final String FISC_OK = "0001";
    public static final String FISC_ATM_OK = "4001";
    public static final String FISC_CLR_OK = "6102"; //PCODE=5102使用
    public static final String FISC_REQ_RC = "0000"; //Request RC
    public static final String FISC_REQ_RC_ChangeDate = "0001"; //Request RC for after chanage date
    //Public Const FISCRC_INIT As String = "0000"         'Request RC

    public static final String RMBTCH_FEPRC_OK = "EF0000";
    public static final String RMBTCH_FEPRC_OKMSG = "匯出成功";
    
    //CBS
    public static final String CBS_OK = "000";

    //改放至HandlerBase
    //Public Const WEBATM As String = "768"
    //Add by David Tai on 2014-10-24 for PSP TSM
    //服務供應商代號(X(2), 銀行業:10)
    public static final String TSMServiceProviderId = "10";
}
