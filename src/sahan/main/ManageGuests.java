/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package sahan.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import sahan.logger.DatabaseLogger;
import sahan.model.MySQL;

/**
 *
 * @author ksoff
 */
public class ManageGuests extends javax.swing.JPanel {

    /**
     * Creates new form ManageGuest
     */
    public ManageGuests() {
        initComponents();
        init();
    }

    private void init() {
        loadGenders();
        loadGuestTypes();
        loadGuests("");
        reset();
    }

    private HashMap<String, Integer> genderMap;

    private void loadGenders() {
        genderMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `gender` ORDER BY `gender` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("gender"));
                genderMap.put(resultSet.getString("gender"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            genderComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Gender: " + ex.getMessage(), ex.getMessage());
        }

    }

    private HashMap<String, Integer> guestTypeMap;

    private void loadGuestTypes() {
        guestTypeMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `guest_type` ORDER BY `type` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("type"));
                guestTypeMap.put(resultSet.getString("type"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            guestTypeComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Guest Types: " + ex.getMessage(), ex.getMessage());
        }
    }

    private void reset() {
        fnameField.setText("");
        lnameField.setText("");
        mobileField.setText("");
        emailField.setText("");
        genderComboBox.setSelectedItem("Select");
        guestTypeComboBox.setSelectedItem("Select");

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
    }

    /*
    * Guest Operations
     */
    private void loadGuests(String guest) {
        DefaultTableModel model = (DefaultTableModel) guestTable.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM `guest` INNER JOIN `gender` ON `guest`.`gender_id`=`gender`.`id` INNER JOIN `guest_type` ON `guest`.`guest_type_id`=`guest_type`.`id`";
        if (!guest.isEmpty()) {
            query += " WHERE `fname` LIKE '%" + guest + "%' OR `lname` LIKE '%" + guest + "%'";
        }
        query += " ORDER BY `mobile` ASC, `status_id` ASC";

        try {
            ResultSet resultSet = MySQL.execute(query);

            while (resultSet.next()) {
                Vector<String> rowData = new Vector();

                rowData.add(resultSet.getString("mobile"));
                rowData.add(resultSet.getString("fname"));
                rowData.add(resultSet.getString("lname"));
                rowData.add(resultSet.getString("email"));
                rowData.add(resultSet.getString("gender.gender"));
                rowData.add(resultSet.getString("guest_type.type"));

                model.addRow(rowData);
            }

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Gender: " + ex.getMessage(), ex.getMessage());
        }
    }

    private void registerGuest() {
        String fname = fnameField.getText();
        String lname = lnameField.getText();
        String mobile = mobileField.getText();
        String email = emailField.getText();
        String gender = String.valueOf(genderComboBox.getSelectedItem());
        String guestType = String.valueOf(guestTypeComboBox.getSelectedItem());

        // validations
        if (fname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter First Name", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            fnameField.requestFocus();
        } else if (lname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Last Name", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            lnameField.requestFocus();
        } else if (mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Mobile", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            mobileField.requestFocus();
        } else if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Email", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
        } else if (gender.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Gender", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            genderComboBox.requestFocus();
        } else if (guestType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Guest Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            guestTypeComboBox.requestFocus();
        } else {

            // data insert
            String query = "INSERT INTO `guest` (`fname`, `lname`, `mobile`, `gender_id`, `guest_type_id`, `email`, `status_id`) "
                    + "VALUES ('" + fname + "', '" + lname + "', '" + mobile + "', " + genderMap.get(gender) + ", " + guestTypeMap.get(guestType) + ", '" + email + "', '1')";
            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Registered", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadGuests("");
            } catch (SQLException ex) {
                DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Guest Register: " + ex.getMessage(), ex.getMessage());
                JOptionPane.showMessageDialog(this, "Something went wrong!! Please try again", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        }
    }

