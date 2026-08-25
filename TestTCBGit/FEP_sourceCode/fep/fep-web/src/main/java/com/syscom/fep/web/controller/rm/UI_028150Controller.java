package com.syscom.fep.web.controller.rm;

import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.mybatis.model.*;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.rm.UI_028150_Form;
import com.syscom.fep.web.service.AtmService;
import com.syscom.fep.web.service.RmService;
import com.syscom.fep.web.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * 匯出/匯入狀態監控畫面
 * <p>
 * 負責處理OP 發動匯出匯入狀態監控交易。
 *
 * @author xingyun_yang
 * @create 2021/10/29
 */
@Controller
public class UI_028150Controller extends BaseController {

    @Autowired
    RmService rmService;
    @Autowired
    AtmService atmService;

    LogData logContext = new LogData();
    //該行總行
//    private static final String BRNO = "000";
    //顯示5筆
    public static final Integer TOP_NUMBER = 5;
    //    private static final String _StrNormal = "Y-正常";
    private static final String _StrConfirm = "Y-許可";
    private static final String _StrStop = "N-暫停";
    //   'Jim, 2012/6//19, 新增第二層統計資料
    //    'Fly 2014/11/18 增加新網銀MMAB2B(Cnt4)


    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_028150_Form form = new UI_028150_Form();
        queryData(mode, form);
        updateDataPnl1_Tick(mode);
        String time = "30";
        form.setTime(time);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }

    //載入資料
    protected void queryData(ModelMap mode, UI_028150_Form form) {
        bindGrid(mode, form);
        getSummaryCnt(mode);
    }

    private void getSummaryCnt(ModelMap mode) {
        List<HashMap<String, Object>> dtRmout = null;
        List<HashMap<String, Object>> dt1172 = null;
        List<HashMap<String, Object>> dtAutoBack = null;
        List<HashMap<String, Object>> dtAutoBackBack = null;
        StringBuilder autoBackBank;
        String wkOUT_Cnt0, wkOUT_Cnt1, wkOUT_Cnt2, wkOUT_Cnt3, wkOUT_Cnt4, wkOUT_Cnt5, wkOUT_Cnt6, wkOUT_Cnt7;
        String wkIN_Cnt0, wkIN_Cnt1, wkIN_Cnt2, wkIN_Cnt4, wkIN_Cnt5, wkIN_Cnt7;
        String wkIN_A_Cnt0, wkIN_A_Cnt1, wkIN_A_Cnt2, wkIN_A_Cnt4, wkIN_A_Cnt5, wkIN_A_Cnt7;
        try {
            wkOUT_Cnt0 = "0";
            wkOUT_Cnt1 = "0";
            wkOUT_Cnt2 = "0";
            wkOUT_Cnt3 = "0";
            wkOUT_Cnt4 = "0";
            wkOUT_Cnt5 = "0";
            wkOUT_Cnt6 = "0";
            wkOUT_Cnt7 = "0";
            //Fly 2017/02/24 當發生deadlock時再retry一次 (2017/2/10 (週五) 下午 04:08)
            try {
                dtRmout = rmService.getRMOUTSummaryCnt();
            } catch (Exception ex) {
                if (ex.toString().contains("deadlocked")) {
                    logContext.setRemark(ex.toString());
                    this.logMessage(Level.INFO, logContext);
                    dtRmout = rmService.getRMOUTSummaryCnt();
                } else {
                    throw ex;
                }
            }
            if (dtRmout != null) {
                for (HashMap<String, Object> row : dtRmout) {
                    switch (String.valueOf(row.get("RMOUT_ORIGINAL"))) {
                        case "0":
                            wkOUT_Cnt0 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt0 : String.valueOf(row.get("CNT"));
                            break;
                        case "1":
                            wkOUT_Cnt1 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt1 : String.valueOf(row.get("CNT"));
                            break;
                        case "2":
                            wkOUT_Cnt2 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt2 : String.valueOf(row.get("CNT"));
                            break;
                        case "3":
                            wkOUT_Cnt3 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt3 : String.valueOf(row.get("CNT"));
                            break;
                        case "4":
                            wkOUT_Cnt4 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt4 : String.valueOf(row.get("CNT"));
                            break;
                        case "5":
                            wkOUT_Cnt5 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt5 : String.valueOf(row.get("CNT"));
                            break;
                        case "6":
                            wkOUT_Cnt6 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt6 : String.valueOf(row.get("CNT"));
                            break;
                        case "7":
                            wkOUT_Cnt7 = String.valueOf(row.get("CNT")).equals("null") ? wkOUT_Cnt7 : String.valueOf(row.get("CNT"));
                            break;
                        default:
                            break;
                    }
                }
            }
            mode.addAttribute("wkOUT_Cnt0", wkOUT_Cnt0);
            mode.addAttribute("wkOUT_Cnt1", wkOUT_Cnt1);
            mode.addAttribute("wkOUT_Cnt2", wkOUT_Cnt2);
            mode.addAttribute("wkOUT_Cnt3", wkOUT_Cnt3);
            mode.addAttribute("wkOUT_Cnt4", wkOUT_Cnt4);
            mode.addAttribute("wkOUT_Cnt5", wkOUT_Cnt5);
            mode.addAttribute("wkOUT_Cnt6", wkOUT_Cnt6);
            mode.addAttribute("wkOUT_Cnt7", wkOUT_Cnt7);
            wkIN_Cnt0 = "0";
            wkIN_Cnt1 = "0";
            wkIN_Cnt2 = "0";
            wkIN_Cnt4 = "0";
            wkIN_Cnt5 = "0";
            wkIN_Cnt7 = "0";
            try {
                dt1172 = rmService.get1172SummaryCnt();
            } catch (Exception ex) {
                if (ex.toString().contains("deadlocked")) {
                    logContext.setRemark(ex.toString());
                    this.logMessage(Level.INFO, logContext);
                    dt1172 = rmService.get1172SummaryCnt();
                } else {
                    throw ex;
                }
            }
            if (dt1172 != null) {
                for (HashMap<String, Object> row : dt1172) {
                    switch (String.valueOf(row.get("RMOUT_ORIGINAL"))) {
                        case "0":
                            wkIN_Cnt0 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt0 : String.valueOf(row.get("CNT"));
                            break;
                        case "1":
                            wkIN_Cnt1 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt1 : String.valueOf(row.get("CNT"));
                            break;
                        case "2":
                            wkIN_Cnt2 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt2 : String.valueOf(row.get("CNT"));
                            break;
                        case "4":
                            wkIN_Cnt4 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt4 : String.valueOf(row.get("CNT"));
                            break;
                        case "5":
                            wkIN_Cnt5 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt5 : String.valueOf(row.get("CNT"));
                            break;
                        case "7":
                            wkIN_Cnt7 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_Cnt7 : String.valueOf(row.get("CNT"));
                            break;
                        default:
                            break;
                    }
                }
            }
            mode.addAttribute("wkIN_Cnt0", wkIN_Cnt0);
            mode.addAttribute("wkIN_Cnt1", wkIN_Cnt1);
            mode.addAttribute("wkIN_Cnt2", wkIN_Cnt2);
            mode.addAttribute("wkIN_Cnt4", wkIN_Cnt4);
            mode.addAttribute("wkIN_Cnt5", wkIN_Cnt5);
            mode.addAttribute("wkIN_Cnt7", wkIN_Cnt7);
            wkIN_A_Cnt0 = "0";
            wkIN_A_Cnt1 = "0";
            wkIN_A_Cnt2 = "0";
            wkIN_A_Cnt4 = "0";
            wkIN_A_Cnt5 = "0";
            wkIN_A_Cnt7 = "0";
            try {
                dtAutoBack = rmService.getAutoBackSummaryCnt();
            } catch (Exception ex) {
                if (ex.toString().contains("deadlocked")) {
                    logContext.setRemark(ex.toString());
                    this.logMessage(Level.INFO, logContext);
                    dtAutoBack = rmService.getAutoBackSummaryCnt();
                } else {
                    throw ex;
                }
            }

            if (dtAutoBack != null) {
                for (HashMap<String, Object> row : dtAutoBack) {
                    switch (String.valueOf(row.get("RMOUT_ORIGINAL"))) {
                        case "0":
                            wkIN_A_Cnt0 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt0 : String.valueOf(row.get("CNT"));
                            break;
                        case "1":
                            wkIN_A_Cnt1 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt1 : String.valueOf(row.get("CNT"));
                            break;
                        case "2":
                            wkIN_A_Cnt2 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt2 : String.valueOf(row.get("CNT"));
                            break;
                        case "3":
                            wkIN_A_Cnt4 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt4 : String.valueOf(row.get("CNT"));
                            break;
                        case "4":
                            wkIN_A_Cnt5 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt5 : String.valueOf(row.get("CNT"));
                            break;
                        case "7":
                            wkIN_A_Cnt7 = String.valueOf(row.get("CNT")).equals("null") ? wkIN_A_Cnt7 : String.valueOf(row.get("CNT"));
                            break;
                        default:
                            break;
                    }
                }
            }
            mode.addAttribute("wkIN_A_Cnt0", wkIN_A_Cnt0);
            mode.addAttribute("wkIN_A_Cnt1", wkIN_A_Cnt1);
            mode.addAttribute("wkIN_A_Cnt2", wkIN_A_Cnt2);
            mode.addAttribute("wkIN_A_Cnt4", wkIN_A_Cnt4);
            mode.addAttribute("wkIN_A_Cnt5", wkIN_A_Cnt5);
            mode.addAttribute("wkIN_A_Cnt7", wkIN_A_Cnt7);
            autoBackBank = new StringBuilder();
            dtAutoBackBack = rmService.getAutoBackBank();
            if (dtAutoBackBack != null) {
                for (HashMap<String, Object> row : dtAutoBackBack) {
                    autoBackBank.append(StringUtils.join(String.valueOf(row.get("ALLBANK_BKNO")).equals("null") ? "" : String.valueOf(row.get("ALLBANK_BKNO")).toString(), ","));
                }
                if (autoBackBank.length() > 2) {
                    autoBackBank = new StringBuilder(autoBackBank.substring(0, autoBackBank.length() - 1));
                }
            }
            mode.addAttribute("autoBackBank", autoBackBank);
        } catch (Exception ex) {
            logContext.setProgramException(ex);
            this.logMessage(Level.INFO, logContext);
        }
    }

    private List<HashMap<String, Object>> getRmOut(ModelMap mode, UI_028150_Form form) {
        List<HashMap<String, Object>> dtRmout = null;
        try {
            String systemDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            dtRmout = rmService.getTopRMOUTByApdateSenderBank(TOP_NUMBER, systemDate, atmService.getStatus().getSysstatHbkno());
            List<HashMap<String, Object>> dtNewRmout = new ArrayList<>();
            for (int i = 0; i < dtRmout.size(); i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("RMOUT_SENDTIME", String.valueOf(dtRmout.get(i).get("RMOUT_SENDTIME")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_SENDTIME")));
                hashMap.put("RMOUT_FISC_SND_CODE", String.valueOf(dtRmout.get(i).get("RMOUT_FISC_SND_CODE")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_FISC_SND_CODE")));
                hashMap.put("RMOUT_BRNO", String.valueOf(dtRmout.get(i).get("RMOUT_BRNO")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_BRNO")));
                hashMap.put("RMOUT_ORIGINAL", String.valueOf(dtRmout.get(i).get("RMOUT_ORIGINAL")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_ORIGINAL")));
                hashMap.put("RMOUT_FEPNO", String.valueOf(dtRmout.get(i).get("RMOUT_FEPNO")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_FEPNO")));
                hashMap.put("RMOUT_FISCSNO", String.valueOf(dtRmout.get(i).get("RMOUT_FISCSNO")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_FISCSNO")));
                hashMap.put("RMOUT_TXAMT", String.valueOf(dtRmout.get(i).get("RMOUT_TXAMT")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_TXAMT")));
                hashMap.put("RMOUT_RECEIVER_BANK", String.valueOf(dtRmout.get(i).get("RMOUT_RECEIVER_BANK")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_RECEIVER_BANK")));
                hashMap.put("RMOUT_RMSNO", String.valueOf(dtRmout.get(i).get("RMOUT_RMSNO")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_RMSNO")));
                hashMap.put("RMOUT_STAN", String.valueOf(dtRmout.get(i).get("RMOUT_STAN")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_STAN")));
                hashMap.put("RMOUT_FISC_RTN_CODE", String.valueOf(dtRmout.get(i).get("RMOUT_FISC_RTN_CODE")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_FISC_RTN_CODE")));
                hashMap.put("RMOUT_IN_ACC_ID_NO", String.valueOf(dtRmout.get(i).get("RMOUT_IN_ACC_ID_NO")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_IN_ACC_ID_NO")));
                hashMap.put("RMOUT_PENDING", String.valueOf(dtRmout.get(i).get("RMOUT_PENDING")).equals("null") ? "" : String.valueOf(dtRmout.get(i).get("RMOUT_PENDING")));
                dtNewRmout.add(hashMap);
            }
            return dtNewRmout;
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
            return null;
        }
    }

    private List<HashMap<String, Object>> getRmin(ModelMap mode) {
        List<HashMap<String, Object>> dtRmin = null;
        try {
            String systemDate = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN);
            dtRmin = rmService.getTopRMINByTxdateReciveBank(TOP_NUMBER, systemDate, atmService.getStatus().getSysstatHbkno());
            List<HashMap<String, Object>> dtNewRmin = new ArrayList<>();
            for (int i = 0; i < dtRmin.size(); i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("RMIN_SENDTIME", String.valueOf(dtRmin.get(i).get("RMIN_SENDTIME")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_SENDTIME")));
                hashMap.put("RMIN_FISC_SND_CODE", String.valueOf(dtRmin.get(i).get("RMIN_FISC_SND_CODE")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_FISC_SND_CODE")));
                hashMap.put("RMIN_BRNO", String.valueOf(dtRmin.get(i).get("RMIN_BRNO")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_BRNO")));
                hashMap.put("RMIN_FEPNO", String.valueOf(dtRmin.get(i).get("RMIN_FEPNO")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_FEPNO")));
                hashMap.put("RMIN_FISCSNO", String.valueOf(dtRmin.get(i).get("RMIN_FISCSNO")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_FISCSNO")));
                hashMap.put("RMIN_TXAMT", String.valueOf(dtRmin.get(i).get("RMIN_TXAMT")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_TXAMT")));
                hashMap.put("RMIN_SENDER_BANK", String.valueOf(dtRmin.get(i).get("RMIN_SENDER_BANK")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_SENDER_BANK")));
                hashMap.put("RMIN_RMSNO", String.valueOf(dtRmin.get(i).get("RMIN_RMSNO")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_RMSNO")));
                hashMap.put("RMIN_STAN", String.valueOf(dtRmin.get(i).get("RMIN_STAN")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_STAN")));
                hashMap.put("RMIN_FISC_RTN_CODE", String.valueOf(dtRmin.get(i).get("RMIN_FISC_RTN_CODE")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_FISC_RTN_CODE")));
                hashMap.put("RMIN_IN_ACC_ID_NO", String.valueOf(dtRmin.get(i).get("RMIN_IN_ACC_ID_NO")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_IN_ACC_ID_NO")));
                hashMap.put("RMIN_PENDING", String.valueOf(dtRmin.get(i).get("RMIN_PENDING")).equals("null") ? "" : String.valueOf(dtRmin.get(i).get("RMIN_PENDING")));
                dtNewRmin.add(hashMap);
            }
            return dtNewRmin;
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
            return null;
        }
    }

    /**
     * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
     */
    private Rmstat getRmStat(ModelMap mode) throws Exception {
        Rmstat defRmstat = new Rmstat();
        defRmstat.setRmstatHbkno(atmService.getStatus().getSysstatHbkno());
        try {
            defRmstat = rmService.getRmstatByPk(defRmstat);
            return defRmstat;
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
            return null;
        }
    }

    /**
     * syscom.config中的BankID與FISCID廢掉 改抓SYSSTAT的值
     */
//    private Allbank getAllBank(ModelMap mode) throws Exception {
//        Allbank defAllBank = new Allbank();
//        //永豐銀行代碼   BRNO '000表示本行
//        defAllBank.setAllbankBrno(BRNO);
//        defAllBank.setAllbankBkno(atmService.getStatus().getSysstatHbkno());
//        try {
//            rmService.getALLBANKbyPK(defAllBank);
//            return defAllBank;
//        } catch (Exception ex) {
//            this.showMessage(mode, MessageType.DANGER, "讀取資料有誤");
//            return null;
//        }
//    }
    private void bindGrid(ModelMap mode, UI_028150_Form form) {
        try {
            //add By maxine on 2011/06/24 for SYSSTAT自行查
            List<Sysstat> dtSysstat = rmService.getStatus();
            if (dtSysstat.size() < 1) {
                this.showMessage(mode, MessageType.DANGER, "SYSSTAT無資料");
                return;
            }
            //Fly 2014/09/15 增加匯款監控資料
            String t24P = "0";
            String fcs = "0";
            String cnt = "0";
            String amt = "0";
            String cntColor = "0";
            String amtColor = "0";
            HashMap<String, Object> clrtotal = rmService.getCntAmt();
            if (clrtotal != null) {
                cnt = String.valueOf(clrtotal.get("CLRTOTAL_REMAIN_CNT")).equals("null") ? cnt : String.valueOf(clrtotal.get("CLRTOTAL_REMAIN_CNT"));
                amt = String.valueOf(clrtotal.get("CLRTOTAL_REMAIN_AMT")).equals("null") ? amt : String.valueOf(clrtotal.get("CLRTOTAL_REMAIN_AMT"));
            } else {
                cnt = "0";
                amt = "0";
            }
            if (clrtotal != null) {
                cntColor = "black";
                amtColor = "black";
            } else {
                cntColor = "red";
                amtColor = "red";
            }
            mode.addAttribute("cntColor", cntColor);
            mode.addAttribute("amtColor", amtColor);
            mode.addAttribute("cnt", cnt);
            mode.addAttribute("amt", amt);

            String t24Color = StringUtils.EMPTY;
            String fcsColor = StringUtils.EMPTY;
            List<HashMap<String, Object>> dbRmmon = rmService.getT24Pending();
            if (dbRmmon.size() > 0) {
                t24P = String.valueOf(dbRmmon.get(0).get("RMMON_T24_PENDING_CNT")).equals("null") ? "" : String.valueOf(dbRmmon.get(0).get("RMMON_T24_PENDING_CNT"));
                fcs = String.valueOf(dbRmmon.get(0).get("RMMON_FCS_CNT")).equals("null") ? "" : String.valueOf(dbRmmon.get(0).get("RMMON_FCS_CNT"));
            } else {
                t24P = "N/A";
                fcs = "N/A";
            }
            if (dbRmmon.size() > 0) {
                t24Color = "black";
                fcsColor = "black";
            } else {
                t24Color = "red";
                fcsColor = "red";
            }
            mode.addAttribute("t24Color", t24Color);
            mode.addAttribute("fcsColor", fcsColor);
            mode.addAttribute("t24P", t24P);
            mode.addAttribute("fcs", fcs);

            //Fly 2019/10/29 增加顯示匯款暫禁記號
            String rmFlagText = StringUtils.EMPTY;
            String rmFlagColor = StringUtils.EMPTY;
            Clrdtl clrdtl = new Clrdtl();
            clrdtl.setClrdtlApId("10000");
            clrdtl.setClrdtlTxdate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
            clrdtl.setClrdtlPaytype(StringUtils.EMPTY);
            List<Clrdtl> dbClr = rmService.getCLRDTLByPK(clrdtl);
            if (dbClr.size() > 0) {
                if ("Y".equals(String.valueOf(dbClr.get(0).getClrdtlRmstat()))) {
                    rmFlagText = "Y-匯出";
                    rmFlagColor = "black";
                } else if ("N".equals(String.valueOf(dbClr.get(0).getClrdtlRmstat()))) {
                    rmFlagText = "N-禁止";
                    rmFlagColor = "red";
                } else if ("R".equals(String.valueOf(dbClr.get(0).getClrdtlRmstat()))) {
                    rmFlagText = "N-R:低於第二水位";
                    rmFlagColor = "red";
                } else {
                    rmFlagText = String.valueOf(dbClr.get(0).getClrdtlRmstat());
                    rmFlagColor = "black";
                }
            }
            mode.addAttribute("rmFlagText", rmFlagText);
            mode.addAttribute("rmFlagColor", rmFlagColor);

            Rmfiscout1 rmfiscout1 = new Rmfiscout1();
            Rmfiscout4 rmfiscout4 = new Rmfiscout4();
            Rmfiscin1 rmfiscin1 = new Rmfiscin1();
            Rmfiscin4 rmfiscin4 = new Rmfiscin4();
            List<HashMap<String, Object>> dvRMOUT = getRmOut(mode, form);
            List<HashMap<String, Object>> dvRMIN = getRmin(mode);
            //Dim defALLBANK As New Syscom.FEP10.Common.Tables.DefALLBANK
            //Candy 2012/01/13 add 增加顯示匯出(入)未下傳/下傳中筆數
            String outpassedcnt = String.valueOf(rmService.getRMOUTTTotalCntByStat("04") + rmService.getRMOUTTTotalCntByStat("99"));
            String outtransferingcnt = String.valueOf(rmService.getRMOUTTTotalCntByStat("05"));
            String inpassedcnt = String.valueOf(rmService.getRMINTotalCntByStat("99"));
            String intransferingcnt = String.valueOf(rmService.getRMINTotalCntByStat("02"));
            //2019/04/08 Modify by Ruling for 匯入檢核AML：增加顯示AML傳送中
            String amltransferingcnt = String.valueOf(rmService.getRMINTotalCntByStat("98"));
            mode.addAttribute("outpassedcnt", outpassedcnt.equals("null") ? "0" : outpassedcnt);
            mode.addAttribute("outtransferingcnt", outtransferingcnt.equals("null") ? "0" : outtransferingcnt);
            mode.addAttribute("inpassedcnt", inpassedcnt.equals("null") ? "0" : inpassedcnt);
            mode.addAttribute("intransferingcnt", intransferingcnt.equals("null") ? "0" : intransferingcnt);
            mode.addAttribute("amltransferingcnt", amltransferingcnt.equals("null") ? "0" : amltransferingcnt);

            //Jim, 2011/12/12, 加上監控匯出電文序號
            rmfiscout1.setRmfiscout1SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            rmfiscout1.setRmfiscout1ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());
            List<HashMap<String, Object>> rmfiscout1List = rmService.getRMFISCOUT1ByPK028060280150(rmfiscout1);
            String RMFISCOUT1_NO = "0";
            if (rmfiscout1List.size() > 0) {
                RMFISCOUT1_NO = String.valueOf(rmfiscout1List.get(0).get("RMFISCOUT1_NO"));
                if (RMFISCOUT1_NO.equals("null")) {
                    RMFISCOUT1_NO = "0";
                }
                //Me.RMFISCOUT1_REP_NO.Text = defRMFISCOUT1.RMFISCOUT1_REP_NO.ToString
            }
            mode.addAttribute("rMFISCOUT1_NO", RMFISCOUT1_NO);
            //Jim, 2011/12/30, 監控其他種類的電文序號
            rmfiscout4.setRmfiscout4SenderBank(SysStatus.getPropertyValue().getSysstatHbkno());
            rmfiscout4.setRmfiscout4ReceiverBank(SysStatus.getPropertyValue().getSysstatFbkno());
            List<HashMap<String, Object>> rmfiscout4List = rmService.getRMFISCOUT4ByPK028060280150(rmfiscout4);
            String RMFISCOUT4_NO = "0";
            if (rmfiscout4List.size() > 0) {
                RMFISCOUT4_NO = String.valueOf(rmfiscout4List.get(0).get("RMFISCOUT4_NO"));
                if (RMFISCOUT4_NO.equals("null")) {
                    RMFISCOUT4_NO = "0";
                }
            }
            mode.addAttribute("rMFISCOUT4_NO", RMFISCOUT4_NO);
            rmfiscin1.setRmfiscin1SenderBank(SysStatus.getPropertyValue().getSysstatFbkno());
            rmfiscin1.setRmfiscin1ReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
            List<HashMap<String, Object>> rmfiscin1List = rmService.getRMFISCIN1ByPK028060280150(rmfiscin1);
            String RMFISCIN1_NO = "0";
            if (rmfiscin1List.size() > 0) {
                RMFISCIN1_NO = String.valueOf(rmfiscin1List.get(0).get("RMFISCIN1_NO"));
                if (RMFISCIN1_NO.equals("null")) {
                    RMFISCIN1_NO = "0";
                }
                //Me.RMFISCOUT1_REP_NO.Text = defRMFISCOUT1.RMFISCOUT1_REP_NO.ToString
            }
            mode.addAttribute("rMFISCIN1_NO", RMFISCIN1_NO);
            //Jim, 2011/12/30, 監控其他種類的電文序號
            rmfiscin4.setRmfiscin4SenderBank(SysStatus.getPropertyValue().getSysstatFbkno());
            rmfiscin4.setRmfiscin4ReceiverBank(SysStatus.getPropertyValue().getSysstatHbkno());
            List<HashMap<String, Object>> rmfiscin4List = rmService.getRMFISCIN4ByPK028060280150(rmfiscin4);
            String RMFISCIN4_NO = "0";
            if (rmfiscin4List.size() > 0) {
                RMFISCIN4_NO = String.valueOf(rmfiscin4List.get(0).get("RMFISCIN4_NO"));
                if (RMFISCIN4_NO.equals("null")) {
                    RMFISCIN4_NO = "0";
                }
            }
            mode.addAttribute("rMFISCIN4_NO", RMFISCIN4_NO);
            Rmstat rmstat = getRmStat(mode);
            //CBSFlagLbl.Text = IIf(Convert.ToBoolean(_dtSYSSTAT.Rows(0)("SYSSTAT_CBS")) = True, _StrNormal, _StrStop).ToString
            //RMSTAT_CBS_OUT_FLAG/RMSTATCBS_IN_FLAG 顯示"Y"=許可, "N"=暫停
            //RMCBSOutFlagLbl.Text = IIf(defRMSTAT.RMSTAT_CBS_OUT_FLAG.ToString = "Y", _StrNormal, _StrStop).ToString
            String rMCBSOutFlagLbl = "Y".equals(rmstat.getRmstatCbsOutFlag()) ? _StrConfirm : _StrStop;
            String rMFISCOFlag1Lbl = "Y".equals(rmstat.getRmstatFiscoFlag1()) ? _StrConfirm : _StrStop;
            String rMFISCOFlag4Lbl = "Y".equals(rmstat.getRmstatFiscoFlag4()) ? _StrConfirm : _StrStop;
            mode.addAttribute("rMCBSOutFlagLbl", rMCBSOutFlagLbl);
            mode.addAttribute("rMFISCOFlag1Lbl", rMFISCOFlag1Lbl);
            mode.addAttribute("rMFISCOFlag4Lbl", rMFISCOFlag4Lbl);
            //RMCBSInFlagLbl.Text = IIf(defRMSTAT.RMSTAT_CBS_IN_FLAG = "Y", _StrNormal, _StrStop).ToString
            String rMCBSInFlagLbl = "Y".equals(rmstat.getRmstatCbsInFlag()) ? _StrConfirm : _StrStop;
            String rMFISCIFlag1Lbl = "Y".equals(rmstat.getRmstatFisciFlag1()) ? _StrConfirm : _StrStop;
            String rMFISCIFlag4Lbl = "Y".equals(rmstat.getRmstatFisciFlag4()) ? _StrConfirm : _StrStop;
            mode.addAttribute("rMCBSInFlagLbl", rMCBSInFlagLbl);
            mode.addAttribute("rMFISCIFlag1Lbl", rMFISCIFlag1Lbl);
            mode.addAttribute("rMFISCIFlag4Lbl", rMFISCIFlag4Lbl);

            String rMFISCOFlag1LblColor = StringUtils.EMPTY;
            String rMFISCOFlag4LblColor = StringUtils.EMPTY;
            String rMFISCIFlag1LblColor = StringUtils.EMPTY;
            String rMFISCIFlag4LblColor = StringUtils.EMPTY;
            if (!"Y".equals(rmstat.getRmstatFiscoFlag1())) {
                rMFISCOFlag1LblColor = "red";
            } else {
                rMFISCOFlag1LblColor = "black";
            }
            if (!"Y".equals(rmstat.getRmstatFiscoFlag4())) {
                rMFISCOFlag4LblColor = "red";
            } else {
                rMFISCOFlag4LblColor = "black";
            }
            if (!"Y".equals(rmstat.getRmstatFisciFlag1())) {
                rMFISCIFlag1LblColor = "red";
            } else {
                rMFISCIFlag1LblColor = "black";
            }
            if (!"Y".equals(rmstat.getRmstatFisciFlag4())) {
                rMFISCIFlag4LblColor = "red";
            } else {
                rMFISCIFlag4LblColor = "black";
            }
            mode.addAttribute("rMFISCOFlag1LblColor", rMFISCOFlag1LblColor);
            mode.addAttribute("rMFISCOFlag4LblColor", rMFISCOFlag4LblColor);
            mode.addAttribute("rMFISCIFlag1LblColor", rMFISCIFlag1LblColor);
            mode.addAttribute("rMFISCIFlag4LblColor", rMFISCIFlag4LblColor);

            //add by maxine on 2011/06/15
            //If Convert.ToBoolean(_dtSYSSTAT.Rows(0)("SYSSTAT_CBS")) <> True Then
            //    CBSFlagLbl.ForeColor = Drawing.Color.Red
            //Else
            //    CBSFlagLbl.ForeColor = Drawing.Color.Black
            //End If

            String rMCBSOutFlagLblColor = StringUtils.EMPTY;
            String rMCBSInFlagLblColor = StringUtils.EMPTY;
            if (!"Y".equals(rmstat.getRmstatCbsOutFlag())) {
                rMCBSOutFlagLblColor = "red";
            } else {
                rMCBSOutFlagLblColor = "black";
            }
            if (!"Y".equals(rmstat.getRmstatCbsInFlag())) {
                rMCBSInFlagLblColor = "red";
            } else {
                rMCBSInFlagLblColor = "black";
            }
            mode.addAttribute("rMCBSOutFlagLblColor", rMCBSOutFlagLblColor);
            mode.addAttribute("rMCBSInFlagLblColor", rMCBSInFlagLblColor);

            String SYSSTAT_AOCT_1000Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatAoct1000()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatAoct1000()), "-", getAoctName(dtSysstat.get(0).getSysstatAoct1000()));
            String SYSSTAT_AOCT_1000LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatAoct1000());
            mode.addAttribute("SYSSTAT_AOCT_1000Lbl", SYSSTAT_AOCT_1000Lbl);
            mode.addAttribute("SYSSTAT_AOCT_1000LblColor", SYSSTAT_AOCT_1000LblColor);

            String SYSSTAT_MBACT_1000Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatMbact1000()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatMbact1000()), "-", getAoctName(dtSysstat.get(0).getSysstatMbact1000()));
            String SYSSTAT_MBACT_1000LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatMbact1000());
            mode.addAttribute("SYSSTAT_MBACT_1000Lbl", SYSSTAT_MBACT_1000Lbl);
            mode.addAttribute("SYSSTAT_MBACT_1000LblColor", SYSSTAT_MBACT_1000LblColor);

            String SYSSTAT_AOCT_1100Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatAoct1100()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatAoct1100()), "-", getAoctName(dtSysstat.get(0).getSysstatAoct1100()));
            String SYSSTAT_AOCT_1100LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatAoct1100());
            mode.addAttribute("SYSSTAT_AOCT_1100Lbl", SYSSTAT_AOCT_1100Lbl);
            mode.addAttribute("SYSSTAT_AOCT_1100LblColor", SYSSTAT_AOCT_1100LblColor);

            String SYSSTAT_MBACT_1100Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatMbact1100()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatMbact1100()), "-", getAoctName(dtSysstat.get(0).getSysstatMbact1100()));
            String SYSSTAT_MBACT_1100LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatMbact1100());
            mode.addAttribute("SYSSTAT_MBACT_1100Lbl", SYSSTAT_MBACT_1100Lbl);
            mode.addAttribute("SYSSTAT_MBACT_1100LblColor", SYSSTAT_MBACT_1100LblColor);

            String SYSSTAT_AOCT_1400Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatAoct1400()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatAoct1400()), "-", getAoctName(dtSysstat.get(0).getSysstatAoct1400()));
            String SYSSTAT_AOCT_1400LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatAoct1400());
            mode.addAttribute("SYSSTAT_AOCT_1400Lbl", SYSSTAT_AOCT_1400Lbl);
            mode.addAttribute("SYSSTAT_AOCT_1400LblColor", SYSSTAT_AOCT_1400LblColor);

            String SYSSTAT_MBACT_1400Lbl = StringUtils.join(String.valueOf(dtSysstat.get(0).getSysstatMbact1400()).equals("null") ? "" : String.valueOf(dtSysstat.get(0).getSysstatMbact1400()), "-", getAoctName(dtSysstat.get(0).getSysstatMbact1400()));
            String SYSSTAT_MBACT_1400LblColor = myGetAOCTNameColor(dtSysstat.get(0).getSysstatMbact1400());
            mode.addAttribute("SYSSTAT_MBACT_1400Lbl", SYSSTAT_MBACT_1400Lbl);
            mode.addAttribute("SYSSTAT_MBACT_1400LblColor", SYSSTAT_MBACT_1400LblColor);

            //TODO:{Matt} 暫時，不確定 若 今日財金關帳時間 為0則 顯示延時關帳
            //defALLBANK = GetALLBANK()
            //SetCloseTimeLbl.Text = IIf(defALLBANK.ALLBANK_SET_CLOSE_TIME = 0, "延時關帳", "").ToString
            if (dvRMOUT.size() > 0) {
                WebUtil.putInAttribute(mode, AttributeName.DvRMOUT, dvRMOUT);
            }
            //RMIN
            if (dvRMIN.size() > 0) {
                WebUtil.putInAttribute(mode, AttributeName.DvRMIN, dvRMIN);
            }
        } catch (Exception ex) {
            this.showMessage(mode, MessageType.DANGER, programError);
        }
    }

    private String myGetAOCTNameColor(Object field) {
        if (field == null) {
            return "black";
        } else {
            switch (field.toString()) {
                case "0":
                case "1":
                    return "black";
                default:
                    return "red";
            }
        }
    }

//    private String myGetMBACTNameColor(Object field) {
//        if (field == null) {
//            return "black";
//        } else {
//            switch (field.toString()) {
//                case "0":
//                case "1":
//                    return "black";
//                default:
//                    return "red";
//            }
//        }
//    }

    //UpdatePanel設定
    @PostMapping(value = "/rm/UI_028150/saveIntervalBtn_Click")
    protected String saveIntervalBtn_Click(@ModelAttribute UI_028150_Form form, ModelMap mode) {
        int time = 30;
        try {
            time = Integer.parseInt(form.getTime());
            if (time < 0 || time > 60) {
                time = 30;
            }
        } catch (NumberFormatException e) {
            this.warnMessage(e, e.getMessage());
        }
        form.setTime(Integer.toString(time));
        queryData(mode, form);
        updateDataPnl1_Tick(mode);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
        return Router.UI_028150.getView();
    }

    //計時器發動的事件
    protected void updateDataPnl1_Tick(ModelMap mode) {
        String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
        StringBuffer str = new StringBuffer(time);
        str.insert(4, "年");
        str.insert(7, "月");
        str.insert(10, "日");
        str.insert(13, "點");
        str.insert(16, "分");
        str.insert(19, "秒");
        mode.addAttribute("newtime", str);
    }
}
