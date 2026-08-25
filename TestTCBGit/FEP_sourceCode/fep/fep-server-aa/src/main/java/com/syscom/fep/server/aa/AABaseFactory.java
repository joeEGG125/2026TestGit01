package com.syscom.fep.server.aa;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.*;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.server.aa.atmp.ATMPAABase;
import com.syscom.fep.server.aa.inbk.*;
import com.syscom.fep.server.aa.rm.*;
import com.syscom.fep.vo.text.fisc.FISCHeader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component("aaBaseFactory")
@Lazy
public class AABaseFactory {

    @PostConstruct
    public void initialization() {
        LogHelperFactory.getGeneralLogger().info("#####################AABaseFactory has been register as Spring Bean....");
    }

    /**
     * run with ATM AA Program
     *
     * @param atmData
     * @return
     * @throws Exception
     */
    public String processRequestData(ATMData atmData) throws Exception {
        // 2010-09-28 modified by HusanYin for judge ATMP or INBK
        int subsys = (int) atmData.getMsgCtl().getMsgctlSubsys();// 得到為哪個子系統
        // ATMP
        if (SubSystem.ATMP == SubSystem.fromValue(subsys)) {
            return this.processAtmpRequestData(atmData);
        }
        // INBK
        else {
            return this.processInbkRequestData(atmData);
        }
    }

//    public String processRequestData2160(INBKData inbkData) throws Exception {
//        return this.processInbkRequestData2160(inbkData);
//    }


    /**
     * run with ATM AA Program
     *
     * @param
     * @return
     * @throws Exception
     */
    public String processHceRequestData(HCEData hceData) throws Exception {
        // 2010-09-28 modified by HusanYin for judge ATMP or INBK
        int subsys = (int) hceData.getMsgCtl().getMsgctlSubsys();// 得到為哪個子系統
        // ATMP
        if (SubSystem.ATMP == SubSystem.fromValue(subsys)) {
            return this.processHceAtmpRequestData(hceData);
        }
        // INBK
        else {
            return this.processHceInbkRequestData(hceData);
        }
    }


    public String processNbRequestData(NBData nbData) throws Exception {
        // 2010-09-28 modified by HusanYin for judge ATMP or INBK
        int subsys = (int) nbData.getMsgCtl().getMsgctlSubsys();// 得到為哪個子系統
        // ATMP
        if (SubSystem.ATMP == SubSystem.fromValue(subsys)) {
            return this.processNbAtmpRequestData(nbData);
        }
        // INBK
        else {
            return this.processNbInbkRequestData(nbData);
        }
    }

    public String processVARequestData(NBData nbData) throws Exception {
        // 2010-09-28 modified by HusanYin for judge ATMP or INBK
        int subsys = (int) nbData.getMsgCtl().getMsgctlSubsys();// 得到為哪個子系統
        // ATMP
        if (SubSystem.ATMP == SubSystem.fromValue(subsys)) {
            return this.processVAAtmpRequestData(nbData);
        }
        // INBK
        else {
            return this.processVAInbkRequestData(nbData);
        }
    }

    public String processMBRequestData(NBData nbData) throws Exception {
        // 2010-09-28 modified by HusanYin for judge ATMP or INBK
        int subsys = (int) nbData.getMsgCtl().getMsgctlSubsys();// 得到為哪個子系統
        // ATMP
        if (SubSystem.ATMP == SubSystem.fromValue(subsys)) {
            return this.processVAAtmpRequestData(nbData);
        }
        // INBK
        else {
            return this.processVAInbkRequestData(nbData);
        }
    }

    public String processMFTRequestData(MFTData mftData) throws Exception {
        // ATMP
        return this.processMFTAtmpRequestData(mftData);

    }

