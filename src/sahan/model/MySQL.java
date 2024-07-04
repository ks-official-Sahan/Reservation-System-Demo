package sahan.model;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import sahan.logger.CommonLogger;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;

/**
 *
 * @author ksoff
 */
public class MySQL {
    
    private static Connection connection;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/grandhotel", "root", "King7f2d!@#$");
        } catch (ClassNotFoundException | SQLException e) {
            CommonLogger.logger.log(Level.SEVERE, "Exception in MySQL connection: " + e.getMessage(), e.getMessage());
        }
    }
    
    public static ResultSet execute(String query) throws SQLException {
        Statement statement = connection.createStatement();
        if (query.toUpperCase().startsWith("SELECT")) {
            return statement.executeQuery(query);
        } else {
            statement.executeUpdate(query);
            return null;
        }
    }
    
}
