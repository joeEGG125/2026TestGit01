package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.web.audit.AuditLog;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.business.fisc.FISC;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019010_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 查詢或變更系統狀態
 *
 * @author Joseph
 * @create 2022/05/11
 */
@Controller
public class UI_019010Controller extends BaseController {
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_019010_Form form = new UI_019010_Form();
        form.setSysstat(new Sysstat());
        BindData(form, mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/inbk/UI_019010/confirm")
    public String confirmClick(@ModelAttribute UI_019010_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        int iRes = 0;
        Sysstat defSYSSTAT = inbkService.getStatus();
        Sysstat dtSYSSTAT = inbkService.getStatus();
        LogData logContext = new LogData();
        this.doKeepFormData(mode, form);

        try {
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            // 塞入auditLog.params
            String SysstatAocName_1000 = getName(form.getSysstat().getSysstatAoct1000(),null,null);
            String SysstatMbacName_1000 = getName(null,form.getSysstat().getSysstatAoct1000(),null);
            auditLog.addParam("1000 通匯各類子系統", SysstatAocName_1000 +" "+SysstatMbacName_1000 );

            String SysstatAocName_1100 = getName(form.getSysstat().getSysstatAoct1100(),null,null);
            String SysstatMbacName_1100 = getName(null,form.getSysstat().getSysstatAoct1100(),null);
            auditLog.addParam("1100 匯款類子系統", SysstatAocName_1100 +" "+SysstatMbacName_1100 );

            String SysstatAocName_1200 = getName(form.getSysstat().getSysstatAoct1200(),null,null);
            String SysstatMbacName_1200 = getName(null,form.getSysstat().getSysstatAoct1200(),null);
            auditLog.addParam("1200 代收款項類子系統", SysstatAocName_1200 +" "+SysstatMbacName_1200 );

            String SysstatAocName_1300 = getName(form.getSysstat().getSysstatAoct1300(),null,null);
            String SysstatMbacName_1300 = getName(null,form.getSysstat().getSysstatAoct1300(),null);
            auditLog.addParam("1300 代繳代發類子系統", SysstatAocName_1300 +" "+SysstatMbacName_1300 );

            String SysstatAocName_1400 = getName(form.getSysstat().getSysstatAoct1400(),null,null);
            String SysstatMbacName_1400 = getName(null,form.getSysstat().getSysstatAoct1400(),null);
            auditLog.addParam("1400 一般通信類子系統", SysstatAocName_1400 +" "+SysstatMbacName_1400 );

            String SysstatMbacName_2000 = getName(null,form.getSysstat().getSysstatMbact2000(),null);
            auditLog.addParam("2000 CD/ATM 共用系統提款作業", SysstatMbacName_2000 );

            String SysstatMbacName_2200 = getName(null,form.getSysstat().getSysstatMbact2200(),null);
            auditLog.addParam("2200 CD/ATM 共用系統轉帳作業", SysstatMbacName_2200 );

            String SysstatMbacName_2500 = getName(null,form.getSysstat().getSysstatMbact2500(),null);
            auditLog.addParam("2500 晶片卡共用系統", SysstatMbacName_2500 );

            String SysstatMbacName_2510 = getName(null,form.getSysstat().getSysstatMbact2510(),null);
            auditLog.addParam("2510 晶片卡提款作業", SysstatMbacName_2510 );

            String SysstatMbacName_2520 = getName(null,form.getSysstat().getSysstatMbact2520(),null);
            auditLog.addParam("2520 晶片卡轉帳作業", SysstatMbacName_2520 );

            String SysstatMbacName_2530 = getName(null,form.getSysstat().getSysstatMbact2530(),null);
            auditLog.addParam("2530 晶片卡繳款作業", SysstatMbacName_2530 );

            String SysstatMbacName_2540 = getName(null,form.getSysstat().getSysstatMbact2540(),null);
            auditLog.addParam("2540 晶片卡消費扣款作業", SysstatMbacName_2540 );

            String SysstatMbacName_2550 = getName(null,form.getSysstat().getSysstatMbact2550(),null);
            auditLog.addParam("2550 晶片卡預先授權作業", SysstatMbacName_2550 );

            String SysstatMbacName_2560 = getName(null,form.getSysstat().getSysstatMbact2560(),null);
            auditLog.addParam("2560 晶片卡全國繳費作業", SysstatMbacName_2560 );

            String SysstatMbacName_2570 = getName(null,form.getSysstat().getSysstatMbact2570(),null);
            auditLog.addParam("2570 晶片卡跨國提款作業", SysstatMbacName_2570 );

            String SysstatMbacName_2700 = getName(null,form.getSysstat().getSysstatMbact2700(),null);
            auditLog.addParam("2700 資金調撥作業", SysstatMbacName_2700 );

            String SysstatMbacName_7100 = getName(null,form.getSysstat().getSysstatMbact7100(),null);
            auditLog.addParam("7100 轉帳退款類交易", SysstatMbacName_7100 );

            String SysstatMbacName_7300 = getName(null,form.getSysstat().getSysstatMbact7300(),null);
            auditLog.addParam("7300 FXML跨行付款交易", SysstatMbacName_7300 );
            setAuditLog(auditLog);

            getLogContext().setProgramName("UI_019010");
            defSYSSTAT.setSysstatHbkno(dtSYSSTAT.getSysstatHbkno());
            // AOCT
            if (dtSYSSTAT.getSysstatAoct1000().equals(form.getSysstat().getSysstatAoct1000())) {
                defSYSSTAT.setSysstatAoct1100(form.getSysstat().getSysstatAoct1100());
                defSYSSTAT.setSysstatAoct1200(form.getSysstat().getSysstatAoct1200());
                defSYSSTAT.setSysstatAoct1300(form.getSysstat().getSysstatAoct1300());
                defSYSSTAT.setSysstatAoct1400(form.getSysstat().getSysstatAoct1400());
            } else {
                defSYSSTAT.setSysstatAoct1000(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1100(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1200(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1300(form.getSysstat().getSysstatAoct1000());
                defSYSSTAT.setSysstatAoct1400(form.getSysstat().getSysstatAoct1000());
            }
            // MBACT
            if (dtSYSSTAT.getSysstatMbact1000().equals(form.getSysstat().getSysstatMbact1000())) {
                defSYSSTAT.setSysstatMbact1100(form.getSysstat().getSysstatMbact1100());
                defSYSSTAT.setSysstatMbact1200(form.getSysstat().getSysstatMbact1200());
                defSYSSTAT.setSysstatMbact1300(form.getSysstat().getSysstatMbact1300());
                defSYSSTAT.setSysstatMbact1400(form.getSysstat().getSysstatMbact1400());
            } else {
                defSYSSTAT.setSysstatMbact1000(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1100(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1200(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1300(form.getSysstat().getSysstatMbact1000());
                defSYSSTAT.setSysstatMbact1400(form.getSysstat().getSysstatMbact1000());
            }
            // SYSSTAT_MBACT_2000～SYSSTAT_MBACT_2200
            defSYSSTAT.setSysstatMbact2000(form.getSysstat().getSysstatMbact2000());
            defSYSSTAT.setSysstatMbact2200(form.getSysstat().getSysstatMbact2200());
            // SYSSTAT_MBACT_2500～SYSSTAT_MBACT_2560
            if (dtSYSSTAT.getSysstatMbact2500().equals(form.getSysstat().getSysstatMbact2500())) {
                defSYSSTAT.setSysstatMbact2510(form.getSysstat().getSysstatMbact2510());
                defSYSSTAT.setSysstatMbact2520(form.getSysstat().getSysstatMbact2520());
                defSYSSTAT.setSysstatMbact2530(form.getSysstat().getSysstatMbact2530());
                defSYSSTAT.setSysstatMbact2540(form.getSysstat().getSysstatMbact2540());
                defSYSSTAT.setSysstatMbact2550(form.getSysstat().getSysstatMbact2550());
                defSYSSTAT.setSysstatMbact2560(form.getSysstat().getSysstatMbact2560());
                defSYSSTAT.setSysstatMbact2570(form.getSysstat().getSysstatMbact2570());
            } else {
                defSYSSTAT.setSysstatMbact2500(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2510(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2520(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2530(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2540(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2550(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2560(form.getSysstat().getSysstatMbact2500());
                defSYSSTAT.setSysstatMbact2570(form.getSysstat().getSysstatMbact2500());
            }
            // SYSSTAT_MBACT_2700
            defSYSSTAT.setSysstatMbact2700(form.getSysstat().getSysstatMbact2700());
            // SYSSTAT_MBACT_7100
            defSYSSTAT.setSysstatMbact7100(form.getSysstat().getSysstatMbact7100());
            // SYSSTAT_MBACT_7300
            defSYSSTAT.setSysstatMbact7300(form.getSysstat().getSysstatMbact7300());
            iRes = inbkService.UpdateSYSSTAT(defSYSSTAT);
            if (iRes > 0) {
                FISC ofiscbusiness = new FISC();
                ofiscbusiness.setLogContext(logContext);
                FEPReturnCode rtnCode = CommonReturnCode.Normal;

                if (rtnCode != CommonReturnCode.Normal) {
                    getLogContext().setRemark("更新FEP成功，更新EAINET失敗");
                    getLogContext().setReturnCode(rtnCode);
                    inbkService.inbkLogMessage(Level.INFO, logContext);
                }
                this.showMessage(mode, MessageType.INFO, UpdateSuccess);
                BindData(form, mode);
            } else {
                this.showMessage(mode, MessageType.WARNING, UpdateFail);
            }

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            FEPBase.sendEMS(getLogContext());
            //20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
            this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
        }
        return Router.UI_019010.getView();
    }

    @PostMapping(value = "/inbk/UI_019010/query")
    public String queryClick(@ModelAttribute UI_019010_Form form, ModelMap mode) {
        this.infoMessage("執行查詢最新系統狀態資料...");
        try {
            BindData(form, mode);

            this.showMessage(mode, MessageType.INFO, "查詢成功，已取得最新資料");

        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019010.getView();
    }

    public String getName(String sysstatAoc,String sysstatMbac,String id){
        String name="";
        if (sysstatAoc != null && sysstatMbac == null) {
            if (sysstatAoc.equals("0")) {
                name = "財金:" + "0-日終 House Keeping 完成";
            }
            if (sysstatAoc.equals("1")) {
                name = "財金:" + "1-日初 House Keeping 完成";
            }
            if (sysstatAoc.equals("2")) {
                name = "財金:" + "2-財金公司AP停止(Stop)作業";
            }
            if (sysstatAoc.equals("A")) {
                name = "財金:" + "A-匯出Exceptional Checkout完成";
            }
            if (sysstatAoc.equals("B")) {
                name = "財金:" + "B-匯入Exceptional Checkout完成";
            }
            if (sysstatAoc.equals("3")) {
                name = "財金:" + "3-AP Pre-checkout參加單位啟動";
            }
            if (sysstatAoc.equals("4")) {
                name = "財金:" + "4-AP Pre-checkout財金公司啟動";
            }
            if (sysstatAoc.equals("C")) {
                name = "財金:" + "C-AP Pre-checkout參加單位啟動";
            }
            if (sysstatAoc.equals("D")) {
                name = "財金:" + "D-AP Pre-checkout財金公司啟動";
            }
            if (sysstatAoc.equals("5")) {
                name = "財金:" + "5-AP checkout(由參加單位啟動)";
            }
            if (sysstatAoc.equals("7")) {
                name = "財金:" + "7-AP checkout(由財金公司啟動)";
            }
            if (sysstatAoc.equals("9")) {
                name = "財金:" + "9-財金公司於結算帳時停止作業";
            }
        }
        if (sysstatMbac != null && sysstatAoc == null) {
            if (sysstatMbac.equals("0")) {
                name = "本行:" + "0-日終 House Keeping 完成";
            }
            if (sysstatMbac.equals("1")) {
                name = "本行:" + "1-AP Checkin";
            }
            if (sysstatMbac.equals("2")) {
                name = "本行:" + "2-AP Exceptional Checkout";
            }
            if (sysstatMbac.equals("A")) {
                name = "本行:" + "A-匯出Exceptional Checkout完成";
            }
            if (sysstatMbac.equals("B")) {
                name = "本行:" + "B-匯入Exceptional Checkout完成";
            }
            if (sysstatMbac.equals("3")) {
                name = "本行:" + "3-AP Pre-Checkout參加單位啟動";
            }
            if (sysstatMbac.equals("4")) {
                name = "本行:" + "4-AP Pre-Checkout財金公司啟動";
            }
            if (sysstatMbac.equals("C")) {
                name = "本行:" + "C-AP Pre-Checkout參加單位啟動";
            }
            if (sysstatMbac.equals("D")) {
                name = "本行:" + "D-AP Pre-Checkout財金公司啟動";
            }
            if (sysstatMbac.equals("5")) {
                name = "本行:" + "5-AP Checkout(由參加單位啟動)";
            }
            if (sysstatMbac.equals("7")) {
                name = "本行:" + "7-AP Checkout(由財金公司啟動)";
            }
            if (sysstatMbac.equals("9")) {
                name = "本行:" + "9-Evening Call(由參加單位啟動)";
            }
        }
        return name;
    }

    // DB相關
    private void BindData(UI_019010_Form form, ModelMap mode) {
        this.doKeepFormData(mode, form);

        try {
            Sysstat dtSysstat = inbkService.getStatus();
            if (dtSysstat != null) {
                form.setSysstat(dtSysstat); // 將查詢的結果塞入到form中
                form.getSysstat().setSysstatSoct(dtSysstat.getSysstatSoct() + "-" + getSoctName(dtSysstat.getSysstatSoct()));
                form.getSysstat().setSysstatMboct(dtSysstat.getSysstatSoct() + "-" + getSoctName(dtSysstat.getSysstatSoct()));
                form.getSysstat().setSysstatAoct2000(dtSysstat.getSysstatAoct2000() + "-" + getAoctName(dtSysstat.getSysstatAoct2000()));
                form.getSysstat().setSysstatAoct2200(dtSysstat.getSysstatAoct2200() + "-" + getAoctName(dtSysstat.getSysstatAoct2200()));
                form.getSysstat().setSysstatAoct2500(dtSysstat.getSysstatAoct2500() + "-" + getAoctName(dtSysstat.getSysstatAoct2500()));
                form.getSysstat().setSysstatAoct2510(dtSysstat.getSysstatAoct2510() + "-" + getAoctName(dtSysstat.getSysstatAoct2510()));
                form.getSysstat().setSysstatAoct2520(dtSysstat.getSysstatAoct2520() + "-" + getAoctName(dtSysstat.getSysstatAoct2520()));
                form.getSysstat().setSysstatAoct2530(dtSysstat.getSysstatAoct2530() + "-" + getAoctName(dtSysstat.getSysstatAoct2530()));
                form.getSysstat().setSysstatAoct2540(dtSysstat.getSysstatAoct2540() + "-" + getAoctName(dtSysstat.getSysstatAoct2540()));
                form.getSysstat().setSysstatAoct2550(dtSysstat.getSysstatAoct2550() + "-" + getAoctName(dtSysstat.getSysstatAoct2550()));
                form.getSysstat().setSysstatAoct2560(dtSysstat.getSysstatAoct2560() + "-" + getAoctName(dtSysstat.getSysstatAoct2560()));
                form.getSysstat().setSysstatAoct2570(dtSysstat.getSysstatAoct2570() + "-" + getAoctName(dtSysstat.getSysstatAoct2570()));
                form.getSysstat().setSysstatAoct2700(dtSysstat.getSysstatAoct2700() + "-" + getAoctName(dtSysstat.getSysstatAoct2700()));
                form.getSysstat().setSysstatAoct7100(dtSysstat.getSysstatAoct7100() + "-" + getAoctName(dtSysstat.getSysstatAoct7100()));
                form.getSysstat().setSysstatAoct7300(dtSysstat.getSysstatAoct7300() + "-" + getAoctName(dtSysstat.getSysstatAoct7300()));

                // 其他通道
                form.setCbs(DbHelper.toBoolean(dtSysstat.getSysstatCbs()));
            }
            WebUtil.putInAttribute(mode, AttributeName.Options, form);
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage()); // 列印異常的log
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

}
