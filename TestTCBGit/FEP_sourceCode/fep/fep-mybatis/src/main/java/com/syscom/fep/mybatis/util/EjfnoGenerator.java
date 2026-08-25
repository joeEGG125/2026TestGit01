package com.syscom.fep.mybatis.util;

import com.syscom.fep.mybatis.ext.mapper.EjfnoExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class EjfnoGenerator {
    // 2022-11-23 Richard modified
    // 改為從Sequence中獲取
    // @Autowired
    // private SpCaller spCaller;
    // private AtomicInteger currentEj = new AtomicInteger(0), nextStepEj = new AtomicInteger(0);

    @Autowired
    private EjfnoExtMapper mapper;

    /**
     * 呼叫存儲過程獲取EJNO
     *
     * @return
     * @author Richard
     */
    public synchronized int generate() {
        // 2022-11-23 Richard modified
        // 改為從Sequence中獲取
        // if (nextStepEj.get() == 0 || currentEj.get() == nextStepEj.get() - 1) {
        // 	Ejfno ejfno = spCaller.getEj();
        // 	currentEj.set(ejfno.getEjfno().intValue());
        // 	nextStepEj.set(currentEj.get() + ejfno.getInterval().intValue());
        // 	return currentEj.get();
        // }
        // return currentEj.incrementAndGet();
        return mapper.getEjfnoSequence();
    }
}
