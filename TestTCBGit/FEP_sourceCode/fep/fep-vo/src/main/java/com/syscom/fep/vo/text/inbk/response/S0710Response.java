package com.syscom.fep.vo.text.inbk.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.inbk.INBKGeneral;
import com.syscom.fep.vo.text.inbk.INBKGeneralResponse;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class S0710Response extends TextBase<INBKGeneral> {

    @XStreamAlias("RsHeader")
    private FEPRsHeader rsHeader;
    @XStreamAlias("SvcRs")
    private S0710SvcRs svcRs;

    public FEPRsHeader getRsHeader() {
        return rsHeader;
    }

    public void setRsHeader(FEPRsHeader rsHeader) {
        this.rsHeader = rsHeader;
    }

    public S0710SvcRs getSvcRs() {
        return svcRs;
    }

    public void setSvcRs(S0710SvcRs svcRs) {
        this.svcRs = svcRs;
    }

    @Override
    public int getTotalLength() {
        return 0;
    }

    /**
     對應到General

     <remark></remark>
     */
    public void toGeneral(INBKGeneral general) {
        general.getResponse().setChlEJNo(getRsHeader().getChlEJNo());
        general.getResponse().setEJNo(getRsHeader().getEJNo());
        general.getResponse().setRqTime(getRsHeader().getRqTime());
        general.getResponse().setRsStatRsStateCode(getRsHeader().getRsStat().getRsStatCode().getValue());
        general.getResponse().setRsStatRsStateCodeType(getRsHeader().getRsStat().getRsStatCode().getType());
        general.getResponse().setRsStatDesc(getRsHeader().getRsStat().getDesc());
        general.getResponse().setRsTime(getRsHeader().getRsTime());
        general.getResponse().setOverrides(getRsHeader().getOverrides());
        general.getResponse().setUNITNO(getSvcRs().getRs().getUNITNO()); //委託單位代號

    }

    /**
     從 General 對應回來

     <remark></remark>
     */
    public String makeMessageFromGeneral(INBKGeneral general) {
        if (getRsHeader() == null) {
            setRsHeader(new FEPRsHeader());
        }
        if (getSvcRs() == null) {
            setSvcRs(new S0710SvcRs());
            getSvcRs().setRs(new S0710Rs());
            if (general.getS0710SvcRs() != null) {
                getSvcRs().setRs(general.getS0710SvcRs().getRs());
            }
        } else {
            setSvcRs(general.getS0710SvcRs());
        }
        INBKGeneralResponse tempVar = general.getResponse();
        getRsHeader().setChlEJNo(tempVar.getChlEJNo());
        getRsHeader().setEJNo(tempVar.getEJNo());
        getRsHeader().setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
        getRsHeader().getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
        getRsHeader().getRsStat().getRsStatCode().setType(tempVar.getRsStatRsStateCodeType());
        getRsHeader().getRsStat().getRsStatCode().setValue(tempVar.getRsStatRsStateCode());
        getRsHeader().getRsStat().setDesc(tempVar.getRsStatDesc());
        getRsHeader().setRqTime(tempVar.getRqTime());
        getRsHeader().setRsTime(tempVar.getRsTime());
        if (tempVar.getOverrides() == null) {
            FEPRsHeader.FEPRsHeaderOverride[] ovr = new FEPRsHeader.FEPRsHeaderOverride[1];
            getRsHeader().setOverrides(ovr);
        } else {
            getRsHeader().setOverrides(tempVar.getOverrides());
        }
        getSvcRs().getRs().setUNITNO(tempVar.getUNITNO()); //委託單位代號
        getSvcRs().getRs().setALIASNAME(tempVar.getALIASNAME()); //委託單位簡稱
        getSvcRs().getRs().setRECCOUNT(tempVar.getRECCOUNT()); //明細筆數

        return serializeToXml(this);
    }

    @XStreamAlias("SvcRs")
    public static class S0710SvcRs{
        @XStreamAlias("Rs")
        private S0710Rs rs;

        public S0710Rs getRs() {
            return this.rs;
        }

        public void setRs(S0710Rs rs) {
            this.rs = rs;
        }
    }

    @XStreamAlias("Rs")
    public static class S0710Rs{
        // 明細筆數
        private String RECCOUNT = "";
        // 委託單位代號
        private String UNITNO = "";
        // 委託單位簡稱
        private String ALIASNAME = "";
        @XStreamAlias("RcdsField")
        private S0710RsRcd[] rcdsField;

        /**
         * 委託單位代號
         *
         * <remark></remark>
         */
        public String getUNITNO() {
            return UNITNO;
        }

        public void setUNITNO(String value) {
            UNITNO = value;
        }

        /**
         * 委託單位簡稱
         *
         * <remark></remark>
         */
        public String getALIASNAME() {
            return ALIASNAME;
        }

        public void setALIASNAME(String value) {
            ALIASNAME = value;
        }

        /**
         * 明細筆數
         *
         * <remark></remark>
         */
        public String getRECCOUNT() {
            return RECCOUNT;
        }

        public final void setRECCOUNT(String value) {
            RECCOUNT = value;
        }

        public S0710RsRcd[] getRcds() {
            return this.rcdsField;
        }

        public void setRcds(S0710RsRcd[] value) {
            this.rcdsField = value;
        }
    }

    @XStreamAlias("RcdsField")
    public static class S0710RsRcd{
        // 費用類別代號
        private String PAYTYPE = "";
        // 費用代號
        private String FEENO = "";
        // 費用名稱
        private String PAYNAME = "";

        /**
         * 費用類別代號
         *
         * <remark></remark>
         */
        public String getPAYTYPE() {
            return PAYTYPE;
        }

        public void setPAYTYPE(String value) {
            PAYTYPE = value;
        }

        /**
         * 費用代號
         *
         * <remark></remark>
         */
        public String getFEENO() {
            return FEENO;
        }

        public void setFEENO(String value) {
            FEENO = value;
        }

        /**
         * 費用名稱
         *
         * <remark></remark>
         */
        public String getPAYNAME() {
            return PAYNAME;
        }

        public void setPAYNAME(String value) {
            PAYNAME = value;
        }
    }
}
