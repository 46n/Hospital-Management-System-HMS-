package hms.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Main application shell for the Hospital Management System.
 */
public class MainFrame extends JFrame {

    private static final Color HEADER_COLOR = Theme.PRIMARY_COLOR;
    private static final Color SIDEBAR_COLOR = new Color(247, 249, 252);
    private static final Color CONTENT_BACKGROUND = new Color(244, 247, 250);
    private static final Color FOOTER_COLOR = new Color(250, 251, 252);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color MUTED_TEXT_COLOR = new Color(96, 108, 120);

    private static final int HEADER_HEIGHT = 78;
    private static final int SIDEBAR_WIDTH = 235;
    private static final int FOOTER_HEIGHT = 34;

    private static final SimpleDateFormat CLOCK_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");

    private CardLayout contentLayout;
    private JLabel dateTimeLabel;
    private Timer clockTimer;

    /**
     * Creates the main application shell. Only shown after a successful
     * login - it holds no login logic of its own.
     */
    public MainFrame() {
        initComponents();
        initializeFrame();
        createHeaderPanel();
        createSidebarPanel();
        createContentPanel();
        createFooterPanel();
        startClock();
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

    private void createHeaderPanel() {
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
                0, Theme.SPACING_LARGE, 0, Theme.SPACING_LARGE));

        JPanel leftHeaderPanel = createTransparentPanel(new BorderLayout());
        leftHeaderPanel.setPreferredSize(new Dimension(300, HEADER_HEIGHT));
        JLabel brandLabel = createLabel(
                "HMS  |  Hospital Management System",
                Color.WHITE,
                new Font(Font.SANS_SERIF, Font.BOLD, 17));
        leftHeaderPanel.add(brandLabel, BorderLayout.CENTER);

        JPanel centerHeaderPanel = createTransparentPanel(new BorderLayout());
        JLabel frameTitleLabel = createLabel(
                "Application Workspace",
                Color.WHITE,
                new Font(Font.SANS_SERIF, Font.BOLD, 19));
        frameTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerHeaderPanel.add(frameTitleLabel, BorderLayout.CENTER);

        JPanel rightHeaderPanel = createTransparentPanel(new BorderLayout());
        rightHeaderPanel.setPreferredSize(new Dimension(300, HEADER_HEIGHT));
        JLabel userAreaLabel = createLabel(
                "User Area",
                new Color(225, 235, 245),
                Theme.DEFAULT_FONT);
        userAreaLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightHeaderPanel.add(userAreaLabel, BorderLayout.CENTER);

        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(centerHeaderPanel, BorderLayout.CENTER);
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
    }

    private void createSidebarPanel() {
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, BORDER_COLOR));

        JPanel sidebarTopPanel = new JPanel();
        sidebarTopPanel.setOpaque(false);
        sidebarTopPanel.setLayout(
                new BoxLayout(sidebarTopPanel, BoxLayout.Y_AXIS));
        sidebarTopPanel.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE,
                Theme.SPACING_MEDIUM,
                Theme.SPACING_LARGE));

        JLabel navigationLabel = createLabel(
                "NAVIGATION",
                MUTED_TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.BOLD, 12));
        navigationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarTopPanel.add(navigationLabel);
        sidebarTopPanel.add(Box.createVerticalStrut(Theme.SPACING_LARGE));
        sidebarTopPanel.add(createSidebarSection("Main"));
        sidebarTopPanel.add(Box.createVerticalStrut(Theme.SPACING_MEDIUM));
        sidebarTopPanel.add(createSidebarSection("Reports"));
        sidebarTopPanel.add(Box.createVerticalStrut(Theme.SPACING_MEDIUM));
        sidebarTopPanel.add(createSidebarSection("System"));

        JPanel sidebarBottomPanel = new JPanel(new BorderLayout());
        sidebarBottomPanel.setOpaque(false);
        sidebarBottomPanel.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_MEDIUM,
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE,
                Theme.SPACING_LARGE));
        JLabel reservedLabel = createLabel(
                "Reserved navigation area",
                MUTED_TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sidebarBottomPanel.add(reservedLabel, BorderLayout.SOUTH);

        sidebarPanel.add(sidebarTopPanel, BorderLayout.NORTH);
        sidebarPanel.add(sidebarBottomPanel, BorderLayout.SOUTH);
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

        JLabel placeholderLabel = createLabel(
                "Future application screens will be displayed here",
                MUTED_TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        contentPanel.add(placeholderPanel, "placeholder");
        contentLayout.show(contentPanel, "placeholder");
    }

    private void createFooterPanel() {
        footerPanel.setLayout(new BorderLayout());
        footerPanel.setBackground(FOOTER_COLOR);
        footerPanel.setPreferredSize(new Dimension(0, FOOTER_HEIGHT));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        0, Theme.SPACING_MEDIUM, 0, Theme.SPACING_MEDIUM)));

        JLabel statusLabel = createLabel(
                "Hospital Management System  |  Ready",
                MUTED_TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateTimeLabel = createLabel(
                CLOCK_FORMAT.format(new Date()),
                MUTED_TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        dateTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(dateTimeLabel, BorderLayout.EAST);
    }

    /** Keeps the footer clock live; stopped in dispose() so it never outlives this frame. */
    private void startClock() {
        clockTimer = new Timer(1000, e -> dateTimeLabel.setText(CLOCK_FORMAT.format(new Date())));
        clockTimer.start();
    }

    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }

    private JPanel createTransparentPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private JLabel createSidebarSection(String text) {
        JLabel label = createLabel(
                text,
                Theme.TEXT_COLOR,
                new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, HEADER_COLOR),
                BorderFactory.createEmptyBorder(
                        Theme.SPACING_SMALL,
                        Theme.SPACING_MEDIUM,
                        Theme.SPACING_SMALL,
                        Theme.SPACING_MEDIUM)));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return label;
    }

    private JLabel createLabel(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    /**
     * Initializes the components managed by the NetBeans GUI Builder.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        sidebarPanel = new javax.swing.JPanel();
        contentPanel = new javax.swing.JPanel();
        footerPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hospital Management System");
        setMinimumSize(new java.awt.Dimension(1000, 700));
        setPreferredSize(new java.awt.Dimension(1200, 800));

        headerPanel.setBackground(new java.awt.Color(31, 78, 121));
        headerPanel.setPreferredSize(new java.awt.Dimension(0, 78));
        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        sidebarPanel.setBackground(new java.awt.Color(247, 249, 252));
        sidebarPanel.setPreferredSize(new java.awt.Dimension(235, 0));
        getContentPane().add(sidebarPanel, java.awt.BorderLayout.WEST);

        contentPanel.setBackground(new java.awt.Color(244, 247, 250));
        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        footerPanel.setBackground(new java.awt.Color(250, 251, 252));
        footerPanel.setPreferredSize(new java.awt.Dimension(0, 34));
        getContentPane().add(footerPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel sidebarPanel;
    // End of variables declaration//GEN-END:variables
}
