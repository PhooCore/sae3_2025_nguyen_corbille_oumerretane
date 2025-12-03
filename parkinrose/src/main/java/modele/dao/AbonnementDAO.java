package modele.dao;

import modele.Abonnement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementDAO {
    
    /**
     * Récupère tous les abonnements disponibles
     * @return Liste de tous les abonnements
     */
    public static List<Abonnement> getAllAbonnements() {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM Abonnement ORDER BY tarif_applique";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Abonnement abonnement = new Abonnement("", "", 0);
                abonnement.setIdAbonnement(rs.getString("id_abonnement"));
                abonnement.setLibelleAbonnement(rs.getString("libelle_abonnement"));
                abonnement.setTarifAbonnement(rs.getDouble("tarif_applique"));
                abonnements.add(abonnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }
    
    /**
     * Récupère les abonnements d'un utilisateur spécifique
     * @param idUsager ID de l'utilisateur
     * @return Liste des abonnements de l'utilisateur
     */
    public static List<Abonnement> getAbonnementsByUsager(int idUsager) {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT a.* FROM Abonnement a " +
                     "INNER JOIN Appartenir ap ON a.id_abonnement = ap.id_abonnement " +
                     "WHERE ap.id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Abonnement abonnement = new Abonnement();
                abonnement.setIdAbonnement(rs.getString("id_abonnement"));
                abonnement.setLibelleAbonnement(rs.getString("libelle_abonnement"));
                abonnement.setTarifAbonnement(rs.getDouble("tarif_applique"));
                abonnements.add(abonnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }
    
    /**
     * Récupère un abonnement par son ID
     * @param idAbonnement ID de l'abonnement
     * @return L'abonnement correspondant, ou null si non trouvé
     */
    public static Abonnement getAbonnementById(String idAbonnement) {
        Abonnement abonnement = null;
        String sql = "SELECT * FROM Abonnement WHERE id_abonnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, idAbonnement);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                abonnement = new Abonnement();
                abonnement.setIdAbonnement(rs.getString("id_abonnement"));
                abonnement.setLibelleAbonnement(rs.getString("libelle_abonnement"));
                abonnement.setTarifAbonnement(rs.getDouble("tarif_applique"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnement;
    }
    
    /**
     * Vérifie si un utilisateur a un abonnement spécifique
     * @param idUsager ID de l'utilisateur
     * @param idAbonnement ID de l'abonnement
     * @return true si l'utilisateur a l'abonnement, false sinon
     */
    public static boolean hasAbonnement(int idUsager, String idAbonnement) {
        String sql = "SELECT COUNT(*) FROM Appartenir WHERE id_usager = ? AND id_abonnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            pstmt.setString(2, idAbonnement);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Ajoute un nouvel abonnement (pour l'admin)
     * @param abonnement L'abonnement à ajouter
     * @return true si l'insertion a réussi, false sinon
     */
    public static boolean insert(Abonnement abonnement) {
        String sql = "INSERT INTO Abonnement (id_abonnement, libelle_abonnement, tarif_applique) VALUES (?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, abonnement.getIdAbonnement());
            pstmt.setString(2, abonnement.getLibelleAbonnement());
            pstmt.setDouble(3, abonnement.getTarifAbonnement());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Met à jour un abonnement existant (pour l'admin)
     * @param abonnement L'abonnement à mettre à jour
     * @return true si la mise à jour a réussi, false sinon
     */
    public static boolean update(Abonnement abonnement) {
        String sql = "UPDATE Abonnement SET libelle_abonnement = ?, tarif_applique = ? WHERE id_abonnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, abonnement.getLibelleAbonnement());
            pstmt.setDouble(2, abonnement.getTarifAbonnement());
            pstmt.setString(3, abonnement.getIdAbonnement());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Supprime un abonnement (pour l'admin)
     * @param idAbonnement ID de l'abonnement à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean delete(String idAbonnement) {
        // D'abord supprimer les relations dans Appartenir
        String sqlDeleteAppartenir = "DELETE FROM Appartenir WHERE id_abonnement = ?";
        String sqlDeleteAbonnement = "DELETE FROM Abonnement WHERE id_abonnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false); // Début de la transaction
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteAppartenir);
                 PreparedStatement pstmt2 = conn.prepareStatement(sqlDeleteAbonnement)) {
                
                // Supprimer les relations
                pstmt1.setString(1, idAbonnement);
                pstmt1.executeUpdate();
                
                // Supprimer l'abonnement
                pstmt2.setString(1, idAbonnement);
                int rowsAffected = pstmt2.executeUpdate();
                
                conn.commit(); // Valider la transaction
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                conn.rollback(); // Annuler la transaction en cas d'erreur
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère les abonnements par type (filtrage)
     * @param type Type d'abonnement (facultatif)
     * @return Liste d'abonnements filtrés
     */
    public static List<Abonnement> getAbonnementsByType(String type) {
        List<Abonnement> abonnements = new ArrayList<>();
        String sql = "SELECT * FROM Abonnement WHERE 1=1";
        
        if (type != null && !type.isEmpty()) {
            sql += " AND id_abonnement LIKE ?";
        }
        sql += " ORDER BY tarif_applique";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (type != null && !type.isEmpty()) {
                pstmt.setString(1, type + "%");
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Abonnement abonnement = new Abonnement();
                abonnement.setIdAbonnement(rs.getString("id_abonnement"));
                abonnement.setLibelleAbonnement(rs.getString("libelle_abonnement"));
                abonnement.setTarifAbonnement(rs.getDouble("tarif_applique"));
                abonnements.add(abonnement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return abonnements;
    }
    
    /**
     * Ajoute un abonnement à un utilisateur
     * @param idUsager ID de l'utilisateur
     * @param idAbonnement ID de l'abonnement
     * @return true si l'ajout a réussi, false sinon
     */
    public static boolean ajouterAbonnementUtilisateur(int idUsager, String idAbonnement) {
        // D'abord supprimer les anciens abonnements
        String sqlDelete = "DELETE FROM Appartenir WHERE id_usager = ?";
        String sqlInsert = "INSERT INTO Appartenir (id_usager, id_abonnement, date_debut) VALUES (?, ?, CURDATE())";
        
        try (Connection conn = MySQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete);
                 PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                
                // Supprimer les anciens abonnements
                pstmtDelete.setInt(1, idUsager);
                pstmtDelete.executeUpdate();
                
                // Ajouter le nouvel abonnement
                pstmtInsert.setInt(1, idUsager);
                pstmtInsert.setString(2, idAbonnement);
                int rowsAffected = pstmtInsert.executeUpdate();
                
                conn.commit();
                return rowsAffected > 0;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime tous les abonnements d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean supprimerAbonnementsUtilisateur(int idUsager) {
        String sql = "DELETE FROM Appartenir WHERE id_usager = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected >= 0; // Retourne true même si aucun abonnement à supprimer
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}