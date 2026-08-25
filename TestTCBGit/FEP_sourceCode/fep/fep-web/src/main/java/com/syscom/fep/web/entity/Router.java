package com.syscom.fep.web.entity;

import com.syscom.fep.common.log.LogHelperFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard
 */
public enum Router {
    DEFAULT(StringUtils.EMPTY, StringUtils.EMPTY, "預設頁面", StringUtils.EMPTY),
    LOGIN(StringUtils.EMPTY, StringUtils.EMPTY, "登錄頁面", "login"),
    PING(StringUtils.EMPTY, StringUtils.EMPTY, "F5PING頁面", "ping/ping"),
    HOME(StringUtils.EMPTY, StringUtils.EMPTY, "主頁面", "home"),
    PAGE_403(StringUtils.EMPTY, StringUtils.EMPTY, "403頁面", "error/403"),
    PAGE_404(StringUtils.EMPTY, StringUtils.EMPTY, "404頁面", "error/404"),
    PAGE_406(StringUtils.EMPTY, StringUtils.EMPTY, "406頁面", "error/406"),
    PAGE_500(StringUtils.EMPTY, StringUtils.EMPTY, "500頁面", "error/500"),
    // INBK
    UI_010102("f01", "跨行OPC作業", "變更押碼基碼-0102", "inbk/UI_010102"),
    UI_013100("f01", "跨行OPC作業", "訊息通知-3100", "inbk/UI_013100"),
    UI_013101("f01", "跨行OPC作業", "應用系統連線作業-3101", "inbk/UI_013101"),
    UI_013106("f01", "跨行OPC作業", "應用系統異常連線作業-3106/7", "inbk/UI_013106"),
    UI_013109("f01", "跨行OPC作業", "應用系統狀態查詢-3109", "inbk/UI_013109"),
    UI_013114("f01", "跨行OPC作業", "應用系統狀態查詢-3114", "inbk/UI_013114"),    // Han add
    UI_013115("f01", "跨行OPC作業", "應用系統狀態查詢-3115", "inbk/UI_013115"),    // Han add
    UI_019010("f01", "跨行OPC作業", "查詢或變更系統狀態", "inbk/UI_019010"),
    UI_019020("f01", "跨行OPC作業", "查詢OPC交易記錄", "inbk/UI_019020"),
    UI_019020_Detail("f01", "跨行OPC作業", "查詢OPC交易記錄明細", "inbk/UI_019020_Detail", true),

    UI_012130("f02", "跨行交易作業", "傳送未完成交易處理結果-2130", "inbk/UI_012130"),
    UI_012140("f02", "跨行交易作業", "人工沖正請求交易-2140", "inbk/UI_012140"),
    UI_012280("f02", "跨行交易作業", "請求傳送滯留訊息-2280(本營業日)", "inbk/UI_012280"),
    UI_012290("f02", "跨行交易作業", "請求傳送交易結果-2290(前營業日)", "inbk/UI_012290"),
    UI_018300("f01", "跨行交易作業", "跨行系統營運資料補遺", "inbk/UI_018300"),
    UI_019030("f02", "跨行交易作業", "代理交易人工補 Confirm", "inbk/UI_019030"),
    UI_019120("f02", "跨行交易作業", "查詢財金傳送未完成交易-2120I", "inbk/UI_019120"),
    UI_019130("f02", "跨行交易作業", "查詢傳送未完成交易結果-2130I", "inbk/UI_019130"),
    UI_019140("f02", "跨行交易作業", "查詢代理單位人工沖正請求-2140I", "inbk/UI_019140"),
    UI_019150("f02", "跨行交易作業", "查詢財金人工沖正結果-2150I", "inbk/UI_019150"),
    UI_019270("f02", "跨行交易作業", "查詢存財金公司請求滯留訊息-2270I", "inbk/UI_019270"),
    UI_019280("f02", "跨行交易作業", "查詢請求傳送滯留訊息-2280I", "inbk/UI_019280"),
    UI_019290("f02", "跨行交易作業", "查詢請求傳送交易結果-2290I", "inbk/UI_019290"),
    UI_019040("f02", "跨行交易作業", "查詢預約轉帳交易結果", "inbk/UI_019040"),
    UI_019050("f02", "跨行交易作業", "查詢全國繳費整批轉即時交易結果", "inbk/UI_019050"),
    UI_019050_Detail("f02", "跨行交易作業", "查詢全國繳費整批轉即時交易結果", "inbk/UI_019050_Detail", true),
    UI_019050_A("f02", "FEP交易查詢", "FEPLOG 查詢", "inbk/UI_019050_A", true),
    UI_019050_B("f02", "FEPTXN交易查詢", "交易日誌(FEPTXN)查詢", "inbk/UI_019050_B", true),
    UI_019060("f02", "跨行交易作業", "查詢全繳API純代理交易結果", "inbk/UI_019060"),
    UI_019060_Detail("f02", "跨行交易作業", "查詢全繳API純代理交易結果", "inbk/UI_019060_Detail", true),
    UI_019041("f02", "跨行交易作業", "預約轉帳整批重發處理", "inbk/UI_019041"),
    UI_019141("f02", "跨行交易作業", "預約跨轉單筆重發處理", "inbk/UI_019141"),

