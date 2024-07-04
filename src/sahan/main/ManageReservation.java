package sahan.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class ManageReservation extends javax.swing.JPanel {

    /**
     * Creates new form ManageGuest
     */
    public ManageReservation() {
        initComponents();
        init();
    }

    private void init() {
        checkinData.setDate(new Date());
        checkoutDate.setDate(new Date());
        loadGuestMobile();
        loadRoomNo();
        loadStatus();
        loadReservations("");
        reset();
    }

    private void loadRoomNo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `room` WHERE `status_id`='1' ORDER BY `room_no` ASC");

            while (resultSet.next()) {
                data.add(resultSet.getString("room_no"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            roomNoComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Room No: " + ex.getMessage(), ex.getMessage());
        }

    }

    private HashMap<String, Integer> guestMobileMap;

    private void loadGuestMobile() {
        guestMobileMap = new HashMap<>();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        Vector<String> data = new Vector();
        data.add("Select");

        try {
            ResultSet resultSet = MySQL.execute("SELECT * FROM `guest`");

            while (resultSet.next()) {
                data.add(resultSet.getString("mobile"));
                guestMobileMap.put(resultSet.getString("mobile"), resultSet.getInt("mobile"));
                //data.add(resultSet.getString("mobile") + ": " + resultSet.getString("fname") + " " + resultSet.getString("lname"));
                //guestMobileMap.put(resultSet.getString("mobile") + ": " + resultSet.getString("fname") + " " + resultSet.getString("lname"), resultSet.getInt("id"));
            }

            model.addAll(data);
            model.setSelectedItem("Select");

            guestMobileComboBox.setModel(model);

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Mobile: " + ex.getMessage(), ex.getMessage());
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
        checkoutDate.setDate(new Date());
        checkinData.setDate(new Date());
        roomNoComboBox.setSelectedItem("Select");
        guestMobileComboBox.setSelectedItem("Select");
        statusComboBox.setSelectedItem("Select");

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-mm-dd");
    }

    /*
    * Guest Operations
     */
    private void loadReservations(String reservation) {
        DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM `reservation` INNER JOIN `room` ON `room`.`room_no`=`reservation`.`room_room_no` INNER JOIN `guest` ON `reservation`.`guest_mobile`=`guest`.`mobile` INNER JOIN `status` ON `reservation`.`status_id`=`status`.`id`";
        if (!reservation.isEmpty()) {
            query += " WHERE `id` LIKE '%" + reservation + "%' OR `room_no` LIKE '%" + reservation + "%' OR `guest_mobile` LIKE '%" + reservation + "%'";
        }

        try {
            ResultSet resultSet = MySQL.execute(query);

            while (resultSet.next()) {
                Vector<String> rowData = new Vector();

                rowData.add(resultSet.getString("id"));
                rowData.add(resultSet.getString("room_no"));
                rowData.add(resultSet.getString("guest_mobile"));
                rowData.add(resultSet.getString("check_in"));
                rowData.add(resultSet.getString("check_out"));
                rowData.add(resultSet.getString("status.status"));

                model.addRow(rowData);
            }

        } catch (SQLException ex) {
            DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Load Reservations: " + ex.getMessage(), ex.getMessage());
        }
    }

    private void registerReservation() {
        String room_no = String.valueOf(roomNoComboBox.getSelectedItem());
        String status = String.valueOf(statusComboBox.getSelectedItem());
        String guestMobile = String.valueOf(guestMobileComboBox.getSelectedItem());
        Date checkin = checkinData.getDate();
        Date checkout = checkoutDate.getDate();

        // validations
        if (room_no.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Room No", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomNoComboBox.requestFocus();
        } else if (guestMobile.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Mobile", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            guestMobileComboBox.requestFocus();
        } else if (status.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Status", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            statusComboBox.requestFocus();
        } else {

            // data insert
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String chechkin_date = format.format(checkin);
            String chechkout_date = format.format(checkout);

            String query = "INSERT INTO `reservation` (`room_room_no`, `guest_mobile`, `status_id`, `check_in`, `check_out`) "
                    + "VALUES ('" + room_no + "',  '" + guestMobile + "', '" + statusMap.get(status) + "', '" + chechkin_date + "', '" + chechkout_date + "')";

            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Registered", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadReservations("");
            } catch (SQLException ex) {
                DatabaseLogger.logger.log(Level.SEVERE, "SQLException in Reservation Register: " + ex.getMessage(), ex.getMessage());
                JOptionPane.showMessageDialog(this, "Something went wrong!! Please try again", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        }
    }

    private String currentId;

    private void updateReservation() {
        String room_no = String.valueOf(roomNoComboBox.getSelectedItem());
        String status = String.valueOf(statusComboBox.getSelectedItem());
        String guestMobile = String.valueOf(guestMobileComboBox.getSelectedItem());
        Date checkin = checkinData.getDate();
        Date checkout = checkoutDate.getDate();

        // validations
        if (room_no.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Room No", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            roomNoComboBox.requestFocus();
        } else if (guestMobile.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Mobile", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            guestMobileComboBox.requestFocus();
        } else if (status.equalsIgnoreCase("Select")) {
            JOptionPane.showMessageDialog(this, "Please Select Status", "Invalid Data", JOptionPane.WARNING_MESSAGE);
            statusComboBox.requestFocus();
        } else {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String chechkin_date = format.format(checkin);
            String chechkout_date = format.format(checkout);

            // data insert
            String query = "UPDATE `reservation` SET  `guest_mobile`='" + guestMobile + "', `status_id`='" + statusMap.get(status) + "', `room_room_no`='" + room_no + "', `check_in`='" + chechkin_date + "', `check_out`='" + chechkout_date + "'"
                    + "WHERE `id`='" + currentId + "'";
            try {
                MySQL.execute(query);
                JOptionPane.showMessageDialog(this, "Successfully Updated", "Success", JOptionPane.INFORMATION_MESSAGE);

                reset();
                loadReservations("");
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
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox<>();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        guestMobileComboBox = new javax.swing.JComboBox<>();
        roomNoComboBox = new javax.swing.JComboBox<>();
        checkinData = new com.toedter.calendar.JDateChooser();
        checkoutDate = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        detailPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        roomTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jLabel1.setText("Manage Reservations");

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

        jLabel3.setText("Guest");

        jLabel6.setText("Check In");

        jLabel7.setText("Status");

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        addButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        addButton.setText("Add Reservation");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        updateButton.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        updateButton.setText("Update Reservation");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        guestMobileComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        roomNoComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select" }));

        jLabel10.setText("Check Out");

        javax.swing.GroupLayout fieldPanelLayout = new javax.swing.GroupLayout(fieldPanel);
        fieldPanel.setLayout(fieldPanelLayout);
        fieldPanelLayout.setHorizontalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(roomNoComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(statusComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(guestMobileComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fieldPanelLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(checkinData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(checkoutDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fieldPanelLayout.createSequentialGroup()
                        .addGroup(fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3)
                            .addComponent(jLabel10))
                        .addContainerGap(20, Short.MAX_VALUE))))
        );
        fieldPanelLayout.setVerticalGroup(
            fieldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fieldPanelLayout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roomNoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(guestMobileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkinData, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkoutDate, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 41, Short.MAX_VALUE))
        );

        roomTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Room No", "Guest Mobile", "Check-in", "Check-out", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
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
        jLabel9.setText("Reservations");

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
        registerReservation();
    }//GEN-LAST:event_addButtonActionPerformed

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        // Search by First Name
        if (searchField.getText().isEmpty()) {
            loadReservations("");
        } else {
            loadReservations(searchField.getText());
        }
    }//GEN-LAST:event_searchFieldKeyReleased

    private void roomTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_roomTableMouseClicked
        // Select Guest
        if (evt.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(evt)) {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    currentId = String.valueOf(roomTable.getValueAt(selectedRow, 0));
                    roomNoComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 1)));
                    guestMobileComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 2)));
                    statusComboBox.setSelectedItem(String.valueOf(roomTable.getValueAt(selectedRow, 5)));

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
                    Date chechkin_date = format.parse(String.valueOf(roomTable.getValueAt(selectedRow, 3)));
                    Date chechkout_date = format.parse(String.valueOf(roomTable.getValueAt(selectedRow, 4)));

                    checkinData.setDate(chechkin_date);
                    checkinData.setDate(chechkout_date);

                    addButton.setEnabled(false);
                    updateButton.setEnabled(true);
                } catch (ParseException ex) {
                    Logger.getLogger(ManageReservation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_roomTableMouseClicked

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // Update Guest
        updateReservation();
    }//GEN-LAST:event_updateButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private com.toedter.calendar.JDateChooser checkinData;
    private com.toedter.calendar.JDateChooser checkoutDate;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JPanel fieldPanel;
    private javax.swing.JComboBox<String> guestMobileComboBox;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox<String> roomNoComboBox;
    private javax.swing.JTable roomTable;
    private javax.swing.JTextField searchField;
    private javax.swing.JComboBox<String> statusComboBox;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
