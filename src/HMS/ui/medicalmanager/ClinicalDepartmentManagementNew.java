/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.medicalmanager;
import hms.manager.ClinicalDepartmentManager;
import hms.model.ClinicalDepartment;
import javax.swing.table.DefaultTableModel;
import hms.manager.ClinicalDepartmentManager;
import hms.manager.UserManager;
import hms.model.MedicalManager;
import hms.model.User;
import hms.model.ClinicalDepartment;


/**
 *
 * @author abd
 */
public class ClinicalDepartmentManagementNew extends javax.swing.JPanel {

    /**
     * Creates new form ClinicalDepartmentManagementNew
     */
    private ClinicalDepartmentManager departmentManager;
    private UserManager userManager;
    private Runnable onBack;
    private String editingDepartmentId = null;

    public ClinicalDepartmentManagementNew() {
        initComponents();
    }
    
    private void refreshDepartmentTable() {

    DefaultTableModel model =
            (DefaultTableModel) tblDepartments.getModel();

    model.setRowCount(0);

    for (ClinicalDepartment department
            : departmentManager.getDepartments()) {

        model.addRow(new Object[]{
            department.getDepartmentId(),
            department.getDepartmentName(),
            department.getDescription(),
            department.getMedicalManager() == null
                    ? ""
                    : department.getMedicalManager().getUsername(),
            department.isActive() ? "Yes" : "No"
        });
    }
}

    public ClinicalDepartmentManagementNew(ClinicalDepartmentManager departmentManager,UserManager userManager,Runnable onBack) {

    initComponents();

    this.departmentManager = departmentManager;
    this.userManager = userManager;
    this.onBack = onBack;

    loadManagers();
    refreshDepartmentTable();
    }
    
    private void prepareNewDepartment() {
    editingDepartmentId = null;
    clearDepartmentFields();
    rbActiveYes.setSelected(true);
    DepartmentNameTf.requestFocus();
    }

    private void clearDepartmentFields() {
        DepartmentNameTf.setText("");
        DescriptionTa.setText("");
        cmbManager.setSelectedIndex(-1);
        bgActive.clearSelection();
    }

    private void goBack() {
        if (onBack != null) {
            onBack.run();
        }
    }
    
    
    
