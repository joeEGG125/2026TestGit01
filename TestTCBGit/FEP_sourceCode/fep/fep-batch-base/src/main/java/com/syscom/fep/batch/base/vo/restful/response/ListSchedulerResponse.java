package com.syscom.fep.batch.base.vo.restful.response;

import com.syscom.fep.batch.base.vo.restful.BatchBaseRestful;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.quartz.Trigger.TriggerState;

import java.util.Date;
import java.util.List;

@XStreamAlias("ListSchedulerResponse")
public class ListSchedulerResponse extends BatchBaseRestful {
    @XStreamAlias("BatchSchedulerResps")
    private List<BatchScheduler> batchSchedulerList;

    public List<BatchScheduler> getBatchSchedulerList() {
        return batchSchedulerList;
    }

    public void setBatchSchedulerList(List<BatchScheduler> batchSchedulerList) {
        this.batchSchedulerList = batchSchedulerList;
    }
}
