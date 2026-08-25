package com.syscom.fep.vo.text.rm.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class C1700_Response extends TextBase<RMGeneral> {
    @XStreamAlias("RsHeader")
    private FEPRsHeader rsHeader;
    @XStreamAlias("SvcRs")
    private C1700SvcRs c1700SvcRs;

    public FEPRsHeader getRsHeader() { return rsHeader; }

    public void setRsHeader(FEPRsHeader rsHeader) { this.rsHeader = rsHeader; }

    public C1700SvcRs getC1700SvcRs() { return c1700SvcRs; }

    public void setC1700SvcRs(C1700SvcRs c1700SvcRs) { this.c1700SvcRs = c1700SvcRs; }

    @Override
    public int getTotalLength() {
        return 0;
    }

    @Override
    public String makeMessageFromGeneral(RMGeneral general)  {
        if (rsHeader == null){
            rsHeader = new FEPRsHeader();
        }
        if (c1700SvcRs == null){
            c1700SvcRs = new C1700SvcRs();
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
        C1700Rs c1700Rs = new C1700Rs();
        c1700Rs.setFepNo(general.getResponse().getFEPNO());;
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
        general.getResponse().setFEPNO(getC1700SvcRs().getRs().fepNo);
    }

    @XStreamAlias("C1700SvcRs")
    public static class C1700SvcRs{
        @XStreamAlias("Rs")
        private C1700Rs  rs;

        public C1700Rs getRs() { return rs; }
        public void setRs(C1700Rs rs) { this.rs = rs;
        }
    }

    @XStreamAlias("C1700Rs")
    public static class C1700Rs{
        @XStreamAlias("fepNo")
        private String fepNo = "";

        public String getFepNo() {
            return fepNo;
        }

        public void setFepNo(String fepNo) {
            this.fepNo = fepNo;
        }
    }
}