/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.patient;
import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.DoctorShift;

import hms.ui.common.AppointmentTableUtil;
import hms.ui.common.MainFrame;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.table.DefaultTableModel;
import hms.ui.common.UIStyles;

/**
 *
 * @author andli
 */
public class AppointmentBookingNew extends javax.swing.JPanel {

    private UserManager userManager;
    private AppointmentManager appointmentManager;
    private DoctorShiftManager shiftManager;
    private ClinicalManager clinicalManager;
    private BillingManager billingManager;
    private SessionManager sessionManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private AppointmentBookingController appointmentController;
    private final SimpleDateFormat dateFormat =
        new SimpleDateFormat("yyyy-MM-dd");

    public AppointmentBookingNew() {
        initComponents();
        applyStyles();
    }

    public AppointmentBookingNew(
            UserManager userManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            ClinicalManager clinicalManager,
            BillingManager billingManager,
            SessionManager sessionManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        initComponents();
        applyStyles();

        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;

        this.appointmentController = new AppointmentBookingController(
                userManager,
                appointmentManager,
                shiftManager,
                clinicalManager,
                billingManager,
                sessionManager,
                mainFrame,
                onLogout
        );
        loadInitialData();
    }
    private void loadInitialData() {
    loadDoctors();
    loadMyAppointments();

    cmbAppointmentDate.removeAllItems();
    cmbAvailableTime.removeAllItems();
}
    private void loadDoctors() {

    DefaultTableModel model =
            (DefaultTableModel) tblDoctors.getModel();

    model.setRowCount(0);

    for (Doctor doctor : appointmentController.getAvailableDoctors()) {

        model.addRow(new Object[]{
            doctor.getDoctorId(),
            doctor.getUsername(),
            doctor.getSpecialty()
        });
    }
}
private void loadMyAppointments() {

    DefaultTableModel model =
            (DefaultTableModel) tblAppointments.getModel();

    model.setRowCount(0);

    for (Appointment appointment
            : appointmentController.getMyAppointments()) {

        model.addRow(
                AppointmentTableUtil.toRow(appointment)
        );
    }
}
    private void loadSelectedDoctorShifts() {

    DefaultTableModel shiftModel =
            (DefaultTableModel) tblDoctorShifts.getModel();

    shiftModel.setRowCount(0);

    cmbAppointmentDate.removeAllItems();
    cmbAvailableTime.removeAllItems();

    int selectedRow = tblDoctors.getSelectedRow();

    if (selectedRow < 0) {
        return;
    }

    String doctorId =
            tblDoctors.getValueAt(selectedRow, 0).toString();

    java.util.List<DoctorShift> shifts =
            appointmentController.getFutureShiftsForDoctor(doctorId);

    if (shifts.isEmpty()) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                "This doctor has no future working shifts available.",
                "No Available Shifts",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
        );

        return;
    }

    Set<String> availableDates = new LinkedHashSet<>();

    for (DoctorShift shift : shifts) {

        String date =
                dateFormat.format(shift.getShiftDate());

        String start =
                shift.getStartTime().toString();

        String end =
                shift.getEndTime().toString();

        shiftModel.addRow(new Object[]{
            date,
            start,
            end
        });

        availableDates.add(date);
    }
    for (String date : availableDates) {
    cmbAppointmentDate.addItem(date);
    }

    }

