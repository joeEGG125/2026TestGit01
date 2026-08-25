package com.syscom.fep.vo.text.rm.response;

import com.syscom.fep.vo.text.FEPRsHeader;
import com.syscom.fep.vo.text.TextBase;
import com.syscom.fep.vo.text.rm.RMGeneral;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("FEP")
public class R1001_Response extends TextBase<RMGeneral> {

    @XStreamAlias("RsHeader")
    private FEPRsHeader rsHeader;
    @XStreamAlias("SvcRs")
    private R1001SvcRs r1001SvcRs;

    public FEPRsHeader getRsHeader() { return rsHeader; }

    public void setRsHeader(FEPRsHeader rsHeader) { this.rsHeader = rsHeader;}

    public R1001SvcRs getR1001SvcRs() { return r1001SvcRs; }

    public void setR1001SvcRs(R1001SvcRs r1001SvcRs) { this.r1001SvcRs = r1001SvcRs; }

    /**
     * 對應到General
     */
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
    }

    /**
     *從 General 對應回來
     */
    @Override
    public String makeMessageFromGeneral(RMGeneral general) throws Exception {
        if (rsHeader == null){
            rsHeader = new FEPRsHeader();
        }
        if (r1001SvcRs == null){
            r1001SvcRs = new R1001SvcRs();
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
        R1001Rs r1001Rs = new R1001Rs();
        r1001Rs.setRsField(new R1001Rs());;
        return serializeToXml(this);
    }
    @Override
    public int getTotalLength() {
        return 0;
    }

    @XStreamAlias("R1001SvcRs")
    public static class R1001SvcRs{
        @XStreamAlias("Rs")
        private R1001Rs rs;

        public R1001Rs getRs() { return rs; }
        public void setRs(R1001Rs rs) { this.rs = rs; }
    }

    @XStreamAlias("Rs")
    public static class R1001Rs{
        @XStreamAlias("rsField")
        private R1001Rs rsField;

        public R1001Rs getRsField() { return rsField; }

        public void setRsField(R1001Rs rsField) { this.rsField = rsField; }

    }
}
