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
            
            System.out.println("DEBUG PaiementDAO: Recherche paiements pour usager " + idUsager);
            
            while (rs.next()) {
                Paiement paiement = new Paiement();
                paiement.setIdPaiement(rs.getString("id_paiement"));
                paiement.setNomCarte(rs.getString("nom_carte"));
                paiement.setNumeroCarte(rs.getString("numero_carte"));
                paiement.setCodeSecretCarte(rs.getString("code_secret_carte"));
                paiement.setMontant(rs.getDouble("montant"));
                paiement.setIdUsager(rs.getInt("id_usager"));
                paiement.setDatePaiement(rs.getTimestamp("date_paiement").toLocalDateTime());
                paiement.setMethodePaiement(rs.getString("methode_paiement"));
                paiement.setStatut(rs.getString("statut"));
                paiement.setIdAbonnement(rs.getString("id_abonnement"));
                
                // Déterminer le type
                if (paiement.getIdAbonnement() != null && !paiement.getIdAbonnement().isEmpty()) {
                    paiement.setTypePaiement("Abonnement");
                } else {
                    paiement.setTypePaiement("Stationnement");
                }
                
                System.out.println("DEBUG: Paiement trouvé - " + paiement.getIdPaiement() + 
                                 " - " + paiement.getMontant() + "€");
                
                paiements.add(paiement);
            }
            
            System.out.println("DEBUG PaiementDAO: " + paiements.size() + " paiements trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur dans getPaiementsByUsager:");
            e.printStackTrace();
        }
        return paiements;
    }
    
    /**
     * Enregistre un paiement
     * @param paiement L'objet Paiement à enregistrer
     * @return true si l'insertion a réussi, false sinon
     */
    public static boolean enregistrerPaiement(Paiement paiement) {
        String sql = "INSERT INTO Paiement (id_paiement, nom_carte, numero_carte, " +
                    "code_secret_carte, id_abonnement, montant, id_usager, date_paiement, " +
                    "methode_paiement, statut) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), 'CARTE', 'REUSSI')";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            System.out.println("=== ENREGISTREMENT PAIEMENT ===");
            System.out.println("ID Paiement: " + paiement.getIdPaiement());
            System.out.println("Nom carte: " + paiement.getNomCarte());
            System.out.println("Montant: " + paiement.getMontant());
            System.out.println("ID Usager: " + paiement.getIdUsager());
            System.out.println("ID Abonnement: " + paiement.getIdAbonnement());
            
            pstmt.setString(1, paiement.getIdPaiement());
            pstmt.setString(2, paiement.getNomCarte());
            pstmt.setString(3, paiement.getNumeroCarte());
            pstmt.setString(4, paiement.getCodeSecretCarte());
            
            // Gérer l'id_abonnement qui peut être null
            if (paiement.getIdAbonnement() != null && !paiement.getIdAbonnement().isEmpty()) {
                pstmt.setString(5, paiement.getIdAbonnement());
                System.out.println("Type: Abonnement");
            } else {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
                System.out.println("Type: Stationnement");
            }
            
            pstmt.setDouble(6, paiement.getMontant());
            pstmt.setInt(7, paiement.getIdUsager());
            
            int rows = pstmt.executeUpdate();
            System.out.println("✅ " + rows + " ligne(s) affectée(s)");
            
            return rows > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'enregistrement du paiement:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Test direct pour débogage
     * @param idUsager ID de l'utilisateur
     */
    public static void debugPaiements(int idUsager) {
        System.out.println("\n=== DEBUG Paiements pour usager " + idUsager + " ===");
        
        try (Connection conn = MySQLConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total FROM Paiement WHERE id_usager = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idUsager);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("Total paiements en base: " + total);
            }
            
            rs.close();
            pstmt.close();
            
            // Détails
            String sqlDetails = "SELECT id_paiement, montant, date_paiement, id_abonnement FROM Paiement WHERE id_usager = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sqlDetails);
            pstmt2.setInt(1, idUsager);
            ResultSet rs2 = pstmt2.executeQuery();
            
            int count = 0;
            while (rs2.next()) {
                count++;
                System.out.println("Paiement " + count + ":");
                System.out.println("  ID: " + rs2.getString("id_paiement"));
                System.out.println("  Montant: " + rs2.getDouble("montant") + "€");
                System.out.println("  Date: " + rs2.getTimestamp("date_paiement"));
                System.out.println("  Abonnement: " + rs2.getString("id_abonnement"));
            }
            
            rs2.close();
            pstmt2.close();
            
        } catch (SQLException e) {
            System.err.println("Erreur debug: " + e.getMessage());
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
}