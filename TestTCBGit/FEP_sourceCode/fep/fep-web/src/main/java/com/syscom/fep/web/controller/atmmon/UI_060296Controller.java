package com.syscom.fep.web.controller.atmmon;

import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.MsgctlExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TxtypeExtMapper;
import com.syscom.fep.mybatis.model.Msgctl;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.atmmon.UI_060291_Form_TreeData;
import com.syscom.fep.web.form.atmmon.UI_060296_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FEP 服務/通路/線路查詢
 *
 * @author Alma
 */
@Controller
public class UI_060296Controller extends BaseController {

    @Autowired
    private InbkService inbkService;

    private MsgctlExtMapper msgctlExtMapper = SpringBeanFactoryUtil.getBean(MsgctlExtMapper.class);
    private TxtypeExtMapper txtypeExtMapper = SpringBeanFactoryUtil.getBean(TxtypeExtMapper.class);

    private Sysstat sysStat = null;

    @Override
    public void pageOnLoad(ModelMap mode) {
        this.showMessage(mode, MessageType.INFO, "");
        UI_060296_Form form = new UI_060296_Form();
        // '進行查詢
        this.bindFormViewData(form, mode);
        // 查詢 SYSSTAT 表中各系統與財金的連線狀態
        this.tabPanel2BindData(this.sysStat, form);
        // 取得系統目前狀態
        this.tabPanel3GetSYSSTAT(this.sysStat, form, mode);
    }

