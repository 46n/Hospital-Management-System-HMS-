package hms.ui.common;

import java.awt.Color;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Reusable styling methods for consistent HMS interfaces.
 */
public final class UIStyles {

    private UIStyles() {
    }

    public static void applyPrimaryButtonStyle(AbstractButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.setBackground(Theme.PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(Theme.DEFAULT_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_SMALL,
                Theme.SPACING_MEDIUM,
                Theme.SPACING_SMALL,
                Theme.SPACING_MEDIUM));
    }

    public static void applyHeadingStyle(JLabel label) {
        label.setForeground(Theme.TEXT_COLOR);
        label.setFont(Theme.HEADING_FONT);
    }
}
