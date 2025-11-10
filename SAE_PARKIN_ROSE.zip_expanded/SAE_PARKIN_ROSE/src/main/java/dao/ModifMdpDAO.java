

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModifMdpDAO {
    
    public boolean modifierMotDePasse(String email, String nouveauMotDePasse) {
        String sql = "UPDATE Usager SET mot_de_passe = ? WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nouveauMotDePasse);
            stmt.setString(2, email);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.err.println("Erreur modification mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean verifierEmailExiste(String email) {
        String sql = "SELECT mail_usager FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retourne true si l'email existe
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Méthode  pour vérifier l'ancien mot de passe
    public boolean verifierAncienMotDePasse(String email, String ancienMotDePasse) {
        String sql = "SELECT mail_usager FROM Usager WHERE mail_usager = ? AND mot_de_passe = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, ancienMotDePasse);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true si l'ancien mot de passe est correct
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification ancien mot de passe: " + e.getMessage());
            return false;
        }
    }
}