private void applyStyles() {

    UIStyles.applyHeadingStyle(lblTitle);

    UIStyles.applyPrimaryButtonStyle(btnBookAppointment);
    UIStyles.applyPrimaryButtonStyle(btnCancelAppointment);
    UIStyles.applyPrimaryButtonStyle(btnRescheduleAppointment);
    UIStyles.applyPrimaryButtonStyle(btnBack);
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblAvailableDoctors = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDoctors = new javax.swing.JTable();
        lblDoctorShifts = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDoctorShifts = new javax.swing.JTable();
        lblAppointmentDate = new javax.swing.JLabel();
        cmbAppointmentDate = new javax.swing.JComboBox<>();
        lblAvailableTime = new javax.swing.JLabel();
        cmbAvailableTime = new javax.swing.JComboBox<>();
        btnBookAppointment = new javax.swing.JButton();
        lblMyAppointments = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        btnCancelAppointment = new javax.swing.JButton();
        btnRescheduleAppointment = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(850, 600));
        setLayout(null);

        lblTitle.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lblTitle.setText("Book an Appointment");
        add(lblTitle);
        lblTitle.setBounds(10, 0, 231, 32);

        lblAvailableDoctors.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        lblAvailableDoctors.setText("Available Doctors");
        add(lblAvailableDoctors);
        lblAvailableDoctors.setBounds(280, 50, 200, 32);

        tblDoctors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Username", "Specialty"
            }
        ));
        tblDoctors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDoctorsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDoctors);

        add(jScrollPane1);
        jScrollPane1.setBounds(170, 80, 375, 200);

        lblDoctorShifts.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        lblDoctorShifts.setText("Selected Doctor's Working Shifts");
        add(lblDoctorShifts);
        lblDoctorShifts.setBounds(680, 50, 260, 30);

        tblDoctorShifts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "Start", "End"
            }
        ));
        jScrollPane2.setViewportView(tblDoctorShifts);

        add(jScrollPane2);
        jScrollPane2.setBounds(610, 80, 375, 200);

        lblAppointmentDate.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        lblAppointmentDate.setText("Appointment Date:");
        add(lblAppointmentDate);
        lblAppointmentDate.setBounds(470, 300, 108, 18);

        cmbAppointmentDate.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAppointmentDate.addActionListener(this::cmbAppointmentDateActionPerformed);
        add(cmbAppointmentDate);
        cmbAppointmentDate.setBounds(600, 300, 72, 20);

        lblAvailableTime.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        lblAvailableTime.setText("Available Time:");
        add(lblAvailableTime);
        lblAvailableTime.setBounds(470, 340, 87, 18);

        cmbAvailableTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAvailableTime.addActionListener(this::cmbAvailableTimeActionPerformed);
        add(cmbAvailableTime);
        cmbAvailableTime.setBounds(600, 340, 72, 20);

        btnBookAppointment.setText("Book Appointment");
        btnBookAppointment.addActionListener(this::btnBookAppointmentActionPerformed);
        add(btnBookAppointment);
        btnBookAppointment.setBounds(480, 370, 180, 30);

        lblMyAppointments.setFont(new java.awt.Font("Dialog", 0, 15)); // NOI18N
        lblMyAppointments.setText("My Appointments");
        add(lblMyAppointments);
        lblMyAppointments.setBounds(160, 410, 111, 20);

        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Patient", "Doctor", "Date", "Time", "Status"
            }
        ));
        jScrollPane3.setViewportView(tblAppointments);

        add(jScrollPane3);
        jScrollPane3.setBounds(160, 440, 827, 170);

        btnCancelAppointment.setText("Cancel Selected Appointment");
        btnCancelAppointment.addActionListener(this::btnCancelAppointmentActionPerformed);
        add(btnCancelAppointment);
        btnCancelAppointment.setBounds(300, 620, 250, 33);

        btnRescheduleAppointment.setText("Reschedule Selected Appointment");
        btnRescheduleAppointment.addActionListener(this::btnRescheduleAppointmentActionPerformed);
        add(btnRescheduleAppointment);
        btnRescheduleAppointment.setBounds(590, 620, 265, 32);

        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);
        add(btnBack);
        btnBack.setBounds(470, 660, 200, 32);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbAvailableTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAvailableTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbAvailableTimeActionPerformed

    private void cmbAppointmentDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAppointmentDateActionPerformed
        loadAvailableTimes();
    }//GEN-LAST:event_cmbAppointmentDateActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        appointmentController.backToDashboard();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelAppointmentActionPerformed
        appointmentController.cancelSelectedAppointment(
        tblAppointments,
        this::loadMyAppointments
);
    }//GEN-LAST:event_btnCancelAppointmentActionPerformed

    private void btnRescheduleAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRescheduleAppointmentActionPerformed
        appointmentController.rescheduleSelectedAppointment(
        tblAppointments,
        cmbAppointmentDate,
        cmbAvailableTime,
        this::loadMyAppointments,
        this::loadAvailableTimes
);
    }//GEN-LAST:event_btnRescheduleAppointmentActionPerformed

    private void tblDoctorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDoctorsMouseClicked

        loadSelectedDoctorShifts();
    }//GEN-LAST:event_tblDoctorsMouseClicked

    private void btnBookAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookAppointmentActionPerformed
        appointmentController.bookSelectedAppointment(
        tblDoctors,
        cmbAppointmentDate,
        cmbAvailableTime,
        this::loadMyAppointments,
        this::loadAvailableTimes
);
    }//GEN-LAST:event_btnBookAppointmentActionPerformed
    private void loadAvailableTimes() {

    cmbAvailableTime.removeAllItems();

    int selectedRow = tblDoctors.getSelectedRow();

    if (selectedRow < 0) {
        return;
    }

    Object selectedDate =
            cmbAppointmentDate.getSelectedItem();

    if (selectedDate == null) {
        return;
    }

    String doctorId =
            tblDoctors.getValueAt(selectedRow, 0).toString();

    try {

        java.util.List<String> slots =
                appointmentController.getTimeSlotsForDate(
                        doctorId,
                        selectedDate.toString()
                );

        if (slots.isEmpty()) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "No consultation times are available for this date.",
                    "No Available Times",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        for (String slot : slots) {
            cmbAvailableTime.addItem(slot);
        }

    } catch (IllegalArgumentException ex) {

        javax.swing.JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Unable to Load Times",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBookAppointment;
    private javax.swing.JButton btnCancelAppointment;
    private javax.swing.JButton btnRescheduleAppointment;
    private javax.swing.JComboBox<String> cmbAppointmentDate;
    private javax.swing.JComboBox<String> cmbAvailableTime;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblAppointmentDate;
    private javax.swing.JLabel lblAvailableDoctors;
    private javax.swing.JLabel lblAvailableTime;
    private javax.swing.JLabel lblDoctorShifts;
    private javax.swing.JLabel lblMyAppointments;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTable tblAppointments;
    private javax.swing.JTable tblDoctorShifts;
    private javax.swing.JTable tblDoctors;
    // End of variables declaration//GEN-END:variables
}
