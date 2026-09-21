package hms.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main application window controller for the Hospital Management System.
 *
 * <p>This frame provides the shared application shell. Role-specific screens,
 * authentication, and application operations belong in separate classes.</p>
 */
public class MainFrame extends JFrame {

    private static final String WINDOW_TITLE = "Hospital Management System";
    private static final Dimension DEFAULT_WINDOW_SIZE = new Dimension(1200, 800);
    private static final Dimension MINIMUM_WINDOW_SIZE = new Dimension(1000, 700);
    private static final String ACTIVE_PANEL = "activePanel";

    /**
     * Creates and configures the shared application window.
     */
    public MainFrame() {
        initComponents();
        configureWindow();
    }

    /**
     * Applies the common window configuration.
     */
    private void configureWindow() {
        setTitle(WINDOW_TITLE);
        setSize(DEFAULT_WINDOW_SIZE);
        setMinimumSize(MINIMUM_WINDOW_SIZE);
        setLocationRelativeTo(null);
    }

    /**
     * Displays a screen inside the central content area.
     *
     * @param panel panel to display
     */
    public void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, ACTIVE_PANEL);

        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();
        cardLayout.show(contentPanel, ACTIVE_PANEL);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Initializes the editable NetBeans JFrame Form.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        sidebarPanel = new javax.swing.JPanel();
        contentPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        headerPanel.setPreferredSize(new Dimension(0, 72));
        headerPanel.setLayout(new BorderLayout());

        titleLabel.setText("Hospital Management System");
        headerPanel.add(titleLabel, BorderLayout.WEST);

        getContentPane().add(headerPanel, BorderLayout.NORTH);

        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setLayout(new BorderLayout());
        getContentPane().add(sidebarPanel, BorderLayout.WEST);

        contentPanel.setLayout(new CardLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
