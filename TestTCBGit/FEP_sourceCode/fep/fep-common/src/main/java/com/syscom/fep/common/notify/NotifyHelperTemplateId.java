package com.syscom.fep.common.notify;

/**
 * 定義所有需要用到的NOTIFYTEMPLATE.TEMPLATE_ID
 */
public interface NotifyHelperTemplateId {
    /**
     * 傳送Batch郵件
     */
    String BATCH = "FE04";
    /**
     * 傳送EMS郵件
     */
    String EMS = "FE05";
    /**
     * 傳送AppMonitor郵件
     */
    String APP_MONITOR = "FE06";
    /**
     * 傳送IMS Gateway郵件
     */
    String IMS_GATEWAY = "FE07";
    /**
     * 傳送跨行主機回覆郵件
     */
    String SYS_NOTICE = "FE08";
}
