package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.vo.constant.RMINStatus;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028090_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Calendar;
import java.util.HashMap;

/**
 * 往來行庫資料查詢
 *
 * @author chen_yu
 * @create 2021/12/06
 */

@Controller
public class UI_028090Controller extends BaseController {
    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028090_Form form = new UI_028090_Form();
        form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028090/queryClick")
    private String queryClick(@ModelAttribute UI_028090_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);

        query(form,mode);
        return Router.UI_028090.getView();
    }

    //依查詢條件查詢的主程式。
    private void query(UI_028090_Form form, ModelMap mode){
        PageInfo<HashMap<String, Object>> dtResult = null;
        try {
            String[] tmpStrs = null;
            String inputDate = "";
//            tmpStrs = String[form.getTradingDate().replace("-","")];
            tmpStrs = form.getTradingDate().split("-");
            inputDate = tmpStrs[0] + StringUtils.leftPad(tmpStrs[1],2,"0") + StringUtils.leftPad(tmpStrs[2],2,"0");
            if ("1".equals(form.getKind())) {  //一般匯款
                //Modify by Jim, 2011/12/21, 由RMIN_PENDING="P"改成RMIN_STAT=99
                String finalInputDate = inputDate;
//                dtResult = rmService.getRMINByDateSendbankPendingEJ(finalInputDate,form.getSenderBank(), RMINStatus.Transferring,form.getEjfno(),form.getPageNum(),form.getPageSize());
                dtResult = rmService.getRMINByDateSendbankPendingEJ(finalInputDate,form.getSenderBank(),RMINStatus.Transferring,form.getEjfno(),form.getPageNum(),form.getPageSize());
                if(dtResult.getList().size() == 0){
                    this.showMessage(mode, MessageType.WARNING, "匯入主檔無此資料");
                    return;
                }
                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
            }else if ("4".equals(form.getKind())) { //一般通訊
                String finalInputDate1 = inputDate;
                dtResult = rmService.getMSGINByDateSendbankFISCRtnCodeEJ(finalInputDate1,form.getSenderBank(),"0001",form.getEjfno(),form.getPageNum(),form.getPageSize());

                if(dtResult.getList().size() == 0){
                    this.showMessage(mode, MessageType.WARNING, "一般通訊匯入主檔無此資料");
                    return;
                }
                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
            } else if ("0".equals(form.getKind())) {
                //UNION  UI.KIND  = 1, 4之內容
                String finalInputDate2 = inputDate;
                dtResult = rmService.getRMINUnionMSGIN(finalInputDate2,form.getSenderBank(),RMINStatus.Transferring,"0001",form.getEjfno(),form.getPageNum(),form.getPageSize());
                int tempVar = dtResult.getList().size();
                for (int i=0; i<tempVar; i++ ) {
                    switch (dtResult.getList().get(i).get("REMTYPE").toString().substring(0,2)){
                        case "11":
                            dtResult.getList().get(i).put("REMTYPE","入戶");
                            break;
                        case "12":
                            dtResult.getList().get(i).put("REMTYPE","同業");
                            break;
                        case "13":
                            dtResult.getList().get(i).put("REMTYPE","國庫");
                            break;
                        case "17":
                            dtResult.getList().get(i).put("REMTYPE","退匯");
                            break;
                        case "18":
                            dtResult.getList().get(i).put("REMTYPE","證券");
                            break;
                        case "19":
                            dtResult.getList().get(i).put("REMTYPE","票券");
                            break;
                        default:
                            dtResult.getList().get(i).put("REMTYPE"," ");
                            break;
                    }
                }
                if(dtResult.getList().size() == 0){
                    this.showMessage(mode, MessageType.WARNING, "匯入主檔和一般通訊匯入主檔皆無資料");
                    return;
                }
                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);
            }
        }catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER,ex.toString());
            }
    }

//    @SuppressWarnings("unused")
//    private void getRminData(UI_028090_Form form, ModelMap mode) throws Exception {
//        String txdate = form.getTradingDate().replace("-","");
//        PageInfo<HashMap<String, Object>> dtResult1 = null;
//        dtResult1 = rmService.getRMINByDateSendbankPendingEJ(txdate,form.getSenderBank(),"P ",form.getEjfno(),form.getPageNum(),form.getPageSize()); //P=尚未傳送CBS
//
//        if (dtResult1.getList().size() == 0) {
//            this.showMessage(mode,MessageType.WARNING,"匯入主檔無此資料");
//            return;
//        }else {
//
//        }
//    }
}
