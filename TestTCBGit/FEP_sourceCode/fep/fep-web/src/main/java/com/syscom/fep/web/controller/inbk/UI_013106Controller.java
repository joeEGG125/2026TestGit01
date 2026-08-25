package com.syscom.fep.web.controller.inbk;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_OPC;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.inbk.UI_013106_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 應用系統異常連線作業-3106/7
 *
 * @author xingyun_yang
 * @create 2021/10/13
 */
@Controller
public class UI_013106Controller extends BaseController {
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        this.bindConstant(mode);
        // 初始化表單數據
        UI_013106_Form form = new UI_013106_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 為頁面綁定一些常量
     * @param mode ModelMap
     */
    private void bindConstant(ModelMap mode) {
        // 初始化PCODE下拉選單
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<String>("", ""));
//        selectOptionList.add(new SelectOption<String>("1000-通匯各類子系統", "1000"));
//        selectOptionList.add(new SelectOption<String>("1100-匯款類子系統", "1100"));
//        selectOptionList.add(new SelectOption<String>("1200-代收款項類子系統", "1200"));
//        selectOptionList.add(new SelectOption<String>("1300-代繳代發類子系統", "1300"));
//        selectOptionList.add(new SelectOption<String>("1400-一般通信類子系統", "1400"));
//        selectOptionList.add(new SelectOption<String>("1600-外幣通匯各類子系統", "1600"));
        selectOptionList.add(new SelectOption<String>("2000-CD/ATM 共用系統提款作業", "2000"));
        selectOptionList.add(new SelectOption<String>("2200-CD/ATM 共用系統轉帳作業", "2200"));
        selectOptionList.add(new SelectOption<String>("2500-晶片卡共用系統", "2500"));
        selectOptionList.add(new SelectOption<String>("2510-晶片卡提款作業", "2510"));
        selectOptionList.add(new SelectOption<String>("2520-晶片卡轉帳作業", "2520"));
        selectOptionList.add(new SelectOption<String>("2530-晶片卡繳款作業", "2530"));
        selectOptionList.add(new SelectOption<String>("2540-晶片卡消費扣款作業", "2540"));
        selectOptionList.add(new SelectOption<String>("2550-晶片卡預先授權作業", "2550"));
        selectOptionList.add(new SelectOption<String>("2560-晶片卡全國繳費作業", "2560"));
        selectOptionList.add(new SelectOption<String>("2570-晶片卡跨國提款作業", "2570"));
        selectOptionList.add(new SelectOption<String>("2700-資金調撥作業", "2700"));
//        selectOptionList.add(new SelectOption<String>("7100-轉帳退款類交易", "7100"));
        selectOptionList.add(new SelectOption<String>("7300-FXML跨行付款交易", "7300"));
        bindData(mode);
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    /**
     * 3106緊急停止(EXCEPTION OUT)
     * 3107重新啟動(EXCEPTION IN)
     * 跨行連線作業通知電文給財金
     * @param form UI_013106_Form
     * @param mode ModelMap
     * @return String
     */
    @PostMapping(value = "/inbk/UI_013106/confirmClick")
    public String confirmClick(@ModelAttribute UI_013106_Form form, ModelMap mode) {
        this.infoMessage("執行明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        FISCGeneral aData = new FISCGeneral();
        FEPHandler fepHandler = new FEPHandler();
        String[] message;
        try {
            if (!checkData(form.getaPIDCob(),mode)){
                return Router.UI_013106.getView();
            }

            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("確認");
            if (StringUtils.isNotBlank(form.getaPIDCob())) {
                auditLog.addParam("輸入AP-ID", form.getaPIDCob());
            }
            if (form.getRadioOption().equals(UI_013106_Form.RadioOption.EXCHECKINRBN)){
                auditLog.addParam("EXCHECKINRBN", "EX-CHECK IN");
            }else {
                auditLog.addParam("EXCHECKOUTRBN", "EX-CHECK OUT");
            }
            if (StringUtils.isNotBlank(form.getcURDdl())) {
                auditLog.addParam("幣別", form.getcURDdl());
            }
            setAuditLog(auditLog);

            aData.setSubSystem(FISCSubSystem.OPC);
            aData.setOPCRequest(new FISC_OPC());
            aData.getOPCRequest().setMessageKind(MessageFlow.Request);
            if (form.getRadioOption().equals(UI_013106_Form.RadioOption.EXCHECKINRBN)){
                aData.getOPCRequest().setProcessingCode("3107");
            }else {
                aData.getOPCRequest().setProcessingCode("3106");
            }

            aData.getOPCRequest().setMessageType("0600");
            aData.getOPCRequest().setAPID(form.getaPIDCob());
//            if (!"".equals(form.getcURDdl())){
//                aData.getOPCRequest().setCUR(form.getcURDdl());
//            }
            //add by Maxine on 2011/09/02 for EMS加UserId
            aData.getOPCRequest().setLogContext(new LogData());
            //modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
            aData.getOPCRequest().getLogContext().setTxUser(WebUtil.getUser().getUserId());

            //Call AA
            fepHandler.dispatch(FEPChannel.FEP,aData);

            //將AA RC 顯示在UI上
            if (aData.getDescription() == null || "".equals(aData.getDescription().toString().trim())){
                aData.setDescription(MessageError);
            }

            String description = aData.getDescription();
            String code = "";
            boolean isSyncSuccess = false;

            // 包含 "-"，再進行分割
            if (description.contains("-")) {
                message = description.split("-");
                code = message[0].replace("財金回覆", "").trim(); // 取出 "0001"

                if (message.length > 1) {
                    isSyncSuccess = message[1].contains("同步成功");  // IMS主機同步成功
                }
            }

            // 若AA回的是財金RC(RC-Description)而且財金RC=0001，且成功同步，UI顯示處理成功
            if ("0001".equals(code) && isSyncSuccess ){
                this.showMessage(mode, MessageType.INFO, description);
            } else {
                // 沒有 "-", code 會是空字串，isSyncSuccess 會是 false，顯示錯誤
                this.showMessage(mode, MessageType.DANGER, description);
            }

        } catch (Exception ex) {
        	//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
            //this.showMessage(mode,MessageType.DANGER,ex);
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
        } finally {
            this.bindConstant(mode);
        }
        return Router.UI_013106.getView();
    }

    //DB相關
    private void bindData(ModelMap mode){
        Sysstat dtSysstat = new Sysstat();
        //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
        //Fcrmstat defFcrmstat = new Fcrmstat();
        //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
        try {
           //defSYSSTAT = SysStatus.PropertyValue
            dtSysstat = inbkService.getStatus();
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
            //defFcrmstat = inbkService.getFCRMSTAT();
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
            if (StringUtils.isNotBlank(dtSysstat.fieldsToXml())){
                String soctLbl = StringUtils.join(dtSysstat.getSysstatSoct(),"-",getSoctName(dtSysstat.getSysstatSoct()));
                String mboctLbl = StringUtils.join(dtSysstat.getSysstatMboct(),"-",getMboctName(dtSysstat.getSysstatMboct()));

                String acot1000Lbl = StringUtils.join(dtSysstat.getSysstatAoct1000(),"-",getAoctName(dtSysstat.getSysstatAoct1000()));
                String aoct1100Lbl = StringUtils.join(dtSysstat.getSysstatAoct1100(),"-",getAoctName(dtSysstat.getSysstatAoct1100()));
                String aoct1200Lbl = StringUtils.join(dtSysstat.getSysstatAoct1200(),"-",getAoctName(dtSysstat.getSysstatAoct1200()));
                String aoct1300Lbl = StringUtils.join(dtSysstat.getSysstatAoct1300(),"-",getAoctName(dtSysstat.getSysstatAoct1300()));
                String aoct1400Lbl = StringUtils.join(dtSysstat.getSysstatAoct1400(),"-",getAoctName(dtSysstat.getSysstatAoct1400()));
                String aoct2000Lbl = StringUtils.join(dtSysstat.getSysstatAoct2000(),"-",getAoctName(dtSysstat.getSysstatAoct2000()));
                String aoct2200Lbl = StringUtils.join(dtSysstat.getSysstatAoct2200(),"-",getAoctName(dtSysstat.getSysstatAoct2200()));
                String aoct2500Lbl = StringUtils.join(dtSysstat.getSysstatAoct2500(),"-",getAoctName(dtSysstat.getSysstatAoct2500()));
                String aoct2510Lbl = StringUtils.join(dtSysstat.getSysstatAoct2510(),"-",getAoctName(dtSysstat.getSysstatAoct2510()));
                String aoct2520Lbl = StringUtils.join(dtSysstat.getSysstatAoct2520(),"-",getAoctName(dtSysstat.getSysstatAoct2520()));
                String aoct2530Lbl = StringUtils.join(dtSysstat.getSysstatAoct2530(),"-",getAoctName(dtSysstat.getSysstatAoct2530()));
                String aoct2540Lbl = StringUtils.join(dtSysstat.getSysstatAoct2540(),"-",getAoctName(dtSysstat.getSysstatAoct2540()));
                String aoct2550Lbl = StringUtils.join(dtSysstat.getSysstatAoct2550(),"-",getAoctName(dtSysstat.getSysstatAoct2550()));
                String aoct2560Lbl = StringUtils.join(dtSysstat.getSysstatAoct2560(),"-",getAoctName(dtSysstat.getSysstatAoct2560()));
                String aoct2570Lbl = StringUtils.join(dtSysstat.getSysstatAoct2570(),"-",getAoctName(dtSysstat.getSysstatAoct2570()));
                String aoct2700Lbl = StringUtils.join(dtSysstat.getSysstatAoct2700(),"-",getAoctName(dtSysstat.getSysstatAoct2700()));
                String aoct7100Lbl = StringUtils.join(dtSysstat.getSysstatAoct7100(),"-",getAoctName(dtSysstat.getSysstatAoct7100()));
                String aoct7300Lbl = StringUtils.join(dtSysstat.getSysstatAoct7300(),"-",getAoctName(dtSysstat.getSysstatAoct7300()));

                String mbact1000Lbl = StringUtils.join(dtSysstat.getSysstatMbact1000(),"-",getMbactName(dtSysstat.getSysstatMbact1000()));
                String mbact1100Lbl = StringUtils.join(dtSysstat.getSysstatMbact1100(),"-",getMbactName(dtSysstat.getSysstatMbact1100()));
                String mbact1200Lbl = StringUtils.join(dtSysstat.getSysstatMbact1200(),"-",getMbactName(dtSysstat.getSysstatMbact1200()));
                String mbact1300Lbl = StringUtils.join(dtSysstat.getSysstatMbact1300(),"-",getMbactName(dtSysstat.getSysstatMbact1300()));
                String mbact1400Lbl = StringUtils.join(dtSysstat.getSysstatMbact1400(),"-",getMbactName(dtSysstat.getSysstatMbact1400()));
                String mbact2000Lbl = StringUtils.join(dtSysstat.getSysstatMbact2000(),"-",getMbactName(dtSysstat.getSysstatMbact2000()));
                String mbact2200Lbl = StringUtils.join(dtSysstat.getSysstatMbact2200(),"-",getMbactName(dtSysstat.getSysstatMbact2200()));
                String mbact2500Lbl = StringUtils.join(dtSysstat.getSysstatMbact2500(),"-",getMbactName(dtSysstat.getSysstatMbact2500()));
                String mbact2510Lbl = StringUtils.join(dtSysstat.getSysstatMbact2510(),"-",getMbactName(dtSysstat.getSysstatMbact2510()));
                String mbact2520Lbl = StringUtils.join(dtSysstat.getSysstatMbact2520(),"-",getMbactName(dtSysstat.getSysstatMbact2520()));
                String mbact2530Lbl = StringUtils.join(dtSysstat.getSysstatMbact2530(),"-",getMbactName(dtSysstat.getSysstatMbact2530()));
                String mbact2540Lbl = StringUtils.join(dtSysstat.getSysstatMbact2540(),"-",getMbactName(dtSysstat.getSysstatMbact2540()));
                String mbact2550Lbl = StringUtils.join(dtSysstat.getSysstatMbact2550(),"-",getMbactName(dtSysstat.getSysstatMbact2550()));
                String mbact2560Lbl = StringUtils.join(dtSysstat.getSysstatMbact2560(),"-",getMbactName(dtSysstat.getSysstatMbact2560()));
                String mbact2570Lbl = StringUtils.join(dtSysstat.getSysstatMbact2570(),"-",getMbactName(dtSysstat.getSysstatMbact2570()));
                String mbact2700Lbl = StringUtils.join(dtSysstat.getSysstatMbact2700(),"-",getMbactName(dtSysstat.getSysstatMbact2700()));
                String mbact7100Lbl = StringUtils.join(dtSysstat.getSysstatMbact7100(),"-",getMbactName(dtSysstat.getSysstatMbact7100()));
                String mbact7300Lbl = StringUtils.join(dtSysstat.getSysstatMbact7300(),"-",getMbactName(dtSysstat.getSysstatMbact7300()));


                mode.addAttribute("soctLbl", soctLbl);
                mode.addAttribute("mboctLbl", mboctLbl);

                mode.addAttribute("acot1000Lbl", acot1000Lbl);
                mode.addAttribute("aoct1100Lbl", aoct1100Lbl);
                mode.addAttribute("aoct1200Lbl", aoct1200Lbl);
                mode.addAttribute("aoct1300Lbl", aoct1300Lbl);
                mode.addAttribute("aoct1400Lbl", aoct1400Lbl);
                mode.addAttribute("aoct2000Lbl", aoct2000Lbl);
                mode.addAttribute("aoct2200Lbl", aoct2200Lbl);
                mode.addAttribute("aoct2500Lbl", aoct2500Lbl);
                mode.addAttribute("aoct2510Lbl", aoct2510Lbl);
                mode.addAttribute("aoct2520Lbl", aoct2520Lbl);
                mode.addAttribute("aoct2530Lbl", aoct2530Lbl);
                mode.addAttribute("aoct2540Lbl", aoct2540Lbl);
                mode.addAttribute("aoct2550Lbl", aoct2550Lbl);
                mode.addAttribute("aoct2560Lbl", aoct2560Lbl);
                mode.addAttribute("aoct2570Lbl", aoct2570Lbl);
                mode.addAttribute("aoct2700Lbl", aoct2700Lbl);
                mode.addAttribute("aoct7100Lbl", aoct7100Lbl);
                mode.addAttribute("aoct7300Lbl", aoct7300Lbl);

                mode.addAttribute("mbact1000Lbl", mbact1000Lbl);
                mode.addAttribute("mbact1100Lbl", mbact1100Lbl);
                mode.addAttribute("mbact1200Lbl", mbact1200Lbl);
                mode.addAttribute("mbact1300Lbl", mbact1300Lbl);
                mode.addAttribute("mbact1400Lbl", mbact1400Lbl);
                mode.addAttribute("mbact2000Lbl", mbact2000Lbl);
                mode.addAttribute("mbact2200Lbl", mbact2200Lbl);
                mode.addAttribute("mbact2500Lbl", mbact2500Lbl);
                mode.addAttribute("mbact2510Lbl", mbact2510Lbl);
                mode.addAttribute("mbact2520Lbl", mbact2520Lbl);
                mode.addAttribute("mbact2530Lbl", mbact2530Lbl);
                mode.addAttribute("mbact2540Lbl", mbact2540Lbl);
                mode.addAttribute("mbact2550Lbl", mbact2550Lbl);
                mode.addAttribute("mbact2560Lbl", mbact2560Lbl);
                mode.addAttribute("mbact2570Lbl", mbact2570Lbl);
                mode.addAttribute("mbact2700Lbl", mbact2700Lbl);
                mode.addAttribute("mbact7100Lbl", mbact7100Lbl);
                mode.addAttribute("mbact7300Lbl", mbact7300Lbl);

                //-----以下功能 移至"FEP 服務/通路/線路控制"處理-----
                //自行
//                Boolean intra = DbHelper.toBoolean(dtSysstat.getSysstatIntra());
//                Boolean iwdI = DbHelper.toBoolean(dtSysstat.getSysstatIwdI());
                //                SYSSTAT_IWD_I.ForeColor = Drawing.Color.Black
//                Boolean iftI = DbHelper.toBoolean(dtSysstat.getSysstatIftI());
//                Boolean admI = DbHelper.toBoolean(dtSysstat.getSysstatAdmI());
//                Boolean fwdI = DbHelper.toBoolean(dtSysstat.getSysstatFwdI());
//                Boolean ipyI = DbHelper.toBoolean(dtSysstat.getSysstatIpyI());
//                Boolean iccdpI = DbHelper.toBoolean(dtSysstat.getSysstatIccdpI());
//                Boolean etxI = DbHelper.toBoolean(dtSysstat.getSysstatEtxI());
//                Boolean caI = DbHelper.toBoolean(dtSysstat.getSysstatCaI());
//                Boolean caaI = DbHelper.toBoolean(dtSysstat.getSysstatCaaI());
//                Boolean aig = DbHelper.toBoolean(dtSysstat.getSysstatAig());
//                Boolean hkIssue = DbHelper.toBoolean(dtSysstat.getSysstatHkIssue());
//                Boolean moIssue = DbHelper.toBoolean(dtSysstat.getSysstatMoIssue());
//                Boolean hkFiscmb = DbHelper.toBoolean(dtSysstat.getSysstatHkFiscmb());
//                Boolean moFiscmb = DbHelper.toBoolean(dtSysstat.getSysstatMoFiscmb());
//                Boolean hkPlus = DbHelper.toBoolean(dtSysstat.getSysstatHkPlus());
//                Boolean moPlus = DbHelper.toBoolean(dtSysstat.getSysstatMoPlus());
                //2022/08/16 Bruce add
//                Boolean iiqI = DbHelper.toBoolean(dtSysstat.getSysstatIiqI());//餘額查詢
//                Boolean nwdi = DbHelper.toBoolean(dtSysstat.getSysstatNwdI());//無卡提款
//                Boolean nfwi = DbHelper.toBoolean(dtSysstat.getSysstatNfwI());//外幣無卡提款
//                mode.addAttribute("iiqI",iiqI);
//                mode.addAttribute("nwdi",nwdi);
//                mode.addAttribute("nfwi",nfwi);
//                mode.addAttribute("intra", intra);
//                mode.addAttribute("iwdI", iwdI);
//                mode.addAttribute("iftI", iftI);
//                mode.addAttribute("admI", admI);
//                mode.addAttribute("fwdI", fwdI);
//                mode.addAttribute("ipyI", ipyI);
//                mode.addAttribute("iccdpI", iccdpI);
//                mode.addAttribute("etxI", etxI);
//                mode.addAttribute("caI", caI);
//                mode.addAttribute("caaI", caaI);
//                mode.addAttribute("aig", aig);
//                mode.addAttribute("hkIssue", hkIssue);
//                mode.addAttribute("moIssue", moIssue);
//                mode.addAttribute("hkFiscmb", hkFiscmb);
//                mode.addAttribute("moFiscmb", moFiscmb);
//                mode.addAttribute("hkPlus", hkPlus);
//                mode.addAttribute("moPlus", moPlus);
                //代理行   
//                Boolean agent = DbHelper.toBoolean(dtSysstat.getSysstatAgent());
//                Boolean iwdA = DbHelper.toBoolean(dtSysstat.getSysstatIwdA());
//                Boolean iftA = DbHelper.toBoolean(dtSysstat.getSysstatIftA());
//                Boolean ipyA = DbHelper.toBoolean(dtSysstat.getSysstatIpyA());
//                Boolean iccdpA = DbHelper.toBoolean(dtSysstat.getSysstatIccdpA());
//                Boolean etxA = DbHelper.toBoolean(dtSysstat.getSysstatEtxA());
//                Boolean t2525A = DbHelper.toBoolean(dtSysstat.getSysstat2525A());
//                Boolean cpuA = DbHelper.toBoolean(dtSysstat.getSysstatCpuA());
//                Boolean cafA = DbHelper.toBoolean(dtSysstat.getSysstatCafA());
//                Boolean cavA = DbHelper.toBoolean(dtSysstat.getSysstatCavA());
//                Boolean camA = DbHelper.toBoolean(dtSysstat.getSysstatCamA());
//                Boolean cajA = DbHelper.toBoolean(dtSysstat.getSysstatCajA());
//                Boolean cauA = DbHelper.toBoolean(dtSysstat.getSysstatCauA());
//                Boolean cwvA = DbHelper.toBoolean(dtSysstat.getSysstatCwvA());
//                Boolean cwmA = DbHelper.toBoolean(dtSysstat.getSysstatCwmA());
//                //2022/08/16 Bruce add
//                Boolean fawa = DbHelper.toBoolean(dtSysstat.getSysstatFawA());//外幣提款
//                Boolean admA = DbHelper.toBoolean(dtSysstat.getSysstatAdmA());//跨行存款
//                Boolean cdpA = DbHelper.toBoolean(dtSysstat.getSysstatCdpA());//全國繳費(ID+ACC)
//                Boolean eafA = DbHelper.toBoolean(dtSysstat.getSysstatEafA());//EMV 國際提款銀聯卡
//                Boolean ewvA = DbHelper.toBoolean(dtSysstat.getSysstatEwvA());//EMV國際提款(PLUS)
//                DbHelper.toBoolean(dtSysstat.getSysstatEwmA());//EMV國際提款(CIRRUS)
//                Boolean eavA = DbHelper.toBoolean(dtSysstat.getSysstatEavA());//EMV預借現金(VISA)
//                Boolean eamA = DbHelper.toBoolean(dtSysstat.getSysstatEamA());//EMV預借現金(MASTER)
//                Boolean nwdA = DbHelper.toBoolean(dtSysstat.getSysstatNwdA());//無卡跨行提款
//                Boolean vaaA = DbHelper.toBoolean(dtSysstat.getSysstatVaaA());//約定及核驗服務
//                Boolean eajA = DbHelper.toBoolean(dtSysstat.getSysstatEajA());//EMV預借現金(JCB)
//                mode.addAttribute("fawa", fawa);
//                mode.addAttribute("eajA", eajA);
//                mode.addAttribute("vaaA", vaaA);
//                mode.addAttribute("eavA", eavA);
//                mode.addAttribute("fawa", fawa);
//                mode.addAttribute("eamA", eamA);
//                mode.addAttribute("nwdA", nwdA);
//                mode.addAttribute("ewvA", ewvA);
//                mode.addAttribute("eafA", eafA);
//                mode.addAttribute("admA", admA);
//                mode.addAttribute("cdpA", cdpA);
//                mode.addAttribute("agent", agent);
//                mode.addAttribute("iwdA", iwdA);
//                mode.addAttribute("iftA", iftA);
//                mode.addAttribute("ipyA", ipyA);
//                mode.addAttribute("iccdpA", iccdpA);
//                mode.addAttribute("etxA", etxA);
//                mode.addAttribute("t2525A", t2525A);
//                mode.addAttribute("cpuA", cpuA);
//                mode.addAttribute("cavA", cavA);
//                mode.addAttribute("camA", camA);
//                mode.addAttribute("cajA", cajA);
//                mode.addAttribute("cauA", cauA);
//                mode.addAttribute("cwvA", cwvA);
//                mode.addAttribute("cwmA", cwmA);
//                mode.addAttribute("cafA", cafA);

                //原存
//                Boolean issue = DbHelper.toBoolean(dtSysstat.getSysstatIssue());
//                Boolean iwdF = DbHelper.toBoolean(dtSysstat.getSysstatIwdF());
//                Boolean iftF = DbHelper.toBoolean(dtSysstat.getSysstatIftF());
//                Boolean ipyF = DbHelper.toBoolean(dtSysstat.getSysstatIpyF());
//                Boolean iccdpF = DbHelper.toBoolean(dtSysstat.getSysstatIccdpF());
//                Boolean cdpF = DbHelper.toBoolean(dtSysstat.getSysstatCdpF());
//                Boolean etxF = DbHelper.toBoolean(dtSysstat.getSysstatEtxF());
//                Boolean t2525F = DbHelper.toBoolean(dtSysstat.getSysstat2525F());
//                Boolean cpuF = DbHelper.toBoolean(dtSysstat.getSysstatCpuF());
//                Boolean gpcadF = DbHelper.toBoolean(dtSysstat.getSysstatGpcadF());
//                Boolean cauF = DbHelper.toBoolean(dtSysstat.getSysstatCauF());
//                Boolean gpcwdF = DbHelper.toBoolean(dtSysstat.getSysstatGpcwdF());
                //2022/08/16 Bruce add
//                Boolean iiqF = DbHelper.toBoolean(dtSysstat.getSysstatIiqF());//餘額查詢
//                Boolean gpemvF = DbHelper.toBoolean(dtSysstat.getSysstatGpemvF());//EMV國際提款PLUS/CIRRUS
//                Boolean gpiwdF = DbHelper.toBoolean(dtSysstat.getSysstatGpiwdF());//晶片卡跨國提款
//                Boolean gpobF = DbHelper.toBoolean(dtSysstat.getSysstatGpobF());//跨境支付交易
//                Boolean vaaF = DbHelper.toBoolean(dtSysstat.getSysstatVaaF());//約定及核驗服務
//                mode.addAttribute("iiqF", iiqF);
//                mode.addAttribute("gpemvF", gpemvF);
//                mode.addAttribute("gpiwdF", gpiwdF);
//                mode.addAttribute("gpobF", gpobF);
//                mode.addAttribute("vaaF", vaaF);
//
//                mode.addAttribute("issue", issue);
//                mode.addAttribute("iwdF", iwdF);
//                mode.addAttribute("iftF", iftF);
//                mode.addAttribute("ipyF", ipyF);
//                mode.addAttribute("iccdpF", iccdpF);
//                mode.addAttribute("cdpF", cdpF);
//                mode.addAttribute("etxF", etxF);
//                mode.addAttribute("t2525F", t2525F);
//                mode.addAttribute("cpuF", cpuF);
//                mode.addAttribute("gpcadF", gpcadF);
//                mode.addAttribute("cauF", cauF);
//                mode.addAttribute("gpcwdF", gpcwdF);
                
                //2022/08/16 純代理
//                Boolean pure = DbHelper.toBoolean(dtSysstat.getSysstatPure());//純代理
//                Boolean iiqp = DbHelper.toBoolean(dtSysstat.getSysstatIiqP());//晶片卡餘額查詢
//                Boolean iccdpP = DbHelper.toBoolean(dtSysstat.getSysstatIccdpP());//全國繳費(晶片)
//                Boolean cdpP = DbHelper.toBoolean(dtSysstat.getSysstatCdpP());//全國繳費(ID+ACC)
//                Boolean ipyP = DbHelper.toBoolean(dtSysstat.getSysstatIpyP());//繳款-跨行(2531)
//                Boolean equP = DbHelper.toBoolean(dtSysstat.getSysstatEquP());//EMV 銀聯卡餘額查詢
//                Boolean eqpP = DbHelper.toBoolean(dtSysstat.getSysstatEqpP());//EMV PLUS餘額查詢
//                Boolean eqcP = DbHelper.toBoolean(dtSysstat.getSysstatEqcP());//EMV CIRRUS卡餘額查詢
//                mode.addAttribute("pure", pure);
//                mode.addAttribute("iiqp", iiqp);
//                mode.addAttribute("iccdpP", iccdpP);
//                mode.addAttribute("cdpP", cdpP);
//                mode.addAttribute("ipyP", ipyP);
//                mode.addAttribute("equP", equP);
//                mode.addAttribute("eqpP", eqpP);
//                mode.addAttribute("eqcP", eqcP);
                
                //其它通道
                Boolean cbs = DbHelper.toBoolean(dtSysstat.getSysstatCbs());
//                Boolean fedi = DbHelper.toBoolean(dtSysstat.getSysstatFedi());
//                Boolean nb = DbHelper.toBoolean(dtSysstat.getSysstatNb());
//                Boolean webatm = DbHelper.toBoolean(dtSysstat.getSysstatWebatm());
//                Boolean ascChannel = DbHelper.toBoolean(dtSysstat.getSysstatAscChannel());
//                Boolean asc = DbHelper.toBoolean(dtSysstat.getSysstatAsc());
//                Boolean ascmd = DbHelper.toBoolean(dtSysstat.getSysstatAscmd());
//                Boolean gcard = DbHelper.toBoolean(dtSysstat.getSysstatGcard());
//                Boolean sps = DbHelper.toBoolean(dtSysstat.getSysstatSps());
//                Boolean ascmac = DbHelper.toBoolean(dtSysstat.getSysstatAscmac());
//                Boolean spsmac = DbHelper.toBoolean(dtSysstat.getSysstatSpsmac());
//                //2022/08/16 Bruce add
//                Boolean pv = DbHelper.toBoolean(dtSysstat.getSysstatPv());//指靜脈
//                Boolean smtp = DbHelper.toBoolean(dtSysstat.getSysstatSmtp());//手機門號轉帳中台
//                mode.addAttribute("smtp", smtp);
//                mode.addAttribute("pv", pv);
                
                mode.addAttribute("cbs", cbs);
//                mode.addAttribute("fedi", fedi);
//                mode.addAttribute("nb", nb);
//                mode.addAttribute("webatm", webatm);
//                mode.addAttribute("ascChannel", ascChannel);
//                mode.addAttribute("asc", asc);
//                mode.addAttribute("ascmd", ascmd);
//                mode.addAttribute("gcard", gcard);
//                mode.addAttribute("sps", sps);
//                mode.addAttribute("ascmac", ascmac);
//                mode.addAttribute("spsmac", spsmac);
              //-----END-----
            }
            String fcrmstatAoctrm = "";
            String fcrmstatMbactrm = "";
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat Start
            //if(defFcrmstat!=null){
                //fcrmstatAoctrm = StringUtils.join(defFcrmstat.getFcrmstatAoctrm(),"-",getAoctName(defFcrmstat.getFcrmstatAoctrm()));
                //fcrmstatMbactrm = StringUtils.join(defFcrmstat.getFcrmstatMbactrm(),"-",getMbactName(defFcrmstat.getFcrmstatMbactrm()));
            //}
            //2022/09/12 Bruce modify 依照Candy需求取消搜尋Fcrmstat end
            mode.addAttribute("fcrmstatAoctrm", fcrmstatAoctrm);
            mode.addAttribute("fcrmstatMbactrm", fcrmstatMbactrm);
        } catch (Exception ex) {
        	//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 Start
			this.errorMessage(ex, ex.getMessage());
			this.showMessage(mode, MessageType.DANGER, programError);
            //this.showMessage(mode,MessageType.DANGER,ex.getMessage());
			//20220912 Bruce Modify 畫面顯示的錯誤訊息改成 程式出現錯誤！！請洽資訊人員 end
        }
    }

    /**
     *資料檢核
     * @return false or true
     */
    private Boolean checkData(String aPidCob, ModelMap mode){
        if (!("1000".equals(aPidCob) ||"1100".equals(aPidCob) || "1200".equals(aPidCob) ||
            "1300".equals(aPidCob) ||"1400".equals(aPidCob) || "1600".equals(aPidCob) ||
            "2000".equals(aPidCob) ||"2200".equals(aPidCob) || "2500".equals(aPidCob) ||
            "2510".equals(aPidCob) ||"2520".equals(aPidCob) || "2530".equals(aPidCob) ||
            "2540".equals(aPidCob) ||"2550".equals(aPidCob) || "2560".equals(aPidCob) ||
            "2570".equals(aPidCob) ||"2700".equals(aPidCob) || "7100".equals(aPidCob) ||
            "7300".equals(aPidCob))){
            this.showMessage(mode,MessageType.DANGER,WebCodeConstant.ApidError);
            return false;
        }
        return true;
    }
}

