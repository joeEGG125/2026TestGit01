package com.syscom.fep.vo.text.rm.request;

import com.syscom.fep.vo.text.FEPRqHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.math.BigDecimal;

@XStreamAlias("FEP")
public class C1700_Request extends TextBase<RMGeneral> {

    @XStreamAlias("RsHeader")
    private FEPRqHeader rqHeader;
    @XStreamAlias("C1700SvcRq")
    private C1700SvcRq c1700SvcRq;

    public FEPRqHeader getRqHeader() { return rqHeader; }

    public void setRqHeader(FEPRqHeader rqHeader) { this.rqHeader = rqHeader; }

    public C1700SvcRq getC1700SvcRq() { return c1700SvcRq; }

    public void setC1700SvcRq(C1700SvcRq c1700SvcRq) { this.c1700SvcRq = c1700SvcRq; }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
    public String makeMessageFromGeneral(RMGeneral general) throws Exception  {
        rqHeader.setChlName(general.getRequest().getChlName());
        rqHeader.setChlEJNo(general.getRequest().getChlEJNo());
        rqHeader.setBranchID(general.getRequest().getBranchID());
        rqHeader.setChlSendTime(general.getRequest().getChlSendTime());
        rqHeader.setMsgID(general.getRequest().getMsgID());
        rqHeader.setMsgType(general.getRequest().getMsgType());
        rqHeader.setTermID(general.getRequest().getTermID());
        rqHeader.setTxnID(general.getRequest().getTxnID());
        rqHeader.setUserID(general.getRequest().getUserID());
        rqHeader.setSignID(general.getRequest().getSignID());
        // 輸入行
        c1700SvcRq.getRq().setKINBR(general.getRequest().getKINBR());
        // 櫃台機號
        c1700SvcRq.getRq().setTRMSEQ(general.getRequest().getTRMSEQ());
        // 分行登錄序號
        c1700SvcRq.getRq().setBRSNO(general.getRequest().getBRSNO());
        // 登錄櫃員
        c1700SvcRq.getRq().setENTTLRNO(general.getRequest().getENTTLRNO());
        // 主管代號1
        c1700SvcRq.getRq().setSUPNO1(general.getRequest().getSUPNO1());
        // 主管代號2
        c1700SvcRq.getRq().setSUPNO2(general.getRequest().getSUPNO2());
        // 登錄日期
        c1700SvcRq.getRq().setTBSDY(general.getRequest().getTBSDY());
        // 登錄時間
        c1700SvcRq.getRq().setTIME(general.getRequest().getTIME());
        //登錄分行
        c1700SvcRq.getRq().setORGBRNO(general.getRequest().getORGBRNO());
        // FEP登錄序號
        c1700SvcRq.getRq().setFEPNO(general.getRequest().getFEPNO());
        //匯款行
        c1700SvcRq.getRq().setREMAMT(general.getRequest().getREMBANK());
        //摘要
        c1700SvcRq.getRq().setREMTYPE(general.getRequest().getREMTYPE());
        //匯款金額9(11)V99
        c1700SvcRq.getRq().setREMAMT(general.getRequest().getREMAMT().toString());
        // 應收手續費
        c1700SvcRq.getRq().setRECFEE(general.getRequest().getRECFEE().toString());
        //實收手續費
        c1700SvcRq.getRq().setACTFEE(general.getRequest().getACTFEE().toString());
        // 匯款日期
        c1700SvcRq.getRq().setREMDATE(general.getRequest().getREMDATE());
        // 解款行
        c1700SvcRq.getRq().setRECBANK(general.getRequest().getRECBANK());
        //  收款人帳號
        c1700SvcRq.getRq().setRECCIF(general.getRequest().getRECCIF());
        //  收款人姓名
        c1700SvcRq.getRq().setRECNM(general.getRequest().getRECNM());
        // 匯款人帳號
        c1700SvcRq.getRq().setREMCIF(general.getRequest().getREMCIF());
        // 匯款人姓名
        c1700SvcRq.getRq().setREMNM(general.getRequest().getREMNM());
        //匯款人電話
        c1700SvcRq.getRq().setREMTEL(general.getRequest().getREMTEL());
        //附言
        c1700SvcRq.getRq().setREMARKFISC(general.getRequest().getREMARKFISC());
        //稅單號碼
        c1700SvcRq.getRq().setTAXNO(general.getRequest().getTAXNO());
        return serializeToXml(this);
    }


