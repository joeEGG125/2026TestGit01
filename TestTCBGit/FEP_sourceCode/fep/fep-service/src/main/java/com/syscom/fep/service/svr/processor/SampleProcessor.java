package com.syscom.fep.service.svr.processor;

import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.service.svr.SvrProcessor;

/**
 * 範例
 */
public class SampleProcessor extends SvrProcessor {
    /**
     * 初始化, 準備工作可以在這個方法中進行
     *
     * @throws Exception
     */
    @Override
    protected void initialization() throws Exception {
        SERVICELOGGER.info("initialization");
    }

    /**
     * 業務處理, 會進行一直loop進行, 如需要進行sleep, 可以寫在裡面
     *
     * @return
     * @throws Exception
     */
    @Override
    protected FEPReturnCode doBusiness() throws Exception {
        SERVICELOGGER.info("doBusiness");
        Thread.sleep(5000L);
        return FEPReturnCode.Normal;
    }

    /**
     * 暫停動作
     *
     * @throws Exception
     */
    @Override
    protected void doPause() throws Exception {}

    /**
     * 停止動作
     *
     * @throws Exception
     */
    @Override
    protected void doStop() throws Exception {
        SERVICELOGGER.info("doStop");
    }
}
