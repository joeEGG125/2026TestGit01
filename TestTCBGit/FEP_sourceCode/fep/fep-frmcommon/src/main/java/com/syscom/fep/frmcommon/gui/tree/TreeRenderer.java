package com.syscom.fep.frmcommon.gui.tree;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.util.IconHandlerUtil;

/**
 * 
 * @author Richard
 *
 */
public class TreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = -6349465833837162299L;
	private static final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		setIcon(getIcon(node, expanded, leaf));
		return this;
	}

	public Icon getIcon(DefaultMutableTreeNode node, boolean expanded, boolean leaf) {
		Object userObj = node.getUserObject();
		if (userObj != null) {
			if (userObj instanceof File) {
				return getFileSystemIcon(node, (File) userObj, expanded, leaf);
			}
			if (leaf) {
				return IconHandlerUtil.getImageIcon(StringUtils.join(".", FilenameUtils.getExtension(userObj.toString().toLowerCase())));
			}
		}
		if (!leaf) {
			return getParentNodeIcon(node, expanded);
		}
		return IconHandlerUtil.getFileIcon();
	}

	public Icon getFileSystemIcon(DefaultMutableTreeNode node, File file, boolean expanded, boolean leaf) {
		if (file != null) {
			return fileSystemView.getSystemIcon(file);
		}
		if (!leaf) {
			return getParentNodeIcon(node, expanded);
		}
		return IconHandlerUtil.getFileIcon();
	}

	public Icon getParentNodeIcon(DefaultMutableTreeNode node, boolean expanded) {
		return expanded ? IconHandlerUtil.getOpenedIcon() : IconHandlerUtil.getFolderIcon();
	}

	public Color getBackgroundSelectionColor() {
		return GuiProperties.CLR_SELECTION_BACKGROUND;
	}

	public Color getTextSelectionColor() {
		return GuiProperties.CLR_SELECTION_FOREGROUND;
	}
}
