package hms.ui.common;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared visual constants for HMS interfaces.
 */
public final class Theme {

    public static final Color PRIMARY_COLOR = new Color(31, 78, 121);
    public static final Color BACKGROUND_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(33, 33, 33);

    public static final Font DEFAULT_FONT =
            new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font HEADING_FONT =
            new Font(Font.SANS_SERIF, Font.BOLD, 20);

    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 750;
    public static final int SPACING_SMALL = 8;
    public static final int SPACING_MEDIUM = 16;
    public static final int SPACING_LARGE = 24;

    private Theme() {
    }
}
