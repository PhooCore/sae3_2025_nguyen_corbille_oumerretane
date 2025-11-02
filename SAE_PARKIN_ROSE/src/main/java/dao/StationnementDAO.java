package dao;

import modèle.Stationnement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StationnementDAO {
    
    private static final Logger logger = Logger.getLogger(StationnementDAO.class.getName());
    
    /**
     * Vérifie si un usager a déjà un stationnement actif
     */
    public static boolean usagerAStationnementActif(int idUsager) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la vérification du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Récupère le stationnement actif d'un usager
     */
    public static Stationnement getStationnementActifByUsager(int idUsager) {
        String sql = "SELECT * FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStationnement(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la récupération du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Récupère le stationnement actif valide d'un usager
     */
    public static Stationnement getStationnementActifValideByUsager(int idUsager) {
        return getStationnementActifByUsager(idUsager);
    }
    
    /**
     * Termine un stationnement en changeant son statut
     */
    public static boolean terminerStationnement(int idStationnement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            
            int lignesmiseajour = stmt.executeUpdate();
            return lignesmiseajour > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la fin du stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Termine un stationnement parking et met à jour les informations
     */
    public static boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart, double cout, String idPaiement) {
        String sql = "UPDATE Stationnement SET heure_depart = ?, cout = ?, id_paiement = ?, " +
                    "statut_paiement = 'PAYE', statut = 'TERMINE' WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
            stmt.setDouble(2, cout);
            stmt.setString(3, idPaiement);
            stmt.setInt(4, idStationnement);
            
            int lignesmiseajour = stmt.executeUpdate();
            return lignesmiseajour > 0;
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la fin du stationnement parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée un stationnement en parking (sans paiement immédiat)
     */
    public static boolean creerStationnementParking(Stationnement stationnement) {
        // Vérifier que l'usager n'a pas déjà un stationnement actif
        if (usagerAStationnementActif(stationnement.getIdUsager())) {
            logger.warning("L'usager " + stationnement.getIdUsager() + " a déjà un stationnement actif");
            return false;
        }
        
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "zone, date_creation, statut, type_stationnement, statut_paiement, heure_arrivee, " +
                    "cout, duree_heures, duree_minutes) " +
                    "VALUES (?, ?, ?, ?, NOW(), 'ACTIF', 'PARKING', 'NON_PAYE', ?, 0.00, 0, 0)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, stationnement.getIdUsager());
            stmt.setString(2, stationnement.getTypeVehicule());
            stmt.setString(3, stationnement.getPlaqueImmatriculation());
            stmt.setString(4, stationnement.getZone());
            stmt.setTimestamp(5, Timestamp.valueOf(stationnement.getHeureArrivee()));
            
            int ligneinseree = stmt.executeUpdate();
            
            if (ligneinseree > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stationnement.setIdStationnement(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la création du stationnement parking: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Récupère tous les stationnements d'un usager (historique)
     */
    public static List<Stationnement> getHistoriqueStationnements(int idUsager) {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT * FROM Stationnement WHERE id_usager = ? ORDER BY date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stationnements.add(mapResultSetToStationnement(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors de la récupération de l'historique: " + e.getMessage());
            e.printStackTrace();
        }
        return stationnements;
    }
    
    /**
     * Met à jour automatiquement les stationnements expirés
     */
    public static void nettoyerStationnementsExpires() {
        String sql = "UPDATE Stationnement SET statut = 'EXPIRE' WHERE statut = 'ACTIF' AND date_fin <= NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int lignesmiseajour = stmt.executeUpdate();
            if (lignesmiseajour > 0) {
                logger.info(lignesmiseajour + " stationnement(s) expiré(s) mis à jour");
            }
            
        } catch (SQLException e) {
            logger.severe("Erreur lors du nettoyage des stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Méthode utilitaire pour convertir un ResultSet en objet Stationnement
     */
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setZone(rs.getString("zone"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        stationnement.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        Timestamp dateFin = rs.getTimestamp("date_fin");
        if (dateFin != null) {
            stationnement.setDateFin(dateFin.toLocalDateTime());
        }
        
        Timestamp heureArrivee = rs.getTimestamp("heure_arrivee");
        if (heureArrivee != null) {
            stationnement.setHeureArrivee(heureArrivee.toLocalDateTime());
        }
        
        Timestamp heureDepart = rs.getTimestamp("heure_depart");
        if (heureDepart != null) {
            stationnement.setHeureDepart(heureDepart.toLocalDateTime());
        }
        
        stationnement.setStatut(rs.getString("statut"));
        stationnement.setTypeStationnement(rs.getString("type_stationnement"));
        stationnement.setStatutPaiement(rs.getString("statut_paiement"));
        stationnement.setIdPaiement(rs.getString("id_paiement"));
        
        return stationnement;
    }
}