package com.syscom.fep.vo.text.rm.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class C1100_Response extends TextBase<RMGeneral> {
    @XStreamAlias("RsHeader")
    private FEPRsHeader rsHeader;
    @XStreamAlias("SvcRs")
    private C1100SvcRs c1100SvcRs;

    public FEPRsHeader getRsHeader() { return rsHeader; }

    public void setRsHeader(FEPRsHeader rsHeader) { this.rsHeader = rsHeader; }

    public C1100SvcRs getC1100SvcRs() { return c1100SvcRs; }

    public void setC1100SvcRs(C1100SvcRs c1100SvcRs) { this.c1100SvcRs = c1100SvcRs; }



    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
    public String makeMessageFromGeneral(RMGeneral general) throws Exception {
        if (rsHeader == null){
            rsHeader = new FEPRsHeader();
        }
        if (c1100SvcRs == null){
            c1100SvcRs = new C1100SvcRs();
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
        if (general.getResponse().getOverrides() == null){
            FEPRsHeader.FEPRsHeaderOverride ovr[] = new FEPRsHeader.FEPRsHeaderOverride[0];
            rsHeader.setOverrides(ovr);
        }else {
            rsHeader.setOverrides(general.getResponse().getOverrides());
        }
        C1100Rs c1100Rs = new C1100Rs();
        c1100Rs.setFepNo(general.getResponse().getFEPNO());
        return serializeToXml(this);
    }

    @Override
    public void toGeneral(RMGeneral rmGeneral) throws Exception {
        rmGeneral.getResponse().setChlEJNo(rsHeader.getChlEJNo());
        rmGeneral.getResponse().setEJNo(rsHeader.getEJNo());
        rmGeneral.getResponse().setRqTime(rsHeader.getRqTime());
        rmGeneral.getResponse().setRsStatRsStateCode(rsHeader.getRsStat().getRsStatCode().getValue());
        rmGeneral.getResponse().setRsStatRsStateCodeType(rsHeader.getRsStat().getRsStatCode().getType());
        rmGeneral.getResponse().setRsStatDesc(rsHeader.getRsStat().getDesc());
        rmGeneral.getResponse().setRsTime(rsHeader.getRsTime());
        rmGeneral.getResponse().setOverrides(rsHeader.getOverrides());
        rmGeneral.getResponse().setFEPNO(getC1100SvcRs().getRs().fepNo);
    }

    @XStreamAlias("C1100SvcRs")
    public static class C1100SvcRs{
        @XStreamAlias("Rs")
        private C1100Rs rs;

        public C1100Rs getRs() { return rs; }

        public void setRs(C1100Rs rs) { this.rs = rs; }
    }

    @XStreamAlias("C1100Rs")
    public static class C1100Rs {
        @XStreamAlias("FepNo")
        private String fepNo = "";

        public String getFepNo() { return fepNo; }

        public void setFepNo(String fepNo) { this.fepNo = fepNo; }
    }
}
