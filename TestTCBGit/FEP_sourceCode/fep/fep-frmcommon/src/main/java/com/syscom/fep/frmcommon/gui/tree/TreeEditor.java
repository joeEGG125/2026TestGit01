package com.syscom.fep.frmcommon.gui.tree;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;

/**
 * 
 * @author Richard
 *
 */
public class TreeEditor extends DefaultTreeCellEditor implements FocusListener, KeyListener {
	protected JTextField editor;

	public TreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
		this(tree, renderer, true);
	}

	public TreeEditor(JTree tree, DefaultTreeCellRenderer renderer, boolean editable) {
		super(tree, renderer);
		editor = (JTextField) ((DefaultCellEditor) realEditor).getComponent();
		editor.setEditable(editable);
		editor.addFocusListener(this);
		editor.addKeyListener(this);
		editor.setBackground(renderer.getBackgroundSelectionColor());
		editor.setBorder(GuiUtil.createLineBorder(renderer.getBorderSelectionColor()));
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component component = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		return component;
	}

	protected void determineOffset(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		editingIcon = ((TreeRenderer) renderer).getIcon((DefaultMutableTreeNode) value, expanded, leaf);
		if (editingIcon != null) {
			offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
		} else {
			offset = renderer.getIconTextGap();
		}
	}

	public void focusGained(FocusEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editor.selectAll();
			}
		});
	}

	public void focusLost(FocusEvent e) {
		// if (!e.isTemporary()) {
		// this.stopCellEditing();
		// }
	}

	public void keyPressed(KeyEvent e) {}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
}
