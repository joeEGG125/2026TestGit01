package com.syscom.fep.web.controller.rm;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.Msgout;
import com.syscom.fep.vo.constant.MSGOUTStatus;
import com.syscom.fep.vo.constant.RMCategory;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028010_Form;
import com.syscom.fep.web.resp.BaseResp;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;

/**
 * 一般通訊
 * @author xingyun_yang
 * @create 2021/9/23
 */
@Controller
public class UI_028010Controller extends BaseController {

    @Autowired
    RmService rmService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028010_Form form = new UI_028010_Form();
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    @PostMapping(value = "/rm/UI_028010/insertMsgout")
    @ResponseBody
    public BaseResp<?> insertMsgout(@RequestBody UI_028010_Form form) {
        this.infoMessage("一般通訊匯出類, 條件 = [", form, "]");
        BaseResp<?> response = new BaseResp<>();
        try {
            Msgout defMsgout =  new Msgout();
            Integer result = 0;
            defMsgout.setMsgoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defMsgout.setMsgoutBrno("900");
            // defMSGOUT.MSGOUT_CATEGORY = RMCategory.MSGOut '一般通訊匯出類  20
            defMsgout.setMsgoutCategory(RMCategory.MSGOut);
            // 'modified by maxine on 2011/06/24 for SYSSTAT自行查
            defMsgout.setMsgoutSenderBank(SysStatus.getPropertyValue().getSysstatHbkno()+"0000");
            // 'defMSGOUT.MSGOUT_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO & "0000"

            String receiverBank = form.getReceiverBank();
            defMsgout.setMsgoutReceiverBank(receiverBank);
            //發訊
            defMsgout.setMsgoutStat(MSGOUTStatus.Send);
            //   'obj.CONVERT_TextEncode(CHNMEMO.Text, defMSGOUT.MSGOUT_CHNMEMO)
            String chnMemo = form.getChnmemo();
            defMsgout.setMsgoutChnmemo(chnMemo);
            String engMemo = form.getEngmemo();
            defMsgout.setMsgoutEngmemo(engMemo);
            defMsgout.setMsgoutRegdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defMsgout.setMsgoutRegtime(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
            defMsgout.setMsgoutApdate(defMsgout.getMsgoutRegdate());
            defMsgout.setMsgoutAptime(defMsgout.getMsgoutRegtime());
            defMsgout.setMsgoutRegTlrno(WebUtil.getUser().getUserId());
            defMsgout.setMsgoutFepno(StringUtils.leftPad(String.valueOf(rmService.getRmNo(defMsgout.getMsgoutBrno(),"03")),7,"0"));
            defMsgout.setMsgoutFepsubno("00");
            defMsgout.setMsgoutFiscSndCode("1411");
            result = rmService.insertMsgOut(defMsgout);
            if (result == 1){
                response.setMessage(MessageType.SUCCESS,"一般通訊匯出主檔"+InsertSuccess);
            }else {
                response.setMessage(MessageType.INFO,"一般通訊匯出主檔"+InsertFail);
            }
        } catch (Exception e) {
            this.errorMessage(e, e.getMessage());
            response.setMessage(MessageType.DANGER, programError);
        }
        return response;
    }
}
