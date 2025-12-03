package modele.dao;

import modele.Paiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {
    
    /**
     * Récupère tous les paiements d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des paiements de l'utilisateur
     */
    public static List<Paiement> getPaiementsByUsager(int idUsager) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? ORDER BY date_paiement DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Paiement paiement = new Paiement();
                paiement.setIdPaiement(rs.getString("id_paiement"));
                paiement.setMontant(rs.getDouble("montant"));
                paiement.setIdUsager(rs.getInt("id_usager"));
                paiement.setDatePaiement(rs.getTimestamp("date_paiement").toLocalDateTime());
                paiement.setMethodePaiement(rs.getString("methode_paiement"));
                paiement.setStatut(rs.getString("statut"));
                
                // Gérer le type de paiement (abonnement ou stationnement)
                if (rs.getString("id_abonnement") != null) {
                    paiement.setTypePaiement("Abonnement");
                    paiement.setIdAbonnement(rs.getString("id_abonnement"));
                } else {
                    paiement.setTypePaiement("Stationnement");
                }
                
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }
    
    /**
     * Enregistre un nouveau paiement
     * @param paiement L'objet Paiement à enregistrer
     * @return true si l'insertion a réussi, false sinon
     */
    public static boolean insert(Paiement paiement) {
        String sql = "INSERT INTO Paiement (id_paiement, id_usager, montant, methode_paiement, " +
                    "date_paiement, statut, id_abonnement, type_paiement) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, paiement.getIdPaiement());
            pstmt.setInt(2, paiement.getIdUsager());
            pstmt.setDouble(3, paiement.getMontant());
            pstmt.setString(4, paiement.getMethodePaiement());
            pstmt.setTimestamp(5, Timestamp.valueOf(paiement.getDatePaiement()));
            pstmt.setString(6, paiement.getStatut());
            pstmt.setString(7, paiement.getIdAbonnement());
            pstmt.setString(8, paiement.getTypePaiement());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère les paiements d'abonnement d'un utilisateur
     * @param idUsager ID de l'utilisateur
     * @return Liste des paiements d'abonnement
     */
    public static List<Paiement> getPaiementsAbonnementByUsager(int idUsager) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM Paiement WHERE id_usager = ? AND id_abonnement IS NOT NULL ORDER BY date_paiement DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Paiement paiement = new Paiement();
                paiement.setIdPaiement(rs.getString("id_paiement"));
                paiement.setMontant(rs.getDouble("montant"));
                paiement.setIdUsager(rs.getInt("id_usager"));
                paiement.setDatePaiement(rs.getTimestamp("date_paiement").toLocalDateTime());
                paiement.setMethodePaiement(rs.getString("methode_paiement"));
                paiement.setStatut(rs.getString("statut"));
                paiement.setIdAbonnement(rs.getString("id_abonnement"));
                paiement.setTypePaiement("Abonnement");
                
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paiements;
    }

    /**
     * Enregistre un paiement (alias de la méthode insert pour compatibilité)
     * @param paiement L'objet Paiement à enregistrer
     * @return true si l'insertion a réussi, false sinon
     */
    public static boolean enregistrerPaiement(Paiement paiement) {
        return insert(paiement);
    }
}