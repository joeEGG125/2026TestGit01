package com.syscom.fep.web.form.common;

import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus;
import com.syscom.fep.vo.communication.ToFEPIMSGetAllLineStatus.LineType;
import com.syscom.fep.web.form.BaseForm;

import java.util.ArrayList;
import java.util.List;

public class UI_080180_Form extends BaseForm {
    private final LineType lineType;
    private final List<ToFEPIMSGetAllLineStatus> status = new ArrayList<>();

    public UI_080180_Form(LineType lineType) {
        this.lineType = lineType;
    }

    public LineType getLineType() {
        return lineType;
    }

    public List<ToFEPIMSGetAllLineStatus> getStatus() {
        return status;
    }
}
