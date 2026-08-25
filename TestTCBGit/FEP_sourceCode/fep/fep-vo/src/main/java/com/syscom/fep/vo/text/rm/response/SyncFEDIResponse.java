package com.syscom.fep.vo.text.rm.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class SyncFEDIResponse extends TextBase<RMGeneral> {
    @XStreamAlias("RsHeader")
    private FEPRsHeader rsHeader;
    @XStreamAlias("SvcRs")
    private SyncFEDISvcRs svcRs;

    public FEPRsHeader getRsHeader() {
        return rsHeader;
    }

    public void setRsHeader(FEPRsHeader rsHeader) {
        this.rsHeader = rsHeader;
    }

    public SyncFEDISvcRs getSvcRs() {
        return svcRs;
    }

    public void setSvcRs(SyncFEDISvcRs svcRs) {
        this.svcRs = svcRs;
    }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
    public String makeMessageFromGeneral(RMGeneral general) {
        if (rsHeader == null) {
            rsHeader = new FEPRsHeader();
        }
        if (svcRs == null) {
            svcRs = new SyncFEDISvcRs();
        }
        rsHeader.setChlEJNo(general.getResponse().getChlEJNo());
        rsHeader.setEJNo(general.getResponse().getEJNo());
        rsHeader.setRsStat(new FEPRsHeader.FEPRsHeaderRsStat());
        rsHeader.getRsStat().setRsStatCode(new FEPRsHeader.FEPRsHeaderRsStatRsStatCode());
        rsHeader.getRsStat().getRsStatCode().setType(general.getResponse().getRsStatRsStateCodeType());
        rsHeader.getRsStat().getRsStatCode().setValue(general.getResponse().getRsStatRsStateCode());
        rsHeader.getRsStat().setDesc(general.getResponse().getRsStatDesc());
        rsHeader.setRqTime(general.getResponse().getRqTime());
        rsHeader.setRsTime(general.getResponse().getRsTime());
        if (general.getResponse().getOverrides() == null) {
            FEPRsHeader.FEPRsHeaderOverride ovr[] = new FEPRsHeader.FEPRsHeaderOverride[0];
            rsHeader.setOverrides(ovr);
        } else {
            rsHeader.setOverrides(general.getResponse().getOverrides());
        }
        getSvcRs().getRs().setFepNo(general.getResponse().getFEPNO());
        return serializeToXml(this);
    }

    @Override
    public void toGeneral(RMGeneral general) throws Exception {
        general.getResponse().setChlEJNo(rsHeader.getChlEJNo());
        general.getResponse().setEJNo(rsHeader.getEJNo());
        general.getResponse().setRqTime(rsHeader.getRqTime());
        general.getResponse().setRsStatRsStateCode(rsHeader.getRsStat().getRsStatCode().getValue());
        general.getResponse().setRsStatRsStateCodeType(rsHeader.getRsStat().getRsStatCode().getType());
        general.getResponse().setRsStatDesc(rsHeader.getRsStat().getDesc());
        general.getResponse().setRsTime(rsHeader.getRsTime());
        general.getResponse().setOverrides(rsHeader.getOverrides());
        general.getResponse().setFEPNO(getSvcRs().getRs().fepNo);
    }

    @XStreamAlias("SvcRs")
    public static class SyncFEDISvcRs {
        @XStreamAlias("Rs")
        private SyncFEDIRs rs;

        public SyncFEDIRs getRs() { return rs; }

        public void setRs(SyncFEDIRs rs) { this.rs = rs; }
    }

    @XStreamAlias("SyncFEDIRs")
    public static class SyncFEDIRs {
        @XStreamAlias("FepNo")
        private String fepNo = "";

        public String getFepNo() { return fepNo; }

        public void setFepNo(String fepNo) { this.fepNo = fepNo; }
    }
}