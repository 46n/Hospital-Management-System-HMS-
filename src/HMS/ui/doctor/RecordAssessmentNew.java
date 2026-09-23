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
import hms.model.AssessmentResult;
import hms.model.AssessmentType;
import hms.model.Doctor;
import hms.model.MedicalRecord;

import hms.ui.common.MainFrame;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import hms.ui.common.UIStyles;

/**
 *
 * @author waleed
 */
public class RecordAssessmentNew extends javax.swing.JPanel {
    
    private ClinicalManager clinicalManager;
    private AppointmentManager appointmentManager;
    private AssessmentTypeManager typeManager;
    private UserManager userManager;
    private SessionManager sessionManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private List<Appointment> doctorAppointments;
    private List<AssessmentType> activeTypes;
    

    /**
     * Creates new form DoctorAssessmentResultNew
     */
    public RecordAssessmentNew() {
        initComponents();
    }
    
    public RecordAssessmentNew(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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

        loadData();
    
    }

        

    private Doctor currentDoctor() {
        return (Doctor) sessionManager.getCurrentUser();
    }
    
    
    
    
    private void loadData() {
        doctorAppointments = appointmentManager.listForDoctor(currentDoctor().getDoctorId());

        cmbAppointment.removeAllItems();

        for (Appointment appointment : doctorAppointments) {
                cmbAppointment.addItem(
                appointment.getAppointmentId() + " - "
                + appointment.getPatient().getUsername()
                + " (" + appointment.getStatus() + ")");
        }
        
    

    activeTypes = new ArrayList<>();

    for (AssessmentType type : typeManager.getTypes()) {
        if (type.isActive()) {
            activeTypes.add(type);
        }
    }

    cmbAssessmentType.removeAllItems();

    for (AssessmentType type : activeTypes) {
        cmbAssessmentType.addItem(
                type.getName() + " ("
                + type.getCategory() + ", "
                + type.getGradingRule() + ")"
        );
    }
    }
    
    
    
    private void recordResult() {
        if (doctorAppointments.isEmpty()) {
        showError("You have no appointments to record a result against.");
        return;
    }

    if (activeTypes.isEmpty()) {
        showError("No active assessment types are available. Ask a Medical Manager to add one.");
        return;
    }

    Appointment appointment = doctorAppointments.get(cmbAppointment.getSelectedIndex());
    AssessmentType type = activeTypes.get(cmbAssessmentType.getSelectedIndex());

    try {
        MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(), currentDoctor().getDoctorId());

        clinicalManager.addAssessmentResult(
                record.getRecordId(), type.getAssessmentTypeId(),
                txtValue.getText().trim(), txtGradeInterpretation.getText().trim());

        txtValue.setText("");
        txtGradeInterpretation.setText("");

        refreshResultsTable(record);

        JOptionPane.showMessageDialog(this, "Assessment result recorded.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
            
}
    
    
    private void refreshResultsTable(MedicalRecord record) {
        DefaultTableModel model =
            (DefaultTableModel) tblAssessmentResults.getModel();

            model.setRowCount(0);

        for (AssessmentResult result : record.getAssessmentResults()) {
        model.addRow(new Object[]{
            result.getResultId(),
            result.getAssessmentName(),
            result.getValue(),
            result.getGrade(),
            result.getResultDate()
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

        UIStyles.applyHeadingStyle(lblRecordAssessmentResult);

        UIStyles.applyPrimaryButtonStyle(btnRecordResult);
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
        lblRecordAssessmentResult = new javax.swing.JLabel();
        lblAppointment = new javax.swing.JLabel();
        lblGradeInterpretation = new javax.swing.JLabel();
        lblAssessmentType = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        cmbAssessmentType = new javax.swing.JComboBox<>();
        lblValue = new javax.swing.JLabel();
        txtGradeInterpretation = new javax.swing.JTextField();
        txtValue = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAssessmentResults = new javax.swing.JTable();
        btnBackToDashboard = new javax.swing.JButton();
        btnRecordResult = new javax.swing.JButton();

        setLayout(null);

        jPanel1.setLayout(new java.awt.BorderLayout());
        add(jPanel1);
        jPanel1.setBounds(196, 139, 0, 0);

        lblRecordAssessmentResult.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblRecordAssessmentResult.setText("Record Assessment Result");
        add(lblRecordAssessmentResult);
        lblRecordAssessmentResult.setBounds(6, 6, 277, 32);

        lblAppointment.setText("Appointment:");
        add(lblAppointment);
        lblAppointment.setBounds(27, 64, 74, 16);

        lblGradeInterpretation.setText("Grade / Interpretation (optional):");
        add(lblGradeInterpretation);
        lblGradeInterpretation.setBounds(340, 110, 180, 16);

        lblAssessmentType.setText("Assessment Type:");
        add(lblAssessmentType);
        lblAssessmentType.setBounds(6, 115, 94, 16);

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbAppointment);
        cmbAppointment.setBounds(118, 53, 210, 30);

        cmbAssessmentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbAssessmentType);
        cmbAssessmentType.setBounds(118, 106, 210, 28);

        lblValue.setText("Value:");
        add(lblValue);
        lblValue.setBounds(470, 60, 32, 16);
        add(txtGradeInterpretation);
        txtGradeInterpretation.setBounds(530, 100, 170, 32);
        add(txtValue);
        txtValue.setBounds(530, 50, 170, 32);

        tblAssessmentResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Result ID", "Assessment", "Value", "Grade", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblAssessmentResults);

        add(jScrollPane1);
        jScrollPane1.setBounds(0, 194, 760, 209);

        btnBackToDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackToDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackToDashboard.setText("Back to Dashboard");
        btnBackToDashboard.addActionListener(this::btnBackToDashboardActionPerformed);
        add(btnBackToDashboard);
        btnBackToDashboard.setBounds(370, 420, 150, 37);

        btnRecordResult.setBackground(new java.awt.Color(51, 51, 51));
        btnRecordResult.setForeground(new java.awt.Color(255, 255, 255));
        btnRecordResult.setText("Record Result");
        btnRecordResult.addActionListener(this::btnRecordResultActionPerformed);
        add(btnRecordResult);
        btnRecordResult.setBounds(202, 139, 122, 37);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRecordResultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordResultActionPerformed
        // TODO add your handling code here:
        recordResult();
    }//GEN-LAST:event_btnRecordResultActionPerformed

    private void btnBackToDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashboardActionPerformed
        // TODO add your handling code here:
        backToDashboard();

    }//GEN-LAST:event_btnBackToDashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashboard;
    private javax.swing.JButton btnRecordResult;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JComboBox<String> cmbAssessmentType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAppointment;
    private javax.swing.JLabel lblAssessmentType;
    private javax.swing.JLabel lblGradeInterpretation;
    private javax.swing.JLabel lblRecordAssessmentResult;
    private javax.swing.JLabel lblValue;
    private javax.swing.JTable tblAssessmentResults;
    private javax.swing.JTextField txtGradeInterpretation;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables
}
