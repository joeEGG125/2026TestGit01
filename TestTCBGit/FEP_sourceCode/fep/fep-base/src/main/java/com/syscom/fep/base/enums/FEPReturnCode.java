package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * FEP共用訊息
 * Generate Date : 2019/10/21
 * Generate Program : GenFEPReturnCode.Form1
 *
 * @author Richard
 */
public enum FEPReturnCode {
    /**
     * ===================== Base Return Code =====================
     */
    /**
     * 檢核成功
     */
    Normal(0),

    /**
     * 處理失敗
     */
    Abnormal(10001),

    /**
     * 未知的MessageID(無法找到要叫那個AA)
     */
    MessageIDError(10002),

    /**
     * 拆解電文錯誤
     */
    ParseTelegramError(10003),

    /**
     * Mail通知失敗
     */
    MailFail(10004),

    /**
     * 一次只能用一個通訊端位址 (通訊協定/網路位址/連接埠)。
     */
    LocalPortInUse(10048),

    /**
     * 連線已被您主機上的軟體中止。
     */
    SocketBrokenByGateway(10053),

    /**
     * 遠端主機已強制關閉一個現存的連線。
     */
    SocketBrokenByRemote(10054),

    /**
     * 連線嘗試失敗，因為連線對象有一段時間並未正確回應，或是連線建立失敗，因為連線的主機無法回應。
     */
    ConnectRemoteTimeOut(10060),

    /**
     * 程式發生例外
     */
    ProgramException(10099),

    /**
     * 自行服務暫停
     */
    IntraBankServiceStop(10101),

    /**
     * 提款暫停服務
     */
    WithdrawServiceStop(10102),

    /**
     * 跨行暫停服務
     */
    InterBankServiceStop(10103),

    /**
     * 帳務主機暫停服務
     */
    CBSStopService(10104),

    /**
     * 自行{業務別}交易暫停服務
     */
    IntraBankSingleServiceStop(10105),

    /**
     * 自行{業務別}交易開始服務
     */
    IntraBankSingleServiceStart(10106),

    /**
     * 代理行{業務別}交易暫停服務
     */
    AgentBankSingleServiceStop(10107),

    /**
     * 代理行{業務別}交易開始服務
     */
    AgentBankSingleServiceStart(10108),

    /**
     * 原存行{業務別}交易暫停服務
     */
    IssueBankSingleServiceStop(10109),

    /**
     * 原存行{業務別}交易開始服務
     */
    IssueBankSingleServiceStart(10110),

    /**
     * {通道或線路}暫停服務
     */
    ChannelServiceStop(10111),

    /**
     * {通道或線路}開始服務
     */
    ChannelServiceStart(10112),

    /**
     * 主機回覆格式錯誤
     */
    CBSResponseFormatError(10200),

    /**
     * 主機回覆錯誤
     */
    CBSResponseError(10201),

    /**
     * 信用卡主機回覆錯誤
     */
    ASCResponseError(10202),

    /**
     * 主機回應逾時
     */
    HostResponseTimeout(10203),

    /**
     * 信用卡主機回應逾時
     */
    CreditResponseTimeout(10204),

    /**
     * SMS主機回應逾時
     */
    SMSResponseTimeout(10205),

    /**
     * SMS服務回應逾時
     */
    SMSServiceResponseTimeout(10206),

    /**
     * 傳送FISCGW_ATM WebService 錯誤
     */
    FISCGWATMSendError(10207),

    /**
     * 接收FISCGW_ATM WebService 錯誤
     */
    FISCGWATMRcvError(10208),

    /**
     * 接收敲價系統 WebService 錯誤
     */
    ETSRcvError(10209),

    /**
     * 普發現金API回覆逾時
     */
    UCDResponseTimeout(10210),

    /**
     * 財金營業日已更換
     */
    FISCBusinessDateChanged(10211),

    /**
     * 執行換日發生錯誤
     */
    BusinessDateChangeError(10212),

    /**
     * 財金營業日 CHANGE TO (YYYYMMDD)
     */
    FISCBusinessDateChangeToYMD(10213),

    /**
     * 自行營業日 CHANGE TO (YYYYMMDD)
     */
    CBSBusinessDateChangeToYMD(10214),

    /**
     * 地區主機帳務狀態切換
     */
    CBSModeChange(10215),

    /**
     * EAINET財金營業日 CHANGE TO (YYYYMMDD)
     */
    EAINETBusinessDateChangeToYMD(10216),

    /**
     * EAINET全行暫停服務 CHANGE TO (Y/N)
     */
    EAINETATMStopChangeToYN(10217),

    /**
     * 預約跨轉整批重發處理
     */
    RETFRReRun(10218),

    /**
     * 預約跨轉單筆重發
     */
    RETFRSingle(10219),

    /**
     * 普發現金API回覆錯誤
     */
    UCDResponseError(10221),

    /**
     * 原交易帳號有誤
     */
    OriginalACTNONotMatch(10301),

    /**
     * 原交易金額有誤
     */
    OriginalAmountNotMatch(10302),

    /**
     * 取ATM交易序號錯誤
     */
    GetATMSeqNoError(10303),

    /**
     * 主機交易序號不符
     */
    CBSTXSeqNoNotMatch(10304),

    /**
     * 收到沖正交易，找無原交易
     */
    OriginalMessageNotFound(10305),

    /**
     * 超過限額
     */
    OverLimit(10311),

    /**
     * 超過EMV累計限額
     */
    OverEMVLimit(10312),

    /**
     * 銷帳編號檢查碼有誤
     */
    RECNOError(10313),

    /**
     * 跨行存款，超過累計限額
     */
    OverODTLimit(10314),

    /**
     * 比對主機回應電文資料不符
     */
    CompareTOTANotMatch(10315),

    /**
     * Garbage資料
     */
    GarbageData(10316),

    /**
     * FTP上載錯誤
     */
    FTPUpLoadError(10401),

    /**
     * FTP下載錯誤
     */
    FTPDownLoadError(10402),

    /**
     * 您的理財密碼設定不成功，請洽本行客服專員
     */
    IPINPwdError(10501),

    /**
     * 向聯徵中心查驗P33資訊中
     */
    P33Process(10612),

    /**
     * 依聯徵中心查驗P33之結果，拒絕交易。
     */
    P33Reject(10613),

    /**
     * 聯徵中心系統維護中，暫無法查驗P33資訊
     */
    P33Maintain(10614),

    /**
     * 帳戶/卡片為第一次進行電子支付交易，應進行身分確認
     */
    P33Validate(10615),

    /**
     * 使用者身分確認結果有誤,，存款單位拒絕交易
     */
    P33IssuerReject(10616),

    /**
     * 主機回覆帳號狀態錯誤
     */
    CheckCBSActnoError(10617),

    /**
     * 查無帳號資料
     */
    NoODSActno(10618),

    /**
     * 已存在活卡
     */
    LiveCardExist(10619),

    /**
     * 已存在未啟用卡片
     */
    UnActiveCardExist(10620),

    /**
     * 訊息格式或內容編輯錯誤
     */
    MessageFormatError(11011),

