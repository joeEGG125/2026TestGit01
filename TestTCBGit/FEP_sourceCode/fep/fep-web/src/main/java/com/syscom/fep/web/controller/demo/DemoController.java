package com.syscom.fep.web.controller.demo;

import com.github.pagehelper.PageInfo;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapter;
import com.syscom.fep.frmcommon.net.ftp.FtpAdapterFactory;
import com.syscom.fep.frmcommon.net.ftp.FtpProperties;
import com.syscom.fep.frmcommon.net.ftp.FtpProtocol;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.model.FeptxnExt;
import com.syscom.fep.mybatis.mapper.AccountMapper;
import com.syscom.fep.mybatis.mapper.AtmboxMapper;
import com.syscom.fep.mybatis.model.Account;
import com.syscom.fep.mybatis.model.Atmbox;
import com.syscom.fep.mybatis.model.Bsdays;
import com.syscom.fep.mybatis.model.Feptxn;
import com.syscom.fep.vo.constant.ZoneCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.*;
import com.syscom.fep.web.form.demo.Demo_FormCheck;
import com.syscom.fep.web.form.demo.Demo_FormDetail;
import com.syscom.fep.web.form.demo.Demo_FormMain;
import com.syscom.fep.web.form.demo.Demo_FormModify;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * For Demo
 *
 * @author Richard
 */
@Controller
public class DemoController extends BaseController {
    @Autowired
    private InbkService inbkService;
    @Autowired
    private FtpAdapterFactory ftpAdapterFatory;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AtmboxMapper atmBoxMapper;
    @Autowired
    private PlatformTransactionManager fepdbTransactionManager;

