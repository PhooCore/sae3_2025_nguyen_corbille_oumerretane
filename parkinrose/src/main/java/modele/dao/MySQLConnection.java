package modele.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    private static final String HOST = "sql7.freesqldatabase.com";
    private static final String DATABASE = "sql7816083";
    private static final String PORT = "3306";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;
    private static final String USER = "sql7816083";
    private static final String PASSWORD = "JvQpL3zIAj";

    public static Connection getConnection() throws SQLException {
        try {
            // Pour MySQL 8.0+
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                // Pour les anciennes versions de MySQL
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("Driver MySQL non trouvé", ex);
            }
        }

        // Ajout de paramètres de connexion pour meilleure compatibilité
        String connectionURL = URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        
        return DriverManager.getConnection(connectionURL, USER, PASSWORD);
    }
}