    /**
     * 檢核電文的BITMAP錯誤
     */
    CheckBitMapError(11012),

    /**
     * 發信單位未參加該跨行業務
     */
    SenderBankNotAttendBusiness(11021),

    /**
     * 發信單位該項跨行業務停止或暫停營業
     */
    SenderBankServiceStop(11022),

    /**
     * 發信單位主機未在跨行作業運作狀態
     */
    SenderBankOperationStop(11023),

    /**
     * 收信單位未參加該跨行業務
     */
    ReceiverBankNotAttendBusiness(11024),

    /**
     * 收信單位該項跨行業務停止或暫停營業
     */
    ReceiverBankServiceStop(11025),

    /**
     * 收信單位主機未在跨行作業運作狀態
     */
    ReceiverBankOperationStop(11026),

    /**
     * 銀行內部停止該項跨行業務
     */
    StopServiceByInternal(11027),

    /**
     * 不允許信用卡交易
     */
    CCardServiceNotAllowed(11028),

    /**
     * 押碼基碼( KEY )不同步
     */
    KeySyncError(11031),

    /**
     * CD/ATM 共用系統交易之客戶亂碼基碼不同步
     */
    PPKeySyncError(11033),

    /**
     * 訊息欄位資料與原交易資料不相符
     */
    OriginalMessageDataError(11041),

    /**
     * 端末機故障
     */
    TerminalError(11051),

    /**
     * 財金回覆逾時
     */
    FISCTimeout(11061),

    /**
     * 財金公司該項跨行業務暫停或停止營業
     */
    FISCServiceStop(11071),

    /**
     * 財金公司主機未在跨行作業運作狀態
     */
    FISCOperationStop(11072),

    /**
     * 無效之訊息類別代碼(MESSAGE TYPE)或交易類別代碼(PROCESSING CODE)
     */
    MessageTypeError(11101),

    /**
     * Trace Number 重覆
     */
    TraceNumberDuplicate(11102),

    /**
     * 訊息與原跨行交易之執行狀態不符
     */
    OriginalMessageError(11103),

    /**
     * STAN 非數字
     */
    STANError(11104),

    /**
     * 訊息發送者ID與訊息內之TXN. SOURCEINSTITUTE ID. 或TXN. DESTINATIONID. 不符
     */
    SenderIdError(11105),

    /**
     * 訊息內容錯誤
     */
    BitFlagError(11106),

    /**
     * 無效之回應代碼
     */
    InvalidResponseCode(11107),

    /**
     * 跨行作業系統或應用系統狀態已更新
     */
    INBKStatusUpdateAlready(11301),

    /**
     * 交易訊息內之代碼( ID )欄錯誤
     */
    APIDError(11302),

    /**
     * 跨行作業系統或應用系統狀態不符
     */
    INBKStatusError(11303),

    /**
     * 參加單位該項跨行業務不可PRECHECK-OUT
     */
    PrecheckOutNotAllowed(11321),

    /**
     * 財金公司之STOP STRVICE 不可執行
     */
    StopServiceToFISCNotAllowed(11327),

    /**
     * 參加單位AP CHECKOUT 時間未到，該交易不可執行
     */
    CheckOutTimingError(11328),

    /**
     * 訊息內AP DATA ELEMENT 之SYNC.CHECK ITEM 不相符
     */
    KeySyncNotMatch(11331),

    /**
     * CHANGE KEY CALL 正進行中
     */
    ChangeKeyCallIsProcessing(11332),

    /**
     * CHANGE KEY CALL 訊息內之RAN-DOM NUMBER 錯誤
     */
    RandomNumberError(11333),

    /**
     * 交易金額或金融卡國際提款交易金額錯誤
     */
    TxAmountError(11423),

    /**
     * 財金回覆-無此交易
     */
    TransactionNotFound(11471),

    /**
     * **交易狀態不明或 Protocal 錯誤
     */
    ProtocalError(11472),

    /**
     * 交易狀態有誤
     */
    StatusNotMatch(11473),

    /**
     * 拒絕交易，需進行人工授權交易
     */
    RejectFallBack(11476),

    /**
     * 國外提款超過當日累計限制次數
     */
    OverOWDCnt(11477),

    /**
     * 身份証號／營利事業統一編號有誤
     */
    CheckIDNOError(11486),

    /**
     * 無此帳戶(依據委託單位、繳費類別及費用代號)
     */
    NPSNotFound(11488),

    /**
     * 特店檔不存在
     */
    MerchentIDNotFound(11489),

    /**
     * 繳稅類別不存在
     */
    PayTypeNotFound(11501),

    /**
     * 通道EJ重複
     */
    ChannelEJFNODuplicate(11502),

    /**
     * 中文欄位或變動長度欄位長度錯誤
     */
    LengthError(11504),

    /**
     * 財金公司訊息通知 -- (NOTICE DATA)
     */
    FISCNoticeCall(11601),

    /**
     * 財金公司不明訊息通知 -- (GARBLED HEADER)
     */
    FISCGarbledMessage(11602),

    /**
     * 財金公司 KEY CHANGE CALL 完成
     */
    FISCKeyChangeCall(11603),

    /**
     * 財金公司 KEY SYNC CALL 完成
     */
    FISCKeySyncCall(11604),

    /**
     * 財金公司(系統名稱) PRE-CHECK OUT 完成
     */
    FISCPreCheckOut(11605),

    /**
     * 財金公司(系統名稱) CHECK OUT 完成
     */
    FISCCheckOut(11606),

    /**
     * 會員單位訊息通知 -- (NOTICE DATA)
     */
    MBankNoticeCall(11607),

    /**
     * 會員單位 CHANGE KEY REQUEST 完成
     */
    MBankChangeKeyRequest(11608),

    /**
     * (系統名稱) CHECK IN 完成
     */
    MBankCheckIn(11609),

    /**
     * (系統名稱) EX. CHECK IN 完成
     */
    MBankExceptionalCheckIn(11610),

    /**
     * (系統名稱) EX. CHECK OUT 完成
     */
    MBankExceptionalCheckOut(11611),

    /**
     * 跨行可用餘額低於最低限額
     */
    FISCAvailableBalanceUnderLimit(11621),

    /**
     * 增加跨行業務基金通知(或沖正)
     */
    FISCBalanceIncrement(11622),

    /**
     * 減少跨行業務基金通知
     */
    FISCBalanceReduce(11623),

    /**
     * 財金公司跨行結帳資料相符(或不符)
     */
    FISCCloseBalanceDeliver(11624),

    /**
     * 會員單位跨行作業總計查詢完成
     */
    MBankCloseBalanceInquery(11625),

    /**
     * 會員單位減少跨行業務基金請求
     */
    MBankBalanceReduceRequest(11626),

    /**
     * 財金公司傳送未完成交易
     */
    FISCUnfinishedTransaction(11631),

    /**
     * CD/ATM 交易人工沖正結果
     */
    FISCErrorCorrectionResult(11632),

    /**
     * 卡號有誤不更新磁軌且不留卡
     */
    UPBinError(11633),

    /**
     * 通道名稱錯誤
     */
    ChannelNameError(11634),