    private void saveDepartment() {
        try {
            String name = DepartmentNameTf.getText().trim();
            String description = DescriptionTa.getText().trim();
            Object selectedManager = cmbManager.getSelectedItem();
            if (selectedManager == null) {
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Please select a Medical Manager."
                );
                return;
            }
            if (!rbActiveYes.isSelected()
                    && !rbActiveNo.isSelected()) {
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Please select Active: Yes or No."
                );
                return;
            }
            String managerText = selectedManager.toString();
            String managerId;
            if (managerText.contains(" - ")) {
                managerId = managerText.substring(
                        0,
                        managerText.indexOf(" - ")
                );
            } else {
                managerId = managerText;
            }
            boolean active = rbActiveYes.isSelected();
            if (editingDepartmentId == null) {
                ClinicalDepartment created =
                        departmentManager.createDepartment(
                                name,
                                description,
                                managerId
                        );
                departmentManager.setActive(
                        created.getDepartmentId(),
                        active
                );
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Department created successfully."
                );
            } else {
                departmentManager.updateDepartment(
                        editingDepartmentId,
                        name,
                        description
                );
                departmentManager.assignManager(
                        editingDepartmentId,
                        managerId
                );
                departmentManager.setActive(
                        editingDepartmentId,
                        active
                );
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Department updated successfully."
                );
            }

            refreshDepartmentTable();
            prepareNewDepartment();

        } catch (IllegalArgumentException ex) {

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

        private void loadManagers() {

        cmbManager.removeAllItems();

        for (User user : userManager.getUsers()) {

            if (user instanceof MedicalManager) {

                MedicalManager manager = (MedicalManager) user;

                cmbManager.addItem(
                        manager.getManagerId()
                        + " - "
                        + manager.getUsername()
                );
            }
        }

        cmbManager.setSelectedIndex(-1);
    }
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgActive = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        pnlDepartmentDetails = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblDepartmentName = new javax.swing.JLabel();
        DepartmentNameTf = new javax.swing.JTextField();
        lblDescription = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        DescriptionTa = new javax.swing.JTextArea();
        lblManager = new javax.swing.JLabel();
        cmbManager = new javax.swing.JComboBox<>();
        rbActiveYes = new javax.swing.JRadioButton();
        rbActiveNo = new javax.swing.JRadioButton();
        lblActive = new javax.swing.JLabel();
        btnClearFields = new javax.swing.JButton();
        btnSaveDepartment = new javax.swing.JButton();
        lblSearch = new javax.swing.JLabel();
        SearchTf = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDepartments = new javax.swing.JTable();
        btnNewDepartment = new javax.swing.JButton();
        btnEditSelected = new javax.swing.JButton();
        btnDeleteDepartment = new javax.swing.JButton();
        btnBackDashboard = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Helvetica Neue", 0, 24)); // NOI18N
        lblTitle.setText("Clinical Department Management");

        pnlDepartmentDetails.setBackground(new java.awt.Color(153, 153, 153));

        jLabel1.setText("Department Details");

        lblDepartmentName.setText("Department Name:");

        lblDescription.setText("Description:");

        DescriptionTa.setColumns(20);
        DescriptionTa.setRows(5);
        jScrollPane2.setViewportView(DescriptionTa);

        lblManager.setText("Managed By:");

        cmbManager.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbManager.addActionListener(this::cmbManagerActionPerformed);

        bgActive.add(rbActiveYes);
        rbActiveYes.setText("Yes");

        bgActive.add(rbActiveNo);
        rbActiveNo.setText("No");

        lblActive.setText("Active:");

        btnClearFields.setBackground(new java.awt.Color(51, 51, 51));
        btnClearFields.setForeground(new java.awt.Color(255, 255, 255));
        btnClearFields.setText("Clear");
        btnClearFields.addActionListener(this::btnClearFieldsActionPerformed);

        btnSaveDepartment.setBackground(new java.awt.Color(51, 51, 51));
        btnSaveDepartment.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveDepartment.setText("Save Department ");
        btnSaveDepartment.addActionListener(this::btnSaveDepartmentActionPerformed);

        javax.swing.GroupLayout pnlDepartmentDetailsLayout = new javax.swing.GroupLayout(pnlDepartmentDetails);
        pnlDepartmentDetails.setLayout(pnlDepartmentDetailsLayout);
        pnlDepartmentDetailsLayout.setHorizontalGroup(
            pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblDepartmentName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(DepartmentNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblDescription)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                        .addComponent(lblManager)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbManager, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                        .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnClearFields)
                            .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                                .addComponent(lblActive)
                                .addGap(18, 18, 18)
                                .addComponent(rbActiveYes)))
                        .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(rbActiveNo))
                            .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnSaveDepartment)
                                .addGap(21, 21, 21)))))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        pnlDepartmentDetailsLayout.setVerticalGroup(
            pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDepartmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDepartmentName)
                    .addComponent(DepartmentNameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblManager)
                    .addComponent(cmbManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbActiveYes)
                    .addComponent(lblActive)
                    .addComponent(rbActiveNo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addGroup(pnlDepartmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearFields, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        lblSearch.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        lblSearch.setText("Search (name or description):");

        btnSearch.setBackground(new java.awt.Color(51, 51, 51));
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setText("Search");

        btnClear.setBackground(new java.awt.Color(51, 51, 51));
        btnClear.setForeground(new java.awt.Color(255, 255, 255));
        btnClear.setText("Clear");

        tblDepartments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Description", "Manager", "Active"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDepartments);

        btnNewDepartment.setBackground(new java.awt.Color(51, 51, 51));
        btnNewDepartment.setForeground(new java.awt.Color(255, 255, 255));
        btnNewDepartment.setText("New Department");
        btnNewDepartment.addActionListener(this::btnNewDepartmentActionPerformed);

        btnEditSelected.setBackground(new java.awt.Color(51, 51, 51));
        btnEditSelected.setForeground(new java.awt.Color(255, 255, 255));
        btnEditSelected.setText("Edit Selected");

        btnDeleteDepartment.setBackground(new java.awt.Color(51, 51, 51));
        btnDeleteDepartment.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteDepartment.setText("Delete Department");

        btnBackDashboard.setBackground(new java.awt.Color(51, 51, 51));
        btnBackDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnBackDashboard.setText("Back to Dashboard");
        btnBackDashboard.addActionListener(this::btnBackDashboardActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SearchTf, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClear))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(pnlDepartmentDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTitle)
                        .addGap(89, 89, 89)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(btnNewDepartment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEditSelected)
                .addGap(24, 24, 24)
                .addComponent(btnDeleteDepartment)
                .addGap(26, 26, 26)
                .addComponent(btnBackDashboard)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(218, 218, 218)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSearch)
                            .addComponent(SearchTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSearch)
                            .addComponent(btnClear))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlDepartmentDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDeleteDepartment, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackDashboard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditSelected, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbManagerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbManagerActionPerformed

    private void btnBackDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackDashboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBackDashboardActionPerformed

    private void btnNewDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewDepartmentActionPerformed
        // TODO add your handling code here:
        prepareNewDepartment();
    }//GEN-LAST:event_btnNewDepartmentActionPerformed

    private void btnClearFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearFieldsActionPerformed
        // TODO add your handling code here:
        clearDepartmentFields();
    }//GEN-LAST:event_btnClearFieldsActionPerformed

    private void btnSaveDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveDepartmentActionPerformed
        // TODO add your handling code here:
        saveDepartment();
    }//GEN-LAST:event_btnSaveDepartmentActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField DepartmentNameTf;
    private javax.swing.JTextArea DescriptionTa;
    private javax.swing.JTextField SearchTf;
    private javax.swing.ButtonGroup bgActive;
    private javax.swing.JButton btnBackDashboard;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClearFields;
    private javax.swing.JButton btnDeleteDepartment;
    private javax.swing.JButton btnEditSelected;
    private javax.swing.JButton btnNewDepartment;
    private javax.swing.JButton btnSaveDepartment;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbManager;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblActive;
    private javax.swing.JLabel lblDepartmentName;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblManager;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlDepartmentDetails;
    private javax.swing.JRadioButton rbActiveNo;
    private javax.swing.JRadioButton rbActiveYes;
    private javax.swing.JTable tblDepartments;
    // End of variables declaration//GEN-END:variables
}
