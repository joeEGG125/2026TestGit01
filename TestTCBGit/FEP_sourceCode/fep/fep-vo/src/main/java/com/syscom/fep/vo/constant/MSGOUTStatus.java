package com.syscom.fep.vo.constant;

/**
 * MSGOUT狀態
 * @author xingyun_yang
 * @create 2021/9/27
 */
public interface MSGOUTStatus {
    /**
     * 已登錄
     */
    public static final String Registered = "01";
    /**
     * 發訊
     */
    public static final String Send = "02";
    /**
     * 已取消
     */
    public static final String Canceled = "03";
    /**
     * 傳送中
     */
    public static final String Transferring = "04";
    /**
     * 已傳送
     */
    public static final String Transferred = "05";
    /**
     * 財金拒絕
     */
    public static final String FISCRefuse = "06";
    /**
     * FEP取消
     */
    public static final String FEPCancel = "07";
    /**
     * 系統問題
     */
    public static final String SystemProblem = "99";
}
