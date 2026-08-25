package com.syscom.fep.frmcommon.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.util.EventListener;

import javax.swing.ImageIcon;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.gui.util.ImageUtil;

/**
 * @author Richard
 * @version 1.0
 */
public class LoadingDialog extends AbstractDialog {
	private static final long serialVersionUID = 8174341720160087177L;

	private static final ImageIcon LOADING_ICON = ImageUtil.obtainImageIcon("images/loading.gif");

	private static LoadingDialog instance = null;
	private boolean result = true;

	private LoadingDialog(Frame owner) {
		super(owner, null, true);
		pack();
	}

	public static boolean showLoadDialog(Frame owner, LoadingActionListener listenter) {
		if (instance == null) {
			instance = new LoadingDialog(owner);
		}
		synchronized (instance) {
			return instance.load(listenter);
		}
	}

	private boolean load(LoadingActionListener listenter) {
		result = true;
		ActionThread thread = new ActionThread(listenter);
		thread.start();

		Window owner = this.getOwner();
		Point point = owner.getLocation();
		setLocation(point.x + owner.getWidth() / 2 - getWidth() / 2, point.y + owner.getHeight() / 2 - getHeight() / 2);
		instance.setVisible(true);

		return result;
	}

	public void initComponents() {}

	public Component guiLayout() {
		return GuiUtil.createLabel(LOADING_ICON);
	}

	private class ActionThread extends Thread {
		private LoadingActionListener listenter;

		public ActionThread(LoadingActionListener listenter) {
			this.listenter = listenter;
		}

		public void run() {
			if (listenter != null) {
				try {
					listenter.actionPerformed();
				} catch (Exception ex) {
					listenter.handleException(ex);
				}
			}
			while (!instance.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			instance.setVisible(false);
		}
	}

	public static interface LoadingActionListener extends EventListener {

		public void actionPerformed() throws Exception;

		public void handleException(Exception ex);

	}
}
