package com.syscom.fep.web.dialect.processor.impl;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.syscom.fep.web.dialect.processor.AbstractStandardExpressionAttributeTagTagProcessor;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;

public class DateTimeFormatExpressionAttributeTagProcessor extends AbstractStandardExpressionAttributeTagTagProcessor {
	private static final String ATTR_NAME = "date-time-format";

	public DateTimeFormatExpressionAttributeTagProcessor(String dialectPrefix) {
		super(dialectPrefix, ATTR_NAME);
	}

	@Override
	protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, Object expressionResult,
			IElementTagStructureHandler structureHandler) {
		String pattern = tag.getAttributeValue("date-time-pattern");
		if (StringUtils.isBlank(pattern)) {
			pattern = FormatUtil.FORMAT_DATE_YYYY_MM_DD_SLASH;
		}
		if (expressionResult != null) {
			String text = StringUtils.EMPTY;
			if (expressionResult instanceof Calendar) {
				text = FormatUtil.dateTimeFormat((Calendar) expressionResult, pattern);
			} else if (expressionResult instanceof Date) {
				Calendar cal = Calendar.getInstance();
				cal.setTime((Date) expressionResult);
				text = FormatUtil.dateTimeFormat(cal, pattern);
			} else if (expressionResult instanceof String && StringUtils.isNotBlank((String) expressionResult)) {
				String srcPattern = tag.getAttributeValue("date-time-pattern-src");
				String targetPattern = tag.getAttributeValue("date-time-pattern-target");
				if (StringUtils.isBlank(srcPattern)) {
					srcPattern = FormatUtil.FORMAT_DATE_YYYYMMDD_PLAIN;
				}
				if (StringUtils.isBlank(targetPattern)) {
					targetPattern = pattern;
				}
				try {
					Date date = FormatUtil.parseDataTime((String) expressionResult, srcPattern);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					text = FormatUtil.dateTimeFormat(cal, targetPattern);
				} catch (ParseException e) {
					text = (String) expressionResult;
					LogHelperFactory.getTraceLogger().exceptionMsg(e, e.getMessage());
				}
			} else if (expressionResult instanceof Number) {
				this.doProcess(context, tag, attributeName, attributeValue, String.valueOf(((Number) expressionResult).intValue()), structureHandler);
			} else {
				LogHelperFactory.getTraceLogger().error("Cannot process with ", expressionResult.getClass().getSimpleName());
			}
			structureHandler.setBody(text, false);
		}
	}
}
