package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.form.rm.UI_028040_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Controller
public class UI_028040Controller extends BaseController {

    @Autowired
    RmService rmService;


    private String fiscNo = "950";

    //add by maxine on 2011/06/24 for SYSSTAT自行查
    private List<Sysstat> _dtSYSSTAT;
    private String _SYSSTAT_HBKNO;

    @PostMapping(value = "/rm/UI_028040/pageLoad")
    @ResponseBody
    public UI_028040_Form pageLoad() {
        //add by maxine on 2011/06/24 for SYSSTAT自行查
        UI_028040_Form form = new UI_028040_Form();
        form.setMessage(MessageType.INFO,"");
        form.setTxKind("1");
        querySYSSTAT(form);
        queryClick(form);
        return form;
    }

    private void querySYSSTAT(UI_028040_Form form) {
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

    @PostMapping(value = "/rm/UI_028040/queryClick")
    @ResponseBody
    public UI_028040_Form queryClick(@RequestBody UI_028040_Form form) {
        form.setKind("0");
        if ("1".equals(form.getTxKind())) {
            getRMFISCOUT1(false, form);
            getRMFISCIN1(false, form);
        } else {
            getRMFISCOUT4(false, form);
            getRMFISCIN4(false, form);
        }
        return form;
    }

    @PostMapping(value = "/rm/UI_028040/executeClick")
    @ResponseBody
    public UI_028040_Form executeClick(@RequestBody UI_028040_Form form) {
        try {
            if (!"".equals(form.getKind())) {
                switch (form.getKind()) {
                    case "0": //both
                        form.setMessage(MessageType.WARNING,"請選擇調整項目");
                        return form;
                    case "1": //send to FISC
                        if ("1".equals(form.getTxKind())) {
                            getRMFISCOUT1(true,form);
                            //add by maxine on 2011/06/15 for 執行完成也要再查詢一次
                            getRMFISCOUT1(false,form);
                        } else {
                            getRMFISCOUT4(true,form);
                            //add by maxine on 2011/06/15 for 執行完成也要再查詢一次
                            getRMFISCOUT4(false,form);
                        }
                        break;
                    case "2": //from FISC
                        if ("1".equals(form.getTxKind())) {
                            getRMFISCIN1(true,form);
                            //add by maxine on 2011/06/15 for 執行完成也要再查詢一次
                            getRMFISCIN1(false,form);
                        } else {
                            getRMFISCIN4(true,form);
                            //add by maxine on 2011/06/15 for 執行完成也要再查詢一次
                            getRMFISCIN4(false,form);
                        }
                        break;
                }
            } else {

            }
            form.setNewReceiverSeq("");
            form.setNewSenderSeq("");

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.getMessage());
        }
        return form;
    }


