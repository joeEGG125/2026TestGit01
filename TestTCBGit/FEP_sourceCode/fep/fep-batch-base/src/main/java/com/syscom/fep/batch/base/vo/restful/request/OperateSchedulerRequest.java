package com.syscom.fep.batch.base.vo.restful.request;

import com.syscom.fep.batch.base.vo.BatchOperateAction;
import com.syscom.fep.batch.base.vo.restful.BatchBaseRestful;
import com.syscom.fep.batch.base.vo.restful.BatchScheduler;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

@XStreamAlias("OperateSchedulerRequest")
public class OperateSchedulerRequest extends BatchBaseRestful {
    private static final long serialVersionUID = 1L;

    @XStreamAlias("BatchSchedulerReqs")
    private List<BatchScheduler> batchSchedulerList;
    @XStreamAlias("Action")
    private BatchOperateAction action;

    public List<BatchScheduler> getBatchSchedulerList() {
        return batchSchedulerList;
    }

    public void setBatchSchedulerList(List<BatchScheduler> batchSchedulerList) {
        this.batchSchedulerList = batchSchedulerList;
    }

    public BatchOperateAction getAction() {
        return action;
    }

    public void setAction(BatchOperateAction action) {
        this.action = action;
    }
}
