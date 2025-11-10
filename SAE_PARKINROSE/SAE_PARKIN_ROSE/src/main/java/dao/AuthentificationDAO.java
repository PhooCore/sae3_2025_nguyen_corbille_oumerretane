package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthentificationDAO {
    
     //Vérifie si l'email et le mot de passe correspondent à un utilisateur
    public static boolean verifierUtilisateur(String email, String motDePasse) {
        String sql = "SELECT id_usager FROM Usager WHERE mail_usager = ? AND mot_de_passe = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, motDePasse);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Retourne true si un utilisateur correspond
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    

    //Récupère les informations de l'utilisateur
    public static String[] getInfosUtilisateur(String email) {
        String sql = "SELECT nom_usager, prenom_usager FROM Usager WHERE mail_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom_usager");
                    String prenom = rs.getString("prenom_usager");
                    return new String[]{nom, prenom};
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des infos: " + e.getMessage());
        }
        
        return null;
    }
}