    /**
     * 資料整理
     *
     * @return
     */
    private void bindFormViewData(UI_060296_Form form, ModelMap mode) {
        try {
            this.sysStat = inbkService.getStatus();
            if (this.sysStat == null) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            } else {
                // 取得基本資料
                this.tabPanel1BaseInfo(this.sysStat, form);
                this.showMessage(mode, MessageType.INFO, QuerySuccess);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 取得基本資料
     *
     * @param sysStat
     * @param form
     */
    private void tabPanel1BaseInfo(Sysstat sysStat, UI_060296_Form form) {
        form.setSysstatHbkno(sysStat.getSysstatHbkno()); // 銀行代號
        form.setSysstatFbkno(sysStat.getSysstatFbkno()); // 財金公司銀行代號
        form.setSysstatScbkno(sysStat.getSysstatScbkno()); // 信用卡虛擬代號
        form.setSysstatLbsdyFisc(sysStat.getSysstatLbsdyFisc()); // 財金上營業日
        form.setSysstatTbsdyFisc(sysStat.getSysstatTbsdyFisc()); // 財金本營業日
        form.setSysstatNbsdyFisc(sysStat.getSysstatNbsdyFisc()); // 財金下營業日
        form.setSysstatFopcsync(sysStat.getSysstatFopcsync()); // OPC FROM FISC 同步基碼
        form.setSysstatTopcsync(sysStat.getSysstatTopcsync()); // OPC TO FISC 同步基碼
        form.setSysstatFcdsync(sysStat.getSysstatFcdsync()); // CD/ATM FROM FISC 同步基碼
        form.setSysstatTcdsync(sysStat.getSysstatTcdsync()); // CD/ATM TO FISC 同步基碼
        form.setSysstatFrmsync(sysStat.getSysstatFrmsync()); // 通匯 FROM FISC 同步基碼
        form.setSysstatTrmsync(sysStat.getSysstatTrmsync()); // 通匯 TO FISC 同步基碼
        form.setSysstatFppsync(sysStat.getSysstatFppsync()); // PIN PROTECTON FROM FISC
        form.setSysstatTppsync(sysStat.getSysstatTppsync()); // PIN PROTECTON TO FISC
        form.setSysstatF3dessync(sysStat.getSysstatF3dessync()); // TRIPLE DES PIN PROTECTION KEY FROM FISC
        form.setSysstatT3dessync(sysStat.getSysstatT3dessync()); // TRIPLE DES PIN PROTECTION KEY TO FISC
        form.setSysstatOpckeyst(this.getKeystName(sysStat.getSysstatOpckeyst())); // OPC KEY STATUS
        form.setSysstatAtmkeyst(this.getKeystName(sysStat.getSysstatAtmkeyst())); // ATM KEY STATUS
        form.setSysstatRmkeyst(this.getKeystName(sysStat.getSysstatRmkeyst())); // RM KEY STATUS
        form.setSysstatPpkeyst(this.getKeystName(sysStat.getSysstatPpkeyst())); // PP KEY STATUS
        form.setSysstat3deskeyst(this.getKeystName(sysStat.getSysstat3enckeyst())); // 3DES KEY STATUS
    }

    /**
     * 查詢 SYSSTAT 表中各系統與財金的連線狀態
     *
     * @param sysStat
     * @param form
     */
    private void tabPanel2BindData(Sysstat sysStat, UI_060296_Form form) {
        if (sysStat != null) {
            form.setSysstatSoct(sysStat.getSysstatSoct() + "-" + this.getSoctName(sysStat.getSysstatSoct()));
            form.setSysstatMboct(sysStat.getSysstatMboct() + "-" + this.getMboctName(sysStat.getSysstatMboct()));
            form.setSysstatAoct1000(sysStat.getSysstatAoct1000() + "-" + this.getAoctName(sysStat.getSysstatAoct1000()));
            form.setSysstatAoct1100(sysStat.getSysstatAoct1100() + "-" + this.getAoctName(sysStat.getSysstatAoct1100()));
            form.setSysstatAoct1200(sysStat.getSysstatAoct1200() + "-" + this.getAoctName(sysStat.getSysstatAoct1200()));
            form.setSysstatAoct1300(sysStat.getSysstatAoct1300() + "-" + this.getAoctName(sysStat.getSysstatAoct1300()));
            form.setSysstatAoct1400(sysStat.getSysstatAoct1400() + "-" + this.getAoctName(sysStat.getSysstatAoct1400()));
            form.setSysstatAoct2000(sysStat.getSysstatAoct2000() + "-" + this.getAoctName(sysStat.getSysstatAoct2000()));
            form.setSysstatAoct2200(sysStat.getSysstatAoct2200() + "-" + this.getAoctName(sysStat.getSysstatAoct2200()));
            form.setSysstatAoct2200(sysStat.getSysstatAoct2200() + "-" + this.getAoctName(sysStat.getSysstatAoct2200()));
            form.setSysstatAoct2500(sysStat.getSysstatAoct2500() + "-" + this.getAoctName(sysStat.getSysstatAoct2500()));
            form.setSysstatAoct2510(sysStat.getSysstatAoct2510() + "-" + this.getAoctName(sysStat.getSysstatAoct2510()));
            form.setSysstatAoct2520(sysStat.getSysstatAoct2520() + "-" + this.getAoctName(sysStat.getSysstatAoct2520()));
            form.setSysstatAoct2530(sysStat.getSysstatAoct2530() + "-" + this.getAoctName(sysStat.getSysstatAoct2530()));
            form.setSysstatAoct2540(sysStat.getSysstatAoct2540() + "-" + this.getAoctName(sysStat.getSysstatAoct2540()));
            form.setSysstatAoct2550(sysStat.getSysstatAoct2550() + "-" + this.getAoctName(sysStat.getSysstatAoct2550()));
            form.setSysstatAoct2560(sysStat.getSysstatAoct2560() + "-" + this.getAoctName(sysStat.getSysstatAoct2560()));
            form.setSysstatAoct2570(sysStat.getSysstatAoct2570() + "-" + this.getAoctName(sysStat.getSysstatAoct2570()));
            form.setSysstatAoct2700(sysStat.getSysstatAoct2700() + "-" + this.getAoctName(sysStat.getSysstatAoct2700())); //2026/3/4 新增for 資金調撥(2700)
            form.setSysstatAoct7100(sysStat.getSysstatAoct7100() + "-" + this.getAoctName(sysStat.getSysstatAoct7100()));
            // 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
            form.setSysstatAoct7300(sysStat.getSysstatAoct7300() + "-" + this.getAoctName(sysStat.getSysstatAoct7300()));
            form.setSysstatMbact1000txt(sysStat.getSysstatMbact1000());
            form.setSysstatMbact1100txt(sysStat.getSysstatMbact1100());
            form.setSysstatMbact1200txt(sysStat.getSysstatMbact1200());
            form.setSysstatMbact1300txt(sysStat.getSysstatMbact1300());
            form.setSysstatMbact1400txt(sysStat.getSysstatMbact1400());
            form.setSysstatMbact2000txt(sysStat.getSysstatMbact2000());
            form.setSysstatMbact2200txt(sysStat.getSysstatMbact2200());
            form.setSysstatMbact2200txt(sysStat.getSysstatMbact2200());
            form.setSysstatMbact2500txt(sysStat.getSysstatMbact2500());
            form.setSysstatMbact2510txt(sysStat.getSysstatMbact2510());
            form.setSysstatMbact2520txt(sysStat.getSysstatMbact2520());
            form.setSysstatMbact2530txt(sysStat.getSysstatMbact2530());
            form.setSysstatMbact2540txt(sysStat.getSysstatMbact2540());
            form.setSysstatMbact2550txt(sysStat.getSysstatMbact2550());
            form.setSysstatMbact2560txt(sysStat.getSysstatMbact2560());
            form.setSysstatMbact2570txt(sysStat.getSysstatMbact2570());
            form.setSysstatMbact2700txt(sysStat.getSysstatMbact2700()); //2026/3/4 新增for 資金調撥(2700)
            form.setSysstatMbact7100txt(sysStat.getSysstatMbact7100());
            // 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
            form.setSysstatMbact7300txt(sysStat.getSysstatMbact7300());
            form.setSysstatMbact1000(this.getMbactName(sysStat.getSysstatMbact1000()));
            form.setSysstatMbact1100(this.getMbactName(sysStat.getSysstatMbact1100()));
            form.setSysstatMbact1200(this.getMbactName(sysStat.getSysstatMbact1200()));
            form.setSysstatMbact1300(this.getMbactName(sysStat.getSysstatMbact1300()));
            form.setSysstatMbact1400(this.getMbactName(sysStat.getSysstatMbact1400()));
            form.setSysstatMbact2000(this.getMbactName(sysStat.getSysstatMbact2000()));
            form.setSysstatMbact2200(this.getMbactName(sysStat.getSysstatMbact2200()));
            form.setSysstatMbact2200(this.getMbactName(sysStat.getSysstatMbact2200()));
            form.setSysstatMbact2500(this.getMbactName(sysStat.getSysstatMbact2500()));
            form.setSysstatMbact2510(this.getMbactName(sysStat.getSysstatMbact2510()));
            form.setSysstatMbact2520(this.getMbactName(sysStat.getSysstatMbact2520()));
            form.setSysstatMbact2530(this.getMbactName(sysStat.getSysstatMbact2530()));
            form.setSysstatMbact2540(this.getMbactName(sysStat.getSysstatMbact2540()));
            form.setSysstatMbact2550(this.getMbactName(sysStat.getSysstatMbact2550()));
            form.setSysstatMbact2560(this.getMbactName(sysStat.getSysstatMbact2560()));
            form.setSysstatMbact2570(this.getMbactName(sysStat.getSysstatMbact2570()));
            form.setSysstatMbact2700(this.getMbactName(sysStat.getSysstatMbact2700())); //2026/3/4 新增for 資金調撥(2700)
            form.setSysstatMbact7100(this.getMbactName(sysStat.getSysstatMbact7100()));
            // 'ChenLi, 2012/10/25, 增加跨行付款交易顯示欄位
            form.setSysstatMbact7300(this.getMbactName(sysStat.getSysstatMbact7300()));
        }
        // 2023/3/29  db 中無此table Fcrmstat
        //		Fcrmstat fcrmstat = new Fcrmstat();
        //		fcrmstat.setFcrmstatCurrency(FCRMSTAT_CURRENCY);
        //		List<Fcrmstat> fcrmstatList = fcrmstatExtMapper.queryByPrimaryKey(fcrmstat);
        //		if(fcrmstatList.size() > 0) {
        //			form.setFcrmstatAoct1600(fcrmstatList.get(0).getFcrmstatAoctrm() + "-" + this.getAoctName(fcrmstatList.get(0).getFcrmstatAoctrm()));
        //			// '2020/10/05 modified by Carrie for Fortify修正:Cross-Site Scripting: Reflected
        //			form.setFcrmstatMbact1600txt(fcrmstatList.get(0).getFcrmstatMbactrm());
        //			form.setFcrmstatMbact1600(this.getMbactName(fcrmstatList.get(0).getFcrmstatMbactrm()));
        //		}
    }

    /**
     * 取得系統目前狀態
     *
     * @param sysStat
     * @param form
     */
    private String tabPanel3GetSYSSTAT(Sysstat sysStat, UI_060296_Form form, ModelMap mode) {
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        if (sysStat == null) {
            return Router.UI_060296.getView();
        }
        return Router.UI_060296.getView();
    }


    @PostMapping(value = "/atmmon/UI_060296/select")
    @ResponseBody
    public UI_060296_Form initData(@RequestBody UI_060296_Form form, ModelMap mode){
        // 記錄auditLog
        AuditLog auditLog = new AuditLog();
        // 塞入auditLog.action
        auditLog.setAction("查詢本行系統狀態");
		// 最後塞入ThreadLocal變量中
        setAuditLog(auditLog);
        getTxtype(form);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return form;
    }


    private void getTxtype(UI_060296_Form form) {

        List<UI_060291_Form_TreeData> level1 = new ArrayList<>();
        List<Map<String, String>> _1List = txtypeExtMapper.getLevel1TxType();
        if (!CollectionUtils.isEmpty(_1List)) {
            for (Map<String, String> map : _1List) {
                UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
                data.setTxType1(Short.parseShort(map.get("TXTYPE_TYPE1")));
                data.setTxType1Name(map.get("TXTYPE_TYPE1_NAME"));
                data.setTreeLevel(1);
                level1.add(data);
            }
        }

        List<UI_060291_Form_TreeData> level2 = new ArrayList<>();
        List<Map<String, String>> _2List = txtypeExtMapper.getLevel2();
        if (!CollectionUtils.isEmpty(_2List)) {
            for (Map<String, String> map : _2List) {
                UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
                data.setTxType1(Short.parseShort(map.get("TXTYPE_TYPE1")));
                data.setTxType1Name(map.get("TXTYPE_TYPE1_NAME"));
                data.setTxType2(Short.parseShort(map.get("TXTYPE_TYPE2")));
                data.setTxType2Name(map.get("TXTYPE_TYPE2_NAME"));
                data.setTxtype(txtypeExtMapper.selectByPrimaryKey(String.valueOf(data.getTxType1()), String.valueOf(data.getTxType2())));
                data.setMsgctl(msgctlExtMapper.selectByTxType(data.getTxType1(), data.getTxType2()));
                data.setTreeLevel(2);
                level2.add(data);
            }
        }

        List<UI_060291_Form_TreeData> level3 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(level2)) {
            for (UI_060291_Form_TreeData treeData : level2) {
                if (!CollectionUtils.isEmpty(treeData.getMsgctl())) {
                    for (Msgctl msgctl : treeData.getMsgctl()) {
                        UI_060291_Form_TreeData data = new UI_060291_Form_TreeData();
                        List<Msgctl> msgctls = new ArrayList<>();
                        msgctls.add(msgctl);
                        data.setMsgctl(msgctls);
                        data.setTxType1(treeData.getTxType1());
                        data.setTxType1Name(treeData.getTxType1Name());
                        data.setTxType2(treeData.getTxType2());
                        data.setTxType2Name(treeData.getTxType2Name());
                        data.setTreeLevel(3);
                        data.setChecked(DbHelper.toBoolean(msgctl.getMsgctlStatus()));
                        if (data.isChecked()) {
                            level2.stream().filter(r -> {
                                if (r.getTxType1() == data.getTxType1()
                                        && r.getTxType2() == data.getTxType2()) {
                                    return true;
                                }
                                return false;
                            }).findFirst().get().setChecked(true);
                            level1.stream().filter(r -> r.getTxType1().equals(data.getTxType1())).findFirst().get().setChecked(true);

                        }
                        level3.add(data);
                    }
                }
            }
        }

        form.setTreeData(level1);
        form.getTreeData().addAll(level2);
        form.getTreeData().addAll(level3);

    }
}
