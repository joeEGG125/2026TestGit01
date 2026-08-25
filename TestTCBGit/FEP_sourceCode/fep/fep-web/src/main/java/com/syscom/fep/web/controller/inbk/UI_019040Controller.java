package com.syscom.fep.web.controller.inbk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.syscom.fep.web.audit.AuditLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.FwdtxnExtMapper;
import com.syscom.fep.mybatis.ext.model.FwdtxnExt;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.PageData;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_019040_Form;
import com.syscom.fep.web.service.InbkService;
import com.syscom.fep.web.util.WebUtil;

/**
 * 查詢預約轉帳交易結果
 *
 * @author xingyun_yang
 * @create 2021/8/23
 */
@Controller
public class UI_019040Controller extends BaseController {
    @Autowired
    FwdtxnExtMapper fwdtxnExtMapper;
    @Autowired
    private InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單資料
        UI_019040_Form form = new UI_019040_Form();
        // 營業日期
        form.setFwdrstTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_DASH));
        mode.addAttribute("fwdtxnTxId","0");
        //判斷radio 的值
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_019040/getFWDTXNByTSBDYFISC")
    public String getFWDTXNByTSBDYFISC(@ModelAttribute UI_019040_Form form, ModelMap mode) {
        this.infoMessage("查詢明細資料, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode,form);
        try {
            String fwdrstTxDate = form.getFwdrstTxDate().replace("-", "");
            String selectValue;
            selectValue = "0";
            switch (form.getRadioOption()) {
                case ALL:
                    selectValue = "0";
                    mode.addAttribute("fwdtxnTxId","0");
                    break;
                case FAIL:
                    selectValue = "1";
                    mode.addAttribute("fwdtxnTxId","0");
                    break;
                case RSTEPS:
                    selectValue = "3";
                    mode.addAttribute("fwdtxnTxId","0");
                    break;
                case ORDER:
                    selectValue = "2";
                    mode.addAttribute("fwdtxnTxId","2");
                    break;
                default:
                    break;
            }
            String fwdtxnTxId = form.getFwdtxnTxId();
            String channel = form.getChannel();
            String fwdtxnTroutActno = form.getFwdtxnTroutActno();
            String fwdtxnTrinBkno = form.getFwdtxnTrinBkno();
            String fwdtxnTrinActno = form.getFwdtxnTrinActno();
            String fwdtxnTxAmt = form.getFwdtxnTxAmt();
            Short  sysFail = form.getSysFail();
            Integer pageNum = form.getPageNum();
            Integer pageSize = form.getPageSize();
//            PageInfo<HashMap<String,Object>> pageInfo = inbkService.getFWDTXNByTSBDYFISC(
//                    fwdrstTxDate,selectValue,fwdtxnTxId,channel,fwdtxnTroutActno,fwdtxnTrinBkno,fwdtxnTrinActno,fwdtxnTxAmt,sysFail,pageNum,pageSize
//            );
            AuditLog auditLog = new AuditLog();
            // 塞入auditLog.action
            auditLog.setAction("查詢");
            // 塞入auditLog.params
            if (StringUtils.isNotBlank(form.getFwdtxnTxId()))
                auditLog.addParam("預約檔日期", form.getFwdtxnTxId());
            auditLog.addParam("系統錯誤", form.getSysFail());
            auditLog.addParam("fwdtxnTxId", form.getRadioOption());
            if (StringUtils.isNotBlank(form.getChannel()))
                auditLog.addParam("通路別", form.getChannel());
            if (StringUtils.isNotBlank(form.getFwdtxnTroutActno()))
                auditLog.addParam("轉出帳號", form.getFwdtxnTroutActno());
            if (StringUtils.isNotBlank(form.getFwdtxnTrinBkno()))
                auditLog.addParam("轉入帳號Bkno", form.getFwdtxnTrinBkno());
            if (StringUtils.isNotBlank(form.getFwdtxnTrinActno()))
                auditLog.addParam("轉入帳號Actno", form.getFwdtxnTrinActno());
            if (StringUtils.isNotBlank(form.getFwdtxnTxAmt()))
                auditLog.addParam("交易金額", form.getFwdtxnTxAmt());
            // 最後塞入ThreadLocal變量中
            setAuditLog(auditLog);
			PageData<UI_019040_Form, HashMap<String, Object>> pageData = new PageData<>(
					inbkService.getFWDTXNByTSBDYFISC(fwdrstTxDate, selectValue, fwdtxnTxId, channel, fwdtxnTroutActno,
							fwdtxnTrinBkno, fwdtxnTrinActno, fwdtxnTxAmt, sysFail, pageNum, pageSize),
					form);
            int i = 0;
            List<HashMap<String,Object>> fwdtxnandrstExtHashs = new ArrayList<>();
            if (pageData.getList().size() > 0) {
            	fwdtxnandrstExtHashs = new ArrayList<>(pageData.getList().size());
                int tempVar = pageData.getList().size();
                for (i = 0; i < tempVar; i++) {
                    HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("FWDTXN_TX_DATE", pageData.getList().get(i).get("FWDTXN_TX_DATE").toString());
                        hashMap.put("FWDTXN_TX_ID", pageData.getList().get(i).get("FWDTXN_TX_ID").toString());
                        hashMap.put("FWDRST_TX_ID", String.valueOf(pageData.getList().get(i).get("FWDRST_TX_ID")));
                        hashMap.put("FWDTXN_CHANNEL_S", pageData.getList().get(i).get("FWDTXN_CHANNEL_S").toString());
                        hashMap.put("FWDRST_TX_DATE", pageData.getList().get(i).get("FWDTXN_TX_DATE").toString());
                        hashMap.put("FWDTXN_PCODE", pageData.getList().get(i).get("FWDTXN_PCODE").toString());
                        hashMap.put("SYSSTAT_HBKNO", SysStatus.getPropertyValue().getSysstatHbkno());
                        hashMap.put("FWDTXN_TROUT_ACTNO", pageData.getList().get(i).get("FWDTXN_TROUT_ACTNO").toString());
                        hashMap.put("FWDTXN_TRIN_BKNO", pageData.getList().get(i).get("FWDTXN_TRIN_BKNO").toString());
                        hashMap.put("FWDTXN_TRIN_ACTNO", pageData.getList().get(i).get("FWDTXN_TRIN_ACTNO").toString());
                        hashMap.put("FWDTXN_TX_AMT", pageData.getList().get(i).get("FWDTXN_TX_AMT").toString());
                        hashMap.put("FWDRST_RUN_NO", pageData.getList().get(i).get("FWDRST_RUN_NO"));
                        hashMap.put("FWDRST_EJFNO", pageData.getList().get(i).get("FWDRST_EJFNO"));
                        hashMap.put("FWDRST_TXRUST", pageData.getList().get(i).get("FWDRST_TXRUST"));
                        hashMap.put("FWDRST_REPLY_CODE", pageData.getList().get(i).get("FWDRST_REPLY_CODE"));
                        hashMap.put("FWDRST_ERR_MSG", pageData.getList().get(i).get("FWDRST_ERR_MSG"));
                        hashMap.put("FWDTXN_RERUN_FG", pageData.getList().get(i).get("FWDTXN_RERUN_FG"));
                        fwdtxnandrstExtHashs.add(hashMap);
                }
            }
            List<FwdtxnExt> fwdtxnandrstExts =
                    fwdtxnExtMapper.getSummary(fwdrstTxDate,selectValue,fwdtxnTxId,channel,fwdtxnTroutActno,fwdtxnTrinBkno,fwdtxnTrinActno,fwdtxnTxAmt,sysFail);
            //總筆數
            int totalCount = fwdtxnandrstExts.size();
            //總金額
            int sum = 0;
            //成功筆數
            int success = 0;
            //失敗筆數
            int failTimes = 0;
            //系統原因
            int sysFailTimes=0;
            //其它原因(財金)
            int otherFailTimes=0;
            //客戶原因(主機)
            int custFailTimes=0;
            for (FwdtxnExt s:fwdtxnandrstExts) {
                sum+=Double.parseDouble(s.getFwdtxnTxAmt());
                if (!"    ".equals(s.getFwdtxnReplyCode()) && !"0000".equals(s.getFwdtxnReplyCode())){
                    failTimes++;
                    if (!StringUtils.isBlank(s.getFwdtxnReplyCode())) {
                        if ("EF".equals(s.getFwdtxnReplyCode().substring(0, 2))) {
                            sysFailTimes++;
                        } else if ("EX".equals(s.getFwdtxnReplyCode().substring(0, 2))) {
                            otherFailTimes++;
                        } else {
                            custFailTimes++;
                        }
                    }
                    else {
                        custFailTimes++;
                    }
                }else {
                    success++;
                }
            }
            if (fwdtxnandrstExtHashs == null || fwdtxnandrstExtHashs.size() == 0) {
                this.showMessage(mode, MessageType.INFO,QueryNoData);
            } else {
                pageData.setList(fwdtxnandrstExtHashs);
                mode.addAttribute("totalCount",totalCount);
                mode.addAttribute("sum",sum);
                mode.addAttribute("success",success);
                mode.addAttribute("failTimes",failTimes);
                mode.addAttribute("sysFailTimes",sysFailTimes);
                mode.addAttribute("otherFailTimes",otherFailTimes);
                mode.addAttribute("custFailTimes",custFailTimes);
                WebUtil.putInAttribute(mode, AttributeName.PageData, pageData);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            this.showMessage(mode, MessageType.DANGER, programError);
        }
        return Router.UI_019040.getView();
    }
}
