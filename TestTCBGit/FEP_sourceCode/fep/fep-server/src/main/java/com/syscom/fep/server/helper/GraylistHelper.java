package com.syscom.fep.server.helper;

import com.syscom.fep.base.cnst.SvrConst;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.enums.SubSystem;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.frmcommon.annotation.StackTracePointCut;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.AtmmstrExtMapper;
import com.syscom.fep.mybatis.ext.mapper.GraylistExtMapper;
import com.syscom.fep.mybatis.model.Graylist;
import com.syscom.fep.server.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class GraylistHelper extends BaseController {

    private final GraylistExtMapper graylistExtMapper = SpringBeanFactoryUtil.getBean(GraylistExtMapper.class);
    private final AtmmstrExtMapper atmmstrExtMapper = SpringBeanFactoryUtil.getBean(AtmmstrExtMapper.class);
    /**
     *
     * @param bankNo
     * @param actNo
     * @param TxnDate
     * @return
     */
    public Graylist getGraylistByAct(String bankNo, String actNo, String TxnDate) throws Exception {
        try {
            return graylistExtMapper.getGraylistByAct(bankNo, actNo, TxnDate);
        } catch (Exception e) {
            LogData log = new LogData();
            log.setSubSys(SubSystem.CMN);
            log.setChannel(FEPChannel.BATCH);
            log.setProgramName(StringUtils.join(ProgramName, ".getGraylistByAct"));
            log.setProgramException(e);
            sendEMS(log);
            throw ExceptionUtil.createException(e);
        }
    }

    public String getATMAddressCByAtmno(String atmAtmno) throws Exception {
        return atmmstrExtMapper.getATMAddressCByAtmno(atmAtmno);
    }

    @Override
    protected String processRequestData(ProgramFlow programFlow, String messageIn) {
        return null;
    }
}