    /**
     * 跨行業務基金可用餘額通知
     */
    FISCUseBalance(11635),

    /**
     * 跨國沖正結果通知
     */
    FISCICReverseNotice(11636),

    /**
     * 國外提領限制記號關閉
     */
    OWDGPClose(11637),

    /**
     * 購貨日期有誤
     */
    MerchantDateError(11638),

    /**
     * VATXN資料不存在
     */
    VATXNNotFound(11641),

    /**
     * 無法連線至遠端主機
     */
    CanNotConnectRemoteHost(12000),

    /**
     * 電文序號錯誤
     */
    FISCNumberError(12101),

    /**
     * 匯款金額超過限額
     */
    RemitAmoutExceed(12102),

    /**
     * 發(收)信單位與發(收)信行代號不符
     */
    SenderBankWithAcceptCodeNotMatch(12106),

    /**
     * 原交易訊息類別錯誤
     */
    TradePcodeError(12107),

    /**
     * 解款單位無此分行
     */
    ReciverBankNotBranch(12113),

    /**
     * 收信行未參加該跨行業務
     */
    ReceiverBankRMNotAttendBusiness(12118),

    /**
     * 發信行未參加該跨行業務
     */
    SenderBankRMNotAttendBusiness(12119),

    /**
     * 通匯序號重覆
     */
    RemitsNumberRepeated(12120),

    /**
     * 退還匯款之原匯款交易不存在
     */
    ReturnRemitOriginalDatanotExist(12121),

    /**
     * 退還匯款與原匯款交易資料比對不符
     */
    ReturnRemitRemitOriginalDataNotMatch(12122),

    /**
     * 退還匯款已退還
     */
    ReturnRemithasreturned(12123),

    /**
     * 必要輸入項目值不得為空白或為零
     */
    NotBlankOrnot0(12201),

    /**
     * 輸入查詢日期不得大於目前日期
     */
    QueryDateMustLessThanToday(12202),

    /**
     * 無此銀行代號
     */
    BankNumberNotFound(12203),

    /**
     * 指定批號錯誤
     */
    AppointedBatchNumberError(12204),

    /**
     * 媒體批號錯誤
     */
    MediaBatchNumberError(12205),

    /**
     * 匯款種類錯誤
     */
    RemitCategoryError(12206),

    /**
     * 整批匯款媒體檔案錯誤
     */
    RemitMediaFileError(12207),

    /**
     * 序號錯誤
     */
    RecordNumberError(12208),

    /**
     * 匯出記錄狀態錯誤
     */
    RemitStatusError(12209),

    /**
     * 記錄狀態錯誤
     */
    RecordStatusError(12210),

    /**
     * 匯款金額,現金/轉帳類別,不在定義範圍內
     */
    RemitAmountNotCashNotTransfer(12211),

    /**
     * 匯款手續費,現金/轉帳類別,不在定義範圍內
     */
    RemitServicechargeNotCashNotTransfer(12212),

    /**
     * 匯款人名稱, 收款人名稱不得空白
     */
    RemitNameReceiverNameNotBlank(12213),

    /**
     * 中文欄位長度錯誤
     */
    ChineseRowError(12214),

    /**
     * 交易金額小於或等於零
     */
    TradeAmountMoneyLessThan0(12215),

    /**
     * 收款人帳號輸入錯誤
     */
    ReceiverAccountError(12216),

    /**
     * EJFNO取號有誤
     */
    EJFNOTakeNumberError(12217),

    /**
     * 此交易必須主管授權,主管代號不得為空白
     */
    SuperintendentCodeNoBlank(12218),

    /**
     * 匯款日期輸入錯誤
     */
    RemitDateError(12219),

    /**
     * 匯款類別輸入錯誤
     */
    RemitKindError(12220),

    /**
     * 發送確認別輸入錯誤
     */
    KindError(12221),

    /**
     * 帳務類別輸入錯誤
     */
    RemitTXKindError(12222),

    /**
     * 收款人帳號必須輸入
     */
    RemitInAccountIsMandatory(12223),

    /**
     * 暫收付科目必須輸入
     */
    SuspendAPNOIsMandatory(12224),

    /**
     * 退匯理由輸入錯誤
     */
    BackReasonError(12225),

    /**
     * 此交易必須主管授權,主管代號1,2必須有值且不能相同
     */
    SuperintendentCodeNotAvailable(12226),

    /**
     * 暫收付科目實際來源帳號必須輸入
     */
    SuspendAccountIsMandatory(12227),

    /**
     * 備註必須輸入
     */
    RemarkIsMandatory(12228),

    /**
     * 虛擬帳號必須輸入
     */
    DummyAccountIsMandatory(12229),

    /**
     * 組RM回應電文錯誤
     */
    PrepareRMResError(12230),

    /**
     * 帳號必須輸入
     */
    AccountIsMandatory(12231),

    /**
     * 輸入的交易類別代碼不符合
     */
    TradePcodeNotMatch(12301),

    /**
     * 匯款金額 + 實收手續費不等於金額合計
     */
    AmountNotMatch(12302),

    /**
     * 輸入的交易類別代碼與匯款主檔的匯款種類不符合
     */
    TradePcodeAndHostNotMatch(12303),

    /**
     * 作業代號不符
     */
    HomeworkCodeNotMatch(12304),

    /**
     * 輸入的匯款交易類別代碼不符合
     */
    RemitPcodeNotMatch(12305),

    /**
     * 輸入的序號類別代碼不符合
     */
    TradeCodeNotMatch(12306),

    /**
     * 輸入的設定FLAG不符合
     */
    FlagNotMatch(12307),

    /**
     * 應付財金手續費輸入不符
     */
    FISCHandleChargeNotMatch(12308),

    /**
     * 應收手續費輸入不符
     */
    HandleChargeNotMatch(12309),

    /**
     * 匯款金額與主檔不符
     */
    RemitAmoutNotMach(12340),

    /**
     * 解款單位與主檔不符
     */
    ReceiverBankNotMatch(12341),

    /**
     * 合計筆數金額與明細不符
     */
    TotalamtNotMatch(12342),

    /**
     * 本交易不得沖正
     */
    TransactionNotAllowReverse(12401),

    /**
     * 跨行匯出，解款行不得為本銀行
     */
    InterBankRemitNotOriginallyBank(12402),

    /**
     * 本行未參加財金匯款，不得進行此交易
     */
    NotParticipationRemittanceNotTransaction(12403),

    /**
     * 本行禁止匯款匯出，不得進行此交易
     */
    ProhibitionCollectsNotTransaction(12404),

    /**
     * 本行禁止匯款櫃員登錄，不得進行此交易
     */
    ProhibtionRemittanceRegisteringNotTransaction(12405),

    /**
     * 本行匯兌已結帳，不得進行此交易
     */
    RemittancePayinNotTransaction(12406),

    /**
     * 國庫及同業匯款只收基本郵電費
     */
    TreasuryAndRemittanceAcceptBasicexpenses(12407),

    /**
     * 已經匯出放行或取消之匯款資料不可再修正
     */
    RemitRemittanceDataNotRevise(12409),

