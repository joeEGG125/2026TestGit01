package com.syscom.fep.web.form.atmmon;

import com.syscom.fep.web.form.BaseForm;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Getter
@Setter
public class UI_060550_Ucdid_Form extends BaseForm {

    private String ucdidIdno = StringUtils.SPACE;

    private String ucdidHealthid = StringUtils.SPACE;

    private String ucdid2566Txdate = StringUtils.SPACE;

    private String ucdid2566Stan = StringUtils.SPACE;

    private String ucdid2566Txtime;

    private String ucdid2566TbsdyFisc;

    private String ucdid2566Tbsdy;

    private String ucdid2566Apistatus;

    private String ucdidTroutBkno;

    private String ucdidTroutActno;

    private String ucdidBkno;

    private String ucdidAtmno;

    private String ucdid2510Txdate;

    private String ucdid2510Stan;

    private String ucdid2510Txtime;

    private String ucdid2510TbsdyFisc;

    private String ucdid2510Tbsdy;

    private String ucdid2510Status;

    private String ucdidUcdApiseqno;

    private String ucdidUcdTxno;

    private String ucdidUcdResptime;

    private String ucdidUcdRc;

    private String ucdidUcdrApiseqno;

    private String ucdidUcdrTxno;

    private String ucdidUcdrResptime;

    private String ucdidUcdrRc;

    private String ucdidUcdrChannel;

    private String ucdidLaststatus;

    private String ucdidHistory;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}