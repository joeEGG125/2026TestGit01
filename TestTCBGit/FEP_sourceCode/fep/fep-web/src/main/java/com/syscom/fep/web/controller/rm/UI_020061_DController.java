package com.syscom.fep.web.controller.rm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_020061_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 往來行庫資料維護
 * @author xingyun_yang
 * @create 2021/11/23
 */
@Controller
public class UI_020061_DController extends BaseController {
    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_020061_Form form = new UI_020061_Form();
        form.setAllbankBkno("");
        form.setAllbankBrno("");
        query(form,mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_020061_D/index")
    private String queryClick(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        query(form,mode);
        return Router.UI_020061_D.getView();
    }
    /**
     * 依查詢條件查詢的主程式。
     */
    private void query(UI_020061_Form form, ModelMap mode){
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            Allbank defAllbank = new Allbank();
            if (StringUtils.isNotBlank(form.getAllbankBkno().trim())){
                defAllbank.setAllbankBkno(form.getAllbankBkno().trim());
            }
            if (StringUtils.isNotBlank(form.getAllbankBrno().trim())){
                defAllbank.setAllbankBrno(form.getAllbankBrno().trim());
            }
            defAllbank.setAllbankSetCloseFlag("1");
            PageInfo<Allbank> pageInfo = rmService.getALLBANKByPKRmPageINfo(form.getPageNum(),form.getPageSize(),defAllbank);
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, "全國銀行檔無參加證券匯款之行庫資料");
                return;
            }
            PageData<UI_020061_Form, Allbank> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }
}