    /**
     * 暫停FLAG已在暫停狀態,不能再設定暫停
     */
    PauseFlagPauseNotPause(12410),

    /**
     * 暫停FLAG已在啟動狀態,不能再設定啟動
     */
    PauseFlagStartNotStart(12411),

    /**
     * 已有分行結帳,不得進行此交易
     */
    RingItUpNotTrade(12412),

    /**
     * CBS主機營業中不能執行此交易
     */
    CBSInservice(12413),

    /**
     * RM AP not Checkin 不能執行此交易
     */
    RMOutOfService(12414),

    /**
     * 匯出優先權FLAG不符合
     */
    RemitPriorityFlagNotMatch(12415),

    /**
     * 通匯類系統正常不需再復原
     */
    RemitsSystemNotNeedRecover(12416),

    /**
     * 一般通訊系統正常不需再復原
     */
    CommunicationSysteNotRecover(12417),

    /**
     * 禁止跨行登錄
     */
    RemitStop(12418),

    /**
     * 匯款單位無此分行
     */
    SenderBankNotBranch(12419),

    /**
     * 此筆序號已下傳
     */
    OrdinalNumberDownload(12420),

    /**
     * 此筆批號重覆
     */
    BatchNumberRepeated(12421),

    /**
     * 發信單位該項跨行業務停止或暫停營業
     */
    SenderBankStopOrPauseBusiness(12422),

    /**
     * 原交易訊息類別錯誤或尚未傳送財金公司
     */
    TradePcodeErrorOrNotSendFISC(12423),

    /**
     * 匯款單位別與解款單位別相同
     */
    SenderBankAndReceiverBankthesame(12424),

    /**
     * 解款行方可執行本交易
     */
    TransferFundCanTrade(12425),

    /**
     * BRS登錄序號重覆
     */
    BRSRegisterNumberRepeated(12426),

    /**
     * 實收手續費大於應收手續費
     */
    HandleChargeNotOK(12427),

    /**
     * 尚有匯出中資料, 請先查詢財金交易處理狀況
     */
    RemitDataNotComplete(12428),

    /**
     * 本行匯出被退暫停此行庫之匯出
     */
    BankReturnRemit(12429),

    /**
     * 借貸不平
     */
    CrDrNotBalance(12450),

    /**
     * 非本日解款不能執行解款狀態變更
     */
    ProcessingDateNotToday(12451),

    /**
     * 資深櫃員不能確認或覆核原匯出登錄交易
     */
    TellerNotAudit(12452),

    /**
     * FEP執行確認取消
     */
    FEPCancel(12453),

    /**
     * 本行更換通匯基碼
     */
    MBankChangekey(12454),

    /**
     * 調整跨行電文序號
     */
    ChangeFISCSno(12455),

    /**
     * 調整跨行通匯序號
     */
    ChangeRMSno(12456),

    /**
     * 調整匯兌收送狀態
     */
    ChangeServiceStatus(12457),

    /**
     * 調整待匯出匯款順序
     */
    ChangePriority(12458),

    /**
     * 匯入補送中心
     */
    SendRMINTX(12459),

    /**
     * 啟動/暫停CBS匯出入狀態
     */
    ChangeCBSRMStatus(12460),

    /**
     * 匯款確認取消交易輸入
     */
    SendRMOUTCancel(12461),

    /**
     * 一般通訊確認取消交易
     */
    SendMSGOUTCancel(12462),

    /**
     * 大批匯款監控啟動
     */
    SendRMBTCHTele(12463),

    /**
     * 整批人工解款監控啟動
     */
    SendSUnisysTele(12464),

    /**
     * 大批匯款監控回饋檔啟動
     */
    SendRMBTCHBackTele(12465),

    /**
     * FEDI轉通匯回饋監控啟動
     */
    SendFEDIBackTele(12466),

    /**
     * 超過查詢限制筆數
     */
    ExceedMaxCount(12467),

    /**
     * 往來行庫已設定暫停匯出
     */
    AllbankStopService(12468),

    /**
     * 此筆已解款,請查明原因
     */
    RMINComplete(12469),

    /**
     * 修改匯出退匯資料
     */
    ModifyRMOUTStat(12470),

    /**
     * 待解筆數超過警示
     */
    WarningREMAIN_CNT(12471),

    /**
     * T24 有Pending筆數未處理
     */
    T24Pending(12472),

    /**
     * FCS(FEPR1300)有未上傳筆數未處理
     */
    FCSFEPPending(12473),

    /**
     * FCS(B2BR1300)有未上傳筆數未處理
     */
    FCSB2BPending(12474),

    /**
     * FCS(ELNR1300)有未上傳筆數未處理
     */
    FCSELNPending(12476),

    /**
     * 匯入交易時間超過警示值
     */
    Warning_CalcFISCTime(12475),

    /**
     * 原匯出行才可執行本交易
     */
    RemitJustcanTrade(12808),

    /**
     * AML傳送狀態設定
     */
    ChangeAMLRMStatus(12810),

    /**
     * 電文ResendAML人工作業
     */
    ResendAML(12811),

    /**
     * 電文BypassAML人工作業
     */
    BypassAML(12812),

    /**
     * 匯出金額大於跨行基金水位
     */
    ExceetCLRFundLevel(12813),

    /**
     * 跨行餘額低於跨行基金水位二
     */
    LowCLRFundLevel2(12814),

    /**
     * 匯出暫禁記號狀態設定
     */
    ChangeCLRRMStatus(12815),

    /**
     * AML異常通知
     */
    AMLErrorNotice(12816),

    /**
     * ＡＴＭ無此交易，現金存款功能未支援
     */
    ATMNotSupportDepositTX(13001),

    /**
     * 無此ATM代號
     */
    ATMNoIsNotExist(13002),

    /**
     * 日期錯誤，不更新磁軌且不留置卡片
     */
    TBSDYError(13003),

    /**
     * ATM MODE與地區檔的MODE是否不符
     */
    ATMModeError(13004),

    /**
     * 非MMA金融信用卡，無法執行交易
     */
    CBSModeIs5(13005),

    /**
     * 卡片已註銷，不更新磁軌且不留置卡片
     */
    CardCancelled(13101),

    /**
     * 失效卡片，不更新磁軌且留置卡片
     */
    CardLoseEfficacy(13102),

    /**
     * 卡片尚未生效
     */
    CardNotEffective(13103),

    /**
     * 卡片已掛失
     */
    CardLost(13104),

    /**
     * 檢核卡片Track2 BIN異常
     */
    Track2Error(13105),

    /**
     * 晶片卡備註欄位檢核有誤
     */
    ICMARKError(13111),

    /**
     * 晶片卡交易序號重複
     */
    ICSeqNoDuplicate(13112),

    /**
     * 晶片卡交易序號錯誤，回發卡單位處理
     */
    ICSeqNoError(13113),

    /**
     * 金融信用卡狀態不合，無法交易
     */
    ComboCardStatusNotMatch(13121),

    /**
     * 金融信用卡未啟用
     */
    ComboCardNotEffective(13122),

    /**
     * 尚未申請國際卡功能
     */
    PlusCirrusNotApply(13123),

