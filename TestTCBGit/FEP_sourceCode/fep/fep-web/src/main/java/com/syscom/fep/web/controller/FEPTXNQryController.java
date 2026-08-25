package com.syscom.fep.web.controller;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnMsgRqExt;
import com.syscom.fep.mybatis.ext.model.FeptxnMsgRsExt;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.service.MsgctlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
public class FEPTXNQryController extends BaseController {
    @Autowired
    private AtmService atmService;
    @Autowired
    private InbkService inbkService;
    @Autowired
    private MsgctlService msgctlService;

    private final LogHelper log = new LogHelper();
    private final String[] keys = WebConfiguration.getInstance().getApikey().split(",");

    @RequestMapping(value = "/WebUtils/FEPTXNQry", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String feptxnQry (@RequestBody FeptxnMsgRqExt request) throws Exception {
        String bankNo = request.getI_BankNo();
        String stanNo = request.getI_StanNo();
        String systemID = request.getI_SystemID();
        String secretKey = request.getI_SecretKey();
        String txDate = request.getI_TxDate(); //民國

        // 1. 驗證輸入
        try {
            validateSecretKey(secretKey);
            validateSystemID(systemID);
        } catch (ValidationException e) {
            return createErrorResponseList(bankNo, stanNo, txDate, e.getErrorCode(), e.getMessage());
        }

        // 2. 獲取資料
        try {
            return getFeptxnData(bankNo, stanNo, txDate);
        } catch (DataAccessException e) {
            return createErrorResponseList(bankNo, stanNo, txDate, e.getErrorCode(), "Data access error: " + e.getMessage());
        } catch (Exception e) {
            return createErrorResponseList(bankNo, stanNo, txDate, "F", "Unexpected error: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/WebUtils/Transaction", method = RequestMethod.GET)
    @ResponseBody
    public String Transaction (
            @RequestParam(value = "I_BankNo") String bankNo, //銀行代號(發動行)
            @RequestParam(value = "I_StanNo") String stanNo, //交易序號
            @RequestParam(value = "I_SystemID") String systemID, //系統代號
            @RequestParam(value = "I_SecretKey") String secretKey, //金鑰
            @RequestParam(value = "I_TxDate") String txDate) //交易日期
    {
        // 1. 驗證輸入
        try {
            validateSecretKey(secretKey);
            validateSystemID(systemID);
        } catch (ValidationException e) {
            return createErrorResponseList(bankNo, stanNo, txDate, e.getErrorCode(), e.getMessage());
        }

        // 2. 獲取資料
        try {
            return getFeptxnData(bankNo, stanNo, txDate);
        } catch (DataAccessException e) {
            return createErrorResponseList(bankNo, stanNo, txDate, e.getErrorCode(), "Data access error: " + e.getMessage());
        } catch (Exception e) {
            return createErrorResponseList(bankNo, stanNo, txDate, "F", "Unexpected error: " + e.getMessage());
        }
    }

    private void validateSystemID(String systemID) throws ValidationException {
        if (!"ntabs".equalsIgnoreCase(systemID)) {
            throw new ValidationException("V", "Invalid SystemID: " + systemID);
        }
    }

    private void validateSecretKey(String secretKey) throws ValidationException {
        if (Arrays.stream(keys).noneMatch(key -> key.equals(secretKey))) {
            throw new ValidationException("V", "Invalid SecretKey");
        }
    }

    private String getFeptxnData(String bankNo, String stanNo, String txDate) throws Exception {
        StringBuilder result = new StringBuilder();
        LocalDate limitDate = LocalDate.now().minusDays(30);
        String adDateStr = CalendarUtil.rocStringToADString(txDate);

        LocalDate w_txDate;
        try {
            w_txDate = LocalDate.parse(adDateStr.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            throw new DataAccessException("F", "Invalid Date Value");
        }
        // 線上保留30天交易資料
        if (w_txDate.isBefore(limitDate)) {
            log.warn("交易日期已超過保留期限(30天), Input Date: " + txDate);
            throw new DataAccessException("F", "Query date exceeds 30-day retention period");
        }

        try {
            log.info("Input: bankNo: " + bankNo + ", stanNo: " + stanNo + ", FeptxnTxDate: " + adDateStr );
            List<FeptxnMsgRsExt> list = inbkService.getAPIFeptxnByStan(adDateStr, bankNo, stanNo);

            if (list == null || list.isEmpty()) {
                throw new DataAccessException("F", "No Data found");
            }

            for (FeptxnMsgRsExt model : list) {
                DataConditional(model);
                result.append(model.toString());    // 將所有null值設為空字串
                if (!model.equals(list.get(list.size()-1))) {
                    result.append("},\n{\n");
                }
            }

            log.info("API 查詢成功, 找到 " + list.size() + "筆資料");
            log.info("Output: {" + result.toString() + "}");
            return "{\n" + result.toString() + "}";
        } catch (Exception e) {
            log.error("API 查詢失敗, Error: " + e.getMessage());
            throw e;
        }
    }

    private void DataConditional(FeptxnMsgRsExt model) {
        Msgfile msgfile;

        // 查詢結果
        model.setQryrc("A");

        String pcode = (StringUtils.isNotBlank(model.getFscode())
                && model.getFscode().length() >= 2
                && model.getFscode().startsWith("I5"))
                ? "2500" : model.getPcode();
        String p3 = null;
        String p4 = null;
        if (StringUtils.isNotBlank(pcode)) {
            if (pcode.length() >= 3) p3 = pcode.substring(0, 3);
            if (pcode.length() >= 4) p4 = pcode.substring(0, 4);
        }

        model.setHceCardNo(model.getTroutACCNo());
        // 留置卡片
        String Pbmcrd = model.getPbmcrd();
        if ("2".equals(Pbmcrd)) {
            model.setPbmcrd("留置卡片");
        }else if ("4".equals(Pbmcrd)) {
            model.setPbmcrd("不留置卡片");
        }else if ("0".equals(Pbmcrd)) {
            model.setPbmcrd("無");
        }else{
            model.setPbmcrd(Pbmcrd);
        }

        // 記帳狀態
        if (!"2290".equals(p4) && ("Y".equals(model.getAcctStat()) || "1".equals(model.getAccType()))) {
            model.setAcctStat("已記帳");
        }else{
            model.setAcctStat("未記帳");
        }

        // 沖帳狀態
        if (!"2290".equals(p4) && ("Y".equals(model.getRvsStat()) || "2".equals(model.getAccType()) ||
                (("2430".equals(p4) || "2470".equals(p4)) && "A".equals(model.getTxnRust())))) {
            model.setRvsStat("已沖正");
        }else{
            model.setRvsStat("");
        }

        // 交易逾時
        if ("1".equals(model.getCbsTimeout())) {
            model.setCbsTimeout("主機交易逾時");
        }else{
            model.setCbsTimeout("");
        }

        if ("1".equals(model.getFiscTimeout())) {
            model.setFiscTimeout("財金交易逾時");
        }else{
            model.setFiscTimeout("");
        }

        // 交易狀態
        if ("A".equals(model.getTxnRust()) || ("1".equals(model.getAccType()) && "2".equals(model.getPending()))) {
            model.setTxnRust("交易完成");
        } else if ("B".equals(model.getTxnRust()) && "1".equals(model.getPending())) {
            model.setTxnRust("交易Pending");
        } else if ("C".equals(model.getTxnRust()) || "D".equals(model.getTxnRust()) || ("2".equals(model.getAccType()) && "2".equals(model.getPending()))) {
            model.setTxnRust("交易已沖正");
        } else if ("10203".equals(model.getAarc())) {
            model.setTxnRust("主機逾時");
        } else {
            model.setTxnRust("交易失敗");
        }

        // 交易類別
        if (StringUtils.isNotBlank(model.getTxnType())) {
            String msgid = model.getTxnType().trim();
            String txnType = msgctlService.selectMsgName(msgid);
            if (StringUtils.isNotBlank(txnType)) {
                txnType = txnType.substring(0, Math.min(txnType.length(), 10)); //超過10位取前10位中文
                model.setTxnType(txnType);
            }
            log.info("交易類別Before:" + msgid + ",交易類別After:" + txnType);
        }

        // 自行三碼回應碼
        if (StringUtils.isNotBlank(model.getCbsrc3())) {
            msgfile = atmService.chkExistInMsgfile(10, model.getCbsrc3().trim());
            if (msgfile != null) {
                model.setCbsrc3(model.getCbsrc3() + " " + msgfile.getMsgfileShortmsg());
            }
        }
        // 自行四碼回應碼
        if (StringUtils.isNotBlank(model.getCbsrc4())) {
            msgfile = atmService.chkExistInMsgfile(2, model.getCbsrc4().trim());
            if (msgfile != null) {
                model.setCbsrc4(model.getCbsrc4() + " " + msgfile.getMsgfileShortmsg());
            }
        }

        String msgfileShortMsg = StringUtils.EMPTY;
        log.info("Channel:" + model.getChannel());
        // 轉入銀行
        if (Objects.equals(model.getChannel(), "FID")) {
            model.setTrinBKNo("");
        }

        // 卡片種類
        if ("226".equals(p3)){
            model.setCardFMT("ID+ACT");
        } else if (Objects.equals(model.getChannel(), "FISC")) {
            if ("6071".equals(model.getAtmType())) {
                model.setCardFMT("無卡");
            } else {
                model.setCardFMT("晶片卡");
            }
        } else {
            String cardFmt = model.getCardFMT();
            if (cardFmt == null) {
                model.setCardFMT("");
            } else {
                switch (cardFmt) {
                    case "Q":
                    case "X":
                        model.setCardFMT("無卡");
                        break;
                    case "E":
                        model.setCardFMT("EMV");
                        break;
                    case "T":
                        model.setCardFMT("磁條");
                        break;
                    case "K":
                    case "9":
                        model.setCardFMT("晶片");
                        break;
                    default:
                        break;
                }
            }
        }

        // FEP回應端末代碼
        if (Objects.equals(model.getChannel(), "FISC")) {
            if (StringUtils.isNotBlank(model.getFeprc())) {
                msgfile = atmService.chkExistInMsgfile(2, model.getFeprc().trim());
                if (msgfile != null) {
                    msgfileShortMsg = msgfile.getMsgfileShortmsg();
                }
            }
        } else {
            if (StringUtils.isNotBlank(model.getFeprc())) {
                msgfileShortMsg = atmService.selectShortMsgByATM(7, model.getFeprc().trim());
            }
        }
        if (StringUtils.isBlank(msgfileShortMsg)) {
            msgfileShortMsg = model.getTxnRust();
        }
        model.setFeprc(model.getFeprc() == null ? msgfileShortMsg : model.getFeprc() + " " + msgfileShortMsg);
        log.info("FEPRC:" + model.getFeprc());

        // 交易分類
        model.setTcbFlow(model.getTcbFlow() + "B");

        // 交易類別(依本行分類)
        pcode = model.getPcode() != null ? model.getPcode().trim() : "";
        String txCode = model.getFscode() != null ? model.getFscode().trim() : "";
        String channel = model.getChannel() != null ? model.getChannel().trim() : "";
        if ("2566".equals(pcode) || Arrays.asList("DX", "AW", "NP").contains(txCode)) {
            model.setTcbtxnType("CL");
        } else if (Arrays.asList("FP", "FA", "FD", "FS", "FX").contains(txCode)) {
            model.setTcbtxnType("FV");
        } else if (txCode.startsWith("P1") || txCode.startsWith("P4")) { // LEFT(FEPTXN_TX_CODE,2)
            model.setTcbtxnType("PG");
        } else if ((pcode.startsWith("24") || pcode.startsWith("26")) && !pcode.endsWith("1")) { // RIGHT(FEPTXN_PCODE,1) <> 1
            model.setTcbtxnType("W5");
        } else if ("FISC".equals(channel)) {
            if (pcode.startsWith("250")) {
                model.setTcbtxnType("IQ");
            } else if (pcode.startsWith("251") || pcode.startsWith("257")) {
                model.setTcbtxnType("WD");
            } else if (pcode.startsWith("24")) {
                model.setTcbtxnType("W5");
            } else if ("2566".equals(pcode)) {
                model.setTcbtxnType("CL");
            } else if ("2549".equals(pcode)) {
                model.setTcbtxnType("CV");
            } else if (pcode.startsWith("254")) {
                model.setTcbtxnType("UH");
            } else if (pcode.startsWith("252") || pcode.startsWith("253") ||
                    pcode.startsWith("255") || pcode.startsWith("256") ||
                    pcode.startsWith("226")) {
                model.setTcbtxnType("TR");
            }
        } else {
            if ((pcode.startsWith("26") && pcode.endsWith("1")) || txCode.startsWith("I")) {
                model.setTcbtxnType("IQ");
            } else if (txCode.startsWith("D")) {
                model.setTcbtxnType("DP");
            } else if (txCode.startsWith("T") || txCode.startsWith("E")) {
                model.setTcbtxnType("TR");
            } else if ("2510".equals(pcode)) {
                model.setTcbtxnType("WD");
            } else if ("2700".equals(pcode)) {
                model.setTcbtxnType("BR");
            } else {
                model.setTcbtxnType(txCode); // Trim(FEPTXN_TX_CODE)
            }
        }

        // 銷帳編號
        if (StringUtils.isNotBlank(model.getPayType()) && model.getPayType().length() >= 2) {
            if ("15".equals(model.getPayType().substring(0, 2))) {
                model.setReconSeqNo(model.getIdNO());
            }
        }
    }

    private String createErrorResponseList(String bankNo, String stanNo, String txDate, String errorCode, String logMessage) {
        FeptxnMsgRsExt errorModel = new FeptxnMsgRsExt();
        errorModel.setBankNo(bankNo);
        errorModel.setStanNo(bankNo + stanNo);
        errorModel.setTxnDate(txDate);
        errorModel.setQryrc(errorCode);
        log.error(logMessage);
//        return "{\n  \"message\": \"" + logMessage + "\"\n},\n" + errorModel.toString();
        return "{\n" + errorModel.toString() + "}";
    }

    // 自定義例外類別，用於封裝驗證錯誤
    private static class ValidationException extends Exception {
        private final String errorCode;

        public ValidationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    // 自定義例外類別，用於封裝資料存取錯誤
    private static class DataAccessException extends Exception {
        private final String errorCode;

        public DataAccessException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