    private String currentMobile;
    private void updateGuest() {
        String fname = fnameField.getText();
        String lname = lnameField.getText();
        String mobile = mobileField.getText();
        String email = emailField.getText();
        String gender = String.valueOf(genderComboBox.getSelectedItem());
        String guestType = String.valueOf(guestTypeComboBox.getSelectedItem());

        // validations
        if (fname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter First Name", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            fnameField.requestFocus();
        } else if (lname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Last Name", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            lnameField.requestFocus();
        } else if (mobile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Mobile", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            mobileField.requestFocus();
        } else if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Email", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
        } else if (gender.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Gender", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            genderComboBox.requestFocus();
        } else if (guestType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Guest Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            guestTypeComboBox.requestFocus();
        } else {

            // data insert
            String query = "UPDATE `guest` SET `fname`='" + fname + "', `lname`='" + lname + "', `mobile`='" + mobile + "', `gender_id`='" + genderMap.get(gender) + "', `guest_type_id`='" + guestTypeMap.get(guestType) + "', `email`='" + email + "' "
                    + "WHERE `mobile`='" + currentMobile + "'";
            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Updated", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadGuests("");
            } catch (SQLException ex) {
                DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Guest Update: " + ex.getMessage(), ex.getMessage());
                JOptionPane.showMessageDialog(this, "Something went wrong!! Please try again", "Warning", JOptionPane.WARNING_MESSAGE);
            }
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

        headerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fieldPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        fnameField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        lnameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        mobileField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        genderComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        guestTypeComboBox = new javax.swing.JComboBox<>();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        detailPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        guestTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setText("Manage Guests");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fieldPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 2, 10, 2));

        jLabel2.setText("First Name");

        fnameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fnameFieldKeyReleased(evt);
            }
        });

        jLabel3.setText("Last Name");

        jLabel4.setText("Mobile");

        jLabel5.setText("Email");

        jLabel6.setText("Gender");

        genderComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        jLabel7.setText("Gender");

        guestTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        addButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        addButton.setText("Add Guest");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        updateButton.setText("Update Guest");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fieldPanelLayout = new javax.swing.GroupLayout(fieldPanel);
        fieldPanel.setLayout(fieldPanelLayout);
        fieldPanelLayout.setHorizontalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fnameField)
                    .addComponent(lnameField, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(mobileField, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(emailField, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(genderComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guestTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fieldPanelLayout.createSequentialGroup()
                        .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fieldPanelLayout.setVerticalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mobileField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guestTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        guestTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Mobile", "First Name", "Last Name", "Email", "Gender", "Guest Type"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        guestTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                guestTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(guestTable);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Developed By Sahan Sachintha (200415401542)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setText("Guests");

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchField)
                    .addComponent(jSeparator1)
                    .addGroup(detailPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        detailPanelLayout.setVerticalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fieldPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(detailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fieldPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Register Guest
        registerGuest();
    }//GEN-LAST:event_addButtonActionPerformed

    private void fnameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fnameFieldKeyReleased
        // Search by First Name
        if (fnameField.getText().isEmpty()) {
            loadGuests("");
        } else {
            loadGuests(fnameField.getText());
        }
    }//GEN-LAST:event_fnameFieldKeyReleased

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        // Search by First Name
        if (searchField.getText().isEmpty()) {
            loadGuests("");
        } else {
            loadGuests(searchField.getText());
        }
    }//GEN-LAST:event_searchFieldKeyReleased

    private void guestTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_guestTableMouseClicked
        // Select Guest
        if (evt.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(evt)) {
            int selectedRow = guestTable.getSelectedRow();
            if (selectedRow != -1) {
                fnameField.setText(String.valueOf(guestTable.getValueAt(selectedRow, 1)));
                lnameField.setText(String.valueOf(guestTable.getValueAt(selectedRow, 2)));
                mobileField.setText(String.valueOf(guestTable.getValueAt(selectedRow, 0)));
                emailField.setText(String.valueOf(guestTable.getValueAt(selectedRow, 3)));
                genderComboBox.setSelectedItem(String.valueOf(guestTable.getValueAt(selectedRow, 4)));
                guestTypeComboBox.setSelectedItem(String.valueOf(guestTable.getValueAt(selectedRow, 5)));

                currentMobile = String.valueOf(guestTable.getValueAt(selectedRow, 0));

                addButton.setEnabled(false);
                updateButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_guestTableMouseClicked

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // Update Guest
        updateGuest();
    }//GEN-LAST:event_updateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JTextField emailField;
    private javax.swing.JPanel fieldPanel;
    private javax.swing.JTextField fnameField;
    private javax.swing.JComboBox<String> genderComboBox;
    private javax.swing.JTable guestTable;
    private javax.swing.JComboBox<String> guestTypeComboBox;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField lnameField;
    private javax.swing.JTextField mobileField;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
