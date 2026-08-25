package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Rmin;
import com.syscom.fep.mybatis.model.Rmout;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028260_Form;
import com.syscom.fep.web.service.MemberShipService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;

/**
 * 修改匯出退匯資料
 *
 * @author xingyun_yang
 * @create 2021/11/30
 */
@Controller
public class UI_028260Controller extends BaseController {
    @Autowired
    RmService rmService;
    @Autowired
    MemberShipService memberShipService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_028260_Form form = new UI_028260_Form();
        form.setDtTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        form.setDtOrgDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028260/index")
    public String onload(@ModelAttribute UI_028260_Form form, ModelMap mode) {
        this.doKeepFormData(mode, form);
        pageOnLoad(mode);
        return Router.UI_028260.getView();
    }

    /**
     * 查詢按鈕
     */
    @PostMapping(value = "/rm/UI_028260/queryClick")
    public String queryClick(@ModelAttribute UI_028260_Form form, ModelMap mode) {
        this.infoMessage("UI_028260查詢, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);
        Rmin defRmin = new Rmin();
        Rmout defRmout = new Rmout();
        BigDecimal decTxAmt = new BigDecimal(0);
        String rmin_brno = "900";
        try {
            //Fly 2014/12/03 For個資LOG紀錄
            // 記錄AuditLog
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            decTxAmt = new BigDecimal(form.getTbTxAmt());
            auditLog.addParam("匯款金額", decTxAmt.toString());
            defRmin.setRminBrno(rmin_brno);
            if (!"".equals(form.getDtTxDate())) {
                defRmin.setRminTxdate(form.getDtTxDate().replace("-", ""));
                auditLog.addParam("匯入日期", form.getDtTxDate());
            }
            if (!"".equals(form.getTbFepNo())) {
                form.setTbFepNo(StringUtils.leftPad(form.getTbFepNo(), 7, '0'));
                defRmin.setRminFepno(form.getTbFepNo());
                auditLog.addParam("匯入登錄序號", form.getTbFepNo());
            }
            if (!"".equals(form.getDtOrgDate())) {
                defRmout.setRmoutTxdate(form.getDtOrgDate().replace("-", ""));
                auditLog.addParam("原匯款日期", form.getDtOrgDate());
            }
            if (!"".equals(form.getTbBrno())) {
                defRmout.setRmoutBrno(form.getTbBrno());
                auditLog.addParam("原登錄分行", form.getTbBrno());
            }
            if (!"".equals(form.getDdlOriginal())) {
                defRmout.setRmoutOriginal(form.getDdlOriginal().substring(0, 1));
                auditLog.addParam("原交易來源", form.getDdlOriginal());
            }
            if (!"".equals(form.getTbOrgFepNo())) {
                form.setTbOrgFepNo(StringUtils.leftPad(form.getTbOrgFepNo(), 7, '0'));
                defRmout.setRmoutFepno(form.getTbOrgFepNo());
                auditLog.addParam("原登錄序號", form.getTbOrgFepNo());
            }
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
            //Modified By ChenLi on 2013/10/1 for 改用GetRMINbyPK查詢資料
            defRmin = rmService.getRMINbyPK(defRmin);
            if (defRmin == null) {
                this.showMessage(mode, MessageType.WARNING, "匯入主檔查無資料");
                return Router.UI_028260.getView();
            } else {
                if (!"02".equals(defRmin.getRminStat())) {
                    this.showMessage(mode, MessageType.WARNING, "此筆匯入狀態不是為02-待解款");
                    return Router.UI_028260.getView();
                } else if (!"1172".equals(defRmin.getRminFiscSndCode())) {
                    this.showMessage(mode, MessageType.WARNING, "此筆資料不是為1172-退還匯款資料");
                    return Router.UI_028260.getView();
                } else if (!decTxAmt.equals(defRmin.getRminTxamt())) {
                    this.showMessage(mode, MessageType.WARNING, "輸入的匯款金額和匯入主檔的不相符");
                    return Router.UI_028260.getView();
                } else {
                    //Modified By ChenLi on 2013/10/1 for 改用GetRMOUTbyPK查詢資料
                    defRmout = rmService.getRMOUTbyPK(defRmout);
                    if (defRmout == null) {
                        this.showMessage(mode, MessageType.WARNING, "查無原匯出資料");
                        return Router.UI_028260.getView();
                    } else if (!"06".equals(defRmout.getRmoutStat())) {
                        this.showMessage(mode, MessageType.WARNING, "此筆匯出狀態不是為06-已解款");
                        return Router.UI_028260.getView();
                    } else if (!decTxAmt.equals(defRmout.getRmoutTxamt())) {
                        this.showMessage(mode, MessageType.WARNING, "輸入的匯款金額和匯出主檔的不相符");
                        return Router.UI_028260.getView();
                    } else {
                        Rmout defRmout_Tmp = new Rmout();
                        defRmout_Tmp.setRmoutTxdate(defRmout.getRmoutTxdate());
                        defRmout_Tmp.setRmoutBrno(defRmout.getRmoutBrno());
                        defRmout_Tmp.setRmoutOriginal(defRmout.getRmoutOriginal());
                        defRmout_Tmp.setRmoutFepno(defRmout.getRmoutFepno());
                        List<Rmout> dtOut = rmService.getRmoutByDef(defRmout_Tmp);
                        WebUtil.putInAttribute(mode, AttributeName.Form, form);
                        WebUtil.putInAttribute(mode, AttributeName.DetailEntity, dtOut.get(0));
                    }
                }
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.toString());
        }
        return Router.UI_028260_Detail.getView();
    }

    /**
     * 設定維護畫面Button Update的Event。
     * 確定按鈕
     */
    @PostMapping(value = "/rm/UI_028260_Detail/btnConfirm")
    public String btnConfirm(@ModelAttribute UI_028260_Form form, ModelMap mode) {
        this.infoMessage("執行UI_028260_Detail, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);

        Rmin defRmin = new Rmin();
        Rmin defRmin_Tmp = new Rmin();
        Rmout defRmout = new Rmout();
        Rmout defRmout_Tmp = new Rmout();
//        String supId = form.getTbSUPNO().trim();
        String rmin_brno = "900";
        String msg = StringUtils.EMPTY;
//       2021-12-21跳過此檢核  if (checkSUPID(supId,supPwd,mode)){ }
        try {
            //Modified By ChenLi on 2013/10/1 for 改用GetRMOUTbyPK與GetRMINbyPK查詢資料
            defRmin_Tmp.setRminBrno(rmin_brno);
            if (!"".equals(form.getDtTxDate())) {
                defRmin_Tmp.setRminTxdate(form.getDtTxDate().replace("-", ""));
            }
            if (!"".equals(form.getTbFepNo())) {
                form.setTbFepNo(StringUtils.leftPad(form.getTbFepNo(), 7, '0'));
                defRmin_Tmp.setRminFepno(form.getTbFepNo());
            }
            defRmin_Tmp = rmService.getRMINbyPK(defRmin_Tmp);

            if (!"".equals(form.getDtOrgDate())) {
                defRmout_Tmp.setRmoutTxdate(form.getDtOrgDate().replace("-", ""));
            }
            if (!"".equals(form.getTbBrno())) {
                defRmout_Tmp.setRmoutBrno(form.getTbBrno());
            }
            if (!"".equals(form.getDdlOriginal())) {
                defRmout_Tmp.setRmoutOriginal(form.getDdlOriginal().substring(0, 1));
            }
            if (!"".equals(form.getTbOrgFepNo())) {
                form.setTbOrgFepNo(StringUtils.leftPad(form.getTbOrgFepNo(), 7, '0'));
                defRmout_Tmp.setRmoutFepno(form.getTbOrgFepNo());
            }
            defRmout_Tmp = rmService.getRMOUTbyPK(defRmout_Tmp);

            defRmin.setRminOrgdate(form.getDtOrgDate().replace("-", ""));
            defRmin.setRminOrgrmsno(defRmin_Tmp.getRminRmsno());
            defRmin.setRminOrgregNo(defRmout_Tmp.getRmoutFepno());
            defRmin.setRminTxdate(form.getDtTxDate().replace("-", ""));
            defRmin.setRminFepno(form.getTbFepNo());
            defRmin.setRminBrno(rmin_brno);

            if (rmService.updateByPrimaryKeyRmin(defRmin) < 1) {
                this.showMessage(mode, MessageType.WARNING, "匯入主檔(RMIN)更新失敗 RMIN_PKEY="
                        + defRmin.getRminTxdate() + "," + defRmin.getRminBrno() + "," + defRmin.getRminFepno());
                msg = "匯入主檔(RMIN)更新失敗 RMIN_PKEY=" + defRmin.getRminTxdate() + "," + defRmin.getRminBrno() + "," + defRmin.getRminFepno();
            } else {
                //08:匯出退匯
                defRmout.setRmoutStat("08");
                defRmout.setRmoutOrgdate(form.getDtTxDate().replace("-", ""));
                defRmout.setRmoutOrgregFepno(form.getTbFepNo());
                defRmout.setRmoutOrgrmsno(defRmin_Tmp.getRminRmsno());
                defRmout.setRmoutTxdate(form.getDtOrgDate().replace("-", ""));
                defRmout.setRmoutBrno(form.getTbBrno());
                defRmout.setRmoutOriginal(form.getDdlOriginal().substring(0, 1));
                defRmout.setRmoutFepno(defRmout_Tmp.getRmoutFepno());

                if (rmService.updateRMOUTbyPK(defRmout) < 1) {
                    this.showMessage(mode, MessageType.WARNING, "匯出主檔(RMOUT)更新失敗 RMOUT_PKEY="
                            + defRmout.getRmoutTxdate() + "," + defRmout.getRmoutBrno() + "," + defRmout.getRmoutFepno());
                    msg = "匯入主檔(RMIN)更新失敗 RMIN_PKEY=" + defRmout.getRmoutTxdate() + "," +
                            defRmout.getRmoutBrno() + "," + defRmout.getRmoutOriginal() + "," + defRmout.getRmoutFepno();
                } else {
                    this.showMessage(mode, MessageType.WARNING, "修改成功");
                    msg = "RMIN_PKEY=" + defRmout.getRmoutTxdate() + "," +
                            defRmout.getRmoutBrno() + "," + defRmout.getRmoutOriginal() + "," + defRmout.getRmoutFepno() + "修改成功";
                }
            }
            prepareAndSendEMSData(msg);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
            return Router.UI_028260.getView();
        }
        return Router.UI_028260.getView();
    }

    /**
     * 資料格式處理
     */
    public static String matchREMTYPE(Object type) {
        if (type == null) {
            return "";
        } else {
            switch (type.toString()) {
                case "11":
                    return "11:跨行電匯";
                case "12":
                    return "12:公庫匯款";
                case "13":
                    return "13:同業匯款";
                case "18":
                    return "18:證券匯款";
                case "19":
                    return "19:票券匯款";
                default:
                    return type.toString();
            }
        }
    }


    public static String matchAMTTYPE(Object amtType) {
        if (amtType == null) {
            return "";
        } else {
            switch (amtType.toString()) {
                case "001":
                    return "001:現金";
                case "002":
                    return "002:轉帳";
                case "003":
                    return "003:轉帳連動";
                default:
                    return amtType.toString();
            }
        }
    }

    //Authentication (SingleSignOn CHECK 機制)
//    private Boolean checkSUPID(String supId, String supPwd, ModelMap mode) {
//        String errMsg = "";
//        Fepuser defFepuser = new Fepuser();
//        Fepuser loginDefFEPUSER = new Fepuser();
//        String ssoMode = "";
//        String ssoUrl = "";

    /// /        String ssoMode = WebConfiguration.getInstance().getSsoMode();
    /// /        String ssoUrl = WebConfiguration.getInstance().getSsoURL();
//
//        loginDefFEPUSER.setFepuserLogonid(WebUtil.getUser().getUserId());
//        if (!supId.equals(loginDefFEPUSER.getFepuserLogonid())) {
//            logContext.setRemark("SSOURL =" + ssoUrl + "; Start to Check SUPID---------------------------");
//            rmService.logMessage(logContext, Level.INFO);
//            defFepuser.setFepuserLogonid(supId);
//            if (memberShipService.getUserInfo(defFepuser, errMsg)) {
//                String strreturn = StringUtils.EMPTY;
//                if (Boolean.parseBoolean(ssoMode)) {
//                    strreturn = checkAuthentication(ssoUrl, supId, supPwd);
//                    if (!"".equals(strreturn)) {
//                        this.showMessage(mode, MessageType.WARNING, "覆核人員" + strreturn);
//                        return false;
//                    }
//                }
//                return true;
//            } else {
//                logContext.setRemark("SUPID=" + supId + ", Query FEPUSER no data");
//                rmService.logMessage(logContext, Level.INFO);
//                this.showMessage(mode, MessageType.WARNING, "覆核人員" + TxHelper.getMessageFromFEPReturnCode(IOReturnCode.QueryNoData));
//                return false;
//            }
//        } else {
//            this.showMessage(mode, MessageType.WARNING, "覆核人員不可與登入使用者相同");
//            return false;
//        }
//    }

    // 暫時不翻寫
    protected String checkAuthentication(String strURL, String strID, String strSscode) throws Exception {
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream in = null;
        String strRtnResult = StringUtils.EMPTY;
        logContext.setProgramName("UI_028200");
        Integer errorStep = 0;
        try {
            // 送至企業平台主機
            // 組合傳輸字元
            String wsData = "ChkUserID=" + strID.trim() + "&ChkUserPwd=" + strSscode.trim(); // 2024-11-06 Richard modified for 【Cleartext Submission of Sensitive Information】
            URL httpUrl = new URL(strURL);
            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
            os = conn.getOutputStream();
            errorStep = 1;
            // 參數編碼(用http ppost傳輸的方式)
            os.write(wsData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            logContext.setRemark("After GetRequestStream, wsData=" + wsData + ", bs=" + conn.getContentLength());
            rmService.logMessage(logContext, Level.INFO);
            errorStep = 2;
            // 取得 WebResponse 的物件 然後把回傳的資料讀出
            in = conn.getInputStream();
            String responseFormServer = IOUtils.toString(in, StandardCharsets.UTF_8);
            errorStep = 3;
            if (responseFormServer.length() < 3) {
                // 企業平台驗證之回覆資料不足(至少要為"Y,,"-->所以是3碼)
                strRtnResult = "企業平台驗證之回覆資料不足";
            } else {
                // 拆解回傳字串
                String[] strRtn = new String[2];
                // Y成功或N失敗
                strRtn[0] = responseFormServer.substring(0, 1);
                // 用於取第二個逗點的位置, 以取回傳錯誤值
                Integer intPos = responseFormServer.indexOf(",", 2);

                if (intPos < 0) {
                    // 表示可能少第三個的逗點
                    strRtnResult = "企業平台驗證之回覆資料不足";
                } else {
                    if (!",".equals(responseFormServer.substring(1, 2))) {
                        // 第二碼一定要是逗點
                        strRtnResult = "企業平台驗證之回覆資料不足";
                    } else {
                        strRtn[1] = responseFormServer.substring(2, intPos - 2 + 2);
                        if ((intPos + 1) == responseFormServer.length() && strRtn[0].equalsIgnoreCase("Y")) {
                            strRtn[1] = "";
                        } else {
                            if (strRtn[0].equalsIgnoreCase("Y")) {
                                // 第三個回傳值
                                strRtnResult = responseFormServer.substring(intPos + 1, responseFormServer.length() - intPos - 1 + intPos + 1);
                                // 若企業平台的"帳號尚未啟用(-1)"或"帳號不存在(-2)"時, 直接檢核平台身份
                                // If (strRtn(1) = "-1" Or strRtn(1) = "-2") Then
                                // strRtnResult = ""
                                // Else
                                // strRtnResult = strRtn(1) + strRtn(2)
                                // End If
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logContext.setRemark("CheckAuthentication Exception:" + ex.toString() + "; step=" + errorStep);
            rmService.logMessage(logContext, Level.INFO);
            strRtnResult = "驗證錯誤: 連接SSO主機失敗";
            return strRtnResult;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return strRtnResult;
    }

    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(String strMsg) throws Exception {
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028260");
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028260");
        /*Rm*/
        logContext.setMessageGroup("4");
        logContext.setMessageParm13("修改匯出退匯資料" + strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setRemark(TxHelper.getMessageFromFEPReturnCode(RMReturnCode.ModifyRMOUTStat, logContext));
        rmService.logMessage(logContext, Level.INFO);
    }
}
