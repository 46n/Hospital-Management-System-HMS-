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
import hms.model.MedicalRequest;

import hms.ui.common.MainFrame;

import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author waleed
 */
public class RequestLabImagingNew extends javax.swing.JPanel {


    private ClinicalManager clinicalManager;
    private AppointmentManager appointmentManager;
    private AssessmentTypeManager typeManager;
    private UserManager userManager;
    private SessionManager sessionManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private List<Appointment> doctorAppointments;

    /** The three assignment-required request categories. Legacy "IMAGING" records still load and display fine - this is only the set offered for new submissions. */
    private static final String[] REQUEST_TYPES = {"LAB", "X_RAY", "SPECIALIZED_IMAGING"};

    /**
     * Creates new form DoctorAssessmentResultNew
     */
    private Doctor currentDoctor() {
        return (Doctor) sessionManager.getCurrentUser();
    }


    public RequestLabImagingNew() {
        initComponents();
    }


    public RequestLabImagingNew(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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

        loadData();
    }


    private void loadData() {

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

        cmbRequestType.removeAllItems();

        for (String type : REQUEST_TYPES) {
            cmbRequestType.addItem(type);
        }

        if (!doctorAppointments.isEmpty()) {
            refreshTable();
        }
    }


    private void submitRequest() {

        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }

        Appointment appointment =
            doctorAppointments.get(cmbAppointment.getSelectedIndex());

        String requestType =
            (String) cmbRequestType.getSelectedItem();

    try {
        MedicalRecord record = clinicalManager.getOrOpenRecord(
                appointment.getAppointmentId(),
                currentDoctor().getDoctorId());

        clinicalManager.addRequest(
                record.getRecordId(),
                requestType,
                txtDetails.getText());

        txtDetails.setText("");

        refreshTable();

        JOptionPane.showMessageDialog(this,
                requestType + " request submitted.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

    } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
    } catch (IOException ex) {
            showError("The request could not be saved: " + ex.getMessage());
        }
    }


    private void refreshTable() {

        DefaultTableModel model =
            (DefaultTableModel) tblRequests.getModel();

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

        for (MedicalRequest request : record.getMedicalRequests()) {
            model.addRow(new Object[]{
                request.getRequestId(),
                request.getRequestType(),
                request.getDetails(),
                request.getRequestDate(),
                request.getStatus()
            });
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
        lblRequestLabImaging = new javax.swing.JLabel();
        lblAppointment = new javax.swing.JLabel();
        lblRequestType = new javax.swing.JLabel();
        lblDetails = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        cmbRequestType = new javax.swing.JComboBox<>();
        txtDetails = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRequests = new javax.swing.JTable();
        btnBackToDashboard = new javax.swing.JButton();
        btnSubmitRequest = new javax.swing.JButton();

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblRequestLabImaging.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblRequestLabImaging.setText("Request Lab / Imaging");

        lblAppointment.setText("Appointment:");

        lblRequestType.setText("Request Type:");

        lblDetails.setText("Details:");

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAppointment.addActionListener(this::cmbAppointmentActionPerformed);

        cmbRequestType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tblRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Request ID", "Type", "Details", "Date", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblRequests);

        btnBackToDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackToDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackToDashboard.setText("Back to Dashboard");
        btnBackToDashboard.addActionListener(this::btnBackToDashboardActionPerformed);

        btnSubmitRequest.setBackground(new java.awt.Color(51, 51, 51));
        btnSubmitRequest.setForeground(new java.awt.Color(255, 255, 255));
        btnSubmitRequest.setText("Submit Request");
        btnSubmitRequest.addActionListener(this::btnSubmitRequestActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnBackToDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(310, 310, 310))
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 690, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblRequestLabImaging)
                        .addContainerGap(450, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(180, 180, 180)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnSubmitRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAppointment)
                            .addComponent(lblRequestType)
                            .addComponent(lblDetails))
                        .addGap(79, 79, 79)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cmbRequestType, 0, 99, Short.MAX_VALUE)
                            .addComponent(cmbAppointment, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDetails, 0, 150, Short.MAX_VALUE))
                        .addGap(125, 125, 125))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblRequestLabImaging)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAppointment))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbRequestType, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblRequestType))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDetails))
                        .addGap(20, 20, 20))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSubmitRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBackToDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentActionPerformed
        // TODO add your handling code here:
        if (doctorAppointments != null) {
        refreshTable();
        }
    }//GEN-LAST:event_cmbAppointmentActionPerformed

    private void btnSubmitRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitRequestActionPerformed
        // TODO add your handling code here:
        submitRequest();
    }//GEN-LAST:event_btnSubmitRequestActionPerformed

    private void btnBackToDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashboardActionPerformed
        // TODO add your handling code here:
        mainFrame.showPanel(new DoctorDashboardNew(
            sessionManager, userManager, appointmentManager,
            clinicalManager, typeManager, mainFrame, onLogout));
    }//GEN-LAST:event_btnBackToDashboardActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashboard;
    private javax.swing.JButton btnSubmitRequest;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JComboBox<String> cmbRequestType;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAppointment;
    private javax.swing.JLabel lblDetails;
    private javax.swing.JLabel lblRequestLabImaging;
    private javax.swing.JLabel lblRequestType;
    private javax.swing.JTable tblRequests;
    private javax.swing.JTextField txtDetails;
    // End of variables declaration//GEN-END:variables
}
