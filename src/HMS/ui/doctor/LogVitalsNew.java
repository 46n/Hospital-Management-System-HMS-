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
import hms.ui.common.UIStyles;

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
            appointmentManager.listActiveForDoctor(
                currentDoctor().getDoctorId()
            );
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
        } else {
            clearVitalFields();
            txtConsultationNotes.setText("");
            txtDiagnosis.setText("");
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
            loadVitalSigns(record.getVitalSigns());

            txtConsultationNotes.setText(
                record.getConsultationNotes() == null
                ? "" : record.getConsultationNotes());

            txtDiagnosis.setText(
                record.getDiagnosis() == null ? "" : record.getDiagnosis());

        } else {
            clearVitalFields();
            txtConsultationNotes.setText("");
            txtDiagnosis.setText("");
        }
    }
    
    private String getVitalSignsText() {

        String systolic = txtSystolic.getText().trim();
        String diastolic = txtDiastolic.getText().trim();
        String pulse = txtPulse.getText().trim();
        String temperature = txtTemperature.getText().trim();

        if (systolic.isEmpty()
            || diastolic.isEmpty()
            || pulse.isEmpty()
            || temperature.isEmpty()) {

            throw new IllegalArgumentException(
                "Please enter all vital signs.");
        }

        try {
            Integer.parseInt(systolic);
            Integer.parseInt(diastolic);
            Integer.parseInt(pulse);
            Double.parseDouble(temperature);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                "Vital signs must contain valid numbers.");
        }

        return "Systolic: " + systolic + " mmHg"
            + "; Diastolic: " + diastolic + " mmHg"
            + "; Pulse: " + pulse + " bpm"
            + "; Temperature: " + temperature + " C";
    }
    
    private void clearVitalFields() {

        txtSystolic.setText("");
        txtDiastolic.setText("");
        txtPulse.setText("");
        txtTemperature.setText("");
    }
    
    
    private void loadVitalSigns(String vitalSigns) {

        clearVitalFields();

        if (vitalSigns == null || vitalSigns.trim().isEmpty()) {
            return;
        }

        String[] parts = vitalSigns.split(";");

        if (parts.length != 4) {
            return;
        }

        txtSystolic.setText(
                parts[0].replace("Systolic:", "")
                    .replace("mmHg", "")
                    .trim());

        txtDiastolic.setText(
                parts[1].replace("Diastolic:", "")
                    .replace("mmHg", "")
                    .trim());

        txtPulse.setText(
                parts[2].replace("Pulse:", "")
                    .replace("bpm", "")
                    .trim());

        txtTemperature.setText(
                parts[3].replace("Temperature:", "")
                    .replace("C", "")
                    .trim());
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
            String vitalSigns = getVitalSignsText();
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(),
                currentDoctor().getDoctorId());

            clinicalManager.updateVitalsAndNotes(
                record.getRecordId(),
                getVitalSignsText(),
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

        String vitalSigns;

        try {
            vitalSigns = getVitalSignsText();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
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
                vitalSigns,
                txtDiagnosis.getText().trim(),
                txtConsultationNotes.getText().trim());

            clinicalManager.finalizeRecord(record.getRecordId());

            JOptionPane.showMessageDialog(this,
                "Record finalized. Appointment marked COMPLETED.",
                "Success",
            JOptionPane.INFORMATION_MESSAGE);

            clearVitalFields();
            txtConsultationNotes.setText("");
            txtDiagnosis.setText("");

            refreshAppointmentCombo();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
}
    
    private void refreshAppointmentCombo() {

        loadAppointments();
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

        UIStyles.applyHeadingStyle(lblLogVitalsConsultationNotes);

        UIStyles.applyPrimaryButtonStyle(btnSaveVitalsNotes);
        UIStyles.applyPrimaryButtonStyle(btnFinalizeRecord);
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
        lblLogVitalsConsultationNotes = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblConsultationNotes = new javax.swing.JLabel();
        lblDiagnosis = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        txtDiagnosis = new javax.swing.JTextField();
        btnSaveVitalsNotes = new javax.swing.JButton();
        btnFinalizeRecord = new javax.swing.JButton();
        btnBackToDashboard = new javax.swing.JButton();
        scrConsultationNotes = new javax.swing.JScrollPane();
        txtConsultationNotes = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        txtPulse = new javax.swing.JTextField();
        txtSystolic = new javax.swing.JTextField();
        txtDiastolic = new javax.swing.JTextField();
        txtTemperature = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setLayout(null);

        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1);
        jPanel1.setBounds(285, 111, 0, 0);

        lblLogVitalsConsultationNotes.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblLogVitalsConsultationNotes.setText("Log Vitals & Consultation Notes");
        add(lblLogVitalsConsultationNotes);
        lblLogVitalsConsultationNotes.setBounds(10, 10, 341, 30);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Appointment:");
        add(jLabel2);
        jLabel2.setBounds(30, 80, 110, 20);

        lblConsultationNotes.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblConsultationNotes.setText("Consultation Notes:");
        add(lblConsultationNotes);
        lblConsultationNotes.setBounds(30, 290, 150, 20);

        lblDiagnosis.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblDiagnosis.setText("Diagnosis:");
        add(lblDiagnosis);
        lblDiagnosis.setBounds(20, 450, 110, 20);

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAppointment.addActionListener(this::cmbAppointmentActionPerformed);
        add(cmbAppointment);
        cmbAppointment.setBounds(150, 80, 490, 30);
        add(txtDiagnosis);
        txtDiagnosis.setBounds(20, 480, 620, 30);

        btnSaveVitalsNotes.setBackground(new java.awt.Color(51, 51, 51));
        btnSaveVitalsNotes.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveVitalsNotes.setText("Save Vitals & Notes");
        btnSaveVitalsNotes.addActionListener(this::btnSaveVitalsNotesActionPerformed);
        add(btnSaveVitalsNotes);
        btnSaveVitalsNotes.setBounds(400, 520, 170, 37);

        btnFinalizeRecord.setBackground(new java.awt.Color(51, 51, 51));
        btnFinalizeRecord.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizeRecord.setText("Finalize Record (Mark Completed)");
        btnFinalizeRecord.addActionListener(this::btnFinalizeRecordActionPerformed);
        add(btnFinalizeRecord);
        btnFinalizeRecord.setBounds(90, 520, 265, 40);

        btnBackToDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackToDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackToDashboard.setText("Back to Dashboard");
        btnBackToDashboard.addActionListener(this::btnBackToDashboardActionPerformed);
        add(btnBackToDashboard);
        btnBackToDashboard.setBounds(270, 590, 170, 37);

        txtConsultationNotes.setColumns(20);
        txtConsultationNotes.setRows(5);
        scrConsultationNotes.setViewportView(txtConsultationNotes);

        add(scrConsultationNotes);
        scrConsultationNotes.setBounds(20, 320, 620, 120);

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Vital Signs", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 18))); // NOI18N
        jPanel2.setLayout(null);
        jPanel2.add(txtPulse);
        txtPulse.setBounds(170, 80, 140, 30);
        jPanel2.add(txtSystolic);
        txtSystolic.setBounds(170, 20, 140, 30);

        txtDiastolic.addActionListener(this::txtDiastolicActionPerformed);
        jPanel2.add(txtDiastolic);
        txtDiastolic.setBounds(470, 20, 140, 30);
        jPanel2.add(txtTemperature);
        txtTemperature.setBounds(470, 80, 140, 30);

        jLabel1.setText("Systolic (mmHg):");
        jPanel2.add(jLabel1);
        jLabel1.setBounds(17, 30, 150, 16);

        jLabel3.setText("Pulse (bpm):");
        jPanel2.add(jLabel3);
        jLabel3.setBounds(17, 80, 150, 16);

        jLabel4.setText("Diastolic (mmHg):");
        jPanel2.add(jLabel4);
        jLabel4.setBounds(327, 30, 150, 16);

        jLabel5.setText("Temperature (°C):");
        jPanel2.add(jLabel5);
        jLabel5.setBounds(327, 90, 150, 16);

        add(jPanel2);
        jPanel2.setBounds(20, 140, 620, 130);
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
        backToDashboard();
    }//GEN-LAST:event_btnBackToDashboardActionPerformed

    private void txtDiastolicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiastolicActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDiastolicActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashboard;
    private javax.swing.JButton btnFinalizeRecord;
    private javax.swing.JButton btnSaveVitalsNotes;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblConsultationNotes;
    private javax.swing.JLabel lblDiagnosis;
    private javax.swing.JLabel lblLogVitalsConsultationNotes;
    private javax.swing.JScrollPane scrConsultationNotes;
    private javax.swing.JTextArea txtConsultationNotes;
    private javax.swing.JTextField txtDiagnosis;
    private javax.swing.JTextField txtDiastolic;
    private javax.swing.JTextField txtPulse;
    private javax.swing.JTextField txtSystolic;
    private javax.swing.JTextField txtTemperature;
    // End of variables declaration//GEN-END:variables
}
