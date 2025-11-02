package dao;

import modèle.Paiement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class PaiementDAO {
    
    private static final Logger logger = Logger.getLogger(PaiementDAO.class.getName());
    
    /**
     * Enregistre un paiement dans la base de données
     */
    public static boolean enregistrerPaiement(Paiement paiement) {
        String sql = "INSERT INTO paiement (id_paiement, nom_carte, numero_carte, code_secret_carte, " +
                    "id_abonnement, montant, id_usager, methode_paiement, statut) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Générer un ID unique si non fourni
            if (paiement.getIdPaiement() == null || paiement.getIdPaiement().isEmpty()) {
                paiement.setIdPaiement(genererIdPaiement());
            }
            
            stmt.setString(1, paiement.getIdPaiement());
            stmt.setString(2, paiement.getNomCarte());
            stmt.setString(3, paiement.getNumeroCarte());
            stmt.setString(4, paiement.getCodeSecretCarte());
            stmt.setString(5, paiement.getIdAbonnement());
            stmt.setDouble(6, paiement.getMontant());
            stmt.setInt(7, paiement.getIdUsager());
            stmt.setString(8, "CARTE");
            stmt.setString(9, "REUSSI");
            
            int ligneinseree = stmt.executeUpdate();
            return ligneinseree > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de l'enregistrement du paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée un stationnement après un paiement réussi (pour voirie)
     */
    public static boolean creerStationnementApresPaiement(int idUsager, String typeVehicule, 
                                                         String plaqueImmatriculation, String zone,
                                                         int dureeHeures, int dureeMinutes, double cout,
                                                         String idPaiement) {
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "zone, duree_heures, duree_minutes, cout, date_creation, date_fin, statut, " +
                    "id_paiement, type_stationnement, statut_paiement) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIF', ?, 'VOIRIE', 'PAYE')";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int dureeTotaleMinutes = dureeHeures * 60 + dureeMinutes;
            
            stmt.setInt(1, idUsager);
            stmt.setString(2, typeVehicule);
            stmt.setString(3, plaqueImmatriculation);
            stmt.setString(4, zone);
            stmt.setInt(5, dureeHeures);
            stmt.setInt(6, dureeMinutes);
            stmt.setDouble(7, cout);
            stmt.setInt(8, dureeTotaleMinutes);
            stmt.setString(9, idPaiement);
            
            int ligneinseree = stmt.executeUpdate();
            return ligneinseree > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la création du stationnement après paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère tous les paiements d'un usager
     */
    public static List<Paiement> getPaiementsByUsager(int idUsager) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE id_usager = ? ORDER BY date_paiement DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Paiement paiement = mapResultSetToPaiement(rs);
                    paiements.add(paiement);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la récupération des paiements: " + e.getMessage());
            e.printStackTrace();
        }
        return paiements;
    }
    
    /**
     * Génère un ID de paiement unique
     */
    private static String genererIdPaiement() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Récupère un paiement par son ID
     */
    public static Paiement getPaiementById(String idPaiement) {
        String sql = "SELECT * FROM paiement WHERE id_paiement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idPaiement);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaiement(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la récupération du paiement: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Méthode utilitaire pour convertir un ResultSet en objet Paiement
     */
    private static Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Paiement paiement = new Paiement(
            rs.getString("nom_carte"),
            rs.getString("numero_carte"),
            rs.getString("code_secret_carte"),
            rs.getDouble("montant"),
            rs.getInt("id_usager")
        );
        
        paiement.setIdPaiement(rs.getString("id_paiement"));
        paiement.setIdAbonnement(rs.getString("id_abonnement"));
        paiement.setMethodePaiement(rs.getString("methode_paiement"));
        paiement.setStatut(rs.getString("statut"));
        
        // Gérer la date de paiement
        Timestamp datePaiement = rs.getTimestamp("date_paiement");
        if (datePaiement != null) {
            paiement.setDatePaiement(datePaiement.toLocalDateTime());
        }
        
        return paiement;
    }
}