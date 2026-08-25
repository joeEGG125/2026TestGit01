package com.syscom.fep.frmcommon.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.MenuListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang3.ArrayUtils;

import com.syscom.fep.frmcommon.gui.GuiProperties;
import com.syscom.fep.frmcommon.gui.tree.TreeRenderer;

/**
 * @author Richard
 * @version 1.0
 */
public class GuiUtil {
	private GuiUtil() {}

	public static Border createLineBorder(Color clr) {
		return BorderFactory.createLineBorder(clr);
	}

	public static TitledBorder createTitleBorder(String title, Color clr) {
		return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(clr), title);
	}

	public static TitledBorder createTitleBorder(String title, Color lineClr, Color titleClr) {
		return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(lineClr), title, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, titleClr);
	}

	public static JPanel createPanel(LayoutManager layout) {
		return createPanel(layout, false);
	}

	public static JPanel createPanel(LayoutManager layout, boolean isOpaque) {
		JPanel panel = new JPanel((layout == null) ? new FlowLayout() : layout);
		panel.setOpaque(isOpaque);
		return panel;
	}

	public static JLabel createLabel(String text) {
		return createLabel(text, null);
	}

	public static JLabel createLabel(String text, Color fgClr) {
		return createLabel(text, false, fgClr);
	}

	public static JLabel createLabel(String text, boolean isOpaque, Color fgClr) {
		return createLabel(text, SwingConstants.LEADING, isOpaque, fgClr);
	}

	public static JLabel createLabel(String text, int alignment, Color fgClr) {
		return createLabel(text, alignment, false, fgClr);
	}

	public static JLabel createLabel(String text, int alignment, boolean isOpaque, Color fgClr) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(alignment);
		label.setOpaque(isOpaque);
		if (fgClr != null) {
			label.setForeground(fgClr);
		}
		return label;
	}

	public static JLabel createLabel(ImageIcon image) {
		JLabel label = new JLabel(image, SwingConstants.LEADING);
		label.setOpaque(true);
		return label;
	}

	public static JTextField createNumberTextField(String text) {
		return createNumberTextField(text, null);
	}

	public static JTextField createNumberTextField(String text, Color fgClr) {
		return createNumberTextField(text, SwingConstants.RIGHT, fgClr, -1);
	}

	public static JTextField createNumberTextField(String text, Color fgClr, int maxLength) {
		return createNumberTextField(text, SwingConstants.RIGHT, fgClr, maxLength);
	}

	public static JTextField createNumberTextField(String text, int alignment, Color fgClr, final int maxLength) {
		JTextField textField = createTextField(text, fgClr, alignment, maxLength);
		textField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				int c = e.getKeyChar();
				if (c != 46 && c != 8) {
					if ((c < 48 || c > 57)) {
						e.consume();
					}
				}
			}
		});
		return textField;
	}

	public static JTextField createTextField(String text) {
		return createTextField(text, null);
	}

	public static JTextField createTextField(String text, Color fgClr) {
		return createTextField(text, fgClr, SwingConstants.LEFT);
	}

	public static JTextField createTextField(String text, Color fgClr, int alignment) {
		return createTextField(text, fgClr, alignment, -1);
	}

	@SuppressWarnings("serial")
	public static JTextField createTextField(String text, Color fgClr, int alignment, final int maxLength) {
		final JTextField textField = new JTextField(text);
		if (fgClr != null) {
			textField.setForeground(fgClr);
		}
		textField.setOpaque(true);
		textField.setHorizontalAlignment(alignment);
		textField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textField.selectAll();
					}
				});
			}
		});
		if (maxLength != -1 && maxLength > 0) {
			textField.setDocument(new PlainDocument() {
				public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
					if (maxLength == -1) {
						super.insertString(offset, string, attributeSet);
						return;
					}
					String oldString = getText(0, getLength());
					if (string.length() + oldString.length() > maxLength) {
						string = string.substring(0, maxLength - oldString.length());
					}
					if (oldString.length() >= maxLength) {
						super.replace(offset, string.length(), string, attributeSet);
					} else if (oldString.length() < maxLength) {
						super.insertString(offset, string, attributeSet);
					}
				}
			});
			textField.setPreferredSize(new Dimension(maxLength * 12, textField.getPreferredSize().height));
			textField.setMinimumSize(new Dimension(maxLength * 12, textField.getPreferredSize().height));
		} else {
			textField.setPreferredSize(new Dimension(8 * 12, textField.getPreferredSize().height));
			textField.setMinimumSize(new Dimension(8 * 12, textField.getPreferredSize().height));
		}
		textField.setText(text);
		return textField;
	}

	public static JPasswordField createPasswordField(String text) {
		return createPasswordField(text, null);
	}

	public static JPasswordField createPasswordField(String text, Color fgClr) {
		return createPasswordField(text, fgClr, SwingConstants.LEFT);
	}

	public static JPasswordField createPasswordField(String text, Color fgClr, int alignment) {
		return createPasswordField(text, fgClr, alignment, -1);
	}

	@SuppressWarnings("serial")
	public static JPasswordField createPasswordField(String text, Color fgClr, int alignment, final int maxLength) {
		final JPasswordField pwdTfd = new JPasswordField(text);
		if (fgClr != null) {
			pwdTfd.setForeground(fgClr);
		}
		pwdTfd.setOpaque(true);
		pwdTfd.setHorizontalAlignment(alignment);
		pwdTfd.setEchoChar('*');
		pwdTfd.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						pwdTfd.selectAll();
					}
				});
			}
		});
		if (maxLength != -1 && maxLength > 0) {
			pwdTfd.setDocument(new PlainDocument() {
				public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
					if (maxLength == -1) {
						super.insertString(offset, string, attributeSet);
						return;
					}
					String oldString = getText(0, getLength());
					if (string.length() + oldString.length() > maxLength) {
						string = string.substring(0, maxLength - oldString.length());
					}
					if (oldString.length() >= maxLength) {
						super.replace(offset, string.length(), string, attributeSet);
					} else if (oldString.length() < maxLength) {
						super.insertString(offset, string, attributeSet);
					}
				}
			});
			pwdTfd.setPreferredSize(new Dimension(maxLength * 12, pwdTfd.getPreferredSize().height));
			pwdTfd.setMinimumSize(new Dimension(maxLength * 12, pwdTfd.getPreferredSize().height));
		} else {
			pwdTfd.setPreferredSize(new Dimension(8 * 12, pwdTfd.getPreferredSize().height));
			pwdTfd.setMinimumSize(new Dimension(8 * 12, pwdTfd.getPreferredSize().height));
		}
		return pwdTfd;
	}

	public static JTextArea createTextArea(String text) {
		return createTextArea(text, null);
	}

	public static JTextArea createTextArea(String text, Color fgClr) {
		return createTextArea(text, fgClr, true);
	}

	public static JTextArea createTextArea(String text, Color fgClr, boolean lineWrap) {
		final JTextArea textArea = new JTextArea(text);
		if (fgClr != null) {
			textArea.setForeground(fgClr);
		}
		textArea.setLineWrap(lineWrap);
		textArea.setOpaque(true);
		textArea.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textArea.selectAll();
					}
				});
			}
		});
		return textArea;
	}

	public static JCheckBox createCheckBox(String text, ActionListener listener) {
		return createCheckBox(text, null, listener);
	}

	public static JCheckBox createCheckBox(String text) {
		return createCheckBox(text, (Color) null);
	}

	public static JCheckBox createCheckBox(String text, Color fgClr) {
		return createCheckBox(text, fgClr, null);
	}

	public static JCheckBox createCheckBox(String text, Color fgClr, ActionListener listener) {
		JCheckBox checkBox = new JCheckBox(text);
		checkBox.setOpaque(false);
		if (fgClr != null) {
			checkBox.setForeground(fgClr);
		}
		checkBox.addActionListener(listener);
		return checkBox;
	}

	public static JRadioButton createRadio(String text, boolean isSelected, ActionListener listener) {
		JRadioButton radioBtn = createRadio(text, isSelected, (Color) null);
		radioBtn.addActionListener(listener);
		return radioBtn;
	}

	public static JRadioButton createRadio(String text, boolean isSelected) {
		return createRadio(text, isSelected, (Color) null);
	}

	public static JRadioButton createRadio(String text, boolean isSelected, Color fgClr) {
		JRadioButton radio = new JRadioButton(text);
		radio.setSelected(isSelected);
		radio.setOpaque(false);
		if (fgClr != null) {
			radio.setForeground(fgClr);
		}
		return radio;
	}

	public static JButton createButton(String text, ActionListener listener) {
		return createButton(text, listener, null);
	}

	public static JButton createButton(String text, ActionListener listener, Color fgClr) {
		JButton btn = new JButton(text);
		if (fgClr != null) {
			btn.setForeground(fgClr);
		}
		btn.addActionListener(listener);
		return btn;
	}

	public static JSpinner createNumberSpinner(int value, int minimum, int maximum, int setpSize) {
		return createNumberSpinner(null, value, minimum, maximum, setpSize);
	}

	public static JSpinner createNumberSpinner(Color fgClr, int value, int minimum, int maximum, int setpSize) {
		final int limitLength = String.valueOf(maximum).length();
		SpinnerNumberModel numberModel = new SpinnerNumberModel(value, minimum, maximum, setpSize);
		final JSpinner spinner = new JSpinner(numberModel);
		final JFormattedTextField editor = ((NumberEditor) spinner.getEditor()).getTextField();
		if (fgClr != null) {
			editor.setForeground(fgClr);
		}
		editor.setPreferredSize(new Dimension(60, 20));
		editor.setHorizontalAlignment(SwingConstants.CENTER);
		editor.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				int c = e.getKeyChar();
				if (c != 46 && c != 8) {
					if ((c < 48 || c > 57)) {
						e.consume();
						return;
					}
				}
				if (editor.getText().trim().length() == limitLength) {
					String selText = editor.getSelectedText();
					if (selText != null && selText.length() == limitLength) {
						return;
					}
					e.consume();
				}
			}

			public void keyPressed(KeyEvent e) {
				int c = e.getKeyCode();
				if (c == KeyEvent.VK_UP || c == KeyEvent.VK_DOWN) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							editor.selectAll();
						}
					});
				}
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.selectAll();
					}
				});
			}
		});
		((AbstractButton) spinner.getComponent(0)).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.selectAll();
					}
				});
			}
		});
		((AbstractButton) spinner.getComponent(1)).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						editor.selectAll();
					}
				});
			}
		});
		return spinner;
	}

	@SuppressWarnings("serial")
	public static PlainDocument createMaxLengthLimitDocument(int maxLength) {
		final int max = maxLength;
		return new PlainDocument() {
			public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
				if (max == -1) {
					super.insertString(offset, string, attributeSet);
					return;
				}
				String oldString = getText(0, getLength());
				if (string.length() + oldString.length() > max) {
					string = string.substring(0, max - oldString.length());
				}
				if (oldString.length() >= max) {
					super.replace(offset, string.length(), string, attributeSet);
				} else if (oldString.length() < max) {
					super.insertString(offset, string, attributeSet);
				}
			}
		};
	}

	public static boolean showConfirmMessage(Component parentComponent, String title, String pattern, Object... arguments) {
		return JOptionPane.YES_OPTION == showOptionDialog(parentComponent, title, pattern, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, arguments);
	}

	public static void showErrorMessage(Component parentComponent, String title, String pattern, Object... arguments) {
		showOptionDialog(parentComponent, title, pattern, JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, arguments);
	}

	public static void showWarnMessage(Component parentComponent, String title, String pattern, Object... arguments) {
		showOptionDialog(parentComponent, title, pattern, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, arguments);
	}

	public static void showMessage(Component parentComponent, String title, String pattern, Object... arguments) {
		showOptionDialog(parentComponent, title, pattern, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, arguments);
	}

	public static int showOptionDialog(Component parentComponent, String title, String pattern, int optionType, int messageType, Icon icon, Object... arguments) {
		String message;
		if (ArrayUtils.isEmpty(arguments)) {
			message = pattern;
		} else {
//			message = MessageFormat.format(pattern, arguments); 【Race Condition Format Flaw】
			message = FormatUtil.messageFormat(pattern, arguments);
		}
		// optionPane
		JOptionPane optionPane = new JOptionPane(message, messageType, optionType, icon, null, null);
		optionPane.setInitialValue(null);
		optionPane.setComponentOrientation(((parentComponent == null) ? JOptionPane.getRootFrame() : parentComponent).getComponentOrientation());
		optionPane.selectInitialValue();
		// dialog
		JDialog dialog = optionPane.createDialog(parentComponent, title);
		dialog.setIconImage(ImageUtil.obtainImage("images/favicon.png"));
		dialog.setVisible(true); 
		dialog.dispose();
		Object selectedValue = optionPane.getValue();
		if (selectedValue == null) {
			return JOptionPane.CLOSED_OPTION;
		}
		if (selectedValue instanceof Integer) {
			return ((Integer) selectedValue).intValue();
		}
		return JOptionPane.CLOSED_OPTION;
	}

	public static Color parseRGBColor(String color) {
		if (color == null) {
			return null;
		}
		String[] s = color.split(",");
		if (s == null || s.length <= 0) {
			return null;
		}
		int r = Integer.parseInt(s[0].trim());
		int g = Integer.parseInt(s[1].trim());
		int b = Integer.parseInt(s[2].trim());
		if (s.length == 4) {
			int a = Integer.parseInt(s[3].trim());
			return new Color(r, g, b, a);
		}
		return new Color(r, g, b);
	}

	public static JMenu createJMenu(String text, char mnemonic) {
		return createJMenu(text, mnemonic, null);
	}

	public static JMenu createJMenu(String text, char mnemonic, MenuListener listener) {
		JMenu menu = new JMenu(text);
		menu.setMnemonic(mnemonic);
		if (listener != null) {
			menu.addMenuListener(listener);
		}
		return menu;
	}

	public static JMenuItem createJMenuItem(String text, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(text);
		if (listener != null) {
			menuItem.addActionListener(listener);
		}
		return menuItem;
	}

	public static JMenuItem createJMenuItem(String text, char mnemonic, KeyStroke keyStroke, ActionListener listener) {
		return createJMenuItem(text, null, mnemonic, keyStroke, listener);
	}

	public static JMenuItem createJMenuItem(String text, ImageIcon icon, char mnemonic, KeyStroke keyStroke, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.setMnemonic(mnemonic);
		menuItem.setAccelerator(keyStroke);
		if (listener != null) {
			menuItem.addActionListener(listener);
		}
		if (icon != null) {
			menuItem.setIcon(icon);
		}
		return menuItem;
	}

	public static JMenuItem createJCheckBoxMenuItem(String text, char mnemonic, KeyStroke keyStroke, ActionListener listener) {
		JMenuItem menuItem = new JCheckBoxMenuItem(text);
		menuItem.setMnemonic(mnemonic);
		menuItem.setAccelerator(keyStroke);
		if (listener != null) {
			menuItem.addActionListener(listener);
		}
		return menuItem;
	}

	public static void autoFitTableColumns(JTable jtable) {
		autoFitTableColumns(jtable, true);
	}

	public static void autoFitTableColumns(JTable jtable, boolean includeRow) {
		autoFitTableColumns(jtable, includeRow, -1);
	}

	public static void autoFitTableColumns(JTable jtable, int rowCountLimited) {
		autoFitTableColumns(jtable, true, rowCountLimited);
	}

	@SuppressWarnings("rawtypes")
	public static void autoFitTableColumns(JTable jtable, boolean includeRow, int rowCountLimited) {
		JTableHeader header = jtable.getTableHeader();
		int rowCount = jtable.getRowCount(), columnIndex, width, preferedWidth;
		Enumeration columns = jtable.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			columnIndex = header.getColumnModel().getColumnIndex(column.getIdentifier());
			width = (int) jtable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(jtable, column.getIdentifier(), false, false, -1, columnIndex).getPreferredSize().getWidth();
			if (includeRow && (rowCountLimited == -1 || rowCountLimited != -1 && rowCount < rowCountLimited)) {
				for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
					preferedWidth = (int) jtable.getCellRenderer(rowIndex, columnIndex).getTableCellRendererComponent(jtable, jtable.getValueAt(rowIndex, columnIndex), false, false, rowIndex, columnIndex).getPreferredSize().getWidth();
					width = Math.max(width, preferedWidth);
				}
			}
			header.setResizingColumn(column); // 此行很重要
			column.setWidth(width + jtable.getIntercellSpacing().width + 3);
		}
	}

	public static int calculateTableColumnWidth(JTable jtable, int columnIndex) {
		int width = 0;
		int rowCount = jtable.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			TableCellRenderer renderer = jtable.getCellRenderer(i, columnIndex);
			Component comp = renderer.getTableCellRendererComponent(jtable, jtable.getValueAt(i, columnIndex), false, false, i, columnIndex);
			int thisWidth = comp.getPreferredSize().width;
			if (thisWidth > width) {
				width = thisWidth;
			}
		}
		return width;
	}

	public static void setTableColumnWidths(JTable table, Insets insets, boolean setMinimum, boolean setMaximum) {
		int columnCount = table.getColumnCount();
		TableColumnModel tcm = table.getColumnModel();
		int spare = (insets == null ? 0 : insets.left + insets.right);
		for (int i = 0; i < columnCount; i++) {
			int width = calculateTableColumnWidth(table, i);
			width += spare;
			TableColumn column = tcm.getColumn(i);
			column.setPreferredWidth(width);
			if (setMinimum == true) {
				column.setMinWidth(width);
			}
			if (setMaximum == true) {
				column.setMaxWidth(width);
			}
		}
	}

	@SuppressWarnings("serial")
	public static DefaultMutableTreeNode createDefaultMutableTreeNode(Object userObj, final String nodename) {
		return new DefaultMutableTreeNode(userObj) {
			public String toString() {
				return nodename;
			}
		};
	}

	public static JTree createTree(DefaultMutableTreeNode root) {
		return createTree(root, new TreeRenderer());
	}

	public static JTree createTree(DefaultMutableTreeNode root, TreeCellRenderer renderer) {
		return createTree(root, renderer, null);
	}

	public static JTree createTree(DefaultMutableTreeNode root, TreeCellRenderer renderer, TreeCellEditor editor) {
		return createTree(root, renderer, editor, TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	public static JTree createTree(DefaultMutableTreeNode root, TreeCellRenderer renderer, TreeCellEditor editor, int treeSelectionModel) {
		@SuppressWarnings("serial")
		JTree tree = new JTree(root) {
			protected void setExpandedState(TreePath path, boolean state) {
				// 如果是root, 則禁止collapse
				Object obj = path.getLastPathComponent();
				if ((obj != null) && (obj instanceof DefaultMutableTreeNode)) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
					if (node.isRoot()) {
						super.setExpandedState(path, true);
					} else {
						super.setExpandedState(path, state);
					}
				}
			}
		};
		// renderer
		if (renderer == null) {
			renderer = new TreeRenderer();
		}
		tree.setCellRenderer(renderer);
		// editor
		if (editor != null) {
			tree.setCellEditor(editor);
			tree.setEditable(true);
		}
		tree.setShowsRootHandles(true);
		tree.setRootVisible(true);
		tree.setAutoscrolls(true);
		tree.setScrollsOnExpand(true);
		tree.getSelectionModel().setSelectionMode(treeSelectionModel);
		return tree;
	}

	public static void setPreferredHeight(Component component, int height) {
		component.setPreferredSize(new Dimension((int) component.getPreferredSize().getWidth(), height));
	}

	public static void setCursor(Component component, int cursorType) {
		component.setCursor(Cursor.getPredefinedCursor(cursorType));
	}

	public static Insets createInsets() {
		return createInsets(GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP, GuiProperties.GAP);
	}

	public static Insets createInsets(int top, int left, int bottom, int right) {
		return new Insets(top, left, bottom, right);
	}
}
