package com.syscom.fep.frmcommon.transaction;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TransactionWrapper
 *
 * @author Richard
 */
public class TransactionWrapper implements AutoCloseable {
    private final LogHelper logger = new LogHelper();
    private final PlatformTransactionManager transactionManager;
    private TransactionStatus transactionStatus;
    /**
     * 是否已經commit
     */
    private final AtomicBoolean committed = new AtomicBoolean(false);
    /**
     * 是否已經rollback
     */
    private final AtomicBoolean rolledBack = new AtomicBoolean(false);
    /**
     * 是否自動rollback, 否則自動commit, 預設為true
     */
    private boolean autoRollback = true;


    public TransactionWrapper(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 指定是否自動rollback, 否則自動commit
     *
     * @param autoRollback true-自動rollback, false-自動commit
     * @return
     */
    public TransactionWrapper setAutoRollback(boolean autoRollback) {
        this.autoRollback = autoRollback;
        return this;
    }

    /**
     * 開始執行事務
     *
     * @return
     * @throws Exception
     */
    public TransactionWrapper beginTransaction() throws Exception {
        return beginTransaction(null);
    }

    /**
     * 開始執行事務
     *
     * @param processor
     * @return
     * @throws Exception
     */
    public TransactionWrapper beginTransaction(TransactionProcessor processor) throws Exception {
        return beginTransaction(processor, false);
    }

    /**
     * 開始執行事務
     *
     * @param processor
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public TransactionWrapper beginTransaction(TransactionProcessor processor, boolean autoCommit) throws Exception {
        return beginTransaction(null, processor, autoCommit);
    }

    /**
     * 開始執行事務
     *
     * @param definition
     * @param processor
     * @return
     * @throws Exception
     */
    public TransactionWrapper beginTransaction(TransactionDefinition definition, TransactionProcessor processor) throws Exception {
        return beginTransaction(definition, processor, false);
    }

    /**
     * 開始執行事務
     *
     * @param definition
     * @param processor
     * @param autoCommit
     * @return
     * @throws Exception
     */
    public TransactionWrapper beginTransaction(TransactionDefinition definition, TransactionProcessor processor, boolean autoCommit) throws Exception {
        if (definition == null) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            this.transactionStatus = transactionManager.getTransaction(def);
            logger.debug("Transaction started");
        } else {
            this.transactionStatus = transactionManager.getTransaction(definition);
            logger.debug("Transaction started with custom definition");
        }
        if (processor != null) {
            processor.doTransaction();
            if (autoCommit)
                finishTransaction(false, "beginTransaction()");
        }
        return this;
    }

    /**
     * 結束事務
     *
     * @param rollback
     * @param methodName
     */
    private void finishTransaction(boolean rollback, String methodName) {
        if (transactionStatus != null && !committed.get() && !rolledBack.get() && !transactionStatus.isCompleted()) {
            if (rollback) {
                transactionManager.rollback(transactionStatus);
                rolledBack.set(true);
                logger.debug("Transaction auto-rollback in ", methodName);
            } else {
                transactionManager.commit(transactionStatus);
                rolledBack.set(true);
                logger.debug("Transaction auto-commit in ", methodName);
            }
        }
    }

    /**
     * 事務提交
     */
    public void commit() {
        if (transactionStatus == null) {
            logger.warn("Transaction was not begin");
            return;
        }
        if (committed.get() || rolledBack.get()) {
            logger.warn("Transaction already completed, committed:", committed, ", rolledBack:", rolledBack);
            return;
        }
        if (!transactionStatus.isCompleted()) {
            if (transactionStatus.isRollbackOnly()) {
                logger.warn("Cannot commit, transaction is marked as rollback-only, performing rollback instead");
                rollback();
                return;
            }
            transactionManager.commit(transactionStatus);
            committed.set(true);
            logger.debug("Transaction committed manually");
        }
    }

    /**
     * 事務回滾
     */
    public void rollback() {
        rollback(null);
    }

    /**
     * 事務回滾, 並指定原因
     *
     * @param reason
     */
    public void rollback(String reason) {
        if (transactionStatus == null) {
            logger.warn("Transaction was not begin");
            return;
        }
        if (committed.get() || rolledBack.get()) {
            logger.warn("Transaction already completed, committed:", committed, ", rolledBack:", rolledBack);
            return;
        }
        if (!transactionStatus.isCompleted()) {
            transactionManager.rollback(transactionStatus);
            rolledBack.set(true);
            StringBuilder sb = new StringBuilder();
            sb.append("Transaction rolled back manually");
            if (StringUtils.isNotBlank(reason)) {
                sb.append(", reason:").append(reason);
            }
            logger.debug(sb.toString());
        }
    }

    /**
     * 強制事務回滾, 並指定原因
     *
     * @param reason
     */
    public void forceRollback(String reason) {
        if (transactionStatus == null) {
            logger.warn("Transaction was not begin");
            return;
        }
        if (rolledBack.get()) {
            logger.warn("Transaction already rolled back");
            return;
        }
        if (!transactionStatus.isCompleted()) {
            transactionManager.rollback(transactionStatus);
            rolledBack.set(true);
            committed.set(false); // 重設 committed 狀態
            StringBuilder sb = new StringBuilder();
            sb.append("Transaction force rolled back");
            if (reason != null && !reason.trim().isEmpty()) {
                sb.append(", reason: ").append(reason);
            }
            logger.debug(sb.toString());
        }
    }

    /**
     * 事務是否完成
     *
     * @return
     */
    public boolean isCompleted() {
        return transactionStatus.isCompleted();
    }

    /**
     * 事務是否有提交
     *
     * @return
     */
    public boolean isCommitted() {
        return committed.get();
    }

    /**
     * 事務是否有回滾
     *
     * @return
     */
    public boolean isRolledBack() {
        return rolledBack.get();
    }

    public boolean isRollbackOnly() {
        return transactionStatus.isRollbackOnly();
    }

    public boolean isNewTransaction() {
        return transactionStatus.isNewTransaction();
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    @Override
    public void close() {
        try {
            finishTransaction(autoRollback, "close()");
        } catch (Throwable t) {
            logger.error(t, "Failed to auto-", autoRollback ? "rollback" : "commit", " transaction in close()");
        }
    }
}
