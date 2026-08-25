package com.syscom.fep.frmcommon.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.log.LogHelper;

/**
 * 
 * @author Richard
 *
 */
public abstract class AbstractPanel extends JPanel {
	private static final long serialVersionUID = -3823772299010262284L;

	private LogHelper logger = new LogHelper();

	private JLabel messageLbl;

	public AbstractPanel() {
		this(true);
	}

	public AbstractPanel(boolean showMessageBar) {
		initComponents();
		initLayout(showMessageBar);
	}

	protected abstract void initComponents();

	protected abstract Component guiLayout();

	private void initLayout(boolean showMessageBar) {
		this.setLayout(new GridBagLayout());
		this.setOpaque(true);
		this.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));

		this.add(guiLayout(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, GuiUtil.createInsets(), 0, 0));

		if (showMessageBar) {
			JPanel messagePane = GuiUtil.createPanel(new GridBagLayout());
			messagePane.setBackground(GuiProperties.CLR_BACKGROUND_PANEL_MESSAGE);
			messagePane.setBorder(GuiUtil.createLineBorder(GuiProperties.CLR_BORDER));
			messagePane.add(GuiUtil.createLabel("訊息"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, GuiUtil.createInsets(), 0, 0));
			messagePane.add(GuiUtil.createLabel(" : "), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, GuiUtil.createInsets(), 0, 0));

			messageLbl = GuiUtil.createLabel(StringUtils.EMPTY);
			messagePane.add(messageLbl, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, GuiUtil.createInsets(), 0, 0));
			this.add(messagePane, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, GuiUtil.createInsets(), 0, 0));
		}
	}

	private boolean checkMessageLbl() {
		return messageLbl != null;
	}

	protected void clearMessage() {
		if (checkMessageLbl()) {
			messageLbl.setText(StringUtils.EMPTY);
		}
	}

	protected void showMessage(Object... messages) {
		showMessage(Level.INFO, messages);
	}

	protected void showWarnMessage(Object... messages) {
		showMessage(Level.WARN, messages);
	}

	protected void showWarnMessage(Exception ex, Object... messages) {
		showExceptionMessage(Level.WARN, ex, messages);
	}

	protected void showErrorMessage(Object... messages) {
		showMessage(Level.ERROR, messages);
	}

	protected void showErrorMessage(Throwable t, Object... messages) {
		showExceptionMessage(Level.ERROR, t, messages);
	}

	private void showMessage(Level level, Object... messages) {
		if (checkMessageLbl()) {
			messageLbl.setForeground(obtainColor(level));
			messageLbl.setText(obtainMessages(level, messages));
		}
	}

	private void showExceptionMessage(Level level, Throwable t, Object... messages) {
		if (checkMessageLbl()) {
			messageLbl.setForeground(obtainColor(level));
			messageLbl.setText(obtainExceptionMessages(level, t, messages));
		}
	}

	private Color obtainColor(Level level) {
		switch (level) {
			case WARN:
				return GuiProperties.CLR_FOREGROUND_WARNNING;
			case ERROR:
				return GuiProperties.CLR_FOREGROUND_ERROR;
			default:
				return GuiProperties.CLR_FOREGROUND;
		}
	}

	private String obtainMessages(Level level, Object... messages) {
		switch (level) {
			case WARN:
				logger.warn(messages);
				break;
			case ERROR:
				logger.error(messages);
				break;
			case INFO:
				logger.info(messages);
				break;
			case DEBUG:
				logger.debug(messages);
				break;
			case TRACE:
				logger.trace(messages);
				break;
			default:
				logger.debug(messages);
				break;
		}
		return StringUtils.join(messages);
	}

	private String obtainExceptionMessages(Level level, Throwable t, Object... messages) {
		switch (level) {
			case ERROR:
				logger.exceptionMsg(t, messages);
				break;
			default:
				logger.warn(t, messages);
				break;
		}
		return StringUtils.join(messages);
	}
}
