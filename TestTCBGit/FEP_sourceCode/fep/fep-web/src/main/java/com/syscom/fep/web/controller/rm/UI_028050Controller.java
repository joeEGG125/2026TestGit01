package com.syscom.fep.web.controller.rm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.model.Rminsno;
import com.syscom.fep.mybatis.model.Rmoutsno;
import com.syscom.fep.mybatis.model.Rmstat;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028050_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;

@Controller
public class UI_028050Controller extends BaseController {

    @Autowired
    RmService rmService;


    //add by maxine on 2011/06/27 for SYSSTAT自行查
    private List<Sysstat> _dtSYSSTAT;
    private String _SYSSTAT_HBKNO;

    @PostMapping(value = "/rm/UI_028050/pageLoad")
    @ResponseBody
    public UI_028050_Form pageLoad() {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        UI_028050_Form form = new UI_028050_Form();
        querySYSSTAT(form);
        getRMSTAT(form);
        return form;
    }

    private void querySYSSTAT(UI_028050_Form form) {
        try {
            _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                form.setMessage(MessageType.DANGER,"SYSSTAT無資料!!");
                return;
            }
            _SYSSTAT_HBKNO = _dtSYSSTAT.get(0).getSysstatHbkno();
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }

    @PostMapping(value = "/rm/UI_028050/queryClick")
    @ResponseBody
    public UI_028050_Form queryClick(@RequestBody UI_028050_Form form) {
        Rmstat defRMSTAT = new Rmstat();
        try {
            //modified by maxine on 2011/06/27 for SYSSTAT自行查
            defRMSTAT.setRmstatHbkno(_SYSSTAT_HBKNO);
            //defRMSTAT.RMSTAT_HBKNO = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRMSTAT = rmService.getRmstatQueryByPrimaryKey(defRMSTAT);
            if (defRMSTAT == null) {
                form.setMessage(MessageType.WARNING,"請先將收/送狀態暫停");
            }

            getRMOUTSNO(false,form);
            getRMINSNO(false,form);

            form.setNewReceiverSeqRv("");
            form.setNewSenderSeqRv("");
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.getMessage());
        }
        return form;
    }

    @PostMapping(value = "/rm/UI_028050/executeClick")
    @ResponseBody
    public UI_028050_Form executeClick(@RequestBody UI_028050_Form form) {
        Rmstat defRMSTAT = new Rmstat();
        try {
            //modified by maxine on 2011/06/27 for SYSSTAT自行查
            defRMSTAT.setRmstatHbkno(_SYSSTAT_HBKNO);
            //defRMSTAT.RMSTAT_HBKNO = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRMSTAT = rmService.getRmstatQueryByPrimaryKey(defRMSTAT);
            if (defRMSTAT == null) {
                form.setMessage(MessageType.WARNING,"請先將收/送狀態暫停");
            }

            if (!"".equals(form.getKind())) {
                switch (form.getKind()) {
                    //Case "0" 'Both
                    //    GetRMOUTSNO(False)
                    //    GetRMINSNO(False)
                    case "1": //RMOUT
                        getRMOUTSNO(true,form);
                        break;
                    case "2": //RMIN
                        getRMINSNO(true,form);
                        break;
                    default:
                        form.setMessage(MessageType.WARNING,"請選擇調整項目");
                        return form;
                }
            } else {

            }

            //add by maxine on 2011/06/16 for 增加查詢按鈕
            queryClick(form);
        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.getMessage());
        }

        return form;
    }

    /**
     查詢並更新GetRMOUTSNO

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMOUTSNO(boolean needUpdated,UI_028050_Form form) {

        Rmoutsno defRMOUTSNO = new Rmoutsno();

        //add by Maxine on 2011/08/02 for 補送EMS
        String strMsg = "";

        try {
            //modified by maxine on 2011/06/27 for SYSSTAT自行查
            defRMOUTSNO.setRmoutsnoSenderBank(_SYSSTAT_HBKNO);
            //defRMOUTSNO.RMOUTSNO_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRMOUTSNO.setRmoutsnoReceiverBank(form.getBkno());
            Rmoutsno result = rmService.getRmoutsnoQueryByPrimaryKey(defRMOUTSNO);
            if (result != null) {
                defRMOUTSNO = result;
            }
            if (result == null) {
                strMsg = "匯出通匯序號檔(RMOUTSNO)無此資料";
                form.setMessage(MessageType.DANGER,"匯出通匯序號檔" + QueryNoData);
            } else {
                form.setSenderSeqRv(defRMOUTSNO.getRmoutsnoNo().toString());
            }
            if (needUpdated) {//when KIND = 1為true
                if (StringUtils.isNotBlank(form.getNewSenderSeqRv())) {
                    defRMOUTSNO.setRmoutsnoNo(Integer.parseInt(form.getNewSenderSeqRv()));
                    //modify by Jim, 2011/09/16, REP_NO也更新
                    defRMOUTSNO.setRmoutsnoRepNo(Integer.parseInt(form.getNewSenderSeqRv()));
                    if (rmService.updateByPrimaryKeyRmoutsno(defRMOUTSNO) < 1) {
                        strMsg = "匯出通匯序號檔(RMOUTSNO)" + UpdateFail;
                        form.setMessage(MessageType.DANGER,"匯出通匯序號檔" + UpdateFail);
                    } else {
                        strMsg = "=" + defRMOUTSNO.getRmoutsnoNo().toString();
                        form.setMessage(MessageType.SUCCESS,UpdateSuccess);
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後送匯出序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {
                    prepareAndSendEMSData(strMsg,form.getKind(),form.getBkno());
                }

            }

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }

    /**
     查詢並更新RMINSNO

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMINSNO(boolean needUpdated,UI_028050_Form form) {

        Rminsno defRMINSNO = new Rminsno();

        //add by Maxine on 2011/08/02 for 補送EMS
        String strMsg = "";

        try {
            defRMINSNO.setRminsnoSenderBank(form.getBkno());
            //modified by maxine on 2011/06/27 for SYSSTAT自行查
            defRMINSNO.setRminsnoReceiverBank(_SYSSTAT_HBKNO);
            //defRMINSNO.RMINSNO_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            Rminsno result = rmService.getRminsnoQueryByPrimaryKey(defRMINSNO);

            if (result != null) {
                defRMINSNO = result;
            }
            if (result == null) {
                strMsg = "匯入通匯序號檔(RMINSNO)無此資料";
                form.setMessage(MessageType.DANGER,"匯入通匯序號檔" + QueryNoData);
            } else {
                form.setReceiverSeqRv(defRMINSNO.getRminsnoNo().toString());
            }

            if (needUpdated) {//when KIND = 2為true
                if (StringUtils.isNotBlank(form.getNewReceiverSeqRv())) {
                    defRMINSNO.setRminsnoNo(Integer.parseInt(form.getNewReceiverSeqRv()));
                    if (rmService.updateByPrimaryKeyRminsno(defRMINSNO) < 1) {
                        strMsg = "匯入通匯序號檔(RMINSNO)" + UpdateFail;
                        form.setMessage(MessageType.DANGER,"匯入通匯序號檔" + UpdateFail);
                    } else {
                        strMsg = "=" + defRMINSNO.getRminsnoNo().toString();
                        form.setMessage(MessageType.SUCCESS,UpdateSuccess);
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後匯入序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {
                    prepareAndSendEMSData(strMsg,form.getKind(),form.getBkno());
                }
            }

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }


    /**
     檢查RMSTAT的狀態欄位是否為暫停
     */
    private void getRMSTAT(UI_028050_Form form) {

        Rmstat defRMSTAT = new Rmstat();
        //modified by maxine on 2011/06/27 for SYSSTAT自行查
        defRMSTAT.setRmstatHbkno(_SYSSTAT_HBKNO);
        defRMSTAT = rmService.getRmstatQueryByPrimaryKey(defRMSTAT);
        //defRMSTAT.RMSTAT_HBKNO = SysStatus.PropertyValue.SYSSTAT_HBKNO
        if (defRMSTAT == null) {
            form.setMessage(MessageType.DANGER,"匯款狀態檔讀無資料");
        } else {
            if ("Y".equals(defRMSTAT.getRmstatFiscoFlag1()) || "Y".equals(defRMSTAT.getRmstatFiscoFlag4()) || "Y".equals(defRMSTAT.getRmstatFisciFlag1()) || "Y".equals(defRMSTAT.getRmstatFisciFlag4())) {
                form.setMessage(MessageType.WARNING,"請先將收/送狀態暫停");
            }
        }
    }


    private void prepareAndSendEMSData(String strMsg, String kind ,String bkno) throws Exception {
        String kindItem = "";
        switch (kind){
            case "1":
                kindItem = "匯出";
                break;
            case "2":
                kindItem = "匯入";
                break;
            default:
                break;
        }
        LogData logContext = new LogData();

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028050");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028050");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(StringUtils.join(kindItem , "-對方總行代號" , bkno , "-調整為[" , strMsg , "]"));
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangeRMSno);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
        //FEPBase.SendEMS(logContext)

    }
}