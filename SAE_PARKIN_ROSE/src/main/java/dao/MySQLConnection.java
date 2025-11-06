package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {
<<<<<<< HEAD
    private static final String URL = "jdbc:mysql://localhost:3306/sae_parking";
    private static final String USER = "root";
    private static final String PASSWORD = "$iutinfo";
=======

    private static final String URL = "jdbc:mysql://127.0.0.1/parkinrosebdd";
    private static final String USER = "root";
    private static final String PASSWORD = "$iutinfo";
    
>>>>>>> 7ced645690d60f8c1613c742b43b2d0b3a6602b9
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trouvé", e);
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}