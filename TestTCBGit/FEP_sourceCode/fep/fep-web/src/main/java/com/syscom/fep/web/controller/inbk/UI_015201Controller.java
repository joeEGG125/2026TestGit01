package com.syscom.fep.web.controller.inbk;

import static com.syscom.fep.vo.constant.NormalRC.FISC_OK;

import java.math.BigDecimal;
import java.util.Calendar;

import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.handler.FEPHandler;
import com.syscom.fep.web.audit.AuditLog;
import com.syscom.fep.web.configurer.WebConfiguration;
import com.syscom.fep.web.entity.AttributeName;
import com.syscom.fep.web.entity.MessageType;
import com.syscom.fep.web.entity.Router;
import com.syscom.fep.web.form.inbk.UI_015201_Form;
import com.syscom.fep.web.service.InbkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.mybatis.model.Clrtotal;
import com.syscom.fep.vo.enums.FISCSubSystem;
import com.syscom.fep.vo.text.fisc.FISCGeneral;
import com.syscom.fep.vo.text.fisc.FISC_CLR;
import com.syscom.fep.web.controller.BaseController;
import com.syscom.fep.web.util.WebUtil;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 查詢財金跨行結帳總計交易-5201
 *
 * @author  Kai
 */
@Controller
public class  UI_015201Controller extends BaseController {

    @Autowired
    public InbkService inbkService;