    /**
     * 2016-06-21 Modify by Ruling for COMBO開卡作業優化
     * 2016-07-15 Modify by Ruling for ATM新功能
     * 2016-02-14 Modify by Ruling for 無卡提款
     *
     * @return
     * @throws Exception
     */
    public String processAtmpRequestData(ATMData atmData) throws Exception {
        // [20221226]改寫成動態取得AA
        ATMPAABase aa = null;
        try {
            Class<?>[] params = {ATMData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.atmp." + atmData.getAaName());
            // 獲得AA
            aa = (ATMPAABase) c.getConstructor(params).newInstance(atmData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        }
        catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", atmData.getAaName());
        }
    }

    public String processHceAtmpRequestData(HCEData hceData) throws Exception {
        // [20221226]改寫成動態取得AA
        ATMPAABase aa = null;
        try {
            Class<?>[] params = {HCEData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.hce." + hceData.getAaName());
            // 獲得AA
            aa = (ATMPAABase) c.getConstructor(params).newInstance(hceData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", hceData.getAaName());
        }

//        ATMPAABase aa = null;
//        switch (atmData.getAaName()) {
//            case "HCEIQSelfIssue":
//                aa = new HCEIQSelfIssue(atmData);
//                break;
////	        case "EATM_IQSelfIssue":
////	            aa = new EATMIQSelfIssue(atmData);
////	            break;
//            default:
//                throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", atmData.getAaName());
//        }
//        LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processHceAtmpRequestData()...");
//        return aa.processRequestData();
    }

    public String processVAAtmpRequestData(NBData nbData) throws Exception {
        // [20221226]改寫成動態取得AA
        ATMPAABase aa = null;
        try {
            Class<?>[] params = {NBData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.va." + nbData.getAaName());
            // 獲得AA
            aa = (ATMPAABase) c.getConstructor(params).newInstance(nbData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
        }
    }

    public String processNbAtmpRequestData(NBData nbData) throws Exception {
        // [20221226]改寫成動態取得AA
        ATMPAABase aa = null;
        try {
            Class<?>[] params = {NBData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.nb." + nbData.getAaName());
            // 獲得AA
            aa = (ATMPAABase) c.getConstructor(params).newInstance(nbData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
        }

//        ATMPAABase aa = null;
//        switch (nbData.getAaName()) {
//            case "NBPYSelfIssue":
//                aa = new NBPYSelfIssue(nbData);
//                break;
//            default:
//                throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
//        }
//        LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processHceAtmpRequestData()...");
//        return aa.processRequestData();
    }

    public String processMFTAtmpRequestData(MFTData mftData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {MFTData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.inbk." + mftData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(mftData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", mftData.getAaName());
        }
    }

    public String processHceInbkRequestData(HCEData hceData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {HCEData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.hce." + hceData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(hceData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", hceData.getAaName());
        }
//        INBKAABase aa = null;
//        switch (atmData.getAaName()) {
//            case "HCEOtherIssueRequestA":
//                aa = new HCEOtherIssueRequestA(atmData);
//                break;
//            case "HCESelfIssueRequestA":
//                aa = new HCESelfIssueRequestA(atmData);
//                break;
//            default:
//                throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", atmData.getAaName());
//        }
//        LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processHceInbkRequestData()...");
//        return aa.processRequestData();
    }

    public String processIVRInbkRequestData(IVRData ivrData) throws Exception {
        INBKAABase aa = null;
        try {
            Class<?>[] params = {IVRData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.ivr." + ivrData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(ivrData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", ivrData.getAaName());
        }
    }

    public String processVAInbkRequestData(NBData nbData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {NBData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.va." + nbData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(nbData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
        }
    }

    public String processNbInbkRequestData(NBData nbData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {NBData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.nb." + nbData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(nbData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
        }
//        INBKAABase aa = null;
//        switch (nbData.getAaName()) {
//        case "NBPYOtherRequestA":
//            aa = new NBPYOtherRequestA(nbData);
//            break;
//        case "NBSelfIssueRequestA":
//            aa = new NBSelfIssueRequestA(nbData);
//            break;
//            default:
//                throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", nbData.getAaName());
//        }
//        LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processHceInbkRequestData()...");
//        return aa.processRequestData();
    }

    /**
     * 2010-09-28 Added by HusanYin for 啟動INBKAA
     * 2010-09-28 modify by HusanYin for 啟動 OtherIssueResquestA
     * 2015-03-31 Modify by Ruling for 跨行提領外幣
     * 2015-09-04 Modify by Fly for EMV 拒絕磁條卡交易
     * 2020-09-18 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈增加代理AA
     *
     * @param atmData
     * @return
     * @throws Exception
     */
    public String processInbkRequestData(ATMData atmData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {ATMData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.inbk." + atmData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(atmData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", atmData.getAaName());
        }
    }
    public String processATMInbkRequestData(ATMData atmData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {ATMData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.atmp." + atmData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(atmData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", atmData.getAaName());
        }
    }
    public String processInbkRequestData2160(INBKData inbkData) throws Exception {
        // [20221226]改寫成動態取得AA
        INBKAABase aa = null;
        try {
            Class<?>[] params = {INBKData.class};
            Class<?> c = Class.forName("com.syscom.fep.server.aa.inbk." + inbkData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(inbkData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", inbkData.getAaName());
        }
    }

    /**
     * run with FISC AA Program
     *
     * @param msgId
     * @param msgReq
     * @param fData
     * @return
     * @throws Exception
     */
    public String processInbkRequestData(String msgId, FISCHeader msgReq, FISCData fData) throws Exception {
        INBKAABase aa = null;
        String fiscRes = StringUtils.EMPTY;
        // 2018-11-15 Modify by 2566約定及核驗服務類別10，增加256600及256602呼叫的AA
        switch (msgId) {
            case "010200":
                aa = new AA0102(fData);
                break;
            case "310000":
                aa = new AA3100(fData);
                break;
            case "310100":
                aa = new AA3101(fData);
                break;
            case "310110":
                aa = new AA3101L(fData);
                break;
            case "310600":
                aa = new AA3106(fData);
                break;
            case "310700":
                aa = new AA3107(fData);
                break;
            case "310710":
                aa = new AA3107L(fData);
                break;
            case "310900":
                aa = new AA3109(fData);
                break;
            case "311400":
                aa = new AA3114(fData); // han add
                break;
            case "311500":
                aa = new AA3115(fData); // han add
                break;
            case "010300":
            case "010500":
            case "010700": //05-21 依Sarah 新增0107
                aa = new FISCChangeKey(fData);
                break;
            case "010100":
            case "320500":
            case "320600":
            case "321500":
                aa = new FISCRequest(fData);
                break;
            case "320100":
                aa = new AA3201(fData);
                break;
            case "320700":
                aa = new AA3207(fData);
                break;
            case "320999":
                aa = new AA3209(fData);
                break;
            case "321000":
            case "321100":
                aa = new FISCCheckoutCall(fData);
                break;
            case "250000":
                aa = new INQRequstI(fData);
                break;
            case "251000":
                aa = new WDRequestI(fData);
                break;
            case "252100":
            case "252200":
            case "252300":
            case "252400":
                aa = new IFTRequestI(fData);
                break;
            case "253100":
                aa = new TAXRequestI(fData);
                break;
            case "253102":
                aa = new TAXConfirmI(fData);
                break;
            case "226100":
            case "256100":
            case "226200":
            case "256200":
            case "226300":
            case "256300":
            case "226400":
            case "256400":
            case "256800":
            case "256900":
                aa = new PAYRequestI(fData);
                break;
            case "241000":
            case "241100":
            case "243000":
            case "245000":
            case "245100":
            case "247000":
                aa = new INTWDRequestI(fData);
                break;
            case "252500":
            case "254100":
            case "254200":
            case "254300":
            case "255100":
            case "255200":
                aa = new PurchaseRequestI(fData);
                break;
            case "252502":
            case "254102":
            case "254202":
            case "254302":
                aa = new PurchaseConfirmI(fData);
                break;
            case "226102":
            case "256102":
            case "226202":
            case "256202":
            case "226302":
            case "256302":
            case "226402":
            case "256402":
            case "256802":
            case "256902":
                aa = new PAYConfirmI(fData);
                break;
            case "245002":
            case "241002":
            case "243002":
            case "247002":
                aa = new INTWDConfirmI(fData);
                break;
            case "252102":
            case "252202":
            case "252302":
            case "252402":
                aa = new IFTConfirmI(fData);
                break;
            case "251002":
                aa = new WDConfirmI(fData);
                break;
//            case "2440":
//            case "2441":
//            case "2445":
            case "255102":
            case "255202":
//                aa = new CommonConfirmI(fData);
                break;
            case "262000":
            case "262100":
            case "262300":
                aa = new EMVRequestI3(fData);
                break;
            case "263000":
            case "263100":
            case "263300":
//                aa = new EMVRequestI(fData);
                break;
            case "263002":
//                aa = new EMVConfirmI(fData);
                break;
            case "212000":
            case "215000":
            case "226000":
            case "227000":
                aa = new PendingAskFromFISC(fData);
                break;
            case "228000":
                aa = new PendingAskFromBank2280(fData);
                break;
            case "229000":
                aa = new PendingAskFromBank2290(fData);
                break;
            case "213000":
                aa = new AA2130(fData);
                break;
            case "214000":
                aa = new AA2140(fData);
                break;
            // case "200002":
            // aa = new SendConfirmByManual(fData);
            // break;
            case "520100":
                aa = new AA5201(fData);
                break;
            case "520200":
                aa = new AA5202(fData);
                break;
            case "500181":
            case "531200":
            case "531400":
            case "510200":
                aa = new CLRequestFromFISC(fData);
                break;
            case "580100":
            case "581200":
            case "581500":
                // aa = new CLRequestFromFISCFCur(fData);
                break;
            case "580200":
                // aa = new AA5802(fData);
                break;
            case "250500":
            case "254500":
            case "254600":
            case "257100":
            case "257200":
                aa = new ICRequestI(fData);
                break;
            case "254502":
            case "254602":
            case "257102":
            case "257202":
                aa = new ICConfirmI(fData);
                break;
            case "257300":
            case "254900":
                aa = new AA2573(fData);
                break;
            case "257400":
                aa = new AA2574(fData);
                break;
            case "255500":
            case "255600":
                aa = new OBRequestI(fData);
                break;
            case "255502":
            case "255602":
                aa = new OBConfirmI(fData);
                break;
            case "256600":
                aa = new VARequestI(fData);
                break;
            case "256602":
                aa = new VAConfirmI(fData);
                break;
            case "270000":
                aa = new FXRequestI(fData);
                break;
            case "270002":
                aa = new FXConfirmI(fData);
                break;
            default:
//                aa = new CommonRequestI(fData);
                break;
        }
        if ("0210".equals(msgReq.getMessageType())) {
            // TODO
            // aa = new LateResponse(fData);
        }
        if ("0202".equals(msgReq.getMessageType()) && "2000".equals(msgReq.getProcessingCode())) {
            aa = new SendConfirmByManual(fData);
        }
        if (aa != null) {
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processRequestData() for msgId = [", msgId, "]...");
            fiscRes = aa.processRequestData();
        }
        return fiscRes;
    }

    /**
     * 全國性繳費稅委託單位資料查詢
     * AA0011
     *
     * @param msgId
     * @param txINBKData
     * @return
     * @throws Exception
     */
    public String processInbkRequestData(String msgId, INBKData txINBKData) throws Exception {
        INBKAABase aa = null;
        switch (msgId) {
            case "S0710":
                aa = new AA0011(txINBKData);
                break;
            case "531300":
                aa = new AA5313(txINBKData); // 全國性繳費稅委託單位資料查詢
                break;
            default:
                throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", msgId);
        }
        LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processRequestData()...");
        return aa.processRequestData();
    }

    /**
     * 呼叫RM AA的程式
     *
     * @param msgId
     * @param msgReq
     * @param fData
     * @return
     * @throws Exception
     */
    public String processRmRequestData(String msgId, FISCHeader msgReq, FISCData fData) throws Exception {
        RMAABase aa = null;
        String fiscRes = StringUtils.EMPTY;
        switch (msgId) {
            case "111200":
            case "112200":
            case "113200":
            case "117200":
            case "118200":
            case "119200":
                aa = new AA11X2(fData);
                break;
            case "151200":
                aa = new AA1512(fData);
                break;
            case "141200":
                aa = new AA1412(fData);
                break;
            case "111110":
            case "112110":
            case "113110":
            case "117110":
            case "118110":
            case "119110":
            case "141110":
            case "151110":
            case "161110":
            case "164110":
            case "165110":
                aa = new AARMLateResponse(fData);
                break;
            case "151100":
                aa = new AA1511(fData);
                break;
            case "151300":
                aa = new AA1513(fData);
                break;
            case "151400":
                aa = new AA1514(fData);
                break;
            case "151500":
                aa = new AA1515(fData);
                break;
            default:
                break;
        }
        if (aa != null) {
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processRmRequestData() for msgId = [", msgId, "]...");
            fiscRes = aa.processRequestData();
        }
        return fiscRes;
    }

    /**
     * 呼叫RM AA的程式
     *
     * @param msgId
     * @param txBRSData
     * @return
     * @throws Exception
     */
    public String processRmRequestData(String msgId, RMData txBRSData) throws Exception {
        RMAABase aa = null;
        String rmRes = StringUtils.EMPTY;
        switch (msgId) {
            case "R1200":
                // aa = new R1200(txBRSData);
                break;
            case "R0100":
                // aa = new R0100(txBRSData);
                break;
            case "R0101":
                // aa = new R0101(txBRSData);
                break;
            case "R0230":
                // aa = new R0230(txBRSData);
                break;
            case "R0231":
                // aa = new R0231(txBRSData);
                break;
            case "RT0011":
                // aa = new RT0011(txBRSData);
                break;
            case "RIM001":
                // aa = new RIM001(txBRSData);
                break;
            case "RIM002":
                // aa = new RIM002(txBRSData);
                break;
            case "RIM003":
                // aa = new RIM003(txBRSData);
                break;
            // case "RIM004":
            // aa = new RIM004(txBRSData);
            // break;
            // case "RIM005":
            // aa = new RIM005(txBRSData);
            // break;
            // case "RIM006":
            // aa = new RIM006(txBRSData);
            // break;
            // case "RIM007":
            // aa = new RIM007(txBRSData);
            // break;
            case "RIM011":
                // aa = new RIM011(txBRSData);
                break;
            case "R1400":
                // aa = new R1400(txBRSData);
                break;
            case "C1400":
                // aa = new C1400(txBRSData);
                break;
            case "RT1301":
                // aa = new RT1301(txBRSData);
                break;
            case "R0140":
                // aa = new R0140(txBRSData);
                break;
            case "C1100":
                aa = new C1100(txBRSData);
                break;
            case "C1000":
                // aa = new C1000(txBRSData);
                break;
            case "C1200":
                // aa = new C1200(txBRSData);
                break;
            case "R1600":
                // aa = new R1600(txBRSData);
                break;
            case "C1600":
                // aa = new C1600(txBRSData);
                break;
            case "C1700":
                aa = new C1700(txBRSData);
                break;
            case "R1100":
                // aa = new R1100(txBRSData);
                break;
            case "R1001":
                aa = new R1001(txBRSData);
                // FCSHandler add
                FEPBase fepBase = new FEPBase();
                aa.setEj(fepBase.getEj());
                break;
            case "R0800":
                // aa = new R0800(txBRSData);
                break;
            case "R2300":
                // aa = new R2300(txBRSData);
                break;
            case "RT2301":
                // aa = new RT2301(txBRSData);
                break;
            case "R2400":
                // aa = new R2400(txBRSData);
                break;
            case "RT1101":
                // aa = new RT1101(txBRSData);
                break;
            case "R1000":
                // aa = new R1000(txBRSData);
                break;
            case "R5100":
                // aa = new R5100(txBRSData);
                break;
            case "FFB001":
                aa = new FFB001(txBRSData);
                break;
            default:
                break;
        }
        if (aa != null) {
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(), ".processRmRequestData() for msgId = [", msgId, "]...");
            rmRes = aa.processRequestData();
        } else {
            /*
             * logContext.ReturnCode = CommonReturnCode.MessageIDError; TODO
             * sendEMS(LogContext);
             */
        }
        return rmRes;
    }

    public String processRequestIMSData(FISCData fData) throws Exception {
        INBKAABase aa = null;
        try {
            Class<?>[] params = {FISCData.class };
            Class<?> c = Class.forName("com.syscom.fep.server.aa.ims." + fData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(fData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", fData.getAaName());
        }
    }

    public String processRequestYData(FISCData fData) throws Exception {
        INBKAABase aa = null;
        try {
            Class<?>[] params = {FISCData.class };
            Class<?> c = Class.forName("com.syscom.fep.server.aa.inbk." + fData.getAaName());
            // 獲得AA
            aa = (INBKAABase) c.getConstructor(params).newInstance(fData);
            LogHelperFactory.getGeneralLogger().info("start to envoke ", aa.getClass().getSimpleName(),
                    ".processRequestData()...");
            return aa.processRequestData();
        } catch (ClassNotFoundException e) {
            throw ExceptionUtil.createIllegalArgumentException("Not implement for aa program = ", fData.getAaName());
        }
    }

    /**
     * 20221215 Bruce add 動態取得AA物件
     *
     * @param
     * @return
     * @throws Exception
     */
    public Object getInstanceObject(String channel, String aaName, MessageBase atmData) {
        Class<?> c = null;
        //java.lang.reflect.Field[] fields = null;
        //String imsPackageName = "";
        //Class<?> imsClassName = null;
        Class<?>[] params = {MessageBase.class};
        Object o = null;
        try {
            //獲得cbsProcessn物件名稱
            c = Class.forName("com.syscom.fep.server.aa." + channel + "." + aaName);
            //獲得cbsProcessn物件裡所有欄位名稱
            //fields = c.getDeclaredFields();
            //使用cbsProcessn以獲得ims電文物件名稱
            //imsPackageName = fields[0].toString().split(" ")[1];
            //獲得ims電文物件名稱
            //imsClassName = Class.forName(imsPackageName);
            //獲得ims實例化電文物件
            o = c.getConstructor(params).newInstance(atmData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return o;
    }
}