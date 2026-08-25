package com.syscom.fep.frmcommon.gui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

@SuppressWarnings("rawtypes")
public abstract class AdvCombobox extends JComboBox {
	private static final long serialVersionUID = -8259806280658689756L;

	private JTextComponent editor;

	private ArrayList<ComboItem> comboItemList = new ArrayList<ComboItem>();
	private boolean nullAllowed;

	public AdvCombobox() {
		this(false);
	}

	public AdvCombobox(boolean nullAllowed) {
		this.nullAllowed = nullAllowed;
		initComponents();
		reload(false);
	}

	public abstract String getCaption();

	public abstract List<ComboItem> getComboItemList();

	@SuppressWarnings("serial")
	private void initComponents() {
		// this
		setEditor(new BasicComboBoxEditor.UIResource() {
			protected JTextField createEditorComponent() {
				JTextField editor = super.createEditorComponent();
				Border border = (Border) UIManager.get("ComboBox.editorBorder");
				if (border != null) {
					editor.setBorder(border);
				}
				editor.setOpaque(false);
				return editor;
			}
		});
		setEditable(true);
		addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				selectAll();
			}

			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		// editor
		editor = (JTextComponent) getEditor().getEditorComponent();
		editor.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
					showPopupMenu();
				}
			}
		});
		editor.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showPopupMenu();
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll();
			}
		});
		editor.setDocument(new PlainDocument() {
			private boolean selecting = false;

			public void remove(int offs, int len) throws BadLocationException {
				if (selecting) {
					return;
				}
				super.remove(offs, len);
				selectItem();
			}

			public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
				if (selecting) {
					return;
				}
				super.insertString(offs, str, a);
				selectItem();
			}

			private void selectItem() throws BadLocationException {
				selecting = true;
				int selectedIndex = selection();
				if (selectedIndex != -1) {
					setSelectedIndex(selectedIndex);
				}
				selecting = false;
			}

			private int selection() throws BadLocationException {
				String pattern = getText(0, getLength()).trim();
				if (!pattern.equalsIgnoreCase("")) {
					for (int i = 0; i < comboItemList.size(); i++) {
						if (comboItemList.get(i) != null && comboItemList.get(i).k != null && comboItemList.get(i).k.toLowerCase().contains(pattern)) {
							return i;
						}
					}
				}
				return -1;
			}
		});
	}

	public void showPopupMenu() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showPopup();
			}
		});
	}

	public void selectAll() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				editor.selectAll();
			}
		});
	}

	public void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		if (index == -1) {
			editor.setText("");
		} else {
			editor.setText(comboItemList.get(index).k);
		}
	}

	public final void removeAllItems() {
		comboItemList.clear();
		super.removeAllItems();
	}

	public final void processKeyEvent(KeyEvent e) {
		if (comboItemList != null && e.getID() == KeyEvent.KEY_TYPED) {
			for (int i = 0; i < comboItemList.size(); i++) {
				if (comboItemList.get(i).shortcut == e.getKeyChar()) {
					setSelectedIndex(i);
					return;
				}
			}
		}
		super.processKeyEvent(e);
	}

	public final JTextComponent getTextEditor() {
		return editor;
	}

	public final Object getData() {
		int index = getSelectedIndex();
		if (index == -1 || index >= comboItemList.size()) {
			return getEditor().getItem();
		} else {
			return comboItemList.get(index).v;
		}
	}

	public final void setSelected(Object value) {
		setSelectedIndex(searchIndex(value, false));
	}

	public final void setSelected(String key) {
		setSelectedIndex(searchIndex(key, true));
	}

	private int searchIndex(Object obj, boolean isKey) {
		if (obj == null) {
			return -1;
		}
		if (isKey) {
			for (int i = 0; i < comboItemList.size(); i++) {
				if (obj.equals(comboItemList.get(i).k)) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < comboItemList.size(); i++) {
				if (obj.equals(comboItemList.get(i).v)) {
					return i;
				}
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	public void reload(boolean isKeepLastSelectedIndex) {
		int lastSelectedIndex = 0;
		if (isKeepLastSelectedIndex && comboItemList.size() > 0) {
			lastSelectedIndex = getSelectedIndex();
		}
		removeAllItems();
		List<ComboItem> list = getComboItemList();
		if (list != null) {
			comboItemList.addAll(list);
			if (nullAllowed) {
				comboItemList.add(0, new ComboItem("", null));
			}
			for (ComboItem keyValuePairItem : comboItemList) {
				super.addItem(keyValuePairItem.k);
			}
		}
		int itemCount = getItemCount();
		if (lastSelectedIndex >= itemCount) {
			lastSelectedIndex = 0;
		}
		setSelectedIndex(lastSelectedIndex);
	}

	public static class ComboItem {
		public String k;
		public Object v;
		public char shortcut;

		public ComboItem(String k, Object v) {
			this(k, v, (char) 0);
		}

		public ComboItem(String k, Object v, char shortcut) {
			this.k = k;
			this.v = v;
			this.shortcut = shortcut;
		}
	}
}
