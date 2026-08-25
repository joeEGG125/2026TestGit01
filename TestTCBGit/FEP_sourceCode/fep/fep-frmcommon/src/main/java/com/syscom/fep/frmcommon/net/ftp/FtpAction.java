package com.syscom.fep.frmcommon.net.ftp;

public enum FtpAction {

	NO_ACTION("No action"),
	MAKE_DIRECTORY("Make directory"),
	CHANGE_DIRECTORY("Change directory");

	private String description;

	private FtpAction(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