    @Override
    public void toGeneral(RMGeneral general) throws Exception {
        general.getRequest().setChlName(rqHeader.getChlName());
        general.getRequest().setChlEJNo(rqHeader.getChlEJNo());
        general.getRequest().setBranchID(rqHeader.getBranchID());
        general.getRequest().setChlSendTime(rqHeader.getChlSendTime());
        general.getRequest().setMsgID(rqHeader.getMsgID());
        general.getRequest().setMsgType(rqHeader.getMsgType());
        general.getRequest().setTermID(rqHeader.getTermID());
        general.getRequest().setTxnID(rqHeader.getTxnID());
        general.getRequest().setUserID(rqHeader.getUserID());
        general.getRequest().setSignID(rqHeader.getSignID());
        general.getRequest().setKINBR(getC1700SvcRq().getRq().getKINBR());
        general.getRequest().setTRMSEQ(getC1700SvcRq().getRq().getTRMSEQ());
        general.getRequest().setBRSNO(getC1700SvcRq().getRq().getBRSNO());
        general.getRequest().setTLRNO(getC1700SvcRq().getRq().getENTTLRNO());
        general.getRequest().setSUPNO1(getC1700SvcRq().getRq().getSUPNO1());
        general.getRequest().setSUPNO2(getC1700SvcRq().getRq().getSUPNO2());
        general.getRequest().setTBSDY(getC1700SvcRq().getRq().getTBSDY());
        general.getRequest().setTIME(getC1700SvcRq().getRq().getTIME());
        general.getRequest().setORGBRNO(getC1700SvcRq().getRq().getORGBRNO());
        general.getRequest().setFEPNO(getC1700SvcRq().getRq().getFEPNO());
        general.getRequest().setREMDATE(getC1700SvcRq().getRq().getREMDATE());
        general.getRequest().setREMAMT(new BigDecimal(getC1700SvcRq().getRq().getREMAMT()));
        general.getRequest().setRECBANK(getC1700SvcRq().getRq().getRECBANK());
        general.getRequest().setREMBANK(getC1700SvcRq().getRq().getREMBANK());
        general.getRequest().setREMTYPE(getC1700SvcRq().getRq().getREMTYPE());
        general.getRequest().setRECFEE(new BigDecimal(getC1700SvcRq().getRq().getRECFEE()));
        general.getRequest().setACTFEE(new BigDecimal(getC1700SvcRq().getRq().getACTFEE()));
        general.getRequest().setRECCIF(getC1700SvcRq().getRq().getRECCIF());
        general.getRequest().setRECNM(getC1700SvcRq().getRq().getRECNM());
        general.getRequest().setREMCIF(getC1700SvcRq().getRq().getREMCIF());
        general.getRequest().setREMNM(getC1700SvcRq().getRq().getREMNM());
        general.getRequest().setREMTEL(getC1700SvcRq().getRq().getREMTEL());
        general.getRequest().setTAXNO(getC1700SvcRq().getRq().getTAXNO());
        general.getRequest().setREMARKFISC(getC1700SvcRq().getRq().getREMARKFISC());
        general.getRequest().setMAC(getC1700SvcRq().getRq().getMAC());
    }
    @XStreamAlias("C1700SvcRq")
    public static class C1700SvcRq {
        @XStreamAlias("Rq")
        private C1700Rq rq;

        public C1700Rq getRq() {
            return rq;
        }

        public void setRq(C1700Rq rq) {
            this.rq = rq;
        }
    }

    @XStreamAlias("C1700Rq")
    public static class C1700Rq {
        //輸入行
        private String KINBR = "";

        //櫃台機號
        private String TRMSEQ = "";

        //分行登錄序號
        private String BRSNO = "";

        //登錄櫃員
        private String ENTTLRNO = "";

        //主管代號1
        private String SUPNO1 = "";

        //主管代號2
        private String SUPNO2 = "";

        //登錄日期
        private String TBSDY = "";

        //登錄時間
        private String TIME = "";

        //登錄分行
        private String ORGBRNO = "";

        //FEP登錄序號
        private String FEPNO = "";

        //匯款行
        private String REMBANK = "";

        //摘要
        private String REMTYPE = "";

        //匯款金額9(11)V99
        private String REMAMT = "";

        //應收手續費
        private String RECFEE = "";

        //實收手續費
        private String ACTFEE = "";

        //匯款日期
        private String REMDATE = "";

        //解款行
        private String RECBANK = "";

