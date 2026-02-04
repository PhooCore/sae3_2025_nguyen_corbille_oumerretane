package modele.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnection {
    private static String HOST;
    private static String DATABASE;
    private static String PORT;
    private static String USER;
    private static String PASSWORD;
    
    static {
        loadDatabaseConfig();
    }
    
    /**
     * Charge la configuration de la base de données depuis le fichier db.properties
     */
    private static void loadDatabaseConfig() {
        Properties props = new Properties();
        
        // Essayer de charger depuis le classpath
        try (InputStream input = MySQLConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Si pas trouvé dans classpath, essayer depuis le fichier système
                try (FileInputStream fileInput = new FileInputStream("db.properties")) {
                    props.load(fileInput);
                }
            }
            
            HOST = props.getProperty("db.host");
            DATABASE = props.getProperty("db.database");
            PORT = props.getProperty("db.port", "3306");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
            System.err.println("Veuillez créer un fichier db.properties à partir du template db.properties.template");
            throw new RuntimeException("Configuration de base de données introuvable", e);
        }
    }
    
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
        
        // Construction de l'URL de connexion
        String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;
        String connectionURL = URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        
        return DriverManager.getConnection(connectionURL, USER, PASSWORD);
    }
}
