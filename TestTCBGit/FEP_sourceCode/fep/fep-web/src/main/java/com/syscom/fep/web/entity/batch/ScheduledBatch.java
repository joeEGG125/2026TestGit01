package com.syscom.fep.web.entity.batch;

import org.quartz.Trigger.TriggerState;
import org.springframework.beans.BeanUtils;

import com.syscom.fep.mybatis.model.Batch;

public class ScheduledBatch extends Batch {
	private static final long serialVersionUID = 1L;

	private boolean operability = true;
	private TriggerState triggerState = TriggerState.NONE;
	private String cronExpression;

	public ScheduledBatch(Batch batch) {
		if (batch == null)
			return;
		BeanUtils.copyProperties(batch, this);
	}

	public boolean isOperability() {
		return operability;
	}

	public void setOperability(boolean operability) {
		this.operability = operability;
	}

	public TriggerState getTriggerState() {
		return triggerState;
	}

	public void setTriggerState(TriggerState triggerState) {
		this.triggerState = triggerState;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

}