    UI_015201("f03", "跨行清算作業", "查詢財金跨行結帳總計交易-5201", "inbk/UI_015201"),
    UI_015202("f03", "跨行清算作業", "查詢財金各項跨行借貸交易-5202", "inbk/UI_015202"),
    UI_019102("f03", "跨行清算作業", "查詢財金跨行結帳資料-5102I", "inbk/UI_019102"),
    UI_019201("f03", "跨行清算作業", "查詢清算總計資料(本行)", "inbk/UI_019201"),
    UI_019202("f03", "跨行清算作業", "查詢清算類別檔資料(本行)", "inbk/UI_019202"),
    UI_019313("f03", "跨行清算作業", "減少跨行基金餘額-櫃員 (For 5313)", "inbk/UI_019313"),
    UI_015313("f03", "跨行清算作業", "減少跨行基金餘額-放行-5313", "inbk/UI_015313"),
    UI_019302("f03", "跨行清算作業", "查詢分行清算日結檔", "inbk/UI_019302"),
    UI_019400("f03", "跨行清算作業", "地區分行清算日結檔", "inbk/UI_019400"),
    UI_019401("f03", "跨行清算作業", "過帳明細檔查詢", "inbk/UI_019401"),

    UI_019510("f03", "金融XML 2.0資金調撥", "查詢2700付款(代理)交易處理結果", "inbk/UI_019510"),
    UI_019520("f03", "金融XML 2.0資金調撥", "查詢2700收款(原存)交易處理結果", "inbk/UI_019520"),
    UI_019530("f03", "金融XML 2.0資金調撥", "資金調撥Pending交易確認及回覆", "inbk/UI_019530"),

