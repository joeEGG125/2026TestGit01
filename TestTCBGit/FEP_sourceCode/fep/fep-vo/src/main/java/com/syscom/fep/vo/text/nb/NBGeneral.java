package com.syscom.fep.vo.text.nb;

import com.syscom.fep.vo.text.ivr.RCV_VO_GeneralTrans_RQ;
import com.syscom.fep.vo.text.nb.RCV_NB_GeneralTrans_RQ;

public class NBGeneral {
    public NBGeneralRequest getRequest() {
        return mRequest;
    }
    public NBGeneralResponse getResponse() {
        return mResponse;
    }

    public NBGeneralRequest getmRequest() {
        return mRequest;
    }

    public void setmRequest(NBGeneralRequest mRequest) {
        this.mRequest = mRequest;
    }

    public NBGeneralResponse getmResponse() {
        return mResponse;
    }

    public void setmResponse(NBGeneralResponse mResponse) {
        this.mResponse = mResponse;
    }

    public RCV_NB_GeneralTrans_RQ getmRequestNB() {
        return mRequestNB;
    }

    public void setmRequestNB(RCV_NB_GeneralTrans_RQ mRequestNB) {
        this.mRequestNB = mRequestNB;
    }

    public SEND_NB_GeneralTrans_RS getmResponseNB() {
        return mResponseNB;
    }

    public void setmResponseNB(SEND_NB_GeneralTrans_RS mResponseNB) {
        this.mResponseNB = mResponseNB;
    }

    private NBGeneralRequest mRequest;
    private NBGeneralResponse mResponse;
    private RCV_NB_GeneralTrans_RQ mRequestNB;
    private SEND_NB_GeneralTrans_RS mResponseNB;

    private RCV_VA_GeneralTrans_RQ vaRequest;
    private RCV_NB_GeneralTrans_RQ nbRequest;

    private RCV_VO_GeneralTrans_RQ voRequest;

    public NBGeneral()
    {
        mRequest = new NBGeneralRequest();
        mResponse = new NBGeneralResponse();
        mRequestNB = new RCV_NB_GeneralTrans_RQ();
        mResponseNB = new SEND_NB_GeneralTrans_RS();
        nbRequest=new RCV_NB_GeneralTrans_RQ();
        vaRequest=new RCV_VA_GeneralTrans_RQ();
        voRequest=new RCV_VO_GeneralTrans_RQ();
    }

    public RCV_NB_GeneralTrans_RQ getNbRequest()
    {
        return nbRequest;
    }

    public RCV_VA_GeneralTrans_RQ getVaRequest()
    {
        return vaRequest;
    }
    public RCV_VO_GeneralTrans_RQ getVoRequest()
    {
        return voRequest;
    }


	public void setVaRequest(RCV_VA_GeneralTrans_RQ vaRequest) {
		this.vaRequest = vaRequest;
	}

	public void setNbRequest(RCV_NB_GeneralTrans_RQ nbRequest) {
		this.nbRequest = nbRequest;
	}

	public void setVoRequest(RCV_VO_GeneralTrans_RQ voRequest) {
		this.voRequest = voRequest;
	}

}
