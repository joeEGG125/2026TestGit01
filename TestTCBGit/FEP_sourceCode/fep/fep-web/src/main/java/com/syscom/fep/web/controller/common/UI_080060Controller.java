package com.syscom.fep.web.controller.common;

import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.common.UI_080060_Form;
import com.syscom.fep.web.service.CommonService;
import com.syscom.fep.web.util.WebUtil;
import com.syscom.safeaa.common.SafeaaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * For Safeaa
 *
 * @author ChenYang
 */
@Controller
public class UI_080060Controller extends BaseController {

    @Autowired
    private CommonService commonService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_080060_Form form = new UI_080060_Form();
        form.setLogonId(WebUtil.getUser().getLoginId());
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 確認按鈕
     */
    @PostMapping(value = "/common/UI_080060/btnConfirm")
    @ResponseBody
    public UI_080060_Form btnConfirm(@RequestBody UI_080060_Form form) {
        this.infoMessage("開始執行, 條件 = [", form.toString(), "]");
        try {
            commonService.changPassword(Integer.parseInt(WebUtil.getUser().getUserId()),form.getLogonId(),form.getOldSscod(),form.getNewSscod(), WebUtil.getUser().getSrcIp());
            form.setMessage(MessageType.SUCCESS,UpdateSuccess);
        } catch (SafeaaException ex) {
            form.setMessage(MessageType.INFO,ex.getMessage());
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,UpdateFail);
        }
        return form;
    }
}
