package hms.ui.common;

import javax.swing.JFrame;

/**
 * Foundation window for the HMS application.
 *
 * <p>Screen navigation will be added in a later development phase.</p>
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("HMS-OODJ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Theme.WINDOW_WIDTH, Theme.WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }
}
