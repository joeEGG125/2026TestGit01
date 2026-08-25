package com.syscom.fep.frmcommon.gui.util;

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * @author Richard
 * @version 1.0
 */
public class IconHandlerUtil {

	private static final String FORDER = "folder";
	private static final String OPENED = "opened";
	private static final String FILE = "file";

	private static final String MENU_FILE_OPEN = "open";
	private static final String MENU_FILE_EXIT = "exit";
	private static final String MENU_EDIT_EXPORT = "export";
	private static final String MENU_EDIT_IMPORT = "import";

	private static final int IMAGE_WIDTH = 20;
	private static final int IMAGE_HEIGHT = 20;

	private static final HashMap<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();

	static {
		// for common
		iconMap.put(FORDER, ImageUtil.obtainImageIcon("images/folder.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		iconMap.put(OPENED, ImageUtil.obtainImageIcon("images/opened.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		iconMap.put(FILE, ImageUtil.obtainImageIcon("images/file.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		// for menu
		iconMap.put(MENU_FILE_OPEN, ImageUtil.obtainImageIcon("images/open.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		iconMap.put(MENU_FILE_EXIT, ImageUtil.obtainImageIcon("images/exit.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		iconMap.put(MENU_EDIT_EXPORT, ImageUtil.obtainImageIcon("images/export.png", IMAGE_WIDTH, IMAGE_HEIGHT));
		iconMap.put(MENU_EDIT_IMPORT, ImageUtil.obtainImageIcon("images/import.png", IMAGE_WIDTH, IMAGE_HEIGHT));
	}

	private IconHandlerUtil() {}

	public static ImageIcon getFolderIcon() {
		return iconMap.get(FORDER);
	}

	public static ImageIcon getOpenedIcon() {
		return iconMap.get(OPENED);
	}

	public static ImageIcon getFileIcon() {
		return iconMap.get(FILE);
	}

	public static ImageIcon getMenuFileOpenIcon() {
		return iconMap.get(MENU_FILE_OPEN);
	}

	public static ImageIcon getMenuFileExitIcon() {
		return iconMap.get(MENU_FILE_EXIT);
	}

	public static ImageIcon getMenuEditExportIcon() {
		return iconMap.get(MENU_EDIT_EXPORT);
	}

	public static ImageIcon getMenuEditImportIcon() {
		return iconMap.get(MENU_EDIT_IMPORT);
	}

	public static ImageIcon getImageIcon(String extension) {
		if (iconMap.containsKey(extension)) {
			return iconMap.get(extension);
		}
		return getFileIcon();
	}
}
