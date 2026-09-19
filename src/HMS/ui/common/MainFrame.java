package hms.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Main application shell for the Hospital Management System.
 *
 * Frame-level properties and the CardLayout content shell are managed by the
 * NetBeans GUI Builder (see MainFrame.form). Card content is added and
 * swapped at runtime by showPanel(), so that part is intentionally kept out
 * of the generated block - there is nothing static to design there.
 */
public class MainFrame extends javax.swing.JFrame {

    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color MUTED_TEXT_COLOR = new Color(96, 108, 120);

    private CardLayout contentLayout;

    /**
     * Creates the main application shell. Only shown after a successful
     * login - it holds no login logic of its own.
     */
    public MainFrame() {
        initComponents();
        contentLayout = (CardLayout) contentPanel.getLayout();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));
        addPlaceholderCard();
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hospital Management System");
        setMinimumSize(new java.awt.Dimension(1000, 700));
        setSize(new java.awt.Dimension(1200, 800));

        contentPanel.setLayout(new java.awt.CardLayout());
        contentPanel.setBackground(new java.awt.Color(244, 247, 250));

        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /** The one hardcoded card shown at startup, before any role dashboard is loaded via showPanel(). */
    private void addPlaceholderCard() {
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setBackground(Color.WHITE);
        placeholderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE)));

        JLabel placeholderLabel = new JLabel("Future application screens will be displayed here");
        placeholderLabel.setForeground(MUTED_TEXT_COLOR);
        placeholderLabel.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 16));
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);

        contentPanel.add(placeholderPanel, "placeholder");
        contentLayout.show(contentPanel, "placeholder");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    // End of variables declaration//GEN-END:variables
}
