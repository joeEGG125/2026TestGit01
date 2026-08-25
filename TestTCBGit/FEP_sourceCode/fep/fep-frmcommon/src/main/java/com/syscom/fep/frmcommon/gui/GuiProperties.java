package com.syscom.fep.frmcommon.gui;

import java.awt.*;
import javax.swing.border.*;

import com.syscom.fep.frmcommon.gui.util.GuiUtil;

/**
 * 
 * @author Richard
 *
 */
public interface GuiProperties {
	public static final int GAP = 5;
	public static final int MENU_HEIGHT = 20;
	public static final int TABLE_ROW_HEIGHT = 20;
	public static final int TABLE_HEADER_HEIGHT = 25;
	public static final int PREFERRED_HEIGHT = 23;

	public static final int SPLIT_DIVIDER_SIZE = 3;

	public static final Font FONT_DEFAULT = new Font(Font.DIALOG, Font.PLAIN | Font.TRUETYPE_FONT, 13);
	public static final Font FONT_TXT_PLAIN = new Font(Font.DIALOG, Font.PLAIN | Font.TRUETYPE_FONT, 12);
	public static final Font FONT_TXT_BOLD = new Font(Font.DIALOG, Font.BOLD | Font.TRUETYPE_FONT, 12);

	public static final Color CLR_BORDER = new Color(100, 100, 100);
	public static final Color CLR_BORDER_TITLE = new Color(0, 112, 192);
	public static final Color CLR_FOREGROUND = Color.BLACK;
	public static final Color CLR_BACKGROUND_PANEL = new Color(52, 55, 59);
	public static final Color CLR_BACKGROUND_PANEL_BUTTON = new Color(153, 153, 153);
	public static final Color CLR_BACKGROUND_PANEL_MESSAGE = new Color(52, 55, 59);

	public static final Color CLR_FOREGROUND_WARNNING = new Color(194, 156, 7);
	public static final Color CLR_FOREGROUND_ERROR = new Color(255, 128, 128);

	public static final Color CLR_SELECTION_BACKGROUND = Color.BLUE;
	public static final Color CLR_SELECTION_FOREGROUND = new Color(255, 220, 120);

	public static final Color CLR_TABLE_BACKGROUND = new Color(243, 235, 124);
	public static final Color CLR_TABLE_SELECTION_BACKGROUND = Color.BLUE;
	public static final Color CLR_TABLE_SINGLE_ROW_BACKGROUND = new Color(64, 64, 64);
	public static final Color CLR_TABLE_DOUBLE_ROW_BACKGROUND = new Color(96, 96, 96);
	public static final Color CLR_TABLE_GRID_LINE = Color.WHITE;

	public static final Border BORDER_EMPTY = new EmptyBorder(1, 1, 1, 1);
	public static final Border BORDER_TABLE_FOCUS = GuiUtil.createLineBorder(new Color(255, 200, 0));

}
