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

import hms.ui.common.MainFrame;

import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author waleed
 */
public class LogVitalsNew extends javax.swing.JPanel {
    
    
    private ClinicalManager clinicalManager;
    private AppointmentManager appointmentManager;
    private AssessmentTypeManager typeManager;
    private UserManager userManager;
    private SessionManager sessionManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private List<Appointment> doctorAppointments;

    /**
     * Creates new form LogVitalsNew
     */
    
    private Doctor currentDoctor() {
        return (Doctor) sessionManager.getCurrentUser();
    }
    
    public LogVitalsNew() {
        initComponents();
    }
    
    public LogVitalsNew(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
        AssessmentTypeManager typeManager, UserManager userManager, SessionManager sessionManager,
        MainFrame mainFrame, Runnable onLogout) {
         initComponents();

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
            loadExistingRecord();
        }
    }
    

   
    private void loadExistingRecord() {

        int index = cmbAppointment.getSelectedIndex();

        if (index < 0 || index >= doctorAppointments.size()) {
            return;
        }

        MedicalRecord record =
            doctorAppointments.get(index).getMedicalRecord();

        if (record != null) {
            txtVitalSigns.setText(
                record.getVitalSigns() == null ? "" : record.getVitalSigns());

            txtConsultationNotes.setText(
                record.getConsultationNotes() == null
                ? "" : record.getConsultationNotes());

            txtDiagnosis.setText(
                record.getDiagnosis() == null ? "" : record.getDiagnosis());

        } else {
            txtVitalSigns.setText("");
            txtConsultationNotes.setText("");
            txtDiagnosis.setText("");
        }
    }
    
    
    private void saveVitals() {

         if (doctorAppointments == null || doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }

        int index = cmbAppointment.getSelectedIndex();

        if (index < 0 || index >= doctorAppointments.size()) {
            showError("Please select an appointment.");
            return;
        }

        if (txtVitalSigns.getText().trim().isEmpty()) {
            showError("Please enter vital signs.");
            return;
        }

        if (txtConsultationNotes.getText().trim().isEmpty()) {
            showError("Please enter consultation notes.");
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            showError("Please enter a diagnosis.");
            return;
        }

        Appointment appointment = doctorAppointments.get(index);

        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(),
                currentDoctor().getDoctorId());

            clinicalManager.updateVitalsAndNotes(
                record.getRecordId(),
                txtVitalSigns.getText().trim(),
                txtDiagnosis.getText().trim(),
                txtConsultationNotes.getText().trim());

            JOptionPane.showMessageDialog(this,
                "Vitals and notes saved.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
}
    
    private void finalizeRecord() {

        if (doctorAppointments == null || doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }

        int index = cmbAppointment.getSelectedIndex();

        if (index < 0 || index >= doctorAppointments.size()) {
            showError("Please select an appointment.");
            return;
        }

        Appointment appointment = doctorAppointments.get(index);

        if ("COMPLETED".equals(appointment.getStatus())) {
            showError("This appointment is already completed.");
            return;
        }

        if (txtVitalSigns.getText().trim().isEmpty()) {
            showError("Please enter vital signs before finalizing.");
            return;
        }

        if (txtConsultationNotes.getText().trim().isEmpty()) {
            showError("Please enter consultation notes before finalizing.");
            return;
        }

        if (txtDiagnosis.getText().trim().isEmpty()) {
            showError("Please enter a diagnosis before finalizing.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Finalize record for appointment "
            + appointment.getAppointmentId() + "?\n"
            + "This marks the appointment COMPLETED and cannot be undone.",
            "Confirm Finalize",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(),
                currentDoctor().getDoctorId());

            clinicalManager.updateVitalsAndNotes(
                record.getRecordId(),
                txtVitalSigns.getText().trim(),
                txtDiagnosis.getText().trim(),
                txtConsultationNotes.getText().trim());

            clinicalManager.finalizeRecord(record.getRecordId());

            JOptionPane.showMessageDialog(this,
                "Record finalized. Appointment marked COMPLETED.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            refreshAppointmentCombo();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
}
    
    private void refreshAppointmentCombo() {

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
    }
    
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Error", JOptionPane.ERROR_MESSAGE);
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
        lblLogVitalsConsultationNotes = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblVitalSigns = new javax.swing.JLabel();
        lblConsultationNotes = new javax.swing.JLabel();
        lblDiagnosis = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        txtDiagnosis = new javax.swing.JTextField();
        txtVitalSigns = new javax.swing.JTextField();
        btnSaveVitalsNotes = new javax.swing.JButton();
        btnFinalizeRecord = new javax.swing.JButton();
        btnBackToDashboard = new javax.swing.JButton();
        scrConsultationNotes = new javax.swing.JScrollPane();
        txtConsultationNotes = new javax.swing.JTextArea();

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblLogVitalsConsultationNotes.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblLogVitalsConsultationNotes.setText("Log Vitals & Consultation Notes");

        jLabel2.setText("Appointment:");

        lblVitalSigns.setText("Vital Signs:");

        lblConsultationNotes.setText("Consultation Notes:");

        lblDiagnosis.setText("Diagnosis:");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAppointment.addActionListener(this::cmbAppointmentActionPerformed);

        btnSaveVitalsNotes.setBackground(new java.awt.Color(51, 51, 51));
        btnSaveVitalsNotes.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveVitalsNotes.setText("Save Vitals & Notes");
        btnSaveVitalsNotes.addActionListener(this::btnSaveVitalsNotesActionPerformed);

        btnFinalizeRecord.setBackground(new java.awt.Color(51, 51, 51));
        btnFinalizeRecord.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizeRecord.setText("Finalize Record (Mark Completed)");
        btnFinalizeRecord.addActionListener(this::btnFinalizeRecordActionPerformed);

        btnBackToDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackToDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackToDashboard.setText("Back to Dashboard");
        btnBackToDashboard.addActionListener(this::btnBackToDashboardActionPerformed);

        txtConsultationNotes.setColumns(20);
        txtConsultationNotes.setRows(5);
        scrConsultationNotes.setViewportView(txtConsultationNotes);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(285, 285, 285)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(lblLogVitalsConsultationNotes))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(264, 264, 264)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblVitalSigns)
                                    .addComponent(jLabel2)
                                    .addComponent(lblConsultationNotes)
                                    .addComponent(lblDiagnosis))
                                .addGap(77, 77, 77)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDiagnosis, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                                    .addComponent(txtVitalSigns)
                                    .addComponent(scrConsultationNotes)))
                            .addComponent(btnSaveVitalsNotes)
                            .addComponent(btnFinalizeRecord))))
                .addContainerGap(66, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnBackToDashboard)
                .addGap(227, 227, 227))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(lblLogVitalsConsultationNotes)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(28, 28, 28)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblVitalSigns))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(64, 64, 64)
                        .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtVitalSigns, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblConsultationNotes))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrConsultationNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDiagnosis, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDiagnosis))
                .addGap(13, 13, 13)
                .addComponent(btnSaveVitalsNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnFinalizeRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(btnBackToDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentActionPerformed
        // TODO add your handling code here:
        if (doctorAppointments != null) {
            loadExistingRecord();
        }
    }//GEN-LAST:event_cmbAppointmentActionPerformed

    private void btnSaveVitalsNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveVitalsNotesActionPerformed
        // TODO add your handling code here:
        saveVitals();
    }//GEN-LAST:event_btnSaveVitalsNotesActionPerformed

    private void btnFinalizeRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizeRecordActionPerformed
        // TODO add your handling code here:
        finalizeRecord();
    }//GEN-LAST:event_btnFinalizeRecordActionPerformed

    private void btnBackToDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashboardActionPerformed
        // TODO add your handling code here:
         mainFrame.showPanel(new DoctorDashboardNew(
            sessionManager, userManager, appointmentManager,
            clinicalManager, typeManager, mainFrame, onLogout));
    }//GEN-LAST:event_btnBackToDashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashboard;
    private javax.swing.JButton btnFinalizeRecord;
    private javax.swing.JButton btnSaveVitalsNotes;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblConsultationNotes;
    private javax.swing.JLabel lblDiagnosis;
    private javax.swing.JLabel lblLogVitalsConsultationNotes;
    private javax.swing.JLabel lblVitalSigns;
    private javax.swing.JScrollPane scrConsultationNotes;
    private javax.swing.JTextArea txtConsultationNotes;
    private javax.swing.JTextField txtDiagnosis;
    private javax.swing.JTextField txtVitalSigns;
    // End of variables declaration//GEN-END:variables
}
