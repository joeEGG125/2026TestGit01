package com.syscom.fep.frmcommon.roundrobin;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.collections4.CollectionUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * RoundRobin輪詢
 *
 * @param <T>
 */
public class RoundRobin<T> {
    private final LogHelper logger = new LogHelper();
    /**
     * 數據列表
     */
    private final List<T> list = Collections.synchronizedList(new ArrayList<>());
    /**
     * 下一筆數據的index
     */
    private final AtomicInteger next = new AtomicInteger(0);
    /**
     * 遞增還是遞減
     */
    private RoundRobinAccumulate accumulate = RoundRobinAccumulate.Increment;
    /**
     * 是否已經完成了一次輪詢
     */
    private final AtomicBoolean roundRobinAll = new AtomicBoolean(false);
    /**
     * 這裡建立同步鎖, 避免編輯列表的同時又需要從列表中取數據
     */
    private ReentrantLock lock;

    public RoundRobin() {}

    public RoundRobin(final List<T> list) {
        this(list, RoundRobinAccumulate.Increment, false);
    }

    public RoundRobin(final List<T> list, RoundRobinAccumulate accumulate) {
        this(list, accumulate, false);
    }

    public RoundRobin(final List<T> list, boolean reentrantLock) {
        this(list, RoundRobinAccumulate.Increment, reentrantLock);
    }

    public RoundRobin(final List<T> list, final RoundRobinAccumulate accumulate, boolean reentrantLock) {
        if (reentrantLock)
            this.lock = new ReentrantLock();
        this.accumulate = accumulate;
        this.addAll(list);
    }

    /**
     * 增加一筆數據
     *
     * @param t
     */
    public void add(T t) {
        doAction((args) -> {
            this.list.add(t);
            int size = this.list.size();
            logger.debug("[add]index:", size - 1, ",t:", t, ",total:", size, ",next:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
            return null;
        });
    }

    /**
     * 增加一個list
     *
     * @param list
     */
    public void addAll(final List<T> list) {
        doAction((args) -> {
            if (CollectionUtils.isNotEmpty(list)) {
                for (T t : list) {
                    this.list.add(t);
                    int size = this.list.size();
                    logger.debug("[add]index:", size - 1, ",t:", t, ",total:", size, ",next:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
                }
            }
            return null;
        });
    }

    /**
     * 設定ReentrantLock
     *
     * @param lock
     */
    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    /**
     * 設定遞增還是遞減
     *
     * @param accumulate
     */
    public void setAccumulate(RoundRobinAccumulate accumulate) {
        this.accumulate = accumulate;
    }

    /**
     * 輪詢取出下一筆數據
     *
     * @return
     */
    public T select() {
        return doAction((args) -> {
            if (this.list.isEmpty())
                return null;
            int size = this.list.size();
            T t = null;
            logger.debug("[select before]total:", size, ",previous:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
            try {
                if (accumulate == RoundRobinAccumulate.Increment)
                    t = this.list.get(this.next.getAndIncrement() % size);
                else
                    t = this.list.get(this.next.getAndDecrement() % size);
            } catch (Exception e) {
                logger.warn(e, e.getMessage());
                t = this.list.get(new SecureRandom().nextInt(size));
            } finally {
                // 遞增有完成一次輪詢
                if (this.next.get() >= size) {
                    this.next.set(0);
                    this.roundRobinAll.set(true);
                }
                // 遞減有完成一次輪詢
                else if (next.get() <= -1) {
                    this.next.set(size - 1);
                    this.roundRobinAll.set(true);
                } else {
                    this.roundRobinAll.set(false);
                }
                logger.debug("[select after]total:", size, ",current:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
                logger.debug("[select after]t:", t);
            }
            return t;
        });
    }

    /**
     * 返回是否有完成一次輪詢
     *
     * @return
     */
    public boolean isRoundRobinAll() {
        return this.roundRobinAll.get();
    }

    /**
     * 重置
     */
    public void reset() {
        doAction((args) -> {
            int size = this.list.size();
            if (this.accumulate == RoundRobinAccumulate.Increment) {
                this.next.set(0);
            } else {
                this.next.set(size - 1);
            }
            this.roundRobinAll.set(false);
            logger.debug("[reset]total:", size, ",next:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
            return null;
        });
    }

    /**
     * 取出當前的值
     *
     * @return
     */
    public T get() {
        return doAction((args) -> this.list.isEmpty() ? null : this.list.get(this.next.get()));
    }

    /**
     * 取出指定index的值
     *
     * @param index
     * @return
     */
    public T get(int index) {
        return doAction((args) -> this.list.isEmpty() ? null : this.list.get(index));
    }

    /**
     * 取得size
     *
     * @return
     */
    public int size() {
        return doAction((args) -> this.list.size());
    }

    /**
     * 獲取List
     *
     * @return
     */
    public List<T> getList() {
        return doAction((args) -> new ArrayList<>(this.list));
    }

    /**
     * 查找符合條件的數據
     *
     * @param predicate
     * @return
     */
    public T found(Predicate<T> predicate) {
        return doAction((args) -> this.list.isEmpty() ? null : this.list.stream().filter(predicate).findFirst().orElse(null));
    }

    /**
     * 重置
     */
    public void clear() {
        doAction((args) -> {
            this.list.clear();
            this.next.set(0);
            this.roundRobinAll.set(false);
            logger.debug("[clear]total:", this.list.size(), ",next:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
            return null;
        });
    }

    /**
     * 改變一筆數據
     *
     * @param t
     */
    public void set(int index, T t) {
        doAction(args -> {
            this.list.set(index, t);
            logger.debug("[set]index:", index, ",t:", t, ",total:", this.list.size(), ",next:", this.next.get(), ",roundRobinAll:", this.roundRobinAll.get());
            return null;
        });
    }

    private <R> R doAction(Function<?, R> function) {
        if (this.lock != null)
            this.lock.lock();
        try {
            return function.apply(null);
        } finally {
            if (this.lock != null)
                this.lock.unlock();
        }
    }
}
