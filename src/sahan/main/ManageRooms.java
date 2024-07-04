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
public class ManageRooms extends javax.swing.JPanel {

    /**
     * Creates new form ManageGuest
     */
    public ManageRooms() {
        initComponents();
        init();
    }

    private void init() {
        loadBedTypes();
        loadRoomTypes();
        loadStatus();
        loadRooms("");
        reset();
    }

    private HashMap<String, Integer> roomTypeMap;

    private void loadRoomTypes() {
        roomTypeMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `room_type` ORDER BY `type` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("type"));
                roomTypeMap.put(resultSet.getString("type"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            roomTypeComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Room Types: " + ex.getMessage(), ex.getMessage());
        }

    }

    private HashMap<String, Integer> bedTypeMap;

    private void loadBedTypes() {
        bedTypeMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `bed_type` ORDER BY `type` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("type"));
                bedTypeMap.put(resultSet.getString("type"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            bedTypeComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Bed Types: " + ex.getMessage(), ex.getMessage());
        }

    }

    private HashMap<String, Integer> statusMap;

    private void loadStatus() {
        statusMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `status` ORDER BY `status` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("status"));
                statusMap.put(resultSet.getString("status"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            statusComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Status: " + ex.getMessage(), ex.getMessage());
        }
    }

    private void reset() {
        roomNoField.setText("");
        bedTypeComboBox.setSelectedItem("Select");
        statusComboBox.setSelectedItem("Select");

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
    }

    /*
    * Guest Operations
     */
    private void loadRooms(String room) {
        DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM `room` INNER JOIN `bed_type` ON `room`.`bed_type_id`=`bed_type`.`id` INNER JOIN `room_type` ON `room`.`room_type_id`=`room_type`.`id` INNER JOIN `status` ON `room`.`status_id`=`status`.`id`";
        if (!room.isEmpty()) {
            query += " WHERE `room_no` LIKE '%" + room + "%' OR `room_type`.`type` LIKE '%" + room + "%' OR `bed_type`.`type` LIKE '%" + room + "%'";
        }
        query += " ORDER BY `room_no` ASC";

        try {
            ResultSet resultSet = MySQL.execute(query);

            while (resultSet.next()) {
                Vector<String> rowData = new Vector();

                rowData.add(resultSet.getString("room_no"));
                rowData.add(resultSet.getString("room_type.type"));
                rowData.add(resultSet.getString("bed_type.type"));
                rowData.add(resultSet.getString("status.status"));

                model.addRow(rowData);
            }

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Rooms: " + ex.getMessage(), ex.getMessage());
        }
    }

    private void registerRoom() {
        String room_no = roomNoField.getText();
        String bedType = String.valueOf(bedTypeComboBox.getSelectedItem());
        String status = String.valueOf(statusComboBox.getSelectedItem());
        String roomType = String.valueOf(roomTypeComboBox.getSelectedItem());

        // validations
        if (room_no.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Room No.", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomNoField.requestFocus();
        } else if (bedType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Bed Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            bedTypeComboBox.requestFocus();
        } else if (roomType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select room Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomTypeComboBox.requestFocus();
        } else if (status.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Status", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            statusComboBox.requestFocus();
        } else {

            // data insert
            String query = "INSERT INTO `room` (`room_no`, `bed_type_id`, `status_id`, `room_type_id`) "
                    + "VALUES ('" + room_no + "',  " + bedTypeMap.get(bedType) + ", " + statusMap.get(status) + ", " + roomTypeMap.get(roomType) + ")";
            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Registered", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadRooms("");
            } catch (SQLException ex) {
                DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Room Register: " + ex.getMessage(), ex.getMessage());
                JOptionPane.showMessageDialog(this, "Something went wrong!! Please try again", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        }
    }

    private void updateRoom() {
        String room_no = roomNoField.getText();
        String bedType = String.valueOf(bedTypeComboBox.getSelectedItem());
        String roomType = String.valueOf(roomTypeComboBox.getSelectedItem());
        String status = String.valueOf(statusComboBox.getSelectedItem());

        // validations
        if (room_no.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Room No", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomNoField.requestFocus();
        } else if (roomType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select room Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomTypeComboBox.requestFocus();
        } else if (bedType.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Bed Type", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            bedTypeComboBox.requestFocus();
        } else if (status.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Status", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            statusComboBox.requestFocus();
        } else {

            // data insert
            String query = "UPDATE `room` SET  `bed_type_id`='" + bedTypeMap.get(bedType) + "', `status_id`='" + statusMap.get(status) + "', `room_type_id`='" + roomTypeMap.get(roomType) + "'"
                    + "WHERE `room_no`='" + room_no + "'";
            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Updated", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadRooms("");
            } catch (SQLException ex) {
                DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Room Update: " + ex.getMessage(), ex.getMessage());
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
        roomNoField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        bedTypeComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox<>();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        roomTypeComboBox = new javax.swing.JComboBox<>();
        detailPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        roomTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setText("Manage Rooms");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fieldPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 2, 10, 2));

        jLabel2.setText("Room No");

        roomNoField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                roomNoFieldKeyReleased(evt);
            }
        });

        jLabel3.setText("Room Type");

        jLabel6.setText("Bed Type");

        bedTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        jLabel7.setText("Status");

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        addButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        addButton.setText("Add Room");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        updateButton.setText("Update Room");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        roomTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        javax.swing.GroupLayout fieldPanelLayout = new javax.swing.GroupLayout(fieldPanel);
        fieldPanel.setLayout(fieldPanelLayout);
        fieldPanelLayout.setHorizontalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bedTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fieldPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(286, 286, 286))
                    .addComponent(roomNoField)
                    .addComponent(roomTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fieldPanelLayout.createSequentialGroup()
                        .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        fieldPanelLayout.setVerticalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roomNoField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roomTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bedTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(112, 112, 112)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        roomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Room No", "Room Type", "Bed Type", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        roomTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                roomTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(roomTable);

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
        jLabel9.setText("Rooms");

        javax.swing.GroupLayout detailPanelLayout = new javax.swing.GroupLayout(detailPanel);
        detailPanel.setLayout(detailPanelLayout);
        detailPanelLayout.setHorizontalGroup(
            detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
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
        registerRoom();
    }//GEN-LAST:event_addButtonActionPerformed

    private void roomNoFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_roomNoFieldKeyReleased
        // Search by First Name
        if (roomNoField.getText().isEmpty()) {
            loadRooms("");
        } else {
            loadRooms(roomNoField.getText());
        }
    }//GEN-LAST:event_roomNoFieldKeyReleased

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        // Search by First Name
        if (searchField.getText().isEmpty()) {
            loadRooms("");
        } else {
            loadRooms(searchField.getText());
        }
    }//GEN-LAST:event_searchFieldKeyReleased

    private void roomTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_roomTableMouseClicked
        // Select Guest
        if (evt.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(evt)) {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow != -1) {
                roomNoField.setText(String.valueOf(roomTable.getValueAt(selectedRow, 0)));
                bedTypeComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 2)));
                roomTypeComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 1)));
                statusComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 3)));

                addButton.setEnabled(false);
                updateButton.setEnabled(true);
            }
        }
    }//GEN-LAST:event_roomTableMouseClicked

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // Update Guest
        updateRoom();
    }//GEN-LAST:event_updateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox<String> bedTypeComboBox;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JPanel fieldPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField roomNoField;
    private javax.swing.JTable roomTable;
    private javax.swing.JComboBox<String> roomTypeComboBox;
    private javax.swing.JTextField searchField;
    private javax.swing.JComboBox<String> statusComboBox;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
