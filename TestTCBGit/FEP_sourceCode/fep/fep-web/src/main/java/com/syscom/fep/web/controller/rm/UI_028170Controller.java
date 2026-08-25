package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.mybatis.model.Msgin;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028170_Form;
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
import java.util.List;


@Controller
public class UI_028170Controller extends BaseController {

    @Autowired
    RmService rmService;

    PageInfo dtResult = null;
    List<HashMap<String,Object>> dtMaintain = new ArrayList<>();

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028170_Form form = new UI_028170_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028170/queryClick")
    public String queryClick(@ModelAttribute UI_028170_Form form, ModelMap mode) throws Exception {
        this.infoMessage("查詢UI_028170, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        bindGridData(form,mode);
        return Router.UI_028170.getView();
    }

    /**
     依查詢條件查詢的主程式。


     Bind 資料至 SyscomGridView 中

     */
    private void bindGridData(UI_028170_Form form, ModelMap mode) {
        dtMaintain = new ArrayList<>();
        String rtnMsg = "";
        dtResult = null;
        try {
            RefString tempRef_rtnMsg = new RefString(rtnMsg);
            if (checkAllField(tempRef_rtnMsg,form.getKind(),form.getBrno())) {
                tempRef_rtnMsg.get();
                String TXDATE = null;
                Msgout defMSGOUT = new Msgout();
                Msgin defMSGIN = new Msgin();
                defMSGIN.setMsginStan(null);
                TXDATE = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

                switch (form.getKind()) {
                    case "1":
                        defMSGOUT.setMsgoutTxdate(TXDATE);
                        defMSGOUT.setMsgoutBrno(form.getBrno());
                        defMSGOUT.setMsgoutStat(MSGOUTStatus.Send);
                        dtResult = rmService.getMsgOutByDef(defMSGOUT,form.getPageNum(),form.getPageSize());
                        break;
                    case "2":
                        defMSGIN.setMsginTxdate(TXDATE);
                        defMSGIN.setMsginBrno(form.getBrno());
                        dtResult = rmService.getMsgInByDef(defMSGIN,form.getPageNum(),form.getPageSize());
                        break;
                    case "3":
                        defMSGOUT.setMsgoutTxdate(TXDATE);
                        defMSGOUT.setMsgoutStat(MSGOUTStatus.Send);
                        dtResult = rmService.getMsgOutByDef(defMSGOUT,form.getPageNum(),form.getPageSize());
                        break;
                    case "4":
                        defMSGIN.setMsginTxdate(TXDATE);
                        dtResult = rmService.getMsgInByDef(defMSGIN,form.getPageNum(),form.getPageSize());
                        break;
                }

                if (dtResult.getList().size() > 0) {
                    switch (form.getKind()) {
                        case "1":
                        case "3":
                            for (int i = 0; i < dtResult.getList().size(); i++) {
                                HashMap<String,Object> hashMap = new HashMap();
                                hashMap.put("SENDDATE",((Msgout) dtResult.getList().get(i)).getMsgoutSenddate());
                                hashMap.put("SENDTIME",((Msgout) dtResult.getList().get(i)).getMsgoutSendtime());
                                hashMap.put("SENDER_BANK",((Msgout) dtResult.getList().get(i)).getMsgoutSenderBank());
                                hashMap.put("RECEIVER_BANK",((Msgout) dtResult.getList().get(i)).getMsgoutReceiverBank());
                                hashMap.put("CHNMEMO",((Msgout) dtResult.getList().get(i)).getMsgoutChnmemo());
                                hashMap.put("ENGMEMO",((Msgout) dtResult.getList().get(i)).getMsgoutEngmemo());
                                hashMap.put("STAT",((Msgout) dtResult.getList().get(i)).getMsgoutStat());
                                dtMaintain.add(hashMap);
                            }
                            break;
                        case "2":
                        case "4":
                            for (int i = 0; i < dtResult.getList().size(); i++) {
                                HashMap<String,Object> hashMap = new HashMap();
                                hashMap.put("SENDDATE",((Msgin) dtResult.getList().get(i)).getMsginSenddate());
                                hashMap.put("SENDTIME",((Msgin) dtResult.getList().get(i)).getMsginSendtime());
                                hashMap.put("SENDER_BANK",((Msgin) dtResult.getList().get(i)).getMsginSenderBank());
                                hashMap.put("RECEIVER_BANK",((Msgin) dtResult.getList().get(i)).getMsginReceiverBank());
                                hashMap.put("CHNMEMO",((Msgin) dtResult.getList().get(i)).getMsginChnmemo());
                                hashMap.put("ENGMEMO",((Msgin) dtResult.getList().get(i)).getMsginEngmemo());
                                hashMap.put("STAT",((Msgin) dtResult.getList().get(i)).getMsginStat());
                                dtMaintain.add(hashMap);
                            }
                            break;
                    }
                }
                dtResult.setList(dtMaintain);
                WebUtil.putInAttribute(mode, AttributeName.PageData, dtResult);

                if (dtResult.getList().size() == 0) {
                    this.showMessage(mode, MessageType.DANGER, QueryNoData);
                }
            } else {
                rtnMsg = tempRef_rtnMsg.get();
                this.showMessage(mode, MessageType.DANGER, "請輸入" + rtnMsg);
            }

        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    public boolean checkAllField(RefString rtnMsg,String kind, String brno) {
        boolean rtn = true;
        switch (kind) {
            case "1":
            case "2":
                if (StringUtils.isBlank(brno)) {
                    rtnMsg.set(" 分行別");
                    rtn = false;
                }
                break;
        }
        return rtn;

    }
}