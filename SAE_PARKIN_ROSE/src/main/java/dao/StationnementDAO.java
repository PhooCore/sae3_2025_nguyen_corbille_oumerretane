package dao;

import modèle.Stationnement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StationnementDAO {
    
	/**
	 * Crée un nouveau stationnement en voirie
	 */
	public static boolean creerStationnementVoirie(int idUsager, String typeVehicule, String plaqueImmatriculation, 
	                                              String idZone, int dureeHeures, int dureeMinutes,  // Changé idTarification → idZone
	                                              double cout, String idPaiement) {
	    // CORRECTION : Utiliser id_zone au lieu de id_tarification
	    String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, id_zone, " +  // Changé ici
	                "duree_heures, duree_minutes, cout, date_creation, date_fin, statut, " +
	                "id_paiement, type_stationnement, statut_paiement) " +
	                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIF', ?, 'VOIRIE', 'PAYE')";
	    
	    try (Connection conn = MySQLConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        int dureeTotaleMinutes = (dureeHeures * 60) + dureeMinutes;
	        
	        stmt.setInt(1, idUsager);
	        stmt.setString(2, typeVehicule);
	        stmt.setString(3, plaqueImmatriculation);
	        stmt.setString(4, idZone);  // Maintenant à la position 4
	        
	        stmt.setInt(5, dureeHeures);
	        stmt.setInt(6, dureeMinutes);
	        stmt.setDouble(7, cout);
	        stmt.setInt(8, dureeTotaleMinutes);
	        stmt.setString(9, idPaiement);
	        
	        int lignesAffectees = stmt.executeUpdate();
	        return lignesAffectees > 0;
	        
	    } catch (SQLException e) {
	        System.err.println("Erreur création stationnement voirie: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
    
	/**
	 * Crée un nouveau stationnement en parking
	 */
	public static boolean creerStationnementParking(int idUsager, String typeVehicule, String plaqueImmatriculation,
	                                               String idParking, LocalDateTime heureArrivee) {
	    // CORRECTION : Supprimer la colonne 'zone' qui n'existe pas
	    String sql = "INSERT INTO Stationnement (id_usager, type_vehicule, plaque_immatriculation, " +
	                "heure_arrivee, type_stationnement, statut_paiement, statut) " +
	                "VALUES (?, ?, ?, ?, 'PARKING', 'NON_PAYE', 'ACTIF')";
	    
	    try (Connection conn = MySQLConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        
	        stmt.setInt(1, idUsager);
	        stmt.setString(2, typeVehicule);
	        stmt.setString(3, plaqueImmatriculation);
	        
	        // SUPPRIMER : Plus besoin de récupérer le libellé du parking
	        // stmt.setString(4, libelleParking != null ? libelleParking : idParking);
	        
	        stmt.setTimestamp(4, Timestamp.valueOf(heureArrivee)); // Maintenant à la position 4
	        
	        int lignesAffectees = stmt.executeUpdate();
	        return lignesAffectees > 0;
	        
	    } catch (SQLException e) {
	        System.err.println("Erreur création stationnement parking: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
    

    
    /**
     * Récupère le libellé du parking depuis son ID
     */
    private static String getLibelleParking(String idParking) {
        String sql = "SELECT libelle_parking FROM Parking WHERE id_parking = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idParking);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("libelle_parking");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération libellé parking: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère le stationnement actif d'un usager
     */
    public static Stationnement getStationnementActifByUsager(int idUsager) {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère le stationnement actif valide d'un usager (vérifie aussi les dates)
     */
    public static Stationnement getStationnementActifValideByUsager(int idUsager) {
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? AND s.statut = 'ACTIF' " +
                    "AND (s.date_fin IS NULL OR s.date_fin > NOW()) " +
                    "ORDER BY s.date_creation DESC LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement actif valide: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Termine un stationnement (pour voirie)
     */
    public static boolean terminerStationnement(int idStationnement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', date_fin = NOW() WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur terminaison stationnement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Termine un stationnement parking avec paiement
     */
    public static boolean terminerStationnementParking(int idStationnement, LocalDateTime heureDepart, 
                                                      double cout, String idPaiement) {
        String sql = "UPDATE Stationnement SET statut = 'TERMINE', heure_depart = ?, cout = ?, " +
                    "id_paiement = ?, statut_paiement = 'PAYE' WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(heureDepart));
            stmt.setDouble(2, cout);
            stmt.setString(3, idPaiement);
            stmt.setInt(4, idStationnement);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur terminaison stationnement parking: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère l'historique des stationnements d'un usager
     */
    public static List<Stationnement> getHistoriqueStationnements(int idUsager) {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.id_usager = ? ORDER BY s.date_creation DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Stationnement stationnement = mapResultSetToStationnement(rs);
                stationnements.add(stationnement);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération historique: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
    
    /**
     * Nettoie les stationnements expirés
     */
    public static void nettoyerStationnementsExpires() {
        String sql = "UPDATE Stationnement SET statut = 'EXPIRE' WHERE statut = 'ACTIF' AND " +
                    "date_fin IS NOT NULL AND date_fin < NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println(rowsUpdated + " stationnement(s) expiré(s) nettoyé(s)");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur nettoyage stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère un stationnement par son ID
     */
    public static Stationnement getStationnementById(int idStationnement) {
        String sql = "SELECT s.* FROM Stationnement s WHERE s.id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idStationnement);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToStationnement(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnement par ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Vérifie si un usager a déjà un stationnement actif
     */
    public static boolean hasStationnementActif(int idUsager) {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE id_usager = ? AND statut = 'ACTIF'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsager);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur vérification stationnement actif: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Méthode utilitaire pour mapper un ResultSet vers un objet Stationnement
     */
    private static Stationnement mapResultSetToStationnement(ResultSet rs) throws SQLException {
        Stationnement stationnement = new Stationnement();
        
        stationnement.setIdStationnement(rs.getInt("id_stationnement"));
        stationnement.setIdUsager(rs.getInt("id_usager"));
        
        // CORRECTION : Utiliser id_zone au lieu de id_tarification
        stationnement.setIdTarification(rs.getString("id_zone")); // Changé ici
        
        stationnement.setTypeVehicule(rs.getString("type_vehicule"));
        stationnement.setPlaqueImmatriculation(rs.getString("plaque_immatriculation"));
        
        // SUPPRIMER : La colonne zone n'existe pas
        // stationnement.setZone(rs.getString("zone"));
        
        stationnement.setDureeHeures(rs.getInt("duree_heures"));
        stationnement.setDureeMinutes(rs.getInt("duree_minutes"));
        stationnement.setCout(rs.getDouble("cout"));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            stationnement.setDateCreation(dateCreation.toLocalDateTime());
        }
        
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
    
    /**
     * Met à jour le statut de paiement d'un stationnement
     */
    public static boolean mettreAJourStatutPaiement(int idStationnement, String idPaiement, String statutPaiement) {
        String sql = "UPDATE Stationnement SET id_paiement = ?, statut_paiement = ? WHERE id_stationnement = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idPaiement);
            stmt.setString(2, statutPaiement);
            stmt.setInt(3, idStationnement);
            
            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut paiement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Récupère les stationnements actifs expirés
     */
    public static List<Stationnement> getStationnementsExpires() {
        List<Stationnement> stationnements = new ArrayList<>();
        String sql = "SELECT s.* FROM Stationnement s " +
                    "WHERE s.statut = 'ACTIF' AND s.date_fin IS NOT NULL AND s.date_fin < NOW()";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Stationnement stationnement = mapResultSetToStationnement(rs);
                stationnements.add(stationnement);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur récupération stationnements expirés: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stationnements;
    }
}