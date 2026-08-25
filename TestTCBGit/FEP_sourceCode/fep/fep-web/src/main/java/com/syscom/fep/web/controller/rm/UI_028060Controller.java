package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.ext.mapper.RmouttExtMapper;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.constant.RMOUTStatus;
import com.syscom.fep.vo.enums.RMReturnCode;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028060_Form;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 負責處理UI028060調整匯兌收送狀態交易
 *
 * @author xingyun_yang
 * @create 2021/11/10
 */
@Controller
public class UI_028060Controller extends BaseController {

    @Autowired
    RmService rmService;
    @Autowired
    RmouttExtMapper rmouttExtMapper;

    private Boolean successFlag = true;
    private static final String _programID = "UI_028060";
    private String modifyStatus = StringUtils.EMPTY;

    @Override
    public void pageOnLoad(ModelMap mode) {
        // 初始化表單數據
        UI_028060_Form form = new UI_028060_Form();
        //add by Maxine on 2011/06/24 for SYSSTAT自行查
        try {
            querySYSSTAT(mode);
            getRMSTAT(mode, form);
            //Fly 2017/11/29 Fortify Open Redirect問題修正
            logContext.setProgramName("UI_028060");
            logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        } catch (Exception e) {
        	this.errorMessage(e, e.getMessage());
        	this.showMessage(mode, MessageType.DANGER, programError);
        }
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    /**
     * add by Maxine on 2011/06/24 for SYSSTAT自行查
     */
    private void querySYSSTAT(ModelMap mode) throws Exception {
        try {
            List<Sysstat> _dtSYSSTAT = rmService.getStatus();
            if (_dtSYSSTAT.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料!!");
                return;
            }
//            @SuppressWarnings("unused")
//            String _SYSSTAT_HBKNO = _dtSYSSTAT.get(0).getSysstatHbkno();
//            @SuppressWarnings("unused")
//            String _SYSSTAT_FBKNO = _dtSYSSTAT.get(0).getSysstatFbkno();
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    @PostMapping(value = "/rm/UI_028060/executeClick")
    protected String executeClick(@ModelAttribute UI_028060_Form form, ModelMap mode) throws Exception {
        this.infoMessage("執行UI_028060, 條件 = [", form.toString(), "]");
        this.doKeepFormData(mode, form);
        Rmstat defRmstat = new Rmstat();
        Prgstat defPrgstat = new Prgstat();
        String _SYSSTAT_HBKNO = SysStatus.getPropertyValue().getSysstatHbkno();
        try {
            logContext.setRemark("開始執行UI_028060, 調整項目=" + form.getKind() + ", 調整交易類別=" + form.getTxKind() +
                    ", FISCO_FLAG1=" + form.getfISCO_FLAG1() + ", FISCI_FLAG1=" + form.getfISCI_FLAG1() +
                    ", FISCO_FLAG4=" + form.getfISCO_FLAG1() + ", FISCI_FLAG4=" + form.getfISCI_FLAG4()
            );
            logMessage(Level.INFO, logContext);
            successFlag = true;
            if (successFlag) {
                //Jim, 2012/7/23, KIND:送往財金/來自財金/送往財金,不自動調整序號, TX_KIND:匯款/一般通訊
                if (!"".equals(form.getKind())) {
                    switch (form.getKind()) {
                        //TO FISC
                        case "1":
                        case "3":
                            //RM
                            if ("1".equals(form.getTxKind())) {
                                modifyStatus = form.getfISCO_FLAG1().toString();
                                if ("Y".equals(form.getfISCO_FLAG1().toString())) {
                                    if (checkRMSTAT(mode, form) && checkPRGSTAT(mode, form) && checkRMOUTT(mode, form)) {
                                        defRmstat.setRmstatFiscoFlag1(form.getfISCO_FLAG1());
                                        //Jim, 2012/7/23, 準備開始更新相關table前，先更新PRGSTAT狀態
                                        defPrgstat.setPrgstatProgramid(_programID);
                                        defPrgstat.setPrgstatFlag(1);
                                        if (rmService.updatePRGSTATByPK(defPrgstat) < 1) {
                                            logContext.setRemark("更新PRGSTAT_FLAG=" + defPrgstat.getPrgstatFlag() + "失敗");
                                            logMessage(Level.INFO, logContext);
                                        }
                                    }else {
                                        return Router.UI_028060.getView();
                                    }
                                } else {
                                    defRmstat.setRmstatFiscoFlag1(form.getfISCO_FLAG1());
                                }
                            } else {
                                //MSG
                                modifyStatus = form.getfISCO_FLAG4();
                                if ("Y".equals(form.getfISCO_FLAG4())) {
                                    if (checkMSGOUT(mode, form)) {
                                        defRmstat.setRmstatFiscoFlag4(form.getfISCO_FLAG4());
                                    }
                                } else {
                                    defRmstat.setRmstatFiscoFlag4(form.getfISCO_FLAG4());
                                }
                            }
                            break;
                        case "2":
                            //Form fisc
                            if ("1".equals(form.getTxKind())) {
                                modifyStatus = form.getfISCI_FLAG1();
                                defRmstat.setRmstatFisciFlag1(form.getfISCI_FLAG1());
                            } else {
                                modifyStatus = form.getfISCI_FLAG4();
                                defRmstat.setRmstatFisciFlag4(form.getfISCI_FLAG4());
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (successFlag) {
                    if ("1".equals(form.getTxKind())) {
                        //Jim, 2012/2/24, 調整收送狀態 = N, 暫停時不要UPDATE RMFISCOUT1
                        if ("Y".equals(form.getfISCO_FLAG1())) {
                            checkRMFISCOUT1(mode, form);
                            if (successFlag) {
                                updateRMOUTSNO();
                            }
                        } else {
                            //MSG
                            checkRMFISCOUT4(mode, form);
                        }
                    }
                }
                if (successFlag) {
                    //modified by Maxine on 2011/06/24 for SYSSTAT自行查
                    defRmstat.setRmstatHbkno(_SYSSTAT_HBKNO);
                    //defRMSTAT.RMSTAT_HBKNO = SysStatus.PropertyValue.SYSSTAT_HBKNO
                    if (rmService.updateRMSTATbyPK(defRmstat) < 1) {
                        //add by maxine on 2011/08/02 for 補送EMS
                        prepareAndSendEMSData("匯款狀態檔" + UpdateFail, form);
                        this.showMessage(mode, MessageType.DANGER, "匯款狀態檔" + UpdateFail);
                        successFlag = false;
                    }
                }
                if (successFlag) {
                    //add by maxine on 2011/08/02 for 補送EMS
                    prepareAndSendEMSData("調整成功", form);
                    this.showMessage(mode, MessageType.INFO, DealSuccess);
                }
            }
            if ("1".equals(form.getKind()) || "3".equals(form.getKind()) && "1".equals(form.getTxKind()) && "Y".equals(form.getfISCO_FLAG1())) {
                defPrgstat.setPrgstatProgramid(_programID);
                defPrgstat.setPrgstatFlag(0);
                rmService.updatePRGSTATByPK(defPrgstat);
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        } finally {
        	try {
        		getRMSTAT(mode, form);
	        } catch (Exception ex) {
	            this.showMessage(mode, MessageType.DANGER, ex);
	        }
            
        }
        return Router.UI_028060.getView();
    }

    /**
     * page load時取得RMSTAT 匯入/出 相關Flag欄位
     */
    private void getRMSTAT(ModelMap mode, UI_028060_Form form) throws Exception {
        Rmstat defRmstat = new Rmstat();
        String _SYSSTAT_HBKNO = SysStatus.getPropertyValue().getSysstatHbkno();
        try {
            //modified by Maxine on 2011/06/24 for SYSSTAT自行查
            defRmstat.setRmstatHbkno(_SYSSTAT_HBKNO);
            //defRMSTAT.RMSTAT_HBKNO = SysStatus.PropertyValue.SYSSTAT_HBKNO
            defRmstat = rmService.getRmstatByPk(defRmstat);
            if (defRmstat == null) {
                this.showMessage(mode, MessageType.INFO, "匯款狀態檔" + QueryNoData);
                successFlag = false;
            } else {
                if ("Y".equals(defRmstat.getRmstatFiscoFlag1()) || "N".equals(defRmstat.getRmstatFiscoFlag1())) {
                    form.setfISCO_FLAG1(defRmstat.getRmstatFiscoFlag1());
                } else {
                    form.setfISCO_FLAG1("");
                }
                if ("Y".equals(defRmstat.getRmstatFisciFlag1()) || "N".equals(defRmstat.getRmstatFisciFlag1())) {
                    form.setfISCI_FLAG1(defRmstat.getRmstatFisciFlag1());
                } else {
                    form.setfISCI_FLAG1("");
                }
                if ("Y".equals(defRmstat.getRmstatFiscoFlag4()) || "N".equals(defRmstat.getRmstatFiscoFlag4())) {
                    form.setfISCO_FLAG4(defRmstat.getRmstatFiscoFlag4());
                } else {
                    form.setfISCO_FLAG4("");
                }
                if ("Y".equals(defRmstat.getRmstatFisciFlag4()) || "N".equals(defRmstat.getRmstatFisciFlag4())) {
                    form.setfISCI_FLAG4(defRmstat.getRmstatFisciFlag4());
                } else {
                    form.setfISCI_FLAG4("");
                }
            }
            //Jim, 2012/7/23, 增加PRGSTAT狀態查詢
            Prgstat defPrgstat = new Prgstat();
            defPrgstat.setPrgstatProgramid("UI_028060");
            defPrgstat = rmService.getPRGSTATByPK(defPrgstat);
            if (defPrgstat != null) {
                switch (defPrgstat.getPrgstatFlag().toString()) {
                    case "0":
                        mode.addAttribute("PRGSTAT_FLAG", " 0-未執行");
                        break;
                    case "1":
                        mode.addAttribute("PRGSTAT_FLAG", " 1-執行中");
                        mode.addAttribute("PRGSTAT_FLAGColor", "red");
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    /**
     * 檢查RMOUT是否仍有傳送中的資料
     */
    private Boolean checkRMOUTT(ModelMap mode, UI_028060_Form form) throws Exception {
        Rmout defRmout = new Rmout();
        defRmout.setRmoutTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        defRmout.setRmoutStat(RMOUTStatus.Transfered);
        List<Rmout> dt = rmService.getRmoutByDef(defRmout);
        if (dt.size() > 0) {
            successFlag = false;
            this.showMessage(mode, MessageType.INFO, "匯出主檔尚有匯出中資料, 請查明後再調整收送狀態");
            //add by maxine on 2011/08/02 for 補送EMS
            prepareAndSendEMSData("匯出主檔尚有匯出中資料, 請查明後再調整收送狀態", form);
            return false;
        }
        return true;
    }

    private Boolean checkMSGOUT(ModelMap mode, UI_028060_Form form) throws Exception {
        List<Msgout> dt = rmService.getMSGOUTBySTAT(RMOUTStatus.Transferring);
        if (dt.size() > 0) {
            successFlag = false;
            this.showMessage(mode, MessageType.INFO, "一般通訊匯出主檔尚有匯出中資料, 請查明後再調整收送狀態");
            //add by maxine on 2011/08/02 for 補送EMS
            prepareAndSendEMSData("一般通訊匯出主檔尚有匯出中資料, 請查明後再調整收送狀態", form);
            return false;
        }
        return true;
    }

    /**
     * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
     */
    private void checkRMFISCOUT1(ModelMap mode, UI_028060_Form form) throws Exception {
        String _SYSSTAT_FBKNO = SysStatus.getPropertyValue().getSysstatFbkno();
        String _SYSSTAT_HBKNO = SysStatus.getPropertyValue().getSysstatHbkno();
        Rmfiscout1 defRMFISCOUT1 = new Rmfiscout1();
        //Fly 2015/05/11 因RMFISCOUT1 Block 造成FEDIGW SELECT RMOUT Timeout 把RMFISCOUT1拆二筆
        Rmfiscout1 defRMFISCOUT1_999 = new Rmfiscout1();

        //modified by Maxine on 2011/06/24 for SYSSTAT自行查
        defRMFISCOUT1.setRmfiscout1SenderBank(_SYSSTAT_HBKNO);
        defRMFISCOUT1.setRmfiscout1ReceiverBank(_SYSSTAT_FBKNO);

        defRMFISCOUT1_999.setRmfiscout1SenderBank(_SYSSTAT_HBKNO);
        defRMFISCOUT1_999.setRmfiscout1ReceiverBank("999");
        //defRMFISCOUT1.RMFISCOUT1_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        //defRMFISCOUT1.RMFISCOUT1_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_FBKNO
        List<HashMap<String, Object>> defRMFISCOUT1list = rmService.getRMFISCOUT1ByPK028060280150(defRMFISCOUT1);
        if (defRMFISCOUT1list.size() < 1) {
            //add by maxine on 2011/08/02 for 補送EMS
            prepareAndSendEMSData("匯出電文序號檔" + QueryNoData, form);
            this.showMessage(mode, MessageType.DANGER, "匯出電文序號檔" + QueryNoData);
            successFlag = false;
        } else {
            List<HashMap<String, Object>> defRMFISCOUT1_999list = rmService.getRMFISCOUT1ByPK028060280150(defRMFISCOUT1_999);
            if (defRMFISCOUT1_999list.size() < 1) {
                prepareAndSendEMSData("匯出電文序號檔" + QueryNoData, form);
                this.showMessage(mode, MessageType.DANGER, "匯出電文序號檔" + QueryNoData);
                successFlag = false;
            } else {
                logContext.setRemark("Before update RMFISCOUT1, RMFISCOUT1_NO=" + defRMFISCOUT1list.get(0).get("RMFISCOUT1_NO") +
                        ", RMFISCOUT1_999_REP_NO=" + defRMFISCOUT1_999list.get(0).get("RMFISCOUT1_REP_NO"));

                //Jim, 2012/2/24, SPEC modify, 一律重算不要設條件
                //If defRMFISCOUT1.RMFISCOUT1_NO <> defRMFISCOUT1.RMFISCOUT1_REP_NO Then
                //Modify by Jim, 2011/06/07, 多筆匯出因REP_NO UPDATE不一致, 改直接抓取匯出成功的最大電文序號

                Rmoutt defRmoutt = new Rmoutt();
                defRmoutt.setRmouttTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
                defRmoutt.setRmouttFiscRtnCode(NormalRC.FISC_OK);
                defRmoutt.setRmouttFepno(null);
                List<Rmoutt> defRmouttList = rmService.getRMOUTTbyDef(defRmoutt);
                if (defRmouttList.size() < 1) {
                    //add by maxine on 2011/08/02 for 補送EMS
                    //PrepareAndSendEMSData("匯出主檔" & QueryNoData)
                    //QueryStatusBar.ShowMessage("匯出主檔" & QueryNoData, StatusBar.MessageType.ErrMsg)
                    //successFlag = False
                    //Return
                    //如果找不到成功的RMOUT資料，則更新成0
                    defRMFISCOUT1.setRmfiscout1No(0);
                    //defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
                    defRMFISCOUT1_999.setRmfiscout1RepNo(0);
                } else {
                    defRmouttList = defRmouttList.stream().sorted(Comparator.comparing(Rmoutt::getRmouttFiscsno).reversed()).collect(Collectors.toList());
                    //如果找不到成功的RMOUT資料，則更新成0
                    if (defRmouttList.size() > 0) {
                        String maxFISCSNO = defRmouttList.get(0).getRmouttFiscsno();
                        defRMFISCOUT1.setRmfiscout1No(Integer.parseInt(maxFISCSNO));
                        //defRMFISCOUT1.RMFISCOUT1_REP_NO = defRMFISCOUT1.RMFISCOUT1_NO
                        defRMFISCOUT1_999.setRmfiscout1RepNo(defRMFISCOUT1.getRmfiscout1No());
                    } else {
                        defRMFISCOUT1.setRmfiscout1No(0);
                        //defRMFISCOUT1.RMFISCOUT1_REP_NO = 0
                        defRMFISCOUT1_999.setRmfiscout1RepNo(0);
                    }
                }
                //End Modify
                if (rmService.updateRMFISCOUT1ByPK(defRMFISCOUT1) < 1) {
                    //'add by maxine on 2011/08/02 for 補送EMS
                    prepareAndSendEMSData("匯出電文序號檔" + UpdateFail, form);
                    this.showMessage(mode, MessageType.DANGER, "匯出電文序號檔" + UpdateFail);
                    successFlag = false;
                }

                if (rmService.updateRMFISCOUT1ByPK(defRMFISCOUT1_999) < 1) {
                    //'add by maxine on 2011/08/02 for 補送EMS
                    prepareAndSendEMSData("匯出電文序號檔" + UpdateFail, form);
                    this.showMessage(mode, MessageType.DANGER, "匯出電文序號檔" + UpdateFail);
                    successFlag = false;
                }

                logContext.setRemark("After update RMFISCOUT1, RMFISCOUT1_NO=" + defRMFISCOUT1.getRmfiscout1No() + ", RMFISCOUT1_999_REP_NO="
                        + defRMFISCOUT1_999.getRmfiscout1RepNo());
                logMessage(Level.INFO, logContext);
            }
        }
    }

    /**
     * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
     */
    private void checkRMFISCOUT4(ModelMap mode, UI_028060_Form form) throws Exception {
        String _SYSSTAT_HBKNO = SysStatus.getPropertyValue().getSysstatHbkno();
        String _SYSSTAT_FBKNO = SysStatus.getPropertyValue().getSysstatFbkno();
        Rmfiscout4 defRmfiscout4 = new Rmfiscout4();
        //modified by Maxine on 2011/06/24 for SYSSTAT自行查
        defRmfiscout4.setRmfiscout4SenderBank(_SYSSTAT_HBKNO);
        defRmfiscout4.setRmfiscout4ReceiverBank(_SYSSTAT_FBKNO);
        //defRMFISCOUT4.RMFISCOUT4_SENDER_BANK = SysStatus.PropertyValue.SYSSTAT_HBKNO
        //defRMFISCOUT4.RMFISCOUT4_RECEIVER_BANK = SysStatus.PropertyValue.SYSSTAT_FBKNO
        if (rmService.getRMFISCOUT4ByPK028060280150(defRmfiscout4).size() < 1) {
            prepareAndSendEMSData("一般通訊匯出電文序號檔" + QueryNoData, form);
            this.showMessage(mode, MessageType.DANGER, "一般通訊匯出電文序號檔" + QueryNoData);
            successFlag = false;
        }
    }

    private void updateRMOUTSNO() throws Exception {
        //Modify by Jim, 2011/06/07, 以匯出主檔的此解款行的最大通匯序號來更新RMOUTSNO
        Integer updateNO;
        Rmoutt defRmoutt = new Rmoutt();
        Rmoutt defRmoutt2 = new Rmoutt();
        Rmoutsno defRmoutsno = new Rmoutsno();
        List<Rmoutsno> dtResult = rmService.getRMOUTSNOByNONotEqualREPNO();
        for (Rmoutsno rmoutsno : dtResult) {
            defRmoutt.setRmouttTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            defRmoutt.setRmouttFiscRtnCode(NormalRC.FISC_OK);
            //Jim, 2012/1/18, 修改更新邏輯，要考慮共用中心的case
            defRmoutt2 = rmouttExtMapper.getRMSNOForUpdateRMOUTSNO(defRmoutt.getRmouttTxdate(), defRmoutt.getRmouttFiscRtnCode(), rmoutsno.getRmoutsnoReceiverBank());
            //查詢正確的RMSNO for 更新RMOUTSNO  vb.net中的 line498  GetRMSNOForUpdateRMOUTSNO()
            if (defRmoutt2 != null) {
                updateNO = Integer.parseInt(defRmoutt.getRmouttRmsno());
            } else {
                updateNO = 0;
            }
            if (updateNO >= 0) {
                defRmoutsno.setRmoutsnoNo(updateNO);
                defRmoutsno.setRmoutsnoRepNo(updateNO);
            }
            // dtRMOUT = _obj.GetRMOUTbyDef(defRMOUT)
            // drRMOUT = dtRMOUT.Select("RMOUT_FISC_RTN_CODE='0001' AND SUBSTRING(RMOUT_RECEIVER_BANK,1,3)='" & dr.Item("RMOUTSNO_RECEIVER_BANK").ToString & "'", "RMOUT_RMSNO DESC")
            // ' '如果找不到成功的RMOUT資料，則更新成0
            // logContext.Remark = "Before update RMOUTSNO where RMOUTSNO_RECEIVER_BANK=" & dr.Item("RMOUTSNO_RECEIVER_BANK").ToString & ", RMOUTSNO_NO=" & dr.Item("RMOUTSNO_NO").ToString & ", RMOUTSNO_REP_NO=" & dr.Item("RMOUTSNO_REP_NO").ToString
            // _obj.LogMessage(logContext, LogLe
            // If drRMOUT.Count = 0 Then
            //     'logContext.Remark = "RMOUTSNO_RECEIVER_BANK=" & dr.Item("RMOUTSNO_RECEIVER_BANK").ToString & " & RMOUT_FISC_RTN_CODE=0001, 查無資料"
            //     '_obj.LogMessage(logContext, Syscom.FEP10.LogLevel.Info)
            //     'Continue For
            //     'dr.Item("RMOUTSNO_NO") = 0
            //     'dr.Item("RMOUTSNO_REP_NO") = 0
            //     defRMOUTSNO.RMOUTSNO_NO = 0
            //     defRMOUTSNO.RMOUTSNO_REP_NO = 0
            // Else
            //     'dr.Item("RMOUTSNO_NO") = drRMOUT(0).Item("RMOUT_RMSNO")
            //     'dr.Item("RMOUTSNO_REP_NO") = drRMOUT(0).Item("RMOUT_RMSNO")
            //     Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_NO)
            //     Decimal.TryParse(drRMOUT(0).Item("RMOUT_RMSNO").ToString, defRMOUTSNO.RMOUTSNO_REP_NO)
            // End If
            defRmoutsno.setRmoutsnoSenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            defRmoutsno.setRmoutsnoReceiverBank(rmoutsno.getRmoutsnoReceiverBank());
            if (rmService.updateRMOUTSNOByPK(defRmoutsno) < 1) {
                logContext.setRemark("After update RMOUTSNO where RMOUTSNO_RECEIVER_BANK=" + rmoutsno.getRmoutsnoReceiverBank() + ", update 0 筆");
                logMessage(Level.DEBUG, logContext);
            } else {
                logContext.setRemark("After update RMOUTSNO where RMOUTSNO_RECEIVER_BANK=" + rmoutsno.getRmoutsnoReceiverBank() +
                        ", update RMOUTSNO_NO=" + rmoutsno.getRmoutsnoNo() + ", RMOUTSNO_REP_NO=" + rmoutsno.getRmoutsnoRepNo());
                ;
                logMessage(Level.DEBUG, logContext);
            }
            // End Modify
            // im ires As Integer = _obj.UpdateRMOUTSNObyDataSet(dtResult)
            // ogContext.Remark = "update RMOUTSNO count=" & ires
            // obj.LogMessage(logContext, LogLevel.Info)
            // f ires <> dtResult.Rows.Count Then
            //    'QueryStatusBar.ShowMessage("匯出通匯序號檔" & UpdateFail, StatusBar.MessageType.ErrMsg)
            // nd If
        }
    }

    /**
     * 執行成功送成功信息
     */
    private void prepareAndSendEMSData(String strMsg, UI_028060_Form form) throws Exception {
        logContext.setChannel(FEPChannel.FEP);
        logContext.setSubSys(SubSystem.RM);
        logContext.setProgramName(_programID);
        logContext.setTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        logContext.setDesBkno(SysStatus.getPropertyValue().getSysstatHbkno());
        logContext.setMessageId(_programID);
        logContext.setMessageGroup("4");
        //logContext.MessageParm13 = KIND.SelectedItem.Text + "-調整為[" + modifyStatus + "], " + strMsg
        logContext.setRemark(form.getKind() + "-調整為(" + modifyStatus + "), " + strMsg);
        logContext.setTxUser(WebUtil.getUser().getUserId());
        logContext.setReturnCode(RMReturnCode.ChangeServiceStatus);
        //rmSrv.LogMessage(logContext, Syscom.FEP10.LogLevel.Info)
        //FEPBase.SendEMS(logContext)

        TxHelper.getRCFromErrorCode(logContext.getReturnCode(), FEPChannel.FEP, logContext);
    }

    /**
     * 檢查RMSTAT目前是否暫停
     */
    private Boolean checkRMSTAT(ModelMap mode, UI_028060_Form form) throws Exception {
        Rmstat defRmstat = new Rmstat();
        defRmstat.setRmstatHbkno(SysStatus.getPropertyValue().getSysstatHbkno());
        Rmstat record = rmService.getRmstatByPk(defRmstat);
        if (record != null) {
            if (!"N".equals(record.getRmstatFiscoFlag1())) {
                successFlag = false;
                this.showMessage(mode, MessageType.INFO, "調整送往財金可匯出並未暫停, 請查明後再調整收送狀態");
                prepareAndSendEMSData("調整送往財金可匯出並未暫停, 請查明後再調整收送狀態", form);
                return false;
            }
        } else {
            logContext.setRemark("CheckRMSTAT, GetRMSTATByPK查無資料, RMSTAT_HBKNO=" + defRmstat.getRmstatHbkno());
            logContext.setProgramName(_programID);
            logMessage(Level.DEBUG, logContext);
        }
        return true;
    }

    /**
     * 檢查PRGSTAT
     */
    private Boolean checkPRGSTAT(ModelMap mode, UI_028060_Form form) throws Exception {
        Prgstat defPrgstat = new Prgstat();
        defPrgstat.setPrgstatProgramid(_programID);
        Prgstat record = rmService.getPRGSTATByPK(defPrgstat);
        if (record != null) {
            if ("1".equals(defPrgstat.getPrgstatFlag().toString())) {
                successFlag = false;
                this.showMessage(mode, MessageType.INFO, "已有其他使用者正在執行本程式, 請查明後再調整收送狀態");
                prepareAndSendEMSData("已有其他使用者正在執行本程式, 請查明後再調整收送狀態", form);
                return false;
            }
        } else {
            logContext.setRemark("CheckPRGSTAT, GetPRGSTATByPK查無資料, PRGSTAT_PROGRAMID=" + defPrgstat.getPrgstatProgramid());
            logContext.setProgramName(_programID);
            logMessage(Level.DEBUG, logContext);
        }
        return true;
    }
}