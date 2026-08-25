package com.syscom.fep.vo.text.atm.request;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.math.BigDecimal;

@XStreamAlias("FEP")
public class EFT226XRequest {
    @XStreamAlias("RqHeader")
    private FEPRqHeader rqHeader;
    @XStreamAlias("SvcRq")
    private EFT226XSvcRq svcRq;

    public FEPRqHeader getRqHeader() {
        return rqHeader;
    }

    public void setRqHeader(FEPRqHeader rqHeader) {
        this.rqHeader = rqHeader;
    }

    public EFT226XSvcRq getSvcRq() {
        return svcRq;
    }

    public void setSvcRq(EFT226XSvcRq svcRq) {
        this.svcRq = svcRq;
    }

    /**
     對應到General

     <remark></remark>
     */
    public void toGeneral(ATMGeneral general) {
    	/*
        general.getRequest().setChlName(this.rqHeader.getChlName());
        general.getRequest().setChlEJNo(this.rqHeader.getChlEJNo());
        general.getRequest().setBranchID(this.rqHeader.getBranchID());
        general.getRequest().setChlSendTime(this.rqHeader.getChlSendTime());
        general.getRequest().setMsgID(this.rqHeader.getMsgID());
        general.getRequest().setMsgType(this.rqHeader.getMsgType());
        general.getRequest().setTermID(this.rqHeader.getTermID());
        general.getRequest().setTxnID(this.rqHeader.getTxnID());
        general.getRequest().setUserID(this.rqHeader.getUserID());
        general.getRequest().setSignID(this.rqHeader.getSignID());
        general.getRequest().setBKNO(this.svcRq.getRq().BKNO); //銀行別
        general.getRequest().setTXACT(this.svcRq.getRq().TXACT); //交易帳號
        general.getRequest().setBknoD(this.svcRq.getRq().BKNO_D); //轉入銀行別
        general.getRequest().setActD(this.svcRq.getRq().ACT_D); //轉入帳號
        try {
            general.getRequest().setTXAMT(new BigDecimal(this.svcRq.getRq().TXAMT));
        }catch (Exception ex){
            general.getRequest().setTXAMT(BigDecimal.valueOf(0));
        }
        general.getRequest().setIDNO(this.svcRq.getRq().IDNO); //身分證字號
        general.getRequest().setPAYCNO(this.svcRq.getRq().PAYCNO); //銷帳編號
        general.getRequest().setVPID(this.svcRq.getRq().VPID); //委託單位代號
        general.getRequest().setCLASS(this.svcRq.getRq().CLASS); //繳款種類
        general.getRequest().setPAYID(this.svcRq.getRq().PAYID); //費用代號
        general.getRequest().setPTYPE(this.svcRq.getRq().PTYPE); //繳費種類
        general.getRequest().setDUEDATE(this.svcRq.getRq().DUEDATE); //繳款到期日
        general.getRequest().setUNIT(this.svcRq.getRq().UNIT); //稽徵機關別
        general.getRequest().setIDENTITY(this.svcRq.getRq().IDENTITY); //識別碼
        general.getRequest().setMENO(this.svcRq.getRq().MENO); //附言欄
        general.getRequest().setMODE(this.svcRq.getRq().MODE); //帳務別
        general.getRequest().setPsbmemoD(this.svcRq.getRq().PSBMEMO_D); //存摺摘要_借方
        general.getRequest().setPsbmemoC(this.svcRq.getRq().PSBMEMO_C); //存摺摘要_貸方
        general.getRequest().setPsbremSD(this.svcRq.getRq().PSBREM_S_D); //存摺備註_借方
        general.getRequest().setPsbremSC(this.svcRq.getRq().PSBREM_S_C); //存摺備註_貸方
        general.getRequest().setPsbremFD(this.svcRq.getRq().PSBREM_F_D); //往來明細_借方
        general.getRequest().setPsbremFC(this.svcRq.getRq().PSBREM_F_C); //往來明細_貸方
        general.getRequest().setIPADDR(this.svcRq.getRq().IPADDR); //使用者登入IP
		*/
    }


    @XStreamAlias("SvcRq")
    public static class EFT226XSvcRq {
        @XStreamAlias("Rq")
        private EFT226XRq rq;

        public EFT226XRq getRq() {
            return rq;
        }

        public void setRq(EFT226XRq rq) {
            this.rq = rq;
        }
    }



    @XStreamAlias("Rq")
    public static class EFT226XRq {
        //銀行別
        private String BKNO = "";

        //交易帳號
        private String TXACT = "";

        //轉入銀行別
        private String BKNO_D = "";

        //轉入帳號
        private String ACT_D = "";

        //交易金額
        private String TXAMT = "";

        //身分證字號
        private String IDNO = "";

