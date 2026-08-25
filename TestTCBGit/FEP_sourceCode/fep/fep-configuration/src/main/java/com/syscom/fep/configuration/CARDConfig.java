package com.syscom.fep.configuration;

import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysconf;

import java.util.List;

public class CARDConfig {
    public static final int SubSystemNo = 4;
    // private static DataTable _dtCARD;
    private static CARDConfig _instance = new CARDConfig();
    private String _APPKIND_RPT;
    private String _CARD_CARDLIST;
    private String _CARD_RPT;
    private String _CARDFDesBankID;
    private String _EncFileName;
    private String _ICCardOffsetFile_P2;
    private String _MakeCardAddressFile;
    private String _MakeCardFeedbackFile;
    private String _Track2AccountType;
    private String _Track2BinCode;
    private String _Track2ServiceId;
    private String _CardServerBatchFolder;
    private String _Error1002File;
    private String _TWDCompanyCode;
    private String _T24UserName;
    private String _T24SSCode;
    private String _PrintTOGOPWServiceIP;
    private String _PrintTOGOPWServicePort;
    private String _PrintCARDTOGOLetter;

    private CARDConfig() {
        fillDataToProperty();
    }

    public static CARDConfig getInstance() {
        return _instance;
    }

    /**
     * APPKIND_P2輸出報表檔名
     *
     * <value></value>
     *
     * @return
     */
    public final String getAppkindRpt() {
        return _APPKIND_RPT;
    }

    /**
     * CARD2製卡明細表檔名
     *
     * <value></value>
     *
     * @return
     */
    public final String getCardCardlist() {
        return _CARD_CARDLIST;
    }

    /**
     * CARD2下傳總表檔名
     *
     * <value></value>
     *
     * @return
     */
    public final String getCardRpt() {
        return _CARD_RPT;
    }

    /**
     * 製卡廠DES BankID
     *
     * <value></value>
     *
     * @return
     */
    public final String getCARDFDesBankID() {
        return _CARDFDesBankID;
    }

    /**
     * 中間檔檔名 (CARD1產出)
     *
     * <value></value>
     *
     * @return
     */
    public final String getEncFileName() {
        return _EncFileName;
    }

    /**
     * P2製卡檔名
     *
     * <value></value>
     *
     * @return
     */
    public final String getICCardOffsetFileP2() {
        return _ICCardOffsetFile_P2;
    }

    /**
     * 批次NotifySentCARD使用的裝封檔
     *
     * <value></value>
     *
     * @return
     */
    public final String getMakeCardAddressFile() {
        return _MakeCardAddressFile;
    }

    /**
     * 批次NotifySentCARD使用的裝封回饋檔
     *
     * <value></value>
     *
     * @return
     */
    public final String getMakeCardFeedbackFile() {
        return _MakeCardFeedbackFile;
    }

    /**
     * Track 2 帳號類別
     *
     * <value></value>
     *
     * @return
     */
    public final String getTrack2AccountType() {
        return _Track2AccountType;
    }

    /**
     * Track 2 Bin Code
     *
     * <value></value>
     *
     * @return
     */
    public final String getTrack2BinCode() {
        return _Track2BinCode;
    }

    /**
     * Track 2 Service Id
     *
     * <value></value>
     *
     * @return
     */
    public final String getTrack2ServiceId() {
        return _Track2ServiceId;
    }

    public final String printCARDTOGOLetter() {
        return _PrintCARDTOGOLetter;
    }

    /**
     * CARD 拆分後製發卡批次檔案目錄
     *
     * @return
     */
    public final String getCardServerBatchFolder() {
        return _CardServerBatchFolder;
    }

    /**
     * 取得ERROR1002用檔案名稱
     *
     * @return
     */
    public final String getError1002File() {
        return _Error1002File;
    }

    public String getTWDCompanyCode() {
        return _TWDCompanyCode;
    }

    public String getT24UserName() {
        return _T24UserName;
    }

    public String getT24SSCode() {
        return _T24SSCode;
    }

    public String getPrintTOGOPWServiceIP() {
        return _PrintTOGOPWServiceIP;
    }

    public String getPrintTOGOPWServicePort() {
        return _PrintTOGOPWServicePort;
    }

    public static CARDConfig Instance() {
        return _instance;
    }

    private void fillDataToProperty() {

        List<Sysconf> sysconfList = FEPCache.getSysconfList(SubSystemNo);
        String sysconfValue = null;
        for (Sysconf sysconf : sysconfList) {
            sysconfValue = sysconf.getSysconfValue();
            switch (sysconf.getSysconfName()) {
                case "APPKIND_RPT":
                    _APPKIND_RPT = DbHelper.toString(sysconfValue);
                    break;
                case "CARD_CARDLIST":
                    _CARD_CARDLIST = DbHelper.toString(sysconfValue);
                    break;
                case "CARD_RPT":
                    _CARD_RPT = DbHelper.toString(sysconfValue);
                    break;
                case "CARDFDesBankID":
                    _CARDFDesBankID = DbHelper.toString(sysconfValue);
                    break;
                case "Error1002File":
                    _Error1002File = DbHelper.toString(sysconfValue);
                    break;
                case "ICCardOffsetFile_P2":
                    _ICCardOffsetFile_P2 = DbHelper.toString(sysconfValue);
                    break;
                case "EncFileName":
                    _EncFileName = DbHelper.toString(sysconfValue);
                    break;
                case "MakeCardAddressFile":
                    _MakeCardAddressFile = DbHelper.toString(sysconfValue);
                    break;
                case "MakeCardFeedbackFile":
                    _MakeCardFeedbackFile = DbHelper.toString(sysconfValue);
                    break;
                case "Track2AccountType":
                    _Track2AccountType = DbHelper.toString(sysconfValue);
                    break;
                case "Track2BinCode":
                    _Track2BinCode = DbHelper.toString(sysconfValue);
                    break;
                case "Track2ServiceId":
                    _Track2ServiceId = DbHelper.toString(sysconfValue);
                    break;
                case "PrintTOGOPWServiceIP":
                    _PrintTOGOPWServiceIP = DbHelper.toString(sysconfValue);
                    break;
                case "PrintTOGOPWServicePort":
                    _PrintTOGOPWServicePort = DbHelper.toString(sysconfValue);
                    break;
            }
        }
    }

    public static Object getValueByName(String sysconfName) {
        Object val = null;
        return val;
    }
}
