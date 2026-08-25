package com.syscom.fep.frmcommon.pool;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericPooledFactory<T> extends BasePooledObjectFactory<T> {
    private List<T> list;
    private AtomicInteger createdIndex = new AtomicInteger(-1);

    public GenericPooledFactory() {
        list = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(T t) {
        if (t != null)
            this.list.add(t);
    }

    public void addAll(List<T> list) {
        if (CollectionUtils.isNotEmpty(list))
            this.list.addAll(list);
    }

    public void removeAll() {
        while (this.size() > 0) {
            this.list.remove(0);
        }
        createdIndex.set(-1);
    }

    public int size() {
        return this.list.size();
    }

    @Override
    public T create() throws Exception {
        return list.get(createdIndex.incrementAndGet());
    }

    @Override
    public PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<T> p, DestroyMode destroyMode) throws Exception {
    }
}