        //銷帳編號
        private String PAYCNO = "";

        //委託單位代號
        private String VPID = "";

        //繳款種類
        private String CLASS = "";

        //費用代號
        private String PAYID = "";

        //繳費種類
        private String PTYPE = "";

        //繳款到期日
        private String DUEDATE = "";

        //稽徵機關別
        private String UNIT = "";

        //識別碼
        private String IDENTITY = "";

        //附言欄
        private String MENO = "";

        //帳務別
        private String MODE = "";

        //存摺摘要_借方
        private String PSBMEMO_D = "";

        //存摺摘要_貸方
        private String PSBMEMO_C = "";

        //存摺備註_借方
        private String PSBREM_S_D = "";

        //存摺備註_貸方
        private String PSBREM_S_C = "";

        //往來明細_借方
        private String PSBREM_F_D = "";

        //往來明細_貸方
        private String PSBREM_F_C = "";

        //使用者登入IP
        private String IPADDR = "";

        public String getBKNO() {
            return BKNO;
        }

        public void setBKNO(String BKNO) {
            this.BKNO = BKNO;
        }

        public String getTXACT() {
            return TXACT;
        }

        public void setTXACT(String TXACT) {
            this.TXACT = TXACT;
        }

        public String getBKNOD() {
            return BKNO_D;
        }

        public void setBKNOD(String BKNO_D) {
            this.BKNO_D = BKNO_D;
        }

        public String getACTD() {
            return ACT_D;
        }

        public void setACTD(String ACT_D) {
            this.ACT_D = ACT_D;
        }

        public String getTXAMT() {
            return TXAMT;
        }

        public void setTXAMT(String TXAMT) {
            this.TXAMT = TXAMT;
        }

        public String getIDNO() {
            return IDNO;
        }

        public void setIDNO(String IDNO) {
            this.IDNO = IDNO;
        }

        public String getPAYCNO() {
            return PAYCNO;
        }

        public void setPAYCNO(String PAYCNO) {
            this.PAYCNO = PAYCNO;
        }

        public String getVPID() {
            return VPID;
        }

        public void setVPID(String VPID) {
            this.VPID = VPID;
        }

        public String getCLASS() {
            return CLASS;
        }

        public void setCLASS(String CLASS) {
            this.CLASS = CLASS;
        }

        public String getPAYID() {
            return PAYID;
        }

        public void setPAYID(String PAYID) {
            this.PAYID = PAYID;
        }

        public String getPTYPE() {
            return PTYPE;
        }

        public void setPTYPE(String PTYPE) {
            this.PTYPE = PTYPE;
        }

        public String getDUEDATE() {
            return DUEDATE;
        }

        public void setDUEDATE(String DUEDATE) {
            this.DUEDATE = DUEDATE;
        }

        public String getUNIT() {
            return UNIT;
        }

        public void setUNIT(String UNIT) {
            this.UNIT = UNIT;
        }

        public String getIDENTITY() {
            return IDENTITY;
        }

        public void setIDENTITY(String IDENTITY) {
            this.IDENTITY = IDENTITY;
        }

        public String getMENO() {
            return MENO;
        }

        public void setMENO(String MENO) {
            this.MENO = MENO;
        }

        public String getMODE() {
            return MODE;
        }

        public void setMODE(String MODE) {
            this.MODE = MODE;
        }

        public String getPSBMEMOD() {
            return PSBMEMO_D;
        }

        public void setPSBMEMOD(String PSBMEMO_D) {
            this.PSBMEMO_D = PSBMEMO_D;
        }

        public String getPSBMEMOC() {
            return PSBMEMO_C;
        }

        public void setPSBMEMOC(String PSBMEMO_C) {
            this.PSBMEMO_C = PSBMEMO_C;
        }

        public String getPSBREMSD() {
            return PSBREM_S_D;
        }

        public void setPSBREMSD(String PSBREM_S_D) {
            this.PSBREM_S_D = PSBREM_S_D;
        }

        public String getPSBREMSC() {
            return PSBREM_S_C;
        }

        public void setPSBREMSC(String PSBREM_S_C) {
            this.PSBREM_S_C = PSBREM_S_C;
        }

        public String getPSBREMFD() {
            return PSBREM_F_D;
        }

        public void setPSBREMFD(String PSBREM_F_D) {
            this.PSBREM_F_D = PSBREM_F_D;
        }

        public String getPSBREMFC() {
            return PSBREM_F_C;
        }

        public void setPSBREMFC(String PSBREM_F_C) {
            this.PSBREM_F_C = PSBREM_F_C;
        }

        public String getIPADDR() {
            return IPADDR;
        }

        public void setIPADDR(String IPADDR) {
            this.IPADDR = IPADDR;
        }
    }
}
