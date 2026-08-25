package com.syscom.fep.frmcommon.netty;

import com.syscom.fep.frmcommon.communication.BaseResponse;
import org.apache.commons.lang3.StringUtils;

public interface NettyEventExecutorDataHandler {

    /**
     * 獲取統計數據
     *
     * @return
     */
    NettyEventExecutorData getNettyEventExecutorData();

    /**
     * 設定線程數
     *
     * @param nThreads
     * @return
     * @throws Exception
     */
    boolean setNettyEventExecutorThreads(int nThreads) throws Exception;

    /**
     * 檢查線程數是否超過最大倍數
     *
     * @param nThreads
     * @return
     */
    default BaseResponse<?, NettyEventExecutorDataErrorCode> checkEventExecutorThreads(int nThreads) {
        // 修改的線程數不能超過配置檔中預設的線程數的最大倍數
        int MAX_THREADS_RATIO = 2;
        BaseResponse<?, NettyEventExecutorDataErrorCode> response = new BaseResponse<>();
        NettyEventExecutorData data = this.getNettyEventExecutorData();
        if (data != null && nThreads > data.getExecutorConfigurationCount() * MAX_THREADS_RATIO) {
            response.setError(StringUtils.join("您輸入的線程數", String.format("「%s」", Integer.toString(nThreads)), ", 己超過設定檔", String.format("「%s」", Integer.toString(data.getExecutorConfigurationCount())), "的", MAX_THREADS_RATIO, "倍, 請重新輸入"));
            response.setErrorCode(NettyEventExecutorDataErrorCode.SET_EVENT_EXECUTOR_THREADS_OVER_MAX_THREADS_RATIO);
            return response;
        }
        return response;
    }
}
