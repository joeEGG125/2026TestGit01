package com.syscom.fep.ws.client;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.ws.client.entity.WsClientType;

import java.util.Objects;

public interface WsClient<WsRequest, WsResponse, MessageOut, MessageIn> {
    LogHelper logHelper = LogHelperFactory.getWsClientLogger();

    /**
     * 發送接收電文
     *
     * @param messageOut
     * @return
     * @throws Exception
     */
    MessageIn sendReceive(MessageOut messageOut) throws Exception;

    /**
     * 建立送出的電文物件
     *
     * @param messageOut
     * @return
     */
    WsRequest createWsRequest(MessageOut messageOut);

    /**
     * 轉換接收的電文
     *
     * @param response
     * @return
     */
    MessageIn transferToMessageIn(WsResponse response);

    /**
     * Client的類型
     *
     * @return
     */
    WsClientType getWsClientType();

    /**
     * 送出的訊息轉字串形式, 在log中列印出來
     *
     * @param messageOut
     * @return
     */
    default String outToString(MessageOut messageOut) {
        return Objects.toString(messageOut);
    }

    /**
     * 接收的訊息轉字串形式, 在log中列印出來
     *
     * @param messageIn
     * @return
     */
    default String inToString(MessageIn messageIn) {
        return Objects.toString(messageIn);
    }

    /**
     * 是否列印log
     *
     * @return
     */
    default boolean isLogEnable() {return true;}
}
