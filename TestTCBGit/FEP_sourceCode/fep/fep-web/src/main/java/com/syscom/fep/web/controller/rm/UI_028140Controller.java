package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.ext.mapper.RmstatExtMapper;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028140_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 負責處理UI028140調整匯款狀態
 *
 * @author chenyu
 * @create 2021/11/10
 */
@Controller
public class UI_028140Controller extends BaseController {
    @Autowired
    RmstatExtMapper rmstatExtMapper;
    @Autowired
    RmService _RMSrv;

    String _ShowMessage = "";

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028140_Form form = new UI_028140_Form();
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        querySYSSTAT(mode);
        RmstatGetAndBind(mode,form);
    }

    @PostMapping(value = "/rm/UI_028140/queryClick")
    public String queryClick(@ModelAttribute UI_028140_Form form, ModelMap mode) {
        this.infoMessage("查詢主檔數據, 條件 = [", form, "]");
        this.doKeepFormData(mode, form);

        Rmstat _defRMSTAT = new Rmstat();
        try {
            _ShowMessage = " ";
            //更新RMSTAT FLAG
            switch (form.getKind()) {
                case "1":  //CBS匯出
                    _defRMSTAT.setRmstatCbsOutFlag(form.getCbsflagddl());
                    form.setCbsoutflag(form.getCbsflagddl());
                    break;
                case "2": //CBS匯入
                    _defRMSTAT.setRmstatCbsInFlag(form.getCbsflagddl());
                    form.setCbsinflag(form.getCbsflagddl());
                    break;
                case "3": //全行匯款
                    _defRMSTAT.setRmstatRmFlag(form.getCbsflagddl());
                    form.setRmstatflag(form.getCbsflagddl());
                    break;
            }
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            _defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());

            //_defRMSTAT.RMSTAT_HBKNO = Configuration.SysStatus.PropertyValue.SYSSTAT_HBKNO
            //modified by maxine on 2011/08/02 for 補送EMS
            String strMsg = "調整成功";

            if (rmstatExtMapper.updateByPrimaryKeySelective(_defRMSTAT) < 0) {
                strMsg = "匯款狀態檔(RMSTAT)更新失敗";
                _ShowMessage = UpdateFail;
                this.showMessage(mode, MessageType.DANGER, IOReturnCode.RMSTATUpdateError);
            }
            prepareAndSendEMSData(form, strMsg);
        } catch (Exception ex) {
            LogData logContext = new LogData();
            logContext.setProgramException(ex);
            this.showMessage(mode, MessageType.DANGER, CommonReturnCode.ProgramException);
        }

//        _ShowMessage = "交易成功!!";
        this.showMessage(mode,MessageType.SUCCESS,DealSuccess);
        return Router.UI_028140.getView();
    }

    private void querySYSSTAT(ModelMap mode) {
        try {
            List<Sysstat> _dtSYSSTAT = _RMSrv.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode,MessageType.DANGER,"SYSSTAT無資料!!");
            }
//            SysStatus.getPropertyValue().getSysstatHbkno();
//            _dtSYSSTAT.get(0).getSysstatHbkno().toString();
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    private void RmstatGetAndBind(ModelMap mode,UI_028140_Form form) {
        Rmstat _defRMSTAT = new Rmstat();
        try {
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            _defRMSTAT.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
            //_defRMSTAT.RMSTAT_HBKNO = Configuration.SysStatus.PropertyValue.SYSSTAT_HBKNO
            _defRMSTAT =_RMSrv.getRmstatByPk(_defRMSTAT);
            if (_defRMSTAT != null) {
                form.setCbsoutflag(_defRMSTAT.getRmstatCbsOutFlag());
                form.setCbsinflag(_defRMSTAT.getRmstatCbsInFlag());
                form.setRmstatflag(_defRMSTAT.getRmstatRmFlag());
                WebUtil.putInAttribute(mode, AttributeName.Form, form);
            } else {
                _ShowMessage = QueryFail;
            }
        } catch (Exception ex) {
            LogData logContext = new LogData();
            logContext.setProgramException(ex);
        }
    }

    private void prepareAndSendEMSData(UI_028140_Form form, String strMsg) {
        LogData logContext = new LogData();

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028140");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setMessageId("UI028140");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(form.getKind() + "-調整為[" + form.getCbsflagddl() + "], " + strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangeCBSRMStatus);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
    }
}
