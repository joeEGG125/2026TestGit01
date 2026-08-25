package com.syscom.fep.web.controller.rm;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.mybatis.model.Allbank;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.server.common.business.rm.RM;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_020061_B_FormDetail;
import com.syscom.fep.web.form.rm.UI_020061_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 往來行庫資料維護
 * 負責處理UI020061全國銀行檔維護作業，提供全國銀行檔(ALLBANK)新增、修改、刪除、查詢功能。
 * 此畫面為單檔維護畫面，取得資料的方式以SAFE DAL為主
 * @author xingyun_yang
 * @create 2021/11/23
 */
@Controller
public class UI_020061_BController extends BaseController {
    @Autowired
    RmService rmService;
    private String exceptionMsg = "UI_020061_B, 往來行庫資料維護發生例外";
    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_020061_Form form = new UI_020061_Form();
        form.setAllbankBkno("");
        form.setAllbankBrno("");
        query(form,mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_020061_B/index")
    private String queryClick(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        query(form,mode);
        return Router.UI_020061_B.getView();
    }

    @PostMapping(value = "/rm/UI_020060/index")
    private String queryClick_UI020060(@ModelAttribute UI_020061_Form form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        query(form,mode);
        return Router.UI_020061_B.getView();
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
            PageInfo<Allbank> pageInfo = PageHelper.startPage(form.getPageNum(), form.getPageSize(), form.getPageNum() > 0 && form.getPageSize() > 0).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                        rmService.queryALLBANKByPKLike(defAllbank);
                }
            });
            if(pageInfo.getSize() == 0){
                this.showMessage(mode, MessageType.INFO, QueryNoData);
                return;
            }
            PageData<UI_020061_Form, Allbank> pageData = new PageData<>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    @PostMapping(value = "/rm/UI_020061_B/showDetial")
    private String showDetial(@ModelAttribute UI_020061_B_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        form.setBtnType(nullToEmptyStr(form.getBtnType()));
        form.setAllbankBkno(nullToEmptyStr(form.getAllbankBkno()));
        form.setAllbankBrno(nullToEmptyStr(form.getAllbankBrno()));
        form.setAllbankBrnoChkcode(nullToEmptyStr(form.getAllbankBrnoChkcode()));
        form.setAllbankType(nullToEmptyStr(form.getAllbankType()));
        form.setAllbankFiscUnit(nullToEmptyStr(form.getAllbankFiscUnit()));
        form.setAllbankUnitBank(nullToEmptyStr(form.getAllbankUnitBank()));
        form.setAllbankAliasname(nullToEmptyStr(form.getAllbankAliasname()).trim());
        form.setAllbankFullname(nullToEmptyStr(form.getAllbankFullname()).trim());
        form.setAllbankEngname(nullToEmptyStr(form.getAllbankEngname()));
        form.setAllbankAddrress(nullToEmptyStr(form.getAllbankAddrress()));
        form.setAllbankZipcode1(nullToEmptyStr(form.getAllbankZipcode1()));
        form.setAllbankZipcode2(nullToEmptyStr(form.getAllbankZipcode2()));
        form.setAllbankTelno(nullToEmptyStr(form.getAllbankTelno()));
        form.setAllbankRmflag(nullToEmptyStr(form.getAllbankRmflag()));
        form.setAllbankRmforward(nullToEmptyStr(form.getAllbankRmforward()));
        form.setAllbankSetCloseTime(nullToEmptyStr(form.getAllbankSetCloseTime()));
        form.setAllbankSetCloseFlag(nullToEmptyStr(form.getAllbankSetCloseFlag()));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_020061_B_Detail.getView();
    }

    @RequestMapping(value = "/rm/UI_020061_B/btnDelete")
    @ResponseBody
    private BaseResp<UI_020061_Form> btnDelete(@RequestBody UI_020061_Form dForm) throws Exception {
        BaseResp<UI_020061_Form> response = new BaseResp<>();
        Allbank defAllbank = new Allbank();
        defAllbank.setAllbankBkno(dForm.getAllbankBkno());
        defAllbank.setAllbankBrno(dForm.getAllbankBrno());
        defAllbank.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
        rmService.deleteALLBANK(defAllbank);
        response.setData(dForm);
        return response;
    }

    /**
     * 明細頁變更儲存
     */
    @PostMapping(value = "/rm/UI_020061_B/btnChange")
    private String btnChange(@ModelAttribute UI_020061_B_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細數據, 條件 = [", form.toString(), "]");
        // this.doKeepFormData(mode, form);
        Allbank defAllbank = new Allbank();
        RefBase<Allbank> refAllbank = new RefBase<>(defAllbank);
        Integer iRes;
        try {
            if (checkAllField(form,refAllbank,mode)){
                defAllbank = refAllbank.get();
                if ("insert".equals(form.getBtnType())){
                    Allbank getAllbank = new Allbank();
                    getAllbank.setAllbankBrno(defAllbank.getAllbankBrno());
                    List<Allbank> allbankList = rmService.getALLBANKByPKLike(getAllbank);
                    if (allbankList!=null){
                        this.showMessage(mode,MessageType.WARNING,"往來行庫資料"+InsertFail+"財金銀行代碼_分支機構重複,請更換");
                        WebUtil.putInAttribute(mode, AttributeName.Form, form);
                        return Router.UI_020061_B_Detail.getView();
                    }
                    iRes = rmService.insertAllBank(defAllbank);
                    if (iRes==1){
                        //Jim, 2011/12/12, 新增成功後需要檢查RMOUTSNO和RMINSNO是否有對應的資料，沒有的話要insert
                        checkRMSNO(defAllbank);

                        // 新增成功後,將Gridview帶到那一頁並選取該筆
                        //LookupNewRowInGrid(pks)
                        //連續新增模式,清除單筆表單控制項內容
                        form = new UI_020061_B_FormDetail();
                        form.setBtnType("insert");
                        this.showMessage(mode,MessageType.INFO,"往來行庫資料"+InsertSuccess);
                    }else {
                        this.showMessage(mode,MessageType.WARNING,"往來行庫資料"+InsertFail);
                    }
                }else {
                    iRes = rmService.updateALLBANK(defAllbank);
                    if (iRes == 1){
                       //修改成功後,將Gridview帶到那一頁並選取該筆
                       //LookupNewRowInGrid(pks)
                        this.showMessage(mode,MessageType.INFO,"往來行庫資料"+UpdateSuccess);
                    }else {
                        this.showMessage(mode,MessageType.WARNING,"往來行庫資料"+UpdateFail);
                    }
                }
            }
            WebUtil.putInAttribute(mode, AttributeName.Form, form);
        } catch (Exception ex) {
            this.showMessage(mode,MessageType.DANGER,ex.getMessage());
        }
        return Router.UI_020061_B_Detail.getView();
    }
    @RequestMapping(value = "/rm/UI_020061_B/setALLBANK_BRNO_CHKCODE")
    @ResponseBody
    private BaseResp<UI_020061_B_FormDetail> setALLBANK_BRNO_CHKCODE(@RequestBody UI_020061_B_FormDetail form){
        RM txRMBusiness = new RM();
        BaseResp<UI_020061_B_FormDetail> response = new BaseResp<>();
        try {
            RefString sBkno =new RefString(form.getAllbankBkno());
            RefString sBrno = new RefString(form.getAllbankBrno());
            RefString sType = new RefString(form.getAllbankType());
            if ("101,102,103,108,118,147".contains(sBkno.get())){
                sType =new RefString( "2");
            }
            String string = txRMBusiness.getBankDigit(sBkno,sBrno,sType);
            form.setAllbankBrnoChkcode(string);
            response.setData(form);
            return response;
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        }
       return response;
    }

    /**
     * 檢核資料正確後，即送入defALLBANK中，以供後續新增或修改之用
     */
    private Boolean checkAllField(UI_020061_B_FormDetail form, RefBase<Allbank> refAllbank, ModelMap mode){
        String strTemp;
        Integer intMaxLen;
        Allbank defAllbank = new Allbank();

        try {
            defAllbank.setAllbankBkno(form.getAllbankBkno());
            defAllbank.setAllbankBrno(form.getAllbankBrno());
            defAllbank.setAllbankFiscUnit(Short.valueOf(form.getAllbankFiscUnit()));
            defAllbank.setAllbankUnitBank(form.getAllbankUnitBank());

            strTemp = form.getAllbankAliasname();
            intMaxLen = 10; //CType(Me.EditFormFrmv.FindControl("ALLBANK_ALIASNAMETxt"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "銀行之簡稱"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankAliasname(strTemp);
                }
            }

            strTemp = form.getAllbankFullname().replaceAll("　"," ").trim();
            intMaxLen = 50; //CType(Me.EditFormFrmv.FindControl("ALLBANK_FULLNAMETxt"), TextBox).Text
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "銀行之全名"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankFullname(strTemp);
                }
            }

            strTemp = form.getAllbankEngname().trim().replaceAll("　"," ").trim();
            intMaxLen = 70;//CType(Me.EditFormFrmv.FindControl("ALLBANK_ENGNAME"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "銀行之英文名稱"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankEngname(strTemp);
                }
            }

            strTemp = form.getAllbankAddrress().replaceAll("　"," ").trim();
            intMaxLen =70;//  CType(Me.EditFormFrmv.FindControl("ALLBANK_ADDRRESSTxt"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "銀行之地址"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankAddrress(strTemp);
                }
            }

            strTemp = form.getAllbankZipcode1().replaceAll("　"," ").trim();
            intMaxLen =3;// CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE1Txt"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "郵遞區號1"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankZipcode1(strTemp);
                }
            }

            strTemp = form.getAllbankZipcode2().replaceAll("　"," ").trim();
            intMaxLen = 2; // CType(Me.EditFormFrmv.FindControl("ALLBANK_ZIPCODE2Txt"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "郵遞區號2"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankZipcode2(strTemp);
                }
            }

            strTemp = form.getAllbankTelno().replaceAll("　"," ").trim();
            intMaxLen = 15; //  CType(Me.EditFormFrmv.FindControl("ALLBANK_TELNOTxt"), TextBox).MaxLength
            if (StringUtils.isNotBlank(strTemp)){
                if(ConvertUtil.toBytes(strTemp, PolyfillUtil.toCharsetName("Big5")).length>intMaxLen){
                    this.showMessage(mode, MessageType.DANGER, "電話"+LenthMoreThenMax.replace("MAX",intMaxLen.toString()));
                    return false;
                }else {
                    defAllbank.setAllbankTelno(strTemp);
                }
            }

            defAllbank.setAllbankRmflag(form.getAllbankRmflag());
            defAllbank.setAllbankRmforward(form.getAllbankRmforward());

            strTemp = form.getAllbankSetCloseTime().replace(":","");
            if (StringUtils.isNotBlank(strTemp)){
                defAllbank.setAllbankSetCloseTime(Integer.parseInt(strTemp));
            }
            defAllbank.setAllbankSetCloseFlag(form.getAllbankSetCloseFlag());
            if ("101,102,103,108,118,147".contains(defAllbank.getAllbankBkno())){
                defAllbank.setAllbankType("2");
                form.setAllbankType("2");
            }else {
                defAllbank.setAllbankType(form.getAllbankType());
            }
            setALLBANK_BRNO_CHKCODE(form);
            defAllbank.setAllbankBrnoChkcode(form.getAllbankBrnoChkcode());

            defAllbank.setUpdateUserid(Integer.parseInt(WebUtil.getUser().getUserId()));
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, ex.toString());
            return false;
        }
        refAllbank.set(defAllbank);
        return true;
    }
    private void checkRMSNO(Allbank defAllbank) throws Exception {
        Rmoutsno defRmoutsno = new Rmoutsno();
        Rminsno defRminsno = new Rminsno();

        try {
            defRmoutsno.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRmoutsno.setRmoutsnoReceiverBank(defAllbank.getAllbankBkno());
            if (rmService.getRmoutsnoQueryByPrimaryKey(defRmoutsno)==null){
                defRmoutsno.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRmoutsno.setRmoutsnoReceiverBank(defAllbank.getAllbankBkno());
                defRmoutsno.setRmoutsnoNo(0);
                defRmoutsno.setRmoutsnoRepNo(0);
                defRmoutsno.setRmoutsnoChgk("0");
                defRmoutsno.setRmoutsnoChgkTimes(0);
                defRmoutsno.setRmoutsnoCdkeyFlag("0");
                defRmoutsno.setRmoutsno3des("3");
                defRmoutsno.setRmoutsnoDesDate("");
                if (rmService.insertRmoutsno(defRmoutsno)<1){
                    logContext.setRemark("ALLBANK_BKNO="+defAllbank.getAllbankBkno()+", 新增RMOUTSNO失敗");
                } else {
                    logContext.setRemark("ALLBANK_BKNO="+defAllbank.getAllbankBkno()+", 新增RMOUTSNO成功");
                }
                rmService.logMessage(logContext, Level.INFO);
            }
            rmService.logMessage(logContext, Level.INFO);
            defRminsno.setRminsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRminsno.setRminsnoReceiverBank(defAllbank.getAllbankBkno());
            if (rmService.getRminsnoQueryByPrimaryKey(defRminsno)==null){
                defRminsno.setRminsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
                defRminsno.setRminsnoReceiverBank(defAllbank.getAllbankBkno());
                defRminsno.setRminsnoNo(0);
                defRminsno.setRminsnoChgk("0");
                defRminsno.setRminsnoChgkTimes(0);
                defRminsno.setRminsnoCdkeyFlag("0");
                defRminsno.setRminsno3des("3");
                defRminsno.setRminsnoDesDate("");
                if (rmService.insertRminsno(defRminsno)<1){
                    logContext.setRemark("ALLBANK_BKNO="+defAllbank.getAllbankBkno()+", 新增RMINSNO失敗");
                } else {
                    logContext.setRemark("ALLBANK_BKNO="+defAllbank.getAllbankBkno()+", 新增RMINSNO成功");
                }
                rmService.logMessage(logContext, Level.INFO);
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            logContext.setRemark(exceptionMsg);
            rmService.logMessage(logContext, Level.INFO);
            sendEMS(logContext);
        }
    }
}
