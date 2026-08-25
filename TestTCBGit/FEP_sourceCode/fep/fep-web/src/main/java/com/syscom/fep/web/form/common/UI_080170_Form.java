package com.syscom.fep.web.form.common;

import com.syscom.fep.vo.communication.ToFEPCBSGetAllLineStatus;
import com.syscom.fep.vo.communication.ToFEPCBSGetAllLineStatus.LineType;
import com.syscom.fep.web.form.BaseForm;

import java.util.ArrayList;
import java.util.List;

public class UI_080170_Form extends BaseForm {
    private final LineType lineType;
    private final List<ToFEPCBSGetAllLineStatus> status = new ArrayList<>();
    private final boolean readOnly;

    public UI_080170_Form(LineType lineType) {
        this(lineType, false);
    }

    public UI_080170_Form(LineType lineType, boolean readOnly) {
        this.lineType = lineType;
        this.readOnly = readOnly;
    }

    public LineType getLineType() {
        return lineType;
    }

    public List<ToFEPCBSGetAllLineStatus> getStatus() {
        return status;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