    /**
     * 悠遊卡號錯誤
     */
    EZCardError(13124),

    /**
     * 問題帳戶，不更新磁軌且不留置卡片
     */
    ACTNOError(13201),

    /**
     * 轉出入帳號相同，不更新磁軌且不留置卡片
     */
    TranInACTNOSameAsTranOut(13202),

    /**
     * 轉入帳號狀況檢核錯誤
     */
    TranInACTNOError(13203),

    /**
     * 轉入帳戶已結清，不更新磁軌且不留置卡片
     */
    TranInACTNOClosed(13204),

    /**
     * 該帳戶不允許做ＡＴＭ存款或提領等交易
     */
    ACTNONotAllowCashTX(13205),

    /**
     * 該帳戶未申請轉帳約定
     */
    NotApplyTransferActno(13206),

    /**
     * 問題帳戶，不更新磁軌且不留置卡片
     */
    NotICCard(13207),

    /**
     * 問題帳戶，不更新磁軌且不留置卡片
     */
    ICCardNotFound(13208),

    /**
     * REATINCARD不合
     */
    RetainCardCountNotMatch(13301),

    /**
     * 裝鈔後存款次數及金額不合
     */
    DepositSettlementError(13302),

    /**
     * 裝鈔後轉帳次數及金額不合
     */
    TransferSettlementError(13303),

    /**
     * 裝鈔後提領張數不合
     */
    WithdrawSettlementError(13304),

    /**
     * 上營業日提領筆數不合
     */
    LBSDYWithdrawSettlementError(13305),

    /**
     * 本營業日提領筆數金額不合
     */
    TBSDYWithdrawSettlementError(13306),

    /**
     * 裝鈔後繳款次數及金額不合
     */
    PayTaxSettlementError(13307),

    /**
     * 鈔匣數字異常
     */
    ATMCashWithdrawError(13308),

    /**
     * 裝幣序號不合
     */
    CoinRWTSeqNoNotMatch(13309),

    /**
     * RWT 不合
     */
    RWTNotMatch(13311),

    /**
     * RWT 裝鈔序號不合
     */
    RWTSeqNoNotMatch(13312),

    /**
     * ADM 錢箱個數為 0
     */
    ADMCashboxCountError(13313),

    /**
     * ATM錢箱內容與中心帳務不合
     */
    ATMBOXSettlementError(13314),

    /**
     * 密碼錯誤次數已達上限
     */
    OverPasswordErrorCount(13401),

    /**
     * 查無匯率
     */
    NoRateTable(13402),

    /**
     * 轉出帳號錯誤，不更新磁軌且不留置卡片
     */
    TranOutACTNOError(13403),

    /**
     * 身份證號不存在
     */
    IDNotFound(13404),

    /**
     * 帳號不存在
     */
    ACTNONotFound(13405),

    /**
     * 收到訊息不符規格
     */
    MsgContentError(13406),

    /**
     * 輸入日期不得大於十二個月
     */
    DueDateError(13407),

    /**
     * 輸入日期不合理
     */
    InputDateError(13408),

    /**
     * 傳入幣別錯誤
     */
    InputCurrencyCodeError(13409),

    /**
     * ATM無此交易，IC卡讀寫設備未支援
     */
    ICEMVError(13410),

    /**
     * 客戶不同意收取Access Fee，交易拒絕
     */
    ACFeeReject(13411),

    /**
     * ATM交易序號重複
     */
    ATMSeqNoDuplicate(13501),

    /**
     * 新台幣仟元剩餘張數不合
     */
    CompareCountError(13601),

    /**
     * 企業戶無卡提款設定記號錯誤
     */
    ApplyTypeError(13602),

    /**
     * 身份證或統一編號與卡檔不符
     */
    IDError(13603),

    /**
     * 無此無卡提款序號
     */
    NWDSeqNotFound(13811),

    /**
     * 提款序號(已提款/已失效/已取消)
     */
    NWDSeqNotMatch(13812),

    /**
     * 類型錯誤
     */
    TypeError(13813),

    /**
     * 輸入資料有誤
     */
    NWDSeqError(13814),

    /**
     * 電文格式錯誤，欄位缺少
     */
    FieldError(13817),

    /**
     * 其他非預期錯誤
     */
    UnExpectError(13818),

    /**
     * 無卡提款功能已失效或狀態有誤
     */
    NCStatusNotMatch(13819),

    /**
     * 掌靜脈驗證有誤
     */
    PVVerifyError(13820),

    /**
     * 其他類檢核錯誤
     */
    OtherCheckError(13999),

    /**
     * G類查詢錯誤
     */
    GQueryError(14001),

    /**
     * 相關欄位檢查錯誤
     */
    CheckFieldError(14002),

    /**
     * 限原存單位辦理
     */
    OriBranchOnly(14003),

    /**
     * 應經主管許可
     */
    SPCDNeeded(14004),

    /**
     * 該卡片帳號已申請
     */
    AccountHasExist(14005),

    /**
     * 金融信用卡不可執行此項目
     */
    ComboCardInvalid(14006),

    /**
     * 金融卡各項申請／撤銷條件不符，交易失敗
     */
    G6600ApplyError(14007),

    /**
     * 金融卡卡片統計檔有問題，本交易失敗
     */
    TCARDError(14008),

    /**
     * 註銷數大於本日收到卡片數，請先查詢G0071
     */
    CancelCountOver(14009),

    /**
     * 金融卡庫存數不足，請先執行G7100
     */
    TCARDInsufficient(14010),

    /**
     * 當日申請或重新發卡之更正，請選８或９
     */
    SameDayApplyEC(14011),

    /**
     * 未啟用金融卡之註銷，請選項目１３；金融信用卡，請選項目１８
     */
    NonApplyCardCancel(14012),

    /**
     * 已啟用金融卡之註銷，請選項目；金融信用卡，請選項目１７
     */
    ApplyCardCancel(14013),

    /**
     * 企業識別卡員工資料檔已註銷
     */
    COIDHasExpired(14014),

    /**
     * 企業識別卡員工資料檔已存在
     */
    COIDHasExist(14015),

    /**
     * 卡片尚未註銷
     */
    AccountHasNotExpired(14016),

    /**
     * 不得重複申請COMBO卡
     */
    ReApplyComboCardNotAllow(14017),

    /**
     * 只允許客服人員執行此交易
     */
    OnlyCallCenterAllowed(14018),

    /**
     * 營業日期與系統日期不合，中斷查詢
     */
    BusinessDateAndSystemDateNotTheSame(14019),

    /**
     * 輸入代碼非ATM機台
     */
    NotATM(14020),

    /**
     * 輸入代碼非ADM機台
     */
    NotADM(14021),

    /**
     * 申請項目錯誤
     */
    WrongTXCD(14022),

    /**
     * 申請項目與整批申請項目不合
     */
    TXCDNotMatch(14023),

    /**
     * 上傳資料帳號重複
     */
    DuplicateACTNO(14024),

    /**
     * 上傳資料身分證號重複
     */
    DuplicateIDNO(14025),

    /**
     * 消費扣款記號錯誤
     */
    WrongICPU(14026),

