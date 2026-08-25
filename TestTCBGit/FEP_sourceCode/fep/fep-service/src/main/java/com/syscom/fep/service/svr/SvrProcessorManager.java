package com.syscom.fep.service.svr;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class SvrProcessorManager {
    protected static final LogHelper SERVICELOGGER = LogHelperFactory.getServiceLogger();
    private final List<SvrProcessor> processorList = new ArrayList<>();

    void runAllProcessors() {
        SvrProcessor[] processors = null;
        synchronized (this.processorList) {
            processors = new SvrProcessor[this.processorList.size()];
            this.processorList.toArray(processors);
        }
        if (ArrayUtils.isNotEmpty(processors)) {
            for (SvrProcessor processor : processors) {
                try {
                    processor.start();
                } catch (Exception e) {
                    // 這裡只列印異常就好
                    SERVICELOGGER.exceptionMsg(e, processor.getName(), " start failed");
                }
            }
        }
    }

    void addProcessor(SvrProcessor processor) {
        synchronized (this.processorList) {
            this.processorList.add(processor);
        }
    }

    void removeProcessor(SvrProcessor processor) {
        synchronized (this.processorList) {
            this.processorList.remove(processor);
        }
    }
}