    // RM
    UI_020060("f04", "跨行匯款管理", "往來行庫資料查詢", "rm/UI_020060"),
    UI_020060_Detail("f04", "跨行匯款管理", "往來行庫資料查詢", "rm/UI_020060_Detail", true),
    UI_020061("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061"),
    UI_020061_A("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_A", true),
    UI_020061_A_Detail("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_A_Detail", true),
    UI_020061_B("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_B", true),
    UI_020061_B_Detail("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_B_Detail", true),
    UI_020061_C("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_C", true),
    UI_020061_D("f04", "跨行匯款管理", "往來行庫資料維護", "rm/UI_020061_D", true),
    UI_028010("f04", "跨行匯款管理", "一般通訊", "rm/UI_028010"),
    UI_028020("f04", "跨行匯款管理", "對財金匯出交易狀況查詢", "rm/UI_028020"),
    UI_028030("f04", "跨行匯款管理", "更換跨行通匯基碼", "rm/UI_028030"),
    UI_028040("f04", "跨行匯款管理", "調整跨行電文序號", "rm/UI_028040"),
    UI_028050("f04", "跨行匯款管理", "調整跨行通匯序號", "rm/UI_028050"),
    UI_028060("f04", "跨行匯款管理", "調整匯兌收送狀態", "rm/UI_028060"),
    UI_028070("f04", "跨行匯款管理", "各行庫換KEY狀態查詢", "rm/UI_028070"),
    UI_028080("f04", "跨行匯款管理", "匯兌財金格式明細查詢", "rm/UI_028080"),
    UI_028090("f04", "跨行匯款管理", "匯入未完成查詢", "rm/UI_028090"),
    UI_028100("f04", "跨行匯款管理", "匯入補送中心", "rm/UI_028100"),
    UI_028110("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110"),
    UI_028110_Detail("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110_Detail", true),
    UI_028110_Detaile("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110_Detaile", true),
    UI_028110_RminView("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110_RminView", true),
    UI_028110_MsgoutView("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110_MsgoutView", true),
    UI_028110_MsginView("f04", "跨行匯款管理", "匯款交易查詢", "rm/UI_028110_MsginView", true),
    UI_028120("f04", "跨行匯款管理", "匯款換KEY交易查詢", "rm/UI_028120"),
    UI_028130("f04", "跨行匯款管理", "調整待匯出匯款順序", "rm/UI_028130"),
    UI_028140("f04", "跨行匯款管理", "調整匯款狀態", "rm/UI_028140"),
    UI_028180("f04", "跨行匯款管理", "匯款確認取消交易輸入(OP)", "rm/UI_028180"),
    UI_028190("f04", "跨行匯款管理", "一般通訊確認取消交易", "rm/UI_028190"),
    UI_028250("f04", "跨行匯款監控", "調整程式執行狀態交易", "rm/UI_028250"),
    UI_028260("f04", "跨行匯款監控", "修改匯出退匯資料", "rm/UI_028260"),
    UI_028260_Detail("f04", "跨行匯款監控", "修改匯出退匯資料", "rm/UI_028260_Detail", true),
    UI_028270("f04", "跨行匯款管理", "調整AML傳送狀態", "rm/UI_028270"),
    UI_028271("f04", "跨行匯款管理", "調整AML傳送狀態-主管", "rm/UI_028271"),
    UI_028021("f04", "跨行匯款管理", "通匯上行電文序號查詢", "rm/UI_028021"),
    UI_028022("f04", "跨行匯款管理", "通匯下行電文序號查詢", "rm/UI_028022"),
    UI_028023("f04", "跨行匯款管理", "通匯待解筆數查詢", "rm/UI_028023"),
    UI_020062("f04", "跨行匯款管理", "調整往來行庫服務狀態", "rm/UI_020062"),
    UI_028280("f04", "跨行匯款管理", "電文重送AML人工作業", "rm/UI_028280"),
    UI_028281("f04", "跨行匯款管理", "電文BypassAML人工作業", "rm/UI_028281"),
    UI_028290("f04", "跨行匯款管理", "調整匯款暫禁記號", "rm/UI_028290"),

    // 跨行匯款監控
    UI_028150("f05", "跨行匯款監控", "匯出/匯入狀態監控畫面", "rm/UI_028150"),
    UI_028160("f05", "跨行匯款監控", "匯款交易監視作業", "rm/UI_028160"),
    UI_028170("f05", "跨行匯款監控", "一般通訊狀態監視作業", "rm/UI_028170"),
    UI_028200("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200"),
    UI_028200_1("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200_1", true),
    UI_028200_1_Detail("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200_1_Detail", true),
    UI_028200_2("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200_2", true),
    UI_028200_2_1("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200_2_1", true),
    UI_028200_2_1_Detail("f05", "跨行匯款管理", "大批匯款監控啟動", "rm/UI_028200_2_1_Detail", true),
    UI_028220("f05", "跨行匯款監控", "大批匯款回饋監控啟動", "rm/UI_028220"),
    UI_028220_Detail("f05", "跨行匯款監控", "大批匯款回饋監控啟動", "rm/UI_028220_Detail", true),
    UI_028230("f05", "跨行匯款監控", "FEDI轉通匯回饋監控啟動", "rm/UI_028230"),
    UI_028240("f05", "跨行匯款監控", "啟動批量修改重送次數", "rm/UI_028240"),

    // DB
    UI_070030("f12", "維護", "跨行系統參數維護", "dbmaintain/UI_070030", true),
    UI_070030_Detail("f12", "維護", "跨行系統參數維護", "dbmaintain/UI_070030_Detail", true),
    UI_070050("f12", "維護", "主機逾時明細維護", "dbmaintain/UI_070050", true),
    UI_070051("f12", "維護", "主機逾時明細查詢", "dbmaintain/UI_070051", true),

