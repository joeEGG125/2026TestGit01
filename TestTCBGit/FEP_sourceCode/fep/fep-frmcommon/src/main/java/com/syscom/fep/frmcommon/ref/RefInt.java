package com.syscom.fep.frmcommon.ref;

public class RefInt extends RefBase<Integer> {
	private static final long serialVersionUID = -8471488485394313834L;

	public RefInt() {
		this(0);
	}

	public RefInt(int value) {
		super(value);
	}

	public void accumulate(int value){
		this.value += value;
	}
}
