package com.syscom.fep.web.form.batch;

import com.syscom.fep.batch.base.vo.BatchOperateAction;
import com.syscom.fep.web.form.BaseForm;

public class UI_000140_Form extends BaseForm {
	private static final long serialVersionUID = -3544751383177059125L;

	private String batchName;
	private String batchId;
	private BatchOperateAction action;
	private String batchExecuteHostName;

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public BatchOperateAction getAction() {
		return action;
	}

	public void setAction(BatchOperateAction action) {
		this.action = action;
	}

	public String getBatchExecuteHostName() {
		return batchExecuteHostName;
	}

	public void setBatchExecuteHostName(String batchExecuteHostName) {
		this.batchExecuteHostName = batchExecuteHostName;
	}
}