    // ATMMON
    UI_019070("f06", "FEP交易查詢", "查詢電子支付交易結果", "inbk/UI_019070"),
    UI_019070_Detail("f06", "FEP交易查詢", "查詢電子支付交易結果", "inbk/UI_019070_Detail"),
    UI_060290("f06", "FEP服務管理", "FEP 服務/通路/線路控制", "atmmon/UI_060290"),
    UI_060291("f06", "FEP服務管理", "FEP 服務/通路/線路控制", "atmmon/UI_060291"),
    UI_060296("f06", "FEP交易查詢", "FEP 服務/通路/線路查詢", "atmmon/UI_060296"),
    UI_060550("f06", "FEP交易查詢", "交易日誌(FEPTXN)查詢", "atmmon/UI_060550"),
    UI_060560("f06", "FEP交易查詢", "歷史交易日誌(FEPTXN)查詢", "atmmon/UI_060560"),
    UI_060550_Ucdid("f06", "FEP交易查詢", "交易日誌(UCDID)查詢", "atmmon/UI_060550_Ucdid", true),
    UI_060550_Detail("f06", "FEP交易查詢", "交易日誌(FEPTXN)查詢", "atmmon/UI_060550_Detail", true),
    UI_060560_Ucdid("f06", "FEP交易查詢", "歷史交易日誌(UCDID)查詢", "atmmon/UI_060560_Ucdid", true),
    UI_060560_Detail("f06", "FEP交易查詢", "歷史交易日誌(FEPTXN)查詢", "atmmon/UI_060560_Detail", true),
    UI_060610_A("f06", "FEP交易查詢", "FEPLOG 查詢", "atmmon/UI_060610_A", true),
    UI_060610_A_Detail("f06", "FEP交易查詢", "FEPLOG 查詢", "atmmon/UI_060610_A_Detail", true),
    UI_060078("f06", "FEP交易查詢", "ATM(ATMC)收付累計查詢", "atmmon/UI_060078"),
    UI_060078_Detail("f06", "FEP交易查詢", "ATM(ATMC)收付累計查詢", "atmmon/UI_060078_Detail", true),
    UI_060295("f06", "FEP交易查詢", "FEP 服務/通路/線路查詢", "atmmon/UI_060295"),
    UI_060610("f06", "FEP交易查詢", "交易流程日誌(FEPLOG)查詢", "atmmon/UI_060610"),
    UI_060610_Detail("f06", "FEP交易查詢", "交易流程日誌(FEPLOG)查詢", "atmmon/UI_060610_Detail", true),
    UI_060550_A("f06", "FEP交易查詢", "交易日誌(FEPTXN)查詢", "atmmon/UI_060550_A", true),
    UI_060550_A_Detail("f06", "FEP交易查詢", "交易日誌(FEPTXN)查詢", "atmmon/UI_060550_A_Detail", true),

    // 批次管理
    UI_000200("f07", "批次管理", "批次程序維護", "batch/UI_000200"),
    UI_000200_Detail("f07", "批次管理", "批次程序維護", "batch/UI_000200_Detail", true),
    UI_000100("f07", "批次管理", "批次作業管理", "batch/UI_000100"),

    UI_000100_C("f07", "批次管理", "批次作業管理", "batch/UI_000100_C", true),
    UI_000100_Detail("f07", "批次管理", "批次作業管理", "batch/UI_000100_Detail", true),
    UI_000300("f07", "批次管理", "批次作業歷程查詢", "batch/UI_000300"),
    UI_000400("f07", "批次管理", "TWS批次執行記錄", "batch/UI_000400"),
    UI_000110("f07", "批次管理", "批次啟動作業", "batch/UI_000110"),
    UI_000120("f07", "批次管理", "批次啟動作業", "batch/UI_000120", true),
    UI_000140("f07", "批次管理", "批次排程檢視", "batch/UI_000140"),
    UI_000700("f07", "批次管理", "每日批次查詢", "batch/UI_000700"),

