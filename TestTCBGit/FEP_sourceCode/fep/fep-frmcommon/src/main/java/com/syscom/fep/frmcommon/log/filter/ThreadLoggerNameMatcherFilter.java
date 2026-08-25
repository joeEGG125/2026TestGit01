package com.syscom.fep.frmcommon.log.filter;

import org.apache.commons.lang3.ArrayUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ThreadLoggerNameMatcherFilter extends AbstractMatcherFilter<ILoggingEvent> {

	private String[] matchThreadName;
	private String[] mismatchLoggerName;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}
		if (ArrayUtils.isEmpty(this.matchThreadName)) {
			return FilterReply.NEUTRAL;
		}
		if (ArrayUtils.contains(this.matchThreadName, event.getThreadName())) {
			if (ArrayUtils.contains(this.mismatchLoggerName, event.getLoggerName())) {
				return onMismatch;
			}
			return onMatch;
		} else {
			return onMismatch;
		}
	}

	public void setMatchThreadName(String matchThreadName) {
		if (matchThreadName.contains(",")) {
			this.matchThreadName = matchThreadName.split(",");
		} else {
			this.matchThreadName = new String[] { matchThreadName };
		}
	}

	public void setMismatchLoggerName(String mismatchLoggerName) {
		if (mismatchLoggerName.contains(",")) {
			this.mismatchLoggerName = mismatchLoggerName.split(",");
		} else {
			this.mismatchLoggerName = new String[] { mismatchLoggerName };
		}
	}
}
