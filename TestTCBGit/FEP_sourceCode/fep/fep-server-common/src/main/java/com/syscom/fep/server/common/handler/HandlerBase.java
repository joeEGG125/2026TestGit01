package com.syscom.fep.server.common.handler;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.enums.FEPChannel;
import com.syscom.fep.cache.FEPCache;
import com.syscom.fep.cache.enums.CacheItem;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.ext.mapper.ChannelExtMapper;
import com.syscom.fep.mybatis.mapper.BinMapper;
import com.syscom.fep.mybatis.model.Bin;
import com.syscom.fep.mybatis.model.Channel;
import com.syscom.fep.vo.text.atm.ATMGeneral;
import org.apache.commons.lang3.StringUtils;

public abstract class HandlerBase extends FEPBase {
    public static final String WEBATM = "768";

    protected String messageId;
    protected String messageCorrelationId;

    private BinMapper binMapper = SpringBeanFactoryUtil.getBean(BinMapper.class);

    private ChannelExtMapper channelExtMapper = SpringBeanFactoryUtil.getBean(ChannelExtMapper.class);

    //--ben--20221003 配合ATMHandler SPEC中新增
    public abstract String dispatch(FEPChannel channel, String atmNo, String data) throws Exception;

    public abstract String dispatch(FEPChannel channel, String data) throws Throwable;

    public abstract boolean dispatch(FEPChannel channel, Object data) throws Exception;

    protected HandlerBase() {
        // 2011.5.25 update ashiang:在HandlerBase一開始強制重讀Cache資料以避免Cache有時會沒有重新Reload的問題
        try {
            FEPCache.reloadCache(CacheItem.SYSSTAT);
            FEPCache.reloadCache(CacheItem.ZONE);
        } catch (Exception e) {
            LogHelperFactory.getTraceLogger().exceptionMsg(e, e.getMessage());
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageCorrelationId() {
        return messageCorrelationId;
    }

    public void setMessageCorrelationId(String messageCorrelationId) {
        this.messageCorrelationId = messageCorrelationId;
    }

    public Bin checkBinForATM(ATMGeneral general) throws Exception {
        Bin bin = null;
        //--ben-20220922-//if (StringUtils.isBlank(general.getRequest().getTXACT())) {
        if (StringUtils.isBlank("")) {
            // 2021-07-23 Richard add
            // 建議你們加判斷如果TRACK2也沒值就直接return null就好, 不要讓他出exception by Ashiang
            //--ben-20220922-//if (StringUtils.isNotBlank(general.getRequest().getTRACK2())) {
            // 以行庫代號( SYSSTAT_HBKNO)及ATM_TITA.TRACK2[1:6]為 KEY, 讀取 BIN 檔
            // modified by Maxine for 讀取 BIN 方式由 Cache 改為讀 DB
            //--ben-20220922-//	bin = this.getBin(general.getRequest().getTRACK2().substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
            //--ben-20220922-//}
        } else {
            // * 交易帳號前二位’00’為晶片卡交易, 不須檢核 BIN */
            //--ben-20220922-//if ("00".equals(general.getRequest().getTXACT().substring(0, 2))) {
            if ("00".equals("".substring(0, 2))) {
                // * 國際卡餘額查詢(IQ2), 須檢核 BIN *
                //--ben-20220922-//if (ATMTXCD.parse(general.getRequest().getTXCD()) == ATMTXCD.IQ2) {
                // 以行庫代號( SYSSTAT_HBKNO)及ATM_TITA.TRACK2[1:6]為 KEY, 讀取 BIN 檔
                // modified by Maxine for 讀取 BIN 方式由 Cache 改為讀 DB
                //--ben-20220922-//	bin = this.getBin(general.getRequest().getTRACK2().substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
                //--ben-20220922-//}
                //--ben-20220922-//} else if ("0".equals(general.getRequest().getTXACT().substring(0, 1))) {
            } else if ("0".equals("".substring(0, 1))) {
                /*
                 * 交易帳號僅第一位’0’為錢卡交易, 須檢核 BIN
                 * 以行庫代號( SYSSTAT_HBKNO)及ATM_TITA.TXACT[2:6]為 KEY, 讀取 BIN 檔
                 */
                // modified by Maxine for 讀取 BIN 方式由 Cache 改為讀 DB
                //--ben-20220922-//bin = this.getBin(general.getRequest().getTXACT().substring(1, 7), SysStatus.getPropertyValue().getSysstatHbkno());
            } else {
                /*
                 * 交易帳號第一位<>’0’為信用卡交易, 須檢核 BIN
                 * 以行庫代號( SYSSTAT_HBKNO)及ATM_TITA.TXACT[1:6]為 KEY, 讀取 BIN 檔
                 */
                // modified by Maxine for 讀取 BIN 方式由 Cache 改為讀 DB
                //--ben-20220922-//bin = this.getBin(general.getRequest().getTXACT().substring(0, 6), SysStatus.getPropertyValue().getSysstatHbkno());
            }
        }
        return bin;
    }

    /**
     * add by Maxine for 讀取 BIN 方式由 Cache 改為讀 DB
     *
     * @param no
     * @param bkno
     * @return
     */
    private Bin getBin(String no, String bkno) {
        return binMapper.selectByPrimaryKey(no, bkno);
    }

    /**
     * 在讀MsgCtl之後, 增加一個步驟, 用傳入dispatch的FEPChannel參數去讀CHANNEL Table, 然後把讀到的CHANNEL資料, 一樣放入xxData.setChannel(channel); 中
     *
     * @param messageBase
     * @param fepChannel
     */
    protected void setChannel(MessageBase messageBase, FEPChannel fepChannel) {
        if (fepChannel != null) {
            try {
                Channel channel = channelExtMapper.selectByPrimaryKey((short) fepChannel.getCode());
                if (channel != null) {
                    messageBase.setChannel(channel);
                }
            } catch (Throwable t) {
                this.logContext.setProgramException(t);
                this.logContext.setProgramName(StringUtils.join(ProgramName, ".setChannel"));
                this.logContext.setRemark(StringUtils.join("select CHANNEL by channel no = [", fepChannel.getCode(), "] with exception occur, ", t.getMessage()));
                sendEMS(this.logContext);
                throw t;
            }
        }
    }
}