    /**
     * 國際卡申請記號錯誤
     */
    WrongAPPGP(14027),

    /**
     * 限定主帳號交易記號錯誤
     */
    WrongSELF(14028),

    /**
     * 限制轉帳交易記號錯誤
     */
    WrongAPPTFR(14029),

    /**
     * NB網路服務類型記號錯誤
     */
    WrongNBTFR(14030),

    /**
     * 大陸人士不得申請國際卡交易
     */
    APPGPChina(14031),

    /**
     * 卡片申請檔已存在該帳號的申請資料
     */
    CardApplyExist(14032),

    /**
     * 此帳號已於分行申請製卡
     */
    CardNoExist(14033),

    /**
     * 帳號已銷戶
     */
    CancelACTNO(14034),

    /**
     * 帳號為久未往來戶
     */
    ACTNOInactive(14035),

    /**
     * MMA交割戶，不得申請金融卡
     */
    ACTNOIsMMA(14036),

    /**
     * 主機檢核錯誤
     */
    CBSCheckError(14037),

    /**
     * 上傳資料身分證號與主機不合
     */
    IDNONotMatch(14038),

    /**
     * 呼叫手機門號平台失敗
     */
    MobileAPIError(14039),
    
    /**
     * 呼叫FIDO平台失敗
     */
    FIDOAPIError(14040),

    /**
     * 呼叫UCD API失敗
     */
    UCDAPIError(14041),

    /**
     * 呼叫AccountInfo API失敗
     */
    AccountInfoAPIError(14042),

    /**
     * 呼叫encHelper發生異常
     */
    ENCLibError(15000),

    /**
     * 客戶亂碼基碼不同步
     */
    ENCCheckPPKeyError(15001),

    /**
     * 訊息押碼錯誤
     */
    ENCCheckMACError(15002),

    /**
     * 交易驗證碼檢核錯誤
     */
    ENCCheckTACError(15003),

    /**
     * 檢核密碼錯誤
     */
    ENCCheckPasswordError(15004),

    /**
     * ATM電文轉換 PIN BLOCK至信用卡電文失敗
     */
    ENCPINBlockConvertError(15005),

    /**
     * 呼叫ENCHelper所傳入的引數檢查失敗
     */
    ENCArgumentError(15006),

    /**
     * 檢核MAC或TAC錯誤
     */
    ENCCheckMACTACError(15007),

    /**
     * 變更密碼失敗
     */
    ENCChangePasswordError(15008),

    /**
     * 換 PP KEY失敗
     */
    ENCChangePPKeyError(15009),

    /**
     * 產生MAC失敗
     */
    ENCMakeMACError(15010),

    /**
     * 產生TerminalAuthen失敗
     */
    ENCMakeTerminalAuthenError(15011),

    /**
     * 更新PPKey失敗
     */
    ENCUpdatePPKeyError(15012),

    /**
     * 產生全繳API交易加密失敗
     */
    ENCEncrAPIError(15013),

    /**
     * 轉換全繳API交易加密失敗
     */
    ENCTrenAPIError(15014),

    /**
     * 產生Paytax繳稅交易加密失敗
     */
    ENCEncPTAXError(15019),

    /**
     * 產生Paytax繳稅交易解密失敗
     */
    ENCDecPTAXError(15020),

    /**
     * 系統錯誤
     */
    SystemError(19999),

    /**
     * ==================================== DB IO Error 列舉 ====================================
     */

    /**
     * 違反資料關聯
     */
    ForeignKeyViolation(547),

    /**
     * 違反索引關聯
     */
    UniqueIndexViolation(2601),

    /**
     * 主鍵值重覆
     */
    PrimaryKeyDuplicate(2627),

    /**
     * 資料庫異常
     */
    InvalidDatabase(4060),

    /**
     * 資料庫登入失敗
     */
    DBLoginFailed(18542),

    /**
     * 資料庫登入失敗
     */
    DBLoginFailed2(18546),

    /**
     * ALARM資料不存在
     */
    ALARMNotFound(18010),

    /**
     * ALLBANK資料不存在
     */
    ALLBANKNotFound(18020),

    /**
     * ALLBANK更新資料異常
     */
    ALLBANKUpdateError(18022),

    /**
     * APTOT資料不存在
     */
    APTOTNotFound(18030),

    /**
     * ATMBOX資料不存在
     */
    ATMBOXNotFound(18040),

    /**
     * ATMBOX新增資料異常
     */
    ATMBOXInsertError(18041),

    /**
     * ATMBOX更新資料異常
     */
    ATMBOXUpdateError(18042),

    /**
     * ATMBOXLOG資料不存在
     */
    ATMBOXLOGNotFound(18050),

    /**
     * ATMBOXLOG新增資料異常
     */
    ATMBOXLOGInsertError(18051),

    /**
     * ATMC資料不存在
     */
    ATMCNotFound(18060),

    /**
     * ATMC新增資料異常
     */
    ATMCInsertError(18061),

    /**
     * ATMC更新資料異常
     */
    ATMCUpdateError(18062),

    /**
     * ATMCASH資料不存在
     */
    ATMCASHNotFound(18070),

    /**
     * ATMCASH更新資料異常
     */
    ATMCASHUpdateError(18072),

    /**
     * ATMMSTR資料不存在
     */
    ATMMSTRNotFound(18080),

    /**
     * ATMSTAT資料不存在
     */
    ATMSTATNotFound(18090),

    /**
     * ATMSTAT更新資料異常
     */
    ATMSTATUpdateError(18092),

    /**
     * ATMSVCLOG資料不存在
     */
    ATMSVCLOGNotFound(18100),

    /**
     * BCTL資料不存在
     */
    BCTLNotFound(18110),

    /**
     * BIN資料不存在
     */
    BINNotFound(18120),

    /**
     * BITMAPDEF資料不存在
     */
    BITMAPDEFNotFound(18130),

    /**
     * BSDAYS資料不存在
     */
    BSDAYSNotFound(18140),

    /**
     * BUSI資料不存在
     */
    BUSINotFound(18150),

    /**
     * CARD資料不存在
     */
    CARDNotFound(18160),

    /**
     * CARD更新資料異常
     */
    CARDUpdateError(18162),

    /**
     * CARDMOD資料不存在
     */
    CARDMODNotFound(18170),

    /**
     * CBSPEND資料不存在
     */
    CBSPENDNotFound(18180),

    /**
     * CBSPEND新增資料異常
     */
    CBSPENDInsertError(18181),

    /**
     * CHANNEL資料不存在
     */
    CHANNELNotFound(18190),

    /**
     * COID資料不存在
     */
    COIDNotFound(18200),

    /**
     * CURCD資料不存在
     */
    CURCDNotFound(18210),

    /**
     * DATAATTR資料不存在
     */
    DATAATTRNotFound(18220),

    /**
     * EF1001資料不存在
     */
    EF1001NotFound(18230),

    /**
     * EF1002資料不存在
     */
    EF1002NotFound(18240),

    /**
     * EJFNO資料不存在
     */
    EJFNONotFound(18250),

    /**
     * EJFNO更新資料異常
     */
    EJFNOUpdateError(18252),