        //收款人帳號
        private String RECCIF = "";

        //收款人姓名
        private String RECNM = "";

        //匯款人帳號
        private String REMCIF = "";

        //匯款人姓名
        private String REMNM = "";

        //匯款人電話
        private String REMTEL = "";

        //附言
        private String REMARKFISC = "";

        //稅單號碼
        private String TAXNO = "";


        public String getKINBR() {
            return KINBR;
        }

        public void setKINBR(String KINBR) {
            this.KINBR = KINBR;
        }

        public String getTRMSEQ() {
            return TRMSEQ;
        }

        public void setTRMSEQ(String TRMSEQ) {
            this.TRMSEQ = TRMSEQ;
        }

        public String getBRSNO() {
            return BRSNO;
        }

        public void setBRSNO(String BRSNO) {
            this.BRSNO = BRSNO;
        }

        public String getENTTLRNO() {
            return ENTTLRNO;
        }

        public void setENTTLRNO(String ENTTLRNO) {
            this.ENTTLRNO = ENTTLRNO;
        }

        public String getSUPNO1() {
            return SUPNO1;
        }

        public void setSUPNO1(String SUPNO1) {
            this.SUPNO1 = SUPNO1;
        }

        public String getSUPNO2() {
            return SUPNO2;
        }

        public void setSUPNO2(String SUPNO2) {
            this.SUPNO2 = SUPNO2;
        }

        public String getTBSDY() {
            return TBSDY;
        }

        public void setTBSDY(String TBSDY) {
            this.TBSDY = TBSDY;
        }

        public String getTIME() {
            return TIME;
        }

        public void setTIME(String TIME) {
            this.TIME = TIME;
        }

        public String getORGBRNO() {
            return ORGBRNO;
        }

        public void setORGBRNO(String ORGBRNO) {
            this.ORGBRNO = ORGBRNO;
        }

        public String getFEPNO() {
            return FEPNO;
        }

        public void setFEPNO(String FEPNO) {
            this.FEPNO = FEPNO;
        }

        public String getREMDATE() {
            return REMDATE;
        }

        public void setREMDATE(String REMDATE) {
            this.REMDATE = REMDATE;
        }

        public String getREMAMT() {
            return REMAMT;
        }

        public void setREMAMT(String REMAMT) {
            this.REMAMT = REMAMT;
        }

        public String getRECBANK() {
            return RECBANK;
        }

        public void setRECBANK(String RECBANK) {
            this.RECBANK = RECBANK;
        }

        public String getREMBANK() {
            return REMBANK;
        }

        public void setREMBANK(String REMBANK) {
            this.REMBANK = REMBANK;
        }

        public String getREMTYPE() {
            return REMTYPE;
        }

        public void setREMTYPE(String REMTYPE) {
            this.REMTYPE = REMTYPE;
        }


        public String getRECFEE() {
            return RECFEE;
        }

        public void setRECFEE(String RECFEE) {
            this.RECFEE = RECFEE;
        }

        public String getACTFEE() {
            return ACTFEE;
        }

        public void setACTFEE(String ACTFEE) {
            this.ACTFEE = ACTFEE;
        }

        public String getRECCIF() {
            return RECCIF;
        }

        public void setRECCIF(String RECCIF) {
            this.RECCIF = RECCIF;
        }

        public String getRECNM() {
            return RECNM;
        }

        public void setRECNM(String RECNM) {
            this.RECNM = RECNM;
        }

        public String getREMCIF() {
            return REMCIF;
        }

        public void setREMCIF(String REMCIF) {
            this.REMCIF = REMCIF;
        }

        public String getREMNM() {
            return REMNM;
        }

        public void setREMNM(String REMNM) {
            this.REMNM = REMNM;
        }

        public String getREMTEL() {
            return REMTEL;
        }

        public String getREMARKFISC() {
            return REMARKFISC;
        }

        public void setREMARKFISC(String REMARKFISC) {
            this.REMARKFISC = REMARKFISC;
        }

        public void setREMTEL(String REMTEL) {
            this.REMTEL = REMTEL;
        }

        public String getTAXNO() {
            return TAXNO;
        }

        public void setTAXNO(String TAXNO) {
            this.TAXNO = TAXNO;
        }

        public String getMAC() {
            return MAC;
        }

        public void setMAC(String MAC) {
            this.MAC = MAC;
        }

        //匯出押碼
        private String MAC = "";

    }
}