    // 權限管理
    UI_080010("f08", "權限管理", "使用者資料維護", "common/UI_080010"),
    UI_080010_Add("f08", "權限管理", "使用者資料異動", "common/UI_080010_Add"),
    UI_080010_Detail("f08", "權限管理", "使用者資料明細", "common/UI_080010_Detail"),
    UI_080030("f08", "權限管理", "群組與功能維護", "common/UI_080030"),
    UI_080040("f08", "權限管理", "角色使用者維護", "common/UI_080040"),
    UI_080050("f08", "權限管理", "使用者群組維護", "common/UI_080050"),
    UI_080060("f08", "權限管理", "變更使用者密碼", "common/UI_080060"),
    UI_080310("f08", "權限管理", "使用者群組查詢", "common/UI_080310"),
    UI_080310_Detail("f08", "權限管理", "使用者群組查詢", "common/UI_080310_Detail", true),
    UI_080320("f08", "權限管理", "使用者資料查詢", "common/UI_080320"),
    UI_080320_Detail("f08", "權限管理", "使用者資料查詢", "common/UI_080320_Detail", true),

    // 系統管理
    UI_080100("f09", "系統管理", "系統及服務監控", "common/UI_080100"),
    UI_080110("f09", "系統管理", "MQ線程數設定", "common/UI_080110"),
    UI_080120("f09", "系統管理", "Service線程數設定", "common/UI_080120"),
    UI_060620("f09", "系統管理", "事件管理系統(EMS)日誌查詢", "atmmon/UI_060620"),
    UI_060620_A("f09", "系統管理", "事件管理系統(EMS)日誌查詢", "atmmon/UI_060620_A", true),
    UI_060620_B("f09", "系統管理", "EMS訊息明細畫面", "atmmon/UI_060620_B", true),
    UI_060621("f09", "系統管理", "事件管理系統(EMS)日誌通知維護", "atmmon/UI_060621"),
    UI_060621_Detail("f09", "系統管理", "事件管理系統(EMS)日誌通知維護", "atmmon/UI_060621_Detail", true),
    UI_060630("f09", "系統管理", "錯誤訊息查詢", "atmmon/UI_060630"),
    UI_060630_Detail("f09", "系統管理", "錯誤訊息查詢", "atmmon/UI_060630_Detail", true),
    UI_060070("f09", "系統管理", "系統共用參數設定", "atmmon/UI_060070"),    //han add
    UI_060070_Detail("f09", "系統管理", "系統共用參數設定", "atmmon/UI_060070_Detail"),    //han add
    UI_060080("f09", "系統管理", "ATM異常訊息維護", "atmmon/UI_060080"),
    UI_060080_Detail("f09", "系統管理", "ATM異常訊息維護", "atmmon/UI_060080_Detail"),
    UI_080070("f09", "系統管理", "使用者軌跡查詢", "common/UI_080070"),
    UI_080070_Detail("f09", "系統管理", "使用者軌跡查詢明細畫面", "common/UI_080070_Detail", true),
    UI_080500("f09", "系統管理", "FISCGW線路及服務切換", "common/UI_080500"),
    UI_130900("f09", "系統管理", "報表分行維護", "common/UI_130900"),
    UI_130900_Detail("f09", "系統管理", "報表分行維護", "common/UI_130900_Detail", true),
    UI_080150("f09", "系統管理", "匯入KMS基碼資料", "common/UI_080150"),
    UI_080170("f09", "系統管理", "調整及監控CBSGW的線路狀態", "common/UI_080170"),
    UI_080180("f09", "系統管理", "調整及監控IMSGW的線路狀態", "common/UI_080180"),

    //參數檔案維護
    UI_070060("f10", "維護", "錯誤訊息維護MSFFILE", "dbmaintain/UI_070060"),
    UI_070060_Detail("f10", "維護", "錯誤訊息維護MSFFILE", "dbmaintain/UI_070060_Detail", true),
    UI_070510("f10", "維護", "偽BIN資料檔HOTBIN", "dbmaintain/UI_070510"), //han add
    UI_070510_Insert("f10", "維護", "偽BIN資料檔HOTBIN", "dbmaintain/UI_070510_Insert"), //han add

