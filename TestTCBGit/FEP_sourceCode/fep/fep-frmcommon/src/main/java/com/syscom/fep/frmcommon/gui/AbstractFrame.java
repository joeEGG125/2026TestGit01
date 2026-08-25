package com.syscom.fep.frmcommon.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;
import com.syscom.fep.frmcommon.gui.util.ImageUtil;

/**
 * 
 * @author Richard
 *
 */
public abstract class AbstractFrame extends JFrame {
	private static final long serialVersionUID = -8268645533979329948L;

	static {
		System.setProperty("java.awt.headless", "false");
	}

	public AbstractFrame(String title) {
		initLookAndFeel();
		initComponent();
		Component comp = guiLayout();
		if (comp != null) {
			JPanel contentPane = GuiUtil.createPanel(new GridBagLayout(), true);
			contentPane.add(comp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, GuiUtil.createInsets(), 0, 0));
			getContentPane().add(contentPane, BorderLayout.CENTER);
		}
		JMenuBar menuBar = createMenuBar();
		if (menuBar != null) {
			setJMenuBar(menuBar);
		}
		setTitle(title);
		setSize(getDimension());
		setIconImage(ImageUtil.obtainImage("images/favicon.png"));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public void showFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				Dimension size = getSize();
				if (size.height > screenSize.height) {
					size.height = screenSize.height;
				}
				if (size.width > screenSize.width) {
					size.width = screenSize.width;
				}
				setLocation((screenSize.width - size.width) / 2, (screenSize.height - size.height) / 2);
				setVisible(true);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {}
		// FontUIResource
		FontUIResource font = new FontUIResource(GuiProperties.FONT_DEFAULT);
		Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, font);
			}
		}
	}

	private void initComponent() {
		JDialog.setDefaultLookAndFeelDecorated(true);
	}

	protected Dimension getDimension() {
		return new Dimension(1000, 500);
	}

	protected abstract Component guiLayout();

	protected abstract JMenuBar createMenuBar();
}
