package com.syscom.fep.frmcommon.pool;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class GenericPool<T> extends GenericObjectPool<T> {
    private final LogHelper logger = new LogHelper();

    public GenericPool(GenericPooledFactory<T> factory) {
        super(factory);
    }

    public GenericPool(GenericPooledFactory<T> factory, GenericObjectPoolConfig<T> config) {
        super(factory, config);
    }

    public GenericPool(GenericPooledFactory<T> factory, GenericObjectPoolConfig<T> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }

    /**
     * 設定取element按照順序取
     */
    public void setBorrowObjectBySequence() {
        this.setLifo(false);
        try {
            this.addObjects(((GenericPooledFactory<T>) this.getFactory()).size());
        } catch (Exception e) {
            logger.warn(e, e.getMessage());
        }
    }
}