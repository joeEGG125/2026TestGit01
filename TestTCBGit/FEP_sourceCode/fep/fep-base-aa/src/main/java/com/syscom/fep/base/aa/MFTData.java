package com.syscom.fep.base.aa;

import com.syscom.fep.vo.text.mft.MFTGeneral;
import com.syscom.fep.vo.text.nb.*;

import java.math.BigDecimal;

public class MFTData extends MessageBase {
    public MFTGeneral getTxMFTObject() {
        return txMFTObject;
    }

    public void setTxMFTObject(MFTGeneral txMFTObject) {
        this.txMFTObject = txMFTObject;
    }

    private MFTGeneral txMFTObject;


}