    @Override
    public void pageOnLoad(ModelMap mode) {
        this.bindConstant(mode);
        // 初始化表單資料
        Demo_FormMain form = new Demo_FormMain();
        // 交易日期
        // form.setTradingDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_2));
        form.setTradingDate("2021-06-08");
        form.setTradingTime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HH_MM_SS));
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * 為頁面綁定一些常量
     *
     * @param mode
     */
    private void bindConstant(ModelMap mode) {
        // 初始化PCODE下拉選單
        List<SelectOption<String>> selectOptionList = new ArrayList<>();
        selectOptionList.add(new SelectOption<String>("0101-財金公司押碼基碼同步通知交易", "0101"));
        selectOptionList.add(new SelectOption<String>("0102-參加單位變更押碼基碼請求交易", "0102"));
        selectOptionList.add(new SelectOption<String>("0105-財金公司變更3-DES押碼基碼通知交易", "0105"));
        selectOptionList.add(new SelectOption<String>("3100-參加單位訊息通知交易", "3100"));
        selectOptionList.add(new SelectOption<String>("3101-參加單位應用系統連線作業請求交易", "3101"));
        selectOptionList.add(new SelectOption<String>("3106-參加單位應用系統異常連線作業結束交易", "3106"));
        selectOptionList.add(new SelectOption<String>("3107-參加單位應用系統緊急停止後重新啟動通知交易", "3107"));
        selectOptionList.add(new SelectOption<String>("3109-參加單位應用系統狀態查詢交易", "3109"));
        selectOptionList.add(new SelectOption<String>("3113-參加單位不明訊息通知交易", "3113"));
        selectOptionList.add(new SelectOption<String>("3201-財金公司訊息通知交易", "3201"));
        selectOptionList.add(new SelectOption<String>("3209-財金公司不明訊息通知交易", "3209"));
        selectOptionList.add(new SelectOption<String>("3210-財金公司應用系統連線結束", "3210"));
        selectOptionList.add(new SelectOption<String>("3211-預定連線作業強迫結束交易", "3211"));
        selectOptionList.add(new SelectOption<String>("3215-財金公司CD/ATM作業狀況查詢交易", "3215"));
        selectOptionList.add(new SelectOption<String>("3114-參加單位CD/ATM作業狀況通知", "3114"));
        selectOptionList.add(new SelectOption<String>("3115-參加單位查詢CD/ATM作業狀況", "3115"));
        WebUtil.putInAttribute(mode, AttributeName.Options, selectOptionList);
    }

    @PostMapping(value = "/demo/Demo/inquiryMain")
    public String doInquiryMain(@ModelAttribute Demo_FormMain form, ModelMap mode) {
        this.logMessage(this.logContext);
        this.infoMessage("查詢主檔資料, 條件 = [", form.toString(), "]");
        this.bindConstant(mode);
        this.doKeepFormData(mode, form);
        try {
            String nbsday = StringUtils.EMPTY;
            String tradingDate = StringUtils.replace(form.getTradingDate(), "-", StringUtils.EMPTY);
            // 找下營業日
            Bsdays bsdays = inbkService.getBsdaysByPk(ZoneCode.TWN, tradingDate);
            if (bsdays != null) {
                // 工作日
                if (DbHelper.toBoolean(bsdays.getBsdaysWorkday())) {
                    nbsday = bsdays.getBsdaysNbsdy();
                } else {
                    // 2012/12/10 Modify by Ruling BSDAYS_NBSDY的值要塞給nbSDY
                    nbsday = bsdays.getBsdaysNbsdy();
                }
            }
            FeptxnExt feptxn = new FeptxnExt();
            feptxn.setTableNameSuffix(tradingDate.substring(6, 8));
            feptxn.setFeptxnTxDate(tradingDate);
            switch (form.getRadioOption()) {
                case EJNO:
                    feptxn.setFeptxnEjfno(form.getEjno());
                    break;
                case PCODE:
                    feptxn.setFeptxnPcode(form.getPcode());
                    break;
                case STAN:
                    feptxn.setFeptxnBkno(form.getBkno());
                    feptxn.setFeptxnStan(form.getStan());
                    break;
                default:
                    break;
            }
            if (StringUtils.isNotBlank(nbsday) && nbsday.length() >= 8) {
                nbsday = nbsday.substring(6, 8);
            }
            PageInfo<Feptxn> pageInfo = inbkService.getFeptxnByTxDate(feptxn, nbsday, form.getPageNum(), form.getPageSize());
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            }
            PageData<Demo_FormMain, Feptxn> pageData = new PageData<Demo_FormMain, Feptxn>(pageInfo, form);
            WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.DEMO.getView();
    }

    @PostMapping(value = "/demo/Demo/inquiryDetail")
    public String doInquiryDetail(@ModelAttribute Demo_FormDetail form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        try {
            String nbsday = StringUtils.EMPTY;
            String tradingDate = form.getFeptxnTxDate();
            // 找下營業日
            Bsdays bsdays = inbkService.getBsdaysByPk(ZoneCode.TWN, tradingDate);
            if (bsdays != null) {
                // 工作日
                if (DbHelper.toBoolean(bsdays.getBsdaysWorkday())) {
                    nbsday = bsdays.getBsdaysNbsdy();
                } else {
                    // 2012/12/10 Modify by Ruling BSDAYS_NBSDY的值要塞給nbSDY
                    nbsday = bsdays.getBsdaysNbsdy();
                }
            }
            FeptxnExt feptxn = new FeptxnExt();
            feptxn.setTableNameSuffix(tradingDate.substring(6, 8));
            feptxn.setFeptxnTxDate(tradingDate);
            feptxn.setFeptxnEjfno(form.getFeptxnEjfno());
            if (StringUtils.isNotBlank(nbsday) && nbsday.length() >= 8) {
                nbsday = nbsday.substring(6, 8);
            }
            PageInfo<Feptxn> pageInfo = inbkService.getFeptxnByTxDate(feptxn, nbsday, 1, 0);
            if (pageInfo.getSize() == 0) {
                this.showMessage(mode, MessageType.INFO, QueryNoData);
            } else {
                feptxn = (FeptxnExt) pageInfo.getList().get(0);
            }
            // 應該只會有一筆資料
            WebUtil.putInAttribute(mode, AttributeName.DetailEntity, feptxn);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.DEMO_Detail.getView();
    }

    @PostMapping(value = "/demo/Demo/doDelete")
    @ResponseBody
    public BaseResp<?> doDelete(@RequestBody List<Demo_FormCheck> formList) {
        this.infoMessage("執行刪除動作, 條件 = [", formList.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        try {
            // TODO delete or do something
            response.setMessage(MessageType.INFO, DeleteSuccess);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // show錯誤訊息到前台頁面
            response.setMessage(MessageType.DANGER, DeleteFail);
        }
        return response;
    }

    @PostMapping(value = "/demo/Demo/doSftp")
    @ResponseBody
    public BaseResp<?> doSftp() {
        FtpProperties sftpProperties = new FtpProperties();
        sftpProperties.setHost("192.168.30.67");
        sftpProperties.setPort(22);
        sftpProperties.setPassword("syscom123");
        sftpProperties.setProtocol(FtpProtocol.SFTP);
        sftpProperties.setUsername("sftpuser");
        FtpAdapter ftp = ftpAdapterFatory.createFtpAdapter(sftpProperties);
        boolean result = ftp.upload("C:/Users/Richard/Desktop/ftp/upload/0.txt", "/data/upload/upload-0.txt");
        BaseResp<?> response = new BaseResp<>();
        if (result)
            response.setMessage(MessageType.SUCCESS, "上傳成功");
        else
            response.setMessage(MessageType.DANGER, "上傳失敗");
        return response;
    }

    @PostMapping(value = "/demo/Demo/500_post")
    public String doErrorPost(@ModelAttribute Demo_FormMain form, ModelMap mode) {
        throw ExceptionUtil.createNotImplementedException("Not Implemented");
    }

    @PostMapping(value = "/demo/Demo/500_ajax")
    @ResponseBody
    public BaseResp<?> doErrorAjax() {
        throw ExceptionUtil.createNotImplementedException("Not Implemented");
    }

    @PostMapping(value = "/demo/Demo/clearUser")
    @ResponseBody
    public BaseResp<?> doClearUser() {
        WebUtil.removeFromSession(SessionKey.LogonUser);
        BaseResp<?> response = new BaseResp<>();
        response.setMessage(MessageType.SUCCESS, "清除登入者訊息");
        return response;
    }

    @PostMapping(value = "/demo/Demo/doModify")
    @ResponseBody
    public BaseResp<?> doModify(@RequestBody Demo_FormModify form) {
        this.infoMessage("執行修改動作, 條件 = [", form.toString(), "]");
        BaseResp<?> response = new BaseResp<>();
        try {
            // TODO modify or do something
            response.setMessage(MessageType.INFO, UpdateSuccess);
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            // show錯誤訊息到前台頁面
            response.setMessage(MessageType.DANGER, UpdateFail);
        }
        return response;
    }

    @PostMapping(value = "/demo/Demo/doTxTest")
    @ResponseBody
    public BaseResp<?> doTxTest() {
        Account[] accounts = new Account[]{
                new Account(StringUtils.leftPad("0", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("1", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("2", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("3", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("4", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("5", 14, "0"), "2", "20210408")
        };
        for (Account account : accounts) {
            accountMapper.deleteByPrimaryKey(account);
        }
        Atmbox atmbox = new Atmbox();
        atmbox.setAtmboxAtmno("1");
        atmbox.setAtmboxBoxno((short) 1);
        atmbox.setAtmboxBrnoSt("2");
        atmbox.setAtmboxCur("TWD");
        atmbox.setAtmboxDeposit(10000);
        atmbox.setAtmboxPresent(200000);
        atmbox.setAtmboxRefill(500);
        atmbox.setAtmboxReject(100);
        atmbox.setAtmboxRwtSeqno(300);
        atmbox.setAtmboxSettle((short) 1);
        atmbox.setAtmboxTxDate("20210507");
        atmbox.setAtmboxUnit(900000);
        atmbox.setAtmboxUnknown(40);
        atmbox.setUpdateTime(Calendar.getInstance().getTime());
        atmbox.setUpdateUserid(100000);
        atmbox.setAtmboxCur("TWD99999999999999999999999999999999999999999999");

        BaseResp<?> response = new BaseResp<>();
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = fepdbTransactionManager.getTransaction(definition);
        try {
            accountMapper.insert(accounts[0]);
            accountMapper.insert(accounts[1]);
            atmBoxMapper.insert(atmbox);
            fepdbTransactionManager.commit(txStatus);
            response.setMessage(MessageType.SUCCESS, "測試成功, 正常提交");
        } catch (Exception e) {
            try {
                fepdbTransactionManager.rollback(txStatus);
                response.setMessage(MessageType.SUCCESS, "測試成功, 正常回滾");
            } catch (TransactionException txe) {
                this.errorMessage(e, e.getMessage());
                response.setMessage(MessageType.DANGER, "測試失敗");
            }
        }
        return response;
    }

    /**
     * 指定需要將電文打到那台ATM Service
     * <p>
     * curl -d "operator=admin" -X POST http://localhost:8081/demo/Demo/post0
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/demo/Demo/post0", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String doPost0(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        return "doPost0 " + ("admin".equals(operator) ? "Hello Administrator" : "Anybody");
    }

    /**
     * 指定需要將電文打到那台ATM Service
     * <p>
     * curl -d "operator=admin" -X POST http://localhost:8081/demo/Demo/post1
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/demo/Demo/post1", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String doPost1(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        return "doPost1 " + ("admin".equals(operator) ? "Hello Administrator" : "Anybody");
    }

    /**
     * 指定需要將電文打到那台ATM Service
     * <p>
     * curl -d "operator=admin" -X POST http://localhost:8081/demo/Demo/post2
     *
     * @param operator
     * @return
     */
    @RequestMapping(value = "/demo/Demo/post2", method = RequestMethod.POST, produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String doPost2(@RequestParam(value = "operator", required = false, defaultValue = StringUtils.EMPTY) String operator) {
        return "doPost2 " + ("admin".equals(operator) ? "Hello Administrator" : "Anybody");
    }
}