    /**
     * ELOG資料不存在
     */
    ELOGNotFound(18260),

    /**
     * EVENT資料不存在
     */
    EVENTNotFound(18270),

    /**
     * EVENTLOG資料不存在
     */
    EVENTLOGNotFound(18280),

    /**
     * EXRATE資料不存在
     */
    EXRATENotFound(18290),

    /**
     * FCALLBANK資料不存在
     */
    FCALLBANKNotFound(18300),

    /**
     * FCALLBANK更新資料異常
     */
    FCALLBANKUpdateError(18301),

    /**
     * FCAPTOT資料不存在
     */
    FCAPTOTNotFound(18310),

    /**
     * FCAPTOT更新資料異常
     */
    FCAPTOTUpdateError(18311),

    /**
     * FCMSGIN資料不存在
     */
    FCMSGINNotFound(18320),

    /**
     * FCMSGIN新增資料異常
     */
    FCMSGINInsertError(18321),

    /**
     * FCMSGIN更新資料異常
     */
    FCMSGINUpdateError(18322),

    /**
     * FCMSGOUT資料不存在
     */
    FCMSGOUTNotFound(18330),

    /**
     * FCMSGOUT新增資料異常
     */
    FCMSGOUTInsertError(18331),

    /**
     * FCMSGOUT更新資料異常
     */
    FCMSGOUTUpdateError(18332),

    /**
     * FCRMIN資料不存在
     */
    FCRMINNotFound(18340),

    /**
     * FCRMIN新增資料異常
     */
    FCRMINInsertError(18341),

    /**
     * FCRMIN更新資料異常
     */
    FCRMINUpdateError(18342),

    /**
     * FCRMINSNO資料不存在
     */
    FCRMINSNONotFound(18350),

    /**
     * FCRMINSNO更新資料異常
     */
    FCRMINSNOUpdateError(18352),

    /**
     * FCRMNOCTL資料不存在
     */
    FCRMNOCTLNotFound(18360),

    /**
     * FCRMNOCTLUPDATEFERROR
     */
    FCRMNOCTLUPDATEFERROR(18362),

    /**
     * FCRMOUT資料不存在
     */
    FCRMOUTNotFound(18370),

    /**
     * FCRMOUT新增資料異常
     */
    FCRMOUTInsertError(18371),

    /**
     * FCRMOUT更新資料異常
     */
    FCRMOUTUpdateError(18372),

    /**
     * FCRMOUTSNO資料不存在
     */
    FCRMOUTSNONotFound(18380),

    /**
     * FCRMOUTSNO更新資料異常
     */
    FCRMOUTSNOUpdateError(18382),

    /**
     * FCRMPEND資料不存在
     */
    FCRMPENDNotFound(18390),

    /**
     * FCRMPEND新增資料異常
     */
    FCRMPENDInsertError(18391),

    /**
     * FCRMPOST資料不存在
     */
    FCRMPOSTNotFound(18400),

    /**
     * FCRMSFP資料不存在
     */
    FCRMSFPNotFound(18410),

    /**
     * FCRMSTAT資料不存在
     */
    FCRMSTATNotFound(18420),

    /**
     * FCRMSTAT更新資料異常
     */
    FCRMSTATUpdateError(18422),

    /**
     * FCRMTOT資料不存在
     */
    FCRMTOTNotFound(18430),

    /**
     * FCRMTOT新增資料異常
     */
    FCRMTOTInsertError(18431),

    /**
     * FCRMTOT更新資料異常
     */
    FCRMTOTUpdateError(18432),

    /**
     * FCRMTOTAL資料不存在
     */
    FCRMTOTALNotFound(18440),

    /**
     * FCRMTOTAL新增資料異常
     */
    FCRMTOTALInsertError(18441),

    /**
     * FCRMTOTAL更新資料異常
     */
    FCRMTOTALUpdateError(18442),

    /**
     * FEPTXN資料不存在
     */
    FEPTXNNotFound(18450),

    /**
     * FEPTXN新增資料異常
     */
    FEPTXNInsertError(18451),

    /**
     * FEPTXN更新資料異常
     */
    FEPTXNUpdateError(18452),

    /**
     * FEPTXN讀取資料異常
     */
    FEPTXNReadError(18454),

    /**
     * FEPTXN更新資料不存在
     */
    FEPTXNUpdateNotFound(18455),

    /**
     * FEPTXN刪除資料不存在
     */
    FEPTXNDeleteNotFound(18456),

    /**
     * FEPUSER資料不存在
     */
    FEPUSERNotFound(18460),

    /**
     * GUARD資料不存在
     */
    GUARDNotFound(18470),

    /**
     * IBTID資料不存在
     */
    IBTIDNotFound(18480),

    /**
     * INBKPARM資料不存在
     */
    INBKPARMNotFound(18490),

    /**
     * INBKPEND資料不存在
     */
    INBKPENDNotFound(18500),

    /**
     * INBKPEND新增資料異常
     */
    INBKPENDInsertError(18501),

    /**
     * INBKRCFMT資料不存在
     */
    INBKRCFMTNotFound(18510),

    /**
     * INTLTXN資料不存在
     */
    INTLTXNNotFound(18520),

    /**
     * INTR資料不存在
     */
    INTRNotFound(18530),

    /**
     * MERCHANT資料不存在
     */
    MERCHANTNotFound(18540),

    /**
     * MODEL資料不存在
     */
    MODELNotFound(18550),

    /**
     * MODULE資料不存在
     */
    MODULENotFound(18560),

    /**
     * MSGCTL資料不存在
     */
    MSGCTLNotFound(18570),

    /**
     * MSGFILE資料不存在
     */
    MSGFILENotFound(18580),

    /**
     * MSGIN資料不存在
     */
    MSGINNotFound(18590),

    /**
     * MSGIN新增資料異常
     */
    MSGINInsertError(18591),

    /**
     * MSGIN更新資料異常
     */
    MSGINUpdateError(18592),

    /**
     * MSGLOG資料不存在
     */
    MSGLOGNotFound(18600),

    /**
     * MSGOUT資料不存在
     */
    MSGOUTNotFound(18610),

    /**
     * MSGOUTINSERTTERROR
     */
    MSGOUTINSERTTERROR(18611),

    /**
     * MSGOUT更新資料異常
     */
    MSGOUTUpdateError(18612),

    /**
     * NPSUNIT資料不存在
     */
    NPSUNITNotFound(18620),

    /**
     * PROGRAM資料不存在
     */
    PROGRAMNotFound(18630),

    /**
     * PRORT資料不存在
     */
    PRORTNotFound(18640),

    /**
     * RETAIN資料不存在
     */
    RETAINNotFound(18650),

    /**
     * RETAIN新增資料異常
     */
    RETAINInsertError(18651),

    /**
     * RMBTCH資料不存在
     */
    RMBTCHNotFound(18660),

    /**
     * RMBTCH新增資料異常
     */
    RMBTCHInsertError(18661),

    /**
     * RMBTCH更新資料異常
     */
    RMBTCHUpdateError(18662),

    /**
     * RMBTCHMTR資料不存在
     */
    RMBTCHMTRNotFound(18666),

