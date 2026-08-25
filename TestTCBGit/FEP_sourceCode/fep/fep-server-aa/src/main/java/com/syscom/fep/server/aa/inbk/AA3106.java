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
import com.syscom.fep.mybatis.model.Fcrmstat;
import com.syscom.fep.mybatis.model.Sysconf;
import com.syscom.fep.mybatis.model.Sysstat;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.server.common.business.cbsbusiness.CBS;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.FISCReturnCode;
import com.syscom.fep.vo.enums.IOReturnCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.List;

public class AA3106 extends INBKAABase {

    private FcrmstatExtMapper dbFCRMSTAT = SpringBeanFactoryUtil.getBean(FcrmstatExtMapper.class);
    private SysstatExtMapper dbSYSSTAT = SpringBeanFactoryUtil.getBean(SysstatExtMapper.class);
    private final NotifyHelper notifyHelper = SpringBeanFactoryUtil.getBean(NotifyHelper.class);
    private SysconfExtMapper sysconfMapper = SpringBeanFactoryUtil.getBean(SysconfExtMapper.class);
    /**
     * 共用變數宣告
     */
    private String programName = AA3106.class.getSimpleName();
    private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
    /**
     * 建構式
     * AA的建構式,在這邊初始化及設定其他相關變數
     *
     * @param txnData AA交易訊息物件(含Timeout, EJ, Channel, 上行電文及上行電文物件
     * @throws Exception e
     */
    public AA3106(FISCData txnData) throws Exception {
        super(txnData);
    }

