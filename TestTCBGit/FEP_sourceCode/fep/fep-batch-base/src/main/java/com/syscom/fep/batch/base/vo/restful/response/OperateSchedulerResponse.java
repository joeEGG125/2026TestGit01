package com.syscom.fep.batch.base.vo.restful.response;

import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import org.quartz.Trigger.TriggerState;

import com.syscom.fep.batch.base.vo.restful.BatchBaseRestful;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

@XStreamAlias("OperateSchedulerResponse")
public class OperateSchedulerResponse extends BatchBaseRestful {
    @XStreamAlias("BatchSchedulerResps")
    private List<BatchScheduler> batchSchedulerList;

    public List<BatchScheduler> getBatchSchedulerList() {
        return batchSchedulerList;
    }

    public void setBatchSchedulerList(List<BatchScheduler> batchSchedulerList) {
        this.batchSchedulerList = batchSchedulerList;
    }
}
