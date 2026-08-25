package hms.ui.common;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main application window controller for the Hospital Management System.
 *
 * <p>Window configuration, screen navigation, and common application
 * structure will be added in later development phases.</p>
 */
public class MainFrame extends JFrame {

    /**
     * Creates the main application window controller.
     */
    public MainFrame() {
        initComponents();
    }

    /**
     * Placeholder for displaying application screens in the future.
     *
     * @param panel panel to display
     */
    public void showPanel(JPanel panel) {
        // Screen navigation will be implemented later.
    }

    /**
     * Initializes this JFrame Form.
     *
     * <p>This section is managed by the NetBeans GUI Builder.</p>
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
