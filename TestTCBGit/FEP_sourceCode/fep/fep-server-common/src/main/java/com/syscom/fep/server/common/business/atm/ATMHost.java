package com.syscom.fep.server.common.business.atm;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.enchelper.ENCHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.frmcommon.util.XmlUtil;
import com.syscom.fep.mybatis.util.SpCaller;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.IVRAdapter;
import com.syscom.fep.server.common.adapter.NBAdapter;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.nb.NBGeneral;
import com.syscom.fep.vo.text.nb.request.ANBRequest;
import com.syscom.fep.vo.text.nb.request.ANBRequest.ANBRq;
import com.syscom.fep.vo.text.nb.request.ANBRequest.ANBSvcRq;
import com.syscom.fep.vo.text.nb.response.ANBResponse;
import com.syscom.fep.vo.text.nb.response.ANBResponse.ANBRs;
import com.syscom.fep.vo.text.nb.response.ANBResponse.ANBSvcRs;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;
import org.slf4j.event.Level;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ATMHost extends ATMCommon {

    protected ATMHost() {
        super();
    }

    protected ATMHost(ATMData atmMsg, String api) {
        super(atmMsg, api);
    }

    protected ATMHost(ATMData atmMsg) throws Exception {
        super(atmMsg);
    }

    // 組電文至 IVR 系統驗密
    public final FEPReturnCode sendToIVR() {
        ENCHelper encHelper = null;
        FEPReturnCode rtn = FEPReturnCode.Abnormal;
        RefString rtnData = new RefString();
        ;
        IVRAdapter ivrAdapter = null;
        try {
            encHelper = new ENCHelper(getFeptxn());

            // ATM電文轉換 PIN BLOCK
            if (StringUtils.isNotBlank(getFeptxn().getFeptxnPinblock())
                    && StringUtils.isNotBlank(getFeptxn().getFeptxnPinblock().trim())) {
                rtn = encHelper.pinBlockConvert(getFeptxn().getFeptxnPinblock(), rtnData); /// *密碼轉換*/
                if (rtn != FEPReturnCode.Normal) {
                    return rtn;
                }
            }

            getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true)); /// * CBS 逾時 FLAG */
            if (feptxnDao.updateByPrimaryKeySelective(getFeptxn()) <= 0) // RC = L013
            {
                return IOReturnCode.FEPTXNUpdateError;
            }

            // "<"和">"會讓標籤誤認因此要替換掉
            ivrAdapter = new IVRAdapter(getAtmTxData(), getFeptxn(), rtnData.get().replace("<", "&lt;").replace(">", "&gt;"));
            ivrAdapter.setMessageToIVR(ivrAdapter.prepareIVRRequest());
            rtn = ivrAdapter.sendReceive();

            if (rtn == FEPReturnCode.Normal) {
                String ivrTota = ivrAdapter.getMessageFromIVR();
                Element root = XmlUtil.load(ivrTota);
                String ivrRSC = XmlUtil.getChildElementValue(root, "RsStatCode", "");
                if (ivrRSC.equals(NormalRC.External_OK)) {
                    /// * 驗密成功 */
                    getFeptxn().setFeptxnCbsRc(NormalRC.Unisys_OK);
                    // 2016/06/21 Modify by Ruling for COMBO開卡作業優化:只限定G51電文送IVR成功回G051，G50仍回4個空白
                    if (ATMTXCD.G51.name().equals(getFeptxn().getFeptxnTxCode())) {
                        getFeptxn().setFeptxnReplyCode(NormalRC.ATM_G51_OK);
                    }
                    rtn = CommonReturnCode.Normal;
                } else {
                    /// * 驗密失敗 */
                    /// * 將 IVR 回應的錯誤代碼, 轉換為回給 ATM 的錯誤代碼 */
                    getFeptxn().setFeptxnCbsRc(ivrRSC);
                    getFeptxn().setFeptxnReplyCode(TxHelper.getRCFromErrorCode(ivrRSC, FEPChannel.IVR,
                            getAtmTxData().getTxChannel(), getAtmTxData().getLogContext()));
                    rtn = CommonReturnCode.CBSResponseError;
                }
            }

        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }
        return rtn;
    }

    private String makeMessageSMS(String action) {
        HashMap<String, String> smsSD = new HashMap<String, String>();
        StringBuilder smsSB = new StringBuilder("");
        try {
            switch (action) {
                case "1":
                case "3":
                    smsSD.put("CHLAP", "FEP");
                    smsSD.put("CUSTIP", "");
                    smsSD.put("FNTTRANSEQ", StringUtils.join(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                            StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 12, '0')));
                    // 2011/08/19 modify by Ruling for SMS 電文 CARDNO 欄位
                    smsSD.put("CARDNO", StringUtils.join(getFeptxn().getFeptxnMajorActno().substring(2, 16), StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, 'O')));
                    smsSD.put("MGRID", "");
                    smsSD.put("CSRID", "");
                    smsSD.put("CUSTID", getFeptxn().getFeptxnIdno());
                    if ("3".equals(action)) {
                        smsSD.put("SMSTYPE", "01"); /// * 啟用 */
                    } else {
                        smsSD.put("SMSTYPE", "00"); /// * 申請+啟用 */
                    }
                  //--ben-20220922-//smsSD.put("SMSMOBILE", atmReq.getMobileN());
                    break;
                case "2":
                    smsSD.put("CHLAP", "FEP");
                    smsSD.put("CUSTIP", "");
                    smsSD.put("FNTTRANSEQ", StringUtils.join(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                            StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 12, '0')));
                    // 2011/08/19 modify by Ruling for SMS 電文 CARDNO 欄位
                    smsSD.put("CARDNO", StringUtils.join(getFeptxn().getFeptxnMajorActno().substring(2, 16), StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, 'O')));
                    smsSD.put("MGRID", "");
                    smsSD.put("CSRID", "");
                    smsSD.put("CUSTID", getFeptxn().getFeptxnIdno()); /// *身份證字號/統編(含檢查碼) */
                    smsSD.put("SMSTYPE", "01"); /// * 啟用 */
                  //--ben-20220922-//smsSD.put("SMSTIME", atmReq.getMEMO()); /// * NB 交易序號 */
                  //--ben-20220922-//smsSD.put("SMSOTP", atmReq.getOTP()); /// * OTP */
                    break;
                case "4":
                    smsSD.put("CHLAP", "FEP");
                    smsSD.put("CUSTIP", "");
                    smsSD.put("FNTTRANSEQ", StringUtils.join(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                            StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 12, '0')));
                    // 2011/08/19 modify by Ruling for SMS 電文 CARDNO 欄位
                    smsSD.put("CARDNO", StringUtils.join(getFeptxn().getFeptxnMajorActno().substring(2, 16), StringUtils.leftPad(getFeptxn().getFeptxnCardSeq().toString(), 2, 'O')));

                    smsSD.put("MGRID", "");
                    smsSD.put("CSRID", "");
                    smsSD.put("CUSTID", getFeptxn().getFeptxnIdno()); /// *身份證字號/統編(含檢查碼) */
                    break;
                default:
                    return "";
            }

            for (Map.Entry<String, String> smsDE : smsSD.entrySet()) {
                smsSB.append(StringUtils.join(smsDE.getKey().toString(), ":", smsDE.getValue().toString(), "|"));
            }
            smsSB.deleteCharAt(smsSB.length() - 1);

        } catch (Exception ex) {
            throw ex;
        }

        return smsSB.toString();
    }

    // 組送NB主機 WEB SERVICE
    public final FEPReturnCode sendToNB(String action) {
        FEPReturnCode rtn = FEPReturnCode.Abnormal;
        NBAdapter nbAdapter = new NBAdapter(getAtmTxData());
        NBGeneral nbGeneralForReq = new NBGeneral();
        NBGeneral nbGeneralForRes = new NBGeneral();
        SpCaller spCaller = SpringBeanFactoryUtil.getBean(SpCaller.class);
        String txtno = ""; // 交易傳輸編號

        try {
            Long atmstatTxSeq = spCaller.getAtmTxSeq(getFeptxn().getFeptxnAtmno());
            if (atmstatTxSeq == null || atmstatTxSeq <= 0) {
                return CommonReturnCode.GetATMSeqNoError;
            }
            txtno = StringUtils.leftPad(atmstatTxSeq.toString(), 7, '0');
            getFeptxn().setFeptxnTxseq(txtno);

            nbAdapter.setAction(action);
            switch (action) {
                case "1":
                case "2":
                case "3":
                case "4":
                    nbAdapter.setMessageToNB(makeMessageSMS(action).toUpperCase());
                    break;
                case "A": /// * A: 申請網銀密碼 */
                    nbGeneralForReq.getRequest().setMsgID("NB_A_APPLYMEMBER_ICCARD");
                    nbGeneralForReq.getRequest().setMsgType("A"); /// * 新申請 */
                    nbGeneralForReq.getRequest().setChlName("FEP");
                    nbGeneralForReq.getRequest()
                            .setChlEJNo(StringUtils.join(
                                    FormatUtil.dateTimeFormat(Calendar.getInstance(),
                                            FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN),
                                    StringUtils.leftPad(getFeptxn().getFeptxnEjfno().toString(), 16, '0')));
                    nbGeneralForReq.getRequest().setChlSendTime(
                            FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_T_HHMMSSSSS));
                    nbGeneralForReq.getRequest().setTxnID("V2102");
                    nbGeneralForReq.getRequest().setBranchID(getFeptxn().getFeptxnAtmno().substring(0, 3));
                    nbGeneralForReq.getRequest().setTermID(getFeptxn().getFeptxnAtmno().substring(3, 5));

                    /// * NB_TITA.DETAIL */
                    nbGeneralForReq.getRequest().setCUSTID(getFeptxn().getFeptxnIdno()); /// * 身份證號/統編含檢查碼 */
                    nbGeneralForReq.getRequest().setCARDNO(getFeptxn().getFeptxnTroutActno());

                    ANBRequest reqClass = new ANBRequest();

                    reqClass.setRqHeader(new FEPRqHeader());
                    reqClass.setSvcRq(new ANBSvcRq());
                    reqClass.getSvcRq().setRq(new ANBRq());
                    nbAdapter.setMessageToNB(reqClass.makeMessageFromGeneral(nbGeneralForReq));
                    break;
                default:
                    logContext.setRemark("傳入的ACTION" + action + "不存在");
                    logMessage(Level.DEBUG, logContext);
                    return FEPReturnCode.Abnormal;
            }

            getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(true)); /// * CBS 逾時 FLAG */
            if (feptxnDao.updateByPrimaryKey(getFeptxn()) <= 0) // RC = L013
            {
                return IOReturnCode.FEPTXNUpdateError;
            }

            // nbAdapter.MessageToNB = reqClass.MakeMessageFromGeneral(nbGeneralForReq)
            rtn = nbAdapter.sendReceive();

            if (rtn == FEPReturnCode.Normal) {
                // 2011-06-28 by kyo for 若有收到回應更新CBS_TIMEOUT為False
                getFeptxn().setFeptxnCbsTimeout(DbHelper.toShort(false));/// * CBS 逾時 FLAG */

                switch (action) {
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                        HashMap<String, String> smsSD = parseSMS(nbAdapter.getMessageFromNB());
                        if (smsSD.get("RSSTAT").toString().equals("0000")) {
                            getFeptxn().setFeptxnCbsRc("0000");
                            getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);

                            /// * OTP ATM申請 */
                            if (action.equals("1") || action.equals("3")) {
                                getFeptxn().setFeptxnRemark(smsSD.get("SMSTIME").toString());
                            }
                        } else {
                            /// * 將 NB 回應的錯誤代碼, 直接回給 ATM, 不需轉換 */
                            getFeptxn().setFeptxnCbsRc(smsSD.get("RSSTAT").toString()); /// * NB回應代號 */
                            getFeptxn().setFeptxnReplyCode(smsSD.get("RSSTAT").toString());
                            rtn = CommonReturnCode.CBSResponseError;
                        }
                        break;
                    case "A": /// * A: 申請網銀密碼 */
                        ANBResponse resClass = new ANBResponse();
                        resClass = (ANBResponse) deserializeFromXml(nbAdapter.getMessageFromNB(), ANBResponse.class);

                        if (resClass.getRsHeader() == null) {
                            resClass.setRsHeader(new FEPRsHeader());
                        }
                        if (resClass.getSvcRs() == null) {
                            resClass.setSvcRs(new ANBSvcRs());
                        }
                        if (resClass.getSvcRs().getRs() == null) {
                            resClass.getSvcRs().setRs(new ANBRs());
                        }

                        resClass.toGeneral(nbGeneralForRes);
                        if (nbGeneralForRes.getResponse().getRsStatRsStateCode().equals("SUCCESS")) {
                            getFeptxn().setFeptxnCbsRc("0000");
                            getFeptxn().setFeptxnReplyCode(NormalRC.ATM_OK);

                            /// * ATM 申請網銀密碼 */
                            if (action.equals("A")) {
                                if (!StringUtils.isNumeric(nbGeneralForRes.getResponse().getRETRY())
                                        || !StringUtils.isNumeric(nbGeneralForRes.getResponse().getAPPLYLIMITDATE())) {
                                    rtn = CommonReturnCode.HostResponseTimeout;
                                } else {
                                    /// * 將 NB 電文傳回值, 寫入 FEPTXN_REMARK */
                                    getFeptxn().setFeptxnRemark(StringUtils.join(
                                            StringUtils.rightPad(nbGeneralForRes.getResponse().getUSERCODE(), 6, ' ')
                                                    .substring(0, 6),
                                            StringUtils.rightPad(nbGeneralForRes.getResponse().getPASSWORD(), 6, ' ')
                                                    .substring(0, 6),
                                            StringUtils.rightPad(nbGeneralForRes.getResponse().getRETRY(), 1, ' ')
                                                    .substring(0, 1),
                                            StringUtils.rightPad(nbGeneralForRes.getResponse().getAPPLYLIMITDATE(), 14, ' ')
                                                    .substring(0, 14)));
                                }
                            }
                        } else {
                            getFeptxn().setFeptxnCbsRc(nbGeneralForRes.getResponse().getRsStatRsStateCode());
                            getFeptxn().setFeptxnReplyCode(nbGeneralForRes.getResponse().getRsStatRsStateCode());
                            rtn = CommonReturnCode.CBSResponseError;
                        }
                        break;
                    default:
                        logContext.setRemark("傳入的ACTION" + action + "不存在");
                        logMessage(Level.DEBUG, logContext);
                        return FEPReturnCode.Abnormal;
                }
            }

        } catch (Exception ex) {
            logContext.setProgramException(ex);
            sendEMS(this.logContext);
            return CommonReturnCode.ProgramException;
        }

        return rtn;

    }

    private HashMap<String, String> parseSMS(String smstota) {
        HashMap<String, String> smsSD = new HashMap<String, String>();
        String[] strArray = null;
        try {
            strArray = smstota.split("[|]", -1);
            for (int i = 0; i < strArray.length; i++) {
                smsSD.put(strArray[i].split("[:]", -1)[0], strArray[i].split("[:]", -1)[1]);
            }
        } catch (Exception ex) {
            throw ex;
        }
        return smsSD;
    }

    // Han add 2022/05/17
    public FEPReturnCode sendToCreditWS(String string, Map<String, String> creditWSRsp) {
        FEPReturnCode rtn = CommonReturnCode.Normal;
        return rtn;
    }
}
