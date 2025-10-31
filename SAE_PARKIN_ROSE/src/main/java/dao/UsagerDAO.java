
package dao;

import modèle.Usager;
import java.sql.*;

public class UsagerDAO {
    
    public static boolean ajouterUsager(Usager usager) {
        String sql = "INSERT INTO Usager (nom_usager, prenom_usager, mail_usager, mot_de_passe) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usager.getNomUsager());
            stmt.setString(2, usager.getPrenomUsager());
            stmt.setString(3, usager.getMailUsager());
            stmt.setString(4, usager.getMotDePasse());

            int rowsInserted = stmt.executeUpdate();
            
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usager.setIdUsager(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'usager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean emailExisteDeja(String email) {
        String sql = "SELECT mail_usager FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            return false;
        }
    }

    public static Usager getUsagerByEmail(String email) {
        String sql = "SELECT * FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            System.out.println("Recherche usager avec email: " + email); // Debug
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usager usager = new Usager();
                    usager.setIdUsager(rs.getInt("id_usager"));
                    usager.setNomUsager(rs.getString("nom_usager"));
                    usager.setPrenomUsager(rs.getString("prenom_usager"));
                    usager.setMailUsager(rs.getString("mail_usager"));
                    usager.setMotDePasse(rs.getString("mot_de_passe"));
                    System.out.println("Usager trouvé: " + usager.getNomUsager() + " (ID: " + usager.getIdUsager() + ")"); // Debug
                    return usager;
                } else {
                    System.out.println("Aucun usager trouvé pour cet email"); // Debug
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'usager: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
