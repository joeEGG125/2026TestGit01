package com.syscom.fep.server.common.cbsprocess;

import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.frmcommon.util.CalendarUtil;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.business.cbsbusiness.ACBSAction;
import com.syscom.fep.vo.CodeGenUtil;
import com.syscom.fep.vo.text.ims.CB_IQTX_I001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O001;
import com.syscom.fep.vo.text.ims.CB_IQTX_O002;
import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Objects;

public class EAIQTXI01 extends ACBSAction {

    public EAIQTXI01(MessageBase txType) {
        super(txType, new CB_IQTX_O001());
    }


    /**
     * 組CBS TITA電文
     *
     * @param txType
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode getCbsTita(String txType) throws Exception {
        /* TITA 請參考合庫主機電文規格(CB_IQTX_I01) */
        // Header
        CB_IQTX_I001 cbsTita = new CB_IQTX_I001();
        String aa = cbsTita.makeMessage(cbsTita, "0");
        cbsTita.setINTRAN("EAII    ");
        cbsTita.setINMSGID(feptxn.getFeptxnTxDate() + feptxn.getFeptxnTxTime() + StringUtils.leftPad(String.valueOf(feptxn.getFeptxnEjfno()), 8, "0"));
        cbsTita.setINSTAN(feptxn.getFeptxnStan());
//        cbsTita.setINDATE(feptxn.getFeptxnTxDate());
//        cbsTita.setINTIME(feptxn.getFeptxnTxTime());
        //外圍專用 設置系統時間
        cbsTita.setINDATE(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN));
        cbsTita.setINTIME(FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSS_PLAIN));
        cbsTita.setINSERV("FEP1");
        cbsTita.setMQIDCNV("N");
        cbsTita.setINTD("910A0037");
        cbsTita.setINAP("ATFEP");
        if(feptxn.getFeptxnIdno()!=null){
            cbsTita.setINID(feptxn.getFeptxnIdno().trim().length() == 11 ? TxHelper.parseCIDtoChar(feptxn.getFeptxnIdno().trim()) : feptxn.getFeptxnIdno().trim());
        }
        cbsTita.setINFF("F");
        cbsTita.setINPGNO("001");
        cbsTita.setINV1CT("0000");
        cbsTita.setINQ_ACQ_BID(feptxn.getFeptxnBkno());
        cbsTita.setINQ_TX_STAN(feptxn.getFeptxnStan());
        //財金營業日(西元年須轉民國年)
        String feptxnTbsdyFisc = feptxn.getFeptxnTbsdyFisc();
        if ("00000000".equals(feptxnTbsdyFisc) || feptxnTbsdyFisc.length() != 8) { // "00000000"不是日期格式，西元轉民國會轉成""，因此特殊處理
            feptxnTbsdyFisc = "000000";
        } else {
            feptxnTbsdyFisc = CalendarUtil.adStringToROCString(feptxnTbsdyFisc);
        }
        cbsTita.setINQ_FG_TXDATE(feptxnTbsdyFisc);

        this.setoTita(cbsTita);
        this.setTitaToString(cbsTita.makeMessage());
        this.setASCIItitaToString(cbsTita.makeMessageAscii());
        return FEPReturnCode.Normal;
    }

    /**
     * 拆解CBS回應電文
     *
     * @param cbsTota
     * @param type
     * @return
     * @throws Exception
     */
    @Override
    public FEPReturnCode processCbsTota(String cbsTota, String type) throws Exception {
        /* 電文內容格式請參照TOTA電文格式(CB_TXIQ_O01) */
        /* 拆解主機回應電文 */
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (CodeGenUtil.ebcdicToAsciiDefaultEmpty(cbsTota).substring(88,92).equals("4001")) {
            CB_IQTX_O001 tota = new CB_IQTX_O001();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            rtnCode = this.updateFEPTxn(tota);
        } else {
            CB_IQTX_O002 tota = new CB_IQTX_O002();
            tota.parseCbsTele(cbsTota);
            this.setTota(tota);
            rtnCode = this.updateFEPTxn(tota);
//            rtnCode = FEPReturnCode.CBSCheckError;
        }

        /* 回覆FEP */
        // 處理 CBS 回應
        return rtnCode;
    }

    /**
     * 更新交易
     *
     * @param cbsTota
     * @return
     * @throws Exception
     */
    public FEPReturnCode updateFEPTxn(CB_IQTX_O001 cbsTota) throws Exception {
        FEPReturnCode rtnCode;
        if (cbsTota.getOUTRTC().equals("4001")) { /*電文CB_IQTX_O001*/
            if (!cbsTota.getTXSTAN().equals(feptxn.getFeptxnStan()) ||
                    !cbsTota.getOUTSERV().equals("FEP1") ||
                    !cbsTota.getOUTTD().equals("910A0037") ||
                    !cbsTota.getOUTAP().equals("ATFEP") ||
                    !cbsTota.getPCODE().equals(feptxn.getFeptxnPcode()) ||
                    !cbsTota.getFSCODE().equals(feptxn.getFeptxnTxCode())) {
                rtnCode = FEPReturnCode.CBSCheckError;
            } else {
                if (cbsTota.getTX_ACCT_FLAG().equals("Y")) {
                    feptxn.setFeptxnAccType((short) 1); //以記帳
                } else {
                    feptxn.setFeptxnAccType((short) 0); //未記帳
                }
                String IMSRC = StringUtils.isNotBlank(cbsTota.getIMS_RC4_FISC()) ? cbsTota.getIMS_RC4_FISC().trim() : cbsTota.getIMS_RC3_TCB();
                feptxn.setFeptxnReplyCode(TxHelper.getRCFromErrorCode(IMSRC, FEPChannel.FEP, FEPChannel.FISC, getNBData().getLogContext()));
            }
        }
        rtnCode = FEPReturnCode.Normal; /* 收到主機回應, 改成 Normal */
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        return rtnCode;
    }

    public FEPReturnCode updateFEPTxn(CB_IQTX_O002 cbsTota) throws Exception {
        FEPReturnCode rtnCode = FEPReturnCode.Normal;
        if (!cbsTota.getOUTSTAN().equals(feptxn.getFeptxnStan()) ||
                !cbsTota.getOUTSERV().equals("FEP1") ||
                !cbsTota.getOUTTD().equals("910A0037") ||
                !cbsTota.getOUTAP().equals("ATFEP")) {
            rtnCode = FEPReturnCode.CBSCheckError;
        } else {
            feptxn.setFeptxnCbsRc(cbsTota.getOUTRTC());
            feptxn.setFeptxnAccType((short) 0); //未記帳
        }
        this.feptxnDao.updateByPrimaryKeySelective(feptxn); /*檔名SEQ為 FEPTXN_TBSDY_FISC[7:2]*/
        return rtnCode;
    }

    public Object getInstanceObject(String cbsProcessName, MessageBase atmData) {
        Class<?> c = null;
        //java.lang.reflect.Field[] fields = null;
        //String imsPackageName = "";
        //Class<?> imsClassName = null;
        Class<?>[] params = {MessageBase.class};
        Object o = null;
        try {
            //獲得cbsProcessn物件名稱
            c = Class.forName("com.syscom.fep.server.common.cbsprocess." + cbsProcessName);
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