    /**
     * RMBTCHMTR新增資料異常
     */
    RMBTCHMTRInsertError(18667),

    /**
     * RMBTCHMTR更新資料異常
     */
    RMBTCHMTRUpdateError(18668),

    /**
     * RMCUST資料不存在
     */
    RMCUSTNotFound(18670),

    /**
     * RMCUST新增資料異常
     */
    RMCUSTInsertError(18671),

    /**
     * RMCUST更新資料異常
     */
    RMCUSTUpdateError(18672),

    /**
     * RMCUST刪除資料異常
     */
    RMCUSTDeleteError(18673),

    /**
     * RMFISCIN1資料不存在
     */
    RMFISCIN1NotFound(18680),

    /**
     * RMFISCIN1更新資料異常
     */
    RMFISCIN1UpdateError(18682),

    /**
     * RMFISCIN4資料不存在
     */
    RMFISCIN4NotFound(18690),

    /**
     * RMFISCIN4更新資料異常
     */
    RMFISCIN4UpdateError(18692),

    /**
     * RMFISCOUT1資料不存在
     */
    RMFISCOUT1NotFound(18700),

    /**
     * RMFISCOUT1更新資料異常
     */
    RMFISCOUT1UpdateError(18702),

    /**
     * RMFISCOUT4資料不存在
     */
    RMFISCOUT4NotFound(18710),

    /**
     * RMFISCOUT4更新資料異常
     */
    RMFISCOUT4UpdateError(18712),

    /**
     * RMIN資料不存在
     */
    RMINNotFound(18720),

    /**
     * RMIN新增資料異常
     */
    RMINInsertError(18721),

    /**
     * RMIN更新資料異常
     */
    RMINUpdateError(18722),

    /**
     * RMINSNO資料不存在
     */
    RMINSNONotFound(18730),

    /**
     * RMINSNO更新資料異常
     */
    RMINSNOUpdateError(18732),

    /**
     * RMNOCTL資料不存在
     */
    RMNOCTLNotFound(18740),

    /**
     * RMNOCTL更新資料異常
     */
    RMNOCTLUpdateError(18742),

    /**
     * RMOUT資料不存在
     */
    RMOUTNotFound(18750),

    /**
     * RMOUT新增資料異常
     */
    RMOUTInsertError(18751),

    /**
     * RMOUT更新資料異常
     */
    RMOUTUpdateError(18752),

    /**
     * RMOUTSNO資料不存在
     */
    RMOUTSNONotFound(18760),

    /**
     * RMOUTSNOUPDATEOERROR
     */
    RMOUTSNOUPDATEOERROR(18762),

    /**
     * RMPEND資料不存在
     */
    RMPENDNotFound(18770),

    /**
     * RMPEND新增資料異常
     */
    RMPENDInsertError(18771),

    /**
     * RMPOST資料不存在
     */
    RMPOSTNotFound(18780),

    /**
     * RMSFP資料不存在
     */
    RMSFPNotFound(18790),

    /**
     * RMSTAT資料不存在
     */
    RMSTATNotFound(18800),

    /**
     * RMSTAT更新資料異常
     */
    RMSTATUpdateError(18802),

    /**
     * RMTOT資料不存在
     */
    RMTOTNotFound(18810),

    /**
     * RMTOT新增資料異常
     */
    RMTOTInsertError(18811),

    /**
     * RMTOT更新資料異常
     */
    RMTOTUpdateError(18812),

    /**
     * RMTOTAL資料不存在
     */
    RMTOTALNotFound(18820),

    /**
     * RMTOTAL新增資料異常
     */
    RMTOTALInsertError(18821),

    /**
     * RMTOTAL更新資料異常
     */
    RMTOTALUpdateError(18822),

    /**
     * RVSNO資料不存在
     */
    RVSNONotFound(18830),

    /**
     * SCTL資料不存在
     */
    SCTLNotFound(18840),

    /**
     * SEQNO資料不存在
     */
    SEQNONotFound(18850),

    /**
     * STAN資料不存在
     */
    STANNotFound(18860),

    /**
     * SUBSYS資料不存在
     */
    SUBSYSNotFound(18870),

    /**
     * SYSCONF資料不存在
     */
    SYSCONFNotFound(18880),

    /**
     * SYSDIAGRAMS資料不存在
     */
    SYSDIAGRAMSNotFound(18890),

    /**
     * SYSSTAT資料不存在
     */
    SYSSTATNotFound(18900),

    /**
     * SYSSTAT更新資料異常
     */
    SYSSTATUpdateError(18902),

    /**
     * TCARD資料不存在
     */
    TCARDNotFound(18910),

    /**
     * VENDOR資料不存在
     */
    VENDORNotFound(18920),

    /**
     * VIRACT資料不存在
     */
    VIRACTNotFound(18930),

    /**
     * ZONE資料不存在
     */
    ZONENotFound(18940),

    /**
     * ZONE更新資料異常
     */
    ZONEUpdateError(18942),

    /**
     * 查詢失敗
     */
    QueryFail(19001),

    /**
     * 查無資料
     */
    QueryNoData(19002),

    /**
     * 新增成功
     */
    InsertSuccess(19003),

    /**
     * 新增失敗
     */
    InsertFail(19004),

    /**
     * 更新成功
     */
    UpdateSuccess(19005),

    /**
     * 更新失敗
     */
    UpdateFail(19006),

    /**
     * 刪除成功
     */
    DeleteSuccess(19007),

    /**
     * 刪除失敗
     */
    DeleteFail(19008),

    /**
     * 檔案已存在
     */
    FileExist(19009),

    /**
     * 檔案不存在
     */
    FileNotExist(19010),

    VACATENONotFound(19011),

    INBK2160InsertError(19012),

    INVALID_CERTIFICATE(19013),

    NOT_SSL_RECORD(19014),

    INVALID_CERTIFICATE_ALIAS(19015),

    CERTIFICATE_NOT_MATCH(19016),

    CERTIFICATE_EXPIRED(19017),

    FISCCloseBalanceSettle(19018),

    SSL_HANDSHAKE_NOT_COMPLETION(19019),

    CUSTOM(0);

    private int value;
    private String stringValue;

    private FEPReturnCode(int value) {
        this.value = value;
    }
    
    private FEPReturnCode(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getValue() {
        return value;
    }

    public static FEPReturnCode fromValue(int value) {
        for (FEPReturnCode e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static FEPReturnCode parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (FEPReturnCode e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }

    public static String toString(String code) {
        try {
            return toString(parse(code));
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static String toString(FEPReturnCode rtnCode) {
        if (rtnCode == null) {
            return "null";
        }
        else if (rtnCode.stringValue != null) {
            return rtnCode.stringValue;
        }
        else{
            return StringUtils.join(rtnCode.getValue());
        }
    }

    /**
     * Create a custom return code with the given string value
     * @param value The custom string value
     * @return A FEPReturnCode instance with the custom value
     */
    public static FEPReturnCode custom(String value) {
        FEPReturnCode customCode = FEPReturnCode.CUSTOM;
        customCode.stringValue = value;
        return customCode;
    }
}