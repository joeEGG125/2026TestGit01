package com.syscom.fep.frmcommon.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;

/**
 * @author Richard
 * @version 1.0
 */
public abstract class AbstractDialog extends JDialog {
	private static final long serialVersionUID = 6368683614176202106L;

	public AbstractDialog(Frame owner, String title) {
		this(owner, title, true, false);
	}

	public AbstractDialog(Frame owner, String title, boolean undecorated) {
		this(owner, title, true, undecorated);
	}

	public AbstractDialog(Frame owner, String title, boolean modal, boolean undecorated) {
		super(owner, title, modal);
		LayoutManager layout = null;
		Object constraints = null;
		if (!undecorated) {
			layout = new GridBagLayout();
			constraints = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, GuiUtil.createInsets(), 0, 0);
		} else {
			setUndecorated(true);
			getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			layout = new BorderLayout();
		}
		initComponents();
		JPanel contentPane = GuiUtil.createPanel(layout, true);
		contentPane.add(guiLayout(), constraints);
		setContentPane(contentPane);
	}

	public abstract void initComponents();

	public abstract Component guiLayout();
}