    UI_070310("f10", "維護", "信用卡BIN維護", "dbmaintain/UI_070310"),
    UI_070310_Detail("f10", "維護", "信用卡BIN維護", "dbmaintain/UI_070310_Detail"),
    UI_070020("f10", "維護", "日曆檔維護BSDAYS", "dbmaintain/UI_070020", true),
    UI_070020_Detail("f10", "維護", "日曆檔維護BSDAYS", "dbmaintain/UI_070020_Detail", true),
    UI_070070("f10", "維護", "交易分PCODE切換", "dbmaintain/UI_070070"),
    //ATM機台管理
    UI_060010_Q("f11", "ATM機台管理", "ATM基本資料查詢", "atmmon/UI_060010_Q"),
    UI_060010_Q_Detail("f11", "ATM機台管理", "ATM基本資料查詢", "atmmon/UI_060010_Q_Detail", true),
    UI_060010("f12", "ATM機台管理", "ATM憑證版本維護", "atmmon/UI_060010"),
    UI_060010_Detail("f12", "ATM機台管理", "ATM憑證版本維護", "atmmon/UI_060010_Detail", true),

    //ATM營況管理
    UI_130100("f14", "ATM營況管理", "手續費參數檔維護", "osm/UI_130100"),
    UI_130100_Detail("f14", "ATM營況管理", "手續費參數檔維護", "osm/UI_130100_Detail"),

    DEMO("f99", "功能演示", "DEMO畫面", "demo/Demo"),
    DEMO_Detail("f99", "功能演示", "DEMO畫面", "demo/Demo_Detail", true),
    DEMOTab("f99", "功能演示", "DEMO頁簽", "demo/DemoTab"),
    DEMOCalendar("f99", "功能演示", "DEMO日曆", "demo/DemoCalendar"),
    DemoDownload("f99", "功能演示", "DEMO檔案下載", "demo/DemoDownload");
    private String parentId, parentName, name, url, view, code = StringUtils.EMPTY, program;
    private boolean sub;

    private Router(String parentId, String parentName, String name, String view) {
        this(parentId, parentName, name, view, false);
    }

    private Router(String parentId, String parentName, String name, String view, boolean sub) {
        this.parentId = parentId;
        this.parentName = parentName;
        this.name = name;
        this.view = view;
        this.sub = sub;
        if (StringUtils.isBlank(parentId) && StringUtils.isBlank(parentName)) {
            this.url = StringUtils.join("/", view);
        } else {
            this.url = StringUtils.join("/", parentId, "/", view, "/index");
        }
        this.code = getCode(view);
        this.program = getProgram(view);
    }

    public static String getCode(String view) {
        try {
            String str = "/UI_";
            int index = view.indexOf(str);
            if (index > 0) {
                int beginIndex = index + str.length();
                int endIndex = view.indexOf("_", beginIndex + 1);
                if (endIndex > 0) {
                    return view.substring(beginIndex, endIndex);
                } else {
                    return view.substring(beginIndex, view.length());
                }
            }
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        int index = view.indexOf("/");
        if (index >= 0) {
            return view.substring(index + 1);
        }
        return view;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getView() {
        return view;
    }

    public boolean isSub() {
        return sub;
    }

    public String getCode() {
        return code;
    }

    public String getProgram() {
        return program;
    }

    /**
     * just for test before SAFEAA go live
     *
     * @return
     */
    public static List<Router> getMenu() {
        List<Router> result = new ArrayList<>();
        for (Router router : values()) {
            if (router.ordinal() >= UI_010102.ordinal() && !router.sub) {
                result.add(router);
            }
        }
        return result;
    }

    // public static Router fromCode(String code) {
    // if (StringUtils.isBlank(code)) {
    // return null;
    // }
    // for (Router router : values()) {
    // if (router.getCode().equals(code) && !router.isSub()) {
    // return router;
    // }
    // }
    // return null;
    // }

    public static Router fromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        for (Router router : values()) {
            if (router.getUrl().equals(url)) {
                return router;
            }
        }
        return null;
    }

    public static Router fromView(String view) {
        if (StringUtils.isBlank(view)) {
            return null;
        }
        for (Router router : values()) {
            if (router.getView().equals(view)) {
                return router;
            }
        }
        return null;
    }

    public static String getProgram(String view) {
        try {
            String str = "/UI_";
            int index = view.indexOf(str);
            if (index > 0) {
                return view.substring(index + 1);
            }
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().warn(e, e.getMessage());
        }
        int index = view.indexOf("/");
        if (index >= 0) {
            return view.substring(index + 1);
        }
        return view;
    }
}