    /**
     查詢並更新GetRMFISCOUT1

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMFISCOUT1(boolean needUpdated, UI_028040_Form form) {

        Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
        //Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
        Rmfiscout1 defRMFISCOUT1_999 = new Rmfiscout1();
        try {
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            defRMFISCOUT1.setRmfiscout1SenderBank(_SYSSTAT_HBKNO);
            //defRMFISCOUT1.RMFISCOUT1_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRMFISCOUT1.setRmfiscout1ReceiverBank(fiscNo);

            defRMFISCOUT1_999.setRmfiscout1SenderBank(_SYSSTAT_HBKNO);
            defRMFISCOUT1_999.setRmfiscout1ReceiverBank("999");

            Rmfiscout1 result = rmService.getRMFISCOUT1ByPK(defRMFISCOUT1);
            Rmfiscout1 result_999 = rmService.getRMFISCOUT1ByPK(defRMFISCOUT1_999);
            if (result != null) {
                defRMFISCOUT1 = result;
            }
            if (result_999 != null) {
                defRMFISCOUT1_999 = result_999;
            }
            //add by Maxine on 2011/08/02 for 補送EMS
            String strMsg = "";

            if (result == null || result_999 == null) {
                strMsg = "匯出電文序號檔(RMFISCOUT1)無此資料";
                form.setMessage(MessageType.DANGER,"匯出電文序號檔" + QueryNoData);
            } else {
                form.setSenderSeq(defRMFISCOUT1.getRmfiscout1No().toString());
            }
            if (needUpdated) {//when KIND = 1為true
                if (StringUtils.isNotBlank(form.getNewSenderSeq())) {
                    defRMFISCOUT1.setRmfiscout1No(Integer.parseInt(form.getNewSenderSeq()));
                    //modify by Jim, 2011/09/16, REP_NO也更新
                    //defRMFISCOUT1.RMFISCOUT1_REP_NO = Decimal.Parse(NEWSENDER_SEQ.Text)
                    defRMFISCOUT1_999.setRmfiscout1RepNo(Integer.parseInt(form.getNewSenderSeq()));
                    if (rmService.updateRMFISCOUT1ByPK(defRMFISCOUT1) < 1) {
                        strMsg = "匯出電文序號檔(RMFISCOUT1)更新失敗";
                        form.setMessage(MessageType.DANGER,"匯出電文序號檔" + UpdateFail);
                    } else {
                        if (rmService.updateRMFISCOUT1ByPK(defRMFISCOUT1_999) < 1) {
                            strMsg = "匯出電文序號檔(RMFISCOUT1_999)更新失敗";
                            form.setMessage(MessageType.DANGER,"匯出電文序號檔" + UpdateFail);
                        } else {
                            strMsg = "=" + defRMFISCOUT1.getRmfiscout1No().toString();
                            form.setMessage(MessageType.SUCCESS,"匯出電文序號檔" + UpdateSuccess);
                        }
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後送外財金序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {
                    prepareAndSendEMSData(strMsg,form.getTxKind(),form.getKind());
                }
            }



        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }

    /**
     查詢並更新GetRMFISCIN1

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMFISCIN1(boolean needUpdated, UI_028040_Form form) {
        Rmfiscin1 defRMFISCIN1 = new Rmfiscin1();
        try {
            defRMFISCIN1.setRmfiscin1SenderBank(fiscNo);
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            defRMFISCIN1.setRmfiscin1ReceiverBank(_SYSSTAT_HBKNO);
            //defRMFISCIN1.RMFISCIN1_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            Rmfiscin1 result = rmService.getRMFISCIN1ByPK(defRMFISCIN1);

            if (result != null) {
                defRMFISCIN1 = result;
            }
            //add by Maxine on 2011/08/02 for 補送EMS
            String strMsg = "";

            if (result == null) {
                strMsg = "匯入電文序號檔(RMFISCIN1)無此資料";
                form.setMessage(MessageType.DANGER,"匯入電文序號檔" + QueryNoData);
            } else {
                form.setReceiverSeq(defRMFISCIN1.getRmfiscin1No().toString());
            }

            if (needUpdated) {//when KIND = 2為
                if (StringUtils.isNotBlank(form.getNewReceiverSeq())) {
                    defRMFISCIN1.setRmfiscin1No(Integer.parseInt(form.getNewReceiverSeq()));
                    if (rmService.updateRMFISCIN1ByPK(defRMFISCIN1) < 1) {
                        strMsg = "匯入電文序號檔(RMFISCIN1)" + UpdateFail;
                        form.setMessage(MessageType.DANGER,"匯入電文序號檔" + UpdateFail);
                    } else {
                        strMsg = "=" + defRMFISCIN1.getRmfiscin1No().toString();
                        form.setMessage(MessageType.SUCCESS,"匯入電文序號檔" + UpdateSuccess);
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後來自財金序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {

                    prepareAndSendEMSData(strMsg,form.getTxKind(),form.getKind());
                }
            }

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }

    /**
     查詢並更新GetRMFISCOUT4

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMFISCOUT4(boolean needUpdated, UI_028040_Form form) {

        Rmfiscout4 defRMFISCOUT4 = new Rmfiscout4();
        try {
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            defRMFISCOUT4.setRmfiscout4SenderBank(_SYSSTAT_HBKNO);
            //defRMFISCOUT4.RMFISCOUT4_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRMFISCOUT4.setRmfiscout4ReceiverBank(fiscNo);
            Rmfiscout4 result = rmService.getRMFISCOUT4ByPK(defRMFISCOUT4);
            if (result != null) {
                defRMFISCOUT4 = result;
            }
            //add by Maxine on 2011/08/02 for 補送EMS
            String strMsg = "";

            if (result == null) {
                strMsg = "一般通訊匯出電文序號檔(RMFISCOUT4)無此資料";
                form.setMessage(MessageType.DANGER,"一般通訊匯出電文序號檔" + QueryNoData);
            } else {
                form.setSenderSeq(defRMFISCOUT4.getRmfiscout4No().toString());
            }
            if (needUpdated) {//when KIND = 1為true
                if (StringUtils.isNotBlank(form.getNewSenderSeq())) {
                    defRMFISCOUT4.setRmfiscout4No(Integer.parseInt(form.getNewSenderSeq()));
                    if (rmService.updateRMFISCOUT4ByPK(defRMFISCOUT4) < 1) {
                        strMsg = "一般通訊匯出電文序號檔(RMFISCOUT4)" + UpdateFail;
                        form.setMessage(MessageType.DANGER,"一般通訊匯出電文序號檔" + UpdateFail);
                    } else {
                        strMsg = "=" + defRMFISCOUT4.getRmfiscout4No().toString();
                        form.setMessage(MessageType.SUCCESS,"一般通訊匯出電文序號檔" + UpdateSuccess);
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後送往財金序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {
                    prepareAndSendEMSData(strMsg,form.getTxKind(),form.getKind());
                }
            }


        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }

    /**
     查詢並更新GetRMFISCIN4

     @param needUpdated 判斷是否需要更新資料庫的flag
     */
    private void getRMFISCIN4(boolean needUpdated, UI_028040_Form form) {

        Rmfiscin4 defRMFISCIN4 = new Rmfiscin4();
        try {
            defRMFISCIN4.setRmfiscin4SenderBank(fiscNo);
            //modified by maxine on 2011/06/24 for SYSSTAT自行查
            defRMFISCIN4.setRmfiscin4ReceiverBank(_SYSSTAT_HBKNO);
            //defRMFISCIN4.RMFISCIN4_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
            Rmfiscin4 result = rmService.getRMFISCIN4ByPK(defRMFISCIN4);

            if (result != null) {
                defRMFISCIN4 = result;
            }


            //add by Maxine on 2011/08/02 for 補送EMS
            String strMsg = "";

            if (result == null) {
                strMsg = "一般通訊匯入電文序號檔(RMFISCIN4)無此資料";
                form.setMessage(MessageType.DANGER,"一般通訊匯入電文序號檔" + QueryNoData);
            } else {
                form.setReceiverSeq(defRMFISCIN4.getRmfiscin4No().toString());
            }
            if (needUpdated) {//when KIND = 4為true
                if (StringUtils.isNotBlank(form.getNewReceiverSeq())) {
                    defRMFISCIN4.setRmfiscin4No(Integer.parseInt(form.getNewReceiverSeq()));
                    if (rmService.updateRMFISCIN4ByPK(defRMFISCIN4) < 1) {
                        strMsg = "一般通訊匯入電文序號檔(RMFISCIN4)" + UpdateFail;
                        form.setMessage(MessageType.DANGER,"一般通訊匯入電文序號檔" + UpdateFail);
                    } else {
                        strMsg = "=" + defRMFISCIN4.getRmfiscin4No().toString();
                        form.setMessage(MessageType.SUCCESS,"一般通訊匯入電文序號檔" + UpdateSuccess);
                    }
                } else {
                    form.setMessage(MessageType.DANGER,"請輸入調整後來自財金序號");
                }

                //add by Maxine on 2011/08/02 for 補送EMS
                if (strMsg.length() > 0) {
                    prepareAndSendEMSData(strMsg,form.getTxKind(),form.getKind());
                }
            }

        } catch (Exception ex) {
            form.setMessage(MessageType.DANGER,ex.toString());
        }
    }


    private void prepareAndSendEMSData(String strMsg, String txKind, String kind) throws Exception {
        String txKindItem = "";
        String kindItem = "";
        switch (txKind){
            case "1":
                txKindItem = "一般匯款";
                break;
            case "4":
                txKindItem = "一般通訊";
                break;
            default:
                break;
        }
        switch (kind){
            case "1":
                kindItem = "送往財金";
                break;
            case "2":
                kindItem = "來自財金";
                break;
            default:
                break;
        }



        LogData logContext = new LogData();

        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName("UI_028040");
        logContext.setTxDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId("UI028040");
        logContext.setMessageGroup("4"); // /*RM*/
        logContext.setRemark(StringUtils.join(txKindItem , "-" , kindItem , "-調整為(" , strMsg , ")"));
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangeFISCSno);
        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
        //FEPBase.SendEMS(logContext)
    }
}