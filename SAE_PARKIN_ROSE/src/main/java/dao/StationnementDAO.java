package dao;

import modèle.Stationnement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StationnementDAO {
    
    /**
     * Vérifie si un véhicule a déjà un stationnement actif
     * Utilisé pour empêcher la création de plusieurs stationnements simultanés pour un même véhicule
     * @param plaqueImmatriculation la plaque du véhicule à vérifier
     * @return true si le véhicule a un stationnement actif, false sinon
     */
    public static boolean vehiculeAStationnementActif(String plaqueImmatriculation) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaqueImmatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Retourne true si le compteur est supérieur à 0 (au moins un stationnement actif)
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Récupère le stationnement actif d'un véhicule spécifique
     * @param plaqueImmatriculation la plaque du véhicule
     * @return l'objet Stationnement actif, ou null si aucun stationnement actif
     */
    public static Stationnement getStationnementActif(String plaqueImmatriculation) {
        String sql = "SELECT * FROM Stationnement WHERE plaque_immatriculation = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, plaqueImmatriculation);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Convertit le ResultSet en objet Stationnement
                    return mapResultSetToStationnement(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Termine un stationnement en changeant son statut et en mettant à jour la date de fin
     * Appelé quand l'utilisateur stoppe manuellement son stationnement
     * @param idStationnement l'ID du stationnement à terminer
     * @return true si la mise à jour a réussi, false sinon
     */
    public static boolean terminerStationnement(int idStationnement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            
            int lignesmiseajour = stmt.executeUpdate();
            return lignesmiseajour > 0; // Retourne true si au moins une ligne a été mise à jour
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fin du stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Crée un nouveau stationnement dans la base de données
     * Calcule automatiquement la date de fin en fonction de la durée
     * @param stationnement l'objet Stationnement contenant toutes les informations
     * @return true si la création a réussi, false sinon
     */
    public static boolean creerStationnement(Stationnement stationnement) {
        String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
                    "zone, duree_heures, duree_minutes, cout, date_creation, date_fin, statut, id_paiement) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIF', ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Calcul de la durée totale en minutes pour la fonction DATE_ADD
            int dureeTotaleMinutes = stationnement.getDureeHeures() * 60 + stationnement.getDureeMinutes();
            
            // Remplissage des paramètres de la requête
            stmt.setInt(1, stationnement.getIdUsager());
            stmt.setString(2, stationnement.getTypeVehicule());
            stmt.setString(3, stationnement.getPlaqueImmatriculation());
            stmt.setString(4, stationnement.getZone());
            stmt.setInt(5, stationnement.getDureeHeures());
            stmt.setInt(6, stationnement.getDureeMinutes());
            stmt.setDouble(7, stationnement.getCout());
            stmt.setInt(8, dureeTotaleMinutes); // Durée totale pour DATE_ADD
            stmt.setString(9, stationnement.getIdPaiement());
            
            int ligneinseree = stmt.executeUpdate();
            
            if (ligneinseree > 0) {
                // Récupération de l'ID auto-généré pour l'objet Stationnement
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stationnement.setIdStationnement(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du stationnement: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Méthode utilitaire pour convertir un ResultSet en objet Stationnement
     * @param rs le ResultSet contenant les données de la base
     * @return un objet Stationnement rempli avec les données
     * @throws SQLException en cas d'erreur de lecture des données
     */
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        
        // Mapping de toutes les colonnes vers les propriétés de l'objet
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        stationnement.setZone(rs.getString("zone"));
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        stationnement.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
        
        // Gestion de la date de fin qui peut être nulle
        Timestamp dateFin = rs.getTimestamp("date_fin");
        if (dateFin != null) {
            stationnement.setDateFin(dateFin.toLocalDateTime());
        }
        
        stationnement.setStatut(rs.getString("statut"));
        stationnement.setIdPaiement(rs.getString("id_paiement"));
        return stationnement;
    }
    
    /**
     * Vérifie si un stationnement est vraiment actif (statut ACTIF ET date non dépassée)
     * @param idUsager l'ID de l'utilisateur
     * @return le stationnement actif valide, ou null si aucun ou expiré
     */
    public static Stationnement getStationnementActifValideByUsager(int idUsager) {
        String sql = "SELECT * FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF' AND date_fin > NOW() ORDER BY date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStationnement(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stationnement actif valide: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Met à jour automatiquement les stationnements expirés
     * Appeler cette méthode périodiquement ou au démarrage
     */
    public static void nettoyerStationnementsExpires() {
        String sql = "UPDATE Stationnement SET statut = 'EXPIRE' WHERE statut = 'ACTIF' AND date_fin <= NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int lignesmiseajour = stmt.executeUpdate();
            if (lignesmiseajour > 0) {
                System.out.println(lignesmiseajour + " stationnement(s) expiré(s) mis à jour");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage des stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
    }
}