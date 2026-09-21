/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.medicalmanager;
import hms.ui.common.UIStyles;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.MedicalManager;
import hms.model.User;
import javax.swing.table.DefaultTableModel;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;

/**
 *
 * @author abd
 */
public class DoctorShiftManagementNew extends javax.swing.JPanel {

    /**
     * Creates new form DoctorShiftManagementNew
     */
    
    private Runnable onBack;
    private SessionManager sessionManager;
    private UserManager userManager;
    private DoctorShiftManager shiftManager; 
    private String editingShiftId = null;
    
    
    public DoctorShiftManagementNew(SessionManager sessionManager, UserManager userManager, DoctorShiftManager shiftManager, Runnable onBack) {

    initComponents();
    applyButtonStyles();

    this.sessionManager = sessionManager;
    this.userManager = userManager;
    this.shiftManager = shiftManager;
    this.onBack = onBack;

    loadDoctors();
    refreshShiftTable();
}
    private MedicalManager getCurrentManager() {
    User currentUser = sessionManager.getCurrentUser();
    if (!(currentUser instanceof MedicalManager)) {
        throw new IllegalStateException(
                "Current user is not a Medical Manager"
        );
    }
    return (MedicalManager) currentUser;
}
    
    private void clearShiftFields() {
    cmbDoctor.setSelectedIndex(-1);
    Date now = new Date();
    DateSpinner.setValue(now);
    StartTimeSpinner.setValue(now);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.HOUR_OF_DAY, 1);
    EndTimeSpinner.setValue(calendar.getTime());
}
    
    private void prepareNewShift() {
    editingShiftId = null;
    clearShiftFields();
    cmbDoctor.requestFocus();
    }
    private void setTimeSpinner(
        javax.swing.JSpinner spinner,
        LocalTime time) {

    Calendar calendar = Calendar.getInstance();

    calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
    calendar.set(Calendar.MINUTE, time.getMinute());
    calendar.set(Calendar.SECOND, 0);

    spinner.setValue(calendar.getTime());
}
    
    private LocalTime getTimeFromSpinner(javax.swing.JSpinner spinner) {
    Date value = (Date) spinner.getValue();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(value);
    return LocalTime.of(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
    );
    }
    
    
    private void removeSelectedShift() {
    int selectedRow = tblDoctorShifts.getSelectedRow();
    if (selectedRow == -1) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select a shift first."
        );
        return;
    }
    int modelRow =
            tblDoctorShifts.convertRowIndexToModel(selectedRow);
    String shiftId =
            tblDoctorShifts.getModel()
                    .getValueAt(modelRow, 0)
                    .toString();
    int choice =
            javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to remove this shift?",
                    "Confirm Remove",
                    javax.swing.JOptionPane.YES_NO_OPTION
            );
    if (choice != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }
    try {
        shiftManager.removeShift(shiftId);
        editingShiftId = null;
        prepareNewShift();
        refreshShiftTable();
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Shift removed successfully."
        );
    } catch (IllegalArgumentException ex) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
}
    
    
    private void loadSelectedShift() {

    int selectedRow = tblDoctorShifts.getSelectedRow();

    if (selectedRow == -1) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Please select a shift first."
        );
        return;
    }

    int modelRow = tblDoctorShifts.convertRowIndexToModel(selectedRow);

    String shiftId = tblDoctorShifts.getModel()
            .getValueAt(modelRow, 0)
            .toString();

    DoctorShift shift = shiftManager.findById(shiftId);

    if (shift == null) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Shift could not be found."
        );
        return;
    }

    editingShiftId = shift.getShiftId();

    if (shift.getDoctor() != null) {
        String doctorItem =
                shift.getDoctor().getDoctorId()
                + " - "
                + shift.getDoctor().getUsername();

        cmbDoctor.setSelectedItem(doctorItem);
    }

    DateSpinner.setValue(shift.getShiftDate());

    setTimeSpinner(
            StartTimeSpinner,
            shift.getStartTime()
    );

    setTimeSpinner(
            EndTimeSpinner,
            shift.getEndTime()
    );

    // Current backend updateShift() does not change the doctor
    cmbDoctor.setEnabled(false);
}
    
    private void saveShift() {
    try {

        Object selectedDoctor = cmbDoctor.getSelectedItem();
        if (selectedDoctor == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Please select a doctor."
            );
            return;
        }
        String doctorText = selectedDoctor.toString();
        String doctorId;
        if (doctorText.contains(" - ")) {
            doctorId = doctorText.substring(
                    0,
                    doctorText.indexOf(" - ")
            );
        } else {
            doctorId = doctorText;
        }

        Date shiftDate =
                (Date) DateSpinner.getValue();

        LocalTime startTime =
                getTimeFromSpinner(StartTimeSpinner);

        LocalTime endTime =
                getTimeFromSpinner(EndTimeSpinner);

        MedicalManager manager =
                getCurrentManager();

        if (editingShiftId == null) {
            shiftManager.createShift(
                    manager.getManagerId(),
                    doctorId,
                    shiftDate,
                    startTime,
                    endTime
            );
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Shift created successfully."
            );
        } else {
            shiftManager.updateShift(
                    editingShiftId,
                    shiftDate,
                    startTime,
                    endTime
            );
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Shift updated successfully."
            );
        }
        refreshShiftTable();
        prepareNewShift();
    } catch (IllegalArgumentException ex) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }
    }
    
    
    
    private void refreshShiftTable() {
    DefaultTableModel model =
            (DefaultTableModel) tblDoctorShifts.getModel();
    model.setRowCount(0);
    MedicalManager manager = getCurrentManager();
    for (DoctorShift shift
            : shiftManager.getShiftsForManager(
                    manager.getManagerId())) {
        model.addRow(new Object[]{
            shift.getShiftId(),
            shift.getDoctor() == null
                    ? ""
                    : shift.getDoctor().getUsername(),
            shift.getShiftDate(),
            shift.getStartTime(),
            shift.getEndTime()
        });
    }
}
    
    private void loadDoctors() {
    cmbDoctor.removeAllItems();
    MedicalManager manager = getCurrentManager();
    for (User user : userManager.getUsers()) {
        if (user instanceof Doctor) {
            Doctor doctor = (Doctor) user;
            if (doctor.getShiftManager() != null
                    && manager.getManagerId().equals(
                            doctor.getShiftManager().getManagerId())) {
                cmbDoctor.addItem(
                        doctor.getDoctorId()
                        + " - "
                        + doctor.getUsername()
                );
            }
        }
    }
    if (cmbDoctor.getItemCount() > 0) {
    cmbDoctor.setSelectedIndex(0);
}
}
    
    
    private void applyButtonStyles() {

    UIStyles.applyPrimaryButtonStyle(btnClearFields);
    UIStyles.applyPrimaryButtonStyle(btnSaveShift);
    UIStyles.applyPrimaryButtonStyle(btnNewShift);
    UIStyles.applyPrimaryButtonStyle(btnEditSelected);
    UIStyles.applyPrimaryButtonStyle(btnRemoveSelected);
    UIStyles.applyPrimaryButtonStyle(btnBackDashboard);

    btnClearFields.setOpaque(true);
    btnClearFields.setContentAreaFilled(true);

    btnSaveShift.setOpaque(true);
    btnSaveShift.setContentAreaFilled(true);

    btnNewShift.setOpaque(true);
    btnNewShift.setContentAreaFilled(true);

    btnEditSelected.setOpaque(true);
    btnEditSelected.setContentAreaFilled(true);

    btnRemoveSelected.setOpaque(true);
    btnRemoveSelected.setContentAreaFilled(true);

    btnBackDashboard.setOpaque(true);
    btnBackDashboard.setContentAreaFilled(true);
}
    
    private void goBack() {
    if (onBack != null) {
        onBack.run();
    }
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDoctorShifts = new javax.swing.JTable();
        btnNewShift = new javax.swing.JButton();
        btnEditSelected = new javax.swing.JButton();
        btnRemoveSelected = new javax.swing.JButton();
        btnBackDashboard = new javax.swing.JButton();
        pnlShiftDetails = new javax.swing.JPanel();
        lblDetailsTitle = new javax.swing.JLabel();
        lblDoctor = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblStartTime = new javax.swing.JLabel();
        lblEndTime = new javax.swing.JLabel();
        cmbDoctor = new javax.swing.JComboBox<>();
        DateSpinner = new javax.swing.JSpinner();
        EndTimeSpinner = new javax.swing.JSpinner();
        StartTimeSpinner = new javax.swing.JSpinner();
        btnClearFields = new javax.swing.JButton();
        btnSaveShift = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        lblTitle.setText("Doctor Shift Management");

        tblDoctorShifts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Doctor", "Date", "Start", "End"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDoctorShifts);

        btnNewShift.setBackground(new java.awt.Color(51, 51, 51));
        btnNewShift.setForeground(new java.awt.Color(255, 255, 255));
        btnNewShift.setText("New Shift");
        btnNewShift.addActionListener(this::btnNewShiftActionPerformed);

        btnEditSelected.setBackground(new java.awt.Color(51, 51, 51));
        btnEditSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnEditSelected.setText("Edit Selected");
        btnEditSelected.addActionListener(this::btnEditSelectedActionPerformed);

        btnRemoveSelected.setBackground(new java.awt.Color(51, 51, 51));
        btnRemoveSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnRemoveSelected.setText("Remove Selected");
        btnRemoveSelected.addActionListener(this::btnRemoveSelectedActionPerformed);

        btnBackDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackDashboard.setText("Back to Dashboard ");
        btnBackDashboard.addActionListener(this::btnBackDashboardActionPerformed);

        lblDetailsTitle.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        lblDetailsTitle.setText("Shift Details");

        lblDoctor.setText("Doctor:");

        lblDate.setText("Date:");

        lblStartTime.setText("Start Time:");

        lblEndTime.setText("End Time:");

        cmbDoctor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        DateSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_WEEK));
        DateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(DateSpinner, "yyyy-MM-dd"));

        EndTimeSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        EndTimeSpinner.setEditor(new javax.swing.JSpinner.DateEditor(EndTimeSpinner, "HH:mm"));

        StartTimeSpinner.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        StartTimeSpinner.setEditor(new javax.swing.JSpinner.DateEditor(StartTimeSpinner, "HH:mm"));

        btnClearFields.setBackground(new java.awt.Color(51, 51, 51));
        btnClearFields.setForeground(new java.awt.Color(255, 255, 255));
        btnClearFields.setText("Clear");
        btnClearFields.addActionListener(this::btnClearFieldsActionPerformed);

        btnSaveShift.setBackground(new java.awt.Color(51, 51, 51));
        btnSaveShift.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveShift.setText("Save Shift");
        btnSaveShift.addActionListener(this::btnSaveShiftActionPerformed);

        javax.swing.GroupLayout pnlShiftDetailsLayout = new javax.swing.GroupLayout(pnlShiftDetails);
        pnlShiftDetails.setLayout(pnlShiftDetailsLayout);
        pnlShiftDetailsLayout.setHorizontalGroup(
            pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDetailsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEndTime)
                            .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                                .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblDoctor)
                                    .addComponent(lblDate))
                                .addGap(18, 18, 18)
                                .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbDoctor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(DateSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                                .addComponent(lblStartTime)
                                .addGap(18, 18, 18)
                                .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(StartTimeSpinner)
                                    .addComponent(EndTimeSpinner)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlShiftDetailsLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(btnClearFields)
                                .addGap(28, 28, 28)
                                .addComponent(btnSaveShift, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(45, 45, 45))))
        );
        pnlShiftDetailsLayout.setVerticalGroup(
            pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDetailsTitle)
                .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlShiftDetailsLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDoctor)
                            .addComponent(cmbDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDate)
                            .addComponent(DateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblStartTime)
                            .addComponent(StartTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblEndTime)
                            .addComponent(EndTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(145, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlShiftDetailsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlShiftDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSaveShift, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnClearFields, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnNewShift)
                                .addGap(18, 18, 18)
                                .addComponent(btnEditSelected)
                                .addGap(18, 18, 18)
                                .addComponent(btnRemoveSelected)
                                .addGap(18, 18, 18)
                                .addComponent(btnBackDashboard))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(pnlShiftDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblTitle)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlShiftDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNewShift, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRemoveSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(58, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewShiftActionPerformed
        // TODO add your handling code here:
        prepareNewShift();
    }//GEN-LAST:event_btnNewShiftActionPerformed

    private void btnBackDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackDashboardActionPerformed
        // TODO add your handling code here:
        goBack();
    }//GEN-LAST:event_btnBackDashboardActionPerformed

    private void btnClearFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearFieldsActionPerformed
        // TODO add your handling code here:
        clearShiftFields();
    }//GEN-LAST:event_btnClearFieldsActionPerformed

    private void btnSaveShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveShiftActionPerformed
        // TODO add your handling code here:
        saveShift();
    }//GEN-LAST:event_btnSaveShiftActionPerformed

    private void btnRemoveSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveSelectedActionPerformed
        // TODO add your handling code here:
        removeSelectedShift();
    }//GEN-LAST:event_btnRemoveSelectedActionPerformed

    private void btnEditSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditSelectedActionPerformed
        // TODO add your handling code here:
        loadSelectedShift();
    }//GEN-LAST:event_btnEditSelectedActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner DateSpinner;
    private javax.swing.JSpinner EndTimeSpinner;
    private javax.swing.JSpinner StartTimeSpinner;
    private javax.swing.JButton btnBackDashboard;
    private javax.swing.JButton btnClearFields;
    private javax.swing.JButton btnEditSelected;
    private javax.swing.JButton btnNewShift;
    private javax.swing.JButton btnRemoveSelected;
    private javax.swing.JButton btnSaveShift;
    private javax.swing.JComboBox<String> cmbDoctor;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDetailsTitle;
    private javax.swing.JLabel lblDoctor;
    private javax.swing.JLabel lblEndTime;
    private javax.swing.JLabel lblStartTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlShiftDetails;
    private javax.swing.JTable tblDoctorShifts;
    // End of variables declaration//GEN-END:variables
}