    /**
     * AA進入點主程式
     * 程式進入點
     *
     * @return Response電文
     * @throws Exception e
     */
    @Override
    public String processRequestData() throws Exception {
        boolean needUpdateFEPTXN = false;
        try {
            // Modify by henny for 修改主流程AA一開始先INSERT FEPTXN後才檢核狀態並更新FEPTXN_AA_RC 20110610

            // 2.準備交易記錄檔
            _rtnCode = getFiscBusiness().prepareFeptxnOpc("3106");

            // 3.新增交易記錄檔
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = getFiscBusiness().insertFEPTxn();
                needUpdateFEPTXN = true;
            }
            // 1.檢查狀態
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = checkSYSSTAT();
            }
            // 4.組送財金Request 電文(OC035)
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = prepareForFiSc();
            }

            // 5.送財金
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = getFiscBusiness().sendRequestToFISCOpc();
            }

            // 6.處理財金Response電文(OC036)
            if (_rtnCode.equals(CommonReturnCode.Normal)) {
                _rtnCode = processResponse();
            }

            // 7.更新SYSSTAT檔案中M-BANK的狀態
            if (_rtnCode.equals(CommonReturnCode.Normal) && NormalRC.FISC_OK.equals(getFiscOPCRes().getResponseCode())) {
                getLogContext().setProgramName(ProgramName);
                _rtnCode = doBusiness();
            }

            // 8.更新交易記錄檔
            if (needUpdateFEPTXN) {
                _rtnCode = updateFEPTXN();
            }

            //add by Maxine on 2011/07/12 for 需顯示交易成功訊息於EMS
            getLogContext().setRemark("FepTxn.FEPTXN_REP_RC=" + feptxn.getFeptxnRepRc() + ";");
            getLogContext().setProgramName(programName);
            logMessage(Level.DEBUG, getLogContext());
            if (NormalRC.FISC_OK.equals(feptxn.getFeptxnRepRc())) {
                //20260414 新增交易，不須與主機同步
                if (!"2700".equals(getFiscOPCReq().getAPID())) {
                    this.sendtoCBS();
                }

                getLogContext().setpCode(feptxn.getFeptxnPcode());
                getLogContext().setDesBkno(feptxn.getFeptxnDesBkno());
                getLogContext().setFiscRC(NormalRC.FISC_OK);
                //OPC
                getLogContext().setMessage("1");
                switch (getFiscOPCReq().getAPID()) {
                    case "1000":
                        getLogContext().setMessageParm13("通匯各類子系統(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1100":
                        getLogContext().setMessageParm13("匯款類子系統(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1200":
                        getLogContext().setMessageParm13("代收款項類子系統(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1300":
                        getLogContext().setMessageParm13("代繳代發類子系統(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1400":
                        getLogContext().setMessageParm13("一般通信類子系統(" + getFiscOPCReq().getAPID() + ")");
                        break;
                    case "1600":
                        getLogContext().setMessageParm13("外幣通匯各類子系統(" + getFiscOPCReq().getAPID() + ")");
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
                getLogContext().setProgramName(programName);
                getLogContext().setRemark(TxHelper.getMessageFromFEPReturnCode(FISCReturnCode.MBankExceptionalCheckOut, getLogContext()));
                getLogContext().setProgramName(programName);
                logMessage(Level.DEBUG, getLogContext());
            }
            
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(programName + ".processRequestData");
            sendEMS(getLogContext());
        } finally {
            getLogContext().setProgramName(programName);
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
            getLogContext().setProgramName(this.aaName);
            getLogContext().setMessageFlowType(MessageFlow.Response);
            //modified by maxine on 2011/07/11 for 避免送多次EMS
            getLogContext().setRemark(getTxData().getTxObject().getDescription());
            //LogContext.Remark = TxHelper.GetMessageFromFEPReturnCode(_rtnCode)
            logMessage(Level.DEBUG, getLogContext());
        }
        return "";
    }

    /**
     * 1.檢查狀態
     * OPC電文檢核相關邏輯
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode checkSYSSTAT() {
        Fcrmstat defFCRMSTAT = new Fcrmstat();

        try {
            // 檢核本行OP狀態，必須等於3，FISC NOTICE CALL完成才可執行本交易
            // 0 - 日終 HOUSE KEEPING 完成
            // 1 - FISC Wakeup Call 完成或M-BANK Wakeup Call完成
            // 2 - FISC Key-Syn Call完成
            // 3 - FISC Notice Call完成
            // 9 - 參加單位關機交易完成
            // D - 參加單位不營業
            if (!"3".equals(SysStatus.getPropertyValue().getSysstatMboct())) {
                return FISCReturnCode.INBKStatusError;
            }

            // 檢核AP系統是否在正常狀態
            // 0 - 日終 HOUSE KEEPING 完成
            // 1 - AP CHECKIN
            // 2 - AP EXCPTIONAL CHECKOUT
            // A - 通匯匯出類作業 EXCPTIONAL CHECKOUT 完成
            // B - 通匯匯入類作業 EXCPTIONAL CHECKOUT 完成
            // 3 - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類未 EXCEPTIONAL CHECKOUT) (AP PRE-CHECKOUT交易由參加單位啟動)
            // 4 - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類未 EXCEPTIONAL CHECKOUT) (AP PRE-CHECKOUT交易由財金公司啟動)
            // C - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類為 EXCEPTIONAL CHECKOUT 狀態) (APPRE-CHECKOUT 交易由參加單位啟動)
            // D - AP 預定 CHECKOUT ，匯入類仍可處理 (匯入類為 EXCEPTIONAL CHECKOUT 狀態) (APPRE-CHECKOUT 交易由財金公司啟動)
            // 5 - AP CHECKOUT (由參加單位啟動）
            // 7 - AP CHECKOUT（由財金公司啟動）
            // 9 - EVENING CALL（由參加單位啟動）
            switch (getFiscOPCReq().getAPID()) {
                // 通匯全部
                case "1000":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1000()) &&
                            !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1000()) &&
                            !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1000())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                // 匯款
                case "1100":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1100()) &&
                            !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1100()) &&
                            !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1100())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                // 託收代收
                case "1200":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1200()) &&
                            !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1200()) &&
                            !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1200())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                // 代繳代發
                case "1300":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1300()) &&
                            !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1300()) &&
                            !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1300())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                // 一般通訊
                case "1400":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact1400()) &&
                            !"A".equals(SysStatus.getPropertyValue().getSysstatMbact1400()) &&
                            !"B".equals(SysStatus.getPropertyValue().getSysstatMbact1400())) {
                        return FISCReturnCode.INBKStatusError;
                    }
                    break;
                // 外幣通匯全部
                case "1600":
                    defFCRMSTAT.setFcrmstatCurrency(getFiscBusiness().getFeptxn().getFeptxnFiscCurMemo());
                    List<Fcrmstat> fcrmstatList = dbFCRMSTAT.queryByPrimaryKey(defFCRMSTAT);
                    if (fcrmstatList.size() > 1) {
                        if (!"1".equals(defFCRMSTAT.getFcrmstatMbactrm()) &&
                                !"A".equals(defFCRMSTAT.getFcrmstatMbactrm()) &&
                                !"B".equals(defFCRMSTAT.getFcrmstatMbactrm())) {
                            return FISCReturnCode.INBKStatusError;
                        }
                    } else {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK CD/ATM CWD STATUS
                case "2000":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2000())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK CD/ATM FUND TRANSFER STATUS
                case "2200":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2200())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統所有各類子系統狀態
                case "2500":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2500()) || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2510())
                            || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2520()) || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2530())
                            || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2540()) || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2550())
                            || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2560()) || !"1".equals(SysStatus.getPropertyValue().getSysstatMbact2570())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統提款作業狀態
                case "2510":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2510())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統轉帳作業狀態
                case "2520":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2520())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統繳款作業狀態
                case "2530":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2530())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統購貨作業狀態
                case "2540":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2540())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統預先授權作業狀態
                case "2550":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2550())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK晶片卡共用系統全國繳費作業狀態
                case "2560":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2560())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                //Fly 2017/05/15 for新增晶片金融卡跨國提款及消費扣款交易
                //M-BANK晶片卡共用系統跨國提款作業狀態
                case "2570":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2570())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                //2026/3/4 新增for 資金調撥(2700)
                //M-BANK資金調撥作業狀態
                case "2700":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact2700())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                //M-BANK轉帳退款類交易狀態
                case "7100":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact7100())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                // M-BANK FXML 跨行付款交
                case "7300":
                    if (!"1".equals(SysStatus.getPropertyValue().getSysstatMbact7300())) {
                        return FEPReturnCode.INBKStatusError;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            getLogContext().setProgramException(ex);
            getLogContext().setProgramName(programName + ".processRequestData");
            sendEMS(getLogContext());
            return CommonReturnCode.ProgramException;
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 4.組送財金Request 電文(OC035)
     * 組送財金Request電文(OC035)
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareForFiSc() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;

        //電文Header
        rtnCode = getFiscBusiness().prepareHeader("0600");
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        //電文Body
        rtnCode = prepareBody();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        //產生財金Flatfile電文
        rtnCode = getFiscOPCReq().makeFISCMsg();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }
        return rtnCode;
    }

    /**
     * 準備財的APData
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode prepareBody() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        String desReturnMac = StringUtils.EMPTY;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        // fiscOPCReq.APID已由UI傳入
        // fiscOPCReq.CUR 已由UI傳入

        //產生MAC()
        RefString mac = new RefString(getFiscOPCReq().getMAC());
        rtnCode = encHelper.makeOpcMac(getFiscOPCReq().getProcessingCode(), getFiscOPCReq().getMessageType(), mac);
        getFiscOPCReq().setMAC(mac.get());
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        // 產生Bitmap
        rtnCode = getFiscBusiness().makeBitmap(getFiscOPCReq().getMessageType(), getFiscOPCReq().getProcessingCode(), MessageFlow.Request);
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        return rtnCode;
    }

    /**
     * 6.處理財金Response電文(OC036)
     * 處理財金Response電文(OC036)
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode processResponse() {
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        @SuppressWarnings("unused")
        String desReturnMac = StringUtils.EMPTY;
        ENCHelper encHelper = new ENCHelper(getFiscBusiness().getFeptxn(), getTxData());

        getFiscBusiness().getFeptxn().setFeptxnMsgflow(FEPTxnMessageFlow.FISC_Response);

        //檢核Header(FISC RC="1001","1002","1003","1004","1005","1006"為Garbled交易送Garbled Message給FISC)
        rtnCode = getFiscBusiness().checkHeader(getFiscOPCRes(), false);
        if (rtnCode.equals(FISCReturnCode.MessageTypeError) ||
                rtnCode.equals(FISCReturnCode.TraceNumberDuplicate) ||
                rtnCode.equals(FISCReturnCode.OriginalMessageError) ||
                rtnCode.equals(FISCReturnCode.STANError) ||
                rtnCode.equals(FISCReturnCode.SenderIdError) ||
                rtnCode.equals(FISCReturnCode.CheckBitMapError)) {
            getFiscBusiness().sendGarbledMessage(getFiscOPCRes().getEj(), rtnCode, getFiscOPCRes());
            return rtnCode;
        }

        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        // 檢核APID
        if (!getFiscOPCReq().getAPID().equals(getFiscOPCRes().getAPID())) {
            // FISC RC=0401
            return FISCReturnCode.OriginalMessageDataError;
        }

        // 檢核MAC
        getFiscBusiness().getFeptxn().setFeptxnRepRc(getFiscOPCRes().getResponseCode());
        rtnCode = encHelper.checkOpcMac(getFiscOPCRes().getProcessingCode(), getFiscOPCRes().getMessageType(), getFiscOPCRes().getMAC());
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
            return rtnCode;
        }

        return rtnCode;
    }

    /**
     * 7.更新SYSSTAT檔案中M-BANK的狀態
     * 更新SYSSTAT檔案中M-BANK的狀態
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode doBusiness() throws Exception {
        @SuppressWarnings("unused")
        FEPReturnCode rtnCode = CommonReturnCode.Normal;
        Sysstat defSYSSTAT = new Sysstat();
        Fcrmstat defFCRMSTAT = new Fcrmstat();
        // modify 新增DBSYSSTAT的參數  20110412 by Husan
        defSYSSTAT.setLogAuditTrail(true);
//        dbSYSSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name
        //modify 新增DBFCRMSTAT的參數  20110412 by Husan
        defFCRMSTAT.setLogAuditTrail(true);
//        dbFCRMSTAT.UpdateSource = MethodBase.GetCurrentMethod().DeclaringType.FullName + "." + MethodBase.GetCurrentMethod().Name
        switch (getFiscOPCReq().getAPID()) {
            //M-BANK RM AP STATUS - 通匯全部
            case "1000":
                SysStatus.getPropertyValue().setSysstatMbact1000("2");
                break;
            //M-BANK RM AP STATUS - 匯款
            case "1100":
                SysStatus.getPropertyValue().setSysstatMbact1100("2");
                break;
            //M-BANK RM AP STATUS - 託收代收
            case "1200":
                SysStatus.getPropertyValue().setSysstatMbact1200("2");
                break;
            //M-BANK RM AP STATUS - 代繳代發
            case "1300":
                SysStatus.getPropertyValue().setSysstatMbact1300("2");
                break;
            //M-BANK RM AP STATUS - 一般通訊
            case "1400":
                SysStatus.getPropertyValue().setSysstatMbact1400("2");
                break;
            //外幣匯款系統
            case "1600":
                defFCRMSTAT.setFcrmstatMbactrm("2");
                break;
            // M-BANK CD/ATM CWD STATUS
            case "2000":
                SysStatus.getPropertyValue().setSysstatMbact2000("2");
                break;
            //M-BANK CD/ATM FUND TRANSFER STATUS
            case "2200":
                SysStatus.getPropertyValue().setSysstatMbact2200("2");
                break;
            //M-BANK晶片卡共用系統所有各類子系統狀態
            case "2500":
                SysStatus.getPropertyValue().setSysstatMbact2500("2");
                SysStatus.getPropertyValue().setSysstatMbact2510("2");
                SysStatus.getPropertyValue().setSysstatMbact2520("2");
                SysStatus.getPropertyValue().setSysstatMbact2530("2");
                SysStatus.getPropertyValue().setSysstatMbact2540("2");
                SysStatus.getPropertyValue().setSysstatMbact2550("2");
                SysStatus.getPropertyValue().setSysstatMbact2560("2");
                SysStatus.getPropertyValue().setSysstatMbact2570("2");
                break;
            //M-BANK晶片卡共用系統提款作業狀態
            case "2510":
                SysStatus.getPropertyValue().setSysstatMbact2510("2");
                break;
            //M-BANK晶片卡共用系統轉帳作業狀態
            case "2520":
                SysStatus.getPropertyValue().setSysstatMbact2520("2");
                break;
            //'M-BANK晶片卡共用系統繳款作業狀態
            case "2530":
                SysStatus.getPropertyValue().setSysstatMbact2530("2");
                break;
            //M-BANK晶片卡共用系統購貨作業狀態
            case "2540":
                SysStatus.getPropertyValue().setSysstatMbact2540("2");
                break;
            //M-BANK晶片卡共用系統預先授權作業狀態
            case "2550":
                SysStatus.getPropertyValue().setSysstatMbact2550("2");
                break;
            //M-BANK晶片卡共用系統全國繳費作業狀態
            case "2560":
                SysStatus.getPropertyValue().setSysstatMbact2560("2");
                break;
            //Fly 2017/05/15 for新增晶片金融卡跨國提款及消費扣款交易
            //M-BANK晶片卡共用系統跨國提款作業狀態
            case "2570":
                SysStatus.getPropertyValue().setSysstatMbact2570("2");
                break;
            //2026/3/4 新增for 資金調撥(2700)
            //M-BANK資金調撥作業狀態
            case "2700":
                SysStatus.getPropertyValue().setSysstatMbact2700("2");
                break;
            //M-BANK FEDI 轉帳退款類交易狀態
            case "7100":
                SysStatus.getPropertyValue().setSysstatMbact7100("2");
                break;
            //M-BANK FXML 跨行付款交易
            case "7300":
                SysStatus.getPropertyValue().setSysstatMbact7300("2");
                break;
            default:
                break;
        }

        //更新系統狀態
        if (!"1600".equals(getFiscOPCReq().getAPID())) {
            //台幣
            if (dbSYSSTAT.updateByPrimaryKey(SysStatus.getPropertyValue()) <= 0) {
                return IOReturnCode.SYSSTATUpdateError;
            }
        } else {
            //外幣
            defFCRMSTAT.setFcrmstatCurrency(getFiscBusiness().getFeptxn().getFeptxnFiscCurMemo());
            if (dbFCRMSTAT.updateByPrimaryKey(defFCRMSTAT) <= 0) {
                return IOReturnCode.FCRMSTATUpdateError;
            }
        }
        return CommonReturnCode.Normal;
    }

    /**
     * 7.更新交易記錄檔
     * 更新FEPTXN
     *
     * @return FEPReturnCode
     */
    private FEPReturnCode updateFEPTXN() {
        FEPReturnCode rtnCode;
        //fiscBusiness.FepTxn.FEPTXN_REP_RC = fiscOPCRes.ResponseCode
        getFiscBusiness().getFeptxn().setFeptxnAaRc(_rtnCode.getValue());

        getFiscBusiness().getFeptxn().setFeptxnAaComplete(DbHelper.toShort(true));
        /* 2026/05/12 2270 資金調撥服務 */
        if (getFiscBusiness().getFeptxn().getFeptxnRepRc().equals("0001")) {
            //處理結果=成功
            getFiscBusiness().getFeptxn().setFeptxnTxrust("A");
        } else {
            //處理結果=Reject
            getFiscBusiness().getFeptxn().setFeptxnTxrust("R");
        }

        rtnCode = getFiscBusiness().updateTxData();
        if (!rtnCode.equals(CommonReturnCode.Normal)) {
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
//                    String subject  = notifyTemplate.getSubject();
                    String body = String.format(
                            "您好，<BR><BR>FEP送出緊急停止跨行連線作業通知(3106)：APID %s，主機回覆錯誤 %s。<BR><BR>請確認主機目前狀態 !!",
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
