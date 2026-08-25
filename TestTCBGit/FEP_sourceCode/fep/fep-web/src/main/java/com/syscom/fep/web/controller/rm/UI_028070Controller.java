package com.syscom.fep.web.controller.rm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.syscom.fep.frmcommon.esapi.ESAPIValidator;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ReflectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.ext.mapper.RmoutsnoExtMapper;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028070_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 各行庫換KEY狀態查詢
 *
 * @author Chen_yu
 * @create 2021/11/30
 */
@Controller
public class UI_028070Controller extends BaseController {
    @Autowired
    RmService rmService;
    @Autowired
    RmoutsnoExtMapper rmoutsnoExtMapper;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028070_Form form = new UI_028070_Form();
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        querySYSSTAT(mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    //add by maxine on 2011/06/24 for SYSSTAT自行查
    private void querySYSSTAT(ModelMap mode) {
        try {
            List<Sysstat> _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料!!");
                return;
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    @PostMapping(value = "/rm/UI_028070/queryClick")
    public String queryClick(@ModelAttribute UI_028070_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢主檔數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        // 2024-11-18 Richard modified for 【Unchecked Input for Loop Condition】
        // bindGridData(form, mode);
        ReflectUtil.envokeMethod(this, "bindGridData",
                new Class[] {UI_028070_Form.class, ModelMap.class},
                new Object[] {form, mode});
        return Router.UI_028070.getView();
    }

    private void bindGridData(UI_028070_Form form, ModelMap mode) {
        PageInfo<HashMap<String, Object>> pageInfo = null;
        try {
            String hbkno = SysStatus.getPropertyValue().getSysstatHbkno();
            // 2024-11-06 Richard modified for 【Unchecked Input for Loop Condition】
            Integer pageNum = ESAPIValidator.getValidInteger(Integer.toString(form.getPageNum()), 1, Integer.MAX_VALUE, true, 1);
            Integer pageSize = ESAPIValidator.getValidInteger(Integer.toString(form.getPageNum()), 1, Integer.MAX_VALUE, true, 20);
            if ("0".equals(form.getBknoRfv())) {
                pageInfo = rmService.getRMOUTSNO(hbkno, "950", false, pageNum, pageSize);
            } else if ("".equals(form.getBknoRfv())) {
                pageInfo = rmService.getRMOUTSNO(hbkno, "", false, pageNum, pageSize);
            } else {
                pageInfo = rmService.getRMOUTSNO(hbkno, form.getBknoRfv(), true, pageNum, pageSize);
            }


            PageData<UI_028070_Form, HashMap<String, Object>> pageData = new PageData<>(pageInfo, form);
            Allbank defALLBANK = new Allbank();
            String no = "";
            List<HashMap<String, Object>> dtMaintain = new ArrayList<>(pageData.getList().size());
            //加上兩個需要送給User的欄位
            for (int i = 0; i < pageInfo.getList().size(); i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("BKNAME", "");
                hashMap.put("CHGK_TIMES2", "");
                hashMap.put("RMOUTSNO_RECEIVER_BANK", pageInfo.getList().get(i).get("RMOUTSNO_RECEIVER_BANK").toString());
                hashMap.put("RMOUTSNO_CDKEY_FLAG", pageInfo.getList().get(i).get("RMOUTSNO_CDKEY_FLAG").toString());
                hashMap.put("RMOUTSNO_CHGK", pageInfo.getList().get(i).get("RMOUTSNO_CHGK").toString());
                hashMap.put("RMOUTSNO_CHGK_TIMES", pageInfo.getList().get(i).get("RMOUTSNO_CHGK_TIMES").toString());

                if (pageInfo.getList().get(i).get("RMOUTSNO_RECEIVER_BANK") != null && !"".equals(pageInfo.getList().get(i).get("RMOUTSNO_RECEIVER_BANK"))) {
                    no = pageInfo.getList().get(i).get("RMOUTSNO_RECEIVER_BANK").toString().trim() + "000";
                    defALLBANK.setAllbankBkno(no.substring(0, 3));
                    defALLBANK.setAllbankBrno(no.substring(3));
                    List<Allbank> m = rmService.getALLBANKbyPKOne(defALLBANK);
                    if (m.size() > 0) {
                        hashMap.put("BKNAME", m.get(0).getAllbankAliasname());
                    }

                    //modified by maxine on 2011/06/24 for SYSSTAT自行查
                    Rminsno defRMINSNO = new Rminsno();
                    defRMINSNO.setRminsnoSenderBank(pageInfo.getList().get(i).get("RMOUTSNO_RECEIVER_BANK").toString());
                    defRMINSNO.setRminsnoReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
                    List<Rminsno> n = rmService.getRMINSNOByPKOne(defRMINSNO);
                    if (n.size() < 1) {
                        this.showMessage(mode, MessageType.WARNING, "匯入通匯序號檔無此資料");
                    } else {
//                        dtResult.get(i).setChgkTimes2(defRMINSNO.getRminsnoChgkTimes());
                        hashMap.put("CHGK_TIMES2", n.get(0).getRminsnoChgkTimes());
                        pageInfo.getList().get(i).get("RMOUTSNO_CDKEY_FLAG").equals(n.get(0).getRminsnoCdkeyFlag());
                    }
                    if (dtMaintain != null) {
                        dtMaintain.add(hashMap);
                    }
                }
            } ;

            if (dtMaintain == null || dtMaintain.size() == 0) {
                this.showMessage(mode, MessageType.WARNING, QueryNoData);
            }
//            PageInfo<HashMap<String, Object>> pageInfo1 = new PageInfo<>();
//
//            PageData<UI_028070_Form, HashMap<String, Object>> pageData1 = new PageData<>(pageInfo1, form);
            pageData.setList(dtMaintain);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.WARNING, ex.getMessage());
        }
    }
}
