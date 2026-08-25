package com.syscom.fep.frmcommon.ref;

import java.io.Serializable;
import java.util.function.Function;

public class RefBase<T> implements Serializable {
    private static final long serialVersionUID = 5640876583765580576L;

    protected T value;

    public RefBase(T value) {
        this.set(value);
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    /**
     * 2025-03-10 Richard add for [Cleartext Submission of Sensitive Information]
     *
     * @param ref
     * @param t
     * @param function
     * @param <T>
     * @param <R>
     */
    public static <T, R> void set(RefBase<R> ref, T t, Function<? super T, ? extends R> function) {
        ref.set(function.apply(t));
    }
}
