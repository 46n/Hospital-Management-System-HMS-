package hms.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Main application shell for the Hospital Management System.
 */
public class MainFrame extends JFrame {

    private static final Color CONTENT_BACKGROUND = new Color(244, 247, 250);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color MUTED_TEXT_COLOR = new Color(96, 108, 120);

    private final JPanel contentPanel = new JPanel();
    private CardLayout contentLayout;

    /**
     * Creates the main application shell. Only shown after a successful
     * login - it holds no login logic of its own.
     */
    public MainFrame() {
        initializeFrame();
        createContentPanel();
        setContentPane(contentPanel);
        setLocationRelativeTo(null);
    }

    /**
     * Replaces the current center content with another application screen.
     *
     * @param panel panel to display
     */
    public void showPanel(JPanel panel) {
        if (panel == null) {
            throw new IllegalArgumentException("The content panel cannot be null.");
        }

        contentPanel.removeAll();
        contentPanel.add(panel, "activeContent");
        contentLayout.show(contentPanel, "activeContent");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void initializeFrame() {
        setTitle("Hospital Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
    }

    private void createContentPanel() {
        contentLayout = new CardLayout();
        contentPanel.setLayout(contentLayout);
        contentPanel.setBackground(CONTENT_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE));

        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(Color.WHITE);
        placeholderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        Theme.SPACING_LARGE,
                        Theme.SPACING_LARGE,
                        Theme.SPACING_LARGE,
                        Theme.SPACING_LARGE)));

        JLabel placeholderLabel = new JLabel("Future application screens will be displayed here");
        placeholderLabel.setForeground(MUTED_TEXT_COLOR);
        placeholderLabel.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 16));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        contentPanel.add(placeholderPanel, "placeholder");
        contentLayout.show(contentPanel, "placeholder");
    }
}
