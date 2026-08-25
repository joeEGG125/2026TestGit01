package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.frmcommon.ref.RefString;
import com.syscom.fep.frmcommon.util.ExceptionUtil;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.ext.mapper.MsgfileExtMapper;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.mybatis.model.Msgfile;
import com.syscom.fep.server.aa.inbk.INBKAABase;
import com.syscom.fep.server.common.FeptxnTxrust;
import com.syscom.fep.server.common.TxHelper;
import com.syscom.fep.server.common.adapter.ATMAdapter;
import com.syscom.fep.server.common.business.host.Credit;
import com.syscom.fep.vo.constant.BINPROD;
import com.syscom.fep.vo.constant.FEPTxnMessageFlow;
import com.syscom.fep.vo.constant.NormalRC;
import com.syscom.fep.vo.enums.ATMTXCD;
import com.syscom.fep.vo.enums.CreditTxType;
import com.syscom.fep.vo.enums.FISCPCode;
import com.syscom.fep.vo.enums.T24TxType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.List;

/**
 * @author Richard
 */
public class PaySelfRequestA extends INBKAABase {

    public PaySelfRequestA(ATMData txnData) throws Exception {
        super(txnData);
    }

    /**
     * 處理電文
     *
     * @return
     */
    @Override
    public String processRequestData() throws Exception {
        throw ExceptionUtil.createNotImplementedException(ProgramName, "尚未實作!!!");
    }
}
