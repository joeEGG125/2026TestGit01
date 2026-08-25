package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.notify.NotifyHelper;
import com.syscom.fep.common.notify.NotifyHelperTemplateId;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.FcrmstatExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysconfExtMapper;
import com.syscom.fep.mybatis.ext.mapper.SysstatExtMapper;
import com.syscom.fep.mybatis.mapper.FcrmstatMapper;
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

public class AA3107 extends INBKAABase {
    //共用變數宣告
    FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    private final NotifyHelper notifyHelper = SpringBeanFactoryUtil.getBean(NotifyHelper.class);
    private SysconfExtMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    //建構式
    public AA3107(FISCData txnData) throws Exception {
        super(txnData);
    }

    //AA進入點主程式
    @Override
    public String processRequestData() {
        boolean needUpdateFEPTXN = false;
        try {
            //'Modify by henny for 修改主流程AA一開始先INSERT FEPTXN後才檢核狀態並更新FEPTXN_AA_RC 20110610
            //'2.準備交易記錄檔
            _rtnCode = getFiscBusiness().prepareFeptxnOpc("3107");

            //3.新增交易記錄檔
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().insertFEPTxn();
                needUpdateFEPTXN = true;
            }
            //1.檢查狀態
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = checkSYSSTAT();
            }
            //4.組送財金Request電文(OC038)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = prepareReqForFISC();
            }
            //5.送財金(Request)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = getFiscBusiness().sendRequestToFISCOpc();
            }
            //6.處理財金Respons電文(OC039)
            if (_rtnCode == CommonReturnCode.Normal) {
                _rtnCode = processResponse();
            }
            //7.更新交易記錄檔
            if (needUpdateFEPTXN) {
                getLogContext().setProgramName(ProgramName);
                _rtnCode = updateFEPTXN(MessageFlow.Response.getValue());
            }
            //8.組送財金Confirm 電文(OC040)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                //+的Response
                _rtnCode = prepareConForFISC();
            } else {
                //-的Response
            }
            //9.更新SYSSTAT檔案中M-BANK的狀態(FISC RC="0001","3206"時才能做DoBusiness)
            if (_rtnCode == CommonReturnCode.Normal && (NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode()) || "3206".equals(getFiscOPCRes().getResponseCode()))) {
                _rtnCode = doBusiness();
            }
            //10.送財金(Confirm)
            if (_rtnCode == CommonReturnCode.Normal && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                //+的Response
                _rtnCode = getFiscBusiness().sendConfirmToFISCOpc();
            }

            //add by Maxine on 2011/07/06 for 需顯示交易成功訊息於EMS
            getLogContext().setRemark(StringUtils.join("_rtnCode=", _rtnCode.toString(), ";"));
            logMessage(Level.DEBUG, getLogContext());
            if (_rtnCode == CommonReturnCode.Normal) {
                //20260414 新增交易，不須與主機同步
                if (!"2700".equals(getFiscOPCReq().getAPID())) {
                    this.sendtoCBS();
                }
            	
                getLogContext().setpCode(getFeptxn().getFeptxnPcode());
                getLogContext().setDesBkno(getFeptxn().getFeptxnDesBkno());
                getLogContext().setFiscRC(NormalRC.FISC_OK);
                getLogContext().setMessageGroup("1"); //OPC
                switch (getFiscOPCReq().getAPID()) {
                    case "1000":
                        getLogContext().setMessageParm13("通匯各類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1100":
                        getLogContext().setMessageParm13("匯款類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1200":
                        getLogContext().setMessageParm13("代收款項類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1300":
                        getLogContext().setMessageParm13("代繳代發類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1400":
                        getLogContext().setMessageParm13("一般通信類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1600":
                        getLogContext().setMessageParm13("外幣通匯各類子系統" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "2000":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("CD/ATM 共用系統提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("CD/ATM 共用系統提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2200":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("CD/ATM 共用系統轉帳作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("CD/ATM 共用系統轉帳作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2500":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡共用系統(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡共用系統(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2510":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2520":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡轉帳作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡轉帳作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2530":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡繳款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡繳款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2540":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡消費扣款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡消費扣款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2550":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡預先授權作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡預先授權作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2560":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡全國繳費作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡全國繳費作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2570":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("晶片卡跨國提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("晶片卡跨國提款作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "2700":
                        if(_rtnCode != FEPReturnCode.Normal) { //2026/3/4 新增for 資金調撥(2700)
                            getLogContext().setMessageParm13("資金調撥作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("資金調撥作業" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                    case "7100":
                        getLogContext().setMessageParm13("轉帳退款類交易" + "(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "7300":
                        if(_rtnCode != FEPReturnCode.Normal) { //06/17 SendToCBS returncode
                            getLogContext().setMessageParm13("FXML跨行付款交易" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步失敗");
                        }else {
                            getLogContext().setMessageParm13("FXML跨行付款交易" + "(" + getFiscOPCReq().getAPID() + "),IMS主機同步成功");
                        }
                        break;
                }
                getLogContext().setProgramName(ProgramName);
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankExceptionalCheckIn, getLogContext()));
                getLogContext().setProgramName(ProgramName);
                logMessage(Level.DEBUG, getLogContext());
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processRequestData");
            sendEMS(getLogContext());
            return "";
        } finally {
            getLogContext().setProgramName(ProgramName);
            String UIMessage=""; // 增加 UI 顯示內容
            if (!CommonReturnCode.Normal.equals(_rtnCode)) {
                if (getFiscOPCRes() != null && StringUtils.isNotBlank(getFiscOPCRes().getResponseCode())) {
                    UIMessage = "財金回覆" + getFiscOPCRes().getResponseCode() ;
                } else {
                    // 如果沒有財金回覆，表示在內部檢核就被擋下，改顯示內部錯誤訊息
                    UIMessage = "內部處理失敗/系統狀態檢核未過: " + _rtnCode.name();
                }
            } else {
                UIMessage = "財金回覆" + getFiscOPCRes().getResponseCode() + "-" + TxHelper.getMessageFromFEPReturnCode(getFiscOPCRes().getResponseCode(), FEPChannel.FISC, getLogContext());
            }
            if ( StringUtils.isNotBlank(getLogContext().getMessageParm13()) ){
                UIMessage = UIMessage + ";" + getLogContext().getMessageParm13();
            }
            getTxData().getTxObject().setDescription(UIMessage);
            getLogContext().setProgramFlowType(ProgramFlow.AAOut);
            String fiscMsg = (getFiscOPCReq() != null) ? getFiscOPCReq().getFISCMessage() : "";
            getLogContext().setMessage(fiscMsg);
            getLogContext().setProgramName(StringUtils.join(this.aaName, ".processRequestData"));
            getLogContext().setMessageFlowType(MessageFlow.Confirmation);
            //modified by maxine on 2011/07/11 for 避免送多次EMS
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            //getLogContext().Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
            getLogContext().setProgramName(this.aaName);
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    //1.檢查狀態
    //''' <summary>
    //''' ATM電文檢核相關邏輯
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode checkSYSSTAT() {
        Fcrmstat defFCRMSTAT = new Fcrmstat();
        FcrmstatMapper dbFCRMSTAT = SpringBeanFactoryUtil.getBean(FcrmstatMapper.class);

        try {
            //'0 - 日終 HOUSE KEEPING 完成
            //'1 - FISC Wakeup Call 完成或M-BANK Wakeup Call完成
            //'2 - FISC Key-Syn Call完成
            //'3 - FISC Notice Call完成
            //'9 - 參加單位關機交易完成
            //'D - 參加單位不營業
            //'檢核本行OP狀態，必須等於3，FISC NOTICE CALL完成才可執行本交易
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
                return FISCReturnCode.INBKStatusError;
            }
            //'0 - 日終 HOUSE KEEPING 完成
            //'1 - AP CHECKIN
            //'2 - AP EXCPTIONAL CHECKOUT
            //'A - 通匯匯出類作業 EXCPTIONAL CHECKOUT 完成
            //'B - 通匯匯入類作業 EXCPTIONAL CHECKOUT 完成
            //'3 - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類未 EXCEPTIONAL CHECKOUT) (AP PRE-CHECKOUT交易由參加單位啟動)
            //'4 - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類未 EXCEPTIONAL CHECKOUT) (AP PRE-CHECKOUT交易由財金公司啟動)
            //'C - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類為 EXCEPTIONAL CHECKOUT 狀態) (APPRE-CHECKOUT 交易由參加單位啟動)
            //'D - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類為 EXCEPTIONAL CHECKOUT 狀態) (APPRE-CHECKOUT 交易由財金公司啟動)
            //'5 - AP CHECKOUT (由參加單位啟動）
            //'7 - AP CHECKOUT（由財金公司啟動）
            //'9 - EVENING CALL（由參加單位啟動）
            //'檢核AP系統是否在正常狀態
            switch (getFiscOPCReq().getAPID()) {
                case "1000": //通匯全部
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1000()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1000())
                            && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "1100": //匯款
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1100()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1100())
                            && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1100())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "1200": //託收代收
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1200()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1200())
                            && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1200())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "1300": //代繳代發
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1300()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1300())
                            && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1300())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "1400": //一般通訊
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1400()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1400())
                            && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1400())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "1600": //外幣通匯全部
                    defFCRMSTAT.setFcrmstatCurrency(getFiscBusiness().getFeptxn().getFeptxnFiscCurMemo());
                    if (dbFCRMSTAT.selectByPrimaryKey(defFCRMSTAT.getFcrmstatCurrency()) != null) {
                        if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact1000()) && !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1000())
                                && !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
                            return FISCReturnCode.INBKStatusError;
                        }
                    } else {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2000": //M-BANK CD/ATM CWD STATUS
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2000())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2200": //M-BANK CD/ATM FUND TRANSFER STATUS
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2200())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2500": //M-BANK晶片卡共用系統所有各類子系統狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2500()) || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2510())
                            || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2520()) || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2530())
                            || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2540()) || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2550())
                            || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2560()) || !"2".equals(SysStatus.getPropertyValue().getSysstatMbact2570())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2510": //M-BANK晶片卡共用系統提款作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2510())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2520": //M-BANK晶片卡共用系統轉帳作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2520())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2530": //M-BANK晶片卡共用系統繳款作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2530())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2540": //M-BANK晶片卡共用系統購貨作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2540())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2550": //M-BANK晶片卡共用系統預先授權作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2550())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "2560": //M-BANK晶片卡共用系統全國繳費作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2560())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                //Fly 2017/05/15 for新增晶片金融卡跨國提款及消費扣款交易
                case "2570": //M-BANK晶片卡共用系統跨國提款作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2570())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                //2026/3/4 新增for 資金調撥(2700)
                case "2700": //M-BANK資金調撥作業狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact2700())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "7100": //M-BANK FEDI 轉帳退款類交易狀態
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact7100())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                case "7300": //M-BANK FXM L跨行付款交易
                    if (!"2".equals(SysStatus.getPropertyValue().getSysstatMbact7300())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".checkSYSSTAT");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    //4.組送財金Request 電文(OC007)
    //''' <summary>
    //''' 組送財金Request 電文(OC007)
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode prepareReqForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //電文Header
        rtnCode = getFiscBusiness().prepareHeader("0600");
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //電文Body
        rtnCode = prepareReqBody();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }

        //產生財金Flatfile電文
        rtnCode = getFiscOPCReq().makeFISCMsg();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        }
        return rtnCode;
    }

    //''' <summary>
    //''' 準備財金的Request APData
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode prepareReqBody() {
        @SuppressWarnings("unused")
        String desReturnMac = "";
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        //'fiscOPCReq.APID已由UI傳入
        //'fiscOPCReq.CUR 已由UI傳入

        //產生MAC
        RefString refMac = new RefString(getFiscOPCReq().getMAC());
        _rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), refMac);
        getFiscOPCReq().setMAC(refMac.get());
        if (_rtnCode != CommonReturnCode.Normal) {
            return _rtnCode;
        }

        //產生Bitmap
        _rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
        if (_rtnCode != CommonReturnCode.Normal) {
            return _rtnCode;
        }
        return CommonReturnCode.Normal;
    }

    //6.處理財金Response電文(OC039)
    //''' <summary>
    //''' 處理財金Response電文(OC039)
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode processResponse() {
        getLogContext().setProgramName(ProgramName+".processResponse");
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        try {
            //檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC)
            rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
            if (rtnCode == FISCReturnCode.MessageTypeError ||
                    rtnCode == FISCReturnCode.TraceNumberDuplicate ||
                    rtnCode == FISCReturnCode.OriginalMessageError ||
                    rtnCode == FISCReturnCode.STANError ||
                    rtnCode == FISCReturnCode.SenderIdError ||
                    rtnCode == FISCReturnCode.CheckBitMapError) {
                getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
                return rtnCode;
            }

            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //檢核APID
            if (!getFiscOPCRes().getAPID().equals(getFiscOPCReq().getAPID())) {
                _rtnCode = FISCReturnCode.OriginalMessageError;
            }

            //檢核MAC
            getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
            rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".processResponse");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //8.組送財金Confirm 電文(OC040)
    //''' <summary>
    //''' 組送財金Confirm電文(OC040)
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    //''' <modify>
    //'''     <modifier>HusanYin</modifier>
    //'''     <reason>修正Const RC</reason>
    //'''     <date>2010/11/25</date>
    //''' </modify>
    private FEPReturnCode prepareConForFISC() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        try {
            getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCRes().getResponseCode());

            //電文Header
            rtnCode = getFiscBusiness().prepareHeader("0602");
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //電文Body
            rtnCode = prepareConBody();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //產生財金Flatfile電文
            rtnCode = getFiscOPCCon().makeFISCMsg();
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            //更新FEPTXN
            rtnCode = updateFEPTXN(MessageFlow.Confirmation.getValue());
            if (rtnCode != CommonReturnCode.Normal) {
                return rtnCode;
            }

            return rtnCode;
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareConForFISC");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
    }

    //''' <summary>
    //''' 準備財金的Confirm APData
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode prepareConBody() {
        try {
            ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

            getFiscOPCCon().setAPID(getFiscBusiness().getFeptxn().getFeptxnApid());

//            if ("1600".equals(getFiscBusiness().getFeptxn().getFeptxnApid())) {
//                getFiscOPCCon().setCUR(getFiscBusiness().getFeptxn().getFeptxnFiscCurMemo());
//            }

            //產生MAC
            RefString refMac = new RefString(getFiscOPCCon().getMAC());
            _rtnCode = encHelper.makeOpcMac(getFiscOPCCon().getProcessingCode(), getFiscOPCCon().getMessageType(), refMac);
            getFiscOPCCon().setMAC(refMac.get());
            if (_rtnCode != CommonReturnCode.Normal) {
                getFiscOPCCon().setResponseCode(TxHelper.getRCFromErrorCode(_rtnCode, FEPChannel.FISC));
            }

            //產生Bitmap
            _rtnCode = getFiscBusiness().makeBitmap(getFiscOPCCon().getMessageType(), getFiscOPCCon().getProcessingCode(), MessageFlow.Confirmation);
            if (_rtnCode != CommonReturnCode.Normal) {
                return _rtnCode;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(ProgramName + ".prepareConBody");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    //9.更新SYSSTAT檔案中M-BANK的狀態(FISC RC=0001,3206 時才能做DoBusiness)
    //''' <summary>
    //''' 更新SYSSTAT檔案中M-BANK的狀態
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode doBusiness() throws Exception {
        getLogContext().setProgramName(ProgramName + ".doBusiness");
        SysstatExtMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean("sysstatExtMapper");
        FcrmstatExtMapper dbFCRMSTAT = SpringBeanFactoryUtil.getBean("fcrmstatExtMapper");
        Fcrmstat defFCRMSTAT = new Fcrmstat();
        //modify 新增DBSYSSTAT的參數  20110412 by Husan
        //TODO
//        dbSYSSTAT.LogAuditTrail = True
//        dbSYSSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name
        //modify 新增DBFCRMSTAT的參數  20110412 by Husan
        //TODO
        //dbFCRMSTAT.LogAuditTrail = True
        //dbFCRMSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name

        switch (getFiscOPCReq().getAPID()) {
            case "1000": //M-BANK RM AP STATUS - 通匯全部
                SysStatus.getPropertyValue().setSysstatMbact1000("1");
                break;
            case "1100": //M-BANK RM AP STATUS - 匯款
                SysStatus.getPropertyValue().setSysstatMbact1100("1");
                break;
            case "1200": //M-BANK RM AP STATUS - 託收代收
                SysStatus.getPropertyValue().setSysstatMbact1200("1");
                break;
            case "1300": //M-BANK RM AP STATUS - 代繳代發
                SysStatus.getPropertyValue().setSysstatMbact1300("1");
                break;
            case "1400": //M-BANK RM AP STATUS - 一般通訊
                SysStatus.getPropertyValue().setSysstatMbact1400("1");
                break;
            case "1600": //外幣匯款系統*/
                defFCRMSTAT.setFcrmstatMbactrm("1");
                break;
            case "2000": //M-BANK CD/ATM CWD STATUS
                SysStatus.getPropertyValue().setSysstatMbact2000("1");
                break;
            case "2200": //M-BANK CD/ATM FUND TRANSFER STATUS
                SysStatus.getPropertyValue().setSysstatMbact2200("1");
                break;
            case "2500": //M-BANK晶片卡共用系統所有各類子系統狀態
                SysStatus.getPropertyValue().setSysstatMbact2500("1");
                SysStatus.getPropertyValue().setSysstatMbact2510("1");
                SysStatus.getPropertyValue().setSysstatMbact2520("1");
                SysStatus.getPropertyValue().setSysstatMbact2530("1");
                SysStatus.getPropertyValue().setSysstatMbact2540("1");
                SysStatus.getPropertyValue().setSysstatMbact2550("1");
                SysStatus.getPropertyValue().setSysstatMbact2560("1");
                SysStatus.getPropertyValue().setSysstatMbact2570("1");
                break;
            case "2510": //M-BANK晶片卡共用系統提款作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2510("1");
                break;
            case "2520": //M-BANK晶片卡共用系統轉帳作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2520("1");
                break;
            case "2530": //M-BANK晶片卡共用系統繳款作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2530("1");
                break;
            case "2540": //M-BANK晶片卡共用系統購貨作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2540("1");
                break;
            case "2550": //M-BANK晶片卡共用系統預先授權作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2550("1");
                break;
            case "2560": //M-BANK晶片卡共用系統全國繳費作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2560("1");
                break;
            //Fly 2017/05/15 for新增晶片金融卡跨國提款及消費扣款交易//
            case "2570": //M-BANK晶片卡共用系統跨國提款作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2570("1");
                break;
            //2026/3/4 新增for 資金調撥(2700)
            case "2700": //M-BANK資金調撥作業狀態
                SysStatus.getPropertyValue().setSysstatMbact2700("1");
                break;
            case "7100": //M-BANK FEDI 轉帳退款類交易狀態
                SysStatus.getPropertyValue().setSysstatMbact7100("1");
                break;
            case "7300": //M-BANK FXML 跨行付款交易
                SysStatus.getPropertyValue().setSysstatMbact7300("1");
                break;
        }

        switch (getFiscOPCReq().getAPID()) {
            case "1600": //外幣匯款系統
                defFCRMSTAT.setFcrmstatCurrency("001");
                if (dbFCRMSTAT.updateByPrimaryKeySelective(defFCRMSTAT) < 0) {
                    return IOReturnCode.FCRMSTATUpdateError;
                }
                break;
            default:
                if (dbSYSSTAT.updateByPrimaryKeySelective(SysStatus.getPropertyValue()) < 0) {
                    return IOReturnCode.SYSSTATUpdateError;
                }
                break;
        }
        return CommonReturnCode.Normal;
    }

    //更新交易記錄檔
    //''' <summary>
    //''' 更新FEPTXN
    //''' </summary>
    //''' <returns></returns>
    //''' <remarks></remarks>
    private FEPReturnCode updateFEPTXN(Integer flow) {
        FEPReturnCode rtnCode = null;
        getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());
        switch (MessageFlow.fromValue(flow)) {
            case Response:
                //fiscBusiness.FepTxn.FEPTXN_REP_RC = fiscOPCRes.ResponseCode
                /* 2026/05/12 2270 資金調撥服務 */
                if (getFiscBusiness().getFeptxn().getFeptxnRepRc().equals("0001")) {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("A"); //處理結果=成功
                } else {
                    getFiscBusiness().getFeptxn().setFeptxnTxrust("R"); //處理結果=Reject
                }
                break;
            case Confirmation:
                getFiscBusiness().getFeptxn().setFeptxnConRc(getFiscOPCReq().getResponseCode());
                getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Confirm);
                getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
                break;
            default:
                break;
        }

        rtnCode = getFiscBusiness().updateTxData();
        if (rtnCode != CommonReturnCode.Normal) {
            return rtnCode;
        } else {
            return _rtnCode;
        }
    }
    
    private void sendtoCBS() {
        try {
            String AATxTYPE = "0";
            String AA = "CBAPIDSTAT"; // 電文cbsprocess
            ACBSAction hostAA = (ACBSAction) this.getInstanceObject(AA, getTxData());
            _rtnCode = new CBS(hostAA, getTxData()).sendToCBS(AATxTYPE);
            Object tota = hostAA.getTota();
//            String noticeEmail = this.getImsPropertiesValue(tota, ImsMethodName.NOTICE_EMAIL.getValue());

            //主機回覆錯誤，發MAIL通知
            if(_rtnCode != FEPReturnCode.Normal ) {
                if (feptxn.getFeptxnCbsTimeout() == 0) {
                    Sysconf sysconf = sysconfMapper.selectByPrimaryKey( (short) 1, "ExCheckErrMail");
                    String to = sysconf != null ? sysconf.getSysconfValue() : "";
                    String[] toEmails = new String[0];
                    if(StringUtils.isBlank(to)){
                        toEmails[0] = "AP1_FEP@tcb-bank.com.tw";
                    }else{
                        toEmails = to.split(";");
                    }
                    String TemplateId = NotifyHelperTemplateId.SYS_NOTICE;
//                    String subject = notifyTemplate.getSubject();
                    String body = String.format(
                            "您好，<BR><BR>FEP送出緊急停止跨行連線作業後重新啟動通知(3107)：APID %s，主機回覆錯誤 %s。<BR><BR>請確認主機目前狀態 !!",
                            feptxn.getFeptxnApid(),
                            feptxn.getFeptxnCbsRc()
                    );
//                    notifyTemplate.setTemplate(body);
                    for (String email : toEmails) {
                        try {
                            notifyHelper.sendSimpleMail(TemplateId, email, body, true);
                            getLogContext().setRemark(StringUtils.join("Send Notify Mail succeed, mailAddress:", email, ", body:", body));
                            logMessage(getLogContext());
                        } catch (Exception e) {
                            getLogContext().setProgramException(e);
                            getLogContext().setRemark("sendAlertMail with exception occur,mailAddress" + email);
                            sendEMS(getLogContext());
                        }
                    }
                }else {
                    getLogContext().setProgramName(ProgramName + ".sendToCBS");
                    getLogContext().setMessage("CBS TIMEOUT.");
                    sendEMS(getLogContext());
                }
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(StringUtils.join(ProgramName, ".sendToCBS"));
            sendEMS(getLogContext());
        }
    }
}
