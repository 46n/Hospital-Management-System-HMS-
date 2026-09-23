/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package HMS.ui.doctor;



import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.MedicalRecord;
import hms.model.Prescription;

import hms.ui.common.MainFrame;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import hms.ui.common.UIStyles;
/**
 *
 * @author waleed
 */
public class IssuePrescriptionNew extends javax.swing.JPanel {
    
    
    private ClinicalManager clinicalManager;
    private AppointmentManager appointmentManager;
    private AssessmentTypeManager typeManager;
    private UserManager userManager;
    private SessionManager sessionManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private List<Appointment> doctorAppointments;

    /**
     * Creates new form DoctorAssessmentResultNew
     */
    private Doctor currentDoctor() {
        return (Doctor) sessionManager.getCurrentUser();
    }
    
    public IssuePrescriptionNew() {
        initComponents();
    }
    
    public IssuePrescriptionNew(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
            AssessmentTypeManager typeManager, UserManager userManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        initComponents();
        applyStyles();

        this.clinicalManager = clinicalManager;
        this.appointmentManager = appointmentManager;
        this.typeManager = typeManager;
        this.userManager = userManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;

        loadAppointments();
    }
    
    

    private void loadAppointments() {

        doctorAppointments =
            appointmentManager.listForDoctor(currentDoctor().getDoctorId());

        cmbAppointment.removeAllItems();

        for (Appointment appointment : doctorAppointments) {
            cmbAppointment.addItem(
                appointment.getAppointmentId() + " - "
                + appointment.getPatient().getUsername()
                + " (" + appointment.getStatus() + ")"
        );
        }

        if (!doctorAppointments.isEmpty()) {
            refreshTable();
        }
    }
    
    
    
    
    private void issuePrescription() {

        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }

        Appointment appointment =
            doctorAppointments.get(cmbAppointment.getSelectedIndex());

        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(),
                currentDoctor().getDoctorId());

            clinicalManager.addPrescription(
                record.getRecordId(),
                txtMedication.getText().trim(),
                txtDosage.getText().trim(),
                txtInstructions.getText().trim());

            txtMedication.setText("");
            txtDosage.setText("");
            txtInstructions.setText("");

            refreshTable();

            JOptionPane.showMessageDialog(this,
                "Prescription issued.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }
    
    
    
    private void refreshTable() {

        DefaultTableModel model =
            (DefaultTableModel) tblPrescriptions.getModel();

        model.setRowCount(0);

        int index = cmbAppointment.getSelectedIndex();

        if (index < 0 || index >= doctorAppointments.size()) {
            return;
        }

        MedicalRecord record =
            doctorAppointments.get(index).getMedicalRecord();

        if (record == null) {
            return;
        }

        for (Prescription prescription : record.getPrescriptions()) {
            model.addRow(new Object[]{
                prescription.getPrescriptionId(),
                prescription.getMedicationDetails(),
                prescription.getDosage(),
                prescription.getInstructions(),
                prescription.getPrescribedDate()
            });
        }
    }
    
    private void backToDashboard() {
    mainFrame.showPanel(new DoctorDashboardNew(sessionManager, userManager,
        appointmentManager, clinicalManager, typeManager, mainFrame, onLogout));
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void applyStyles() {

        UIStyles.applyHeadingStyle(lblIssuePrescription);

        UIStyles.applyPrimaryButtonStyle(btnIssuePrescription);
        UIStyles.applyPrimaryButtonStyle(btnBackToDashboard);
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblIssuePrescription = new javax.swing.JLabel();
        lblAppointment = new javax.swing.JLabel();
        lblInstructions = new javax.swing.JLabel();
        lblMedication = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        lblValue = new javax.swing.JLabel();
        txtInstructions = new javax.swing.JTextField();
        txtDosage = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPrescriptions = new javax.swing.JTable();
        btnBackToDashboard = new javax.swing.JButton();
        btnIssuePrescription = new javax.swing.JButton();
        txtMedication = new javax.swing.JTextField();

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblIssuePrescription.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblIssuePrescription.setText("Issue Prescription");

        lblAppointment.setText("Appointment:");

        lblInstructions.setText("Instructions:");

        lblMedication.setText("Medication:");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAppointment.addActionListener(this::cmbAppointmentActionPerformed);

        lblValue.setText("Dosage:");

        tblPrescriptions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Rx ID", "Medication", "Dosage", "Instructions", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblPrescriptions);

        btnBackToDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackToDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackToDashboard.setText("Back to Dashboard");
        btnBackToDashboard.addActionListener(this::btnBackToDashboardActionPerformed);

        btnIssuePrescription.setBackground(new java.awt.Color(51, 51, 51));
        btnIssuePrescription.setForeground(new java.awt.Color(255, 255, 255));
        btnIssuePrescription.setText("Issue Prescription");
        btnIssuePrescription.addActionListener(this::btnIssuePrescriptionActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblAppointment)
                    .addComponent(lblMedication))
                .addGap(63, 63, 63)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIssuePrescription)
                        .addGap(118, 118, 118))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(lblInstructions, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbAppointment, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtMedication))
                                .addGap(103, 103, 103)
                                .addComponent(lblValue, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(25, 25, 25)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtInstructions, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addComponent(txtDosage))
                .addGap(18, 18, 18))
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblIssuePrescription)
                .addContainerGap(514, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackToDashboard)
                .addGap(354, 354, 354))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lblIssuePrescription)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAppointment)
                    .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValue)
                    .addComponent(txtDosage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblInstructions)
                            .addComponent(lblMedication)
                            .addComponent(txtInstructions, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMedication, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIssuePrescription, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnBackToDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentActionPerformed
        // TODO add your handling code here:
        if (doctorAppointments != null) {
            refreshTable();
        }
    }//GEN-LAST:event_cmbAppointmentActionPerformed

    private void btnIssuePrescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIssuePrescriptionActionPerformed
        // TODO add your handling code here:
        issuePrescription();
    }//GEN-LAST:event_btnIssuePrescriptionActionPerformed

    private void btnBackToDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashboardActionPerformed
        // TODO add your handling code here:
        backToDashboard();
    }//GEN-LAST:event_btnBackToDashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashboard;
    private javax.swing.JButton btnIssuePrescription;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAppointment;
    private javax.swing.JLabel lblInstructions;
    private javax.swing.JLabel lblIssuePrescription;
    private javax.swing.JLabel lblMedication;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblPrescriptions;
    private javax.swing.JTextField txtDosage;
    private javax.swing.JTextField txtInstructions;
    private javax.swing.JTextField txtMedication;
    // End of variables declaration//GEN-END:variables
}
