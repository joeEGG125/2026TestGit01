package com.syscom.fep.service.svr;

import com.syscom.fep.base.FEPBase;
import com.syscom.fep.base.cnst.Const;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.log.LogMDC;
import com.syscom.fep.frmcommon.thread.ThreadWrapper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PreDestroy;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processor基礎類
 * <p>
 * 內部自帶線程處理, 所以只需要實作業務邏輯部分
 *
 * @author Richard
 */
public abstract class SvrProcessor extends FEPBase implements Runnable {
    protected static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
    // 線程名稱, 用於在log中進行甄別
    private final String THREAD_NAME = StringUtils.replace(this.getClass().getSimpleName(), "Processor", StringUtils.EMPTY);
    private Thread thread;
    // 暫停/停止/處理中flag
    private final AtomicBoolean pauseFlag = new AtomicBoolean(false);
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private final AtomicBoolean businessFlag = new AtomicBoolean(false);

    /**
     * 初始化, 準備工作可以在這個方法中進行
     *
     * @throws Exception
     */
    protected abstract void initialization() throws Exception;

    /**
     * 業務處理, 會進行一直loop進行, 如需要進行sleep, 可以寫在裡面
     *
     * @return
     * @throws Exception
     */
    protected abstract FEPReturnCode doBusiness() throws Exception;

    /**
     * 暫停動作
     *
     * @throws Exception
     */
    protected abstract void doPause() throws Exception;

    /**
     * 停止動作
     *
     * @throws Exception
     */
    protected abstract void doStop() throws Exception;

    /**
     * 啟動線程, 開始執行
     */
    public void start() {
        LogMDC.put(Const.MDC_PROFILE, this.getMDCProfileName());
        try {
            SERVICELOGGER.info(this.getName(), " Initialization Starting...");
            initialization();
            SERVICELOGGER.info(this.getName(), " Initialization Successfully.");
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, this.getName(), " Initialization exception occur.");
            return;
        }
        try {
            SERVICELOGGER.info(this.getName(), " Starting...");
            if (thread == null) {
                SERVICELOGGER.info(this.getName(), " Create Thread...");
                thread = new Thread(ThreadWrapper.wrap(this), this.getName());
                thread.start();
            }
            SERVICELOGGER.info(this.getName(), " Start Successfully.");
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, this.getName(), " Start Fail.");
        }
    }

    /**
     * 暫停
     */
    public void pause() {
        pauseFlag.set(true);
        // 等待doBusiness執行完畢
        if (businessFlag.get()) {
            synchronized (businessFlag) {
                SERVICELOGGER.info(this.getName(), " start to wait doBusiness before pause...");
                try {
                    businessFlag.wait();
                } catch (InterruptedException e) {
                    SERVICELOGGER.warn(e, e.getMessage());
                }
                SERVICELOGGER.info(this.getName(), " wait doBusiness succeed before pause...");
            }
        }
        // 如果pause為false, 則直接return
        if (!pauseFlag.get()) {
            SERVICELOGGER.info(this.getName(), " was resume...");
            return;
        }
        // 如果有stop, 則直接return
        if (stopFlag.get()) {
            SERVICELOGGER.info(this.getName(), " is stopped...");
            return;
        }
        try {
            SERVICELOGGER.info(this.getName(), " pause Starting...");
            doPause();
            SERVICELOGGER.info(this.getName(), " pause Successfully.");
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, this.getName(), " doPause Fail.");
        } finally {
            SERVICELOGGER.info(this.getName(), " Pause Finished.");
        }
    }

    /**
     * 在暫停過後恢復運行
     */
    public void resume() {
        if (pauseFlag.get()) {
            SERVICELOGGER.info(this.getName(), " resume Starting...");
            pauseFlag.set(false);
            synchronized (pauseFlag) {
                SERVICELOGGER.info(this.getName(), " Call pauseFlag.notifyAll() to resume...");
                pauseFlag.notifyAll();
            }
            SERVICELOGGER.info(this.getName(), " resume Finished...");
        } else{
            SERVICELOGGER.warn(this.getName(), " no need to resume...");
        }
    }

    /**
     * 停止
     */
    @PreDestroy
    public void stop() {
        stopFlag.set(true);
        // 等待doBusiness執行完畢
        if (businessFlag.get()) {
            synchronized (businessFlag) {
                SERVICELOGGER.info(this.getName(), " start to wait doBusiness before stop...");
                try {
                    businessFlag.wait();
                } catch (InterruptedException e) {
                    SERVICELOGGER.warn(e, e.getMessage());
                }
                SERVICELOGGER.info(this.getName(), " wait doBusiness succeed before stop...");
            }
        }
        // 如果之前有pause, 這裡要notifyAll一下
        synchronized (pauseFlag) {
            pauseFlag.notifyAll();
        }
        try {
            SERVICELOGGER.info(this.getName(), " stop Starting...");
            doStop();
            SERVICELOGGER.info(this.getName(), " stop Successfully...");
        } catch (Exception e) {
            SERVICELOGGER.exceptionMsg(e, this.getName(), " doStop Fail.");
        } finally {
            SERVICELOGGER.info(this.getName(), " Stop Finished.");
        }
        if (thread != null) {
            thread = null;
        }
    }

    @Override
    public void run() {
        LogMDC.put(Const.MDC_PROFILE, this.getMDCProfileName());
        while (!stopFlag.get()) {
            // 如果有pause, 則暫停
            if (pauseFlag.get()) {
                SERVICELOGGER.info(this.getName(), " is Pausing...");
                synchronized (pauseFlag) {
                    try {
                        pauseFlag.wait();
                    } catch (InterruptedException e) {
                        SERVICELOGGER.warn(e, e.getMessage());
                    }
                }
                SERVICELOGGER.info(this.getName(), " receive pauseFlag.notifyAll() and continue to run...");
            }
            // 如果有stop, 則直接break跳出loop
            if (stopFlag.get()) {
                SERVICELOGGER.info(this.getName(), " is stopped...");
                break;
            }
            final String startToRunTime = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_TIME_HHMMSSSSS_PLAIN);
            LogMDC.put(Const.MDC_SERVICE_SVR_START_TO_RUN_TIME, startToRunTime);
            try {
                SERVICELOGGER.info(this.getName(), " start to doBusiness...");
                businessFlag.set(true);
                doBusiness();
                SERVICELOGGER.info(this.getName(), " doBusiness Succeed");
            } catch (Exception e) {
                SERVICELOGGER.exceptionMsg(e, this.getName(), " doBusiness with exception occur");
            } finally {
                businessFlag.set(false);
                // 之前有pause或者stop在等待業務處理完畢後, 要notifyAll一下讓pause或者stop繼續往下走
                synchronized (businessFlag) {
                    businessFlag.notifyAll();
                }
            }
        }
    }

    /**
     * 是否暫停中
     *
     * @return
     */
    public boolean isPaused() {
        return pauseFlag.get();
    }

    /**
     * 是否停止
     *
     * @return
     */
    public boolean isStop() {
        return stopFlag.get();
    }

    /**
     * 線程名稱
     *
     * @return
     */
    protected String getName() {
        return THREAD_NAME;
    }

    /**
     * MDC profileName, 決定了log檔檔名
     *
     * @return
     */
    protected String getMDCProfileName() {
        return THREAD_NAME;
    }

    /**
     * 設置業務已經處理完成
     */
    protected void doBusinessFinished(){
        this.businessFlag.set(false);
    }
}
