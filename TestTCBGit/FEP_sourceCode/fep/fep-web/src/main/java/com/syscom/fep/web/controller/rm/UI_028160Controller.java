package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028160_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;


@Controller
public class UI_028160Controller extends BaseController {

    @Autowired
    RmService rmService;

    PageInfo<HashMap<String, Object>> dtResult = new PageInfo<>();

    public void pageOnLoad(ModelMap mode) {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        UI_028160_Form form = new UI_028160_Form();
        form.setResultGrdv("false");
        form.setResultGrdv2("false");
        form.setResultLoadCntText("false");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }



    @PostMapping(value = "/rm/UI_028160/queryClick")
    public String queryClick(@ModelAttribute UI_028160_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢UI_028160, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGridData(form,mode);
        return Router.UI_028160.getView();
    }

    @PostMapping(value = "/rm/UI_028160/selectChange")
    public String selectChange(@ModelAttribute UI_028160_Form form, ModelMap mode) throws Exception {
        form.setResultGrdv("false");
        form.setResultGrdv2("false");
        form.setResultLoadCntText("false");
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_028160.getView();
    }

    /**
     依查詢條件查詢的主程式。


     Bind 資料至 SyscomGridView 中

     */
    private void bindGridData(UI_028160_Form form, ModelMap mode) {
        try {
            dtResult = new PageInfo<>();
            dtResult.setList(new ArrayList<>());
            if ("1".equals(form.getKind()) || "2".equals(form.getKind())) {
                if (StringUtils.isBlank(form.getBrno())) {
                    this.showMessage(mode,MessageType.WARNING,"請輸入分行別");
                    return;
                }
            }
            form.setResultGrdv("false");
            form.setResultGrdv2("false");
            form.setResultLoadCntText("false");
            switch (form.getKind()) {
                case "1": //依分行別查詢匯出狀態
                    dtResult = rmService.getRMOUTTByTxdateBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), form.getBrno(),form.getPageNum(),form.getPageSize()); //RMOUTT_STAT 08=匯出退匯07=財金拒絕 99-系統問題
                    if (dtResult.getList().size() == 0) {
                        this.showMessage(mode,MessageType.WARNING,"匯出暫存檔" + QueryNoData);
                    } else {
                        form.setResultGrdv("true");
                    }
                    break;
                case "2": //依分行別查詢匯入狀態
                    dtResult = rmService.getRMINTByTxdateBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), form.getBrno(),form.getPageNum(),form.getPageSize()); //RMINT_STAT = 04,99 / *04=自動退匯, 99-系統問題 */
                    if (dtResult.getList().size() == 0) {
                        this.showMessage(mode,MessageType.WARNING,"匯入暫存檔" + QueryNoData);
                    } else {
                        form.setResultGrdv("true");
                    }
                    break;
                case "3": //查詢全行之匯出狀態
                    dtResult = rmService.getRMOUTTByTxdateBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), "",form.getPageNum(),form.getPageSize()); //RMOUTT_STAT 08=匯出退匯07=財金拒絕 99-系統問題
                    if (dtResult.getList().size() == 0) {
                        this.showMessage(mode,MessageType.WARNING,"匯出暫存檔" + QueryNoData);
                    } else {
                        form.setResultGrdv("true");
                    }
                    break;
                case "4": //查詢全行之匯入狀態
                    dtResult = rmService.getRMINTByTxdateBrno(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), "",form.getPageNum(),form.getPageSize()); //RMINT_STAT = 04,99 / *04=自動退匯, 99-系統問題 */
                    if (dtResult.getList().size() == 0) {
                        this.showMessage(mode,MessageType.WARNING,"匯入暫存檔" + QueryNoData);
                    } else {
                        form.setResultGrdv("true");
                    }
                    break;
                case "5":
                case "6":
                    Integer passedCnt = 0;
                    PageInfo<HashMap<String, Object>> passedDt = new PageInfo<>();

                    int transferingCnt = 0;
                    PageInfo<HashMap<String, Object>> transferingDt = new PageInfo<>();
                    if ("5".equals(form.getKind())) {
                        passedCnt = rmService.getRMOUTTTotalCntByStat("04");
                        //Jim, 2012/1/11, 下傳中筆數也要算進狀態=99的 (不調整table)
                        int tmpCnt = rmService.getRMOUTTTotalCntByStat("99");
                        passedCnt = passedCnt + tmpCnt;
                        String[] stats = {"04","99"};
                        passedDt = rmService.getRMOUTTSumByStatGroupByBrno(stats,form.getPageNum(),form.getPageSize());

                        String[] transferingDtStats = {"05"};
                        transferingCnt = rmService.getRMOUTTTotalCntByStat("05");
                        transferingDt = rmService.getRMOUTTSumByStatGroupByBrno(transferingDtStats,form.getPageNum(),form.getPageSize());
                    } else {
                        passedCnt = rmService.getRMINTotalCntByStat("99");
                        passedDt = rmService.getRMINSumByStatGroupByBrno("99",form.getPageNum(),form.getPageSize());

                        transferingCnt = rmService.getRMINTotalCntByStat("02");
                        transferingDt = rmService.getRMINSumByStatGroupByBrno("02",form.getPageNum(),form.getPageSize());
                    }
                    if (passedCnt >= 0) {
                        form.setUnDownloadCntText("未下傳總筆數 :  " + passedCnt);
                    }

                    if (transferingCnt >= 0) {
                        form.setDownloadingCntText("下傳中總筆數 :  " + transferingCnt);
                    }
                    form.setResultLoadCntText("true");
                    if (passedDt != null && transferingDt != null) {
                        int rowCnt = 0;
                        if (passedDt.getList().size() > transferingDt.getList().size()) {
                            rowCnt = passedDt.getList().size();
                        } else {
                            rowCnt = transferingDt.getList().size();
                        }
                        for (int i = 1; i <= rowCnt; i++) {
                            dtResult.getList().add(new HashMap<>());
                        }

                        if (rowCnt == 0) {
                            dtResult = null;
                        }
                    }

                    if (passedDt != null) {
                        for (int i = 0; i < passedDt.getList().size(); i++) {
                            if ("5".equals(form.getKind())) {
                                dtResult.getList().get(i).put("unDownloadBRNO",passedDt.getList().get(i).get("RMOUTT_BRNO"));
                            } else {
                                dtResult.getList().get(i).put("unDownloadBRNO",passedDt.getList().get(i).get("RECEIVER_BRNO"));
                            }
                            dtResult.getList().get(i).put("unDownloadCNT",passedDt.getList().get(i).get("CNT"));
                            dtResult.getList().get(i).put("unDownloadTXAMT",passedDt.getList().get(i).get("AMT"));
                        }
                    }

                    if (transferingDt != null) {
                        for (int i = 0; i < transferingDt.getList().size(); i++) {
                            if ("5".equals(form.getKind())) {
                                dtResult.getList().get(i).put("DownloadingBRNO",transferingDt.getList().get(i).get("RMOUTT_BRNO"));
                            } else {
                                dtResult.getList().get(i).put("DownloadingBRNO",transferingDt.getList().get(i).get("RECEIVER_BRNO"));
                            }
                            dtResult.getList().get(i).put("DownloadingCNT",transferingDt.getList().get(i).get("CNT"));
                            dtResult.getList().get(i).put("DownloadingTXAMT",transferingDt.getList().get(i).get("AMT"));
                        }
                    }

                    form.setResultGrdv2("true");
                    break;
            }

            if ("true".equals(form.getResultGrdv())) {
                if (dtResult.getList().size() > 0) {
                    for (int i = 0; i < dtResult.getList().size(); i++) {
                        String name = "";
                        RefString tempRef_name = new RefString(name);
                        getSTATName(dtResult.getList().get(i).get("STAT").toString(), tempRef_name, form.getKind());
                        name = tempRef_name.get();
                        dtResult.getList().get(i).put("O_STAT",dtResult.getList().get(i).get("STAT").toString() + "=" + name + " " + dtResult.getList().get(i).get("RC").toString());
                    }
                }

                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
            } else if ("true".equals(form.getResultGrdv2())) {

                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
            }

        } catch (Exception ex) {
            this.showMessage(mode,MessageType.DANGER,ex.toString());
        }
    }

    private void getSTATName(String stat, RefString name, String kind) {
        switch (stat) {
            //modified by Maxine for RMOUT STAT Maping Error
            //Case "04"
            //    name = "自動退匯"
            case "04":
                if (kind.equals("1") || kind.equals("3")) {
                    name.set("已放行");
                } else {
                    name.set("自動退匯");
                }
                break;
            case "05":
                name.set("傳送中");
                break;
            case "07":
                name.set("財金拒絕");
                break;
            case "08":
                name.set("匯出退匯");
                break;
            case "99":
                name.set("系統問題");
                break;
            default:
                name.set("");
                break;
        }
        return;
    }
}