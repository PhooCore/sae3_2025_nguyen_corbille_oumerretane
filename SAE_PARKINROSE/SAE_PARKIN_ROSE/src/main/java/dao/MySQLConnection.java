package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1/parkinrosebdd";
    private static final String USER = "root";
    private static final String PASSWORD = "$iutinfo";


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL non trouvé", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}