    @Override
    public void pageOnLoad(ModelMap mode) {
        UI_015201_Form form = new UI_015201_Form();
        form.setLblTxDate(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYY_MM_DD_SLASH));
        // 財金STAN
        String bankNo = StringUtils.EMPTY;
        try {
            bankNo = SysStatus.getPropertyValue().getSysstatHbkno();
        } catch (Exception e) {
            this.showMessage(mode, MessageType.DANGER,"查詢Sysstat出現異常");
        }
        form.setLblBankNo(bankNo);
        WebUtil.putInAttribute(mode, AttributeName.Form, form);
    }
    @PostMapping(value = "/inbk/UI_015201/getClrtotalDetailed", produces = "application/json;charset=utf-8")
    public String getClrtotalDetailed(@ModelAttribute UI_015201_Form form, ModelMap mode) {
        String queryOnly = WebConfiguration.getInstance().getQueryOnly();
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction("查詢");
            if (StringUtils.isNotBlank(form.getLblBankNo())) {
                auditLog.addParam("銀行代號", form.getLblBankNo());
            }
            if (StringUtils.isNotBlank(form.getLblTxDate())) {
                auditLog.addParam("查詢日期", form.getLblTxDate().replace("-",""));
            }
            if (StringUtils.isNotBlank(form.getLblTxTime())) {
                auditLog.addParam("查詢時間", form.getLblTxTime());
            }
            if (StringUtils.isNotBlank(form.getLblBknoStan())) {
                auditLog.addParam("查詢序號", form.getLblBknoStan());
            }
            setAuditLog(auditLog);

            //2017/10/16 Modify for SSTQ查詢按確認按鈕時改查資料庫CLRTOTAL的資料
            if ("1".equals(queryOnly)) {
                //SSTQ查DB
                callDB(form,mode);
                return Router.UI_015201.getView();
            }

            FISCGeneral aData = new FISCGeneral();
            FEPHandler fepHandler = new FEPHandler();
            String[] message = null;
            aData.setCLRRequest(new FISC_CLR());
            aData.setSubSystem(FISCSubSystem.CLR);
            aData.getCLRRequest().setMessageKind(MessageFlow.Request);
            aData.getCLRRequest().setProcessingCode("5201");
            aData.getCLRRequest().setMessageType("0500");

            //add by Maxine on 2011/09/02 for EMS加UserId
            aData.getCLRRequest().setLogContext(new LogData());
            //modified by Maxine on 2011/12/13 for 用FEPUSER_LOGONID代替FEPUSER_TLRNO
            aData.getCLRRequest().getLogContext().setTxUser(WebUtil.getUser().getUserId());

            //Call AA
            fepHandler.dispatch(FEPChannel.FEP, aData);

            //將AA RC 顯示在UI上
            if (aData.getDescription() == null || StringUtils.isBlank(aData.getDescription())) {
                aData.setDescription(MessageError);
            }
            message = aData.getDescription().split("[-]", -1);

            double totalBal = 0;
            if (message.length == 2 && FISC_OK.equals(message[0])) {//若AA回的是財金RC(RC-Description)而且財金RC=0001，UI顯示處理成功
                Clrtotal clrtotal = new Clrtotal();
                form.setLblTxTime(charTimeToTime(aData.getCLRResponse().getTxnInitiateDateAndTime().substring(6)));
                form.setLblBknoStan(strDataIsEmpty(StringUtils.join(aData.getCLRResponse().getTxnSourceInstituteId().substring(0, 3) , "-" , aData.getCLRResponse().getSystemTraceAuditNo()), "-"));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //lblATM_CNT_DR = aData.CLRResponse.ATM_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getAtmCntDr())) {
                    clrtotal.setClrtotalAtmCntDr(0);
                } else {
                    clrtotal.setClrtotalAtmCntDr(Integer.parseInt(aData.getCLRResponse().getAtmCntDr()));
                }
                clrtotal.setClrtotalAtmAmtDr(new BigDecimal(aData.getCLRResponse().getAtmAmtDr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblATM_CNT_CR = aData.CLRResponse.ATM_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getAtmCntCr())) {
                    clrtotal.setClrtotalAtmCntCr(0);
                } else {
                    clrtotal.setClrtotalAtmCntCr(Integer.parseInt(aData.getCLRResponse().getAtmCntCr()));
                }
                clrtotal.setClrtotalAtmAmtCr(new BigDecimal(aData.getCLRResponse().getAtmAmtCr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblATM_EC_CNT_DR = aData.CLRResponse.ATM_EC_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getAtmEcCntDr())) {
                    clrtotal.setClrtotalAtmEcCntDr(0);
                } else {
                    clrtotal.setClrtotalAtmEcCntDr(Integer.parseInt(aData.getCLRResponse().getAtmEcCntDr()));
                }
                clrtotal.setClrtotalAtmEcAmtDr(new BigDecimal(aData.getCLRResponse().getAtmEcAmtDr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblATM_EC_CNT_CR = aData.CLRResponse.ATM_EC_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getAtmEcCntCr())) {
                    clrtotal.setClrtotalAtmEcCntCr(0);
                } else {
                    clrtotal.setClrtotalAtmEcCntCr(Integer.parseInt(aData.getCLRResponse().getAtmEcCntCr()));
                }
                clrtotal.setClrtotalAtmEcAmtCr(new BigDecimal(aData.getCLRResponse().getAtmEcAmtCr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblRM_CNT_DR = aData.CLRResponse.RM_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getRmCntDr())) {
                    clrtotal.setClrtotalRmCntDr(0);
                } else {
                    clrtotal.setClrtotalRmCntDr(Integer.parseInt(aData.getCLRResponse().getRmCntDr()));
                }
                clrtotal.setClrtotalRmAmtDr(new BigDecimal(aData.getCLRResponse().getRmAmtDr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblRM_CNT_CR = aData.CLRResponse.RM_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getRmCntCr())) {
                    clrtotal.setClrtotalRmCntCr(0);
                } else {
                    clrtotal.setClrtotalRmCntCr(Integer.parseInt(aData.getCLRResponse().getRmCntCr()));
                }
                clrtotal.setClrtotalRmAmtCr(new BigDecimal(aData.getCLRResponse().getRmAmtCr()));
                clrtotal.setClrtotalFeeAmtDr(new BigDecimal(aData.getCLRResponse().getFeeAmtDr()));
                clrtotal.setClrtotalFeeAmtCr(new BigDecimal(aData.getCLRResponse().getFeeAmtCr()));
                clrtotal.setClrtotalFeeEcAmtDr(new BigDecimal(aData.getCLRResponse().getFeeEcAmtDr()));
                clrtotal.setClrtotalFeeEcAmtCr(new BigDecimal(aData.getCLRResponse().getFeeEcAmtCr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblPOS_CNT_DR = aData.CLRResponse.POS_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getPosCntDr())) {
                    clrtotal.setClrtotalPosCntDr(0);
                } else {
                    clrtotal.setClrtotalPosCntDr(Integer.parseInt(aData.getCLRResponse().getPosCntDr()));
                }
                clrtotal.setClrtotalPosAmtDr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getPosAmtDr()) ? "0" : aData.getCLRResponse().getPosAmtDr())));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblPOS_CNT_CR = aData.CLRResponse.POS_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getPosCntCr())) {
                    clrtotal.setClrtotalPosCntCr(0);
                } else {
                    clrtotal.setClrtotalPosCntCr(Integer.parseInt(aData.getCLRResponse().getPosCntCr()));
                }
                clrtotal.setClrtotalPosAmtCr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getPosAmtCr()) ? "0" : aData.getCLRResponse().getPosAmtCr())));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblFEDI_CNT_DR = aData.CLRResponse.FEDI_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getFediCntDr())) {
                    clrtotal.setClrtotalFediCntDr(0);
                } else {
                    clrtotal.setClrtotalFediCntDr(Integer.parseInt(aData.getCLRResponse().getFediCntDr()));
                }
                clrtotal.setClrtotalFediAmtDr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getFediAmtDr()) ? "0" : aData.getCLRResponse().getFediAmtDr())));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblFEDI_CNT_CR = aData.CLRResponse.FEDI_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getFediCntCr())) {
                    clrtotal.setClrtotalFediCntCr(0);
                } else {
                    clrtotal.setClrtotalFediCntCr(Integer.parseInt(aData.getCLRResponse().getFediCntCr()));
                }
                clrtotal.setClrtotalFediAmtCr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getFediAmtCr()) ? "0" : aData.getCLRResponse().getFediAmtCr())));

                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblNB_CNT_DR = aData.CLRResponse.NB_CNT_DR
                //2013/07/26 Modify by Ruling for 財金調整清算電文:NB_CNT_DR電文欄位移除，UI預設為0
                clrtotal.setClrtotalNbCntDr(0);
                //If String.IsNullOrEmpty(aData.CLRResponse.NB_CNT_DR) Then
                //    LblNB_CNT_DR = "0"
                //Else
                //    LblNB_CNT_DR = CInt(aData.CLRResponse.NB_CNT_DR).ToString
                //End If

                //2013/07/26 Modify by Ruling for 財金調整清算電文:NB_AMT_DR電文欄位移除，UI預設為0.00
                clrtotal.setClrtotalNbAmtDr(BigDecimal.valueOf(0.00));
                //LblNB_AMT_DR = Format(Double.Parse(IIf(String.IsNullOrEmpty(aData.CLRResponse.NB_AMT_DR), 0, aData.CLRResponse.NB_AMT_DR).ToString), "#,###,###,##0.00")

                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblNB_CNT_CR = aData.CLRResponse.NB_CNT_CR
                //2013/07/26 Modify by Ruling for 財金調整清算電文:NB_CNT_CR電文欄位移除，UI預設為0
                clrtotal.setClrtotalNbCntCr(0);
                //If String.IsNullOrEmpty(aData.CLRResponse.NB_CNT_CR) Then
                //    LblNB_CNT_CR = "0"
                //Else
                //    LblNB_CNT_CR = CInt(aData.CLRResponse.NB_CNT_CR).ToString
                //End If

                //2013/07/26 Modify by Ruling for 財金調整清算電文:NB_AMT_CR電文欄位移除，UI預設為0.00
                clrtotal.setClrtotalNbAmtCr(BigDecimal.valueOf(0.00));
                //LblNB_AMT_CR = Format(Double.Parse(IIf(String.IsNullOrEmpty(aData.CLRResponse.NB_AMT_CR), 0, aData.CLRResponse.NB_AMT_CR).ToString), "#,###,###,##0.00")

                clrtotal.setClrtotalSumAmtDr(new BigDecimal(aData.getCLRResponse().getSumAmtDr()));
                clrtotal.setClrtotalSumAmtCr(new BigDecimal(aData.getCLRResponse().getSumAmtCr()));
                clrtotal.setClrtotalOddsDr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getOddsDr()) ? "0" : aData.getCLRResponse().getOddsDr())));
                clrtotal.setClrtotalOddsCr(new BigDecimal((StringUtils.isBlank(aData.getCLRResponse().getOddsCr()) ? "0" : aData.getCLRResponse().getOddsCr())));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblREMAIN_CNT = aData.CLRResponse.REMAIN_CNT
                if (StringUtils.isBlank(aData.getCLRResponse().getRemainCnt())) {
                    clrtotal.setClrtotalRemainCnt(0);
                } else {
                    clrtotal.setClrtotalRemainCnt(Integer.parseInt(aData.getCLRResponse().getRemainCnt()));
                }
                clrtotal.setClrtotalRemainAmt(new BigDecimal(aData.getCLRResponse().getRemainAmt()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblFG_CNT_DR = aData.CLRResponse.FG_CNT_DR
                if (StringUtils.isBlank(aData.getCLRResponse().getFgCntDr())) {
                    clrtotal.setClrtotalFgCntDr(0);
                } else {
                    clrtotal.setClrtotalFgCntDr(Integer.parseInt(aData.getCLRResponse().getFgCntDr()));
                }
                clrtotal.setClrtotalFgAmtDr(new BigDecimal(aData.getCLRResponse().getFgAmtDr()));
                //2012-02-14 modified by KK for 如果沒有值則顯示零
                //LblFG_CNT_CR = aData.CLRResponse.FG_CNT_CR
                if (StringUtils.isBlank(aData.getCLRResponse().getFgCntCr())) {
                    clrtotal.setClrtotalFgCntCr(0);
                } else {
                    clrtotal.setClrtotalFgCntCr(Integer.parseInt(aData.getCLRResponse().getFgCntCr()));
                }
                clrtotal.setClrtotalFgAmtCr(new BigDecimal(aData.getCLRResponse().getFgAmtCr()));
                totalBal = Double.parseDouble(aData.getCLRResponse().getRevolAmt()) + Double.parseDouble(aData.getCLRResponse().getActBal());
                clrtotal.setClrtotalRevolAmt(new BigDecimal(aData.getCLRResponse().getRevolAmt()));
                clrtotal.setClrtotalActBal(new BigDecimal(aData.getCLRResponse().getActBal()));
                WebUtil.putInAttribute(mode, AttributeName.DetailEntity, clrtotal);
            }
            this.showMessage(mode,MessageType.INFO,aData.getDescription());
            mode.addAttribute("totalBal", totalBal);
            mode.addAttribute(AttributeName.Form.toString(), form);
            return Router.UI_015201.getView();
        } catch (Exception ex) {
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode, MessageType.DANGER,programError);
            mode.addAttribute(AttributeName.Form.toString(), form);
            return Router.UI_015201.getView();
        }
    }


    protected void callDB(UI_015201_Form form,ModelMap mode) {
        try {
            Clrtotal defCLRTOTAL = new Clrtotal();
            defCLRTOTAL.setClrtotalStDate(form.getLblTxDate().replace("/",""));
            defCLRTOTAL.setClrtotalCur("000");
            defCLRTOTAL.setClrtotalSource((short) 1);
            defCLRTOTAL = inbkService.getClrtotal(defCLRTOTAL.getClrtotalStDate(),defCLRTOTAL.getClrtotalCur(),defCLRTOTAL.getClrtotalSource());
            double totalBal = 0;
            if (defCLRTOTAL != null) {
                form.setLblTxTime(charTimeToTime(defCLRTOTAL.getClrtotalTxTime()));
                form.setLblBknoStan(strDataIsEmpty(defCLRTOTAL.getClrtotalBkno().substring(0, 3) + "-" + defCLRTOTAL.getClrtotalStan(), "-"));

                defCLRTOTAL.setClrtotalNbCntDr(0);
                defCLRTOTAL.setClrtotalNbAmtDr(BigDecimal.valueOf(0.00));
                defCLRTOTAL.setClrtotalNbCntCr(0);
                defCLRTOTAL.setClrtotalNbAmtCr(BigDecimal.valueOf(0.00));
                totalBal = defCLRTOTAL.getClrtotalRevolAmt().doubleValue() + defCLRTOTAL.getClrtotalActBal().doubleValue(); //"#,###,###,##0.00"

                WebUtil.putInAttribute(mode, AttributeName.DetailEntity, defCLRTOTAL);
                mode.addAttribute("totalBal", totalBal);
                this.showMessage(mode,MessageType.SUCCESS,QuerySuccess);
            } else {
                this.showMessage(mode,MessageType.INFO,QueryNoData);
            }
            mode.addAttribute(AttributeName.Form.toString(), form);
        }catch (Exception ex){
            this.errorMessage(ex, ex.getMessage());
            this.showMessage(mode,MessageType.DANGER, ex.getMessage());
        }
    }

}
