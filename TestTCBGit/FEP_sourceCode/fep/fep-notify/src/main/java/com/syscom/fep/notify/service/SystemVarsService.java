package com.syscom.fep.notify.service;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.SysconfMapper;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.mybatis.model.Sysstat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.syscom.fep.notify.cnst.NotifyConstant.SYSTEM_VAR_SYMBOL;

@Service
public class SystemVarsService extends FEPBase {
    // private static LogHelper logger = LogHelperFactory.getGeneralLogger();

    @Autowired
    private SysconfExtMapper sysconfMapper;

    @Cacheable(value = "systemVarsCache")
    public String getSysStatCache(LogData logData, String statusName) {
        return getSystemVarsByName(logData, statusName);
    }


    @CacheEvict(value = "systemVarsCache", allEntries = true)
    public void cleanSystemVarsCache() {
        // 清除全部的 Cache 不會動到 JPA
    }


    private String getSystemVarsByName(LogData logData, String varName) {
        // logger.info("根據 systemVarName:", varName, " 獲取 SystemVar");
        logData.setRemark(StringUtils.join("根據 systemVarName:", varName, " 獲取 SystemVar"));
        logData.setProgramName(StringUtils.join(ProgramName, ".send"));
        logMessage(logData);
        String varValue = null;
        String noSymbolVarName = varName.replaceAll(SYSTEM_VAR_SYMBOL, "");
        // 從db sysstat 取資料
        if (noSymbolVarName.indexOf("SYSSTAT_", 0) == 0) {
            String noTableNameVarName = noSymbolVarName.replace("SYSSTAT_", "");
            varValue = getSysStatVal(noTableNameVarName);
        }

        // 從db sysconf 取資料
        if (Objects.isNull(varValue)) {
            String[] confSysNoAndName = noSymbolVarName.split("\\.");
            if (confSysNoAndName.length >= 2) {
                varValue = getSysConfVal(confSysNoAndName[0], confSysNoAndName[1]);
            }
        }
//        Sysconf configMapper = sysconfMapper.selectByPrimaryKey((short) 8,"AATimeout");
//        System.out.println(configMapper);
        //Map<String, String> systemVars = DemoData.systemVars;
        //String varValue = systemVars.get(noSymbolVarName);
        return varValue;
    }

    private String getSysConfVal(String subSysNO, String confVarName) {
        SysconfMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
        Sysconf sysConf = sysconfMapper.selectByPrimaryKey(Short.valueOf(subSysNO), confVarName);
        return sysConf.getSysconfValue();
    }

    private String getSysStatVal(String statVarName) {
        SysstatExtMapper sysstatExtMapper = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
        Sysstat sysstat = sysstatExtMapper.selectAll().get(0);
        switch (statVarName.toUpperCase()) {
            case "HBKNO":
                return sysstat.getSysstatHbkno();
            case "FBKNO":
                return sysstat.getSysstatFbkno();
            case "SCBKNO":
                return sysstat.getSysstatScbkno();
            case "LBSDY_FISC":
                return sysstat.getSysstatLbsdyFisc();
            case "TBSDY_FISC":
                return sysstat.getSysstatTbsdyFisc();
            case "NBSDY_FISC":
                return sysstat.getSysstatNbsdyFisc();
            case "FOPCSYNC":
                return sysstat.getSysstatFopcsync();
            case "TOPCSYNC":
                return sysstat.getSysstatTopcsync();
            case "FCDSYNC":
                return sysstat.getSysstatFcdsync();
            case "TCDSYNC":
                return sysstat.getSysstatTcdsync();
            case "FRMSYNC":
                return sysstat.getSysstatFrmsync();
            case "TRMSYNC":
                return sysstat.getSysstatTrmsync();
            case "FPPSYNC":
                return sysstat.getSysstatFppsync();
            case "TPPSYNC":
                return sysstat.getSysstatTppsync();
            case "F3DESSYNC":
                return sysstat.getSysstatF3dessync();
            case "T3DESSYNC":
                return sysstat.getSysstatT3dessync();
            case "SOCT":
                return sysstat.getSysstatSoct();
            case "AOCT_2000":
                return sysstat.getSysstatAoct2000();
            case "AOCT_2200":
                return sysstat.getSysstatAoct2200();
            case "AOCT_2500":
                return sysstat.getSysstatAoct2500();
            case "AOCT_2510":
                return sysstat.getSysstatAoct2510();
            case "AOCT_2520":
                return sysstat.getSysstatAoct2520();
            case "AOCT_2530":
                return sysstat.getSysstatAoct2530();
            case "AOCT_2540":
                return sysstat.getSysstatAoct2540();
            case "AOCT_2550":
                return sysstat.getSysstatAoct2550();
            case "AOCT_2560":
                return sysstat.getSysstatAoct2560();
            case "AOCT_7100":
                return sysstat.getSysstatAoct7100();
            case "AOCT_1000":
                return sysstat.getSysstatAoct1000();
            case "AOCT_1100":
                return sysstat.getSysstatAoct1100();
            case "AOCT_1200":
                return sysstat.getSysstatAoct1200();
            case "AOCT_1300":
                return sysstat.getSysstatAoct1300();
            case "AOCT_1400":
                return sysstat.getSysstatAoct1400();
            case "MBOCT":
                return sysstat.getSysstatMboct();
            case "MBACT_2000":
                return sysstat.getSysstatMbact2000();
            case "MBACT_2200":
                return sysstat.getSysstatMbact2200();
            case "MBACT_2500":
                return sysstat.getSysstatMbact2500();
            case "MBACT_2510":
                return sysstat.getSysstatMbact2510();
            case "MBACT_2520":
                return sysstat.getSysstatMbact2520();
            case "MBACT_2530":
                return sysstat.getSysstatMbact2530();
            case "MBACT_2540":
                return sysstat.getSysstatMbact2540();
            case "MBACT_2550":
                return sysstat.getSysstatMbact2550();
            case "MBACT_2560":
                return sysstat.getSysstatMbact2560();
            case "MBACT_7100":
                return sysstat.getSysstatMbact7100();
            case "MBACT_1000":
                return sysstat.getSysstatMbact1000();
            case "MBACT_1100":
                return sysstat.getSysstatMbact1100();
            case "MBACT_1200":
                return sysstat.getSysstatMbact1200();
            case "MBACT_1300":
                return sysstat.getSysstatMbact1300();
            case "MBACT_1400":
                return sysstat.getSysstatMbact1400();
            case "OPCKEYST":
                return String.valueOf(sysstat.getSysstatOpckeyst());
            case "ATMKEYST":
                return String.valueOf(sysstat.getSysstatAtmkeyst());
            case "RMKEYST":
                return String.valueOf(sysstat.getSysstatRmkeyst());
            case "PPKEYST":
                return String.valueOf(sysstat.getSysstatPpkeyst());
            //case "3DESKEYST":
            //    return sysstat.getSysstat3Des();
//            case "INTRA":
//                return String.valueOf(sysstat.getSysstatIntra());
//            case "AGENT":
//                return String.valueOf(sysstat.getSysstatAgent());
//            case "ISSUE":
//                return String.valueOf(sysstat.getSysstatIssue());
//            case "IWD_I":
//                return String.valueOf(sysstat.getSysstatIwdI());
//            case "IWD_A":
//                return String.valueOf(sysstat.getSysstatIwdA());
//            case "IWD_F":
//                return String.valueOf(sysstat.getSysstatIwdF());
//            case "IFT_I":
//                return String.valueOf(sysstat.getSysstatIftI());
//            case "IFT_A":
//                return String.valueOf(sysstat.getSysstatIftA());
//            case "IFT_F":
//                return String.valueOf(sysstat.getSysstatIftF());
//            case "IPY_I":
//                return String.valueOf(sysstat.getSysstatIpyI());
//            case "IPY_A":
//                return String.valueOf(sysstat.getSysstatIpyA());
//            case "IPY_F":
//                return String.valueOf(sysstat.getSysstatIpyF());
//            case "CDP_F":
//                return String.valueOf(sysstat.getSysstatCdpF());
//            case "ICCDP_I":
//                return String.valueOf(sysstat.getSysstatIccdpA());
//            case "ICCDP_A":
//                return String.valueOf(sysstat.getSysstatIccdpA());
//            case "ICCDP_F":
//                return String.valueOf(sysstat.getSysstatIccdpF());
//            case "ETX_I":
//                return String.valueOf(sysstat.getSysstatEtxI());
//            case "ETX_A":
//                return String.valueOf(sysstat.getSysstatEtxA());
//            case "ETX_F":
//                return String.valueOf(sysstat.getSysstatEtxF());
//            case "2525_A":
//                return String.valueOf(sysstat.getSysstat2525A());
//            case "2525_F":
//                return String.valueOf(sysstat.getSysstat2525F());
//            case "CPU_A":
//                return String.valueOf(sysstat.getSysstatCpuA());
//            case "CPU_F":
//                return String.valueOf(sysstat.getSysstatCpuF());
//            case "CAU_A":
//                return String.valueOf(sysstat.getSysstatCauA());
//            case "CAU_F":
//                return String.valueOf(sysstat.getSysstatCauF());
//            case "CWV_A":
//                return String.valueOf(sysstat.getSysstatCwvA());
//            case "CWM_A":
//                return String.valueOf(sysstat.getSysstatCwmA());
//            case "GPCWD_F":
//                return String.valueOf(sysstat.getSysstatGpcwdF());
//            case "CA_I":
//                return String.valueOf(sysstat.getSysstatCaI());
//            case "CAF_A":
//                return String.valueOf(sysstat.getSysstatCafA());
//            case "CAV_A":
//                return String.valueOf(sysstat.getSysstatCavA());
//            case "CAM_A":
//                return String.valueOf(sysstat.getSysstatCamA());
//            case "GPCAD_F":
//                return String.valueOf(sysstat.getSysstatGpcadF());
//            case "CAJ_A":
//                return String.valueOf(sysstat.getSysstatCajA());
//            case "CAA_I":
//                return String.valueOf(sysstat.getSysstatCaaI());
//            case "FWD_I":
//                return String.valueOf(sysstat.getSysstatFwdI());
//            case "ADM_I":
//                return String.valueOf(sysstat.getSysstatAdmI());
            case "CBS":
                return String.valueOf(sysstat.getSysstatCbs());
//            case "FEDI":
//                return String.valueOf(sysstat.getSysstatFedi());
//            case "NB":
//                return String.valueOf(sysstat.getSysstatNb());
//            case "WEBATM":
//                return String.valueOf(sysstat.getSysstatWebatm());
//            case "ASC_CHANNEL":
//                return String.valueOf(sysstat.getSysstatAscChannel());
//            case "ASC":
//                return String.valueOf(sysstat.getSysstatAsc());
//            case "ASCMD":
//                return String.valueOf(sysstat.getSysstatAscmd());
//            case "GCARD":
//                return String.valueOf(sysstat.getSysstatGcard());
//            case "ASCMAC":
//                return String.valueOf(sysstat.getSysstatAscmac());
//            case "SPS":
//                return String.valueOf(sysstat.getSysstatSps());
//            case "SPSMAC":
//                return String.valueOf(sysstat.getSysstatSpsmac());
//            case "AIG":
//                return String.valueOf(sysstat.getSysstatAig());
//            case "HK_ISSUE":
//                return String.valueOf(sysstat.getSysstatIssue());
//            case "HK_FISCMB":
//                return String.valueOf(sysstat.getSysstatHkFiscmb());
//            case "HK_PLUS":
//                return String.valueOf(sysstat.getSysstatHkPlus());
//            case "MO_ISSUE":
//                return String.valueOf(sysstat.getSysstatMoIssue());
//            case "MO_FISCMB":
//                return String.valueOf(sysstat.getSysstatMoFiscmb());
//            case "MO_PLUS":
//                return String.valueOf(sysstat.getSysstatMoPlus());
            case "AOCT_7300":
                return sysstat.getSysstatAoct7300();
            case "MBACT_7300":
                return sysstat.getSysstatMbact7300();
            case "T24_TWN":
                return String.valueOf(sysstat.getSysstatT24Twn());
            case "T24_HKG":
//                return String.valueOf(sysstat.getSysstatT24Hkg());
//            case "T24_MAC":
//                return String.valueOf(sysstat.getSysstatT24Mac());
//            case "CBS_HKG":
//                return sysstat.getSysstatCbsHkg();
//            case "CBS_MAC":
//                return sysstat.getSysstatCbsMac();
//            case "HK_FISCMQ":
//                return String.valueOf(sysstat.getSysstatHkFiscmq());
//            case "MO_FISCMQ":
//                return String.valueOf(sysstat.getSysstatMoFiscmq());
//            case "SVCS":
//                return String.valueOf(sysstat.getSysstatSvcs());
//            case "HK_SMS":
//                return String.valueOf(sysstat.getSysstatHkSms());
//            case "FAW_A":
//                return String.valueOf(sysstat.getSysstatFawA());
//            case "EAF_A":
//                return String.valueOf(sysstat.getSysstatEafA());
//            case "EAV_A":
//                return String.valueOf(sysstat.getSysstatEavA());
//            case "EAM_A":
//                return String.valueOf(sysstat.getSysstatEamA());
//            case "EWV_A":
//                return String.valueOf(sysstat.getSysstatEwvA());
//            case "EWM_A":
//                return String.valueOf(sysstat.getSysstatEwmA());
//            case "GPEMV_F":
//                return String.valueOf(sysstat.getSysstatGpemvF());
//            case "PURE":
//                return String.valueOf(sysstat.getSysstatPure());
//            case "IIQ_I":
//                return String.valueOf(sysstat.getSysstatIiqI());
//            case "IIQ_F":
//                return String.valueOf(sysstat.getSysstatIiqF());
//            case "IIQ_P":
//                return String.valueOf(sysstat.getSysstatIiqP());
//            case "IFT_P":
//                return String.valueOf(sysstat.getSysstatIftP());
//            case "ICCDP_P":
//                return String.valueOf(sysstat.getSysstatIccdpP());
//            case "IPY_P":
//                return String.valueOf(sysstat.getSysstatIpyP());
//            case "CDP_A":
//                return String.valueOf(sysstat.getSysstatCdpA());
//            case "CDP_P":
//                return String.valueOf(sysstat.getSysstatCdpP());
//            case "IQV_P":
//                return String.valueOf(sysstat.getSysstatIqvP());
//            case "IQM_P":
//                return String.valueOf(sysstat.getSysstatIqmP());
//            case "IQC_P":
//                return String.valueOf(sysstat.getSysstatIqcP());
//            case "EQP_P":
//                return String.valueOf(sysstat.getSysstatEqpP());
//            case "EQC_P":
//                return String.valueOf(sysstat.getSysstatEqcP());
//            case "EQU_P":
//                return String.valueOf(sysstat.getSysstatEquP());
//            case "ADM_A":
//                return String.valueOf(sysstat.getSysstatAdmA());
//            case "NWD_I":
//                return String.valueOf(sysstat.getSysstatNwdI());
//            case "NWD_A":
//                return String.valueOf(sysstat.getSysstatNwdA());
            case "AOCT_2570":
                return sysstat.getSysstatAoct2570();
            case "MBACT_2570":
                return sysstat.getSysstatMbact2570();
            case "GPIWD_F":
//                return String.valueOf(sysstat.getSysstatGpiwdF());
//            case "GPOB_F":
//                return String.valueOf(sysstat.getSysstatGpobF());
//            case "NFW_I":
//                return String.valueOf(sysstat.getSysstatNfwI());
//            case "VAA_F":
//                return String.valueOf(sysstat.getSysstatVaaF());
//            case "VAA_A":
//                return String.valueOf(sysstat.getSysstatVaaA());
//            case "PV":
//                return String.valueOf(sysstat.getSysstatPv());
//            case "SMTP":
//                return String.valueOf(sysstat.getSysstatSmtp());
//            case "KEYBLOCKST":
//                return sysstat.getSysstatkeyblocksy();
        }
        return null;
    }
}
